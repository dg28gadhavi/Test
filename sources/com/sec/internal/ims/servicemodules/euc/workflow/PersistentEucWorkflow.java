package com.sec.internal.ims.servicemodules.euc.workflow;

import android.util.Log;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException;
import com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.List;

public class PersistentEucWorkflow extends BaseEucWorkflow {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "PersistentEucWorkflow";
    private final IEucFactory mEucFactory;

    public /* bridge */ /* synthetic */ void discard(String str) {
        super.discard(str);
    }

    public /* bridge */ /* synthetic */ void start() {
        super.start();
    }

    public /* bridge */ /* synthetic */ void stop() {
        super.stop();
    }

    public PersistentEucWorkflow(IEucPersistence iEucPersistence, IEucDisplayManager iEucDisplayManager, IEucFactory iEucFactory, IEucStoreAndForward iEucStoreAndForward) {
        super(iEucPersistence, iEucDisplayManager, iEucStoreAndForward);
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(iEucFactory);
    }

    public void load(String str) {
        this.mOwnIdentities.add(str);
        List asList = Arrays.asList(new EucType[]{EucType.PERSISTENT, EucType.ACKNOWLEDGEMENT});
        try {
            for (IEucData next : this.mEucPersistence.getAllEucs((List<EucState>) Arrays.asList(new EucState[]{EucState.ACCEPTED_NOT_SENT, EucState.REJECTED_NOT_SENT, EucState.NONE}), (List<EucType>) asList, str)) {
                if (next.getState() == EucState.ACCEPTED_NOT_SENT) {
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
            Iterable<IEucQuery> combine = this.mEucFactory.combine(iEucPersistence.getAllEucs(eucState, (List<EucType>) asList, str), this.mEucPersistence.getDialogsByTypes(eucState, asList, this.mLanguageCode, str));
            loadToCache(combine);
            displayQueries(combine, this.mLanguageCode);
        } catch (EucPersistenceException | NullPointerException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "Unable to obtain EUCs from persistence: " + e2);
        }
    }

    public void handleIncomingEuc(IEucQuery iEucQuery) {
        this.mCache.put(iEucQuery);
        IEucData eucData = iEucQuery.getEucData();
        String id = eucData.getId();
        EucMessageKey eucMessageKey = new EucMessageKey(id, eucData.getOwnIdentity(), EucType.PERSISTENT, eucData.getRemoteUri());
        try {
            if (eucData.getType() == EucType.ACKNOWLEDGEMENT && this.mCache.get(eucMessageKey) != null) {
                this.mDisplayManager.hide(eucMessageKey);
                this.mCache.remove(eucMessageKey);
                this.mEucPersistence.updateEuc(eucMessageKey, EucState.DISMISSED, (String) null);
            }
        } catch (EucPersistenceException e) {
            String str = LOG_TAG;
            Log.e(str, "Unable to update EUC with id=" + id + " in persistence: " + e);
            IMSLog.s(str, "Unable to update EUC with key=" + eucMessageKey + " in persistence: " + e);
        }
        try {
            this.mEucPersistence.insertEuc(eucData);
            this.mEucPersistence.insertDialogs(iEucQuery);
        } catch (EucPersistenceException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "Unable to insert EUC with id=" + id + " in persistence: " + e2);
            IMSLog.s(str2, "Unable to insert EUC with key=" + eucMessageKey + " in persistence: " + e2);
        }
        IDialogData dialogData = iEucQuery.getDialogData(this.mLanguageCode);
        if (!dialogData.getSubject().isEmpty() || !dialogData.getText().isEmpty()) {
            this.mDisplayManager.display(iEucQuery, this.mLanguageCode, createDisplayManagerRequestCallback(iEucQuery));
        }
    }

    public IEucStoreAndForward.IResponseCallback createSendResponseCallback() {
        return new IEucStoreAndForward.IResponseCallback() {
            public void onStatus(EucSendResponseStatus eucSendResponseStatus) {
                EucSendResponseStatus.Status status = eucSendResponseStatus.getStatus();
                String id = eucSendResponseStatus.getId();
                String ownIdentity = eucSendResponseStatus.getOwnIdentity();
                EucMessageKey eucMessageKey = new EucMessageKey(id, ownIdentity, EucType.PERSISTENT, eucSendResponseStatus.getRemoteUri());
                if (PersistentEucWorkflow.this.mHandleMap.containsKey(eucMessageKey)) {
                    PersistentEucWorkflow.this.mHandleMap.get(eucMessageKey).invalidate();
                    PersistentEucWorkflow.this.mHandleMap.remove(eucMessageKey);
                }
                try {
                    IEucData eucByKey = PersistentEucWorkflow.this.mEucPersistence.getEucByKey(eucMessageKey);
                    if (eucByKey != null) {
                        int i = AnonymousClass2.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status[status.ordinal()];
                        if (i == 1) {
                            EucState state = eucByKey.getState();
                            int i2 = AnonymousClass2.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState[state.ordinal()];
                            if (i2 == 1) {
                                PersistentEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.ACCEPTED, (String) null);
                            } else if (i2 == 2) {
                                PersistentEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.REJECTED, (String) null);
                            } else {
                                String r8 = PersistentEucWorkflow.LOG_TAG;
                                Log.e(r8, "Wrong state: " + state.getId() + " for EUCR with id=" + id);
                                String r82 = PersistentEucWorkflow.LOG_TAG;
                                IMSLog.s(r82, "Wrong state: " + state.getId() + " for EUCR with key=" + eucMessageKey);
                                throw new IllegalStateException("Illegal persistent EUC state!");
                            }
                        } else if (i == 2) {
                            Log.e(PersistentEucWorkflow.LOG_TAG, "Network error. Message will not be send");
                            PersistentEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.FAILED, (String) null);
                        } else if (i == 3) {
                            String r83 = PersistentEucWorkflow.LOG_TAG;
                            Log.e(r83, "Internal error. Msg will be send on a new regi for identity: " + IMSLog.checker(ownIdentity));
                        }
                    } else {
                        String r84 = PersistentEucWorkflow.LOG_TAG;
                        Log.e(r84, "EUCR with id=" + id + " was not found!");
                        String r85 = PersistentEucWorkflow.LOG_TAG;
                        IMSLog.s(r85, "EUCR with key=" + eucMessageKey + " was not found!");
                    }
                } catch (EucPersistenceException unused) {
                    String r86 = PersistentEucWorkflow.LOG_TAG;
                    Log.e(r86, "Unable to change EUCs state in persistence for EUCR with id=" + id);
                    String r87 = PersistentEucWorkflow.LOG_TAG;
                    IMSLog.s(r87, "Unable to change EUCs state in persistence for EUCR with key=" + eucMessageKey);
                }
            }
        };
    }

    /* renamed from: com.sec.internal.ims.servicemodules.euc.workflow.PersistentEucWorkflow$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
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
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.euc.workflow.PersistentEucWorkflow.AnonymousClass2.<clinit>():void");
        }
    }

    public void changeLanguage(String str) {
        this.mLanguageCode = str;
        changeLanguage(str, EucType.PERSISTENT);
        changeLanguage(str, EucType.ACKNOWLEDGEMENT);
    }
}
