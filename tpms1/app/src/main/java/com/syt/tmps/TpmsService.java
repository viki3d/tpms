package com.syt.tmps;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;

/* JADX INFO: loaded from: classes.dex */
public class TpmsService extends Service {
    private static final String TAG = "TpmsService";
    Runnable getCurentWindow = new Runnable() { // from class: com.syt.tmps.TpmsService.1
        @Override // java.lang.Runnable
        public void run() {
            try {
                ActivityManager am = (ActivityManager) TpmsService.this.getSystemService("activity");
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                Log.d("TestService", "pkg:" + cn.getPackageName());
                Log.d("TestService", "cls:" + cn.getClassName());
            } catch (Exception e) {
                //ThrowableExtension.printStackTrace(e);
                e.printStackTrace();
            }
            new Handler().postDelayed(TpmsService.this.getCurentWindow, 2000L);
        }
    };

    @Override // android.app.Service
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        ((TpmsApplication) getApplication()).attachService(this);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override // android.app.Service
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }
}
