package com.sec.internal.constants.ims.servicemodules.im.params;

import com.sec.ims.util.ImsUri;

public class SendReportMsgParams {
    private String mSpamDate;
    private ImsUri mSpamFrom;
    private String mSpamMsgImdnId;
    private ImsUri mSpamTo;

    public SendReportMsgParams(ImsUri imsUri, ImsUri imsUri2, String str, String str2) {
        this.mSpamFrom = imsUri;
        this.mSpamTo = imsUri2;
        this.mSpamDate = str;
        this.mSpamMsgImdnId = str2;
    }

    public ImsUri getSpamFrom() {
        return this.mSpamFrom;
    }

    public ImsUri getSpamTo() {
        return this.mSpamTo;
    }

    public String getSpamDate() {
        return this.mSpamDate;
    }

    public String getSpamMsgImdnId() {
        return this.mSpamMsgImdnId;
    }

    public String toString() {
        return "SendReportMsgParams [mSpamFrom=" + this.mSpamFrom + ", mSpamTo=" + this.mSpamTo + ", mSpamDate=" + this.mSpamDate + ", mSpamMsgImdnId=" + this.mSpamMsgImdnId + "]";
    }
}
