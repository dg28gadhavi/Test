package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ModifyVideoData extends Table {
    public static ModifyVideoData getRootAsModifyVideoData(ByteBuffer byteBuffer) {
        return getRootAsModifyVideoData(byteBuffer, new ModifyVideoData());
    }

    public static ModifyVideoData getRootAsModifyVideoData(ByteBuffer byteBuffer, ModifyVideoData modifyVideoData) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return modifyVideoData.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ModifyVideoData __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long session() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long direction() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isHeldCall() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createModifyVideoData(FlatBufferBuilder flatBufferBuilder, long j, long j2, boolean z) {
        flatBufferBuilder.startObject(3);
        addDirection(flatBufferBuilder, j2);
        addSession(flatBufferBuilder, j);
        addIsHeldCall(flatBufferBuilder, z);
        return endModifyVideoData(flatBufferBuilder);
    }

    public static void startModifyVideoData(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addDirection(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addIsHeldCall(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static int endModifyVideoData(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
