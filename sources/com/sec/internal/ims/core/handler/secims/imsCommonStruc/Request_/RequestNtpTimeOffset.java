package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestNtpTimeOffset extends Table {
    public static RequestNtpTimeOffset getRootAsRequestNtpTimeOffset(ByteBuffer byteBuffer) {
        return getRootAsRequestNtpTimeOffset(byteBuffer, new RequestNtpTimeOffset());
    }

    public static RequestNtpTimeOffset getRootAsRequestNtpTimeOffset(ByteBuffer byteBuffer, RequestNtpTimeOffset requestNtpTimeOffset) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestNtpTimeOffset.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestNtpTimeOffset __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long offset() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createRequestNtpTimeOffset(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addOffset(flatBufferBuilder, j);
        return endRequestNtpTimeOffset(flatBufferBuilder);
    }

    public static void startRequestNtpTimeOffset(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addOffset(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(0, j, 0);
    }

    public static int endRequestNtpTimeOffset(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
