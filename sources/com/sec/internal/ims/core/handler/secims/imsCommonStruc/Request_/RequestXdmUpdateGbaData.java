package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUpdateGbaData extends Table {
    public static RequestXdmUpdateGbaData getRootAsRequestXdmUpdateGbaData(ByteBuffer byteBuffer) {
        return getRootAsRequestXdmUpdateGbaData(byteBuffer, new RequestXdmUpdateGbaData());
    }

    public static RequestXdmUpdateGbaData getRootAsRequestXdmUpdateGbaData(ByteBuffer byteBuffer, RequestXdmUpdateGbaData requestXdmUpdateGbaData) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestXdmUpdateGbaData.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestXdmUpdateGbaData __assign(int i, ByteBuffer byteBuffer) {
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

    public String imsi() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imsiAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean gbaUiccSupported() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestXdmUpdateGbaData(FlatBufferBuilder flatBufferBuilder, long j, int i, boolean z) {
        flatBufferBuilder.startObject(3);
        addImsi(flatBufferBuilder, i);
        addRid(flatBufferBuilder, j);
        addGbaUiccSupported(flatBufferBuilder, z);
        return endRequestXdmUpdateGbaData(flatBufferBuilder);
    }

    public static void startRequestXdmUpdateGbaData(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addRid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImsi(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addGbaUiccSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static int endRequestXdmUpdateGbaData(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
