package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class MessageRevokeResponseReceived extends Table {
    public static MessageRevokeResponseReceived getRootAsMessageRevokeResponseReceived(ByteBuffer byteBuffer) {
        return getRootAsMessageRevokeResponseReceived(byteBuffer, new MessageRevokeResponseReceived());
    }

    public static MessageRevokeResponseReceived getRootAsMessageRevokeResponseReceived(ByteBuffer byteBuffer, MessageRevokeResponseReceived messageRevokeResponseReceived) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return messageRevokeResponseReceived.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public MessageRevokeResponseReceived __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String uri() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String imdnMessageId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean result() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createMessageRevokeResponseReceived(FlatBufferBuilder flatBufferBuilder, int i, int i2, boolean z) {
        flatBufferBuilder.startObject(3);
        addImdnMessageId(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        addResult(flatBufferBuilder, z);
        return endMessageRevokeResponseReceived(flatBufferBuilder);
    }

    public static void startMessageRevokeResponseReceived(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addImdnMessageId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addResult(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static int endMessageRevokeResponseReceived(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
