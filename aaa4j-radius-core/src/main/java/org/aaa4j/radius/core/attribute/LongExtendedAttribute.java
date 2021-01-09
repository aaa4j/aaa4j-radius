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
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * A long extended attribute in the long extended attribute space. A long extended attribute is encapsulated in one or
 * more {@link StandardAttribute} attributes containing {@link LongExtendedData} data.
 *
 * @param <D> data type
 */
public class LongExtendedAttribute<D extends Data> extends Attribute<D> {

    private final int extendedType;

    /**
     * Constructs a long extended attribute from an attribute type, extended type, and data.
     *
     * @param type the attribute type (int in range [0, 255])
     * @param extendedType the attribute extended type (int in range [0, 255])
     * @param data the attribute data
     */
    public LongExtendedAttribute(int type, int extendedType, D data) {
        super(new AttributeType(type, extendedType), data);

        if (extendedType < 0 || extendedType > 255) {
            throw new IllegalArgumentException("Extended type must be in range [0, 255]");
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
     * Codec for a {@link LongExtendedAttribute} of a particular data type ({@link D}). An instance of {@link Codec} is
     * capable of decoding attributes with {@link LongExtendedData} data into extended attributes with a particular data
     * type.
     *
     * @param <D> the attribute data type used in the codec's target attribute
     */
    public static class Codec<D extends Data> implements AttributeCodec {

        private final Factory<D> factory;

        private final DataCodec<D> dataCodec;

        /**
         * Constructs a codec for a {@link LongExtendedAttribute} using the given data codec and attribute instance
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
        @SuppressWarnings("unchecked")
        public int decode(CodecContext codecContext, Deque<Attribute<?>> attributeStack) {
            Attribute<LongExtendedData> topAttribute = (Attribute<LongExtendedData>) attributeStack.getFirst();
            AttributeType type = topAttribute.getType();
            int extendedType = topAttribute.getData().getExtendedType();

            List<Attribute<LongExtendedData>> fragments = new ArrayList<>();
            boolean expectingMore = false;

            while (attributeStack.size() > 0 && attributeStack.peekFirst().getType().equals(type)) {
                Attribute<LongExtendedData> nextAttribute = (Attribute<LongExtendedData>) attributeStack.getFirst();

                if (nextAttribute.getData().getExtendedType() != extendedType) {
                    // The contained type is not the same so we are done getting the fragments for the top attribute
                    break;
                }

                fragments.add((Attribute<LongExtendedData>) attributeStack.removeFirst());

                if (!nextAttribute.getData().hasMore()) {
                    expectingMore = false;

                    break;
                }

                expectingMore = true;
            }

            if (expectingMore) {
                // We were expecting more fragments but didn't get them, so discard all the previous fragments
                ListIterator<Attribute<LongExtendedData>> iterator = fragments.listIterator(fragments.size());

                while (iterator.hasPrevious()) {
                    attributeStack.addFirst(iterator.previous());
                }

                return fragments.size();
            }

            List<byte[]> extDataFragments = new ArrayList<>();
            int extDataLength = 0;

            for (Attribute<LongExtendedData> fragment : fragments) {
                byte[] extDataFragment = fragment.getData().getExtData();

                extDataFragments.add(extDataFragment);
                extDataLength += extDataFragment.length;
            }

            byte[] extData = new byte[extDataLength];

            for (int i = 0, position = 0; i < extDataFragments.size(); i++) {
                byte[] extDataFragment = extDataFragments.get(i);
                System.arraycopy(extDataFragment, 0, extData, position, extDataFragment.length);
                position += extDataFragment.length;
            }

            D data = dataCodec.decode(codecContext, extData);

            if (data == null) {
                // We couldn't decode the long-extended fragments so place the fragment attributes back on the stack
                ListIterator<Attribute<LongExtendedData>> iterator = fragments.listIterator(fragments.size());

                while (iterator.hasPrevious()) {
                    attributeStack.addFirst(iterator.previous());
                }

                return fragments.size();
            }

            LongExtendedAttribute<D> longExtendedAttribute = factory.build(type.head(), extendedType, data);

            attributeStack.addFirst(longExtendedAttribute);

            return 0;
        }

        @Override
        public void encode(CodecContext codecContext, Deque<Attribute<?>> attributeStack) {
            @SuppressWarnings("unchecked")
            LongExtendedAttribute<D> longExtendedAttribute = (LongExtendedAttribute<D>) attributeStack.removeFirst();

            byte[] extData = dataCodec.encode(codecContext, longExtendedAttribute.getData());

            List<Attribute<LongExtendedData>> fragments = new ArrayList<>();

            for (int i = 0; i < extData.length; i += 251) {
                int extendedType = longExtendedAttribute.extendedType;
                boolean more = extData.length > i + 251;
                byte[] extDataFragment = Arrays.copyOfRange(extData, i, Math.min(i + 251, extData.length));

                LongExtendedData longExtendedData = new LongExtendedData(extendedType, extDataFragment, more, false);

                StandardAttribute<LongExtendedData> fragment = new StandardAttribute<>(
                        longExtendedAttribute.getType().head(), longExtendedData);

                fragments.add(fragment);
            }

            ListIterator<Attribute<LongExtendedData>> iterator = fragments.listIterator(fragments.size());

            while (iterator.hasPrevious()) {
                attributeStack.addFirst(iterator.previous());
            }
        }

    }

    /**
     * Attribute factory that returns a concrete instance of {@link LongExtendedAttribute} or a subtype when given a
     * standard attribute type (integer in range [0, 255]), extended type (integer in range [0, 255]), and attribute
     * data ({@link D}).
     *
     * @param <D> the attribute data type used in the codec's target attribute
     */
    @FunctionalInterface
    public interface Factory<D extends Data> {

        /**
         * Builds a concrete instance of {@link LongExtendedAttribute}.
         *
         * @param type the attribute type (integer in range [0, 255])
         * @param extendedType the attribute extended type (integer in range [0, 255])
         * @param data the attribute's data
         *
         * @return an instance of {@link LongExtendedAttribute} or its subtypes with the provided type, extended type,
         * and data
         */
        LongExtendedAttribute<D> build(int type, int extendedType, D data);

    }

}