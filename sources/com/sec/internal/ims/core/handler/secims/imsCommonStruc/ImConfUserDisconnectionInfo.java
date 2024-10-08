package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConfUserDisconnectionInfo extends Table {
    public static ImConfUserDisconnectionInfo getRootAsImConfUserDisconnectionInfo(ByteBuffer byteBuffer) {
        return getRootAsImConfUserDisconnectionInfo(byteBuffer, new ImConfUserDisconnectionInfo());
    }

    public static ImConfUserDisconnectionInfo getRootAsImConfUserDisconnectionInfo(ByteBuffer byteBuffer, ImConfUserDisconnectionInfo imConfUserDisconnectionInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imConfUserDisconnectionInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImConfUserDisconnectionInfo __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String when() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer whenAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String reason() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reasonAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String by() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer byAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createImConfUserDisconnectionInfo(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addBy(flatBufferBuilder, i3);
        addReason(flatBufferBuilder, i2);
        addWhen(flatBufferBuilder, i);
        return endImConfUserDisconnectionInfo(flatBufferBuilder);
    }

    public static void startImConfUserDisconnectionInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addWhen(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addBy(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endImConfUserDisconnectionInfo(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
