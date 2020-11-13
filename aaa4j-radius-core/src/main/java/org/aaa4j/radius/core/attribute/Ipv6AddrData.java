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

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * "ipv6addr" attribute data type. The ipv6addr data is mapped to a {@link Inet6Address}.
 */
public final class Ipv6AddrData extends Data {

    private final Inet6Address value;

    /**
     * Constructs ipv6addr data from a given {@link Inet6Address} object.
     *
     * @param value the Inet6Address object
     */
    public Ipv6AddrData(Inet6Address value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public int length() {
        return 16;
    }

    /**
     * Gets the ipv6addr value.
     *
     * @return the ipv6addr data as a {@link Inet6Address}
     */
    public Inet6Address getValue() {
        return value;
    }

    /**
     * A codec for "ipv6addr" data.
     */
    public static final class Codec implements DataCodec<Ipv6AddrData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public Ipv6AddrData decode(CodecContext codecContext, byte[] bytes) {
            if (bytes.length != 16) {
                return null;
            }

            try {
                return new Ipv6AddrData((Inet6Address) Inet6Address.getByAddress(bytes));
            }
            catch (UnknownHostException e) {
                // Thrown if bytes is an invalid length, which is already handled
                return null;
            }
        }

        @Override
        public byte[] encode(CodecContext codecContext, Ipv6AddrData data) {
            return data.value.getAddress();
        }

    }

}
