package com.sec.internal.ims.core.handler.secims;

public class CallProfile {
    int amrBeMaxRed;
    int amrBeWbMaxRed;
    String amrMode;
    int amrOaMaxRed;
    int amrOaPayloadType;
    int amrOaWbMaxRed;
    int amrOpenPayloadType;
    int amrPayloadType;
    String amrWbMode;
    int amrWbOaPayloadType;
    int amrWbPayloadType;
    int audioAs;
    boolean audioAvpf;
    String audioCodec;
    int audioDscp;
    int audioPort;
    int audioRr;
    int audioRs;
    boolean audioRtcpXr;
    boolean audioSrtp;
    String displayFormat;
    String displayFormatHevc;
    int dtmfMode;
    int dtmfPayloadType;
    int dtmfWbPayloadType;
    boolean enableAvSync;
    boolean enableEvsCodec;
    boolean enableRtcpOnActiveCall;
    boolean enableScr;
    int evs2ndPayload;
    String evsBandwidthReceive;
    String evsBandwidthReceiveExt;
    String evsBandwidthSend;
    String evsBandwidthSendExt;
    String evsBitRateReceive;
    String evsBitRateReceiveExt;
    String evsBitRateSend;
    String evsBitRateSendExt;
    String evsChannelAwareReceive;
    String evsChannelRecv;
    String evsChannelSend;
    String evsCodecModeRequest;
    String evsDefaultBandwidth;
    String evsDefaultBitrate;
    String evsDiscontinuousTransmission;
    String evsDtxRecv;
    String evsHeaderFull;
    String evsLimitedCodec;
    int evsMaxRed;
    String evsModeSwitch;
    int evsPayload;
    int evsPayloadExt;
    boolean evsUseDefaultRtcpBw;
    int frameRate;
    int h263QcifPayloadType;
    int h264720pLPayloadType;
    int h264720pPayloadType;
    int h264CifLPayloadType;
    int h264CifPayloadType;
    int h264QvgaLPayloadType;
    int h264QvgaPayloadType;
    int h264VgaLPayloadType;
    int h264VgaPayloadType;
    int h265Hd720pLPayloadType;
    int h265Hd720pPayloadType;
    int h265QvgaLPayloadType;
    int h265QvgaPayloadType;
    int h265VgaLPayloadType;
    int h265VgaPayloadType;
    boolean ignoreRtcpTimeoutOnHoldCall;
    int maxPTime;
    int pTime;
    String packetizationMode;
    int rtcpTimeout;
    int rtpTimeout;
    int textAs;
    boolean textAvpf;
    int textPort;
    int textRr;
    int textRs;
    boolean textSrtp;
    boolean useSpsForH264Hd;
    int videoAs;
    boolean videoAvpf;
    boolean videoCapabilities;
    String videoCodec;
    int videoPort;
    int videoRr;
    int videoRs;
    boolean videoRtcpXr;
    boolean videoSrtp;

