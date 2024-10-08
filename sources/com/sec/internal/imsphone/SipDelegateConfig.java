package com.sec.internal.imsphone;

import android.telephony.ims.SipDelegateConfiguration;
import android.text.TextUtils;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SipDelegateConfig {
    static final String LOG_TAG = "SipDelegateConfig";
    String mAssociatedUriHeader;
    /* access modifiers changed from: private */
    public Builder mBuilder;
    String mContactUserParam;
    String mHomeDomain;
    String mImei;
    InetSocketAddress mLocalAddress;
    int mMaxUdpPayloadSize;
    String mPaniHeader;
    String mPlaniHeader;
    String mPrivateUserIdentifier;
    String mPublicUserIdentifier;
    String mSecurityVerifyHeader;
    String mServiceRouteHeader;
    String mSipAuthHeader;
    String mSipAuthNonce;
    InetSocketAddress mSipServerAddress;
    int mTransportType;
    String mUserAgentHeader;
    int mVersion;

    SipDelegateConfig() {
        this.mTransportType = -1;
        this.mLocalAddress = new InetSocketAddress(0);
        this.mSipServerAddress = new InetSocketAddress(0);
        this.mMaxUdpPayloadSize = -1;
        this.mPublicUserIdentifier = null;
        this.mPrivateUserIdentifier = null;
        this.mHomeDomain = null;
        this.mImei = null;
        this.mSipAuthHeader = null;
        this.mSipAuthNonce = null;
        this.mServiceRouteHeader = null;
        this.mUserAgentHeader = null;
        this.mContactUserParam = null;
        this.mPaniHeader = null;
        this.mPlaniHeader = null;
        this.mAssociatedUriHeader = null;
        this.mSecurityVerifyHeader = null;
        this.mVersion = 1;
    }

    private SipDelegateConfig(int i) {
        this.mTransportType = -1;
        this.mLocalAddress = new InetSocketAddress(0);
        this.mSipServerAddress = new InetSocketAddress(0);
        this.mMaxUdpPayloadSize = -1;
        this.mPublicUserIdentifier = null;
        this.mPrivateUserIdentifier = null;
        this.mHomeDomain = null;
        this.mImei = null;
        this.mSipAuthHeader = null;
        this.mSipAuthNonce = null;
        this.mServiceRouteHeader = null;
        this.mUserAgentHeader = null;
        this.mContactUserParam = null;
        this.mPaniHeader = null;
        this.mPlaniHeader = null;
        this.mAssociatedUriHeader = null;
        this.mSecurityVerifyHeader = null;
        this.mVersion = i;
    }

    /* access modifiers changed from: package-private */
    public Builder getBuilder() {
        return (Builder) Optional.ofNullable(this.mBuilder).orElseGet(new SipDelegateConfig$$ExternalSyntheticLambda0(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Builder lambda$getBuilder$0() {
        Builder builder = new Builder(this.mVersion);
        this.mBuilder = builder;
        return builder;
    }

    public SipDelegateConfiguration convert() {
        synchronized (this) {
            this.mVersion = (int) ((((long) this.mVersion) + 1) % 2147483647L);
        }
        return new SipDelegateConfiguration.Builder((long) this.mVersion, this.mTransportType, this.mLocalAddress, this.mSipServerAddress).setHomeDomain(this.mHomeDomain).setImei(this.mImei).setPublicUserIdentifier(this.mPublicUserIdentifier).setPrivateUserIdentifier(this.mPrivateUserIdentifier).setSipContactUserParameter(this.mContactUserParam).setSipAuthenticationHeader(this.mSipAuthHeader).setSipAuthenticationNonce(this.mSipAuthNonce).setSipUserAgentHeader(this.mUserAgentHeader).setIpSecConfiguration(new SipDelegateConfiguration.IpSecConfiguration(0, 0, 0, 0, 0, 0, this.mSecurityVerifyHeader)).setSipPaniHeader(this.mPaniHeader).setSipPlaniHeader(this.mPlaniHeader).setSipServiceRouteHeader(this.mServiceRouteHeader).setSipAssociatedUriHeader(this.mAssociatedUriHeader).setMaxUdpPayloadSizeBytes(this.mMaxUdpPayloadSize).build();
    }

    public boolean isPaniChanged(String str, String str2) {
        IMSLog.i(LOG_TAG, String.format(Locale.US, "isPaniChanged: PANI %s -> %s, Last PANI: %s -> %s", new Object[]{IMSLog.checker(this.mPaniHeader, true), IMSLog.checker(str, true), IMSLog.checker(this.mPlaniHeader, true), IMSLog.checker(str2, true)}));
        if (!TextUtils.equals(this.mPaniHeader, str) || isLastPaniChanged(str2)) {
            return true;
        }
        return false;
    }

    private boolean isLastPaniChanged(String str) {
        return (!TextUtils.isEmpty(this.mPlaniHeader) || !TextUtils.isEmpty(str)) && !TextUtils.equals(this.mPlaniHeader, str);
    }

    static class Builder {
        SipDelegateConfig mNewConfig;

        Builder(int i) {
            this.mNewConfig = new SipDelegateConfig(i);
        }

        public Builder setMaxUdpPayloadSizeBytes(int i) {
            this.mNewConfig.mMaxUdpPayloadSize = i;
            return this;
        }

        public Builder setPublicUserIdentifier(String str) {
            this.mNewConfig.mPublicUserIdentifier = str;
            return this;
        }

        public Builder setPrivateUserIdentifier(String str) {
            this.mNewConfig.mPrivateUserIdentifier = str;
            return this;
        }

        public Builder setHomeDomain(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mNewConfig.mHomeDomain = str;
            } else {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setHomeDomain: Empty! Ignore.");
            }
            return this;
        }

        public Builder setImei(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mNewConfig.mImei = str;
            } else {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setImei: Empty! Ignore.");
            }
            return this;
        }

        public Builder setSipAuthenticationHeader(String str) {
            this.mNewConfig.mSipAuthHeader = str;
            return this;
        }

        public Builder setSipAuthenticationNonce(String str) {
            this.mNewConfig.mSipAuthNonce = str;
            return this;
        }

        public Builder setSipServiceRouteHeader(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mNewConfig.mServiceRouteHeader = str;
            } else {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setSipServiceRouteHeader: Empty! Ignore.");
            }
            return this;
        }

        public Builder setSipUserAgentHeader(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mNewConfig.mUserAgentHeader = str;
            } else {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setSipUserAgentHeader: Empty! Ignore.");
            }
            return this;
        }

        public Builder setSipContactUserParameter(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mNewConfig.mContactUserParam = str;
            } else {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setSipContactUserParameter: Empty! Ignore.");
            }
            return this;
        }

        public Builder setSipPaniHeader(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mNewConfig.mPaniHeader = str;
            } else {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setSipPaniHeader: Empty! Ignore.");
            }
            return this;
        }

        public Builder setSipPlaniHeader(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mNewConfig.mPlaniHeader = str;
            } else {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setSipPlaniHeader: Empty! Ignore.");
            }
            return this;
        }

        public Builder setSipAssociatedUriHeader(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mNewConfig.mAssociatedUriHeader = str;
            } else {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setSipAssociatedUriHeader: Empty! Ignore.");
            }
            return this;
        }

        public Builder setTransport(String str) {
            IMSLog.i(SipDelegateConfig.LOG_TAG, String.format("setTransport: [%s]", new Object[]{str}));
            this.mNewConfig.mTransportType = "UDP".equalsIgnoreCase(str) ^ true ? 1 : 0;
            return this;
        }

        public Builder setLocalAddress(String str, int i) {
            try {
                IMSLog.s(SipDelegateConfig.LOG_TAG, String.format(Locale.US, "setLocalAddress: %s:%d", new Object[]{str, Integer.valueOf(i)}));
                this.mNewConfig.mLocalAddress = new InetSocketAddress(InetAddress.getByName(str), i);
            } catch (UnknownHostException e) {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setLocalAddress: " + e);
            }
            return this;
        }

        public Builder setSecurityVerifyHeader(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mNewConfig.mSecurityVerifyHeader = str;
            } else {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setSecurityVerifyHeader: Empty! Ignore.");
            }
            return this;
        }

        public Builder setPcscfAddress(String str) {
            int intValue = ((Integer) Optional.ofNullable(this.mNewConfig.mServiceRouteHeader).map(new SipDelegateConfig$Builder$$ExternalSyntheticLambda0(str)).map(new SipDelegateConfig$Builder$$ExternalSyntheticLambda1()).orElse(5060)).intValue();
            try {
                IMSLog.i(SipDelegateConfig.LOG_TAG, String.format(Locale.US, "setPcscfAddress: %s:%d", new Object[]{str, Integer.valueOf(intValue)}));
                this.mNewConfig.mSipServerAddress = new InetSocketAddress(InetAddress.getByName(str), intValue);
            } catch (UnknownHostException e) {
                IMSLog.e(SipDelegateConfig.LOG_TAG, "setPcscfAddress: " + e);
            }
            return this;
        }

        /* access modifiers changed from: private */
        public static /* synthetic */ String lambda$setPcscfAddress$0(String str, String str2) {
            Matcher matcher = Pattern.compile(str + "]?:(\\d+)").matcher(str2);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }

        public SipDelegateConfig build() {
            this.mNewConfig.mBuilder = this;
            return this.mNewConfig;
        }
    }
}
