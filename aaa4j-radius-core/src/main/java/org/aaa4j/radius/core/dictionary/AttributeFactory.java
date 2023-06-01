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
import org.aaa4j.radius.core.attribute.Data;

/**
 * Factory for building attributes from values.
 *
 * @param <D> the data type
 */
public interface AttributeFactory<D extends Data> {

    /**
     * Builds an attribute using the given data.
     *
     * @param data the attribute data
     *
     * @return a new attribute
     */
    Attribute<D> build(D data);

}
