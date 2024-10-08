package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ResponseGetMSISDN extends NSDSResponse {
    public static final Parcelable.Creator<ResponseGetMSISDN> CREATOR = new Parcelable.Creator<ResponseGetMSISDN>() {
        public ResponseGetMSISDN createFromParcel(Parcel parcel) {
            return new ResponseGetMSISDN(parcel);
        }

        public ResponseGetMSISDN[] newArray(int i) {
            return new ResponseGetMSISDN[i];
        }
    };
    public String msisdn;
    @SerializedName("service-fingerprint")
    public String serviceFingerprint;

    public int describeContents() {
        return 0;
    }

    protected ResponseGetMSISDN(Parcel parcel) {
        super(parcel);
        readFromParcel(parcel);
    }

    public void readFromParcel(Parcel parcel) {
        this.msisdn = parcel.readString();
        this.serviceFingerprint = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.msisdn);
        parcel.writeString(this.serviceFingerprint);
    }
}
