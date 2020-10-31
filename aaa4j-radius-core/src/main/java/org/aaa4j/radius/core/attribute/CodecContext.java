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

import org.aaa4j.radius.core.util.RandomProvider;

import java.util.Objects;

/**
 * Attribute context used by {@link AttributeCodec} and {@link DataCodec} during encoding and decoding. Contains packet
 * data and functions that an attribute codec would require when encoding and decoding RADIUS attributes.
 */
public final class CodecContext {

    private final byte[] secret;

    private final byte[] requestAuthenticator;

    private final RandomProvider randomProvider;

    /**
     * Constructs a codec context given the provided data.
     *
     * @param secret the RADIUS shared secret bytes to use
     * @param requestAuthenticator the request authenticator bytes
     * @param randomProvider the random provider to use for random number generation
     */
    public CodecContext(byte[] secret, byte[] requestAuthenticator, RandomProvider randomProvider) {
        this.secret = Objects.requireNonNull(secret);
        this.requestAuthenticator = Objects.requireNonNull(requestAuthenticator);
        this.randomProvider = Objects.requireNonNull(randomProvider);
    }

    /**
     * Returns the context secret.
     *
     * @return secret byte array
     */
    public byte[] getSecret() {
        return secret;
    }

    /**
     * Returns the context request authenticator.
     *
     * @return request authenticator byte array
     */
    public byte[] getRequestAuthenticator() {
        return requestAuthenticator;
    }

    /**
     * Returns the context random provider
     *
     * @return random provider
     */
    public RandomProvider getRandomProvider() {
        return randomProvider;
    }

}
