package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EcholocateSignalMsg extends Table {
    public static EcholocateSignalMsg getRootAsEcholocateSignalMsg(ByteBuffer byteBuffer) {
        return getRootAsEcholocateSignalMsg(byteBuffer, new EcholocateSignalMsg());
    }

    public static EcholocateSignalMsg getRootAsEcholocateSignalMsg(ByteBuffer byteBuffer, EcholocateSignalMsg echolocateSignalMsg) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return echolocateSignalMsg.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public EcholocateSignalMsg __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String origin() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer originAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String line1() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer line1AsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String callid() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer callidAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String cseq() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cseqAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String sessionid() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sessionidAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String reason() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reasonAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String contents() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contentsAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String dispname() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer dispnameAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String peernumber() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer peernumberAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public boolean isEpdgCall() {
        int __offset = __offset(22);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createEcholocateSignalMsg(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, boolean z) {
        flatBufferBuilder.startObject(10);
        addPeernumber(flatBufferBuilder, i9);
        addDispname(flatBufferBuilder, i8);
        addContents(flatBufferBuilder, i7);
        addReason(flatBufferBuilder, i6);
        addSessionid(flatBufferBuilder, i5);
        addCseq(flatBufferBuilder, i4);
        addCallid(flatBufferBuilder, i3);
        addLine1(flatBufferBuilder, i2);
        addOrigin(flatBufferBuilder, i);
        addIsEpdgCall(flatBufferBuilder, z);
        return endEcholocateSignalMsg(flatBufferBuilder);
    }

    public static void startEcholocateSignalMsg(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(10);
    }

    public static void addOrigin(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addLine1(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addCallid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addCseq(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addSessionid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addDispname(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addPeernumber(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addIsEpdgCall(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(9, z, false);
    }

    public static int endEcholocateSignalMsg(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        return endObject;
    }
}
