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
 * An extended attribute in the extended attribute space. An extended attribute is encapsulated in a
 * {@link StandardAttribute} attribute containing {@link ExtendedData} data.
 *
 * @param <D> data type
 */
public class ExtendedAttribute<D extends Data> extends Attribute<D> {

    private final int extendedType;

    /**
     * Constructs an extended attribute from an attribute type, extended type, and data.
     *
     * @param type the attribute type (int in range [0, 255])
     * @param extendedType the attribute extended type (int in range [0, 255])
     * @param data the attribute data
     */
    public ExtendedAttribute(int type, int extendedType, D data) {
        super(new AttributeType(type, extendedType), data);

        if (extendedType < 0 || extendedType > 255) {
            throw new IllegalArgumentException("Extended type must be in range [0, 255]");
        }

        if (data.length() > 252) {
            throw new IllegalArgumentException("Data length must be in range [0, 252]");
        }

        this.extendedType = extendedType;
    }

    /**
     * Gets the extended attribute type.
     *
     * @return the extended attribute type (int in range [0, 255])
     */
    public int getExtendedType() {
        return extendedType;
    }

    /**
     * Codec for a {@link ExtendedAttribute} of a particular data type ({@link D}). An instance of
     * {@link ExtendedAttribute.Codec} is capable of decoding attributes with {@link ExtendedData} data into extended
     * attributes with a particular data type.
     *
     * @param <D> the attribute data type used in the codec's target attribute
     */
    public static class Codec<D extends Data> implements AttributeCodec {

        private final Factory<D> factory;

        private final DataCodec<D> dataCodec;

        /**
         * Constructs a codec for a {@link ExtendedAttribute} using the given data codec and attribute instance factory.
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
            Attribute<ExtendedData> attribute = (Attribute<ExtendedData>) attributeStack.removeFirst();

            int type = attribute.getType().head();
            int extendedType = attribute.getData().getExtendedType();
            byte[] extData = attribute.getData().getExtData();

            D data = dataCodec.decode(codecContext, extData);

            if (data == null) {
                attributeStack.addFirst(attribute);

                // Inform the caller that this attribute shouldn't be processed anymore
                return 1;
            }

            ExtendedAttribute<D> extendedAttribute = factory.build(type, extendedType, data);

            attributeStack.addFirst(extendedAttribute);

            return 0;
        }

        @Override
        public void encode(CodecContext codecContext, Deque<Attribute<?>> attributeStack) {
            @SuppressWarnings("unchecked")
            ExtendedAttribute<D> extendedAttribute = (ExtendedAttribute<D>) attributeStack.removeFirst();

            int extendedType = extendedAttribute.extendedType;
            byte[] extData = dataCodec.encode(codecContext, extendedAttribute.getData());

            ExtendedData extendedData = new ExtendedData(extendedType, extData);

            StandardAttribute<ExtendedData> attribute = new StandardAttribute<>(extendedAttribute.getType().head(),
                    extendedData);

            attributeStack.addFirst(attribute);
        }

    }

    /**
     * Attribute factory that returns a concrete instance of {@link ExtendedAttribute} or a subtype when given a
     * standard attribute type (integer in range [0, 255]), extended type (integer in range [0, 255]), and attribute
     * data ({@link D}).
     *
     * @param <D> the attribute data type used in the codec's target attribute
     */
    @FunctionalInterface
    public interface Factory<D extends Data> {

        /**
         * Builds a concrete instance of {@link ExtendedAttribute}.
         *
         * @param type the attribute type (integer in range [0, 255])
         * @param extendedType the attribute extended type (integer in range [0, 255])
         * @param data the attribute's data
         *
         * @return an instance of {@link ExtendedAttribute} or its subtypes with the provided type, extended type, and
         * data
         */
        ExtendedAttribute<D> build(int type, int extendedType, D data);

    }

}
