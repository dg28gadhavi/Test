package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_.EcholocateRtpMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_.EcholocateSignalMsg;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EcholocateMsg extends Table {
    public static EcholocateMsg getRootAsEcholocateMsg(ByteBuffer byteBuffer) {
        return getRootAsEcholocateMsg(byteBuffer, new EcholocateMsg());
    }

    public static EcholocateMsg getRootAsEcholocateMsg(ByteBuffer byteBuffer, EcholocateMsg echolocateMsg) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return echolocateMsg.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public EcholocateMsg __assign(int i, ByteBuffer byteBuffer) {
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

    public int msgtype() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public EcholocateSignalMsg echolocateSignalData() {
        return echolocateSignalData(new EcholocateSignalMsg());
    }

    public EcholocateSignalMsg echolocateSignalData(EcholocateSignalMsg echolocateSignalMsg) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return echolocateSignalMsg.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public EcholocateRtpMsg echolocateRtpData() {
        return echolocateRtpData(new EcholocateRtpMsg());
    }

    public EcholocateRtpMsg echolocateRtpData(EcholocateRtpMsg echolocateRtpMsg) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return echolocateRtpMsg.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createEcholocateMsg(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addEcholocateRtpData(flatBufferBuilder, i3);
        addEcholocateSignalData(flatBufferBuilder, i2);
        addMsgtype(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endEcholocateMsg(flatBufferBuilder);
    }

    public static void startEcholocateMsg(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addMsgtype(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addEcholocateSignalData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addEcholocateRtpData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endEcholocateMsg(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
