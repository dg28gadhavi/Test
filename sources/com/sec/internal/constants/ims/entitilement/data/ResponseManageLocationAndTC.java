package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ResponseManageLocationAndTC extends NSDSResponse {
    public static final Parcelable.Creator<ResponseManageLocationAndTC> CREATOR = new Parcelable.Creator<ResponseManageLocationAndTC>() {
        public ResponseManageLocationAndTC createFromParcel(Parcel parcel) {
            return new ResponseManageLocationAndTC(parcel);
        }

        public ResponseManageLocationAndTC[] newArray(int i) {
            return new ResponseManageLocationAndTC[i];
        }
    };
    @SerializedName("address-id")
    public String addressId;
    @SerializedName("aid-expiration")
    public String aidExpiration;
    @SerializedName("location-status")
    public Boolean locationStatus;
    @SerializedName("server-data")
    public String serverData;
    @SerializedName("server-url")
    public String serverUrl;
    @SerializedName("service-status")
    public Integer serviceStatus;
    @SerializedName("tc-status")
    public Boolean tcStatus;

    public int describeContents() {
        return 0;
    }

    protected ResponseManageLocationAndTC(Parcel parcel) {
        super(parcel);
        Boolean bool;
        Boolean bool2;
        byte readByte = parcel.readByte();
        boolean z = true;
        Integer num = null;
        if (readByte == 2) {
            bool = null;
        } else {
            bool = Boolean.valueOf(readByte != 0);
        }
        this.locationStatus = bool;
        byte readByte2 = parcel.readByte();
        if (readByte2 == 2) {
            bool2 = null;
        } else {
            bool2 = Boolean.valueOf(readByte2 == 0 ? false : z);
        }
        this.tcStatus = bool2;
        this.serviceStatus = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
        this.serverData = parcel.readString();
        this.serverUrl = parcel.readString();
        this.addressId = parcel.readString();
        this.aidExpiration = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        Boolean bool = this.locationStatus;
        if (bool == null) {
            parcel.writeByte((byte) 2);
        } else {
            parcel.writeByte(bool.booleanValue() ? (byte) 1 : 0);
        }
        Boolean bool2 = this.tcStatus;
        if (bool2 == null) {
            parcel.writeByte((byte) 2);
        } else {
            parcel.writeByte(bool2.booleanValue() ? (byte) 1 : 0);
        }
        if (this.serviceStatus == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.serviceStatus.intValue());
        }
        parcel.writeString(this.serverData);
        parcel.writeString(this.serverUrl);
        parcel.writeString(this.addressId);
        parcel.writeString(this.aidExpiration);
    }
}
