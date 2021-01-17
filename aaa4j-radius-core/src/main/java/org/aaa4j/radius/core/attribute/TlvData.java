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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * "tlv" attribute data type. A TlvData object contains one or more type-level-values ({@link Tlv}s).
 */
public final class TlvData extends Data {

    private final List<Tlv> tlvs;

    /**
     * Constructs tlv data from a list of {@link Tlv}s.
     *
     * @param tlvs the list of TLVs
     */
    public TlvData(List<Tlv> tlvs) {
        this.tlvs = Objects.requireNonNull(tlvs);
    }

    @Override
    public int length() {
        return tlvs.stream()
                .map(tlv -> 2 + tlv.data.length())
                .reduce(0, Integer::sum);
    }

    /**
     * Gets the TLVs.
     *
     * @return the TLVs
     */
    public List<Tlv> getTlvs() {
        return Collections.unmodifiableList(tlvs);
    }

    /**
     * A Type-Length-Value triplet used in {@link TlvData}.
     */
    public static final class Tlv {

        private final int type;

        private final Data data;

        /**
         * Constructs a TLV with the given type and data.
         *
         * @param type the type (int in range [0, 255] in the TLV
         * @param data the data in the TLV (length of data must be in range [0, 253])
         */
        public Tlv(int type, Data data) {
            if (type < 0 || type > 255) {
                throw new IllegalArgumentException("Type must be in range [0, 255]");
            }

            this.type = type;
            this.data = Objects.requireNonNull(data);

            if (data.length() > 253) {
                throw new IllegalArgumentException("Data length must be in range [0, 253]");
            }
        }

        /**
         * Gets the TLV type (int in range [0, 255]).
         *
         * @return the TLV type
         */
        public int getType() {
            return type;
        }

        /**
         * Gets the TLV data.
         *
         * @return the TLV data
         */
        public Data getData() {
            return data;
        }

    }

    /**
     * A codec for "tlv" data.
     */
    public static final class Codec implements DataCodec<TlvData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public TlvData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length < 2) {
                return null;
            }

            List<Tlv> tlvs = new ArrayList<>();

            int position = 0;

            while (position < bytes.length) {
                if (position + 2 >= bytes.length) {
                    // We don't have enough bytes
                    return null;
                }

                int tlvType = bytes[position];
                int tlvLength = bytes[position + 1];

                if (tlvLength < 2) {
                    // TLV length is invalid
                    return null;
                }

                if (tlvLength > (bytes.length - position)) {
                    // Not enough bytes in the attribute for this TLV
                    return null;
                }

                byte[] tlvData = new byte[tlvLength - 2];
                System.arraycopy(bytes, position + 2, tlvData, 0, tlvLength - 2);

                AttributeType tlvAttributeType = new AttributeType(parentAttributeType, tlvType);

                TlvDefinition tlvDefinition = codecContext.getDictionary().getTlvDefinition(tlvAttributeType);

                if (tlvDefinition == null) {
                    // We can't decode the TLV if we have no definition for it
                    return null;
                }

                Data data = tlvDefinition.getDataCodec().decode(codecContext, tlvAttributeType, tlvData);

                if (data == null) {
                    // The data codec couldn't decode the TLV data
                    return null;
                }

                Tlv tlv = new Tlv(tlvType, data);

                tlvs.add(tlv);

                position = position + tlvLength;
            }

            return new TlvData(tlvs);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            TlvData tlvData = (TlvData) data;

            byte[] bytes = new byte[data.length()];

            int position = 0;

            for (Tlv tlv : tlvData.tlvs) {
                int tlvType = tlv.type;
                int tlvLength = 2 + tlv.data.length();

                bytes[position] = (byte) tlvType;
                bytes[position + 1] = (byte) tlvLength;

                AttributeType tlvAttributeType = new AttributeType(parentAttributeType, tlvType);

                TlvDefinition tlvDefinition = codecContext.getDictionary().getTlvDefinition(tlvAttributeType);

                if (tlvDefinition == null) {
                    // We can't decode the TLV if we have no definition for it
                    throw new IllegalArgumentException("Missing TLV dictionary definition for " + tlvAttributeType);
                }

                byte[] tlvDataBytes = tlvDefinition.getDataCodec().encode(codecContext, tlvAttributeType, tlv.data);

                System.arraycopy(tlvDataBytes, 0, bytes, position + 2, tlvDataBytes.length);

                position = position + 2 + tlvDataBytes.length;
            }

            return bytes;
        }

    }

}
