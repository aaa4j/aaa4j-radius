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

package org.aaa4j.radius.core.attribute;

/**
 * Filter for transforming data within attribute {@link Data}.
 */
public interface DataFilter {

    /**
     * Decodes data.
     *
     * @param codecContext the codec context
     * @param data the data to decode
     *
     * @return the decoded data or null if the given bytes can not be decoded
     */
    byte[] decode(CodecContext codecContext, byte[] data);

    /**
     * Encodes data.
     *
     * @param codecContext the codec context
     * @param data the data bytes to encode
     *
     * @return byte array of the encoded data
     */
    byte[] encode(CodecContext codecContext, byte[] data);

}
