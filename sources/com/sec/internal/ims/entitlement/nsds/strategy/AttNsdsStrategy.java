package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy;

public class AttNsdsStrategy extends DefaultNsdsMnoStrategy {
    public AttNsdsStrategy(Context context) {
        super(context);
        this.mStrategyType = DefaultNsdsMnoStrategy.NsdsStrategyType.ATT;
        this.sMapEntitlementServices.put("vowifi", 1);
    }
}
