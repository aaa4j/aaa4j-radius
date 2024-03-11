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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Data filter for attribute data that uses the Tunnel-Password attribute "encryption" as defined in RFC 2868.
 */
public class TunnelPasswordDataFilter implements DataFilter {

    public static final TunnelPasswordDataFilter INSTANCE = new TunnelPasswordDataFilter();

    @Override
    public byte[] decode(CodecContext codecContext, byte[] data) {
        if (data.length < 18) {
             return null;
        }

        if (((data.length - 2) % 16) != 0) {
            return null;
        }

        byte[] salt = new byte[2];
        System.arraycopy(data, 0, salt, 0, 2);

        if ((salt[0] & 0x80) != 0x80) {
             return null;
        }

        byte[] paddedLengthAndPassword = new byte[data.length - 2];
        byte[] ciphertext = new byte[data.length - 2];

        System.arraycopy(data, 2, ciphertext, 0, data.length - 2);

        MessageDigest md5 = getMd5Instance();

        md5.update(codecContext.getSecret());
        md5.update(codecContext.getRequestAuthenticator());
        md5.update(salt);

        byte[] b = md5.digest();

        for (int i = 0; i < ciphertext.length; i += 16) {
            if (i != 0) {
                md5.update(codecContext.getSecret());
                md5.update(Arrays.copyOfRange(ciphertext, i - 16, i));
                b = md5.digest();
            }

            for (int j = 0; j < 16; j++) {
                paddedLengthAndPassword[i + j] = (byte) (ciphertext[i + j] ^ b[j]);
            }

        }

        int passwordLength = paddedLengthAndPassword[0] & 0xff;

        if (passwordLength > paddedLengthAndPassword.length - 1) {
             return null;
        }

        byte[] password = Arrays.copyOfRange(paddedLengthAndPassword, 1, passwordLength + 1);

        return password;
    }

    @Override
    public byte[] encode(CodecContext codecContext, byte[] data) {
        byte[] salt = new byte[2];
        codecContext.getRandomProvider().nextBytes(salt);

        salt[0] = (byte) (salt[0] | 0x80);

        byte[] paddedLengthAndPassword = new byte[1 + data.length - (data.length % 16) + 15];
        byte[] ciphertext = new byte[paddedLengthAndPassword.length];

        paddedLengthAndPassword[0] = (byte) (data.length & 0xff);
        System.arraycopy(data, 0, paddedLengthAndPassword, 1, data.length);

        MessageDigest md5 = getMd5Instance();

        md5.update(codecContext.getSecret());
        md5.update(codecContext.getRequestAuthenticator());
        md5.update(salt);

        byte[] b = md5.digest();

        for (int i = 0; i < paddedLengthAndPassword.length; i += 16) {
            if (i != 0) {
                md5.update(codecContext.getSecret());
                md5.update(Arrays.copyOfRange(ciphertext, i - 16, i));
                b = md5.digest();
            }

            for (int j = 0; j < 16; j++) {
                ciphertext[i + j] = (byte) (paddedLengthAndPassword[i + j] ^ b[j]);
            }
        }

        byte[] encoded = new byte[2 + ciphertext.length];

        System.arraycopy(salt, 0, encoded, 0, salt.length);
        System.arraycopy(ciphertext, 0, encoded, 2, ciphertext.length);

        return encoded;
    }

    private static MessageDigest getMd5Instance() {
        try {
            return MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            // Shouldn't happen since every JRE Is required to support MD5
            throw new AssertionError(e);
        }
    }

}
