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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * "time" attribute data type. "time" data is mapped to an {@link Instant}.
 */
public final class TimeData extends Data {

    /**
     * The largest number of seconds since the epoch representable in 32 unsigned bits.
     */
    private static final long MAX_EPOCH_SECOND = 4294967295L;

    private final Instant value;

    /**
     * Constructs time data from a given instant. "time" data has one-second resolution, so the instant is truncated to
     * second precision and any sub-second component of the given instant is discarded.
     *
     * @param value the instant (must be in range [1970-01-01T00:00:00Z, 2106-02-07T06:28:15Z])
     */
    public TimeData(Instant value) {
        Objects.requireNonNull(value);

        if (value.getEpochSecond() < 0 || value.getEpochSecond() > MAX_EPOCH_SECOND) {
            throw new IllegalArgumentException(
                    "Instant must be in range [1970-01-01T00:00:00Z, 2106-02-07T06:28:15Z]");
        }

        this.value = value.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public int length() {
        return 4;
    }

    /**
     * Gets the time value.
     *
     * @return the instant, at second precision
     */
    public Instant getValue() {
        return value;
    }

    /**
     * A codec for "time" data.
     */
    public static final class Codec implements DataCodec<TimeData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public TimeData decode(CodecContext codecContext, AttributeType parentAttributeType, byte[] bytes) {
            if (bytes.length != 4) {
                return null;
            }

            long value = (bytes[0] & 0xffL) << 24
                    | (bytes[1] & 0xffL) << 16
                    | (bytes[2] & 0xffL) << 8
                    | (bytes[3] & 0xffL);

            return new TimeData(Instant.ofEpochSecond(value));
        }

        @Override
        public byte[] encode(CodecContext codecContext, AttributeType parentAttributeType, Data data) {
            TimeData timeData = (TimeData) data;

            byte[] bytes = new byte[4];

            long value = timeData.value.getEpochSecond();

            bytes[0] = (byte) ((value & 0xff000000L) >>> 24);
            bytes[1] = (byte) ((value & 0x00ff0000L) >>> 16);
            bytes[2] = (byte) ((value & 0x0000ff00L) >>> 8);
            bytes[3] = (byte) (value & 0x000000ffL);

            return bytes;
        }

    }

}
