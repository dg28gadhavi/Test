package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendMessageRevokeRequest extends Table {
    public static RequestSendMessageRevokeRequest getRootAsRequestSendMessageRevokeRequest(ByteBuffer byteBuffer) {
        return getRootAsRequestSendMessageRevokeRequest(byteBuffer, new RequestSendMessageRevokeRequest());
    }

    public static RequestSendMessageRevokeRequest getRootAsRequestSendMessageRevokeRequest(ByteBuffer byteBuffer, RequestSendMessageRevokeRequest requestSendMessageRevokeRequest) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendMessageRevokeRequest.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendMessageRevokeRequest __assign(int i, ByteBuffer byteBuffer) {
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

    public long registrationHandle() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String conversationId() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer conversationIdAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String contributionId() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contributionIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public int service() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createRequestSendMessageRevokeRequest(FlatBufferBuilder flatBufferBuilder, int i, int i2, long j, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(6);
        addService(flatBufferBuilder, i5);
        addContributionId(flatBufferBuilder, i4);
        addConversationId(flatBufferBuilder, i3);
        addRegistrationHandle(flatBufferBuilder, j);
        addImdnMessageId(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        return endRequestSendMessageRevokeRequest(flatBufferBuilder);
    }

    public static void startRequestSendMessageRevokeRequest(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addImdnMessageId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addRegistrationHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addConversationId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addContributionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addService(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(5, i, 0);
    }

    public static int endRequestSendMessageRevokeRequest(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
