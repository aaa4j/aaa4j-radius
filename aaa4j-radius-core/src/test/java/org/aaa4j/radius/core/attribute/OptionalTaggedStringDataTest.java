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

@DisplayName("OptionalTaggedStringData")
class OptionalTaggedStringDataTest {

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
            new OptionalTaggedStringData(null, 2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new OptionalTaggedStringData(new byte[3], 32);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new OptionalTaggedStringData(new byte[3], -1);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        OptionalTaggedStringData optionalTaggedStringData = new OptionalTaggedStringData(fromHex("8af9bc"), 31);

        assertEquals(4, optionalTaggedStringData.length());
        assertEquals("8af9bc", toHex(optionalTaggedStringData.getValue()));
        assertTrue(optionalTaggedStringData.getTag().isPresent());
        assertEquals(31, optionalTaggedStringData.getTag().get());
    }

    @Test
    @DisplayName("Optional tagged string data is decoded successfully")
    void testDecode() {
        {
            // With tag
            byte[] encoded = fromHex("028af9bc");

            OptionalTaggedStringData optionalTaggedStringData = OptionalTaggedStringData.Codec.INSTANCE.decode(null,
                    null, encoded);

            assertNotNull(optionalTaggedStringData);
            assertEquals("8af9bc", toHex(optionalTaggedStringData.getValue()));
            assertTrue(optionalTaggedStringData.getTag().isPresent());
            assertEquals(2, optionalTaggedStringData.getTag().get());
        }
        {
            // Without tag
            byte[] encoded = fromHex("8af9bc");

            OptionalTaggedStringData optionalTaggedStringData = OptionalTaggedStringData.Codec.INSTANCE.decode(null,
                    null, encoded);

            assertNotNull(optionalTaggedStringData);
            assertEquals("8af9bc", toHex(optionalTaggedStringData.getValue()));
            assertFalse(optionalTaggedStringData.getTag().isPresent());
        }
    }

    @Test
    @DisplayName("An empty value is decoded into null")
    void testDecodeEmpty() {
        // RFC 2868 gives these attributes a minimum length of 3, so there is always at least one octet of value.
        // An attribute with none is malformed and is left undecoded rather than reported as an empty value.
        assertNull(OptionalTaggedStringData.Codec.INSTANCE.decode(null, null, fromHex("")));
    }

    @Test
    @DisplayName("The tag is recognised up to 0x1f and treated as data from 0x20")
    void testDecodeTagBoundary() {
        // RFC 2868 section 3.3 puts the boundary between 0x1f and 0x20, so these two cases pin it exactly
        OptionalTaggedStringData tagged = OptionalTaggedStringData.Codec.INSTANCE.decode(null, null,
                fromHex("1f8af9bc"));

        assertNotNull(tagged);
        assertTrue(tagged.getTag().isPresent());
        assertEquals(31, tagged.getTag().get());
        assertEquals("8af9bc", toHex(tagged.getValue()));

        // 0x20 is the first octet value that counts as data rather than as a tag, so it stays in the value
        OptionalTaggedStringData untagged = OptionalTaggedStringData.Codec.INSTANCE.decode(null, null,
                fromHex("208af9bc"));

        assertNotNull(untagged);
        assertFalse(untagged.getTag().isPresent());
        assertEquals("208af9bc", toHex(untagged.getValue()));
    }

    @Test
    @DisplayName("Optional tagged string data is re-encoded into the bytes it was decoded from")
    void testDecodeEncodeRoundTrip() {
        // Every wire form has to survive a decode followed by an encode unchanged, which is what a proxy does
        // when it forwards an attribute it received. The forms are: no tag octet, an explicit zero tag, a real
        // tag, the highest tag, and a leading octet above 0x1f that is data rather than a tag.
        for (String wire : new String[] { "8af9bc", "008af9bc", "058af9bc", "1f8af9bc", "208af9bc" }) {
            byte[] encoded = fromHex(wire);

            OptionalTaggedStringData decoded = OptionalTaggedStringData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(decoded, wire);
            assertEquals(wire, toHex(OptionalTaggedStringData.Codec.INSTANCE.encode(null, null, decoded)), wire);
        }
    }

    @Test
    @DisplayName("Both wire forms of an unused tag are accepted")
    void testDecodeUnusedTag() {
        // FreeRADIUS omits the tag octet altogether when the tag is unused, since it treats zero as "no tag",
        // whereas RFC 3580 section 3.31 has senders set the octet to zero explicitly. Both forms appear on the
        // wire and both have to yield the same value.
        OptionalTaggedStringData omitted = OptionalTaggedStringData.Codec.INSTANCE.decode(null, null,
                fromHex("8af9bc"));

        assertNotNull(omitted);
        assertEquals("8af9bc", toHex(omitted.getValue()));
        assertFalse(omitted.getTag().isPresent());

        OptionalTaggedStringData explicit = OptionalTaggedStringData.Codec.INSTANCE.decode(null, null,
                fromHex("008af9bc"));

        assertNotNull(explicit);
        assertEquals("8af9bc", toHex(explicit.getValue()));
        assertTrue(explicit.getTag().isPresent());
        assertEquals(0, explicit.getTag().get());
    }

    @Test
    @DisplayName("Optional tagged string data is encoded successfully")
    void testEncode() {
        {
            // With tag
            OptionalTaggedStringData optionalTaggedStringData = new OptionalTaggedStringData(fromHex("8af9bc"), 2);
            byte[] encoded = OptionalTaggedStringData.Codec.INSTANCE.encode(null, null, optionalTaggedStringData);

            assertEquals("028af9bc", toHex(encoded));
        }
        {
            // Without tag
            OptionalTaggedStringData optionalTaggedStringData = new OptionalTaggedStringData(fromHex("8af9bc"));
            byte[] encoded = OptionalTaggedStringData.Codec.INSTANCE.encode(null, null, optionalTaggedStringData);

            assertEquals("8af9bc", toHex(encoded));
        }
    }

    @Test
    @DisplayName("Optional tagged string data is encoded and decoded successfully using a data filter")
    void testEncodeDecodeWithDataFilter() {
        OptionalTaggedStringData.Codec codec = new OptionalTaggedStringData.Codec(MARKER_FILTER);

        {
            // With tag. Only the string is filtered, so the tag stays in front of the filtered data rather than
            // being filtered along with it.
            byte[] encoded = codec.encode(null, null, new OptionalTaggedStringData(fromHex("8af9bc"), 2));

            assertEquals("02aa8af9bc", toHex(encoded));

            OptionalTaggedStringData decoded = codec.decode(null, null, encoded);

            assertNotNull(decoded);
            assertEquals("8af9bc", toHex(decoded.getValue()));
            assertTrue(decoded.getTag().isPresent());
            assertEquals(2, decoded.getTag().get());
        }
        {
            // Without tag
            byte[] encoded = codec.encode(null, null, new OptionalTaggedStringData(fromHex("8af9bc")));

            assertEquals("aa8af9bc", toHex(encoded));

            OptionalTaggedStringData decoded = codec.decode(null, null, encoded);

            assertNotNull(decoded);
            assertEquals("8af9bc", toHex(decoded.getValue()));
            assertFalse(decoded.getTag().isPresent());
        }
    }

}