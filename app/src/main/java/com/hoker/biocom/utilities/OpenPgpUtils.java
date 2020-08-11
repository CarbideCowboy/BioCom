package com.hoker.biocom.utilities;

import java.util.Locale;

public class OpenPgpUtils {

    public static String convertKeyIdToHex(long keyId) {
        return "0x" + convertKeyIdToHex32bit(keyId >> 32) + convertKeyIdToHex32bit(keyId);
    }

    private static String convertKeyIdToHex32bit(long keyId) {
        StringBuilder hexString = new StringBuilder(Long.toHexString(keyId & 0xffffffffL).toLowerCase(Locale.ENGLISH));
        while (hexString.length() < 8) {
            hexString.insert(0, "0");
        }
        return hexString.toString();
    }
}

