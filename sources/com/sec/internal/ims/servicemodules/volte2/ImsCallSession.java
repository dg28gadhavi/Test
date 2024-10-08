package com.sec.internal.ims.servicemodules.volte2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;
import com.sec.ims.Dialog;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.IImsMediaCallProvider;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.ims.volte2.data.QuantumSecurityInfo;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallParams;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.imsdc.IdcImsCallSessionData;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.volte2.data.DefaultCallProfileBuilder;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import com.sec.sve.generalevent.VcidEvent;
import com.voltecrypt.service.SxRequestPeerProfileEntity;
import com.voltecrypt.service.SxRequestQMKeyEntity;
import java.util.Arrays;

public class ImsCallSession extends IImsCallSession.Stub {
    public static final int CALL_FORWARD_REQUEST_MARK_INCREMENT = 10;
    public static final String EVENT_NOTIFY_DSDA_VIDEO_CAPA = "NotifyDSDAVideoCapa";
    public static final String EVENT_NOTIFY_QUANTUM_ENCRYPTION_STATUS = "NotifyQuantumEncryptionStatus";
    public static final String EVENT_VCID_FAILURE = "VCIDGeneralFailure";
    protected static final int EVT_CURRENT_LOCATION_DISCOVERY_DURING_EMERGENCY_CALL = 8;
    protected static final int EVT_IMSDC_EVENT = 6;
    protected static final int EVT_IMS_CALL_EVENT = 1;
    protected static final int EVT_IMS_MEDIA_EVENT = 2;
    protected static final int EVT_REFER_STATUS = 5;
    protected static final int EVT_RRC_CONNECTION_EVENT = 3;
    protected static final int EVT_USSD_EVENT = 4;
    protected static final int EVT_VCS_EVENT = 7;
    protected static final int IMSDC_UPDATE_TELECOM_CALLID = 1;
    public static final int QUANTUM_KEY_RETRY_DELAY = 500;
    protected static final String TELECOM_EVENT_PREFIX = "com.samsung.telecom.IMS.EVENT";
    protected static final String TELEPHONY_EVENT_PREFIX = "com.samsung.telephony.event.";
    protected static final int VCS_TOUCH_SCREEN = 1;
    protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    /* access modifiers changed from: private */
    public String LOG_TAG = "ImsCallSession";
    protected PreciseAlarmManager mAm = null;
    protected int mCallId = -1;
    protected CallProfile mCallProfile;
    protected int mCmcType = 0;
    protected Context mContext = null;
    protected IDiagnosisController mDiagnosisController = null;
    protected Surface mDisplaySurface = null;
    private int mDtmfCode = 0;
    private int mEndReason = 0;
    private int mEndType = 0;
    protected boolean mEpsFallback = false;
    protected boolean mForwarded = false;
    protected boolean mHandOffTimedOut = false;
    private IdcImsCallSessionData mIdcData = null;
    protected ImsCallDedicatedBearer mImsCallDedicatedBearer = null;
    protected ImsCallSessionEventHandler mImsCallSessionEventHandler = null;
    protected boolean mIsEstablished = false;
    protected boolean mIsNrSaMode = false;
    private boolean mIsPeerProfileRetried = false;
    private boolean mIsUsingCamera = false;
    protected KeepAliveSender mKaSender = null;
    private int mKeyRetryCnt = 6;
    protected int mLastUsedCamera = -1;
    protected final RemoteCallbackList<IImsCallSessionEventListener> mListeners = new RemoteCallbackList<>();
    protected boolean mLocalHoldTone = true;
    protected final Looper mLooper;
    protected IImsMediaController mMediaController = null;
    protected Mno mMno = Mno.DEFAULT;
    protected CallProfile mModifyRequestedProfile = null;
    protected IVolteServiceModuleInternal mModule = null;
    protected boolean mOldLocalHoldTone = true;
    protected int mPhoneId = 0;
    protected int mPrevUsedCamera = -1;
    protected ImsRegistration mRegistration;
    protected IRegistrationManager mRegistrationManager = null;
    private boolean mRelayChTerminated = false;
    private int mRequestMark = 0;
    protected int mResumeCallRetriggerTimer = 0;
    protected int mSessionId = -1;
    protected boolean mSrvccStarted = false;
    public long mStartTime = 0;
    private final ITelephonyManager mTelephonyManager;
    protected int[] mTimerState;
    private boolean mUserCameraOff = false;
    protected Handler mUssdStackEventHandler = null;
    private int mVideoCrbtSupportType = 0;
    protected Handler mVolteStackEventHandler = null;
    protected IVolteServiceInterface mVolteSvcIntf = null;
    protected CallStateMachine smCallStateMachine = null;

    public int acceptECTRequest() throws RemoteException {
        return -1;
    }

    public void recording(int i, String str) throws RemoteException {
    }

    public int rejectECTRequest() throws RemoteException {
        return -1;
    }

    public void setMute(boolean z) throws RemoteException {
    }

    public int startECT(int i, String str) throws RemoteException {
        return -1;
    }

    public void setDedicatedBearerState(int i, int i2) {
        this.mImsCallDedicatedBearer.setDedicatedBearerState(i, i2);
    }

    public int getDedicatedBearerState(int i) {
        return this.mImsCallDedicatedBearer.getDedicatedBearerState(i);
    }

    public void setPreAlerting() {
        this.smCallStateMachine.setPreAlerting();
    }

    public boolean getPreAlerting() {
        return this.smCallStateMachine.getPreAlerting();
    }

    public void onReceiveSIPMSG(String str, boolean z) {
        if (!TextUtils.isEmpty(str)) {
            this.smCallStateMachine.onReceiveSIPMSG(str, z);
        }
    }

