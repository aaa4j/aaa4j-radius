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
    @DisplayName("Getters return the correct values")
    void testGetters() throws UnknownHostException {
        Ipv6AddrData ipv6AddrData = new Ipv6AddrData((Inet6Address) Inet6Address.getByName("2001:db8::2a"));

        assertEquals(16, ipv6AddrData.length());
        assertEquals(Inet6Address.getByName("2001:db8::2a"), ipv6AddrData.getValue());
    }

    @Test
    @DisplayName("Scope identifier is discarded on construction")
    void testScopeIdDiscarded() throws UnknownHostException {
        byte[] linkLocal = fromHex("fe800000000000000000000000000001");

        {
            Ipv6AddrData ipv6AddrData = new Ipv6AddrData(Inet6Address.getByAddress(null, linkLocal, 3));

            // The wire form has no scope field, so getValue() must not claim one
            assertEquals(0, ipv6AddrData.getValue().getScopeId());
            assertEquals("fe800000000000000000000000000001",
                    toHex(Ipv6AddrData.Codec.INSTANCE.encode(null, null, ipv6AddrData)));
        }
        {
            // An IPv4-mapped address still keeps all 16 octets through the rebuild
            Ipv6AddrData ipv6AddrData = new Ipv6AddrData(
                    Inet6Address.getByAddress(null, fromHex("00000000000000000000ffff01020304"), 7));

            assertEquals(0, ipv6AddrData.getValue().getScopeId());
            assertEquals("00000000000000000000ffff01020304",
                    toHex(Ipv6AddrData.Codec.INSTANCE.encode(null, null, ipv6AddrData)));
        }
    }

    @Test
    @DisplayName("ipv6addr data is decoded successfully")
    void testDecode() throws UnknownHostException {
        byte[] encoded = fromHex("20010db800000000000000000000002a");

        Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, null, encoded);

        assertNotNull(ipv6AddrData);
        assertEquals(Inet6Address.getByName("2001:db8::2a"), ipv6AddrData.getValue());
    }

    @Test
    @DisplayName("IPv4-mapped and IPv4-compatible addresses round trip")
    void testIpv4MappedAddresses() {
        {
            // ::ffff:1.2.3.4 is a valid IPv6 address, but InetAddress.getByAddress(byte[]) hands back an
            // Inet4Address for these 16 octets
            byte[] encoded = fromHex("00000000000000000000ffff01020304");

            Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(ipv6AddrData);
            assertEquals("00000000000000000000ffff01020304",
                    toHex(Ipv6AddrData.Codec.INSTANCE.encode(null, null, ipv6AddrData)));
        }
        {
            // ::1.2.3.4, the IPv4-compatible form
            byte[] encoded = fromHex("00000000000000000000000001020304");

            Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(ipv6AddrData);
            assertEquals("00000000000000000000000001020304",
                    toHex(Ipv6AddrData.Codec.INSTANCE.encode(null, null, ipv6AddrData)));
        }
    }

    @Test
    @DisplayName("ipv6addr data is encoded successfully")
    void testEncode() throws UnknownHostException {
        Ipv6AddrData ipv6AddrData = new Ipv6AddrData((Inet6Address) Inet6Address.getByName("2001:db8::2a"));
        byte[] encoded = Ipv6AddrData.Codec.INSTANCE.encode(null, null, ipv6AddrData);

        assertEquals("20010db800000000000000000000002a", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid ipv6addr data is decoded into null")
    void testDecodeInvalidLength() {
        {
            byte[] encoded = fromHex("7f000001");
            Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(ipv6AddrData);
        }
        {
            byte[] encoded = fromHex("20010db800000000000000000000002a00");
            Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(ipv6AddrData);
        }
        {
            byte[] encoded = fromHex("20010db80000000000000000000000");
            Ipv6AddrData ipv6AddrData = Ipv6AddrData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(ipv6AddrData);
        }
    }

}