package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUpdateGbaKey extends Table {
    public static RequestXdmUpdateGbaKey getRootAsRequestXdmUpdateGbaKey(ByteBuffer byteBuffer) {
        return getRootAsRequestXdmUpdateGbaKey(byteBuffer, new RequestXdmUpdateGbaKey());
    }

    public static RequestXdmUpdateGbaKey getRootAsRequestXdmUpdateGbaKey(ByteBuffer byteBuffer, RequestXdmUpdateGbaKey requestXdmUpdateGbaKey) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestXdmUpdateGbaKey.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestXdmUpdateGbaKey __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long rid() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String btid() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer btidAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String gbaKey() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer gbaKeyAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestXdmUpdateGbaKey(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addGbaKey(flatBufferBuilder, i2);
        addBtid(flatBufferBuilder, i);
        addRid(flatBufferBuilder, j);
        return endRequestXdmUpdateGbaKey(flatBufferBuilder);
    }

    public static void startRequestXdmUpdateGbaKey(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addRid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addBtid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addGbaKey(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRequestXdmUpdateGbaKey(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
