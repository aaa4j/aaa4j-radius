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
import org.aaa4j.radius.client.RadiusClientException;
import org.aaa4j.radius.client.RetransmissionStrategy;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodecException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Arrays;

/**
 * A client using UDP as the underlying transport layer. Create an instance using {@link Builder}.
 *
 * @implNote The client does not currently reuse sockets.
 */
public final class UdpRadiusClient extends AbstractRadiusClient {

    private static final RetransmissionStrategy DEFAULT_RETRANSMISSION_STRATEGY
            = new IntervalRetransmissionStrategy(3, Duration.ofSeconds(5));

    private final RetransmissionStrategy retransmissionStrategy;

    private UdpRadiusClient(Builder builder) {
        super(builder);

        this.retransmissionStrategy = builder.retransmissionStrategy == null
                ? DEFAULT_RETRANSMISSION_STRATEGY
                : builder.retransmissionStrategy;
    }

    /**
     * Creates a new builder for {@link UdpRadiusClient}.
     *
     * @return a new builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Packet doSend(Packet requestPacket) throws RadiusClientException {
        DatagramSocket datagramSocket = null;

        try {
            byte[] authenticatorBytes = new byte[16];
            randomProvider.nextBytes(authenticatorBytes);

            byte[] outBytes = packetCodec.encodeRequest(requestPacket, secret, authenticatorBytes);

            DatagramPacket outDatagramPacket = new DatagramPacket(outBytes, outBytes.length, address);

            datagramSocket = new DatagramSocket();

            int maxAttempts = retransmissionStrategy.getMaxAttempts();

            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                Duration timeoutDuration = retransmissionStrategy.timeoutForAttempt(attempt);
                datagramSocket.setSoTimeout((int) Math.min(timeoutDuration.toMillis(), Integer.MAX_VALUE));

                datagramSocket.send(outDatagramPacket);

                byte[] inBuffer = new byte[MAX_PACKET_SIZE];
                DatagramPacket inDatagramPacket = new DatagramPacket(inBuffer, inBuffer.length);

                try {
                    // Block until we receive the response or until we time out
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

    @Override
    public void doClose() {
        // Nothing to do
    }

    /**
     * Builder for {@link UdpRadiusClient}s.
     */
    public final static class Builder extends AbstractRadiusClient.Builder<UdpRadiusClient, Builder> {

        RetransmissionStrategy retransmissionStrategy;

        /**
         * {@inheritDoc}
         */
        public Builder address(InetSocketAddress address) {
            return super.address(address);
        }

        /**
         * {@inheritDoc}
         */
        public Builder secret(byte[] secret) {
            return super.secret(secret);
        }

        /**
         * {@inheritDoc}
         */
        public Builder dictionary(Dictionary dictionary) {
            return super.dictionary(dictionary);
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
         * Returns a new {@link UdpRadiusClient} built using the builder's options.
         *
         * @return a new {@link UdpRadiusClient}
         */
        public UdpRadiusClient build() {
            return new UdpRadiusClient(this);
        }

    }

}
