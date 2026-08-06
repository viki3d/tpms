package com.tpms.view;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.lidroid.xutils.view.annotation.event.OnCompoundButtonCheckedChange;
import com.std.dev.TpmsDataSrc;
import com.syt.tmps.R;
import com.syt.tmps.TpmsApplication;
import com.tpms.biz.Tpms;
import com.tpms.modle.TiresState;
import com.tpms.utils.Log;
import com.tpms.widget.CDialog;

/* JADX INFO: loaded from: classes.dex */
public class SetDetailActivity extends Fragment {

    @ViewInject(R.id.cb_betta_warring)
    ToggleButton cb_betta_warring;

    @ViewInject(R.id.cb_connect_warring)
    ToggleButton cb_connect_warring;

    @ViewInject(R.id.cb_showui_enable)
    ToggleButton cb_showui_enable;

    @ViewInject(R.id.cb_sound_warring)
    ToggleButton cb_sound_warring;

    @ViewInject(R.id.cb_spare_tire_enable)
    ToggleButton cb_spare_tire_enable;
    TiresState mBackLeft;
    TiresState mBackRight;
    TiresState mFrontLeft;
    TiresState mFrontRight;
    Tpms mTpms;
    CDialog resetDlg;

    @ViewInject(R.id.tv_hi_temp)
    TextView tv_hi_temp;

    @ViewInject(R.id.tv_hipressure)
    TextView tv_hipressure;

    @ViewInject(R.id.tv_lopressure)
    TextView tv_lopressure;

    @ViewInject(R.id.tv_pressureunit)
    TextView tv_pressureunit;

