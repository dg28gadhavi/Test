package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Observable;

public class ChatData extends Observable {
    private final String mChatId;
    private ChatMode mChatMode;
    private ChatType mChatType;
    private String mContributionId;
    private String mConversationId;
    private ImsUri mCreatedBy;
    private ImDirection mDirection;
    private ImIconData mIconData;
    private final String mIconPath;
    private int mId;
    private long mInsertedTimeStamp;
    private ImsUri mInvitedBy;
    private boolean mIsChatbotRole;
    private boolean mIsIconUpdateRequiredOnSessionEstablished;
    private boolean mIsMuted;
    private boolean mIsReusable;
    private final int mMaxParticipantCount;
    private String mOwnGroupAlias;
    private String mOwnIMSI;
    private String mOwnNumber;
    private ImsUri mSessionUri;
    private State mState;
    private String mSubject;
    private ImSubjectData mSubjectData;

    public ChatData(String str, String str2, String str3, String str4, ChatType chatType, ImDirection imDirection, String str5, String str6, String str7, String str8, ChatMode chatMode, ImsUri imsUri, ImsUri imsUri2, ImsUri imsUri3) {
        this.mChatType = ChatType.ONE_TO_ONE_CHAT;
        this.mChatMode = ChatMode.OFF;
        this.mState = State.NONE;
        this.mIsReusable = true;
        this.mChatId = str;
        this.mChatType = chatType;
        this.mOwnNumber = str2;
        this.mOwnGroupAlias = str3;
        this.mSubject = str4;
        this.mIconPath = str8;
        this.mMaxParticipantCount = 100;
        this.mDirection = imDirection;
        this.mConversationId = str5;
        this.mContributionId = str6;
        this.mInsertedTimeStamp = System.currentTimeMillis();
        this.mOwnIMSI = str7;
        this.mChatMode = chatMode;
        this.mSessionUri = imsUri;
        if (str4 != null) {
            this.mSubjectData = new ImSubjectData(str4, (ImsUri) null, (Date) null);
        }
        int phoneId = SimManagerFactory.getPhoneId(this.mOwnIMSI);
        if (ImsUtil.isMcsSupported(phoneId == -1 ? SimUtil.getSimSlotPriority() : phoneId)) {
            this.mInsertedTimeStamp = RcsUtils.getEpochNanosec();
        }
        this.mCreatedBy = imsUri2;
        this.mInvitedBy = imsUri3;
    }

    public ChatData(int i, String str, String str2, String str3, ChatType chatType, State state, String str4, boolean z, int i2, ImDirection imDirection, String str5, String str6, ImsUri imsUri, boolean z2, long j, String str7, ImsUri imsUri2, Date date, String str8, ImsUri imsUri3, Date date2, String str9, boolean z3, ChatMode chatMode, ImsUri imsUri4, ImsUri imsUri5) {
        String str10 = str4;
        ImsUri imsUri6 = imsUri2;
        Date date3 = date;
        String str11 = str8;
        this.mChatType = ChatType.ONE_TO_ONE_CHAT;
        this.mChatMode = ChatMode.OFF;
        State state2 = State.NONE;
        this.mId = i;
        this.mChatId = str;
        this.mOwnNumber = str2;
        this.mOwnGroupAlias = str3;
        this.mChatType = chatType;
        this.mState = state;
        this.mSubject = str10;
        this.mIsMuted = z;
        this.mMaxParticipantCount = i2;
        this.mDirection = imDirection;
        this.mConversationId = str5;
        this.mContributionId = str6;
        this.mSessionUri = imsUri;
        this.mIsReusable = z2;
        this.mInsertedTimeStamp = j;
        this.mOwnIMSI = str7;
        this.mIconPath = str11;
        this.mIsChatbotRole = z3;
        this.mChatMode = chatMode;
        if (!(str10 == null && imsUri6 == null && date3 == null)) {
            this.mSubjectData = new ImSubjectData(str4, imsUri6, date3);
        }
        if (!(str11 == null && imsUri3 == null && date2 == null)) {
            this.mIconData = new ImIconData(str9 == null ? ImIconData.IconType.ICON_TYPE_FILE : ImIconData.IconType.ICON_TYPE_URI, imsUri3, date2, str8, str9);
        }
        this.mCreatedBy = imsUri4;
        this.mInvitedBy = imsUri5;
    }

