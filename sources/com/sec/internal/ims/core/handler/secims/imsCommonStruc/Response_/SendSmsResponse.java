package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SendSmsResponse extends Table {
    public static SendSmsResponse getRootAsSendSmsResponse(ByteBuffer byteBuffer) {
        return getRootAsSendSmsResponse(byteBuffer, new SendSmsResponse());
    }

    public static SendSmsResponse getRootAsSendSmsResponse(ByteBuffer byteBuffer, SendSmsResponse sendSmsResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sendSmsResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SendSmsResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public long statusCode() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String callId() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer callIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String errStr() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer errStrAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public long retryAfter() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String content() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String contentType() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String contentSubType() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentSubTypeAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public static int createSendSmsResponse(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i, int i2, long j3, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(8);
        addContentSubType(flatBufferBuilder, i5);
        addContentType(flatBufferBuilder, i4);
        addContent(flatBufferBuilder, i3);
        addRetryAfter(flatBufferBuilder, j3);
        addErrStr(flatBufferBuilder, i2);
        addCallId(flatBufferBuilder, i);
        addStatusCode(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endSendSmsResponse(flatBufferBuilder);
    }

    public static void startSendSmsResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(8);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addStatusCode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addCallId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addErrStr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addRetryAfter(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(4, (int) j, 0);
    }

    public static void addContent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addContentType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addContentSubType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static int endSendSmsResponse(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 14);
        flatBufferBuilder.required(endObject, 16);
        flatBufferBuilder.required(endObject, 18);
        return endObject;
    }
}
