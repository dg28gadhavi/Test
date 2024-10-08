package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImNotificationStatusReceived extends Table {
    public static ImNotificationStatusReceived getRootAsImNotificationStatusReceived(ByteBuffer byteBuffer) {
        return getRootAsImNotificationStatusReceived(byteBuffer, new ImNotificationStatusReceived());
    }

    public static ImNotificationStatusReceived getRootAsImNotificationStatusReceived(ByteBuffer byteBuffer, ImNotificationStatusReceived imNotificationStatusReceived) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imNotificationStatusReceived.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImNotificationStatusReceived __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long userHandle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long sessionId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String uri() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String cpimDateTime() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cpimDateTimeAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String conversationId() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer conversationIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public ImNotificationParam status() {
        return status(new ImNotificationParam());
    }

    public ImNotificationParam status(ImNotificationParam imNotificationParam) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return imNotificationParam.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String userAlias() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public static int createImNotificationStatusReceived(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i, int i2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(7);
        addUserAlias(flatBufferBuilder, i5);
        addStatus(flatBufferBuilder, i4);
        addConversationId(flatBufferBuilder, i3);
        addCpimDateTime(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j2);
        addUserHandle(flatBufferBuilder, j);
        return endImNotificationStatusReceived(flatBufferBuilder);
    }

    public static void startImNotificationStatusReceived(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addUserHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addCpimDateTime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addConversationId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int endImNotificationStatusReceived(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 14);
        return endObject;
    }
}
