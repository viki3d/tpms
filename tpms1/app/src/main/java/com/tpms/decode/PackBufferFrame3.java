package com.tpms.decode;

import com.tpms.utils.SLOG;
import java.nio.ByteBuffer;

/* JADX INFO: loaded from: classes.dex */
public class PackBufferFrame3 extends PackBufferFrame {
    private static final String TAG = "PackBufferFrame3";

    @Override // com.tpms.decode.PackBufferFrame
    protected boolean calcCC(byte[] buf) {
        int datalen = buf[2];
        if (datalen == 0) {
            SLOG.LogByteArr("PackBufferFrame3ERR", buf, buf.length);
            return false;
        }
        byte cc = sumCC(buf);
        return cc == buf[datalen + (-1)];
    }

    @Override // com.tpms.decode.PackBufferFrame
    public byte sumCC(byte[] buf) {
        int datalen = buf[2];
        byte calc = buf[0];
        for (int i = 1; i < datalen - 1; i++) {
            calc = (byte) (buf[i] ^ calc);
        }
        byte cc = calc;
        return cc;
    }

    @Override // com.tpms.decode.PackBufferFrame
    protected byte[] protocolFrameFilter2(byte[] buf, int len) {
        if (buf == null) {
            return null;
        }
        if (len >= 3) {
            if (buf[0] == 85 && buf[1] == -86) {
                int datalen = buf[2];
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
}
