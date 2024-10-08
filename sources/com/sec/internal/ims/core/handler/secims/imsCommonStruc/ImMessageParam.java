package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImMessageParam extends Table {
    public static ImMessageParam getRootAsImMessageParam(ByteBuffer byteBuffer) {
        return getRootAsImMessageParam(byteBuffer, new ImMessageParam());
    }

    public static ImMessageParam getRootAsImMessageParam(ByteBuffer byteBuffer, ImMessageParam imMessageParam) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imMessageParam.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImMessageParam __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String sender() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer senderAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String receiver() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer receiverAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String userAlias() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String body() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer bodyAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String contentType() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public ImdnParams imdn() {
        return imdn(new ImdnParams());
    }

    public ImdnParams imdn(ImdnParams imdnParams) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return imdnParams.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public boolean isPublicAccountMsg() {
        int __offset = __offset(16);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean silenceSupported() {
        int __offset = __offset(18);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public CpimNamespace cpimNamespaces(int i) {
        return cpimNamespaces(new CpimNamespace(), i);
    }

    public CpimNamespace cpimNamespaces(CpimNamespace cpimNamespace, int i) {
        int __offset = __offset(20);
        if (__offset != 0) {
            return cpimNamespace.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int cpimNamespacesLength() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String ccParticipants(int i) {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int ccParticipantsLength() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String pAssertedId() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer pAssertedIdAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String requestUri() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer requestUriAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public static int createImMessageParam(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6, boolean z, boolean z2, int i7, int i8, int i9, int i10) {
        flatBufferBuilder.startObject(12);
        addRequestUri(flatBufferBuilder, i10);
        addPAssertedId(flatBufferBuilder, i9);
        addCcParticipants(flatBufferBuilder, i8);
        addCpimNamespaces(flatBufferBuilder, i7);
        addImdn(flatBufferBuilder, i6);
        addContentType(flatBufferBuilder, i5);
        addBody(flatBufferBuilder, i4);
        addUserAlias(flatBufferBuilder, i3);
        addReceiver(flatBufferBuilder, i2);
        addSender(flatBufferBuilder, i);
        addSilenceSupported(flatBufferBuilder, z2);
        addIsPublicAccountMsg(flatBufferBuilder, z);
        return endImMessageParam(flatBufferBuilder);
    }

    public static void startImMessageParam(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(12);
    }

    public static void addSender(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addReceiver(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addBody(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addContentType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addImdn(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addIsPublicAccountMsg(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(6, z, false);
    }

    public static void addSilenceSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(7, z, false);
    }

    public static void addCpimNamespaces(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static int createCpimNamespacesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startCpimNamespacesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addCcParticipants(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static int createCcParticipantsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startCcParticipantsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addPAssertedId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addRequestUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static int endImMessageParam(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        return endObject;
    }
}
