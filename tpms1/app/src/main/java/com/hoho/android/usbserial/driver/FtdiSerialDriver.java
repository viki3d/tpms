package com.hoho.android.usbserial.driver;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import com.tpms.utils.Log;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class FtdiSerialDriver implements UsbSerialDriver {
    private final UsbDevice mDevice;
    private final UsbSerialPort mPort;

    private enum DeviceType {
        TYPE_BM,
        TYPE_AM,
        TYPE_2232C,
        TYPE_R,
        TYPE_2232H,
        TYPE_4232H
    }

    public FtdiSerialDriver(UsbDevice device) {
        this.mDevice = device;
        this.mPort = new FtdiSerialPort(this.mDevice, 0);
    }

    @Override // com.hoho.android.usbserial.driver.UsbSerialDriver
    public UsbDevice getDevice() {
        return this.mDevice;
    }

    @Override // com.hoho.android.usbserial.driver.UsbSerialDriver
    public List<UsbSerialPort> getPorts() {
        return Collections.singletonList(this.mPort);
    }

    private class FtdiSerialPort extends CommonUsbSerialPort {
        private static final boolean ENABLE_ASYNC_READS = false;
        public static final int FTDI_DEVICE_IN_REQTYPE = 192;
        public static final int FTDI_DEVICE_OUT_REQTYPE = 64;
        private static final int MODEM_STATUS_HEADER_LENGTH = 2;
        private static final int SIO_MODEM_CTRL_REQUEST = 1;
        private static final int SIO_RESET_PURGE_RX = 1;
        private static final int SIO_RESET_PURGE_TX = 2;
        private static final int SIO_RESET_REQUEST = 0;
        private static final int SIO_RESET_SIO = 0;
        private static final int SIO_SET_BAUD_RATE_REQUEST = 3;
        private static final int SIO_SET_DATA_REQUEST = 4;
        private static final int SIO_SET_FLOW_CTRL_REQUEST = 2;
        public static final int USB_ENDPOINT_IN = 128;
        public static final int USB_ENDPOINT_OUT = 0;
        public static final int USB_READ_TIMEOUT_MILLIS = 5000;
        public static final int USB_RECIP_DEVICE = 0;
        public static final int USB_RECIP_ENDPOINT = 2;
        public static final int USB_RECIP_INTERFACE = 1;
        public static final int USB_RECIP_OTHER = 3;
        public static final int USB_TYPE_CLASS = 0;
        public static final int USB_TYPE_RESERVED = 0;
        public static final int USB_TYPE_STANDARD = 0;
        public static final int USB_TYPE_VENDOR = 0;
        public static final int USB_WRITE_TIMEOUT_MILLIS = 5000;
        private final String TAG;
        private int mInterface;
        private int mMaxPacketSize;
        private DeviceType mType;

        public FtdiSerialPort(UsbDevice device, int portNumber) {
            super(device, portNumber);
            this.TAG = FtdiSerialDriver.class.getSimpleName();
            this.mInterface = 0;
            this.mMaxPacketSize = 64;
        }

        @Override // com.hoho.android.usbserial.driver.UsbSerialPort
        public UsbSerialDriver getDriver() {
            return FtdiSerialDriver.this;
        }

        private final int filterStatusBytes(byte[] src, byte[] dest, int totalBytesRead, int maxPacketSize) {
            int count = 0;
            int packetsCount = (totalBytesRead / maxPacketSize) + (totalBytesRead % maxPacketSize == 0 ? 0 : 1);
            while (true) {
                int packetIdx = count;
                if (packetIdx >= packetsCount) {
                    return totalBytesRead - (packetsCount * 2);
                }
                int count2 = packetIdx == packetsCount + (-1) ? (totalBytesRead % maxPacketSize) - 2 : maxPacketSize - 2;
                if (count2 > 0) {
                    System.arraycopy(src, (packetIdx * maxPacketSize) + 2, dest, (maxPacketSize - 2) * packetIdx, count2);
                }
                count = packetIdx + 1;
            }
        }

        public void reset() throws IOException {
            int result = this.mConnection.controlTransfer(64, 0, 0, 0, null, 0, 5000);
            if (result != 0) {
                throw new IOException("Reset failed: result=" + result);
            }
            this.mType = DeviceType.TYPE_R;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void open(UsbDeviceConnection connection) throws IOException {
            if (this.mConnection != null) {
                throw new IOException("Already open");
            }
            this.mConnection = connection;
            for (int i = 0; i < this.mDevice.getInterfaceCount(); i++) {
                try {
                    if (connection.claimInterface(this.mDevice.getInterface(i), true)) {
                        Log.d(this.TAG, "claimInterface " + i + " SUCCESS");
                    } else {
                        throw new IOException("Error claiming interface " + i);
                    }
                } catch (Throwable th) {
                    if (0 == 0) {
                        close();
                        this.mConnection = null;
                    }
                    throw th;
                }
            }
            reset();
            if (1 == 0) {
                close();
                this.mConnection = null;
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
            int iFilterStatusBytes;
            UsbEndpoint endpoint = this.mDevice.getInterface(0).getEndpoint(0);
            synchronized (this.mReadBufferLock) {
                int readAmt = Math.min(dest.length, this.mReadBuffer.length);
                int totalBytesRead = this.mConnection.bulkTransfer(endpoint, this.mReadBuffer, readAmt, timeoutMillis);
                if (totalBytesRead < 2) {
                    throw new IOException("Expected at least 2 bytes");
                }
                iFilterStatusBytes = filterStatusBytes(this.mReadBuffer, dest, totalBytesRead, endpoint.getMaxPacketSize());
            }
            return iFilterStatusBytes;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public int write(byte[] src, int timeoutMillis) throws IOException {
            int writeLength;
            byte[] writeBuffer;
            int amtWritten;
            UsbEndpoint endpoint = this.mDevice.getInterface(0).getEndpoint(1);
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
                    amtWritten = this.mConnection.bulkTransfer(endpoint, writeBuffer, writeLength, timeoutMillis);
                }
                if (amtWritten <= 0) {
                    throw new IOException("Error writing " + writeLength + " bytes at offset " + offset + " length=" + src.length);
                }
                Log.d(this.TAG, "Wrote amtWritten=" + amtWritten + " attempted=" + writeLength);
                offset += amtWritten;
            }
            return offset;
        }

        private int setBaudRate(int baudRate) throws IOException {
            long[] vals = convertBaudrate(baudRate);
            long actualBaudrate = vals[0];
            long index = vals[1];
            long value = vals[2];
            int result = this.mConnection.controlTransfer(64, 3, (int) value, (int) index, null, 0, 5000);
            if (result != 0) {
                throw new IOException("Setting baudrate failed: result=" + result);
            }
            return (int) actualBaudrate;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException {
            int config;
            int config2;
            setBaudRate(baudRate);
            switch (parity) {
                case 0:
                    config = dataBits | 0;
                    break;
                case 1:
                    config = dataBits | 256;
                    break;
                case 2:
                    config = dataBits | 512;
                    break;
                case 3:
                    config = dataBits | 768;
                    break;
                case 4:
                    config = dataBits | 1024;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown parity value: " + parity);
            }
            switch (stopBits) {
                case 1:
                    config2 = config | 0;
                    break;
                case 2:
                    config2 = config | 4096;
                    break;
                case 3:
                    config2 = config | 2048;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown stopBits value: " + stopBits);
            }
            int result = this.mConnection.controlTransfer(64, 4, config2, 0, null, 0, 5000);
            if (result != 0) {
                throw new IOException("Setting parameters failed: result=" + result);
            }
        }

        private long[] convertBaudrate(int baudrate) {
            long index;
            int baudDiff;
            int divisor = 24000000 / baudrate;
            int[] fracCode = {0, 3, 2, 4, 1, 5, 6, 7};
            int bestBaudDiff = 0;
            int bestBaudDiff2 = 0;
            int bestBaud = 0;
            for (int bestDivisor = 0; bestDivisor < 2; bestDivisor++) {
                int tryDivisor = divisor + bestDivisor;
                if (tryDivisor <= 8) {
                    tryDivisor = 8;
                } else if (this.mType != DeviceType.TYPE_AM && tryDivisor < 12) {
                    tryDivisor = 12;
                } else if (divisor < 16) {
                    tryDivisor = 16;
                } else if (this.mType != DeviceType.TYPE_AM && tryDivisor > 131071) {
                    tryDivisor = 131071;
                }
                int baudEstimate = ((tryDivisor / 2) + 24000000) / tryDivisor;
                if (baudEstimate < baudrate) {
                    baudDiff = baudrate - baudEstimate;
                } else {
                    baudDiff = baudEstimate - baudrate;
                }
                if (bestDivisor == 0 || baudDiff < bestBaudDiff) {
                    bestBaud = tryDivisor;
                    bestBaudDiff2 = baudEstimate;
                    bestBaudDiff = baudDiff;
                    if (baudDiff == 0) {
                        break;
                    }
                }
            }
            long encodedDivisor = (bestBaud >> 3) | (fracCode[bestBaud & 7] << 14);
            if (encodedDivisor == 1) {
                encodedDivisor = 0;
            } else if (encodedDivisor == 16385) {
                encodedDivisor = 1;
            }
            long value = encodedDivisor & 65535;
            if (this.mType == DeviceType.TYPE_2232C || this.mType == DeviceType.TYPE_2232H || this.mType == DeviceType.TYPE_4232H) {
                long index2 = (encodedDivisor >> 8) & 65535;
                index = (index2 & 65280) | 0;
            } else {
                index = (encodedDivisor >> 16) & 65535;
            }
            return new long[]{bestBaudDiff2, index, value};
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
            return false;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void setDTR(boolean value) throws IOException {
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getRI() throws IOException {
            return false;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean getRTS() throws IOException {
            return false;
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public void setRTS(boolean value) throws IOException {
        }

        @Override // com.hoho.android.usbserial.driver.CommonUsbSerialPort, com.hoho.android.usbserial.driver.UsbSerialPort
        public boolean purgeHwBuffers(boolean purgeReadBuffers, boolean purgeWriteBuffers) throws IOException {
            int result;
            int result2;
            if (purgeReadBuffers && (result2 = this.mConnection.controlTransfer(64, 0, 1, 0, null, 0, 5000)) != 0) {
                throw new IOException("Flushing RX failed: result=" + result2);
            }
            if (purgeWriteBuffers && (result = this.mConnection.controlTransfer(64, 0, 2, 0, null, 0, 5000)) != 0) {
                throw new IOException("Flushing RX failed: result=" + result);
            }
            return true;
        }
    }

    public static Map<Integer, int[]> getSupportedDevices() {
        Map<Integer, int[]> supportedDevices = new LinkedHashMap<>();
        supportedDevices.put(1027, new int[]{24577, 24597});
        return supportedDevices;
    }
}
