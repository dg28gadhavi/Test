package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import java.util.List;

public class ImSessionConferenceInfoUpdateEvent {
    private boolean isRequestbyMcs;
    public final String mChatId;
    public final ImConferenceInfoType mConferenceInfoType;
    public final ImIconData mIconData;
    public final int mMaxUserCount;
    public final String mOwnImsi;
    public final List<ImConferenceParticipantInfo> mParticipantsInfo;
    public final Object mRawHandle;
    public final ImSubjectData mSubjectData;
    public final String mTimeStamp;

    public enum ImConferenceInfoType {
        FULL,
        PARTIAL,
        DELETED
    }

    public ImSessionConferenceInfoUpdateEvent(String str, ImConferenceInfoType imConferenceInfoType, List<ImConferenceParticipantInfo> list, int i, ImSubjectData imSubjectData, Object obj, String str2, ImIconData imIconData, String str3) {
        this.mChatId = str;
        this.mConferenceInfoType = imConferenceInfoType;
        this.mParticipantsInfo = list;
        this.mMaxUserCount = i;
        this.mSubjectData = imSubjectData;
        this.mIconData = imIconData;
        this.mRawHandle = obj;
        this.mOwnImsi = str2;
        this.mTimeStamp = str3;
    }

    public boolean getRequestByMcs() {
        return this.isRequestbyMcs;
    }

    public void setRequestByMcs(boolean z) {
        this.isRequestbyMcs = z;
    }

    public String toString() {
        return "ImSessionConferenceInfoUpdateEvent [mChatId=" + this.mChatId + ", mConferenceInfoType=" + this.mConferenceInfoType + ", mParticipantsInfo=" + this.mParticipantsInfo + ", mMaxUserCount=" + this.mMaxUserCount + ", mSubjectData=" + this.mSubjectData + ", mIconData=" + this.mIconData + ", mRawHandle=" + this.mRawHandle + "]";
    }
}
