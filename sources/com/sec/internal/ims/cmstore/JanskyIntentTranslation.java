package com.sec.internal.ims.cmstore;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.TelephonyUtilsWrapper;
import com.sec.internal.ims.cmstore.CloudMessageIntent;
import com.sec.internal.log.IMSLog;

public class JanskyIntentTranslation {
    private String LOG_TAG;
    private final Context mContext;
    private int mPhoneId;
    private MessageStoreClient mStoreClient;

    public JanskyIntentTranslation(Context context, MessageStoreClient messageStoreClient) {
        String simpleName = JanskyIntentTranslation.class.getSimpleName();
        this.LOG_TAG = simpleName;
        Log.i(simpleName, "Create JanskyServiceTranslation.");
        this.mContext = context;
        this.mStoreClient = messageStoreClient;
        this.mPhoneId = messageStoreClient.getClientID();
        this.LOG_TAG += "[" + this.mStoreClient.getClientID() + "]";
    }

    public void onNotifyMessageApp(String str, String str2, boolean z) {
        Intent intent = new Intent(CloudMessageIntent.Action.MSGINTENT);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, str);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, str2);
        intent.putExtra("linenum", this.mStoreClient.getPrerenceManager().getUserCtn());
        if ("FT".equals(str)) {
            intent.putExtra(CloudMessageIntent.Extras.FETCH_URI_RESPONSE, !z);
        }
        String str3 = this.LOG_TAG;
        Log.i(str3, "onNotifyMessageApp : " + str);
        String str4 = this.LOG_TAG;
        IMSLog.s(str4, "onNotifyMessageApp, broadcastIntent: " + intent.toString() + intent.getExtras());
        sendBroadcastToMsgApp(this.mContext, intent);
    }

    /* renamed from: com.sec.internal.ims.cmstore.JanskyIntentTranslation$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$InitialSyncStatusFlag[] r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.InitialSyncStatusFlag.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag = r0
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$InitialSyncStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.InitialSyncStatusFlag.START     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$InitialSyncStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.InitialSyncStatusFlag.FINISHED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$InitialSyncStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.InitialSyncStatusFlag.FAIL     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$InitialSyncStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.InitialSyncStatusFlag.IGNORED     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.JanskyIntentTranslation.AnonymousClass1.<clinit>():void");
        }
    }

    public void onNotifyMessageAppInitialSyncStatus(String str, String str2, CloudMessageBufferDBConstants.InitialSyncStatusFlag initialSyncStatusFlag) {
        Intent intent;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag[initialSyncStatusFlag.ordinal()];
        if (i == 1) {
            intent = new Intent(CloudMessageIntent.Action.MSGINTENT_INITSYNSTART);
        } else if (i == 2) {
            intent = new Intent(CloudMessageIntent.Action.MSGINTENT_INITSYNCEND);
        } else if (i != 3) {
            intent = null;
        } else {
            intent = new Intent(CloudMessageIntent.Action.MSGINTENT_INITSYNCFAIL);
        }
        if (intent != null) {
            intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
            intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, str2);
            intent.putExtra("linenum", str);
            String str3 = this.LOG_TAG;
            IMSLog.s(str3, "onNotifyMessageAppInitialSyncStatus, broadcastIntent: " + intent.toString() + intent.getExtras());
            sendBroadcastToMsgApp(this.mContext, intent);
        }
    }

    public void onNotifyVVMAppInitialSyncStatus(String str, String str2, CloudMessageBufferDBConstants.InitialSyncStatusFlag initialSyncStatusFlag, boolean z) {
        Intent intent;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag[initialSyncStatusFlag.ordinal()];
        if (i == 1) {
            intent = new Intent(CloudMessageIntent.Action.VVMINTENT_INITIALSYNCSTART);
        } else if (i == 2) {
            intent = new Intent(CloudMessageIntent.Action.VVMINTENT_INITIALSYNCEND);
        } else if (i != 3) {
            intent = i != 4 ? null : new Intent(CloudMessageIntent.Action.VVMINTENT_NORMALSYNCPROCESSING);
        } else {
            intent = new Intent(CloudMessageIntent.Action.VVMINTENT_INITIALSYNCFAIL);
        }
        if (intent != null) {
            intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
            intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, str2);
            intent.putExtra("linenum", str);
            intent.putExtra(CloudMessageIntent.Extras.FULLSYNC, z);
            String str3 = this.LOG_TAG;
            IMSLog.i(str3, "onNotifyVVMAppInitialSyncStatus messageType: " + str2);
            broadcastIntent(intent);
        }
    }

    public void onNotifyMessageAppCloudDeleteFailure(String str, String str2) {
        Intent intent = new Intent(CloudMessageIntent.Action.MSGDELETEFAILURE);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, str);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, str2);
        intent.putExtra("linenum", this.mStoreClient.getPrerenceManager().getUserCtn());
        String str3 = this.LOG_TAG;
        Log.i(str3, "onNotifyMessageAppCloudDeleteFailure : " + str);
        String str4 = this.LOG_TAG;
        IMSLog.s(str4, "onNotifyMessageAppCloudDeleteFailure, broadcastIntent: " + intent.toString() + intent.getExtras());
        sendBroadcastToMsgApp(this.mContext, intent);
    }

    public void onNotifyMessageAppUI(int i, String str, int i2) {
        Intent intent = new Intent(CloudMessageIntent.Action.MSGUIINTENT);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.ExtrasAMBSUI.SCREENNAME, i);
        intent.putExtra(CloudMessageIntent.ExtrasAMBSUI.STYLE, str);
        intent.putExtra(CloudMessageIntent.ExtrasAMBSUI.PARAM, i2);
        intent.putExtra("linenum", this.mStoreClient.getPrerenceManager().getUserCtn());
        String str2 = this.LOG_TAG;
        Log.i(str2, "onNotifyMessageAppUI : " + i);
        String str3 = this.LOG_TAG;
        IMSLog.s(str3, "onNotifyMessageAppUI, broadcastIntent: " + intent.toString() + intent.getExtras());
        sendBroadcastToMsgApp(this.mContext, intent);
    }

    public void onNotifyVVMApp(String str, String str2) {
        Intent intent = new Intent(CloudMessageIntent.Action.VVMINTENT);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, str);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, str2);
        intent.putExtra("linenum", this.mStoreClient.getPrerenceManager().getUserCtn());
        String str3 = this.LOG_TAG;
        Log.i(str3, "onNotifyVVMApp msgType: " + str);
        broadcastIntent(intent);
    }

    public void notifyAppNetworkOperationResult(boolean z) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyAppNetworkOperationResult opInProgress: " + z);
        Intent intent = new Intent(CloudMessageIntent.Action.VVMINTENT);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, "VVMDATA");
        intent.putExtra(CloudMessageIntent.Extras.NETWORK_OP_IN_PROGRESS, z);
        intent.putExtra("linenum", this.mStoreClient.getPrerenceManager().getUserCtn());
        broadcastIntent(intent);
    }

    public void onNotifyVVMAppCloudDeleteFailure(String str, String str2) {
        Intent intent = new Intent(CloudMessageIntent.Action.VVMDATADELETEFAILURE);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, str);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, str2);
        intent.putExtra("linenum", this.mStoreClient.getPrerenceManager().getUserCtn());
        String str3 = this.LOG_TAG;
        IMSLog.i(str3, "onNotifyVVMAppCloudDeleteFailure msgType: " + str);
        broadcastIntent(intent);
    }

    public void broadcastIntent(Intent intent) throws NullPointerException {
        String str = this.LOG_TAG;
        IMSLog.s(str, "broadcastIntent: " + intent.toString() + intent.getExtras());
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(this.mPhoneId));
        intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_VVM);
        intent.addFlags(IntentUtil.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        if (subscriptionUserHandle != null) {
            this.mContext.sendBroadcastAsUser(intent, subscriptionUserHandle);
        } else {
            this.mContext.sendBroadcastAsUser(intent, ContextExt.CURRENT_OR_SELF);
        }
    }

    public void sendBroadcastToMsgApp(Context context, Intent intent) {
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(this.mPhoneId));
        intent.putExtra("sim_slot", this.mStoreClient.getClientID());
        intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
        intent.addFlags(IntentUtil.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        if (subscriptionUserHandle != null) {
            context.sendBroadcastAsUser(intent, subscriptionUserHandle, CloudMessageIntent.Permission.MSGAPP);
        } else {
            context.sendBroadcastAsUser(intent, ContextExt.CURRENT_OR_SELF, CloudMessageIntent.Permission.MSGAPP);
        }
    }

    public void notifyAppOperationResult(String str, int i) {
        Intent intent = new Intent(CloudMessageIntent.Action.MSGAPPREQUEST);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra("body", str);
        intent.putExtra("code", i);
        intent.putExtra("linenum", this.mStoreClient.getPrerenceManager().getUserCtn());
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "notifyAppOperationResult, broadcastIntent: " + intent + " " + intent.getExtras());
        sendBroadcastToMsgApp(this.mContext, intent);
    }
}
