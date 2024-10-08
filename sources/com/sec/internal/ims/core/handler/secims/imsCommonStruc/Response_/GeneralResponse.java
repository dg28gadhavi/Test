package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GeneralResponse extends Table {
    public static GeneralResponse getRootAsGeneralResponse(ByteBuffer byteBuffer) {
        return getRootAsGeneralResponse(byteBuffer, new GeneralResponse());
    }

    public static GeneralResponse getRootAsGeneralResponse(ByteBuffer byteBuffer, GeneralResponse generalResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return generalResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public GeneralResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public int result() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int reason() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public long sipError() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String errorStr() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer errorStrAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createGeneralResponse(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, long j2, int i3) {
        flatBufferBuilder.startObject(5);
        addErrorStr(flatBufferBuilder, i3);
        addSipError(flatBufferBuilder, j2);
        addReason(flatBufferBuilder, i2);
        addResult(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endGeneralResponse(flatBufferBuilder);
    }

    public static void startGeneralResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addResult(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addSipError(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addErrorStr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endGeneralResponse(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
