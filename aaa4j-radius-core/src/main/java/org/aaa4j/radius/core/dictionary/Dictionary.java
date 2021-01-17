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

package org.aaa4j.radius.core.dictionary;

import org.aaa4j.radius.core.attribute.AttributeType;

/**
 * A dictionary provides definitions for packets and attributes. A definition ({@link PacketDefinition},
 * {@link AttributeDefinition}) contains information to decode and encode attributes and packets when received or sent
 * over the wire.
 */
public interface Dictionary {

    /**
     * Gets a packet definition for a given packet code
     *
     * @param code packet code (integer in range [0, 255])
     * 
     * @return a packet definition or null if there's no definition for the packet code
     */
    PacketDefinition getPacketDefinition(int code);

    /**
     * Gets an attribute definition for a given attribute identifier.
     *
     * @param type an attribute identifier
     * 
     * @return the attribute definition or null if there's no definition for the attribute type
     */
    AttributeDefinition getAttributeDefinition(AttributeType type);

    /**
     * Gets a TLV definition for a given attribute identifier.
     *
     * @param type an attribute identifier
     *
     * @return a TLV definition or null if there's no definition for the attribute type
     */
    TlvDefinition getTlvDefinition(AttributeType type);

}
