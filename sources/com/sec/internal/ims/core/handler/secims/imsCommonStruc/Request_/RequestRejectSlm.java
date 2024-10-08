package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestRejectSlm extends Table {
    public static RequestRejectSlm getRootAsRequestRejectSlm(ByteBuffer byteBuffer) {
        return getRootAsRequestRejectSlm(byteBuffer, new RequestRejectSlm());
    }

    public static RequestRejectSlm getRootAsRequestRejectSlm(ByteBuffer byteBuffer, RequestRejectSlm requestRejectSlm) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestRejectSlm.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestRejectSlm __assign(int i, ByteBuffer byteBuffer) {
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

    public long sipCode() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public WarningHdr warningHdr() {
        return warningHdr(new WarningHdr());
    }

    public WarningHdr warningHdr(WarningHdr warningHdr) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return warningHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createRequestRejectSlm(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i) {
        flatBufferBuilder.startObject(3);
        addWarningHdr(flatBufferBuilder, i);
        addSipCode(flatBufferBuilder, j2);
        addSessionHandle(flatBufferBuilder, j);
        return endRequestRejectSlm(flatBufferBuilder);
    }

    public static void startRequestRejectSlm(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSipCode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRequestRejectSlm(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
