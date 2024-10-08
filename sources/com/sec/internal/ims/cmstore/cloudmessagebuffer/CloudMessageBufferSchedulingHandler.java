package com.sec.internal.ims.cmstore.cloudmessagebuffer;

import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.SyncParam;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUriParam;
import com.sec.internal.ims.cmstore.params.DeviceSessionPartcptsUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValueList;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.McsNotificationListContainer;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageBufferEvent;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.interfaces.ims.cmstore.IMcsFcmPushNotificationListener;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.McsLargePollingNotification;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import com.sec.internal.omanetapi.nms.data.NmsEvent;
import com.sec.internal.omanetapi.nms.data.NmsEventList;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import com.sec.internal.omanetapi.nms.data.Reference;
import java.util.Iterator;
import java.util.Map;

public class CloudMessageBufferSchedulingHandler extends CloudMessageBufferDBHelper implements ICloudMessageBufferEvent {
    /* access modifiers changed from: private */
    public String TAG = CloudMessageBufferSchedulingHandler.class.getSimpleName();
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;

    private void onBufferDBReadBatch(String str) {
    }

    private void onUpdateFromDeviceLegacy() {
    }

    public CloudMessageBufferSchedulingHandler(Looper looper, MessageStoreClient messageStoreClient, IDeviceDataChangeListener iDeviceDataChangeListener, IBufferDBEventListener iBufferDBEventListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, boolean z) {
        super(looper, messageStoreClient, iDeviceDataChangeListener, iBufferDBEventListener, z);
        String str = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str;
        Log.d(str, "onCreate");
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        registerRegistrants();
        registerNmsEventListPushListener();
    }

    private void registerNmsEventListPushListener() {
        this.mStoreClient.setMcsFcmPushNotificationListener(new IMcsFcmPushNotificationListener() {
            public void largePollingPushNotification(McsLargePollingNotification mcsLargePollingNotification) {
            }

            public void syncBlockfilterPushNotification(String str) {
            }

            public void syncConfigPushNotification(String str) {
            }

            public void syncContactPushNotification(String str) {
            }

            public void syncStatusPushNotification(String str) {
            }

            public void nmsEventListPushNotification(NmsEventList nmsEventList) {
                boolean z;
                IMSLog.i(CloudMessageBufferSchedulingHandler.this.TAG, "nmsEventListPushNotification");
                long oMASubscriptionIndex = CloudMessageBufferSchedulingHandler.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                long longValue = nmsEventList.index.longValue();
                int i = (longValue > (oMASubscriptionIndex + 1) ? 1 : (longValue == (oMASubscriptionIndex + 1) ? 0 : -1));
                if (i > 0) {
                    z = McsNotificationListContainer.getInstance(CloudMessageBufferSchedulingHandler.this.mStoreClient.getClientID()).isEmpty();
                    McsNotificationListContainer.getInstance(CloudMessageBufferSchedulingHandler.this.mStoreClient.getClientID()).insertContainer(Long.valueOf(longValue), nmsEventList, CloudMessageBufferSchedulingHandler.this.mStoreClient.getClientID(), Long.valueOf(oMASubscriptionIndex));
                } else {
                    if (i == 0) {
                        CloudMessageBufferSchedulingHandler.this.mStoreClient.getPrerenceManager().saveOMASubscriptionRestartToken(nmsEventList.restartToken);
                        CloudMessageBufferSchedulingHandler.this.mStoreClient.getPrerenceManager().saveOMASubscriptionIndex(longValue);
                        CloudMessageBufferSchedulingHandler.this.onNativeChannelReceived(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setMcsNmsEventList(nmsEventList).build());
                        long oMASubscriptionIndex2 = CloudMessageBufferSchedulingHandler.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                        while (!McsNotificationListContainer.getInstance(CloudMessageBufferSchedulingHandler.this.mStoreClient.getClientID()).isEmpty() && McsNotificationListContainer.getInstance(CloudMessageBufferSchedulingHandler.this.mStoreClient.getClientID()).peekFirstIndex() == oMASubscriptionIndex2 + 1) {
                            Map.Entry<Long, NmsEventList> popFirstEntry = McsNotificationListContainer.getInstance(CloudMessageBufferSchedulingHandler.this.mStoreClient.getClientID()).popFirstEntry();
                            if (popFirstEntry == null) {
                                Log.e(CloudMessageBufferSchedulingHandler.this.TAG, "handleNmsEvent: firstEntry is null");
                            } else {
                                NmsEventList value = popFirstEntry.getValue();
                                String str = value.restartToken;
                                long longValue2 = value.index.longValue();
                                CloudMessageBufferSchedulingHandler.this.mStoreClient.getPrerenceManager().saveOMASubscriptionRestartToken(str);
                                CloudMessageBufferSchedulingHandler.this.mStoreClient.getPrerenceManager().saveOMASubscriptionIndex(longValue2);
                                CloudMessageBufferSchedulingHandler.this.onNativeChannelReceived(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setMcsNmsEventList(value).build());
                                String r9 = CloudMessageBufferSchedulingHandler.this.TAG;
                                IMSLog.i(r9, "onComplete: Process nmsEventList from the NotificationListContainer, savedIndex: " + oMASubscriptionIndex2 + " currIndex:" + longValue2);
                                oMASubscriptionIndex2 = CloudMessageBufferSchedulingHandler.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                                if (McsNotificationListContainer.getInstance(CloudMessageBufferSchedulingHandler.this.mStoreClient.getClientID()).isEmpty()) {
                                    Log.i(CloudMessageBufferSchedulingHandler.this.TAG, "NotificationListContainer is empty, all the disordered notifications have been proceeded, remove UPDATE_SUBSCRIPTION_CHANNEL_DELAY");
                                    CloudMessageBufferSchedulingHandler.this.mStoreClient.getNetAPIWorkingStatusController().removeUpdateSubscriptionChannelEvent();
                                }
                            }
                        }
                    }
                    z = false;
                }
                if (z) {
                    CloudMessageBufferSchedulingHandler.this.mStoreClient.getNetAPIWorkingStatusController().updateDelayedSubscriptionChannel();
                }
            }

            public void syncMessagePushNotification(String str, int i) {
                String userTelCtn = CloudMessageBufferSchedulingHandler.this.mStoreClient.getPrerenceManager().getUserTelCtn();
                MultiLineScheduler multiLineScheduler = CloudMessageBufferSchedulingHandler.this.mMultiLnScheduler;
                SyncMsgType syncMsgType = SyncMsgType.DEFAULT;
                int lineInitSyncStatus = multiLineScheduler.getLineInitSyncStatus(userTelCtn, syncMsgType);
                String r2 = CloudMessageBufferSchedulingHandler.this.TAG;
                IMSLog.i(r2, "syncMessagePushNotification  " + str + " initStatus:" + lineInitSyncStatus);
                if (!TextUtils.equals(str, "init")) {
                    return;
                }
                if (lineInitSyncStatus != OMASyncEventType.INITIAL_SYNC_COMPLETE.getId()) {
                    Log.i(CloudMessageBufferSchedulingHandler.this.TAG, "initial sync not complete yet, no need to upload data");
                    CloudMessageBufferSchedulingHandler.this.mMultiLnScheduler.updateLineUploadStatus(userTelCtn, syncMsgType, OMASyncEventType.INITIAL_UPLOAD_PENDING.getId());
                    return;
                }
                CloudMessageBufferSchedulingHandler cloudMessageBufferSchedulingHandler = CloudMessageBufferSchedulingHandler.this;
                cloudMessageBufferSchedulingHandler.notifyNetAPIUploadMessages(cloudMessageBufferSchedulingHandler.mStoreClient.getPrerenceManager().getUserTelCtn(), SyncMsgType.MESSAGE, false);
            }
        });
    }

