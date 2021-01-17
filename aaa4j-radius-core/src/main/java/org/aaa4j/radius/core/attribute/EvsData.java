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

/**
 * "evs" attribute data type. evs data extends the vendor-specific space.
 */
public final class EvsData extends ContainerData {

    private final int vendorId;

    private final int vendorType;

    private final byte[] evsData;

    /**
     * Constructs evs data from a given vendor identifier, vendor type, and vendor data.
     *
     * @param vendorId the vendor identifier
     * @param vendorType the vendor type (int in range [0, 255])
     * @param evsData the vendor data
     */
    public EvsData(int vendorId, int vendorType, byte[] evsData) {
        if (vendorType < 0 || vendorType > 255) {
            throw new IllegalArgumentException("Vendor type must be in range [0, 255]");
        }

        this.vendorId = vendorId;
        this.vendorType = vendorType;
        this.evsData = Objects.requireNonNull(evsData);
    }

    @Override
    public int length() {
        return 5 + evsData.length;
    }

    @Override
    public int[] getContainedType() {
        return new int[] { vendorId, vendorType };
    }

    /**
     * Gets the vendor identifier.
     *
     * @return the vendor identifier
     */
    public int getVendorId() {
        return vendorId;
    }

    /**
     * Gets the extended vendor type.
     *
     * @return the extended vendor type.
     */
    public int getVendorType() {
        return vendorType;
    }

    /**
     * Gets the extended vendor data.
     *
     * @return the extended vendor data
     */
    public byte[] getEvsData() {
        return evsData;
    }

    /**
     * A codec for "evs" data.
     */
    public static class Codec implements DataCodec<EvsData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public EvsData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length < 5) {
                return null;
            }

            int vendorId = (bytes[0] & 0xff) << 24
                    | (bytes[1] & 0xff) << 16
                    | (bytes[2] & 0xff) << 8
                    | bytes[3] & 0xff;

            int vendorType = bytes[4];

            byte[] evsData = Arrays.copyOfRange(bytes, 5, bytes.length);

            return new EvsData(vendorId, vendorType, evsData);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            EvsData evsData = (EvsData) data;

            byte[] bytes = new byte[5 + evsData.evsData.length];

            bytes[0] = (byte) ((evsData.vendorId & 0xff000000) >>> 24);
            bytes[1] = (byte) ((evsData.vendorId & 0x00ff0000) >>> 16);
            bytes[2] = (byte) ((evsData.vendorId & 0x0000ff00) >>> 8);
            bytes[3] = (byte) (evsData.vendorId & 0x000000ff);
            bytes[4] = (byte) evsData.vendorType;

            System.arraycopy(evsData.evsData, 0, bytes, 5, evsData.evsData.length);

            return bytes;
        }

    }

}
