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

import java.util.Arrays;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OptionalTaggedTextData")
class OptionalTaggedTextDataTest {

    /**
     * A filter that prepends a marker octet. The marker gives the filtered data a different length to the value, and is
     * outside the tag range so that filtered data is not mistaken for a tag when it's decoded.
     */
    private static final DataFilter MARKER_FILTER = new DataFilter() {

        @Override
        public byte[] decode(CodecContext codecContext, byte[] data) {
            return Arrays.copyOfRange(data, 1, data.length);
        }

        @Override
        public byte[] encode(CodecContext codecContext, byte[] data) {
            byte[] filtered = new byte[1 + data.length];

            filtered[0] = (byte) 0xaa;
            System.arraycopy(data, 0, filtered, 1, data.length);

            return filtered;
        }

    };

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new OptionalTaggedTextData(null, 2);
        });

        assertThrows(NullPointerException.class, () -> {
            new OptionalTaggedTextData(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new OptionalTaggedTextData("abc", 32);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new OptionalTaggedTextData("abc", -1);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        {
            // With tag
            OptionalTaggedTextData optionalTaggedTextData = new OptionalTaggedTextData("abc", 31);

            assertEquals(4, optionalTaggedTextData.length());
            assertEquals("abc", optionalTaggedTextData.getValue());
            assertTrue(optionalTaggedTextData.getTag().isPresent());
            assertEquals(31, optionalTaggedTextData.getTag().get());
        }
        {
            // Without tag
            OptionalTaggedTextData optionalTaggedTextData = new OptionalTaggedTextData("abc");

            assertEquals(3, optionalTaggedTextData.length());
            assertEquals("abc", optionalTaggedTextData.getValue());
            assertFalse(optionalTaggedTextData.getTag().isPresent());
        }
    }

    @Test
    @DisplayName("Length is measured in UTF-8 bytes rather than characters")
    void testLengthIsInBytes() {
        assertEquals(3, new OptionalTaggedTextData("é", 1).length());
        assertEquals(2, new OptionalTaggedTextData("é").length());
    }

    @Test
    @DisplayName("Optional tagged text data is decoded successfully")
    void testDecode() {
        {
            // With tag
            byte[] encoded = fromHex("02616263");

            OptionalTaggedTextData optionalTaggedTextData = OptionalTaggedTextData.Codec.INSTANCE.decode(null, null,
                    encoded);

            assertNotNull(optionalTaggedTextData);
            assertEquals("abc", optionalTaggedTextData.getValue());
            assertTrue(optionalTaggedTextData.getTag().isPresent());
            assertEquals(2, optionalTaggedTextData.getTag().get());
        }
        {
            // Without tag. The first octet is greater than 0x1f so it's the first byte of the text.
            byte[] encoded = fromHex("616263");

            OptionalTaggedTextData optionalTaggedTextData = OptionalTaggedTextData.Codec.INSTANCE.decode(null, null,
                    encoded);

            assertNotNull(optionalTaggedTextData);
            assertEquals("abc", optionalTaggedTextData.getValue());
            assertFalse(optionalTaggedTextData.getTag().isPresent());
        }
    }

    @Test
    @DisplayName("An empty value is decoded into null")
    void testDecodeEmpty() {
        // RFC 2868 gives these attributes a minimum length of 3, so there is always at least one octet of value.
        // An attribute with none is malformed and is left undecoded rather than reported as an empty value.
        assertNull(OptionalTaggedTextData.Codec.INSTANCE.decode(null, null, fromHex("")));
    }

    @Test
    @DisplayName("The tag is recognised up to 0x1f and treated as text from 0x20")
    void testDecodeTagBoundary() {
        // RFC 2868 section 3.3 puts the boundary between 0x1f and 0x20, so these two cases pin it exactly
        OptionalTaggedTextData tagged = OptionalTaggedTextData.Codec.INSTANCE.decode(null, null,
                fromHex("1f616263"));

        assertNotNull(tagged);
        assertTrue(tagged.getTag().isPresent());
        assertEquals(31, tagged.getTag().get());
        assertEquals("abc", tagged.getValue());

        // 0x20 is a space, and is the first octet value that counts as text rather than as a tag
        OptionalTaggedTextData untagged = OptionalTaggedTextData.Codec.INSTANCE.decode(null, null,
                fromHex("20616263"));

        assertNotNull(untagged);
        assertFalse(untagged.getTag().isPresent());
        assertEquals(" abc", untagged.getValue());
    }

    @Test
    @DisplayName("Data that isn't valid UTF-8 is decoded into null")
    void testDecodeInvalidUtf8() {
        // 0xff is above 0x1f, so RFC 2868 section 3.3 makes it the first byte of the text rather than a tag. It
        // isn't valid UTF-8, so there's no text to decode and the attribute is left undecoded, which keeps the
        // octets intact instead of substituting a replacement character for them.
        assertNull(OptionalTaggedTextData.Codec.INSTANCE.decode(null, null, fromHex("ff616263")));

        // the same byte after a valid tag
        assertNull(OptionalTaggedTextData.Codec.INSTANCE.decode(null, null, fromHex("01ff616263")));
    }

    @Test
    @DisplayName("Optional tagged text data is re-encoded into the bytes it was decoded from")
    void testDecodeEncodeRoundTrip() {
        // Every wire form has to survive a decode followed by an encode unchanged, which is what a proxy does
        // when it forwards an attribute it received. The forms are: no tag octet, an explicit zero tag, a real
        // tag, the highest tag, and a leading octet above 0x1f that is data rather than a tag.
        for (String wire : new String[] { "616263", "00616263", "05616263", "1f616263", "20616263" }) {
            byte[] encoded = fromHex(wire);

            OptionalTaggedTextData decoded = OptionalTaggedTextData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(decoded, wire);
            assertEquals(wire, toHex(OptionalTaggedTextData.Codec.INSTANCE.encode(null, null, decoded)), wire);
        }
    }

    @Test
    @DisplayName("Both wire forms of an unused tag are accepted")
    void testDecodeUnusedTag() {
        // FreeRADIUS omits the tag octet altogether when the tag is unused, since it treats zero as "no tag",
        // whereas RFC 3580 section 3.31 has senders set the octet to zero explicitly. Both forms appear on the
        // wire and both have to yield the same text.
        OptionalTaggedTextData omitted = OptionalTaggedTextData.Codec.INSTANCE.decode(null, null, fromHex("616263"));

        assertNotNull(omitted);
        assertEquals("abc", omitted.getValue());
        assertFalse(omitted.getTag().isPresent());

        OptionalTaggedTextData explicit = OptionalTaggedTextData.Codec.INSTANCE.decode(null, null,
                fromHex("00616263"));

        assertNotNull(explicit);
        assertEquals("abc", explicit.getValue());
        assertTrue(explicit.getTag().isPresent());
        assertEquals(0, explicit.getTag().get());
    }

    @Test
    @DisplayName("Optional tagged text data is encoded successfully")
    void testEncode() {
        {
            // With tag
            OptionalTaggedTextData optionalTaggedTextData = new OptionalTaggedTextData("abc", 2);
            byte[] encoded = OptionalTaggedTextData.Codec.INSTANCE.encode(null, null, optionalTaggedTextData);

            assertEquals("02616263", toHex(encoded));
        }
        {
            // Without tag
            OptionalTaggedTextData optionalTaggedTextData = new OptionalTaggedTextData("abc");
            byte[] encoded = OptionalTaggedTextData.Codec.INSTANCE.encode(null, null, optionalTaggedTextData);

            assertEquals("616263", toHex(encoded));
        }
    }

    @Test
    @DisplayName("Multi-byte text is encoded and decoded successfully")
    void testEncodeDecodeMultiByteText() {
        OptionalTaggedTextData optionalTaggedTextData = new OptionalTaggedTextData("é", 1);
        byte[] encoded = OptionalTaggedTextData.Codec.INSTANCE.encode(null, null, optionalTaggedTextData);

        assertEquals("01c3a9", toHex(encoded));

        OptionalTaggedTextData decoded = OptionalTaggedTextData.Codec.INSTANCE.decode(null, null, encoded);

        assertNotNull(decoded);
        assertEquals("é", decoded.getValue());
        assertTrue(decoded.getTag().isPresent());
        assertEquals(1, decoded.getTag().get());
    }

    @Test
    @DisplayName("Optional tagged text data is encoded and decoded successfully using a data filter")
    void testEncodeDecodeWithDataFilter() {
        OptionalTaggedTextData.Codec codec = new OptionalTaggedTextData.Codec(MARKER_FILTER);

        {
            // With tag. Only the text is filtered, so the tag stays in front of the filtered data rather than
            // being filtered along with it.
            byte[] encoded = codec.encode(null, null, new OptionalTaggedTextData("abc", 1));

            assertEquals("01aa616263", toHex(encoded));

            OptionalTaggedTextData decoded = codec.decode(null, null, encoded);

            assertNotNull(decoded);
            assertEquals("abc", decoded.getValue());
            assertTrue(decoded.getTag().isPresent());
            assertEquals(1, decoded.getTag().get());
        }
        {
            // Without tag
            byte[] encoded = codec.encode(null, null, new OptionalTaggedTextData("abc"));

            assertEquals("aa616263", toHex(encoded));

            OptionalTaggedTextData decoded = codec.decode(null, null, encoded);

            assertNotNull(decoded);
            assertEquals("abc", decoded.getValue());
            assertFalse(decoded.getTag().isPresent());
        }
    }

}
