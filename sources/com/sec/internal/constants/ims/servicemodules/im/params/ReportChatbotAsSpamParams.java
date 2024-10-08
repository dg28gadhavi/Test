package com.sec.internal.constants.ims.servicemodules.im.params;

import com.sec.ims.util.ImsUri;

public class ReportChatbotAsSpamParams {
    public ImsUri mChatbotUri;
    public int mPhoneId;
    public String mRequestId;
    public String mSpamInfo;

    public ReportChatbotAsSpamParams(int i, String str, ImsUri imsUri, String str2) {
        this.mChatbotUri = imsUri;
        this.mSpamInfo = str2;
        this.mPhoneId = i;
        this.mRequestId = str;
    }

    public String toString() {
        return "ReportChatbotAsSpamParams [ spamInfo = " + this.mSpamInfo + ", PhoneId = " + this.mPhoneId + ", RequestId = " + this.mRequestId + "]";
    }
}
