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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * "concat" attribute data type. Concat data permits the transport of more than 253 bytes of data in the standard
 * attribute space by fragmenting the data across multiple attributes.
 *
 * <p>
 * An attribute containing concat data may be fragmented into multiple attributes when transmitted over the wire. Concat
 * data of the same contiguous attribute is reassembled into a single {@link ConcatData} object when received over the
 * wire.
 * </p>
 */
public final class ConcatData extends Data {

    private final byte[] data;

    private final List<byte[]> fragments;

    /**
     * Constructs concat data from data as a byte array.
     *
     * @param data data byte array
     */
    public ConcatData(byte[] data) {
        Objects.requireNonNull(data);

        if (data.length == 0) {
            throw new IllegalArgumentException("ConcatData data length must be in range [1, +Infinity]");
        }

        this.data = data;

        if (data.length < 254) {
            fragments = Collections.singletonList(data);
        }
        else {
            fragments = new ArrayList<>((data.length - 1) / 253 + 1);

            for (int i = 0; i < data.length;) {
                byte[] fragment = Arrays.copyOfRange(data, i, Math.min(data.length, i + 253));
                fragments.add(fragment);
                i += fragment.length;
            }
        }
    }

    /**
     * Constructs concat data from data byte array fragments. The provided fragments will be sent as is, even if some of
     * the fragments are smaller than a full attribute worth of data (253 bytes).
     *
     * @param fragments list of byte array fragments
     * 
     * @throws IllegalArgumentException if any of the fragment lengths are outside of the range [1, 253]
     */
    public ConcatData(List<byte[]> fragments) {
        Objects.requireNonNull(fragments);

        int dataLength = 0;

        for (byte[] fragment : fragments) {
            if (fragment.length == 0 || fragment.length > 253) {
                throw new IllegalArgumentException("ConcatData fragment length must be in range [1, 253]");
            }

            dataLength += fragment.length;
        }

        byte[] data = new byte[dataLength];

        for (int i = 0, j = 0; i < fragments.size(); i++) {
            byte[] fragment = fragments.get(i);
            System.arraycopy(fragment, 0, data, j, fragment.length);
            j += fragment.length;
        }

        this.data = data;
        this.fragments = new ArrayList<>(fragments);
    }

    @Override
    public int length() {
        return data.length;
    }

    /**
     * Gets the concatenated data.
     *
     * @return the fully-assembled data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the data fragments.
     *
     * @return list of byte array fragments
     */
    public List<byte[]> getFragments() {
        return Collections.unmodifiableList(fragments);
    }

}
