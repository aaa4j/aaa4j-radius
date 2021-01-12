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

package org.aaa4j.radius.core.attribute.attributes;

import org.aaa4j.radius.core.attribute.AttributeType;
import org.aaa4j.radius.core.attribute.EvsData;
import org.aaa4j.radius.core.attribute.LongExtendedAttribute;

/**
 * Extended-Vendor-Specific-5 (245.26) attribute.
 */
public final class ExtendedVendorSpecific5 extends LongExtendedAttribute<EvsData> {

    public static final AttributeType TYPE = new AttributeType(245, 26);

    public static final String NAME = "Extended-Vendor-Specific-5";

    public ExtendedVendorSpecific5(EvsData data) {
        super(TYPE.head(), TYPE.at(1), data);
    }

}
