package com.sec.internal.ims.servicemodules.csh;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.gsma.services.rcs.sharing.video.VideoSharing;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.AtomicGenerator;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.ims.servicemodules.csh.event.CshAcceptSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshCancelSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshErrorReason;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.CshRejectSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshSessionResult;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.ICshSuccessCallback;
import com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.VshOrientation;
import com.sec.internal.ims.servicemodules.csh.event.VshResolution;
import com.sec.internal.ims.servicemodules.csh.event.VshStartSessionParams;

public class VideoShare extends StateMachine implements IContentShare {
    private static final int DEFAULT_WARNNING_TIME_GAP = 30;
    private static final int EVENT_ACCEPT_INCOMING_SESSION = 5;
    private static final int EVENT_ACCEPT_SESSION_DONE = 6;
    private static final int EVENT_CANCEL_BY_USER_SESSION = 7;
    private static final int EVENT_INCOMING_SESSION_DONE = 4;
    private static final int EVENT_MAX_DURATION_TIME = 10;
    private static final int EVENT_SESSION_ESTABLISHED = 8;
    private static final int EVENT_SESSION_FAILED = 11;
    private static final int EVENT_SESSION_TERMINATED_BY_STACK = 9;
    private static final int EVENT_SET_PHONE_ORIENTATION = 3;
    private static final int EVENT_START_OUTGOING_SESSION = 1;
    private static final int EVENT_START_SESSION_DONE = 2;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "VideoShare";
    /* access modifiers changed from: private */
    public final CshInfo mContent;
    private State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public IvshServiceInterface mImsService;
    /* access modifiers changed from: private */
    public State mInProgressApproachMaxTimeState = new InProgressApproachMaxTimeState();
    /* access modifiers changed from: private */
    public State mInProgressState = new InProgressState();
    /* access modifiers changed from: private */
    public State mIncomingPendingState = new IncomingPendingState();
    private State mInitialState = new InitialState();
    private PendingIntent mMaxDurationIntent = null;
    /* access modifiers changed from: private */
    public State mOutgoingPendingState = new OutgoingPendingState();
    /* access modifiers changed from: private */
    public State mPreTerminatedLocalState = new PreTerminatedLocalState();
    /* access modifiers changed from: private */
    public State mPreTerminatedRemoteState = new PreTerminatedRemoteState();
    /* access modifiers changed from: private */
    public int mSessionId;
    /* access modifiers changed from: private */
    public State mTerminatedState = new TerminatedState();
    /* access modifiers changed from: private */
    public VideoShareModule mVshModule;
    private int mWarningTime;

    public VideoShare(IvshServiceInterface ivshServiceInterface, VideoShareModule videoShareModule, CshInfo cshInfo) {
        super("Vsh Session " + cshInfo.dataPath, (Handler) videoShareModule);
        this.mVshModule = videoShareModule;
        cshInfo.shareId = AtomicGenerator.generateUniqueLong();
        this.mContent = cshInfo;
        this.mImsService = ivshServiceInterface;
        init();
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public void setSessionId(int i) {
        this.mSessionId = i;
    }

    public CshInfo getContent() {
        return this.mContent;
    }

    public void startQutgoingSession() {
        sendMessage(obtainMessage(1, 0));
    }

    public void incomingSessionDone() {
        sendMessage(obtainMessage(4, 0));
    }

    public void sessioinEstablished(VshResolution vshResolution, PendingIntent pendingIntent) {
        this.mContent.videoWidth = ResolutionTranslator.getWidth(vshResolution);
        this.mContent.videoHeight = ResolutionTranslator.getHeight(vshResolution);
        this.mMaxDurationIntent = pendingIntent;
        sendMessage(obtainMessage(8, (Object) vshResolution));
    }

    public void setPhoneOrientation(VshOrientation vshOrientation) {
        sendMessage(obtainMessage(3, (Object) vshOrientation));
    }

    public void sessionTerminatedByStack() {
        sendMessage(obtainMessage(9, 0));
    }

    public void acceptIncomingSession() {
        sendMessage(obtainMessage(5, 0));
    }

    public void acceptIncomingSession(int i) {
        sendMessage(obtainMessage(5, i));
    }

    public void cancelByUserSession() {
        sendMessage(obtainMessage(7, 0));
    }

    public void maxDurationTime() {
        sendMessage(obtainMessage(10, 0));
    }

    public void sessionFailed() {
        sendMessage(obtainMessage(11, 0));
    }

    /* access modifiers changed from: private */
    public void startDurationTimer() {
        int maxDurationTime = this.mVshModule.getMaxDurationTime();
        if (maxDurationTime > 30) {
            this.mWarningTime = maxDurationTime - 30;
        } else {
            this.mWarningTime = 0;
        }
        String str = LOG_TAG;
        Log.i(str, "Start VS MAX DURATION Timer Warning Time : " + this.mWarningTime + "s");
        if (this.mWarningTime > 0 && this.mMaxDurationIntent != null) {
            ((AlarmManager) this.mVshModule.getContext().getSystemService("alarm")).setRepeating(2, SystemClock.elapsedRealtime() + (((long) this.mWarningTime) * 1000), 30000, this.mMaxDurationIntent);
        }
    }

    /* access modifiers changed from: private */
    public void stopDurationTimer() {
        if (this.mMaxDurationIntent != null) {
            String str = LOG_TAG;
            Log.d(str, "Stop VS MAX DURATION Timer #" + this.mSessionId);
            ((AlarmManager) this.mVshModule.getContext().getSystemService("alarm")).cancel(this.mMaxDurationIntent);
            this.mMaxDurationIntent = null;
        }
    }

    private void init() {
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mOutgoingPendingState, this.mDefaultState);
        addState(this.mIncomingPendingState, this.mDefaultState);
        addState(this.mInProgressState, this.mDefaultState);
        addState(this.mInProgressApproachMaxTimeState, this.mDefaultState);
        addState(this.mTerminatedState, this.mDefaultState);
        addState(this.mPreTerminatedRemoteState, this.mDefaultState);
        addState(this.mPreTerminatedLocalState, this.mDefaultState);
        setInitialState(this.mInitialState);
        start();
    }

