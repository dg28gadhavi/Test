package com.sec.internal.imsphone;

import android.net.Uri;
import android.telephony.ims.ImsException;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import android.text.TextUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.Set;

public class RcsCapabilityExchangeImpl extends RcsCapabilityExchangeImplBase {
    private static final String LOG_TAG = RcsCapabilityExchangeImpl.class.getSimpleName();
    private ICapabilityDiscoveryModule mCapabilityDisModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
    private int mPhoneId;

    public RcsCapabilityExchangeImpl(int i) {
        this.mPhoneId = i;
        IMSLog.i(LOG_TAG, this.mPhoneId, "create RcsCapabilityExchangeImpl");
    }

    public void publishCapabilities(String str, RcsCapabilityExchangeImplBase.PublishResponseCallback publishResponseCallback) {
        if (publishResponseCallback == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "publishCapabilities: callback is null");
            return;
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, this.mPhoneId, "publishCapabilities");
        if (TextUtils.isEmpty(str)) {
            try {
                publishResponseCallback.onCommandError(2);
            } catch (ImsException e) {
                String str3 = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.e(str3, i, "publishCapabilities: failed: " + e.getMessage());
            }
        } else {
            if (this.mCapabilityDisModule == null) {
                IMSLog.i(str2, this.mPhoneId, "publishCapabilities : mCapabilityDisModule is null");
                this.mCapabilityDisModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
            }
            ICapabilityDiscoveryModule iCapabilityDiscoveryModule = this.mCapabilityDisModule;
            if (iCapabilityDiscoveryModule != null) {
                iCapabilityDiscoveryModule.publishCapabilities(str, publishResponseCallback, this.mPhoneId);
                return;
            }
            try {
                publishResponseCallback.onCommandError(9);
            } catch (ImsException e2) {
                String str4 = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.e(str4, i2, "publishCapabilities: failed: " + e2.getMessage());
            }
        }
    }

    public void subscribeForCapabilities(Collection<Uri> collection, RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback) {
        if (subscribeResponseCallback == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "subscribeForCapabilities: callback is null");
            return;
        }
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "subscribeForCapabilities");
        if (collection == null) {
            try {
                subscribeResponseCallback.onCommandError(2);
            } catch (ImsException e) {
                String str2 = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.e(str2, i, "subscribeForCapabilities: failed: " + e.getMessage());
            }
        } else {
            if (this.mCapabilityDisModule == null) {
                IMSLog.i(str, this.mPhoneId, "subscribeForCapabilities : mCapabilityDisModule is null");
                this.mCapabilityDisModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
            }
            ICapabilityDiscoveryModule iCapabilityDiscoveryModule = this.mCapabilityDisModule;
            if (iCapabilityDiscoveryModule != null) {
                iCapabilityDiscoveryModule.subscribeForCapabilities(collection, subscribeResponseCallback, this.mPhoneId);
                return;
            }
            try {
                subscribeResponseCallback.onCommandError(9);
            } catch (ImsException e2) {
                String str3 = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.e(str3, i2, "subscribeForCapabilities: failed: " + e2.getMessage());
            }
        }
    }

    public void sendOptionsCapabilityRequest(Uri uri, Set<String> set, RcsCapabilityExchangeImplBase.OptionsResponseCallback optionsResponseCallback) {
        if (optionsResponseCallback == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "sendOptionsCapabilityRequest: callback is null");
            return;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "sendOptionsCapabilityRequest");
        ICapabilityDiscoveryModule iCapabilityDiscoveryModule = this.mCapabilityDisModule;
        if (iCapabilityDiscoveryModule == null || !iCapabilityDiscoveryModule.isRunning()) {
            try {
                optionsResponseCallback.onCommandError(9);
            } catch (ImsException e) {
                String str = LOG_TAG;
                IMSLog.e(str, "onCommandError: failed: " + e.getMessage());
            }
        } else {
            this.mCapabilityDisModule.sendOptionsCapabilityRequest(uri, set, optionsResponseCallback, this.mPhoneId);
        }
    }
}
