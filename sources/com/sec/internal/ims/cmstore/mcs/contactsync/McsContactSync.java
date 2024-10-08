package com.sec.internal.ims.cmstore.mcs.contactsync;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.sec.ims.ICentralMsgStoreServiceListener;
import com.sec.internal.constants.ims.cmstore.mcs.contactsync.McsContactSyncConstants;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.mcs.MCSClient;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.cmstore.IMcsFcmPushNotificationListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.McsLargePollingNotification;
import com.sec.internal.omanetapi.nms.data.NmsEventList;
import com.sec.sve.generalevent.VcidEvent;

public class McsContactSync extends Handler {
    private static final Signature CS_SIGNATURES = new Signature("3082030b308201f3a003020102020427ba3599300d06092a864886f70d01010b050030363120301e060355040b131750726f64756374506c616e6e696e674469766973696f6e3112301006035504031309534b54656c65636f6d301e170d3133303432353034313334345a170d3433303431383034313334345a30363120301e060355040b131750726f64756374506c616e6e696e674469766973696f6e3112301006035504031309534b54656c65636f6d30820122300d06092a864886f70d01010105000382010f003082010a0282010100bc1ee71062800dc6ea0aad37ac18de0f5c827a90f64c8ec8c61718e58a675d80e81016872f1f1da06e0f05f8423e0037727adefbd043993244822539158d47b521b5d883a151523ee2f4a2fdd19ac364f29a93c1e05159a8b90902968237edf5931e20ca4d80a76bd5f699430d9dfd57387b49063860fe567780bd7e7b6206fef3e030cd1ec39c324cf8914b1c3b1cf1142f16a18bcf401c87ec1253fb1161cc5b97677973c730e574c54ade32ace9693d36be430fc1191ab42938a54d0b36b11e474a2b859682d21aa78ee1e3d023e738ac704edcdc710e16b267985e65f3647c4b040110fa2c03f91ad4055b2a740dcfc1cab708492b9575902a0f97b807f50203010001a321301f301d0603551d0e0416041402ff5ede4a1a2017d26f1d3e6079083f8ab63dc6300d06092a864886f70d01010b050003820101007f10b1b880d054df5bcaab1204071ee9cc554601b431a886e318a5a600a73c4a0300e4c27d09ddea81d2c9f858baf83dc517b68e760dda559fb2500fbd64c308fc367bc038e39d53794612023b08f759dcf13b7d74002e680a1f661bed3f0aa138325345318ee80566bbe4263d42317fd2ff0746ab78d160dc2af871297f2110fc6db4b270a6b0e45b85de411cb02b881721c655127f1e7678af0c96d7621a1822d9931afe5f1e6bfae05f88a800c0783e2c4bd98b81e9de14f54ff9561a9f1b13fb00c44ca10d62b8a22a601e536440b7090355212594f2cb48fbe8e136c46c9169784e9faf42a417bdd3603cb652f6c6e49d6c7b836d310cdbc14f111e254c");
    public static final int EVENT_DEFAULT_SMS_PACKAGE_CHANGED_TO_NON_SEC = 8;
    static final int EVENT_MCS_ACCESS_TOKEN_SHARE = 4;
    private static final int EVENT_MCS_CONTACT_SYNC_TRIGGER_BY_NETWORK = 7;
    private static final int EVENT_MCS_DEREGISTRATION_COMPLETED = 6;
    private static final int EVENT_MCS_REGISTRATION_COMPLETED = 5;
    public static final int EVENT_MCS_START_CONTACT_SYNC_INIT = 1;
    public static final int EVENT_MCS_START_CONTACT_SYNC_UPDATE = 2;
    private static final int EVENT_MCS_STOP_CONTACT_SYNC = 3;
    /* access modifiers changed from: private */
    public final String LOG_TAG;
    private final ICentralMsgStoreServiceListener mCmsListener;
    private final Context mContext;
    private McsContactSyncIntentReceiver mIntentReceiver;
    private final MCSClient mMcsClient;
    /* access modifiers changed from: private */
    public final int mPhoneId;
    private PackageManager pm = null;

