package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class InstanceToken implements Parcelable {
    public static final Parcelable.Creator<InstanceToken> CREATOR = new Parcelable.Creator<InstanceToken>() {
        public InstanceToken createFromParcel(Parcel parcel) {
            return new InstanceToken(parcel);
        }

        public InstanceToken[] newArray(int i) {
            return new InstanceToken[i];
        }
    };
    @SerializedName("expiration-time")
    public String expirationTime;
    @SerializedName("service-instance-token")
    public String serviceInstanceToken;

    public int describeContents() {
        return 0;
    }

    protected InstanceToken(Parcel parcel) {
        this.serviceInstanceToken = parcel.readString();
        this.expirationTime = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.serviceInstanceToken);
        parcel.writeString(this.expirationTime);
    }
}
