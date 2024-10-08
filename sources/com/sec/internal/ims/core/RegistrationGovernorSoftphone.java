package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.sec.ims.extensions.WiFiManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RegistrationGovernorSoftphone extends RegistrationGovernorAtt {
    protected static final String INTENT_ACTION_WIFI_MODE_CHANGED = "com.samsung.android.net.wifi.SEC_NETWORK_STATE_CHANGED";
    private static final String LOG_TAG = "RegiGvnSoftp";
    protected int mForbiddenCount = 0;
    protected final long[] mForbiddenRetryTimeMs = {17000, 34000, 68000};
    protected final ShutdownEventReceiver mShutdownEventReceiver;
    protected final WifiEventReceiver mWifiEventReceiver;

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        return 0;
    }

    public RegistrationGovernorSoftphone(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        ShutdownEventReceiver shutdownEventReceiver = new ShutdownEventReceiver();
        this.mShutdownEventReceiver = shutdownEventReceiver;
        WifiEventReceiver wifiEventReceiver = new WifiEventReceiver();
        this.mWifiEventReceiver = wifiEventReceiver;
        Log.i(LOG_TAG, "Register : ShutdownEventReceiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        context.registerReceiver(shutdownEventReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(INTENT_ACTION_WIFI_MODE_CHANGED);
        context.registerReceiver(wifiEventReceiver, intentFilter2);
    }

    public boolean isLocationInfoLoaded(int i) {
        IMSLog.e(LOG_TAG, this.mPhoneId, "update geo location");
        Optional.ofNullable(ImsRegistry.getGeolocationController()).ifPresent(new RegistrationGovernorSoftphone$$ExternalSyntheticLambda0(this));
        return true;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$isLocationInfoLoaded$0(IGeolocationController iGeolocationController) {
        iGeolocationController.startGeolocationUpdate(this.mPhoneId, false);
    }

    public void unRegisterIntentReceiver() {
        Log.i(LOG_TAG, "Un-register intent receiver(s)");
        try {
            this.mContext.unregisterReceiver(this.mShutdownEventReceiver);
            this.mContext.unregisterReceiver(this.mWifiEventReceiver);
        } catch (IllegalArgumentException unused) {
            Log.e(LOG_TAG, "unRegisterIntentReceiver: Receiver not registered!");
        }
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        this.mForbiddenCount = 0;
        if (this.mPdnController.isWifiConnected()) {
            sendDelayMsgToWifi(true);
        } else {
            sendDelayMsgToWifi(false);
        }
    }

    public void onDeregistrationDone(boolean z) {
        sendDelayMsgToWifi(false);
    }

    protected class ShutdownEventReceiver extends BroadcastReceiver {
        protected ShutdownEventReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            RegistrationGovernorSoftphone.this.mIsPermanentStopped = true;
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction()) && RegistrationGovernorSoftphone.this.mPdnController.isWifiConnected()) {
                RegistrationGovernorSoftphone.this.mRegMan.sendDeregister(13);
                while (true) {
                    if (RegistrationGovernorSoftphone.this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING || RegistrationGovernorSoftphone.this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || RegistrationGovernorSoftphone.this.mTask.getState() == RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Log.e(RegistrationGovernorSoftphone.LOG_TAG, "Sleep exception : " + e);
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    protected class WifiEventReceiver extends BroadcastReceiver {
        protected WifiEventReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (RegistrationGovernorSoftphone.INTENT_ACTION_WIFI_MODE_CHANGED.equals(intent.getAction())) {
                Log.i(RegistrationGovernorSoftphone.LOG_TAG, "WifiEventReceiver: INTENT_ACTION_WIFI_MODE_CHANGED Received.");
                if (intent.getExtras().getBoolean("delayState", false)) {
                    RegistrationGovernorSoftphone.this.mRegMan.sendDeregister(13);
                    Log.i(RegistrationGovernorSoftphone.LOG_TAG, "WifiEventReceiver: send Deregister message.");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (this.mRegMan.getTelephonyCallStatus(this.mTask.getPhoneId()) != 0 || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
            return false;
        }
        if (!this.mTask.mIsUpdateRegistering) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: Task State is UpdateRegistering");
        return false;
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkCallStatus() && checkNetworkEvent(i));
    }

    public Set<String> filterService(Set<String> set, int i) {
        if (isImsDisabled()) {
            return new HashSet();
        }
        HashSet hashSet = new HashSet();
        boolean z = true;
        boolean z2 = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mTask.getPhoneId()) == 1;
        if (ImsConstants.SystemSettings.VILTE_SLOT1.get(this.mContext, -1) != 0) {
            z = false;
        }
        if (set != null) {
            hashSet.addAll(set);
        }
        if (!z2) {
            for (String removeService : ImsProfile.getVoLteServiceList()) {
                removeService(hashSet, removeService, "VoLTE disabled");
            }
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
        }
        if (!z || (i != 18 && !NetworkUtil.isMobileDataOn(this.mContext))) {
            removeService(hashSet, "mmtel-video", "MobileData or Setting off");
        }
        if (!this.mConfigModule.isValidAcsVersion(this.mTask.getPhoneId())) {
            for (String removeService2 : ImsProfile.getRcsServiceList()) {
                removeService(hashSet, removeService2, "RCS disabled.");
            }
        }
        return hashSet;
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv4Addr(List<String> list, List<String> list2, LinkPropertiesWrapper linkPropertiesWrapper) {
        boolean z;
        Iterator<String> it = list.iterator();
        while (true) {
            if (it.hasNext()) {
                if (NetworkUtil.isIPv4Address(it.next())) {
                    z = true;
                    break;
                }
            } else {
                z = false;
                break;
            }
        }
        if (z) {
            Log.i(LOG_TAG, "ipv4 address found");
            list2.clear();
            if (list2.isEmpty()) {
                for (String next : list) {
                    if (NetworkUtil.isIPv4Address(next)) {
                        list2.add(next);
                    }
                }
            }
        } else {
            Log.i(LOG_TAG, "Ipv4 pcscf addr isn't exist for Softphone");
            list2.clear();
        }
        return list2;
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        Log.i(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (j < 0) {
            j = 0;
        }
        if (SipErrorBase.isImsForbiddenError(sipError)) {
            Log.i(LOG_TAG, "403 response : " + this.mForbiddenCount);
            int i = this.mForbiddenCount;
            if (i >= 3) {
                Log.i(LOG_TAG, "got 403 response over 3 times...");
                this.mIsPermanentStopped = true;
                return;
            }
            long[] jArr = this.mForbiddenRetryTimeMs;
            this.mForbiddenCount = i + 1;
            long j2 = jArr[i];
            if (j2 > 0) {
                startRetryTimer(j2);
                return;
            }
            return;
        }
        super.onRegistrationError(sipError, j, z);
    }

    public SipError onSipError(String str, SipError sipError) {
        Log.i(LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        this.mIsValid = this.mNumOfPcscfIp > 0;
        if ("mmtel".equals(str) && (SipErrorBase.SIP_INVITE_TIMEOUT.equals(sipError) || SipErrorBase.SIP_TIMEOUT.equals(sipError) || SipErrorBase.FORBIDDEN.equals(sipError) || SipErrorBase.SERVER_TIMEOUT.equals(sipError))) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, false, this.mIsValid, "Sip Error[MMTEL]. DeRegister..");
        }
        if (SipErrorBase.PROXY_AUTHENTICATION_REQUIRED.equals(sipError) || SipErrorBase.SERVICE_UNAVAILABLE.equals(sipError)) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, false, this.mIsValid, "Sip Error 407 or 503. DeRegister..");
        }
        return sipError;
    }

    public void onPublishError(SipError sipError) {
        if (SipErrorBase.FORBIDDEN.equals(sipError)) {
            this.mTask.setDeregiReason(45);
            this.mRegMan.deregister(this.mTask, false, true, "publish error");
        }
    }

    public void releaseThrottle(int i) {
        if (i == 0) {
            this.mIsPermanentStopped = false;
        } else if (i == 1) {
            Log.i(LOG_TAG, "releaseThrottle: sendDeregister");
            this.mRegMan.sendDeregister(12);
            this.mIsPermanentStopped = false;
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mForbiddenCount = 0;
            stopRetryTimer();
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + i);
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkEvent(int i) {
        boolean isNetworkRoaming = this.mTelephonyManager.isNetworkRoaming();
        boolean isMobileDataOn = NetworkUtil.isMobileDataOn(this.mContext);
        boolean z = ImsConstants.SystemSettings.DATA_ROAMING.get(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN) == ImsConstants.SystemSettings.ROAMING_DATA_ENABLED;
        if (i != 13 || (isMobileDataOn && (!isNetworkRoaming || z))) {
            return true;
        }
        Log.i(LOG_TAG, "Mobile data off. Do not try IMS registration on LTE.");
        return false;
    }

    private void sendDelayMsgToWifi(boolean z) {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            Message obtain = Message.obtain((Handler) null, WiFiManagerExt.SEC_COMMAND_ID_DELAY_DISCONNECT_TRANSITION);
            obtain.arg1 = z ? 1 : 0;
            obtain.arg2 = z ? 10000 : 0;
            WiFiManagerExt.callSECApi(wifiManager, obtain);
            Log.i(LOG_TAG, "Notify to WiFiManager");
        }
    }

    public boolean determineDeRegistration(int i, int i2) {
        int registrationRat = this.mTask.getRegistrationRat();
        if ((registrationRat == 18 || i != 18) && (registrationRat != 18 || i == 18)) {
            return super.determineDeRegistration(i, i2);
        }
        this.mTask.setDeregiReason(4);
        this.mRegMan.tryDeregisterInternal(this.mTask, false, false);
        return true;
    }
}
