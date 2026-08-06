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
import java.util.List;
import java.util.Optional;

/**
 * An attribute containing other attributes. Attribute lookups by class descend into containers for reading, but a
 * contained attribute can only be replaced by rebuilding its container: attributes are immutable, and the packet's
 * attribute list holds only the container itself.
 */
public interface AttributeGroup {

    /**
     * Gets the attributes contained within this attribute.
     *
     * @return the contained attributes, in the order they appear in the attribute
     */
    List<Attribute<?>> getGroupedAttributes();

    /**
     * Gets the first contained attribute of a specific attribute class, searching nested containers in order.
     *
     * @param aClass attribute type class
     * @param <A> attribute type for {@code aClass} parameter
     *
     * @return optional with the first contained attribute of the specified class (or empty optional)
     */
    default <A extends Attribute<?>> Optional<A> getAttribute(Class<A> aClass) {
        for (Attribute<?> attribute : getGroupedAttributes()) {
            if (aClass.isInstance(attribute)) {
                return Optional.of(aClass.cast(attribute));
            }

            if (attribute instanceof AttributeGroup) {
                Optional<A> contained = ((AttributeGroup) attribute).getAttribute(aClass);

                if (contained.isPresent()) {
                    return contained;
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Gets all the contained attributes of a specific attribute class, searching nested containers in order.
     *
     * @param aClass attribute type class
     * @param <A> attribute type for {@code aClass} parameter
     *
     * @return list of contained attributes of the given class
     */
    default <A extends Attribute<?>> List<A> getAttributes(Class<A> aClass) {
        List<A> found = new ArrayList<>();

        for (Attribute<?> attribute : getGroupedAttributes()) {
            if (aClass.isInstance(attribute)) {
                found.add(aClass.cast(attribute));
            }

            if (attribute instanceof AttributeGroup) {
                found.addAll(((AttributeGroup) attribute).getAttributes(aClass));
            }
        }

        return found;
    }

}
