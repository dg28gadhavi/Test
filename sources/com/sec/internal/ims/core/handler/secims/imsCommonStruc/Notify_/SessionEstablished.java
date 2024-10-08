package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SessionEstablished extends Table {
    public static SessionEstablished getRootAsSessionEstablished(ByteBuffer byteBuffer) {
        return getRootAsSessionEstablished(byteBuffer, new SessionEstablished());
    }

    public static SessionEstablished getRootAsSessionEstablished(ByteBuffer byteBuffer, SessionEstablished sessionEstablished) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sessionEstablished.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public SessionEstablished __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionHandle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError imError) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return imError.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String acceptContent(int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int acceptContentLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String acceptWrappedContent(int i) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int acceptWrappedContentLength() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createSessionEstablished(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addAcceptWrappedContent(flatBufferBuilder, i3);
        addAcceptContent(flatBufferBuilder, i2);
        addImError(flatBufferBuilder, i);
        addSessionHandle(flatBufferBuilder, j);
        return endSessionEstablished(flatBufferBuilder);
    }

    public static void startSessionEstablished(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addAcceptContent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createAcceptContentVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startAcceptContentVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addAcceptWrappedContent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int createAcceptWrappedContentVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startAcceptWrappedContentVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endSessionEstablished(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
