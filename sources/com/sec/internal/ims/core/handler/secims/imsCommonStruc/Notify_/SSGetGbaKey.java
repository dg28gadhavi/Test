package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SSGetGbaKey extends Table {
    public static SSGetGbaKey getRootAsSSGetGbaKey(ByteBuffer byteBuffer) {
        return getRootAsSSGetGbaKey(byteBuffer, new SSGetGbaKey());
    }

    public static SSGetGbaKey getRootAsSSGetGbaKey(ByteBuffer byteBuffer, SSGetGbaKey sSGetGbaKey) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sSGetGbaKey.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SSGetGbaKey __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String gbatype() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer gbatypeAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String ck() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer ckAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String ik() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer ikAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String nonce() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer nonceAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String lifetime() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer lifetimeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String btid() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer btidAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createSSGetGbaKey(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6) {
        flatBufferBuilder.startObject(6);
        addBtid(flatBufferBuilder, i6);
        addLifetime(flatBufferBuilder, i5);
        addNonce(flatBufferBuilder, i4);
        addIk(flatBufferBuilder, i3);
        addCk(flatBufferBuilder, i2);
        addGbatype(flatBufferBuilder, i);
        return endSSGetGbaKey(flatBufferBuilder);
    }

    public static void startSSGetGbaKey(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addGbatype(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addCk(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addIk(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addNonce(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addLifetime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addBtid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endSSGetGbaKey(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        flatBufferBuilder.required(endObject, 14);
        return endObject;
    }
}
