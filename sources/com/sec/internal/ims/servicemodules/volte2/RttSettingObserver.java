package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.sec.internal.log.IMSLog;

public class RttSettingObserver {
    /* access modifiers changed from: private */
    public static String LOG_TAG;
    private static String NAME;
    /* access modifiers changed from: private */
    public static String rttSettingDb = "preferred_rtt_mode";
    /* access modifiers changed from: private */
    public Context mContext;
    public ContentObserver mRttSettingObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            int i = Settings.Secure.getInt(RttSettingObserver.this.mContext.getContentResolver(), RttSettingObserver.rttSettingDb, 0);
            String r0 = RttSettingObserver.LOG_TAG;
            IMSLog.i(r0, "RttSettingObserver onChange: " + i);
            RttSettingObserver.this.mVsm.setRttMode(i);
        }
    };
    /* access modifiers changed from: private */
    public IVolteServiceModuleInternal mVsm = null;

    static {
        String simpleName = VolteServiceModule.class.getSimpleName();
        NAME = simpleName;
        LOG_TAG = simpleName;
    }

    RttSettingObserver(Context context, IVolteServiceModuleInternal iVolteServiceModuleInternal) {
        this.mContext = context;
        this.mVsm = iVolteServiceModuleInternal;
    }

    /* access modifiers changed from: protected */
    public void init() {
        Context context = this.mContext;
        if (context != null) {
            registerRttSettingObserver(context);
        }
    }

    private void registerRttSettingObserver(Context context) {
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(rttSettingDb), false, this.mRttSettingObserver);
    }
}
