package com.sec.internal.ims.servicemodules.euc.workflow;

import android.util.Log;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException;
import com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import java.util.Collections;
import java.util.List;

public class NotificationEucWorkflow extends BaseEucWorkflow {
    private static final String LOG_TAG = "NotificationEucWorkflow";
    private final IEucFactory mEucFactory;

    public IEucStoreAndForward.IResponseCallback createSendResponseCallback() {
        return null;
    }

    public /* bridge */ /* synthetic */ void discard(String str) {
        super.discard(str);
    }

    public /* bridge */ /* synthetic */ void start() {
        super.start();
    }

    public /* bridge */ /* synthetic */ void stop() {
        super.stop();
    }

    public NotificationEucWorkflow(IEucPersistence iEucPersistence, IEucDisplayManager iEucDisplayManager, IEucStoreAndForward iEucStoreAndForward, IEucFactory iEucFactory) {
        super(iEucPersistence, iEucDisplayManager, iEucStoreAndForward);
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(iEucFactory);
    }

    public void load(String str) {
        this.mOwnIdentities.add(str);
        EucType eucType = EucType.NOTIFICATION;
        List singletonList = Collections.singletonList(eucType);
        try {
            IEucPersistence iEucPersistence = this.mEucPersistence;
            EucState eucState = EucState.NONE;
            Iterable<IEucQuery> combine = this.mEucFactory.combine(iEucPersistence.getAllEucs(eucState, eucType, str), this.mEucPersistence.getDialogsByTypes(eucState, singletonList, this.mLanguageCode, str));
            loadToCache(combine);
            displayQueries(combine, this.mLanguageCode);
        } catch (EucPersistenceException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "Unable to obtain EUCs from persistence: " + e);
        }
    }

    public void handleIncomingEuc(IEucQuery iEucQuery) {
        this.mCache.put(iEucQuery);
        try {
            this.mEucPersistence.insertEuc(iEucQuery.getEucData());
            this.mEucPersistence.insertDialogs(iEucQuery);
        } catch (EucPersistenceException e) {
            String str = LOG_TAG;
            Log.e(str, "Unable to store EUC with key=" + iEucQuery.getEucData().getKey() + " in persistence: " + e);
        }
        this.mDisplayManager.display(iEucQuery, this.mLanguageCode, createDisplayManagerRequestCallback(iEucQuery));
    }

    public void changeLanguage(String str) {
        this.mLanguageCode = str;
        changeLanguage(str, EucType.NOTIFICATION);
    }
}
