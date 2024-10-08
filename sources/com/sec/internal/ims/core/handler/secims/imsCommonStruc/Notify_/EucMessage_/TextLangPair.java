package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TextLangPair extends Table {
    public static TextLangPair getRootAsTextLangPair(ByteBuffer byteBuffer) {
        return getRootAsTextLangPair(byteBuffer, new TextLangPair());
    }

    public static TextLangPair getRootAsTextLangPair(ByteBuffer byteBuffer, TextLangPair textLangPair) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return textLangPair.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public TextLangPair __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String text() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String lang() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer langAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createTextLangPair(FlatBufferBuilder flatBufferBuilder, int i, int i2) {
        flatBufferBuilder.startObject(2);
        addLang(flatBufferBuilder, i2);
        addText(flatBufferBuilder, i);
        return endTextLangPair(flatBufferBuilder);
    }

    public static void startTextLangPair(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addText(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addLang(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endTextLangPair(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        return endObject;
    }
}
