package com.sec.internal.ims.core.handler.secims;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.Dialog;
import com.sec.ims.ImsRegistration;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_.Contact;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserAgent extends StateMachine implements IUserAgent {
    private static final String ECC_IWLAN = "IWLAN";
    private static final int EVENT_ACCEPT_CALL_TRANSFER = 21;
    public static final int EVENT_AKA_CHALLENGE_TIME_OUT = 46;
    private static final int EVENT_CREATE_UA = 1;
    private static final int EVENT_DELAYED_DEREGISTERED = 800;
    private static final int EVENT_DELETE_UA = 4;
    private static final int EVENT_DEREGISTERED = 12;
    private static final int EVENT_DEREGISTERED_TIMEOUT = 13;
    private static final int EVENT_DEREGISTER_COMPLETE = 11;
    private static final int EVENT_DISCONNECTED = 100;
    private static final int EVENT_EMERGENCY_REGISTRATION_FAILED = 900;
    private static final int EVENT_ENABLE_QUANTUM_SECURITY_SERVICE = 115;
    private static final int EVENT_RECOVER_REGISESSION = 9000;
    private static final int EVENT_REGISTERED = 8;
    private static final int EVENT_REGISTER_REQUESTED = 7;
    private static final int EVENT_REG_INFO_NOTIFY = 101;
    private static final int EVENT_REQUEST_ANSWER_CALL = 16;
    private static final int EVENT_REQUEST_CANCEL_TRANSFER_CALL = 45;
    private static final int EVENT_REQUEST_DELETE_TCP_CLIENT_SOCKET = 49;
    private static final int EVENT_REQUEST_DEREGISTER = 10;
    private static final int EVENT_REQUEST_DEREGISTER_INTERNAL = 43;
    private static final int EVENT_REQUEST_EMERGENCY_LOCATION_PUBLISH = 65;
    private static final int EVENT_REQUEST_END_CALL = 15;
    private static final int EVENT_REQUEST_EXTEND_TO_CONFERENCE = 107;
    private static final int EVENT_REQUEST_HANDLE_CMC_CSFB = 55;
    private static final int EVENT_REQUEST_HANDLE_DTMF = 23;
    private static final int EVENT_REQUEST_HOLD_CALL = 17;
    private static final int EVENT_REQUEST_HOLD_VIDEO = 26;
    private static final int EVENT_REQUEST_MAKE_CALL = 14;
    private static final int EVENT_REQUEST_MAKE_CONF_CALL = 36;
    private static final int EVENT_REQUEST_MERGE_CALL = 19;
    private static final int EVENT_REQUEST_MODIFY_CALL_TYPE = 104;
    private static final int EVENT_REQUEST_MODIFY_VIDEO_QUALITY = 111;
    private static final int EVENT_REQUEST_NETWORK_SUSPENDED = 38;
    private static final int EVENT_REQUEST_PROGRESS_INCOMING_CALL = 25;
    private static final int EVENT_REQUEST_PUBLISH = 41;
    private static final int EVENT_REQUEST_PUBLISH_DIALOG = 47;
    private static final int EVENT_REQUEST_PULLING_CALL = 29;
    private static final int EVENT_REQUEST_REGISTER = 6;
    private static final int EVENT_REQUEST_REJECT_CALL = 22;
    private static final int EVENT_REQUEST_REJECT_MODIFY_CALL_TYPE = 106;
    private static final int EVENT_REQUEST_REPLY_MODIFY_CALL_TYPE = 105;
    private static final int EVENT_REQUEST_REPLY_WITH_IDC = 62;
    private static final int EVENT_REQUEST_RESUME_CALL = 18;
    private static final int EVENT_REQUEST_RESUME_VIDEO = 27;
    private static final int EVENT_REQUEST_SEND_CMC_INFO = 59;
    private static final int EVENT_REQUEST_SEND_INFO = 48;
    private static final int EVENT_REQUEST_SEND_NEGOTIATED_LOCAL_SDP = 64;
    private static final int EVENT_REQUEST_SEND_TEXT = 51;
    private static final int EVENT_REQUEST_SEND_VCS_INFO = 61;
    private static final int EVENT_REQUEST_START_CAMERA = 28;
    private static final int EVENT_REQUEST_START_CMC_RECORD = 58;
    private static final int EVENT_REQUEST_START_RECORD = 56;
    private static final int EVENT_REQUEST_START_VIDEO_EARLYMEDIA = 54;
    private static final int EVENT_REQUEST_STOP_CAMERA = 30;
    private static final int EVENT_REQUEST_STOP_RECORD = 57;
    private static final int EVENT_REQUEST_TRANSFER_CALL = 20;
    private static final int EVENT_REQUEST_UNPUBLISH = 42;
    private static final int EVENT_REQUEST_UPDATE_CALL = 37;
    private static final int EVENT_REQUEST_UPDATE_CALLWAITING_STATUS = 39;
    private static final int EVENT_REQUEST_UPDATE_WITH_IDC = 63;
    private static final int EVENT_RETRY_UA_CREATE = 3;
    private static final int EVENT_SEND_AUTH_RESPONSE = 9;
    private static final int EVENT_SEND_DTMF_EVENT = 113;
    private static final int EVENT_SEND_MEDIA_EVENT = 1001;
    private static final int EVENT_SEND_REQUEST = 1000;
    private static final int EVENT_SEND_SMS = 31;
    private static final int EVENT_SEND_SMS_RESPONSE = 33;
    private static final int EVENT_SEND_SMS_RP_ACK_RESPONSE = 32;
    private static final int EVENT_SET_QUANTUM_SECURITY_INFO = 114;
    private static final int EVENT_SET_VIDEO_CRT_AUDIO = 112;
    private static final int EVENT_SET_VOWIFI_5GSA_MODE = 60;
    private static final int EVENT_START_LOCAL_RINGBACKTONE = 109;
    private static final int EVENT_STOP_LOCAL_RINGBACKTONE = 110;
    private static final int EVENT_UA_CREATED = 2;
    private static final int EVENT_UA_DELETED = 5;
    private static final int EVENT_UPDATE_AUDIO_INTERFACE = 108;
    private static final int EVENT_UPDATE_CONF_CALL = 35;
    private static final int EVENT_UPDATE_GEOLOCATION = 44;
    private static final int EVENT_UPDATE_PANI = 34;
    private static final int EVENT_UPDATE_PANI_RESPONSE = 2000;
    private static final int EVENT_UPDATE_RAT = 50;
    private static final int EVENT_UPDATE_ROUTE_TABLE = 102;
    private static final int EVENT_UPDATE_TIME_IN_PLANI = 52;
    private static final int EVENT_UPDATE_VCE_CONFIG = 40;
    private static final String LOG_TAG = "UserAgent";
    private static final String PERMISSION = "com.sec.imsservice.PERMISSION";
    private static final String PROPERTY_ECC_PATH = "ril.subtype";
    protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    Context mContext = null;
    private final State mDefaultState = new DefaultState();
    private final State mDeregisteringState = new DeregisteringState();
    /* access modifiers changed from: private */
    public UserAgentState mDestState = UserAgentState.INITIAL;
    private List<NameAddr> mDeviceList = new ArrayList();
    /* access modifiers changed from: private */
    public int mEcmpMode = 0;
    /* access modifiers changed from: private */
    public final State mEmergencyState = new EmergencyState();
    private boolean mEpdgOverCellularData = false;
    private boolean mEpdgStatus = false;
    /* access modifiers changed from: private */
    public SipError mError;
    /* access modifiers changed from: private */
    public int mHandle = -1;
    /* access modifiers changed from: private */
    public List<NameAddr> mImpuList = Collections.synchronizedList(new ArrayList());
    private final IImsFramework mImsFramework;
    /* access modifiers changed from: private */
    public ImsProfile mImsProfile = null;
    private final State mInitialState = new InitialState();
    /* access modifiers changed from: private */
    public UaEventListener mListener = null;
    private Network mNetwork = null;
    /* access modifiers changed from: private */
    public Set<String> mNotifyServiceList = new HashSet();
    /* access modifiers changed from: private */
    public boolean mPcscfGoneDeregi = false;
    private int mPdn;
    private IPdnController mPdnController = null;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    private final State mProhibitedState = new ProhibitedState();
    private final State mReRegisteringState = new ReRegisteringState();
    /* access modifiers changed from: private */
    public final State mReadyState = new ReadyState();
    /* access modifiers changed from: private */
    public String mRegisterSipResponse = null;
    private final State mRegisteredState = new RegisteredState();
    private final State mRegisteringState = new RegisteringState();
    /* access modifiers changed from: private */
    public ImsRegistration mRegistration = null;
    /* access modifiers changed from: private */
    public long mRetryAfterMs;
    /* access modifiers changed from: private */
    public ISimManager mSimManager = null;
    /* access modifiers changed from: private */
    public IStackIF mStackIf = null;
    private boolean mSuspendStatus = false;
    ITelephonyManager mTelephonyManager = null;
    private final State mTerminatingState = new TerminatingState();
    /* access modifiers changed from: private */
    public List<String> mThirdPartyFeatureTags = null;
    /* access modifiers changed from: private */
    public UaProfile mUaProfile = null;

    public interface UaEventListener {
        void onContactActivated(UserAgent userAgent, int i);

        void onCreated(UserAgent userAgent);

        void onDeregistered(UserAgent userAgent, SipError sipError, long j, boolean z, boolean z2);

        void onNotifyNullProfile(UserAgent userAgent);

        void onRefreshRegNotification(int i);

        void onRegEventContactUriNotification(int i, List<ImsUri> list, int i2, String str, String str2);

        void onRegistered(UserAgent userAgent);

        void onRegistrationError(UserAgent userAgent, SipError sipError, long j);

        void onSubscribeError(UserAgent userAgent, SipError sipError);

        void onUpdatePani(UserAgent userAgent);
    }

    public enum UserAgentState {
        DEFAULT,
        INITIAL,
        READY,
        REGISTERING,
        REGISTERED,
        REREGISTERING,
        DEREGISTERING,
        TERMINATING,
        EMERGENCY,
        PROHIBITTED
    }

    public UserAgent(Context context, Handler handler, IStackIF iStackIF, ITelephonyManager iTelephonyManager, IPdnController iPdnController, ISimManager iSimManager, IImsFramework iImsFramework) {
        super("UserAgent - ", handler);
        this.mSimManager = iSimManager;
        this.mImsFramework = iImsFramework;
        this.mPhoneId = iSimManager.getSimSlotIndex();
        initState();
        this.mContext = context;
        this.mStackIf = iStackIF;
        this.mTelephonyManager = iTelephonyManager;
        this.mPdnController = iPdnController;
    }

    public UserAgent(Handler handler, IImsFramework iImsFramework) {
        super("UserAgent - ", handler);
        this.mImsFramework = iImsFramework;
    }

    public void setImsProfile(ImsProfile imsProfile) {
        this.mImsProfile = imsProfile;
    }

    public void setUaProfile(UaProfile uaProfile) {
        this.mUaProfile = uaProfile;
    }

    public void setPdn(int i) {
        this.mPdn = i;
    }

    public int getPdn() {
        return this.mPdn;
    }

    public void setNetwork(Network network) {
        this.mNetwork = network;
    }

    public IPdnController getPdnController() {
        return this.mPdnController;
    }

    public boolean isRegistered(boolean z) {
        return getCurrentState().equals(this.mRegisteredState) || (z && getCurrentState().equals(this.mReRegisteringState));
    }

    public String getStateName() {
        return getCurrentState().getName();
    }

    public void registerListener(UaEventListener uaEventListener) {
        this.mListener = uaEventListener;
    }

    public void unRegisterListener() {
        this.mListener = null;
    }

    public void setThirdPartyFeatureTags(List<String> list) {
        this.mThirdPartyFeatureTags = list;
    }

    public int create() {
        Log.i("UserAgent[" + this.mPhoneId + "]", "create:");
        sendMessage(1);
        return 0;
    }

    public int register() {
        Log.i("UserAgent[" + this.mPhoneId + "]", "register:");
        if (!this.mImsProfile.hasEmergencySupport()) {
            updateEpdgStatus();
        } else if (DeviceUtil.isApAssistedMode()) {
            this.mEpdgStatus = this.mPdnController.isEmergencyEpdgConnected(this.mPhoneId);
        } else {
            String str = SemSystemProperties.get(PROPERTY_ECC_PATH, "");
            Log.i("UserAgent[" + this.mPhoneId + "]", "eccPath : " + str);
            this.mEpdgStatus = str.equalsIgnoreCase(ECC_IWLAN);
        }
        if (!SimUtil.isDualIMS() || !Mno.fromName(this.mImsProfile.getMnoName()).isChn() || !SemSystemProperties.get("ro.boot.hardware", "").contains("qcom")) {
            sendMessage(6);
            return 0;
        }
        sendMessageDelayed(6, 10);
        return 0;
    }

    /* access modifiers changed from: private */
    public void updateEpdgStatus() {
        boolean isEpdgConnected = this.mPdnController.isEpdgConnected(this.mPhoneId);
        this.mEpdgStatus = isEpdgConnected;
        boolean z = false;
        if (isEpdgConnected) {
            if (this.mPdnController.getEpdgPhysicalInterface(this.mPhoneId) == 2) {
                z = true;
            }
            this.mEpdgOverCellularData = z;
        } else {
            this.mEpdgOverCellularData = false;
        }
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null) {
            imsRegistration.setEpdgStatus(this.mEpdgStatus);
            this.mRegistration.setEpdgOverCellularData(this.mEpdgOverCellularData);
        }
    }

    public void deregisterInternal(boolean z) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "deregisterInternal: local=" + z);
        sendMessageDelayed(43, z ? 1 : 0, -1, 500);
    }

    public void deregisterLocal() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "deregisterLocal:");
        sendMessage(13);
    }

    public void suspended(boolean z) {
        this.mSuspendStatus = z;
        sendMessage(38, z ? 1 : 0, -1);
    }

    public boolean getSuspendState() {
        return this.mSuspendStatus;
    }

    public void deleteTcpClientSocket() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "deleteTcpClientSocket:");
        sendMessage(49);
    }

    public void updateAudioInterface(String str, Message message) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "updateAudioInterface: mode =" + str);
        Bundle bundle = new Bundle();
        bundle.putString("mode", str);
        bundle.putParcelable("result", message);
        sendMessage(108, (Object) bundle);
    }

    public void setVideoCrtAudio(int i, boolean z) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "setVideoCrtAudio: sessionId = " + i + ", on = " + z);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putBoolean("vcrtAudioOn", z);
        sendMessage(112, (Object) bundle);
    }

    public void sendDtmfEvent(int i, String str) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "sendDtmfEvent: sessionId = " + i + ", dtmfEvent = " + str);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putString("dtmfEvent", str);
        sendMessage(113, (Object) bundle);
    }

    public void sendRequestToStack(ResipStackRequest resipStackRequest) {
        sendMessage(1000, (Object) resipStackRequest);
    }

    public void makeCall(String str, String str2, int i, String str3, String str4, AdditionalContents additionalContents, String str5, String str6, HashMap<String, String> hashMap, String str7, boolean z, List<String> list, int i2, Bundle bundle, String str8, int i3, String str9, Message message) {
        int i4 = i;
        HashMap<String, String> hashMap2 = hashMap;
        List<String> list2 = list;
        Bundle bundle2 = bundle;
        int i5 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i5, "makeCall: destUri=" + IMSLog.checker(str) + ", type=" + i + " origUri=" + IMSLog.checker(str2));
        Bundle bundle3 = new Bundle();
        String str10 = str;
        bundle3.putString("destUri", str);
        String str11 = str2;
        bundle3.putString("origUri", str2);
        bundle3.putParcelable("result", message);
        bundle3.putInt("type", i);
        if (additionalContents != null) {
            bundle3.putString("additionalContentsContents", additionalContents.contents());
            bundle3.putString("additionalContentsMime", additionalContents.mimeType());
        }
        String str12 = str5;
        bundle3.putString("cli", str5);
        String str13 = str3;
        bundle3.putString("dispName", str3);
        bundle3.putString("alertInfo", str7);
        String str14 = str4;
        bundle3.putString("dialedNumber", str4);
        bundle3.putString("pEmergencyInfo", str6);
        bundle3.putBoolean("isLteEpsOnlyAttached", z);
        if (hashMap2 != null) {
            bundle3.putSerializable("additionalSipHeaders", hashMap2);
        }
        if (list2 != null) {
            bundle3.putStringArrayList("p2p", new ArrayList(list2));
        }
        bundle3.putInt("cmcBoundSessionId", i2);
        if (bundle2 != null && !bundle.isEmpty()) {
            bundle3.putBundle(CallConstants.ComposerData.TAG, bundle2);
        }
        bundle3.putString("replaceCallId", str8);
        bundle3.putInt("cmcEdCallSlot", i3);
        bundle3.putString("idcExtra", str9);
        sendMessage(14, (Object) bundle3);
    }

    public void rejectCall(int i, SipError sipError) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "rejectCall: sessionId " + i);
        sendMessage(22, i, -1, sipError);
    }

    public void progressIncomingCall(int i, HashMap<String, String> hashMap, String str) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "progressIncomingCall: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        if (hashMap != null) {
            bundle.putSerializable("headers", hashMap);
        }
        bundle.putString("idcExtra", str);
        sendMessage(25, (Object) bundle);
    }

    public void endCall(int i, SipReason sipReason) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "endCall: sessionId " + i);
        sendMessage(15, i, -1, sipReason);
    }

    public void answerCall(int i, int i2, String str, String str2) {
        int i3 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i3, "answerCall: sessionId " + i + " callType " + i2 + " cmcCallEstablishTime " + str + " idcExtra " + str2);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("callType", i2);
        bundle.putString("cmcCallTime", str);
        bundle.putString("idcExtra", str2);
        sendMessage(16, (Object) bundle);
    }

    public void handleDtmf(int i, int i2, int i3, int i4, Message message) {
        Log.i("UserAgent[" + this.mPhoneId + "]", "handleDtmf: sessionId " + i + " mode " + i3 + " operation " + i4);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("code", i2);
        bundle.putInt("mode", i3);
        bundle.putInt("operation", i4);
        bundle.putParcelable("result", message);
        sendMessage(23, (Object) bundle);
    }

    public void sendText(int i, String str, int i2) {
        int i3 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i3, "sendText: sessionId " + i + " text " + str + " len " + i2);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putString("text", str);
        bundle.putInt("len", i2);
        sendMessage(51, (Object) bundle);
    }

    public void holdCall(int i, Message message) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "holdCall: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putParcelable("result", message);
        sendMessage(17, (Object) bundle);
    }

    public void resumeCall(int i, Message message) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "resumeCall: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putParcelable("result", message);
        sendMessage(18, (Object) bundle);
    }

    public void holdVideo(int i, Message message) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "holdVideo: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putParcelable("result", message);
        sendMessage(26, (Object) bundle);
    }

    public void resumeVideo(int i, Message message) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "resumeVideo: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putParcelable("result", message);
        sendMessage(27, (Object) bundle);
    }

    public void startCamera(int i, int i2) {
        int i3 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i3, "startCamera: sessionId: " + i + ", cameraId: " + i2);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("cameraId", i2);
        sendMessage(28, (Object) bundle);
    }

    public void stopCamera() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "stopCamera");
        sendMessage(30);
    }

    public void mergeCall(int i, int i2, String str, int i3, String str2, String str3, String str4, String str5, String str6, String str7, String str8, boolean z, HashMap<String, String> hashMap, Message message) {
        HashMap<String, String> hashMap2 = hashMap;
        IMSLog.i(LOG_TAG, this.mPhoneId, "mergeCall: ");
        Bundle bundle = new Bundle();
        int i4 = i;
        bundle.putInt("session1", i);
        int i5 = i2;
        bundle.putInt("session2", i2);
        String str9 = str;
        bundle.putString("confuri", str);
        int i6 = i3;
        bundle.putInt("calltype", i3);
        String str10 = str2;
        bundle.putString("eventSubscribe", str2);
        String str11 = str3;
        bundle.putString("dialogType", str3);
        String str12 = str4;
        bundle.putString("origUri", str4);
        String str13 = str5;
        bundle.putString("referUriType", str5);
        String str14 = str6;
        bundle.putString("removeReferUriType", str6);
        String str15 = str7;
        bundle.putString("referUriAsserted", str7);
        bundle.putString("useAnonymousUpdate", str8);
        bundle.putBoolean("supportPrematureEnd", z);
        if (hashMap2 != null) {
            bundle.putSerializable("extraHeaders", hashMap2);
        }
        bundle.putParcelable("result", message);
        sendMessage(19, (Object) bundle);
    }

    public void conference(String[] strArr, String str, int i, String str2, String str3, String str4, String str5, String str6, String str7, String str8, boolean z, Message message) {
        Bundle bundle = new Bundle();
        bundle.putString("confuri", str);
        bundle.putInt("calltype", i);
        bundle.putString("eventSubscribe", str2);
        bundle.putString("dialogType", str3);
        bundle.putStringArray("participants", strArr);
        bundle.putString("origUri", str4);
        bundle.putString("referUriType", str5);
        bundle.putString("removeReferUriType", str6);
        bundle.putString("referUriAsserted", str7);
        bundle.putString("useAnonymousUpdate", str8);
        bundle.putBoolean("supportPrematureEnd", z);
        bundle.putParcelable("result", message);
        sendMessage(36, (Object) bundle);
    }

    public void extendToConfCall(String[] strArr, String str, int i, String str2, String str3, int i2, String str4, String str5, String str6, String str7, String str8, boolean z) {
        Bundle bundle = new Bundle();
        bundle.putString("confuri", str);
        bundle.putInt("calltype", i);
        bundle.putString("eventSubscribe", str2);
        bundle.putString("dialogType", str3);
        bundle.putStringArray("participants", strArr);
        bundle.putInt("sessId", i2);
        bundle.putString("origUri", str4);
        bundle.putString("referUriType", str5);
        bundle.putString("removeReferUriType", str6);
        bundle.putString("referUriAsserted", str7);
        bundle.putString("useAnonymousUpdate", str8);
        bundle.putBoolean("supportPrematureEnd", z);
        sendMessage(107, (Object) bundle);
    }

    public void updateConfCall(int i, int i2, int i3, String str) {
        int i4 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i4, "updateConfCall  ConfSession " + i + " cmd " + i2 + " participantId " + i3);
        Bundle bundle = new Bundle();
        bundle.putInt("confsession", i);
        bundle.putInt("updateCmd", i2);
        bundle.putInt("participantId", i3);
        bundle.putString("participant", str);
        sendMessage(35, (Object) bundle);
    }

    public void transferCall(int i, String str, int i2, Message message) {
        int i3 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i3, "transferCall: sessionId " + i + " targetUri " + IMSLog.checker(str) + " replacingSessionId " + i2);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putString("targetUri", str);
        if (i2 > 0) {
            bundle.putInt("replacingSessionId", i2);
        }
        bundle.putParcelable("result", message);
        sendMessage(20, (Object) bundle);
    }

    public void cancelTransferCall(int i, Message message) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "cancelTransferCall: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putParcelable("result", message);
        sendMessage(45, (Object) bundle);
    }

    public void pullingCall(String str, String str2, String str3, Dialog dialog, List<String> list, Message message) {
        int i = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("pullingCall: pullingUri=");
        sb.append(IMSLog.checker(str));
        sb.append(", targetUri=");
        sb.append(IMSLog.checker(str2));
        sb.append(", origUri=");
        sb.append(IMSLog.checker(str3));
        sb.append(", targetDialog=");
        sb.append(IMSLog.checker(dialog + ""));
        IMSLog.i(LOG_TAG, i, sb.toString());
        Bundle bundle = new Bundle();
        bundle.putString("pullingUri", str);
        bundle.putString("targetUri", str2);
        bundle.putString("origUri", str3);
        bundle.putParcelable("targetDialog", dialog);
        if (list != null) {
            bundle.putStringArrayList("p2p", new ArrayList(list));
        }
        bundle.putParcelable("result", message);
        sendMessage(29, (Object) bundle);
    }

    public void publishDialog(String str, String str2, String str3, int i, Message message, boolean z) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "publishDialog: origUri=" + IMSLog.checker(str) + ", dispName=" + IMSLog.checker(str2) + ", expires=" + i + "");
        Bundle bundle = new Bundle();
        bundle.putString("origUri", str);
        bundle.putString("dispName", str2);
        bundle.putString("body", str3);
        bundle.putInt("expires", i);
        bundle.putParcelable("result", message);
        if (z) {
            sendMessageDelayed(47, (Object) bundle, 500);
        } else {
            sendMessage(47, (Object) bundle);
        }
    }

    public void acceptCallTranfer(int i, boolean z, int i2, String str) {
        int i3 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i3, "acceptCallTransfer: session " + i + " accepted " + z + " status " + i2 + " reason " + str);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putBoolean("accepted", z);
        if (i2 > 0) {
            bundle.putInt("status", i2);
            bundle.putString("reason", str);
        }
        sendMessage(21, (Object) bundle);
    }

    public void startRecord(int i, String str) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "startRecord: sessionId " + i + " filePath " + str);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putString("filePath", str);
        sendMessage(56, (Object) bundle);
    }

    public void stopRecord(int i) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "stopRecord: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        sendMessage(57, (Object) bundle);
    }

    public void startCmcRecord(int i, int i2, int i3, long j, int i4, String str, int i5, int i6, int i7, int i8, int i9, long j2, String str2) {
        int i10 = i;
        String str3 = str;
        int i11 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i11, "startCmcRecord: sessionId " + i + " filePath " + str);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        int i12 = i2;
        bundle.putInt("audioSource", i2);
        int i13 = i3;
        bundle.putInt("outputFormat", i3);
        long j3 = j;
        bundle.putLong("maxFileSize", j);
        int i14 = i4;
        bundle.putInt("maxDuration", i4);
        bundle.putString("outputPath", str);
        int i15 = i5;
        bundle.putInt("audioEncodingBR", i5);
        int i16 = i6;
        bundle.putInt("audioChannels", i6);
        bundle.putInt("audioSamplingR", i7);
        bundle.putInt("audioEncoder", i8);
        bundle.putInt("durationInterval", i9);
        bundle.putLong("fileSizeInterval", j2);
        bundle.putString("author", str2);
        sendMessage(58, (Object) bundle);
    }

    public void sendSms(String str, String str2, String str3, byte[] bArr, boolean z, String str4, boolean z2, Message message) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "sendSms: scaUri " + IMSLog.checker(str) + " localUri " + IMSLog.checker(str2) + " contentType " + str3 + " isDeleveryReport " + z + " callId " + str4);
        Bundle bundle = new Bundle();
        bundle.putString("sca", str);
        bundle.putString("localuri", str2);
        bundle.putString("contentType", str3);
        bundle.putByteArray("pdu", bArr);
        bundle.putBoolean("isDeliveryReport", z);
        bundle.putParcelable("result", message);
        bundle.putString("callId", str4);
        bundle.putBoolean("isEmergency", z2);
        sendMessage(31, (Object) bundle);
    }

    public void sendSmsRpAckResponse(String str) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "sendSmsRpAckResponse: callId " + str);
        sendMessage(32, (Object) str);
    }

    public void sendSmsResponse(String str, int i) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "sendSmsResponse: callId " + str);
        sendMessage(33, i, 0, str);
    }

    public void modifyCallType(int i, int i2, int i3) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("oldType", i2);
        bundle.putInt("newType", i3);
        sendMessage(104, (Object) bundle);
    }

    public void replyModifyCallType(int i, int i2, int i3, int i4, String str) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("reqType", i4);
        bundle.putInt("curType", i2);
        bundle.putInt("repType", i3);
        bundle.putString("cmcCallTime", str);
        sendMessage(105, (Object) bundle);
    }

    public void replyWithIdc(int i, int i2, int i3, int i4, String str) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("reqType", i4);
        bundle.putInt("curType", i2);
        bundle.putInt("repType", i3);
        bundle.putString("idcExtra", str);
        sendMessage(62, (Object) bundle);
    }

    public void rejectModifyCallType(int i, int i2) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("reason", i2);
        sendMessage(106, (Object) bundle);
    }

    public void updateWithIdc(int i, int i2, String str) {
        int i3 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i3, "updateWithIdc(): sessionId " + i + ", action " + i2);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("action", i2);
        bundle.putString("idcExtra", str);
        sendMessage(63, (Object) bundle);
    }

    public void updateCall(int i, int i2, int i3, SipReason sipReason, String str) {
        int i4 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i4, "updateCall(): sessionId " + i + ", action " + i2);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("action", i2);
        bundle.putInt("codecType", i3);
        bundle.putInt("cause", sipReason.getCause());
        bundle.putString("reasonText", sipReason.getText());
        bundle.putString("idcExtra", str);
        sendMessage(37, (Object) bundle);
    }

    public void sendInfo(int i, int i2, int i3, AdditionalContents additionalContents, Message message) {
        int i4 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i4, "sendInfo: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("calltype", i2);
        bundle.putInt("ussdtype", i3);
        bundle.putParcelable("result", message);
        if (additionalContents != null) {
            bundle.putString("additionalContentsContents", additionalContents.contents());
            bundle.putString("additionalContentsMime", additionalContents.mimeType());
        }
        sendMessage(48, (Object) bundle);
    }

    public void sendEmergencyLocationPublish(int i, Message message) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "sendEmergencyLocationPublish: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putParcelable("result", message);
        sendMessage(65, (Object) bundle);
    }

    public void sendCmcInfo(int i, AdditionalContents additionalContents) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "sendCmcInfo: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        if (additionalContents != null) {
            bundle.putString("additionalContentsContents", additionalContents.contents());
            bundle.putString("additionalContentsMime", additionalContents.mimeType());
        }
        sendMessage(59, (Object) bundle);
    }

    public void sendVcsInfo(int i, AdditionalContents additionalContents) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "sendVcsInfo: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        if (additionalContents != null) {
            bundle.putString("additionalContentsContents", additionalContents.contents());
            bundle.putString("additionalContentsMime", additionalContents.mimeType());
        }
        sendMessage(61, (Object) bundle);
    }

    public void enableQuantumSecurityService(int i, boolean z) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "enableQuantumSecurityService: sessionId " + i + " enable " + z);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putBoolean("enable", z);
        sendMessage(115, (Object) bundle);
    }

    public void setQuantumSecurityInfo(int i, int i2, int i3, String str, String str2) {
        int i4 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i4, "setQuantumSecurityInfo: sessionId " + i + " callDirection " + i2 + " cryptoMode " + i3 + " qtSessionId " + IMSLog.checker(str) + " sessionKey " + IMSLog.checker(str2));
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("callDirection", i2);
        bundle.putInt("cryptoMode", i3);
        bundle.putString("qtSessionId", str);
        bundle.putString("sessionKey", str2);
        sendMessage(114, (Object) bundle);
    }

    public void startVideoEarlyMedia(int i) {
        Log.i(LOG_TAG, "startVideoEarlyMedia: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        sendMessage(54, (Object) bundle);
    }

    public void handleCmcCsfb(int i) {
        Log.i(LOG_TAG, "handleCmcCsfb: sessionId " + i);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        sendMessage(55, (Object) bundle);
    }

    public void updateCallwaitingStatus() {
        if (!this.mImsFramework.getBoolean(this.mPhoneId, GlobalSettingsConstants.SS.CALLWAITING_BY_NETWORK, false)) {
            sendMessage(39);
        }
    }

    public void requestPublish(PresenceInfo presenceInfo, Message message) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("presenceInfo", presenceInfo);
        bundle.putParcelable("result", message);
        sendMessage(41, (Object) bundle);
    }

    public void requestUnpublish() {
        sendMessage(42);
    }

    public void sendMediaEvent(int i, int i2, int i3) {
        Bundle bundle = new Bundle();
        bundle.putInt(SoftphoneNamespaces.SoftphoneCallHandling.TARGET, i);
        bundle.putInt("event", i2);
        bundle.putInt("eventType", i3);
        sendMessage(1001, (Object) bundle);
    }

    public void sendNegotiatedLocalSdp(int i, String str) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putString(IdcExtra.Key.SDP, str);
        sendMessage(64, (Object) bundle);
    }

    private void initState() {
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mReadyState, this.mDefaultState);
        addState(this.mRegisteringState, this.mReadyState);
        addState(this.mRegisteredState, this.mReadyState);
        addState(this.mReRegisteringState, this.mRegisteredState);
        addState(this.mDeregisteringState, this.mReadyState);
        addState(this.mTerminatingState, this.mReadyState);
        addState(this.mProhibitedState, this.mDefaultState);
        addState(this.mEmergencyState, this.mReadyState);
        setInitialState(this.mInitialState);
        start();
    }

    /* access modifiers changed from: private */
    public void setDestState(UserAgentState userAgentState) {
        Log.i("UserAgent[" + this.mPhoneId + "]", "setDestState to : " + userAgentState);
        this.mDestState = userAgentState;
        if (userAgentState == UserAgentState.DEFAULT) {
            transitionTo(this.mDefaultState);
        } else if (userAgentState == UserAgentState.READY) {
            transitionTo(this.mReadyState);
        } else if (userAgentState == UserAgentState.INITIAL) {
            transitionTo(this.mInitialState);
        } else if (userAgentState == UserAgentState.REGISTERING) {
            transitionTo(this.mRegisteringState);
        } else if (userAgentState == UserAgentState.REGISTERED) {
            transitionTo(this.mRegisteredState);
        } else if (userAgentState == UserAgentState.REREGISTERING) {
            transitionTo(this.mReRegisteringState);
        } else if (userAgentState == UserAgentState.DEREGISTERING) {
            transitionTo(this.mDeregisteringState);
        } else if (userAgentState == UserAgentState.TERMINATING) {
            transitionTo(this.mTerminatingState);
        } else if (userAgentState == UserAgentState.EMERGENCY) {
            transitionTo(this.mEmergencyState);
        } else if (userAgentState == UserAgentState.PROHIBITTED) {
            transitionTo(this.mProhibitedState);
        } else {
            Log.e(LOG_TAG, "Unexpected State : " + userAgentState);
            transitionTo(this.mDefaultState);
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            Log.i(UserAgent.LOG_TAG, UserAgent.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 13) {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_DEREGISTERED_TIMEOUT");
                if (UserAgent.this.mListener == null) {
                    return true;
                }
                UserAgent.this.mListener.onDeregistered(UserAgent.this, SipErrorBase.OK, 0, true, false);
                return true;
            } else if (i != 41) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "Unexpected event " + message.what + ". current state is " + UserAgent.this.getCurrentState().getName());
                return false;
            } else {
                Message message2 = (Message) ((Bundle) message.obj).getParcelable("result");
                FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
                GeneralResponse.startGeneralResponse(flatBufferBuilder);
                GeneralResponse.addHandle(flatBufferBuilder, (long) UserAgent.this.mHandle);
                GeneralResponse.addResult(flatBufferBuilder, 1);
                flatBufferBuilder.finish(GeneralResponse.endGeneralResponse(flatBufferBuilder));
                AsyncResult.forMessage(message2, GeneralResponse.getRootAsGeneralResponse(flatBufferBuilder.dataBuffer()), (Throwable) null);
                message2.sendToTarget();
                return true;
            }
        }
    }

    private class InitialState extends State {
        private InitialState() {
        }

        public void enter() {
            Log.i(UserAgent.LOG_TAG, UserAgent.this.getCurrentState().getName() + " enter.");
            UserAgent.this.mHandle = -1;
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                UserAgent.this.mStackIf.createUA(UserAgent.this.mUaProfile, UserAgent.this.obtainMessage(2));
                return true;
            } else if (i == 2) {
                AsyncResult asyncResult = (AsyncResult) message.obj;
                GeneralResponse generalResponse = (GeneralResponse) asyncResult.result;
                if (asyncResult.exception == null && generalResponse != null) {
                    Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "UA created. handle " + generalResponse.handle() + " result " + generalResponse.result() + " reason " + generalResponse.reason());
                    if (generalResponse.result() == 0) {
                        UserAgent.this.mHandle = (int) generalResponse.handle();
                        if (UserAgent.this.mImsProfile.isUicclessEmergency()) {
                            IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "No need for emergency registration. Move to EmergencyState.");
                            UserAgent userAgent = UserAgent.this;
                            userAgent.transitionTo(userAgent.mEmergencyState);
                        } else {
                            UserAgent userAgent2 = UserAgent.this;
                            userAgent2.transitionTo(userAgent2.mReadyState);
                        }
                        UserAgent.this.mStackIf.registerUaListener(UserAgent.this.mHandle, new EventListener());
                        if (UserAgent.this.mListener != null) {
                            UserAgent.this.mListener.onCreated(UserAgent.this);
                        }
                        return true;
                    } else if (generalResponse.reason() == 6) {
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "create() failed. notify with null agent");
                        if (UserAgent.this.mListener != null) {
                            UserAgent.this.mListener.onCreated((UserAgent) null);
                        }
                        return true;
                    }
                }
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "create() failed. retry 3 seconds later ");
                UserAgent.this.sendMessageDelayed(3, (long) RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
                return true;
            } else if (i == 3) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "retry UA creation...");
                UserAgent.this.create();
                return true;
            } else if (i == 4) {
                UserAgent.this.deferMessage(message);
                return true;
            } else if (i == 5) {
                UserAgent.this.mStackIf.deleteUA(UserAgent.this.mHandle, UserAgent.this.obtainMessage(5));
                UserAgent.this.setDestState(UserAgentState.TERMINATING);
                return true;
            } else if (i != 10) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "Unexpected event " + message.what + ". current state is " + UserAgent.this.getCurrentState().getName());
                return false;
            } else {
                IMSLog.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "Event " + message.what + " received in  " + UserAgent.this.getCurrentState().getName() + " This shouldn't be handled here - defer");
                UserAgent.this.deferMessage(message);
                return true;
            }
        }
    }

    private class ReadyState extends State {
        private ReadyState() {
        }

        public void enter() {
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, r0, UserAgent.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            Message message2 = message;
            int i = message2.what;
            if (i == 1) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "UserAgent is already created.");
                return true;
            } else if (i == 12) {
                UserAgent.this.setDestState(UserAgentState.READY);
                return true;
            } else if (i == 15) {
                UserAgent.this.mStackIf.endCall(UserAgent.this.mHandle, message2.arg1, (SipReason) message2.obj, (Message) null);
                return true;
            } else if (i == 34) {
                List list = (List) message2.obj;
                if (UserAgent.this.mImsProfile != null && Mno.fromName(UserAgent.this.mImsProfile.getMnoName()).isKor()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("real_pani", (String) list.get(0));
                    ImsSharedPrefHelper.put(UserAgent.this.mPhoneId, UserAgent.this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, contentValues);
                    String string = ImsSharedPrefHelper.getString(UserAgent.this.mPhoneId, UserAgent.this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, "fake_pani", "");
                    if (!TextUtils.isEmpty(string)) {
                        list.set(0, string);
                    }
                }
                UserAgent.this.mStackIf.updatePani(UserAgent.this.mHandle, list, UserAgent.this.obtainMessage(2000));
                return true;
            } else if (i == 44) {
                UserAgent.this.mStackIf.updateGeolocation(UserAgent.this.mHandle, (LocationInfo) message2.obj);
                return true;
            } else if (i == 50) {
                UserAgent.this.mStackIf.updateRat(UserAgent.this.mHandle, message2.arg1);
                return true;
            } else if (i == 52) {
                UserAgent.this.mStackIf.updateTimeInPlani(UserAgent.this.mHandle, ((Long) message2.obj).longValue());
                return true;
            } else if (i != 60) {
                if (i != 100) {
                    if (i == 108) {
                        Bundle bundle = (Bundle) message2.obj;
                        UserAgent.this.mStackIf.updateAudioInterface(UserAgent.this.mHandle, bundle.getString("mode"), (Message) bundle.getParcelable("result"));
                        return true;
                    } else if (i == 4) {
                        UserAgent.this.mStackIf.deleteUA(UserAgent.this.mHandle, UserAgent.this.obtainMessage(5));
                        UserAgent.this.setDestState(UserAgentState.TERMINATING);
                        return true;
                    } else if (i != 5) {
                        if (i == 6) {
                            ArrayList arrayList = new ArrayList();
                            arrayList.addAll(UserAgent.this.mUaProfile.getServiceList());
                            UserAgent.this.mStackIf.register(UserAgent.this.mHandle, UserAgent.this.mUaProfile.getPcscfIp(), UserAgent.this.mUaProfile.getPcscfPort(), UserAgent.this.mUaProfile.getRegExpires(), arrayList, UserAgent.this.mUaProfile.getLinkedImpuList(), UserAgent.this.mUaProfile.getOwnCapabilities(), UserAgent.this.mThirdPartyFeatureTags, UserAgent.this.mUaProfile.getAccessToken(), UserAgent.this.mUaProfile.getAuthServerUrl(), UserAgent.this.mUaProfile.getSingleRegiEnabled(), UserAgent.this.mUaProfile.getImMsgTech(), UserAgent.this.mUaProfile.getIsAddMmtelCallComposerTag(), UserAgent.this.obtainMessage(7));
                            UserAgent.this.setDestState(UserAgentState.REGISTERING);
                            return true;
                        } else if (i == 9) {
                            UserAgent.this.mStackIf.sendAuthResponse(UserAgent.this.mHandle, message2.arg1, (String) message2.obj);
                            return true;
                        } else if (i != 10) {
                            return false;
                        } else {
                            IMSLog.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "Event " + message2.what + " received in  " + UserAgent.this.getCurrentState().getName() + " This shouldn't be handled here - defer");
                            UserAgent.this.deferMessage(message2);
                            return true;
                        }
                    }
                }
                UserAgent.this.setDestState(UserAgentState.INITIAL);
                return true;
            } else {
                UserAgent.this.mStackIf.setVowifi5gsaMode(UserAgent.this.mHandle, message2.arg1);
                return true;
            }
        }
    }

    private class RegisteringState extends State {
        private RegisteringState() {
        }

        public void enter() {
            Log.i(UserAgent.LOG_TAG, UserAgent.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 4) {
                UserAgent.this.mStackIf.deleteUA(UserAgent.this.mHandle, UserAgent.this.obtainMessage(5));
                UserAgent.this.setDestState(UserAgentState.TERMINATING);
                return true;
            } else if (i != 10) {
                boolean z = false;
                if (i == 38) {
                    IStackIF r0 = UserAgent.this.mStackIf;
                    int r9 = UserAgent.this.mHandle;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    r0.networkSuspended(r9, z);
                    return true;
                } else if (i == 41) {
                    UserAgent.this.deferMessage(message);
                    return true;
                } else if (i == 43) {
                    Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + "EVENT_REQUEST_DEREGISTER_INTERNAL");
                    IStackIF r02 = UserAgent.this.mStackIf;
                    int r1 = UserAgent.this.mHandle;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    r02.deregister(r1, z, UserAgent.this.obtainMessage(11));
                    UserAgent.this.setDestState(UserAgentState.DEREGISTERING);
                    return true;
                } else if (i == 46) {
                    IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_AKA_CHALLENGE_TIME_OUT");
                    if (UserAgent.this.mListener == null) {
                        return true;
                    }
                    UserAgent.this.mListener.onRegistrationError(UserAgent.this, SipErrorBase.OK, UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    return true;
                } else if (i == 900) {
                    Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "[Registering] emergency registration failed. move on to emergency state.");
                    UserAgent.this.setDestState(UserAgentState.EMERGENCY);
                    return true;
                } else if (i == 2000) {
                    GeneralResponse generalResponse = (GeneralResponse) ((AsyncResult) message.obj).result;
                    if (!Mno.fromName(UserAgent.this.mImsProfile.getMnoName()).isKor() || generalResponse.result() != 1 || generalResponse.reason() != 4) {
                        return true;
                    }
                    IMSLog.c(LogClass.REGI_DO_RECOVERY_ACTION, "imsprofile is null. recover it", true);
                    UserAgent.this.mListener.onNotifyNullProfile(UserAgent.this);
                    return true;
                } else if (i != 7) {
                    if (i == 8) {
                        UserAgent.this.setDestState(UserAgentState.REGISTERED);
                        return true;
                    } else if (i == 12) {
                        Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " Defer event " + message.what);
                        UserAgent.this.deferMessage(message);
                        return true;
                    } else if (i != 13) {
                        return false;
                    } else {
                        Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_DELETE_UA");
                        if (UserAgent.this.mListener == null) {
                            return true;
                        }
                        UserAgent.this.mListener.onRegistrationError(UserAgent.this, SipErrorBase.OK, UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                        return true;
                    }
                } else if (((AsyncResult) message.obj).exception == null) {
                    return true;
                } else {
                    Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "register() failed. retry in 3 seconds.");
                    UserAgent.this.sendMessageDelayed(6, (long) RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
                    UserAgent.this.setDestState(UserAgentState.READY);
                    return true;
                }
            } else {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_REQUEST_DEREGISTER");
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " Defer EVENT_REQUEST_DEREGISTER");
                UserAgent.this.setDestState(UserAgentState.DEREGISTERING);
                return true;
            }
        }
    }

    private class RegisteredState extends State {
        private RegisteredState() {
        }

        public void enter() {
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, r0, getName() + " enter.");
            onRegistered();
        }

        public void exit() {
            if (UserAgent.this.mDestState != UserAgentState.REGISTERED) {
                if (!(UserAgent.this.mDestState == UserAgentState.DEREGISTERING || UserAgent.this.mDestState == UserAgentState.TERMINATING || UserAgent.this.mListener == null)) {
                    if (UserAgent.this.mError == null) {
                        Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "exit: Unknown error.");
                        UserAgent.this.mError = SipErrorBase.UNKNOWN_LOCAL_ERROR;
                    }
                    Log.d(UserAgent.LOG_TAG, "[" + UserAgent.this.mPhoneId + "] UA.RegisteredState.exit() mPcscfGoneDeregi = " + UserAgent.this.mPcscfGoneDeregi);
                    UaEventListener r1 = UserAgent.this.mListener;
                    UserAgent userAgent = UserAgent.this;
                    r1.onDeregistered(userAgent, userAgent.mError, UserAgent.this.mRetryAfterMs, false, UserAgent.this.mPcscfGoneDeregi);
                    UserAgent.this.mPcscfGoneDeregi = false;
                }
                if (UserAgent.this.mDestState != UserAgentState.REREGISTERING) {
                    UserAgent.this.mRegistration = null;
                }
                UserAgent.this.mError = null;
            }
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(android.os.Message r40) {
            /*
                r39 = this;
                r0 = r39
                r1 = r40
                int r2 = r1.what
                r4 = 4
                if (r2 == r4) goto L_0x0a91
                r6 = 6
                if (r2 == r6) goto L_0x09fb
                r6 = 101(0x65, float:1.42E-43)
                if (r2 == r6) goto L_0x09f2
                r6 = 102(0x66, float:1.43E-43)
                if (r2 == r6) goto L_0x09e2
                java.lang.String r6 = "targetUri"
                java.lang.String r7 = "cmcCallTime"
                java.lang.String r8 = "supportPrematureEnd"
                java.lang.String r9 = "useAnonymousUpdate"
                java.lang.String r10 = "referUriAsserted"
                java.lang.String r11 = "removeReferUriType"
                java.lang.String r12 = "referUriType"
                java.lang.String r13 = "dialogType"
                java.lang.String r14 = "eventSubscribe"
                java.lang.String r15 = "confuri"
                java.lang.String r3 = "calltype"
                java.lang.String r4 = "additionalContentsMime"
                java.lang.String r5 = "additionalContentsContents"
                r17 = r6
                java.lang.String r6 = "idcExtra"
                r18 = r4
                java.lang.String r4 = "origUri"
                r19 = r5
                java.lang.String r5 = "result"
                r21 = r6
                java.lang.String r6 = "sessionId"
                switch(r2) {
                    case 6: goto L_0x09fb;
                    case 8: goto L_0x09bd;
                    case 10: goto L_0x0994;
                    case 14: goto L_0x08d3;
                    case 15: goto L_0x08bb;
                    case 16: goto L_0x0892;
                    case 17: goto L_0x0873;
                    case 18: goto L_0x0854;
                    case 19: goto L_0x07f7;
                    case 20: goto L_0x07ca;
                    case 21: goto L_0x079c;
                    case 22: goto L_0x0784;
                    case 23: goto L_0x0752;
                    case 45: goto L_0x0733;
                    case 51: goto L_0x070d;
                    case 1000: goto L_0x06fe;
                    case 1001: goto L_0x06d6;
                    case 2000: goto L_0x0694;
                    case 9000: goto L_0x0687;
                    default: goto L_0x0049;
                }
            L_0x0049:
                switch(r2) {
                    case 25: goto L_0x065e;
                    case 26: goto L_0x063f;
                    case 27: goto L_0x0620;
                    case 28: goto L_0x0601;
                    case 29: goto L_0x05c4;
                    case 30: goto L_0x05b3;
                    case 31: goto L_0x05aa;
                    case 32: goto L_0x0595;
                    case 33: goto L_0x057e;
                    default: goto L_0x004c;
                }
            L_0x004c:
                switch(r2) {
                    case 35: goto L_0x0550;
                    case 36: goto L_0x0505;
                    case 37: goto L_0x04d9;
                    case 38: goto L_0x04c0;
                    default: goto L_0x004f;
                }
            L_0x004f:
                switch(r2) {
                    case 40: goto L_0x04a7;
                    case 41: goto L_0x0483;
                    case 42: goto L_0x0472;
                    default: goto L_0x0052;
                }
            L_0x0052:
                switch(r2) {
                    case 47: goto L_0x0440;
                    case 48: goto L_0x03e3;
                    case 49: goto L_0x03d2;
                    default: goto L_0x0055;
                }
            L_0x0055:
                switch(r2) {
                    case 54: goto L_0x03b9;
                    case 55: goto L_0x03a0;
                    case 56: goto L_0x0381;
                    case 57: goto L_0x0368;
                    case 58: goto L_0x0307;
                    case 59: goto L_0x02bc;
                    default: goto L_0x0058;
                }
            L_0x0058:
                switch(r2) {
                    case 61: goto L_0x0271;
                    case 62: goto L_0x0244;
                    case 63: goto L_0x0225;
                    case 64: goto L_0x020b;
                    case 65: goto L_0x01f8;
                    default: goto L_0x005b;
                }
            L_0x005b:
                switch(r2) {
                    case 104: goto L_0x01d9;
                    case 105: goto L_0x01ae;
                    case 106: goto L_0x0194;
                    case 107: goto L_0x014a;
                    default: goto L_0x005e;
                }
            L_0x005e:
                switch(r2) {
                    case 109: goto L_0x0119;
                    case 110: goto L_0x0108;
                    case 111: goto L_0x00e9;
                    case 112: goto L_0x00c9;
                    case 113: goto L_0x00aa;
                    case 114: goto L_0x007d;
                    case 115: goto L_0x0064;
                    default: goto L_0x0061;
                }
            L_0x0061:
                r5 = 0
                goto L_0x0ab1
            L_0x0064:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r0 = r0.mStackIf
                int r2 = r1.getInt(r6)
                java.lang.String r3 = "enable"
                boolean r1 = r1.getBoolean(r3)
                r0.enableQuantumSecurityService(r2, r1)
                goto L_0x0a8f
            L_0x007d:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r7 = r0.mStackIf
                int r8 = r1.getInt(r6)
                java.lang.String r0 = "callDirection"
                int r9 = r1.getInt(r0)
                java.lang.String r0 = "cryptoMode"
                int r10 = r1.getInt(r0)
                java.lang.String r0 = "qtSessionId"
                java.lang.String r11 = r1.getString(r0)
                java.lang.String r0 = "sessionKey"
                java.lang.String r12 = r1.getString(r0)
                r7.setQuantumSecurityInfo(r8, r9, r10, r11, r12)
                goto L_0x0a8f
            L_0x00aa:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                java.lang.String r4 = "dtmfEvent"
                java.lang.String r1 = r1.getString(r4)
                r2.sendDtmfEvent(r0, r3, r1)
                goto L_0x0a8f
            L_0x00c9:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                java.lang.String r4 = "vcrtAudioOn"
                boolean r1 = r1.getBoolean(r4)
                r2.setVideoCrtAudio(r0, r3, r1)
                goto L_0x0a8f
            L_0x00e9:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r0 = r0.mStackIf
                int r2 = r1.getInt(r6)
                java.lang.String r3 = "oldQual"
                int r3 = r1.getInt(r3)
                java.lang.String r4 = "newQual"
                int r1 = r1.getInt(r4)
                r0.modifyVideoQuality(r2, r3, r1)
                goto L_0x0a8f
            L_0x0108:
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r1 = r1.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                r1.stopLocalRingBackTone(r0)
                goto L_0x0a8f
            L_0x0119:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r6 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r7 = r0.mHandle
                java.lang.String r0 = "streamType"
                int r8 = r1.getInt(r0)
                java.lang.String r0 = "volume"
                int r9 = r1.getInt(r0)
                java.lang.String r0 = "toneType"
                int r10 = r1.getInt(r0)
                android.os.Parcelable r0 = r1.getParcelable(r5)
                r11 = r0
                android.os.Message r11 = (android.os.Message) r11
                r6.startLocalRingBackTone(r7, r8, r9, r10, r11)
                goto L_0x0a8f
            L_0x014a:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r17 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r18 = r0.mHandle
                java.lang.String r19 = r1.getString(r15)
                int r20 = r1.getInt(r3)
                java.lang.String r21 = r1.getString(r14)
                java.lang.String r22 = r1.getString(r13)
                java.lang.String r0 = "participants"
                java.lang.String[] r23 = r1.getStringArray(r0)
                java.lang.String r0 = "sessId"
                int r24 = r1.getInt(r0)
                java.lang.String r25 = r1.getString(r4)
                java.lang.String r26 = r1.getString(r12)
                java.lang.String r27 = r1.getString(r11)
                java.lang.String r28 = r1.getString(r10)
                java.lang.String r29 = r1.getString(r9)
                boolean r30 = r1.getBoolean(r8)
                r17.extendToConfCall(r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30)
                goto L_0x0a8f
            L_0x0194:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r0 = r0.mStackIf
                int r2 = r1.getInt(r6)
                java.lang.String r3 = "reason"
                int r1 = r1.getInt(r3)
                r0.rejectModifyCallType(r2, r1)
                goto L_0x0a8f
            L_0x01ae:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r8 = r0.mStackIf
                int r9 = r1.getInt(r6)
                java.lang.String r0 = "reqType"
                int r10 = r1.getInt(r0)
                java.lang.String r0 = "curType"
                int r11 = r1.getInt(r0)
                java.lang.String r0 = "repType"
                int r12 = r1.getInt(r0)
                java.lang.String r13 = r1.getString(r7)
                r8.replyModifyCallType(r9, r10, r11, r12, r13)
                goto L_0x0a8f
            L_0x01d9:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r0 = r0.mStackIf
                int r2 = r1.getInt(r6)
                java.lang.String r3 = "oldType"
                int r3 = r1.getInt(r3)
                java.lang.String r4 = "newType"
                int r1 = r1.getInt(r4)
                r0.modifyCallType(r2, r3, r1)
                goto L_0x0a8f
            L_0x01f8:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r0 = r0.mStackIf
                int r1 = r1.getInt(r6)
                r0.sendEmergencyLocationPublish(r1)
                goto L_0x0a8f
            L_0x020b:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r0 = r0.mStackIf
                int r2 = r1.getInt(r6)
                java.lang.String r3 = "sdp"
                java.lang.String r1 = r1.getString(r3)
                r0.sendNegotiatedLocalSdp(r2, r1)
                goto L_0x0a8f
            L_0x0225:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r0 = r0.mStackIf
                int r2 = r1.getInt(r6)
                java.lang.String r3 = "action"
                int r3 = r1.getInt(r3)
                r8 = r21
                java.lang.String r1 = r1.getString(r8)
                r0.updateWithIdc(r2, r3, r1)
                goto L_0x0a8f
            L_0x0244:
                r8 = r21
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r9 = r0.mStackIf
                int r10 = r1.getInt(r6)
                java.lang.String r0 = "reqType"
                int r11 = r1.getInt(r0)
                java.lang.String r0 = "curType"
                int r12 = r1.getInt(r0)
                java.lang.String r0 = "repType"
                int r13 = r1.getInt(r0)
                java.lang.String r14 = r1.getString(r8)
                r9.replyWithIdc(r10, r11, r12, r13, r14)
                goto L_0x0a8f
            L_0x0271:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                r2 = r19
                java.lang.String r2 = r1.getString(r2)
                r7 = r18
                java.lang.String r3 = r1.getString(r7)
                com.google.flatbuffers.FlatBufferBuilder r4 = new com.google.flatbuffers.FlatBufferBuilder
                r5 = 0
                r4.<init>(r5)
                int r2 = r4.createString((java.lang.CharSequence) r2)
                int r3 = r4.createString((java.lang.CharSequence) r3)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.startAdditionalContents(r4)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.addMimeType(r4, r3)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.addContents(r4, r2)
                int r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.endAdditionalContents(r4)
                r4.finish(r2)
                java.nio.ByteBuffer r2 = r4.dataBuffer()
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.getRootAsAdditionalContents(r2)
                com.sec.internal.ims.core.handler.secims.UserAgent r3 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r3 = r3.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r1 = r1.getInt(r6)
                r3.sendVcsInfo(r0, r1, r2)
                goto L_0x0a8f
            L_0x02bc:
                r7 = r18
                r2 = r19
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                java.lang.String r2 = r1.getString(r2)
                java.lang.String r3 = r1.getString(r7)
                com.google.flatbuffers.FlatBufferBuilder r4 = new com.google.flatbuffers.FlatBufferBuilder
                r5 = 0
                r4.<init>(r5)
                int r2 = r4.createString((java.lang.CharSequence) r2)
                int r3 = r4.createString((java.lang.CharSequence) r3)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.startAdditionalContents(r4)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.addMimeType(r4, r3)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.addContents(r4, r2)
                int r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.endAdditionalContents(r4)
                r4.finish(r2)
                java.nio.ByteBuffer r2 = r4.dataBuffer()
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.getRootAsAdditionalContents(r2)
                com.sec.internal.ims.core.handler.secims.UserAgent r3 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r3 = r3.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r1 = r1.getInt(r6)
                r3.sendCmcInfo(r0, r1, r2)
                goto L_0x0a8f
            L_0x0307:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r17 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r18 = r0.mHandle
                int r19 = r1.getInt(r6)
                java.lang.String r0 = "audioSource"
                int r20 = r1.getInt(r0)
                java.lang.String r0 = "outputFormat"
                int r21 = r1.getInt(r0)
                java.lang.String r0 = "maxFileSize"
                long r22 = r1.getLong(r0)
                java.lang.String r0 = "maxDuration"
                int r24 = r1.getInt(r0)
                java.lang.String r0 = "outputPath"
                java.lang.String r25 = r1.getString(r0)
                java.lang.String r0 = "audioEncodingBR"
                int r26 = r1.getInt(r0)
                java.lang.String r0 = "audioChannels"
                int r27 = r1.getInt(r0)
                java.lang.String r0 = "audioSamplingR"
                int r28 = r1.getInt(r0)
                java.lang.String r0 = "audioEncoder"
                int r29 = r1.getInt(r0)
                java.lang.String r0 = "durationInterval"
                int r30 = r1.getInt(r0)
                java.lang.String r0 = "fileSizeInterval"
                long r31 = r1.getLong(r0)
                java.lang.String r0 = "author"
                java.lang.String r33 = r1.getString(r0)
                r17.startCmcRecord(r18, r19, r20, r21, r22, r24, r25, r26, r27, r28, r29, r30, r31, r33)
                goto L_0x0a8f
            L_0x0368:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r1 = r1.getInt(r6)
                r2.stopRecord(r0, r1)
                goto L_0x0a8f
            L_0x0381:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                java.lang.String r4 = "filePath"
                java.lang.String r1 = r1.getString(r4)
                r2.startRecord(r0, r3, r1)
                goto L_0x0a8f
            L_0x03a0:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r1 = r1.getInt(r6)
                r2.handleCmcCsfb(r0, r1)
                goto L_0x0a8f
            L_0x03b9:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r1 = r1.getInt(r6)
                r2.startVideoEarlyMedia(r0, r1)
                goto L_0x0a8f
            L_0x03d2:
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r1 = r1.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                r1.deleteTcpClientSocket(r0)
                goto L_0x0a8f
            L_0x03e3:
                r7 = r18
                r2 = r19
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                java.lang.String r2 = r1.getString(r2)
                java.lang.String r4 = r1.getString(r7)
                com.google.flatbuffers.FlatBufferBuilder r7 = new com.google.flatbuffers.FlatBufferBuilder
                r8 = 0
                r7.<init>(r8)
                int r2 = r7.createString((java.lang.CharSequence) r2)
                int r4 = r7.createString((java.lang.CharSequence) r4)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.startAdditionalContents(r7)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.addMimeType(r7, r4)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.addContents(r7, r2)
                int r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.endAdditionalContents(r7)
                r7.finish(r2)
                java.nio.ByteBuffer r2 = r7.dataBuffer()
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r12 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.getRootAsAdditionalContents(r2)
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r7 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r8 = r0.mHandle
                int r9 = r1.getInt(r6)
                int r10 = r1.getInt(r3)
                java.lang.String r0 = "ussdtype"
                int r11 = r1.getInt(r0)
                android.os.Parcelable r0 = r1.getParcelable(r5)
                r13 = r0
                android.os.Message r13 = (android.os.Message) r13
                r7.sendInfo(r8, r9, r10, r11, r12, r13)
                goto L_0x0a8f
            L_0x0440:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r6 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r7 = r0.mHandle
                java.lang.String r8 = r1.getString(r4)
                java.lang.String r0 = "dispName"
                java.lang.String r9 = r1.getString(r0)
                java.lang.String r0 = "body"
                java.lang.String r10 = r1.getString(r0)
                java.lang.String r0 = "expires"
                int r11 = r1.getInt(r0)
                android.os.Parcelable r0 = r1.getParcelable(r5)
                r12 = r0
                android.os.Message r12 = (android.os.Message) r12
                r6.publishDialog(r7, r8, r9, r10, r11, r12)
                goto L_0x0a8f
            L_0x0472:
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r1 = r1.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                r1.requestUnpublish(r0)
                goto L_0x0a8f
            L_0x0483:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                java.lang.String r3 = "presenceInfo"
                android.os.Parcelable r3 = r1.getParcelable(r3)
                com.sec.ims.presence.PresenceInfo r3 = (com.sec.ims.presence.PresenceInfo) r3
                android.os.Parcelable r1 = r1.getParcelable(r5)
                android.os.Message r1 = (android.os.Message) r1
                r2.requestPublish(r0, r3, r1)
                goto L_0x0a8f
            L_0x04a7:
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                java.lang.Object r1 = r1.obj
                java.lang.Boolean r1 = (java.lang.Boolean) r1
                boolean r1 = r1.booleanValue()
                r2.updateVceConfig(r0, r1)
                goto L_0x0a8f
            L_0x04c0:
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r1 = r1.arg1
                r3 = 1
                if (r1 != r3) goto L_0x04d3
                r1 = 1
                goto L_0x04d4
            L_0x04d3:
                r1 = 0
            L_0x04d4:
                r2.networkSuspended(r0, r1)
                goto L_0x0a8f
            L_0x04d9:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r7 = r0.mStackIf
                int r8 = r1.getInt(r6)
                java.lang.String r0 = "action"
                int r9 = r1.getInt(r0)
                java.lang.String r0 = "codecType"
                int r10 = r1.getInt(r0)
                java.lang.String r0 = "cause"
                int r11 = r1.getInt(r0)
                java.lang.String r0 = "reasonText"
                java.lang.String r12 = r1.getString(r0)
                r7.updateCall(r8, r9, r10, r11, r12)
                goto L_0x0a8f
            L_0x0505:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r17 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r18 = r0.mHandle
                java.lang.String r19 = r1.getString(r15)
                int r20 = r1.getInt(r3)
                java.lang.String r21 = r1.getString(r14)
                java.lang.String r22 = r1.getString(r13)
                java.lang.String r0 = "participants"
                java.lang.String[] r23 = r1.getStringArray(r0)
                java.lang.String r24 = r1.getString(r4)
                java.lang.String r25 = r1.getString(r12)
                java.lang.String r26 = r1.getString(r11)
                java.lang.String r27 = r1.getString(r10)
                java.lang.String r28 = r1.getString(r9)
                boolean r29 = r1.getBoolean(r8)
                android.os.Parcelable r0 = r1.getParcelable(r5)
                r30 = r0
                android.os.Message r30 = (android.os.Message) r30
                r17.conference(r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30)
                goto L_0x0a8f
            L_0x0550:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r3 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r4 = r0.mHandle
                java.lang.String r0 = "confsession"
                int r5 = r1.getInt(r0)
                java.lang.String r0 = "updateCmd"
                int r6 = r1.getInt(r0)
                java.lang.String r0 = "participantId"
                int r7 = r1.getInt(r0)
                java.lang.String r0 = "participant"
                java.lang.String r8 = r1.getString(r0)
                r3.updateConfCall(r4, r5, r6, r7, r8)
                goto L_0x0a8f
            L_0x057e:
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                java.lang.Object r3 = r1.obj
                java.lang.String r3 = (java.lang.String) r3
                int r1 = r1.arg1
                r2.sendSmsResponse(r0, r3, r1)
                goto L_0x0a8f
            L_0x0595:
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                java.lang.Object r1 = r1.obj
                java.lang.String r1 = (java.lang.String) r1
                r2.sendSmsRpAckResponse(r0, r1)
                goto L_0x0a8f
            L_0x05aa:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                r0.sendSms(r1)
                goto L_0x0a8f
            L_0x05b3:
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r1 = r1.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                r1.stopCamera(r0)
                goto L_0x0a8f
            L_0x05c4:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r6 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r7 = r0.mHandle
                java.lang.String r0 = "pullingUri"
                java.lang.String r8 = r1.getString(r0)
                r2 = r17
                java.lang.String r9 = r1.getString(r2)
                java.lang.String r10 = r1.getString(r4)
                java.lang.String r0 = "targetDialog"
                android.os.Parcelable r0 = r1.getParcelable(r0)
                r11 = r0
                com.sec.ims.Dialog r11 = (com.sec.ims.Dialog) r11
                java.lang.String r0 = "p2p"
                java.util.ArrayList r12 = r1.getStringArrayList(r0)
                android.os.Parcelable r0 = r1.getParcelable(r5)
                r13 = r0
                android.os.Message r13 = (android.os.Message) r13
                r6.pullingCall(r7, r8, r9, r10, r11, r12, r13)
                goto L_0x0a8f
            L_0x0601:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                java.lang.String r4 = "cameraId"
                int r1 = r1.getInt(r4)
                r2.startCamera(r0, r3, r1)
                goto L_0x0a8f
            L_0x0620:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                android.os.Parcelable r1 = r1.getParcelable(r5)
                android.os.Message r1 = (android.os.Message) r1
                r2.resumeVideo(r0, r3, r1)
                goto L_0x0a8f
            L_0x063f:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                android.os.Parcelable r1 = r1.getParcelable(r5)
                android.os.Message r1 = (android.os.Message) r1
                r2.holdVideo(r0, r3, r1)
                goto L_0x0a8f
            L_0x065e:
                r8 = r21
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r9 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r10 = r0.mHandle
                int r11 = r1.getInt(r6)
                java.lang.String r0 = "headers"
                java.io.Serializable r0 = r1.getSerializable(r0)
                r12 = r0
                java.util.HashMap r12 = (java.util.HashMap) r12
                java.lang.String r13 = r1.getString(r8)
                r14 = 0
                r9.progressIncomingCall(r10, r11, r12, r13, r14)
                goto L_0x0a8f
            L_0x0687:
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UserAgent$UaEventListener r1 = r1.mListener
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                r1.onNotifyNullProfile(r0)
                goto L_0x0a8f
            L_0x0694:
                java.lang.Object r1 = r1.obj
                com.sec.internal.helper.AsyncResult r1 = (com.sec.internal.helper.AsyncResult) r1
                java.lang.Object r1 = r1.result
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse r1 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.ims.settings.ImsProfile r2 = r2.mImsProfile
                java.lang.String r2 = r2.getMnoName()
                com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.fromName(r2)
                boolean r2 = r2.isKor()
                if (r2 == 0) goto L_0x0a8f
                int r2 = r1.result()
                r3 = 1
                if (r2 != r3) goto L_0x06d3
                int r1 = r1.reason()
                r2 = 4
                if (r1 != r2) goto L_0x06d3
                r1 = 285278215(0x11010007, float:1.0176314E-28)
                java.lang.String r2 = "imsprofile is null. recover it"
                com.sec.internal.log.IMSLog.c(r1, r2, r3)
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UserAgent$UaEventListener r1 = r1.mListener
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                r1.onNotifyNullProfile(r0)
                goto L_0x0a8f
            L_0x06d3:
                r4 = r3
                goto L_0x0ab0
            L_0x06d6:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                java.lang.String r3 = "target"
                int r3 = r1.getInt(r3)
                java.lang.String r4 = "event"
                int r4 = r1.getInt(r4)
                java.lang.String r5 = "eventType"
                int r1 = r1.getInt(r5)
                r2.sendMediaEvent(r0, r3, r4, r1)
                goto L_0x0a8f
            L_0x06fe:
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r0 = r0.mStackIf
                java.lang.Object r1 = r1.obj
                com.sec.internal.ims.core.handler.secims.ResipStackRequest r1 = (com.sec.internal.ims.core.handler.secims.ResipStackRequest) r1
                r0.send(r1)
                goto L_0x0a8f
            L_0x070d:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                java.lang.String r4 = "text"
                java.lang.String r4 = r1.getString(r4)
                java.lang.String r5 = "len"
                int r1 = r1.getInt(r5)
                r2.sendText(r0, r3, r4, r1)
                goto L_0x0a8f
            L_0x0733:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                android.os.Parcelable r1 = r1.getParcelable(r5)
                android.os.Message r1 = (android.os.Message) r1
                r2.cancelTransferCall(r0, r3, r1)
                goto L_0x0a8f
            L_0x0752:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r7 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r8 = r0.mHandle
                int r9 = r1.getInt(r6)
                java.lang.String r0 = "code"
                int r10 = r1.getInt(r0)
                java.lang.String r0 = "mode"
                int r11 = r1.getInt(r0)
                java.lang.String r0 = "operation"
                int r12 = r1.getInt(r0)
                android.os.Parcelable r0 = r1.getParcelable(r5)
                r13 = r0
                android.os.Message r13 = (android.os.Message) r13
                r7.handleDtmf(r8, r9, r10, r11, r12, r13)
                goto L_0x0a8f
            L_0x0784:
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.arg1
                java.lang.Object r1 = r1.obj
                com.sec.ims.util.SipError r1 = (com.sec.ims.util.SipError) r1
                r4 = 0
                r2.rejectCall(r0, r3, r1, r4)
                goto L_0x0a8f
            L_0x079c:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r7 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r8 = r0.mHandle
                int r9 = r1.getInt(r6)
                java.lang.String r0 = "accepted"
                boolean r10 = r1.getBoolean(r0)
                java.lang.String r0 = "status"
                int r11 = r1.getInt(r0)
                java.lang.String r0 = "reason"
                java.lang.String r12 = r1.getString(r0)
                r13 = 0
                r7.acceptCallTransfer(r8, r9, r10, r11, r12, r13)
                goto L_0x0a8f
            L_0x07ca:
                r2 = r17
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r3 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r7 = r3.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r8 = r0.mHandle
                int r9 = r1.getInt(r6)
                java.lang.String r10 = r1.getString(r2)
                java.lang.String r0 = "replacingSessionId"
                int r11 = r1.getInt(r0)
                android.os.Parcelable r0 = r1.getParcelable(r5)
                r12 = r0
                android.os.Message r12 = (android.os.Message) r12
                r7.transferCall(r8, r9, r10, r11, r12)
                goto L_0x0a8f
            L_0x07f7:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r17 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r18 = r0.mHandle
                java.lang.String r0 = "session1"
                int r19 = r1.getInt(r0)
                java.lang.String r0 = "session2"
                int r20 = r1.getInt(r0)
                java.lang.String r21 = r1.getString(r15)
                int r22 = r1.getInt(r3)
                java.lang.String r23 = r1.getString(r14)
                java.lang.String r24 = r1.getString(r13)
                java.lang.String r25 = r1.getString(r4)
                java.lang.String r26 = r1.getString(r12)
                java.lang.String r27 = r1.getString(r11)
                java.lang.String r28 = r1.getString(r10)
                java.lang.String r29 = r1.getString(r9)
                boolean r30 = r1.getBoolean(r8)
                java.lang.String r0 = "extraHeaders"
                java.io.Serializable r0 = r1.getSerializable(r0)
                r31 = r0
                java.util.HashMap r31 = (java.util.HashMap) r31
                android.os.Parcelable r0 = r1.getParcelable(r5)
                r32 = r0
                android.os.Message r32 = (android.os.Message) r32
                r17.mergeCall(r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30, r31, r32)
                goto L_0x0a8f
            L_0x0854:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                android.os.Parcelable r1 = r1.getParcelable(r5)
                android.os.Message r1 = (android.os.Message) r1
                r2.resumeCall(r0, r3, r1)
                goto L_0x0a8f
            L_0x0873:
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.getInt(r6)
                android.os.Parcelable r1 = r1.getParcelable(r5)
                android.os.Message r1 = (android.os.Message) r1
                r2.holdCall(r0, r3, r1)
                goto L_0x0a8f
            L_0x0892:
                r8 = r21
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r9 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r10 = r0.mHandle
                int r11 = r1.getInt(r6)
                java.lang.String r0 = "callType"
                int r12 = r1.getInt(r0)
                java.lang.String r13 = r1.getString(r7)
                java.lang.String r14 = r1.getString(r8)
                r9.answerCall(r10, r11, r12, r13, r14)
                goto L_0x0a8f
            L_0x08bb:
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r0 = r0.mHandle
                int r3 = r1.arg1
                java.lang.Object r1 = r1.obj
                com.sec.internal.constants.ims.SipReason r1 = (com.sec.internal.constants.ims.SipReason) r1
                r6 = 0
                r2.endCall(r0, r3, r1, r6)
                goto L_0x0a8f
            L_0x08d3:
                r7 = r18
                r2 = r19
                r8 = r21
                r6 = 0
                java.lang.Object r1 = r1.obj
                android.os.Bundle r1 = (android.os.Bundle) r1
                java.lang.String r2 = r1.getString(r2)
                java.lang.String r3 = r1.getString(r7)
                if (r2 == 0) goto L_0x0913
                if (r3 == 0) goto L_0x0913
                com.google.flatbuffers.FlatBufferBuilder r6 = new com.google.flatbuffers.FlatBufferBuilder
                r7 = 0
                r6.<init>(r7)
                int r2 = r6.createString((java.lang.CharSequence) r2)
                int r3 = r6.createString((java.lang.CharSequence) r3)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.startAdditionalContents(r6)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.addMimeType(r6, r3)
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.addContents(r6, r2)
                int r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.endAdditionalContents(r6)
                r6.finish(r2)
                java.nio.ByteBuffer r2 = r6.dataBuffer()
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r3 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents.getRootAsAdditionalContents(r2)
                r26 = r3
                goto L_0x0915
            L_0x0913:
                r26 = r6
            L_0x0915:
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r17 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r18 = r0.mHandle
                java.lang.String r0 = "destUri"
                java.lang.String r19 = r1.getString(r0)
                java.lang.String r20 = r1.getString(r4)
                java.lang.String r0 = "type"
                int r21 = r1.getInt(r0)
                java.lang.String r0 = "dispName"
                java.lang.String r22 = r1.getString(r0)
                java.lang.String r0 = "dialedNumber"
                java.lang.String r23 = r1.getString(r0)
                r24 = 0
                r25 = -1
                java.lang.String r0 = "cli"
                java.lang.String r27 = r1.getString(r0)
                java.lang.String r0 = "pEmergencyInfo"
                java.lang.String r28 = r1.getString(r0)
                java.lang.String r0 = "additionalSipHeaders"
                java.io.Serializable r0 = r1.getSerializable(r0)
                r29 = r0
                java.util.HashMap r29 = (java.util.HashMap) r29
                java.lang.String r0 = "alertInfo"
                java.lang.String r30 = r1.getString(r0)
                java.lang.String r0 = "isLteEpsOnlyAttached"
                boolean r31 = r1.getBoolean(r0)
                java.lang.String r0 = "p2p"
                java.util.ArrayList r32 = r1.getStringArrayList(r0)
                java.lang.String r0 = "cmcBoundSessionId"
                int r33 = r1.getInt(r0)
                java.lang.String r0 = "composer_data"
                android.os.Bundle r34 = r1.getBundle(r0)
                java.lang.String r0 = "replaceCallId"
                java.lang.String r35 = r1.getString(r0)
                java.lang.String r0 = "cmcEdCallSlot"
                int r36 = r1.getInt(r0)
                java.lang.String r37 = r1.getString(r8)
                android.os.Parcelable r0 = r1.getParcelable(r5)
                r38 = r0
                android.os.Message r38 = (android.os.Message) r38
                r17.makeCall(r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30, r31, r32, r33, r34, r35, r36, r37, r38)
                goto L_0x0a8f
            L_0x0994:
                r7 = 0
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r2.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r3 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r3 = r3.mHandle
                int r1 = r1.arg1
                r4 = 1
                if (r1 != r4) goto L_0x09a8
                r5 = 1
                goto L_0x09a9
            L_0x09a8:
                r5 = r7
            L_0x09a9:
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                r4 = 11
                android.os.Message r1 = r1.obtainMessage(r4)
                r2.deregister(r3, r5, r1)
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UserAgent$UserAgentState r1 = com.sec.internal.ims.core.handler.secims.UserAgent.UserAgentState.DEREGISTERING
                r0.setDestState(r1)
                goto L_0x0a8f
            L_0x09bd:
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r1 = r1.mPhoneId
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = r39.getName()
                r2.append(r3)
                java.lang.String r3 = " reRegistered."
                r2.append(r3)
                java.lang.String r2 = r2.toString()
                java.lang.String r3 = "UserAgent"
                com.sec.internal.log.IMSLog.i(r3, r1, r2)
                r39.onRegistered()
                goto L_0x0a8f
            L_0x09e2:
                int r2 = r1.arg1
                r3 = -1
                if (r2 == r3) goto L_0x0a8f
                java.lang.Object r1 = r1.obj
                java.lang.String r1 = (java.lang.String) r1
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                r0.updateRouteTable(r2, r1)
                goto L_0x0a8f
            L_0x09f2:
                java.lang.Object r1 = r1.obj
                com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged r1 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged) r1
                r0.onRegInfoNotify(r1)
                goto L_0x0a8f
            L_0x09fb:
                java.util.ArrayList r7 = new java.util.ArrayList
                r7.<init>()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                java.util.Set r1 = r1.getServiceList()
                r7.addAll(r1)
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r2 = r1.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r3 = r1.mHandle
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                java.lang.String r4 = r1.getPcscfIp()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                int r5 = r1.getPcscfPort()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                int r6 = r1.getRegExpires()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                java.util.List r8 = r1.getLinkedImpuList()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                com.sec.ims.options.Capabilities r9 = r1.getOwnCapabilities()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                java.util.List r10 = r1.mThirdPartyFeatureTags
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                java.lang.String r11 = r1.getAccessToken()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                java.lang.String r12 = r1.getAuthServerUrl()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                boolean r13 = r1.getSingleRegiEnabled()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                java.lang.String r14 = r1.getImMsgTech()
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UaProfile r1 = r1.mUaProfile
                boolean r15 = r1.getIsAddMmtelCallComposerTag()
                r16 = 0
                r2.register(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16)
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UserAgent$UserAgentState r1 = com.sec.internal.ims.core.handler.secims.UserAgent.UserAgentState.REREGISTERING
                r0.setDestState(r1)
            L_0x0a8f:
                r4 = 1
                goto L_0x0ab0
            L_0x0a91:
                com.sec.internal.ims.core.handler.secims.UserAgent r1 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.IStackIF r1 = r1.mStackIf
                com.sec.internal.ims.core.handler.secims.UserAgent r2 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                int r2 = r2.mHandle
                com.sec.internal.ims.core.handler.secims.UserAgent r3 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                r4 = 11
                android.os.Message r3 = r3.obtainMessage(r4)
                r4 = 1
                r1.deregister(r2, r4, r3)
                com.sec.internal.ims.core.handler.secims.UserAgent r0 = com.sec.internal.ims.core.handler.secims.UserAgent.this
                com.sec.internal.ims.core.handler.secims.UserAgent$UserAgentState r1 = com.sec.internal.ims.core.handler.secims.UserAgent.UserAgentState.DEREGISTERING
                r0.setDestState(r1)
            L_0x0ab0:
                r5 = r4
            L_0x0ab1:
                return r5
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.UserAgent.RegisteredState.processMessage(android.os.Message):boolean");
        }

        private void onRegistered() {
            UserAgent.this.updateEpdgStatus();
            UserAgent userAgent = UserAgent.this;
            userAgent.mRegistration = userAgent.buildImsRegistration();
            UserAgent.this.mStackIf.setPreferredImpu(UserAgent.this.mHandle, UserAgent.this.mRegistration.getPreferredImpu().getUri().toString());
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onRegistered(UserAgent.this);
            }
        }

        private void onRegInfoNotify(RegInfoChanged regInfoChanged) {
            IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "onRegInfoNotify:");
            if (UserAgent.this.mRegistration == null) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "onRegInfoNotify: unexpected RegInfoNotify. mHandle " + UserAgent.this.mHandle);
                return;
            }
            int contactsLength = regInfoChanged.contactsLength();
            Contact[] contactArr = new Contact[contactsLength];
            for (int i = 0; i < contactsLength; i++) {
                contactArr[i] = regInfoChanged.contacts(i);
            }
            for (int i2 = 0; i2 < contactsLength; i2++) {
                Contact contact = contactArr[i2];
                NameAddr nameAddr = new NameAddr(contact.displayName(), ImsUri.parse(contact.uri()));
                Log.i(UserAgent.LOG_TAG, "onRegInfoNotify: " + nameAddr + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + contact.state());
                if (contact.state() == 1) {
                    UserAgent.this.addImpu(nameAddr);
                    UserAgent.this.addDevice(nameAddr);
                } else if (contact.state() == 2) {
                    UserAgent.this.removeImpu(nameAddr.getUri());
                    UserAgent.this.removeDevice(nameAddr.getUri());
                }
            }
            UserAgent userAgent = UserAgent.this;
            userAgent.mRegistration = userAgent.buildImsRegistration();
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onRegistered(UserAgent.this);
            }
        }

        private void sendSms(Bundle bundle) {
            String string = bundle.getString("sca");
            String string2 = bundle.getString("localuri");
            String bytesToHex = UserAgent.bytesToHex(bundle.getByteArray("pdu"));
            String string3 = bundle.getString("contentType");
            if (string3 == null) {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "sendSms: null contentType. ");
                return;
            }
            String[] split = string3.split("/");
            if (split.length < 2) {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "sendSms: invalid contentType. " + string3);
                return;
            }
            UserAgent.this.mStackIf.sendSms(UserAgent.this.mHandle, string, string2, bytesToHex, split[0], split[1], bundle.getString("callId"), bundle.getBoolean("isEmergency"), (Message) bundle.getParcelable("result"));
        }
    }

    private class ReRegisteringState extends State {
        private ReRegisteringState() {
        }

        public void enter() {
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, r0, getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 8) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_REGISTERD");
                UserAgent.this.setDestState(UserAgentState.REGISTERED);
                return true;
            } else if (i == 10) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_REQUEST_DEREGISTER");
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " Defer EVENT_REQUEST_DEREGISTER");
                UserAgent.this.deferMessage(message);
                return true;
            } else if (i != 13) {
                if (i != 31) {
                    if (i == 2000) {
                        GeneralResponse generalResponse = (GeneralResponse) ((AsyncResult) message.obj).result;
                        if (!Mno.fromName(UserAgent.this.mImsProfile.getMnoName()).isKor() || generalResponse.result() != 1 || generalResponse.reason() != 4) {
                            return true;
                        }
                        IMSLog.c(LogClass.REGI_DO_RECOVERY_ACTION, "imsprofile is null. recover it", true);
                        UserAgent.this.mListener.onNotifyNullProfile(UserAgent.this);
                        return true;
                    } else if (!(i == 41 || i == 42)) {
                        return false;
                    }
                }
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " Defer event " + message.what);
                UserAgent.this.deferMessage(message);
                return true;
            } else {
                Log.i(UserAgent.LOG_TAG, getName() + " EVENT_DEREGISTERED_TIMEOUT");
                if (UserAgent.this.mListener != null) {
                    UserAgent.this.mListener.onDeregistered(UserAgent.this, SipErrorBase.OK, 0, true, false);
                }
                UserAgent.this.setDestState(UserAgentState.DEREGISTERING);
                return true;
            }
        }
    }

    private class DeregisteringState extends State {
        private DeregisteringState() {
        }

        public void enter() {
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, r0, getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 4) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DELETE_UA");
                UserAgent.this.mStackIf.deleteUA(UserAgent.this.mHandle, UserAgent.this.obtainMessage(5));
                UserAgent.this.setDestState(UserAgentState.TERMINATING);
            } else if (i != 800) {
                switch (i) {
                    case 10:
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_REQUEST_DEREGISTER");
                        Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " UA is already being deregisted.");
                        break;
                    case 11:
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DEREGISTER_COMPELETE");
                        break;
                    case 12:
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DEREGISTERED");
                        if (UserAgent.this.mUaProfile != null && (UserAgent.this.mUaProfile.getPdn().equals(DeviceConfigManager.IMS) || UserAgent.this.getImsProfile().getCmcType() != 0)) {
                            Mno mno = UserAgent.this.mUaProfile.getMno();
                            int i2 = (mno == Mno.MAGTICOM_GE || mno == Mno.MEGAFON_RUSSIA || mno == Mno.VODAFONE || mno == Mno.CTC || mno == Mno.CTCMO) ? 1000 : 600;
                            if (mno == Mno.KDDI) {
                                i2 = 200;
                            }
                            UserAgent userAgent = UserAgent.this;
                            userAgent.sendMessageDelayed(userAgent.obtainMessage(800), (long) i2);
                            break;
                        } else {
                            if (UserAgent.this.mListener != null) {
                                UserAgent.this.mListener.onDeregistered(UserAgent.this, SipErrorBase.OK, 0, true, false);
                            }
                            UserAgent.this.sendMessage(4);
                            break;
                        }
                        break;
                    case 13:
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DEREGISTERED_TIMEOUT");
                        if (UserAgent.this.mListener != null) {
                            UserAgent.this.mListener.onDeregistered(UserAgent.this, SipErrorBase.OK, 0, true, false);
                            break;
                        }
                        break;
                    default:
                        return false;
                }
            } else {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DELAYED_DEREGISTERED");
                if (UserAgent.this.mListener != null) {
                    UserAgent.this.mListener.onDeregistered(UserAgent.this, SipErrorBase.OK, 0, true, false);
                }
                UserAgent.this.sendMessage(4);
            }
            return true;
        }
    }

    private class TerminatingState extends State {
        private TerminatingState() {
        }

        public void enter() {
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, r0, getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 4) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DELETE_UA");
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " UA is already being deleted.");
            } else if (i == 5) {
                if (UserAgent.this.mHandle != -1) {
                    UserAgent.this.mStackIf.unRegisterUaListener(UserAgent.this.mHandle);
                }
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_UA_DELETED");
                UserAgent.this.setDestState(UserAgentState.INITIAL);
            } else if (i == 10) {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_REQUEST_DEREGISTER");
                IMSLog.e(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " UA is already being deregisted.");
            } else if (i != 11) {
                return false;
            } else {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_DEREGISTERED");
            }
            return true;
        }
    }

    private class EmergencyState extends State {
        private EmergencyState() {
        }

        public void enter() {
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, r0, getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            Message message2 = message;
            int i = message2.what;
            if (i == 6) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "register is not required for emergency call.");
            } else if (i == 10) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "deregister is not required for emergency call. delete UA.");
                UserAgent.this.sendMessage(4);
            } else if (i == 23) {
                Bundle bundle = (Bundle) message2.obj;
                UserAgent.this.mStackIf.handleDtmf(UserAgent.this.mHandle, bundle.getInt("sessionId"), bundle.getInt("code"), bundle.getInt("mode"), bundle.getInt("operation"), (Message) bundle.getParcelable("result"));
            } else if (i == 37) {
                Bundle bundle2 = (Bundle) message2.obj;
                UserAgent.this.mStackIf.updateCall(bundle2.getInt("sessionId"), bundle2.getInt("action"), bundle2.getInt("codecType"), bundle2.getInt("cause"), bundle2.getString("reasonText"));
            } else if (i == 51) {
                Bundle bundle3 = (Bundle) message2.obj;
                UserAgent.this.mStackIf.sendText(UserAgent.this.mHandle, bundle3.getInt("sessionId"), bundle3.getString("text"), bundle3.getInt("len"));
            } else if (i == 102) {
                int i2 = message2.arg1;
                if (i2 != -1) {
                    UserAgent.this.updateRouteTable(i2, (String) message2.obj);
                }
            } else if (i == 1001) {
                Bundle bundle4 = (Bundle) message2.obj;
                UserAgent.this.mStackIf.sendMediaEvent(UserAgent.this.mHandle, bundle4.getInt(SoftphoneNamespaces.SoftphoneCallHandling.TARGET), bundle4.getInt("event"), bundle4.getInt("eventType"));
            } else if (i == 14) {
                Bundle bundle5 = (Bundle) message2.obj;
                UserAgent.this.mStackIf.makeCall(UserAgent.this.mHandle, bundle5.getString("destUri"), bundle5.getString("origUri"), bundle5.getInt("type"), bundle5.getString("dispName"), bundle5.getString("dialedNumber"), UserAgent.this.mUaProfile.getPcscfIp(), UserAgent.this.mUaProfile.getPcscfPort(), (AdditionalContents) null, (String) null, bundle5.getString("PEmergencyInfo"), (HashMap<String, String>) null, bundle5.getString("alertInfo"), bundle5.getBoolean("isLteEpsOnlyAttached"), bundle5.getStringArrayList("p2p"), bundle5.getInt("cmcBoundSessionId"), bundle5.getBundle(CallConstants.ComposerData.TAG), bundle5.getString("replaceCallId"), bundle5.getInt("cmcEdCallSlot"), bundle5.getString("idcExtra"), (Message) bundle5.getParcelable("result"));
            } else if (i == 15) {
                UserAgent.this.mStackIf.endCall(UserAgent.this.mHandle, message2.arg1, (SipReason) message2.obj, (Message) null);
            } else if (i == 109) {
                Bundle bundle6 = (Bundle) message2.obj;
                UserAgent.this.mStackIf.startLocalRingBackTone(UserAgent.this.mHandle, bundle6.getInt("streamType"), bundle6.getInt("volume"), bundle6.getInt("toneType"), (Message) bundle6.getParcelable("result"));
            } else if (i != 110) {
                switch (i) {
                    case 62:
                        Bundle bundle7 = (Bundle) message2.obj;
                        UserAgent.this.mStackIf.replyWithIdc(bundle7.getInt("sessionId"), bundle7.getInt("reqType"), bundle7.getInt("curType"), bundle7.getInt("repType"), bundle7.getString("idcExtra"));
                        break;
                    case 63:
                        Bundle bundle8 = (Bundle) message2.obj;
                        UserAgent.this.mStackIf.updateWithIdc(bundle8.getInt("sessionId"), bundle8.getInt("action"), bundle8.getString("idcExtra"));
                        break;
                    case 64:
                        Bundle bundle9 = (Bundle) message2.obj;
                        UserAgent.this.mStackIf.sendNegotiatedLocalSdp(bundle9.getInt("sessionId"), bundle9.getString(IdcExtra.Key.SDP));
                        break;
                    default:
                        switch (i) {
                            case 104:
                                Bundle bundle10 = (Bundle) message2.obj;
                                UserAgent.this.mStackIf.modifyCallType(bundle10.getInt("sessionId"), bundle10.getInt("oldType"), bundle10.getInt("newType"));
                                break;
                            case 105:
                                Bundle bundle11 = (Bundle) message2.obj;
                                UserAgent.this.mStackIf.replyModifyCallType(bundle11.getInt("sessionId"), bundle11.getInt("reqType"), bundle11.getInt("curType"), bundle11.getInt("repType"), bundle11.getString("cmcCallTime"));
                                break;
                            case 106:
                                Bundle bundle12 = (Bundle) message2.obj;
                                UserAgent.this.mStackIf.rejectModifyCallType(bundle12.getInt("sessionId"), bundle12.getInt("reason"));
                                break;
                            default:
                                return false;
                        }
                }
            } else {
                UserAgent.this.mStackIf.stopLocalRingBackTone(UserAgent.this.mHandle);
            }
            return true;
        }
    }

    private class ProhibitedState extends State {
        private ProhibitedState() {
        }

        public void enter() {
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, r0, getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.e(UserAgent.LOG_TAG, r0, "Unexpected event " + message.what + ". current state is " + UserAgent.this.getCurrentState().getName());
            return false;
        }
    }

    public class EventListener extends StackEventListener {
        public EventListener() {
        }

        public void onISIMAuthRequested(int i, String str, int i2) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onISIMAuthRequested: handle " + i + " nonce " + str + " tid " + i2);
            if (i != UserAgent.this.mHandle) {
                IMSLog.e(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "onISIMAuthRequested: handle mismatch. mHandle " + UserAgent.this.mHandle + " handle " + i + " tid " + i2);
                return;
            }
            Message obtainMessage = UserAgent.this.obtainMessage(9, i2);
            if (UserAgent.this.mSimManager.hasVsim()) {
                UserAgent.this.mSimManager.requestSoftphoneAuthentication(str, UserAgent.this.mImsProfile.getImpi(), obtainMessage, UserAgent.this.mImsProfile.getId());
            } else {
                UserAgent.this.mSimManager.requestIsimAuthentication(str, obtainMessage);
            }
        }

        public void onRegistered(int i, List<String> list, List<String> list2, SipError sipError, long j, int i2, String str) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onRegistered: handle=" + i + " error=" + sipError + " ecmpMode=" + i2 + " serviceList=" + list);
            if (UserAgent.this.hasMessages(UserAgent.EVENT_RECOVER_REGISESSION)) {
                UserAgent.this.removeMessages(UserAgent.EVENT_RECOVER_REGISESSION);
            }
            if (i == UserAgent.this.mHandle) {
                int i3 = 0;
                if (Mno.fromName(UserAgent.this.mImsProfile.getMnoName()) != Mno.TMOUS || !SipErrorBase.OK.equals(sipError) || !list2.isEmpty()) {
                    UserAgent.this.mRegisterSipResponse = str;
                    synchronized (UserAgent.this.mImpuList) {
                        UserAgent.this.mImpuList.clear();
                        for (String parse : list2) {
                            ImsUri parse2 = ImsUri.parse(parse);
                            if (parse2 != null) {
                                UserAgent.this.mImpuList.add(new NameAddr(parse2));
                            }
                        }
                    }
                    UserAgent.this.mEcmpMode = i2;
                    UserAgent.this.mError = sipError;
                    try {
                        i3 = Integer.parseInt(ImsSharedPrefHelper.getString(UserAgent.this.mPhoneId, UserAgent.this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, "fake_reg_response", ""));
                    } catch (NumberFormatException unused) {
                    }
                    if (i3 != 0) {
                        if (Mno.fromName(UserAgent.this.mImsProfile.getMnoName()).isKor()) {
                            Log.i(UserAgent.LOG_TAG, "!!!sip response is replaced to fake : " + i3);
                            if (i3 == 403) {
                                UserAgent.this.mError = SipErrorBase.FORBIDDEN;
                            } else {
                                UserAgent.this.mError = new SipError(i3, "");
                            }
                        } else {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("fake_reg_response", "");
                            ImsSharedPrefHelper.put(UserAgent.this.mPhoneId, UserAgent.this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, contentValues);
                        }
                    }
                    if (SipErrorBase.OK.equals(UserAgent.this.mError) || SipErrorBase.OK_SMC.equals(UserAgent.this.mError)) {
                        UserAgent.this.mNotifyServiceList.addAll(list);
                        if (Mno.fromName(UserAgent.this.mImsProfile.getMnoName()).isKor() && j > 0) {
                            UserAgent.this.sendMessageDelayed((int) UserAgent.EVENT_RECOVER_REGISESSION, j + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
                        }
                        UserAgent.this.sendMessage(8);
                        return;
                    }
                    if (UserAgent.this.mListener != null) {
                        UaEventListener r5 = UserAgent.this.mListener;
                        UserAgent userAgent = UserAgent.this;
                        r5.onRegistrationError(userAgent, userAgent.mError, j);
                    }
                    if (UserAgent.this.mImsProfile.hasEmergencySupport()) {
                        UserAgent.this.sendMessage(900);
                        return;
                    }
                    return;
                }
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "onRegistered: Empty IRS. deregister.");
                UserAgent.this.deregisterInternal(false);
                if (UserAgent.this.mListener != null) {
                    UserAgent.this.mListener.onRegistrationError(UserAgent.this, SipErrorBase.MISSING_P_ASSOCIATED_URI, UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                }
            }
        }

        public void onDeregistered(int i, SipError sipError, long j, boolean z) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onDeregistered: handle " + i + " error " + sipError + " retryAfter " + j + " pcscfGoneDeregi" + z);
            if (UserAgent.this.hasMessages(UserAgent.EVENT_RECOVER_REGISESSION)) {
                UserAgent.this.removeMessages(UserAgent.EVENT_RECOVER_REGISESSION);
            }
            if (i == UserAgent.this.mHandle) {
                UserAgent.this.mError = sipError;
                UserAgent.this.mRetryAfterMs = j;
                UserAgent.this.mPcscfGoneDeregi = z;
                UserAgent.this.sendMessage(12);
            }
        }

        public void onSubscribed(int i, SipError sipError) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onSubscribed: handle " + i + " error " + sipError);
            if (i == UserAgent.this.mHandle && UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onSubscribeError(UserAgent.this, sipError);
            }
        }

        public void onRegInfoNotification(int i, RegInfoChanged regInfoChanged) {
            if (i == UserAgent.this.mHandle) {
                UserAgent.this.sendMessage(101, (Object) regInfoChanged);
            }
        }

        public void onUpdateRouteTableRequested(int i, int i2, String str) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onUpdateRouteTableRequested:");
            if (i != UserAgent.this.mHandle) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "onUpdateRouteTableRequested: handle mismatch. mHandle " + UserAgent.this.mHandle + " handle " + i);
                return;
            }
            UserAgent userAgent = UserAgent.this;
            userAgent.sendMessage(userAgent.obtainMessage(102, i2, 0, str));
        }

        public void onRegImpuNotification(int i, String str) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onRegImpuNotification: handle(" + i + ")");
            if (i == UserAgent.this.mHandle) {
                int simSlotIndex = UserAgent.this.mSimManager.getSimSlotIndex();
                Intent intent = new Intent("com.sec.imsservice.REGISTERED_IMPU");
                intent.putExtra("phoneid", simSlotIndex);
                intent.putExtra("impu", str);
                UserAgent.this.mContext.sendBroadcast(intent, UserAgent.PERMISSION);
            }
        }

        public void onUpdatePani() {
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onUpdatePani(UserAgent.this);
            }
        }

        public void onRefreshRegNotification(int i) {
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onRefreshRegNotification(i);
            }
        }

        public void onContactActivated(int i) {
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, r0, "onContactActivated: handle(" + i + ")");
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onContactActivated(UserAgent.this, i);
            }
        }

        public void onRegEventContactUriNotification(int i, List<String> list, int i2, String str, String str2) {
            int r0 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, r0, "onRegEventContactUri: handle(" + i + ")");
            ArrayList arrayList = new ArrayList();
            for (String parse : list) {
                arrayList.add(ImsUri.parse(parse));
            }
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onRegEventContactUriNotification(i, arrayList, i2, str, str2);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateRouteTable(int i, String str) {
        Log.i("UserAgent[" + this.mPhoneId + "]", "UpdateRouteTable: op " + i + " address " + str);
        if (i == 0) {
            this.mPdnController.requestRouteToHostAddress(this.mPdn, str);
        } else if (i == 1) {
            this.mPdnController.removeRouteToHostAddress(this.mPdn, str);
        }
    }

    public static String bytesToHex(byte[] bArr) {
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

    private String extractDomain(UaProfile uaProfile, String str) {
        String domain = uaProfile.getDomain();
        if (uaProfile.getMno() == Mno.CMCC || uaProfile.getMno() == Mno.CU) {
            Log.i("UserAgent[" + this.mPhoneId + "]", "extractDomain:  don't use phone-context as domain.");
            return domain;
        } else if (TextUtils.isEmpty(str) || !uaProfile.getImpu().contains(str)) {
            return domain;
        } else {
            for (NameAddr next : this.mImpuList) {
                if (!TextUtils.isEmpty(next.getUri().getPhoneContext())) {
                    Log.i("UserAgent[" + this.mPhoneId + "]", "extractDomain: For IMSI-based registration, use phone-context as domain.");
                    return next.getUri().getPhoneContext();
                }
            }
            return domain;
        }
    }

    /* access modifiers changed from: private */
    public void addImpu(NameAddr nameAddr) {
        synchronized (this.mImpuList) {
            boolean z = true;
            for (NameAddr next : this.mImpuList) {
                if (nameAddr.getUri().equals(next.getUri()) && TextUtils.equals(nameAddr.getUri().getParam("gr"), next.getUri().getParam("gr"))) {
                    next.setDisplayName(nameAddr.getDisplayName());
                    z = false;
                }
            }
            if (z) {
                this.mImpuList.add(nameAddr);
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeImpu(ImsUri imsUri) {
        Iterator<NameAddr> it = this.mImpuList.iterator();
        while (it.hasNext()) {
            NameAddr next = it.next();
            if (next.getUri().equals(imsUri) && TextUtils.equals(next.getUri().getParam("gr"), imsUri.getParam("gr"))) {
                it.remove();
            }
        }
    }

    /* access modifiers changed from: private */
    public void addDevice(NameAddr nameAddr) {
        boolean z = true;
        for (NameAddr next : this.mDeviceList) {
            if (nameAddr.getUri().equals(next.getUri()) && TextUtils.equals(nameAddr.getUri().getParam("gr"), next.getUri().getParam("gr"))) {
                next.setDisplayName(nameAddr.getDisplayName());
                z = false;
            }
        }
        if (z) {
            this.mDeviceList.add(nameAddr);
        }
    }

    /* access modifiers changed from: private */
    public void removeDevice(ImsUri imsUri) {
        Iterator<NameAddr> it = this.mDeviceList.iterator();
        while (it.hasNext()) {
            NameAddr next = it.next();
            if (next.getUri().equals(imsUri) && TextUtils.equals(next.getUri().getParam("gr"), imsUri.getParam("gr"))) {
                it.remove();
            }
        }
    }

    private NameAddr getFirstImpuByUriType(ImsUri.UriType uriType) {
        return (NameAddr) this.mImpuList.stream().filter(new UserAgent$$ExternalSyntheticLambda0(uriType)).findFirst().orElse((Object) null);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getFirstImpuByUriType$0(ImsUri.UriType uriType, NameAddr nameAddr) {
        return nameAddr.getUri().getUriType() == uriType;
    }

    private NameAddr getPreferredImpu(Set<String> set) {
        Mno mno = this.mUaProfile.getMno();
        NameAddr nameAddr = null;
        if (mno == Mno.VZW) {
            String impi = this.mUaProfile.getImpi();
            ImsUri parse = ImsUri.parse(this.mUaProfile.getImpu());
            int indexOf = impi.indexOf(64);
            if (indexOf > 0 && parse != null) {
                String substring = impi.substring(0, indexOf);
                String user = parse.getUser();
                if (!TextUtils.isEmpty(user) && !user.contains(substring)) {
                    nameAddr = new NameAddr("", parse);
                }
            }
        } else if (mno == Mno.ATT || this.mImsProfile.isSipUriOnly()) {
            nameAddr = getFirstImpuByUriType(ImsUri.UriType.SIP_URI);
        } else if (mno.isKor() || mno == Mno.RJIL) {
            nameAddr = getFirstImpuByUriType(ImsUri.UriType.TEL_URI);
        }
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "getPreferredImpu: " + IMSLog.checker(nameAddr));
        if (nameAddr != null) {
            return nameAddr;
        }
        if (this.mImpuList.isEmpty()) {
            return new NameAddr("", this.mUaProfile.getImpu());
        }
        if (Arrays.asList(ImsProfile.getRcsServiceList()).containsAll(set)) {
            nameAddr = getFirstImpuByUriType(ImsUri.UriType.TEL_URI);
        }
        return (nameAddr != null || this.mImpuList.isEmpty()) ? nameAddr : this.mImpuList.get(0);
    }

    /* access modifiers changed from: private */
    public ImsRegistration buildImsRegistration() {
        int subscriptionId = this.mSimManager.getSubscriptionId();
        String extractDomain = extractDomain(this.mUaProfile, this.mTelephonyManager.getSubscriberId(subscriptionId));
        HashSet hashSet = new HashSet();
        if (this.mNotifyServiceList.size() != 0) {
            hashSet.addAll(this.mNotifyServiceList);
            this.mNotifyServiceList.clear();
        } else {
            hashSet.addAll(this.mUaProfile.getServiceList());
            hashSet.remove("datachannel");
        }
        return ImsRegistration.getBuilder().setHandle(this.mHandle).setImsProfile(new ImsProfile(this.mImsProfile)).setServices(hashSet).setPrivateUserId(this.mUaProfile.getImpi()).setPublicUserId(this.mImpuList).setRegisteredPublicUserId(ImsUri.parse(this.mUaProfile.getImpu())).setPreferredPublicUserId(getPreferredImpu(hashSet)).setDomain(extractDomain).setPcscf(this.mUaProfile.getPcscfIp()).setEpdgStatus(this.mEpdgStatus).setEpdgOverCellularData(this.mEpdgOverCellularData).setPdnType(this.mPdn).setUuid(this.mUaProfile.getUuid()).setInstanceId(this.mUaProfile.getInstanceId()).setEcmpStatus(this.mEcmpMode).setDeviceList(this.mDeviceList).setRegisterSipResponse(this.mRegisterSipResponse).setNetwork(this.mNetwork).setPAssociatedUri2nd((!OmcCode.isKOROmcCode() || this.mSimManager.getSimMno() != Mno.LGU || !hashSet.contains("mmtel") || getImsProfile().getCmcType() != 0) ? "" : getPAssociatedUri2nd(hashSet, this.mImpuList)).setSubscriptionId(subscriptionId).setPhoneId(this.mSimManager.getSimSlotIndex()).build();
    }

    private String getPAssociatedUri2nd(Set<String> set, List<NameAddr> list) {
        String extractPAssociatedUri2nd = extractPAssociatedUri2nd(list);
        Log.i("UserAgent[" + this.mPhoneId + "]", "getPAssociatedUri2nd() : " + IMSLog.checker(extractPAssociatedUri2nd));
        return extractPAssociatedUri2nd;
    }

    private String extractPAssociatedUri2nd(List<NameAddr> list) {
        String line1Number = this.mSimManager.getLine1Number();
        if (line1Number != null) {
            line1Number = line1Number.replace("+82", "0");
        }
        Log.i("UserAgent[" + this.mPhoneId + "]", "extractPAssociatedUri2nd");
        String str = null;
        for (NameAddr uri : list) {
            ImsUri uri2 = uri.getUri();
            if (uri2 != null) {
                Log.i("UserAgent[" + this.mPhoneId + "]", "extractPAssociatedUri2nd  uri");
                if (uri2.getUriType() == ImsUri.UriType.SIP_URI && uri2.toString() != null) {
                    Log.i("UserAgent[" + this.mPhoneId + "]", "extractPAssociatedUri2nd: uri=" + IMSLog.checker(uri2.toString()));
                    String onlyNumberFromURI = getOnlyNumberFromURI(uri2.toString());
                    if (!(line1Number == null || onlyNumberFromURI == null || onlyNumberFromURI.equals(line1Number))) {
                        str = onlyNumberFromURI;
                    }
                }
            }
        }
        return str;
    }

    private String getOnlyNumberFromURI(String str) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("tel:", "tel:");
        linkedHashMap.put("sip:", "sip:");
        linkedHashMap.put("*31#", "[*]31#");
        linkedHashMap.put("#31#", "#31#");
        Log.i("UserAgent[" + this.mPhoneId + "]", "getOnlyNumberFromURI");
        String str2 = str;
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            if (str.contains((CharSequence) entry.getKey())) {
                str2 = str.split((String) entry.getValue())[1];
            }
        }
        String[] strArr = {"@", ";"};
        for (int i = 0; i < 2; i++) {
            String str3 = strArr[i];
            if (str2.contains(str3)) {
                str2 = str2.split(str3)[0];
            }
        }
        return str2;
    }

    public SipError getErrorCode() {
        return this.mError;
    }

    public UaProfile getUaProfile() {
        return this.mUaProfile;
    }

    public boolean isRegistering() {
        return getCurrentState().equals(this.mRegisteringState) || getCurrentState().equals(this.mReRegisteringState);
    }

    public void updateVceConfig(boolean z) {
        sendMessage(40, (Object) Boolean.valueOf(z));
    }

    public void updateGeolocation(LocationInfo locationInfo) {
        sendMessage(44, (Object) locationInfo);
    }

    public ImsProfile getImsProfile() {
        return this.mImsProfile;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public void updateTimeInPlani(long j) {
        sendMessage(52, (Object) Long.valueOf(j));
    }

    public void updateRat(int i) {
        sendMessage(50, i);
    }

    public void setVowifi5gsaMode(int i) {
        sendMessage(60, i);
    }

    public void deregister(boolean z, boolean z2) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "deregister: local=" + z + ", needDelay=" + z2);
        if (z2) {
            sendMessageDelayed(10, z ? 1 : 0, -1, 500);
        } else {
            sendMessage(10, z ? 1 : 0);
        }
    }

    public int getHandle() {
        return this.mHandle;
    }

    public void updatePani(String str, String str2) {
        ArrayList arrayList = new ArrayList();
        IMSLog.i(LOG_TAG, this.mPhoneId, "updatePani");
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "updatePani: pani=" + str + ", updatePani: lastPani=" + str2);
        if (!TextUtils.isEmpty(str)) {
            arrayList.add(str);
            if (!TextUtils.isEmpty(str2)) {
                arrayList.add(str2);
            }
            sendMessage(34, (Object) arrayList);
        }
    }

    public void terminate() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "terminate:");
        sendMessage(4);
    }

    public void notifyE911RegistrationFailed() {
        sendMessage(900);
    }

    public ImsRegistration getImsRegistration() {
        return this.mRegistration;
    }

    public boolean isDeregistring() {
        return getCurrentState().equals(this.mDeregisteringState);
    }

    public Network getNetwork() {
        return this.mNetwork;
    }
}
