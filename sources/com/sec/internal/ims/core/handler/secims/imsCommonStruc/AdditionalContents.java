package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AdditionalContents extends Table {
    public static AdditionalContents getRootAsAdditionalContents(ByteBuffer byteBuffer) {
        return getRootAsAdditionalContents(byteBuffer, new AdditionalContents());
    }

    public static AdditionalContents getRootAsAdditionalContents(ByteBuffer byteBuffer, AdditionalContents additionalContents) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return additionalContents.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public AdditionalContents __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String mimeType() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer mimeTypeAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String contents() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentsAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int rawContents(int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.get(__vector(__offset) + (i * 1)) & 255;
        }
        return 0;
    }

    public int rawContentsLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer rawContentsAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createAdditionalContents(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addRawContents(flatBufferBuilder, i3);
        addContents(flatBufferBuilder, i2);
        addMimeType(flatBufferBuilder, i);
        return endAdditionalContents(flatBufferBuilder);
    }

    public static void startAdditionalContents(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addMimeType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addRawContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
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

    public static int endAdditionalContents(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
