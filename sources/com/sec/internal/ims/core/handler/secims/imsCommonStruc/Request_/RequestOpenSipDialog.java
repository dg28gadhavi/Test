package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestOpenSipDialog extends Table {
    public static RequestOpenSipDialog getRootAsRequestOpenSipDialog(ByteBuffer byteBuffer) {
        return getRootAsRequestOpenSipDialog(byteBuffer, new RequestOpenSipDialog());
    }

    public static RequestOpenSipDialog getRootAsRequestOpenSipDialog(ByteBuffer byteBuffer, RequestOpenSipDialog requestOpenSipDialog) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestOpenSipDialog.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestOpenSipDialog __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public boolean isRequired() {
        int __offset = __offset(4);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestOpenSipDialog(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.startObject(1);
        addIsRequired(flatBufferBuilder, z);
        return endRequestOpenSipDialog(flatBufferBuilder);
    }

    public static void startRequestOpenSipDialog(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addIsRequired(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(0, z, false);
    }

    public static int endRequestOpenSipDialog(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
