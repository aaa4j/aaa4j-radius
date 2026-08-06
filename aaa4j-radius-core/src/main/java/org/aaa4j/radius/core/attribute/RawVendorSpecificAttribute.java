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

/**
 * A vendor-specific attribute whose data could not be decoded, either because the dictionary has no definition for the
 * vendor type or because the data codec rejected the value. The undecoded octets are retained so that the attribute
 * re-encodes to the octets it arrived as.
 */
public final class RawVendorSpecificAttribute extends VendorSpecificAttribute<RawData> {

    /**
     * Constructs a raw vendor specific attribute from an attribute type, vendor id, vendor type, and raw data.
     *
     * @param type the attribute type (integer in range [0, 255])
     * @param vendorId the vendor identifier (integer in range [0, 16777215])
     * @param vendorType the vendor type (integer in range [0, 255])
     * @param data the raw attribute data
     */
    public RawVendorSpecificAttribute(int type, int vendorId, int vendorType, RawData data) {
        super(type, vendorId, vendorType, data);
    }

}
