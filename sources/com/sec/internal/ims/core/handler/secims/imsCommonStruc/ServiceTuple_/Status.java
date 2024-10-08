package com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Status extends Table {
    public static Status getRootAsStatus(ByteBuffer byteBuffer) {
        return getRootAsStatus(byteBuffer, new Status());
    }

    public static Status getRootAsStatus(ByteBuffer byteBuffer, Status status) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return status.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public Status __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String basic() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer basicAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public static int createStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addBasic(flatBufferBuilder, i);
        return endStatus(flatBufferBuilder);
    }

    public static void startStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addBasic(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int endStatus(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
