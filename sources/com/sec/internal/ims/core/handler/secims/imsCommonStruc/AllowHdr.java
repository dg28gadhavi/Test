package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AllowHdr extends Table {
    public static AllowHdr getRootAsAllowHdr(ByteBuffer byteBuffer) {
        return getRootAsAllowHdr(byteBuffer, new AllowHdr());
    }

    public static AllowHdr getRootAsAllowHdr(ByteBuffer byteBuffer, AllowHdr allowHdr) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return allowHdr.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public AllowHdr __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String text() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public static int createAllowHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addText(flatBufferBuilder, i);
        return endAllowHdr(flatBufferBuilder);
    }

    public static void startAllowHdr(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addText(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int endAllowHdr(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
