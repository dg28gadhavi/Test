package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestEmergencyLocationPublish extends Table {
    public static RequestEmergencyLocationPublish getRootAsRequestEmergencyLocationPublish(ByteBuffer byteBuffer) {
        return getRootAsRequestEmergencyLocationPublish(byteBuffer, new RequestEmergencyLocationPublish());
    }

    public static RequestEmergencyLocationPublish getRootAsRequestEmergencyLocationPublish(ByteBuffer byteBuffer, RequestEmergencyLocationPublish requestEmergencyLocationPublish) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestEmergencyLocationPublish.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestEmergencyLocationPublish __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long session() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestEmergencyLocationPublish(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addSession(flatBufferBuilder, j);
        return endRequestEmergencyLocationPublish(flatBufferBuilder);
    }

    public static void startRequestEmergencyLocationPublish(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static int endRequestEmergencyLocationPublish(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
