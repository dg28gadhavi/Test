package com.sec.internal.ims.servicemodules.sms;

import android.os.Parcel;
import android.os.Parcelable;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.servicemodules.sms.SmsMessage;
import com.sec.internal.helper.os.Debug;

public class SmsEvent implements Parcelable {
    public static final Parcelable.Creator<SmsEvent> CREATOR = new Parcelable.Creator<SmsEvent>() {
        public SmsEvent createFromParcel(Parcel parcel) {
            return new SmsEvent(parcel);
        }

        public SmsEvent[] newArray(int i) {
            return new SmsEvent[i];
        }
    };
    public static final int SMSIP_CST_NOTI_INFO = 12;
    public static final int SMSIP_CST_RECEIVED = 11;
    private String mCallID;
    private String mContent;
    private String mContentType;
    private byte[] mData;
    private int mEventType;
    private boolean mIsEmergency;
    private String mLocalUri;
    private int mMessageID;
    private int mPhoneId;
    private String mReason;
    private int mReasonCode;
    private ImsRegistration mRegistration;
    private int mRetryAfter;
    private int mRpRef;
    private String mSmscAddr;
    private int mState;
    private int mTpDcs;
    private int mTpMr;
    private int mTpPid;

    public static class State {
        public static final int MO_RECEIVING_202_ACCEPTED = 102;
        public static final int MO_RECEIVING_CALLID = 101;
        public static final int MO_SENDING_START = 100;
        public static final int MT_RECEIVING_DELIVER_REPORT_ACK = 106;
        public static final int MT_RECEIVING_INCOMING_SMS = 103;
        public static final int MT_RECEIVING_STATUS_REPORT = 104;
        public static final int MT_SENDING_DELIVER_REPORT = 105;
        public static final int NONE = 0;
    }

    public int describeContents() {
        return 0;
    }

    public SmsEvent(ImsRegistration imsRegistration, int i, int i2, int i3, String str, byte[] bArr, String str2, String str3, String str4, int i4) {
        this.mLocalUri = null;
        this.mTpPid = 0;
        this.mTpDcs = 0;
        this.mTpMr = 0;
        this.mState = 0;
        this.mRpRef = -1;
        this.mPhoneId = 0;
        this.mContent = null;
        this.mRegistration = imsRegistration;
        this.mData = bArr;
        this.mEventType = i;
        this.mMessageID = i2;
        this.mReasonCode = i3;
        this.mContentType = str2;
        this.mCallID = str3;
        this.mSmscAddr = str4;
        this.mRetryAfter = i4;
        this.mReason = str;
        this.mIsEmergency = false;
    }

    public SmsEvent(Parcel parcel) {
        this.mRegistration = null;
        this.mData = null;
        this.mEventType = -1;
        this.mMessageID = -1;
        this.mReasonCode = -1;
        this.mContentType = null;
        this.mCallID = null;
        this.mSmscAddr = null;
        this.mLocalUri = null;
        this.mReason = null;
        this.mRetryAfter = -1;
        this.mTpPid = 0;
        this.mTpDcs = 0;
        this.mTpMr = 0;
        this.mState = 0;
        this.mRpRef = -1;
        this.mPhoneId = 0;
        this.mContent = null;
        this.mIsEmergency = false;
        int readInt = parcel.readInt();
        if (readInt > 0) {
            byte[] bArr = new byte[readInt];
            parcel.readByteArray(bArr);
            this.mData = bArr;
        } else {
            this.mData = null;
        }
        this.mEventType = parcel.readInt();
        this.mMessageID = parcel.readInt();
        this.mReasonCode = parcel.readInt();
        this.mReason = parcel.readString();
        this.mContentType = parcel.readString();
        this.mCallID = parcel.readString();
        this.mSmscAddr = parcel.readString();
        this.mRetryAfter = parcel.readInt();
        this.mIsEmergency = false;
    }

    public SmsEvent() {
        this.mRegistration = null;
        this.mData = null;
        this.mEventType = -1;
        this.mMessageID = -1;
        this.mReasonCode = -1;
        this.mContentType = null;
        this.mCallID = null;
        this.mSmscAddr = null;
        this.mLocalUri = null;
        this.mReason = null;
        this.mRetryAfter = -1;
        this.mTpPid = 0;
        this.mTpDcs = 0;
        this.mTpMr = 0;
        this.mState = 0;
        this.mRpRef = -1;
        this.mPhoneId = 0;
        this.mContent = null;
        this.mIsEmergency = false;
        setState(0);
        this.mRpRef = -1;
    }

    public ImsRegistration getImsRegistration() {
        return this.mRegistration;
    }

    public int getState() {
        return this.mState;
    }

    public int getRpRef() {
        return this.mRpRef;
    }

