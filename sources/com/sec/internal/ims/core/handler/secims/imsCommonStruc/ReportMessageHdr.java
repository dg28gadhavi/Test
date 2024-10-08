package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReportMessageHdr extends Table {
    public static ReportMessageHdr getRootAsReportMessageHdr(ByteBuffer byteBuffer) {
        return getRootAsReportMessageHdr(byteBuffer, new ReportMessageHdr());
    }

    public static ReportMessageHdr getRootAsReportMessageHdr(ByteBuffer byteBuffer, ReportMessageHdr reportMessageHdr) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return reportMessageHdr.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ReportMessageHdr __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String spamFrom() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer spamFromAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String spamTo() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer spamToAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String spamDate() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer spamDateAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createReportMessageHdr(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addSpamDate(flatBufferBuilder, i3);
        addSpamTo(flatBufferBuilder, i2);
        addSpamFrom(flatBufferBuilder, i);
        return endReportMessageHdr(flatBufferBuilder);
    }

    public static void startReportMessageHdr(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSpamFrom(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addSpamTo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addSpamDate(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endReportMessageHdr(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
