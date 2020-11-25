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

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LongExtendedData")
class LongExtendedDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new LongExtendedData(241, null, false, false);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LongExtendedData(-1, new byte[] { 0x00, 0x01 }, false, false);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LongExtendedData(256, new byte[] { 0x00, 0x01 }, false, false);
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        LongExtendedData longExtendedData = new LongExtendedData(10, fromHex("a8f000874daa9b"), false, false);

        assertEquals(9, longExtendedData.length());
        assertEquals(10, longExtendedData.getExtendedType());
        assertArrayEquals(new int[] { 10 }, longExtendedData.getContainedType());
        assertEquals("a8f000874daa9b", toHex(longExtendedData.getExtData()));
        assertFalse(longExtendedData.hasMore());
        assertFalse(longExtendedData.isTruncated());
    }

    @Test
    @DisplayName("long-extended data is decoded successfully")
    void testDecode() {
        {
            byte[] encoded = fromHex("0ac0a8f000874daa9b");

            LongExtendedData longExtendedData = LongExtendedData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(longExtendedData);
            assertEquals(9, longExtendedData.length());
            assertEquals(10, longExtendedData.getExtendedType());
            assertArrayEquals(new int[]{10}, longExtendedData.getContainedType());
            assertEquals("a8f000874daa9b", toHex(longExtendedData.getExtData()));
            assertTrue(longExtendedData.hasMore());
            assertTrue(longExtendedData.isTruncated());
        }
        {
            byte[] encoded = fromHex("0b009c0e122fe2120c");

            LongExtendedData longExtendedData = LongExtendedData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(longExtendedData);
            assertEquals(9, longExtendedData.length());
            assertEquals(11, longExtendedData.getExtendedType());
            assertArrayEquals(new int[]{11}, longExtendedData.getContainedType());
            assertEquals("9c0e122fe2120c", toHex(longExtendedData.getExtData()));
            assertFalse(longExtendedData.hasMore());
            assertFalse(longExtendedData.isTruncated());
        }
        {
            byte[] encoded = fromHex("0d80582dd2b7e9da2332731851d42ee54e7cea185f168f7d2fffc47361e2869b" +
                    "a65310e2db3631c6e815561cc2967e4252d84d5d1226afb79c71ac523832ee09" +
                    "d50a13490d255d3ae641b97678d331c62236e16c90ec2cdd0193c8ac59ffd0c0" +
                    "82f4c9875586316bf78166e640a0f1bd538ff6694159f3782d417ca471512f3d" +
                    "c41528f7e6d3b6c38d64c92ddecea356ee7f597bcffa7656129eb4a859084837" +
                    "e8f5f5bd3cdeda8700e24960a3da9aed3e4ab5e930b4f83401dee5a0041235ad" +
                    "4c17e7e6c8bf2bb3fbdab2b7a89c664cfbd980c0ea0669914516bd90a63817be" +
                    "90ee0dcfce12d8d4831e470b621ecf3a15b3423383d6cc7c791c96de7a943a");

            LongExtendedData longExtendedData = LongExtendedData.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(longExtendedData);
            assertEquals(255, longExtendedData.length());
            assertEquals(13, longExtendedData.getExtendedType());
            assertArrayEquals(new int[]{13}, longExtendedData.getContainedType());

            byte[] expectedExtData = fromHex("582dd2b7e9da2332731851d42ee54e7cea185f168f7d2fffc47361e2869ba653" +
                    "10e2db3631c6e815561cc2967e4252d84d5d1226afb79c71ac523832ee09d50a" +
                    "13490d255d3ae641b97678d331c62236e16c90ec2cdd0193c8ac59ffd0c082f4" +
                    "c9875586316bf78166e640a0f1bd538ff6694159f3782d417ca471512f3dc415" +
                    "28f7e6d3b6c38d64c92ddecea356ee7f597bcffa7656129eb4a859084837e8f5" +
                    "f5bd3cdeda8700e24960a3da9aed3e4ab5e930b4f83401dee5a0041235ad4c17" +
                    "e7e6c8bf2bb3fbdab2b7a89c664cfbd980c0ea0669914516bd90a63817be90ee" +
                    "0dcfce12d8d4831e470b621ecf3a15b3423383d6cc7c791c96de7a943a");

            assertArrayEquals(expectedExtData, longExtendedData.getExtData());
            assertTrue(longExtendedData.hasMore());
            assertFalse(longExtendedData.isTruncated());
        }
    }

    @Test
    @DisplayName("long-extended data is encoded successfully")
    void testEncode() {
        {
            LongExtendedData longExtendedData = new LongExtendedData(10, fromHex("a8f000874daa9b"), true, true);
            byte[] encoded = LongExtendedData.Codec.INSTANCE.encode(null, longExtendedData);

            assertEquals("0ac0a8f000874daa9b", toHex(encoded));
        }
        {
            LongExtendedData longExtendedData = new LongExtendedData(11, fromHex("9c0e122fe2120c"), false, false);
            byte[] encoded = LongExtendedData.Codec.INSTANCE.encode(null, longExtendedData);

            assertEquals("0b009c0e122fe2120c", toHex(encoded));
        }
        {
            byte[] extData = fromHex("582dd2b7e9da2332731851d42ee54e7cea185f168f7d2fffc47361e2869ba653" +
                    "10e2db3631c6e815561cc2967e4252d84d5d1226afb79c71ac523832ee09d50a" +
                    "13490d255d3ae641b97678d331c62236e16c90ec2cdd0193c8ac59ffd0c082f4" +
                    "c9875586316bf78166e640a0f1bd538ff6694159f3782d417ca471512f3dc415" +
                    "28f7e6d3b6c38d64c92ddecea356ee7f597bcffa7656129eb4a859084837e8f5" +
                    "f5bd3cdeda8700e24960a3da9aed3e4ab5e930b4f83401dee5a0041235ad4c17" +
                    "e7e6c8bf2bb3fbdab2b7a89c664cfbd980c0ea0669914516bd90a63817be90ee" +
                    "0dcfce12d8d4831e470b621ecf3a15b3423383d6cc7c791c96de7a943a");

            LongExtendedData longExtendedData = new LongExtendedData(13, extData, true, false);
            byte[] encoded = LongExtendedData.Codec.INSTANCE.encode(null, longExtendedData);

            String expected = "0d80582dd2b7e9da2332731851d42ee54e7cea185f168f7d2fffc47361e2869b" +
                    "a65310e2db3631c6e815561cc2967e4252d84d5d1226afb79c71ac523832ee09" +
                    "d50a13490d255d3ae641b97678d331c62236e16c90ec2cdd0193c8ac59ffd0c0" +
                    "82f4c9875586316bf78166e640a0f1bd538ff6694159f3782d417ca471512f3d" +
                    "c41528f7e6d3b6c38d64c92ddecea356ee7f597bcffa7656129eb4a859084837" +
                    "e8f5f5bd3cdeda8700e24960a3da9aed3e4ab5e930b4f83401dee5a0041235ad" +
                    "4c17e7e6c8bf2bb3fbdab2b7a89c664cfbd980c0ea0669914516bd90a63817be" +
                    "90ee0dcfce12d8d4831e470b621ecf3a15b3423383d6cc7c791c96de7a943a";

            assertEquals(expected, toHex(encoded));
        }
    }

    @Test
    @DisplayName("Invalid long-extended data is decoded into null")
    void testDecodeInvalid() {
        // Too few bytes
        byte[] encoded = fromHex("");
        LongExtendedData longExtendedData = LongExtendedData.Codec.INSTANCE.decode(null, encoded);

        assertNull(longExtendedData);
    }

}