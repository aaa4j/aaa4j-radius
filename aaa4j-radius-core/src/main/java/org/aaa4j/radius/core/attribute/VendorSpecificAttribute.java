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

import java.util.Deque;
import java.util.Objects;

/**
 * A vendor specific attribute in the standard attribute space. A vendor specific attribute is encapsulated in a
 * {@link StandardAttribute} attribute containing {@link VsaData} data. All {@link VendorSpecificAttribute} follow the
 * typical vendor attribute format (i.e., 1 byte for the vendor attribute type followed by the vendor data). Complex
 * vendor attributes are not applicable to {@link VendorSpecificAttribute}.
 *
 * @param <D> data type
 */
public class VendorSpecificAttribute<D extends Data> extends Attribute<D> {

    private final int vendorId;

    private final int vendorType;

    /**
     * Constructs a vendor specific attribute from an attribute type, vendor id, vendor type, and data.
     *
     * @param type the attribute type (integer in range [0, 255])
     * @param vendorId the vendor identifier (integer in range [0, 16777215])
     * @param vendorType the vendor type (integer in range [0, 255])
     * @param data the attribute data
     */
    public VendorSpecificAttribute(int type, int vendorId, int vendorType, D data) {
        super(new AttributeType(type, vendorId, vendorType), data);

        if (vendorId < 0 || vendorId > 16777215) {
            throw new IllegalArgumentException("Vendor identifier must be in range [0, 16777215]");
        }

        if (vendorType < 0 || vendorType > 255) {
            throw new IllegalArgumentException("Vendor type must be in range [0, 255]");
        }

        if (data.length() > 248) {
            throw new IllegalArgumentException("Data length must be in range [0, 248]");
        }

        this.vendorId = vendorId;
        this.vendorType = vendorType;
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
     * Gets the vendor type.
     *
     * @return the vendor type
     */
    public int getVendorType() {
        return vendorType;
    }

    /**
     * Codec for a {@link VendorSpecificAttribute} of a particular data type ({@link D}). An instance of
     * {@link VendorSpecificAttribute.Codec} is capable of decoding attributes with {@link VsaData} data into concrete
     * {@link VendorSpecificAttribute}s.
     *
     * @param <D> the attribute data type used in the codec's target attribute
     */
    public static class Codec<D extends Data> implements AttributeCodec {

        private final Factory<D> factory;

        private final DataCodec<D> dataCodec;

        /**
         * Constructs a codec for a {@link VendorSpecificAttribute} using the given data codec and attribute instance
         * factory.
         *
         * @param dataCodec the data codec
         * @param factory the attribute instance factory
         */
        public Codec(DataCodec<D> dataCodec, Factory<D> factory) {
            this.dataCodec = Objects.requireNonNull(dataCodec);
            this.factory = Objects.requireNonNull(factory);
        }

        @Override
        public int decode(CodecContext codecContext, Deque<Attribute<?>> attributeStack) {
            @SuppressWarnings("unchecked")
            Attribute<VsaData> attribute = (Attribute<VsaData>) attributeStack.removeFirst();

            int type = attribute.getType().head();
            int vendorId = attribute.getData().getVendorId();
            int vendorType = attribute.getData().getVendorType();
            byte[] vsaData = attribute.getData().getVsaData();

            D data = dataCodec.decode(codecContext,
                    new AttributeType(attribute.getType(), vendorId).with(vendorType), vsaData);

            if (data == null) {
                attributeStack.addFirst(attribute);

                // Inform the caller that this attribute shouldn't be processed anymore
                return 1;
            }

            VendorSpecificAttribute<D> vendorSpecificAttribute = factory.build(type, vendorId, vendorType, data);

            attributeStack.addFirst(vendorSpecificAttribute);

            return 0;
        }

        @Override
        public void encode(CodecContext codecContext, Deque<Attribute<?>> attributeStack) {
            @SuppressWarnings("unchecked")
            VendorSpecificAttribute<D> vendorSpecificAttribute =
                    (VendorSpecificAttribute<D>) attributeStack.removeFirst();

            int vendorId = vendorSpecificAttribute.vendorId;
            int vendorType = vendorSpecificAttribute.vendorType;
            byte[] data = dataCodec.encode(codecContext, vendorSpecificAttribute.getType(),
                    vendorSpecificAttribute.getData());

            VsaData vsaData = new VsaData(vendorId, vendorType, data);

            StandardAttribute<VsaData> attribute =
                    new StandardAttribute<>(vendorSpecificAttribute.getType().head(), vsaData);

            attributeStack.addFirst(attribute);
        }

    }

    /**
     * Attribute factory that returns a concrete instance of {@link VendorSpecificAttribute} or a subtype when given a
     * standard attribute type (integer in range [0, 255]), vendor id (integer in range [0, 16777215]), vendor type
     * (integer in range [0, 255]), and attribute data ({@link D}).
     *
     * @param <D> the attribute data type used in the codec's target attribute
     */
    @FunctionalInterface
    public interface Factory<D extends Data> {

        /**
         * Builds a concrete instance of {@link VendorSpecificAttribute}.
         *
         * @param type the attribute type (integer in range [0, 255])
         * @param vendorId the vendor identifier (integer in range [0, 16777215])
         * @param vendorType the vendor type (integer in range [0, 255])
         * @param data the attribute's data
         *
         * @return an instance of {@link VendorSpecificAttribute} or its subtypes with the provided vendor id, vendor
         * type, and data
         */
        VendorSpecificAttribute<D> build(int type, int vendorId, int vendorType, D data);

    }

}