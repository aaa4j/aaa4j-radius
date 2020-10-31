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
import org.aaa4j.radius.core.attribute.Ipv4Data;
import org.aaa4j.radius.core.attribute.StandardAttribute;

/**
 * NAS-IP-Address (4) attribute.
 */
public final class NasIpAddress extends StandardAttribute<Ipv4Data> {

    public static final AttributeType TYPE = new AttributeType(4);

    public static final String NAME = "NAS-IP-Address";

    public NasIpAddress(Ipv4Data data) {
        super(TYPE.head(), data);
    }

}
