package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.List;

/* compiled from: SimDataAdaptor */
class SimDataAdaptorBog extends SimDataAdaptor {
    private static final String LOG_TAG = "SimDataAdaptorBog";

    SimDataAdaptorBog(SimManager simManager) {
        super(simManager);
    }

    public String getImpuFromList(List<String> list) {
        if (!this.mSimManager.isEsim()) {
            return super.getImpuFromList(list);
        }
        Log.i(LOG_TAG, "getImpuFromList for BOG ESIM :");
        if (list == null || list.size() == 0) {
            return null;
        }
        if (list.size() <= 1 || TextUtils.isEmpty(list.get(this.mPreferredImpuIndex)) || !isValidImpu(list.get(this.mPreferredImpuIndex))) {
            return getValidImpu(list);
        }
        return list.get(this.mPreferredImpuIndex);
    }

    /* access modifiers changed from: protected */
    public String getValidImpu(List<String> list) {
        for (String next : list) {
            if (isValidImpu(next)) {
                return next;
            }
        }
        return null;
    }

    private boolean isValidImpu(String str) {
        ImsUri parse = ImsUri.parse(str);
        if (parse == null || parse.getUriType() != ImsUri.UriType.SIP_URI) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "invalid impu : " + str);
            return false;
        }
        String user = parse.getUser();
        if (TextUtils.isEmpty(user)) {
            return true;
        }
        String imsi = this.mSimManager.getImsi();
        if (TextUtils.isEmpty(imsi) || !user.contains(imsi) || user.length() == imsi.length()) {
            return true;
        }
        String str3 = LOG_TAG;
        IMSLog.s(str3, "invalid impu : " + str);
        return false;
    }
}
