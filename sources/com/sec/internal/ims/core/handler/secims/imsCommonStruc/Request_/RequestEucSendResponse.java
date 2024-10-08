package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestEucSendResponse extends Table {
    public static RequestEucSendResponse getRootAsRequestEucSendResponse(ByteBuffer byteBuffer) {
        return getRootAsRequestEucSendResponse(byteBuffer, new RequestEucSendResponse());
    }

    public static RequestEucSendResponse getRootAsRequestEucSendResponse(ByteBuffer byteBuffer, RequestEucSendResponse requestEucSendResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestEucSendResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestEucSendResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public int value() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String id() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public int type() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String pin() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer pinAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String remoteUri() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createRequestEucSendResponse(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(6);
        addRemoteUri(flatBufferBuilder, i5);
        addPin(flatBufferBuilder, i4);
        addType(flatBufferBuilder, i3);
        addId(flatBufferBuilder, i2);
        addValue(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestEucSendResponse(flatBufferBuilder);
    }

    public static void startRequestEucSendResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addValue(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static void addPin(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endRequestEucSendResponse(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 14);
        return endObject;
    }
}
