package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XdmReqGbaData extends Table {
    public static XdmReqGbaData getRootAsXdmReqGbaData(ByteBuffer byteBuffer) {
        return getRootAsXdmReqGbaData(byteBuffer, new XdmReqGbaData());
    }

    public static XdmReqGbaData getRootAsXdmReqGbaData(ByteBuffer byteBuffer, XdmReqGbaData xdmReqGbaData) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return xdmReqGbaData.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public XdmReqGbaData __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long rid() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String nafFqdn() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer nafFqdnAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int hexProtocolId(int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.get(__vector(__offset) + (i * 1)) & 255;
        }
        return 0;
    }

    public int hexProtocolIdLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer hexProtocolIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createXdmReqGbaData(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addHexProtocolId(flatBufferBuilder, i2);
        addNafFqdn(flatBufferBuilder, i);
        addRid(flatBufferBuilder, j);
        return endXdmReqGbaData(flatBufferBuilder);
    }

    public static void startXdmReqGbaData(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addRid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addNafFqdn(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addHexProtocolId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createHexProtocolIdVector(FlatBufferBuilder flatBufferBuilder, byte[] bArr) {
        flatBufferBuilder.startVector(1, bArr.length, 1);
        for (int length = bArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addByte(bArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startHexProtocolIdVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(1, i, 1);
    }

    public static int endXdmReqGbaData(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
