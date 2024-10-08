package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Response extends Table {
    public static Response getRootAsResponse(ByteBuffer byteBuffer) {
        return getRootAsResponse(byteBuffer, new Response());
    }

    public static Response getRootAsResponse(ByteBuffer byteBuffer, Response response) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return response.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public Response __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int resid() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public byte respType() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.get(__offset + this.bb_pos);
        }
        return 0;
    }

    public Table resp(Table table) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __union(table, __offset);
        }
        return null;
    }

    public static int createResponse(FlatBufferBuilder flatBufferBuilder, int i, byte b, int i2) {
        flatBufferBuilder.startObject(3);
        addResp(flatBufferBuilder, i2);
        addResid(flatBufferBuilder, i);
        addRespType(flatBufferBuilder, b);
        return endResponse(flatBufferBuilder);
    }

    public static void startResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addResid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static void addRespType(FlatBufferBuilder flatBufferBuilder, byte b) {
        flatBufferBuilder.addByte(1, b, 0);
    }

    public static void addResp(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endResponse(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
