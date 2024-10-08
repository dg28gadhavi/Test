package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendText extends Table {
    public static RequestSendText getRootAsRequestSendText(ByteBuffer byteBuffer) {
        return getRootAsRequestSendText(byteBuffer, new RequestSendText());
    }

    public static RequestSendText getRootAsRequestSendText(ByteBuffer byteBuffer, RequestSendText requestSendText) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendText.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendText __assign(int i, ByteBuffer byteBuffer) {
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

    public long session() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String text() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long len() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestSendText(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i, long j3) {
        flatBufferBuilder.startObject(4);
        addLen(flatBufferBuilder, j3);
        addText(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endRequestSendText(flatBufferBuilder);
    }

    public static void startRequestSendText(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addText(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addLen(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static int endRequestSendText(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
