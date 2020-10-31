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

import org.aaa4j.radius.client.IntervalRetransmissionStrategy;
import org.aaa4j.radius.client.RadiusClient;
import org.aaa4j.radius.client.RadiusClientException;
import org.aaa4j.radius.client.RetransmissionStrategy;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.packet.IncrementingPacketIdGenerator;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodec;
import org.aaa4j.radius.core.packet.PacketCodecException;
import org.aaa4j.radius.core.packet.PacketIdGenerator;
import org.aaa4j.radius.core.util.RandomProvider;
import org.aaa4j.radius.core.util.SecureRandomProvider;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

/**
 * A client using UDP as the underlying transport layer. Create an instance using {@link Builder}.
 */
public class UdpRadiusClient implements RadiusClient {

    private static final int MAX_PACKET_SIZE = 4096;

    private static final RetransmissionStrategy DEFAULT_RETRANSMISSION_STRATEGY = new IntervalRetransmissionStrategy(3,
            Duration.ofSeconds(5));

    private final InetSocketAddress address;

    private final byte[] secret;

    private final RetransmissionStrategy retransmissionStrategy;

    private final RandomProvider randomProvider;

    private final PacketCodec packetCodec;

    private UdpRadiusClient(Builder builder) {
        this.address = Objects.requireNonNull(builder.address);
        this.secret = Objects.requireNonNull(builder.secret);

        this.retransmissionStrategy = builder.retransmissionStrategy == null
                ? DEFAULT_RETRANSMISSION_STRATEGY
                : builder.retransmissionStrategy;

        Dictionary dictionary = builder.dictionary == null
                ? new StandardDictionary()
                : builder.dictionary;

        PacketIdGenerator packetIdGenerator = builder.packetIdGenerator == null
                ? new IncrementingPacketIdGenerator(0)
                : builder.packetIdGenerator;

        this.randomProvider = builder.randomProvider == null
                ? new SecureRandomProvider()
                : builder.randomProvider;

        this.packetCodec = new PacketCodec(dictionary, randomProvider, packetIdGenerator);
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
    public Packet send(Packet requestPacket) throws RadiusClientException {
        DatagramSocket datagramSocket = null;

        try {
            byte[] authenticatorBytes = new byte[16];
            randomProvider.nextBytes(authenticatorBytes);

            byte[] outBytes = packetCodec.encodeRequest(requestPacket, secret, authenticatorBytes);

            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(0);

            DatagramPacket outDatagramPacket = new DatagramPacket(outBytes, outBytes.length, address);

            int maxAttempts = retransmissionStrategy.getMaxAttempts();

            DatagramPacket inDatagramPacket = null;

            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                Duration timeoutDuration = retransmissionStrategy.timeoutForAttempt(attempt);
                datagramSocket.setSoTimeout(Math.toIntExact(timeoutDuration.toMillis()));

                datagramSocket.send(outDatagramPacket);

                byte[] inBuffer = new byte[MAX_PACKET_SIZE];
                inDatagramPacket = new DatagramPacket(inBuffer, inBuffer.length);

                try {
                    datagramSocket.receive(inDatagramPacket);

                    byte[] inBytes = Arrays.copyOfRange(inDatagramPacket.getData(), 0, inDatagramPacket.getLength());

                    return packetCodec.decodeResponse(inBytes, secret, authenticatorBytes);
                }
                catch (SocketTimeoutException ignored) {
                    // Continue for loop
                }
            }

            throw new RadiusClientException("Timeout waiting for response");
        }
        catch (PacketCodecException | IOException e) {
            throw new RadiusClientException(e);
        }
        finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }
    }

    /**
     * Builder for {@link UdpRadiusClient}s.
     */
    public final static class Builder {

        InetSocketAddress address;

        byte[] secret;

        RetransmissionStrategy retransmissionStrategy;

        Dictionary dictionary;

        PacketIdGenerator packetIdGenerator;

        RandomProvider randomProvider;

        /**
         * Sets the address of the server. Required.
         *
         * @param address the server address
         * 
         * @return this builder
         */
        public Builder address(InetSocketAddress address) {
            this.address = address;

            return this;
        }

        /**
         * Sets the RADIUS shared secret. Required.
         *
         * @param secret the address to bind to
         * 
         * @return this builder
         */
        public Builder secret(byte[] secret) {
            this.secret = secret;

            return this;
        }

        /**
         * Sets the {@link RetransmissionStrategy} to use. Optional. When not set, a default retransmission strategy
         * will be used that will attempt a maximum of three attempts at a 5-second interval.
         *
         * @param retransmissionStrategy the retransmission strategy to use
         * 
         * @return this builder
         */
        public Builder retransmissionStrategy(RetransmissionStrategy retransmissionStrategy) {
            this.retransmissionStrategy = retransmissionStrategy;

            return this;
        }

        /**
         * Sets the {@link Dictionary} to use. Optional. When not set, the standard dictionary will be used.
         *
         * @param dictionary the dictionary to use
         * 
         * @return this builder
         */
        public Builder dictionary(Dictionary dictionary) {
            this.dictionary = dictionary;

            return this;
        }

        /**
         * Sets the {@link PacketIdGenerator} to use to generate packet identifiers for outbound packets. Optional. When
         * not set, a default incrementing packet identifier generator will be used.
         *
         * @param packetIdGenerator the packet identifier generator to use
         * 
         * @return this builder
         */
        public Builder packetIdGenerator(PacketIdGenerator packetIdGenerator) {
            this.packetIdGenerator = packetIdGenerator;

            return this;
        }

        /**
         * Sets the {@link RandomProvider} to use for all randomness required by RADIUS. Optional. When not set, a
         * cryptographically-secure random source will be used.
         *
         * @param randomProvider the random provider to use
         * 
         * @return this builder
         */
        public Builder randomProvider(RandomProvider randomProvider) {
            this.randomProvider = randomProvider;

            return this;
        }

        /**
         * Returns a new {@link UdpRadiusClient} built using the builder's options.
         *
         * @return a new {@link UdpRadiusClient}
         */
        public UdpRadiusClient build() {
            return new UdpRadiusClient(this);
        }

    }

}
