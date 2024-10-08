package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SessionClosed extends Table {
    public static SessionClosed getRootAsSessionClosed(ByteBuffer byteBuffer) {
        return getRootAsSessionClosed(byteBuffer, new SessionClosed());
    }

    public static SessionClosed getRootAsSessionClosed(ByteBuffer byteBuffer, SessionClosed sessionClosed) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sessionClosed.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SessionClosed __assign(int i, ByteBuffer byteBuffer) {
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

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError imError) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return imError.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public ReasonHdr reasonHdr() {
        return reasonHdr(new ReasonHdr());
    }

    public ReasonHdr reasonHdr(ReasonHdr reasonHdr) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return reasonHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String referredBy() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer referredByAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createSessionClosed(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addReferredBy(flatBufferBuilder, i3);
        addReasonHdr(flatBufferBuilder, i2);
        addImError(flatBufferBuilder, i);
        addSessionHandle(flatBufferBuilder, j);
        return endSessionClosed(flatBufferBuilder);
    }

    public static void startSessionClosed(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addReasonHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addReferredBy(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endSessionClosed(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
