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
 * An "integer" attribute data type containing a 1-byte tag. "integer" data is a 32-bit unsigned integer but is
 * represented as a signed Java <code>int</code>.
 */
public final class TaggedIntegerData extends Data {

    private final int value;

    private final int tag;

    /**
     * Constructs tagged integer data from a given integer value and tag.
     *
     * @param value the byte array
     * @param tag the tag (int in range [0, 31])
     */
    public TaggedIntegerData(int value, int tag) {
        if (tag < 0 || tag > 31) {
            throw new IllegalArgumentException("Tag must be in range [0, 31]");
        }

        if (value < 0 || value > 16777215) {
            throw new IllegalArgumentException("Value must be in range [0, 16777215]");
        }

        this.tag = tag;
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
     * Gets the tag.
     *
     * @return the tag (int in range [0, 31])
     */
    public int getTag() {
        return tag;
    }

    /**
     * A codec for tagged "integer" data.
     */
    public static final class Codec implements DataCodec<TaggedIntegerData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public TaggedIntegerData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length != 4) {
                return null;
            }

            int tag = bytes[0] & 0xff;

            if (tag > 31) {
                return null;
            }

            int value = (bytes[1] & 0xff) << 16
                    | (bytes[2] & 0xff) << 8
                    | bytes[3] & 0xff;

            return new TaggedIntegerData(value, tag);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            TaggedIntegerData taggedIntegerData = (TaggedIntegerData) data;

            byte[] bytes = new byte[4];

            bytes[0] = (byte) taggedIntegerData.tag;
            bytes[1] = (byte) ((taggedIntegerData.value & 0x00ff0000) >>> 16);
            bytes[2] = (byte) ((taggedIntegerData.value & 0x0000ff00) >>> 8);
            bytes[3] = (byte) (taggedIntegerData.value & 0x000000ff);

            return bytes;
        }

    }

}
