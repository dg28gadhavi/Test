package com.sec.internal.interfaces.ims.core;

import android.os.Message;
import android.util.Log;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.ims.core.RegisterTask;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRegistrationManager extends ISequentialInitializable {
    public static final int ID_SIM2_OFFSET = 5000;
    public static final String KEY_INSTANCE_ID = "instanceId";
    public static final String LOG_TAG = "RegiMgr";

    static int getRegistrationInfoId(int i, int i2) {
        return i + (i2 * 5000);
    }

    void addPendingUpdateRegistration(IRegisterTask iRegisterTask, int i);

    void blockVoWifiRegisterOnRoaminByCsfbError(int i, int i2);

    void bootCompleted();

    void cancelUpdateSipDelegateRegistration(int i);

    void deregister(IRegisterTask iRegisterTask, boolean z, boolean z2, int i, String str);

    void deregister(IRegisterTask iRegisterTask, boolean z, boolean z2, String str);

    void deregisterProfile(int i, int i2);

    void deregisterProfile(int i, int i2, boolean z);

    void deregisterProfile(List<Integer> list, boolean z, int i);

    void doPendingUpdateRegistration();

    int findBestNetwork(int i, ImsProfile imsProfile, IRegistrationGovernor iRegistrationGovernor);

    void forceNotifyToApp(int i);

    int getCmcLineSlotIndex();

    boolean getCsfbSupported(int i);

    int getCurrentNetwork(int i);

    int getCurrentNetworkByPhoneId(int i);

    String[] getCurrentPcscf(int i);

    IRegistrationGovernor getEmergencyGovernor(int i);

    String getImpi(ImsProfile imsProfile, int i);

    ImsProfile getImsProfile(int i, ImsProfile.PROFILE_TYPE profile_type);

    String getImsiByUserAgent(IUserAgent iUserAgent);

    String getImsiByUserAgentHandle(int i);

    NetworkEvent getNetworkEvent(int i);

    List<IRegisterTask> getPendingRegistration(int i);

    ImsProfile[] getProfileList(int i);

    IRegistrationGovernor getRegistrationGovernor(int i);

    IRegistrationGovernor getRegistrationGovernorByProfileId(int i, int i2);

    ImsRegistration getRegistrationInfo(int i);

    ImsRegistration[] getRegistrationInfo();

    ImsRegistration[] getRegistrationInfoByPhoneId(int i);

    Map<Integer, ImsRegistration> getRegistrationList();

    Set<String> getServiceForNetwork(ImsProfile imsProfile, int i, boolean z, int i2);

    int getTelephonyCallStatus(int i);

    IUserAgent getUserAgent(int i);

    IUserAgent getUserAgent(String str);

    IUserAgent getUserAgent(String str, int i);

    IUserAgent getUserAgentByImsi(String str, String str2);

    IUserAgent[] getUserAgentByPhoneId(int i, String str);

    IUserAgent getUserAgentByRegId(int i);

    IUserAgent getUserAgentOnPdn(int i, int i2);

    void handleE911RegiTimeOut(IRegisterTask iRegisterTask);

    void handleInactiveCiaOnMobileConnected(int i);

    void handleInactiveCiaOnMobileDisconnected(int i);

    boolean hasOmaDmFinished(int i);

    int isCmcRegistered(int i);

    boolean isEmergencyCallProhibited(int i);

    boolean isEpdnRequestPending(int i);

    boolean isInvite403DisabledService(int i);

    boolean isPdnConnected(ImsProfile imsProfile, int i);

    boolean isRcsRegistered(int i);

    boolean isSelfActivationRequired(int i);

    boolean isSupportVoWiFiDisable5GSA(int i);

    boolean isSupportVoWiFiDisable5GSAFromConfiguration(int i);

    boolean isSuspended(int i);

    boolean isVoWiFiSupported(int i);

    void moveNextPcscf(int i, Message message);

    void notifyRCSAllowedChangedbyMDM();

    void notifyRomaingSettingsChanged(int i, int i2);

    void onDmConfigurationComplete(int i);

    void onUpdateSipDelegateRegistrationTimeOut(int i);

    void refreshAuEmergencyProfile(int i);

    void registerCmcRegiListener(IImsRegistrationListener iImsRegistrationListener, int i);

    void registerListener(IImsRegistrationListener iImsRegistrationListener, int i);

    void registerListener(IImsRegistrationListener iImsRegistrationListener, boolean z, int i);

    void registerP2pListener(IImsRegistrationListener iImsRegistrationListener);

    int registerProfile(ImsProfile imsProfile, int i);

    void releaseThrottleByAcs(int i);

    void releaseThrottleByCmc(IRegisterTask iRegisterTask);

    void removeE911RegiTimer();

    void requestFullNetworkRegistration(int i, int i2, String str);

    void requestTryRegister(int i);

    void requestTryRegsiter(int i, long j);

    void requestUpdateSipDelegateRegistration(int i);

    void sendDeregister(int i);

    void sendDeregister(int i, int i2);

    void sendDeregister(IRegisterTask iRegisterTask, long j);

    void sendDummyDnsQuery();

    void sendReRegister(int i, int i2);

    void setInvite403DisableService(boolean z, int i);

    void setOwnCapabilities(int i, Capabilities capabilities);

    void setRegiConfig(int i);

    void setRttMode(int i, boolean z);

    void setSSACPolicy(int i, boolean z);

    void setSilentLogEnabled(boolean z);

    void setThirdPartyFeatureTags(String[] strArr);

    void setTtyMode(int i, int i2);

    void startEmergencyRegistration(int i, Message message);

    void startEmergencyRegistration(int i, Message message, int i2);

    void stopEmergencyPdnOnly(int i);

    void stopEmergencyRegistration(int i);

    void stopPdnConnectivity(int i, IRegisterTask iRegisterTask);

    void suspendRegister(boolean z, int i);

    void suspended(RegisterTask registerTask, boolean z, int i);

    void unregisterCmcRegiListener(IImsRegistrationListener iImsRegistrationListener, int i);

    void unregisterListener(IImsRegistrationListener iImsRegistrationListener, int i);

    void updateChatService(int i, int i2);

    void updateEmcAttachAuth(int i, int i2);

    void updateEmergencyTaskByAuthFailure(int i);

    void updateEpsFbInImsCall(int i);

    void updateNrPreferredMode(int i, boolean z);

    void updateNrSaMode(int i, boolean z);

    void updatePcoInfo(int i, int i2, int i3);

    void updateRegistrationBySSAC(int i, boolean z);

    void updateTelephonyCallStatus(int i, int i2);

    void updateVo5gIconStatus(int i, int i2);

    static int getDeregistrationTimeout(ImsProfile imsProfile, int i) {
        int deregTimeout = imsProfile.getDeregTimeout(i);
        if (deregTimeout >= 1000) {
            return deregTimeout;
        }
        Log.e(LOG_TAG, "Under 1000 Deregi Timer : " + deregTimeout);
        return ImsCallUtil.DEFAULT_LOCATION_ACQUIRE_TIME;
    }
}
