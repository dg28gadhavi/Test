package com.sec.internal.ims.servicemodules.euc.workflow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException;
import com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VolatileEucWorkflow extends BaseEucWorkflow {
    private static final String INTENT_EUCR_VOLATILE_TIMEOUT = "com.sec.internal.ims.servicemodules.euc.workflow.action.VOLATILE_TIMEOUT";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "VolatileEucWorkflow";
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String r3 = VolatileEucWorkflow.LOG_TAG;
            Log.d(r3, "onReceive: EUCR Volatile intent: " + intent.getAction());
            if (VolatileEucWorkflow.INTENT_EUCR_VOLATILE_TIMEOUT.equals(intent.getAction())) {
                Log.i(VolatileEucWorkflow.LOG_TAG, "onReceive: EUCR Volatile message timeout.");
                onEucrVolatileTimeout();
            }
        }

        private void onEucrVolatileTimeout() {
            VolatileEucWorkflow volatileEucWorkflow = VolatileEucWorkflow.this;
            volatileEucWorkflow.timeoutMessage((IEucData) volatileEucWorkflow.mCurrentAlarm.second);
            VolatileEucWorkflow.this.unscheduleCurrentAlarmTimerIntent();
            VolatileEucWorkflow.this.scheduleNextAlarmTimerIntent((IEucData) null);
        }
    };
    private final Context mContext;
    /* access modifiers changed from: private */
    public Pair<PendingIntent, IEucData> mCurrentAlarm = null;
    private final IEucFactory mEucFactory;
    private final Handler mHandler;

    public /* bridge */ /* synthetic */ void discard(String str) {
        super.discard(str);
    }

    public VolatileEucWorkflow(Context context, Handler handler, IEucPersistence iEucPersistence, IEucDisplayManager iEucDisplayManager, IEucStoreAndForward iEucStoreAndForward, IEucFactory iEucFactory) {
        super(iEucPersistence, iEucDisplayManager, iEucStoreAndForward);
        this.mContext = context;
        this.mHandler = handler;
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(iEucFactory);
    }

    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_EUCR_VOLATILE_TIMEOUT);
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter, (String) null, this.mHandler);
        Log.d(LOG_TAG, "Receiver registered.");
    }

    public void stop() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        Log.d(LOG_TAG, "Receiver unregistered.");
    }

    public void load(String str) {
        this.mOwnIdentities.add(str);
        EucType eucType = EucType.VOLATILE;
        List singletonList = Collections.singletonList(eucType);
        try {
            for (IEucData next : this.mEucPersistence.getAllEucs((List<EucState>) Arrays.asList(new EucState[]{EucState.ACCEPTED_NOT_SENT, EucState.REJECTED_NOT_SENT}), eucType, str)) {
                if (isMessageTimedOut(next).booleanValue()) {
                    timeoutMessage(next);
                } else if (next.getState() == EucState.ACCEPTED_NOT_SENT) {
                    sendResponse(next, EucResponseData.Response.ACCEPT, next.getUserPin());
                } else if (next.getState() == EucState.REJECTED_NOT_SENT) {
                    sendResponse(next, EucResponseData.Response.DECLINE, next.getUserPin());
                }
            }
        } catch (EucPersistenceException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "Unable to obtain EUCs from persistence: " + e);
        }
        try {
            IEucPersistence iEucPersistence = this.mEucPersistence;
            EucState eucState = EucState.NONE;
            Iterable<IEucQuery> combine = this.mEucFactory.combine(iEucPersistence.getAllEucs(eucState, EucType.VOLATILE, str), this.mEucPersistence.getDialogsByTypes(eucState, singletonList, this.mLanguageCode, str));
            for (IEucQuery eucData : combine) {
                IEucData eucData2 = eucData.getEucData();
                if (isMessageTimedOut(eucData2).booleanValue()) {
                    timeoutMessage(eucData2);
                }
            }
            scheduleNextAlarmTimerIntent((IEucData) null);
            loadToCache(combine);
            displayQueries(combine, this.mLanguageCode);
        } catch (EucPersistenceException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "Unable to obtain EUCs from persistence: " + e2);
        }
    }

    public void handleIncomingEuc(IEucQuery iEucQuery) {
        IEucData eucData = iEucQuery.getEucData();
        String str = LOG_TAG;
        Log.d(str, "handleIncomingEuc with id=" + eucData.getKey());
        try {
            this.mEucPersistence.insertEuc(eucData);
            this.mEucPersistence.insertDialogs(iEucQuery);
        } catch (EucPersistenceException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "Unable to store EUC with key=" + eucData.getKey() + " in persistence: " + e);
        }
        if (eucData.getTimeOut().longValue() > System.currentTimeMillis()) {
            this.mCache.put(iEucQuery);
            scheduleNextAlarmTimerIntent(eucData);
            this.mDisplayManager.display(iEucQuery, this.mLanguageCode, createDisplayManagerRequestCallback(iEucQuery));
        }
    }

    public void changeLanguage(String str) {
        this.mLanguageCode = str;
        changeLanguage(str, EucType.VOLATILE);
    }

    public IEucStoreAndForward.IResponseCallback createSendResponseCallback() {
        return new IEucStoreAndForward.IResponseCallback() {
            public void onStatus(EucSendResponseStatus eucSendResponseStatus) {
                EucSendResponseStatus.Status status = eucSendResponseStatus.getStatus();
                String id = eucSendResponseStatus.getId();
                String ownIdentity = eucSendResponseStatus.getOwnIdentity();
                EucMessageKey eucMessageKey = new EucMessageKey(id, ownIdentity, EucType.VOLATILE, eucSendResponseStatus.getRemoteUri());
                try {
                    IEucData eucByKey = VolatileEucWorkflow.this.mEucPersistence.getEucByKey(eucMessageKey);
                    if (eucByKey != null) {
                        int i = AnonymousClass3.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status[status.ordinal()];
                        if (i == 1) {
                            EucState state = eucByKey.getState();
                            int i2 = AnonymousClass3.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState[state.ordinal()];
                            if (i2 == 1) {
                                VolatileEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.ACCEPTED, (String) null);
                                reschedule(eucMessageKey);
                            } else if (i2 == 2) {
                                VolatileEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.REJECTED, (String) null);
                                reschedule(eucMessageKey);
                            } else {
                                String r8 = VolatileEucWorkflow.LOG_TAG;
                                Log.e(r8, "Wrong state: " + state.getId() + " for EUCR with id=" + id);
                                String r82 = VolatileEucWorkflow.LOG_TAG;
                                IMSLog.s(r82, "Wrong state: " + state.getId() + " for EUCR with key=" + eucMessageKey);
                                throw new IllegalStateException("Illegal volatile EUC state!");
                            }
                        } else if (i == 2) {
                            Log.e(VolatileEucWorkflow.LOG_TAG, "Network error. Message will not be send");
                            VolatileEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.FAILED, (String) null);
                            reschedule(eucMessageKey);
                        } else if (i == 3) {
                            String r83 = VolatileEucWorkflow.LOG_TAG;
                            Log.e(r83, "Internal error. Msg will be send on a new regi for identity: " + IMSLog.checker(ownIdentity));
                        }
                    } else {
                        String r84 = VolatileEucWorkflow.LOG_TAG;
                        Log.e(r84, "EUCR with id=" + id + " was not found!");
                        String r85 = VolatileEucWorkflow.LOG_TAG;
                        IMSLog.s(r85, "EUCR with key=" + eucMessageKey + " was not found!");
                    }
                } catch (EucPersistenceException unused) {
                    String r86 = VolatileEucWorkflow.LOG_TAG;
                    Log.e(r86, "Unable to change EUCs state in persistence for EUCR with id=" + id);
                    String r87 = VolatileEucWorkflow.LOG_TAG;
                    IMSLog.s(r87, "Unable to change EUCs state in persistence for EUCR with key=" + eucMessageKey);
                }
            }

            private void reschedule(EucMessageKey eucMessageKey) {
                if (VolatileEucWorkflow.this.mCurrentAlarm != null && eucMessageKey.equals(((IEucData) VolatileEucWorkflow.this.mCurrentAlarm.second).getKey())) {
                    VolatileEucWorkflow.this.unscheduleCurrentAlarmTimerIntent();
                    VolatileEucWorkflow.this.scheduleNextAlarmTimerIntent((IEucData) null);
                }
            }
        };
    }

    /* renamed from: com.sec.internal.ims.servicemodules.euc.workflow.VolatileEucWorkflow$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState;

        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0039 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x001d */
        static {
            /*
                com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus$Status[] r0 = com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus.Status.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status = r0
                r1 = 1
                com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus$Status r2 = com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus.Status.SUCCESS     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus$Status r3 = com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus.Status.FAILURE_NETWORK     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r2 = $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus$Status r3 = com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus.Status.FAILURE_INTERNAL     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r4 = 3
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                com.sec.internal.ims.servicemodules.euc.data.EucState[] r2 = com.sec.internal.ims.servicemodules.euc.data.EucState.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState = r2
                com.sec.internal.ims.servicemodules.euc.data.EucState r3 = com.sec.internal.ims.servicemodules.euc.data.EucState.ACCEPTED_NOT_SENT     // Catch:{ NoSuchFieldError -> 0x0039 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0039 }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x0039 }
            L_0x0039:
                int[] r1 = $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState     // Catch:{ NoSuchFieldError -> 0x0043 }
                com.sec.internal.ims.servicemodules.euc.data.EucState r2 = com.sec.internal.ims.servicemodules.euc.data.EucState.REJECTED_NOT_SENT     // Catch:{ NoSuchFieldError -> 0x0043 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0043 }
                r1[r2] = r0     // Catch:{ NoSuchFieldError -> 0x0043 }
            L_0x0043:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.euc.workflow.VolatileEucWorkflow.AnonymousClass3.<clinit>():void");
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0087, code lost:
        r4.mCache.remove(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x008c, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0059, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:?, code lost:
        r1 = LOG_TAG;
        android.util.Log.e(r1, "Unable to change EUCs state in persistence for EUCR with id=" + r5);
        com.sec.internal.log.IMSLog.s(r1, "Unable to change EUCs state in persistence for EUCR with key=" + r0);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:8:0x005b */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void timeoutMessage(com.sec.internal.ims.servicemodules.euc.data.IEucData r5) {
        /*
            r4 = this;
            com.sec.internal.ims.servicemodules.euc.data.EucMessageKey r0 = r5.getKey()
            java.lang.String r5 = r5.getId()
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Timeout message with id="
            r2.append(r3)
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Timeout message with key="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r1, r2)
            java.util.Map<com.sec.internal.ims.servicemodules.euc.data.EucMessageKey, com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward$IResponseHandle> r1 = r4.mHandleMap
            boolean r1 = r1.containsKey(r0)
            if (r1 != 0) goto L_0x0040
            com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager r1 = r4.mDisplayManager
            r1.hide(r0)
            goto L_0x004b
        L_0x0040:
            java.util.Map<com.sec.internal.ims.servicemodules.euc.data.EucMessageKey, com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward$IResponseHandle> r1 = r4.mHandleMap
            java.lang.Object r1 = r1.get(r0)
            com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward$IResponseHandle r1 = (com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward.IResponseHandle) r1
            r1.invalidate()
        L_0x004b:
            com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence r1 = r4.mEucPersistence     // Catch:{ EucPersistenceException -> 0x005b }
            com.sec.internal.ims.servicemodules.euc.data.EucState r2 = com.sec.internal.ims.servicemodules.euc.data.EucState.TIMED_OUT     // Catch:{ EucPersistenceException -> 0x005b }
            r3 = 0
            r1.updateEuc(r0, r2, r3)     // Catch:{ EucPersistenceException -> 0x005b }
        L_0x0053:
            com.sec.internal.ims.servicemodules.euc.cache.IEucCache r4 = r4.mCache
            r4.remove(r0)
            goto L_0x0086
        L_0x0059:
            r5 = move-exception
            goto L_0x0087
        L_0x005b:
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x0059 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0059 }
            r2.<init>()     // Catch:{ all -> 0x0059 }
            java.lang.String r3 = "Unable to change EUCs state in persistence for EUCR with id="
            r2.append(r3)     // Catch:{ all -> 0x0059 }
            r2.append(r5)     // Catch:{ all -> 0x0059 }
            java.lang.String r5 = r2.toString()     // Catch:{ all -> 0x0059 }
            android.util.Log.e(r1, r5)     // Catch:{ all -> 0x0059 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0059 }
            r5.<init>()     // Catch:{ all -> 0x0059 }
            java.lang.String r2 = "Unable to change EUCs state in persistence for EUCR with key="
            r5.append(r2)     // Catch:{ all -> 0x0059 }
            r5.append(r0)     // Catch:{ all -> 0x0059 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0059 }
            com.sec.internal.log.IMSLog.s(r1, r5)     // Catch:{ all -> 0x0059 }
            goto L_0x0053
        L_0x0086:
            return
        L_0x0087:
            com.sec.internal.ims.servicemodules.euc.cache.IEucCache r4 = r4.mCache
            r4.remove(r0)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.euc.workflow.VolatileEucWorkflow.timeoutMessage(com.sec.internal.ims.servicemodules.euc.data.IEucData):void");
    }

    private Boolean isMessageTimedOut(IEucData iEucData) {
        return Boolean.valueOf(getRemainingTimeout(iEucData) < 0);
    }

    private long getRemainingTimeout(IEucData iEucData) {
        return iEucData.getTimeOut().longValue() - System.currentTimeMillis();
    }

    /* access modifiers changed from: private */
    public void scheduleNextAlarmTimerIntent(IEucData iEucData) {
        if (iEucData == null) {
            try {
                iEucData = this.mEucPersistence.getVolatileEucByMostRecentTimeout(this.mOwnIdentities);
            } catch (EucPersistenceException e) {
                String str = LOG_TAG;
                Log.e(str, "Unable to obtain EUCs from persistence: " + e);
            }
        }
        if (iEucData != null) {
            Pair<PendingIntent, IEucData> pair = this.mCurrentAlarm;
            if (pair != null) {
                if (getRemainingTimeout((IEucData) pair.second) > getRemainingTimeout(iEucData)) {
                    unscheduleCurrentAlarmTimerIntent();
                } else {
                    return;
                }
            }
            Pair<PendingIntent, IEucData> create = Pair.create(PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_EUCR_VOLATILE_TIMEOUT), 33554432), iEucData);
            this.mCurrentAlarm = create;
            AlarmTimer.start(this.mContext, (PendingIntent) create.first, getRemainingTimeout(iEucData));
        }
    }

    /* access modifiers changed from: private */
    public void unscheduleCurrentAlarmTimerIntent() {
        AlarmTimer.stop(this.mContext, (PendingIntent) this.mCurrentAlarm.first);
        this.mCurrentAlarm = null;
    }
}
