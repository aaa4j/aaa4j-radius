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

import org.aaa4j.radius.core.dictionary.AttributeDefinition;
import org.aaa4j.radius.core.dictionary.Dictionaries;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.PacketDefinition;
import org.aaa4j.radius.core.dictionary.TlvDefinition;
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodec;
import org.aaa4j.radius.core.packet.PacketCodecException;
import org.aaa4j.radius.core.packet.PacketIdGenerator;
import org.aaa4j.radius.core.util.RandomProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("VendorSpecificAttribute")
class VendorSpecificAttributeTest {

    /**
     * A dictionary with a single typed vendor attribute, Test-Integer (26.32473.1), plus a TLV definition for its data
     * codec and for vendor type 2. Composed with the standard dictionary for the Vendor-Specific container.
     */
    private static class TestDictionary implements Dictionary {

        private static final AttributeType TYPE_1 = new AttributeType(26, 32473, 1);

        private static final AttributeType TYPE_2 = new AttributeType(26, 32473, 2);

        private static final AttributeDefinition<?, ?> DEFINITION_1 = new AttributeDefinition<>(
                TYPE_1,
                "Test-Integer",
                VendorSpecificAttribute.class,
                IntegerData.class,
                new VendorSpecificAttribute.Codec<>(
                        IntegerData.Codec.INSTANCE,
                        (type, vendorId, vendorType, data) -> new VendorSpecificAttribute<>(type, vendorId,
                                vendorType, data)),
                (IntegerData data) -> new VendorSpecificAttribute<>(26, 32473, 1, data));

        @Override
        public PacketDefinition getPacketDefinition(int code) {
            return null;
        }

        @Override
        public AttributeDefinition<?, ?> getAttributeDefinition(AttributeType type) {
            return TYPE_1.equals(type) ? DEFINITION_1 : null;
        }

        @Override
        public AttributeDefinition<?, ?> getAttributeDefinition(String name) {
            return null;
        }

        @Override
        public Integer getNumericAttributeValue(AttributeType type, String name) {
            return null;
        }

        @Override
        public TlvDefinition getTlvDefinition(AttributeType type) {
            if (TYPE_1.equals(type)) {
                return new TlvDefinition(TYPE_1, "Test-Integer", IntegerData.Codec.INSTANCE);
            }

            if (TYPE_2.equals(type)) {
                return new TlvDefinition(TYPE_2, "Test-String", StringData.Codec.INSTANCE);
            }

            return null;
        }

    }

    private PacketIdGenerator mockedPacketIdGenerator;

    private PacketCodec packetCodec;

