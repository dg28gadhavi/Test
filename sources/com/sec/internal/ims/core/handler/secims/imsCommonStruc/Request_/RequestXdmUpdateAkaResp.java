package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUpdateAkaResp extends Table {
    public static RequestXdmUpdateAkaResp getRootAsRequestXdmUpdateAkaResp(ByteBuffer byteBuffer) {
        return getRootAsRequestXdmUpdateAkaResp(byteBuffer, new RequestXdmUpdateAkaResp());
    }

    public static RequestXdmUpdateAkaResp getRootAsRequestXdmUpdateAkaResp(ByteBuffer byteBuffer, RequestXdmUpdateAkaResp requestXdmUpdateAkaResp) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestXdmUpdateAkaResp.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestXdmUpdateAkaResp __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sid() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int akaResp(int i) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.get(__vector(__offset) + (i * 1)) & 255;
        }
        return 0;
    }

    public int akaRespLength() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer akaRespAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public long recvMng() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestXdmUpdateAkaResp(FlatBufferBuilder flatBufferBuilder, long j, int i, long j2) {
        flatBufferBuilder.startObject(3);
        addRecvMng(flatBufferBuilder, j2);
        addAkaResp(flatBufferBuilder, i);
        addSid(flatBufferBuilder, j);
        return endRequestXdmUpdateAkaResp(flatBufferBuilder);
    }

    public static void startRequestXdmUpdateAkaResp(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addAkaResp(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int createAkaRespVector(FlatBufferBuilder flatBufferBuilder, byte[] bArr) {
        flatBufferBuilder.startVector(1, bArr.length, 1);
        for (int length = bArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addByte(bArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startAkaRespVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(1, i, 1);
    }

    public static void addRecvMng(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static int endRequestXdmUpdateAkaResp(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
