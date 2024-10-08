package com.sec.internal.ims.servicemodules.volte2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.util.Log;
import android.util.SparseArray;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.volte2.data.RelayChannel;
import com.sec.internal.ims.servicemodules.volte2.data.RelayStreams;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class CmcMediaController implements ICmcMediaController {
    private static final int EVENT_CMC_MEDIA_EVENT = 11;
    private static final int EVENT_CMC_RECORDER_START = 2;
    private static final int EVENT_CMC_RECORDER_STOP = 3;
    private static final int EVENT_RETRY_CREATE_RELAY_CHANNEL = 12;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "CmcMediaController";
    private Handler mCmcMediaEventHandler = null;
    /* access modifiers changed from: private */
    public ICmcMediaServiceInterface mCmcMediaIntf = null;
    private final RemoteCallbackList<ISemCmcRecordingListener> mCmcRecordingCallbacks = new RemoteCallbackList<>();
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog;
    private int mExtStream = -1;
    protected ImsCallSessionManager mImsCallSessionManager;
    private int mIntStream = -1;
    /* access modifiers changed from: private */
    public boolean mPendingRelayChannelCreation = false;
    private RelayChannel mRelayChannel = null;
    private int mRelayDirection = 0;
    /* access modifiers changed from: private */
    public SparseArray<RelayStreams> mRelayStreamMap = new SparseArray<>();
    private IVolteServiceModuleInternal mVolteServiceModule = null;

    public CmcMediaController(IVolteServiceModuleInternal iVolteServiceModuleInternal, Looper looper, ImsCallSessionManager imsCallSessionManager, SimpleEventLog simpleEventLog) {
        this.mEventLog = simpleEventLog;
        this.mImsCallSessionManager = imsCallSessionManager;
        this.mVolteServiceModule = iVolteServiceModuleInternal;
        this.mCmcMediaIntf = ImsRegistry.getHandlerFactory().getCmcHandler();
        this.mCmcMediaEventHandler = new Handler(looper) {
            public void handleMessage(Message message) {
                super.handleMessage(message);
                AsyncResult asyncResult = (AsyncResult) message.obj;
                int i = message.what;
                if (i == 11) {
                    CmcMediaController.this.onImsRelayEvent((IMSMediaEvent) asyncResult.result);
                } else if (i == 12) {
                    int i2 = message.arg1;
                    int i3 = message.arg2;
                    String r1 = CmcMediaController.LOG_TAG;
                    Log.i(r1, "EVT_RETRY_CREATE_RELAY_CHANNEL extStreamId: " + i2 + " intStreamId: " + i3);
                    RelayStreams relayStreams = (RelayStreams) CmcMediaController.this.mRelayStreamMap.get(i2);
                    RelayStreams relayStreams2 = (RelayStreams) CmcMediaController.this.mRelayStreamMap.get(i3);
                    if (!(relayStreams == null || relayStreams2 == null)) {
                        ImsCallSession r3 = CmcMediaController.this.getSession(relayStreams.getSessionId());
                        ImsCallSession r4 = CmcMediaController.this.getSession(relayStreams2.getSessionId());
                        if (!(!CmcMediaController.this.mPendingRelayChannelCreation || r3 == null || r4 == null)) {
                            int sreCreateRelayChannel = CmcMediaController.this.mCmcMediaIntf.sreCreateRelayChannel(i2, i3);
                            if (sreCreateRelayChannel > -1) {
                                CallConstants.STATE callState = r3.getCallState();
                                CallConstants.STATE state = CallConstants.STATE.HeldCall;
                                int i4 = (callState == state || r4.getCallState() == state) ? 1 : 0;
                                IMSLog.c(LogClass.CMC_START_RELAY, sreCreateRelayChannel + "," + i4);
                                CmcMediaController.this.mCmcMediaIntf.sreStartRelayChannel(sreCreateRelayChannel, i4);
                                relayStreams.setRelayChannelId(sreCreateRelayChannel);
                                relayStreams2.setRelayChannelId(sreCreateRelayChannel);
                                SimpleEventLog r12 = CmcMediaController.this.mEventLog;
                                r12.add("Start Pending RelayChannel " + sreCreateRelayChannel + " with direction " + i4);
                            } else {
                                String r0 = CmcMediaController.LOG_TAG;
                                Log.i(r0, "failed to create relay channel relayChannelId: " + sreCreateRelayChannel);
                            }
                        }
                    }
                    CmcMediaController.this.resetCreateRelayChannelParams();
                }
            }
        };
        init();
    }

    public void connectToSve(int i) {
        this.mCmcMediaIntf.sendConnectToSve(i);
    }

    public void disconnectToSve() {
        this.mCmcMediaIntf.sendDisonnectToSve();
    }

    public void init() {
        this.mCmcMediaIntf.registerForCmcMediaEvent(this.mCmcMediaEventHandler, 11, (Object) null);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0103, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0161, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x018d, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void onRelayStreamEvent(com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent r6) {
        /*
            r5 = this;
            monitor-enter(r5)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x018e }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x018e }
            r1.<init>()     // Catch:{ all -> 0x018e }
            java.lang.String r2 = "onRelayStreamEvent : "
            r1.append(r2)     // Catch:{ all -> 0x018e }
            int r2 = r6.getRelayStreamEvent()     // Catch:{ all -> 0x018e }
            r1.append(r2)     // Catch:{ all -> 0x018e }
            java.lang.String r2 = " phoneId : "
            r1.append(r2)     // Catch:{ all -> 0x018e }
            int r2 = r6.getPhoneId()     // Catch:{ all -> 0x018e }
            r1.append(r2)     // Catch:{ all -> 0x018e }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x018e }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x018e }
            int r1 = r6.getRelayStreamEvent()     // Catch:{ all -> 0x018e }
            r2 = 0
            if (r1 == 0) goto L_0x0162
            r3 = 1
            if (r1 == r3) goto L_0x0116
            r3 = 3
            if (r1 == r3) goto L_0x010f
            r3 = 4
            if (r1 == r3) goto L_0x010f
            r3 = 5
            if (r1 == r3) goto L_0x0104
            switch(r1) {
                case 10: goto L_0x0057;
                case 11: goto L_0x0057;
                case 12: goto L_0x0057;
                default: goto L_0x003d;
            }     // Catch:{ all -> 0x018e }
        L_0x003d:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x018e }
            r1.<init>()     // Catch:{ all -> 0x018e }
            java.lang.String r2 = "not handled RelayStreamEvent : "
            r1.append(r2)     // Catch:{ all -> 0x018e }
            int r6 = r6.getRelayStreamEvent()     // Catch:{ all -> 0x018e }
            r1.append(r6)     // Catch:{ all -> 0x018e }
            java.lang.String r6 = r1.toString()     // Catch:{ all -> 0x018e }
            android.util.Log.e(r0, r6)     // Catch:{ all -> 0x018e }
            monitor-exit(r5)
            return
        L_0x0057:
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r0 = r5.mRelayStreamMap     // Catch:{ all -> 0x018e }
            int r0 = r0.size()     // Catch:{ all -> 0x018e }
            if (r2 >= r0) goto L_0x0102
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r0 = r5.mRelayStreamMap     // Catch:{ all -> 0x018e }
            java.lang.Object r0 = r0.valueAt(r2)     // Catch:{ all -> 0x018e }
            com.sec.internal.ims.servicemodules.volte2.data.RelayStreams r0 = (com.sec.internal.ims.servicemodules.volte2.data.RelayStreams) r0     // Catch:{ all -> 0x018e }
            int r1 = r0.getSessionId()     // Catch:{ all -> 0x018e }
            int r3 = r6.getSessionID()     // Catch:{ all -> 0x018e }
            if (r1 != r3) goto L_0x00fe
            int r1 = r6.getRelayStreamEvent()     // Catch:{ all -> 0x018e }
            r3 = 10
            if (r1 != r3) goto L_0x009d
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x018e }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x018e }
            r3.<init>()     // Catch:{ all -> 0x018e }
            java.lang.String r4 = "hold relay channel : "
            r3.append(r4)     // Catch:{ all -> 0x018e }
            int r4 = r0.getRelayChannelId()     // Catch:{ all -> 0x018e }
            r3.append(r4)     // Catch:{ all -> 0x018e }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x018e }
            android.util.Log.i(r1, r3)     // Catch:{ all -> 0x018e }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r1 = r5.mCmcMediaIntf     // Catch:{ all -> 0x018e }
            int r0 = r0.getRelayChannelId()     // Catch:{ all -> 0x018e }
            r1.sreHoldRelayChannel(r0)     // Catch:{ all -> 0x018e }
            goto L_0x00fe
        L_0x009d:
            int r1 = r6.getRelayStreamEvent()     // Catch:{ all -> 0x018e }
            r3 = 11
            if (r1 != r3) goto L_0x00ca
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x018e }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x018e }
            r3.<init>()     // Catch:{ all -> 0x018e }
            java.lang.String r4 = "resume relay channel : "
            r3.append(r4)     // Catch:{ all -> 0x018e }
            int r4 = r0.getRelayChannelId()     // Catch:{ all -> 0x018e }
            r3.append(r4)     // Catch:{ all -> 0x018e }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x018e }
            android.util.Log.i(r1, r3)     // Catch:{ all -> 0x018e }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r1 = r5.mCmcMediaIntf     // Catch:{ all -> 0x018e }
            int r0 = r0.getRelayChannelId()     // Catch:{ all -> 0x018e }
            r1.sreResumeRelayChannel(r0)     // Catch:{ all -> 0x018e }
            goto L_0x00fe
        L_0x00ca:
            int r1 = r6.getRelayStreamEvent()     // Catch:{ all -> 0x018e }
            r3 = 12
            if (r1 != r3) goto L_0x00fe
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x018e }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x018e }
            r3.<init>()     // Catch:{ all -> 0x018e }
            java.lang.String r4 = "start record relay channel : "
            r3.append(r4)     // Catch:{ all -> 0x018e }
            int r4 = r0.getRelayChannelId()     // Catch:{ all -> 0x018e }
            r3.append(r4)     // Catch:{ all -> 0x018e }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x018e }
            android.util.Log.i(r1, r3)     // Catch:{ all -> 0x018e }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r1 = r5.mCmcMediaIntf     // Catch:{ all -> 0x018e }
            int r3 = r6.getSessionID()     // Catch:{ all -> 0x018e }
            int r4 = r0.getStreamId()     // Catch:{ all -> 0x018e }
            int r0 = r0.getRelayChannelId()     // Catch:{ all -> 0x018e }
            r1.sreStartRecordingChannel(r3, r4, r0)     // Catch:{ all -> 0x018e }
        L_0x00fe:
            int r2 = r2 + 1
            goto L_0x0057
        L_0x0102:
            monitor-exit(r5)
            return
        L_0x0104:
            com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$AudioRtpStats r6 = r6.getRelayRtpStats()     // Catch:{ all -> 0x018e }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r0 = r5.mCmcMediaIntf     // Catch:{ all -> 0x018e }
            r0.sendRtpStatsToStack(r6)     // Catch:{ all -> 0x018e }
            monitor-exit(r5)
            return
        L_0x010f:
            java.lang.String r6 = "Ignore RTP/RTCP_TIMEOUT for CMC at PD"
            android.util.Log.i(r0, r6)     // Catch:{ all -> 0x018e }
            monitor-exit(r5)
            return
        L_0x0116:
            r5.resetCreateRelayChannelParams()     // Catch:{ all -> 0x018e }
            int r6 = r6.getStreamId()     // Catch:{ all -> 0x018e }
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r1 = r5.mRelayStreamMap     // Catch:{ all -> 0x018e }
            java.lang.Object r1 = r1.get(r6)     // Catch:{ all -> 0x018e }
            com.sec.internal.ims.servicemodules.volte2.data.RelayStreams r1 = (com.sec.internal.ims.servicemodules.volte2.data.RelayStreams) r1     // Catch:{ all -> 0x018e }
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r2 = r5.mRelayStreamMap     // Catch:{ all -> 0x018e }
            r2.delete(r6)     // Catch:{ all -> 0x018e }
            if (r1 == 0) goto L_0x0160
            int r6 = r1.getBoundStreamId()     // Catch:{ all -> 0x018e }
            r2 = -1
            if (r6 <= r2) goto L_0x0160
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r6 = r5.mRelayStreamMap     // Catch:{ all -> 0x018e }
            int r1 = r1.getBoundStreamId()     // Catch:{ all -> 0x018e }
            java.lang.Object r6 = r6.get(r1)     // Catch:{ all -> 0x018e }
            com.sec.internal.ims.servicemodules.volte2.data.RelayStreams r6 = (com.sec.internal.ims.servicemodules.volte2.data.RelayStreams) r6     // Catch:{ all -> 0x018e }
            if (r6 == 0) goto L_0x0160
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x018e }
            r1.<init>()     // Catch:{ all -> 0x018e }
            java.lang.String r3 = "reset bound stream "
            r1.append(r3)     // Catch:{ all -> 0x018e }
            int r3 = r6.getStreamId()     // Catch:{ all -> 0x018e }
            r1.append(r3)     // Catch:{ all -> 0x018e }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x018e }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x018e }
            r6.setRelayChannelId(r2)     // Catch:{ all -> 0x018e }
            r6.setBoundStreamId(r2)     // Catch:{ all -> 0x018e }
        L_0x0160:
            monitor-exit(r5)
            return
        L_0x0162:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSessionManager r0 = r5.mImsCallSessionManager     // Catch:{ all -> 0x018e }
            int r1 = r6.getSessionID()     // Catch:{ all -> 0x018e }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r0.getSession(r1)     // Catch:{ all -> 0x018e }
            if (r0 == 0) goto L_0x0172
            int r2 = r0.getCmcType()     // Catch:{ all -> 0x018e }
        L_0x0172:
            com.sec.internal.ims.servicemodules.volte2.data.RelayStreams r0 = new com.sec.internal.ims.servicemodules.volte2.data.RelayStreams     // Catch:{ all -> 0x018e }
            r0.<init>((com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent) r6, (int) r2)     // Catch:{ all -> 0x018e }
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r1 = r5.mRelayStreamMap     // Catch:{ all -> 0x018e }
            int r6 = r6.getStreamId()     // Catch:{ all -> 0x018e }
            r1.put(r6, r0)     // Catch:{ all -> 0x018e }
            android.util.SparseArray<com.sec.internal.ims.servicemodules.volte2.data.RelayStreams> r6 = r5.mRelayStreamMap     // Catch:{ all -> 0x018e }
            int r6 = r6.size()     // Catch:{ all -> 0x018e }
            r0 = 2
            if (r6 < r0) goto L_0x018c
            r5.handleRelayChannel()     // Catch:{ all -> 0x018e }
        L_0x018c:
            monitor-exit(r5)
            return
        L_0x018e:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.CmcMediaController.onRelayStreamEvent(com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent):void");
    }

    private void handleRelayChannel() {
        Log.i(LOG_TAG, "handleRelayChannel");
        int i = -1;
        int i2 = -1;
        for (int i3 = 0; i3 < this.mRelayStreamMap.size(); i3++) {
            RelayStreams valueAt = this.mRelayStreamMap.valueAt(i3);
            ImsCallSession session = getSession(valueAt.getSessionId());
            String str = LOG_TAG;
            Log.i(str, "Streamid : " + valueAt.getStreamId() + " SessionId : " + valueAt.getSessionId());
            if (session == null) {
                Log.e(str, "Session is null");
                this.mRelayStreamMap.delete(valueAt.getStreamId());
            } else {
                int cmcType = session.getCmcType();
                if (cmcType == 0 && session.getCallProfile().getCmcBoundSessionId() > -1 && valueAt.getRelayChannelId() == -1) {
                    if (i > -1) {
                        this.mRelayStreamMap.delete(i);
                    }
                    i = valueAt.getStreamId();
                } else if ((cmcType == 1 || cmcType == 3 || cmcType == 7 || cmcType == 5) && session.getCallProfile().getCmcBoundSessionId() > -1 && valueAt.getRelayChannelId() == -1) {
                    if (i2 > -1) {
                        this.mRelayStreamMap.delete(i2);
                    }
                    i2 = valueAt.getStreamId();
                }
            }
        }
        Log.i(LOG_TAG, "extStream: " + i + ", intStream" + i2);
        if (i != -1 && i2 != -1) {
            startRelayChannel(i, i2);
        }
    }

    private void startRelayChannel(int i, int i2) {
        RelayStreams relayStreams = this.mRelayStreamMap.get(i);
        RelayStreams relayStreams2 = this.mRelayStreamMap.get(i2);
        if (relayStreams != null && relayStreams2 != null && relayStreams.getRelayChannelId() == -1 && relayStreams2.getRelayChannelId() == -1) {
            ImsCallSession session = getSession(relayStreams.getSessionId());
            ImsCallSession session2 = getSession(relayStreams2.getSessionId());
            if (session == null || session2 == null) {
                Log.e(LOG_TAG, "extSession or intSession is null");
            } else if (session.getCallProfile().getCmcBoundSessionId() == relayStreams2.getSessionId() || session2.getCallProfile().getCmcBoundSessionId() == relayStreams.getSessionId()) {
                relayStreams.setBoundStreamId(i2);
                relayStreams2.setBoundStreamId(i);
                int sreCreateRelayChannel = this.mCmcMediaIntf.sreCreateRelayChannel(i, i2);
                CallConstants.STATE callState = session.getCallState();
                CallConstants.STATE state = CallConstants.STATE.HeldCall;
                int i3 = (callState == state || session2.getCallState() == state) ? 1 : 0;
                String str = LOG_TAG;
                Log.i(str, "Start Relay Channel " + sreCreateRelayChannel + " with direction " + i3);
                if (sreCreateRelayChannel > -1) {
                    IMSLog.c(LogClass.CMC_START_RELAY, sreCreateRelayChannel + "," + i3);
                    this.mCmcMediaIntf.sreStartRelayChannel(sreCreateRelayChannel, i3);
                    relayStreams.setRelayChannelId(sreCreateRelayChannel);
                    relayStreams2.setRelayChannelId(sreCreateRelayChannel);
                    resetCreateRelayChannelParams();
                    SimpleEventLog simpleEventLog = this.mEventLog;
                    simpleEventLog.add("Start RelayChannel " + sreCreateRelayChannel + " with direction " + i3);
                    return;
                }
                Handler handler = this.mCmcMediaEventHandler;
                handler.sendMessageDelayed(handler.obtainMessage(12, i, i2), 200);
                this.mPendingRelayChannelCreation = true;
                this.mExtStream = i;
                this.mIntStream = i2;
                this.mRelayDirection = i3;
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.add("Pending StartRelayChannel with " + i3);
            }
        }
    }

    /* access modifiers changed from: private */
    public ImsCallSession getSession(int i) {
        List<ImsCallSession> sessionList = this.mImsCallSessionManager.getSessionList();
        synchronized (sessionList) {
            for (ImsCallSession next : sessionList) {
                if (next != null && next.getSessionId() == i) {
                    return next;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void onImsRelayEvent(IMSMediaEvent iMSMediaEvent) {
        RelayChannel relayChannel;
        ImsCallSession session;
        int i;
        if (iMSMediaEvent.isRelayChannelEvent()) {
            String str = LOG_TAG;
            Log.i(str, "RelayEvent : " + iMSMediaEvent.getRelayChannelEvent());
            RelayStreams relayStreams = null;
            int i2 = -1;
            if (iMSMediaEvent.getRelayChannelEvent() == 0) {
                RelayStreams relayStreams2 = null;
                for (int i3 = 0; i3 < this.mRelayStreamMap.size(); i3++) {
                    RelayStreams valueAt = this.mRelayStreamMap.valueAt(i3);
                    if (relayStreams == null && ImsCallUtil.isCmcPrimaryType(valueAt.getCmcType())) {
                        relayStreams = valueAt;
                    } else if (relayStreams2 == null && valueAt.getCmcType() == 0) {
                        relayStreams2 = valueAt;
                    }
                }
                if (relayStreams != null && relayStreams2 != null) {
                    this.mRelayChannel = new RelayChannel(relayStreams, relayStreams2, relayStreams.getRelayChannelId());
                    ImsCallSession session2 = getSession(relayStreams2.getSessionId());
                    if (session2 != null) {
                        i2 = session2.getPhoneId();
                    }
                    this.mVolteServiceModule.notifyOnCmcRelayEvent(iMSMediaEvent.getRelayChannelEvent(), i2, relayStreams.getSessionId());
                }
            } else if (iMSMediaEvent.getRelayChannelEvent() == 1) {
                if (!(!this.mPendingRelayChannelCreation || (i = this.mExtStream) == -1 || this.mIntStream == -1)) {
                    RelayStreams relayStreams3 = this.mRelayStreamMap.get(i);
                    RelayStreams relayStreams4 = this.mRelayStreamMap.get(this.mIntStream);
                    if (!(relayStreams3 == null || relayStreams4 == null)) {
                        int sreCreateRelayChannel = this.mCmcMediaIntf.sreCreateRelayChannel(this.mExtStream, this.mIntStream);
                        Log.i(str, "Retry Start Relay Channel : " + sreCreateRelayChannel);
                        IMSLog.c(LogClass.CMC_START_RELAY, sreCreateRelayChannel + "," + this.mRelayDirection);
                        this.mCmcMediaIntf.sreStartRelayChannel(sreCreateRelayChannel, this.mRelayDirection);
                        relayStreams3.setRelayChannelId(sreCreateRelayChannel);
                        relayStreams4.setRelayChannelId(sreCreateRelayChannel);
                        this.mEventLog.add("Retry StartRelayChannel " + sreCreateRelayChannel + " with direction " + this.mRelayDirection);
                    }
                }
                resetCreateRelayChannelParams();
                if (!this.mPendingRelayChannelCreation && (relayChannel = this.mRelayChannel) != null) {
                    RelayStreams intStream = relayChannel.getIntStream();
                    RelayStreams extStream = this.mRelayChannel.getExtStream();
                    int sessionId = intStream != null ? intStream.getSessionId() : -1;
                    if (!(extStream == null || (session = getSession(extStream.getSessionId())) == null)) {
                        i2 = session.getPhoneId();
                    }
                    this.mVolteServiceModule.notifyOnCmcRelayEvent(iMSMediaEvent.getRelayChannelEvent(), i2, sessionId);
                }
                this.mRelayChannel = null;
            }
        } else {
            ImsCallSession session3 = getSession(iMSMediaEvent.getSessionID());
            if (session3 == null) {
                Log.i(LOG_TAG, "onImsRelayEvent: session " + iMSMediaEvent.getSessionID() + " not found.");
                return;
            }
            iMSMediaEvent.setSessionID(session3.getSessionId());
            iMSMediaEvent.setPhoneId(session3.getPhoneId());
            if (iMSMediaEvent.isRelayStreamEvent()) {
                onRelayStreamEvent(iMSMediaEvent);
            } else if (iMSMediaEvent.isCmcRecordingEvent()) {
                onCmcRecordingEvent(iMSMediaEvent);
            }
        }
    }

    private void onCmcRecordingEvent(IMSMediaEvent iMSMediaEvent) {
        int i;
        String str = LOG_TAG;
        Log.i(str, "onCmcRecordingEvent: event " + iMSMediaEvent.getCmcRecordingEvent());
        int cmcRecordingEvent = iMSMediaEvent.getCmcRecordingEvent();
        if (cmcRecordingEvent != 0) {
            if (cmcRecordingEvent != 5) {
                switch (cmcRecordingEvent) {
                    case 7:
                        i = 801;
                        break;
                    case 8:
                        i = 900;
                        break;
                    case 9:
                        i = Id.REQUEST_ALARM_WAKE_UP;
                        break;
                    case 10:
                        i = 701;
                        break;
                    case 11:
                        i = Id.REQUEST_PRESENCE_UNPUBLISH;
                        break;
                    default:
                        i = 1;
                        break;
                }
            } else {
                i = 800;
            }
            this.mVolteServiceModule.notifyOnCmcRecordingEvent(iMSMediaEvent.getPhoneId(), i, iMSMediaEvent.getCmcRecordingArg(), iMSMediaEvent.getSessionID());
        }
    }

    /* access modifiers changed from: private */
    public void resetCreateRelayChannelParams() {
        if (this.mCmcMediaEventHandler.hasMessages(12)) {
            this.mCmcMediaEventHandler.removeMessages(12);
        }
        this.mPendingRelayChannelCreation = false;
        this.mExtStream = -1;
        this.mIntStream = -1;
        this.mRelayDirection = 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00b6, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void sendCmcRecordingEvent(int r23, int r24, com.samsung.android.ims.cmc.SemCmcRecordingInfo r25) {
        /*
            r22 = this;
            r1 = r22
            r0 = r23
            r2 = r24
            monitor-enter(r22)
            r3 = 1
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r1.getActiveCallByCmcType(r0, r3)     // Catch:{ all -> 0x00b7 }
            if (r3 != 0) goto L_0x0018
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00b7 }
            java.lang.String r2 = "sendCmcRecordingEvent: PD active session is null"
            android.util.Log.e(r0, r2)     // Catch:{ all -> 0x00b7 }
            monitor-exit(r22)
            return
        L_0x0018:
            r4 = 2
            if (r2 == r4) goto L_0x0046
            r4 = 3
            if (r2 == r4) goto L_0x0037
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00b7 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b7 }
            r3.<init>()     // Catch:{ all -> 0x00b7 }
            java.lang.String r4 = "sendCmcRecordingEvent: ignore event = "
            r3.append(r4)     // Catch:{ all -> 0x00b7 }
            r3.append(r2)     // Catch:{ all -> 0x00b7 }
            java.lang.String r2 = r3.toString()     // Catch:{ all -> 0x00b7 }
            android.util.Log.e(r0, r2)     // Catch:{ all -> 0x00b7 }
            monitor-exit(r22)
            return
        L_0x0037:
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r2 = r1.mCmcMediaIntf     // Catch:{ all -> 0x00b7 }
            int r4 = r3.getPhoneId()     // Catch:{ all -> 0x00b7 }
            int r3 = r3.getSessionId()     // Catch:{ all -> 0x00b7 }
            boolean r2 = r2.stopCmcRecord(r4, r3)     // Catch:{ all -> 0x00b7 }
            goto L_0x009f
        L_0x0046:
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x00b7 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b7 }
            r4.<init>()     // Catch:{ all -> 0x00b7 }
            java.lang.String r5 = "sendCmcRecordingEvent: SemCmcRecordingInfo "
            r4.append(r5)     // Catch:{ all -> 0x00b7 }
            java.lang.String r5 = r25.toString()     // Catch:{ all -> 0x00b7 }
            r4.append(r5)     // Catch:{ all -> 0x00b7 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00b7 }
            android.util.Log.i(r2, r4)     // Catch:{ all -> 0x00b7 }
            com.sec.internal.interfaces.ims.core.handler.ICmcMediaServiceInterface r5 = r1.mCmcMediaIntf     // Catch:{ all -> 0x00b7 }
            int r6 = r3.getPhoneId()     // Catch:{ all -> 0x00b7 }
            int r7 = r3.getSessionId()     // Catch:{ all -> 0x00b7 }
            int r8 = r25.getAudioSource()     // Catch:{ all -> 0x00b7 }
            int r9 = r25.getOutputFormat()     // Catch:{ all -> 0x00b7 }
            long r10 = r25.getMaxFileSize()     // Catch:{ all -> 0x00b7 }
            int r12 = r25.getMaxDuration()     // Catch:{ all -> 0x00b7 }
            java.lang.String r13 = r25.getOutputPath()     // Catch:{ all -> 0x00b7 }
            int r14 = r25.getAudioEncodingBitRate()     // Catch:{ all -> 0x00b7 }
            int r15 = r25.getAudioChannels()     // Catch:{ all -> 0x00b7 }
            int r16 = r25.getAudioSamplingRate()     // Catch:{ all -> 0x00b7 }
            int r17 = r25.getAudioEncoder()     // Catch:{ all -> 0x00b7 }
            int r18 = r25.getDurationInterval()     // Catch:{ all -> 0x00b7 }
            long r19 = r25.getFileSizeInterval()     // Catch:{ all -> 0x00b7 }
            java.lang.String r21 = r25.getAuthor()     // Catch:{ all -> 0x00b7 }
            boolean r2 = r5.startCmcRecord(r6, r7, r8, r9, r10, r12, r13, r14, r15, r16, r17, r18, r19, r21)     // Catch:{ all -> 0x00b7 }
        L_0x009f:
            if (r2 != 0) goto L_0x00b5
            com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent r2 = new com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent     // Catch:{ all -> 0x00b7 }
            r2.<init>()     // Catch:{ all -> 0x00b7 }
            r3 = 4
            r2.setCmcRecordingEvent(r3)     // Catch:{ all -> 0x00b7 }
            com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE     // Catch:{ all -> 0x00b7 }
            r2.setState(r3)     // Catch:{ all -> 0x00b7 }
            r2.setPhoneId(r0)     // Catch:{ all -> 0x00b7 }
            r1.onCmcRecordingEvent(r2)     // Catch:{ all -> 0x00b7 }
        L_0x00b5:
            monitor-exit(r22)
            return
        L_0x00b7:
            r0 = move-exception
            monitor-exit(r22)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.CmcMediaController.sendCmcRecordingEvent(int, int, com.samsung.android.ims.cmc.SemCmcRecordingInfo):void");
    }

    private ImsCallSession getActiveCallByCmcType(int i, int i2) {
        List<ImsCallSession> sessionList = this.mImsCallSessionManager.getSessionList();
        synchronized (sessionList) {
            for (ImsCallSession next : sessionList) {
                if (next != null && next.getCallState() == CallConstants.STATE.InCall && next.getCmcType() == i2 && next.getPhoneId() == i) {
                    return next;
                }
            }
            return null;
        }
    }
}
