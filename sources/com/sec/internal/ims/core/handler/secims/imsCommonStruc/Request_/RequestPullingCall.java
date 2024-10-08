package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPullingCall extends Table {
    public static RequestPullingCall getRootAsRequestPullingCall(ByteBuffer byteBuffer) {
        return getRootAsRequestPullingCall(byteBuffer, new RequestPullingCall());
    }

    public static RequestPullingCall getRootAsRequestPullingCall(ByteBuffer byteBuffer, RequestPullingCall requestPullingCall) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestPullingCall.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestPullingCall __assign(int i, ByteBuffer byteBuffer) {
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

    public String pullingUri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer pullingUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String targetUri() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer targetUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String origUri() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer origUriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String callId() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer callIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String localTag() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer localTagAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String remoteTag() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer remoteTagAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public int callType() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int codec() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public long audioDirection() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long videoDirection() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isVideoPortZero() {
        int __offset = __offset(26);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String mdmnExtNumber() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer mdmnExtNumberAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String p2pList(int i) {
        int __offset = __offset(30);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int p2pListLength() {
        int __offset = __offset(30);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public int cmcEdCallSlot() {
        int __offset = __offset(32);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createRequestPullingCall(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, long j2, long j3, boolean z, int i9, int i10, int i11) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(15);
        addCmcEdCallSlot(flatBufferBuilder, i11);
        addP2pList(flatBufferBuilder, i10);
        addMdmnExtNumber(flatBufferBuilder, i9);
        addVideoDirection(flatBufferBuilder, j3);
        long j4 = j2;
        addAudioDirection(flatBufferBuilder, j2);
        int i12 = i8;
        addCodec(flatBufferBuilder, i8);
        int i13 = i7;
        addCallType(flatBufferBuilder, i7);
        int i14 = i6;
        addRemoteTag(flatBufferBuilder, i6);
        int i15 = i5;
        addLocalTag(flatBufferBuilder, i5);
        int i16 = i4;
        addCallId(flatBufferBuilder, i4);
        int i17 = i3;
        addOrigUri(flatBufferBuilder, i3);
        int i18 = i2;
        addTargetUri(flatBufferBuilder, i2);
        int i19 = i;
        addPullingUri(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addIsVideoPortZero(flatBufferBuilder, z);
        return endRequestPullingCall(flatBufferBuilder);
    }

    public static void startRequestPullingCall(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(15);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addPullingUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addTargetUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addOrigUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addCallId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addLocalTag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addRemoteTag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addCallType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(7, i, 0);
    }

    public static void addCodec(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(8, i, 0);
    }

    public static void addAudioDirection(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(9, (int) j, 0);
    }

    public static void addVideoDirection(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(10, (int) j, 0);
    }

    public static void addIsVideoPortZero(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(11, z, false);
    }

    public static void addMdmnExtNumber(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(12, i, 0);
    }

    public static void addP2pList(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(13, i, 0);
    }

    public static int createP2pListVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startP2pListVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addCmcEdCallSlot(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(14, i, 0);
    }

    public static int endRequestPullingCall(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        flatBufferBuilder.required(endObject, 14);
        flatBufferBuilder.required(endObject, 16);
        flatBufferBuilder.required(endObject, 28);
        return endObject;
    }
}
