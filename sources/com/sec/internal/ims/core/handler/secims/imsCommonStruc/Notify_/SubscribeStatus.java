package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SubscribeStatus extends Table {
    public static SubscribeStatus getRootAsSubscribeStatus(ByteBuffer byteBuffer) {
        return getRootAsSubscribeStatus(byteBuffer, new SubscribeStatus());
    }

    public static SubscribeStatus getRootAsSubscribeStatus(ByteBuffer byteBuffer, SubscribeStatus subscribeStatus) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return subscribeStatus.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SubscribeStatus __assign(int i, ByteBuffer byteBuffer) {
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

    public long respCode() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String respReason() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer respReasonAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createSubscribeStatus(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i) {
        flatBufferBuilder.startObject(3);
        addRespReason(flatBufferBuilder, i);
        addRespCode(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endSubscribeStatus(flatBufferBuilder);
    }

    public static void startSubscribeStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addRespCode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addRespReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endSubscribeStatus(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
