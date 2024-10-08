package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimpleEventLog;
import java.util.List;

class SimDataAdaptor {
    protected static final String LOG_TAG = "SimDataAdaptor";
    protected String mLastOperator = "";
    protected int mPreferredImpuIndex = 1;
    protected SimManager mSimManager;
    protected SimpleEventLog mSimpleEventLog = null;

    public boolean hasValidMsisdn() {
        return true;
    }

    SimDataAdaptor(SimManager simManager) {
        this.mSimManager = simManager;
        this.mSimpleEventLog = simManager.getSimpleEventLog();
        this.mLastOperator = this.mSimManager.getSimOperator();
    }

    public static SimDataAdaptor getSimDataAdaptor(SimManager simManager) {
        Mno simMno = simManager.getSimMno();
        if (simMno == Mno.TMOUS) {
            return new SimDataAdaptorTmoUs(simManager);
        }
        if (simMno == Mno.ATT) {
            return new SimDataAdaptorAtt(simManager);
        }
        if (simMno == Mno.VZW) {
            return new SimDataAdaptorVzw(simManager);
        }
        if (simMno == Mno.GCF) {
            return new SimDataAdaptorGcf(simManager);
        }
        if (simMno == Mno.KDDI) {
            return new SimDataAdaptorKddi(simManager);
        }
        if (simMno == Mno.CMCC) {
            return new SimDataAdaptorCmcc(simManager);
        }
        if (simMno == Mno.SPRINT) {
            return new SimDataAdaptorSpr(simManager);
        }
        if (simMno == Mno.USCC) {
            return new SimDataAdaptorUsc(simManager);
        }
        if (simMno == Mno.BOG) {
            return new SimDataAdaptorBog(simManager);
        }
        if (simMno == Mno.ONENONE) {
            return new SimDataAdaptorONENONE(simManager);
        }
        return new SimDataAdaptor(simManager);
    }

    public String getEmergencyImpu(List<String> list) {
        Log.i(LOG_TAG, "getEmergencyImpu:");
        if (list == null || list.size() == 0 || TextUtils.isEmpty(list.get(0))) {
            return null;
        }
        return list.get(0);
    }

    public String getImpuFromList(List<String> list) {
        Log.i(LOG_TAG, "getImpuFromList:");
        if (list == null || list.size() == 0) {
            return null;
        }
        if (list.size() <= 1 || TextUtils.isEmpty(list.get(this.mPreferredImpuIndex)) || !SimManager.isValidImpu(list.get(this.mPreferredImpuIndex))) {
            return getValidImpu(list);
        }
        return list.get(this.mPreferredImpuIndex);
    }

    /* access modifiers changed from: protected */
    public String getValidImpu(List<String> list) {
        for (String next : list) {
            if (SimManager.isValidImpu(next)) {
                return next;
            }
        }
        return null;
    }

    public boolean needHandleLoadedAgain(String str) {
        if (TextUtils.equals(str, this.mLastOperator)) {
            return false;
        }
        String str2 = LOG_TAG;
        Log.i(str2, "Different operator. Last:" + this.mLastOperator + ", new:" + str);
        this.mLastOperator = str;
        return true;
    }
}
