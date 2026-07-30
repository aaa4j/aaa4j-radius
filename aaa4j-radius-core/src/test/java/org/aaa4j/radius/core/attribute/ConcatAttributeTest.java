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

import org.aaa4j.radius.core.attribute.attributes.EapMessage;
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

@DisplayName("ConcatAttribute")
class ConcatAttributeTest {

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
    @DisplayName("Concat attribute composed of a single fragment in a request packet is encoded successfully")
    void encodeRequestConcatAttributeSingleFragment() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        // 253 bytes
        byte[] data = fromHex("ce41e2d8485bf5022b7226da96d6ee0b358af2f540239f8a24cb330287717ad6" +
                "f16e893845a2a9a3a46f5ff416c5191e825b43b2740498db2c05697b9ee8c3a7" +
                "4aa6f590e38eb3ab755f934f6845adcc8aca56de4f3c9af0b276c461de4f170e" +
                "fd73a6874018f8775413fcde72837acb26cd0b2bdc6977d6e67de2ed7aa216e6" +
                "419751f1e528b43f1c516432d816d6a6b1a8d475e393d6ad74c6b5437bf51bd8" +
                "63fc38fa62a3f60dee03610aca3df9015dc56abb70ef05ef84045d361c05ca75" +
                "7b60c2a33ed14fc1bbd488abda36ac49fe79942891f8cf27cedcb9e2212d8304" +
                "46af7bcfe50c5545366cfff52e036a6d95f36e004f9c41b1ec23f4f994");

        Packet requestPacket = new Packet(1, List.of(
                new EapMessage(new ConcatData(data))));

