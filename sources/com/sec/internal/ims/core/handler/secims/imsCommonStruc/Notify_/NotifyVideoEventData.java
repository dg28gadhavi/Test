package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NotifyVideoEventData extends Table {
    public static NotifyVideoEventData getRootAsNotifyVideoEventData(ByteBuffer byteBuffer) {
        return getRootAsNotifyVideoEventData(byteBuffer, new NotifyVideoEventData());
    }

    public static NotifyVideoEventData getRootAsNotifyVideoEventData(ByteBuffer byteBuffer, NotifyVideoEventData notifyVideoEventData) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return notifyVideoEventData.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public NotifyVideoEventData __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long phoneId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long session() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long event() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long arg1() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long arg2() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createNotifyVideoEventData(FlatBufferBuilder flatBufferBuilder, long j, long j2, long j3, long j4, long j5) {
        flatBufferBuilder.startObject(5);
        addArg2(flatBufferBuilder, j5);
        addArg1(flatBufferBuilder, j4);
        addEvent(flatBufferBuilder, j3);
        addSession(flatBufferBuilder, j2);
        addPhoneId(flatBufferBuilder, j);
        return endNotifyVideoEventData(flatBufferBuilder);
    }

    public static void startNotifyVideoEventData(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addEvent(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addArg1(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addArg2(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(4, (int) j, 0);
    }

    public static int endNotifyVideoEventData(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
