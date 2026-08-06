package com.tpms.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler INSTANCE = new CrashHandler();
    private static Thread s_td = null;
    private Map<String, String> infos = new HashMap();
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    MailSenderInfo mailInfo = null;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void initDir() {
    }

    public class MailSenderInfo {
        private String[] attachFileNames;
        private String mailServerHost = "smtp.126.com";
        private String mailServerPort = "25";
        private String fromAddress = "chang19test@126.com";
        private String toAddress = "chang19test@126.com";
        private String userName = "chang19test@126.com";
        private String password = "19ufoufo19";
        private boolean validate = true;
        private String subject = "HCF-BUG";
        private String content = "请解决附件bug";

        public MailSenderInfo() {
        }
    }

    public void init(Context context) {
        this.mContext = context;
        this.mailInfo = new MailSenderInfo();
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && this.mDefaultHandler != null) {
            android.util.Log.e(TAG, "error uncaughtException 0");
            this.mDefaultHandler.uncaughtException(thread, ex);
            return;
        }
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            android.util.Log.e(TAG, "error : ", e);
        }
        android.util.Log.e(TAG, "error uncaughtException 1");
        Process.killProcess(Process.myPid());
        System.exit(1);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.tpms.utils.CrashHandler$1] */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        new Thread() { // from class: com.tpms.utils.CrashHandler.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Looper.prepare();
                Toast.makeText(CrashHandler.this.mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        collectDeviceInfo(this.mContext);
        String fileName = saveCrashInfo2File(ex);
        android.util.Log.i(TAG, "log file name:" + fileName);
        return true;
    }

    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
           //PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 1);
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), android.content.pm.PackageManager.GET_ACTIVITIES);

            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                this.infos.put("packageName", ctx.getPackageName());
                this.infos.put("versionName", versionName);
                this.infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            android.util.Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                this.infos.put(field.getName(), field.get(null).toString());
                android.util.Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e2) {
                android.util.Log.e(TAG, "an error occured when collect crash info", e2);
            }
        }
    }

    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : this.infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
//        ThrowableExtension.printStackTrace(ex, printWriter);
        ex.printStackTrace(printWriter);
        for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
            //ThrowableExtension.printStackTrace(cause, printWriter);
            cause.printStackTrace(printWriter);
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        android.util.Log.i(TAG, "write file in");
        android.util.Log.e(TAG, sb.toString());
        try {
            long timestamp = System.currentTimeMillis();
            String time = this.formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            String path = this.mContext.getExternalFilesDir("").getPath();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(sb.toString().getBytes());
            fos.close();
            return fileName;
        } catch (Exception e) {
            android.util.Log.e(TAG, "an error occured while writing file...", e);
            DirSizeLimitUtil LimitUtil = new DirSizeLimitUtil(this.mContext.getExternalFilesDir("").getPath(), 2097152.0d);
            LimitUtil.sizeProc();
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void logFileProcSync(String packageName, String fileName, Context cont) {
        if (fileName != null) {
            boolean retflag = true;
            String str = packageName + "报错，\n请查看附件log解决bug";
            try {
                android.util.Log.i(TAG, "logFileProc===" + fileName);
            } catch (Exception e) {
//                ThrowableExtension.printStackTrace(e);
                e.printStackTrace();
                retflag = false;
            }
            if (retflag) {
                new File(fileName).delete();
            }
        }
        DirSizeLimitUtil LimitUtil = new DirSizeLimitUtil(cont.getExternalFilesDir("").getPath(), 2097152.0d);
        LimitUtil.sizeProc();
    }

    public static void logFileProc(final String packName, final String fileName, final Context cont) {
        s_td = new Thread(new Runnable() { // from class: com.tpms.utils.CrashHandler.2
            @Override // java.lang.Runnable
            public void run() {
                CrashHandler.logFileProcSync(packName, fileName, cont);
            }
        });
        s_td.start();
    }

    public void sendLogToMail(String txtBody) {
    }

    public static void _sendLogToMail(String txtBody) {
        Thread td = new Thread(new Runnable() { // from class: com.tpms.utils.CrashHandler.3
            @Override // java.lang.Runnable
            public void run() {
            }
        });
        td.start();
    }
}