    /* access modifiers changed from: protected */
    public void notifySessionChanged(int i) {
        int beginBroadcast = this.mListeners.beginBroadcast();
        String str = this.LOG_TAG;
        Log.i(str, "onSessionChanged callId=" + i + ":" + beginBroadcast);
        for (int i2 = 0; i2 < beginBroadcast; i2++) {
            try {
                this.mListeners.getBroadcastItem(i2).onSessionChanged(i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyImsGeneralEvent(String str, Bundle bundle) {
        int beginBroadcast = this.mListeners.beginBroadcast();
        String str2 = this.LOG_TAG;
        Log.i(str2, "notifyImsGeneralEvent: " + str + ", extras=" + bundle);
        if (this.mCallProfile != null && "VCIDGeneralFailure".equals(str)) {
            this.mCallProfile.setAlertInfo("");
        }
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onImsGeneralEvent(str, bundle);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyRetryingVoLteOrCsCall(int i) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyRetryingVoLteOrCsCall: " + i);
        this.smCallStateMachine.sendMessage(1000, i);
    }

    public ImsCallSession(Context context, CallProfile callProfile, ImsRegistration imsRegistration, Looper looper, IVolteServiceModuleInternal iVolteServiceModuleInternal) {
        this.mContext = context;
        this.mModule = iVolteServiceModuleInternal;
        this.mCallProfile = callProfile;
        this.mRegistration = imsRegistration;
        this.mLooper = looper;
        this.mPhoneId = callProfile.getPhoneId();
        this.mAm = PreciseAlarmManager.getInstance(context);
        this.mVolteStackEventHandler = new Handler(looper) {
            public void handleMessage(Message message) {
                ImsRegistration imsRegistration;
                super.handleMessage(message);
                switch (message.what) {
                    case 1:
                        ImsCallSession.this.onImsCallEvent((CallStateEvent) ((AsyncResult) message.obj).result);
                        return;
                    case 2:
                        ImsCallSession.this.mImsCallSessionEventHandler.onImsMediaEvent((IMSMediaEvent) message.obj);
                        return;
                    case 3:
                        RrcConnectionEvent rrcConnectionEvent = (RrcConnectionEvent) ((AsyncResult) message.obj).result;
                        String r0 = ImsCallSession.this.LOG_TAG;
                        Log.i(r0, "rrcEvent.getEvent() : " + rrcConnectionEvent.getEvent());
                        ImsCallSession imsCallSession = ImsCallSession.this;
                        if (imsCallSession.mMno == Mno.VZW && (imsRegistration = imsCallSession.mRegistration) != null && imsRegistration.getImsProfile().getNeedVoLteRetryInNr() && rrcConnectionEvent.getEvent() == RrcConnectionEvent.RrcEvent.REJECTED && ImsCallSession.this.mRegistration.getRegiRat() == 20) {
                            ImsCallSession.this.removeCallStateMachineMessage(301);
                            CallStateMachine callStateMachine = ImsCallSession.this.smCallStateMachine;
                            callStateMachine.sipReason = callStateMachine.getSipReasonFromUserReason(23);
                            SecImsNotifier.getInstance().onTriggerEpsFallback(ImsCallSession.this.getPhoneId(), 2);
                            return;
                        }
                        Mno mno = ImsCallSession.this.mMno;
                        if (mno != Mno.VZW && mno != Mno.DOCOMO && mno != Mno.SWISSCOM) {
                            return;
                        }
                        if (rrcConnectionEvent.getEvent() == RrcConnectionEvent.RrcEvent.REJECTED || rrcConnectionEvent.getEvent() == RrcConnectionEvent.RrcEvent.TIMER_EXPIRED) {
                            ImsCallSession.this.smCallStateMachine.sendMessage(401);
                            return;
                        }
                        return;
                    case 5:
                        ImsCallSession.this.mImsCallSessionEventHandler.onReferStatus((AsyncResult) message.obj);
                        return;
                    case 6:
                        ImsCallSession.this.handleImsDcEvent(message.arg1, (Bundle) message.obj);
                        return;
                    case 7:
                        if (message.arg1 == 1) {
                            ImsCallSession imsCallSession2 = ImsCallSession.this;
                            imsCallSession2.mVolteSvcIntf.sendVcsInfo(imsCallSession2.getSessionId(), (Bundle) message.obj);
                            return;
                        }
                        return;
                    case 8:
                        ImsCallSession imsCallSession3 = ImsCallSession.this;
                        imsCallSession3.mImsCallSessionEventHandler.onCurrentLocationDiscoveryDuringEmergencyCall(imsCallSession3.mSessionId, (AsyncResult) message.obj);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mUssdStackEventHandler = new Handler(looper) {
            public void handleMessage(Message message) {
                super.handleMessage(message);
                AsyncResult asyncResult = (AsyncResult) message.obj;
                if (message.what == 4) {
                    ImsCallSession.this.mImsCallSessionEventHandler.onUssdEvent((UssdEvent) asyncResult.result);
                }
            }
        };
        this.mMediaController = this.mModule.getImsMediaController();
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
        int[] iArr = new int[ImsCallUtil.EMERGENCY_TIMER_STATE.MAX.ordinal()];
        this.mTimerState = iArr;
        Arrays.fill(iArr, -1);
    }

    public void init(IVolteServiceInterface iVolteServiceInterface, IRegistrationManager iRegistrationManager) {
        ImsRegistration imsRegistration;
        ImsRegistration imsRegistration2;
        String[] currentPcscf;
        int i;
        this.mVolteSvcIntf = iVolteServiceInterface;
        this.mRegistrationManager = iRegistrationManager;
        ImsRegistration imsRegistration3 = this.mRegistration;
        if (imsRegistration3 != null) {
            if (imsRegistration3.getImsProfile().isSamsungMdmnEnabled()) {
                this.mMno = Mno.MDMN;
                Log.i(this.LOG_TAG, "init(): this is MDMN call");
                this.mCallProfile.setSamsungMdmnCall(true);
                CallProfile callProfile = this.mCallProfile;
                callProfile.setOriginatingUri(ImsUri.parse("sip:" + this.mRegistration.getImpi()));
            } else {
                this.mMno = Mno.fromName(this.mRegistration.getImsProfile().getMnoName());
            }
            this.mCmcType = this.mRegistration.getImsProfile().getCmcType();
        } else {
            this.mMno = SimUtil.getSimMno(this.mPhoneId);
        }
        CallStateMachine callStateMachine = new CallStateMachine(this.mContext, this, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, "CallStateMachine", this.mLooper);
        this.smCallStateMachine = callStateMachine;
        callStateMachine.init();
        Log.i(this.LOG_TAG, "start CallStatMachine");
        this.smCallStateMachine.start();
        this.mDiagnosisController = new DiagnosisController(this.smCallStateMachine);
        this.mImsCallDedicatedBearer = new ImsCallDedicatedBearer(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine);
        this.mImsCallSessionEventHandler = new ImsCallSessionEventHandler(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine, this.mCallProfile, this.mVolteSvcIntf, this.mMediaController);
        this.mVolteSvcIntf.registerForCallStateEvent(this.mVolteStackEventHandler, 1, (Object) null);
        this.mVolteSvcIntf.registerForRrcConnectionEvent(this.mVolteStackEventHandler, 3, (Object) null);
        this.mVolteSvcIntf.registerForUssdEvent(this.mUssdStackEventHandler, 4, (Object) null);
        this.mVolteSvcIntf.registerForReferStatus(this.mVolteStackEventHandler, 5, this);
        this.mMediaController.registerForMediaEvent(this);
        if (this.mModule.isSupportImsDataChannel(this.mPhoneId)) {
            this.mIdcData = new IdcImsCallSessionData();
        }
        if (!((!this.mMno.isOneOf(Mno.VZW, Mno.VIVA_BAHRAIN, Mno.ETISALAT_UAE) && !this.mMno.isChn()) || (imsRegistration2 = this.mRegistration) == null || (currentPcscf = this.mRegistrationManager.getCurrentPcscf(imsRegistration2.getHandle())) == null)) {
            try {
                i = Integer.parseInt(currentPcscf[1]);
            } catch (NumberFormatException unused) {
                Log.i(this.LOG_TAG, "use default port 5060");
                i = 5060;
            }
            this.mKaSender = new KeepAliveSender(this.mContext, this.mRegistration, currentPcscf[0], i, this.mMno);
            if (this.mMno != Mno.VZW) {
                this.mResumeCallRetriggerTimer = NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_GEN_FAILURE;
            }
        }
        setIsNrSaMode();
        if (isQuantumEncryptionServiceAvailable() && (imsRegistration = this.mRegistration) != null) {
            String removeUriPlusPrefix = ImsCallUtil.removeUriPlusPrefix(imsRegistration.getOwnNumber(), "+86", Debug.isProductShip());
            String str = this.LOG_TAG;
            Log.i(str, "localPhoneNumber : " + IMSLog.checker(removeUriPlusPrefix));
            this.mCallProfile.getQuantumSecurityInfo().setLocalPhoneNumber(removeUriPlusPrefix);
        }
    }

    public IdcImsCallSessionData getIdcData() {
        return this.mIdcData;
    }

    public void setIsNrSaMode() {
        NetworkEvent network = this.mModule.getNetwork(getPhoneId());
        this.mIsNrSaMode = network != null && network.network == 20;
        String str = this.LOG_TAG;
        Log.i(str, "mIsNrSaMode = " + this.mIsNrSaMode);
    }

    public boolean getIsNrSaMode() {
        return this.mIsNrSaMode;
    }

    public void setPendingCall(boolean z) {
        this.smCallStateMachine.setPendingCall(z);
    }

    public boolean isEpdgCall() {
        IPdnController pdnController = ImsRegistry.getPdnController();
        boolean isEpdgConnected = pdnController.isEpdgConnected(this.mPhoneId);
        CallProfile callProfile = this.mCallProfile;
        if (callProfile == null || !ImsCallUtil.isE911Call(callProfile.getCallType())) {
            return isEpdgConnected;
        }
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        int e911PdnSelectionVowifi = imsProfile != null ? imsProfile.getE911PdnSelectionVowifi() : 0;
        if (e911PdnSelectionVowifi == 0) {
            Log.i(this.LOG_TAG, "use isEmergencyEpdgConnected for EPDN");
            return pdnController.isEmergencyEpdgConnected(this.mPhoneId);
        } else if (e911PdnSelectionVowifi != 1) {
            return isEpdgConnected;
        } else {
            Log.i(this.LOG_TAG, "use EmergemcyRat for IPC_RAT_EPDG");
            return ImsConstants.EmergencyRat.IWLAN.equalsIgnoreCase(this.mCallProfile.getEmergencyRat());
        }
    }

    public void setCallId(int i) {
        this.mCallId = i;
        this.mRequestMark = i;
    }

    public int getCallId() {
        return this.mCallId;
    }

    public void setSessionId(int i) {
        String appendSessionIdToLogTag = IMSLog.appendSessionIdToLogTag(this.LOG_TAG, i);
        this.LOG_TAG = appendSessionIdToLogTag;
        Log.i(appendSessionIdToLogTag, "Session ID : [" + i + "]");
        this.mSessionId = i;
        this.smCallStateMachine.setLogTag(i);
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public void setCmcType(int i) {
        this.mCmcType = i;
    }

    public int getCmcType() {
        return this.mCmcType;
    }

    public void setVideoCrbtSupportType(int i) {
        this.mVideoCrbtSupportType = i;
    }

    public void setIsEstablished(boolean z) {
        this.mIsEstablished = z;
    }

    public boolean getIsEstablished() {
        return this.mIsEstablished;
    }

    public void setSrvccStarted(boolean z) {
        this.mSrvccStarted = z;
    }

    public boolean getSrvccStarted() {
        return this.mSrvccStarted;
    }

    public void setEpsFallback(boolean z) {
        this.mEpsFallback = z;
    }

    public boolean getEpsFallback() {
        return this.mEpsFallback;
    }

    public void setForwarded(boolean z) {
        this.mForwarded = z;
    }

    public boolean getForwarded() {
        return this.mForwarded;
    }

    public int getVideoCrbtSupportType() {
        return this.mVideoCrbtSupportType;
    }

    public CallConstants.STATE getCallState() {
        return this.smCallStateMachine.getState();
    }

    public int getCallStateOrdinal() {
        return this.smCallStateMachine.getState().ordinal();
    }

    public int getPrevCallStateOrdinal() {
        CallStateMachine callStateMachine = this.smCallStateMachine;
        return callStateMachine.getStateByName(callStateMachine.getPreviousState().getName()).ordinal();
    }

    public CallProfile getCallProfile() {
        return this.mCallProfile;
    }

    public CallProfile getModifyRequestedProfile() {
        return this.mModifyRequestedProfile;
    }

    public synchronized boolean getUsingCamera() {
        return this.mIsUsingCamera;
    }

    public synchronized void setUsingCamera(boolean z) {
        this.mIsUsingCamera = z;
    }

    public void setUserCameraOff(boolean z) {
        String str = this.LOG_TAG;
        Log.i(str, "setUserCameraOff : " + z);
        this.mUserCameraOff = z;
    }

    public synchronized Surface getDisplaySurface() {
        return this.mDisplaySurface;
    }

    public synchronized void setDisplaySurface(Surface surface) {
        this.mDisplaySurface = surface;
    }

    public void setHoldBeforeTransfer(boolean z) {
        this.smCallStateMachine.mHoldBeforeTransfer = z;
    }

    public void registerSessionEventListener(IImsCallSessionEventListener iImsCallSessionEventListener) throws RemoteException {
        Log.i(this.LOG_TAG, "registerListener");
        this.mListeners.register(iImsCallSessionEventListener);
        if (this.smCallStateMachine.needToLateEndedNotify()) {
            try {
                CallConstants.STATE state = this.smCallStateMachine.getState();
                if (state == CallConstants.STATE.EndingCall || state == CallConstants.STATE.EndedCall) {
                    Log.e(this.LOG_TAG, "notify the ended call for a late registered session.");
                    this.smCallStateMachine.notifyOnEnded(getErrorCode());
                }
            } catch (ArrayIndexOutOfBoundsException unused) {
            }
        }
    }

    public void unregisterSessionEventListener(IImsCallSessionEventListener iImsCallSessionEventListener) throws RemoteException {
        Log.i(this.LOG_TAG, "deregisterListener");
        this.mListeners.unregister(iImsCallSessionEventListener);
    }

    public void replaceSessionEventListener(ImsCallSession imsCallSession) {
        int beginBroadcast = imsCallSession.mListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            this.mListeners.register(imsCallSession.mListeners.getBroadcastItem(i));
        }
        imsCallSession.mListeners.finishBroadcast();
    }

    public int start(String str, CallProfile callProfile) throws RemoteException {
        IMSLog.LAZER_TYPE lazer_type = IMSLog.LAZER_TYPE.CALL;
        IMSLog.lazer(lazer_type, getCallId() + " - START OUTGOING");
        this.mStartTime = System.currentTimeMillis();
        if (str != null) {
            CallStateMachine callStateMachine = this.smCallStateMachine;
            callStateMachine.sendMessage(callStateMachine.obtainMessage(15, (Object) str));
            return getCallId();
        }
        IMSLog.lazer(lazer_type, getCallId() + " - OUTGOING FAIL by target is empty");
        Log.e(this.LOG_TAG, "start(): target is NULL");
        throw new RemoteException("Cannot make call: target URI is null");
    }

    /* access modifiers changed from: protected */
    public boolean needUpdateEccUrn() {
        return ImsCallUtil.isE911Call(this.mCallProfile.getCallType()) && ImsUtil.isSimMobilityActivated(this.mPhoneId) && !this.mMno.isEmeasewaoce() && !this.mMno.isTw();
    }

    public void startIncoming() {
        this.smCallStateMachine.sendMessage(21);
    }

    public void accept(CallProfile callProfile) throws RemoteException {
        this.smCallStateMachine.sendMessage(22, (Object) callProfile);
    }

    public void acceptIdc(String str) throws RemoteException {
        if (this.mModule.isSupportImsDataChannel(this.mPhoneId) && !TextUtils.isEmpty(str)) {
            this.smCallStateMachine.sendMessage(150, (Object) IdcExtra.getBuilder().setSdp(str).build());
        }
    }

    public void reject(int i) throws RemoteException {
        if ((!getIsEstablished() || getCallState() == CallConstants.STATE.ModifyRequested || !this.smCallStateMachine.hasMessages(212)) && getCallState() != CallConstants.STATE.InCall) {
            this.mEndType = 2;
            this.mEndReason = i;
            this.smCallStateMachine.sendMessage(23, i);
            return;
        }
        Log.i(this.LOG_TAG, "ignore reject for a while(3 secs) msg after call accepted or call was already InCall State.");
        throw new RemoteException("Reject Failed");
    }

    public void terminate(int i) throws RemoteException {
        terminate(i, false);
    }

    public void terminate(int i, boolean z) throws RemoteException {
        if (!isEpdgCall() || i != 8) {
            String str = this.LOG_TAG;
            Log.i(str, "Local Release ? " + z);
            int i2 = z ? 3 : 1;
            this.mEndType = i2;
            this.mEndReason = i;
            CallStateMachine callStateMachine = this.smCallStateMachine;
            if (callStateMachine.quit) {
                throw new RemoteException("Session already quitted");
            } else if (i == 22) {
                callStateMachine.sendMessage(306, i, i2);
            } else {
                CallProfile callProfile = this.mCallProfile;
                if (callProfile == null || callProfile.getRejectCause() == 0) {
                    this.smCallStateMachine.sendMessage(1, i, this.mEndType);
                } else {
                    this.smCallStateMachine.sendMessage(3, i, this.mEndType);
                }
            }
        } else {
            Log.i(this.LOG_TAG, "SRVCC Completed. But already switched to epdg, don't terminate call");
        }
    }

    public void hold(MediaProfile mediaProfile) throws RemoteException {
        this.smCallStateMachine.sendMessage(51);
    }

    public void resume() throws RemoteException {
        this.smCallStateMachine.sendMessage(71);
    }

    public void update(CallProfile callProfile, int i, String str) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("profile", callProfile);
        bundle.putInt("cause", i);
        bundle.putString("reasonText", str);
        if (callProfile != null || isSrvccAvailable()) {
            this.smCallStateMachine.sendMessage(52, (Object) bundle);
        } else {
            Log.i(this.LOG_TAG, "SRVCC isn't available");
        }
    }

    public void reinvite() throws RemoteException {
        this.smCallStateMachine.sendMessage(502);
    }

    public void transfer(String str) throws RemoteException {
        this.mModule.transfer(this.mSessionId, str);
    }

    public void cancelTransfer() throws RemoteException {
        Log.i(this.LOG_TAG, "cancelTransfer:");
        this.smCallStateMachine.sendMessage(60);
    }

    public void pushCall(String str) {
        String str2 = this.LOG_TAG;
        Log.i(str2, "transfer: msisdn=" + IMSLog.checker(str));
        this.smCallStateMachine.sendMessage(59, (Object) str);
    }

    public void reInviteIdc(int i, String str) {
        if (this.mModule.isSupportImsDataChannel(this.mPhoneId) && !TextUtils.isEmpty(str)) {
            this.smCallStateMachine.sendMessage(151, (Object) IdcExtra.getBuilder().setSdp(str).setReqId(i).build());
        }
    }

    public void sendNegotiatedLocalSdp(String str) {
        if (this.mModule.isSupportImsDataChannel(this.mPhoneId)) {
            this.mVolteSvcIntf.sendNegotiatedLocalSdp(this.mSessionId, str);
            if (getIdcData() != null) {
                getIdcData().transitState(IdcImsCallSessionData.State.NEGOTIATED);
            }
        }
    }

    public ImsRegistration getRegistration() {
        return this.mRegistration;
    }

    public void setEpdgState(boolean z) {
        String str = this.LOG_TAG;
        Log.i(str, "setEpdgState: " + z);
        boolean z2 = false;
        if (z) {
            this.mCallProfile.setRadioTech(18);
            CallProfile callProfile = this.mCallProfile;
            if (ImsRegistry.getPdnController().getEpdgPhysicalInterface(this.mPhoneId) == 2) {
                z2 = true;
            }
            callProfile.setCrossSimCall(z2);
        } else {
            this.mCallProfile.setRadioTech(14);
            this.mCallProfile.setCrossSimCall(false);
            Mno mno = this.mMno;
            if (mno == Mno.TMOUS || mno == Mno.SPRINT) {
                Log.i(this.LOG_TAG, "[TMOUS/SPRINT] check DBR after handover");
                this.mModule.handleDedicatedEventAfterHandover(this.mPhoneId);
            }
        }
        this.smCallStateMachine.sendMessage(400, z ? 1 : 0);
    }

    public void setEpdgStateNoNotify(boolean z) {
        String str = this.LOG_TAG;
        Log.i(str, "setEpdgStateNoNotify: " + z);
        boolean z2 = false;
        if (z) {
            this.mCallProfile.setRadioTech(18);
            CallProfile callProfile = this.mCallProfile;
            if (ImsRegistry.getPdnController().getEpdgPhysicalInterface(this.mPhoneId) == 2) {
                z2 = true;
            }
            callProfile.setCrossSimCall(z2);
            return;
        }
        this.mCallProfile.setRadioTech(14);
        this.mCallProfile.setCrossSimCall(false);
    }

    public int pulling(String str, Dialog dialog) throws RemoteException {
        if (str == null || dialog == null) {
            Log.e(this.LOG_TAG, "transfer(): target is NULL");
            throw new RemoteException("Cannot transfer call: target is empty");
        }
        this.mCallProfile.setDialingNumber(dialog.getRemoteNumber());
        this.mCallProfile.setConferenceCall(0);
        Bundle bundle = new Bundle();
        bundle.putString("msisdn", str);
        bundle.putParcelable("targetDialog", dialog);
        ContentValues contentValues = new ContentValues();
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration == null || !imsRegistration.getImsProfile().isSoftphoneEnabled()) {
            Log.i(this.LOG_TAG, "pulling for mdService MEP");
            contentValues.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_TOTAL_COUNT, 1);
            contentValues.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_MEP_COUNT, 1);
        } else {
            Log.i(this.LOG_TAG, "pulling for Softphone");
            contentValues.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_TOTAL_COUNT, 1);
            contentValues.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_SOFTPHONE_COUNT, 1);
        }
        contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(this.mPhoneId, this.mContext, "DRPT", contentValues);
        this.smCallStateMachine.sendMessage(12, (Object) bundle);
        return getCallId();
    }

    public void merge(int i, int i2) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void startConference(String[] strArr, CallProfile callProfile) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void inviteParticipants(int i) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void removeParticipants(int i) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void inviteGroupParticipant(String str) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void removeGroupParticipant(String str) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void extendToConference(String[] strArr) throws RemoteException {
        if (strArr != null) {
            this.smCallStateMachine.sendMessage(73, (Object) strArr);
        } else {
            Log.e(this.LOG_TAG, "extendToConference(): there is no participants");
            throw new RemoteException("Cannot extendToConference : participants is null");
        }
    }

    public void holdVideo() throws RemoteException {
        this.smCallStateMachine.sendMessage(80);
    }

    public void resumeVideo() throws RemoteException {
        this.smCallStateMachine.sendMessage(81);
    }

    public void startCameraForProvider(int i) {
        if (isNeedCameraRequest()) {
            setUserCameraOff(false);
            if (getCallState() != CallConstants.STATE.IncomingCall || getUsingCamera() || isTPhoneRelaxMode() || this.smCallStateMachine.hasMessages(24)) {
                startCamera(i);
                return;
            }
            Log.d(this.LOG_TAG, "delay camera start for Incoming Call");
            this.smCallStateMachine.sendMessageDelayed(24, 0, -1, 100);
        } else if (i == -1) {
            Log.i(this.LOG_TAG, "startCamera called with dummy type");
            startLastUsedCamera();
        }
    }

    public void stopCameraForProvider(boolean z) {
        if (z || isNeedCameraRequest()) {
            setUserCameraOff(true);
            if (getUsingCamera()) {
                stopCamera();
            }
        }
    }

    private boolean isNeedCameraRequest() {
        Mno mno = this.mMno;
        boolean z = false;
        if (mno == Mno.DOCOMO || mno.isKor()) {
            boolean isVideoCall = ImsCallUtil.isVideoCall(getCallProfile().getCallType());
            boolean z2 = getCallState() == CallConstants.STATE.ModifyingCall || getCallState() == CallConstants.STATE.ModifyRequested;
            if (isVideoCall || z2) {
                z = true;
            }
        }
        Log.i(this.LOG_TAG, "isNeedCameraRequest=" + z);
        return z;
    }

    /* access modifiers changed from: package-private */
    public void handleImsDcEvent(int i, Bundle bundle) {
        if (i == 1) {
            String string = bundle.getString("com.samsung.telephony.extra.ims.IMSDC_TELECOM_CALL_ID", "");
            if (TextUtils.isEmpty(string) || !string.startsWith("TC@")) {
                String str = this.LOG_TAG;
                Log.i(str, "handleImsDcEvent invalid TelecomCallId " + string);
            } else if (this.mIdcData == null) {
                Log.i(this.LOG_TAG, "handleImsDcEvent mIdcData null");
            } else {
                if (string.contains("_")) {
                    string = string.substring(0, string.indexOf("_"));
                } else {
                    String str2 = this.LOG_TAG;
                    Log.i(str2, "handleImsDcEvent unexpected format: " + string);
                }
                String str3 = this.LOG_TAG;
                Log.i(str3, "handleImsDcEvent TelecomCallId " + string);
                this.mIdcData.setTelecomCallId(string);
                if (!TextUtils.isEmpty(this.mIdcData.getLocalBdcTlsId()) && !this.mIdcData.getIsNotifiedTelecomCallId()) {
                    this.mModule.getIdcServiceHelper().setTelecomCallId(this.mIdcData.getLocalBdcTlsId(), string);
                    this.mIdcData.setIsNotifiedTelecomCallId(true);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean preProcessImsCallEvent(String str, Bundle bundle) {
        int i;
        int i2;
        boolean z = false;
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        boolean z2 = true;
        if (!str.contains("_")) {
            return true;
        }
        String[] split = str.split("_");
        if (split.length > 0) {
            Log.i(this.LOG_TAG, "preProcessImsCallEvent: event type: " + split[0]);
            if (TELECOM_EVENT_PREFIX.equals(split[0])) {
                if ("VCS".equals(split[1])) {
                    if (this.mMno != Mno.CMCC) {
                        Log.i(this.LOG_TAG, "not support VCS for mno " + this.mMno);
                        return false;
                    }
                    if ("TOUCHSCREEN".equals(split[2])) {
                        String string = bundle.getString("com.samsung.telephony.extra.ims.VCS_ACTION");
                        i2 = (!"slide".equals(string) || VcidEvent.BUNDLE_VALUE_ACTION_START.equals(bundle.getString("com.samsung.telephony.extra.ims.VCS_SLIDING_STAGE"))) ? 0 : 100;
                        Log.i(this.LOG_TAG, "preProcessImsCallEvent: state: " + string + ", delay: " + i2);
                        i = 1;
                    } else {
                        i2 = 0;
                        i = -1;
                    }
                    if (i > -1) {
                        Handler handler = this.mVolteStackEventHandler;
                        handler.sendMessageDelayed(handler.obtainMessage(7, i, -1, bundle), (long) i2);
                        z2 = z;
                    }
                }
            } else if (split[0].length() > 28) {
                String substring = split[0].substring(28);
                if (this.mModule.isSupportImsDataChannel(this.mPhoneId) && "IMSDC".equals(substring)) {
                    int i3 = "UPDATETELECOMCALLID".equals(split[1]) ? 1 : -1;
                    if (i3 > -1) {
                        Handler handler2 = this.mVolteStackEventHandler;
                        handler2.sendMessage(handler2.obtainMessage(6, i3, -1, bundle));
                        z2 = z;
                    }
                }
            } else {
                Log.i(this.LOG_TAG, "unknown function name format: " + split[0]);
            }
            z = true;
            z2 = z;
        }
        Log.i(this.LOG_TAG, "preProcessImsCallEvent allowGotoSve: " + z2);
        return z2;
    }

    public void sendImsCallEvent(String str, Bundle bundle) throws RemoteException {
        String str2 = this.LOG_TAG;
        Log.i(str2, "sendImsCallEvent: event: " + str);
        if (preProcessImsCallEvent(str, bundle)) {
            bundle.putInt("sessionId", this.mSessionId);
            this.mMediaController.sendGeneralBundleEvent(str, bundle);
        }
    }

    public void sendDtmf(int i, int i2, Message message) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putInt("code", i);
        bundle.putInt("mode", 0);
        bundle.putInt("operation", 1);
        bundle.putParcelable("result", message);
        this.smCallStateMachine.sendMessage(56, (Object) bundle);
    }

    public void sendText(String str, int i) {
        Bundle bundle = new Bundle();
        bundle.putInt("len", i);
        bundle.putString("text", str);
        this.smCallStateMachine.sendMessage(64, (Object) bundle);
    }

    public void startDtmf(int i) throws RemoteException {
        this.mDtmfCode = i;
        Bundle bundle = new Bundle();
        bundle.putInt("code", i);
        bundle.putInt("mode", 1);
        bundle.putInt("operation", 1);
        this.smCallStateMachine.sendMessage(56, (Object) bundle);
    }

    public void stopDtmf() throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putInt("code", this.mDtmfCode);
        bundle.putInt("mode", 1);
        bundle.putInt("operation", 2);
        this.smCallStateMachine.sendMessage(56, (Object) bundle);
        this.mDtmfCode = 0;
    }

    public void setTtyMode(int i) {
        Log.i(this.LOG_TAG, "setTtyMode: " + i);
        boolean z = false;
        boolean z2 = this.mCallProfile.getCallType() == 9 || this.mCallProfile.getCallType() == 11 || this.mCallProfile.getCallType() == 10 || this.mCallProfile.getCallType() == 14 || this.mCallProfile.getCallType() == 15;
        if (!(i == Extensions.TelecomManager.TTY_MODE_OFF || i == Extensions.TelecomManager.RTT_MODE)) {
            z = true;
        }
        Log.i(this.LOG_TAG, "setTtyMode: curTty " + z2 + " desTty " + z);
        if (!z2 && z) {
            CallProfile callProfile = new CallProfile();
            callProfile.setCallType(9);
            callProfile.getMediaProfile().setVideoQuality(this.mCallProfile.getMediaProfile().getVideoQuality());
            Bundle bundle = new Bundle();
            bundle.putParcelable("profile", callProfile);
            this.smCallStateMachine.sendMessage(52, (Object) bundle);
        }
    }

    public void info(int i, String str) throws RemoteException {
        String str2 = this.LOG_TAG;
        Log.i(str2, "info: request=" + IMSLog.checker(str));
        Bundle bundle = new Bundle();
        bundle.putInt("type", i);
        bundle.putString(McsConstants.BundleData.INFO, str);
        this.smCallStateMachine.sendMessage(101, (Object) bundle);
    }

    public void notifyImsMediaEvent(IMSMediaEvent iMSMediaEvent) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyImsMediaEvent: " + iMSMediaEvent.getState());
        if (getCallState() == CallConstants.STATE.ResumingCall) {
            Handler handler = this.mVolteStackEventHandler;
            handler.sendMessageDelayed(handler.obtainMessage(2, iMSMediaEvent), 1000);
            return;
        }
        Handler handler2 = this.mVolteStackEventHandler;
        handler2.sendMessage(handler2.obtainMessage(2, iMSMediaEvent));
    }

    public void notifyCmcDtmfEvent(int i) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyCmcDtmfEvent, dtmfKey: " + i);
        this.smCallStateMachine.sendMessage(86, i, -1);
    }

    public void notifyCmcInfoEvent(CmcInfoEvent cmcInfoEvent) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyCmcInfoEvent, cmcInfo: " + cmcInfoEvent);
        this.smCallStateMachine.sendMessage(87, (Object) cmcInfoEvent);
    }

