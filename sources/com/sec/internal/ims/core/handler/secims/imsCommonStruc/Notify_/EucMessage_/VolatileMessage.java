package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class VolatileMessage extends Table {
    public static VolatileMessage getRootAsVolatileMessage(ByteBuffer byteBuffer) {
        return getRootAsVolatileMessage(byteBuffer, new VolatileMessage());
    }

    public static VolatileMessage getRootAsVolatileMessage(ByteBuffer byteBuffer, VolatileMessage volatileMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return volatileMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public VolatileMessage __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public RequestMessage request() {
        return request(new RequestMessage());
    }

    public RequestMessage request(RequestMessage requestMessage) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return requestMessage.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public long timeout() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createVolatileMessage(FlatBufferBuilder flatBufferBuilder, int i, long j) {
        flatBufferBuilder.startObject(2);
        addTimeout(flatBufferBuilder, j);
        addRequest(flatBufferBuilder, i);
        return endVolatileMessage(flatBufferBuilder);
    }

    public static void startVolatileMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addRequest(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addTimeout(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(1, j, 0);
    }

    public static int endVolatileMessage(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
