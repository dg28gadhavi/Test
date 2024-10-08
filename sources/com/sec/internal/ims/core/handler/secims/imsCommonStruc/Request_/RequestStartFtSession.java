package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReportMessageHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartFtSession extends Table {
    public static RequestStartFtSession getRootAsRequestStartFtSession(ByteBuffer byteBuffer) {
        return getRootAsRequestStartFtSession(byteBuffer, new RequestStartFtSession());
    }

    public static RequestStartFtSession getRootAsRequestStartFtSession(ByteBuffer byteBuffer, RequestStartFtSession requestStartFtSession) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestStartFtSession.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestStartFtSession __assign(int i, ByteBuffer byteBuffer) {
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

    public FtPayloadParam payload() {
        return payload(new FtPayloadParam());
    }

    public FtPayloadParam payload(FtPayloadParam ftPayloadParam) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ftPayloadParam.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public ReportMessageHdr reportData() {
        return reportData(new ReportMessageHdr());
    }

    public ReportMessageHdr reportData(ReportMessageHdr reportMessageHdr) {
        int __offset = __offset(10);
        if (__offset != 0) {
            return reportMessageHdr.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createRequestStartFtSession(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3) {
        flatBufferBuilder.startObject(4);
        addReportData(flatBufferBuilder, i3);
        addPayload(flatBufferBuilder, i2);
        addSessionData(flatBufferBuilder, i);
        addRegistrationHandle(flatBufferBuilder, j);
        return endRequestStartFtSession(flatBufferBuilder);
    }

    public static void startRequestStartFtSession(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSessionData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addPayload(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addReportData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endRequestStartFtSession(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 10);
        return endObject;
    }
}
