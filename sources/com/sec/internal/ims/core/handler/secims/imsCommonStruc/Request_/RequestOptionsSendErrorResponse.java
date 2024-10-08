package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestOptionsSendErrorResponse extends Table {
    public static RequestOptionsSendErrorResponse getRootAsRequestOptionsSendErrorResponse(ByteBuffer byteBuffer) {
        return getRootAsRequestOptionsSendErrorResponse(byteBuffer, new RequestOptionsSendErrorResponse());
    }

    public static RequestOptionsSendErrorResponse getRootAsRequestOptionsSendErrorResponse(ByteBuffer byteBuffer, RequestOptionsSendErrorResponse requestOptionsSendErrorResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestOptionsSendErrorResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestOptionsSendErrorResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public String uri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String txId() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer txIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public int errorCode() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String reason() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reasonAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestOptionsSendErrorResponse(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(5);
        addReason(flatBufferBuilder, i4);
        addErrorCode(flatBufferBuilder, i3);
        addTxId(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestOptionsSendErrorResponse(flatBufferBuilder);
    }

    public static void startRequestOptionsSendErrorResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addTxId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addErrorCode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endRequestOptionsSendErrorResponse(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
