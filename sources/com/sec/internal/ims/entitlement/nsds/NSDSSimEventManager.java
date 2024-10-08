package com.sec.internal.ims.entitlement.nsds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.entitlement.config.EntitlementConfigService;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.MnoNsdsConfigStrategyCreator;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.MigrationHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.EntFeatureDetector;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NSDSSimEventManager extends Handler {
    private static final int EVENT_SIMMOBILITY_CHANGED = 2;
    private static final int EVENT_SIM_SUBSCRIBE_ID_CHANGED = 1;
    private static final int EVT_DEVICE_READY = 10;
    private static final int EVT_SIM_READY = 0;
    private static final int EVT_SIM_REFRESH = 3;
    private static final String LOG_TAG = "NSDSSimEventManager";
    public static final int NOTIFY_SIM_READY = 100;
    private static NSDSSimEventManager mInstance;
    private static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public static final UriMatcher sUriMatcher;
    protected ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z, Uri uri) {
            IMSLog.i(NSDSSimEventManager.LOG_TAG, "Uri changed:" + uri);
            int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
            if (uri.getFragment() != null && uri.getFragment().contains("simslot")) {
                activeDataPhoneId = Character.getNumericValue(uri.getFragment().charAt(7));
                IMSLog.i(NSDSSimEventManager.LOG_TAG, "query : Exist simslot on uri: " + activeDataPhoneId);
            }
            if (NSDSSimEventManager.sUriMatcher.match(uri) == 2) {
                NSDSSimEventManager.this.onSimMobilityChanged(activeDataPhoneId);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    protected BroadcastReceiver mDeviceReadyReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            IMSLog.i(NSDSSimEventManager.LOG_TAG, "DeviceReadyReceiver: " + intent.getAction());
            if (NSDSSimEventManager.this.isDeviceReady()) {
                for (ISimManager next : NSDSSimEventManager.this.mSimManagers) {
                    DeviceIdHelper.makeDeviceId(NSDSSimEventManager.this.mContext, next.getSimSlotIndex());
                    NSDSSimEventManager.this.onEventSimReady(next.getSimSlotIndex(), 0);
                }
                NSDSSimEventManager nSDSSimEventManager = NSDSSimEventManager.this;
                nSDSSimEventManager.sendMessage(nSDSSimEventManager.obtainMessage(10));
            }
        }
    };
    private List<Messenger> mSimEvtMessengers = new ArrayList();
    private boolean mSimEvtRegistered = false;
    protected List<ISimManager> mSimManagers = new ArrayList();
    protected Map<Integer, Boolean> mSimMobilitystatus = new HashMap();
    protected boolean notifySimReadyAlreadyDone = false;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        ImsConstants.SystemSettings.SettingsItem settingsItem = ImsConstants.SystemSettings.IMS_SIM_MOBILITY;
        uriMatcher.addURI(settingsItem.getAuthority(), settingsItem.getPath(), 2);
    }

    public NSDSSimEventManager(Context context, Looper looper) {
        super(looper);
        this.mContext = context;
        initSimManagers();
        registerContentObserver();
        registerDeviceReadyReceiver();
    }

    public static NSDSSimEventManager getInstance() {
        return mInstance;
    }

    public static NSDSSimEventManager createInstance(Looper looper, Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new NSDSSimEventManager(context, looper);
            }
        }
        return mInstance;
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.IMS_SIM_MOBILITY.getUri(), false, this.mContentObserver);
    }

    public ISimManager getSimManager(String str) {
        for (ISimManager next : this.mSimManagers) {
            if (str.equals(next.getImsi())) {
                return next;
            }
        }
        return null;
    }

    public ISimManager getSimManagerFromSimSlot(int i) {
        for (ISimManager next : this.mSimManagers) {
            if (next.getSimSlotIndex() == i) {
                return next;
            }
        }
        IMSLog.i(LOG_TAG, "ISimManager[" + i + "] is not exist. Return null..");
        return null;
    }

    public void registerSimEventMessenger(Messenger messenger, int i) {
        synchronized (mLock) {
            if (messenger == null) {
                IMSLog.e(LOG_TAG, "registerSimEventMessenger: null messenger");
                return;
            }
            IMSLog.i(LOG_TAG, "registerSimEventMessenger size: " + this.mSimEvtMessengers.size());
            if (!this.mSimEvtMessengers.contains(messenger)) {
                this.mSimEvtMessengers.add(messenger);
            }
            notifyLazySimReady(messenger, i);
        }
    }

    public void unregisterSimEventMessenger(Messenger messenger) {
        synchronized (mLock) {
            if (messenger == null) {
                IMSLog.e(LOG_TAG, "unregisterSimEventMessenger: messenger null");
                return;
            }
            IMSLog.i(LOG_TAG, "unregisterSimEventMessenger: " + this.mSimEvtMessengers.size());
            this.mSimEvtMessengers.remove(messenger);
        }
    }

    private void registerDeviceReadyReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NSDSNamespaces.NSDSActions.DEVICE_READY_AFTER_BOOTUP);
        IMSLog.i(LOG_TAG, "registerDeviceReadyReceiver");
        this.mContext.registerReceiver(this.mDeviceReadyReceiver, intentFilter);
    }

    private void unregisterDeviceReadyReceiver() {
        try {
            IMSLog.i(LOG_TAG, "unregisterDeviceReadyReceiver");
            this.mContext.unregisterReceiver(this.mDeviceReadyReceiver);
        } catch (IllegalArgumentException e) {
            IMSLog.e(LOG_TAG, "unregisterDeviceReadyReceiver: " + e.getMessage());
        }
    }

    public void handleMessage(Message message) {
        IMSLog.i(LOG_TAG, "handleMessage:" + message.what);
        int i = message.what;
        if (i != 0) {
            if (i == 1) {
                onSimSubscribeIdChanged((SubscriptionInfo) ((AsyncResult) message.obj).result);
                return;
            } else if (i != 3) {
                if (i == 10) {
                    unregisterDeviceReadyReceiver();
                    return;
                }
                return;
            }
        }
        AsyncResult asyncResult = (AsyncResult) message.obj;
        DeviceIdHelper.makeDeviceId(this.mContext, ((Integer) asyncResult.result).intValue());
        onEventSimReady(((Integer) asyncResult.result).intValue(), i);
    }

    private void initSimManagers() {
        this.mSimManagers.clear();
        this.mSimManagers.addAll(SimManagerFactory.getAllSimManagers());
        for (ISimManager simSlotIndex : this.mSimManagers) {
            this.mSimMobilitystatus.put(Integer.valueOf(simSlotIndex.getSimSlotIndex()), Boolean.FALSE);
        }
        if (!this.mSimEvtRegistered) {
            registerForSimEvents();
        }
    }

    /* access modifiers changed from: private */
    public void onEventSimReady(int i, int i2) {
        ISimManager simManagerFromSimSlot;
        IMSLog.i(LOG_TAG, i, "onEventSimReady:");
        if (isDeviceReady() && (simManagerFromSimSlot = getSimManagerFromSimSlot(i)) != null) {
            boolean z = false;
            if ((i < 0 || simManagerFromSimSlot.hasNoSim()) || simManagerFromSimSlot.hasVsim()) {
                z = true;
            }
            notifySimReady(z, i, i2);
        }
    }

    private void registerForSimEvents() {
        for (ISimManager next : this.mSimManagers) {
            next.registerForSimReady(this, 0, (Object) null);
            next.registerForSimRefresh(this, 3, (Object) null);
            next.registerForSimRemoved(this, 3, (Object) null);
        }
        SimManagerFactory.registerForSubIdChange(this, 1, (Object) null);
        this.mSimEvtRegistered = true;
    }

    private void onSimSubscribeIdChanged(SubscriptionInfo subscriptionInfo) {
        int simSlotIndex = subscriptionInfo.getSimSlotIndex();
        IMSLog.i(LOG_TAG, simSlotIndex, "onSimSubscribeIdChanged, subId: " + subscriptionInfo.getSubscriptionId());
        for (ISimManager next : this.mSimManagers) {
            if (next.getSimSlotIndex() == subscriptionInfo.getSimSlotIndex()) {
                next.setSubscriptionInfo(subscriptionInfo);
            }
        }
    }

    private void notifySimReady(boolean z, int i, int i2) {
        IMSLog.i(LOG_TAG, "notifySimReady, isSimAbsent: " + z);
        String deviceId = DeviceIdHelper.getDeviceId(this.mContext, i);
        boolean isSimSwapped = isSimSwapped(i);
        IMSLog.i(LOG_TAG, i, " isSimSwapped:" + isSimSwapped);
        IMSLog.c(LogClass.ES_CHECK_SIMSWAP, i + ",SIMSWAP:" + isSimSwapped);
        if (isSimSwapped) {
            NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, deviceId);
            MnoNsdsStrategyCreator.resetMnoStrategy();
        }
        MnoNsdsConfigStrategyCreator.updateMnoStrategy(this.mContext, i);
        this.notifySimReadyAlreadyDone = i2 == 0;
        notifyMessengerSimReady(getSimManagerFromSimSlot(i));
    }

    private void notifyLazySimReady(Messenger messenger, int i) {
        ISimManager simManagerFromSimSlot = getSimManagerFromSimSlot(i);
        IMSLog.i(LOG_TAG, "notifyLazySimReady : notifySimReadyAlreadyDone " + this.notifySimReadyAlreadyDone);
        if (this.notifySimReadyAlreadyDone && simManagerFromSimSlot != null && simManagerFromSimSlot.isSimAvailable()) {
            try {
                messenger.send(obtainSimReadyMessage(simManagerFromSimSlot));
            } catch (RemoteException e) {
                IMSLog.e(LOG_TAG, "notifyLazySimReady: " + e.getMessage());
                this.mSimEvtMessengers.remove(messenger);
            }
        }
    }

    private boolean isSimSwapped(int i) {
        String str;
        ISimManager simManagerFromSimSlot = getSimManagerFromSimSlot(i);
        String prefForSlot = NSDSSharedPrefHelper.getPrefForSlot(this.mContext, i, "imsi");
        if (simManagerFromSimSlot == null) {
            str = null;
        } else {
            str = simManagerFromSimSlot.getImsi();
        }
        NSDSSharedPrefHelper.savePrefForSlot(this.mContext, i, NSDSNamespaces.NSDSSharedPref.PREF_PREV_IMSI, prefForSlot);
        NSDSSharedPrefHelper.savePrefForSlot(this.mContext, i, "imsi", str);
        if (TextUtils.isEmpty(prefForSlot) || prefForSlot.equals(str)) {
            return isSimSwapPending(i);
        }
        Context context = this.mContext;
        NSDSSharedPrefHelper.save(context, DeviceIdHelper.getDeviceId(context, i), NSDSNamespaces.NSDSSharedPref.PREF_PEDNING_SIM_SWAP, true);
        return true;
    }

    private boolean isSimSwapPending(int i) {
        return NSDSSharedPrefHelper.isSimSwapPending(this.mContext, DeviceIdHelper.getDeviceId(this.mContext, i));
    }

    private void notifyMessengerSimReady(ISimManager iSimManager) {
        synchronized (mLock) {
            for (int size = this.mSimEvtMessengers.size() - 1; size >= 0; size--) {
                try {
                    this.mSimEvtMessengers.get(size).send(obtainSimReadyMessage(iSimManager));
                } catch (RemoteException e) {
                    IMSLog.e(LOG_TAG, "notifyMessengerSimReady: dead messenger, removed" + e.getMessage());
                    this.mSimEvtMessengers.remove(size);
                }
            }
            IMSLog.i(LOG_TAG, "notifyMessengerSimReady: notified");
        }
    }

    private Bundle getSimEvtBundle(ISimManager iSimManager) {
        int simSlotIndex = iSimManager.getSimSlotIndex();
        IMSLog.i(LOG_TAG, "getSimEvtBundle: phoneId " + simSlotIndex);
        boolean z = simSlotIndex < 0 || iSimManager.hasNoSim() || iSimManager.hasVsim();
        Bundle bundle = new Bundle();
        bundle.putInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, simSlotIndex);
        bundle.putBoolean(NSDSNamespaces.NSDSExtras.SIM_ABSENT, z);
        bundle.putBoolean(NSDSNamespaces.NSDSExtras.SIM_SWAPPED, isSimSwapPending(simSlotIndex));
        return bundle;
    }

    private Message obtainSimReadyMessage(ISimManager iSimManager) {
        Message message = new Message();
        message.what = 100;
        message.obj = getSimEvtBundle(iSimManager);
        return message;
    }

    public static void startIMSDeviceConfigService(Context context, ISimManager iSimManager) {
        boolean z;
        boolean z2;
        int simSlotIndex = iSimManager.getSimSlotIndex();
        Mno simMno = iSimManager.getSimMno();
        String simMnoName = iSimManager.getSimMnoName();
        IMSLog.i(LOG_TAG, simSlotIndex, "startIMSDeviceConfigService : check CSC , SimMnoName: " + simMnoName);
        IMSLog.c(LogClass.ES_START_MNO, simSlotIndex + ",START:" + simMnoName);
        boolean z3 = true;
        boolean z4 = false;
        if (EntFeatureDetector.checkVSimFeatureEnabled("Nsds", simSlotIndex)) {
            z2 = true;
        } else if (EntFeatureDetector.checkVSimFeatureEnabled("Nsdsconfig", simSlotIndex)) {
            z2 = true;
            z = false;
            if (z2 || z) {
                if (simMno == Mno.TMOUS && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 33) {
                    String gid1 = iSimManager.getGid1();
                    if (TextUtils.isEmpty(gid1) || !NSDSConfigHelper.getAllowedGid(simSlotIndex).contains(gid1)) {
                        IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : ConfigService is disabled. Don't allow Gid");
                    }
                }
                if (simMno == Mno.TMOUS && z2) {
                    String str = SemSystemProperties.get("ro.simbased.changetype", "");
                    if (!Mno.Country.US.getCountryIso().equalsIgnoreCase(SemSystemProperties.getCountryIso()) || (str.contains("SED") && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) < 29)) {
                        IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : ConfigService is disabled");
                        z2 = false;
                        z = false;
                    }
                }
                if (!ImsUtil.isSimMobilityActivated(simSlotIndex)) {
                    if (DmConfigHelper.getImsSwitchValue(context, DeviceConfigManager.IMS, simSlotIndex) == 1) {
                        if (!(DmConfigHelper.getImsSwitchValue(context, "volte", simSlotIndex) == 1)) {
                            if (simMno == Mno.ATT) {
                                z = false;
                            } else {
                                z4 = z2;
                            }
                            IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : Nsds is disabled");
                        } else {
                            z4 = z2;
                        }
                    } else {
                        IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : IMS is disabled");
                        z = false;
                    }
                    z2 = (simMno != Mno.TMOUS || !DeviceUtil.isTablet() || !DeviceUtil.isSupport5G(context)) ? z4 : true;
                }
            }
            if (simMno == Mno.GCI) {
                IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : GCI");
                z = true;
            } else {
                z3 = z2;
            }
            IMSLog.i(LOG_TAG, simSlotIndex, "startIMSDeviceConfigService : ConfigService [" + z3 + "], Nsds[" + z + "]");
            IMSLog.c(LogClass.ES_START_SERVICE, simSlotIndex + ",DC:" + z3 + ",NSDS:" + z);
            if (z3 || z) {
                if (z3) {
                    EntitlementConfigService.startEntitlementConfigService(context, simSlotIndex);
                }
                if (z) {
                    NSDSMultiSimService.startNsdsMultiSimService(context, simSlotIndex);
                    return;
                }
                return;
            }
            return;
        } else {
            z2 = false;
        }
        z = z2;
        String gid12 = iSimManager.getGid1();
        IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : ConfigService is disabled. Don't allow Gid");
    }

    /* access modifiers changed from: private */
    public void onSimMobilityChanged(int i) {
        boolean z;
        ISimManager simManagerFromSimSlot = getSimManagerFromSimSlot(i);
        Iterator<ImsProfile> it = SlotBasedConfig.getInstance(i).getProfiles().iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().getSimMobility()) {
                    z = true;
                    break;
                }
            } else {
                z = false;
                break;
            }
        }
        if (simManagerFromSimSlot != null && z != this.mSimMobilitystatus.get(Integer.valueOf(i)).booleanValue()) {
            IMSLog.i(LOG_TAG, i, "onSimMobilityChanged to " + z + " : Start again entitlement service");
            this.mSimMobilitystatus.put(Integer.valueOf(i), Boolean.valueOf(z));
            startIMSDeviceConfigService(this.mContext, simManagerFromSimSlot);
        }
    }

    /* access modifiers changed from: private */
    public boolean isDeviceReady() {
        if (!NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            IMSLog.i(LOG_TAG, "isDeviceReady() User lock ");
            return false;
        } else if (MigrationHelper.checkMigrateDB(this.mContext)) {
            return true;
        } else {
            MigrationHelper.migrateDBToCe(this.mContext);
            return true;
        }
    }
}