    public CallProfile(Builder builder) {
        this.audioCodec = builder.audioCodec;
        this.audioPort = builder.audioPort;
        this.audioDscp = builder.audioDscp;
        this.amrPayloadType = builder.amrPayloadType;
        this.amrOaPayloadType = builder.amrOaPayloadType;
        this.amrWbPayloadType = builder.amrWbPayloadType;
        this.amrWbOaPayloadType = builder.amrWbOaPayloadType;
        this.amrOpenPayloadType = builder.amrOpenPayloadType;
        this.dtmfPayloadType = builder.dtmfPayloadType;
        this.dtmfWbPayloadType = builder.dtmfWbPayloadType;
        this.amrOaMaxRed = builder.amrOaMaxRed;
        this.amrBeMaxRed = builder.amrBeMaxRed;
        this.amrOaWbMaxRed = builder.amrOaWbMaxRed;
        this.amrBeWbMaxRed = builder.amrBeWbMaxRed;
        this.evsMaxRed = builder.evsMaxRed;
        this.amrMode = builder.amrMode;
        this.amrWbMode = builder.amrWbMode;
        this.audioAs = builder.audioAs;
        this.audioRs = builder.audioRs;
        this.audioRr = builder.audioRr;
        this.pTime = builder.pTime;
        this.maxPTime = builder.maxPTime;
        this.videoCodec = builder.videoCodec;
        this.videoPort = builder.videoPort;
        this.frameRate = builder.frameRate;
        this.displayFormat = builder.displayFormat;
        this.displayFormatHevc = builder.displayFormatHevc;
        this.packetizationMode = builder.packetizationMode;
        this.h265QvgaPayloadType = builder.h265QvgaPayloadType;
        this.h265QvgaLPayloadType = builder.h265QvgaLPayloadType;
        this.h265VgaPayloadType = builder.h265VgaPayloadType;
        this.h265VgaLPayloadType = builder.h265VgaLPayloadType;
        this.h265Hd720pPayloadType = builder.h265Hd720pPayloadType;
        this.h265Hd720pLPayloadType = builder.h265Hd720pLPayloadType;
        this.h264720pPayloadType = builder.h264720pPayloadType;
        this.h264720pLPayloadType = builder.h264720pLPayloadType;
        this.h264VgaPayloadType = builder.h264VgaPayloadType;
        this.h264VgaLPayloadType = builder.h264VgaLPayloadType;
        this.h264QvgaPayloadType = builder.h264QvgaPayloadType;
        this.h264QvgaLPayloadType = builder.h264QvgaLPayloadType;
        this.h264CifPayloadType = builder.h264CifPayloadType;
        this.h264CifLPayloadType = builder.h264CifLPayloadType;
        this.h263QcifPayloadType = builder.h263QcifPayloadType;
        this.useSpsForH264Hd = builder.useSpsForH264Hd;
        this.videoAs = builder.videoAs;
        this.videoRs = builder.videoRs;
        this.videoRr = builder.videoRr;
        this.textAs = builder.textAs;
        this.textRs = builder.textRs;
        this.textRr = builder.textRr;
        this.textPort = builder.textPort;
        this.audioAvpf = builder.audioAvpf;
        this.audioSrtp = builder.audioSrtp;
        this.videoAvpf = builder.videoAvpf;
        this.videoSrtp = builder.videoSrtp;
        this.textAvpf = builder.textAvpf;
        this.textSrtp = builder.textSrtp;
        this.videoCapabilities = builder.videoCapabilities;
        this.rtpTimeout = builder.rtpTimeout;
        this.rtcpTimeout = builder.rtcpTimeout;
        this.ignoreRtcpTimeoutOnHoldCall = builder.ignoreRtcpTimeoutOnHoldCall;
        this.enableRtcpOnActiveCall = builder.enableRtcpOnActiveCall;
        this.enableAvSync = builder.enableAvSync;
        this.enableScr = builder.enableScr;
        this.audioRtcpXr = builder.audioRtcpXr;
        this.videoRtcpXr = builder.videoRtcpXr;
        this.dtmfMode = builder.dtmfMode;
        this.enableEvsCodec = builder.enableEvsCodec;
        this.evsDiscontinuousTransmission = builder.evsDiscontinuousTransmission;
        this.evsDtxRecv = builder.evsDtxRecv;
        this.evsHeaderFull = builder.evsHeaderFull;
        this.evsModeSwitch = builder.evsModeSwitch;
        this.evsChannelSend = builder.evsChannelSend;
        this.evsChannelRecv = builder.evsChannelRecv;
        this.evsChannelAwareReceive = builder.evsChannelAwareReceive;
        this.evsCodecModeRequest = builder.evsCodecModeRequest;
        this.evsBitRateSend = builder.evsBitRateSend;
        this.evsBitRateReceive = builder.evsBitRateReceive;
        this.evsBandwidthSend = builder.evsBandwidthSend;
        this.evsBandwidthReceive = builder.evsBandwidthReceive;
        this.evsPayload = builder.evsPayload;
        this.evs2ndPayload = builder.evs2ndPayload;
        this.evsDefaultBandwidth = builder.evsDefaultBandwidth;
        this.evsDefaultBitrate = builder.evsDefaultBitrate;
        this.evsPayloadExt = builder.evsPayloadExt;
        this.evsBitRateSendExt = builder.evsBitRateSendExt;
        this.evsBitRateReceiveExt = builder.evsBitRateReceiveExt;
        this.evsBandwidthSendExt = builder.evsBandwidthSendExt;
        this.evsBandwidthReceiveExt = builder.evsBandwidthReceiveExt;
        this.evsLimitedCodec = builder.evsLimitedCodec;
        this.evsUseDefaultRtcpBw = builder.evsUseDefaultRtcpBw;
    }

