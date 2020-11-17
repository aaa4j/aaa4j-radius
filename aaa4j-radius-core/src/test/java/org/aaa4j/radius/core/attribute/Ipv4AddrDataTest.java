package org.aaa4j.radius.core.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Ipv4AddrData")
class Ipv4AddrDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new Ipv4AddrData(null);
        });
    }

    @Test
    @DisplayName("Methods returns the correct values")
    void testMethods() throws UnknownHostException {
        Ipv4AddrData ipv4AddrData = new Ipv4AddrData((Inet4Address) Inet4Address.getByName("192.168.10.0"));

        assertEquals(4, ipv4AddrData.length());
        assertEquals(Inet4Address.getByName("192.168.10.0"), ipv4AddrData.getValue());
    }

    @Test
    @DisplayName("ipv4addr data is decoded successfully")
    void testDecode() throws UnknownHostException {
        byte[] encoded = fromHex("c0a80a00");

        Ipv4AddrData ipv4AddrData = Ipv4AddrData.Codec.INSTANCE.decode(null, encoded);

        assertNotNull(ipv4AddrData);
        assertEquals(Inet4Address.getByName("192.168.10.0"), ipv4AddrData.getValue());
    }

    @Test
    @DisplayName("ipv4addr data is encoded successfully")
    void testEncode() throws UnknownHostException {
        Ipv4AddrData ipv4AddrData = new Ipv4AddrData((Inet4Address) Inet4Address.getByName("192.168.10.0"));
        byte[] encoded = Ipv4AddrData.Codec.INSTANCE.encode(null, ipv4AddrData);

        assertEquals("c0a80a00", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid ipv4addr data is decoded into null")
    void testDecodeInvalidLength() {
        {
            // Not few bytes
            byte[] encoded = fromHex("");
            Ipv4AddrData ipv4AddrData = Ipv4AddrData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv4AddrData);
        }
        {
            // Too many bytes
            byte[] encoded = fromHex("c0a80a0000");
            Ipv4AddrData ipv4AddrData = Ipv4AddrData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv4AddrData);
        }
    }

}