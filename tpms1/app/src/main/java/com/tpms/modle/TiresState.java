package com.tpms.modle;

import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class TiresState {
    public int AirPressure;
    public int Temperature;
    public String TiresID = "";
    public boolean Leakage = false;
    public boolean LowPower = false;
    public boolean NoSignal = false;
    public String error = "";
    public Map<String, AlarmCntrol> mAlarmCntrols = new HashMap();
}
