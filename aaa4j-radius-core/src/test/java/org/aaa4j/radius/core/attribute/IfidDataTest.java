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

@DisplayName("IfidData")
class IfidDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new IfidData(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new IfidData(fromHex("02aaccfffec51fda00"));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        IfidData ifidData = new IfidData(fromHex("02aaccfffec51fda"));

        assertEquals(8, ifidData.length());
        assertEquals("02aaccfffec51fda", toHex(ifidData.getValue()));
    }

    @Test
    @DisplayName("ifid data is decoded successfully")
    void testDecode() {
        byte[] encoded = fromHex("02aaccfffec51fda");

        IfidData ifidData = IfidData.Codec.INSTANCE.decode(null, encoded);

        assertNotNull(ifidData);
        assertEquals("02aaccfffec51fda", toHex(ifidData.getValue()));
    }

    @Test
    @DisplayName("ifid data is encoded successfully")
    void testEncode() {
        IfidData ifidData = new IfidData(fromHex("02aaccfffec51fda"));
        byte[] encoded = IfidData.Codec.INSTANCE.encode(null, ifidData);

        assertEquals("02aaccfffec51fda", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid ifid data is decoded into null")
    void testDecodeInvalid() {
        {
            byte[] encoded = fromHex("");
            IfidData ifidData = IfidData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ifidData);
        }
        {
            byte[] encoded = fromHex("02aaccfffec51fda00");
            IfidData ifidData = IfidData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ifidData);
        }
        {
            byte[] encoded = fromHex("02aaccfffec51f");
            IfidData ifidData = IfidData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ifidData);
        }
    }

}