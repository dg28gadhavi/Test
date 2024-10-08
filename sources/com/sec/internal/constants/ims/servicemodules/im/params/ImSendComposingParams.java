package com.sec.internal.constants.ims.servicemodules.im.params;

public class ImSendComposingParams {
    public final int mInterval;
    public final boolean mIsComposing;
    public final Object mRawHandle;
    public final String mUserAlias;

    public ImSendComposingParams(Object obj, boolean z, int i, String str) {
        this.mRawHandle = obj;
        this.mIsComposing = z;
        this.mInterval = i;
        this.mUserAlias = str;
    }

    public String toString() {
        return "ImSendComposingParams [mRawHandle=" + this.mRawHandle + ", mIsComposing=" + this.mIsComposing + ", mInterval=" + this.mInterval + "]";
    }
}
