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
            // Prefix is null
            new Ipv4PrefixData(0, (byte[]) null);
        });

        assertThrows(NullPointerException.class, () -> {
            // Prefix is null
            new Ipv4PrefixData(0, (Inet4Address) null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Prefix length is not 32 when prefix is all zero
            new Ipv4PrefixData(1, fromHex("00000000"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Prefix length is not 32 when prefix is all zero
            new Ipv4PrefixData(1, (Inet4Address) Inet4Address.getByName("0.0.0.0"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Prefix length is outside of range
            new Ipv4PrefixData(33, fromHex("c0a80a00"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Prefix length is outside of range
            new Ipv4PrefixData(33, (Inet4Address) Inet4Address.getByName("192.168.10.0"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Prefix bits are not zero outside of the prefix length
            new Ipv4PrefixData(24, fromHex("ffffffff"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Prefix bits are not zero outside of the prefix length
            new Ipv4PrefixData(24, (Inet4Address) Inet4Address.getByName("255.255.255.255"));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() throws UnknownHostException {
        Ipv4PrefixData ipv4PrefixData = new Ipv4PrefixData(24, (Inet4Address) Inet4Address.getByName("192.168.10.0"));

        assertEquals(6, ipv4PrefixData.length());
        assertEquals(24, ipv4PrefixData.getPrefixLength());
        assertEquals(Inet4Address.getByName("192.168.10.0"), ipv4PrefixData.getPrefixAddress());
    }

    @Test
    @DisplayName("ipv4prefix data is decoded successfully")
    void testDecode() throws UnknownHostException {
        byte[] encoded = fromHex("0018c0a80a00");

        Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, null, encoded);

        assertNotNull(ipv4PrefixData);
        assertEquals(24, ipv4PrefixData.getPrefixLength());
        assertEquals(Inet4Address.getByName("192.168.10.0"), ipv4PrefixData.getPrefixAddress());
    }

    @Test
    @DisplayName("ipv4prefix data is encoded successfully")
    void testEncode() throws UnknownHostException {
        Ipv4PrefixData ipv4PrefixData = new Ipv4PrefixData(24, (Inet4Address) Inet4Address.getByName("192.168.10.0"));
        byte[] encoded = Ipv4PrefixData.Codec.INSTANCE.encode(null, null, ipv4PrefixData);

        assertEquals("0018c0a80a00", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid ipv4prefix data is decoded into null")
    void testDecodeInvalid() {
        {
            // Reserved byte is not 0x00
            byte[] encoded = fromHex("ff18c0a80a00");
            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(ipv4PrefixData);
        }
        {
            // Too few bytes
            byte[] encoded = fromHex("0018c0a80a");
            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(ipv4PrefixData);
        }
        {
            // Prefix length outside of range
            byte[] encoded = fromHex("0051c0a80a00");
            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(ipv4PrefixData);
        }
        {
            // Too many bytes
            byte[] encoded = fromHex("0018c0a80a0000");
            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(ipv4PrefixData);
        }
        {
            // Prefix length is not 32 when all prefix bytes are 0x00
            byte[] encoded = fromHex("000100000000");
            Ipv4PrefixData ipv4PrefixData = Ipv4PrefixData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(ipv4PrefixData);
        }
    }

}