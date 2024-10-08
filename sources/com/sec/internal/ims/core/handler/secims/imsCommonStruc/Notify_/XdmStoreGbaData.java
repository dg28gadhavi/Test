package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XdmStoreGbaData extends Table {
    public static XdmStoreGbaData getRootAsXdmStoreGbaData(ByteBuffer byteBuffer) {
        return getRootAsXdmStoreGbaData(byteBuffer, new XdmStoreGbaData());
    }

    public static XdmStoreGbaData getRootAsXdmStoreGbaData(ByteBuffer byteBuffer, XdmStoreGbaData xdmStoreGbaData) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return xdmStoreGbaData.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public XdmStoreGbaData __assign(int i, ByteBuffer byteBuffer) {
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

    public String btid() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer btidAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String lifetime() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer lifetimeAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String gbaType() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer gbaTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String nafFqdn() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer nafFqdnAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public int hexProtocolId(int i) {
        int __offset = __offset(16);
        if (__offset != 0) {
            return this.bb.get(__vector(__offset) + (i * 1)) & 255;
        }
        return 0;
    }

    public int hexProtocolIdLength() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer hexProtocolIdAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public static int createXdmStoreGbaData(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5, int i6) {
        flatBufferBuilder.startObject(7);
        addHexProtocolId(flatBufferBuilder, i6);
        addNafFqdn(flatBufferBuilder, i5);
        addGbaType(flatBufferBuilder, i4);
        addLifetime(flatBufferBuilder, i3);
        addBtid(flatBufferBuilder, i2);
        addNonce(flatBufferBuilder, i);
        addRid(flatBufferBuilder, j);
        return endXdmStoreGbaData(flatBufferBuilder);
    }

    public static void startXdmStoreGbaData(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addRid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addNonce(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addBtid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addLifetime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addGbaType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addNafFqdn(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addHexProtocolId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
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

    public static int endXdmStoreGbaData(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        flatBufferBuilder.required(endObject, 14);
        flatBufferBuilder.required(endObject, 16);
        return endObject;
    }
}
