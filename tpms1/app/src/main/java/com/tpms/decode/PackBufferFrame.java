package com.tpms.decode;

//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.tpms.utils.Log;
import com.tpms.utils.SLOG;
import de.greenrobot.event.EventBus;
import java.nio.ByteBuffer;
import java.util.Vector;

/* JADX INFO: loaded from: classes.dex */
public class PackBufferFrame {
    protected static boolean DEBUG = false;
    protected static final int MAX_NETPACKBUFFER_SIZE = 65536;
    private static final String TAG = "PackBufferFrame";
    protected int m_uCurBufferPosition;
    protected Object mutex = new Object();
    Vector<ByteBuffer> mRet = new Vector<>();
    protected byte[] m_pNetPackBuffer = new byte[81920];

    public PackBufferFrame() {
        this.m_uCurBufferPosition = 0;
        this.m_uCurBufferPosition = 0;
    }

    public void resetBufferPosition() {
        synchronized (this.mutex) {
            this.m_uCurBufferPosition = 0;
        }
    }

    protected byte[] erase(byte[] buf, int buflen, int dellen) {
        if (DEBUG) {
            Log.i(TAG, "dellen:" + dellen);
            SLOG.LogByteArr(TAG, buf, buflen);
        }
        if (buflen <= dellen) {
            return null;
        }
        byte[] newbuf = new byte[buflen - dellen];
        try {
            System.arraycopy(buf, dellen, newbuf, 0, buflen - dellen);
            return newbuf;
        } catch (Exception e) {
            //ThrowableExtension.printStackTrace(e);
            e.printStackTrace();

            return newbuf;
        }
    }

    protected boolean calcCC(byte[] buf) {
        int datalen = Math.abs((int) buf[3]);
        byte cc = sumCC(buf);
        return cc == buf[datalen + (-1)];
    }

    public byte sumCC(byte[] buf) {
        int datalen = Math.abs((int) buf[3]);
        byte b = buf[3];
        byte calc = 0;
        for (int i = 0; i < datalen - 1; i++) {
            calc = (byte) (buf[i] + calc);
        }
        byte cc = calc;
        Log.i(TAG, "cc:0x" + SLOG.byteToHexString(cc));
        return cc;
    }

    protected byte[] protocolFrameFilter2(byte[] buf, int len) {
        if (buf == null) {
            return null;
        }
        if (len >= 4) {
            if (buf[0] == -86) {
            }
            if (buf[1] == 85) {
            }
            if (buf[0] == -86) {
                int datalen = buf[3];
                if (datalen <= len) {
                    if (calcCC(buf)) {
                        ByteBuffer bbuf = ByteBuffer.allocate(datalen);
                        this.mRet.add(bbuf);
                        bbuf.put(buf, 0, datalen);
                        byte[] buf2 = erase(buf, len, datalen);
                        if (buf2 == null) {
                            return null;
                        }
                        return protocolFrameFilter2(buf2, buf2.length);
                    }
                    byte[] buf3 = erase(buf, len, 1);
                    if (buf3 == null) {
                        return null;
                    }
                    return protocolFrameFilter2(buf3, buf3.length);
                }
                return returnNewBuf(buf, len);
            }
            byte[] buf4 = erase(buf, len, 1);
            if (buf4 == null) {
                return null;
            }
            return protocolFrameFilter2(buf4, buf4.length);
        }
        return returnNewBuf(buf, len);
    }

    byte[] returnNewBuf(byte[] buf, int len) {
        byte[] newbuf = new byte[len];
        try {
            System.arraycopy(buf, 0, newbuf, 0, len);
            return newbuf;
        } catch (Exception e) {
            //ThrowableExtension.printStackTrace(e);
            e.printStackTrace();
            return null;
        }
    }

    public boolean addBuffer(byte[] buffer, int len) {
        synchronized (this.mutex) {
            try {
                if (buffer == null || len <= 0) {
                    return false;
                }
                if (this.m_uCurBufferPosition + len > 65536) {
                    this.m_uCurBufferPosition = 0;
                    SLOG.e(TAG, "m_uCurBufferPosition > MAX_NETPACKBUFFER_SIZE");
                }
                System.arraycopy(buffer, 0, this.m_pNetPackBuffer, this.m_uCurBufferPosition, len);
                this.m_uCurBufferPosition += len;
                this.mRet.clear();
                byte[] retbuf = protocolFrameFilter2(returnNewBuf(this.m_pNetPackBuffer, this.m_uCurBufferPosition), this.m_uCurBufferPosition);
                if (retbuf != null) {
                    System.arraycopy(retbuf, 0, this.m_pNetPackBuffer, 0, retbuf.length);
                    this.m_uCurBufferPosition = retbuf.length;
                } else {
                    this.m_uCurBufferPosition = 0;
                }
                for (int i = 0; i < this.mRet.size(); i++) {
                    ByteBuffer buf = this.mRet.get(i);
                    EventBus.getDefault().post(buf);
                }
                return true;
            } catch (Throwable th) {
                throw th;
            }
        }
    }
}
