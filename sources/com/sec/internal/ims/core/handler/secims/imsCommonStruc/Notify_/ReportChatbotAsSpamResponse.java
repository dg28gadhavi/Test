package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReportChatbotAsSpamResponse extends Table {
    public static ReportChatbotAsSpamResponse getRootAsReportChatbotAsSpamResponse(ByteBuffer byteBuffer) {
        return getRootAsReportChatbotAsSpamResponse(byteBuffer, new ReportChatbotAsSpamResponse());
    }

    public static ReportChatbotAsSpamResponse getRootAsReportChatbotAsSpamResponse(ByteBuffer byteBuffer, ReportChatbotAsSpamResponse reportChatbotAsSpamResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return reportChatbotAsSpamResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ReportChatbotAsSpamResponse __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String uri() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String requestId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer requestIdAsByteBuffer() {
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

    public static int createReportChatbotAsSpamResponse(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addImError(flatBufferBuilder, i3);
        addRequestId(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        return endReportChatbotAsSpamResponse(flatBufferBuilder);
    }

    public static void startReportChatbotAsSpamResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addRequestId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endReportChatbotAsSpamResponse(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
