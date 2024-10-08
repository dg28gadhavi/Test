package com.sec.internal.helper;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.List;

public class RegiConfig {
    private static final String LOG_TAG = "RegiConfig";
    private String mAppRealm;
    private String mAppUserPwd;
    private Context mContext;
    private String mEndUserConfReqId;
    private String mHomeNetworkDomain;
    private List<String> mIpType;
    private boolean mKeepAlive;
    private List<String> mLboPcscfAddress;
    private int mPcscfIdx = 0;
    private int mPhoneId;
    private String mPrivateUserIdentity;
    private String mPublicUserIdentity;
    private String mQValue;
    private int mRcsVolteSingleReg;
    private int mRegRetryBaseTime;
    private int mRegRetryMaxTime;
    private int mTimer1;
    private int mTimer2;
    private int mTimer4;
    private String mTransProtoPsRoamSignaling;
    private String mTransProtoPsSignaling;
    private String mTransProtoWifiSignaling;
    private String mTransportPsMedia;
    private String mTransportPsMediaRoaming;
    private String mTransportWifiMedia;

    public RegiConfig(int i, Context context) {
        this.mPhoneId = i;
        this.mContext = context;
        loadDefaultValues();
    }

    private void loadDefaultValues() {
        this.mAppUserPwd = "";
        this.mTransportWifiMedia = "MSRPoTLS";
        this.mTransportPsMedia = DiagnosisConstants.RCSM_KEY_MSRP;
        this.mTransportPsMediaRoaming = DiagnosisConstants.RCSM_KEY_MSRP;
        this.mQValue = "";
        this.mTransProtoWifiSignaling = "SIPoTLS";
        this.mTransProtoPsSignaling = "SIPoUDP";
        this.mEndUserConfReqId = "";
        this.mTransProtoPsRoamSignaling = "SIPoUDP";
        this.mKeepAlive = true;
        this.mRcsVolteSingleReg = -1;
        this.mTimer1 = -1;
        this.mTimer2 = -1;
        this.mTimer4 = -1;
        this.mRegRetryBaseTime = -1;
        this.mRegRetryMaxTime = -1;
    }

    public void load() {
        setPcscfAddress();
        setPrivateUserIdentity();
        setPublicUserIdentity();
        setAppRealm();
        setAppUserPwd();
        setHomeNetworkDomain();
        setTransportWifiMedia();
        setTransportPsMedia();
        setKeepAlive();
        setQValue();
        setTransportProtoSignaling();
        setEndUserConfReqId();
        setRcsVolteSingleReg();
        setTimer();
        setRegRetryTime();
    }

