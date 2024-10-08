package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ChangeGroupChatLeaderParams {
    public final Message mCallback;
    public final List<ImsUri> mLeader;
    public final Object mRawHandle;
    public final String mReqKey = UUID.randomUUID().toString();

    public ChangeGroupChatLeaderParams(Object obj, List<ImsUri> list, Message message) {
        this.mRawHandle = obj;
        this.mLeader = list;
        this.mCallback = message;
    }

    public String toString() {
        return "ChangeGroupChatLeaderParams [mRawHandle=" + this.mRawHandle + ", mLeader=" + IMSLog.numberChecker((Collection<ImsUri>) this.mLeader) + ", mCallback=" + this.mCallback + ", mReqKey=" + this.mReqKey + "]";
    }
}
