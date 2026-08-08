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

import org.aaa4j.radius.core.attribute.TlvData;

/**
 * Softwire46-V4V6Bind (8) TLV, defined in RFC 8658.
 *
 * <p>
 * The binding of an IPv4 address to an IPv6 prefix, used by Lightweight 4over6.
 * </p>
 */
public final class Softwire46V4v6Bind extends TlvData.Tlv {

    public static final int TYPE = 8;

    public static final String NAME = "Softwire46-V4V6Bind";

    public Softwire46V4v6Bind(TlvData data) {
        super(TYPE, data);
    }

}
