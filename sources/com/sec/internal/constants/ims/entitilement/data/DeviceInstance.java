package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class DeviceInstance implements Parcelable {
    @SerializedName("device-id")
    public String deviceId;
    @SerializedName("device-name")
    public String deviceName;
    @SerializedName("device-type")
    public int deviceType;
    @SerializedName("service-instances")
    public ArrayList<ServiceInstance> serviceInstances;

    public int describeContents() {
        return 0;
    }

    protected DeviceInstance(Parcel parcel) {
        this.deviceId = parcel.readString();
        this.deviceName = parcel.readString();
        this.deviceType = parcel.readInt();
        if (parcel.readByte() == 1) {
            ArrayList<ServiceInstance> arrayList = new ArrayList<>();
            this.serviceInstances = arrayList;
            parcel.readList(arrayList, ServiceInstance.class.getClassLoader());
            return;
        }
        this.serviceInstances = null;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.deviceId);
        parcel.writeString(this.deviceName);
        parcel.writeInt(this.deviceType);
        if (this.serviceInstances == null) {
            parcel.writeByte((byte) 0);
            return;
        }
        parcel.writeByte((byte) 1);
        parcel.writeList(this.serviceInstances);
    }
}
