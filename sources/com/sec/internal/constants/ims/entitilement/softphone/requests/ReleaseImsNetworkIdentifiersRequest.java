package com.sec.internal.constants.ims.entitilement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class ReleaseImsNetworkIdentifiersRequest {
    @SerializedName("subscriberIdentitiessubscriberCredentials")
    public SubscriberCredentials mSubscriberCredentials;

    public static class SubscriberCredentials {
        @SerializedName("privateUserId")
        public String mPrivateUserId;
        @SerializedName("publicUserId")
        public String mPublicUserId;

        public SubscriberCredentials(String str, String str2) {
            this.mPrivateUserId = str;
            this.mPublicUserId = str2;
        }

        public String toString() {
            return "SubscriberCredentials [mPrivateUserId = " + this.mPrivateUserId + ", mPublicUserId = " + this.mPublicUserId + "]";
        }
    }

    public ReleaseImsNetworkIdentifiersRequest(String str, String str2) {
        this.mSubscriberCredentials = new SubscriberCredentials(str, str2);
    }

    public String toString() {
        return "ReleaseImsNetworkIdentifiersRequest [mSubscriberCredentials = " + this.mSubscriberCredentials + "]";
    }
}
