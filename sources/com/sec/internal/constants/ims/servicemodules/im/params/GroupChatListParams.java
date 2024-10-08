package com.sec.internal.constants.ims.servicemodules.im.params;

public class GroupChatListParams {
    private final boolean increaseMode;
    private final String mOwnImsi;
    private final int version;

    public GroupChatListParams(int i, boolean z, String str) {
        this.version = i;
        this.increaseMode = z;
        this.mOwnImsi = str;
    }

    public int getVersion() {
        return this.version;
    }

    public boolean getIncreaseMode() {
        return this.increaseMode;
    }

    public String getOwnImsi() {
        return this.mOwnImsi;
    }
}
