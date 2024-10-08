package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPresenceSubscribe extends Table {
    public static RequestPresenceSubscribe getRootAsRequestPresenceSubscribe(ByteBuffer byteBuffer) {
        return getRootAsRequestPresenceSubscribe(byteBuffer, new RequestPresenceSubscribe());
    }

    public static RequestPresenceSubscribe getRootAsRequestPresenceSubscribe(ByteBuffer byteBuffer, RequestPresenceSubscribe requestPresenceSubscribe) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestPresenceSubscribe.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestPresenceSubscribe __assign(int i, ByteBuffer byteBuffer) {
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

    public String subscriptionId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscriptionIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String uri(int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int uriLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public boolean isAnonymous() {
        int __offset = __offset(10);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isListSubscribe() {
        int __offset = __offset(12);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean gzipEnable() {
        int __offset = __offset(14);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long expires() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestPresenceSubscribe(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, boolean z, boolean z2, boolean z3, long j2) {
        flatBufferBuilder.startObject(7);
        addExpires(flatBufferBuilder, j2);
        addUri(flatBufferBuilder, i2);
        addSubscriptionId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addGzipEnable(flatBufferBuilder, z3);
        addIsListSubscribe(flatBufferBuilder, z2);
        addIsAnonymous(flatBufferBuilder, z);
        return endRequestPresenceSubscribe(flatBufferBuilder);
    }

    public static void startRequestPresenceSubscribe(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createUriVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startUriVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addIsAnonymous(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(3, z, false);
    }

    public static void addIsListSubscribe(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(4, z, false);
    }

    public static void addGzipEnable(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(5, z, false);
    }

    public static void addExpires(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(6, (int) j, 0);
    }

    public static int endRequestPresenceSubscribe(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
