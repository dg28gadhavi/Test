package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReceiveMessageNotification extends Table {
    public static ReceiveMessageNotification getRootAsReceiveMessageNotification(ByteBuffer byteBuffer) {
        return getRootAsReceiveMessageNotification(byteBuffer, new ReceiveMessageNotification());
    }

    public static ReceiveMessageNotification getRootAsReceiveMessageNotification(ByteBuffer byteBuffer, ReceiveMessageNotification receiveMessageNotification) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return receiveMessageNotification.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ReceiveMessageNotification __assign(int i, ByteBuffer byteBuffer) {
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

    public String messageBody() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer messageBodyAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createReceiveMessageNotification(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addMessageBody(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endReceiveMessageNotification(flatBufferBuilder);
    }

    public static void startReceiveMessageNotification(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addMessageBody(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endReceiveMessageNotification(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