    public static class Builder {
        int amrBeMaxRed;
        int amrBeWbMaxRed;
        String amrMode;
        int amrOaMaxRed;
        int amrOaPayloadType;
        int amrOaWbMaxRed;
        int amrOpenPayloadType;
        int amrPayloadType;
        String amrWbMode;
        int amrWbOaPayloadType;
        int amrWbPayloadType;
        int audioAs;
        boolean audioAvpf;
        String audioCodec;
        int audioDscp;
        int audioPort;
        int audioRr;
        int audioRs;
        boolean audioRtcpXr;
        boolean audioSrtp;
        String displayFormat;
        String displayFormatHevc;
        int dtmfMode;
        int dtmfPayloadType;
        int dtmfWbPayloadType;
        boolean enableAvSync;
        boolean enableEvsCodec;
        boolean enableRtcpOnActiveCall;
        boolean enableScr;
        int evs2ndPayload;
        String evsBandwidthReceive;
        String evsBandwidthReceiveExt;
        String evsBandwidthSend;
        String evsBandwidthSendExt;
        String evsBitRateReceive;
        String evsBitRateReceiveExt;
        String evsBitRateSend;
        String evsBitRateSendExt;
        String evsChannelAwareReceive;
        String evsChannelRecv;
        String evsChannelSend;
        String evsCodecModeRequest;
        String evsDefaultBandwidth;
        String evsDefaultBitrate;
        String evsDiscontinuousTransmission;
        String evsDtxRecv;
        String evsHeaderFull;
        String evsLimitedCodec;
        int evsMaxRed;
        String evsModeSwitch;
        int evsPayload;
        int evsPayloadExt;
        boolean evsUseDefaultRtcpBw;
        int frameRate;
        int h263QcifPayloadType;
        int h264720pLPayloadType;
        int h264720pPayloadType;
        int h264CifLPayloadType;
        int h264CifPayloadType;
        int h264QvgaLPayloadType;
        int h264QvgaPayloadType;
        int h264VgaLPayloadType;
        int h264VgaPayloadType;
        int h265Hd720pLPayloadType;
        int h265Hd720pPayloadType;
        int h265QvgaLPayloadType;
        int h265QvgaPayloadType;
        int h265VgaLPayloadType;
        int h265VgaPayloadType;
        boolean ignoreRtcpTimeoutOnHoldCall;
        int maxPTime = 240;
        int pTime = 20;
        String packetizationMode;
        int rtcpTimeout;
        int rtpTimeout;
        int textAs;
        boolean textAvpf;
        int textPort;
        int textRr;
        int textRs;
        boolean textSrtp;
        boolean useSpsForH264Hd;
        int videoAs;
        boolean videoAvpf;
        boolean videoCapabilities;
        String videoCodec;
        int videoPort;
        int videoRr;
        int videoRs;
        boolean videoRtcpXr;
        boolean videoSrtp;

        public static Builder newBuilder() {
            return new Builder();
        }

        public CallProfile build() {
            return new CallProfile(this);
        }

        public Builder setAudioCodec(String str) {
            this.audioCodec = str;
            return this;
        }

        public Builder setAudioPort(int i) {
            this.audioPort = i;
            return this;
        }

        public Builder setAudioDscp(int i) {
            this.audioDscp = i;
            return this;
        }

        public Builder setAmrPayloadType(int i) {
            this.amrPayloadType = i;
            return this;
        }

        public Builder setAmrOaPayloadType(int i) {
            this.amrOaPayloadType = i;
            return this;
        }

        public Builder setAmrWbPayloadType(int i) {
            this.amrWbPayloadType = i;
            return this;
        }

        public Builder setAmrWbOaPayloadType(int i) {
            this.amrWbOaPayloadType = i;
            return this;
        }

        public Builder setAmrOpenPayloadType(int i) {
            this.amrOpenPayloadType = i;
            return this;
        }

        public Builder setDtmfWbPayloadType(int i) {
            this.dtmfWbPayloadType = i;
            return this;
        }

        public Builder setDtmfPayloadType(int i) {
            this.dtmfPayloadType = i;
            return this;
        }

        public Builder setAmrOaMaxRed(int i) {
            this.amrOaMaxRed = i;
            return this;
        }

        public Builder setAmrBeMaxRed(int i) {
            this.amrBeMaxRed = i;
            return this;
        }

        public Builder setAmrOaWbMaxRed(int i) {
            this.amrOaWbMaxRed = i;
            return this;
        }

