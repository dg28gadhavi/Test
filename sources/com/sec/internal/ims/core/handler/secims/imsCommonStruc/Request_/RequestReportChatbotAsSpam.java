package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestReportChatbotAsSpam extends Table {
    public static RequestReportChatbotAsSpam getRootAsRequestReportChatbotAsSpam(ByteBuffer byteBuffer) {
        return getRootAsRequestReportChatbotAsSpam(byteBuffer, new RequestReportChatbotAsSpam());
    }

    public static RequestReportChatbotAsSpam getRootAsRequestReportChatbotAsSpam(ByteBuffer byteBuffer, RequestReportChatbotAsSpam requestReportChatbotAsSpam) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestReportChatbotAsSpam.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestReportChatbotAsSpam __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long registrationHandle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String chatbotUri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer chatbotUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String requestId() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer requestIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String spamInfo() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer spamInfoAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createRequestReportChatbotAsSpam(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addSpamInfo(flatBufferBuilder, i3);
        addRequestId(flatBufferBuilder, i2);
        addChatbotUri(flatBufferBuilder, i);
        addRegistrationHandle(flatBufferBuilder, j);
        return endRequestReportChatbotAsSpam(flatBufferBuilder);
    }

    public static void startRequestReportChatbotAsSpam(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addChatbotUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addRequestId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addSpamInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endRequestReportChatbotAsSpam(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
