package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImComposingStatus;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImComposingStatusReceived extends Table {
    public static ImComposingStatusReceived getRootAsImComposingStatusReceived(ByteBuffer byteBuffer) {
        return getRootAsImComposingStatusReceived(byteBuffer, new ImComposingStatusReceived());
    }

    public static ImComposingStatusReceived getRootAsImComposingStatusReceived(ByteBuffer byteBuffer, ImComposingStatusReceived imComposingStatusReceived) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imComposingStatusReceived.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImComposingStatusReceived __assign(int i, ByteBuffer byteBuffer) {
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

    public String uri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public ImComposingStatus status() {
        return status(new ImComposingStatus());
    }

    public ImComposingStatus status(ImComposingStatus imComposingStatus) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return imComposingStatus.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String userAlias() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createImComposingStatusReceived(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addUserAlias(flatBufferBuilder, i3);
        addStatus(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endImComposingStatusReceived(flatBufferBuilder);
    }

    public static void startImComposingStatusReceived(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endImComposingStatusReceived(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