    public IImsMediaCallProvider getMediaCallProvider() throws RemoteException {
        return this.mMediaController;
    }

    public void requestCallDataUsage() throws RemoteException {
        this.smCallStateMachine.requestCallDataUsage();
    }

    public String getIncomingInviteRawSip() {
        return this.mCallProfile.getSipInviteMsg();
    }

    public void updateCallProfile(CallParams callParams) {
        String str = this.LOG_TAG;
        Log.i(str, "updateCallProfile: " + callParams);
        new DefaultCallProfileBuilder(callParams).builder().build(this.mCallProfile);
        setAudioCodecTypeProfile(callParams.getAudioCodec());
        setMediaProfile(callParams);
    }

    private void setMediaProfile(CallParams callParams) {
        if (this.mCallProfile.getMediaProfile() != null) {
            this.mCallProfile.getMediaProfile().setVideoOrientation(callParams.getVideoOrientation());
            String videoSize = this.mCallProfile.getMediaProfile().getVideoSize();
            this.mCallProfile.getMediaProfile().setVideoSize(callParams.getVideoWidth(), callParams.getVideoHeight());
            String videoSize2 = this.mCallProfile.getMediaProfile().getVideoSize();
            if ((videoSize.equals(videoSize2) && !this.mMno.isChn()) || callParams.getVideoCrbtType() != 0) {
                return;
            }
            if (!videoSize2.contains("LAND") || videoSize2.contains("QCIF")) {
                this.mMediaController.changeCameraCapabilities(getSessionId(), callParams.getVideoWidth(), callParams.getVideoHeight());
            } else {
                this.mMediaController.changeCameraCapabilities(getSessionId(), callParams.getVideoHeight(), callParams.getVideoWidth());
            }
        }
    }

