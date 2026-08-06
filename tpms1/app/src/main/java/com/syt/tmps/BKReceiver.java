package com.syt.tmps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;

/* JADX INFO: loaded from: classes.dex */
public class BKReceiver extends BroadcastReceiver {
    private static final String BOOT_COMPLATE = "android.intent.action.BOOT_COMPLETED";
    private static final String MSG_SYS_STD_TPMS_DATA_TEST_RECV = "MSG_SYS_STD_TPMS_DATA_TEST_RECV";
    private static String TAG = "BKReceiver";
    ModelManager Util = null;
    TpmsApplication app = null;
    private SharedPreferences mPreferences;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String abcString = intent.getAction();
        this.Util = new ModelManager();
        Log.i(TAG, "onReceive act:" + abcString);
        if (this.app == null) {
            this.app = (TpmsApplication) context.getApplicationContext();
        }
        if (intent.getAction().equals(BOOT_COMPLATE)) {
            this.app.startTpms();
            return;
        }
        if (intent.getAction().equals(MSG_SYS_STD_TPMS_DATA_TEST_RECV)) {
            Log.i(TAG, "接受广播MSG_SYS_STD_CANBUS_DATA_TEST_RECV");
            String data = intent.getStringExtra("data");
            byte[] buf = hexStringToBytes(data);
            this.app.datasrc.testAddBuf(buf);
            return;
        }
        if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
            this.app.startTpms();
            return;
        }
        if (!intent.getAction().equals("android.intent.action.ACC_EVENT")) {
            if (intent.getAction().equals("android.intent.action.USER_PRESENT")) {
                this.app.startTpms();
            }
        } else {
            int ret = intent.getIntExtra("android.intent.extra.ACC_STATE", -1);
            if (ret == 0) {
                this.app.stopTpms();
            } else {
                this.app.startTpms();
            }
        }
    }

    private void delayMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            //ThrowableExtension.printStackTrace(e);
            e.printStackTrace();
        }
    }

    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            Log.e(TAG, "hexString == null || hexString.equals()");
            return null;
        }
        String hexString2 = hexString.replace("0x", "").replace("+", "").replace(" ", "").toUpperCase();
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
}
