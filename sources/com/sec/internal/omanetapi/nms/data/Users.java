package com.sec.internal.omanetapi.nms.data;

import com.google.gson.annotations.SerializedName;

public class Users {
    @SerializedName("state")
    public String mState;
    @SerializedName("user")
    public User[] mUser;

    public static class User {
        @SerializedName("display-text")
        public String mDisplayText;
        @SerializedName("endpoint")
        public Endpoint[] mEndpoint;
        @SerializedName("entity")
        public String mEntity;
        @SerializedName("yourown")
        public boolean mOwn;
        @SerializedName("roles")
        public String[] mRole;
        @SerializedName("state")
        public String mState;

        public static class Endpoint {
            @SerializedName("disconnection-info")
            public DisconnectionInfo mDisconnectionInfo;
            @SerializedName("disconnection-method")
            public String mDisconnectionMethod;
            @SerializedName("entity")
            public String mEntity;
            @SerializedName("joining-info")
            public JoiningInfo mJoingingInfo;
            @SerializedName("state")
            public String mState;
            @SerializedName("status")
            public String mStatus;

            public static class DisconnectionInfo {
                @SerializedName("by")
                public String mBy;
                @SerializedName("reason")
                public String mReason;
                @SerializedName("when")
                public String mWhen;
            }

            public static class JoiningInfo {
                @SerializedName("by")
                public String mBy;
                @SerializedName("reason")
                public String mReason;
                @SerializedName("when")
                public String mWhen;
            }
        }
    }
}
