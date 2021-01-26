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

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OptionalTaggedStringData")
class OptionalTaggedStringDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new OptionalTaggedStringData(null, 2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new OptionalTaggedStringData(new byte[3], 32);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new OptionalTaggedStringData(new byte[3], -1);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        OptionalTaggedStringData optionalTaggedStringData = new OptionalTaggedStringData(fromHex("8af9bc"), 31);

        assertEquals(4, optionalTaggedStringData.length());
        assertEquals("8af9bc", toHex(optionalTaggedStringData.getValue()));
        assertTrue(optionalTaggedStringData.getTag().isPresent());
        assertEquals(31, optionalTaggedStringData.getTag().get());
    }

    @Test
    @DisplayName("Optional tagged string data is decoded successfully")
    void testDecode() {
        {
            // With tag
            byte[] encoded = fromHex("028af9bc");

            OptionalTaggedStringData optionalTaggedStringData = OptionalTaggedStringData.Codec.INSTANCE.decode(null,
                    null, encoded);

            assertNotNull(optionalTaggedStringData);
            assertEquals("8af9bc", toHex(optionalTaggedStringData.getValue()));
            assertTrue(optionalTaggedStringData.getTag().isPresent());
            assertEquals(2, optionalTaggedStringData.getTag().get());
        }
        {
            // Without tag
            byte[] encoded = fromHex("8af9bc");

            OptionalTaggedStringData optionalTaggedStringData = OptionalTaggedStringData.Codec.INSTANCE.decode(null,
                    null, encoded);

            assertNotNull(optionalTaggedStringData);
            assertEquals("8af9bc", toHex(optionalTaggedStringData.getValue()));
            assertFalse(optionalTaggedStringData.getTag().isPresent());
        }
    }

    @Test
    @DisplayName("Optional tagged string data is encoded successfully")
    void testEncode() {
        {
            // With tag
            OptionalTaggedStringData optionalTaggedStringData = new OptionalTaggedStringData(fromHex("8af9bc"), 2);
            byte[] encoded = OptionalTaggedStringData.Codec.INSTANCE.encode(null, null, optionalTaggedStringData);

            assertEquals("028af9bc", toHex(encoded));
        }
        {
            // Without tag
            OptionalTaggedStringData optionalTaggedStringData = new OptionalTaggedStringData(fromHex("8af9bc"));
            byte[] encoded = OptionalTaggedStringData.Codec.INSTANCE.encode(null, null, optionalTaggedStringData);

            assertEquals("8af9bc", toHex(encoded));
        }
    }

}