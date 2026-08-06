package com.tpms.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.text.TextUtils;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.syt.tmps.R;

/* JADX INFO: loaded from: classes.dex */
public class SoundPoolCtrl {
    AudioManager mAudioM;
    private int playerId = 0;
    String TAG = "SoundPoolCtrl";
    boolean isPlayer = false;
    String mGuid = "";
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() { // from class: com.tpms.utils.SoundPoolCtrl.1
        @Override // android.media.AudioManager.OnAudioFocusChangeListener
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == -2) {
                Log.i(SoundPoolCtrl.this.TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                return;
            }
            if (focusChange == -3) {
                Log.d(SoundPoolCtrl.this.TAG, "有应用申请了短焦点 我压低声音  AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:" + focusChange);
                return;
            }
            if (focusChange == 1) {
                Log.d(SoundPoolCtrl.this.TAG, "AUDIOFOCUS_GAIN");
                return;
            }
            if (focusChange == -1) {
                Log.d(SoundPoolCtrl.this.TAG, "AUDIOFOCUS_LOSS");
                return;
            }
            if (focusChange == 1) {
                Log.d(SoundPoolCtrl.this.TAG, "永久获取媒体焦点（播放音乐）现在没有播放 AUDIOFOCUS_REQUEST_GRANTED");
                return;
            }
            Log.i(SoundPoolCtrl.this.TAG, "focusChange:" + focusChange);
        }
    };
    private SoundPool soundPool = new SoundPool(10, 3, 100);

    public SoundPoolCtrl(Context cont) {
        this.mAudioM = null;
        this.soundPool.load(cont, R.raw.alarm, 1);
//        this.mAudioM = (AudioManager) cont.getSystemService("audio");
        this.mAudioM = (AudioManager) cont.getSystemService(android.content.Context.AUDIO_SERVICE);
    }

    public void player(String guid) {
        Log.i(this.TAG, "player isPlayer:" + this.isPlayer + ";guid:" + guid);
        if (this.isPlayer) {
            return;
        }
        this.playerId = this.soundPool.play(1, 15.0f, 15.0f, 1, -1, 1.0f);
        this.mGuid = guid;
        this.isPlayer = true;
    }

    public String getSoundGuid() {
        return this.mGuid;
    }

    public void stop(String guid) {
        Log.i(this.TAG, "stop isPlayer:" + this.isPlayer + ";guid:" + guid);
        if (this.isPlayer) {
            if (TextUtils.isEmpty(guid) || guid.equals(this.mGuid)) {
                try {
                    this.soundPool.stop(this.playerId);
                    this.playerId = 0;
                } catch (Exception e) {
                    //ThrowableExtension.printStackTrace(e);
                    e.printStackTrace();
                }
                this.isPlayer = false;
                this.mGuid = "";
            }
        }
    }
}