    private static class DefaultState extends State {
        private DefaultState() {
        }
    }

    private class InitialState extends State {
        private InitialState() {
        }

        public void enter() {
            VideoShare.this.mContent.shareState = 1;
        }

        public boolean processMessage(Message message) {
            String r0 = VideoShare.LOG_TAG;
            Log.i(r0, "InitialState Event: " + message.what);
            int i = message.what;
            if (i == 1) {
                VideoShare.this.mImsService.startVshSession(new VshStartSessionParams(VideoShare.this.mContent.shareContactUri.toString(), VideoShare.this.obtainMessage(2)));
                return true;
            } else if (i == 2) {
                CshSessionResult cshSessionResult = (CshSessionResult) ((AsyncResult) message.obj).result;
                VideoShare.this.mSessionId = cshSessionResult.mSessionNumber;
                if (VideoShare.this.mSessionId < 0 || cshSessionResult.mReason != CshErrorReason.SUCCESS) {
                    VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.FAILED_INITIATION.toInt();
                    VideoShare videoShare = VideoShare.this;
                    videoShare.transitionTo(videoShare.mTerminatedState);
                    return true;
                }
                VideoShare videoShare2 = VideoShare.this;
                videoShare2.transitionTo(videoShare2.mOutgoingPendingState);
                return true;
            } else if (i != 4) {
                return false;
            } else {
                VideoShare.this.mVshModule.notityIncommingSession(VideoShare.this.mContent.shareId, VideoShare.this.mContent.shareContactUri, VideoShare.this.mContent.dataPath);
                VideoShare videoShare3 = VideoShare.this;
                videoShare3.transitionTo(videoShare3.mIncomingPendingState);
                return true;
            }
        }
    }

    private class OutgoingPendingState extends State {
        private OutgoingPendingState() {
        }

        public void enter() {
            VideoShare.this.mContent.shareState = 2;
            VideoShare.this.mVshModule.putSession(VideoShare.this);
            VideoShare.this.mVshModule.notifyContentChange(VideoShare.this);
        }

        public boolean processMessage(Message message) {
            String r0 = VideoShare.LOG_TAG;
            Log.i(r0, "OutgoingPendingState Event: " + message.what);
            int i = message.what;
            if (i == 7) {
                VideoShare.this.mImsService.cancelVshSession(new CshCancelSessionParams(VideoShare.this.mSessionId, new ICshSuccessCallback() {
                    public void onSuccess() {
                        Log.d(VideoShare.LOG_TAG, "cancelVshSession  onSuccess");
                        VideoShare.this.sessionTerminatedByStack();
                    }

                    public void onFailure() {
                        Log.d(VideoShare.LOG_TAG, "cancelVshSession onFailure");
                        VideoShare videoShare = VideoShare.this;
                        videoShare.transitionTo(videoShare.mTerminatedState);
                    }
                }));
                VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.ABORTED_BY_USER.toInt();
                VideoShare videoShare = VideoShare.this;
                videoShare.transitionTo(videoShare.mPreTerminatedLocalState);
            } else if (i == 8) {
                VideoShare videoShare2 = VideoShare.this;
                videoShare2.transitionTo(videoShare2.mInProgressState);
            } else if (i == 9) {
                VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.ABORTED_BY_SYSTEM.toInt();
                VideoShare videoShare3 = VideoShare.this;
                videoShare3.transitionTo(videoShare3.mTerminatedState);
            } else if (i != 11) {
                return false;
            } else {
                VideoShare videoShare4 = VideoShare.this;
                videoShare4.transitionTo(videoShare4.mTerminatedState);
            }
            return true;
        }
    }

