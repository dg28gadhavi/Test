package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.GroupChatInfo;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GroupChatListUpdated extends Table {
    public static GroupChatListUpdated getRootAsGroupChatListUpdated(ByteBuffer byteBuffer) {
        return getRootAsGroupChatListUpdated(byteBuffer, new GroupChatListUpdated());
    }

    public static GroupChatListUpdated getRootAsGroupChatListUpdated(ByteBuffer byteBuffer, GroupChatListUpdated groupChatListUpdated) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return groupChatListUpdated.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public GroupChatListUpdated __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long version() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean increaseMode() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public GroupChatInfo groupChats(int i) {
        return groupChats(new GroupChatInfo(), i);
    }

    public GroupChatInfo groupChats(GroupChatInfo groupChatInfo, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return groupChatInfo.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int groupChatsLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public long uaHandle() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createGroupChatListUpdated(FlatBufferBuilder flatBufferBuilder, long j, boolean z, int i, long j2) {
        flatBufferBuilder.startObject(4);
        addUaHandle(flatBufferBuilder, j2);
        addGroupChats(flatBufferBuilder, i);
        addVersion(flatBufferBuilder, j);
        addIncreaseMode(flatBufferBuilder, z);
        return endGroupChatListUpdated(flatBufferBuilder);
    }

    public static void startGroupChatListUpdated(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addVersion(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addIncreaseMode(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static void addGroupChats(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createGroupChatsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startGroupChatsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addUaHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static int endGroupChatListUpdated(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
