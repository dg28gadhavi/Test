package com.sec.internal.ims.servicemodules.volte2.data;

public class TextInfo {
    private final int mSessionId;
    private final String mText;
    private final int mTextLen;

    public TextInfo(int i, String str, int i2) {
        this.mSessionId = i;
        this.mText = str;
        this.mTextLen = i2;
    }

    public String getText() {
        return this.mText;
    }

    public int getTextLen() {
        return this.mTextLen;
    }

    public int getSessionId() {
        return this.mSessionId;
    }
}
