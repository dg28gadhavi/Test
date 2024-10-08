package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class ResponseRegisteredDevices extends NSDSResponse {
    public static final Parcelable.Creator<ResponseRegisteredDevices> CREATOR = new Parcelable.Creator<ResponseRegisteredDevices>() {
        public ResponseRegisteredDevices createFromParcel(Parcel parcel) {
            return new ResponseRegisteredDevices(parcel);
        }

        public ResponseRegisteredDevices[] newArray(int i) {
            return new ResponseRegisteredDevices[i];
        }
    };
    @SerializedName("device-info")
    public ArrayList<DeviceInstance> deviceInstance;

    public int describeContents() {
        return 0;
    }

    protected ResponseRegisteredDevices(Parcel parcel) {
        super(parcel);
        if (parcel.readByte() == 1) {
            ArrayList<DeviceInstance> arrayList = new ArrayList<>();
            this.deviceInstance = arrayList;
            parcel.readList(arrayList, DeviceInstance.class.getClassLoader());
            return;
        }
        this.deviceInstance = null;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.deviceInstance == null) {
            parcel.writeByte((byte) 0);
            return;
        }
        parcel.writeByte((byte) 1);
        parcel.writeList(this.deviceInstance);
    }
}
