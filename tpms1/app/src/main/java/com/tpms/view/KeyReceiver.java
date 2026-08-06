package com.tpms.view;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.tpms.utils.Util;

/* JADX INFO: loaded from: classes.dex */
public class KeyReceiver extends BroadcastReceiver {
    private static final String BOOT_COMPLATE = "android.intent.action.BOOT_COMPLETED";
    private SharedPreferences mPreferences;
    private boolean aux_acc = false;
    Util Util = null;
    private int mScaFb = -1;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        intent.getAction();
        this.Util = new Util();
        if (intent.getAction().equals(BOOT_COMPLATE)) {
            System.out.println("======AUX收到开机广播");
            getPreferenceValue(context);
            if (this.aux_acc) {
                System.out.println("=====true 断电时在播放");
                context.getSharedPreferences("aux_pref", -1).edit().putBoolean("LaunchAccOff", true).commit();
                ActivityManager am = (ActivityManager) context.getSystemService("activity");
                am.getRunningTasks(1).get(0).topActivity.getPackageName().equals(context.getPackageName());
                savePreferenceValue(context);
                return;
            }
            System.out.println("======断电时AUX没有播放！！！");
        }
    }

    private void getPreferenceValue(Context context) {
        this.mPreferences = context.getSharedPreferences("aux_pref", -1);
        this.aux_acc = this.mPreferences.getBoolean("aux_goplay", false);
    }

    private void savePreferenceValue(Context context) {
        this.mPreferences = context.getSharedPreferences("aux_pref", -1);
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putBoolean("aux_goplay", false);
        editor.commit();
    }

    private void delayMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
//            ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;

        }
    }
}
