package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Request extends Table {
    public static Request getRootAsRequest(ByteBuffer byteBuffer) {
        return getRootAsRequest(byteBuffer, new Request());
    }

    public static Request getRootAsRequest(ByteBuffer byteBuffer, Request request) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return request.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public Request __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int reqid() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public byte reqType() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.get(__offset + this.bb_pos);
        }
        return 0;
    }

    public Table req(Table table) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __union(table, __offset);
        }
        return null;
    }

    public static int createRequest(FlatBufferBuilder flatBufferBuilder, int i, byte b, int i2) {
        flatBufferBuilder.startObject(3);
        addReq(flatBufferBuilder, i2);
        addReqid(flatBufferBuilder, i);
        addReqType(flatBufferBuilder, b);
        return endRequest(flatBufferBuilder);
    }

    public static void startRequest(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addReqid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static void addReqType(FlatBufferBuilder flatBufferBuilder, byte b) {
        flatBufferBuilder.addByte(1, b, 0);
    }

    public static void addReq(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRequest(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
