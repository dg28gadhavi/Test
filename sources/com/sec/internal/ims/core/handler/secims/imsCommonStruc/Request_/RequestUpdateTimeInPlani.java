package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateTimeInPlani extends Table {
    public static RequestUpdateTimeInPlani getRootAsRequestUpdateTimeInPlani(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateTimeInPlani(byteBuffer, new RequestUpdateTimeInPlani());
    }

    public static RequestUpdateTimeInPlani getRootAsRequestUpdateTimeInPlani(ByteBuffer byteBuffer, RequestUpdateTimeInPlani requestUpdateTimeInPlani) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateTimeInPlani.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateTimeInPlani __assign(int i, ByteBuffer byteBuffer) {
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

    public long time() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createRequestUpdateTimeInPlani(FlatBufferBuilder flatBufferBuilder, long j, long j2) {
        flatBufferBuilder.startObject(2);
        addTime(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endRequestUpdateTimeInPlani(flatBufferBuilder);
    }

    public static void startRequestUpdateTimeInPlani(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addTime(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(1, j, 0);
    }

    public static int endRequestUpdateTimeInPlani(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
