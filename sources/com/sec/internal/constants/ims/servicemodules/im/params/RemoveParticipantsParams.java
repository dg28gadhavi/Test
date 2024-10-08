package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.UUID;

public class RemoveParticipantsParams {
    public final Message mCallback;
    public final Object mRawHandle;
    public final List<ImsUri> mRemovedParticipants;
    public final String mReqKey = UUID.randomUUID().toString();

    public RemoveParticipantsParams(Object obj, List<ImsUri> list, Message message) {
        this.mRawHandle = obj;
        this.mRemovedParticipants = list;
        this.mCallback = message;
    }

    public String toString() {
        return "RemoveParticipantParams [mRawHandle=" + this.mRawHandle + ", mRemovedParticipants=" + IMSLog.checker(this.mRemovedParticipants) + ", mCallback=" + this.mCallback + ", mReqKey=" + this.mReqKey + "]";
    }
}
