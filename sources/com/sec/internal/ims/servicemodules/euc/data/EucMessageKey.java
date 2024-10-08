package com.sec.internal.ims.servicemodules.euc.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.sec.ims.util.ImsUri;

public final class EucMessageKey implements Parcelable {
    public static final Parcelable.Creator<EucMessageKey> CREATOR = new Parcelable.Creator<EucMessageKey>() {
        public EucMessageKey createFromParcel(Parcel parcel) {
            return new EucMessageKey(parcel);
        }

        public EucMessageKey[] newArray(int i) {
            return new EucMessageKey[i];
        }
    };
    private String mEucId;
    private EucType mEucType;
    private String mOwnIdentity;
    private ImsUri mRemoteUri;

    public int describeContents() {
        return 0;
    }

    public String getEucId() {
        return this.mEucId;
    }

    public String getOwnIdentity() {
        return this.mOwnIdentity;
    }

    public EucType getEucType() {
        return this.mEucType;
    }

    public ImsUri getRemoteUri() {
        return this.mRemoteUri;
    }

    public EucMessageKey(String str, String str2, EucType eucType, ImsUri imsUri) {
        this.mEucId = str;
        this.mOwnIdentity = str2;
        this.mEucType = eucType;
        this.mRemoteUri = imsUri;
    }

    public EucMessageKey(Parcel parcel) {
        this.mEucId = parcel.readString();
        this.mOwnIdentity = parcel.readString();
        this.mEucType = (EucType) parcel.readSerializable();
        this.mRemoteUri = (ImsUri) parcel.readTypedObject(ImsUri.CREATOR);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || EucMessageKey.class != obj.getClass()) {
            return false;
        }
        EucMessageKey eucMessageKey = (EucMessageKey) obj;
        if (!this.mEucId.equals(eucMessageKey.mEucId) || !this.mOwnIdentity.equals(eucMessageKey.mOwnIdentity) || this.mEucType != eucMessageKey.mEucType || !this.mRemoteUri.equals(eucMessageKey.mRemoteUri)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((((this.mEucId.hashCode() * 31) + this.mOwnIdentity.hashCode()) * 31) + this.mEucType.hashCode()) * 31) + this.mRemoteUri.hashCode();
    }

    public String toString() {
        return "EucMessageKey[mEucId='" + this.mEucId + '\'' + ", mOwnIdentity='" + this.mOwnIdentity + '\'' + ", mEucType=" + this.mEucType + ", mRemoteUri='" + this.mRemoteUri + '\'' + ']';
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mEucId);
        parcel.writeString(this.mOwnIdentity);
        parcel.writeSerializable(this.mEucType);
        parcel.writeTypedObject(this.mRemoteUri, i);
    }

    public byte[] marshall() {
        Parcel obtain = Parcel.obtain();
        writeToParcel(obtain, 0);
        byte[] marshall = obtain.marshall();
        obtain.recycle();
        return marshall;
    }

    public static EucMessageKey unmarshall(byte[] bArr) {
        Parcel obtain = Parcel.obtain();
        obtain.unmarshall(bArr, 0, bArr.length);
        obtain.setDataPosition(0);
        EucMessageKey createFromParcel = CREATOR.createFromParcel(obtain);
        obtain.recycle();
        return createFromParcel;
    }
}
