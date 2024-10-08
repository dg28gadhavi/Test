package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PresenceServiceStatus extends Table {
    public static PresenceServiceStatus getRootAsPresenceServiceStatus(ByteBuffer byteBuffer) {
        return getRootAsPresenceServiceStatus(byteBuffer, new PresenceServiceStatus());
    }

    public static PresenceServiceStatus getRootAsPresenceServiceStatus(ByteBuffer byteBuffer, PresenceServiceStatus presenceServiceStatus) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return presenceServiceStatus.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public PresenceServiceStatus __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String serviceId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer serviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String version() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer versionAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String status() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer statusAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String mediaCapabilities(int i) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int mediaCapabilitiesLength() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createPresenceServiceStatus(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(4);
        addMediaCapabilities(flatBufferBuilder, i4);
        addStatus(flatBufferBuilder, i3);
        addVersion(flatBufferBuilder, i2);
        addServiceId(flatBufferBuilder, i);
        return endPresenceServiceStatus(flatBufferBuilder);
    }

    public static void startPresenceServiceStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addServiceId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addVersion(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addMediaCapabilities(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int createMediaCapabilitiesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startMediaCapabilitiesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endPresenceServiceStatus(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
