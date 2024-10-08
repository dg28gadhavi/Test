package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_.Contact;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RegInfoChanged extends Table {
    public static RegInfoChanged getRootAsRegInfoChanged(ByteBuffer byteBuffer) {
        return getRootAsRegInfoChanged(byteBuffer, new RegInfoChanged());
    }

    public static RegInfoChanged getRootAsRegInfoChanged(ByteBuffer byteBuffer, RegInfoChanged regInfoChanged) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return regInfoChanged.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RegInfoChanged __assign(int i, ByteBuffer byteBuffer) {
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

    public Contact contacts(int i) {
        return contacts(new Contact(), i);
    }

    public Contact contacts(Contact contact, int i) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return contact.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int contactsLength() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createRegInfoChanged(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addContacts(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRegInfoChanged(flatBufferBuilder);
    }

    public static void startRegInfoChanged(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addContacts(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
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

    public static int endRegInfoChanged(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
