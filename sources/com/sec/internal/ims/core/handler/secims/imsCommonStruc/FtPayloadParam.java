package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class FtPayloadParam extends Table {
    public static FtPayloadParam getRootAsFtPayloadParam(ByteBuffer byteBuffer) {
        return getRootAsFtPayloadParam(byteBuffer, new FtPayloadParam());
    }

    public static FtPayloadParam getRootAsFtPayloadParam(ByteBuffer byteBuffer, FtPayloadParam ftPayloadParam) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return ftPayloadParam.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public FtPayloadParam __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public ImFileAttr fileAttr() {
        return fileAttr(new ImFileAttr());
    }

    public ImFileAttr fileAttr(ImFileAttr imFileAttr) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return imFileAttr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public ImFileAttr iconAttr() {
        return iconAttr(new ImFileAttr());
    }

    public ImFileAttr iconAttr(ImFileAttr imFileAttr) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return imFileAttr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public ImdnParams imdn() {
        return imdn(new ImdnParams());
    }

    public ImdnParams imdn(ImdnParams imdnParams) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return imdnParams.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public boolean isPublicAccountMsg() {
        int __offset = __offset(10);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isPush() {
        int __offset = __offset(12);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public CpimNamespace cpimNamespaces(int i) {
        return cpimNamespaces(new CpimNamespace(), i);
    }

    public CpimNamespace cpimNamespaces(CpimNamespace cpimNamespace, int i) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return cpimNamespace.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int cpimNamespacesLength() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String sender() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer senderAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String receiver() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer receiverAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public boolean silenceSupported() {
        int __offset = __offset(20);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String fileFingerPrint() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer fileFingerPrintAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
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

    public static int createFtPayloadParam(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, boolean z, boolean z2, int i4, int i5, int i6, boolean z3, int i7, int i8, int i9) {
        flatBufferBuilder.startObject(12);
        addRequestUri(flatBufferBuilder, i9);
        addPAssertedId(flatBufferBuilder, i8);
        addFileFingerPrint(flatBufferBuilder, i7);
        addReceiver(flatBufferBuilder, i6);
        addSender(flatBufferBuilder, i5);
        addCpimNamespaces(flatBufferBuilder, i4);
        addImdn(flatBufferBuilder, i3);
        addIconAttr(flatBufferBuilder, i2);
        addFileAttr(flatBufferBuilder, i);
        addSilenceSupported(flatBufferBuilder, z3);
        addIsPush(flatBufferBuilder, z2);
        addIsPublicAccountMsg(flatBufferBuilder, z);
        return endFtPayloadParam(flatBufferBuilder);
    }

    public static void startFtPayloadParam(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(12);
    }

    public static void addFileAttr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addIconAttr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addImdn(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addIsPublicAccountMsg(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(3, z, false);
    }

    public static void addIsPush(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(4, z, false);
    }

    public static void addCpimNamespaces(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
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

    public static void addSender(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addReceiver(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addSilenceSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(8, z, false);
    }

    public static void addFileFingerPrint(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static void addPAssertedId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addRequestUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static int endFtPayloadParam(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
