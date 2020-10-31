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

package org.aaa4j.radius.client;

import java.time.Duration;

/**
 * A retransmission strategy that performs a fixed number of attempts with a fixed timeout between the attempts.
 */
public final class IntervalRetransmissionStrategy implements RetransmissionStrategy {

    private final int maxAttempts;

    private final Duration timeout;

    /**
     * Constructs a new retransmission strategy with the given parameters.
     *
     * @param maxAttempts the total maximum number of attempts
     * @param timeout the timeout duration in between attempts
     */
    public IntervalRetransmissionStrategy(int maxAttempts, Duration timeout) {
        this.maxAttempts = maxAttempts;
        this.timeout = timeout;
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    @Override
    public Duration timeoutForAttempt(int attempt) {
        if (attempt < 0 || attempt >= maxAttempts) {
            throw new IllegalArgumentException("Attempt " + attempt + " is outside of the valid range");
        }

        return timeout;
    }

}
