package com.tpms.utils;

//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.ByteArrayOutputStream;

/* JADX INFO: loaded from: classes.dex */
public class Util {
    private static String hexString = "0123456789ABCDEF";

    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        for (int i = 0; i < bs.length; i++) {
            int bit = (bs[i] & 240) >> 4;
            sb.append(chars[bit]);
            int bit2 = bs[i] & 15;
            sb.append(chars[bit2]);
        }
        return sb.toString();
    }

    public static byte[] hexStringToByte(String hex) {
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) ((toByte(achar[pos]) << 4) | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        for (byte b : bArray) {
            String sTemp = Integer.toHexString(b & 255);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static final String byteToUpperString(byte bt) {
        String sTemp = Integer.toHexString(bt & 255);
        return sTemp.toUpperCase();
    }

    public static String toHexString1(byte[] b) {
        StringBuffer buffer = new StringBuffer();
        for (byte b2 : b) {
            buffer.append(toHexString1(b2));
        }
        return buffer.toString();
    }

    public static String toHexString1(byte b) {
        String s = Integer.toHexString(b & 255);
        if (s.length() == 1) {
            return "0" + s;
        }
        return s;
    }

    public static String hexStr2Str(String hexStr) {
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int n = "0123456789ABCDEF".indexOf(hexs[i * 2]) * 16;
            bytes[i] = (byte) ((n + "0123456789ABCDEF".indexOf(hexs[(i * 2) + 1])) & 255);
        }
        return new String(bytes);
    }

    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (Integer.parseInt(s.substring(i * 2, (i * 2) + 2), 16) & 255);
            } catch (Exception e) {
//                ThrowableExtension.printStackTrace(e);
                e.printStackTrace();
            }
        }
        try {
            return new String(baKeyword, "utf-8");
        } catch (Exception e1) {
//            ThrowableExtension.printStackTrace(e1);
            e1.printStackTrace();
            return s;
        }
    }

    public static String encode(String str) {
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 240) >> 4));
            sb.append(hexString.charAt((bytes[i] & 15) >> 0));
        }
        return sb.toString();
    }

    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        for (int i = 0; i < bytes.length(); i += 2) {
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4) | hexString.indexOf(bytes.charAt(i + 1)));
        }
        return new String(baos.toByteArray());
    }

    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (((byte) (_b0 << 4)) ^ _b1);
        return ret;
    }

    public static byte[] HexString2Bytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[(i * 2) + 1]);
        }
        return ret;
    }

    public static byte[] hexStringToBytes(String hexString2) {
        if (hexString2 == null || hexString2.equals("")) {
            return null;
        }
        int length = hexString2.length() / 2;
        char[] hexChars = hexString2.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) << 4) | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String hexstringTo32(String hexString2) {
        int n = 8 - hexString2.length();
        switch (n) {
            case 1:
                return "0" + hexString2;
            case 2:
                return "00" + hexString2;
            case 3:
                return "000" + hexString2;
            case 4:
                return "0000" + hexString2;
            case 5:
                return "00000" + hexString2;
            case 6:
                return "000000" + hexString2;
            case 7:
                return "00000000" + hexString2;
            default:
                return hexString2;
        }
    }
}
