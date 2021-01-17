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

package org.aaa4j.radius.core.packet;

import org.aaa4j.radius.core.attribute.AttributeType;
import org.aaa4j.radius.core.attribute.RawAttribute;
import org.aaa4j.radius.core.dictionary.AttributeDefinition;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.PacketDefinition;
import org.aaa4j.radius.core.dictionary.TlvDefinition;
import org.aaa4j.radius.core.util.RandomProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@DisplayName("PacketCodec")
class PacketCodecTest {

    private PacketIdGenerator mockedPacketIdGenerator;

    private RandomProvider mockedRandomProvider;

    private PacketCodec packetCodec;

    @BeforeEach
    void setUp() {
        mockedPacketIdGenerator = mock(PacketIdGenerator.class);
        mockedRandomProvider = mock(RandomProvider.class);

        packetCodec = new PacketCodec(new EmptyDictionary(), mockedRandomProvider, mockedPacketIdGenerator);
    }

    @AfterEach
    void tearDown() {
        reset(mockedPacketIdGenerator);
        reset(mockedRandomProvider);
    }

    private static class EmptyDictionary implements Dictionary {

        @Override
        public PacketDefinition getPacketDefinition(int code) {
            return null;
        }

        @Override
        public AttributeDefinition getAttributeDefinition(AttributeType type) {
            return null;
        }

        @Override
        public TlvDefinition getTlvDefinition(AttributeType type) {
            return null;
        }

    }

    @Nested
    @DisplayName("Simple packet encoding and decoding")
    class SimplePackets {

        @Test
        @DisplayName("Simple request packet is encoded successfully")
        void encodeRequest() throws PacketCodecException {
            when(mockedPacketIdGenerator.nextId()).thenReturn(42);

            Packet requestPacket = new Packet(1, List.of(
                    new RawAttribute(1, "jdoe".getBytes(UTF_8)),
                    new RawAttribute(32, "00a1b2c3d4".getBytes(UTF_8))));

            byte[] actual = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                    fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

            assertEquals("012a0026f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434",
                    toHex(actual));
        }

