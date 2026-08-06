package com.tpms.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.std.dev.TpmsDataSrc;
import com.syt.tmps.R;
import com.syt.tmps.TpmsApplication;

/* JADX INFO: loaded from: classes.dex */
public class TestScorActivity extends Activity {
    private String TAG = "MainActivity";
    TpmsDataSrc datasrc = null;
    boolean player = true;
    TpmsApplication app = null;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_set);
        this.app = (TpmsApplication) getApplication();
        this.datasrc = this.app.getDataSrc();
    }

    public void onClick(View v) {
    }

    private void sleep() {
        try {
            Thread.sleep(300L);
        } catch (InterruptedException e) {
//            ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;

        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }
}
