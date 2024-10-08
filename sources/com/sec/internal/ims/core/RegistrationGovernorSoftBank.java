package com.sec.internal.ims.core;

import android.content.Context;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;

public class RegistrationGovernorSoftBank extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGovernorSoftBank";

    public RegistrationGovernorSoftBank(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
    }

    public void onRegistrationTerminated(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd("onRegistrationTerminated: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j);
        if (SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(sipError)) {
            Log.e(LOG_TAG, "onRegistrationError: Silently Purge the IMS Registration and dont send REGISTER");
            this.mCurPcscfIpIdx++;
            this.mFailureCounter = 0;
            this.mIsPermanentStopped = true;
            return;
        }
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        startRetryTimer(1000);
    }

    /* access modifiers changed from: protected */
    public void handleTimeoutError(long j) {
        Log.e(LOG_TAG, "onRegistrationError: Timer F fired.");
        this.mTask.mKeepPdn = true;
        this.mRegHandler.sendTryRegister(this.mPhoneId);
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        this.mFailureCounter = this.mFailureCounter + 1;
        this.mCurPcscfIpIdx = this.mCurPcscfIpIdx + 1;
        if (SipErrorBase.FORBIDDEN.getCode() == sipError.getCode()) {
            handleForbiddenError(j);
        } else if (SipErrorBase.REQUEST_TIMEOUT.equals(sipError) || SipErrorBase.NOT_FOUND.getCode() == sipError.getCode() || SipErrorBase.SERVER_INTERNAL_ERROR.equals(sipError) || SipErrorBase.SERVICE_UNAVAILABLE.equals(sipError) || SipErrorBase.BUSY_EVERYWHERE.equals(sipError)) {
            if (j == 0) {
                this.mCurPcscfIpIdx--;
                j = getWaitTime();
            } else if (z) {
                this.mCurPcscfIpIdx--;
            }
            startRetryTimer(j);
        } else if (SipErrorBase.SIP_TIMEOUT.equals(sipError)) {
            handleTimeoutError(j);
        }
    }

    public void onSubscribeError(int i, SipError sipError) {
        Log.i(LOG_TAG, "onSubscribeError: state " + this.mTask.getState() + " error " + sipError + ", event = " + i);
        if (i == 0) {
            if (SipErrorBase.SIP_TIMEOUT.equals(sipError) || SipErrorBase.REQUEST_TIMEOUT.equals(sipError)) {
                Log.i(LOG_TAG, " complain to governor");
                this.mTask.getGovernor().onRegistrationError(sipError, 0, false);
            }
            if (!SipErrorBase.REQUEST_TIMEOUT.equals(sipError) && SipErrorBase.NOT_FOUND.getCode() != sipError.getCode() && !SipErrorBase.SERVER_INTERNAL_ERROR.equals(sipError) && !SipErrorBase.SERVICE_UNAVAILABLE.equals(sipError)) {
                SipErrorBase.BUSY_EVERYWHERE.equals(sipError);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int i) {
        if (i == 18 || getVoiceTechType(this.mPhoneId) == 0) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: volte disabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }
}
