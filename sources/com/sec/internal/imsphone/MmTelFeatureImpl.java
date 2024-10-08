package com.sec.internal.imsphone;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsConferenceState;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.SrvccCall;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsSmsImplBase;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsUt;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.PublishDialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imsphone.cmc.CmcCallSessionManager;
import com.sec.internal.imsphone.cmc.CmcImsCallSessionImpl;
import com.sec.internal.imsphone.cmc.CmcImsServiceUtil;
import com.sec.internal.imsphone.cmc.ICmcConnectivityController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class MmTelFeatureImpl extends MmTelFeature {
    private static final Map<Integer, String> CAP_TO_SERVICE;
    private static final String IMS_CALL_PERMISSION = "android.permission.ACCESS_IMS_CALL_SERVICE";
    private static final String LOG_TAG = "MmTelFeatureImpl";
    private static final int REGISTRATION_TECH_3G = 4;
    private static final Map<Integer, Integer> REG_TECH_TO_NET_TYPE;
    private static Map<Integer, ImsCallSessionImpl> mCallSessionList = new ConcurrentHashMap();
    private static CmcImsServiceUtil mCmcImsServiceUtil = null;
    private ImsCallSessionImpl mConferenceHost = null;
    private ICmcConnectivityController mConnectivityController;
    private Map<Integer, Bundle> mImsConferenceState = new HashMap();
    ImsEcbmImpl mImsEcbm;
    ImsSmsImpl mImsSmsImpl;
    IImsUt mImsUt;
    private boolean mIsInitialMerge = false;
    private boolean mIsReady = false;
    ImsMultiEndPointImpl mMultEndPoint;
    private final int mPhoneId;
    IRegistrationManager mRegistrationManager;
    private String mServiceUrn = "";
    public IVolteServiceModule mVolteServiceModule;

    static {
        HashMap hashMap = new HashMap();
        REG_TECH_TO_NET_TYPE = hashMap;
        hashMap.put(3, 20);
        hashMap.put(0, 13);
        hashMap.put(1, 18);
        hashMap.put(2, 18);
        HashMap hashMap2 = new HashMap();
        CAP_TO_SERVICE = hashMap2;
        hashMap2.put(1, "mmtel");
        hashMap2.put(2, "mmtel-video");
        hashMap2.put(8, "smsip");
    }

    public MmTelFeatureImpl(Context context, int i, Executor executor) {
        super(executor);
        this.mContext = context;
        this.mPhoneId = i;
        setFeatureState(2);
        this.mVolteServiceModule = ImsRegistry.getServiceModuleManager().getVolteServiceModule();
        this.mRegistrationManager = ImsRegistry.getRegistrationManager();
        this.mConnectivityController = ImsRegistry.getICmcConnectivityController();
        if (mCmcImsServiceUtil == null) {
            mCmcImsServiceUtil = new CmcImsServiceUtil(this, this.mConnectivityController, this.mVolteServiceModule);
        }
    }

    public boolean isReady() {
        return this.mIsReady;
    }

    public boolean queryCapabilityConfiguration(int i, int i2) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "queryCapabilityConfiguration");
        IMSLog.i(LOG_TAG, this.mPhoneId, "queryCapabilityConfiguration");
        try {
            int intValue = REG_TECH_TO_NET_TYPE.getOrDefault(Integer.valueOf(i2), 0).intValue();
            String orDefault = CAP_TO_SERVICE.getOrDefault(Integer.valueOf(i), NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
            if (NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(orDefault)) {
                return false;
            }
            return ImsRegistry.isServiceAvailable(orDefault, intValue, this.mPhoneId);
        } catch (RemoteException | NullPointerException e) {
            int i3 = this.mPhoneId;
            IMSLog.e(LOG_TAG, i3, "queryCapabilityConfiguration: failed: " + e.getMessage());
            return false;
        }
    }

    public void changeEnabledCapabilities(CapabilityChangeRequest capabilityChangeRequest, ImsFeature.CapabilityCallbackProxy capabilityCallbackProxy) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "changeEnabledCapabilities");
        IMSLog.i(LOG_TAG, this.mPhoneId, "changeEnabledCapabilities");
        if (capabilityChangeRequest != null) {
            MmTelFeature.MmTelCapabilities queryCapabilityStatus = queryCapabilityStatus();
            for (CapabilityChangeRequest.CapabilityPair capabilityPair : capabilityChangeRequest.getCapabilitiesToDisable()) {
                queryCapabilityStatus.removeCapabilities(capabilityPair.getCapability());
                int i = this.mPhoneId;
                IMSLog.s(LOG_TAG, i, "changeEnabledCapabilities: disabled capa = " + capabilityPair.getCapability());
            }
            for (CapabilityChangeRequest.CapabilityPair capabilityPair2 : capabilityChangeRequest.getCapabilitiesToEnable()) {
                queryCapabilityStatus.addCapabilities(capabilityPair2.getCapability());
                int i2 = this.mPhoneId;
                IMSLog.s(LOG_TAG, i2, "changeEnabledCapabilities: enabled capa = " + capabilityPair2.getCapability());
            }
            notifyCapabilitiesStatusChanged(queryCapabilityStatus);
        }
    }

    public ImsCallProfile createCallProfile(int i, int i2) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "createCallProfile");
        ImsCallProfile imsCallProfile = new ImsCallProfile(i, i2);
        ImsRegistration[] registrationInfoByPhoneId = this.mRegistrationManager.getRegistrationInfoByPhoneId(this.mPhoneId);
        if (registrationInfoByPhoneId != null) {
            int length = registrationInfoByPhoneId.length;
            int i3 = 0;
            while (true) {
                if (i3 >= length) {
                    break;
                }
                ImsRegistration imsRegistration = registrationInfoByPhoneId[i3];
                if (imsRegistration != null && imsRegistration.getImsProfile() != null && imsRegistration.hasVolteService() && (i == 2 || !imsRegistration.getImsProfile().hasEmergencySupport())) {
                    Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
                    imsCallProfile.setCallExtraBoolean("ResumeHostAndMerge", (fromName == Mno.VZW || fromName == Mno.USCC) ? false : true);
                    if (i != 2) {
                        imsCallProfile.setCallExtraBoolean("android.telephony.ims.extra.IS_CROSS_SIM_CALL", isCrossSimTech(imsRegistration));
                        if (imsRegistration.getImsProfile().getCmcType() == 0) {
                            imsCallProfile.setCallExtraInt("android.telephony.ims.extra.CALL_NETWORK_TYPE", imsRegistration.getCurrentRat());
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                i3++;
            }
        }
        return imsCallProfile;
    }

    private boolean isCrossSimTech(ImsRegistration imsRegistration) {
        return getRegistrationTech(imsRegistration.getCurrentRat(), imsRegistration.isEpdgOverCellularData()) == 2;
    }

    public static int getRegistrationTech(int i, boolean z) {
        if (NetworkUtil.is3gppPsVoiceNetwork(i)) {
            return 0;
        }
        if (TelephonyManagerExt.getNetworkClass(i) == 2) {
            return 4;
        }
        if (i == 18) {
            return z ? 2 : 1;
        }
        return -1;
    }

    public IImsCallSession createCallSessionInterface(ImsCallProfile imsCallProfile) throws RemoteException {
        int i;
        long clearCallingIdentity;
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "createCallSessionInterface");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            try {
                boolean z = true;
                CallProfile convertToSecCallProfile = DataTypeConvertor.convertToSecCallProfile(this.mPhoneId, imsCallProfile, iVolteServiceModule.getTtyMode(this.mPhoneId) != Extensions.TelecomManager.TTY_MODE_OFF);
                Bundle bundle = imsCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
                if (bundle != null) {
                    i = bundle.containsKey("com.samsung.telephony.extra.CMC_TYPE") ? bundle.getInt("com.samsung.telephony.extra.CMC_TYPE") : 0;
                    if (bundle.containsKey("com.samsung.ims.extra.ECHO_CALL_ID")) {
                        Log.i(LOG_TAG, "get Echo Call ID " + bundle.getString("com.samsung.ims.extra.ECHO_CALL_ID"));
                    }
                } else {
                    i = 0;
                }
                int prepareCallSession = mCmcImsServiceUtil.prepareCallSession(i, imsCallProfile, convertToSecCallProfile, this.mPhoneId);
                if (prepareCallSession > 0) {
                    return mCmcImsServiceUtil.createCallSession(prepareCallSession, imsCallProfile, convertToSecCallProfile);
                }
                ImsRegistration[] registrationInfoByPhoneId = this.mRegistrationManager.getRegistrationInfoByPhoneId(this.mPhoneId);
                if (registrationInfoByPhoneId != null) {
                    for (ImsRegistration imsRegistration : registrationInfoByPhoneId) {
                        if (!(imsRegistration == null || imsRegistration.getImsProfile() == null || !imsRegistration.hasVolteService())) {
                            if (prepareCallSession == imsRegistration.getImsProfile().getCmcType()) {
                                if (convertToSecCallProfile.getUrn() == "urn:service:unspecified") {
                                    if (this.mServiceUrn.isEmpty()) {
                                        convertToSecCallProfile.setUrn(ImsCallUtil.ECC_SERVICE_URN_DEFAULT);
                                    } else {
                                        convertToSecCallProfile.setUrn(this.mServiceUrn);
                                        this.mServiceUrn = "";
                                    }
                                }
                            }
                        }
                    }
                }
                convertToSecCallProfile.setCmcType(prepareCallSession);
                if (SimUtil.isSoftphoneEnabled() && convertToSecCallProfile.getCallType() == 7) {
                    convertToSecCallProfile.setCallType(13);
                    convertToSecCallProfile.setUrn((String) null);
                }
                Mno mno = SimUtil.getMno(this.mPhoneId);
                int volteRegHandle = getVolteRegHandle();
                if (!mno.isKor() && mno != Mno.VZW && mno != Mno.USCC && ImsRegistry.getCmcAccountManager().isSecondaryDevice() && volteRegHandle == -1 && TelephonyManagerWrapper.getInstance(this.mContext).isVoiceCapable() && prepareCallSession == 0 && !ImsCallUtil.isE911Call(convertToSecCallProfile.getCallType())) {
                    convertToSecCallProfile.setForceCSFB(true);
                }
                Log.i(LOG_TAG, "mno: " + mno + " phoneId: " + this.mPhoneId + " cmcType: " + prepareCallSession + " volteRegHandle: " + volteRegHandle + " isVoiceCap: " + TelephonyManagerWrapper.getInstance(this.mContext).isVoiceCapable() + " => isForceCSFB(): " + convertToSecCallProfile.isForceCSFB());
                clearCallingIdentity = Binder.clearCallingIdentity();
                com.sec.ims.volte2.IImsCallSession createSession = this.mVolteServiceModule.createSession(convertToSecCallProfile);
                Binder.restoreCallingIdentity(clearCallingIdentity);
                if (createSession != null) {
                    if (imsCallProfile.getCallExtraInt("android.telephony.ims.extra.CALL_NETWORK_TYPE") != 18) {
                        z = false;
                    }
                    createSession.setEpdgState(z);
                }
                ImsCallSessionImpl imsCallSessionImpl = new ImsCallSessionImpl(imsCallProfile, createSession, (IImsCallSessionListener) null, this);
                mCallSessionList.put(Integer.valueOf(imsCallSessionImpl.getCallIdInt()), imsCallSessionImpl);
                if (isEnabledWifiDirectFeature()) {
                    mCmcImsServiceUtil.acquireP2pNetwork();
                }
                return imsCallSessionImpl;
            } catch (RemoteException unused) {
                throw new UnsupportedOperationException();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(clearCallingIdentity);
                throw th;
            }
        } else {
            throw new RemoteException();
        }
    }

    public int getVolteRegHandle() {
        for (ImsRegistration imsRegistration : this.mRegistrationManager.getRegistrationInfo()) {
            if (imsRegistration != null && imsRegistration.getPhoneId() == this.mPhoneId && imsRegistration.hasVolteService() && imsRegistration.getImsProfile() != null && imsRegistration.getImsProfile().getCmcType() == 0) {
                return imsRegistration.getHandle();
            }
        }
        return -1;
    }

    public boolean isEnabledWifiDirectFeature() {
        return this.mConnectivityController.isEnabledWifiDirectFeature();
    }

    public IImsUt getUtInterface() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getUtInterface");
        if (this.mImsUt == null) {
            this.mImsUt = new ImsUtImpl(this.mPhoneId);
        }
        return this.mImsUt;
    }

    public IImsEcbm getEcbmInterface() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getEcbmInterface");
        if (this.mImsEcbm == null) {
            this.mImsEcbm = new ImsEcbmImpl();
        }
        return this.mImsEcbm;
    }

    public ImsEcbmImpl getImsEcbmImpl() throws RemoteException {
        return getEcbmInterface();
    }

    public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getMultiEndpointInterface");
        if (this.mMultEndPoint == null) {
            this.mMultEndPoint = new ImsMultiEndPointImpl(this.mPhoneId);
        }
        return this.mMultEndPoint;
    }

    public int getCmcTypeFromRegHandle(int i) {
        for (ImsRegistration imsRegistration : this.mRegistrationManager.getRegistrationInfo()) {
            if (imsRegistration != null && i == imsRegistration.getHandle() && imsRegistration.getImsProfile() != null) {
                return imsRegistration.getImsProfile().getCmcType();
            }
        }
        return -1;
    }

    public void setUiTtyMode(int i, Message message) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "setUiTtyMode");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.setUiTTYMode(this.mPhoneId, i, message);
        }
    }

    public void onFeatureReady() {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "onFeatureReady");
        IMSLog.i(LOG_TAG, this.mPhoneId, "onFeatureReady called!");
        this.mIsReady = true;
    }

    public void notifySrvccStarted(Consumer<List<SrvccCall>> consumer) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "notifySrvccStarted");
        if (this.mVolteServiceModule != null) {
            ArrayList arrayList = new ArrayList();
            for (Map.Entry<Integer, ImsCallSessionImpl> value : mCallSessionList.entrySet()) {
                ImsCallSessionImpl imsCallSessionImpl = (ImsCallSessionImpl) value.getValue();
                if (imsCallSessionImpl.mSession != null) {
                    arrayList.add(ImsCallUtil.convertImsCalltoSrvccCall(imsCallSessionImpl));
                }
            }
            consumer.accept(arrayList);
            this.mVolteServiceModule.onSrvccStateChange(this.mPhoneId, Call.SrvccState.STARTED);
        }
    }

    public void notifySrvccCompleted() {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "notifySrvccCompleted");
        Log.i(LOG_TAG, "notifySrvccCompleted()");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.onSrvccStateChange(this.mPhoneId, Call.SrvccState.COMPLETED);
        }
    }

    public void notifySrvccFailed() {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "notifySrvccFailed");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.onSrvccStateChange(this.mPhoneId, Call.SrvccState.FAILED);
        }
    }

    public void notifySrvccCanceled() {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "notifySrvccCanceled");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.onSrvccStateChange(this.mPhoneId, Call.SrvccState.CANCELED);
        }
    }

    public void onIncomingCall(int i, Bundle bundle) {
        Log.i(LOG_TAG, "onIncomingCall()");
        try {
            Log.e(LOG_TAG, "incoming call event");
            notifyIncomingCallSession(getPendingCallSession(Integer.toString(i)), bundle);
            Log.e(LOG_TAG, "incoming call notified");
        } catch (RemoteException unused) {
            Log.w(LOG_TAG, "onReceive: Couldn't get Incoming call session.");
        }
    }

    public IImsCallSession getPendingCallSession(String str) throws RemoteException {
        Mno mno;
        int i;
        ImsCallSessionImpl imsCallSessionImpl;
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getPendingCallSession");
        com.sec.ims.volte2.IImsCallSession pendingSession = this.mVolteServiceModule.getPendingSession(str);
        if (pendingSession == null) {
            throw new RemoteException("Session does not exist");
        } else if (!ImsCallUtil.isEndCallState(CallConstants.STATE.values()[pendingSession.getCallStateOrdinal()])) {
            CallProfile callProfile = pendingSession.getCallProfile();
            ImsCallProfile imsCallProfile = new ImsCallProfile(1, DataTypeConvertor.convertToGoogleCallType(callProfile.getCallType()), prepareComposerDataBundle(callProfile.getComposerData()), new ImsStreamMediaProfile());
            ImsRegistration registration = pendingSession.getRegistration();
            if (registration != null) {
                int currentRat = registration.getCurrentRat();
                if (callProfile.getRadioTech() != 0) {
                    currentRat = ServiceStateWrapper.rilRadioTechnologyToNetworkType(callProfile.getRadioTech());
                }
                if (ImsCallUtil.isCmcSecondaryType(pendingSession.getCmcType())) {
                    imsCallProfile.setCallExtraInt("android.telephony.ims.extra.CALL_NETWORK_TYPE", 13);
                } else {
                    imsCallProfile.setCallExtraInt("android.telephony.ims.extra.CALL_NETWORK_TYPE", currentRat);
                }
                imsCallProfile.setCallExtraBoolean("android.telephony.ims.extra.IS_CROSS_SIM_CALL", isCrossSimTech(registration));
                pendingSession.setEpdgStateNoNotify(currentRat == 18);
                mno = Mno.fromName(registration.getImsProfile().getMnoName());
            } else {
                mno = null;
            }
            imsCallProfile.setCallExtra("oi", callProfile.getDialingNumber());
            imsCallProfile.mMediaProfile = DataTypeConvertor.convertToGoogleMediaProfile(callProfile.getMediaProfile());
            if (mno == Mno.DOCOMO) {
                i = DataTypeConvertor.getOirExtraFromDialingNumberForDcm(callProfile.getLetteringText());
            } else {
                String dialingNumber = callProfile.getDialingNumber();
                if (TextUtils.isEmpty(dialingNumber)) {
                    dialingNumber = NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
                }
                i = DataTypeConvertor.getOirExtraFromDialingNumber(dialingNumber);
            }
            imsCallProfile.setCallExtraInt("oir", i);
            imsCallProfile.setCallExtraInt("cnap", i);
            imsCallProfile.setCallExtra("cna", callProfile.getLetteringText());
            imsCallProfile.setCallExtra("com.samsung.telephony.extra.PHOTO_RING_AVAILABLE", callProfile.getPhotoRing());
            imsCallProfile.setCallExtraBoolean("com.samsung.telephony.extra.IS_TWO_PHONE_MODE", !TextUtils.isEmpty(callProfile.getNumberPlus()));
            imsCallProfile.setCallExtraBoolean("com.samsung.telephony.extra.MT_CONFERENCE", "1".equals(callProfile.getIsFocus()));
            imsCallProfile.setCallExtraStringArray("android.telephony.ims.extra.FORWARDED_NUMBER", new String[]{callProfile.getNumberPlus()});
            imsCallProfile.setCallExtra("com.samsung.telephony.extra.ALERT_INFO", callProfile.getAlertInfo());
            imsCallProfile.mMediaProfile.setRttMode(callProfile.getMediaProfile().getRttMode());
            if (callProfile.getHistoryInfo() != null) {
                imsCallProfile.setCallExtraStringArray("android.telephony.ims.extra.FORWARDED_NUMBER", new String[]{callProfile.getHistoryInfo()});
                if ("anonymous".equalsIgnoreCase(callProfile.getHistoryInfo())) {
                    imsCallProfile.setCallExtra("com.samsung.telephony.extra.CALL_FORWARDING_PRESENTATION", "1");
                } else {
                    imsCallProfile.setCallExtra("com.samsung.telephony.extra.CALL_FORWARDING_PRESENTATION", "0");
                }
            }
            if (callProfile.getVerstat() != null) {
                imsCallProfile.setCallExtra("com.samsung.telephony.extra.ims.VERSTAT", callProfile.getVerstat());
                if (callProfile.getVerstat().equals("TN-Validation-Passed")) {
                    imsCallProfile.setCallerNumberVerificationStatus(1);
                } else if (callProfile.getVerstat().equals("TN-Validation-Failed")) {
                    imsCallProfile.setCallerNumberVerificationStatus(2);
                } else {
                    imsCallProfile.setCallerNumberVerificationStatus(0);
                }
            } else {
                imsCallProfile.setCallerNumberVerificationStatus(0);
            }
            if (callProfile.getOrganization() != null) {
                imsCallProfile.setCallExtra("com.samsung.telephony.extra.ims.ORG", callProfile.getOrganization());
            }
            if (callProfile.getHDIcon() == 1) {
                imsCallProfile.mRestrictCause = 0;
            } else {
                imsCallProfile.mRestrictCause = 3;
            }
            if (callProfile.getEchoCallId() != null) {
                imsCallProfile.setCallExtra("com.samsung.ims.extra.ECHO_CALL_ID", callProfile.getEchoCallId());
                imsCallProfile.setCallExtraBoolean("com.samsung.ims.extra.EPSFB_SUCCESS", callProfile.getEPSFBsuccess());
            }
            if (callProfile.getEchoCellId() != null) {
                imsCallProfile.setCallExtra("com.samsung.ims.extra.ECHO_CELL_ID", callProfile.getEchoCellId());
            }
            if (!callProfile.isVideoCRBT()) {
                imsCallProfile.setCallExtraBoolean("com.samsung.telephony.extra.VIDEO_CRBT", false);
                imsCallProfile.setCallExtraInt("com.samsung.telephony.extra.VIDEO_CRT_MT", ImsConstants.VcrtPost.NO_VCRT);
            } else if (callProfile.getDirection() == 0) {
                imsCallProfile.setCallExtraBoolean("com.samsung.telephony.extra.VIDEO_CRBT", true);
            } else if (callProfile.getDirection() == 1) {
                int i2 = callProfile.getDelayRinging() ? ImsConstants.VcrtPost.NO_VCRT : ImsConstants.VcrtPost.VCRT_AVAILABLE;
                imsCallProfile.setCallExtraInt("com.samsung.telephony.extra.VIDEO_CRT_MT", i2);
                if (i2 == ImsConstants.VcrtPost.VCRT_AVAILABLE) {
                    Log.i(LOG_TAG, "setVideoCrtAudio with false in default");
                    setVideoCrtAudio(this.mPhoneId, false);
                }
            }
            if (callProfile.getDtmfEvent() != null) {
                imsCallProfile.setCallExtra("com.samsung.telephony.extra.DTMF_EVENT", callProfile.getDtmfEvent());
            }
            if (pendingSession.isQuantumEncryptionServiceAvailable()) {
                Log.i(LOG_TAG, "update extra com.samsung.telephony.extra.QUANTUM_ENCRYPTION_STATUS: " + callProfile.getQuantumSecurityInfo().getEncryptStatus());
                imsCallProfile.setCallExtraInt("com.samsung.telephony.extra.QUANTUM_ENCRYPTION_STATUS", callProfile.getQuantumSecurityInfo().getEncryptStatus());
            }
            mCmcImsServiceUtil.getPendingCallSession(this.mPhoneId, imsCallProfile, pendingSession);
            if (pendingSession.getCmcType() > 0) {
                Log.d(LOG_TAG, "getPendingCallSession, create imsCallSessionImpl for [CMC+D2D volte call]");
                imsCallSessionImpl = new CmcImsCallSessionImpl(imsCallProfile, new CmcCallSessionManager(pendingSession, this.mConnectivityController, this.mVolteServiceModule), (IImsCallSessionListener) null, this);
            } else {
                Log.d(LOG_TAG, "getPendingCallSession, create imsCallSessionImpl for [NORMAL volte call]");
                imsCallSessionImpl = new ImsCallSessionImpl(imsCallProfile, pendingSession, (IImsCallSessionListener) null, this);
            }
            mCallSessionList.put(Integer.valueOf(imsCallSessionImpl.getCallIdInt()), imsCallSessionImpl);
            if (this.mVolteServiceModule.isNotifyRejectedCall(this.mPhoneId) && callProfile.getRejectCause() != 0) {
                imsCallSessionImpl.mCallProfile.setCallExtra("android.telephony.ims.extra.CALL_DISCONNECT_CAUSE", Integer.toString(callProfile.getRejectCause()));
                imsCallSessionImpl.terminate(callProfile.getRejectCause());
            }
            return imsCallSessionImpl;
        } else {
            throw new RemoteException();
        }
    }

    public Bundle prepareComposerDataBundle(Bundle bundle) {
        Bundle bundle2 = new Bundle();
        if (bundle != null && !bundle.isEmpty()) {
            if (bundle.containsKey("importance")) {
                bundle2.putBoolean(ImsConstants.Intents.EXTRA_CALL_IMPORTANCE, bundle.getBoolean("importance"));
            }
            if (bundle.containsKey(CallConstants.ComposerData.IMAGE)) {
                bundle2.putString(ImsConstants.Intents.EXTRA_CALL_IMAGE, bundle.getString(CallConstants.ComposerData.IMAGE));
            }
            if (bundle.containsKey("subject")) {
                bundle2.putString(ImsConstants.Intents.EXTRA_CALL_SUBJECT, bundle.getString("subject"));
            }
            if (bundle.containsKey(CallConstants.ComposerData.LONGITUDE)) {
                bundle2.putString(ImsConstants.Intents.EXTRA_CALL_LONGITUDE, bundle.getString(CallConstants.ComposerData.LONGITUDE));
            }
            if (bundle.containsKey(CallConstants.ComposerData.LATITUDE)) {
                bundle2.putString(ImsConstants.Intents.EXTRA_CALL_LATITUDE, bundle.getString(CallConstants.ComposerData.LATITUDE));
            }
            if (bundle.containsKey(CallConstants.ComposerData.RADIUS)) {
                bundle2.putString(ImsConstants.Intents.EXTRA_CALL_RADIUS, bundle.getString(CallConstants.ComposerData.RADIUS));
            }
            if (bundle.containsKey(CallConstants.ComposerData.CALL_REASON)) {
                bundle2.putString(ImsConstants.Intents.EXTRA_CALL_CALLREASON, bundle.getString(CallConstants.ComposerData.CALL_REASON));
            }
        }
        return bundle2;
    }

    public void initImsSmsImplAdapter() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "initImsSmsImplAdapter");
        Log.d("MmTelFeatureCompat", "initImsSmsImplAdapter[" + this.mPhoneId + "]");
        this.mImsSmsImpl = null;
    }

    public ImsSmsImplBase getSmsImplementation() {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getSmsImplementation");
        if (this.mImsSmsImpl == null) {
            this.mImsSmsImpl = new ImsSmsImpl(this.mPhoneId);
        }
        return this.mImsSmsImpl;
    }

    public void changeAudioPath(int i, int i2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "changeAudioPath");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.updateAudioInterface(i, i2);
            return;
        }
        throw new RemoteException();
    }

    public int startLocalRingBackTone(int i, int i2, int i3) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "startLocalRingBackTone");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            return iVolteServiceModule.startLocalRingBackTone(i, i2, i3);
        }
        throw new RemoteException();
    }

    public int stopLocalRingBackTone() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "stopLocalRingBackTone");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            return iVolteServiceModule.stopLocalRingBackTone();
        }
        throw new RemoteException();
    }

    public void setVideoCrtAudio(int i, boolean z) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "setVideoCrtAudio");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.setVideoCrtAudio(i, z);
            return;
        }
        throw new RemoteException();
    }

    public void sendDtmfEvent(int i, String str) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "sendDtmfEvent");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.sendDtmfEvent(i, str);
            return;
        }
        throw new RemoteException();
    }

    public String getTrn(String str, String str2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getTrn");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            return iVolteServiceModule.getTrn(str, str2);
        }
        throw new RemoteException();
    }

    public void sendPublishDialog(int i, PublishDialog publishDialog) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "sendPublishDialog");
        if (this.mVolteServiceModule != null) {
            mCmcImsServiceUtil.sendPublishDialog(i, publishDialog);
            return;
        }
        throw new RemoteException();
    }

    public boolean isCmcEmergencyCallSupported(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "isCmcEmergencyCallSupported");
        if (ImsRegistry.getCmcAccountManager() != null) {
            return ImsRegistry.getCmcAccountManager().isEmergencyCallSupported();
        }
        throw new RemoteException();
    }

    public void triggerAutoConfigurationForApp(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "triggerAutoConfigurationForApp");
        ImsRegistry.triggerAutoConfigurationForApp(i);
    }

    public void setTtyMode(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "setTtyMode");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.setTtyMode(this.mPhoneId, i);
            return;
        }
        throw new RemoteException();
    }

    public void notifyEpsFallbackResult(int i, int i2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "notifyEpsFallbackResult");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.notifyEpsFallbackResult(this.mPhoneId, i2);
            return;
        }
        throw new RemoteException();
    }

    public void onTriggerEpsFallback(int i) throws RemoteException {
        Log.i(LOG_TAG, "onTriggerEpsFallback()");
        triggerEpsFallback(i);
        Log.i(LOG_TAG, "triggerEpsFallback notified");
    }

    public int getInitialCallNetworkType(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getInitialCallNetworkType");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule == null) {
            throw new RemoteException();
        } else if (iVolteServiceModule.getE911CallCount(i) != 0) {
            return 0;
        } else {
            int i2 = 0;
            for (ImsRegistration imsRegistration : this.mRegistrationManager.getRegistrationInfo()) {
                if (imsRegistration != null && imsRegistration.getPhoneId() == this.mPhoneId && imsRegistration.hasVolteService() && imsRegistration.getImsProfile() != null && imsRegistration.getImsProfile().getCmcType() == 0) {
                    i2 = REG_TECH_TO_NET_TYPE.getOrDefault(Integer.valueOf(getRegistrationTech(imsRegistration.getCurrentRat(), imsRegistration.isEpdgOverCellularData())), 0).intValue();
                }
            }
            return i2;
        }
    }

    public CmcImsServiceUtil getCmcImsServiceUtil() {
        return mCmcImsServiceUtil;
    }

    public ICmcConnectivityController.DeviceType getDeviceType() {
        return this.mConnectivityController.getDeviceType();
    }

    public void setCallSession(int i, ImsCallSessionImpl imsCallSessionImpl) {
        mCallSessionList.put(Integer.valueOf(i), imsCallSessionImpl);
    }

    public ImsCallSessionImpl getCallSession(int i) {
        return mCallSessionList.get(Integer.valueOf(i));
    }

    public void onCallClosed(int i) {
        mCallSessionList.remove(Integer.valueOf(i));
    }

    public ImsCallSessionImpl getConferenceHost() {
        return this.mConferenceHost;
    }

    public void setConferenceHost(ImsCallSessionImpl imsCallSessionImpl) {
        this.mConferenceHost = imsCallSessionImpl;
        if (imsCallSessionImpl == null) {
            this.mImsConferenceState.clear();
        }
    }

    public boolean hasConferenceHost() {
        return this.mConferenceHost != null;
    }

    public void putConferenceState(int i, String str, String str2, String str3, ImsCallProfile imsCallProfile, String str4) {
        this.mImsConferenceState.put(Integer.valueOf(i), getConfStateBundle(i, str, str2, str3, imsCallProfile, str4));
    }

    private Bundle getConfStateBundle(int i, String str, String str2, String str3, ImsCallProfile imsCallProfile) {
        return getConfStateBundle(i, str, str2, str3, imsCallProfile, "");
    }

    private Bundle getConfStateBundle(int i, String str, String str2, String str3, ImsCallProfile imsCallProfile, String str4) {
        Bundle bundle = new Bundle();
        bundle.putString("user", str);
        bundle.putString("endpoint", str2);
        bundle.putString("status", str3);
        bundle.putString("display-text", str4);
        bundle.putInt("callId", i);
        bundle.putString("cna", imsCallProfile.getCallExtra("cna"));
        bundle.putInt("cnap", imsCallProfile.getCallExtraInt("cnap"));
        bundle.putInt("oir", imsCallProfile.getCallExtraInt("oir"));
        bundle.putInt("audioQuality", imsCallProfile.getMediaProfile().getAudioQuality());
        bundle.putBoolean("com.samsung.telephony.extra.MT_CONFERENCE", imsCallProfile.getCallExtraBoolean("com.samsung.telephony.extra.MT_CONFERENCE"));
        bundle.putString("com.samsung.telephony.extra.ims.VERSTAT", imsCallProfile.getCallExtra("com.samsung.telephony.extra.ims.VERSTAT"));
        Log.i(LOG_TAG, "confState : " + IMSLog.checker(bundle.toString()));
        return bundle;
    }

    public void putConferenceStateList(int i, int i2, String str, String str2, String str3, int i3, ImsCallProfile imsCallProfile) {
        Bundle confStateBundle = getConfStateBundle(i2, str, str2, str3, imsCallProfile);
        confStateBundle.putInt("sipError", i3);
        confStateBundle.putString("uriType", "tel");
        if ("disconnected".equals(str3)) {
            confStateBundle.putInt("disconnectCause", 2);
        }
        this.mImsConferenceState.put(Integer.valueOf(i), confStateBundle);
    }

    public void removeConferenceState(int i) {
        this.mImsConferenceState.remove(Integer.valueOf(i));
    }

    public void clearConferenceStateList() {
        this.mImsConferenceState.clear();
    }

    public ImsConferenceState getImsConferenceState() {
        ImsConferenceState imsConferenceState = new ImsConferenceState();
        for (Map.Entry next : this.mImsConferenceState.entrySet()) {
            imsConferenceState.mParticipants.put(((Integer) next.getKey()).toString(), (Bundle) next.getValue());
        }
        return imsConferenceState;
    }

    public void updateSecConferenceInfo(ImsCallProfile imsCallProfile) {
        Bundle bundle;
        Bundle bundle2 = new Bundle();
        Bundle bundle3 = imsCallProfile.mCallExtras.getBundle("secConferenceInfo");
        for (Map.Entry next : this.mImsConferenceState.entrySet()) {
            Integer num = (Integer) next.getKey();
            Bundle bundle4 = (Bundle) next.getValue();
            CallProfile callProfile = null;
            if (bundle3 != null) {
                Iterator it = bundle3.keySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    String str = (String) it.next();
                    if (TextUtils.equals(str, num.toString())) {
                        bundle = bundle3.getBundle(str);
                        break;
                    }
                }
            }
            bundle = null;
            if (bundle != null) {
                bundle.putAll(bundle4);
                bundle2.putBundle(num.toString(), bundle);
            } else {
                ImsCallSessionImpl callSession = getCallSession(num.intValue());
                int i = bundle4.getInt("callId");
                if (i > 0) {
                    callSession = getCallSession(i);
                }
                if (callSession != null) {
                    bundle4.putString("cna", callSession.getCallProfile().getCallExtra("cna"));
                    try {
                        com.sec.ims.volte2.IImsCallSession sessionByCallId = this.mVolteServiceModule.getSessionByCallId(num.intValue());
                        if (sessionByCallId != null) {
                            callProfile = sessionByCallId.getCallProfile();
                        }
                    } catch (RemoteException unused) {
                    }
                    if (callProfile != null && !TextUtils.isEmpty(callProfile.getVerstat())) {
                        bundle4.putString("com.samsung.telephony.extra.ims.VERSTAT", callProfile.getVerstat());
                    }
                    bundle2.putBundle(num.toString(), bundle4);
                }
            }
        }
        imsCallProfile.mCallExtras.putBundle("secConferenceInfo", bundle2);
    }

    public int getParticipantId(String str) {
        try {
            int parseInt = Integer.parseInt(str);
            if (this.mImsConferenceState.containsKey(Integer.valueOf(parseInt))) {
                return parseInt;
            }
        } catch (IllegalArgumentException unused) {
        }
        for (Map.Entry next : this.mImsConferenceState.entrySet()) {
            if (str.equals(((Bundle) next.getValue()).getString("user"))) {
                return ((Integer) next.getKey()).intValue();
            }
        }
        return -1;
    }

    public void updateParticipant(int i, String str) {
        updateParticipant(i, (String) null, (String) null, str, -1);
    }

    public void updateParticipant(int i, String str, int i2) {
        updateParticipant(i, (String) null, (String) null, str, i2);
    }

    public void updateParticipant(int i, String str, String str2, String str3, int i2) {
        Bundle bundle = this.mImsConferenceState.get(Integer.valueOf(i));
        if (bundle != null) {
            if (!TextUtils.isEmpty(str)) {
                bundle.putString("user", str);
            }
            if (!TextUtils.isEmpty(str2)) {
                bundle.putString("endpoint", str2);
            }
            if (!TextUtils.isEmpty(str3)) {
                bundle.putString("status", str3);
            }
            if (i2 != -1) {
                bundle.putInt("android.telephony.ims.extra.CALL_DISCONNECT_CAUSE", i2);
            }
            this.mImsConferenceState.replace(Integer.valueOf(i), bundle);
        }
    }

    public void setInitialMerge(boolean z) {
        this.mIsInitialMerge = z;
    }

    public boolean isInitialMerge() {
        return this.mIsInitialMerge;
    }

    public void setServiceUrn(String str) {
        this.mServiceUrn = str;
    }

    public void preparePushCall(DialogEvent dialogEvent) throws RemoteException {
        Log.i(LOG_TAG, "preparePushCall(), size: " + mCallSessionList.size());
        if (dialogEvent == null && mCallSessionList.size() > 0) {
            Log.i(LOG_TAG, "Push for [PD]");
            for (Map.Entry<Integer, ImsCallSessionImpl> value : mCallSessionList.entrySet()) {
                ImsCallSessionImpl imsCallSessionImpl = (ImsCallSessionImpl) value.getValue();
                com.sec.ims.volte2.IImsCallSession iImsCallSession = imsCallSessionImpl.mSession;
                if (iImsCallSession != null && ImsCallUtil.isP2pPrimaryType(iImsCallSession.getCmcType())) {
                    imsCallSessionImpl.mListener.callSessionResumeFailed(new ImsReasonInfo(6007, 6007));
                    return;
                }
            }
        } else if (dialogEvent != null) {
            Log.i(LOG_TAG, "Push for [SD]");
            this.mMultEndPoint.setP2pPushDialogInfo(dialogEvent, getCmcTypeFromRegHandle(dialogEvent.getRegId()));
        }
    }
}
