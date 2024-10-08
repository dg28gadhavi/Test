package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SlmSipResponseReceived extends Table {
    public static SlmSipResponseReceived getRootAsSlmSipResponseReceived(ByteBuffer byteBuffer) {
        return getRootAsSlmSipResponseReceived(byteBuffer, new SlmSipResponseReceived());
    }

    public static SlmSipResponseReceived getRootAsSlmSipResponseReceived(ByteBuffer byteBuffer, SlmSipResponseReceived slmSipResponseReceived) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return slmSipResponseReceived.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SlmSipResponseReceived __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionHandle() {
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

    public WarningHdr warningHdr() {
        return warningHdr(new WarningHdr());
    }

    public WarningHdr warningHdr(WarningHdr warningHdr) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return warningHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public ReasonHdr reasonHdr() {
        return reasonHdr(new ReasonHdr());
    }

    public ReasonHdr reasonHdr(ReasonHdr reasonHdr) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return reasonHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String passertedId() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer passertedIdAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createSlmSipResponseReceived(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(6);
        addPassertedId(flatBufferBuilder, i5);
        addReasonHdr(flatBufferBuilder, i4);
        addWarningHdr(flatBufferBuilder, i3);
        addImError(flatBufferBuilder, i2);
        addImdnMessageId(flatBufferBuilder, i);
        addSessionHandle(flatBufferBuilder, j);
        return endSlmSipResponseReceived(flatBufferBuilder);
    }

    public static void startSlmSipResponseReceived(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImdnMessageId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addReasonHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addPassertedId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endSlmSipResponseReceived(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
