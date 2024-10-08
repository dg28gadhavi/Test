package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendMsg extends Table {
    public static RequestSendMsg getRootAsRequestSendMsg(ByteBuffer byteBuffer) {
        return getRootAsRequestSendMsg(byteBuffer, new RequestSendMsg());
    }

    public static RequestSendMsg getRootAsRequestSendMsg(ByteBuffer byteBuffer, RequestSendMsg requestSendMsg) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendMsg.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendMsg __assign(int i, ByteBuffer byteBuffer) {
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

    public String smsc() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer smscAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String localUri() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer localUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String contentType() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String contentSubType() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentSubTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public int contentLen() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String contentBody() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentBodyAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String inReplyTo() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer inReplyToAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public boolean isEmergency() {
        int __offset = __offset(20);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestSendMsg(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z) {
        flatBufferBuilder.startObject(9);
        addInReplyTo(flatBufferBuilder, i7);
        addContentBody(flatBufferBuilder, i6);
        addContentLen(flatBufferBuilder, i5);
        addContentSubType(flatBufferBuilder, i4);
        addContentType(flatBufferBuilder, i3);
        addLocalUri(flatBufferBuilder, i2);
        addSmsc(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addIsEmergency(flatBufferBuilder, z);
        return endRequestSendMsg(flatBufferBuilder);
    }

    public static void startRequestSendMsg(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(9);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSmsc(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addLocalUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addContentType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addContentSubType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addContentLen(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(5, i, 0);
    }

    public static void addContentBody(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addInReplyTo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addIsEmergency(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(8, z, false);
    }

    public static int endRequestSendMsg(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        flatBufferBuilder.required(endObject, 16);
        return endObject;
    }
}
