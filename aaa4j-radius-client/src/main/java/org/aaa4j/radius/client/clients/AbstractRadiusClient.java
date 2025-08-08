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

package org.aaa4j.radius.client.clients;

import org.aaa4j.radius.client.RadiusClient;
import org.aaa4j.radius.client.RadiusClientException;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.packet.IncrementingPacketIdGenerator;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodec;
import org.aaa4j.radius.core.util.RandomProvider;
import org.aaa4j.radius.core.util.SecureRandomProvider;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

abstract class AbstractRadiusClient implements RadiusClient {

    static final int MAX_PACKET_SIZE = 4096;

    final AtomicLong activeSenderCount = new AtomicLong(0);

    volatile CountDownLatch closeLatch;

    final Lock sharedLock = new ReentrantLock();

    final Lock closeLock = new ReentrantLock();

    final InetSocketAddress address;

    final byte[] secret;

    final Dictionary dictionary;

    final RandomProvider randomProvider = new SecureRandomProvider();

    final PacketCodec packetCodec;

    /**
     * Indicates that the client has already been closed
     */
    volatile boolean isClosed = false;

    public AbstractRadiusClient(Builder<?, ?> builder, byte[] defaultSecret) {
        this.address = Objects.requireNonNull(builder.address);
        this.secret = Objects.requireNonNull(builder.secret == null ? defaultSecret : builder.secret);
        this.dictionary = builder.dictionary == null ? new StandardDictionary() : builder.dictionary;

        this.packetCodec = new PacketCodec(dictionary, randomProvider, new IncrementingPacketIdGenerator(1));
    }

    public AbstractRadiusClient(Builder<?, ?> builder) {
        this(builder, null);
    }

    @Override
    public final Packet send(Packet requestPacket) throws RadiusClientException {
        sharedLock.lock();

        try {
            if (isClosed) {
                throw new IllegalStateException("Client has already been closed");
            }

            activeSenderCount.incrementAndGet();
        }
        finally {
            sharedLock.unlock();
        }

        try {
            return doSend(requestPacket);
        }
        finally {
            sharedLock.lock();

            try {
                activeSenderCount.decrementAndGet();

                if (closeLatch != null) {
                    // Signal the thread(s) waiting on close
                    closeLatch.countDown();
                }
            }
            finally {
                sharedLock.unlock();
            }
        }
    }

    @Override
    public final void close() {
        // Block and wait for other calls to close to finish
        closeLock.lock();

        try {
            if (isClosed) {
                return;
            }

            long numActiveSenders;

            sharedLock.lock();

            try {
                isClosed = true;

                numActiveSenders = activeSenderCount.get();

                if (numActiveSenders > 0) {
                    closeLatch = new CountDownLatch(Math.toIntExact(numActiveSenders));
                }
            }
            finally {
                sharedLock.unlock();
            }

            if (numActiveSenders > 0) {
                // Wait for all the senders to finish
                try {
                    closeLatch.await();
                }
                catch (InterruptedException ignored) {
                    // Return without cleaning up
                    return;
                }
                finally {
                    closeLatch = null;
                }
            }

            // Close all resources
            doClose();
        }
        finally {
            closeLatch = null;

            closeLock.unlock();
        }
    }

    abstract Packet doSend(Packet requestPacket) throws RadiusClientException;

    abstract void doClose();

    static abstract class Builder<T extends RadiusClient, B extends Builder<T, B>> {

        InetSocketAddress address;

        byte[] secret;

        Dictionary dictionary;

        /**
         * Sets the address of the server. Required.
         *
         * @param address the server address
         *
         * @return this builder
         */
        public B address(InetSocketAddress address) {
            this.address = address;

            @SuppressWarnings("unchecked")
            B builder = (B) this;

            return builder;
        }

        /**
         * Sets the RADIUS shared secret. Required.
         *
         * @param secret the address to bind to
         *
         * @return this builder
         */
        public B secret(byte[] secret) {
            this.secret = secret;

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

        public abstract T build();

    }

}
