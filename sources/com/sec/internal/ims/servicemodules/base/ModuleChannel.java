package com.sec.internal.ims.servicemodules.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.sec.internal.ims.registry.ImsRegistry;

public class ModuleChannel {
    public static final String CAPDISCOVERY = "CapabilityDiscoveryModule";
    public static final int EVT_CAPDISCOVERY_DISABLE_FEATURE = 8002;
    public static final int EVT_CAPDISCOVERY_ENABLE_FEATURE = 8001;
    public static final int EVT_MODULE_CHANNEL_BASE = 8000;
    public static final int EVT_MODULE_CHANNEL_RESPONSE = 8999;
    protected Handler mDst;
    protected Handler mSrc;

    public interface Listener {
        void onFinished(int i, Object obj);
    }

    public static ModuleChannel createChannel(String str, Handler handler) {
        return new ModuleChannel(handler, ImsRegistry.getServiceModuleManager().getServiceModuleHandler(str));
    }

    private ModuleChannel(Handler handler, Handler handler2) {
        this.mSrc = handler;
        this.mDst = handler2;
    }

    public void sendEvent(int i, Object obj, Listener listener) {
        Message obtain = Message.obtain(this.mDst, i, obj);
        Handler handler = this.mSrc;
        if (!(handler == null || listener == null)) {
            Message obtain2 = Message.obtain(handler, EVT_MODULE_CHANNEL_RESPONSE, listener);
            Bundle bundle = new Bundle();
            bundle.putParcelable("callback_msg", obtain2);
            obtain.setData(bundle);
        }
        if (obtain.getTarget() != null) {
            obtain.sendToTarget();
        }
    }

    public void disableFeature(long j) {
        Message.obtain(this.mDst, EVT_CAPDISCOVERY_DISABLE_FEATURE, Long.valueOf(j)).sendToTarget();
    }
}
