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
import java.util.Arrays;
import java.util.Objects;

/**
 * "ipv6prefix" attribute data type.
 */
public final class Ipv6PrefixData extends Data {

    private final int prefixLength;

    private final byte[] prefixBytes;

    private final Inet6Address prefixAddress;

    /**
     * Constructs ipv6prefix data from a given prefix length and prefix bytes. The prefix bytes may be longer than
     * prefix length allows but must contain zero bits outside of the prefix length.
     *
     * @param prefixLength the prefix length (int in range [0, 128])
     * @param prefixBytes the prefix byte array
     */
    public Ipv6PrefixData(int prefixLength, byte[] prefixBytes) {
        if (prefixLength > 128 || prefixLength < 0) {
            throw new IllegalArgumentException("Prefix length must be in range [0, 128]");
        }

        Objects.requireNonNull(prefixBytes);

        if (((prefixLength + 7) / 8) > prefixBytes.length) {
            throw new IllegalArgumentException("Prefix bytes is not large enough to contain the prefix length");
        }

        byte[] maskedPrefixBytes = mask(prefixLength, prefixBytes);

        for (int i = 0; i < prefixBytes.length; i++) {
            if (maskedPrefixBytes[i] != prefixBytes[i]) {
                throw new IllegalArgumentException(
                        "The prefix bytes must not contain non-zero bits outside of the prefix length");
            }
        }

        this.prefixLength = prefixLength;
        this.prefixBytes = Arrays.copyOf(maskedPrefixBytes, prefixBytes.length);

        try {
            this.prefixAddress = (Inet6Address) Inet6Address.getByAddress(maskedPrefixBytes);
        }
        catch (UnknownHostException e) {
            // Thrown if bytes is an invalid length, which shouldn't happen
            throw new AssertionError(e);
        }
    }

    /**
     * Constructs ipv6prefix data from a given prefix length and prefix address. The prefix address must contain zero
     * bits outside of the prefix length.
     *
     * @param prefixLength the prefix length (int in range [0, 128])
     * @param prefixAddress the prefix address
     */
    public Ipv6PrefixData(int prefixLength, Inet6Address prefixAddress) {
        if (prefixLength > 128 || prefixLength < 0) {
            throw new IllegalArgumentException("Prefix length must be in range [0, 128]");
        }

        Objects.requireNonNull(prefixAddress);

        byte[] prefixBytes = prefixAddress.getAddress();

        byte[] maskedPrefixBytes = mask(prefixLength, prefixBytes);

        for (int i = 0; i < prefixBytes.length; i++) {
            if (maskedPrefixBytes[i] != prefixBytes[i]) {
                throw new IllegalArgumentException(
                        "The prefix address must not contain non-zero bits outside of the prefix length");
            }
        }

        this.prefixLength = prefixLength;
        this.prefixBytes = Arrays.copyOf(maskedPrefixBytes, ((prefixLength + 7) / 8));

        try {
            this.prefixAddress = (Inet6Address) Inet6Address.getByAddress(maskedPrefixBytes);
        }
        catch (UnknownHostException e) {
            // Thrown if bytes is an invalid length, which shouldn't happen
            throw new AssertionError(e);
        }
    }

    @Override
    public int length() {
        return 2 + prefixBytes.length;
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
    public Inet6Address getPrefixAddress() {
        return prefixAddress;
    }

    /**
     * A codec for "ipv6prefix" data.
     */
    public static final class Codec implements DataCodec<Ipv6PrefixData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public Ipv6PrefixData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length > 18 || bytes.length < 2) {
                return null;
            }

            if (bytes[0] != 0x00) {
                // First byte is reserved and must be 0x00
                return null;
            }

            int prefixLength = bytes[1] & 0xff;

            if (prefixLength > 128) {
                return null;
            }

            if (((prefixLength + 7) / 8) > bytes.length - 2) {
                // Not enough prefix bytes for the prefix length
                return null;
            }

            byte[] prefixBytes = Arrays.copyOfRange(bytes, 2, bytes.length);

            byte[] maskedPrefixBytes = mask(prefixLength, prefixBytes);

            for (int i = 0; i < prefixBytes.length; i++) {
                if (maskedPrefixBytes[i] != prefixBytes[i]) {
                    // The raw address bytes must not contain non-zero bits outside of the prefix length
                    return null;
                }
            }

