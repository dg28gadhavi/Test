package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImExtension;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class FtIncomingSession extends Table {
    public static FtIncomingSession getRootAsFtIncomingSession(ByteBuffer byteBuffer) {
        return getRootAsFtIncomingSession(byteBuffer, new FtIncomingSession());
    }

    public static FtIncomingSession getRootAsFtIncomingSession(ByteBuffer byteBuffer, FtIncomingSession ftIncomingSession) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return ftIncomingSession.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public FtIncomingSession __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public BaseSessionData session() {
        return session(new BaseSessionData());
    }

    public BaseSessionData session(BaseSessionData baseSessionData) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return baseSessionData.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public FtPayloadParam payload() {
        return payload(new FtPayloadParam());
    }

    public FtPayloadParam payload(FtPayloadParam ftPayloadParam) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ftPayloadParam.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public long userHandle() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public ImExtension extension() {
        return extension(new ImExtension());
    }

    public ImExtension extension(ImExtension imExtension) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return imExtension.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public boolean isLmm() {
        int __offset = __offset(12);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createFtIncomingSession(FlatBufferBuilder flatBufferBuilder, int i, int i2, long j, int i3, boolean z) {
        flatBufferBuilder.startObject(5);
        addExtension(flatBufferBuilder, i3);
        addUserHandle(flatBufferBuilder, j);
        addPayload(flatBufferBuilder, i2);
        addSession(flatBufferBuilder, i);
        addIsLmm(flatBufferBuilder, z);
        return endFtIncomingSession(flatBufferBuilder);
    }

    public static void startFtIncomingSession(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addPayload(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addUserHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addExtension(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addIsLmm(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(4, z, false);
    }

    public static int endFtIncomingSession(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
