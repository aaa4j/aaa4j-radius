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

import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.TlvDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * "vsa" attribute data type. vsa data encapsulates vendor-specific attributes: a vendor identifier followed by one or
 * more vendor sub-attributes in the format recommended by RFC 2865 (a 1-byte vendor type and a 1-byte vendor length per
 * sub-attribute).
 *
 * <p>
 * The number of sub-attributes is the wire layout: data decoded from a compact attribute (multiple sub-attributes in a
 * single Vendor-Specific attribute) re-encodes to the compact form it arrived as, and data constructed with a single
 * sub-attribute encodes to its own attribute.
 * </p>
 */
public class VsaData extends ContainerData {

    private final int vendorId;

    private final List<Vsa> vsas;

    /**
     * Constructs vsa data from a given vendor identifier and vendor sub-attributes. The sub-attributes are encoded into
     * a single Vendor-Specific attribute in the order given.
     *
     * @param vendorId the vendor identifier (int in range [0, 16777215])
     * @param vsas the vendor sub-attributes (at least one)
     */
    public VsaData(int vendorId, List<Vsa> vsas) {
        if (vendorId < 0 || vendorId > 16777215) {
            throw new IllegalArgumentException("Vendor identifier must be in range [0, 16777215]");
        }

        Objects.requireNonNull(vsas);

        if (vsas.isEmpty()) {
            throw new IllegalArgumentException("At least one vendor sub-attribute is required");
        }

        int length = 4;

        for (Vsa vsa : vsas) {
            Objects.requireNonNull(vsa);

            length = length + 2 + vsa.data.length();
        }

        if (length > 253) {
            throw new IllegalArgumentException("Vendor sub-attributes must fit in 249 bytes");
        }

        this.vendorId = vendorId;
        this.vsas = new ArrayList<>(vsas);
    }

    /**
     * Constructs vsa data from a given vendor identifier and a single vendor sub-attribute.
     *
     * @param vendorId the vendor identifier (int in range [0, 16777215])
     * @param vendorType the vendor type (int in range [0, 255])
     * @param data the vendor data
     */
    public VsaData(int vendorId, int vendorType, Data data) {
        this(vendorId, Collections.singletonList(new Vsa(vendorType, data)));
    }

    @Override
    public int length() {
        int length = 4;

        for (Vsa vsa : vsas) {
            length = length + 2 + vsa.data.length();
        }

        return length;
    }

    @Override
    public int[] getContainedType() {
        // Only a single sub-attribute maps to a single contained attribute; compact data contains several attributes
        // and so has no single contained type
        return vsas.size() == 1 ? new int[] { vendorId, vsas.get(0).vendorType } : null;
    }

