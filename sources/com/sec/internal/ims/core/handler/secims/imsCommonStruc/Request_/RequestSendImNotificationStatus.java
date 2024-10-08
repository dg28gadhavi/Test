package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImExtension;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendImNotificationStatus extends Table {
    public static RequestSendImNotificationStatus getRootAsRequestSendImNotificationStatus(ByteBuffer byteBuffer) {
        return getRootAsRequestSendImNotificationStatus(byteBuffer, new RequestSendImNotificationStatus());
    }

    public static RequestSendImNotificationStatus getRootAsRequestSendImNotificationStatus(ByteBuffer byteBuffer, RequestSendImNotificationStatus requestSendImNotificationStatus) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendImNotificationStatus.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendImNotificationStatus __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public ImNotificationParam notifications(int i) {
        return notifications(new ImNotificationParam(), i);
    }

    public ImNotificationParam notifications(ImNotificationParam imNotificationParam, int i) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return imNotificationParam.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int notificationsLength() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public long registrationHandle() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String uri() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
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

    public String contributionId() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contributionIdAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public int service() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String deviceId() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer deviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public ImExtension extension() {
        return extension(new ImExtension());
    }

    public ImExtension extension(ImExtension imExtension) {
        int __offset = __offset(20);
        if (__offset != 0) {
            return imExtension.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public boolean isGroupChat() {
        int __offset = __offset(22);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isBotSessionAnonymized() {
        int __offset = __offset(24);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String cpimDateTime() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cpimDateTimeAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String userAlias() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public static int createRequestSendImNotificationStatus(FlatBufferBuilder flatBufferBuilder, long j, int i, long j2, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, boolean z2, int i8, int i9) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(13);
        addUserAlias(flatBufferBuilder, i9);
        addCpimDateTime(flatBufferBuilder, i8);
        int i10 = i7;
        addExtension(flatBufferBuilder, i7);
        int i11 = i6;
        addDeviceId(flatBufferBuilder, i6);
        int i12 = i5;
        addService(flatBufferBuilder, i5);
        int i13 = i4;
        addContributionId(flatBufferBuilder, i4);
        int i14 = i3;
        addConversationId(flatBufferBuilder, i3);
        int i15 = i2;
        addUri(flatBufferBuilder, i2);
        long j3 = j2;
        addRegistrationHandle(flatBufferBuilder, j2);
        int i16 = i;
        addNotifications(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        addIsBotSessionAnonymized(flatBufferBuilder, z2);
        boolean z3 = z;
        addIsGroupChat(flatBufferBuilder, z);
        return endRequestSendImNotificationStatus(flatBufferBuilder);
    }

    public static void startRequestSendImNotificationStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(13);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addNotifications(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int createNotificationsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startNotificationsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addConversationId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addContributionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addService(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(6, i, 0);
    }

    public static void addDeviceId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addExtension(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addIsGroupChat(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(9, z, false);
    }

    public static void addIsBotSessionAnonymized(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(10, z, false);
    }

    public static void addCpimDateTime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(12, i, 0);
    }

    public static int endRequestSendImNotificationStatus(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
