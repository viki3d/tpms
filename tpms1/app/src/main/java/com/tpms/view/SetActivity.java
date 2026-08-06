package com.tpms.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.std.dev.TpmsDataSrc;
import com.syt.tmps.R;
import com.syt.tmps.TpmsApplication;
import com.tpms.modle.TiresState;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class SetActivity extends Activity {
    TiresState mBackLeft;
    TiresState mBackRight;
    TiresState mFrontLeft;
    TiresState mFrontRight;
    private String TAG = "TpmsMainActivity";
    TpmsDataSrc datasrc = null;
    TpmsApplication app = null;
    private final BroadcastReceiver filterReceiver = new BroadcastReceiver() { // from class: com.tpms.view.SetActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String reason;
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) && (reason = intent.getStringExtra("reason")) != null && reason.equals("homekey")) {
                SetActivity.this.finish();
            }
        }
    };
    Map<String, Fragment> Fragments = new HashMap();

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        ViewUtils.inject(this);
        this.app = (TpmsApplication) getApplication();
        this.datasrc = this.app.getDataSrc();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        registerReceiver(this.filterReceiver, filter);
        initView();
    }

    public void onClick(View v) {
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
    }

    @Override // android.app.Activity
    protected void onPause() {
        finish();
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        unregisterReceiver(this.filterReceiver);
        super.onDestroy();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        super.onBackPressed();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Fragment ShowFragment(String className) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        try {
            Fragment fg = this.Fragments.get(className);
            if (fg == null) {
                fg = (Fragment) Class.forName(className).newInstance();
                this.Fragments.put(className, fg);
                ft.add(R.id.fragment_container, fg);
            }
            for (Map.Entry<String, Fragment> entry : this.Fragments.entrySet()) {
                ft.hide(entry.getValue());
            }
            ft.show(fg);
            ft.commit();
            return fg;
        } catch (Exception e) {
            //ThrowableExtension.printStackTrace(e);
            e.printStackTrace();

            return null;
        }
    }

    private void initView() {
        String bshowfragment = getIntent().getStringExtra("framgent");
        if (!TextUtils.isEmpty(bshowfragment)) {
            ShowFragment(bshowfragment);
        } else {
            ShowFragment("com.tpms.view.SetDetailActivity");
        }
        RadioGroup RG = (RadioGroup) findViewById(R.id.tablable);
        RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: com.tpms.view.SetActivity.2
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            @SuppressLint({"CommitTransaction"})
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                try {
                    String fragmentClsName = (String) SetActivity.this.findViewById(checkedId).getTag();
                    SetActivity.this.ShowFragment(fragmentClsName);
                } catch (Exception e) {
//                    ThrowableExtension.printStackTrace(e);
                    e.printStackTrace();

                }
            }
        });
    }

    @OnClick({R.id.back_ui})
    public void back_ui(View v) {
        finish();
    }
}
