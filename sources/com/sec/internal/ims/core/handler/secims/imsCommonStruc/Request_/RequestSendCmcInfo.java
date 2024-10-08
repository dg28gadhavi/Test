package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendCmcInfo extends Table {
    public static RequestSendCmcInfo getRootAsRequestSendCmcInfo(ByteBuffer byteBuffer) {
        return getRootAsRequestSendCmcInfo(byteBuffer, new RequestSendCmcInfo());
    }

    public static RequestSendCmcInfo getRootAsRequestSendCmcInfo(ByteBuffer byteBuffer, RequestSendCmcInfo requestSendCmcInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendCmcInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendCmcInfo __assign(int i, ByteBuffer byteBuffer) {
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

    public long session() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public AdditionalContents additionalContents() {
        return additionalContents(new AdditionalContents());
    }

    public AdditionalContents additionalContents(AdditionalContents additionalContents) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return additionalContents.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createRequestSendCmcInfo(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i) {
        flatBufferBuilder.startObject(3);
        addAdditionalContents(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endRequestSendCmcInfo(flatBufferBuilder);
    }

    public static void startRequestSendCmcInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRequestSendCmcInfo(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
