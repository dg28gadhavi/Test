package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestOptionsCapExchange extends Table {
    public static RequestOptionsCapExchange getRootAsRequestOptionsCapExchange(ByteBuffer byteBuffer) {
        return getRootAsRequestOptionsCapExchange(byteBuffer, new RequestOptionsCapExchange());
    }

    public static RequestOptionsCapExchange getRootAsRequestOptionsCapExchange(ByteBuffer byteBuffer, RequestOptionsCapExchange requestOptionsCapExchange) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestOptionsCapExchange.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestOptionsCapExchange __assign(int i, ByteBuffer byteBuffer) {
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

    public int myFeatures(int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__vector(__offset) + (i * 4));
        }
        return 0;
    }

    public int myFeaturesLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer myFeaturesAsByteBuffer() {
        return __vector_as_bytebuffer(8, 4);
    }

    public String extFeature() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer extFeatureAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String capabilities(int i) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int capabilitiesLength() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createRequestOptionsCapExchange(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(5);
        addCapabilities(flatBufferBuilder, i4);
        addExtFeature(flatBufferBuilder, i3);
        addMyFeatures(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestOptionsCapExchange(flatBufferBuilder);
    }

    public static void startRequestOptionsCapExchange(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addMyFeatures(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createMyFeaturesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addInt(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startMyFeaturesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addExtFeature(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addCapabilities(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int createCapabilitiesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startCapabilitiesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endRequestOptionsCapExchange(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
