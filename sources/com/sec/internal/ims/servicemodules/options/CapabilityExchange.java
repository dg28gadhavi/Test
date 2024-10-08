package com.sec.internal.ims.servicemodules.options;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CapabilityExchange {
    private static final String LOG_TAG = "CapabilityExchange";
    protected static final int MAX_PARTIAL_POLL_LIST = 2000;
    protected static final int POLL_LIMIT = 1000;
    protected static final int POLL_REMOVE_LIMIT = 100;
    private static long mPollingPartialCount;
    private CapabilityDiscoveryModule mCapabilityDiscovery;
    private CapabilityUtil mCapabilityUtil;
    private SimpleEventLog mEventLog;
    protected int room = 0;

    private enum Result {
        FALSE,
        TRUE,
        PROCEED
    }

    CapabilityExchange(CapabilityDiscoveryModule capabilityDiscoveryModule, CapabilityUtil capabilityUtil, SimpleEventLog simpleEventLog) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mEventLog = simpleEventLog;
    }

    /* access modifiers changed from: package-private */
    public void poll(Context context, boolean z, boolean z2, int i, Map<Integer, ImsRegistration> map, List<Date> list) {
        boolean z3;
        boolean z4 = z;
        boolean z5 = z2;
        int i2 = i;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i2, "poll: isPeriodic = " + z4 + ", isForce = " + z5 + ", " + this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).size() + " contacts");
        StringBuilder sb = new StringBuilder();
        sb.append(i2);
        sb.append(",");
        sb.append(z4);
        sb.append(",");
        sb.append(this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).size());
        IMSLog.c(LogClass.CDM_POLL, sb.toString());
        this.mCapabilityDiscovery.removeMessages(z4 ? 18 : 1, Integer.valueOf(i));
        stopThrottledRetryTimer(context, i2, z4);
        if (!stopPoll(map, i2)) {
            setThrottleContactSync(i2);
            if (z4 && this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).isEmpty()) {
                fillPollingList(i2, z5);
            } else if (this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).isEmpty()) {
                IMSLog.i(LOG_TAG, i2, "poll: no uris to request");
            } else {
                Date date = new Date();
                long pollingRatePeriod = this.mCapabilityDiscovery.getCapabilityConfig(i2).getPollingRatePeriod();
                trimPollingHistory(date, pollingRatePeriod, i, list);
                this.room = this.mCapabilityDiscovery.getCapabilityConfig(i2).getPollingRate() - list.size();
                IMSLog.i(LOG_TAG, i2, "poll: room: " + this.room + ", " + list.size() + " request sent in " + pollingRatePeriod + " seconds.");
                if (this.mCapabilityDiscovery.getCapabilityControl(i2) == this.mCapabilityDiscovery.getPresenceModule()) {
                    z3 = requestCapabilityForPresence(i2, z4, date);
                    List<Date> list2 = list;
                } else {
                    z3 = requestCapabilityForOptions(i2, date, list);
                }
                if (z3) {
                    throttledRetryTimer(context, i, this.room, pollingRatePeriod, list, z);
                    return;
                }
                this.mCapabilityDiscovery.setForcePollingGuard(false, i2);
                if (z4) {
                    delayPoll(i2, date);
                    CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                    capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(16, i2, 0, (Object) null), 10000);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void delayPoll(int i, Date date) {
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        long pollingPeriod = ((long) this.mCapabilityDiscovery.getCapabilityConfig(i).getPollingPeriod()) * 1000;
        if (rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.USE_RAND_DELAY_PERIODIC_POLL)) {
            pollingPeriod = this.mCapabilityUtil.getRandomizedDelayForPeriodicPolling(i, pollingPeriod);
        }
        if (pollingPeriod > 0) {
            this.mCapabilityDiscovery.startPollingTimer(pollingPeriod, i);
            this.mCapabilityDiscovery.savePollTimestamp(date.getTime(), i);
        }
    }

    /* access modifiers changed from: package-private */
    public void throttledRetryTimer(Context context, int i, int i2, long j, List<Date> list, boolean z) {
        long j2;
        if (this.mCapabilityDiscovery.getCapabilityControl(i) == this.mCapabilityDiscovery.getPresenceModule()) {
            if (this.mCapabilityDiscovery.getCapabilityConfig(i).getPollListSubExpiry() == 0) {
                j2 = 30000;
            } else {
                j2 = RcsPolicyManager.getRcsStrategy(i).getThrottledDelay((long) this.mCapabilityDiscovery.getCapabilityConfig(i).getPollListSubExpiry()) * 1000;
            }
            if (i2 == 0 && j != 0) {
                j2 = (j * 1000) - (j2 * ((long) list.size()));
            }
        } else {
            j2 = j * 1000;
        }
        startThrottledRetryTimer(context, z, j2, i);
    }

    /* access modifiers changed from: package-private */
    public boolean stopPoll(Map<Integer, ImsRegistration> map, int i) {
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        if (!this.mCapabilityUtil.checkModuleReady(i) || ((rcsStrategy != null && !rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.POLL_ALLOWED)) || (this.mCapabilityDiscovery.getCapabilityControl(i) != null && !this.mCapabilityDiscovery.getCapabilityControl(i).isReadyToRequest(i)))) {
            IMSLog.i(LOG_TAG, i, "stopPoll: cancel poll request");
            this.mCapabilityDiscovery.setForcePollingGuard(false, i);
            return true;
        } else if (map.isEmpty() || !map.containsKey(Integer.valueOf(i))) {
            IMSLog.i(LOG_TAG, i, "stopPoll: not registered.");
            this.mCapabilityDiscovery.setForcePollingGuard(false, i);
            return true;
        } else if (SimUtil.getSimMno(i) != Mno.TMOUS || !this.mCapabilityDiscovery.getCapabilityConfig(i).isDisableInitialScan()) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, i, "stopPoll: initial scan is disabled");
            this.mCapabilityDiscovery.setForcePollingGuard(false, i);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setThrottleContactSync(int i) {
        if (this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).size() >= 1000) {
            this.mCapabilityDiscovery.getPhonebook().setThrottleContactSync(true, i);
        } else if (this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).size() <= 100) {
            this.mCapabilityDiscovery.getPhonebook().setThrottleContactSync(false, i);
        }
    }

    /* access modifiers changed from: package-private */
    public void trimPollingHistory(Date date, long j, int i, List<Date> list) {
        Iterator<Date> it = list.iterator();
        if (this.mCapabilityDiscovery.getCapabilityConfig(i) != null) {
            while (it.hasNext()) {
                if (date.getTime() - it.next().getTime() > 1000 * j) {
                    it.remove();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean requestCapabilityForPresence(int i, boolean z, Date date) {
        int i2;
        CapabilityConstants.RequestType requestType;
        CapabilityConstants.RequestType requestType2;
        IMSLog.s(LOG_TAG, i, "requestCapabilityForPresence:");
        if (this.room > 0 || !z) {
            Set set = this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i));
            synchronized (set) {
                if (set.size() == 1) {
                    CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                    ImsUri imsUri = (ImsUri) set.iterator().next();
                    if (z) {
                        requestType2 = CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC;
                    } else {
                        requestType2 = CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE;
                    }
                    if (capabilityDiscoveryModule.requestCapabilityExchange(imsUri, requestType2, false, i, 0)) {
                        set.clear();
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                } else {
                    CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                    if (z) {
                        requestType = CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC;
                    } else {
                        requestType = CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE;
                    }
                    i2 = capabilityDiscoveryModule2.requestCapabilityExchange(set, requestType, i, 0);
                }
            }
            this.mCapabilityDiscovery.putUrisToRequestList(i, set);
            if (i2 > 1) {
                this.mCapabilityDiscovery.setLastListSubscribeStamp(date.getTime(), i);
            }
            if (z && i2 > 0) {
                this.mCapabilityDiscovery.addPollingHistory(date, i);
            }
        }
        if (this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).size() <= 0) {
            return false;
        }
        IMSLog.i(LOG_TAG, i, "poll: remained mUrisToRequest size: " + this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).size());
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean requestCapabilityForOptions(int i, Date date, List<Date> list) {
        boolean z;
        IMSLog.s(LOG_TAG, i, "requestCapabilityForOptions:");
        Set set = this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i));
        synchronized (set) {
            Iterator it = set.iterator();
            boolean z2 = false;
            while (true) {
                z = true;
                if (!it.hasNext()) {
                    break;
                }
                ImsUri imsUri = (ImsUri) it.next();
                if (this.room != 0) {
                    if (this.mCapabilityDiscovery.requestCapabilityExchange(imsUri, CapabilityConstants.RequestType.REQUEST_TYPE_NONE, false, i, 0)) {
                        it.remove();
                        this.mCapabilityDiscovery.addPollingHistory(date, i);
                        this.room--;
                    } else {
                        z2 = true;
                    }
                    if (list.size() >= this.mCapabilityDiscovery.getCapabilityConfig(i).getPollingRate()) {
                        break;
                    }
                } else {
                    IMSLog.i(LOG_TAG, i, "poll: room is 0");
                    break;
                }
            }
            if (set.size() > 0) {
                IMSLog.i(LOG_TAG, i, "poll: remained mUrisToRequest size: " + set.size());
            } else {
                z = z2;
            }
        }
        this.mCapabilityDiscovery.putUrisToRequestList(i, set);
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean requestCapabilityExchange(ImsUri imsUri, CapabilityConstants.RequestType requestType, boolean z, int i, Capabilities capabilities, IRegistrationManager iRegistrationManager, Map<Integer, ImsRegistration> map, String str, int i2, int i3) {
        ImsUri imsUri2 = imsUri;
        int i4 = i;
        IRegistrationManager iRegistrationManager2 = iRegistrationManager;
        Map<Integer, ImsRegistration> map2 = map;
        Result validateCapexRequest = validateCapexRequest(imsUri, i4, iRegistrationManager2, map2);
        if (validateCapexRequest == Result.PROCEED) {
            IMSLog.s(LOG_TAG, i4, "requestCapabilityExchange: uri = " + imsUri);
            StringBuilder sb = new StringBuilder();
            sb.append("requestCapabilityExchange: ");
            sb.append(imsUri.toStringLimit());
            sb.append(", requesttype: ");
            CapabilityConstants.RequestType requestType2 = requestType;
            sb.append(requestType);
            sb.append(", isAlwaysForce: ");
            boolean z2 = z;
            sb.append(z);
            IMSLog.i(LOG_TAG, i4, sb.toString());
            int i5 = i;
            return this.mCapabilityDiscovery.getCapabilityControl(i4).requestCapabilityExchange(imsUri, (ICapabilityExchangeControl.ICapabilityExchangeCallback) null, requestType, z, this.mCapabilityUtil.filterInCallFeatures(this.mCapabilityUtil.filterFeaturesWithService(capabilities.getFeature(), iRegistrationManager2.getServiceForNetwork(map2.get(Integer.valueOf(i)).getImsProfile(), i2, false, i4), i2, i5), imsUri, str), i5, capabilities.getExtFeatureAsJoinedString(), i3);
        } else if (validateCapexRequest == Result.TRUE) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean sendOptionsRequest(ImsUri imsUri, int i, Set<String> set, IRegistrationManager iRegistrationManager, Map<Integer, ImsRegistration> map) {
        if (validateCapexRequest(imsUri, i, iRegistrationManager, map) == Result.PROCEED) {
            return this.mCapabilityDiscovery.getCapabilityControl(i).sendOptionsRequest(imsUri, true, set, i);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public int requestCapabilityExchange(Set<ImsUri> set, CapabilityConstants.RequestType requestType, int i, int i2) {
        if (set == null || set.size() == 0 || this.mCapabilityDiscovery.getCapabilityControl(i) == null) {
            return 0;
        }
        IMSLog.i(LOG_TAG, i, "requestCapabilityExchange: " + set.size() + " contacts");
        StringBuilder sb = new StringBuilder();
        sb.append("requestCapabilityExchange internalRequestId : ");
        sb.append(i2);
        IMSLog.i(LOG_TAG, i, sb.toString());
        ArrayList arrayList = new ArrayList();
        for (ImsUri next : set) {
            if (!this.mCapabilityUtil.isAllowedPrefixesUri(next, i) && !ChatbotUriUtil.hasChatbotRoleSession(next, i)) {
                arrayList.add(next);
            }
        }
        if (arrayList.size() > 0) {
            IMSLog.s(LOG_TAG, i, "requestCapabilityExchange: remove notAllowedUris = " + arrayList);
            set.removeAll(arrayList);
            IMSLog.i(LOG_TAG, i, "requestCapabilityExchange: " + set.size() + " contacts after removed notAllowedUris");
            if (set.size() == 0) {
                return 0;
            }
        }
        return this.mCapabilityDiscovery.getCapabilityControl(i).requestCapabilityExchange(set, requestType, i, i2);
    }

    /* access modifiers changed from: package-private */
    public void requestInitialCapabilitiesQuery(int i, boolean z, long j) {
        IMSLog.i(LOG_TAG, i, "requestInitialCapabilitiesQuery:");
        this.mCapabilityDiscovery.removeMessages(3, Integer.valueOf(i));
        if (this.mCapabilityDiscovery.getCapabilityConfig(i) != null && this.mCapabilityDiscovery.getCapabilityConfig(i).isDisableInitialScan()) {
            IMSLog.i(LOG_TAG, i, "requestInitialCapabilitiesQuery: disable initial scan");
            if (!RcsPolicyManager.getRcsStrategy(i).boolSetting(RcsPolicySettings.RcsPolicy.POLL_ALLOWED) && z) {
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(16, i, 0, (Object) null), 10000);
            }
        } else if (!this.mCapabilityDiscovery.getPhonebook().isReady(i)) {
            if (i == SimUtil.getActiveDataPhoneId()) {
                IMSLog.i(LOG_TAG, i, "requestInitialCapabilitiesQuery: contact is not ready. retry in 1 second.");
                CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule2.sendMessageDelayed(capabilityDiscoveryModule2.obtainMessage(3, Integer.valueOf(i)), 1000);
            }
        } else if (this.mCapabilityDiscovery.getCapabilityControl(i) == null || this.mCapabilityDiscovery.getCapabilityConfig(i) == null || !this.mCapabilityDiscovery.getCapabilityControl(i).isReadyToRequest(i)) {
            IMSLog.i(LOG_TAG, i, "requestInitialCapabilitiesQuery: not ready. retry in 1 second.");
            CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule3.sendMessageDelayed(capabilityDiscoveryModule3.obtainMessage(3, Integer.valueOf(i)), 1000);
        } else {
            IMSLog.i(LOG_TAG, i, "requestInitialCapabilitiesQuery: mLastPollTimestamp: " + j + ", mPollingPeriod: " + this.mCapabilityDiscovery.getCapabilityConfig(i).getPollingPeriod());
            if (this.mCapabilityDiscovery.isPollingInProgress(i)) {
                IMSLog.i(LOG_TAG, i, "requestInitialCapabilitiesQuery: Polling already in progress");
            } else if (this.mCapabilityDiscovery.getCapabilityConfig(i).getPollingPeriod() <= 0 || this.mCapabilityDiscovery.getCapabilityConfig(i).getPollingRate() <= 0) {
                if (!this.mCapabilityDiscovery.getCapabilityConfig(i).usePresence() || !RcsPolicyManager.getRcsStrategy(i).boolSetting(RcsPolicySettings.RcsPolicy.POLL_ALLOWED)) {
                    this.mCapabilityDiscovery.onContactChanged(z);
                    if (z) {
                        this.mCapabilityDiscovery.setInitialQuery(false, i);
                        return;
                    }
                    return;
                }
                this.mCapabilityDiscovery.onContactChanged(true);
            } else if (j == 0) {
                IMSLog.i(LOG_TAG, i, "requestInitialCapabilitiesQuery: Polling has not been performed yet, start polling");
                CapabilityDiscoveryModule capabilityDiscoveryModule4 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule4.sendMessage(capabilityDiscoveryModule4.obtainMessage(18, 0, 0, Integer.valueOf(i)));
            } else {
                this.mCapabilityDiscovery.startPoll(i);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void forcePoll(int i) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "forcePoll forcePollingGuard = " + this.mCapabilityDiscovery.getForcePollingGuard(i));
        if (!this.mCapabilityDiscovery.getForcePollingGuard(i)) {
            this.mCapabilityDiscovery.setForcePollingGuard(true, i);
            resetPartialPolling(i);
            this.mCapabilityDiscovery.removeMessages(18, Integer.valueOf(i));
            fillPollingList(i, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void fillPollingList(int i, boolean z) {
        new Thread(new CapabilityExchange$$ExternalSyntheticLambda0(this, i, z)).start();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$fillPollingList$0(int i, boolean z) {
        boolean z2;
        int i2 = i;
        boolean z3 = z;
        if (this.mCapabilityDiscovery.getCapabilitiesCache(i2) == null) {
            IMSLog.e(LOG_TAG, i2, "fillPollingList: CapabilitiesCache is null");
        } else if (this.mCapabilityDiscovery.getCapabilityControl(i2) == null) {
            IMSLog.e(LOG_TAG, i2, "fillPollingList: getCapabilityControl is null");
        } else {
            int i3 = 1;
            boolean z4 = this.mCapabilityDiscovery.getCapabilityControl(i2) == this.mCapabilityDiscovery.getPresenceModule();
            CapabilityConfig capabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(i2);
            IMSLog.i(LOG_TAG, i2, "fillPollingList count : " + mPollingPartialCount);
            try {
                TreeMap<Integer, ImsUri> capabilitiesForPolling = this.mCapabilityDiscovery.getCapabilitiesCache(i2).getCapabilitiesForPolling(z4 ? 2000 : -1, mPollingPartialCount, (long) capabilityConfig.getNonRCScapInfoExpiry(), (long) capabilityConfig.getCapInfoExpiry(), z);
                Set set = this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i));
                this.mEventLog.logAndAdd(i2, "fillPollingList: capexListSize = " + capabilitiesForPolling.size() + ", mPollingPartialCount = " + mPollingPartialCount + ", S");
                IMSLog.c(LogClass.CDM_FILL_POLL_LIST_S, i2 + "," + capabilitiesForPolling.size() + "," + mPollingPartialCount);
                for (Integer intValue : capabilitiesForPolling.keySet()) {
                    updatePollList(set, capabilitiesForPolling.get(Integer.valueOf(intValue.intValue())), true, i2);
                }
                mPollingPartialCount = capabilitiesForPolling.size() > 0 ? (long) capabilitiesForPolling.lastKey().intValue() : 0;
                if (!z4) {
                    z2 = z;
                } else if (capabilitiesForPolling.size() == 2000) {
                    long partialPollingPeriod = getPartialPollingPeriod(i);
                    this.mEventLog.logAndAdd(i2, "fillPollingList: exceed max, retry partial poll after " + partialPollingPeriod);
                    IMSLog.c(LogClass.CDM_RETRY_PARTIAL_POLL, "" + i2);
                    z2 = z;
                    this.mCapabilityDiscovery.startPartialPollingTimer(partialPollingPeriod, z2, i2);
                } else {
                    z2 = z;
                    resetPartialPolling(i);
                }
                this.mEventLog.logAndAdd(i2, "fillPollingList: E");
                IMSLog.c(LogClass.CDM_FILL_POLL_LIST_E, "" + i2);
                if (!set.isEmpty()) {
                    CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                    if (!z2) {
                        i3 = 0;
                    }
                    capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(18, i3, 0, Integer.valueOf(i)));
                    return;
                }
                resetPartialPolling(i);
                delayPoll(i2, new Date());
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetPartialPolling(int i) {
        IMSLog.i(LOG_TAG, i, "resetPartialPolling");
        this.mCapabilityDiscovery.stopPartialPollingTimer(i);
        mPollingPartialCount = 0;
    }

    /* access modifiers changed from: package-private */
    public void exchangeCapabilities(Map<Integer, ImsRegistration> map, IRegistrationManager iRegistrationManager, String str, long j, int i, String str2, String str3) {
        Map<Integer, ImsRegistration> map2 = map;
        IRegistrationManager iRegistrationManager2 = iRegistrationManager;
        ImsUri normalizedUri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(str, true);
        if (normalizedUri == null) {
            Log.i(LOG_TAG, "getCapabilities: uri is null");
            return;
        }
        Log.i(LOG_TAG, "exchangeCapabilities: myFeatures = " + Capabilities.dumpFeature(j));
        if (iRegistrationManager2 == null || !map2.containsKey(Integer.valueOf(i))) {
            Log.i(LOG_TAG, "exchangeCapabilities: mRegMan or mImsRegInfo is null");
        } else if (iRegistrationManager2.isSuspended(map2.get(Integer.valueOf(i)).getHandle())) {
            Log.i(LOG_TAG, "cannot exchange capabilities. currently in suspend");
        } else {
            this.mCapabilityDiscovery.getOptionsModule().requestCapabilityExchange(normalizedUri, (ICapabilityExchangeControl.ICapabilityExchangeCallback) null, CapabilityConstants.RequestType.REQUEST_TYPE_NONE, true, this.mCapabilityUtil.filterInCallFeatures(j, normalizedUri, str3), i, str2, 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void startThrottledRetryTimer(Context context, boolean z, long j, int i) {
        stopThrottledRetryTimer(context, i, z);
        IMSLog.i(LOG_TAG, i, "startThrottledRetryTimer: isPeriodic = " + z + ", millis " + j);
        if (j >= SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF) {
            Intent intent = new Intent("com.sec.internal.ims.servicemodules.options.sub_throttled_timeout");
            intent.putExtra("IS_PERIODIC", z);
            intent.setPackage(context.getPackageName());
            this.mCapabilityDiscovery.setThrottledIntent(PendingIntent.getBroadcast(context, 0, intent, 33554432), i);
            AlarmTimer.start(context, this.mCapabilityDiscovery.getThrottledIntent(i), j);
        } else if (z) {
            PreciseAlarmManager.getInstance(context).sendMessageDelayed(getClass().getSimpleName(), this.mCapabilityDiscovery.obtainMessage(18, 0, 0, Integer.valueOf(i)), j);
        } else {
            PreciseAlarmManager.getInstance(context).sendMessageDelayed(getClass().getSimpleName(), this.mCapabilityDiscovery.obtainMessage(1, Integer.valueOf(i)), j);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopThrottledRetryTimer(Context context, int i, boolean z) {
        IMSLog.i(LOG_TAG, i, "stopThrottledRetryTimer: isPeriodic = " + z);
        PreciseAlarmManager.getInstance(context).removeMessage(this.mCapabilityDiscovery.obtainMessage(z ? 18 : 1, Integer.valueOf(i)));
        if (this.mCapabilityDiscovery.getThrottledIntent(i) != null) {
            AlarmTimer.stop(context, this.mCapabilityDiscovery.getThrottledIntent(i));
            this.mCapabilityDiscovery.setThrottledIntent((PendingIntent) null, i);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updatePollList(Set<ImsUri> set, ImsUri imsUri, boolean z, int i) {
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        if (rcsStrategy == null) {
            Log.e(LOG_TAG, "updatePollList: mnoStrategy is null.");
            return false;
        } else if (!rcsStrategy.isCapabilityValidUri(imsUri)) {
            Log.e(LOG_TAG, "updatePollList: isCapabilityValidUri is false.");
            return false;
        } else {
            synchronized (set) {
                if (z) {
                    boolean add = set.add(imsUri);
                    return add;
                }
                boolean remove = set.remove(imsUri);
                return remove;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Result validateCapexRequest(ImsUri imsUri, int i, IRegistrationManager iRegistrationManager, Map<Integer, ImsRegistration> map) {
        ImsRegistration imsRegistration;
        if (imsUri == null || this.mCapabilityDiscovery.getCapabilityControl(i) == null) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: uri or mControl is null");
            return Result.FALSE;
        }
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        if (rcsStrategy == null) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: mnoStrategy is null.");
            return Result.FALSE;
        } else if (!this.mCapabilityUtil.isAllowedPrefixesUri(imsUri, i) && !ChatbotUriUtil.hasChatbotRoleSession(imsUri, i)) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: isAllowedPrefixesUri and hasChatbotRoleSession are false.");
            return Result.TRUE;
        } else if (!rcsStrategy.isCapabilityValidUri(imsUri)) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: isPresenceValidUri is false.");
            return Result.TRUE;
        } else if (this.mCapabilityUtil.blockOptionsToOwnUri(imsUri, i)) {
            return Result.TRUE;
        } else {
            if (ChatbotUriUtil.hasUriBotPlatform(imsUri, i) && !TextUtils.isEmpty(imsUri.getParam("user"))) {
                IMSLog.i(LOG_TAG, i, "remove user=phone param for chatbot serviceId");
                imsUri.removeUserParam();
            }
            if (iRegistrationManager == null || map.isEmpty() || (imsRegistration = map.get(Integer.valueOf(i))) == null) {
                IMSLog.i(LOG_TAG, i, "requestCapabilityExchange: mRegMan or ImsRegistration is null");
                return Result.FALSE;
            } else if (!iRegistrationManager.isSuspended(imsRegistration.getHandle())) {
                return Result.PROCEED;
            } else {
                IMSLog.i(LOG_TAG, i, "both phoneId 0 and phoneId 1 was suspended, cannot exchange capabilities.");
                return Result.FALSE;
            }
        }
    }

    private long getPartialPollingPeriod(int i) {
        long j;
        int pollListSubExpiry = this.mCapabilityDiscovery.getCapabilityConfig(i).getPollListSubExpiry();
        if (pollListSubExpiry == 0) {
            j = 30000;
        } else {
            j = RcsPolicyManager.getRcsStrategy(i).getThrottledDelay((long) pollListSubExpiry) * 1000;
        }
        return ((long) (2000 / this.mCapabilityDiscovery.mPresenceModule.getPresenceConfig(i).getMaxUri())) * j;
    }
}
