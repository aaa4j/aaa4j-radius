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

package org.aaa4j.radius.server;

import org.aaa4j.radius.core.packet.Packet;

import java.net.InetAddress;

/**
 * A RADIUS server accepts RADIUS requests and replies with RADIUS responses.
 */
public interface RadiusServer {

    /**
     * Starts the server. Blocks until the server has started or has failed to start. If the server is unable to start,
     * an {@link java.io.IOException} will handled by {@link Handler#handleException(Exception)} and
     * {@link #isRunning()} will return false. The server may only be started once and may not be started after it has
     * been stopped.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for the server to start
     */
    void start() throws InterruptedException;

    /**
     * Stops the server. Blocks until the server has stopped. Calling this method on a stopped server is permitted and
     * has no effect.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for the server to start
     *
     */
    void stop() throws InterruptedException;

    /**
     * Returns whether or not the server is running (i.e., is listening for incoming RADIUS packets).
     *
     * @return true if the server is running
     */
    boolean isRunning();

    /**
     * A RADIUS server handler. Dictates which RADIUS clients will be accepted and how the RADIUS requests will be
     * processed.
     */
    interface Handler {

        /**
         * Handles a client. The handler should return a byte array with the RADIUS shared secret if the client is to be
         * handled. If the client if unknown and should be ignored then {@code null} should be returned.
         *
         * @param clientAddress the client address
         * 
         * @return optional secret bytes
         */
        byte[] handleClient(InetAddress clientAddress);

        /**
         * Handles an incoming RADIUS request packet. The request packet is decoded using the shared RADIUS shared
         * secret from {@link #handleClient(InetAddress)}. The response packet is encoded using the same shared RADIUS
         * secret. The handler should return a {@link Packet} to be returned to the client or {@code null} if no packet
         * should be returned.
         *
         * @param clientAddress the client address
         * @param requestPacket the request packet
         * 
         * @return the response packet
         */
        Packet handlePacket(InetAddress clientAddress, Packet requestPacket);

        /**
         * Handles thrown exceptions. Exceptions may be thrown during decoding/encoding or during client or packet
         * handling in {@link #handleClient(InetAddress)} and {@link #handlePacket(InetAddress, Packet)}.
         *
         * @param exception the exception that was thrown
         */
        default void handleException(Exception exception) {
            exception.printStackTrace();
        }

    }

}
