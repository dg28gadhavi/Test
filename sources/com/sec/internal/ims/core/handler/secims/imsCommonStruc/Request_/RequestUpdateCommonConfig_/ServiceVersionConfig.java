package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ServiceVersionConfig extends Table {
    public static ServiceVersionConfig getRootAsServiceVersionConfig(ByteBuffer byteBuffer) {
        return getRootAsServiceVersionConfig(byteBuffer, new ServiceVersionConfig());
    }

    public static ServiceVersionConfig getRootAsServiceVersionConfig(ByteBuffer byteBuffer, ServiceVersionConfig serviceVersionConfig) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return serviceVersionConfig.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ServiceVersionConfig __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public ExtraHeader extraHeaders() {
        return extraHeaders(new ExtraHeader());
    }

    public ExtraHeader extraHeaders(ExtraHeader extraHeader) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return extraHeader.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createServiceVersionConfig(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addExtraHeaders(flatBufferBuilder, i);
        return endServiceVersionConfig(flatBufferBuilder);
    }

    public static void startServiceVersionConfig(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addExtraHeaders(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int endServiceVersionConfig(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
