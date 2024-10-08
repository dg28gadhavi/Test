package com.sec.internal.ims.servicemodules.session;

import android.content.Context;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.RcsBigDataProcessor;

public class EcBigDataProcessor extends RcsBigDataProcessor {
    private SessionModule mSessionModule;

    /* access modifiers changed from: protected */
    public String getMessageType(MessageBase messageBase, boolean z) {
        return DiagnosisConstants.RCSM_MTYP_EC;
    }

    /* access modifiers changed from: protected */
    public String getMessageTypeForILA(String str) {
        return DiagnosisConstants.DRCS_KEY_RCS_EC_MO_SUCCESS;
    }

    /* access modifiers changed from: protected */
    public boolean isChatBot(int i, ImSession imSession) {
        return false;
    }

    EcBigDataProcessor(Context context, SessionModule sessionModule) {
        super(context);
        this.mSessionModule = sessionModule;
    }

    /* access modifiers changed from: protected */
    public ImSession getImSession(String str) {
        return this.mSessionModule.getMessagingSession(str);
    }

    /* access modifiers changed from: protected */
    public ImsRegistration getImsRegistration(int i) {
        return this.mSessionModule.getImsRegistration(i);
    }

    /* access modifiers changed from: protected */
    public boolean isWifiConnected() {
        return this.mSessionModule.isWifiConnected();
    }
}