    @BeforeEach
    void setUp() {
        mockedPacketIdGenerator = mock(PacketIdGenerator.class);

        packetCodec = new PacketCodec(Dictionaries.of(new TestDictionary(), new StandardDictionary()),
                mock(RandomProvider.class), mockedPacketIdGenerator);
    }

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new VendorSpecificAttribute<>(26, 32473, 58, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new VendorSpecificAttribute<>(26, -1, 58, new IntegerData(42));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Vendor identifiers are three octets: the high octet of the field is always zero
            new VendorSpecificAttribute<>(26, 16777216, 58, new IntegerData(42));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new VendorSpecificAttribute<>(26, 32473, -1, new IntegerData(42));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new VendorSpecificAttribute<>(26, 32473, 256, new IntegerData(42));
        });
    }

    @Test
    @DisplayName("Constructor validates the data length")
    void testConstructorDataLength() {
        // A vendor specific attribute spends eight bytes on the attribute type and length, the vendor identifier,
        // the vendor type and the vendor length, leaving 247 for the data
        VendorSpecificAttribute<StringData> vendorSpecificAttribute = new VendorSpecificAttribute<>(26, 32473, 58,
                new StringData(new byte[247]));

        assertEquals(247, vendorSpecificAttribute.getData().length());

        assertThrows(IllegalArgumentException.class, () -> {
            // One byte more needs a 256 byte attribute, which the one octet length field can't express
            new VendorSpecificAttribute<>(26, 32473, 58, new StringData(new byte[248]));
        });
    }

    @Test
    @DisplayName("Vendor specific attribute in a request packet is decoded successfully")
    void decodeRequestVendorSpecificAttribute() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        byte[] encoded = fromHex("01050020f58c0714b19ce47b2e4976e62dd7d6fc1a0c00007ed9010600000005");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertEquals(1, requestPacket.getAttributes().size());
        assertThat(requestPacket.getAttributes().get(0), instanceOf(VendorSpecificAttribute.class));

        VendorSpecificAttribute<?> attribute = (VendorSpecificAttribute<?>) requestPacket.getAttributes().get(0);

        assertEquals(32473, attribute.getVendorId());
        assertEquals(1, attribute.getVendorType());
        assertThat(attribute.getData(), instanceOf(IntegerData.class));
        assertEquals(5, ((IntegerData) attribute.getData()).getValue());

        // The typed attribute re-encodes exactly as it arrived
        byte[] reencoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                requestPacket.getReceivedFields().getAuthenticator());

        assertEquals(toHex(encoded), toHex(reencoded));
    }

    @Test
    @DisplayName("Compact vendor specific attribute is decoded into its container and preserved")
    void decodeRequestCompactVendorSpecificAttribute() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        // One Vendor-Specific attribute carrying two sub-attributes: Test-Integer and vendor type 2 (string)
        byte[] encoded = fromHex("01050023f58c0714b19ce47b2e4976e62dd7d6fc1a0f00007ed90106000000050203ab");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertEquals(1, requestPacket.getAttributes().size());
        assertThat(requestPacket.getAttributes().get(0).getData(), instanceOf(VsaData.class));

        VsaData vsaData = (VsaData) requestPacket.getAttributes().get(0).getData();

        assertEquals(32473, vsaData.getVendorId());
        assertEquals(2, vsaData.getVsas().size());
        assertThat(vsaData.getVsas().get(0).getData(), instanceOf(IntegerData.class));
        assertEquals(5, ((IntegerData) vsaData.getVsas().get(0).getData()).getValue());
        assertThat(vsaData.getVsas().get(1).getData(), instanceOf(StringData.class));
        assertEquals("ab", toHex(((StringData) vsaData.getVsas().get(1).getData()).getValue()));

        // The sub-attributes surface as typed attributes within the group: Test-Integer has a typed definition;
        // vendor type 2 has only a TLV definition and falls back to a generic vendor specific attribute
        assertThat(requestPacket.getAttributes().get(0), instanceOf(VendorSpecificAttributeGroup.class));

        VendorSpecificAttributeGroup group = (VendorSpecificAttributeGroup) requestPacket.getAttributes().get(0);

        assertEquals(32473, group.getVendorId());
        assertEquals(2, group.getGroupedAttributes().size());

        VendorSpecificAttribute<?> first = (VendorSpecificAttribute<?>) group.getGroupedAttributes().get(0);

        assertEquals(1, first.getVendorType());
        assertThat(first.getData(), instanceOf(IntegerData.class));

        VendorSpecificAttribute<?> second = (VendorSpecificAttribute<?>) group.getGroupedAttributes().get(1);

        assertEquals(2, second.getVendorType());
        assertThat(second.getData(), instanceOf(StringData.class));

        // The compact form re-encodes exactly as it arrived rather than splitting
        byte[] reencoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                requestPacket.getReceivedFields().getAuthenticator());

        assertEquals(toHex(encoded), toHex(reencoded));
    }

    @Test
    @DisplayName("Compact attribute with an unknown sub-attribute keeps it raw within the group")
    void decodeRequestCompactWithUnknownSub() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        // Test-Integer followed by unknown vendor type 99
        byte[] encoded = fromHex("01050025f58c0714b19ce47b2e4976e62dd7d6fc1a1100007ed90106000000056305aabbcc");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertEquals(1, requestPacket.getAttributes().size());
        assertThat(requestPacket.getAttributes().get(0), instanceOf(VendorSpecificAttributeGroup.class));

        VendorSpecificAttributeGroup group = (VendorSpecificAttributeGroup) requestPacket.getAttributes().get(0);

        assertThat(group.getGroupedAttributes().get(0), instanceOf(VendorSpecificAttribute.class));
        assertThat(group.getGroupedAttributes().get(1), instanceOf(RawVendorSpecificAttribute.class));

        RawVendorSpecificAttribute raw = (RawVendorSpecificAttribute) group.getGroupedAttributes().get(1);

        assertEquals(99, raw.getVendorType());
        assertEquals("aabbcc", toHex(raw.getData().getValue()));

        // Packet lookups descend into the group
        assertTrue(requestPacket.getAttribute(RawVendorSpecificAttribute.class).isPresent());
        assertSame(raw, requestPacket.getAttribute(RawVendorSpecificAttribute.class).get());

        byte[] reencoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                requestPacket.getReceivedFields().getAuthenticator());

        assertEquals(toHex(encoded), toHex(reencoded));
    }

    @Test
    @DisplayName("Mixed split and compact vendor specific attributes are preserved")
    void decodeRequestMixedVendorSpecificAttributes() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        // A split attribute, a compact attribute, and an attribute with an unknown vendor type, in that order
        byte[] encoded = fromHex("0105003af58c0714b19ce47b2e4976e62dd7d6fc"
                + "1a0c00007ed9010600000005"
                + "1a0f00007ed90106000000050203ab"
                + "1a0b00007ed96305aabbcc");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertEquals(3, requestPacket.getAttributes().size());

        // The split attribute decodes to the typed attribute
        assertThat(requestPacket.getAttributes().get(0), instanceOf(VendorSpecificAttribute.class));

        // The compact attribute stays as its container with both sub-attributes decoded
        assertThat(requestPacket.getAttributes().get(1).getData(), instanceOf(VsaData.class));
        assertEquals(2, ((VsaData) requestPacket.getAttributes().get(1).getData()).getVsas().size());

        // The unknown vendor type keeps its octets
        assertThat(requestPacket.getAttributes().get(2).getData(), instanceOf(VsaData.class));
        assertThat(((VsaData) requestPacket.getAttributes().get(2).getData()).getVsas().get(0).getData(),
                instanceOf(RawData.class));

        // The compact attribute surfaces as a group, and lookups descend into it in document order: the split
        // attribute first, then the group's two sub-attributes
        assertThat(requestPacket.getAttributes().get(1), instanceOf(VendorSpecificAttributeGroup.class));
        assertEquals(3, requestPacket.getAttributes(VendorSpecificAttribute.class).size());
        assertSame(requestPacket.getAttributes().get(0),
                requestPacket.getAttribute(VendorSpecificAttribute.class).get());

        // Everything re-encodes exactly as it arrived: split stays split, compact stays compact
        byte[] reencoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                requestPacket.getReceivedFields().getAuthenticator());

        assertEquals(toHex(encoded), toHex(reencoded));
    }

    @Test
    @DisplayName("Vendor specific attribute is encoded successfully")
    void encodeRequestVendorSpecificAttribute() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        Packet requestPacket = new Packet(1, List.of(
                new VendorSpecificAttribute<>(26, 32473, 1, new IntegerData(5))));

        byte[] encoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertEquals("01050020f58c0714b19ce47b2e4976e62dd7d6fc1a0c00007ed9010600000005", toHex(encoded));
    }

    @Test
    @DisplayName("Vendor specific group is encoded to the compact form")
    void encodeRequestVendorSpecificAttributeGroup() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        Packet requestPacket = new Packet(1, List.of(new VendorSpecificAttributeGroup(26, 32473,
                List.<VendorSpecificAttribute<?>>of(
                        new VendorSpecificAttribute<>(26, 32473, 1, new IntegerData(5)),
                        new VendorSpecificAttribute<>(26, 32473, 2, new StringData(fromHex("ab")))))));

        byte[] encoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertEquals("01050023f58c0714b19ce47b2e4976e62dd7d6fc1a0f00007ed90106000000050203ab", toHex(encoded));
    }

}
