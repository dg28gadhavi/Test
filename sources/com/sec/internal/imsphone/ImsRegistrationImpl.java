package com.sec.internal.imsphone;

import android.telephony.ims.stub.ImsRegistrationImplBase;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.concurrent.Executor;

public class ImsRegistrationImpl extends ImsRegistrationImplBase {
    private static final String LOG_TAG = "ImsRegistrationImpl";
    private int mPhoneId;
    private final IRegistrationManager mRegistrationManager;
    private int registrationState = 0;

    public ImsRegistrationImpl(int i, Executor executor) {
        super(executor);
        this.mPhoneId = i;
        this.mRegistrationManager = ImsRegistry.getRegistrationManager();
    }

    public void setRegistered() {
        this.registrationState = 2;
    }

    public void setNotRegistered() {
        this.registrationState = 0;
    }

    public boolean isRegistered() {
        return this.registrationState == 2;
    }

    public void triggerFullNetworkRegistration(int i, String str) {
        this.mRegistrationManager.requestFullNetworkRegistration(this.mPhoneId, i, str);
    }

    public void triggerDeregistration(int i) {
        this.mRegistrationManager.sendDeregister(i);
    }

    public void triggerSipDelegateDeregistration() {
        IMSLog.i(LOG_TAG, "triggerSipDelegateDeregistration: Postpone the request");
    }

    public void updateSipDelegateRegistration() {
        this.mRegistrationManager.requestUpdateSipDelegateRegistration(this.mPhoneId);
    }
}
