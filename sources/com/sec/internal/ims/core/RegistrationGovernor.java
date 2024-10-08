package com.sec.internal.ims.core;

import android.net.Uri;
import android.os.Message;
import android.os.SystemClock;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.helper.DelayedMessage;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import java.util.List;

public abstract class RegistrationGovernor implements IRegistrationGovernor {
    protected static final long DEFAULT_RETRY_AFTER_MS = 1000;
    public static final int PREFERED_IMPU_TYPE_ANY_FIRST = 0;
    public static final int PREFERED_IMPU_TYPE_IMSI_BASED = 1;
    public static final int RELEASE_AIRPLANEMODE_ON = 1;
    public static final int RELEASE_ALWAYS = 0;
    public static final int RELEASE_AUTOCONFIG_UPDATED = 7;
    public static final int RELEASE_CMC_UPDATED = 8;
    public static final int RELEASE_DETACH_WITH_REATTACH = 10;
    public static final int RELEASE_LTE_NETWORK_IN_SERVICE = 12;
    public static final int RELEASE_NETWORK_CHANGED = 6;
    public static final int RELEASE_NO_ALTERNATIVE = 14;
    public static final int RELEASE_NR_NETWORK_IN_SERVICE = 13;
    public static final int RELEASE_PDN_DISCONNECTED = 5;
    public static final int RELEASE_PLMN_CHANGED = 9;
    public static final int RELEASE_ROAMING_CHANGED = 11;
    public static final int RELEASE_SIM_REMOVED = 4;
    public static final int RELEASE_WFC_TURNED_OFF = 3;
    public static final int RELEASE_WIFI_TURNED_OFF = 2;
    public static final long RETRY_AFTER_EPDGDEREGI_MS = 1000;
    public static final long RETRY_AFTER_PDNLOST_MS = 3000;
    public static final String RETRY_TO_NEXT_PCSCF = "next_pcscf";
    public static final String RETRY_TO_SAME_PCSCF = "same_pcscf";
    public static final int THROTTLE_AFTER_PDN_DISCONNECTED = 1;
    public static final int THROTTLE_REASON_DEFAULT = 0;
    protected int mCallStatus = 0;
    protected String mCountry = null;
    protected int mCurImpu = 0;
    protected int mCurPcscfIpIdx = 0;
    protected boolean mDelayedDeregisterTimerRunning = false;
    protected boolean mDiscardCurrentNetwork = false;
    protected int mFailureCounter = 0;
    protected boolean mHandlePcscfOnAlternativeCall = false;
    protected boolean mHasPdnFailure = false;
    protected boolean mHasVoLteCall = false;
    protected boolean mIPsecAllow = true;
    protected boolean mIsPermanentPdnFailed = false;
    protected boolean mIsPermanentStopped = false;
    protected boolean mIsReadyToGetReattach = false;
    protected boolean mIsValid = false;
    protected boolean mMoveToNextPcscfAfterTimerB = false;
    protected boolean mNeedToCheckLocationSetting = true;
    protected boolean mNeedToCheckSrvcc = false;
    protected boolean mNonVoLTESimByPdnFail = false;
    protected int mNumOfPcscfIp = 0;
    protected PcoType mPcoType = PcoType.PCO_POSTPAY;
    protected List<String> mPcscfIpList = null;
    protected long mPdnFailRetryTime = -1;
    protected int mPdnRejectCounter = 0;
    protected int mPhoneId;
    protected boolean mPse911Prohibited = false;
    protected long mRegBaseTimeMs = 30000;
    protected long mRegMaxTimeMs = 1800000;
    protected long mRegiAt = 0;
    protected Message mRetryTimeout = null;
    protected int mSubscribeForbiddenCounter = 0;
    protected int mThrottleReason = 0;
    protected boolean mThrottledforImsNotAvailable = false;
    protected DelayedMessage mTimEshtablishTimeout = null;
    protected DelayedMessage mTimEshtablishTimeoutRCS = null;
    protected String mTimEshtablishTimeoutReason = null;
    protected boolean mUpsmEnabled = false;
    protected int mWFCSubscribeForbiddenCounter = 0;

    public boolean blockImmediatelyUpdateRegi() {
        return false;
    }

    public void checkAcsPcscfListChange() {
    }

    public boolean checkEmergencyInProgress() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkEpdgEvent(int i) {
        return true;
    }

    public void checkProfileUpdateFromDM(boolean z) {
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int i) {
        return true;
    }

    public void checkUnProcessedVoLTEState() {
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int i) {
        return true;
    }

    public void clear() {
    }

    public void enableRcsOverIms(ImsProfile imsProfile) {
    }

    public void finishOmadmProvisioningUpdate() {
    }

    public int getDetailedDeRegiReason(int i) {
        if (i != 32) {
            return i != 33 ? 42 : 81;
        }
        return 71;
    }

    public int getP2pListSize(int i) {
        return 0;
    }

    public void handlePcscfRestoration(List<String> list) {
    }

    public boolean hasNetworkFailure() {
        return false;
    }

    public boolean isDelayedDeregisterTimerRunning() {
        return false;
    }

    public boolean isDeregisterOnLocationUpdate() {
        return false;
    }

    public boolean isIPSecAllow() {
        return true;
    }

    public boolean isMobilePreferredForRcs() {
        return false;
    }

    public boolean isNeedDelayedDeregister() {
        return false;
    }

    public boolean isOmadmConfigAvailable() {
        return true;
    }

    public boolean isReadyToDualRegister(boolean z) {
        return true;
    }

    public boolean isThrottledforImsNotAvailable() {
        return false;
    }

    public boolean needImsNotAvailable() {
        return false;
    }

    public boolean needPendingPdnConnected() {
        return false;
    }

    public void notifyLocationTimeout() {
    }

