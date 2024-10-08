package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.XdmGeneralResponse_.Pair;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XdmGeneralResponse extends Table {
    public static XdmGeneralResponse getRootAsXdmGeneralResponse(ByteBuffer byteBuffer) {
        return getRootAsXdmGeneralResponse(byteBuffer, new XdmGeneralResponse());
    }

    public static XdmGeneralResponse getRootAsXdmGeneralResponse(ByteBuffer byteBuffer, XdmGeneralResponse xdmGeneralResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return xdmGeneralResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public XdmGeneralResponse __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long rid() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean success() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String reason() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reasonAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public int statusCode() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String etag() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer etagAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String retryAfter() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer retryAfterAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public Pair result(int i) {
        return result(new Pair(), i);
    }

    public Pair result(Pair pair, int i) {
        int __offset = __offset(16);
        if (__offset != 0) {
            return pair.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int resultLength() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createXdmGeneralResponse(FlatBufferBuilder flatBufferBuilder, long j, boolean z, int i, int i2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(7);
        addResult(flatBufferBuilder, i5);
        addRetryAfter(flatBufferBuilder, i4);
        addEtag(flatBufferBuilder, i3);
        addStatusCode(flatBufferBuilder, i2);
        addReason(flatBufferBuilder, i);
        addRid(flatBufferBuilder, j);
        addSuccess(flatBufferBuilder, z);
        return endXdmGeneralResponse(flatBufferBuilder);
    }

    public static void startXdmGeneralResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addRid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSuccess(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addStatusCode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static void addEtag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addRetryAfter(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addResult(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int createResultVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startResultVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endXdmGeneralResponse(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
