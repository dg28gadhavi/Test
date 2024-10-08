package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImdnParams extends Table {
    public static ImdnParams getRootAsImdnParams(ByteBuffer byteBuffer) {
        return getRootAsImdnParams(byteBuffer, new ImdnParams());
    }

    public static ImdnParams getRootAsImdnParams(ByteBuffer byteBuffer, ImdnParams imdnParams) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imdnParams.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImdnParams __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String messageId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer messageIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String datetime() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer datetimeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int noti(int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__vector(__offset) + (i * 4));
        }
        return 0;
    }

    public int notiLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer notiAsByteBuffer() {
        return __vector_as_bytebuffer(8, 4);
    }

    public ImdnRecRoute recRoute(int i) {
        return recRoute(new ImdnRecRoute(), i);
    }

    public ImdnRecRoute recRoute(ImdnRecRoute imdnRecRoute, int i) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return imdnRecRoute.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int recRouteLength() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String deviceId() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer deviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String originalToHdr() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer originalToHdrAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createImdnParams(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6) {
        flatBufferBuilder.startObject(6);
        addOriginalToHdr(flatBufferBuilder, i6);
        addDeviceId(flatBufferBuilder, i5);
        addRecRoute(flatBufferBuilder, i4);
        addNoti(flatBufferBuilder, i3);
        addDatetime(flatBufferBuilder, i2);
        addMessageId(flatBufferBuilder, i);
        return endImdnParams(flatBufferBuilder);
    }

    public static void startImdnParams(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addMessageId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addDatetime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addNoti(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createNotiVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addInt(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startNotiVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addRecRoute(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int createRecRouteVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startRecRouteVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addDeviceId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addOriginalToHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endImdnParams(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
