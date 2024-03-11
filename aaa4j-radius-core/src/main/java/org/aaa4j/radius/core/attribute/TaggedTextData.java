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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * A "text" attribute data type containing a 1-byte tag. The text data is mapped to a {@link String}.
 */
public class TaggedTextData extends Data {

    private final String value;

    private final int tag;

    /**
     * Constructs tagged text data from a given String and tag.
     *
     * @param value the string
     * @param tag the tag (int in range [0, 31])
     */
    public TaggedTextData(String value, int tag) {
        if (tag < 0 || tag > 31) {
            throw new IllegalArgumentException("Tag must be in range [0, 31]");
        }

        this.tag = tag;
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public int length() {
        return 1 + value.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * Gets the text value as a String.
     *
     * @return the text data as a String
     */
    public String getValue() {
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
     * A codec for tagged "text" data.
     */
    public static class Codec implements DataCodec<TaggedTextData> {

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
        public TaggedTextData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length < 1) {
                return null;
            }

            int tag = bytes[0] & 0xff;

            if (tag > 31) {
                // If the tag field is unused, the tag must be 0x00
                return null;
            }

            byte[] textData = Arrays.copyOfRange(bytes, 1, bytes.length);

            if (dataFilter != null) {
                byte[] filteredData = dataFilter.decode(codecContext, textData);

                if (filteredData != null) {
                    return new TaggedTextData(new String(filteredData, StandardCharsets.UTF_8), tag);
                }
                else {
                    return null;
                }
            }

            return new TaggedTextData(new String(textData, StandardCharsets.UTF_8), tag);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            TaggedTextData taggedTextData = (TaggedTextData) data;

            byte[] bytes = new byte[taggedTextData.length()];

            bytes[0] = (byte) taggedTextData.tag;

            if (dataFilter != null) {
                byte[] filteredData =
                        dataFilter.encode(codecContext, taggedTextData.value.getBytes(StandardCharsets.UTF_8));

                System.arraycopy(filteredData, 0, bytes, 1, filteredData.length);
            }
            else {
                byte[] textData = taggedTextData.value.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(textData, 0, bytes, 1, textData.length);
            }

            return bytes;
        }

    }

}
