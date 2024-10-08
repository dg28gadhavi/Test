package com.sec.internal.ims.cmstore;

import android.content.Context;
import android.os.Binder;
import android.os.RemoteCallbackList;
import com.sec.ims.ICentralMsgStoreServiceListener;
import com.sec.internal.ims.cmstore.adapters.McsRetryMapAdapter;
import com.sec.internal.ims.cmstore.adapters.RetryMapAdapter;
import com.sec.internal.ims.cmstore.adapters.RetryStackAdapter;
import com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler;
import com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CmsHttpController;
import com.sec.internal.ims.gba.GbaServiceModule;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.cmstore.IMcsFcmPushNotificationListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.ArrayList;

public interface MessageStoreClient {
    Binder getBinder();

    int getClientID();

    CloudMessageBufferSchedulingHandler getCloudMessageBufferSchedulingHandler();

    CloudMessageStrategyManager getCloudMessageStrategyManager();

    Context getContext();

    String getCurrentIMSI();

    CmsHttpController getHttpController();

    ArrayList<IMcsFcmPushNotificationListener> getMcsFcmPushNotificationListener();

    RemoteCallbackList<ICentralMsgStoreServiceListener> getMcsProvisioningListener();

    McsRetryMapAdapter getMcsRetryMapAdapter();

    NetAPIWorkingStatusController getNetAPIWorkingStatusController();

    CloudMessagePreferenceManager getPrerenceManager();

    boolean getProvisionStatus();

    WorkflowMcs getProvisionWorkFlow();

    RetryMapAdapter getRetryMapAdapter();

    RetryStackAdapter getRetryStackAdapter();

    ISimManager getSimManager();

    boolean isRcsRegistered();

    void notifyAppNetworkOperationResult(boolean z);

    void onCreate(IImsFramework iImsFramework, GbaServiceModule gbaServiceModule);

    void onDestroy();

    void registerCmsProvisioningListener(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener, boolean z);

    void setMcsFcmPushNotificationListener(IMcsFcmPushNotificationListener iMcsFcmPushNotificationListener);

    void setProvisionStatus(boolean z);

    void unregisterCmsProvisioningListener(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener);

    boolean updateDelay(int i, long j);

    void updateEvent(int i);
}
