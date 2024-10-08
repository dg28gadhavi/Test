package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RetryHdr extends Table {
    public static RetryHdr getRootAsRetryHdr(ByteBuffer byteBuffer) {
        return getRootAsRetryHdr(byteBuffer, new RetryHdr());
    }

    public static RetryHdr getRootAsRetryHdr(ByteBuffer byteBuffer, RetryHdr retryHdr) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return retryHdr.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RetryHdr __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int retryTimer() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String contactValue() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contactValueAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRetryHdr(FlatBufferBuilder flatBufferBuilder, int i, int i2) {
        flatBufferBuilder.startObject(2);
        addContactValue(flatBufferBuilder, i2);
        addRetryTimer(flatBufferBuilder, i);
        return endRetryHdr(flatBufferBuilder);
    }

    public static void startRetryHdr(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addRetryTimer(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static void addContactValue(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRetryHdr(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
