package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Notify extends Table {
    public static Notify getRootAsNotify(ByteBuffer byteBuffer) {
        return getRootAsNotify(byteBuffer, new Notify());
    }

    public static Notify getRootAsNotify(ByteBuffer byteBuffer, Notify notify) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return notify.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public Notify __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int notifyid() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public byte notiType() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.get(__offset + this.bb_pos);
        }
        return 0;
    }

    public Table noti(Table table) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __union(table, __offset);
        }
        return null;
    }

    public static int createNotify(FlatBufferBuilder flatBufferBuilder, int i, byte b, int i2) {
        flatBufferBuilder.startObject(3);
        addNoti(flatBufferBuilder, i2);
        addNotifyid(flatBufferBuilder, i);
        addNotiType(flatBufferBuilder, b);
        return endNotify(flatBufferBuilder);
    }

    public static void startNotify(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addNotifyid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static void addNotiType(FlatBufferBuilder flatBufferBuilder, byte b) {
        flatBufferBuilder.addByte(1, b, 0);
    }

    public static void addNoti(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endNotify(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
