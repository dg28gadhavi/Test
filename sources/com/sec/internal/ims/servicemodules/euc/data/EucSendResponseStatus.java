package com.sec.internal.ims.servicemodules.euc.data;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.Preconditions;

public class EucSendResponseStatus {
    private final String mId;
    private final String mOwnIdentity;
    private final ImsUri mRemoteUri;
    private final Status mStatus;
    private final EucType mType;

    public enum Status {
        SUCCESS,
        FAILURE_INTERNAL,
        FAILURE_NETWORK
    }

    public EucSendResponseStatus(String str, EucType eucType, ImsUri imsUri, String str2, Status status) {
        boolean z = eucType == EucType.PERSISTENT || eucType == EucType.VOLATILE;
        Preconditions.checkArgument(z, "EucType " + eucType + " is not applicable for EucSendResponseStatus");
        this.mId = str;
        this.mType = eucType;
        this.mRemoteUri = imsUri;
        this.mOwnIdentity = str2;
        this.mStatus = status;
    }

    public String getId() {
        return this.mId;
    }

    public ImsUri getRemoteUri() {
        return this.mRemoteUri;
    }

    public String getOwnIdentity() {
        return this.mOwnIdentity;
    }

    public Status getStatus() {
        return this.mStatus;
    }

    public EucMessageKey getKey() {
        return new EucMessageKey(this.mId, this.mOwnIdentity, this.mType, this.mRemoteUri);
    }

    public String toString() {
        return getClass().getSimpleName() + " [mId=" + this.mId + ", mType=" + this.mType + ", mRemoteUri=" + this.mRemoteUri + ", mOwnIdentity=" + this.mOwnIdentity + ", mStatus=" + this.mStatus + "]";
    }
}
