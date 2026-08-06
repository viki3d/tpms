package com.tpms.encode;

import com.syt.tmps.TpmsApplication;
import com.tpms.utils.Log;
import com.tpms.utils.SLOG;

/* JADX INFO: loaded from: classes.dex */
public class PackBufferFrameEn3 extends PackBufferFrameEn {
    String TAG;

    public PackBufferFrameEn3(TpmsApplication app) {
        super(app);
        this.TAG = PackBufferFrameEn3.class.getSimpleName();
    }

    @Override // com.tpms.encode.PackBufferFrameEn
    protected byte calcCC(byte[] buf) {
        int datalen = buf[2];
        byte calc = buf[0];
        for (int i = 1; i < datalen - 1; i++) {
            calc = (byte) (buf[i] ^ calc);
        }
        byte cc = calc;
        Log.i(this.TAG, "cc:" + SLOG.byteToHexString(cc));
        return cc;
    }
}
