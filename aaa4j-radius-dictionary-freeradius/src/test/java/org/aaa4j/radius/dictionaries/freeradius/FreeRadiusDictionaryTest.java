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

package org.aaa4j.radius.dictionaries.freeradius;

import org.aaa4j.radius.core.attribute.AttributeType;
import org.aaa4j.radius.core.attribute.OptionalTaggedTextData;
import org.aaa4j.radius.core.attribute.StringData;
import org.aaa4j.radius.core.attribute.TaggedIntegerData;
import org.aaa4j.radius.core.attribute.TaggedStringData;
import org.aaa4j.radius.core.dictionary.AttributeDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("FreeRadiusDictionary")
class FreeRadiusDictionaryTest {

    private static final FreeRadiusDictionary DICTIONARY = new FreeRadiusDictionary();

    private static final int VENDOR_SPECIFIC = 26;

    private static final int MICROSOFT = 311;

    private static final int ASCEND = 529;

    private static final int LUCENT = 4846;

    private static Class<?> dataClassOf(AttributeType attributeType) {
        AttributeDefinition<?, ?> attributeDefinition = DICTIONARY.getAttributeDefinition(attributeType);

        assertNotNull(attributeDefinition, "no definition for attribute " + attributeType);

        return attributeDefinition.getDataClass();
    }

    private static Class<?> dataClassOf(int type) {
        return dataClassOf(new AttributeType(type));
    }

    /**
     * Covers the mapping from the FreeRADIUS "has_tag" modifier onto the tagged data types. The FreeRADIUS dictionaries
     * are parsed at runtime, so these assertions pin the behaviour of {@code typeFromString} for the three RFC 2868 tag
     * classes.
     */
    @Nested
    @DisplayName("Tagged attributes")
    class TaggedAttributes {

        @Test
        @DisplayName("Integer attributes with has_tag use the mandatory tagged integer type")
        void taggedIntegerAttributes() {
            // Tunnel-Type (64), Tunnel-Medium-Type (65) and Tunnel-Preference (83) are "integer has_tag"
            assertEquals(TaggedIntegerData.class, dataClassOf(64));
            assertEquals(TaggedIntegerData.class, dataClassOf(65));
            assertEquals(TaggedIntegerData.class, dataClassOf(83));
        }

        @Test
        @DisplayName("String attributes with has_tag use the optionally tagged text type")
        void optionallyTaggedTextAttributes() {
            // These are "string has_tag" with no encryption, so the tag is optional and self-disambiguating
            assertEquals(OptionalTaggedTextData.class, dataClassOf(66));
            assertEquals(OptionalTaggedTextData.class, dataClassOf(67));
            assertEquals(OptionalTaggedTextData.class, dataClassOf(81));
            assertEquals(OptionalTaggedTextData.class, dataClassOf(82));
            assertEquals(OptionalTaggedTextData.class, dataClassOf(90));
            assertEquals(OptionalTaggedTextData.class, dataClassOf(91));
        }

        @Test
        @DisplayName("Tunnel-Password uses the octet-based tagged string type rather than the text type")
        void tunnelPasswordUsesStringRatherThanText() {
            // Tunnel-Password (69) is "string has_tag,encrypt=2" in the FreeRADIUS dictionary, but FreeRADIUS's
            // "string" means text. A password is arbitrary octets, and RFC 8044 gives attribute 69 the "string"
            // data type, so the encrypted tagged form must not decode as UTF-8 text.
            assertEquals(TaggedStringData.class, dataClassOf(69));
        }

    }

    /**
     * Covers what happens to attributes that declare encryption. An attribute whose encryption can't be applied is left
     * undefined rather than registered without it, because registering it would put the value on the wire in the clear
     * under a name that implies otherwise.
     */
    @Nested
    @DisplayName("Encrypted attributes")
    class EncryptedAttributes {

        @Test
        @DisplayName("Supported encryption schemes are registered")
        void supportedSchemesAreRegistered() {
            // Tunnel-Password (69) is "string has_tag,encrypt=2"
            assertEquals(TaggedStringData.class, dataClassOf(69));

            // Microsoft's MPPE-Send-Key (16) and MPPE-Recv-Key (17) are "octets encrypt=2"
            for (int type : new int[] { 16, 17 }) {
                assertEquals(StringData.class, dataClassOf(new AttributeType(VENDOR_SPECIFIC, MICROSOFT, type)));
            }
        }

        @Test
        @DisplayName("An attribute using an unsupported encryption scheme is not registered")
        void unsupportedSchemeIsNotRegistered() {
            // Send-Secret (214) and Receive-Secret (215) are "string encrypt=3", the Ascend secret scheme. There
            // is no filter for it, so registering them would send the secret in the clear.
            for (int vendorId : new int[] { ASCEND, LUCENT }) {
                assertNull(DICTIONARY.getAttributeDefinition(new AttributeType(VENDOR_SPECIFIC, vendorId, 214)));
                assertNull(DICTIONARY.getAttributeDefinition(new AttributeType(VENDOR_SPECIFIC, vendorId, 215)));
            }
        }

        @Test
        @DisplayName("Attributes without encryption are unaffected")
        void unencryptedAttributesAreUnaffected() {
            // A neighbouring attribute with no encrypt flag still resolves, so the skip is scoped to the
            // attributes that ask for encryption rather than to the whole vendor
            assertNotNull(DICTIONARY.getAttributeDefinition(new AttributeType(VENDOR_SPECIFIC, ASCEND, 213)));
        }

    }

}
