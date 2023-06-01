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
import org.aaa4j.radius.core.attribute.EnumData;
import org.aaa4j.radius.core.attribute.StandardAttribute;

/**
 * Ingress-Filters (57) attribute.
 */
public final class IngressFilters extends StandardAttribute<EnumData> {

    public static final AttributeType TYPE = new AttributeType(57);

    public static final String NAME = "Ingress-Filters";

    public IngressFilters(EnumData data) {
        super(TYPE.head(), data);
    }

}
