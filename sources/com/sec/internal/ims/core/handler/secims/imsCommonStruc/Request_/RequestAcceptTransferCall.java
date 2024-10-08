package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestAcceptTransferCall extends Table {
    public static RequestAcceptTransferCall getRootAsRequestAcceptTransferCall(ByteBuffer byteBuffer) {
        return getRootAsRequestAcceptTransferCall(byteBuffer, new RequestAcceptTransferCall());
    }

    public static RequestAcceptTransferCall getRootAsRequestAcceptTransferCall(ByteBuffer byteBuffer, RequestAcceptTransferCall requestAcceptTransferCall) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestAcceptTransferCall.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestAcceptTransferCall __assign(int i, ByteBuffer byteBuffer) {
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

    public boolean accept() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long statusCode() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String reasonPhrase() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reasonPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestAcceptTransferCall(FlatBufferBuilder flatBufferBuilder, long j, long j2, boolean z, long j3, int i) {
        flatBufferBuilder.startObject(5);
        addReasonPhrase(flatBufferBuilder, i);
        addStatusCode(flatBufferBuilder, j3);
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        addAccept(flatBufferBuilder, z);
        return endRequestAcceptTransferCall(flatBufferBuilder);
    }

    public static void startRequestAcceptTransferCall(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addAccept(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static void addStatusCode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addReasonPhrase(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endRequestAcceptTransferCall(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
