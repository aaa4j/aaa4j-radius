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
import org.aaa4j.radius.core.attribute.attributes.AsmPrefix64;
import org.aaa4j.radius.core.attribute.attributes.BindIpv6Prefix;
import org.aaa4j.radius.core.attribute.attributes.EaLength;
import org.aaa4j.radius.core.attribute.attributes.IpPortExtIpv4Addr;
import org.aaa4j.radius.core.attribute.attributes.IpPortLimit;
import org.aaa4j.radius.core.attribute.attributes.IpPortLimitInfo;
import org.aaa4j.radius.core.attribute.attributes.IpPortType;
import org.aaa4j.radius.core.attribute.attributes.Psid;
import org.aaa4j.radius.core.attribute.attributes.PsidLen;
import org.aaa4j.radius.core.attribute.attributes.PsidOffset;
import org.aaa4j.radius.core.attribute.attributes.RuleIpv4Prefix;
import org.aaa4j.radius.core.attribute.attributes.RuleIpv6Prefix;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Br;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Configuration;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Lightweight4over6;
import org.aaa4j.radius.core.attribute.attributes.Softwire46MapE;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Multicast;
import org.aaa4j.radius.core.attribute.attributes.Softwire46PortParams;
import org.aaa4j.radius.core.attribute.attributes.Softwire46RuleBmr;
import org.aaa4j.radius.core.attribute.attributes.Softwire46RuleFmr;
import org.aaa4j.radius.core.attribute.attributes.Softwire46V4v6Bind;
import org.aaa4j.radius.core.attribute.attributes.Ipv66rdBrIpv4Address;
import org.aaa4j.radius.core.attribute.attributes.Ipv66rdConfiguration;
import org.aaa4j.radius.core.attribute.attributes.Ipv66rdIpv4MaskLen;
import org.aaa4j.radius.core.attribute.attributes.Ipv66rdPrefix;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Ipv4Address;
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodec;
import org.aaa4j.radius.core.packet.PacketCodecException;
import org.aaa4j.radius.core.util.SecureRandomProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Standard TLVs")
class StandardTlvTest {

    private static final byte[] SECRET = "abc123".getBytes(US_ASCII);

    private static final byte[] REQUEST_AUTHENTICATOR = new byte[16];

    private PacketCodec packetCodec;

    @BeforeEach
    void setUp() {
        packetCodec = new PacketCodec(new StandardDictionary(), new SecureRandomProvider(), () -> 7);
    }

    @Test
    @DisplayName("A nested TLV tree is decoded into the concrete TLV classes")
    void testNestedTlvsDecodeIntoConcreteClasses() throws Exception {
        Packet accessAccept = new Packet(2, List.of(softwire46MapE()));

        byte[] encoded = packetCodec.encodeResponse(accessAccept, SECRET, 7, REQUEST_AUTHENTICATOR);
        Packet decoded = packetCodec.decodeResponse(encoded, SECRET, REQUEST_AUTHENTICATOR);

        TlvData configuration = (TlvData) decoded.getAttribute(Softwire46Configuration.class).get().getData();

        Softwire46MapE mapE = configuration.getTlv(Softwire46MapE.class).get();
        TlvData mapEData = (TlvData) mapE.getData();

        assertThat(mapEData.getTlvs().get(0), instanceOf(Softwire46RuleBmr.class));
        assertThat(mapEData.getTlvs().get(1), instanceOf(Softwire46Br.class));
        assertThat(mapEData.getTlvs().get(2), instanceOf(Softwire46PortParams.class));

        TlvData rule = (TlvData) mapEData.getTlv(Softwire46RuleBmr.class).get().getData();

        assertThat(rule.getTlvs().get(0), instanceOf(RuleIpv6Prefix.class));
        assertThat(rule.getTlvs().get(1), instanceOf(RuleIpv4Prefix.class));
        assertEquals(16, ((IntegerData) rule.getTlv(EaLength.class).get().getData()).getValue());

        // Three levels below the attribute, found by class rather than by number
        TlvData portParams = (TlvData) mapEData.getTlv(Softwire46PortParams.class).get().getData();

        assertEquals(6, ((IntegerData) portParams.getTlv(PsidOffset.class).get().getData()).getValue());
        assertEquals(8, ((IntegerData) portParams.getTlv(PsidLen.class).get().getData()).getValue());
        assertEquals(0x1234, ((IntegerData) portParams.getTlv(Psid.class).get().getData()).getValue());
    }

