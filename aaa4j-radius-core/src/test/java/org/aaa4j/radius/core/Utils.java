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

package org.aaa4j.radius.core;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public final class Utils {

    private Utils() {
        throw new AssertionError();
    }

    public static byte[] fromHex(String hex) {
        try {
            return Hex.decodeHex(hex);
        }
        catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

}
