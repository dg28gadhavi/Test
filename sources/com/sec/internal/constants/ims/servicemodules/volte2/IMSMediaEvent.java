package com.sec.internal.constants.ims.servicemodules.volte2;

public class IMSMediaEvent {
    public static final int AUDIO_FIRST_RTPRX_RECV = 29;
    public static final int AUDIO_FIRST_RTPTX_SEND = 18;
    public static final int AUDIO_RTCP_TIMEOUT = 61;
    public static final int AUDIO_RTP_STATS = 32;
    public static final int AUDIO_RTP_TIMEOUT = 28;
    public static final int AUDIO_RTP_TIMEOUT_CLEAR = 31;
    public static final int CAM_DISABLED_ERROR = 16;
    public static final int CMC_RECORDER_DURATION_IN_PROGRESS = 9;
    public static final int CMC_RECORDER_FILEOPERATION_ERROR = 2;
    public static final int CMC_RECORDER_FILESIZE_IN_PROGRESS = 8;
    public static final int CMC_RECORDER_INSUFFICIENT_START_MEMORY = 3;
    public static final int CMC_RECORDER_MAX_DURATION_REACHED = 5;
    public static final int CMC_RECORDER_MAX_FILESIZE_APPROACHING = 6;
    public static final int CMC_RECORDER_MAX_FILESIZE_REACHED = 7;
    public static final int CMC_RECORDER_NO_ERROR = 0;
    public static final int CMC_RECORDER_NO_SPACE = 1;
    public static final int CMC_RECORDER_RECORDERERROR = 4;
    public static final int CMC_RECORDER_START_SUCCESS = 10;
    public static final int CMC_RECORDER_STOP_SUCCESS = 11;
    public static final int DTMF_RECEIVED = 0;
    public static final int EPDG_DL_INFO = 78;
    public static final int EVENT_TYPE_AUDIO = 0;
    public static final int EVENT_TYPE_DTMF = 4;
    public static final int EVENT_TYPE_RELAY_STREAM = 3;
    public static final int EVENT_TYPE_TEXT = 2;
    public static final int EVENT_TYPE_VIDEO = 1;
    public static final int RECORD_ERROR_FILEOPEARTION = 2;
    public static final int RECORD_ERROR_NO_SPACE = 1;
    public static final int RELAY_CHANNEL_ESTABLISHED = 0;
    public static final int RELAY_CHANNEL_HOLDED = 4;
    public static final int RELAY_CHANNEL_RESUMED = 5;
    public static final int RELAY_CHANNEL_STARTED = 2;
    public static final int RELAY_CHANNEL_STOPED = 3;
    public static final int RELAY_CHANNEL_TERMINATED = 1;
    public static final int RELAY_ECHOLOCATE_RESULT = 5;
    public static final int RELAY_HOLD_STREAM = 10;
    public static final int RELAY_RECORD_START = 12;
    public static final int RELAY_RESUME_STREAM = 11;
    public static final int RELAY_RTCP_TIMEOUT = 4;
    public static final int RELAY_RTP_TIMEOUT = 3;
    public static final int RELAY_STREAM_CREATED = 0;
    public static final int RELAY_STREAM_DELETED = 1;
    public static final int RELAY_STREAM_UPDATED = 2;
    public static final int TEXT_PACKET_RECEIVED = 1;
    public static final int TEXT_RTP_TIMEOUT = 2;
    public static final int VIDEO_FIRST_PACKET_RECV = 17;
    public static final int VIDEO_RESP_RTCP_REPORT = 117;
    public static final int VIDEO_RTCP_TIMEOUT = 21;
    public static final int VIDEO_RTCP_TIMEOUT_CLEAR = 23;
    public static final int VIDEO_RTP_TIMEOUT = 20;
    public static final int VIDEO_RTP_TIMEOUT_CLEAR = 22;
    private int mAudioEvent = -1;
    private AudioRtpStats mAudioRtpStats = null;
    private int mCameraId = -1;
    private int mChannelId = -1;
    private int mCmcRecordingArg = 0;
    private int mCmcRecordingEvent = -1;
    private int mDtmKey = -1;
    private int mDtmfEvent = -1;
    private String mFileName = null;
    private int mGeneralEvent = -1;
    private int mHeight = -1;
    private boolean mIsHeldCall = false;
    private boolean mIsNearEnd = false;
    private int mPhoneId = -1;
    private int mRelayChannelEvent = -1;
    private int mRelayChannelId = -1;
    private AudioRtpStats mRelayRtpStats = null;
    private int mRelayStreamEvent = -1;
    private RtpLossRateNoti mRtpLossRate = null;
    private String mRttText = null;
    private int mRttTextLen = -1;
    private int mSessionID = -1;
    private MEDIA_STATE mState = MEDIA_STATE.NOT_INITIALIZED;
    private int mStreamId = -1;
    private int mTextEvent = -1;
    private int mVideoEvent = -1;
    private int mWidth = -1;

