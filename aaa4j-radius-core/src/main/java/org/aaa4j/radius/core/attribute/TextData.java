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
import java.util.Objects;

/**
 * "text" attribute data type. The text data is mapped to a {@link String}.
 */
public class TextData extends Data {

    private final String value;

    /**
     * Constructs text data from a given String.
     *
     * @param value the string
     */
    public TextData(String value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public int length() {
        return value.getBytes(StandardCharsets.UTF_8).length;
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
     * A codec for "text" data.
     */
    public static class Codec implements DataCodec<TextData> {

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
        public TextData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (dataFilter != null) {
                bytes = dataFilter.decode(codecContext, bytes);

                if (bytes == null) {
                    return null;
                }
            }

            return new TextData(new String(bytes, StandardCharsets.UTF_8));
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            TextData textData = (TextData) data;

            if (dataFilter != null) {
                return dataFilter.encode(codecContext, textData.value.getBytes(StandardCharsets.UTF_8));
            }

            return textData.value.getBytes(StandardCharsets.UTF_8);
        }

    }

}
