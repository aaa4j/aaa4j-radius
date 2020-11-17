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
    @DisplayName("Getters return the correct values")
    void testGetters() throws UnknownHostException {
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