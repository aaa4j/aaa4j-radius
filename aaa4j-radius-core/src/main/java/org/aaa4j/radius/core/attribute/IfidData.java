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
 * "ifid" attribute data type. The "ifid" data type is mapped to a byte array.
 */
public class IfidData extends Data {

    private final byte[] value;

    /**
     * Constructs string data from a given byte array.
     *
     * @param value the byte array of the interface identifier
     */
    public IfidData(byte[] value) {
        Objects.requireNonNull(value);

        if (value.length != 8) {
            throw new IllegalArgumentException("value length must be 8");
        }

        this.value = Arrays.copyOf(value, value.length);
    }

    @Override
    public int length() {
        return 8;
    }

    /**
     * Gets the ifid value as a byte array.
     *
     * @return the ifid value as a byte array
     */
    public byte[] getValue() {
        return Arrays.copyOf(value, value.length);
    }

    /**
     * A codec for "ifid" data.
     */
    public static class Codec implements DataCodec<IfidData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public IfidData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length != 8) {
                return null;
            }

            return new IfidData(bytes);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            IfidData ifidData = (IfidData) data;

            return ifidData.value;
        }

    }

}
