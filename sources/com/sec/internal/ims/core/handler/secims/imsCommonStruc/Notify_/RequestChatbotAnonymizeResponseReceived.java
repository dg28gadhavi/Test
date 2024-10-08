package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestChatbotAnonymizeResponseReceived extends Table {
    public static RequestChatbotAnonymizeResponseReceived getRootAsRequestChatbotAnonymizeResponseReceived(ByteBuffer byteBuffer) {
        return getRootAsRequestChatbotAnonymizeResponseReceived(byteBuffer, new RequestChatbotAnonymizeResponseReceived());
    }

    public static RequestChatbotAnonymizeResponseReceived getRootAsRequestChatbotAnonymizeResponseReceived(ByteBuffer byteBuffer, RequestChatbotAnonymizeResponseReceived requestChatbotAnonymizeResponseReceived) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestChatbotAnonymizeResponseReceived.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestChatbotAnonymizeResponseReceived __assign(int i, ByteBuffer byteBuffer) {
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

    public String result() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer resultAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestChatbotAnonymizeResponseReceived(FlatBufferBuilder flatBufferBuilder, int i, int i2) {
        flatBufferBuilder.startObject(2);
        addResult(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        return endRequestChatbotAnonymizeResponseReceived(flatBufferBuilder);
    }

    public static void startRequestChatbotAnonymizeResponseReceived(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addResult(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestChatbotAnonymizeResponseReceived(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
