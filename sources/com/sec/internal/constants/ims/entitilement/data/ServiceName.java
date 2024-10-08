package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ServiceName implements Parcelable {
    public static final Parcelable.Creator<ServiceName> CREATOR = new Parcelable.Creator<ServiceName>() {
        public ServiceName createFromParcel(Parcel parcel) {
            return new ServiceName(parcel);
        }

        public ServiceName[] newArray(int i) {
            return new ServiceName[i];
        }
    };
    @SerializedName("appstore-url")
    public String appstoreUrl;
    @SerializedName("client-id")
    public String clientId;
    @SerializedName("package-name")
    public String packageName;
    @SerializedName("service-name")
    public String serviceName;

    public int describeContents() {
        return 0;
    }

    protected ServiceName(Parcel parcel) {
        readFromParcel(parcel);
    }

    public void readFromParcel(Parcel parcel) {
        this.serviceName = parcel.readString();
        this.clientId = parcel.readString();
        this.packageName = parcel.readString();
        this.appstoreUrl = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.serviceName);
        parcel.writeString(this.clientId);
        parcel.writeString(this.packageName);
        parcel.writeString(this.appstoreUrl);
    }

    public String toString() {
        return "serviceName:" + this.serviceName + " clientId:" + this.clientId + " packageName:" + this.packageName + " appstoreUrl:" + this.appstoreUrl;
    }
}
