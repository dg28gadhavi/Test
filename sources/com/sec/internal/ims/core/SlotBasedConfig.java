package com.sec.internal.ims.core;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.text.TextUtils;
import android.util.ArraySet;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ISimMobilityStatusListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SlotBasedConfig {
    private static final HashMap<Integer, SlotBasedConfig> sInstances = new HashMap<>();
    private String mBlockedServicesForCrossSim;
    private boolean mCdmaAvailableForVoice;
    private RemoteCallbackList<IImsRegistrationListener> mCmcRegistrationListeners;
    private boolean mDataUsageExceeded;
    private int mEmcAttachAuth;
    private int mHoEnable;
    private ImsIconManager mIconManager;
    private boolean mInviteRejected;
    private NetworkEvent mNetworkEvent;
    private boolean mNotifiedImsNotAvailable;
    private int mNrPreferredMode;
    private int mNrSaMode;
    private RegistrationManager.OmadmConfigState mOmadmState;
    private int mOnlyEpsFallback;
    private List<ImsProfile> mProfileList = new CopyOnWriteArrayList();
    private Map<Integer, ImsProfile> mProfileListExt = new ConcurrentHashMap();
    private boolean mRTTMode;
    private RegistrationConstants.RegistrationType mRcsVolteSingleRegistration = RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG;
    private Map<Integer, ImsRegistration> mRegistrationList = new ConcurrentHashMap();
    private RemoteCallbackList<IImsRegistrationListener> mRegistrationListeners;
    private RegisterTaskList mRegistrationTasks = new RegisterTaskList();
    private boolean mSSACPolicy;
    private boolean mSimMobilityActivation;
    private boolean mSimMobilityActivationForRcs;
    private RemoteCallbackList<ISimMobilityStatusListener> mSimMobilityStatusListeners = new RemoteCallbackList<>();
    private boolean mSuspendRegiWhileIrat;
    private boolean mTTYMode;
    private boolean mUnprocessedOmadmConfig;

    private SlotBasedConfig() {
        clear();
    }

    public static SlotBasedConfig getInstance(int i) {
        HashMap<Integer, SlotBasedConfig> hashMap = sInstances;
        synchronized (hashMap) {
            if (hashMap.containsKey(Integer.valueOf(i))) {
                SlotBasedConfig slotBasedConfig = hashMap.get(Integer.valueOf(i));
                return slotBasedConfig;
            }
            SlotBasedConfig slotBasedConfig2 = new SlotBasedConfig();
            hashMap.put(Integer.valueOf(i), slotBasedConfig2);
            return slotBasedConfig2;
        }
    }

    public void clear() {
        ImsIconManager imsIconManager = this.mIconManager;
        if (imsIconManager != null) {
            imsIconManager.updateRegistrationIcon();
        }
        this.mProfileList.clear();
        this.mProfileListExt.clear();
        this.mRegistrationList.clear();
        this.mRegistrationTasks.clear();
        this.mRcsVolteSingleRegistration = RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG;
        this.mTTYMode = false;
        this.mRTTMode = false;
        this.mInviteRejected = false;
        this.mCdmaAvailableForVoice = false;
        this.mSimMobilityActivation = false;
        this.mSimMobilityActivationForRcs = false;
        this.mSSACPolicy = true;
        this.mSuspendRegiWhileIrat = false;
        this.mDataUsageExceeded = false;
        this.mNotifiedImsNotAvailable = false;
        this.mOmadmState = RegistrationManager.OmadmConfigState.IDLE;
        this.mUnprocessedOmadmConfig = false;
        this.mHoEnable = -1;
        this.mOnlyEpsFallback = -1;
        this.mNrPreferredMode = -1;
        this.mNrSaMode = -1;
        this.mEmcAttachAuth = 0;
        this.mNetworkEvent = new NetworkEvent();
        this.mBlockedServicesForCrossSim = "";
    }

    /* access modifiers changed from: package-private */
    public void setIconManager(ImsIconManager imsIconManager) {
        this.mIconManager = imsIconManager;
    }

    public ImsIconManager getIconManager() {
        return this.mIconManager;
    }

    /* access modifiers changed from: package-private */
    public void createIconManager(Context context, IRegistrationManager iRegistrationManager, PdnController pdnController, Mno mno, int i) {
        this.mIconManager = new ImsIconManager(context, iRegistrationManager, pdnController, mno, i);
    }

    /* access modifiers changed from: package-private */
    public void createIconManager(ImsIconManager imsIconManager) {
        this.mIconManager = imsIconManager;
    }

    /* access modifiers changed from: package-private */
    public void clearProfiles() {
        this.mProfileList.clear();
    }

    public List<ImsProfile> getProfiles() {
        return new ArrayList(this.mProfileList);
    }

    /* access modifiers changed from: package-private */
    public void addProfile(ImsProfile imsProfile) {
        this.mProfileList.add(imsProfile);
    }

    /* access modifiers changed from: package-private */
    public Map<Integer, ImsProfile> getExtendedProfiles() {
        return this.mProfileListExt;
    }

    /* access modifiers changed from: package-private */
    public void addExtendedProfile(int i, ImsProfile imsProfile) {
        this.mProfileListExt.put(Integer.valueOf(i), imsProfile);
    }

    /* access modifiers changed from: package-private */
    public void removeExtendedProfile(int i) {
        this.mProfileListExt.remove(Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void setBlockedServicesForCrossSim(String str) {
        this.mBlockedServicesForCrossSim = str;
    }

    public Set<String> getBlockedServicesForCrossSim() {
        ArraySet arraySet = new ArraySet();
        if (!TextUtils.isEmpty(this.mBlockedServicesForCrossSim)) {
            arraySet.addAll(Arrays.asList(this.mBlockedServicesForCrossSim.split(",", 0)));
        }
        return arraySet;
    }

    public Map<Integer, ImsRegistration> getImsRegistrations() {
        return this.mRegistrationList;
    }

    public void addImsRegistration(int i, ImsRegistration imsRegistration) {
        this.mRegistrationList.put(Integer.valueOf(i), imsRegistration);
    }

    /* access modifiers changed from: package-private */
    public RemoteCallbackList<IImsRegistrationListener> getImsRegistrationListeners() {
        return this.mRegistrationListeners;
    }

    /* access modifiers changed from: package-private */
    public void setImsRegistrationListeners(RemoteCallbackList<IImsRegistrationListener> remoteCallbackList) {
        this.mRegistrationListeners = remoteCallbackList;
    }

    /* access modifiers changed from: package-private */
    public RemoteCallbackList<IImsRegistrationListener> getCmcRegistrationListeners() {
        return this.mCmcRegistrationListeners;
    }

    /* access modifiers changed from: package-private */
    public void setCmcRegistrationListeners(RemoteCallbackList<IImsRegistrationListener> remoteCallbackList) {
        this.mCmcRegistrationListeners = remoteCallbackList;
    }

    /* access modifiers changed from: package-private */
    public RemoteCallbackList<ISimMobilityStatusListener> getSimMobilityStatusListeners() {
        return this.mSimMobilityStatusListeners;
    }

    public NetworkEvent getNetworkEvent() {
        return this.mNetworkEvent;
    }

    /* access modifiers changed from: package-private */
    public void setNetworkEvent(NetworkEvent networkEvent) {
        this.mNetworkEvent = networkEvent;
    }

    /* access modifiers changed from: package-private */
    public boolean getTTYMode() {
        return this.mTTYMode;
    }

    /* access modifiers changed from: package-private */
    public void setTTYMode(Boolean bool) {
        this.mTTYMode = bool.booleanValue();
    }

    public boolean getRTTMode() {
        return this.mRTTMode;
    }

    public void setRTTMode(Boolean bool) {
        this.mRTTMode = bool.booleanValue();
    }

    /* access modifiers changed from: package-private */
    public boolean isInviteRejected() {
        return this.mInviteRejected;
    }

    /* access modifiers changed from: package-private */
    public void setInviteReject(boolean z) {
        this.mInviteRejected = z;
    }

    /* access modifiers changed from: package-private */
    public boolean isCdmaAvailableForVoice() {
        return this.mCdmaAvailableForVoice;
    }

    /* access modifiers changed from: package-private */
    public void setCdmaAvailableForVoice(boolean z) {
        this.mCdmaAvailableForVoice = z;
    }

    public RegisterTaskList getRegistrationTasks() {
        return this.mRegistrationTasks;
    }

    /* access modifiers changed from: package-private */
    public void setRegistrationTasks(RegisterTaskList registerTaskList) {
        this.mRegistrationTasks = registerTaskList;
    }

    public void activeSimMobility(boolean z) {
        this.mSimMobilityActivation = z;
    }

    public boolean isSimMobilityActivated() {
        return this.mSimMobilityActivation;
    }

    public void activeSimMobilityForRcs(boolean z) {
        this.mSimMobilityActivationForRcs = z;
    }

    public boolean isSimMobilityActivatedForRcs() {
        return this.mSimMobilityActivationForRcs;
    }

    /* access modifiers changed from: package-private */
    public boolean isSsacEnabled() {
        return this.mSSACPolicy;
    }

    /* access modifiers changed from: package-private */
    public void enableSsac(boolean z) {
        this.mSSACPolicy = z;
    }

    /* access modifiers changed from: package-private */
    public boolean isSuspendedWhileIrat() {
        return this.mSuspendRegiWhileIrat;
    }

    /* access modifiers changed from: package-private */
    public void setSuspendWhileIrat(boolean z) {
        this.mSuspendRegiWhileIrat = z;
    }

    /* access modifiers changed from: package-private */
    public boolean isDataUsageExceeded() {
        return this.mDataUsageExceeded;
    }

    /* access modifiers changed from: package-private */
    public void setDataUsageExceed(boolean z) {
        this.mDataUsageExceeded = z;
    }

    public boolean isNotifiedImsNotAvailable() {
        return this.mNotifiedImsNotAvailable;
    }

    public void setNotifiedImsNotAvailable(boolean z) {
        this.mNotifiedImsNotAvailable = z;
    }

    public boolean hasOmaDmFinished() {
        return this.mOmadmState == RegistrationManager.OmadmConfigState.FINISHED;
    }

    public RegistrationManager.OmadmConfigState getOmadmState() {
        return this.mOmadmState;
    }

    public void setOmadmState(RegistrationManager.OmadmConfigState omadmConfigState) {
        this.mOmadmState = omadmConfigState;
    }

    public void setUnprocessedOmadmConfig(boolean z) {
        this.mUnprocessedOmadmConfig = z;
    }

    public boolean getUnprocessedOmadmConfig() {
        return this.mUnprocessedOmadmConfig;
    }

    /* access modifiers changed from: package-private */
    public RegistrationConstants.RegistrationType getRcsVolteSingleRegistration() {
        return this.mRcsVolteSingleRegistration;
    }

    /* access modifiers changed from: package-private */
    public void setRcsVolteSingleRegistration(RegistrationConstants.RegistrationType registrationType) {
        this.mRcsVolteSingleRegistration = registrationType;
    }

    /* access modifiers changed from: package-private */
    public int getHoEnable() {
        return this.mHoEnable;
    }

    /* access modifiers changed from: package-private */
    public void setHoEnable(boolean z) {
        this.mHoEnable = z ? 1 : 0;
    }

    /* access modifiers changed from: package-private */
    public int getEmcAttachAuth() {
        return this.mEmcAttachAuth;
    }

    /* access modifiers changed from: package-private */
    public void setEmcAttachAuth(int i) {
        this.mEmcAttachAuth = i;
    }

    /* access modifiers changed from: package-private */
    public int getOnlyEpsFallback() {
        return this.mOnlyEpsFallback;
    }

    /* access modifiers changed from: package-private */
    public void setOnlyEpsFallback(boolean z) {
        this.mOnlyEpsFallback = z ? 1 : 0;
    }

    /* access modifiers changed from: package-private */
    public int getNrPreferredMode() {
        return this.mNrPreferredMode;
    }

    /* access modifiers changed from: package-private */
    public void setNrPreferredMode(boolean z) {
        this.mNrPreferredMode = z ? 1 : 0;
    }

    /* access modifiers changed from: package-private */
    public int getNrSaMode() {
        return this.mNrSaMode;
    }

    /* access modifiers changed from: package-private */
    public void setNrSaMode(boolean z) {
        this.mNrSaMode = z ? 1 : 0;
    }

    public static class RegisterTaskList extends CopyOnWriteArrayList<RegisterTask> {
        public boolean remove(Object obj) {
            ((RegisterTask) obj).getGovernor().unRegisterIntentReceiver();
            return super.remove(obj);
        }

        public void clear() {
            try {
                Iterator it = iterator();
                while (it.hasNext()) {
                    ((RegisterTask) it.next()).getGovernor().unRegisterIntentReceiver();
                }
            } catch (NullPointerException unused) {
            }
            super.clear();
        }

        public boolean removeAll(Collection<?> collection) {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                ((RegisterTask) it.next()).getGovernor().unRegisterIntentReceiver();
            }
            return super.removeAll(collection);
        }
    }
}
