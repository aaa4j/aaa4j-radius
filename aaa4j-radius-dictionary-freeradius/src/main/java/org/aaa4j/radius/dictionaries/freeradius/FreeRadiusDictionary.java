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
import org.aaa4j.radius.core.attribute.EnumData;
import org.aaa4j.radius.core.attribute.IfidData;
import org.aaa4j.radius.core.attribute.Integer64Data;
import org.aaa4j.radius.core.attribute.IntegerData;
import org.aaa4j.radius.core.attribute.Ipv4AddrData;
import org.aaa4j.radius.core.attribute.Ipv4PrefixData;
import org.aaa4j.radius.core.attribute.Ipv6AddrData;
import org.aaa4j.radius.core.attribute.Ipv6PrefixData;
import org.aaa4j.radius.core.attribute.StandardAttribute;
import org.aaa4j.radius.core.attribute.StringData;
import org.aaa4j.radius.core.attribute.TextData;
import org.aaa4j.radius.core.attribute.VendorSpecificAttribute;
import org.aaa4j.radius.core.dictionary.AttributeDefinition;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.PacketDefinition;
import org.aaa4j.radius.core.dictionary.TlvDefinition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A dictionary using FreeRADIUS dictionary files.
 */
public class FreeRadiusDictionary implements Dictionary {

    private static final Map<AttributeType, AttributeDefinition<?, ?>> typeAttributeDefinitionsMap = new HashMap<>();

    private static final Map<String, AttributeDefinition<?, ?>> nameAttributeDefinitionsMap = new HashMap<>();

    private static final Map<AttributeType, Map<String, Integer>> numericAttributeValueMap = new HashMap<>();

    private static final Map<AttributeType, TlvDefinition> tlvDefinitionsMap = new HashMap<>();

    private static final String RESOURCE_BASE_PATH = "freeradius-dictionary/";

    private static final String ROOT_DICTIONARY_NAME = "dictionary";

    private static final String INCLUDE = "$INCLUDE";

    private static final String VENDOR = "VENDOR";

    private static final String BEGIN_VENDOR = "BEGIN-VENDOR";

    private static final String ATTRIBUTE = "ATTRIBUTE";

    private static final String END_VENDOR = "END-VENDOR";

    private static final String VALUE = "VALUE";

    private static final String MEMBER = "MEMBER";

    private static final String STRUCT = "STRUCT";

    private static final String FLAGS = "FLAGS";

    private static final Pattern DIGITS_PATTERN = Pattern.compile("^\\d+$");

    private static final int VENDOR_SPECIFIC_ATTRIBUTE = 26;

    private static final AttributeType VENDOR_SPECIFIC_ATTRIBUTE_TYPE =
            new AttributeType(VENDOR_SPECIFIC_ATTRIBUTE);

