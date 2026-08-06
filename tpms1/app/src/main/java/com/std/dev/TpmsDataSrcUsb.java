package com.std.dev;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.syt.tmps.TpmsApplication;
import com.tpms.decode.PackBufferFrame;
import com.tpms.modle.DeviceOpenEvent;
import com.tpms.utils.Log;
import de.greenrobot.event.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes.dex */
public class TpmsDataSrcUsb extends TpmsDataSrc {
    private static final String ACTION_USB_PERMISSION = "com.android.cz.USB_PERMISSION";
    private String TAG;
    private List<UsbSerialPort> mEntries;
    private final ExecutorService mExecutor;
    boolean mIsStart;
    private final SerialInputOutputManager.Listener mListener;
    Handler mMainHander;
    PendingIntent mPermissionIntent;
    UsbSerialPort mPort;
    SerialInputOutputManager mSerialIoManager;
    private UsbManager mUsbManager;
    private final BroadcastReceiver mUsbReceiver;

    public TpmsDataSrcUsb(TpmsApplication app) {
        super(app);
        this.TAG = "TpmsDataSrcUsb";
        this.mUsbManager = null;
        this.mEntries = null;
        this.mIsStart = false;
        this.mExecutor = Executors.newSingleThreadExecutor();
        this.mListener = new SerialInputOutputManager.Listener() { // from class: com.std.dev.TpmsDataSrcUsb.2
            @Override // com.hoho.android.usbserial.util.SerialInputOutputManager.Listener
            public void onRunError(Exception e) {
                Log.d(TpmsDataSrcUsb.this.TAG, "Runner stopped. 读取报错，可能是断开了");
                TpmsDataSrcUsb.this.mMainHander.post(new Runnable() { // from class: com.std.dev.TpmsDataSrcUsb.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        EventBus.getDefault().post(new DeviceOpenEvent(false));
                    }
                });
            }

            @Override // com.hoho.android.usbserial.util.SerialInputOutputManager.Listener
            public void onNewData(byte[] data) {
                Log.e(TpmsDataSrcUsb.this.TAG, "usb read onNewData" + HexDump.dumpHexString(data));
                byte[] recBytes = new byte[data.length];
                System.arraycopy(data, 0, recBytes, 0, data.length);
                TpmsDataSrcUsb.this.BufferFrame.addBuffer(recBytes, recBytes.length);
            }
        };
        this.mUsbReceiver = new BroadcastReceiver() { // from class: com.std.dev.TpmsDataSrcUsb.3
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TpmsDataSrcUsb.ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
                        if (intent.getBooleanExtra("permission", false)) {
                            if (device != null) {
                                Log.i("UsbR", "permission granted for device " + device);
                                Log.i("UsbR", "getInterfaceCount:" + device.getInterfaceCount());
                                TpmsDataSrcUsb.this.onStartUsbConnent();
                            } else {
                                Log.i("usb", "no permission " + device);
                            }
                        } else {
                            Log.i("usb", "permission denied for device " + device);
                        }
                    }
                }
            }
        };
        this.mMainHander = new Handler();
    }

    @Override // com.std.dev.TpmsDataSrc
    public void init() {
        this.mUsbManager = (UsbManager) this.theapp.getSystemService("usb");
        this.mEntries = new ArrayList();
        this.mPermissionIntent = PendingIntent.getBroadcast(this.theapp, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter1 = new IntentFilter(ACTION_USB_PERMISSION);
        this.theapp.registerReceiver(this.mUsbReceiver, filter1);
        EventBus.getDefault().register(this);
    }

    @Override // com.std.dev.TpmsDataSrc
    public void setBufferFrame(PackBufferFrame frame) {
        this.BufferFrame = frame;
    }

    @Override // com.std.dev.TpmsDataSrc
    public void writeData(byte[] databuf) {
        if (this.mSerialIoManager != null) {
            try {
                this.mSerialIoManager.writeAsync(databuf);
            } catch (Exception e) {
            }
            Log.i(this.TAG, " usb writeAsync " + HexDump.dumpHexString(databuf));
            return;
        }
        Log.e(this.TAG, " usb writeAsync mSerialIoManager =null ");
    }

    @Override // com.std.dev.TpmsDataSrc
    public void start() {
        Log.i(this.TAG, "start mIsStart:" + this.mIsStart);
        if (this.mIsStart) {
            return;
        }
        startReadThread();
        this.mIsStart = true;
    }

    @Override // com.std.dev.TpmsDataSrc
    public void stop() {
        Log.i(this.TAG, "stop mIsStart:" + this.mIsStart);
        if (!this.mIsStart) {
            return;
        }
        stopIoManager();
        if (this.mPort != null) {
            try {
                this.mPort.close();
            } catch (Exception e) {
//                ThrowableExtension.printStackTrace(e);
                e.printStackTrace();;

            }
            this.mPort = null;
        }
        this.mIsStart = false;
    }

    private void startReadThread() {
        if (this.mPort == null) {
            onStartUsbConnent();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.std.dev.TpmsDataSrcUsb$1] */
    public void onStartUsbConnent() {
        new AsyncTask<Void, Void, List<UsbSerialPort>>() { // from class: com.std.dev.TpmsDataSrcUsb.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public List<UsbSerialPort> doInBackground(Void... params) {
                Log.d(TpmsDataSrcUsb.this.TAG, "Refreshing device list 刷新设备列表 ...");
                TpmsDataSrcUsb.this.sleep(1000L);
                List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(TpmsDataSrcUsb.this.mUsbManager);
                List<UsbSerialPort> result = new ArrayList<>();
                for (UsbSerialDriver driver : drivers) {
                    List<UsbSerialPort> ports = driver.getPorts();
                    String str = TpmsDataSrcUsb.this.TAG;
                    Object[] objArr = new Object[3];
                    objArr[0] = driver;
                    objArr[1] = Integer.valueOf(ports.size());
                    objArr[2] = ports.size() == 1 ? "" : "s";
                    Log.d(str, String.format("+ %s: %s port%s", objArr));
                    result.addAll(ports);
                }
                return result;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(List<UsbSerialPort> result) {
                TpmsDataSrcUsb.this.mEntries.clear();
                TpmsDataSrcUsb.this.mEntries.addAll(result);
                Log.d(TpmsDataSrcUsb.this.TAG, "Done refreshing, " + TpmsDataSrcUsb.this.mEntries.size() + " entries found.");
                if (TpmsDataSrcUsb.this.mEntries.size() == 0) {
                    EventBus.getDefault().post(new DeviceOpenEvent(false));
                }
                for (int i = 0; i < TpmsDataSrcUsb.this.mEntries.size(); i++) {
                    UsbSerialPort port = (UsbSerialPort) TpmsDataSrcUsb.this.mEntries.get(i);
                    if (port != null) {
                        if ("1027_24577".equals(port.getDriver().getDevice().getVendorId() + "_" + port.getDriver().getDevice().getProductId())) {
                            TpmsDataSrcUsb.this.openUsbPort(port);
                            Log.i(TpmsDataSrcUsb.this.TAG, "onPostExecute 1027_24577");
                        } else {
                            if ("1027_24597".equals(port.getDriver().getDevice().getVendorId() + "_" + port.getDriver().getDevice().getProductId())) {
                                TpmsDataSrcUsb.this.openUsbPort(port);
                                Log.i(TpmsDataSrcUsb.this.TAG, "onPostExecute 1027_24597");
                            } else {
                                if ("6790_29987".equals(port.getDriver().getDevice().getVendorId() + "_" + port.getDriver().getDevice().getProductId())) {
                                    TpmsDataSrcUsb.this.openUsbPort(port);
                                    Log.i(TpmsDataSrcUsb.this.TAG, "onPostExecute 6790_29987");
                                }
                            }
                        }
                    }
                }
            }
        }.execute((Void) null);
    }

    private void startIoManager() {
        if (this.mPort != null) {
            Log.i(this.TAG, "Starting io manager ..");
            this.mSerialIoManager = new SerialInputOutputManager(this.mPort, this.mListener);
            this.mExecutor.submit(this.mSerialIoManager);
        }
    }

    private void stopIoManager() {
        if (this.mSerialIoManager != null) {
            Log.i(this.TAG, "Stopping io manager ..");
            this.mSerialIoManager.stop();
            this.mSerialIoManager = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openUsbPort(UsbSerialPort Port) {
        UsbInterface mInterface = null;
        this.mPort = Port;
        if (this.mPort == null) {
            EventBus.getDefault().post(new DeviceOpenEvent(false));
            return;
        }
        Log.i(this.TAG, "interfacecount:" + this.mPort.getDriver().getDevice().getInterfaceCount());
        if (0 < this.mPort.getDriver().getDevice().getInterfaceCount()) {
            UsbInterface usbInterface = this.mPort.getDriver().getDevice().getInterface(0);
            mInterface = usbInterface;
        }
        if (mInterface == null) {
            Log.e(this.TAG, "USB device NO  Interface");
            EventBus.getDefault().post(new DeviceOpenEvent(false));
            return;
        }
        UsbDevice dev = this.mPort.getDriver().getDevice();
        sysSetPerMission(dev);
        if (this.mUsbManager.hasPermission(dev)) {
            UsbDeviceConnection connection = this.mUsbManager.openDevice(this.mPort.getDriver().getDevice());
            if (connection == null) {
                Log.e(this.TAG, "Error openDevice:  connection " + connection);
                EventBus.getDefault().post(new DeviceOpenEvent(false));
                return;
            }
            try {
                this.mPort.open(connection);
                try {
                    this.mPort.setParameters(19200, 8, 1, 0);
                    EventBus.getDefault().post(new DeviceOpenEvent(true));
                    Log.i(this.TAG, "port name:" + this.mPort.getClass().getSimpleName());
                    onDeviceStateChange();
                    return;
                } catch (Exception e) {
                    Log.e(this.TAG, "Error setting up device: " + e.getMessage());
                    EventBus.getDefault().post(new DeviceOpenEvent(false));
                    return;
                }
            } catch (Exception e2) {
                Log.e(this.TAG, " usb open device: " + e2.getMessage());
                EventBus.getDefault().post(new DeviceOpenEvent(false));
                return;
            }
        }
        Log.e(this.TAG, "permission denied for device else");
        this.mUsbManager.requestPermission(this.mPort.getDriver().getDevice(), this.mPermissionIntent);
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sleep(long ms) {
        try {
            SystemClock.sleep(1000L);
        } catch (Exception e) {
        }
    }

    public void onEventMainThread(DeviceOpenEvent open) {
        if (!open.mOpen) {
            this.theapp.stopTpms();
        }
    }

    private boolean sysSetPerMission(UsbDevice dev) {
        sleep(500L);
        return true;
    }

    @Override // com.std.dev.TpmsDataSrc
    public String getDevName() {
        try {
            return this.mPort.getDriver().getDevice().getDeviceName();
        } catch (Exception e) {
            return "";
        }
    }
}
