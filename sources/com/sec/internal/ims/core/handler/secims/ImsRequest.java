package com.sec.internal.ims.core.handler.secims;

import android.os.Message;
import com.google.flatbuffers.FlatBufferBuilder;

/* compiled from: StackIF */
class ImsRequest {
    private FlatBufferBuilder mReqBuffer;
    Message mResult;
    int mTid;

    ImsRequest() {
    }

    static ImsRequest obtain(FlatBufferBuilder flatBufferBuilder, Message message) {
        ImsRequest imsRequest = new ImsRequest();
        imsRequest.mReqBuffer = flatBufferBuilder;
        imsRequest.mResult = message;
        if (message == null || message.getTarget() != null) {
            return imsRequest;
        }
        throw new NullPointerException("Message target must not be null");
    }

    public FlatBufferBuilder getReqBuffer() {
        return this.mReqBuffer;
    }
}
