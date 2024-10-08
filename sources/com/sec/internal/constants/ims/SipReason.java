package com.sec.internal.constants.ims;

public class SipReason {
    private int mCause;
    private String[] mExtension;
    private boolean mIsLocalRelease;
    private String mProtocol;
    private String mText;

    public SipReason getFromUserReason(int i) {
        return null;
    }

    public SipReason() {
        this.mIsLocalRelease = false;
    }

    public SipReason(String str, int i, String str2, String... strArr) {
        this.mProtocol = str;
        this.mCause = i;
        this.mText = str2;
        this.mExtension = strArr;
        this.mIsLocalRelease = false;
    }

    public SipReason(String str, int i, String str2, boolean z, String... strArr) {
        this.mProtocol = str;
        this.mCause = i;
        this.mText = str2;
        this.mExtension = strArr;
        this.mIsLocalRelease = z;
    }

    public String getProtocol() {
        return this.mProtocol;
    }

    public int getCause() {
        return this.mCause;
    }

    public String getText() {
        return this.mText;
    }

    public String[] getExtensions() {
        return this.mExtension;
    }

    public boolean isLocalRelease() {
        return this.mIsLocalRelease;
    }

    public void setLocalRelease(boolean z) {
        this.mIsLocalRelease = z;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Reason: ");
        stringBuffer.append(this.mProtocol);
        stringBuffer.append(";");
        stringBuffer.append("cause=");
        stringBuffer.append(this.mCause);
        stringBuffer.append(";");
        stringBuffer.append("text=");
        stringBuffer.append(this.mText);
        stringBuffer.append(";");
        String[] strArr = this.mExtension;
        if (strArr != null) {
            for (String append : strArr) {
                stringBuffer.append(append);
            }
        }
        return stringBuffer.toString();
    }
}
