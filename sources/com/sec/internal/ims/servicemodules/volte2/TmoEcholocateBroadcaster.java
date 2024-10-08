package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.ims.servicemodules.volte2.data.EcholocateEvent;
import com.sec.internal.log.IMSLog;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;

public class TmoEcholocateBroadcaster {
    private static final String LOG_TAG = "Echolocate_Broadcaster";
    private final Context mContext;
    private TmoEcholocateController mEchoController;
    private TmoEcholocateInfo mEchoInfo;
    protected Queue<Intent> mPendingQue = new LinkedList();
    protected boolean[] mRetryINVITE;

    public TmoEcholocateBroadcaster(Context context, TmoEcholocateController tmoEcholocateController, TmoEcholocateInfo tmoEcholocateInfo) {
        this.mContext = context;
        this.mEchoController = tmoEcholocateController;
        this.mEchoInfo = tmoEcholocateInfo;
        this.mRetryINVITE = new boolean[tmoEcholocateController.getPhoneCount()];
    }

    public void reset(int i) {
        this.mRetryINVITE[i] = false;
        this.mPendingQue.clear();
    }

    /* access modifiers changed from: protected */
    public void sendDetailCallEvent(int i, EcholocateEvent.EcholocateHandoverMessage echolocateHandoverMessage) {
        if (!this.mEchoInfo.checkSecurity(this.mEchoController.getSalescode())) {
            Log.i(LOG_TAG, "sendDetailCallEvent - Do not broadcast.");
            return;
        }
        Intent intent = new Intent("diagandroid.phone.detailedCallState");
        intent.putExtra("CallNumber", echolocateHandoverMessage.getCallNumber());
        intent.putExtra("CallState", echolocateHandoverMessage.getCallState());
        intent.putExtra("VoiceAccessNetworkStateType", echolocateHandoverMessage.getNetworkType());
        intent.putExtra("VoiceAccessNetworkStateBand", echolocateHandoverMessage.getNetworkBand());
        intent.putExtra("VoiceAccessNetworkStateSignal", echolocateHandoverMessage.getNetworkSignal());
        intent.putExtra("CallID", echolocateHandoverMessage.getCallId());
        intent.putExtra("oemIntentTimestamp", echolocateHandoverMessage.getTime());
        intent.putExtra("cellid", echolocateHandoverMessage.getCellId());
        intent.putExtra("EpdgHoFailureCause", "NA");
        this.mContext.sendBroadcast(intent, "diagandroid.phone.receiveDetailedCallState");
        Log.i(LOG_TAG, "sendEPSFB state for now");
    }

    /* access modifiers changed from: protected */
    public void sendPendingSignallingMSG(long j) {
        while (!this.mPendingQue.isEmpty()) {
            Intent poll = this.mPendingQue.poll();
            if (j > 0) {
                poll.putExtra("cellid", String.valueOf(j));
                poll.putExtra("RAT", "3GPP-E-UTRAN-FDD");
            }
            Log.i(LOG_TAG, "sendPendingSignallingMSG :: Origin " + poll.getStringExtra("IMSSignallingMessageOrigin") + " oemIntentTimestamp " + poll.getStringExtra("oemIntentTimestamp"));
            this.mContext.sendBroadcast(poll, "diagandroid.phone.receiveDetailedCallState");
        }
    }

