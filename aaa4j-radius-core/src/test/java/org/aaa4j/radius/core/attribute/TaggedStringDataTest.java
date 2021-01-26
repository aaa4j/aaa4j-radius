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

@DisplayName("TaggedStringData")
class TaggedStringDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new TaggedStringData(null, 2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new TaggedStringData(new byte[3], 32);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new TaggedStringData(new byte[3], -1);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        TaggedStringData taggedStringData = new TaggedStringData(fromHex("8af9bc"), 31);

        assertEquals(4, taggedStringData.length());
        assertEquals("8af9bc", toHex(taggedStringData.getValue()));
        assertEquals(31, taggedStringData.getTag());
    }

    @Test
    @DisplayName("Tagged string data is decoded successfully")
    void testDecode() {
        byte[] encoded = fromHex("028af9bc");

        TaggedStringData taggedStringData = TaggedStringData.Codec.INSTANCE.decode(null, null, encoded);

        assertNotNull(taggedStringData);
        assertEquals("8af9bc", toHex(taggedStringData.getValue()));
        assertEquals(2, taggedStringData.getTag());
    }

    @Test
    @DisplayName("Tagged string data is encoded successfully")
    void testEncode() {
        TaggedStringData taggedStringData = new TaggedStringData(fromHex("8af9bc"), 2);
        byte[] encoded = TaggedStringData.Codec.INSTANCE.encode(null, null, taggedStringData);

        assertEquals("028af9bc", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid tagged string data is decoded into null")
    void testDecodeInvalidLength() {
        {
            // Too few bytes
            byte[] encoded = fromHex("");
            TaggedStringData taggedStringData = TaggedStringData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(taggedStringData);
        }
        {
            // Invalid tag (outside of [0, 31])
            byte[] encoded = fromHex("ff8af9bc");
            TaggedStringData taggedStringData = TaggedStringData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(taggedStringData);
        }
    }

}