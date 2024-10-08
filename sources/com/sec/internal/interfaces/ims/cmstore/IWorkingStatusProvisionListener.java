package com.sec.internal.interfaces.ims.cmstore;

import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;

public interface IWorkingStatusProvisionListener {

    public enum WorkingStatus {
        PROVISION_SUCCESS,
        OMA_PROVISION_FAILED,
        SEND_TOCLOUD_UNSYNC,
        NET_WORK_STATUS_CHANGED,
        DEFAULT_MSGAPP_CHGTO_NATIVE,
        RESTART_SERVICE,
        BUFFERDB_CLEAN,
        MAILBOX_MIGRATION_RESET,
        UPDATE_CMS_CONFIG
    }

    void onChannelLifetimeUpdateComplete();

    void onChannelStateReset();

    void onCleanBufferDbRequired();

    void onCloudSyncWorkingStopped();

    void onCmsRegistrationCompletedEvent();

    void onDeviceFlagUpdateSchedulerStarted();

    void onEsimHotswap();

    void onInitialDBSyncCompleted();

    void onMailBoxMigrationReset();

    void onNetworkChangeDetected();

    void onOmaFailExceedMaxCount();

    void onOmaProvisionFailed(ParamOMAresponseforBufDB paramOMAresponseforBufDB, long j);

    void onProvisionSuccess();

    void onRestartService();

    void onRestartService(boolean z);

    void onStartFcmRetry();

    void onUserDeleteAccount(boolean z);

    void onVVMNormalSyncComplete(boolean z);

    void pauseService();

    void stopService();
}
