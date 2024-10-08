package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SmsRpAckNotification extends Table {
    public static SmsRpAckNotification getRootAsSmsRpAckNotification(ByteBuffer byteBuffer) {
        return getRootAsSmsRpAckNotification(byteBuffer, new SmsRpAckNotification());
    }

    public static SmsRpAckNotification getRootAsSmsRpAckNotification(ByteBuffer byteBuffer, SmsRpAckNotification smsRpAckNotification) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return smsRpAckNotification.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SmsRpAckNotification __assign(int i, ByteBuffer byteBuffer) {
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

    public String ackCode() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer ackCodeAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long ackLen() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String contentType() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String contentSubType() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentSubTypeAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createSmsRpAckNotification(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, long j2, int i3, int i4) {
        flatBufferBuilder.startObject(6);
        addContentSubType(flatBufferBuilder, i4);
        addContentType(flatBufferBuilder, i3);
        addAckLen(flatBufferBuilder, j2);
        addAckCode(flatBufferBuilder, i2);
        addCallId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endSmsRpAckNotification(flatBufferBuilder);
    }

    public static void startSmsRpAckNotification(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addCallId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addAckCode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addAckLen(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addContentType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addContentSubType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endSmsRpAckNotification(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 12);
        flatBufferBuilder.required(endObject, 14);
        return endObject;
    }
}
