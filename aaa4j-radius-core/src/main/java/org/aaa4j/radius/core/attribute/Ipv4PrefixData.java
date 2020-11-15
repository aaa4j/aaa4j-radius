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
import java.util.Arrays;
import java.util.Objects;

/**
 * "ipv4prefix" attribute data type.
 */
public final class Ipv4PrefixData extends Data {

    private final int prefixLength;

    private final Inet4Address address;

    /**
     * Constructs ipv4prefix data from a given prefix length and address ({@link Inet4Address} object). The address
     * returned by {@link #getAddress()} is masked accordingly to the prefix length.
     *
     * @param prefixLength the prefix length (int in range [0, 32])
     * @param address the Inet4Address object
     */
    public Ipv4PrefixData(int prefixLength, Inet4Address address) {
        if (prefixLength > 32 || prefixLength < 0) {
            throw new IllegalArgumentException("Prefix length must be in range [0, 32]");
        }

        Objects.requireNonNull(address);

        this.prefixLength = prefixLength;

        try {
            this.address = (Inet4Address) Inet4Address.getByAddress(mask(prefixLength, address.getAddress()));
        }
        catch (UnknownHostException e) {
            // Thrown if bytes is an invalid length, which shouldn't happen
            throw new AssertionError(e);
        }
    }

    @Override
    public int length() {
        return 6;
    }

    /**
     * Gets the prefix length.
     *
     * @return the prefix length
     */
    public int getPrefixLength() {
        return prefixLength;
    }

    /**
     * Gets the address. The address will be masked accordingly to the prefix length.
     *
     * @return the address
     */
    public Inet4Address getAddress() {
        return address;
    }

    /**
     * A codec for "ipv4prefix" data.
     */
    public static final class Codec implements DataCodec<Ipv4PrefixData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public Ipv4PrefixData decode(CodecContext codecContext, byte[] bytes) {
            if (bytes.length != 6) {
                return null;
            }

            int prefixLength = bytes[1] & 0xff;

            if (prefixLength > 32) {
                return null;
            }

            byte[] addressBytes = mask(prefixLength, Arrays.copyOfRange(bytes, 2, bytes.length));

            try {
                return new Ipv4PrefixData(prefixLength, (Inet4Address) Inet4Address.getByAddress(addressBytes));
            }
            catch (UnknownHostException e) {
                // Thrown if bytes is an invalid length, which shouldn't happen
                throw new AssertionError(e);
            }
        }

        @Override
        public byte[] encode(CodecContext codecContext, Ipv4PrefixData data) {
            byte[] bytes = new byte[6];

            bytes[1] = (byte) data.prefixLength;

            System.arraycopy(data.address.getAddress(), 0, bytes, 2, 4);

            return bytes;
        }

    }

    private static byte[] mask(int prefixLength, byte[] addressBytes) {
        int addressValue = (addressBytes[0] & 0xff) << 24
                | (addressBytes[1] & 0xff) << 16
                | (addressBytes[2] & 0xff) << 8
                | addressBytes[3] & 0xff;

        int maskedAddressValue = prefixLength == 0 ? 0 : addressValue & (0xffffffff << (32 - prefixLength));

        byte[] maskedAddressBytes = new byte[4];

        maskedAddressBytes[0] = (byte) ((maskedAddressValue & 0xff000000) >>> 24);
        maskedAddressBytes[1] = (byte) ((maskedAddressValue & 0x00ff0000) >>> 16);
        maskedAddressBytes[2] = (byte) ((maskedAddressValue & 0x0000ff00) >>> 8);
        maskedAddressBytes[3] = (byte) (maskedAddressValue & 0x000000ff);

        return maskedAddressBytes;
    }

}
