package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SubscribeResponse extends Table {
    public static SubscribeResponse getRootAsSubscribeResponse(ByteBuffer byteBuffer) {
        return getRootAsSubscribeResponse(byteBuffer, new SubscribeResponse());
    }

    public static SubscribeResponse getRootAsSubscribeResponse(ByteBuffer byteBuffer, SubscribeResponse subscribeResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return subscribeResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SubscribeResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public long subscriptionId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long expiry() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int state() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int reason() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createSubscribeResponse(FlatBufferBuilder flatBufferBuilder, long j, long j2, long j3, int i, int i2) {
        flatBufferBuilder.startObject(5);
        addReason(flatBufferBuilder, i2);
        addState(flatBufferBuilder, i);
        addExpiry(flatBufferBuilder, j3);
        addSubscriptionId(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endSubscribeResponse(flatBufferBuilder);
    }

    public static void startSubscribeResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addExpiry(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addState(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(4, i, 0);
    }

    public static int endSubscribeResponse(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
