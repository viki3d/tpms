package com.tpms.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.std.dev.TpmsDataSrc;
import com.syt.tmps.R;
import com.syt.tmps.TpmsApplication;
//import com.tencent.bugly.beta.tinker.TinkerReport;
import com.tpms.TinkerReport;
import com.tpms.modle.PaireIDOkEvent;
import com.tpms.modle.QueryIDOkEvent;
import com.tpms.utils.Log;
import de.greenrobot.event.EventBus;

/* JADX INFO: loaded from: classes.dex */
public class PaireIDActivity extends Activity {

    @ViewInject(R.id.btn_paire_canel)
    Button btn_paire_canel;

    @ViewInject(R.id.btn_paire_start)
    Button btn_paire_start;

    @ViewInject(R.id.ib_left_back_id)
    ImageButton ib_left_back_id;

    @ViewInject(R.id.ib_left_front_id)
    ImageButton ib_left_front_id;

    @ViewInject(R.id.ib_right_back_id)
    ImageButton ib_right_back_id;

    @ViewInject(R.id.ib_right_front_id)
    ImageButton ib_right_front_id;
    View mImgBtn;

    @ViewInject(R.id.progressBar1)
    ProgressBar progressBar1;
    Handler timeOut;

    @ViewInject(R.id.tires_container)
    LinearLayout tires_container;

    @ViewInject(R.id.tv_left_back_id)
    TextView tv_left_back_id;

    @ViewInject(R.id.tv_left_front_id)
    TextView tv_left_front_id;

    @ViewInject(R.id.tv_right_back_id)
    TextView tv_right_back_id;

    @ViewInject(R.id.tv_right_front_id)
    TextView tv_right_front_id;

    @ViewInject(R.id.tv_sptires_id)
    TextView tv_sptires_id;

