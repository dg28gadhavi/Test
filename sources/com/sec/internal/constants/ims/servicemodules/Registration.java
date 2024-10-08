package com.sec.internal.constants.ims.servicemodules;

import com.sec.ims.ImsRegistration;

public class Registration {
    private ImsRegistration mImsRegistration;
    private Boolean mIsReRegi;

    public Registration(ImsRegistration imsRegistration, boolean z) {
        this.mImsRegistration = imsRegistration;
        this.mIsReRegi = Boolean.valueOf(z);
    }

    public ImsRegistration getImsRegi() {
        return this.mImsRegistration;
    }

    public boolean isReRegi() {
        return this.mIsReRegi.booleanValue();
    }
}
