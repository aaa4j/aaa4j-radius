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

import org.aaa4j.radius.core.attribute.attributes.ExtendedVendorSpecific1;
import org.aaa4j.radius.core.attribute.attributes.ExtendedVendorSpecific5;
import org.aaa4j.radius.core.attribute.attributes.ExtendedVendorSpecific6;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Packet-level tests for the "evs" data type across every space it can occupy: 241.26 (short extended, always a single
 * attribute), and 245.26 / 246.26 (long extended, which fragments a value across multiple attributes). Each is crossed
 * with a known vendor type (typed child) and an unknown one (raw child). Attributes are built and encoded rather than
 * hand-coded as hex, then round-tripped, so the tests describe structure rather than byte arithmetic.
 */
@DisplayName("Extended-Vendor-Specific (evs)")
class ExtendedVendorSpecificTest {

    private static final int VENDOR_ID = 32473;

    private static final byte[] SECRET = "abc123".getBytes(US_ASCII);

    private static final byte[] AUTHENTICATOR = fromHex("f58c0714b19ce47b2e4976e62dd7d6fc");

    /**
     * Provides a typed integer child for vendor {@value #VENDOR_ID} type 1, and a string child for type 2, in each of
     * the three EVS spaces. Composed with the standard dictionary, which provides the EVS wrapper definitions.
     */
    private static class TestDictionary implements Dictionary {

        @Override
        public PacketDefinition getPacketDefinition(int code) {
            return null;
        }

        @Override
        public AttributeDefinition<?, ?> getAttributeDefinition(AttributeType type) {
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
            for (int space : new int[] { 241, 245, 246 }) {
                if (type.equals(new AttributeType(space, 26, VENDOR_ID, 1))) {
                    return new TlvDefinition(type, "Test-Integer", IntegerData.Codec.INSTANCE);
                }

                if (type.equals(new AttributeType(space, 26, VENDOR_ID, 2))) {
                    return new TlvDefinition(type, "Test-String", StringData.Codec.INSTANCE);
                }
            }

            return null;
        }

    }

    private PacketIdGenerator mockedPacketIdGenerator;

    private PacketCodec packetCodec;

    @BeforeEach
    void setUp() {
        mockedPacketIdGenerator = mock(PacketIdGenerator.class);
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        packetCodec = new PacketCodec(Dictionaries.of(new TestDictionary(), new StandardDictionary()),
                mock(RandomProvider.class), mockedPacketIdGenerator);
    }

    // --- 241.26: short extended space, always a single attribute --------------------------------

    @Test
    @DisplayName("Short-extended EVS, known vendor type, decodes to a typed child")
    void shortExtendedKnownVendor() throws PacketCodecException {
        Attribute<?> decoded = roundTrip(new ExtendedVendorSpecific1(new EvsData(VENDOR_ID, 1, new IntegerData(5))),
                241, 1);

        assertThat(decoded, instanceOf(ExtendedVendorSpecific1.class));

        EvsData evsData = (EvsData) decoded.getData();

        assertEquals(VENDOR_ID, evsData.getVendorId());
        assertEquals(1, evsData.getVendorType());
        assertThat(evsData.getData(), instanceOf(IntegerData.class));
        assertEquals(5, ((IntegerData) evsData.getData()).getValue());
    }

    @Test
    @DisplayName("Short-extended EVS, unknown vendor type, keeps the raw child")
    void shortExtendedUnknownVendor() throws PacketCodecException {
        Attribute<?> decoded = roundTrip(
                new ExtendedVendorSpecific1(new EvsData(VENDOR_ID, 99, new RawData(fromHex("aabbcc")))), 241, 1);

        assertThat(decoded, instanceOf(ExtendedVendorSpecific1.class));

        EvsData evsData = (EvsData) decoded.getData();

        assertEquals(99, evsData.getVendorType());
        assertThat(evsData.getData(), instanceOf(RawData.class));
        assertEquals("aabbcc", toHex(((RawData) evsData.getData()).getValue()));
    }

    // --- 245.26: long extended space, single fragment ------------------------------------------

    @Test
    @DisplayName("Long-extended EVS in one fragment, known vendor type, decodes to a typed child")
    void longExtendedSingleFragmentKnownVendor() throws PacketCodecException {
        Attribute<?> decoded = roundTrip(new ExtendedVendorSpecific5(new EvsData(VENDOR_ID, 1, new IntegerData(5))),
                245, 1);

        assertThat(decoded, instanceOf(ExtendedVendorSpecific5.class));
        assertThat(((EvsData) decoded.getData()).getData(), instanceOf(IntegerData.class));
        assertEquals(5, ((IntegerData) ((EvsData) decoded.getData()).getData()).getValue());
    }

