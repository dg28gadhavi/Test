package com.sec.internal.ims.entitlement.config;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.cmstore.MStoreDebugTool;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.MnoNsdsConfigStrategyCreator;
import com.sec.internal.ims.entitlement.nsds.NSDSSimEventManager;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.MigrationHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class EntitlementConfigService extends Service {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = EntitlementConfigService.class.getSimpleName();
    protected static Map<Integer, EntitlementConfigModuleBase> mModuleMap = new HashMap();
    protected boolean mConfigRcvRegistered = false;
    protected BroadcastReceiver mConfigReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String r7 = EntitlementConfigService.LOG_TAG;
            IMSLog.i(r7, "onReceive: " + intent.getAction());
            int intExtra = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
            String deviceId = DeviceIdHelper.getDeviceId(EntitlementConfigService.this.mContext, intExtra);
            if (NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED.equals(intent.getAction())) {
                boolean booleanExtra = intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.CHANGED_CONFIG, false);
                String entitlementServerUrl = NSDSSharedPrefHelper.getEntitlementServerUrl(EntitlementConfigService.this.mContext, deviceId, MStoreDebugTool.DEFAULT_PRO_ENTITLEMENT);
                SimpleEventLog r3 = EntitlementConfigService.this.mEventLog;
                r3.logAndAdd(intExtra, EntitlementConfigService.LOG_TAG + " URL : " + entitlementServerUrl);
                SimpleEventLog r6 = EntitlementConfigService.this.mEventLog;
                r6.logAndAdd(intExtra, EntitlementConfigService.LOG_TAG + " DEVICE_CONFIG_UPDATED result [" + intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false) + "],  errorcode [" + intent.getIntExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, 0) + "], changed [" + booleanExtra + "]");
                StringBuilder sb = new StringBuilder();
                sb.append("RESULT:");
                sb.append(intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false));
                sb.append(",ERRC:");
                sb.append(intent.getIntExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, 0));
                IMSLog.c(LogClass.ES_DC_INTENT_RESULT, sb.toString());
            } else if (EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG.equals(intent.getAction()) || EntitlementNamespaces.EntitlementActions.ACTION_RETRY_DEVICE_CONFIG.equals(intent.getAction())) {
                int intExtra2 = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
                if (EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG.equals(intent.getAction())) {
                    NSDSSharedPrefHelper.saveActionTrigger(EntitlementConfigService.this.mContext, deviceId, 3);
                }
                EntitlementConfigService.this.mServiceHandler.sendMessage(EntitlementConfigService.this.mServiceHandler.obtainMessage(107, Integer.valueOf(intExtra2)));
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext = null;
    private ConnectivityManager.NetworkCallback mDefaultNetworkCallback = null;
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog = null;
    private Messenger mMessenger;
    private NSDSDatabaseHelper mNSDSDatabaseHelper;
    /* access modifiers changed from: private */
    public ServiceHandler mServiceHandler;
    protected Looper mServiceLooper;
    protected boolean[] mSimEvtRegistered = new boolean[SimUtil.getPhoneCount()];

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            String r0 = EntitlementConfigService.LOG_TAG;
            IMSLog.i(r0, "handleMessage:" + message.what);
            int i = message.what;
            if (i != 100) {
                switch (i) {
                    case 106:
                        EntitlementConfigService.this.initEntitlementConfigService();
                        return;
                    case 107:
                        EntitlementConfigService.this.onDeviceReady(((Integer) message.obj).intValue());
                        return;
                    case 108:
                        EntitlementConfigService.this.forceConfigUpdate(((Integer) message.obj).intValue());
                        return;
                    default:
                        switch (i) {
                            case 200:
                                EntitlementConfigService.this.retriveAkaToken(((Integer) message.obj).intValue());
                                return;
                            case 201:
                                EntitlementConfigService.this.updateEntitlementUrl(message.getData());
                                return;
                            case 202:
                                EntitlementConfigService.this.actionTriggerForDeletingDB();
                                return;
                            default:
                                return;
                        }
                }
            } else {
                EntitlementConfigService.this.onEventSimReady((Bundle) message.obj);
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        String str = LOG_TAG;
        IMSLog.i(str, "onCreate");
        HandlerThread handlerThread = new HandlerThread("EntitlementConfigService", 10);
        handlerThread.start();
        this.mServiceLooper = handlerThread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
        this.mMessenger = new Messenger(this.mServiceHandler);
        Context applicationContext = getApplicationContext();
        this.mContext = applicationContext;
        this.mEventLog = new SimpleEventLog(applicationContext, str, 20);
        this.mNSDSDatabaseHelper = new NSDSDatabaseHelper(this.mContext);
        NSDSSimEventManager.createInstance(this.mServiceLooper, this);
        EntitlementConfigFactory.createInstance(this.mServiceLooper, this);
        try {
            Message message = new Message();
            message.what = 106;
            this.mMessenger.send(message);
        } catch (RemoteException unused) {
            IMSLog.i(LOG_TAG, "initialize failed");
        }
    }

    public IBinder onBind(Intent intent) {
        IMSLog.i(LOG_TAG, "onBind");
        return this.mMessenger.getBinder();
    }

    public void onDestroy() {
        IMSLog.i(LOG_TAG, "onDestroy");
        if (this.mMessenger != null) {
            NSDSSimEventManager.getInstance().unregisterSimEventMessenger(this.mMessenger);
        }
        unregisterConfigReceiver();
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, "onStartCommand");
        if (intent != null) {
            int intExtra = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
            if (!this.mSimEvtRegistered[intExtra]) {
                registerForSimEvents(intExtra);
            }
            IMSLog.i(str, "Received <" + intExtra + "> startId:" + i2 + " intent:" + intent);
            if (EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG.equals(intent.getAction()) || EntitlementNamespaces.EntitlementActions.ACTION_RETRY_DEVICE_CONFIG.equals(intent.getAction())) {
                int intExtra2 = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 0);
                int intExtra3 = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
                if (intExtra2 == 18) {
                    this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(108, Integer.valueOf(intExtra3)));
                } else if (intExtra2 != 19) {
                    this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(107, Integer.valueOf(intExtra3)));
                } else {
                    this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(200, Integer.valueOf(intExtra3)));
                }
            }
            return 1;
        }
        IMSLog.i(str, "handleIntent() - Intent is null. return....");
        return 1;
    }

    /* access modifiers changed from: private */
    public void initEntitlementConfigService() {
        IMSLog.i(LOG_TAG, "initEntitlementConfigService");
        registerConfigReceiver();
        this.mContext.getContentResolver().update(Uri.withAppendedPath(EntitlementConfigContract.AUTHORITY_URI, "binding_service"), new ContentValues(), (String) null, (String[]) null);
    }

    private void registerForSimEvents(int i) {
        NSDSSimEventManager.getInstance().registerSimEventMessenger(this.mMessenger, i);
        this.mSimEvtRegistered[i] = true;
    }

    /* access modifiers changed from: private */
    public void onEventSimReady(Bundle bundle) {
        int i = bundle.getInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
        boolean z = bundle.getBoolean(NSDSNamespaces.NSDSExtras.SIM_ABSENT, false);
        boolean z2 = bundle.getBoolean(NSDSNamespaces.NSDSExtras.SIM_SWAPPED, false);
        String str = LOG_TAG;
        IMSLog.i(str, i, "onEventSimReady: isSimAbsent " + z + " isSimSwapped " + z2);
        if (!z) {
            EntitlementConfigModuleBase addAndGetConfigModule = addAndGetConfigModule(i);
            MnoNsdsConfigStrategyCreator.updateMnoStrategy(this.mContext, i);
            resetDeviceConfigState();
            if (addAndGetConfigModule != null) {
                addAndGetConfigModule.onSimReady(z2);
                if (isDeviceReady()) {
                    addAndGetConfigModule.onDeviceReady();
                    return;
                }
                return;
            }
            IMSLog.e(str, "onEventSimReady: config module was not initiated");
        } else if (mModuleMap.get(Integer.valueOf(i)) != null) {
            IMSLog.i(str, "remove nsdsconfigmodule for " + i);
            mModuleMap.get(Integer.valueOf(i)).unregisterReceiver();
            mModuleMap.remove(Integer.valueOf(i));
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDeviceReady() {
        if (!NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            IMSLog.i(LOG_TAG, "isDeviceReady() User lock ");
            return false;
        }
        if (!MigrationHelper.checkMigrateDB(this.mContext)) {
            MigrationHelper.migrateDBToCe(this.mContext);
        }
        if (!NetworkUtil.isConnected(this.mContext)) {
            registerDefaultNetworkCallback();
            return false;
        }
        unregisterNetworkCallback();
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0024, code lost:
        r2 = com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper.getDeviceId(r7.mContext, r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void resetDeviceConfigState() {
        /*
            r7 = this;
            java.util.List r0 = com.sec.internal.ims.core.sim.SimManagerFactory.getAllSimManagers()
            java.util.Iterator r0 = r0.iterator()
        L_0x0008:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x005d
            java.lang.Object r1 = r0.next()
            com.sec.internal.interfaces.ims.core.ISimManager r1 = (com.sec.internal.interfaces.ims.core.ISimManager) r1
            int r1 = r1.getSimSlotIndex()
            java.util.Map<java.lang.Integer, com.sec.internal.ims.entitlement.config.EntitlementConfigModuleBase> r2 = mModuleMap
            java.lang.Integer r3 = java.lang.Integer.valueOf(r1)
            java.lang.Object r2 = r2.get(r3)
            if (r2 != 0) goto L_0x0008
            android.content.Context r2 = r7.mContext
            java.lang.String r2 = com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper.getDeviceId(r2, r1)
            android.content.Context r3 = r7.mContext
            java.lang.String r4 = "device_config_state"
            java.lang.String r3 = com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.get(r3, r2, r4)
            if (r3 == 0) goto L_0x0008
            java.lang.String r5 = "deviceconfig_in_progress"
            boolean r3 = r5.equals(r3)
            if (r3 == 0) goto L_0x0008
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "["
            r5.append(r6)
            r5.append(r1)
            java.lang.String r1 = "] reset... device config state"
            r5.append(r1)
            java.lang.String r1 = r5.toString()
            com.sec.internal.log.IMSLog.i(r3, r1)
            android.content.Context r1 = r7.mContext
            com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.remove(r1, r2, r4)
            goto L_0x0008
        L_0x005d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.config.EntitlementConfigService.resetDeviceConfigState():void");
    }

    private ConnectivityManager.NetworkCallback getDefaultNetworkCallback() {
        return new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                IMSLog.i(EntitlementConfigService.LOG_TAG, "onAvailable");
                if (network != null && EntitlementConfigService.this.isDeviceReady()) {
                    EntitlementConfigService.this.onDeviceReady();
                }
            }

            public void onLost(Network network) {
                IMSLog.i(EntitlementConfigService.LOG_TAG, "onLost");
            }
        };
    }

    private void registerDefaultNetworkCallback() {
        ConnectivityManager connectivityManager;
        if (this.mDefaultNetworkCallback == null && (connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity")) != null) {
            IMSLog.i(LOG_TAG, "registerDefaultNetworkCallback");
            ConnectivityManager.NetworkCallback defaultNetworkCallback = getDefaultNetworkCallback();
            this.mDefaultNetworkCallback = defaultNetworkCallback;
            connectivityManager.registerDefaultNetworkCallback(defaultNetworkCallback);
        }
    }

    private void unregisterNetworkCallback() {
        ConnectivityManager connectivityManager;
        if (this.mDefaultNetworkCallback != null && (connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity")) != null) {
            IMSLog.i(LOG_TAG, "unregisterNetworkCallback");
            connectivityManager.unregisterNetworkCallback(this.mDefaultNetworkCallback);
            this.mDefaultNetworkCallback = null;
        }
    }

    private void registerConfigReceiver() {
        if (!this.mConfigRcvRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED);
            intentFilter.addAction(EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG);
            intentFilter.addAction(EntitlementNamespaces.EntitlementActions.ACTION_RETRY_DEVICE_CONFIG);
            registerReceiver(this.mConfigReceiver, intentFilter);
            this.mConfigRcvRegistered = true;
        }
    }

    private void unregisterConfigReceiver() {
        if (this.mConfigRcvRegistered) {
            unregisterReceiver(this.mConfigReceiver);
            this.mConfigReceiver = null;
            this.mConfigRcvRegistered = false;
        }
    }

    private EntitlementConfigModuleBase addAndGetConfigModule(int i) {
        EntitlementConfigModuleBase entitlementConfigModuleBase;
        EntitlementConfigModuleBase entitlementConfigModuleBase2 = mModuleMap.get(Integer.valueOf(i));
        if (entitlementConfigModuleBase2 == null) {
            if (EntitlementConfigFactory.getInstance() == null) {
                entitlementConfigModuleBase = null;
            } else {
                entitlementConfigModuleBase = EntitlementConfigFactory.getInstance().getDeviceConfigModule(getSimManager(i));
            }
            entitlementConfigModuleBase2 = entitlementConfigModuleBase;
            if (entitlementConfigModuleBase2 != null) {
                IMSLog.i(LOG_TAG, i, "addAndGetConfigModule: added for phoneId ");
                mModuleMap.put(Integer.valueOf(i), entitlementConfigModuleBase2);
            }
        }
        return entitlementConfigModuleBase2;
    }

    private ISimManager getSimManager(int i) {
        return NSDSSimEventManager.getInstance().getSimManagerFromSimSlot(i);
    }

    /* access modifiers changed from: private */
    public void onDeviceReady(int i) {
        IMSLog.i(LOG_TAG, i, "onDeviceReady");
        EntitlementConfigModuleBase entitlementConfigModuleBase = mModuleMap.get(Integer.valueOf(i));
        if (entitlementConfigModuleBase != null) {
            entitlementConfigModuleBase.onDeviceReady();
        }
    }

    /* access modifiers changed from: private */
    public void onDeviceReady() {
        IMSLog.i(LOG_TAG, "onDeviceReady");
        for (EntitlementConfigModuleBase onDeviceReady : mModuleMap.values()) {
            onDeviceReady.onDeviceReady();
        }
    }

    /* access modifiers changed from: private */
    public void forceConfigUpdate(int i) {
        EntitlementConfigModuleBase entitlementConfigModuleBase = mModuleMap.get(Integer.valueOf(i));
        if (entitlementConfigModuleBase == null) {
            IMSLog.e(LOG_TAG, i, "configModule is null");
        } else if (entitlementConfigModuleBase instanceof NSDSConfigModule) {
            entitlementConfigModuleBase.forceConfigUpdate();
        } else {
            IMSLog.i(LOG_TAG, "check why config module is not instance of NSDSConfigModule");
        }
    }

    /* access modifiers changed from: private */
    public void retriveAkaToken(int i) {
        EntitlementConfigModuleBase entitlementConfigModuleBase = mModuleMap.get(Integer.valueOf(i));
        if (entitlementConfigModuleBase == null) {
            IMSLog.e(LOG_TAG, i, "configModule is null");
        } else if (entitlementConfigModuleBase instanceof NSDSConfigModule) {
            entitlementConfigModuleBase.retriveAkaToken();
        } else {
            IMSLog.i(LOG_TAG, "check why config module is not instance of NSDSConfigModule");
        }
    }

    public static void updateTelephonyCallStatus(int i, int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, "updateTelephonyCallStatus : " + i2 + " for " + i);
        for (int i3 = 0; i3 < SimUtil.getPhoneCount(); i3++) {
            EntitlementConfigModuleBase entitlementConfigModuleBase = mModuleMap.get(Integer.valueOf(i3));
            if (entitlementConfigModuleBase == null) {
                IMSLog.e(LOG_TAG, i3, "configModule is null");
            } else if (entitlementConfigModuleBase instanceof NSDSConfigModule) {
                entitlementConfigModuleBase.updateTelephonyCallStatus(i, i2);
            } else {
                IMSLog.i(LOG_TAG, "check why config module is not instance of NSDSConfigModule");
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateEntitlementUrl(Bundle bundle) {
        String string = bundle.getString("URL");
        String str = LOG_TAG;
        IMSLog.i(str, "updateEntitlementUrl: url " + string);
        if (!TextUtils.isEmpty(string)) {
            int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
            String subscriberId = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getSubscriberId(SimUtil.getSubId());
            String deviceId = DeviceIdHelper.getDeviceId(this.mContext, activeDataPhoneId);
            this.mContext.getContentResolver().delete(EntitlementConfigContract.DeviceConfig.CONTENT_URI, (String) null, (String[]) null);
            this.mNSDSDatabaseHelper.deleteConfigAndResetDeviceAndAccountStatus(deviceId, subscriberId, activeDataPhoneId);
            NSDSSharedPrefHelper.saveActionTrigger(this.mContext, deviceId, 6);
            NSDSSharedPrefHelper.setEntitlementServerUrl(this.mContext, deviceId, string);
        }
    }

    public static void startEntitlementConfigService(Context context, int i) {
        IMSLog.i(LOG_TAG, "startEntitlementConfigService()");
        Intent intent = new Intent(context, EntitlementConfigService.class);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, i);
        context.startService(intent);
    }

    public void actionTriggerForDeletingDB() {
        String deviceId = DeviceIdHelper.getDeviceId(this.mContext, SimUtil.getActiveDataPhoneId());
        int intInDe = NSDSSharedPrefHelper.getIntInDe(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF_CONFIG, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_ACTION_TRIGGER);
        String str = LOG_TAG;
        IMSLog.i(str, "actionTriggerForDeletingDB (saved) : " + intInDe);
        if (intInDe != 2 && intInDe != 6) {
            NSDSSharedPrefHelper.saveActionTrigger(this.mContext, deviceId, 5);
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        IMSLog.prepareDump(this.mContext, printWriter);
        for (EntitlementConfigModuleBase dump : mModuleMap.values()) {
            dump.dump();
        }
        this.mEventLog.dump();
        IMSLog.postDump(printWriter);
    }
}
