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
import org.aaa4j.radius.core.dictionary.TlvFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        Objects.requireNonNull(tlvs);

        if (tlvs.isEmpty()) {
            throw new IllegalArgumentException("At least one TLV is required");
        }

        this.tlvs = tlvs;
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
     * Gets the first TLV of a specific class. Only the TLVs held directly are searched.
     *
     * @param tlvClass TLV class
     * @param <T> TLV type for {@code tlvClass} parameter
     *
     * @return an optional TLV of the given class
     */
    public <T extends Tlv> Optional<T> getTlv(Class<T> tlvClass) {
        for (Tlv tlv : tlvs) {
            if (tlvClass.isInstance(tlv)) {
                return Optional.of(tlvClass.cast(tlv));
            }
        }

        return Optional.empty();
    }

    /**
     * Gets all the TLVs of a specific class, in order. Only the TLVs held directly are searched.
     *
     * @param tlvClass TLV class
     * @param <T> TLV type for {@code tlvClass} parameter
     *
     * @return list of TLVs of the given class
     */
    public <T extends Tlv> List<T> getTlvs(Class<T> tlvClass) {
        List<T> found = new ArrayList<>();

        for (Tlv tlv : tlvs) {
            if (tlvClass.isInstance(tlv)) {
                found.add(tlvClass.cast(tlv));
            }
        }

        return found;
    }

    /**
     * A Type-Length-Value triplet used in {@link TlvData}. A dictionary may define a subclass for a particular TLV,
     * which fixes the type and is built when the TLV is decoded.
     */
    public static class Tlv {

        private final int type;

        private final Data data;

        /**
         * Constructs a TLV with the given type and data.
         *
         * @param type the type (int in range [0, 255] in the TLV
         * @param data the data in the TLV (length of data must be in range [1, 253])
         */
        public Tlv(int type, Data data) {
            if (type < 0 || type > 255) {
                throw new IllegalArgumentException("Type must be in range [0, 255]");
            }

            this.type = type;
            this.data = Objects.requireNonNull(data);

            if (data.length() < 1 || data.length() > 253) {
                throw new IllegalArgumentException("Data length must be in range [1, 253]");
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

                int tlvType = bytes[position] & 0xff;
                int tlvLength = bytes[position + 1] & 0xff;

                if (tlvLength < 3) {
                    // TLV length is invalid: it covers the type and length fields plus at least one data byte
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

                Data data = null;

                if (tlvDefinition != null) {
                    data = tlvDefinition.getDataCodec().decode(codecContext, tlvAttributeType, tlvData);
                }

                Tlv tlv;

                if (data == null) {
                    // An unknown or undecodable TLV is an invalid attribute, but the TLV containing it is not, so
                    // the octets are kept rather than rejecting the well-formed TLVs alongside it
                    tlv = new Tlv(tlvType, new RawData(tlvData));
                }
                else {
                    tlv = buildTlv(tlvDefinition, tlvType, data);
                }

                tlvs.add(tlv);

                position = position + tlvLength;
            }

            return new TlvData(tlvs);
        }

        private static Tlv buildTlv(TlvDefinition tlvDefinition, int tlvType, Data data) {
            TlvFactory tlvFactory = tlvDefinition.getTlvFactory();

            if (tlvFactory != null) {
                Tlv tlv = tlvFactory.build(data);

                if (tlv != null) {
                    return tlv;
                }
            }

            return new Tlv(tlvType, data);
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            TlvData tlvData = (TlvData) data;

            byte[][] encodedTlvs = new byte[tlvData.tlvs.size()][];

            int length = 0;

            for (int i = 0; i < tlvData.tlvs.size(); i++) {
                Tlv tlv = tlvData.tlvs.get(i);

                AttributeType tlvAttributeType = new AttributeType(parentAttributeType, tlv.type);

                if (tlv.data instanceof RawData) {
                    // Written back exactly as received; the dictionary may still have no definition for it
                    encodedTlvs[i] = ((RawData) tlv.data).getValue();
                }
                else {
                    TlvDefinition tlvDefinition = codecContext.getDictionary().getTlvDefinition(tlvAttributeType);

                    if (tlvDefinition == null) {
                        // We can't encode the TLV if we have no definition for it
                        throw new IllegalArgumentException("Missing TLV dictionary definition for "
                                + tlvAttributeType);
                    }

                    encodedTlvs[i] = tlvDefinition.getDataCodec().encode(codecContext, tlvAttributeType, tlv.data);
                }

                if (encodedTlvs[i].length < 1 || encodedTlvs[i].length > 253) {
                    throw new IllegalArgumentException("Encoded TLV data length must be in range [1, 253] for "
                            + tlvAttributeType);
                }

                length = length + 2 + encodedTlvs[i].length;
            }

            byte[] bytes = new byte[length];

            int position = 0;

            for (int i = 0; i < encodedTlvs.length; i++) {
                bytes[position] = (byte) tlvData.tlvs.get(i).type;
                bytes[position + 1] = (byte) (2 + encodedTlvs[i].length);

                System.arraycopy(encodedTlvs[i], 0, bytes, position + 2, encodedTlvs[i].length);

                position = position + 2 + encodedTlvs[i].length;
            }

            return bytes;
        }

    }

}
