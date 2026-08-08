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

import org.aaa4j.radius.core.attribute.IntegerData;
import org.aaa4j.radius.core.attribute.TlvData;

/**
 * IPv4MaskLen (1) TLV, defined in RFC 6930.
 *
 * <p>
 * The number of high-order bits that are identical across all CE IPv4 addresses in the 6rd domain.
 * </p>
 */
public final class Ipv66rdIpv4MaskLen extends TlvData.Tlv {

    public static final int TYPE = 1;

    public static final String NAME = "IPv4MaskLen";

    public Ipv66rdIpv4MaskLen(IntegerData data) {
        super(TYPE, data);
    }

}
