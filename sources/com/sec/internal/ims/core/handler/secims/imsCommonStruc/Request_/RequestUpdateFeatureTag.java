package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateFeatureTag extends Table {
    public static RequestUpdateFeatureTag getRootAsRequestUpdateFeatureTag(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateFeatureTag(byteBuffer, new RequestUpdateFeatureTag());
    }

    public static RequestUpdateFeatureTag getRootAsRequestUpdateFeatureTag(ByteBuffer byteBuffer, RequestUpdateFeatureTag requestUpdateFeatureTag) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateFeatureTag.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateFeatureTag __assign(int i, ByteBuffer byteBuffer) {
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

    public int featureTagList(int i) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__vector(__offset) + (i * 4));
        }
        return 0;
    }

    public int featureTagListLength() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer featureTagListAsByteBuffer() {
        return __vector_as_bytebuffer(6, 4);
    }

    public static int createRequestUpdateFeatureTag(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addFeatureTagList(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestUpdateFeatureTag(flatBufferBuilder);
    }

    public static void startRequestUpdateFeatureTag(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addFeatureTagList(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int createFeatureTagListVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addInt(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startFeatureTagListVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endRequestUpdateFeatureTag(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
