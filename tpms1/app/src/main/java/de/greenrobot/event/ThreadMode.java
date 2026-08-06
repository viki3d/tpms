package de.greenrobot.event;

/* JADX INFO: loaded from: classes.dex */
public enum ThreadMode {
    PostThread,
    MainThread,
    BackgroundThread,
    Async;

    /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
    public static ThreadMode[] valuesCustom() {
        ThreadMode[] threadModeArrValuesCustom = values();
        int length = threadModeArrValuesCustom.length;
        ThreadMode[] threadModeArr = new ThreadMode[length];
        System.arraycopy(threadModeArrValuesCustom, 0, threadModeArr, 0, length);
        return threadModeArr;
    }
}
