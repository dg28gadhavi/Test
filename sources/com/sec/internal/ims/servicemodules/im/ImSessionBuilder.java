package com.sec.internal.ims.servicemodules.im;

import android.annotation.SuppressLint;
import android.os.Looper;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.im.interfaces.IGetter;
import com.sec.internal.ims.servicemodules.im.listener.ImSessionListener;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImSessionBuilder {
    public List<String> mAcceptTypes = new ArrayList();
    public List<String> mAcceptWrappedTypes = new ArrayList();
    public ChatData mChatData;
    public String mChatId;
    public ChatMode mChatMode;
    public ChatData.ChatType mChatType;
    public ImConfig mConfig;
    public String mContributionId;
    public String mConversationId;
    public ImsUri mCreatedBy;
    public ImDirection mDirection = ImDirection.IRRELEVANT;
    public IGetter mGetter;
    public String mIconPath;
    public IImServiceInterface mImsService;
    public ImsUri mInvitedBy;
    public ImSessionListener mListener;
    public Looper mLooper;
    public final Map<String, Integer> mNeedToRevokeMessages = new HashMap();
    public String mOwnGroupAlias;
    public String mOwnIMSI;
    public String mOwnNumber;
    public final Map<ImsUri, ImParticipant> mParticipants = new HashMap();
    @SuppressLint({"UseSparseArrays"})
    public final Set<ImsUri> mParticipantsUri = new HashSet();
    public Object mRawHandle;
    public String mRequestMessageId;
    public String mSdpContentType;
    public String mServiceId;
    public ImIncomingSessionEvent.ImSessionType mSessionType;
    public ImsUri mSessionUri;
    public ISlmServiceInterface mSlmService;
    public String mSubject;
    public int mThreadId = -1;
    public UriGenerator mUriGenerator;

    public ImSessionBuilder listener(ImSessionListener imSessionListener) {
        this.mListener = imSessionListener;
        return this;
    }

    public ImSessionBuilder looper(Looper looper) {
        this.mLooper = looper;
        return this;
    }

    public ImSessionBuilder imsService(IImServiceInterface iImServiceInterface) {
        this.mImsService = iImServiceInterface;
        return this;
    }

    public ImSessionBuilder slmService(ISlmServiceInterface iSlmServiceInterface) {
        this.mSlmService = iSlmServiceInterface;
        return this;
    }

    public ImSessionBuilder config(ImConfig imConfig) {
        this.mConfig = imConfig;
        return this;
    }

    public ImSessionBuilder uriGenerator(UriGenerator uriGenerator) {
        this.mUriGenerator = uriGenerator;
        return this;
    }

    public ImSessionBuilder chatId(String str) {
        this.mChatId = str;
        return this;
    }

    public ImSessionBuilder chatData(ChatData chatData) {
        this.mChatData = chatData;
        return this;
    }

    public ImSessionBuilder chatType(ChatData.ChatType chatType) {
        this.mChatType = chatType;
        return this;
    }

    public ImSessionBuilder chatMode(ChatMode chatMode) {
        this.mChatMode = chatMode;
        return this;
    }

    public ImSessionBuilder sessionType(ImIncomingSessionEvent.ImSessionType imSessionType) {
        this.mSessionType = imSessionType;
        return this;
    }

    public ImSessionBuilder participantsUri(Collection<ImsUri> collection) {
        this.mParticipantsUri.addAll(collection);
        return this;
    }

    public ImSessionBuilder participants(Map<ImsUri, ImParticipant> map) {
        this.mParticipants.putAll(map);
        return this;
    }

    public ImSessionBuilder contributionId(String str) {
        this.mContributionId = str;
        return this;
    }

    public ImSessionBuilder conversationId(String str) {
        this.mConversationId = str;
        return this;
    }

    public ImSessionBuilder rawHandle(Object obj) {
        this.mRawHandle = obj;
        return this;
    }

    public ImSessionBuilder subject(String str) {
        this.mSubject = str;
        return this;
    }

    public ImSessionBuilder iconPath(String str) {
        this.mIconPath = str;
        return this;
    }

    public ImSessionBuilder threadId(int i) {
        this.mThreadId = i;
        return this;
    }

    public ImSessionBuilder ownPhoneNum(String str) {
        this.mOwnNumber = str;
        return this;
    }

    public ImSessionBuilder ownSimIMSI(String str) {
        this.mOwnIMSI = str;
        return this;
    }

    public ImSessionBuilder ownGroupAlias(String str) {
        this.mOwnGroupAlias = str;
        return this;
    }

    public ImSessionBuilder sdpContentType(String str) {
        this.mSdpContentType = str;
        return this;
    }

    public ImSessionBuilder requestMessageId(String str) {
        this.mRequestMessageId = str;
        return this;
    }

    public ImSessionBuilder direction(ImDirection imDirection) {
        this.mDirection = imDirection;
        return this;
    }

    public ImSessionBuilder getter(IGetter iGetter) {
        this.mGetter = iGetter;
        return this;
    }

    public ImSessionBuilder serviceId(String str) {
        this.mServiceId = str;
        return this;
    }

    public ImSessionBuilder acceptTypes(List<String> list) {
        this.mAcceptTypes = list;
        return this;
    }

    public ImSessionBuilder acceptWrappedTypes(List<String> list) {
        this.mAcceptWrappedTypes = list;
        return this;
    }

    public ImSessionBuilder needToRevokeMessages(Map<String, Integer> map) {
        this.mNeedToRevokeMessages.putAll(map);
        return this;
    }

    public ImSessionBuilder sessionUri(ImsUri imsUri) {
        this.mSessionUri = imsUri;
        return this;
    }

    public ImSessionBuilder createdBy(ImsUri imsUri) {
        this.mCreatedBy = imsUri;
        return this;
    }

    public ImSessionBuilder invitedBy(ImsUri imsUri) {
        this.mInvitedBy = imsUri;
        return this;
    }

    public ImSession build() {
        Preconditions.checkNotNull(this.mLooper);
        Preconditions.checkNotNull(this.mListener);
        Preconditions.checkNotNull(this.mGetter);
        Preconditions.checkNotNull(this.mImsService);
        Preconditions.checkNotNull(this.mSlmService);
        Preconditions.checkNotNull(this.mConfig);
        if (this.mChatId == null && this.mChatData == null) {
            throw new IllegalArgumentException("mChatId is null");
        }
        if (this.mChatType == null && this.mChatData == null) {
            boolean z = true;
            if (this.mParticipantsUri.size() + this.mParticipants.size() <= 1 && this.mSessionType != ImIncomingSessionEvent.ImSessionType.CONFERENCE) {
                z = false;
            }
            this.mChatType = z ? ChatData.ChatType.REGULAR_GROUP_CHAT : ChatData.ChatType.ONE_TO_ONE_CHAT;
        }
        return new ImSession(this);
    }
}
