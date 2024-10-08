package com.sec.internal.constants.ims.entitilement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class ExchangeForAccessTokenRequest {
    @SerializedName("client_id")
    public String mClientId;
    @SerializedName("client_secret")
    public String mClientSecret;
    @SerializedName("grant_type")
    public String mGrantType;
    @SerializedName("password")
    public String mPassword;
    @SerializedName("scope")
    public String mScope;
    @SerializedName("username")
    public String mUsername;

    public ExchangeForAccessTokenRequest(String str, String str2, String str3, String str4, String str5, String str6) {
        this.mClientId = str;
        this.mClientSecret = str2;
        this.mUsername = str3;
        this.mGrantType = str4;
        this.mPassword = str5;
        this.mScope = str6;
    }

    public String toString() {
        return "ExchangeForAccessTokenRequest [mClientId = " + this.mClientId + ", mClientSecret = " + this.mClientSecret + ", mUsername = " + this.mUsername + ", mGrantType = " + this.mGrantType + ", mPassword = " + this.mPassword + ", mScope = " + this.mScope + "]";
    }
}
