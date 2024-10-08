package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestChatbotAnonymize extends Table {
    public static RequestChatbotAnonymize getRootAsRequestChatbotAnonymize(ByteBuffer byteBuffer) {
        return getRootAsRequestChatbotAnonymize(byteBuffer, new RequestChatbotAnonymize());
    }

    public static RequestChatbotAnonymize getRootAsRequestChatbotAnonymize(ByteBuffer byteBuffer, RequestChatbotAnonymize requestChatbotAnonymize) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestChatbotAnonymize.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestChatbotAnonymize __assign(int i, ByteBuffer byteBuffer) {
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

    public String anonymizeInfo() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer anonymizeInfoAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String commandId() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer commandIdAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createRequestChatbotAnonymize(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addCommandId(flatBufferBuilder, i3);
        addAnonymizeInfo(flatBufferBuilder, i2);
        addChatbotUri(flatBufferBuilder, i);
        addRegistrationHandle(flatBufferBuilder, j);
        return endRequestChatbotAnonymize(flatBufferBuilder);
    }

    public static void startRequestChatbotAnonymize(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addChatbotUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addAnonymizeInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addCommandId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endRequestChatbotAnonymize(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
