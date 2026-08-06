package com.tpms.utils;

/* JADX INFO: loaded from: classes.dex */
public class FormatTransfer {
    public static byte[] toLH(int n) {
        byte[] b = {(byte) (n & 255), (byte) ((n >> 8) & 255), (byte) ((n >> 16) & 255), (byte) ((n >> 24) & 255)};
        return b;
    }

    public static byte[] toHH(int n) {
        byte[] b = {(byte) ((n >> 24) & 255), (byte) ((n >> 16) & 255), (byte) ((n >> 8) & 255), (byte) (n & 255)};
        return b;
    }

    public static byte[] toLH(short n) {
        byte[] b = {(byte) (n & 255), (byte) ((n >> 8) & 255)};
        return b;
    }

    public static byte[] toHH(short n) {
        byte[] b = {(byte) ((n >> 8) & 255), (byte) (n & 255)};
        return b;
    }

    public static byte[] toLH(float f) {
        return toLH(Float.floatToRawIntBits(f));
    }

    public static byte[] toHH(float f) {
        return toHH(Float.floatToRawIntBits(f));
    }

    public static byte[] stringToBytes(String s, int length) {
        while (s.getBytes().length < length) {
            s = s + " ";
        }
        return s.getBytes();
    }

    public static String bytesToString(byte[] b) {
        StringBuffer result = new StringBuffer("");
        for (byte b2 : b) {
            result.append((char) (b2 & 255));
        }
        return result.toString();
    }

    public static byte[] stringToBytes(String s) {
        return s.getBytes();
    }

    public static int hBytesToInt(byte[] b) {
        int s;
        int s2 = 0;
        for (int i = 0; i < 3; i++) {
            if (b[i] >= 0) {
                s = s2 + b[i];
            } else {
                s = s2 + 256 + b[i];
            }
            s2 = s * 256;
        }
        int i2 = b[3];
        if (i2 < 0) {
            return s2 + 256 + b[3];
        }
        return s2 + b[3];
    }

    public static int lBytesToInt(byte[] b) {
        int s;
        int s2 = 0;
        for (int s3 = 0; s3 < 3; s3++) {
            if (b[3 - s3] < 0) {
                s = s2 + 256 + b[3 - s3];
            } else {
                s = s2 + b[3 - s3];
            }
            s2 = s * 256;
        }
        int i = b[0];
        if (i < 0) {
            return s2 + 256 + b[0];
        }
        return s2 + b[0];
    }

    public static short hBytesToShort(byte[] b) {
        int s;
        int s2;
        if (b[0] >= 0) {
            s = 0 + b[0];
        } else {
            s = 0 + 256 + b[0];
        }
        int s3 = s * 256;
        if (b[1] < 0) {
            s2 = s3 + 256 + b[1];
        } else {
            s2 = s3 + b[1];
        }
        short result = (short) s2;
        return result;
    }

    public static short lBytesToShort(byte[] b) {
        int s;
        int s2;
        if (b[1] >= 0) {
            s = 0 + b[1];
        } else {
            s = 0 + 256 + b[1];
        }
        int s3 = s * 256;
        if (b[0] < 0) {
            s2 = s3 + 256 + b[0];
        } else {
            s2 = s3 + b[0];
        }
        short result = (short) s2;
        return result;
    }

    public static float hBytesToFloat(byte[] b) {
        new Float(0.0d);
        int i = ((((((b[0] & 255) << 8) | (b[1] & 255)) << 8) | (b[2] & 255)) << 8) | (b[3] & 255);
        return Float.intBitsToFloat(i);
    }

    public static float lBytesToFloat(byte[] b) {
        new Float(0.0d);
        int i = ((((((b[3] & 255) << 8) | (b[2] & 255)) << 8) | (b[1] & 255)) << 8) | (b[0] & 255);
        return Float.intBitsToFloat(i);
    }

    public static byte[] bytesReverseOrder(byte[] b) {
        int length = b.length;
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[(length - i) - 1] = b[i];
        }
        return result;
    }

    public static void printBytes(byte[] bb) {
        int length = bb.length;
        for (int i = 0; i < length; i++) {
            System.out.print(bb + " ");
        }
        System.out.println("");
    }

    public static void logBytes(byte[] bb) {
        int length = bb.length;
        String out = "";
        for (int i = 0; i < length; i++) {
            out = out + bb + " ";
        }
    }

    public static int reverseInt(int i) {
        int result = hBytesToInt(toLH(i));
        return result;
    }

    public static short reverseShort(short s) {
        short result = hBytesToShort(toLH(s));
        return result;
    }

    public static float reverseFloat(float f) {
        float result = hBytesToFloat(toLH(f));
        return result;
    }
}
