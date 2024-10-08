package com.voltecrypt.service;

import android.os.Parcel;
import android.os.Parcelable;
import com.sec.internal.log.IMSLog;
import java.util.Objects;

public class SxRequestAuthenticationEntity implements Parcelable {
    public static final Parcelable.Creator<SxRequestAuthenticationEntity> CREATOR = new Parcelable.Creator<SxRequestAuthenticationEntity>() {
        public SxRequestAuthenticationEntity createFromParcel(Parcel parcel) {
            return new SxRequestAuthenticationEntity(parcel);
        }

        public SxRequestAuthenticationEntity[] newArray(int i) {
            return new SxRequestAuthenticationEntity[i];
        }
    };
    private String appKey;
    private String appPackageName;
    private String organizationCode;
    private String requestMark;
    private long time = System.currentTimeMillis();

    public int describeContents() {
        return 0;
    }

    public SxRequestAuthenticationEntity() {
    }

    public SxRequestAuthenticationEntity(String str, String str2, String str3, String str4) {
        this.organizationCode = str;
        this.appPackageName = str2;
        this.appKey = str3;
        this.requestMark = str4;
    }

    protected SxRequestAuthenticationEntity(Parcel parcel) {
        this.time = parcel.readLong();
        this.organizationCode = parcel.readString();
        this.appPackageName = parcel.readString();
        this.appKey = parcel.readString();
        this.requestMark = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.time);
        parcel.writeString(this.organizationCode);
        parcel.writeString(this.appPackageName);
        parcel.writeString(this.appKey);
        parcel.writeString(this.requestMark);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SxRequestAuthenticationEntity sxRequestAuthenticationEntity = (SxRequestAuthenticationEntity) obj;
        if (!Objects.equals(Long.valueOf(this.time), Long.valueOf(sxRequestAuthenticationEntity.time)) || !Objects.equals(this.organizationCode, sxRequestAuthenticationEntity.organizationCode) || !Objects.equals(this.appPackageName, sxRequestAuthenticationEntity.appPackageName) || !Objects.equals(this.appKey, sxRequestAuthenticationEntity.appKey) || !Objects.equals(this.requestMark, sxRequestAuthenticationEntity.requestMark)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Long.valueOf(this.time), this.organizationCode, this.appPackageName, this.appKey, this.requestMark});
    }

    public String toString() {
        return "SxRequestAuthenticationEntity{time='" + this.time + '\'' + ", organizationCode='" + this.organizationCode + '\'' + ", appPackageName='" + this.appPackageName + '\'' + ", appKey='" + IMSLog.checker(this.appKey) + '\'' + ", requestMark=" + this.requestMark + '}';
    }
}
