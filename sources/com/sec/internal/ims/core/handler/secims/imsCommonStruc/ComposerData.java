package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ComposerData extends Table {
    public static ComposerData getRootAsComposerData(ByteBuffer byteBuffer) {
        return getRootAsComposerData(byteBuffer, new ComposerData());
    }

    public static ComposerData getRootAsComposerData(ByteBuffer byteBuffer, ComposerData composerData) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return composerData.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ComposerData __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public boolean importance() {
        int __offset = __offset(4);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String subject() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String image() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imageAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String callReason() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer callReasonAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String latitude() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer latitudeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String longitude() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer longitudeAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String radius() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer radiusAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public static int createComposerData(FlatBufferBuilder flatBufferBuilder, boolean z, int i, int i2, int i3, int i4, int i5, int i6) {
        flatBufferBuilder.startObject(7);
        addRadius(flatBufferBuilder, i6);
        addLongitude(flatBufferBuilder, i5);
        addLatitude(flatBufferBuilder, i4);
        addCallReason(flatBufferBuilder, i3);
        addImage(flatBufferBuilder, i2);
        addSubject(flatBufferBuilder, i);
        addImportance(flatBufferBuilder, z);
        return endComposerData(flatBufferBuilder);
    }

    public static void startComposerData(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addImportance(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(0, z, false);
    }

    public static void addSubject(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addImage(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addCallReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addLatitude(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addLongitude(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addRadius(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int endComposerData(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
