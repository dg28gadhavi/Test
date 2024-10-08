package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader_.Pair;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ExtraHeader extends Table {
    public static ExtraHeader getRootAsExtraHeader(ByteBuffer byteBuffer) {
        return getRootAsExtraHeader(byteBuffer, new ExtraHeader());
    }

    public static ExtraHeader getRootAsExtraHeader(ByteBuffer byteBuffer, ExtraHeader extraHeader) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return extraHeader.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ExtraHeader __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public Pair pair(int i) {
        return pair(new Pair(), i);
    }

    public Pair pair(Pair pair, int i) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return pair.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int pairLength() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createExtraHeader(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addPair(flatBufferBuilder, i);
        return endExtraHeader(flatBufferBuilder);
    }

    public static void startExtraHeader(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addPair(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int createPairVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startPairVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endExtraHeader(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
