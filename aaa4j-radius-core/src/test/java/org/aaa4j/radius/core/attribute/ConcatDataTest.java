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

import java.util.List;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ConcatData")
class ConcatDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new ConcatData((byte[]) null);
        });

        assertThrows(NullPointerException.class, () -> {
            new ConcatData((List<byte[]>) null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // The data must contain at least one byte
            new ConcatData(new byte[0]);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // At least one fragment is required
            new ConcatData(List.of());
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // A fragment must contain at least one byte
            new ConcatData(List.of(fromHex("ab"), new byte[0]));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // A fragment can't hold more than a full attribute's worth of data
            new ConcatData(List.of(new byte[254]));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        {
            ConcatData concatData = new ConcatData(fromHex("e1273a01729a"));

            assertEquals(6, concatData.length());
            assertEquals("e1273a01729a", toHex(concatData.getData()));
            assertEquals(1, concatData.getFragments().size());
            assertEquals("e1273a01729a", toHex(concatData.getFragments().get(0)));
        }
        {
            ConcatData concatData = new ConcatData(List.of(fromHex("e1273a"), fromHex("01729a")));

            assertEquals(6, concatData.length());
            assertEquals("e1273a01729a", toHex(concatData.getData()));
            assertEquals(2, concatData.getFragments().size());
            assertEquals("e1273a", toHex(concatData.getFragments().get(0)));
            assertEquals("01729a", toHex(concatData.getFragments().get(1)));
        }
    }

    @Test
    @DisplayName("Data is fragmented into full attributes worth of data")
    void testCanonicalFragmentation() {
        {
            // The largest data that still fits a single fragment
            ConcatData concatData = new ConcatData(patterned(253));

            assertEquals(1, concatData.getFragments().size());
            assertEquals(253, concatData.getFragments().get(0).length);
        }
        {
            // One byte more no longer fits a single attribute
            ConcatData concatData = new ConcatData(patterned(254));

            assertEquals(2, concatData.getFragments().size());
            assertEquals(253, concatData.getFragments().get(0).length);
            assertEquals(1, concatData.getFragments().get(1).length);
        }
        {
            ConcatData concatData = new ConcatData(patterned(600));

            assertEquals(3, concatData.getFragments().size());
            assertEquals(253, concatData.getFragments().get(0).length);
            assertEquals(253, concatData.getFragments().get(1).length);
            assertEquals(94, concatData.getFragments().get(2).length);
            assertEquals(600, concatData.length());
            assertArrayEquals(patterned(600), concatData.getData());

            // The fragments carry the data in order without gaps or overlaps
            byte[] reassembled = new byte[600];
            int position = 0;

            for (byte[] fragment : concatData.getFragments()) {
                System.arraycopy(fragment, 0, reassembled, position, fragment.length);
                position += fragment.length;
            }

            assertArrayEquals(patterned(600), reassembled);
        }
    }

    @Test
    @DisplayName("Fragments given to the constructor are preserved as given")
    void testFragmentsPreserved() {
        // Received fragmentation is kept rather than re-chunked so that proxied data re-encodes as it arrived, even
        // though the same 300 bytes given as a single array would fragment into 253 + 47
        ConcatData concatData = new ConcatData(List.of(patterned(100), patterned(100), patterned(100)));

        assertEquals(3, concatData.getFragments().size());
        assertEquals(100, concatData.getFragments().get(0).length);
        assertEquals(100, concatData.getFragments().get(1).length);
        assertEquals(100, concatData.getFragments().get(2).length);
        assertEquals(300, concatData.length());
    }

    private static byte[] patterned(int length) {
        byte[] bytes = new byte[length];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        return bytes;
    }

}
