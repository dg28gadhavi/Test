package com.sec.internal.ims.core.cmc;

import android.content.Context;
import android.util.Log;
import com.samsung.android.cmcsetting.CmcSaInfo;
import com.samsung.android.cmcsetting.CmcSettingManager;
import com.samsung.android.cmcsetting.CmcSettingManagerConstants;
import com.samsung.android.cmcsetting.listeners.CmcActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcCallActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcDeviceInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcLineInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcNetworkModeInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcSameWifiNetworkStatusListener;
import com.samsung.android.cmcsetting.listeners.CmcSamsungAccountInfoChangedListener;
import java.util.List;

public class CmcSettingManagerWrapper {
    private static final String LOG_TAG = "CmcSettingManagerWrapper";
    private CmcAccountManager mCmcAccountMgr;
    CmcSettingManager mCmcSettingManager;
    protected Context mContext;

    public CmcSettingManagerWrapper(Context context, CmcAccountManager cmcAccountManager) {
        this.mContext = context;
        this.mCmcAccountMgr = cmcAccountManager;
    }

    public void init() {
        Log.i(LOG_TAG, "init");
        CmcSettingManager cmcSettingManager = new CmcSettingManager();
        this.mCmcSettingManager = cmcSettingManager;
        if (cmcSettingManager.init(this.mContext)) {
            Log.i(LOG_TAG, "init listeners");
            this.mCmcSettingManager.registerListener((CmcActivationInfoChangedListener) new CmcSettingManagerWrapper$$ExternalSyntheticLambda0(this));
            this.mCmcSettingManager.registerListener((CmcNetworkModeInfoChangedListener) new CmcSettingManagerWrapper$$ExternalSyntheticLambda1(this));
            this.mCmcSettingManager.registerListener((CmcLineInfoChangedListener) new CmcSettingManagerWrapper$$ExternalSyntheticLambda2(this));
            this.mCmcSettingManager.registerListener((CmcDeviceInfoChangedListener) new CmcSettingManagerWrapper$$ExternalSyntheticLambda3(this));
            this.mCmcSettingManager.registerListener((CmcCallActivationInfoChangedListener) new CmcSettingManagerWrapper$$ExternalSyntheticLambda4(this));
            this.mCmcSettingManager.registerListener((CmcSamsungAccountInfoChangedListener) new CmcSettingManagerWrapper$$ExternalSyntheticLambda5(this));
            this.mCmcSettingManager.registerListener((CmcSameWifiNetworkStatusListener) new CmcSettingManagerWrapper$$ExternalSyntheticLambda6(this));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$init$0() {
        Log.i(LOG_TAG, "onChangedCmcActivation");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$init$1() {
        Log.i(LOG_TAG, "onChangedNetworkMode");
        this.mCmcAccountMgr.notifyCmcNwPrefChanged();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$init$2() {
        Log.i(LOG_TAG, "onChangedLineInfo");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$init$3() {
        Log.i(LOG_TAG, "onChangedDeviceInfo");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$init$4() {
        Log.i(LOG_TAG, "onChangedCmcCallActivation");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$init$5() {
        Log.i(LOG_TAG, "onChangedSamsungAccountInfo:");
        this.mCmcAccountMgr.onChangedSamsungAccountInfo(getCmcSaAccessToken());
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$init$6() {
        Log.i(LOG_TAG, "onChangedSameWifiOnly:");
        this.mCmcAccountMgr.notifyCmcDeviceChanged();
    }

    public boolean getCmcSupported() {
        return this.mCmcSettingManager.getCmcSupported();
    }

    public String getDeviceType() {
        CmcSettingManagerConstants.DeviceType ownDeviceType = this.mCmcSettingManager.getOwnDeviceType();
        if (ownDeviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            return "pd";
        }
        return ownDeviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD ? "sd" : "";
    }

    public String getDeviceId() {
        return this.mCmcSettingManager.getOwnDeviceId();
    }

    public int getPreferredNetwork() {
        if (this.mCmcSettingManager.getOwnNetworkMode() == CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_USE_MOBILE_NETWORK) {
            return 0;
        }
        CmcSettingManagerConstants.NetworkMode networkMode = CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_USE_MOBILE_NETWORK;
        return 1;
    }

    public String getServiceVersion() {
        return this.mCmcSettingManager.getOwnServiceVersion();
    }

    public String getLineId() {
        return this.mCmcSettingManager.getLineId();
    }

    public List<String> getDeviceIdList() {
        return this.mCmcSettingManager.getDeviceIdList();
    }

    public String getLineImpu() {
        return this.mCmcSettingManager.getLineImpu();
    }

    public String getDeviceTypeWithDeviceId(String str) {
        CmcSettingManagerConstants.DeviceType deviceType = this.mCmcSettingManager.getDeviceType(str);
        if (deviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            return "pd";
        }
        return deviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD ? "sd" : "";
    }

    public List<String> getPcscfAddressList() {
        return this.mCmcSettingManager.getLinePcscfAddrList();
    }

    public boolean isCallAllowedSdByPd(String str) {
        if (this.mCmcSettingManager.getOwnDeviceType() == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            return true;
        }
        return this.mCmcSettingManager.isCallAllowedSdByPd(str);
    }

    public boolean getOwnCmcActivation() {
        return this.mCmcSettingManager.getOwnCmcActivation();
    }

    public boolean getCmcCallActivation(String str) {
        return this.mCmcSettingManager.getCmcCallActivation(str);
    }

    public String getCmcSaAccessToken() {
        CmcSaInfo samsungAccountInfo = this.mCmcSettingManager.getSamsungAccountInfo();
        if (samsungAccountInfo == null) {
            return "";
        }
        return samsungAccountInfo.getSaAccessToken();
    }

    public boolean isSameWifiNetworkOnly() {
        return this.mCmcSettingManager.isSameWifiNetworkOnly();
    }

    public boolean isEmergencyCallSupported() {
        return this.mCmcSettingManager.isEmergencyCallSupported();
    }

    public boolean isDualCmc() {
        return this.mCmcSettingManager.isDualSimSupportedOnPd() && this.mCmcSettingManager.getSelectedSimSlotsOnPd().size() > 1;
    }

    public List<Integer> getSelectedSimSlotsOnPd() {
        return this.mCmcSettingManager.getSelectedSimSlotsOnPd();
    }
}
