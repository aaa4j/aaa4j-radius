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
 *
 * <p>
 * The tag octet is only present if the sender chose to include one. On decoding, a leading octet in the range [0x00,
 * 0x1f] is taken to be a tag and anything above that is taken to be the first byte of the string, which is the rule
 * given in RFC 2868 section 3.3. Whether a tag was present is preserved, so decoding and re-encoding reproduces the
 * wire form that was received. That matters when proxying, where the form chosen by the original sender should be
 * passed on unchanged.
 * </p>
 *
 * <p>
 * Note that this rule cannot disambiguate a value whose own first octet falls in the tag range, which RFC 6158 section
 * 3.2.2 records as a limitation of the tagging scheme.
 * </p>
 *
 * <p>
 * When a {@link DataFilter} is used, it applies to the value alone, so the tag travels unfiltered ahead of the filtered
 * data.
 * </p>
 */
public class OptionalTaggedStringData extends Data {

    private final byte[] value;

    private final Integer tag;

    /**
     * Constructs tagged string data from a given byte array and tag.
     *
     * <p>
     * Use this constructor to group related attributes, giving each attribute in the group the same tag. Passing a tag
     * of zero is not the same as passing no tag: it puts a zero octet on the wire. Prefer
     * {@link #OptionalTaggedStringData(byte[])} when the attribute isn't part of a group, since RFC 2868 section 3 says
     * that the tag field "SHOULD NOT be used" in that case, and a receiver that predates RFC 2868 would read the zero
     * octet as the first byte of the string.
     * </p>
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
     * Constructs tagged string data from a given byte array. No tag octet is included in the encoded data.
     *
     * <p>
     * This is the constructor to use unless the attribute belongs to a group of related attributes.
     * </p>
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
                return null;
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

                if (dataBytes == null) {
                    return null;
                }
            }

            return new OptionalTaggedStringData(dataBytes, tag);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            OptionalTaggedStringData optionalTaggedStringData = (OptionalTaggedStringData) data;

            byte[] stringData = optionalTaggedStringData.value;

            if (dataFilter != null) {
                stringData = dataFilter.encode(codecContext, stringData);
            }

            if (optionalTaggedStringData.tag == null) {
                return stringData;
            }

            byte[] bytes = new byte[1 + stringData.length];

            bytes[0] = optionalTaggedStringData.tag.byteValue();

            System.arraycopy(stringData, 0, bytes, 1, stringData.length);

            return bytes;
        }

    }

}
