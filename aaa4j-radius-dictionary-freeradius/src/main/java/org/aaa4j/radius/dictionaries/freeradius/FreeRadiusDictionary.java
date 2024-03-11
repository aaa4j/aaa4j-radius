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
import org.aaa4j.radius.core.attribute.DataCodec;
import org.aaa4j.radius.core.attribute.EnumData;
import org.aaa4j.radius.core.attribute.IfidData;
import org.aaa4j.radius.core.attribute.Integer64Data;
import org.aaa4j.radius.core.attribute.IntegerData;
import org.aaa4j.radius.core.attribute.Ipv4AddrData;
import org.aaa4j.radius.core.attribute.Ipv4PrefixData;
import org.aaa4j.radius.core.attribute.Ipv6AddrData;
import org.aaa4j.radius.core.attribute.Ipv6PrefixData;
import org.aaa4j.radius.core.attribute.OptionalTaggedStringData;
import org.aaa4j.radius.core.attribute.OptionalTaggedTextData;
import org.aaa4j.radius.core.attribute.StandardAttribute;
import org.aaa4j.radius.core.attribute.StringData;
import org.aaa4j.radius.core.attribute.TaggedIntegerData;
import org.aaa4j.radius.core.attribute.TaggedStringData;
import org.aaa4j.radius.core.attribute.TaggedTextData;
import org.aaa4j.radius.core.attribute.TextData;
import org.aaa4j.radius.core.attribute.TunnelPasswordDataFilter;
import org.aaa4j.radius.core.attribute.UserPasswordDataFilter;
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

                                            if (!DIGITS_PATTERN.asPredicate().test(attributeId)) {
                                                // Only consider non-nested attributes
                                                break tokens;
                                            }

                                            boolean hasTag = false;
                                            boolean usesUserPasswordEncryption = false;
                                            boolean usesTunnelPasswordEncryption = false;

                                            if (tokens.length > j + 1) {
                                                String modifiers = tokens[++j];

                                                hasTag = modifiers.contains("has_tag");
                                                usesUserPasswordEncryption = modifiers.contains("encrypt=1");
                                                usesTunnelPasswordEncryption = modifiers.contains("encrypt=2");
                                            }

                                            boolean usesEncryption = usesUserPasswordEncryption
                                                    || usesTunnelPasswordEncryption;

                                            int intAttributeId = Integer.parseInt(attributeId);

                                            AttributeType attributeType = new AttributeType(intAttributeId);
                                            String fullName = String.format("%s", attributeName);

                                            switch (typeFromString(typeLiteral, hasTag, usesEncryption)) {
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
                                                    DataCodec<IntegerData> dataCodec = IntegerData.Codec.INSTANCE;

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new IntegerData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    IntegerData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            dataCodec,
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
                                                    DataCodec<Ipv4AddrData> dataCodec = Ipv4AddrData.Codec.INSTANCE;

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new Ipv4AddrData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv4AddrData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            dataCodec,
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
                                                case OPTIONAL_TAGGED_STRING: {

                                                }
                                                break;
                                                case OPTIONAL_TAGGED_TEXT: {

                                                }
                                                break;
                                                case STRING: {
                                                    DataCodec<StringData> dataCodec = StringData.Codec.INSTANCE;

                                                    if (usesUserPasswordEncryption) {
                                                        dataCodec = new StringData.Codec(UserPasswordDataFilter.INSTANCE);
                                                    }

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new StringData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    StringData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            dataCodec,
                                                                            StandardAttribute::new),
                                                                    (StringData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case TAGGED_INTEGER: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TaggedIntegerData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            TaggedIntegerData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (TaggedIntegerData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case TAGGED_STRING: {
                                                    DataCodec<TaggedStringData> dataCodec = TaggedStringData.Codec.INSTANCE;

                                                    if (usesUserPasswordEncryption) {
                                                        dataCodec = new TaggedStringData.Codec(UserPasswordDataFilter.INSTANCE);
                                                    }

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new TaggedStringData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TaggedStringData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            dataCodec,
                                                                            StandardAttribute::new),
                                                                    (TaggedStringData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case TAGGED_TEXT: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TaggedTextData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            TaggedTextData.Codec.INSTANCE,
                                                                            StandardAttribute::new),
                                                                    (TaggedTextData data) ->
                                                                            new StandardAttribute<>(intAttributeId,
                                                                                    data));

                                                    typeAttributeDefinitionsMap.put(attributeType, attributeDefinition);
                                                    nameAttributeDefinitionsMap.put(fullName.toLowerCase(Locale.ROOT),
                                                            attributeDefinition);
                                                }
                                                break;
                                                case TEXT: {
                                                    DataCodec<TextData> dataCodec = TextData.Codec.INSTANCE;

                                                    if (usesUserPasswordEncryption) {
                                                        dataCodec = new TextData.Codec(UserPasswordDataFilter.INSTANCE);
                                                    }

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new TextData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TextData.class,
                                                                    new StandardAttribute.Codec<>(
                                                                            dataCodec,
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

                                            boolean hasTag = false;
                                            boolean usesUserPasswordEncryption = false;
                                            boolean usesTunnelPasswordEncryption = false;

                                            if (tokens.length > j + 1) {
                                                String modifiers = tokens[++j];

                                                hasTag = modifiers.contains("has_tag");
                                                usesUserPasswordEncryption = modifiers.contains("encrypt=1");
                                                usesTunnelPasswordEncryption = modifiers.contains("encrypt=2");
                                            }

                                            boolean usesEncryption = usesUserPasswordEncryption
                                                    || usesTunnelPasswordEncryption;

                                            if (!DIGITS_PATTERN.asPredicate().test(attributeId)) {
                                                // Only consider non-nested attributes
                                                break tokens;
                                            }

                                            int intAttributeId = Integer.parseInt(attributeId);

                                            AttributeType attributeType = new AttributeType(VENDOR_SPECIFIC_ATTRIBUTE,
                                                    currentVendorId, intAttributeId);
                                            String fullName = String.format("%s-%s", currentVendorName, attributeName);
                                            final Integer finalVendorId = currentVendorId;

                                            switch (typeFromString(typeLiteral, hasTag, usesEncryption)) {
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
                                                    DataCodec<IntegerData> dataCodec = IntegerData.Codec.INSTANCE;

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new IntegerData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    IntegerData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            dataCodec,
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
                                                    DataCodec<Ipv4AddrData> dataCodec = Ipv4AddrData.Codec.INSTANCE;

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new Ipv4AddrData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    Ipv4AddrData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            dataCodec,
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
                                                case OPTIONAL_TAGGED_STRING: {
                                                    DataCodec<OptionalTaggedStringData> dataCodec = OptionalTaggedStringData.Codec.INSTANCE;

                                                    if (usesUserPasswordEncryption) {
                                                        dataCodec = new OptionalTaggedStringData.Codec(UserPasswordDataFilter.INSTANCE);
                                                    }

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new OptionalTaggedStringData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    OptionalTaggedStringData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            dataCodec,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (OptionalTaggedStringData data) ->
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
                                                case OPTIONAL_TAGGED_TEXT: {
                                                    DataCodec<OptionalTaggedTextData> dataCodec = OptionalTaggedTextData.Codec.INSTANCE;

                                                    if (usesUserPasswordEncryption) {
                                                        dataCodec = new OptionalTaggedTextData.Codec(UserPasswordDataFilter.INSTANCE);
                                                    }

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new OptionalTaggedTextData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    OptionalTaggedTextData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            dataCodec,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (OptionalTaggedTextData data) ->
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
                                                case STRING: {
                                                    DataCodec<StringData> dataCodec = StringData.Codec.INSTANCE;

                                                    if (usesUserPasswordEncryption) {
                                                        dataCodec = new StringData.Codec(UserPasswordDataFilter.INSTANCE);
                                                    }

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new StringData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    StringData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            dataCodec,
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
                                                case TAGGED_INTEGER: {
                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TaggedIntegerData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            TaggedIntegerData.Codec.INSTANCE,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (TaggedIntegerData data) ->
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
                                                case TAGGED_STRING: {
                                                    DataCodec<TaggedStringData> dataCodec = TaggedStringData.Codec.INSTANCE;

                                                    if (usesUserPasswordEncryption) {
                                                        dataCodec = new TaggedStringData.Codec(UserPasswordDataFilter.INSTANCE);
                                                    }

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new TaggedStringData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TaggedStringData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            dataCodec,
                                                                            (type, vendorId, vendorType, data) ->
                                                                                    new VendorSpecificAttribute<>(
                                                                                            VENDOR_SPECIFIC_ATTRIBUTE,
                                                                                            vendorId,
                                                                                            vendorType, data)),
                                                                    (TaggedStringData data) ->
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
                                                case TAGGED_TEXT: {
                                                    DataCodec<TaggedTextData> dataCodec = TaggedTextData.Codec.INSTANCE;

                                                    if (usesUserPasswordEncryption) {
                                                        dataCodec = new TaggedTextData.Codec(UserPasswordDataFilter.INSTANCE);
                                                    }

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new TaggedTextData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TextData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            dataCodec,
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
                                                case TEXT: {
                                                    DataCodec<TextData> dataCodec = TextData.Codec.INSTANCE;

                                                    if (usesUserPasswordEncryption) {
                                                        dataCodec = new TextData.Codec(UserPasswordDataFilter.INSTANCE);
                                                    }

                                                    if (usesTunnelPasswordEncryption) {
                                                        dataCodec = new TextData.Codec(TunnelPasswordDataFilter.INSTANCE);
                                                    }

                                                    AttributeDefinition<?, ?> attributeDefinition =
                                                            new AttributeDefinition<>(
                                                                    attributeType,
                                                                    fullName,
                                                                    VendorSpecificAttribute.class,
                                                                    TextData.class,
                                                                    new VendorSpecificAttribute.Codec<>(
                                                                            dataCodec,
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

    private static DataType typeFromString(String type, boolean hasTag, boolean hasEncryption) {
        if (type.contains("[")) {
            // Turn attributes with size limits (e.g., octets[24]) into the simplified attribute
            type = type.substring(0, type.indexOf("["));
        }

        DataType dataType = DataType.UNKNOWN;

        switch (type) {
            case "byte":
                dataType = DataType.STRING;
                break;
            case "combo-ip":
                dataType = DataType.STRING;
                break;
            case "date":
                dataType = DataType.STRING;
                break;
            case "ether":
                dataType = DataType.STRING;
                break;
            case "ifid":
                dataType = DataType.IFID;
                break;
            case "integer":
                dataType = DataType.INTEGER;
                break;
            case "integer64":
                dataType = DataType.INTEGER_64;
                break;
            case "ipaddr":
                dataType = DataType.IPV4_ADDR;
                break;
            case "ipv4prefix":
                dataType = DataType.IPV4_PREFIX;
                break;
            case "ipv6addr":
                dataType = DataType.IPV6_ADDR;
                break;
            case "ipv6prefix":
                dataType = DataType.IPV6_PREFIX;
                break;
            case "octets":
                dataType = DataType.STRING;
                break;
            case "short":
                dataType = DataType.INTEGER;
                break;
            case "signed":
                dataType = DataType.INTEGER;
                break;
            case "string":
                dataType = DataType.TEXT;
                break;
            case "struct":
                dataType = DataType.STRING;
                break;
            case "time_delta":
                dataType = DataType.STRING;
                break;
            case "tlv":
                dataType = DataType.STRING;
                break;
            case "uint32":
                dataType = DataType.INTEGER;
                break;
            case "vsa":
                dataType = DataType.VSA;
                break;
        }

        switch (dataType) {
            case INTEGER: {
                if (hasTag) {
                    dataType = DataType.TAGGED_INTEGER;
                }
            }
            break;
            case STRING: {
                if (hasTag) {
                    dataType = hasEncryption ? DataType.TAGGED_STRING : DataType.OPTIONAL_TAGGED_STRING;
                }
            }
            break;
            case TEXT: {
                if (hasTag) {
                    dataType = hasEncryption ? DataType.TAGGED_TEXT : DataType.OPTIONAL_TAGGED_TEXT;
                }
            }
            break;
        }

        return dataType;
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
        OPTIONAL_TAGGED_TEXT,
        STRING,
        TAGGED_INTEGER,
        TAGGED_STRING,
        TAGGED_TEXT,
        TEXT,
        TIME,
        TLV,
        UNKNOWN,
        VSA
    }

}
