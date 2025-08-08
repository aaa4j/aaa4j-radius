/*
 * Copyright 2020 The AAA4J-RADIUS Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aaa4j.radius.server.servers;

import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.packet.IncrementingPacketIdGenerator;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodec;
import org.aaa4j.radius.core.packet.PacketCodecException;
import org.aaa4j.radius.core.util.SecureRandomProvider;
import org.aaa4j.radius.server.DeduplicationCache;
import org.aaa4j.radius.server.RadiusServer;
import org.aaa4j.radius.server.TimedDeduplicationCache;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

abstract class AbstractRadiusServer implements RadiusServer {

    static final int MAX_PACKET_SIZE = 4096;

    static final Supplier<DeduplicationCache> DEFAULT_DEDUPLICATION_CACHE_SUPPLIER = () ->
            new TimedDeduplicationCache(Duration.ofSeconds(30));

    final CountDownLatch startCountDownLatch = new CountDownLatch(1);

    final CountDownLatch stopCountDownLatch = new CountDownLatch(1);

    final InetSocketAddress bindAddress;

    final Executor executor;

    final boolean isInternalExecutor;

    final Dictionary dictionary;

    final String threadNamePrefix;

    final PacketCodec packetCodec;

    /**
     * Indicates that the server is listening for and processing new requests.
     */
    volatile boolean isRunning = false;

    boolean isStarted = false;

    boolean isStopped = false;

    Thread listenerThread;

    public AbstractRadiusServer(Builder<?, ?> builder, String threadNamePrefix) {
        this.bindAddress = Objects.requireNonNull(builder.bindAddress);

        if (builder.executor != null) {
            this.executor = builder.executor;
            this.isInternalExecutor = false;
        }
        else {
            this.executor = Executors.newCachedThreadPool(new ThreadFactory() {
                private final AtomicLong threadNumber = new AtomicLong(1);

                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable,
                            String.format("%s-worker-%s", threadNamePrefix, threadNumber.getAndIncrement()));
                    thread.setDaemon(false);

                    return thread;
                }
            });
            this.isInternalExecutor = true;
        }

        this.dictionary = builder.dictionary == null ? new StandardDictionary() : builder.dictionary;

        this.threadNamePrefix = Objects.requireNonNull(threadNamePrefix);

        this.packetCodec = new PacketCodec(dictionary, new SecureRandomProvider(),
                new IncrementingPacketIdGenerator(1));
    }

    @Override
    public final synchronized void start() throws InterruptedException {
        if (!isStarted && !isStopped) {
            isRunning = true;

            listenerThread = new Thread(this::listen, String.format("%s-listener", threadNamePrefix));
            listenerThread.setDaemon(false);
            listenerThread.start();

            isStarted = true;
        }

        startCountDownLatch.await();
    }

    @Override
    public final synchronized void stop() throws InterruptedException {
        if (isStarted && !isStopped && isRunning) {
            isRunning = false;

            // Close sockets and other resources
            close();

            listenerThread.interrupt();

            isStopped = true;
        }

        stopCountDownLatch.await();
    }

    @Override
    public final boolean isRunning() {
        return isRunning;
    }

    byte[] processRequest(InetSocketAddress clientSocketAddress, DeduplicationCache deduplicationCache, byte[] secret,
                          byte[] requestPacketBytes, PacketProcessor packetProcessor)
            throws PacketCodecException
    {
        byte[] responsePacketBytes = null;

        Packet requestPacket = packetCodec.decodeRequest(requestPacketBytes, secret);

        // Check the duplication cache for a cached response
        DeduplicationCache.Result result = deduplicationCache.handleRequest(clientSocketAddress,
                requestPacketBytes);

        switch (result.getState()) {
            case NEW_REQUEST:
                // The response will be generated since it's a new request
                try {
                    Packet responsePacket = packetProcessor.process(requestPacket);

                    if (responsePacket != null) {
                        responsePacketBytes = packetCodec.encodeResponse(responsePacket, secret,
                                requestPacket.getReceivedFields().getIdentifier(),
                                requestPacket.getReceivedFields().getAuthenticator());

                        deduplicationCache.handleResponse(clientSocketAddress, requestPacketBytes,
                                responsePacketBytes);
                    }
                }
                catch (Throwable e) {
                    deduplicationCache.unhandleRequest(clientSocketAddress, requestPacketBytes);

                    throw e;
                }
                break;

            case IN_PROGRESS_REQUEST:
                // Ignore the request since it's a duplicate of one that's being handled
                break;

            case CACHED_RESPONSE:
                responsePacketBytes = result.getResponsePacket();
                break;
        }

        return responsePacketBytes;
    }

    /**
     * Closes all resources.
     */
    abstract void close();

    /**
     * Starts listening for RADIUS requests. The method is run in a separate listener thread. Implementations count down
     * on {@link #startCountDownLatch} when the thread has started listening. Implementations count down on
     * {@link #startCountDownLatch} and {@link #stopCountDownLatch} when the method returns.
     */
    abstract void listen();

    @FunctionalInterface
    interface PacketProcessor {

        Packet process(Packet packet);

    }

    /**
     * Builder for {@link AbstractRadiusServer}.
     *
     * @param <T> the concrete type of {@link AbstractRadiusServer}
     * @param <B> the builder for {@link T}
     */
    static abstract class Builder<T extends AbstractRadiusServer, B extends Builder<T, B>> {

        InetSocketAddress bindAddress;

        Executor executor;

        Dictionary dictionary;

        Supplier<DeduplicationCache> deduplicationCacheSupplier;

        /**
         * Sets the address to bind the server to. Required.
         *
         * @param bindAddress the address to bind to
         *
         * @return this builder
         */
        public B bindAddress(InetSocketAddress bindAddress) {
            this.bindAddress = bindAddress;

            @SuppressWarnings("unchecked")
            B builder = (B) this;

            return builder;
        }

        /**
         * Sets the executor used to run the handling of RADIUS clients and requests. Optional. When not set, the common
         * ForkJoinPool will be used.
         *
         * @param executor the executor to use
         *
         * @return this builder
         */
        public B executor(Executor executor) {
            this.executor = executor;

            @SuppressWarnings("unchecked")
            B builder = (B) this;

            return builder;
        }

        /**
         * Sets the {@link Dictionary} to use. Optional. When not set, the standard dictionary will be used.
         *
         * @param dictionary the dictionary to use
         *
         * @return this builder
         */
        public B dictionary(Dictionary dictionary) {
            this.dictionary = dictionary;

            @SuppressWarnings("unchecked")
            B builder = (B) this;

            return builder;
        }

        /**
         * Sets the duplication strategy. Optional. When not set, a default duplication strategy that caches responses
         * for 30 seconds will be used.
         *
         * @param deduplicationCacheSupplier the duplication cache supplier
         *
         * @return this builder
         */
        public B deduplicationCacheSupplier(Supplier<DeduplicationCache> deduplicationCacheSupplier) {
            this.deduplicationCacheSupplier = deduplicationCacheSupplier;

            @SuppressWarnings("unchecked")
            B builder = (B) this;

            return builder;
        }

        public abstract T build();

    }

}
