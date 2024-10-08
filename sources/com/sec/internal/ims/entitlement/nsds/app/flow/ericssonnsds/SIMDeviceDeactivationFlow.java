package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.DeviceDeactivation;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceDeactivation;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class SIMDeviceDeactivationFlow extends NSDSAppFlowBase implements ISIMDeviceDeactivation {
    private static final int DEACTIVATE_DEVICE = 0;
    private static final String LOG_TAG = SIMDeviceDeactivationFlow.class.getSimpleName();
    private static int mDeactivateCause = 0;

    /* access modifiers changed from: protected */
    public void queueOperation(int i, Bundle bundle) {
    }

    public SIMDeviceDeactivationFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        super(looper, context, baseFlowImpl, nSDSDatabaseHelper);
    }

    public void deactivateDevice(int i) {
        if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.e(LOG_TAG, "requestSimDeviceDeactivation: not activated");
            notifyNSDSFlowResponse(true, (String) null, -1, -1);
            return;
        }
        Message message = new Message();
        message.what = 0;
        message.arg1 = i;
        sendMessage(message);
    }

    private void handleResponseDeactivation(Bundle bundle) {
        int i = -1;
        boolean z = false;
        if (bundle == null) {
            IMSLog.i(LOG_TAG, "handleRefreshDeviceResponse. response is null");
        } else {
            ResponseManageConnectivity responseManageConnectivity = (ResponseManageConnectivity) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY);
            if (responseManageConnectivity != null) {
                int i2 = responseManageConnectivity.responseCode;
                if (i2 == 1000) {
                    String str = LOG_TAG;
                    IMSLog.i(str, "ResponseManageConnectivity content : messageId: " + responseManageConnectivity.messageId + ", responseCode: " + responseManageConnectivity.responseCode);
                    z = true;
                } else {
                    i = i2;
                }
            }
        }
        notifyNSDSFlowResponse(z, NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, 2, i);
    }

    private void performDeactivation(int i) {
        mDeactivateCause = i;
        String str = LOG_TAG;
        IMSLog.i(str, "deactivateDevice: deactivationCause" + i);
        DeviceDeactivation deviceDeactivation = new DeviceDeactivation(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0", this.mUserAgent, this.mImeiForUA);
        if (mDeactivateCause == 1) {
            deviceDeactivation.deactivateDevice();
        }
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        IMSLog.i(str, "handleMessage: " + message.what);
        int i = message.what;
        if (i == 0) {
            performDeactivation(message.arg1);
        } else if (i == 111) {
            handleResponseDeactivation(message.getData());
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean z, String str, int i, int i2) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "notifyNSDSFlowResponse: success " + z + " errorcode " + i2);
        this.mNSDSDatabaseHelper.resetDeviceStatus(this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getImsi(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        ArrayList arrayList = new ArrayList();
        if (z) {
            if (i2 == -1 && mDeactivateCause == 1) {
                arrayList.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_CONNECTIVITY_DEACTIVATION_SUCCESS_FOR_INVALID_FINGERPRINT));
            }
        } else if (!(str == null || i2 == -1)) {
            arrayList.add(Integer.valueOf(NSDSErrorTranslator.translate(str, i, i2)));
        }
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, z);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_DEACTIVATION_CAUSE, mDeactivateCause);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }
}
