package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.interfaces.ims.core.handler.IMiscHandler;

public class MiscHandler extends BaseHandler implements IMiscHandler {
    public void registerForEcholocateEvent(Handler handler, int i, Object obj) {
    }

    public void registerForXqMtripEvent(Handler handler, int i, Object obj) {
    }

    public void unregisterForEcholocateEvent(Handler handler) {
    }

    public void unregisterForXqMtripEvent(Handler handler) {
    }

    protected MiscHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + message.what);
    }
}
