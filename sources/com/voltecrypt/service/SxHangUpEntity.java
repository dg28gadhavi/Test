package com.voltecrypt.service;

import android.os.Parcel;
import android.os.Parcelable;
import com.sec.internal.log.IMSLog;
import java.util.Objects;

public class SxHangUpEntity implements Parcelable {
    public static final Parcelable.Creator<SxHangUpEntity> CREATOR = new Parcelable.Creator<SxHangUpEntity>() {
        public SxHangUpEntity createFromParcel(Parcel parcel) {
            return new SxHangUpEntity(parcel);
        }

        public SxHangUpEntity[] newArray(int i) {
            return new SxHangUpEntity[i];
        }
    };
    private String callId;
    private String callPhoneNum;
    private String calledPhoneNum;
    private String des;
    private int isEncrypt;
    private int reason;
    private String sessionId;
    private long time = System.currentTimeMillis();
    private int type;

    public int describeContents() {
        return 0;
    }

    public SxHangUpEntity() {
    }

    public SxHangUpEntity(String str, String str2, int i, String str3, int i2, int i3, String str4, String str5) {
        this.callPhoneNum = str;
        this.calledPhoneNum = str2;
        this.type = i;
        this.sessionId = str3;
        this.isEncrypt = i2;
        this.reason = i3;
        this.des = str4;
        this.callId = str5;
    }

    protected SxHangUpEntity(Parcel parcel) {
        this.time = parcel.readLong();
        this.callPhoneNum = parcel.readString();
        this.calledPhoneNum = parcel.readString();
        this.type = parcel.readInt();
        this.sessionId = parcel.readString();
        this.isEncrypt = parcel.readInt();
        this.reason = parcel.readInt();
        this.des = parcel.readString();
        this.callId = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.time);
        parcel.writeString(this.callPhoneNum);
        parcel.writeString(this.calledPhoneNum);
        parcel.writeInt(this.type);
        parcel.writeString(this.sessionId);
        parcel.writeInt(this.isEncrypt);
        parcel.writeInt(this.reason);
        parcel.writeString(this.des);
        parcel.writeString(this.callId);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SxHangUpEntity sxHangUpEntity = (SxHangUpEntity) obj;
        if (!Objects.equals(Long.valueOf(this.time), Long.valueOf(sxHangUpEntity.time)) || !Objects.equals(this.callPhoneNum, sxHangUpEntity.callPhoneNum) || !Objects.equals(this.calledPhoneNum, sxHangUpEntity.calledPhoneNum) || !Objects.equals(Integer.valueOf(this.type), Integer.valueOf(sxHangUpEntity.type)) || !Objects.equals(this.sessionId, sxHangUpEntity.sessionId) || !Objects.equals(Integer.valueOf(this.isEncrypt), Integer.valueOf(sxHangUpEntity.isEncrypt)) || !Objects.equals(Integer.valueOf(this.reason), Integer.valueOf(sxHangUpEntity.reason)) || !Objects.equals(this.des, sxHangUpEntity.des) || !Objects.equals(this.callId, sxHangUpEntity.callId)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Long.valueOf(this.time), this.callPhoneNum, this.calledPhoneNum, Integer.valueOf(this.type), this.sessionId, Integer.valueOf(this.isEncrypt), Integer.valueOf(this.reason), this.des, this.callId});
    }

    public String toString() {
        return "SxHangUpEntity{time='" + this.time + '\'' + ", callPhoneNum='" + IMSLog.checker(this.callPhoneNum) + '\'' + ", calledPhoneNum='" + IMSLog.checker(this.calledPhoneNum) + '\'' + ", type='" + this.type + '\'' + ", sessionId='" + IMSLog.checker(this.sessionId) + '\'' + ", isEncrypt='" + this.isEncrypt + '\'' + ", reason='" + this.reason + '\'' + ", des='" + this.des + '\'' + ", callId=" + IMSLog.checker(this.callId) + '}';
    }
}
