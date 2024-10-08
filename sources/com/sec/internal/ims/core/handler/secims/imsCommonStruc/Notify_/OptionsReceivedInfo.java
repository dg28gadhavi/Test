package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class OptionsReceivedInfo extends Table {
    public static OptionsReceivedInfo getRootAsOptionsReceivedInfo(ByteBuffer byteBuffer) {
        return getRootAsOptionsReceivedInfo(byteBuffer, new OptionsReceivedInfo());
    }

    public static OptionsReceivedInfo getRootAsOptionsReceivedInfo(ByteBuffer byteBuffer, OptionsReceivedInfo optionsReceivedInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return optionsReceivedInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public OptionsReceivedInfo __assign(int i, ByteBuffer byteBuffer) {
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

    public String remoteUri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean isResponse() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean success() {
        int __offset = __offset(10);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int reason() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int tags(int i) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return this.bb.getInt(__vector(__offset) + (i * 4));
        }
        return 0;
    }

    public int tagsLength() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer tagsAsByteBuffer() {
        return __vector_as_bytebuffer(14, 4);
    }

    public String txId() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer txIdAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public int lastSeen() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String extFeature() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer extFeatureAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String pAssertedId(int i) {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int pAssertedIdLength() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public boolean isChatbotParticipant() {
        int __offset = __offset(24);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isCmcCheck() {
        int __offset = __offset(26);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String caps(int i) {
        int __offset = __offset(28);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int capsLength() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public int respCode() {
        int __offset = __offset(30);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String failReason() {
        int __offset = __offset(32);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer failReasonAsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public static int createOptionsReceivedInfo(FlatBufferBuilder flatBufferBuilder, long j, int i, boolean z, boolean z2, int i2, int i3, int i4, int i5, int i6, int i7, boolean z3, boolean z4, int i8, int i9, int i10) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(15);
        addFailReason(flatBufferBuilder, i10);
        addRespCode(flatBufferBuilder, i9);
        addCaps(flatBufferBuilder, i8);
        int i11 = i7;
        addPAssertedId(flatBufferBuilder, i7);
        int i12 = i6;
        addExtFeature(flatBufferBuilder, i6);
        int i13 = i5;
        addLastSeen(flatBufferBuilder, i5);
        int i14 = i4;
        addTxId(flatBufferBuilder, i4);
        int i15 = i3;
        addTags(flatBufferBuilder, i3);
        int i16 = i2;
        addReason(flatBufferBuilder, i2);
        int i17 = i;
        addRemoteUri(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        boolean z5 = z4;
        addIsCmcCheck(flatBufferBuilder, z4);
        boolean z6 = z3;
        addIsChatbotParticipant(flatBufferBuilder, z3);
        boolean z7 = z2;
        addSuccess(flatBufferBuilder, z2);
        boolean z8 = z;
        addIsResponse(flatBufferBuilder, z);
        return endOptionsReceivedInfo(flatBufferBuilder);
    }

    public static void startOptionsReceivedInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(15);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addIsResponse(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static void addSuccess(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(3, z, false);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(4, i, 0);
    }

    public static void addTags(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int createTagsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addInt(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startTagsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addTxId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addLastSeen(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(7, i, 0);
    }

    public static void addExtFeature(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addPAssertedId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static int createPAssertedIdVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startPAssertedIdVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addIsChatbotParticipant(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(10, z, false);
    }

    public static void addIsCmcCheck(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(11, z, false);
    }

    public static void addCaps(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(12, i, 0);
    }

    public static int createCapsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startCapsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addRespCode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(13, i, 0);
    }

    public static void addFailReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(14, i, 0);
    }

    public static int endOptionsReceivedInfo(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 16);
        return endObject;
    }
}
