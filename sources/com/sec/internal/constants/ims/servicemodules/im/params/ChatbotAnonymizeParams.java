package com.sec.internal.constants.ims.servicemodules.im.params;

import com.sec.ims.util.ImsUri;

public class ChatbotAnonymizeParams {
    public String mAliasXml;
    public ImsUri mChatbotUri;
    public String mCommandId;
    public int mPhoneId;

    public ChatbotAnonymizeParams(int i, ImsUri imsUri, String str, String str2) {
        this.mPhoneId = i;
        this.mChatbotUri = imsUri;
        this.mAliasXml = str;
        this.mCommandId = str2;
    }

    public String toString() {
        return "ChatbotAnonymizeParams [chatbotURL=" + this.mChatbotUri.toString() + ", PhoneId = " + this.mPhoneId + ", mChatbotUri = " + this.mChatbotUri + ", mAliasXml = " + this.mAliasXml + ", mCommandId = " + this.mCommandId + "]";
    }
}
