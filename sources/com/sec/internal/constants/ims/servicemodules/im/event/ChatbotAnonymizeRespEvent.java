package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.constants.ims.servicemodules.im.ImError;

public class ChatbotAnonymizeRespEvent {
    public String mChatbotUri;
    public String mCommandId;
    public ImError mError;
    public int mRetryAfter;

    public ChatbotAnonymizeRespEvent(String str, ImError imError, String str2, int i) {
        this.mChatbotUri = str;
        this.mError = imError;
        this.mCommandId = str2;
        this.mRetryAfter = i;
    }

    public String toString() {
        return "ChatbotAnonymizeRespEvent [mError = " + this.mError + ", mCommandId = " + this.mCommandId + ", mretryAfter = " + this.mRetryAfter + "]";
    }
}
