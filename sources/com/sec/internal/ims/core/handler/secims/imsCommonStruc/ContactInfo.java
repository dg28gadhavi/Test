package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactInfo extends Table {
    public static ContactInfo getRootAsContactInfo(ByteBuffer byteBuffer) {
        return getRootAsContactInfo(byteBuffer, new ContactInfo());
    }

    public static ContactInfo getRootAsContactInfo(ByteBuffer byteBuffer, ContactInfo contactInfo) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return contactInfo.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ContactInfo __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String uri() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String number() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer numberAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String subscriptionState() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscriptionStateAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String subscriptionFailureReason() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer subscriptionFailureReasonAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public PresenceServiceStatus serviceStatus(int i) {
        return serviceStatus(new PresenceServiceStatus(), i);
    }

    public PresenceServiceStatus serviceStatus(PresenceServiceStatus presenceServiceStatus, int i) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return presenceServiceStatus.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int serviceStatusLength() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String note() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer noteAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String iconUri() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer iconUriAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String email() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer emailAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String homepage() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer homepageAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String className() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer classNameAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String rawPidf() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer rawPidfAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String entityUri() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer entityUriAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public static int createContactInfo(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12) {
        flatBufferBuilder.startObject(12);
        addEntityUri(flatBufferBuilder, i12);
        addRawPidf(flatBufferBuilder, i11);
        addClassName(flatBufferBuilder, i10);
        addHomepage(flatBufferBuilder, i9);
        addEmail(flatBufferBuilder, i8);
        addIconUri(flatBufferBuilder, i7);
        addNote(flatBufferBuilder, i6);
        addServiceStatus(flatBufferBuilder, i5);
        addSubscriptionFailureReason(flatBufferBuilder, i4);
        addSubscriptionState(flatBufferBuilder, i3);
        addNumber(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        return endContactInfo(flatBufferBuilder);
    }

    public static void startContactInfo(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(12);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addNumber(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addSubscriptionState(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addSubscriptionFailureReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addServiceStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int createServiceStatusVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startServiceStatusVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addNote(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addIconUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addEmail(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addHomepage(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addClassName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static void addRawPidf(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addEntityUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static int endContactInfo(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
