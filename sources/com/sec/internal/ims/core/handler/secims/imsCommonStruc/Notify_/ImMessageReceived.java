package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImMessageReceived extends Table {
    public static ImMessageReceived getRootAsImMessageReceived(ByteBuffer byteBuffer) {
        return getRootAsImMessageReceived(byteBuffer, new ImMessageReceived());
    }

    public static ImMessageReceived getRootAsImMessageReceived(ByteBuffer byteBuffer, ImMessageReceived imMessageReceived) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imMessageReceived.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImMessageReceived __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public BaseSessionData sessionData() {
        return sessionData(new BaseSessionData());
    }

    public BaseSessionData sessionData(BaseSessionData baseSessionData) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return baseSessionData.__assign(__indirect(__offset + this.bb_pos), this.bb);
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

    public static int createImMessageReceived(FlatBufferBuilder flatBufferBuilder, int i, int i2) {
        flatBufferBuilder.startObject(2);
        addMessageParam(flatBufferBuilder, i2);
        addSessionData(flatBufferBuilder, i);
        return endImMessageReceived(flatBufferBuilder);
    }

    public static void startImMessageReceived(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSessionData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addMessageParam(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endImMessageReceived(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
