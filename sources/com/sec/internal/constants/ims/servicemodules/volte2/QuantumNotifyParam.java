package com.sec.internal.constants.ims.servicemodules.volte2;

import com.sec.internal.log.IMSLog;
import java.util.Arrays;

public class QuantumNotifyParam {
    String mComment;
    byte[] mKey;
    String mQtSessionId;
    String mRequestMark;
    int mStatus;
    String mToken;

    public int getStatus() {
        return this.mStatus;
    }

    public void setStatus(int i) {
        this.mStatus = i;
    }

    public String getComment() {
        return this.mComment;
    }

    public void setComment(String str) {
        this.mComment = str;
    }

    public String getRequestMark() {
        return this.mRequestMark;
    }

    public void setRequestMark(String str) {
        this.mRequestMark = str;
    }

    public String getToken() {
        return this.mToken;
    }

    public void setToken(String str) {
        this.mToken = str;
    }

    public String getQtSessionId() {
        return this.mQtSessionId;
    }

    public void setQtSessionId(String str) {
        this.mQtSessionId = str;
    }

    public byte[] getKey() {
        return this.mKey;
    }

    public void setKey(byte[] bArr) {
        this.mKey = bArr;
    }

    public QuantumNotifyParam(Builder builder) {
        this.mStatus = builder.mStatus;
        this.mComment = builder.mComment;
        this.mRequestMark = builder.mRequestMark;
        this.mToken = builder.mToken;
        this.mQtSessionId = builder.mQtSessionId;
        this.mKey = builder.mKey;
    }

    public String toString() {
        return "QuantumNotifyParam{mStatus=" + this.mStatus + ", mComment='" + this.mComment + '\'' + ", mRequestMark='" + this.mRequestMark + '\'' + ", mToken='" + this.mToken + '\'' + ", mQtSessionId='" + IMSLog.checker(this.mQtSessionId) + '\'' + ", mKey=" + IMSLog.checker(Arrays.toString(this.mKey)) + '}';
    }

    public static class Builder {
        String mComment;
        byte[] mKey;
        String mQtSessionId;
        String mRequestMark;
        int mStatus;
        String mToken;

        public QuantumNotifyParam build() {
            return new QuantumNotifyParam(this);
        }

        public Builder setStatus(int i) {
            this.mStatus = i;
            return this;
        }

        public Builder setComment(String str) {
            this.mComment = str;
            return this;
        }

        public Builder setRequestMark(String str) {
            this.mRequestMark = str;
            return this;
        }

        public Builder setToken(String str) {
            this.mToken = str;
            return this;
        }

        public Builder setQtSessionId(String str) {
            this.mQtSessionId = str;
            return this;
        }

        public Builder setKey(byte[] bArr) {
            this.mKey = bArr;
            return this;
        }
    }
}
