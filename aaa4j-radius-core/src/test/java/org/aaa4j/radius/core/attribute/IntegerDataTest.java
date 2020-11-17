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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("IntegerData")
class IntegerDataTest {

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        IntegerData integerData = new IntegerData(42);

        assertEquals(4, integerData.length());
        assertEquals(42, integerData.getValue());
    }

    @Test
    @DisplayName("integer data is decoded successfully")
    void testDecode() {
        {
            byte[] encoded = fromHex("0280de81");

            IntegerData integerData = IntegerData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(integerData);
            assertEquals(42_000_001, integerData.getValue());
        }
        {
            byte[] encoded = fromHex("ffffffd6");

            IntegerData integerData = IntegerData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(integerData);
            assertEquals(-42L, integerData.getValue());
        }
    }

    @Test
    @DisplayName("integer data is encoded successfully")
    void testEncode() {
        {
            byte[] encoded = IntegerData.Codec.INSTANCE.encode(null, new IntegerData(42_000_001));

            assertEquals("0280de81", toHex(encoded));
        }
        {
            byte[] encoded = IntegerData.Codec.INSTANCE.encode(null, new IntegerData(-42));

            assertEquals("ffffffd6", toHex(encoded));
        }
    }

    @Test
    @DisplayName("Invalid integer data is decoded into null")
    void testDecodeInvalidLength() {
        {
            // Too few bytes
            byte[] encoded = fromHex("");
            IntegerData integerData = IntegerData.Codec.INSTANCE.decode(null, encoded);

            assertNull(integerData);
        }
        {
            // Too few bytes
            byte[] encoded = fromHex("00ac");
            IntegerData integerData = IntegerData.Codec.INSTANCE.decode(null, encoded);

            assertNull(integerData);
        }
        {
            // Too many bytes
            byte[] encoded = fromHex("ff0280de81");
            IntegerData integerData = IntegerData.Codec.INSTANCE.decode(null, encoded);

            assertNull(integerData);
        }
    }

}