    @Test
    @DisplayName("The basic and forwarding mapping rules are distinct classes")
    void testBmrAndFmrAreDistinct() throws Exception {
        // Both are named Softwire46-Rule; only the TLV number tells them apart
        assertEquals(4, Softwire46RuleBmr.TYPE);
        assertEquals(5, Softwire46RuleFmr.TYPE);
        assertEquals(Softwire46RuleBmr.NAME, Softwire46RuleFmr.NAME);

        TlvData ruleData = new TlvData(List.of(new EaLength(new IntegerData(16))));

        Packet accessAccept = new Packet(2, List.of(new Softwire46Configuration(new TlvData(List.of(
                new Softwire46MapE(new TlvData(List.of(
                        new Softwire46RuleBmr(ruleData),
                        new Softwire46RuleFmr(ruleData),
                        new Softwire46Br(new Ipv6AddrData(ipv6("2001:db8::1")))))))))));

        byte[] encoded = packetCodec.encodeResponse(accessAccept, SECRET, 7, REQUEST_AUTHENTICATOR);
        Packet decoded = packetCodec.decodeResponse(encoded, SECRET, REQUEST_AUTHENTICATOR);

        TlvData mapE = (TlvData) ((TlvData) decoded.getAttribute(Softwire46Configuration.class).get().getData())
                .getTlv(Softwire46MapE.class).get().getData();

        assertTrue(mapE.getTlv(Softwire46RuleBmr.class).isPresent());
        assertTrue(mapE.getTlv(Softwire46RuleFmr.class).isPresent());
        assertEquals(1, mapE.getTlvs(Softwire46RuleBmr.class).size());
        assertEquals(1, mapE.getTlvs(Softwire46RuleFmr.class).size());
    }

    @Test
    @DisplayName("Concrete TLVs encode to the same octets as bare TLVs")
    void testWireCompatibleWithBareTlvs() throws Exception {
        Packet withConcrete = new Packet(2, List.of(new IpPortLimitInfo(new TlvData(List.of(
                new IpPortType(new IntegerData(2)),
                new IpPortLimit(new IntegerData(1024)),
                new IpPortExtIpv4Addr(new Ipv4AddrData(ipv4("198.51.100.7"))))))));

        Packet withBare = new Packet(2, List.of(new IpPortLimitInfo(new TlvData(List.of(
                new Tlv(1, new IntegerData(2)),
                new Tlv(2, new IntegerData(1024)),
                new Tlv(3, new Ipv4AddrData(ipv4("198.51.100.7"))))))));

        assertEquals(toHex(packetCodec.encodeResponse(withBare, SECRET, 7, REQUEST_AUTHENTICATOR)),
                toHex(packetCodec.encodeResponse(withConcrete, SECRET, 7, REQUEST_AUTHENTICATOR)));
    }

    @Test
    @DisplayName("A TLV with no definition is kept as a bare TLV holding raw data")
    void testUnknownTlvStaysBare() throws PacketCodecException {
        // Type 99 has no definition inside IP-Port-Limit-Info
        Packet accessAccept = new Packet(2, List.of(new IpPortLimitInfo(new TlvData(List.of(
                new IpPortLimit(new IntegerData(1024)),
                new Tlv(99, new RawData(fromHex("aabbcc"))))))));

        byte[] encoded = packetCodec.encodeResponse(accessAccept, SECRET, 7, REQUEST_AUTHENTICATOR);
        Packet decoded = packetCodec.decodeResponse(encoded, SECRET, REQUEST_AUTHENTICATOR);

        TlvData limitInfo = (TlvData) decoded.getAttribute(IpPortLimitInfo.class).get().getData();

        assertThat(limitInfo.getTlvs().get(0), instanceOf(IpPortLimit.class));
        assertEquals(Tlv.class, limitInfo.getTlvs().get(1).getClass());
        assertEquals(99, limitInfo.getTlvs().get(1).getType());
        assertThat(limitInfo.getTlvs().get(1).getData(), instanceOf(RawData.class));

        assertEquals(toHex(encoded),
                toHex(packetCodec.encodeResponse(decoded, SECRET, 7, REQUEST_AUTHENTICATOR)));
    }

    @Test
    @DisplayName("Lookup searches only the TLVs held directly")
    void testLookupIsNotRecursive() throws Exception {
        Packet accessAccept = new Packet(2, List.of(softwire46MapE()));

        Packet decoded = packetCodec.decodeResponse(
                packetCodec.encodeResponse(accessAccept, SECRET, 7, REQUEST_AUTHENTICATOR), SECRET,
                REQUEST_AUTHENTICATOR);

        TlvData configuration = (TlvData) decoded.getAttribute(Softwire46Configuration.class).get().getData();

        // The PSID belongs to the port parameters, three levels down, not to the configuration
        assertFalse(configuration.getTlv(Psid.class).isPresent());
        assertTrue(configuration.getTlv(Softwire46MapE.class).isPresent());
    }