    public enum MEDIA_STATE {
        NOT_INITIALIZED,
        CAPTURE_SUCCEEDED,
        CAPTURE_FAILED,
        CAMERA_EVENT,
        VIDEO_HELD,
        VIDEO_RESUMED,
        VIDEO_AVAILABLE,
        VIDEO_ORIENTATION,
        CAMERA_FIRST_FRAME_READY,
        VIDEO_CALL_ESTABLISHED,
        VIDEO_RTP_TIMEOUT,
        VIDEO_RTCP_TIMEOUT,
        CAMERA_SWITCH_SUCCESS,
        CAMERA_SWITCH_FAIL,
        VIDEO_POOR_QUALITY,
        VIDEO_FAIR_QUALITY,
        VIDEO_GOOD_QUALITY,
        VIDEO_VERYPOOR_QUALITY,
        VIDEO_MAX_QUALITY,
        CALL_DOWNGRADED,
        VIDEO_HOLD_FAILED,
        VIDEO_RESUME_FAILED,
        CAMERA_START_FAIL,
        CAMERA_START_SUCCESS,
        CAMERA_STOP_SUCCESS,
        NO_FAR_FRAME,
        VIDEO_ATTEMPTED,
        CHANGE_PEER_DIMENSION,
        CAMERA_DISABLED_ERROR,
        RECORD_START_SUCCESS,
        RECORD_START_FAILURE,
        RECORD_START_FAILURE_NO_SPACE,
        RECORD_STOP_SUCCESS,
        RECORD_STOP_FAILURE,
        RECORD_STOP_NO_SPACE,
        EMOJI_START_SUCCESS,
        EMOJI_START_FAILURE,
        EMOJI_STOP_SUCCESS,
        EMOJI_STOP_FAILURE,
        EMOJI_INFO_CHANGE,
        IMS_GENERAL_EVENT_SUCCESS,
        IMS_GENERAL_EVENT_FAILURE,
        VCID_GENERAL_FAILURE
    }

    public static class AudioRtpStats {
        public int mChannelId;
        public int mDelay;
        public int mDirection;
        public int mJitter;
        public int mLossData;
        public int mMeasuredPeriod;

        public AudioRtpStats(int i, int i2, int i3, int i4, int i5, int i6) {
            this.mChannelId = i;
            this.mLossData = i2;
            this.mDelay = i3;
            this.mJitter = i4;
            this.mMeasuredPeriod = i5;
            this.mDirection = i6;
        }
    }

    public void setState(MEDIA_STATE media_state) {
        this.mState = media_state;
    }

    public MEDIA_STATE getState() {
        return this.mState;
    }

