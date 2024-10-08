package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestCloseImSession extends Table {
    public static RequestCloseImSession getRootAsRequestCloseImSession(ByteBuffer byteBuffer) {
        return getRootAsRequestCloseImSession(byteBuffer, new RequestCloseImSession());
    }

    public static RequestCloseImSession getRootAsRequestCloseImSession(ByteBuffer byteBuffer, RequestCloseImSession requestCloseImSession) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestCloseImSession.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestCloseImSession __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public ReasonHdr reasonHdr() {
        return reasonHdr(new ReasonHdr());
    }

    public ReasonHdr reasonHdr(ReasonHdr reasonHdr) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return reasonHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createRequestCloseImSession(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addReasonHdr(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endRequestCloseImSession(flatBufferBuilder);
    }

    public static void startRequestCloseImSession(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addReasonHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestCloseImSession(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
