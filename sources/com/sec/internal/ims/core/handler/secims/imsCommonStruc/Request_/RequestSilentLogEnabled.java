package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSilentLogEnabled extends Table {
    public static RequestSilentLogEnabled getRootAsRequestSilentLogEnabled(ByteBuffer byteBuffer) {
        return getRootAsRequestSilentLogEnabled(byteBuffer, new RequestSilentLogEnabled());
    }

    public static RequestSilentLogEnabled getRootAsRequestSilentLogEnabled(ByteBuffer byteBuffer, RequestSilentLogEnabled requestSilentLogEnabled) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSilentLogEnabled.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSilentLogEnabled __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public boolean onoff() {
        int __offset = __offset(4);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestSilentLogEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.startObject(1);
        addOnoff(flatBufferBuilder, z);
        return endRequestSilentLogEnabled(flatBufferBuilder);
    }

    public static void startRequestSilentLogEnabled(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addOnoff(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(0, z, false);
    }

    public static int endRequestSilentLogEnabled(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
