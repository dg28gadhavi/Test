package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CshGeneralResponse extends Table {
    public static CshGeneralResponse getRootAsCshGeneralResponse(ByteBuffer byteBuffer) {
        return getRootAsCshGeneralResponse(byteBuffer, new CshGeneralResponse());
    }

    public static CshGeneralResponse getRootAsCshGeneralResponse(ByteBuffer byteBuffer, CshGeneralResponse cshGeneralResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return cshGeneralResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public CshGeneralResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public int error() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createCshGeneralResponse(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addError(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endCshGeneralResponse(flatBufferBuilder);
    }

    public static void startCshGeneralResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static int endCshGeneralResponse(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
