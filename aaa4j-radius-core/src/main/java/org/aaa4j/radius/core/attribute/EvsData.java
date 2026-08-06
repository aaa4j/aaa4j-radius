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

import org.aaa4j.radius.core.dictionary.TlvDefinition;

import java.util.Arrays;
import java.util.Objects;

/**
 * "evs" attribute data type. evs data extends the vendor-specific space.
 */
public final class EvsData extends ContainerData {

    private final int vendorId;

    private final int vendorType;

    private final Data data;

    /**
     * Constructs evs data from a given vendor identifier, vendor type, and vendor data.
     *
     * @param vendorId the vendor identifier
     * @param vendorType the vendor type (int in range [0, 255])
     * @param data the vendor data (length of data must be at least 1)
     */
    public EvsData(int vendorId, int vendorType, Data data) {
        if (vendorType < 0 || vendorType > 255) {
            throw new IllegalArgumentException("Vendor type must be in range [0, 255]");
        }

        this.vendorId = vendorId;
        this.vendorType = vendorType;
        this.data = Objects.requireNonNull(data);

        if (data.length() < 1) {
            throw new IllegalArgumentException("Vendor data length must be at least 1");
        }
    }

    @Override
    public int length() {
        return 5 + data.length();
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
    public Data getData() {
        return data;
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
            if (bytes.length < 6) {
                // The vendor data field must contain at least one byte
                return null;
            }

            int vendorId = (bytes[0] & 0xff) << 24
                    | (bytes[1] & 0xff) << 16
                    | (bytes[2] & 0xff) << 8
                    | (bytes[3] & 0xff);

            int vendorType = bytes[4] & 0xff;

            byte[] evsData = Arrays.copyOfRange(bytes, 5, bytes.length);

            AttributeType evsAttributeType = new AttributeType(new AttributeType(parentAttributeType, vendorId),
                    vendorType);

            TlvDefinition tlvDefinition = codecContext.getDictionary().getTlvDefinition(evsAttributeType);

            Data data = null;

            if (tlvDefinition != null) {
                data = tlvDefinition.getDataCodec().decode(codecContext, evsAttributeType, evsData);
            }

            if (data == null) {
                // An unknown or undecodable contained attribute is an invalid attribute; the octets are kept
                data = new RawData(evsData);
            }

            return new EvsData(vendorId, vendorType, data);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            EvsData evsData = (EvsData) data;

            AttributeType evsAttributeType = new AttributeType(new AttributeType(parentAttributeType,
                    evsData.vendorId), evsData.vendorType);

            byte[] vendorData;

            if (evsData.data instanceof RawData) {
                // Written back exactly as received; the dictionary may still have no definition for it
                vendorData = ((RawData) evsData.data).getValue();
            }
            else {
                TlvDefinition tlvDefinition = codecContext.getDictionary().getTlvDefinition(evsAttributeType);

                if (tlvDefinition == null) {
                    // We can't encode the contained data if we have no definition for it
                    throw new IllegalArgumentException("Missing TLV dictionary definition for " + evsAttributeType);
                }

                vendorData = tlvDefinition.getDataCodec().encode(codecContext, evsAttributeType, evsData.data);
            }

            byte[] bytes = new byte[5 + vendorData.length];

            bytes[0] = (byte) ((evsData.vendorId & 0xff000000) >>> 24);
            bytes[1] = (byte) ((evsData.vendorId & 0x00ff0000) >>> 16);
            bytes[2] = (byte) ((evsData.vendorId & 0x0000ff00) >>> 8);
            bytes[3] = (byte) (evsData.vendorId & 0x000000ff);
            bytes[4] = (byte) evsData.vendorType;

            System.arraycopy(vendorData, 0, bytes, 5, vendorData.length);

            return bytes;
        }

    }

}
