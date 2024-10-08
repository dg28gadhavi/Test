package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestModifyVideoQuality extends Table {
    public static RequestModifyVideoQuality getRootAsRequestModifyVideoQuality(ByteBuffer byteBuffer) {
        return getRootAsRequestModifyVideoQuality(byteBuffer, new RequestModifyVideoQuality());
    }

    public static RequestModifyVideoQuality getRootAsRequestModifyVideoQuality(ByteBuffer byteBuffer, RequestModifyVideoQuality requestModifyVideoQuality) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestModifyVideoQuality.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestModifyVideoQuality __assign(int i, ByteBuffer byteBuffer) {
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

    public int oldQual() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int newQual() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createRequestModifyVideoQuality(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addNewQual(flatBufferBuilder, i2);
        addOldQual(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j);
        return endRequestModifyVideoQuality(flatBufferBuilder);
    }

    public static void startRequestModifyVideoQuality(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addOldQual(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addNewQual(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static int endRequestModifyVideoQuality(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
