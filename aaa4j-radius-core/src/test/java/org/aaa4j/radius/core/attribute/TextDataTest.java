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

@DisplayName("TextData")
class TextDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new TextData(null);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        {
            TextData textData = new TextData("Hello, World!");

            assertEquals(13, textData.length());
            assertEquals("Hello, World!", textData.getValue());
        }
        {
            TextData textData = new TextData("你好，世界");

            assertEquals(15, textData.length());
            assertEquals("你好，世界", textData.getValue());
        }
    }

    @Test
    @DisplayName("text data is decoded successfully")
    void testDecode() {
        {
            byte[] encoded = fromHex("48656c6c6f2c20576f726c6421");

            TextData textData = TextData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(textData);
            assertEquals("Hello, World!", textData.getValue());
        }
        {
            byte[] encoded = fromHex("e4bda0e5a5bdefbc8ce4b896e7958c");

            TextData textData = TextData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(textData);
            assertEquals("你好，世界", textData.getValue());
        }
    }

    @Test
    @DisplayName("text data is encoded successfully")
    void testEncode() {
        {
            TextData textData = new TextData("Hello, World!");
            byte[] encoded = TextData.Codec.INSTANCE.encode(null, null, textData);

            assertEquals("48656c6c6f2c20576f726c6421", toHex(encoded));
        }
        {
            TextData textData = new TextData("你好，世界");
            byte[] encoded = TextData.Codec.INSTANCE.encode(null, null, textData);

            assertEquals("e4bda0e5a5bdefbc8ce4b896e7958c", toHex(encoded));
        }
    }

}