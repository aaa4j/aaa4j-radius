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

import org.aaa4j.radius.core.attribute.attributes.TunnelAssignmentId;
import org.aaa4j.radius.core.attribute.attributes.TunnelClientAuthId;
import org.aaa4j.radius.core.attribute.attributes.TunnelClientEndpoint;
import org.aaa4j.radius.core.attribute.attributes.TunnelMediumType;
import org.aaa4j.radius.core.attribute.attributes.TunnelPassword;
import org.aaa4j.radius.core.attribute.attributes.TunnelPreference;
import org.aaa4j.radius.core.attribute.attributes.TunnelPrivateGroupId;
import org.aaa4j.radius.core.attribute.attributes.TunnelServerAuthId;
import org.aaa4j.radius.core.attribute.attributes.TunnelServerEndpoint;
import org.aaa4j.radius.core.attribute.attributes.TunnelType;
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodec;
import org.aaa4j.radius.core.packet.PacketCodecException;
import org.aaa4j.radius.core.util.SecureRandomProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round trips the RFC 2868 tunnel attributes through the standard dictionary. These attributes are only correct if the
 * dictionary registers them with a tagged data type, so this covers the dictionary registrations rather than the data
 * types themselves.
 */
@DisplayName("Tunnel attributes (RFC 2868)")
class TunnelAttributeTest {

    private static final byte[] SECRET = "secret".getBytes(StandardCharsets.UTF_8);

