package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple_.Status;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ServiceTuple extends Table {
    public static ServiceTuple getRootAsServiceTuple(ByteBuffer byteBuffer) {
        return getRootAsServiceTuple(byteBuffer, new ServiceTuple());
    }

    public static ServiceTuple getRootAsServiceTuple(ByteBuffer byteBuffer, ServiceTuple serviceTuple) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return serviceTuple.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ServiceTuple __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String serviceId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer serviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String version() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer versionAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String description() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer descriptionAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public Status status() {
        return status(new Status());
    }

    public Status status(Status status) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return status.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public Element mediaCapabilities(int i) {
        return mediaCapabilities(new Element(), i);
    }

    public Element mediaCapabilities(Element element, int i) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return element.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int mediaCapabilitiesLength() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public Element contacts(int i) {
        return contacts(new Element(), i);
    }

    public Element contacts(Element element, int i) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return element.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int contactsLength() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public Element extensions(int i) {
        return extensions(new Element(), i);
    }

    public Element extensions(Element element, int i) {
        int __offset = __offset(16);
        if (__offset != 0) {
            return element.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int extensionsLength() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public Element notes(int i) {
        return notes(new Element(), i);
    }

    public Element notes(Element element, int i) {
        int __offset = __offset(18);
        if (__offset != 0) {
            return element.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int notesLength() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String timestamp() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String tupleId() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer tupleIdAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String displaytext() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer displaytextAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public static int createServiceTuple(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11) {
        flatBufferBuilder.startObject(11);
        addDisplaytext(flatBufferBuilder, i11);
        addTupleId(flatBufferBuilder, i10);
        addTimestamp(flatBufferBuilder, i9);
        addNotes(flatBufferBuilder, i8);
        addExtensions(flatBufferBuilder, i7);
        addContacts(flatBufferBuilder, i6);
        addMediaCapabilities(flatBufferBuilder, i5);
        addStatus(flatBufferBuilder, i4);
        addDescription(flatBufferBuilder, i3);
        addVersion(flatBufferBuilder, i2);
        addServiceId(flatBufferBuilder, i);
        return endServiceTuple(flatBufferBuilder);
    }

    public static void startServiceTuple(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(11);
    }

    public static void addServiceId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addVersion(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addDescription(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addMediaCapabilities(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int createMediaCapabilitiesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startMediaCapabilitiesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addContacts(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
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

    public static void addExtensions(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int createExtensionsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startExtensionsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addNotes(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static int createNotesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startNotesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addTimestamp(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addTupleId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static void addDisplaytext(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static int endServiceTuple(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