        public Builder setAmrBeWbMaxRed(int i) {
            this.amrBeWbMaxRed = i;
            return this;
        }

        public Builder setEvsMaxRed(int i) {
            this.evsMaxRed = i;
            return this;
        }

        public Builder setAmrMode(String str) {
            this.amrMode = str;
            return this;
        }

        public Builder setAmrWbMode(String str) {
            this.amrWbMode = str;
            return this;
        }

        public Builder setAudioAs(int i) {
            this.audioAs = i;
            return this;
        }

        public Builder setAudioRs(int i) {
            this.audioRs = i;
            return this;
        }

        public Builder setAudioRr(int i) {
            this.audioRr = i;
            return this;
        }

        public Builder setPTime(int i) {
            this.pTime = i;
            return this;
        }

        public Builder setMaxPTime(int i) {
            this.maxPTime = i;
            return this;
        }

        public Builder setVideoCodec(String str) {
            this.videoCodec = str;
            return this;
        }

        public Builder setVideoPort(int i) {
            this.videoPort = i;
            return this;
        }

        public Builder setFrameRate(int i) {
            this.frameRate = i;
            return this;
        }

        public Builder setDisplayFormat(String str) {
            this.displayFormat = str;
            return this;
        }

        public Builder setDisplayFormatHevc(String str) {
            this.displayFormatHevc = str;
            return this;
        }

        public Builder setPacketizationMode(String str) {
            this.packetizationMode = str;
            return this;
        }

        public Builder setH265QvgaPayloadType(int i) {
            this.h265QvgaPayloadType = i;
            return this;
        }

        public Builder setH265QvgaLPayloadType(int i) {
            this.h265QvgaLPayloadType = i;
            return this;
        }

        public Builder setH265VgaPayloadType(int i) {
            this.h265VgaPayloadType = i;
            return this;
        }

        public Builder setH265VgaLPayloadType(int i) {
            this.h265VgaLPayloadType = i;
            return this;
        }

        public Builder setH265Hd720pPayloadType(int i) {
            this.h265Hd720pPayloadType = i;
            return this;
        }

        public Builder setH265Hd720pLPayloadType(int i) {
            this.h265Hd720pLPayloadType = i;
            return this;
        }

        public Builder setH264720pPayloadType(int i) {
            this.h264720pPayloadType = i;
            return this;
        }

        public Builder setH264720pLPayloadType(int i) {
            this.h264720pLPayloadType = i;
            return this;
        }

        public Builder setH264VgaPayloadType(int i) {
            this.h264VgaPayloadType = i;
            return this;
        }

        public Builder setH264VgaLPayloadType(int i) {
            this.h264VgaLPayloadType = i;
            return this;
        }

        public Builder setH264QvgaPayloadType(int i) {
            this.h264QvgaPayloadType = i;
            return this;
        }

        public Builder setH264QvgaLPayloadType(int i) {
            this.h264QvgaLPayloadType = i;
            return this;
        }

        public Builder setH264CifPayloadType(int i) {
            this.h264CifPayloadType = i;
            return this;
        }

        public Builder setH264CifLPayloadType(int i) {
            this.h264CifLPayloadType = i;
            return this;
        }

        public Builder setH263QcifPayloadType(int i) {
            this.h263QcifPayloadType = i;
            return this;
        }

        public Builder setUseSpsForH264Hd(boolean z) {
            this.useSpsForH264Hd = z;
            return this;
        }

        public Builder setVideoAs(int i) {
            this.videoAs = i;
            return this;
        }

        public Builder setVideoRs(int i) {
            this.videoRs = i;
            return this;
        }

        public Builder setVideoRr(int i) {
            this.videoRr = i;
            return this;
        }

        public Builder setAudioAvpf(boolean z) {
            this.audioAvpf = z;
            return this;
        }

        public Builder setAudioSrtp(boolean z) {
            this.audioSrtp = z;
            return this;
        }

        public Builder setVideoAvpf(boolean z) {
            this.videoAvpf = z;
            return this;
        }

        public Builder setVideoSrtp(boolean z) {
            this.videoSrtp = z;
            return this;
        }

        public Builder setTextAvpf(boolean z) {
            this.textAvpf = z;
            return this;
        }

        public Builder setTextSrtp(boolean z) {
            this.textSrtp = z;
            return this;
        }