    @Test
    @DisplayName("The other standard TLV trees decode into their concrete classes")
    void testOtherTlvTrees() throws Exception {
        Packet accessAccept = new Packet(2, List.of(
                new Ipv66rdConfiguration(new TlvData(List.of(
                        new Ipv66rdIpv4MaskLen(new IntegerData(0)),
                        new Ipv66rdPrefix(new Ipv6PrefixData(32,
                                fromHex("20010db8000000000000000000000000"))),
                        new Ipv66rdBrIpv4Address(new Ipv4AddrData(ipv4("192.0.2.1")))))),
                new Softwire46Configuration(new TlvData(List.of(
                        new Softwire46Lightweight4over6(new TlvData(List.of(
                                new Softwire46Br(new Ipv6AddrData(ipv6("2001:db8::2"))),
                                new Softwire46V4v6Bind(new TlvData(List.of(
                                        new Softwire46Ipv4Address(new Ipv4AddrData(ipv4("192.0.2.5"))),
                                        new BindIpv6Prefix(
                                                new Ipv6PrefixData(64, fromHex("20010db800000000")))))))))))),
                new Softwire46Multicast(new TlvData(List.of(
                        new AsmPrefix64(new Ipv6PrefixData(96, fromHex("20010db80000000000000000"))))))));

        byte[] encoded = packetCodec.encodeResponse(accessAccept, SECRET, 7, REQUEST_AUTHENTICATOR);
        Packet decoded = packetCodec.decodeResponse(encoded, SECRET, REQUEST_AUTHENTICATOR);

        TlvData sixrd = (TlvData) decoded.getAttribute(Ipv66rdConfiguration.class).get().getData();

        assertTrue(sixrd.getTlv(Ipv66rdIpv4MaskLen.class).isPresent());
        assertTrue(sixrd.getTlv(Ipv66rdPrefix.class).isPresent());
        assertTrue(sixrd.getTlv(Ipv66rdBrIpv4Address.class).isPresent());

        TlvData lw = (TlvData) ((TlvData) decoded.getAttribute(Softwire46Configuration.class).get().getData())
                .getTlv(Softwire46Lightweight4over6.class).get().getData();
        TlvData bind = (TlvData) lw.getTlv(Softwire46V4v6Bind.class).get().getData();

        assertTrue(bind.getTlv(Softwire46Ipv4Address.class).isPresent());
        assertTrue(bind.getTlv(BindIpv6Prefix.class).isPresent());

        TlvData multicast = (TlvData) decoded.getAttribute(Softwire46Multicast.class).get().getData();

        assertTrue(multicast.getTlv(AsmPrefix64.class).isPresent());

        // RFC 6930 fixes the 6rd prefix suboption at 20 octets, so its prefix field is a full 16 octets, and
        // RFC 8658 section 3.3.1 fixes ASM-Prefix64 at 16 octets with a /96 prefix, so its field is 12. Decoding
        // must not trim either to the minimum the prefix length would need.
        Ipv6PrefixData sixRdPrefix = (Ipv6PrefixData) sixrd.getTlv(Ipv66rdPrefix.class).get().getData();

        assertEquals(16, sixRdPrefix.getPrefixBytes().length);
        assertEquals(32, sixRdPrefix.getPrefixLength());

        Ipv6PrefixData asmPrefix = (Ipv6PrefixData) multicast.getTlv(AsmPrefix64.class).get().getData();

        assertEquals(12, asmPrefix.getPrefixBytes().length);
        assertEquals(96, asmPrefix.getPrefixLength());

        // re-encoding reproduces the wire form, so a proxy forwards these lengths unchanged
        assertEquals(toHex(encoded), toHex(packetCodec.encodeResponse(decoded, SECRET, 7, REQUEST_AUTHENTICATOR)));
    }

    private static Softwire46Configuration softwire46MapE() throws UnknownHostException {
        return new Softwire46Configuration(new TlvData(List.of(
                new Softwire46MapE(new TlvData(List.of(
                        new Softwire46RuleBmr(new TlvData(List.of(
                                new RuleIpv6Prefix(new Ipv6PrefixData(56, fromHex("20010db800000000"))),
                                new RuleIpv4Prefix(new Ipv4PrefixData(24, ipv4("192.0.2.0"))),
                                new EaLength(new IntegerData(16))))),
                        new Softwire46Br(new Ipv6AddrData(ipv6("2001:db8::1"))),
                        new Softwire46PortParams(new TlvData(List.of(
                                new PsidOffset(new IntegerData(6)),
                                new PsidLen(new IntegerData(8)),
                                new Psid(new IntegerData(0x1234)))))))))));
    }

    private static Inet4Address ipv4(String address) throws UnknownHostException {
        return (Inet4Address) InetAddress.getByName(address);
    }

    private static Inet6Address ipv6(String address) throws UnknownHostException {
        return (Inet6Address) InetAddress.getByName(address);
    }

}
