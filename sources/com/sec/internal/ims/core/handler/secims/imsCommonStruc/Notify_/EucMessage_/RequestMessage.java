package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestMessage extends Table {
    public static RequestMessage getRootAsRequestMessage(ByteBuffer byteBuffer) {
        return getRootAsRequestMessage(byteBuffer, new RequestMessage());
    }

    public static RequestMessage getRootAsRequestMessage(ByteBuffer byteBuffer, RequestMessage requestMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestMessage __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public BaseMessage base() {
        return base(new BaseMessage());
    }

    public BaseMessage base(BaseMessage baseMessage) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return baseMessage.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public EucContent content() {
        return content(new EucContent());
    }

    public EucContent content(EucContent eucContent) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return eucContent.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public TextLangPair acceptButtons(int i) {
        return acceptButtons(new TextLangPair(), i);
    }

    public TextLangPair acceptButtons(TextLangPair textLangPair, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return textLangPair.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int acceptButtonsLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public TextLangPair rejectButtons(int i) {
        return rejectButtons(new TextLangPair(), i);
    }

    public TextLangPair rejectButtons(TextLangPair textLangPair, int i) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return textLangPair.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int rejectButtonsLength() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public boolean pin() {
        int __offset = __offset(12);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean externalEucr() {
        int __offset = __offset(14);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestMessage(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, boolean z, boolean z2) {
        flatBufferBuilder.startObject(6);
        addRejectButtons(flatBufferBuilder, i4);
        addAcceptButtons(flatBufferBuilder, i3);
        addContent(flatBufferBuilder, i2);
        addBase(flatBufferBuilder, i);
        addExternalEucr(flatBufferBuilder, z2);
        addPin(flatBufferBuilder, z);
        return endRequestMessage(flatBufferBuilder);
    }

    public static void startRequestMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addBase(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addContent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addAcceptButtons(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createAcceptButtonsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startAcceptButtonsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addRejectButtons(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int createRejectButtonsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startRejectButtonsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addPin(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(4, z, false);
    }

    public static void addExternalEucr(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(5, z, false);
    }

    public static int endRequestMessage(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
