package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SipMessage extends Table {
    public static SipMessage getRootAsSipMessage(ByteBuffer byteBuffer) {
        return getRootAsSipMessage(byteBuffer, new SipMessage());
    }

    public static SipMessage getRootAsSipMessage(ByteBuffer byteBuffer, SipMessage sipMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sipMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SipMessage __assign(int i, ByteBuffer byteBuffer) {
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

    public int direction() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int origin() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String sipMessage() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sipMessageAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String hexContents() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer hexContentsAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public long phoneId() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int mno() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean isRcsProfile() {
        int __offset = __offset(18);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int rawContents(int i) {
        int __offset = __offset(20);
        if (__offset != 0) {
            return this.bb.get(__vector(__offset) + (i * 1)) & 255;
        }
        return 0;
    }

    public int rawContentsLength() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer rawContentsAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public static int createSipMessage(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, long j2, int i5, boolean z, int i6) {
        flatBufferBuilder.startObject(9);
        addRawContents(flatBufferBuilder, i6);
        addMno(flatBufferBuilder, i5);
        addPhoneId(flatBufferBuilder, j2);
        addHexContents(flatBufferBuilder, i4);
        addSipMessage(flatBufferBuilder, i3);
        addOrigin(flatBufferBuilder, i2);
        addDirection(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addIsRcsProfile(flatBufferBuilder, z);
        return endSipMessage(flatBufferBuilder);
    }

    public static void startSipMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(9);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addDirection(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addOrigin(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addSipMessage(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addHexContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(5, (int) j, 0);
    }

    public static void addMno(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(6, i, 0);
    }

    public static void addIsRcsProfile(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(7, z, false);
    }

    public static void addRawContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static int createRawContentsVector(FlatBufferBuilder flatBufferBuilder, byte[] bArr) {
        flatBufferBuilder.startVector(1, bArr.length, 1);
        for (int length = bArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addByte(bArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startRawContentsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(1, i, 1);
    }

    public static int endSipMessage(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