    private void setAudioCodecTypeProfile(String str) {
        if (str != null) {
            this.mCallProfile.getMediaProfile().setAudioCodec(ImsCallUtil.getAudioCodec(str));
        }
    }

    public void forceNotifyCurrentCodec() {
        this.smCallStateMachine.sendMessage(100);
    }

    /* access modifiers changed from: protected */
    public void onImsCallEvent(CallStateEvent callStateEvent) {
        this.mImsCallSessionEventHandler.onImsCallEventHandler(callStateEvent);
    }

    /* access modifiers changed from: protected */
    public void notifyCallDowngraded() {
        IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
        iMSMediaEvent.setSessionID(this.mSessionId);
        iMSMediaEvent.setPhoneId(this.mPhoneId);
        iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CALL_DOWNGRADED);
        this.mMediaController.onCallDowngraded(iMSMediaEvent);
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"DefaultLocale"})
    public ImsUri buildUri(String str, String str2, int i) {
        UriGeneratorFactory instance = UriGeneratorFactory.getInstance();
        ImsUri originatingUri = getOriginatingUri();
        UriGenerator.URIServiceType uRIServiceType = UriGenerator.URIServiceType.VOLTE_URI;
        UriGenerator uriGenerator = instance.get(originatingUri, uRIServiceType);
        if (i == 12) {
            return uriGenerator.getUssdRuri(str);
        }
        if (!TextUtils.isEmpty(str) && str.toLowerCase().startsWith("urn")) {
            return ImsUri.parse(str);
        }
        if (this.mMno == Mno.TIM_BRAZIL && str.length() == 3) {
            return uriGenerator.getNetworkPreferredUri(uRIServiceType, ImsUri.UriType.TEL_URI, str, str2);
        }
        if (this.mCmcType > 0 && !TextUtils.isEmpty(str) && str.contains("@")) {
            str = str.substring(0, str.indexOf("@"));
            String str3 = this.LOG_TAG;
            Log.i(str3, "number = " + IMSLog.checker(str));
        }
        return uriGenerator.getNetworkPreferredUri(uRIServiceType, str, str2);
    }

    /* access modifiers changed from: protected */
    public ImsUri getOriginatingUri() {
        if (this.mCallProfile.getOriginatingUri() != null) {
            return this.mCallProfile.getOriginatingUri();
        }
        return getPreferredImpu(this.mRegistration);
    }

    private ImsUri getPreferredImpu(ImsRegistration imsRegistration) {
        ImsUri uri = imsRegistration.getPreferredImpu().getUri();
        Mno mno = this.mMno;
        if ((mno != Mno.ATT && mno != Mno.SMARTFREN) || DeviceUtil.getGcfMode() || imsRegistration.getImpuList() == null) {
            return uri;
        }
        for (NameAddr nameAddr : imsRegistration.getImpuList()) {
            if (nameAddr.getUri().getUriType().equals(ImsUri.UriType.TEL_URI)) {
                Log.i(this.LOG_TAG, "getPreferredImpu: Found TEL URI");
                return nameAddr.getUri();
            }
        }
        return uri;
    }

    /* access modifiers changed from: protected */
    public ImsUri getSecondImpu(ImsRegistration imsRegistration) {
        ImsUri uri = imsRegistration.getPreferredImpu().getUri();
        if (this.mMno != Mno.LGU || imsRegistration.getImpuList() == null) {
            return uri;
        }
        for (NameAddr nameAddr : imsRegistration.getImpuList()) {
            if (nameAddr.getUri() != uri) {
                Log.i(this.LOG_TAG, "getSecondImpu: Found Second Number");
                return nameAddr.getUri();
            }
        }
        return uri;
    }

    /* access modifiers changed from: protected */
    public String getConferenceUri(ImsProfile imsProfile) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (imsProfile == null || simManagerFromSimSlot == null) {
            return null;
        }
        return ImsCallUtil.getConferenceUri(this.mPhoneId, imsProfile, simManagerFromSimSlot.getSimOperator(), this.mTelephonyManager.getIsimDomain(simManagerFromSimSlot.getSubscriptionId()), this.mMno);
    }

    /* access modifiers changed from: protected */
    public String getConfSubscribeEnabled(ImsProfile imsProfile) {
        if (imsProfile == null) {
            return null;
        }
        return imsProfile.getConferenceSubscribe();
    }

    /* access modifiers changed from: protected */
    public String getConfSubscribeDialogType(ImsProfile imsProfile) {
        if (imsProfile == null) {
            return null;
        }
        return imsProfile.getConferenceDialogType();
    }

    /* access modifiers changed from: protected */
    public String getConfReferUriType(ImsProfile imsProfile) {
        if (imsProfile == null) {
            return null;
        }
        return imsProfile.getConferenceReferUriType();
    }

    /* access modifiers changed from: protected */
    public String getConfRemoveReferUriType(ImsProfile imsProfile) {
        if (imsProfile == null) {
            return null;
        }
        return imsProfile.getConferenceRemoveReferUriType();
    }

    /* access modifiers changed from: protected */
    public String getConfReferUriAsserted(ImsProfile imsProfile) {
        if (imsProfile == null) {
            return null;
        }
        return imsProfile.getConferenceReferUriAsserted();
    }

    /* access modifiers changed from: protected */
    public String getConfUseAnonymousUpdate(ImsProfile imsProfile) {
        if (imsProfile == null) {
            return null;
        }
        return imsProfile.getConferenceUseAnonymousUpdate();
    }

    /* access modifiers changed from: protected */
    public boolean getConfSupportPrematureEnd(ImsProfile imsProfile) {
        if (imsProfile == null) {
            return false;
        }
        return imsProfile.getConferenceSupportPrematureEnd();
    }

    /* access modifiers changed from: protected */
    public void setRttDedicatedBearerTimeoutMessage(Message message) {
        this.mImsCallDedicatedBearer.setRttDedicatedBearerTimeoutMessage(message);
    }

    /* access modifiers changed from: protected */
    public void startRttDedicatedBearerTimer(long j) {
        this.mImsCallDedicatedBearer.startRttDedicatedBearerTimer(j);
    }

    /* access modifiers changed from: protected */
    public void stopRttDedicatedBearerTimer() {
        this.mImsCallDedicatedBearer.stopRttDedicatedBearerTimer();
    }

    /* access modifiers changed from: protected */
    public boolean getDRBLost() {
        return this.mImsCallDedicatedBearer.getDRBLost();
    }

    /* access modifiers changed from: protected */
    public void setDRBLost(boolean z) {
        this.mImsCallDedicatedBearer.setDRBLost(z);
    }

    public int getErrorCode() {
        CallStateMachine callStateMachine = this.smCallStateMachine;
        SipError sipError = callStateMachine.sipError;
        if (sipError != null) {
            return sipError.getCode();
        }
        SipReason sipReason = callStateMachine.sipReason;
        if (sipReason != null) {
            return sipReason.getCause();
        }
        return callStateMachine.errorCode;
    }

    public String getErrorMessage() {
        CallStateMachine callStateMachine = this.smCallStateMachine;
        SipError sipError = callStateMachine.sipError;
        if (sipError != null) {
            return sipError.getReason();
        }
        SipReason sipReason = callStateMachine.sipReason;
        if (sipReason != null) {
            return sipReason.getText();
        }
        return callStateMachine.errorMessage;
    }

    public int getEndType() {
        return this.mEndType;
    }

    public int getEndReason() {
        return this.mEndReason;
    }

    public void setEndReason(int i) {
        this.mEndReason = i;
    }

    public void setEndType(int i) {
        this.mEndType = i;
    }

    public void setPreviewResolution(int i, int i2) {
        String str = this.LOG_TAG;
        Log.i(str, "setPreviewResolution width : " + i + " height : " + i2);
        this.mMediaController.setPreviewResolution(i, i2);
    }

    public void startCamera(int i) {
        NetworkEvent network;
        Mno mno = this.mMno;
        if ((mno == Mno.DOCOMO || mno.isKor()) && this.mUserCameraOff) {
            Log.i(this.LOG_TAG, "Camera is Off by user");
            return;
        }
        String str = this.LOG_TAG;
        Log.i(str, "startCamera " + i);
        if (i < 0 || i == 2) {
            int defaultCameraId = this.mMediaController.getDefaultCameraId();
            this.mLastUsedCamera = defaultCameraId;
            if (defaultCameraId < 0) {
                this.mLastUsedCamera = 1;
            }
        } else {
            this.mLastUsedCamera = i;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "mLastUsedCamera " + this.mLastUsedCamera);
        if (this.mMno.isKor() && (((network = this.mModule.getNetwork(this.mPhoneId)) != null && !NetworkUtil.is3gppPsVoiceNetwork(network.network)) || (this.mCallProfile.getMediaProfile().getWidth() == 176 && this.mCallProfile.getMediaProfile().getHeight() == 144))) {
            setPreviewResolution(MNO.ORANGE_SENEGAL, 144);
        }
        this.mMediaController.startCamera(this.mSessionId, this.mLastUsedCamera);
    }

    public void startLastUsedCamera() {
        String str = this.LOG_TAG;
        Log.i(str, "startLastUsedCamera " + this.mLastUsedCamera);
        startCamera(this.mLastUsedCamera);
    }

    public void stopCamera() {
        Log.i(this.LOG_TAG, "stopCamera");
        this.mMediaController.stopCamera(this.mSessionId);
        this.mIsUsingCamera = false;
    }

    public void setStartCameraState(boolean z) {
        this.smCallStateMachine.setStartCameraState(z);
    }

    public void onSwitchCamera() {
        String str = this.LOG_TAG;
        Log.i(str, "onSwitchCamera - mLastUsedCamera " + this.mLastUsedCamera);
        if (this.mLastUsedCamera == 1) {
            this.mLastUsedCamera = 0;
        } else {
            this.mLastUsedCamera = 1;
        }
    }

    public void onUpdateGeolocation() {
        Log.i(this.LOG_TAG, "onUpdateGeolocation: ON_LOCATION_ACQUIRING_SUCCESS");
        this.smCallStateMachine.sendMessage(501);
    }

    public void handleRegistrationDone(ImsRegistration imsRegistration) {
        Log.i(this.LOG_TAG, "handleRegistrationDone");
        this.mRegistration = imsRegistration;
        this.smCallStateMachine.onRegistrationDone(imsRegistration);
        setPendingCall(false);
        boolean isE911Call = ImsCallUtil.isE911Call(this.mCallProfile.getCallType());
        if (this.mMno.isKor() || isE911Call) {
            this.smCallStateMachine.sendMessage(11);
        } else if (this.mMno != Mno.VZW) {
        } else {
            if (this.mRegistration.hasService("mmtel")) {
                this.smCallStateMachine.sendMessage(11);
            } else {
                this.smCallStateMachine.sendMessage(211);
            }
        }
    }

    public void handleRegistrationFailed() {
        Log.i(this.LOG_TAG, "handleRegistrationFailed");
        this.mRegistration = null;
        setPendingCall(false);
        this.smCallStateMachine.sendMessage(211);
    }

    public void replaceRegistrationInfo(ImsRegistration imsRegistration) {
        String str = this.LOG_TAG;
        Log.i(str, "replaceRegistrationInfo from " + this.mRegistration.getHandle() + " to " + imsRegistration.getHandle());
        this.mRegistration = imsRegistration;
    }

    public void replaceSipCallId(String str) {
        String str2 = this.LOG_TAG;
        Log.i(str2, "replaceSipCallId " + str);
        this.mCallProfile.setSipCallId(str);
        this.mVolteSvcIntf.replaceSipCallId(this.mSessionId, str);
    }

    /* access modifiers changed from: protected */
    public boolean isTPhoneRelaxMode() {
        return ImsCallUtil.isTPhoneRelaxMode(this.mContext, this.mCallProfile.getDialingNumber());
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public long getCmcCallEstablishTime() {
        return this.smCallStateMachine.mCmcCallEstablishTime;
    }

    public boolean isRemoteHeld() {
        return this.smCallStateMachine.mRemoteHeld;
    }

    private boolean isSrvccAvailable() {
        return ImsCallUtil.isSrvccAvailable(this.mModule.getSrvccVersion(this.mPhoneId), this.mMno, isEpdgCall(), getCallState(), this.mCallProfile.isConferenceCall());
    }

    /* access modifiers changed from: protected */
    public String getPEmergencyInfo(String str, String str2) {
        return ImsCallUtil.getPEmergencyInfo(this.mPhoneId, this.mContext, str, str2);
    }

    /* access modifiers changed from: protected */
    public String getImei(int i) {
        return this.mTelephonyManager.getImei(i);
    }

    public boolean isE911Call() {
        CallProfile callProfile = this.mCallProfile;
        return callProfile != null && ImsCallUtil.isE911Call(callProfile.getCallType());
    }

    public void setRelayChTerminated(boolean z) {
        this.mRelayChTerminated = z;
    }

    public boolean getRelayChTerminated() {
        return this.mRelayChTerminated;
    }

    /* access modifiers changed from: protected */
    public void setTimerState(int i, int i2) {
        this.mTimerState[i] = i2;
    }

    /* access modifiers changed from: protected */
    public int getTimerState(int i) {
        return this.mTimerState[i];
    }

    public boolean getPendingCall() {
        return this.smCallStateMachine.getPendingCall();
    }

    public boolean isQuantumEncryptionCall() {
        return this.mModule.isQSSSuccessAuthAndLogin(this.mPhoneId) && getQuantumSecurityInfo().getEncryptStatus() != 4;
    }

    public boolean isQuantumEncryptionServiceAvailable() {
        if (this.mCmcType != 0) {
            return false;
        }
        CallProfile callProfile = this.mCallProfile;
        if (callProfile != null) {
            if (callProfile.isECallConvertedToNormal() || ImsCallUtil.isVideoCall(this.mCallProfile.getCallType())) {
                Log.i(this.LOG_TAG, "E call or VT call, not support QEC");
                return false;
            } else if (this.mCallProfile.isDowngradedVideoCall()) {
                Log.i(this.LOG_TAG, "Downgraded VT Call, not support QEC");
                return false;
            }
        }
        if (this.mModule.getSessionCount(this.mPhoneId) <= 1) {
            return this.mModule.isQSSSuccessAuthAndLogin(this.mPhoneId);
        }
        Log.i(this.LOG_TAG, "Under multicall, not support QEC");
        return false;
    }

    public QuantumSecurityInfo getQuantumSecurityInfo() {
        return this.mCallProfile.getQuantumSecurityInfo();
    }

    public void notifyQuantumEncryptionStatus(int i) {
        getQuantumSecurityInfo().setEncryptStatus(i);
        notifyImsGeneralEvent("NotifyQuantumEncryptionStatus", new Bundle());
    }

    public void disableQuantumEncryption() {
        Log.i(this.LOG_TAG, "disableQuantumEncryption()");
        notifyQuantumEncryptionStatus(4);
        this.mVolteSvcIntf.enableQuantumSecurityService(this.mSessionId, false);
    }

    public void requestQuantumPeerProfileStatus(boolean z, boolean z2) {
        if (isQuantumEncryptionServiceAvailable()) {
            if (SimUtil.isCurrentDSDASupport() || SimUtil.getActiveDataPhoneId() == this.mPhoneId || !NetworkUtil.isMobileDataConnected(this.mContext)) {
                this.mIsPeerProfileRetried = z;
                if (!z) {
                    this.mVolteSvcIntf.enableQuantumSecurityService(this.mSessionId, true);
                }
                if (z2) {
                    this.mRequestMark += 10;
                }
                if (ImsRegistry.getServiceModuleManager().getQuantumEncryptionServiceModule().onRequestPeerProfileStatus(new SxRequestPeerProfileEntity(getQuantumSecurityInfo().getLocalPhoneNumber(), this.mCallProfile.getQuantumSecurityInfo().getRemoteTelNum(), this.mCallProfile.getDirection(), this.mCallProfile.getSipCallId(), String.valueOf(this.mRequestMark))) == -1) {
                    Log.e(this.LOG_TAG, "onRequestPeerProfileStatus fail");
                    disableQuantumEncryption();
                    return;
                }
                return;
            }
            Log.i(this.LOG_TAG, "dsds mode, no data available");
            disableQuantumEncryption();
        }
    }

    private boolean checkQuantumPeerProfileCondition(int i) {
        if (!isQuantumEncryptionServiceAvailable()) {
            Log.i(this.LOG_TAG, "updateQuantumPeerProfileStatus auth and login fail");
            return false;
        } else if (i == 401) {
            return true;
        } else {
            String str = this.LOG_TAG;
            Log.i(str, "updateQuantumPeerProfileStatus fail: " + i);
            return false;
        }
    }

    public void updateQuantumPeerProfileStatus(int i, String str, String str2, String str3) {
        int i2;
        try {
            i2 = Integer.parseInt(str3);
        } catch (NumberFormatException unused) {
            Log.i(this.LOG_TAG, "requestMark is not Integer");
            i2 = 0;
        }
        if ("NOTIFY_SESSION_ID".equals(str) || i2 == this.mRequestMark) {
            int encryptStatus = this.mCallProfile.getQuantumSecurityInfo().getEncryptStatus();
            if (encryptStatus != 0) {
                Log.i(this.LOG_TAG, "skip updateQuantumPeerProfileStatus because Quantum Encryption status is already " + encryptStatus);
            } else if (this.mIsPeerProfileRetried || i != 403) {
                this.mIsPeerProfileRetried = false;
                if (!checkQuantumPeerProfileCondition(i)) {
                    disableQuantumEncryption();
                    return;
                }
                getQuantumSecurityInfo().setPeerProfileStatus(i);
                getQuantumSecurityInfo().setQtSessionId(str2);
                getQuantumSecurityInfo().setCryptoMode(2);
                notifyQuantumEncryptionStatus(1);
                if (!SemSystemProperties.get("ro.build.type", "user").equals("user")) {
                    Toast.makeText(this.mContext, "Getting SessionId Success!!!", 1).show();
                }
                if (ImsRegistry.getServiceModuleManager().getQuantumEncryptionServiceModule().onRequestQMKey(new SxRequestQMKeyEntity(this.mCallProfile.getQuantumSecurityInfo().getLocalPhoneNumber(), this.mCallProfile.getQuantumSecurityInfo().getRemoteTelNum(), QuantumSecurityInfo.QUANTUM_ENCRYPT_KEY_LENGTH, str2, this.mCallProfile.getDirection(), this.mCallProfile.getSipCallId(), String.valueOf(this.mRequestMark))) == -1) {
                    Log.i(this.LOG_TAG, "onRequestQMKey fail");
                    disableQuantumEncryption();
                } else if (!"NOTIFY_SESSION_ID".equals(str)) {
                    setQuantumSecurityInfo();
                } else {
                    Log.i(this.LOG_TAG, "In this case, no need to send QSM IPC to voice engine");
                }
            } else {
                Log.i(this.LOG_TAG, "retry peerProfileStatus by 403");
                requestQuantumPeerProfileStatus(true, false);
            }
        } else {
            Log.e(this.LOG_TAG, "mRequestMark(" + this.mRequestMark + ") is not matched with requestMark(" + str3 + ")");
        }
    }

    private boolean checkQuantumQMKeyCondition(int i) {
        if (!isQuantumEncryptionServiceAvailable()) {
            Log.i(this.LOG_TAG, "updateQuantumQMKeyStatus auth and login fail");
            return false;
        } else if (i != 351) {
            String str = this.LOG_TAG;
            Log.i(str, "updateQuantumQMKeyStatus fail: " + i);
            this.mKeyRetryCnt = this.mKeyRetryCnt - 1;
            String str2 = this.LOG_TAG;
            Log.i(str2, "retry QMKey cnt: " + this.mKeyRetryCnt);
            return false;
        } else if (getQuantumSecurityInfo().getPeerProfileStatus() == 401) {
            return true;
        } else {
            String str3 = this.LOG_TAG;
            Log.i(str3, "updateQuantumQMKeyStatus abnormal peerProfileStatus: " + getQuantumSecurityInfo().getPeerProfileStatus());
            return false;
        }
    }

    public void updateQuantumQMKeyStatus(int i, String str, String str2, byte[] bArr, String str3) {
        int i2;
        int i3;
        try {
            i2 = Integer.parseInt(str3);
        } catch (NumberFormatException unused) {
            Log.i(this.LOG_TAG, "requestMark is not Integer");
            i2 = 0;
        }
        if (i2 != this.mRequestMark) {
            String str4 = this.LOG_TAG;
            Log.e(str4, "mRequestMark(" + this.mRequestMark + ") is not matched with requestMark(" + str3);
        } else if (this.mCallProfile.getQuantumSecurityInfo().getEncryptStatus() == 4 || this.mCallProfile.getQuantumSecurityInfo().getPeerProfileStatus() == -1) {
            Log.i(this.LOG_TAG, "skip updateQuantumQMKeyStatus because Quantum Encryption is disabled or PeerProfileStatus is invalid");
        } else {
            boolean checkQuantumQMKeyCondition = checkQuantumQMKeyCondition(i);
            if (checkQuantumQMKeyCondition || (i3 = this.mKeyRetryCnt) >= 6 || i3 < 0) {
                this.mKeyRetryCnt = 6;
                if (!checkQuantumQMKeyCondition) {
                    disableQuantumEncryption();
                    return;
                }
                if (!SemSystemProperties.get("ro.build.type", "user").equals("user")) {
                    Toast.makeText(this.mContext, "Getting SessionKey Success!!!", 1).show();
                }
                notifyQuantumEncryptionStatus(2);
                String bytesToHex = bytesToHex(bArr);
                if (TextUtils.isEmpty(getQuantumSecurityInfo().getSessionKey()) || !getQuantumSecurityInfo().getSessionKey().equals(bytesToHex)) {
                    getQuantumSecurityInfo().setSessionKey(bytesToHex);
                    Log.i(this.LOG_TAG, "updateQuantumQMKeyStatus all infos ready. setQuantumSecurityInfo()");
                    setQuantumSecurityInfo();
                    return;
                }
                Log.i(this.LOG_TAG, "updateQuantumQMKeyStatus sessionKey already updated");
                return;
            }
            if (ImsRegistry.getServiceModuleManager().getQuantumEncryptionServiceModule().onRequestQMKeyWithDelay(new SxRequestQMKeyEntity(this.mCallProfile.getQuantumSecurityInfo().getLocalPhoneNumber(), this.mCallProfile.getQuantumSecurityInfo().getRemoteTelNum(), QuantumSecurityInfo.QUANTUM_ENCRYPT_KEY_LENGTH, str2, this.mCallProfile.getDirection(), this.mCallProfile.getSipCallId(), String.valueOf(this.mRequestMark)), 500) == -1) {
                Log.i(this.LOG_TAG, "onRequestQMKeyWithDelay fail");
                disableQuantumEncryption();
            }
        }
    }

    private void setQuantumSecurityInfo() {
        Bundle bundle = new Bundle();
        bundle.putInt("call_direction", getQuantumSecurityInfo().getCallDirection());
        bundle.putInt("crypto_mode", getQuantumSecurityInfo().getCryptoMode());
        bundle.putString("qt_session_id", getQuantumSecurityInfo().getQtSessionId());
        bundle.putString("session_key", getQuantumSecurityInfo().getSessionKey());
        this.mVolteSvcIntf.setQuantumSecurityInfo(this.mSessionId, bundle);
    }

    private static String bytesToHex(byte[] bArr) {
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            byte b = bArr[i] & 255;
            int i2 = i * 2;
            char[] cArr2 = hexArray;
            cArr[i2] = cArr2[b >>> 4];
            cArr[i2 + 1] = cArr2[b & 15];
        }
        return new String(cArr);
    }

    public void notifyDSDAVideoCapa(boolean z) {
        if (this.mCmcType == 0) {
            this.mCallProfile.setDSDAVideoCapa(z);
            notifyImsGeneralEvent("NotifyDSDAVideoCapa", new Bundle());
        }
    }

    public void removeCallStateMachineMessage(int i) {
        this.smCallStateMachine.removeMessages(i);
    }
}
