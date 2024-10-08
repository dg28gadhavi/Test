package com.sec.internal.interfaces.ims.cmstore;

import android.os.Handler;
import com.sec.internal.ims.cmstore.helper.SyncParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;

public interface IDeviceDataChangeListener {
    boolean isNativeMsgAppDefault();

    boolean isNormalVVMSyncing();

    void onInitialDBCopyDone();

    void onMailBoxResetBufferDbDone();

    void onWipeOutResetSyncHandler();

    void registerForUpdateFromCloud(Handler handler, int i, Object obj);

    void registerForUpdateOfWorkingStatus(Handler handler, int i, Object obj);

    void sendAppSync(SyncParam syncParam, boolean z);

    void sendDeviceInitialSyncDownload(BufferDBChangeParamList bufferDBChangeParamList);

    void sendDeviceNormalSyncDownload(BufferDBChangeParamList bufferDBChangeParamList);

    void sendDeviceUpdate(BufferDBChangeParamList bufferDBChangeParamList);

    void sendDeviceUpload(BufferDBChangeParamList bufferDBChangeParamList);

    void sendGetVVMQuotaInfo(BufferDBChangeParamList bufferDBChangeParamList);

    void setVVMSyncState(boolean z);

    void stopAppSync(SyncParam syncParam);
}
