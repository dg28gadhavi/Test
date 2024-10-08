package com.sec.internal.ims.entitlement.nsds;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSModule;
import com.sec.internal.interfaces.ims.core.ISimManager;

public class NSDSModuleFactory {
    private static final String LOG_TAG = "NSDSModuleFactory";
    private static final Mno[] mSimMno = {Mno.ATT, Mno.TMOUS, Mno.GCI};
    private static NSDSModuleFactory sInstance = null;
    private final Context mContext;
    private final Looper mServiceLooper;

    private NSDSModuleFactory(Looper looper, Context context) {
        this.mServiceLooper = looper;
        this.mContext = context;
    }

    public static synchronized void createInstance(Looper looper, Context context) {
        synchronized (NSDSModuleFactory.class) {
            if (sInstance == null) {
                sInstance = new NSDSModuleFactory(looper, context);
            }
        }
    }

    public static NSDSModuleFactory getInstance() {
        return sInstance;
    }

    public NSDSModuleBase getNsdsModule(ISimManager iSimManager) {
        if (iSimManager == null) {
            Log.e(LOG_TAG, "getNsdsModule: simManager null");
            return null;
        }
        Mno simMno = iSimManager.getSimMno();
        for (Mno mno : mSimMno) {
            if (mno == simMno) {
                Log.i(LOG_TAG, "getNsdsModule: sim mno = " + simMno);
                return new NSDSModule(this.mServiceLooper, this.mContext, iSimManager);
            }
        }
        return null;
    }
}
