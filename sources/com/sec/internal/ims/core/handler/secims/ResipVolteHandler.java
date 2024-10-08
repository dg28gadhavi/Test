package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import android.widget.Toast;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallParams;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.QuantumSecurityStatusEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.handler.VolteHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallSendCmcInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DTMFDataEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DedicatedBearerEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.IncomingCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ModifyCallData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.QuantumSecurityStatusEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReferReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReferStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RrcConnectionEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RtpLossRateNoti;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SipMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.TextDataEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CallResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.volte2.data.CallSetupData;
import com.sec.internal.ims.servicemodules.volte2.data.ConfCallSetupData;
import com.sec.internal.ims.servicemodules.volte2.data.DtmfInfo;
import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;
import com.sec.internal.ims.servicemodules.volte2.data.SIPDataEvent;
import com.sec.internal.ims.servicemodules.volte2.data.TextInfo;
import com.sec.internal.ims.servicemodules.volte2.util.DialogXmlParser;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.log.CmcPingTestLogger;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlSerializer;

public class ResipVolteHandler extends VolteHandler {
    private static final int ADD_CONF_PARTICIPANT = 0;
    private static final String ALTERNATIVE_SERVICE = "application/3gpp-ims+xml";
    private static final String CMC_INFO_MIME_TYPE = "application/cmc-info+xml";
    private static final String DIALOG_EVENT_MIME_TYPE = "application/dialog-info+xml";
    private static final int EVENT_CALL_STATE_CHANGE = 100;
    private static final int EVENT_CDPN_INFO = 107;
    private static final int EVENT_CMC_INFO = 115;
    private static final int EVENT_CONFERENCE_UPDATE = 102;
    private static final int EVENT_CURRENT_LOCATION_DISCOVERY_DURING_EMERGENCY_CALL = 117;
    private static final int EVENT_DEDICATED_BEARER_EVENT = 110;
    private static final int EVENT_DELAYED_CALL_STATE_CHANGE = 200;
    private static final int EVENT_DIALOG_EVENT_RECEIVED = 105;
    private static final int EVENT_DTMF_INFO = 112;
    private static final int EVENT_END_CALL_RESPONSE = 2;
    private static final int EVENT_HOLD_CALL_RESPONSE = 4;
    private static final int EVENT_INFO_CALL_RESPONSE = 7;
    private static final int EVENT_MAKE_CALL_RESPONSE = 1;
    private static final int EVENT_MERGE_CALL_RESPONSE = 3;
    private static final int EVENT_MODIFY_CALL = 106;
    private static final int EVENT_NEW_INCOMING_CALL = 101;
    private static final int EVENT_PULLING_CALL_RESPONSE = 6;
    private static final int EVENT_QUANTUM_SECURITY_STATUS = 116;
    private static final int EVENT_REFER_RECEIVED = 103;
    private static final int EVENT_REFER_STATUS = 104;
    private static final int EVENT_RESUME_CALL_RESPONSE = 5;
    private static final int EVENT_RRC_CONNECTION = 111;
    private static final int EVENT_RTP_LOSS_RATE_NOTI = 108;
    private static final int EVENT_SIPMSG_INFO = 114;
    private static final int EVENT_TEXT_INFO = 113;
    private static final int EVENT_UPDATE_AUDIO_INTEFACE_RESPONSE = 8;
    private static final String LOCATION_DISCOVERY = "application/vnd.3gpp.current-location-discovery+xml";
    private static final String LOG_TAG = "ResipVolteHandler";
    private static final int MO_TIMEOUT_MILLIS = 30000;
    private static final int MT_WAKELOCK_TIME = 1000;
    private static final int REMOVE_CONF_PARTICIPANT = 1;
    private static final String URN_SOS = "urn:service:sos";
    private static final String URN_SOS_AMBULANCE = "urn:service:sos.ambulance";
    private static final String URN_SOS_FIRE = "urn:service:sos.fire";
    private static final String URN_SOS_MARINE = "urn:service:sos.marine";
    private static final String URN_SOS_MOUNTAIN = "urn:service:sos.mountain";
    private static final String URN_SOS_POLICE = "urn:service:sos.police";
    private static final String USSD_INDI_BY_MESSAGE_MIME_TYPE = "application/ussd";
    private static final String USSD_MIME_TYPE = "application/vnd.3gpp.ussd+xml";
    private static final String VCS_INFO_MIME_TYPE = "application/text";
    private static final int VCS_SLIDING_END = -3;
    private static final int VCS_SLIDING_INVALID = 0;
    private static final int VCS_SLIDING_PRE = -2;
    private static final int VCS_SLIDING_START = -1;
    private static final Set<String> mMainSosSubserviceSet = new HashSet(Arrays.asList(new String[]{"urn:service:sos", URN_SOS_AMBULANCE, URN_SOS_FIRE, URN_SOS_MARINE, URN_SOS_MOUNTAIN, URN_SOS_POLICE}));
    private AudioInterfaceHandler mAudioInterfaceHandler = null;
    private HandlerThread mAudioInterfaceThread = null;
    protected boolean[] mAutomaticMode;
    private final SparseArray<Call> mCallList = new SparseArray<>();
    private final RegistrantList mCallStateEventRegistrants = new RegistrantList();
    private final RegistrantList mCdpnInfoRegistrants = new RegistrantList();
    private final RegistrantList mCmcInfoEventRegistrants = new RegistrantList();
    private final Context mContext;
    private final RegistrantList mCurrentLocationDiscoveryDuringEmergencyCallRegistrants = new RegistrantList();
    private final RegistrantList mDedicatedBearerEventRegistrants = new RegistrantList();
    private final RegistrantList mDialogEventRegistrants = new RegistrantList();
    private final RegistrantList mDtmfEventRegistrants = new RegistrantList();
    private final IImsFramework mImsFramework;
    private final RegistrantList mIncomingCallEventRegistrants = new RegistrantList();
    private boolean[] mOutOfService;
    private final RegistrantList mQuantumSecurityStatusEventRegistrants = new RegistrantList();
    private final RegistrantList mReferStatusRegistrants = new RegistrantList();
    private final RegistrantList mRrcConnectionEventRegistrants = new RegistrantList();
    private final RegistrantList mRtpLossRateNotiRegistrants = new RegistrantList();
    protected int[] mRttMode;
    private final RegistrantList mSIPMSGNotiRegistrants = new RegistrantList();
    private StackIF mStackIf;
    private final RegistrantList mTextEventRegistrants = new RegistrantList();
    protected int[] mTtyMode;
    private final RegistrantList mUssdEventRegistrants = new RegistrantList();

    private static class AlternativeService {
        CallStateEvent.ALTERNATIVE_SERVICE mAction = CallStateEvent.ALTERNATIVE_SERVICE.NONE;
        String mReason;
        String mType;
    }

    private int convertDedicatedBearerState(int i) {
        if (i == 1) {
            return 1;
        }
        if (i != 2) {
            return i != 3 ? 0 : 3;
        }
        return 2;
    }

    private int getParticipantStatus(int i) {
        if (i == 1) {
            return 1;
        }
        if (i == 2) {
            return 2;
        }
        if (i == 3) {
            return 3;
        }
        if (i == 4) {
            return 4;
        }
        if (i == 5) {
            return 5;
        }
        return i == 6 ? 6 : 0;
    }

    static class Call {
        boolean isConference;
        int mCallType;
        CountDownLatch mLock;
        CallParams mParam;
        NameAddr mPeer;
        CallResponse mResponse;
        int mSessionId;
        UserAgent mUa;

        public Call(UserAgent userAgent, ImsUri imsUri, String str) {
            this.mLock = null;
            this.mSessionId = -1;
            this.mResponse = null;
            this.isConference = false;
            this.mUa = userAgent;
            this.mPeer = new NameAddr(str, imsUri);
            this.mSessionId = -1;
        }

        public Call(UserAgent userAgent, NameAddr nameAddr) {
            this.mLock = null;
            this.mResponse = null;
            this.isConference = false;
            this.mUa = userAgent;
            this.mPeer = nameAddr;
            this.mSessionId = -1;
        }
    }

    private static class UssdReceived {
        boolean hasErrorCode;
        String mString;
        Type mType;

        enum Type {
            RESPONSE_TO_USER_INIT,
            NET_INIT_REQUEST,
            NET_INIT_NOTIFY
        }

        private UssdReceived() {
            this.hasErrorCode = false;
        }

        /* access modifiers changed from: package-private */
        public int getVolteConstantsUssdStatus() {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type[this.mType.ordinal()];
            int i2 = 1;
            if (i != 1) {
                i2 = 2;
                if (!(i == 2 || i == 3)) {
                    Log.e(ResipVolteHandler.LOG_TAG, "Invalid USSD type! - " + this.mType);
                    return -1;
                }
            }
            return i2;
        }
    }

