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

import java.net.InetSocketAddress;

/**
 * Strategy for handling duplicate requests.
 */
public interface DuplicationStrategy {

    /**
     * Handles a request.
     *
     * @param clientAddress the client address
     * @param requestPacket the request packet
     *
     * @return the duplication strategy result
     */
    Result handleRequest(InetSocketAddress clientAddress, Packet requestPacket);

    /**
     * Handles a response. The response will be saved to the cache.
     *
     * @param clientAddress the client address
     * @param requestPacket the request packet
     * @param responsePacket the response packet
     */
    void handleResponse(InetSocketAddress clientAddress, Packet requestPacket, Packet responsePacket);

    /**
     * Unhandles a request. Removes an in-progress request from the cache.
     *
     * @param clientAddress the client address
     * @param requestPacket the request packet
     */
    void unhandleRequest(InetSocketAddress clientAddress, Packet requestPacket);

    /**
     * A duplication strategy result contains a State and a possible response packet. When {@link #getState()} is
     * {@link State#CACHED_RESPONSE}, {@link #getResponsePacket()} will contain a value.
     */
    final class Result {

        private final State state;

        private final Packet responsePacket;

        /**
         * Creates a new duplication strategy result for the given state and response packet.
         *
         * @param state the state
         * @param responsePacket the response packet
         */
        public Result(State state, Packet responsePacket) {
            this.state = state;
            this.responsePacket = responsePacket;
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
         * Gets the response packet.
         *
         * @return the response packet or null if there is none
         */
        public Packet getResponsePacket() {
            return responsePacket;
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
