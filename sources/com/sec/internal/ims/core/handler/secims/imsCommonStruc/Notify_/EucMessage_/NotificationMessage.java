package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NotificationMessage extends Table {
    public static NotificationMessage getRootAsNotificationMessage(ByteBuffer byteBuffer) {
        return getRootAsNotificationMessage(byteBuffer, new NotificationMessage());
    }

    public static NotificationMessage getRootAsNotificationMessage(ByteBuffer byteBuffer, NotificationMessage notificationMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return notificationMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public NotificationMessage __assign(int i, ByteBuffer byteBuffer) {
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

    public TextLangPair okButtons(int i) {
        return okButtons(new TextLangPair(), i);
    }

    public TextLangPair okButtons(TextLangPair textLangPair, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return textLangPair.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int okButtonsLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createNotificationMessage(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addOkButtons(flatBufferBuilder, i3);
        addContent(flatBufferBuilder, i2);
        addBase(flatBufferBuilder, i);
        return endNotificationMessage(flatBufferBuilder);
    }

    public static void startNotificationMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addBase(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addContent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addOkButtons(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createOkButtonsVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startOkButtonsVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endNotificationMessage(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
