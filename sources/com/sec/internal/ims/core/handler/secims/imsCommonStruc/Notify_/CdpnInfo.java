package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CdpnInfo extends Table {
    public static CdpnInfo getRootAsCdpnInfo(ByteBuffer byteBuffer) {
        return getRootAsCdpnInfo(byteBuffer, new CdpnInfo());
    }

    public static CdpnInfo getRootAsCdpnInfo(ByteBuffer byteBuffer, CdpnInfo cdpnInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return cdpnInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public CdpnInfo __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String calledPartyNumber() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer calledPartyNumberAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public static int createCdpnInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addCalledPartyNumber(flatBufferBuilder, i);
        return endCdpnInfo(flatBufferBuilder);
    }

    public static void startCdpnInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addCalledPartyNumber(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int endCdpnInfo(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
