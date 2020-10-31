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

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

/**
 * A "concat" attribute that's concatenated from multiple attributes transmit more than 253 octets in the standard
 * attribute space.
 */
public class ConcatAttribute extends Attribute<ConcatData> {

    /**
     * Constructs a concat attribute from an attribute type and concat data.
     *
     * @param type the attribute type (int in range [0, 255])
     * @param concatData the concat data
     */
    public ConcatAttribute(int type, ConcatData concatData) {
        super(new AttributeType(type), concatData);
    }

    /**
     * Codec for a {@link ConcatAttribute}. An instance of {@link ConcatAttribute.Codec} is capable of decoding raw
     * attributes into concat attributes.
     */
    public static class Codec implements AttributeCodec {

        private final Factory factory;

        /**
         * Constructs a codec for a {@link StandardAttribute} using the attribute instance factory.
         *
         * @param factory the attribute instance factory
         */
        public Codec(Factory factory) {
            this.factory = factory;
        }

        @Override
        public int decode(CodecContext codecContext, Deque<Attribute<?>> attributeStack) {
            RawAttribute rawAttribute = (RawAttribute) attributeStack.removeFirst();
            AttributeType type = rawAttribute.getType();

            List<byte[]> fragments = new ArrayList<>();
            fragments.add(rawAttribute.getData().getValue());

            while (attributeStack.size() > 0 && attributeStack.peekFirst().getType().equals(type)) {
                fragments.add(((RawAttribute) attributeStack.removeFirst()).getData().getValue());
            }

            ConcatAttribute concatAttribute = factory.build(type.head(), new ConcatData(fragments));
            attributeStack.addFirst(concatAttribute);

            return 1;
        }

        @Override
        public void encode(CodecContext codecContext, Deque<Attribute<?>> attributeStack) {
            ConcatAttribute concatAttribute = (ConcatAttribute) attributeStack.removeFirst();

            List<byte[]> fragments = concatAttribute.getData().getFragments();
            ListIterator<byte[]> iterator = fragments.listIterator(fragments.size());

            while (iterator.hasPrevious()) {
                RawAttribute rawAttribute = new RawAttribute(concatAttribute.getType().head(), iterator.previous());
                attributeStack.addFirst(rawAttribute);
            }
        }

        /**
         * Attribute factory that returns a concrete instance of {@link ConcatAttribute} or a subtype when given a
         * top-level attribute type (integer in range [0, 255]) and concat data ({@link ConcatData}).
         */
        @FunctionalInterface
        public interface Factory {

            /**
             * Builds a concrete instance of {@link ConcatAttribute}.
             *
             * @param type the attribute type (integer in range [0, 255])
             * @param concatData the concat data
             *
             * @return an instance of {@link ConcatAttribute} or its subtypes with the provided type and data
             */
            ConcatAttribute build(int type, ConcatData concatData);

        }

    }

}
