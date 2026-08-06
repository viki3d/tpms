package com.tpms.utils;

import android.media.AudioTrack;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public class AudioTrackPlayer {
    private byte[] audioData;
    private AudioTrack audioTrack;

    private void releaseAudioTrack() {
        if (this.audioTrack != null) {
            this.audioTrack.stop();
            this.audioTrack.release();
            this.audioTrack = null;
        }
    }

    public AudioTrackPlayer() {
        releaseAudioTrack();
        int min = AudioTrack.getMinBufferSize(44100, 12, 2);
        this.audioTrack = new AudioTrack(3, 44100, 12, 2, min, 0);
    }

    public boolean isPlaying() {
        return this.audioTrack.getPlayState() == 3;
    }

    public void load(InputStream in) {
        try {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream(264848);
                while (true) {
                    int b = in.read();
                    if (b == -1) {
                        break;
                    } else {
                        out.write(b);
                    }
                }
                this.audioData = out.toByteArray();
                in.close();
            } catch (Throwable th) {
                in.close();
                throw th;
            }
        } catch (IOException e) {
//            ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;
        }
        this.audioTrack.write(this.audioData, 0, this.audioData.length);
    }

    public void start() {
        this.audioTrack.write(this.audioData, 0, this.audioData.length);
        this.audioTrack.play();
    }

    public void pause() {
        this.audioTrack.pause();
    }
}
