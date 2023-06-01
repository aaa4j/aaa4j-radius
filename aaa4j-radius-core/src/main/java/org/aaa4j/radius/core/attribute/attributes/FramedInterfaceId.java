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
import org.aaa4j.radius.core.attribute.IfidData;
import org.aaa4j.radius.core.attribute.StandardAttribute;

/**
 * Framed-Interface-Id (96) attribute.
 */
public final class FramedInterfaceId extends StandardAttribute<IfidData> {

    public static final AttributeType TYPE = new AttributeType(96);

    public static final String NAME = "Framed-Interface-Id";

    public FramedInterfaceId(IfidData data) {
        super(TYPE.head(), data);
    }

}
