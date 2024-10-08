package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CallSendCmcInfo extends Table {
    public static CallSendCmcInfo getRootAsCallSendCmcInfo(ByteBuffer byteBuffer) {
        return getRootAsCallSendCmcInfo(byteBuffer, new CallSendCmcInfo());
    }

    public static CallSendCmcInfo getRootAsCallSendCmcInfo(ByteBuffer byteBuffer, CallSendCmcInfo callSendCmcInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return callSendCmcInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public CallSendCmcInfo __assign(int i, ByteBuffer byteBuffer) {
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

    public long sessionId() {
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

    public static int createCallSendCmcInfo(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i) {
        flatBufferBuilder.startObject(3);
        addAdditionalContents(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endCallSendCmcInfo(flatBufferBuilder);
    }

    public static void startCallSendCmcInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endCallSendCmcInfo(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
