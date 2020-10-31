package org.aaa4j.radius.core;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public final class Utils {

    private Utils() {
        throw new AssertionError();
    }

    public static byte[] fromHex(String hex) {
        try {
            return Hex.decodeHex(hex);
        }
        catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

}
