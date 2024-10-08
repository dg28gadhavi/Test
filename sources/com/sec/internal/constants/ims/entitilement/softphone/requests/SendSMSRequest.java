package com.sec.internal.constants.ims.entitilement.softphone.requests;

import com.google.gson.annotations.SerializedName;
import com.sec.internal.constants.ims.MIMEContentType;
import java.util.ArrayList;
import java.util.List;

public class SendSMSRequest {
    @SerializedName("to")
    public List<String> mCalleeNumber;
    @SerializedName("messageContent")
    public List<MessageContent> mMessageContent = new ArrayList();
    @SerializedName("replyAll")
    public boolean mReplyAll;

    public static class MessageContent {
        @SerializedName("body")
        public String mContent;
        @SerializedName("contentType")
        public String mContentType = MIMEContentType.PLAIN_TEXT;
        @SerializedName("contentTransferEncoding")
        public String mEncoding = "8bit";

        public MessageContent(String str) {
            this.mContent = str;
        }

        public String toString() {
            return "MessageContent [mContentType = " + this.mContentType + ", mEncoding = " + this.mEncoding + ", mContent = " + this.mContent + "]";
        }
    }

    public SendSMSRequest(boolean z, String str, String str2) {
        ArrayList arrayList = new ArrayList();
        this.mCalleeNumber = arrayList;
        this.mReplyAll = z;
        arrayList.add(str2);
        this.mMessageContent.add(new MessageContent(str));
    }

    public String toString() {
        return "SendSMSRequest [mReplyAll = " + this.mReplyAll + ", mMessageContent.size = " + this.mMessageContent.size() + ", mCalleeNumber.size = " + this.mCalleeNumber.size() + "]";
    }
}
