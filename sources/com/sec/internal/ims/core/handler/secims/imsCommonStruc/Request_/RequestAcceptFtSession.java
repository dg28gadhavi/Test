package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestAcceptFtSession extends Table {
    public static RequestAcceptFtSession getRootAsRequestAcceptFtSession(ByteBuffer byteBuffer) {
        return getRootAsRequestAcceptFtSession(byteBuffer, new RequestAcceptFtSession());
    }

    public static RequestAcceptFtSession getRootAsRequestAcceptFtSession(ByteBuffer byteBuffer, RequestAcceptFtSession requestAcceptFtSession) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestAcceptFtSession.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestAcceptFtSession __assign(int i, ByteBuffer byteBuffer) {
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

    public long start() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public long end() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public String filePath() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer filePathAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String userAlias() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestAcceptFtSession(FlatBufferBuilder flatBufferBuilder, long j, long j2, long j3, int i, int i2) {
        flatBufferBuilder.startObject(5);
        addEnd(flatBufferBuilder, j3);
        addStart(flatBufferBuilder, j2);
        addUserAlias(flatBufferBuilder, i2);
        addFilePath(flatBufferBuilder, i);
        addSessionHandle(flatBufferBuilder, j);
        return endRequestAcceptFtSession(flatBufferBuilder);
    }

    public static void startRequestAcceptFtSession(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addStart(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(1, j, 0);
    }

    public static void addEnd(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(2, j, 0);
    }

    public static void addFilePath(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endRequestAcceptFtSession(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        return endObject;
    }
}
