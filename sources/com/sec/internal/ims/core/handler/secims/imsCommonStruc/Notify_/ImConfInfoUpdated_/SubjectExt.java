package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SubjectExt extends Table {
    public static SubjectExt getRootAsSubjectExt(ByteBuffer byteBuffer) {
        return getRootAsSubjectExt(byteBuffer, new SubjectExt());
    }

    public static SubjectExt getRootAsSubjectExt(ByteBuffer byteBuffer, SubjectExt subjectExt) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return subjectExt.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SubjectExt __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String subject() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String participant() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer participantAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String timestamp() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createSubjectExt(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addTimestamp(flatBufferBuilder, i3);
        addParticipant(flatBufferBuilder, i2);
        addSubject(flatBufferBuilder, i);
        return endSubjectExt(flatBufferBuilder);
    }

    public static void startSubjectExt(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSubject(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addParticipant(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addTimestamp(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endSubjectExt(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
