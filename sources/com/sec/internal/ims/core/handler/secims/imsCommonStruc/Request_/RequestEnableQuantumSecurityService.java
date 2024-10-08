package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestEnableQuantumSecurityService extends Table {
    public static RequestEnableQuantumSecurityService getRootAsRequestEnableQuantumSecurityService(ByteBuffer byteBuffer) {
        return getRootAsRequestEnableQuantumSecurityService(byteBuffer, new RequestEnableQuantumSecurityService());
    }

    public static RequestEnableQuantumSecurityService getRootAsRequestEnableQuantumSecurityService(ByteBuffer byteBuffer, RequestEnableQuantumSecurityService requestEnableQuantumSecurityService) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestEnableQuantumSecurityService.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestEnableQuantumSecurityService __assign(int i, ByteBuffer byteBuffer) {
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

    public boolean enable() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestEnableQuantumSecurityService(FlatBufferBuilder flatBufferBuilder, long j, boolean z) {
        flatBufferBuilder.startObject(2);
        addSession(flatBufferBuilder, j);
        addEnable(flatBufferBuilder, z);
        return endRequestEnableQuantumSecurityService(flatBufferBuilder);
    }

    public static void startRequestEnableQuantumSecurityService(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addEnable(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static int endRequestEnableQuantumSecurityService(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
