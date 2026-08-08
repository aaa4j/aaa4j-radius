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
 * Softwire46-MAP-T (2) TLV, defined in RFC 8658.
 *
 * <p>
 * A MAP-T configuration: one basic mapping rule, any forwarding mapping rules, a default mapping rule and optional port
 * parameters.
 * </p>
 */
public final class Softwire46MapT extends TlvData.Tlv {

    public static final int TYPE = 2;

    public static final String NAME = "Softwire46-MAP-T";

    public Softwire46MapT(TlvData data) {
        super(TYPE, data);
    }

}
