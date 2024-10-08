package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConferenceInfoUpdated_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImParticipantInfo extends Table {
    public static ImParticipantInfo getRootAsImParticipantInfo(ByteBuffer byteBuffer) {
        return getRootAsImParticipantInfo(byteBuffer, new ImParticipantInfo());
    }

    public static ImParticipantInfo getRootAsImParticipantInfo(ByteBuffer byteBuffer, ImParticipantInfo imParticipantInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imParticipantInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImParticipantInfo __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String uri() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public boolean isOwn() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int status() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int reason() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createImParticipantInfo(FlatBufferBuilder flatBufferBuilder, int i, boolean z, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addReason(flatBufferBuilder, i3);
        addStatus(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        addIsOwn(flatBufferBuilder, z);
        return endImParticipantInfo(flatBufferBuilder);
    }

    public static void startImParticipantInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addIsOwn(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static int endImParticipantInfo(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