    /* access modifiers changed from: protected */
    public void sendTmoEcholocateHandoverFail(EcholocateEvent.EchoSignallingIntentData echoSignallingIntentData) {
        String str;
        String str2;
        if (!this.mEchoInfo.checkSecurity(this.mEchoController.getSalescode())) {
            Log.i(LOG_TAG, "sendTmoEcholocateHandoverFail: sendDetailCallEvent - Do not broadcast.");
            return;
        }
        Intent intent = new Intent("diagandroid.phone.detailedCallState");
        intent.putExtra("VoiceAccessNetworkStateBand", echoSignallingIntentData.getNetworkBand());
        intent.putExtra("VoiceAccessNetworkStateSignal", echoSignallingIntentData.getNetworkSignal());
        intent.putExtra("oemIntentTimestamp", echoSignallingIntentData.getTime());
        String networkType = echoSignallingIntentData.getNetworkType();
        intent.putExtra("VoiceAccessNetworkStateType", networkType);
        EcholocateEvent.EcholocateSignalMessage signalMsg = echoSignallingIntentData.getSignalMsg();
        ImsCallSession sessionByRegId = this.mEchoController.mModule.getSessionByRegId(Integer.parseInt(signalMsg.getSessionid()));
        intent.putExtra("cellid", this.mEchoInfo.getCellId(sessionByRegId.getPhoneId(), networkType, signalMsg.isEpdgCall()));
        if (sessionByRegId.getCallProfile() != null) {
            str2 = sessionByRegId.getCallProfile().getDialingNumber();
            str = sessionByRegId.getCallProfile().getEchoCallId();
        } else {
            str2 = null;
            str = null;
        }
        intent.putExtra("CallState", "EPDG_HO_FAILED");
        intent.putExtra("CallID", str);
        intent.putExtra("CallNumber", str2);
        String str3 = "IMS_REGISTRATION_FAILURE_AFTER_HO_" + signalMsg.getLine1().split(" ")[1];
        intent.putExtra("EpdgHoFailureCause", str3);
        this.mContext.sendBroadcast(intent, "diagandroid.phone.receiveDetailedCallState");
        Log.i(LOG_TAG, "sendTmoEcholocateHandoverFail :: Origin [" + signalMsg.getOrigin() + "] Line1 [ " + IMSLog.checker(signalMsg.getLine1()) + "] Cseq [" + signalMsg.getCseq() + "] Reason [" + signalMsg.getReason() + "] callId_App [" + str + "] callId_IMS [" + signalMsg.getCallId() + "] handoverFailString [" + str3 + "]");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x02cf, code lost:
        r5 = r5.substring(r6 + 1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendTmoEcholocateSignallingMSG(com.sec.internal.ims.servicemodules.volte2.data.EcholocateEvent.EchoSignallingIntentData r24) {
        /*
            r23 = this;
            r0 = r23
            com.sec.internal.ims.servicemodules.volte2.data.EcholocateEvent$EcholocateSignalMessage r1 = r24.getSignalMsg()
            android.content.Intent r2 = new android.content.Intent
            java.lang.String r3 = "diagandroid.phone.imsSignallingMessage"
            r2.<init>(r3)
            java.lang.String r3 = r1.getSessionid()
            int r3 = java.lang.Integer.parseInt(r3)
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo r4 = r0.mEchoInfo
            int r4 = r4.getPhoneIdFromSessionId(r3)
            java.lang.String r5 = r1.getOrigin()
            java.lang.String r6 = "SENT"
            boolean r5 = r6.equals(r5)
            r7 = 100
            r9 = 1
            r10 = 0
            java.lang.String r11 = "Echolocate_Broadcaster"
            if (r5 == 0) goto L_0x0073
            java.lang.String r5 = r1.getLine1()
            java.lang.String r12 = "INVITE"
            boolean r5 = r5.contains(r12)
            if (r5 == 0) goto L_0x0073
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r12 = "Check mRetryINVITE["
            r5.append(r12)
            r5.append(r4)
            java.lang.String r12 = "]: "
            r5.append(r12)
            boolean[] r12 = r0.mRetryINVITE
            boolean r12 = r12[r4]
            r5.append(r12)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r11, r5)
            boolean[] r5 = r0.mRetryINVITE
            boolean r12 = r5[r4]
            if (r12 == 0) goto L_0x0062
            r5[r4] = r10
            goto L_0x0073
        L_0x0062:
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r1 = r0.mEchoController
            r2 = 2
            r5 = r24
            android.os.Message r2 = r1.obtainMessage(r2, r5)
            r1.sendMessageDelayed(r2, r7)
            boolean[] r0 = r0.mRetryINVITE
            r0[r4] = r9
            return
        L_0x0073:
            r5 = r24
            java.lang.String r12 = r1.getCallId()
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            java.lang.String r14 = "CSeq: "
            r13.append(r14)
            java.lang.String r14 = r1.getCseq()
            r13.append(r14)
            java.lang.String r13 = r13.toString()
            java.lang.String r14 = r1.getContents()
            boolean r15 = android.text.TextUtils.isEmpty(r14)
            java.lang.String r16 = "NA"
            if (r15 == 0) goto L_0x009d
            r14 = r16
            goto L_0x00a3
        L_0x009d:
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo r15 = r0.mEchoInfo
            java.lang.String r14 = r15.getSDPContents(r14)
        L_0x00a3:
            java.lang.String r15 = "VoiceAccessNetworkStateBand"
            java.lang.String r7 = r24.getNetworkBand()
            r2.putExtra(r15, r7)
            java.lang.String r7 = "VoiceAccessNetworkStateSignal"
            java.lang.String r8 = r24.getNetworkSignal()
            r2.putExtra(r7, r8)
            java.lang.String r7 = "IMSSignallingMessageCallID"
            r2.putExtra(r7, r12)
            java.lang.String r7 = "IMSSignallingCSeq"
            r2.putExtra(r7, r13)
            java.lang.String r7 = "IMSSignallingMessageLine1"
            java.lang.String r8 = r1.getLine1()
            r2.putExtra(r7, r8)
            java.lang.String r7 = "IMSSignallingMessageOrigin"
            java.lang.String r8 = r1.getOrigin()
            r2.putExtra(r7, r8)
            java.lang.String r7 = "IMSSignallingMessageSDP"
            r2.putExtra(r7, r14)
            java.lang.String r7 = "oemIntentTimestamp"
            java.lang.String r8 = r24.getTime()
            r2.putExtra(r7, r8)
            java.lang.String r5 = r24.getNetworkType()
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r7 = r0.mEchoController
            com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r7 = r7.mModule
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r7.getSession(r3)
            java.lang.String r7 = ", e:"
            java.lang.String r8 = "mCallIDList add [s:"
            java.lang.String r15 = "]"
            if (r3 == 0) goto L_0x018d
            com.sec.ims.volte2.data.CallProfile r17 = r3.getCallProfile()
            if (r17 == 0) goto L_0x018d
            com.sec.ims.volte2.data.CallProfile r17 = r3.getCallProfile()
            java.lang.String r17 = r17.getDialingNumber()
            com.sec.ims.volte2.data.CallProfile r18 = r3.getCallProfile()
            java.lang.String r18 = r18.getEchoCallId()
            boolean r19 = r3.isEpdgCall()
            int r20 = r3.getEndReason()
            boolean r21 = android.text.TextUtils.isEmpty(r18)
            if (r21 == 0) goto L_0x0156
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r9 = r0.mEchoController
            java.util.Map<java.lang.String, java.lang.String> r9 = r9.mCallIDList
            com.sec.ims.volte2.data.CallProfile r18 = r3.getCallProfile()
            java.lang.String r10 = r18.getSipCallId()
            java.lang.Object r9 = r9.get(r10)
            java.lang.String r9 = (java.lang.String) r9
            boolean r10 = android.text.TextUtils.isEmpty(r9)
            if (r10 == 0) goto L_0x014c
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo r9 = r0.mEchoInfo
            java.lang.String r9 = r9.getNewAppCallId()
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            r22 = r14
            java.lang.String r14 = "create the echo callID "
            r10.append(r14)
            r10.append(r9)
            java.lang.String r10 = r10.toString()
            android.util.Log.i(r11, r10)
            goto L_0x014e
        L_0x014c:
            r22 = r14
        L_0x014e:
            com.sec.ims.volte2.data.CallProfile r10 = r3.getCallProfile()
            r10.setEchoCallId(r9)
            goto L_0x015a
        L_0x0156:
            r22 = r14
            r9 = r18
        L_0x015a:
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r10 = r0.mEchoController
            java.util.Map<java.lang.String, java.lang.String> r10 = r10.mCallIDList
            boolean r10 = r10.containsKey(r12)
            if (r10 != 0) goto L_0x0186
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            r10.append(r8)
            r10.append(r12)
            r10.append(r7)
            r10.append(r9)
            r10.append(r15)
            java.lang.String r10 = r10.toString()
            android.util.Log.i(r11, r10)
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r10 = r0.mEchoController
            java.util.Map<java.lang.String, java.lang.String> r10 = r10.mCallIDList
            r10.put(r12, r9)
        L_0x0186:
            r24 = r9
            r10 = r19
            r14 = r20
            goto L_0x0195
        L_0x018d:
            r22 = r14
            r17 = 0
            r24 = r17
            r10 = 0
            r14 = 0
        L_0x0195:
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo r9 = r0.mEchoInfo
            boolean r9 = r9.isEndCall(r13)
            r18 = r13
            java.lang.String r13 = "Reason:"
            if (r9 == 0) goto L_0x020e
            java.lang.String r9 = r1.getOrigin()
            boolean r6 = r6.equals(r9)
            if (r6 == 0) goto L_0x01ef
            java.lang.String r6 = r1.getLine1()
            java.lang.String r9 = "CANCEL"
            boolean r6 = r6.contains(r9)
            if (r6 != 0) goto L_0x01c3
            java.lang.String r6 = r1.getLine1()
            java.lang.String r9 = "BYE"
            boolean r6 = r6.contains(r9)
            if (r6 == 0) goto L_0x01ef
        L_0x01c3:
            r6 = 14
            if (r14 != r6) goto L_0x01ca
            java.lang.String r16 = "DeviceReason:De-Reg"
            goto L_0x022b
        L_0x01ca:
            java.lang.String r6 = r1.getReason()
            boolean r6 = android.text.TextUtils.isEmpty(r6)
            if (r6 == 0) goto L_0x01d7
            java.lang.String r6 = "DeviceReason:Normal"
            goto L_0x01ec
        L_0x01d7:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r9 = "DeviceReason:"
            r6.append(r9)
            java.lang.String r9 = r1.getReason()
            r6.append(r9)
            java.lang.String r6 = r6.toString()
        L_0x01ec:
            r16 = r6
            goto L_0x022b
        L_0x01ef:
            java.lang.String r6 = r1.getReason()
            boolean r6 = android.text.TextUtils.isEmpty(r6)
            if (r6 == 0) goto L_0x01fa
            goto L_0x022b
        L_0x01fa:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r13)
            java.lang.String r9 = r1.getReason()
            r6.append(r9)
            java.lang.String r6 = r6.toString()
            goto L_0x01ec
        L_0x020e:
            java.lang.String r6 = r1.getReason()
            boolean r6 = android.text.TextUtils.isEmpty(r6)
            if (r6 != 0) goto L_0x022b
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r13)
            java.lang.String r9 = r1.getReason()
            r6.append(r9)
            java.lang.String r16 = r6.toString()
        L_0x022b:
            r6 = r16
            java.lang.String r9 = "IMSSignallingMessageReason"
            r2.putExtra(r9, r6)
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo r6 = r0.mEchoInfo
            java.lang.String r6 = r6.getCellId(r4, r5, r10)
            java.lang.String r9 = "VoiceAccessNetworkStateType"
            r2.putExtra(r9, r5)
            java.lang.String r9 = "cellid"
            r2.putExtra(r9, r6)
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo r6 = r0.mEchoInfo
            java.lang.String r5 = r6.getRatType(r4, r5)
            java.lang.String r6 = "RAT"
            r2.putExtra(r6, r5)
            if (r3 != 0) goto L_0x02fd
            java.lang.String r5 = r1.getPeerNumber()
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r9 = "CallNumber from h_from:"
            r6.append(r9)
            java.lang.String r9 = com.sec.internal.log.IMSLog.checker(r5)
            r6.append(r9)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r11, r6)
            boolean r6 = android.text.TextUtils.isEmpty(r5)
            if (r6 == 0) goto L_0x0277
            java.lang.String r0 = "Can't find callNumber :: STOP"
            android.util.Log.i(r11, r0)
            return
        L_0x0277:
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r6 = r0.mEchoController
            java.util.Map<java.lang.String, java.lang.String> r6 = r6.mCallIDList
            java.lang.Object r6 = r6.get(r12)
            java.lang.String r6 = (java.lang.String) r6
            boolean r9 = android.text.TextUtils.isEmpty(r6)
            if (r9 == 0) goto L_0x02af
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo r6 = r0.mEchoInfo
            java.lang.String r6 = r6.getNewAppCallId()
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r9.append(r8)
            r9.append(r12)
            r9.append(r7)
            r9.append(r6)
            r9.append(r15)
            java.lang.String r7 = r9.toString()
            android.util.Log.i(r11, r7)
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r7 = r0.mEchoController
            java.util.Map<java.lang.String, java.lang.String> r7 = r7.mCallIDList
            r7.put(r12, r6)
        L_0x02af:
            r9 = r6
            r6 = 58
            int r6 = r5.indexOf(r6)
            if (r6 <= 0) goto L_0x02e2
            r7 = 0
            java.lang.String r8 = r5.substring(r7, r6)
            java.lang.String r7 = "sip"
            boolean r7 = r7.equalsIgnoreCase(r8)
            if (r7 != 0) goto L_0x02cf
            java.lang.String r7 = "tel"
            boolean r7 = r7.equalsIgnoreCase(r8)
            if (r7 == 0) goto L_0x02e2
        L_0x02cf:
            r7 = 1
            int r6 = r6 + r7
            java.lang.String r5 = r5.substring(r6)
            r6 = 64
            int r6 = r5.indexOf(r6)
            if (r6 <= 0) goto L_0x02e2
            r7 = 0
            java.lang.String r5 = r5.substring(r7, r6)
        L_0x02e2:
            r17 = r5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Extracted callNumber:"
            r5.append(r6)
            java.lang.String r6 = com.sec.internal.log.IMSLog.checker(r17)
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r11, r5)
            goto L_0x02ff
        L_0x02fd:
            r9 = r24
        L_0x02ff:
            r5 = r17
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo r6 = r0.mEchoInfo
            r7 = r18
            boolean r6 = r6.isEndCall(r7)
            if (r6 == 0) goto L_0x0317
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r6 = r0.mEchoController
            r7 = 3
            android.os.Message r7 = r6.obtainMessage(r7, r12)
            r12 = 1000(0x3e8, double:4.94E-321)
            r6.sendMessageDelayed(r7, r12)
        L_0x0317:
            java.lang.String r6 = "CallID"
            r2.putExtra(r6, r9)
            java.lang.String r6 = "CallNumber"
            r2.putExtra(r6, r5)
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r5 = r0.mEchoController
            int r5 = r5.getCallState()
            r6 = 1
            if (r5 != r6) goto L_0x036b
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r1 = r0.mEchoController
            long r7 = r1.getDiffTime()
            r9 = 0
            int r1 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r1 != 0) goto L_0x035f
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r1 = r0.mEchoController
            long r7 = java.lang.System.currentTimeMillis()
            r9 = 100
            long r7 = r7 - r9
            r1.setDiffTime(r7)
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r1 = r0.mEchoController
            r1.setEPSFBsuccess(r4, r6)
            if (r3 == 0) goto L_0x035f
            com.sec.ims.volte2.data.CallProfile r1 = r3.getCallProfile()
            if (r1 == 0) goto L_0x035f
            com.sec.ims.volte2.data.CallProfile r1 = r3.getCallProfile()
            r1.setEPSFBsuccess(r6)
            com.sec.ims.volte2.data.CallProfile r1 = r3.getCallProfile()
            java.lang.String r3 = "-1"
            r1.setEchoCellId(r3)
        L_0x035f:
            java.util.Queue<android.content.Intent> r0 = r0.mPendingQue
            r0.add(r2)
            java.lang.String r0 = "sendTmoEcholocateSignallingMSG :: pending case with EPSFB before SUCCESS"
            android.util.Log.i(r11, r0)
            return
        L_0x036b:
            android.content.Context r0 = r0.mContext
            java.lang.String r3 = "diagandroid.phone.receiveDetailedCallState"
            r0.sendBroadcast(r2, r3)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "sendTmoEcholocateSignallingMSG :: Origin ["
            r0.append(r2)
            java.lang.String r2 = r1.getOrigin()
            r0.append(r2)
            java.lang.String r2 = "] Line1 [ "
            r0.append(r2)
            java.lang.String r2 = r1.getLine1()
            java.lang.String r2 = com.sec.internal.log.IMSLog.checker(r2)
            r0.append(r2)
            java.lang.String r2 = "] Cseq ["
            r0.append(r2)
            java.lang.String r2 = r1.getCseq()
            r0.append(r2)
            java.lang.String r2 = "] Reason ["
            r0.append(r2)
            java.lang.String r2 = r1.getReason()
            r0.append(r2)
            java.lang.String r2 = "] callId_App ["
            r0.append(r2)
            r0.append(r9)
            java.lang.String r2 = "] callId_IMS ["
            r0.append(r2)
            java.lang.String r1 = r1.getCallId()
            r0.append(r1)
            java.lang.String r1 = "] sdpContents ["
            r0.append(r1)
            r14 = r22
            r0.append(r14)
            r0.append(r15)
            java.lang.String r0 = r0.toString()
            android.util.Log.i(r11, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.TmoEcholocateBroadcaster.sendTmoEcholocateSignallingMSG(com.sec.internal.ims.servicemodules.volte2.data.EcholocateEvent$EchoSignallingIntentData):void");
    }

    /* access modifiers changed from: protected */
    public void sendTmoEcholocateRTP(EcholocateEvent.EcholocateRtpMessage echolocateRtpMessage) {
        Intent intent;
        if (TextUtils.isEmpty(echolocateRtpMessage.getId())) {
            Log.i(LOG_TAG, "sendTmoEcholocateRTP :: Session Id is NULL");
            return;
        }
        String dir = echolocateRtpMessage.getDir();
        if ("DL".equals(dir)) {
            intent = new Intent("diagandroid.phone.RTPDLStat");
            intent.putExtra("RTPDownlinkStatusLossRate", echolocateRtpMessage.getLossrate());
            intent.putExtra("RTPDownlinkStatusDelay", echolocateRtpMessage.getDelay());
            intent.putExtra("RTPDownlinkStatusJitter", echolocateRtpMessage.getJitter());
            intent.putExtra("RTPDownlinkStatusMeasuredPeriod", echolocateRtpMessage.getMeasuredperiod());
        } else {
            intent = new Intent("diagandroid.phone.RTPULStat");
            intent.putExtra("RTPUplinkStatusLossRate", echolocateRtpMessage.getLossrate());
            intent.putExtra("RTPUplinkStatusDelay", echolocateRtpMessage.getDelay());
            intent.putExtra("RTPUplinkStatusJitter", echolocateRtpMessage.getJitter());
            intent.putExtra("RTPUplinkStatusMeasuredPeriod", echolocateRtpMessage.getMeasuredperiod());
        }
        int parseInt = Integer.parseInt(echolocateRtpMessage.getId());
        int phoneIdFromSessionId = this.mEchoInfo.getPhoneIdFromSessionId(parseInt);
        ImsCallSession session = this.mEchoController.mModule.getSession(parseInt);
        if (session == null) {
            Log.e(LOG_TAG, "Can't get call num from sessionID");
            return;
        }
        boolean isEpdgCall = session.isEpdgCall();
        String networkType = this.mEchoInfo.getNetworkType(phoneIdFromSessionId, isEpdgCall);
        intent.putExtra("VoiceAccessNetworkStateType", networkType);
        intent.putExtra("VoiceAccessNetworkStateSignal", this.mEchoInfo.getNwStateSignal(phoneIdFromSessionId, isEpdgCall));
        intent.putExtra("VoiceAccessNetworkStateBand", this.mEchoInfo.getLteBand(phoneIdFromSessionId, isEpdgCall, networkType));
        String dialingNumber = session.getCallProfile().getDialingNumber();
        if (TextUtils.isEmpty(dialingNumber)) {
            dialingNumber = "null";
        }
        String echoCallId = session.getCallProfile().getEchoCallId();
        if (TextUtils.isEmpty(echoCallId)) {
            Log.e(LOG_TAG, "Can't find echo CallId from session");
            return;
        }
        String cellId = this.mEchoInfo.getCellId(phoneIdFromSessionId, networkType, isEpdgCall);
        intent.putExtra("CallNumber", dialingNumber);
        intent.putExtra("CallID", echoCallId);
        intent.putExtra("oemIntentTimestamp", this.mEchoInfo.getTimeStamp(0));
        intent.putExtra("cellid", cellId);
        this.mContext.sendBroadcast(intent, "diagandroid.phone.receiveDetailedCallState");
        Log.i(LOG_TAG, "sendTmoEcholocateRTP :: dir [" + dir + "] LossRate [" + echolocateRtpMessage.getLossrate() + "] Jitter [" + echolocateRtpMessage.getJitter() + "] Measuredperiod [" + echolocateRtpMessage.getMeasuredperiod() + "] Delay [" + echolocateRtpMessage.getDelay() + "]");
    }

    /* access modifiers changed from: protected */
    public void sendEmergencyCallTimerStateMSG(int i, EcholocateEvent.EcholocateEmergencyMessage echolocateEmergencyMessage) {
        boolean isEpdgCall = echolocateEmergencyMessage.isEpdgCall();
        String networkType = this.mEchoInfo.getNetworkType(i, isEpdgCall);
        Intent intent = new Intent("diagandroid.phone.emergencyCallTimerState");
        intent.putExtra("CallNumber", echolocateEmergencyMessage.getCallNumber());
        intent.putExtra("TimerName", echolocateEmergencyMessage.getTimerName());
        intent.putExtra("TimerState", echolocateEmergencyMessage.getStateName());
        intent.putExtra("VoiceAccessNetworkStateType", networkType);
        intent.putExtra("VoiceAccessNetworkStateSignal", this.mEchoInfo.getNwStateSignal(i, isEpdgCall));
        intent.putExtra("VoiceAccessNetworkStateBand", this.mEchoInfo.getLteBand(i, isEpdgCall, networkType));
        intent.putExtra("CallID", echolocateEmergencyMessage.getCallId());
        intent.putExtra("oemIntentTimestamp", this.mEchoInfo.getTimeStamp(0));
        intent.putExtra("cellid", this.mEchoInfo.getCellId(i, networkType, isEpdgCall));
        Log.i(LOG_TAG, "sendEmergencyCallTimerStateMSG[" + i + "], callId = " + echolocateEmergencyMessage.getCallId() + ", timer=" + echolocateEmergencyMessage.getTimerName() + " state=" + echolocateEmergencyMessage.getStateName());
        this.mContext.sendBroadcastAsUser(intent, UserHandle.SEM_CURRENT, "diagandroid.phone.receiveDetailedCallState");
    }

    /* access modifiers changed from: protected */
    public void sendTmoEcholocateCarrierConfig(int i, int i2, String str) {
        if (!this.mEchoInfo.checkSecurity(this.mEchoController.getSalescode())) {
            Log.i(LOG_TAG, "Do not broadcast. ICDV or Signature key is wrong");
        } else if (i2 == 1 || i2 == 2) {
            ImsCallSession preCallSession = this.mEchoInfo.getPreCallSession(i);
            if (preCallSession == null) {
                Log.i(LOG_TAG, "phoneId is not valid - STOP");
                return;
            }
            String echoCallId = preCallSession.getCallProfile().getEchoCallId();
            if (TextUtils.isEmpty(str)) {
                Log.i(LOG_TAG, "phoneNumber is not valid - use call profile number");
                str = preCallSession.getCallProfile().getDialingNumber();
            }
            Intent intent = new Intent("diagandroid.phone.carrierConfig");
            String voiceConfig = this.mEchoInfo.getVoiceConfig();
            String voWiFiConfig = this.mEchoInfo.getVoWiFiConfig();
            LinkedHashMap<String, String> sa5gBandConfig = this.mEchoInfo.getSa5gBandConfig(i);
            String configVersion = this.mEchoInfo.getConfigVersion();
            Log.i(LOG_TAG, "sendTmoEcholocateCarrierConfig voiceconfig : " + voiceConfig + ", vowificonfig : " + voWiFiConfig + ", Sa5gbandconfig : " + sa5gBandConfig + ", configversion : " + configVersion + ", phoneId : " + i + ", callNumber : " + IMSLog.checker(str));
            intent.putExtra("carrierVoiceConfig", voiceConfig);
            intent.putExtra("carrierVoWiFiConfig", voWiFiConfig);
            intent.putExtra("carrierSa5gBandConfig", sa5gBandConfig);
            intent.putExtra("carrierConfigVersion", configVersion);
            intent.putExtra("CallID", echoCallId);
            intent.putExtra("CallNumber", str);
            intent.putExtra("oemIntentTimestamp", this.mEchoInfo.getTimeStamp(0));
            this.mContext.sendBroadcast(intent, "diagandroid.phone.receiveDetailedCallState");
        } else {
            Log.i(LOG_TAG, "sendTmoEcholocateCarrierConfig ignore callstate ");
        }
    }

    public void sendDedicatedEventAfterHandover(int i) {
        Log.i(LOG_TAG, "sendDedicatedEventAfterHandover:" + i);
        ImsCallSession foregroundSession = this.mEchoController.mModule.getForegroundSession(i);
        if (foregroundSession == null) {
            Log.i(LOG_TAG, "sendDedicatedEventAfterHandover - No call session.");
        } else if (foregroundSession.isEpdgCall()) {
            Log.i(LOG_TAG, "sendDedicatedEventAfterHandover - call is on EPDG.");
        } else {
            Log.i(LOG_TAG, "DedicatedBearer:" + foregroundSession.getDedicatedBearerState(1));
            if (foregroundSession.getDedicatedBearerState(1) == 3) {
                Intent intent = new Intent("diagandroid.phone.detailedCallState");
                intent.putExtra("CallNumber", foregroundSession.getCallProfile().getDialingNumber());
                intent.putExtra("CallState", "EPDG_HO_FAILED");
                String networkType = this.mEchoInfo.getNetworkType(i, false);
                intent.putExtra("VoiceAccessNetworkStateType", networkType);
                intent.putExtra("VoiceAccessNetworkStateBand", this.mEchoInfo.getLteBand(i, false, networkType));
                intent.putExtra("VoiceAccessNetworkStateSignal", this.mEchoInfo.getNwStateSignal(i, false));
                intent.putExtra("CallID", foregroundSession.getCallProfile().getEchoCallId());
                intent.putExtra("oemIntentTimestamp", this.mEchoInfo.getTimeStamp(0));
                intent.putExtra("cellid", this.mEchoInfo.getCellId(i, networkType, false));
                intent.putExtra("EpdgHoFailureCause", "5QI_QCI_1_FLOW_SETUP_FAILURE");
                this.mContext.sendBroadcast(intent, "diagandroid.phone.receiveDetailedCallState");
                Log.i(LOG_TAG, "sendDedicatedEventAfterHandover : 5QI_QCI_1_FLOW_SETUP_FAILURE, CallNumber:" + IMSLog.checker(foregroundSession.getCallProfile().getDialingNumber()) + ", CallID:" + foregroundSession.getCallProfile().getEchoCallId());
            }
        }
    }
}
