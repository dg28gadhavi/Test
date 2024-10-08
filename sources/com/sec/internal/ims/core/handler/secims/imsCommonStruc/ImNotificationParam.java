package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImNotificationParam extends Table {
    public static ImNotificationParam getRootAsImNotificationParam(ByteBuffer byteBuffer) {
        return getRootAsImNotificationParam(byteBuffer, new ImNotificationParam());
    }

    public static ImNotificationParam getRootAsImNotificationParam(ByteBuffer byteBuffer, ImNotificationParam imNotificationParam) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imNotificationParam.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImNotificationParam __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String imdnMessageId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String imdnDateTime() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imdnDateTimeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int status(int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__vector(__offset) + (i * 4));
        }
        return 0;
    }

    public int statusLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer statusAsByteBuffer() {
        return __vector_as_bytebuffer(8, 4);
    }

    public String imdnOriginalTo() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imdnOriginalToAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public ImdnRecRoute imdnRecRoute(int i) {
        return imdnRecRoute(new ImdnRecRoute(), i);
    }

    public ImdnRecRoute imdnRecRoute(ImdnRecRoute imdnRecRoute, int i) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return imdnRecRoute.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int imdnRecRouteLength() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createImNotificationParam(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(5);
        addImdnRecRoute(flatBufferBuilder, i5);
        addImdnOriginalTo(flatBufferBuilder, i4);
        addStatus(flatBufferBuilder, i3);
        addImdnDateTime(flatBufferBuilder, i2);
        addImdnMessageId(flatBufferBuilder, i);
        return endImNotificationParam(flatBufferBuilder);
    }

    public static void startImNotificationParam(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addImdnMessageId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addImdnDateTime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createStatusVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addInt(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startStatusVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addImdnOriginalTo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addImdnRecRoute(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int createImdnRecRouteVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startImdnRecRouteVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endImNotificationParam(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