    public void notifyReattachToRil() {
    }

    public void notifyVoLteOnOffToRil(boolean z) {
    }

    public void onConfigUpdated() {
    }

    public void onContactActivated() {
    }

    public void onDeregistrationDone(boolean z) {
    }

    public void onEpdgDisconnected() {
    }

    public void onLocalIpChanged() {
    }

    public void onLocationCacheExpiry() {
    }

    public void onLteDataNetworkModeSettingChanged(boolean z) {
    }

    public void onPackageDataCleared(Uri uri) {
    }

    public void onPdnConnecting(int i) {
    }

    public void onPublishError(SipError sipError) {
    }

    public void onRegEventContactUriNotification(List<ImsUri> list, int i, String str, String str2) {
    }

    public void onServiceStateDataChanged(boolean z, boolean z2) {
    }

    public void onSrvccComplete() {
    }

    public void onSubscribeError(int i, SipError sipError) {
    }

    public void onTelephonyCallStatusChanged(int i) {
    }

    public boolean onUpdateGeolocation(LocationInfo locationInfo) {
        return false;
    }

    public boolean onUpdatedPcoInfo(int i, int i2) {
        return false;
    }

    public void onVolteSettingChanged() {
    }

    public void onWfcProfileChanged(byte[] bArr) {
    }

    public void registerAllowedNetworkTypesListener() {
    }

    public void requestLocation(int i) {
    }

    public void resetAllPcscfChecked() {
    }

    public void resetAllRetryFlow() {
    }

    public void resetIPSecAllow() {
    }

    public void resetPcscfPreference() {
    }

    public void retryDNSQuery() {
    }

    public void runDelayedDeregister() {
    }

    public void setNeedDelayedDeregister(boolean z) {
    }

    public void startOmadmProvisioningUpdate() {
    }

    public void startTimsTimer(String str) {
    }

    public void stopTimsTimer(String str) {
    }

    public void unRegisterIntentReceiver() {
    }

    public enum PcoType {
        PCO_DEFAULT(-2),
        PCO_AWAITING(-1),
        PCO_POSTPAY(0),
        PCO_RESTRICTED_ACCESS(2),
        PCO_ZERO_BALANCE(3),
        PCO_RATE_THROTTLING(4),
        PCO_SELF_ACTIVATION(5);
        
        private int mType;

        private PcoType(int i) {
            this.mType = i;
        }

        public static PcoType fromType(int i) {
            for (PcoType pcoType : values()) {
                if (pcoType.mType == i) {
                    return pcoType;
                }
            }
            return PCO_DEFAULT;
        }
    }

    public void resetPdnFailureInfo() {
        this.mPdnRejectCounter = 0;
        this.mHasPdnFailure = false;
    }

    public boolean hasPdnFailure() {
        return this.mHasPdnFailure;
    }

    public boolean isNonVoLteSimByPdnFail() {
        return this.mNonVoLTESimByPdnFail;
    }

    public void makeThrottle() {
        this.mIsPermanentStopped = true;
    }

    public void throttleforImsNotAvailable() {
        this.mThrottledforImsNotAvailable = true;
    }

    public void resetPermanentFailure() {
        this.mDiscardCurrentNetwork = false;
    }

    public void resetPcscfList() {
        this.mIsValid = false;
    }

    public void setPcoType(PcoType pcoType) {
        this.mPcoType = pcoType;
    }

    public void resetPcoType() {
        this.mPcoType = PcoType.PCO_POSTPAY;
    }

    public void addDelay(long j) {
        addDelay(j, 0);
    }

    public void addDelay(long j, int i) {
        this.mRegiAt = SystemClock.elapsedRealtime() + j;
        this.mThrottleReason = i;
    }

    public void setRetryTimeOnPdnFail(long j) {
        this.mPdnFailRetryTime = j;
    }

    /* access modifiers changed from: protected */
    public void setCallStatus(int i) {
        this.mCallStatus = i;
    }

    public boolean isThrottled() {
        return getThrottleState() != IRegistrationGovernor.ThrottleState.IDLE;
    }

    public IRegistrationGovernor.ThrottleState getThrottleState() {
        IRegistrationGovernor.ThrottleState throttleState = IRegistrationGovernor.ThrottleState.IDLE;
        if (this.mIsPermanentStopped) {
            return IRegistrationGovernor.ThrottleState.PERMANENTLY_STOPPED;
        }
        return this.mRegiAt > SystemClock.elapsedRealtime() ? IRegistrationGovernor.ThrottleState.TEMPORARY_THROTTLED : throttleState;
    }

    public boolean hasValidPcscfIpList() {
        return this.mIsValid;
    }

    public int getNextImpuType() {
        return this.mCurImpu;
    }

    public int getFailureCount() {
        return this.mFailureCounter;
    }

    public boolean isExistRetryTimer() {
        return this.mRetryTimeout != null;
    }

    public boolean isPse911Prohibited() {
        return this.mPse911Prohibited;
    }

    public PcoType getPcoType() {
        return this.mPcoType;
    }

    public int getWFCSubscribeForbiddenCounter() {
        return this.mWFCSubscribeForbiddenCounter;
    }

    public int getPcscfOrdinal() {
        return this.mCurPcscfIpIdx;
    }

    public long getRetryTimeOnPdnFail() {
        return this.mPdnFailRetryTime;
    }

    /* access modifiers changed from: protected */
    public int getCallStatus() {
        return this.mCallStatus;
    }

    public void onPdnConnected() {
        if (this.mRegiAt > SystemClock.elapsedRealtime() && this.mThrottleReason == 1) {
            this.mRegiAt = 0;
            this.mThrottleReason = 0;
        }
    }

    public boolean isReadyToGetReattach() {
        return this.mIsReadyToGetReattach;
    }
}
