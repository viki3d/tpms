package com.std.dev;

//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.tpms.utils.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* JADX INFO: loaded from: classes.dex */
public class Serialport {
    private static final String TAG = "Serialport";
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public static native void close(FileDescriptor fileDescriptor);

    public static native FileDescriptor open(String str, int i, int i2);

    public Serialport(File device, int baudrate, int flags) throws IOException, SecurityException {
        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\nexit\n";
                su.getOutputStream().write(cmd.getBytes());
                if (su.waitFor() != 0 || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
//                ThrowableExtension.printStackTrace(e);
                e.printStackTrace();;

                throw new SecurityException();
            }
        }
        this.mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (this.mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        this.mFileInputStream = new FileInputStream(this.mFd);
        this.mFileOutputStream = new FileOutputStream(this.mFd);
    }

    public int read(byte[] buffer) throws IOException {
        return this.mFileInputStream.read(buffer);
    }

    public void write(byte[] buffer, int len) throws IOException {
        this.mFileOutputStream.write(buffer, 0, len);
    }

    public InputStream getInputStream() {
        return this.mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return this.mFileOutputStream;
    }

    public void close() {
        try {
            this.mFileOutputStream.close();
        } catch (IOException e) {
//            ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;

        }
        this.mFileOutputStream = null;
        try {
            this.mFileInputStream.close();
        } catch (IOException e2) {
  //          ThrowableExtension.printStackTrace(e2);
            e2.printStackTrace();;

        }
        this.mFileInputStream = null;
        OsWrap.close(this.mFd);
        this.mFd = null;
    }

    static {
        System.loadLibrary("stdSerialport");
    }
}
