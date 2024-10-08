package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SlmLMMInvited extends Table {
    public static SlmLMMInvited getRootAsSlmLMMInvited(ByteBuffer byteBuffer) {
        return getRootAsSlmLMMInvited(byteBuffer, new SlmLMMInvited());
    }

    public static SlmLMMInvited getRootAsSlmLMMInvited(ByteBuffer byteBuffer, SlmLMMInvited slmLMMInvited) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return slmLMMInvited.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SlmLMMInvited __assign(int i, ByteBuffer byteBuffer) {
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

    public String userAlias() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String sender() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer senderAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long userHandle() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isGroup() {
        int __offset = __offset(12);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createSlmLMMInvited(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, long j2, boolean z) {
        flatBufferBuilder.startObject(5);
        addUserHandle(flatBufferBuilder, j2);
        addSender(flatBufferBuilder, i2);
        addUserAlias(flatBufferBuilder, i);
        addSessionHandle(flatBufferBuilder, j);
        addIsGroup(flatBufferBuilder, z);
        return endSlmLMMInvited(flatBufferBuilder);
    }

    public static void startSlmLMMInvited(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addSender(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addUserHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addIsGroup(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(4, z, false);
    }

    public static int endSlmLMMInvited(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
