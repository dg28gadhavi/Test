package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AllowHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.RetryHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SessionStarted extends Table {
    public static SessionStarted getRootAsSessionStarted(ByteBuffer byteBuffer) {
        return getRootAsSessionStarted(byteBuffer, new SessionStarted());
    }

    public static SessionStarted getRootAsSessionStarted(ByteBuffer byteBuffer, SessionStarted sessionStarted) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sessionStarted.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SessionStarted __assign(int i, ByteBuffer byteBuffer) {
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

    public String sessionUri() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sessionUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public RetryHdr retryHdr() {
        return retryHdr(new RetryHdr());
    }

    public RetryHdr retryHdr(RetryHdr retryHdr) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return retryHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public WarningHdr warningHdr() {
        return warningHdr(new WarningHdr());
    }

    public WarningHdr warningHdr(WarningHdr warningHdr) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return warningHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public AllowHdr allowHdr() {
        return allowHdr(new AllowHdr());
    }

    public AllowHdr allowHdr(AllowHdr allowHdr) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return allowHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public boolean isMsgRevokeSupported() {
        int __offset = __offset(16);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isMsgFallbackSupported() {
        int __offset = __offset(18);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isChatbotRole() {
        int __offset = __offset(20);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String displayName() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public static int createSessionStarted(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5, boolean z, boolean z2, boolean z3, int i6) {
        flatBufferBuilder.startObject(10);
        addDisplayName(flatBufferBuilder, i6);
        addAllowHdr(flatBufferBuilder, i5);
        addWarningHdr(flatBufferBuilder, i4);
        addRetryHdr(flatBufferBuilder, i3);
        addSessionUri(flatBufferBuilder, i2);
        addImError(flatBufferBuilder, i);
        addSessionHandle(flatBufferBuilder, j);
        addIsChatbotRole(flatBufferBuilder, z3);
        addIsMsgFallbackSupported(flatBufferBuilder, z2);
        addIsMsgRevokeSupported(flatBufferBuilder, z);
        return endSessionStarted(flatBufferBuilder);
    }

    public static void startSessionStarted(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(10);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addSessionUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addRetryHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addAllowHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addIsMsgRevokeSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(6, z, false);
    }

    public static void addIsMsgFallbackSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(7, z, false);
    }

    public static void addIsChatbotRole(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(8, z, false);
    }

    public static void addDisplayName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static int endSessionStarted(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
