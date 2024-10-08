package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.core.handler.CmcHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.NotifyCmcRecordEventData;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.sve.ICmcMediaEventListener;
import com.sec.sve.SecVideoEngineManager;

public class ResipCmcHandler extends CmcHandler {
    private static final int EVENT_CMC_RECORD_EVENT = 303;
    private static final int EVENT_CONNECT_TO_SVE = 300;
    private static final int EVENT_DISCONNECT_TO_SVE = 302;
    private static final int EVENT_SVE_CONNECTED = 301;
    private static final int NOTIFY_RECORD_START_FAILURE = 51;
    private static final int NOTIFY_RECORD_START_FAILURE_NO_MEMORY = 52;
    private static final int NOTIFY_RECORD_START_SUCCESS = 50;
    private static final int NOTIFY_RECORD_STOP_FAILURE = 61;
    private static final int NOTIFY_RECORD_STOP_NO_MEMORY = 62;
    private static final int NOTIFY_RECORD_STOP_SUCCESS = 60;
    public static final int NOTIFY_RELAY_MEDIA_PAUSE = 4;
    public static final int NOTIFY_RELAY_MEDIA_RESUME = 3;
    public static final int NOTIFY_RELAY_MEDIA_START = 1;
    public static final int NOTIFY_RELAY_MEDIA_STOP = 2;
    private ICmcMediaEventListener mCmcMediaEventlistener = new ICmcMediaEventListener.Stub() {
        public void onRelayEvent(int i, int i2) {
            String access$000 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$000, "onRelayEvent streamId : " + i + " event : " + i2);
            if (i2 <= 0) {
                Log.e(ResipCmcHandler.this.LOG_TAG, "Invalid Relay Event");
            } else if (i2 == 1 || i2 == 2 || i2 == 3 || i2 == 4) {
                ResipCmcHandler.this.mStackIf.sendRelayEvent(i, i2);
            }
        }

        public void onRelayStreamEvent(int i, int i2, int i3) {
            String access$200 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$200, "onRelayStreamEvent streamId : " + i + " event : " + i2 + " Session Id : " + i3);
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setRelayStreamEvent(i2);
            iMSMediaEvent.setStreamId(i);
            iMSMediaEvent.setSessionID(i3);
            ResipCmcHandler.this.mCmcMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onRelayRtpStats(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setRelayStreamEvent(5);
            int i8 = i;
            iMSMediaEvent.setStreamId(i);
            int i9 = i2;
            iMSMediaEvent.setSessionID(i2);
            iMSMediaEvent.setRelayRtpStats(new IMSMediaEvent.AudioRtpStats(i, i3, i4, i5, i6, i7));
            ResipCmcHandler.this.mCmcMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onRelayChannelEvent(int i, int i2) {
            String access$500 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$500, "onRelayChannelEvent channelId : " + i + " event : " + i2);
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setRelayChannelEvent(i2);
            iMSMediaEvent.setRelayChannelId(i);
            ResipCmcHandler.this.mCmcMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onCmcRecordEvent(int i, int i2, int i3) {
            String access$700 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$700, "onCmcRecordEvent sessionId : " + i + " event : " + i2 + " arg : " + i3);
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setSessionID(i);
            iMSMediaEvent.setCmcRecordingEvent(i2);
            iMSMediaEvent.setCmcRecordingArg(i3);
            ResipCmcHandler.this.mCmcMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onCmcRecorderStoppedEvent(int i, int i2, String str) {
            String access$900 = ResipCmcHandler.this.LOG_TAG;
            Log.i(access$900, "onCmcRecorderStoppedEvent startTime : " + i + " , stopTime : " + i2);
        }
    };
    private int mCmcRecordingCh = -1;
    private int mCmcRegiPhoneId = -1;
    private Context mContext;
    private IImsFramework mImsFramework;
    /* access modifiers changed from: private */
    public StackIF mStackIf;
    /* access modifiers changed from: private */
    public boolean mSveConnected = false;
    /* access modifiers changed from: private */
    public boolean mSveConnecting = false;
    private SecVideoEngineManager mSveManager;

    public ResipCmcHandler(Looper looper, Context context, IImsFramework iImsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = iImsFramework;
        this.mSveManager = new SecVideoEngineManager(context, new SecVideoEngineManager.ConnectionListener() {
            public void onDisconnected() {
                Log.i(ResipCmcHandler.this.LOG_TAG, "sve disconnected");
                ResipCmcHandler.this.mSveConnected = false;
                ResipCmcHandler.this.mSveConnecting = false;
                if (ResipCmcHandler.this.needToReconnect()) {
                    ResipCmcHandler.this.sendEmptyMessageDelayed(300, 1000);
                }
            }

            public void onConnected() {
                Log.i(ResipCmcHandler.this.LOG_TAG, "sve connected.");
                ResipCmcHandler.this.mSveConnected = true;
                ResipCmcHandler.this.mSveConnecting = false;
                ResipCmcHandler.this.sendEmptyMessage(301);
            }
        });
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerCmcRecordEvent(this, 303, (Object) null);
        this.mSveConnected = false;
        this.mSveConnecting = false;
        this.mCmcRegiPhoneId = -1;
    }

