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
 * A "string" attribute data type containing a 1-byte tag. The string (of octets) data is mapped to a byte array.
 */
public class TaggedStringData extends Data {

    private final byte[] value;

    private final int tag;

    /**
     * Constructs tagged string data from a given byte array and tag.
     *
     * @param value the byte array
     * @param tag the tag (int in range [0, 31])
     */
    public TaggedStringData(byte[] value, int tag) {
        if (tag < 0 || tag > 31) {
            throw new IllegalArgumentException("Tag must be in range [0, 31]");
        }

        this.tag = tag;
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public int length() {
        return 1 + value.length;
    }

    /**
     * Gets the octet string value as a byte array.
     *
     * @return the string value as a byte array
     */
    public byte[] getValue() {
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
     * A codec for tagged "string" data.
     */
    public static class Codec implements DataCodec<TaggedStringData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public TaggedStringData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length < 1) {
                return null;
            }

            int tag = bytes[0] & 0xff;

            if (tag > 31) {
                // If the tag field is unused, the tag must be 0x00
                return null;
            }

            return new TaggedStringData(Arrays.copyOfRange(bytes, 1, bytes.length), tag);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            TaggedStringData taggedStringData = (TaggedStringData) data;

            byte[] bytes = new byte[1 + taggedStringData.value.length];

            bytes[0] = (byte) taggedStringData.tag;

            System.arraycopy(taggedStringData.value, 0, bytes, 1, taggedStringData.value.length);

            return bytes;
        }

    }

}
