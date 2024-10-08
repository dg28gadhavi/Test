package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPresenceUnsubscribe extends Table {
    public static RequestPresenceUnsubscribe getRootAsRequestPresenceUnsubscribe(ByteBuffer byteBuffer) {
        return getRootAsRequestPresenceUnsubscribe(byteBuffer, new RequestPresenceUnsubscribe());
    }

    public static RequestPresenceUnsubscribe getRootAsRequestPresenceUnsubscribe(ByteBuffer byteBuffer, RequestPresenceUnsubscribe requestPresenceUnsubscribe) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestPresenceUnsubscribe.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestPresenceUnsubscribe __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createRequestPresenceUnsubscribe(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addSubscriptionId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestPresenceUnsubscribe(flatBufferBuilder);
    }

    public static void startRequestPresenceUnsubscribe(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestPresenceUnsubscribe(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
