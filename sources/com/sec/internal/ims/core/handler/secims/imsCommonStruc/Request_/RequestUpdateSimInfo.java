package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateSimInfo extends Table {
    public static RequestUpdateSimInfo getRootAsRequestUpdateSimInfo(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateSimInfo(byteBuffer, new RequestUpdateSimInfo());
    }

    public static RequestUpdateSimInfo getRootAsRequestUpdateSimInfo(ByteBuffer byteBuffer, RequestUpdateSimInfo requestUpdateSimInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateSimInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateSimInfo __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long phoneId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long simInfo() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestUpdateSimInfo(FlatBufferBuilder flatBufferBuilder, long j, long j2) {
        flatBufferBuilder.startObject(2);
        addSimInfo(flatBufferBuilder, j2);
        addPhoneId(flatBufferBuilder, j);
        return endRequestUpdateSimInfo(flatBufferBuilder);
    }

    public static void startRequestUpdateSimInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSimInfo(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static int endRequestUpdateSimInfo(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
