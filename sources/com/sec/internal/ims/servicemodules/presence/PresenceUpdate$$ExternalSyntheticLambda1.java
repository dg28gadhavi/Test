package com.sec.internal.ims.servicemodules.presence;

import com.sec.ims.presence.PresenceInfo;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class PresenceUpdate$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ PresenceUpdate f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ PresenceInfo f$2;
    public final /* synthetic */ PresenceSubscription f$3;

    public /* synthetic */ PresenceUpdate$$ExternalSyntheticLambda1(PresenceUpdate presenceUpdate, int i, PresenceInfo presenceInfo, PresenceSubscription presenceSubscription) {
        this.f$0 = presenceUpdate;
        this.f$1 = i;
        this.f$2 = presenceInfo;
        this.f$3 = presenceSubscription;
    }

    public final void run() {
        this.f$0.lambda$onNewPresenceInformation$0(this.f$1, this.f$2, this.f$3);
    }
}
