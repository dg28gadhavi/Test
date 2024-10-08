package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RegiConfig extends Table {
    public static RegiConfig getRootAsRegiConfig(ByteBuffer byteBuffer) {
        return getRootAsRegiConfig(byteBuffer, new RegiConfig());
    }

    public static RegiConfig getRootAsRegiConfig(ByteBuffer byteBuffer, RegiConfig regiConfig) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return regiConfig.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RegiConfig __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String imei() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imeiAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String supported() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer supportedAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String privacy() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer privacyAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRegiConfig(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addPrivacy(flatBufferBuilder, i3);
        addSupported(flatBufferBuilder, i2);
        addImei(flatBufferBuilder, i);
        return endRegiConfig(flatBufferBuilder);
    }

    public static void startRegiConfig(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addImei(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addSupported(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addPrivacy(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRegiConfig(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
