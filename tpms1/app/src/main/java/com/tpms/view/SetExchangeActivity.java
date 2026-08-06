package com.tpms.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.std.dev.TpmsDataSrc;
import com.syt.tmps.R;
import com.syt.tmps.TpmsApplication;
import com.tpms.biz.Tpms;
import com.tpms.modle.TiresExchangeEvent;
import com.tpms.utils.Log;
import com.tpms.widget.CDialog;
import com.tpms.widget.PAlertDialog;
import de.greenrobot.event.EventBus;

/* JADX INFO: loaded from: classes.dex */
public class SetExchangeActivity extends Fragment {

    @ViewInject(R.id.btn_cannel_exchange)
    Button btn_cannel_exchange;

    @ViewInject(R.id.btn_sp_bl)
    Button btn_sp_bl;

    @ViewInject(R.id.btn_sp_br)
    Button btn_sp_br;

    @ViewInject(R.id.btn_sp_fl)
    Button btn_sp_fl;

    @ViewInject(R.id.btn_sp_fr)
    Button btn_sp_fr;

    @ViewInject(R.id.btn_start_exchange)
    Button btn_start_exchange;

    @ViewInject(R.id.iv_exchange)
    ImageView iv_exchange;
    CDialog mExChangeFailed;
    Toast mExChangeOk;
    Handler mFailedHander;
    AlertDialog mPDlg;
    Button mSelectBtn;
    Tpms mTpms;

    @ViewInject(R.id.tv_exchange_hint)
    TextView tv_exchange_hint;
    private String TAG = "SetExchangeActivity";
    TpmsDataSrc datasrc = null;
    TpmsApplication app = null;
    private final BroadcastReceiver filterReceiver = new BroadcastReceiver() { // from class: com.tpms.view.SetExchangeActivity.1
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
    Runnable mFailedAble = new Runnable() { // from class: com.tpms.view.SetExchangeActivity.2
        @Override // java.lang.Runnable
        public void run() {
            SetExchangeActivity.this.mExChangeFailed.show();
            if (SetExchangeActivity.this.mPDlg != null) {
                SetExchangeActivity.this.mPDlg.dismiss();
                SetExchangeActivity.this.mPDlg = null;
            }
            SetExchangeActivity.this.btn_cannel_exchange(SetExchangeActivity.this.btn_cannel_exchange);
        }
    };

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exchange2, (ViewGroup) null);
        ViewUtils.inject(this, view);
        this.app = (TpmsApplication) getActivity().getApplication();
        EventBus.getDefault().register(this);
        this.datasrc = this.app.getDataSrc();
        this.mTpms = this.app.getTpms();
        this.btn_cannel_exchange.setVisibility(8);
        this.mExChangeOk = Toast.makeText(getActivity(), getString(R.string.jiaohuanchenggong), 2000);
        this.mExChangeFailed = new CDialog(getActivity(), R.layout.confirm_dialog);
        this.mFailedHander = new Handler();
        boolean spret = this.app.getTpms().getSparetireEnable();
        showLeftTires(!spret);
        return view;
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override // android.app.Fragment
    public void onHiddenChanged(boolean arg0) {
        super.onHiddenChanged(arg0);
        Log.i(this.TAG, "onHiddenChanged :" + arg0);
        if (getView() == null) {
            Log.i(this.TAG, "还没有创建view");
        } else if (arg0) {
            this.mFailedHander.removeCallbacks(this.mFailedAble);
        } else {
            boolean spret = this.app.getTpms().getSparetireEnable();
            showLeftTires(!spret);
        }
    }

    private void showLeftTires(boolean show) {
        if (show) {
            this.btn_sp_fl.setVisibility(4);
            this.btn_sp_fr.setVisibility(4);
            this.btn_sp_bl.setVisibility(4);
            this.btn_sp_br.setVisibility(4);
            this.iv_exchange.setImageLevel(0);
            btn_cannel_exchange(null);
            return;
        }
        this.btn_sp_fl.setVisibility(0);
        this.btn_sp_fr.setVisibility(0);
        this.btn_sp_bl.setVisibility(0);
        this.btn_sp_br.setVisibility(0);
        btn_cannel_exchange(null);
        this.mSelectBtn = null;
    }

    @OnClick({R.id.back_exchange})
    public void back_exchange(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(1);
    }

    @OnClick({R.id.deputy_cross})
    public void deputy_cross(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(2);
    }

    @OnClick({R.id.deputy_master})
    public void deputy_master(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(5);
    }

    @OnClick({R.id.deputy_updown})
    public void deputy_updown(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(6);
    }

    @OnClick({R.id.master_cross})
    public void master_cross(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(4);
    }

