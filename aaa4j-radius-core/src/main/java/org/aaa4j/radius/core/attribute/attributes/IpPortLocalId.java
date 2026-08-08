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

import org.aaa4j.radius.core.attribute.StringData;
import org.aaa4j.radius.core.attribute.TlvData;

/**
 * IP-Port-Local-Id (11) TLV, defined in RFC 8045.
 *
 * <p>
 * A local identifier for the subscriber the ports belong to.
 * </p>
 */
public final class IpPortLocalId extends TlvData.Tlv {

    public static final int TYPE = 11;

    public static final String NAME = "IP-Port-Local-Id";

    public IpPortLocalId(StringData data) {
        super(TYPE, data);
    }

}
