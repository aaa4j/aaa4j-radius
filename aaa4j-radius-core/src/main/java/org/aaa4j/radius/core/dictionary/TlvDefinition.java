package org.aaa4j.radius.core.dictionary;

import org.aaa4j.radius.core.attribute.AttributeType;
import org.aaa4j.radius.core.attribute.DataCodec;

import java.util.Objects;

public class TlvDefinition {

    private final AttributeType type;

    private final String name;

    private final DataCodec<?> dataCodec;

    /**
     * Constructs an attribute definition from the given definition parameters.
     *
     * @param type the attribute type (i.e., the unique "dotted" attribute number)
     * @param name the attribute name
     * @param dataCodec the attribute codec used to encode and decode the attribute
     */
    public TlvDefinition(AttributeType type, String name, DataCodec<?> dataCodec) {
        this.type = Objects.requireNonNull(type);
        this.name = Objects.requireNonNull(name);
        this.dataCodec = Objects.requireNonNull(dataCodec);
    }

    public DataCodec<?> getDataCodec() {
        return dataCodec;
    }

}
