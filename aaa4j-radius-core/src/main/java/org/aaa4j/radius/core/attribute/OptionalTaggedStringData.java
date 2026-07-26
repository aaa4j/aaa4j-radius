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
import java.util.Optional;

/**
 * A "string" attribute data type containing an optional 1-byte tag. The string (of octets) data is mapped to a byte
 * array.
 */
public class OptionalTaggedStringData extends Data {

    private final byte[] value;

    private final Integer tag;

    /**
     * Constructs tagged string data from a given byte array and tag.
     *
     * @param value the byte array
     * @param tag the tag (int in range [0, 31])
     */
    public OptionalTaggedStringData(byte[] value, int tag) {
        if (tag < 0 || tag > 31) {
            throw new IllegalArgumentException("Tag must be in range [0, 31]");
        }

        this.tag = tag;
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Constructs tagged string data from a given byte array. No tag is included in the data with this constructor.
     *
     * @param value the byte array
     */
    public OptionalTaggedStringData(byte[] value) {
        this.tag = null;
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public int length() {
        return tag == null ? value.length : 1 + value.length;
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
     * Gets the optional tag.
     *
     * @return optional tag (Optional with Integer in range [0, 31])
     */
    public Optional<Integer> getTag() {
        return Optional.ofNullable(tag);
    }

    /**
     * A codec for optional tagged "string" data.
     */
    public static class Codec implements DataCodec<OptionalTaggedStringData> {

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
        public OptionalTaggedStringData decode(CodecContext codecContext, AttributeType parentAttributeType,
                byte[] bytes) {
            if (bytes.length < 1) {
                return new OptionalTaggedStringData(new byte[] {});
            }

            int tag = bytes[0] & 0xff;

            if (tag > 31) {
                if (dataFilter != null) {
                    bytes = dataFilter.decode(codecContext, bytes);

                    if (bytes == null) {
                        return null;
                    }
                }

                return new OptionalTaggedStringData(bytes);
            }

            byte[] dataBytes = Arrays.copyOfRange(bytes, 1, bytes.length);

            if (dataFilter != null) {
                dataBytes = dataFilter.decode(codecContext, dataBytes);
            }

            if (dataBytes == null) {
                return null;
            }

            return new OptionalTaggedStringData(dataBytes, tag);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            OptionalTaggedStringData optionalTaggedStringData = (OptionalTaggedStringData) data;

            if (optionalTaggedStringData.tag != null) {
                byte[] bytes = new byte[1 + optionalTaggedStringData.value.length];

                bytes[0] = optionalTaggedStringData.tag.byteValue();
                System.arraycopy(optionalTaggedStringData.value, 0, bytes, 1, optionalTaggedStringData.value.length);

                if (dataFilter != null) {
                    return dataFilter.encode(codecContext, bytes);
                }

                return bytes;
            }

            if (dataFilter != null) {
                return dataFilter.encode(codecContext, optionalTaggedStringData.value);
            }

            return optionalTaggedStringData.value;
        }

    }

}
