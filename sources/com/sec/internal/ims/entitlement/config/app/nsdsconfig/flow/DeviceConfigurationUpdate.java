package com.sec.internal.ims.entitlement.config.app.nsdsconfig.flow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.DeviceConfiguration;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.MnoNsdsConfigStrategyCreator;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.ConfigurationRetrievalWithSIM;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.ConfigurationUpdate;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.Base64Decoder;
import com.sec.internal.ims.entitlement.util.DeviceConfigParser;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.entitlement.config.IMnoNsdsConfigStrategy;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class DeviceConfigurationUpdate extends NSDSAppFlowBase {
    public static final int FORCE_CONFIG_UPDATE = 2;
    private static final String LOG_TAG = DeviceConfigurationUpdate.class.getSimpleName();
    public static final int RETRIEVE_DEVICE_CONFIG = 0;
    public static final int UPDATE_DEVICE_CONFIG = 1;
    private int mIsConfigUpdated = 0;
    private boolean mIsForced = false;

    public DeviceConfigurationUpdate(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        super(looper, context, baseFlowImpl, nSDSDatabaseHelper);
    }

    public void performDeviceConfigRetrieval(int i, int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, "performDeviceConfigRetrieval: eventType " + i + " retryCount " + i2);
        this.mDeviceEventType = i;
        this.mRetryCount = i2;
        IMnoNsdsConfigStrategy mnoNsdsConfigStrategy = getMnoNsdsConfigStrategy();
        int nextOperation = mnoNsdsConfigStrategy != null ? mnoNsdsConfigStrategy.getNextOperation(this.mDeviceEventType, -1) : -1;
        if (nextOperation == -1) {
            IMSLog.i(str, "performDeviceConfigRetrieval: next operation is empty.");
        } else {
            queueOperation(nextOperation, (Bundle) null);
        }
    }

    private int getActionTrigger() {
        int i;
        String deviceId = this.mBaseFlowImpl.getDeviceId();
        if (checkGroupName(this.mDeviceGroup)) {
            i = 4;
        } else {
            i = NSDSSharedPrefHelper.getIntInDe(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF_CONFIG, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_ACTION_TRIGGER);
            String str = LOG_TAG;
            IMSLog.i(str, "getActionTrigger (saved) :  " + i);
            if (i == -1) {
                i = 0;
            } else if (i == 0) {
                i = 1;
            }
        }
        NSDSSharedPrefHelper.saveActionTrigger(this.mContext, deviceId, i);
        String str2 = LOG_TAG;
        IMSLog.i(str2, "getActionTrigger (updated) :  " + i);
        return i;
    }

    private void retrieveDeviceConfiguration() {
        IMnoNsdsConfigStrategy mnoNsdsConfigStrategy = getMnoNsdsConfigStrategy();
        ConfigurationRetrievalWithSIM configurationRetrievalWithSIM = new ConfigurationRetrievalWithSIM(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0", this.mUserAgent, this.mImeiForUA);
        String deviceId = this.mBaseFlowImpl.getDeviceId();
        String imsi = this.mBaseFlowImpl.getSimManager().getImsi();
        String vIMSIforSIMDevice = NSDSHelper.getVIMSIforSIMDevice(this.mContext, imsi);
        String entitlementServerUrl = mnoNsdsConfigStrategy != null ? mnoNsdsConfigStrategy.getEntitlementServerUrl(deviceId) : null;
        configurationRetrievalWithSIM.retriveDeviceConfiguration(entitlementServerUrl, this.mDeviceGroup, vIMSIforSIMDevice, NSDSHelper.getMSISDNFromSIM(this.mContext, this.mBaseFlowImpl.getSimManager().getSubscriptionId()), "1.0", NSDSDatabaseHelper.getConfigVersion(this.mContext, imsi), NSDSHelper.getTACfromCellInfo(this.mContext), getActionTrigger());
    }

    private void updateDeviceConfiguration(boolean z) {
        String imsi = this.mBaseFlowImpl.getSimManager().getImsi();
        String vIMSIforSIMDevice = NSDSHelper.getVIMSIforSIMDevice(this.mContext, imsi);
        this.mIsForced = z;
        new ConfigurationUpdate(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0", this.mUserAgent, this.mImeiForUA).updateDeviceConfiguration(this.mDeviceGroup, vIMSIforSIMDevice, NSDSHelper.getMSISDNFromSIM(this.mContext, this.mBaseFlowImpl.getSimManager().getSubscriptionId()), "1.0", NSDSDatabaseHelper.getConfigVersion(this.mContext, imsi), NSDSHelper.getTACfromCellInfo(this.mContext), getActionTrigger());
    }

    private void handleResponseGetDeviceConfig(Bundle bundle, boolean z) {
        String str;
        int httpErrRespCode = getHttpErrRespCode(bundle);
        String httpErrRespReason = getHttpErrRespReason(bundle);
        String str2 = LOG_TAG;
        IMSLog.i(str2, "handleResponseGetDeviceConfig: refresh " + z + ", http error code = " + httpErrRespCode + ", reason = " + httpErrRespReason);
        int i = 1;
        boolean z2 = false;
        if (bundle != null && httpErrRespCode <= 0 && httpErrRespReason == null) {
            ResponseManageConnectivity responseManageConnectivity = (ResponseManageConnectivity) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY);
            if (responseManageConnectivity != null) {
                int i2 = responseManageConnectivity.responseCode;
                if (i2 != 1000 || (str = responseManageConnectivity.deviceConfig) == null) {
                    httpErrRespCode = i2;
                } else {
                    String decode = Base64Decoder.decode(str);
                    responseManageConnectivity.deviceConfig = decode;
                    persistDeviceConfig(responseManageConnectivity, DeviceConfigParser.parseDeviceConfig(decode), this.mBaseFlowImpl.getSimManager().getImsi(), this.mIsForced);
                    z2 = true;
                }
            } else {
                IMSLog.e(str2, "ResponseManageConnectivity is NULL");
                httpErrRespCode = 1400;
            }
        }
        if (!z && !z2) {
            IMSLog.i(str2, "!!!Device config retrieval failed. report it!!!");
            notifyBootupDeviceActivationFailure(httpErrRespCode);
        }
        IMSLog.i(str2, "handleResponseGetDeviceConfig - response code = " + httpErrRespCode);
        String deviceId = this.mBaseFlowImpl.getDeviceId();
        if (NSDSNamespaces.NSDSDeviceState.DEVICECONFIG_IN_PROGRESS.equals(NSDSSharedPrefHelper.get(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE))) {
            IMSLog.i(str2, "handleResponseGetDeviceConfig... reset device_config_state");
            NSDSSharedPrefHelper.remove(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE);
        }
        if (!z) {
            i = 3;
        }
        notifyNSDSFlowResponse(z2, NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, i, httpErrRespCode);
    }

    private void persistDeviceConfig(ResponseManageConnectivity responseManageConnectivity, DeviceConfiguration deviceConfiguration, String str, boolean z) {
        if (responseManageConnectivity != null) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "ResponseManageConnectivity : messageId:" + responseManageConnectivity.messageId + " responseCode:" + responseManageConnectivity.responseCode + " serviceNames:" + responseManageConnectivity.serviceNames + " deviceConfig:" + hidePrivateInfoFromMsg(responseManageConnectivity.deviceConfig));
            if (responseManageConnectivity.responseCode == 1000) {
                String versionInfo = getVersionInfo(deviceConfiguration);
                String configVersion = NSDSDatabaseHelper.getConfigVersion(this.mContext, str);
                this.mIsConfigUpdated = 1;
                if (!this.mNSDSDatabaseHelper.isDeviceConfigAvailable(str)) {
                    NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_GROUP_NAME, this.mDeviceGroup);
                    this.mNSDSDatabaseHelper.insertDeviceConfig(responseManageConnectivity, versionInfo, str);
                    this.mIsConfigUpdated = 2;
                } else if (z || isConfigVersionUpdated(versionInfo, configVersion) || changedGroupName(this.mDeviceGroup)) {
                    this.mNSDSDatabaseHelper.updateDeviceConfig(responseManageConnectivity, versionInfo, str);
                    this.mIsConfigUpdated = 2;
                }
                IMnoNsdsConfigStrategy mnoNsdsConfigStrategy = getMnoNsdsConfigStrategy();
                if (mnoNsdsConfigStrategy != null) {
                    mnoNsdsConfigStrategy.scheduleRefreshDeviceConfig(this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
                }
                NSDSSharedPrefHelper.saveActionTrigger(this.mContext, this.mBaseFlowImpl.getDeviceId(), 1);
                return;
            }
            return;
        }
        IMSLog.e(LOG_TAG, "ResponseManageConnectivity is NULL");
    }

    private boolean isConfigVersionUpdated(String str, String str2) {
        String str3 = LOG_TAG;
        IMSLog.i(str3, "isConfigVersionUpdated: nwVersion-" + str + " dbVersion-" + str2);
        IMSLog.c(LogClass.ES_DC_VERSION, "NWV:" + str + ",DBV:" + str2);
        if (str == null || str2 == null) {
            IMSLog.e(str3, "isConfigVersionUpdated: invalid version info");
            return false;
        }
        try {
            if (Double.compare(Double.valueOf(str2).doubleValue(), Double.valueOf(str).doubleValue()) < 0) {
                IMSLog.i(str3, "isConfigVersionUpdated: config is updated");
                return true;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean changedGroupName(String str) {
        String deviceId = this.mBaseFlowImpl.getDeviceId();
        String str2 = NSDSSharedPrefHelper.get(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_GROUP_NAME);
        if (TextUtils.isEmpty(str2) || !str2.equalsIgnoreCase(str)) {
            NSDSSharedPrefHelper.save(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_GROUP_NAME, str);
            IMSLog.i(LOG_TAG, "changedGroupName : changed");
            IMSLog.c(LogClass.ES_DC_GROUP_NAME, "CHANGED");
            return true;
        }
        IMSLog.i(LOG_TAG, "changedGroupName: not changed");
        return false;
    }

    private boolean checkGroupName(String str) {
        String str2 = NSDSSharedPrefHelper.get(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_GROUP_NAME);
        if (TextUtils.isEmpty(str2) || str2.equalsIgnoreCase(str)) {
            return false;
        }
        IMSLog.i(LOG_TAG, "checkGroupName : changed");
        return true;
    }

    private String getVersionInfo(DeviceConfiguration deviceConfiguration) {
        DeviceConfiguration.ConfigInfo configInfo;
        if (deviceConfiguration != null && (configInfo = deviceConfiguration.mConfigInfo) != null) {
            return configInfo.mVersion;
        }
        IMSLog.e(LOG_TAG, "getVersionInfo: configuration info is null, vail");
        return null;
    }

    private void notifyBootupDeviceActivationFailure(int i) {
        ArrayList arrayList = new ArrayList();
        int translate = NSDSErrorTranslator.translate(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, 3, i);
        int simSlotIndex = this.mBaseFlowImpl.getSimManager().getSimSlotIndex();
        arrayList.add(Integer.valueOf(translate));
        arrayList.add(1400);
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, simSlotIndex);
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        notifyCallbackForNsdsEvent(0, simSlotIndex);
    }

    /* access modifiers changed from: protected */
    public IMnoNsdsConfigStrategy getMnoNsdsConfigStrategy() {
        return MnoNsdsConfigStrategyCreator.getMnoStrategy(this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 0) {
            retrieveDeviceConfiguration();
        } else if (i == 1) {
            updateDeviceConfiguration(false);
        } else if (i == 2) {
            updateDeviceConfiguration(true);
        } else if (i == 102) {
            handleResponseGetDeviceConfig(message.getData(), false);
        } else if (i != 109) {
            String str = LOG_TAG;
            IMSLog.i(str, "Unknown flow request: " + message.what);
        } else {
            handleResponseGetDeviceConfig(message.getData(), true);
        }
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int i, Bundle bundle) {
        int i2;
        if (i == 10) {
            i2 = 0;
        } else if (i == 11) {
            i2 = 1;
        } else if (i != 14) {
            IMSLog.i(LOG_TAG, "queueOperation: did not match any nsds base operations");
            i2 = -1;
        } else {
            i2 = 2;
        }
        if (i2 != -1) {
            Message obtainMessage = obtainMessage(i2);
            obtainMessage.setData(bundle);
            sendMessage(obtainMessage);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean z, String str, int i, int i2) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "notifyNSDSFlowResponse: success " + z + " isForced " + this.mIsForced);
        StringBuilder sb = new StringBuilder();
        sb.append("SUCS:");
        sb.append(z);
        IMSLog.c(LogClass.ES_DC_RESULT, sb.toString());
        ArrayList arrayList = new ArrayList();
        if (!(z || str == null || i2 == -1)) {
            int translate = NSDSErrorTranslator.translate(str, i, i2);
            arrayList.add(Integer.valueOf(translate));
            IMSLog.i(str2, "notifyNSDSFlowResponse: errorCode " + translate);
        }
        int i3 = this.mIsConfigUpdated;
        boolean z2 = i3 == 2;
        if (i3 > 0) {
            arrayList.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_CONNECTIVITY_NEW_CONFIG_UPDATED));
            this.mIsConfigUpdated = 0;
        }
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, z);
        intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
        intent.putExtra("retry_count", this.mRetryCount);
        intent.putExtra(NSDSNamespaces.NSDSExtras.FORCED_CONFIG_UPDATE, this.mIsForced);
        intent.putExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, i2);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.CHANGED_CONFIG, z2);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        this.mIsForced = false;
    }

    private String hidePrivateInfoFromMsg(String str) {
        if (TextUtils.isEmpty(str)) {
            return "NO CONFIG";
        }
        if (!Debug.isProductShip()) {
            return str;
        }
        return str.replaceAll("sip:+[0-9+-]+", "sip:xxxxxxxxxxx").replaceAll("tel:+[0-9+-]+", "tel:xxxxxxxxxxx");
    }
}
