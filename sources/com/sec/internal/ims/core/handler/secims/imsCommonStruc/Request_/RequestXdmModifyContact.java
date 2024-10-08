package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_.Contact;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmModifyContact extends Table {
    public static RequestXdmModifyContact getRootAsRequestXdmModifyContact(ByteBuffer byteBuffer) {
        return getRootAsRequestXdmModifyContact(byteBuffer, new RequestXdmModifyContact());
    }

    public static RequestXdmModifyContact getRootAsRequestXdmModifyContact(ByteBuffer byteBuffer, RequestXdmModifyContact requestXdmModifyContact) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestXdmModifyContact.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestXdmModifyContact __assign(int i, ByteBuffer byteBuffer) {
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

    public String contactId() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contactIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
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

    public Contact contact() {
        return contact(new Contact());
    }

    public Contact contact(Contact contact) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return contact.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createRequestXdmModifyContact(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(6);
        addContact(flatBufferBuilder, i5);
        addEtag(flatBufferBuilder, i4);
        addUuid(flatBufferBuilder, i3);
        addContactId(flatBufferBuilder, i2);
        addImpu(flatBufferBuilder, i);
        addRid(flatBufferBuilder, j);
        return endRequestXdmModifyContact(flatBufferBuilder);
    }

    public static void startRequestXdmModifyContact(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addRid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImpu(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addContactId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addUuid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addEtag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addContact(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endRequestXdmModifyContact(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        return endObject;
    }
}