    public void sendConnectToSve(int i) {
        this.mCmcRegiPhoneId = i;
        sendEmptyMessage(300);
    }

    public void sendDisonnectToSve() {
        if (!needToReconnect()) {
            sendEmptyMessage(302);
        }
    }

    /* access modifiers changed from: private */
    public boolean needToReconnect() {
        int i = this.mImsFramework.getCmcConnectivityController().isEnabledWifiDirectFeature() ? 7 : 5;
        for (int i2 = 1; i2 <= i; i2 += 2) {
            UserAgent uaByCmcType = getUaByCmcType(this.mCmcRegiPhoneId, i2);
            if (uaByCmcType != null && uaByCmcType.getImsRegistration() != null) {
                return true;
            }
        }
        return false;
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 300:
                connectToSve();
                return;
            case 301:
                onSveConnected();
                return;
            case 302:
                disconnectToSve();
                return;
            case 303:
                onCmcRecordEvent((AsyncResult) message.obj);
                return;
            default:
                return;
        }
    }

    private void connectToSve() {
        String str = this.LOG_TAG;
        Log.e(str, "SVE is not connected ? " + this.mSveConnected);
        if (!this.mSveConnected && !this.mSveConnecting) {
            Log.i(this.LOG_TAG, "connectToSve");
            this.mSveManager.connectService();
            this.mSveConnecting = true;
        }
    }

    private void disconnectToSve() {
        String str = this.LOG_TAG;
        Log.i(str, "SVE is connected ? " + this.mSveConnected);
        if (this.mSveConnected || this.mSveConnecting) {
            Log.i(this.LOG_TAG, "disconnectToSve");
            this.mSveManager.disconnectService();
            this.mSveConnecting = false;
            this.mSveConnected = false;
        }
    }

    private void onSveConnected() {
        if (this.mSveConnected) {
            registerCmcMediaEventListener();
            Log.i(this.LOG_TAG, "onSveConnected");
            return;
        }
        Log.e(this.LOG_TAG, "SVE was not connected!!!");
    }

    private void onCmcRecordEvent(AsyncResult asyncResult) {
        NotifyCmcRecordEventData notifyCmcRecordEventData = (NotifyCmcRecordEventData) asyncResult.result;
        int session = (int) notifyCmcRecordEventData.session();
        int event = (int) notifyCmcRecordEventData.event();
        String str = this.LOG_TAG;
        Log.i(str, "onCmcRecordEvent() session: " + session + ", event: " + event);
        IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
        iMSMediaEvent.setPhoneId((int) notifyCmcRecordEventData.phoneId());
        iMSMediaEvent.setSessionID(session);
        if (event == 50) {
            iMSMediaEvent.setCmcRecordingEvent(10);
        } else if (event == 60) {
            iMSMediaEvent.setCmcRecordingEvent(11);
        } else {
            iMSMediaEvent.setCmcRecordingEvent(event);
        }
        this.mCmcMediaEventRegistrants.notifyResult(iMSMediaEvent);
    }

    public void registerCmcMediaEventListener() {
        this.mSveManager.registerForCmcEventListener(this.mCmcMediaEventlistener);
    }

    public void unregisterCmcMediaEventListener() {
        this.mSveManager.unregisterForCmcEventListener(this.mCmcMediaEventlistener);
    }

    public boolean startCmcRecord(int i, int i2, int i3, int i4, long j, int i5, String str, int i6, int i7, int i8, int i9, int i10, long j2, String str2) {
        UserAgent uaByCmcType = getUaByCmcType(i, 1);
        if (uaByCmcType == null) {
            Log.e(this.LOG_TAG, "startCmcRecord: can't find UserAgent for cmc.");
            return false;
        }
        uaByCmcType.startCmcRecord(i2, i3, i4, j, i5, str, i6, i7, i8, i9, i10, j2, str2);
        return true;
    }

    public boolean stopCmcRecord(int i, int i2) {
        UserAgent uaByCmcType = getUaByCmcType(i, 1);
        if (uaByCmcType == null) {
            Log.e(this.LOG_TAG, "stopCmcRecord: can't find UserAgent for cmc.");
            return false;
        }
        uaByCmcType.stopRecord(i2);
        return true;
    }

    private UserAgent getUaByCmcType(int i, int i2) {
        IUserAgent[] userAgentByPhoneId = this.mImsFramework.getRegistrationManager().getUserAgentByPhoneId(i, "mmtel");
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

    public void sendRtpStatsToStack(IMSMediaEvent.AudioRtpStats audioRtpStats) {
        this.mStackIf.sendRtpStatsToStack(audioRtpStats);
    }

    public void sreInitialize() {
        this.mSveManager.sreInitialize();
    }

    public String sreGetVersion() {
        return this.mSveManager.sreGetVersion();
    }

    public int sreSetMdmn(int i, boolean z) {
        return this.mSveManager.sreSetMdmn(i, z);
    }

    public boolean sreGetMdmn(int i) {
        return this.mSveManager.sreGetMdmn(i);
    }

    public int sreSetNetId(int i, long j) {
        return this.mSveManager.sreSetNetId(i, j);
    }

    public int sreCreateStream(int i, int i2, int i3, String str, int i4, String str2, int i5, boolean z, boolean z2, int i6, int i7, String str3, boolean z3, boolean z4) {
        return this.mSveManager.sreCreateStream(i, i2, i3, str, i4, str2, i5, z, z2, i6, i7, str3, z3, z4);
    }

    public int sreStartStream(int i, int i2, int i3) {
        return this.mSveManager.sreStartStream(i, i2, i3);
    }

    public int sreDeleteStream(int i) {
        return this.mSveManager.sreDeleteStream(i);
    }

    public int sreUpdateStream(int i) {
        return this.mSveManager.sreUpdateStream(i);
    }

    public int sreCreateRelayChannel(int i, int i2) {
        return this.mSveManager.sreCreateRelayChannel(i, i2);
    }

    public int sreDeleteRelayChannel(int i) {
        return this.mSveManager.sreDeleteRelayChannel(i);
    }

    public int sreStartRelayChannel(int i, int i2) {
        return this.mSveManager.sreStartRelayChannel(i, i2);
    }

    public int sreStopRelayChannel(int i) {
        return this.mSveManager.sreStopRelayChannel(i);
    }

    public int sreHoldRelaySession(int i) {
        IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
        iMSMediaEvent.setRelayStreamEvent(10);
        iMSMediaEvent.setSessionID(i);
        this.mCmcMediaEventRegistrants.notifyResult(iMSMediaEvent);
        return 0;
    }

    public int sreResumeRelaySession(int i) {
        IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
        iMSMediaEvent.setRelayStreamEvent(11);
        iMSMediaEvent.setSessionID(i);
        this.mCmcMediaEventRegistrants.notifyResult(iMSMediaEvent);
        return 0;
    }

    public int sreHoldRelayChannel(int i) {
        return this.mSveManager.sreHoldRelayChannel(i);
    }

    public int sreResumeRelayChannel(int i) {
        return this.mSveManager.sreResumeRelayChannel(i);
    }

    public int sreUpdateRelayChannel(int i, int i2) {
        return this.mSveManager.sreUpdateRelayChannel(i, i2);
    }

    public int sreSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6) {
        return this.mSveManager.sreSetConnection(i, str, i2, str2, i3, i4, i5, i6);
    }

    public int sreEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) {
        return this.mSveManager.sreEnableSRTP(i, i2, i3, bArr, i4);
    }

    public int sreSetRtcpOnCall(int i, int i2, int i3, int i4, int i5) {
        return this.mSveManager.sreSetRtcpOnCall(i, i2, i3, i4, i5);
    }

    public int sreSetRtpTimeout(int i, int i2) {
        return this.mSveManager.sreSetRtpTimeout(i, i2);
    }

    public int sreSetRtcpTimeout(int i, int i2) {
        return this.mSveManager.sreSetRtcpTimeout(i, i2);
    }

    public int sreSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) {
        return this.mSveManager.sreSetRtcpXr(i, i2, i3, i4, i5, iArr);
    }

    public int sreSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, int i15) {
        short s = (short) i13;
        short s2 = (short) i14;
        return this.mSveManager.sreSetCodecInfo(i, str, i2, i3, i4, i5, i6, i7, z, i8, i9, i10, i11, i12, c, c2, c3, c4, c5, c6, s, s2, str2, str3, str4, str5, str6, str7, str8, str9, i15);
    }

    public int sreSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5, int i6) {
        return this.mSveManager.sreSetDtmfCodecInfo(i, i2, i3, i4, i5, i6);
    }

    public int sreStartRecording(int i) {
        IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
        iMSMediaEvent.setRelayStreamEvent(12);
        iMSMediaEvent.setSessionID(i);
        this.mCmcMediaEventRegistrants.notifyResult(iMSMediaEvent);
        return 0;
    }

    public int sreStartRecordingChannel(int i, int i2, int i3) {
        this.mCmcRecordingCh = i3;
        return this.mSveManager.sreStartRecording(i, i2, i3);
    }

    public int sreStopRecording(int i) {
        return this.mSveManager.sreStopRecording(i, this.mCmcRecordingCh);
    }
}
