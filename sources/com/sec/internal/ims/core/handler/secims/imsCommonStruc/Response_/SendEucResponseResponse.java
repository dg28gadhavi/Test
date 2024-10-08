package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SendEucResponseResponse extends Table {
    public static SendEucResponseResponse getRootAsSendEucResponseResponse(ByteBuffer byteBuffer) {
        return getRootAsSendEucResponseResponse(byteBuffer, new SendEucResponseResponse());
    }

    public static SendEucResponseResponse getRootAsSendEucResponseResponse(ByteBuffer byteBuffer, SendEucResponseResponse sendEucResponseResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sendEucResponseResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SendEucResponseResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public String id() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int type() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String remoteUri() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public int status() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createSendEucResponseResponse(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(5);
        addStatus(flatBufferBuilder, i4);
        addRemoteUri(flatBufferBuilder, i3);
        addType(flatBufferBuilder, i2);
        addId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endSendEucResponseResponse(flatBufferBuilder);
    }

    public static void startSendEucResponseResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(4, i, 0);
    }

    public static int endSendEucResponseResponse(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
