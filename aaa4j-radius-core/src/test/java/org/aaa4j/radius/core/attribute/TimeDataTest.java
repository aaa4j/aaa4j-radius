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

import java.time.Instant;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TimeData")
class TimeDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new TimeData(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // Before 1970-01-01T00:00:00Z
            new TimeData(Instant.ofEpochSecond(-1L));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            // After 2106-02-07T06:28:15Z
            new TimeData(Instant.ofEpochSecond(4294967296L));
        });
    }

    @Test
    @DisplayName("Getters return the correct values")
    void testGetters() {
        TimeData timeData = new TimeData(Instant.ofEpochSecond(1605300122L));

        assertEquals(4, timeData.length());
        assertEquals(Instant.ofEpochSecond(1605300122L), timeData.getValue());
    }

    @Test
    @DisplayName("Sub-second precision is truncated on construction")
    void testSubSecondTruncation() {
        {
            TimeData timeData = new TimeData(Instant.ofEpochSecond(1605300122L, 500000000L));

            // getValue() reflects what will be encoded rather than what was passed in
            assertEquals(Instant.ofEpochSecond(1605300122L), timeData.getValue());
            assertEquals(0, timeData.getValue().getNano());
            assertEquals("5faeef9a", toHex(TimeData.Codec.INSTANCE.encode(null, null, timeData)));
        }
        {
            // One nanosecond short of the next second still truncates down
            TimeData timeData = new TimeData(Instant.ofEpochSecond(1605300122L, 999999999L));

            assertEquals(Instant.ofEpochSecond(1605300122L), timeData.getValue());
            assertEquals("5faeef9a", toHex(TimeData.Codec.INSTANCE.encode(null, null, timeData)));
        }
        {
            // The upper bound is unaffected by a sub-second component
            TimeData timeData = new TimeData(Instant.ofEpochSecond(4294967295L, 1L));

            assertEquals(Instant.ofEpochSecond(4294967295L), timeData.getValue());
            assertEquals("ffffffff", toHex(TimeData.Codec.INSTANCE.encode(null, null, timeData)));
        }
    }

    @Test
    @DisplayName("time data is decoded successfully")
    void testDecode() {
        byte[] encoded = fromHex("5faeef9a");

        TimeData timeData = TimeData.Codec.INSTANCE.decode(null, null, encoded);

        assertNotNull(timeData);
        assertEquals(Instant.ofEpochSecond(1605300122L), timeData.getValue());
    }

    @Test
    @DisplayName("time data is encoded successfully")
    void testEncode() {
        TimeData timeData = new TimeData(Instant.ofEpochSecond(1605300122L));
        byte[] encoded = TimeData.Codec.INSTANCE.encode(null, null, timeData);

        assertEquals("5faeef9a", toHex(encoded));
    }

    @Test
    @DisplayName("time data at and beyond 2038 is decoded successfully")
    void testDecodePost2038() {
        {
            // 2038-01-19T03:14:07Z, the last second representable in a signed 32-bit integer
            byte[] encoded = fromHex("7fffffff");

            TimeData timeData = TimeData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(timeData);
            assertEquals(Instant.ofEpochSecond(2147483647L), timeData.getValue());
        }
        {
            // 2038-01-19T03:14:08Z, the first second that sets the high bit
            byte[] encoded = fromHex("80000000");

            TimeData timeData = TimeData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(timeData);
            assertEquals(Instant.ofEpochSecond(2147483648L), timeData.getValue());
        }
        {
            // 2106-02-07T06:28:15Z, the last second representable in an unsigned 32-bit integer
            byte[] encoded = fromHex("ffffffff");

            TimeData timeData = TimeData.Codec.INSTANCE.decode(null, null, encoded);

            assertNotNull(timeData);
            assertEquals(Instant.ofEpochSecond(4294967295L), timeData.getValue());
        }
    }

    @Test
    @DisplayName("time data at and beyond 2038 is encoded successfully")
    void testEncodePost2038() {
        {
            TimeData timeData = new TimeData(Instant.ofEpochSecond(2147483648L));
            byte[] encoded = TimeData.Codec.INSTANCE.encode(null, null, timeData);

            assertEquals("80000000", toHex(encoded));
        }
        {
            TimeData timeData = new TimeData(Instant.ofEpochSecond(4294967295L));
            byte[] encoded = TimeData.Codec.INSTANCE.encode(null, null, timeData);

            assertEquals("ffffffff", toHex(encoded));
        }
    }

    @Test
    @DisplayName("Invalid time data is decoded into null")
    void testDecodeInvalid() {
        {
            byte[] encoded = fromHex("");
            TimeData timeData = TimeData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(timeData);
        }
        {
            byte[] encoded = fromHex("5faeef9a00");
            TimeData timeData = TimeData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(timeData);
        }
        {
            byte[] encoded = fromHex("5faeef");
            TimeData timeData = TimeData.Codec.INSTANCE.decode(null, null, encoded);

            assertNull(timeData);
        }
    }

}