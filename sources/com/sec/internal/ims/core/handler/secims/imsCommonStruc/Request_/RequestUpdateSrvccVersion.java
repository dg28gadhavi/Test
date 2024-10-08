package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateSrvccVersion extends Table {
    public static RequestUpdateSrvccVersion getRootAsRequestUpdateSrvccVersion(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateSrvccVersion(byteBuffer, new RequestUpdateSrvccVersion());
    }

    public static RequestUpdateSrvccVersion getRootAsRequestUpdateSrvccVersion(ByteBuffer byteBuffer, RequestUpdateSrvccVersion requestUpdateSrvccVersion) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateSrvccVersion.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateSrvccVersion __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long phoneId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long version() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestUpdateSrvccVersion(FlatBufferBuilder flatBufferBuilder, long j, long j2) {
        flatBufferBuilder.startObject(2);
        addVersion(flatBufferBuilder, j2);
        addPhoneId(flatBufferBuilder, j);
        return endRequestUpdateSrvccVersion(flatBufferBuilder);
    }

    public static void startRequestUpdateSrvccVersion(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addVersion(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static int endRequestUpdateSrvccVersion(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
