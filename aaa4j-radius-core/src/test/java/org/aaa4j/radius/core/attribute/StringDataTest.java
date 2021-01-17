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
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("StringData")
class StringDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new StringData(null);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        StringData stringData = new StringData(fromHex("8af9bc"));

        assertEquals(3, stringData.length());
        assertEquals("8af9bc", toHex(stringData.getValue()));
    }

    @Test
    @DisplayName("string data is decoded successfully")
    void testDecode() {
        byte[] encoded = fromHex("8af9bc");

        StringData stringData = StringData.Codec.INSTANCE.decode(null, null, encoded);

        assertNotNull(stringData);
        assertEquals("8af9bc", toHex(stringData.getValue()));
    }

    @Test
    @DisplayName("string data is encoded successfully")
    void testEncode() {
        StringData stringData = new StringData(fromHex("8af9bc"));
        byte[] encoded = StringData.Codec.INSTANCE.encode(null, null, stringData);

        assertEquals("8af9bc", toHex(encoded));
    }

}