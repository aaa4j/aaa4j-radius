package org.aaa4j.radius.core.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RawAttribute")
class RawAttributeTest {

    @Test
    void testEqualityEquals() {
        RawAttribute a = new RawAttribute(1, "jdoe".getBytes(UTF_8));
        RawAttribute b = new RawAttribute(1, "jdoe".getBytes(UTF_8));

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testEqualityNotEqualsType() {
        RawAttribute a = new RawAttribute(1, "jdoe".getBytes(UTF_8));
        RawAttribute b = new RawAttribute(50, "jdoe".getBytes(UTF_8));

        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test
    void testEqualityNotEqualsData() {
        RawAttribute a = new RawAttribute(1, "jdoe".getBytes(UTF_8));
        RawAttribute b = new RawAttribute(1, "bob".getBytes(UTF_8));

        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

}