package com.sec.internal.ims.gba;

import android.content.Context;
import android.util.SparseArray;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;

public class GbaServiceModuleProxy {
    private static SparseArray<GbaServiceModuleProxy> mProxy = new SparseArray<>();
    private Context mContext;
    private IGbaServiceModule mGbaServiceModule;

    public static synchronized GbaServiceModuleProxy getInstance(int i) {
        GbaServiceModuleProxy gbaServiceModuleProxy;
        synchronized (GbaServiceModuleProxy.class) {
            if (mProxy.get(i) == null) {
                mProxy.put(i, new GbaServiceModuleProxy(i));
            }
            gbaServiceModuleProxy = mProxy.get(i);
        }
        return gbaServiceModuleProxy;
    }

    private GbaServiceModuleProxy(int i) {
        this.mContext = null;
        this.mGbaServiceModule = null;
        this.mContext = ImsRegistry.getContext();
    }

    public synchronized void setContext(Context context) {
        this.mContext = context;
    }

    public synchronized Context getContext() {
        if (this.mContext == null) {
            this.mContext = ImsServiceStub.getInstance().getContext();
        }
        return this.mContext;
    }

    public synchronized void setGbaServiceModule(GbaServiceModule gbaServiceModule) {
        this.mGbaServiceModule = gbaServiceModule;
    }

    public synchronized IGbaServiceModule getGbaServiceModule() {
        if (this.mGbaServiceModule == null) {
            this.mGbaServiceModule = ImsRegistry.getServiceModuleManager().getGbaServiceModule();
        }
        return this.mGbaServiceModule;
    }
}
