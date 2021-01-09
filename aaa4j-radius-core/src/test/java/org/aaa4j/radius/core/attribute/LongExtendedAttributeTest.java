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

import org.aaa4j.radius.core.attribute.attributes.NasIdentifier;
import org.aaa4j.radius.core.attribute.attributes.SamlAssertion;
import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.PacketCodec;
import org.aaa4j.radius.core.packet.PacketCodecException;
import org.aaa4j.radius.core.packet.PacketIdGenerator;
import org.aaa4j.radius.core.util.RandomProvider;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@DisplayName("LongExtendedAttribute")
class LongExtendedAttributeTest {

    private PacketIdGenerator mockedPacketIdGenerator;

    private RandomProvider mockedRandomProvider;

    private PacketCodec packetCodec;

    @BeforeEach
    void setUp() {
        mockedPacketIdGenerator = mock(PacketIdGenerator.class);
        mockedRandomProvider = mock(RandomProvider.class);

        packetCodec = new PacketCodec(new StandardDictionary(), mockedRandomProvider, mockedPacketIdGenerator);
    }

    @AfterEach
    void tearDown() {
        reset(mockedPacketIdGenerator);
        reset(mockedRandomProvider);
    }

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new LongExtendedAttribute<>(245, 241, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LongExtendedAttribute<>(-1, 241, new IntegerData(256));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LongExtendedAttribute<>(256, 241, new IntegerData(256));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LongExtendedAttribute<>(241, -1, new IntegerData(256));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LongExtendedAttribute<>(241, 256, new IntegerData(256));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        LongExtendedAttribute<IntegerData> longExtendedAttribute = new LongExtendedAttribute<IntegerData>(245, 241,
                new IntegerData(256));

        assertEquals(new AttributeType(245, 241), longExtendedAttribute.getType());
        assertEquals(241, longExtendedAttribute.getExtendedType());
        assertEquals(256, longExtendedAttribute.getData().getValue());
    }

    @Test
    @DisplayName("Long extended attribute in a request packet is encoded successfully")
    void encodeRequestLongExtendedAttribute() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(42);

        String combined = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "ccc";

        Packet requestPacket = new Packet(1, List.of(
                new NasIdentifier(new TextData("00a1b2c3d4")),
                new SamlAssertion(new TextData(combined))));

        byte[] actual = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        String expected = "012a0225f58c0714b19ce47b2e4976e62dd7d6fc200c30306131623263336434" +
                "f5ff018061616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "61616161616161616161616161616161616161616161616161616161616161f5" +
                "ff01806262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "626262626262626262626262626262626262626262626262626262626262f507" +
                "0100636363";

        assertEquals(expected, toHex(actual));
    }

    @Test
    @DisplayName("Long extended attribute in a request packet is decoded successfully")
    void decodeRequestLongExtendedAttribute() throws PacketCodecException {
        byte[] encoded = fromHex("012a0225f58c0714b19ce47b2e4976e62dd7d6fc200c30306131623263336434" +
                "f5ff018061616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "61616161616161616161616161616161616161616161616161616161616161f5" +
                "ff01806262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "626262626262626262626262626262626262626262626262626262626262f507" +
                "0100636363");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertThat(requestPacket.getAttributes().get(0), instanceOf(NasIdentifier.class));
        NasIdentifier nasIdentifier = (NasIdentifier) requestPacket.getAttributes().get(0);
        assertEquals("00a1b2c3d4", nasIdentifier.getData().getValue());

        String combined = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "ccc";

        assertThat(requestPacket.getAttributes().get(1), instanceOf(SamlAssertion.class));
        SamlAssertion samlAssertion = (SamlAssertion) requestPacket.getAttributes().get(1);
        assertEquals(combined, samlAssertion.getData().getValue());
    }

    @Test
    @DisplayName("Long extended attribute in a response packet is encoded successfully")
    void encodeResponseLongExtendedAttribute() throws PacketCodecException {
        String combined = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "ccc";

        Packet responsePacket = new Packet(2, List.of(
                new SamlAssertion(new TextData(combined))));

        byte[] actual = packetCodec.encodeResponse(responsePacket, "abc123".getBytes(US_ASCII), 42,
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        String expected = "022a021965a83c683eb8a5ed13201c838d31c7a2f5ff01806161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "61616161616161616161616161616161616161f5ff0180626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "626262626262626262626262626262626262f5070100636363";

        assertEquals(expected, toHex(actual));
    }

    @Test
    @DisplayName("Long extended attribute in a response packet is decoded successfully")
    void decodeResponseLongExtendedAttribute() throws PacketCodecException {
        String combined = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "ccc";

        byte[] encoded = fromHex("022a021965a83c683eb8a5ed13201c838d31c7a2f5ff01806161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "6161616161616161616161616161616161616161616161616161616161616161" +
                "61616161616161616161616161616161616161f5ff0180626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "6262626262626262626262626262626262626262626262626262626262626262" +
                "626262626262626262626262626262626262f5070100636363");

        Packet requestPacket = packetCodec.decodeResponse(encoded, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertThat(requestPacket.getAttributes().get(0), instanceOf(SamlAssertion.class));
        SamlAssertion samlAssertion = (SamlAssertion) requestPacket.getAttributes().get(0);
        assertEquals(combined, samlAssertion.getData().getValue());
    }

}