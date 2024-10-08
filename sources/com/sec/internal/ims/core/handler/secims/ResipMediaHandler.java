package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioManager;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.ToneGenerator;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.MediaHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ModifyVideoData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.NotifyVideoEventData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CallResponse;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.sve.IImsMediaEventListener;
import com.sec.sve.SecVideoEngineManager;
import com.sec.sve.generalevent.VcidEvent;

public class ResipMediaHandler extends MediaHandler {
    private static final int DTMF_VOLUME = 85;
    private static final int EVENT_CAMERA_START = 400;
    private static final int EVENT_CAMERA_STOP = 401;
    private static final int EVENT_CONNECT_TO_SVE = 300;
    private static final int EVENT_HOLD_VIDEO_RESPONSE = 201;
    private static final int EVENT_MODIFY_VIDEO = 107;
    private static final int EVENT_RESUME_VIDEO_RESPONSE = 202;
    private static final int EVENT_SVE_CONNECTED = 301;
    private static final int EVENT_VIDEO_EVENT = 108;
    public static final int MAX_VIDEO_CODEC_LIST_CHAR_SIZE = 256;
    private static final int NOTIFY_CAM_CAPTURE_FAILURE = 13;
    private static final int NOTIFY_CAM_CAPTURE_SUCCESS = 12;
    private static final int NOTIFY_CAM_DISABLED_ERROR = 16;
    private static final int NOTIFY_CAM_FIRST_FRAME_READY = 8;
    private static final int NOTIFY_CAM_START_FAILURE = 7;
    private static final int NOTIFY_CAM_START_SUCCESS = 6;
    private static final int NOTIFY_CAM_STOP_SUCCESS = 9;
    private static final int NOTIFY_CAM_SWITCH_FAILURE = 11;
    private static final int NOTIFY_CAM_SWITCH_SUCCESS = 10;
    private static final int NOTIFY_CHANGE_PEER_DIMENSION = 15;
    private static final int NOTIFY_EMOJI_INFO_CHANGE = 74;
    private static final int NOTIFY_EMOJI_START_FAILURE = 71;
    private static final int NOTIFY_EMOJI_START_SUCCESS = 70;
    private static final int NOTIFY_EMOJI_STOP_FAILURE = 73;
    private static final int NOTIFY_EMOJI_STOP_SUCCESS = 72;
    private static final int NOTIFY_FAR_FRAME_READY = 5;
    private static final int NOTIFY_IMS_GENERAL_EVENT_FAILURE = 81;
    private static final int NOTIFY_IMS_GENERAL_EVENT_SUCCESS = 80;
    private static final int NOTIFY_LCL_CAPTURE_FAILURE = 2;
    private static final int NOTIFY_LCL_CAPTURE_SUCCESS = 1;
    private static final int NOTIFY_NO_FAR_FRAME = 14;
    private static final int NOTIFY_RECORD_START_FAILURE = 51;
    private static final int NOTIFY_RECORD_START_FAILURE_NO_MEMORY = 52;
    private static final int NOTIFY_RECORD_START_SUCCESS = 50;
    private static final int NOTIFY_RECORD_STOP_FAILURE = 61;
    private static final int NOTIFY_RECORD_STOP_SUCCESS = 60;
    private static final int NOTIFY_RMT_CAPTURE_FAILURE = 4;
    private static final int NOTIFY_RMT_CAPTURE_SUCCESS = 3;
    private static final int NOTIFY_VCID_GENERAL_FAILURE = 90;
    public static final int NOTIFY_VIDEO_ATTEMPTED = 40;
    public static final int NOTIFY_VIDEO_FAIR_QUALITY = 31;
    public static final int NOTIFY_VIDEO_FIRST_PACKET_RECV = 17;
    public static final int NOTIFY_VIDEO_GOOD_QUALITY = 32;
    public static final int NOTIFY_VIDEO_MAX_QUALITY = 34;
    public static final int NOTIFY_VIDEO_POOR_QUALITY = 30;
    public static final int NOTIFY_VIDEO_RTCP_CLEAR = 23;
    public static final int NOTIFY_VIDEO_RTCP_TIMEOUT = 21;
    public static final int NOTIFY_VIDEO_RTP_CLEAR = 22;
    public static final int NOTIFY_VIDEO_RTP_TIMEOUT = 20;
    public static final int NOTIFY_VIDEO_VERYPOOR_QUALITY = 33;
    private Context mContext;
    public String mHwSupportedVideoCodecList = "";
    private IImsFramework mImsFramework;
    private IImsMediaEventListener mMediaEventlistener = new IImsMediaEventListener.Stub() {
        public void onAudioInjectionEnded(long j, long j2) {
        }

        public void onRecordingStopped(long j, long j2, String str) {
        }

        public void onAudioRtpRtcpTimeout(int i, int i2) {
            String access$000 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$000, "onAudioRtpRtcpTimeout " + i2);
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setChannelId(i);
            iMSMediaEvent.setPhoneId(i / 8);
            iMSMediaEvent.setAudioEvent(i2);
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onRtpLossRate(int i, int i2, float f, float f2, int i3) {
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setChannelId(i);
            iMSMediaEvent.setPhoneId(i / 8);
            iMSMediaEvent.setAudioEvent(78);
            iMSMediaEvent.setRtpLossRate(new RtpLossRateNoti(i2, f, f2, i3));
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onRtpStats(int i, int i2, int i3, int i4, int i5, int i6) {
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setChannelId(i);
            iMSMediaEvent.setPhoneId(i / 8);
            iMSMediaEvent.setAudioEvent(32);
            iMSMediaEvent.setAudioRtpStats(new IMSMediaEvent.AudioRtpStats(i, i2, i3, i4, i5, i6));
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onVideoEvent(int i, int i2, int i3, int i4, int i5) {
            String access$400 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$400, "Result : " + i + " event : " + i2 + " session id : " + i3);
            if (i != 3) {
                Log.e(ResipMediaHandler.this.LOG_TAG, "Invalid Video Event");
            }
            if (i3 >= 1007) {
                Log.e(ResipMediaHandler.this.LOG_TAG, "Ignore PTT Video Event in legacy VoLTE");
                return;
            }
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setSessionID(i3);
            if (i2 == 80) {
                iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.IMS_GENERAL_EVENT_SUCCESS);
                iMSMediaEvent.setGeneralEvent(i2);
            } else if (i2 == 81) {
                iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.IMS_GENERAL_EVENT_FAILURE);
                iMSMediaEvent.setGeneralEvent(i2);
            } else if (i2 != 90) {
                if (i2 != 117) {
                    switch (i2) {
                        case 1:
                        case 3:
                        case 12:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAPTURE_SUCCEEDED);
                            break;
                        case 2:
                        case 4:
                        case 13:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAPTURE_FAILED);
                            break;
                        case 5:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE);
                            break;
                        case 6:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS);
                            break;
                        case 7:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL);
                            break;
                        case 8:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_FIRST_FRAME_READY);
                            break;
                        case 9:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_STOP_SUCCESS);
                            break;
                        case 10:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_SUCCESS);
                            break;
                        case 11:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_FAIL);
                            break;
                        case 14:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.NO_FAR_FRAME);
                            break;
                        case 15:
                            iMSMediaEvent.setWidth(i4);
                            iMSMediaEvent.setHeight(i5);
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CHANGE_PEER_DIMENSION);
                            break;
                        case 16:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_DISABLED_ERROR);
                            iMSMediaEvent.setVideoEvent(i2);
                            break;
                        case 17:
                            iMSMediaEvent.setVideoEvent(i2);
                            break;
                        default:
                            switch (i2) {
                                case 20:
                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RTP_TIMEOUT);
                                    iMSMediaEvent.setVideoEvent(i2);
                                    break;
                                case 21:
                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RTCP_TIMEOUT);
                                    iMSMediaEvent.setVideoEvent(i2);
                                    break;
                                case 22:
                                case 23:
                                    break;
                                default:
                                    switch (i2) {
                                        case 30:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY);
                                            break;
                                        case 31:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY);
                                            break;
                                        case 32:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY);
                                            break;
                                        case 33:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY);
                                            break;
                                        case 34:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_MAX_QUALITY);
                                            break;
                                        default:
                                            switch (i2) {
                                                case 70:
                                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_SUCCESS);
                                                    break;
                                                case 71:
                                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE);
                                                    break;
                                                case 72:
                                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_SUCCESS);
                                                    break;
                                                case 73:
                                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_FAILURE);
                                                    break;
                                                case 74:
                                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_INFO_CHANGE);
                                                    break;
                                            }
                                    }
                            }
                    }
                }
                iMSMediaEvent.setVideoEvent(i2);
            } else {
                iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VCID_GENERAL_FAILURE);
                iMSMediaEvent.setGeneralEvent(i2);
            }
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onTextReceive(int i, int i2, String str, int i3, int i4) {
            String access$800 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$800, "onTextReceive " + i4);
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setTextEvent(i4);
            iMSMediaEvent.setChannelId(i);
            iMSMediaEvent.setSessionID(i2);
            iMSMediaEvent.setPhoneId(i / 8);
            iMSMediaEvent.setRttText(str);
            iMSMediaEvent.setRttTextLen(i3);
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onTextRtpRtcpTimeout(int i, int i2) {
            String access$1000 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$1000, "onTextRtpRtcpTimeout " + i2);
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setTextEvent(i2);
            iMSMediaEvent.setChannelId(i);
            iMSMediaEvent.setPhoneId(i / 8);
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onDtmfEvent(int i, int i2) {
            String access$1200 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$1200, "onDtmfEvent dtmfKey : " + i2);
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setDtmfEvent(0);
            iMSMediaEvent.setDtmfKey(i2);
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }

        public void onRecordEvent(int i, int i2) {
            String access$1400 = ResipMediaHandler.this.LOG_TAG;
            Log.i(access$1400, "onRecordEvent errCode : " + i2);
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setSessionID(i);
            if (i2 == 1) {
                iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_NO_SPACE);
            } else if (i2 == 2) {
                iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS);
            }
            ResipMediaHandler.this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }
    };
    private RingBackToneHandler mRingBackToneHandler = null;
    private HandlerThread mRingBackToneThread = null;
    private StackIF mStackIf;
    /* access modifiers changed from: private */
    public boolean mSveConnected = false;
    /* access modifiers changed from: private */
    public boolean mSveConnecting = false;
    private SecVideoEngineManager mSveManager;
    private ToneGenerator mToneGenerator = null;

    public void setCamera(int i) {
    }

    public ResipMediaHandler(Looper looper, Context context, IImsFramework iImsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = iImsFramework;
        this.mSveManager = new SecVideoEngineManager(context, new SecVideoEngineManager.ConnectionListener() {
            public void onDisconnected() {
                Log.i(ResipMediaHandler.this.LOG_TAG, "sve disconnected");
                ResipMediaHandler.this.mSveConnected = false;
                ResipMediaHandler.this.mSveConnecting = false;
                ResipMediaHandler.this.sendEmptyMessageDelayed(300, 1000);
            }

            public void onConnected() {
                Log.i(ResipMediaHandler.this.LOG_TAG, "sve connected.");
                ResipMediaHandler.this.mSveConnected = true;
                ResipMediaHandler.this.mSveConnecting = false;
                ResipMediaHandler.this.sendEmptyMessage(301);
            }
        });
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerModifyVideoEvent(this, 107, (Object) null);
        this.mStackIf.registerVideoEvent(this, 108, (Object) null);
        this.mSveConnected = false;
        this.mSveConnecting = false;
        sendEmptyMessage(300);
        HandlerThread handlerThread = new HandlerThread("RingBackToneThread");
        this.mRingBackToneThread = handlerThread;
        handlerThread.start();
        this.mRingBackToneHandler = new RingBackToneHandler(this.mRingBackToneThread.getLooper());
    }

    public void sendRtpStatsToStack(IMSMediaEvent.AudioRtpStats audioRtpStats) {
        this.mStackIf.sendRtpStatsToStack(audioRtpStats);
    }

    public void holdVideo(int i, int i2) {
        String str = this.LOG_TAG;
        Log.i(str, "holdVideo: sessionId " + i2);
        UserAgent ua = getUa(i);
        if (ua != null) {
            ua.holdVideo(i2, obtainMessage(201, Integer.valueOf(i2)));
        }
    }

    public void resumeVideo(int i, int i2) {
        String str = this.LOG_TAG;
        Log.i(str, "resumeVideo: sessionId " + i2);
        UserAgent ua = getUa(i);
        if (ua != null) {
            ua.resumeVideo(i2, obtainMessage(202, Integer.valueOf(i2)));
        }
    }

    public void startCamera(int i, int i2, int i3) {
        UserAgent ua = getUa(i);
        if (ua == null) {
            Log.e(this.LOG_TAG, "startCamera: can't find UserAgent for mmtel-video.");
            return;
        }
        SemSystemProperties.set("persist.ims.salescode.sve", OmcCode.getNWCode(i));
        ua.startCamera(i2, i3);
    }

    public void setPreviewResolution(int i, int i2) {
        this.mSveManager.setPreviewResolution(i, i2);
    }

    public void setPreviewSurface(int i, Object obj, int i2) {
        String str = this.LOG_TAG;
        Log.i(str, "setPreviewSurface() sessionId : " + i + " color : " + i2);
        this.mSveManager.setPreviewSurface(i, (Surface) obj, i2);
    }

    public void setDisplaySurface(int i, Object obj, int i2) {
        String str = this.LOG_TAG;
        Log.i(str, "setDisplaySurface() sessionId : " + i + " color : " + i2);
        this.mSveManager.setDisplaySurface(i, (Surface) obj, i2);
    }

    public void stopCamera(int i) {
        UserAgent ua = getUa(i);
        if (ua == null) {
            Log.e(this.LOG_TAG, "stopCamera: can't find UserAgent for mmtel-video.");
        } else {
            ua.stopCamera();
        }
    }

    public void setOrientation(int i) {
        this.mSveManager.setOrientation(i);
    }

    public void setZoom(float f) {
        this.mSveManager.setZoom(f);
    }

    public void switchCamera() {
        this.mSveManager.switchCamera();
    }

    public void sendStillImage(int i, boolean z, String str, String str2) {
        this.mSveManager.sendStillImage(i, z, str, str2);
    }

    public void setCameraEffect(int i) {
        this.mSveManager.setCameraEffect(i);
    }

    public void startRecord(int i, int i2, String str) {
        UserAgent ua = getUa(i);
        if (ua == null) {
            Log.e(this.LOG_TAG, "startRecord: can't find UserAgent for mmtel-video.");
        } else {
            ua.startRecord(i2, str);
        }
    }

    public void stopRecord(int i, int i2) {
        UserAgent ua = getUa(i);
        if (ua == null) {
            Log.e(this.LOG_TAG, "stopRecord: can't find UserAgent for mmtel-video.");
        } else {
            ua.stopRecord(i2);
        }
    }

    public void startEmoji(int i, int i2, String str) {
        this.mSveManager.startEmoji(i2, str);
    }

    public void stopEmoji(int i, int i2) {
        this.mSveManager.stopEmoji(i2);
    }

    public void restartEmoji(int i, int i2) {
        this.mSveManager.restartEmoji(i2);
    }

    public void sendGeneralEvent(int i, int i2, int i3, String str) {
        this.mSveManager.sveSendGeneralEvent(i, i2, i3, str);
    }

    public void sendGeneralBundleEvent(String str, Bundle bundle) {
        this.mSveManager.sendGeneralBundleEvent(str, bundle);
        String str2 = this.LOG_TAG;
        Log.i(str2, "sendGeneralBundleEvent - event: " + str + "extras: " + bundle.toString());
    }

    public void bindToNetwork(Network network) {
        String str = this.LOG_TAG;
        Log.i(str, "bindToNetwork : " + network);
        this.mSveManager.bindToNetwork(network);
    }

    private UserAgent getUa(int i) {
        IRegistrationManager registrationManager = this.mImsFramework.getRegistrationManager();
        UserAgent userAgent = (UserAgent) registrationManager.getUserAgent("mmtel-video", i);
        return userAgent == null ? (UserAgent) registrationManager.getUserAgent("vs", i) : userAgent;
    }

    private UserAgent getUaWithService(int i, String str) {
        IRegistrationManager registrationManager = this.mImsFramework.getRegistrationManager();
        if (i != -1) {
            return (UserAgent) registrationManager.getUserAgent(str, i);
        }
        return (UserAgent) registrationManager.getUserAgent(str);
    }

    private UserAgent getUaForMediaEvent(int i, int i2, int i3) {
        if (i3 != 1) {
            return getUaWithService(i, "mmtel");
        }
        if (i2 == 0 || i2 == 1) {
            return getUaWithService(i, "vs");
        }
        return getUaWithService(i, "mmtel-video");
    }

    private void onModifyVideo(AsyncResult asyncResult) {
        ModifyVideoData modifyVideoData = (ModifyVideoData) asyncResult.result;
        int session = (int) modifyVideoData.session();
        int direction = (int) modifyVideoData.direction();
        boolean isHeldCall = modifyVideoData.isHeldCall();
        String str = this.LOG_TAG;
        Log.i(str, "onModifyVideo() session: " + session + ", direction: " + direction + ", isHoldCall: " + isHeldCall);
        IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
        iMSMediaEvent.setSessionID(session);
        if (direction == 0) {
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_HELD);
        } else {
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RESUMED);
        }
        iMSMediaEvent.setIsHeldCall(isHeldCall);
        this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
    }

    private void onVideoEvent(AsyncResult asyncResult) {
        NotifyVideoEventData notifyVideoEventData = (NotifyVideoEventData) asyncResult.result;
        int session = (int) notifyVideoEventData.session();
        int event = (int) notifyVideoEventData.event();
        int arg1 = (int) notifyVideoEventData.arg1();
        int arg2 = (int) notifyVideoEventData.arg2();
        String str = this.LOG_TAG;
        Log.i(str, "onVideoEvent() session: " + session + ", event: " + event);
        IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
        iMSMediaEvent.setPhoneId((int) notifyVideoEventData.phoneId());
        iMSMediaEvent.setSessionID(session);
        if (event == 20) {
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RTP_TIMEOUT);
        } else if (event == 21) {
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RTCP_TIMEOUT);
        } else if (event == 40) {
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_ATTEMPTED);
        } else if (event == 60) {
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS);
        } else if (event != 61) {
            switch (event) {
                case 1:
                case 3:
                case 12:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAPTURE_SUCCEEDED);
                    break;
                case 2:
                case 4:
                case 13:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAPTURE_FAILED);
                    break;
                case 5:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE);
                    break;
                case 6:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_START_SUCCESS);
                    break;
                case 7:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL);
                    break;
                case 8:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_FIRST_FRAME_READY);
                    break;
                case 9:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_STOP_SUCCESS);
                    break;
                case 10:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_SUCCESS);
                    break;
                case 11:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_SWITCH_FAIL);
                    break;
                case 14:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.NO_FAR_FRAME);
                    break;
                case 15:
                    iMSMediaEvent.setWidth(arg1);
                    iMSMediaEvent.setHeight(arg2);
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CHANGE_PEER_DIMENSION);
                    break;
                case 16:
                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.CAMERA_DISABLED_ERROR);
                    break;
                default:
                    switch (event) {
                        case 30:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY);
                            break;
                        case 31:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY);
                            break;
                        case 32:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY);
                            break;
                        case 33:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY);
                            break;
                        case 34:
                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_MAX_QUALITY);
                            break;
                        default:
                            switch (event) {
                                case 50:
                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.RECORD_START_SUCCESS);
                                    break;
                                case 51:
                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE);
                                    break;
                                case 52:
                                    iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE_NO_SPACE);
                                    break;
                                default:
                                    switch (event) {
                                        case 70:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_SUCCESS);
                                            break;
                                        case 71:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_START_FAILURE);
                                            break;
                                        case 72:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_SUCCESS);
                                            break;
                                        case 73:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_STOP_FAILURE);
                                            break;
                                        case 74:
                                            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.EMOJI_INFO_CHANGE);
                                            break;
                                    }
                            }
                    }
            }
        } else {
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE);
        }
        this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
    }

    private void onHoldVideoResponse(AsyncResult asyncResult) {
        if (((CallResponse) asyncResult.result).result() != 0) {
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setSessionID(((Integer) asyncResult.userObj).intValue());
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_HOLD_FAILED);
            this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }
    }

    private void onResumeVideoResponse(AsyncResult asyncResult) {
        if (((CallResponse) asyncResult.result).result() != 0) {
            IMSMediaEvent iMSMediaEvent = new IMSMediaEvent();
            iMSMediaEvent.setSessionID(((Integer) asyncResult.userObj).intValue());
            iMSMediaEvent.setState(IMSMediaEvent.MEDIA_STATE.VIDEO_RESUME_FAILED);
            this.mMediaEventRegistrants.notifyResult(iMSMediaEvent);
        }
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 107) {
            onModifyVideo((AsyncResult) message.obj);
        } else if (i == 108) {
            onVideoEvent((AsyncResult) message.obj);
        } else if (i == 201) {
            onHoldVideoResponse((AsyncResult) message.obj);
        } else if (i == 202) {
            onResumeVideoResponse((AsyncResult) message.obj);
        } else if (i == 300) {
            connectToSve();
        } else if (i == 301) {
            onSveConnected();
        } else if (i == 400) {
            this.mSveManager.sveStartCamera(message.arg1, message.arg2);
        } else if (i == 401) {
            this.mSveManager.sveStopCamera();
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

    private void onSveConnected() {
        if (this.mSveConnected) {
            registerMediaEventListener();
            this.mSveManager.sveSendGeneralEvent(0, 0, 0, "");
            this.mSveManager.saeTerminate();
            this.mHwSupportedVideoCodecList = getMediaSupportedVideoCodecs();
            String str = this.LOG_TAG;
            Log.i(str, "hwSupportedVideoCodecList : " + this.mHwSupportedVideoCodecList);
            return;
        }
        Log.e(this.LOG_TAG, "SVE was not connected!!!");
    }

    public void saeInitialize(int i, int i2, int i3) {
        String str = this.LOG_TAG;
        Log.i(str, "saeInitialize convertedMno = " + i + " " + i2 + " " + i3);
        this.mSveManager.saeInitialize(i, i2, i3);
    }

    public int saeSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) {
        short s = (short) i13;
        short s2 = (short) i14;
        return this.mSveManager.saeSetCodecInfo(i, str, i2, i3, i4, i5, i6, i7, z, i8, i9, i10, i11, i12, c, c2, c3, c4, c5, c6, s, s2, str2, str3, str4, str5, str6, str7, str8, str9);
    }

    public int saeCreateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6, String str3, boolean z, boolean z2) {
        return this.mSveManager.saeCreateChannel(i, i2, str, i3, str2, i4, i5, i6, str3, z, z2);
    }

    public int saeStartChannel(int i, int i2, boolean z) {
        return this.mSveManager.saeStartChannel(i, i2, z);
    }

    public int saeUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) {
        return this.mSveManager.saeUpdateChannel(i, i2, str, i3, str2, i4, i5, i6);
    }

    public int saeStopChannel(int i) {
        return this.mSveManager.saeStopChannel(i);
    }

    public int saeModifyChannel(int i, int i2) {
        return this.mSveManager.saeModifyChannel(i, i2);
    }

    public int saeDeleteChannel(int i) {
        return this.mSveManager.saeDeleteChannel(i);
    }

    public int saeHandleDtmf(int i, int i2, int i3, int i4) {
        return this.mSveManager.saeHandleDtmf(i, i2, i3, i4);
    }

    public int saeSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5) {
        return this.mSveManager.saeSetDtmfCodecInfo(i, i2, i3, i4, i5);
    }

    public int saeEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) {
        return this.mSveManager.saeEnableSRTP(i, i2, i3, bArr, i4);
    }

    public int saeSetRtcpOnCall(int i, int i2, int i3) {
        return this.mSveManager.saeSetRtcpOnCall(i, i2, i3);
    }

    public int saeSetRtpTimeout(int i, long j) {
        return this.mSveManager.saeSetRtpTimeout(i, j);
    }

    public int saeSetRtcpTimeout(int i, long j) {
        return this.mSveManager.saeSetRtcpTimeout(i, j);
    }

    public int saeSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) {
        return this.mSveManager.saeSetRtcpXr(i, i2, i3, i4, i5, iArr);
    }

    public Object saeGetLastPlayedVoiceTime(int i) {
        return this.mSveManager.saeGetLastPlayedVoiceTime(i);
    }

    public int saeSetVoicePlayDelay(int i, int i2) {
        return this.mSveManager.saeSetVoicePlayDelay(i, i2);
    }

    public int saeSetTOS(int i, int i2) {
        return this.mSveManager.saeSetTOS(i, i2);
    }

    public int saeGetVersion(byte[] bArr, int i) {
        return this.mSveManager.saeGetVersion(bArr, i);
    }

    public int saeGetAudioRxTrackId(int i) {
        return this.mSveManager.saeGetAudioRxTrackId(i);
    }

    public int saeSetAudioPath(int i, int i2) {
        return this.mSveManager.saeSetAudioPath(i, i2);
    }

    public int sveCreateChannel() {
        return this.mSveManager.sveCreateChannel();
    }

    public int sveStartChannel(int i, int i2, int i3) {
        return this.mSveManager.sveStartChannel(i, i2, i3);
    }

    public int sveStopChannel(int i) {
        return this.mSveManager.sveStopChannel(i);
    }

    public int sveSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6, long j) {
        return this.mSveManager.sveSetConnection(i, str, i2, str2, i3, i4, i5, i6, j);
    }

    public int sveSetCodecInfo(int i, int i2, int i3, int i4, int i5, int i6, String str, int i7, int i8, int i9, int i10, int i11, boolean z, int i12, boolean z2, int i13, int i14, int i15, int i16, int i17, byte[] bArr, byte[] bArr2, byte[] bArr3, int i18, int i19, int i20) {
        return this.mSveManager.sveSetCodecInfo(i, i2, i3, i4, i5, i6, str, i7, i8, i9, i10, i11, z, i12, z2, i13, i14, i15, i16, i17, bArr, bArr2, bArr3, i18, i19, i20);
    }

    public int sveSetSRTPParams(int i, String str, byte[] bArr, int i2, int i3, int i4, int i5, String str2, byte[] bArr2, int i6, int i7, int i8, int i9) {
        return this.mSveManager.sveSetSRTPParams(i, str, bArr, i2, i3, i4, i5, str2, bArr2, i6, i7, i8, i9);
    }

    public int sveSetMediaConfig(int i, boolean z, int i2, boolean z2, int i3, int i4, int i5) {
        return this.mSveManager.sveSetMediaConfig(i, z, i2, z2, i3, i4, i5);
    }

    public int sveStartCamera(int i, int i2) {
        sendMessage(obtainMessage(400, i, i2));
        return 0;
    }

    public int sveStopCamera() {
        sendEmptyMessage(401);
        return 0;
    }

    public int sveSetHeldInfo(int i, boolean z, boolean z2) {
        return this.mSveManager.sveSetHeldInfo(i, z, z2);
    }

    public Object sveGetLastPlayedVideoTime(int i) {
        return this.mSveManager.sveGetLastPlayedVideoTime(i);
    }

    public int sveSetVideoPlayDelay(int i, int i2) {
        return this.mSveManager.sveSetVideoPlayDelay(i, i2);
    }

    public int sveSetNetworkQoS(int i, int i2, int i3, int i4) {
        return this.mSveManager.sveSetNetworkQoS(i, i2, i3, i4);
    }

    public int sveSendGeneralEvent(int i, int i2, int i3, String str) {
        return this.mSveManager.sveSendGeneralEvent(i, i2, i3, str);
    }

    public Object sveGetRtcpTimeInfo(int i) {
        return this.mSveManager.sveGetRtcpTimeInfo(i);
    }

    public int sveStartRecording(int i, int i2) {
        return this.mSveManager.sveStartRecording(i, i2);
    }

    public int sveStopRecording(int i) {
        return this.mSveManager.sveStopRecording(i);
    }

    public int saeStartRecording(int i, int i2, int i3, boolean z) {
        return this.mSveManager.saeStartRecording(i, i2, i3, z);
    }

    public int saeStopRecording(int i, boolean z) {
        return this.mSveManager.saeStopRecording(i, z);
    }

    public int sveRecorderCreate(int i, String str, int i2, int i3, String str2, int i4) {
        return this.mSveManager.sveRecorderCreate(i, str, i2, i3, str2, i4);
    }

    public int sveCmcRecorderCreate(int i, int i2, int i3, String str, int i4, int i5, long j, int i6, String str2, int i7, int i8, int i9, int i10, int i11, long j2, String str3) {
        int i12 = i;
        int i13 = i2;
        int i14 = i3;
        String str4 = this.LOG_TAG;
        Log.i(str4, "sveCmcRecorderCreate maxFileSize : " + j + ", fileSizeInterval : " + j2);
        return this.mSveManager.sveCmcRecorderCreate(i, i2, i3, str, i4, i5, j, i6, str2, i7, i8, i9, i10, i11, j2, str3);
    }

    public int sveRecorderDelete(int i) {
        return this.mSveManager.sveRecorderDelete(i);
    }

    public int sveRecorderStart(int i) {
        return this.mSveManager.sveRecorderStart(i);
    }

    public int sveRecorderStop(int i, boolean z) {
        return this.mSveManager.sveRecorderStop(i, z);
    }

    public void initToneGenerator() {
        if (this.mToneGenerator == null) {
            Log.i(this.LOG_TAG, "init ToneGenerator");
            this.mToneGenerator = new ToneGenerator(8, 85);
        }
    }

    public void deinitToneGenerator() {
        if (this.mToneGenerator != null) {
            Log.i(this.LOG_TAG, "deinit ToneGenerator");
            this.mToneGenerator.release();
            this.mToneGenerator = null;
        }
    }

    public void triggerTone(boolean z, int i, int i2) {
        if (this.mToneGenerator == null) {
            Log.e(this.LOG_TAG, "ToneGenerator was not initialized");
            return;
        }
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Tone #");
        sb.append(i);
        sb.append(z ? " start" : VcidEvent.BUNDLE_VALUE_ACTION_STOP);
        Log.i(str, sb.toString());
        if (i < 0 || i > 15) {
            i = 0;
        }
        if (z) {
            this.mToneGenerator.startTone(i, i2);
        } else {
            this.mToneGenerator.stopTone();
        }
    }

    public void setAudioParameters(int i, String str) {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (audioManager != null) {
            IVolteServiceModule volteServiceModule = this.mImsFramework.getServiceModuleManager().getVolteServiceModule();
            int i2 = 1;
            if (SimUtil.getPhoneCount() != 1) {
                if (i != 0) {
                    i2 = 0;
                }
                if (volteServiceModule.hasCsCall(i2)) {
                    Log.i(this.LOG_TAG, "skip to set to Audio F/W");
                    return;
                }
            }
            String str2 = this.LOG_TAG;
            Log.i(str2, " set to Audio F/W" + str);
            audioManager.setParameters(str);
        }
    }

    private static class RingBackToneHandler extends Handler {
        public static final int MUTE_RINGBACK_TONE = 3;
        public static final int START_RINGBACK_TONE = 1;
        public static final int STOP_RINGBACK_TONE = 2;
        private static final String TAG = "RBTHandler";
        private int mStreamType;
        private ToneGenerator mToneGenerator = null;
        private int mToneType;
        private int mVolume;

        public RingBackToneHandler(Looper looper) {
            super(looper);
        }

        private synchronized void startRingBackTone() {
            Log.i(TAG, "Start RBT!");
            if (!(this.mToneGenerator != null || this.mStreamType == -1 || this.mVolume == -1)) {
                this.mToneGenerator = new ToneGenerator(this.mStreamType, this.mVolume);
            }
            ToneGenerator toneGenerator = this.mToneGenerator;
            if (toneGenerator != null) {
                toneGenerator.startTone(this.mToneType);
            }
        }

        private synchronized void stopRingBackTone() {
            Log.i(TAG, "Stop RBT!");
            ToneGenerator toneGenerator = this.mToneGenerator;
            if (toneGenerator != null) {
                toneGenerator.stopTone();
                this.mToneGenerator.release();
            }
            this.mToneGenerator = null;
            this.mStreamType = -1;
            this.mVolume = -1;
        }

        private synchronized void muteRingBackTone() {
            ToneGenerator toneGenerator = this.mToneGenerator;
            if (toneGenerator != null) {
                toneGenerator.semSetVolume(0.0f);
                this.mVolume = 0;
            }
        }

        public synchronized void setRingBackToneData(int i, int i2, int i3) {
            this.mStreamType = i;
            this.mVolume = i2;
            this.mToneType = i3;
            if (this.mToneGenerator != null) {
                Log.i(TAG, "setRingBackToneData stopTone!");
                this.mToneGenerator.stopTone();
                this.mToneGenerator.release();
            }
            this.mToneGenerator = new ToneGenerator(i, i2);
        }

        public synchronized boolean isPlayingRingBackTone() {
            return this.mToneGenerator != null && this.mVolume > 0;
        }

        public void handleMessage(Message message) {
            if (message == null) {
                Log.e(TAG, "Invalid Message");
                return;
            }
            Log.i(TAG, "Event " + message.what);
            int i = message.what;
            if (i == 1) {
                startRingBackTone();
            } else if (i == 2) {
                stopRingBackTone();
            } else if (i != 3) {
                Log.e(TAG, "Invalid event");
            } else {
                muteRingBackTone();
            }
        }
    }

    public int startLocalRingBackTone(int i, int i2, int i3) {
        String str = this.LOG_TAG;
        Log.i(str, "start RBT with st" + i + " v-" + i2 + " tt-" + i3);
        this.mRingBackToneHandler.setRingBackToneData(i, i2, i3);
        this.mRingBackToneHandler.sendEmptyMessage(1);
        return 0;
    }

    public int stopLocalRingBackTone() {
        this.mRingBackToneHandler.sendEmptyMessage(2);
        return 0;
    }

    public boolean muteLocalRingBackTone() {
        if (!this.mRingBackToneHandler.isPlayingRingBackTone() && !this.mRingBackToneHandler.hasMessages(1)) {
            return false;
        }
        this.mRingBackToneHandler.sendEmptyMessage(3);
        return true;
    }

    public void registerMediaEventListener() {
        this.mSveManager.registerForMediaEventListener(this.mMediaEventlistener);
    }

    public void unregisterMediaEventListener() {
        this.mSveManager.unregisterForMediaEventListener(this.mMediaEventlistener);
    }

    public void sendMediaEvent(int i, int i2, int i3, int i4) {
        UserAgent uaForMediaEvent = getUaForMediaEvent(i, i2, i4);
        if (uaForMediaEvent == null) {
            Log.e(this.LOG_TAG, "User Agent was empty!");
        } else {
            uaForMediaEvent.sendMediaEvent(i2, i3, i4);
        }
    }

    public void steInitialize() {
        this.mSveManager.steInitialize();
    }

    public int steSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) {
        short s = (short) i13;
        short s2 = (short) i14;
        return this.mSveManager.steSetCodecInfo(i, str, i2, i3, i4, i5, i6, i7, z, i8, i9, i10, i11, i12, c, c2, c3, c4, c5, c6, s, s2, str2, str3, str4, str5, str6, str7, str8, str9);
    }

    public int steCreateChannel(int i, String str, int i2, String str2, int i3, int i4, int i5, String str3, boolean z, boolean z2) {
        return this.mSveManager.steCreateChannel(i, str, i2, str2, i3, i4, i5, str3, z, z2);
    }

    public int steStartChannel(int i, int i2, boolean z) {
        return this.mSveManager.steStartChannel(i, i2, z);
    }

    public int steStopChannel(int i) {
        return this.mSveManager.steStopChannel(i);
    }

    public int steDeleteChannel(int i) {
        return this.mSveManager.steDeleteChannel(i);
    }

    public int steUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) {
        return this.mSveManager.steUpdateChannel(i, i2, str, i3, str2, i4, i5, i6);
    }

    public int steModifyChannel(int i, int i2) {
        return this.mSveManager.steModifyChannel(i, i2);
    }

    public int steSetNetId(int i, int i2) {
        return this.mSveManager.steSetNetId(i, i2);
    }

    public int steSetSessionId(int i, int i2) {
        return this.mSveManager.steSetSessionId(i, i2);
    }

    public int steSendText(int i, String str, int i2) {
        return this.mSveManager.steSendText(i, str, i2);
    }

    public int steSetCallOptions(int i, boolean z) {
        return this.mSveManager.steSetCallOptions(i, z);
    }

    public int steSetRtcpTimeout(int i, long j) {
        return this.mSveManager.steSetRtcpTimeout(i, j);
    }

    public int steEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) {
        return this.mSveManager.steEnableSRTP(i, i2, i3, bArr, i4);
    }

    public int steSetRtcpOnCall(int i, int i2, int i3) {
        return this.mSveManager.steSetRtcpOnCall(i, i2, i3);
    }

    public String getHwSupportedVideoCodecs(String str) {
        String str2 = this.mHwSupportedVideoCodecList;
        if (str2 == null || str2.isEmpty()) {
            Log.i(this.LOG_TAG, "getHwSupportedVideoCodecs - fails to get HW supported codec");
            return str;
        }
        String[] split = str.split(",");
        String[] split2 = this.mHwSupportedVideoCodecList.split(",");
        String str3 = "";
        for (String str4 : split) {
            for (String equals : split2) {
                if (equals.equals(str4)) {
                    if (TextUtils.isEmpty(str3)) {
                        str3 = str4;
                    } else {
                        str3 = str3 + "," + str4;
                    }
                }
            }
        }
        Log.i(this.LOG_TAG, "getHwSupportedVideoCodecs filteredCodecs : " + str3);
        return str3;
    }

    public String getMediaSupportedVideoCodecs() {
        MediaCodecList mediaCodecList = new MediaCodecList(0);
        MediaFormat createVideoFormat = MediaFormat.createVideoFormat("video/hevc", 720, 1280);
        MediaFormat createVideoFormat2 = MediaFormat.createVideoFormat("video/hevc", 1280, 720);
        try {
            String findEncoderForFormat = mediaCodecList.findEncoderForFormat(createVideoFormat);
            String findDecoderForFormat = mediaCodecList.findDecoderForFormat(createVideoFormat);
            String findEncoderForFormat2 = mediaCodecList.findEncoderForFormat(createVideoFormat2);
            String findDecoderForFormat2 = mediaCodecList.findDecoderForFormat(createVideoFormat2);
            String str = this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("isMediaH265VideoCodecSupported: encoders: ");
            String str2 = "null";
            sb.append(findEncoderForFormat == null ? str2 : findEncoderForFormat);
            sb.append(", isMediaH265VideoCodecSupported: decoders: ");
            sb.append(findDecoderForFormat == null ? str2 : findDecoderForFormat);
            sb.append(", isMediaH265LVideoCodecSupported: encoders: ");
            sb.append(findEncoderForFormat2 == null ? str2 : findEncoderForFormat2);
            sb.append(", isMediaH265LVideoCodecSupported: decoders: ");
            if (findDecoderForFormat2 != null) {
                str2 = findDecoderForFormat2;
            }
            sb.append(str2);
            Log.i(str, sb.toString());
            if (TextUtils.isEmpty(findEncoderForFormat) || TextUtils.isEmpty(findDecoderForFormat) || TextUtils.isEmpty(findEncoderForFormat2) || TextUtils.isEmpty(findDecoderForFormat2)) {
                return "H263,H264";
            }
            return "H263,H264" + ",H265";
        } catch (IllegalArgumentException | NullPointerException e) {
            String str3 = this.LOG_TAG;
            Log.e(str3, "getMediaSupportedVideoCodecs: " + e);
            return "H263,H264";
        }
    }

    public boolean getSupportVowifiDisable5gsa() {
        Cursor query;
        Uri.Builder buildUpon = Uri.withAppendedPath(Uri.withAppendedPath(Uri.parse("content://com.samsung.ims.entitlementconfig.provider"), "config"), "xpath").buildUpon();
        buildUpon.appendQueryParameter("tag_name", "//VoNR/VoWiFiDisable5GSA");
        Uri build = buildUpon.build();
        String str = null;
        try {
            query = this.mContext.getContentResolver().query(build, (String[]) null, (String) null, (String[]) null, (String) null);
            String str2 = this.LOG_TAG;
            Log.i(str2, "getSupportVowifiDisable5gsa : " + build);
            if (query == null || !query.moveToFirst()) {
                Log.i(this.LOG_TAG, "getSupportVowifiDisable5gsa : cursor is null");
            } else {
                str = query.getString(1);
            }
            if (query != null) {
                query.close();
            }
        } catch (SQLException e) {
            String str3 = this.LOG_TAG;
            Log.e(str3, "getSupportVowifiDisable5gsa: " + e);
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        if (ConfigConstants.VALUE.INFO_COMPLETED.equalsIgnoreCase(str)) {
            return false;
        }
        return true;
        throw th;
    }
}