    public void setPhoneId(int i) {
        this.mPhoneId = i;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public void setSessionID(int i) {
        this.mSessionID = i;
    }

    public int getSessionID() {
        return this.mSessionID;
    }

    public int getCameraId() {
        return this.mCameraId;
    }

    public void setIsNearEnd(boolean z) {
        this.mIsNearEnd = z;
    }

    public boolean getIsNearEnd() {
        return this.mIsNearEnd;
    }

    public void setFileName(String str) {
        this.mFileName = str;
    }

    public String getFileName() {
        return this.mFileName;
    }

    public void setIsHeldCall(boolean z) {
        this.mIsHeldCall = z;
    }

    public boolean isHeldCall() {
        return this.mIsHeldCall;
    }

    public void setChannelId(int i) {
        this.mChannelId = i;
    }

    public int getChannelId() {
        return this.mChannelId;
    }

    public void setStreamId(int i) {
        this.mStreamId = i;
    }

    public int getStreamId() {
        return this.mStreamId;
    }

    public void setRelayChannelId(int i) {
        this.mRelayChannelId = i;
    }

    public int getRelayChannelId() {
        return this.mRelayChannelId;
    }

    public void setAudioEvent(int i) {
        this.mAudioEvent = i;
    }

    public int getAudioEvent() {
        return this.mAudioEvent;
    }

    public boolean isAudioEvent() {
        return this.mAudioEvent != -1;
    }

    public void setAudioRtpStats(AudioRtpStats audioRtpStats) {
        this.mAudioRtpStats = audioRtpStats;
    }

    public AudioRtpStats getAudioRtpStats() {
        return this.mAudioRtpStats;
    }

    public void setRtpLossRate(RtpLossRateNoti rtpLossRateNoti) {
        this.mRtpLossRate = rtpLossRateNoti;
    }

    public RtpLossRateNoti getRtpLossRate() {
        return this.mRtpLossRate;
    }

    public void setVideoEvent(int i) {
        this.mVideoEvent = i;
    }

    public void setTextEvent(int i) {
        this.mTextEvent = i;
    }

    public int getTextEvent() {
        return this.mTextEvent;
    }

    public boolean isTextEvent() {
        return this.mTextEvent != -1;
    }

    public int getVideoEvent() {
        return this.mVideoEvent;
    }

    public boolean isVideoEvent() {
        return this.mVideoEvent != -1;
    }

    public void setRelayStreamEvent(int i) {
        this.mRelayStreamEvent = i;
    }

    public int getRelayStreamEvent() {
        return this.mRelayStreamEvent;
    }

    public boolean isRelayStreamEvent() {
        return this.mRelayStreamEvent != -1;
    }

    public void setRelayRtpStats(AudioRtpStats audioRtpStats) {
        this.mRelayRtpStats = audioRtpStats;
    }

    public AudioRtpStats getRelayRtpStats() {
        return this.mRelayRtpStats;
    }

    public void setRelayChannelEvent(int i) {
        this.mRelayChannelEvent = i;
    }

    public int getRelayChannelEvent() {
        return this.mRelayChannelEvent;
    }

    public boolean isRelayChannelEvent() {
        return this.mRelayChannelEvent != -1;
    }

    public void setCmcRecordingEvent(int i) {
        this.mCmcRecordingEvent = i;
    }

    public int getCmcRecordingEvent() {
        return this.mCmcRecordingEvent;
    }

    public boolean isCmcRecordingEvent() {
        return this.mCmcRecordingEvent != -1;
    }

    public void setCmcRecordingArg(int i) {
        this.mCmcRecordingArg = i;
    }

    public int getCmcRecordingArg() {
        return this.mCmcRecordingArg;
    }

    public void setWidth(int i) {
        this.mWidth = i;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setHeight(int i) {
        this.mHeight = i;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void setRttText(String str) {
        this.mRttText = str;
    }

    public String getRttText() {
        return this.mRttText;
    }

    public void setRttTextLen(int i) {
        this.mRttTextLen = i;
    }

    public int getRttTextLen() {
        return this.mRttTextLen;
    }

    public void setDtmfEvent(int i) {
        this.mDtmfEvent = i;
    }

    public int getDtmfEvent() {
        return this.mDtmfEvent;
    }

    public boolean isDtmfEvent() {
        return this.mDtmfEvent != -1;
    }

    public void setGeneralEvent(int i) {
        this.mGeneralEvent = i;
    }

    public int getGeneralEvent() {
        return this.mGeneralEvent;
    }

    public boolean isGeneralEvent() {
        return this.mGeneralEvent != -1;
    }

    public void setDtmfKey(int i) {
        this.mDtmKey = i;
    }

    public int getDtmfKey() {
        return this.mDtmKey;
    }
}
