package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AckMessage extends Table {
    public static AckMessage getRootAsAckMessage(ByteBuffer byteBuffer) {
        return getRootAsAckMessage(byteBuffer, new AckMessage());
    }

    public static AckMessage getRootAsAckMessage(ByteBuffer byteBuffer, AckMessage ackMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return ackMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public AckMessage __assign(int i, ByteBuffer byteBuffer) {
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

    public EucContent content() {
        return content(new EucContent());
    }

    public EucContent content(EucContent eucContent) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return eucContent.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public int status() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createAckMessage(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addStatus(flatBufferBuilder, i3);
        addContent(flatBufferBuilder, i2);
        addBase(flatBufferBuilder, i);
        return endAckMessage(flatBufferBuilder);
    }

    public static void startAckMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addBase(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addContent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static int endAckMessage(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
