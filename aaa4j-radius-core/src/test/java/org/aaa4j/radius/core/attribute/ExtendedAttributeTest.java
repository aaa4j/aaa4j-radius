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

import org.aaa4j.radius.core.attribute.attributes.FragStatus;
import org.aaa4j.radius.core.attribute.attributes.NasIdentifier;
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

@DisplayName("ExtendedAttribute")
class ExtendedAttributeTest {

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
            new ExtendedAttribute<>(241, 1, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ExtendedAttribute<>(-1, 1, new IntegerData(256));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ExtendedAttribute<>(256, 1, new IntegerData(256));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ExtendedAttribute<>(241, -1, new IntegerData(256));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ExtendedAttribute<>(241, 256, new IntegerData(256));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        ExtendedAttribute<IntegerData> extendedAttribute = new ExtendedAttribute<>(241, 242, new IntegerData(256));

        assertEquals(new AttributeType(241, 242), extendedAttribute.getType());
        assertEquals(242, extendedAttribute.getExtendedType());
        assertEquals(256, extendedAttribute.getData().getValue());
    }

    @Test
    @DisplayName("Extended attribute in a request packet is encoded successfully")
    void encodeRequestExtendedAttribute() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(42);

        Packet requestPacket = new Packet(1, List.of(
                new NasIdentifier(new TextData("00a1b2c3d4")),
                new FragStatus(new IntegerData(256))));

        byte[] actual = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertEquals("012a0027f58c0714b19ce47b2e4976e62dd7d6fc200c30306131623263336434f1070100000100",
                toHex(actual));
    }

    @Test
    @DisplayName("Extended attribute in a request packet is decoded successfully")
    void decodeRequestExtendedAttribute() throws PacketCodecException {
        byte[] encoded = fromHex("012a0027f58c0714b19ce47b2e4976e62dd7d6fc200c30306131623263336434f1070100000100");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertThat(requestPacket.getAttributes().get(0), instanceOf(NasIdentifier.class));
        NasIdentifier nasIdentifier = (NasIdentifier) requestPacket.getAttributes().get(0);
        assertEquals("00a1b2c3d4", nasIdentifier.getData().getValue());

        assertThat(requestPacket.getAttributes().get(1), instanceOf(FragStatus.class));
        FragStatus fragStatus = (FragStatus) requestPacket.getAttributes().get(1);
        assertEquals(256, fragStatus.getData().getValue());
    }

    @Test
    @DisplayName("Extended attribute in a response packet is encoded successfully")
    void encodeResponseExtendedAttribute() throws PacketCodecException {
        Packet requestPacket = new Packet(2, List.of(new FragStatus(new IntegerData(1024))));

        byte[] actual = packetCodec.encodeResponse(requestPacket, "abc123".getBytes(US_ASCII), 42,
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertEquals("022a001b9b4299a3622bd66afc97eb3a1153d018f1070100000400",
                toHex(actual));
    }

    @Test
    @DisplayName("Extended attribute in a response packet is decoded successfully")
    void decodeResponseExtendedAttribute() throws PacketCodecException {
        byte[] encoded = fromHex("022a001b9b4299a3622bd66afc97eb3a1153d018f1070100000400");

        Packet requestPacket = packetCodec.decodeResponse(encoded, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertThat(requestPacket.getAttributes().get(0), instanceOf(FragStatus.class));
        FragStatus fragStatus = (FragStatus) requestPacket.getAttributes().get(0);
        assertEquals(1024, fragStatus.getData().getValue());
    }

}