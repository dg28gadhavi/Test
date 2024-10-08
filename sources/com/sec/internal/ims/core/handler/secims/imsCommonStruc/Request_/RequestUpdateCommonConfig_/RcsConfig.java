package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RcsConfig extends Table {
    public static RcsConfig getRootAsRcsConfig(ByteBuffer byteBuffer) {
        return getRootAsRcsConfig(byteBuffer, new RcsConfig());
    }

    public static RcsConfig getRootAsRcsConfig(ByteBuffer byteBuffer, RcsConfig rcsConfig) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return rcsConfig.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RcsConfig __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long rcsFtChunkSize() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long rcsIshChunkSize() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String confUri() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer confUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public boolean isMsrpCema() {
        int __offset = __offset(10);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String downloadsPath() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer downloadsPathAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public boolean isConfSubscribeEnabled() {
        int __offset = __offset(14);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String exploderUri() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer exploderUriAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public long pagerModeSizeLimit() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String endUserConfReqId() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer endUserConfReqIdAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public boolean useMsrpDiscardPort() {
        int __offset = __offset(22);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isAggrImdnSupported() {
        int __offset = __offset(24);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isCbPrivacyDisable() {
        int __offset = __offset(26);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int cbMsgTech() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String supportedBotVersions() {
        int __offset = __offset(30);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer supportedBotVersionsAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public int supportCancelMessage() {
        int __offset = __offset(32);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean supportRealtimeUserAlias() {
        int __offset = __offset(34);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRcsConfig(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i, boolean z, int i2, boolean z2, int i3, long j3, int i4, boolean z3, boolean z4, boolean z5, int i5, int i6, int i7, boolean z6) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(16);
        addSupportCancelMessage(flatBufferBuilder, i7);
        addSupportedBotVersions(flatBufferBuilder, i6);
        addCbMsgTech(flatBufferBuilder, i5);
        int i8 = i4;
        addEndUserConfReqId(flatBufferBuilder, i4);
        long j4 = j3;
        addPagerModeSizeLimit(flatBufferBuilder, j3);
        int i9 = i3;
        addExploderUri(flatBufferBuilder, i3);
        int i10 = i2;
        addDownloadsPath(flatBufferBuilder, i2);
        int i11 = i;
        addConfUri(flatBufferBuilder, i);
        long j5 = j2;
        addRcsIshChunkSize(flatBufferBuilder, j2);
        addRcsFtChunkSize(flatBufferBuilder, j);
        addSupportRealtimeUserAlias(flatBufferBuilder, z6);
        addIsCbPrivacyDisable(flatBufferBuilder, z5);
        addIsAggrImdnSupported(flatBufferBuilder, z4);
        addUseMsrpDiscardPort(flatBufferBuilder, z3);
        boolean z7 = z2;
        addIsConfSubscribeEnabled(flatBufferBuilder, z2);
        boolean z8 = z;
        addIsMsrpCema(flatBufferBuilder, z);
        return endRcsConfig(flatBufferBuilder);
    }

    public static void startRcsConfig(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(16);
    }

    public static void addRcsFtChunkSize(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addRcsIshChunkSize(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addConfUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addIsMsrpCema(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(3, z, false);
    }

    public static void addDownloadsPath(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addIsConfSubscribeEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(5, z, false);
    }

    public static void addExploderUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addPagerModeSizeLimit(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(7, (int) j, 0);
    }

    public static void addEndUserConfReqId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addUseMsrpDiscardPort(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(9, z, false);
    }

    public static void addIsAggrImdnSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(10, z, false);
    }

    public static void addIsCbPrivacyDisable(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(11, z, false);
    }

    public static void addCbMsgTech(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(12, i, 0);
    }

    public static void addSupportedBotVersions(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(13, i, 0);
    }

    public static void addSupportCancelMessage(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(14, i, 0);
    }

    public static void addSupportRealtimeUserAlias(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(15, z, false);
    }

    public static int endRcsConfig(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
