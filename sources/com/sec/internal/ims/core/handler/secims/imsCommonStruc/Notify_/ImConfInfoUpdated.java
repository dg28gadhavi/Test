package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUser;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.Icon;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.SubjectExt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConfInfoUpdated extends Table {
    public static ImConfInfoUpdated getRootAsImConfInfoUpdated(ByteBuffer byteBuffer) {
        return getRootAsImConfInfoUpdated(byteBuffer, new ImConfInfoUpdated());
    }

    public static ImConfInfoUpdated getRootAsImConfInfoUpdated(ByteBuffer byteBuffer, ImConfInfoUpdated imConfInfoUpdated) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imConfInfoUpdated.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImConfInfoUpdated __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String state() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer stateAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public SubjectExt subjectData() {
        return subjectData(new SubjectExt());
    }

    public SubjectExt subjectData(SubjectExt subjectExt) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return subjectExt.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public long maxUserCnt() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public ImConfUser users(int i) {
        return users(new ImConfUser(), i);
    }

    public ImConfUser users(ImConfUser imConfUser, int i) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return imConfUser.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int usersLength() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public Icon iconData() {
        return iconData(new Icon());
    }

    public Icon iconData(Icon icon) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return icon.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String timestamp() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public static int createImConfInfoUpdated(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, long j2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(7);
        addTimestamp(flatBufferBuilder, i5);
        addIconData(flatBufferBuilder, i4);
        addUsers(flatBufferBuilder, i3);
        addMaxUserCnt(flatBufferBuilder, j2);
        addSubjectData(flatBufferBuilder, i2);
        addState(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endImConfInfoUpdated(flatBufferBuilder);
    }

    public static void startImConfInfoUpdated(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addState(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addSubjectData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addMaxUserCnt(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addUsers(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int createUsersVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startUsersVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addIconData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addTimestamp(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int endImConfInfoUpdated(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
