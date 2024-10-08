package com.sec.internal.ims.core.handler.secims;

import android.os.Message;
import com.google.flatbuffers.FlatBufferBuilder;

public class ResipStackRequest {
    public Message mCallback;
    public int mId;
    public int mOffset;
    public FlatBufferBuilder mRequest;

    public ResipStackRequest(int i, FlatBufferBuilder flatBufferBuilder, int i2, Message message) {
        this.mId = i;
        this.mRequest = flatBufferBuilder;
        this.mOffset = i2;
        this.mCallback = message;
    }
}
