package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;

public final class SendMessageRevokeParams {
    public final Message mCallback;
    public String mContributionId;
    public String mConversationId;
    public final String mImdnId;
    public String mOwnImsi;
    public final ImsUri mUri;

    public SendMessageRevokeParams(ImsUri imsUri, String str, Message message, String str2, String str3, String str4) {
        this.mUri = imsUri;
        this.mImdnId = str;
        this.mCallback = message;
        this.mConversationId = str2;
        this.mContributionId = str3;
        this.mOwnImsi = str4;
    }

    public String toString() {
        return "SendRevokeParams [mUri=" + this.mUri + ", mImdnId=" + this.mImdnId + ", mCallback=" + this.mCallback + ", mConversationId=" + this.mConversationId + ", mContributionId=" + this.mContributionId + "]";
    }
}
