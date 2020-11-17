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

@DisplayName("Ipv6PrefixData")
class Ipv6PrefixDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            // Prefix byte array is null
            new Ipv6PrefixData(128, (byte[]) null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Prefix length is out of range
            new Ipv6PrefixData(-1, new byte[] {});
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Prefix length is out of range
            new Ipv6PrefixData(129, fromHex("20010db8000000000000000000000001ff"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Bits outside of the prefix are non-zero
            new Ipv6PrefixData(127, fromHex("20010db8000000000000000000000001"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Not enough bytes for the prefix length
            new Ipv6PrefixData(64, new byte[] {});
        });

        assertThrows(NullPointerException.class, () -> {
            // Prefix address is null
            new Ipv6PrefixData(128, (Inet6Address) null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Bits outside of the prefix are non-zero
            new Ipv6PrefixData(127, (Inet6Address) Inet6Address.getByName("2001:db8::1"));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() throws UnknownHostException {
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(0, fromHex(""));

            assertEquals(2, ipv6PrefixData.length());
            assertEquals(0, ipv6PrefixData.getPrefixLength());
            assertEquals("", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("::"), ipv6PrefixData.getPrefixAddress());
        }
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(32, fromHex("20010db8000000000000000000000000"));

            assertEquals(18, ipv6PrefixData.length());
            assertEquals(32, ipv6PrefixData.getPrefixLength());
            assertEquals("20010db8000000000000000000000000", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("2001:db8::"), ipv6PrefixData.getPrefixAddress());
        }
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(32, fromHex("20010db8"));

            assertEquals(6, ipv6PrefixData.length());
            assertEquals(32, ipv6PrefixData.getPrefixLength());
            assertEquals("20010db8", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("2001:db8::"), ipv6PrefixData.getPrefixAddress());
        }
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(128, fromHex("20010db8000000000000000000000000"));

            assertEquals(18, ipv6PrefixData.length());
            assertEquals(128, ipv6PrefixData.getPrefixLength());
            assertEquals("20010db8000000000000000000000000", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("2001:db8::"), ipv6PrefixData.getPrefixAddress());
        }
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(8, fromHex("20"));

            assertEquals(3, ipv6PrefixData.length());
            assertEquals(8, ipv6PrefixData.getPrefixLength());
            assertEquals("20", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("2000::"), ipv6PrefixData.getPrefixAddress());
        }
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(7, (Inet6Address) Inet6Address.getByName("2000::"));

            assertEquals(3, ipv6PrefixData.length());
            assertEquals(7, ipv6PrefixData.getPrefixLength());
            assertEquals("20", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("2000::"), ipv6PrefixData.getPrefixAddress());
        }
    }

    @Test
    @DisplayName("ipv6prefix data is decoded successfully")
    void testDecode() throws UnknownHostException {
        {
            byte[] encoded = fromHex("002020010db8000000000000000000000000");

            Ipv6PrefixData ipv6PrefixData = Ipv6PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(ipv6PrefixData);
            assertEquals(32, ipv6PrefixData.getPrefixLength());
            assertEquals("20010db8000000000000000000000000", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("2001:db8::"), ipv6PrefixData.getPrefixAddress());
        }
        {
            byte[] encoded = fromHex("002020010db8");

            Ipv6PrefixData ipv6PrefixData = Ipv6PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(ipv6PrefixData);
            assertEquals(32, ipv6PrefixData.getPrefixLength());
            assertEquals("20010db8", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("2001:db8::"), ipv6PrefixData.getPrefixAddress());
        }
        {
            byte[] encoded = fromHex("004020010db8ffffffff0000000000000000");

            Ipv6PrefixData ipv6PrefixData = Ipv6PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(ipv6PrefixData);
            assertEquals(64, ipv6PrefixData.getPrefixLength());
            assertEquals("20010db8ffffffff0000000000000000", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("2001:db8:ffff:ffff::"), ipv6PrefixData.getPrefixAddress());
        }
        {
            byte[] encoded = fromHex("008020010db8000000000000000000000001");

            Ipv6PrefixData ipv6PrefixData = Ipv6PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(ipv6PrefixData);
            assertEquals(128, ipv6PrefixData.getPrefixLength());
            assertEquals("20010db8000000000000000000000001", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("2001:db8::1"), ipv6PrefixData.getPrefixAddress());
        }
        {
            byte[] encoded = fromHex("0000");

            Ipv6PrefixData ipv6PrefixData = Ipv6PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(ipv6PrefixData);
            assertEquals(0, ipv6PrefixData.getPrefixLength());
            assertEquals("", toHex(ipv6PrefixData.getPrefixBytes()));
            assertEquals(Inet6Address.getByName("::"), ipv6PrefixData.getPrefixAddress());
        }
    }

    @Test
    @DisplayName("ipv6prefix data is encoded successfully")
    void testEncode() {
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(32, fromHex("20010db8000000000000000000000000"));
            byte[] encoded = Ipv6PrefixData.Codec.INSTANCE.encode(null, ipv6PrefixData);

            assertEquals("002020010db8000000000000000000000000", toHex(encoded));
        }
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(32, fromHex("20010db8"));
            byte[] encoded = Ipv6PrefixData.Codec.INSTANCE.encode(null, ipv6PrefixData);

            assertEquals("002020010db8", toHex(encoded));
        }
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(64, fromHex("20010db8ffffffff0000000000000000"));
            byte[] encoded = Ipv6PrefixData.Codec.INSTANCE.encode(null, ipv6PrefixData);

            assertEquals("004020010db8ffffffff0000000000000000", toHex(encoded));
        }
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(128, fromHex("20010db8000000000000000000000001"));
            byte[] encoded = Ipv6PrefixData.Codec.INSTANCE.encode(null, ipv6PrefixData);

            assertEquals("008020010db8000000000000000000000001", toHex(encoded));
        }
        {
            Ipv6PrefixData ipv6PrefixData = new Ipv6PrefixData(0, fromHex(""));
            byte[] encoded = Ipv6PrefixData.Codec.INSTANCE.encode(null, ipv6PrefixData);

            assertEquals("0000", toHex(encoded));
        }
    }

    @Test
    @DisplayName("Invalid ipv6prefix data is decoded into null")
    void testDecodeInvalid() {
        {
            // Reserved byte is not 0x00
            byte[] encoded = fromHex("ff2020010db8");
            Ipv6PrefixData ipv6PrefixData = Ipv6PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv6PrefixData);
        }
        {
            // Prefix length 129 is larger than 128
            byte[] encoded = fromHex("008120010db8");
            Ipv6PrefixData ipv6PrefixData = Ipv6PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv6PrefixData);
        }
        {
            // Not enough prefix bytes for 128-bit prefix
            byte[] encoded = fromHex("008020010db80000000000000000000000");
            Ipv6PrefixData ipv6PrefixData = Ipv6PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv6PrefixData);
        }
        {
            // Bits outside of the prefix are not 0
            byte[] encoded = fromHex("002020010db8000000000000000000000001");
            Ipv6PrefixData ipv6PrefixData = Ipv6PrefixData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ipv6PrefixData);
        }
    }

}