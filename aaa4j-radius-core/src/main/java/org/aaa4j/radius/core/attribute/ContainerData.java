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

import org.aaa4j.radius.core.dictionary.Dictionary;

/**
 * Attribute data encapsulating other attributes.
 */
public abstract class ContainerData extends Data {

    /**
     * Gets the contained type.
     *
     * @return the contained type, or null if the data doesn't contain exactly one attribute
     */
    public abstract int[] getContainedType();

    /**
     * Builds this data into its final attribute form when it doesn't map to a single contained attribute (for example,
     * several sub-attributes carried together). Data that maps to a single contained attribute returns null and is
     * built by looking up the definition of {@link #getContainedType()} instead.
     *
     * @param dictionary the dictionary used to build the contained attributes
     * @param type the enclosing attribute type (integer in range [0, 255])
     *
     * @return the built attribute, or null if this data maps to a single contained attribute
     */
    public Attribute<?> build(Dictionary dictionary, int type) {
        return null;
    }

}
