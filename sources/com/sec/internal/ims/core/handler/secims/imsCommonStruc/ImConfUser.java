package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConfUser extends Table {
    public static ImConfUser getRootAsImConfUser(ByteBuffer byteBuffer) {
        return getRootAsImConfUser(byteBuffer, new ImConfUser());
    }

    public static ImConfUser getRootAsImConfUser(ByteBuffer byteBuffer, ImConfUser imConfUser) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imConfUser.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImConfUser __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String entity() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer entityAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String state() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer stateAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean yourOwn() {
        int __offset = __offset(8);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public ImConfUserEndpoint endpoint() {
        return endpoint(new ImConfUserEndpoint());
    }

    public ImConfUserEndpoint endpoint(ImConfUserEndpoint imConfUserEndpoint) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return imConfUserEndpoint.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String roles() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer rolesAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String userAlias() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createImConfUser(FlatBufferBuilder flatBufferBuilder, int i, int i2, boolean z, int i3, int i4, int i5) {
        flatBufferBuilder.startObject(6);
        addUserAlias(flatBufferBuilder, i5);
        addRoles(flatBufferBuilder, i4);
        addEndpoint(flatBufferBuilder, i3);
        addState(flatBufferBuilder, i2);
        addEntity(flatBufferBuilder, i);
        addYourOwn(flatBufferBuilder, z);
        return endImConfUser(flatBufferBuilder);
    }

    public static void startImConfUser(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addEntity(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addState(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addYourOwn(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(2, z, false);
    }

    public static void addEndpoint(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addRoles(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endImConfUser(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
