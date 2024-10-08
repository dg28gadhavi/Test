package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XDMConfig extends Table {
    public static XDMConfig getRootAsXDMConfig(ByteBuffer byteBuffer) {
        return getRootAsXDMConfig(byteBuffer, new XDMConfig());
    }

    public static XDMConfig getRootAsXDMConfig(ByteBuffer byteBuffer, XDMConfig xDMConfig) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return xDMConfig.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public XDMConfig __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String httpUserName() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer httpUserNameAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String httpPasswd() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer httpPasswdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String xcapRootUri() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer xcapRootUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String authProxyServer() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer authProxyServerAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public long authProxyPort() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String bsfServer() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer bsfServerAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public long bsfServerPort() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String userAgent() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAgentAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public int mno() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String impu() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String impi() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer impiAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public boolean enableGba() {
        int __offset = __offset(26);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createXDMConfig(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, long j, int i5, long j2, int i6, int i7, int i8, int i9, boolean z) {
        flatBufferBuilder.startObject(12);
        addImpi(flatBufferBuilder, i9);
        addImpu(flatBufferBuilder, i8);
        addMno(flatBufferBuilder, i7);
        addUserAgent(flatBufferBuilder, i6);
        addBsfServerPort(flatBufferBuilder, j2);
        addBsfServer(flatBufferBuilder, i5);
        addAuthProxyPort(flatBufferBuilder, j);
        addAuthProxyServer(flatBufferBuilder, i4);
        addXcapRootUri(flatBufferBuilder, i3);
        addHttpPasswd(flatBufferBuilder, i2);
        addHttpUserName(flatBufferBuilder, i);
        addEnableGba(flatBufferBuilder, z);
        return endXDMConfig(flatBufferBuilder);
    }

    public static void startXDMConfig(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(12);
    }

    public static void addHttpUserName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addHttpPasswd(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addXcapRootUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addAuthProxyServer(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addAuthProxyPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(4, (int) j, 0);
    }

    public static void addBsfServer(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addBsfServerPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(6, (int) j, 0);
    }

    public static void addUserAgent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addMno(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(8, i, 0);
    }

    public static void addImpu(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static void addImpi(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addEnableGba(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(11, z, false);
    }

    public static int endXDMConfig(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        flatBufferBuilder.required(endObject, 14);
        flatBufferBuilder.required(endObject, 18);
        flatBufferBuilder.required(endObject, 22);
        flatBufferBuilder.required(endObject, 24);
        return endObject;
    }
}
