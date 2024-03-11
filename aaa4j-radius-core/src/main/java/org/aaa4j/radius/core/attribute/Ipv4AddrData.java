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

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * "ipv4addr" attribute data type. The ipv4addr data is mapped to a {@link Inet4Address}.
 */
public final class Ipv4AddrData extends Data {

    private final Inet4Address value;

    /**
     * Constructs ipv4addr data from a given {@link Inet4Address} object.
     *
     * @param value the Inet4Address object
     */
    public Ipv4AddrData(Inet4Address value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public int length() {
        return 4;
    }

    /**
     * Gets the ipv4addr value.
     *
     * @return the ipv4addr data as a {@link Inet4Address}
     */
    public Inet4Address getValue() {
        return value;
    }

    /**
     * A codec for "ipv4addr" data.
     */
    public static final class Codec implements DataCodec<Ipv4AddrData> {

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
        public Ipv4AddrData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (dataFilter != null) {
                bytes = dataFilter.decode(codecContext, bytes);

                if (bytes == null) {
                    return null;
                }
            }

            if (bytes.length != 4) {
                return null;
            }

            try {
                return new Ipv4AddrData((Inet4Address) Inet4Address.getByAddress(bytes));
            }
            catch (UnknownHostException e) {
                // Thrown if bytes is an invalid length, which is already handled
                return null;
            }
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            Ipv4AddrData ipv4AddrData = (Ipv4AddrData) data;

            if (dataFilter != null) {
                return dataFilter.encode(codecContext, ipv4AddrData.value.getAddress());
            }

            return ipv4AddrData.value.getAddress();
        }

    }

}
