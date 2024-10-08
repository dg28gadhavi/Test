package com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.OperationUsingManagePushToken;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.SimSwapNSDSConfigHelper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISimSwapFlow;
import com.sec.internal.interfaces.ims.entitlement.nsds.SimSwapCompletedListener;
import java.util.ArrayList;

public class AttSimSwapFlow extends NSDSAppFlowBase implements ISimSwapFlow {
    private static final String LOG_TAG = AttSimSwapFlow.class.getSimpleName();
    private static final int REMOVE_PUSH_TOKEN = 0;
    private SimSwapCompletedListener mSimSwapCompletedListener;

    /* access modifiers changed from: protected */
    public void queueOperation(int i, Bundle bundle) {
    }

    public AttSimSwapFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        super(looper, context, baseFlowImpl, nSDSDatabaseHelper);
        this.mDeviceEventType = 3;
    }

    public void handleSimSwap(SimSwapCompletedListener simSwapCompletedListener) {
        this.mSimSwapCompletedListener = simSwapCompletedListener;
        Log.i(LOG_TAG, "handleSimSwap....");
        String deviceId = this.mBaseFlowImpl.getDeviceId();
        int simSlotIndex = this.mBaseFlowImpl.getSimManager().getSimSlotIndex();
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null || !mnoNsdsStrategy.isNsdsUIAppSwitchOn(deviceId, simSlotIndex)) {
            notifyNSDSFlowResponse(true, (String) null, -1, -1);
        } else {
            sendEmptyMessage(0);
        }
    }

    private void removePushToken() {
        Log.i(LOG_TAG, "removePushToken()");
        String configValue = SimSwapNSDSConfigHelper.getConfigValue(this.mContext, NSDSNamespaces.NSDSSharedPref.PREF_AKA_TOKEN);
        String configValue2 = SimSwapNSDSConfigHelper.getConfigValue(this.mContext, NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
        String configValue3 = SimSwapNSDSConfigHelper.getConfigValue(this.mContext, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        String configValue4 = SimSwapNSDSConfigHelper.getConfigValue(this.mContext, SimSwapNSDSConfigHelper.KEY_NATIVE_MSISDN);
        int simSlotIndex = this.mBaseFlowImpl.getSimManager().getSimSlotIndex();
        if (configValue4 == null) {
            configValue4 = NSDSHelper.getMSISDNFromSIM(this.mContext, simSlotIndex);
        }
        new OperationUsingManagePushToken(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0", (String) null, (String) null).removeVoWiFiPushToken(configValue4, (String) null, configValue3, NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM, configValue, configValue2, 30000);
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleRemovePushTokenResponse(Bundle bundle) {
        NSDSAppFlowBase.NSDSResponseStatus nSDSResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(1000, (String) null, -1);
        if (bundle == null) {
            return nSDSResponseStatus;
        }
        ResponseManagePushToken responseManagePushToken = (ResponseManagePushToken) bundle.getParcelable("managePushToken");
        if (responseManagePushToken != null) {
            String str = LOG_TAG;
            Log.i(str, "responseManagePushToken : messageId:" + responseManagePushToken.messageId + "responseCode:" + responseManagePushToken.responseCode);
            if (responseManagePushToken.responseCode != 1000) {
                Log.i(str, "responseManagePushToken failed");
                nSDSResponseStatus.failedOperation = 1;
            }
        } else {
            Log.e(LOG_TAG, "responseManagePushToken is NULL");
        }
        return nSDSResponseStatus;
    }

    private void resetDeviceStatus() {
        String deviceId = this.mBaseFlowImpl.getDeviceId();
        ISimManager simManager = this.mBaseFlowImpl.getSimManager();
        NSDSSharedPrefHelper.save(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.DEACTIVATED);
        NSDSSharedPrefHelper.remove(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
        NSDSSharedPrefHelper.removePrefForSlot(this.mContext, simManager.getSimSlotIndex(), NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
        NSDSSharedPrefHelper.removeAkaToken(this.mContext, simManager.getImsi());
        NSDSSharedPrefHelper.remove(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        NSDSSharedPrefHelper.remove(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER);
        NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, deviceId);
        this.mNSDSDatabaseHelper.resetE911AidInfoForNativeLine(deviceId);
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean z, String str, int i, int i2) {
        String str2 = LOG_TAG;
        Log.i(str2, "notifyNSDSFlowResponse: success " + z);
        resetDeviceStatus();
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE));
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, true);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        SimSwapCompletedListener simSwapCompletedListener = this.mSimSwapCompletedListener;
        if (simSwapCompletedListener != null) {
            simSwapCompletedListener.onSimSwapCompleted();
        }
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        Log.i(str, "msg:" + message.what);
        int i = message.what;
        if (i == 0) {
            removePushToken();
        } else if (i == 113) {
            performNextOperationIf(5, handleRemovePushTokenResponse(message.getData()), message.getData());
        }
    }
}
