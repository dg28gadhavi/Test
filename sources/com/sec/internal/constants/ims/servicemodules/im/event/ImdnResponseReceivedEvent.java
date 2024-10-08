package com.sec.internal.constants.ims.servicemodules.im.event;

import com.android.internal.util.Preconditions;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import java.util.Collection;

public class ImdnResponseReceivedEvent {
    public final Collection<String> mMessageIds;
    public final Result mResult;

    public ImdnResponseReceivedEvent(Result result, Collection<String> collection) {
        this.mResult = (Result) Preconditions.checkNotNull(result, "%s", new Object[]{"ImdnResponseReceivedEvent: result is null."});
        this.mMessageIds = collection;
    }

    public String toString() {
        return "ImdnResponseReceivedEvent [mResult=" + this.mResult + ", mMessageIds=" + this.mMessageIds + "]";
    }
}
