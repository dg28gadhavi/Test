package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReasonHdr extends Table {
    public static ReasonHdr getRootAsReasonHdr(ByteBuffer byteBuffer) {
        return getRootAsReasonHdr(byteBuffer, new ReasonHdr());
    }

    public static ReasonHdr getRootAsReasonHdr(ByteBuffer byteBuffer, ReasonHdr reasonHdr) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return reasonHdr.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ReasonHdr __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long code() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String text() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createReasonHdr(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addText(flatBufferBuilder, i);
        addCode(flatBufferBuilder, j);
        return endReasonHdr(flatBufferBuilder);
    }

    public static void startReasonHdr(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addCode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addText(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endReasonHdr(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
