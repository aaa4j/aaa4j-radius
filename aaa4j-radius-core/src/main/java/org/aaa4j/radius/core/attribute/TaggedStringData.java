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
 *
 * <p>
 * The tag occupies the first octet, so the leading octet is never part of the value. RFC 2868 section 3.5 gives this
 * layout for Tunnel-Password.
 * </p>
 *
 * <p>
 * Valid tags are 0x01 through 0x1f, which group related attributes within a packet. Any other value carries no grouping
 * and is kept as it was received.
 * </p>
 *
 * <p>
 * When a {@link DataFilter} is used, it applies to the value alone, so the tag travels unfiltered ahead of the filtered
 * data.
 * </p>
 */
public class TaggedStringData extends Data {

    private final byte[] value;

    private final int tag;

    /**
     * Constructs tagged string data from a given byte array and tag.
     *
     * @param value the byte array
     * @param tag the tag (int in range [0, 255])
     */
    public TaggedStringData(byte[] value, int tag) {
        if (tag < 0 || tag > 255) {
            throw new IllegalArgumentException("Tag must be in range [0, 255]");
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
     * Gets the tag. Only 0x01 through 0x1f group related attributes; any other value carries no grouping.
     *
     * @return the tag (int in range [0, 255])
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

        private final DataFilter dataFilter;

        /**
         * Constructs a {@link Codec}.
         */
        public Codec() {
            this.dataFilter = null;
        }

        /**
         * Constructs a {@link Codec} with the provided {@link DataFilter}.
         *
         * @param dataFilter the data filter
         */
        public Codec(DataFilter dataFilter) {
            this.dataFilter = dataFilter;
        }

        @Override
        public TaggedStringData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length < 1) {
                return null;
            }

            // The tag octet is always present on these attributes, so it's never treated as the first byte of the
            // value. An out of range tag carries no tunnel, but it's kept as received so that re-encoding
            // reproduces the octet that arrived.
            int tag = bytes[0] & 0xff;

            byte[] stringData = Arrays.copyOfRange(bytes, 1, bytes.length);

            if (dataFilter != null) {
                byte[] filteredData = dataFilter.decode(codecContext, stringData);

                if (filteredData != null) {
                    stringData = filteredData;
                }
                else {
                    return null;
                }
            }

            return new TaggedStringData(stringData, tag);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            TaggedStringData taggedStringData = (TaggedStringData) data;

            byte[] stringData = taggedStringData.value;

            if (dataFilter != null) {
                stringData = dataFilter.encode(codecContext, stringData);
            }

            byte[] bytes = new byte[1 + stringData.length];

            bytes[0] = (byte) taggedStringData.tag;

            System.arraycopy(stringData, 0, bytes, 1, stringData.length);

            return bytes;
        }

    }

}