    @OnClick({R.id.mater_updown})
    public void mater_updown(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(3);
    }

    @OnClick({R.id.btn_sp_fl})
    public void btn_sp_fl(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(9);
    }

    @OnClick({R.id.btn_sp_fr})
    public void btn_sp_fr(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(10);
    }

    @OnClick({R.id.btn_sp_bl})
    public void btn_sp_bl(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(7);
    }

    @OnClick({R.id.btn_sp_br})
    public void btn_sp_br(View v) {
        setPress(v);
        this.iv_exchange.getBackground().setLevel(8);
    }

    @OnClick({R.id.btn_cannel_exchange})
    public void btn_cannel_exchange(View v) {
        setUnPress();
        boolean spret = this.mTpms.getSparetireEnable();
        if (spret) {
            this.iv_exchange.setBackgroundResource(R.drawable.exchange_sptires_level);
        } else {
            this.iv_exchange.setBackgroundResource(R.drawable.exchange_nosptires_level);
        }
        this.iv_exchange.getBackground().setLevel(0);
        this.tv_exchange_hint.setText("");
        this.mFailedHander.removeCallbacks(this.mFailedAble);
    }

    @OnClick({R.id.btn_start_exchange})
    public void btn_start_exchange(View v) {
        if (this.mSelectBtn == null) {
            return;
        }
        int id = this.mSelectBtn.getId();
        if (id == R.id.back_exchange) {
            this.mTpms.exchangeLeftBackRightBack();
        } else {
            switch (id) {
                case R.id.btn_sp_bl /* 2131165234 */:
                    this.mTpms.exchange_sp_bl();
                    break;
                case R.id.btn_sp_br /* 2131165235 */:
                    this.mTpms.exchange_sp_br();
                    break;
                case R.id.btn_sp_fl /* 2131165236 */:
                    this.mTpms.exchange_sp_fl();
                    break;
                case R.id.btn_sp_fr /* 2131165237 */:
                    this.mTpms.exchange_sp_fr();
                    break;
                default:
                    switch (id) {
                        case R.id.deputy_cross /* 2131165264 */:
                            this.mTpms.exchangeRightFrontLeftBack();
                            break;
                        case R.id.deputy_master /* 2131165265 */:
                            this.mTpms.exchangeLeftFrontRightFront();
                            break;
                        case R.id.deputy_updown /* 2131165266 */:
                            this.mTpms.exchangeRightFrontRightBack();
                            break;
                        default:
                            switch (id) {
                                case R.id.master_cross /* 2131165315 */:
                                    this.mTpms.exchangeLeftFrontRightBack();
                                    break;
                                case R.id.mater_updown /* 2131165316 */:
                                    this.mTpms.exchangeLeftFrontLeftBack();
                                    break;
                            }
                            break;
                    }
                    break;
            }
        }
        this.tv_exchange_hint.setText(this.mSelectBtn.getContentDescription());
        this.mPDlg = PAlertDialog.showDiolg(getActivity(), "");
        this.mFailedHander.postDelayed(this.mFailedAble, 2000L);
    }

    private void setPress(View v) {
        if (this.mSelectBtn != null) {
            this.mSelectBtn.getBackground().setLevel(0);
        }
        this.mSelectBtn = (Button) v;
        this.mSelectBtn.getBackground().setLevel(1);
        this.tv_exchange_hint.setText(this.mSelectBtn.getContentDescription());
    }

    private void setUnPress() {
        if (this.mSelectBtn != null) {
            this.mSelectBtn.getBackground().setLevel(0);
        }
        this.mSelectBtn = null;
    }

    public void onEventMainThread(TiresExchangeEvent ev) {
        Log.i(this.TAG, "交换成功 ev:" + ev.EventName);
        if (!ev.EventName.equals("左前右前") && !ev.EventName.equals("左前左后") && !ev.EventName.equals("左前右后") && !ev.EventName.equals("右前左后") && !ev.EventName.equals("右前右后")) {
            ev.EventName.equals("左后右后");
        }
        if (this.mPDlg != null) {
            this.mPDlg.dismiss();
            this.mPDlg = null;
        }
        this.mExChangeOk.show();
        btn_cannel_exchange(this.btn_cannel_exchange);
    }

    @Override // android.app.Fragment
    public void onStop() {
        this.mFailedHander.removeCallbacks(this.mFailedAble);
        super.onStop();
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        this.mFailedHander.removeCallbacks(this.mFailedAble);
        super.onDestroy();
    }

    @Override // android.app.Fragment
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i(this.TAG, "setUserVisibleHint:" + isVisibleToUser);
    }
}
