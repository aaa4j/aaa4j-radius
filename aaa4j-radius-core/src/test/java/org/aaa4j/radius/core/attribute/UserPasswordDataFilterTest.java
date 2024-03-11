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

import org.aaa4j.radius.core.dictionary.dictionaries.StandardDictionary;
import org.aaa4j.radius.core.util.SecureRandomProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.aaa4j.radius.core.Utils.fromHex;
import static org.aaa4j.radius.core.Utils.toHex;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("UserPasswordDataFilter")
class UserPasswordDataFilterTest {

    @Test
    @DisplayName("UserPassword data is decoded successfully")
    void testDecode() {
        UserPasswordDataFilter userPasswordDataFilter = new UserPasswordDataFilter();

        CodecContext codecContext = new CodecContext(new StandardDictionary(),
                fromHex("d955e791c15fe6996434be163c8c019d21cd901b867600c2662e8a4628c5bff3"),
                fromHex("9fa4ee463dbfd1b0c99a209490c52cb6"),
                new SecureRandomProvider());

        {
            byte[] hiddenPassword = fromHex("b04ef795318fb58d6da1ccc94ff1552c");

            byte[] passwordBytes = userPasswordDataFilter.decode(codecContext, hiddenPassword);

            String password = new String(passwordBytes, StandardCharsets.UTF_8);

            assertEquals("", password);
        }
        {
            byte[] hiddenPassword = fromHex("d12c9495318fb58d6da1ccc94ff1552c");

            byte[] passwordBytes = userPasswordDataFilter.decode(codecContext, hiddenPassword);

            String password = new String(passwordBytes, StandardCharsets.UTF_8);

            assertEquals("abc", password);
        }
        {
            byte[] hiddenPassword = fromHex("d12c94f154e9d2e504cba7a5229f3a5c");

            byte[] passwordBytes = userPasswordDataFilter.decode(codecContext, hiddenPassword);

            String password = new String(passwordBytes, StandardCharsets.UTF_8);

            assertEquals("abcdefghijklmnop", password);
        }
        {
            byte[] hiddenPassword = fromHex("d12c94f154e9d2e504cba7a5229f3a5cf3e675578cc3abb486e814c9a97dd5d0" +
                    "657fd2d428d79fa68b9be651ac0893a440e94a20e238962bc3de4b9184ccbb7c" +
                    "72dfdb01a95d8c1d473daad70b763494a65dc6c705e276634f8040ae9af5d0b3" +
                    "a501bb4ce27950968a2ce5926dfd2f1a5c642f59e615b4188389cfe99e518ea9");

            byte[] passwordBytes = userPasswordDataFilter.decode(codecContext, hiddenPassword);

            String password = new String(passwordBytes, StandardCharsets.UTF_8);

            assertEquals("abcdefghijklmnopqrstuvwxyz" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "abcdefghijklmnopqrstuvwx",
                    password);
        }
    }

    @Test
    @DisplayName("UserPassword is encoded successfully")
    void testEncode() {
        UserPasswordDataFilter userPasswordDataFilter =
                new UserPasswordDataFilter();

        CodecContext codecContext = new CodecContext(new StandardDictionary(),
                fromHex("d955e791c15fe6996434be163c8c019d21cd901b867600c2662e8a4628c5bff3"),
                fromHex("9fa4ee463dbfd1b0c99a209490c52cb6"),
                new SecureRandomProvider());

        {
            byte[] password = "".getBytes(StandardCharsets.UTF_8);

            byte[] hiddenPassword = userPasswordDataFilter.encode(codecContext, password);

            assertEquals("b04ef795318fb58d6da1ccc94ff1552c", toHex(hiddenPassword));
        }
        {
            byte[] password = "abc".getBytes(StandardCharsets.UTF_8);

            byte[] hiddenPassword = userPasswordDataFilter.encode(codecContext, password);

            assertEquals("d12c9495318fb58d6da1ccc94ff1552c", toHex(hiddenPassword));
        }
        {
            byte[] password = "abcdefghijklmnop".getBytes(StandardCharsets.UTF_8);

            byte[] hiddenPassword = userPasswordDataFilter.encode(codecContext, password);

            assertEquals("d12c94f154e9d2e504cba7a5229f3a5c", toHex(hiddenPassword));
        }
        {
            byte[] password = ("abcdefghijklmnopqrstuvwxyz" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "abcdefghijklmnopqrstuvwx").getBytes(StandardCharsets.UTF_8);

            byte[] hiddenPassword = userPasswordDataFilter.encode(codecContext, password);

            assertEquals("d12c94f154e9d2e504cba7a5229f3a5cf3e675578cc3abb486e814c9a97dd5d0" +
                            "657fd2d428d79fa68b9be651ac0893a440e94a20e238962bc3de4b9184ccbb7c" +
                            "72dfdb01a95d8c1d473daad70b763494a65dc6c705e276634f8040ae9af5d0b3" +
                            "a501bb4ce27950968a2ce5926dfd2f1a5c642f59e615b4188389cfe99e518ea9",
                    toHex(hiddenPassword));
        }
    }

}