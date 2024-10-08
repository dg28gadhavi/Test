package com.sec.internal.ims.core;

import android.content.Context;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorCbn extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnCbn";
    protected List<String> mRcsPcscfList = new ArrayList();

    public RegistrationGovernorCbn(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        super.onPdnRequestFailed(pdnFailReason, i);
        if (!isMatchedPdnFailReason(pdnFailReason)) {
            onPdnFailCounterInNr();
        }
    }

    public void checkAcsPcscfListChange() {
        if (this.mTask.isRcsOnly()) {
            ArrayList arrayList = new ArrayList();
            String readStringParam = RcsConfigurationHelper.readStringParam(this.mContext, "address", (String) null);
            if (readStringParam == null) {
                IMSLog.i(LOG_TAG, "checkAcsPcscfIpListChange : lboPcscfAddress is null");
                return;
            }
            arrayList.add(readStringParam);
            IMSLog.i(LOG_TAG, "checkAcsPcscfIpListChange : previous pcscf = " + this.mRcsPcscfList + ", new pcscf = " + arrayList);
            if (!arrayList.equals(this.mRcsPcscfList)) {
                if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    this.mTask.setDeregiReason(8);
                    this.mRegMan.deregister(this.mTask, true, false, "pcscf updated");
                }
                resetPcscfList();
                ArrayList arrayList2 = new ArrayList();
                this.mRcsPcscfList = arrayList2;
                arrayList2.add(readStringParam);
                IMSLog.i(LOG_TAG, "checkAcsPcscfIpListChange : resetPcscfList");
            }
        }
    }

    public Set<String> filterService(Set<String> set, int i) {
        HashSet hashSet = new HashSet();
        Set<String> hashSet2 = new HashSet<>(set);
        if (isImsDisabled()) {
            return new HashSet();
        }
        if ((i == 13 || i == 20) && this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "filterService: IMSVoPS is not supported");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
            return new HashSet();
        }
        if (this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            hashSet2 = applyMmtelUserSettings(hashSet2, i);
        }
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) != 1) {
            z = false;
        }
        if (z) {
            hashSet.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
            if (!hashSet.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        if (!hashSet2.isEmpty()) {
            hashSet2.retainAll(hashSet);
        }
        return hashSet2;
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        forceTurnOnVoLteWhenMenuRemoved();
        return super.getVoiceTechType();
    }
}
