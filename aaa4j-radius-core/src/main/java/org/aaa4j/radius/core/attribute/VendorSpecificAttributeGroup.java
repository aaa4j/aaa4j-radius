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

import org.aaa4j.radius.core.dictionary.AttributeDefinition;
import org.aaa4j.radius.core.dictionary.AttributeFactory;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.VendorSpecificGroupFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A group of vendor-specific attributes transported in a single Vendor-Specific attribute (the compact form permitted
 * by RFC 2865). The contained attributes are the source of truth: the group's vsa data is derived from them at
 * construction, and the group encodes to a single attribute carrying the sub-attributes in order.
 */
public class VendorSpecificAttributeGroup extends StandardAttribute<VsaData> implements AttributeGroup {

    private final List<Attribute<?>> attributes;

    /**
     * Constructs a vendor specific group from an attribute type, vendor id, and vendor-specific attributes.
     *
     * @param type the attribute type (integer in range [0, 255])
     * @param vendorId the vendor identifier (integer in range [0, 16777215])
     * @param attributes the vendor-specific attributes (at least one, each with the given vendor identifier)
     */
    public VendorSpecificAttributeGroup(int type, int vendorId, List<VendorSpecificAttribute<?>> attributes) {
        super(type, toVsaData(vendorId, attributes));

        this.attributes = Collections.unmodifiableList(new ArrayList<>(attributes));
    }

    /**
     * Builds a vendor specific group by building each sub-attribute of the given vsa data into a typed vendor-specific
     * attribute (or a raw one when it could not be decoded), using the dictionary to find the typed attribute for each
     * sub-attribute. The group itself is built with the vendor's group factory when the dictionary has one, so that a
     * vendor's own group type is recovered.
     *
     * @param dictionary the dictionary used to build the sub-attributes and the group
     * @param type the attribute type (integer in range [0, 255])
     * @param vsaData the vsa data holding the sub-attributes
     *
     * @return a vendor specific group holding the built sub-attributes
     */
    public static VendorSpecificAttributeGroup fromVsaData(Dictionary dictionary, int type, VsaData vsaData) {
        List<VendorSpecificAttribute<?>> groupAttributes = new ArrayList<>();

        for (VsaData.Vsa vsa : vsaData.getVsas()) {
            groupAttributes.add(buildSubAttribute(dictionary, type, vsaData.getVendorId(), vsa));
        }

        VendorSpecificGroupFactory groupFactory = dictionary
                .getVendorSpecificGroupFactory(new AttributeType(type, vsaData.getVendorId()));

        if (groupFactory != null) {
            VendorSpecificAttributeGroup group = groupFactory.build(type, vsaData.getVendorId(), groupAttributes);

            if (group != null) {
                return group;
            }
        }

        return new VendorSpecificAttributeGroup(type, vsaData.getVendorId(), groupAttributes);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static VendorSpecificAttribute<?> buildSubAttribute(Dictionary dictionary, int type, int vendorId,
            VsaData.Vsa vsa) {
        Data data = vsa.getData();

        if (data instanceof RawData) {
            return new RawVendorSpecificAttribute(type, vendorId, vsa.getVendorType(), (RawData) data);
        }

        AttributeDefinition<?, ?> attributeDefinition = dictionary
                .getAttributeDefinition(new AttributeType(type, vendorId, vsa.getVendorType()));

        if (attributeDefinition != null && attributeDefinition.getDataClass().isInstance(data)) {
            Attribute<?> attribute = ((AttributeFactory) attributeDefinition.getAttributeFactory()).build(data);

            if (attribute instanceof VendorSpecificAttribute) {
                return (VendorSpecificAttribute<?>) attribute;
            }
        }

        // There's no typed attribute for the sub-attribute; a generic attribute still encodes through the dictionary
        return new VendorSpecificAttribute<>(type, vendorId, vsa.getVendorType(), data);
    }

    @Override
    public List<Attribute<?>> getGroupedAttributes() {
        return attributes;
    }

    /**
     * Gets the vendor identifier.
     *
     * @return the vendor identifier
     */
    public int getVendorId() {
        return getData().getVendorId();
    }

    private static VsaData toVsaData(int vendorId, List<VendorSpecificAttribute<?>> attributes) {
        Objects.requireNonNull(attributes);

        List<VsaData.Vsa> vsas = new ArrayList<>();

        for (VendorSpecificAttribute<?> attribute : attributes) {
            Objects.requireNonNull(attribute);

            if (attribute.getVendorId() != vendorId) {
                throw new IllegalArgumentException(
                        "All attributes in the group must have the vendor identifier " + vendorId);
            }

            vsas.add(new VsaData.Vsa(attribute.getVendorType(), attribute.getData()));
        }

        return new VsaData(vendorId, vsas);
    }

}
