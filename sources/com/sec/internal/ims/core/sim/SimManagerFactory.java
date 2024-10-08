package com.sec.internal.ims.core.sim;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimManagerFactory {
    private static final String LOG_TAG = "SimManagerFactory";
    public static final int PHONE_ID_NON_EXISTING = -1;
    /* access modifiers changed from: private */
    public static RegistrantList mADSChangeRegistrants = new RegistrantList();
    private static Context mContext;
    private static boolean mCreated = false;
    /* access modifiers changed from: private */
    public static int mDefaultSimSubId = 0;
    /* access modifiers changed from: private */
    public static boolean mIsMultiSimSupported = false;
    private static Looper mLooper;
    /* access modifiers changed from: private */
    public static RegistrantList mSubIdChangeRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public static SubscriptionManager mSubMan;
    private static TelephonyCallbackListener mTelephonyCallbackListener = new TelephonyCallbackListener();
    /* access modifiers changed from: private */
    public static volatile List<SimManager> sSimManagerList = new CopyOnWriteArrayList();

    public static void createInstance(Looper looper, Context context) {
        if (!mCreated) {
            mContext = context;
            mLooper = looper;
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
            mSubMan = subscriptionManager;
            SimUtil.setSubMgr(subscriptionManager);
            int phoneCount = TelephonyManagerWrapper.getInstance(mContext).getPhoneCount();
            SimUtil.setPhoneCount(phoneCount);
            Log.i(LOG_TAG, "maxSimCount=" + phoneCount);
            mIsMultiSimSupported = phoneCount > 1;
            mDefaultSimSubId = SubscriptionManager.getActiveDataSubscriptionId();
            Log.i(LOG_TAG, "Current default subId=" + mDefaultSimSubId);
            Log.i(LOG_TAG, "getConfigDualIMS = " + SimUtil.getConfigDualIMS());
            for (int i = 0; i < phoneCount; i++) {
                sSimManagerList.add(new SimManager(mLooper, mContext, i, mSubMan.getActiveSubscriptionInfoForSimSlotIndex(i), TelephonyManagerWrapper.getInstance(context)));
            }
            mCreated = true;
        }
    }

    public static void initInstances() {
        for (SimManager initializeSimState : sSimManagerList) {
            initializeSimState.initializeSimState();
        }
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (telephonyManager == null) {
            Log.i(LOG_TAG, "TelephonyManager is null, should not happen");
        } else {
            telephonyManager.registerTelephonyCallback(mContext.getMainExecutor(), mTelephonyCallbackListener);
        }
        mSubMan.addOnSubscriptionsChangedListener(new SubscriptionManager.OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                List<SubscriptionInfo> activeSubscriptionInfoList = SimManagerFactory.mSubMan.getActiveSubscriptionInfoList();
                if (activeSubscriptionInfoList == null) {
                    Log.e(SimManagerFactory.LOG_TAG, "subInfoList is null");
                    return;
                }
                boolean z = false;
                for (SubscriptionInfo next : activeSubscriptionInfoList) {
                    Log.i(SimManagerFactory.LOG_TAG, "onSubscriptionsChanged: subInfo=" + next);
                    for (ISimManager iSimManager : SimManagerFactory.sSimManagerList) {
                        if (iSimManager.getSimSlotIndex() == next.getSimSlotIndex()) {
                            if (iSimManager.getSubscriptionId() != next.getSubscriptionId()) {
                                iSimManager.setSubscriptionInfo(next);
                                z = true;
                            } else {
                                Log.i(SimManagerFactory.LOG_TAG, "Do not notify: SubId is not changed.");
                            }
                        }
                    }
                    if (z) {
                        SimManagerFactory.mSubIdChangeRegistrants.notifyResult(next);
                    }
                }
                SimManagerFactory.updateAdsSlot();
            }
        });
    }

    public static List<? extends ISimManager> getAllSimManagers() {
        return sSimManagerList;
    }

    public static synchronized ISimManager getSimManager() {
        synchronized (SimManagerFactory.class) {
            for (ISimManager next : sSimManagerList) {
                if (next.getSimSlotIndex() == SimUtil.getActiveDataPhoneId()) {
                    return next;
                }
            }
            if (!sSimManagerList.isEmpty()) {
                Log.e(LOG_TAG, "Not matched. Return slot 0's.");
                ISimManager iSimManager = sSimManagerList.get(0);
                return iSimManager;
            }
            Log.e(LOG_TAG, "SimManager is not yet initiated!");
            return null;
        }
    }

    public static synchronized ISimManager getSimManagerFromSimSlot(int i) {
        synchronized (SimManagerFactory.class) {
            for (ISimManager next : sSimManagerList) {
                if (next.getSimSlotIndex() == i) {
                    return next;
                }
            }
            IMSLog.i(LOG_TAG, i, "getSimManagerFromSimSlot, No matched ISimManager. Return null..");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized com.sec.internal.interfaces.ims.core.ISimManager getSimManagerFromSubId(int r4) {
        /*
            java.lang.Class<com.sec.internal.ims.core.sim.SimManagerFactory> r0 = com.sec.internal.ims.core.sim.SimManagerFactory.class
            monitor-enter(r0)
            java.util.List<com.sec.internal.ims.core.sim.SimManager> r1 = sSimManagerList     // Catch:{ all -> 0x002f }
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x002f }
        L_0x0009:
            boolean r2 = r1.hasNext()     // Catch:{ all -> 0x002f }
            if (r2 == 0) goto L_0x0025
            java.lang.Object r2 = r1.next()     // Catch:{ all -> 0x002f }
            com.sec.internal.interfaces.ims.core.ISimManager r2 = (com.sec.internal.interfaces.ims.core.ISimManager) r2     // Catch:{ all -> 0x002f }
            int r3 = r2.getSubscriptionId()     // Catch:{ all -> 0x002f }
            if (r3 != r4) goto L_0x0009
            if (r4 < 0) goto L_0x0023
            boolean r3 = r2.hasNoSim()     // Catch:{ all -> 0x002f }
            if (r3 != 0) goto L_0x0009
        L_0x0023:
            monitor-exit(r0)
            return r2
        L_0x0025:
            java.lang.String r1 = "SimManagerFactory"
            java.lang.String r2 = "getSimManagerFromSubId, No matched ISimManager. Return null.."
            com.sec.internal.log.IMSLog.i(r1, r4, r2)     // Catch:{ all -> 0x002f }
            monitor-exit(r0)
            r4 = 0
            return r4
        L_0x002f:
            r4 = move-exception
            monitor-exit(r0)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSubId(int):com.sec.internal.interfaces.ims.core.ISimManager");
    }

    public static void registerForADSChange(Handler handler, int i, Object obj) {
        mADSChangeRegistrants.add(new Registrant(handler, i, obj));
    }

    public static void registerForSubIdChange(Handler handler, int i, Object obj) {
        mSubIdChangeRegistrants.add(new Registrant(handler, i, obj));
    }

    public static void unregisterForADSChange(Handler handler) {
        mADSChangeRegistrants.remove(handler);
    }

    private static class TelephonyCallbackListener extends TelephonyCallback implements TelephonyCallback.ActiveDataSubscriptionIdListener {
        private TelephonyCallbackListener() {
        }

        public void onActiveDataSubscriptionIdChanged(int i) {
            int slotIndex = SubscriptionManager.getSlotIndex(i);
            Log.i(SimManagerFactory.LOG_TAG, "onActiveDataSubscriptionIdChanged subId=" + i + ", slot=" + slotIndex);
            int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
            if (!SimUtil.isValidSimSlot(slotIndex) || i < 0 || (activeDataPhoneId == slotIndex && SimManagerFactory.mDefaultSimSubId == i)) {
                Log.i(SimManagerFactory.LOG_TAG, "Current default subId=" + SimManagerFactory.mDefaultSimSubId + " slot=" + activeDataPhoneId);
                return;
            }
            SimUtil.setActiveDataPhoneId(slotIndex);
            SimManagerFactory.mDefaultSimSubId = i;
            if (SimManagerFactory.mIsMultiSimSupported) {
                Log.i(SimManagerFactory.LOG_TAG, "notify ADS changed " + i);
                SimManagerFactory.mADSChangeRegistrants.notifyRegistrants();
            }
        }
    }

    public static void dump() {
        IMSLog.dump(LOG_TAG, "Dump of SimManagerFactory:");
        for (SimManager dump : sSimManagerList) {
            dump.dump();
        }
    }

    public static void notifySubscriptionIdChanged(SubscriptionInfo subscriptionInfo) {
        mSubIdChangeRegistrants.notifyResult(subscriptionInfo);
    }

    public static int getPhoneId(String str) {
        for (ISimManager next : sSimManagerList) {
            if (next.isSimLoaded() && next.getImsi().equals(str)) {
                return next.getSimSlotIndex();
            }
        }
        return -1;
    }

    public static String getImsiFromPhoneId(int i) {
        ISimManager simManagerFromSimSlot = getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null || !simManagerFromSimSlot.isSimLoaded()) {
            return null;
        }
        return simManagerFromSimSlot.getImsi();
    }

    public static boolean isOutboundSim(int i) {
        ISimManager simManagerFromSimSlot = getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot != null) {
            return simManagerFromSimSlot.isOutBoundSIM();
        }
        Log.i(LOG_TAG, "isOutboundSim, sm is null");
        return false;
    }

    public static int getSlotId(int i) {
        if (i < 0) {
            IMSLog.e(LOG_TAG, i, "subId is wrong");
            return -1;
        }
        ISimManager simManagerFromSubId = getSimManagerFromSubId(i);
        if (simManagerFromSubId != null) {
            return simManagerFromSubId.getSimSlotIndex();
        }
        Log.e(LOG_TAG, "Simmanager is not created yet");
        return -1;
    }

    public static void updateAdsSlot() {
        int activeDataSubscriptionId = SubscriptionManager.getActiveDataSubscriptionId();
        int slotIndex = SubscriptionManager.getSlotIndex(activeDataSubscriptionId);
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        Log.i(LOG_TAG, "updateAdsSlot: subId:" + activeDataSubscriptionId + " phoneId:" + slotIndex + " curActiveDataPhone:" + activeDataPhoneId);
        if (SimUtil.isValidSimSlot(slotIndex) && activeDataSubscriptionId >= 0) {
            if (activeDataPhoneId != slotIndex || mDefaultSimSubId != activeDataSubscriptionId) {
                Log.i(LOG_TAG, "updateAdsSlot: Data subscription changed: subId=" + activeDataSubscriptionId + ", slot=" + slotIndex);
                SimUtil.setActiveDataPhoneId(slotIndex);
                mDefaultSimSubId = activeDataSubscriptionId;
                ISimManager simManagerFromSimSlot = getSimManagerFromSimSlot(slotIndex);
                if (simManagerFromSimSlot == null) {
                    Log.i(LOG_TAG, "SimManagerMainInstance is not exist. Do not notify.");
                    return;
                }
                simManagerFromSimSlot.notifyADSChanged(slotIndex);
                if (mIsMultiSimSupported) {
                    mADSChangeRegistrants.notifyRegistrants();
                }
            }
        }
    }
}
