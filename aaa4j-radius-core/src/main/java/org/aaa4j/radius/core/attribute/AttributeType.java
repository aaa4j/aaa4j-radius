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

import java.util.Arrays;
import java.util.Objects;

/**
 * Attribute type in the "dotted number" notation. The attribute type is the unique numeric attribute identifier. An
 * attribute type with a single type component indicates that the attribute is a top-level attribute. An attribute type
 * with more than one type components indicates that the attribute is encapsulated within other attributes.
 */
public final class AttributeType {

    private final int[] types;

    /**
     * Constructs an attribute type from the given type components.
     *
     * @param types the type components
     */
    public AttributeType(int... types) {
        Objects.requireNonNull(types);
        this.types = Arrays.copyOf(types, types.length);

        if (this.types.length == 0) {
            throw new IllegalArgumentException("At least one type is required");
        }

        if (this.types[0] < 0 || this.types[0] > 255) {
            throw new IllegalArgumentException("First type in attribute type must be in range [0, 255]");
        }
    }

    /**
     * Constructs a new attribute type by appending a type component to an existing attribute type.
     *
     * @param init the initial attribute type
     * @param last the type component to append to the initial attribute type
     */
    public AttributeType(AttributeType init, int last) {
        Objects.requireNonNull(init);

        types = new int[init.types.length + 1];
        System.arraycopy(init.types, 0, types, 0, init.types.length);
        types[types.length - 1] = last;
    }

    /**
     * Returns the first component of the attribute type.
     * 
     * @return the first component of the attribute type
     */
    public int head() {
        return types[0];
    }

    /**
     * Returns the last component of the attribute type.
     * 
     * @return the last component of the attribute type
     */
    public int last() {
        return types[types.length - 1];
    }

    /**
     * Returns the number of components in this attribute type.
     * 
     * @return the number of components in this attribute type
     */
    public int length() {
        return types.length;
    }

    /**
     * Returns a new attribute type by appending the provided type components to this attribute type.
     *
     * @param types the attribute types to append
     * @return a new attribute type
     */
    public AttributeType with(int... types) {
        Objects.requireNonNull(types);

        int[] combinedTypes = new int[this.types.length + types.length];
        System.arraycopy(this.types, 0, combinedTypes, 0, this.types.length);
        System.arraycopy(types, 0, combinedTypes, this.types.length, types.length);

        return new AttributeType(combinedTypes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(types);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        AttributeType that = (AttributeType) obj;

        return Arrays.equals(types, that.types);
    }

}
