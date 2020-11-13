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
 * "integer64" attribute data type. "integer64" data type is a 64-bit unsigned integer but is represented as a signed
 * Java <code>long</code>.
 */
public final class Integer64Data extends Data {

    private final long value;

    /**
     * Constructs integer64 data from a given long value.
     *
     * @param value the long
     */
    public Integer64Data(long value) {
        this.value = value;
    }

    @Override
    public int length() {
        return 8;
    }

    /**
     * Gets the long value.
     *
     * @return the value as a signed Java long
     */
    public long getValue() {
        return value;
    }

    /**
     * A codec for "integer64" data.
     */
    public static final class Codec implements DataCodec<Integer64Data> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public Integer64Data decode(CodecContext codecContext, byte[] bytes) {
            if (bytes.length != 8) {
                return null;
            }

            long value = (bytes[0] & 0xffL) << 56
                    | (bytes[1] & 0xffL) << 48
                    | (bytes[2] & 0xffL) << 40
                    | (bytes[3] & 0xffL) << 32
                    | (bytes[4] & 0xffL) << 24
                    | (bytes[5] & 0xffL) << 16
                    | (bytes[6] & 0xffL) << 8
                    | (bytes[7] & 0xffL);

            return new Integer64Data(value);
        }

        @Override
        public byte[] encode(CodecContext codecContext, Integer64Data data) {
            byte[] bytes = new byte[8];

            bytes[0] = (byte) ((data.value & 0xff00000000000000L) >>> 56);
            bytes[1] = (byte) ((data.value & 0x00ff000000000000L) >>> 48);
            bytes[2] = (byte) ((data.value & 0x0000ff0000000000L) >>> 40);
            bytes[3] = (byte) ((data.value & 0x000000ff00000000L) >>> 32);
            bytes[4] = (byte) ((data.value & 0x00000000ff000000L) >>> 24);
            bytes[5] = (byte) ((data.value & 0x0000000000ff0000L) >>> 16);
            bytes[6] = (byte) ((data.value & 0x000000000000ff00L) >>> 8);
            bytes[7] = (byte) (data.value & 0x00000000000000ffL);

            return bytes;
        }

    }

}
