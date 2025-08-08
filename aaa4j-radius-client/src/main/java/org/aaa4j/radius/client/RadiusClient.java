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

package org.aaa4j.radius.client;

import org.aaa4j.radius.core.packet.Packet;

/**
 * A RADIUS client sends RADIUS request packets and receives RADIUS response packets.
 */
public interface RadiusClient extends AutoCloseable {

    /**
     * Sends a RADIUS request packet.
     *
     * @param requestPacket the request packet to send
     * 
     * @return a RADIUS response packet
     *
     * @throws IllegalStateException if the client has already been closed
     * @throws RadiusClientException if an error occurs (e.g., IO error or timeout)
     */
    Packet send(Packet requestPacket) throws IllegalStateException, RadiusClientException;

    /**
     * Initiates an orderly shutdown where all packets in flight are handled to completion. Blocks until all resources
     * are released. Calling this method multiple times is permitted.
     */
    @Override
    void close();

}
