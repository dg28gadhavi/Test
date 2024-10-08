package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPublishDialog extends Table {
    public static RequestPublishDialog getRootAsRequestPublishDialog(ByteBuffer byteBuffer) {
        return getRootAsRequestPublishDialog(byteBuffer, new RequestPublishDialog());
    }

    public static RequestPublishDialog getRootAsRequestPublishDialog(ByteBuffer byteBuffer, RequestPublishDialog requestPublishDialog) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestPublishDialog.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestPublishDialog __assign(int i, ByteBuffer byteBuffer) {
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

    public String origUri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer origUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String dispName() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer dispNameAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String xmlBody() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer xmlBodyAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public int expireTime() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createRequestPublishDialog(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(5);
        addExpireTime(flatBufferBuilder, i4);
        addXmlBody(flatBufferBuilder, i3);
        addDispName(flatBufferBuilder, i2);
        addOrigUri(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestPublishDialog(flatBufferBuilder);
    }

    public static void startRequestPublishDialog(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addOrigUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addDispName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addXmlBody(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addExpireTime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(4, i, 0);
    }

    public static int endRequestPublishDialog(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
