package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestGroupInfoSubscribe extends Table {
    public static RequestGroupInfoSubscribe getRootAsRequestGroupInfoSubscribe(ByteBuffer byteBuffer) {
        return getRootAsRequestGroupInfoSubscribe(byteBuffer, new RequestGroupInfoSubscribe());
    }

    public static RequestGroupInfoSubscribe getRootAsRequestGroupInfoSubscribe(ByteBuffer byteBuffer, RequestGroupInfoSubscribe requestGroupInfoSubscribe) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestGroupInfoSubscribe.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestGroupInfoSubscribe __assign(int i, ByteBuffer byteBuffer) {
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

    public String uri() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestGroupInfoSubscribe(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addUri(flatBufferBuilder, i2);
        addSubscriptionId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestGroupInfoSubscribe(flatBufferBuilder);
    }

    public static void startRequestGroupInfoSubscribe(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRequestGroupInfoSubscribe(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
