package com.sec.internal.ims.cmstore.mcs.contactsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.sec.internal.constants.ims.cmstore.mcs.contactsync.McsContactSyncConstants;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;

public class McsContactSyncIntentReceiver extends BroadcastReceiver {
    private final String LOG_TAG = McsContactSyncIntentReceiver.class.getSimpleName();
    private final McsContactSync mMcsContactSync;
    private final int mPhoneId;

    public McsContactSyncIntentReceiver(McsContactSync mcsContactSync, int i) {
        this.mMcsContactSync = mcsContactSync;
        this.mPhoneId = i;
    }

    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(McsContactSyncConstants.Intents.ACTION_MCS_SHARE_ACCESS_TOKEN);
        return intentFilter;
    }

    public IntentFilter getPackageIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        intentFilter.addDataScheme("package");
        return intentFilter;
    }

    public void onReceive(Context context, Intent intent) {
        if (McsContactSyncConstants.Intents.ACTION_MCS_SHARE_ACCESS_TOKEN.equals(intent.getAction())) {
            Bundle bundle = new Bundle();
            if (intent.getExtras() != null) {
                bundle.putAll(intent.getExtras());
            }
            EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "onReceive: ACTION_MCS_SHARE_ACCESS_TOKEN");
            IMSLog.c(LogClass.MCS_CS_RECEIVE_TOKEN_REQUEST, this.mPhoneId + ",CS:RCV_TK_REQ");
            McsContactSync mcsContactSync = this.mMcsContactSync;
            mcsContactSync.sendMessage(mcsContactSync.obtainMessage(4, bundle));
        } else if (("android.intent.action.PACKAGE_ADDED".equals(intent.getAction()) || "android.intent.action.PACKAGE_DATA_CLEARED".equals(intent.getAction())) && McsContactSyncConstants.Packages.CS_PACKAGE_NAME.equals(intent.getData().getSchemeSpecificPart())) {
            String str = this.LOG_TAG;
            int i = this.mPhoneId;
            EventLogHelper.infoLogAndAdd(str, i, "onReceive: " + intent.getAction());
            IMSLog.c(LogClass.MCS_CS_RECEIVE_PACKAGE_ACTION, this.mPhoneId + ",CS:RCV_PKG_ACT");
            McsContactSync mcsContactSync2 = this.mMcsContactSync;
            mcsContactSync2.sendMessage(mcsContactSync2.obtainMessage(2, Boolean.FALSE));
        }
    }
}
