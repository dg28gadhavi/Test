package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestImSetMoreInfoToSipUA extends Table {
    public static RequestImSetMoreInfoToSipUA getRootAsRequestImSetMoreInfoToSipUA(ByteBuffer byteBuffer) {
        return getRootAsRequestImSetMoreInfoToSipUA(byteBuffer, new RequestImSetMoreInfoToSipUA());
    }

    public static RequestImSetMoreInfoToSipUA getRootAsRequestImSetMoreInfoToSipUA(ByteBuffer byteBuffer, RequestImSetMoreInfoToSipUA requestImSetMoreInfoToSipUA) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestImSetMoreInfoToSipUA.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestImSetMoreInfoToSipUA __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String value() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer valueAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public static int createRequestImSetMoreInfoToSipUA(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addValue(flatBufferBuilder, i);
        return endRequestImSetMoreInfoToSipUA(flatBufferBuilder);
    }

    public static void startRequestImSetMoreInfoToSipUA(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addValue(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int endRequestImSetMoreInfoToSipUA(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
