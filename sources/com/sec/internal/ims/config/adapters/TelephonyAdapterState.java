package com.sec.internal.ims.config.adapters;

import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;

public class TelephonyAdapterState implements ITelephonyAdapter {
    protected static String ABSENT_STATE = "AbsentState";
    protected static String IDLE_STATE = "IdleState";
    protected static String READY_STATE = "ReadyState";
    protected static String SMS_DEST_PORT = "37273";
    protected static String SMS_ORIG_PORT = "0";

    public void cleanup() {
    }

    public String getAppToken(boolean z) {
        return null;
    }

    public String getDeviceId(int i) {
        return null;
    }

    public String getExistingOtp() {
        return null;
    }

    public String getExistingPortOtp() {
        return null;
    }

    public String getIdentityByPhoneId(int i) {
        return null;
    }

    public String getIidToken() {
        return null;
    }

    public String getImei() {
        return null;
    }

    public String getImsi() {
        return null;
    }

    public String getMcc() {
        return null;
    }

    public String getMnc() {
        return null;
    }

    public String getMsisdn() {
        return null;
    }

    public String getMsisdn(int i) {
        return null;
    }

    public String getMsisdnNumber() {
        return null;
    }

    public String getNetType() {
        return null;
    }

    public String getOtp() {
        return null;
    }

    public String getPortOtp() {
        return null;
    }

    public String getPrimaryIdentity() {
        return null;
    }

    public String getSimCountryCode() {
        return null;
    }

    public String getSipUri() {
        return null;
    }

    public String getSubscriberId(int i) {
        return null;
    }

    public boolean isReady() {
        return false;
    }

    public void notifyAutoConfigurationListener(int i, boolean z) {
    }

    public void onADSChanged() {
    }

    public void registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
    }

    public void registerUneregisterForOTP(boolean z) {
    }

    public void sendIidToken(String str) {
    }

    public void sendMsisdnNumber(String str) {
    }

    public void sendVerificationCode(String str) {
    }

    public void unregisterAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
    }

    public String getSmsDestPort() {
        return SMS_DEST_PORT;
    }

    public String getSmsOrigPort() {
        return SMS_ORIG_PORT;
    }
}
