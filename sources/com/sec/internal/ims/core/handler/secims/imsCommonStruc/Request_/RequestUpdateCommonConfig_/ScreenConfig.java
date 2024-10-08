package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ScreenConfig extends Table {
    public static ScreenConfig getRootAsScreenConfig(ByteBuffer byteBuffer) {
        return getRootAsScreenConfig(byteBuffer, new ScreenConfig());
    }

    public static ScreenConfig getRootAsScreenConfig(ByteBuffer byteBuffer, ScreenConfig screenConfig) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return screenConfig.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ScreenConfig __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long on() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createScreenConfig(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addOn(flatBufferBuilder, j);
        return endScreenConfig(flatBufferBuilder);
    }

    public static void startScreenConfig(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addOn(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static int endScreenConfig(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
