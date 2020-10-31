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

package org.aaa4j.radius.core.attribute;

import java.util.Arrays;
import java.util.Objects;

/**
 * A raw, unprocessed attribute in the standard, top-level attribute space. A raw attribute contains a string of octets
 * ({@link StringData}) as they were received over the wire. An outgoing raw attribute's data is transmitted as is with
 * one exception: a raw attribute with a type of 80 and data length of 16 is treated as a Message-Authenticator
 * attribute and has its contents replaced with a message authentication code calculated for the packet.
 */
public final class RawAttribute extends Attribute<StringData> {

    /**
     * Constructs a raw attribute from an attribute type and data.
     *
     * @param type the attribute type (int in range [0, 255])
     * @param data the attribute data
     */
    public RawAttribute(int type, byte[] data) {
        super(new AttributeType(type), new StringData(data));

        if (data.length > 253) {
            throw new IllegalArgumentException("Data length must be in range [0, 253]");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType().head(), Arrays.hashCode(getData().getValue()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        RawAttribute that = (RawAttribute) obj;

        return getType().equals(that.getType()) && Arrays.equals(getData().getValue(), that.getData().getValue());
    }

}
