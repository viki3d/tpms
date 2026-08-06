package com.hoho.android.usbserial.driver;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import com.tpms.utils.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class CdcAcmSerialDriver implements UsbSerialDriver {
    private final String TAG = CdcAcmSerialDriver.class.getSimpleName();
    private final UsbDevice mDevice;
    private final UsbSerialPort mPort;

    public CdcAcmSerialDriver(UsbDevice device) {
        this.mDevice = device;
        this.mPort = new CdcAcmSerialPort(device, 0);
    }

    @Override // com.hoho.android.usbserial.driver.UsbSerialDriver
    public UsbDevice getDevice() {
        return this.mDevice;
    }

    @Override // com.hoho.android.usbserial.driver.UsbSerialDriver
    public List<UsbSerialPort> getPorts() {
        return Collections.singletonList(this.mPort);
    }

    class CdcAcmSerialPort extends CommonUsbSerialPort {
        private static final int GET_LINE_CODING = 33;
        private static final int SEND_BREAK = 35;
        private static final int SET_CONTROL_LINE_STATE = 34;
        private static final int SET_LINE_CODING = 32;
        private static final int USB_RECIP_INTERFACE = 1;
        private static final int USB_RT_ACM = 33;
        private UsbEndpoint mControlEndpoint;
        private UsbInterface mControlInterface;
        private UsbInterface mDataInterface;
        private boolean mDtr;
        private final boolean mEnableAsyncReads;
        private UsbEndpoint mReadEndpoint;
        private boolean mRts;
        private UsbEndpoint mWriteEndpoint;

        public CdcAcmSerialPort(UsbDevice device, int portNumber) {
            super(device, portNumber);
            this.mRts = false;
            this.mDtr = false;
            this.mEnableAsyncReads = Build.VERSION.SDK_INT >= 17;
        }

        @Override // com.hoho.android.usbserial.driver.UsbSerialPort
        public UsbSerialDriver getDriver() {
            return CdcAcmSerialDriver.this;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void open(UsbDeviceConnection usbDeviceConnection) throws IOException {
            if (this.mConnection != null) {
                throw new IOException("Already open");
            }
            this.mConnection = usbDeviceConnection;
            boolean z = false;
            try {
                if (1 == this.mDevice.getInterfaceCount()) {
                    Log.d(CdcAcmSerialDriver.this.TAG, "device might be castrated ACM device, trying single interface logic");
                    openSingleInterface();
                } else {
                    Log.d(CdcAcmSerialDriver.this.TAG, "trying default interface logic");
                    openInterface();
                }
                if (this.mEnableAsyncReads) {
                    Log.d(CdcAcmSerialDriver.this.TAG, "Async reads enabled");
                } else {
                    Log.d(CdcAcmSerialDriver.this.TAG, "Async reads disabled.");
                }
                boolean z2 = true;
            } finally {
                if (!z) {
                    this.mConnection = null;
                    this.mControlEndpoint = null;
                    this.mReadEndpoint = null;
                    this.mWriteEndpoint = null;
                }
            }
        }

        private void openSingleInterface() throws IOException {
            this.mControlInterface = this.mDevice.getInterface(0);
            Log.d(CdcAcmSerialDriver.this.TAG, "Control iface=" + this.mControlInterface);
            this.mDataInterface = this.mDevice.getInterface(0);
            Log.d(CdcAcmSerialDriver.this.TAG, "data iface=" + this.mDataInterface);
            if (!this.mConnection.claimInterface(this.mControlInterface, true)) {
                throw new IOException("Could not claim shared control/data interface.");
            }
            int endCount = this.mControlInterface.getEndpointCount();
            if (endCount < 3) {
                Log.d(CdcAcmSerialDriver.this.TAG, "not enough endpoints - need 3. count=" + this.mControlInterface.getEndpointCount());
                throw new IOException("Insufficient number of endpoints(" + this.mControlInterface.getEndpointCount() + ")");
            }
            this.mControlEndpoint = null;
            this.mReadEndpoint = null;
            this.mWriteEndpoint = null;
            for (int i = 0; i < endCount; i++) {
                UsbEndpoint ep = this.mControlInterface.getEndpoint(i);
                if (ep.getDirection() == 128 && ep.getType() == 3) {
                    Log.d(CdcAcmSerialDriver.this.TAG, "Found controlling endpoint");
                    this.mControlEndpoint = ep;
                } else if (ep.getDirection() == 128 && ep.getType() == 2) {
                    Log.d(CdcAcmSerialDriver.this.TAG, "Found reading endpoint");
                    this.mReadEndpoint = ep;
                } else if (ep.getDirection() == 0 && ep.getType() == 2) {
                    Log.d(CdcAcmSerialDriver.this.TAG, "Found writing endpoint");
                    this.mWriteEndpoint = ep;
                }
                if (this.mControlEndpoint != null && this.mReadEndpoint != null && this.mWriteEndpoint != null) {
                    Log.d(CdcAcmSerialDriver.this.TAG, "Found all required endpoints");
                    break;
                }
            }
            if (this.mControlEndpoint == null || this.mReadEndpoint == null || this.mWriteEndpoint == null) {
                Log.d(CdcAcmSerialDriver.this.TAG, "Could not establish all endpoints");
                throw new IOException("Could not establish all endpoints");
            }
        }

        private void openInterface() throws IOException {
            Log.d(CdcAcmSerialDriver.this.TAG, "claiming interfaces, count=" + this.mDevice.getInterfaceCount());
            this.mControlInterface = this.mDevice.getInterface(0);
            Log.d(CdcAcmSerialDriver.this.TAG, "Control iface=" + this.mControlInterface);
            if (!this.mConnection.claimInterface(this.mControlInterface, true)) {
                throw new IOException("Could not claim control interface.");
            }
            this.mControlEndpoint = this.mControlInterface.getEndpoint(0);
            Log.d(CdcAcmSerialDriver.this.TAG, "Control endpoint direction: " + this.mControlEndpoint.getDirection());
            Log.d(CdcAcmSerialDriver.this.TAG, "Claiming data interface.");
            this.mDataInterface = this.mDevice.getInterface(1);
            Log.d(CdcAcmSerialDriver.this.TAG, "data iface=" + this.mDataInterface);
            if (!this.mConnection.claimInterface(this.mDataInterface, true)) {
                throw new IOException("Could not claim data interface.");
            }
            this.mReadEndpoint = this.mDataInterface.getEndpoint(1);
            Log.d(CdcAcmSerialDriver.this.TAG, "Read endpoint direction: " + this.mReadEndpoint.getDirection());
            this.mWriteEndpoint = this.mDataInterface.getEndpoint(0);
            Log.d(CdcAcmSerialDriver.this.TAG, "Write endpoint direction: " + this.mWriteEndpoint.getDirection());
        }

        private int sendAcmControlMessage(int request, int value, byte[] buf) {
            return this.mConnection.controlTransfer(33, request, value, 0, buf, buf != null ? buf.length : 0, 5000);
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void close() throws IOException {
            if (this.mConnection == null) {
                throw new IOException("Already closed");
            }
            this.mConnection.close();
            this.mConnection = null;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public int read(byte[] dest, int timeoutMillis) throws IOException {
            if (this.mEnableAsyncReads) {
                UsbRequest request = new UsbRequest();
                try {
                    request.initialize(this.mConnection, this.mReadEndpoint);
                    ByteBuffer buf = ByteBuffer.wrap(dest);
                    if (!request.queue(buf, dest.length)) {
                        throw new IOException("Error queueing request.");
                    }
                    UsbRequest response = this.mConnection.requestWait();
                    if (response == null) {
                        throw new IOException("Null response");
                    }
                    int nread = buf.position();
                    if (nread > 0) {
                        request.close();
                        return nread;
                    }
                    request.close();
                    return 0;
                } catch (Throwable th) {
                    request.close();
                    throw th;
                }
            }
            synchronized (this.mReadBufferLock) {
                int readAmt = Math.min(dest.length, this.mReadBuffer.length);
                int numBytesRead = this.mConnection.bulkTransfer(this.mReadEndpoint, this.mReadBuffer, readAmt, timeoutMillis);
                if (numBytesRead < 0) {
                    return timeoutMillis == Integer.MAX_VALUE ? -1 : 0;
                }
                System.arraycopy(this.mReadBuffer, 0, dest, 0, numBytesRead);
                return numBytesRead;
            }
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public int write(byte[] src, int timeoutMillis) throws IOException {
            int writeLength;
            byte[] writeBuffer;
            int amtWritten;
            int offset = 0;
            while (offset < src.length) {
                synchronized (this.mWriteBufferLock) {
                    writeLength = Math.min(src.length - offset, this.mWriteBuffer.length);
                    if (offset == 0) {
                        writeBuffer = src;
                    } else {
                        byte[] writeBuffer2 = this.mWriteBuffer;
                        System.arraycopy(src, offset, writeBuffer2, 0, writeLength);
                        writeBuffer = this.mWriteBuffer;
                    }
                    amtWritten = this.mConnection.bulkTransfer(this.mWriteEndpoint, writeBuffer, writeLength, timeoutMillis);
                }
                if (amtWritten > 0) {
                    Log.d(CdcAcmSerialDriver.this.TAG, "Wrote amt=" + amtWritten + " attempted=" + writeLength);
                    offset += amtWritten;
                } else {
                    throw new IOException("Error writing " + writeLength + " bytes at offset " + offset + " length=" + src.length);
                }
            }
            return offset;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) {
            byte stopBitsByte;
            byte parityBitesByte;
            switch (stopBits) {
                case 1:
                    stopBitsByte = 0;
                    break;
                case 2:
                    stopBitsByte = 2;
                    break;
                case 3:
                    stopBitsByte = 1;
                    break;
                default:
                    throw new IllegalArgumentException("Bad value for stopBits: " + stopBits);
            }
            switch (parity) {
                case 0:
                    parityBitesByte = 0;
                    break;
                case 1:
                    parityBitesByte = 1;
                    break;
                case 2:
                    parityBitesByte = 2;
                    break;
                case 3:
                    parityBitesByte = 3;
                    break;
                case 4:
                    parityBitesByte = 4;
                    break;
                default:
                    throw new IllegalArgumentException("Bad value for parity: " + parity);
            }
            byte[] msg = {(byte) (baudRate & 255), (byte) ((baudRate >> 8) & 255), (byte) ((baudRate >> 16) & 255), (byte) ((baudRate >> 24) & 255), stopBitsByte, parityBitesByte, (byte) dataBits};
            sendAcmControlMessage(32, 0, msg);
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getCD() throws IOException {
            return false;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getCTS() throws IOException {
            return false;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getDSR() throws IOException {
            return false;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getDTR() throws IOException {
            return this.mDtr;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void setDTR(boolean value) throws IOException {
            this.mDtr = value;
            setDtrRts();
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getRI() throws IOException {
            return false;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getRTS() throws IOException {
            return this.mRts;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void setRTS(boolean value) throws IOException {
            this.mRts = value;
            setDtrRts();
        }

        private void setDtrRts() {
            sendAcmControlMessage(34, (this.mRts ? 2 : 0) | (this.mDtr ? 1 : 0), null);
        }
    }

    public static Map<Integer, int[]> getSupportedDevices() {
        Map<Integer, int[]> supportedDevices = new LinkedHashMap<>();
        supportedDevices.put(9025, new int[]{1, 67, 16, 66, 59, 68, 63, 68, 32822, 32823});
        supportedDevices.put(5824, new int[]{1155});
        supportedDevices.put(1003, new int[]{8260});
        supportedDevices.put(7855, new int[]{4});
        return supportedDevices;
    }
}
