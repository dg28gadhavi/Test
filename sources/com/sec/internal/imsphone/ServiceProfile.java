package com.sec.internal.imsphone;

import com.android.ims.internal.IImsRegistrationListener;

public class ServiceProfile {
    private int mPhoneId;
    private IImsRegistrationListener mRegistrationListener;
    private int mServiceClass;

    public ServiceProfile(int i, int i2, IImsRegistrationListener iImsRegistrationListener) {
        this.mPhoneId = i;
        this.mServiceClass = i2;
        this.mRegistrationListener = iImsRegistrationListener;
    }

    public void setRegistrationListener(IImsRegistrationListener iImsRegistrationListener) {
        this.mRegistrationListener = iImsRegistrationListener;
    }

    public int getServiceClass() {
        return this.mServiceClass;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public IImsRegistrationListener getRegistrationListener() {
        return this.mRegistrationListener;
    }
}
