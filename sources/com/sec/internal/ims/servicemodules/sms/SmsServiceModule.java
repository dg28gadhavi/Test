package com.sec.internal.ims.servicemodules.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.TelephonyManager;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.sms.ISmsServiceEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.util.IMessagingAppInfoListener;
import com.sec.internal.ims.util.MessagingAppInfoReceiver;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.log.IMSLog;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class SmsServiceModule extends ServiceModuleBase implements ISmsServiceModule, IMessagingAppInfoListener {
    private static final String ACTION_EMERGENCY_CALLBACK_MODE_INTERNAL = "com.samsung.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED_INTERNAL";
    private static final String ALTERNATIVE_SERVICE = "application/3gpp-ims+xml";
    private static final String ASVC_INITIAL_REGISTRATION = "initial-registration";
    private static final String ASVC_RESTORATION = "restoration";
    protected static final int EMERGENCY_GEOLOCATION_UPDATED = 10;
    protected static final int EMERGENCY_REGISTER_DONE_EVENT = 5;
    protected static final int EMERGENCY_REGISTER_FAIL_EVENT = 6;
    protected static final int EMERGENCY_REGISTER_START_EVENT = 4;
    /* access modifiers changed from: private */
    public static final String LOG_TAG;
    private static final int MAX_RETRANS_COUNT_ON_RP_ERR = 1;
    public static final String NAME;
    private static final int NOTI_503_OUTAGE = 777;
    private static final int NOTI_DEREGISTERED = 999;
    public static final int NOTI_INTERNAL_ADDR_ERR = 10001;
    public static final int NOTI_INTERNAL_BASE = 10000;
    public static final int NOTI_INTERNAL_EMERGENCY_REGI_FAIL = 10002;
    public static final int NOTI_INTERNAL_END = 11000;
    public static final int NOTI_INTERNAL_LIMITED_REGI = 10004;
    public static final int NOTI_INTERNAL_NO_RP_ACK = 10003;
    private static final int NOTI_SUBMIT_REPORT_TIMEOUT = 801;
    protected static final int RESET_EMERGENCY_GEOLOCATION_STATE = 8;
    private static final int RETRANS_ON_RP_ERROR_TIMEOUT = 3;
    protected static final int RRC_CONNECTION_EVENT = 2;
    protected static final int SCBM_TIMEOUT_EVENT = 7;
    protected static final int SEND_SMS_EVENT = 3;
    private static final int SIP_R_CAUSE_200_OK = 200;
    private static final int SIP_R_CAUSE_LIMITED = 404;
    private static final int SIP_R_CAUSE_TEMP_ERROR = 480;
    protected static final int SMS_EVENT = 1;
    private static final int STATE_TIMEOUT = 1;
    private static final int SUBMIT_REPORT_TIMEOUT = 2;
    protected static final int TIMEOUT_EMERGENCY_GEOLOCATION_UPDATE = 9;
    private static final int TIMER_EMERGENCY_REGISTER_FAIL = 10000;
    private static final int TIMER_RESET_EMERGENCY_GEOLOCATION = 1000;
    private static final int TIMER_STATE = 180000;
    protected static int TIMER_SUBMIT_REPORT = 40000;
    private static final int TIMER_SUBMIT_REPORT_SPR = 10000;
    private static final int TIMER_VZW_SCBM = 300000;
    private static final int VZW_E911_FALSE = 0;
    private static final int VZW_E911_REREGI = 2;
    private static final int VZW_E911_TRUE = 1;
    /* access modifiers changed from: private */
    public int MAX_RETRANS_COUNT = 3;
    /* access modifiers changed from: private */
    public int MAX_RETRANS_COUNT_SPR = 2;
    private int m3GPP2SendingMsgId = -1;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r7, android.content.Intent r8) {
            /*
                r6 = this;
                java.lang.String r7 = r8.getAction()
                r7.hashCode()
                int r0 = r7.hashCode()
                r1 = 1
                r2 = 0
                r3 = -1
                switch(r0) {
                    case -1926447105: goto L_0x003f;
                    case -1664867553: goto L_0x0034;
                    case -1326089125: goto L_0x0029;
                    case 1262364259: goto L_0x001e;
                    case 2038466647: goto L_0x0013;
                    default: goto L_0x0011;
                }
            L_0x0011:
                r0 = r3
                goto L_0x0049
            L_0x0013:
                java.lang.String r0 = "android.intent.action.DEVICE_STORAGE_FULL"
                boolean r0 = r7.equals(r0)
                if (r0 != 0) goto L_0x001c
                goto L_0x0011
            L_0x001c:
                r0 = 4
                goto L_0x0049
            L_0x001e:
                java.lang.String r0 = "android.intent.action.DEVICE_STORAGE_NOT_FULL"
                boolean r0 = r7.equals(r0)
                if (r0 != 0) goto L_0x0027
                goto L_0x0011
            L_0x0027:
                r0 = 3
                goto L_0x0049
            L_0x0029:
                java.lang.String r0 = "android.intent.action.PHONE_STATE"
                boolean r0 = r7.equals(r0)
                if (r0 != 0) goto L_0x0032
                goto L_0x0011
            L_0x0032:
                r0 = 2
                goto L_0x0049
            L_0x0034:
                java.lang.String r0 = "com.samsung.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED_INTERNAL"
                boolean r0 = r7.equals(r0)
                if (r0 != 0) goto L_0x003d
                goto L_0x0011
            L_0x003d:
                r0 = r1
                goto L_0x0049
            L_0x003f:
                java.lang.String r0 = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED"
                boolean r0 = r7.equals(r0)
                if (r0 != 0) goto L_0x0048
                goto L_0x0011
            L_0x0048:
                r0 = r2
            L_0x0049:
                r4 = 7
                java.lang.String r5 = "mBroadcastReceiver.onReceive: "
                switch(r0) {
                    case 0: goto L_0x0144;
                    case 1: goto L_0x0144;
                    case 2: goto L_0x0097;
                    case 3: goto L_0x0074;
                    case 4: goto L_0x0051;
                    default: goto L_0x004f;
                }
            L_0x004f:
                goto L_0x01b0
            L_0x0051:
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r8 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                r8.mStorageAvailable = r2
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r6 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                com.sec.internal.ims.servicemodules.sms.SmsLogger r6 = r6.mSmsLogger
                java.lang.String r8 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.LOG_TAG
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                r0.append(r5)
                r0.append(r7)
                java.lang.String r7 = r0.toString()
                r6.logAndAdd(r8, r7)
                goto L_0x01b0
            L_0x0074:
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r8 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                r8.mStorageAvailable = r1
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r6 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                com.sec.internal.ims.servicemodules.sms.SmsLogger r6 = r6.mSmsLogger
                java.lang.String r8 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.LOG_TAG
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                r0.append(r5)
                r0.append(r7)
                java.lang.String r7 = r0.toString()
                r6.logAndAdd(r8, r7)
                goto L_0x01b0
            L_0x0097:
                java.lang.String r0 = "subscription"
                int r0 = r8.getIntExtra(r0, r3)
                java.lang.String r1 = "state"
                java.lang.String r8 = r8.getStringExtra(r1)
                if (r0 == r3) goto L_0x00a9
                goto L_0x01b0
            L_0x00a9:
                if (r8 != 0) goto L_0x00b1
                com.android.internal.telephony.PhoneConstants$State r8 = com.android.internal.telephony.PhoneConstants.State.IDLE
                java.lang.String r8 = r8.toString()
            L_0x00b1:
                java.lang.String r0 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.LOG_TAG
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                r1.append(r5)
                r1.append(r7)
                java.lang.String r7 = ", newCallState: "
                r1.append(r7)
                r1.append(r8)
                java.lang.String r7 = r1.toString()
                android.util.Log.d(r0, r7)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean[] r7 = r7.mIsInScbm
                boolean r7 = r7[r2]
                if (r7 == 0) goto L_0x013b
                com.android.internal.telephony.PhoneConstants$State r7 = com.android.internal.telephony.PhoneConstants.State.OFFHOOK
                java.lang.String r7 = r7.toString()
                boolean r7 = r7.equals(r8)
                if (r7 == 0) goto L_0x013b
                com.android.internal.telephony.PhoneConstants$State r7 = com.android.internal.telephony.PhoneConstants.State.OFFHOOK
                java.lang.String r7 = r7.toString()
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r0 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                java.lang.String[] r0 = r0.mCallState
                r0 = r0[r2]
                boolean r7 = r7.equals(r0)
                if (r7 != 0) goto L_0x013b
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                android.content.Context r7 = r7.mContext
                java.lang.String r0 = "telecom"
                java.lang.Object r7 = r7.getSystemService(r0)
                android.telecom.TelecomManager r7 = (android.telecom.TelecomManager) r7
                if (r7 == 0) goto L_0x013b
                boolean r7 = r7.isInEmergencyCall()
                if (r7 == 0) goto L_0x013b
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean[] r7 = r7.mIsInScbm
                r7[r2] = r2
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                android.content.Context r7 = r7.mContext
                com.sec.internal.helper.PreciseAlarmManager r7 = com.sec.internal.helper.PreciseAlarmManager.getInstance(r7)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r0 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                java.lang.Integer r1 = java.lang.Integer.valueOf(r2)
                android.os.Message r0 = r0.obtainMessage(r4, r1)
                r7.removeMessage((android.os.Message) r0)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                android.content.Context r7 = r7.mContext
                com.sec.internal.ims.servicemodules.sms.SmsUtil.broadcastSCBMState(r7, r2, r2)
                java.lang.String r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.LOG_TAG
                java.lang.String r0 = "SCBM timer was removed by E911 Call"
                android.util.Log.d(r7, r0)
            L_0x013b:
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r6 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                java.lang.String[] r6 = r6.mCallState
                r6[r2] = r8
                goto L_0x01b0
            L_0x0144:
                java.lang.String r0 = "phone"
                int r0 = r8.getIntExtra(r0, r2)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r1 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                int r1 = r1.mMaxPhoneCount
                if (r0 >= r1) goto L_0x0153
                goto L_0x0154
            L_0x0153:
                r0 = r2
            L_0x0154:
                java.lang.String r1 = "android.telephony.extra.PHONE_IN_ECM_STATE"
                boolean r8 = r8.getBooleanExtra(r1, r2)
                java.lang.String r1 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.LOG_TAG
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                r3.append(r5)
                r3.append(r7)
                java.lang.String r7 = ", ecmState: "
                r3.append(r7)
                r3.append(r8)
                java.lang.String r7 = ", phoneId: "
                r3.append(r7)
                r3.append(r0)
                java.lang.String r7 = r3.toString()
                android.util.Log.d(r1, r7)
                if (r8 == 0) goto L_0x01b0
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean[] r7 = r7.mIsInScbm
                boolean r7 = r7[r0]
                if (r7 == 0) goto L_0x01b0
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                boolean[] r7 = r7.mIsInScbm
                r7[r0] = r2
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r7 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                android.content.Context r7 = r7.mContext
                com.sec.internal.helper.PreciseAlarmManager r7 = com.sec.internal.helper.PreciseAlarmManager.getInstance(r7)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r8 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                java.lang.Integer r1 = java.lang.Integer.valueOf(r0)
                android.os.Message r8 = r8.obtainMessage(r4, r1)
                r7.removeMessage((android.os.Message) r8)
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule r6 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.this
                android.content.Context r6 = r6.mContext
                com.sec.internal.ims.servicemodules.sms.SmsUtil.broadcastSCBMState(r6, r2, r0)
            L_0x01b0:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.sms.SmsServiceModule.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
        }
    };
    /* access modifiers changed from: private */
    public String[] mCallState;
    Context mContext;
    private ArrayList<SmsEvent> mEmergencyGeolocationPendingQueue;
    private EmergencyGeolocationState mEmergencyGeolocationState = EmergencyGeolocationState.NONE;
    private ArrayList<LinkedList<SmsEvent>> mEmergencyPendingQueue;
    private boolean[] mEmergencyRegiProcessiong;
    /* access modifiers changed from: private */
    public final ISmsServiceInterface mImsService;
    private boolean[] mIsDeregisterTimerRunning;
    private boolean[] mIsDeregistering;
    private boolean mIsGeolocationResetTimerStarted = false;
    /* access modifiers changed from: private */
    public boolean[] mIsInScbm;
    private boolean mIsRetryIfNoSubmitReport = false;
    private String mLastMOContentType = null;
    ConcurrentHashMap<Integer, RemoteCallbackList<ISmsServiceEventListener>> mListeners = new ConcurrentHashMap<>();
    /* access modifiers changed from: private */
    public int mMaxPhoneCount = 1;
    /* access modifiers changed from: private */
    public MessagingAppInfoReceiver mMessagingAppInfoReceiver = null;
    /* access modifiers changed from: private */
    public ConcurrentHashMap<Integer, SmsEvent> mPendingQueue = new ConcurrentHashMap<>();
    /* access modifiers changed from: private */
    public int mRetransCount = 0;
    /* access modifiers changed from: private */
    public SmsLogger mSmsLogger = SmsLogger.getInstance();
    /* access modifiers changed from: private */
    public boolean mStorageAvailable = true;
    /* access modifiers changed from: private */
    public final TelephonyManager mTelephonyManager;
    private Handler mTimeoutHandler = null;

    private static class AlternativeService {
        String mAction;
        String mType;
    }

    enum EmergencyGeolocationState {
        NONE,
        UPDATING,
        UPDATED,
        TIMEOUT
    }

    public void handleIntent(Intent intent) {
    }

    public void sendSMSResponse(boolean z, int i) {
    }

    static {
        String simpleName = SmsServiceModule.class.getSimpleName();
        NAME = simpleName;
        LOG_TAG = simpleName;
    }

    public static class AlternativeServiceXmlParser {
        public static AlternativeService parseXml(String str) throws XPathExpressionException {
            AlternativeService alternativeService = new AlternativeService();
            String r1 = SmsServiceModule.LOG_TAG;
            Log.d(r1, "AlternativeServiceXmlParser parseXml:" + str);
            XPath newXPath = XPathFactory.newInstance().newXPath();
            XPathExpression compile = newXPath.compile("/ims-3gpp/alternative-service");
            XPathExpression compile2 = newXPath.compile("type");
            XPathExpression compile3 = newXPath.compile("reason");
            XPathExpression compile4 = newXPath.compile("action");
            Node node = (Node) compile.evaluate(new InputSource(new StringReader(str)), XPathConstants.NODE);
            if (node == null) {
                return alternativeService;
            }
            String evaluate = compile2.evaluate(node);
            String evaluate2 = compile3.evaluate(node);
            String evaluate3 = compile4.evaluate(node);
            String trim = evaluate.trim();
            String trim2 = evaluate2.trim();
            String trim3 = evaluate3.trim();
            String r3 = SmsServiceModule.LOG_TAG;
            Log.d(r3, "parseXml:" + trim + "," + trim2 + "," + trim3);
            alternativeService.mType = trim;
            alternativeService.mAction = trim3;
            return alternativeService;
        }
    }

    public SmsServiceModule(Looper looper, Context context, ISmsServiceInterface iSmsServiceInterface) {
        super(looper);
        int phoneCount = SimUtil.getPhoneCount();
        this.mMaxPhoneCount = phoneCount;
        this.mEmergencyRegiProcessiong = new boolean[phoneCount];
        this.mIsInScbm = new boolean[phoneCount];
        this.mEmergencyPendingQueue = new ArrayList<>();
        this.mEmergencyGeolocationPendingQueue = new ArrayList<>();
        int i = this.mMaxPhoneCount;
        this.mCallState = new String[i];
        this.mIsDeregisterTimerRunning = new boolean[i];
        this.mIsDeregistering = new boolean[i];
        for (int i2 = 0; i2 < this.mMaxPhoneCount; i2++) {
            this.mEmergencyRegiProcessiong[i2] = false;
            this.mIsInScbm[i2] = false;
            this.mEmergencyPendingQueue.add(new LinkedList());
            this.mCallState[i2] = PhoneConstants.State.IDLE.toString();
            this.mIsDeregisterTimerRunning[i2] = false;
            this.mIsDeregistering[i2] = false;
        }
        this.mContext = context;
        this.mImsService = iSmsServiceInterface;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService(com.sec.internal.constants.ims.os.PhoneConstants.PHONE_KEY);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_DEVICE_STORAGE_FULL);
        intentFilter.addAction(ImsConstants.Intents.ACTION_DEVICE_STORAGE_NOT_FULL);
        intentFilter.addAction(ImsConstants.Intents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        intentFilter.addAction("com.samsung.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED_INTERNAL");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public String[] getServicesRequiring() {
        return new String[]{"smsip"};
    }

    public void init() {
        super.init();
        super.start();
        this.mImsService.registerForSMSEvent(this, 1, (Object) null);
        this.mImsService.registerForRrcConnectionEvent(this, 2, (Object) null);
        if (this.mMessagingAppInfoReceiver == null) {
            MessagingAppInfoReceiver messagingAppInfoReceiver = new MessagingAppInfoReceiver(this.mContext, this);
            this.mMessagingAppInfoReceiver = messagingAppInfoReceiver;
            messagingAppInfoReceiver.registerReceiver();
        }
        this.mTimeoutHandler = new Handler(getLooper()) {
            public void handleMessage(Message message) {
                String r0 = SmsServiceModule.LOG_TAG;
                Log.e(r0, "message timeout - what : " + message.what + ", obj : " + message.obj + ", mRetransCount :" + SmsServiceModule.this.mRetransCount);
                SmsLogger r02 = SmsServiceModule.this.mSmsLogger;
                StringBuilder sb = new StringBuilder();
                sb.append(SmsServiceModule.LOG_TAG);
                sb.append("_TIMEOUT");
                String sb2 = sb.toString();
                r02.add(sb2, "message timeout - what : " + message.what + ", obj : " + message.obj);
                SmsEvent smsEvent = (SmsEvent) message.obj;
                if (smsEvent == null) {
                    Log.e(SmsServiceModule.LOG_TAG, "the pending message doesn't exist");
                    return;
                }
                int phoneId = smsEvent.getImsRegistration() != null ? smsEvent.getImsRegistration().getPhoneId() : 0;
                SmsServiceModule.this.mPendingQueue.remove(Integer.valueOf(smsEvent.getMessageID()));
                Mno simMno = SimUtil.getSimMno(phoneId);
                if (simMno.isOrange() || simMno.isTmobile()) {
                    SmsServiceModule.this.MAX_RETRANS_COUNT = 1;
                }
                int messageID = smsEvent.getMessageID();
                if (smsEvent.getContentType().equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
                    messageID = smsEvent.getTpMr();
                }
                int i = messageID;
                String r3 = SmsServiceModule.LOG_TAG;
                Log.d(r3, "msgId = " + smsEvent.getMessageID() + " tpMR = " + smsEvent.getTpMr());
                int i2 = message.what;
                if (i2 != 1) {
                    if (i2 != 2) {
                        if (i2 == 3 && SmsServiceModule.this.mRetransCount < 1) {
                            SmsServiceModule.this.retryToSendMessage(phoneId, smsEvent);
                        }
                    } else if ((simMno == Mno.DOCOMO || simMno.isOrange() || simMno.isTmobile()) && SmsServiceModule.this.mRetransCount >= SmsServiceModule.this.MAX_RETRANS_COUNT) {
                        if (simMno.isOrange() || simMno.isTmobile()) {
                            int i3 = phoneId;
                            SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(SmsServiceModule.this.mContext, i3, DiagnosisConstants.RCSM_ORST_REGI, 404, (String) null, true);
                            SmsServiceModule.this.onReceiveSMSAckInternal(i3, i, 404, smsEvent.getContentType(), (byte[]) null, SmsServiceModule.this.mRetransCount);
                            return;
                        }
                        SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(SmsServiceModule.this.mContext, phoneId, DiagnosisConstants.RCSM_ORST_REGI, 408, (String) null, true);
                    } else if (!simMno.isSprint() || SmsServiceModule.this.mRetransCount < SmsServiceModule.this.MAX_RETRANS_COUNT_SPR) {
                        SmsServiceModule.this.retryToSendMessage(phoneId, smsEvent);
                    } else {
                        int i4 = phoneId;
                        SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(SmsServiceModule.this.mContext, i4, DiagnosisConstants.RCSM_ORST_REGI, 801, (String) null, true);
                        SmsServiceModule.this.onReceiveSMSAckInternal(i4, i, 801, smsEvent.getContentType(), (byte[]) null, -1);
                    }
                } else if (smsEvent.getState() == 102) {
                    int i5 = phoneId;
                    SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(SmsServiceModule.this.mContext, i5, DiagnosisConstants.RCSM_ORST_REGI, 0, "FF", true);
                    SmsServiceModule.this.onReceiveSMSAckInternal(i5, i, 10003, smsEvent.getContentType(), (byte[]) null, -1);
                }
            }
        };
    }

    public void onConfigured(int i) {
        Log.d(LOG_TAG, "onConfigured:");
        this.mEnabledFeatures[i] = 0;
        if (SimUtil.getSimMno(i).isOrange()) {
            TIMER_SUBMIT_REPORT = Id.NOTIFY_MISC_BASE_ID;
        } else {
            TIMER_SUBMIT_REPORT = 40000;
        }
    }

    public void onSimReady(int i) {
        SmsUtil.broadcastDcnNumber(this.mContext, i);
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        super.onRegistered(imsRegistration);
        int phoneId = imsRegistration.getPhoneId();
        this.mIsDeregistering[phoneId] = false;
        String str = LOG_TAG;
        Log.i(str, "Registered to SMS service. " + imsRegistration);
        updateCapabilities(phoneId);
        this.mImsService.setMsgAppInfoToSipUa(phoneId, this.mMessagingAppInfoReceiver.mMsgAppVersion);
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        String str = LOG_TAG;
        Log.i(str, "Deregistered from SMS service. reason " + i);
        this.mIsDeregistering[imsRegistration.getPhoneId()] = false;
        updateCapabilities(imsRegistration.getPhoneId());
        if (SimUtil.getSimMno(imsRegistration.getPhoneId()) == Mno.BSNL && this.mLastMOContentType != null) {
            fallbackForSpecificReason(NOTI_DEREGISTERED);
        }
        super.onDeregistered(imsRegistration, i);
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        String str = LOG_TAG;
        Log.i(str, "handleMessage() - what : " + message.what);
        switch (message.what) {
            case 1:
                handleSmsEvent((SmsEvent) ((AsyncResult) message.obj).result);
                return;
            case 2:
                handleRRCConnection((RrcConnectionEvent) ((AsyncResult) message.obj).result);
                return;
            case 3:
                SmsEvent smsEvent = (SmsEvent) message.obj;
                sendSMSOverIMS(smsEvent.getEventType(), smsEvent.getData(), smsEvent.getSmscAddr(), smsEvent.getContentType(), smsEvent.getMessageID(), false);
                return;
            case 4:
                SmsEvent smsEvent2 = (SmsEvent) message.obj;
                ImsRegistry.getRegistrationManager().startEmergencyRegistration(smsEvent2.getEventType(), obtainMessage(5, smsEvent2));
                return;
            case 5:
                handleEmergencyRegisterDone((SmsEvent) message.obj);
                return;
            case 6:
                handleEmergencyRegisterFail((SmsEvent) message.obj);
                return;
            case 7:
                int intValue = ((Integer) message.obj).intValue();
                this.mIsInScbm[intValue] = false;
                PreciseAlarmManager.getInstance(this.mContext).removeMessage(obtainMessage(7, Integer.valueOf(intValue)));
                ImsRegistry.getRegistrationManager().stopEmergencyRegistration(intValue);
                SmsUtil.broadcastSCBMState(this.mContext, false, intValue);
                return;
            case 8:
                this.mIsGeolocationResetTimerStarted = false;
                this.mEmergencyGeolocationState = EmergencyGeolocationState.NONE;
                return;
            case 9:
                int intValue2 = ((Integer) message.obj).intValue();
                this.mEmergencyGeolocationState = EmergencyGeolocationState.TIMEOUT;
                IGeolocationController geolocationController = ImsRegistry.getGeolocationController();
                if (geolocationController != null && !geolocationController.updateGeolocationFromLastKnown(intValue2)) {
                    sendPendingEmergencySmsWithGeolocation();
                }
                if (!this.mIsGeolocationResetTimerStarted) {
                    this.mIsGeolocationResetTimerStarted = true;
                    sendMessageDelayed(obtainMessage(8), 1000);
                    return;
                }
                return;
            case 10:
                if (this.mEmergencyGeolocationState == EmergencyGeolocationState.UPDATING) {
                    this.mEmergencyGeolocationState = EmergencyGeolocationState.UPDATED;
                    removeMessages(9);
                }
                sendPendingEmergencySmsWithGeolocation();
                if (!this.mIsGeolocationResetTimerStarted) {
                    this.mIsGeolocationResetTimerStarted = true;
                    sendMessageDelayed(obtainMessage(8), 1000);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void handleSmsEvent(SmsEvent smsEvent) {
        String str = LOG_TAG;
        Log.i(str, "handleSmsEvent coming " + smsEvent.toString());
        int eventType = smsEvent.getEventType();
        if (eventType == 11) {
            onReceiveOtherInfo(smsEvent);
        } else if (eventType != 12) {
            onReceiveSmsMessage(smsEvent);
        } else {
            onReceiveNotiInfo(smsEvent);
        }
    }

    private void handleRRCConnection(RrcConnectionEvent rrcConnectionEvent) {
        String str = LOG_TAG;
        Log.d(str, "rrcEvent.getEvent() : " + rrcConnectionEvent.getEvent());
        if (SimManagerFactory.getSimManager().getSimMno() == Mno.VZW) {
            if ((rrcConnectionEvent.getEvent() == RrcConnectionEvent.RrcEvent.REJECTED || rrcConnectionEvent.getEvent() == RrcConnectionEvent.RrcEvent.TIMER_EXPIRED) && this.mLastMOContentType != null) {
                fallbackForSpecificReason(800);
            }
        }
    }

    private void onReceiveSmsMessage(SmsEvent smsEvent) {
        int i;
        String contentType = smsEvent.getContentType();
        int i2 = GsmSmsUtil.get3gppRPError(smsEvent.getContentType(), smsEvent.getData());
        ImsRegistration imsRegistration = smsEvent.getImsRegistration();
        int phoneId = smsEvent.getPhoneId();
        if (imsRegistration != null) {
            phoneId = imsRegistration.getPhoneId();
            i = imsRegistration.getSubscriptionId();
        } else {
            i = -1;
        }
        int i3 = phoneId;
        int i4 = i;
        String str = LOG_TAG;
        Log.i(str, "onReceiveSmsMessage: errorCode=" + i2);
        if (i2 > 0 || GsmSmsUtil.isAck(contentType, smsEvent.getData())) {
            onReceiveAck(smsEvent, contentType, i3, i4, imsRegistration, i2);
        } else {
            onReceiveIncomingSms(smsEvent, contentType, i3, i4, imsRegistration);
        }
    }

    private void onReceiveIncomingSms(SmsEvent smsEvent, String str, int i, int i2, ImsRegistration imsRegistration) {
        boolean z;
        String subscriberId = TelephonyManagerExt.getSubscriberId(this.mTelephonyManager, i2);
        boolean z2 = false;
        if (SimUtil.getSimMno(i) == Mno.VZW && imsRegistration != null) {
            if (TextUtils.isEmpty(subscriberId) || !imsRegistration.isImsiBased(subscriberId)) {
                z = false;
            } else {
                Log.d(LOG_TAG, "onReceiveIncomingSms: isLimitedRegi = true");
                z = true;
            }
            if (this.mIsInScbm[i]) {
                if (str.equals(GsmSmsUtil.CONTENT_TYPE_3GPP) && smsEvent.getData() != null && GsmSmsUtil.is911FromPdu(GsmSmsUtil.get3gppTpduFromPdu(smsEvent.getData()))) {
                    z2 = true;
                }
                if (imsRegistration.getImsProfile().hasEmergencySupport() || z2) {
                    PreciseAlarmManager.getInstance(this.mContext).removeMessage(obtainMessage(7, Integer.valueOf(i)));
                    PreciseAlarmManager.getInstance(this.mContext).sendMessageDelayed(getClass().getSimpleName(), obtainMessage(7, Integer.valueOf(i)), 300000);
                }
            }
            z2 = z;
        }
        if (str.equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
            if (!z2 || smsEvent.getData() == null || GsmSmsUtil.isAdminMsg(GsmSmsUtil.get3gppTpduFromPdu(smsEvent.getData()))) {
                onReceive3GPPIncomingSms(smsEvent);
            } else {
                this.mImsService.sendSMSResponse(i, smsEvent.getCallID(), 404);
                return;
            }
        } else if (!str.equals(CdmaSmsUtil.CONTENT_TYPE_3GPP2)) {
            SmsUtil.sendSmotInfoToHQM(this.mContext, i, "1", "SSM_onReceiveIncomingSms_noContentType", true);
        } else if (z2 && smsEvent.getData() != null && !CdmaSmsUtil.isAdminMsg(smsEvent.getData())) {
            this.mImsService.sendSMSResponse(i, smsEvent.getCallID(), 404);
            return;
        } else if (!this.mStorageAvailable) {
            this.mSmsLogger.logAndAdd(LOG_TAG, "incoming sms but mStorageAvailable = false");
            this.mImsService.sendSMSResponse(i, smsEvent.getCallID(), 480);
            onReceive3GPP2IncomingSms(smsEvent);
            return;
        } else {
            onReceive3GPP2IncomingSms(smsEvent);
        }
        this.mImsService.sendSMSResponse(i, smsEvent.getCallID(), 200);
    }

    private void onReceive3GPPIncomingSms(SmsEvent smsEvent) {
        RemoteCallbackList remoteCallbackList;
        ImsRegistration imsRegistration = smsEvent.getImsRegistration();
        int i = 0;
        int phoneId = imsRegistration != null ? imsRegistration.getPhoneId() : 0;
        if (smsEvent.getData() == null || smsEvent.getCallID() == null || smsEvent.getSmscAddr() == null) {
            SmsUtil.sendSmotInfoToHQM(this.mContext, phoneId, "1", "SSM_onReceive3GPPIncomingSms_WrongFormat", true);
            return;
        }
        byte[] bArr = GsmSmsUtil.get3gppTpduFromPdu(smsEvent.getData());
        if (bArr == null) {
            Log.e(LOG_TAG, "incoming tpdu is null. send RP Error report" + smsEvent.getCallID() + "] SmscAddr [" + smsEvent.getSmscAddr() + "]");
            SmsUtil.sendSmotInfoToHQM(this.mContext, phoneId, "1", "SSM_onReceive3GPPIncomingSms_tPduNull", true);
            String trimSipAddr = GsmSmsUtil.trimSipAddr(smsEvent.getSmscAddr());
            byte[] makeRPErrorPdu = GsmSmsUtil.makeRPErrorPdu(smsEvent.getData());
            if (makeRPErrorPdu == null) {
                SmsUtil.sendSmotInfoToHQM(this.mContext, phoneId, "1", "SSM_onReceive3GPPIncomingSms_deliverPduNull", true);
                return;
            }
            this.mLastMOContentType = GsmSmsUtil.CONTENT_TYPE_3GPP;
            ISmsServiceInterface iSmsServiceInterface = this.mImsService;
            String localUri = SmsUtil.getLocalUri(imsRegistration);
            String callID = smsEvent.getCallID();
            if (smsEvent.getImsRegistration() != null) {
                i = smsEvent.getImsRegistration().getHandle();
            }
            iSmsServiceInterface.sendMessage(trimSipAddr, localUri, GsmSmsUtil.CONTENT_TYPE_3GPP, makeRPErrorPdu, true, callID, 0, i, false);
        } else if (smsEvent.getData().length <= 1) {
            SmsUtil.sendSmotInfoToHQM(this.mContext, phoneId, "1", "SSM_onReceive3GPPIncomingSms_DataError", true);
        } else {
            SmsEvent smsEvent2 = new SmsEvent();
            smsEvent2.setContentType(smsEvent.getContentType());
            smsEvent2.setRpRef(smsEvent.getData()[1] & 255);
            smsEvent2.setSmscAddr(GsmSmsUtil.trimSipAddr(GsmSmsUtil.removeDisplayName(smsEvent.getSmscAddr())));
            smsEvent2.setMessageID(smsEvent.getMessageID() & 255);
            smsEvent2.setCallID(smsEvent.getCallID());
            smsEvent2.setData(bArr);
            if (!(smsEvent2.getRpRef() == -1 || smsEvent2.getSmscAddr() == null)) {
                if (GsmSmsUtil.isStatusReport(bArr)) {
                    smsEvent2.setMessageID(SmsUtil.getNewMsgId() & 255);
                    smsEvent2.setState(104);
                    Handler handler = this.mTimeoutHandler;
                    if (handler != null) {
                        handler.sendMessageDelayed(handler.obtainMessage(1, smsEvent2), 180000);
                    }
                } else {
                    smsEvent2.setMessageID(SmsUtil.getNewMsgId() & 255);
                    smsEvent2.setState(103);
                    byte[] tPPidDcsFromPdu = GsmSmsUtil.getTPPidDcsFromPdu(bArr);
                    if (tPPidDcsFromPdu != null) {
                        smsEvent2.setTpPid(tPPidDcsFromPdu[0]);
                        smsEvent2.setTpDcs(tPPidDcsFromPdu[1]);
                        Log.i(LOG_TAG, "Incoming SMS new setMessageID : " + smsEvent2.getMessageID() + " TpPid : " + smsEvent2.getTpPid() + " TpDcs : " + smsEvent2.getTpDcs());
                    }
                    Handler handler2 = this.mTimeoutHandler;
                    if (handler2 != null) {
                        handler2.sendMessageDelayed(handler2.obtainMessage(1, smsEvent2), 180000);
                    }
                }
                this.mPendingQueue.put(Integer.valueOf(smsEvent2.getMessageID()), smsEvent2);
            }
            SmsLogger smsLogger = this.mSmsLogger;
            String str = LOG_TAG;
            smsLogger.logAndAdd(str, "onReceive3GPPIncomingSms: " + smsEvent2);
            Log.i(str + '/' + phoneId, "onReceive3GPPIncomingSms");
            IMSLog.c(LogClass.SMS_RECEIVE_MSG_3GPP, phoneId + "," + smsEvent2.toKeyDump());
            SmsUtil.storeMtSmsInfoOfDrcsToImsLogAgent(this.mContext, phoneId);
            if (this.mListeners.containsKey(Integer.valueOf(phoneId)) && (remoteCallbackList = this.mListeners.get(Integer.valueOf(phoneId))) != null) {
                try {
                    int beginBroadcast = remoteCallbackList.beginBroadcast();
                    while (beginBroadcast > 0) {
                        int i2 = beginBroadcast - 1;
                        try {
                            remoteCallbackList.getBroadcastItem(i2).onReceiveIncomingSMS(smsEvent2.getMessageID(), smsEvent2.getContentType(), smsEvent2.getData());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        beginBroadcast = i2;
                    }
                } catch (IllegalStateException e2) {
                    e2.printStackTrace();
                } catch (Throwable th) {
                    remoteCallbackList.finishBroadcast();
                    throw th;
                }
                remoteCallbackList.finishBroadcast();
            }
        }
    }

    private void onReceive3GPP2IncomingSms(SmsEvent smsEvent) {
        RemoteCallbackList remoteCallbackList;
        ImsRegistration imsRegistration = smsEvent.getImsRegistration();
        int phoneId = imsRegistration != null ? imsRegistration.getPhoneId() : 0;
        if (smsEvent.getData() == null) {
            SmsUtil.sendSmotInfoToHQM(this.mContext, phoneId, "1", "SSM_onReceive3GPP2IncomingSms_WrongFormat", true);
        } else if (!CdmaSmsUtil.isValid3GPP2PDU(smsEvent.getData())) {
            SmsUtil.sendSmotInfoToHQM(this.mContext, phoneId, "1", "SSM_onReceive3GPP2IncomingSms_InvalidPdu", true);
        } else {
            SmsLogger smsLogger = this.mSmsLogger;
            String str = LOG_TAG;
            smsLogger.logAndAdd(str, "onReceive3GPP2IncomingSms: " + smsEvent);
            Log.i(str + '/' + phoneId, "onReceive3GPP2IncomingSms");
            IMSLog.c(LogClass.SMS_RECEIVE_MSG_3GPP2, phoneId + "," + smsEvent.toKeyDump());
            SmsUtil.storeMtSmsInfoOfDrcsToImsLogAgent(this.mContext, phoneId);
            if (this.mListeners.containsKey(Integer.valueOf(phoneId)) && (remoteCallbackList = this.mListeners.get(Integer.valueOf(phoneId))) != null) {
                try {
                    int beginBroadcast = remoteCallbackList.beginBroadcast();
                    while (beginBroadcast > 0) {
                        beginBroadcast--;
                        try {
                            remoteCallbackList.getBroadcastItem(beginBroadcast).onReceiveIncomingSMS(smsEvent.getMessageID(), smsEvent.getContentType(), smsEvent.getData());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IllegalStateException e2) {
                    e2.printStackTrace();
                } catch (Throwable th) {
                    remoteCallbackList.finishBroadcast();
                    throw th;
                }
                remoteCallbackList.finishBroadcast();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0026, code lost:
        r9 = com.sec.internal.ims.servicemodules.sms.SmsUtil.getMessageIdByCallId(r7.mPendingQueue, r12);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onReceiveAck(com.sec.internal.ims.servicemodules.sms.SmsEvent r8, java.lang.String r9, int r10, int r11, com.sec.ims.ImsRegistration r12, int r13) {
        /*
            r7 = this;
            r11 = -1
            if (r12 != 0) goto L_0x001a
            int r12 = r8.getReasonCode()
            r0 = 408(0x198, float:5.72E-43)
            if (r12 != r0) goto L_0x001a
            r7.m3GPP2SendingMsgId = r11
            android.content.Context r1 = r7.mContext
            java.lang.String r3 = "3"
            r4 = 408(0x198, float:5.72E-43)
            r5 = 0
            r6 = 0
            r2 = r10
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(r1, r2, r3, r4, r5, r6)
            return
        L_0x001a:
            java.lang.String r12 = r8.getCallID()
            byte[] r0 = r8.getData()
            if (r0 != 0) goto L_0x0045
            if (r12 == 0) goto L_0x003f
            java.util.concurrent.ConcurrentHashMap<java.lang.Integer, com.sec.internal.ims.servicemodules.sms.SmsEvent> r9 = r7.mPendingQueue
            int r9 = com.sec.internal.ims.servicemodules.sms.SmsUtil.getMessageIdByCallId(r9, r12)
            if (r9 <= r11) goto L_0x003f
            java.util.concurrent.ConcurrentHashMap<java.lang.Integer, com.sec.internal.ims.servicemodules.sms.SmsEvent> r12 = r7.mPendingQueue
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)
            java.lang.Object r9 = r12.get(r9)
            com.sec.internal.ims.servicemodules.sms.SmsEvent r9 = (com.sec.internal.ims.servicemodules.sms.SmsEvent) r9
            int r9 = r9.getTpMr()
            goto L_0x0040
        L_0x003f:
            r9 = r11
        L_0x0040:
            boolean r12 = r7.onReceiveSipResponse(r8)
            goto L_0x008e
        L_0x0045:
            java.lang.String r12 = "application/vnd.3gpp.sms"
            boolean r12 = r9.equals(r12)
            if (r12 == 0) goto L_0x007f
            com.sec.internal.constants.Mno r9 = com.sec.internal.helper.SimUtil.getSimMno(r10)
            com.sec.internal.constants.Mno r12 = com.sec.internal.constants.Mno.KT
            if (r9 != r12) goto L_0x0079
            boolean r9 = com.sec.internal.ims.servicemodules.sms.GsmSmsUtil.isRPErrorForRetransmission(r13)
            if (r9 == 0) goto L_0x0079
            android.content.Context r0 = r7.mContext
            java.lang.String r2 = "2"
            r3 = 0
            byte r9 = (byte) r13
            java.lang.Byte r9 = java.lang.Byte.valueOf(r9)
            java.lang.Object[] r9 = new java.lang.Object[]{r9}
            java.lang.String r12 = "%02X"
            java.lang.String r4 = java.lang.String.format(r12, r9)
            r5 = 1
            r1 = r10
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(r0, r1, r2, r3, r4, r5)
            boolean r9 = r7.onReceive3GPPSmsRpError(r8)
            goto L_0x007d
        L_0x0079:
            boolean r9 = r7.onReceive3GPPSmsAck(r8)
        L_0x007d:
            r12 = r9
            goto L_0x008d
        L_0x007f:
            java.lang.String r12 = "application/vnd.3gpp2.sms"
            boolean r9 = r9.equals(r12)
            if (r9 == 0) goto L_0x008c
            boolean r12 = r7.onReceive3GPP2SmsAck(r8)
            goto L_0x008d
        L_0x008c:
            r12 = 0
        L_0x008d:
            r9 = r11
        L_0x008e:
            if (r12 != 0) goto L_0x00ef
            if (r13 <= 0) goto L_0x00b1
            int r12 = com.sec.internal.ims.servicemodules.sms.GsmSmsUtil.getRilRPErrCode(r13)
            r8.setReasonCode(r12)
            byte[] r12 = r8.getData()
            byte[] r12 = com.sec.internal.ims.servicemodules.sms.GsmSmsUtil.get3gppTpduFromPdu(r12)
            r8.setData(r12)
            boolean r12 = com.sec.internal.helper.os.ImsGateConfig.isGateEnabled()
            if (r12 == 0) goto L_0x00b1
            java.lang.String r12 = "GATE"
            java.lang.String r13 = "<GATE-M>SMS_GENERIC_FAILURE</GATE-M>"
            com.sec.internal.log.IMSLog.g(r12, r13)
        L_0x00b1:
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = LOG_TAG
            r12.append(r13)
            r13 = 47
            r12.append(r13)
            r12.append(r10)
            java.lang.String r12 = r12.toString()
            java.lang.String r13 = "onReceiveAck"
            android.util.Log.i(r12, r13)
            int r12 = r8.getTpMr()
            if (r12 != 0) goto L_0x00d3
            goto L_0x00d7
        L_0x00d3:
            int r9 = r8.getTpMr()
        L_0x00d7:
            r2 = r9
            int r3 = r8.getReasonCode()
            java.lang.String r4 = r8.getContentType()
            byte[] r5 = r8.getData()
            int r6 = r8.getRetryAfter()
            r0 = r7
            r1 = r10
            r0.broadcastOnReceiveSMSAck(r1, r2, r3, r4, r5, r6)
            r7.m3GPP2SendingMsgId = r11
        L_0x00ef:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.sms.SmsServiceModule.onReceiveAck(com.sec.internal.ims.servicemodules.sms.SmsEvent, java.lang.String, int, int, com.sec.ims.ImsRegistration, int):void");
    }

    private boolean onReceiveSipResponse(SmsEvent smsEvent) {
        String callID = smsEvent.getCallID();
        ImsRegistration imsRegistration = smsEvent.getImsRegistration();
        int phoneId = smsEvent.getPhoneId();
        if (imsRegistration != null) {
            phoneId = imsRegistration.getPhoneId();
        }
        int i = phoneId;
        Mno simMno = SimUtil.getSimMno(i);
        if (!simMno.isEur() && smsEvent.getReasonCode() == 708) {
            smsEvent.setReasonCode(408);
        }
        SmsLogger smsLogger = this.mSmsLogger;
        String str = LOG_TAG;
        smsLogger.logAndAdd(str, "onReceiveSipResponse: " + smsEvent);
        IMSLog.c(LogClass.SMS_RECEIVE_SIP_RESPONSE, i + "," + smsEvent.toKeyDump());
        int messageIdByCallId = callID != null ? SmsUtil.getMessageIdByCallId(this.mPendingQueue, smsEvent.getCallID()) : -1;
        if (messageIdByCallId >= 0) {
            SmsEvent remove = this.mPendingQueue.remove(Integer.valueOf(messageIdByCallId));
            int state = remove.getState();
            if (state == 101) {
                return handleMOReceivingCallID(smsEvent, remove, imsRegistration, i, simMno);
            }
            if (state != 106) {
                return false;
            }
            handleMTReceivingDeliverReportAck(smsEvent, remove, imsRegistration, i);
            return true;
        } else if (smsEvent.getData() != null) {
            return false;
        } else {
            Log.i(str + '/' + i, "onReceiveSipResponse");
            int reasonCode = smsEvent.getReasonCode();
            String reason = smsEvent.getReason();
            SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(this.mContext, i, "1", reasonCode, (String) null, true);
            if (simMno == Mno.VZW && reasonCode == 503 && !TextUtils.isEmpty(reason) && reason.contains("Outage")) {
                reasonCode = NOTI_503_OUTAGE;
            }
            int i2 = reasonCode;
            int i3 = this.m3GPP2SendingMsgId;
            if (i3 < 0) {
                i3 = smsEvent.getMessageID();
            }
            this.m3GPP2SendingMsgId = -1;
            broadcastOnReceiveSMSAck(i, i3, i2, CdmaSmsUtil.CONTENT_TYPE_3GPP2, (byte[]) null, -1);
            if (smsEvent.getReasonCode() < 300 || imsRegistration == null) {
                return true;
            }
            SmsUtil.onSipError(imsRegistration, smsEvent.getReasonCode(), smsEvent.getReason());
            return true;
        }
    }

    private boolean onReceive3GPPSmsRpError(SmsEvent smsEvent) {
        ImsRegistration imsRegistration = smsEvent.getImsRegistration();
        int phoneId = imsRegistration != null ? imsRegistration.getPhoneId() : 0;
        if (smsEvent.getData() != null) {
            SmsEvent remove = smsEvent.getData().length > 0 ? this.mPendingQueue.remove(Integer.valueOf(SmsUtil.getMessageIdByPdu(this.mPendingQueue, smsEvent.getData()))) : null;
            if (remove == null) {
                Log.e(LOG_TAG, "unexpected RP-ERROR");
                return false;
            }
            SmsLogger smsLogger = this.mSmsLogger;
            String str = LOG_TAG;
            smsLogger.logAndAdd(str, "onReceive3GPPSmsRpError: " + remove);
            IMSLog.c(LogClass.SMS_RECEIVE_3GPP_RP_ERR, phoneId + "," + remove.toKeyDump());
            Handler handler = this.mTimeoutHandler;
            if (handler != null) {
                handler.removeMessages(1, remove);
                if (this.mIsRetryIfNoSubmitReport) {
                    this.mTimeoutHandler.removeMessages(2, remove);
                }
            }
            if (this.mRetransCount < 1) {
                Log.i(str, "retry to send message on RP-ERROR");
                Handler handler2 = this.mTimeoutHandler;
                if (handler2 != null) {
                    handler2.sendMessage(handler2.obtainMessage(3, remove));
                }
                this.mPendingQueue.put(Integer.valueOf(remove.getMessageID()), remove);
                return true;
            }
        }
        return false;
    }

    private boolean onReceive3GPPSmsAck(SmsEvent smsEvent) {
        SmsEvent smsEvent2;
        int i;
        String str = Build.TYPE;
        if (("eng".equals(str) || "userdebug".equals(str)) && ConfigConstants.VALUE.INFO_COMPLETED.equals(SemSystemProperties.get("ro.product_ship", CloudMessageProviderContract.JsonData.TRUE)) && CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ril.ims.smstest.ignoreack", ConfigConstants.VALUE.INFO_COMPLETED))) {
            Log.i(LOG_TAG, "Ignore ack for test");
            SemSystemProperties.set("ril.ims.smstest.ignoreack", ConfigConstants.VALUE.INFO_COMPLETED);
            return true;
        }
        ImsRegistration imsRegistration = smsEvent.getImsRegistration();
        int phoneId = imsRegistration != null ? imsRegistration.getPhoneId() : 0;
        if (smsEvent.getData() != null) {
            if (smsEvent.getData().length > 0) {
                i = SmsUtil.getMessageIdByPdu(this.mPendingQueue, smsEvent.getData());
                smsEvent2 = this.mPendingQueue.remove(Integer.valueOf(i));
            } else {
                smsEvent2 = null;
                i = -1;
            }
            SmsEvent smsEvent3 = smsEvent2;
            if (smsEvent3 == null) {
                this.mSmsLogger.logAndAdd(LOG_TAG, "unexpected SUBMIT report - pendingMessage is null");
                return false;
            }
            int state = smsEvent3.getState();
            if (state < 100 || state > 102) {
                SmsLogger smsLogger = this.mSmsLogger;
                String str2 = LOG_TAG;
                smsLogger.logAndAdd(str2, "unexpected SUBMIT report - pendingState is " + state);
                this.mPendingQueue.put(Integer.valueOf(i), smsEvent3);
            } else {
                Handler handler = this.mTimeoutHandler;
                if (handler != null) {
                    handler.removeMessages(1, smsEvent3);
                    if (this.mIsRetryIfNoSubmitReport) {
                        this.mTimeoutHandler.removeMessages(2, smsEvent3);
                    }
                }
                smsEvent3.setData(GsmSmsUtil.get3gppTpduFromPdu(smsEvent.getData()));
                smsEvent3.setContentType(smsEvent.getContentType());
                smsEvent3.setRetryAfter(smsEvent.getRetryAfter());
                int i2 = GsmSmsUtil.get3gppRPError(smsEvent.getContentType(), smsEvent.getData());
                if (i2 > 0) {
                    SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(this.mContext, phoneId, "2", 0, String.format("%02X", new Object[]{Byte.valueOf((byte) i2)}), true);
                    smsEvent3.setReasonCode(GsmSmsUtil.getRilRPErrCode(i2));
                } else {
                    SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(this.mContext, phoneId, "0", 0, "00", true);
                    smsEvent3.setReasonCode(0);
                }
                SmsLogger smsLogger2 = this.mSmsLogger;
                String str3 = LOG_TAG;
                smsLogger2.logAndAdd(str3, "onReceive3GPPSmsAck: " + smsEvent3);
                IMSLog.c(LogClass.SMS_RECEIVE_3GPP_ACK, phoneId + "," + smsEvent3.toKeyDump());
                if (GsmSmsUtil.isAck(smsEvent.getContentType(), smsEvent.getData())) {
                    synchronized (this.mListeners) {
                        Log.i(str3 + '/' + phoneId, "onReceive3GPPSmsAck");
                        broadcastOnReceiveSMSAck(phoneId, smsEvent3.getTpMr(), smsEvent3.getReasonCode(), smsEvent3.getContentType(), smsEvent3.getData(), smsEvent3.getRetryAfter());
                    }
                    return true;
                } else if (smsEvent.getTpMr() == 0) {
                    smsEvent.setTpMr(smsEvent3.getTpMr());
                }
            }
        }
        return false;
    }

    private boolean onReceive3GPP2SmsAck(SmsEvent smsEvent) {
        int reasonCode = smsEvent.getReasonCode();
        if (reasonCode == 100) {
            return true;
        }
        ImsRegistration imsRegistration = smsEvent.getImsRegistration();
        int phoneId = imsRegistration != null ? imsRegistration.getPhoneId() : 0;
        int i = this.m3GPP2SendingMsgId;
        if (i < 0) {
            i = smsEvent.getMessageID();
        }
        int i2 = i;
        int i3 = reasonCode;
        SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(this.mContext, phoneId, "1", i3, (String) null, true);
        Log.i(LOG_TAG + '/' + phoneId, "onReceive3GPP2SmsAck");
        broadcastOnReceiveSMSAck(phoneId, i2, i3, smsEvent.getContentType(), smsEvent.getData(), smsEvent.getRetryAfter());
        return true;
    }

    private void onReceiveNotiInfo(SmsEvent smsEvent) {
        int messageID = smsEvent.getMessageID();
        if (messageID >= 0) {
            SmsEvent remove = this.mPendingQueue.remove(Integer.valueOf(messageID));
            if (remove != null) {
                int state = remove.getState();
                if (state == 100) {
                    remove.setState(101);
                    remove.setCallID(smsEvent.getCallID());
                    this.mPendingQueue.put(Integer.valueOf(messageID), remove);
                } else if (state == 105) {
                    remove.setState(106);
                    remove.setCallID(smsEvent.getCallID());
                    this.mPendingQueue.put(Integer.valueOf(messageID), remove);
                }
            } else {
                Log.e(LOG_TAG, "no pending message");
            }
        }
    }

    private void onReceiveOtherInfo(SmsEvent smsEvent) {
        int messageID = smsEvent.getMessageID();
        String contentType = smsEvent.getContentType();
        if (messageID >= 0 && smsEvent.getReasonCode() == NOTI_DEREGISTERED) {
            String str = LOG_TAG;
            Log.e(str, "cannot send message as NOTI_DEREGISTERED");
            ImsRegistration imsRegistration = smsEvent.getImsRegistration();
            int phoneId = imsRegistration != null ? imsRegistration.getPhoneId() : 0;
            if (contentType.equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
                SmsEvent remove = this.mPendingQueue.remove(Integer.valueOf(messageID));
                if (remove == null) {
                    Log.e(str, "no pending message");
                    return;
                }
                Log.d(str, "remove pending message");
                remove.setReasonCode(NOTI_DEREGISTERED);
                remove.setRetryAfter(-1);
                this.m3GPP2SendingMsgId = messageID;
                messageID = remove.getTpMr();
            }
            int i = messageID;
            SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(this.mContext, phoneId, "1", NOTI_DEREGISTERED, (String) null, false);
            Log.i(str + '/' + phoneId, "onReceiveOtherInfo");
            broadcastOnReceiveSMSAck(phoneId, i, NOTI_DEREGISTERED, contentType, (byte[]) null, -1);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d0, code lost:
        if (ASVC_INITIAL_REGISTRATION.equals(r0.mAction) != false) goto L_0x00f7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f5, code lost:
        if (r16.getReasonCode() == 408) goto L_0x00f7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0123, code lost:
        if (ASVC_INITIAL_REGISTRATION.equals(r0.mAction) != false) goto L_0x00f7;
     */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0128  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0132  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleMOReceivingCallID(com.sec.internal.ims.servicemodules.sms.SmsEvent r16, com.sec.internal.ims.servicemodules.sms.SmsEvent r17, com.sec.ims.ImsRegistration r18, int r19, com.sec.internal.constants.Mno r20) {
        /*
            r15 = this;
            r1 = r15
            r2 = r17
            r3 = r18
            r0 = r20
            int r4 = r16.getReasonCode()
            r5 = 100
            r8 = 1
            if (r4 != r5) goto L_0x0011
            return r8
        L_0x0011:
            int r4 = r16.getReasonCode()
            r5 = 200(0xc8, float:2.8E-43)
            r6 = 300(0x12c, float:4.2E-43)
            r7 = 0
            if (r4 < r5) goto L_0x0086
            int r4 = r16.getReasonCode()
            if (r4 >= r6) goto L_0x0086
            r3 = 102(0x66, float:1.43E-43)
            r2.setState(r3)
            java.lang.String r3 = android.os.Build.TYPE
            java.lang.String r4 = "eng"
            boolean r4 = r4.equals(r3)
            if (r4 != 0) goto L_0x003a
            java.lang.String r4 = "userdebug"
            boolean r3 = r4.equals(r3)
            if (r3 == 0) goto L_0x005a
        L_0x003a:
            java.lang.String r3 = "ro.product_ship"
            java.lang.String r4 = "true"
            java.lang.String r3 = android.os.SemSystemProperties.get(r3, r4)
            java.lang.String r5 = "false"
            boolean r3 = r5.equals(r3)
            if (r3 == 0) goto L_0x005a
            java.lang.String r3 = "ril.ims.smstest.ignoreack"
            java.lang.String r3 = android.os.SemSystemProperties.get(r3, r5)
            boolean r3 = r4.equals(r3)
            if (r3 == 0) goto L_0x005a
            r7 = r8
        L_0x005a:
            android.os.Handler r3 = r1.mTimeoutHandler
            if (r3 == 0) goto L_0x0078
            boolean r4 = r1.mIsRetryIfNoSubmitReport
            if (r4 == 0) goto L_0x0078
            if (r7 != 0) goto L_0x0078
            r4 = 2
            android.os.Message r4 = r3.obtainMessage(r4, r2)
            boolean r0 = r20.isSprint()
            if (r0 == 0) goto L_0x0072
            r5 = 10000(0x2710, double:4.9407E-320)
            goto L_0x0075
        L_0x0072:
            int r0 = TIMER_SUBMIT_REPORT
            long r5 = (long) r0
        L_0x0075:
            r3.sendMessageDelayed(r4, r5)
        L_0x0078:
            java.util.concurrent.ConcurrentHashMap<java.lang.Integer, com.sec.internal.ims.servicemodules.sms.SmsEvent> r0 = r1.mPendingQueue
            int r1 = r17.getMessageID()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r0.put(r1, r2)
            return r8
        L_0x0086:
            int r4 = r16.getReasonCode()
            if (r4 < r6) goto L_0x0176
            if (r3 == 0) goto L_0x0176
            android.content.Context r9 = r1.mContext
            java.lang.String r11 = "1"
            int r12 = r16.getReasonCode()
            r13 = 0
            r14 = 1
            r10 = r19
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(r9, r10, r11, r12, r13, r14)
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.KT
            java.lang.String r5 = "initial-registration"
            java.lang.String r6 = "restoration"
            java.lang.String r9 = "application/3gpp-ims+xml"
            r10 = 504(0x1f8, float:7.06E-43)
            if (r0 != r4) goto L_0x00f9
            int r0 = r16.getReasonCode()
            if (r0 != r10) goto L_0x00ef
            java.lang.String r0 = r16.getContentType()
            boolean r0 = r9.equals(r0)
            if (r0 == 0) goto L_0x00ef
            java.lang.String r0 = r16.getContent()     // Catch:{ XPathExpressionException -> 0x00d3 }
            com.sec.internal.ims.servicemodules.sms.SmsServiceModule$AlternativeService r0 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.AlternativeServiceXmlParser.parseXml(r0)     // Catch:{ XPathExpressionException -> 0x00d3 }
            java.lang.String r4 = r0.mType     // Catch:{ XPathExpressionException -> 0x00d3 }
            boolean r4 = r6.equals(r4)     // Catch:{ XPathExpressionException -> 0x00d3 }
            if (r4 == 0) goto L_0x0126
            java.lang.String r0 = r0.mAction     // Catch:{ XPathExpressionException -> 0x00d3 }
            boolean r0 = r5.equals(r0)     // Catch:{ XPathExpressionException -> 0x00d3 }
            if (r0 == 0) goto L_0x0126
            goto L_0x00f7
        L_0x00d3:
            r0 = move-exception
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "onReceiveSipResponse: XPath expression failed :"
            r5.append(r6)
            java.lang.String r0 = r0.getMessage()
            r5.append(r0)
            java.lang.String r0 = r5.toString()
            android.util.Log.e(r4, r0)
            goto L_0x0126
        L_0x00ef:
            int r0 = r16.getReasonCode()
            r4 = 408(0x198, float:5.72E-43)
            if (r0 != r4) goto L_0x0126
        L_0x00f7:
            r7 = r8
            goto L_0x0126
        L_0x00f9:
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.EDF
            if (r0 != r4) goto L_0x0126
            int r0 = r16.getReasonCode()
            if (r0 != r10) goto L_0x0126
            java.lang.String r0 = r16.getContentType()
            boolean r0 = r9.equals(r0)
            if (r0 == 0) goto L_0x0126
            java.lang.String r0 = r16.getContent()     // Catch:{ XPathExpressionException -> 0x0126 }
            com.sec.internal.ims.servicemodules.sms.SmsServiceModule$AlternativeService r0 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.AlternativeServiceXmlParser.parseXml(r0)     // Catch:{ XPathExpressionException -> 0x0126 }
            java.lang.String r4 = r0.mType     // Catch:{ XPathExpressionException -> 0x0126 }
            boolean r4 = r6.equals(r4)     // Catch:{ XPathExpressionException -> 0x0126 }
            if (r4 == 0) goto L_0x0126
            java.lang.String r0 = r0.mAction     // Catch:{ XPathExpressionException -> 0x0126 }
            boolean r0 = r5.equals(r0)     // Catch:{ XPathExpressionException -> 0x0126 }
            if (r0 == 0) goto L_0x0126
            goto L_0x00f7
        L_0x0126:
            if (r7 == 0) goto L_0x0132
            int r0 = r16.getReasonCode()
            java.lang.String r4 = "initial_registration"
            com.sec.internal.ims.servicemodules.sms.SmsUtil.onSipError(r3, r0, r4)
            goto L_0x013d
        L_0x0132:
            int r0 = r16.getReasonCode()
            java.lang.String r4 = r16.getReason()
            com.sec.internal.ims.servicemodules.sms.SmsUtil.onSipError(r3, r0, r4)
        L_0x013d:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r3 = LOG_TAG
            r0.append(r3)
            r3 = 47
            r0.append(r3)
            r3 = r19
            r0.append(r3)
            java.lang.String r0 = r0.toString()
            java.lang.String r4 = "onReceiveSipResponse"
            android.util.Log.i(r0, r4)
            int r0 = r17.getTpMr()
            int r4 = r16.getReasonCode()
            java.lang.String r5 = r16.getContentType()
            byte[] r6 = r16.getData()
            int r7 = r16.getRetryAfter()
            r1 = r15
            r2 = r19
            r3 = r0
            r1.broadcastOnReceiveSMSAck(r2, r3, r4, r5, r6, r7)
            return r8
        L_0x0176:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.sms.SmsServiceModule.handleMOReceivingCallID(com.sec.internal.ims.servicemodules.sms.SmsEvent, com.sec.internal.ims.servicemodules.sms.SmsEvent, com.sec.ims.ImsRegistration, int, com.sec.internal.constants.Mno):boolean");
    }

    private void handleMTReceivingDeliverReportAck(SmsEvent smsEvent, SmsEvent smsEvent2, ImsRegistration imsRegistration, int i) {
        RemoteCallbackList remoteCallbackList;
        Handler handler = this.mTimeoutHandler;
        if (handler != null) {
            handler.removeMessages(1, smsEvent2);
        }
        if (smsEvent.getReasonCode() >= 300 && imsRegistration != null) {
            if (smsEvent.getRetryAfter() > 0) {
                this.mPendingQueue.put(Integer.valueOf(smsEvent2.getMessageID()), smsEvent2);
                Handler handler2 = this.mTimeoutHandler;
                if (handler2 != null) {
                    handler2.sendMessageDelayed(handler2.obtainMessage(1, smsEvent2), 180000);
                }
            }
            SmsUtil.onSipError(imsRegistration, smsEvent.getReasonCode(), smsEvent.getReason());
        }
        Log.i(LOG_TAG + '/' + i, "onReceiveSipResponse");
        if (this.mListeners.containsKey(Integer.valueOf(i)) && (remoteCallbackList = this.mListeners.get(Integer.valueOf(i))) != null) {
            try {
                int beginBroadcast = remoteCallbackList.beginBroadcast();
                while (beginBroadcast > 0) {
                    beginBroadcast--;
                    try {
                        remoteCallbackList.getBroadcastItem(beginBroadcast).onReceiveSMSDeliveryReportAck(smsEvent2.getMessageID(), smsEvent.getReasonCode(), smsEvent.getRetryAfter());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
            } catch (Throwable th) {
                remoteCallbackList.finishBroadcast();
                throw th;
            }
            remoteCallbackList.finishBroadcast();
        }
    }

    private void sendPendingEmergencySms(int i) {
        Log.d(LOG_TAG, "sendPendingEmergencySms");
        LinkedList linkedList = this.mEmergencyPendingQueue.get(i);
        this.mEmergencyPendingQueue.set(i, new LinkedList());
        while (!linkedList.isEmpty()) {
            sendMessage(obtainMessage(3, linkedList.remove()));
        }
    }

    private void sendPendingEmergencySmsWithGeolocation() {
        String str = LOG_TAG;
        Log.d(str, "sendPendingEmergencySmsWithGeolocation : " + this.mEmergencyGeolocationState.toString());
        while (!this.mEmergencyGeolocationPendingQueue.isEmpty()) {
            sendMessage(obtainMessage(3, this.mEmergencyGeolocationPendingQueue.remove(0)));
        }
    }

    private void failPendingEmergencySms(int i) {
        Log.d(LOG_TAG, "failPendingEmergencySms");
        LinkedList linkedList = this.mEmergencyPendingQueue.get(i);
        this.mEmergencyPendingQueue.set(i, new LinkedList());
        while (!linkedList.isEmpty()) {
            SmsEvent smsEvent = (SmsEvent) linkedList.remove();
            onReceiveSMSAckInternal(i, smsEvent.getMessageID(), 10002, smsEvent.getContentType(), (byte[]) null, -1);
        }
    }

    private void handleEmergencyRegisterDone(SmsEvent smsEvent) {
        String str = LOG_TAG;
        Log.d(str, "handleEmergencyRegisterDone");
        int eventType = smsEvent.getEventType();
        if (this.mEmergencyRegiProcessiong[eventType]) {
            removeMessages(6, smsEvent);
            if (getImsRegistration(eventType, true) != null) {
                this.mEmergencyRegiProcessiong[eventType] = false;
                sendPendingEmergencySms(eventType);
                return;
            }
            Log.d(str, "handleEmergencyRegisterDone: Emergency Regi failed.");
            sendMessage(obtainMessage(6, smsEvent));
        }
    }

    private void handleEmergencyRegisterFail(SmsEvent smsEvent) {
        Log.d(LOG_TAG, "handleEmergencyRegisterFail");
        int eventType = smsEvent.getEventType();
        boolean[] zArr = this.mEmergencyRegiProcessiong;
        if (zArr[eventType]) {
            zArr[eventType] = false;
            failPendingEmergencySms(eventType);
        }
    }

    public void registerForSMSStateChange(int i, ISmsServiceEventListener iSmsServiceEventListener) {
        StringBuilder sb = new StringBuilder();
        String str = LOG_TAG;
        sb.append(str);
        sb.append(i);
        String sb2 = sb.toString();
        Log.i(sb2, "registerForSMSStateChange[" + i + "]");
        if (!this.mListeners.containsKey(Integer.valueOf(i))) {
            this.mListeners.put(Integer.valueOf(i), new RemoteCallbackList());
        }
        RemoteCallbackList remoteCallbackList = this.mListeners.get(Integer.valueOf(i));
        if (remoteCallbackList != null) {
            Log.i(str + i, "registerForSMSStateChange register");
            remoteCallbackList.register(iSmsServiceEventListener);
        }
    }

    public void deRegisterForSMSStateChange(int i, ISmsServiceEventListener iSmsServiceEventListener) {
        RemoteCallbackList remoteCallbackList;
        Log.i(LOG_TAG + i, "deRegisterForSMSStateChange[" + i + "]");
        if (this.mListeners.containsKey(Integer.valueOf(i)) && (remoteCallbackList = this.mListeners.get(Integer.valueOf(i))) != null) {
            remoteCallbackList.unregister(iSmsServiceEventListener);
        }
    }

    public void handleEventDefaultAppChanged() {
        Log.d(LOG_TAG, "handleEventDefaultAppChanged");
        for (int i = 0; i < this.mTelephonyManager.getPhoneCount(); i++) {
            if (isRegistered(i)) {
                this.mMessagingAppInfoReceiver.registerReceiver();
                String str = LOG_TAG;
                Log.i(str, "onChange[" + i + "] : MessageApplication is changed. MsgApp = " + this.mMessagingAppInfoReceiver.mDefaultMsgApp + ", Version = " + this.mMessagingAppInfoReceiver.mMsgAppVersion);
                this.mImsService.setMsgAppInfoToSipUa(i, this.mMessagingAppInfoReceiver.mMsgAppVersion);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x0174 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0175  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendSMSOverIMS(int r33, byte[] r34, java.lang.String r35, java.lang.String r36, int r37, boolean r38) {
        /*
            r32 = this;
            r10 = r32
            r11 = r33
            r7 = r35
            r12 = r36
            r13 = r37
            com.sec.ims.ImsRegistration r8 = r32.getImsRegistration(r33)
            r14 = 1
            com.sec.ims.ImsRegistration r9 = r10.getImsRegistration(r11, r14)
            com.sec.internal.constants.Mno r15 = com.sec.internal.helper.SimUtil.getSimMno(r33)
            boolean r0 = com.sec.internal.helper.os.Debug.isProductShip()
            java.lang.String r1 = " destAddr="
            java.lang.String r6 = "sendSMSOverIMS: "
            r5 = 0
            if (r0 == 0) goto L_0x004b
            if (r7 == 0) goto L_0x004b
            int r0 = r35.length()
            r2 = 3
            if (r0 <= r2) goto L_0x004b
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r6)
            r3.append(r11)
            r3.append(r1)
            java.lang.String r2 = r7.substring(r5, r2)
            r3.append(r2)
            java.lang.String r2 = r3.toString()
            android.util.Log.i(r0, r2)
            goto L_0x0065
        L_0x004b:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r6)
            r2.append(r11)
            r2.append(r1)
            r2.append(r7)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r0, r2)
        L_0x0065:
            r16 = 0
            if (r8 == 0) goto L_0x0077
            int r0 = r8.getSubscriptionId()
            com.sec.ims.settings.ImsProfile r2 = r8.getImsProfile()
            r31 = r2
            r2 = r0
            r0 = r31
            goto L_0x007b
        L_0x0077:
            r0 = -1
            r2 = r0
            r0 = r16
        L_0x007b:
            com.sec.internal.ims.servicemodules.sms.SmsEvent r4 = new com.sec.internal.ims.servicemodules.sms.SmsEvent
            r4.<init>()
            r4.setContentType(r12)
            boolean r3 = r10.isEmergencyNumber(r7)
            r4.setEmergency(r3)
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.VZW
            if (r15 != r3) goto L_0x00c4
            r0 = r32
            r1 = r2
            r2 = r33
            r3 = r37
            r17 = r4
            r4 = r36
            r5 = r8
            boolean r0 = r0.vzwSendSmsLimitedRegi(r1, r2, r3, r4, r5)
            if (r0 == 0) goto L_0x00a1
            return
        L_0x00a1:
            r0 = r32
            r1 = r35
            r2 = r33
            r3 = r37
            r4 = r34
            r5 = r17
            r18 = r6
            r6 = r9
            int r5 = r0.vzwSendSmsE911(r1, r2, r3, r4, r5, r6)
            if (r5 != r14) goto L_0x00b8
            r8 = r9
            goto L_0x00bc
        L_0x00b8:
            r0 = 2
            if (r5 != r0) goto L_0x00bc
            return
        L_0x00bc:
            java.lang.String r0 = r10.vzwSendSmsDestAddr(r7)
            r9 = r0
            r7 = r8
            r8 = r5
            goto L_0x010b
        L_0x00c4:
            r17 = r4
            r18 = r6
            if (r0 == 0) goto L_0x0108
            r2 = 64
            boolean r0 = r0.isNeedPidfSipMsg(r2)
            if (r0 == 0) goto L_0x0108
            boolean r0 = r17.isEmergency()
            if (r0 == 0) goto L_0x0108
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "sendSMSOverIMS: add geolocation"
            r2.append(r3)
            r2.append(r11)
            r2.append(r1)
            r2.append(r7)
            java.lang.String r1 = r2.toString()
            android.util.Log.i(r0, r1)
            r0 = r32
            r1 = r33
            r2 = r34
            r3 = r35
            r4 = r37
            r5 = r17
            boolean r0 = r0.handleEmergencySmsWithGeolocation(r1, r2, r3, r4, r5)
            if (r0 != 0) goto L_0x0108
            return
        L_0x0108:
            r9 = r7
            r7 = r8
            r8 = 0
        L_0x010b:
            boolean r0 = com.sec.internal.ims.servicemodules.sms.SmsUtil.isProhibited(r7)
            java.lang.String r6 = "SSM_sendSMSOverIMS_notRegi"
            java.lang.String r5 = "0"
            if (r0 != 0) goto L_0x02a5
            if (r7 != 0) goto L_0x0119
            goto L_0x02a5
        L_0x0119:
            r1 = r17
            r1.setImsRegistration(r7)
            if (r9 == 0) goto L_0x0169
            java.lang.String r0 = "application/vnd.3gpp.sms"
            boolean r0 = r12.equals(r0)
            if (r0 == 0) goto L_0x0145
            r0 = r32
            r2 = r34
            r3 = r9
            r4 = r15
            r14 = r5
            r5 = r33
            r13 = r6
            r6 = r37
            r19 = r7
            r7 = r36
            r20 = r8
            r8 = r19
            r35 = r9
            r9 = r38
            com.sec.internal.ims.servicemodules.sms.SmsEvent r4 = r0.make3gppSMS(r1, r2, r3, r4, r5, r6, r7, r8, r9)
            goto L_0x0172
        L_0x0145:
            r14 = r5
            r13 = r6
            r19 = r7
            r20 = r8
            r35 = r9
            java.lang.String r0 = "application/vnd.3gpp2.sms"
            boolean r0 = r12.equals(r0)
            if (r0 == 0) goto L_0x0171
            r0 = r32
            r2 = r34
            r3 = r35
            r4 = r15
            r5 = r33
            r6 = r37
            r7 = r36
            r8 = r19
            com.sec.internal.ims.servicemodules.sms.SmsEvent r4 = r0.make3gpp2SMS(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x0172
        L_0x0169:
            r14 = r5
            r13 = r6
            r19 = r7
            r20 = r8
            r35 = r9
        L_0x0171:
            r4 = r1
        L_0x0172:
            if (r4 != 0) goto L_0x0175
            return
        L_0x0175:
            com.sec.ims.util.NameAddr r0 = r19.getPreferredImpu()
            if (r0 != 0) goto L_0x019e
            android.content.Context r0 = r10.mContext
            java.lang.String r2 = "1"
            r3 = 999(0x3e7, float:1.4E-42)
            r4 = 0
            r5 = 0
            r1 = r33
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(r0, r1, r2, r3, r4, r5)
            android.content.Context r0 = r10.mContext
            r6 = 0
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendSmotInfoToHQM(r0, r11, r14, r13, r6)
            r7 = r37
            r10.m3GPP2SendingMsgId = r7
            r5 = 0
            r6 = -1
            r0 = r32
            r2 = r37
            r4 = r36
            r0.broadcastOnReceiveSMSAck(r1, r2, r3, r4, r5, r6)
            return
        L_0x019e:
            r6 = 0
            java.lang.String r0 = com.sec.internal.ims.servicemodules.sms.SmsUtil.getLocalUri(r19)
            r4.setLocalUri(r0)
            r10.mRetransCount = r6
            java.lang.String r0 = r4.getContentType()
            r10.mLastMOContentType = r0
            com.sec.internal.ims.servicemodules.sms.SmsLogger r0 = r10.mSmsLogger
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r3 = r18
            r2.append(r3)
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            r0.logAndAdd(r1, r2)
            java.lang.String r0 = ""
            if (r15 == 0) goto L_0x01e3
            java.lang.String r2 = r15.getCountryCode()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r5 = "Country Code = "
            r3.append(r5)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r1, r3)
            goto L_0x01e4
        L_0x01e3:
            r2 = r0
        L_0x01e4:
            android.telephony.TelephonyManager r3 = r10.mTelephonyManager
            r7 = r35
            boolean r3 = r3.isEmergencyNumber(r7)
            if (r3 != 0) goto L_0x01ff
            java.lang.String r3 = "922"
            boolean r3 = r3.equals(r7)
            if (r3 == 0) goto L_0x0210
            java.lang.String r3 = "us"
            boolean r2 = r3.equalsIgnoreCase(r2)
            if (r2 == 0) goto L_0x0210
        L_0x01ff:
            java.lang.String r2 = "Send EMERGENCY_SMS_OVER_IMS intent for GPS"
            android.util.Log.i(r1, r2)
            android.content.Intent r1 = new android.content.Intent
            java.lang.String r2 = "com.samsung.intent.action.EMERGENCY_SMS_OVER_IMS"
            r1.<init>(r2)
            android.content.Context r2 = r10.mContext
            r2.sendBroadcast(r1)
        L_0x0210:
            if (r7 == 0) goto L_0x0218
            java.lang.String r1 = "(?<=.{2}).(?=.{2})"
            java.lang.String r0 = r7.replaceAll(r1, r0)
        L_0x0218:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r11)
            java.lang.String r2 = ","
            r1.append(r2)
            r1.append(r0)
            r1.append(r2)
            java.lang.String r0 = r4.toKeyDump()
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            r1 = 1342177281(0x50000001, float:8.5899356E9)
            com.sec.internal.log.IMSLog.c(r1, r0)
            com.sec.internal.ims.servicemodules.sms.ISmsServiceInterface r0 = r10.mImsService
            java.lang.String r22 = r4.getSmscAddr()
            java.lang.String r23 = r4.getLocalUri()
            java.lang.String r24 = r4.getContentType()
            byte[] r25 = r4.getData()
            r26 = 0
            r27 = 0
            int r28 = r4.getMessageID()
            int r29 = r19.getHandle()
            boolean r30 = r4.isEmergency()
            r21 = r0
            r21.sendMessage(r22, r23, r24, r25, r26, r27, r28, r29, r30)
            r5 = r20
            r0 = 1
            if (r5 != r0) goto L_0x02a4
            android.content.Context r0 = r10.mContext
            com.sec.internal.helper.PreciseAlarmManager r0 = com.sec.internal.helper.PreciseAlarmManager.getInstance(r0)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r33)
            r2 = 7
            android.os.Message r1 = r10.obtainMessage(r2, r1)
            r0.removeMessage((android.os.Message) r1)
            android.content.Context r0 = r10.mContext
            com.sec.internal.helper.PreciseAlarmManager r0 = com.sec.internal.helper.PreciseAlarmManager.getInstance(r0)
            java.lang.Class r1 = r32.getClass()
            java.lang.String r1 = r1.getSimpleName()
            java.lang.Integer r3 = java.lang.Integer.valueOf(r33)
            android.os.Message r2 = r10.obtainMessage(r2, r3)
            r3 = 300000(0x493e0, double:1.482197E-318)
            r0.sendMessageDelayed(r1, r2, r3)
            boolean[] r0 = r10.mIsInScbm
            boolean r1 = r0[r11]
            if (r1 != 0) goto L_0x02a4
            r1 = 1
            r0[r11] = r1
            android.content.Context r0 = r10.mContext
            com.sec.internal.ims.servicemodules.sms.SmsUtil.broadcastSCBMState(r0, r1, r11)
        L_0x02a4:
            return
        L_0x02a5:
            r14 = r5
            r8 = r6
            r19 = r7
            r7 = r13
            r1 = r17
            r6 = 0
            com.sec.internal.interfaces.ims.core.ISimManager r0 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r33)
            if (r0 == 0) goto L_0x02b7
            com.sec.internal.constants.Mno r16 = r0.getNetMno()
        L_0x02b7:
            r0 = r16
            if (r19 == 0) goto L_0x02c4
            boolean r2 = r19.isProhibited()
            if (r2 == 0) goto L_0x02c4
            r2 = 777(0x309, float:1.089E-42)
            goto L_0x02c6
        L_0x02c4:
            r2 = 999(0x3e7, float:1.4E-42)
        L_0x02c6:
            boolean r1 = r1.isEmergency()
            if (r1 == 0) goto L_0x02d4
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.VZW
            if (r0 != r1) goto L_0x02d4
            r0 = 10002(0x2712, float:1.4016E-41)
            r9 = r0
            goto L_0x02d5
        L_0x02d4:
            r9 = r2
        L_0x02d5:
            r10.m3GPP2SendingMsgId = r7
            android.content.Context r0 = r10.mContext
            java.lang.String r2 = "1"
            r3 = 999(0x3e7, float:1.4E-42)
            r4 = 0
            r5 = 0
            r1 = r33
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendIsmoInfoToHqmAndStoreMoSmsInfoOfDrcsToImsLogAgent(r0, r1, r2, r3, r4, r5)
            android.content.Context r0 = r10.mContext
            com.sec.internal.ims.servicemodules.sms.SmsUtil.sendSmotInfoToHQM(r0, r11, r14, r8, r6)
            r5 = 0
            r6 = -1
            r0 = r32
            r2 = r37
            r3 = r9
            r4 = r36
            r0.broadcastOnReceiveSMSAck(r1, r2, r3, r4, r5, r6)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.sms.SmsServiceModule.sendSMSOverIMS(int, byte[], java.lang.String, java.lang.String, int, boolean):void");
    }

    private SmsEvent make3gppSMS(SmsEvent smsEvent, byte[] bArr, String str, Mno mno, int i, int i2, String str2, ImsRegistration imsRegistration, boolean z) {
        SmsEvent smsEvent2 = smsEvent;
        byte[] bArr2 = bArr;
        String str3 = str;
        Mno mno2 = mno;
        int i3 = i;
        int i4 = i2;
        ImsRegistration imsRegistration2 = imsRegistration;
        boolean z2 = z;
        smsEvent2.setRpRef(SmsUtil.getIncreasedRPRef());
        String scaForRpDa = GsmSmsUtil.getScaForRpDa(z2, bArr2, str3, mno2);
        if ("noSCA".equals(scaForRpDa)) {
            SmsUtil.sendSmotInfoToHQM(this.mContext, i3, "0", "SSM_sendSMSOverIMS_emptySCA", true);
            onReceiveSMSAckInternal(i, i2, 10001, str2, (byte[]) null, -1);
            return null;
        }
        String sca = GsmSmsUtil.getSca(scaForRpDa, str3, mno2, imsRegistration2);
        if (!z2) {
            if (mno2 != Mno.VZW) {
                scaForRpDa = sca;
            }
            smsEvent2.setData(GsmSmsUtil.get3gppPduFromTpdu(bArr2, smsEvent.getRpRef(), GsmSmsUtil.removeSipPrefix(scaForRpDa), ""));
        } else {
            smsEvent2.setData(GsmSmsUtil.getRpSMMAPdu(smsEvent.getRpRef()));
        }
        String scaFromPsismscPSI = GsmSmsUtil.getScaFromPsismscPSI(this.mContext, sca, mno, this.mTelephonyManager, i, imsRegistration);
        if (mno2 != Mno.LGU || !"noPSI".equals(scaFromPsismscPSI)) {
            if (mno2 == Mno.DOCOMO || mno.isOrange() || mno.isSprint() || mno.isTmobile()) {
                this.mIsRetryIfNoSubmitReport = true;
            }
            smsEvent2.setSmscAddr(SmsUtil.getNetworkPreferredUri(imsRegistration2, scaFromPsismscPSI, mno2 == Mno.ATT || mno2 == Mno.VZW || mno2 == Mno.CU));
            if (!z2) {
                smsEvent2.setMessageID(SmsUtil.getNewMsgId() & 255);
                smsEvent2.setTpMr(GsmSmsUtil.getTPMRFromPdu(bArr));
            } else {
                smsEvent2.setMessageID(i4);
                smsEvent2.setTpMr(i4);
            }
            if (this.mPendingQueue.containsKey(Integer.valueOf(smsEvent.getMessageID()))) {
                Log.e(LOG_TAG, "send message already pending");
            } else {
                smsEvent2.setState(100);
                Handler handler = this.mTimeoutHandler;
                if (handler != null) {
                    handler.sendMessageDelayed(handler.obtainMessage(1, smsEvent2), 180000);
                }
                this.mPendingQueue.put(Integer.valueOf(smsEvent.getMessageID()), smsEvent2);
            }
            return smsEvent2;
        }
        SmsUtil.sendSmotInfoToHQM(this.mContext, i3, "0", "SSM_sendSMSOverIMS_LguNoPSI", true);
        return null;
    }

    private SmsEvent make3gpp2SMS(SmsEvent smsEvent, byte[] bArr, String str, Mno mno, int i, int i2, String str2, ImsRegistration imsRegistration) {
        boolean z;
        ImsRegistration imsRegistration2;
        SmsEvent smsEvent2 = smsEvent;
        Mno mno2 = mno;
        int i3 = i;
        int i4 = i2;
        try {
            if (mno2 == Mno.VZW) {
                String str3 = str;
                imsRegistration2 = imsRegistration;
                z = true;
            } else {
                String str4 = str;
                imsRegistration2 = imsRegistration;
                z = false;
            }
            smsEvent.setSmscAddr(SmsUtil.getNetworkPreferredUri(imsRegistration2, str, z));
            smsEvent.setData(bArr);
            if (mno2 != Mno.VZW || bArr.length <= 256) {
                smsEvent.setMessageID(i4);
                this.m3GPP2SendingMsgId = i4;
                return smsEvent2;
            }
            SmsUtil.sendSmotInfoToHQM(this.mContext, i3, "0", "SSM_sendSMSOverIMS_overSize", true);
            return null;
        } catch (NullPointerException e) {
            e.printStackTrace();
            SmsUtil.sendSmotInfoToHQM(this.mContext, i3, "0", "SSM_sendSMSOverIMS_AddrErr", false);
            onReceiveSMSAckInternal(i, i2, 10001, str2, (byte[]) null, -1);
            return null;
        }
    }

    private boolean vzwSendSmsLimitedRegi(int i, int i2, int i3, String str, ImsRegistration imsRegistration) {
        String subscriberId = TelephonyManagerExt.getSubscriberId(this.mTelephonyManager, i);
        if (imsRegistration == null || TextUtils.isEmpty(subscriberId) || !imsRegistration.isImsiBased(subscriberId)) {
            return false;
        }
        Log.d(LOG_TAG, "Limited Regi Mode, fallback to 1xRTT");
        onReceiveSMSAckInternal(i2, i3, 10004, str, (byte[]) null, -1);
        return true;
    }

    private int vzwSendSmsE911(String str, int i, int i2, byte[] bArr, SmsEvent smsEvent, ImsRegistration imsRegistration) {
        if (!isEmergencyNumber(str) || SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) < 29) {
            return 0;
        }
        String str2 = LOG_TAG;
        Log.d(str2, "sendSMSOverIMS: isVzwE911 = true, mEmergencyRegiProcessiong = " + this.mEmergencyRegiProcessiong[i]);
        if (imsRegistration != null) {
            Log.d(str2, "sendSMSOverIMS: regInfo = eRegInfo");
            return 1;
        }
        smsEvent.setEventType(i);
        smsEvent.setMessageID(i2);
        smsEvent.setData(bArr);
        smsEvent.setSmscAddr(str);
        this.mEmergencyPendingQueue.get(i).add(smsEvent);
        boolean[] zArr = this.mEmergencyRegiProcessiong;
        if (zArr[i]) {
            return 2;
        }
        zArr[i] = true;
        sendMessage(obtainMessage(4, smsEvent));
        sendMessageDelayed(obtainMessage(6, smsEvent), 10000);
        return 2;
    }

    private boolean isEmergencyNumber(String str) {
        return "911".equals(str) || "9339".equals(str) || "922".equals(str);
    }

    private String vzwSendSmsDestAddr(String str) {
        if (str == null || str.length() != 14 || !str.startsWith("0111") || !GsmSmsUtil.isNanp(str.substring(4))) {
            return str;
        }
        Log.i(LOG_TAG, "6.5.2b is applied");
        return str.substring(3);
    }

    public void onDeregistering(ImsRegistration imsRegistration) {
        Log.i(LOG_TAG, "onDeregistering");
        super.onDeregistering(imsRegistration);
        this.mIsDeregistering[imsRegistration.getPhoneId()] = true;
    }

    public void sendDeliverReport(int i, byte[] bArr) {
        if (bArr != null && bArr.length >= 4) {
            byte b = bArr[2] & 255;
            SmsEvent remove = this.mPendingQueue.remove(Integer.valueOf(b));
            if (remove != null) {
                Handler handler = this.mTimeoutHandler;
                if (handler != null) {
                    handler.removeMessages(1, remove);
                }
                ImsRegistration imsRegistration = getImsRegistration(i);
                if (imsRegistration == null || imsRegistration.getPreferredImpu() == null || this.mIsDeregistering[i]) {
                    String str = LOG_TAG;
                    Log.e(str, "sendDeliverReport() called. but not registered IMS");
                    Log.i(str + '/' + i, "sendDeliverReport: msgId = " + b);
                    post(new SmsServiceModule$$ExternalSyntheticLambda0(this, i, b));
                } else if (remove.getRpRef() == -1 || remove.getCallID() == null || remove.getSmscAddr() == null) {
                    Log.e(LOG_TAG, "sendDeliverReport wrong format");
                } else {
                    int tpPid = remove.getTpPid();
                    int tpDcs = remove.getTpDcs();
                    if ((tpPid & 63) == 63 && (tpDcs & 2) == 2) {
                        Log.i(LOG_TAG, "sendDeliverReport() set TP-PID and TP-DCS");
                    } else {
                        Log.i(LOG_TAG, "sendDeliverReport() do not set TP-PID and TP-DCS");
                        tpPid = 0;
                        tpDcs = 0;
                    }
                    remove.setData(GsmSmsUtil.getDeliverReportFromPdu(i, remove.getRpRef(), bArr, tpPid, tpDcs));
                    remove.setState(105);
                    remove.setImsRegistration(imsRegistration);
                    remove.setLocalUri(SmsUtil.getLocalUri(imsRegistration));
                    Handler handler2 = this.mTimeoutHandler;
                    if (handler2 != null) {
                        handler2.sendMessageDelayed(handler2.obtainMessage(1, remove), 180000);
                    }
                    this.mPendingQueue.put(Integer.valueOf(b), remove);
                    this.mSmsLogger.logAndAdd(LOG_TAG, "sendDeliverReport: " + remove);
                    IMSLog.c(LogClass.SMS_SEND_DELIVER_REPROT, i + "," + remove.toKeyDump());
                    this.mImsService.sendMessage(remove.getSmscAddr(), remove.getLocalUri(), remove.getContentType(), remove.getData(), false, remove.getCallID(), b, remove.getImsRegistration().getHandle(), false);
                    this.mLastMOContentType = remove.getContentType();
                }
            } else {
                Log.e(LOG_TAG, "sendDeliverReport no incoming Message to send DeliverReport!");
            }
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$sendDeliverReport$0(int i, int i2) {
        RemoteCallbackList remoteCallbackList;
        if (this.mListeners.containsKey(Integer.valueOf(i)) && (remoteCallbackList = this.mListeners.get(Integer.valueOf(i))) != null) {
            try {
                int beginBroadcast = remoteCallbackList.beginBroadcast();
                while (beginBroadcast > 0) {
                    beginBroadcast--;
                    try {
                        remoteCallbackList.getBroadcastItem(beginBroadcast).onReceiveSMSDeliveryReportAck(i2, NOTI_DEREGISTERED, -1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
            } catch (Throwable th) {
                remoteCallbackList.finishBroadcast();
                throw th;
            }
            remoteCallbackList.finishBroadcast();
        }
    }

    public boolean getSmsFallback(int i) {
        boolean z = ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.ENABLE_DEFAULT_SMS_FALLBACK, false);
        String str = LOG_TAG;
        IMSLog.i(str, i, "getSmsFallback: " + z);
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isRegistered(int i) {
        return getImsRegistration(i) != null;
    }

    public void onMessagingAppPackageReplaced() {
        post(new Runnable() {
            public void run() {
                if (SmsServiceModule.this.mMessagingAppInfoReceiver != null) {
                    String r0 = SmsServiceModule.LOG_TAG;
                    Log.i(r0, "onMessagingAppPackageReplaced: " + SmsServiceModule.this.mMessagingAppInfoReceiver.mMsgAppVersion);
                    for (int i = 0; i < SmsServiceModule.this.mTelephonyManager.getPhoneCount(); i++) {
                        if (SmsServiceModule.this.isRegistered(i)) {
                            SmsServiceModule.this.mImsService.setMsgAppInfoToSipUa(i, SmsServiceModule.this.mMessagingAppInfoReceiver.mMsgAppVersion);
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void retryToSendMessage(int i, SmsEvent smsEvent) {
        String str = LOG_TAG;
        Log.i(str, "retry to send message");
        if (!isRegistered(i)) {
            smsEvent.setReasonCode(NOTI_DEREGISTERED);
            smsEvent.setRetryAfter(-1);
            onReceiveSmsMessage(smsEvent);
            return;
        }
        byte[] data = smsEvent.getData();
        if (data == null) {
            smsEvent.setReasonCode(10001);
            smsEvent.setRetryAfter(-1);
            onReceiveSmsMessage(smsEvent);
            Log.e(str, "Aborting, reason: null pdu obtained via SmsEvent.getData() call");
            return;
        }
        GsmSmsUtil.set3gppTPRD(data);
        Log.i(str, smsEvent.toString());
        this.mImsService.sendMessage(smsEvent.getSmscAddr(), smsEvent.getLocalUri(), smsEvent.getContentType(), data, false, (String) null, smsEvent.getMessageID(), smsEvent.getImsRegistration() != null ? smsEvent.getImsRegistration().getHandle() : 0, smsEvent.isEmergency());
        smsEvent.setState(100);
        Handler handler = this.mTimeoutHandler;
        if (handler != null) {
            handler.sendMessageDelayed(handler.obtainMessage(1, smsEvent), 180000);
        }
        this.mPendingQueue.put(Integer.valueOf(smsEvent.getMessageID()), smsEvent);
        this.mRetransCount++;
    }

    private void fallbackForSpecificReason(int i) {
        int i2;
        if (this.mLastMOContentType.equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
            for (Integer intValue : this.mPendingQueue.keySet()) {
                int intValue2 = intValue.intValue();
                if (intValue2 >= 0) {
                    SmsEvent remove = this.mPendingQueue.remove(Integer.valueOf(intValue2));
                    if (remove != null) {
                        remove.setReasonCode(i);
                        if (remove.getData() != null) {
                            String str = LOG_TAG;
                            Log.i(str, "Fallback 3gpp message with reason " + i);
                            Handler handler = this.mTimeoutHandler;
                            if (handler != null && handler.hasMessages(1, Integer.valueOf(intValue2))) {
                                this.mTimeoutHandler.removeMessages(1, Integer.valueOf(intValue2));
                            }
                            Handler handler2 = this.mTimeoutHandler;
                            if (handler2 != null && this.mIsRetryIfNoSubmitReport && handler2.hasMessages(2, Integer.valueOf(intValue2))) {
                                this.mTimeoutHandler.removeMessages(2, Integer.valueOf(intValue2));
                            }
                            ImsRegistration imsRegistration = remove.getImsRegistration();
                            broadcastOnReceiveSMSAck(imsRegistration != null ? imsRegistration.getPhoneId() : 0, remove.getTpMr(), remove.getReasonCode(), remove.getContentType(), GsmSmsUtil.get3gppTpduFromPdu(remove.getData()), remove.getRetryAfter());
                        }
                    } else {
                        return;
                    }
                }
            }
        } else if (this.mLastMOContentType.equals(CdmaSmsUtil.CONTENT_TYPE_3GPP2) && (i2 = this.m3GPP2SendingMsgId) != -1) {
            this.m3GPP2SendingMsgId = -1;
            String str2 = LOG_TAG;
            Log.i(str2, "Fallback 3gpp2 message with reason " + i);
            broadcastOnReceiveSMSAck(0, i2, 800, CdmaSmsUtil.CONTENT_TYPE_3GPP2, (byte[]) null, -1);
        }
    }

    /* access modifiers changed from: private */
    public void onReceiveSMSAckInternal(int i, int i2, int i3, String str, byte[] bArr, int i4) {
        Log.i(LOG_TAG + '/' + i, "onReceiveSMSAckInternal: " + i3);
        broadcastOnReceiveSMSAck(i, i2, i3, str, bArr, i4);
    }

    private synchronized void broadcastOnReceiveSMSAck(int i, int i2, int i3, String str, byte[] bArr, int i4) {
        RemoteCallbackList remoteCallbackList;
        Log.d(LOG_TAG + '/' + i, "broadcastOnReceiveSMSAck: " + i3);
        if (this.mListeners.containsKey(Integer.valueOf(i)) && (remoteCallbackList = this.mListeners.get(Integer.valueOf(i))) != null) {
            try {
                int beginBroadcast = remoteCallbackList.beginBroadcast();
                while (beginBroadcast > 0) {
                    beginBroadcast--;
                    try {
                        remoteCallbackList.getBroadcastItem(beginBroadcast).onReceiveSMSAck(i2, i3, str, bArr, i4);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IllegalStateException e2) {
                try {
                    e2.printStackTrace();
                } catch (Throwable th) {
                    remoteCallbackList.finishBroadcast();
                    throw th;
                }
            }
            remoteCallbackList.finishBroadcast();
        }
    }

    public boolean isSmsOverIpEnabled(int i) {
        ImsRegistration imsRegistration = getImsRegistration(i);
        String str = LOG_TAG;
        Log.i(str, "regInfo: " + imsRegistration);
        if (imsRegistration == null || !isRunning()) {
            Log.i(str, "disallow sms Service");
            return false;
        }
        if (imsRegistration.hasService("smsip")) {
            if (!(SimUtil.getSimMno(i) == Mno.ORANGE || SimUtil.getSimMno(i) == Mno.ORANGE_POLAND)) {
                if (imsRegistration.getImsProfile().getDisallowReregi()) {
                    if (SmsUtil.isServiceAvailable(this.mTelephonyManager, i, true)) {
                        return true;
                    }
                } else if (this.mIsDeregisterTimerRunning[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isVolteSupported(int i) {
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration == null) {
            return false;
        }
        String str = LOG_TAG;
        Log.d(str, "IsVolteSupported= " + imsRegistration.hasService("mmtel"));
        return imsRegistration.hasService("mmtel");
    }

    public void updateCapabilities(int i) {
        getServiceModuleManager().updateCapabilities(i);
    }

    public ImsFeature.Capabilities queryCapabilityStatus(int i) {
        ImsFeature.Capabilities capabilities = new ImsFeature.Capabilities();
        if (isSmsOverIpEnabled(i)) {
            String str = LOG_TAG;
            Log.i(str, "Sms Service queryCapabilityStatus[" + i + "]: addCapabilities CAPABILITY_TYPE_SMS");
            capabilities.addCapabilities(8);
        } else {
            String str2 = LOG_TAG;
            Log.i(str2, "Sms Service queryCapabilityStatus[" + i + "]: removeCapabilities CAPABILITY_TYPE_SMS");
            capabilities.removeCapabilities(8);
        }
        return capabilities;
    }

    public void setDelayedDeregisterTimerRunning(int i, boolean z) {
        this.mIsDeregisterTimerRunning[i] = z;
        updateCapabilities(i);
    }

    public ConcurrentHashMap<Integer, SmsEvent> getPendingQueue() {
        return this.mPendingQueue;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.sms.SmsServiceModule$4  reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$sms$SmsServiceModule$EmergencyGeolocationState;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule$EmergencyGeolocationState[] r0 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.EmergencyGeolocationState.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$servicemodules$sms$SmsServiceModule$EmergencyGeolocationState = r0
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule$EmergencyGeolocationState r1 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.EmergencyGeolocationState.NONE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$sms$SmsServiceModule$EmergencyGeolocationState     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule$EmergencyGeolocationState r1 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.EmergencyGeolocationState.UPDATING     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$sms$SmsServiceModule$EmergencyGeolocationState     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule$EmergencyGeolocationState r1 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.EmergencyGeolocationState.UPDATED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$sms$SmsServiceModule$EmergencyGeolocationState     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.servicemodules.sms.SmsServiceModule$EmergencyGeolocationState r1 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.EmergencyGeolocationState.TIMEOUT     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.sms.SmsServiceModule.AnonymousClass4.<clinit>():void");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0026, code lost:
        if (r1 != 2) goto L_0x003f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleEmergencySmsWithGeolocation(int r5, byte[] r6, java.lang.String r7, int r8, com.sec.internal.ims.servicemodules.sms.SmsEvent r9) {
        /*
            r4 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleEmergencySmsWithGeolocation: mEmergencyGeolocationState="
            r1.append(r2)
            com.sec.internal.ims.servicemodules.sms.SmsServiceModule$EmergencyGeolocationState r2 = r4.mEmergencyGeolocationState
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            int[] r1 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.AnonymousClass4.$SwitchMap$com$sec$internal$ims$servicemodules$sms$SmsServiceModule$EmergencyGeolocationState
            com.sec.internal.ims.servicemodules.sms.SmsServiceModule$EmergencyGeolocationState r2 = r4.mEmergencyGeolocationState
            int r2 = r2.ordinal()
            r1 = r1[r2]
            r2 = 1
            if (r1 == r2) goto L_0x0029
            r0 = 2
            if (r1 == r0) goto L_0x005e
            goto L_0x003f
        L_0x0029:
            com.sec.internal.interfaces.ims.core.IGeolocationController r1 = com.sec.internal.ims.registry.ImsRegistry.getGeolocationController()
            if (r1 == 0) goto L_0x0040
            java.lang.String r3 = "handleEmergencySmsWithGeolocation: Start geolocation update for emergency SMS"
            android.util.Log.i(r0, r3)
            boolean r1 = r1.startGeolocationUpdate(r5, r2)
            if (r1 != 0) goto L_0x0040
            java.lang.String r4 = "handleEmergencySmsWithGeolocation: Geolocation update request failed. Send SMS without geolocation update"
            android.util.Log.i(r0, r4)
        L_0x003f:
            return r2
        L_0x0040:
            com.sec.internal.ims.servicemodules.sms.SmsServiceModule$EmergencyGeolocationState r0 = com.sec.internal.ims.servicemodules.sms.SmsServiceModule.EmergencyGeolocationState.UPDATING
            r4.mEmergencyGeolocationState = r0
            com.sec.ims.ImsRegistration r0 = r9.getImsRegistration()
            r1 = 9
            java.lang.Integer r2 = java.lang.Integer.valueOf(r5)
            android.os.Message r1 = r4.obtainMessage(r1, r2)
            com.sec.ims.settings.ImsProfile r0 = r0.getImsProfile()
            int r0 = r0.getLocationAcquireFailSMS()
            long r2 = (long) r0
            r4.sendMessageDelayed(r1, r2)
        L_0x005e:
            r9.setEventType(r5)
            r9.setMessageID(r8)
            r9.setData(r6)
            r9.setSmscAddr(r7)
            java.util.ArrayList<com.sec.internal.ims.servicemodules.sms.SmsEvent> r4 = r4.mEmergencyGeolocationPendingQueue
            r4.add(r9)
            r4 = 0
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.sms.SmsServiceModule.handleEmergencySmsWithGeolocation(int, byte[], java.lang.String, int, com.sec.internal.ims.servicemodules.sms.SmsEvent):boolean");
    }

    public void onUpdateGeolocation() {
        if (!this.mEmergencyGeolocationPendingQueue.isEmpty()) {
            Log.i(LOG_TAG, "onUpdateGeolocation");
            sendEmptyMessage(10);
        }
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(str);
        IMSLog.dump(str, "mIncommingMagId : " + SmsUtil.getIncommingMagId());
        IMSLog.dump(str, "mRPMsgRef : " + SmsUtil.getRPMsgRef());
        IMSLog.dump(str, "m3GPP2SendingMsgId : " + this.m3GPP2SendingMsgId);
        IMSLog.dump(str, "mLastMOContentType : " + this.mLastMOContentType);
        IMSLog.dump(str, "mRetransCount : " + this.mRetransCount);
        IMSLog.dump(str, "mStorageAvailable : " + this.mStorageAvailable);
        IMSLog.dump(str, "mPendingQueue :");
        IMSLog.increaseIndent(str);
        for (Map.Entry next : this.mPendingQueue.entrySet()) {
            String str2 = LOG_TAG;
            IMSLog.dump(str2, "key : " + next.getKey() + ", value : " + next.getValue());
        }
        this.mSmsLogger.dump();
        String str3 = LOG_TAG;
        IMSLog.decreaseIndent(str3);
        IMSLog.decreaseIndent(str3);
    }
}
