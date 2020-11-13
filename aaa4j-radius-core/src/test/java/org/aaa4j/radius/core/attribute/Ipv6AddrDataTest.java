package org.aaa4j.radius.core.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.Inet6Address;
import java.net.UnknownHostException;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Ipv6AddrData")
class Ipv6AddrDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new Ipv6AddrData(null);
        });
    }

    @Test
    @DisplayName("length method returns the correct length")
    void testLength() throws UnknownHostException {
        Ipv6AddrData ipv6AddrData = new Ipv6AddrData((Inet6Address) Inet6Address.getByName("2001:db8::2a"));

        assertEquals(16, ipv6AddrData.length());
    }

    @Test
    @DisplayName("getValue method returns the correct value")
    void testGetValue() throws UnknownHostException {
        Ipv6AddrData ipv6AddrData = new Ipv6AddrData((Inet6Address) Inet6Address.getByName("2001:db8::2a"));

        assertEquals(Inet6Address.getByName("2001:db8::2a"), ipv6AddrData.getValue());
    }

    @Test
    @DisplayName("ipv6addr data is decoded successfully")
    void testDecode() throws UnknownHostException {
        byte[] encoded = fromHex("20010db800000000000000000000002a");

        Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, encoded);

        assertNotNull(ipv6AddrData);
        assertEquals(Inet6Address.getByName("2001:db8::2a"), ipv6AddrData.getValue());
    }

    @Test
    @DisplayName("ipv6addr data is encoded successfully")
    void testEncode() throws UnknownHostException {
        Ipv6AddrData ipv6AddrData = new Ipv6AddrData((Inet6Address) Inet6Address.getByName("2001:db8::2a"));
        byte[] encoded = Ipv6AddrData.Codec.INSTANCE.encode(null, ipv6AddrData);

        assertEquals("20010db800000000000000000000002a", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid ipv6addr data is decoded into null")
    void testDecodeInvalidLength() {
        {
            byte[] encoded = fromHex("7f000001");
            Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv6AddrData);
        }
        {
            byte[] encoded = fromHex("20010db800000000000000000000002a00");
            Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv6AddrData);
        }
        {
            byte[] encoded = fromHex("20010db80000000000000000000000");
            Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv6AddrData);
        }
    }

}