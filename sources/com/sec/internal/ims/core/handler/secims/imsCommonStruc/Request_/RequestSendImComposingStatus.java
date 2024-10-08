package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImComposingStatus;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendImComposingStatus extends Table {
    public static RequestSendImComposingStatus getRootAsRequestSendImComposingStatus(ByteBuffer byteBuffer) {
        return getRootAsRequestSendImComposingStatus(byteBuffer, new RequestSendImComposingStatus());
    }

    public static RequestSendImComposingStatus getRootAsRequestSendImComposingStatus(ByteBuffer byteBuffer, RequestSendImComposingStatus requestSendImComposingStatus) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendImComposingStatus.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendImComposingStatus __assign(int i, ByteBuffer byteBuffer) {
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

    public ImComposingStatus status() {
        return status(new ImComposingStatus());
    }

    public ImComposingStatus status(ImComposingStatus imComposingStatus) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return imComposingStatus.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String userAlias() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestSendImComposingStatus(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addUserAlias(flatBufferBuilder, i2);
        addStatus(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endRequestSendImComposingStatus(flatBufferBuilder);
    }

    public static void startRequestSendImComposingStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRequestSendImComposingStatus(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
