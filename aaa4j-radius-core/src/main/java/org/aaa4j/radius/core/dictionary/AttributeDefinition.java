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

package org.aaa4j.radius.core.dictionary;

import org.aaa4j.radius.core.attribute.Attribute;
import org.aaa4j.radius.core.attribute.AttributeCodec;
import org.aaa4j.radius.core.attribute.AttributeType;
import org.aaa4j.radius.core.attribute.Data;

import java.util.Objects;

/**
 * An attribute (see {@link Attribute}) dictionary (see {@link Dictionary}) definition.
 */
public final class AttributeDefinition {

    private final AttributeType type;

    private final String name;

    private final Class<? extends Attribute<?>> attributeClass;

    private final Class<? extends Data> dataClass;

    private final AttributeCodec attributeCodec;

    /**
     * Constructs an attribute definition from the given definition parameters.
     *
     * @param type the attribute type (i.e., the unique "dotted" attribute number)
     * @param name the attribute name
     * @param attributeClass the attribute class
     * @param dataClass the attribute data class
     * @param attributeCodec the attribute codec used to encode and decode the attribute
     * @param <A> the concrete attribute type parameter
     * @param <D> the concrete data type parameter
     */
    public <A extends Attribute<D>, D extends Data> AttributeDefinition(AttributeType type, String name,
            Class<A> attributeClass, Class<D> dataClass, AttributeCodec attributeCodec) {
        this.type = Objects.requireNonNull(type);
        this.name = Objects.requireNonNull(name);
        this.attributeClass = Objects.requireNonNull(attributeClass);
        this.dataClass = Objects.requireNonNull(dataClass);
        this.attributeCodec = Objects.requireNonNull(attributeCodec);
    }

    /**
     * Gets the attribute identifier.
     *
     * @return the attribute identifier
     */
    public AttributeType getIdentifier() {
        return type;
    }

    /**
     * Gets the attribute name.
     *
     * @return the attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the attribute class.
     *
     * @return the attribute class
     */
    public Class<? extends Attribute<?>> getAttributeClass() {
        return attributeClass;
    }

    /**
     * Gets the attribute data class.
     *
     * @return the attribute data class
     */
    public Class<? extends Data> getDataClass() {
        return dataClass;
    }

    /**
     * Gets the attribute codec used to encode and decode the attribute.
     *
     * @return the attribute codec
     */
    public AttributeCodec getAttributeCodec() {
        return attributeCodec;
    }

}
