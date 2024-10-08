package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Response3gppAuthentication extends NSDSResponse {
    public static final Parcelable.Creator<Response3gppAuthentication> CREATOR = new Parcelable.Creator<Response3gppAuthentication>() {
        public Response3gppAuthentication createFromParcel(Parcel parcel) {
            return new Response3gppAuthentication(parcel);
        }

        public Response3gppAuthentication[] newArray(int i) {
            return new Response3gppAuthentication[i];
        }
    };
    @SerializedName("aka-challenge")
    public String akaChallenge;
    @SerializedName("aka-token")
    public String akaToken;

    public int describeContents() {
        return 0;
    }

    public Response3gppAuthentication(Parcel parcel) {
        super(parcel);
        this.akaChallenge = parcel.readString();
        this.akaToken = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.akaChallenge);
        parcel.writeString(this.akaToken);
    }
}
