package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;

public abstract class BaseHandler extends Handler {
    protected final String LOG_TAG = getClass().getSimpleName();

    public void init() {
    }

    protected BaseHandler(Looper looper) {
        super(looper);
    }
}
