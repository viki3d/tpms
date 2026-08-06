package com.std.dev;

//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.FileDescriptor;

/* JADX INFO: loaded from: classes.dex */
public class OsWrap extends BaseWrap {
    private static String mPackName = "android.system.Os";

    public static void close(FileDescriptor fd) {
        try {
            Class[] paramsClass = {FileDescriptor.class};
            runRelMethod(mPackName, null, paramsClass, fd);
        } catch (Exception e) {
//            ThrowableExtension.printStackTrace(e);
            e.printStackTrace();;

        }
    }
}