    /* renamed from: com.sec.internal.ims.core.handler.secims.ResipVolteHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.sec.internal.ims.core.handler.secims.ResipVolteHandler$UssdReceived$Type[] r0 = com.sec.internal.ims.core.handler.secims.ResipVolteHandler.UssdReceived.Type.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type = r0
                com.sec.internal.ims.core.handler.secims.ResipVolteHandler$UssdReceived$Type r1 = com.sec.internal.ims.core.handler.secims.ResipVolteHandler.UssdReceived.Type.NET_INIT_NOTIFY     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.core.handler.secims.ResipVolteHandler$UssdReceived$Type r1 = com.sec.internal.ims.core.handler.secims.ResipVolteHandler.UssdReceived.Type.NET_INIT_REQUEST     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$handler$secims$ResipVolteHandler$UssdReceived$Type     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.core.handler.secims.ResipVolteHandler$UssdReceived$Type r1 = com.sec.internal.ims.core.handler.secims.ResipVolteHandler.UssdReceived.Type.RESPONSE_TO_USER_INIT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.AnonymousClass1.<clinit>():void");
        }
    }

    public static class UssdXmlParser {
        private static UssdXmlParser sInstance;
        XPath mXPath;
        XPathExpression mXPathErrorCode;
        XPathExpression mXPathNiNotify;
        XPathExpression mXPathNiRequest;
        XPathExpression mXPathUssdData;
        XPathExpression mXPathUssdString;

        public static UssdXmlParser getInstance() {
            if (sInstance == null) {
                sInstance = new UssdXmlParser();
            }
            return sInstance;
        }

        private UssdXmlParser() {
            init();
        }

        private void init() {
            XPath newXPath = XPathFactory.newInstance().newXPath();
            this.mXPath = newXPath;
            try {
                this.mXPathUssdData = newXPath.compile("/ussd-data");
                this.mXPathUssdString = this.mXPath.compile("ussd-string");
                this.mXPathErrorCode = this.mXPath.compile("error-code");
                this.mXPathNiRequest = this.mXPath.compile("boolean(anyExt/UnstructuredSS-Request)");
                this.mXPathNiNotify = this.mXPath.compile("boolean(anyExt/UnstructuredSS-Notify)");
            } catch (XPathExpressionException e) {
                Log.e(ResipVolteHandler.LOG_TAG, "XPath compile failed!", e);
            }
        }

        /* access modifiers changed from: private */
        public UssdReceived parseUssdXml(String str) throws XPathExpressionException {
            UssdReceived ussdReceived = new UssdReceived();
            if (str.contains("&")) {
                str = str.replaceAll("(?i)&(?!(#x?[\\d\\w]+;)|(quot;)|(lt;)|(gt;)|(apos;)|(amp;))", "&amp;");
            }
            Node node = (Node) this.mXPathUssdData.evaluate(new InputSource(new StringReader(str)), XPathConstants.NODE);
            String evaluate = this.mXPathErrorCode.evaluate(node);
            String evaluate2 = this.mXPathUssdString.evaluate(node);
            if (TextUtils.isEmpty(evaluate) || !TextUtils.isEmpty(evaluate2)) {
                ussdReceived.mString = evaluate2;
            } else {
                ussdReceived.mString = "error-code" + evaluate;
                ussdReceived.hasErrorCode = true;
            }
            Boolean bool = (Boolean) this.mXPathNiNotify.evaluate(node, XPathConstants.BOOLEAN);
            if (((Boolean) this.mXPathNiRequest.evaluate(node, XPathConstants.BOOLEAN)).booleanValue()) {
                ussdReceived.mType = UssdReceived.Type.NET_INIT_REQUEST;
            } else if (bool.booleanValue()) {
                ussdReceived.mType = UssdReceived.Type.NET_INIT_NOTIFY;
            } else {
                ussdReceived.mType = UssdReceived.Type.RESPONSE_TO_USER_INIT;
            }
            return ussdReceived;
        }
    }

    public static class InfoXmlParser {
        private static InfoXmlParser sInstance;
        XPath mXPath;

        public static InfoXmlParser getInstance() {
            if (sInstance == null) {
                sInstance = new InfoXmlParser();
            }
            return sInstance;
        }

        private InfoXmlParser() {
            init();
        }

        private void init() {
            this.mXPath = XPathFactory.newInstance().newXPath();
        }

        /* access modifiers changed from: private */
        public String parseInfoXml(String str) throws Exception {
            String str2 = "oneShot";
            try {
                NodeList nodeList = (NodeList) this.mXPath.evaluate("//requestForLocationInformation/*", DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(str.replaceAll("(\r\n|\r|\n|\n\r)", "")))), XPathConstants.NODESET);
                int i = 0;
                while (true) {
                    if (i >= nodeList.getLength()) {
                        str2 = MessageContextValues.none;
                        break;
                    } else if (str2.equals(nodeList.item(i).getNodeName())) {
                        break;
                    } else {
                        i++;
                    }
                }
                return str2;
            } catch (Exception e) {
                Log.e(ResipVolteHandler.LOG_TAG, "exception" + e);
                return MessageContextValues.none;
            }
        }
    }

    public ResipVolteHandler(Looper looper, Context context, IImsFramework iImsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = iImsFramework;
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerNewIncomingCallEvent(this, 101, (Object) null);
        this.mStackIf.registerCallStatusEvent(this, 100, (Object) null);
        this.mStackIf.registerModifyCallEvent(this, 106, (Object) null);
        this.mStackIf.registerConferenceUpdateEvent(this, 102, (Object) null);
        this.mStackIf.registerReferReceivedEvent(this, 103, (Object) null);
        this.mStackIf.registerReferStatusEvent(this, 104, (Object) null);
        this.mStackIf.registerDialogEvent(this, 105, (Object) null);
        this.mStackIf.registerCdpnInfoEvent(this, 107, (Object) null);
        this.mStackIf.registerDedicatedBearerEvent(this, 110, (Object) null);
        this.mStackIf.registerForRrcConnectionEvent(this, 111, (Object) null);
        this.mStackIf.registerQuantumSecurityStatusEvent(this, 116, (Object) null);
        this.mStackIf.registerRtpLossRateNoti(this, 108, (Object) null);
        this.mStackIf.registerDtmfEvent(this, 112, (Object) null);
        this.mStackIf.registerTextEvent(this, 113, (Object) null);
        this.mStackIf.registerSIPMSGEvent(this, 114, (Object) null);
        this.mStackIf.registerCmcInfo(this, 115, (Object) null);
        this.mStackIf.registerCurrentLocationDiscoveryDuringEmergencyCallEvent(this, 117, (Object) null);
        int size = SimManagerFactory.getAllSimManagers().size();
        int[] iArr = new int[size];
        this.mTtyMode = iArr;
        this.mRttMode = new int[size];
        this.mAutomaticMode = new boolean[size];
        this.mOutOfService = new boolean[size];
        Arrays.fill(iArr, Extensions.TelecomManager.TTY_MODE_OFF);
        Arrays.fill(this.mRttMode, -1);
        Arrays.fill(this.mAutomaticMode, false);
        HandlerThread handlerThread = new HandlerThread("AudioInterfaceThread");
        this.mAudioInterfaceThread = handlerThread;
        handlerThread.start();
        this.mAudioInterfaceHandler = new AudioInterfaceHandler(this.mAudioInterfaceThread.getLooper());
    }

    public void registerForCallStateEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForCallStateEvent:");
        this.mCallStateEventRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForCallStateEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForCallStateEvent:");
        this.mCallStateEventRegistrants.remove(handler);
    }

    public void registerForIncomingCallEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForCallStateEvent:");
        this.mIncomingCallEventRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForIncomingCallEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForCallStateEvent:");
        this.mIncomingCallEventRegistrants.remove(handler);
    }

    public void registerForUssdEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForUssdEvent:");
        this.mUssdEventRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForUssdEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForUssdEvent:");
        this.mUssdEventRegistrants.remove(handler);
    }

    public void registerForCurrentLocationDiscoveryDuringEmergencyCallEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForCurrentLocationDiscoveryDuringEmergencyCallEvent:");
        this.mCurrentLocationDiscoveryDuringEmergencyCallRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForCurrentLocationDiscoveryDuringEmergencyCallEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForCurrentLocationDiscoveryDuringEmergencyCallEvent:");
        this.mCurrentLocationDiscoveryDuringEmergencyCallRegistrants.remove(handler);
    }

    public void registerForReferStatus(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForReferStatus:");
        this.mReferStatusRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForReferStatus(Handler handler) {
        Log.i(LOG_TAG, "unregisterForReferStatus:");
        this.mReferStatusRegistrants.remove(handler);
    }

    public void registerForDialogEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForDialogEvent:");
        this.mDialogEventRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForDialogEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForDialogEvent:");
        this.mDialogEventRegistrants.remove(handler);
    }

    public void registerForCmcInfoEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForCmcInfoEvent:");
        this.mCmcInfoEventRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForCmcInfoEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForCmcInfoEvent:");
        this.mCmcInfoEventRegistrants.remove(handler);
    }

    public void registerForCdpnInfoEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForCdpnInfoEvent:");
        this.mCdpnInfoRegistrants.add(new Registrant(handler, i, obj));
    }

    public void registerForDedicatedBearerNotifyEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForDedicatedBearerNotifyEvent:");
        this.mDedicatedBearerEventRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForDedicatedBearerNotifyEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForDedicatedBearerNotifyEvent:");
        this.mDedicatedBearerEventRegistrants.remove(handler);
    }

    public void registerForRrcConnectionEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForRrcConnectionEvent:");
        this.mRrcConnectionEventRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForRrcConnectionEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForRrcConnectionEvent:");
        this.mRrcConnectionEventRegistrants.remove(handler);
    }

    public void registerQuantumSecurityStatusEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerQuantumSecurityStatusEvent:");
        this.mQuantumSecurityStatusEventRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterQuantumSecurityStatusEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterQuantumSecurityStatusEvent:");
        this.mQuantumSecurityStatusEventRegistrants.remove(handler);
    }

    public void registerForDtmfEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForDtmfEvent:");
        this.mDtmfEventRegistrants.add(handler, i, obj);
    }

    public void unregisterForDtmfEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForDtmfEvent:");
        this.mDtmfEventRegistrants.remove(handler);
    }

    public void registerForTextEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForTextEvent:");
        this.mTextEventRegistrants.add(handler, i, obj);
    }

    public void unregisterForTextEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForTextEvent:");
        this.mTextEventRegistrants.remove(handler);
    }

    public void registerForSIPMSGEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForSIPMSGEvent:");
        this.mSIPMSGNotiRegistrants.add(handler, i, obj);
    }

    public void unregisterForSIPMSGEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForSIPMSGEvent:");
        this.mSIPMSGNotiRegistrants.remove(handler);
    }

    public void registerForRtpLossRateNoti(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForRtpLossRateNoti:");
        this.mRtpLossRateNotiRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForRtpLossRateNoti(Handler handler) {
        Log.i(LOG_TAG, "unregisterForRtpLossRateNoti:");
        this.mRtpLossRateNotiRegistrants.remove(handler);
    }

    public void unregisterForCdpnInfoEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForCdpnInfoEvent:");
        this.mCdpnInfoRegistrants.remove(handler);
    }

    private AdditionalContents createUssdContents(int i, String str, int i2) {
        XmlSerializer newSerializer = Xml.newSerializer();
        StringWriter stringWriter = new StringWriter();
        try {
            newSerializer.setOutput(stringWriter);
            newSerializer.startDocument("UTF-8", (Boolean) null);
            newSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            newSerializer.startTag("", "ussd-data");
            Mno simMno = SimUtil.getSimMno(i);
            if (simMno != Mno.SMART_CAMBODIA) {
                newSerializer.startTag("", CloudMessageProviderContract.VVMAccountInfoColumns.LANGUAGE);
                if (simMno == Mno.HK3) {
                    newSerializer.text("un");
                } else {
                    if (simMno != Mno.H3G_AT) {
                        if (simMno != Mno.TIGO_BOLIVIA) {
                            newSerializer.text("en");
                        }
                    }
                    newSerializer.text("undefined");
                }
                newSerializer.endTag("", CloudMessageProviderContract.VVMAccountInfoColumns.LANGUAGE);
            }
            if (i2 == 3) {
                Log.i(LOG_TAG, "createUssdContents: error - \n" + str);
                newSerializer.startTag("", "error-code");
                newSerializer.text(str);
                newSerializer.endTag("", "error-code");
            } else if (i2 == 4) {
                Log.i(LOG_TAG, "createUssdContents: notify response");
                newSerializer.startTag("", "UnstructuredSS-Notify");
                newSerializer.endTag("", "UnstructuredSS-Notify");
            } else {
                Log.i(LOG_TAG, "createUssdContents: dialstring - \n" + str);
                newSerializer.startTag("", "ussd-string");
                newSerializer.text(str);
                newSerializer.endTag("", "ussd-string");
            }
            newSerializer.endTag("", "ussd-data");
            newSerializer.endDocument();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            Log.e(LOG_TAG, "Failed to createUssdContents()", e);
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) USSD_MIME_TYPE);
        int createString2 = flatBufferBuilder.createString((CharSequence) stringWriter.toString());
        AdditionalContents.startAdditionalContents(flatBufferBuilder);
        AdditionalContents.addMimeType(flatBufferBuilder, createString);
        AdditionalContents.addContents(flatBufferBuilder, createString2);
        flatBufferBuilder.finish(AdditionalContents.endAdditionalContents(flatBufferBuilder));
        AdditionalContents rootAsAdditionalContents = AdditionalContents.getRootAsAdditionalContents(flatBufferBuilder.dataBuffer());
        Log.i(LOG_TAG, "createUssdContents: built contents - \n" + rootAsAdditionalContents.contents());
        return rootAsAdditionalContents;
    }

    private AdditionalContents createCmcInfoContents(int i, int i2, int i3, String str) {
        XmlSerializer newSerializer = Xml.newSerializer();
        StringWriter stringWriter = new StringWriter();
        try {
            newSerializer.setOutput(stringWriter);
            newSerializer.startDocument("UTF-8", (Boolean) null);
            newSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            newSerializer.startTag("", "cmc-info-data");
            newSerializer.startTag("", "record-event");
            newSerializer.text(Integer.toString(i2));
            newSerializer.endTag("", "record-event");
            newSerializer.startTag("", "extra");
            newSerializer.text(Integer.toString(i3));
            newSerializer.endTag("", "extra");
            newSerializer.startTag("", "external-call-id");
            newSerializer.text(str);
            newSerializer.endTag("", "external-call-id");
            newSerializer.endTag("", "cmc-info-data");
            newSerializer.endDocument();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            Log.e(LOG_TAG, "Failed to createCmcInfoContents()", e);
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) CMC_INFO_MIME_TYPE);
        int createString2 = flatBufferBuilder.createString((CharSequence) stringWriter.toString());
        AdditionalContents.startAdditionalContents(flatBufferBuilder);
        AdditionalContents.addMimeType(flatBufferBuilder, createString);
        AdditionalContents.addContents(flatBufferBuilder, createString2);
        flatBufferBuilder.finish(AdditionalContents.endAdditionalContents(flatBufferBuilder));
        AdditionalContents rootAsAdditionalContents = AdditionalContents.getRootAsAdditionalContents(flatBufferBuilder.dataBuffer());
        Log.i(LOG_TAG, "createCmcInfoContents: built contents - \n" + rootAsAdditionalContents.contents());
        return rootAsAdditionalContents;
    }

    private AdditionalContents createVcsInfoContents(String str, int i, int i2, int i3, int i4, String str2) {
        String str3 = str;
        Log.i(LOG_TAG, "createVcsInfoContents event " + str3);
        XmlSerializer newSerializer = Xml.newSerializer();
        StringWriter stringWriter = new StringWriter();
        try {
            newSerializer.setOutput(stringWriter);
            newSerializer.startDocument("UTF-8", (Boolean) null);
            newSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            newSerializer.startTag("", "msml");
            newSerializer.attribute("", "version", "1.1");
            if (!"touch".equals(str3)) {
                if (!"slide".equals(str3)) {
                    newSerializer.startTag("", "send");
                    newSerializer.attribute("", "event", str3);
                    newSerializer.endTag("", "send");
                    newSerializer.endTag("", "msml");
                    newSerializer.endDocument();
                    FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
                    int createString = flatBufferBuilder.createString((CharSequence) VCS_INFO_MIME_TYPE);
                    int createString2 = flatBufferBuilder.createString((CharSequence) stringWriter.toString().replaceAll("'", CmcConstants.E_NUM_STR_QUOTE));
                    AdditionalContents.startAdditionalContents(flatBufferBuilder);
                    AdditionalContents.addMimeType(flatBufferBuilder, createString);
                    AdditionalContents.addContents(flatBufferBuilder, createString2);
                    flatBufferBuilder.finish(AdditionalContents.endAdditionalContents(flatBufferBuilder));
                    AdditionalContents rootAsAdditionalContents = AdditionalContents.getRootAsAdditionalContents(flatBufferBuilder.dataBuffer());
                    Log.i(LOG_TAG, "createVcsInfoContents: built contents - \n" + rootAsAdditionalContents.contents());
                    return rootAsAdditionalContents;
                }
            }
            newSerializer.startTag("", "position");
            String str4 = "(" + Integer.toString(i) + ", " + Integer.toString(i2) + ")";
            if ("slide".equals(str3)) {
                str4 = str4 + " (" + Integer.toString(i4) + ", " + str2 + ")";
                str3 = "move";
            }
            newSerializer.attribute("", "digits", str4 + "#");
            newSerializer.attribute("", "dur", Integer.toString(i3));
            newSerializer.startTag("", "positionexit");
            newSerializer.startTag("", "send");
            newSerializer.attribute("", "event", str3);
            newSerializer.endTag("", "send");
            newSerializer.endTag("", "positionexit");
            newSerializer.endTag("", "position");
            newSerializer.endTag("", "msml");
            newSerializer.endDocument();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            Log.e(LOG_TAG, "Failed to createVcsInfoContents()", e);
        }
        FlatBufferBuilder flatBufferBuilder2 = new FlatBufferBuilder(0);
        int createString3 = flatBufferBuilder2.createString((CharSequence) VCS_INFO_MIME_TYPE);
        int createString22 = flatBufferBuilder2.createString((CharSequence) stringWriter.toString().replaceAll("'", CmcConstants.E_NUM_STR_QUOTE));
        AdditionalContents.startAdditionalContents(flatBufferBuilder2);
        AdditionalContents.addMimeType(flatBufferBuilder2, createString3);
        AdditionalContents.addContents(flatBufferBuilder2, createString22);
        flatBufferBuilder2.finish(AdditionalContents.endAdditionalContents(flatBufferBuilder2));
        AdditionalContents rootAsAdditionalContents2 = AdditionalContents.getRootAsAdditionalContents(flatBufferBuilder2.dataBuffer());
        Log.i(LOG_TAG, "createVcsInfoContents: built contents - \n" + rootAsAdditionalContents2.contents());
        return rootAsAdditionalContents2;
    }

    public int makeCall(int i, CallSetupData callSetupData, HashMap<String, String> hashMap, int i2) {
        UserAgent userAgent;
        int i3 = i;
        CallSetupData callSetupData2 = callSetupData;
        int i4 = i2;
        Log.i(LOG_TAG, "makeCall: regId=" + i3 + " " + callSetupData2 + " additionalSipHeaders=" + hashMap);
        ImsUri destinationUri = callSetupData.getDestinationUri();
        int callType = callSetupData.getCallType();
        boolean isEmergency = callSetupData.isEmergency();
        boolean z = callType == 12;
        if (!isEmergency || i3 >= 0) {
            userAgent = getUaByRegId(i);
        } else {
            Log.i(LOG_TAG, "makeCall: using emergency UA.");
            userAgent = getEmergencyUa(i4);
        }
        if (userAgent == null) {
            Log.e(LOG_TAG, "makeCall: UserAgent not found.");
            return -1;
        }
        String imsUri = callSetupData.getOriginatingUri() != null ? callSetupData.getOriginatingUri().toString() : null;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Call call = new Call(userAgent, destinationUri, callSetupData.getDialingNumber());
        call.mCallType = convertToCallTypeForward(callType);
        CallParams callParams = new CallParams();
        String audioCodec = userAgent.getImsProfile().getAudioCodec();
        Mno fromName = Mno.fromName(call.mUa.getImsProfile().getMnoName());
        if (fromName == Mno.RJIL && isOutOfService(i4)) {
            IMSLog.i(LOG_TAG, i4, "Delete TCP socket when out of service");
            setOutOfService(false, i4);
            call.mUa.deleteTcpClientSocket();
        }
        if ((fromName == Mno.KDDI || fromName == Mno.DOCOMO) && audioCodec.contains("EVS")) {
            String evsBandwidthSend = userAgent.getImsProfile().getEvsBandwidthSend();
            if (evsBandwidthSend.contains("fb")) {
                callParams.setAudioCodec("EVS-FB");
            } else if (evsBandwidthSend.contains("swb")) {
                callParams.setAudioCodec("EVS-SWB");
            } else if (evsBandwidthSend.contains("wb")) {
                callParams.setAudioCodec("EVS-WB");
            } else if (evsBandwidthSend.contains("nb")) {
                callParams.setAudioCodec("EVS-NB");
            }
        } else if (audioCodec.contains("AMR-WB") || audioCodec.contains("AMRBE-WB")) {
            callParams.setAudioCodec("AMR-WB");
        } else {
            callParams.setAudioCodec("AMR-NB");
        }
        callParams.setCmcEdCallSlot(callSetupData.getCmcEdCallSlot());
        String cli = userAgent.getImsProfile().getSupportClir() ? callSetupData.getCli() : null;
        call.mParam = callParams;
        call.mLock = countDownLatch;
        Log.i(LOG_TAG, "makeCall: Do device support 3gpp 24.390 USSI?" + userAgent.getImsProfile().getSupport3gppUssi());
        boolean z2 = z && userAgent.getImsProfile().getSupport3gppUssi();
        String imsUri2 = destinationUri.toString();
        int i5 = call.mCallType;
        String letteringText = callSetupData.getLetteringText();
        String dialingNumber = callSetupData.getDialingNumber();
        AdditionalContents createUssdContents = z2 ? createUssdContents(userAgent.getPhoneId(), callSetupData.getDialingNumber(), 0) : null;
        String pEmergencyInfo = callSetupData.getPEmergencyInfo();
        String alertInfo = callSetupData.getAlertInfo();
        boolean lteEpsOnlyAttached = callSetupData.getLteEpsOnlyAttached();
        List<String> p2p = callSetupData.getP2p();
        int cmcBoundSessionId = callSetupData.getCmcBoundSessionId();
        Bundle composerData = callSetupData.getComposerData();
        String replaceCallId = callSetupData.getReplaceCallId();
        int cmcEdCallSlot = callSetupData.getCmcEdCallSlot();
        String idcExtra = callSetupData.getIdcExtra();
        Message obtainMessage = obtainMessage(1, call);
        Call call2 = call;
        CountDownLatch countDownLatch2 = countDownLatch;
        String str = LOG_TAG;
        userAgent.makeCall(imsUri2, imsUri, i5, letteringText, dialingNumber, createUssdContents, cli, pEmergencyInfo, hashMap, alertInfo, lteEpsOnlyAttached, p2p, cmcBoundSessionId, composerData, replaceCallId, cmcEdCallSlot, idcExtra, obtainMessage);
        try {
            if (!countDownLatch2.await(30000, TimeUnit.MILLISECONDS)) {
                Log.e(str, "makeCall: timeout.");
                return -1;
            }
            CallResponse callResponse = call2.mResponse;
            if (callResponse == null || callResponse.result() == 0) {
                IMSLog.c(LogClass.VOLTE_MAKE_CALL, "MakeCall," + i4 + "," + call2.mSessionId);
                if (!Debug.isProductShip() && userAgent.getImsProfile().getCmcType() > 0) {
                    CmcPingTestLogger.ping(userAgent.getImsProfile().getPcscfList());
                }
                return call2.mSessionId;
            }
            Log.e(str, "makeCall: call failed. reason " + call2.mResponse.reason());
            callSetupData2.setCallSetupError(call2.mResponse.reason());
            return -1;
        } catch (InterruptedException unused) {
            return -1;
        }
    }

    public int rejectCall(int i, int i2, SipError sipError) {
        Log.i(LOG_TAG, "rejectCall: sessionId " + i + " callType " + i2 + " error " + sipError);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "rejectCall: session not found.");
            return -1;
        }
        callBySession.mUa.rejectCall(callBySession.mSessionId, sipError);
        return 0;
    }

    public int deleteTcpSocket(int i, int i2) {
        Log.i(LOG_TAG, "DeleteTcpSocket: sessionId " + i + " callType " + i2);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "DeleteTcpSocket: session not found.");
            return -1;
        }
        callBySession.mUa.deleteTcpClientSocket();
        return 0;
    }

    public int endCall(int i, int i2, SipReason sipReason) {
        Log.i(LOG_TAG, "endCall: sessionId " + i + " callType " + i2 + " reason " + sipReason);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "endCall: session not found.");
            return -1;
        }
        Mno fromName = Mno.fromName(callBySession.mUa.getImsProfile().getMnoName());
        if (sipReason != null) {
            Log.i(LOG_TAG, "endCall: reason : " + sipReason.getText());
            if (fromName.isJpn()) {
                if (fromName == Mno.DOCOMO && ("PS BARRING".equals(sipReason.getText()) || "RRC CONNECTION REJECT".equals(sipReason.getText()))) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket()");
                    callBySession.mUa.deleteTcpClientSocket();
                }
                if ((fromName == Mno.KDDI || fromName == Mno.DOCOMO) && "INVITE FLUSH".equals(sipReason.getText())) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket() at INVITE FLUSH");
                    callBySession.mUa.deleteTcpClientSocket();
                }
            } else if (fromName == Mno.CMCC) {
                if ("SRVCC".equals(sipReason.getText())) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket()");
                    callBySession.mUa.deleteTcpClientSocket();
                }
            } else if (fromName == Mno.VZW) {
                if ("RRC CONNECTION REJECT".equals(sipReason.getText())) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket()");
                    callBySession.mUa.deleteTcpClientSocket();
                }
            } else if (fromName.isOrangeGPG() || fromName == Mno.ORANGE_MOLDOVA) {
                if ("SIP response time-out".equals(sipReason.getText())) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket()");
                    callBySession.mUa.deleteTcpClientSocket();
                }
            } else if (fromName == Mno.KDDI || fromName == Mno.FET) {
                if ("SESSIONPROGRESS TIMEOUT".equals(sipReason.getText())) {
                    Log.i(LOG_TAG, "deleteTcpClientSocket()");
                    callBySession.mUa.deleteTcpClientSocket();
                }
            } else if (fromName.isKor() && "INVITE FLUSH".equals(sipReason.getText())) {
                Log.i(LOG_TAG, "deleteTcpClientSocket() at INVITE FLUSH");
                callBySession.mUa.deleteTcpClientSocket();
            }
        }
        callBySession.mUa.endCall(callBySession.mSessionId, sipReason);
        return 0;
    }

    public int proceedIncomingCall(int i, HashMap<String, String> hashMap, String str) {
        Log.i(LOG_TAG, "proceedIncomingCall: sessoinId " + i);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "proceedIncomingCall: session not found.");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_INCOMING_CALL, "IncomingCall," + callBySession.mUa.getPhoneId() + "," + callBySession.mSessionId);
        callBySession.mUa.progressIncomingCall(callBySession.mSessionId, hashMap, str);
        return 0;
    }

    public int answerCallWithCallType(int i, int i2) {
        return answerCall(i, convertToCallTypeForward(i2), "0", (String) null);
    }

    public int answerCallWithCallType(int i, int i2, String str) {
        return answerCall(i, convertToCallTypeForward(i2), str, (String) null);
    }

    public int answerCallWithCallType(int i, int i2, String str, String str2) {
        return answerCall(i, convertToCallTypeForward(i2), str, str2);
    }

    private int answerCall(int i, int i2, String str, String str2) {
        Log.i(LOG_TAG, "answerCallWithCallType: sessionId " + i + " callType " + i2 + " cmcCallEstablishTime " + str + " idcExtra " + str2);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "answerCallWithCallType: session not found.");
            dumpCall();
            return -1;
        }
        if (i2 == -1) {
            i2 = callBySession.mCallType;
        }
        callBySession.mUa.answerCall(i, i2, str, str2);
        if (Debug.isProductShip() || callBySession.mUa.getImsProfile().getCmcType() <= 0) {
            return 0;
        }
        CmcPingTestLogger.ping(callBySession.mUa.getImsProfile().getPcscfList());
        return 0;
    }

    public int sendText(int i, String str, int i2) {
        Log.i(LOG_TAG, "sendText: sessionId " + i + ", text: " + str + " len : " + i2);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendText: session not found.");
            return -1;
        }
        callBySession.mUa.sendText(i, str, i2);
        return 0;
    }

    public int handleDtmf(int i, int i2, int i3, int i4, Message message) {
        Log.i(LOG_TAG, "handleDtmf: sessionId " + i + " code " + IMSLog.checker(Integer.valueOf(i2)) + " mode " + i3 + " operation " + i4);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendDtmf: session not found.");
            return -1;
        }
        callBySession.mUa.handleDtmf(i, i2, i3, i4, message);
        return 0;
    }

    public int holdCall(int i) {
        Log.i(LOG_TAG, "holdCall: sessionId " + i);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "holdCall: session not found.");
            dumpCall();
            return -1;
        }
        callBySession.mParam.setIndicationFlag(0);
        IMSLog.c(LogClass.VOLTE_HOLD_CALL, "HoldCall," + callBySession.mUa.getPhoneId() + "," + i);
        callBySession.mUa.holdCall(i, obtainMessage(4));
        return 0;
    }

    public int resumeCall(int i) {
        Log.i(LOG_TAG, "resumeCall: sessionId " + i);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "resumeCall: session not found.");
            dumpCall();
            return -1;
        }
        callBySession.mParam.setIndicationFlag(0);
        IMSLog.c(LogClass.VOLTE_RESUME_CALL, "ResumeCall," + callBySession.mUa.getPhoneId() + "," + i);
        callBySession.mUa.resumeCall(i, obtainMessage(5));
        return 0;
    }

    public int startNWayConferenceCall(int i, ConfCallSetupData confCallSetupData) {
        ConfCallSetupData confCallSetupData2 = confCallSetupData;
        Log.i(LOG_TAG, "startNWayConferenceCall: regId=" + i + " " + confCallSetupData2);
        UserAgent uaByRegId = getUaByRegId(i);
        if (uaByRegId == null) {
            Log.e(LOG_TAG, "startNWayConferenceCall: no UserAgent found.");
            return -1;
        } else if (checkConfererenceCallData(confCallSetupData2) == -1) {
            return -1;
        } else {
            String imsUri = confCallSetupData.getOriginatingUri() != null ? confCallSetupData.getOriginatingUri().toString() : null;
            boolean supportPrematureEnd = confCallSetupData.getSupportPrematureEnd();
            if (confCallSetupData.getParticipants() != null) {
                return startNWayConferenceCall(uaByRegId, confCallSetupData.getConferenceUri(), imsUri, confCallSetupData.getParticipants(), confCallSetupData.getCallType(), confCallSetupData.isSubscriptionEnabled(), confCallSetupData.getSubscribeDialogType(), confCallSetupData.getReferUriType(), confCallSetupData.getRemoveReferUriType(), confCallSetupData.getReferUriAsserted(), confCallSetupData.getUseAnonymousUpdate(), supportPrematureEnd);
            } else if (confCallSetupData.getSessionIds().size() < 2) {
                Log.e(LOG_TAG, "startNWayConferenceCall: not enough sessionIds");
                return -1;
            } else {
                return startNWayConferenceCall(uaByRegId, confCallSetupData.getConferenceUri(), imsUri, confCallSetupData.getSessionIds().get(0).intValue(), confCallSetupData.getSessionIds().get(1).intValue(), confCallSetupData.getCallType(), confCallSetupData.isSubscriptionEnabled(), confCallSetupData.getSubscribeDialogType(), confCallSetupData.getReferUriType(), confCallSetupData.getRemoveReferUriType(), confCallSetupData.getReferUriAsserted(), confCallSetupData.getUseAnonymousUpdate(), supportPrematureEnd, confCallSetupData.getExtraSipHeaders());
            }
        }
    }

    private int startNWayConferenceCall(UserAgent userAgent, String str, String str2, int i, int i2, int i3, String str3, String str4, String str5, String str6, String str7, String str8, boolean z, HashMap<String, String> hashMap) {
        Call callBySession = getCallBySession(i);
        Call callBySession2 = getCallBySession(i2);
        if (callBySession == null || callBySession2 == null) {
            return -1;
        }
        Call call = new Call(userAgent, ImsUri.parse(str), "");
        int convertToCallTypeForward = convertToCallTypeForward(i3);
        call.mCallType = convertToCallTypeForward;
        call.isConference = true;
        call.mParam = new CallParams();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        call.mLock = countDownLatch;
        CountDownLatch countDownLatch2 = countDownLatch;
        Call call2 = call;
        userAgent.mergeCall(i, i2, str, convertToCallTypeForward, str3, str4, str2, str5, str6, str7, str8, z, hashMap, obtainMessage(3, call));
        try {
            if (!countDownLatch2.await(30000, TimeUnit.MILLISECONDS)) {
                Log.e(LOG_TAG, "startNWayConferenceCall: timeout.");
                return -1;
            }
            Call call3 = call2;
            CallResponse callResponse = call3.mResponse;
            if (callResponse == null || callResponse.result() == 0) {
                return call3.mSessionId;
            }
            Log.i(LOG_TAG, "startNWayConferenceCall: call failed. reason " + call3.mResponse.reason());
            return -1;
        } catch (InterruptedException unused) {
            return -1;
        }
    }

    private int startNWayConferenceCall(UserAgent userAgent, String str, String str2, List<String> list, int i, String str3, String str4, String str5, String str6, String str7, String str8, boolean z) {
        UserAgent userAgent2 = userAgent;
        Call call = new Call(userAgent2, ImsUri.parse(str), "");
        int convertToCallTypeForward = convertToCallTypeForward(i);
        call.mCallType = convertToCallTypeForward;
        call.isConference = true;
        call.mParam = new CallParams();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        call.mLock = countDownLatch;
        String str9 = str;
        String str10 = str3;
        String str11 = str4;
        String str12 = str2;
        String str13 = str5;
        String str14 = str6;
        String str15 = str7;
        String str16 = str8;
        boolean z2 = z;
        userAgent2.conference((String[]) list.toArray(new String[list.size()]), str9, convertToCallTypeForward, str10, str11, str12, str13, str14, str15, str16, z2, obtainMessage(3, call));
        try {
            if (!countDownLatch.await(30000, TimeUnit.MILLISECONDS)) {
                Log.e(LOG_TAG, "startNWayConferenceCall: timeout.");
                return -1;
            }
            CallResponse callResponse = call.mResponse;
            if (callResponse == null || callResponse.result() == 0) {
                return call.mSessionId;
            }
            Log.e(LOG_TAG, "startNWayConferenceCall: call failed. reason " + call.mResponse.reason());
            return -1;
        } catch (InterruptedException unused) {
            return -1;
        }
    }

    public int addParticipantToNWayConferenceCall(int i, int i2) {
        Log.i(LOG_TAG, "addParticipantToNWayConferenceCall (" + i + ") participantId " + i2);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "No conference session to add a participant");
            return -1;
        }
        callBySession.mUa.updateConfCall(i, 0, i2, "");
        return 0;
    }

    public int removeParticipantFromNWayConferenceCall(int i, int i2) {
        Log.i(LOG_TAG, "removeParticipantFromNWayConferenceCall (" + i + ") removeSession " + i2);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "No conference session to add a participant");
            return -1;
        }
        callBySession.mUa.updateConfCall(i, 1, i2, "");
        return 0;
    }

    public int addParticipantToNWayConferenceCall(int i, String str) {
        Log.i(LOG_TAG, "addParticipantToNWayConferenceCall (" + i + ") participant " + str);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "No conference session to add a participant");
            return -1;
        }
        callBySession.mUa.updateConfCall(i, 0, -1, str);
        return 0;
    }

    public int removeParticipantFromNWayConferenceCall(int i, String str) {
        Log.i(LOG_TAG, "removeParticipantFromNWayConferenceCall (" + i + ") participant " + str);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "No conference session to add a participant");
            return -1;
        }
        callBySession.mUa.updateConfCall(i, 1, -1, str);
        return 0;
    }

    public int modifyCallType(int i, int i2, int i3) {
        Log.i(LOG_TAG, "modifyCallType(): sessionId " + i + ", oldType " + i2 + ", newType " + i3);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "modifyCallType(): session not found.");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_MODIFY_CALL, "ModifyCall," + callBySession.mUa.getPhoneId() + "," + callBySession.mSessionId + "," + i2 + "," + i3);
        callBySession.mUa.modifyCallType(callBySession.mSessionId, i2, i3);
        return 0;
    }

    public int replyModifyCallType(int i, int i2, int i3, int i4) {
        return replyModifyCallType(i, i2, i3, i4, "");
    }

    public int replyModifyCallType(int i, int i2, int i3, int i4, String str) {
        Log.i(LOG_TAG, "replyModifyCallType(): sessionId " + i + ", reqType " + i4 + ", curType " + i2 + ", repType " + i3 + ", cmcCallTime " + str);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "replyModifyCallType(): session not found.");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_MODIFY_REPLY, "ReplyModifyCall," + callBySession.mUa.getPhoneId() + "," + callBySession.mSessionId + "," + i4 + "," + i2 + "," + i3);
        callBySession.mUa.replyModifyCallType(callBySession.mSessionId, i2, i3, i4, str);
        return 0;
    }

    public int replyWithIdc(int i, int i2, int i3, int i4, String str) {
        Log.i(LOG_TAG, "replyWithIdc(): sessionId " + i + ", idcExtra " + str);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "replyWithIdc(): session not found.");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_MODIFY_REPLY, "replyWithIdc," + callBySession.mUa.getPhoneId() + "," + callBySession.mSessionId + "," + i4 + "," + i2 + "," + i3);
        callBySession.mUa.replyWithIdc(callBySession.mSessionId, i2, i3, i4, str);
        return 0;
    }

    public int rejectModifyCallType(int i, int i2) {
        Log.i(LOG_TAG, "rejectModifyCallType(): sessionId " + i + ", reason" + i2);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "rejectModifyCallType(): session not found.");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_MODIFY_REJECT, "RejectModifyCall," + callBySession.mUa.getPhoneId() + "," + callBySession.mSessionId + "," + i2);
        callBySession.mUa.rejectModifyCallType(callBySession.mSessionId, i2);
        return 0;
    }

    public int sendReInvite(int i, SipReason sipReason) {
        Log.i(LOG_TAG, "sendReInvite(): sessionId " + i + ", reason " + sipReason);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendReInvite(): session not found.");
            return -1;
        }
        callBySession.mUa.updateCall(callBySession.mSessionId, 0, -1, sipReason, "");
        return 0;
    }

    public int sendReInviteWithIdcExtra(int i, String str) {
        Log.i(LOG_TAG, "sendReInviteWithIdcExtra(): sessionId " + i);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendReInviteWithIdcExtra(): session not found.");
            return -1;
        }
        callBySession.mUa.updateWithIdc(callBySession.mSessionId, 2, str);
        return 0;
    }

    private int checkConfererenceCallData(ConfCallSetupData confCallSetupData) {
        if (confCallSetupData.getConferenceUri() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: conference server uri is not configured.");
            return -1;
        } else if (confCallSetupData.isSubscriptionEnabled() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: confSubscribe no global xml file");
            return -1;
        } else if (confCallSetupData.getSubscribeDialogType() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: subscribeDialogType no global xml file");
            return -1;
        } else if (confCallSetupData.getReferUriType() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: referUriType no global xml file");
            return -1;
        } else if (confCallSetupData.getRemoveReferUriType() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: removeReferUriType no global xml file");
            return -1;
        } else if (confCallSetupData.getReferUriAsserted() == null) {
            Log.e(LOG_TAG, "checkConfererenceCallData: referUriAsserted no global xml file");
            return -1;
        } else if (confCallSetupData.getUseAnonymousUpdate() != null) {
            return 1;
        } else {
            Log.e(LOG_TAG, "checkConfererenceCallData: useAnonymousUpdate no global xml file");
            return -1;
        }
    }

    public int addUserForConferenceCall(int i, ConfCallSetupData confCallSetupData, boolean z) {
        ConfCallSetupData confCallSetupData2 = confCallSetupData;
        Log.i(LOG_TAG, "addUserForConferenceCall: sessionId=" + i + " " + confCallSetupData2 + " create " + z);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "addUserForConferenceCall: session not found.");
            return -1;
        }
        String imsUri = confCallSetupData.getOriginatingUri() != null ? confCallSetupData.getOriginatingUri().toString() : null;
        if (checkConfererenceCallData(confCallSetupData2) == -1) {
            return -1;
        }
        callBySession.mUa.extendToConfCall((String[]) confCallSetupData.getParticipants().toArray(new String[confCallSetupData.getParticipants().size()]), confCallSetupData.getConferenceUri(), convertToCallTypeForward(confCallSetupData.getCallType()), confCallSetupData.isSubscriptionEnabled(), confCallSetupData.getSubscribeDialogType(), i, imsUri, confCallSetupData.getReferUriType(), confCallSetupData.getRemoveReferUriType(), confCallSetupData.getReferUriAsserted(), confCallSetupData.getUseAnonymousUpdate(), confCallSetupData.getSupportPrematureEnd());
        return 0;
    }

    public int transferCall(int i, String str) {
        Log.i(LOG_TAG, "transferCall: sessionId " + i + " taruri " + IMSLog.checker(str));
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "transferCall: session not found.");
            return -1;
        }
        callBySession.mUa.transferCall(callBySession.mSessionId, str, 0, (Message) null);
        return 0;
    }

    public int cancelTransferCall(int i) {
        Log.i(LOG_TAG, "cancelTransferCall: sessionId " + i);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "cancelTransferCall: session not found.");
            return -1;
        }
        callBySession.mUa.cancelTransferCall(callBySession.mSessionId, (Message) null);
        return 0;
    }

    public int pullingCall(int i, String str, String str2, String str3, Dialog dialog, List<String> list) {
        Dialog dialog2 = dialog;
        StringBuilder sb = new StringBuilder();
        sb.append("pullingCall: regId=");
        int i2 = i;
        sb.append(i);
        sb.append(" taruri ");
        sb.append(IMSLog.checker(str));
        sb.append(" msisdn ");
        sb.append(IMSLog.checker(str2));
        sb.append(" targetDialog ");
        sb.append(IMSLog.checker(dialog2 + ""));
        Log.i(LOG_TAG, sb.toString());
        UserAgent uaByRegId = getUaByRegId(i);
        if (uaByRegId == null) {
            Log.e(LOG_TAG, "pullingCall: UserAgent not found.");
            return -1;
        }
        ImsUri parse = ImsUri.parse(str);
        if (parse == null) {
            Log.e(LOG_TAG, "Pulling Uri is wrong");
            return -1;
        }
        String mnoName = uaByRegId.getImsProfile().getMnoName();
        Log.i(LOG_TAG, "targetDialog.getCallType()= " + dialog.getCallType() + ", mno=" + mnoName + ", " + dialog.isVideoPortZero() + ", " + dialog.isPullAvailable());
        if (mnoName.contains("VZW") && dialog.isVideoPortZero() && dialog.isPullAvailable() && dialog.getCallType() == 1) {
            dialog2.setCallType(2);
            Log.i(LOG_TAG, "recover call type= " + dialog.getCallType());
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String str4 = str2;
        Call call = new Call(uaByRegId, parse, str2);
        call.mCallType = convertToCallTypeForward(dialog.getCallType());
        CallParams callParams = new CallParams();
        callParams.setAudioCodec("AMR-WB");
        call.mParam = callParams;
        call.mLock = countDownLatch;
        uaByRegId.pullingCall(parse.toString(), parse.toString(), str3, dialog, list, obtainMessage(6, call));
        try {
            if (!countDownLatch.await(30000, TimeUnit.MILLISECONDS)) {
                Log.e(LOG_TAG, "pullingCall: timeout.");
                return -1;
            }
            CallResponse callResponse = call.mResponse;
            if (callResponse == null || callResponse.result() == 0) {
                return call.mSessionId;
            }
            Log.i(LOG_TAG, "pullingCall: call failed. reason " + call.mResponse.reason());
            return -1;
        } catch (InterruptedException unused) {
            return -1;
        }
    }

    public int publishDialog(int i, String str, String str2, String str3, int i2, boolean z) {
        Log.i(LOG_TAG, "publishDialog: regId=" + i);
        UserAgent uaByRegId = getUaByRegId(i);
        if (uaByRegId == null) {
            Log.e(LOG_TAG, "publishDialog: UserAgent not found.");
            return -1;
        }
        ImsUri parse = ImsUri.parse(str);
        if (parse == null) {
            Log.e(LOG_TAG, "publishUri Uri is wrong");
            return -1;
        }
        uaByRegId.publishDialog(parse.toString(), str2, str3, i2, (Message) null, z);
        return 0;
    }

    public int setTtyMode(int i, int i2, int i3) {
        int[] iArr = this.mTtyMode;
        if (iArr[i] != i3) {
            iArr[i] = i3;
            StackIF stackIF = this.mStackIf;
            boolean z = true;
            boolean z2 = (i3 == Extensions.TelecomManager.TTY_MODE_OFF || i3 == Extensions.TelecomManager.RTT_MODE) ? false : true;
            if (this.mRttMode[i] != Extensions.TelecomManager.RTT_MODE) {
                z = false;
            }
            stackIF.configCall(i, z2, z, this.mAutomaticMode[i]);
            UserAgent ua = getUa(i, "mmtel");
            if (ua == null) {
                ua = getUa(i, "mmtel-video");
            }
            if (ua != null && ua.getImsProfile().getTtyType() == 4) {
                int i4 = i3 == Extensions.TelecomManager.RTT_MODE ? 3 : 2;
                Log.i(LOG_TAG, "TTY mode " + i3 + " convert to TextMode " + i4);
                this.mStackIf.setTextMode(i, i4);
            }
        }
        return 0;
    }

    public void setAutomaticMode(int i, boolean z) {
        this.mAutomaticMode[i] = z;
        StackIF stackIF = this.mStackIf;
        int i2 = this.mTtyMode[i];
        boolean z2 = true;
        boolean z3 = (i2 == Extensions.TelecomManager.TTY_MODE_OFF || i2 == Extensions.TelecomManager.RTT_MODE) ? false : true;
        if (this.mRttMode[i] != Extensions.TelecomManager.RTT_MODE) {
            z2 = false;
        }
        stackIF.configCall(i, z3, z2, z);
    }

    public void setOutOfService(boolean z, int i) {
        try {
            IMSLog.i(LOG_TAG, i, "setOutOfService() : " + z);
            this.mOutOfService[i] = z;
        } catch (ArrayIndexOutOfBoundsException unused) {
            IMSLog.e(LOG_TAG, i, "setOutOfService: mOutOfService out of bound");
        }
    }

    private boolean isOutOfService(int i) {
        try {
            IMSLog.i(LOG_TAG, i, "isOutOfService() : " + this.mOutOfService[i]);
            return this.mOutOfService[i];
        } catch (ArrayIndexOutOfBoundsException unused) {
            IMSLog.e(LOG_TAG, i, "isOutOfService: mOutOfService out of bound");
            return false;
        }
    }

    public void setRttMode(int i, int i2) {
        int[] iArr = this.mRttMode;
        if (iArr[i] != i2) {
            iArr[i] = i2;
            StackIF stackIF = this.mStackIf;
            int i3 = this.mTtyMode[i];
            int i4 = 1;
            stackIF.configCall(i, (i3 == Extensions.TelecomManager.TTY_MODE_OFF || i3 == Extensions.TelecomManager.RTT_MODE) ? false : true, i2 == Extensions.TelecomManager.RTT_MODE, this.mAutomaticMode[i]);
            UserAgent ua = getUa(i, "mmtel");
            if (ua == null) {
                ua = getUa(i, "mmtel-video");
            }
            if (ua != null) {
                if (ua.getImsProfile().getTtyType() != 4) {
                    if (ua.getImsProfile().getTtyType() == 3) {
                        if (i2 != Extensions.TelecomManager.RTT_MODE) {
                            i4 = 0;
                        }
                    }
                    this.mStackIf.setTextMode(i, i4);
                    Log.i(LOG_TAG, "RTT mode " + i2 + " convert to TextMode " + i4);
                } else if (!(i2 == Extensions.TelecomManager.RTT_MODE || i2 == Extensions.TelecomManager.RTT_MODE_OFF)) {
                    i4 = 2;
                    this.mStackIf.setTextMode(i, i4);
                    Log.i(LOG_TAG, "RTT mode " + i2 + " convert to TextMode " + i4);
                }
                i4 = 3;
                this.mStackIf.setTextMode(i, i4);
                Log.i(LOG_TAG, "RTT mode " + i2 + " convert to TextMode " + i4);
            }
        }
    }

    public void updateAudioInterface(int i, String str) {
        updateAudioInterface(i, str, (UserAgent) null);
    }

    public void updateAudioInterface(int i, String str, UserAgent userAgent) {
        UserAgent uaByRegId = getUaByRegId(i);
        if (uaByRegId != null) {
            userAgent = uaByRegId;
        } else if (userAgent == null) {
            Log.e(LOG_TAG, "Not Registered Volte Services");
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        userAgent.updateAudioInterface(str, this.mAudioInterfaceHandler.obtainMessage(8, countDownLatch));
        try {
            if (!countDownLatch.await(2500, TimeUnit.MILLISECONDS)) {
                Log.e(LOG_TAG, "updateAudioInterface: timeout.");
            }
        } catch (InterruptedException unused) {
        }
    }

    public void setVideoCrtAudio(int i, boolean z) {
        Log.i(LOG_TAG, "setVideoCrtAudio(): sessionId = " + i + ", on = " + z);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "setVideoCrtAudio(): session not found.");
        } else {
            callBySession.mUa.setVideoCrtAudio(callBySession.mSessionId, z);
        }
    }

    public void sendDtmfEvent(int i, String str) {
        Log.i(LOG_TAG, "sendDtmfEvent(): sessionId = " + i + ", dtmfEvent = " + str);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendDtmfEvent(): session not found.");
        } else {
            callBySession.mUa.sendDtmfEvent(callBySession.mSessionId, str);
        }
    }

    public int sendInfo(int i, int i2, String str, int i3) {
        Log.i(LOG_TAG, "sendInfo: " + str);
        Call callBySession = getCallBySession(i);
        int convertToCallTypeForward = convertToCallTypeForward(i2);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendInfo: session not found.");
            return -1;
        }
        UserAgent userAgent = callBySession.mUa;
        userAgent.sendInfo(i, convertToCallTypeForward, i3, createUssdContents(userAgent.getPhoneId(), str, i3), obtainMessage(7));
        return 0;
    }

    public int sendEmergencyLocationPublish(int i) {
        Log.i(LOG_TAG, "sendEmergencyLocationPublish: sessionid=" + i);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendEmergencyLocationPublish: session not found.");
            return -1;
        }
        callBySession.mUa.sendEmergencyLocationPublish(i, (Message) null);
        return 0;
    }

    public int sendCmcInfo(int i, Bundle bundle) {
        Log.i(LOG_TAG, "sendCmcInfo");
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendCmcInfo: session not found.");
            return -1;
        }
        int i2 = bundle.getInt("record_event");
        int i3 = bundle.getInt("extra");
        String string = bundle.getString("sip_call_id");
        UserAgent userAgent = callBySession.mUa;
        userAgent.sendCmcInfo(i, createCmcInfoContents(userAgent.getPhoneId(), i2, i3, string));
        return 0;
    }

    public int sendVcsInfo(int i, Bundle bundle) {
        int i2;
        Log.i(LOG_TAG, "sendVcsInfo");
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendVcsInfo: session not found.");
            return -1;
        }
        String string = bundle.getString("com.samsung.telephony.extra.ims.VCS_ACTION");
        int i3 = bundle.getInt("com.samsung.telephony.extra.ims.VCS_X_POS");
        int i4 = bundle.getInt("com.samsung.telephony.extra.ims.VCS_Y_POS");
        int i5 = bundle.getInt("com.samsung.telephony.extra.ims.VCS_DURATION");
        if ("slide".equals(string)) {
            String string2 = bundle.getString("com.samsung.telephony.extra.ims.VCS_SLIDING_STAGE");
            if (TextUtils.isEmpty(string2) || convertSlidingStage(string2) == 0) {
                Log.e(LOG_TAG, "sendVcsInfo: slidingStage is invalid");
                return -1;
            }
            i2 = convertSlidingStage(string2);
        } else {
            i2 = 0;
        }
        String string3 = bundle.getString("com.samsung.telephony.extra.ims.VCS_TIMESTAMP");
        Log.i(LOG_TAG, "sendVcsInfo event:" + string + ", x:" + i3 + ", y:" + i4 + ", duration:" + i5 + ", slidingStage:" + i2 + ", timestamp:" + string3);
        callBySession.mUa.sendVcsInfo(i, createVcsInfoContents(string, i3, i4, i5, i2, string3));
        return 0;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int convertSlidingStage(java.lang.String r3) {
        /*
            r2 = this;
            r3.hashCode()
            int r2 = r3.hashCode()
            r0 = 0
            r1 = -1
            switch(r2) {
                case 100571: goto L_0x0026;
                case 111267: goto L_0x001a;
                case 109757538: goto L_0x000e;
                default: goto L_0x000c;
            }
        L_0x000c:
            r2 = r1
            goto L_0x0030
        L_0x000e:
            java.lang.String r2 = "start"
            boolean r2 = r3.equals(r2)
            if (r2 != 0) goto L_0x0018
            goto L_0x000c
        L_0x0018:
            r2 = 2
            goto L_0x0030
        L_0x001a:
            java.lang.String r2 = "pre"
            boolean r2 = r3.equals(r2)
            if (r2 != 0) goto L_0x0024
            goto L_0x000c
        L_0x0024:
            r2 = 1
            goto L_0x0030
        L_0x0026:
            java.lang.String r2 = "end"
            boolean r2 = r3.equals(r2)
            if (r2 != 0) goto L_0x002f
            goto L_0x000c
        L_0x002f:
            r2 = r0
        L_0x0030:
            switch(r2) {
                case 0: goto L_0x004d;
                case 1: goto L_0x004b;
                case 2: goto L_0x004a;
                default: goto L_0x0033;
            }
        L_0x0033:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r1 = "convertSlidingStage: invalid stage "
            r2.append(r1)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "ResipVolteHandler"
            android.util.Log.e(r3, r2)
            return r0
        L_0x004a:
            return r1
        L_0x004b:
            r2 = -2
            return r2
        L_0x004d:
            r2 = -3
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.convertSlidingStage(java.lang.String):int");
    }

    public int enableQuantumSecurityService(int i, boolean z) {
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "enableQuantumSecurityService: session not found.");
            return -1;
        }
        callBySession.mUa.enableQuantumSecurityService(i, z);
        return 0;
    }

    public int setQuantumSecurityInfo(int i, Bundle bundle) {
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "setQuantumSecurityInfo: session not found.");
            return -1;
        }
        callBySession.mUa.setQuantumSecurityInfo(i, bundle.getInt("call_direction"), bundle.getInt("crypto_mode"), bundle.getString("qt_session_id"), bundle.getString("session_key"));
        return 0;
    }

    public int startVideoEarlyMedia(int i) {
        Log.i(LOG_TAG, "startVideoEarlyMedia(): sessionId " + i);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "startVideoEarlyMedia(): session not found.");
            return -1;
        }
        callBySession.mUa.startVideoEarlyMedia(callBySession.mSessionId);
        return 0;
    }

    public void updateScreenOnOff(int i, int i2) {
        this.mStackIf.updateScreenOnOff(i, i2);
    }

    public void updateXqEnable(int i, boolean z) {
        this.mStackIf.updateXqEnable(i, z);
    }

    public int handleCmcCsfb(int i) {
        Log.i(LOG_TAG, "handleCmcCsfb(): sessionId " + i);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "handleCmcCsfb(): session not found.");
            return -1;
        }
        callBySession.mUa.handleCmcCsfb(callBySession.mSessionId);
        return 0;
    }

    public void replaceSipCallId(int i, String str) {
        Log.i(LOG_TAG, "replaceSipCallId(): sessionId " + i + ", callId " + str);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "replaceSipCallId(): session not found.");
        } else {
            callBySession.mParam.setSipCallId(str);
        }
    }

    public void replaceUserAgent(int i, int i2) {
        Call callBySession = getCallBySession(i);
        Call callBySession2 = getCallBySession(i2);
        if (callBySession == null || callBySession2 == null) {
            Log.i(LOG_TAG, "call not found with session id " + i2);
            return;
        }
        callBySession.mUa = callBySession2.mUa;
        Log.i(LOG_TAG, "session(" + i + ") ProfileHandle changed to " + callBySession.mUa.getHandle());
    }

    public void clearAllCallInternal(int i) {
        this.mStackIf.clearAllCallInternal(i);
    }

    public void updateNrSaModeOnStart(int i) {
        Log.i(LOG_TAG, "updateNrSaModeOnStart: sessionId=" + i);
        this.mStackIf.updateNrSaModeOnStart(i);
    }

    public void sendNegotiatedLocalSdp(int i, String str) {
        Log.i(LOG_TAG, "sendNegotiatedLocalSdp(): sessionId " + i);
        Call callBySession = getCallBySession(i);
        if (callBySession == null) {
            Log.e(LOG_TAG, "sendNegotiatedLocalSdp(): session not found.");
        } else {
            callBySession.mUa.sendNegotiatedLocalSdp(i, str);
        }
    }

    private UserAgent getUa(int i, String str) {
        return getUa(i, str, 0);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUa(int i, String str, int i2) {
        IUserAgent[] userAgentByPhoneId = this.mImsFramework.getRegistrationManager().getUserAgentByPhoneId(i, str);
        if (userAgentByPhoneId.length == 0) {
            return null;
        }
        for (IUserAgent iUserAgent : userAgentByPhoneId) {
            if (iUserAgent != null && iUserAgent.getImsProfile().getCmcType() == i2) {
                return (UserAgent) iUserAgent;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public UserAgent getUaByRegId(int i) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByRegId(i);
    }

    /* access modifiers changed from: protected */
    public UserAgent getEmergencyUa(int i) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentOnPdn(15, i);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUa(int i) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(i);
    }

    private void notifyUssdEvent(Call call, UssdEvent.USSD_STATE ussd_state, CallStatus callStatus) {
        Log.i(LOG_TAG, "notifyUssdEvent() session: " + call.mSessionId);
        UssdEvent ussdEvent = new UssdEvent();
        ussdEvent.setSessionID(call.mSessionId);
        Mno fromName = Mno.fromName(call.mUa.getImsProfile().getMnoName());
        ussdEvent.setState(ussd_state);
        if (ussd_state == UssdEvent.USSD_STATE.USSD_RESPONSE) {
            ussdEvent.setErrorCode(new SipError((int) callStatus.statusCode(), callStatus.reasonPhrase()));
        } else if (ussd_state == UssdEvent.USSD_STATE.USSD_INDICATION) {
            if (!(callStatus == null || callStatus.additionalContents() == null)) {
                if (USSD_MIME_TYPE.equals(callStatus.additionalContents().mimeType()) && !TextUtils.isEmpty(callStatus.additionalContents().contents())) {
                    try {
                        UssdReceived r5 = UssdXmlParser.getInstance().parseUssdXml(callStatus.additionalContents().contents());
                        ussdEvent.setData((Object) r5.mString.getBytes("UTF-8"));
                        if (callStatus.state() == 11) {
                            ussdEvent.setStatus(3);
                            if (r5.hasErrorCode && fromName != Mno.DOCOMO) {
                                Log.i(LOG_TAG, "BYE from NW has <error-code>");
                                ussdEvent.setData((Object) null);
                            }
                        } else {
                            ussdEvent.setStatus(r5.getVolteConstantsUssdStatus());
                        }
                        ussdEvent.setDCS(148);
                    } catch (UnsupportedEncodingException | XPathExpressionException e) {
                        Log.e(LOG_TAG, "notifyCallStatus: error parsing USSD xml", e);
                    }
                } else if (USSD_INDI_BY_MESSAGE_MIME_TYPE.equals(callStatus.additionalContents().mimeType())) {
                    int rawContentsLength = callStatus.additionalContents().rawContentsLength();
                    byte[] bArr = new byte[rawContentsLength];
                    for (int i = 0; i < rawContentsLength; i++) {
                        bArr[i] = (byte) callStatus.additionalContents().rawContents(i);
                    }
                    if (rawContentsLength > 1 && bArr[rawContentsLength - 1] == 0) {
                        Log.i(LOG_TAG, "Remove invalid last byte (0x00)");
                        rawContentsLength--;
                    }
                    byte[] bArr2 = new byte[rawContentsLength];
                    System.arraycopy(bArr, 0, bArr2, 0, rawContentsLength);
                    ussdEvent.setData((Object) bArr2);
                    if (callStatus.state() == 11) {
                        ussdEvent.setStatus(3);
                    } else {
                        ussdEvent.setStatus(1);
                    }
                    ussdEvent.setDCS(0);
                }
            }
            if (ussdEvent.getData() == null) {
                ussdEvent.setData((Object) new byte[0]);
                ussdEvent.setStatus(3);
            }
        }
        if (callStatus == null || !ImsCallUtil.isCSFBbySIPErrorCode((int) callStatus.statusCode()) || ussd_state == UssdEvent.USSD_STATE.USSD_RESPONSE) {
            this.mUssdEventRegistrants.notifyResult(ussdEvent);
            return;
        }
        UssdEvent ussdEvent2 = new UssdEvent();
        ussdEvent2.setSessionID(call.mSessionId);
        ussdEvent2.setState(UssdEvent.USSD_STATE.USSD_ERROR);
        ussdEvent2.setErrorCode(new SipError((int) callStatus.statusCode(), callStatus.reasonPhrase()));
        this.mUssdEventRegistrants.notifyResult(ussdEvent2);
    }

    private void notifyIncomingCall(Call call, CallStatus callStatus) {
        boolean z;
        int i;
        int i2;
        if (call == null) {
            Log.i(LOG_TAG, "notifyIncomingCall : incoming call instance is null!!");
            return;
        }
        boolean z2 = true;
        int convertToCallTypeBackward = callStatus != null ? convertToCallTypeBackward(callStatus.callType()) : 1;
        int i3 = 0;
        if (callStatus != null) {
            if (!callStatus.remoteVideoCapa() || !getLocalVideoCapa(call)) {
                z2 = false;
            }
            i2 = (int) callStatus.width();
            i = (int) callStatus.height();
            Mno fromName = Mno.fromName(call.mUa.getImsProfile().getMnoName());
            if (callStatus.isFocus() && (fromName == Mno.SKT || fromName == Mno.KT || fromName == Mno.LGU || fromName == Mno.KDDI)) {
                call.mParam.setIsFocus("1");
            }
            CallParams callParams = call.mParam;
            if (!callStatus.cvoEnabled()) {
                i3 = -1;
            }
            callParams.setVideoOrientation(i3);
            if ((call.mUa.getUaProfile().getVideoCrbtSupportType() & 2) == 2) {
                call.mParam.setDelayRinging(callStatus.delayRinging());
            }
            z = z2;
        } else {
            i2 = NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE;
            i = 640;
            z = false;
        }
        call.mParam.setVideoWidth(i2);
        call.mParam.setVideoHeight(i);
        IncomingCallEvent incomingCallEvent = new IncomingCallEvent(call.mUa.getImsRegistration(), call.mSessionId, convertToCallTypeBackward, call.mPeer, false, z, "", call.mParam);
        Log.i(LOG_TAG, "notifyIncomingCall() session: " + call.mSessionId + ", callType: " + convertToCallTypeBackward);
        this.mIncomingCallEventRegistrants.notifyResult(incomingCallEvent);
    }

    private boolean getLocalVideoCapa(Call call) {
        ImsRegistration imsRegistration;
        if (call == null || (imsRegistration = call.mUa.getImsRegistration()) == null) {
            return false;
        }
        return imsRegistration.hasService("mmtel-video");
    }

    /* JADX WARNING: Removed duplicated region for block: B:271:0x0504  */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x0506  */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x0573  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x057b  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x05c9  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x05f8 A[Catch:{ XPathExpressionException -> 0x0715 }] */
    /* JADX WARNING: Removed duplicated region for block: B:293:0x0613 A[Catch:{ XPathExpressionException -> 0x0715 }] */
    /* JADX WARNING: Removed duplicated region for block: B:341:0x071f  */
    /* JADX WARNING: Removed duplicated region for block: B:343:0x072c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyCallStatus(com.sec.internal.ims.core.handler.secims.ResipVolteHandler.Call r43, com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE r44, com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus r45, int r46) {
        /*
            r42 = this;
            r1 = r42
            r0 = r43
            r2 = r44
            r3 = r46
            if (r45 == 0) goto L_0x0013
            int r5 = r45.callType()
            int r5 = r1.convertToCallTypeBackward(r5)
            goto L_0x0014
        L_0x0013:
            r5 = 1
        L_0x0014:
            if (r45 == 0) goto L_0x001c
            long r7 = r45.statusCode()
            int r7 = (int) r7
            goto L_0x001d
        L_0x001c:
            r7 = 0
        L_0x001d:
            java.lang.String r8 = ""
            if (r45 == 0) goto L_0x0026
            java.lang.String r9 = r45.reasonPhrase()
            goto L_0x0027
        L_0x0026:
            r9 = r8
        L_0x0027:
            if (r45 == 0) goto L_0x0031
            boolean r10 = r45.remoteVideoCapa()
            if (r10 == 0) goto L_0x0031
            r10 = 1
            goto L_0x0032
        L_0x0031:
            r10 = 0
        L_0x0032:
            if (r45 == 0) goto L_0x003a
            long r11 = r45.width()
            int r11 = (int) r11
            goto L_0x003c
        L_0x003a:
            r11 = 480(0x1e0, float:6.73E-43)
        L_0x003c:
            if (r45 == 0) goto L_0x0044
            long r12 = r45.height()
            int r12 = (int) r12
            goto L_0x0046
        L_0x0044:
            r12 = 640(0x280, float:8.97E-43)
        L_0x0046:
            if (r45 == 0) goto L_0x004d
            java.lang.String r14 = r45.conferenceSupport()
            goto L_0x004e
        L_0x004d:
            r14 = 0
        L_0x004e:
            if (r45 == 0) goto L_0x0058
            boolean r15 = r45.isFocus()
            if (r15 == 0) goto L_0x0058
            r15 = 1
            goto L_0x0059
        L_0x0058:
            r15 = 0
        L_0x0059:
            if (r10 == 0) goto L_0x0064
            boolean r16 = r42.getLocalVideoCapa(r43)
            if (r16 == 0) goto L_0x0064
            r16 = 1
            goto L_0x0066
        L_0x0064:
            r16 = 0
        L_0x0066:
            r18 = r14
            if (r45 == 0) goto L_0x0070
            long r13 = r45.localVideoRtpPort()
            int r13 = (int) r13
            goto L_0x0071
        L_0x0070:
            r13 = 0
        L_0x0071:
            r19 = r7
            if (r45 == 0) goto L_0x007b
            long r6 = r45.localVideoRtcpPort()
            int r6 = (int) r6
            goto L_0x007c
        L_0x007b:
            r6 = 0
        L_0x007c:
            r7 = r15
            if (r45 == 0) goto L_0x0086
            long r14 = r45.remoteVideoRtpPort()
            int r14 = (int) r14
            r15 = r14
            goto L_0x0087
        L_0x0086:
            r15 = 0
        L_0x0087:
            r21 = r5
            if (r45 == 0) goto L_0x0092
            long r4 = r45.remoteVideoRtcpPort()
            int r14 = (int) r4
            r4 = r14
            goto L_0x0093
        L_0x0092:
            r4 = 0
        L_0x0093:
            if (r45 == 0) goto L_0x009a
            java.lang.String r5 = r45.serviceUrn()
            goto L_0x009b
        L_0x009a:
            r5 = 0
        L_0x009b:
            r22 = r4
            if (r45 == 0) goto L_0x00a6
            long r3 = r45.retryAfter()
            int r14 = (int) r3
            r3 = r14
            goto L_0x00a7
        L_0x00a6:
            r3 = 0
        L_0x00a7:
            if (r45 == 0) goto L_0x00b2
            boolean r4 = r45.localHoldTone()
            if (r4 == 0) goto L_0x00b0
            goto L_0x00b2
        L_0x00b0:
            r4 = 0
            goto L_0x00b3
        L_0x00b2:
            r4 = 1
        L_0x00b3:
            if (r45 == 0) goto L_0x00ba
            java.lang.String r14 = r45.historyInfo()
            goto L_0x00bb
        L_0x00ba:
            r14 = r8
        L_0x00bb:
            if (r45 == 0) goto L_0x00c8
            java.lang.String r23 = r45.dtmfEvent()
            r41 = r23
            r23 = r8
            r8 = r41
            goto L_0x00ca
        L_0x00c8:
            r23 = r8
        L_0x00ca:
            if (r45 == 0) goto L_0x00d5
            boolean r24 = r45.cvoEnabled()
            if (r24 == 0) goto L_0x00d3
            goto L_0x00d5
        L_0x00d3:
            r1 = 0
            goto L_0x00d6
        L_0x00d5:
            r1 = 1
        L_0x00d6:
            if (r45 == 0) goto L_0x00df
            java.lang.String r24 = r45.alertInfo()
            r25 = r24
            goto L_0x00e1
        L_0x00df:
            r25 = 0
        L_0x00e1:
            r24 = r3
            r26 = r4
            if (r45 == 0) goto L_0x00ed
            long r3 = r45.videoCrbtType()
            int r3 = (int) r3
            goto L_0x00ee
        L_0x00ed:
            r3 = 0
        L_0x00ee:
            if (r45 == 0) goto L_0x00f5
            java.lang.String r4 = r45.cmcDeviceId()
            goto L_0x00f7
        L_0x00f5:
            r4 = r23
        L_0x00f7:
            r27 = r3
            r28 = r4
            if (r45 == 0) goto L_0x0103
            long r3 = r45.audioRxTrackId()
            int r3 = (int) r3
            goto L_0x0104
        L_0x0103:
            r3 = 0
        L_0x0104:
            if (r45 == 0) goto L_0x010b
            java.lang.String r4 = r45.audioBitRate()
            goto L_0x010d
        L_0x010b:
            r4 = r23
        L_0x010d:
            if (r45 == 0) goto L_0x0116
            java.lang.String r29 = r45.cmcCallTime()
            r30 = r29
            goto L_0x0118
        L_0x0116:
            r30 = r23
        L_0x0118:
            if (r45 == 0) goto L_0x0121
            java.lang.String r17 = r45.featureCaps()
            r31 = r17
            goto L_0x0123
        L_0x0121:
            r31 = 0
        L_0x0123:
            r17 = r3
            r29 = r4
            if (r45 == 0) goto L_0x012f
            long r3 = r45.audioEarlyMediaDir()
            int r3 = (int) r3
            goto L_0x0130
        L_0x012f:
            r3 = 0
        L_0x0130:
            if (r45 == 0) goto L_0x013b
            boolean r4 = r45.delayRinging()
            if (r4 == 0) goto L_0x013b
            r32 = 1
            goto L_0x013d
        L_0x013b:
            r32 = 0
        L_0x013d:
            com.sec.internal.ims.core.handler.secims.UserAgent r4 = r0.mUa
            if (r4 == 0) goto L_0x014e
            com.sec.ims.settings.ImsProfile r4 = r4.getImsProfile()
            java.lang.String r4 = r4.getMnoName()
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.fromName(r4)
            goto L_0x0150
        L_0x014e:
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.DEFAULT
        L_0x0150:
            boolean r33 = android.text.TextUtils.isEmpty(r5)
            if (r33 == 0) goto L_0x0159
            r34 = r5
            goto L_0x0161
        L_0x0159:
            java.lang.String r33 = com.sec.internal.log.IMSLog.checker(r5)
            r34 = r5
            r5 = r33
        L_0x0161:
            if (r45 == 0) goto L_0x016c
            java.lang.String r33 = r45.idcExtra()
            r35 = r33
            r33 = r4
            goto L_0x0170
        L_0x016c:
            r33 = r4
            r35 = r23
        L_0x0170:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r36 = r3
            java.lang.String r3 = "notifyCallStatus() session: "
            r4.append(r3)
            int r3 = r0.mSessionId
            r4.append(r3)
            java.lang.String r3 = ", callstate: "
            r4.append(r3)
            r4.append(r2)
            java.lang.String r3 = ", callType: "
            r4.append(r3)
            r3 = r21
            r4.append(r3)
            java.lang.String r2 = ", statusCode: "
            r4.append(r2)
            r2 = r19
            r4.append(r2)
            java.lang.String r2 = ", reasonPhrase: "
            r4.append(r2)
            r4.append(r9)
            java.lang.String r2 = ", remoteVideoCapa: "
            r4.append(r2)
            r4.append(r10)
            java.lang.String r2 = ", localVideoCapa: "
            r4.append(r2)
            boolean r2 = r42.getLocalVideoCapa(r43)
            r4.append(r2)
            java.lang.String r2 = ", width: "
            r4.append(r2)
            r4.append(r11)
            java.lang.String r2 = ", height: "
            r4.append(r2)
            r4.append(r12)
            java.lang.String r2 = ", isFocus: "
            r4.append(r2)
            r4.append(r7)
            java.lang.String r2 = ", conferenceSupport: "
            r4.append(r2)
            r2 = r18
            r4.append(r2)
            java.lang.String r10 = ", localVideoRtpPort: "
            r4.append(r10)
            r4.append(r13)
            java.lang.String r10 = ", localVideoRtcpPort: "
            r4.append(r10)
            r4.append(r6)
            java.lang.String r10 = ", RemoteVideoRtpPort: "
            r4.append(r10)
            r4.append(r15)
            java.lang.String r10 = ", RemoteVideoRtcpPort: "
            r4.append(r10)
            r10 = r22
            r4.append(r10)
            r18 = r12
            java.lang.String r12 = ", ServiceUrn: "
            r4.append(r12)
            r4.append(r5)
            java.lang.String r5 = ", retryAfter: "
            r4.append(r5)
            r5 = r24
            r4.append(r5)
            java.lang.String r12 = ", historyInfo: "
            r4.append(r12)
            r4.append(r14)
            java.lang.String r12 = ", dtmfEvent: "
            r4.append(r12)
            r4.append(r8)
            java.lang.String r12 = ", holdTone: "
            r4.append(r12)
            r12 = r26
            r4.append(r12)
            r21 = r14
            java.lang.String r14 = ", cvoEnabled : "
            r4.append(r14)
            r4.append(r1)
            java.lang.String r14 = ", AlertInfo : "
            r4.append(r14)
            r14 = r25
            r4.append(r14)
            r22 = r1
            java.lang.String r1 = ", videoCrbtType : "
            r4.append(r1)
            r1 = r27
            r4.append(r1)
            r24 = r14
            java.lang.String r14 = ", cmcDeviceId : "
            r4.append(r14)
            r14 = r28
            r4.append(r14)
            java.lang.String r14 = ", audioRxTrackId : "
            r4.append(r14)
            r14 = r17
            r4.append(r14)
            java.lang.String r14 = ", audioBitRate : "
            r4.append(r14)
            r14 = r29
            r4.append(r14)
            java.lang.String r14 = ", cmcCallTime : "
            r4.append(r14)
            r14 = r30
            r4.append(r14)
            r25 = r14
            java.lang.String r14 = ", featureCaps: "
            r4.append(r14)
            r14 = r31
            r4.append(r14)
            r26 = r14
            java.lang.String r14 = ", audioEarlyMediaDir: "
            r4.append(r14)
            r14 = r36
            r4.append(r14)
            java.lang.String r14 = ", delayRinging: "
            r4.append(r14)
            r14 = r32
            r4.append(r14)
            r27 = r14
            java.lang.String r14 = ", idcExtra: "
            r4.append(r14)
            r14 = r35
            r4.append(r14)
            java.lang.String r4 = r4.toString()
            r30 = r14
            java.lang.String r14 = "ResipVolteHandler"
            android.util.Log.i(r14, r4)
            if (r2 == 0) goto L_0x02b5
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r4.setConferenceSupported(r2)
        L_0x02b5:
            if (r7 == 0) goto L_0x0317
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            java.lang.String r7 = "1"
            r4.setIsFocus(r7)
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.VZW
            r7 = r33
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TELSTRA
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.KDDI
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.VODAFONE_EG
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.SKT
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.KT
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.LGU
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.RJIL
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.PROXIMUS
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TELENOR_NORWAY
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.AIRTEL
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.ZAIN_KSA
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.MTN_SOUTHAFRICA
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.ETISALAT_EG
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.DIGI_HUNGARY
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.XL_ID
            if (r7 == r4) goto L_0x0314
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TMOUS
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.DISH
            com.sec.internal.constants.Mno[] r2 = new com.sec.internal.constants.Mno[]{r4, r2}
            boolean r2 = r7.isOneOf(r2)
            if (r2 == 0) goto L_0x0319
            r2 = 1
            if (r3 == r2) goto L_0x0314
            r2 = 5
            if (r3 != r2) goto L_0x0319
        L_0x0314:
            r16 = 0
            goto L_0x0319
        L_0x0317:
            r7 = r33
        L_0x0319:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.TELSTRA
            if (r7 == r2) goto L_0x035c
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.TELENOR_SWE
            if (r7 == r2) goto L_0x035c
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.VODAFONE_EG
            if (r7 == r2) goto L_0x035c
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.VODAFONE_AUSTRALIA
            if (r7 == r2) goto L_0x035c
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.RJIL
            if (r7 == r2) goto L_0x035c
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.SWISSCOM
            if (r7 == r2) goto L_0x035c
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.WE_EG
            if (r7 == r2) goto L_0x035c
            boolean r2 = r7.isCanada()
            if (r2 != 0) goto L_0x035c
            boolean r2 = r7.isIndia()
            if (r2 != 0) goto L_0x035c
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.XL_ID
            if (r7 == r2) goto L_0x035c
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.TMOUS
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.DISH
            com.sec.internal.constants.Mno[] r2 = new com.sec.internal.constants.Mno[]{r2, r4}
            boolean r2 = r7.isOneOf(r2)
            if (r2 == 0) goto L_0x035a
            r2 = 1
            if (r3 == r2) goto L_0x035c
            r2 = 5
            if (r3 != r2) goto L_0x035a
            goto L_0x035c
        L_0x035a:
            r2 = 0
            goto L_0x035d
        L_0x035c:
            r2 = 1
        L_0x035d:
            boolean r4 = r0.isConference
            if (r4 == 0) goto L_0x0365
            if (r2 == 0) goto L_0x0365
            r16 = 0
        L_0x0365:
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_REMOTE
            r4 = r44
            r31 = r5
            if (r4 != r2) goto L_0x0373
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.ATT
            if (r7 != r5) goto L_0x0373
            r16 = 0
        L_0x0373:
            boolean r5 = r7.isChn()
            r32 = r1
            r1 = 2
            if (r5 != 0) goto L_0x0390
            boolean r5 = r7.isJpn()
            if (r5 != 0) goto L_0x0390
            boolean r5 = r7.isKor()
            if (r5 == 0) goto L_0x0389
            goto L_0x0390
        L_0x0389:
            r1 = r42
            r33 = r11
            r5 = r16
            goto L_0x03c7
        L_0x0390:
            boolean r5 = r7.isKor()
            if (r5 == 0) goto L_0x03a3
            if (r3 != r1) goto L_0x03a3
            r5 = 176(0xb0, float:2.47E-43)
            if (r11 != r5) goto L_0x03a3
            java.lang.String r5 = "force to set modifiable to false for 3G QCIF Video Call"
            android.util.Log.i(r14, r5)
            r5 = 0
            goto L_0x03a5
        L_0x03a3:
            r5 = r16
        L_0x03a5:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r33 = r11
            java.lang.String r11 = "setModifyHeader : "
            r1.append(r11)
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r14, r1)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r1 = r0.mParam
            java.lang.String r11 = java.lang.Boolean.toString(r5)
            r1.setModifyHeader(r11)
            r1 = r42
        L_0x03c7:
            r11 = r22
            boolean r22 = r1.IsModifiableNeedToBeIgnored(r0, r4, r7)
            if (r22 == 0) goto L_0x03d7
            java.lang.String r5 = "force to set modifiable to false"
            android.util.Log.i(r14, r5)
            r22 = 0
            goto L_0x03d9
        L_0x03d7:
            r22 = r5
        L_0x03d9:
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r5 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_LOCAL
            if (r4 == r5) goto L_0x03e3
            if (r4 == r2) goto L_0x03e3
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r5 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_BOTH
            if (r4 != r5) goto L_0x03f2
        L_0x03e3:
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.RJIL
            if (r7 == r5) goto L_0x03eb
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TELSTRA
            if (r7 != r5) goto L_0x03f2
        L_0x03eb:
            java.lang.String r5 = "force to set modifiable to false when call is held!!"
            android.util.Log.i(r14, r5)
            r22 = 0
        L_0x03f2:
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.DOCOMO
            if (r7 != r5) goto L_0x0400
            r5 = 7
            if (r3 != r5) goto L_0x0400
            java.lang.String r5 = "force to set modifiable to true for Docomo"
            android.util.Log.i(r14, r5)
            r5 = 1
            goto L_0x0402
        L_0x0400:
            r5 = r22
        L_0x0402:
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.DOCOMO
            r1 = 709(0x2c5, float:9.94E-43)
            if (r7 == r4) goto L_0x0410
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.KDDI
            if (r7 != r4) goto L_0x040d
            goto L_0x0410
        L_0x040d:
            r4 = r19
            goto L_0x041e
        L_0x0410:
            r4 = r19
            if (r4 != r1) goto L_0x041e
            java.lang.String r1 = "deleteTcpClientSocket() at INVITE FLUSH"
            android.util.Log.i(r14, r1)
            com.sec.internal.ims.core.handler.secims.UserAgent r1 = r0.mUa
            r1.deleteTcpClientSocket()
        L_0x041e:
            boolean r1 = r7.isKor()
            if (r1 == 0) goto L_0x043a
            r1 = 406(0x196, float:5.69E-43)
            if (r4 == r1) goto L_0x0430
            r1 = 408(0x198, float:5.72E-43)
            if (r4 == r1) goto L_0x0430
            r1 = 709(0x2c5, float:9.94E-43)
            if (r4 != r1) goto L_0x043a
        L_0x0430:
            java.lang.String r1 = "deleteTcpClientSocket() at INVITE FLUSH for KOR"
            android.util.Log.i(r14, r1)
            com.sec.internal.ims.core.handler.secims.UserAgent r1 = r0.mUa
            r1.deleteTcpClientSocket()
        L_0x043a:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.TELEFONICA_GERMANY
            if (r7 != r1) goto L_0x0453
            r1 = 5487(0x156f, float:7.689E-42)
            if (r4 != r1) goto L_0x0453
            java.lang.String r1 = "Session Terminated by UE"
            boolean r1 = r1.equals(r9)
            if (r1 == 0) goto L_0x0453
            java.lang.String r1 = "Remote side ends the call normally."
            android.util.Log.i(r14, r1)
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ENDED
            r4 = 0
            goto L_0x0455
        L_0x0453:
            r1 = r44
        L_0x0455:
            boolean r19 = android.text.TextUtils.isEmpty(r9)
            if (r19 != 0) goto L_0x04be
            r19 = r7
            java.lang.String r7 = "Q.850"
            boolean r7 = r9.startsWith(r7)
            if (r7 == 0) goto L_0x04b7
            java.lang.String r7 = "#:"
            java.lang.String[] r7 = r9.split(r7)
            r22 = r5
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r5 = r0.mParam
            r35 = r4
            r20 = 0
            r4 = r7[r20]
            r20 = r2
            r37 = r9
            r39 = r25
            r40 = r27
            r38 = r28
            r2 = r29
            r9 = r36
            r25 = r21
            r21 = r3
            r3 = r24
            r24 = r14
            r14 = r26
            r41 = r17
            r17 = r1
            r1 = r41
            r5.setRejectProtocol(r4)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r5 = 1
            r27 = r7[r5]
            int r5 = java.lang.Integer.parseInt(r27)
            r4.setRejectCode(r5)
            int r4 = r7.length
            r5 = 3
            if (r4 < r5) goto L_0x04af
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r5 = 2
            r5 = r7[r5]
            r4.setRejectText(r5)
            goto L_0x04e2
        L_0x04af:
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r5 = r23
            r4.setRejectText(r5)
            goto L_0x04e2
        L_0x04b7:
            r20 = r2
            r35 = r4
            r22 = r5
            goto L_0x04c6
        L_0x04be:
            r20 = r2
            r35 = r4
            r22 = r5
            r19 = r7
        L_0x04c6:
            r37 = r9
            r39 = r25
            r40 = r27
            r38 = r28
            r2 = r29
            r9 = r36
            r25 = r21
            r21 = r3
            r3 = r24
            r24 = r14
            r14 = r26
            r41 = r17
            r17 = r1
            r1 = r41
        L_0x04e2:
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r4.setLocalVideoRTPPort(r13)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r4.setLocalVideoRTCPPort(r6)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r4.setRemoteVideoRTPPort(r15)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r4.setRemoteVideoRTCPPort(r10)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r4.setLocalHoldTone(r12)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r4.setDtmfEvent(r8)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            if (r11 == 0) goto L_0x0506
            r5 = 0
            goto L_0x0507
        L_0x0506:
            r5 = -1
        L_0x0507:
            r4.setVideoOrientation(r5)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r0.mParam
            r4.setAlertInfo(r3)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r3 = r0.mParam
            r4 = r32
            r3.setVideoCrbtType(r4)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r3 = r0.mParam
            r11 = r33
            r3.setVideoWidth(r11)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r3 = r0.mParam
            r12 = r18
            r3.setVideoHeight(r12)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r3 = r0.mParam
            r3.setAudioRxTrackId(r1)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r1 = r0.mParam
            r1.setAudioBitRate(r2)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r1 = r0.mParam
            r1.setFeatureCaps(r14)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r1 = r0.mParam
            r1.setAudioEarlyMediaDir(r9)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r1 = r0.mParam
            r2 = r40
            r1.setDelayRinging(r2)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r1 = r0.mParam
            java.lang.String r1 = r1.getHistoryInfo()
            if (r1 != 0) goto L_0x0549
            if (r2 != 0) goto L_0x0550
        L_0x0549:
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r1 = r0.mParam
            r14 = r25
            r1.setHistoryInfo(r14)
        L_0x0550:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "setVideoOrientation_resip"
            r1.append(r2)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r2 = r0.mParam
            int r2 = r2.getVideoOrientation()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r2 = r24
            android.util.Log.i(r2, r1)
            r3 = r17
            r1 = r20
            if (r3 != r1) goto L_0x057b
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r1 = r0.mParam
            r4 = 1
            r1.setRemoteHeld(r4)
            r5 = 0
            goto L_0x0582
        L_0x057b:
            r4 = 1
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r1 = r0.mParam
            r5 = 0
            r1.setRemoteHeld(r5)
        L_0x0582:
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent r1 = new com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent
            r1.<init>()
            r6 = r21
            r1.setCallType(r6)
            int r6 = r0.mSessionId
            r1.setSessionID(r6)
            com.sec.ims.util.NameAddr r6 = r0.mPeer
            r1.setPeerAddr(r6)
            r1.setState(r3)
            com.sec.ims.util.SipError r3 = new com.sec.ims.util.SipError
            r6 = r35
            r8 = r37
            r3.<init>(r6, r8)
            r1.setErrorCode(r3)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r3 = r0.mParam
            r1.setParams(r3)
            r3 = r22
            r1.setRemoteVideoCapa(r3)
            r14 = r31
            r1.setRetryAfter(r14)
            boolean r3 = r0.isConference
            r1.setConference(r3)
            r3 = r38
            r1.setCmcDeviceId(r3)
            r3 = r39
            r1.setCmcCallTime(r3)
            boolean r3 = android.text.TextUtils.isEmpty(r30)
            if (r3 != 0) goto L_0x05ce
            r3 = r30
            r1.setIdcExtra(r3)
        L_0x05ce:
            if (r45 == 0) goto L_0x071b
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r3 = r45.additionalContents()
            if (r3 == 0) goto L_0x071b
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r3 = r45.additionalContents()
            java.lang.String r3 = r3.mimeType()
            java.lang.String r6 = "application/3gpp-ims+xml"
            boolean r3 = r6.equals(r3)
            if (r3 == 0) goto L_0x071b
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r3 = r45.additionalContents()     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r3 = r3.contents()     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$AlternativeService r3 = com.sec.internal.ims.core.handler.secims.ResipVolteHandler.AlternativeServiceXmlParser.parseXml(r3)     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r6 = r3.mAction     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.ALTERNATIVE_SERVICE.INITIAL_REGISTRATION     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r6 != r7) goto L_0x0613
            java.lang.String r0 = "initial registration handling required!"
            android.util.Log.i(r2, r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r0 = r3.mAction     // Catch:{ XPathExpressionException -> 0x0715 }
            r1.setAlternativeService(r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r0 = r3.mType     // Catch:{ XPathExpressionException -> 0x0715 }
            r1.setAlternativeServiceType(r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r0 = r3.mReason     // Catch:{ XPathExpressionException -> 0x0715 }
            r1.setAlternativeServiceReason(r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            r7 = r34
            r1.setAlternativeServiceUrn(r7)     // Catch:{ XPathExpressionException -> 0x0715 }
            goto L_0x071b
        L_0x0613:
            r7 = r34
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r8 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY_REGISTRATION     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r6 == r8) goto L_0x0646
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r9 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r6 != r9) goto L_0x061e
            goto L_0x0646
        L_0x061e:
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.CMCC     // Catch:{ XPathExpressionException -> 0x0715 }
            r6 = r19
            if (r6 != r0) goto L_0x071b
            boolean r0 = r7.isEmpty()     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r0 != 0) goto L_0x071b
            java.lang.String r0 = r3.mType     // Catch:{ XPathExpressionException -> 0x0715 }
            boolean r0 = r0.isEmpty()     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r0 != 0) goto L_0x071b
            java.lang.String r0 = "For CMCC emergency call alternative-service handling required!"
            android.util.Log.i(r2, r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r0 = r3.mType     // Catch:{ XPathExpressionException -> 0x0715 }
            r1.setAlternativeServiceType(r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r0 = r3.mReason     // Catch:{ XPathExpressionException -> 0x0715 }
            r1.setAlternativeServiceReason(r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            r1.setAlternativeServiceUrn(r7)     // Catch:{ XPathExpressionException -> 0x0715 }
            goto L_0x071b
        L_0x0646:
            r6 = r19
            com.sec.internal.ims.core.handler.secims.UserAgent r9 = r0.mUa     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.interfaces.ims.core.IPdnController r9 = r9.getPdnController()     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.ims.core.handler.secims.UserAgent r10 = r0.mUa     // Catch:{ XPathExpressionException -> 0x0715 }
            int r10 = r10.getPhoneId()     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.constants.ims.os.EmcBsIndication r9 = r9.getEmcBsIndication(r10)     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.ims.core.handler.secims.UserAgent r10 = r0.mUa     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.ims.settings.ImsProfile r10 = r10.getImsProfile()     // Catch:{ XPathExpressionException -> 0x0715 }
            boolean r10 = r10.getSupport380PolicyByEmcbs()     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r10 == 0) goto L_0x0669
            com.sec.internal.constants.ims.os.EmcBsIndication r10 = com.sec.internal.constants.ims.os.EmcBsIndication.NOT_SUPPORTED     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r9 != r10) goto L_0x0669
            goto L_0x066a
        L_0x0669:
            r4 = r5
        L_0x066a:
            java.lang.String r5 = "urn:service:sos"
            if (r4 == 0) goto L_0x06a5
            boolean r4 = android.text.TextUtils.isEmpty(r7)     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r4 != 0) goto L_0x06a5
            java.lang.String r4 = r7.toLowerCase()     // Catch:{ XPathExpressionException -> 0x0715 }
            boolean r4 = r4.contains(r5)     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r4 == 0) goto L_0x06a5
            java.util.Set<java.lang.String> r4 = mMainSosSubserviceSet     // Catch:{ XPathExpressionException -> 0x0715 }
            boolean r4 = r4.contains(r7)     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r4 != 0) goto L_0x06a5
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ XPathExpressionException -> 0x0715 }
            r0.<init>()     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r3 = "alternative-service handling NOT required! serviceUrn: "
            r0.append(r3)     // Catch:{ XPathExpressionException -> 0x0715 }
            r0.append(r7)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r3 = " eMCBS: "
            r0.append(r3)     // Catch:{ XPathExpressionException -> 0x0715 }
            r0.append(r9)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r0 = r0.toString()     // Catch:{ XPathExpressionException -> 0x0715 }
            android.util.Log.e(r2, r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            goto L_0x071b
        L_0x06a5:
            com.sec.internal.ims.core.handler.secims.UserAgent r0 = r0.mUa     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.ims.settings.ImsProfile r0 = r0.getImsProfile()     // Catch:{ XPathExpressionException -> 0x0715 }
            boolean r0 = r0.getSosUrnRequired()     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r0 == 0) goto L_0x06d6
            boolean r0 = android.text.TextUtils.isEmpty(r7)     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r0 != 0) goto L_0x06c1
            java.lang.String r0 = r7.toLowerCase()     // Catch:{ XPathExpressionException -> 0x0715 }
            boolean r0 = r0.contains(r5)     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r0 != 0) goto L_0x06d6
        L_0x06c1:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ XPathExpressionException -> 0x0715 }
            r0.<init>()     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r3 = "alternative-service handling NOT required!, eMCBS: "
            r0.append(r3)     // Catch:{ XPathExpressionException -> 0x0715 }
            r0.append(r9)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r0 = r0.toString()     // Catch:{ XPathExpressionException -> 0x0715 }
            android.util.Log.e(r2, r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            goto L_0x071b
        L_0x06d6:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ XPathExpressionException -> 0x0715 }
            r0.<init>()     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r4 = "alternative-service handling required!, eMCBS: "
            r0.append(r4)     // Catch:{ XPathExpressionException -> 0x0715 }
            r0.append(r9)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r0 = r0.toString()     // Catch:{ XPathExpressionException -> 0x0715 }
            android.util.Log.e(r2, r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r0 = r3.mAction     // Catch:{ XPathExpressionException -> 0x0715 }
            r1.setAlternativeService(r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r0 = r3.mType     // Catch:{ XPathExpressionException -> 0x0715 }
            r1.setAlternativeServiceType(r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            java.lang.String r0 = r3.mReason     // Catch:{ XPathExpressionException -> 0x0715 }
            r1.setAlternativeServiceReason(r0)     // Catch:{ XPathExpressionException -> 0x0715 }
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.CMCC     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r6 == r0) goto L_0x0703
            boolean r0 = r6.isEur()     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r0 == 0) goto L_0x0711
        L_0x0703:
            boolean r0 = r7.isEmpty()     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r0 == 0) goto L_0x0711
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$ALTERNATIVE_SERVICE r0 = r3.mAction     // Catch:{ XPathExpressionException -> 0x0715 }
            if (r0 != r8) goto L_0x0711
            r1.setAlternativeServiceUrn(r5)     // Catch:{ XPathExpressionException -> 0x0715 }
            goto L_0x071b
        L_0x0711:
            r1.setAlternativeServiceUrn(r7)     // Catch:{ XPathExpressionException -> 0x0715 }
            goto L_0x071b
        L_0x0715:
            r0 = move-exception
            java.lang.String r3 = "notifyCallStatus: error parsing AlternativeService xml"
            android.util.Log.e(r2, r3, r0)
        L_0x071b:
            r2 = r46
            if (r2 <= 0) goto L_0x072c
            r0 = 200(0xc8, float:2.8E-43)
            r3 = r42
            android.os.Message r0 = r3.obtainMessage(r0, r1)
            long r1 = (long) r2
            r3.sendMessageDelayed(r0, r1)
            return
        L_0x072c:
            r3 = r42
            com.sec.internal.helper.RegistrantList r0 = r3.mCallStateEventRegistrants
            r0.notifyResult(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.notifyCallStatus(com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call, com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE, com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus, int):void");
    }

    public static class AlternativeServiceXmlParser {
        public static AlternativeService parseXml(String str) throws XPathExpressionException {
            AlternativeService alternativeService = new AlternativeService();
            Log.i(ResipVolteHandler.LOG_TAG, "AlternativeServiceXmlParser parseXml:" + str);
            XPath newXPath = XPathFactory.newInstance().newXPath();
            XPathExpression compile = newXPath.compile("//ims-3gpp/alternative-service");
            XPathExpression compile2 = newXPath.compile("type");
            XPathExpression compile3 = newXPath.compile("reason");
            XPathExpression compile4 = newXPath.compile("action");
            Node node = (Node) compile.evaluate(new InputSource(new StringReader(str)), XPathConstants.NODE);
            if (node == null) {
                Log.i(ResipVolteHandler.LOG_TAG, "parseXml not found Node '/ims-3gpp/alternative-service");
                return alternativeService;
            }
            String evaluate = compile2.evaluate(node);
            String evaluate2 = compile3.evaluate(node);
            String replace = compile4.evaluate(node).replace("\n", "");
            Log.i(ResipVolteHandler.LOG_TAG, "parseXml:" + evaluate + "," + evaluate2 + "," + replace);
            if ("initial-registration".equals(replace)) {
                Log.i(ResipVolteHandler.LOG_TAG, "initial-registration is found");
                alternativeService.mAction = CallStateEvent.ALTERNATIVE_SERVICE.INITIAL_REGISTRATION;
                alternativeService.mType = evaluate;
                alternativeService.mReason = evaluate2;
            } else if ("emergency-registration".equals(replace)) {
                alternativeService.mAction = CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY_REGISTRATION;
                alternativeService.mType = evaluate;
                alternativeService.mReason = evaluate2;
            } else if ("emergency".equals(evaluate)) {
                alternativeService.mAction = CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY;
                alternativeService.mType = evaluate;
                alternativeService.mReason = evaluate2;
            }
            return alternativeService;
        }
    }

    public static class CmcInfoXmlParser {
        public static CmcInfoEvent parseXml(String str) throws XPathExpressionException {
            CmcInfoEvent cmcInfoEvent = new CmcInfoEvent();
            Log.i(ResipVolteHandler.LOG_TAG, "CmcInfoXmlParser parseXml:" + str);
            XPath newXPath = XPathFactory.newInstance().newXPath();
            XPathExpression compile = newXPath.compile("//cmc-info-data");
            XPathExpression compile2 = newXPath.compile("record-event");
            XPathExpression compile3 = newXPath.compile("external-call-id");
            Node node = (Node) compile.evaluate(new InputSource(new StringReader(str)), XPathConstants.NODE);
            if (node == null) {
                Log.i(ResipVolteHandler.LOG_TAG, "parseXml not found Node : cmc-info-data");
                return cmcInfoEvent;
            }
            String evaluate = compile2.evaluate(node);
            String evaluate2 = compile3.evaluate(node);
            Log.i(ResipVolteHandler.LOG_TAG, "parseXml: " + evaluate + ", " + evaluate2);
            cmcInfoEvent.setRecordEvent(Integer.parseInt(evaluate));
            cmcInfoEvent.setExternalCallId(evaluate2);
            return cmcInfoEvent;
        }
    }

    private void onMakeCallResponse(AsyncResult asyncResult) {
        Log.i(LOG_TAG, "onMakeCallResponse:");
        CallResponse callResponse = (CallResponse) asyncResult.result;
        int session = callResponse.session();
        int result = callResponse.result();
        int reason = callResponse.reason();
        Call call = (Call) asyncResult.userObj;
        Log.i(LOG_TAG, "onMakeCallResponse: nameAddr=" + IMSLog.checker(call.mPeer + "") + " session=" + session + " success=" + result + " reason=" + reason);
        call.mSessionId = session;
        call.mResponse = callResponse;
        if (callResponse.sipCallId() != null) {
            call.mParam.setSipCallId(callResponse.sipCallId());
        }
        if (result == 1) {
            call.mUa.stopCamera();
        } else {
            addCall(session, call);
        }
        call.mLock.countDown();
    }

    private void onHoldResumeResponse(AsyncResult asyncResult, boolean z) {
        CallResponse callResponse = (CallResponse) asyncResult.result;
        int session = callResponse.session();
        int result = callResponse.result();
        int reason = callResponse.reason();
        StringBuilder sb = new StringBuilder();
        sb.append("onHoldResumeResponse: ");
        sb.append(z ? "hold" : "resume");
        sb.append(" session=");
        sb.append(session);
        sb.append(" success=");
        sb.append(result);
        sb.append(" reason=");
        sb.append(reason);
        Log.i(LOG_TAG, sb.toString());
    }

    private void onInfoResponse(AsyncResult asyncResult) {
        UssdEvent ussdEvent = new UssdEvent();
        GeneralResponse generalResponse = (GeneralResponse) asyncResult.result;
        if (generalResponse.result() == 0) {
            ussdEvent.setState(UssdEvent.USSD_STATE.USSD_RESPONSE);
        } else {
            ussdEvent.setState(UssdEvent.USSD_STATE.USSD_ERROR);
        }
        ussdEvent.setErrorCode(new SipError((int) generalResponse.sipError(), generalResponse.errorStr()));
        this.mUssdEventRegistrants.notifyResult(ussdEvent);
    }

    /* access modifiers changed from: private */
    public void onUpdateAudioInterfaceResponse(AsyncResult asyncResult) {
        Log.i(LOG_TAG, "onUpdateAudioInterfaceResponse:");
        ((CountDownLatch) asyncResult.userObj).countDown();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00ac, code lost:
        android.util.Log.i(LOG_TAG, "Find conference call!!");
        r10.mCallType = r1.callType();
        r10.isConference = true;
        r10.mParam.setConfSessionId(r2);
        r4 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0017, code lost:
        r6 = r4.mUa;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onCallStateChange(com.sec.internal.helper.AsyncResult r17) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            java.lang.Object r1 = r1.result
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus r1 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CallStatus) r1
            long r2 = r1.session()
            int r2 = (int) r2
            int r3 = r1.state()
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call r4 = r0.getCallBySession(r2)
            if (r4 == 0) goto L_0x0020
            com.sec.internal.ims.core.handler.secims.UserAgent r6 = r4.mUa
            if (r6 == 0) goto L_0x0020
            int r6 = r6.getPhoneId()
            goto L_0x0021
        L_0x0020:
            r6 = 0
        L_0x0021:
            com.sec.internal.constants.Mno r7 = com.sec.internal.helper.SimUtil.getSimMno(r6)
            java.lang.String r8 = "ResipVolteHandler"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "onCallStateChange() session: "
            r9.append(r10)
            r9.append(r2)
            java.lang.String r10 = " state: "
            r9.append(r10)
            int r10 = r1.state()
            r9.append(r10)
            java.lang.String r10 = " calltype : "
            r9.append(r10)
            int r10 = r1.callType()
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            android.util.Log.i(r8, r9)
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.SKT
            r9 = 2
            r10 = 18
            r11 = 14
            r12 = 6
            r13 = 11
            r14 = 8
            r15 = 1
            if (r7 != r8) goto L_0x00d0
            int r8 = r1.callType()
            if (r8 != r12) goto L_0x00d0
            if (r3 == r14) goto L_0x0070
            if (r3 == r13) goto L_0x0070
            if (r3 == r11) goto L_0x0070
            if (r3 != r10) goto L_0x00d0
        L_0x0070:
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r8 = r0.mCallList
            monitor-enter(r8)
            r5 = 0
        L_0x0074:
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r10 = r0.mCallList     // Catch:{ all -> 0x00cd }
            int r10 = r10.size()     // Catch:{ all -> 0x00cd }
            if (r5 >= r10) goto L_0x00cb
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r10 = r0.mCallList     // Catch:{ all -> 0x00cd }
            int r10 = r10.keyAt(r5)     // Catch:{ all -> 0x00cd }
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r11 = r0.mCallList     // Catch:{ all -> 0x00cd }
            java.lang.Object r10 = r11.get(r10)     // Catch:{ all -> 0x00cd }
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call r10 = (com.sec.internal.ims.core.handler.secims.ResipVolteHandler.Call) r10     // Catch:{ all -> 0x00cd }
            if (r10 == 0) goto L_0x00a4
            java.lang.String r11 = "ResipVolteHandler"
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ all -> 0x00cd }
            r13.<init>()     // Catch:{ all -> 0x00cd }
            java.lang.String r14 = "candidate callType :  "
            r13.append(r14)     // Catch:{ all -> 0x00cd }
            int r14 = r10.mCallType     // Catch:{ all -> 0x00cd }
            r13.append(r14)     // Catch:{ all -> 0x00cd }
            java.lang.String r13 = r13.toString()     // Catch:{ all -> 0x00cd }
            android.util.Log.i(r11, r13)     // Catch:{ all -> 0x00cd }
        L_0x00a4:
            if (r10 == 0) goto L_0x00c2
            int r11 = r10.mCallType     // Catch:{ all -> 0x00cd }
            if (r11 == r9) goto L_0x00ac
            if (r11 != r12) goto L_0x00c2
        L_0x00ac:
            java.lang.String r4 = "ResipVolteHandler"
            java.lang.String r5 = "Find conference call!!"
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x00cd }
            int r4 = r1.callType()     // Catch:{ all -> 0x00cd }
            r10.mCallType = r4     // Catch:{ all -> 0x00cd }
            r10.isConference = r15     // Catch:{ all -> 0x00cd }
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r4 = r10.mParam     // Catch:{ all -> 0x00cd }
            r4.setConfSessionId(r2)     // Catch:{ all -> 0x00cd }
            r4 = r10
            goto L_0x00cb
        L_0x00c2:
            int r5 = r5 + 1
            r11 = 14
            r13 = 11
            r14 = 8
            goto L_0x0074
        L_0x00cb:
            monitor-exit(r8)     // Catch:{ all -> 0x00cd }
            goto L_0x00d0
        L_0x00cd:
            r0 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x00cd }
            throw r0
        L_0x00d0:
            if (r4 != 0) goto L_0x0116
            java.lang.String r3 = "ResipVolteHandler"
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "onCallStateChange: unknown sessionId "
            r4.append(r5)
            r4.append(r2)
            java.lang.String r2 = r4.toString()
            android.util.Log.i(r3, r2)
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.TELEFONICA_UK
            if (r7 == r2) goto L_0x00f0
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.TMOBILE
            if (r7 != r2) goto L_0x0115
        L_0x00f0:
            long r1 = r1.statusCode()
            r3 = 708(0x2c4, double:3.5E-321)
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 != 0) goto L_0x0115
            java.lang.String r1 = "ResipVolteHandler"
            java.lang.String r2 = "onCallStateChange: notifyCallStatus if 708"
            android.util.Log.i(r1, r2)
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent r1 = new com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent
            r1.<init>()
            com.sec.ims.util.SipError r2 = new com.sec.ims.util.SipError
            r3 = 708(0x2c4, float:9.92E-43)
            r2.<init>(r3)
            r1.setErrorCode(r2)
            com.sec.internal.helper.RegistrantList r0 = r0.mCallStateEventRegistrants
            r0.notifyResult(r1)
        L_0x0115:
            return
        L_0x0116:
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.LGU
            if (r7 != r5) goto L_0x0139
            int r5 = r1.callType()
            if (r5 == r12) goto L_0x0127
            int r5 = r1.callType()
            r7 = 5
            if (r5 != r7) goto L_0x0139
        L_0x0127:
            r5 = 8
            if (r3 == r5) goto L_0x0137
            r5 = 11
            if (r3 == r5) goto L_0x0137
            r5 = 14
            if (r3 == r5) goto L_0x0137
            r5 = 18
            if (r3 != r5) goto L_0x0139
        L_0x0137:
            r4.isConference = r15
        L_0x0139:
            int r5 = r1.callType()
            r7 = 12
            if (r5 != r7) goto L_0x0180
            r5 = 8
            if (r3 != r5) goto L_0x0160
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r5 = r4.mParam
            boolean r5 = r5.isIncomingCall()
            if (r5 == 0) goto L_0x015a
            java.lang.String r5 = "ResipVolteHandler"
            java.lang.String r7 = "USSD indicated from network"
            android.util.Log.i(r5, r7)
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r5 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_INDICATION
            r0.notifyUssdEvent(r4, r5, r1)
            goto L_0x0180
        L_0x015a:
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r5 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_RESPONSE
            r0.notifyUssdEvent(r4, r5, r1)
            goto L_0x0180
        L_0x0160:
            r5 = 11
            if (r3 != r5) goto L_0x016d
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_INDICATION
            r0.notifyUssdEvent(r4, r3, r1)
            r0.removeCall(r2)
            return
        L_0x016d:
            r5 = 17
            if (r3 != r5) goto L_0x0177
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r5 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_INDICATION
            r0.notifyUssdEvent(r4, r5, r1)
            goto L_0x0180
        L_0x0177:
            r5 = 19
            if (r3 != r5) goto L_0x0180
            com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r5 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_RESPONSE
            r0.notifyUssdEvent(r4, r5, r1)
        L_0x0180:
            java.lang.String r5 = "ResipVolteHandler"
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "audioCodec="
            r7.append(r8)
            java.lang.String r8 = r1.audioCodecName()
            r7.append(r8)
            java.lang.String r8 = " remoteMmtelCapa="
            r7.append(r8)
            boolean r8 = r1.remoteMmtelCapa()
            r7.append(r8)
            java.lang.String r8 = " remoteVideoCapa="
            r7.append(r8)
            boolean r8 = r1.remoteVideoCapa()
            r7.append(r8)
            java.lang.String r8 = " height="
            r7.append(r8)
            long r10 = r1.height()
            r7.append(r10)
            java.lang.String r8 = " width="
            r7.append(r8)
            long r10 = r1.width()
            r7.append(r10)
            java.lang.String r8 = " isFocus="
            r7.append(r8)
            boolean r8 = r1.isFocus()
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            android.util.Log.i(r5, r7)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r6)
            java.lang.String r6 = ","
            r5.append(r6)
            r5.append(r2)
            java.lang.String r6 = ","
            r5.append(r6)
            r5.append(r3)
            java.lang.String r6 = ","
            r5.append(r6)
            int r6 = r1.callType()
            r5.append(r6)
            java.lang.String r6 = ","
            r5.append(r6)
            java.lang.String r6 = r1.audioCodecName()
            r5.append(r6)
            java.lang.String r6 = ","
            r5.append(r6)
            boolean r6 = r1.remoteVideoCapa()
            if (r6 == 0) goto L_0x0219
            boolean r6 = r0.getLocalVideoCapa(r4)
            if (r6 == 0) goto L_0x0219
            r6 = r15
            goto L_0x021a
        L_0x0219:
            r6 = 0
        L_0x021a:
            r5.append(r6)
            java.lang.String r6 = ","
            r5.append(r6)
            long r6 = r1.height()
            r5.append(r6)
            java.lang.String r6 = ","
            r5.append(r6)
            long r6 = r1.width()
            r5.append(r6)
            java.lang.String r6 = ","
            r5.append(r6)
            boolean r6 = r1.isFocus()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            r6 = 805306368(0x30000000, float:4.656613E-10)
            com.sec.internal.log.IMSLog.c(r6, r5)
            com.sec.internal.ims.core.handler.secims.UserAgent r5 = r4.mUa
            com.sec.ims.settings.ImsProfile r5 = r5.getImsProfile()
            java.lang.String r5 = r5.getMnoName()
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.fromName(r5)
            java.lang.String r6 = r1.audioCodecName()
            java.lang.String r7 = "ResipVolteHandler"
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r10 = "onCallStateChange: audioCodec "
            r8.append(r10)
            r8.append(r6)
            java.lang.String r8 = r8.toString()
            android.util.Log.i(r7, r8)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r7 = r4.mParam
            int r7 = r7.getCallState()
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r4.mParam
            int r10 = r1.state()
            r8.setCallState(r10)
            java.lang.String r8 = "ResipVolteHandler"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "onCallStateChange: oldState=  "
            r10.append(r11)
            r10.append(r7)
            java.lang.String r11 = ", newState="
            r10.append(r11)
            r10.append(r3)
            java.lang.String r10 = r10.toString()
            android.util.Log.i(r8, r10)
            java.lang.String r8 = r1.sipCallId()
            boolean r8 = android.text.TextUtils.isEmpty(r8)
            if (r8 != 0) goto L_0x02b2
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r4.mParam
            java.lang.String r10 = r1.sipCallId()
            r8.setSipCallId(r10)
        L_0x02b2:
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r4.mParam
            boolean r10 = r1.touchScreenEnabled()
            r8.setTouchScreenEnabled(r10)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r4.mParam
            r8.setisHDIcon(r15)
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.VZW
            if (r5 == r8) goto L_0x02c8
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.SINGTEL
            if (r5 != r8) goto L_0x02db
        L_0x02c8:
            boolean r8 = r1.remoteMmtelCapa()
            if (r8 != 0) goto L_0x02db
            java.lang.String r8 = "ResipVolteHandler"
            java.lang.String r10 = "disable HD icon by remote doesn't have MMTEL capability"
            android.util.Log.i(r8, r10)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r4.mParam
            r10 = 0
            r8.setisHDIcon(r10)
        L_0x02db:
            boolean r8 = android.text.TextUtils.isEmpty(r6)
            r10 = 4
            if (r8 != 0) goto L_0x02e8
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r4.mParam
            r8.setAudioCodec(r6)
            goto L_0x02fc
        L_0x02e8:
            if (r3 != r10) goto L_0x02fc
            boolean r8 = r5.isKor()
            if (r8 == 0) goto L_0x02fc
            java.lang.String r8 = "ResipVolteHandler"
            java.lang.String r11 = "KOR model need to update audio codec as NULL"
            android.util.Log.i(r8, r11)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r4.mParam
            r8.setAudioCodec(r6)
        L_0x02fc:
            r6 = 3
            if (r3 != r6) goto L_0x030a
            r0.notifyIncomingCall(r4, r1)
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r1 = "onCallStateChange: Incoming call event"
            android.util.Log.i(r0, r1)
            return
        L_0x030a:
            r6 = 10
            if (r3 != r6) goto L_0x0313
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r8 = r4.mParam
            r8.setIndicationFlag(r15)
        L_0x0313:
            long r11 = r1.statusCode()
            int r8 = (int) r11
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r8 = r0.convertToVolteState(r3, r8)
            if (r8 != 0) goto L_0x0335
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onCallStateChange: unknown event "
            r1.append(r2)
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            return
        L_0x0335:
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r11 = r0.mCallList
            monitor-enter(r11)
            int r12 = r1.callType()     // Catch:{ all -> 0x0405 }
            r4.mCallType = r12     // Catch:{ all -> 0x0405 }
            monitor-exit(r11)     // Catch:{ all -> 0x0405 }
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r11 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.EXTEND_TO_CONFERENCE
            if (r8 != r11) goto L_0x035e
            java.lang.String r11 = "ResipVolteHandler"
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "extend to conference event "
            r12.append(r13)
            long r13 = r1.statusCode()
            int r13 = (int) r13
            r12.append(r13)
            java.lang.String r12 = r12.toString()
            android.util.Log.i(r11, r12)
        L_0x035e:
            r11 = -1
            r0.notifyCallStatus(r4, r8, r1, r11)
            r8 = 11
            if (r3 != r8) goto L_0x03c9
            boolean r8 = r4.isConference
            if (r8 == 0) goto L_0x038a
            long r11 = r1.statusCode()
            int r8 = (int) r11
            r11 = 800(0x320, float:1.121E-42)
            if (r8 == r11) goto L_0x0382
            long r11 = r1.statusCode()
            int r8 = (int) r11
            r11 = 606(0x25e, float:8.49E-43)
            if (r8 != r11) goto L_0x038a
            boolean r8 = r5.isChn()
            if (r8 != 0) goto L_0x038a
        L_0x0382:
            java.lang.String r4 = "ResipVolteHandler"
            java.lang.String r8 = "conference call error received; don't remove session yet."
            android.util.Log.i(r4, r8)
            goto L_0x03c9
        L_0x038a:
            boolean r8 = r4.isConference
            if (r8 == 0) goto L_0x03ad
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.SKT
            if (r5 != r8) goto L_0x03ad
            long r11 = r1.statusCode()
            int r8 = (int) r11
            if (r8 != 0) goto L_0x03ad
            java.lang.String r4 = "ResipVolteHandler"
            java.lang.String r8 = "conference call is ended; clear all call List"
            android.util.Log.i(r4, r8)
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r8 = r0.mCallList
            monitor-enter(r8)
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r4 = r0.mCallList     // Catch:{ all -> 0x03aa }
            r4.clear()     // Catch:{ all -> 0x03aa }
            monitor-exit(r8)     // Catch:{ all -> 0x03aa }
            goto L_0x03c9
        L_0x03aa:
            r0 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x03aa }
            throw r0
        L_0x03ad:
            boolean r4 = r4.isConference
            if (r4 == 0) goto L_0x03ba
            long r11 = r1.statusCode()
            int r4 = (int) r11
            r8 = 486(0x1e6, float:6.81E-43)
            if (r4 == r8) goto L_0x03c9
        L_0x03ba:
            java.lang.String r4 = "LTE Retry in UAC Barred"
            java.lang.String r8 = r1.reasonPhrase()
            boolean r4 = r4.equals(r8)
            if (r4 != 0) goto L_0x03c9
            r0.removeCall(r2)
        L_0x03c9:
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.CMCC
            if (r5 != r4) goto L_0x0404
            int r4 = r1.callType()
            if (r4 == r9) goto L_0x03d9
            int r4 = r1.callType()
            if (r4 != r10) goto L_0x0404
        L_0x03d9:
            r4 = 8
            if (r3 != r4) goto L_0x0404
            r3 = 9
            if (r7 == r3) goto L_0x0404
            if (r7 == r6) goto L_0x0404
            r3 = 14
            if (r7 == r3) goto L_0x0404
            boolean r1 = r1.touchScreenEnabled()
            if (r1 == 0) goto L_0x0404
            java.lang.String r1 = "ResipVolteHandler"
            java.lang.String r3 = "touch screen enabled"
            android.util.Log.i(r1, r3)
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
            java.lang.String r3 = "com.samsung.telephony.extra.ims.VCS_ACTION"
            java.lang.String r4 = "ack:g.3gpp.cmos"
            r1.putString(r3, r4)
            r0.sendVcsInfo(r2, r1)
        L_0x0404:
            return
        L_0x0405:
            r0 = move-exception
            monitor-exit(r11)     // Catch:{ all -> 0x0405 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.onCallStateChange(com.sec.internal.helper.AsyncResult):void");
    }

    private void onModifyCall(AsyncResult asyncResult) {
        ModifyCallData modifyCallData = (ModifyCallData) asyncResult.result;
        int session = (int) modifyCallData.session();
        int oldType = (int) modifyCallData.oldType();
        int newType = (int) modifyCallData.newType();
        boolean isSdToSdPull = modifyCallData.isSdToSdPull();
        Call callBySession = getCallBySession(session);
        if (callBySession == null) {
            Log.i(LOG_TAG, "onModifyCall: unknown sessionId " + session);
        } else if (!TextUtils.isEmpty(modifyCallData.idcExtra())) {
            Log.i(LOG_TAG, "[IDC] onModifyCall() Transaction Handling");
            onModifyIdcSession(modifyCallData);
        } else {
            Mno fromName = Mno.fromName(callBySession.mUa.getImsProfile().getMnoName());
            Log.i(LOG_TAG, "onModifyCall() session: " + session + ", oldCallType: " + oldType + ", newCallType: " + newType + ", isSdToSdPull: " + isSdToSdPull);
            if (ImsCallUtil.isUpgradeCall(oldType, newType) && ((this.mTtyMode[callBySession.mUa.getPhoneId()] != Extensions.TelecomManager.TTY_MODE_OFF && this.mTtyMode[callBySession.mUa.getPhoneId()] != Extensions.TelecomManager.RTT_MODE) || getCall(9) != null)) {
                Log.i(LOG_TAG, "Rejecting modify request since TTY call(" + this.mTtyMode[callBySession.mUa.getPhoneId()] + ") exists");
                rejectModifyCallType(session, Id.REQUEST_UPDATE_TIME_IN_PLANI);
            } else if (fromName == Mno.ATT && oldType == 1 && newType == 3) {
                Log.i(LOG_TAG, "onModifyCall: ATT - RX upgrade to videoshare with recvonly -> automatically reject with audio only 200 OK");
                replyModifyCallType(session, oldType, oldType, newType);
            } else if (fromName != Mno.RJIL || !ImsCallUtil.isOneWayVideoCall(newType)) {
                callBySession.mCallType = convertToCallTypeBackward(newType);
                CallStateEvent callStateEvent = new CallStateEvent();
                callStateEvent.setState(CallStateEvent.CALL_STATE.MODIFY_REQUESTED);
                callStateEvent.setCallType(newType);
                callStateEvent.setSessionID(session);
                callStateEvent.setIsSdToSdPull(isSdToSdPull);
                callStateEvent.setParams(callBySession.mParam);
                this.mCallStateEventRegistrants.notifyResult(callStateEvent);
            } else {
                Log.i(LOG_TAG, "onModifyCall: RJIL - network does not support 1-way videoreject with 603");
                rejectModifyCallType(session, Id.REQUEST_UPDATE_TIME_IN_PLANI);
            }
        }
    }

    private void onModifyIdcSession(ModifyCallData modifyCallData) {
        Log.i(LOG_TAG, "[IDC] onModifyIdcSession()");
        String idcExtra = modifyCallData.idcExtra();
        CallStateEvent callStateEvent = new CallStateEvent();
        callStateEvent.setState(CallStateEvent.CALL_STATE.MODIFY_REQUESTED);
        callStateEvent.setSessionID((int) modifyCallData.session());
        callStateEvent.setIdcExtra(idcExtra);
        this.mCallStateEventRegistrants.notifyResult(callStateEvent);
    }

    private void onNewIncomingCall(AsyncResult asyncResult) {
        String str;
        IncomingCall incomingCall = (IncomingCall) asyncResult.result;
        UserAgent ua = getUa((int) incomingCall.handle());
        if (ua == null) {
            Log.i(LOG_TAG, "onNewIncomingCall: UserAgent not found.");
            return;
        }
        Log.i(LOG_TAG, "acquire wakelock for MT call by 1 sec");
        ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "ImsService").acquire(1000);
        Call call = new Call(ua, new NameAddr(incomingCall.displayName(), incomingCall.peeruri() != null ? ImsUri.parse(incomingCall.peeruri()) : null));
        call.mSessionId = (int) incomingCall.session();
        call.mCallType = incomingCall.callType();
        if (Mno.fromName(call.mUa.getImsProfile().getMnoName()) == Mno.RJIL && isOutOfService(call.mUa.getPhoneId())) {
            IMSLog.i(LOG_TAG, call.mUa.getPhoneId(), "Delete TCP socket when out of service");
            setOutOfService(false, call.mUa.getPhoneId());
            call.mUa.deleteTcpClientSocket();
        }
        CallParams callParams = new CallParams();
        callParams.setAsIncomingCall();
        if (incomingCall.referredBy() != null) {
            callParams.setReferredBy(incomingCall.referredBy());
        }
        if (incomingCall.sipCallId() != null) {
            callParams.setSipCallId(incomingCall.sipCallId());
        }
        if (incomingCall.rawSipmsg() != null) {
            callParams.setSipInviteMsg(incomingCall.rawSipmsg());
        }
        if (incomingCall.terminatingId() != null) {
            callParams.setTerminatingId(ImsUri.parse(incomingCall.terminatingId()));
        }
        if (incomingCall.numberPlus() != null) {
            callParams.setNumberPlus(incomingCall.numberPlus());
        }
        if (incomingCall.replaces() != null) {
            callParams.setReplaces(incomingCall.replaces());
        }
        if (incomingCall.photoRing() != null) {
            callParams.setPhotoRing(incomingCall.photoRing());
        }
        if (incomingCall.alertInfo() != null) {
            callParams.setAlertInfo(incomingCall.alertInfo());
        }
        if (incomingCall.historyInfo() != null) {
            callParams.setHistoryInfo(incomingCall.historyInfo());
        }
        if (incomingCall.verstat() != null) {
            callParams.setVerstat(incomingCall.verstat());
        }
        if (incomingCall.organization() != null) {
            callParams.setOrganization(incomingCall.organization());
        }
        if (incomingCall.cmcDeviceId() != null) {
            callParams.setCmcDeviceId(incomingCall.cmcDeviceId());
        }
        if (incomingCall.composerData() != null) {
            Log.i(LOG_TAG, "onNewIncomingCall: has composer data");
            ComposerData composerData = incomingCall.composerData();
            Bundle bundle = new Bundle();
            if (!TextUtils.isEmpty(composerData.subject())) {
                bundle.putString("subject", composerData.subject());
            }
            if (!TextUtils.isEmpty(composerData.image())) {
                bundle.putString(CallConstants.ComposerData.IMAGE, composerData.image());
            }
            if (!TextUtils.isEmpty(composerData.callReason())) {
                bundle.putString(CallConstants.ComposerData.CALL_REASON, composerData.callReason());
            }
            if (!TextUtils.isEmpty(composerData.latitude())) {
                bundle.putString(CallConstants.ComposerData.LATITUDE, composerData.latitude());
            }
            if (!TextUtils.isEmpty(composerData.longitude())) {
                bundle.putString(CallConstants.ComposerData.LONGITUDE, composerData.longitude());
            }
            if (!TextUtils.isEmpty(composerData.radius())) {
                bundle.putString(CallConstants.ComposerData.RADIUS, composerData.radius());
            }
            bundle.putBoolean("importance", composerData.importance());
            callParams.setComposerData(bundle);
        }
        if (incomingCall.idcExtra() != null) {
            str = incomingCall.idcExtra();
        } else {
            str = "";
        }
        callParams.setHasDiversion(incomingCall.hasDiversion());
        callParams.setCmcEdCallSlot((int) incomingCall.cmcEdCallSlot());
        call.mParam = callParams;
        addCall(call.mSessionId, call);
        StringBuilder sb = new StringBuilder();
        sb.append("onNewIncomingCall: sessionId ");
        sb.append(call.mSessionId);
        sb.append(" peer ");
        sb.append(IMSLog.checker(call.mPeer + ""));
        Log.i(LOG_TAG, sb.toString());
        IncomingCallEvent incomingCallEvent = new IncomingCallEvent(ua.getImsRegistration(), call.mSessionId, convertToCallTypeForward(call.mCallType), call.mPeer, true, false, str, call.mParam);
        Log.i(LOG_TAG, "notifyIncomingCall() pre Alerting session: " + call.mSessionId + ", callType: " + call.mCallType);
        this.mIncomingCallEventRegistrants.notifyResult(incomingCallEvent);
    }

    private void onDedicatedBearerEventReceived(AsyncResult asyncResult) {
        Log.i(LOG_TAG, "onDedicatedBearerEventReceived:");
        DedicatedBearerEvent dedicatedBearerEvent = (DedicatedBearerEvent) asyncResult.result;
        this.mDedicatedBearerEventRegistrants.notifyResult(new com.sec.internal.ims.servicemodules.volte2.data.DedicatedBearerEvent(convertDedicatedBearerState(dedicatedBearerEvent.bearerState()), (int) dedicatedBearerEvent.qci(), (int) dedicatedBearerEvent.session()));
    }

    private void onRtpLossRateNoti(AsyncResult asyncResult) {
        Log.i(LOG_TAG, "onRtpLossRateNoti:");
        RtpLossRateNoti rtpLossRateNoti = (RtpLossRateNoti) asyncResult.result;
        this.mRtpLossRateNotiRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti((int) rtpLossRateNoti.interval(), (float) ((int) rtpLossRateNoti.lossrate()), rtpLossRateNoti.jitter(), (int) rtpLossRateNoti.notification()));
    }

    private void onRrcConnectionEventReceived(AsyncResult asyncResult) {
        Log.i(LOG_TAG, "onRrcConnectionEventReceived:");
        RrcConnectionEvent rrcConnectionEvent = (RrcConnectionEvent) asyncResult.result;
        if (rrcConnectionEvent.event() == 1) {
            this.mRrcConnectionEventRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent(RrcConnectionEvent.RrcEvent.REJECTED));
        } else if (rrcConnectionEvent.event() == 2) {
            this.mRrcConnectionEventRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent(RrcConnectionEvent.RrcEvent.TIMER_EXPIRED));
        }
    }

    private void onQuantumSecurityStatusEventReceived(AsyncResult asyncResult) {
        String str;
        QuantumSecurityStatusEvent quantumSecurityStatusEvent = (QuantumSecurityStatusEvent) asyncResult.result;
        if (quantumSecurityStatusEvent.event() == 1) {
            this.mQuantumSecurityStatusEventRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.QuantumSecurityStatusEvent((int) quantumSecurityStatusEvent.session(), QuantumSecurityStatusEvent.QuantumEvent.FALLBACK_TO_NORMAL_CALL, quantumSecurityStatusEvent.qtSessionId()));
        } else if (quantumSecurityStatusEvent.event() == 2) {
            this.mQuantumSecurityStatusEventRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.QuantumSecurityStatusEvent((int) quantumSecurityStatusEvent.session(), QuantumSecurityStatusEvent.QuantumEvent.SUCCESS, quantumSecurityStatusEvent.qtSessionId()));
        } else if (quantumSecurityStatusEvent.event() == 3) {
            this.mQuantumSecurityStatusEventRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.QuantumSecurityStatusEvent((int) quantumSecurityStatusEvent.session(), QuantumSecurityStatusEvent.QuantumEvent.NOTIFY_SESSION_ID, quantumSecurityStatusEvent.qtSessionId()));
        } else {
            Log.i(LOG_TAG, "unsupported event: " + quantumSecurityStatusEvent.event());
        }
        if (!SemSystemProperties.get("ro.build.type", "user").equals("user")) {
            if (quantumSecurityStatusEvent.event() == 2) {
                str = "Encryption SUCCESS noti received!!!";
            } else if (quantumSecurityStatusEvent.event() == 1) {
                str = "Fallback to NORMAL CALL noti received!!!";
            } else {
                str = quantumSecurityStatusEvent.event() == 3 ? "NOTIFY_SESSION_ID CALL noti received!!!" : "Unknown noti received!!!";
            }
            Toast.makeText(this.mContext, str, 1).show();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0036, code lost:
        r2 = r0.mUa;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onConferenceUpdate(com.sec.internal.helper.AsyncResult r10) {
        /*
            r9 = this;
            java.lang.Object r10 = r10.result
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged r10 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged) r10
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onConferenceUpdate: session "
            r1.append(r2)
            long r2 = r10.session()
            r1.append(r2)
            java.lang.String r2 = " event "
            r1.append(r2)
            int r2 = r10.event()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            long r0 = r10.session()
            int r0 = (int) r0
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call r0 = r9.getCallBySession(r0)
            r1 = 0
            if (r0 == 0) goto L_0x003f
            com.sec.internal.ims.core.handler.secims.UserAgent r2 = r0.mUa
            if (r2 == 0) goto L_0x003f
            int r2 = r2.getPhoneId()
            goto L_0x0040
        L_0x003f:
            r2 = r1
        L_0x0040:
            com.sec.internal.constants.Mno r2 = com.sec.internal.helper.SimUtil.getSimMno(r2)
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.SKT
            if (r2 != r3) goto L_0x0095
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r2 = r9.mCallList
            monitor-enter(r2)
            r3 = r1
        L_0x004c:
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r4 = r9.mCallList     // Catch:{ all -> 0x0092 }
            int r4 = r4.size()     // Catch:{ all -> 0x0092 }
            if (r3 >= r4) goto L_0x0090
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r4 = r9.mCallList     // Catch:{ all -> 0x0092 }
            int r4 = r4.keyAt(r3)     // Catch:{ all -> 0x0092 }
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r5 = r9.mCallList     // Catch:{ all -> 0x0092 }
            java.lang.Object r4 = r5.get(r4)     // Catch:{ all -> 0x0092 }
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call r4 = (com.sec.internal.ims.core.handler.secims.ResipVolteHandler.Call) r4     // Catch:{ all -> 0x0092 }
            if (r4 == 0) goto L_0x007d
            java.lang.String r5 = "ResipVolteHandler"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0092 }
            r6.<init>()     // Catch:{ all -> 0x0092 }
            java.lang.String r7 = "tempCall.mCallType :  "
            r6.append(r7)     // Catch:{ all -> 0x0092 }
            int r7 = r4.mCallType     // Catch:{ all -> 0x0092 }
            r6.append(r7)     // Catch:{ all -> 0x0092 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0092 }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x0092 }
        L_0x007d:
            if (r4 == 0) goto L_0x008d
            int r5 = r4.mCallType     // Catch:{ all -> 0x0092 }
            r6 = 6
            if (r5 != r6) goto L_0x008d
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r3 = "Find confcall!!"
            android.util.Log.i(r0, r3)     // Catch:{ all -> 0x0092 }
            r0 = r4
            goto L_0x0090
        L_0x008d:
            int r3 = r3 + 1
            goto L_0x004c
        L_0x0090:
            monitor-exit(r2)     // Catch:{ all -> 0x0092 }
            goto L_0x0095
        L_0x0092:
            r9 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0092 }
            throw r9
        L_0x0095:
            if (r0 != 0) goto L_0x009f
            java.lang.String r9 = "ResipVolteHandler"
            java.lang.String r10 = "onConferenceUpdate: session not found."
            android.util.Log.i(r9, r10)
            return
        L_0x009f:
            int r2 = r0.mSessionId
            int r3 = r10.event()
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent r4 = new com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent
            r4.<init>()
            int r5 = r0.mCallType
            int r5 = r9.convertToCallTypeBackward(r5)
            r4.setCallType(r5)
            r4.setSessionID(r2)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r2 = r0.mParam
            r4.setParams(r2)
            boolean r0 = r0.isConference
            r4.setConference(r0)
            r0 = 2
            if (r3 != 0) goto L_0x015a
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_ADDED
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
        L_0x00ca:
            int r5 = r10.participantsLength()
            if (r1 >= r5) goto L_0x00da
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant r5 = r10.participants(r1)
            r3.add(r5)
            int r1 = r1 + 1
            goto L_0x00ca
        L_0x00da:
            java.util.Iterator r10 = r3.iterator()
        L_0x00de:
            boolean r1 = r10.hasNext()
            if (r1 == 0) goto L_0x0247
            java.lang.Object r1 = r10.next()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant r1 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant) r1
            java.lang.String r3 = "ResipVolteHandler"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "onConferenceUpdate: "
            r5.append(r6)
            java.lang.String r6 = r1.uri()
            java.lang.String r6 = com.sec.internal.log.IMSLog.checker(r6)
            r5.append(r6)
            java.lang.String r6 = " added.  partid "
            r5.append(r6)
            long r6 = r1.participantid()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r3, r5)
            int r3 = r1.status()
            int r3 = r9.getParticipantStatus(r3)
            long r5 = r1.sessionId()
            int r5 = (int) r5
            java.lang.String r6 = r1.uri()
            long r7 = r1.participantid()
            int r1 = (int) r7
            r4.addUpdatedParticipantsList(r6, r1, r5, r3)
            if (r3 != r0) goto L_0x00de
            java.lang.String r1 = "ResipVolteHandler"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r6 = "Session ("
            r3.append(r6)
            r3.append(r5)
            java.lang.String r6 = ") join to conference"
            r3.append(r6)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r1, r3)
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent r1 = new com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_ADDED
            r1.<init>(r3)
            r1.setSessionID(r5)
            com.sec.internal.helper.RegistrantList r3 = r9.mCallStateEventRegistrants
            r3.notifyResult(r1)
            goto L_0x00de
        L_0x015a:
            r2 = 1
            if (r3 != r2) goto L_0x01c1
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_REMOVED
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
        L_0x0164:
            int r3 = r10.participantsLength()
            if (r1 >= r3) goto L_0x0174
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant r3 = r10.participants(r1)
            r0.add(r3)
            int r1 = r1 + 1
            goto L_0x0164
        L_0x0174:
            java.util.Iterator r10 = r0.iterator()
        L_0x0178:
            boolean r0 = r10.hasNext()
            if (r0 == 0) goto L_0x0247
            java.lang.Object r0 = r10.next()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant r0 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant) r0
            java.lang.String r1 = "ResipVolteHandler"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r5 = "onConferenceUpdate: "
            r3.append(r5)
            java.lang.String r5 = r0.uri()
            java.lang.String r5 = com.sec.internal.log.IMSLog.checker(r5)
            r3.append(r5)
            java.lang.String r5 = " removed."
            r3.append(r5)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r1, r3)
            java.lang.String r1 = r0.uri()
            long r5 = r0.participantid()
            int r3 = (int) r5
            long r5 = r0.sessionId()
            int r5 = (int) r5
            int r0 = r0.status()
            int r0 = r9.getParticipantStatus(r0)
            r4.addUpdatedParticipantsList(r1, r3, r5, r0)
            goto L_0x0178
        L_0x01c1:
            if (r3 != r0) goto L_0x0250
            java.lang.String r0 = "ResipVolteHandler"
            java.lang.String r2 = "onConferenceUpdate: CONF_PARTICIPANT_UPDATED"
            android.util.Log.i(r0, r2)
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_PARTICIPANTS_UPDATED
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
        L_0x01d1:
            int r3 = r10.participantsLength()
            if (r1 >= r3) goto L_0x01e1
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant r3 = r10.participants(r1)
            r0.add(r3)
            int r1 = r1 + 1
            goto L_0x01d1
        L_0x01e1:
            java.util.Iterator r10 = r0.iterator()
        L_0x01e5:
            boolean r0 = r10.hasNext()
            if (r0 == 0) goto L_0x0247
            java.lang.Object r0 = r10.next()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant r0 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant) r0
            com.sec.ims.util.NameAddr r1 = new com.sec.ims.util.NameAddr
            java.lang.String r3 = ""
            java.lang.String r5 = r0.uri()
            com.sec.ims.util.ImsUri r5 = com.sec.ims.util.ImsUri.parse(r5)
            r1.<init>(r3, r5)
            r4.setPeerAddr(r1)
            java.lang.String r1 = r0.uri()
            long r5 = r0.participantid()
            int r3 = (int) r5
            long r5 = r0.sessionId()
            int r5 = (int) r5
            int r6 = r0.status()
            int r6 = r9.getParticipantStatus(r6)
            r4.addUpdatedParticipantsList(r1, r3, r5, r6)
            java.lang.String r1 = "ResipVolteHandler"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r5 = "onConferenceUpdate: "
            r3.append(r5)
            java.lang.String r5 = r0.uri()
            java.lang.String r5 = com.sec.internal.log.IMSLog.checker(r5)
            r3.append(r5)
            java.lang.String r5 = " update id . "
            r3.append(r5)
            long r5 = r0.participantid()
            r3.append(r5)
            java.lang.String r0 = r3.toString()
            android.util.Log.i(r1, r0)
            goto L_0x01e5
        L_0x0247:
            r4.setState(r2)
            com.sec.internal.helper.RegistrantList r9 = r9.mCallStateEventRegistrants
            r9.notifyResult(r4)
            return
        L_0x0250:
            java.lang.String r9 = "ResipVolteHandler"
            java.lang.String r10 = "onConferenceUpdate: unknown event. ignore."
            android.util.Log.i(r9, r10)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.onConferenceUpdate(com.sec.internal.helper.AsyncResult):void");
    }

    private void onReferReceived(AsyncResult asyncResult) {
        ReferReceived referReceived = (ReferReceived) asyncResult.result;
        UserAgent ua = getUa((int) referReceived.handle());
        if (ua == null) {
            Log.e(LOG_TAG, "onReferReceived: unknown handle " + referReceived.handle());
            return;
        }
        ua.acceptCallTranfer((int) referReceived.session(), true, 0, (String) null);
    }

    private void onReferStatus(AsyncResult asyncResult) {
        ReferStatus referStatus = (ReferStatus) asyncResult.result;
        Log.i(LOG_TAG, "onReferStatus: session " + referStatus.session() + " respCode " + referStatus.statusCode());
        this.mReferStatusRegistrants.notifyResult(new com.sec.internal.ims.servicemodules.volte2.data.ReferStatus((int) referStatus.session(), (int) referStatus.statusCode()));
    }

    private void onDialogEventReceived(AsyncResult asyncResult) {
        UserAgent userAgent;
        String str;
        DialogEvent dialogEvent;
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DialogEvent dialogEvent2 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.DialogEvent) asyncResult.result;
        String str2 = "";
        if (dialogEvent2 != null) {
            AdditionalContents additionalContents = dialogEvent2.additionalContents();
            String mimeType = (additionalContents == null || additionalContents.mimeType() == null) ? str2 : additionalContents.mimeType();
            if (!(additionalContents == null || additionalContents.contents() == null)) {
                str2 = additionalContents.contents();
            }
            userAgent = getUa((int) dialogEvent2.handle());
            str = str2;
            str2 = mimeType;
        } else {
            userAgent = null;
            str = str2;
        }
        if (userAgent == null) {
            Log.e(LOG_TAG, "ignore dialog event UA is null");
            return;
        }
        ImsRegistration imsRegistration = userAgent.getImsRegistration();
        if (imsRegistration == null) {
            Log.e(LOG_TAG, "ignore dialog event without registration");
            return;
        }
        Log.i(LOG_TAG, "onDialogEventReceived: has AdditionalContents of type " + str2 + " (" + str.length() + " bytes)");
        if (!str2.equals(DIALOG_EVENT_MIME_TYPE)) {
            Log.e(LOG_TAG, "onDialogEventReceived: contentType mismatch!");
            return;
        }
        try {
            if (!(userAgent.getImsProfile().getCmcType() == 2 || userAgent.getImsProfile().getCmcType() == 4)) {
                if (userAgent.getImsProfile().getCmcType() != 8) {
                    dialogEvent = DialogXmlParser.getInstance().parseDialogInfoXml(str);
                    dialogEvent.setRegId(imsRegistration.getHandle());
                    dialogEvent.setPhoneId(imsRegistration.getPhoneId());
                    this.mDialogEventRegistrants.notifyResult(dialogEvent);
                }
            }
            dialogEvent = DialogXmlParser.getInstance().parseDialogInfoXml(str, userAgent.getImsProfile().getCmcType());
            dialogEvent.setRegId(imsRegistration.getHandle());
            dialogEvent.setPhoneId(imsRegistration.getPhoneId());
            this.mDialogEventRegistrants.notifyResult(dialogEvent);
        } catch (XPathExpressionException e) {
            Log.e(LOG_TAG, "failed to parse dialog xml!", e);
        }
    }

    private void onCdpnInfoReceived(AsyncResult asyncResult) {
        this.mCdpnInfoRegistrants.notifyResult((String) asyncResult.result);
    }

    private void onDtmfInfo(AsyncResult asyncResult) {
        DTMFDataEvent dTMFDataEvent = (DTMFDataEvent) asyncResult.result;
        this.mDtmfEventRegistrants.notifyResult(new DtmfInfo((int) dTMFDataEvent.event(), (int) dTMFDataEvent.volume(), (int) dTMFDataEvent.duration(), (int) dTMFDataEvent.endbit()));
    }

    private void onTextInfo(AsyncResult asyncResult) {
        TextDataEvent textDataEvent = (TextDataEvent) asyncResult.result;
        this.mTextEventRegistrants.notifyResult(new TextInfo(0, textDataEvent.text(), (int) textDataEvent.len()));
    }

    private void onCmcInfoReceived(AsyncResult asyncResult) {
        UserAgent userAgent;
        String str;
        CallSendCmcInfo callSendCmcInfo = (CallSendCmcInfo) asyncResult.result;
        String str2 = "";
        if (callSendCmcInfo != null) {
            AdditionalContents additionalContents = callSendCmcInfo.additionalContents();
            String mimeType = (additionalContents == null || additionalContents.mimeType() == null) ? str2 : additionalContents.mimeType();
            if (!(additionalContents == null || additionalContents.contents() == null)) {
                str2 = additionalContents.contents();
            }
            userAgent = getUa((int) callSendCmcInfo.handle());
            str = str2;
            str2 = mimeType;
        } else {
            userAgent = null;
            str = str2;
        }
        if (userAgent == null) {
            Log.e(LOG_TAG, "ignore CmcInfo event UA is null");
        } else if (userAgent.getImsRegistration() == null) {
            Log.e(LOG_TAG, "ignore CmcInfo event without registration");
        } else {
            Log.i(LOG_TAG, "onCmcInfoReceived: has AdditionalContents of type " + str2 + " (" + str.length() + " bytes)");
            if (!str2.equals(CMC_INFO_MIME_TYPE)) {
                Log.e(LOG_TAG, "onCmcInfoReceived: contentType mismatch!");
                return;
            }
            try {
                if (userAgent.getImsProfile().getCmcType() == 2) {
                    this.mCmcInfoEventRegistrants.notifyResult(CmcInfoXmlParser.parseXml(str));
                }
            } catch (XPathExpressionException e) {
                Log.e(LOG_TAG, "failed to parse cmc info xml!", e);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:19:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onCurrentLocationDiscoveryDuringEmergencyCall(com.sec.internal.helper.AsyncResult r5) {
        /*
            r4 = this;
            java.lang.Object r5 = r5.result
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CurrentLocationDiscoveryDuringEmergencyCall r5 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.CurrentLocationDiscoveryDuringEmergencyCall) r5
            java.lang.String r0 = "ResipVolteHandler"
            if (r5 != 0) goto L_0x000e
            java.lang.String r4 = "onCurrentLocationDiscoveryDuringEmergencyCall() result is null"
            android.util.Log.d(r0, r4)
            return
        L_0x000e:
            int r1 = r5.sessionId()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r2 = r5.additionalContents()
            if (r2 == 0) goto L_0x004d
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r2 = r5.additionalContents()
            java.lang.String r2 = r2.mimeType()
            java.lang.String r3 = "application/vnd.3gpp.current-location-discovery+xml"
            boolean r2 = r3.equals(r2)
            if (r2 == 0) goto L_0x004d
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r2 = r5.additionalContents()
            java.lang.String r2 = r2.contents()
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 != 0) goto L_0x004d
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$InfoXmlParser r2 = com.sec.internal.ims.core.handler.secims.ResipVolteHandler.InfoXmlParser.getInstance()     // Catch:{ Exception -> 0x0047 }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents r5 = r5.additionalContents()     // Catch:{ Exception -> 0x0047 }
            java.lang.String r5 = r5.contents()     // Catch:{ Exception -> 0x0047 }
            java.lang.String r5 = r2.parseInfoXml(r5)     // Catch:{ Exception -> 0x0047 }
            goto L_0x004f
        L_0x0047:
            r5 = move-exception
            java.lang.String r2 = "onCurrentLocationDiscoveryDuringEmergencyCall: error parsing INFO xml"
            android.util.Log.e(r0, r2, r5)
        L_0x004d:
            java.lang.String r5 = "none"
        L_0x004f:
            java.lang.String r2 = "oneShot"
            boolean r5 = r2.equals(r5)
            if (r5 == 0) goto L_0x0074
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r2 = "onCurrentLocationDiscoveryDuringEmergencyCall() session: "
            r5.append(r2)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r0, r5)
            com.sec.internal.helper.RegistrantList r4 = r4.mCurrentLocationDiscoveryDuringEmergencyCallRegistrants
            java.lang.Integer r5 = java.lang.Integer.valueOf(r1)
            r4.notifyResult(r5)
        L_0x0074:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.onCurrentLocationDiscoveryDuringEmergencyCall(com.sec.internal.helper.AsyncResult):void");
    }

    private CallStateEvent.CALL_STATE convertToVolteState(int i, int i2) {
        if (i == 1) {
            return CallStateEvent.CALL_STATE.TRYING;
        }
        if (i == 2) {
            return CallStateEvent.CALL_STATE.CALLING;
        }
        if (i == 4) {
            return CallStateEvent.CALL_STATE.RINGING_BACK;
        }
        if (i == 5) {
            return CallStateEvent.CALL_STATE.FORWARDED;
        }
        if (i == 18) {
            return CallStateEvent.CALL_STATE.EXTEND_TO_CONFERENCE;
        }
        switch (i) {
            case 8:
                return CallStateEvent.CALL_STATE.ESTABLISHED;
            case 9:
                return CallStateEvent.CALL_STATE.HELD_LOCAL;
            case 10:
                return CallStateEvent.CALL_STATE.HELD_REMOTE;
            case 11:
                if (i2 != 0) {
                    return CallStateEvent.CALL_STATE.ERROR;
                }
                return CallStateEvent.CALL_STATE.ENDED;
            case 12:
                return CallStateEvent.CALL_STATE.EARLY_MEDIA_START;
            case 13:
                return CallStateEvent.CALL_STATE.HELD_BOTH;
            case 14:
                if (i2 == 0 || i2 == 1122) {
                    return CallStateEvent.CALL_STATE.MODIFIED;
                }
                return CallStateEvent.CALL_STATE.ERROR;
            case 15:
                return CallStateEvent.CALL_STATE.SESSIONPROGRESS;
            case 16:
                return CallStateEvent.CALL_STATE.REFRESHFAIL;
            default:
                Log.e(LOG_TAG, "convertToVolteState: unknown Call state " + i);
                return null;
        }
    }

    private int convertToCallTypeForward(int i) {
        switch (i) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            case 11:
                return 11;
            case 12:
                return 12;
            case 13:
                return 1;
            case 14:
                return 14;
            case 15:
                return 15;
            case 18:
                return 18;
            case 19:
                return 19;
            default:
                Log.e(LOG_TAG, "convertToCallType:: unknown call type " + i);
                return 1;
        }
    }

    private int convertToCallTypeBackward(int i) {
        switch (i) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            case 11:
                return 11;
            case 12:
                return 12;
            case 14:
                return 14;
            case 15:
                return 15;
            case 18:
                return 18;
            case 19:
                return 19;
            default:
                Log.e(LOG_TAG, "convertToCallType: unknown call type " + i);
                return 1;
        }
    }

    private void addCall(int i, Call call) {
        synchronized (this.mCallList) {
            Log.i(LOG_TAG, "Add Call " + i);
            this.mCallList.append(i, call);
        }
    }

    private void removeCall(int i) {
        synchronized (this.mCallList) {
            Log.i(LOG_TAG, "Remove Call " + i);
            this.mCallList.remove(i);
        }
    }

    private Call getCallBySession(int i) {
        synchronized (this.mCallList) {
            for (int i2 = 0; i2 < this.mCallList.size(); i2++) {
                Call call = this.mCallList.get(this.mCallList.keyAt(i2));
                if (call != null && call.mSessionId == i) {
                    return call;
                }
            }
            return null;
        }
    }

    private Call getCall(int i) {
        synchronized (this.mCallList) {
            for (int i2 = 0; i2 < this.mCallList.size(); i2++) {
                Call call = this.mCallList.get(this.mCallList.keyAt(i2));
                if (call != null && call.mCallType == i) {
                    return call;
                }
            }
            return null;
        }
    }

    private void dumpCall() {
        synchronized (this.mCallList) {
            Log.i(LOG_TAG, "Call List Size : " + this.mCallList.size());
            for (int i = 0; i < this.mCallList.size(); i++) {
                Call call = this.mCallList.get(this.mCallList.keyAt(i));
                if (call != null) {
                    Log.i(LOG_TAG, "Session Id : " + call.mSessionId + " in the list");
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006e, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0070, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean IsModifiableNeedToBeIgnored(com.sec.internal.ims.core.handler.secims.ResipVolteHandler.Call r7, com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE r8, com.sec.internal.constants.Mno r9) {
        /*
            r6 = this;
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r0 = r6.mCallList
            monitor-enter(r0)
            com.sec.internal.ims.core.handler.secims.UserAgent r7 = r7.mUa     // Catch:{ all -> 0x0071 }
            int r7 = r7.getPhoneId()     // Catch:{ all -> 0x0071 }
            r1 = 0
            r2 = r1
            r3 = r2
        L_0x000c:
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r4 = r6.mCallList     // Catch:{ all -> 0x0071 }
            int r4 = r4.size()     // Catch:{ all -> 0x0071 }
            if (r2 >= r4) goto L_0x0045
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r4 = r6.mCallList     // Catch:{ all -> 0x0071 }
            int r4 = r4.keyAt(r2)     // Catch:{ all -> 0x0071 }
            android.util.SparseArray<com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call> r5 = r6.mCallList     // Catch:{ all -> 0x0071 }
            java.lang.Object r4 = r5.get(r4)     // Catch:{ all -> 0x0071 }
            com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call r4 = (com.sec.internal.ims.core.handler.secims.ResipVolteHandler.Call) r4     // Catch:{ all -> 0x0071 }
            if (r4 == 0) goto L_0x0042
            com.sec.internal.ims.core.handler.secims.UserAgent r5 = r4.mUa     // Catch:{ all -> 0x0071 }
            com.sec.internal.ims.core.handler.secims.UaProfile r5 = r5.getUaProfile()     // Catch:{ all -> 0x0071 }
            if (r5 == 0) goto L_0x0042
            com.sec.internal.ims.core.handler.secims.UserAgent r5 = r4.mUa     // Catch:{ all -> 0x0071 }
            com.sec.internal.ims.core.handler.secims.UaProfile r5 = r5.getUaProfile()     // Catch:{ all -> 0x0071 }
            int r5 = r5.getCmcType()     // Catch:{ all -> 0x0071 }
            if (r5 != 0) goto L_0x0042
            com.sec.internal.ims.core.handler.secims.UserAgent r4 = r4.mUa     // Catch:{ all -> 0x0071 }
            int r4 = r4.getPhoneId()     // Catch:{ all -> 0x0071 }
            if (r4 != r7) goto L_0x0042
            int r3 = r3 + 1
        L_0x0042:
            int r2 = r2 + 1
            goto L_0x000c
        L_0x0045:
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r6 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_LOCAL     // Catch:{ all -> 0x0071 }
            r7 = 1
            if (r8 == r6) goto L_0x0054
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r6 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_REMOTE     // Catch:{ all -> 0x0071 }
            if (r8 == r6) goto L_0x0054
            com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r6 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_BOTH     // Catch:{ all -> 0x0071 }
            if (r8 == r6) goto L_0x0054
            if (r3 <= r7) goto L_0x006d
        L_0x0054:
            boolean r6 = r9.isChn()     // Catch:{ all -> 0x0071 }
            if (r6 != 0) goto L_0x006f
            boolean r6 = r9.isHkMo()     // Catch:{ all -> 0x0071 }
            if (r6 != 0) goto L_0x006f
            boolean r6 = r9.isKor()     // Catch:{ all -> 0x0071 }
            if (r6 != 0) goto L_0x006f
            boolean r6 = r9.isJpn()     // Catch:{ all -> 0x0071 }
            if (r6 == 0) goto L_0x006d
            goto L_0x006f
        L_0x006d:
            monitor-exit(r0)     // Catch:{ all -> 0x0071 }
            return r1
        L_0x006f:
            monitor-exit(r0)     // Catch:{ all -> 0x0071 }
            return r7
        L_0x0071:
            r6 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0071 }
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipVolteHandler.IsModifiableNeedToBeIgnored(com.sec.internal.ims.core.handler.secims.ResipVolteHandler$Call, com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE, com.sec.internal.constants.Mno):boolean");
    }

    public void handleMessage(Message message) {
        Log.i(LOG_TAG, "handleMessage: evt " + message.what);
        int i = message.what;
        if (i != 1) {
            if (i == 200) {
                this.mCallStateEventRegistrants.notifyResult(message.obj);
                return;
            } else if (i != 3) {
                if (i == 4) {
                    onHoldResumeResponse((AsyncResult) message.obj, true);
                    return;
                } else if (i == 5) {
                    onHoldResumeResponse((AsyncResult) message.obj, false);
                    return;
                } else if (i != 6) {
                    if (i != 7) {
                        switch (i) {
                            case 100:
                                onCallStateChange((AsyncResult) message.obj);
                                return;
                            case 101:
                                onNewIncomingCall((AsyncResult) message.obj);
                                return;
                            case 102:
                                onConferenceUpdate((AsyncResult) message.obj);
                                return;
                            case 103:
                                onReferReceived((AsyncResult) message.obj);
                                return;
                            case 104:
                                onReferStatus((AsyncResult) message.obj);
                                return;
                            case 105:
                                onDialogEventReceived((AsyncResult) message.obj);
                                return;
                            case 106:
                                onModifyCall((AsyncResult) message.obj);
                                return;
                            case 107:
                                onCdpnInfoReceived((AsyncResult) message.obj);
                                return;
                            case 108:
                                onRtpLossRateNoti((AsyncResult) message.obj);
                                return;
                            default:
                                switch (i) {
                                    case 110:
                                        onDedicatedBearerEventReceived((AsyncResult) message.obj);
                                        return;
                                    case 111:
                                        onRrcConnectionEventReceived((AsyncResult) message.obj);
                                        return;
                                    case 112:
                                        onDtmfInfo((AsyncResult) message.obj);
                                        return;
                                    case 113:
                                        onTextInfo((AsyncResult) message.obj);
                                        return;
                                    case 114:
                                        sendSIPMSGInfo((Notify) ((AsyncResult) message.obj).result);
                                        return;
                                    case 115:
                                        onCmcInfoReceived((AsyncResult) message.obj);
                                        return;
                                    case 116:
                                        onQuantumSecurityStatusEventReceived((AsyncResult) message.obj);
                                        return;
                                    case 117:
                                        onCurrentLocationDiscoveryDuringEmergencyCall((AsyncResult) message.obj);
                                        return;
                                    default:
                                        return;
                                }
                        }
                    } else {
                        onInfoResponse((AsyncResult) message.obj);
                        return;
                    }
                }
            }
        }
        onMakeCallResponse((AsyncResult) message.obj);
    }

    private void sendSIPMSGInfo(Notify notify) {
        SipMessage sipMessage = (SipMessage) notify.noti(new SipMessage());
        String sipMessage2 = sipMessage.sipMessage();
        if (!TextUtils.isEmpty(sipMessage2)) {
            this.mSIPMSGNotiRegistrants.notifyResult(new SIPDataEvent(sipMessage2, sipMessage.direction() == 0));
        }
    }

    private class AudioInterfaceHandler extends Handler {
        public AudioInterfaceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            Log.i(ResipVolteHandler.LOG_TAG, "Event " + message.what);
            if (message.what != 8) {
                Log.e(ResipVolteHandler.LOG_TAG, "Invalid event");
            } else {
                ResipVolteHandler.this.onUpdateAudioInterfaceResponse((AsyncResult) message.obj);
            }
        }
    }
}
