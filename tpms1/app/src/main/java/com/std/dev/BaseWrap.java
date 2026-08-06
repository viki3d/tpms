package com.std.dev;

//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import com.tpms.utils.Log;
import java.lang.reflect.Method;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class BaseWrap {
    private static final String TAG = "BaseWrap";

    protected static <T> T runRelMethod(String str, Object obj, Class[] clsArr, Object... objArr) {
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        try {
            Class<?> cls = Class.forName(str);
            Log.d(TAG, "class name is : " + cls.getName());
            if (cls != null) {
                new HashMap();
                Method method = cls.getMethod(methodName, clsArr);
                if (method != null) {
                    return (T) method.invoke(obj, objArr);
                }
            }
        } catch (Exception e) {
 //           ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;

        }
        return null;
    }
}
