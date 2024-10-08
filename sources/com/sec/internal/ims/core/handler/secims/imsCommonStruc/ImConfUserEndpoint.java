package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConfUserEndpoint extends Table {
    public static ImConfUserEndpoint getRootAsImConfUserEndpoint(ByteBuffer byteBuffer) {
        return getRootAsImConfUserEndpoint(byteBuffer, new ImConfUserEndpoint());
    }

    public static ImConfUserEndpoint getRootAsImConfUserEndpoint(ByteBuffer byteBuffer, ImConfUserEndpoint imConfUserEndpoint) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imConfUserEndpoint.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImConfUserEndpoint __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String entity() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer entityAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String status() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer statusAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String disconnectMethod() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer disconnectMethodAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public ImConfUserJoiningInfo joininginfo() {
        return joininginfo(new ImConfUserJoiningInfo());
    }

    public ImConfUserJoiningInfo joininginfo(ImConfUserJoiningInfo imConfUserJoiningInfo) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return imConfUserJoiningInfo.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public ImConfUserDisconnectionInfo disconnectioninfo() {
        return disconnectioninfo(new ImConfUserDisconnectionInfo());
    }

    public ImConfUserDisconnectionInfo disconnectioninfo(ImConfUserDisconnectionInfo imConfUserDisconnectionInfo) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return imConfUserDisconnectionInfo.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createImConfUserEndpoint(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(5);
        addDisconnectioninfo(flatBufferBuilder, i5);
        addJoininginfo(flatBufferBuilder, i4);
        addDisconnectMethod(flatBufferBuilder, i3);
        addStatus(flatBufferBuilder, i2);
        addEntity(flatBufferBuilder, i);
        return endImConfUserEndpoint(flatBufferBuilder);
    }

    public static void startImConfUserEndpoint(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addEntity(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addDisconnectMethod(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addJoininginfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addDisconnectioninfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endImConfUserEndpoint(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
