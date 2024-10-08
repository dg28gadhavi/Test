package com.sec.internal.ims.core.handler.secims;

/* compiled from: StackIF */
class DumpRequest {
    private final String mTag;
    private final String mTimeStamp;
    private final String mValue;

    public DumpRequest(String str, String str2, String str3) {
        this.mTag = str;
        this.mValue = str2;
        this.mTimeStamp = str3;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mTimeStamp);
        sb.append("  [");
        sb.append(this.mTag);
        sb.append("] ");
        sb.append(StackIF.checkLogEnable() ? this.mValue : "***");
        return sb.toString();
    }
}
