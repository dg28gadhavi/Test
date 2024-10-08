package com.sec.internal.omanetapi.nms.data;

import com.google.gson.annotations.SerializedName;

public class ConferenceDescription {
    @SerializedName("icon")
    public Icon mIcon;
    @SerializedName("maximum-user-count")
    public int mMaxCount;
    @SerializedName("policy")
    public String[] mPolicy;
    @SerializedName("subject")
    public String mSubject;
    @SerializedName("subject-ext")
    public SubjectExt mSubjectExt;

    public static class Icon {
        @SerializedName("file-info")
        public FileInfo mFileInfo;
        @SerializedName("icon-uri")
        public String mIconUri;
        @SerializedName("participant")
        public String mParticipant;
        @SerializedName("timestamp")
        public String mTimestamp;

        public static class FileInfo {
            @SerializedName("content-type")
            public String mContentType;
            @SerializedName("data")
            public String mData;
        }
    }

    public static class SubjectExt {
        @SerializedName("participant")
        public String mParticipant;
        @SerializedName("timestamp")
        public String mTimestamp;
    }
}
