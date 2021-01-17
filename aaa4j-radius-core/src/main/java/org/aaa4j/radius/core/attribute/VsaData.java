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
 * "vsa" attribute data type. vsa data encapsulates vendor-specific attributes.
 */
public class VsaData extends ContainerData {

    private final int vendorId;

    private final byte[] vsaData;

    /**
     * Constructs vsa data from a given vendor identifier and vendor data.
     *
     * @param vendorId the vendor identifier
     * @param vsaData the vendor data
     */
    public VsaData(int vendorId, byte[] vsaData) {
        this.vendorId = vendorId;
        this.vsaData = Objects.requireNonNull(vsaData);
    }

    @Override
    public int length() {
        return 4 + vsaData.length;
    }

    @Override
    public int[] getContainedType() {
        return new int[] { vendorId };
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
     * Gets the vendor data.
     *
     * @return the vendor data
     */
    public byte[] getVsaData() {
        return vsaData;
    }

    /**
     * A codec for "vsa" data.
     */
    public static class Codec implements DataCodec<VsaData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public VsaData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length < 4) {
                return null;
            }

            int vendorId = (bytes[0] & 0xff) << 24
                    | (bytes[1] & 0xff) << 16
                    | (bytes[2] & 0xff) << 8
                    | bytes[3] & 0xff;

            byte[] vsaData = Arrays.copyOfRange(bytes, 4, bytes.length);

            return new VsaData(vendorId, vsaData);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            VsaData vsaData = (VsaData) data;

            byte[] bytes = new byte[4 + vsaData.vsaData.length];

            bytes[0] = (byte) ((vsaData.vendorId & 0xff000000) >>> 24);
            bytes[1] = (byte) ((vsaData.vendorId & 0x00ff0000) >>> 16);
            bytes[2] = (byte) ((vsaData.vendorId & 0x0000ff00) >>> 8);
            bytes[3] = (byte) (vsaData.vendorId & 0x000000ff);

            System.arraycopy(vsaData.vsaData, 0, bytes, 4, vsaData.vsaData.length);

            return bytes;
        }

    }

}
