package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImSessionParam extends Table {
    public static ImSessionParam getRootAsImSessionParam(ByteBuffer byteBuffer) {
        return getRootAsImSessionParam(byteBuffer, new ImSessionParam());
    }

    public static ImSessionParam getRootAsImSessionParam(ByteBuffer byteBuffer, ImSessionParam imSessionParam) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imSessionParam.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImSessionParam __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public BaseSessionData baseSessionData() {
        return baseSessionData(new BaseSessionData());
    }

    public BaseSessionData baseSessionData(BaseSessionData baseSessionData) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return baseSessionData.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String sender() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer senderAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String subject() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public boolean isRejoin() {
        int __offset = __offset(10);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isClosedGroupchat() {
        int __offset = __offset(12);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isInviteforbye() {
        int __offset = __offset(14);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isExtension() {
        int __offset = __offset(16);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String acceptTypes(int i) {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int acceptTypesLength() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String acceptWrappedTypes(int i) {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int acceptWrappedTypesLength() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public boolean isMsgRevokeSupported() {
        int __offset = __offset(22);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isMsgFallbackSupported() {
        int __offset = __offset(24);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isGeolocationPush() {
        int __offset = __offset(26);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isSendOnly() {
        int __offset = __offset(28);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createImSessionParam(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, boolean z, boolean z2, boolean z3, boolean z4, int i4, int i5, boolean z5, boolean z6, boolean z7, boolean z8) {
        flatBufferBuilder.startObject(13);
        addAcceptWrappedTypes(flatBufferBuilder, i5);
        addAcceptTypes(flatBufferBuilder, i4);
        addSubject(flatBufferBuilder, i3);
        addSender(flatBufferBuilder, i2);
        addBaseSessionData(flatBufferBuilder, i);
        addIsSendOnly(flatBufferBuilder, z8);
        addIsGeolocationPush(flatBufferBuilder, z7);
        addIsMsgFallbackSupported(flatBufferBuilder, z6);
        addIsMsgRevokeSupported(flatBufferBuilder, z5);
        addIsExtension(flatBufferBuilder, z4);
        addIsInviteforbye(flatBufferBuilder, z3);
        addIsClosedGroupchat(flatBufferBuilder, z2);
        addIsRejoin(flatBufferBuilder, z);
        return endImSessionParam(flatBufferBuilder);
    }

    public static void startImSessionParam(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(13);
    }

    public static void addBaseSessionData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addSender(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addSubject(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addIsRejoin(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(3, z, false);
    }

    public static void addIsClosedGroupchat(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(4, z, false);
    }

    public static void addIsInviteforbye(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(5, z, false);
    }

    public static void addIsExtension(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(6, z, false);
    }

    public static void addAcceptTypes(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static int createAcceptTypesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startAcceptTypesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addAcceptWrappedTypes(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static int createAcceptWrappedTypesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startAcceptWrappedTypesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addIsMsgRevokeSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(9, z, false);
    }

    public static void addIsMsgFallbackSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(10, z, false);
    }

    public static void addIsGeolocationPush(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(11, z, false);
    }

    public static void addIsSendOnly(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(12, z, false);
    }

    public static int endImSessionParam(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