    private void setPcscfAddress() {
        this.mLboPcscfAddress = RcsConfigurationHelper.readListStringParam(this.mContext, ImsUtil.getPathWithPhoneId("address", this.mPhoneId));
        this.mIpType = RcsConfigurationHelper.readListStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE, this.mPhoneId));
    }

    private void setPrivateUserIdentity() {
        this.mPrivateUserIdentity = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY, this.mPhoneId), (String) null);
    }

    private void setPublicUserIdentity() {
        this.mPublicUserIdentity = RcsConfigurationHelper.getImpu(this.mContext, this.mPhoneId);
    }

    private void setHomeNetworkDomain() {
        this.mHomeNetworkDomain = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME, this.mPhoneId), (String) null);
    }

    private void setAppUserPwd() {
        this.mAppUserPwd = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId("UserPwd", this.mPhoneId), "");
    }

    private void setAppRealm() {
        this.mAppRealm = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId("realm", this.mPhoneId), (String) null);
    }

    private void setTransportWifiMedia() {
        this.mTransportWifiMedia = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_MEDIA, this.mPhoneId), "MSRPoTLS");
    }

    private void setTransportPsMedia() {
        this.mTransportPsMedia = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_MEDIA, this.mPhoneId), DiagnosisConstants.RCSM_KEY_MSRP);
        this.mTransportPsMediaRoaming = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_MEDIA_ROAMING, this.mPhoneId), this.mTransportPsMedia);
    }

    private void setKeepAlive() {
        this.mKeepAlive = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.KEEP_ALIVE_ENABLED, this.mPhoneId), Boolean.TRUE).booleanValue();
    }

    public void setQValue() {
        this.mQValue = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.EXT_Q_VALUE, this.mPhoneId), "");
    }

    private void setTransportProtoSignaling() {
        this.mTransProtoWifiSignaling = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_SIGNALLING, this.mPhoneId), "SIPoTLS");
        this.mTransProtoPsSignaling = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING, this.mPhoneId), "SIPoUDP");
        this.mTransProtoPsRoamSignaling = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING_ROAMING, this.mPhoneId), this.mTransProtoPsSignaling);
    }

    private void setEndUserConfReqId() {
        this.mEndUserConfReqId = RcsConfigurationHelper.readStringParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.EXT_END_USER_CONF_REQID, this.mPhoneId), "");
    }

    private void setRcsVolteSingleReg() {
        this.mRcsVolteSingleReg = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.EXT_RCS_VOLTE_SINGLE_REGISTRATION, this.mPhoneId), -1).intValue();
    }

    private void setTimer() {
        this.mTimer1 = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TIMER_T1, this.mPhoneId), -1).intValue();
        this.mTimer2 = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TIMER_T2, this.mPhoneId), -1).intValue();
        this.mTimer4 = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.TIMER_T4, this.mPhoneId), -1).intValue();
    }

    private void setRegRetryTime() {
        this.mRegRetryBaseTime = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.REG_RETRY_BASE_TIME, this.mPhoneId), -1).intValue();
        this.mRegRetryMaxTime = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.REG_RETRY_MAX_TIME, this.mPhoneId), -1).intValue();
    }

    public String getPrivateUserIdentity() {
        return this.mPrivateUserIdentity;
    }

    public String getPublicUserIdentity() {
        return this.mPublicUserIdentity;
    }

    public String getHomeNetworkDomain() {
        return this.mHomeNetworkDomain;
    }

    public String getAppUserPwd(String str) {
        return TextUtils.isEmpty(this.mAppUserPwd) ? str : this.mAppUserPwd;
    }

    public String getAppRealm() {
        return this.mAppRealm;
    }

    public String getTransportWifiMedia() {
        return this.mTransportWifiMedia;
    }

    public String getTransportPsMedia() {
        return this.mTransportPsMedia;
    }

    public String getTransportPsMediaRoaming() {
        return this.mTransportPsMediaRoaming;
    }

    public boolean getKeepAlive() {
        return this.mKeepAlive;
    }

    public String getQValue() {
        return this.mQValue;
    }

    public String getTransProtoWifiSignaling() {
        return this.mTransProtoWifiSignaling;
    }

    public String getTransProtoPsSignaling() {
        return this.mTransProtoPsSignaling;
    }

    public String getTransProtoPsRoamSignaling() {
        return this.mTransProtoPsRoamSignaling;
    }

    public String getEndUserConfReqId() {
        return this.mEndUserConfReqId;
    }

    public int getRcsVolteSingleReg() {
        return this.mRcsVolteSingleReg;
    }

    public int getTimer1(int i) {
        int i2 = this.mTimer1;
        return i2 != -1 ? i2 : i;
    }

    public int getTimer2(int i) {
        int i2 = this.mTimer2;
        return i2 != -1 ? i2 : i;
    }

    public int getTimer4(int i) {
        int i2 = this.mTimer4;
        return i2 != -1 ? i2 : i;
    }

    public int getRegRetryBaseTime(int i) {
        int i2 = this.mRegRetryBaseTime;
        return i2 != -1 ? i2 : i;
    }

    public int getRegRetryMaxTime(int i) {
        int i2 = this.mRegRetryMaxTime;
        return i2 != -1 ? i2 : i;
    }

    public Bundle getLboPcscfAddressAndIpType() {
        String str;
        String str2;
        if (CollectionUtils.isNullOrEmpty((Collection<?>) this.mLboPcscfAddress)) {
            str2 = null;
            str = null;
        } else if (ConfigUtil.isRcsChn(SimUtil.getSimMno(this.mPhoneId))) {
            if (this.mPcscfIdx >= this.mLboPcscfAddress.size()) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getRcsPcscfAddress : reset pcscfIdx because lboPcscflist is changed");
                this.mPcscfIdx = 0;
            }
            str2 = this.mLboPcscfAddress.get(this.mPcscfIdx);
            str = this.mIpType.get(this.mPcscfIdx);
            IMSLog.d(LOG_TAG, this.mPhoneId, "getRcsPcscfAddress mPcscfIdx:" + this.mPcscfIdx + " lboPcscfAddress:" + str2 + " ipType:" + str);
            this.mPcscfIdx = (this.mPcscfIdx + 1) % this.mLboPcscfAddress.size();
        } else {
            str2 = this.mLboPcscfAddress.get(0);
            str = this.mIpType.get(0);
        }
        Bundle bundle = new Bundle();
        bundle.putString("address", str2);
        bundle.putString(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE, str);
        return bundle;
    }
}