    public McsContactSync(MCSClient mCSClient, Context context, int i) {
        String simpleName = McsContactSync.class.getSimpleName();
        this.LOG_TAG = simpleName;
        AnonymousClass1 r1 = new ICentralMsgStoreServiceListener.Stub() {
            public void onCmsAccountInfoDelivered(String str, String str2, int i) {
            }

            public void onCmsPushMessageReceived(String str, String str2, String str3) {
            }

            public void onCmsSdChanged(boolean z, String str, int i) {
            }

            public void onCmsSdManagementCompleted(int i, String str, int i2, int i3) {
            }

            public void onCmsRegistrationCompleted(int i, int i2) {
                String r0 = McsContactSync.this.LOG_TAG;
                int r1 = McsContactSync.this.mPhoneId;
                IMSLog.i(r0, r1, "onCmsRegistrationCompleted: result = " + i);
                if (i == 100 && i2 != 1) {
                    McsContactSync mcsContactSync = McsContactSync.this;
                    mcsContactSync.sendMessage(mcsContactSync.obtainMessage(5, Integer.valueOf(i2)));
                }
            }

            public void onCmsDeRegistrationCompleted(int i) {
                String r0 = McsContactSync.this.LOG_TAG;
                int r1 = McsContactSync.this.mPhoneId;
                IMSLog.i(r0, r1, "onCmsDeRegistrationCompleted: result = " + i);
                if (i == 100) {
                    McsContactSync.this.sendEmptyMessage(6);
                }
            }
        };
        this.mCmsListener = r1;
        this.mMcsClient = mCSClient;
        this.mContext = context;
        this.mPhoneId = i;
        this.pm = context.getPackageManager();
        registerMcsContactSyncIntentReceiver();
        registerContactPushEventListPushListener();
        mCSClient.registerCmsProvisioningListener(r1, false);
        IMSLog.i(simpleName, i, "created");
    }

    private void registerContactPushEventListPushListener() {
        this.mMcsClient.setMcsFcmPushNotificationListener(new IMcsFcmPushNotificationListener() {
            public void largePollingPushNotification(McsLargePollingNotification mcsLargePollingNotification) {
            }

            public void nmsEventListPushNotification(NmsEventList nmsEventList) {
            }

            public void syncBlockfilterPushNotification(String str) {
            }

            public void syncConfigPushNotification(String str) {
            }

            public void syncMessagePushNotification(String str, int i) {
            }

            public void syncStatusPushNotification(String str) {
            }

            public void syncContactPushNotification(String str) {
                McsContactSync mcsContactSync = McsContactSync.this;
                mcsContactSync.sendMessage(mcsContactSync.obtainMessage(7, str));
            }
        });
    }

