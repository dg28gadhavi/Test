package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage_.XqContent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XqMessage extends Table {
    public static XqMessage getRootAsXqMessage(ByteBuffer byteBuffer) {
        return getRootAsXqMessage(byteBuffer, new XqMessage());
    }

    public static XqMessage getRootAsXqMessage(ByteBuffer byteBuffer, XqMessage xqMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return xqMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public XqMessage __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int mtrip() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public long sequence() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public XqContent mContent(int i) {
        return mContent(new XqContent(), i);
    }

    public XqContent mContent(XqContent xqContent, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return xqContent.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int mContentLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createXqMessage(FlatBufferBuilder flatBufferBuilder, int i, long j, int i2) {
        flatBufferBuilder.startObject(3);
        addMContent(flatBufferBuilder, i2);
        addSequence(flatBufferBuilder, j);
        addMtrip(flatBufferBuilder, i);
        return endXqMessage(flatBufferBuilder);
    }

    public static void startXqMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addMtrip(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static void addSequence(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addMContent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createMContentVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startMContentVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endXqMessage(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
