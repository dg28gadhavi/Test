package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XdmAuth extends Table {
    public static XdmAuth getRootAsXdmAuth(ByteBuffer byteBuffer) {
        return getRootAsXdmAuth(byteBuffer, new XdmAuth());
    }

    public static XdmAuth getRootAsXdmAuth(ByteBuffer byteBuffer, XdmAuth xdmAuth) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return xdmAuth.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public XdmAuth __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String nonce() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer nonceAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public long recvMng() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createXdmAuth(FlatBufferBuilder flatBufferBuilder, long j, int i, long j2) {
        flatBufferBuilder.startObject(3);
        addRecvMng(flatBufferBuilder, j2);
        addNonce(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endXdmAuth(flatBufferBuilder);
    }

    public static void startXdmAuth(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addNonce(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addRecvMng(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static int endXdmAuth(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
