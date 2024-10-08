package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class UpdateParticipantsResponse extends Table {
    public static UpdateParticipantsResponse getRootAsUpdateParticipantsResponse(ByteBuffer byteBuffer) {
        return getRootAsUpdateParticipantsResponse(byteBuffer, new UpdateParticipantsResponse());
    }

    public static UpdateParticipantsResponse getRootAsUpdateParticipantsResponse(ByteBuffer byteBuffer, UpdateParticipantsResponse updateParticipantsResponse) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return updateParticipantsResponse.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public UpdateParticipantsResponse __assign(int i, ByteBuffer byteBuffer) {
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

    public String reqKey() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer reqKeyAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public WarningHdr warningHdr() {
        return warningHdr(new WarningHdr());
    }

    public WarningHdr warningHdr(WarningHdr warningHdr) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return warningHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createUpdateParticipantsResponse(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addWarningHdr(flatBufferBuilder, i3);
        addReqKey(flatBufferBuilder, i2);
        addImError(flatBufferBuilder, i);
        addSessionHandle(flatBufferBuilder, j);
        return endUpdateParticipantsResponse(flatBufferBuilder);
    }

    public static void startUpdateParticipantsResponse(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addSessionHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addImError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addReqKey(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endUpdateParticipantsResponse(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
