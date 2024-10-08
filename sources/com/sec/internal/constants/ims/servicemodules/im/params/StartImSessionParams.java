package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class StartImSessionParams {
    public List<String> mAcceptTypes;
    public List<String> mAcceptWrappedTypes;
    public Message mCallback;
    public String mChatId;
    public ChatMode mChatMode;
    public String mContributionId;
    public String mConversationId;
    public Message mDedicatedBearerCallback;
    public String mInReplyToContributionId;
    public boolean mIsChatbotParticipant;
    public boolean mIsClosedGroupChat;
    public boolean mIsConf;
    public boolean mIsGeolocationPush;
    public boolean mIsInviteForBye;
    public boolean mIsRejoin;
    public String mOwnImsi;
    public String mPrevContributionId;
    public List<ImsUri> mReceivers;
    public String mSdpContentType;
    public SendMessageParams mSendMessageParams;
    public String mServiceId;
    public ServiceType mServiceType;
    public String mSubject;
    public Message mSynchronousCallback;
    public String mUserAlias;

    public enum ServiceType {
        NORMAL
    }

    public StartImSessionParams(String str, String str2, List<ImsUri> list, String str3, String str4, String str5, ServiceType serviceType, boolean z, String str6, Message message, Message message2, Message message3, SendMessageParams sendMessageParams, String str7, String str8, boolean z2, boolean z3, boolean z4, String str9, List<String> list2, List<String> list3, String str10, boolean z5, ChatMode chatMode) {
        this.mChatId = str;
        this.mSubject = str2;
        List<ImsUri> list4 = list;
        this.mReceivers = new ArrayList(list);
        this.mContributionId = str3;
        this.mPrevContributionId = str4;
        this.mUserAlias = str5;
        this.mServiceType = serviceType;
        this.mIsConf = z;
        this.mSdpContentType = str6;
        this.mCallback = message;
        this.mSynchronousCallback = message3;
        this.mDedicatedBearerCallback = message2;
        this.mSendMessageParams = sendMessageParams;
        this.mConversationId = str7;
        this.mInReplyToContributionId = str8;
        this.mIsRejoin = z2;
        this.mIsClosedGroupChat = z3;
        this.mIsInviteForBye = z4;
        this.mServiceId = str9;
        this.mAcceptTypes = new ArrayList(list2);
        this.mAcceptWrappedTypes = new ArrayList(list3);
        this.mOwnImsi = str10;
        this.mIsChatbotParticipant = z5;
        this.mChatMode = chatMode;
    }

    public String toString() {
        return "StartImSessionParams [mChatId=" + this.mChatId + ", mSubject=" + IMSLog.checker(this.mSubject) + ", mReceivers=" + IMSLog.checker(this.mReceivers) + ", mContributionId=" + this.mContributionId + ", mPrevContributionId=" + this.mPrevContributionId + ", mConversationId=" + this.mConversationId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mServiceType=" + this.mServiceType + ", mIsConf=" + this.mIsConf + ", mSdpContentType=" + this.mSdpContentType + ", mCallback=" + this.mCallback + ", mSendMessageParams=" + this.mSendMessageParams + ", mIsRejoin=" + this.mIsRejoin + ", mIsClosedGroupChat=" + this.mIsClosedGroupChat + ", mIsInviteForBye=" + this.mIsInviteForBye + ", mServiceId=" + this.mServiceId + ", mAcceptTypes=" + this.mAcceptTypes + ", mAcceptWrappedTypes=" + this.mAcceptWrappedTypes + ", mOwnImsi=" + IMSLog.checker(this.mOwnImsi) + ", mIsChatbotParticipant=" + this.mIsChatbotParticipant + ", mChatMode=" + this.mChatMode + "]";
    }
}
