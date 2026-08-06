package com.tpms.decode;

import android.content.Context;
import com.tpms.modle.AlarmAgrs;
import com.tpms.modle.HeartbeatEvent;
import com.tpms.modle.PaireIDOkEvent;
import com.tpms.modle.QueryIDOkEvent;
import com.tpms.modle.TimeSeedEvent;
import com.tpms.modle.TiresExchangeEvent;
import com.tpms.modle.TiresStateEvent;
import com.tpms.utils.Log;
import com.tpms.utils.SLOG;
import de.greenrobot.event.EventBus;
import java.nio.ByteBuffer;

/* JADX INFO: loaded from: classes.dex */
public class FrameDecode3 extends FrameDecode {
    private static String TAG = "FrameDecode3";

    @Override // com.tpms.decode.FrameDecode
    public void init(Context ctx) {
        super.init(ctx);
        this.mPackBufferFrame = new PackBufferFrame3();
    }

    @Override // com.tpms.decode.FrameDecode
    public void onEventMainThread(ByteBuffer frameBuf) {
        String log;
        byte[] frame = frameBuf.array();
        byte cmd = frame[2];
        SLOG.LogByteArr(TAG + "完整V", frame, frame.length);
        Log.i(TAG, "cmd:" + ((int) cmd));
        if (cmd == 8) {
            Log.i(TAG, "轮胎状态,4秒上报一次");
            TiresStateEvent event = new TiresStateEvent();
            if (frame[3] == 0) {
                Log.i(TAG, "左前");
                event.tires = 1;
            } else if (frame[3] == 1) {
                Log.i(TAG, "右前");
                event.tires = 2;
            } else if (frame[3] == 16) {
                Log.i(TAG, "左后");
                event.tires = 0;
            } else if (frame[3] == 17) {
                Log.i(TAG, "右后");
                event.tires = 3;
            } else if (frame[3] == 5) {
                event.tires = 5;
            } else {
                return;
            }
            event.mState.AirPressure = (int) (((double) (frame[4] & 255)) * 3.44d);
            event.mState.Temperature = (frame[5] & 255) - 50;
            Log.i(TAG, "气压参数:" + event.mState.AirPressure + ";温度:" + event.mState.Temperature);
            if ((frame[6] & 32) != 0) {
                Log.i(TAG, "信号丢失了");
                event.mState.NoSignal = true;
            }
            int i = event.tires;
            if ((frame[6] & 8) != 0) {
                event.mState.Leakage = true;
                Log.i(TAG, "漏气");
            }
            if ((frame[6] & 16) != 0) {
                event.mState.LowPower = true;
                Log.i(TAG, "低电");
            }
            EventBus.getDefault().post(event);
            return;
        }
        if (cmd == 6) {
            if (frame[3] == 24) {
                PaireIDOkEvent paire = new PaireIDOkEvent();
                if (frame[4] == 0) {
                    Log.i(TAG, "配对学习成功 左前");
                    paire.tires = 1;
                } else if (frame[4] == 1) {
                    Log.i(TAG, "配对学习成功 右前");
                    paire.tires = 2;
                } else if (frame[4] == 16) {
                    Log.i(TAG, "配对学习成功 左后");
                    paire.tires = 0;
                } else if (frame[4] == 17) {
                    Log.i(TAG, "配对学习成功 右后");
                    paire.tires = 3;
                } else if (frame[4] == 5) {
                    paire.tires = 5;
                } else {
                    return;
                }
                EventBus.getDefault().post(paire);
                return;
            }
            if (frame[3] != 22) {
                if (frame[3] == 0 && frame[4] == -120) {
                    Log.i(TAG, "是握手了,也叫心跳,最新版本 不支持了");
                    EventBus.getDefault().post(new HeartbeatEvent(0));
                    return;
                } else {
                    if (frame[3] == -75) {
                        EventBus.getDefault().post(new TimeSeedEvent(frame[4]));
                        return;
                    }
                    return;
                }
            }
            return;
        }
        if (cmd == 9) {
            SLOG.LogByteArr("find", frame, frame.length);
            QueryIDOkEvent paire2 = new QueryIDOkEvent();
            if (frame[3] != 1) {
                if (frame[3] == 2) {
                    paire2.tires = 2;
                    log = "右前";
                } else if (frame[3] == 3) {
                    log = "左后";
                    paire2.tires = 0;
                } else if (frame[3] == 4) {
                    log = "右后";
                    paire2.tires = 3;
                } else {
                    log = "备胎";
                    paire2.tires = 5;
                }
            } else {
                log = "左前";
                paire2.tires = 1;
            }
            paire2.mID = Util.byteToUpperString(frame[4]) + Util.byteToUpperString(frame[5]) + Util.byteToUpperString(frame[6]) + Util.byteToUpperString(frame[7]);
            Log.i(TAG, "查寻反回的id " + log + ";id:" + paire2.mID);
            EventBus.getDefault().post(paire2);
            return;
        }
        if (cmd != 10 && cmd == 7 && frame[3] == 48) {
            TiresExchangeEvent event2 = new TiresExchangeEvent("左前右前");
            if (frame[4] == 0 && frame[5] == 1) {
                Log.i(TAG, "左前右前 调换");
                event2 = new TiresExchangeEvent("左前右前");
            } else if (frame[4] == 0 && frame[5] == 16) {
                Log.i(TAG, "左前左后 调换");
                event2 = new TiresExchangeEvent("左前左后");
            } else if (frame[4] == 0 && frame[5] == 17) {
                Log.i(TAG, "左前右后 调换");
                event2 = new TiresExchangeEvent("左前右后");
            } else if (frame[4] == 1 && frame[5] == 16) {
                Log.i(TAG, "右前左后 调换");
                event2 = new TiresExchangeEvent("右前左后");
            } else if (frame[4] == 1 && frame[5] == 17) {
                Log.i(TAG, "右前右后 调换");
                event2 = new TiresExchangeEvent("右前右后");
            } else if (frame[4] == 16 && frame[5] == 17) {
                Log.i(TAG, "左后右后 调换");
                event2 = new TiresExchangeEvent("左后右后");
            } else if (frame[4] == 16 || frame[5] == 5) {
                event2 = new TiresExchangeEvent("备胎交换");
            }
            EventBus.getDefault().post(event2);
        }
    }

    @Override // com.tpms.decode.FrameDecode
    public void decodeAlarmAgrsProc(byte b1, byte b2, byte b3) {
        AlarmAgrs alargs = new AlarmAgrs();
        alargs.AirPressureHi = (b1 & 255) * 10;
        alargs.AirPressureLo = (b2 & 255) * 10;
        alargs.Temperature = b3 & 255;
        EventBus.getDefault().post(alargs);
    }
}
