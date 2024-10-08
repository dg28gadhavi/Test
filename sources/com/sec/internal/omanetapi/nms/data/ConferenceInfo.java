package com.sec.internal.omanetapi.nms.data;

import com.google.gson.annotations.SerializedName;

public class ConferenceInfo {
    @SerializedName("conference-description")
    public ConferenceDescription mConferenceDescription;
    @SerializedName("conference-state")
    public ConferenceState mConferenceState;
    @SerializedName("created-by")
    public String mCreatedBy;
    @SerializedName("entity")
    public String mEntity;
    @SerializedName("state")
    public String mState;
    @SerializedName("timestamp")
    public String mTimestamp;
    @SerializedName("users")
    public Users mUsers;
}
