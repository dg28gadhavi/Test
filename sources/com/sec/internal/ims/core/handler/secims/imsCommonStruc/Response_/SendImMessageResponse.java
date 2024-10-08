package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SendImMessageResponse extends Table {
    public static SendImMessageResponse getRootAsSendImMessageResponse(ByteBuffer byteBuffer) {
        return getRootAsSendImMessageResponse(byteBuffer, new SendImMessageResponse());
    }

    public static SendImMessageResponse getRootAsSendImMessageResponse(ByteBuffer byteBuffer, SendImMessageResponse sendImMessageResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sendImMessageResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SendImMessageResponse __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String imdnMessageId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError imError) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return imError.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createSendImMessageResponse(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addImError(flatBufferBuilder, i2);
        addImdnMessageId(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endSendImMessageResponse(flatBufferBuilder);
    }

    public static void startSendImMessageResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImdnMessageId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endSendImMessageResponse(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