    static {
        try {
            List<String> rootFileLines = getDictionaryLines(ROOT_DICTIONARY_NAME);

            for (String rootFileLine : rootFileLines) {
                String[] rootFileTokens = rootFileLine.split("\\s");

                for (String rootFileToken : rootFileTokens) {
                    if (rootFileToken.equals(INCLUDE)) {
                        List<String> lines = getDictionaryLines(rootFileTokens[1]);

                        Integer currentVendorId = null;
                        String currentVendorName = null;

                        file:
                        for (String line : lines) {
                            String[] tokens = line.split("\\s");

                            tokens:
                            for (int j = 0; j < tokens.length; j++) {
                                String token = tokens[j];

                                switch (token) {
                                    case VENDOR: {
                                        String vendorName = tokens[++j];
                                        int vendorId = Integer.parseInt(tokens[++j]);

                                        currentVendorId = vendorId;
                                        currentVendorName = vendorName;

                                        if (tokens.length > j + 1) {
                                            // Handle format: (e.g., "format=1,1,c")
                                            String format = tokens[++j];
                                        }
                                        break;
                                    }
                                    case BEGIN_VENDOR: {
                                        String vendorName = tokens[++j];

                                        currentVendorName = vendorName;

                                        if (tokens.length > j + 1) {
                                            // Handle format (e.g.,
                                            // "parent=.Extended-Attribute-5.Extended-Vendor-Specific-5")
                                            String format = tokens[++j];
                                        }
                                        break;
                                    }
                                    case END_VENDOR: {
                                        String vendorName = tokens[++j];

                                        currentVendorId = null;
                                        currentVendorName = null;
                                        break;
                                    }
                                    case ATTRIBUTE: {
                                        if (currentVendorId == null) {
                                            // It's a standard attribute
                                            String attributeName = tokens[++j];
                                            String attributeId = tokens[++j];
                                            String typeLiteral = tokens[++j];

                                            if (tokens.length > j + 1) {
                                                // Handle format
                                                String format = tokens[++j];
                                            }

                                            if (!DIGITS_PATTERN.asPredicate().test(attributeId)) {
                                                // Only consider non-nested attributes
                                                break tokens;
                                            }

                                            int intAttributeId = Integer.parseInt(attributeId);

                                            AttributeType attributeType = new AttributeType(intAttributeId);
                                            String fullName = String.format("%s", attributeName);

                                            switch (typeFromString(typeLiteral)) {
                                                case CONCAT:
                                                    break;
                                                case ENUM: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    EnumData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            EnumData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (EnumData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case EVS:
                                                    break;
                                                case EXTENDED:
                                                    break;
                                                case IFID: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    IfidData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            IfidData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (IfidData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case INTEGER_64: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Integer64Data.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            Integer64Data.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (Integer64Data data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case INTEGER: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    IntegerData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            IntegerData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (IntegerData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case IPV4_ADDR: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv4AddrData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            Ipv4AddrData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (Ipv4AddrData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case IPV4_PREFIX: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv4PrefixData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            Ipv4PrefixData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (Ipv4PrefixData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case IPV6_ADDR: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv6AddrData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            Ipv6AddrData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (Ipv6AddrData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case IPV6_PREFIX: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv6PrefixData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            Ipv6PrefixData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (Ipv6PrefixData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case LONG_EXTENDED:
                                                    break;
                                                case OPTIONAL_TAGGED_STRING:
                                                    break;
                                                case STRING: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    StringData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            StringData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (StringData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case TAGGED_INTEGER:
                                                    break;
                                                case TAGGED_STRING:
                                                    break;
                                                case TEXT: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TextData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            TextData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (TextData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case TIME:
                                                    break;
                                                case TLV:
                                                    break;
                                                case UNKNOWN:
                                                    break;
                                                case VSA:
                                                    break;
                                            }
                                        }
                                        else {
                                            // It's a vendor-specific attribute
                                            String attributeName = tokens[++j];
                                            String attributeId = tokens[++j];
                                            String typeLiteral = tokens[++j];

                                            if (tokens.length > j + 1) {
                                                // Handle format
                                                String format = tokens[++j];
                                            }

                                            if (!DIGITS_PATTERN.asPredicate().test(attributeId)) {
                                                // Only consider non-nested attributes
                                                break tokens;
                                            }

                                            int intAttributeId = Integer.parseInt(attributeId);

                                            AttributeType attributeType = new AttributeType(VENDOR_SPECIFIC_ATTRIBUTE,
                                                    currentVendorId, intAttributeId);
                                            String fullName = String.format("%s-%s", currentVendorName, attributeName);
                                            final Integer finalVendorId = currentVendorId;

                                            switch (typeFromString(typeLiteral)) {
                                                case CONCAT:
                                                    break;
                                                case ENUM: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    EnumData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            EnumData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (EnumData data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case EVS:
                                                    break;
                                                case EXTENDED:
                                                    break;
                                                case IFID: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    IfidData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            IfidData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (IfidData data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case INTEGER_64: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Integer64Data.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            Integer64Data.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (Integer64Data data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case INTEGER: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    IntegerData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            IntegerData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (IntegerData data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case IPV4_ADDR: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv4AddrData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            Ipv4AddrData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (Ipv4AddrData data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case IPV4_PREFIX: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv4PrefixData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            Ipv4PrefixData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (Ipv4PrefixData data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case IPV6_ADDR: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv6AddrData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            Ipv6AddrData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (Ipv6AddrData data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case IPV6_PREFIX: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv6PrefixData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            Ipv6PrefixData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (Ipv6PrefixData data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case LONG_EXTENDED:
                                                    break;
                                                case OPTIONAL_TAGGED_STRING:
                                                    break;
                                                case STRING: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    StringData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            StringData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (StringData data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case TAGGED_INTEGER:
                                                    break;
                                                case TAGGED_STRING:
                                                    break;
                                                case TEXT: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TextData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            TextData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (TextData data) ->
                                                                            new VendorSpecificAttribute<>(
                                                                                    VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                    finalVendorId,
                                                                                    intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case TIME:
                                                    break;
                                                case TLV:
                                                    break;
                                                case UNKNOWN:
                                                    break;
                                                case VSA:
                                                    break;
                                            }
                                        }
                                        break;
                                    }
                                    case MEMBER:
                                        // Ignore MEMBER
                                        break tokens;
                                    case VALUE: {
                                        String attributeName = tokens[++j];
                                        String valueName = tokens[++j];
                                        String valueValue = tokens[++j];

                                        String fullName = currentVendorId == null
                                                ? attributeName
                                                : String.format("%s-%s", currentVendorName, attributeName);

                                        AttributeDefinition<?, ?> attributeDefinition =
                                                nameAttributeDefinitionsMap.get(fullName.toLowerCase(Locale.ROOT));

                                        if (attributeDefinition == null) {
                                            // No attribute found for the value (shouldn't happen if all attributes are
                                            // parsed first as they should be defined in the dictionary)
                                            break tokens;
                                        }

                                        if (!DIGITS_PATTERN.asPredicate().test(valueValue)) {
                                            // Only consider numeric values
                                            break tokens;
                                        }

                                        Map<String, Integer> valuesMap =
                                                numericAttributeValueMap.computeIfAbsent(
                                                        attributeDefinition.getIdentifier(), k -> new HashMap<>());

                                        valuesMap.put(valueName.toLowerCase(Locale.ROOT),
                                                Integer.parseUnsignedInt(valueValue));

                                        break;
                                    }
                                    case STRUCT:
                                        // Ignore STRUCT
                                        break tokens;
                                    case FLAGS:
                                        String flagName = tokens[++j];

                                        if (flagName.equals("internal")) {
                                            // Disregard the entire file
                                            break file;
                                        }
                                        break;
                                    default:
                                        // Ignore unknown tokens
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load FreeRADIUS dictionary", e);
        }

        typeAttributeDefinitionsMap.forEach((attributeType, attributeDefinition) ->
                nameAttributeDefinitionsMap.put(attributeDefinition.getName().toLowerCase(Locale.ROOT),
                        attributeDefinition));
    }

    public FreeRadiusDictionary() {

    }

    private static List<String> getDictionaryLines(String file) throws IOException {
        String filePath = RESOURCE_BASE_PATH + file;

        InputStream inputStream = FreeRadiusDictionary.class.getClassLoader().getResourceAsStream(filePath);

        if (inputStream == null) {
            throw new FileNotFoundException("Resource file not found: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> lines = new ArrayList<>();

            while (reader.ready()) {
                String line = reader.readLine().trim().replaceAll("\\s+", " ");

                if (line.contains("#")) {
                    line = line.substring(0, line.indexOf("#")).trim();
                }

                if (line.length() > 0) {
                    lines.add(line);
                }
            }

            return lines;
        }
    }

    private static DataType typeFromString(String type) {
        if (type.contains("[")) {
            // Turn attributes with size limits (e.g., octets[24]) into the simplified attribute
            type = type.substring(0, type.indexOf("["));
        }

        switch (type) {
            case "byte":
                return DataType.STRING;
            case "combo-ip":
                return DataType.STRING;
            case "date":
                return DataType.STRING;
            case "ether":
                return DataType.STRING;
            case "ifid":
                return DataType.IFID;
            case "integer":
                return DataType.INTEGER;
            case "integer64":
                return DataType.INTEGER_64;
            case "ipaddr":
                return DataType.IPV4_ADDR;
            case "ipv4prefix":
                return DataType.IPV4_PREFIX;
            case "ipv6addr":
                return DataType.IPV6_ADDR;
            case "ipv6prefix":
                return DataType.IPV6_PREFIX;
            case "octets":
                return DataType.STRING;
            case "short":
                return DataType.INTEGER;
            case "signed":
                return DataType.INTEGER;
            case "string":
                return DataType.TEXT;
            case "struct":
                return DataType.STRING;
            case "time_delta":
                return DataType.STRING;
            case "tlv":
                return DataType.STRING;
            case "uint32":
                return DataType.INTEGER;
            case "vsa":
                return DataType.VSA;
        }

        return DataType.UNKNOWN;
    }

    @Override
    public PacketDefinition getPacketDefinition(int code) {
        return null;
    }

    @Override
    public AttributeDefinition<?, ?> getAttributeDefinition(AttributeType type) {
        Objects.requireNonNull(type);

        return typeAttributeDefinitionsMap.getOrDefault(type, null);
    }

    @Override
    public AttributeDefinition<?, ?> getAttributeDefinition(String name) {
        Objects.requireNonNull(name);

        return nameAttributeDefinitionsMap.getOrDefault(name.toLowerCase(Locale.ROOT), null);
    }

    @Override
    public Integer getNumericAttributeValue(AttributeType type, String name) {
        Map<String, Integer> valueMap = numericAttributeValueMap.get(type);

        if (valueMap == null) {
            return null;
        }

        return valueMap.getOrDefault(name.toLowerCase(Locale.ROOT), null);
    }

    @Override
    public TlvDefinition getTlvDefinition(AttributeType type) {
        Objects.requireNonNull(type);

        return tlvDefinitionsMap.getOrDefault(type, null);
    }

    private enum DataType {
        CONCAT,
        ENUM,
        EVS,
        EXTENDED,
        IFID,
        INTEGER_64,
        INTEGER,
        IPV4_ADDR,
        IPV4_PREFIX,
        IPV6_ADDR,
        IPV6_PREFIX,
        LONG_EXTENDED,
        OPTIONAL_TAGGED_STRING,
        STRING,
        TAGGED_INTEGER,
        TAGGED_STRING,
        TEXT,
        TIME,
        TLV,
        UNKNOWN,
        VSA
    }

}
