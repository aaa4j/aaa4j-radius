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

import java.util.Deque;

/**
 * An attribute encoder and decoder. An attribute codec encodes and decodes attributes by manipulating the stack of
 * attributes. When decoding, the codec turns lower-level attributes into higher-level attributes (e.g., from
 * {@link RawAttribute} into a concrete type such as UserName) and when encoding, the codec turns higher-level
 * attributes into lower-level attributes (e.g., from a concrete type such as UserName into {@link RawAttribute}).
 */
public interface AttributeCodec {

    /**
     * Decodes the top attribute(s) from lower-level attributes into higher-level attributes.
     *
     * @param codecContext the codec context
     * @param attributeStack the current attribute stack
     * 
     * @return number of attributes which should not be processed anymore (i.e., because they contain invalid data or
     * because the codec has processed them and knows that they don't contain any encapsulated attributes to parse).
     */
    int decode(CodecContext codecContext, Deque<Attribute<?>> attributeStack);

    /**
     * Encodes the top attribute(s) of the attribute stack into lower-level attributes.
     *
     * @param codecContext the codec context
     * @param attributeStack the current attribute stack
     */
    void encode(CodecContext codecContext, Deque<Attribute<?>> attributeStack);

}
