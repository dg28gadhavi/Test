package com.sec.internal.ims.servicemodules.euc.workflow;

import android.util.Log;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.cache.EucCache;
import com.sec.internal.ims.servicemodules.euc.cache.IEucCache;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException;
import com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class BaseEucWorkflow implements IEucWorkflow {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "BaseEucWorkflow";
    protected final IEucCache mCache = new EucCache();
    final IEucDisplayManager mDisplayManager;
    final IEucPersistence mEucPersistence;
    Map<EucMessageKey, IEucStoreAndForward.IResponseHandle> mHandleMap = new HashMap();
    String mLanguageCode = DeviceLocale.DEFAULT_LANG_VALUE;
    List<String> mOwnIdentities = new ArrayList();
    private final IEucStoreAndForward mStoreAndForward;

    /* access modifiers changed from: package-private */
    public abstract IEucStoreAndForward.IResponseCallback createSendResponseCallback();

    public void start() {
    }

    public void stop() {
    }

    BaseEucWorkflow(IEucPersistence iEucPersistence, IEucDisplayManager iEucDisplayManager, IEucStoreAndForward iEucStoreAndForward) {
        this.mEucPersistence = (IEucPersistence) Preconditions.checkNotNull(iEucPersistence);
        this.mDisplayManager = (IEucDisplayManager) Preconditions.checkNotNull(iEucDisplayManager);
        this.mStoreAndForward = iEucStoreAndForward;
    }

    /* access modifiers changed from: package-private */
    public void loadToCache(Iterable<IEucQuery> iterable) {
        for (IEucQuery put : iterable) {
            this.mCache.put(put);
        }
    }

    /* access modifiers changed from: package-private */
    public void changeLanguage(String str, EucType eucType) {
        String str2;
        ArrayList arrayList = new ArrayList();
        for (IEucQuery next : this.mCache.getAllByType(eucType)) {
            if (!next.hasDialog(str)) {
                arrayList.add(next.getEucData().getId());
            }
        }
        if (!arrayList.isEmpty()) {
            try {
                List<IDialogData> dialogs = this.mEucPersistence.getDialogs(arrayList, eucType, str, this.mOwnIdentities);
                Preconditions.checkState(!dialogs.isEmpty(), "No dialogs found for given EUCRs, it should not happen!");
                for (IDialogData next2 : dialogs) {
                    EucMessageKey key = next2.getKey();
                    IEucQuery iEucQuery = this.mCache.get(key);
                    Preconditions.checkState(iEucQuery != null, "No query in cache for id=" + key.getEucId() + ". Should not happen!");
                    iEucQuery.addDialogData(next2);
                }
            } catch (EucPersistenceException | IllegalStateException | NullPointerException e) {
                Log.e(LOG_TAG, "Unable to obtain dialogs data for type=" + eucType + " language=" + str + " from persistence: " + e);
            } catch (IllegalArgumentException e2) {
                String str3 = LOG_TAG;
                if (arrayList.isEmpty()) {
                    str2 = "idList";
                } else {
                    str2 = "mOwnIdentities list is empty - wrong argument in query to persistence: " + e2;
                }
                Log.e(str3, str2);
            }
            replaceDisplay(eucType, str);
        }
    }

    /* access modifiers changed from: package-private */
    public void displayQueries(Iterable<IEucQuery> iterable, String str) {
        for (IEucQuery next : iterable) {
            IDialogData dialogData = next.getDialogData(str);
            if (dialogData != null && (!dialogData.getSubject().isEmpty() || !dialogData.getText().isEmpty())) {
                this.mDisplayManager.display(next, str, createDisplayManagerRequestCallback(next));
            }
        }
    }

    private void replaceDisplay(EucType eucType, String str) {
        this.mDisplayManager.hideAllForType(eucType);
        displayQueries(this.mCache.getAllByType(eucType), str);
    }

    /* access modifiers changed from: package-private */
    public void sendResponse(IEucData iEucData, EucResponseData.Response response, String str) {
        IEucStoreAndForward.IResponseHandle iResponseHandle;
        if (str == null) {
            iResponseHandle = this.mStoreAndForward.sendResponse(iEucData, response, createSendResponseCallback());
        } else {
            iResponseHandle = this.mStoreAndForward.sendResponse(iEucData, response, str, createSendResponseCallback());
        }
        if (iResponseHandle != null) {
            this.mHandleMap.put(new EucMessageKey(iEucData.getId(), iEucData.getOwnIdentity(), iEucData.getType(), iEucData.getRemoteUri()), iResponseHandle);
        } else {
            Log.e(LOG_TAG, "Handle is null");
        }
    }

    /* access modifiers changed from: package-private */
    public IEucDisplayManager.IDisplayCallback createDisplayManagerRequestCallback(IEucQuery iEucQuery) {
        final IEucData eucData = iEucQuery.getEucData();
        final String id = eucData.getId();
        final EucType type = eucData.getType();
        final EucMessageKey eucMessageKey = new EucMessageKey(id, eucData.getOwnIdentity(), type, eucData.getRemoteUri());
        return new IEucDisplayManager.IDisplayCallback() {
            /* JADX WARNING: Can't wrap try/catch for region: R(2:21|22) */
            /* JADX WARNING: Code restructure failed: missing block: B:20:0x0064, code lost:
                r4 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
                r4 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.m902$$Nest$sfgetLOG_TAG();
                android.util.Log.e(r4, "Unable to change EUCs state in persistence for EUCR with id=" + r5);
                r4 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.m902$$Nest$sfgetLOG_TAG();
                com.sec.internal.log.IMSLog.s(r4, "Unable to change EUCs state in persistence for EUCR with key=" + r3);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:24:0x009c, code lost:
                r3.this$0.mCache.remove(r3);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a5, code lost:
                throw r4;
             */
            /* JADX WARNING: Failed to process nested try/catch */
            /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0066 */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onSuccess(com.sec.internal.ims.servicemodules.euc.data.EucResponseData.Response r4, java.lang.String r5) {
                /*
                    r3 = this;
                    int[] r0 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.AnonymousClass2.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.data.EucType r1 = r2     // Catch:{ EucPersistenceException -> 0x0066 }
                    int r1 = r1.ordinal()     // Catch:{ EucPersistenceException -> 0x0066 }
                    r0 = r0[r1]     // Catch:{ EucPersistenceException -> 0x0066 }
                    r1 = 1
                    if (r0 == r1) goto L_0x003b
                    r1 = 2
                    if (r0 == r1) goto L_0x003b
                    r1 = 3
                    if (r0 == r1) goto L_0x0024
                    r1 = 4
                    if (r0 == r1) goto L_0x0024
                    r4 = 5
                    if (r0 == r4) goto L_0x001a
                    goto L_0x005a
                L_0x001a:
                    java.lang.String r4 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.LOG_TAG     // Catch:{ EucPersistenceException -> 0x0066 }
                    java.lang.String r5 = "EULA is not handled here!"
                    android.util.Log.e(r4, r5)     // Catch:{ EucPersistenceException -> 0x0066 }
                    goto L_0x005a
                L_0x0024:
                    com.sec.internal.ims.servicemodules.euc.data.EucResponseData$Response r0 = com.sec.internal.ims.servicemodules.euc.data.EucResponseData.Response.ACCEPT     // Catch:{ EucPersistenceException -> 0x0066 }
                    boolean r4 = r4.equals(r0)     // Catch:{ EucPersistenceException -> 0x0066 }
                    java.lang.String r0 = "Only ok button expected for notification or acknowledgment!"
                    com.sec.internal.helper.Preconditions.checkState(r4, r0)     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow r4 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.this     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence r4 = r4.mEucPersistence     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.data.EucMessageKey r0 = r3     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.data.EucState r1 = com.sec.internal.ims.servicemodules.euc.data.EucState.ACCEPTED     // Catch:{ EucPersistenceException -> 0x0066 }
                    r4.updateEuc(r0, r1, r5)     // Catch:{ EucPersistenceException -> 0x0066 }
                    goto L_0x005a
                L_0x003b:
                    com.sec.internal.ims.servicemodules.euc.data.EucResponseData$Response r0 = com.sec.internal.ims.servicemodules.euc.data.EucResponseData.Response.ACCEPT     // Catch:{ EucPersistenceException -> 0x0066 }
                    boolean r4 = r4.equals(r0)     // Catch:{ EucPersistenceException -> 0x0066 }
                    if (r4 == 0) goto L_0x0046
                    com.sec.internal.ims.servicemodules.euc.data.EucState r4 = com.sec.internal.ims.servicemodules.euc.data.EucState.ACCEPTED_NOT_SENT     // Catch:{ EucPersistenceException -> 0x0066 }
                    goto L_0x004a
                L_0x0046:
                    com.sec.internal.ims.servicemodules.euc.data.EucState r4 = com.sec.internal.ims.servicemodules.euc.data.EucState.REJECTED_NOT_SENT     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.data.EucResponseData$Response r0 = com.sec.internal.ims.servicemodules.euc.data.EucResponseData.Response.DECLINE     // Catch:{ EucPersistenceException -> 0x0066 }
                L_0x004a:
                    com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow r1 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.this     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence r1 = r1.mEucPersistence     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.data.EucMessageKey r2 = r3     // Catch:{ EucPersistenceException -> 0x0066 }
                    r1.updateEuc(r2, r4, r5)     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow r4 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.this     // Catch:{ EucPersistenceException -> 0x0066 }
                    com.sec.internal.ims.servicemodules.euc.data.IEucData r1 = r4     // Catch:{ EucPersistenceException -> 0x0066 }
                    r4.sendResponse(r1, r0, r5)     // Catch:{ EucPersistenceException -> 0x0066 }
                L_0x005a:
                    com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow r4 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.this
                    com.sec.internal.ims.servicemodules.euc.cache.IEucCache r4 = r4.mCache
                    com.sec.internal.ims.servicemodules.euc.data.EucMessageKey r3 = r3
                    r4.remove(r3)
                    goto L_0x009b
                L_0x0064:
                    r4 = move-exception
                    goto L_0x009c
                L_0x0066:
                    java.lang.String r4 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.LOG_TAG     // Catch:{ all -> 0x0064 }
                    java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0064 }
                    r5.<init>()     // Catch:{ all -> 0x0064 }
                    java.lang.String r0 = "Unable to change EUCs state in persistence for EUCR with id="
                    r5.append(r0)     // Catch:{ all -> 0x0064 }
                    java.lang.String r0 = r5     // Catch:{ all -> 0x0064 }
                    r5.append(r0)     // Catch:{ all -> 0x0064 }
                    java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0064 }
                    android.util.Log.e(r4, r5)     // Catch:{ all -> 0x0064 }
                    java.lang.String r4 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.LOG_TAG     // Catch:{ all -> 0x0064 }
                    java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0064 }
                    r5.<init>()     // Catch:{ all -> 0x0064 }
                    java.lang.String r0 = "Unable to change EUCs state in persistence for EUCR with key="
                    r5.append(r0)     // Catch:{ all -> 0x0064 }
                    com.sec.internal.ims.servicemodules.euc.data.EucMessageKey r0 = r3     // Catch:{ all -> 0x0064 }
                    r5.append(r0)     // Catch:{ all -> 0x0064 }
                    java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0064 }
                    com.sec.internal.log.IMSLog.s(r4, r5)     // Catch:{ all -> 0x0064 }
                    goto L_0x005a
                L_0x009b:
                    return
                L_0x009c:
                    com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow r5 = com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.this
                    com.sec.internal.ims.servicemodules.euc.cache.IEucCache r5 = r5.mCache
                    com.sec.internal.ims.servicemodules.euc.data.EucMessageKey r3 = r3
                    r5.remove(r3)
                    throw r4
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.AnonymousClass1.onSuccess(com.sec.internal.ims.servicemodules.euc.data.EucResponseData$Response, java.lang.String):void");
            }
        };
    }

    /* renamed from: com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.ims.servicemodules.euc.data.EucType[] r0 = com.sec.internal.ims.servicemodules.euc.data.EucType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType = r0
                com.sec.internal.ims.servicemodules.euc.data.EucType r1 = com.sec.internal.ims.servicemodules.euc.data.EucType.PERSISTENT     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.servicemodules.euc.data.EucType r1 = com.sec.internal.ims.servicemodules.euc.data.EucType.VOLATILE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.servicemodules.euc.data.EucType r1 = com.sec.internal.ims.servicemodules.euc.data.EucType.ACKNOWLEDGEMENT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.servicemodules.euc.data.EucType r1 = com.sec.internal.ims.servicemodules.euc.data.EucType.NOTIFICATION     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.servicemodules.euc.data.EucType r1 = com.sec.internal.ims.servicemodules.euc.data.EucType.EULA     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow.AnonymousClass2.<clinit>():void");
        }
    }

    public void discard(String str) {
        Iterator<Map.Entry<EucMessageKey, IEucStoreAndForward.IResponseHandle>> it = this.mHandleMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry next = it.next();
            if (str.equals(((EucMessageKey) next.getKey()).getOwnIdentity())) {
                ((IEucStoreAndForward.IResponseHandle) next.getValue()).invalidate();
                it.remove();
            }
        }
        this.mDisplayManager.hideAllForOwnIdentity(str);
        this.mCache.clearAllForOwnIdentity(str);
        this.mOwnIdentities.remove(str);
    }
}
