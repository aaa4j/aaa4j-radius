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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * A "text" attribute data type containing an optional 1-byte tag. The text data is mapped to a {@link String}.
 *
 * <p>
 * The tag octet is only present if the sender chose to include one. On decoding, a leading octet in the range [0x00,
 * 0x1f] is taken to be a tag and anything above that is taken to be the first byte of the text, which is the rule given
 * in RFC 2868 section 3.3. Whether a tag was present is preserved, so decoding and re-encoding reproduces the wire form
 * that was received. That matters when proxying, where the form chosen by the original sender should be passed on
 * unchanged.
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
public class OptionalTaggedTextData extends Data {

    private final String value;

    private final Integer tag;

    /**
     * Constructs tagged text data from a given string and tag.
     *
     * <p>
     * Use this constructor to group related attributes, giving each attribute in the group the same tag. Passing a tag
     * of zero is not the same as passing no tag: it puts a zero octet on the wire. Prefer
     * {@link #OptionalTaggedTextData(String)} when the attribute isn't part of a group, since RFC 2868 section 3 says
     * that the tag field "SHOULD NOT be used" in that case, and a receiver that predates RFC 2868 would read the zero
     * octet as the first character of the text.
     * </p>
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
     * Constructs tagged text data from a given string. No tag octet is included in the encoded data.
     *
     * <p>
     * This is the constructor to use unless the attribute belongs to a group of related attributes.
     * </p>
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

        private static String decodeUtf8(byte[] bytes) {
            try {
                // A new decoder each time: decoders are stateful, and this codec instance is shared
                return StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes))
                        .toString();
            }
            catch (CharacterCodingException e) {
                // Not valid UTF-8, so not text
                return null;
            }
        }

        @Override
        public OptionalTaggedTextData decode(CodecContext codecContext, AttributeType parentAttributeType,
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

                String untaggedValue = decodeUtf8(bytes);

                if (untaggedValue == null) {
                    return null;
                }

                return new OptionalTaggedTextData(untaggedValue);
            }

            byte[] dataBytes = Arrays.copyOfRange(bytes, 1, bytes.length);

            if (dataFilter != null) {
                dataBytes = dataFilter.decode(codecContext, dataBytes);

                if (dataBytes == null) {
                    return null;
                }
            }

            String value = decodeUtf8(dataBytes);

            if (value == null) {
                return null;
            }

            return new OptionalTaggedTextData(value, tag);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            OptionalTaggedTextData optionalTaggedTextData = (OptionalTaggedTextData) data;

            byte[] textData = optionalTaggedTextData.value.getBytes(StandardCharsets.UTF_8);

            if (dataFilter != null) {
                textData = dataFilter.encode(codecContext, textData);
            }

            if (optionalTaggedTextData.tag == null) {
                return textData;
            }

            byte[] bytes = new byte[1 + textData.length];

            bytes[0] = optionalTaggedTextData.tag.byteValue();

            System.arraycopy(textData, 0, bytes, 1, textData.length);

            return bytes;
        }

    }

}
