package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Element;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.PersonTuple;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPresencePublish extends Table {
    public static RequestPresencePublish getRootAsRequestPresencePublish(ByteBuffer byteBuffer) {
        return getRootAsRequestPresencePublish(byteBuffer, new RequestPresencePublish());
    }

    public static RequestPresencePublish getRootAsRequestPresencePublish(ByteBuffer byteBuffer, RequestPresencePublish requestPresencePublish) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestPresencePublish.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestPresencePublish __assign(int i, ByteBuffer byteBuffer) {
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

    public String uri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public PersonTuple personTuples(int i) {
        return personTuples(new PersonTuple(), i);
    }

    public PersonTuple personTuples(PersonTuple personTuple, int i) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return personTuple.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int personTuplesLength() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ServiceTuple serviceTuples(int i) {
        return serviceTuples(new ServiceTuple(), i);
    }

    public ServiceTuple serviceTuples(ServiceTuple serviceTuple, int i) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return serviceTuple.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int serviceTuplesLength() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public DeviceTuple deviceTuples(int i) {
        return deviceTuples(new DeviceTuple(), i);
    }

    public DeviceTuple deviceTuples(DeviceTuple deviceTuple, int i) {
        int __offset = __offset(12);
        if (__offset != 0) {
            return deviceTuple.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int deviceTuplesLength() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public Element notes(int i) {
        return notes(new Element(), i);
    }

    public Element notes(Element element, int i) {
        int __offset = __offset(14);
        if (__offset != 0) {
            return element.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int notesLength() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String timestamp() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public long expireTime() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String eTag() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer eTagAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public boolean gzipEnable() {
        int __offset = __offset(22);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String pidfXml() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer pidfXmlAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public static int createRequestPresencePublish(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5, int i6, long j2, int i7, boolean z, int i8) {
        flatBufferBuilder.startObject(11);
        addPidfXml(flatBufferBuilder, i8);
        addETag(flatBufferBuilder, i7);
        addExpireTime(flatBufferBuilder, j2);
        addTimestamp(flatBufferBuilder, i6);
        addNotes(flatBufferBuilder, i5);
        addDeviceTuples(flatBufferBuilder, i4);
        addServiceTuples(flatBufferBuilder, i3);
        addPersonTuples(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addGzipEnable(flatBufferBuilder, z);
        return endRequestPresencePublish(flatBufferBuilder);
    }

    public static void startRequestPresencePublish(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(11);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addPersonTuples(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int createPersonTuplesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startPersonTuplesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addServiceTuples(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int createServiceTuplesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startServiceTuplesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addDeviceTuples(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static int createDeviceTuplesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startDeviceTuplesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addNotes(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int createNotesVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startNotesVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addTimestamp(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addExpireTime(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(7, (int) j, 0);
    }

    public static void addETag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addGzipEnable(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(9, z, false);
    }

    public static void addPidfXml(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static int endRequestPresencePublish(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
