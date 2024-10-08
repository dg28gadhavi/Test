package com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy;

import android.content.Context;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.DefaultNsdsConfigStrategy;

public class TmoNsdsConfigStrategy extends DefaultNsdsConfigStrategy {
    public TmoNsdsConfigStrategy(Context context) {
        super(context);
        this.mStrategyType = DefaultNsdsConfigStrategy.NsdsConfigStrategyType.TMOUS;
    }
}
