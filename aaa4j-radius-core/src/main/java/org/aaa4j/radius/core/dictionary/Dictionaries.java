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

package org.aaa4j.radius.core.dictionary;

import org.aaa4j.radius.core.attribute.AttributeType;

import java.util.Arrays;
import java.util.List;

/**
 * Utilities for {@link Dictionary}. See {@link #of(Dictionary...)} to retrieve a compound dictionary.
 */
public final class Dictionaries {

    private Dictionaries() {

    }

    /**
     * Returns a dictionary that combines the provided dictionaries. If the dictionaries have overlapping definitions,
     * the dictionaries provided first in the list take precedence.
     *
     * @param dictionaries dictionaries
     *
     * @return a dictionary that uses all the provided dictionaries for definition lookups
     */
    public static Dictionary of(Dictionary... dictionaries) {
        return new CompoundDictionary(Arrays.asList(dictionaries));
    }

    private static class CompoundDictionary implements Dictionary {

        List<Dictionary> dictionaries;

        private CompoundDictionary(List<Dictionary> dictionaries) {
            this.dictionaries = dictionaries;
        }

        @Override
        public PacketDefinition getPacketDefinition(int code) {
            for (Dictionary dictionary : dictionaries) {
                PacketDefinition packetDefinition = dictionary.getPacketDefinition(code);

                if (packetDefinition != null) {
                    return packetDefinition;
                }
            }

            return null;
        }

        @Override
        public AttributeDefinition<?, ?> getAttributeDefinition(AttributeType type) {
            for (Dictionary dictionary : dictionaries) {
                AttributeDefinition<?, ?> attributeDefinition = dictionary.getAttributeDefinition(type);

                if (attributeDefinition != null) {
                    return attributeDefinition;
                }
            }

            return null;
        }

        @Override
        public AttributeDefinition<?, ?> getAttributeDefinition(String name) {
            for (Dictionary dictionary : dictionaries) {
                AttributeDefinition<?, ?> attributeDefinition = dictionary.getAttributeDefinition(name);

                if (attributeDefinition != null) {
                    return attributeDefinition;
                }
            }

            return null;
        }

        @Override
        public Integer getNumericAttributeValue(AttributeType type, String name) {
            for (Dictionary dictionary : dictionaries) {
                Integer numericAttributeValue = dictionary.getNumericAttributeValue(type, name);

                if (numericAttributeValue != null) {
                    return numericAttributeValue;
                }
            }

            return null;
        }

        @Override
        public TlvDefinition getTlvDefinition(AttributeType type) {
            for (Dictionary dictionary : dictionaries) {
                TlvDefinition tlvDefinition = dictionary.getTlvDefinition(type);

                if (tlvDefinition != null) {
                    return tlvDefinition;
                }
            }

            return null;
        }

    }

}
