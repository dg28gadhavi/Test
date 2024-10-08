package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSetVideoCrtAudio extends Table {
    public static RequestSetVideoCrtAudio getRootAsRequestSetVideoCrtAudio(ByteBuffer byteBuffer) {
        return getRootAsRequestSetVideoCrtAudio(byteBuffer, new RequestSetVideoCrtAudio());
    }

    public static RequestSetVideoCrtAudio getRootAsRequestSetVideoCrtAudio(ByteBuffer byteBuffer, RequestSetVideoCrtAudio requestSetVideoCrtAudio) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSetVideoCrtAudio.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSetVideoCrtAudio __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long handle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long session() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean on() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestSetVideoCrtAudio(FlatBufferBuilder flatBufferBuilder, long j, long j2, boolean z) {
        flatBufferBuilder.startObject(3);
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        addOn(flatBufferBuilder, z);
        return endRequestSetVideoCrtAudio(flatBufferBuilder);
    }

    public static void startRequestSetVideoCrtAudio(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addOn(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static int endRequestSetVideoCrtAudio(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
