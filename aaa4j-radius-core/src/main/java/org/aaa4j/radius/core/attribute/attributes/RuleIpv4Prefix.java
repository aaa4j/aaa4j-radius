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

import org.aaa4j.radius.core.attribute.Ipv4PrefixData;
import org.aaa4j.radius.core.attribute.TlvData;

/**
 * Rule-IPv4-Prefix (11) TLV, defined in RFC 8658.
 *
 * <p>
 * The IPv4 prefix of a mapping rule.
 * </p>
 */
public final class RuleIpv4Prefix extends TlvData.Tlv {

    public static final int TYPE = 11;

    public static final String NAME = "Rule-IPv4-Prefix";

    public RuleIpv4Prefix(Ipv4PrefixData data) {
        super(TYPE, data);
    }

}
