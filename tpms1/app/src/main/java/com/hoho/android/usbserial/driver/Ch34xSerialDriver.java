package com.hoho.android.usbserial.driver;

import com.tpms.TinkerReport;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
//import com.tencent.bugly.beta.tinker.TinkerReport;
import com.tpms.utils.Log;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class Ch34xSerialDriver implements UsbSerialDriver {
    private static final String TAG = Ch34xSerialDriver.class.getSimpleName();
    private final UsbDevice mDevice;
    private final UsbSerialPort mPort;

    public Ch34xSerialDriver(UsbDevice device) {
        this.mDevice = device;
        this.mPort = new Ch340SerialPort(this.mDevice, 0);
    }

    @Override // com.hoho.android.usbserial.driver.UsbSerialDriver
    public UsbDevice getDevice() {
        return this.mDevice;
    }

    @Override // com.hoho.android.usbserial.driver.UsbSerialDriver
    public List<UsbSerialPort> getPorts() {
        return Collections.singletonList(this.mPort);
    }

    public class Ch340SerialPort extends CommonUsbSerialPort {
        private static final int USB_TIMEOUT_MILLIS = 5000;
        private final int DEFAULT_BAUD_RATE;
        private boolean dtr;
        private UsbEndpoint mReadEndpoint;
        private UsbEndpoint mWriteEndpoint;
        private boolean rts;

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public /* bridge */ /* synthetic */ int getPortNumber() {
            return super.getPortNumber();
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public /* bridge */ /* synthetic */ String getSerial() {
            return super.getSerial();
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort
        public /* bridge */ /* synthetic */ String toString() {
            return super.toString();
        }

        public Ch340SerialPort(UsbDevice device, int portNumber) {
            super(device, portNumber);
            this.DEFAULT_BAUD_RATE = 9600;
            this.dtr = false;
            this.rts = false;
        }

        @Override // com.hoho.android.usbserial.driver.UsbSerialPort
        public UsbSerialDriver getDriver() {
            return Ch34xSerialDriver.this;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void open(UsbDeviceConnection connection) throws IOException {
            if (this.mConnection != null) {
                throw new IOException("Already opened.");
            }
            this.mConnection = connection;
            for (int i = 0; i < this.mDevice.getInterfaceCount(); i++) {
                try {
                    UsbInterface usbIface = this.mDevice.getInterface(i);
                    if (this.mConnection.claimInterface(usbIface, true)) {
                        Log.d(Ch34xSerialDriver.TAG, "claimInterface " + i + " SUCCESS");
                    } else {
                        Log.d(Ch34xSerialDriver.TAG, "claimInterface " + i + " FAIL");
                    }
                } catch (Throwable th) {
                    if (0 == 0) {
                        try {
                            close();
                        } catch (IOException e) {
                        }
                    }
                    throw th;
                }
            }
            UsbInterface dataIface = this.mDevice.getInterface(this.mDevice.getInterfaceCount() - 1);
            for (int i2 = 0; i2 < dataIface.getEndpointCount(); i2++) {
                UsbEndpoint ep = dataIface.getEndpoint(i2);
                if (ep.getType() == 2) {
                    if (ep.getDirection() == 128) {
                        this.mReadEndpoint = ep;
                    } else {
                        this.mWriteEndpoint = ep;
                    }
                }
            }
            initialize();
            setBaudRate(9600);
            if (1 == 0) {
                try {
                    close();
                } catch (IOException e2) {
                }
            }
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void close() throws IOException {
            if (this.mConnection == null) {
                throw new IOException("Already closed");
            }
            try {
                this.mConnection.close();
            } finally {
                this.mConnection = null;
            }
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public int read(byte[] dest, int timeoutMillis) throws IOException {
            synchronized (this.mReadBufferLock) {
                int readAmt = Math.min(dest.length, this.mReadBuffer.length);
                int numBytesRead = this.mConnection.bulkTransfer(this.mReadEndpoint, this.mReadBuffer, readAmt, timeoutMillis);
                if (numBytesRead < 0) {
                    return 0;
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
                    Log.d(Ch34xSerialDriver.TAG, "Wrote amt=" + amtWritten + " attempted=" + writeLength);
                    offset += amtWritten;
                } else {
                    throw new IOException("Error writing " + writeLength + " bytes at offset " + offset + " length=" + src.length);
                }
            }
            return offset;
        }

        private int controlOut(int request, int value, int index) {
            return this.mConnection.controlTransfer(65, request, value, index, null, 0, 5000);
        }

        private int controlIn(int request, int value, int index, byte[] buffer) {
            return this.mConnection.controlTransfer(192, request, value, index, buffer, buffer.length, 5000);
        }

        private void checkState(String msg, int request, int value, int[] expected) throws IOException {
            int current;
            byte[] buffer = new byte[expected.length];
            int ret = controlIn(request, value, 0, buffer);
            if (ret < 0) {
                throw new IOException("Faild send cmd [" + msg + "]");
            }
            if (ret != expected.length) {
                throw new IOException("Expected " + expected.length + " bytes, but get " + ret + " [" + msg + "]");
            }
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != -1 && expected[i] != (current = buffer[i] & 255)) {
                    throw new IOException("Expected 0x" + Integer.toHexString(expected[i]) + " bytes, but get 0x" + Integer.toHexString(current) + " [" + msg + "]");
                }
            }
        }

        private void writeHandshakeByte() throws IOException {
            if (controlOut(164, ((this.dtr ? 32 : 0) | (this.rts ? 64 : 0)) ^ (-1), 0) < 0) {
                throw new IOException("Faild to set handshake byte");
            }
        }

        private void initialize() throws IOException {
            checkState("init #1", 95, 0, new int[]{-1, 0});
            if (controlOut(161, 0, 0) < 0) {
                throw new IOException("init failed! #2");
            }
            setBaudRate(9600);
            checkState("init #4", 149, 9496, new int[]{-1, 0});
            if (controlOut(TinkerReport.KEY_APPLIED_PACKAGE_CHECK_PATCH_TINKER_ID_NOT_FOUND, 9496, 80) < 0) {
                throw new IOException("init failed! #5");
            }
            checkState("init #6", 149, 1798, new int[]{255, 238});
            if (controlOut(161, 20511, 55562) < 0) {
                throw new IOException("init failed! #7");
            }
            setBaudRate(9600);
            writeHandshakeByte();
            checkState("init #10", 149, 1798, new int[]{-1, 238});
        }

        private void setBaudRate(int baudRate) throws IOException {
            int[] baud = {2400, 55553, 56, 4800, 25602, 31, 9600, 45570, 19, 19200, 55554, 13, 38400, 25603, 10, 115200, 52227, 8};
            for (int i = 0; i < baud.length / 3; i++) {
                if (baud[i * 3] == baudRate) {
                    int ret = controlOut(TinkerReport.KEY_APPLIED_PACKAGE_CHECK_PATCH_TINKER_ID_NOT_FOUND, 4882, baud[(i * 3) + 1]);
                    if (ret < 0) {
                        throw new IOException("Error setting baud rate. #1");
                    }
                    int ret2 = controlOut(TinkerReport.KEY_APPLIED_PACKAGE_CHECK_PATCH_TINKER_ID_NOT_FOUND, 3884, baud[(i * 3) + 2]);
                    if (ret2 < 0) {
                        throw new IOException("Error setting baud rate. #1");
                    }
                    return;
                }
            }
            throw new IOException("Baud rate " + baudRate + " currently not supported");
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException {
            setBaudRate(baudRate);
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
            return this.dtr;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void setDTR(boolean value) throws IOException {
            this.dtr = value;
            writeHandshakeByte();
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getRI() throws IOException {
            return false;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getRTS() throws IOException {
            return this.rts;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void setRTS(boolean value) throws IOException {
            this.rts = value;
            writeHandshakeByte();
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean purgeHwBuffers(boolean purgeReadBuffers, boolean purgeWriteBuffers) throws IOException {
            return true;
        }
    }

    public static Map<Integer, int[]> getSupportedDevices() {
        Map<Integer, int[]> supportedDevices = new LinkedHashMap<>();
        supportedDevices.put(6790, new int[]{29987});
        return supportedDevices;
    }
}
