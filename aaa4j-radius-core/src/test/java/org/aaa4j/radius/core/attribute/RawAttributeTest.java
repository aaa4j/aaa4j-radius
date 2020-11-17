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