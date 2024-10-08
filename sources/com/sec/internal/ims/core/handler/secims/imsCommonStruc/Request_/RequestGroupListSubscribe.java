package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestGroupListSubscribe extends Table {
    public static RequestGroupListSubscribe getRootAsRequestGroupListSubscribe(ByteBuffer byteBuffer) {
        return getRootAsRequestGroupListSubscribe(byteBuffer, new RequestGroupListSubscribe());
    }

    public static RequestGroupListSubscribe getRootAsRequestGroupListSubscribe(ByteBuffer byteBuffer, RequestGroupListSubscribe requestGroupListSubscribe) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestGroupListSubscribe.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestGroupListSubscribe __assign(int i, ByteBuffer byteBuffer) {
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

    public String subscriptionId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscriptionIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public long version() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isIncrease() {
        int __offset = __offset(10);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestGroupListSubscribe(FlatBufferBuilder flatBufferBuilder, long j, int i, long j2, boolean z) {
        flatBufferBuilder.startObject(4);
        addVersion(flatBufferBuilder, j2);
        addSubscriptionId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addIsIncrease(flatBufferBuilder, z);
        return endRequestGroupListSubscribe(flatBufferBuilder);
    }

    public static void startRequestGroupListSubscribe(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addVersion(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addIsIncrease(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(3, z, false);
    }

    public static int endRequestGroupListSubscribe(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
