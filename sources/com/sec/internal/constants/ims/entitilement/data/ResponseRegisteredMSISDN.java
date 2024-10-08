package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class ResponseRegisteredMSISDN extends NSDSResponse {
    public static final Parcelable.Creator<ResponseRegisteredMSISDN> CREATOR = new Parcelable.Creator<ResponseRegisteredMSISDN>() {
        public ResponseRegisteredMSISDN createFromParcel(Parcel parcel) {
            return new ResponseRegisteredMSISDN(parcel);
        }

        public ResponseRegisteredMSISDN[] newArray(int i) {
            return new ResponseRegisteredMSISDN[i];
        }
    };
    @SerializedName("registered-msisdns")
    public ArrayList<RegisteredMSISDN> registeredMSISDNs;

    public int describeContents() {
        return 0;
    }

    protected ResponseRegisteredMSISDN(Parcel parcel) {
        super(parcel);
        if (parcel.readByte() == 1) {
            ArrayList<RegisteredMSISDN> arrayList = new ArrayList<>();
            this.registeredMSISDNs = arrayList;
            parcel.readList(arrayList, RegisteredMSISDN.class.getClassLoader());
            return;
        }
        this.registeredMSISDNs = null;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.registeredMSISDNs == null) {
            parcel.writeByte((byte) 0);
            return;
        }
        parcel.writeByte((byte) 1);
        parcel.writeList(this.registeredMSISDNs);
    }
}