    public byte[] getData() {
        return this.mData;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public int getMessageID() {
        return this.mMessageID;
    }

    public int getReasonCode() {
        return this.mReasonCode;
    }

    public String getReason() {
        return this.mReason;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public String getCallID() {
        return this.mCallID;
    }

    public String getSmscAddr() {
        return this.mSmscAddr;
    }

    public String getLocalUri() {
        return this.mLocalUri;
    }

    public int getRetryAfter() {
        return this.mRetryAfter;
    }

    public int getTpPid() {
        return this.mTpPid;
    }

    public int getTpDcs() {
        return this.mTpDcs;
    }

    public int getTpMr() {
        return this.mTpMr;
    }

    public String getContent() {
        return this.mContent;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public boolean isEmergency() {
        return this.mIsEmergency;
    }

    public void setImsRegistration(ImsRegistration imsRegistration) {
        this.mRegistration = imsRegistration;
    }

    public void setState(int i) {
        this.mState = i;
    }

    public void setRpRef(int i) {
        this.mRpRef = i;
    }

    public void setData(byte[] bArr) {
        this.mData = bArr;
    }

    public void setEventType(int i) {
        this.mEventType = i;
    }

    public void setMessageID(int i) {
        this.mMessageID = i;
    }

    public void setReasonCode(int i) {
        this.mReasonCode = i;
    }

    public void setReason(String str) {
        this.mReason = str;
    }

    public void setContentType(String str) {
        this.mContentType = str;
    }

    public void setCallID(String str) {
        this.mCallID = str;
    }

    public void setSmscAddr(String str) {
        this.mSmscAddr = str;
    }

    public void setLocalUri(String str) {
        this.mLocalUri = str;
    }

    public void setRetryAfter(int i) {
        this.mRetryAfter = i;
    }

    public void setTpPid(int i) {
        this.mTpPid = i;
    }

    public void setTpDcs(int i) {
        this.mTpDcs = i;
    }

    public void setTpMr(int i) {
        this.mTpMr = i;
    }

    public void setContent(String str) {
        this.mContent = str;
    }

    public void setPhoneId(int i) {
        this.mPhoneId = i;
    }

    public void setEmergency(boolean z) {
        this.mIsEmergency = z;
    }

    public void writeToParcel(Parcel parcel, int i) {
        byte[] bArr = this.mData;
        if (bArr != null) {
            parcel.writeInt(bArr.length);
            parcel.writeByteArray(this.mData);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.mEventType);
        parcel.writeInt(this.mMessageID);
        parcel.writeInt(this.mReasonCode);
        parcel.writeString(this.mReason);
        parcel.writeString(this.mContentType);
        parcel.writeString(this.mCallID);
        parcel.writeString(this.mSmscAddr);
        parcel.writeInt(this.mRetryAfter);
    }

    public String toString() {
        String str;
        switch (this.mState) {
            case 100:
                str = "" + "[OUTGOING] state MO_SENDING_START ";
                break;
            case 101:
                str = "" + "[OUTGOING] state MO_RECEIVING_CALLID ";
                break;
            case 102:
                str = "" + "[OUTGOING] state MO_RECEIVING_202_ACCEPTED ";
                break;
            case 103:
                str = "" + "[INCOMING] state MT_RECEIVING_INCOMING_SMS ";
                break;
            case 104:
                str = "" + "[INCOMING] state MT_RECEIVING_STATUS_REPORT ";
                break;
            case 105:
                str = "" + "[INCOMING] state MT_SENDING_DELIVER_REPORT ";
                break;
            case 106:
                str = "" + "[INCOMING] state MT_RECEIVING_DELIVER_REPORT_ACK ";
                break;
            default:
                str = "" + "[NONE] ";
                break;
        }
        if (this.mContentType != null) {
            str = str + "contentType [" + this.mContentType + "] ";
        }
        if (this.mMessageID >= 0) {
            str = str + "messageID [" + this.mMessageID + "] ";
        }
        if (this.mRpRef >= 0) {
            str = str + "rpRef [" + this.mRpRef + "] ";
        }
        if (this.mReasonCode >= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("reasonCode [");
            int i = this.mReasonCode;
            if (i >= 32768) {
                i -= 32768;
            }
            sb.append(i);
            sb.append("] ");
            str = sb.toString();
        }
        if (this.mReason != null) {
            str = str + "reason [" + this.mReason + "] ";
        }
        if (this.mCallID != null) {
            str = str + "callID [" + this.mCallID + "] ";
        }
        if (this.mSmscAddr != null) {
            if (!Debug.isProductShip()) {
                str = str + "smscAddr [" + this.mSmscAddr + "] ";
            } else {
                int i2 = (this.mSmscAddr.startsWith("sip:") || this.mSmscAddr.startsWith("tel:")) ? 7 : 3;
                if (this.mSmscAddr.length() > i2) {
                    str = str + "smscAddr [" + this.mSmscAddr.substring(0, i2) + "] ";
                } else {
                    str = str + "smscAddr [" + this.mSmscAddr + "] ";
                }
            }
        }
        if (this.mRegistration == null) {
            return str;
        }
        return str + "regId [" + this.mRegistration.getHandle() + "] ";
    }

    public String toKeyDump() {
        int i = this.mState;
        String str = "";
        if (i >= 100 && i <= 106) {
            str = str + this.mState + ",";
        }
        if (this.mMessageID >= 0) {
            str = str + this.mMessageID + ",";
        }
        if (this.mTpMr >= 0) {
            str = str + this.mTpMr + ",";
        }
        if (this.mRpRef >= 0) {
            str = str + this.mRpRef + ",";
        }
        if (this.mReasonCode >= 0) {
            str = str + this.mReasonCode + ",";
        }
        if (this.mReason != null) {
            str = str + this.mReason + ",";
        }
        String str2 = this.mContentType;
        if (str2 == null) {
            return str;
        }
        if (str2.equals(GsmSmsUtil.CONTENT_TYPE_3GPP)) {
            return str + SmsMessage.FORMAT_3GPP;
        }
        return str + SmsMessage.FORMAT_3GPP2;
    }
}
