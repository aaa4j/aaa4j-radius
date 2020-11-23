/*
 * Copyright 2020 The AAA4J Authors
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

import java.util.Arrays;
import java.util.Objects;

/**
 * "extended" attribute data type. Extended data contains a nested attribute in the extended attribute space.
 */
public class ExtendedData extends ContainerData {

    private final int extendedType;

    private final byte[] extData;

    /**
     * Constructs extended data from a given extended attribute type and extended data byte array.
     *
     * @param extendedType the extended type
     * @param extData the extended data
     */
    public ExtendedData(int extendedType, byte[] extData) {
        if (extendedType < 0 || extendedType > 255) {
            throw new IllegalArgumentException("Extended type must be in range [0, 255]");
        }

        Objects.requireNonNull(extData);

        this.extendedType = extendedType;
        this.extData = Arrays.copyOf(extData, extData.length);
    }

    @Override
    public int length() {
        return 1 + extData.length;
    }

    /**
     * Get the extended type. For example, if this data contains a Frag-Status (241.1) attribute then the extended type
     * will be 1.
     *
     * @return the extended type
     */
    public int getExtendedType() {
        return extendedType;
    }

    /**
     * Gets the extended data.
     *
     * @return the extended data as a byte array
     */
    public byte[] getExtData() {
        return extData;
    }

    @Override
    public int[] getContainedType() {
        return new int[] { extendedType };
    }

    /**
     * A codec for "extended" data.
     */
    public static final class Codec implements DataCodec<ExtendedData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public ExtendedData decode(CodecContext codecContext, byte[] bytes) {
            if (bytes.length == 0) {
                return null;
            }

            int extendedType = bytes[0] & 0xff;
            byte[] extData = new byte[bytes.length - 1];

            System.arraycopy(bytes, 1, extData, 0, bytes.length - 1);

            return new ExtendedData(extendedType, extData);
        }

        @Override
        public byte[] encode(CodecContext codecContext, ExtendedData data) {
            byte[] bytes = new byte[data.extData.length + 1];

            bytes[0] = (byte) data.extendedType;
            System.arraycopy(data.extData, 0, bytes, 1, data.extData.length);

            return bytes;
        }

    }

}