package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class BaseSessionData extends Table {
    public static BaseSessionData getRootAsBaseSessionData(ByteBuffer byteBuffer) {
        return getRootAsBaseSessionData(byteBuffer, new BaseSessionData());
    }

    public static BaseSessionData getRootAsBaseSessionData(ByteBuffer byteBuffer, BaseSessionData baseSessionData) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return baseSessionData.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public BaseSessionData __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionHandle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String id() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean isConference() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String sessionUri() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sessionUriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String userAlias() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String receivers(int i) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int receiversLength() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String contributionId() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contributionIdAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String conversationId() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer conversationIdAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String inReplyToContributionId() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer inReplyToContributionIdAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String sessionReplaces() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sessionReplacesAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String sdpContentType() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sdpContentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String serviceId() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer serviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public boolean isChatbotParticipant() {
        int __offset = __offset(28);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String chatMode() {
        int __offset = __offset(30);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer chatModeAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public static int createBaseSessionData(FlatBufferBuilder flatBufferBuilder, long j, int i, boolean z, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, boolean z2, int i11) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(14);
        addChatMode(flatBufferBuilder, i11);
        int i12 = i10;
        addServiceId(flatBufferBuilder, i10);
        int i13 = i9;
        addSdpContentType(flatBufferBuilder, i9);
        int i14 = i8;
        addSessionReplaces(flatBufferBuilder, i8);
        int i15 = i7;
        addInReplyToContributionId(flatBufferBuilder, i7);
        int i16 = i6;
        addConversationId(flatBufferBuilder, i6);
        int i17 = i5;
        addContributionId(flatBufferBuilder, i5);
        int i18 = i4;
        addReceivers(flatBufferBuilder, i4);
        int i19 = i3;
        addUserAlias(flatBufferBuilder, i3);
        int i20 = i2;
        addSessionUri(flatBufferBuilder, i2);
        int i21 = i;
        addId(flatBufferBuilder, i);
        addSessionHandle(flatBufferBuilder, j);
        addIsChatbotParticipant(flatBufferBuilder, z2);
        boolean z3 = z;
        addIsConference(flatBufferBuilder, z);
        return endBaseSessionData(flatBufferBuilder);
    }

    public static void startBaseSessionData(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(14);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addIsConference(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static void addSessionUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addReceivers(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int createReceiversVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startReceiversVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addContributionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addConversationId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addInReplyToContributionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addSessionReplaces(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static void addSdpContentType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addServiceId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static void addIsChatbotParticipant(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(12, z, false);
    }

    public static void addChatMode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(13, i, 0);
    }

    public static int endBaseSessionData(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
