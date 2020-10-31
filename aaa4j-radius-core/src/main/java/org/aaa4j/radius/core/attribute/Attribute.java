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

import java.util.Objects;

/**
 * An attribute. An attribute can be a top-level attribute ({@link StandardAttribute} or an encapsulated attribute. Each
 * attribute contains a numeric type identifier (see {@link AttributeType}) and attribute data (see {@link Data}).
 *
 * @param <D> the type of attribute data
 */
public abstract class Attribute<D extends Data> {

    private final AttributeType type;

    private final D data;

    /**
     * Constructs an attribute from an identifier and data.
     *
     * @param type the attribute identifier
     * @param data the attribute data
     */
    public Attribute(AttributeType type, D data) {
        this.type = Objects.requireNonNull(type);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets the attribute identifier.
     *
     * @return the attribute identifier
     */
    public final AttributeType getType() {
        return type;
    }

    /**
     * Gets the attribute data.
     *
     * @return the attribute data
     */
    public final D getData() {
        return data;
    }

}