        byte[] actual = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertEquals("01050113f58c0714b19ce47b2e4976e62dd7d6fc4fffce41e2d8485bf5022b72" +
                "26da96d6ee0b358af2f540239f8a24cb330287717ad6f16e893845a2a9a3a46f" +
                "5ff416c5191e825b43b2740498db2c05697b9ee8c3a74aa6f590e38eb3ab755f" +
                "934f6845adcc8aca56de4f3c9af0b276c461de4f170efd73a6874018f8775413" +
                "fcde72837acb26cd0b2bdc6977d6e67de2ed7aa216e6419751f1e528b43f1c51" +
                "6432d816d6a6b1a8d475e393d6ad74c6b5437bf51bd863fc38fa62a3f60dee03" +
                "610aca3df9015dc56abb70ef05ef84045d361c05ca757b60c2a33ed14fc1bbd4" +
                "88abda36ac49fe79942891f8cf27cedcb9e2212d830446af7bcfe50c5545366c" +
                "fff52e036a6d95f36e004f9c41b1ec23f4f994",
                toHex(actual));
    }

    @Test
    @DisplayName("Concat attribute composed of a single fragment in a request packet is decoded successfully")
    void decodeRequestConcatAttributeSingleFragment() throws PacketCodecException {
        byte[] encoded = fromHex("01050113f58c0714b19ce47b2e4976e62dd7d6fc4fffce41e2d8485bf5022b72" +
                "26da96d6ee0b358af2f540239f8a24cb330287717ad6f16e893845a2a9a3a46f" +
                "5ff416c5191e825b43b2740498db2c05697b9ee8c3a74aa6f590e38eb3ab755f" +
                "934f6845adcc8aca56de4f3c9af0b276c461de4f170efd73a6874018f8775413" +
                "fcde72837acb26cd0b2bdc6977d6e67de2ed7aa216e6419751f1e528b43f1c51" +
                "6432d816d6a6b1a8d475e393d6ad74c6b5437bf51bd863fc38fa62a3f60dee03" +
                "610aca3df9015dc56abb70ef05ef84045d361c05ca757b60c2a33ed14fc1bbd4" +
                "88abda36ac49fe79942891f8cf27cedcb9e2212d830446af7bcfe50c5545366c" +
                "fff52e036a6d95f36e004f9c41b1ec23f4f994");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertEquals(1, requestPacket.getAttributes().size());

        assertThat(requestPacket.getAttributes().get(0), instanceOf(EapMessage.class));
        EapMessage eapMessage = (EapMessage) requestPacket.getAttributes().get(0);

        String expected = "ce41e2d8485bf5022b7226da96d6ee0b358af2f540239f8a24cb330287717ad6" +
                "f16e893845a2a9a3a46f5ff416c5191e825b43b2740498db2c05697b9ee8c3a7" +
                "4aa6f590e38eb3ab755f934f6845adcc8aca56de4f3c9af0b276c461de4f170e" +
                "fd73a6874018f8775413fcde72837acb26cd0b2bdc6977d6e67de2ed7aa216e6" +
                "419751f1e528b43f1c516432d816d6a6b1a8d475e393d6ad74c6b5437bf51bd8" +
                "63fc38fa62a3f60dee03610aca3df9015dc56abb70ef05ef84045d361c05ca75" +
                "7b60c2a33ed14fc1bbd488abda36ac49fe79942891f8cf27cedcb9e2212d8304" +
                "46af7bcfe50c5545366cfff52e036a6d95f36e004f9c41b1ec23f4f994";

        assertEquals(expected, toHex(eapMessage.getData().getData()));
    }

    @Test
    @DisplayName("Concat attribute composed of multiple fragments in a request packet is encoded successfully")
    void encodeRequestConcatAttributeMultipleFragments() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        // 259 bytes
        byte[] data = fromHex("ce41e2d8485bf5022b7226da96d6ee0b358af2f540239f8a24cb330287717ad6" +
                "f16e893845a2a9a3a46f5ff416c5191e825b43b2740498db2c05697b9ee8c3a7" +
                "4aa6f590e38eb3ab755f934f6845adcc8aca56de4f3c9af0b276c461de4f170e" +
                "fd73a6874018f8775413fcde72837acb26cd0b2bdc6977d6e67de2ed7aa216e6" +
                "419751f1e528b43f1c516432d816d6a6b1a8d475e393d6ad74c6b5437bf51bd8" +
                "63fc38fa62a3f60dee03610aca3df9015dc56abb70ef05ef84045d361c05ca75" +
                "7b60c2a33ed14fc1bbd488abda36ac49fe79942891f8cf27cedcb9e2212d8304" +
                "46af7bcfe50c5545366cfff52e036a6d95f36e004f9c41b1ec23f4f994" +
                "aabbccddeeff");

        Packet requestPacket = new Packet(1, List.of(
                new EapMessage(new ConcatData(data))));

        byte[] actual = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertEquals("0105011bf58c0714b19ce47b2e4976e62dd7d6fc4fffce41e2d8485bf5022b72" +
                "26da96d6ee0b358af2f540239f8a24cb330287717ad6f16e893845a2a9a3a46f" +
                "5ff416c5191e825b43b2740498db2c05697b9ee8c3a74aa6f590e38eb3ab755f" +
                "934f6845adcc8aca56de4f3c9af0b276c461de4f170efd73a6874018f8775413" +
                "fcde72837acb26cd0b2bdc6977d6e67de2ed7aa216e6419751f1e528b43f1c51" +
                "6432d816d6a6b1a8d475e393d6ad74c6b5437bf51bd863fc38fa62a3f60dee03" +
                "610aca3df9015dc56abb70ef05ef84045d361c05ca757b60c2a33ed14fc1bbd4" +
                "88abda36ac49fe79942891f8cf27cedcb9e2212d830446af7bcfe50c5545366c" +
                "fff52e036a6d95f36e004f9c41b1ec23f4f9944f08aabbccddeeff",
                toHex(actual));
    }

    @Test
    @DisplayName("Concat attribute composed of multiple fragments in a request packet is decoded successfully")
    void decodeRequestConcatAttributeMultipleFragments() throws PacketCodecException {
        byte[] encoded = fromHex("0105011bf58c0714b19ce47b2e4976e62dd7d6fc4fffce41e2d8485bf5022b72" +
                "26da96d6ee0b358af2f540239f8a24cb330287717ad6f16e893845a2a9a3a46f" +
                "5ff416c5191e825b43b2740498db2c05697b9ee8c3a74aa6f590e38eb3ab755f" +
                "934f6845adcc8aca56de4f3c9af0b276c461de4f170efd73a6874018f8775413" +
                "fcde72837acb26cd0b2bdc6977d6e67de2ed7aa216e6419751f1e528b43f1c51" +
                "6432d816d6a6b1a8d475e393d6ad74c6b5437bf51bd863fc38fa62a3f60dee03" +
                "610aca3df9015dc56abb70ef05ef84045d361c05ca757b60c2a33ed14fc1bbd4" +
                "88abda36ac49fe79942891f8cf27cedcb9e2212d830446af7bcfe50c5545366c" +
                "fff52e036a6d95f36e004f9c41b1ec23f4f9944f08aabbccddeeff");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertEquals(1, requestPacket.getAttributes().size());

        assertThat(requestPacket.getAttributes().get(0), instanceOf(EapMessage.class));
        EapMessage eapMessage = (EapMessage) requestPacket.getAttributes().get(0);

        String expected = "ce41e2d8485bf5022b7226da96d6ee0b358af2f540239f8a24cb330287717ad6" +
                "f16e893845a2a9a3a46f5ff416c5191e825b43b2740498db2c05697b9ee8c3a7" +
                "4aa6f590e38eb3ab755f934f6845adcc8aca56de4f3c9af0b276c461de4f170e" +
                "fd73a6874018f8775413fcde72837acb26cd0b2bdc6977d6e67de2ed7aa216e6" +
                "419751f1e528b43f1c516432d816d6a6b1a8d475e393d6ad74c6b5437bf51bd8" +
                "63fc38fa62a3f60dee03610aca3df9015dc56abb70ef05ef84045d361c05ca75" +
                "7b60c2a33ed14fc1bbd488abda36ac49fe79942891f8cf27cedcb9e2212d8304" +
                "46af7bcfe50c5545366cfff52e036a6d95f36e004f9c41b1ec23f4f994" +
                "aabbccddeeff";

        assertEquals(expected, toHex(eapMessage.getData().getData()));
    }

    @Test
    @DisplayName("Concat attribute with a zero-length fragment is left undecoded")
    void decodeRequestConcatAttributeZeroLengthFragment() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        {
            // EAP-Message (79) with no data
            byte[] encoded = fromHex("01050016f58c0714b19ce47b2e4976e62dd7d6fc4f02");

            Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

            assertEquals(1, requestPacket.getAttributes().size());
            assertThat(requestPacket.getAttributes().get(0), instanceOf(RawAttribute.class));

            // The bytes survive a round trip exactly as they arrived
            byte[] reencoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                    requestPacket.getReceivedFields().getAuthenticator());

            assertEquals(toHex(encoded), toHex(reencoded));
        }
        {
            // A valid fragment followed by a zero-length one: the whole run is left undecoded
            byte[] encoded = fromHex("01050019f58c0714b19ce47b2e4976e62dd7d6fc4f03aa4f02");

            Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

            assertEquals(2, requestPacket.getAttributes().size());
            assertThat(requestPacket.getAttributes().get(0), instanceOf(RawAttribute.class));
            assertThat(requestPacket.getAttributes().get(1), instanceOf(RawAttribute.class));

            byte[] reencoded = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                    requestPacket.getReceivedFields().getAuthenticator());

            assertEquals(toHex(encoded), toHex(reencoded));
        }
    }

    @Test
    @DisplayName("Concat attribute filling the packet to the maximum length is encoded successfully")
    void encodeRequestConcatAttributeAtMaximumPacketLength() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        // 4044 bytes fragment into 15 * 253 + 249, so the attributes take 16 * 2 + 4044 = 4076 bytes and the packet
        // is exactly the 4096 byte maximum
        Packet requestPacket = new Packet(1, List.of(new EapMessage(new ConcatData(new byte[4044]))));

        byte[] actual = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertEquals(4096, actual.length);
    }

    @Test
    @DisplayName("Concat attribute exceeding the maximum packet length throws codec exception")
    void encodeRequestConcatAttributeExceedingMaximumPacketLength() {
        when(mockedPacketIdGenerator.nextId()).thenReturn(5);

        // One byte more than the case above needs a 4097 byte packet, which RFC 2865 forbids and which would
        // eventually silently wrap the two byte length field
        Packet requestPacket = new Packet(1, List.of(new EapMessage(new ConcatData(new byte[4045]))));

        assertThrows(PacketCodecException.class, () -> {
            packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                    fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));
        });
    }

    @Test
    @DisplayName("Concat attribute exceeding the maximum packet length in a response packet throws codec exception")
    void encodeResponseConcatAttributeExceedingMaximumPacketLength() {
        // Same arithmetic as the request-side guard; this pins the guard in encodeResponse
        Packet responsePacket = new Packet(2, List.of(new EapMessage(new ConcatData(new byte[4045]))));

        assertThrows(PacketCodecException.class, () -> {
            packetCodec.encodeResponse(responsePacket, "abc123".getBytes(US_ASCII), 5,
                    fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));
        });
    }

}
