package com.sec.internal.interfaces.ims.core;

import android.net.Uri;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.ims.core.RegistrationGovernor;
import java.util.List;
import java.util.Set;

public interface IRegistrationGovernor {
    public static final int EVENT_PRESENCE = 1;
    public static final int EVENT_REGISTRATION = 0;

    public enum CallEvent {
        EVENT_CALL_UNKNOWN,
        EVENT_CALL_ESTABLISHED,
        EVENT_CALL_LAST_CALL_END,
        EVENT_CALL_ALT_SERVICE_INITIAL_REGI,
        EVENT_CALL_ALT_SERVICE_EMERGENCY_REGI,
        EVENT_CALL_ALT_SERVICE_EMERGENCY
    }

    public enum ThrottleState {
        IDLE,
        TEMPORARY_THROTTLED,
        PERMANENTLY_STOPPED
    }

    void addDelay(long j);

    void addDelay(long j, int i);

    boolean allowRoaming();

    Set<String> applyCrossSimPolicy(Set<String> set, int i);

    Set<String> applyDataSimPolicyForCrossSim(Set<String> set, int i);

    Set<String> applyPsDataOffExempt(Set<String> set, int i);

    boolean blockImmediatelyUpdateRegi();

    void checkAcsPcscfListChange();

    boolean checkEmergencyInProgress();

    void checkProfileUpdateFromDM(boolean z);

    void checkUnProcessedVoLTEState();

    List<String> checkValidPcscfIp(List<String> list);

    List<String> checkValidPcscfIpForPcscfRestoration(List<String> list);

    void clear();

    boolean determineDeRegistration(int i, int i2);

    void enableRcsOverIms(ImsProfile imsProfile);

    Set<String> filterService(Set<String> set, int i);

    void finishOmadmProvisioningUpdate();

    String getCurrentPcscfIp();

    int getDetailedDeRegiReason(int i);

    int getFailureCount();

    int getFailureType();

    String getMatchedPdnFailReasonFromGlobalSettings(PdnFailReason pdnFailReason);

    int getNextImpuType();

    long getNextRetryMillis();

    int getNumOfEmerPcscfIp();

    int getNumOfPcscfIp();

    int getP2pListSize(int i);

    RegistrationGovernor.PcoType getPcoType();

    int getPcscfOrdinal();

    long getRetryTimeOnPdnFail();

    RegistrationConstants.RegisterTaskState getState();

    ThrottleState getThrottleState();

    String getUpdateRegiPendingReason(int i, NetworkEvent networkEvent, boolean z, boolean z2);

    int getWFCSubscribeForbiddenCounter();

    void handlePcscfRestoration(List<String> list);

    boolean hasEmergencyTaskInPriority(List<? extends IRegisterTask> list);

    boolean hasNetworkFailure();

    boolean hasPdnFailure();

    boolean hasValidPcscfIpList();

    void increasePcscfIdx();

    boolean isDelayedDeregisterTimerRunning();

    boolean isDeregisterOnLocationUpdate();

    boolean isExistRetryTimer();

    boolean isIPSecAllow();

    boolean isLocationInfoLoaded(int i);

    boolean isMatchedPdnFailReason(PdnFailReason pdnFailReason);

    boolean isMobilePreferredForRcs();

    boolean isNeedDelayedDeregister();

    boolean isNoNextPcscf();

    boolean isNonVoLteSimByPdnFail();

    boolean isOmadmConfigAvailable();

    boolean isPse911Prohibited();

    boolean isReadyToDualRegister(boolean z);

    boolean isReadyToGetReattach();

    boolean isReadyToRegister(int i);

    boolean isSrvccCase();

    boolean isThrottled();

    boolean isThrottledforImsNotAvailable();

    void makeThrottle();

    boolean needImsNotAvailable();

    boolean needPendingPdnConnected();

    void notifyLocationTimeout();

    void notifyReattachToRil();

    void notifyVoLteOnOffToRil(boolean z);

    void onAdsChanged(int i);

    void onCallStatus(CallEvent callEvent, SipError sipError, int i);

    void onConfigUpdated();

    void onContactActivated();

    void onDeregistrationDone(boolean z);

    void onEpdgConnected() {
    }

    void onEpdgDisconnected();

    void onEpdgIkeError() {
    }

    void onLocalIpChanged();

    void onLocationCacheExpiry();

    void onLteDataNetworkModeSettingChanged(boolean z);

    IRegisterTask onManualDeregister(boolean z);

    void onPackageDataCleared(Uri uri);

    void onPdnConnected();

    void onPdnConnecting(int i);

    void onPdnRequestFailed(PdnFailReason pdnFailReason, int i);

    void onPublishError(SipError sipError);

    void onRegEventContactUriNotification(List<ImsUri> list, int i, String str, String str2);

    void onRegistrationDone();

    void onRegistrationError(SipError sipError, long j, boolean z);

    void onRegistrationTerminated(SipError sipError, long j, boolean z);

    void onServiceStateDataChanged(boolean z, boolean z2);

    SipError onSipError(String str, SipError sipError);

    void onSrvccComplete();

    void onSubscribeError(int i, SipError sipError);

    void onTelephonyCallStatusChanged(int i);

    void onTimsTimerExpired();

    boolean onUpdateGeolocation(LocationInfo locationInfo);

    boolean onUpdatedPcoInfo(int i, int i2);

    void onVolteSettingChanged();

    void onWfcProfileChanged(byte[] bArr);

    void registerAllowedNetworkTypesListener();

    void releaseThrottle(int i);

    void requestLocation(int i);

    void resetAllPcscfChecked();

    void resetAllRetryFlow();

    void resetIPSecAllow();

    void resetPcoType();

    void resetPcscfList();

    void resetPcscfPreference();

    void resetPdnFailureInfo();

    void resetPermanentFailure();

    void resetRetry();

    void retryDNSQuery();

    void runDelayedDeregister();

    void setNeedDelayedDeregister(boolean z);

    void setPcoType(RegistrationGovernor.PcoType pcoType);

    void setRetryTimeOnPdnFail(long j);

    void startOmadmProvisioningUpdate();

    void startTimsTimer(String str);

    void stopTimsTimer(String str);

    void throttleforImsNotAvailable();

    void unRegisterIntentReceiver();

    void updatePcscfIpList(List<String> list);
}
