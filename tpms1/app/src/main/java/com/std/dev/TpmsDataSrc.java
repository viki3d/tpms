package com.std.dev;

import com.syt.tmps.TpmsApplication;
import com.tpms.decode.PackBufferFrame;

/* JADX INFO: loaded from: classes.dex */
public class TpmsDataSrc {
    public static final int MESSAGE_DATA_PROC = 1;
    protected TpmsApplication theapp;
    private String TAG = "TpmsDataSrc";
    protected PackBufferFrame BufferFrame = null;
    boolean DEBUG = true;

    public TpmsDataSrc(TpmsApplication app) {
        this.theapp = app;
    }

    public void init() {
    }

    public void setBufferFrame(PackBufferFrame frame) {
        this.BufferFrame = frame;
    }

    public void writeData(byte[] datafuf) {
    }

    public void start() {
    }

    public void stop() {
    }

    public void testAddBuf(byte[] buf) {
        this.BufferFrame.addBuffer(buf, buf.length);
    }

    public String getDevName() {
        return "";
    }
}
