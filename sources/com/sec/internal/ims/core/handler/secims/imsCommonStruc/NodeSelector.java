package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NodeSelector extends Table {
    public static NodeSelector getRootAsNodeSelector(ByteBuffer byteBuffer) {
        return getRootAsNodeSelector(byteBuffer, new NodeSelector());
    }

    public static NodeSelector getRootAsNodeSelector(ByteBuffer byteBuffer, NodeSelector nodeSelector) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return nodeSelector.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public NodeSelector __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String node() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer nodeAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public long pos() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String attr() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer attrAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String attrVal() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer attrValAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createNodeSelector(FlatBufferBuilder flatBufferBuilder, int i, long j, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addAttrVal(flatBufferBuilder, i3);
        addAttr(flatBufferBuilder, i2);
        addPos(flatBufferBuilder, j);
        addNode(flatBufferBuilder, i);
        return endNodeSelector(flatBufferBuilder);
    }

    public static void startNodeSelector(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addNode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addPos(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addAttr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addAttrVal(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endNodeSelector(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
