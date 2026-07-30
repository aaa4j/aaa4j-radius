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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("VendorSpecificAttribute")
class VendorSpecificAttributeTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new VendorSpecificAttribute<>(26, 32473, 58, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new VendorSpecificAttribute<>(26, -1, 58, new IntegerData(42));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Vendor identifiers are three octets: the high octet of the field is always zero
            new VendorSpecificAttribute<>(26, 16777216, 58, new IntegerData(42));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new VendorSpecificAttribute<>(26, 32473, -1, new IntegerData(42));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new VendorSpecificAttribute<>(26, 32473, 256, new IntegerData(42));
        });
    }

    @Test
    @DisplayName("Constructor validates the data length")
    void testConstructorDataLength() {
        // A vendor specific attribute spends eight bytes on the attribute type and length, the vendor identifier,
        // the vendor type and the vendor length, leaving 247 for the data
        VendorSpecificAttribute<StringData> vendorSpecificAttribute = new VendorSpecificAttribute<>(26, 32473, 58,
                new StringData(new byte[247]));

        assertEquals(247, vendorSpecificAttribute.getData().length());

        assertThrows(IllegalArgumentException.class, () -> {
            // One byte more needs a 256 byte attribute, which the one octet length field can't express
            new VendorSpecificAttribute<>(26, 32473, 58, new StringData(new byte[248]));
        });
    }

}
