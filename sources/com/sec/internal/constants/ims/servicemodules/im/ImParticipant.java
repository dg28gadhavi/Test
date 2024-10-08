package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.Observable;

public class ImParticipant extends Observable {
    public static final String NO_ALIAS = "";
    private final String mChatId;
    private int mId;
    private Status mStatus;
    private Type mType;
    private final ImsUri mUri;
    private String mUserAlias;

    public ImParticipant(String str, ImsUri imsUri) {
        this.mType = Type.REGULAR;
        this.mStatus = Status.INITIAL;
        this.mUserAlias = "";
        this.mChatId = str;
        this.mUri = imsUri;
    }

    public ImParticipant(String str, Status status, ImsUri imsUri) {
        this.mType = Type.REGULAR;
        Status status2 = Status.INITIAL;
        this.mUserAlias = "";
        this.mChatId = str;
        this.mStatus = status;
        this.mUri = imsUri;
    }

    public ImParticipant(String str, Status status, Type type, ImsUri imsUri, String str2) {
        this.mType = Type.REGULAR;
        Status status2 = Status.INITIAL;
        this.mChatId = str;
        this.mStatus = status;
        this.mType = type;
        this.mUri = imsUri;
        this.mUserAlias = str2;
    }

    public ImParticipant(int i, String str, Status status, Type type, ImsUri imsUri, String str2) {
        this.mType = Type.REGULAR;
        Status status2 = Status.INITIAL;
        this.mId = i;
        this.mChatId = str;
        this.mStatus = status;
        this.mType = type;
        this.mUri = imsUri;
        this.mUserAlias = str2;
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int i) {
        this.mId = i;
    }

    public String getChatId() {
        return this.mChatId;
    }

    public Type getType() {
        return this.mType;
    }

    public void setType(Type type) {
        this.mType = type;
    }

    public Status getStatus() {
        return this.mStatus;
    }

    public void setStatus(Status status) {
        this.mStatus = status;
    }

    public ImsUri getUri() {
        return this.mUri;
    }

    public String getUserAlias() {
        if (this.mUserAlias == null) {
            this.mUserAlias = "";
        }
        return this.mUserAlias;
    }

    public void setUserAlias(String str) {
        this.mUserAlias = str;
    }

    public enum Type implements IEnumerationWithId<Type> {
        REGULAR(0),
        INITIATOR(1),
        CHAIRMAN(2);
        
        private static final ReverseEnumMap<Type> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(Type.class);
        }

        private Type(int i) {
            this.id = i;
        }

        public int getId() {
            return this.id;
        }

        public Type getFromId(int i) {
            return map.get(Integer.valueOf(i));
        }

        public static Type fromId(int i) {
            return map.get(Integer.valueOf(i));
        }
    }

    public enum Status implements IEnumerationWithId<Status> {
        INITIAL(0),
        INVITED(1),
        ACCEPTED(2),
        DECLINED(3),
        TIMEOUT(4),
        GONE(5),
        TO_INVITE(6),
        FAILED(7),
        PENDING(8);
        
        private static final ReverseEnumMap<Status> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(Status.class);
        }

        private Status(int i) {
            this.id = i;
        }

        public int getId() {
            return this.id;
        }

        public Status getFromId(int i) {
            return map.get(Integer.valueOf(i));
        }

        public static Status fromId(int i) {
            return map.get(Integer.valueOf(i));
        }
    }

    public String toString() {
        return "ImParticipant [mId=" + this.mId + ", mChatId=" + this.mChatId + ", mType=" + this.mType + ", mStatus=" + this.mStatus + ", mUri=" + IMSLog.numberChecker(this.mUri) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + "]";
    }

    public int hashCode() {
        String str = this.mChatId;
        int i = 0;
        int hashCode = ((str == null ? 0 : str.hashCode()) + 31) * 31;
        ImsUri imsUri = this.mUri;
        if (imsUri != null) {
            i = imsUri.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImParticipant imParticipant = (ImParticipant) obj;
        String str = this.mChatId;
        if (str == null) {
            if (imParticipant.mChatId != null) {
                return false;
            }
        } else if (!str.equals(imParticipant.mChatId)) {
            return false;
        }
        ImsUri imsUri = this.mUri;
        if (imsUri != null) {
            return imsUri.equals(imParticipant.mUri);
        }
        if (imParticipant.mUri == null) {
            return true;
        }
        return false;
    }
}
