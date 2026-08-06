package com.tpms.decode;

import android.content.Context;
import com.tpms.modle.AlarmAgrs;
import com.tpms.modle.PaireIDOkEvent;
import com.tpms.modle.QueryIDOkEvent;
import com.tpms.modle.ShakeHands;
import com.tpms.modle.TiresState;
import com.tpms.modle.TiresStateEvent;
import com.tpms.utils.Log;
import com.tpms.utils.SLOG;
import de.greenrobot.event.EventBus;
import java.nio.ByteBuffer;

/* JADX INFO: loaded from: classes.dex */
public class FrameDecode {
    private static String TAG = "FrameDecode";
    long downtime = 0;
    protected PackBufferFrame mPackBufferFrame;
    Context mctx;

    public void init(Context ctx) {
        this.mctx = ctx;
        EventBus.getDefault().register(this);
        this.mPackBufferFrame = new PackBufferFrame();
    }

    public PackBufferFrame getPackBufferFrame() {
        return this.mPackBufferFrame;
    }

    public void onEventMainThread(ByteBuffer frameBuf) {
        byte[] frame = frameBuf.array();
        byte cmd = frame[4];
        SLOG.LogByteArr(TAG, frame, frame.length);
        if (cmd == 33) {
            Log.i("find", "查寻反回基本参数");
            SLOG.LogByteArr("find", frame, frame.length);
            decodeAlarmAgrsProc(frame[6], frame[7], frame[8]);
        } else if (cmd == 17) {
            Log.i(TAG, "是握手了");
            EventBus.getDefault().post(new ShakeHands(0));
        } else if (cmd == 65) {
            Log.i("find", "查寻反回的id");
            SLOG.LogByteArr("find", frame, frame.length);
            QueryIDOkEvent paire = new QueryIDOkEvent();
            paire.tires = frame[5];
            paire.mID = Util.byteToUpperString(frame[7]) + Util.byteToUpperString(frame[6]);
            EventBus.getDefault().post(paire);
        } else if (cmd == 97) {
            Log.i("find", "配对反回的id");
            SLOG.LogByteArr("find", frame, frame.length);
            PaireIDOkEvent paire2 = new PaireIDOkEvent();
            paire2.tires = frame[5];
            paire2.mID = Util.byteToUpperString(frame[7]) + Util.byteToUpperString(frame[6]);
            EventBus.getDefault().post(paire2);
        } else if (cmd == 113) {
            Log.i(TAG, "轮胎状态");
            TiresStateEvent event = new TiresStateEvent();
            event.tires = frame[5];
            event.mState.AirPressure = (frame[6] & 255) + ((frame[7] & 255) << 8);
            Log.i(TAG, "气压参数:low:" + (frame[6] & 255) + ";hi:" + ((frame[7] & 255) << 8));
            event.mState.Temperature = frame[8] & 255;
            if ((frame[9] & (-128)) != 0) {
                Log.i(TAG, "无效数据");
                EventBus.getDefault().post(event);
                return;
            }
            if ((frame[9] & 32) == 0) {
                Log.i(TAG, "无效报警");
                EventBus.getDefault().post(event);
                return;
            }
            if ((frame[9] & 1) != 0) {
                StringBuilder sb = new StringBuilder();
                TiresState tiresState = event.mState;
                sb.append(tiresState.error);
                sb.append(" 低压");
                tiresState.error = sb.toString();
                Log.i(TAG, "低压");
            }
            if ((frame[9] & 2) != 0) {
                StringBuilder sb2 = new StringBuilder();
                TiresState tiresState2 = event.mState;
                sb2.append(tiresState2.error);
                sb2.append(" 高压");
                tiresState2.error = sb2.toString();
                Log.i(TAG, "高压");
            }
            if ((4 & frame[9]) != 0) {
                StringBuilder sb3 = new StringBuilder();
                TiresState tiresState3 = event.mState;
                sb3.append(tiresState3.error);
                sb3.append(" 高温");
                tiresState3.error = sb3.toString();
                Log.i(TAG, "高温");
            }
            if ((frame[9] & 8) != 0) {
                StringBuilder sb4 = new StringBuilder();
                TiresState tiresState4 = event.mState;
                sb4.append(tiresState4.error);
                sb4.append(" 漏气");
                tiresState4.error = sb4.toString();
                Log.i(TAG, "漏气");
            }
            if ((frame[9] & 16) != 0) {
                StringBuilder sb5 = new StringBuilder();
                TiresState tiresState5 = event.mState;
                sb5.append(tiresState5.error);
                sb5.append(" 低电");
                tiresState5.error = sb5.toString();
                Log.i(TAG, "低电");
            }
            EventBus.getDefault().post(event);
        } else if (cmd == -127) {
            Log.i(TAG, "协议版本号,日月,年,版本号");
        } else if (cmd == -1) {
            Log.i(TAG, "错误");
            if (frame[5] == 1) {
                Log.i(TAG, "通信错误");
            } else if (frame[5] == 2) {
                Log.i(TAG, "不支持该功能号");
            } else if (frame[5] != 3) {
                if (frame[5] == 4) {
                    Log.i(TAG, "写rom失败");
                } else if (frame[5] == 5) {
                    Log.i(TAG, "配对超时");
                } else if (frame[5] == 7) {
                    Log.i(TAG, "接收机RF错误");
                } else if (frame[5] == 8) {
                    Log.i(TAG, "压力传感器错误");
                } else if (frame[5] == 9) {
                    Log.i(TAG, "温度传感器错误");
                }
            } else {
                Log.i(TAG, "不支持该子功能号");
            }
            EventBus.getDefault().post(new ShakeHands(frame[5]));
        }
        Log.i(TAG, "cmd:" + ((int) cmd));
    }

    public void decodeAlarmAgrsProc(byte b1, byte b2, byte b3) {
        AlarmAgrs alargs = new AlarmAgrs();
        alargs.AirPressureHi = (b1 & 255) * 10;
        alargs.AirPressureLo = (b2 & 255) * 10;
        alargs.Temperature = b3 & 255;
        EventBus.getDefault().post(alargs);
    }
}
