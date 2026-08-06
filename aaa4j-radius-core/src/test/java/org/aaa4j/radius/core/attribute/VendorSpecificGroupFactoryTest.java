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
import org.aaa4j.radius.core.dictionary.VendorSpecificGroupFactory;
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodec;
import org.aaa4j.radius.core.packet.PacketCodecException;
import org.aaa4j.radius.core.packet.PacketIdGenerator;
import org.aaa4j.radius.core.util.RandomProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("VendorSpecificGroupFactory")
class VendorSpecificGroupFactoryTest {

    private static final int VENDOR_ID = 32473;

    private static final AttributeType TYPE_1 = new AttributeType(26, VENDOR_ID, 1);

    private static final AttributeType TYPE_2 = new AttributeType(26, VENDOR_ID, 2);

    /**
     * A vendor's own attribute: the type and vendor identifier are fixed by the class.
     */
    private abstract static class TestVendorAttribute<D extends Data> extends VendorSpecificAttribute<D> {

        TestVendorAttribute(int vendorType, D data) {
            super(26, VENDOR_ID, vendorType, data);
        }

    }

    private static final class TestVendorInteger extends TestVendorAttribute<IntegerData> {

        TestVendorInteger(IntegerData data) {
            super(1, data);
        }

    }

    private static final class TestVendorString extends TestVendorAttribute<StringData> {

        TestVendorString(StringData data) {
            super(2, data);
        }

    }

    /**
     * The vendor's own group. The list constructor is used by the group factory when decoding.
     */
    private static final class TestVendorGroup extends VendorSpecificAttributeGroup {

        TestVendorGroup(List<VendorSpecificAttribute<?>> attributes) {
            super(26, VENDOR_ID, attributes);
        }

        TestVendorGroup(TestVendorAttribute<?>... attributes) {
            this(new ArrayList<VendorSpecificAttribute<?>>(List.of(attributes)));
        }

    }

    private static class TestDictionary implements Dictionary {

        private static final AttributeDefinition<?, ?> DEFINITION_1 = new AttributeDefinition<>(
                TYPE_1,
                "Test-Vendor-Integer",
                TestVendorInteger.class,
                IntegerData.class,
                new VendorSpecificAttribute.Codec<>(
                        IntegerData.Codec.INSTANCE,
                        (type, vendorId, vendorType, data) -> new TestVendorInteger(data)),
                (IntegerData data) -> new TestVendorInteger(data));

        private static final AttributeDefinition<?, ?> DEFINITION_2 = new AttributeDefinition<>(
                TYPE_2,
                "Test-Vendor-String",
                TestVendorString.class,
                StringData.class,
                new VendorSpecificAttribute.Codec<>(
                        StringData.Codec.INSTANCE,
                        (type, vendorId, vendorType, data) -> new TestVendorString(data)),
                (StringData data) -> new TestVendorString(data));

        @Override
        public PacketDefinition getPacketDefinition(int code) {
            return null;
        }

        @Override
        public AttributeDefinition<?, ?> getAttributeDefinition(AttributeType type) {
            if (TYPE_1.equals(type)) {
                return DEFINITION_1;
            }

            if (TYPE_2.equals(type)) {
                return DEFINITION_2;
            }

            return null;
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
                return new TlvDefinition(TYPE_1, "Test-Vendor-Integer", IntegerData.Codec.INSTANCE);
            }

            if (TYPE_2.equals(type)) {
                return new TlvDefinition(TYPE_2, "Test-Vendor-String", StringData.Codec.INSTANCE);
            }

            return null;
        }

        @Override
        public VendorSpecificGroupFactory getVendorSpecificGroupFactory(AttributeType type) {
            return new AttributeType(26, VENDOR_ID).equals(type)
                    ? (attributeType, vendorId, attributes) -> new TestVendorGroup(attributes)
                    : null;
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
    @DisplayName("Compact attribute is decoded into the vendor's own group with the vendor's own attributes")
    void decodeIntoVendorGroup() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        byte[] encoded = fromHex("010500235be4b7b78b5c8b1eb4f9e0e1b3b5f0a91a0f00007ed90106000000050203ab");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertEquals(1, requestPacket.getAttributes().size());
        assertThat(requestPacket.getAttributes().get(0), instanceOf(TestVendorGroup.class));

        TestVendorGroup group = (TestVendorGroup) requestPacket.getAttributes().get(0);

        assertEquals(2, group.getGroupedAttributes().size());
        assertThat(group.getGroupedAttributes().get(0), instanceOf(TestVendorInteger.class));
        assertThat(group.getGroupedAttributes().get(1), instanceOf(TestVendorString.class));

        byte[] reencoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("5be4b7b78b5c8b1eb4f9e0e1b3b5f0a9"));

        assertEquals(toHex(encoded), toHex(reencoded));
    }

    @Test
    @DisplayName("Vendor group survives an encode and decode round trip")
    void roundTripVendorGroup() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        Packet requestPacket = new Packet(1, List.of(new TestVendorGroup(
                new TestVendorInteger(new IntegerData(5)),
                new TestVendorString(new StringData(fromHex("ab"))))));

        byte[] encoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        Packet decodedPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertThat(decodedPacket.getAttributes().get(0), instanceOf(TestVendorGroup.class));
        assertEquals(2, decodedPacket.getAttributes(VendorSpecificAttribute.class).size());
    }

    @Test
    @DisplayName("A group of one sub-attribute is decoded into the sub-attribute, not the group")
    void decodeSingleSubAttribute() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        // A group of one and a lone vendor specific attribute encode to the same octets, so the decoded form
        // is the sub-attribute
        Packet requestPacket = new Packet(1, List.of(new TestVendorGroup(
                new TestVendorInteger(new IntegerData(5)))));

        byte[] encoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        Packet decodedPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertEquals(1, decodedPacket.getAttributes().size());
        assertThat(decodedPacket.getAttributes().get(0), instanceOf(TestVendorInteger.class));
    }

    @Test
    @DisplayName("A dictionary without a group factory decodes into the base group")
    void decodeWithoutFactory() throws PacketCodecException {
        PacketCodec plainCodec = new PacketCodec(Dictionaries.of(new TestDictionary() {

            @Override
            public VendorSpecificGroupFactory getVendorSpecificGroupFactory(AttributeType type) {
                return null;
            }

        }, new StandardDictionary()), mock(RandomProvider.class), mockedPacketIdGenerator);

        byte[] encoded = fromHex("010500235be4b7b78b5c8b1eb4f9e0e1b3b5f0a91a0f00007ed90106000000050203ab");

        Packet requestPacket = plainCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertThat(requestPacket.getAttributes().get(0), instanceOf(VendorSpecificAttributeGroup.class));
        assertEquals(VendorSpecificAttributeGroup.class, requestPacket.getAttributes().get(0).getClass());
    }

}
