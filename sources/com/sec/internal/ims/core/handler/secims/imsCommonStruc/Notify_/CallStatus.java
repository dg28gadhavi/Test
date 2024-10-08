package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CallStatus extends Table {
    public static CallStatus getRootAsCallStatus(ByteBuffer byteBuffer) {
        return getRootAsCallStatus(byteBuffer, new CallStatus());
    }

    public static CallStatus getRootAsCallStatus(ByteBuffer byteBuffer, CallStatus callStatus) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return callStatus.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public CallStatus __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long handle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long session() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int callType() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int state() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public long statusCode() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String reasonPhrase() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reasonPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public boolean remoteVideoCapa() {
        int __offset = __offset(16);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String audioCodecName() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer audioCodecNameAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public AdditionalContents additionalContents() {
        return additionalContents(new AdditionalContents());
    }

    public AdditionalContents additionalContents(AdditionalContents additionalContents) {
        int __offset = __offset(20);
        if (__offset != 0) {
            return additionalContents.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public long width() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long height() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String conferenceSupport() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer conferenceSupportAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public boolean isFocus() {
        int __offset = __offset(28);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean remoteMmtelCapa() {
        int __offset = __offset(30);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long localVideoRtpPort() {
        int __offset = __offset(32);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long localVideoRtcpPort() {
        int __offset = __offset(34);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long remoteVideoRtpPort() {
        int __offset = __offset(36);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long remoteVideoRtcpPort() {
        int __offset = __offset(38);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String serviceUrn() {
        int __offset = __offset(40);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer serviceUrnAsByteBuffer() {
        return __vector_as_bytebuffer(40, 1);
    }

    public long retryAfter() {
        int __offset = __offset(42);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean localHoldTone() {
        int __offset = __offset(44);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String historyInfo() {
        int __offset = __offset(46);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer historyInfoAsByteBuffer() {
        return __vector_as_bytebuffer(46, 1);
    }

    public String dtmfEvent() {
        int __offset = __offset(48);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer dtmfEventAsByteBuffer() {
        return __vector_as_bytebuffer(48, 1);
    }

    public boolean cvoEnabled() {
        int __offset = __offset(50);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String alertInfo() {
        int __offset = __offset(52);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer alertInfoAsByteBuffer() {
        return __vector_as_bytebuffer(52, 1);
    }

    public String cmcDeviceId() {
        int __offset = __offset(54);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cmcDeviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(54, 1);
    }

    public long videoCrbtType() {
        int __offset = __offset(56);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long audioRxTrackId() {
        int __offset = __offset(58);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String audioBitRate() {
        int __offset = __offset(60);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer audioBitRateAsByteBuffer() {
        return __vector_as_bytebuffer(60, 1);
    }

    public String cmcCallTime() {
        int __offset = __offset(62);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cmcCallTimeAsByteBuffer() {
        return __vector_as_bytebuffer(62, 1);
    }

    public String featureCaps() {
        int __offset = __offset(64);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer featureCapsAsByteBuffer() {
        return __vector_as_bytebuffer(64, 1);
    }

    public long audioEarlyMediaDir() {
        int __offset = __offset(66);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean delayRinging() {
        int __offset = __offset(68);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String sipCallId() {
        int __offset = __offset(70);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sipCallIdAsByteBuffer() {
        return __vector_as_bytebuffer(70, 1);
    }

    public boolean touchScreenEnabled() {
        int __offset = __offset(72);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String idcExtra() {
        int __offset = __offset(74);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idcExtraAsByteBuffer() {
        return __vector_as_bytebuffer(74, 1);
    }

    public static int createCallStatus(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i, int i2, long j3, int i3, boolean z, int i4, int i5, long j4, long j5, int i6, boolean z2, boolean z3, long j6, long j7, long j8, long j9, int i7, long j10, boolean z4, int i8, int i9, boolean z5, int i10, int i11, long j11, long j12, int i12, int i13, int i14, long j13, boolean z6, int i15, boolean z7, int i16) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(36);
        addIdcExtra(flatBufferBuilder, i16);
        addSipCallId(flatBufferBuilder, i15);
        addAudioEarlyMediaDir(flatBufferBuilder, j13);
        addFeatureCaps(flatBufferBuilder, i14);
        addCmcCallTime(flatBufferBuilder, i13);
        addAudioBitRate(flatBufferBuilder, i12);
        addAudioRxTrackId(flatBufferBuilder, j12);
        addVideoCrbtType(flatBufferBuilder, j11);
        addCmcDeviceId(flatBufferBuilder, i11);
        addAlertInfo(flatBufferBuilder, i10);
        addDtmfEvent(flatBufferBuilder, i9);
        addHistoryInfo(flatBufferBuilder, i8);
        addRetryAfter(flatBufferBuilder, j10);
        addServiceUrn(flatBufferBuilder, i7);
        addRemoteVideoRtcpPort(flatBufferBuilder, j9);
        addRemoteVideoRtpPort(flatBufferBuilder, j8);
        addLocalVideoRtcpPort(flatBufferBuilder, j7);
        addLocalVideoRtpPort(flatBufferBuilder, j6);
        addConferenceSupport(flatBufferBuilder, i6);
        addHeight(flatBufferBuilder, j5);
        addWidth(flatBufferBuilder, j4);
        int i17 = i5;
        addAdditionalContents(flatBufferBuilder, i5);
        int i18 = i4;
        addAudioCodecName(flatBufferBuilder, i4);
        int i19 = i3;
        addReasonPhrase(flatBufferBuilder, i3);
        long j14 = j3;
        addStatusCode(flatBufferBuilder, j3);
        int i20 = i2;
        addState(flatBufferBuilder, i2);
        int i21 = i;
        addCallType(flatBufferBuilder, i);
        long j15 = j2;
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        addTouchScreenEnabled(flatBufferBuilder, z7);
        addDelayRinging(flatBufferBuilder, z6);
        addCvoEnabled(flatBufferBuilder, z5);
        addLocalHoldTone(flatBufferBuilder, z4);
        addRemoteMmtelCapa(flatBufferBuilder, z3);
        addIsFocus(flatBufferBuilder, z2);
        boolean z8 = z;
        addRemoteVideoCapa(flatBufferBuilder, z);
        return endCallStatus(flatBufferBuilder);
    }

    public static void startCallStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(36);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addCallType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addState(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static void addStatusCode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(4, (int) j, 0);
    }

    public static void addReasonPhrase(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addRemoteVideoCapa(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(6, z, false);
    }

    public static void addAudioCodecName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addWidth(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(9, (int) j, 0);
    }

    public static void addHeight(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(10, (int) j, 0);
    }

    public static void addConferenceSupport(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static void addIsFocus(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(12, z, false);
    }

    public static void addRemoteMmtelCapa(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(13, z, false);
    }

    public static void addLocalVideoRtpPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(14, (int) j, 0);
    }

    public static void addLocalVideoRtcpPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(15, (int) j, 0);
    }

    public static void addRemoteVideoRtpPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(16, (int) j, 0);
    }

    public static void addRemoteVideoRtcpPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(17, (int) j, 0);
    }

    public static void addServiceUrn(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(18, i, 0);
    }

    public static void addRetryAfter(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(19, (int) j, 0);
    }

    public static void addLocalHoldTone(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(20, z, false);
    }

    public static void addHistoryInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(21, i, 0);
    }

    public static void addDtmfEvent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(22, i, 0);
    }

    public static void addCvoEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(23, z, false);
    }

    public static void addAlertInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(24, i, 0);
    }

    public static void addCmcDeviceId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(25, i, 0);
    }

    public static void addVideoCrbtType(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(26, (int) j, 0);
    }

    public static void addAudioRxTrackId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(27, (int) j, 0);
    }

    public static void addAudioBitRate(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(28, i, 0);
    }

    public static void addCmcCallTime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(29, i, 0);
    }

    public static void addFeatureCaps(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(30, i, 0);
    }

    public static void addAudioEarlyMediaDir(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(31, (int) j, 0);
    }

    public static void addDelayRinging(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(32, z, false);
    }

    public static void addSipCallId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(33, i, 0);
    }

    public static void addTouchScreenEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(34, z, false);
    }

    public static void addIdcExtra(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(35, i, 0);
    }

    public static int endCallStatus(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
