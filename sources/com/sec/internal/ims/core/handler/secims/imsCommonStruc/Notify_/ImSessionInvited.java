package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImSessionInvited extends Table {
    public static ImSessionInvited getRootAsImSessionInvited(ByteBuffer byteBuffer) {
        return getRootAsImSessionInvited(byteBuffer, new ImSessionInvited());
    }

    public static ImSessionInvited getRootAsImSessionInvited(ByteBuffer byteBuffer, ImSessionInvited imSessionInvited) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imSessionInvited.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImSessionInvited __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public ImSessionParam session() {
        return session(new ImSessionParam());
    }

    public ImSessionParam session(ImSessionParam imSessionParam) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return imSessionParam.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public ImMessageParam messageParam() {
        return messageParam(new ImMessageParam());
    }

    public ImMessageParam messageParam(ImMessageParam imMessageParam) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return imMessageParam.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String remoteMsrpAddr() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer remoteMsrpAddrAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String createdBy() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer createdByAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String invitedBy() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer invitedByAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public boolean isDeferred() {
        int __offset = __offset(14);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isForStoredNoti() {
        int __offset = __offset(16);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long userHandle() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createImSessionInvited(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, boolean z, boolean z2, long j) {
        flatBufferBuilder.startObject(8);
        addUserHandle(flatBufferBuilder, j);
        addInvitedBy(flatBufferBuilder, i5);
        addCreatedBy(flatBufferBuilder, i4);
        addRemoteMsrpAddr(flatBufferBuilder, i3);
        addMessageParam(flatBufferBuilder, i2);
        addSession(flatBufferBuilder, i);
        addIsForStoredNoti(flatBufferBuilder, z2);
        addIsDeferred(flatBufferBuilder, z);
        return endImSessionInvited(flatBufferBuilder);
    }

    public static void startImSessionInvited(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(8);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addMessageParam(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addRemoteMsrpAddr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addCreatedBy(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addInvitedBy(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addIsDeferred(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(5, z, false);
    }

    public static void addIsForStoredNoti(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(6, z, false);
    }

    public static void addUserHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(7, (int) j, 0);
    }

    public static int endImSessionInvited(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
