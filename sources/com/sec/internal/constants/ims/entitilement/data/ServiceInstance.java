package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ServiceInstance implements Parcelable {
    public static final transient Parcelable.Creator<ServiceInstance> CREATOR = new Parcelable.Creator<ServiceInstance>() {
        public ServiceInstance createFromParcel(Parcel parcel) {
            return new ServiceInstance(parcel);
        }

        public ServiceInstance[] newArray(int i) {
            return new ServiceInstance[i];
        }
    };
    @SerializedName("config-parameters")
    public String configParameters;
    @SerializedName("end-time")
    public String endTime;
    @SerializedName("expiration-time")
    public Integer expirationTime;
    @SerializedName("friendly-name")
    public String friendlyName;
    @SerializedName("is-owner")
    public Boolean isOwner;
    public String msisdn;
    @SerializedName("provisioning-parameters")
    public ProvisioningParameters provisioningParameters;
    @SerializedName("service-instance-id")
    public String serviceInstanceId;
    @SerializedName("service-name")
    public String serviceName;

    public int describeContents() {
        return 0;
    }

    public ServiceInstance() {
    }

    protected ServiceInstance(Parcel parcel) {
        Boolean bool;
        this.serviceName = parcel.readString();
        this.serviceInstanceId = parcel.readString();
        byte readByte = parcel.readByte();
        Integer num = null;
        if (readByte == 2) {
            bool = null;
        } else {
            bool = Boolean.valueOf(readByte != 0);
        }
        this.isOwner = bool;
        this.endTime = parcel.readString();
        this.expirationTime = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
        this.msisdn = parcel.readString();
        this.friendlyName = parcel.readString();
        this.provisioningParameters = (ProvisioningParameters) parcel.readValue(ProvisioningParameters.class.getClassLoader());
        this.configParameters = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.serviceName);
        parcel.writeString(this.serviceInstanceId);
        Boolean bool = this.isOwner;
        if (bool == null) {
            parcel.writeByte((byte) 2);
        } else {
            parcel.writeByte(bool.booleanValue() ? (byte) 1 : 0);
        }
        parcel.writeString(this.endTime);
        if (this.expirationTime == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.expirationTime.intValue());
        }
        parcel.writeString(this.msisdn);
        parcel.writeString(this.friendlyName);
        parcel.writeValue(this.provisioningParameters);
        parcel.writeString(this.configParameters);
    }
}
