package com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds;

import android.content.Context;
import com.sec.ims.util.ImsUri;

public abstract class FcmMessage {
    protected transient String origMessage;

    public abstract void broadcastFcmMessage(Context context);

    public boolean shouldBroadcast(Context context) {
        return true;
    }

    public void setOrigMessage(String str) {
        this.origMessage = str;
    }

    /* access modifiers changed from: protected */
    public String deriveMsisdnFromRecipientUri(String str) {
        ImsUri parse = ImsUri.parse(str);
        if (parse != null) {
            return parse.getMsisdn();
        }
        return null;
    }
}