    private void registerRegistrants() {
        Log.d(this.TAG, "registerRegistrants()");
        this.mDeviceDataChangeListener.registerForUpdateFromCloud(this, 3, (Object) null);
        this.mDeviceDataChangeListener.registerForUpdateOfWorkingStatus(this, 4, (Object) null);
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        String str = this.TAG;
        Log.i(str, "message: " + message.what);
        switch (message.what) {
            case 1:
                startInitialSyncDBCopyTask();
                return;
            case 3:
                onUpdateFromCloud((ParamOMAresponseforBufDB) ((AsyncResult) message.obj).result);
                return;
            case 4:
                onWorkingStatusChanged((IWorkingStatusProvisionListener.WorkingStatus) ((AsyncResult) message.obj).result);
                return;
            case 6:
                onUpdateFromDeviceLegacy();
                return;
            case 7:
                DeviceSessionPartcptsUpdateParam deviceSessionPartcptsUpdateParam = (DeviceSessionPartcptsUpdateParam) message.obj;
                int i = deviceSessionPartcptsUpdateParam.mTableindex;
                if (i == 2) {
                    this.mRcsScheduler.onUpdateFromDeviceSessionPartcpts(deviceSessionPartcptsUpdateParam);
                    return;
                } else if (i == 10) {
                    this.mRcsScheduler.onUpdateFromDeviceSession(deviceSessionPartcptsUpdateParam);
                    return;
                } else {
                    return;
                }
            case 8:
                onUpdateFromDeviceMsgAppFetch((DeviceMsgAppFetchUpdateParam) message.obj);
                return;
            case 11:
                handleRCSDbReady();
                return;
            case 12:
                onServiceRestarted();
                return;
            case 13:
                handleReceivedMessageJson((String) message.obj);
                return;
            case 14:
                handleSentMessageJson((String) message.obj);
                return;
            case 15:
                handleReadMessageJson((String) message.obj);
                return;
            case 16:
                handleUnReadMessageJson((String) message.obj);
                return;
            case 17:
                handleDeleteMessageJson((String) message.obj);
                return;
            case 18:
                handleUploadMessageJson((String) message.obj);
                return;
            case 19:
                handleDownloadMessageJson((String) message.obj);
                return;
            case 20:
                handleWipeOutMessageJson((String) message.obj);
                return;
            case 22:
                handleBufferDbReadMessageJson((String) message.obj);
                return;
            case 23:
                onBufferDBReadBatch((String) message.obj);
                return;
            case 24:
                handleStartSync((ParamAppJsonValueList) message.obj, true);
                return;
            case 25:
                handleStopSync((ParamAppJsonValueList) message.obj);
                return;
            case 26:
                onUpdateFromDeviceMsgAppFetchFailed((DeviceMsgAppFetchUpdateParam) message.obj);
                return;
            case 27:
                appFetchingFailedMsg(String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail.getId()));
                return;
            case 28:
                fetchingPendingMsg();
                return;
            case 29:
                handleStartSync((ParamAppJsonValueList) message.obj, false);
                return;
            case 30:
                handleUpdateFromDeviceFtUriFetch((DeviceMsgAppFetchUriParam) message.obj);
                return;
            case 31:
                handleCancelMessageJson((String) message.obj);
                return;
            case 32:
                handleStarredMessageJson((String) message.obj);
                return;
            case 33:
                handleUnStarredMessageJson((String) message.obj);
                return;
            default:
                return;
        }
    }

    private void onServiceRestarted() {
        this.mProvisionSuccess = false;
        setBufferDBLoaded(false);
    }

    private void handleStartSync(ParamAppJsonValueList paramAppJsonValueList, boolean z) {
        String str = this.TAG;
        IMSLog.s(str, "handleStartSync: " + paramAppJsonValueList + " isFullSync: " + z);
        if (paramAppJsonValueList != null) {
            Iterator<ParamAppJsonValue> it = paramAppJsonValueList.mOperationList.iterator();
            while (it.hasNext()) {
                ParamAppJsonValue next = it.next();
                String str2 = next.mLine;
                String str3 = next.mAppType;
                String str4 = next.mDataType;
                if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(str3) && CloudMessageProviderContract.DataTypes.MSGAPP_ALL.equalsIgnoreCase(str4)) {
                    MultiLineScheduler multiLineScheduler = this.mMultiLnScheduler;
                    SyncMsgType syncMsgType = SyncMsgType.MESSAGE;
                    multiLineScheduler.insertNewLine(str2, syncMsgType);
                    this.mDeviceDataChangeListener.sendAppSync(new SyncParam(str2, syncMsgType), z);
                } else if ("VVMDATA".equalsIgnoreCase(str3) && "VVMDATA".equalsIgnoreCase(str4)) {
                    String str5 = this.TAG;
                    Log.i(str5, "VM Normal Sync Processing: " + this.mDeviceDataChangeListener.isNormalVVMSyncing());
                    if (!this.mDeviceDataChangeListener.isNormalVVMSyncing()) {
                        MultiLineScheduler multiLineScheduler2 = this.mMultiLnScheduler;
                        SyncMsgType syncMsgType2 = SyncMsgType.VM;
                        multiLineScheduler2.insertNewLine(str2, syncMsgType2);
                        this.mDeviceDataChangeListener.sendAppSync(new SyncParam(str2, syncMsgType2), z);
                    } else {
                        notifyVVMNormalSyncStatus(str4, str2, z);
                    }
                } else if ("VVMDATA".equalsIgnoreCase(str3) && CloudMessageProviderContract.DataTypes.VVMGREETING.equalsIgnoreCase(str4)) {
                    String str6 = this.TAG;
                    Log.i(str6, "Greeting Normal Sync Processing: " + this.mDeviceDataChangeListener.isNormalVVMSyncing());
                    if (!this.mDeviceDataChangeListener.isNormalVVMSyncing()) {
                        this.mVVMScheduler.wipeOutData(18, str2);
                        this.mDeviceDataChangeListener.sendAppSync(new SyncParam(str2, SyncMsgType.VM_GREETINGS), z);
                    } else {
                        notifyVVMNormalSyncStatus(str4, str2, z);
                    }
                }
            }
        }
    }

    private void notifyVVMNormalSyncStatus(String str, String str2, boolean z) {
        if ("VVMDATA".equalsIgnoreCase(str)) {
            this.mVVMScheduler.notifyInitialSyncStatus("VVMDATA", "VVMDATA", str2, CloudMessageBufferDBConstants.InitialSyncStatusFlag.IGNORED, z);
        } else if (CloudMessageProviderContract.DataTypes.VVMGREETING.equalsIgnoreCase(str)) {
            this.mVVMScheduler.notifyInitialSyncStatus("VVMDATA", CloudMessageProviderContract.DataTypes.VVMGREETING, str2, CloudMessageBufferDBConstants.InitialSyncStatusFlag.IGNORED, z);
        }
    }

    private void handleStopSync(ParamAppJsonValueList paramAppJsonValueList) {
        String str = this.TAG;
        IMSLog.s(str, "handleStopSync: " + paramAppJsonValueList);
        if (paramAppJsonValueList != null) {
            Iterator<ParamAppJsonValue> it = paramAppJsonValueList.mOperationList.iterator();
            while (it.hasNext()) {
                ParamAppJsonValue next = it.next();
                String str2 = next.mLine;
                String str3 = next.mAppType;
                if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(str3) || CloudMessageProviderContract.ApplicationTypes.RCSDATA.equalsIgnoreCase(str3)) {
                    MultiLineScheduler multiLineScheduler = this.mMultiLnScheduler;
                    SyncMsgType syncMsgType = SyncMsgType.MESSAGE;
                    multiLineScheduler.deleteLine(str2, syncMsgType);
                    this.mDeviceDataChangeListener.stopAppSync(new SyncParam(str2, syncMsgType));
                } else if ("VVMDATA".equalsIgnoreCase(str3)) {
                    MultiLineScheduler multiLineScheduler2 = this.mMultiLnScheduler;
                    SyncMsgType syncMsgType2 = SyncMsgType.VM;
                    multiLineScheduler2.deleteLine(str2, syncMsgType2);
                    this.mDeviceDataChangeListener.stopAppSync(new SyncParam(str2, syncMsgType2));
                }
            }
        }
    }

    private void onWorkingStatusChanged(IWorkingStatusProvisionListener.WorkingStatus workingStatus) {
        String str = this.TAG;
        Log.i(str, "onWorkingStatusChanged: " + workingStatus);
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus[workingStatus.ordinal()];
        if (i == 1) {
            handleProvisionSuccess();
        } else if (i != 3) {
            switch (i) {
                case 5:
                    handleDftMsgAppChangedToNative();
                    return;
                case 6:
                    restartService();
                    return;
                case 7:
                    cleanAllBufferDB();
                    return;
                case 8:
                    onMailBoxReset();
                    return;
                case 9:
                    this.mSummaryQuery.onUpdateCmsConfigInitSyncDataTtl();
                    this.mSmsScheduler.onUpdateCmsConfig();
                    this.mMmsScheduler.onUpdateCmsConfig();
                    this.mRcsScheduler.onUpdateCmsConfig();
                    return;
                default:
                    return;
            }
        } else {
            onSendCloudUnSyncedUpdate();
        }
    }

    private void handleDftMsgAppChangedToNative() {
        int lineInitSyncStatus = this.mMultiLnScheduler.getLineInitSyncStatus(this.mStoreClient.getPrerenceManager().getUserTelCtn(), SyncMsgType.DEFAULT);
        String str = this.TAG;
        Log.i(str, "handleDftMsgAppChangedToNative initSyncStatus: " + lineInitSyncStatus);
        if (this.mIsCmsEnabled || lineInitSyncStatus == OMASyncEventType.INITIAL_SYNC_COMPLETE.getId()) {
            if (!this.mIsCmsEnabled) {
                this.mIsGoforwardSync = true;
            }
            startGoForwardSyncDbCopyTask();
        }
    }

    private void handleProvisionSuccess() {
        this.mProvisionSuccess = true;
        String userTelCtn = this.mStoreClient.getPrerenceManager().getUserTelCtn();
        MultiLineScheduler multiLineScheduler = this.mMultiLnScheduler;
        SyncMsgType syncMsgType = SyncMsgType.DEFAULT;
        int lineInitSyncStatus = multiLineScheduler.getLineInitSyncStatus(userTelCtn, syncMsgType);
        OMASyncEventType valueOf = OMASyncEventType.valueOf(lineInitSyncStatus);
        String str = this.TAG;
        Log.i(str, "check initial sync status: " + lineInitSyncStatus + "event: " + valueOf + " linenum:" + IMSLog.checker(userTelCtn));
        if (valueOf == null) {
            valueOf = OMASyncEventType.DEFAULT;
        }
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[valueOf.ordinal()];
        if (i == 1) {
            this.mDeviceDataChangeListener.onInitialDBCopyDone();
            onSendCloudUnSyncedUpdate();
            onSendDeviceUnSyncedUpdate();
            this.mBufferDBloaded = this.mStoreClient.getPrerenceManager().getBufferDbLoaded();
            int lineUploadStatus = this.mMultiLnScheduler.getLineUploadStatus(userTelCtn, syncMsgType);
            String str2 = this.TAG;
            IMSLog.i(str2, "upload status: " + lineUploadStatus);
            if (!this.mIsCmsEnabled) {
                return;
            }
            if (lineUploadStatus == OMASyncEventType.INITIAL_UPLOAD_PENDING.getId() || lineUploadStatus == OMASyncEventType.INITIAL_UPLOAD_STARTED.getId()) {
                notifyNetAPIUploadMessages(userTelCtn, syncMsgType, false);
            }
        } else if (i == 2) {
            this.mDeviceDataChangeListener.onInitialDBCopyDone();
            onSendUnDownloadedMessage(userTelCtn, syncMsgType, false, CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId());
            this.mBufferDBloaded = this.mStoreClient.getPrerenceManager().getBufferDbLoaded();
        } else if (i == 3 || i == 4) {
            startInitialDBCopy();
            this.mDeviceDataChangeListener.onInitialDBCopyDone();
        } else {
            this.mDeviceDataChangeListener.onInitialDBCopyDone();
        }
    }

    private void handleRCSDbReady() {
        this.mRCSDbReady = true;
        resetImsi();
        startInitialDBCopy();
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a7  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00d1  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleUpdateFromDeviceFtUriFetch(com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUriParam r14) {
        /*
            r13 = this;
            java.lang.String r0 = r13.TAG
            java.lang.String r1 = "handleUpdateFromDeviceFtUriFetch"
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r0 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList
            r0.<init>()
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r13.mStoreClient
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r1 = r1.getPrerenceManager()
            java.lang.String r1 = r1.getUserTelCtn()
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri
            int r9 = r2.getId()
            long r4 = r14.mBufferRowId
            int r2 = r14.mTableindex
            r3 = 1
            if (r2 == r3) goto L_0x007f
            r3 = 4
            if (r2 == r3) goto L_0x005d
            r3 = 14
            if (r2 == r3) goto L_0x007f
            r3 = 11
            if (r2 == r3) goto L_0x007f
            r3 = 12
            if (r2 == r3) goto L_0x007f
            r3 = 17
            if (r2 == r3) goto L_0x003d
            r3 = 18
            if (r2 == r3) goto L_0x003d
            r2 = -1
            goto L_0x00a5
        L_0x003d:
            com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler r2 = r13.mVVMScheduler
            int r10 = r2.queryVVMPDUActionStatus(r4)
            com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler r2 = r13.mVVMScheduler
            r2.onUpdateFromDeviceFtUriFetch(r14)
            if (r10 != r9) goto L_0x00a4
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r11 = r0.mChangelst
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r12 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam
            r3 = 17
            boolean r6 = r13.mIsGoforwardSync
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r13.mStoreClient
            r2 = r12
            r7 = r1
            r2.<init>(r3, r4, r6, r7, r8)
            r11.add(r12)
            goto L_0x00a4
        L_0x005d:
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r2 = r13.mMmsScheduler
            int r8 = r2.queryMMSPDUActionStatus(r4)
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r2 = r13.mMmsScheduler
            boolean r2 = r2.onUpdateFromDeviceFtUriFetch(r14)
            if (r2 == 0) goto L_0x007d
            if (r8 != r9) goto L_0x007d
            java.lang.String r2 = r13.TAG
            java.lang.String r3 = "all MMS parts updated, add parts to download list"
            android.util.Log.d(r2, r3)
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r2 = r13.mMmsScheduler
            boolean r7 = r13.mIsGoforwardSync
            r3 = r0
            r6 = r1
            r2.addMmsPartDownloadList(r3, r4, r6, r7)
        L_0x007d:
            r2 = r8
            goto L_0x00a5
        L_0x007f:
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r2 = r13.mRcsScheduler
            int r10 = r2.queryRCSPDUActionStatus(r4)
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r2 = r13.mRcsScheduler
            r2.onUpdateFromDeviceFtUriFetch(r14)
            if (r10 != r9) goto L_0x00a4
            java.lang.String r2 = r13.TAG
            java.lang.String r3 = "all RCS parts updated, add parts to download list"
            android.util.Log.d(r2, r3)
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r11 = r0.mChangelst
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r12 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam
            r3 = 1
            boolean r6 = r13.mIsGoforwardSync
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r13.mStoreClient
            r2 = r12
            r7 = r1
            r2.<init>(r3, r4, r6, r7, r8)
            r11.add(r12)
        L_0x00a4:
            r2 = r10
        L_0x00a5:
            if (r2 != r9) goto L_0x00d1
            java.lang.String r14 = r13.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "NormalSync action with listsize : "
            r1.append(r2)
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r2 = r0.mChangelst
            int r2 = r2.size()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r14, r1)
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r14 = r0.mChangelst
            int r14 = r14.size()
            if (r14 <= 0) goto L_0x00f7
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r13 = r13.mDeviceDataChangeListener
            r13.sendDeviceNormalSyncDownload(r0)
            goto L_0x00f7
        L_0x00d1:
            boolean r0 = r13.mIsCmsEnabled
            if (r0 == 0) goto L_0x00f0
            int r0 = r14.mTableindex
            r2 = 6
            if (r0 != r2) goto L_0x00e8
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r0 = r13.mMmsScheduler
            long r2 = r14.mImsPartId
            boolean r0 = r0.queryPartType(r2)
            if (r0 == 0) goto L_0x00e8
            r13.onNotifyToMsgAppFetched(r1, r14)
            goto L_0x00f7
        L_0x00e8:
            java.lang.String r0 = r14.mAppType
            int r14 = r14.mTableindex
            r13.downloadMessageOnFetchUrlSuccess(r1, r0, r14)
            goto L_0x00f7
        L_0x00f0:
            java.lang.String r0 = r14.mAppType
            int r14 = r14.mTableindex
            r13.downloadMessageOnFetchUrlSuccess(r1, r0, r14)
        L_0x00f7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler.handleUpdateFromDeviceFtUriFetch(com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUriParam):void");
    }

    private void onNotifyToMsgAppFetched(String str, DeviceMsgAppFetchUriParam deviceMsgAppFetchUriParam) {
        Log.d(this.TAG, "onNotifyToMsgAppFetched");
        int lineInitSyncStatus = this.mMultiLnScheduler.getLineInitSyncStatus(this.mStoreClient.getPrerenceManager().getUserTelCtn(), SyncMsgType.DEFAULT);
        if (deviceMsgAppFetchUriParam.mTableindex == 6) {
            this.mMmsScheduler.onMmsPayloadUpdateWithDB(deviceMsgAppFetchUriParam.mImsPartId, str);
            if ((this.mMmsScheduler.queryPendingFetchForce() == 0 && this.mMmsScheduler.queryPendingUrlFetch() == 0) && lineInitSyncStatus != OMASyncEventType.INITIAL_SYNC_COMPLETE.getId()) {
                processDownloadComplete(str);
            }
        }
    }

    private void downloadMessageOnFetchUrlSuccess(String str, String str2, int i) {
        int queryPendingUrlFetch = this.mRcsScheduler.queryPendingUrlFetch();
        int queryPendingUrlFetch2 = this.mMmsScheduler.queryPendingUrlFetch();
        int queryPendingVVMUrlFetch = this.mVVMScheduler.queryPendingVVMUrlFetch(17);
        int queryPendingVVMUrlFetch2 = this.mVVMScheduler.queryPendingVVMUrlFetch(18);
        String str3 = this.TAG;
        Log.i(str3, "downloadMessageOnFetchUrlSuccess pendingMMS: " + queryPendingUrlFetch2 + " pendingRCS: " + queryPendingUrlFetch + " pendingVVM : " + queryPendingVVMUrlFetch + " pendingVVMGreetingcount: " + queryPendingVVMUrlFetch2);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(str2)) {
            if (this.mIsCmsEnabled) {
                if (queryPendingUrlFetch2 == 0 && queryPendingUrlFetch == 0) {
                    Log.i(this.TAG, "downloadMessageOnFetchUrlSuccess onSendUnDownloadedMessage for RCS Message");
                    onSendUnDownloadedMessage(str, SyncMsgType.MESSAGE, false, CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId());
                }
            } else if (queryPendingUrlFetch2 == 0 && queryPendingUrlFetch == 0) {
                onSendUnDownloadedMessage(str, SyncMsgType.MESSAGE, false, CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId());
            }
        } else if (queryPendingVVMUrlFetch == 0 && i == 17) {
            onSendUnDownloadedMessage(str, SyncMsgType.VM, false, CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId());
        } else if (queryPendingVVMUrlFetch2 == 0 && i == 18) {
            onSendUnDownloadedMessage(str, SyncMsgType.VM_GREETINGS, false, CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId());
        }
    }

    private void onUpdateFromDeviceMsgAppFetch(DeviceMsgAppFetchUpdateParam deviceMsgAppFetchUpdateParam) {
        int i = deviceMsgAppFetchUpdateParam.mTableindex;
        if (!(i == 1 || i == 14)) {
            if (i == 3) {
                this.mSmsScheduler.onUpdateFromDeviceMsgAppFetch(deviceMsgAppFetchUpdateParam, this.mIsGoforwardSync);
                return;
            } else if (i == 4) {
                this.mMmsScheduler.onUpdateFromDeviceMsgAppFetch(deviceMsgAppFetchUpdateParam, this.mIsGoforwardSync);
                return;
            } else if (!(i == 11 || i == 12)) {
                switch (i) {
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                        this.mVVMScheduler.onUpdateFromDeviceMsgAppFetch(deviceMsgAppFetchUpdateParam, this.mIsGoforwardSync);
                        return;
                    default:
                        return;
                }
            }
        }
        this.mRcsScheduler.onUpdateFromDeviceMsgAppFetch(deviceMsgAppFetchUpdateParam, this.mIsGoforwardSync);
    }

    private void onUpdateFromDeviceMsgAppFetchFailed(DeviceMsgAppFetchUpdateParam deviceMsgAppFetchUpdateParam) {
        String str = this.TAG;
        Log.d(str, "onUpdateFromDeviceMsgAppFetchFailed " + deviceMsgAppFetchUpdateParam);
        int i = deviceMsgAppFetchUpdateParam.mTableindex;
        if (!(i == 1 || i == 14)) {
            if (i == 3) {
                this.mSmsScheduler.onUpdateFromDeviceMsgAppFetchFailed(deviceMsgAppFetchUpdateParam);
                return;
            } else if (i == 4) {
                this.mMmsScheduler.onUpdateFromDeviceMsgAppFetchFailed(deviceMsgAppFetchUpdateParam);
                return;
            } else if (!(i == 11 || i == 12)) {
                return;
            }
        }
        this.mRcsScheduler.onUpdateFromDeviceMsgAppFetchFailed(deviceMsgAppFetchUpdateParam);
    }

    private void handleSearchObject(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        this.mMultiLnScheduler.updateLineInitsyncStatus(paramOMAresponseforBufDB.getLine(), paramOMAresponseforBufDB.getSyncMsgType(), paramOMAresponseforBufDB.getSearchCursor(), paramOMAresponseforBufDB.getOMASyncEventType().getId());
        if (SyncMsgType.DEFAULT.equals(paramOMAresponseforBufDB.getSyncMsgType()) || SyncMsgType.MESSAGE.equals(paramOMAresponseforBufDB.getSyncMsgType())) {
            this.mSmsScheduler.notifyInitialSyncStatus(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MSGAPP_ALL, paramOMAresponseforBufDB.getLine(), CloudMessageBufferDBConstants.InitialSyncStatusFlag.START, paramOMAresponseforBufDB.getIsFullSync());
        } else if (SyncMsgType.VM.equals(paramOMAresponseforBufDB.getSyncMsgType())) {
            this.mVVMScheduler.notifyInitialSyncStatus("VVMDATA", "VVMDATA", paramOMAresponseforBufDB.getLine(), CloudMessageBufferDBConstants.InitialSyncStatusFlag.START, paramOMAresponseforBufDB.getIsFullSync());
        } else if (SyncMsgType.VM_GREETINGS.equals(paramOMAresponseforBufDB.getSyncMsgType())) {
            this.mVVMScheduler.notifyInitialSyncStatus("VVMDATA", CloudMessageProviderContract.DataTypes.VVMGREETING, paramOMAresponseforBufDB.getLine(), CloudMessageBufferDBConstants.InitialSyncStatusFlag.START, paramOMAresponseforBufDB.getIsFullSync());
        }
        ObjectList objectList = paramOMAresponseforBufDB.getObjectList();
        int i = 17;
        if (!(objectList == null || objectList.object == null)) {
            int i2 = 0;
            while (true) {
                Object[] objectArr = objectList.object;
                if (i2 < objectArr.length) {
                    Object object = objectArr[i2];
                    ParamOMAObject paramOMAObject = new ParamOMAObject(object, false, -1, this.mICloudMessageManagerHelper, this.mStoreClient);
                    if (paramOMAObject.mObjectType != -1) {
                        Log.d(this.TAG, "param.mObjectType: " + paramOMAObject.mObjectType);
                        int i3 = paramOMAObject.mObjectType;
                        if (i3 == 3) {
                            this.mSmsScheduler.handleObjectSMSCloudSearch(paramOMAObject);
                        } else if (i3 == 4) {
                            this.mMmsScheduler.handleObjectMMSCloudSearch(paramOMAObject);
                        } else if (i3 == 17) {
                            this.mVVMScheduler.handleObjectVvmMessageCloudSearch(paramOMAObject, z);
                        } else if (i3 == 18) {
                            this.mVVMScheduler.handleObjectVvmGreetingCloudSearch(paramOMAObject);
                        } else if (i3 == 34) {
                            this.mRcsScheduler.handleCloudNotifyGSOChangedObj(paramOMAObject, object);
                        } else if (i3 != 38) {
                            switch (i3) {
                                case 11:
                                case 12:
                                case 14:
                                    if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldSkipMessage(paramOMAObject)) {
                                        break;
                                    } else {
                                        this.mRcsScheduler.handleObjectRCSMessageCloudSearch(paramOMAObject, z);
                                        break;
                                    }
                                case 13:
                                    this.mRcsScheduler.handleObjectRCSIMDNCloudSearch(paramOMAObject);
                                    break;
                            }
                        } else {
                            this.mRcsScheduler.handleCloudNotifyConferenceInfo(paramOMAObject, object, true);
                        }
                    }
                    i2++;
                }
            }
        }
        if (paramOMAresponseforBufDB.getActionType().equals(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE)) {
            SyncMsgType syncMsgType = paramOMAresponseforBufDB.getSyncMsgType();
            SyncMsgType syncMsgType2 = SyncMsgType.VM;
            if (syncMsgType == syncMsgType2 || paramOMAresponseforBufDB.getSyncMsgType() == SyncMsgType.VM_GREETINGS) {
                if (paramOMAresponseforBufDB.getSyncMsgType() == syncMsgType2) {
                    this.mVVMScheduler.handleSyncSummaryComplete(paramOMAresponseforBufDB.getLine());
                } else {
                    i = 18;
                }
                if (this.mVVMScheduler.queryPendingVVMUrlFetch(i) > 0) {
                    onSendUnDownloadedMessage(paramOMAresponseforBufDB.getLine(), paramOMAresponseforBufDB.getSyncMsgType(), false, CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId());
                } else {
                    downloadMessageOnFetchUrlSuccess(paramOMAresponseforBufDB.getLine(), "VVMDATA", i);
                }
            } else {
                if (!this.mRcsScheduler.isEmptySession()) {
                    this.mRcsScheduler.handleNotifySessionToApp();
                }
                onSendUnDownloadedMessage(paramOMAresponseforBufDB.getLine(), paramOMAresponseforBufDB.getSyncMsgType(), false, CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId());
            }
        }
    }

    private void handleSearchObjectForVVM(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        String str = this.TAG;
        Log.d(str, "handleSearchObjectForVVM: " + paramOMAresponseforBufDB);
        ObjectList objectList = paramOMAresponseforBufDB.getObjectList();
        if (objectList != null && objectList.object != null) {
            int i = 0;
            while (true) {
                Object[] objectArr = objectList.object;
                if (i < objectArr.length) {
                    ParamOMAObject paramOMAObject = new ParamOMAObject(objectArr[i], false, -1, this.mICloudMessageManagerHelper, this.mStoreClient);
                    if (paramOMAObject.mObjectType == -1) {
                        Log.e(this.TAG, "errorL in object list");
                    } else {
                        String str2 = this.TAG;
                        Log.d(str2, "param.mObjectType: " + paramOMAObject.mObjectType);
                        int i2 = paramOMAObject.mObjectType;
                        if (i2 == 0) {
                            Log.e(this.TAG, "invalid message context");
                        } else if (i2 == 17) {
                            this.mVVMScheduler.handleObjectVvmMessageCloudSearch(paramOMAObject, z);
                        }
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private void handleDownloadedPayload(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        if (paramOMAresponseforBufDB.getBufferDBChangeParam() != null) {
            int i = paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex;
            if (i == 1) {
                this.mRcsScheduler.onRcsPayloadDownloaded(paramOMAresponseforBufDB, false);
            } else if (i == 6) {
                this.mMmsScheduler.onMmsPayloadDownloaded(paramOMAresponseforBufDB, false);
            }
        }
    }

    private void handleDownloadedAllPayloads(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB.getBufferDBChangeParam() != null) {
            int i = paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex;
            if (i == 1) {
                this.mRcsScheduler.onRcsAllPayloadsDownloaded(paramOMAresponseforBufDB, false);
            } else if (i == 4) {
                this.mMmsScheduler.onMmsAllPayloadsDownloadFromMcs(paramOMAresponseforBufDB);
            } else if (i == 17) {
                this.mVVMScheduler.onVvmAllPayloadDownloaded(paramOMAresponseforBufDB, false);
            } else if (i == 18) {
                this.mVVMScheduler.onGreetingAllPayloadDownloaded(paramOMAresponseforBufDB, false);
            }
        }
    }

    private void onUpdateFromCloud(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        String str = this.TAG;
        Log.i(str, "onUpdateFromCloud: " + paramOMAresponseforBufDB + " mIsGoforwardSync:" + this.mIsGoforwardSync);
        if (paramOMAresponseforBufDB != null && paramOMAresponseforBufDB.getActionType() != null) {
            switch (AnonymousClass2.$SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[paramOMAresponseforBufDB.getActionType().ordinal()]) {
                case 1:
                    onInitialSyncComplete(true, paramOMAresponseforBufDB.getLine(), paramOMAresponseforBufDB.getSyncMsgType(), paramOMAresponseforBufDB);
                    return;
                case 2:
                case 3:
                    handleSearchObject(paramOMAresponseforBufDB, false);
                    return;
                case 5:
                    onInitialSyncComplete(false, paramOMAresponseforBufDB.getLine(), paramOMAresponseforBufDB.getSyncMsgType(), paramOMAresponseforBufDB);
                    return;
                case 7:
                    handleDownloadedPayload(paramOMAresponseforBufDB, true);
                    return;
                case 8:
                    handleDownloadedAllPayloads(paramOMAresponseforBufDB);
                    return;
                case 9:
                    int queryPendingFetchForce = this.mMmsScheduler.queryPendingFetchForce();
                    String line = paramOMAresponseforBufDB.getLine();
                    String str2 = this.TAG;
                    Log.i(str2, "ALL_PAYLOAD_NOTIFY pendingMMScount: " + queryPendingFetchForce);
                    if (queryPendingFetchForce == 0) {
                        processDownloadComplete(line);
                        return;
                    } else {
                        onSendPayloadObject(line, SyncMsgType.DEFAULT);
                        return;
                    }
                case 10:
                    int lineUploadStatus = this.mMultiLnScheduler.getLineUploadStatus(paramOMAresponseforBufDB.getLine(), paramOMAresponseforBufDB.getSyncMsgType());
                    String str3 = this.TAG;
                    IMSLog.i(str3, "msg download complete, cmsEnable:" + this.mIsCmsEnabled + " uploadStatus:" + lineUploadStatus);
                    if (!this.mIsCmsEnabled || lineUploadStatus == OMASyncEventType.INITIAL_UPLOAD_PENDING.getId()) {
                        notifyNetAPIUploadMessages(paramOMAresponseforBufDB.getLine(), paramOMAresponseforBufDB.getSyncMsgType(), false);
                        return;
                    }
                    return;
                case 11:
                    this.mMultiLnScheduler.updateLineUploadStatus(paramOMAresponseforBufDB.getLine(), SyncMsgType.DEFAULT, OMASyncEventType.INITIAL_UPLOAD_STARTED.getId());
                    return;
                case 12:
                    onCloudUploadSuccess(paramOMAresponseforBufDB);
                    return;
                case 13:
                    this.mMultiLnScheduler.updateLineUploadStatus(paramOMAresponseforBufDB.getLine(), SyncMsgType.DEFAULT, OMASyncEventType.INITIAL_UPLOAD_COMPLETED.getId());
                    this.mBufferDBChangeNetAPI.mChangelst.clear();
                    return;
                case 14:
                    onCloudNormalSyncObjectDownload(paramOMAresponseforBufDB, false);
                    return;
                case 15:
                    handleDownloadedPayload(paramOMAresponseforBufDB, false);
                    return;
                case 16:
                    handleDownloadedAllPayloads(paramOMAresponseforBufDB);
                    return;
                case 17:
                    handleDownloadedImdns(paramOMAresponseforBufDB);
                    return;
                case 19:
                    onMailBoxReset();
                    return;
                case 21:
                case 22:
                    onCloudUpdateFlagSuccess(paramOMAresponseforBufDB);
                    return;
                case 23:
                    onCloudNotificationReceivedUnknownType(paramOMAresponseforBufDB);
                    return;
                case 24:
                    onDownloadFailure(paramOMAresponseforBufDB);
                    return;
                case 25:
                    onUploadFailureHandling(paramOMAresponseforBufDB);
                    return;
                case 26:
                    onCloudDeleteObjectFailed(paramOMAresponseforBufDB);
                    return;
                case 28:
                    this.mVVMScheduler.handleVvmProfileDownloaded(paramOMAresponseforBufDB);
                    return;
                case 29:
                    this.mVVMScheduler.handleVvmQuotaInfo(paramOMAresponseforBufDB);
                    return;
                case 30:
                    onBulkFlagUpdateComplete(paramOMAresponseforBufDB);
                    return;
                case 31:
                    onBulkCreationComplete(paramOMAresponseforBufDB);
                    return;
                case 32:
                    handleSearchObjectForVVM(paramOMAresponseforBufDB, false);
                    return;
                case 33:
                    handleSearchObjectForVVM(paramOMAresponseforBufDB, false);
                    int queryPendingVVMUrlFetch = this.mVVMScheduler.queryPendingVVMUrlFetch(17);
                    String str4 = this.TAG;
                    Log.i(str4, "onSendUnDownloadedMessage pendingVVMCount: " + queryPendingVVMUrlFetch);
                    if (queryPendingVVMUrlFetch > 0) {
                        onSendUnDownloadedMessage(paramOMAresponseforBufDB.getLine(), SyncMsgType.VM, false, CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId());
                    } else {
                        downloadMessageOnFetchUrlSuccess(paramOMAresponseforBufDB.getLine(), "VVMDATA", 17);
                    }
                    this.mVVMScheduler.handleSyncSummaryComplete(paramOMAresponseforBufDB.getLine());
                    return;
                case 34:
                    onAdhocV2tPayloadDownloadFailure(paramOMAresponseforBufDB);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus;

        /* JADX WARNING: Can't wrap try/catch for region: R(101:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|17|18|19|21|22|23|(2:25|26)|27|(2:29|30)|31|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|(2:83|84)|85|87|88|89|90|91|92|93|94|95|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(102:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|17|18|19|21|22|23|(2:25|26)|27|29|30|31|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|(2:83|84)|85|87|88|89|90|91|92|93|94|95|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(103:0|(2:1|2)|3|5|6|7|9|10|11|13|14|15|17|18|19|21|22|23|(2:25|26)|27|29|30|31|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|(2:83|84)|85|87|88|89|90|91|92|93|94|95|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(104:0|(2:1|2)|3|5|6|7|9|10|11|13|14|15|17|18|19|21|22|23|(2:25|26)|27|29|30|31|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|83|84|85|87|88|89|90|91|92|93|94|95|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(106:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|21|22|23|25|26|27|29|30|31|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|83|84|85|87|88|89|90|91|92|93|94|95|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(96:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|(2:13|14)|15|(2:17|18)|19|21|22|23|(2:25|26)|27|(2:29|30)|31|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|(2:83|84)|85|87|88|89|90|91|92|93|94|95|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|(3:113|114|116)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(97:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|(2:13|14)|15|17|18|19|21|22|23|(2:25|26)|27|(2:29|30)|31|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|(2:83|84)|85|87|88|89|90|91|92|93|94|95|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|(3:113|114|116)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(98:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|17|18|19|21|22|23|(2:25|26)|27|(2:29|30)|31|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|(2:83|84)|85|87|88|89|90|91|92|93|94|95|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|(3:113|114|116)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:101:0x01e2 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:103:0x01ec */
        /* JADX WARNING: Missing exception handler attribute for start block: B:105:0x01f6 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:107:0x0200 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:109:0x020a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:111:0x0214 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:113:0x021e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x009c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x00b4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x00c0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:51:0x00cc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:53:0x00d8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:55:0x00e4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:57:0x00f0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:59:0x00fc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:61:0x0108 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:63:0x0114 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:65:0x0120 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:67:0x012c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:69:0x0138 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:71:0x0144 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:73:0x0150 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:75:0x015c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:77:0x0168 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:79:0x0174 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:81:0x0180 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:83:0x018c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:89:0x01a9 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:91:0x01b3 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:93:0x01bd */
        /* JADX WARNING: Missing exception handler attribute for start block: B:99:0x01d8 */
        static {
            /*
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType[] r0 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType = r0
                r1 = 1
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r2 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r3 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r4 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_PARTIAL_SYNC_SUMMARY     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r5 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.MATCH_DB     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r6 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.SYNC_FAILED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                r5 = 6
                int[] r6 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r7 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_DOWNLOAD     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r7 = r7.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r6[r7] = r5     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                r6 = 7
                int[] r7 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r8 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.ONE_PAYLOAD_DOWNLOAD     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r7[r8] = r6     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                r7 = 8
                int[] r8 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r9 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.ALL_PAYLOAD_DOWNLOAD     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r8[r9] = r7     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                r8 = 9
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.ALL_PAYLOAD_NOTIFY     // Catch:{ NoSuchFieldError -> 0x006c }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r9[r10] = r8     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.MESSAGE_DOWNLOAD_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r11 = 10
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_UPLOAD_STARTED     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r11 = 11
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_UPLOADED     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r11 = 12
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.MESSAGE_UPLOAD_COMPLETE     // Catch:{ NoSuchFieldError -> 0x009c }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r11 = 13
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.NOTIFICATION_OBJECT_DOWNLOADED     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r11 = 14
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.NOTIFICATION_PAYLOAD_DOWNLOADED     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r11 = 15
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.NOTIFICATION_ALL_PAYLOAD_DOWNLOADED     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r11 = 16
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.NOTIFICATION_IMDN_DOWNLOADED     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r11 = 17
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.NOTIFICATION_OBJECTS_DOWNLOAD_COMPLETE     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r11 = 18
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.MAILBOX_RESET     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r11 = 19
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x00f0 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.CLOUD_OBJECT_UPDATE     // Catch:{ NoSuchFieldError -> 0x00f0 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f0 }
                r11 = 20
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x00f0 }
            L_0x00f0:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x00fc }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_FLAG_UPDATED     // Catch:{ NoSuchFieldError -> 0x00fc }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fc }
                r11 = 21
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x00fc }
            L_0x00fc:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0108 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_UPDATE_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0108 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0108 }
                r11 = 22
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0108 }
            L_0x0108:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0114 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION     // Catch:{ NoSuchFieldError -> 0x0114 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0114 }
                r11 = 23
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0114 }
            L_0x0114:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0120 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND     // Catch:{ NoSuchFieldError -> 0x0120 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0120 }
                r11 = 24
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0120 }
            L_0x0120:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x012c }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.VVM_FAX_ERROR_WITH_NO_RETRY     // Catch:{ NoSuchFieldError -> 0x012c }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x012c }
                r11 = 25
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x012c }
            L_0x012c:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0138 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_DELETE_UPDATE_FAILED     // Catch:{ NoSuchFieldError -> 0x0138 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0138 }
                r11 = 26
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0138 }
            L_0x0138:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0144 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_READ_UPDATE_FAILED     // Catch:{ NoSuchFieldError -> 0x0144 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0144 }
                r11 = 27
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0144 }
            L_0x0144:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0150 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.VVM_PROFILE_DOWNLOADED     // Catch:{ NoSuchFieldError -> 0x0150 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0150 }
                r11 = 28
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0150 }
            L_0x0150:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x015c }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.VVM_QUOTA_INFO     // Catch:{ NoSuchFieldError -> 0x015c }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x015c }
                r11 = 29
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x015c }
            L_0x015c:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0168 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0168 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0168 }
                r11 = 30
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0168 }
            L_0x0168:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0174 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.BULK_MESSAGES_UPLOADED     // Catch:{ NoSuchFieldError -> 0x0174 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0174 }
                r11 = 31
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0174 }
            L_0x0174:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0180 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.VVM_NORMAL_SYNC_SUMMARY_PARTIAL     // Catch:{ NoSuchFieldError -> 0x0180 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0180 }
                r11 = 32
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0180 }
            L_0x0180:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x018c }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.VVM_NORMAL_SYNC_SUMMARY_COMPLETE     // Catch:{ NoSuchFieldError -> 0x018c }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x018c }
                r11 = 33
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x018c }
            L_0x018c:
                int[] r9 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType     // Catch:{ NoSuchFieldError -> 0x0198 }
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r10 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.ADHOC_PAYLOAD_DOWNLOAD_FAILED     // Catch:{ NoSuchFieldError -> 0x0198 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0198 }
                r11 = 34
                r9[r10] = r11     // Catch:{ NoSuchFieldError -> 0x0198 }
            L_0x0198:
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType[] r9 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.values()
                int r9 = r9.length
                int[] r9 = new int[r9]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType = r9
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r10 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ NoSuchFieldError -> 0x01a9 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x01a9 }
                r9[r10] = r1     // Catch:{ NoSuchFieldError -> 0x01a9 }
            L_0x01a9:
                int[] r9 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x01b3 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r10 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE     // Catch:{ NoSuchFieldError -> 0x01b3 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x01b3 }
                r9[r10] = r0     // Catch:{ NoSuchFieldError -> 0x01b3 }
            L_0x01b3:
                int[] r9 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x01bd }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r10 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.START_INITIAL_SYNC     // Catch:{ NoSuchFieldError -> 0x01bd }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x01bd }
                r9[r10] = r2     // Catch:{ NoSuchFieldError -> 0x01bd }
            L_0x01bd:
                int[] r9 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x01c7 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r10 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DEFAULT     // Catch:{ NoSuchFieldError -> 0x01c7 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x01c7 }
                r9[r10] = r3     // Catch:{ NoSuchFieldError -> 0x01c7 }
            L_0x01c7:
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus[] r9 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.values()
                int r9 = r9.length
                int[] r9 = new int[r9]
                $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus = r9
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus r10 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.PROVISION_SUCCESS     // Catch:{ NoSuchFieldError -> 0x01d8 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x01d8 }
                r9[r10] = r1     // Catch:{ NoSuchFieldError -> 0x01d8 }
            L_0x01d8:
                int[] r1 = $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus     // Catch:{ NoSuchFieldError -> 0x01e2 }
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus r9 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.OMA_PROVISION_FAILED     // Catch:{ NoSuchFieldError -> 0x01e2 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x01e2 }
                r1[r9] = r0     // Catch:{ NoSuchFieldError -> 0x01e2 }
            L_0x01e2:
                int[] r0 = $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus     // Catch:{ NoSuchFieldError -> 0x01ec }
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus r1 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.SEND_TOCLOUD_UNSYNC     // Catch:{ NoSuchFieldError -> 0x01ec }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x01ec }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x01ec }
            L_0x01ec:
                int[] r0 = $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus     // Catch:{ NoSuchFieldError -> 0x01f6 }
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus r1 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.NET_WORK_STATUS_CHANGED     // Catch:{ NoSuchFieldError -> 0x01f6 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x01f6 }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x01f6 }
            L_0x01f6:
                int[] r0 = $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus     // Catch:{ NoSuchFieldError -> 0x0200 }
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus r1 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.DEFAULT_MSGAPP_CHGTO_NATIVE     // Catch:{ NoSuchFieldError -> 0x0200 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0200 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x0200 }
            L_0x0200:
                int[] r0 = $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus     // Catch:{ NoSuchFieldError -> 0x020a }
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus r1 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.RESTART_SERVICE     // Catch:{ NoSuchFieldError -> 0x020a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x020a }
                r0[r1] = r5     // Catch:{ NoSuchFieldError -> 0x020a }
            L_0x020a:
                int[] r0 = $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus     // Catch:{ NoSuchFieldError -> 0x0214 }
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus r1 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.BUFFERDB_CLEAN     // Catch:{ NoSuchFieldError -> 0x0214 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0214 }
                r0[r1] = r6     // Catch:{ NoSuchFieldError -> 0x0214 }
            L_0x0214:
                int[] r0 = $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus     // Catch:{ NoSuchFieldError -> 0x021e }
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus r1 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.MAILBOX_MIGRATION_RESET     // Catch:{ NoSuchFieldError -> 0x021e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x021e }
                r0[r1] = r7     // Catch:{ NoSuchFieldError -> 0x021e }
            L_0x021e:
                int[] r0 = $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus     // Catch:{ NoSuchFieldError -> 0x0228 }
                com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener$WorkingStatus r1 = com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener.WorkingStatus.UPDATE_CMS_CONFIG     // Catch:{ NoSuchFieldError -> 0x0228 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0228 }
                r0[r1] = r8     // Catch:{ NoSuchFieldError -> 0x0228 }
            L_0x0228:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler.AnonymousClass2.<clinit>():void");
        }
    }

    private void processDownloadComplete(String str) {
        ParamOMAresponseforBufDB.Builder line = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MESSAGE_DOWNLOAD_COMPLETE).setLine(str);
        SyncMsgType syncMsgType = SyncMsgType.DEFAULT;
        sendMessage(obtainMessage(3, new AsyncResult((Object) null, line.setSyncType(syncMsgType).build(), (Throwable) null)));
        sendMessage(obtainMessage(3, new AsyncResult((Object) null, new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_COMPLETE).setOMASyncEventType(OMASyncEventType.INITIAL_SYNC_COMPLETE).setLine(str).setSyncType(syncMsgType).build(), (Throwable) null)));
    }

    private void onCloudDeleteObjectFailed(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB != null && paramOMAresponseforBufDB.getBufferDBChangeParam() != null) {
            int i = paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex;
            long j = paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId;
            String str = paramOMAresponseforBufDB.getBufferDBChangeParam().mLine;
            if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isNotifyAppOnUpdateCloudFail()) {
                return;
            }
            if (i == 1) {
                this.mRcsScheduler.notifyMsgAppDeleteFail(i, j, str);
            } else if (i == 17) {
                this.mVVMScheduler.notifyMsgAppDeleteFail(i, j, str);
            } else if (i == 3) {
                this.mSmsScheduler.notifyMsgAppDeleteFail(i, j, str);
            } else if (i == 4) {
                this.mMmsScheduler.notifyMsgAppDeleteFail(i, j, str);
            }
        }
    }

    private void onBulkFlagUpdateComplete(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB.getBulkResponseList() == null || paramOMAresponseforBufDB.getBulkResponseList().response == null) {
            Log.e(this.TAG, "onBulkFlagUpdateComplete: invalid return results");
            return;
        }
        for (int i = 0; i < paramOMAresponseforBufDB.getBulkResponseList().response.length; i++) {
            if (paramOMAresponseforBufDB.getBulkResponseList().response[i].success != null && paramOMAresponseforBufDB.getBulkResponseList().response[i].success.resourceURL != null) {
                handleBulkOpSingleUrlSuccess(paramOMAresponseforBufDB.getBulkResponseList().response[i].success.resourceURL.toString());
            } else if (!(paramOMAresponseforBufDB.getBulkResponseList().response[i].failure == null || !this.mStoreClient.getCloudMessageStrategyManager().getStrategy().bulkOpTreatSuccessIndividualResponse(paramOMAresponseforBufDB.getBulkResponseList().response[i].code) || paramOMAresponseforBufDB.getBulkResponseList().response[i].failure == null || paramOMAresponseforBufDB.getBulkResponseList().response[i].failure.serviceException == null || paramOMAresponseforBufDB.getBulkResponseList().response[i].failure.serviceException.variables == null)) {
                handleBulkOpSingleUrlSuccess(paramOMAresponseforBufDB.getBulkResponseList().response[i].failure.serviceException.variables[0]);
            }
        }
    }

    private void onUploadFailureHandling(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB != null && paramOMAresponseforBufDB.getBufferDBChangeParam() != null) {
            int i = paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex;
            if (i == 18 || i == 19 || i == 20 || i == 17) {
                this.mVVMScheduler.handleUpdateVVMResponse(paramOMAresponseforBufDB, false);
            }
        }
    }

    private void onDownloadFailure(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB != null && paramOMAresponseforBufDB.getBufferDBChangeParam() != null) {
            int i = paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex;
            if (i == 17 || i == 18) {
                this.mVVMScheduler.handleDownLoadMessageResponse(paramOMAresponseforBufDB, false);
            } else if (i == 1) {
                this.mRcsScheduler.handleDownLoadMessageResponse(paramOMAresponseforBufDB, false);
            }
        }
    }

    private void onMailBoxReset() {
        cleanAllBufferDB();
        startInitialSyncDBCopyTask();
        this.mDeviceDataChangeListener.onMailBoxResetBufferDbDone();
    }

    private void onCloudNormalSyncObjectDownload(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        if (paramOMAresponseforBufDB.getBufferDBChangeParam() != null) {
            ParamOMAObject paramOMAObject = new ParamOMAObject(paramOMAresponseforBufDB.getObject(), paramOMAresponseforBufDB.getBufferDBChangeParam().mIsGoforwardSync, paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex, this.mICloudMessageManagerHelper, this.mStoreClient);
            if (paramOMAObject.mObjectType != -1 || paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex == 7) {
                this.mSummaryQuery.insertSummaryDbUsingObjectIfNonExist(paramOMAObject, paramOMAObject.mObjectType);
                int i = paramOMAObject.mObjectType;
                if (i != 1) {
                    if (i == 34) {
                        this.mRcsScheduler.handleCloudNotifyGSOChangedObj(paramOMAObject, paramOMAresponseforBufDB.getObject());
                        return;
                    } else if (i == 38) {
                        this.mRcsScheduler.handleCloudNotifyConferenceInfo(paramOMAObject, paramOMAresponseforBufDB.getObject(), false);
                        return;
                    } else if (i == 3) {
                        this.mSmsScheduler.handleNormalSyncObjectSmsDownload(paramOMAObject);
                        return;
                    } else if (i == 4) {
                        this.mMmsScheduler.handleNormalSyncObjectMmsDownload(paramOMAObject, z);
                        return;
                    } else if (i == 17) {
                        this.mVVMScheduler.handleNormalSyncDownloadedVVMMessage(paramOMAObject);
                        return;
                    } else if (i != 18) {
                        switch (i) {
                            case 11:
                            case 12:
                            case 14:
                                break;
                            case 13:
                                this.mRcsScheduler.handleNormalSyncObjectRcsImdnDownload(paramOMAObject);
                                return;
                            default:
                                return;
                        }
                    } else {
                        this.mVVMScheduler.handleNormalSyncDownloadedVVMGreeting(paramOMAObject);
                        return;
                    }
                }
                this.mRcsScheduler.handleNormalSyncObjectRcsMessageDownload(paramOMAObject, z);
            }
        }
    }

    private void onCloudNotificationReceivedUnknownType(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        String str;
        NotificationList[] notificationList = paramOMAresponseforBufDB.getNotificationList();
        if (CmsUtil.isMcsSupported(this.mContext, this.mStoreClient.getClientID())) {
            if (notificationList == null || notificationList.length == 0) {
                notificationList = new NotificationList[]{new NotificationList()};
            }
            notificationList[0].nmsEventList = paramOMAresponseforBufDB.getMcsNmsEventList();
        }
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        if (notificationList == null) {
            this.mIsGoforwardSync = false;
            return;
        }
        boolean z = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGoForwardSyncSupported() && this.mIsGoforwardSync;
        for (NotificationList notificationList2 : notificationList) {
            NmsEventList nmsEventList = notificationList2.nmsEventList;
            if (nmsEventList != null && nmsEventList.nmsEvent != null) {
                int i = 0;
                while (true) {
                    NmsEvent[] nmsEventArr = nmsEventList.nmsEvent;
                    if (i >= nmsEventArr.length) {
                        break;
                    }
                    NmsEvent nmsEvent = nmsEventArr[i];
                    String str2 = this.TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("onCloudNotificationReceivedUnknownType, ChangedObj:");
                    String str3 = null;
                    sb.append(nmsEvent.changedObject == null ? null : "not null");
                    sb.append(" DeletedObj:");
                    if (nmsEvent.deletedObject == null) {
                        str = null;
                    } else {
                        str = "not null";
                    }
                    sb.append(str);
                    sb.append(" ExpiredObj:");
                    if (nmsEvent.expiredObject != null) {
                        str3 = "not null";
                    }
                    sb.append(str3);
                    sb.append(" shouldSkipDeletedObjt:");
                    sb.append(z);
                    sb.append(" mIsGoforwardSync:");
                    sb.append(this.mIsGoforwardSync);
                    Log.i(str2, sb.toString());
                    ChangedObject changedObject = nmsEvent.changedObject;
                    if (changedObject != null) {
                        handleCloudNotifyChangedObj(changedObject, bufferDBChangeParamList, this.mIsGoforwardSync);
                    }
                    DeletedObject deletedObject = nmsEvent.deletedObject;
                    if (deletedObject != null && !z) {
                        handleCloudNotifyDeletedObj(deletedObject, false);
                    }
                    DeletedObject deletedObject2 = nmsEvent.expiredObject;
                    if (deletedObject2 != null) {
                        handleExpiredObject(deletedObject2);
                    }
                    i++;
                }
            }
        }
        if (bufferDBChangeParamList.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(bufferDBChangeParamList);
        }
        if (this.mIsGoforwardSync) {
            onSendCloudUnSyncedUpdate();
            this.mIsGoforwardSync = false;
        }
    }

    private void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB.getBufferDBChangeParam() != null) {
            int i = paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex;
            if (!(i == 1 || i == 14)) {
                if (i == 17) {
                    this.mVVMScheduler.onCloudUpdateFlagSuccess(paramOMAresponseforBufDB, false);
                    return;
                } else if (i == 3) {
                    this.mSmsScheduler.onCloudUpdateFlagSuccess(paramOMAresponseforBufDB, false);
                    return;
                } else if (i == 4) {
                    this.mMmsScheduler.onCloudUpdateFlagSuccess(paramOMAresponseforBufDB, false);
                    return;
                } else if (!(i == 11 || i == 12)) {
                    return;
                }
            }
            this.mRcsScheduler.onCloudUpdateFlagSuccess(paramOMAresponseforBufDB, false);
        }
    }

    private void onCloudUploadSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB.getBufferDBChangeParam() != null) {
            if (paramOMAresponseforBufDB.getReference() != null) {
                int i = paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex;
                if (!(i == 1 || i == 14)) {
                    if (i == 3) {
                        this.mSmsScheduler.onCloudUploadSuccess(paramOMAresponseforBufDB, false);
                        return;
                    } else if (i == 4) {
                        this.mMmsScheduler.onCloudUploadSuccess(paramOMAresponseforBufDB, false);
                        return;
                    } else if (!(i == 11 || i == 12)) {
                        switch (i) {
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                                this.mVVMScheduler.handleUpdateVVMResponse(paramOMAresponseforBufDB, true);
                                return;
                            default:
                                return;
                        }
                    }
                }
                this.mRcsScheduler.onCloudUploadSuccess(paramOMAresponseforBufDB, false);
                return;
            }
            int i2 = paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex;
            if (i2 != 3) {
                switch (i2) {
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                        this.mVVMScheduler.handleUpdateVVMResponse(paramOMAresponseforBufDB, true);
                        return;
                    default:
                        return;
                }
            } else {
                this.mSmsScheduler.onGroupSMSUploadSuccess(paramOMAresponseforBufDB);
            }
        }
    }

    private void onInitialSyncComplete(boolean z, String str, SyncMsgType syncMsgType, ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        String str2;
        if (str != null) {
            boolean isFullSync = paramOMAresponseforBufDB.getIsFullSync();
            if (z) {
                this.mMultiLnScheduler.updateLineInitsyncStatus(str, paramOMAresponseforBufDB.getSyncMsgType(), paramOMAresponseforBufDB.getSearchCursor(), paramOMAresponseforBufDB.getOMASyncEventType().getId());
            } else {
                IMSLog.c(LogClass.MCS_INIT_SYNC_STATUS, this.mPhoneId + "," + BaseSyncHandler.SyncOperation.DOWNLOAD.ordinal() + "," + paramOMAresponseforBufDB.getOMASyncEventType().getId());
            }
            if (SyncMsgType.DEFAULT.equals(paramOMAresponseforBufDB.getSyncMsgType()) || SyncMsgType.MESSAGE.equals(paramOMAresponseforBufDB.getSyncMsgType())) {
                if (z) {
                    this.mSmsScheduler.notifyInitialSyncStatus(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MSGAPP_ALL, str, CloudMessageBufferDBConstants.InitialSyncStatusFlag.FINISHED, isFullSync);
                } else {
                    this.mSmsScheduler.notifyInitialSyncStatus(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MSGAPP_ALL, str, CloudMessageBufferDBConstants.InitialSyncStatusFlag.FAIL, isFullSync);
                }
            } else if (SyncMsgType.VM.equals(paramOMAresponseforBufDB.getSyncMsgType()) || SyncMsgType.VM_GREETINGS.equals(paramOMAresponseforBufDB.getSyncMsgType())) {
                if (isFullSync || this.mDeviceDataChangeListener.isNormalVVMSyncing()) {
                    this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(false);
                }
                if (SyncMsgType.VM_GREETINGS.equals(paramOMAresponseforBufDB.getSyncMsgType())) {
                    str2 = CloudMessageProviderContract.DataTypes.VVMGREETING;
                } else {
                    if (this.mDeviceDataChangeListener.isNormalVVMSyncing()) {
                        Log.i(this.TAG, "Actually Normal Sync Completion");
                        this.mDeviceDataChangeListener.setVVMSyncState(false);
                        isFullSync = false;
                    }
                    str2 = "VVMDATA";
                }
                this.mVVMScheduler.notifyInitialSyncStatus("VVMDATA", str2, str, z ? CloudMessageBufferDBConstants.InitialSyncStatusFlag.FINISHED : CloudMessageBufferDBConstants.InitialSyncStatusFlag.FAIL, isFullSync);
            }
            onHandlePendingNmsEvent();
            if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getIsInitSyncIndicatorRequired()) {
                Log.i(this.TAG, "Send a to init sync termial flag(RowId = -1) to messaging app");
                this.mSmsScheduler.notifyMsgAppCldNotification(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.SMS, -1, false);
            }
        }
    }

    private void startInitialDBCopy() {
        this.mBufferDBloaded = this.mStoreClient.getPrerenceManager().getBufferDbLoaded();
        String str = this.TAG;
        Log.d(str, "startInitialDBCopy(), mProvisionSuccess: " + this.mProvisionSuccess + ", mRCSDbReady: " + this.mRCSDbReady + ", mBufferDBloaded: " + this.mBufferDBloaded);
        if (this.mRCSDbReady && !this.mBufferDBloaded && this.mProvisionSuccess) {
            sendMessage(obtainMessage(1, (Object) null));
        }
    }

    private void restartService() {
        sendMessage(obtainMessage(12, (Object) null));
    }

    public void onRCSDbReady() {
        Log.d(this.TAG, "onRCSDbReady()");
        sendMessage(obtainMessage(11, (Object) null));
    }

    public void onFtUriResponseJson(String str, String str2) {
        int i;
        int i2;
        try {
            JsonElement parse = new JsonParser().parse(str2);
            if (parse.isJsonArray()) {
                JsonArray asJsonArray = parse.getAsJsonArray();
                Log.i(this.TAG, "jsonArray size : " + asJsonArray.size());
                long j = -1;
                long j2 = -1;
                String str3 = "";
                long j3 = -1;
                for (int i3 = 0; i3 < asJsonArray.size(); i3++) {
                    JsonObject asJsonObject = asJsonArray.get(i3).getAsJsonObject();
                    if (asJsonObject.get("id") == null || asJsonObject.get("id").isJsonNull()) {
                        Log.e(this.TAG, "onFtUriResponseJson id is null");
                    } else {
                        j = asJsonObject.get("id").getAsLong();
                    }
                    if (asJsonObject.get(CloudMessageProviderContract.JsonData.REMOTE_ID) == null || asJsonObject.get(CloudMessageProviderContract.JsonData.REMOTE_ID).isJsonNull()) {
                        Log.e(this.TAG, "onFtUriResponseJson remoteId is null");
                    } else {
                        j3 = asJsonObject.get(CloudMessageProviderContract.JsonData.REMOTE_ID).getAsLong();
                    }
                    if (asJsonObject.get("type") == null || asJsonObject.get("type").isJsonNull()) {
                        Log.e(this.TAG, "onFtUriResponseJson messageType is null");
                    } else {
                        str3 = asJsonObject.get("type").getAsString();
                    }
                    if (asJsonObject.get(CloudMessageProviderContract.JsonData.IMS_PARTID) == null || asJsonObject.get(CloudMessageProviderContract.JsonData.IMS_PARTID).isJsonNull()) {
                        Log.e(this.TAG, "onFtUriResponseJson imsPartId is null");
                    } else {
                        j2 = asJsonObject.get(CloudMessageProviderContract.JsonData.IMS_PARTID).getAsLong();
                    }
                    if (str3.equalsIgnoreCase(CloudMessageProviderContract.DataTypes.MMS)) {
                        i2 = 4;
                    } else if (str3.equalsIgnoreCase("FT")) {
                        i2 = 1;
                    } else if (str3.equalsIgnoreCase("VVMDATA")) {
                        i2 = 17;
                    } else if (str3.equalsIgnoreCase(CloudMessageProviderContract.DataTypes.VVMGREETING)) {
                        i2 = 18;
                    } else {
                        i = 0;
                        Log.i(this.TAG, "onFtUriResponseJson tableId: " + i + "localId: " + j + " remotId: " + j3 + " partId:" + j2);
                        sendMessage(obtainMessage(30, new DeviceMsgAppFetchUriParam(i, j, j3, j2, str)));
                    }
                    i = i2;
                    Log.i(this.TAG, "onFtUriResponseJson tableId: " + i + "localId: " + j + " remotId: " + j3 + " partId:" + j2);
                    sendMessage(obtainMessage(30, new DeviceMsgAppFetchUriParam(i, j, j3, j2, str)));
                }
            }
        } catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d4 A[Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00de A[Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onBufferDBReadResult(java.lang.String r11, java.lang.String r12, java.lang.String r13, int r14, boolean r15) {
        /*
            r10 = this;
            java.lang.String r0 = r10.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onBufferDBReadResult "
            r1.append(r2)
            r1.append(r11)
            java.lang.String r2 = " "
            r1.append(r2)
            r1.append(r12)
            r1.append(r2)
            r1.append(r13)
            r1.append(r2)
            r1.append(r15)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r14)
            if (r4 != 0) goto L_0x0046
            java.lang.String r0 = r10.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "illegal syncAction: "
            r1.append(r2)
            r1.append(r14)
            java.lang.String r14 = r1.toString()
            android.util.Log.e(r0, r14)
        L_0x0046:
            if (r15 != 0) goto L_0x004a
            java.lang.String r13 = "0"
        L_0x004a:
            java.lang.String r14 = "MMS"
            boolean r14 = r14.equalsIgnoreCase(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            r0 = 0
            if (r14 == 0) goto L_0x0058
            r11 = 4
        L_0x0054:
            r3 = r11
            r9 = r0
            goto L_0x00ba
        L_0x0058:
            java.lang.String r14 = "SMS"
            boolean r14 = r14.equalsIgnoreCase(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            if (r14 == 0) goto L_0x0062
            r11 = 3
            goto L_0x0054
        L_0x0062:
            java.lang.String r14 = "FT"
            boolean r14 = r14.equalsIgnoreCase(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            r1 = 1
            if (r14 == 0) goto L_0x006e
            r3 = r1
            r9 = r3
            goto L_0x00ba
        L_0x006e:
            java.lang.String r14 = "CHAT"
            boolean r14 = r14.equalsIgnoreCase(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            if (r14 == 0) goto L_0x0079
            r9 = r0
            r3 = r1
            goto L_0x00ba
        L_0x0079:
            java.lang.String r14 = "VVMDATA"
            boolean r14 = r14.equalsIgnoreCase(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            if (r14 != 0) goto L_0x00b7
            java.lang.String r14 = "ADHOCV2T"
            boolean r14 = r14.equalsIgnoreCase(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            if (r14 == 0) goto L_0x008a
            goto L_0x00b7
        L_0x008a:
            java.lang.String r14 = "GREETING"
            boolean r14 = r14.equalsIgnoreCase(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            if (r14 == 0) goto L_0x0095
            r11 = 18
            goto L_0x0054
        L_0x0095:
            java.lang.String r14 = "RCS_SESSION"
            boolean r14 = r14.equalsIgnoreCase(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            if (r14 == 0) goto L_0x00a0
            r11 = 10
            goto L_0x0054
        L_0x00a0:
            java.lang.String r10 = r10.TAG     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            r12.<init>()     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            java.lang.String r13 = "onBufferDBReadResult wrong input type: "
            r12.append(r13)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            r12.append(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            java.lang.String r11 = r12.toString()     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            android.util.Log.d(r10, r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            return
        L_0x00b7:
            r11 = 17
            goto L_0x0054
        L_0x00ba:
            com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam r11 = new com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            int r12 = r12.intValue()     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            long r5 = (long) r12     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            java.lang.Integer r12 = java.lang.Integer.valueOf(r13)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            int r12 = r12.intValue()     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            long r7 = (long) r12     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            r2 = r11
            r2.<init>(r3, r4, r5, r7, r9)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            if (r15 == 0) goto L_0x00de
            r12 = 8
            android.os.Message r11 = r10.obtainMessage(r12, r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            r10.sendMessage(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            goto L_0x00ec
        L_0x00de:
            r12 = 26
            android.os.Message r11 = r10.obtainMessage(r12, r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            r10.sendMessage(r11)     // Catch:{ NullPointerException | NumberFormatException -> 0x00e8 }
            goto L_0x00ec
        L_0x00e8:
            r10 = move-exception
            r10.printStackTrace()
        L_0x00ec:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler.onBufferDBReadResult(java.lang.String, java.lang.String, java.lang.String, int, boolean):void");
    }

    public void createSession(String str) {
        sendMessage(obtainMessage(7, new DeviceSessionPartcptsUpdateParam(10, CloudMessageBufferDBConstants.ActionStatusFlag.Insert, str)));
    }

    public void createParticipant(String str) {
        sendMessage(obtainMessage(7, new DeviceSessionPartcptsUpdateParam(2, CloudMessageBufferDBConstants.ActionStatusFlag.Insert, str)));
    }

    public void deleteSession(String str) {
        sendMessage(obtainMessage(7, new DeviceSessionPartcptsUpdateParam(10, CloudMessageBufferDBConstants.ActionStatusFlag.Delete, str)));
    }

    public void deleteParticipant(String str) {
        sendMessage(obtainMessage(7, new DeviceSessionPartcptsUpdateParam(2, CloudMessageBufferDBConstants.ActionStatusFlag.Delete, str)));
    }

    public void onReturnAppFetchingFailedMsg(String str) {
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(str)) {
            sendMessage(obtainMessage(27, (Object) null));
        }
    }

    public void onNativeChannelReceived(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (!this.mDeviceDataChangeListener.isNativeMsgAppDefault()) {
            Log.d(this.TAG, "onNativeChannelReceived: msg app not default application - Ignore native channel notification");
        } else {
            sendMessage(obtainMessage(3, new AsyncResult((Object) null, paramOMAresponseforBufDB, (Throwable) null)));
        }
    }

    public void receivedMessageJson(String str) {
        sendMessage(obtainMessage(13, str));
    }

    public void sentMessageJson(String str) {
        sendMessage(obtainMessage(14, str));
    }

    public void readMessageJson(String str, String str2) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldEnableNetAPIPutFlag(str)) {
            sendMessage(obtainMessage(15, str2));
        }
    }

    public void cancelMessageJson(String str, String str2) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldEnableNetAPIPutFlag(str)) {
            sendMessage(obtainMessage(31, str2));
        }
    }

    public void starredRCSMessageList(String str) {
        sendMessage(obtainMessage(32, str));
    }

    public void unStarredRCSMessageList(String str) {
        sendMessage(obtainMessage(33, str));
    }

    public void unReadMessageJson(String str) {
        sendMessage(obtainMessage(16, str));
    }

    public void deleteMessageJson(String str) {
        sendMessage(obtainMessage(17, str));
    }

    public void uploadMessageJson(String str) {
        sendMessage(obtainMessage(18, str));
    }

    public void downloadMessageJson(String str) {
        sendMessage(obtainMessage(19, str));
    }

    public void wipeOutMessageJson(String str) {
        sendMessage(obtainMessage(20, str));
    }

    public void bufferDbReadBatchMessageJson(String str) {
        sendMessage(obtainMessage(23, str));
    }

    public void startFullSync(String str, String str2) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isAppTriggerMessageSearch()) {
            ParamAppJsonValueList decodeJson = decodeJson(str, str2, CloudMessageBufferDBConstants.MsgOperationFlag.StartFullSync);
            if (decodeJson == null) {
                Log.e(this.TAG, "error parsing startfullsync json value");
            } else {
                sendMessage(obtainMessage(24, decodeJson));
            }
        }
    }

    public void startDeltaSync(String str, String str2) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isAppTriggerMessageSearch()) {
            ParamAppJsonValueList decodeJson = decodeJson(str, str2, CloudMessageBufferDBConstants.MsgOperationFlag.StartDeltaSync);
            if (decodeJson == null) {
                Log.e(this.TAG, "error parsing startDeltaSync json value");
            } else {
                sendMessage(obtainMessage(29, decodeJson));
            }
        }
    }

    public void stopSync(String str, String str2) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isAppTriggerMessageSearch()) {
            ParamAppJsonValueList decodeJson = decodeJson(str, str2, CloudMessageBufferDBConstants.MsgOperationFlag.StopSync);
            if (decodeJson == null) {
                Log.e(this.TAG, "error parsing startfullsync json value");
            } else {
                sendMessage(obtainMessage(25, decodeJson));
            }
        }
    }

    public void resyncPendingMsg() {
        sendEmptyMessage(28);
    }

    private void onBulkCreationComplete(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB == null || paramOMAresponseforBufDB.getBufferDBChangeParamList() == null || paramOMAresponseforBufDB.getBufferDBChangeParamList().mChangelst == null) {
            Log.d(this.TAG, "DBchange list is empty: do nothting ");
        } else if (paramOMAresponseforBufDB.getBulkResponseList() != null && paramOMAresponseforBufDB.getBulkResponseList().response != null) {
            BufferDBChangeParamList bufferDBChangeParamList = paramOMAresponseforBufDB.getBufferDBChangeParamList();
            int length = paramOMAresponseforBufDB.getBulkResponseList().response.length;
            if (length > bufferDBChangeParamList.mChangelst.size()) {
                length = bufferDBChangeParamList.mChangelst.size();
            }
            for (int i = 0; i < length; i++) {
                if (!(paramOMAresponseforBufDB.getBulkResponseList().response[i].success == null || paramOMAresponseforBufDB.getBulkResponseList().response[i].success.resourceURL == null)) {
                    handleBulkOpSingleUrlSuccess(paramOMAresponseforBufDB.getBulkResponseList().response[i].success.resourceURL.toString());
                    Reference reference = new Reference();
                    reference.resourceURL = paramOMAresponseforBufDB.getBulkResponseList().response[i].success.resourceURL;
                    reference.path = "";
                    onCloudUploadSuccess(new ParamOMAresponseforBufDB.Builder().setReference(reference).setBufferDBChangeParam(bufferDBChangeParamList.mChangelst.get(i)).build());
                }
            }
        }
    }

    private void handleDownloadedImdns(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB.getBufferDBChangeParam() != null && paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex == 13) {
            this.mRcsScheduler.onRcsChatImdnsDownloaded(paramOMAresponseforBufDB);
        }
    }

    private void onAdhocV2tPayloadDownloadFailure(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB.getBufferDBChangeParam() != null && paramOMAresponseforBufDB.getBufferDBChangeParam().mIsAdhocV2t && paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex == 17) {
            this.mVVMScheduler.onAdhocV2tPayloadDownloadFailure(paramOMAresponseforBufDB);
        }
    }
}
