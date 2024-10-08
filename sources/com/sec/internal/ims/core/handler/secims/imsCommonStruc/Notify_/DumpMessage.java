package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DumpMessage extends Table {
    public static DumpMessage getRootAsDumpMessage(ByteBuffer byteBuffer) {
        return getRootAsDumpMessage(byteBuffer, new DumpMessage());
    }

    public static DumpMessage getRootAsDumpMessage(ByteBuffer byteBuffer, DumpMessage dumpMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return dumpMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public DumpMessage __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String tag() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer tagAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String value() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer valueAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean secure() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createDumpMessage(FlatBufferBuilder flatBufferBuilder, int i, int i2, boolean z) {
        flatBufferBuilder.startObject(3);
        addValue(flatBufferBuilder, i2);
        addTag(flatBufferBuilder, i);
        addSecure(flatBufferBuilder, z);
        return endDumpMessage(flatBufferBuilder);
    }

    public static void startDumpMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addTag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addValue(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addSecure(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static int endDumpMessage(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