    @ViewInject(R.id.tv_title_state)
    TextView tv_title_state;
    private String TAG = "PaireIDActivity";
    TpmsDataSrc datasrc = null;
    TpmsApplication app = null;
    int mTimeOut = TinkerReport.KEY_APPLIED_EXCEPTION;
    Runnable timeOutCnt = new Runnable() { // from class: com.tpms.view.PaireIDActivity.1
        @Override // java.lang.Runnable
        public void run() {
            if (PaireIDActivity.this.mTimeOut <= 0) {
                PaireIDActivity.this.timeOut.removeCallbacks(PaireIDActivity.this.timeOutCnt);
                PaireIDActivity.this.btn_paire_canel(PaireIDActivity.this.btn_paire_canel);
                PaireIDActivity.this.btn_paire_start.setVisibility(8);
                PaireIDActivity.this.tv_title_state.setText(R.string.dianjikaishianniujintupeidui);
                return;
            }
            TextView textView = PaireIDActivity.this.tv_title_state;
            StringBuilder sb = new StringBuilder();
            sb.append(PaireIDActivity.this.getString(R.string.zhengzaipeidui));
            PaireIDActivity paireIDActivity = PaireIDActivity.this;
            int i = paireIDActivity.mTimeOut;
            paireIDActivity.mTimeOut = i - 1;
            sb.append(i);
            textView.setText(sb.toString());
            PaireIDActivity.this.timeOut.postDelayed(PaireIDActivity.this.timeOutCnt, 1000L);
        }
    };

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paire_id);
        ViewUtils.inject(this);
        EventBus.getDefault().register(this);
        this.app = (TpmsApplication) getApplication();
        this.datasrc = this.app.getDataSrc();
        this.app.getTpms().querySensorID();
        this.btn_paire_start.setVisibility(4);
        this.btn_paire_canel.setVisibility(8);
        this.timeOut = new Handler();
        boolean spret = this.app.getTpms().getSparetireEnable();
        if (!spret) {
            this.tv_sptires_id.setVisibility(4);
        } else {
            this.tv_sptires_id.setVisibility(0);
        }
    }

    @OnClick({R.id.ib_left_front_id})
    public void ib_left_front_id(View v) {
        setSelectButton(v);
    }

    @OnClick({R.id.ib_right_front_id})
    public void ib_right_front_id(View v) {
        setSelectButton(v);
    }

    @OnClick({R.id.ib_right_back_id})
    public void ib_right_back_id(View v) {
        setSelectButton(v);
    }

    @OnClick({R.id.ib_left_back_id})
    public void ib_left_back_id(View v) {
        setSelectButton(v);
    }

    @OnClick({R.id.tv_sptires_id})
    public void tv_sptires_id(View v) {
        Log.i(this.TAG, "tv_sptires_id");
        setSelectButton(v);
    }

    private void setSelectButton(View v) {
        if (this.btn_paire_canel.getVisibility() == 0) {
            btn_paire_canel(this.btn_paire_canel);
            return;
        }
        if (this.mImgBtn != null) {
            this.mImgBtn.getBackground().setLevel(0);
        }
        this.mImgBtn = v;
        this.mImgBtn.getBackground().setLevel(1);
        this.btn_paire_start.setVisibility(0);
        this.btn_paire_canel.setVisibility(8);
        this.tv_title_state.setText(getString(R.string.dianjikaishianniujintupeidui));
    }

    @OnClick({R.id.btn_paire_canel})
    public void btn_paire_canel(View v) {
        this.btn_paire_start.setVisibility(4);
        this.btn_paire_canel.setVisibility(8);
        Log.i(this.TAG, "btn_paire_canel");
        this.tv_title_state.setText(R.string.qinxuanzeyaopeiduideluntai);
        this.progressBar1.setVisibility(8);
        this.app.getTpms().stopPaire();
        this.timeOut.removeCallbacks(this.timeOutCnt);
        if (this.mImgBtn != null) {
            this.mImgBtn.getBackground().setLevel(0);
        }
        this.tires_container.setVisibility(8);
    }

    @OnClick({R.id.btn_paire_start})
    public void btn_paire_start(View v) {
        if (this.mImgBtn == null) {
            return;
        }
        int selectid = this.mImgBtn.getId();
        if (selectid != R.id.tv_sptires_id) {
            switch (selectid) {
                case R.id.ib_left_back_id /* 2131165287 */:
                    this.app.getTpms().paireBackLeft();
                    break;
                case R.id.ib_left_front_id /* 2131165288 */:
                    this.app.getTpms().paireFrontLeft();
                    break;
                case R.id.ib_right_back_id /* 2131165289 */:
                    this.app.getTpms().paireBackRight();
                    break;
                case R.id.ib_right_front_id /* 2131165290 */:
                    this.app.getTpms().paireFrontRight();
                    break;
            }
        } else {
            this.app.getTpms().paireSpTired();
        }
        this.btn_paire_canel.setVisibility(0);
        this.btn_paire_start.setVisibility(8);
        this.tv_title_state.setText(getString(R.string.zhengzaipeidui) + this.mTimeOut);
        this.progressBar1.setVisibility(0);
        this.mTimeOut = TinkerReport.KEY_APPLIED_EXCEPTION;
        this.timeOut.postDelayed(this.timeOutCnt, 1000L);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.i(this.TAG, "onDestroy onDestroy");
        EventBus.getDefault().unregister(this);
        this.app.getTpms().stopPaire();
    }

    @Override // android.app.Activity
    protected void onStop() {
        Log.i(this.TAG, "onStop onStop");
        this.app.getTpms().stopPaire();
        super.onStop();
    }

    public void onEventMainThread(PaireIDOkEvent id) {
        Log.w(this.TAG, "收到了配对也就是学习到了ID:" + id.tires + ";mac:" + id.mID);
        if (id.tires == 1) {
            this.ib_left_front_id.getBackground().setLevel(0);
        } else if (id.tires == 2) {
            this.ib_right_front_id.getBackground().setLevel(0);
        } else if (id.tires == 3) {
            this.ib_right_back_id.getBackground().setLevel(0);
        } else if (id.tires == 0) {
            this.ib_left_back_id.getBackground().setLevel(0);
        } else if (id.tires == 5) {
            this.tv_sptires_id.getBackground().setLevel(0);
        }
        this.app.getTpms().querySensorID();
        if (this.mImgBtn != null) {
            this.mImgBtn.getBackground().setLevel(2);
        }
        if (this.btn_paire_canel.getVisibility() != 8) {
            Toast.makeText(this, getString(R.string.xuexichenggong), 2000).show();
        }
        btn_paire_canel(this.btn_paire_canel);
    }

    public void onEventMainThread(QueryIDOkEvent id) {
        Log.i(this.TAG, "收到了查寻ID:" + id.tires + ";mac:" + id.mID);
        if (id.tires == 1) {
            Log.i("test", "查到 左前id:" + id.mID);
            this.tv_left_front_id.setText("ID:" + id.mID);
            this.ib_left_front_id.getBackground().setLevel(0);
            return;
        }
        if (id.tires == 2) {
            Log.i("test", "查到 右前id:" + id.mID);
            this.tv_right_front_id.setText("ID:" + id.mID);
            this.ib_right_front_id.getBackground().setLevel(0);
            return;
        }
        if (id.tires == 3) {
            Log.i("test", "查到 右后id:" + id.mID);
            this.tv_right_back_id.setText("ID:" + id.mID);
            this.ib_right_back_id.getBackground().setLevel(0);
            return;
        }
        if (id.tires == 0) {
            Log.i("test", "查到 左后id:" + id.mID);
            this.tv_left_back_id.setText("ID:" + id.mID);
            this.ib_left_back_id.getBackground().setLevel(0);
            return;
        }
        if (id.tires == 5) {
            Log.i("test", "查到 备胎id:" + id.mID);
            this.tv_sptires_id.setText("ID:" + id.mID);
            this.tv_sptires_id.getBackground().setLevel(0);
        }
    }

    @OnClick({R.id.back_ui})
    public void back_ui(View v) {
        finish();
    }

    @OnClick({R.id.view_plane})
    public void view_plane(View v) {
        Log.i(this.TAG, "遮罩层，屏蔽下层的点击事件");
    }
}
