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
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("VsaData")
class VsaDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new VsaData(32473, 58, null);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        VsaData vsaData = new VsaData(32473, 58, fromHex("9f4fde4bb9"));

        assertEquals(11, vsaData.length());
        assertEquals(32473, vsaData.getVendorId());
        assertEquals(58, vsaData.getVendorType());
        assertEquals("9f4fde4bb9", toHex(vsaData.getVsaData()));
    }

    @Test
    @DisplayName("vsa data is decoded successfully")
    void testDecode() {
        byte[] encoded = fromHex("00007ed93a079f4fde4bb9");

        VsaData vsaData = VsaData.Codec.INSTANCE.decode(null, null, encoded);

        assertNotNull(vsaData);
        assertEquals(32473, vsaData.getVendorId());
        assertEquals(58, vsaData.getVendorType());
        assertEquals("9f4fde4bb9", toHex(vsaData.getVsaData()));
    }

    @Test
    @DisplayName("vsa data is encoded successfully")
    void testEncode() {
        VsaData vsaData = new VsaData(32473, 58, fromHex("9f4fde4bb9"));
        byte[] encoded = VsaData.Codec.INSTANCE.encode(null, null, vsaData);

        assertEquals("00007ed93a079f4fde4bb9", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid vsa data is decoded into null")
    void testDecodeInvalid() {
        {
            // Too few bytes
            byte[] encoded = fromHex("3a9f4f");
            VsaData vsaData = VsaData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(vsaData);
        }
    }

}