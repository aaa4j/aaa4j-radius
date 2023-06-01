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

import org.aaa4j.radius.core.attribute.Attribute;
import org.aaa4j.radius.core.attribute.EnumData;
import org.aaa4j.radius.core.attribute.Integer64Data;
import org.aaa4j.radius.core.attribute.IntegerData;
import org.aaa4j.radius.core.attribute.StringData;
import org.aaa4j.radius.core.attribute.TextData;
import org.aaa4j.radius.core.dictionary.AttributeDefinition;
import org.aaa4j.radius.core.dictionary.AttributeFactory;
import org.aaa4j.radius.core.dictionary.Dictionary;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for creating {@link Attribute}s using FreeRADIUS-formatted attribute names and values.
 */
public final class FreeRadiusAttributeFactory {

    private static final Pattern HEX_PATTERN = Pattern.compile("^0x([a-f0-9]+)$");

    private final Dictionary dictionary;

    /**
     * Constructs a FreeRadiusAttributeFactory.
     *
     * @param dictionary the dictionary to use
     */
    public FreeRadiusAttributeFactory(Dictionary dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    /**
     * Builds an attribute from the given attribute name and value. The name and value use FreeRADIUS formatting.
     *
     * @param name the attribute name (e.g., "Cisco-Disconnect-Cause")
     * @param value the attribute value (e.g., "No-Modem-Available" or "9")
     *
     * @return an attribute or null if no attribute could be found in the dictionary
     *
     * @throws ValueFormatException if the value is invalid for the attribute
     */
    @SuppressWarnings("unchecked")
    public Attribute<?> build(String name, String value) throws ValueFormatException {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        AttributeDefinition<?, ?> attributeDefinition =
                dictionary.getAttributeDefinition(name.toLowerCase(Locale.ROOT));

        if (attributeDefinition == null) {
            return null;
        }

        if (attributeDefinition.getDataClass().equals(EnumData.class)) {
            Integer numericAttributeValue =
                    dictionary.getNumericAttributeValue(attributeDefinition.getIdentifier(),
                            value.toUpperCase(Locale.ROOT));

            try {
                EnumData data = numericAttributeValue != null
                        ? new EnumData(numericAttributeValue)
                        : new EnumData(new BigInteger(value).intValueExact());

                AttributeFactory<EnumData> attributeFactory =
                        (AttributeFactory<EnumData>) attributeDefinition.getAttributeFactory();

                return attributeFactory.build(data);
            }
            catch (IllegalArgumentException | ArithmeticException e) {
                throw new ValueFormatException("Value is not a valid enum value: " + value);
            }
        }
        else if (attributeDefinition.getDataClass().equals(Integer64Data.class)) {
            try {
                Integer64Data data = new Integer64Data(new BigInteger(value).longValueExact());

                AttributeFactory<Integer64Data> attributeFactory =
                        (AttributeFactory<Integer64Data>) attributeDefinition.getAttributeFactory();

                return attributeFactory.build(data);
            }
            catch (IllegalArgumentException | ArithmeticException e) {
                throw new ValueFormatException("Value is not a valid integer64 value: " + value);
            }
        }
        else if (attributeDefinition.getDataClass().equals(IntegerData.class)) {
            // FreeRADIUS uses "integer" data type for enum attributes
            Integer numericAttributeValue =
                    dictionary.getNumericAttributeValue(attributeDefinition.getIdentifier(),
                            value.toUpperCase(Locale.ROOT));

            try {
                IntegerData data = numericAttributeValue != null
                        ? new IntegerData(numericAttributeValue)
                        : new IntegerData(new BigInteger(value).intValueExact());

                AttributeFactory<IntegerData> attributeFactory =
                        (AttributeFactory<IntegerData>) attributeDefinition.getAttributeFactory();

                return attributeFactory.build(data);
            }
            catch (IllegalArgumentException | ArithmeticException e) {
                throw new ValueFormatException("Value is not a valid integer value: " + value);
            }
        }
        else if (attributeDefinition.getDataClass().equals(StringData.class)) {
            try {
                Matcher matcher = HEX_PATTERN.matcher(value);

                if (!matcher.matches() || value.length() % 2 != 0) {
                    throw new ValueFormatException("Value is not a valid string value: " + value);
                }

                byte[] bytes = HexFormat.parseHex(matcher.group(1));

                StringData data = new StringData(bytes);

                AttributeFactory<StringData> attributeFactory =
                        (AttributeFactory<StringData>) attributeDefinition.getAttributeFactory();

                return attributeFactory.build(data);
            }
            catch (IllegalArgumentException e) {
                throw new ValueFormatException("Value is not a valid string value: " + value);
            }
        }
        else if (attributeDefinition.getDataClass().equals(TextData.class)) {
            try {
                TextData data = new TextData(value);

                AttributeFactory<TextData> attributeFactory =
                        (AttributeFactory<TextData>) attributeDefinition.getAttributeFactory();

                return attributeFactory.build(data);
            }
            catch (IllegalArgumentException e) {
                throw new ValueFormatException("Value is not a valid text value: " + value);
            }
        }

        return null;
    }

    /**
     * Exception indicating that the provided attribute value is invalid for the attribute and could not be parsed.
     */
    public static class ValueFormatException extends Exception {

        /**
         * Constructs a ValueFormatException.
         *
         * @param message the exception message
         */
        ValueFormatException(String message) {
            super(message);
        }

    }

    /**
     * Simplified version of {@code java.util.HexFormat} from Java 17+.
     */
    private static class HexFormat {

        private static final byte[] DIGITS = {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1,
                -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        };

        private static int fromHexDigits(CharSequence string, int index) {
            int high = fromHexDigit(string.charAt(index));
            int low = fromHexDigit(string.charAt(index + 1));
            return (high << 4) | low;
        }

        public static int fromHexDigit(int ch) {
            int value;
            if ((ch >>> 8) == 0 && (value = DIGITS[ch]) >= 0) {
                return value;
            }
            throw new NumberFormatException("not a hexadecimal digit: \"" + (char) ch + "\" = " + ch);
        }

        public static byte[] parseHex(CharSequence string) {
            byte[] bytes = new byte[string.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) fromHexDigits(string, i * 2);
            }
            return bytes;
        }

    }

}
