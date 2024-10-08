package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateParticipants extends Table {
    public static RequestUpdateParticipants getRootAsRequestUpdateParticipants(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateParticipants(byteBuffer, new RequestUpdateParticipants());
    }

    public static RequestUpdateParticipants getRootAsRequestUpdateParticipants(ByteBuffer byteBuffer, RequestUpdateParticipants requestUpdateParticipants) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateParticipants.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateParticipants __assign(int i, ByteBuffer byteBuffer) {
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

    public int reqType() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String receiver(int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int receiverLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String userAlias() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String subject() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String reqKey() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reqKeyAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public ImFileAttr iconAttr() {
        return iconAttr(new ImFileAttr());
    }

    public ImFileAttr iconAttr(ImFileAttr imFileAttr) {
        int __offset = __offset(16);
        if (__offset != 0) {
            return imFileAttr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createRequestUpdateParticipants(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5, int i6) {
        flatBufferBuilder.startObject(7);
        addIconAttr(flatBufferBuilder, i6);
        addReqKey(flatBufferBuilder, i5);
        addSubject(flatBufferBuilder, i4);
        addUserAlias(flatBufferBuilder, i3);
        addReceiver(flatBufferBuilder, i2);
        addReqType(flatBufferBuilder, i);
        addSessionHandle(flatBufferBuilder, j);
        return endRequestUpdateParticipants(flatBufferBuilder);
    }

    public static void startRequestUpdateParticipants(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addReqType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addReceiver(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createReceiverVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startReceiverVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addSubject(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addReqKey(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addIconAttr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int endRequestUpdateParticipants(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
