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

import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.TlvDefinition;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EvsData")
class EvsDataTest {

    private static CodecContext codecContext() {
        Dictionary mockedDictionary = mock(Dictionary.class);

        when(mockedDictionary.getTlvDefinition(new AttributeType(241, 26, 32473, 1)))
                .thenReturn(new TlvDefinition(
                        new AttributeType(241, 26, 32473, 1),
                        "Test-Integer",
                        IntegerData.Codec.INSTANCE));

        return new CodecContext(mockedDictionary, new byte[32], new byte[16], new SecureRandomProvider());
    }

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EvsData(32473, -1, new StringData(fromHex("abcd")));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new EvsData(32473, 256, new StringData(fromHex("abcd")));
        });

        assertThrows(NullPointerException.class, () -> {
            new EvsData(32473, 10, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // The vendor data field must carry at least one byte
            new EvsData(32473, 10, new StringData(new byte[0]));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        StringData data = new StringData(fromHex("e1273a01729a"));

        EvsData evsData = new EvsData(32473, 10, data);

        assertEquals(11, evsData.length());
        assertArrayEquals(new int[] { 32473, 10 }, evsData.getContainedType());
        assertEquals(32473, evsData.getVendorId());
        assertEquals(10, evsData.getVendorType());
        assertEquals(data, evsData.getData());
    }

    @Test
    @DisplayName("evs data is decoded successfully")
    void testDecode() {
        CodecContext codecContext = codecContext();

        // Vendor type 1 has a dictionary definition with integer data
        byte[] encoded = fromHex("00007ed90100000005");

        EvsData evsData = EvsData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 26), encoded);

        assertNotNull(evsData);
        assertEquals(32473, evsData.getVendorId());
        assertEquals(1, evsData.getVendorType());
        assertThat(evsData.getData(), instanceOf(IntegerData.class));
        assertEquals(5, ((IntegerData) evsData.getData()).getValue());

        assertEquals(toHex(encoded),
                toHex(EvsData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 26), evsData)));
    }

    @Test
    @DisplayName("evs data with a high vendor type is decoded into raw TLV data")
    void testDecodeHighVendorType() {
        CodecContext codecContext = codecContext();

        {
            // Vendor type 128, the lowest vendor type with the high bit set; no dictionary definition
            byte[] encoded = fromHex("00007ed980e1273a01729a");

            EvsData evsData = EvsData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 26), encoded);

            assertNotNull(evsData);
            assertEquals(32473, evsData.getVendorId());
            assertEquals(128, evsData.getVendorType());
            assertThat(evsData.getData(), instanceOf(RawData.class));
            assertEquals("e1273a01729a", toHex(((RawData) evsData.getData()).getValue()));

            assertEquals(toHex(encoded),
                    toHex(EvsData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 26), evsData)));
        }
        {
            // Vendor type 255, the highest vendor type
            byte[] encoded = fromHex("00007ed9ffe1273a01729a");

            EvsData evsData = EvsData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 26), encoded);

            assertNotNull(evsData);
            assertEquals(255, evsData.getVendorType());
            assertThat(evsData.getData(), instanceOf(RawData.class));

            assertEquals(toHex(encoded),
                    toHex(EvsData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 26), evsData)));
        }
    }

    @Test
    @DisplayName("evs data is encoded successfully")
    void testEncode() {
        CodecContext codecContext = codecContext();

        EvsData evsData = new EvsData(32473, 1, new IntegerData(5));

        assertEquals("00007ed90100000005",
                toHex(EvsData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 26), evsData)));
    }

    @Test
    @DisplayName("Encoding vendor data without a dictionary definition throws")
    void testEncodeUnknown() {
        CodecContext codecContext = codecContext();

        assertThrows(IllegalArgumentException.class, () -> {
            EvsData evsData = new EvsData(32473, 99, new IntegerData(5));

            EvsData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 26), evsData);
        });

        // Raw TLV data carries its own octets and doesn't need a definition
        EvsData evsData = new EvsData(32473, 99, new RawData(fromHex("aabbcc")));

        assertEquals("00007ed963aabbcc",
                toHex(EvsData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 26), evsData)));
    }

    @Test
    @DisplayName("Invalid evs data is decoded into null")
    void testDecodeInvalid() {
        CodecContext codecContext = codecContext();

        {
            // Too few bytes
            byte[] encoded = fromHex("00007ed9");

            assertNull(EvsData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 26), encoded));
        }
        {
            // Vendor id and vendor type but no vendor data
            byte[] encoded = fromHex("00007ed90a");

            assertNull(EvsData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 26), encoded));
        }
    }

}
