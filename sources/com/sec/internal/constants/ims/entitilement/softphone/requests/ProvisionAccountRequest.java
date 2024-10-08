package com.sec.internal.constants.ims.entitilement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class ProvisionAccountRequest {
    @SerializedName("provisionSPRequest")
    public ProvisionSPRequest mProvisionSPRequest;

    public static class ProvisionSPRequest {
        @SerializedName("tcAccept")
        public String mTcAccept;

        public ProvisionSPRequest(String str) {
            this.mTcAccept = str;
        }

        public String toString() {
            return "ProvisionSPRequest [mTcAccept = " + this.mTcAccept + "]";
        }
    }

    public ProvisionAccountRequest(String str) {
        this.mProvisionSPRequest = new ProvisionSPRequest(str);
    }

    public String toString() {
        return "ProvisionAccountRequest [mProvisionSPRequest = " + this.mProvisionSPRequest + "]";
    }
}
