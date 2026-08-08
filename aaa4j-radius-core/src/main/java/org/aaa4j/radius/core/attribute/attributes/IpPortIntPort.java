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
 * IP-Port-Int-Port (6) TLV, defined in RFC 8045.
 *
 * <p>
 * The internal port number of the port mapping.
 * </p>
 */
public final class IpPortIntPort extends TlvData.Tlv {

    public static final int TYPE = 6;

    public static final String NAME = "IP-Port-Int-Port";

    public IpPortIntPort(IntegerData data) {
        super(TYPE, data);
    }

}
