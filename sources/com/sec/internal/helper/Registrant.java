package com.sec.internal.helper;

import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

public class Registrant {
    WeakReference refH;
    Object userObj;
    int what;

    public Registrant(Handler handler, int i, Object obj) {
        this.refH = new WeakReference(handler);
        this.what = i;
        this.userObj = obj;
    }

    public void clear() {
        this.refH = null;
        this.userObj = null;
    }

    public void notifyResult(Object obj) {
        internalNotifyRegistrant(obj, (Throwable) null);
    }

    /* access modifiers changed from: package-private */
    public void internalNotifyRegistrant(Object obj, Throwable th) {
        Handler handler = getHandler();
        if (handler == null) {
            clear();
            return;
        }
        Message obtain = Message.obtain();
        obtain.what = this.what;
        obtain.obj = new AsyncResult(this.userObj, obj, th);
        handler.sendMessage(obtain);
    }

    public Handler getHandler() {
        WeakReference weakReference = this.refH;
        if (weakReference == null) {
            return null;
        }
        return (Handler) weakReference.get();
    }
}
