package com.tpms.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.syt.tmps.R;

/* JADX INFO: loaded from: classes.dex */
public class SoundPoolCtrl2 extends SoundPoolCtrl {
    String TAG;
    MediaPlayer mediaPlayer;

    public SoundPoolCtrl2(Context cont) {
        super(cont);
        this.TAG = "SoundPoolCtrl2";
        if (this.mediaPlayer == null) {
            this.mediaPlayer = MediaPlayer.create(cont, R.raw.alarm);
        }
    }

    @Override // com.tpms.utils.SoundPoolCtrl
    public void player(String guid) {
        Log.i(this.TAG, "player isPlayer:" + this.isPlayer + ";guid:" + guid);
        if (this.isPlayer) {
            return;
        }
        this.mediaPlayer.start();
        this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { // from class: com.tpms.utils.SoundPoolCtrl2.1
            @Override // android.media.MediaPlayer.OnCompletionListener
            public void onCompletion(MediaPlayer arg0) {
                if (!SoundPoolCtrl2.this.isPlayer) {
                    Log.i(SoundPoolCtrl2.this.TAG, "is over");
                } else {
                    SoundPoolCtrl2.this.mediaPlayer.start();
                    SoundPoolCtrl2.this.mediaPlayer.setLooping(true);
                }
            }
        });
        this.mGuid = guid;
        this.isPlayer = true;
    }

    @Override // com.tpms.utils.SoundPoolCtrl
    public void stop(String guid) {
        Log.i(this.TAG, "stop isPlayer:" + this.isPlayer + ";guid:" + guid);
        if (this.isPlayer) {
            if (TextUtils.isEmpty(guid) || guid.equals(this.mGuid)) {
                this.isPlayer = false;
                try {
                    this.mediaPlayer.pause();
                } catch (Exception e) {
//                    ThrowableExtension.printStackTrace(e);
                    e.printStackTrace();;
                }
                this.mGuid = "";
            }
        }
    }
}
