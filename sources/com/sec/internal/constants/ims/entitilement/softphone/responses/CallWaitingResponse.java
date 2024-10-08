package com.sec.internal.constants.ims.entitilement.softphone.responses;

import com.google.gson.annotations.SerializedName;

public class CallWaitingResponse extends SoftphoneResponse {
    @SerializedName("@active")
    public String mActive;
}
