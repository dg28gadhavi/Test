package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactAddress;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactName;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactNumber;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactOrg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactUri;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Contact extends Table {
    public static Contact getRootAsContact(ByteBuffer byteBuffer) {
        return getRootAsContact(byteBuffer, new Contact());
    }

    public static Contact getRootAsContact(ByteBuffer byteBuffer, Contact contact) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return contact.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public Contact __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String contactId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contactIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public ContactName contactNames(int i) {
        return contactNames(new ContactName(), i);
    }

    public ContactName contactNames(ContactName contactName, int i) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return contactName.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int contactNamesLength() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ContactAddress contactAddresses(int i) {
        return contactAddresses(new ContactAddress(), i);
    }

    public ContactAddress contactAddresses(ContactAddress contactAddress, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return contactAddress.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int contactAddressesLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ContactNumber contactNumbers(int i) {
        return contactNumbers(new ContactNumber(), i);
    }

    public ContactNumber contactNumbers(ContactNumber contactNumber, int i) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return contactNumber.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int contactNumbersLength() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ContactUri contactUris(int i) {
        return contactUris(new ContactUri(), i);
    }

    public ContactUri contactUris(ContactUri contactUri, int i) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return contactUri.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int contactUrisLength() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ContactOrg contactOrgs(int i) {
        return contactOrgs(new ContactOrg(), i);
    }

    public ContactOrg contactOrgs(ContactOrg contactOrg, int i) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return contactOrg.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int contactOrgsLength() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createContact(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6) {
        flatBufferBuilder.startObject(6);
        addContactOrgs(flatBufferBuilder, i6);
        addContactUris(flatBufferBuilder, i5);
        addContactNumbers(flatBufferBuilder, i4);
        addContactAddresses(flatBufferBuilder, i3);
        addContactNames(flatBufferBuilder, i2);
        addContactId(flatBufferBuilder, i);
        return endContact(flatBufferBuilder);
    }

    public static void startContact(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addContactId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addContactNames(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int createContactNamesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startContactNamesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addContactAddresses(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createContactAddressesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startContactAddressesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addContactNumbers(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int createContactNumbersVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startContactNumbersVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addContactUris(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int createContactUrisVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startContactUrisVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addContactOrgs(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int createContactOrgsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startContactOrgsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endContact(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