    @ViewInject(R.id.tv_tempunit)
    TextView tv_tempunit;
    private String TAG = "TpmsMainActivity";
    TpmsDataSrc datasrc = null;
    TpmsApplication app = null;

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set, (ViewGroup) null);
        ViewUtils.inject(this, view);
        this.app = (TpmsApplication) getActivity().getApplication();
        this.datasrc = this.app.getDataSrc();
        this.mTpms = this.app.getTpms();
        initView();
        return view;
    }

    public void initView() {
        this.tv_hi_temp.setText(this.mTpms.getTempString(this.mTpms.getHiTemp()) + this.mTpms.getWenduDanwei());
        this.tv_tempunit.setText(this.mTpms.getWenduDanwei());
        this.tv_hipressure.setText(this.mTpms.getPressString(this.mTpms.getHiPress()) + this.mTpms.getYaliDanwei());
        this.tv_lopressure.setText(this.mTpms.getPressString(this.mTpms.getLowPress()) + this.mTpms.getYaliDanwei());
        this.tv_pressureunit.setText(this.mTpms.getYaliDanwei());
        this.cb_showui_enable.setChecked(this.mTpms.getShowUiEnable());
        this.cb_sound_warring.setChecked(this.mTpms.getSoundWarringEnable());
        this.cb_betta_warring.setChecked(this.mTpms.getBettaWarringEnable());
        this.cb_connect_warring.setChecked(this.mTpms.getConnectWarringEnable());
        this.cb_spare_tire_enable.setChecked(this.mTpms.getSparetireEnable());
    }

    @Override // android.app.Fragment
    public void onHiddenChanged(boolean arg0) {
        super.onHiddenChanged(arg0);
        Log.i(this.TAG, "onHiddenChanged :" + arg0);
        if (getView() == null) {
            Log.i(this.TAG, "还没有创建view");
        }
    }

    @OnCompoundButtonCheckedChange({R.id.cb_showui_enable})
    public void cb_showui_enable(CompoundButton checkBox, boolean isChecked) {
        this.mTpms.setShowUiEnable(isChecked);
    }

    @OnCompoundButtonCheckedChange({R.id.cb_sound_warring})
    public void cb_sound_warring(CompoundButton checkBox, boolean isChecked) {
        this.mTpms.setSoundWarringEnable(isChecked);
    }

    @OnCompoundButtonCheckedChange({R.id.cb_betta_warring})
    public void cb_betta_warring(CompoundButton checkBox, boolean isChecked) {
        this.mTpms.setBettaWarringEnable(isChecked);
    }

    @OnCompoundButtonCheckedChange({R.id.cb_connect_warring})
    public void cb_connect_warring(CompoundButton checkBox, boolean isChecked) {
        this.mTpms.setConnectWarringEnable(isChecked);
    }

    @OnCompoundButtonCheckedChange({R.id.cb_spare_tire_enable})
    public void cb_spare_tire_enable(CompoundButton checkBox, boolean isChecked) {
        this.mTpms.setSparetireEnable(isChecked);
    }

    @OnClick({R.id.btn_temp_def})
    public void btn_temp_def(View v) {
        this.tv_hi_temp.setText(this.mTpms.getTempString(this.mTpms.setHiTempDef()) + this.mTpms.getWenduDanwei());
    }

    @OnClick({R.id.btn_hipressure_def})
    public void btn_hipressure_def(View v) {
        this.tv_hipressure.setText(this.mTpms.getPressString(this.mTpms.setHiPressDef()) + this.mTpms.getYaliDanwei());
    }

    @OnClick({R.id.btn_lowpressure_def})
    public void btn_lowpressure_def(View v) {
        this.tv_lopressure.setText(this.mTpms.getPressString(this.mTpms.setLowPressDef()) + this.mTpms.getYaliDanwei());
    }

    @OnClick({R.id.btn_temp_dec})
    public void btn_temp_dec(View v) {
        this.tv_hi_temp.setText(this.mTpms.getTempString(this.mTpms.decHiTemp()) + this.mTpms.getWenduDanwei());
    }

    @OnClick({R.id.btn_hipressure_dec})
    public void btn_hipressure_dec(View v) {
        Log.i("test", "hipressure:" + this.mTpms.getHiPress());
        this.tv_hipressure.setText(this.mTpms.getPressString(this.mTpms.decHiPressStamp()) + this.mTpms.getYaliDanwei());
    }

    @OnClick({R.id.btn_lowpressure_dec})
    public void btn_lowpressure_dec(View v) {
        this.tv_lopressure.setText(this.mTpms.getPressString(this.mTpms.decLowPressStamp()) + this.mTpms.getYaliDanwei());
    }

    @OnClick({R.id.btn_temp_add})
    public void btn_temp_add(View v) {
        this.tv_hi_temp.setText(this.mTpms.getTempString(this.mTpms.addHiTemp()) + this.mTpms.getWenduDanwei());
    }

    @OnClick({R.id.btn_hipressure_add})
    public void btn_hipressure_add(View v) {
        this.tv_hipressure.setText(this.mTpms.getPressString(this.mTpms.addHiPressStamp()) + this.mTpms.getYaliDanwei());
    }

    @OnClick({R.id.btn_lowpressure_add})
    public void btn_lowpressure_add(View v) {
        this.tv_lopressure.setText(this.mTpms.getPressString(this.mTpms.addLowPressStamp()) + this.mTpms.getYaliDanwei());
    }

    @OnClick({R.id.btn_tempunit_dec})
    public void btn_tempunit_dec(View v) {
        String str = this.mTpms.setNextWenduDanwei();
        this.tv_tempunit.setText(str);
        this.tv_hi_temp.setText(this.mTpms.getTempString(this.mTpms.getHiTemp()) + this.mTpms.getWenduDanwei());
    }

    @OnClick({R.id.btn_tempunit_add})
    public void btn_tempunit_add(View v) {
        String str = this.mTpms.setNextWenduDanwei();
        this.tv_tempunit.setText(str);
        this.tv_hi_temp.setText(this.mTpms.getTempString(this.mTpms.getHiTemp()) + this.mTpms.getWenduDanwei());
    }

    @OnClick({R.id.btn_pressureunit_dec})
    public void btn_pressureunit_dec(View v) {
        String str = this.mTpms.setPreYaliDanwei();
        this.tv_pressureunit.setText(str);
        this.tv_hipressure.setText(this.mTpms.getPressString(this.mTpms.getHiPress()) + this.mTpms.getYaliDanwei());
        this.tv_lopressure.setText(this.mTpms.getPressString(this.mTpms.getLowPress()) + this.mTpms.getYaliDanwei());
    }

    @OnClick({R.id.btn_pressureunit_add})
    public void btn_pressureunit_add(View v) {
        String str = this.mTpms.setNextYaliDanwei();
        this.tv_pressureunit.setText(str);
        this.tv_hipressure.setText(this.mTpms.getPressString(this.mTpms.getHiPress()) + this.mTpms.getYaliDanwei());
        this.tv_lopressure.setText(this.mTpms.getPressString(this.mTpms.getLowPress()) + this.mTpms.getYaliDanwei());
    }

    @OnClick({R.id.tv_reset_all})
    public void tv_reset_all(View v) {
        View vi = getActivity().getLayoutInflater().inflate(R.layout.reset_dialog, (ViewGroup) null);
        this.resetDlg = new CDialog(getActivity(), vi);
        this.resetDlg.show();
        vi.findViewById(R.id.close_btn_ok).setOnClickListener(new View.OnClickListener() { // from class: com.tpms.view.SetDetailActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v2) {
                Log.i(SetDetailActivity.this.TAG, "点击了确定 恢复所有");
                SetDetailActivity.this.resetDlg.dismiss();
                SetDetailActivity.this.mTpms.resetAll();
                SetDetailActivity.this.initView();
            }
        });
    }
}
