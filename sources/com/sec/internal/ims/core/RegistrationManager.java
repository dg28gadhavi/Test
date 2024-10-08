package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ISimMobilityStatusListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.extensions.WiFiManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.google.SecImsServiceConnector;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.sim.SimManager;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.TimeBasedUuidGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.core.handler.IRegistrationInterface;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class RegistrationManager implements IRegistrationManager {
    protected static final int ADHOC_ID_SIM2_OFFSET = 20000;
    protected static final int ADHOC_IMS_PROFILE_ID_BASE = 10000;
    protected static final int HANDOFF_EVENT_TIMER = 300;
    protected static final int ID_SIM2_OFFSET = 5000;
    protected static final int MAX_RECOVERY_ACTION_COUNT = 7;
    protected boolean mAresLookupRequired = true;
    protected SparseArray<ImsProfile> mAuEmergencyProfile;
    protected int mCallState = 0;
    protected ICmcAccountManager mCmcAccountManager;
    protected IConfigModule mConfigModule;
    protected Context mContext;
    protected int mEmmCause;
    protected SimpleEventLog mEventLog;
    protected IGeolocationController mGeolocationCon = null;
    protected RegistrationManagerHandler mHandler;
    protected Message mHasSilentE911 = null;
    protected IImsFramework mImsFramework;
    protected boolean mIsNonADSDeRegRequired = false;
    protected boolean mIsVolteAllowedWithDsac = true;
    protected boolean mMoveNextPcscf = false;
    protected NetworkEventController mNetEvtCtr;
    protected PdnController mPdnController;
    protected int mPhoneIdForSilentE911 = -1;
    protected IRcsPolicyManager mRcsPolicyManager;
    protected IRegistrationInterface mRegStackIf;
    protected int mRegiRetryLimit = 0;
    private IImsRegistrationListener mRegisterP2pListener = null;
    protected SecImsServiceConnector mSecImsServiceConnector;
    protected List<ISimManager> mSimManagers;
    protected ITelephonyManager mTelephonyManager;
    protected List<String> mThirdPartyFeatureTags = null;
    protected UserEventController mUserEvtCtr;
    protected IVolteServiceModule mVsm;
    protected int mlegacyPhoneCount = 0;

    public enum OmadmConfigState {
        IDLE,
        TRIGGERED,
        FINISHED
    }

    public void setVolteServiceModule(IVolteServiceModule iVolteServiceModule) {
        this.mVsm = iVolteServiceModule;
        this.mNetEvtCtr.setVolteServiceModule(iVolteServiceModule);
        this.mUserEvtCtr.setVolteServiceModule(iVolteServiceModule);
    }

    public void setConfigModule(IConfigModule iConfigModule) {
        this.mConfigModule = iConfigModule;
        this.mHandler.setConfigModule(iConfigModule);
        this.mUserEvtCtr.setConfigModule(iConfigModule);
    }

    public void setStackInterface(IRegistrationInterface iRegistrationInterface) {
        this.mRegStackIf = iRegistrationInterface;
        iRegistrationInterface.setEventLog(this.mEventLog);
        this.mRegStackIf.setRegistrationHandler(this.mHandler);
        this.mRegStackIf.setSimManagers(this.mSimManagers);
        this.mRegStackIf.setPdnController(this.mPdnController);
    }

    public void setGeolocationController(GeolocationController geolocationController) {
        this.mGeolocationCon = geolocationController;
    }

    /* access modifiers changed from: package-private */
    public RegistrationManagerHandler getRegistrationManagerHandler() {
        return this.mHandler;
    }

    /* access modifiers changed from: protected */
    public ImsIconManager getImsIconManager(int i) {
        ImsIconManager iconManager = SlotBasedConfig.getInstance(i).getIconManager();
        if (iconManager == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "getImsIconManager is not exist.");
        }
        return iconManager;
    }

    public ISimManager getSimManager(int i) {
        try {
            return this.mSimManagers.get(i);
        } catch (IndexOutOfBoundsException e) {
            IMSLog.e(IRegistrationManager.LOG_TAG, i, "getSimManager: " + e.toString());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public SimpleEventLog getEventLog() {
        return this.mEventLog;
    }

    public OmadmConfigState getOmadmState(int i) {
        return SlotBasedConfig.getInstance(i).getOmadmState();
    }

    public boolean hasOmaDmFinished(int i) {
        return SlotBasedConfig.getInstance(i).hasOmaDmFinished();
    }

    /* access modifiers changed from: package-private */
    public boolean getAresLookupRequired() {
        return this.mAresLookupRequired;
    }

    /* access modifiers changed from: package-private */
    public int getEmmCause() {
        return this.mEmmCause;
    }

    public boolean isInvite403DisabledService(int i) {
        return SlotBasedConfig.getInstance(i).isInviteRejected();
    }

    /* access modifiers changed from: package-private */
    public boolean isAdhocProfile(ImsProfile imsProfile) {
        return imsProfile.getId() >= 10000;
    }

    /* access modifiers changed from: package-private */
    public boolean getVolteAllowedWithDsac() {
        return this.mIsVolteAllowedWithDsac;
    }

    /* access modifiers changed from: protected */
    public boolean isCdmaAvailableForVoice(int i) {
        return SlotBasedConfig.getInstance(i).isCdmaAvailableForVoice();
    }

    /* access modifiers changed from: package-private */
    public void resetNotifiedImsNotAvailable(int i) {
        SlotBasedConfig.getInstance(i).setNotifiedImsNotAvailable(false);
    }

    /* access modifiers changed from: package-private */
    public void setOmadmState(int i, OmadmConfigState omadmConfigState) {
        SlotBasedConfig.getInstance(i).setOmadmState(omadmConfigState);
    }

    /* access modifiers changed from: package-private */
    public boolean getUnprocessedOmadmConfig(int i) {
        return SlotBasedConfig.getInstance(i).getUnprocessedOmadmConfig();
    }

    /* access modifiers changed from: package-private */
    public void setUnprocessedOmadmConfig(int i, boolean z) {
        SlotBasedConfig.getInstance(i).setUnprocessedOmadmConfig(z);
    }

    /* access modifiers changed from: package-private */
    public void setAresLookupRequired(boolean z) {
        this.mAresLookupRequired = z;
    }

    /* access modifiers changed from: package-private */
    public void setEmmCause(int i) {
        this.mEmmCause = i;
    }

    public void setInvite403DisableService(boolean z, int i) {
        SlotBasedConfig.getInstance(i).setInviteReject(z);
    }

    /* access modifiers changed from: package-private */
    public void setVolteAllowedWithDsac(boolean z) {
        this.mIsVolteAllowedWithDsac = z;
    }

    /* access modifiers changed from: package-private */
    public void setCdmaAvailableForVoice(int i, boolean z) {
        SlotBasedConfig.getInstance(i).setCdmaAvailableForVoice(z);
    }

    /* access modifiers changed from: protected */
    public void setCallState(int i) {
        this.mCallState = i;
    }

    public void setNonADSDeRegRequired(boolean z) {
        this.mIsNonADSDeRegRequired = z;
    }

    public synchronized void registerListener(IImsRegistrationListener iImsRegistrationListener, int i) {
        registerListener(iImsRegistrationListener, true, i);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0061, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void registerListener(com.sec.ims.IImsRegistrationListener r3, boolean r4, int r5) {
        /*
            r2 = this;
            monitor-enter(r2)
            if (r3 != 0) goto L_0x000c
            java.lang.String r3 = "RegiMgr"
            java.lang.String r4 = "listener is null.."
            com.sec.internal.log.IMSLog.i(r3, r5, r4)     // Catch:{ all -> 0x0062 }
            monitor-exit(r2)
            return
        L_0x000c:
            com.sec.internal.ims.core.SlotBasedConfig r0 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r5)     // Catch:{ all -> 0x0062 }
            android.os.RemoteCallbackList r0 = r0.getImsRegistrationListeners()     // Catch:{ all -> 0x0062 }
            if (r0 != 0) goto L_0x0026
            android.os.RemoteCallbackList r0 = new android.os.RemoteCallbackList     // Catch:{ all -> 0x0062 }
            r0.<init>()     // Catch:{ all -> 0x0062 }
            r0.register(r3)     // Catch:{ all -> 0x0062 }
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r5)     // Catch:{ all -> 0x0062 }
            r1.setImsRegistrationListeners(r0)     // Catch:{ all -> 0x0062 }
            goto L_0x0029
        L_0x0026:
            r0.register(r3)     // Catch:{ all -> 0x0062 }
        L_0x0029:
            if (r4 == 0) goto L_0x0060
            com.sec.internal.ims.core.SlotBasedConfig r4 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r5)     // Catch:{ all -> 0x0062 }
            java.util.Map r4 = r4.getImsRegistrations()     // Catch:{ all -> 0x0062 }
            java.util.Collection r4 = r4.values()     // Catch:{ all -> 0x0062 }
            java.util.Iterator r4 = r4.iterator()     // Catch:{ all -> 0x0062 }
        L_0x003b:
            boolean r0 = r4.hasNext()     // Catch:{ all -> 0x0062 }
            if (r0 == 0) goto L_0x0060
            java.lang.Object r0 = r4.next()     // Catch:{ all -> 0x0062 }
            com.sec.ims.ImsRegistration r0 = (com.sec.ims.ImsRegistration) r0     // Catch:{ all -> 0x0062 }
            int r1 = r0.getPhoneId()     // Catch:{ RemoteException -> 0x005b }
            if (r1 != r5) goto L_0x003b
            com.sec.ims.settings.ImsProfile r1 = r0.getImsProfile()     // Catch:{ RemoteException -> 0x005b }
            boolean r1 = com.sec.internal.ims.core.RegistrationUtils.isCmcProfile(r1)     // Catch:{ RemoteException -> 0x005b }
            if (r1 != 0) goto L_0x003b
            r3.onRegistered(r0)     // Catch:{ RemoteException -> 0x005b }
            goto L_0x003b
        L_0x005b:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0062 }
            goto L_0x003b
        L_0x0060:
            monitor-exit(r2)
            return
        L_0x0062:
            r3 = move-exception
            monitor-exit(r2)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManager.registerListener(com.sec.ims.IImsRegistrationListener, boolean, int):void");
    }

    public synchronized void unregisterListener(IImsRegistrationListener iImsRegistrationListener, int i) {
        if (iImsRegistrationListener == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "listener is null..");
            return;
        }
        RemoteCallbackList<IImsRegistrationListener> imsRegistrationListeners = SlotBasedConfig.getInstance(i).getImsRegistrationListeners();
        if (imsRegistrationListeners != null) {
            imsRegistrationListeners.unregister(iImsRegistrationListener);
        }
    }

    public synchronized void registerP2pListener(IImsRegistrationListener iImsRegistrationListener) {
        this.mRegisterP2pListener = iImsRegistrationListener;
        Log.d(IRegistrationManager.LOG_TAG, "registerP2pListener done");
    }

    public IImsRegistrationListener getP2pListener() {
        return this.mRegisterP2pListener;
    }

    private void notifyImsP2pRegistration(boolean z, ImsRegistration imsRegistration, ImsRegistrationError imsRegistrationError, int i) {
        int cmcType = imsRegistration.getImsProfile().getCmcType();
        Log.d(IRegistrationManager.LOG_TAG, "notifyImsP2pRegistration(): " + cmcType);
        IImsRegistrationListener iImsRegistrationListener = this.mRegisterP2pListener;
        if (iImsRegistrationListener != null && cmcType >= 2) {
            if (z) {
                try {
                    iImsRegistrationListener.onRegistered(imsRegistration);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                this.mRegisterP2pListener.onDeregistered(imsRegistration, new ImsRegistrationError(imsRegistrationError.getSipErrorCode(), imsRegistrationError.getSipErrorReason(), imsRegistrationError.getDetailedDeregiReason(), i));
            }
        }
    }

    public void testNotifyImsRegistration(ImsRegistration imsRegistration, boolean z, RegisterTask registerTask, ImsRegistrationError imsRegistrationError) {
        notifyImsRegistration(imsRegistration, z, registerTask, imsRegistrationError);
    }

    /* access modifiers changed from: protected */
    public synchronized void notifyImsRegistration(ImsRegistration imsRegistration, boolean z, IRegisterTask iRegisterTask, ImsRegistrationError imsRegistrationError) {
        RemoteCallbackList<IImsRegistrationListener> imsRegistrationListeners;
        IMSLog.i(IRegistrationManager.LOG_TAG, imsRegistration.getPhoneId(), "notifyImsRegistration(): " + imsRegistration.getImsProfile());
        notifyImsP2pRegistration(z, imsRegistration, imsRegistrationError, iRegisterTask.getDeregiReason());
        notifyCmcRegistration(z, imsRegistration, imsRegistrationError);
        if (this.mImsFramework.getIilManager(iRegisterTask.getPhoneId()) != null) {
            this.mImsFramework.getIilManager(iRegisterTask.getPhoneId()).notifyImsRegistration(imsRegistration, z, imsRegistrationError);
        }
        if (!RegistrationUtils.isCmcProfile(imsRegistration.getImsProfile()) && (imsRegistrationListeners = SlotBasedConfig.getInstance(imsRegistration.getPhoneId()).getImsRegistrationListeners()) != null) {
            int beginBroadcast = imsRegistrationListeners.beginBroadcast();
            while (beginBroadcast > 0) {
                beginBroadcast--;
                if (z) {
                    try {
                        imsRegistrationListeners.getBroadcastItem(beginBroadcast).onRegistered(imsRegistration);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    imsRegistrationListeners.getBroadcastItem(beginBroadcast).onDeregistered(imsRegistration, imsRegistrationError);
                }
            }
            Log.i(IRegistrationManager.LOG_TAG, "notify mRegistrationList, finish");
            imsRegistrationListeners.finishBroadcast();
        }
        if (!RegistrationUtils.isCmcProfile(imsRegistration.getImsProfile())) {
            SecImsNotifier.getInstance().notifyImsRegistration(imsRegistration, z, imsRegistrationError);
        }
        this.mImsFramework.getServiceModuleManager().notifyImsRegistration(imsRegistration, z, imsRegistrationError.getSipErrorCode());
        this.mImsFramework.getImsDiagMonitor().handleRegistrationEvent(imsRegistration, z);
        boolean z2 = true;
        boolean z3 = z && imsRegistration.getEpdgStatus() && (imsRegistration.hasService("mmtel") || imsRegistration.hasService("mmtel-video"));
        IMSLog.i(IRegistrationManager.LOG_TAG, "notifyImsRegistration: isVoWiFiRegistered [" + z3 + "]");
        Context context = this.mContext;
        if (z3) {
            z2 = false;
        }
        WiFiManagerExt.setMaxDtimInSuspendMode(context, z2);
        Intent intent = new Intent(ImsConstants.Intents.ACTION_IMS_STATE);
        intent.putExtra(ImsConstants.Intents.EXTRA_REGISTERED, z).putExtra(ImsConstants.Intents.EXTRA_REGISTERED_SERVICES, imsRegistration.getServices().toString()).putExtra(ImsConstants.Intents.EXTRA_VOWIFI, imsRegistration.getEpdgStatus()).putExtra(ImsConstants.Intents.EXTRA_SIP_ERROR_CODE, imsRegistrationError.getSipErrorCode()).putExtra(ImsConstants.Intents.EXTRA_REGI_PHONE_ID, imsRegistration.getPhoneId()).putExtra(ImsConstants.Intents.EXTRA_SIP_ERROR_REASON, imsRegistrationError.getSipErrorReason());
        IntentUtil.sendBroadcast(this.mContext, intent);
    }

    public List<IRegisterTask> getPendingRegistration(int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            return new CopyOnWriteArrayList(pendingRegistrationInternal);
        }
        IMSLog.e(IRegistrationManager.LOG_TAG, "getPendingRegistration : no task return null");
        return null;
    }

    public ImsRegistration[] getRegistrationInfo() {
        RegisterTask registerTaskByRegHandle;
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            for (ImsRegistration next : SlotBasedConfig.getInstance(i).getImsRegistrations().values()) {
                if (!(next == null || (registerTaskByRegHandle = getRegisterTaskByRegHandle(next.getHandle())) == null || registerTaskByRegHandle.getState() != RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    arrayList.add(next);
                }
            }
        }
        return (ImsRegistration[]) arrayList.toArray(new ImsRegistration[0]);
    }

    public ImsRegistration getRegistrationInfoByServiceType(String str, int i) {
        RegisterTask registerTaskByRegHandle;
        for (ImsRegistration next : SlotBasedConfig.getInstance(i).getImsRegistrations().values()) {
            if (next != null && next.getPhoneId() == i && next.getImsProfile().getCmcType() == 0 && !next.getImsProfile().hasEmergencySupport() && (registerTaskByRegHandle = getRegisterTaskByRegHandle(next.getHandle())) != null && registerTaskByRegHandle.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && ImsUtil.isMatchedService(next.getServices(), str)) {
                if (!registerTaskByRegHandle.getMno().isKor()) {
                    return next;
                }
                Set services = next.getServices();
                if (getNetworkEvent(registerTaskByRegHandle.getPhoneId()) == null) {
                    return next;
                }
                for (String str2 : next.getServices()) {
                    if ("mmtel".equals(str2) && (!NetworkUtil.is3gppPsVoiceNetwork(getNetworkEvent(registerTaskByRegHandle.getPhoneId()).network) || getNetworkEvent(registerTaskByRegHandle.getPhoneId()).outOfService)) {
                        services.remove(str2);
                    }
                }
                return new ImsRegistration(next, services);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void notifySimMobilityStatusChanged(int i, ISimManager iSimManager) {
        boolean hasSimMobilityProfile = RegistrationUtils.hasSimMobilityProfile(i);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "notifySimMobilityStatusChanged: old[" + SlotBasedConfig.getInstance(i).isSimMobilityActivated() + "], new [" + hasSimMobilityProfile + "]");
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(ImsConstants.Uris.SETTINGS_PROVIDER_SIMMOBILITY_URI.toString(), i), (ContentObserver) null);
        int isSimMobilityFeatureEnabled = SimUtil.isSimMobilityFeatureEnabled();
        Mno simMno = iSimManager.getSimMno();
        if (!iSimManager.isLabSimCard() && simMno != Mno.GCF && CollectionUtils.isNullOrEmpty((Collection<?>) iSimManager.getNetworkNames())) {
            isSimMobilityFeatureEnabled = 2;
        }
        if (RegistrationUtils.hasSimMobilityProfile(i)) {
            isSimMobilityFeatureEnabled = 4;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 0);
        contentValues.put(DiagnosisConstants.DRPT_KEY_SIM_MOBILITY_ENABLED, Integer.valueOf(isSimMobilityFeatureEnabled));
        ImsLogAgentUtil.storeLogToAgent(i, this.mContext, "DRPT", contentValues);
        this.mEventLog.logAndAdd(i, "notifySimMobilityStatusChanged: " + isSimMobilityFeatureEnabled);
        IMSLog.c(LogClass.REGI_SIMMO_STATE_CHANGED, i + ",SIMMO:" + isSimMobilityFeatureEnabled);
        RemoteCallbackList<ISimMobilityStatusListener> simMobilityStatusListeners = SlotBasedConfig.getInstance(i).getSimMobilityStatusListeners();
        for (int beginBroadcast = simMobilityStatusListeners.beginBroadcast() + -1; beginBroadcast >= 0; beginBroadcast--) {
            try {
                simMobilityStatusListeners.getBroadcastItem(beginBroadcast).onSimMobilityStateChanged(hasSimMobilityProfile);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.i(IRegistrationManager.LOG_TAG, "notify SimMobilityStatusChanged, finish");
        simMobilityStatusListeners.finishBroadcast();
    }

    public synchronized void registerSimMobilityStatusListener(ISimMobilityStatusListener iSimMobilityStatusListener, int i) {
        registerSimMobilityStatusListener(iSimMobilityStatusListener, true, i);
    }

    public synchronized void registerSimMobilityStatusListener(ISimMobilityStatusListener iSimMobilityStatusListener, boolean z, int i) {
        if (iSimMobilityStatusListener == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "listener is null..");
            return;
        }
        SlotBasedConfig.getInstance(i).getSimMobilityStatusListeners().register(iSimMobilityStatusListener);
        if (z) {
            try {
                iSimMobilityStatusListener.onSimMobilityStateChanged(RegistrationUtils.hasSimMobilityProfile(i));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public synchronized void unregisterSimMobilityStatusListener(ISimMobilityStatusListener iSimMobilityStatusListener, int i) {
        SlotBasedConfig.getInstance(i).getSimMobilityStatusListeners().unregister(iSimMobilityStatusListener);
    }

    public synchronized void registerCmcRegiListener(IImsRegistrationListener iImsRegistrationListener, int i) {
        if (iImsRegistrationListener == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "registerCmcRegiListener: listener is null..");
            return;
        }
        RemoteCallbackList<IImsRegistrationListener> cmcRegistrationListeners = SlotBasedConfig.getInstance(i).getCmcRegistrationListeners();
        if (cmcRegistrationListeners == null) {
            RemoteCallbackList remoteCallbackList = new RemoteCallbackList();
            remoteCallbackList.register(iImsRegistrationListener);
            SlotBasedConfig.getInstance(i).setCmcRegistrationListeners(remoteCallbackList);
        } else {
            cmcRegistrationListeners.register(iImsRegistrationListener);
        }
        for (ImsRegistration next : SlotBasedConfig.getInstance(i).getImsRegistrations().values()) {
            try {
                if (next.getPhoneId() == i && RegistrationUtils.isCmcProfile(next.getImsProfile())) {
                    iImsRegistrationListener.onRegistered(next);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void unregisterCmcRegiListener(IImsRegistrationListener iImsRegistrationListener, int i) {
        RemoteCallbackList<IImsRegistrationListener> cmcRegistrationListeners = SlotBasedConfig.getInstance(i).getCmcRegistrationListeners();
        if (cmcRegistrationListeners != null) {
            cmcRegistrationListeners.unregister(iImsRegistrationListener);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyCmcRegistration(boolean z, ImsRegistration imsRegistration, ImsRegistrationError imsRegistrationError) {
        if (imsRegistration.getImsProfile().getCmcType() != 0) {
            Log.d(IRegistrationManager.LOG_TAG, "notifyCmcRegistration(): CmcType: " + imsRegistration.getImsProfile().getCmcType());
            RemoteCallbackList<IImsRegistrationListener> cmcRegistrationListeners = SlotBasedConfig.getInstance(imsRegistration.getPhoneId()).getCmcRegistrationListeners();
            if (cmcRegistrationListeners != null) {
                int beginBroadcast = cmcRegistrationListeners.beginBroadcast();
                while (beginBroadcast > 0) {
                    beginBroadcast--;
                    if (z) {
                        try {
                            cmcRegistrationListeners.getBroadcastItem(beginBroadcast).onRegistered(imsRegistration);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        cmcRegistrationListeners.getBroadcastItem(beginBroadcast).onDeregistered(imsRegistration, imsRegistrationError);
                    }
                }
                Log.i(IRegistrationManager.LOG_TAG, "notifyCmcRegistration, finish");
                cmcRegistrationListeners.finishBroadcast();
            }
        }
    }

    /* renamed from: com.sec.internal.ims.core.RegistrationManager$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                com.sec.ims.settings.ImsProfile$PROFILE_TYPE[] r0 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE = r0
                com.sec.ims.settings.ImsProfile$PROFILE_TYPE r1 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.EMERGENCY     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.ims.settings.ImsProfile$PROFILE_TYPE r1 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.VOLTE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.ims.settings.ImsProfile$PROFILE_TYPE r1 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.RCS     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.ims.settings.ImsProfile$PROFILE_TYPE r1 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.CHAT     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManager.AnonymousClass1.<clinit>():void");
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v3, resolved type: com.sec.ims.settings.ImsProfile} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v5, resolved type: com.sec.ims.settings.ImsProfile} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v17, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v7, resolved type: com.sec.ims.settings.ImsProfile} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.ims.settings.ImsProfile getImsProfile(int r6, com.sec.ims.settings.ImsProfile.PROFILE_TYPE r7) {
        /*
            r5 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "getImsProfile: profile ["
            r0.append(r1)
            r0.append(r7)
            java.lang.String r1 = "]"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r2 = "RegiMgr"
            com.sec.internal.log.IMSLog.i(r2, r6, r0)
            int[] r0 = com.sec.internal.ims.core.RegistrationManager.AnonymousClass1.$SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE
            int r3 = r7.ordinal()
            r0 = r0[r3]
            r3 = 1
            if (r0 == r3) goto L_0x0088
            r3 = 2
            r4 = 0
            if (r0 == r3) goto L_0x006b
            r3 = 3
            if (r0 == r3) goto L_0x004e
            r3 = 4
            if (r0 == r3) goto L_0x0031
            goto L_0x008c
        L_0x0031:
            com.sec.ims.settings.ImsProfile[] r5 = r5.getProfileList(r6)
            java.util.stream.Stream r5 = java.util.Arrays.stream(r5)
            com.sec.internal.ims.core.RegistrationManager$$ExternalSyntheticLambda11 r0 = new com.sec.internal.ims.core.RegistrationManager$$ExternalSyntheticLambda11
            r0.<init>()
            java.util.stream.Stream r5 = r5.filter(r0)
            java.util.Optional r5 = r5.findFirst()
            java.lang.Object r5 = r5.orElse(r4)
            r4 = r5
            com.sec.ims.settings.ImsProfile r4 = (com.sec.ims.settings.ImsProfile) r4
            goto L_0x008c
        L_0x004e:
            com.sec.ims.settings.ImsProfile[] r5 = r5.getProfileList(r6)
            java.util.stream.Stream r5 = java.util.Arrays.stream(r5)
            com.sec.internal.ims.core.RegistrationManager$$ExternalSyntheticLambda10 r0 = new com.sec.internal.ims.core.RegistrationManager$$ExternalSyntheticLambda10
            r0.<init>()
            java.util.stream.Stream r5 = r5.filter(r0)
            java.util.Optional r5 = r5.findFirst()
            java.lang.Object r5 = r5.orElse(r4)
            r4 = r5
            com.sec.ims.settings.ImsProfile r4 = (com.sec.ims.settings.ImsProfile) r4
            goto L_0x008c
        L_0x006b:
            com.sec.ims.settings.ImsProfile[] r5 = r5.getProfileList(r6)
            java.util.stream.Stream r5 = java.util.Arrays.stream(r5)
            com.sec.internal.ims.core.RegistrationManager$$ExternalSyntheticLambda9 r0 = new com.sec.internal.ims.core.RegistrationManager$$ExternalSyntheticLambda9
            r0.<init>()
            java.util.stream.Stream r5 = r5.filter(r0)
            java.util.Optional r5 = r5.findFirst()
            java.lang.Object r5 = r5.orElse(r4)
            r4 = r5
            com.sec.ims.settings.ImsProfile r4 = (com.sec.ims.settings.ImsProfile) r4
            goto L_0x008c
        L_0x0088:
            com.sec.ims.settings.ImsProfile r4 = r5.getEmergencyProfile(r6)
        L_0x008c:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r0 = "getImsProfile: found ["
            r5.append(r0)
            if (r4 == 0) goto L_0x009d
            java.lang.String r0 = r4.getName()
            goto L_0x009e
        L_0x009d:
            r0 = r4
        L_0x009e:
            r5.append(r0)
            java.lang.String r0 = "] for ["
            r5.append(r0)
            r5.append(r7)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.e(r2, r6, r5)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManager.getImsProfile(int, com.sec.ims.settings.ImsProfile$PROFILE_TYPE):com.sec.ims.settings.ImsProfile");
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getImsProfile$0(ImsProfile imsProfile) {
        return !imsProfile.hasEmergencySupport() && DeviceConfigManager.IMS.equalsIgnoreCase(imsProfile.getPdn()) && ImsProfile.hasVolteService(imsProfile);
    }

    /* access modifiers changed from: protected */
    public ImsProfile getEmergencyProfile(int i) {
        ImsProfile imsProfile;
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile:");
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager == null) {
            return null;
        }
        Mno devMno = iSimManager.getDevMno();
        boolean needForceToUsePsE911 = ImsUtil.needForceToUsePsE911(i, iSimManager.hasNoSim());
        if (iSimManager.hasNoSim() || RegistrationUtils.checkAusEmergencyCall(devMno, i, iSimManager) || needForceToUsePsE911) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile(no SIM): profile in case of no SIM or AU sales code");
            if (iSimManager.hasNoSim() && !devMno.isAus()) {
                String string = ImsSharedPrefHelper.getString(i, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, "mnoname", "");
                Mno fromName = Mno.fromName(string);
                if (fromName == Mno.DEFAULT || fromName == Mno.GOOGLEGC) {
                    fromName = iSimManager.getNetMno();
                }
                IMSLog.i(IRegistrationManager.LOG_TAG, i, String.format(Locale.US, "getEmergencyProfile(no SIM): Previous mnoname [%s] => [%s]", new Object[]{string, fromName}));
                devMno = fromName;
            }
            String handleExceptionalMnoName = RegistrationUtils.handleExceptionalMnoName(devMno, i, iSimManager);
            if (devMno.isAus() && !handleExceptionalMnoName.equals(Mno.DEFAULT.getName()) && (imsProfile = this.mAuEmergencyProfile.get(i)) != null) {
                return imsProfile;
            }
            if (needForceToUsePsE911) {
                Mno mnoFromNetworkPlmn = iSimManager.getMnoFromNetworkPlmn(getNetworkEvent(i).operatorNumeric);
                if (mnoFromNetworkPlmn.equals(Mno.DEFAULT)) {
                    mnoFromNetworkPlmn = Mno.GCF;
                }
                devMno = mnoFromNetworkPlmn;
                handleExceptionalMnoName = devMno.getName();
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile(networkPlmn or GCF): mno: " + handleExceptionalMnoName);
            } else {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile(no SIM): mno: " + handleExceptionalMnoName);
            }
            for (ImsProfile next : ImsProfileLoaderInternal.getProfileListWithMnoName(this.mContext, handleExceptionalMnoName, i)) {
                if (next.hasEmergencySupport()) {
                    if (devMno.isAus()) {
                        this.mAuEmergencyProfile.put(i, next);
                    }
                    IMSLog.i(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile(no SIM or networkPlmn/GCF): profile: " + next.getName());
                    return next;
                }
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile(no SIM): no profile found");
            return null;
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile: from SlotBasedConfig");
        List<ImsProfile> profiles = SlotBasedConfig.getInstance(i).getProfiles();
        if (CollectionUtils.isNullOrEmpty((Collection<?>) profiles)) {
            IMSLog.e(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile: ProfileList is Empty");
            return null;
        }
        synchronized (profiles) {
            for (ImsProfile next2 : profiles) {
                if (next2.hasEmergencySupport()) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile: profile: " + next2.getName());
                    return next2;
                }
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "getEmergencyProfile: no profile found");
            return null;
        }
    }

    public void onDmConfigurationComplete(int i) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(29, i, 0));
    }

    public IRegistrationGovernor getEmergencyGovernor(int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.mProfile.hasEmergencySupport()) {
                    Log.e(IRegistrationManager.LOG_TAG, "getRegistrationGovernor: return Emergency Gvn");
                    return registerTask.getGovernor();
                }
            }
        }
        Log.e(IRegistrationManager.LOG_TAG, "getRegistrationGovernor: not found Emergency task");
        return null;
    }

    public IRegistrationGovernor getRegistrationGovernor(int i) {
        RegisterTask registerTaskByRegHandle = getRegisterTaskByRegHandle(i);
        if (registerTaskByRegHandle != null) {
            return registerTaskByRegHandle.getGovernor();
        }
        Log.e(IRegistrationManager.LOG_TAG, "getRegistrationGovernor: unknown handle " + i);
        return null;
    }

    public IRegistrationGovernor getRegistrationGovernorByProfileId(int i, int i2) {
        return (IRegistrationGovernor) Optional.ofNullable(getRegisterTaskByProfileId(i, i2)).map(new NetworkEventController$$ExternalSyntheticLambda1()).orElse((Object) null);
    }

    /* access modifiers changed from: protected */
    public RegisterTask getRegisterTask(int i) {
        Log.i(IRegistrationManager.LOG_TAG, "getRegisterTask:");
        for (int i2 = 0; i2 < this.mSimManagers.size(); i2++) {
            RegisterTask registerTaskByProfileId = getRegisterTaskByProfileId(i, i2);
            if (registerTaskByProfileId != null) {
                return registerTaskByProfileId;
            }
        }
        Log.i(IRegistrationManager.LOG_TAG, "getRegisterTask: Not exist matched RegisterTask. Return null..");
        return null;
    }

    /* access modifiers changed from: protected */
    public RegisterTask getRegisterTaskByRegHandle(int i) {
        for (int i2 = 0; i2 < this.mSimManagers.size(); i2++) {
            SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i2);
            if (pendingRegistrationInternal != null) {
                Iterator it = pendingRegistrationInternal.iterator();
                while (it.hasNext()) {
                    RegisterTask registerTask = (RegisterTask) it.next();
                    ImsRegistration imsRegistration = registerTask.mReg;
                    if (imsRegistration != null && imsRegistration.getHandle() == i) {
                        return registerTask;
                    }
                }
                continue;
            }
        }
        Log.i(IRegistrationManager.LOG_TAG, "getRegisterTaskByRegHandle: can not find handle : " + i);
        return null;
    }

    /* access modifiers changed from: protected */
    public RegisterTask getRegisterTaskByProfileId(int i, int i2) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i2);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getProfile().getId() == i) {
                    return registerTask;
                }
            }
        }
        Log.i(IRegistrationManager.LOG_TAG, "getRegisterTaskByProfileId: can not find profile id : " + i);
        return null;
    }

    public void requestTryRegister(int i) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(2, Integer.valueOf(i)));
    }

    public void requestTryRegsiter(int i, long j) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(2, Integer.valueOf(i)), j);
    }

    public void requestFullNetworkRegistration(int i, int i2, String str) {
        if (!this.mHandler.hasMessages(63, Integer.valueOf(i))) {
            this.mHandler.obtainMessage(63, i2, -1, Integer.valueOf(i)).sendToTarget();
        }
    }

    public void requestUpdateSipDelegateRegistration(int i) {
        if (this.mHandler.hasMessages(139, Integer.valueOf(i))) {
            this.mEventLog.logAndAdd(i, "requestUpdateSipDelegateRegistration: Ignore by postponed update registration event by dma change");
            return;
        }
        this.mHandler.updateSipDelegateRegistration(i, this.mSecImsServiceConnector.getSipTransportImpl(i).hasSipDelegate());
    }

    public void cancelUpdateSipDelegateRegistration(int i) {
        this.mEventLog.logAndAdd(i, "cancelUpdateSipDelegateRegistration");
        this.mHandler.removeMessages(58, Integer.valueOf(i));
    }

    public void onUpdateSipDelegateRegistrationTimeOut(int i) {
        if (SlotBasedConfig.getInstance(i).getRegistrationTasks().stream().anyMatch(new RegistrationManager$$ExternalSyntheticLambda0())) {
            this.mEventLog.logAndAdd(i, "onUpdateSipDelegateRegistrationTimeOut: But now registering. Ignore.");
        } else {
            this.mSecImsServiceConnector.getSipTransportImpl(i).onUpdateRegistrationTimeout();
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$onUpdateSipDelegateRegistrationTimeOut$1(RegisterTask registerTask) {
        return registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING && ImsProfile.hasChatService(registerTask.getProfile());
    }

    public void sendReRegister(RegisterTask registerTask) {
        this.mHandler.notifySendReRegisterRequested(registerTask);
    }

    /* access modifiers changed from: protected */
    public String getPublicUserIdentity(RegisterTask registerTask, ISimManager iSimManager) {
        String str;
        int phoneId = registerTask.getPhoneId();
        ImsProfile profile = registerTask.getProfile();
        if (registerTask.getGovernor().getNextImpuType() == 1) {
            str = iSimManager.getDerivedImpu();
        } else if (profile.hasEmergencySupport() && profile.isUicclessEmergency()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, "profile.hasEmergencySupport() && profile.isUicclessEmergency()");
            String emergencyImpu = iSimManager.getEmergencyImpu();
            if (registerTask.getMno() == Mno.VZW && !iSimManager.hasNoSim() && this.mPdnController.hasEmergencyServiceOnly(phoneId)) {
                str = iSimManager.getDerivedImpu();
            } else if (!registerTask.getMno().isKor() || iSimManager.hasNoSim() || (str = getPreferredImpuOnPdn(11, phoneId)) == null) {
                str = emergencyImpu;
            }
        } else if (!profile.hasEmergencySupport() || profile.isUicclessEmergency()) {
            str = RegistrationUtils.getPublicUserIdentity(profile, phoneId, this.mRcsPolicyManager.getRcsPublicUserIdentity(phoneId), iSimManager);
        } else {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "profile.hasEmergencySupport() && !profile.isUicclessEmergency()");
            String preferredImpuOnPdn = getPreferredImpuOnPdn(11, phoneId);
            if (preferredImpuOnPdn == null) {
                preferredImpuOnPdn = "";
            }
            str = (TextUtils.isEmpty(preferredImpuOnPdn) || !SimManager.isValidImpu(preferredImpuOnPdn) || registerTask.getMno().isOneOf(Mno.ATT, Mno.KDDI, Mno.H3G_AT)) ? RegistrationUtils.getPublicUserIdentity(profile, phoneId, this.mRcsPolicyManager.getRcsPublicUserIdentity(phoneId), iSimManager) : preferredImpuOnPdn;
        }
        IMSLog.s(IRegistrationManager.LOG_TAG, phoneId, "impu : " + str);
        return str;
    }

    /* access modifiers changed from: protected */
    public boolean validateImpi(RegisterTask registerTask, ISimManager iSimManager, String str) {
        String str2;
        int phoneId = registerTask.getPhoneId();
        Mno mno = registerTask.getMno();
        if (mno == Mno.TELEFONICA_GERMANY && registerTask.isRcsOnly()) {
            if (iSimManager.hasIsim()) {
                str2 = RegistrationUtils.getPrivateUserIdentityfromIsim(phoneId, this.mTelephonyManager, iSimManager, mno);
            } else {
                str2 = iSimManager.getDerivedImpi();
            }
            if (!(str2 == null || str == null)) {
                int indexOf = str2.indexOf(64);
                if (indexOf > 0) {
                    str2 = str2.substring(0, indexOf);
                }
                int indexOf2 = str.indexOf(64);
                if (indexOf2 > 0) {
                    str = str.substring(0, indexOf2);
                }
                IMSLog.s(IRegistrationManager.LOG_TAG, phoneId, "impiFromSim : " + str2);
                IMSLog.s(IRegistrationManager.LOG_TAG, phoneId, "impi : " + str);
                if (!str2.equals(str)) {
                    IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "error : invalid IMPI");
                    this.mEventLog.logAndAdd(phoneId, registerTask, "registerInternal : error - invalid IMPI");
                    IMSLog.c(LogClass.REGI_INVALID_IMPI, phoneId + ",REG FAIL:INVALID IMPI");
                    registerTask.setReason("");
                    return false;
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean validateImpu(RegisterTask registerTask, String str) {
        int phoneId = registerTask.getPhoneId();
        ImsProfile profile = registerTask.getProfile();
        if (((registerTask.getMno() == Mno.CMCC || registerTask.getMno() == Mno.CU) && profile.hasEmergencySupport() && !profile.isUicclessEmergency()) || SimManager.isValidImpu(str)) {
            return true;
        }
        IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "error : invalid IMPU");
        this.mEventLog.logAndAdd(phoneId, registerTask, "registerInternal : error - invalid IMPU");
        IMSLog.c(LogClass.REGI_INVALID_IMPU, phoneId + ",REG FAIL:INVALD IMPU");
        registerTask.setReason("");
        return false;
    }

    public String getHomeNetworkDomain(ImsProfile imsProfile, int i) {
        return RegistrationUtils.getHomeNetworkDomain(this.mContext, imsProfile, i, this.mTelephonyManager, this.mRcsPolicyManager, getSimManager(i));
    }

    public String getPrivateUserIdentity(RegisterTask registerTask) {
        int phoneId = registerTask.getPhoneId();
        String impi = getImpi(registerTask.getProfile(), phoneId);
        if (registerTask.isRcsOnly()) {
            return (registerTask.getMno() == Mno.SINGTEL || registerTask.getMno() == Mno.STARHUB || registerTask.getMno() == Mno.RJIL) ? RcsConfigurationHelper.getUserName(this.mContext, phoneId) : impi;
        }
        return impi;
    }

    public String getImpi(ImsProfile imsProfile, int i) {
        return RegistrationUtils.getPrivateUserIdentity(this.mContext, imsProfile, i, this.mTelephonyManager, this.mRcsPolicyManager, getSimManager(i));
    }

    /* access modifiers changed from: protected */
    public String getInterfaceName(RegisterTask registerTask, String str, int i) {
        String str2;
        String acsServerType = ConfigUtil.getAcsServerType(i);
        if (((!registerTask.getMno().isVodafone() || !RcsUtils.DualRcs.isDualRcsReg()) && !ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(acsServerType)) || !registerTask.isRcsOnly()) {
            str2 = this.mPdnController.getInterfaceName(registerTask);
        } else {
            str2 = this.mRcsPolicyManager.changeRcsIfacename(registerTask, this.mPdnController, str);
        }
        if (registerTask.getProfile() != null) {
            int cmcType = registerTask.getProfile().getCmcType();
            if (cmcType == 7 || cmcType == 8) {
                str2 = "p2p-wlan0-0";
            } else if (cmcType == 5) {
                str2 = "swlan0";
            }
        }
        if (!SimUtil.isSoftphoneEnabled() || !NetworkUtil.isIPv4Address(str) || this.mPdnController.getLinkProperties(registerTask) == null || this.mPdnController.getLinkProperties(registerTask).hasIPv4Address() || str2 == null || str2.contains("v4")) {
            return str2;
        }
        String str3 = "v4-" + str2;
        IMSLog.i(IRegistrationManager.LOG_TAG, registerTask.getPhoneId(), "Stacked IP interface" + str3);
        return str3;
    }

    public ImsRegistration getRegistrationInfo(int i) {
        int i2 = ImsConstants.Phone.SLOT_1;
        if (i >= 20000) {
            i2 = ImsConstants.Phone.SLOT_2;
        } else if (i < 10000 && i >= 5000) {
            i2 = ImsConstants.Phone.SLOT_2;
        }
        return RegistrationUtils.getRegistrationInfo(i2, i);
    }

    public NetworkEvent getNetworkEvent(int i) {
        return RegistrationUtils.getNetworkEvent(i);
    }

    public ImsRegistration[] getRegistrationInfoByPhoneId(int i) {
        return RegistrationUtils.getRegistrationInfoByPhoneId(i, getRegistrationInfo());
    }

    public ImsProfile[] getProfileList(int i) {
        return RegistrationUtils.getProfileList(i);
    }

    public int getCmcLineSlotIndex() {
        return this.mCmcAccountManager.getCurrentLineSlotIndex();
    }

    public void releaseThrottleByAcs(int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getPhoneId() == i && ImsProfile.hasRcsService(registerTask.getProfile())) {
                    registerTask.getGovernor().releaseThrottle(7);
                    break;
                }
            }
        }
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(2, Integer.valueOf(i)));
    }

    public void releaseThrottleByCmc(IRegisterTask iRegisterTask) {
        if (iRegisterTask.getGovernor().isThrottled()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, iRegisterTask.getPhoneId(), "releaseThrottleByCmc: releaseThrottle");
            iRegisterTask.getGovernor().releaseThrottle(8);
        }
    }

    public void blockVoWifiRegisterOnRoaminByCsfbError(int i, int i2) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(144, i, i2, (Object) null));
    }

    public void updateChatService(int i, int i2) {
        int i3 = i2 == 2 ? 138 : i2 == 1 ? 137 : -1;
        if (i3 != -1) {
            this.mHandler.removeMessages(i3, Integer.valueOf(i));
            this.mHandler.obtainMessage(i3, Integer.valueOf(i)).sendToTarget();
        }
    }

    public void updatePcoInfo(int i, int i2, int i3) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(703, i3, i, Integer.valueOf(i2)));
    }

    public boolean isVoWiFiSupported(int i) {
        try {
            if (this.mImsFramework.isServiceAvailable("mmtel", 18, i) || this.mImsFramework.isServiceAvailable("mmtel-video", 18, i)) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isPdnConnected(ImsProfile imsProfile, int i) {
        if (imsProfile == null) {
            Log.e(IRegistrationManager.LOG_TAG, "isPdnConnected: profile not found.");
            return false;
        }
        RegisterTask registerTaskByProfileId = getRegisterTaskByProfileId(imsProfile.getId(), i);
        if (registerTaskByProfileId == null) {
            Log.e(IRegistrationManager.LOG_TAG, "isPdnConnected: task not found.");
            return false;
        }
        boolean isConnected = this.mPdnController.isConnected(registerTaskByProfileId.getPdnType(), registerTaskByProfileId);
        Log.i(IRegistrationManager.LOG_TAG, "isPdnConnected: " + isConnected + ", PdnType: " + registerTaskByProfileId.getPdnType());
        return isConnected;
    }

    public boolean hasVoLteSim(int i) {
        if (getSimManager(i) != null) {
            return RegistrationUtils.hasVoLteSim(i, getSimManager(i), this.mTelephonyManager, RegistrationUtils.getPendingRegistrationInternal(i));
        }
        return false;
    }

    public Map<Integer, ImsRegistration> getRegistrationList() {
        HashMap hashMap = new HashMap();
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            hashMap.putAll(SlotBasedConfig.getInstance(i).getImsRegistrations());
        }
        return hashMap;
    }

    public boolean isEmergencyCallProhibited(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "isEmergencyCallProhibited:");
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal == null) {
            return false;
        }
        Iterator it = pendingRegistrationInternal.iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            if (registerTask != null && registerTask.getProfile().getPdnType() == 11 && registerTask.getGovernor().isPse911Prohibited()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEpdnRequestPending(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "isEpdnRequestPending:");
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal == null) {
            return false;
        }
        Iterator it = pendingRegistrationInternal.iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            if (registerTask.getProfile().hasEmergencySupport() && registerTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING) {
                return true;
            }
        }
        return false;
    }

    public boolean isRcsRegistered(int i) {
        return RegistrationUtils.isRcsRegistered(i, getRegistrationInfo());
    }

    public int isCmcRegistered(int i) {
        Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            int cmcType = registerTask.getProfile().getCmcType();
            if ((cmcType == 1 || cmcType == 2) && registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                return registerTask.getProfile().getId();
            }
        }
        return 0;
    }

    public int getTelephonyCallStatus(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "getTelephonyCallStatus:");
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal == null) {
            return -1;
        }
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        boolean z = iVolteServiceModule != null && iVolteServiceModule.getSessionCount(i) > 0;
        IMSLog.d(IRegistrationManager.LOG_TAG, i, "getTelephonyCallStatus: hasImsCall = " + z);
        Iterator it = pendingRegistrationInternal.iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            if (registerTask.mProfile.hasEmergencySupport()) {
                return 0;
            }
            if (!z && this.mTelephonyManager.getVoiceNetworkType(SimUtil.getSubId(i)) == 0 && registerTask.getRegistrationRat() == 18 && registerTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && registerTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
                IMSLog.d(IRegistrationManager.LOG_TAG, i, "getTelephonyCallStatus: Have No normal IMS/CS call => allow VoWifi registration.");
                return 0;
            }
        }
        return this.mTelephonyManager.getCallState(i);
    }

    public void setSSACPolicy(int i, boolean z) {
        SlotBasedConfig.getInstance(i).enableSsac(z);
        if (!z) {
            this.mHandler.removeMessages(121, Integer.valueOf(i));
        }
    }

    public void notifyRomaingSettingsChanged(int i, int i2) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(46, i, i2, (Object) null));
    }

    public void notifyRCSAllowedChangedbyMDM() {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(53));
    }

    public Set<String> getServiceForNetwork(ImsProfile imsProfile, int i, boolean z, int i2) {
        RegisterTask registerTaskByProfileId;
        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "getServiceForNetwork: network " + i);
        int blurNetworkType = NetworkEvent.blurNetworkType(i);
        HashSet hashSet = new HashSet();
        if (!imsProfile.getNetworkSet().contains(Integer.valueOf(blurNetworkType))) {
            return hashSet;
        }
        Set<String> serviceSet = imsProfile.getServiceSet(Integer.valueOf(blurNetworkType));
        if (z) {
            serviceSet = imsProfile.getAllServiceSetFromAllNetwork();
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "getServiceForNetwork: service " + serviceSet);
        if (imsProfile.hasEmergencySupport() || (registerTaskByProfileId = getRegisterTaskByProfileId(imsProfile.getId(), i2)) == null) {
            return serviceSet;
        }
        registerTaskByProfileId.clearFilteredReason();
        if (blurNetworkType == 18 && registerTaskByProfileId.getProfile().getPdnType() == 11 && this.mPdnController.getEpdgPhysicalInterface(i2) == 2) {
            serviceSet = registerTaskByProfileId.getGovernor().applyCrossSimPolicy(serviceSet, i2);
        }
        Set<String> filterserviceFbe = RegistrationUtils.filterserviceFbe(this.mContext, registerTaskByProfileId.getGovernor().filterService(serviceSet, blurNetworkType), registerTaskByProfileId.getProfile());
        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "getServiceForNetwork: filtered service " + filterserviceFbe);
        return filterserviceFbe;
    }

    public void addPendingUpdateRegistration(IRegisterTask iRegisterTask, int i) {
        iRegisterTask.setPendingUpdate(true);
        this.mHandler.removeMessages(32);
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(32, iRegisterTask), ((long) i) * 1000);
    }

    /* access modifiers changed from: protected */
    public String getUuid(int i, ImsProfile imsProfile) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "getUuid:");
        if (!imsProfile.isEnableSessionId()) {
            return "";
        }
        String replace = UUID.randomUUID().toString().replace(CmcConstants.E_NUM_SLOT_SPLIT, "");
        Log.i(IRegistrationManager.LOG_TAG, "UUID=" + replace);
        return replace;
    }

    /* access modifiers changed from: protected */
    public String getInstanceId(int i, int i2, ImsProfile imsProfile) {
        Mno mno = SimUtil.getMno();
        if (i2 == 11 || i2 == 15 || ((!ConfigUtil.isRcsChn(mno) || !ImsProfile.isRcsUp24Profile(imsProfile.getRcsProfile())) && mno != Mno.MTS_RUSSIA)) {
            String instanceId = getInstanceId(i, mno);
            IMSLog.s(IRegistrationManager.LOG_TAG, "getInstanceId by phoneId: " + instanceId);
            return instanceId;
        }
        String uuidInstanceId = new TimeBasedUuidGenerator(i, this.mContext).getUuidInstanceId();
        IMSLog.s(IRegistrationManager.LOG_TAG, "getInstanceId time based uuid: " + uuidInstanceId);
        return uuidInstanceId;
    }

    /* access modifiers changed from: package-private */
    public String getInstanceId(int i, Mno mno) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "getInstanceId:");
        ISimManager iSimManager = this.mSimManagers.get(i);
        String str = "";
        if (iSimManager == null) {
            return str;
        }
        String imei = this.mTelephonyManager.getImei(iSimManager.getSimSlotIndex());
        if (TextUtils.isEmpty(imei) || iSimManager.hasVsim()) {
            return (String) Optional.ofNullable(ImsSharedPrefHelper.getString(i, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, IRegistrationManager.KEY_INSTANCE_ID, (String) null)).orElseGet(new RegistrationManager$$ExternalSyntheticLambda3(this, i));
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "getInstanceId: deviceId len: " + imei.length());
        if (imei.length() < 14) {
            Log.i(IRegistrationManager.LOG_TAG, "Invalid deviceId. Read imei again");
            imei = this.mTelephonyManager.getImei(i);
        }
        String meid = this.mTelephonyManager.getMeid(i);
        if (!TextUtils.isEmpty(imei) && imei.length() >= 14) {
            if (mno == Mno.TMOUS) {
                str = this.mTelephonyManager.getDeviceSoftwareVersion(SimUtil.getSubId(i));
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "getInstanceId: imei len=" + imei.length() + ", dsv=" + str);
            return "<urn:gsma:imei:" + DeviceUtil.getFormattedDeviceId(imei, str) + ">";
        } else if (TextUtils.isEmpty(meid) || meid.length() < 14) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "getInstanceId: imei/meid seems be wrong!");
            return null;
        } else {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "getInstanceId: meid len=" + meid.length());
            return "<urn:device-id:meid:" + DeviceUtil.getFormattedDeviceId(meid, str) + ">";
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ String lambda$getInstanceId$2(int i) {
        String str = "<urn:uuid:" + UUID.randomUUID().toString() + ">";
        ImsSharedPrefHelper.save(i, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, IRegistrationManager.KEY_INSTANCE_ID, str);
        return str;
    }

    public String getAvailableNetworkType(String str) {
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            for (ImsRegistration next : SlotBasedConfig.getInstance(i).getImsRegistrations().values()) {
                if (next.hasService(str)) {
                    return next.getImsProfile().getPdn();
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String getPreferredImpuOnPdn(int i, int i2) {
        IMSLog.i(IRegistrationManager.LOG_TAG, "getPreferredImpuOnPdn: phoneId=" + i2 + " pdn=" + i);
        return (String) SlotBasedConfig.getInstance(i2).getImsRegistrations().values().stream().filter(new RegistrationManager$$ExternalSyntheticLambda5(i)).findFirst().map(new RegistrationManager$$ExternalSyntheticLambda6()).map(new RegistrationManager$$ExternalSyntheticLambda7()).orElse((Object) null);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getPreferredImpuOnPdn$3(int i, ImsRegistration imsRegistration) {
        return imsRegistration.getImsProfile().getPdnType() == i;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x026d  */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x02a2  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x02b3  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x02c4  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x02d0  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x0324  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x033b  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x0342  */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x0384  */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x039a  */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x03f4  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01b8  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01bd  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String buildUserAgentString(com.sec.ims.settings.ImsProfile r17, java.lang.String r18, int r19) {
        /*
            r16 = this;
            r1 = r16
            r2 = r19
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            android.content.Context r3 = r1.mContext
            java.lang.String r4 = "volte"
            int r3 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r3, (java.lang.String) r4, (int) r2)
            r4 = 0
            r5 = 1
            if (r3 != r5) goto L_0x0018
            r3 = r5
            goto L_0x0019
        L_0x0018:
            r3 = r4
        L_0x0019:
            java.lang.String r6 = r17.getMnoName()
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.fromName(r6)
            java.lang.String r7 = ""
            r8 = 3
            if (r3 == 0) goto L_0x00d8
            com.sec.internal.constants.ims.os.NetworkEvent r9 = r1.getNetworkEvent(r2)
            r10 = 20
            int r9 = r9.network
            if (r10 != r9) goto L_0x0049
            com.sec.internal.interfaces.ims.IImsFramework r9 = r1.mImsFramework
            java.lang.String r10 = "sip_ua_vonr_service_type"
            java.lang.String r9 = r9.getString(r2, r10, r7)
            boolean r10 = android.text.TextUtils.isEmpty(r9)
            if (r10 != 0) goto L_0x0043
            r0.add(r9)
            goto L_0x004e
        L_0x0043:
            java.lang.String r9 = "EPSFB"
            r0.add(r9)
            goto L_0x004e
        L_0x0049:
            java.lang.String r9 = "VoLTE"
            r0.add(r9)
        L_0x004e:
            android.content.Context r9 = r1.mContext
            java.lang.String r10 = "rcs"
            int r9 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r9, (java.lang.String) r10, (int) r2)
            if (r9 != r5) goto L_0x0075
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.TMOUS
            if (r6 != r9) goto L_0x006d
            java.lang.String r9 = "ft_http"
            r10 = r17
            boolean r9 = r10.hasService(r9)
            if (r9 == 0) goto L_0x006f
            java.lang.String r9 = "RCSUP"
            r0.add(r9)
            goto L_0x0077
        L_0x006d:
            r10 = r17
        L_0x006f:
            java.lang.String r9 = "RCS"
            r0.add(r9)
            goto L_0x0077
        L_0x0075:
            r10 = r17
        L_0x0077:
            java.util.List r9 = r1.getPendingRegistration(r2)
            boolean r11 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.util.Collection<?>) r9)
            if (r11 != 0) goto L_0x0092
            boolean r11 = r17.isEpdgSupported()
            if (r11 == 0) goto L_0x0092
            java.lang.String r11 = "ePDG"
            boolean r12 = r0.contains(r11)
            if (r12 != 0) goto L_0x0092
            r0.add(r11)
        L_0x0092:
            android.content.Context r11 = r1.mContext
            java.lang.String r12 = "mmtel-video"
            int r11 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r11, (java.lang.String) r12, (int) r2)
            if (r11 != r5) goto L_0x00a1
            java.lang.String r11 = "IR94"
            r0.add(r11)
        L_0x00a1:
            boolean r11 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.util.Collection<?>) r9)
            if (r11 != 0) goto L_0x00bf
            java.lang.String r11 = "RTT"
            boolean r12 = r0.contains(r11)
            if (r12 != 0) goto L_0x00bf
            int r12 = r17.getTtyType()
            r13 = 4
            if (r12 == r13) goto L_0x00bc
            int r12 = r17.getTtyType()
            if (r12 != r8) goto L_0x00bf
        L_0x00bc:
            r0.add(r11)
        L_0x00bf:
            boolean r9 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.util.Collection<?>) r9)
            if (r9 != 0) goto L_0x00df
            boolean r9 = r17.getSupport3gppUssi()
            if (r9 == 0) goto L_0x00df
            java.lang.String r9 = "ussd"
            boolean r11 = r0.contains(r9)
            if (r11 != 0) goto L_0x00df
            r0.add(r9)
            goto L_0x00df
        L_0x00d8:
            r10 = r17
            java.lang.String r9 = "TAS"
            r0.add(r9)
        L_0x00df:
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.TMOUS
            if (r6 != r9) goto L_0x00e8
            java.lang.String r9 = "VVM"
            r0.add(r9)
        L_0x00e8:
            java.util.Iterator r0 = r0.iterator()
        L_0x00ec:
            boolean r9 = r0.hasNext()
            if (r9 == 0) goto L_0x0109
            java.lang.Object r9 = r0.next()
            java.lang.String r9 = (java.lang.String) r9
            boolean r11 = r7.isEmpty()
            if (r11 != 0) goto L_0x0104
            java.lang.String r11 = "-"
            java.lang.String r7 = r7.concat(r11)
        L_0x0104:
            java.lang.String r7 = r7.concat(r9)
            goto L_0x00ec
        L_0x0109:
            java.lang.String r0 = "[SUPPORT]"
            r9 = r18
            java.lang.String r0 = r9.replace(r0, r7)
            java.lang.String r9 = "[OS_VERSION]"
            java.lang.String r11 = android.os.Build.VERSION.RELEASE
            java.lang.String r9 = r0.replace(r9, r11)
            java.lang.String r0 = "ro.build.PDA"
            java.lang.String r11 = android.os.SemSystemProperties.get(r0)
            java.lang.String r12 = r17.getUiccMobilityVersion()
            java.lang.String r0 = "[IMEISV]"
            boolean r13 = r9.contains(r0)
            java.lang.String r15 = "RegiMgr"
            if (r13 == 0) goto L_0x01a5
            java.lang.String r13 = "iphonesubinfo"
            android.os.IBinder r13 = android.os.ServiceManager.getService(r13)
            com.android.internal.telephony.IPhoneSubInfo r13 = com.android.internal.telephony.IPhoneSubInfo.Stub.asInterface(r13)
            if (r13 == 0) goto L_0x01a5
            com.sec.internal.helper.os.ITelephonyManager r5 = r1.mTelephonyManager     // Catch:{ RemoteException -> 0x019f }
            java.lang.String r5 = r5.getImei(r2)     // Catch:{ RemoteException -> 0x019f }
            if (r5 == 0) goto L_0x01a5
            int r8 = r5.length()     // Catch:{ RemoteException -> 0x019f }
            r14 = 14
            if (r8 <= r14) goto L_0x01a5
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x019f }
            r8.<init>()     // Catch:{ RemoteException -> 0x019f }
            java.lang.String r4 = r5.substring(r4, r14)     // Catch:{ RemoteException -> 0x019f }
            r8.append(r4)     // Catch:{ RemoteException -> 0x019f }
            java.lang.String r4 = "imsservice"
            r5 = 0
            java.lang.String r4 = r13.getDeviceSvn(r4, r5)     // Catch:{ RemoteException -> 0x019d }
            r8.append(r4)     // Catch:{ RemoteException -> 0x019d }
            java.lang.String r4 = r8.toString()     // Catch:{ RemoteException -> 0x019d }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x019d }
            r8.<init>()     // Catch:{ RemoteException -> 0x019d }
            java.lang.String r13 = "imeiSV = "
            r8.append(r13)     // Catch:{ RemoteException -> 0x019d }
            java.lang.String r13 = com.sec.internal.log.IMSLog.checker(r4)     // Catch:{ RemoteException -> 0x019d }
            r8.append(r13)     // Catch:{ RemoteException -> 0x019d }
            java.lang.String r8 = r8.toString()     // Catch:{ RemoteException -> 0x019d }
            com.sec.internal.log.IMSLog.d(r15, r8)     // Catch:{ RemoteException -> 0x019d }
            java.lang.String r4 = r9.replace(r0, r4)     // Catch:{ RemoteException -> 0x019d }
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x019a }
            r0.<init>()     // Catch:{ RemoteException -> 0x019a }
            java.lang.String r8 = "inside sipUserAgent = "
            r0.append(r8)     // Catch:{ RemoteException -> 0x019a }
            java.lang.String r8 = com.sec.internal.log.IMSLog.checker(r4)     // Catch:{ RemoteException -> 0x019a }
            r0.append(r8)     // Catch:{ RemoteException -> 0x019a }
            java.lang.String r0 = r0.toString()     // Catch:{ RemoteException -> 0x019a }
            com.sec.internal.log.IMSLog.d(r15, r0)     // Catch:{ RemoteException -> 0x019a }
            r9 = r4
            goto L_0x01a6
        L_0x019a:
            r0 = move-exception
            r9 = r4
            goto L_0x01a1
        L_0x019d:
            r0 = move-exception
            goto L_0x01a1
        L_0x019f:
            r0 = move-exception
            r5 = 0
        L_0x01a1:
            r0.printStackTrace()
            goto L_0x01a6
        L_0x01a5:
            r5 = 0
        L_0x01a6:
            com.sec.internal.interfaces.ims.IImsFramework r0 = r1.mImsFramework
            com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager r0 = r0.getServiceModuleManager()
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r0 = r0.getImModule()
            android.content.Context r4 = r1.mContext
            java.lang.String r8 = r17.getRcsProfile()
            if (r0 == 0) goto L_0x01bd
            com.sec.internal.ims.servicemodules.im.ImConfig r14 = r0.getImConfig(r2)
            goto L_0x01be
        L_0x01bd:
            r14 = r5
        L_0x01be:
            java.lang.String r0 = com.sec.internal.ims.core.RegistrationUtils.replaceEnablerPlaceholderWithEnablerVersion(r4, r8, r9, r2, r14)
            boolean r4 = r6.isTmobile()
            java.lang.String r5 = "[BUILD_VERSION]"
            if (r4 != 0) goto L_0x01ce
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TELEKOM_ALBANIA
            if (r6 != r4) goto L_0x01e7
        L_0x01ce:
            if (r11 == 0) goto L_0x01e7
            int r4 = r11.length()
            r8 = 8
            if (r4 <= r8) goto L_0x01e7
            int r4 = r11.length()
            int r4 = r4 - r8
            java.lang.String r4 = r11.substring(r4)
            java.lang.String r0 = r0.replace(r5, r4)
            goto L_0x02b7
        L_0x01e7:
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.PLUS_POLAND
            java.lang.String r8 = "ril.sw_ver"
            java.lang.String r9 = "_"
            if (r6 != r4) goto L_0x024c
            if (r11 == 0) goto L_0x024c
            int r4 = r11.length()
            r13 = 3
            if (r4 <= r13) goto L_0x024c
            int r4 = r11.length()
            int r4 = r4 - r13
            java.lang.String r4 = r11.substring(r4)
            java.lang.String r8 = android.os.SemSystemProperties.get(r8)
            java.lang.String r11 = "ril.official_cscver"
            java.lang.String r11 = android.os.SemSystemProperties.get(r11)
            if (r8 == 0) goto L_0x0247
            int r14 = r8.length()
            if (r14 <= r13) goto L_0x0247
            if (r11 == 0) goto L_0x0247
            int r14 = r11.length()
            if (r14 <= r13) goto L_0x0247
            int r14 = r8.length()
            int r14 = r14 - r13
            java.lang.String r8 = r8.substring(r14)
            int r14 = r11.length()
            int r14 = r14 - r13
            java.lang.String r11 = r11.substring(r14)
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            r13.append(r4)
            r13.append(r9)
            r13.append(r8)
            r13.append(r9)
            r13.append(r11)
            java.lang.String r4 = r13.toString()
        L_0x0247:
            java.lang.String r0 = r0.replace(r5, r4)
            goto L_0x02b7
        L_0x024c:
            boolean r4 = r6.isKor()
            if (r4 != 0) goto L_0x0258
            boolean r4 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r6)
            if (r4 == 0) goto L_0x02b1
        L_0x0258:
            if (r11 == 0) goto L_0x02b1
            int r4 = r11.length()
            r13 = 3
            if (r4 <= r13) goto L_0x02b1
            boolean r4 = r6.isKor()
            if (r4 == 0) goto L_0x02a2
            boolean r4 = com.sec.internal.helper.OmcCode.isKOROmcCode()
            if (r4 == 0) goto L_0x02a2
            int r4 = r11.length()
            int r4 = r4 - r13
            java.lang.String r4 = r11.substring(r4)
            java.lang.String r8 = android.os.SemSystemProperties.get(r8)
            if (r8 == 0) goto L_0x029d
            int r11 = r8.length()
            if (r11 <= r13) goto L_0x029d
            int r11 = r8.length()
            int r11 = r11 - r13
            java.lang.String r8 = r8.substring(r11)
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r11.append(r4)
            r11.append(r9)
            r11.append(r8)
            java.lang.String r4 = r11.toString()
        L_0x029d:
            java.lang.String r0 = r0.replace(r5, r4)
            goto L_0x02b7
        L_0x02a2:
            int r4 = r11.length()
            r8 = 3
            int r4 = r4 - r8
            java.lang.String r4 = r11.substring(r4)
            java.lang.String r0 = r0.replace(r5, r4)
            goto L_0x02b7
        L_0x02b1:
            if (r11 == 0) goto L_0x02b7
            java.lang.String r0 = r0.replace(r5, r11)
        L_0x02b7:
            java.lang.String r4 = android.os.Build.MODEL
            java.lang.String r5 = "unknown"
            boolean r5 = r5.equals(r4)
            java.lang.String r8 = "[PRODUCT_MODEL]"
            if (r5 == 0) goto L_0x02d0
            java.lang.String r4 = "ro.product.base_model"
            java.lang.String r4 = android.os.SemSystemProperties.get(r4)
            java.lang.String r0 = r0.replace(r8, r4)
            goto L_0x031c
        L_0x02d0:
            com.samsung.android.feature.SemFloatingFeature r5 = com.samsung.android.feature.SemFloatingFeature.getInstance()
            java.lang.String r9 = "SEC_FLOATING_FEATURE_COMMON_SUPPORT_MHS_DONGLE"
            java.lang.String r5 = r5.getString(r9)
            boolean r5 = java.lang.Boolean.parseBoolean(r5)
            if (r5 == 0) goto L_0x0303
            boolean r4 = r6.isKor()
            if (r4 == 0) goto L_0x02e9
            java.lang.String r4 = "NT930QCA"
            goto L_0x02eb
        L_0x02e9:
            java.lang.String r4 = "NP930QCA"
        L_0x02eb:
            java.lang.String r5 = "ril.PcModelName"
            java.lang.String r4 = android.os.SemSystemProperties.get(r5, r4)
            java.lang.String r0 = r0.replace(r8, r4)
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.KT
            if (r6 != r4) goto L_0x031c
            java.lang.String r4 = "Android_Phone"
            java.lang.String r5 = "Laptop_PC"
            java.lang.String r0 = r0.replace(r4, r5)
            goto L_0x031c
        L_0x0303:
            boolean r5 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((int) r19)
            if (r5 == 0) goto L_0x0318
            boolean r5 = com.sec.internal.ims.util.ConfigUtil.isRcsOnly(r17)
            if (r5 == 0) goto L_0x0318
            java.lang.String r4 = com.sec.internal.ims.config.ConfigContract.BUILD.getTerminalModel()
            java.lang.String r0 = r0.replace(r8, r4)
            goto L_0x031c
        L_0x0318:
            java.lang.String r0 = r0.replace(r8, r4)
        L_0x031c:
            java.lang.String r4 = "[CLIENT_VERSION]"
            boolean r5 = r0.contains(r4)
            if (r5 == 0) goto L_0x0333
            com.sec.internal.interfaces.ims.IImsFramework r5 = r1.mImsFramework
            java.lang.String r8 = "rcs_client_version"
            java.lang.String r9 = "6.0"
            java.lang.String r5 = r5.getString(r2, r8, r9)
            java.lang.String r0 = r0.replace(r4, r5)
        L_0x0333:
            boolean r4 = com.sec.internal.helper.OmcCode.isSKTOmcCode()
            java.lang.String r5 = "[OMCCODE]"
            if (r4 == 0) goto L_0x0342
            java.lang.String r4 = "SKT"
            java.lang.String r0 = r0.replace(r5, r4)
            goto L_0x037e
        L_0x0342:
            boolean r4 = com.sec.internal.helper.OmcCode.isKTTOmcCode()
            if (r4 == 0) goto L_0x034f
            java.lang.String r4 = "KT"
            java.lang.String r0 = r0.replace(r5, r4)
            goto L_0x037e
        L_0x034f:
            boolean r4 = com.sec.internal.helper.OmcCode.isLGTOmcCode()
            if (r4 == 0) goto L_0x035c
            java.lang.String r4 = "LGU"
            java.lang.String r0 = r0.replace(r5, r4)
            goto L_0x037e
        L_0x035c:
            boolean r4 = com.sec.internal.helper.OmcCode.isKorOpenOmcCode()
            if (r4 != 0) goto L_0x0378
            boolean r4 = r6.isKor()
            if (r4 == 0) goto L_0x036f
            boolean r4 = r17.getSimMobility()
            if (r4 == 0) goto L_0x036f
            goto L_0x0378
        L_0x036f:
            java.lang.String r4 = com.sec.internal.helper.OmcCode.getUserAgentNWCode(r2, r6)
            java.lang.String r0 = r0.replace(r5, r4)
            goto L_0x037e
        L_0x0378:
            java.lang.String r4 = "OMD"
            java.lang.String r0 = r0.replace(r5, r4)
        L_0x037e:
            boolean r4 = r6.isKor()
            if (r4 == 0) goto L_0x038a
            java.lang.String r4 = "[UICC_VERSION]"
            java.lang.String r0 = r0.replace(r4, r12)
        L_0x038a:
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r4 = r1.mSimManagers
            java.lang.Object r4 = r4.get(r2)
            com.sec.internal.interfaces.ims.core.ISimManager r4 = (com.sec.internal.interfaces.ims.core.ISimManager) r4
            boolean r5 = r6.isOrange()
            java.lang.String r8 = "[MNO_CUSTOM]"
            if (r5 == 0) goto L_0x03f0
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            boolean r9 = r6.isOrangeGPG()
            if (r9 == 0) goto L_0x03d7
            boolean r9 = com.sec.internal.helper.os.DeviceUtil.isTablet()
            java.lang.String r10 = "[PRODUCT_TYPE]"
            if (r9 == 0) goto L_0x03b4
            java.lang.String r9 = "device-type/tablet"
            java.lang.String r0 = r0.replace(r10, r9)
            goto L_0x03ba
        L_0x03b4:
            java.lang.String r9 = "device-type/smart-phone"
            java.lang.String r0 = r0.replace(r10, r9)
        L_0x03ba:
            java.lang.String r9 = "mno-custom/"
            r5.append(r9)
            if (r4 == 0) goto L_0x03ce
            boolean r9 = r4.isSimAvailable()
            if (r9 == 0) goto L_0x03ce
            java.lang.String r9 = r4.getSimOperator()
            r5.append(r9)
        L_0x03ce:
            java.lang.String r5 = r5.toString()
            java.lang.String r0 = r0.replace(r8, r5)
            goto L_0x03f0
        L_0x03d7:
            if (r4 == 0) goto L_0x03e6
            boolean r9 = r4.isSimAvailable()
            if (r9 == 0) goto L_0x03e6
            java.lang.String r9 = r4.getSimOperator()
            r5.append(r9)
        L_0x03e6:
            java.lang.String r9 = "[MCCMNC]"
            java.lang.String r5 = r5.toString()
            java.lang.String r0 = r0.replace(r9, r5)
        L_0x03f0:
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TMOUS
            if (r6 != r5) goto L_0x043f
            java.lang.String r5 = "VoNR"
            boolean r5 = r7.contains(r5)
            if (r5 == 0) goto L_0x03ff
            java.lang.String r5 = "PRD-NG114/1"
            goto L_0x0401
        L_0x03ff:
            java.lang.String r5 = "PRD-IR92/13"
        L_0x0401:
            java.lang.String r6 = "[IMS_PROFILE_VOICE]"
            java.lang.String r0 = r0.replace(r6, r5)
            com.sec.internal.google.SecImsNotifier r5 = com.sec.internal.google.SecImsNotifier.getInstance()
            r6 = 1
            java.lang.String r2 = r5.getRcsClientConfiguration(r2, r6)
            boolean r5 = android.text.TextUtils.isEmpty(r2)
            if (r5 == 0) goto L_0x0418
            java.lang.String r2 = "UP_2.3"
        L_0x0418:
            java.lang.String r5 = "[RCS_PROFILE]"
            java.lang.String r0 = r0.replace(r5, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            if (r4 == 0) goto L_0x0437
            java.lang.String r5 = "cc/"
            r2.append(r5)
            android.content.Context r1 = r1.mContext
            java.lang.String r4 = r4.getImsi()
            java.lang.String r1 = com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper.getConfigVersion(r1, r4)
            r2.append(r1)
        L_0x0437:
            java.lang.String r1 = r2.toString()
            java.lang.String r0 = r0.replace(r8, r1)
        L_0x043f:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "buildUserAgentString: isVoLteEnabled="
            r1.append(r2)
            r1.append(r3)
            java.lang.String r2 = ", sipUserAgent="
            r1.append(r2)
            r1.append(r0)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r15, r1)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManager.buildUserAgentString(com.sec.ims.settings.ImsProfile, java.lang.String, int):java.lang.String");
    }

    /* access modifiers changed from: protected */
    public void updateVceConfig(IRegisterTask iRegisterTask, boolean z) {
        this.mRegStackIf.updateVceConfig(iRegisterTask, z);
    }

    /* access modifiers changed from: protected */
    public void logTask() {
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            StringBuilder sb = new StringBuilder("RegisterTask(s): ");
            List<IRegisterTask> pendingRegistration = getPendingRegistration(i);
            if (CollectionUtils.isNullOrEmpty((Collection<?>) pendingRegistration)) {
                sb.append("Nothing!");
            } else {
                for (IRegisterTask next : pendingRegistration) {
                    sb.append(next.getProfile().getName());
                    sb.append(" (");
                    sb.append(next.getState());
                    if (next.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        sb.append(", rat = ");
                        sb.append(next.getRegistrationRat());
                        sb.append(", service = ");
                        sb.append((String) Optional.ofNullable(next.getImsRegistration()).map(new RegistrationManager$$ExternalSyntheticLambda1()).map(new RegistrationManager$$ExternalSyntheticLambda4()).orElse(""));
                    }
                    sb.append("), ");
                }
                IMSLog.i(IRegistrationManager.LOG_TAG, i, sb.toString().replaceAll(", $", ""));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updatePani(int i) {
        Optional.ofNullable(RegistrationUtils.getPendingRegistrationInternal(i)).ifPresent(new RegistrationManager$$ExternalSyntheticLambda2(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$updatePani$4(SlotBasedConfig.RegisterTaskList registerTaskList) {
        registerTaskList.forEach(new RegistrationManager$$ExternalSyntheticLambda8(this));
    }

    /* access modifiers changed from: protected */
    public void updatePani(RegisterTask registerTask) {
        ImsProfile profile = registerTask.getProfile();
        if (profile.hasEmergencySupport() || registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING) || !TextUtils.isEmpty(profile.getLastPaniHeader())) {
            int phoneId = registerTask.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "updatePani " + registerTask);
            this.mRegStackIf.updatePani(registerTask);
            if (!profile.hasEmergencySupport() && !RegistrationUtils.isCmcProfile(profile) && ImsProfile.hasRcsService(profile)) {
                String pani = registerTask.getPani();
                String lastPani = registerTask.getLastPani();
                if (!TextUtils.isEmpty(pani)) {
                    this.mSecImsServiceConnector.getSipTransportImpl(phoneId).onPaniUpdated(pani, lastPani);
                }
            }
        }
    }

    public void dump() {
        IMSLog.dump(IRegistrationManager.LOG_TAG, "Dump of RegistrationManager:");
        IMSLog.increaseIndent(IRegistrationManager.LOG_TAG);
        IMSLog.dump(IRegistrationManager.LOG_TAG, "GCF mode: [" + DeviceUtil.getGcfMode() + "]");
        IMSLog.dump(IRegistrationManager.LOG_TAG, "RegisterTask(s) -");
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                IMSLog.dump(IRegistrationManager.LOG_TAG, "SIM slot: [" + registerTask.getPhoneId() + "] state: [" + registerTask.getState() + "] IMS Profile: [" + registerTask.getProfile() + "]");
                StringBuilder sb = new StringBuilder();
                sb.append("Governor: ");
                sb.append(registerTask.getGovernor());
                IMSLog.dump(IRegistrationManager.LOG_TAG, sb.toString());
            }
        }
        this.mEventLog.dump();
        IMSLog.decreaseIndent(IRegistrationManager.LOG_TAG);
        this.mRegStackIf.dump();
    }

    /* access modifiers changed from: protected */
    public void reportRegistrationStatus(IRegisterTask iRegisterTask) {
        int phoneId = iRegisterTask.getPhoneId();
        ContentValues contentValues = new ContentValues();
        DiagnosisConstants.REGI_FRSN regi_frsn = DiagnosisConstants.REGI_FRSN.UNKNOWN;
        int code = regi_frsn.getCode();
        int lastRegiFailReason = iRegisterTask.getLastRegiFailReason();
        if (iRegisterTask.getUserAgent() != null) {
            SipError errorCode = iRegisterTask.getUserAgent().getErrorCode();
            if (errorCode != null) {
                code = errorCode.getCode();
            }
            int registrationRat = iRegisterTask.getRegistrationRat();
            ImsProfile profile = iRegisterTask.getProfile();
            Set set = (Set) Optional.ofNullable(iRegisterTask.getImsRegistration()).map(new RegistrationManager$$ExternalSyntheticLambda1()).orElse(new HashSet());
            RegistrationConstants.RegisterTaskState state = iRegisterTask.getState();
            RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.REGISTERED;
            if (state == registerTaskState && code == DiagnosisConstants.REGI_FRSN.OK.getCode()) {
                if (code != lastRegiFailReason) {
                    DiagnosisConstants.REGI_FRSN regi_frsn2 = DiagnosisConstants.REGI_FRSN.OK_AFTER_FAIL;
                    if (lastRegiFailReason != regi_frsn2.getCode()) {
                        code = regi_frsn2.getCode();
                    }
                }
                if (profile.hasService("mmtel", registrationRat) && !set.contains("mmtel")) {
                    code = iRegisterTask.getRegiFailReason();
                }
            } else if (iRegisterTask.getState() != registerTaskState) {
                if (iRegisterTask.getRegiFailReason() < DiagnosisConstants.REGI_FRSN.OFFSET_DEREGI_REASON.getCode()) {
                    contentValues.put(DiagnosisConstants.REGI_KEY_FAIL_COUNT, Integer.valueOf(iRegisterTask.getGovernor().getFailureCount()));
                } else {
                    code = iRegisterTask.getRegiFailReason();
                }
                iRegisterTask.setRegiFailReason(regi_frsn.getCode());
            }
            contentValues.put(DiagnosisConstants.REGI_KEY_DATA_RAT_TYPE, Integer.valueOf(registrationRat));
            contentValues.put(DiagnosisConstants.REGI_KEY_SERVICE_SET_ALL, DiagnosisConstants.convertServiceSetToHex(profile.getServiceSet(Integer.valueOf(registrationRat))));
            if (!set.isEmpty()) {
                contentValues.put(DiagnosisConstants.REGI_KEY_SERVICE_SET_REGISTERED, DiagnosisConstants.convertServiceSetToHex(set));
            }
            contentValues.put(DiagnosisConstants.REGI_KEY_PANI_PREFIX, Integer.valueOf(DiagnosisConstants.getPaniPrefix(iRegisterTask.getPani())));
            contentValues.put(DiagnosisConstants.REGI_KEY_PDN_TYPE, Integer.valueOf(DiagnosisConstants.getPdnType(profile.getPdn())));
            contentValues.put(DiagnosisConstants.REGI_KEY_PCSCF_ORDINAL, Integer.valueOf(iRegisterTask.getGovernor().getPcscfOrdinal()));
            contentValues.put("ROAM", Integer.valueOf(this.mPdnController.isDataRoaming(phoneId) ? 1 : 0));
            IVolteServiceModule iVolteServiceModule = this.mVsm;
            if (iVolteServiceModule != null) {
                contentValues.put(DiagnosisConstants.REGI_KEY_SIGNAL_STRENGTH, Integer.valueOf(Math.max(0, iVolteServiceModule.getSignalLevel())));
            }
        } else {
            code = iRegisterTask.getRegiFailReason();
        }
        contentValues.put(DiagnosisConstants.REGI_KEY_REQUEST_CODE, Integer.valueOf(iRegisterTask.getRegiRequestType().getCode()));
        contentValues.put(DiagnosisConstants.REGI_KEY_FAIL_REASON, Integer.valueOf(code));
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "reportRegiStatus: reason [" + code + "], prev [" + lastRegiFailReason + "]");
        if (code > regi_frsn.getCode() && code != DiagnosisConstants.REGI_FRSN.OK.getCode()) {
            ImsLogAgentUtil.sendLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_REGI, contentValues);
        }
        reportRcsChatRegistrationStatus(iRegisterTask);
        iRegisterTask.setLastRegiFailReason(code);
    }

    /* access modifiers changed from: protected */
    public void reportRcsChatRegistrationStatus(IRegisterTask iRegisterTask) {
        int i;
        if (ImsProfile.hasRcsService(iRegisterTask.getProfile())) {
            ContentValues contentValues = new ContentValues();
            int phoneId = iRegisterTask.getPhoneId();
            if (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                HashSet hashSet = new HashSet();
                hashSet.addAll((Collection) Optional.ofNullable(iRegisterTask.getImsRegistration()).map(new RegistrationManager$$ExternalSyntheticLambda1()).orElse(new HashSet()));
                if (hashSet.removeAll(Arrays.asList(ImsProfile.getChatServiceList()))) {
                    i = 2;
                } else if (hashSet.removeAll(Arrays.asList(ImsProfile.getRcsServiceList()))) {
                    i = 1;
                }
                contentValues.put(DiagnosisConstants.KEY_SEND_MODE, 1);
                contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 0);
                contentValues.put(DiagnosisConstants.DRCS_KEY_RCS_REGI_STATUS, Integer.valueOf(i));
                ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_DRCS, contentValues);
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "reportRcsRegiStatus: " + i);
            }
            i = 0;
            contentValues.put(DiagnosisConstants.KEY_SEND_MODE, 1);
            contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 0);
            contentValues.put(DiagnosisConstants.DRCS_KEY_RCS_REGI_STATUS, Integer.valueOf(i));
            ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_DRCS, contentValues);
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "reportRcsRegiStatus: " + i);
        }
    }

    /* access modifiers changed from: protected */
    public void reportRegistrationCount(IRegisterTask iRegisterTask) {
        StringBuilder sb = new StringBuilder("R");
        int pdnType = iRegisterTask.getPdnType();
        if (pdnType == -1 || pdnType == 0 || pdnType == 1) {
            sb.append("R");
        } else if (pdnType != 11) {
            int phoneId = iRegisterTask.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "reportRegistrationCount: PDN type [" + pdnType + "] - ignore!");
            return;
        } else {
            sb.append("G");
        }
        if (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            sb.append("S");
        } else {
            sb.append("F");
        }
        int registrationRat = iRegisterTask.getRegistrationRat();
        int networkClass = TelephonyManagerExt.getNetworkClass(registrationRat);
        if (networkClass == 4) {
            sb.append("N");
        } else if (networkClass == 3) {
            if (registrationRat != 18) {
                sb.append(DiagnosisConstants.RCSM_ORST_HTTP);
            } else if (this.mPdnController.getEpdgPhysicalInterface(iRegisterTask.getPhoneId()) == 2) {
                sb.append("C");
            } else {
                sb.append("W");
            }
        } else if (networkClass == 2 || networkClass == 1) {
            sb.append("L");
        } else {
            int phoneId2 = iRegisterTask.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, "reportRegistrationCount: rat [" + registrationRat + "] - ignore!");
        }
        if (DiagnosisConstants.REGI_COUNT_KEYS.contains(sb.toString())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
            contentValues.put(sb.toString(), 1);
            int phoneId3 = iRegisterTask.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId3, "reportRegistrationCount: key [" + sb + "]");
            ImsLogAgentUtil.storeLogToAgent(iRegisterTask.getPhoneId(), this.mContext, "DRPT", contentValues);
        }
    }

    /* access modifiers changed from: protected */
    public void reportDualImsStatus(int i) {
        int i2 = SimUtil.isDualIMS() ? getRegistrationInfoByPhoneId(1 - i) != null ? 2 : 1 : 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 2);
        contentValues.put(DiagnosisConstants.DRPT_KEY_DUAL_IMS_ACTIVE, Integer.valueOf(i2));
        ImsLogAgentUtil.storeLogToAgent(i, this.mContext, "DRPT", contentValues);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "reportDualImsStatus: " + i2);
    }

    public IUserAgent getUserAgent(int i) {
        return this.mRegStackIf.getUserAgent(i);
    }

    public IUserAgent getUserAgent(String str, int i) {
        return this.mRegStackIf.getUserAgent(str, i);
    }

    public IUserAgent getUserAgentByRegId(int i) {
        return this.mRegStackIf.getUserAgentByRegId(i);
    }

    public IUserAgent getUserAgentByImsi(String str, String str2) {
        return this.mRegStackIf.getUserAgentByImsi(str, str2);
    }

    public String getImsiByUserAgentHandle(int i) {
        return this.mRegStackIf.getImsiByUserAgentHandle(i);
    }

    public IUserAgent[] getUserAgentByPhoneId(int i, String str) {
        return this.mRegStackIf.getUserAgentByPhoneId(i, str);
    }

    public IUserAgent getUserAgentOnPdn(int i, int i2) {
        return this.mRegStackIf.getUserAgentOnPdn(i, i2);
    }

    public IUserAgent getUserAgent(String str) {
        return this.mRegStackIf.getUserAgent(str);
    }

    public String getImsiByUserAgent(IUserAgent iUserAgent) {
        return this.mRegStackIf.getImsiByUserAgent(iUserAgent);
    }

    public void forceNotifyToApp(int i) {
        IConfigModule iConfigModule = this.mConfigModule;
        if (iConfigModule != null && iConfigModule.isRcsEnabled(i)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "forceNotifyToApp");
            Intent intent = new Intent();
            intent.setAction(ImsConstants.Intents.ACTION_SERVICE_UP);
            intent.putExtra(ImsConstants.Intents.EXTRA_ANDORID_PHONE_ID, i);
            intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
            intent.addFlags(LogClass.SIM_EVENT);
            this.mContext.sendBroadcast(intent);
        }
    }

    public void sendDmState(int i, boolean z) {
        if (SimUtil.isDualIMS() && SimUtil.getActiveSubInfoCount() > 1) {
            this.mRegStackIf.sendDmState(i, z);
        }
    }

    public void removeE911RegiTimer() {
        if (this.mHandler.hasMessages(155)) {
            this.mHandler.removeMessages(155);
        }
    }

    public void updateEmergencyTaskByAuthFailure(int i) {
        IRegisterTask registeringEmergencyTask = getRegisteringEmergencyTask(i);
        if (registeringEmergencyTask != null) {
            if (registeringEmergencyTask.getResultMessage() != null) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isERegiAuthFailed", true);
                registeringEmergencyTask.getResultMessage().setData(bundle);
            }
            RegistrationUtils.sendEmergencyRegistrationFailed(registeringEmergencyTask);
        }
    }

    private IRegisterTask getRegisteringEmergencyTask(int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal == null) {
            return null;
        }
        Iterator it = pendingRegistrationInternal.iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            if (registerTask.getProfile().hasEmergencySupport() && registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
                return registerTask;
            }
        }
        return null;
    }
}
