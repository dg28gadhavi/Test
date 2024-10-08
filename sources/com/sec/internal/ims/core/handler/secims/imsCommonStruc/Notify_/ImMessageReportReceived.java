package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImMessageReportReceived extends Table {
    public static ImMessageReportReceived getRootAsImMessageReportReceived(ByteBuffer byteBuffer) {
        return getRootAsImMessageReportReceived(byteBuffer, new ImMessageReportReceived());
    }

    public static ImMessageReportReceived getRootAsImMessageReportReceived(ByteBuffer byteBuffer, ImMessageReportReceived imMessageReportReceived) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imMessageReportReceived.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImMessageReportReceived __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String imdnMessageId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError imError) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return imError.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public boolean isChat() {
        int __offset = __offset(10);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createImMessageReportReceived(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, boolean z) {
        flatBufferBuilder.startObject(4);
        addImError(flatBufferBuilder, i2);
        addImdnMessageId(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        addIsChat(flatBufferBuilder, z);
        return endImMessageReportReceived(flatBufferBuilder);
    }

    public static void startImMessageReportReceived(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImdnMessageId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addIsChat(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(3, z, false);
    }

    public static int endImMessageReportReceived(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
