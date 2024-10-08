package com.sec.internal.ims.core;

import android.content.Context;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.helper.os.SystemUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.ICmcServiceHelper;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegistrationGovernorCmc extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnCmc";
    private boolean mLocalIpChangedDuringRegistering = false;
    private Map<Integer, Integer> mP2pSdList = new HashMap();
    private int mPermanentErrorCount = 0;

    public int getFailureType() {
        return 16;
    }

    public RegistrationGovernorCmc(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        boolean z = false;
        setUpsmEventReceiver();
        SemEmergencyManager instance = SemEmergencyManager.getInstance(this.mContext);
        if (instance != null && SystemUtil.checkUltraPowerSavingMode(instance)) {
            z = true;
        }
        this.mUpsmEnabled = z;
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        Log.e(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        StringBuilder sb = new StringBuilder();
        sb.append("onRegistrationError : ");
        sb.append(sipError);
        sb.append(", fail count : ");
        sb.append(this.mFailureCounter);
        eventLog.logAndAdd(sb.toString());
        boolean z2 = this.mLocalIpChangedDuringRegistering;
        setLocalIpChangeDuringRegistering(false);
        if (onP2pRegistrationError(this.mTask.getProfile().getCmcType(), sipError)) {
            Log.i(LOG_TAG, "Don't re-try registration regardless of error in p2p");
        } else if (SipErrorBase.isImsForbiddenError(sipError)) {
            handleForbiddenError(j);
            onCmcRegistrationError();
        } else {
            this.mPermanentErrorCount = 0;
            if (!z2 || sipError.getCode() != 1001) {
                super.onRegistrationError(sipError, j, z);
            } else {
                Log.i(LOG_TAG, "Don't do anything during deregistering with local IP changed");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleTimeoutError(long j) {
        if ((this.mCurPcscfIpIdx == this.mNumOfPcscfIp || checkValidPcscfIp(this.mPcscfIpList).isEmpty()) && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            resetPcscfList();
        }
    }

    public void releaseThrottle(int i) {
        if (i == 1) {
            Log.i("RegiGvnCmc[" + this.mTask.getPhoneId() + "]", "releaseThrottle: flight mode on");
            this.mTask.setDeregiReason(23);
            this.mRegMan.deregister(this.mTask, false, false, "flight mode enabled");
            this.mIsPermanentStopped = false;
        } else if (i == 7 || i == 8) {
            this.mIsPermanentStopped = false;
        } else if (i == 4) {
            this.mIsPermanentStopped = false;
            this.mCurImpu = 0;
        } else if (i == 5) {
            resetRetry();
            stopRetryTimer();
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + i);
        }
    }

    public Set<String> filterService(Set<String> set, int i) {
        if (ImsRegistry.getICmcConnectivityController().isEnabledWifiDirectFeature()) {
            IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "cmcType(wifidirect): " + this.mTask.getProfile().getCmcType());
            if (this.mTask.getProfile().getCmcType() == 7 || this.mTask.getProfile().getCmcType() == 8) {
                IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "Skip filterService in [WIFI-DIRECT] mode");
                return set;
            }
        }
        HashSet hashSet = new HashSet();
        HashSet hashSet2 = new HashSet(set);
        if (ImsRegistry.getCmcAccountManager().isCmcEnabled()) {
            for (String add : ImsProfile.getVoLteServiceList()) {
                hashSet.add(add);
            }
        }
        if (!hashSet2.isEmpty()) {
            hashSet2.retainAll(hashSet);
        }
        return hashSet2;
    }

    public void onDeregistrationDone(boolean z) {
        if (this.mTask.getProfile().getCmcType() != 0) {
            ImsRegistry.getCmcAccountManager().notifyCmcDeviceChanged();
        }
        setLocalIpChangeDuringRegistering(false);
    }

    public void onRegistrationDone() {
        this.mPermanentErrorCount = 0;
        if (this.mTask.getProfile().getCmcType() != 0) {
            ImsRegistry.getCmcAccountManager().notifyCmcDeviceChanged();
        }
    }

    public List<String> addIpv4Addr(List<String> list, List<String> list2, LinkPropertiesWrapper linkPropertiesWrapper) {
        if (list2.isEmpty()) {
            Log.i(LOG_TAG, "Empty Pcscf IP list");
            for (String next : list) {
                if (NetworkUtil.isIPv4Address(next)) {
                    list2.add(next);
                }
            }
        }
        return list2;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int i) {
        if (i == 18 || getVoiceTechType(this.mTask.getPhoneId()) == 0) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: volte disabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    public boolean isReadyToRegister(int i) {
        if (ImsRegistry.getICmcConnectivityController().isEnabledWifiDirectFeature()) {
            int phoneId = this.mTask.getPhoneId();
            IMSLog.i(LOG_TAG, phoneId, "cmcType(wifidirect): " + this.mTask.getProfile().getCmcType());
            if (this.mTask.getProfile().getCmcType() == 7 || this.mTask.getProfile().getCmcType() == 8) {
                IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "Skip isReadyToRegister in [WIFI-DIRECT] mode");
                return super.isReadyToRegister(i);
            }
        }
        ICmcAccountManager cmcAccountManager = ImsRegistry.getCmcAccountManager();
        int currentLineSlotIndex = cmcAccountManager.getCurrentLineSlotIndex();
        if (cmcAccountManager.isProfileUpdateFailed()) {
            IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "isReadyToRegister: profile update failed");
            return false;
        } else if (this.mUpsmEnabled) {
            IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "isReadyToRegister: UPMS ON");
            return false;
        } else {
            if (i != 18) {
                if (cmcAccountManager.isSupportSameWiFiOnly()) {
                    int phoneId2 = this.mTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId2, "isReadyToRegister: cmc SameWifiOnly in rat: " + i);
                    return false;
                } else if (cmcAccountManager.isWifiOnly()) {
                    int phoneId3 = this.mTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId3, "isReadyToRegister: cmc WiFi preferred rat:" + i + ",lineslot:" + currentLineSlotIndex);
                    return false;
                }
            }
            if (cmcAccountManager.isSecondaryDevice()) {
                if (this.mTask.getPhoneId() != SimUtil.getActiveDataPhoneId()) {
                    int phoneId4 = this.mTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId4, "isReadyToRegister: cmc non ads - line slot:" + currentLineSlotIndex);
                    return false;
                }
            } else if (!cmcAccountManager.hasSecondaryDevice()) {
                return false;
            } else {
                if (i == 18) {
                    if (this.mTask.getPhoneId() != currentLineSlotIndex) {
                        int phoneId5 = this.mTask.getPhoneId();
                        IMSLog.i(LOG_TAG, phoneId5, "isReadyToRegister: wifi : non line slot: " + currentLineSlotIndex);
                        return false;
                    } else if (TelephonyManagerWrapper.getInstance(this.mContext).getSimState(currentLineSlotIndex) == 1) {
                        int phoneId6 = this.mTask.getPhoneId();
                        IMSLog.i(LOG_TAG, phoneId6, "isReadyToRegister: wifi : SIM ABSENT at slot: " + currentLineSlotIndex);
                        return false;
                    }
                } else if (!(this.mTask.getPhoneId() == currentLineSlotIndex && this.mTask.getPhoneId() == SimUtil.getActiveDataPhoneId())) {
                    int phoneId7 = this.mTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId7, "isReadyToRegister: cmc non ads or line slot" + currentLineSlotIndex);
                    return false;
                }
            }
            return super.isReadyToRegister(i);
        }
    }

    public void onRegEventContactUriNotification(List<ImsUri> list, int i, String str, String str2) {
        ICmcServiceHelper cmcServiceHelper;
        int i2;
        ArrayList arrayList = new ArrayList();
        String currentPcscfIp = getCurrentPcscfIp();
        if (currentPcscfIp.isEmpty()) {
            IMSLog.e(LOG_TAG, this.mTask.getPhoneId(), "current pcscfIp is empty");
            return;
        }
        IMSLog.i(LOG_TAG, "onRegEventContactUriNotification: emergencyNumbers: " + IMSLog.checker(str2));
        ImsRegistry.getCmcAccountManager().setEmergencyNumbers(str2);
        int i3 = NetworkUtil.isIPv6Address(currentPcscfIp) ? 2 : 1;
        IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "localIpType : " + i3);
        int cmcType = this.mTask.getProfile().getCmcType();
        if (cmcType > 2) {
            int p2pListSize = getP2pListSize(cmcType);
            if (i == 1) {
                i2 = p2pListSize + 1;
            } else {
                i2 = p2pListSize - 1;
                if (i2 < 0) {
                    i2 = 0;
                }
            }
            this.mP2pSdList.put(Integer.valueOf(cmcType), Integer.valueOf(i2));
            IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "cmcType: " + cmcType + ", isRegi:" + i + ", size: " + this.mP2pSdList.get(Integer.valueOf(cmcType)));
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (ImsUri next : list) {
            if (next.getHost() == null) {
                break;
            }
            boolean isIPv6Address = NetworkUtil.isIPv6Address(next.getHost());
            sb.append("(IP : ");
            sb.append(next.getHost());
            sb.append(", isIPv6 : ");
            sb.append(isIPv6Address);
            sb.append(")");
            if ((i3 == 2 && isIPv6Address) || (i3 == 1 && !isIPv6Address)) {
                arrayList.add(next.getHost());
            }
        }
        IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "onRegEventContactUriNotification strUriList: " + sb + " hostlist: " + arrayList);
        ImsRegistry.getCmcAccountManager().setRegiEventNotifyHostInfo(arrayList);
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        if (iVolteServiceModule != null && (cmcServiceHelper = iVolteServiceModule.getCmcServiceHelper()) != null) {
            cmcServiceHelper.onRegEventContactUriNotification(this.mTask.getPhoneId(), arrayList);
        }
    }

    /* access modifiers changed from: protected */
    public int onUltraPowerSavingModeChanged() {
        int onUltraPowerSavingModeChanged = super.onUltraPowerSavingModeChanged();
        if (onUltraPowerSavingModeChanged == 0) {
            this.mRegMan.deregister(this.mTask, false, false, 0, "UPSM ON. CMC deregister");
        } else if (onUltraPowerSavingModeChanged == -1) {
            this.mRegHandler.sendTryRegister(this.mTask.getPhoneId());
        }
        return -1;
    }

    private void onCmcRegistrationError() {
        boolean z = true;
        this.mPermanentErrorCount++;
        ICmcAccountManager cmcAccountManager = ImsRegistry.getCmcAccountManager();
        if (this.mPermanentErrorCount >= 2) {
            z = false;
        }
        cmcAccountManager.startSAService(z);
    }

    private boolean onP2pRegistrationError(int i, SipError sipError) {
        if (i != 8) {
            return i == 4 && SipErrorBase.isImsForbiddenError(sipError);
        }
        return true;
    }

    public int getP2pListSize(int i) {
        int i2;
        Iterator<Map.Entry<Integer, Integer>> it = this.mP2pSdList.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                i2 = 0;
                break;
            }
            Map.Entry next = it.next();
            if (((Integer) next.getKey()).intValue() == i) {
                i2 = ((Integer) next.getValue()).intValue();
                break;
            }
        }
        int phoneId = this.mTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "getP2pListSize size: " + i2);
        return i2;
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv6Addr(List<String> list, List<String> list2, LinkPropertiesWrapper linkPropertiesWrapper) {
        boolean hasGlobalIPv6Address = linkPropertiesWrapper.hasGlobalIPv6Address();
        boolean hasIPv6DefaultRoute = linkPropertiesWrapper.hasIPv6DefaultRoute();
        int phoneId = this.mTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "addIpv6Addr: globalIpv6: " + hasGlobalIPv6Address + ", default route: " + hasIPv6DefaultRoute);
        if (hasGlobalIPv6Address) {
            for (String next : list) {
                if (NetworkUtil.isIPv6Address(next)) {
                    list2.add(next);
                }
            }
        }
        return list2;
    }

    public void updatePcscfIpList(List<String> list) {
        if (list == null) {
            Log.e(LOG_TAG, "updatePcscfIpList: null P-CSCF list!");
        } else {
            super.updatePcscfIpList(new ArrayList(list));
        }
    }

    public void onRegistrationTerminated(SipError sipError, long j, boolean z) {
        setLocalIpChangeDuringRegistering(false);
        super.onRegistrationTerminated(sipError, j, z);
    }

    public void onLocalIpChanged() {
        if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
            setLocalIpChangeDuringRegistering(true);
        }
    }

    private void setLocalIpChangeDuringRegistering(boolean z) {
        if (this.mLocalIpChangedDuringRegistering != z) {
            Log.i(LOG_TAG, "setLocalIpChangeDuringRegistering: " + z);
            this.mLocalIpChangedDuringRegistering = z;
        }
    }
}
