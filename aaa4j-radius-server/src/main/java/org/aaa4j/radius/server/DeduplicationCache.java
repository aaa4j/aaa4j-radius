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

import java.net.InetSocketAddress;

/**
 * Cache for handling duplicate requests.
 */
public interface DeduplicationCache {

    /**
     * Handles a request.
     *
     * @param clientAddress the client address
     * @param requestPacketBytes the request packet bytes
     *
     * @return the duplication strategy result
     */
    Result handleRequest(InetSocketAddress clientAddress, byte[] requestPacketBytes);

    /**
     * Handles a response. The response will be saved to the cache.
     *
     * @param clientAddress the client address
     * @param requestPacketBytes the request packet bytes
     * @param responsePacketBytes the response packet bytes
     */
    void handleResponse(InetSocketAddress clientAddress, byte[] requestPacketBytes, byte[] responsePacketBytes);

    /**
     * Unhandles a request. Removes an in-progress request from the cache.
     *
     * @param clientAddress the client address
     * @param requestPacketBytes the request packet
     */
    void unhandleRequest(InetSocketAddress clientAddress, byte[] requestPacketBytes);

    /**
     * Clears the cache.
     */
    void clear();

    /**
     * A duplication strategy result contains a State and a possible response packet. When {@link #getState()} is
     * {@link State#CACHED_RESPONSE}, {@link #getResponsePacket()} will contain a value.
     */
    final class Result {

        private final State state;

        private final byte[] responsePacketBytes;

        /**
         * Creates a new duplication strategy result for the given state and response packet.
         *
         * @param state the state
         * @param responsePacketBytes the response packet bytes
         */
        public Result(State state, byte[] responsePacketBytes) {
            this.state = state;
            this.responsePacketBytes = responsePacketBytes;
        }

        /**
         * Gets the state.
         *
         * @return the state
         */
        public State getState() {
            return state;
        }

        /**
         * Gets the response packet bytes.
         *
         * @return the response packet bytes or null if there is none
         */
        public byte[] getResponsePacket() {
            return responsePacketBytes;
        }

        public enum State {
            /**
             * This is a new request.
             */
            NEW_REQUEST,

            /**
             * The request is a duplicate and the original is currently being processed.
             */
            IN_PROGRESS_REQUEST,

            /**
             * There's a cached response available to an earlier request.
             */
            CACHED_RESPONSE
        }

    }

}
