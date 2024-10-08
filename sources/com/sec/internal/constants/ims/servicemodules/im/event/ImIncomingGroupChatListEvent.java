package com.sec.internal.constants.ims.servicemodules.im.event;

import android.net.Uri;
import java.util.List;

public class ImIncomingGroupChatListEvent {
    public List<Entry> entryList;
    public String mOwnImsi;
    public int version;

    public ImIncomingGroupChatListEvent(int i, List<Entry> list, String str) {
        this.version = i;
        this.entryList = list;
        this.mOwnImsi = str;
    }

    public static class Entry {
        public String pConvID;
        public Uri sessionUri;
        public String subject;

        public Entry(Uri uri, String str, String str2) {
            this.sessionUri = uri;
            this.pConvID = str;
            this.subject = str2;
        }
    }
}
