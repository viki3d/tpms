package com.tpms.utils;

import android.content.Context;
import android.os.Environment;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class Log {
    private static final char DEBUG = 'd';
    private static final char ERROR = 'e';
    private static final char INFO = 'i';
    private static final char VERBOSE = 'v';
    private static final char WARN = 'w';
    private static String TAG = "LogToFile";
    private static String logPath = null;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
    private static Date date = new Date();
    public static boolean mLogFileEnable = false;
    public static String fileName = "";
    static FileOutputStream fos = null;
    static BufferedWriter bw = null;

    public static void init(Context context) {
        logPath = getFilePath(context) + "/Logs";
        createLogFile();
    }

    private static String getFilePath(Context context) {
        if (Environment.isExternalStorageEmulated()) {
            String str = "2";
        }
        if (Environment.isExternalStorageRemovable()) {
            return context.getExternalFilesDir(null).getPath();
        }
        return context.getExternalFilesDir("").getPath();
    }

    public static void setLogToFile(boolean LogFileEnable) {
        mLogFileEnable = LogFileEnable;
    }

    public static void v(String tag, String msg) {
        android.util.Log.v(tag, msg);
        writeToFile(VERBOSE, tag, msg);
    }

    public static void d(String tag, String msg) {
        android.util.Log.d(tag, msg);
        writeToFile(DEBUG, tag, msg);
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(tag, msg);
        writeToFile(INFO, tag, msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(tag, msg);
        writeToFile(WARN, tag, msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
        writeToFile(ERROR, tag, msg);
    }

    private static synchronized void writeToFile(char type, String tag, String msg) {
        if (logPath == null) {
            android.util.Log.e(TAG, "logPath == null ，未初始化LogToFile");
            return;
        }
        if (mLogFileEnable) {
            DirSizeLimitUtil limitUtil = new DirSizeLimitUtil(logPath, 2097152.0d);
            limitUtil.sizeProc();
            String log = dateFormat.format(date) + " " + type + " " + tag + " " + msg + "\n";
            File file = new File(logPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File logFile = new File(fileName);
            if (bw == null || (logFile != null && logFile.length() > 102400)) {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (Exception e) {
//                        ThrowableExtension.printStackTrace(e);
                        e.printStackTrace();;
                    }
                    bw = null;
                }
                createLogFile();
                try {
                    bw.write(log);
                    bw.flush();
                } catch (Exception e2) {
  //                  ThrowableExtension.printStackTrace(e2);
                    e2.printStackTrace();;
                    try {
                        if (bw != null) {
                            bw.close();
                            bw = null;
                        }
                    } catch (IOException e3) {
                        //ThrowableExtension.printStackTrace(e2);
                        e2.printStackTrace();;

                    }
                }
                return;
            }
            try {
                bw.write(log);
                bw.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            return;
            //throw th;
        }
    }

    private static void createLogFile() {
        File file = new File(logPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        fileName = logPath + "/log_" + dateFormat.format(new Date()) + ".log";
        try {
            fos = new FileOutputStream(fileName, true);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
        } catch (Exception e) {
//            ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;

        }
    }
}
