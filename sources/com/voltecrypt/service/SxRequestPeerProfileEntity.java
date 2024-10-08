package com.voltecrypt.service;

import android.os.Parcel;
import android.os.Parcelable;
import com.sec.internal.log.IMSLog;
import java.util.Objects;

public class SxRequestPeerProfileEntity implements Parcelable {
    public static final Parcelable.Creator<SxRequestPeerProfileEntity> CREATOR = new Parcelable.Creator<SxRequestPeerProfileEntity>() {
        public SxRequestPeerProfileEntity createFromParcel(Parcel parcel) {
            return new SxRequestPeerProfileEntity(parcel);
        }

        public SxRequestPeerProfileEntity[] newArray(int i) {
            return new SxRequestPeerProfileEntity[i];
        }
    };
    private String callId;
    private String callPhoneNum;
    private String calledPhoneNum;
    private String requestMark;
    private long time = System.currentTimeMillis();
    private int type;

    public int describeContents() {
        return 0;
    }

    public SxRequestPeerProfileEntity() {
    }

    public SxRequestPeerProfileEntity(String str, String str2, int i, String str3, String str4) {
        this.callPhoneNum = str;
        this.calledPhoneNum = str2;
        this.type = i;
        this.callId = str3;
        this.requestMark = str4;
    }

    public String getRequestMark() {
        return this.requestMark;
    }

    protected SxRequestPeerProfileEntity(Parcel parcel) {
        this.time = parcel.readLong();
        this.callPhoneNum = parcel.readString();
        this.calledPhoneNum = parcel.readString();
        this.type = parcel.readInt();
        this.callId = parcel.readString();
        this.requestMark = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.time);
        parcel.writeString(this.callPhoneNum);
        parcel.writeString(this.calledPhoneNum);
        parcel.writeInt(this.type);
        parcel.writeString(this.callId);
        parcel.writeString(this.requestMark);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SxRequestPeerProfileEntity sxRequestPeerProfileEntity = (SxRequestPeerProfileEntity) obj;
        if (!Objects.equals(Long.valueOf(this.time), Long.valueOf(sxRequestPeerProfileEntity.time)) || !Objects.equals(this.callPhoneNum, sxRequestPeerProfileEntity.callPhoneNum) || !Objects.equals(this.calledPhoneNum, sxRequestPeerProfileEntity.calledPhoneNum) || !Objects.equals(Integer.valueOf(this.type), Integer.valueOf(sxRequestPeerProfileEntity.type)) || !Objects.equals(this.callId, sxRequestPeerProfileEntity.callId) || !Objects.equals(this.requestMark, sxRequestPeerProfileEntity.requestMark)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Long.valueOf(this.time), this.callPhoneNum, this.calledPhoneNum, Integer.valueOf(this.type), this.callId, this.requestMark});
    }

    public String toString() {
        return "SxRequestPeerProfileEntity{time='" + this.time + '\'' + ", callPhoneNum='" + IMSLog.checker(this.callPhoneNum) + '\'' + ", calledPhoneNum='" + IMSLog.checker(this.calledPhoneNum) + '\'' + ", type='" + this.type + '\'' + ", callId='" + IMSLog.checker(this.callId) + '\'' + ", requestMark=" + this.requestMark + '}';
    }
}