    private static final byte[] REQUEST_AUTHENTICATOR = new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10
    };

    private static final int ACCESS_ACCEPT = 2;

    private static Packet roundTrip(List<Attribute<?>> attributes) throws PacketCodecException {
        PacketCodec packetCodec = new PacketCodec(new StandardDictionary(), new SecureRandomProvider());

        byte[] encoded = packetCodec.encodeResponse(new Packet(ACCESS_ACCEPT, attributes), SECRET, 1,
                REQUEST_AUTHENTICATOR);

        return packetCodec.decodeResponse(encoded, SECRET, REQUEST_AUTHENTICATOR);
    }

    /**
     * Builds a valid Access-Accept carrying the given attribute bytes verbatim, so that a decode can be driven from a
     * chosen wire form rather than from attributes this library built itself.
     */
    private static byte[] accessAccept(byte[] attributeBytes) throws Exception {
        byte[] packet = new byte[20 + attributeBytes.length];

        packet[0] = ACCESS_ACCEPT;
        packet[1] = 1;
        packet[2] = (byte) ((packet.length >> 8) & 0xff);
        packet[3] = (byte) (packet.length & 0xff);
        System.arraycopy(REQUEST_AUTHENTICATOR, 0, packet, 4, 16);
        System.arraycopy(attributeBytes, 0, packet, 20, attributeBytes.length);

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(packet, 0, 4);
        md5.update(REQUEST_AUTHENTICATOR);
        md5.update(packet, 20, packet.length - 20);
        md5.update(SECRET);
        System.arraycopy(md5.digest(), 0, packet, 4, 16);

        return packet;
    }

    private static byte[] attributesOf(byte[] packet) {
        return Arrays.copyOfRange(packet, 20, packet.length);
    }

    @Test
    @DisplayName("Proxying preserves the received wire form of tagged and untagged attributes")
    void proxyPreservesTheReceivedWireForm() throws Exception {
        // A proxy decodes what it received and re-encodes it onwards. Whether the sender included a tag octet,
        // and what it put in it, has to survive that trip untouched. Attribute 81 appears three ways: with no tag
        // octet at all, with an explicit zero tag, and with a real tag.
        byte[] attributeBytes = fromHex(
                // 64 Tunnel-Type, len 6, tag 0, VLAN (13)
                "400600" + "00000d"
                // 64 Tunnel-Type, len 6, tag 1, VLAN (13)
                        + "40060100" + "000d"
                        // 81 Tunnel-Private-Group-Id, len 5, no tag octet, "100"
                        + "5105" + "313030"
                        // 81 Tunnel-Private-Group-Id, len 6, explicit zero tag, "100"
                        + "510600" + "313030"
                        // 81 Tunnel-Private-Group-Id, len 6, tag 5, "200"
                        + "510605" + "323030"
                        // 66 Tunnel-Client-Endpoint, len 5, no tag octet, "abc"
                        + "4205" + "616263");

        byte[] received = accessAccept(attributeBytes);

        PacketCodec packetCodec = new PacketCodec(new StandardDictionary(), new SecureRandomProvider());

        Packet decoded = packetCodec.decodeResponse(received, SECRET, REQUEST_AUTHENTICATOR);

        assertEquals(6, decoded.getAttributes().size());

        byte[] forwarded = packetCodec.encodeResponse(decoded, SECRET, 1, REQUEST_AUTHENTICATOR);

        assertEquals(toHex(attributesOf(received)), toHex(attributesOf(forwarded)));
    }

    @Test
    @DisplayName("Proxying preserves tags when the shared secret changes")
    void proxyPreservesTagsAcrossSecretChange() throws PacketCodecException {
        // Tunnel-Password can't be forwarded verbatim: a proxy has a different secret with the next hop, so the
        // value has to be decrypted and re-encrypted under a fresh salt. The tag is what groups it with the other
        // attributes describing the same tunnel, so it has to survive that.
        byte[] upstreamSecret = "upstream".getBytes(StandardCharsets.UTF_8);
        byte[] downstreamSecret = "downstream".getBytes(StandardCharsets.UTF_8);

        byte[] downstreamAuthenticator = new byte[] {
                0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
                0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20
        };

        PacketCodec packetCodec = new PacketCodec(new StandardDictionary(), new SecureRandomProvider());

        byte[] fromServer = packetCodec.encodeResponse(new Packet(ACCESS_ACCEPT, Arrays.asList(
                new TunnelType(new TaggedEnumData(13, 5)),
                new TunnelPrivateGroupId(new OptionalTaggedTextData("100", 5)),
                new TunnelPassword(new TaggedStringData("s3cret".getBytes(StandardCharsets.UTF_8), 5)))),
                upstreamSecret, 1, REQUEST_AUTHENTICATOR);

        Packet received = packetCodec.decodeResponse(fromServer, upstreamSecret, REQUEST_AUTHENTICATOR);

        byte[] toClient = packetCodec.encodeResponse(received, downstreamSecret, 1, downstreamAuthenticator);

        Packet decoded = packetCodec.decodeResponse(toClient, downstreamSecret, downstreamAuthenticator);

        assertEquals(5, decoded.getAttribute(TunnelType.class).get().getData().getTag());
        assertEquals(5, decoded.getAttribute(TunnelPrivateGroupId.class).get().getData().getTag().get());
        assertEquals(5, decoded.getAttribute(TunnelPassword.class).get().getData().getTag());

        assertEquals("s3cret", new String(decoded.getAttribute(TunnelPassword.class).get().getData().getValue(),
                StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("The RFC 3580 VLAN assignment attributes round trip with an unused tag")
    void vlanAssignment() throws PacketCodecException {
        // RFC 3580 section 3.31: where it's only desired to specify the VLANID, the tag is set to zero
        Packet decoded = roundTrip(Arrays.asList(
                new TunnelType(new TaggedEnumData(13, 0)),
                new TunnelMediumType(new TaggedEnumData(6, 0)),
                new TunnelPrivateGroupId(new OptionalTaggedTextData("100", 0))));

        assertTrue(decoded.getAttribute(TunnelType.class).isPresent());
        assertEquals(13, decoded.getAttribute(TunnelType.class).get().getData().getValue());
        assertEquals(0, decoded.getAttribute(TunnelType.class).get().getData().getTag());

        assertEquals(6, decoded.getAttribute(TunnelMediumType.class).get().getData().getValue());
        assertEquals(0, decoded.getAttribute(TunnelMediumType.class).get().getData().getTag());

        assertEquals("100", decoded.getAttribute(TunnelPrivateGroupId.class).get().getData().getValue());
        assertEquals(0, decoded.getAttribute(TunnelPrivateGroupId.class).get().getData().getTag().get());
    }

    @Test
    @DisplayName("A non-zero tag survives the round trip rather than folding into the value")
    void nonZeroTagIsNotFoldedIntoTheValue() throws PacketCodecException {
        // Decoded as an untagged 32-bit integer this would read as 16777229 rather than 13
        Packet decoded = roundTrip(Collections.singletonList(
                new TunnelType(new TaggedEnumData(13, 1))));

        TunnelType tunnelType = decoded.getAttribute(TunnelType.class).get();

        assertEquals(13, tunnelType.getData().getValue());
        assertEquals(1, tunnelType.getData().getTag());
    }

    @Test
    @DisplayName("Two alternative tunnels are kept apart by their tags")
    void twoTunnelsAreGroupedByTag() throws PacketCodecException {
        Packet decoded = roundTrip(Arrays.asList(
                new TunnelType(new TaggedEnumData(13, 1)),
                new TunnelPrivateGroupId(new OptionalTaggedTextData("100", 1)),
                new TunnelPreference(new TaggedIntegerData(1, 1)),
                new TunnelType(new TaggedEnumData(3, 2)),
                new TunnelPrivateGroupId(new OptionalTaggedTextData("200", 2)),
                new TunnelPreference(new TaggedIntegerData(2, 2))));

        List<TunnelType> tunnelTypes = decoded.getAttributes(TunnelType.class);
        List<TunnelPrivateGroupId> groupIds = decoded.getAttributes(TunnelPrivateGroupId.class);
        List<TunnelPreference> preferences = decoded.getAttributes(TunnelPreference.class);

        assertEquals(2, tunnelTypes.size());
        assertEquals(2, groupIds.size());
        assertEquals(2, preferences.size());

        // Tag 1 is the VLAN, tag 2 is the L2TP tunnel
        assertEquals(13, tunnelTypes.get(0).getData().getValue());
        assertEquals(1, tunnelTypes.get(0).getData().getTag());
        assertEquals("100", groupIds.get(0).getData().getValue());
        assertEquals(1, groupIds.get(0).getData().getTag().get());
        assertEquals(1, preferences.get(0).getData().getValue());
        assertEquals(1, preferences.get(0).getData().getTag());

        assertEquals(3, tunnelTypes.get(1).getData().getValue());
        assertEquals(2, tunnelTypes.get(1).getData().getTag());
        assertEquals("200", groupIds.get(1).getData().getValue());
        assertEquals(2, groupIds.get(1).getData().getTag().get());
        assertEquals(2, preferences.get(1).getData().getValue());
        assertEquals(2, preferences.get(1).getData().getTag());
    }

    @Test
    @DisplayName("The text tunnel attributes round trip with and without a tag")
    void optionallyTaggedTextAttributes() throws PacketCodecException {
        List<Attribute<?>> attributes = new ArrayList<>();

        attributes.add(new TunnelClientEndpoint(new OptionalTaggedTextData("192.0.2.1", 1)));
        attributes.add(new TunnelServerEndpoint(new OptionalTaggedTextData("192.0.2.2")));
        attributes.add(new TunnelAssignmentId(new OptionalTaggedTextData("assignment", 3)));
        attributes.add(new TunnelClientAuthId(new OptionalTaggedTextData("client")));
        attributes.add(new TunnelServerAuthId(new OptionalTaggedTextData("server", 5)));

        Packet decoded = roundTrip(attributes);

        assertEquals("192.0.2.1", decoded.getAttribute(TunnelClientEndpoint.class).get().getData().getValue());
        assertEquals(1, decoded.getAttribute(TunnelClientEndpoint.class).get().getData().getTag().get());

        assertEquals("192.0.2.2", decoded.getAttribute(TunnelServerEndpoint.class).get().getData().getValue());
        assertTrue(!decoded.getAttribute(TunnelServerEndpoint.class).get().getData().getTag().isPresent());

        assertEquals("assignment", decoded.getAttribute(TunnelAssignmentId.class).get().getData().getValue());
        assertEquals(3, decoded.getAttribute(TunnelAssignmentId.class).get().getData().getTag().get());

        assertEquals("client", decoded.getAttribute(TunnelClientAuthId.class).get().getData().getValue());
        assertTrue(!decoded.getAttribute(TunnelClientAuthId.class).get().getData().getTag().isPresent());

        assertEquals("server", decoded.getAttribute(TunnelServerAuthId.class).get().getData().getValue());
        assertEquals(5, decoded.getAttribute(TunnelServerAuthId.class).get().getData().getTag().get());
    }

    @Test
    @DisplayName("Tunnel-Password round trips through encryption with its tag in the clear")
    void tunnelPassword() throws PacketCodecException {
        Packet decoded = roundTrip(Collections.singletonList(
                new TunnelPassword(new TaggedStringData("s3cret".getBytes(StandardCharsets.UTF_8), 1))));

        TunnelPassword tunnelPassword = decoded.getAttribute(TunnelPassword.class).get();

        assertEquals("s3cret", new String(tunnelPassword.getData().getValue(), StandardCharsets.UTF_8));
        assertEquals(1, tunnelPassword.getData().getTag());
    }

    @Test
    @DisplayName("An over-long Tunnel-Password is rejected rather than silently truncated")
    void overLongTunnelPasswordIsRejected() throws PacketCodecException {
        // The encoded value is the tag, a two octet salt, and the password padded out to a multiple of 16. The
        // longest password that still fits a 253 octet attribute is therefore 239 octets. The data type's own
        // length() reports the plaintext length and so can't catch this; the check happens when the encoded
        // value is wrapped for transmission.
        byte[] longest = new byte[239];
        Arrays.fill(longest, (byte) 'x');

        Packet decoded = roundTrip(Collections.singletonList(
                new TunnelPassword(new TaggedStringData(longest, 1))));

        assertTrue(Arrays.equals(longest, decoded.getAttribute(TunnelPassword.class).get().getData().getValue()));

        byte[] tooLong = new byte[240];
        Arrays.fill(tooLong, (byte) 'x');

        List<Attribute<?>> attributes = Collections.singletonList(
                new TunnelPassword(new TaggedStringData(tooLong, 1)));

        assertThrows(IllegalArgumentException.class, () -> roundTrip(attributes));
    }

    @Test
    @DisplayName("Tunnel-Password carries arbitrary octets rather than text")
    void tunnelPasswordIsNotText() throws PacketCodecException {
        // A password is not required to be valid UTF-8, so the data type must be octets
        byte[] password = new byte[] { (byte) 0x80, (byte) 0xfe, 0x00, (byte) 0xff };

        Packet decoded = roundTrip(Collections.singletonList(
                new TunnelPassword(new TaggedStringData(password, 2))));

        TunnelPassword tunnelPassword = decoded.getAttribute(TunnelPassword.class).get();

        assertTrue(Arrays.equals(password, tunnelPassword.getData().getValue()));
        assertEquals(2, tunnelPassword.getData().getTag());
    }

}
