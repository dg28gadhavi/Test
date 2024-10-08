package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdatePani extends Table {
    public static RequestUpdatePani getRootAsRequestUpdatePani(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdatePani(byteBuffer, new RequestUpdatePani());
    }

    public static RequestUpdatePani getRootAsRequestUpdatePani(ByteBuffer byteBuffer, RequestUpdatePani requestUpdatePani) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdatePani.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdatePani __assign(int i, ByteBuffer byteBuffer) {
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

    public String pani() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer paniAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String lastPani() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer lastPaniAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestUpdatePani(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addLastPani(flatBufferBuilder, i2);
        addPani(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestUpdatePani(flatBufferBuilder);
    }

    public static void startRequestUpdatePani(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addPani(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addLastPani(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRequestUpdatePani(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
