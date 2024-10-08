package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.CpimNamespace_.Pair;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CpimNamespace extends Table {
    public static CpimNamespace getRootAsCpimNamespace(ByteBuffer byteBuffer) {
        return getRootAsCpimNamespace(byteBuffer, new CpimNamespace());
    }

    public static CpimNamespace getRootAsCpimNamespace(ByteBuffer byteBuffer, CpimNamespace cpimNamespace) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return cpimNamespace.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public CpimNamespace __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String name() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer nameAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String uri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public Pair headers(int i) {
        return headers(new Pair(), i);
    }

    public Pair headers(Pair pair, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return pair.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int headersLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createCpimNamespace(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addHeaders(flatBufferBuilder, i3);
        addUri(flatBufferBuilder, i2);
        addName(flatBufferBuilder, i);
        return endCpimNamespace(flatBufferBuilder);
    }

    public static void startCpimNamespace(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addHeaders(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createHeadersVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startHeadersVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endCpimNamespace(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
