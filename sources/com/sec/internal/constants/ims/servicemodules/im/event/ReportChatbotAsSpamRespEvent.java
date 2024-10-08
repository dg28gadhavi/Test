package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.constants.ims.servicemodules.im.ImError;

public class ReportChatbotAsSpamRespEvent {
    public ImError mError;
    public String mRequestId;
    public String mUri;

    public ReportChatbotAsSpamRespEvent(String str, String str2, ImError imError) {
        this.mUri = str;
        this.mError = imError;
        this.mRequestId = str2;
    }

    public String toString() {
        return "ReportChatbotAsSpamRespEvent, mError = " + this.mError + ", mRequestId = " + this.mRequestId + "]";
    }
}
