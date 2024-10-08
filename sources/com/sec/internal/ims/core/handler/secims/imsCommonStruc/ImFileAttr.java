package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImFileAttr extends Table {
    public static ImFileAttr getRootAsImFileAttr(ByteBuffer byteBuffer) {
        return getRootAsImFileAttr(byteBuffer, new ImFileAttr());
    }

    public static ImFileAttr getRootAsImFileAttr(ByteBuffer byteBuffer, ImFileAttr imFileAttr) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imFileAttr.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImFileAttr __assign(int i, ByteBuffer byteBuffer) {
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

    public String path() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer pathAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String contentType() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long size() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public long start() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public long end() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public long timeDuration() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createImFileAttr(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, long j, long j2, long j3, long j4) {
        flatBufferBuilder.startObject(7);
        addTimeDuration(flatBufferBuilder, j4);
        addEnd(flatBufferBuilder, j3);
        addStart(flatBufferBuilder, j2);
        addSize(flatBufferBuilder, j);
        addContentType(flatBufferBuilder, i3);
        addPath(flatBufferBuilder, i2);
        addName(flatBufferBuilder, i);
        return endImFileAttr(flatBufferBuilder);
    }

    public static void startImFileAttr(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addPath(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addContentType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addSize(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(3, j, 0);
    }

    public static void addStart(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(4, j, 0);
    }

    public static void addEnd(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(5, j, 0);
    }

    public static void addTimeDuration(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(6, j, 0);
    }

    public static int endImFileAttr(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
