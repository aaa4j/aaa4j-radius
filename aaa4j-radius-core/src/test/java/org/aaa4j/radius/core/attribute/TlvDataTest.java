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

import org.aaa4j.radius.core.attribute.TlvData.Tlv;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.TlvDefinition;
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.util.SecureRandomProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("TlvData")
class TlvDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            // TLV list is null
            new TlvData(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // TLV type is out of range
            new TlvData(List.of(new Tlv(1000, new IntegerData(42))));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // TLV data length is too long
            new TlvData(List.of(new Tlv(10, new StringData(new byte[256]))));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // TLV data is empty
            new TlvData(List.of(new Tlv(10, new StringData(new byte[0]))));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // A tlv attribute must contain at least one TLV
            new TlvData(List.of());
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() throws UnknownHostException {
        Tlv softwire46Rule = new Tlv(5, new TlvData(List.of(
                new Tlv(10, new Ipv6PrefixData(32, fromHex("20010db8"))),
                new Tlv(11, new Ipv4PrefixData(24, (Inet4Address) Inet4Address.getByName("192.168.10.0"))),
                new Tlv(12, new IntegerData(16)))));

        Tlv softwire46Br1Tlv = new Tlv(6, new Ipv6AddrData((Inet6Address) Inet6Address.getByName("2001:db8::2a")));
        Tlv softwire46Br2Tlv = new Tlv(6, new Ipv6AddrData((Inet6Address) Inet6Address.getByName("2001:db8::2b")));

        Tlv softwire46PortparamsTlv = new Tlv(9, new TlvData(List.of(
                new Tlv(15, new IntegerData(4)),
                new Tlv(16, new IntegerData(24)),
                new Tlv(17, new IntegerData(648824)))));

        TlvData tlvData = new TlvData(List.of(
                new Tlv(1, new TlvData(List.of(
                        softwire46Rule,
                        softwire46Br1Tlv,
                        softwire46Br2Tlv,
                        softwire46PortparamsTlv)))));

        assertEquals(82, tlvData.length());
    }

    @Test
    @DisplayName("tlv data is decoded successfully")
    void testDecode() throws UnknownHostException {
        byte[] encoded = fromHex("015205180a08002020010db80b080018c0a80a000c0600000010061220010db8" +
                "00000000000000000000002a061220010db800000000000000000000002b0914" +
                "0f060000000410060000001811060009e678");

        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 9), encoded);

        assertNotNull(tlvData);

        List<Tlv> tlvs = tlvData.getTlvs();

        assertEquals(1, tlvs.size());
        assertEquals(1, tlvs.get(0).getType());
        assertThat(tlvs.get(0).getData(), instanceOf(TlvData.class));

        TlvData softwire46MapETlvData = (TlvData) tlvs.get(0).getData();

        assertEquals(4, softwire46MapETlvData.getTlvs().size());

        Tlv softwire46Rule = softwire46MapETlvData.getTlvs().get(0);
        Tlv softwire46Br1Tlv = softwire46MapETlvData.getTlvs().get(1);
        Tlv softwire46Br2Tlv = softwire46MapETlvData.getTlvs().get(2);
        Tlv softwire46PortparamsTlv = softwire46MapETlvData.getTlvs().get(3);

        assertEquals(5, softwire46Rule.getType());
        assertThat(softwire46Rule.getData(), instanceOf(TlvData.class));

        TlvData softwire46RuleTlvData = (TlvData) softwire46Rule.getData();

        assertEquals(3, softwire46RuleTlvData.getTlvs().size());

        assertEquals(10, softwire46RuleTlvData.getTlvs().get(0).getType());
        assertThat(softwire46RuleTlvData.getTlvs().get(0).getData(), instanceOf(Ipv6PrefixData.class));
        Ipv6PrefixData ruleIpv6Prefix = (Ipv6PrefixData) softwire46RuleTlvData.getTlvs().get(0).getData();
        assertEquals(32, ruleIpv6Prefix.getPrefixLength());
        assertEquals("20010db8", toHex(ruleIpv6Prefix.getPrefixBytes()));

        assertEquals(11, softwire46RuleTlvData.getTlvs().get(1).getType());
        assertThat(softwire46RuleTlvData.getTlvs().get(1).getData(), instanceOf(Ipv4PrefixData.class));
        Ipv4PrefixData ruleIpv4Prefix = (Ipv4PrefixData) softwire46RuleTlvData.getTlvs().get(1).getData();
        assertEquals(24, ruleIpv4Prefix.getPrefixLength());
        assertEquals(Inet4Address.getByName("192.168.10.0"), ruleIpv4Prefix.getPrefixAddress());

        assertEquals(12, softwire46RuleTlvData.getTlvs().get(2).getType());
        assertThat(softwire46RuleTlvData.getTlvs().get(2).getData(), instanceOf(IntegerData.class));
        assertEquals(16, ((IntegerData) softwire46RuleTlvData.getTlvs().get(2).getData()).getValue());

        assertEquals(6, softwire46Br1Tlv.getType());
        assertThat(softwire46Br1Tlv.getData(), instanceOf(Ipv6AddrData.class));
        assertEquals(Inet6Address.getByName("2001:db8::2a"), ((Ipv6AddrData) softwire46Br1Tlv.getData()).getValue());

        assertEquals(6, softwire46Br2Tlv.getType());
        assertThat(softwire46Br2Tlv.getData(), instanceOf(Ipv6AddrData.class));
        assertEquals(Inet6Address.getByName("2001:db8::2b"), ((Ipv6AddrData) softwire46Br2Tlv.getData()).getValue());

        assertEquals(9, softwire46PortparamsTlv.getType());
        assertThat(softwire46PortparamsTlv.getData(), instanceOf(TlvData.class));

        TlvData softwire46PortparamsTlvData = (TlvData) softwire46PortparamsTlv.getData();
        assertEquals(3, softwire46PortparamsTlvData.getTlvs().size());

        assertEquals(15, softwire46PortparamsTlvData.getTlvs().get(0).getType());
        assertThat(softwire46PortparamsTlvData.getTlvs().get(0).getData(), instanceOf(IntegerData.class));
        assertEquals(4, ((IntegerData) softwire46PortparamsTlvData.getTlvs().get(0).getData()).getValue());

        assertEquals(16, softwire46PortparamsTlvData.getTlvs().get(1).getType());
        assertThat(softwire46PortparamsTlvData.getTlvs().get(1).getData(), instanceOf(IntegerData.class));
        assertEquals(24, ((IntegerData) softwire46PortparamsTlvData.getTlvs().get(1).getData()).getValue());

        assertEquals(17, softwire46PortparamsTlvData.getTlvs().get(2).getType());
        assertThat(softwire46PortparamsTlvData.getTlvs().get(2).getData(), instanceOf(IntegerData.class));
        assertEquals(648824, ((IntegerData) softwire46PortparamsTlvData.getTlvs().get(2).getData()).getValue());
    }

    @Test
    @DisplayName("tlv data is encoded successfully")
    void testEncode() throws UnknownHostException {
        Tlv softwire46Rule = new Tlv(5, new TlvData(List.of(
                new Tlv(10, new Ipv6PrefixData(32, fromHex("20010db8"))),
                new Tlv(11, new Ipv4PrefixData(24, (Inet4Address) Inet4Address.getByName("192.168.10.0"))),
                new Tlv(12, new IntegerData(16)))));

        Tlv softwire46Br1Tlv = new Tlv(6, new Ipv6AddrData((Inet6Address) Inet6Address.getByName("2001:db8::2a")));
        Tlv softwire46Br2Tlv = new Tlv(6, new Ipv6AddrData((Inet6Address) Inet6Address.getByName("2001:db8::2b")));

        Tlv softwire46PortparamsTlv = new Tlv(9, new TlvData(List.of(
                new Tlv(15, new IntegerData(4)),
                new Tlv(16, new IntegerData(24)),
                new Tlv(17, new IntegerData(648824)))));

        TlvData tlvData = new TlvData(List.of(
                new Tlv(1, new TlvData(List.of(
                        softwire46Rule,
                        softwire46Br1Tlv,
                        softwire46Br2Tlv,
                        softwire46PortparamsTlv)))));

        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        byte[] encoded = TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 9), tlvData);

        String expected = "015205180a08002020010db80b080018c0a80a000c0600000010061220010db8" +
                "00000000000000000000002a061220010db800000000000000000000002b0914" +
                "0f060000000410060000001811060009e678";

        assertEquals(expected, toHex(encoded));
    }

    @Test
    @DisplayName("tlv data with a length above 127 round trips successfully")
    void testLongTlv() {
        // A TLV carrying 200 bytes of data has a TLV-Length of 202, which sets the high bit of the length byte
        byte[] localId = new byte[200];

        for (int i = 0; i < localId.length; i++) {
            localId[i] = (byte) i;
        }

        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        // IP-Port-Local-Id (241.6.11) carries string data
        TlvData tlvData = new TlvData(List.of(new Tlv(11, new StringData(localId))));

        byte[] encoded = TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 6), tlvData);

        assertEquals(202, encoded.length);
        assertEquals(0x0b, encoded[0] & 0xff);
        assertEquals(0xca, encoded[1] & 0xff);

        TlvData decoded = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 6), encoded);

        assertNotNull(decoded);
        assertEquals(1, decoded.getTlvs().size());
        assertEquals(11, decoded.getTlvs().get(0).getType());
        assertThat(decoded.getTlvs().get(0).getData(), instanceOf(StringData.class));
        assertEquals(toHex(localId), toHex(((StringData) decoded.getTlvs().get(0).getData()).getValue()));
    }

    @Test
    @DisplayName("tlv data with a type above 127 is decoded successfully")
    void testDecodeHighTlvType() {
        AttributeType tlvAttributeType = new AttributeType(241, 6, 200);

        Dictionary mockedDictionary = mock(Dictionary.class);

        when(mockedDictionary.getTlvDefinition(tlvAttributeType))
                .thenReturn(new TlvDefinition(tlvAttributeType, "Test-Tlv", StringData.Codec.INSTANCE));

        CodecContext codecContext = new CodecContext(mockedDictionary, new byte[32], new byte[16],
                new SecureRandomProvider());

        byte[] encoded = fromHex("c804abcd");

        TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 6), encoded);

        assertNotNull(tlvData);

        List<Tlv> tlvs = tlvData.getTlvs();

        assertEquals(1, tlvs.size());
        assertEquals(200, tlvs.get(0).getType());
        assertThat(tlvs.get(0).getData(), instanceOf(StringData.class));
        assertEquals("abcd", toHex(((StringData) tlvs.get(0).getData()).getValue()));
    }

    @Test
    @DisplayName("Unknown TLVs are decoded into raw TLV data")
    void testDecodeUnknownTlv() {
        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        {
            // A known TLV followed by a TLV type with no dictionary definition: the unknown TLV keeps its octets
            // and the well-formed sibling still decodes
            byte[] encoded = fromHex("0106000000056305aabbcc");

            TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 6), encoded);

            assertNotNull(tlvData);
            assertEquals(2, tlvData.getTlvs().size());

            assertEquals(1, tlvData.getTlvs().get(0).getType());
            assertThat(tlvData.getTlvs().get(0).getData(), instanceOf(IntegerData.class));
            assertEquals(5, ((IntegerData) tlvData.getTlvs().get(0).getData()).getValue());

            assertEquals(99, tlvData.getTlvs().get(1).getType());
            assertThat(tlvData.getTlvs().get(1).getData(), instanceOf(RawData.class));
            assertEquals("aabbcc", toHex(((RawData) tlvData.getTlvs().get(1).getData()).getValue()));

            // The octets survive a round trip exactly as they arrived
            assertEquals("0106000000056305aabbcc",
                    toHex(TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 6), tlvData)));
        }
        {
            // An entirely unknown container type: every child is kept raw and the whole attribute round trips
            byte[] encoded = fromHex("015205180a08002020010db80b080018c0a80a000c0600000010061220010db8" +
                    "00000000000000000000002a061220010db800000000000000000000002b0914" +
                    "0f060000000410060000001811060009e678");

            TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(1, 1), encoded);

            assertNotNull(tlvData);
            assertEquals(1, tlvData.getTlvs().size());
            assertEquals(1, tlvData.getTlvs().get(0).getType());
            assertThat(tlvData.getTlvs().get(0).getData(), instanceOf(RawData.class));

            assertEquals(toHex(encoded),
                    toHex(TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(1, 1), tlvData)));
        }
    }

    @Test
    @DisplayName("Undecodable TLV data is decoded into raw TLV data")
    void testDecodeUndecodableTlv() {
        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        // IP-Port-Type (241.6.1) carries integer data, which must be four bytes; three bytes can't be decoded
        byte[] encoded = fromHex("0105aabbcc");

        TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 6), encoded);

        assertNotNull(tlvData);
        assertEquals(1, tlvData.getTlvs().size());
        assertEquals(1, tlvData.getTlvs().get(0).getType());
        assertThat(tlvData.getTlvs().get(0).getData(), instanceOf(RawData.class));
        assertEquals("aabbcc", toHex(((RawData) tlvData.getTlvs().get(0).getData()).getValue()));

        assertEquals("0105aabbcc",
                toHex(TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 6), tlvData)));
    }

    @Test
    @DisplayName("An invalid nested TLV doesn't invalidate its container")
    void testDecodeInvalidNestedTlv() {
        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        // The Softwire46 attribute from testDecode with the port params TLV corrupted (its first inner TLV claims
        // seven bytes, leaving a TLV length of zero further in): the container and its well-formed children decode
        // and only the corrupted child is kept raw
        byte[] encoded = fromHex("015205180a08002020010db80b080018c0a80a000c0600000010061220010db8" +
                "00000000000000000000002a061220010db800000000000000000000002b0914" +
                "0f070000000410060000001811060009e678");

        TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 9), encoded);

        assertNotNull(tlvData);
        assertEquals(1, tlvData.getTlvs().size());
        assertEquals(1, tlvData.getTlvs().get(0).getType());
        assertThat(tlvData.getTlvs().get(0).getData(), instanceOf(TlvData.class));

        TlvData softwire46MapETlvData = (TlvData) tlvData.getTlvs().get(0).getData();

        assertEquals(4, softwire46MapETlvData.getTlvs().size());
        assertEquals(5, softwire46MapETlvData.getTlvs().get(0).getType());
        assertThat(softwire46MapETlvData.getTlvs().get(0).getData(), instanceOf(TlvData.class));
        assertEquals(6, softwire46MapETlvData.getTlvs().get(1).getType());
        assertThat(softwire46MapETlvData.getTlvs().get(1).getData(), instanceOf(Ipv6AddrData.class));
        assertEquals(6, softwire46MapETlvData.getTlvs().get(2).getType());
        assertThat(softwire46MapETlvData.getTlvs().get(2).getData(), instanceOf(Ipv6AddrData.class));

        assertEquals(9, softwire46MapETlvData.getTlvs().get(3).getType());
        assertThat(softwire46MapETlvData.getTlvs().get(3).getData(), instanceOf(RawData.class));
        assertEquals("0f070000000410060000001811060009e678",
                toHex(((RawData) softwire46MapETlvData.getTlvs().get(3).getData()).getValue()));

        // The corrupted child re-encodes exactly as it arrived
        assertEquals(toHex(encoded),
                toHex(TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 9), tlvData)));
    }

    @Test
    @DisplayName("Encoding a TLV without a dictionary definition throws")
    void testEncodeUnknownTlv() {
        // A mocked dictionary with no definitions at all
        CodecContext codecContext = new CodecContext(mock(Dictionary.class), new byte[32], new byte[16],
                new SecureRandomProvider());

        assertThrows(IllegalArgumentException.class, () -> {
            TlvData tlvData = new TlvData(List.of(new Tlv(99, new IntegerData(5))));

            TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 6), tlvData);
        });

        // Raw TLV data carries its own octets and doesn't need a definition
        TlvData tlvData = new TlvData(List.of(new Tlv(99, new RawData(fromHex("aabbcc")))));

        assertEquals("6305aabbcc",
                toHex(TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 6), tlvData)));
    }

    @Test
    @DisplayName("TLV data is encoded by its codec, not sized by its data length")
    void testEncodeEncrypted() {
        // A TLV whose codec encrypts: five bytes of plaintext become eighteen bytes on the wire, so the buffer and
        // the TLV length field must come from the encoded size
        Dictionary mockedDictionary = mock(Dictionary.class);

        AttributeType tlvAttributeType = new AttributeType(241, 6, 16);

        when(mockedDictionary.getTlvDefinition(tlvAttributeType))
                .thenReturn(new TlvDefinition(tlvAttributeType, "Test-Encrypted-String",
                        new StringData.Codec(TunnelPasswordDataFilter.INSTANCE)));

        CodecContext codecContext = new CodecContext(mockedDictionary, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"), new SecureRandomProvider());

        TlvData tlvData = new TlvData(List.of(new Tlv(16, new StringData("hello".getBytes(US_ASCII)))));

        byte[] encoded = TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 6), tlvData);

        assertEquals(20, encoded.length);
        assertEquals(0x14, encoded[1] & 0xff);

        // And the encrypted data decodes back to the plaintext
        TlvData decoded = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 6), encoded);

        assertNotNull(decoded);
        assertEquals("hello",
                new String(((StringData) decoded.getTlvs().get(0).getData()).getValue(), US_ASCII));
    }

    @Test
    @DisplayName("Lightweight 4over6 Bind-IPv6-Prefix is decoded as an IPv6 prefix")
    void testDecodeBindIpv6Prefix() {
        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        // Softwire46-Lightweight-4over6 containing Softwire46-V4V6Bind with IPv4-Address 192.0.2.1 and
        // Bind-IPv6-Prefix 2001:db8::/64
        byte[] encoded = fromHex("031608140d06c00002010e0c004020010db800000000");

        TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 9), encoded);

        assertNotNull(tlvData);

        TlvData v4v6Bind = (TlvData) ((TlvData) tlvData.getTlvs().get(0).getData()).getTlvs().get(0).getData();

        assertEquals(13, v4v6Bind.getTlvs().get(0).getType());
        assertThat(v4v6Bind.getTlvs().get(0).getData(), instanceOf(Ipv4AddrData.class));

        assertEquals(14, v4v6Bind.getTlvs().get(1).getType());
        assertThat(v4v6Bind.getTlvs().get(1).getData(), instanceOf(Ipv6PrefixData.class));

        Ipv6PrefixData bindIpv6Prefix = (Ipv6PrefixData) v4v6Bind.getTlvs().get(1).getData();

        assertEquals(64, bindIpv6Prefix.getPrefixLength());
        assertEquals("20010db800000000", toHex(bindIpv6Prefix.getPrefixBytes()));

        assertEquals(toHex(encoded),
                toHex(TlvData.Codec.INSTANCE.encode(codecContext, new AttributeType(241, 9), tlvData)));
    }

    @Test
    @DisplayName("Invalid tlv data is decoded into null")
    void testDecodeInvalid() {
        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        {
            // TLV length below the minimum of 3
            byte[] encoded = fromHex("0b0200");

            TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 6), encoded);

            assertNull(tlvData);
        }
        {
            // TLV length exceeding the available bytes
            byte[] encoded = fromHex("01ff0000");

            TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 6), encoded);

            assertNull(tlvData);
        }
        {
            // A trailing byte that can't hold a TLV header
            byte[] encoded = fromHex("0106000000050a");

            TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 6), encoded);

            assertNull(tlvData);
        }
    }

}
