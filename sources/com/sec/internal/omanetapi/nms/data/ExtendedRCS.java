package com.sec.internal.omanetapi.nms.data;

import com.google.gson.annotations.SerializedName;

public class ExtendedRCS {
    @SerializedName("Reference-ID")
    public String mReferenceId;
    @SerializedName("Reference-Type")
    public int mReferenceType;
    @SerializedName("Reference-Value")
    public String mReferenceValue;
}
