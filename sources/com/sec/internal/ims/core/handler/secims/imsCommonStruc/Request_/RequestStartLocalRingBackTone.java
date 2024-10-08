package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartLocalRingBackTone extends Table {
    public static RequestStartLocalRingBackTone getRootAsRequestStartLocalRingBackTone(ByteBuffer byteBuffer) {
        return getRootAsRequestStartLocalRingBackTone(byteBuffer, new RequestStartLocalRingBackTone());
    }

    public static RequestStartLocalRingBackTone getRootAsRequestStartLocalRingBackTone(ByteBuffer byteBuffer, RequestStartLocalRingBackTone requestStartLocalRingBackTone) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestStartLocalRingBackTone.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestStartLocalRingBackTone __assign(int i, ByteBuffer byteBuffer) {
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

    public long streamType() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long volume() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long toneType() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestStartLocalRingBackTone(FlatBufferBuilder flatBufferBuilder, long j, long j2, long j3, long j4) {
        flatBufferBuilder.startObject(4);
        addToneType(flatBufferBuilder, j4);
        addVolume(flatBufferBuilder, j3);
        addStreamType(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endRequestStartLocalRingBackTone(flatBufferBuilder);
    }

    public static void startRequestStartLocalRingBackTone(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addStreamType(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addVolume(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addToneType(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static int endRequestStartLocalRingBackTone(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
