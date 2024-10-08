package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface;

public abstract class CmcHandler extends BaseHandler implements ICmcMediaServiceInterface {
    protected final RegistrantList mCmcMediaEventRegistrants = new RegistrantList();

    public boolean startCmcRecord(int i, int i2, int i3, int i4, long j, int i5, String str, int i6, int i7, int i8, int i9, int i10, long j2, String str2) {
        return false;
    }

    public boolean stopCmcRecord(int i, int i2) {
        return false;
    }

    protected CmcHandler(Looper looper) {
        super(looper);
    }

    public void registerForCmcMediaEvent(Handler handler, int i, Object obj) {
        this.mCmcMediaEventRegistrants.addUnique(handler, i, obj);
    }

    public void unregisterForCmcMediaEvent(Handler handler) {
        this.mCmcMediaEventRegistrants.remove(handler);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + message.what);
    }
}
