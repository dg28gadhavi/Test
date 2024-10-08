package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestChatbotAnonymizeResponse extends Table {
    public static RequestChatbotAnonymizeResponse getRootAsRequestChatbotAnonymizeResponse(ByteBuffer byteBuffer) {
        return getRootAsRequestChatbotAnonymizeResponse(byteBuffer, new RequestChatbotAnonymizeResponse());
    }

    public static RequestChatbotAnonymizeResponse getRootAsRequestChatbotAnonymizeResponse(ByteBuffer byteBuffer, RequestChatbotAnonymizeResponse requestChatbotAnonymizeResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestChatbotAnonymizeResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestChatbotAnonymizeResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public String commandId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer commandIdAsByteBuffer() {
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

    public int retryAfter() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createRequestChatbotAnonymizeResponse(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(4);
        addRetryAfter(flatBufferBuilder, i4);
        addImError(flatBufferBuilder, i3);
        addCommandId(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        return endRequestChatbotAnonymizeResponse(flatBufferBuilder);
    }

    public static void startRequestChatbotAnonymizeResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addCommandId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addRetryAfter(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static int endRequestChatbotAnonymizeResponse(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
