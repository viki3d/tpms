package com.std.dev;

//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.syt.tmps.TpmsApplication;
import com.tpms.decode.PackBufferFrame;
import com.tpms.utils.SLOG;
import java.io.File;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class TpmsDataSrc3 extends TpmsDataSrc {
    private String TAG;
    Serialport mPort;
    protected ReadThread mReadThread;

    public TpmsDataSrc3(TpmsApplication app) {
        super(app);
        this.TAG = "TpmsDataSrc3";
    }

    @Override // com.std.dev.TpmsDataSrc
    public void init() {
        try {
            this.mPort = new Serialport(new File("/dev/ttyS1"), 19200, 0);
        } catch (Exception e) {
            //ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;
        }
    }

    @Override // com.std.dev.TpmsDataSrc
    public void setBufferFrame(PackBufferFrame frame) {
        this.BufferFrame = frame;
    }

    @Override // com.std.dev.TpmsDataSrc
    public void writeData(byte[] databuf) {
        try {
            this.mPort.write(databuf, databuf.length);
        } catch (IOException e) {
            //ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;
        }
    }

    @Override // com.std.dev.TpmsDataSrc
    public void start() {
        startReadThread();
    }

    @Override // com.std.dev.TpmsDataSrc
    public void stop() {
        if (this.mReadThread != null) {
            this.mReadThread.interrupt();
            this.mReadThread = null;
            if (this.mPort != null) {
                this.mPort.close();
                this.mPort = null;
            }
        }
    }

    private void startReadThread() {
        if (this.mReadThread != null) {
            return;
        }
        try {
            this.mReadThread = new ReadThread();
            this.mReadThread.start();
        } catch (Exception e) {
            //ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;
        }
    }

    private class ReadThread extends Thread {
        private ReadThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            super.run();
            byte[] buffer = new byte[512];
            while (!isInterrupted()) {
                try {
                    int size = TpmsDataSrc3.this.mPort.read(buffer);
                    if (size == 0) {
                        try {
                            Thread.sleep(20L);
                        } catch (Exception e) {
                        }
                    } else {
                        SLOG.LogByteArr(TpmsDataSrc3.this.TAG + "read", buffer, size);
                        byte[] recBytes = new byte[size];
                        System.arraycopy(buffer, 0, recBytes, 0, size);
                        TpmsDataSrc3.this.BufferFrame.addBuffer(recBytes, recBytes.length);
                    }
                } catch (Exception e2) {
                    //ThrowableExtension.printStackTrace(e2);
                    e2.printStackTrace();
                    return;
                }
            }
        }
    }
}
