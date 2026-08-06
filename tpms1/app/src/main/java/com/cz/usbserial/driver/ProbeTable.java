package com.cz.usbserial.driver;

import android.util.Pair;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class ProbeTable {
    private final Map<Pair<Integer, Integer>, Class<? extends UsbSerialDriver>> mProbeTable = new LinkedHashMap();

    public ProbeTable addProduct(int vendorId, int productId, Class<? extends UsbSerialDriver> driverClass) {
        this.mProbeTable.put(Pair.create(Integer.valueOf(vendorId), Integer.valueOf(productId)), driverClass);
        return this;
    }

    ProbeTable addDriver(Class<? extends UsbSerialDriver> driverClass) {
        try {
            Method method = driverClass.getMethod("getSupportedDevices", new Class[0]);
            try {
                Map<Integer, int[]> devices = (Map) method.invoke(null, new Object[0]);
                for (Map.Entry<Integer, int[]> entry : devices.entrySet()) {
                    int vendorId = entry.getKey().intValue();
                    for (int productId : entry.getValue()) {
                        addProduct(vendorId, productId, driverClass);
                    }
                }
                return this;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e2) {
                throw new RuntimeException(e2);
            } catch (InvocationTargetException e3) {
                throw new RuntimeException(e3);
            }
        } catch (NoSuchMethodException e4) {
            throw new RuntimeException(e4);
        } catch (SecurityException e5) {
            throw new RuntimeException(e5);
        }
    }

    public Class<? extends UsbSerialDriver> findDriver(int vendorId, int productId) {
        Pair<Integer, Integer> pair = Pair.create(Integer.valueOf(vendorId), Integer.valueOf(productId));
        return this.mProbeTable.get(pair);
    }
}
