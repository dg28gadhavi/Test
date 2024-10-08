package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestMsgSetMsgAppInfoToSipUa extends Table {
    public static RequestMsgSetMsgAppInfoToSipUa getRootAsRequestMsgSetMsgAppInfoToSipUa(ByteBuffer byteBuffer) {
        return getRootAsRequestMsgSetMsgAppInfoToSipUa(byteBuffer, new RequestMsgSetMsgAppInfoToSipUa());
    }

    public static RequestMsgSetMsgAppInfoToSipUa getRootAsRequestMsgSetMsgAppInfoToSipUa(ByteBuffer byteBuffer, RequestMsgSetMsgAppInfoToSipUa requestMsgSetMsgAppInfoToSipUa) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestMsgSetMsgAppInfoToSipUa.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestMsgSetMsgAppInfoToSipUa __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createRequestMsgSetMsgAppInfoToSipUa(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addValue(flatBufferBuilder, i);
        return endRequestMsgSetMsgAppInfoToSipUa(flatBufferBuilder);
    }

    public static void startRequestMsgSetMsgAppInfoToSipUa(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addValue(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int endRequestMsgSetMsgAppInfoToSipUa(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
