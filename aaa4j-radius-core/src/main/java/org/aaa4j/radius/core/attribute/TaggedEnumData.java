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
 * An "enum" attribute data type containing a 1-byte tag. The value is represented as a signed Java <code>int</code>.
 *
 * <p>
 * The tag occupies the first of the four octets, so the leading octet is never part of the value and a tagged enum
 * carries values in the range [0, 16777215] rather than the full 32 bits an untagged enum would. RFC 2868 sections 3.1
 * and 3.2 give this layout for Tunnel-Type and Tunnel-Medium-Type.
 * </p>
 *
 * <p>
 * Valid tags are 0x01 through 0x1f, which group related attributes within a packet. Any other value carries no grouping
 * and is kept as it was received.
 * </p>
 */
public final class TaggedEnumData extends Data {

    private final int value;

    private final int tag;

    /**
     * Constructs tagged enum data from a given integer value and tag.
     *
     * @param value the integer (int in range [0, 16777215])
     * @param tag the tag (int in range [0, 255])
     */
    public TaggedEnumData(int value, int tag) {
        if (tag < 0 || tag > 255) {
            throw new IllegalArgumentException("Tag must be in range [0, 255]");
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
     * Gets the tag. Only 0x01 through 0x1f group related attributes; any other value carries no grouping.
     *
     * @return the tag (int in range [0, 255])
     */
    public int getTag() {
        return tag;
    }

    /**
     * A codec for tagged "enum" data.
     */
    public static final class Codec implements DataCodec<TaggedEnumData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public TaggedEnumData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length != 4) {
                return null;
            }

            int tag = bytes[0] & 0xff;

            int value = (bytes[1] & 0xff) << 16
                    | (bytes[2] & 0xff) << 8
                    | (bytes[3] & 0xff);

            return new TaggedEnumData(value, tag);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            TaggedEnumData taggedEnumData = (TaggedEnumData) data;

            byte[] bytes = new byte[4];

            bytes[0] = (byte) taggedEnumData.tag;
            bytes[1] = (byte) ((taggedEnumData.value & 0x00ff0000) >>> 16);
            bytes[2] = (byte) ((taggedEnumData.value & 0x0000ff00) >>> 8);
            bytes[3] = (byte) (taggedEnumData.value & 0x000000ff);

            return bytes;
        }

    }

}
