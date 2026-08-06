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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ExtendedData")
class ExtendedDataTest {

    private static CodecContext codecContext() {
        return new CodecContext(new StandardDictionary(), new byte[32], new byte[16], new SecureRandomProvider());
    }

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new ExtendedData(241, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ExtendedData(-1, new StringData(fromHex("0001")));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ExtendedData(256, new StringData(fromHex("0001")));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // The contained data must carry at least one byte
            new ExtendedData(3, new StringData(new byte[0]));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // One byte more than fits alongside the type, length, and extended type fields
            new ExtendedData(3, new StringData(new byte[253]));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        StringData data = new StringData(fromHex("0280de81"));

        ExtendedData extendedData = new ExtendedData(3, data);

        assertEquals(5, extendedData.length());
        assertEquals(3, extendedData.getExtendedType());
        assertArrayEquals(new int[] { 3 }, extendedData.getContainedType());
        assertEquals(data, extendedData.getData());
    }

    @Test
    @DisplayName("extended data is decoded successfully")
    void testDecode() {
        CodecContext codecContext = codecContext();

        // Frag-Status (241.1) carries integer data
        byte[] encoded = fromHex("0100000001");

        ExtendedData extendedData = ExtendedData.Codec.INSTANCE.decode(codecContext, new AttributeType(241),
                encoded);

        assertNotNull(extendedData);
        assertEquals(5, extendedData.length());
        assertEquals(1, extendedData.getExtendedType());
        assertArrayEquals(new int[] { 1 }, extendedData.getContainedType());
        assertThat(extendedData.getData(), instanceOf(IntegerData.class));
        assertEquals(1, ((IntegerData) extendedData.getData()).getValue());
    }

    @Test
    @DisplayName("Unknown or undecodable contained data is decoded into raw TLV data")
    void testDecodeRaw() {
        CodecContext codecContext = codecContext();

        {
            // Extended type 200 has no dictionary definition
            byte[] encoded = fromHex("c8ab");

            ExtendedData extendedData = ExtendedData.Codec.INSTANCE.decode(codecContext, new AttributeType(241),
                    encoded);

            assertNotNull(extendedData);
            assertEquals(200, extendedData.getExtendedType());
            assertThat(extendedData.getData(), instanceOf(RawData.class));
            assertEquals("ab", toHex(((RawData) extendedData.getData()).getValue()));

            assertEquals(toHex(encoded),
                    toHex(ExtendedData.Codec.INSTANCE.encode(codecContext, new AttributeType(241), extendedData)));
        }
        {
            // Frag-Status carries integer data, which must be four bytes; three bytes can't be decoded
            byte[] encoded = fromHex("01aabbcc");

            ExtendedData extendedData = ExtendedData.Codec.INSTANCE.decode(codecContext, new AttributeType(241),
                    encoded);

            assertNotNull(extendedData);
            assertThat(extendedData.getData(), instanceOf(RawData.class));

            assertEquals(toHex(encoded),
                    toHex(ExtendedData.Codec.INSTANCE.encode(codecContext, new AttributeType(241), extendedData)));
        }
    }

    @Test
    @DisplayName("extended data is encoded successfully")
    void testEncode() {
        CodecContext codecContext = codecContext();

        ExtendedData extendedData = new ExtendedData(1, new IntegerData(1));

        byte[] encoded = ExtendedData.Codec.INSTANCE.encode(codecContext, new AttributeType(241), extendedData);

        assertEquals("0100000001", toHex(encoded));
    }

    @Test
    @DisplayName("Encoding contained data without a dictionary definition throws")
    void testEncodeUnknown() {
        CodecContext codecContext = codecContext();

        assertThrows(IllegalArgumentException.class, () -> {
            // Extended type 200 has no dictionary definition and the data isn't raw
            ExtendedData extendedData = new ExtendedData(200, new IntegerData(1));

            ExtendedData.Codec.INSTANCE.encode(codecContext, new AttributeType(241), extendedData);
        });

        // Raw TLV data carries its own octets and doesn't need a definition
        ExtendedData extendedData = new ExtendedData(200, new RawData(fromHex("ab")));

        assertEquals("c8ab",
                toHex(ExtendedData.Codec.INSTANCE.encode(codecContext, new AttributeType(241), extendedData)));
    }

    @Test
    @DisplayName("Invalid extended data is decoded into null")
    void testDecodeInvalid() {
        CodecContext codecContext = codecContext();

        {
            // Too few bytes
            byte[] encoded = fromHex("");

            assertNull(ExtendedData.Codec.INSTANCE.decode(codecContext, new AttributeType(241), encoded));
        }
        {
            // Extended type but no value
            byte[] encoded = fromHex("03");

            assertNull(ExtendedData.Codec.INSTANCE.decode(codecContext, new AttributeType(241), encoded));
        }
    }

}