    @Override
    public Attribute<?> build(Dictionary dictionary, int type) {
        if (vsas.size() == 1) {
            // A single sub-attribute maps to a single contained attribute, built from getContainedType() instead
            return null;
        }

        return VendorSpecificAttributeGroup.fromVsaData(dictionary, type, this);
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
     * Gets the vendor sub-attributes.
     *
     * @return the vendor sub-attributes, in the order they appear in the attribute
     */
    public List<Vsa> getVsas() {
        return Collections.unmodifiableList(vsas);
    }

    /**
     * A vendor sub-attribute (vendor type and data) used in {@link VsaData}.
     */
    public static final class Vsa {

        private final int vendorType;

        private final Data data;

        /**
         * Constructs a vendor sub-attribute with the given vendor type and data.
         *
         * @param vendorType the vendor type (int in range [0, 255])
         * @param data the data in the sub-attribute (length of data must be in range [0, 247])
         */
        public Vsa(int vendorType, Data data) {
            if (vendorType < 0 || vendorType > 255) {
                throw new IllegalArgumentException("Vendor type must be in range [0, 255]");
            }

            this.vendorType = vendorType;
            this.data = Objects.requireNonNull(data);

            // Unlike RFC 6929 TLVs, RFC 2865 doesn't require a vendor sub-attribute to carry data, so zero-length
            // data is permitted
            if (data.length() > 247) {
                throw new IllegalArgumentException("Data length must be in range [0, 247]");
            }
        }

        /**
         * Gets the vendor type.
         *
         * @return the vendor type (int in range [0, 255])
         */
        public int getVendorType() {
            return vendorType;
        }

        /**
         * Gets the sub-attribute data.
         *
         * @return the sub-attribute data
         */
        public Data getData() {
            return data;
        }

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
            if (bytes.length < 6) {
                return null;
            }

            int vendorId = (bytes[0] & 0xff) << 24
                    | (bytes[1] & 0xff) << 16
                    | (bytes[2] & 0xff) << 8
                    | (bytes[3] & 0xff);

            List<Vsa> vsas = new ArrayList<>();

            int position = 4;

            while (position < bytes.length) {
                if (position + 2 > bytes.length) {
                    // Not enough bytes for a sub-attribute header
                    return null;
                }

                int vendorType = bytes[position] & 0xff;
                int vendorLength = bytes[position + 1] & 0xff;

                if (vendorLength < 2) {
                    // The vendor length covers at least the vendor type and vendor length fields
                    return null;
                }

                if (vendorLength > bytes.length - position) {
                    // Not enough bytes in the attribute for this sub-attribute
                    return null;
                }

                byte[] vsaData = Arrays.copyOfRange(bytes, position + 2, position + vendorLength);

                AttributeType vsaAttributeType = new AttributeType(new AttributeType(parentAttributeType, vendorId),
                        vendorType);

                TlvDefinition tlvDefinition = codecContext.getDictionary().getTlvDefinition(vsaAttributeType);

                Data data = null;

                if (tlvDefinition != null) {
                    data = tlvDefinition.getDataCodec().decode(codecContext, vsaAttributeType, vsaData);
                }

                if (data == null) {
                    // An unknown or undecodable sub-attribute is an invalid attribute, but the attribute containing
                    // it is not, so the octets are kept rather than rejecting the sub-attributes alongside it
                    data = new RawData(vsaData);
                }

                vsas.add(new Vsa(vendorType, data));

                position = position + vendorLength;
            }

            return new VsaData(vendorId, vsas);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            VsaData vsaData = (VsaData) data;

            // The data codecs, not Data.length(), decide how many bytes each sub-attribute occupies, so everything
            // is encoded before the buffer and the vendor length fields are written
            byte[][] encodedVsas = new byte[vsaData.vsas.size()][];

            int length = 4;

            for (int i = 0; i < vsaData.vsas.size(); i++) {
                Vsa vsa = vsaData.vsas.get(i);

                AttributeType vsaAttributeType = new AttributeType(
                        new AttributeType(parentAttributeType, vsaData.vendorId), vsa.vendorType);

                if (vsa.data instanceof RawData) {
                    // Written back exactly as received; the dictionary may still have no definition for it
                    encodedVsas[i] = ((RawData) vsa.data).getValue();
                }
                else {
                    TlvDefinition tlvDefinition = codecContext.getDictionary().getTlvDefinition(vsaAttributeType);

                    if (tlvDefinition == null) {
                        // We can't encode the sub-attribute if we have no definition for it
                        throw new IllegalArgumentException("Missing TLV dictionary definition for "
                                + vsaAttributeType);
                    }

                    encodedVsas[i] = tlvDefinition.getDataCodec().encode(codecContext, vsaAttributeType, vsa.data);
                }

                if (encodedVsas[i].length > 247) {
                    throw new IllegalArgumentException("Encoded sub-attribute data length must be in range [0, 247]"
                            + " for " + vsaAttributeType);
                }

                length = length + 2 + encodedVsas[i].length;
            }

            if (length > 253) {
                throw new IllegalArgumentException(
                        "Encoded vsa data length " + length + " exceeds the maximum length 253");
            }

            byte[] bytes = new byte[length];

            bytes[0] = (byte) ((vsaData.vendorId & 0xff000000) >>> 24);
            bytes[1] = (byte) ((vsaData.vendorId & 0x00ff0000) >>> 16);
            bytes[2] = (byte) ((vsaData.vendorId & 0x0000ff00) >>> 8);
            bytes[3] = (byte) (vsaData.vendorId & 0x000000ff);

            int position = 4;

            for (int i = 0; i < encodedVsas.length; i++) {
                bytes[position] = (byte) vsaData.vsas.get(i).vendorType;
                bytes[position + 1] = (byte) (2 + encodedVsas[i].length);

                System.arraycopy(encodedVsas[i], 0, bytes, position + 2, encodedVsas[i].length);

                position = position + 2 + encodedVsas[i].length;
            }

            return bytes;
        }

    }

}
