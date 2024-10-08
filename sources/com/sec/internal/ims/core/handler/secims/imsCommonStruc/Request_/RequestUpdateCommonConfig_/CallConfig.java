package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CallConfig extends Table {
    public static CallConfig getRootAsCallConfig(ByteBuffer byteBuffer) {
        return getRootAsCallConfig(byteBuffer, new CallConfig());
    }

    public static CallConfig getRootAsCallConfig(ByteBuffer byteBuffer, CallConfig callConfig) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return callConfig.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public CallConfig __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public boolean ttySessionRequired() {
        int __offset = __offset(4);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean rttSessionRequired() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean automaticMode() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createCallConfig(FlatBufferBuilder flatBufferBuilder, boolean z, boolean z2, boolean z3) {
        flatBufferBuilder.startObject(3);
        addAutomaticMode(flatBufferBuilder, z3);
        addRttSessionRequired(flatBufferBuilder, z2);
        addTtySessionRequired(flatBufferBuilder, z);
        return endCallConfig(flatBufferBuilder);
    }

    public static void startCallConfig(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addTtySessionRequired(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(0, z, false);
    }

    public static void addRttSessionRequired(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static void addAutomaticMode(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static int endCallConfig(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
