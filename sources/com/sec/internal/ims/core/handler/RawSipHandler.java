package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.sec.internal.interfaces.ims.core.handler.ISipDialogInterface;

public abstract class RawSipHandler extends BaseHandler implements ISipDialogInterface {
    public void openSipDialog(boolean z) {
    }

    public void registerForIncomingMessages(Handler handler, int i, Object obj) {
    }

    public void registerForOutgoingMessages(Handler handler, int i, Object obj) {
    }

    public boolean sendSip(int i, String str, Message message) {
        return false;
    }

    public void unregisterForEvent(Handler handler) {
    }

    protected RawSipHandler(Looper looper) {
        super(looper);
    }
}
