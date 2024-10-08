package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_.Contact;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUploadContacts extends Table {
    public static RequestXdmUploadContacts getRootAsRequestXdmUploadContacts(ByteBuffer byteBuffer) {
        return getRootAsRequestXdmUploadContacts(byteBuffer, new RequestXdmUploadContacts());
    }

    public static RequestXdmUploadContacts getRootAsRequestXdmUploadContacts(ByteBuffer byteBuffer, RequestXdmUploadContacts requestXdmUploadContacts) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestXdmUploadContacts.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestXdmUploadContacts __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long rid() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String impu() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public Contact contacts(int i) {
        return contacts(new Contact(), i);
    }

    public Contact contacts(Contact contact, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return contact.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int contactsLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String uuid() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uuidAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String etag() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer etagAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public long mtc() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestXdmUploadContacts(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, long j2) {
        flatBufferBuilder.startObject(6);
        addMtc(flatBufferBuilder, j2);
        addEtag(flatBufferBuilder, i4);
        addUuid(flatBufferBuilder, i3);
        addContacts(flatBufferBuilder, i2);
        addImpu(flatBufferBuilder, i);
        addRid(flatBufferBuilder, j);
        return endRequestXdmUploadContacts(flatBufferBuilder);
    }

    public static void startRequestXdmUploadContacts(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addRid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImpu(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addContacts(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createContactsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startContactsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addUuid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addEtag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addMtc(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(5, (int) j, 0);
    }

    public static int endRequestXdmUploadContacts(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
