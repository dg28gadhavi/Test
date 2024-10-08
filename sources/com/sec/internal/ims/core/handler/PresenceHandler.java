package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.presence.IPresenceStackInterface;
import java.util.List;

public abstract class PresenceHandler extends BaseHandler implements IPresenceStackInterface {
    public void publish(PresenceInfo presenceInfo, Message message, int i) {
    }

    public void registerForPresenceInfo(Handler handler, int i, Object obj) {
    }

    public void registerForPresenceNotifyInfo(Handler handler, int i, Object obj) {
    }

    public void registerForPresenceNotifyStatus(Handler handler, int i, Object obj) {
    }

    public void registerForPublishFailure(Handler handler, int i, Object obj) {
    }

    public void registerForWatcherInfo(Handler handler, int i, Object obj) {
    }

    public void subscribe(ImsUri imsUri, boolean z, Message message, String str, int i) {
    }

    public void subscribeList(List<ImsUri> list, boolean z, Message message, String str, boolean z2, int i, int i2) {
    }

    public void unpublish(int i) {
    }

    protected PresenceHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + message.what);
    }
}
