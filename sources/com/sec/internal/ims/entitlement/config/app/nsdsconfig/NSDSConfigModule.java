package com.sec.internal.ims.entitlement.config.app.nsdsconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.entitlement.config.EntitlementConfigModuleBase;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.MnoNsdsConfigStrategyCreator;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.EntitlementConfigDBHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.IntentScheduler;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.entitlement.config.IEntitlementConfig;
import com.sec.internal.interfaces.ims.entitlement.config.IMnoNsdsConfigStrategy;
import com.sec.internal.log.IMSLog;

public class NSDSConfigModule extends EntitlementConfigModuleBase {
    private static final String ACTION_RECEIVED_SMS_NOTIFICATION = "android.intent.action.DATA_SMS_RECEIVED";
    private static final String DATA_AUTHORITY = "localhost";
    private static final String DATA_SCHEME = "sms";
    private static final String DEST_PORT = "16161";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = NSDSConfigModule.class.getSimpleName();
    private static final String NSDS_SMS_PUSH_MESSAGE = "carrierconfigupdate";
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z, Uri uri) {
            String r3 = NSDSConfigModule.LOG_TAG;
            Log.i(r3, "Uri changed:" + uri);
            if (Settings.Global.getUriFor(Extensions.Settings.Global.DEVICE_PROVISIONED).equals(uri)) {
                Log.i(NSDSConfigModule.LOG_TAG, "Setup Wizard is completed");
                String str = NSDSSharedPrefHelper.get(NSDSConfigModule.this.mContext, DeviceIdHelper.getDeviceId(NSDSConfigModule.this.mContext, NSDSConfigModule.this.mPhoneId), NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE);
                if (str == null || !NSDSNamespaces.NSDSDeviceState.DEVICECONFIG_IN_PROGRESS.equals(str)) {
                    NSDSConfigModule.this.onDeviceReady();
                } else {
                    Log.i(NSDSConfigModule.LOG_TAG, "Duplicate requests are ignored.");
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private IEntitlementConfig mEntitlementConfigImpl;
    final ContentObserver mEsimRequestObserver = new ContentObserver(this) {
        public void onChange(boolean z, Uri uri) {
            if (Settings.Global.getUriFor("sim_manager_esim_requested").equals(uri)) {
                Log.i(NSDSConfigModule.LOG_TAG, "eSIM status is changed");
                NSDSConfigModule.this.onDeviceReady();
            }
        }
    };
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog;
    private boolean mIsEsimRequestedObserverRegistered = false;
    protected BroadcastReceiver mNSDSPushSmsReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0048, code lost:
            if (r1.contains(com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.NSDS_SMS_PUSH_MESSAGE) != false) goto L_0x004a;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r6, android.content.Intent r7) {
            /*
                r5 = this;
                java.lang.String r6 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.LOG_TAG     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r0 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                int r0 = r0.mPhoneId     // Catch:{ SecurityException -> 0x00b8 }
                java.lang.String r1 = "onReceive: push SMS"
                com.sec.internal.log.IMSLog.i(r6, r0, r1)     // Catch:{ SecurityException -> 0x00b8 }
                android.telephony.SmsMessage[] r6 = android.provider.Telephony.Sms.Intents.getMessagesFromIntent(r7)     // Catch:{ SecurityException -> 0x00b8 }
                if (r6 == 0) goto L_0x00a6
                r0 = 0
                r6 = r6[r0]     // Catch:{ SecurityException -> 0x00b8 }
                if (r6 == 0) goto L_0x00a6
                java.lang.String r1 = "subscription"
                r2 = -1
                int r7 = r7.getIntExtra(r1, r2)     // Catch:{ SecurityException -> 0x00b8 }
                int r7 = com.sec.internal.helper.SimUtil.getSlotId(r7)     // Catch:{ SecurityException -> 0x00b8 }
                java.lang.String r1 = r6.getDisplayMessageBody()     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r2 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                int r2 = r2.mPhoneId     // Catch:{ SecurityException -> 0x00b8 }
                if (r2 != r7) goto L_0x0076
                boolean r2 = android.text.TextUtils.isEmpty(r1)     // Catch:{ SecurityException -> 0x00b8 }
                r3 = 1
                java.lang.String r4 = "carrierconfigupdate"
                if (r2 != 0) goto L_0x004c
                java.lang.String r2 = " "
                boolean r2 = r1.equals(r2)     // Catch:{ SecurityException -> 0x00b8 }
                if (r2 == 0) goto L_0x0044
                goto L_0x004c
            L_0x0044:
                boolean r6 = r1.contains(r4)     // Catch:{ SecurityException -> 0x00b8 }
                if (r6 == 0) goto L_0x007f
            L_0x004a:
                r0 = r3
                goto L_0x007f
            L_0x004c:
                java.lang.String r1 = new java.lang.String     // Catch:{ SecurityException -> 0x00b8 }
                byte[] r6 = r6.getUserData()     // Catch:{ SecurityException -> 0x00b8 }
                java.nio.charset.Charset r2 = java.nio.charset.StandardCharsets.UTF_16     // Catch:{ SecurityException -> 0x00b8 }
                r1.<init>(r6, r2)     // Catch:{ SecurityException -> 0x00b8 }
                boolean r6 = android.text.TextUtils.isEmpty(r1)     // Catch:{ SecurityException -> 0x00b8 }
                if (r6 != 0) goto L_0x0064
                boolean r6 = r1.contains(r4)     // Catch:{ SecurityException -> 0x00b8 }
                if (r6 == 0) goto L_0x0064
                goto L_0x004a
            L_0x0064:
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r6 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.helper.SimpleEventLog r6 = r6.mEventLog     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r1 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                int r1 = r1.mPhoneId     // Catch:{ SecurityException -> 0x00b8 }
                java.lang.String r2 = "onReceive: failed to read SMS data"
                r6.logAndAdd(r1, r2)     // Catch:{ SecurityException -> 0x00b8 }
                goto L_0x007f
            L_0x0076:
                java.lang.String r6 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.LOG_TAG     // Catch:{ SecurityException -> 0x00b8 }
                java.lang.String r1 = "onReceive: discard mismatch phoneId"
                com.sec.internal.log.IMSLog.i(r6, r7, r1)     // Catch:{ SecurityException -> 0x00b8 }
            L_0x007f:
                if (r0 == 0) goto L_0x00d5
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r6 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.helper.SimpleEventLog r6 = r6.mEventLog     // Catch:{ SecurityException -> 0x00b8 }
                java.lang.String r0 = "onReceive: start device config by push SMS"
                r6.logAndAdd(r7, r0)     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r6 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                android.content.Context r6 = r6.mContext     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r0 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                android.content.Context r0 = r0.mContext     // Catch:{ SecurityException -> 0x00b8 }
                java.lang.String r7 = com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper.getDeviceId(r0, r7)     // Catch:{ SecurityException -> 0x00b8 }
                r0 = 7
                com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.saveActionTrigger(r6, r7, r0)     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r5 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                r5.onDeviceReady()     // Catch:{ SecurityException -> 0x00b8 }
                goto L_0x00d5
            L_0x00a6:
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r6 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.helper.SimpleEventLog r6 = r6.mEventLog     // Catch:{ SecurityException -> 0x00b8 }
                com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule r5 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.this     // Catch:{ SecurityException -> 0x00b8 }
                int r5 = r5.mPhoneId     // Catch:{ SecurityException -> 0x00b8 }
                java.lang.String r7 = "onReceive : SMS data is empty"
                r6.logAndAdd(r5, r7)     // Catch:{ SecurityException -> 0x00b8 }
                goto L_0x00d5
            L_0x00b8:
                r5 = move-exception
                java.lang.String r6 = com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.LOG_TAG
                java.lang.StringBuilder r7 = new java.lang.StringBuilder
                r7.<init>()
                java.lang.String r0 = "onReceive: "
                r7.append(r0)
                java.lang.String r5 = r5.toString()
                r7.append(r5)
                java.lang.String r5 = r7.toString()
                com.sec.internal.log.IMSLog.e(r6, r5)
            L_0x00d5:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule.AnonymousClass4.onReceive(android.content.Context, android.content.Intent):void");
        }
    };
    private boolean mNeedUpdate = false;
    /* access modifiers changed from: private */
    public int mPhoneId;
    private ISimManager mSimManager;
    /* access modifiers changed from: private */
    public boolean mTimeoutEsimReady = false;
    protected BroadcastReceiver mTimeoutEsimReadyReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int intExtra = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
            String r0 = NSDSConfigModule.LOG_TAG;
            IMSLog.i(r0, intExtra, "onReceive: " + intent.getAction());
            if (intExtra == NSDSConfigModule.this.mPhoneId) {
                NSDSConfigModule.this.mEventLog.logAndAdd(intExtra, "eSIM timeout");
                IMSLog.c(LogClass.ES_DC_ESIM_TIMEOUT, NSDSConfigModule.this.mPhoneId + ",eSIM timeout");
                NSDSConfigModule.this.mTimeoutEsimReady = true;
                NSDSConfigModule.this.onDeviceReady();
            }
        }
    };

    public NSDSConfigModule(Looper looper, Context context, ISimManager iSimManager) {
        super(looper);
        this.mContext = context;
        this.mSimManager = iSimManager;
        this.mPhoneId = iSimManager.getSimSlotIndex();
        this.mEntitlementConfigImpl = new NSDSDeviceConfigImpl(getLooper(), this.mContext, this.mSimManager);
        registerContentObserver();
        registerTimeoutEsimReadyReceiver();
        registerPushSmsReceiver();
        init();
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 100);
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(Extensions.Settings.Global.DEVICE_PROVISIONED), false, this.mContentObserver);
    }

    private void registerTimeoutEsimReadyReceiver() {
        IMSLog.i(LOG_TAG, "registerTimeoutEsimReadyReceiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EntitlementNamespaces.EntitlementActions.ACTION_TIMEOUT_ESIM_READY);
        this.mContext.registerReceiver(this.mTimeoutEsimReadyReceiver, intentFilter);
    }

    private void registerPushSmsReceiver() {
        IMSLog.i(LOG_TAG, "registerPushSmsReceiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.DATA_SMS_RECEIVED");
        intentFilter.addDataScheme(DATA_SCHEME);
        intentFilter.addDataAuthority(DATA_AUTHORITY, DEST_PORT);
        this.mContext.registerReceiver(this.mNSDSPushSmsReceiver, intentFilter);
    }

    public void unregisterReceiver() {
        this.mContext.unregisterReceiver(this.mTimeoutEsimReadyReceiver);
        this.mContext.unregisterReceiver(this.mNSDSPushSmsReceiver);
    }

    public void registerEsimRequestedObserver() {
        if (!this.mIsEsimRequestedObserverRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "registerEsimRequestedObserver");
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("sim_manager_esim_requested"), false, this.mEsimRequestObserver);
            this.mIsEsimRequestedObserverRegistered = true;
        }
    }

    private void unregisterEsimRequestedObserver() {
        if (this.mIsEsimRequestedObserverRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterEsimRequestedObserver");
            this.mContext.getContentResolver().unregisterContentObserver(this.mEsimRequestObserver);
            this.mIsEsimRequestedObserverRegistered = false;
        }
    }

    public void onSimReady(boolean z) {
        String str = LOG_TAG;
        Log.i(str, "onSimReady: isSwapped " + z);
        this.mTimeoutEsimReady = false;
        if (z) {
            String prefForSlot = NSDSSharedPrefHelper.getPrefForSlot(this.mContext, this.mPhoneId, NSDSNamespaces.NSDSSharedPref.PREF_PREV_IMSI);
            if (!TextUtils.isEmpty(prefForSlot)) {
                EntitlementConfigDBHelper.deleteConfig(this.mContext, prefForSlot);
                Context context = this.mContext;
                NSDSSharedPrefHelper.saveActionTrigger(context, DeviceIdHelper.getDeviceId(context, this.mPhoneId), 2);
            }
        }
    }

    public final boolean esimRequested() {
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "sim_manager_esim_requested", 0) == 1) {
            z = true;
        }
        this.mEventLog.logAndAdd(this.mPhoneId, "esimRequested: " + z);
        return z;
    }

    private boolean isDuringCall() {
        ITelephonyManager instance = TelephonyManagerWrapper.getInstance(this.mContext);
        return (instance.getCallState(this.mPhoneId) == 0 && instance.getCallState(SimUtil.getOppositeSimSlot(this.mPhoneId)) == 0) ? false : true;
    }

    public void onDeviceReady() {
        start();
        IMnoNsdsConfigStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null && !mnoNsdsStrategy.isDeviceProvisioned()) {
            Log.i(LOG_TAG, "Waiting for OOBE setup complete...");
        } else if (!esimRequested() || this.mTimeoutEsimReady) {
            unregisterEsimRequestedObserver();
            IntentScheduler.stopTimer(this.mContext, this.mPhoneId, EntitlementNamespaces.EntitlementActions.ACTION_TIMEOUT_ESIM_READY);
            String deviceId = DeviceIdHelper.getDeviceId(this.mContext, this.mPhoneId);
            boolean isSimSwapPending = NSDSSharedPrefHelper.isSimSwapPending(this.mContext, deviceId);
            if (NSDSSharedPrefHelper.get(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE) != null) {
                Log.i(LOG_TAG, "onDeviceReady... reset deviceconfig_state");
                NSDSSharedPrefHelper.remove(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE);
            }
            if (otherSimInProgress(this.mPhoneId)) {
                Log.i(LOG_TAG, "Waiting for other SIM operation until 5sec");
                Bundle bundle = new Bundle();
                bundle.putInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mPhoneId);
                IntentScheduler.scheduleTimer(this.mContext, this.mPhoneId, EntitlementNamespaces.EntitlementActions.ACTION_RETRY_DEVICE_CONFIG, bundle, 5000);
                return;
            }
            IntentScheduler.stopTimer(this.mContext, this.mPhoneId, EntitlementNamespaces.EntitlementActions.ACTION_RETRY_DEVICE_CONFIG);
            NSDSSharedPrefHelper.save(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE, NSDSNamespaces.NSDSDeviceState.DEVICECONFIG_IN_PROGRESS);
            if (isDuringCall()) {
                this.mNeedUpdate = true;
                this.mEventLog.logAndAdd(this.mPhoneId, "during a call. retry after call ended");
                return;
            }
            if (isSimSwapPending) {
                NSDSSharedPrefHelper.clearSimSwapPending(this.mContext, deviceId);
            }
            this.mEntitlementConfigImpl.getDeviceConfig(this.mSimManager.getImsi(), 0);
        } else {
            this.mEventLog.logAndAdd(this.mPhoneId, "eSIM profile download is in progress");
            IMSLog.c(LogClass.ES_DC_ESIM_DELAY, this.mPhoneId + ",eSIM requested");
            registerEsimRequestedObserver();
            IntentScheduler.scheduleTimer(this.mContext, this.mPhoneId, EntitlementNamespaces.EntitlementActions.ACTION_TIMEOUT_ESIM_READY, new Bundle(), SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
        }
    }

    public void forceConfigUpdate() {
        this.mEntitlementConfigImpl.getDeviceConfig(this.mSimManager.getImsi(), 18);
    }

    public void retriveAkaToken() {
        String deviceId = DeviceIdHelper.getDeviceId(this.mContext, this.mPhoneId);
        if (NSDSSharedPrefHelper.isSimSwapPending(this.mContext, deviceId)) {
            NSDSSharedPrefHelper.clearSimSwapPending(this.mContext, deviceId);
        }
        this.mEntitlementConfigImpl.getDeviceConfig(this.mSimManager.getImsi(), 19);
    }

    public void updateTelephonyCallStatus(int i, int i2) {
        if (this.mNeedUpdate && !isDuringCall()) {
            this.mEventLog.logAndAdd(this.mPhoneId, "Call ended. Resume configuration");
            this.mNeedUpdate = false;
            onDeviceReady();
        }
    }

    private IMnoNsdsConfigStrategy getMnoNsdsStrategy() {
        return MnoNsdsConfigStrategyCreator.getMnoStrategy(this.mPhoneId);
    }

    private boolean otherSimInProgress(int i) {
        String str;
        Log.i(LOG_TAG, "Check otherSimInProgress");
        for (ISimManager simSlotIndex : SimManagerFactory.getAllSimManagers()) {
            int simSlotIndex2 = simSlotIndex.getSimSlotIndex();
            if (simSlotIndex2 != i && (str = NSDSSharedPrefHelper.get(this.mContext, DeviceIdHelper.getDeviceId(this.mContext, simSlotIndex2), NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE)) != null && NSDSNamespaces.NSDSDeviceState.DEVICECONFIG_IN_PROGRESS.equals(str)) {
                Log.i(LOG_TAG, "otherSimInProgress... pending device config");
                return true;
            }
        }
        return false;
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName());
        IMSLog.increaseIndent(str);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(str);
    }
}
