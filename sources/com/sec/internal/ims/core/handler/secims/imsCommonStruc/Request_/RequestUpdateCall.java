package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateCall extends Table {
    public static RequestUpdateCall getRootAsRequestUpdateCall(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateCall(byteBuffer, new RequestUpdateCall());
    }

    public static RequestUpdateCall getRootAsRequestUpdateCall(ByteBuffer byteBuffer, RequestUpdateCall requestUpdateCall) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateCall.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateCall __assign(int i, ByteBuffer byteBuffer) {
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

    public int action() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int codecType() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public long cause() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String reasonText() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reasonTextAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String idcExtra() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idcExtraAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createRequestUpdateCall(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, long j2, int i3, int i4) {
        flatBufferBuilder.startObject(6);
        addIdcExtra(flatBufferBuilder, i4);
        addReasonText(flatBufferBuilder, i3);
        addCause(flatBufferBuilder, j2);
        addCodecType(flatBufferBuilder, i2);
        addAction(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j);
        return endRequestUpdateCall(flatBufferBuilder);
    }

    public static void startRequestUpdateCall(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addAction(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addCodecType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addCause(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addReasonText(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addIdcExtra(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endRequestUpdateCall(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