    public void handleMessage(Message message) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: msg: " + message.what);
        switch (message.what) {
            case 1:
                startMcsContactSync(true, false, ((Boolean) message.obj).booleanValue());
                return;
            case 2:
                startMcsContactSync(false, false, ((Boolean) message.obj).booleanValue());
                return;
            case 3:
                stopMcsContactSync(((Boolean) message.obj).booleanValue(), false);
                return;
            case 4:
                Bundle bundle = (Bundle) message.obj;
                if (bundle != null) {
                    handleShareMcsAccessToken(bundle.getString(McsContactSyncConstants.Intents.EXTRA_USER_MDN, ""), TextUtils.equals("yes", bundle.getString(McsContactSyncConstants.Intents.EXTRA_FORCE_REFRESH, "")));
                    return;
                }
                return;
            case 5:
                handleMcsRegistrationCompleted(((Integer) message.obj).intValue());
                return;
            case 6:
                handleMcsDeRegistrationCompleted();
                return;
            case 7:
                handleMcsContactSyncTriggerByNetwork((String) message.obj);
                return;
            case 8:
                stopMcsContactSync(false, false);
                return;
            default:
                IMSLog.e(this.LOG_TAG, this.mPhoneId, "handleMessage: Undefined message.");
                return;
        }
    }

    private boolean checkCSAppSignatures() {
        if (PackageUtils.hasPackage(this.mContext, McsContactSyncConstants.Packages.CS_PACKAGE_NAME)) {
            try {
                SigningInfo signingInfo = this.pm.getPackageInfo(McsContactSyncConstants.Packages.CS_PACKAGE_NAME, PackageManager.PackageInfoFlags.of(134217728)).signingInfo;
                if (signingInfo == null) {
                    IMSLog.i(this.LOG_TAG, this.mPhoneId, "checkCSAppSignatures: signingInfo is null.");
                    return false;
                }
                Signature[] apkContentsSigners = signingInfo.getApkContentsSigners();
                if (apkContentsSigners != null) {
                    for (Signature equals : apkContentsSigners) {
                        if (equals.equals(CS_SIGNATURES)) {
                            IMSLog.d(this.LOG_TAG, this.mPhoneId, "checkCSAppSignatures: matched signingInfo.");
                            return true;
                        }
                    }
                }
                IMSLog.i(this.LOG_TAG, this.mPhoneId, "checkCSAppSignatures: mismatched signingInfo.");
            } catch (PackageManager.NameNotFoundException unused) {
                IMSLog.i(this.LOG_TAG, "com.skt.contactsync is not installed");
            }
        }
        return false;
    }

    private void startMcsContactSync(boolean z, boolean z2, boolean z3) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "startMcsContactSync: initialSync = " + z + ", triggerByNetwork = " + z2 + ", needStartActivity = " + z3);
        if (!isMcsUser()) {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "startMcsContactSync: it's not MCS user");
        } else if (checkCSAppSignatures() && CmsUtil.isDefaultMessageAppInUse(this.mContext)) {
            String mcsUserMdn = getMcsUserMdn();
            String oasisServerRoot = getOasisServerRoot();
            String mcsAccessToken = getMcsAccessToken();
            if (TextUtils.isEmpty(mcsUserMdn) || TextUtils.isEmpty(oasisServerRoot) || TextUtils.isEmpty(mcsAccessToken)) {
                IMSLog.e(this.LOG_TAG, this.mPhoneId, "startMcsContactSync: mandatory param is invalid");
                return;
            }
            Intent intent = new Intent();
            if (z3) {
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(McsContactSyncConstants.Packages.CS_LAUNCH_URI));
                intent.addFlags(LogClass.SIM_EVENT);
            } else {
                intent.setAction(McsContactSyncConstants.Intents.ACTION_MCS_START_CONTACT_SYNC);
            }
            intent.putExtra(McsContactSyncConstants.Intents.EXTRA_USER_MDN, mcsUserMdn);
            intent.putExtra(McsContactSyncConstants.Intents.EXTRA_SERVER_ROOT, oasisServerRoot);
            intent.putExtra(McsContactSyncConstants.Intents.EXTRA_ACCESS_TOKEN, mcsAccessToken);
            if (z) {
                intent.putExtra(McsContactSyncConstants.Intents.EXTRA_INITIAL_SYNC, "yes");
            }
            if (z2) {
                intent.putExtra(McsContactSyncConstants.Intents.EXTRA_PUSH_EVENT, "yes");
            }
            intent.setPackage(McsContactSyncConstants.Packages.CS_PACKAGE_NAME);
            intent.addFlags(32);
            if (z3) {
                try {
                    this.mContext.startActivity(intent);
                    String str2 = this.LOG_TAG;
                    int i2 = this.mPhoneId;
                    EventLogHelper.add(str2, i2, "startActivity [initial: " + z + "]");
                    IMSLog.c(LogClass.MCS_CS_START_ACTIVITY, this.mPhoneId + ",CS:ST_ACT," + z);
                } catch (ActivityNotFoundException e) {
                    String str3 = this.LOG_TAG;
                    int i3 = this.mPhoneId;
                    IMSLog.e(str3, i3, "startMcsContactSync: " + e.getMessage());
                }
            } else {
                this.mContext.sendBroadcast(intent);
                String str4 = this.LOG_TAG;
                int i4 = this.mPhoneId;
                EventLogHelper.add(str4, i4, "start [initial: " + z + ", push: " + z2 + "]");
                IMSLog.c(LogClass.MCS_CS_START, this.mPhoneId + ",CS:ST," + z + "," + z2);
            }
        }
    }

    public void stopMcsContactSync(boolean z, boolean z2) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "stopMcsContactSync: serviceOff = " + z + ", triggerByNetwork = " + z2);
        String mcsUserMdn = getMcsUserMdn();
        if (checkCSAppSignatures()) {
            if (TextUtils.isEmpty(mcsUserMdn)) {
                IMSLog.e(this.LOG_TAG, this.mPhoneId, "stopMcsContactSync: userMdn is empty");
                return;
            }
            Intent intent = new Intent();
            intent.setAction(McsContactSyncConstants.Intents.ACTION_MCS_STOP_CONTACT_SYNC);
            intent.putExtra(McsContactSyncConstants.Intents.EXTRA_USER_MDN, mcsUserMdn);
            if (z) {
                intent.putExtra(McsContactSyncConstants.Intents.EXTRA_SERVICE_OFF, "yes");
            }
            if (z2) {
                intent.putExtra(McsContactSyncConstants.Intents.EXTRA_PUSH_EVENT, "yes");
            }
            intent.setPackage(McsContactSyncConstants.Packages.CS_PACKAGE_NAME);
            intent.addFlags(32);
            String str2 = this.LOG_TAG;
            int i2 = this.mPhoneId;
            EventLogHelper.add(str2, i2, "stop  [serviceOff: " + z + ", push: " + z2 + "]");
            IMSLog.c(LogClass.MCS_CS_STOP, this.mPhoneId + ",CS:STP," + z + "," + z2);
            this.mContext.sendBroadcast(intent);
        }
    }

    private void handleShareMcsAccessToken(String str, boolean z) {
        String str2 = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "handleShareMcsAccessToken: userMdn = " + IMSLog.numberChecker(str) + ", forceRefresh = " + z);
        if (!isMcsUser()) {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "handleShareMcsAccessToken: it's not MCS user");
        } else if (TextUtils.isEmpty(str) || TextUtils.equals(str, getMcsUserMdn())) {
            IMSLog.c(LogClass.MCS_CS_SHARE_ACCESS_TOKEN, this.mPhoneId + ",CS:SHR_TK," + z);
            if (this.mMcsClient.requestMcsAccessToken(z)) {
                IMSLog.i(this.LOG_TAG, this.mPhoneId, "handleShareMcsAccessToken: requested to refresh");
            } else if (z) {
                IMSLog.e(this.LOG_TAG, this.mPhoneId, "handleShareMcsAccessToken: requestMcsAccessToken failed");
            } else {
                startMcsContactSync(false, false, false);
            }
        } else {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "handleShareMcsAccessToken: userMdn is different");
        }
    }

    private boolean isMcsUser() {
        return this.mMcsClient.getPrerenceManager().getMcsUser() == 1;
    }

    private String getMcsUserMdn() {
        String userCtn = this.mMcsClient.getPrerenceManager().getUserCtn();
        String formatNumberToE164 = PhoneNumberUtils.formatNumberToE164(userCtn, Util.getSimCountryCode(this.mContext, this.mPhoneId));
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "getMcsUserMdn: userMdn = " + userCtn + ", e164UserMdn = " + formatNumberToE164);
        return formatNumberToE164 != null ? formatNumberToE164 : "";
    }

    private String getOasisServerRoot() {
        String oasisServerRoot = this.mMcsClient.getPrerenceManager().getOasisServerRoot();
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "getOasisServerRoot: serverRoot = " + oasisServerRoot);
        return oasisServerRoot;
    }

    private String getMcsAccessToken() {
        String mcsAccessToken = this.mMcsClient.getPrerenceManager().getMcsAccessToken();
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "getMcsAccessToken: accessToken = " + mcsAccessToken);
        return mcsAccessToken;
    }

    private void registerMcsContactSyncIntentReceiver() {
        if (this.mIntentReceiver == null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "registerMcsContactSyncIntentReceiver");
            McsContactSyncIntentReceiver mcsContactSyncIntentReceiver = new McsContactSyncIntentReceiver(this, this.mPhoneId);
            this.mIntentReceiver = mcsContactSyncIntentReceiver;
            this.mContext.registerReceiver(mcsContactSyncIntentReceiver, mcsContactSyncIntentReceiver.getIntentFilter());
            Context context = this.mContext;
            McsContactSyncIntentReceiver mcsContactSyncIntentReceiver2 = this.mIntentReceiver;
            context.registerReceiver(mcsContactSyncIntentReceiver2, mcsContactSyncIntentReceiver2.getPackageIntentFilter());
        }
    }

    private void unregisterMcsContactSyncIntentReceiver() {
        if (this.mIntentReceiver != null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "unregisterMcsContactSyncIntentReceiver");
            this.mContext.unregisterReceiver(this.mIntentReceiver);
            this.mIntentReceiver = null;
        }
    }

    private void handleMcsRegistrationCompleted(int i) {
        String str = this.LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "handleMcsRegistrationCompleted: details = " + i);
        if (i == 2) {
            sendMessage(obtainMessage(1, Boolean.TRUE));
        } else if (i == 5) {
            sendMessage(obtainMessage(2, Boolean.FALSE));
        }
    }

    private void handleMcsDeRegistrationCompleted() {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "handleMcsDeRegistrationCompleted");
        sendMessage(obtainMessage(3, Boolean.TRUE));
    }

    private void handleMcsContactSyncTriggerByNetwork(String str) {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "handleMcsContactSyncTriggerByNetwork syncType : " + str);
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -838846263:
                if (str.equals("update")) {
                    c = 0;
                    break;
                }
                break;
            case 3237136:
                if (str.equals("init")) {
                    c = 1;
                    break;
                }
                break;
            case 3540994:
                if (str.equals(VcidEvent.BUNDLE_VALUE_ACTION_STOP)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                startMcsContactSync(false, true, false);
                return;
            case 1:
                startMcsContactSync(true, true, false);
                return;
            case 2:
                stopMcsContactSync(false, true);
                return;
            default:
                return;
        }
    }
}
