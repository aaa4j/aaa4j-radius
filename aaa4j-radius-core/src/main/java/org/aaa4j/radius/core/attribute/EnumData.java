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

/**
 * "enum" attribute data type. "enum" data is a 32-bit unsigned integer but is represented as a signed Java
 * <code>int</code>. EnumData is identical to {@link IntegerData} but is used to represent well-known enumerated values.
 */
public final class EnumData extends Data {

    private final int value;

    /**
     * Constructs integer data from a given integer value.
     *
     * @param value the integer
     */
    public EnumData(int value) {
        this.value = value;
    }

    @Override
    public int length() {
        return 4;
    }

    /**
     * Gets the integer value.
     *
     * @return the integer as a signed Java int
     */
    public int getValue() {
        return value;
    }

    /**
     * A codec for "enum" data.
     */
    public static final class Codec implements DataCodec<EnumData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public EnumData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length != 4) {
                return null;
            }

            int value = (bytes[0] & 0xff) << 24
                    | (bytes[1] & 0xff) << 16
                    | (bytes[2] & 0xff) << 8
                    | bytes[3] & 0xff;

            return new EnumData(value);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            EnumData enumData = (EnumData) data;

            byte[] bytes = new byte[4];

            bytes[0] = (byte) ((enumData.value & 0xff000000) >>> 24);
            bytes[1] = (byte) ((enumData.value & 0x00ff0000) >>> 16);
            bytes[2] = (byte) ((enumData.value & 0x0000ff00) >>> 8);
            bytes[3] = (byte) (enumData.value & 0x000000ff);

            return bytes;
        }

    }

}
