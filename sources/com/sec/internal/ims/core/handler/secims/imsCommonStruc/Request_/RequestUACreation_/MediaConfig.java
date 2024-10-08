package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUACreation_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class MediaConfig extends Table {
    public static MediaConfig getRootAsMediaConfig(ByteBuffer byteBuffer) {
        return getRootAsMediaConfig(byteBuffer, new MediaConfig());
    }

    public static MediaConfig getRootAsMediaConfig(ByteBuffer byteBuffer, MediaConfig mediaConfig) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return mediaConfig.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public MediaConfig __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long audioPort() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long audioDscp() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isAmrOctecAlign() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String audioCodec() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer audioCodecAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public long dtmfMode() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String audioAs() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer audioAsAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String audioRs() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer audioRsAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String audioRr() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer audioRrAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String amrModeChangeCapability() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer amrModeChangeCapabilityAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public long amrMaxRed() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String amrMode() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer amrModeAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String amrWbMode() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer amrWbModeAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public long amrPayload() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long amrbePayload() {
        int __offset = __offset(30);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long amrWbPayload() {
        int __offset = __offset(32);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long amrbeWbPayload() {
        int __offset = __offset(34);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long dtmfPayload() {
        int __offset = __offset(36);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long dtmfWbPayload() {
        int __offset = __offset(38);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long pTime() {
        int __offset = __offset(40);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long maxTime() {
        int __offset = __offset(42);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String videoCodec() {
        int __offset = __offset(44);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer videoCodecAsByteBuffer() {
        return __vector_as_bytebuffer(44, 1);
    }

    public long videoPort() {
        int __offset = __offset(46);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long frameRate() {
        int __offset = __offset(48);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String displayFormat() {
        int __offset = __offset(50);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer displayFormatAsByteBuffer() {
        return __vector_as_bytebuffer(50, 1);
    }

    public String displayFormatHevc() {
        int __offset = __offset(52);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer displayFormatHevcAsByteBuffer() {
        return __vector_as_bytebuffer(52, 1);
    }

    public String packetizationMode() {
        int __offset = __offset(54);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer packetizationModeAsByteBuffer() {
        return __vector_as_bytebuffer(54, 1);
    }

    public long h265Hd720pPayload() {
        int __offset = __offset(56);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h265Hd720plPayload() {
        int __offset = __offset(58);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h265VgaPayload() {
        int __offset = __offset(60);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h265VgalPayload() {
        int __offset = __offset(62);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h265QvgaPayload() {
        int __offset = __offset(64);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h265QvgalPayload() {
        int __offset = __offset(66);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h264720pPayload() {
        int __offset = __offset(68);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h264720plPayload() {
        int __offset = __offset(70);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h264VgaPayload() {
        int __offset = __offset(72);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h264VgalPayload() {
        int __offset = __offset(74);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h264QvgaPayload() {
        int __offset = __offset(76);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h264QvgalPayload() {
        int __offset = __offset(78);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h264CifPayload() {
        int __offset = __offset(80);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h264CiflPayload() {
        int __offset = __offset(82);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long h263QcifPayload() {
        int __offset = __offset(84);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean useSpsForH264Hd() {
        int __offset = __offset(86);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long videoAs() {
        int __offset = __offset(88);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long videoRs() {
        int __offset = __offset(90);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long videoRr() {
        int __offset = __offset(92);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long textAs() {
        int __offset = __offset(94);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long textRs() {
        int __offset = __offset(96);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long textRr() {
        int __offset = __offset(98);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long textPort() {
        int __offset = __offset(100);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int localSendStrengthtag() {
        int __offset = __offset(102);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int localReceivedStrengthtag() {
        int __offset = __offset(104);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int remoteSendStrengthtag() {
        int __offset = __offset(106);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int remoteReceivedStrengthtag() {
        int __offset = __offset(108);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean audioAvpf() {
        int __offset = __offset(110);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean audioSrtp() {
        int __offset = __offset(112);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean videoAvpf() {
        int __offset = __offset(114);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean videoSrtp() {
        int __offset = __offset(116);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean textAvpf() {
        int __offset = __offset(118);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean textSrtp() {
        int __offset = __offset(120);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean videoCapabilities() {
        int __offset = __offset(122);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean enableScr() {
        int __offset = __offset(124);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long rtpTimeout() {
        int __offset = __offset(126);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long rtcpTimeout() {
        int __offset = __offset(128);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean audioRtcpXr() {
        int __offset = __offset(130);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean videoRtcpXr() {
        int __offset = __offset(132);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean enableEvsCodec() {
        int __offset = __offset(134);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String evsDiscontinuousTransmission() {
        int __offset = __offset(136);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsDiscontinuousTransmissionAsByteBuffer() {
        return __vector_as_bytebuffer(136, 1);
    }

    public String evsDtxRecv() {
        int __offset = __offset(138);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsDtxRecvAsByteBuffer() {
        return __vector_as_bytebuffer(138, 1);
    }

    public String evsHeaderFull() {
        int __offset = __offset(140);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsHeaderFullAsByteBuffer() {
        return __vector_as_bytebuffer(140, 1);
    }

    public String evsModeSwitch() {
        int __offset = __offset(142);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsModeSwitchAsByteBuffer() {
        return __vector_as_bytebuffer(142, 1);
    }

    public String evsChannelSend() {
        int __offset = __offset(144);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsChannelSendAsByteBuffer() {
        return __vector_as_bytebuffer(144, 1);
    }

    public String evsChannelRecv() {
        int __offset = __offset(146);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsChannelRecvAsByteBuffer() {
        return __vector_as_bytebuffer(146, 1);
    }

    public String evsChannelAwareReceive() {
        int __offset = __offset(148);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsChannelAwareReceiveAsByteBuffer() {
        return __vector_as_bytebuffer(148, 1);
    }

    public String evsCodecModeRequest() {
        int __offset = __offset(150);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsCodecModeRequestAsByteBuffer() {
        return __vector_as_bytebuffer(150, 1);
    }

    public String evsBitRateSend() {
        int __offset = __offset(152);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsBitRateSendAsByteBuffer() {
        return __vector_as_bytebuffer(152, 1);
    }

    public String evsBitRateReceive() {
        int __offset = __offset(154);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsBitRateReceiveAsByteBuffer() {
        return __vector_as_bytebuffer(154, 1);
    }

    public String evsBandwidthSend() {
        int __offset = __offset(MNO.TANGO_LUXEMBOURG);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsBandwidthSendAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.TANGO_LUXEMBOURG, 1);
    }

    public String evsBandwidthReceive() {
        int __offset = __offset(MNO.STC_KSA);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsBandwidthReceiveAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.STC_KSA, 1);
    }

    public long evsPayload() {
        int __offset = __offset(MNO.UMOBILE);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String evsDefaultBandwidth() {
        int __offset = __offset(MNO.TMOBILE_ROMANIA);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsDefaultBandwidthAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.TMOBILE_ROMANIA, 1);
    }

    public String evsDefaultBitrate() {
        int __offset = __offset(MNO.CLARO_COLOMBIA);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsDefaultBitrateAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.CLARO_COLOMBIA, 1);
    }

    public boolean enableRtcpOnActiveCall() {
        int __offset = __offset(MNO.TELENOR_BG);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long amrOpenPayload() {
        int __offset = __offset(MNO.TELIA_FI);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean enableAvSync() {
        int __offset = __offset(MNO.ALTAN_MEXICO);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean ignoreRtcpTimeoutOnHoldCall() {
        int __offset = __offset(MNO.TIGO_PANAMA);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long amrbeMaxRed() {
        int __offset = __offset(MNO.VODAFONE_ROMANIA);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long amrWbMaxRed() {
        int __offset = __offset(MNO.ORANGE_SENEGAL);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long amrbeWbMaxRed() {
        int __offset = __offset(MNO.MAGTICOM_GE);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long evsMaxRed() {
        int __offset = __offset(MNO.EVR_ESN);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long evs2ndPayload() {
        int __offset = __offset(MNO.TPG_SG);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long evsPayloadExt() {
        int __offset = __offset(MNO.WOM_CHILE);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String evsBitRateSendExt() {
        int __offset = __offset(MNO.MTN_IRAN);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsBitRateSendExtAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.MTN_IRAN, 1);
    }

    public String evsBitRateReceiveExt() {
        int __offset = __offset(MNO.CLARO_URUGUAY);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsBitRateReceiveExtAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.CLARO_URUGUAY, 1);
    }

    public String evsBandwidthSendExt() {
        int __offset = __offset(MNO.MTN_GHANA);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsBandwidthSendExtAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.MTN_GHANA, 1);
    }

    public String evsBandwidthReceiveExt() {
        int __offset = __offset(MNO.TELEFONICA_SPAIN);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsBandwidthReceiveExtAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.TELEFONICA_SPAIN, 1);
    }

    public String evsLimitedCodec() {
        int __offset = __offset(MNO.KOODO);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer evsLimitedCodecAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.KOODO, 1);
    }

    public boolean evsUseDefaultRtcpBw() {
        int __offset = __offset(MNO.BATELCO_BAHRAIN);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createMediaConfig(FlatBufferBuilder flatBufferBuilder, long j, long j2, boolean z, int i, long j3, int i2, int i3, int i4, int i5, long j4, int i6, int i7, long j5, long j6, long j7, long j8, long j9, long j10, long j11, long j12, int i8, long j13, long j14, int i9, int i10, int i11, long j15, long j16, long j17, long j18, long j19, long j20, long j21, long j22, long j23, long j24, long j25, long j26, long j27, long j28, long j29, boolean z2, long j30, long j31, long j32, long j33, long j34, long j35, long j36, int i12, int i13, int i14, int i15, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8, boolean z9, boolean z10, long j37, long j38, boolean z11, boolean z12, boolean z13, int i16, int i17, int i18, int i19, int i20, int i21, int i22, int i23, int i24, int i25, int i26, int i27, long j39, int i28, int i29, boolean z14, long j40, boolean z15, boolean z16, long j41, long j42, long j43, long j44, long j45, long j46, int i30, int i31, int i32, int i33, int i34, boolean z17) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(97);
        addEvsLimitedCodec(flatBufferBuilder, i34);
        addEvsBandwidthReceiveExt(flatBufferBuilder, i33);
        addEvsBandwidthSendExt(flatBufferBuilder, i32);
        addEvsBitRateReceiveExt(flatBufferBuilder, i31);
        addEvsBitRateSendExt(flatBufferBuilder, i30);
        addEvsPayloadExt(flatBufferBuilder, j46);
        addEvs2ndPayload(flatBufferBuilder, j45);
        addEvsMaxRed(flatBufferBuilder, j44);
        addAmrbeWbMaxRed(flatBufferBuilder, j43);
        addAmrWbMaxRed(flatBufferBuilder, j42);
        addAmrbeMaxRed(flatBufferBuilder, j41);
        addAmrOpenPayload(flatBufferBuilder, j40);
        addEvsDefaultBitrate(flatBufferBuilder, i29);
        addEvsDefaultBandwidth(flatBufferBuilder, i28);
        addEvsPayload(flatBufferBuilder, j39);
        addEvsBandwidthReceive(flatBufferBuilder, i27);
        addEvsBandwidthSend(flatBufferBuilder, i26);
        addEvsBitRateReceive(flatBufferBuilder, i25);
        addEvsBitRateSend(flatBufferBuilder, i24);
        addEvsCodecModeRequest(flatBufferBuilder, i23);
        addEvsChannelAwareReceive(flatBufferBuilder, i22);
        addEvsChannelRecv(flatBufferBuilder, i21);
        addEvsChannelSend(flatBufferBuilder, i20);
        addEvsModeSwitch(flatBufferBuilder, i19);
        addEvsHeaderFull(flatBufferBuilder, i18);
        addEvsDtxRecv(flatBufferBuilder, i17);
        addEvsDiscontinuousTransmission(flatBufferBuilder, i16);
        addRtcpTimeout(flatBufferBuilder, j38);
        addRtpTimeout(flatBufferBuilder, j37);
        addRemoteReceivedStrengthtag(flatBufferBuilder, i15);
        addRemoteSendStrengthtag(flatBufferBuilder, i14);
        addLocalReceivedStrengthtag(flatBufferBuilder, i13);
        addLocalSendStrengthtag(flatBufferBuilder, i12);
        addTextPort(flatBufferBuilder, j36);
        addTextRr(flatBufferBuilder, j35);
        addTextRs(flatBufferBuilder, j34);
        addTextAs(flatBufferBuilder, j33);
        addVideoRr(flatBufferBuilder, j32);
        addVideoRs(flatBufferBuilder, j31);
        addVideoAs(flatBufferBuilder, j30);
        addH263QcifPayload(flatBufferBuilder, j29);
        addH264CiflPayload(flatBufferBuilder, j28);
        addH264CifPayload(flatBufferBuilder, j27);
        addH264QvgalPayload(flatBufferBuilder, j26);
        addH264QvgaPayload(flatBufferBuilder, j25);
        addH264VgalPayload(flatBufferBuilder, j24);
        addH264VgaPayload(flatBufferBuilder, j23);
        addH264720plPayload(flatBufferBuilder, j22);
        addH264720pPayload(flatBufferBuilder, j21);
        addH265QvgalPayload(flatBufferBuilder, j20);
        addH265QvgaPayload(flatBufferBuilder, j19);
        addH265VgalPayload(flatBufferBuilder, j18);
        addH265VgaPayload(flatBufferBuilder, j17);
        addH265Hd720plPayload(flatBufferBuilder, j16);
        addH265Hd720pPayload(flatBufferBuilder, j15);
        addPacketizationMode(flatBufferBuilder, i11);
        addDisplayFormatHevc(flatBufferBuilder, i10);
        addDisplayFormat(flatBufferBuilder, i9);
        addFrameRate(flatBufferBuilder, j14);
        addVideoPort(flatBufferBuilder, j13);
        addVideoCodec(flatBufferBuilder, i8);
        addMaxTime(flatBufferBuilder, j12);
        addPTime(flatBufferBuilder, j11);
        addDtmfWbPayload(flatBufferBuilder, j10);
        addDtmfPayload(flatBufferBuilder, j9);
        addAmrbeWbPayload(flatBufferBuilder, j8);
        addAmrWbPayload(flatBufferBuilder, j7);
        addAmrbePayload(flatBufferBuilder, j6);
        addAmrPayload(flatBufferBuilder, j5);
        addAmrWbMode(flatBufferBuilder, i7);
        addAmrMode(flatBufferBuilder, i6);
        addAmrMaxRed(flatBufferBuilder, j4);
        int i35 = i5;
        addAmrModeChangeCapability(flatBufferBuilder, i5);
        int i36 = i4;
        addAudioRr(flatBufferBuilder, i4);
        int i37 = i3;
        addAudioRs(flatBufferBuilder, i3);
        int i38 = i2;
        addAudioAs(flatBufferBuilder, i2);
        long j47 = j3;
        addDtmfMode(flatBufferBuilder, j3);
        int i39 = i;
        addAudioCodec(flatBufferBuilder, i);
        long j48 = j2;
        addAudioDscp(flatBufferBuilder, j2);
        addAudioPort(flatBufferBuilder, j);
        addEvsUseDefaultRtcpBw(flatBufferBuilder, z17);
        addIgnoreRtcpTimeoutOnHoldCall(flatBufferBuilder, z16);
        addEnableAvSync(flatBufferBuilder, z15);
        addEnableRtcpOnActiveCall(flatBufferBuilder, z14);
        addEnableEvsCodec(flatBufferBuilder, z13);
        addVideoRtcpXr(flatBufferBuilder, z12);
        addAudioRtcpXr(flatBufferBuilder, z11);
        addEnableScr(flatBufferBuilder, z10);
        addVideoCapabilities(flatBufferBuilder, z9);
        addTextSrtp(flatBufferBuilder, z8);
        addTextAvpf(flatBufferBuilder, z7);
        addVideoSrtp(flatBufferBuilder, z6);
        addVideoAvpf(flatBufferBuilder, z5);
        addAudioSrtp(flatBufferBuilder, z4);
        addAudioAvpf(flatBufferBuilder, z3);
        addUseSpsForH264Hd(flatBufferBuilder, z2);
        boolean z18 = z;
        addIsAmrOctecAlign(flatBufferBuilder, z);
        return endMediaConfig(flatBufferBuilder);
    }

    public static void startMediaConfig(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(97);
    }

    public static void addAudioPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addAudioDscp(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addIsAmrOctecAlign(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static void addAudioCodec(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addDtmfMode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(4, (int) j, 0);
    }

    public static void addAudioAs(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addAudioRs(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addAudioRr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addAmrModeChangeCapability(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addAmrMaxRed(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(9, (int) j, 0);
    }

    public static void addAmrMode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addAmrWbMode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static void addAmrPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(12, (int) j, 0);
    }

    public static void addAmrbePayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(13, (int) j, 0);
    }

    public static void addAmrWbPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(14, (int) j, 0);
    }

    public static void addAmrbeWbPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(15, (int) j, 0);
    }

    public static void addDtmfPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(16, (int) j, 0);
    }

    public static void addDtmfWbPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(17, (int) j, 0);
    }

    public static void addPTime(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(18, (int) j, 0);
    }

    public static void addMaxTime(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(19, (int) j, 0);
    }

    public static void addVideoCodec(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(20, i, 0);
    }

    public static void addVideoPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(21, (int) j, 0);
    }

    public static void addFrameRate(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(22, (int) j, 0);
    }

    public static void addDisplayFormat(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(23, i, 0);
    }

    public static void addDisplayFormatHevc(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(24, i, 0);
    }

    public static void addPacketizationMode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(25, i, 0);
    }

    public static void addH265Hd720pPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(26, (int) j, 0);
    }

    public static void addH265Hd720plPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(27, (int) j, 0);
    }

    public static void addH265VgaPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(28, (int) j, 0);
    }

    public static void addH265VgalPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(29, (int) j, 0);
    }

    public static void addH265QvgaPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(30, (int) j, 0);
    }

    public static void addH265QvgalPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(31, (int) j, 0);
    }

    public static void addH264720pPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(32, (int) j, 0);
    }

    public static void addH264720plPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(33, (int) j, 0);
    }

    public static void addH264VgaPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(34, (int) j, 0);
    }

    public static void addH264VgalPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(35, (int) j, 0);
    }

    public static void addH264QvgaPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(36, (int) j, 0);
    }

    public static void addH264QvgalPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(37, (int) j, 0);
    }

    public static void addH264CifPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(38, (int) j, 0);
    }

    public static void addH264CiflPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(39, (int) j, 0);
    }

    public static void addH263QcifPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(40, (int) j, 0);
    }

    public static void addUseSpsForH264Hd(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(41, z, false);
    }

    public static void addVideoAs(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(42, (int) j, 0);
    }

    public static void addVideoRs(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(43, (int) j, 0);
    }

    public static void addVideoRr(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(44, (int) j, 0);
    }

    public static void addTextAs(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(45, (int) j, 0);
    }

    public static void addTextRs(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(46, (int) j, 0);
    }

    public static void addTextRr(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(47, (int) j, 0);
    }

    public static void addTextPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(48, (int) j, 0);
    }

    public static void addLocalSendStrengthtag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(49, i, 0);
    }

    public static void addLocalReceivedStrengthtag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(50, i, 0);
    }

    public static void addRemoteSendStrengthtag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(51, i, 0);
    }

    public static void addRemoteReceivedStrengthtag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(52, i, 0);
    }

    public static void addAudioAvpf(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(53, z, false);
    }

    public static void addAudioSrtp(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(54, z, false);
    }

    public static void addVideoAvpf(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(55, z, false);
    }

    public static void addVideoSrtp(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(56, z, false);
    }

    public static void addTextAvpf(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(57, z, false);
    }

    public static void addTextSrtp(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(58, z, false);
    }

    public static void addVideoCapabilities(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(59, z, false);
    }

    public static void addEnableScr(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(60, z, false);
    }

    public static void addRtpTimeout(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(61, (int) j, 0);
    }

    public static void addRtcpTimeout(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(62, (int) j, 0);
    }

    public static void addAudioRtcpXr(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(63, z, false);
    }

    public static void addVideoRtcpXr(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(64, z, false);
    }

    public static void addEnableEvsCodec(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(65, z, false);
    }

    public static void addEvsDiscontinuousTransmission(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(66, i, 0);
    }

    public static void addEvsDtxRecv(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(67, i, 0);
    }

    public static void addEvsHeaderFull(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(68, i, 0);
    }

    public static void addEvsModeSwitch(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(69, i, 0);
    }

    public static void addEvsChannelSend(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(70, i, 0);
    }

    public static void addEvsChannelRecv(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(71, i, 0);
    }

    public static void addEvsChannelAwareReceive(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(72, i, 0);
    }

    public static void addEvsCodecModeRequest(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(73, i, 0);
    }

    public static void addEvsBitRateSend(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(74, i, 0);
    }

    public static void addEvsBitRateReceive(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(75, i, 0);
    }

    public static void addEvsBandwidthSend(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(76, i, 0);
    }

    public static void addEvsBandwidthReceive(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(77, i, 0);
    }

    public static void addEvsPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(78, (int) j, 0);
    }

    public static void addEvsDefaultBandwidth(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(79, i, 0);
    }

    public static void addEvsDefaultBitrate(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(80, i, 0);
    }

    public static void addEnableRtcpOnActiveCall(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(81, z, false);
    }

    public static void addAmrOpenPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(82, (int) j, 0);
    }

    public static void addEnableAvSync(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(83, z, false);
    }

    public static void addIgnoreRtcpTimeoutOnHoldCall(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(84, z, false);
    }

    public static void addAmrbeMaxRed(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(85, (int) j, 0);
    }

    public static void addAmrWbMaxRed(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(86, (int) j, 0);
    }

    public static void addAmrbeWbMaxRed(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(87, (int) j, 0);
    }

    public static void addEvsMaxRed(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(88, (int) j, 0);
    }

    public static void addEvs2ndPayload(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(89, (int) j, 0);
    }

    public static void addEvsPayloadExt(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(90, (int) j, 0);
    }

    public static void addEvsBitRateSendExt(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(91, i, 0);
    }

    public static void addEvsBitRateReceiveExt(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(92, i, 0);
    }

    public static void addEvsBandwidthSendExt(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(93, i, 0);
    }

    public static void addEvsBandwidthReceiveExt(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(94, i, 0);
    }

    public static void addEvsLimitedCodec(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(95, i, 0);
    }

    public static void addEvsUseDefaultRtcpBw(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(96, z, false);
    }

    public static int endMediaConfig(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
