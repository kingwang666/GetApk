package com.wang.getapk.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUtil {

    private static String bytes2Hex(byte[] src) {
        char[] res = new char[src.length * 2];
        final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (int i = 0, j = 0; i < src.length; i++) {
            res[j++] = hexDigits[src[i] >>> 4 & 0x0f];
            res[j++] = hexDigits[src[i] & 0x0f];
        }

        return new String(res);
    }

    public static String getMD5(byte[] data) {
        String value = "";
        if (data == null || data.length == 0) {
            return value;
        }
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            value = bytes2Hex(digester.digest(data));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getSHA1(byte[] data) {
        String value = "";
        if (data == null || data.length == 0) {
            return value;
        }
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA1");
            value = bytes2Hex(digester.digest(data));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getSHA256(byte[] data) {
        String value = "";
        if (data == null || data.length == 0) {
            return value;
        }
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA256");
            value = bytes2Hex(digester.digest(data));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return value;
    }
}