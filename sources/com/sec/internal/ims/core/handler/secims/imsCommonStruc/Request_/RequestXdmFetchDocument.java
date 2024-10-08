package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.NodeSelector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmFetchDocument extends Table {
    public static RequestXdmFetchDocument getRootAsRequestXdmFetchDocument(ByteBuffer byteBuffer) {
        return getRootAsRequestXdmFetchDocument(byteBuffer, new RequestXdmFetchDocument());
    }

    public static RequestXdmFetchDocument getRootAsRequestXdmFetchDocument(ByteBuffer byteBuffer, RequestXdmFetchDocument requestXdmFetchDocument) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestXdmFetchDocument.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestXdmFetchDocument __assign(int i, ByteBuffer byteBuffer) {
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

    public String impu() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int type() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String name() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer nameAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public NodeSelector nodeSelector() {
        return nodeSelector(new NodeSelector());
    }

    public NodeSelector nodeSelector(NodeSelector nodeSelector) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return nodeSelector.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String accessToken() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer accessTokenAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createRequestXdmFetchDocument(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(6);
        addAccessToken(flatBufferBuilder, i5);
        addNodeSelector(flatBufferBuilder, i4);
        addName(flatBufferBuilder, i3);
        addType(flatBufferBuilder, i2);
        addImpu(flatBufferBuilder, i);
        addRid(flatBufferBuilder, j);
        return endRequestXdmFetchDocument(flatBufferBuilder);
    }

    public static void startRequestXdmFetchDocument(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addRid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImpu(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addNodeSelector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addAccessToken(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endRequestXdmFetchDocument(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
