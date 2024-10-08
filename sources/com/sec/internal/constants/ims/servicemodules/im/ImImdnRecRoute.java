package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.log.IMSLog;

public class ImImdnRecRoute {
    private int mId;
    private final String mImdnMsgId;
    private int mMessageId;
    private final String mUri;
    private final String mUserAlias;

    public ImImdnRecRoute(int i, int i2, String str, String str2, String str3) {
        this.mId = i;
        this.mMessageId = i2;
        this.mImdnMsgId = str;
        this.mUserAlias = str3;
        this.mUri = str2;
    }

    public ImImdnRecRoute(String str, String str2, String str3) {
        this.mImdnMsgId = str;
        this.mUserAlias = str3;
        this.mUri = str2;
    }

    public void setId(int i) {
        this.mId = i;
    }

    public int getMessageId() {
        return this.mMessageId;
    }

    public void setMessageId(int i) {
        this.mMessageId = i;
    }

    public String getImdnMsgId() {
        return this.mImdnMsgId;
    }

    public String getRecordRouteUri() {
        return this.mUri;
    }

    public String getRecordRouteDispName() {
        return this.mUserAlias;
    }

    public String toString() {
        return "ImImdnRecRoute [mId=" + this.mId + ", mMessageId=" + this.mMessageId + ", mImdnMsgId=" + this.mImdnMsgId + ", mUri=" + this.mUri + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + "]";
    }

    public int hashCode() {
        String str = this.mImdnMsgId;
        int i = 0;
        int hashCode = ((str == null ? 0 : str.hashCode()) + 31) * 31;
        String str2 = this.mUri;
        int hashCode2 = (hashCode + (str2 == null ? 0 : str2.hashCode())) * 31;
        String str3 = this.mUserAlias;
        if (str3 != null) {
            i = str3.hashCode();
        }
        return hashCode2 + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImImdnRecRoute imImdnRecRoute = (ImImdnRecRoute) obj;
        String str = this.mImdnMsgId;
        if (str == null) {
            if (imImdnRecRoute.mImdnMsgId != null) {
                return false;
            }
        } else if (!str.equals(imImdnRecRoute.mImdnMsgId)) {
            return false;
        }
        String str2 = this.mUri;
        if (str2 != null) {
            return str2.equals(imImdnRecRoute.mUri);
        }
        if (imImdnRecRoute.mUri == null) {
            return true;
        }
        return false;
    }
}
