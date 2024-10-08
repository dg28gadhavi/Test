package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestModifyCallType extends Table {
    public static RequestModifyCallType getRootAsRequestModifyCallType(ByteBuffer byteBuffer) {
        return getRootAsRequestModifyCallType(byteBuffer, new RequestModifyCallType());
    }

    public static RequestModifyCallType getRootAsRequestModifyCallType(ByteBuffer byteBuffer, RequestModifyCallType requestModifyCallType) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestModifyCallType.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestModifyCallType __assign(int i, ByteBuffer byteBuffer) {
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

    public int oldType() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int newType() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean isSdToSdPull() {
        int __offset = __offset(10);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String idcExtra() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idcExtraAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestModifyCallType(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, boolean z, int i3) {
        flatBufferBuilder.startObject(5);
        addIdcExtra(flatBufferBuilder, i3);
        addNewType(flatBufferBuilder, i2);
        addOldType(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j);
        addIsSdToSdPull(flatBufferBuilder, z);
        return endRequestModifyCallType(flatBufferBuilder);
    }

    public static void startRequestModifyCallType(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addOldType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addNewType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addIsSdToSdPull(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(3, z, false);
    }

    public static void addIdcExtra(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endRequestModifyCallType(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
