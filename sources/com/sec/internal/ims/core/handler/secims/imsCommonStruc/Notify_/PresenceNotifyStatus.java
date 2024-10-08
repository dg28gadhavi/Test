package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PresenceNotifyStatus extends Table {
    public static PresenceNotifyStatus getRootAsPresenceNotifyStatus(ByteBuffer byteBuffer) {
        return getRootAsPresenceNotifyStatus(byteBuffer, new PresenceNotifyStatus());
    }

    public static PresenceNotifyStatus getRootAsPresenceNotifyStatus(ByteBuffer byteBuffer, PresenceNotifyStatus presenceNotifyStatus) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return presenceNotifyStatus.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public PresenceNotifyStatus __assign(int i, ByteBuffer byteBuffer) {
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

    public String subscriptionId() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscriptionIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String subscribeTerminatedReason() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscribeTerminatedReasonAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public long phoneId() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createPresenceNotifyStatus(FlatBufferBuilder flatBufferBuilder, long j, boolean z, int i, int i2, long j2) {
        flatBufferBuilder.startObject(5);
        addPhoneId(flatBufferBuilder, j2);
        addSubscribeTerminatedReason(flatBufferBuilder, i2);
        addSubscriptionId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addIsSuccess(flatBufferBuilder, z);
        return endPresenceNotifyStatus(flatBufferBuilder);
    }

    public static void startPresenceNotifyStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addIsSuccess(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static void addSubscriptionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addSubscribeTerminatedReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(4, (int) j, 0);
    }

    public static int endPresenceNotifyStatus(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
