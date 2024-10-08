package com.sec.internal.omanetapi.nms.data;

import com.google.gson.annotations.SerializedName;

public class ConferenceState {
    @SerializedName("active")
    public boolean mActivation;
    @SerializedName("user-count")
    public int mUserCount;
}