    private class IncomingPendingState extends State {
        boolean acceptByUser;

        private IncomingPendingState() {
            this.acceptByUser = false;
        }

        public void enter() {
            VideoShare.this.mContent.shareState = 2;
            VideoShare.this.mVshModule.putSession(VideoShare.this);
            VideoShare.this.mVshModule.notifyContentChange(VideoShare.this);
            this.acceptByUser = false;
        }

        public boolean processMessage(Message message) {
            String r0 = VideoShare.LOG_TAG;
            Log.i(r0, "IncomingPendingState Event: " + message.what);
            switch (message.what) {
                case 5:
                    VideoShare.this.mImsService.acceptVshSession(new CshAcceptSessionParams(VideoShare.this.mSessionId, VideoShare.this.obtainMessage(6, message.arg1)));
                    this.acceptByUser = true;
                    return true;
                case 6:
                    CshSessionResult cshSessionResult = (CshSessionResult) ((AsyncResult) message.obj).result;
                    if (cshSessionResult.mSessionNumber < 0 || cshSessionResult.mReason != CshErrorReason.SUCCESS) {
                        VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.FAILED_SHARING.toInt();
                        VideoShare videoShare = VideoShare.this;
                        videoShare.transitionTo(videoShare.mTerminatedState);
                        return true;
                    }
                    VideoShare videoShare2 = VideoShare.this;
                    videoShare2.transitionTo(videoShare2.mInProgressState);
                    return true;
                case 7:
                    if (!this.acceptByUser) {
                        VideoShare.this.mImsService.rejectVshSession(new CshRejectSessionParams(VideoShare.this.mSessionId, new ICshSuccessCallback() {
                            public void onSuccess() {
                                Log.d(VideoShare.LOG_TAG, "ICshSuccessCallback::onSuccess Enter");
                                VideoShare.this.sessionTerminatedByStack();
                            }

                            public void onFailure() {
                                Log.d(VideoShare.LOG_TAG, "ICshSuccessCallback::onFailure Enter");
                                VideoShare videoShare = VideoShare.this;
                                videoShare.transitionTo(videoShare.mTerminatedState);
                            }
                        }));
                        VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.REJECTED_BY_USER.toInt();
                        VideoShare videoShare3 = VideoShare.this;
                        videoShare3.transitionTo(videoShare3.mPreTerminatedLocalState);
                        return true;
                    }
                    VideoShare.this.doStopSession();
                    VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.ABORTED_BY_USER.toInt();
                    VideoShare videoShare4 = VideoShare.this;
                    videoShare4.transitionTo(videoShare4.mPreTerminatedLocalState);
                    return true;
                case 8:
                    VideoShare videoShare5 = VideoShare.this;
                    videoShare5.transitionTo(videoShare5.mInProgressState);
                    return true;
                case 9:
                    VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.ABORTED_BY_SYSTEM.toInt();
                    VideoShare videoShare6 = VideoShare.this;
                    videoShare6.transitionTo(videoShare6.mTerminatedState);
                    return true;
                case 11:
                    VideoShare videoShare7 = VideoShare.this;
                    videoShare7.transitionTo(videoShare7.mTerminatedState);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class InProgressState extends State {
        private InProgressState() {
        }

        public void enter() {
            VideoShare.this.mContent.shareState = 3;
            VideoShare.this.mVshModule.notifyContentChange(VideoShare.this);
            VideoShare.this.startDurationTimer();
        }

        public boolean processMessage(Message message) {
            String r0 = VideoShare.LOG_TAG;
            Log.i(r0, "InProgressState Event: " + message.what);
            int i = message.what;
            if (i == 3) {
                VideoShare.this.mImsService.setVshPhoneOrientation((VshOrientation) message.obj);
            } else if (i != 7) {
                switch (i) {
                    case 9:
                        VideoShare.this.stopDurationTimer();
                        VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.REJECTED_BY_REMOTE.toInt();
                        VideoShare videoShare = VideoShare.this;
                        videoShare.transitionTo(videoShare.mPreTerminatedRemoteState);
                        break;
                    case 10:
                        VideoShare.this.mVshModule.notifyApprochingVsMaxDuration(VideoShare.this.mContent.shareId, 30);
                        VideoShare videoShare2 = VideoShare.this;
                        videoShare2.transitionTo(videoShare2.mInProgressApproachMaxTimeState);
                        break;
                    case 11:
                        VideoShare videoShare3 = VideoShare.this;
                        videoShare3.transitionTo(videoShare3.mTerminatedState);
                        break;
                    default:
                        return false;
                }
            } else {
                VideoShare.this.doStopSession();
                VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.ABORTED_BY_USER.toInt();
                VideoShare videoShare4 = VideoShare.this;
                videoShare4.transitionTo(videoShare4.mPreTerminatedLocalState);
            }
            return true;
        }
    }

    private class InProgressApproachMaxTimeState extends State {
        public void enter() {
        }

        private InProgressApproachMaxTimeState() {
        }

        public boolean processMessage(Message message) {
            String r0 = VideoShare.LOG_TAG;
            Log.i(r0, "InProgressApproachMaxTimeState Event: " + message.what);
            int i = message.what;
            if (i != 3) {
                if (i != 7) {
                    if (i == 9) {
                        VideoShare.this.stopDurationTimer();
                        VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.REJECTED_BY_REMOTE.toInt();
                        VideoShare videoShare = VideoShare.this;
                        videoShare.transitionTo(videoShare.mPreTerminatedRemoteState);
                    } else if (i != 10) {
                        return false;
                    }
                }
                VideoShare.this.doStopSession();
                VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.ABORTED_BY_USER.toInt();
                VideoShare videoShare2 = VideoShare.this;
                videoShare2.transitionTo(videoShare2.mPreTerminatedLocalState);
            } else {
                VideoShare.this.mImsService.setVshPhoneOrientation((VshOrientation) message.obj);
            }
            return true;
        }
    }

    private class PreTerminatedRemoteState extends State {
        private PreTerminatedRemoteState() {
        }

        public void enter() {
            VideoShare.this.mContent.shareState = 16;
            VideoShare.this.mVshModule.notifyContentChange(VideoShare.this);
        }

        public boolean processMessage(Message message) {
            String r0 = VideoShare.LOG_TAG;
            Log.i(r0, "PreTerminatedRemoteState Event: " + message.what);
            if (message.what != 7) {
                return false;
            }
            VideoShare.this.mContent.reasonCode = VideoSharing.ReasonCode.REJECTED_BY_USER.toInt();
            VideoShare videoShare = VideoShare.this;
            videoShare.transitionTo(videoShare.mTerminatedState);
            return true;
        }
    }

    private class PreTerminatedLocalState extends State {
        private PreTerminatedLocalState() {
        }

        public void enter() {
            VideoShare.this.mContent.shareState = 15;
            VideoShare.this.mVshModule.notifyContentChange(VideoShare.this);
        }

        public boolean processMessage(Message message) {
            String r0 = VideoShare.LOG_TAG;
            Log.i(r0, "PreTerminatedLocalState Event: " + message.what);
            if (message.what != 9) {
                return false;
            }
            VideoShare videoShare = VideoShare.this;
            videoShare.transitionTo(videoShare.mTerminatedState);
            return true;
        }
    }

    private class TerminatedState extends State {
        private TerminatedState() {
        }

        public void enter() {
            VideoShare.this.mContent.shareState = 14;
            VideoShare.this.mVshModule.notifyContentChange(VideoShare.this);
            VideoShare.this.mVshModule.deleteSession(VideoShare.this.mSessionId);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void doStopSession() {
        stopDurationTimer();
        this.mImsService.stopVshSession(new CshCancelSessionParams(this.mSessionId, new ICshSuccessCallback() {
            public void onSuccess() {
                Log.i(VideoShare.LOG_TAG, "stopVshSession  onSuccess");
                VideoShare.this.sessionTerminatedByStack();
            }

            public void onFailure() {
                Log.d(VideoShare.LOG_TAG, "stopVshSession onFailure");
                VideoShare videoShare = VideoShare.this;
                videoShare.transitionTo(videoShare.mTerminatedState);
            }
        }));
    }
}
