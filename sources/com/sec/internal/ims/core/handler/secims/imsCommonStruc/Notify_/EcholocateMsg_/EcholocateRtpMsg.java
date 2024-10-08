package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EcholocateRtpMsg extends Table {
    public static EcholocateRtpMsg getRootAsEcholocateRtpMsg(ByteBuffer byteBuffer) {
        return getRootAsEcholocateRtpMsg(byteBuffer, new EcholocateRtpMsg());
    }

    public static EcholocateRtpMsg getRootAsEcholocateRtpMsg(ByteBuffer byteBuffer, EcholocateRtpMsg echolocateRtpMsg) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return echolocateRtpMsg.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public EcholocateRtpMsg __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String dir() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer dirAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String id() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String lossrate() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer lossrateAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String delay() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer delayAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String jitter() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer jitterAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String measuredperiod() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer measuredperiodAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String nwstate() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer nwstateAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public static int createEcholocateRtpMsg(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        flatBufferBuilder.startObject(7);
        addNwstate(flatBufferBuilder, i7);
        addMeasuredperiod(flatBufferBuilder, i6);
        addJitter(flatBufferBuilder, i5);
        addDelay(flatBufferBuilder, i4);
        addLossrate(flatBufferBuilder, i3);
        addId(flatBufferBuilder, i2);
        addDir(flatBufferBuilder, i);
        return endEcholocateRtpMsg(flatBufferBuilder);
    }

    public static void startEcholocateRtpMsg(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(7);
    }

    public static void addDir(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addLossrate(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addDelay(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addJitter(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addMeasuredperiod(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addNwstate(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static int endEcholocateRtpMsg(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 12);
        flatBufferBuilder.required(endObject, 14);
        flatBufferBuilder.required(endObject, 16);
        return endObject;
    }
}
