package com.sec.internal.imsphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.stub.ImsSmsImplBase;
import android.util.Log;
import com.android.internal.telephony.TelephonyFeatures;
import com.android.internal.telephony.uicc.IccUtils;
import com.sec.ims.sms.ISmsServiceEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.sms.SmsMessage;
import com.sec.internal.constants.ims.servicemodules.sms.SmsResponse;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.sms.SmsLogger;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public final class ImsSmsImpl extends ImsSmsImplBase {
    private static final String ACTION_TEST_PDU_IMS = "com.sec.internal.google.ImsSmsImpl.PduTest";
    private static final int CDMA_NETWORK_TYPE = 1;
    private static final String CONTENT_TYPE_3GPP = "application/vnd.3gpp.sms";
    private static final String CONTENT_TYPE_3GPP2 = "application/vnd.3gpp2.sms";
    private static final int EVENT_SMS_DELIVER_REPORT_RETRY = 4;
    private static final int EVENT_SMS_NO_RESPONSE_TIMEOUT = 2;
    private static final int EVENT_SMS_RETRY = 1;
    private static final int EVENT_SMS_SEND_DELAYED_MESSAGE = 3;
    private static final int GSM_NETWORK_TYPE = 2;
    private static final String IMS_CALL_PERMISSION = "android.permission.ACCESS_IMS_CALL_SERVICE";
    private static final String LOG_TAG_HEAD = "ImsSmsImpl";
    private static final String MAP_KEY_CONTENT_TYPE = "contentType";
    private static final String MAP_KEY_DEST_ADDR = "destAddr";
    private static final String MAP_KEY_MESSAGE_ID = "messageId";
    private static final String MAP_KEY_PDU = "pdu";
    private static final String MAP_KEY_RETRY_COUNT = "retryCount";
    private static final String MAP_KEY_STATUS_REPORT = "statusReport";
    private static final String MAP_KEY_TOKEN = "token";
    private static final int MAX_SEND_RETRIES_1 = 1;
    private static final int MAX_SEND_RETRIES_2 = 2;
    private static final int MAX_SEND_RETRIES_4 = 4;
    private static final int PDU_TYPE_RECEIVED_CDMA_SMS = 1;
    private static final int PDU_TYPE_RECEIVED_GSM_SMS = 0;
    private static final int RIL_CODE_RP_ERROR = 32768;
    private static final int RIL_CODE_RP_ERROR_END = 33023;
    private static final int RP_CAUSE_CONGESTION = 42;
    private static final int RP_CAUSE_DESTINATION_OUT_OF_ORDER = 27;
    private static final int RP_CAUSE_MEMORY_CAP_EXCEEDED = 22;
    private static final int RP_CAUSE_NETWORK_OUT_OF_ORDER = 38;
    private static final int RP_CAUSE_NONE_ERROR = 0;
    private static final int RP_CAUSE_NOT_COMPATIBLE_PROTOCOL = 98;
    private static final int RP_CAUSE_PROTOCOL_ERROR = 111;
    private static final int RP_CAUSE_REQUESTED_FACILITY_NOT_IMPLEMENTED = 69;
    private static final int RP_CAUSE_RESOURCES_UNAVAILABLE = 47;
    private static final int RP_CAUSE_SMS_TRANSFER_REJECTED = 21;
    private static final int RP_CAUSE_TEMPORARY_FAILURE = 41;
    private static final int RP_CAUSE_UNIDENTIFIED_SUBSCRIBER = 28;
    private static final int RP_CAUSE_UNKNOWN_SUBSCRIBER = 30;
    private static final int SEND_RETRY_DELAY = 30000;
    private static final int TIMER_STATE = 130000;
    private static final int TIMER_STATE_FOR_O2C = 30000;
    private static final int TP_CAUSE_INVALID_SME_ADDRESS = 195;
    private static final int TP_CAUSE_SM_REJECTED_OR_DUPLICATE = 197;
    /* access modifiers changed from: private */
    public String LOG_TAG;
    private Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentNetworkType;
    private final ArrayList<ImsSmsTracker> mDeliveryPendingList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private Map<Integer, ImsSmsTracker> mImsSmsTrackers = new ConcurrentSkipListMap();
    protected BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ImsSmsImpl.ACTION_TEST_PDU_IMS)) {
                int intExtra = intent.getIntExtra("phoneId", 0);
                int intExtra2 = intent.getIntExtra("type", -1);
                String stringExtra = intent.getStringExtra("hexString");
                String r1 = ImsSmsImpl.this.LOG_TAG;
                Log.d(r1, "mIntentReceiver.onReceive: phoneId = " + intExtra + ", pduType = " + intExtra2 + ", pduHexString = " + stringExtra);
                if (ImsSmsImpl.this.mPhoneId == intExtra) {
                    byte[] hexStringToBytes = IccUtils.hexStringToBytes(stringExtra);
                    if (hexStringToBytes == null) {
                        Log.e(ImsSmsImpl.this.LOG_TAG, "mIntentReceiver.onReceive: pdu is null");
                        return;
                    }
                    String str = SmsMessage.FORMAT_3GPP;
                    if (intExtra2 == 0) {
                        Log.d(ImsSmsImpl.this.LOG_TAG, "mIntentReceiver.onReceive: PDU_TYPE_RECEIVED_GSM_SMS_IMS");
                    } else if (intExtra2 != 1) {
                        Log.d(ImsSmsImpl.this.LOG_TAG, "mIntentReceiver.onReceive: unsupported pduType");
                    } else {
                        Log.d(ImsSmsImpl.this.LOG_TAG, "mIntentReceiver.onReceive: PDU_TYPE_RECEIVED_CDMA_SMS_IMS");
                        str = SmsMessage.FORMAT_3GPP2;
                    }
                    ImsSmsImpl.this.onSmsPduTestReceived(255, str, hexStringToBytes);
                }
            }
        }
    };
    private int mLastRetryCount;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    /* access modifiers changed from: private */
    public LastSentDeliveryAck mSentDeliveryAck;
    private SmsEventListener mSmsEventListener = new SmsEventListener();
    /* access modifiers changed from: private */
    public SmsLogger mSmsLogger = SmsLogger.getInstance();
    private ISmsServiceModule mSmsServiceModule;
    private String mSmsc;
    private Map<Integer, Integer> mStatusMsgIds = new HashMap();
    private int mTpmr;

    private int resultToCause(int i) {
        if (i == 1) {
            return 0;
        }
        if (i != 3) {
            return i != 4 ? 41 : 111;
        }
        return 22;
    }

    public ImsSmsImpl(int i) {
        String str = "";
        this.LOG_TAG = str;
        StringBuilder sb = new StringBuilder();
        sb.append(LOG_TAG_HEAD);
        sb.append(i != 0 ? "2" : str);
        this.LOG_TAG = sb.toString();
        this.mPhoneId = i;
        this.mTpmr = -1;
        this.mContext = ImsRegistry.getContext();
        ISmsServiceModule smsServiceModule = ImsRegistry.getServiceModuleManager().getSmsServiceModule();
        this.mSmsServiceModule = smsServiceModule;
        if (smsServiceModule != null) {
            registerSmsEventListener();
        }
        if (!TelephonyFeatures.SHIP_BUILD) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_TEST_PDU_IMS);
            Log.d(this.LOG_TAG, "register for intent action=com.sec.internal.google.ImsSmsImpl.PduTest");
            this.mContext.registerReceiver(this.mIntentReceiver, intentFilter);
        }
        HandlerThread handlerThread = new HandlerThread(LOG_TAG_HEAD);
        this.mHandlerThread = handlerThread;
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message message) {
                String r0 = ImsSmsImpl.this.LOG_TAG;
                Log.d(r0, "handleMessage: event " + message.what);
                int i = message.what;
                if (i == 1) {
                    ImsSmsImpl.this.handleSmsRetry((ImsSmsTracker) message.obj);
                } else if (i == 2) {
                    ImsSmsImpl.this.handleNoResponseTimeout((ImsSmsTracker) message.obj);
                } else if (i == 3) {
                    ImsSmsImpl.this.handleSendDelayedMessage();
                } else if (i == 4) {
                    ImsSmsImpl.this.handleRetryDeliveryReportAck((LastSentDeliveryAck) message.obj);
                }
            }
        };
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x012b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendSms(int r23, int r24, java.lang.String r25, java.lang.String r26, boolean r27, byte[] r28) {
        /*
            r22 = this;
            r9 = r22
            r15 = r23
            r14 = r24
            r13 = r25
            r0 = r28
            com.sec.internal.constants.ims.servicemodules.sms.SmsMessage r1 = new com.sec.internal.constants.ims.servicemodules.sms.SmsMessage
            r1.<init>()
            int r12 = r9.mLastRetryCount
            java.lang.String r11 = "3gpp"
            boolean r2 = r11.equals(r13)
            if (r2 == 0) goto L_0x001c
            java.lang.String r2 = "application/vnd.3gpp.sms"
            goto L_0x001e
        L_0x001c:
            java.lang.String r2 = "application/vnd.3gpp2.sms"
        L_0x001e:
            r18 = r2
            boolean r2 = r11.equals(r13)     // Catch:{ RuntimeException -> 0x00d8 }
            r10 = 0
            if (r2 == 0) goto L_0x0065
            r2 = 2
            r9.mCurrentNetworkType = r2     // Catch:{ RuntimeException -> 0x00d8 }
            byte[] r2 = com.android.internal.telephony.uicc.IccUtils.hexStringToBytes(r26)     // Catch:{ RuntimeException -> 0x00d8 }
            int r3 = r2.length     // Catch:{ RuntimeException -> 0x00d8 }
            int r4 = r0.length     // Catch:{ RuntimeException -> 0x00d8 }
            int r3 = r3 + r4
            byte[] r3 = new byte[r3]     // Catch:{ RuntimeException -> 0x00d8 }
            int r4 = r2.length     // Catch:{ RuntimeException -> 0x00d8 }
            java.lang.System.arraycopy(r2, r10, r3, r10, r4)     // Catch:{ RuntimeException -> 0x00d8 }
            int r2 = r2.length     // Catch:{ RuntimeException -> 0x00d8 }
            int r4 = r0.length     // Catch:{ RuntimeException -> 0x00d8 }
            java.lang.System.arraycopy(r0, r10, r3, r2, r4)     // Catch:{ RuntimeException -> 0x00d8 }
            r1.parseSubmitPdu(r3, r13)     // Catch:{ RuntimeException -> 0x00d8 }
            java.lang.String r0 = r1.getDestinationAddress()     // Catch:{ RuntimeException -> 0x00d8 }
            boolean r1 = r1.getStatusReportRequested()     // Catch:{ RuntimeException -> 0x00d8 }
            boolean r2 = r9.isTPRDset(r3)     // Catch:{ RuntimeException -> 0x00d8 }
            if (r2 == 0) goto L_0x0056
            byte r2 = r9.getTPMR(r3)     // Catch:{ RuntimeException -> 0x00d8 }
            r2 = r2 & 255(0xff, float:3.57E-43)
            r9.mTpmr = r2     // Catch:{ RuntimeException -> 0x00d8 }
            goto L_0x005b
        L_0x0056:
            int r2 = r9.mPhoneId     // Catch:{ RuntimeException -> 0x00d8 }
            r9.setTPMRintoTPDU(r3, r2)     // Catch:{ RuntimeException -> 0x00d8 }
        L_0x005b:
            int r2 = r9.mTpmr     // Catch:{ RuntimeException -> 0x00d8 }
            r17 = r0
            r19 = r1
            r0 = r2
            r16 = r3
            goto L_0x0092
        L_0x0065:
            java.lang.String r2 = "3gpp2"
            boolean r2 = r2.equals(r13)     // Catch:{ RuntimeException -> 0x00d8 }
            if (r2 == 0) goto L_0x008a
            r2 = 1
            r9.mCurrentNetworkType = r2     // Catch:{ RuntimeException -> 0x00d8 }
            r1.parseSubmitPdu(r0, r13)     // Catch:{ RuntimeException -> 0x00d8 }
            int r0 = r1.getMsgID()     // Catch:{ RuntimeException -> 0x00d8 }
            byte[] r2 = r1.getTpdu()     // Catch:{ RuntimeException -> 0x00d8 }
            java.lang.String r3 = r1.getDestinationAddress()     // Catch:{ RuntimeException -> 0x00d8 }
            boolean r1 = r1.getStatusReportRequested()     // Catch:{ RuntimeException -> 0x00d8 }
            r19 = r1
            r16 = r2
            r17 = r3
            goto L_0x0092
        L_0x008a:
            r0 = 0
            r16 = r0
            r17 = r16
            r0 = r10
            r19 = r0
        L_0x0092:
            r1 = r22
            r2 = r23
            r3 = r0
            r4 = r17
            r5 = r16
            r6 = r18
            r7 = r12
            r8 = r19
            java.util.HashMap r1 = r1.getImsSmsTrackerMap(r2, r3, r4, r5, r6, r7, r8)     // Catch:{ RuntimeException -> 0x00d8 }
            com.sec.internal.imsphone.ImsSmsImpl$ImsSmsTracker r2 = new com.sec.internal.imsphone.ImsSmsImpl$ImsSmsTracker     // Catch:{ RuntimeException -> 0x00d8 }
            int r3 = r9.mPhoneId     // Catch:{ RuntimeException -> 0x00d8 }
            r20 = 0
            r21 = 0
            r4 = r10
            r10 = r2
            r5 = r11
            r11 = r3
            r3 = r12
            r12 = r1
            r1 = r13
            r13 = r23
            r8 = r14
            r14 = r3
            r7 = r15
            r15 = r0
            r10.<init>(r11, r12, r13, r14, r15, r16, r17, r18, r19, r20)     // Catch:{ RuntimeException -> 0x00d6 }
            java.util.Map<java.lang.Integer, com.sec.internal.imsphone.ImsSmsImpl$ImsSmsTracker> r0 = r9.mImsSmsTrackers     // Catch:{ RuntimeException -> 0x00d6 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r23)     // Catch:{ RuntimeException -> 0x00d6 }
            boolean r0 = r0.containsKey(r3)     // Catch:{ RuntimeException -> 0x00d6 }
            if (r0 != 0) goto L_0x00d1
            java.util.Map<java.lang.Integer, com.sec.internal.imsphone.ImsSmsImpl$ImsSmsTracker> r0 = r9.mImsSmsTrackers     // Catch:{ RuntimeException -> 0x00d6 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r23)     // Catch:{ RuntimeException -> 0x00d6 }
            r0.put(r3, r2)     // Catch:{ RuntimeException -> 0x00d6 }
        L_0x00d1:
            r9.sendSmsOverIms(r2, r4)     // Catch:{ RuntimeException -> 0x00d6 }
            goto L_0x0162
        L_0x00d6:
            r0 = move-exception
            goto L_0x00dd
        L_0x00d8:
            r0 = move-exception
            r5 = r11
            r1 = r13
            r8 = r14
            r7 = r15
        L_0x00dd:
            java.lang.String r2 = r9.LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Can not send sms: "
            r3.append(r4)
            java.lang.String r0 = r0.getMessage()
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            android.util.Log.e(r2, r0)
            boolean r0 = r1.equals(r5)
            java.lang.String r10 = " messageId = "
            if (r0 == 0) goto L_0x012b
            r4 = 2
            r5 = 1
            r6 = 2
            r1 = r22
            r2 = r23
            r3 = r24
            r1.onSendSmsResultError(r2, r3, r4, r5, r6)
            com.sec.internal.ims.servicemodules.sms.SmsLogger r0 = r9.mSmsLogger
            java.lang.String r1 = r9.LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onSendSmsResult token = "
            r2.append(r3)
            r2.append(r7)
            r2.append(r10)
            r2.append(r8)
            java.lang.String r2 = r2.toString()
            r0.logAndAdd(r1, r2)
            r11 = r7
            goto L_0x0159
        L_0x012b:
            r4 = 2
            r5 = 1
            r6 = 31
            r0 = 2
            r1 = r22
            r2 = r23
            r3 = r24
            r11 = r7
            r7 = r0
            r1.onSendSmsResultIncludeErrClass(r2, r3, r4, r5, r6, r7)
            com.sec.internal.ims.servicemodules.sms.SmsLogger r0 = r9.mSmsLogger
            java.lang.String r1 = r9.LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onSendSmsResponse token = "
            r2.append(r3)
            r2.append(r11)
            r2.append(r10)
            r2.append(r8)
            java.lang.String r2 = r2.toString()
            r0.logAndAdd(r1, r2)
        L_0x0159:
            java.util.Map<java.lang.Integer, com.sec.internal.imsphone.ImsSmsImpl$ImsSmsTracker> r0 = r9.mImsSmsTrackers
            java.lang.Integer r1 = java.lang.Integer.valueOf(r23)
            r0.remove(r1)
        L_0x0162:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsSmsImpl.sendSms(int, int, java.lang.String, java.lang.String, boolean, byte[]):void");
    }

    public void acknowledgeSms(int i, int i2, int i3) {
        byte[] bArr = new byte[4];
        if (this.mCurrentNetworkType == 2) {
            int i4 = 0;
            if (i3 == 1) {
                bArr[0] = 0;
                bArr[1] = 0;
                bArr[2] = (byte) i;
                bArr[3] = 0;
            } else {
                int resultToCause = resultToCause(i3);
                bArr[0] = (byte) resultToCause;
                bArr[1] = (byte) 128;
                bArr[2] = (byte) i;
                bArr[3] = 0;
                i4 = resultToCause;
            }
            this.mSmsServiceModule.sendDeliverReport(this.mPhoneId, bArr);
            if (this.mSentDeliveryAck != null) {
                this.mSentDeliveryAck = null;
            }
            this.mSentDeliveryAck = new LastSentDeliveryAck(bArr, i4, 2);
            SmsLogger smsLogger = this.mSmsLogger;
            String str = this.LOG_TAG;
            smsLogger.logAndAdd(str, "> SMS_ACK : messageRef = " + i);
        }
    }

    public void onMemoryAvailable(int i) {
        int i2 = i;
        ImsSmsTracker imsSmsTracker = new ImsSmsTracker(this.mPhoneId, getImsSmsTrackerMap(i, MNO.TIGO_NICARAGUA, this.mSmsc, (byte[]) null, "application/vnd.3gpp.sms", 0, false), i, 0, MNO.TIGO_NICARAGUA, (byte[]) null, this.mSmsc, "application/vnd.3gpp.sms", false, false);
        if (!this.mImsSmsTrackers.containsKey(Integer.valueOf(i))) {
            this.mImsSmsTrackers.put(Integer.valueOf(i), imsSmsTracker);
        }
        try {
            this.mSmsServiceModule.sendSMSOverIMS(this.mPhoneId, (byte[]) null, this.mSmsc, "application/vnd.3gpp.sms", MNO.TIGO_NICARAGUA, true);
            Log.i(this.LOG_TAG, "onMemoryAvailable");
        } catch (RuntimeException e) {
            String str = this.LOG_TAG;
            Log.e(str, "Can not send onMemoryAvailable: " + e.getMessage());
            onMemoryAvailableResult(i2, 2, 2);
            SmsLogger smsLogger = this.mSmsLogger;
            String str2 = this.LOG_TAG;
            smsLogger.logAndAdd(str2, "onMemoryAvailableResult token = " + i2);
            this.mImsSmsTrackers.remove(Integer.valueOf(i));
        }
    }

    public void acknowledgeSmsReport(int i, int i2, int i3) {
        int intValue = this.mStatusMsgIds.remove(Integer.valueOf(i2)).intValue();
        String str = this.LOG_TAG;
        Log.i(str, "acknowledgeSmsReport messageRef = " + i2 + ", statusMsgId = " + intValue);
        acknowledgeSms(intValue, intValue, i3);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0054 A[SYNTHETIC, Splitter:B:17:0x0054] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006e A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x006f A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getSmsFormat() {
        /*
            r9 = this;
            long r0 = android.os.Binder.clearCallingIdentity()
            android.content.Context r2 = r9.mContext     // Catch:{ all -> 0x0073 }
            android.content.ContentResolver r3 = r2.getContentResolver()     // Catch:{ all -> 0x0073 }
            java.lang.String r2 = "content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/SMS_FORMAT"
            android.net.Uri r2 = android.net.Uri.parse(r2)     // Catch:{ all -> 0x0073 }
            android.net.Uri$Builder r2 = r2.buildUpon()     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0073 }
            r4.<init>()     // Catch:{ all -> 0x0073 }
            java.lang.String r5 = "simslot"
            r4.append(r5)     // Catch:{ all -> 0x0073 }
            int r5 = r9.mPhoneId     // Catch:{ all -> 0x0073 }
            r4.append(r5)     // Catch:{ all -> 0x0073 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0073 }
            android.net.Uri$Builder r2 = r2.fragment(r4)     // Catch:{ all -> 0x0073 }
            android.net.Uri r4 = r2.build()     // Catch:{ all -> 0x0073 }
            r5 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            android.database.Cursor r2 = r3.query(r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0073 }
            if (r2 == 0) goto L_0x0050
            boolean r3 = r2.moveToFirst()     // Catch:{ all -> 0x0046 }
            if (r3 == 0) goto L_0x0050
            r3 = 1
            java.lang.String r3 = r2.getString(r3)     // Catch:{ all -> 0x0046 }
            goto L_0x0052
        L_0x0046:
            r9 = move-exception
            r2.close()     // Catch:{ all -> 0x004b }
            goto L_0x004f
        L_0x004b:
            r2 = move-exception
            r9.addSuppressed(r2)     // Catch:{ all -> 0x0073 }
        L_0x004f:
            throw r9     // Catch:{ all -> 0x0073 }
        L_0x0050:
            java.lang.String r3 = "3GPP"
        L_0x0052:
            if (r2 == 0) goto L_0x0057
            r2.close()     // Catch:{ all -> 0x0073 }
        L_0x0057:
            android.os.Binder.restoreCallingIdentity(r0)
            java.lang.String r0 = "3GPP2"
            boolean r0 = r0.equals(r3)
            java.lang.String r1 = "3gpp"
            if (r0 == 0) goto L_0x0072
            android.content.Context r0 = r9.mContext
            int r9 = r9.mPhoneId
            boolean r9 = com.sec.internal.ims.util.ImsUtil.isCdmalessEnabled(r0, r9)
            if (r9 == 0) goto L_0x006f
            return r1
        L_0x006f:
            java.lang.String r9 = "3gpp2"
            return r9
        L_0x0072:
            return r1
        L_0x0073:
            r9 = move-exception
            android.os.Binder.restoreCallingIdentity(r0)
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsSmsImpl.getSmsFormat():java.lang.String");
    }

    public void onReady() {
        updateTPMR(this.mPhoneId);
    }

    public void setRetryCount(int i, int i2) {
        this.mLastRetryCount = i2;
    }

    public void setSmsc(String str) {
        this.mSmsc = str;
    }

    public void acknowledgeSms(int i, int i2, int i3, byte[] bArr) {
        byte[] bArr2 = new byte[(bArr.length + 4)];
        if (this.mCurrentNetworkType == 2) {
            bArr2[0] = (byte) 0;
            bArr2[1] = (byte) 0;
            bArr2[2] = (byte) i;
            bArr2[3] = (byte) bArr.length;
            System.arraycopy(bArr, 0, bArr2, 4, bArr.length);
            this.mSmsServiceModule.sendDeliverReport(this.mPhoneId, bArr2);
            SmsLogger smsLogger = this.mSmsLogger;
            String str = this.LOG_TAG;
            smsLogger.logAndAdd(str, "> SMS_ACK_WITH_PDU : messageRef = " + i);
        }
    }

    private void registerSmsEventListener() {
        ISmsServiceModule iSmsServiceModule = this.mSmsServiceModule;
        if (iSmsServiceModule != null) {
            iSmsServiceModule.registerForSMSStateChange(this.mPhoneId, this.mSmsEventListener);
        }
    }

    /* access modifiers changed from: private */
    public void handleSendDelayedMessage() {
        if (this.mImsSmsTrackers.size() > 0) {
            Iterator<Map.Entry<Integer, ImsSmsTracker>> it = this.mImsSmsTrackers.entrySet().iterator();
            if (it.hasNext()) {
                int token = ((ImsSmsTracker) it.next().getValue()).getToken();
                ImsSmsTracker remove = this.mImsSmsTrackers.remove(Integer.valueOf(token));
                if (remove != null && !remove.mSentComplete) {
                    this.mImsSmsTrackers.put(Integer.valueOf(token), remove);
                    sendSmsOverIms(remove, true);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003e, code lost:
        if (com.sec.internal.helper.SimUtil.getSimMno(r9.mPhoneId) == com.sec.internal.constants.Mno.TELEFONICA_CZ) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0040, code lost:
        r9 = r9.mHandler;
        r9.sendMessageDelayed(r9.obtainMessage(2, r10), r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        android.util.Log.e(r9.LOG_TAG, "exception during sms retry");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006e, code lost:
        if (r9.mHandler != null) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0078, code lost:
        if (com.sec.internal.helper.SimUtil.getSimMno(r9.mPhoneId) == com.sec.internal.constants.Mno.TELEFONICA_CZ) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007a, code lost:
        r9 = r9.mHandler;
        r9.sendMessageDelayed(r9.obtainMessage(2, r10), r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0084, code lost:
        r9 = r9.mHandler;
        r9.sendMessageDelayed(r9.obtainMessage(2, r10), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008d, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleSmsRetry(com.sec.internal.imsphone.ImsSmsImpl.ImsSmsTracker r10) {
        /*
            r9 = this;
            int r0 = r10.mToken
            r1 = 0
            r10.mSentComplete = r1
            r2 = 30000(0x7530, double:1.4822E-319)
            r4 = 130000(0x1fbd0, double:6.42285E-319)
            r6 = 2
            java.util.Map<java.lang.Integer, com.sec.internal.imsphone.ImsSmsImpl$ImsSmsTracker> r7 = r9.mImsSmsTrackers     // Catch:{ all -> 0x0054 }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x0054 }
            boolean r7 = r7.containsKey(r8)     // Catch:{ all -> 0x0054 }
            if (r7 != 0) goto L_0x0020
            java.util.Map<java.lang.Integer, com.sec.internal.imsphone.ImsSmsImpl$ImsSmsTracker> r7 = r9.mImsSmsTrackers     // Catch:{ all -> 0x0054 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x0054 }
            r7.put(r0, r10)     // Catch:{ all -> 0x0054 }
        L_0x0020:
            java.lang.String r0 = r10.mContentType     // Catch:{ all -> 0x0054 }
            java.lang.String r7 = "application/vnd.3gpp.sms"
            boolean r0 = r0.equals(r7)     // Catch:{ all -> 0x0054 }
            if (r0 == 0) goto L_0x002f
            byte[] r0 = r10.mPdu     // Catch:{ all -> 0x0054 }
            r9.setTPRDintoTPDU(r0)     // Catch:{ all -> 0x0054 }
        L_0x002f:
            r9.sendSmsOverIms(r10, r1)     // Catch:{ all -> 0x0054 }
            android.os.Handler r0 = r9.mHandler
            if (r0 == 0) goto L_0x006a
            int r0 = r9.mPhoneId
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.TELEFONICA_CZ
            if (r0 != r1) goto L_0x004a
        L_0x0040:
            android.os.Handler r9 = r9.mHandler
            android.os.Message r10 = r9.obtainMessage(r6, r10)
            r9.sendMessageDelayed(r10, r2)
            goto L_0x006a
        L_0x004a:
            android.os.Handler r9 = r9.mHandler
            android.os.Message r10 = r9.obtainMessage(r6, r10)
            r9.sendMessageDelayed(r10, r4)
            goto L_0x006a
        L_0x0054:
            java.lang.String r0 = r9.LOG_TAG     // Catch:{ all -> 0x006b }
            java.lang.String r1 = "exception during sms retry"
            android.util.Log.e(r0, r1)     // Catch:{ all -> 0x006b }
            android.os.Handler r0 = r9.mHandler
            if (r0 == 0) goto L_0x006a
            int r0 = r9.mPhoneId
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.TELEFONICA_CZ
            if (r0 != r1) goto L_0x004a
            goto L_0x0040
        L_0x006a:
            return
        L_0x006b:
            r0 = move-exception
            android.os.Handler r1 = r9.mHandler
            if (r1 == 0) goto L_0x008d
            int r1 = r9.mPhoneId
            com.sec.internal.constants.Mno r1 = com.sec.internal.helper.SimUtil.getSimMno(r1)
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.TELEFONICA_CZ
            if (r1 != r7) goto L_0x0084
            android.os.Handler r9 = r9.mHandler
            android.os.Message r10 = r9.obtainMessage(r6, r10)
            r9.sendMessageDelayed(r10, r2)
            goto L_0x008d
        L_0x0084:
            android.os.Handler r9 = r9.mHandler
            android.os.Message r10 = r9.obtainMessage(r6, r10)
            r9.sendMessageDelayed(r10, r4)
        L_0x008d:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsSmsImpl.handleSmsRetry(com.sec.internal.imsphone.ImsSmsImpl$ImsSmsTracker):void");
    }

    /* access modifiers changed from: private */
    public void handleNoResponseTimeout(ImsSmsTracker imsSmsTracker) {
        int i = imsSmsTracker.mToken;
        int i2 = imsSmsTracker.mMessageId;
        if (this.mImsSmsTrackers.containsKey(Integer.valueOf(i))) {
            int i3 = canFallbackForTimeout() ? 4 : 2;
            if ("application/vnd.3gpp.sms".equals(imsSmsTracker.mContentType)) {
                onSendSmsResultError(i, i2, i3, 1, -1);
                SmsLogger smsLogger = this.mSmsLogger;
                String str = this.LOG_TAG;
                smsLogger.logAndAdd(str, "handleNoResponseTimeout: onSendSmsResult token = " + i + " messageId = " + i2 + " reason = timeOut");
            } else {
                onSendSmsResultIncludeErrClass(i, i2, i3, 1, 31, 2);
                SmsLogger smsLogger2 = this.mSmsLogger;
                String str2 = this.LOG_TAG;
                smsLogger2.logAndAdd(str2, "handleNoResponseTimeout: onSendSmsResponse token = " + i + " messageId = " + i2 + " reason = timeOut");
            }
            this.mImsSmsTrackers.remove(Integer.valueOf(i));
            if (this.mHandler != null && this.mImsSmsTrackers.size() > 0) {
                Handler handler = this.mHandler;
                handler.sendMessage(handler.obtainMessage(3));
                Log.d(this.LOG_TAG, "handleNoResponseTimeout : send next delayed message.");
            }
        }
    }

    public void handleRetryDeliveryReportAck(LastSentDeliveryAck lastSentDeliveryAck) {
        if (lastSentDeliveryAck == null) {
            Log.e(this.LOG_TAG, "sentDeliveryAck is null");
        } else if (lastSentDeliveryAck.mNetworkType == 2) {
            this.mSmsServiceModule.sendDeliverReport(this.mPhoneId, lastSentDeliveryAck.mPdu);
        }
    }

    /* access modifiers changed from: private */
    public void handleStatusReport(int i, int i2, String str, byte[] bArr) {
        boolean z;
        Log.d(this.LOG_TAG, "handleStatusReport messageRef = " + i + " mDeliveryPendingList.size() = " + this.mDeliveryPendingList.size());
        int size = this.mDeliveryPendingList.size();
        int i3 = 0;
        while (true) {
            if (i3 >= size) {
                z = false;
                break;
            }
            ImsSmsTracker imsSmsTracker = this.mDeliveryPendingList.get(i3);
            if (imsSmsTracker.mMessageId == i) {
                this.mStatusMsgIds.put(Integer.valueOf(i), Integer.valueOf(i2));
                onSmsStatusReportReceived(imsSmsTracker.mToken, str, bArr);
                this.mDeliveryPendingList.remove(i3);
                z = true;
                break;
            }
            i3++;
        }
        if (!z) {
            Log.d(this.LOG_TAG, "statusReport is not matched. But, the messageId is forcibly saved.");
            this.mStatusMsgIds.put(Integer.valueOf(i), Integer.valueOf(i2));
            onSmsStatusReportReceived(0, str, bArr);
        }
    }

    /* access modifiers changed from: private */
    public int getTokenByMessageId(int i) {
        for (Map.Entry<Integer, ImsSmsTracker> value : this.mImsSmsTrackers.entrySet()) {
            ImsSmsTracker imsSmsTracker = (ImsSmsTracker) value.getValue();
            if (i == imsSmsTracker.getMessageId()) {
                return imsSmsTracker.getToken();
            }
        }
        return -1;
    }

    private void sendSmsOverIms(ImsSmsTracker imsSmsTracker, boolean z) {
        boolean z2;
        HashMap<String, Object> data = imsSmsTracker.getData();
        byte[] bArr = (byte[]) data.get(MAP_KEY_PDU);
        String str = (String) data.get(MAP_KEY_DEST_ADDR);
        String str2 = (String) data.get(MAP_KEY_CONTENT_TYPE);
        int intValue = ((Integer) data.get("messageId")).intValue();
        boolean z3 = true;
        if (z || this.mImsSmsTrackers.size() > 1) {
            z2 = false;
        } else {
            this.mSmsServiceModule.sendSMSOverIMS(imsSmsTracker.mPhoneId, bArr, str, str2, intValue, false);
            z2 = true;
        }
        if (z) {
            this.mSmsServiceModule.sendSMSOverIMS(imsSmsTracker.mPhoneId, bArr, str, str2, intValue, false);
        } else {
            z3 = z2;
        }
        this.mSmsLogger.logAndAdd(this.LOG_TAG, "> SEND_SMS : token = " + imsSmsTracker.mToken + " " + imsSmsTracker.mContentType + " destAddr = " + IMSLog.checker(str) + " messageId = " + intValue + " statusReportRequested = " + imsSmsTracker.mStatusReportRequested + " smsSent = " + z3);
        if (!TelephonyFeatures.SHIP_BUILD) {
            Log.d(this.LOG_TAG, "pdu = " + IccUtils.bytesToHexString(bArr));
        }
        if (this.mHandler != null && z3) {
            if (SimUtil.getSimMno(this.mPhoneId) == Mno.TELEFONICA_CZ) {
                Handler handler = this.mHandler;
                handler.sendMessageDelayed(handler.obtainMessage(2, imsSmsTracker), 30000);
                return;
            }
            Handler handler2 = this.mHandler;
            handler2.sendMessageDelayed(handler2.obtainMessage(2, imsSmsTracker), 130000);
        }
    }

    private void setTPMRintoTPDU(byte[] bArr, int i) {
        byte b;
        int i2;
        if (bArr != null && bArr.length > 0 && (b = bArr[0]) > 0 && bArr.length > (i2 = b + 2) && (bArr[b + 1] & 1) == 1) {
            if (this.mTpmr == -1) {
                updateTPMR(i);
            }
            int i3 = this.mTpmr & 255;
            this.mTpmr = i3;
            if (i3 >= 255) {
                this.mTpmr = 0;
            } else {
                this.mTpmr = i3 + 1;
            }
            setTelephonyProperty(i, "persist.radio.tpmr_sms", String.valueOf(this.mTpmr));
            bArr[i2] = (byte) this.mTpmr;
            String str = this.LOG_TAG;
            Log.d(str, "setTPMRintoTPDU mTpmr : " + this.mTpmr);
        }
    }

    public void updateTPMR(int i) {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        String telephonyProperty = TelephonyManager.getTelephonyProperty(i, "persist.radio.tpmr_sms", "0");
        if (telephonyProperty == null || telephonyProperty.isEmpty()) {
            this.mTpmr = 0;
        } else {
            this.mTpmr = Integer.parseInt(telephonyProperty) & 255;
        }
    }

    private void setTelephonyProperty(int i, String str, String str2) {
        String str3;
        StringBuffer stringBuffer = new StringBuffer("");
        String str4 = SystemProperties.get(str);
        if (str2 == null) {
            str2 = "";
        }
        String replace = str2.replace(',', ' ');
        String[] split = str4 != null ? str4.split(",") : null;
        if (SubscriptionManager.isValidPhoneId(i)) {
            for (int i2 = 0; i2 < i; i2++) {
                if (split == null || i2 >= split.length) {
                    str3 = "";
                } else {
                    str3 = split[i2];
                }
                stringBuffer.append(str3);
                stringBuffer.append(",");
            }
            stringBuffer.append(replace);
            if (split != null) {
                for (int i3 = i + 1; i3 < split.length; i3++) {
                    stringBuffer.append(",");
                    stringBuffer.append(split[i3]);
                }
            }
            String stringBuffer2 = stringBuffer.toString();
            int length = stringBuffer2.length();
            try {
                length = stringBuffer2.getBytes("utf-8").length;
            } catch (UnsupportedEncodingException unused) {
                Log.e(this.LOG_TAG, "setTelephonyProperty: utf-8 not supported");
            }
            if (length > 91) {
                String str5 = this.LOG_TAG;
                Log.e(str5, "setTelephonyProperty: property too long phoneId=" + i + " property=" + str + " value: " + replace + " propVal=" + stringBuffer2);
                return;
            }
            SystemProperties.set(str, stringBuffer2);
        }
    }

    private void setTPRDintoTPDU(byte[] bArr) {
        byte b;
        int i;
        if (bArr != null && bArr.length > 0 && (b = bArr[0]) > 0 && bArr.length > (i = b + 1)) {
            byte b2 = bArr[i];
            if ((b2 & 1) == 1) {
                bArr[i] = (byte) (b2 | 4);
            }
        }
    }

    private boolean isTPRDset(byte[] bArr) {
        int i;
        if (bArr != null && bArr.length > 0) {
            byte b = bArr[0];
            if (b > 0 && bArr.length > (i = b + 1)) {
                byte b2 = bArr[i];
                if ((b2 & 1) == 1) {
                    if ((b2 & 4) == 4) {
                        return true;
                    }
                    return false;
                }
            }
            Log.e(this.LOG_TAG, "isTPRDset() sca is wrong: return false");
        }
        return false;
    }

    private byte getTPMR(byte[] bArr) {
        byte b;
        int i;
        if (bArr == null || bArr.length <= 0 || (b = bArr[0]) <= 0 || bArr.length <= (i = b + 2) || (bArr[b + 1] & 1) != 1) {
            return 0;
        }
        return bArr[i];
    }

    private boolean getSmsFallback() {
        ISmsServiceModule iSmsServiceModule = this.mSmsServiceModule;
        if (iSmsServiceModule == null) {
            return false;
        }
        return iSmsServiceModule.getSmsFallback(this.mPhoneId);
    }

    /* access modifiers changed from: private */
    public void onReceiveSMSSuccssAcknowledgment(int i, int i2, int i3, int i4, int i5, SmsResponse smsResponse) {
        int i6 = i3;
        int i7 = i4;
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onReceiveSMSAck: mno = ");
        sb.append(simMno.getName());
        sb.append(" messageId = ");
        sb.append(i3);
        sb.append(" reasonCode = ");
        sb.append(i4);
        sb.append(" retryAfter = ");
        int i8 = i5;
        sb.append(i5);
        Log.d(str, sb.toString());
        boolean z = smsResponse.getContentType() == 1;
        ImsSmsTracker remove = this.mImsSmsTrackers.remove(Integer.valueOf(i2));
        if (remove != null) {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.removeMessages(2, remove);
            }
            remove.mSentComplete = true;
            if (remove.mStatusReportRequested && !simMno.isKor()) {
                this.mDeliveryPendingList.add(remove);
            }
            if (this.mHandler != null && this.mImsSmsTrackers.size() > 0) {
                Handler handler2 = this.mHandler;
                handler2.sendMessage(handler2.obtainMessage(3));
            }
        }
        smsResponse.setMessageRef(i3);
        if (10000 < i7 && i7 < 11000) {
            handleInternalError(i2, i3, i4, smsResponse, z);
        } else if (32768 >= i7 || i7 >= RIL_CODE_RP_ERROR_END) {
            handleAck(simMno, i2, i3, i4, smsResponse, remove, z, i5);
        } else {
            handleRPError(simMno, i2, i3, i4, smsResponse, remove, i);
        }
    }

    private void handleAck(Mno mno, int i, int i2, int i3, SmsResponse smsResponse, ImsSmsTracker imsSmsTracker, boolean z, int i4) {
        Mno mno2 = mno;
        int i5 = i;
        int i6 = i2;
        int i7 = i3;
        SmsResponse smsResponse2 = smsResponse;
        if (mno2 == Mno.VZW) {
            handleVzwAck(i, i2, i3, smsResponse, imsSmsTracker, z);
        } else if (mno2 == Mno.SPRINT) {
            handleSprAck(i, i2, i3, smsResponse);
        } else if (mno2 == Mno.BELL) {
            handleBellAck(i, i2, i3, smsResponse);
        } else if (mno2 == Mno.UPC_CH) {
            handleUpcChAck(i, i2, i3, smsResponse);
        } else if (mno2 == Mno.CTC) {
            handleCTCAck(i, i2, i3, smsResponse, imsSmsTracker);
        } else if (mno2 == Mno.SWISSCOM) {
            handleSwisscomAck(i, i2, i3, smsResponse, imsSmsTracker);
        } else if (mno2 == Mno.DOCOMO) {
            handleDocomoAck(i, i2, i3, smsResponse, imsSmsTracker, i4);
        } else if (mno2 == Mno.SOFTBANK) {
            handleSbmAck(i, i2, i3, smsResponse);
        } else if (mno.isOneOf(Mno.KDDI, Mno.RAKUTEN_JAPAN)) {
            handleKddiRakutenAck(i, i2, i3, smsResponse, imsSmsTracker, i4);
        } else if (mno.isOrangeGPG()) {
            handleOrangeAck(i, i2, i3, smsResponse);
        } else if (mno.isOneOf(Mno.CMCC, Mno.CU, Mno.CMHK)) {
            handleCmccCuCmhkAck(i, i2, i3, smsResponse);
        } else if (i7 == 0 || !getSmsFallback()) {
            handleResult(i, i2, i3, 1, smsResponse);
        } else {
            smsResponse.setErrorClass(0);
            smsResponse.setErrorCause(19);
            handleResult(i, i2, i3, 4, smsResponse);
        }
    }

    private void handleInternalError(int i, int i2, int i3, SmsResponse smsResponse, boolean z) {
        int i4 = 4;
        if (i3 == 10001) {
            if (z) {
                smsResponse.setErrorClass(3);
                smsResponse.setErrorCause(105);
            } else {
                smsResponse.setErrorClass(0);
                smsResponse.setErrorCause(4);
            }
            i4 = 2;
        } else if (i3 != 10002) {
            if (i3 != 10004) {
                if (z) {
                    smsResponse.setErrorClass(3);
                    smsResponse.setErrorCause(107);
                } else {
                    smsResponse.setErrorClass(0);
                    smsResponse.setErrorCause(9);
                }
                i4 = 2;
            } else {
                smsResponse.setErrorClass(0);
                smsResponse.setErrorCause(19);
            }
        } else if (z) {
            smsResponse.setErrorClass(9);
            i4 = 2;
        } else {
            smsResponse.setErrorClass(0);
            smsResponse.setErrorCause(19);
        }
        handleResult(i, i2, i3, i4, smsResponse);
    }

    private void handleRPError(Mno mno, int i, int i2, int i3, SmsResponse smsResponse, ImsSmsTracker imsSmsTracker, int i4) {
        int i5;
        Mno mno2 = mno;
        SmsResponse smsResponse2 = smsResponse;
        ImsSmsTracker imsSmsTracker2 = imsSmsTracker;
        byte[] tpdu = smsResponse.getTpdu();
        byte b = tpdu.length > 3 ? tpdu[3] & 255 : 0;
        int i6 = i3 - 32768;
        int i7 = 4;
        if (!mno.isOrangeGPG() || !(i6 == 41 || i6 == 42)) {
            i5 = 2;
            if (!isErrorForSpecificCarrier(mno2, b, i6)) {
                if (mno2 == Mno.DOCOMO && i6 == 21 && b == 197) {
                    smsResponse2.setErrorClass(0);
                    Log.d(this.LOG_TAG, "Forced success for NTT");
                    i5 = 1;
                } else if (getSmsFallback()) {
                    smsResponse2.setErrorClass(0);
                    smsResponse2.setErrorCause(19);
                } else if (i6 == 42 || i6 == 111 || i6 == 47 || i6 == 27 || i6 == 41 || i6 == 98) {
                    i5 = 3;
                }
            }
            Log.i(this.LOG_TAG, "handleRPError: rpCause= " + i6 + ", tpCause= " + b + ", status= " + i5);
            handleResult(i, i2, i3, i5, smsResponse);
        }
        if (imsSmsTracker2 == null) {
            Log.d(this.LOG_TAG, "imsSmsTracker is null");
        } else {
            int i8 = imsSmsTracker2.mRetryCount;
            if (i8 < 1) {
                imsSmsTracker2.mRetryCount = i8 + 1;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker2), 30000);
                return;
            }
        }
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        int[] subId = SubscriptionManager.getSubId(i4);
        int i9 = subId != null ? subId[0] : -1;
        if (!telephonyManager.isNetworkRoaming(i9) || telephonyManager.getDataNetworkType(i9) != 18) {
            smsResponse2.setErrorClass(0);
            smsResponse2.setErrorCause(19);
            Log.d(this.LOG_TAG, "orange, set errorcause as fallbackIMS due to RP# " + i6);
        } else {
            Log.d(this.LOG_TAG, "orange, RP# " + i6 + ", isRoaming is true and DataNetworkType is IWLAN, so CS fallback does not done");
            i7 = 1;
        }
        i5 = i7;
        Log.i(this.LOG_TAG, "handleRPError: rpCause= " + i6 + ", tpCause= " + b + ", status= " + i5);
        handleResult(i, i2, i3, i5, smsResponse);
    }

    private boolean isErrorForSpecificCarrier(Mno mno, int i, int i2) {
        if (mno == Mno.BELL) {
            return i == 195 || i2 == 111 || i2 == 30 || i2 == 28;
        }
        if (mno == Mno.KT) {
            if (i2 == 41 || i2 == 42 || i2 == 47 || i2 == 98 || i2 == 111) {
                return true;
            }
            return false;
        } else if (mno == Mno.SMARTFREN) {
            if (i2 == 111) {
                return true;
            }
            return false;
        } else if (mno != Mno.SPARK) {
            return false;
        } else {
            if (i2 == 69) {
                return true;
            }
            return false;
        }
    }

    private void handleVzwAck(int i, int i2, int i3, SmsResponse smsResponse, ImsSmsTracker imsSmsTracker, boolean z) {
        int i4 = i3;
        SmsResponse smsResponse2 = smsResponse;
        ImsSmsTracker imsSmsTracker2 = imsSmsTracker;
        if (i4 >= 400 && i4 <= 599 && imsSmsTracker2 != null) {
            String str = this.LOG_TAG;
            Log.d(str, "imsSmsTracker.mRetryCount =  " + imsSmsTracker2.mRetryCount);
            int i5 = imsSmsTracker2.mRetryCount;
            if (i5 < 1) {
                imsSmsTracker2.mRetryCount = i5 + 1;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker), 30000);
            } else if (z) {
                smsResponse.setErrorClass(9);
                handleResult(i, i2, i3, 2, smsResponse);
            } else {
                smsResponse.setErrorClass(0);
                smsResponse.setErrorCause(19);
                handleResult(i, i2, i3, 4, smsResponse);
            }
        } else if (i4 != 777 && i4 != 800) {
            handleResult(i, i2, i3, 1, smsResponse);
        } else if (z) {
            smsResponse.setErrorClass(9);
            handleResult(i, i2, i3, 2, smsResponse);
        } else {
            smsResponse.setErrorClass(0);
            smsResponse.setErrorCause(19);
            handleResult(i, i2, i3, 4, smsResponse);
        }
    }

    private void handleKddiRakutenAck(int i, int i2, int i3, SmsResponse smsResponse, ImsSmsTracker imsSmsTracker, int i4) {
        if (i4 == -1) {
            if (i3 == 0) {
                handleResult(i, i2, i3, 1, smsResponse);
            } else if (SimUtil.getSimMno(this.mPhoneId) == Mno.RAKUTEN_JAPAN && (i3 == 408 || i3 == 488)) {
                smsResponse.setErrorClass(0);
                smsResponse.setErrorCause(19);
                handleResult(i, i2, i3, 4, smsResponse);
            } else {
                smsResponse.setErrorClass(9);
                handleResult(i, i2, i3, 2, smsResponse);
            }
        } else if (i3 == 403 || i3 == 404 || i3 == 408 || i3 == 500 || i3 == 503 || i3 == 504 || i3 < 100 || i3 > 699 || imsSmsTracker == null) {
            handleResult(i, i2, i3, 1, smsResponse);
        } else {
            int i5 = imsSmsTracker.mRetryCount;
            if (i5 < 4) {
                imsSmsTracker.mRetryCount = i5 + 1;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker), ((long) i4) * 1000);
                return;
            }
            smsResponse.setErrorClass(9);
            handleResult(i, i2, i3, 2, smsResponse);
        }
    }

    private void handleDocomoAck(int i, int i2, int i3, SmsResponse smsResponse, ImsSmsTracker imsSmsTracker, int i4) {
        int i5;
        if (i3 == 504 && i4 == -1) {
            i5 = 5;
        } else if (i3 == 999) {
            Log.e(this.LOG_TAG, "Waiting SMS resend timer. 999 error ignore!");
            return;
        } else {
            i5 = i4;
        }
        if ((i3 != 408 && i3 != 504) || i5 == -1 || imsSmsTracker == null) {
            handleResult(i, i2, i3, 1, smsResponse);
            return;
        }
        int i6 = imsSmsTracker.mRetryCount;
        if (i6 < 1) {
            imsSmsTracker.mRetryCount = i6 + 1;
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker), ((long) i5) * 1000);
            return;
        }
        smsResponse.setErrorClass(9);
        handleResult(i, i2, i3, 2, smsResponse);
    }

    private void handleSbmAck(int i, int i2, int i3, SmsResponse smsResponse) {
        if (i3 == 0) {
            handleResult(i, i2, i3, 1, smsResponse);
        } else if (i3 == 415) {
            smsResponse.setErrorClass(0);
            smsResponse.setErrorCause(19);
            handleResult(i, i2, i3, 4, smsResponse);
        } else {
            smsResponse.setErrorClass(9);
            handleResult(i, i2, i3, 2, smsResponse);
        }
    }

    private void handleSprAck(int i, int i2, int i3, SmsResponse smsResponse) {
        if (i3 < 400 || i3 > 699) {
            handleResult(i, i2, i3, 1, smsResponse);
            return;
        }
        smsResponse.setErrorClass(0);
        smsResponse.setErrorCause(19);
        handleResult(i, i2, i3, 4, smsResponse);
    }

    private void handleBellAck(int i, int i2, int i3, SmsResponse smsResponse) {
        if (i3 == 500 || i3 == 503 || i3 == 504 || i3 == 408) {
            smsResponse.setErrorClass(0);
            smsResponse.setErrorCause(19);
            handleResult(i, i2, i3, 4, smsResponse);
            return;
        }
        handleResult(i, i2, i3, 1, smsResponse);
    }

    private void handleOrangeAck(int i, int i2, int i3, SmsResponse smsResponse) {
        if (i3 == 403 || i3 == 408 || ((i3 >= 500 && i3 < 600) || i3 == 708)) {
            smsResponse.setErrorClass(0);
            smsResponse.setErrorCause(19);
            handleResult(i, i2, i3, 4, smsResponse);
            return;
        }
        handleResult(i, i2, i3, 1, smsResponse);
    }

    private void handleUpcChAck(int i, int i2, int i3, SmsResponse smsResponse) {
        if (i3 == 408 || i3 == 480 || i3 == 503) {
            smsResponse.setErrorClass(0);
            smsResponse.setErrorCause(19);
            handleResult(i, i2, i3, 4, smsResponse);
            return;
        }
        handleResult(i, i2, i3, 1, smsResponse);
    }

    private void handleCmccCuCmhkAck(int i, int i2, int i3, SmsResponse smsResponse) {
        if (i3 <= 0 || i3 >= 32768) {
            handleResult(i, i2, i3, 1, smsResponse);
            return;
        }
        smsResponse.setErrorClass(0);
        smsResponse.setErrorCause(19);
        handleResult(i, i2, i3, 4, smsResponse);
    }

    private void handleCTCAck(int i, int i2, int i3, SmsResponse smsResponse, ImsSmsTracker imsSmsTracker) {
        int i4;
        if (i3 != 503 || imsSmsTracker == null || (i4 = imsSmsTracker.mRetryCount) >= 1) {
            handleResult(i, i2, i3, 1, smsResponse);
            return;
        }
        imsSmsTracker.mRetryCount = i4 + 1;
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker), 30000);
    }

    private void handleSwisscomAck(int i, int i2, int i3, SmsResponse smsResponse, ImsSmsTracker imsSmsTracker) {
        if ((i3 == 400 || i3 == 403 || i3 == 404 || i3 == 488 || (i3 >= 500 && i3 < 600)) && imsSmsTracker != null) {
            int i4 = imsSmsTracker.mRetryCount;
            if (i4 < 2) {
                imsSmsTracker.mRetryCount = i4 + 1;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker), 30000);
                return;
            }
            smsResponse.setErrorClass(0);
            smsResponse.setErrorCause(19);
            handleResult(i, i2, i3, 4, smsResponse);
            return;
        }
        handleResult(i, i2, i3, 1, smsResponse);
    }

    private void handleResult(int i, int i2, int i3, int i4, SmsResponse smsResponse) {
        if (smsResponse.getContentType() == 1) {
            handleCdmaResult(i, i2, i3, smsResponse);
        } else {
            handleGsmResult(i, i2, i3, i4, smsResponse);
        }
    }

    private void handleCdmaResult(int i, int i2, int i3, SmsResponse smsResponse) {
        int errorCause = smsResponse.getErrorCause();
        int errorClass = smsResponse.getErrorClass();
        int reasonCode = smsResponse.getReasonCode();
        if (errorClass != 0) {
            if (errorClass == 9) {
                Log.d(this.LOG_TAG, "Ims failed. Retry to send over 1x");
                if (canFallback(1)) {
                    onSendSmsResultIncludeErrClass(i, i2, 4, reasonCode, errorCause, errorClass);
                } else {
                    onSendSmsResultIncludeErrClass(i, i2, 2, reasonCode, errorCause, errorClass);
                }
            } else if (errorClass == 2) {
                onSendSmsResultIncludeErrClass(i, i2, 3, reasonCode, errorCause, errorClass);
            } else if (errorClass != 3) {
                onSendSmsResultIncludeErrClass(i, i2, 2, reasonCode, errorCause, errorClass);
            } else {
                onSendSmsResultIncludeErrClass(i, i2, 2, reasonCode, errorCause, errorClass);
            }
        } else if (i3 == 10004) {
            onSendSmsResultIncludeErrClass(i, i2, 4, 0, errorCause, errorClass);
        } else {
            onSendSmsResultIncludeErrClass(i, i2, 1, 0, errorCause, errorClass);
        }
        SmsLogger smsLogger = this.mSmsLogger;
        String str = this.LOG_TAG;
        smsLogger.logAndAdd(str, "< SEND_SMS_CDMA : token = " + i + " messageId = " + i2 + " reasonCode = " + i3 + " errorCause = " + errorCause + " errorClass = " + errorClass);
    }

    private void handleGsmResult(int i, int i2, int i3, int i4, SmsResponse smsResponse) {
        if (i2 == 257) {
            if (i4 != 1) {
                if (i4 != 3) {
                    onMemoryAvailableResult(i, 2, 2);
                } else {
                    onMemoryAvailableResult(i, 3, 2);
                }
            } else if (smsResponse.getErrorClass() == 0) {
                onMemoryAvailableResult(i, 1, 1);
            } else {
                onMemoryAvailableResult(i, 2, 2);
            }
            this.mSmsLogger.logAndAdd(this.LOG_TAG, "onMemoryAvailableResult token = " + i);
            return;
        }
        int reasonCode = smsResponse.getReasonCode();
        if (i4 != 1) {
            if (i4 == 3) {
                onSendSmsResultError(i, i2, 3, reasonCode, 2);
            } else if (i4 != 4) {
                onSendSmsResultError(i, i2, i4, reasonCode, 2);
            } else if (canFallback(2)) {
                Log.d(this.LOG_TAG, "Ims failed. Retry SMS Over SGs/CS");
                onSendSmsResultError(i, i2, 4, reasonCode, 1);
            } else {
                onSendSmsResultError(i, i2, 2, reasonCode, 2);
            }
        } else if (smsResponse.getErrorClass() == 0) {
            onSendSmsResultSuccess(i, i2);
        } else {
            onSendSmsResultError(i, i2, 2, reasonCode, 2);
            i4 = 2;
        }
        this.mSmsLogger.logAndAdd(this.LOG_TAG, "< SEND_SMS : token = " + i + " messageId = " + i2 + " reasonCode = " + i3 + " status = " + i4 + " (1:Ok 2:Error 3:Retry 4:Fallback)");
    }

    private boolean canFallback(int i) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
            Mno simMno = SimUtil.getSimMno(this.mPhoneId);
            String telephonyProperty = TelephonyManager.getTelephonyProperty(this.mPhoneId, "gsm.sim.operator.numeric", "00000");
            int iccType = IccUtils.getIccType(this.mPhoneId);
            if (simMno == Mno.CMCC && iccType == 2 && (telephonyProperty.equals("46000") || telephonyProperty.equals("46002") || telephonyProperty.equals("46007") || telephonyProperty.equals("46008"))) {
                return true;
            }
            if (simMno.isOneOf(Mno.BELL, Mno.SOFTBANK, Mno.SPRINT)) {
                return true;
            }
            if (simMno == Mno.VZW) {
                boolean parseBoolean = Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(this.mPhoneId, "gsm.operator.isroaming", (String) null));
                if (!ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId)) {
                    if (!parseBoolean || telephonyManager.getNetworkType() != 13) {
                        if (i == 1 && this.mSmsServiceModule.isVolteSupported(this.mPhoneId)) {
                            return false;
                        }
                    }
                }
                Log.d(this.LOG_TAG, "fallback always over NAS (cdmaless / volte roaming)");
                return true;
            } else if (simMno == Mno.RJIL) {
                return false;
            } else {
                if (simMno == Mno.PLAY) {
                    ServiceStateWrapper serviceStateWrapper = new ServiceStateWrapper(telephonyManager.semGetServiceState(this.mPhoneId));
                    if (serviceStateWrapper.getVoiceRoaming() && serviceStateWrapper.getVoiceRoamingType() != 2 && telephonyManager.getDataNetworkType() == 18) {
                        Log.d(this.LOG_TAG, "Block fallback for Play in VoWiFi international roaming");
                        return false;
                    }
                } else if (simMno.isOrangeGPG() && telephonyManager.isNetworkRoaming() && telephonyManager.getDataNetworkType() == 18) {
                    return false;
                }
            }
            if (telephonyManager.semGetServiceState(this.mPhoneId) == null) {
                Log.d(this.LOG_TAG, "serviceState is null");
                return false;
            }
            String str = this.LOG_TAG;
            Log.d(str, "serviceState.getState() = " + telephonyManager.semGetServiceState(this.mPhoneId).getState());
            if (telephonyManager.semGetServiceState(this.mPhoneId).getState() == 0) {
                return true;
            }
            return false;
        } catch (SecurityException unused) {
            Log.e(this.LOG_TAG, "No permission for telephony service");
            return false;
        }
    }

    private boolean canFallbackForTimeout() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
            Mno simMno = SimUtil.getSimMno(this.mPhoneId);
            String telephonyProperty = TelephonyManager.getTelephonyProperty(this.mPhoneId, "gsm.sim.operator.numeric", "00000");
            int iccType = IccUtils.getIccType(this.mPhoneId);
            if (simMno == Mno.CMCC && iccType == 2 && (telephonyProperty.equals("46000") || telephonyProperty.equals("46002") || telephonyProperty.equals("46007") || telephonyProperty.equals("46008"))) {
                return true;
            }
            if (simMno.isOneOf(Mno.BELL, Mno.SPRINT)) {
                return true;
            }
            if (!simMno.isOrangeGPG() || !telephonyManager.isNetworkRoaming() || telephonyManager.getDataNetworkType() != 18) {
                if (getSmsFallback() || simMno.isOrangeGPG()) {
                    if (telephonyManager.semGetServiceState(this.mPhoneId) == null) {
                        Log.d(this.LOG_TAG, "serviceState is null");
                        return false;
                    }
                    String str = this.LOG_TAG;
                    Log.d(str, "serviceState.getState() = " + telephonyManager.semGetServiceState(this.mPhoneId).getState());
                    if (telephonyManager.semGetServiceState(this.mPhoneId).getState() == 0) {
                        Log.d(this.LOG_TAG, "CanFallbackForTimeout() : SmsFallbackDefaultSupported");
                        return true;
                    }
                }
                Log.d(this.LOG_TAG, "CanFallbackForTimeout() : SmsFallback is not Supported");
                return false;
            }
            Log.d(this.LOG_TAG, "Block timeout fallback for Orange in VoWiFi roaming");
            return false;
        } catch (SecurityException unused) {
            Log.e(this.LOG_TAG, "No permission for telephony service");
        }
    }

    private HashMap<String, Object> getImsSmsTrackerMap(int i, int i2, String str, byte[] bArr, String str2, int i3, boolean z) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("token", Integer.valueOf(i));
        hashMap.put("messageId", Integer.valueOf(i2));
        hashMap.put(MAP_KEY_DEST_ADDR, str);
        hashMap.put(MAP_KEY_PDU, bArr);
        hashMap.put(MAP_KEY_CONTENT_TYPE, str2);
        hashMap.put(MAP_KEY_RETRY_COUNT, Integer.valueOf(i3));
        hashMap.put(MAP_KEY_STATUS_REPORT, Boolean.valueOf(z));
        return hashMap;
    }

    /* access modifiers changed from: private */
    public void onSmsPduTestReceived(int i, String str, byte[] bArr) throws RuntimeException {
        String str2 = this.LOG_TAG;
        Log.d(str2, "Incoming PduTest: " + IccUtils.bytesToHexString(bArr));
        onSmsReceived(i, str, bArr);
    }

    private class SmsEventListener extends ISmsServiceEventListener.Stub {
        private SmsEventListener() {
        }

        public void onReceiveIncomingSMS(int i, String str, byte[] bArr) {
            if (str != null) {
                SmsMessage smsMessage = new SmsMessage();
                if (str.equals("application/vnd.3gpp.sms")) {
                    ImsSmsImpl.this.mCurrentNetworkType = 2;
                    smsMessage.parseDeliverPdu(bArr, SmsMessage.FORMAT_3GPP);
                    if (smsMessage.getMessageType() == 1) {
                        ImsSmsImpl.this.onSmsReceived(i, SmsMessage.FORMAT_3GPP, bArr);
                    } else if (smsMessage.getMessageType() == 2) {
                        ImsSmsImpl.this.handleStatusReport(smsMessage.getMessageRef(), i, SmsMessage.FORMAT_3GPP, bArr);
                    }
                } else if (str.equals("application/vnd.3gpp2.sms")) {
                    ImsSmsImpl.this.mCurrentNetworkType = 1;
                    byte[] convertToFrameworkSmsFormat = smsMessage.convertToFrameworkSmsFormat(bArr);
                    int msgID = smsMessage.getMsgID();
                    if (smsMessage.getMessageType() == 4) {
                        ImsSmsImpl.this.handleStatusReport(smsMessage.getMsgID(), i, SmsMessage.FORMAT_3GPP2, convertToFrameworkSmsFormat);
                    } else {
                        ImsSmsImpl.this.onSmsReceived(msgID, SmsMessage.FORMAT_3GPP2, convertToFrameworkSmsFormat);
                    }
                }
                SmsLogger r0 = ImsSmsImpl.this.mSmsLogger;
                String r1 = ImsSmsImpl.this.LOG_TAG;
                r0.logAndAdd(r1, "< NEW_SMS : contentType = " + str + " messageId = " + i);
                if (!TelephonyFeatures.SHIP_BUILD) {
                    String r6 = ImsSmsImpl.this.LOG_TAG;
                    Log.d(r6, "pdu = " + IccUtils.bytesToHexString(bArr));
                }
            }
        }

        public void onReceiveSMSAck(int i, int i2, String str, byte[] bArr, int i3) {
            int i4 = "application/vnd.3gpp2.sms".equals(str) ? 1 : 2;
            int r3 = ImsSmsImpl.this.getTokenByMessageId(i);
            if (r3 == -1) {
                String r8 = ImsSmsImpl.this.LOG_TAG;
                Log.i(r8, "messageID = " + i + " cannot find token");
                return;
            }
            SmsResponse smsResponse = new SmsResponse(i, i2, bArr, i4);
            ImsSmsImpl imsSmsImpl = ImsSmsImpl.this;
            imsSmsImpl.onReceiveSMSSuccssAcknowledgment(imsSmsImpl.mPhoneId, r3, i, i2, i3, smsResponse);
        }

        public void onReceiveSMSDeliveryReportAck(int i, int i2, int i3) {
            Mno simMno = SimUtil.getSimMno(ImsSmsImpl.this.mPhoneId);
            ImsSmsImpl.this.mSmsLogger.logAndAdd(ImsSmsImpl.this.LOG_TAG, "< SMS_ACK : mno " + simMno + " messageId " + i + " reasonCode " + i2 + " retryAfter " + i3);
            if (simMno != Mno.KDDI || i3 == -1 || ImsSmsImpl.this.mSentDeliveryAck == null || ImsSmsImpl.this.mSentDeliveryAck.mRetryCount >= 4) {
                ImsSmsImpl.this.onReceiveSmsDeliveryReportAck(i, i2);
                return;
            }
            ImsSmsImpl.this.mHandler.sendMessageDelayed(ImsSmsImpl.this.mHandler.obtainMessage(4, ImsSmsImpl.this.mSentDeliveryAck), ((long) i3) * 1000);
            ImsSmsImpl.this.mSentDeliveryAck.mRetryCount++;
        }
    }

    private static class ImsSmsTracker {
        public String mContentType;
        private final HashMap<String, Object> mData;
        public final String mDestAddress;
        public int mMessageId;
        public byte[] mPdu;
        public int mPhoneId;
        public int mRetryCount;
        public boolean mSentComplete;
        public boolean mStatusReportRequested;
        public int mToken;

        private ImsSmsTracker(int i, HashMap<String, Object> hashMap, int i2, int i3, int i4, byte[] bArr, String str, String str2, boolean z, boolean z2) {
            this.mPhoneId = i;
            this.mData = hashMap;
            this.mToken = i2;
            this.mRetryCount = i3;
            this.mMessageId = i4;
            this.mPdu = bArr;
            this.mDestAddress = str;
            this.mContentType = str2;
            this.mStatusReportRequested = z;
            this.mSentComplete = z2;
        }

        public int getToken() {
            return this.mToken;
        }

        public int getRetryCount() {
            return this.mRetryCount;
        }

        public int getMessageId() {
            return this.mMessageId;
        }

        public HashMap<String, Object> getData() {
            return this.mData;
        }
    }

    private static final class LastSentDeliveryAck {
        public int mNetworkType;
        public byte[] mPdu;
        public int mRetryCount = 0;

        public LastSentDeliveryAck(byte[] bArr, int i, int i2) {
            this.mPdu = bArr;
            this.mNetworkType = i2;
        }
    }
}
