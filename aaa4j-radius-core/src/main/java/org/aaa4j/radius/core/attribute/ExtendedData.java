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
 * "extended" attribute data type. Extended data contains a nested attribute in the extended attribute space.
 */
public class ExtendedData extends ContainerData {

    private final int extendedType;

    private final Data data;

    /**
     * Constructs extended data from a given extended attribute type and contained data.
     *
     * @param extendedType the extended type (int in range [0, 255])
     * @param data the contained data (length of data must be in range [1, 252])
     */
    public ExtendedData(int extendedType, Data data) {
        if (extendedType < 0 || extendedType > 255) {
            throw new IllegalArgumentException("Extended type must be in range [0, 255]");
        }

        this.extendedType = extendedType;
        this.data = Objects.requireNonNull(data);

        if (data.length() < 1 || data.length() > 252) {
            throw new IllegalArgumentException("Data length must be in range [1, 252]");
        }
    }

    @Override
    public int length() {
        return 1 + data.length();
    }

    /**
     * Get the extended type. For example, if this data contains a Frag-Status (241.1) attribute then the extended type
     * will be 1.
     *
     * @return the extended type
     */
    public int getExtendedType() {
        return extendedType;
    }

    /**
     * Gets the contained data.
     *
     * @return the contained data
     */
    public Data getData() {
        return data;
    }

    @Override
    public int[] getContainedType() {
        return new int[] { extendedType };
    }

    /**
     * A codec for "extended" data.
     */
    public static final class Codec implements DataCodec<ExtendedData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public ExtendedData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length < 2) {
                // The value field must contain at least one byte
                return null;
            }

            int extendedType = bytes[0] & 0xff;
            byte[] extData = Arrays.copyOfRange(bytes, 1, bytes.length);

            AttributeType extendedAttributeType = new AttributeType(parentAttributeType, extendedType);

            TlvDefinition tlvDefinition = codecContext.getDictionary().getTlvDefinition(extendedAttributeType);

            Data data = null;

            if (tlvDefinition != null) {
                data = tlvDefinition.getDataCodec().decode(codecContext, extendedAttributeType, extData);
            }

            if (data == null) {
                // An unknown or undecodable contained attribute is an invalid attribute; the octets are kept
                data = new RawData(extData);
            }

            return new ExtendedData(extendedType, data);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            ExtendedData extendedData = (ExtendedData) data;

            AttributeType extendedAttributeType = new AttributeType(parentAttributeType, extendedData.extendedType);

            byte[] extData;

            if (extendedData.data instanceof RawData) {
                // Written back exactly as received; the dictionary may still have no definition for it
                extData = ((RawData) extendedData.data).getValue();
            }
            else {
                TlvDefinition tlvDefinition = codecContext.getDictionary().getTlvDefinition(extendedAttributeType);

                if (tlvDefinition == null) {
                    // We can't encode the contained data if we have no definition for it
                    throw new IllegalArgumentException("Missing TLV dictionary definition for "
                            + extendedAttributeType);
                }

                extData = tlvDefinition.getDataCodec().encode(codecContext, extendedAttributeType,
                        extendedData.data);
            }

            byte[] bytes = new byte[extData.length + 1];

            bytes[0] = (byte) extendedData.extendedType;
            System.arraycopy(extData, 0, bytes, 1, extData.length);

            return bytes;
        }

    }

}
