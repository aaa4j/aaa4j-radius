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
import org.aaa4j.radius.core.attribute.DataCodec;

import java.util.Objects;

public class TlvDefinition {

    private final AttributeType type;

    private final String name;

    private final DataCodec<?> dataCodec;

    private final TlvFactory tlvFactory;

    /**
     * Constructs an attribute definition from the given definition parameters.
     *
     * @param type the attribute type (i.e., the unique "dotted" attribute number)
     * @param name the attribute name
     * @param dataCodec the attribute codec used to encode and decode the attribute
     */
    public TlvDefinition(AttributeType type, String name, DataCodec<?> dataCodec) {
        this(type, name, dataCodec, null);
    }

    /**
     * Constructs an attribute definition from the given definition parameters and a factory building the TLV.
     *
     * @param type the attribute type (i.e., the unique "dotted" attribute number)
     * @param name the attribute name
     * @param dataCodec the attribute codec used to encode and decode the attribute
     * @param tlvFactory the factory used to create instances of this TLV, or null to decode into
     * {@link org.aaa4j.radius.core.attribute.TlvData.Tlv}
     */
    public TlvDefinition(AttributeType type, String name, DataCodec<?> dataCodec, TlvFactory tlvFactory) {
        this.type = Objects.requireNonNull(type);
        this.name = Objects.requireNonNull(name);
        this.dataCodec = Objects.requireNonNull(dataCodec);
        this.tlvFactory = tlvFactory;
    }

    /**
     * Gets the TLV identifier.
     *
     * @return the TLV identifier
     */
    public AttributeType getIdentifier() {
        return type;
    }

    /**
     * Gets the TLV name.
     *
     * @return the TLV name
     */
    public String getName() {
        return name;
    }

    public DataCodec<?> getDataCodec() {
        return dataCodec;
    }

    /**
     * Gets the factory used to create instances of this TLV.
     *
     * @return the factory, or null if the TLV has no concrete type
     */
    public TlvFactory getTlvFactory() {
        return tlvFactory;
    }

}
