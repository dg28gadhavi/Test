package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendDmState extends Table {
    public static RequestSendDmState getRootAsRequestSendDmState(ByteBuffer byteBuffer) {
        return getRootAsRequestSendDmState(byteBuffer, new RequestSendDmState());
    }

    public static RequestSendDmState getRootAsRequestSendDmState(ByteBuffer byteBuffer, RequestSendDmState requestSendDmState) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendDmState.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendDmState __assign(int i, ByteBuffer byteBuffer) {
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

    public boolean isOn() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestSendDmState(FlatBufferBuilder flatBufferBuilder, long j, boolean z) {
        flatBufferBuilder.startObject(2);
        addPhoneId(flatBufferBuilder, j);
        addIsOn(flatBufferBuilder, z);
        return endRequestSendDmState(flatBufferBuilder);
    }

    public static void startRequestSendDmState(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addIsOn(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static int endRequestSendDmState(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
