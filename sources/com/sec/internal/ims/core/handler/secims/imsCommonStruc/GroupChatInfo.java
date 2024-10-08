package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GroupChatInfo extends Table {
    public static GroupChatInfo getRootAsGroupChatInfo(ByteBuffer byteBuffer) {
        return getRootAsGroupChatInfo(byteBuffer, new GroupChatInfo());
    }

    public static GroupChatInfo getRootAsGroupChatInfo(ByteBuffer byteBuffer, GroupChatInfo groupChatInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return groupChatInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public GroupChatInfo __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String method() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer methodAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String uri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String conversationId() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer conversationIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String subject() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createGroupChatInfo(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(4);
        addSubject(flatBufferBuilder, i4);
        addConversationId(flatBufferBuilder, i3);
        addUri(flatBufferBuilder, i2);
        addMethod(flatBufferBuilder, i);
        return endGroupChatInfo(flatBufferBuilder);
    }

    public static void startGroupChatInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addMethod(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addConversationId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addSubject(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endGroupChatInfo(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
