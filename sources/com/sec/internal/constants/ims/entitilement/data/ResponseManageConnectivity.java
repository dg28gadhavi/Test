package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class ResponseManageConnectivity extends NSDSResponse {
    public static final Parcelable.Creator<ResponseManageConnectivity> CREATOR = new Parcelable.Creator<ResponseManageConnectivity>() {
        public ResponseManageConnectivity createFromParcel(Parcel parcel) {
            return new ResponseManageConnectivity(parcel);
        }

        public ResponseManageConnectivity[] newArray(int i) {
            return new ResponseManageConnectivity[i];
        }
    };
    public String certificate;
    @SerializedName("device-config")
    public String deviceConfig;
    @SerializedName("epdg-addresses")
    public ArrayList<String> epdgAddresses;
    @SerializedName("service-names")
    public ArrayList<ServiceName> serviceNames;
    @SerializedName("session-cookie")
    public String sessionCookie;

    public int describeContents() {
        return 0;
    }

    public ResponseManageConnectivity(Parcel parcel) {
        super(parcel);
        readFromParcel(parcel);
    }

    public void readFromParcel(Parcel parcel) {
        if (parcel.readByte() == 1) {
            this.certificate = parcel.readString();
        } else {
            this.certificate = null;
        }
        if (parcel.readByte() == 1) {
            ArrayList<String> arrayList = new ArrayList<>();
            this.epdgAddresses = arrayList;
            parcel.readList(arrayList, (ClassLoader) null);
        } else {
            this.epdgAddresses = null;
        }
        if (parcel.readByte() == 1) {
            ArrayList<ServiceName> arrayList2 = new ArrayList<>();
            this.serviceNames = arrayList2;
            parcel.readTypedList(arrayList2, ServiceName.CREATOR);
        } else {
            this.serviceNames = null;
        }
        if (parcel.readByte() == 1) {
            this.deviceConfig = parcel.readString();
        } else {
            this.deviceConfig = null;
        }
        if (parcel.readByte() == 1) {
            this.sessionCookie = parcel.readString();
        } else {
            this.sessionCookie = null;
        }
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.certificate == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.certificate);
        }
        if (this.epdgAddresses == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeList(this.epdgAddresses);
        }
        if (this.serviceNames == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeTypedList(this.serviceNames);
        }
        if (this.deviceConfig == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.deviceConfig);
        }
        if (this.sessionCookie == null) {
            parcel.writeByte((byte) 0);
            return;
        }
        parcel.writeByte((byte) 1);
        parcel.writeString(this.sessionCookie);
    }
}
