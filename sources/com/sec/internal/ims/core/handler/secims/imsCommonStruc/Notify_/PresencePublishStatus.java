package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PresencePublishStatus extends Table {
    public static PresencePublishStatus getRootAsPresencePublishStatus(ByteBuffer byteBuffer) {
        return getRootAsPresencePublishStatus(byteBuffer, new PresencePublishStatus());
    }

    public static PresencePublishStatus getRootAsPresencePublishStatus(ByteBuffer byteBuffer, PresencePublishStatus presencePublishStatus) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return presencePublishStatus.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public PresencePublishStatus __assign(int i, ByteBuffer byteBuffer) {
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

    public boolean isSuccess() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long remoteExpires() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long sipErrorCode() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String sipErrorPhrase() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sipErrorPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String etag() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer etagAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public long minExpires() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isRefresh() {
        int __offset = __offset(18);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long retryAfter() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createPresencePublishStatus(FlatBufferBuilder flatBufferBuilder, long j, boolean z, long j2, long j3, int i, int i2, long j4, boolean z2, long j5) {
        flatBufferBuilder.startObject(9);
        addRetryAfter(flatBufferBuilder, j5);
        addMinExpires(flatBufferBuilder, j4);
        addEtag(flatBufferBuilder, i2);
        addSipErrorPhrase(flatBufferBuilder, i);
        addSipErrorCode(flatBufferBuilder, j3);
        addRemoteExpires(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        addIsRefresh(flatBufferBuilder, z2);
        addIsSuccess(flatBufferBuilder, z);
        return endPresencePublishStatus(flatBufferBuilder);
    }

    public static void startPresencePublishStatus(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(9);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addIsSuccess(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static void addRemoteExpires(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addSipErrorCode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static void addSipErrorPhrase(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addEtag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addMinExpires(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(6, (int) j, 0);
    }

    public static void addIsRefresh(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(7, z, false);
    }

    public static void addRetryAfter(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(8, (int) j, 0);
    }

    public static int endPresencePublishStatus(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 12);
        return endObject;
    }
}
