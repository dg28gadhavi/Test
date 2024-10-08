package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.SignalStrengthWrapper;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.servicemodules.volte2.data.EcholocateEvent;
import com.sec.internal.ims.util.ImsPhoneStateManager;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.handler.IMiscHandler;
import com.sec.internal.log.IMSLog;
import com.sec.sve.generalevent.VcidEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TmoEcholocateController extends Handler {
    private static final int EVENT_CALL_STATUS_CHANGE_EVENT = 4;
    private static final int EVENT_ECHOLOCATE_EMERGENCY_TIMER_STATE_RECEIVED = 7;
    private static final int EVENT_ECHOLOCATE_HANDOVER_RECEIVED = 8;
    private static final int EVENT_ECHOLOCATE_RECEIVED = 1;
    protected static final int EVENT_ECHOLOCATE_REMOVE_CALLID_CACHE = 3;
    protected static final int EVENT_ECHOLOCATE_SIP_RECEIVED = 2;
    private static final int EVENT_HANDOVER_SUCCESS = 5;
    private static final int EVENT_PDN_DISCONNECT = 6;
    private static final String LOG_TAG = "Echolocate_Controller";
    protected Map<String, String> mCallIDList = new HashMap();
    /* access modifiers changed from: private */
    public boolean mCallOffhook = false;
    /* access modifiers changed from: private */
    public int mCallState;
    private final Context mContext;
    /* access modifiers changed from: private */
    public long mDiffTime = 0;
    /* access modifiers changed from: private */
    public boolean[] mEPSFBsuccess;
    protected TmoEcholocateBroadcaster mEchoBroadcaster = null;
    protected TmoEcholocateInfo mEchoInfo = null;
    private IMiscHandler mMiscHandler = null;
    protected VolteServiceModuleInternal mModule = null;
    private int mPhoneCount = 1;
    private ImsPhoneStateManager mPhoneStateManager;
    private String mSalesCode = "";
    /* access modifiers changed from: private */
    public SignalStrengthWrapper[] mSignalStrength = null;

    private class PhoneStateListenerInternal extends PhoneStateListener {
        int mPhoneId;
        int mState = 0;

        public PhoneStateListenerInternal(int i) {
            this.mPhoneId = i;
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (signalStrength != null) {
                TmoEcholocateController.this.mSignalStrength[this.mPhoneId] = new SignalStrengthWrapper(signalStrength);
            } else {
                Log.i(TmoEcholocateController.LOG_TAG, "getLteSignalStrength is null");
            }
        }

        public void onCallStateChanged(int i, String str) {
            if (this.mState != i) {
                if (i == 2) {
                    TmoEcholocateController.this.mCallOffhook = true;
                } else {
                    TmoEcholocateController.this.mCallOffhook = false;
                    if (i == 0 && TmoEcholocateController.this.mModule.getSessionCount(this.mPhoneId) == 0) {
                        TmoEcholocateController tmoEcholocateController = TmoEcholocateController.this;
                        TmoEcholocateInfo tmoEcholocateInfo = tmoEcholocateController.mEchoInfo;
                        tmoEcholocateController.mCallState = 0;
                        TmoEcholocateController.this.mEPSFBsuccess[this.mPhoneId] = false;
                        TmoEcholocateController.this.mDiffTime = 0;
                        TmoEcholocateController.this.mEchoBroadcaster.reset(this.mPhoneId);
                    }
                }
                Log.i(TmoEcholocateController.LOG_TAG, "onCallStateChanged[" + this.mPhoneId + "] " + this.mState + "->" + i + ", mCallOffhook:" + TmoEcholocateController.this.mCallOffhook + " Number [" + IMSLog.checker(str) + "]");
                this.mState = i;
                TmoEcholocateController tmoEcholocateController2 = TmoEcholocateController.this;
                tmoEcholocateController2.sendMessageDelayed(tmoEcholocateController2.obtainMessage(4, this.mPhoneId, i, str), 1000);
            }
        }

        public void onDataConnectionStateChanged(int i, int i2) {
            if (i == 0 && i2 == 20) {
                TmoEcholocateController tmoEcholocateController = TmoEcholocateController.this;
                tmoEcholocateController.sendMessage(tmoEcholocateController.obtainMessage(6, Integer.valueOf(this.mPhoneId)));
            }
        }
    }

    public TmoEcholocateController(Context context, VolteServiceModuleInternal volteServiceModuleInternal, IPdnController iPdnController, int i, Looper looper) {
        super(looper);
        this.mModule = volteServiceModuleInternal;
        this.mContext = context;
        this.mMiscHandler = ImsRegistry.getHandlerFactory().getMiscHandler();
        this.mPhoneCount = i;
        this.mSignalStrength = new SignalStrengthWrapper[i];
        this.mEPSFBsuccess = new boolean[i];
        this.mPhoneStateManager = new ImsPhoneStateManager(context, MNO.MOD_QATAR);
        TmoEcholocateInfo tmoEcholocateInfo = new TmoEcholocateInfo(context, this, iPdnController, volteServiceModuleInternal);
        this.mEchoInfo = tmoEcholocateInfo;
        this.mEchoBroadcaster = new TmoEcholocateBroadcaster(context, this, tmoEcholocateInfo);
    }

    public void start() {
        this.mMiscHandler.registerForEcholocateEvent(this, 1, (Object) null);
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (this.mPhoneStateManager.hasListener(i)) {
                this.mPhoneStateManager.unRegisterListener(i);
            }
            int subId = SimUtil.getSubId(i);
            Log.i(LOG_TAG, "registerListener pCnt[" + i + "], subId[" + subId + "]");
            this.mPhoneStateManager.registerListener(new PhoneStateListenerInternal(i), subId, i);
        }
        setSalesCode();
        Log.i(LOG_TAG, VcidEvent.BUNDLE_VALUE_ACTION_START);
    }

    public void stop() {
        this.mMiscHandler.unregisterForEcholocateEvent(this);
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (this.mPhoneStateManager.hasListener(i)) {
                this.mPhoneStateManager.unRegisterListener(i);
            }
        }
        Log.i(LOG_TAG, VcidEvent.BUNDLE_VALUE_ACTION_STOP);
    }

    public void handleEcholocateSipReceived(Message message) {
        EcholocateEvent.EchoSignallingIntentData echoSignallingIntentData = (EcholocateEvent.EchoSignallingIntentData) message.obj;
        EcholocateEvent.EcholocateSignalMessage signalMsg = echoSignallingIntentData.getSignalMsg();
        String origin = signalMsg.getOrigin();
        String cseq = signalMsg.getCseq();
        Log.i(LOG_TAG, "handleEcholocateSipReceived: origin = " + origin + ", cseq = " + cseq);
        if (!"RECEIVED".equals(origin) || !cseq.contains("REGISTER")) {
            this.mEchoBroadcaster.sendTmoEcholocateSignallingMSG(echoSignallingIntentData);
            return;
        }
        ImsRegistration imsRegistration = (ImsRegistration) Optional.ofNullable(this.mModule.getSessionByRegId(Integer.parseInt(signalMsg.getSessionid()))).map(new TmoEcholocateController$$ExternalSyntheticLambda0()).orElse((Object) null);
        if (imsRegistration == null) {
            Log.i(LOG_TAG, "handleEcholocateSipReceived: No VoLTE registration. Return..");
            return;
        }
        boolean z = imsRegistration.getRegiRat() == 18;
        Log.i(LOG_TAG, "handleEcholocateSipReceived: isEpdgRegistered = " + z + ", isEpdgCall = " + signalMsg.isEpdgCall());
        if (signalMsg.isEpdgCall() != z) {
            this.mEchoBroadcaster.sendTmoEcholocateHandoverFail(echoSignallingIntentData);
        }
    }

    public void handleMessage(Message message) {
        Log.i(LOG_TAG, "handleMessage: evt " + message.what);
        switch (message.what) {
            case 1:
                handleEcholocateEventReceived((AsyncResult) message.obj);
                return;
            case 2:
                handleEcholocateSipReceived(message);
                return;
            case 3:
                handleRemoveCallId((String) message.obj);
                return;
            case 4:
                this.mEchoBroadcaster.sendTmoEcholocateCarrierConfig(message.arg1, message.arg2, (String) message.obj);
                return;
            case 5:
                this.mEchoBroadcaster.sendDetailCallEvent(message.arg1, (EcholocateEvent.EcholocateHandoverMessage) message.obj);
                return;
            case 6:
                if (this.mCallState == 1) {
                    handleTmoEcholocateEPSFB(message.arg1, 3, 0);
                    return;
                }
                return;
            case 7:
                this.mEchoBroadcaster.sendEmergencyCallTimerStateMSG(message.arg1, (EcholocateEvent.EcholocateEmergencyMessage) message.obj);
                return;
            case 8:
                this.mEchoBroadcaster.sendDedicatedEventAfterHandover(((Integer) message.obj).intValue());
                return;
            default:
                Log.i(LOG_TAG, "This message is not supported");
                return;
        }
    }

    private void handleEcholocateEventReceived(AsyncResult asyncResult) {
        if (this.mEchoInfo.checkSecurity(this.mSalesCode)) {
            EcholocateEvent echolocateEvent = (EcholocateEvent) asyncResult.result;
            if (echolocateEvent.getType() == EcholocateEvent.EcholocateType.signalMsg) {
                EcholocateEvent.EcholocateSignalMessage signalData = echolocateEvent.getSignalData();
                int parseInt = Integer.parseInt(signalData.getSessionid());
                boolean isEpdgCall = signalData.isEpdgCall();
                int phoneIdFromSessionId = this.mEchoInfo.getPhoneIdFromSessionId(parseInt);
                String networkType = this.mEchoInfo.getNetworkType(phoneIdFromSessionId, isEpdgCall);
                sendMessage(obtainMessage(2, new EcholocateEvent.EchoSignallingIntentData(signalData, this.mEchoInfo.getLteBand(phoneIdFromSessionId, isEpdgCall, networkType), this.mEchoInfo.getNwStateSignal(phoneIdFromSessionId, isEpdgCall), networkType, this.mEchoInfo.getTimeStamp(0))));
            } else if (echolocateEvent.getType() == EcholocateEvent.EcholocateType.rtpMsg) {
                this.mEchoBroadcaster.sendTmoEcholocateRTP(echolocateEvent.getRtpData());
            }
        } else {
            Log.i(LOG_TAG, "Do not broadcast. ICDV or Signature key is wrong");
        }
    }

    private void handleRemoveCallId(String str) {
        if (this.mCallIDList.containsKey(str)) {
            Log.i(LOG_TAG, "Remove Call id on cache");
            if (this.mCallIDList.size() == 1) {
                this.mCallIDList.clear();
            } else {
                this.mCallIDList.remove(str);
            }
        }
    }

    public void handleTmoEcholocatePSHO(int i, int i2, int i3, int i4, long j) {
        int i5 = i;
        int i6 = i2;
        Log.i(LOG_TAG, "sendTmoEcholocatePSHO state : " + i6 + " mCallState : " + this.mCallState);
        ImsCallSession foregroundSession = this.mModule.getForegroundSession(i5);
        if (foregroundSession == null) {
            Log.i(LOG_TAG, "imsCallSession is not valid - STOP");
            return;
        }
        String networkTypeForPSHO = this.mEchoInfo.getNetworkTypeForPSHO(i5, i6, i3, i4);
        String lteBand = this.mEchoInfo.getLteBand(i5, false, networkTypeForPSHO);
        String pSHOState = this.mEchoInfo.getPSHOState(i6);
        String dialingNumber = foregroundSession.getCallProfile().getDialingNumber();
        String echoCallId = foregroundSession.getCallProfile().getEchoCallId();
        String cellId = this.mEchoInfo.getCellId(i5, networkTypeForPSHO, false);
        if (i6 == 2 || "0".equals(cellId) || "-1".equals(cellId)) {
            cellId = String.valueOf(j);
        }
        EcholocateEvent.EcholocateHandoverMessage echolocateHandoverMessage = new EcholocateEvent.EcholocateHandoverMessage(dialingNumber, pSHOState, networkTypeForPSHO, this.mEchoInfo.getNwStateSignal(i5, false), lteBand, echoCallId, this.mEchoInfo.getTimeStamp(0), cellId);
        if (i6 == 1) {
            this.mEchoBroadcaster.sendDetailCallEvent(i5, echolocateHandoverMessage);
            this.mCallState = 1;
        } else if (i6 == 4 || i6 == 3) {
            if (hasMessages(5)) {
                removeMessages(5);
            }
            this.mEchoBroadcaster.sendDetailCallEvent(i5, echolocateHandoverMessage);
            this.mCallState = 0;
        } else if (this.mCallState == 1 && i6 == 2) {
            sendMessageDelayed(obtainMessage(5, i5, 0, echolocateHandoverMessage), 200);
            this.mCallState = 0;
        }
    }

    public void handleTmoEcholocateEPSFB(int i, int i2, long j) {
        int i3 = i;
        int i4 = i2;
        Log.i(LOG_TAG, "sendTmoEcholocateEPSFB state : " + i4 + " mCallState : " + this.mCallState + " EPSFBsuccess[" + i3 + "]: " + this.mEPSFBsuccess[i3]);
        ImsCallSession preCallSession = this.mEchoInfo.getPreCallSession(i3);
        if (preCallSession == null) {
            Log.i(LOG_TAG, "imsCallSession is not valid - STOP");
            return;
        }
        String networkTypeForEPSFB = this.mEchoInfo.getNetworkTypeForEPSFB(i4);
        String lteBand = this.mEchoInfo.getLteBand(i3, false, networkTypeForEPSFB);
        String ePSFBState = this.mEchoInfo.getEPSFBState(i4);
        String dialingNumber = preCallSession.getCallProfile().getDialingNumber();
        String echoCallId = preCallSession.getCallProfile().getEchoCallId();
        String cellId = this.mEchoInfo.getCellId(i3, networkTypeForEPSFB, false);
        if (i4 == 2 || "0".equals(cellId) || "-1".equals(cellId)) {
            cellId = String.valueOf(j);
        }
        String nwStateSignal = this.mEchoInfo.getNwStateSignal(i3, false);
        EcholocateEvent.EcholocateHandoverMessage echolocateHandoverMessage = r6;
        String timeStamp = this.mEchoInfo.getTimeStamp(0);
        String str = LOG_TAG;
        EcholocateEvent.EcholocateHandoverMessage echolocateHandoverMessage2 = new EcholocateEvent.EcholocateHandoverMessage(dialingNumber, ePSFBState, networkTypeForEPSFB, nwStateSignal, lteBand, echoCallId, timeStamp, cellId);
        if (i4 == 1) {
            this.mEchoBroadcaster.sendDetailCallEvent(i3, echolocateHandoverMessage);
            this.mCallState = 1;
            this.mEPSFBsuccess[i3] = false;
        } else if (i4 == 4 || i4 == 3) {
            if (hasMessages(5)) {
                removeMessages(5);
            }
            this.mEchoBroadcaster.sendDetailCallEvent(i3, echolocateHandoverMessage);
            this.mCallState = 0;
            this.mEPSFBsuccess[i3] = false;
            this.mEchoBroadcaster.sendPendingSignallingMSG(0);
        } else if (this.mCallState == 1 && i4 == 2) {
            long j2 = this.mDiffTime;
            if (j2 != 0) {
                echolocateHandoverMessage.setTime(j2);
                this.mDiffTime = 0;
            }
            sendMessageDelayed(obtainMessage(5, i3, 0, echolocateHandoverMessage), 200);
            this.mCallState = 0;
            this.mEPSFBsuccess[i3] = true;
            Log.i(str, "set EPSFB:" + preCallSession.mSessionId);
            preCallSession.getCallProfile().setEPSFBsuccess(true);
            preCallSession.getCallProfile().setEchoCellId(cellId);
            this.mEchoBroadcaster.sendPendingSignallingMSG(j);
        }
    }

    public void handleEmergencyCallTimerState(int i, EcholocateEvent.EcholocateEmergencyMessage echolocateEmergencyMessage) {
        if (this.mEchoInfo.checkSecurity(this.mSalesCode)) {
            Log.i(LOG_TAG, "sendEmergencyCallTimerState");
            sendMessage(obtainMessage(7, i, 0, echolocateEmergencyMessage));
        }
    }

    public void handleDedicatedEventAfterHandover(int i) {
        if (!this.mEchoInfo.checkSecurity(this.mSalesCode)) {
            Log.i(LOG_TAG, "handleDedicatedEventAfterHandover - Do not broadcast.");
        } else if (hasMessages(8)) {
            Log.i(LOG_TAG, "EVENT_ECHOLOCATE_HANDOVER_RECEIVED requested, return");
        } else {
            Log.i(LOG_TAG, "handleDedicatedEventAfterHandover wait 2 seconds");
            sendMessageDelayed(obtainMessage(8, Integer.valueOf(i)), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        }
    }

    /* access modifiers changed from: protected */
    public int getCallState() {
        return this.mCallState;
    }

    /* access modifiers changed from: protected */
    public int getPhoneCount() {
        return this.mPhoneCount;
    }

    /* access modifiers changed from: protected */
    public long setDiffTime(long j) {
        this.mDiffTime = j;
        return j;
    }

    /* access modifiers changed from: protected */
    public long getDiffTime() {
        return this.mDiffTime;
    }

    public String getEchoCallId(String str) {
        return this.mCallIDList.get(str);
    }

    private void setSalesCode() {
        try {
            this.mSalesCode = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY);
        } catch (Exception unused) {
            Log.d(LOG_TAG, "Problem getting sales code!");
        }
        if (this.mSalesCode == null) {
            this.mSalesCode = "";
        }
        Log.d(LOG_TAG, "sales_code : " + this.mSalesCode);
    }

    /* access modifiers changed from: protected */
    public String getSalescode() {
        return this.mSalesCode;
    }

    /* access modifiers changed from: protected */
    public void setEPSFBsuccess(int i, boolean z) {
        Log.i(LOG_TAG, "setEPSFBsuccess[" + i + "]: " + z);
        this.mEPSFBsuccess[i] = z;
    }

    /* access modifiers changed from: protected */
    public boolean getEPSFBsuccess(int i) {
        Log.i(LOG_TAG, "getEPSFBsuccess[" + i + "]: " + this.mEPSFBsuccess[i]);
        return this.mEPSFBsuccess[i];
    }

    /* access modifiers changed from: protected */
    public SignalStrengthWrapper getSignalStrength(int i) {
        return this.mSignalStrength[i];
    }
}
