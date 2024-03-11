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

import java.util.Objects;

/**
 * "string" attribute data type. The string (of octets) data is mapped to a byte array. Not to be confused with
 * {@link TextData}.
 */
public class StringData extends Data {

    private final byte[] value;

    /**
     * Constructs string data from a given byte array.
     *
     * @param value the byte array
     */
    public StringData(byte[] value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public int length() {
        return value.length;
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
     * A codec for "string" data.
     */
    public static class Codec implements DataCodec<StringData> {

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
        public StringData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (dataFilter != null) {
                bytes = dataFilter.decode(codecContext, bytes);

                if (bytes == null) {
                    return null;
                }
            }

            return new StringData(bytes);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            StringData stringData = (StringData) data;

            if (dataFilter != null) {
                return dataFilter.encode(codecContext, stringData.value);
            }

            return stringData.value;
        }

    }

}
