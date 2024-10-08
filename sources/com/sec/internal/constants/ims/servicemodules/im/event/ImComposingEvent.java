package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.log.IMSLog;

public class ImComposingEvent {
    public final String mChatId;
    public final int mInterval;
    public final boolean mIsComposing;
    public final String mUri;
    public final String mUserAlias;

    public ImComposingEvent(String str, String str2, String str3, boolean z, int i) {
        this.mChatId = str;
        this.mUri = str2;
        this.mUserAlias = str3;
        this.mIsComposing = z;
        this.mInterval = i;
    }

    public String toString() {
        return "ImComposingEvent [mChatId=" + this.mChatId + ", mUri=" + IMSLog.checker(this.mUri) + ", mIsComposing=" + this.mIsComposing + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mInterval=" + this.mInterval + "]";
    }
}
