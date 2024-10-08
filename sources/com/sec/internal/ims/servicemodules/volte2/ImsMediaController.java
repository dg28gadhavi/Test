package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import com.sec.ims.volte2.IImsMediaCallProvider;
import com.sec.ims.volte2.IVideoServiceEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.servicemodules.volte2.data.DtmfInfo;
import com.sec.internal.ims.servicemodules.volte2.data.TextInfo;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface;
import com.sec.internal.log.IMSLog;
import com.sec.sve.generalevent.VcidEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImsMediaController extends IImsMediaCallProvider.Stub implements IImsMediaController {
    public static final int CAMERA_ID_DEFAULT = 2;
    public static final int CAMERA_ID_FRONT = 1;
    public static final int CAMERA_ID_REAR = 0;
    private static final int EVENT_IMS_MEDIA_EVENT = 1;
    public static final String EVENT_VCID_FAILURE = "VCIDGeneralFailure";
    private static final String LOG_TAG = ImsMediaController.class.getSimpleName();
    private List<ImsCallSession> mCallSessions = null;
    private final RemoteCallbackList<IVideoServiceEventListener> mCallbacks = new RemoteCallbackList<>();
    private final Context mContext;
    private int mDefaultCameraId = -1;
    private SimpleEventLog mEventLog;
    private boolean mIsUsingCamera = false;
    private Handler mMediaEventHandler = null;
    private IMediaServiceInterface mMediaSvcIntf = null;
    private int mPendingCameraId = -1;
    private int mPendingCameraRequestor = -1;
    private IVolteServiceModuleInternal mVolteServiceModule = null;

    public ImsMediaController(IVolteServiceModuleInternal iVolteServiceModuleInternal, Looper looper, Context context, SimpleEventLog simpleEventLog) {
        this.mEventLog = simpleEventLog;
        this.mCallSessions = new ArrayList();
        this.mVolteServiceModule = iVolteServiceModuleInternal;
        this.mMediaSvcIntf = iVolteServiceModuleInternal.getMediaSvcIntf();
        this.mContext = context;
        this.mMediaEventHandler = new Handler(looper) {
            public void handleMessage(Message message) {
                super.handleMessage(message);
                AsyncResult asyncResult = (AsyncResult) message.obj;
                if (message.what == 1) {
                    ImsMediaController.this.onImsMediaEvent((IMSMediaEvent) asyncResult.result);
                }
            }
        };
        init();
    }

    public void init() {
        this.mMediaSvcIntf.registerForMediaEvent(this.mMediaEventHandler, 1, (Object) null);
    }

    public void registerForMediaEvent(ImsCallSession imsCallSession) {
        if (imsCallSession != null) {
            String str = LOG_TAG;
            Log.i(str, "registerForMediaEvent: session " + imsCallSession.getSessionId());
            synchronized (this.mCallSessions) {
                this.mCallSessions.add(imsCallSession);
            }
            return;
        }
        Log.e(LOG_TAG, "registerForMediaEvent: session null!!!");
    }

    public void unregisterForMediaEvent(ImsCallSession imsCallSession) {
        if (imsCallSession != null) {
            String str = LOG_TAG;
            Log.i(str, "unregisterForMediaEvent: session " + imsCallSession.getSessionId());
            synchronized (this.mCallSessions) {
                this.mCallSessions.remove(imsCallSession);
            }
            return;
        }
        Log.e(LOG_TAG, "unregisterForMediaEvent: session null!!!");
    }

    public synchronized void registerForVideoServiceEvent(IVideoServiceEventListener iVideoServiceEventListener) {
        Log.i(LOG_TAG, "registerForVideoServiceEvent");
        this.mCallbacks.register(iVideoServiceEventListener);
    }

    public synchronized void unregisterForVideoServiceEvent(IVideoServiceEventListener iVideoServiceEventListener) {
        Log.i(LOG_TAG, "unregisterForVideoServiceEvent");
        this.mCallbacks.unregister(iVideoServiceEventListener);
    }

    public void setCamera(String str) {
        try {
            int parseInt = Integer.parseInt(str);
            this.mDefaultCameraId = parseInt;
            this.mMediaSvcIntf.setCamera(parseInt);
        } catch (NumberFormatException unused) {
            String str2 = LOG_TAG;
            Log.i(str2, "Invalid for ImsVideoCall : setCamera- " + str);
        }
    }

    public void setPreviewSurface(int i, Surface surface) {
        this.mMediaSvcIntf.setPreviewSurface(i, surface, 0);
    }

    public void setDisplaySurface(int i, Surface surface) {
        ImsCallSession session = getSession(i);
        if (session != null) {
            session.setDisplaySurface(surface);
            if (session.getCallProfile().getCallType() == 6 && session.getCallProfile().getConfSessionId() > 0) {
                i = session.getCallProfile().getConfSessionId();
                String str = LOG_TAG;
                Log.i(str, "setDisplaySurface sessionId changed to " + i);
            }
        }
        this.mMediaSvcIntf.setDisplaySurface(i, surface, 0);
    }

    public void setDeviceOrientation(int i) {
        this.mMediaSvcIntf.setOrientation(i);
    }

    public void setZoom(float f) {
        this.mMediaSvcIntf.setZoom(f);
    }

    public void requestCallDataUsage() {
        this.mMediaSvcIntf.requestCallDataUsage();
    }

    public void holdVideo(int i) {
        String str = LOG_TAG;
        Log.i(str, "holdVideo: sessionId=" + i);
        ImsCallSession session = getSession(i);
        int phoneId = (session == null || session.getCallState() == null) ? 0 : session.getPhoneId();
        IMSLog.c(LogClass.VOLTE_HOLD_VIDEO, phoneId + "," + i);
        this.mMediaSvcIntf.holdVideo(phoneId, i);
        setVideoPause(i, true);
    }

    public void resumeVideo(int i) {
        String str = LOG_TAG;
        Log.i(str, "resumeVideo: sessionId=" + i);
        ImsCallSession session = getSession(i);
        int phoneId = (session == null || session.getCallState() == null) ? 0 : session.getPhoneId();
        IMSLog.c(LogClass.VOLTE_RESUME_VIDEO, phoneId + "," + i);
        this.mMediaSvcIntf.resumeVideo(phoneId, i);
    }

    public void setPreviewResolution(int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "setPreviewResolution width : " + i + " height : " + i2);
        this.mMediaSvcIntf.setPreviewResolution(i, i2);
    }

    private synchronized void logCamera(boolean z, int i, int i2, boolean z2) {
        ImsCallSession session;
        if (this.mIsUsingCamera != z) {
            String str = "null";
            if (!(i < 0 || (session = getSession(i)) == null || session.getCallState() == null)) {
                str = session.getCallState().name();
            }
            SimpleEventLog simpleEventLog = this.mEventLog;
            StringBuilder sb = new StringBuilder();
            sb.append(z ? VcidEvent.BUNDLE_VALUE_ACTION_START : VcidEvent.BUNDLE_VALUE_ACTION_STOP);
            sb.append("Camera: sessionId=");
            sb.append(i);
            sb.append(" (");
            sb.append(str);
            sb.append("), camera=");
            sb.append(i2);
            sb.append(" noti=");
            sb.append(z2);
            simpleEventLog.add(sb.toString());
            this.mIsUsingCamera = z;
        }
    }

    public void startCamera(Surface surface) {
        Log.i(LOG_TAG, "startCamera:");
        logCamera(true, -1, -1, false);
        this.mMediaSvcIntf.startCamera(surface);
    }

    public void startCamera(int i, int i2) {
        ImsCallSession activeCall;
        String str = LOG_TAG;
        Log.i(str, "startCamera: sessionId=" + i + " camera=" + i2);
        ImsCallSession session = getSession(i);
        if (i < 0 && (activeCall = getActiveCall()) != null) {
            i = activeCall.getSessionId();
            Log.i(str, "startCamera: using active sessionId=" + i + " camera=" + i2);
        }
        ImsCallSession cameraUsingSession = getCameraUsingSession();
        if (cameraUsingSession != null) {
            if (cameraUsingSession.getSessionId() == i) {
                Log.i(str, "startCamera: camera already active for session " + i);
                if (this.mDefaultCameraId == -1) {
                    this.mDefaultCameraId = i2;
                    return;
                }
                return;
            } else if (cameraUsingSession.getCallState() == CallConstants.STATE.VideoHeld) {
                cameraUsingSession.stopCamera();
            } else {
                Log.i(str, "startCamera: camera in use. pending sesssion " + i);
                this.mPendingCameraRequestor = i;
                this.mPendingCameraId = i2;
                if (session != null) {
                    session.setUsingCamera(false);
                    return;
                }
                return;
            }
        }
        this.mPendingCameraRequestor = -1;
        this.mPendingCameraId = -1;
        if (i2 != 2 && i2 >= 0) {
            this.mDefaultCameraId = i2;
        }
        if (session != null) {
            session.setUsingCamera(true);
        }
        ImsCallSession session2 = getSession(i);
        int phoneId = session2 != null ? session2.getPhoneId() : 0;
        IMSLog.c(LogClass.VOLTE_START_CAMERA, phoneId + "," + i + "," + i2);
        logCamera(true, i, i2, false);
        this.mMediaSvcIntf.startCamera(phoneId, i, this.mDefaultCameraId);
    }

    public void startCameraForActiveExcept(int i) {
        String str = LOG_TAG;
        Log.i(str, "startCameraForActiveExcept: " + i);
        ImsCallSession activeExcept = getActiveExcept(i);
        if (activeExcept != null) {
            Log.i(str, "active VT session found");
            activeExcept.startLastUsedCamera();
        }
    }

    public void stopCamera() {
        int i = 0;
        for (ImsCallSession next : this.mCallSessions) {
            next.setUsingCamera(false);
            i = next.getPhoneId();
        }
        Log.i(LOG_TAG, "stopCamera:");
        logCamera(false, -1, -1, false);
        IMSLog.c(LogClass.VOLTE_STOP_CAMERA, "" + i);
        this.mMediaSvcIntf.stopCamera(i);
    }

    public void stopActiveCamera() {
        String str = LOG_TAG;
        Log.i(str, "stopActiveCamera:");
        ImsCallSession cameraUsingSession = getCameraUsingSession();
        if (cameraUsingSession != null) {
            Log.i(str, "active VT session found");
            cameraUsingSession.stopCamera();
        }
    }

    public void stopCamera(int i) {
        String str = LOG_TAG;
        Log.i(str, "stopCamera: sessionId=" + i);
        ImsCallSession session = getSession(i);
        if (session == null || session.getUsingCamera()) {
            if (this.mPendingCameraRequestor == i) {
                this.mPendingCameraRequestor = -1;
                ImsCallSession cameraUsingSession = getCameraUsingSession();
                if (!(cameraUsingSession == null || cameraUsingSession.getSessionId() == i)) {
                    Log.i(str, "stopCamera: cancel pending camera.");
                    return;
                }
            }
            int i2 = 0;
            for (ImsCallSession next : this.mCallSessions) {
                next.setUsingCamera(false);
                if (next.getSessionId() == i) {
                    i2 = next.getPhoneId();
                }
            }
            logCamera(false, i, -1, false);
            this.mMediaSvcIntf.stopCamera(i2);
            if (this.mPendingCameraRequestor > 0) {
                Log.i(LOG_TAG, "stopCamera: start camera for pending session " + this.mPendingCameraRequestor);
                ImsCallSession session2 = getSession(this.mPendingCameraRequestor);
                if (!(session2 == null || session2.getCallState() == CallConstants.STATE.ReadyToCall)) {
                    logCamera(true, this.mPendingCameraRequestor, this.mPendingCameraId, false);
                    this.mMediaSvcIntf.startCamera(session2.getPhoneId(), this.mPendingCameraRequestor, this.mPendingCameraId);
                    session2.setUsingCamera(true);
                }
                this.mPendingCameraRequestor = -1;
                this.mPendingCameraId = -1;
                return;
            }
            return;
        }
        Log.i(str, "Do not call stopCamera multiple times");
    }

    public void switchCamera() {
        String str = LOG_TAG;
        Log.i(str, "switchCamera: current camera " + this.mDefaultCameraId);
        ImsCallSession cameraUsingSession = getCameraUsingSession();
        if (cameraUsingSession == null || cameraUsingSession.getCallState() == CallConstants.STATE.IncomingCall) {
            Log.i(str, "switchCamera: skip because incoming vtcall state");
            return;
        }
        if (this.mDefaultCameraId == 1) {
            this.mDefaultCameraId = 0;
        } else {
            this.mDefaultCameraId = 1;
        }
        this.mMediaSvcIntf.switchCamera();
    }

    public void resetCameraId() {
        Log.i(LOG_TAG, "resetCameraId:");
        this.mDefaultCameraId = -1;
        this.mMediaSvcIntf.resetCameraId();
    }

    public void getCameraInfo(int i) {
        this.mMediaSvcIntf.getCameraInfo(i);
    }

    public void startRender(boolean z) {
        this.mMediaSvcIntf.startRender(z);
    }

    public void startVideoRenderer(Surface surface) {
        this.mMediaSvcIntf.startVideoRenderer(surface);
    }

    public void stopVideoRenderer() {
        this.mMediaSvcIntf.stopVideoRenderer();
    }

    public void swipeVideoSurface() {
        this.mMediaSvcIntf.swipeVideoSurface();
    }

    public void deinitSurface(boolean z) {
        this.mMediaSvcIntf.deinitSurface(z);
    }

    public void getMaxZoom() {
        this.mMediaSvcIntf.getMaxZoom();
    }

    public void getZoom() {
        this.mMediaSvcIntf.getZoom();
    }

    public int getDefaultCameraId() {
        return this.mDefaultCameraId;
    }

    public synchronized void startRecord(String str) {
        ImsCallSession activeCall = getActiveCall();
        if (activeCall == null) {
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE);
            onRecordEvent(iMSMediaEvent);
        } else if (TextUtils.isEmpty(str)) {
            String str2 = LOG_TAG;
            Log.i(str2, "invalid filepath=" + str);
        } else {
            File file = new File(str);
            new File(file.isDirectory() ? file.getPath() : file.getParent()).mkdirs();
            this.mMediaSvcIntf.startRecord(activeCall.getPhoneId(), activeCall.getSessionId(), str);
        }
    }

    public synchronized void stopRecord() {
        ImsCallSession activeCall = getActiveCall();
        if (activeCall != null) {
            this.mMediaSvcIntf.stopRecord(activeCall.getPhoneId(), activeCall.getSessionId());
        } else {
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE);
            onRecordEvent(iMSMediaEvent);
        }
    }

    public synchronized void startEmoji(String str) {
        ImsCallSession activeCall = getActiveCall();
        if (activeCall != null) {
            this.mMediaSvcIntf.startEmoji(activeCall.getPhoneId(), activeCall.getSessionId(), str);
        } else {
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE);
            onEmojiEvent(iMSMediaEvent);
        }
    }

    public synchronized void stopEmoji(int i) {
        String str = LOG_TAG;
        Log.i(str, "stopEmoji : " + i);
        this.mMediaSvcIntf.stopEmoji(0, i);
    }

    public synchronized void sendGeneralEvent(int i, int i2, int i3, String str) {
        if (getActiveCall() != null) {
            String str2 = LOG_TAG;
            Log.i(str2, "sendGeneralEvent - event: " + i + ", arg1: " + i2 + ", arg2:" + i3 + ", arg3:" + str);
            this.mMediaSvcIntf.sendGeneralEvent(i, i2, i3, str);
        }
    }

    public synchronized void sendGeneralBundleEvent(String str, Bundle bundle) {
        if (hasActiveCall()) {
            String str2 = LOG_TAG;
            Log.i(str2, "sendGeneralBundleEvent - event: " + str + ", extras: " + bundle.toString());
            this.mMediaSvcIntf.sendGeneralBundleEvent(str, bundle);
        }
    }

    public void sendLiveVideo(int i) {
        Log.i(LOG_TAG, "sendStillImage() disable");
        this.mMediaSvcIntf.sendStillImage(i, false, (String) null, (String) null);
    }

    public void sendStillImage(int i, String str, int i2, String str2, int i3) {
        String str3 = LOG_TAG;
        Log.i(str3, "sendStillImage() enable filePath: " + str + " frameSize: " + str2);
        this.mMediaSvcIntf.sendStillImage(i, true, str, str2);
    }

    public void setCameraEffect(int i) {
        String str = LOG_TAG;
        Log.i(str, "setCameraEffect() value: " + i);
        this.mMediaSvcIntf.setCameraEffect(i);
    }

    public void bindToNetwork(Network network) {
        String str = LOG_TAG;
        Log.i(str, "bindToNetwork() " + network);
        this.mMediaSvcIntf.bindToNetwork(network);
    }

    private String getFrameSize() {
        ImsCallSession activeCall = getActiveCall();
        return activeCall != null ? activeCall.getCallProfile().getMediaProfile().getVideoSize() : "VGA";
    }

    private ImsCallSession getCameraUsingSession() {
        for (ImsCallSession next : this.mCallSessions) {
            if (next.getUsingCamera()) {
                return next;
            }
        }
        return null;
    }

    private ImsCallSession getActiveExcept(int i) {
        for (ImsCallSession next : this.mCallSessions) {
            if (next != null && next.getSessionId() != i && next.getCallState() == CallConstants.STATE.IncomingCall && ImsCallUtil.isVideoCall(next.getCallProfile().getCallType())) {
                return next;
            }
        }
        for (ImsCallSession next2 : this.mCallSessions) {
            if (next2 != null && next2.getSessionId() != i && next2.getCallState() == CallConstants.STATE.InCall && ImsCallUtil.isCameraUsingCall(next2.getCallProfile().getCallType())) {
                return next2;
            }
        }
        for (ImsCallSession next3 : this.mCallSessions) {
            if (next3 != null) {
                if (!SimUtil.getSimMno(next3.getPhoneId()).isChn()) {
                    return null;
                }
                if (next3.getSessionId() != i && next3.getCallState() == CallConstants.STATE.ModifyingCall && next3.getCallProfile().getCallType() == 1) {
                    return next3;
                }
            }
        }
        return null;
    }

    private ImsCallSession getActiveCall() {
        for (ImsCallSession next : this.mCallSessions) {
            if (next != null && next.getCallState() == CallConstants.STATE.InCall) {
                return next;
            }
        }
        return null;
    }

    private boolean hasActiveCall() {
        for (ImsCallSession next : this.mCallSessions) {
            if (next != null && next.getCallState() != CallConstants.STATE.Idle && next.getCallState() != CallConstants.STATE.EndingCall && next.getCallState() != CallConstants.STATE.EndedCall) {
                return true;
            }
        }
        return false;
    }

    private void onCaptureEvent(IMSMediaEvent iMSMediaEvent, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "onCaptureEvent: success=" + z);
    }

    private synchronized void onCameraEvent(IMSMediaEvent iMSMediaEvent) {
        String str = LOG_TAG;
        Log.i(str, "onCameraEvent " + iMSMediaEvent.getState());
        int i = iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS ? 1 : -1;
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_STOP_SUCCESS) {
            i = 3;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL) {
            i = 2;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_SUCCESS) {
            i = 5;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_FAIL) {
            i = 6;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_DISABLED_ERROR) {
            i = 7;
        }
        if (i == -1) {
            Log.i(str, "camera state not supported");
            return;
        }
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i2 = 0; i2 < beginBroadcast; i2++) {
            try {
                this.mCallbacks.getBroadcastItem(i2).onCameraState(iMSMediaEvent.getSessionID(), i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onVideoQuality(IMSMediaEvent iMSMediaEvent) {
        String str = LOG_TAG;
        Log.i(str, "onVideoQuality " + iMSMediaEvent.getState());
        int i = iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY ? 0 : -1;
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY) {
            i = 0;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY) {
            i = 1;
        }
        int i2 = 2;
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY) {
            i = 2;
        }
        if (iMSMediaEvent.getState() != IMSMediaEvent.MEDIA_STATE.VIDEO_MAX_QUALITY) {
            i2 = i;
        }
        if (i2 == -1) {
            Log.i(str, "video quality not supported");
            return;
        }
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i3 = 0; i3 < beginBroadcast; i3++) {
            try {
                this.mCallbacks.getBroadcastItem(i3).onVideoQualityChanged(iMSMediaEvent.getSessionID(), i2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onRecordEvent(IMSMediaEvent iMSMediaEvent) {
        String str = LOG_TAG;
        Log.i(str, "onRecordEvent " + iMSMediaEvent.getState());
        int i = iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_START_SUCCESS ? 0 : -1;
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE) {
            i = 1;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE_NO_SPACE) {
            i = 2;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS) {
            i = 3;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE) {
            i = 4;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.RECORD_STOP_NO_SPACE) {
            i = 5;
        }
        if (i == -1) {
            Log.i(str, "unknwon record event");
            return;
        }
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i2 = 0; i2 < beginBroadcast; i2++) {
            try {
                this.mCallbacks.getBroadcastItem(i2).onRecordState(iMSMediaEvent.getSessionID(), i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onEmojiEvent(IMSMediaEvent iMSMediaEvent) {
        String str = LOG_TAG;
        Log.i(str, "onEmojiEvent " + iMSMediaEvent.getState());
        int i = iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_START_SUCCESS ? 0 : -1;
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE) {
            i = 1;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_SUCCESS) {
            i = 2;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_FAILURE) {
            i = 3;
        }
        if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.EMOJI_INFO_CHANGE) {
            this.mMediaSvcIntf.restartEmoji(0, iMSMediaEvent.getSessionID());
        } else if (i == -1) {
            Log.i(str, "unknown emoji event");
        } else {
            int beginBroadcast = this.mCallbacks.beginBroadcast();
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                try {
                    this.mCallbacks.getBroadcastItem(i2).onEmojiState(iMSMediaEvent.getSessionID(), i);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallbacks.finishBroadcast();
        }
    }

    private synchronized void onGeneralEvent(IMSMediaEvent iMSMediaEvent) {
        String str = LOG_TAG;
        Log.i(str, "onGeneralEvent " + iMSMediaEvent.getState());
        String str2 = "";
        Bundle bundle = new Bundle();
        if (AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[iMSMediaEvent.getState().ordinal()] == 3) {
            str2 = "VCIDGeneralFailure";
        }
        ImsCallSession sessionByIMSMediaEvent = getSessionByIMSMediaEvent(iMSMediaEvent);
        if (sessionByIMSMediaEvent != null) {
            sessionByIMSMediaEvent.notifyImsGeneralEvent(str2, bundle);
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.ImsMediaController$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE;

        /* JADX WARNING: Can't wrap try/catch for region: R(70:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|(3:69|70|72)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(72:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|72) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x009c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x00b4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x00c0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x00cc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x00d8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x00e4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x00f0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00fc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x0108 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x0114 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x0120 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:51:0x012c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:53:0x0138 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:55:0x0144 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:57:0x0150 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:59:0x015c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:61:0x0168 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:63:0x0174 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:65:0x0180 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:67:0x018c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:69:0x0198 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE[] r0 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE = r0
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.IMS_GENERAL_EVENT_SUCCESS     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.IMS_GENERAL_EVENT_FAILURE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VCID_GENERAL_FAILURE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAPTURE_SUCCEEDED     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAPTURE_FAILED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_HELD     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_RESUMED     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_ORIENTATION     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.NO_FAR_FRAME     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_FIRST_FRAME_READY     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_STOP_SUCCESS     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_SUCCESS     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_EVENT     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_FAIL     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_DISABLED_ERROR     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CHANGE_PEER_DIMENSION     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00f0 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY     // Catch:{ NoSuchFieldError -> 0x00f0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f0 }
                r2 = 20
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00f0 }
            L_0x00f0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00fc }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY     // Catch:{ NoSuchFieldError -> 0x00fc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fc }
                r2 = 21
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00fc }
            L_0x00fc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0108 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY     // Catch:{ NoSuchFieldError -> 0x0108 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0108 }
                r2 = 22
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0108 }
            L_0x0108:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0114 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY     // Catch:{ NoSuchFieldError -> 0x0114 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0114 }
                r2 = 23
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0114 }
            L_0x0114:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0120 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_MAX_QUALITY     // Catch:{ NoSuchFieldError -> 0x0120 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0120 }
                r2 = 24
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0120 }
            L_0x0120:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x012c }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_START_SUCCESS     // Catch:{ NoSuchFieldError -> 0x012c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x012c }
                r2 = 25
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x012c }
            L_0x012c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0138 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE     // Catch:{ NoSuchFieldError -> 0x0138 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0138 }
                r2 = 26
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0138 }
            L_0x0138:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0144 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE_NO_SPACE     // Catch:{ NoSuchFieldError -> 0x0144 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0144 }
                r2 = 27
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0144 }
            L_0x0144:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0150 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS     // Catch:{ NoSuchFieldError -> 0x0150 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0150 }
                r2 = 28
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0150 }
            L_0x0150:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x015c }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE     // Catch:{ NoSuchFieldError -> 0x015c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x015c }
                r2 = 29
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x015c }
            L_0x015c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0168 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_STOP_NO_SPACE     // Catch:{ NoSuchFieldError -> 0x0168 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0168 }
                r2 = 30
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0168 }
            L_0x0168:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0174 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.EMOJI_START_SUCCESS     // Catch:{ NoSuchFieldError -> 0x0174 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0174 }
                r2 = 31
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0174 }
            L_0x0174:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0180 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE     // Catch:{ NoSuchFieldError -> 0x0180 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0180 }
                r2 = 32
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0180 }
            L_0x0180:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x018c }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_SUCCESS     // Catch:{ NoSuchFieldError -> 0x018c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x018c }
                r2 = 33
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x018c }
            L_0x018c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0198 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_FAILURE     // Catch:{ NoSuchFieldError -> 0x0198 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0198 }
                r2 = 34
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0198 }
            L_0x0198:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x01a4 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.EMOJI_INFO_CHANGE     // Catch:{ NoSuchFieldError -> 0x01a4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x01a4 }
                r2 = 35
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x01a4 }
            L_0x01a4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsMediaController.AnonymousClass3.<clinit>():void");
        }
    }

    private synchronized void onVideoHold(IMSMediaEvent iMSMediaEvent) {
        Log.i(LOG_TAG, "onVideoHold or no far frame");
        if (!iMSMediaEvent.isHeldCall()) {
            ImsCallSession session = getSession(iMSMediaEvent.getSessionID());
            if (session == null || session.getCallState() != CallConstants.STATE.HoldingVideo) {
                int beginBroadcast = this.mCallbacks.beginBroadcast();
                for (int i = 0; i < beginBroadcast; i++) {
                    try {
                        this.mCallbacks.getBroadcastItem(i).onVideoState(iMSMediaEvent.getSessionID(), 1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                this.mCallbacks.finishBroadcast();
            }
        }
    }

    private synchronized void onVideoResumed(IMSMediaEvent iMSMediaEvent) {
        Log.i(LOG_TAG, "onVideoResumed or far frame ready");
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onVideoState(iMSMediaEvent.getSessionID(), 2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onVideoAvailable(IMSMediaEvent iMSMediaEvent) {
        Log.i(LOG_TAG, "onVideoAvailable");
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onVideoState(iMSMediaEvent.getSessionID(), 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void onCallDowngraded(IMSMediaEvent iMSMediaEvent) {
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onVideoState(iMSMediaEvent.getSessionID(), 3);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onVideoOrientationChanged(IMSMediaEvent iMSMediaEvent) {
        Log.i(LOG_TAG, "onVideoOrientationChanged");
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onVideoOrientChanged(iMSMediaEvent.getSessionID());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private synchronized void onCameraFirstFrameReady(IMSMediaEvent iMSMediaEvent) {
        Log.i(LOG_TAG, "onCameraFirstFrameReady");
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onCameraState(iMSMediaEvent.getSessionID(), 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private void onClearUsingCamera() {
        synchronized (this.mCallSessions) {
            for (ImsCallSession next : this.mCallSessions) {
                if (next != null) {
                    next.setUsingCamera(false);
                }
            }
        }
    }

    private void onCameraStopSuccess() {
        if (this.mPendingCameraRequestor > 0) {
            String str = LOG_TAG;
            Log.i(str, "CAMERA_STOP_SUCCESS: start camera for pending session " + this.mPendingCameraRequestor);
            ImsCallSession session = getSession(this.mPendingCameraRequestor);
            if (!(session == null || session.getCallState() == CallConstants.STATE.ReadyToCall)) {
                logCamera(true, this.mPendingCameraRequestor, this.mPendingCameraId, false);
                this.mMediaSvcIntf.startCamera(session.getPhoneId(), this.mPendingCameraRequestor, this.mPendingCameraId);
                session.setUsingCamera(true);
            }
            this.mPendingCameraRequestor = -1;
            this.mPendingCameraId = -1;
        }
    }

    private synchronized void onChangePeerDimension(IMSMediaEvent iMSMediaEvent) {
        Log.i(LOG_TAG, "onChangePeerDimension");
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mCallbacks.getBroadcastItem(i).onChangePeerDimension(iMSMediaEvent.getSessionID(), iMSMediaEvent.getWidth(), iMSMediaEvent.getHeight());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void changeCameraCapabilities(int i, int i2, int i3) {
        Log.i(LOG_TAG, "changeCameraCapabilities");
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i4 = 0; i4 < beginBroadcast; i4++) {
            try {
                this.mCallbacks.getBroadcastItem(i4).changeCameraCapabilities(i, i2, i3);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void receiveSessionModifyRequest(int i, CallProfile callProfile) {
        Log.i(LOG_TAG, "receiveSessionModifyRequest");
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i2 = 0; i2 < beginBroadcast; i2++) {
            try {
                this.mCallbacks.getBroadcastItem(i2).receiveSessionModifyRequest(i, callProfile);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void receiveSessionModifyResponse(int i, int i2, CallProfile callProfile, CallProfile callProfile2) {
        Log.i(LOG_TAG, "receiveSessionModifyResponse");
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i3 = 0; i3 < beginBroadcast; i3++) {
            try {
                this.mCallbacks.getBroadcastItem(i3).receiveSessionModifyResponse(i, i2, callProfile, callProfile2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void setVideoPause(int i, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "setVideoPause : " + z);
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i2 = 0; i2 < beginBroadcast; i2++) {
            try {
                this.mCallbacks.getBroadcastItem(i2).setVideoPause(i, z);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    public synchronized void onChangeCallDataUsage(int i, long j) {
        String str = LOG_TAG;
        Log.i(str, "onChangeCallDataUsage : " + j);
        int beginBroadcast = this.mCallbacks.beginBroadcast();
        for (int i2 = 0; i2 < beginBroadcast; i2++) {
            try {
                this.mCallbacks.getBroadcastItem(i2).onChangeCallDataUsage(i, j);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mCallbacks.finishBroadcast();
    }

    private ImsCallSession getSession(int i) {
        synchronized (this.mCallSessions) {
            for (ImsCallSession next : this.mCallSessions) {
                if (next != null && next.getSessionId() == i) {
                    return next;
                }
            }
            return null;
        }
    }

    private void onHandleAudioEvent(IMSMediaEvent iMSMediaEvent) {
        Log.i(LOG_TAG, "handling Audio Event");
        int audioEvent = iMSMediaEvent.getAudioEvent();
        if (!(audioEvent == 18 || audioEvent == 61)) {
            if (audioEvent == 78) {
                IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mVolteServiceModule;
                if (iVolteServiceModuleInternal != null) {
                    iVolteServiceModuleInternal.sendRtpLossRate(iMSMediaEvent.getPhoneId(), iMSMediaEvent.getRtpLossRate());
                    return;
                }
                return;
            } else if (!(audioEvent == 28 || audioEvent == 29 || audioEvent == 31)) {
                if (audioEvent == 32) {
                    this.mMediaSvcIntf.sendRtpStatsToStack(iMSMediaEvent.getAudioRtpStats());
                    return;
                }
                return;
            }
        }
        this.mMediaSvcIntf.sendMediaEvent(iMSMediaEvent.getPhoneId(), iMSMediaEvent.getChannelId(), iMSMediaEvent.getAudioEvent(), 0);
    }

    private void onHandleTextEvent(IMSMediaEvent iMSMediaEvent) {
        Log.i(LOG_TAG, "handling Text Event");
        int textEvent = iMSMediaEvent.getTextEvent();
        if (textEvent != 1) {
            if (textEvent == 2) {
                this.mMediaSvcIntf.sendMediaEvent(iMSMediaEvent.getPhoneId(), iMSMediaEvent.getChannelId(), iMSMediaEvent.getTextEvent(), 2);
            }
        } else if (this.mVolteServiceModule != null) {
            this.mVolteServiceModule.onTextReceived(new TextInfo(iMSMediaEvent.getSessionID(), iMSMediaEvent.getRttText(), iMSMediaEvent.getRttTextLen()));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006e, code lost:
        if (r7.getSessionID() != 1) goto L_0x0093;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean onHandleVideoEvent(com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent r7) {
        /*
            r6 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "handling Video Event"
            android.util.Log.i(r0, r1)
            int r1 = r7.getVideoEvent()
            r2 = 16
            r3 = 0
            r4 = 1
            if (r1 == r2) goto L_0x0064
            r2 = 17
            if (r1 == r2) goto L_0x001e
            r0 = 117(0x75, float:1.64E-43)
            if (r1 == r0) goto L_0x0071
            switch(r1) {
                case 20: goto L_0x0071;
                case 21: goto L_0x0071;
                case 22: goto L_0x0071;
                case 23: goto L_0x0071;
                default: goto L_0x001c;
            }
        L_0x001c:
            goto L_0x0093
        L_0x001e:
            java.util.List<com.sec.internal.ims.servicemodules.volte2.ImsCallSession> r1 = r6.mCallSessions
            monitor-enter(r1)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r2 = r6.getSessionByIMSMediaEvent(r7)     // Catch:{ all -> 0x0061 }
            if (r2 == 0) goto L_0x005e
            com.sec.ims.volte2.data.CallProfile r5 = r2.getCallProfile()     // Catch:{ all -> 0x0061 }
            boolean r5 = r5.getDelayRinging()     // Catch:{ all -> 0x0061 }
            if (r5 == 0) goto L_0x005e
            java.lang.String r5 = "MT CRT 1st RTP got, set delay ringing false & notify Ringing"
            android.util.Log.i(r0, r5)     // Catch:{ all -> 0x0061 }
            com.sec.ims.volte2.data.CallProfile r0 = r2.getCallProfile()     // Catch:{ all -> 0x0061 }
            r0.setDelayRinging(r3)     // Catch:{ all -> 0x0061 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r6.mVolteServiceModule     // Catch:{ all -> 0x0061 }
            if (r0 == 0) goto L_0x004c
            int r5 = r2.getPhoneId()     // Catch:{ all -> 0x0061 }
            int r2 = r2.getCallId()     // Catch:{ all -> 0x0061 }
            r0.notifyOnIncomingCall(r5, r2)     // Catch:{ all -> 0x0061 }
        L_0x004c:
            com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface r6 = r6.mMediaSvcIntf     // Catch:{ all -> 0x0061 }
            int r0 = r7.getPhoneId()     // Catch:{ all -> 0x0061 }
            int r2 = r7.getSessionID()     // Catch:{ all -> 0x0061 }
            int r7 = r7.getVideoEvent()     // Catch:{ all -> 0x0061 }
            r6.sendMediaEvent(r0, r2, r7, r4)     // Catch:{ all -> 0x0061 }
            goto L_0x005f
        L_0x005e:
            r3 = r4
        L_0x005f:
            monitor-exit(r1)     // Catch:{ all -> 0x0061 }
            return r3
        L_0x0061:
            r6 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0061 }
            throw r6
        L_0x0064:
            int r0 = r7.getSessionID()
            if (r0 == 0) goto L_0x0071
            int r0 = r7.getSessionID()
            if (r0 == r4) goto L_0x0071
            goto L_0x0093
        L_0x0071:
            com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface r6 = r6.mMediaSvcIntf
            int r0 = r7.getPhoneId()
            int r1 = r7.getSessionID()
            int r2 = r7.getVideoEvent()
            r6.sendMediaEvent(r0, r1, r2, r4)
            int r6 = r7.getVideoEvent()
            r0 = 20
            if (r6 == r0) goto L_0x0093
            int r6 = r7.getVideoEvent()
            r7 = 21
            if (r6 == r7) goto L_0x0093
            return r3
        L_0x0093:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsMediaController.onHandleVideoEvent(com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent):boolean");
    }

    private void onHandleDtmfEvent(IMSMediaEvent iMSMediaEvent) {
        Log.i(LOG_TAG, "handling DTMF Event");
        if (iMSMediaEvent.getDtmfEvent() == 0 && this.mVolteServiceModule != null) {
            this.mVolteServiceModule.getCmcServiceHelper().onCmcDtmfInfo(new DtmfInfo(iMSMediaEvent.getDtmfKey(), -1, -1, -1));
        }
    }

    private ImsCallSession getSessionByIMSMediaEvent(IMSMediaEvent iMSMediaEvent) {
        for (ImsCallSession next : this.mCallSessions) {
            if (next != null) {
                Mno simMno = SimUtil.getSimMno(next.getPhoneId());
                int callType = next.getCallProfile().getCallType();
                if (simMno == Mno.SKT && callType == 6) {
                    String str = LOG_TAG;
                    Log.i(str, "Find conference call session : " + next.getSessionId());
                    return next;
                } else if (next.getSessionId() == iMSMediaEvent.getSessionID()) {
                    return next;
                }
            }
        }
        return null;
    }

    private void onNotifyIMSMediaEvent(ImsCallSession imsCallSession, IMSMediaEvent iMSMediaEvent) {
        String str = LOG_TAG;
        Log.i(str, "onImsMediaEvent: state=" + iMSMediaEvent.getState() + " phoneId=" + imsCallSession.getPhoneId());
        switch (AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[iMSMediaEvent.getState().ordinal()]) {
            case 1:
            case 2:
            case 3:
                onGeneralEvent(iMSMediaEvent);
                break;
            case 4:
                iMSMediaEvent.setIsNearEnd(false);
                iMSMediaEvent.setFileName((String) null);
                onCaptureEvent(iMSMediaEvent, true);
                break;
            case 5:
                onCaptureEvent(iMSMediaEvent, false);
                break;
            case 6:
                onVideoHold(iMSMediaEvent);
                break;
            case 7:
                onVideoResumed(iMSMediaEvent);
                break;
            case 8:
                onVideoAvailable(iMSMediaEvent);
                break;
            case 9:
                onVideoOrientationChanged(iMSMediaEvent);
                break;
            case 10:
                onVideoHold(iMSMediaEvent);
                break;
            case 11:
                onCameraFirstFrameReady(iMSMediaEvent);
                break;
            case 12:
                onClearUsingCamera();
                imsCallSession.setUsingCamera(true);
                imsCallSession.setStartCameraState(true);
                logCamera(true, -1, -1, true);
                onCameraEvent(iMSMediaEvent);
                break;
            case 13:
                onClearUsingCamera();
                onCameraStopSuccess();
                logCamera(false, -1, -1, true);
                final int sessionId = imsCallSession.getSessionId();
                IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mVolteServiceModule;
                if (iVolteServiceModuleInternal != null) {
                    iVolteServiceModuleInternal.post(new Runnable() {
                        public void run() {
                            ImsMediaController.this.startCameraForActiveExcept(sessionId);
                        }
                    });
                }
                onCameraEvent(iMSMediaEvent);
                break;
            case 14:
                onClearUsingCamera();
                imsCallSession.setStartCameraState(false);
                onCameraEvent(iMSMediaEvent);
                break;
            case 15:
                imsCallSession.onSwitchCamera();
                onCameraEvent(iMSMediaEvent);
                break;
            case 16:
            case 17:
            case 18:
                onCameraEvent(iMSMediaEvent);
                break;
            case 19:
                onChangePeerDimension(iMSMediaEvent);
                break;
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
                onVideoQuality(iMSMediaEvent);
                break;
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
                onRecordEvent(iMSMediaEvent);
                break;
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
                onEmojiEvent(iMSMediaEvent);
                break;
        }
        imsCallSession.notifyImsMediaEvent(iMSMediaEvent);
    }

    /* access modifiers changed from: private */
    public void onImsMediaEvent(IMSMediaEvent iMSMediaEvent) {
        ImsCallSession sessionByIMSMediaEvent;
        if (iMSMediaEvent.isAudioEvent()) {
            onHandleAudioEvent(iMSMediaEvent);
        } else if (iMSMediaEvent.isTextEvent()) {
            onHandleTextEvent(iMSMediaEvent);
        } else if (iMSMediaEvent.isGeneralEvent()) {
            onGeneralEvent(iMSMediaEvent);
        } else {
            if (iMSMediaEvent.isVideoEvent()) {
                if (!onHandleVideoEvent(iMSMediaEvent)) {
                    return;
                }
            } else if (iMSMediaEvent.isDtmfEvent()) {
                onHandleDtmfEvent(iMSMediaEvent);
                return;
            }
            synchronized (this.mCallSessions) {
                sessionByIMSMediaEvent = getSessionByIMSMediaEvent(iMSMediaEvent);
            }
            if (sessionByIMSMediaEvent == null) {
                String str = LOG_TAG;
                Log.i(str, "onImsMediaEvent: session " + iMSMediaEvent.getSessionID() + " not found.");
                if (iMSMediaEvent.getSessionID() != 0 && iMSMediaEvent.getSessionID() != 1 && iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS) {
                    stopCamera(iMSMediaEvent.getSessionID());
                    return;
                }
                return;
            }
            iMSMediaEvent.setSessionID(sessionByIMSMediaEvent.getSessionId());
            iMSMediaEvent.setPhoneId(sessionByIMSMediaEvent.getPhoneId());
            onNotifyIMSMediaEvent(sessionByIMSMediaEvent, iMSMediaEvent);
        }
    }
}
