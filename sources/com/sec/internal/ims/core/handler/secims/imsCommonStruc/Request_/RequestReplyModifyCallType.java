package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestReplyModifyCallType extends Table {
    public static RequestReplyModifyCallType getRootAsRequestReplyModifyCallType(ByteBuffer byteBuffer) {
        return getRootAsRequestReplyModifyCallType(byteBuffer, new RequestReplyModifyCallType());
    }

    public static RequestReplyModifyCallType getRootAsRequestReplyModifyCallType(ByteBuffer byteBuffer, RequestReplyModifyCallType requestReplyModifyCallType) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestReplyModifyCallType.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestReplyModifyCallType __assign(int i, ByteBuffer byteBuffer) {
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

    public int reqType() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int curType() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int repType() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String cmcCallTime() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cmcCallTimeAsByteBuffer() {
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

    public static int createRequestReplyModifyCallType(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(6);
        addIdcExtra(flatBufferBuilder, i5);
        addCmcCallTime(flatBufferBuilder, i4);
        addRepType(flatBufferBuilder, i3);
        addCurType(flatBufferBuilder, i2);
        addReqType(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j);
        return endRequestReplyModifyCallType(flatBufferBuilder);
    }

    public static void startRequestReplyModifyCallType(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addReqType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addCurType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addRepType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static void addCmcCallTime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addIdcExtra(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endRequestReplyModifyCallType(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
