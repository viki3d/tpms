package com.tpms.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.std.dev.TpmsDataSrc;
import com.syt.tmps.R;
import com.syt.tmps.TpmsApplication;
import com.tpms.utils.Log;
import com.tpms.utils.SoundPoolCtrl;
import com.tpms.widget.CDialog;
import com.tpms.widget.ClickToast;
import com.tpms.widget.PAlertDialog;

/* JADX INFO: loaded from: classes.dex */
public class TestActivity extends Activity {
    AudioManager mAudioManager;
    SoundPoolCtrl mSound;
    CDialog mdlg;
    NotificationManager notificationManager;
    CDialog resetDlg;
    private SoundPool soundPool;

    @ViewInject(R.id.tv_screen_info)
    TextView tv_screen_info;
    private String TAG = "MainActivity";
    TpmsDataSrc datasrc = null;
    boolean player = true;
    int volindex = 0;
    float speed = 0.3f;
    int playret = 0;
    TpmsApplication app = null;
    int mNotificationState = -1;
    private final BroadcastReceiver filterReceiver = new BroadcastReceiver() { // from class: com.tpms.view.TestActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String reason;
            String action = intent.getAction();
            if (!"android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || (reason = intent.getStringExtra("reason")) == null) {
                return;
            }
            reason.equals("homekey");
        }
    };
    ClickToast ctotast = null;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        ViewUtils.inject(this);
        this.app = (TpmsApplication) getApplication();
        this.datasrc = this.app.getDataSrc();
        this.notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        this.mSound = new SoundPoolCtrl(this);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.hand_work) {
            this.app.getTpms().shakeHand();
            return;
        }
        if (v.getId() == R.id.query) {
            this.app.getTpms().querySensorID();
            return;
        }
        if (v.getId() == R.id.query_front_left) {
            this.app.getTpms().queryFrontLeft();
        } else if (v.getId() == R.id.query_two_back) {
            this.app.getTpms().queryBackLeft();
            this.app.getTpms().queryBackRight();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(300L);
        } catch (InterruptedException e) {
//            ThrowableExtension.printStackTrace(e);
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

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            finish();
            onDestroy();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick({R.id.btn_time_select})
    public void btn_time_select(View v) {
        showTimeDialog();
    }

    @OnClick({R.id.btn_enter_apk})
    public void btn_enter_apk(View v) {
        startActivity(new Intent(this, (Class<?>) TpmsMainActivity.class));
    }

    @OnClick({R.id.btn_click_toast})
    public void btn_click_toast(View v) {
        this.ctotast = new ClickToast();
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.click_error_toast, (ViewGroup) null);
        view.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() { // from class: com.tpms.view.TestActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v2) {
                TestActivity.this.ctotast.hideCustomToast();
                TestActivity.this.showTimeDialog();
            }
        });
        this.ctotast.initToast(getApplicationContext(), view, "测试");
        this.ctotast.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showTimeDialog() {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.time_dialog, (ViewGroup) null);
        this.mdlg = new CDialog(this, view);
        RadioGroup rg = view.findViewById(R.id.time_select);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: com.tpms.view.TestActivity.3
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i(TestActivity.this.TAG, "showTimeDialog...:" + checkedId);
                TestActivity.this.mdlg.dismiss();
                TestActivity.this.ctotast = null;
            }
        });
        this.mdlg.show();
    }

    private void showTimeDialog_x() {
        final String[] items = {"10分钟内", "20分钟内", "30分钟内", "熄火前不再提示"};
        boolean[] zArr = {false, true, false, false};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog adlg = builder.setTitle("此轮胎相同警告不再提示").setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() { // from class: com.tpms.view.TestActivity.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(TestActivity.this, items[which], android.widget.Toast.LENGTH_LONG).show();
            }
        }).create();
        adlg.show();
    }

    @OnClick({R.id.btn_exchangeing})
    public void btn_exchangeing(View view) {
        long time = System.currentTimeMillis() / 1000;
        Log.i(this.TAG, "time:" + time);
        PAlertDialog.showDiolg(this, "");
    }

    @OnClick({R.id.btn_exchange_failed})
    public void btn_exchange_failed(View view) {
        new CDialog(this, R.layout.confirm_dialog).show();
    }

    @OnClick({R.id.btn_reset_data})
    public void btn_reset_data(View view) {
        View vi = getLayoutInflater().inflate(R.layout.reset_dialog, (ViewGroup) null);
        this.resetDlg = new CDialog(this, vi);
        this.resetDlg.show();
        vi.findViewById(R.id.close_btn_ok).setOnClickListener(new View.OnClickListener() { // from class: com.tpms.view.TestActivity.5
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                TestActivity.this.resetDlg.dismiss();
                Toast.makeText(TestActivity.this, "点击了关闭", Toast.LENGTH_LONG).show();
            }
        });
    }

    @OnClick({R.id.btn_open_usb})
    public void btn_open_usb(View v) {
        this.app.getDataSrc().start();
    }

    @OnClick({R.id.btn_close_usb})
    public void btn_close_usb(View v) {
        this.app.getDataSrc().stop();
    }

    @OnClick({R.id.btn_get_px})
    public void btn_get_px(View v) {
        this.tv_screen_info.setText("");
    }

    @OnClick({R.id.btn_notif_ok})
    public void btn_notif_ok(View v) {
        showNormalNotifMsg();
    }

    @OnClick({R.id.btn_notif_error})
    public void btn_notif_error(View v) {
        showErrorNotifMsg();
    }

    protected void showNormalNotifMsg() {
        Log.i(this.TAG, "showNormalNotifMsg mNotificationState:" + this.mNotificationState);
        if (this.mNotificationState == 1) {
            return;
        }
        this.notificationManager.cancel(1);
        Notification notification = new Notification(R.drawable.ic_notif_ok, "胎压", System.currentTimeMillis());
        notification.flags |= 2;
    }

    protected void showErrorNotifMsg() {
        Log.i(this.TAG, "showErrorNotifMsg mNotificationState:" + this.mNotificationState);
        if (this.mNotificationState == 0) {
            return;
        }
        this.notificationManager.cancel(1);
        Notification notification = new Notification(R.drawable.ic_notif_error, "胎压", System.currentTimeMillis());
        notification.flags |= 2;
        PendingIntent.getActivity(this.app, 0, new Intent(this.app, (Class<?>) TpmsMainActivity.class), 0);
    }

    @OnClick({R.id.btn_play_sound})
    public void btn_play_sound(View v) {
        this.mSound.player("1");
    }

    @OnClick({R.id.btn_stop_sound})
    public void btn_stop_sound(View v) {
        this.mSound.stop("1");
    }

    @OnClick({R.id.btn_stop_data})
    public void btn_stop_data(View v) {
        this.app.stopTpms();
    }

    @OnClick({R.id.btn_start_data})
    public void btn_start_data(View v) {
        this.app.startTpms();
    }

    @OnClick({R.id.btn_error})
    public void btn_error(View v) {
        int i = 1 / 0;
    }
}
