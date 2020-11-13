package org.aaa4j.radius.core.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Integer64Data")
class Integer64DataTest {

    @Test
    @DisplayName("length method returns the correct length")
    void testLength() {
        Integer64Data integer64Data = new Integer64Data(42);

        assertEquals(8, integer64Data.length());
    }

    @Test
    @DisplayName("getValue method returns the correct value")
    void testGetValue() {
        Integer64Data integer64Data = new Integer64Data(42);

        assertEquals(42, integer64Data.getValue());
    }

    @Test
    @DisplayName("integer64 data is decoded successfully")
    void testDecode() {
        {
            byte[] encoded = fromHex("018b7e72f75abe81");

            Integer64Data integer64Data = Integer64Data.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(integer64Data);
            assertEquals(111_321_648_042_000_001L, integer64Data.getValue());
        }
        {
            byte[] encoded = fromHex("ffffffffffffffd6");

            Integer64Data integer64Data = Integer64Data.Codec.INSTANCE.decode(null, encoded);

            assertNotNull(integer64Data);
            assertEquals(-42L, integer64Data.getValue());
        }
    }

    @Test
    @DisplayName("integer64 data is encoded successfully")
    void testEncode() {
        {
            byte[] encoded = Integer64Data.Codec.INSTANCE.encode(null, new Integer64Data(111_321_648_042_000_001L));

            assertEquals("018b7e72f75abe81", toHex(encoded));
        }
        {
            byte[] encoded = Integer64Data.Codec.INSTANCE.encode(null, new Integer64Data(-42L));

            assertEquals("ffffffffffffffd6", toHex(encoded));
        }
    }

    @Test
    @DisplayName("invalid integer64 data is decoded into null")
    void testDecodeInvalidLength() {
        {
            byte[] encoded = fromHex("");

            Integer64Data integer64Data = Integer64Data.Codec.INSTANCE.decode(null, encoded);

            assertNull(integer64Data);
        }
        {
            byte[] encoded = fromHex("8b7e72f75abe81");

            Integer64Data integer64Data = Integer64Data.Codec.INSTANCE.decode(null, encoded);

            assertNull(integer64Data);
        }
        {
            byte[] encoded = fromHex("00018b7e72f75abe81");

            Integer64Data integer64Data = Integer64Data.Codec.INSTANCE.decode(null, encoded);

            assertNull(integer64Data);
        }
    }

}