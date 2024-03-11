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
import java.util.Optional;

/**
 * A "text" attribute data type containing an optional 1-byte tag. The text data is mapped to a {@link String}.
 */
public class OptionalTaggedTextData extends Data {

    private final String value;

    private final Integer tag;

    /**
     * Constructs tagged string data from a given byte array and tag.
     *
     * @param value the string
     * @param tag the tag (int in range [0, 31])
     */
    public OptionalTaggedTextData(String value, int tag) {
        if (tag < 0 || tag > 31) {
            throw new IllegalArgumentException("Tag must be in range [0, 31]");
        }

        this.tag = tag;
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Constructs tagged string data from a given string. No tag is included in the data with this constructor.
     *
     * @param value the string
     */
    public OptionalTaggedTextData(String value) {
        this.tag = null;
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public int length() {
        return (tag == null ? 0 : 1) + value.getBytes(StandardCharsets.UTF_8).length;
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
     * Gets the optional tag.
     *
     * @return optional tag (Optional with Integer in range [0, 31])
     */
    public Optional<Integer> getTag() {
        return Optional.ofNullable(tag);
    }

    /**
     * A codec for optional tagged "text" data.
     */
    public static class Codec implements DataCodec<OptionalTaggedTextData> {

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
        public OptionalTaggedTextData decode(CodecContext codecContext, AttributeType parentAttributeType,
                                             byte[] bytes)
        {
            if (bytes.length < 1) {
                return new OptionalTaggedTextData("");
            }

            int tag = bytes[0] & 0xff;

            if (tag > 31) {
                if (dataFilter != null) {
                    bytes = dataFilter.decode(codecContext, bytes);

                    if (bytes == null) {
                        return null;
                    }
                }

                return new OptionalTaggedTextData(new String(bytes, StandardCharsets.UTF_8));
            }

            byte[] dataBytes = Arrays.copyOfRange(bytes, 1, bytes.length);

            if (dataFilter != null) {
                dataBytes = dataFilter.decode(codecContext, dataBytes);
            }

            if (dataBytes == null) {
                return null;
            }

            return new OptionalTaggedTextData(new String(dataBytes, StandardCharsets.UTF_8), tag);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            OptionalTaggedTextData optionalTaggedTextData = (OptionalTaggedTextData) data;

            if (optionalTaggedTextData.tag != null) {
                byte[] bytes = new byte[optionalTaggedTextData.length()];
                byte[] dataBytes = optionalTaggedTextData.value.getBytes(StandardCharsets.UTF_8);

                dataBytes[0] = optionalTaggedTextData.tag.byteValue();
                System.arraycopy(dataBytes, 0, bytes, 1, dataBytes.length);

                if (dataFilter != null) {
                    return dataFilter.encode(codecContext, bytes);
                }

                return bytes;
            }

            byte[] bytes = optionalTaggedTextData.value.getBytes(StandardCharsets.UTF_8);

            if (dataFilter != null) {
                return dataFilter.encode(codecContext, bytes);
            }

            return bytes;
        }

    }

}
