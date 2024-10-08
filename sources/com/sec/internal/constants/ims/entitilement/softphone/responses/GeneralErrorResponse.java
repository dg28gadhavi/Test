package com.sec.internal.constants.ims.entitilement.softphone.responses;

import com.google.gson.annotations.SerializedName;

public class GeneralErrorResponse {
    @SerializedName("error")
    public String mError;

    public String toString() {
        return "GeneralErrorResponse [mError = " + this.mError + "]";
    }
}
