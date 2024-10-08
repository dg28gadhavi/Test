package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ContactInfo;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NewPresenceInfo extends Table {
    public static NewPresenceInfo getRootAsNewPresenceInfo(ByteBuffer byteBuffer) {
        return getRootAsNewPresenceInfo(byteBuffer, new NewPresenceInfo());
    }

    public static NewPresenceInfo getRootAsNewPresenceInfo(ByteBuffer byteBuffer, NewPresenceInfo newPresenceInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return newPresenceInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public NewPresenceInfo __assign(int i, ByteBuffer byteBuffer) {
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

    public String subscriptionId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscriptionIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public ContactInfo contactInfo(int i) {
        return contactInfo(new ContactInfo(), i);
    }

    public ContactInfo contactInfo(ContactInfo contactInfo, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return contactInfo.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int contactInfoLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String subscriptionState() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscriptionStateAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String subscriptionStateReason() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscriptionStateReasonAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createNewPresenceInfo(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(5);
        addSubscriptionStateReason(flatBufferBuilder, i4);
        addSubscriptionState(flatBufferBuilder, i3);
        addContactInfo(flatBufferBuilder, i2);
        addSubscriptionId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endNewPresenceInfo(flatBufferBuilder);
    }

    public static void startNewPresenceInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addContactInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createContactInfoVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startContactInfoVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addSubscriptionState(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addSubscriptionStateReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int endNewPresenceInfo(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
