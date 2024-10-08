package com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeviceConfig {
    @SerializedName("vvmConfig")
    public VVMConfig mVVMConfig;

    public static class VVMConfig {
        @SerializedName("RootURL")
        public String mRootUrl;
        @SerializedName("VVMFolderID")
        public VVMFolderID mVVMFolderID;
        @SerializedName("WSGURI")
        public String mWsgUri;

        public static class FolderName {
            @SerializedName("@name")
            public String mName;
            @SerializedName("$")
            public String mValue;
        }

        public static class VVMFolderID {
            @SerializedName("FolderName")
            public List<FolderName> mFolderName;
        }
    }
}
