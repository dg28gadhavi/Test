package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DialogSubscribeStatus extends Table {
    public static DialogSubscribeStatus getRootAsDialogSubscribeStatus(ByteBuffer byteBuffer) {
        return getRootAsDialogSubscribeStatus(byteBuffer, new DialogSubscribeStatus());
    }

    public static DialogSubscribeStatus getRootAsDialogSubscribeStatus(ByteBuffer byteBuffer, DialogSubscribeStatus dialogSubscribeStatus) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return dialogSubscribeStatus.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public DialogSubscribeStatus __assign(int i, ByteBuffer byteBuffer) {
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

    public long statusCode() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String reasonPhrase() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reasonPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createDialogSubscribeStatus(FlatBufferBuilder flatBufferBuilder, long j, long j2, int i) {
        flatBufferBuilder.startObject(3);
        addReasonPhrase(flatBufferBuilder, i);
        addStatusCode(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endDialogSubscribeStatus(flatBufferBuilder);
    }

    public static void startDialogSubscribeStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addStatusCode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addReasonPhrase(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endDialogSubscribeStatus(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
