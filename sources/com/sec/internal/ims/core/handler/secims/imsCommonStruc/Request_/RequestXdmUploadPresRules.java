package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUploadPresRules extends Table {
    public static RequestXdmUploadPresRules getRootAsRequestXdmUploadPresRules(ByteBuffer byteBuffer) {
        return getRootAsRequestXdmUploadPresRules(byteBuffer, new RequestXdmUploadPresRules());
    }

    public static RequestXdmUploadPresRules getRootAsRequestXdmUploadPresRules(ByteBuffer byteBuffer, RequestXdmUploadPresRules requestXdmUploadPresRules) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestXdmUploadPresRules.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestXdmUploadPresRules __assign(int i, ByteBuffer byteBuffer) {
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

    public String impu() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String rules() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer rulesAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String btid() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer btidAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String gbaKey() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer gbaKeyAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestXdmUploadPresRules(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(5);
        addGbaKey(flatBufferBuilder, i4);
        addBtid(flatBufferBuilder, i3);
        addRules(flatBufferBuilder, i2);
        addImpu(flatBufferBuilder, i);
        addRid(flatBufferBuilder, j);
        return endRequestXdmUploadPresRules(flatBufferBuilder);
    }

    public static void startRequestXdmUploadPresRules(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addRid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImpu(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addRules(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addBtid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addGbaKey(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endRequestXdmUploadPresRules(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
