package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConferenceInfoUpdated_.ImParticipantInfo;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConferenceInfoUpdated extends Table {
    public static ImConferenceInfoUpdated getRootAsImConferenceInfoUpdated(ByteBuffer byteBuffer) {
        return getRootAsImConferenceInfoUpdated(byteBuffer, new ImConferenceInfoUpdated());
    }

    public static ImConferenceInfoUpdated getRootAsImConferenceInfoUpdated(ByteBuffer byteBuffer, ImConferenceInfoUpdated imConferenceInfoUpdated) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imConferenceInfoUpdated.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImConferenceInfoUpdated __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String sessionId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sessionIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
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

    public long maxUserCount() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public ImParticipantInfo participants(int i) {
        return participants(new ImParticipantInfo(), i);
    }

    public ImParticipantInfo participants(ImParticipantInfo imParticipantInfo, int i) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return imParticipantInfo.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int participantsLength() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createImConferenceInfoUpdated(FlatBufferBuilder flatBufferBuilder, int i, int i2, long j, int i3) {
        flatBufferBuilder.startObject(4);
        addParticipants(flatBufferBuilder, i3);
        addMaxUserCount(flatBufferBuilder, j);
        addSubject(flatBufferBuilder, i2);
        addSessionId(flatBufferBuilder, i);
        return endImConferenceInfoUpdated(flatBufferBuilder);
    }

    public static void startImConferenceInfoUpdated(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addSubject(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addMaxUserCount(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addParticipants(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int createParticipantsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startParticipantsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endImConferenceInfoUpdated(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
