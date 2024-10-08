package com.sec.internal.constants.ims.entitilement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class RevokeTokenRequest {
    @SerializedName("client_id")
    public String mClientId;
    @SerializedName("client_secret")
    public String mClientSecret;
    @SerializedName("token")
    public String mToken;
    @SerializedName("token_type_hint")
    public String mTokenType;

    public RevokeTokenRequest(String str, String str2, String str3, String str4) {
        this.mClientId = str;
        this.mClientSecret = str2;
        this.mToken = str3;
        this.mTokenType = str4;
    }

    public String toString() {
        return "RevokeTokenRequest [mClientId = " + this.mClientId + ", mClientSecret = " + this.mClientSecret + ", mToken = " + this.mToken + ", mTokenType = " + this.mTokenType + "]";
    }
}
