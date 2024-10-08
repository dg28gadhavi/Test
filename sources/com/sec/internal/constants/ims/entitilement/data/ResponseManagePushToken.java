package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ResponseManagePushToken extends NSDSResponse {
    public static final Parcelable.Creator<ResponseManagePushToken> CREATOR = new Parcelable.Creator<ResponseManagePushToken>() {
        public ResponseManagePushToken createFromParcel(Parcel parcel) {
            return new ResponseManagePushToken(parcel);
        }

        public ResponseManagePushToken[] newArray(int i) {
            return new ResponseManagePushToken[i];
        }
    };

    public int describeContents() {
        return 0;
    }

    public ResponseManagePushToken(Parcel parcel) {
        super(parcel);
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
