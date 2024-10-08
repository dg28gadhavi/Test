package com.sec.internal.ims.servicemodules.volte2;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.sec.ims.DialogEvent;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IRttEventListener;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.ims.volte2.IVolteServiceEventListener;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti;
import com.sec.internal.ims.servicemodules.volte2.data.DedicatedBearerEvent;
import com.sec.internal.ims.servicemodules.volte2.data.TextInfo;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VolteNotifier {
    public static final String LOG_TAG = "VolteNotifier";
    private final Map<Integer, RemoteCallbackList<IImsCallEventListener>> mCallStateListeners = new ConcurrentHashMap();
    private final Map<Integer, RemoteCallbackList<ISemCmcRecordingListener>> mCmcRecordingListeners = new ConcurrentHashMap();
    private final Map<Integer, RemoteCallbackList<IDialogEventListener>> mDialogEventListeners = new ConcurrentHashMap();
    private final Map<Integer, RemoteCallbackList<IVolteServiceEventListener>> mListeners = new ConcurrentHashMap();
    private final Map<Integer, RemoteCallbackList<IRttEventListener>> mRttEventListeners = new ConcurrentHashMap();

    public void registerForVolteServiceEvent(int i, IVolteServiceEventListener iVolteServiceEventListener) {
        String str = LOG_TAG;
        Log.i(str, "registerForVolteServiceEvent to phone#" + i);
        if (!this.mListeners.containsKey(Integer.valueOf(i))) {
            this.mListeners.put(Integer.valueOf(i), new RemoteCallbackList());
        }
        this.mListeners.get(Integer.valueOf(i)).register(iVolteServiceEventListener);
    }

    public void deRegisterForVolteServiceEvent(int i, IVolteServiceEventListener iVolteServiceEventListener) {
        String str = LOG_TAG;
        Log.i(str, "deRegisterForVolteServiceEvent to phone#" + i);
        if (this.mListeners.containsKey(Integer.valueOf(i))) {
            this.mListeners.get(Integer.valueOf(i)).unregister(iVolteServiceEventListener);
        }
    }

    public void registerRttEventListener(int i, IRttEventListener iRttEventListener) {
        String str = LOG_TAG;
        Log.i(str, "registerRttEventListener to phone#" + i);
        if (!this.mRttEventListeners.containsKey(Integer.valueOf(i))) {
            this.mRttEventListeners.put(Integer.valueOf(i), new RemoteCallbackList());
        }
        this.mRttEventListeners.get(Integer.valueOf(i)).register(iRttEventListener);
    }

    public void unregisterRttEventListener(int i, IRttEventListener iRttEventListener) {
        String str = LOG_TAG;
        Log.i(str, "unregisterRttEventListener to phone#" + i);
        if (this.mRttEventListeners.containsKey(Integer.valueOf(i))) {
            this.mRttEventListeners.get(Integer.valueOf(i)).unregister(iRttEventListener);
        }
    }

    public void registerCmcRecordingListener(int i, ISemCmcRecordingListener iSemCmcRecordingListener) {
        String str = LOG_TAG;
        Log.i(str, "registerCmcRecordingListener to phone#" + i);
        if (!this.mCmcRecordingListeners.containsKey(Integer.valueOf(i))) {
            this.mCmcRecordingListeners.put(Integer.valueOf(i), new RemoteCallbackList());
        }
        this.mCmcRecordingListeners.get(Integer.valueOf(i)).register(iSemCmcRecordingListener);
    }

    public void unregisterCmcRecordingListener(int i, ISemCmcRecordingListener iSemCmcRecordingListener) {
        String str = LOG_TAG;
        Log.i(str, "unregisterCmcRecordingListener to phone#" + i);
        if (this.mCmcRecordingListeners.containsKey(Integer.valueOf(i))) {
            this.mCmcRecordingListeners.get(Integer.valueOf(i)).unregister(iSemCmcRecordingListener);
        }
    }

    public void registerDialogEventListener(int i, IDialogEventListener iDialogEventListener) {
        String str = LOG_TAG;
        Log.i(str, "registerDialogEventListener to phone#" + i);
        if (!this.mDialogEventListeners.containsKey(Integer.valueOf(i))) {
            this.mDialogEventListeners.put(Integer.valueOf(i), new RemoteCallbackList());
        }
        this.mDialogEventListeners.get(Integer.valueOf(i)).register(iDialogEventListener);
    }

    public void unregisterDialogEventListener(int i, IDialogEventListener iDialogEventListener) {
        String str = LOG_TAG;
        Log.i(str, "unregisterDialogEventListener to phone#" + i);
        if (this.mDialogEventListeners.containsKey(Integer.valueOf(i))) {
            this.mDialogEventListeners.get(Integer.valueOf(i)).unregister(iDialogEventListener);
        }
    }

    public void registerForCallStateEvent(int i, IImsCallEventListener iImsCallEventListener) {
        String str = LOG_TAG;
        Log.i(str, "registerForCallStateEvent to phone#" + i);
        if (!this.mCallStateListeners.containsKey(Integer.valueOf(i))) {
            this.mCallStateListeners.put(Integer.valueOf(i), new RemoteCallbackList());
        }
        this.mCallStateListeners.get(Integer.valueOf(i)).register(iImsCallEventListener);
    }

    public void deregisterForCallStateEvent(int i, IImsCallEventListener iImsCallEventListener) {
        String str = LOG_TAG;
        Log.i(str, "deregisterForCallStateEvent to phone#" + i);
        if (this.mCallStateListeners.containsKey(Integer.valueOf(i))) {
            this.mCallStateListeners.get(Integer.valueOf(i)).unregister(iImsCallEventListener);
        }
    }

    public synchronized void notifyOnPulling(int i, int i2) {
        if (this.mListeners.containsKey(Integer.valueOf(i))) {
            RemoteCallbackList remoteCallbackList = this.mListeners.get(Integer.valueOf(i));
            int beginBroadcast = remoteCallbackList.beginBroadcast();
            for (int i3 = 0; i3 < beginBroadcast; i3++) {
                try {
                    remoteCallbackList.getBroadcastItem(i3).onPullingCall(i2);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onPullingCall event!", e);
                }
            }
            remoteCallbackList.finishBroadcast();
        }
    }

    public synchronized void notifyOnIncomingCall(int i, int i2) {
        if (this.mListeners.containsKey(Integer.valueOf(i))) {
            RemoteCallbackList remoteCallbackList = this.mListeners.get(Integer.valueOf(i));
            int beginBroadcast = remoteCallbackList.beginBroadcast();
            Log.i(LOG_TAG + '/' + i, "onIncomingCall: listeners length = " + beginBroadcast);
            for (int i3 = 0; i3 < beginBroadcast; i3++) {
                try {
                    remoteCallbackList.getBroadcastItem(i3).onIncomingCall(i2);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify incoming call event!", e);
                }
            }
            remoteCallbackList.finishBroadcast();
        }
    }

    private ImsCallInfo makeImsCallInfo(ImsCallSession imsCallSession) {
        ImsCallInfo imsCallInfo = new ImsCallInfo(imsCallSession.getCallId(), imsCallSession.getCallProfile().getCallType(), imsCallSession.getCallProfile().isDowngradedVideoCall(), imsCallSession.getCallProfile().isDowngradedAtEstablish(), imsCallSession.getDedicatedBearerState(1), imsCallSession.getDedicatedBearerState(2), imsCallSession.getDedicatedBearerState(8), imsCallSession.getErrorCode(), imsCallSession.getErrorMessage(), imsCallSession.getCallProfile().getDialingNumber(), imsCallSession.getCallProfile().getDirection(), imsCallSession.getCallProfile().isConferenceCall());
        imsCallInfo.setRatInfo(imsCallSession.getCallProfile().getRadioTech());
        return imsCallInfo;
    }

    public synchronized void notifyIncomingPreAlerting(ImsCallSession imsCallSession) {
        int phoneId = imsCallSession.getPhoneId();
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            int beginBroadcast = this.mCallStateListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            ImsCallInfo makeImsCallInfo = makeImsCallInfo(imsCallSession);
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.mCallStateListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onIncomingPreAlerting(makeImsCallInfo, imsCallSession.getCallProfile().getDialingNumber());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public synchronized void notifyIncomingCallEvent(ImsCallSession imsCallSession) {
        int phoneId = imsCallSession.getPhoneId();
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            int beginBroadcast = this.mCallStateListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            ImsCallInfo makeImsCallInfo = makeImsCallInfo(imsCallSession);
            makeImsCallInfo.setSamsungMdmnCall(imsCallSession.getCallProfile().isSamsungMdmnCall());
            makeImsCallInfo.setNumberPlus(imsCallSession.getCallProfile().getNumberPlus());
            makeImsCallInfo.setCmcDeviceId(imsCallSession.getCallProfile().getCmcDeviceId());
            makeImsCallInfo.setRatInfo(imsCallSession.getCallProfile().getRadioTech());
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.mCallStateListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onIncomingCall(makeImsCallInfo, imsCallSession.getCallProfile().getDialingNumber());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public synchronized void notifyCallStateEvent(CallStateEvent callStateEvent, ImsCallSession imsCallSession) {
        int phoneId = imsCallSession.getPhoneId();
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            int beginBroadcast = this.mCallStateListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            ImsCallInfo makeImsCallInfo = makeImsCallInfo(imsCallSession);
            makeImsCallInfo.setSamsungMdmnCall(imsCallSession.getCallProfile().isSamsungMdmnCall());
            makeImsCallInfo.setCmcDeviceId(imsCallSession.getCallProfile().getCmcDeviceId());
            makeImsCallInfo.setRatInfo(imsCallSession.getCallProfile().getRadioTech());
            for (int i = 0; i < beginBroadcast; i++) {
                IImsCallEventListener broadcastItem = this.mCallStateListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i);
                try {
                    switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[callStateEvent.getState().ordinal()]) {
                        case 1:
                            broadcastItem.onCallStarted(makeImsCallInfo);
                            break;
                        case 2:
                            broadcastItem.onCallEstablished(makeImsCallInfo);
                            break;
                        case 3:
                            broadcastItem.onCallModifyRequested(makeImsCallInfo, imsCallSession.getCallProfile().getCallType());
                            break;
                        case 4:
                            broadcastItem.onCallModified(makeImsCallInfo);
                            break;
                        case 5:
                            if (callStateEvent.getUpdatedParticipantsList().size() <= 0) {
                                break;
                            } else {
                                broadcastItem.onConferenceParticipantAdded(makeImsCallInfo, callStateEvent.getUpdatedParticipantsList().get(0).getUri());
                                break;
                            }
                        case 6:
                            if (callStateEvent.getUpdatedParticipantsList().size() <= 0) {
                                break;
                            } else {
                                broadcastItem.onConferenceParticipantRemoved(makeImsCallInfo, callStateEvent.getUpdatedParticipantsList().get(0).getUri());
                                break;
                            }
                        case 7:
                            broadcastItem.onCallHeld(makeImsCallInfo, true, false);
                            break;
                        case 8:
                            broadcastItem.onCallHeld(makeImsCallInfo, false, true);
                            break;
                        case 9:
                            broadcastItem.onCallHeld(makeImsCallInfo, true, true);
                            break;
                        case 10:
                        case 11:
                            broadcastItem.onCallEnded(makeImsCallInfo, imsCallSession.getErrorCode());
                            break;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public synchronized void onDedicatedBearerEvent(ImsCallSession imsCallSession, DedicatedBearerEvent dedicatedBearerEvent) {
        int phoneId = imsCallSession.getPhoneId();
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            int beginBroadcast = this.mCallStateListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            ImsCallInfo makeImsCallInfo = makeImsCallInfo(imsCallSession);
            makeImsCallInfo.setRatInfo(imsCallSession.getCallProfile().getRadioTech());
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.mCallStateListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onDedicatedBearerEvent(makeImsCallInfo, dedicatedBearerEvent.getBearerState(), dedicatedBearerEvent.getQci());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public synchronized void notifyOnRtpLossRate(int i, RtpLossRateNoti rtpLossRateNoti) {
        String str = LOG_TAG;
        Log.i(str, "onRtpLossRateNoti: interval" + rtpLossRateNoti.getInterval() + " : LossRate" + rtpLossRateNoti.getLossRate() + " : Jitter " + rtpLossRateNoti.getJitter() + " : Notification" + rtpLossRateNoti.getNotification());
        if (this.mCallStateListeners.containsKey(Integer.valueOf(i))) {
            synchronized (this.mCallStateListeners.get(Integer.valueOf(i))) {
                int beginBroadcast = this.mCallStateListeners.get(Integer.valueOf(i)).beginBroadcast();
                for (int i2 = 0; i2 < beginBroadcast; i2++) {
                    try {
                        this.mCallStateListeners.get(Integer.valueOf(i)).getBroadcastItem(i2).onRtpLossRateNoti(rtpLossRateNoti.getInterval(), rtpLossRateNoti.getLossRate(), rtpLossRateNoti.getJitter(), rtpLossRateNoti.getNotification());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                this.mCallStateListeners.get(Integer.valueOf(i)).finishBroadcast();
            }
        }
    }

    public synchronized void notifyOnCmcRecordingEvent(int i, int i2, int i3) {
        if (this.mCmcRecordingListeners.containsKey(Integer.valueOf(i))) {
            int beginBroadcast = this.mCmcRecordingListeners.get(Integer.valueOf(i)).beginBroadcast();
            for (int i4 = 0; i4 < beginBroadcast; i4++) {
                ISemCmcRecordingListener broadcastItem = this.mCmcRecordingListeners.get(Integer.valueOf(i)).getBroadcastItem(i4);
                if (i2 >= 800) {
                    try {
                        broadcastItem.onInfo(i2, i3);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "failed notify cmc recording event!", e);
                    }
                } else if (i2 > 0 && i2 < 700) {
                    broadcastItem.onError(i2, i3);
                }
            }
            this.mCmcRecordingListeners.get(Integer.valueOf(i)).finishBroadcast();
        }
    }

    public synchronized void notifyOnDialogEvent(DialogEvent dialogEvent) {
        if (this.mDialogEventListeners.containsKey(Integer.valueOf(dialogEvent.getPhoneId()))) {
            int beginBroadcast = this.mDialogEventListeners.get(Integer.valueOf(dialogEvent.getPhoneId())).beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.mDialogEventListeners.get(Integer.valueOf(dialogEvent.getPhoneId())).getBroadcastItem(i).onDialogEvent(dialogEvent);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify dialog event!", e);
                }
            }
            this.mDialogEventListeners.get(Integer.valueOf(dialogEvent.getPhoneId())).finishBroadcast();
        }
    }

    public void onSendRttSessionModifyRequest(int i, ImsCallSession imsCallSession, boolean z) {
        if (imsCallSession != null && this.mRttEventListeners.containsKey(Integer.valueOf(i))) {
            int beginBroadcast = this.mRttEventListeners.get(Integer.valueOf(i)).beginBroadcast();
            IMSLog.c(LogClass.VOLTE_RECV_REQUEST_RTT, i + "," + imsCallSession.getSessionId() + "," + z);
            String str = LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("onSendRttSessionModifyRequest : mode = ");
            sb.append(z);
            Log.i(str, sb.toString());
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                try {
                    this.mRttEventListeners.get(Integer.valueOf(i)).getBroadcastItem(i2).onSendRttSessionModifyRequest(imsCallSession.getCallId(), z);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onSendRttSessionModifyRequest!", e);
                }
            }
            this.mRttEventListeners.get(Integer.valueOf(i)).finishBroadcast();
        }
    }

    public void onSendRttSessionModifyResponse(int i, ImsCallSession imsCallSession, boolean z, boolean z2) {
        if (imsCallSession != null && this.mRttEventListeners.containsKey(Integer.valueOf(i))) {
            int beginBroadcast = this.mRttEventListeners.get(Integer.valueOf(i)).beginBroadcast();
            IMSLog.c(LogClass.VOLTE_RECV_RESPONSE_RTT, i + "," + imsCallSession.getSessionId() + "," + z + "," + z2);
            String str = LOG_TAG;
            Log.i(str, "onSendRttSessionModifyResponse : mode = " + z + ", result = " + z2 + ", Listeners length : " + beginBroadcast);
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                try {
                    IRttEventListener broadcastItem = this.mRttEventListeners.get(Integer.valueOf(i)).getBroadcastItem(i2);
                    if (broadcastItem != null) {
                        broadcastItem.onSendRttSessionModifyResponse(imsCallSession.getCallId(), z, z2);
                    }
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onSendRttSessionModifyResponse!", e);
                }
            }
            this.mRttEventListeners.get(Integer.valueOf(i)).finishBroadcast();
        }
    }

    public void notifyOnRttEventBySession(int i, TextInfo textInfo) {
        if (this.mRttEventListeners.containsKey(Integer.valueOf(i))) {
            int beginBroadcast = this.mRttEventListeners.get(Integer.valueOf(i)).beginBroadcast();
            String str = LOG_TAG;
            Log.i(str, "notifyOnRttEventBySession : getText = " + textInfo.getText() + " , len : " + textInfo.getTextLen() + ", SessionId : " + textInfo.getSessionId() + ", Listeners length : " + beginBroadcast);
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                try {
                    this.mRttEventListeners.get(Integer.valueOf(i)).getBroadcastItem(i2).onRttEventBySession(textInfo.getSessionId(), textInfo.getText());
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onTextReceived event!", e);
                }
            }
            this.mRttEventListeners.get(Integer.valueOf(i)).finishBroadcast();
        }
    }

    public void notifyOnRttEvent(int i, TextInfo textInfo) {
        if (this.mRttEventListeners.containsKey(Integer.valueOf(i))) {
            int beginBroadcast = this.mRttEventListeners.get(Integer.valueOf(i)).beginBroadcast();
            String str = LOG_TAG;
            Log.i(str, "notifyOnRttEvent : getText = " + textInfo.getText() + " , len : " + textInfo.getTextLen() + ", Listeners length : " + beginBroadcast);
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                try {
                    this.mRttEventListeners.get(Integer.valueOf(i)).getBroadcastItem(i2).onRttEvent(textInfo.getText());
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify onTextInfo event!", e);
                }
            }
            this.mRttEventListeners.get(Integer.valueOf(i)).finishBroadcast();
        }
    }

    public void notifyImsCallEventForVideo(ImsCallSession imsCallSession, IMSMediaEvent iMSMediaEvent) {
        if (imsCallSession == null) {
            Log.e(LOG_TAG, "notifyImsCallEventForVideo: unknown session");
            return;
        }
        int phoneId = imsCallSession.getPhoneId();
        if (this.mCallStateListeners.containsKey(Integer.valueOf(phoneId))) {
            String str = LOG_TAG;
            Log.d(str, "notifyImsCallEventForVideo: session[" + iMSMediaEvent.getSessionID() + "]");
            ImsCallInfo makeImsCallInfo = makeImsCallInfo(imsCallSession);
            makeImsCallInfo.setRatInfo(imsCallSession.getCallProfile().getRadioTech());
            int beginBroadcast = this.mCallStateListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                IImsCallEventListener broadcastItem = this.mCallStateListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i);
                try {
                    int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[iMSMediaEvent.getState().ordinal()];
                    if (i2 != 1) {
                        if (i2 != 2) {
                            if (i2 == 3) {
                                broadcastItem.onVideoAvailable(makeImsCallInfo);
                            }
                        } else if (!iMSMediaEvent.isHeldCall()) {
                            broadcastItem.onVideoResumed(makeImsCallInfo);
                        }
                    } else if (!iMSMediaEvent.isHeldCall()) {
                        broadcastItem.onVideoHeld(makeImsCallInfo);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallStateListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.VolteNotifier$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE;

        /* JADX WARNING: Can't wrap try/catch for region: R(31:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Can't wrap try/catch for region: R(32:0|(2:1|2)|3|5|6|7|9|10|11|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Can't wrap try/catch for region: R(33:0|1|2|3|5|6|7|9|10|11|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0039 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0043 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x004d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0058 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0063 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x006e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x0079 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x0085 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0091 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x009d */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE[] r0 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE = r0
                r1 = 1
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_HELD     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_RESUMED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r4 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE[] r3 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.values()
                int r3 = r3.length
                int[] r3 = new int[r3]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE = r3
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r4 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CALLING     // Catch:{ NoSuchFieldError -> 0x0039 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0039 }
                r3[r4] = r1     // Catch:{ NoSuchFieldError -> 0x0039 }
            L_0x0039:
                int[] r1 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0043 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ESTABLISHED     // Catch:{ NoSuchFieldError -> 0x0043 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0043 }
                r1[r3] = r0     // Catch:{ NoSuchFieldError -> 0x0043 }
            L_0x0043:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x004d }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.MODIFY_REQUESTED     // Catch:{ NoSuchFieldError -> 0x004d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x004d }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x004d }
            L_0x004d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0058 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.MODIFIED     // Catch:{ NoSuchFieldError -> 0x0058 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0058 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0058 }
            L_0x0058:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0063 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_ADDED     // Catch:{ NoSuchFieldError -> 0x0063 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0063 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0063 }
            L_0x0063:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x006e }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_REMOVED     // Catch:{ NoSuchFieldError -> 0x006e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006e }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006e }
            L_0x006e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0079 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_LOCAL     // Catch:{ NoSuchFieldError -> 0x0079 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0079 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0079 }
            L_0x0079:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0085 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_REMOTE     // Catch:{ NoSuchFieldError -> 0x0085 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0085 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0085 }
            L_0x0085:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0091 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_BOTH     // Catch:{ NoSuchFieldError -> 0x0091 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0091 }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0091 }
            L_0x0091:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x009d }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ENDED     // Catch:{ NoSuchFieldError -> 0x009d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009d }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009d }
            L_0x009d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x00a9 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ERROR     // Catch:{ NoSuchFieldError -> 0x00a9 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a9 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a9 }
            L_0x00a9:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteNotifier.AnonymousClass1.<clinit>():void");
        }
    }
}
