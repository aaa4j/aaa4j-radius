package org.aaa4j.radius.core.attribute;

import org.aaa4j.radius.core.attribute.attributes.FilterId;
import org.aaa4j.radius.core.attribute.attributes.NasIdentifier;
import org.aaa4j.radius.core.attribute.attributes.UserName;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@DisplayName("StandardAttribute")
class StandardAttributeTest {

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
    @DisplayName("Standard attribute in a request packet is encoded successfully")
    void encodeRequestStandardAttribute() throws PacketCodecException {
        when(mockedPacketIdGenerator.nextId()).thenReturn(42);

        Packet requestPacket = new Packet(1, List.of(
                new UserName(new TextData("jdoe")),
                new NasIdentifier(new TextData("00a1b2c3d4"))));

        byte[] actual = packetCodec.encodeRequest(requestPacket, "abc123".getBytes(US_ASCII),
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertEquals("012a0026f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434",
                toHex(actual));
    }

    @Test
    @DisplayName("Standard attribute in a request packet is decoded successfully")
    void decodeRequestStandardAttribute() throws PacketCodecException {
        byte[] encoded = fromHex("012a0026f58c0714b19ce47b2e4976e62dd7d6fc01066a646f65200c30306131623263336434");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertThat(requestPacket.getAttributes().get(0), instanceOf(UserName.class));
        UserName userName = (UserName) requestPacket.getAttributes().get(0);
        assertEquals("jdoe", userName.getData().getValue());

        assertThat(requestPacket.getAttributes().get(1), instanceOf(NasIdentifier.class));
        NasIdentifier nasIdentifier = (NasIdentifier) requestPacket.getAttributes().get(1);
        assertEquals("00a1b2c3d4", nasIdentifier.getData().getValue());
    }

    @Test
    @DisplayName("Standard attribute in a response packet is encoded successfully")
    void encodeResponseStandardAttribute() throws PacketCodecException {
        Packet requestPacket = new Packet(2, List.of(
                new FilterId(new TextData("Administrator"))));

        byte[] actual = packetCodec.encodeResponse(requestPacket, "abc123".getBytes(US_ASCII), 42,
                fromHex("f58c0714b19ce47b2e4976e62dd7d6fc"));

        assertEquals("022a0023a4e5b10fcd0de0818d9af33c945a50230b0f41646d696e6973747261746f72",
                toHex(actual));
    }

    @Test
    @DisplayName("Standard attribute in a response packet is decoded successfully")
    void decodeResponseStandardAttribute() throws PacketCodecException {
        byte[] encoded = fromHex("022a0023a4e5b10fcd0de0818d9af33c945a50230b0f41646d696e6973747261746f72");

        Packet requestPacket = packetCodec.decodeRequest(encoded, "abc123".getBytes(US_ASCII));

        assertThat(requestPacket.getAttributes().get(0), instanceOf(FilterId.class));
        FilterId filterId = (FilterId) requestPacket.getAttributes().get(0);
        assertEquals("Administrator", filterId.getData().getValue());
    }

}