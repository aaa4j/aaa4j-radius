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
 * Strategy for retransmitting RADIUS packets that may have been lost in transit.
 */
public interface RetransmissionStrategy {

    /**
     * Gets the maximum number of transmission attempts. The value <code>1</code> means that only one attempt will be
     * made (i.e., there will be no retransmissions if there is no response packet within the attempt timeout).
     *
     * @return maximum number of transmission attempts a client should attempt
     */
    int getMaxAttempts();

    /**
     * Gets the timeout duration that a client should wait before timing out.
     *
     * @param attempt the attempt number (starting at <code>0</code>)
     * 
     * @return the timeout duration to wait for the given attempt
     */
    Duration timeoutForAttempt(int attempt);

}
