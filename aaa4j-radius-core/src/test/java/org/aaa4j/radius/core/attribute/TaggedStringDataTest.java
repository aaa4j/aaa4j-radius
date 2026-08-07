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

import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.util.SecureRandomProvider;
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
            new TaggedStringData(new byte[3], 256);
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
        // Too few bytes
        byte[] encoded = fromHex("");
        TaggedStringData taggedStringData = TaggedStringData.Codec.INSTANCE.decode(null, null, encoded);

        assertNull(taggedStringData);
    }

    @Test
    @DisplayName("Data is re-encoded into the bytes it was decoded from")
    void testDecodeEncodeRoundTrip() {
        // The tag octet is always present here, so every tag value has to survive a decode followed by an encode
        // unchanged, which is what a proxy does when it forwards an attribute it received.
        for (String wire : new String[] { "008af9bc", "058af9bc", "1f8af9bc", "208af9bc", "ff8af9bc" }) {
            byte[] encoded = fromHex(wire);

            TaggedStringData decoded = TaggedStringData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(decoded, wire);
            assertEquals(wire, toHex(TaggedStringData.Codec.INSTANCE.encode(null, null, decoded)), wire);
        }
    }

    @Test
    @DisplayName("An out of range tag is kept as received rather than rejected")
    void testDecodeOutOfRangeTag() {
        // The tag octet is always present on these attributes, so a tag outside [0, 31] indicates no tunnel but
        // is neither rejected nor mistaken for part of the value. 0x20 is the first out of range value and 0xff
        // is well past it.
        for (int tag : new int[] { 0x20, 0xff }) {
            byte[] encoded = fromHex(String.format("%02x8af9bc", tag));

            TaggedStringData taggedStringData = TaggedStringData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(taggedStringData);
            assertEquals("8af9bc", toHex(taggedStringData.getValue()));
            assertEquals(tag, taggedStringData.getTag());

            // re-encoding has to reproduce the octet that arrived, so a proxy forwards it unaltered
            assertEquals(toHex(encoded),
                    toHex(TaggedStringData.Codec.INSTANCE.encode(null, null, taggedStringData)));
        }
    }

    @Test
    @DisplayName("Tagged string data is encoded and decoded successfully using a data filter")
    void testEncodeDecodeWithDataFilter() {
        // The tunnel password filter prepends a two-octet salt and pads the value out to a multiple of 16, so the
        // filtered data is always longer than the value it was given.
        TaggedStringData.Codec codec = new TaggedStringData.Codec(TunnelPasswordDataFilter.INSTANCE);

        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        byte[] password = "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        byte[] encoded = codec.encode(codecContext, new AttributeType(69), new TaggedStringData(password, 1));

        // Tag (1) followed by the salt (2) and one 16-octet cipher block
        assertEquals(19, encoded.length);

        // The tag is not encrypted, so it stays readable in the first octet
        assertEquals(1, encoded[0]);

        TaggedStringData decoded = codec.decode(codecContext, new AttributeType(69), encoded);

        assertNotNull(decoded);
        assertEquals("hello", new String(decoded.getValue(), java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(1, decoded.getTag());
    }

}