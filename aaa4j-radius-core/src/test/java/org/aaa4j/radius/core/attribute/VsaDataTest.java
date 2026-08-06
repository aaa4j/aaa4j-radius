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

import org.aaa4j.radius.core.attribute.VsaData.Vsa;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.TlvDefinition;
import org.aaa4j.radius.core.util.SecureRandomProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
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

@DisplayName("VsaData")
class VsaDataTest {

    private static CodecContext codecContext() {
        Dictionary mockedDictionary = mock(Dictionary.class);

        when(mockedDictionary.getTlvDefinition(new AttributeType(26, 32473, 1)))
                .thenReturn(new TlvDefinition(
                        new AttributeType(26, 32473, 1),
                        "Test-Integer",
                        IntegerData.Codec.INSTANCE));

        when(mockedDictionary.getTlvDefinition(new AttributeType(26, 32473, 2)))
                .thenReturn(new TlvDefinition(
                        new AttributeType(26, 32473, 2),
                        "Test-String",
                        StringData.Codec.INSTANCE));

        when(mockedDictionary.getTlvDefinition(new AttributeType(26, 32473, 16)))
                .thenReturn(new TlvDefinition(
                        new AttributeType(26, 32473, 16),
                        "Test-Encrypted-String",
                        new StringData.Codec(TunnelPasswordDataFilter.INSTANCE)));

        return new CodecContext(mockedDictionary, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"), new SecureRandomProvider());
    }

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new VsaData(-1, 58, new StringData(fromHex("ab")));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Vendor identifiers are three octets: the high octet of the field is always zero
            new VsaData(16777216, 58, new StringData(fromHex("ab")));
        });

        assertThrows(NullPointerException.class, () -> {
            new VsaData(32473, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // At least one vendor sub-attribute is required
            new VsaData(32473, List.of());
        });

        assertThrows(NullPointerException.class, () -> {
            new Vsa(58, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Vsa(-1, new StringData(fromHex("ab")));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Vsa(256, new StringData(fromHex("ab")));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // One byte more than fits alongside the attribute header, vendor id, vendor type, and vendor length
            new Vsa(58, new StringData(new byte[248]));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Two sub-attributes that together exceed the 249 bytes available for them
            new VsaData(32473, List.of(
                    new Vsa(1, new StringData(new byte[124])),
                    new Vsa(2, new StringData(new byte[124]))));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        {
            VsaData vsaData = new VsaData(32473, 58, new StringData(fromHex("9f4fde4bb9")));

            assertEquals(11, vsaData.length());
            assertEquals(32473, vsaData.getVendorId());
            assertArrayEquals(new int[] { 32473, 58 }, vsaData.getContainedType());
            assertEquals(1, vsaData.getVsas().size());
            assertEquals(58, vsaData.getVsas().get(0).getVendorType());
            assertEquals("9f4fde4bb9", toHex(((StringData) vsaData.getVsas().get(0).getData()).getValue()));
        }
        {
            // Compact data contains several attributes and so has no single contained type
            VsaData vsaData = new VsaData(32473, List.of(
                    new Vsa(1, new IntegerData(5)),
                    new Vsa(2, new StringData(fromHex("ab")))));

            assertEquals(13, vsaData.length());
            assertNull(vsaData.getContainedType());
            assertEquals(2, vsaData.getVsas().size());
        }
    }

    @Test
    @DisplayName("vsa data is decoded successfully")
    void testDecode() {
        CodecContext codecContext = codecContext();

        byte[] encoded = fromHex("00007ed9010600000005");

        VsaData vsaData = VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded);

        assertNotNull(vsaData);
        assertEquals(32473, vsaData.getVendorId());
        assertArrayEquals(new int[] { 32473, 1 }, vsaData.getContainedType());
        assertEquals(1, vsaData.getVsas().size());
        assertEquals(1, vsaData.getVsas().get(0).getVendorType());
        assertThat(vsaData.getVsas().get(0).getData(), instanceOf(IntegerData.class));
        assertEquals(5, ((IntegerData) vsaData.getVsas().get(0).getData()).getValue());
    }

    @Test
    @DisplayName("Compact vsa data is decoded successfully")
    void testDecodeCompact() {
        CodecContext codecContext = codecContext();

        byte[] encoded = fromHex("00007ed90106000000050203ab");

        VsaData vsaData = VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded);

        assertNotNull(vsaData);
        assertEquals(32473, vsaData.getVendorId());
        assertNull(vsaData.getContainedType());
        assertEquals(2, vsaData.getVsas().size());

        assertEquals(1, vsaData.getVsas().get(0).getVendorType());
        assertThat(vsaData.getVsas().get(0).getData(), instanceOf(IntegerData.class));
        assertEquals(5, ((IntegerData) vsaData.getVsas().get(0).getData()).getValue());

        assertEquals(2, vsaData.getVsas().get(1).getVendorType());
        assertThat(vsaData.getVsas().get(1).getData(), instanceOf(StringData.class));
        assertEquals("ab", toHex(((StringData) vsaData.getVsas().get(1).getData()).getValue()));

        // The compact form survives a round trip exactly as it arrived
        assertEquals(toHex(encoded),
                toHex(VsaData.Codec.INSTANCE.encode(codecContext, new AttributeType(26), vsaData)));
    }

    @Test
    @DisplayName("Unknown sub-attributes are decoded into raw TLV data")
    void testDecodeUnknownSub() {
        CodecContext codecContext = codecContext();

        {
            // Vendor type 99 has no dictionary definition: its octets are kept and the sibling still decodes
            byte[] encoded = fromHex("00007ed90106000000056305aabbcc");

            VsaData vsaData = VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded);

            assertNotNull(vsaData);
            assertEquals(2, vsaData.getVsas().size());
            assertThat(vsaData.getVsas().get(0).getData(), instanceOf(IntegerData.class));
            assertEquals(99, vsaData.getVsas().get(1).getVendorType());
            assertThat(vsaData.getVsas().get(1).getData(), instanceOf(RawData.class));
            assertEquals("aabbcc", toHex(((RawData) vsaData.getVsas().get(1).getData()).getValue()));

            assertEquals(toHex(encoded),
                    toHex(VsaData.Codec.INSTANCE.encode(codecContext, new AttributeType(26), vsaData)));
        }
        {
            // A known vendor type whose data can't be decoded (integer data must be four bytes)
            byte[] encoded = fromHex("00007ed90105aabbcc");

            VsaData vsaData = VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded);

            assertNotNull(vsaData);
            assertEquals(1, vsaData.getVsas().size());
            assertThat(vsaData.getVsas().get(0).getData(), instanceOf(RawData.class));

            assertEquals(toHex(encoded),
                    toHex(VsaData.Codec.INSTANCE.encode(codecContext, new AttributeType(26), vsaData)));
        }
        {
            // A sub-attribute with no data at all: RFC 2865 doesn't forbid it, so it round trips
            byte[] encoded = fromHex("00007ed96302");

            VsaData vsaData = VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded);

            assertNotNull(vsaData);
            assertEquals(1, vsaData.getVsas().size());
            assertThat(vsaData.getVsas().get(0).getData(), instanceOf(RawData.class));
            assertEquals(0, vsaData.getVsas().get(0).getData().length());

            assertEquals(toHex(encoded),
                    toHex(VsaData.Codec.INSTANCE.encode(codecContext, new AttributeType(26), vsaData)));
        }
    }

    @Test
    @DisplayName("vsa data is encoded successfully")
    void testEncode() {
        CodecContext codecContext = codecContext();

        {
            VsaData vsaData = new VsaData(32473, 1, new IntegerData(5));

            assertEquals("00007ed9010600000005",
                    toHex(VsaData.Codec.INSTANCE.encode(codecContext, new AttributeType(26), vsaData)));
        }
        {
            // Compact form built directly
            VsaData vsaData = new VsaData(32473, List.of(
                    new Vsa(1, new IntegerData(5)),
                    new Vsa(2, new StringData(fromHex("ab")))));

            assertEquals("00007ed90106000000050203ab",
                    toHex(VsaData.Codec.INSTANCE.encode(codecContext, new AttributeType(26), vsaData)));
        }
    }

    @Test
    @DisplayName("Sub-attribute data is encoded by its codec, not sized by its data length")
    void testEncodeEncrypted() {
        CodecContext codecContext = codecContext();

        // Vendor type 16 uses salt encryption, so five bytes of plaintext become eighteen bytes on the wire: the
        // buffer and the vendor length field must come from the encoded size
        VsaData vsaData = new VsaData(32473, 16, new StringData("hello".getBytes(US_ASCII)));

        byte[] encoded = VsaData.Codec.INSTANCE.encode(codecContext, new AttributeType(26), vsaData);

        assertEquals(24, encoded.length);
        assertEquals(0x14, encoded[5] & 0xff);

        // And the encrypted data decodes back to the plaintext
        VsaData decoded = VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded);

        assertNotNull(decoded);
        assertThat(decoded.getVsas().get(0).getData(), instanceOf(StringData.class));
        assertEquals("hello", new String(((StringData) decoded.getVsas().get(0).getData()).getValue(), US_ASCII));
    }

    @Test
    @DisplayName("Encoding a sub-attribute without a dictionary definition throws")
    void testEncodeUnknownSub() {
        CodecContext codecContext = codecContext();

        assertThrows(IllegalArgumentException.class, () -> {
            VsaData vsaData = new VsaData(32473, 99, new IntegerData(5));

            VsaData.Codec.INSTANCE.encode(codecContext, new AttributeType(26), vsaData);
        });

        // Raw TLV data carries its own octets and doesn't need a definition
        VsaData vsaData = new VsaData(32473, 99, new RawData(fromHex("aabbcc")));

        assertEquals("00007ed96305aabbcc",
                toHex(VsaData.Codec.INSTANCE.encode(codecContext, new AttributeType(26), vsaData)));
    }

    @Test
    @DisplayName("Invalid vsa data is decoded into null")
    void testDecodeInvalid() {
        CodecContext codecContext = codecContext();

        {
            // Too few bytes
            byte[] encoded = fromHex("00007ed9");

            assertNull(VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded));
        }
        {
            // Vendor id and vendor type but no vendor length
            byte[] encoded = fromHex("00007ed901");

            assertNull(VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded));
        }
        {
            // Vendor length below the minimum of 2
            byte[] encoded = fromHex("00007ed9010100");

            assertNull(VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded));
        }
        {
            // Vendor length exceeding the available bytes
            byte[] encoded = fromHex("00007ed901ff0000");

            assertNull(VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded));
        }
        {
            // A trailing byte that can't hold a sub-attribute header
            byte[] encoded = fromHex("00007ed901060000000563");

            assertNull(VsaData.Codec.INSTANCE.decode(codecContext, new AttributeType(26), encoded));
        }
    }

}
