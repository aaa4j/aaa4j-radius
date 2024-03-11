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
 * Data filter for attribute data that uses the User-Password attribute "encryption" as defined in RFC 2865.
 */
public class UserPasswordDataFilter implements DataFilter {

    public static final UserPasswordDataFilter INSTANCE = new UserPasswordDataFilter();

    @Override
    public byte[] decode(CodecContext codecContext, byte[] data) {
        MessageDigest md5 = getMd5Instance();

        byte[] paddedPassword = new byte[data.length];

        md5.update(codecContext.getSecret());
        md5.update(codecContext.getRequestAuthenticator());

        byte[] b = md5.digest();

        for (int i = 0; i < data.length; i += 16) {
            if (i != 0) {
                md5.update(codecContext.getSecret());
                md5.update(Arrays.copyOfRange(data, i - 16, i));
                b = md5.digest();
            }

            for (int j = 0; j < 16; j++) {
                paddedPassword[i + j] = (byte) (data[i + j] ^ b[j]);
            }

        }

        // The decoded password is padded with null characters, chop them off
        int passwordLength = 0;

        for (int i = paddedPassword.length - 1; i >= 0; i--) {
            if (paddedPassword[i] != '\0') {
                passwordLength = i + 1;

                break;
            }
        }

        byte[] password = Arrays.copyOfRange(paddedPassword, 0, passwordLength);

        return password;
    }

    @Override
    public byte[] encode(CodecContext codecContext, byte[] data) {
        if (data.length > 128) {
            throw new IllegalArgumentException("Password length must be in range [0, 128]");
        }

        MessageDigest md5 = getMd5Instance();

        byte[] paddedPassword = new byte[data.length - ((data.length - 1) % 16) + 15];
        System.arraycopy(data, 0, paddedPassword, 0, data.length);

        byte[] hiddenPassword = new byte[paddedPassword.length];

        md5.update(codecContext.getSecret());
        md5.update(codecContext.getRequestAuthenticator());

        byte[] b = md5.digest();

        for (int i = 0; i < paddedPassword.length; i += 16) {
            if (i != 0) {
                md5.update(codecContext.getSecret());
                md5.update(Arrays.copyOfRange(hiddenPassword, i - 16, i));
                b = md5.digest();
            }

            for (int j = 0; j < 16; j++) {
                hiddenPassword[i + j] = (byte) (paddedPassword[i + j] ^ b[j]);
            }
        }

        return hiddenPassword;
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