    @Test
    @DisplayName("Long-extended EVS in one fragment, unknown vendor type, keeps the raw child")
    void longExtendedSingleFragmentUnknownVendor() throws PacketCodecException {
        Attribute<?> decoded = roundTrip(
                new ExtendedVendorSpecific5(new EvsData(VENDOR_ID, 99, new RawData(fromHex("aabbcc")))), 245, 1);

        assertThat(((EvsData) decoded.getData()).getData(), instanceOf(RawData.class));
    }

    // --- 245.26 / 246.26: long extended space, spanning multiple fragments ----------------------

    @Test
    @DisplayName("Long-extended EVS spanning multiple fragments reassembles a raw child")
    void longExtendedSpanningUnknownVendor() throws PacketCodecException {
        // 300-byte raw child + 5 EVS header bytes exceeds one long-extended fragment
        Attribute<?> decoded = roundTrip(
                new ExtendedVendorSpecific5(new EvsData(VENDOR_ID, 7, new RawData(patterned(300)))), 245, 2);

        assertThat(decoded, instanceOf(ExtendedVendorSpecific5.class));

        EvsData evsData = (EvsData) decoded.getData();

        assertEquals(VENDOR_ID, evsData.getVendorId());
        assertEquals(7, evsData.getVendorType());
        assertThat(evsData.getData(), instanceOf(RawData.class));
        assertEquals(toHex(patterned(300)), toHex(((RawData) evsData.getData()).getValue()));
    }

    @Test
    @DisplayName("Long-extended EVS spanning multiple fragments reassembles a typed child")
    void longExtendedSpanningKnownVendor() throws PacketCodecException {
        // A 300-byte string at the string-typed path, spanning two fragments and still decoding typed
        Attribute<?> decoded = roundTrip(
                new ExtendedVendorSpecific5(new EvsData(VENDOR_ID, 2, new StringData(patterned(300)))), 245, 2);

        EvsData evsData = (EvsData) decoded.getData();

        assertThat(evsData.getData(), instanceOf(StringData.class));
        assertEquals(toHex(patterned(300)), toHex(((StringData) evsData.getData()).getValue()));
    }

    @Test
    @DisplayName("Extended-Vendor-Specific-6 behaves like 5 (long extended, spanning)")
    void longExtended246() throws PacketCodecException {
        Attribute<?> decoded = roundTrip(
                new ExtendedVendorSpecific6(new EvsData(VENDOR_ID, 7, new RawData(patterned(300)))), 246, 2);

        assertThat(decoded, instanceOf(ExtendedVendorSpecific6.class));
        assertEquals(toHex(patterned(300)), toHex(((RawData) ((EvsData) decoded.getData()).getData()).getValue()));
    }

    // --- the encode gap: typed child for an undefined path (pins current behaviour) -------------

    @Test
    @DisplayName("Encoding a typed EVS child for an undefined vendor path throws")
    void encodeTypedChildWithoutDefinition() {
        // Vendor type 3 has no dictionary definition and the child isn't raw
        Packet packet = new Packet(1,
                List.of(new ExtendedVendorSpecific5(new EvsData(VENDOR_ID, 3, new IntegerData(5)))));

        // Encoding a typed child requires a dictionary definition, so this throws for the undefined vendor path
        assertThrows(IllegalArgumentException.class, () -> {
            packetCodec.encodeRequest(packet, SECRET, AUTHENTICATOR);
        });
    }

    /**
     * Encodes a single-attribute request, asserts the value occupies at least {@code expectedTopLevel} top-level
     * attributes of {@code topLevelType}, decodes it back, asserts a byte-identical re-encode, and returns the one
     * decoded attribute.
     */
    private Attribute<?> roundTrip(Attribute<?> attribute, int topLevelType, int expectedTopLevel)
            throws PacketCodecException {
        Packet packet = new Packet(1, List.of(attribute));

        byte[] encoded = packetCodec.encodeRequest(packet, SECRET, AUTHENTICATOR);

        assertEquals(expectedTopLevel, countTopLevel(encoded, topLevelType));

        Packet decoded = packetCodec.decodeRequest(encoded, SECRET);

        assertEquals(1, decoded.getAttributes().size());

        byte[] reencoded = packetCodec.encodeRequest(decoded, SECRET, AUTHENTICATOR);

        assertEquals(toHex(encoded), toHex(reencoded));

        return decoded.getAttributes().get(0);
    }

    private static int countTopLevel(byte[] packet, int type) {
        int count = 0;
        int position = 20;

        while (position < packet.length) {
            if ((packet[position] & 0xff) == type) {
                count++;
            }

            position += packet[position + 1] & 0xff;
        }

        return count;
    }

    private static byte[] patterned(int length) {
        byte[] bytes = new byte[length];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        return bytes;
    }

}