        @Test
        @DisplayName("Simple response packet is encoded successfully")
        void encodeResponse() throws PacketCodecException {
            Packet requestPacket = new Packet(2, List.of(
                    new RawAttribute(11, "Administrator".getBytes(UTF_8))));

            byte[] actual = packetCodec.encodeResponse(requestPacket, "abc123".getBytes(US_ASCII), 42,
                    fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

            assertEquals("022a0023a4e5b10fcd0de0818d9af33c945a50230b0f41646d696e6973747261746f72",
                    toHex(actual));
        }

        @Test
        @DisplayName("Simple request packet is decoded successfully")
        void decodeRequest() throws PacketCodecException {
            byte[] encoded = fromHex("012a0026f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434");

            Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

            assertEquals(1, requestPacket.getCode());
            assertEquals(42, requestPacket.getReceivedFields().getIdentifier());
            assertEquals("f58c0714b19ce47b2e4976e62dd7d6fc",
                    toHex(requestPacket.getReceivedFields().getAuthenticator()));
            assertEquals(2, requestPacket.getAttributes().size());
            assertEquals(new RawAttribute(1, "jdoe".getBytes(UTF_8)),
                    requestPacket.getAttributes().get(0));
            assertEquals(new RawAttribute(32, "00a1b2c3d4".getBytes(UTF_8)),
                    requestPacket.getAttributes().get(1));
        }

        @Test
        @DisplayName("Simple response packet is decoded successfully")
        void decodeResponse() throws PacketCodecException {
            byte[] encoded = fromHex("022a0023a4e5b10fcd0de0818d9af33c945a50230b0f41646d696e6973747261746f72");

            Packet responsePacket = packetCodec.decodeResponse(encoded,
                    "abc123".getBytes(US_ASCII), fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

            assertEquals(2, responsePacket.getCode());
            assertEquals(42, responsePacket.getReceivedFields().getIdentifier());
            assertEquals("a4e5b10fcd0de0818d9af33c945a5023",
                    toHex(responsePacket.getReceivedFields().getAuthenticator()));
            assertEquals(1, responsePacket.getAttributes().size());
            assertEquals(new RawAttribute(11, "Administrator".getBytes(UTF_8)),
                    responsePacket.getAttributes().get(0));
        }

    }

    @Nested
    @DisplayName("Malformed packets")
    class MalformedPackets {

        @Test
        @DisplayName("Decoding request packet with length smaller than actual length throws codec exception")
        void requestPacketLengthTooShortThrows() {
            byte[] encoded = fromHex("012a0026f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434");

            // Set encoded length 1 less than actual length
            encoded[2] = 0x00;
            encoded[3] = 0x25;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));
            });
        }

        @Test
        @DisplayName("Decoding request packet with length greater than actual length throws codec exception")
        void requestPacketLengthTooLongThrows() {
            byte[] encoded = fromHex("012a0026f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434");

            // Set encoded length 1 greater than actual length
            encoded[2] = 0x00;
            encoded[3] = 0x27;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));
            });
        }

        @Test
        @DisplayName("Decoding response packet with length smaller than actual length throws codec exception")
        void responsePacketLengthTooShortThrows() {
            byte[] encoded = fromHex("022a0023a4e5b10fcd0de0818d9af33c945a50230b0f41646d696e6973747261746f72");

            // Set encoded length 1 less than actual length
            encoded[2] = 0x00;
            encoded[3] = 0x22;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeResponse(encoded, "abc123".getBytes(US_ASCII),
                        fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));
            });
        }

        @Test
        @DisplayName("Decoding response packet with length greater than actual length throws codec exception")
        void responsePacketLengthTooLongThrows() {
            byte[] encoded = fromHex("022a0023a4e5b10fcd0de0818d9af33c945a50230b0f41646d696e6973747261746f72");

            // Set encoded length 1 greater than actual length
            encoded[2] = 0x00;
            encoded[3] = 0x24;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeResponse(encoded, "abc123".getBytes(US_ASCII),
                        fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));
            });
        }

        @Test
        @DisplayName("Decoding request packet with attribute length smaller than actual length throws codec exception")
        void requestAttributeLengthTooShortThrows() {
            byte[] encoded = fromHex("012a0026f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434");

            // Set NAS-Identifier attribute length to 1
            encoded[27] = 0x01;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));
            });
        }

        @Test
        @DisplayName("Decoding request packet with attribute length greater than actual length throws codec exception")
        void requestAttributeLengthTooLongThrows() {
            byte[] encoded = fromHex("012a0026f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434");

            // Set NAS-Identifier attribute length 1 greater than actual length
            encoded[27] = 0x0d;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));
            });
        }

        @Test
        @DisplayName("Decoding response packet with attribute length smaller than actual length throws codec exception")
        void responseAttributeLengthTooShortThrows() {
            byte[] encoded = fromHex("022a0023a4e5b10fcd0de0818d9af33c945a50230b0f41646d696e6973747261746f72");

            // Set NAS-Identifier attribute length to 1
            encoded[21] = 0x01;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeResponse(encoded, "abc123".getBytes(US_ASCII),
                        fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));
            });
        }

        @Test
        @DisplayName("Decoding response packet with attribute length greater than actual length throws codec exception")
        void responseAttributeLengthTooLongThrows() {
            byte[] encoded = fromHex("022a0023a4e5b10fcd0de0818d9af33c945a50230b0f41646d696e6973747261746f72");

            // Set NAS-Identifier attribute length 1 greater than actual length
            encoded[21] = 0x10;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeResponse(encoded, "abc123".getBytes(US_ASCII),
                        fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));
            });
        }

    }

    @Nested
    @DisplayName("Response packet authenticator")
    class ResponsePacketAuthenticator {

        @Test
        @DisplayName("Decoding response packet with invalid authenticator throws codec exception")
        void responseInvalidAuthenticatorThrows() {
            byte[] encoded = fromHex("022a0023a4e5b10fcd0de0818d9af33c945a50230b0f41646d696e6973747261746f72");

            // Mangle the response authenticator
            encoded[5] = 0x00;
            encoded[6] = 0x00;
            encoded[7] = 0x00;
            encoded[8] = 0x00;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeResponse(encoded, "abc123".getBytes(US_ASCII),
                        fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));
            });
        }

    }

    @Nested
    @DisplayName("Message authenticator")
    class MessageAuthenticator {

        @Test
        @DisplayName("Request packet with a message authenticator is encoded successfully")
        void encodeRequestWithMessageAuthenticator() throws PacketCodecException {
            when(mockedPacketIdGenerator.nextId()).thenReturn(42);

            Packet requestPacket = new Packet(1, List.of(
                    new RawAttribute(1, "jdoe".getBytes(UTF_8)),
                    new RawAttribute(32, "00a1b2c3d4".getBytes(UTF_8)),
                    new RawAttribute(80, new byte[16])));

            byte[] actual = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                    fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

            assertEquals("012a0038f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434" +
                    "5012dd82a2f526c29b98961566bb6b284e0d",
                    toHex(actual));
        }

        @Test
        @DisplayName("Request packet with a message authenticator is decoded successfully")
        void decodeRequestWithMessageAuthenticator() throws PacketCodecException {
            byte[] encoded = fromHex("012a0038f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434" +
                    "5012dd82a2f526c29b98961566bb6b284e0d");

            Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

            assertEquals(3, requestPacket.getAttributes().size());

            assertEquals(new RawAttribute(80, fromHex("dd82a2f526c29b98961566bb6b284e0d")),
                    requestPacket.getAttributes().get(2));
        }

        @Test
        @DisplayName("Response packet with a message authenticator is encoded successfully")
        void encodeResponseWithMessageAuthenticator() throws PacketCodecException {
            Packet requestPacket = new Packet(2, List.of(
                    new RawAttribute(11, "Administrator".getBytes(UTF_8)),
                    new RawAttribute(80, new byte[16])));

            byte[] actual = packetCodec.encodeResponse(requestPacket, "abc123".getBytes(US_ASCII), 42,
                    fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

            assertEquals("022a003539bbf61c668c400d60fb501ce792cc620b0f41646d696e6973747261746f72" +
                    "50123e5fb5763904507ab322e0121088aadf",
                    toHex(actual));
        }

        @Test
        @DisplayName("Response packet with a message authenticator is decoded successfully")
        void decodeResponseWithMessageAuthenticator() throws PacketCodecException {
            byte[] encoded = fromHex("022a003539bbf61c668c400d60fb501ce792cc620b0f41646d696e6973747261746f72" +
                    "50123e5fb5763904507ab322e0121088aadf");

            Packet requestPacket = packetCodec.decodeResponse(encoded, "abc123".getBytes(US_ASCII),
                    fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

            assertEquals(2, requestPacket.getAttributes().size());

            assertEquals(new RawAttribute(80, fromHex("3e5fb5763904507ab322e0121088aadf")),
                    requestPacket.getAttributes().get(1));
        }

        @Test
        @DisplayName("Decoding request packet with invalid message authenticator throws codec exception")
        void decodeRequestWithInvalidMessageAuthenticatorThrows() {
            byte[] encoded = fromHex("012a0038f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434" +
                    "5012dd82a2f526c29b98961566bb6b284e0d");

            // Mangle the message authenticator
            encoded[40] = 0x00;
            encoded[41] = 0x00;
            encoded[42] = 0x00;
            encoded[43] = 0x00;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));
            });
        }

        @Test
        @DisplayName("Decoding response packet with invalid message authenticator throws codec exception")
        void decodeResponseWithInvalidMessageAuthenticatorThrows() {
            byte[] encoded = fromHex("022a003539bbf61c668c400d60fb501ce792cc620b0f41646d696e6973747261746f72" +
                    "50123e5fb5763904507ab322e0121088aadf");

            // Mangle the message authenticator
            encoded[40] = 0x00;
            encoded[41] = 0x00;
            encoded[42] = 0x00;
            encoded[43] = 0x00;

            assertThrows(PacketCodecException.class, () -> {
                packetCodec.decodeResponse(encoded, "abc123".getBytes(US_ASCII),
                        fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));
            });
        }

    }

}
