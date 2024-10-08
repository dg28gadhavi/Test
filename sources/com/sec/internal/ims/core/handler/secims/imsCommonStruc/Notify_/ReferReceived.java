package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReferReceived extends Table {
    public static ReferReceived getRootAsReferReceived(ByteBuffer byteBuffer) {
        return getRootAsReferReceived(byteBuffer, new ReferReceived());
    }

    public static ReferReceived getRootAsReferReceived(ByteBuffer byteBuffer, ReferReceived referReceived) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return referReceived.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ReferReceived __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long handle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long session() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String referorUri() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer referorUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String targetUri() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer targetUriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createReferReceived(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i, int i2) {
        flatBufferBuilder.startObject(4);
        addTargetUri(flatBufferBuilder, i2);
        addReferorUri(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endReferReceived(flatBufferBuilder);
    }

    public static void startReferReceived(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addReferorUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addTargetUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endReferReceived(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
