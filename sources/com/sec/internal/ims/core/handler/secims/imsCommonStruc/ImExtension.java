package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImExtension extends Table {
    public static ImExtension getRootAsImExtension(ByteBuffer byteBuffer) {
        return getRootAsImExtension(byteBuffer, new ImExtension());
    }

    public static ImExtension getRootAsImExtension(ByteBuffer byteBuffer, ImExtension imExtension) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imExtension.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImExtension __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public ExtraHeader sipExtensions() {
        return sipExtensions(new ExtraHeader());
    }

    public ExtraHeader sipExtensions(ExtraHeader extraHeader) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return extraHeader.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createImExtension(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addSipExtensions(flatBufferBuilder, i);
        return endImExtension(flatBufferBuilder);
    }

    public static void startImExtension(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addSipExtensions(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int endImExtension(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