        public Builder setVideoCapabilities(boolean z) {
            this.videoCapabilities = z;
            return this;
        }

        public Builder setTextAs(int i) {
            this.textAs = i;
            return this;
        }

        public Builder setTextRs(int i) {
            this.textRs = i;
            return this;
        }

        public Builder setTextRr(int i) {
            this.textRr = i;
            return this;
        }

        public Builder setTextPort(int i) {
            this.textPort = i;
            return this;
        }

        public Builder setRtpTimeout(int i) {
            this.rtpTimeout = i;
            return this;
        }

        public Builder setRtcpTimeout(int i) {
            this.rtcpTimeout = i;
            return this;
        }

        public Builder setIgnoreRtcpTimeoutOnHoldCall(boolean z) {
            this.ignoreRtcpTimeoutOnHoldCall = z;
            return this;
        }

        public Builder setEnableRtcpOnActiveCall(boolean z) {
            this.enableRtcpOnActiveCall = z;
            return this;
        }

        public Builder setEnableAvSync(boolean z) {
            this.enableAvSync = z;
            return this;
        }

        public Builder setEnableScr(boolean z) {
            this.enableScr = z;
            return this;
        }

        public Builder setAudioRtcpXr(boolean z) {
            this.audioRtcpXr = z;
            return this;
        }

        public Builder setVideoRtcpXr(boolean z) {
            this.videoRtcpXr = z;
            return this;
        }

        public Builder setDtmfMode(int i) {
            this.dtmfMode = i;
            return this;
        }

        public Builder setEnableEvsCodec(boolean z) {
            this.enableEvsCodec = z;
            return this;
        }

        public Builder setEvsDiscontinuousTransmission(String str) {
            this.evsDiscontinuousTransmission = str;
            return this;
        }

        public Builder setEvsDtxRecv(String str) {
            this.evsDtxRecv = str;
            return this;
        }

        public Builder setEvsHeaderFull(String str) {
            this.evsHeaderFull = str;
            return this;
        }

        public Builder setEvsModeSwitch(String str) {
            this.evsModeSwitch = str;
            return this;
        }

        public Builder setEvsChannelSend(String str) {
            this.evsChannelSend = str;
            return this;
        }

        public Builder setEvsChannelRecv(String str) {
            this.evsChannelRecv = str;
            return this;
        }

        public Builder setEvsChannelAwareReceive(String str) {
            this.evsChannelAwareReceive = str;
            return this;
        }

        public Builder setEvsCodecModeRequest(String str) {
            this.evsCodecModeRequest = str;
            return this;
        }

        public Builder setEvsBitRateSend(String str) {
            this.evsBitRateSend = str;
            return this;
        }

        public Builder setEvsBitRateReceive(String str) {
            this.evsBitRateReceive = str;
            return this;
        }

        public Builder setEvsBandwidthSend(String str) {
            this.evsBandwidthSend = str;
            return this;
        }

        public Builder setEvsBandwidthReceive(String str) {
            this.evsBandwidthReceive = str;
            return this;
        }

        public Builder setEvsPayload(int i) {
            this.evsPayload = i;
            return this;
        }

        public Builder setEvs2ndPayload(int i) {
            this.evs2ndPayload = i;
            return this;
        }

        public Builder setEvsDefaultBandwidth(String str) {
            this.evsDefaultBandwidth = str;
            return this;
        }

        public Builder setEvsDefaultBitrate(String str) {
            this.evsDefaultBitrate = str;
            return this;
        }

        public Builder setEvsPayloadExt(int i) {
            this.evsPayloadExt = i;
            return this;
        }

        public Builder setEvsBitRateSendExt(String str) {
            this.evsBitRateSendExt = str;
            return this;
        }

        public Builder setEvsBitRateReceiveExt(String str) {
            this.evsBitRateReceiveExt = str;
            return this;
        }

        public Builder setEvsBandwidthSendExt(String str) {
            this.evsBandwidthSendExt = str;
            return this;
        }

        public Builder setEvsBandwidthReceiveExt(String str) {
            this.evsBandwidthReceiveExt = str;
            return this;
        }

        public Builder setEvsLimitedCodec(String str) {
            this.evsLimitedCodec = str;
            return this;
        }

        public Builder setEvsUseDefaultRtcpBw(boolean z) {
            this.evsUseDefaultRtcpBw = z;
            return this;
        }
    }
}
