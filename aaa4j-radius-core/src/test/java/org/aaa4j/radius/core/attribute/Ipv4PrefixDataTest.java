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

@DisplayName("Ipv4PrefixData")
class Ipv4PrefixDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new Ipv4PrefixData(0, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Ipv4PrefixData(33, (Inet4Address) Inet4Address.getByName("192.168.10.0"));
        });
    }

    @Test
    @DisplayName("length method returns the correct length")
    void testLength() throws UnknownHostException {
        Ipv4PrefixData ipv4PrefixData = new Ipv4PrefixData(24, (Inet4Address) Inet4Address.getByName("192.168.10.0"));

        assertEquals(6, ipv4PrefixData.length());
    }

    @Test
    @DisplayName("getPrefixLength method returns the correct value")
    void testGetPrefixLength() throws UnknownHostException {
        Ipv4PrefixData ipv4PrefixData = new Ipv4PrefixData(24, (Inet4Address) Inet4Address.getByName("192.168.10.0"));

        assertEquals(24, ipv4PrefixData.getPrefixLength());
    }

    @Test
    @DisplayName("getAddress method returns the correct value")
    void testGetAddress() throws UnknownHostException {
        {
            Ipv4PrefixData ipv4PrefixData = new Ipv4PrefixData(24,
                    (Inet4Address) Inet4Address.getByName("192.168.10.0"));

            assertEquals(Inet4Address.getByName("192.168.10.0"), ipv4PrefixData.getAddress());
        }
        {
            Ipv4PrefixData ipv4PrefixData = new Ipv4PrefixData(24,
                    (Inet4Address) Inet4Address.getByName("192.168.10.255"));

            assertEquals(Inet4Address.getByName("192.168.10.0"), ipv4PrefixData.getAddress());
        }
    }

    @Test
    @DisplayName("ipv4prefix data is decoded successfully")
    void testDecode() throws UnknownHostException {
        {
            byte[] encoded = fromHex("0018c0a80a00");

            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(ipv4PrefixData);
            assertEquals(24, ipv4PrefixData.getPrefixLength());
            assertEquals(Inet4Address.getByName("192.168.10.0"), ipv4PrefixData.getAddress());
        }
        {
            byte[] encoded = fromHex("0018c0a80aff");

            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(ipv4PrefixData);
            assertEquals(24, ipv4PrefixData.getPrefixLength());
            assertEquals(Inet4Address.getByName("192.168.10.0"), ipv4PrefixData.getAddress());
        }
    }

    @Test
    @DisplayName("ipv4prefix data is encoded successfully")
    void testEncode() throws UnknownHostException {
        Ipv4PrefixData ipv4PrefixData = new Ipv4PrefixData(24, (Inet4Address) Inet4Address.getByName("192.168.10.0"));
        byte[] encoded = Ipv4PrefixData.Codec.INSTANCE.encode(null, ipv4PrefixData);

        assertEquals("0018c0a80a00", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid ipv4prefix data is decoded into null")
    void testDecodeInvalid() {
        {
            byte[] encoded = fromHex("0018c0a80a");
            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv4PrefixData);
        }
        {
            byte[] encoded = fromHex("0051c0a80a00");
            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv4PrefixData);
        }
        {
            byte[] encoded = fromHex("0018c0a80a0000");
            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv4PrefixData);
        }
    }

}