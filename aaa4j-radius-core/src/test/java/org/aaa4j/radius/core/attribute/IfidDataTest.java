package org.aaa4j.radius.core.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("IfidData")
class IfidDataTest {

    @Test
    @DisplayName("Constructor validates arguments")
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new IfidData(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new IfidData(fromHex("02aaccfffec51fda00"));
        });
    }

    @Test
    @DisplayName("length method returns the correct length")
    void testLength() {
        IfidData ifidData = new IfidData(fromHex("02aaccfffec51fda"));

        assertEquals(8, ifidData.length());
    }

    @Test
    @DisplayName("getValue method returns the correct value")
    void testGetValue() {
        IfidData ifidData = new IfidData(fromHex("02aaccfffec51fda"));

        assertEquals("02aaccfffec51fda", toHex(ifidData.getValue()));
    }

    @Test
    @DisplayName("ifid data is decoded successfully")
    void testDecode() {
        byte[] encoded = fromHex("02aaccfffec51fda");

        IfidData ifidData = IfidData.Codec.INSTANCE.decode(null, encoded);

        assertNotNull(ifidData);
        assertEquals("02aaccfffec51fda", toHex(ifidData.getValue()));
    }

    @Test
    @DisplayName("ifid data is encoded successfully")
    void testEncode() {
        IfidData ifidData = new IfidData(fromHex("02aaccfffec51fda"));
        byte[] encoded = IfidData.Codec.INSTANCE.encode(null, ifidData);

        assertEquals("02aaccfffec51fda", toHex(encoded));
    }

    @Test
    @DisplayName("Invalid ifid data is decoded into null")
    void testDecodeInvalid() {
        {
            byte[] encoded = fromHex("");
            IfidData ifidData = IfidData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ifidData);
        }
        {
            byte[] encoded = fromHex("02aaccfffec51fda00");
            IfidData ifidData = IfidData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ifidData);
        }
        {
            byte[] encoded = fromHex("02aaccfffec51f");
            IfidData ifidData = IfidData.Codec.INSTANCE.decode(null, encoded);

            assertNull(ifidData);
        }
    }

}