package com.tpms.encode;

import com.syt.tmps.TpmsApplication;
import com.tpms.utils.Log;
import com.tpms.utils.SLOG;

/* JADX INFO: loaded from: classes.dex */
public class PackBufferFrameEn {
    String TAG = PackBufferFrameEn.class.getSimpleName();
    TpmsApplication theApp;

    public PackBufferFrameEn(TpmsApplication app) {
        this.theApp = app;
    }

    public void send(byte[] frame) {
        byte cc = calcCC(frame);
        frame[frame.length - 1] = cc;
        SLOG.LogByteArr(this.TAG + "write", frame, frame.length);
        this.theApp.getDataSrc().writeData(frame);
    }

    protected byte calcCC(byte[] buf) {
        int datalen = Math.abs((int) buf[3]);
        byte b = buf[3];
        byte calc = 0;
        for (int i = 0; i < datalen - 1; i++) {
            calc = (byte) (buf[i] + calc);
        }
        byte cc = calc;
        Log.i(this.TAG, "cc:" + ((int) cc));
        return cc;
    }
}
