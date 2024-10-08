package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ConfCallChanged extends Table {
    public static ConfCallChanged getRootAsConfCallChanged(ByteBuffer byteBuffer) {
        return getRootAsConfCallChanged(byteBuffer, new ConfCallChanged());
    }

    public static ConfCallChanged getRootAsConfCallChanged(ByteBuffer byteBuffer, ConfCallChanged confCallChanged) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return confCallChanged.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ConfCallChanged __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long session() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int event() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public Participant participants(int i) {
        return participants(new Participant(), i);
    }

    public Participant participants(Participant participant, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return participant.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int participantsLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createConfCallChanged(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addParticipants(flatBufferBuilder, i2);
        addEvent(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j);
        return endConfCallChanged(flatBufferBuilder);
    }

    public static void startConfCallChanged(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addEvent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addParticipants(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
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

    public static int endConfCallChanged(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
