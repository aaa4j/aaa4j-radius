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
 * A standard top-level attribute in the standard attribute space.
 *
 * @param <D> data type
 */
public class StandardAttribute<D extends Data> extends Attribute<D> {

    /**
     * Constructs a standard attribute from an attribute type and data.
     *
     * @param type the attribute type (int in range [0, 255])
     * @param data the attribute data
     */
    public StandardAttribute(int type, D data) {
        super(new AttributeType(type), data);

        if (data.length() > 253) {
            throw new IllegalArgumentException("Data length must be in range [0, 253]");
        }
    }

    /**
     * Codec for a {@link StandardAttribute} of a particular data type ({@link D}). An instance of {@link Codec} is
     * capable of decoding raw attributes into standard attributes with a particular data type.
     *
     * @param <D> the attribute data type used in the codec's target attribute
     */
    public static class Codec<D extends Data> implements AttributeCodec {

        private final Factory<D> factory;

        private final DataCodec<D> dataCodec;

        /**
         * Constructs a codec for a {@link StandardAttribute} using the given data codec and attribute instance factory.
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
            RawAttribute rawAttribute = (RawAttribute) attributeStack.removeFirst();

            D data = dataCodec.decode(codecContext, rawAttribute.getData().getValue());

            if (data == null) {
                attributeStack.addFirst(rawAttribute);

                // Inform the caller that this attribute shouldn't be processed anymore
                return 1;
            }

            StandardAttribute<D> standardAttribute = factory.build(rawAttribute.getType().head(), data);

            attributeStack.addFirst(standardAttribute);

            return 0;
        }

        @Override
        public void encode(CodecContext codecContext, Deque<Attribute<?>> attributeStack) {
            @SuppressWarnings("unchecked")
            StandardAttribute<D> standardAttribute = (StandardAttribute<D>) attributeStack.removeFirst();

            byte[] bytes = dataCodec.encode(codecContext, standardAttribute.getData());

            RawAttribute rawAttribute = new RawAttribute(standardAttribute.getType().head(), bytes);
            attributeStack.addFirst(rawAttribute);
        }

        /**
         * Attribute factory that returns a concrete instance of {@link StandardAttribute} or a subtype when given a
         * standard attribute type (integer in range [0, 255]) and attribute data ({@link D}).
         *
         * @param <D> the attribute data type used in the codec's target attribute
         */
        @FunctionalInterface
        public interface Factory<D extends Data> {

            /**
             * Builds a concrete instance of {@link StandardAttribute}.
             *
             * @param type the attribute type (integer in range [0, 255])
             * @param data the attribute's data
             * 
             * @return an instance of {@link StandardAttribute} or its subtypes with the provided type and data
             */
            StandardAttribute<D> build(int type, D data);

        }

    }

}
