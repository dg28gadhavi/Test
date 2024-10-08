package com.sec.internal.ims.core;

import android.content.Context;
import android.provider.Settings;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class RegistrationGovernorDish extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnDish";

    public RegistrationGovernorDish(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " unsolicit " + z);
        if (j < 0) {
            j = 0;
        }
        this.mFailureCounter++;
        this.mCurPcscfIpIdx++;
        if (!this.mTask.getProfile().hasEmergencySupport() || !SipErrorBase.SIP_TIMEOUT.equals(sipError) || this.mTask.getProfile().getE911RegiTime() <= 0) {
            handleRetryTimer(j);
        } else {
            handleTimeOutEmerRegiError();
        }
    }

    public Set<String> filterService(Set<String> set, int i) {
        Set<String> filterService = super.filterService(set, i);
        boolean isDataAllowed = isDataAllowed();
        IMSLog.i(LOG_TAG, this.mPhoneId, "Data allowed: " + isDataAllowed);
        if (!isDataAllowed && i != 18) {
            removeService(filterService, "mmtel-video", "MobileData OFF");
        }
        if (this.mTask.isRcsOnly() && !RcsUtils.DualRcs.isRegAllowed(this.mContext, this.mPhoneId)) {
            for (String removeService : ImsProfile.getRcsServiceList()) {
                removeService(filterService, removeService, "No DualRcs");
            }
        }
        return filterService;
    }

    private boolean isDataAllowed() {
        boolean z = Settings.Global.getInt(this.mContext.getContentResolver(), "data_roaming", 0) == 1;
        boolean isNetworkRoaming = this.mTelephonyManager.isNetworkRoaming();
        if ((isNetworkRoaming || !NetworkUtil.isMobileDataOn(this.mContext)) && (!isNetworkRoaming || !z)) {
            return false;
        }
        return true;
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkCallStatus());
    }

    public void onTelephonyCallStatusChanged(int i) {
        super.onTelephonyCallStatusChanged(i);
        if (i == 0 && this.mTask.getProfile().hasEmergencySupport()) {
            this.mTask.setDeregiReason(7);
            this.mRegMan.deregister(this.mTask, true, false, "Call status changed. Deregister..");
        }
    }
}
