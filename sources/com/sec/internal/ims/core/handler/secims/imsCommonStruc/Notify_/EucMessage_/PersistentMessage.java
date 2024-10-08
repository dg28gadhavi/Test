package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PersistentMessage extends Table {
    public static PersistentMessage getRootAsPersistentMessage(ByteBuffer byteBuffer) {
        return getRootAsPersistentMessage(byteBuffer, new PersistentMessage());
    }

    public static PersistentMessage getRootAsPersistentMessage(ByteBuffer byteBuffer, PersistentMessage persistentMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return persistentMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public PersistentMessage __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createPersistentMessage(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addRequest(flatBufferBuilder, i);
        return endPersistentMessage(flatBufferBuilder);
    }

    public static void startPersistentMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addRequest(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int endPersistentMessage(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
