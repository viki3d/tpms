package de.greenrobot.event.util;

/* JADX INFO: loaded from: classes.dex */
public class ThrowableFailureEvent implements HasExecutionScope {
    private Object executionContext;
    protected final boolean suppressErrorUi;
    protected final Throwable throwable;

    public ThrowableFailureEvent(Throwable throwable) {
        this.throwable = throwable;
        this.suppressErrorUi = false;
    }

    public ThrowableFailureEvent(Throwable throwable, boolean suppressErrorUi) {
        this.throwable = throwable;
        this.suppressErrorUi = suppressErrorUi;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public boolean isSuppressErrorUi() {
        return this.suppressErrorUi;
    }

    @Override // de.greenrobot.event.util.HasExecutionScope
    public Object getExecutionScope() {
        return this.executionContext;
    }

    @Override // de.greenrobot.event.util.HasExecutionScope
    public void setExecutionScope(Object executionContext) {
        this.executionContext = executionContext;
    }
}
