package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SipdialogGeneralResponse extends Table {
    public static SipdialogGeneralResponse getRootAsSipdialogGeneralResponse(ByteBuffer byteBuffer) {
        return getRootAsSipdialogGeneralResponse(byteBuffer, new SipdialogGeneralResponse());
    }

    public static SipdialogGeneralResponse getRootAsSipdialogGeneralResponse(ByteBuffer byteBuffer, SipdialogGeneralResponse sipdialogGeneralResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sipdialogGeneralResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SipdialogGeneralResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public boolean success() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String sipmessage() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sipmessageAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createSipdialogGeneralResponse(FlatBufferBuilder flatBufferBuilder, long j, boolean z, int i) {
        flatBufferBuilder.startObject(3);
        addSipmessage(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addSuccess(flatBufferBuilder, z);
        return endSipdialogGeneralResponse(flatBufferBuilder);
    }

    public static void startSipdialogGeneralResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSuccess(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static void addSipmessage(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endSipdialogGeneralResponse(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
