package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateAudioInterface extends Table {
    public static RequestUpdateAudioInterface getRootAsRequestUpdateAudioInterface(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateAudioInterface(byteBuffer, new RequestUpdateAudioInterface());
    }

    public static RequestUpdateAudioInterface getRootAsRequestUpdateAudioInterface(ByteBuffer byteBuffer, RequestUpdateAudioInterface requestUpdateAudioInterface) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateAudioInterface.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateAudioInterface __assign(int i, ByteBuffer byteBuffer) {
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

    public String mode() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer modeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestUpdateAudioInterface(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addMode(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestUpdateAudioInterface(flatBufferBuilder);
    }

    public static void startRequestUpdateAudioInterface(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addMode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestUpdateAudioInterface(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
