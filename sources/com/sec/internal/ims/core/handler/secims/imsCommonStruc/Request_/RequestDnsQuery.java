package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestDnsQuery extends Table {
    public static RequestDnsQuery getRootAsRequestDnsQuery(ByteBuffer byteBuffer) {
        return getRootAsRequestDnsQuery(byteBuffer, new RequestDnsQuery());
    }

    public static RequestDnsQuery getRootAsRequestDnsQuery(ByteBuffer byteBuffer, RequestDnsQuery requestDnsQuery) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestDnsQuery.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestDnsQuery __assign(int i, ByteBuffer byteBuffer) {
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

    public long netId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public String interfaceNw() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer interfaceNwAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String hostname() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer hostnameAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String dnsServerList(int i) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int dnsServerListLength() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String type() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer typeAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String transport() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer transportAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String family() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer familyAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public static int createRequestDnsQuery(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i, int i2, int i3, int i4, int i5, int i6) {
        flatBufferBuilder.startObject(8);
        addNetId(flatBufferBuilder, j2);
        addFamily(flatBufferBuilder, i6);
        addTransport(flatBufferBuilder, i5);
        addType(flatBufferBuilder, i4);
        addDnsServerList(flatBufferBuilder, i3);
        addHostname(flatBufferBuilder, i2);
        addInterfaceNw(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestDnsQuery(flatBufferBuilder);
    }

    public static void startRequestDnsQuery(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(8);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addNetId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(1, j, 0);
    }

    public static void addInterfaceNw(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addHostname(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addDnsServerList(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int createDnsServerListVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startDnsServerListVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addTransport(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addFamily(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static int endRequestDnsQuery(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
