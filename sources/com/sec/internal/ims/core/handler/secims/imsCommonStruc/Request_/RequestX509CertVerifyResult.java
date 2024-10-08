package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestX509CertVerifyResult extends Table {
    public static RequestX509CertVerifyResult getRootAsRequestX509CertVerifyResult(ByteBuffer byteBuffer) {
        return getRootAsRequestX509CertVerifyResult(byteBuffer, new RequestX509CertVerifyResult());
    }

    public static RequestX509CertVerifyResult getRootAsRequestX509CertVerifyResult(ByteBuffer byteBuffer, RequestX509CertVerifyResult requestX509CertVerifyResult) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestX509CertVerifyResult.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestX509CertVerifyResult __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public boolean result() {
        int __offset = __offset(4);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String reason() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reasonAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestX509CertVerifyResult(FlatBufferBuilder flatBufferBuilder, boolean z, int i) {
        flatBufferBuilder.startObject(2);
        addReason(flatBufferBuilder, i);
        addResult(flatBufferBuilder, z);
        return endRequestX509CertVerifyResult(flatBufferBuilder);
    }

    public static void startRequestX509CertVerifyResult(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addResult(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(0, z, false);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestX509CertVerifyResult(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
