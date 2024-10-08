package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.SupportedFeature;
import java.util.EnumSet;
import java.util.List;

public class ImSessionEstablishedEvent {
    public List<String> mAcceptTypes;
    public List<String> mAcceptWrappedTypes;
    public String mChatId;
    public EnumSet<SupportedFeature> mFeatures;
    public Object mRawHandle;
    public ImsUri mSessionUri;

    public ImSessionEstablishedEvent(Object obj, String str, ImsUri imsUri, EnumSet<SupportedFeature> enumSet, List<String> list, List<String> list2) {
        this.mRawHandle = obj;
        this.mChatId = str;
        this.mSessionUri = imsUri;
        this.mFeatures = enumSet;
        this.mAcceptTypes = list;
        this.mAcceptWrappedTypes = list2;
    }

    public String toString() {
        return "ImSessionEstablishedEvent [mRawHandle=" + this.mRawHandle + ", mChatId=" + this.mChatId + ", mSessionUri=" + this.mSessionUri + ", mFeatures=" + this.mFeatures + ", mAcceptTypes=" + this.mAcceptTypes + ", mAcceptWrappedTypes=" + this.mAcceptWrappedTypes + "]";
    }
}
