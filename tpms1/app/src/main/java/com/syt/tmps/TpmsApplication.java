package com.syt.tmps;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.os.Process;
import com.std.dev.TpmsDataSrc;
import com.std.dev.TpmsDataSrcUsb;
//import com.tencent.bugly.Bugly; REMOVED! WOW!
import com.tpms.biz.Tpms;
import com.tpms.biz.Tpms3;
import com.tpms.utils.Log;

/* JADX INFO: loaded from: classes.dex */
public class TpmsApplication extends Application {
    private Service mAppService;
    private Tpms tpms;
    public String TAG = TpmsApplication.class.getSimpleName();
    TpmsDataSrc datasrc = null;
    BKReceiver mReceive = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.syt.tmps.TpmsApplication.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (!action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
                if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                    Log.e(TpmsApplication.this.TAG, " ACTION_USB_ACCESSORY_ATTACHED usb 插入");
                    TpmsApplication.this.startTpms();
                    return;
                }
                return;
            }
            UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
            if (device != null) {
                String name = device.getDeviceName();
                int did = device.getDeviceId();
                Log.i(TpmsApplication.this.TAG, "==================================name:" + name + ";did:" + did);
                if (TpmsApplication.this.datasrc == null) {
                    Log.i(TpmsApplication.this.TAG, "datasrc==null");
                } else if (name.equals(TpmsApplication.this.datasrc.getDevName())) {
                    Log.i(TpmsApplication.this.TAG, "kill safe");
                    Process.myPid();
                    TpmsApplication.this.stopTpms();
                }
            }
        }
    };

    public void attachService(Service service) {
        this.mAppService = service;
    }

    public Service getTpmsServices() {
        return this.mAppService;
    }

    public TpmsApplication() {
        Log.i(this.TAG, "BTApplication tid:" + Thread.currentThread().getId());
    }

    public TpmsDataSrc getDataSrc() {
        return this.datasrc;
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        Log.init(this);
        Log.setLogToFile(false);
        Log.i(this.TAG, "App is onCreate tid:" + Thread.currentThread().getId());
        startService(new Intent(this, (Class<?>) TpmsService.class));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        registerReceiver(this.mReceiver, filter);
        startTpms();
    }

    @Override // android.app.Application
    public void onTerminate() {
        super.onTerminate();
        Log.i(this.TAG, "App is onTerminate tid:" + Thread.currentThread().getId());
    }

    public Tpms getTpms() {
        return this.tpms;
    }

    public void startTpms() {
        Log.i(this.TAG, "startTpms");
        if (this.datasrc == null) {
            this.datasrc = new TpmsDataSrcUsb(this);
            this.datasrc.init();
            this.tpms = new Tpms3(this);
            this.tpms.init();
            this.datasrc.setBufferFrame(this.tpms.getDecode().getPackBufferFrame());
        }
        this.datasrc.start();
        this.tpms.initShakeHand();
    }

    public void stopTpms() {
        Log.i(this.TAG, "stopTpms");
        if (this.datasrc != null) {
            this.datasrc.stop();
        }
        if (this.tpms != null) {
            this.tpms.unintShakeHand();
        }
    }
}
