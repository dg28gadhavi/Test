package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Participant;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendSlmFile extends Table {
    public static RequestSendSlmFile getRootAsRequestSendSlmFile(ByteBuffer byteBuffer) {
        return getRootAsRequestSendSlmFile(byteBuffer, new RequestSendSlmFile());
    }

    public static RequestSendSlmFile getRootAsRequestSendSlmFile(ByteBuffer byteBuffer, RequestSendSlmFile requestSendSlmFile) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendSlmFile.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendSlmFile __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long registrationHandle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public BaseSessionData sessionData() {
        return sessionData(new BaseSessionData());
    }

    public BaseSessionData sessionData(BaseSessionData baseSessionData) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return baseSessionData.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public FtPayloadParam payloadParam() {
        return payloadParam(new FtPayloadParam());
    }

    public FtPayloadParam payloadParam(FtPayloadParam ftPayloadParam) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ftPayloadParam.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public Participant participant(int i) {
        return participant(new Participant(), i);
    }

    public Participant participant(Participant participant, int i) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return participant.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int participantLength() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public static int createRequestSendSlmFile(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addParticipant(flatBufferBuilder, i3);
        addPayloadParam(flatBufferBuilder, i2);
        addSessionData(flatBufferBuilder, i);
        addRegistrationHandle(flatBufferBuilder, j);
        return endRequestSendSlmFile(flatBufferBuilder);
    }

    public static void startRequestSendSlmFile(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSessionData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addPayloadParam(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addParticipant(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int createParticipantVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startParticipantVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static int endRequestSendSlmFile(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        return endObject;
    }
}
