package com.sec.internal.helper.httpclient;

import android.os.Parcel;
import android.os.Parcelable;
import java.net.HttpCookie;

public class HttpCookieParcelable implements Parcelable {
    public static final Parcelable.Creator<HttpCookieParcelable> CREATOR = new Parcelable.Creator<HttpCookieParcelable>() {
        public HttpCookieParcelable[] newArray(int i) {
            return new HttpCookieParcelable[i];
        }

        public HttpCookieParcelable createFromParcel(Parcel parcel) {
            return new HttpCookieParcelable(parcel);
        }
    };
    private HttpCookie cookie;

    public int describeContents() {
        return 0;
    }

    public HttpCookieParcelable(HttpCookie httpCookie) {
        this.cookie = httpCookie;
    }

    public HttpCookieParcelable(Parcel parcel) {
        HttpCookie httpCookie = new HttpCookie(parcel.readString(), parcel.readString());
        this.cookie = httpCookie;
        httpCookie.setComment(parcel.readString());
        this.cookie.setCommentURL(parcel.readString());
        boolean z = true;
        this.cookie.setDiscard(parcel.readByte() != 0);
        this.cookie.setDomain(parcel.readString());
        this.cookie.setMaxAge(parcel.readLong());
        this.cookie.setPath(parcel.readString());
        this.cookie.setPortlist(parcel.readString());
        this.cookie.setSecure(parcel.readByte() == 0 ? false : z);
        this.cookie.setVersion(parcel.readInt());
    }

    public HttpCookie getCookie() {
        return this.cookie;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.cookie.getName());
        parcel.writeString(this.cookie.getValue());
        parcel.writeString(this.cookie.getComment());
        parcel.writeString(this.cookie.getCommentURL());
        parcel.writeByte(this.cookie.getDiscard() ? (byte) 1 : 0);
        parcel.writeString(this.cookie.getDomain());
        parcel.writeLong(this.cookie.getMaxAge());
        parcel.writeString(this.cookie.getPath());
        parcel.writeString(this.cookie.getPortlist());
        parcel.writeByte(this.cookie.getSecure() ? (byte) 1 : 0);
        parcel.writeInt(this.cookie.getVersion());
    }
}
