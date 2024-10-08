package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GroupChatInfoUpdated extends Table {
    public static GroupChatInfoUpdated getRootAsGroupChatInfoUpdated(ByteBuffer byteBuffer) {
        return getRootAsGroupChatInfoUpdated(byteBuffer, new GroupChatInfoUpdated());
    }

    public static GroupChatInfoUpdated getRootAsGroupChatInfoUpdated(ByteBuffer byteBuffer, GroupChatInfoUpdated groupChatInfoUpdated) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return groupChatInfoUpdated.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public GroupChatInfoUpdated __assign(int i, ByteBuffer byteBuffer) {
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

    public ImConfInfoUpdated info() {
        return info(new ImConfInfoUpdated());
    }

    public ImConfInfoUpdated info(ImConfInfoUpdated imConfInfoUpdated) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return imConfInfoUpdated.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public long uaHandle() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createGroupChatInfoUpdated(FlatBufferBuilder flatBufferBuilder, int i, int i2, long j) {
        flatBufferBuilder.startObject(3);
        addUaHandle(flatBufferBuilder, j);
        addInfo(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        return endGroupChatInfoUpdated(flatBufferBuilder);
    }

    public static void startGroupChatInfoUpdated(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addUaHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static int endGroupChatInfoUpdated(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
