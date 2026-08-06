package com.cz.usbserial.driver;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class ProlificSerialDriver implements UsbSerialDriver {
    private final String TAG = ProlificSerialDriver.class.getSimpleName();
    private final UsbDevice mDevice;
    private final UsbSerialPort mPort;

    public ProlificSerialDriver(UsbDevice device) {
        this.mDevice = device;
        this.mPort = new ProlificSerialPort(this.mDevice, 0);
    }

    @Override // com.cz.usbserial.driver.UsbSerialDriver
    public List<UsbSerialPort> getPorts() {
        return Collections.singletonList(this.mPort);
    }

    @Override // com.cz.usbserial.driver.UsbSerialDriver
    public UsbDevice getDevice() {
        return this.mDevice;
    }

    class ProlificSerialPort extends CommonUsbSerialPort {
        private static final int CONTROL_DTR = 1;
        private static final int CONTROL_RTS = 2;
        private static final int DEVICE_TYPE_0 = 1;
        private static final int DEVICE_TYPE_1 = 2;
        private static final int DEVICE_TYPE_HX = 0;
        private static final int FLUSH_RX_REQUEST = 8;
        private static final int FLUSH_TX_REQUEST = 9;
        private static final int INTERRUPT_ENDPOINT = 129;
        private static final int PROLIFIC_CTRL_OUT_REQTYPE = 33;
        private static final int PROLIFIC_VENDOR_IN_REQTYPE = 192;
        private static final int PROLIFIC_VENDOR_OUT_REQTYPE = 64;
        private static final int PROLIFIC_VENDOR_READ_REQUEST = 1;
        private static final int PROLIFIC_VENDOR_WRITE_REQUEST = 1;
        private static final int READ_ENDPOINT = 131;
        private static final int SET_CONTROL_REQUEST = 34;
        private static final int SET_LINE_REQUEST = 32;
        private static final int STATUS_BUFFER_SIZE = 10;
        private static final int STATUS_BYTE_IDX = 8;
        private static final int STATUS_FLAG_CD = 1;
        private static final int STATUS_FLAG_CTS = 128;
        private static final int STATUS_FLAG_DSR = 2;
        private static final int STATUS_FLAG_RI = 8;
        private static final int USB_READ_TIMEOUT_MILLIS = 1000;
        private static final int USB_RECIP_INTERFACE = 1;
        private static final int USB_WRITE_TIMEOUT_MILLIS = 5000;
        private static final int WRITE_ENDPOINT = 2;
        private int mBaudRate;
        private int mControlLinesValue;
        private int mDataBits;
        private int mDeviceType;
        private UsbEndpoint mInterruptEndpoint;
        private int mParity;
        private UsbEndpoint mReadEndpoint;
        private IOException mReadStatusException;
        private volatile Thread mReadStatusThread;
        private final Object mReadStatusThreadLock;
        private int mStatus;
        private int mStopBits;
        boolean mStopReadStatusThread;
        private UsbEndpoint mWriteEndpoint;

        public ProlificSerialPort(UsbDevice device, int portNumber) {
            super(device, portNumber);
            this.mDeviceType = 0;
            this.mControlLinesValue = 0;
            this.mBaudRate = -1;
            this.mDataBits = -1;
            this.mStopBits = -1;
            this.mParity = -1;
            this.mStatus = 0;
            this.mReadStatusThread = null;
            this.mReadStatusThreadLock = new Object();
            this.mStopReadStatusThread = false;
            this.mReadStatusException = null;
        }

        @Override // com.cz.usbserial.driver.UsbSerialPort
        public UsbSerialDriver getDriver() {
            return ProlificSerialDriver.this;
        }

        private final byte[] inControlTransfer(int requestType, int request, int value, int index, int length) throws IOException {
            byte[] buffer = new byte[length];
            int result = this.mConnection.controlTransfer(requestType, request, value, index, buffer, length, 1000);
            if (result != length) {
                throw new IOException(String.format("ControlTransfer with value 0x%x failed: %d", Integer.valueOf(value), Integer.valueOf(result)));
            }
            return buffer;
        }

        private final void outControlTransfer(int requestType, int request, int value, int index, byte[] data) throws IOException {
            int length = data == null ? 0 : data.length;
            int result = this.mConnection.controlTransfer(requestType, request, value, index, data, length, 5000);
            if (result != length) {
                throw new IOException(String.format("ControlTransfer with value 0x%x failed: %d", Integer.valueOf(value), Integer.valueOf(result)));
            }
        }

        private final byte[] vendorIn(int value, int index, int length) throws IOException {
            return inControlTransfer(192, 1, value, index, length);
        }

        private final void vendorOut(int value, int index, byte[] data) throws IOException {
            outControlTransfer(64, 1, value, index, data);
        }

        private void resetDevice() throws IOException {
            purgeHwBuffers(true, true);
        }

        private final void ctrlOut(int request, int value, int index, byte[] data) throws IOException {
            outControlTransfer(33, request, value, index, data);
        }

        private void doBlackMagic() throws IOException {
            vendorIn(33924, 0, 1);
            vendorOut(1028, 0, null);
            vendorIn(33924, 0, 1);
            vendorIn(33667, 0, 1);
            vendorIn(33924, 0, 1);
            vendorOut(1028, 1, null);
            vendorIn(33924, 0, 1);
            vendorIn(33667, 0, 1);
            vendorOut(0, 1, null);
            vendorOut(1, 0, null);
            vendorOut(2, this.mDeviceType == 0 ? 68 : 36, null);
        }

        private void setControlLines(int newControlLinesValue) throws IOException {
            ctrlOut(34, newControlLinesValue, 0, null);
            this.mControlLinesValue = newControlLinesValue;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final void readStatusThreadFunction() {
            while (!this.mStopReadStatusThread) {
                try {
                    byte[] buffer = new byte[10];
                    int readBytesCount = this.mConnection.bulkTransfer(this.mInterruptEndpoint, buffer, 10, 500);
                    if (readBytesCount > 0) {
                        if (readBytesCount == 10) {
                            this.mStatus = buffer[8] & 255;
                        } else {
                            throw new IOException(String.format("Invalid CTS / DSR / CD / RI status buffer received, expected %d bytes, but received %d", 10, Integer.valueOf(readBytesCount)));
                        }
                    }
                } catch (IOException e) {
                    this.mReadStatusException = e;
                    return;
                }
            }
        }

        private final int getStatus() throws IOException {
            if (this.mReadStatusThread == null && this.mReadStatusException == null) {
                synchronized (this.mReadStatusThreadLock) {
                    if (this.mReadStatusThread == null) {
                        byte[] buffer = new byte[10];
                        int readBytes = this.mConnection.bulkTransfer(this.mInterruptEndpoint, buffer, 10, 100);
                        if (readBytes != 10) {
                            Log.w(ProlificSerialDriver.this.TAG, "Could not read initial CTS / DSR / CD / RI status");
                        } else {
                            this.mStatus = buffer[8] & 255;
                        }
                        this.mReadStatusThread = new Thread(new Runnable() { // from class: com.cz.usbserial.driver.ProlificSerialDriver.ProlificSerialPort.1
                            @Override // java.lang.Runnable
                            public void run() {
                                ProlificSerialPort.this.readStatusThreadFunction();
                            }
                        });
                        this.mReadStatusThread.setDaemon(true);
                        this.mReadStatusThread.start();
                    }
                }
            }
            IOException readStatusException = this.mReadStatusException;
            if (this.mReadStatusException != null) {
                this.mReadStatusException = null;
                throw readStatusException;
            }
            return this.mStatus;
        }

        private final boolean testStatusFlag(int flag) throws IOException {
            return (getStatus() & flag) == flag;
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public void open(UsbDeviceConnection connection) throws IOException {
            if (this.mConnection != null) {
                throw new IOException("Already open");
            }
            UsbInterface usbInterface = this.mDevice.getInterface(0);
            if (!connection.claimInterface(usbInterface, true)) {
                throw new IOException("Error claiming Prolific interface 0");
            }
            this.mConnection = connection;
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                try {
                    UsbEndpoint currentEndpoint = usbInterface.getEndpoint(i);
                    int address = currentEndpoint.getAddress();
                    if (address == 2) {
                        this.mWriteEndpoint = currentEndpoint;
                    } else if (address == INTERRUPT_ENDPOINT) {
                        this.mInterruptEndpoint = currentEndpoint;
                    } else if (address == READ_ENDPOINT) {
                        this.mReadEndpoint = currentEndpoint;
                    }
                } catch (Throwable th) {
                    if (0 == 0) {
                        this.mConnection = null;
                        connection.releaseInterface(usbInterface);
                    }
                    throw th;
                }
            }
            if (this.mDevice.getDeviceClass() == 2) {
                this.mDeviceType = 1;
            } else {
                try {
                    try {
                        Method getRawDescriptorsMethod = this.mConnection.getClass().getMethod("getRawDescriptors", new Class[0]);
                        byte[] rawDescriptors = (byte[]) getRawDescriptorsMethod.invoke(this.mConnection, new Object[0]);
                        byte maxPacketSize0 = rawDescriptors[7];
                        if (maxPacketSize0 == 64) {
                            this.mDeviceType = 0;
                        } else if (this.mDevice.getDeviceClass() != 0 && this.mDevice.getDeviceClass() != 255) {
                            Log.w(ProlificSerialDriver.this.TAG, "Could not detect PL2303 subtype, Assuming that it is a HX device");
                            this.mDeviceType = 0;
                        } else {
                            this.mDeviceType = 2;
                        }
                    } catch (NoSuchMethodException e) {
                        Log.w(ProlificSerialDriver.this.TAG, "Method UsbDeviceConnection.getRawDescriptors, required for PL2303 subtype detection, not available! Assuming that it is a HX device");
                        this.mDeviceType = 0;
                    }
                } catch (Exception e2) {
                    Log.e(ProlificSerialDriver.this.TAG, "An unexpected exception occured while trying to detect PL2303 subtype", e2);
                }
            }
            setControlLines(this.mControlLinesValue);
            resetDevice();
            doBlackMagic();
            if (1 == 0) {
                this.mConnection = null;
                connection.releaseInterface(usbInterface);
            }
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public void close() throws IOException {
            if (this.mConnection == null) {
                throw new IOException("Already closed");
            }
            try {
                this.mStopReadStatusThread = true;
                synchronized (this.mReadStatusThreadLock) {
                    if (this.mReadStatusThread != null) {
                        try {
                            this.mReadStatusThread.join();
                        } catch (Exception e) {
                            Log.w(ProlificSerialDriver.this.TAG, "An error occured while waiting for status read thread", e);
                        }
                    }
                }
                resetDevice();
                try {
                    this.mConnection.releaseInterface(this.mDevice.getInterface(0));
                } finally {
                    this.mConnection = null;
                }
            } catch (Throwable th) {
                try {
                    this.mConnection.releaseInterface(this.mDevice.getInterface(0));
                    throw th;
                } finally {
                    this.mConnection = null;
                }
            }
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
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

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
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
                if (amtWritten <= 0) {
                    throw new IOException("Error writing " + writeLength + " bytes at offset " + offset + " length=" + src.length);
                }
                offset += amtWritten;
            }
            return offset;
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException {
            if (this.mBaudRate == baudRate && this.mDataBits == dataBits && this.mStopBits == stopBits && this.mParity == parity) {
                return;
            }
            byte[] lineRequestData = new byte[7];
            lineRequestData[0] = (byte) (baudRate & 255);
            lineRequestData[1] = (byte) ((baudRate >> 8) & 255);
            lineRequestData[2] = (byte) ((baudRate >> 16) & 255);
            lineRequestData[3] = (byte) ((baudRate >> 24) & 255);
            switch (stopBits) {
                case 1:
                    lineRequestData[4] = 0;
                    break;
                case 2:
                    lineRequestData[4] = 2;
                    break;
                case 3:
                    lineRequestData[4] = 1;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown stopBits value: " + stopBits);
            }
            switch (parity) {
                case 0:
                    lineRequestData[5] = 0;
                    break;
                case 1:
                    lineRequestData[5] = 1;
                    break;
                case 2:
                    lineRequestData[5] = 2;
                    break;
                case 3:
                    lineRequestData[5] = 3;
                    break;
                case 4:
                    lineRequestData[5] = 4;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown parity value: " + parity);
            }
            lineRequestData[6] = (byte) dataBits;
            ctrlOut(32, 0, 0, lineRequestData);
            resetDevice();
            this.mBaudRate = baudRate;
            this.mDataBits = dataBits;
            this.mStopBits = stopBits;
            this.mParity = parity;
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public boolean getCD() throws IOException {
            return testStatusFlag(1);
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public boolean getCTS() throws IOException {
            return testStatusFlag(128);
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public boolean getDSR() throws IOException {
            return testStatusFlag(2);
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public boolean getDTR() throws IOException {
            return (this.mControlLinesValue & 1) == 1;
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public void setDTR(boolean value) throws IOException {
            int newControlLinesValue;
            if (value) {
                newControlLinesValue = this.mControlLinesValue | 1;
            } else {
                int newControlLinesValue2 = this.mControlLinesValue;
                newControlLinesValue = newControlLinesValue2 & (-2);
            }
            setControlLines(newControlLinesValue);
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public boolean getRI() throws IOException {
            return testStatusFlag(8);
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public boolean getRTS() throws IOException {
            return (this.mControlLinesValue & 2) == 2;
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public void setRTS(boolean value) throws IOException {
            int newControlLinesValue;
            if (value) {
                newControlLinesValue = this.mControlLinesValue | 2;
            } else {
                int newControlLinesValue2 = this.mControlLinesValue;
                newControlLinesValue = newControlLinesValue2 & (-3);
            }
            setControlLines(newControlLinesValue);
        }

        @Override // com.cz.usbserial.driver.CommonUsbSerialPort, com.cz.usbserial.driver.UsbSerialPort
        public boolean purgeHwBuffers(boolean purgeReadBuffers, boolean purgeWriteBuffers) throws IOException {
            if (purgeReadBuffers) {
                vendorOut(8, 0, null);
            }
            if (purgeWriteBuffers) {
                vendorOut(9, 0, null);
            }
            return purgeReadBuffers || purgeWriteBuffers;
        }
    }

    public static Map<Integer, int[]> getSupportedDevices() {
        Map<Integer, int[]> supportedDevices = new LinkedHashMap<>();
        supportedDevices.put(1659, new int[]{8963});
        return supportedDevices;
    }
}