    public enum ChatType implements IEnumerationWithId<ChatType> {
        ONE_TO_ONE_CHAT(0),
        REGULAR_GROUP_CHAT(1),
        PARTICIPANT_BASED_GROUP_CHAT(3),
        ONE_TO_MANY_CHAT(4);
        
        private static final ReverseEnumMap<ChatType> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(ChatType.class);
        }

        private ChatType(int i) {
            this.id = i;
        }

        public int getId() {
            return this.id;
        }

        public ChatType getFromId(int i) {
            return fromId(i);
        }

        public static ChatType fromId(int i) {
            ChatType chatType = ONE_TO_ONE_CHAT;
            try {
                return map.get(Integer.valueOf(i));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return chatType;
            }
        }

        public static boolean isGroupChat(ChatType chatType) {
            return chatType != ONE_TO_ONE_CHAT;
        }

        public static boolean isGroupChatIdBasedGroupChat(ChatType chatType) {
            return chatType == REGULAR_GROUP_CHAT;
        }

        public static boolean isClosedGroupChat(ChatType chatType) {
            return chatType == PARTICIPANT_BASED_GROUP_CHAT;
        }
    }

    public ChatType getChatType() {
        return this.mChatType;
    }

    public void updateChatType(ChatType chatType) {
        if (this.mChatType != chatType) {
            this.mChatType = chatType;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public ChatMode getChatMode() {
        return this.mChatMode;
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int i) {
        this.mId = i;
    }

    public ImDirection getDirection() {
        return this.mDirection;
    }

    public String getChatId() {
        return this.mChatId;
    }

    public String getOwnPhoneNum() {
        return this.mOwnNumber;
    }

    public String getOwnIMSI() {
        return this.mOwnIMSI;
    }

    public String getConversationId() {
        return this.mConversationId;
    }

    public String getContributionId() {
        return this.mContributionId;
    }

    public ImsUri getSessionUri() {
        return this.mSessionUri;
    }

    public boolean isGroupChat() {
        return ChatType.isGroupChat(this.mChatType);
    }

    public void updateIsMuted(boolean z) {
        if (this.mIsMuted != z) {
            this.mIsMuted = z;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public long getInsertedTimeStamp() {
        return this.mInsertedTimeStamp;
    }

    public void setInsertedTimeStamp(long j) {
        this.mInsertedTimeStamp = j;
    }

    public String getOwnGroupAlias() {
        return this.mOwnGroupAlias;
    }

    public void setSubject(String str) {
        this.mSubject = str;
    }

    public void setContributionId(String str) {
        this.mContributionId = str;
    }

    public void setConversationId(String str) {
        this.mConversationId = str;
    }

    public void setSessionUri(ImsUri imsUri) {
        this.mSessionUri = imsUri;
    }

    public void setOwnPhoneNum(String str) {
        this.mOwnNumber = str;
    }

    public void setOwnIMSI(String str) {
        if (str != null && !str.equals(this.mOwnIMSI)) {
            this.mOwnIMSI = str;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void setDirection(ImDirection imDirection) {
        if (imDirection != null && imDirection != this.mDirection) {
            this.mDirection = imDirection;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public State getState() {
        return this.mState;
    }

    public void setState(int i) {
        this.mState = State.fromId(i);
    }

    public String getSubject() {
        return this.mSubject;
    }

    public ImSubjectData getSubjectData() {
        return this.mSubjectData;
    }

    public String getIconPath() {
        return this.mIconPath;
    }

    public ImIconData getIconData() {
        return this.mIconData;
    }

    public void setIconUpdatedRequiredOnSessionEstablished(boolean z) {
        this.mIsIconUpdateRequiredOnSessionEstablished = z;
    }

    public boolean isIconUpdatedRequiredOnSessionEstablished() {
        return this.mIsIconUpdateRequiredOnSessionEstablished;
    }

    public boolean isMuted() {
        return this.mIsMuted;
    }

    public boolean isReusable() {
        return this.mIsReusable;
    }

    public boolean isChatbotRole() {
        return this.mIsChatbotRole;
    }

    public ImsUri getCreatedBy() {
        return this.mCreatedBy;
    }

    public ImsUri getInvitedBy() {
        return this.mInvitedBy;
    }

    public enum State implements IEnumerationWithId<State> {
        NONE(-1),
        ACTIVE(1),
        INACTIVE(0),
        CLOSED_BY_USER(2),
        CLOSED_INVOLUNTARILY(3),
        CLOSED_VOLUNTARILY(4);
        
        private static final ReverseEnumMap<State> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(State.class);
        }

        private State(int i) {
            this.id = i;
        }

        public int getId() {
            return this.id;
        }

        public State getFromId(int i) {
            return fromId(i);
        }

        public static State fromId(int i) {
            State state = CLOSED_BY_USER;
            try {
                return map.get(Integer.valueOf(i));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return state;
            }
        }
    }

    public void updateState(State state) {
        if (state != this.mState) {
            this.mState = state;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateSubject(String str) {
        if (str != null && !str.equals(this.mSubject)) {
            this.mSubject = str;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateSubjectData(ImSubjectData imSubjectData) {
        if (imSubjectData != null) {
            this.mSubjectData = imSubjectData;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateIconData(ImIconData imIconData) {
        this.mIconData = imIconData;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public void updateOwnGroupAlias(String str) {
        if (str != null && !str.equals(this.mOwnGroupAlias)) {
            this.mOwnGroupAlias = str;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateIsReusable(boolean z) {
        if (z != this.mIsReusable) {
            this.mIsReusable = z;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateIsChatbotRole(boolean z) {
        if (z != this.mIsChatbotRole) {
            this.mIsChatbotRole = z;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateCreatedBy(ImsUri imsUri) {
        if (imsUri != this.mCreatedBy) {
            this.mCreatedBy = imsUri;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateInvitedBy(ImsUri imsUri) {
        if (imsUri != this.mInvitedBy) {
            this.mInvitedBy = imsUri;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void triggerObservers(ImCacheAction imCacheAction) {
        setChanged();
        notifyObservers(imCacheAction);
    }

    public int getMaxParticipantsCount() {
        return this.mMaxParticipantCount;
    }

    public int hashCode() {
        String str = this.mChatId;
        return (((str == null ? 0 : str.hashCode()) + 31) * 31) + this.mId;
    }

    public String toString() {
        return "ChatData [mId=" + this.mId + ", mChatId=" + this.mChatId + ", mOwnNumber=" + IMSLog.numberChecker(this.mOwnNumber) + ", mChatType=" + this.mChatType + ", mState=" + this.mState + ", mSubject=" + IMSLog.checker(this.mSubject) + ", mIsMuted=" + this.mIsMuted + ", mMaxParticipantCount=" + this.mMaxParticipantCount + ", mConversationId=" + this.mConversationId + ", mContributionId=" + this.mContributionId + ", mDirection=" + this.mDirection + ", mIsReusable=" + this.mIsReusable + ", mInsertedTimeStamp=" + this.mInsertedTimeStamp + ", mOwnIMSI=" + IMSLog.numberChecker(this.mOwnIMSI) + ", mIsChatbotRole=" + this.mIsChatbotRole + ", mChatMode=" + this.mChatMode + ", mCreatedBy=" + IMSLog.numberChecker(this.mCreatedBy) + ", mInvitedBy=" + IMSLog.numberChecker(this.mInvitedBy) + "]";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChatData chatData = (ChatData) obj;
        String str = this.mChatId;
        if (str == null) {
            if (chatData.mChatId != null) {
                return false;
            }
        } else if (!str.equals(chatData.mChatId)) {
            return false;
        }
        if (this.mId == chatData.mId) {
            return true;
        }
        return false;
    }
}
