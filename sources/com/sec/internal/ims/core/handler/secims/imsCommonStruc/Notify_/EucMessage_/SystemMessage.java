package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SystemMessage extends Table {
    public static SystemMessage getRootAsSystemMessage(ByteBuffer byteBuffer) {
        return getRootAsSystemMessage(byteBuffer, new SystemMessage());
    }

    public static SystemMessage getRootAsSystemMessage(ByteBuffer byteBuffer, SystemMessage systemMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return systemMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SystemMessage __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public BaseMessage base() {
        return base(new BaseMessage());
    }

    public BaseMessage base(BaseMessage baseMessage) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return baseMessage.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String type() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer typeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String data() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer dataAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createSystemMessage(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addData(flatBufferBuilder, i3);
        addType(flatBufferBuilder, i2);
        addBase(flatBufferBuilder, i);
        return endSystemMessage(flatBufferBuilder);
    }

    public static void startSystemMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addBase(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endSystemMessage(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
