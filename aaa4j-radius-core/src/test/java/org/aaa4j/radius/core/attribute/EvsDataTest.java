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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("EvsData")
class EvsDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EvsData(32473, -1, fromHex("abcd"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new EvsData(32473, 256, fromHex("abcd"));
        });

        assertThrows(NullPointerException.class, () -> {
            new EvsData(32473, 10, null);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        EvsData evsData = new EvsData(32473, 10, fromHex("e1273a01729a"));

        assertEquals(11, evsData.length());
        assertArrayEquals(new int[] { 32473, 10 }, evsData.getContainedType());
        assertEquals(32473, evsData.getVendorId());
        assertEquals(10, evsData.getVendorType());
        assertEquals("e1273a01729a", toHex(evsData.getEvsData()));
    }

    @Test
    @DisplayName("evs data is decoded successfully")
    void testDecode() {
        byte[] encoded = fromHex("00007ed90ae1273a01729a");

        EvsData evsData = EvsData.Codec.INSTANCE.decode(null, null, encoded);

        assertNotNull(evsData);
        assertEquals(32473, evsData.getVendorId());
        assertEquals(10, evsData.getVendorType());
        assertEquals("e1273a01729a", toHex(evsData.getEvsData()));
    }

    @Test
    @DisplayName("evs data is encoded successfully")
    void testEncode() {
        EvsData evsData = new EvsData(32473, 10, fromHex("e1273a01729a"));
        byte[] encoded = EvsData.Codec.INSTANCE.encode(null, null, evsData);

        assertEquals("00007ed90ae1273a01729a", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid evs data is decoded into null")
    void testDecodeInvalid() {
        {
            // Too few bytes
            byte[] encoded = fromHex("00007ed9");
            EvsData evsData = EvsData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(evsData);
        }
    }

}