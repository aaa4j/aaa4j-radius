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

import java.util.Objects;

/**
 * Octets carried without a typed interpretation. It is the value-level counterpart of {@link RawAttribute}: the octets
 * are retained verbatim so that the attribute carrying them re-encodes to exactly what it arrived as. Raw data appears
 * wherever bytes have no typed meaning at their layer: an undecodable top-level attribute, or an undecodable child of a
 * container data type ("tlv", "vsa", "evs", "extended"). In either case the dictionary has no definition for the value
 * or the data codec rejected it.
 */
public final class RawData extends Data {

    private final byte[] value;

    /**
     * Constructs raw data from a given byte array.
     *
     * @param value the byte array
     */
    public RawData(byte[] value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public int length() {
        return value.length;
    }

    /**
     * Gets the undecoded octets as a byte array.
     *
     * @return the undecoded octets
     */
    public byte[] getValue() {
        return value;
    }

}
