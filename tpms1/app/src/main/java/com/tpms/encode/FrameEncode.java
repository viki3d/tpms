package com.tpms.encode;

import android.content.Context;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.syt.tmps.TpmsApplication;
import com.tpms.utils.Log;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class FrameEncode {
    private static final HashMap<String, String> activityMap = new HashMap<>();
    Context mctx;
    TpmsApplication theApp;
    String mOldHintTxt = "";
    PackBufferFrameEn FrameEn = null;
    private String TAG = "FrameEncode";

    public FrameEncode(TpmsApplication app) {
        this.theApp = app;
    }

    public void init(Context ctx) {
        this.mctx = ctx;
        this.FrameEn = new PackBufferFrameEn(this.theApp);
    }

    protected void send(String content) {
    }

    public void send(byte[] frame) {
        this.FrameEn.send(frame);
        sleep(60L);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            //ThrowableExtension.printStackTrace(e);
            e.printStackTrace();
        }
    }

    public void HeartbeatEventAck() {
    }

    public void SendHeartbeat() {
    }

    public void SendEncryption(byte seed) {
    }

    public void paireBackLeft() {
        Log.i(this.TAG, "配对右左轮ID");
    }

    public void paireBackRight() {
        Log.i(this.TAG, "配对右后轮ID");
    }

    public void paireFrontLeft() {
        Log.i(this.TAG, "配对前左轮ID");
    }

    public void paireFrontRight() {
        Log.i(this.TAG, "配对前右ID");
    }

    public void paireSpTired() {
        Log.i(this.TAG, "配对备胎ID");
    }

    public void querySensorID() {
        Log.i(this.TAG, "querySensorID");
    }

    public void stopPaire() {
        Log.i(this.TAG, "stopPaire");
    }

    public void exchangeLeftFrontRightFront() {
        Log.i(this.TAG, "exchangeLeftFrontRightFront");
    }

    public void exchangeLeftFrontLeftBack() {
        Log.i(this.TAG, "exchangeLeftFrontLeftBack");
    }

    public void exchangeLeftFrontRightBack() {
        Log.i(this.TAG, "exchangeLeftFrontRightBack");
    }

    public void exchangeRightFrontLeftBack() {
        Log.i(this.TAG, "exchangeRightFrontLeftBack");
    }

    public void exchangeRightFrontRightBack() {
        Log.i(this.TAG, "exchangeRightFrontRightBack");
    }

    public void exchangeLeftBackRightBack() {
        Log.i(this.TAG, "exchangeLeftBackRightBack");
    }

    public void exchange_sp_fl() {
    }

    public void exchange_sp_fr() {
    }

    public void exchange_sp_bl() {
    }

    public void exchange_sp_br() {
    }
}
