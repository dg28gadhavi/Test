package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorIR92 extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnIR92";

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int i) {
        return true;
    }

    public RegistrationGovernorIR92(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        if (registerTask.getMno() == Mno.ALTICE) {
            IMSLog.i(LOG_TAG, registerTask.getPhoneId(), "Force to enable vocecall_type for ATL.");
            ImsConstants.SystemSettings.setVoiceCallType(context, 0, registerTask.getPhoneId());
        }
        if (!DmConfigHelper.readSwitch(this.mContext, "mmtel", true, this.mPhoneId)) {
            this.mRegMan.getEventLog().logAndAdd("VOLTE_ENABLED=0. Recover OMADM nodes.");
            ContentValues contentValues = new ContentValues();
            contentValues.put("VOLTE_ENABLED", "1");
            contentValues.put("LVC_ENABLED", "1");
            contentValues.put("EAB_SETTING", "1");
            contentValues.put("VWF_ENABLED", "1");
            this.mContext.getContentResolver().insert(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/", this.mPhoneId), contentValues);
        }
        updateVolteState();
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> set) {
        Log.i(LOG_TAG, "applyVoPsPolicy:");
        if (set == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mTask.getPhoneId());
            if (simManagerFromSimSlot != null && "GenericIR92_US:CSpire".equals(simManagerFromSimSlot.getSimMnoName())) {
                return new HashSet();
            }
            removeService(set, "mmtel", "applyVoPsPolicy");
        }
        return set;
    }

    public Set<String> filterService(Set<String> set, int i) {
        return super.filterService(applyMmtelUserSettings(set, i), i);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0029, code lost:
        if (r3 >= 3) goto L_0x0018;
     */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:14:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onPdnRequestFailed(com.sec.internal.constants.ims.core.PdnFailReason r3, int r4) {
        /*
            r2 = this;
            super.onPdnRequestFailed(r3, r4)
            com.sec.internal.ims.core.RegisterTask r4 = r2.mTask
            int r4 = r4.getRegistrationRat()
            r0 = 13
            if (r4 != r0) goto L_0x003e
            com.sec.internal.ims.core.RegisterTask r4 = r2.mTask
            int r4 = r4.getPhoneId()
            com.sec.internal.constants.ims.core.PdnFailReason r0 = com.sec.internal.constants.ims.core.PdnFailReason.SERVICE_OPTION_NOT_SUBSCRIBED
            r1 = 1
            if (r0 != r3) goto L_0x001a
        L_0x0018:
            r3 = r1
            goto L_0x002d
        L_0x001a:
            java.lang.String r3 = "voice_domain_pref_eutran"
            int r3 = com.sec.internal.ims.registry.ImsRegistry.getInt(r4, r3, r1)
            r4 = 3
            if (r3 != r4) goto L_0x002c
            int r3 = r2.mPdnRejectCounter
            int r3 = r3 + r1
            r2.mPdnRejectCounter = r3
            if (r3 < r4) goto L_0x002c
            goto L_0x0018
        L_0x002c:
            r3 = 0
        L_0x002d:
            if (r3 == 0) goto L_0x003e
            java.lang.String r3 = "RegiGvnIR92"
            java.lang.String r4 = "send ImsNotAvailable"
            android.util.Log.i(r3, r4)
            com.sec.internal.ims.core.RegistrationManagerInternal r3 = r2.mRegMan
            com.sec.internal.ims.core.RegisterTask r2 = r2.mTask
            r3.notifyImsNotAvailable(r2, r1)
        L_0x003e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorIR92.onPdnRequestFailed(com.sec.internal.constants.ims.core.PdnFailReason, int):void");
    }

    public boolean isReadyToRegister(int i) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mTask.getPhoneId());
        if (simManagerFromSimSlot != null && ("GenericIR92_US:CSpire".equals(simManagerFromSimSlot.getSimMnoName()) || "GenericIR92_US:Cellcom".equals(simManagerFromSimSlot.getSimMnoName()))) {
            String line1Number = simManagerFromSimSlot.getLine1Number(simManagerFromSimSlot.getSubscriptionId());
            if (TextUtils.isEmpty(line1Number) || line1Number.startsWith("000000")) {
                Log.e(LOG_TAG, "Invalid MSISDN, pending IMS Register until SIM OTA");
                return false;
            }
        }
        return super.isReadyToRegister(i);
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        eventLog.logAndAdd("onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mTask.getPhoneId());
        if (simManagerFromSimSlot == null || ((!"GenericIR92_US:Cellcom".equals(simManagerFromSimSlot.getSimMnoName()) && !"GenericIR92_US:CSpire".equals(simManagerFromSimSlot.getSimMnoName())) || (!SipErrorBase.isImsForbiddenError(sipError) && !SipErrorBase.NOT_FOUND.equals(sipError)))) {
            super.onRegistrationError(sipError, j, z);
            return;
        }
        int i = this.mFailureCounter + 1;
        this.mFailureCounter = i;
        int i2 = this.mNumOfPcscfIp;
        this.mCurPcscfIpIdx = (this.mCurPcscfIpIdx + 1) % i2;
        if (i < i2 || (this.mCurImpu == 1 && i < i2 * 3)) {
            if (j <= 0) {
                j = getWaitTime();
            }
            startRetryTimer(j);
        } else if (!ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) || this.mCurImpu == 1) {
            Log.e(LOG_TAG, "onRegistrationError: Permanently prohibited.");
            this.mIsPermanentStopped = true;
            if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
            }
        } else {
            Log.e(LOG_TAG, "onRegistrationError: Retry with IMSI based Register before blocking PLMN");
            if (j <= 0) {
                j = getWaitTime();
            }
            startRetryTimer(j);
            this.mCurImpu = 1;
        }
    }

    public void onVolteSettingChanged() {
        updateVolteState();
    }
}
