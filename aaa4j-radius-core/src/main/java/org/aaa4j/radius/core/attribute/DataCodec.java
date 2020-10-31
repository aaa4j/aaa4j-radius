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
 * A codec (encoder and decoder) for attribute data.
 *
 * @param <D> the data type that the codec is for
 */
public interface DataCodec<D extends Data> {

    /**
     * Decodes bytes into a data type.
     *
     * @param codecContext the codec context
     * @param bytes the bytes to decode
     * 
     * @return the decoded data or null if the given bytes can not be decoded
     */
    D decode(CodecContext codecContext, byte[] bytes);

    /**
     * Encodes a data type into bytes.
     *
     * @param codecContext the codec context
     * @param data the data to encode
     * 
     * @return byte array of the encoded data
     */
    byte[] encode(CodecContext codecContext, D data);

}
