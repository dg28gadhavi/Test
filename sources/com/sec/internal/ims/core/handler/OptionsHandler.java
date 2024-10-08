package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;
import java.util.List;

public abstract class OptionsHandler extends BaseHandler implements IOptionsServiceInterface {
    public void registerForCmcOptionsEvent(Handler handler, int i, Object obj) {
    }

    public void registerForOptionsEvent(Handler handler, int i, Object obj) {
    }

    public void registerForP2pOptionsEvent(Handler handler, int i, Object obj) {
    }

    public void requestCapabilityExchange(ImsUri imsUri, long j, int i, String str, List<String> list) {
    }

    public void requestSendCmcCheckMsg(int i, int i2, String str) {
    }

    public void setOwnCapabilities(long j, int i) {
    }

    protected OptionsHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + message.what);
    }
}
