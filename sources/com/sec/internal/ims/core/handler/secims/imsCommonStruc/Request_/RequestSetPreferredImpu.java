package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSetPreferredImpu extends Table {
    public static RequestSetPreferredImpu getRootAsRequestSetPreferredImpu(ByteBuffer byteBuffer) {
        return getRootAsRequestSetPreferredImpu(byteBuffer, new RequestSetPreferredImpu());
    }

    public static RequestSetPreferredImpu getRootAsRequestSetPreferredImpu(ByteBuffer byteBuffer, RequestSetPreferredImpu requestSetPreferredImpu) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSetPreferredImpu.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSetPreferredImpu __assign(int i, ByteBuffer byteBuffer) {
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

    public String impu() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestSetPreferredImpu(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addImpu(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestSetPreferredImpu(flatBufferBuilder);
    }

    public static void startRequestSetPreferredImpu(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImpu(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestSetPreferredImpu(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
