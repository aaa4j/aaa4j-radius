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

@DisplayName("TextData")
class TextDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new TextData(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // A high surrogate with no low surrogate following it can't be encoded as UTF-8
            new TextData(String.valueOf((char) 0xd800));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // A low surrogate on its own can't be encoded as UTF-8 either
            new TextData(String.valueOf((char) 0xdc00));
        });

        // A complete surrogate pair is an ordinary four-byte code point
        assertEquals(4, new TextData(new String(new char[] { (char) 0xd83d, (char) 0xde00 })).length());
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
    @DisplayName("Invalid UTF-8 text data is decoded into null")
    void testDecodeInvalidUtf8() {
        {
            // Lone continuation byte
            byte[] encoded = fromHex("ff");

            assertNull(TextData.Codec.INSTANCE.decode(null, null, encoded));
        }
        {
            // Two-byte sequence introducer followed by a byte that isn't a continuation byte
            byte[] encoded = fromHex("c328");

            assertNull(TextData.Codec.INSTANCE.decode(null, null, encoded));
        }
        {
            // Truncated three-byte sequence: "你好，世界" missing its final byte
            byte[] encoded = fromHex("e4bda0e5a5bdefbc8ce4b896e795");

            assertNull(TextData.Codec.INSTANCE.decode(null, null, encoded));
        }
        {
            // Surrogate half encoded as UTF-8 (CESU-8), which is not valid UTF-8
            byte[] encoded = fromHex("eda080");

            assertNull(TextData.Codec.INSTANCE.decode(null, null, encoded));
        }
        {
            // Overlong encoding of "/"
            byte[] encoded = fromHex("c0af");

            assertNull(TextData.Codec.INSTANCE.decode(null, null, encoded));
        }
    }

    @Test
    @DisplayName("text data round trips unchanged")
    void testDecodeIsLossless() {
        {
            // Empty
            byte[] encoded = fromHex("");

            TextData textData = TextData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(textData);
            assertEquals("", toHex(TextData.Codec.INSTANCE.encode(null, null, textData)));
        }
        {
            // NUL, a valid one-byte code point that terminates a string in some languages
            byte[] encoded = fromHex("00");

            TextData textData = TextData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(textData);
            assertEquals("00", toHex(TextData.Codec.INSTANCE.encode(null, null, textData)));
        }
        {
            // The highest one-byte code point
            byte[] encoded = fromHex("7f");

            TextData textData = TextData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(textData);
            assertEquals("7f", toHex(TextData.Codec.INSTANCE.encode(null, null, textData)));
        }
        {
            // Two-byte code point: £
            byte[] encoded = fromHex("c2a3");

            TextData textData = TextData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(textData);
            assertEquals("c2a3", toHex(TextData.Codec.INSTANCE.encode(null, null, textData)));
        }
        {
            // Four-byte code point, held as a surrogate pair in the Java string
            byte[] encoded = fromHex("f09f9880");

            TextData textData = TextData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(textData);
            assertEquals(2, textData.getValue().length());
            assertEquals("f09f9880", toHex(TextData.Codec.INSTANCE.encode(null, null, textData)));
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