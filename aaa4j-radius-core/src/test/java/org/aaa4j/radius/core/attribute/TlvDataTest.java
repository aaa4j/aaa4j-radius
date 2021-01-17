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
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.util.SecureRandomProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.List;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    @DisplayName("Invalid tlv data is decoded into null")
    void testDecodeInvalid() {
        CodecContext codecContext = new CodecContext(new StandardDictionary(), new byte[32], new byte[16],
                new SecureRandomProvider());

        {
            // Not in dictionary
            byte[] encoded = fromHex("015205180a08002020010db80b080018c0a80a000c0600000010061220010db8" +
                    "00000000000000000000002a061220010db800000000000000000000002b0914" +
                    "0f060000000410060000001811060009e678");

            TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(1, 1), encoded);

            assertNull(tlvData);
        }
        {
            // Invalid length on a nested TLV (too few bytes)
            byte[] encoded = fromHex("015205180a08002020010db80b080018c0a80a000c0600000010061220010db8" +
                    "00000000000000000000002a061220010db800000000000000000000002b0914" +
                    "0f070000000410060000001811060009e678");

            TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 9), encoded);

            assertNull(tlvData);
        }
        {
            // Invalid length on a nested TLV (too many bytes)
            byte[] encoded = fromHex("015205180a08002020010db80b080018c0a80a000c0600000010061220010db8" +
                    "00000000000000000000002a061220010db800000000000000000000002b0914" +
                    "0f050000000410060000001811060009e678");

            TlvData tlvData = TlvData.Codec.INSTANCE.decode(codecContext, new AttributeType(241, 9), encoded);

            assertNull(tlvData);
        }
    }

}