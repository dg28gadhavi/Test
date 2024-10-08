package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GroupChatSubscribeStatus extends Table {
    public static GroupChatSubscribeStatus getRootAsGroupChatSubscribeStatus(ByteBuffer byteBuffer) {
        return getRootAsGroupChatSubscribeStatus(byteBuffer, new GroupChatSubscribeStatus());
    }

    public static GroupChatSubscribeStatus getRootAsGroupChatSubscribeStatus(ByteBuffer byteBuffer, GroupChatSubscribeStatus groupChatSubscribeStatus) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return groupChatSubscribeStatus.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public GroupChatSubscribeStatus __assign(int i, ByteBuffer byteBuffer) {
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

    public boolean isSuccess() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long sipErrorCode() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String sipErrorPhrase() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sipErrorPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String subscriptionId() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscriptionIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createGroupChatSubscribeStatus(FlatBufferBuilder flatBufferBuilder, long j, boolean z, long j2, int i, int i2) {
        flatBufferBuilder.startObject(5);
        addSubscriptionId(flatBufferBuilder, i2);
        addSipErrorPhrase(flatBufferBuilder, i);
        addSipErrorCode(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        addIsSuccess(flatBufferBuilder, z);
        return endGroupChatSubscribeStatus(flatBufferBuilder);
    }

    public static void startGroupChatSubscribeStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addIsSuccess(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static void addSipErrorCode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addSipErrorPhrase(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endGroupChatSubscribeStatus(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        return endObject;
    }
}
