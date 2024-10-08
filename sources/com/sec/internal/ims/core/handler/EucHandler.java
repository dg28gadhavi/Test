package com.sec.internal.ims.core.handler;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucServiceInterface;

public abstract class EucHandler extends BaseHandler implements IEucServiceInterface {
    protected EucHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + message.what);
    }
}
