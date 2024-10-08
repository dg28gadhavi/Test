package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;

/* compiled from: SimDataAdaptor */
class SimDataAdaptorSpr extends SimDataAdaptor {
    private static final String LOG_TAG = "SimDataAdaptorSpr";
    private String mLastMsisdn = null;

    public SimDataAdaptorSpr(SimManager simManager) {
        super(simManager);
    }

    public boolean hasValidMsisdn() {
        SimManager simManager = this.mSimManager;
        String line1Number = simManager.getLine1Number(simManager.getSubscriptionId());
        String str = LOG_TAG;
        IMSLog.s(str, "hasValidMsisdn : " + line1Number + " Subscription ID :" + this.mSimManager.getSubscriptionId());
        this.mLastMsisdn = line1Number;
        if (isValidMsisdn(line1Number)) {
            return true;
        }
        this.mSimpleEventLog.logAndAdd("hasValidMsisdn: HFA isn't completed for SPR");
        IMSLog.c(LogClass.SIM_SPR_NEED_HFA, this.mSimManager.getSimSlotIndex() + ",NEED HFA");
        return false;
    }

    public boolean needHandleLoadedAgain(String str) {
        if (super.needHandleLoadedAgain(str)) {
            return true;
        }
        String line1Number = this.mSimManager.getLine1Number();
        if (TextUtils.equals(this.mLastMsisdn, line1Number) || !isValidMsisdn(line1Number)) {
            return false;
        }
        return true;
    }

    private boolean isValidMsisdn(String str) {
        return !TextUtils.isEmpty(str) && !str.startsWith("000000");
    }
}
