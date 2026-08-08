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

import org.aaa4j.radius.core.attribute.Ipv4AddrData;
import org.aaa4j.radius.core.attribute.TlvData;

/**
 * 6rdBRIPv4Address (3) TLV, defined in RFC 6930.
 *
 * <p>
 * The IPv4 address of a 6rd border relay. One or more may be present.
 * </p>
 */
public final class Ipv66rdBrIpv4Address extends TlvData.Tlv {

    public static final int TYPE = 3;

    public static final String NAME = "6rdBRIPv4Address";

    public Ipv66rdBrIpv4Address(Ipv4AddrData data) {
        super(TYPE, data);
    }

}
