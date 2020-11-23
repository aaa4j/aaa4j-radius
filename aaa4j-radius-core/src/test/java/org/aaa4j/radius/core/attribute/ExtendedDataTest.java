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

@DisplayName("ExtendedData")
class ExtendedDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new ExtendedData(241, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ExtendedData(-1, new byte[] { 0x00, 0x01 });
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ExtendedData(256, new byte[] { 0x00, 0x01 });
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        ExtendedData extendedData = new ExtendedData(3, fromHex("0280de81"));

        assertEquals(5, extendedData.length());
        assertEquals(3, extendedData.getExtendedType());
        assertArrayEquals(new int[] { 3 }, extendedData.getContainedType());
        assertEquals("0280de81", toHex(extendedData.getExtData()));
    }

    @Test
    @DisplayName("extended data is decoded successfully")
    void testDecode() {
        byte[] encoded = fromHex("030280de81");

        ExtendedData extendedData = ExtendedData.Codec.INSTANCE.decode(null, encoded);

        assertNotNull(extendedData);
        assertEquals(5, extendedData.length());
        assertEquals(3, extendedData.getExtendedType());
        assertArrayEquals(new int[] { 3 }, extendedData.getContainedType());
        assertEquals("0280de81", toHex(extendedData.getExtData()));
    }

    @Test
    @DisplayName("extended data is encoded successfully")
    void testEncode() {
        ExtendedData extendedData = new ExtendedData(3, fromHex("0280de81"));
        byte[] encoded = ExtendedData.Codec.INSTANCE.encode(null, extendedData);

        assertEquals("030280de81", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid extended data is decoded into null")
    void testDecodeInvalid() {
        // Too few bytes
        byte[] encoded = fromHex("");
        ExtendedData extendedData = ExtendedData.Codec.INSTANCE.decode(null, encoded);

        assertNull(extendedData);
    }

}