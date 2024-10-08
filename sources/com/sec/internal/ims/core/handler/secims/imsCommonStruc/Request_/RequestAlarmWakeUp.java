package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestAlarmWakeUp extends Table {
    public static RequestAlarmWakeUp getRootAsRequestAlarmWakeUp(ByteBuffer byteBuffer) {
        return getRootAsRequestAlarmWakeUp(byteBuffer, new RequestAlarmWakeUp());
    }

    public static RequestAlarmWakeUp getRootAsRequestAlarmWakeUp(ByteBuffer byteBuffer, RequestAlarmWakeUp requestAlarmWakeUp) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestAlarmWakeUp.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestAlarmWakeUp __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long id() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestAlarmWakeUp(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addId(flatBufferBuilder, j);
        return endRequestAlarmWakeUp(flatBufferBuilder);
    }

    public static void startRequestAlarmWakeUp(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static int endRequestAlarmWakeUp(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
