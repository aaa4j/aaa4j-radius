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

package org.aaa4j.radius.core.packet.packets;

import org.aaa4j.radius.core.attribute.Attribute;
import org.aaa4j.radius.core.packet.Packet;

import java.util.List;

/**
 * Accounting-Request (4) packet.
 */
public final class AccountingRequest extends Packet {

    /**
     * Packet code.
     */
    public static final int CODE = 4;

    /**
     * Packet name.
     */
    public static final String NAME = "Accounting-Request";

    /**
     * Constructs Accounting-Request packets.
     */
    public AccountingRequest() {
        super(CODE);
    }

    /**
     * Constructs Accounting-Request packets.
     *
     * @param attributes the attributes to include in the packet
     */
    public AccountingRequest(List<Attribute<?>> attributes) {
        super(CODE, attributes);
    }

    /**
     * Constructs incoming Accounting-Request packets.
     *
     * @param attributes the attributes to include in the packet
     * @param receivedFields the received fields from the incoming packet
     */
    public AccountingRequest(List<Attribute<?>> attributes, ReceivedFields receivedFields) {
        super(CODE, attributes, receivedFields);
    }

}