            return new Ipv6PrefixData(prefixLength, prefixBytes);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            Ipv6PrefixData ipv6PrefixData = (Ipv6PrefixData) data;

            byte[] bytes = new byte[2 + ipv6PrefixData.prefixBytes.length];

            bytes[1] = (byte) ipv6PrefixData.prefixLength;

            System.arraycopy(ipv6PrefixData.prefixBytes, 0, bytes, 2, ipv6PrefixData.prefixBytes.length);

            return bytes;
        }

    }

    private static byte[] mask(int prefixLength, byte[] prefixBytes) {
        byte[] maskedPrefixBytes = new byte[16];

        System.arraycopy(prefixBytes, 0, maskedPrefixBytes, 0, prefixBytes.length);

        long prefixValue1 = (maskedPrefixBytes[0] & 0xffL) << 56
                | (maskedPrefixBytes[1] & 0xffL) << 48
                | (maskedPrefixBytes[2] & 0xffL) << 40
                | (maskedPrefixBytes[3] & 0xffL) << 32
                | (maskedPrefixBytes[4] & 0xffL) << 24
                | (maskedPrefixBytes[5] & 0xffL) << 16
                | (maskedPrefixBytes[6] & 0xffL) << 8
                | (maskedPrefixBytes[7] & 0xffL);

        long prefixValue2 = (maskedPrefixBytes[8] & 0xffL) << 56
                | (maskedPrefixBytes[9] & 0xffL) << 48
                | (maskedPrefixBytes[10] & 0xffL) << 40
                | (maskedPrefixBytes[11] & 0xffL) << 32
                | (maskedPrefixBytes[12] & 0xffL) << 24
                | (maskedPrefixBytes[13] & 0xffL) << 16
                | (maskedPrefixBytes[14] & 0xffL) << 8
                | (maskedPrefixBytes[15] & 0xffL);

        long maskedPrefixValue1 = prefixLength == 0
                ? 0L
                : prefixValue1 & (0xffffffffffffffffL << Math.max(0, 64 - prefixLength));

        long maskedPrefixValue2 = prefixLength <= 64
                ? 0L
                : prefixValue2 & (0xffffffffffffffffL << Math.max(0, 128 - prefixLength));

        maskedPrefixBytes[0] = (byte) ((maskedPrefixValue1 & 0xff00000000000000L) >>> 56);
        maskedPrefixBytes[1] = (byte) ((maskedPrefixValue1 & 0x00ff000000000000L) >>> 48);
        maskedPrefixBytes[2] = (byte) ((maskedPrefixValue1 & 0x0000ff0000000000L) >>> 40);
        maskedPrefixBytes[3] = (byte) ((maskedPrefixValue1 & 0x000000ff00000000L) >>> 32);
        maskedPrefixBytes[4] = (byte) ((maskedPrefixValue1 & 0x00000000ff000000L) >>> 24);
        maskedPrefixBytes[5] = (byte) ((maskedPrefixValue1 & 0x0000000000ff0000L) >>> 16);
        maskedPrefixBytes[6] = (byte) ((maskedPrefixValue1 & 0x000000000000ff00L) >>> 8);
        maskedPrefixBytes[7] = (byte) (maskedPrefixValue1 & 0x00000000000000ffL);

        maskedPrefixBytes[8] = (byte) ((maskedPrefixValue2 & 0xff00000000000000L) >>> 56);
        maskedPrefixBytes[9] = (byte) ((maskedPrefixValue2 & 0x00ff000000000000L) >>> 48);
        maskedPrefixBytes[10] = (byte) ((maskedPrefixValue2 & 0x0000ff0000000000L) >>> 40);
        maskedPrefixBytes[11] = (byte) ((maskedPrefixValue2 & 0x000000ff00000000L) >>> 32);
        maskedPrefixBytes[12] = (byte) ((maskedPrefixValue2 & 0x00000000ff000000L) >>> 24);
        maskedPrefixBytes[13] = (byte) ((maskedPrefixValue2 & 0x0000000000ff0000L) >>> 16);
        maskedPrefixBytes[14] = (byte) ((maskedPrefixValue2 & 0x000000000000ff00L) >>> 8);
        maskedPrefixBytes[15] = (byte) (maskedPrefixValue2 & 0x00000000000000ffL);

        return maskedPrefixBytes;
    }

}
