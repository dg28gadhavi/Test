package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateXqEnable extends Table {
    public static RequestUpdateXqEnable getRootAsRequestUpdateXqEnable(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateXqEnable(byteBuffer, new RequestUpdateXqEnable());
    }

    public static RequestUpdateXqEnable getRootAsRequestUpdateXqEnable(ByteBuffer byteBuffer, RequestUpdateXqEnable requestUpdateXqEnable) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateXqEnable.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateXqEnable __assign(int i, ByteBuffer byteBuffer) {
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

    public boolean enable() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestUpdateXqEnable(FlatBufferBuilder flatBufferBuilder, long j, boolean z) {
        flatBufferBuilder.startObject(2);
        addPhoneId(flatBufferBuilder, j);
        addEnable(flatBufferBuilder, z);
        return endRequestUpdateXqEnable(flatBufferBuilder);
    }

    public static void startRequestUpdateXqEnable(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addEnable(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static int endRequestUpdateXqEnable(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
