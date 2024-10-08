package com.sec.internal.ims.entitlement.config;

import android.content.Context;
import android.os.Looper;
import com.sec.internal.constants.Mno;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntitlementConfigFactory {
    private static final String LOG_TAG = "EntitlementConfigFactory";
    private static EntitlementConfigFactory sInstance = null;
    private static Map<Mno, Class<?>> sSalesCodeConfigImplMap;
    private Context mContext;
    private Looper mServiceLooper;

    static {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        sSalesCodeConfigImplMap = concurrentHashMap;
        concurrentHashMap.put(Mno.TMOUS, NSDSConfigModule.class);
    }

    private EntitlementConfigFactory(Looper looper, Context context) {
        this.mServiceLooper = looper;
        this.mContext = context;
    }

    public static synchronized void createInstance(Looper looper, Context context) {
        synchronized (EntitlementConfigFactory.class) {
            if (sInstance == null) {
                sInstance = new EntitlementConfigFactory(looper, context);
            }
        }
    }

    public static EntitlementConfigFactory getInstance() {
        return sInstance;
    }

    public EntitlementConfigModuleBase getDeviceConfigModule(ISimManager iSimManager) {
        if (iSimManager == null) {
            return null;
        }
        try {
            Mno simMno = iSimManager.getSimMno();
            String str = LOG_TAG;
            IMSLog.i(str, "createMnoStrategy: mno = " + simMno);
            if (sSalesCodeConfigImplMap.get(simMno) != null) {
                return (EntitlementConfigModuleBase) sSalesCodeConfigImplMap.get(simMno).getConstructor(new Class[]{Looper.class, Context.class, ISimManager.class}).newInstance(new Object[]{this.mServiceLooper, this.mContext, iSimManager});
            }
        } catch (IllegalAccessException | IllegalArgumentException | IllegalStateException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "Exception : " + e.getMessage());
        }
        return null;
    }
}
