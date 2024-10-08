package com.sec.internal.imsphone;

import android.net.Uri;
import android.os.RemoteException;
import android.telephony.ims.ImsException;
import android.telephony.ims.SipDetails;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.RcsFeature;
import android.telephony.ims.stub.CapabilityExchangeEventListener;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class RcsFeatureImpl extends RcsFeature {
    private static final Map<Integer, String> CAP_TO_SERVICE;
    private static final String LOG_TAG = "RcsFeatureImpl";
    private static final Map<Integer, Integer> REG_TECH_TO_NET_TYPE;
    private CapabilityExchangeEventListener mCapabilityExchangeEventListener = null;
    private boolean mIsReady = false;
    int mPhoneId;

    public RcsFeatureImpl(int i, Executor executor) {
        super(executor);
        this.mPhoneId = i;
    }

    public boolean isReady() {
        return this.mIsReady;
    }

    public void onFeatureReady() {
        this.mIsReady = true;
        setFeatureState(2);
    }

    static {
        HashMap hashMap = new HashMap();
        REG_TECH_TO_NET_TYPE = hashMap;
        hashMap.put(3, 20);
        hashMap.put(0, 13);
        hashMap.put(1, 18);
        hashMap.put(2, 18);
        HashMap hashMap2 = new HashMap();
        CAP_TO_SERVICE = hashMap2;
        hashMap2.put(1, "options");
        hashMap2.put(2, SipMsg.EVENT_PRESENCE);
    }

    public boolean queryCapabilityConfiguration(int i, int i2) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "queryCapabilityConfiguration");
        try {
            int intValue = REG_TECH_TO_NET_TYPE.getOrDefault(Integer.valueOf(i2), -1).intValue();
            String orDefault = CAP_TO_SERVICE.getOrDefault(Integer.valueOf(i), NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
            if (NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(orDefault)) {
                return false;
            }
            return ImsRegistry.isServiceAvailable(orDefault, intValue, this.mPhoneId);
        } catch (RemoteException | NullPointerException e) {
            int i3 = this.mPhoneId;
            IMSLog.e(LOG_TAG, i3, "queryCapabilityConfiguration: failed: " + e.getMessage());
            return false;
        }
    }

    public void changeEnabledCapabilities(CapabilityChangeRequest capabilityChangeRequest, ImsFeature.CapabilityCallbackProxy capabilityCallbackProxy) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "changeEnabledCapabilities");
        if (capabilityChangeRequest != null) {
            RcsFeature.RcsImsCapabilities queryCapabilityStatus = queryCapabilityStatus();
            for (CapabilityChangeRequest.CapabilityPair capabilityPair : capabilityChangeRequest.getCapabilitiesToDisable()) {
                queryCapabilityStatus.removeCapabilities(capabilityPair.getCapability());
                int i = this.mPhoneId;
                IMSLog.s(LOG_TAG, i, "changeEnabledCapabilities: disabled capa = " + capabilityPair.getCapability());
            }
            for (CapabilityChangeRequest.CapabilityPair capabilityPair2 : capabilityChangeRequest.getCapabilitiesToEnable()) {
                queryCapabilityStatus.addCapabilities(capabilityPair2.getCapability());
                int i2 = this.mPhoneId;
                IMSLog.s(LOG_TAG, i2, "changeEnabledCapabilities: enabled capa = " + capabilityPair2.getCapability());
            }
            notifyCapabilitiesStatusChanged(queryCapabilityStatus);
        }
    }

    public RcsCapabilityExchangeImplBase createCapabilityExchangeImpl(CapabilityExchangeEventListener capabilityExchangeEventListener) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "createCapabilityExchangeImpl");
        this.mCapabilityExchangeEventListener = capabilityExchangeEventListener;
        return new RcsCapabilityExchangeImpl(this.mPhoneId);
    }

    public void destroyCapabilityExchangeImpl(RcsCapabilityExchangeImplBase rcsCapabilityExchangeImplBase) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "destroyCapabilityExchangeImpl");
    }

    public void onRequestPublishCapabilities(int i) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "onRequestPublishCapabilities: publishTriggerType = " + i);
        try {
            CapabilityExchangeEventListener capabilityExchangeEventListener = this.mCapabilityExchangeEventListener;
            if (capabilityExchangeEventListener != null) {
                capabilityExchangeEventListener.onRequestPublishCapabilities(i);
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "onRequestPublishCapabilities : mCapabilityExchangeEventListener is Null");
            }
        } catch (ImsException e) {
            e.printStackTrace();
        }
    }

    public void onUnPublish() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onUnpublish");
        try {
            CapabilityExchangeEventListener capabilityExchangeEventListener = this.mCapabilityExchangeEventListener;
            if (capabilityExchangeEventListener != null) {
                capabilityExchangeEventListener.onUnpublish();
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "onUnpublish : mCapabilityExchangeEventListener is Null");
            }
        } catch (ImsException e) {
            e.printStackTrace();
        }
    }

    public void onPublishUpdated(int i, String str, int i2, String str2) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onPublishUpdated");
        try {
            if (this.mCapabilityExchangeEventListener != null) {
                SipDetails.Builder builder = new SipDetails.Builder(2);
                if (str == null) {
                    str = "";
                }
                SipDetails.Builder sipResponseCode = builder.setSipResponseCode(i, str);
                if (str2 == null) {
                    str2 = "";
                }
                this.mCapabilityExchangeEventListener.onPublishUpdated(sipResponseCode.setSipResponseReasonHeader(i2, str2).build());
                return;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPublishUpdated : mCapabilityExchangeEventListener is Null");
        } catch (ImsException e) {
            e.printStackTrace();
        }
    }

    public void onRemoteCapabilityRequest(Uri uri, Set<String> set, CapabilityExchangeEventListener.OptionsRequestCallback optionsRequestCallback) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onRemoteCapabilityRequest");
        try {
            CapabilityExchangeEventListener capabilityExchangeEventListener = this.mCapabilityExchangeEventListener;
            if (capabilityExchangeEventListener != null) {
                capabilityExchangeEventListener.onRemoteCapabilityRequest(uri, set, optionsRequestCallback);
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "onRemoteCapabilityRequest : mCapabilityExchangeEventListener is Null");
            }
        } catch (ImsException e) {
            e.printStackTrace();
        }
    }
}
