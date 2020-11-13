package org.aaa4j.radius.core.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("TimeData")
class TimeDataTest {

    @Test
    @DisplayName("length method returns the correct length")
    void testLength() {
        TimeData timeData = new TimeData(Instant.ofEpochSecond(1605300122L));

        assertEquals(4, timeData.length());
    }

    @Test
    @DisplayName("getValue method returns the correct value")
    void testGetValue() {
        TimeData timeData = new TimeData(Instant.ofEpochSecond(1605300122L));

        assertEquals(Instant.ofEpochSecond(1605300122L), timeData.getValue());
    }

    @Test
    @DisplayName("time data is decoded successfully")
    void testDecode() {
        byte[] encoded = fromHex("5faeef9a");

        TimeData timeData = TimeData.Codec.INSTANCE.decode(null, encoded);

        assertNotNull(timeData);
        assertEquals(Instant.ofEpochSecond(1605300122L), timeData.getValue());
    }

    @Test
    @DisplayName("time data is encoded successfully")
    void testEncode() {
        TimeData timeData = new TimeData(Instant.ofEpochSecond(1605300122L));
        byte[] encoded = TimeData.Codec.INSTANCE.encode(null, timeData);

        assertEquals("5faeef9a", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid time data is decoded into null")
    void testDecodeInvalid() {
        {
            byte[] encoded = fromHex("");

            TimeData timeData = TimeData.Codec.INSTANCE.decode(null, encoded);

            assertNull(timeData);
        }
        {
            byte[] encoded = fromHex("5faeef9a00");

            TimeData timeData = TimeData.Codec.INSTANCE.decode(null, encoded);

            assertNull(timeData);
        }
        {
            byte[] encoded = fromHex("5faeef");

            TimeData timeData = TimeData.Codec.INSTANCE.decode(null, encoded);

            assertNull(timeData);
        }
    }

}