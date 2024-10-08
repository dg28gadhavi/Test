package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ResponseManageService extends NSDSResponse {
    public static final Parcelable.Creator<ResponseManageService> CREATOR = new Parcelable.Creator<ResponseManageService>() {
        public ResponseManageService createFromParcel(Parcel parcel) {
            return new ResponseManageService(parcel);
        }

        public ResponseManageService[] newArray(int i) {
            return new ResponseManageService[i];
        }
    };
    @SerializedName("instance-token")
    public InstanceToken instanceToken;
    @SerializedName("service-instance")
    public ServiceInstance serviceInstance;

    public int describeContents() {
        return 0;
    }

    protected ResponseManageService(Parcel parcel) {
        super(parcel);
        this.serviceInstance = (ServiceInstance) parcel.readValue(ServiceInstance.class.getClassLoader());
        this.instanceToken = (InstanceToken) parcel.readValue(InstanceToken.class.getClassLoader());
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeValue(this.serviceInstance);
        parcel.writeValue(this.instanceToken);
    }
}
