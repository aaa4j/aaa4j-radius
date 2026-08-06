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

import org.aaa4j.radius.core.attribute.VendorSpecificAttribute;
import org.aaa4j.radius.core.attribute.VendorSpecificAttributeGroup;

import java.util.List;

/**
 * Factory that returns a concrete instance of {@link VendorSpecificAttributeGroup} or a subtype when given a standard
 * attribute type (integer in range [0, 255]), vendor id (integer in range [0, 16777215]), and the group's vendor
 * specific attributes. A dictionary returns a factory for a vendor so that a compact Vendor-Specific attribute decodes
 * into the vendor's own group type.
 */
@FunctionalInterface
public interface VendorSpecificGroupFactory {

    /**
     * Builds a concrete instance of {@link VendorSpecificAttributeGroup}.
     *
     * @param type the attribute type (integer in range [0, 255])
     * @param vendorId the vendor identifier (integer in range [0, 16777215])
     * @param attributes the group's vendor specific attributes (at least two, each with the given vendor identifier)
     *
     * @return an instance of {@link VendorSpecificAttributeGroup} or its subtypes holding the given attributes
     */
    VendorSpecificAttributeGroup build(int type, int vendorId, List<VendorSpecificAttribute<?>> attributes);

}
