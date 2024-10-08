package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestNetworkSuspended extends Table {
    public static RequestNetworkSuspended getRootAsRequestNetworkSuspended(ByteBuffer byteBuffer) {
        return getRootAsRequestNetworkSuspended(byteBuffer, new RequestNetworkSuspended());
    }

    public static RequestNetworkSuspended getRootAsRequestNetworkSuspended(ByteBuffer byteBuffer, RequestNetworkSuspended requestNetworkSuspended) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestNetworkSuspended.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestNetworkSuspended __assign(int i, ByteBuffer byteBuffer) {
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

    public boolean state() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestNetworkSuspended(FlatBufferBuilder flatBufferBuilder, long j, boolean z) {
        flatBufferBuilder.startObject(2);
        addHandle(flatBufferBuilder, j);
        addState(flatBufferBuilder, z);
        return endRequestNetworkSuspended(flatBufferBuilder);
    }

    public static void startRequestNetworkSuspended(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addState(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static int endRequestNetworkSuspended(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
