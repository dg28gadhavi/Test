package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class BaseMessage extends Table {
    public static BaseMessage getRootAsBaseMessage(ByteBuffer byteBuffer) {
        return getRootAsBaseMessage(byteBuffer, new BaseMessage());
    }

    public static BaseMessage getRootAsBaseMessage(ByteBuffer byteBuffer, BaseMessage baseMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return baseMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public BaseMessage __assign(int i, ByteBuffer byteBuffer) {
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

    public String id() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String remoteUri() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long timestamp() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createBaseMessage(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, long j2) {
        flatBufferBuilder.startObject(4);
        addTimestamp(flatBufferBuilder, j2);
        addRemoteUri(flatBufferBuilder, i2);
        addId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endBaseMessage(flatBufferBuilder);
    }

    public static void startBaseMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addTimestamp(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(3, j, 0);
    }

    public static int endBaseMessage(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
