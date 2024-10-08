package com.sec.internal.ims.cmstore.callHandling.errorHandling;

public class OmaErrorKey {
    String mApiClass;
    int mErrorCode;
    String mHandlerClass;

    public OmaErrorKey(int i, String str, String str2) {
        this.mErrorCode = i;
        this.mApiClass = str;
        this.mHandlerClass = str2;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof OmaErrorKey) {
            OmaErrorKey omaErrorKey = (OmaErrorKey) obj;
            return omaErrorKey.mErrorCode == this.mErrorCode && omaErrorKey.mApiClass.equals(this.mApiClass) && omaErrorKey.mHandlerClass.equals(this.mHandlerClass);
        }
    }

    public int hashCode() {
        return this.mErrorCode + this.mApiClass.hashCode() + this.mHandlerClass.hashCode();
    }

    public String toString() {
        return "OmaErrorKey = [ mErrorCode = " + this.mErrorCode + " ], [ mApiClass = " + this.mApiClass + " ], [ mHandlerClass = " + this.mHandlerClass + " ].";
    }
}
