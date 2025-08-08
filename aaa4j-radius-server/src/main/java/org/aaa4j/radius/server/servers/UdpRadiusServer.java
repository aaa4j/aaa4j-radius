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
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.server.DeduplicationCache;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A RADIUS server using UDP as the underlying transport layer.
 *
 * <p>
 * Build an instance of {@link UdpRadiusServer} by using a {@link Builder} object retrieved from {@link #newBuilder()}.
 * </p>
 */
public final class UdpRadiusServer extends AbstractRadiusServer {

    private static final String THREAD_NAME_PREFIX = "aaa4j-radius-server-udp";

    private static final AtomicLong SERVER_ID_COUNTER = new AtomicLong(1);

    private final DeduplicationCache deduplicationCache;

    private final Handler handler;

    private DatagramSocket serverSocket;

    private UdpRadiusServer(Builder builder) {
        super(builder, String.format("%s-%d", THREAD_NAME_PREFIX, SERVER_ID_COUNTER.getAndIncrement()));

        this.deduplicationCache = builder.deduplicationCacheSupplier == null
                ? DEFAULT_DEDUPLICATION_CACHE_SUPPLIER.get() : builder.deduplicationCacheSupplier.get();
        this.handler = Objects.requireNonNull(builder.handler);
    }

    /**
     * Creates a new builder object.
     *
     * @return a new builder object
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    void close() {
        try {
            startCountDownLatch.await();
        }
        catch (InterruptedException ignored) {
            // Ignored
        }

        if (serverSocket != null) {
            serverSocket.close();
        }

        if (isInternalExecutor) {
            ((ExecutorService) executor).shutdown();
        }

        deduplicationCache.clear();
    }

    @Override
    void listen() {
        try (DatagramSocket serverSocket = new DatagramSocket(bindAddress)) {
            this.serverSocket = serverSocket;

            startCountDownLatch.countDown();

            while (isRunning) {
                byte[] buffer = new byte[MAX_PACKET_SIZE];
                DatagramPacket requestDatagramPacket = new DatagramPacket(buffer, buffer.length);

                // Block and wait
                serverSocket.receive(requestDatagramPacket);

                // Handle the request
                executor.execute(() -> handleRequest(serverSocket, requestDatagramPacket));
            }
        }
        catch (Throwable e) {
            if (isRunning) {
                try {
                    handler.handleException(e);
                }
                catch (Exception ignored) {
                    // Ignored
                }
            }
        }
        finally {
            startCountDownLatch.countDown();

            if (isRunning) {
                close();

                isRunning = false;
            }

            stopCountDownLatch.countDown();
        }
    }

    private void handleRequest(DatagramSocket serverSocket, DatagramPacket requestDatagramPacket) {
        try {
            InetSocketAddress clientInetSocketAddress = (InetSocketAddress) requestDatagramPacket.getSocketAddress();

            byte[] secret = handler.handleClient(clientInetSocketAddress);

            if (secret == null) {
                // The handler doesn't want to handle requests from this client
                return;
            }

            byte[] requestPacketBytes = new byte[requestDatagramPacket.getLength()];
            System.arraycopy(requestDatagramPacket.getData(), 0, requestPacketBytes, 0,
                    requestDatagramPacket.getLength());

            // Perform deduplication and get a response from the handler
            byte[] responsePacketBytes = processRequest(clientInetSocketAddress, deduplicationCache, secret,
                    requestPacketBytes, (Packet packet) -> handler.handlePacket(clientInetSocketAddress, packet));

            if (responsePacketBytes != null) {
                DatagramPacket responseDatagramPacket = new DatagramPacket(responsePacketBytes,
                        responsePacketBytes.length, clientInetSocketAddress);

                // Send the response
                serverSocket.send(responseDatagramPacket);
            }
        }
        catch (Throwable e) {
            try {
                handler.handleException(e);
            }
            catch (Exception ignored) {
                // Ignored
            }
        }
    }

    /**
     * A UDP RADIUS server handler.
     */
    public interface Handler {

        /**
         * Handles a client.
         *
         * @param clientSocketAddress the client socket address
         *
         * @return the shared secret to use for the client or {@code null} if the client should be ignored
         */
        byte[] handleClient(InetSocketAddress clientSocketAddress);

        /**
         * Handles an incoming RADIUS request packet.
         *
         * @param clientSocketAddress the client socket address
         * @param requestPacket the request packet
         *
         * @return the response packet or {@code null} if no response should be sent back
         */
        Packet handlePacket(InetSocketAddress clientSocketAddress, Packet requestPacket);

        /**
         * Handles thrown exceptions.
         *
         * @param throwable the exception that was thrown
         */
        default void handleException(Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    /**
     * Builder for {@link UdpRadiusServer}s.
     */
    public final static class Builder extends AbstractRadiusServer.Builder<UdpRadiusServer, UdpRadiusServer.Builder> {

        Handler handler;

        /**
         * {@inheritDoc}
         */
        public Builder bindAddress(InetSocketAddress bindAddress) {
            return super.bindAddress(bindAddress);
        }

        /**
         * {@inheritDoc}
         */
        public Builder executor(Executor executor) {
            return super.executor(executor);
        }

        /**
         * {@inheritDoc}
         */
        public Builder dictionary(Dictionary dictionary) {
            return super.dictionary(dictionary);
        }

        /**
         * {@inheritDoc}
         */
        public Builder deduplicationCacheSupplier(Supplier<DeduplicationCache> deduplicationCacheSupplier) {
            return super.deduplicationCacheSupplier(deduplicationCacheSupplier);
        }

        /**
         * Sets the server handler. Required.
         *
         * @param handler the handler to use
         *
         * @return this builder
         */
        public Builder handler(Handler handler) {
            this.handler = handler;

            return this;
        }

        /**
         * Returns a new {@link UdpRadiusServer} built using the builder's options.
         *
         * @return a new {@link UdpRadiusServer}
         */
        public UdpRadiusServer build() {
            return new UdpRadiusServer(this);
        }

    }

}
