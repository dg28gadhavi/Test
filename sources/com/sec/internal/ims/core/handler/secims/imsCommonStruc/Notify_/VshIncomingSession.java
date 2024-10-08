package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class VshIncomingSession extends Table {
    public static VshIncomingSession getRootAsVshIncomingSession(ByteBuffer byteBuffer) {
        return getRootAsVshIncomingSession(byteBuffer, new VshIncomingSession());
    }

    public static VshIncomingSession getRootAsVshIncomingSession(ByteBuffer byteBuffer, VshIncomingSession vshIncomingSession) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return vshIncomingSession.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public VshIncomingSession __assign(int i, ByteBuffer byteBuffer) {
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

    public String remoteUri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int type() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String fileName() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer fileNameAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public long userHandle() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createVshIncomingSession(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, long j2) {
        flatBufferBuilder.startObject(5);
        addUserHandle(flatBufferBuilder, j2);
        addFileName(flatBufferBuilder, i3);
        addType(flatBufferBuilder, i2);
        addRemoteUri(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endVshIncomingSession(flatBufferBuilder);
    }

    public static void startVshIncomingSession(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addFileName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addUserHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(4, (int) j, 0);
    }

    public static int endVshIncomingSession(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
