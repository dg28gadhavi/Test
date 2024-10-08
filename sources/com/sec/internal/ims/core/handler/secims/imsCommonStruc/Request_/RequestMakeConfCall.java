package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestMakeConfCall extends Table {
    public static RequestMakeConfCall getRootAsRequestMakeConfCall(ByteBuffer byteBuffer) {
        return getRootAsRequestMakeConfCall(byteBuffer, new RequestMakeConfCall());
    }

    public static RequestMakeConfCall getRootAsRequestMakeConfCall(ByteBuffer byteBuffer, RequestMakeConfCall requestMakeConfCall) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestMakeConfCall.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestMakeConfCall __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long handle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String confuri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer confuriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int callType() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int confType() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String eventSubscribe() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer eventSubscribeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String dialogType() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer dialogTypeAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public int sessionId(int i) {
        int __offset = __offset(16);
        if (__offset != 0) {
            return this.bb.getInt(__vector(__offset) + (i * 4));
        }
        return 0;
    }

    public int sessionIdLength() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer sessionIdAsByteBuffer() {
        return __vector_as_bytebuffer(16, 4);
    }

    public String participants(int i) {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int participantsLength() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String origUri() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer origUriAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String referuriType() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer referuriTypeAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String removeReferuriType() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer removeReferuriTypeAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String referuriAsserted() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer referuriAssertedAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String useAnonymousUpdate() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer useAnonymousUpdateAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public boolean supportPrematureEnd() {
        int __offset = __offset(30);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public ExtraHeader extraHeaders() {
        return extraHeaders(new ExtraHeader());
    }

    public ExtraHeader extraHeaders(ExtraHeader extraHeader) {
        int __offset = __offset(32);
        if (__offset != 0) {
            return extraHeader.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createRequestMakeConfCall(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, boolean z, int i13) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(15);
        addExtraHeaders(flatBufferBuilder, i13);
        addUseAnonymousUpdate(flatBufferBuilder, i12);
        int i14 = i11;
        addReferuriAsserted(flatBufferBuilder, i11);
        int i15 = i10;
        addRemoveReferuriType(flatBufferBuilder, i10);
        int i16 = i9;
        addReferuriType(flatBufferBuilder, i9);
        int i17 = i8;
        addOrigUri(flatBufferBuilder, i8);
        int i18 = i7;
        addParticipants(flatBufferBuilder, i7);
        int i19 = i6;
        addSessionId(flatBufferBuilder, i6);
        int i20 = i5;
        addDialogType(flatBufferBuilder, i5);
        int i21 = i4;
        addEventSubscribe(flatBufferBuilder, i4);
        int i22 = i3;
        addConfType(flatBufferBuilder, i3);
        int i23 = i2;
        addCallType(flatBufferBuilder, i2);
        int i24 = i;
        addConfuri(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addSupportPrematureEnd(flatBufferBuilder, z);
        return endRequestMakeConfCall(flatBufferBuilder);
    }

    public static void startRequestMakeConfCall(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(15);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addConfuri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addCallType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addConfType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static void addEventSubscribe(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addDialogType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int createSessionIdVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addInt(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startSessionIdVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addParticipants(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static int createParticipantsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startParticipantsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addOrigUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addReferuriType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static void addRemoveReferuriType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addReferuriAsserted(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static void addUseAnonymousUpdate(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(12, i, 0);
    }

    public static void addSupportPrematureEnd(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(13, z, false);
    }

    public static void addExtraHeaders(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(14, i, 0);
    }

    public static int endRequestMakeConfCall(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 12);
        flatBufferBuilder.required(endObject, 14);
        flatBufferBuilder.required(endObject, 22);
        flatBufferBuilder.required(endObject, 24);
        flatBufferBuilder.required(endObject, 26);
        flatBufferBuilder.required(endObject, 28);
        return endObject;
    }
}
