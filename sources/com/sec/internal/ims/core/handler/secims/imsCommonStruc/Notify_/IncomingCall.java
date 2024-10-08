package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IncomingCall extends Table {
    public static IncomingCall getRootAsIncomingCall(ByteBuffer byteBuffer) {
        return getRootAsIncomingCall(byteBuffer, new IncomingCall());
    }

    public static IncomingCall getRootAsIncomingCall(ByteBuffer byteBuffer, IncomingCall incomingCall) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return incomingCall.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public IncomingCall __assign(int i, ByteBuffer byteBuffer) {
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

    public String peeruri() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer peeruriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String displayName() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String referredBy() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer referredByAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public long replacingSession() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String sipCallId() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sipCallIdAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String terminatingId() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer terminatingIdAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String numberPlus() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer numberPlusAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String rawSipmsg() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer rawSipmsgAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String replaces() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer replacesAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String alertInfo() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer alertInfoAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String photoRing() {
        int __offset = __offset(30);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer photoRingAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public String historyInfo() {
        int __offset = __offset(32);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer historyInfoAsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public boolean cvoEnabled() {
        int __offset = __offset(34);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String verstat() {
        int __offset = __offset(36);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer verstatAsByteBuffer() {
        return __vector_as_bytebuffer(36, 1);
    }

    public String organization() {
        int __offset = __offset(38);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer organizationAsByteBuffer() {
        return __vector_as_bytebuffer(38, 1);
    }

    public String cmcDeviceId() {
        int __offset = __offset(40);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cmcDeviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(40, 1);
    }

    public ComposerData composerData() {
        return composerData(new ComposerData());
    }

    public ComposerData composerData(ComposerData composerData) {
        int __offset = __offset(42);
        if (__offset != 0) {
            return composerData.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public boolean hasDiversion() {
        int __offset = __offset(44);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long cmcEdCallSlot() {
        int __offset = __offset(46);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String idcExtra() {
        int __offset = __offset(48);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idcExtraAsByteBuffer() {
        return __vector_as_bytebuffer(48, 1);
    }

    public static int createIncomingCall(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i, int i2, int i3, int i4, long j3, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, boolean z, int i13, int i14, int i15, int i16, boolean z2, long j4, int i17) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(23);
        addIdcExtra(flatBufferBuilder, i17);
        addCmcEdCallSlot(flatBufferBuilder, j4);
        addComposerData(flatBufferBuilder, i16);
        addCmcDeviceId(flatBufferBuilder, i15);
        addOrganization(flatBufferBuilder, i14);
        addVerstat(flatBufferBuilder, i13);
        addHistoryInfo(flatBufferBuilder, i12);
        addPhotoRing(flatBufferBuilder, i11);
        addAlertInfo(flatBufferBuilder, i10);
        addReplaces(flatBufferBuilder, i9);
        addRawSipmsg(flatBufferBuilder, i8);
        addNumberPlus(flatBufferBuilder, i7);
        int i18 = i6;
        addTerminatingId(flatBufferBuilder, i6);
        int i19 = i5;
        addSipCallId(flatBufferBuilder, i5);
        long j5 = j3;
        addReplacingSession(flatBufferBuilder, j3);
        int i20 = i4;
        addReferredBy(flatBufferBuilder, i4);
        int i21 = i3;
        addDisplayName(flatBufferBuilder, i3);
        int i22 = i2;
        addPeeruri(flatBufferBuilder, i2);
        int i23 = i;
        addCallType(flatBufferBuilder, i);
        long j6 = j2;
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        addHasDiversion(flatBufferBuilder, z2);
        addCvoEnabled(flatBufferBuilder, z);
        return endIncomingCall(flatBufferBuilder);
    }

    public static void startIncomingCall(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(23);
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

    public static void addPeeruri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addDisplayName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addReferredBy(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addReplacingSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(6, (int) j, 0);
    }

    public static void addSipCallId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addTerminatingId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addNumberPlus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static void addRawSipmsg(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addReplaces(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static void addAlertInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(12, i, 0);
    }

    public static void addPhotoRing(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(13, i, 0);
    }

    public static void addHistoryInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(14, i, 0);
    }

    public static void addCvoEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(15, z, false);
    }

    public static void addVerstat(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(16, i, 0);
    }

    public static void addOrganization(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(17, i, 0);
    }

    public static void addCmcDeviceId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(18, i, 0);
    }

    public static void addComposerData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(19, i, 0);
    }

    public static void addHasDiversion(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(20, z, false);
    }

    public static void addCmcEdCallSlot(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(21, (int) j, 0);
    }

    public static void addIdcExtra(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(22, i, 0);
    }

    public static int endIncomingCall(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
