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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("VendorSpecificAttributeGroup")
class VendorSpecificAttributeGroupTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new VendorSpecificAttributeGroup(26, 32473, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // At least one attribute is required
            new VendorSpecificAttributeGroup(26, 32473, List.of());
        });

        assertThrows(NullPointerException.class, () -> {
            new VendorSpecificAttributeGroup(26, 32473, Arrays.asList((VendorSpecificAttribute<?>) null));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // The attribute's vendor identifier doesn't match the group's
            new VendorSpecificAttributeGroup(26, 9, List.<VendorSpecificAttribute<?>>of(
                    new VendorSpecificAttribute<>(26, 32473, 1, new IntegerData(5))));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        VendorSpecificAttribute<IntegerData> first = new VendorSpecificAttribute<>(26, 32473, 1, new IntegerData(5));
        VendorSpecificAttribute<StringData> second = new VendorSpecificAttribute<>(26, 32473, 2,
                new StringData(fromHex("ab")));

        VendorSpecificAttributeGroup group = new VendorSpecificAttributeGroup(26, 32473,
                List.<VendorSpecificAttribute<?>>of(first, second));

        assertEquals(new AttributeType(26), group.getType());
        assertEquals(32473, group.getVendorId());

        assertEquals(2, group.getGroupedAttributes().size());
        assertSame(first, group.getGroupedAttributes().get(0));
        assertSame(second, group.getGroupedAttributes().get(1));

        // The vsa data is derived from the contained attributes: same vendor types, same data instances
        VsaData vsaData = group.getData();

        assertEquals(32473, vsaData.getVendorId());
        assertEquals(13, vsaData.length());
        assertEquals(2, vsaData.getVsas().size());
        assertEquals(1, vsaData.getVsas().get(0).getVendorType());
        assertSame(first.getData(), vsaData.getVsas().get(0).getData());
        assertEquals(2, vsaData.getVsas().get(1).getVendorType());
        assertSame(second.getData(), vsaData.getVsas().get(1).getData());
    }

    @Test
    @DisplayName("Contained attributes are found by class")
    void testGetAttribute() {
        VendorSpecificAttribute<IntegerData> typed = new VendorSpecificAttribute<>(26, 32473, 1, new IntegerData(5));
        RawVendorSpecificAttribute raw = new RawVendorSpecificAttribute(26, 32473, 99,
                new RawData(fromHex("aabbcc")));

        VendorSpecificAttributeGroup group = new VendorSpecificAttributeGroup(26, 32473,
                List.<VendorSpecificAttribute<?>>of(typed, raw));

        assertTrue(group.getAttribute(RawVendorSpecificAttribute.class).isPresent());
        assertSame(raw, group.getAttribute(RawVendorSpecificAttribute.class).get());

        assertEquals(2, group.getAttributes(VendorSpecificAttribute.class).size());
        assertSame(typed, group.getAttributes(VendorSpecificAttribute.class).get(0));

        assertFalse(group.getAttribute(VendorSpecificAttributeGroup.class).isPresent());
    }

}
