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

@DisplayName("EnumData")
class EnumDataTest {

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        EnumData enumData = new EnumData(42);

        assertEquals(4, enumData.length());
        assertEquals(42, enumData.getValue());
    }

    @Test
    @DisplayName("enum data is decoded successfully")
    void testDecode() {
        {
            byte[] encoded = fromHex("0280de81");

            EnumData enumData = EnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(enumData);
            assertEquals(42_000_001, enumData.getValue());
        }
        {
            byte[] encoded = fromHex("ffffffd6");

            EnumData enumData = EnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(enumData);
            assertEquals(-42L, enumData.getValue());
        }
    }

    @Test
    @DisplayName("enum data is encoded successfully")
    void testEncode() {
        {
            byte[] encoded = EnumData.Codec.INSTANCE.encode(null, null, new EnumData(42_000_001));

            assertEquals("0280de81", toHex(encoded));
        }
        {
            byte[] encoded = EnumData.Codec.INSTANCE.encode(null, null, new EnumData(-42));

            assertEquals("ffffffd6", toHex(encoded));
        }
    }

    @Test
    @DisplayName("Invalid enum data is decoded into null")
    void testDecodeInvalidLength() {
        {
            // Too few bytes
            byte[] encoded = fromHex("");
            EnumData enumData = EnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(enumData);
        }
        {
            // Too few bytes
            byte[] encoded = fromHex("00ac");
            EnumData enumData = EnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(enumData);
        }
        {
            // Too many bytes
            byte[] encoded = fromHex("ff0280de81");
            EnumData enumData = EnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(enumData);
        }
    }

}