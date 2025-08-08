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

import org.aaa4j.radius.client.RadiusClientException;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodecException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

/**
 * A client using TCP as the underlying transport layer. Create an instance using {@link Builder}.
 *
 * @implNote The client currently creates a new TCP connection for each packet sent
 */
public final class TcpRadiusClient extends AbstractRadiusClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final Duration timeout;

    private TcpRadiusClient(Builder builder) {
        super(builder);

        this.timeout = builder.timeout == null ? DEFAULT_TIMEOUT : builder.timeout;
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
    public Packet doSend(Packet requestPacket) throws RadiusClientException {
        try {
            byte[] authenticatorBytes = new byte[16];
            randomProvider.nextBytes(authenticatorBytes);

            byte[] outBytes = packetCodec.encodeRequest(requestPacket, secret, authenticatorBytes);

            Instant startInstant = Instant.now();

            try (Socket clientSocket = new Socket()) {
                // Block until connection is established
                clientSocket.connect(address, remainingTimeout(startInstant));

                // Block until the packet is sent
                clientSocket.setSoTimeout(remainingTimeout(startInstant));
                clientSocket.getOutputStream().write(outBytes);
                clientSocket.getOutputStream().flush();

                InputStream inputStream = clientSocket.getInputStream();
                byte[] inBuffer = new byte[MAX_PACKET_SIZE];
                int position = 0;

                while (true) {
                    // Block and wait for more bytes
                    clientSocket.setSoTimeout(remainingTimeout(startInstant));
                    int bytesRead = inputStream.read(inBuffer, position, inBuffer.length - position);

                    if (bytesRead == -1) {
                        clientSocket.close();

                        throw new RadiusClientException("No response from server");
                    }

                    position += bytesRead;

                    if (position >= 4) {
                        // We have enough bytes to get the packet length, so let's see if we can get a packet
                        int packetLength = (short) ((inBuffer[2] << 8) | (inBuffer[3] & 0xff));

                        if (packetLength < 20 || packetLength > MAX_PACKET_SIZE) {
                            // The packet is going to be an invalid length, so close the connection
                            clientSocket.close();

                            throw new RadiusClientException("Unexpected response packet");
                        }

                        if (position >= packetLength) {
                            // We have enough bytes for a full packet
                            byte[] responsePacketBytes = new byte[packetLength];
                            System.arraycopy(inBuffer, 0, responsePacketBytes, 0, packetLength);

                            clientSocket.close();

                            return packetCodec.decodeResponse(responsePacketBytes, secret, authenticatorBytes);
                        }
                    }
                }
            }
            catch (IOException e) {
                throw new RadiusClientException(e);
            }
        }
        catch (PacketCodecException e) {
            throw new RadiusClientException(e);
        }
    }

    @Override
    public void doClose() {
        // Nothing to do
    }

    private int remainingTimeout(Instant startInstant) {
        long elapsedMillis = Duration.between(startInstant, Instant.now()).toMillis();
        return Math.toIntExact(Math.max(1, timeout.toMillis() - elapsedMillis));
    }

    /**
     * Builder for {@link TcpRadiusClient}s.
     */
    public final static class Builder extends AbstractRadiusClient.Builder<TcpRadiusClient, TcpRadiusClient.Builder> {

        private Duration timeout;

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
         * Sets the timeout to wait for receiving a response to a request. Optional. When not set, a default timeout of
         * 5 seconds is used.
         *
         * @param timeout the response timeout
         *
         * @return this builder
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;

            return this;
        }

        /**
         * Returns a new {@link TcpRadiusClient} built using the builder's options.
         *
         * @return a new {@link TcpRadiusClient}
         */
        public TcpRadiusClient build() {
            return new TcpRadiusClient(this);
        }

    }

}
