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

package org.aaa4j.radius.core.packet;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An identifier generator that increments the packet identifier.
 */
public class IncrementingPacketIdGenerator implements PacketIdGenerator {

    private final AtomicInteger counter;

    /**
     * Constructs a new {@link IncrementingPacketIdGenerator} with the provided initial value.
     *
     * @param initial the initial identifier in range [0, 255]
     */
    public IncrementingPacketIdGenerator(int initial) {
        if (initial < 0 || initial > 255) {
            throw new IllegalArgumentException("Initial id must be in range [0, 255]");
        }

        this.counter = new AtomicInteger(initial);
    }

    @Override
    public int nextId() {
        return counter.getAndUpdate(value -> (value + 1) % 256);
    }

}
