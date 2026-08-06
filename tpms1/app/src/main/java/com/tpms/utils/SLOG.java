package com.tpms.utils;

/* JADX INFO: loaded from: classes.dex */
public class SLOG {
    private static final boolean DEBUG = true;
    private static final String TAG = "SLOG";
    private static final boolean WARNING = true;

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String msg, Exception e, String fileName) {
        StackTraceElement[] trace = e.getStackTrace();
        if (trace != null && trace.length != 0) {
            for (int i = 0; i < trace.length; i++) {
                if (trace[i].getFileName() != null && trace[i].getFileName().equals(fileName)) {
                    Log.e(TAG, trace[i].getFileName() + ":" + trace[i].getLineNumber());
                }
            }
        }
        Log.e(TAG, msg);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static String byteToHexString(byte b) {
        StringBuilder stringBuilder = new StringBuilder("");
        int v = b & 255;
        String hv = Integer.toHexString(v);
        if (hv.length() < 2) {
            stringBuilder.append(0);
        }
        stringBuilder.append(hv);
        return stringBuilder.toString();
    }

    public static String LogByteArr(String TAG2, byte[] frame_buffer, int frameSize) {
        StringBuffer printSb = new StringBuffer();
        String hexStr = null;
        for (int i = 0; i < frameSize; i++) {
            try {
                hexStr = byteToHexString(frame_buffer[i]);
                printSb.append(" " + hexStr);
            } catch (Exception e) {
                return hexStr;
            }
        }
        hexStr = printSb.toString();
        Log.i(TAG2, "len:" + frameSize + ";data:" + hexStr);
        return hexStr;
    }
}
