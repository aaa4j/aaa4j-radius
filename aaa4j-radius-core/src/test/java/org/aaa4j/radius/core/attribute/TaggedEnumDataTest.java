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

@DisplayName("TaggedEnumData")
class TaggedEnumDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TaggedEnumData(1, -1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new TaggedEnumData(1, 256);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new TaggedEnumData(-1, 2);
        });

        // The tag takes the first octet, leaving three octets for the value
        assertThrows(IllegalArgumentException.class, () -> {
            new TaggedEnumData(16777216, 2);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        TaggedEnumData taggedEnumData = new TaggedEnumData(42, 2);

        assertEquals(4, taggedEnumData.length());
        assertEquals(42, taggedEnumData.getValue());
        assertEquals(2, taggedEnumData.getTag());
    }

    @Test
    @DisplayName("Tagged enum data is decoded successfully")
    void testDecode() {
        {
            byte[] encoded = fromHex("0200a411");

            TaggedEnumData taggedEnumData = TaggedEnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(taggedEnumData);
            assertEquals(42_001, taggedEnumData.getValue());
            assertEquals(2, taggedEnumData.getTag());
        }
        {
            byte[] encoded = fromHex("0000a411");

            TaggedEnumData taggedEnumData = TaggedEnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(taggedEnumData);
            assertEquals(42_001, taggedEnumData.getValue());
            assertEquals(0, taggedEnumData.getTag());
        }
        {
            byte[] encoded = fromHex("1f00a411");

            TaggedEnumData taggedEnumData = TaggedEnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(taggedEnumData);
            assertEquals(42_001, taggedEnumData.getValue());
            assertEquals(31, taggedEnumData.getTag());
        }
    }

    @Test
    @DisplayName("Tagged enum data is encoded successfully")
    void testEncode() {
        {
            byte[] encoded = TaggedEnumData.Codec.INSTANCE.encode(null, null, new TaggedEnumData(42_001, 2));

            assertEquals("0200a411", toHex(encoded));
        }
        {
            byte[] encoded = TaggedEnumData.Codec.INSTANCE.encode(null, null, new TaggedEnumData(42_001, 0));

            assertEquals("0000a411", toHex(encoded));
        }
        {
            byte[] encoded = TaggedEnumData.Codec.INSTANCE.encode(null, null, new TaggedEnumData(42_001, 31));

            assertEquals("1f00a411", toHex(encoded));
        }
    }

    @Test
    @DisplayName("Data is re-encoded into the bytes it was decoded from")
    void testDecodeEncodeRoundTrip() {
        // The tag octet is always present here, so every tag value has to survive a decode followed by an encode
        // unchanged, which is what a proxy does when it forwards an attribute it received.
        for (String wire : new String[] { "0000000d", "0100000d", "1f00000d", "2000000d", "ff00000d" }) {
            byte[] encoded = fromHex(wire);

            TaggedEnumData decoded = TaggedEnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(decoded, wire);
            assertEquals(wire, toHex(TaggedEnumData.Codec.INSTANCE.encode(null, null, decoded)), wire);
        }
    }

    @Test
    @DisplayName("An out of range tag is kept as received rather than rejected")
    void testDecodeOutOfRangeTag() {
        // 0x1f is the highest tag that identifies a tunnel. 0x20 and 0xff don't, but the octet is still kept and
        // re-encoding reproduces it, so a proxy forwards the attribute unaltered.
        for (int tag : new int[] { 0x1f, 0x20, 0xff }) {
            byte[] encoded = fromHex(String.format("%02x00a411", tag));

            TaggedEnumData data = TaggedEnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(data);
            assertEquals(tag, data.getTag());
            assertEquals(42_001, data.getValue());
            assertEquals(toHex(encoded), toHex(TaggedEnumData.Codec.INSTANCE.encode(null, null, data)));
        }
    }

    @Test
    @DisplayName("Invalid tagged enum data is decoded into null")
    void testDecodeInvalidLength() {
        {
            // Too few bytes
            byte[] encoded = fromHex("00a411");
            TaggedEnumData taggedEnumData = TaggedEnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(taggedEnumData);
        }
        {
            // Too many bytes
            byte[] encoded = fromHex("0200a41100");
            TaggedEnumData taggedEnumData = TaggedEnumData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(taggedEnumData);
        }
    }

}
