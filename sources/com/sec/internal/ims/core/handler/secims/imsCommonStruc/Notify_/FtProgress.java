package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class FtProgress extends Table {
    public static FtProgress getRootAsFtProgress(ByteBuffer byteBuffer) {
        return getRootAsFtProgress(byteBuffer, new FtProgress());
    }

    public static FtProgress getRootAsFtProgress(ByteBuffer byteBuffer, FtProgress ftProgress) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return ftProgress.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public FtProgress __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionHandle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long total() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long transferred() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long state() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError imError) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return imError.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public WarningHdr warningHdr() {
        return warningHdr(new WarningHdr());
    }

    public WarningHdr warningHdr(WarningHdr warningHdr) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return warningHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public ReasonHdr reasonHdr() {
        return reasonHdr(new ReasonHdr());
    }

    public ReasonHdr reasonHdr(ReasonHdr reasonHdr) {
        int __offset = __offset(16);
        if (__offset != 0) {
            return reasonHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createFtProgress(FlatBufferBuilder flatBufferBuilder, long j, long j2, long j3, long j4, int i, int i2, int i3) {
        flatBufferBuilder.startObject(7);
        addReasonHdr(flatBufferBuilder, i3);
        addWarningHdr(flatBufferBuilder, i2);
        addImError(flatBufferBuilder, i);
        addState(flatBufferBuilder, j4);
        addTransferred(flatBufferBuilder, j3);
        addTotal(flatBufferBuilder, j2);
        addSessionHandle(flatBufferBuilder, j);
        return endFtProgress(flatBufferBuilder);
    }

    public static void startFtProgress(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addTotal(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addTransferred(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addState(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addReasonHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int endFtProgress(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 12);
        return endObject;
    }
}
