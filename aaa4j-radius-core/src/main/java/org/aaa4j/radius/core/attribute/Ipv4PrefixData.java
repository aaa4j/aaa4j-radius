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

    private final byte[] prefixBytes;

    private final Inet4Address prefixAddress;

    /**
     * Constructs ipv4prefix data from a given prefix length and prefix bytes. The prefix bytes must contain zero bits
     * outside of the prefix length.
     *
     * @param prefixLength the prefix length (int in range [0, 32])
     * @param prefixBytes the prefix byte array
     */
    public Ipv4PrefixData(int prefixLength, byte[] prefixBytes) {
        if (prefixLength > 32 || prefixLength < 0) {
            throw new IllegalArgumentException("Prefix length must be in range [0, 32]");
        }

        Objects.requireNonNull(prefixBytes);

        if (prefixBytes.length != 4) {
            throw new IllegalArgumentException("Prefix bytes must have length 4");
        }

        byte[] maskedPrefixBytes = mask(prefixLength, prefixBytes);

        boolean allZero = true;

        for (int i = 0; i < prefixBytes.length; i++) {
            if (maskedPrefixBytes[i] != prefixBytes[i]) {
                throw new IllegalArgumentException(
                        "The prefix bytes must not contain non-zero bits outside of the prefix length");
            }

            if (maskedPrefixBytes[i] != 0x00) {
                allZero = false;
            }
        }

        if (allZero && prefixLength != 32) {
            throw new IllegalArgumentException("Prefix length must be 32 when all prefix bits are zero");
        }

        this.prefixLength = prefixLength;
        this.prefixBytes = prefixBytes;

        try {
            this.prefixAddress = (Inet4Address) Inet4Address.getByAddress(maskedPrefixBytes);
        }
        catch (UnknownHostException e) {
            // Thrown if bytes is an invalid length, which shouldn't happen
            throw new AssertionError(e);
        }
    }

    /**
     * Constructs ipv4prefix data from a given prefix length and address ({@link Inet4Address} object).
     *
     * @param prefixLength the prefix length (int in range [0, 32])
     * @param address the Inet4Address object
     */
    public Ipv4PrefixData(int prefixLength, Inet4Address address) {
        this(prefixLength, address.getAddress());
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
     * Gets the prefix bytes.
     *
     * @return the prefix byte array
     */
    public byte[] getPrefixBytes() {
        return Arrays.copyOf(prefixBytes, prefixBytes.length);
    }

    /**
     * Gets the prefix address.
     *
     * @return the prefix address
     */
    public Inet4Address getPrefixAddress() {
        return prefixAddress;
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

            if (bytes[0] != 0x00) {
                // First byte is reserved and must be 0x00
                return null;
            }

            int prefixLength = bytes[1] & 0xff;

            if (prefixLength > 32) {
                return null;
            }

            byte[] prefixBytes = Arrays.copyOfRange(bytes, 2, bytes.length);

            byte[] maskedPrefixBytes = mask(prefixLength, prefixBytes);

            boolean allZero = true;

            for (int i = 0; i < prefixBytes.length; i++) {
                if (maskedPrefixBytes[i] != prefixBytes[i]) {
                    // The raw address bytes must not contain non-zero bits outside of the prefix length
                    return null;
                }

                if (maskedPrefixBytes[i] != 0x00) {
                    allZero = false;
                }
            }

            if (allZero && prefixLength != 32) {
                // Prefix length must be 32 when all prefix bits are zero
                return null;
            }

            try {
                return new Ipv4PrefixData(prefixLength, (Inet4Address) Inet4Address.getByAddress(prefixBytes));
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

            System.arraycopy(data.prefixBytes, 0, bytes, 2, 4);

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
