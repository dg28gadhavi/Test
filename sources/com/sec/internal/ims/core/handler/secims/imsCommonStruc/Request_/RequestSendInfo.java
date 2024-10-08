package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendInfo extends Table {
    public static RequestSendInfo getRootAsRequestSendInfo(ByteBuffer byteBuffer) {
        return getRootAsRequestSendInfo(byteBuffer, new RequestSendInfo());
    }

    public static RequestSendInfo getRootAsRequestSendInfo(ByteBuffer byteBuffer, RequestSendInfo requestSendInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendInfo __assign(int i, ByteBuffer byteBuffer) {
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

    public int callType() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public long ussdType() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public AdditionalContents additionalContents() {
        return additionalContents(new AdditionalContents());
    }

    public AdditionalContents additionalContents(AdditionalContents additionalContents) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return additionalContents.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createRequestSendInfo(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i, long j3, int i2) {
        flatBufferBuilder.startObject(5);
        addAdditionalContents(flatBufferBuilder, i2);
        addUssdType(flatBufferBuilder, j3);
        addCallType(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endRequestSendInfo(flatBufferBuilder);
    }

    public static void startRequestSendInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addCallType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addUssdType(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endRequestSendInfo(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
