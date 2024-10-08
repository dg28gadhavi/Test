package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartImSession extends Table {
    public static RequestStartImSession getRootAsRequestStartImSession(ByteBuffer byteBuffer) {
        return getRootAsRequestStartImSession(byteBuffer, new RequestStartImSession());
    }

    public static RequestStartImSession getRootAsRequestStartImSession(ByteBuffer byteBuffer, RequestStartImSession requestStartImSession) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestStartImSession.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestStartImSession __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long registrationHandle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public ImSessionParam session() {
        return session(new ImSessionParam());
    }

    public ImSessionParam session(ImSessionParam imSessionParam) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return imSessionParam.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public ImMessageParam messageParam() {
        return messageParam(new ImMessageParam());
    }

    public ImMessageParam messageParam(ImMessageParam imMessageParam) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return imMessageParam.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createRequestStartImSession(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addMessageParam(flatBufferBuilder, i2);
        addSession(flatBufferBuilder, i);
        addRegistrationHandle(flatBufferBuilder, j);
        return endRequestStartImSession(flatBufferBuilder);
    }

    public static void startRequestStartImSession(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addRegistrationHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addMessageParam(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRequestStartImSession(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
