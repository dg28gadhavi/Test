package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReceiveSmsNotification extends Table {
    public static ReceiveSmsNotification getRootAsReceiveSmsNotification(ByteBuffer byteBuffer) {
        return getRootAsReceiveSmsNotification(byteBuffer, new ReceiveSmsNotification());
    }

    public static ReceiveSmsNotification getRootAsReceiveSmsNotification(ByteBuffer byteBuffer, ReceiveSmsNotification receiveSmsNotification) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return receiveSmsNotification.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ReceiveSmsNotification __assign(int i, ByteBuffer byteBuffer) {
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

    public String callId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer callIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String content() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentAsByteBuffer() {
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

    public long len() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String scUri() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer scUriAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public static int createReceiveSmsNotification(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, long j2, int i5) {
        flatBufferBuilder.startObject(7);
        addScUri(flatBufferBuilder, i5);
        addLen(flatBufferBuilder, j2);
        addContentSubType(flatBufferBuilder, i4);
        addContentType(flatBufferBuilder, i3);
        addContent(flatBufferBuilder, i2);
        addCallId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endReceiveSmsNotification(flatBufferBuilder);
    }

    public static void startReceiveSmsNotification(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addCallId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addContent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addContentType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addContentSubType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addLen(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(5, (int) j, 0);
    }

    public static void addScUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int endReceiveSmsNotification(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        flatBufferBuilder.required(endObject, 16);
        return endObject;
    }
}
