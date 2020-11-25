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

import java.util.Arrays;
import java.util.Objects;

/**
 * "long-extended" attribute data type. Long extended data encapsulates an attribute in the long extended attribute
 * space, allowing the transport of attributes with more than 253 bytes of data.
 */
public class LongExtendedData extends ContainerData {

    private final int extendedType;

    private final byte[] extData;

    private final boolean more;

    private final boolean truncation;

    /**
     * Constructs long extended data.
     *
     * @param extendedType the extended type
     * @param extData the extended data
     * @param more more boolean flag
     * @param truncation truncation boolean flag
     */
    public LongExtendedData(int extendedType, byte[] extData, boolean more, boolean truncation) {
        if (extendedType < 0 || extendedType > 255) {
            throw new IllegalArgumentException("Extended type must be in range [0, 255]");
        }

        Objects.requireNonNull(extData);

        this.extendedType = extendedType;
        this.extData = extData;
        this.more = more;
        this.truncation = truncation;
    }

    @Override
    public int length() {
        return 2 + extData.length;
    }

    @Override
    public int[] getContainedType() {
        return new int[] { extendedType };
    }

    /**
     * Gets the extended type.
     *
     * @return the extended type
     */
    public int getExtendedType() {
        return extendedType;
    }

    /**
     * Gets the extended data.
     *
     * @return the extended data byte array
     */
    public byte[] getExtData() {
        return extData;
    }

    /**
     * Gets the more flag.
     *
     * @return the more flag
     */
    public boolean hasMore() {
        return more;
    }

    /**
     * Gets the truncation flag.
     *
     * @return the truncation flag
     */
    public boolean isTruncated() {
        return truncation;
    }

    /**
     * A codec for "long-extended" data.
     */
    public static final class Codec implements DataCodec<LongExtendedData> {

        /**
         * An instance of {@link Codec}.
         */
        public static final Codec INSTANCE = new Codec();

        @Override
        public LongExtendedData decode(CodecContext codecContext, byte[] bytes) {
            if (bytes.length < 2) {
                return null;
            }

            int extendedType = bytes[0] & 0xff;
            byte[] extData = Arrays.copyOfRange(bytes, 2, bytes.length);
            boolean more = (bytes[1] & 0x80) == 0x80;
            boolean truncation = (bytes[1] & 0x40) == 0x40;

            return new LongExtendedData(extendedType, extData, more, truncation);
        }

        @Override
        public byte[] encode(CodecContext codecContext, LongExtendedData data) {
            byte[] bytes = new byte[data.extData.length + 2];

            bytes[0] = (byte) data.extendedType;
            bytes[1] = (byte) ((data.more ? 0x80 : 0x00) | (data.truncation ? 0x40 : 0x00));
            System.arraycopy(data.extData, 0, bytes, 2, data.extData.length);

            return bytes;
        }

    }

}