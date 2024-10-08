package com.sec.internal.constants.ims.entitilement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class AddAddressRequest {
    @SerializedName("locationRequest")
    public LocationRequest mLocationRequest;

    public static class LocationRequest {
        @SerializedName("location")
        public String mLocation;

        public LocationRequest(String str) {
            this.mLocation = str;
        }

        public String toString() {
            return "LocationRequest [mLocation = " + this.mLocation + "]";
        }
    }

    public AddAddressRequest(String str) {
        this.mLocationRequest = new LocationRequest(str);
    }

    public String toString() {
        return "AddAddressRequest [mLocationRequest = " + this.mLocationRequest + "]";
    }
}
