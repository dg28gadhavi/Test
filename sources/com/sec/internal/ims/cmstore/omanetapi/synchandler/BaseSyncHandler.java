package com.sec.internal.ims.cmstore.omanetapi.synchandler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslationMcs;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkCreation;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateFile;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetAllPayloads;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetLargeFile;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageHeadLargeFile;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
import com.sec.internal.ims.cmstore.params.ParamBulkCreation;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import com.sec.internal.omanetapi.nms.data.Response;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public abstract class BaseSyncHandler extends Handler implements IControllerCommonInterface, IAPICallFlowListener {
    private final int NO_RETRY_AFTER_VALUE = -1;
    private String TAG = BaseSyncHandler.class.getSimpleName();
    private final String TAG_CN = BaseSyncHandler.class.getSimpleName();
    protected boolean isCmsEnabled = false;
    protected final BufferDBTranslation mBufferDBTranslation;
    protected ParamBulkCreation mBulkCreation = null;
    protected final Queue<BufferDBChangeParam> mBulkUploadQueue = new LinkedList();
    protected OMASyncEventType mEventType;
    FileHandler mFileHandler;
    protected ICloudMessageManagerHelper mICloudMessageManagerHelper;
    protected final INetAPIEventListener mINetAPIEventListener;
    protected boolean mIsFTThumbnailDownload = false;
    protected boolean mIsFullSync = false;
    protected boolean mIsHandlerRunning = false;
    protected boolean mIsSearchFinished = false;
    protected final String mLine;
    private OMANetAPIHandler.OnApiSucceedOnceListener mOnApiSucceedOnceListener = null;
    protected String mSearchCursor;
    protected MessageStoreClient mStoreClient;
    protected final SyncMsgType mSyncMsgType;
    protected final IUIEventCallback mUIInterface;
    protected final Queue<BufferDBChangeParam> mWorkingDownloadQueue = new LinkedList();
    protected final HashSet<Pair<Integer, Long>> mWorkingDownloadSet = new HashSet<>();
    protected final Queue<BufferDBChangeParam> mWorkingUploadQueue = new LinkedList();

    public enum SyncOperation {
        DOWNLOAD,
        UPLOAD,
        BULK_UPLOAD
    }

    /* access modifiers changed from: protected */
    public abstract void makeBulkUploadparameter();

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
    }

    /* access modifiers changed from: protected */
    public abstract HttpRequestParams peekBulkUploadQueue();

    /* access modifiers changed from: protected */
    public abstract Pair<HttpRequestParams, Boolean> peekDownloadQueue();

    /* access modifiers changed from: protected */
    public abstract Pair<HttpRequestParams, Boolean> peekUploadQueue();

    /* access modifiers changed from: protected */
    public abstract void setBulkUploadQueue(BufferDBChangeParamList bufferDBChangeParamList);

    /* access modifiers changed from: protected */
    public abstract void setWorkingQueue(BufferDBChangeParam bufferDBChangeParam, SyncOperation syncOperation);

    public boolean updateDelayRetry(int i, long j) {
        return false;
    }

    BaseSyncHandler(Looper looper, MessageStoreClient messageStoreClient, INetAPIEventListener iNetAPIEventListener, IUIEventCallback iUIEventCallback, String str, SyncMsgType syncMsgType, ICloudMessageManagerHelper iCloudMessageManagerHelper, boolean z) {
        super(looper);
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mINetAPIEventListener = iNetAPIEventListener;
        boolean isMcsSupported = CmsUtil.isMcsSupported(this.mStoreClient.getContext(), this.mStoreClient.getClientID());
        this.isCmsEnabled = isMcsSupported;
        if (isMcsSupported) {
            BufferDBTranslationMcs bufferDBTranslationMcs = new BufferDBTranslationMcs(this.mStoreClient, iCloudMessageManagerHelper);
            this.mBufferDBTranslation = bufferDBTranslationMcs;
            this.mFileHandler = new FileHandler(this, looper, bufferDBTranslationMcs, messageStoreClient);
        } else {
            this.mBufferDBTranslation = new BufferDBTranslation(this.mStoreClient, iCloudMessageManagerHelper);
        }
        this.mUIInterface = iUIEventCallback;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mLine = str;
        this.mIsFullSync = z;
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableFolderIdInSearch()) {
            this.mSyncMsgType = syncMsgType;
        } else {
            this.mSyncMsgType = SyncMsgType.DEFAULT;
        }
        this.mSearchCursor = this.mBufferDBTranslation.getSearchCursorByLine(str, this.mSyncMsgType);
        OMASyncEventType initialSyncStatusByLine = this.mBufferDBTranslation.getInitialSyncStatusByLine(str, this.mSyncMsgType, CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS);
        this.mEventType = initialSyncStatusByLine;
        if (OMASyncEventType.INITIAL_SYNC_COMPLETE.equals(initialSyncStatusByLine) || OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE.equals(this.mEventType)) {
            this.mIsSearchFinished = true;
        }
    }

    public void resetSearchParam() {
        this.mSearchCursor = this.mBufferDBTranslation.getSearchCursorByLine(this.mLine, this.mSyncMsgType);
        this.mEventType = this.mBufferDBTranslation.getInitialSyncStatusByLine(this.mLine, this.mSyncMsgType, CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS);
        this.mWorkingDownloadQueue.clear();
        this.mWorkingUploadQueue.clear();
        this.mWorkingDownloadSet.clear();
        String str = this.TAG;
        Log.d(str, "resetSearchParam, cursor: " + this.mSearchCursor + " event: " + this.mEventType);
    }

    public void setIsFullSyncParam(boolean z) {
        this.mIsFullSync = z;
        String str = this.TAG;
        Log.i(str, "setIsFullSyncParam, mIsFullSync: " + this.mIsFullSync);
    }

    public boolean getIsFullSyncParam() {
        return this.mIsFullSync;
    }

    public void setInitSyncComplete() {
        this.mIsHandlerRunning = false;
        this.mIsSearchFinished = true;
        this.mWorkingDownloadQueue.clear();
        if (!this.isCmsEnabled) {
            this.mWorkingUploadQueue.clear();
        }
        this.mWorkingDownloadSet.clear();
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldClearCursorUponInitSyncDone()) {
            this.mSearchCursor = "";
        }
        this.mEventType = null;
        logWorkingStatus();
    }

    public void appendToWorkingQueue(BufferDBChangeParamList bufferDBChangeParamList, SyncOperation syncOperation) {
        String str = this.TAG;
        Log.d(str, "appendToWorkingQueue: " + syncOperation);
        if (SyncOperation.BULK_UPLOAD.equals(syncOperation)) {
            Message message = new Message();
            message.obj = bufferDBChangeParamList;
            message.what = OMASyncEventType.ADD_TO_QUEUE_BULKUPLOAD.getId();
            sendMessage(message);
        }
    }

    public void appendToWorkingQueue(BufferDBChangeParam bufferDBChangeParam, SyncOperation syncOperation) {
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        bufferDBChangeParamList.mChangelst.add(bufferDBChangeParam);
        if (SyncOperation.DOWNLOAD.equals(syncOperation)) {
            Message message = new Message();
            message.obj = bufferDBChangeParamList;
            message.what = OMASyncEventType.ADD_TO_WORKINGQUEUE.getId();
            sendMessage(message);
        } else if (SyncOperation.UPLOAD.equals(syncOperation)) {
            Message message2 = new Message();
            message2.obj = bufferDBChangeParamList;
            message2.what = OMASyncEventType.ADD_TO_UPLOADWORKINGQUEUE.getId();
            sendMessage(message2);
        }
    }

    public void handleMessage(Message message) {
        int i;
        super.handleMessage(message);
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        Log.i(this.TAG, "message: " + valueOf);
        logWorkingStatus();
        if (valueOf == null) {
            valueOf = OMASyncEventType.DEFAULT;
        }
        boolean z = false;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[valueOf.ordinal()]) {
            case 1:
                this.mIsHandlerRunning = true;
                this.mINetAPIEventListener.onInitialSyncStarted();
                this.mStoreClient.getHttpController().execute(new CloudMessageObjectsOpSearch(this, this.mSearchCursor, this.mLine, this.mSyncMsgType, false, this.mStoreClient, this.mIsFullSync));
                this.mIsSearchFinished = false;
                this.mUIInterface.showInitsyncIndicator(true);
                return;
            case 2:
                if (this.mIsHandlerRunning) {
                    this.mINetAPIEventListener.onPartialSyncSummaryCompleted((ParamOMAresponseforBufDB) message.obj);
                    this.mStoreClient.getHttpController().execute(new CloudMessageObjectsOpSearch(this, this.mSearchCursor, this.mLine, this.mSyncMsgType, false, this.mStoreClient, this.mIsFullSync));
                    this.mUIInterface.showInitsyncIndicator(true);
                }
                this.mIsSearchFinished = false;
                return;
            case 3:
                this.mStoreClient.getHttpController().execute(new CloudMessageObjectsOpSearch(this, this.mSearchCursor, this.mLine, this.mSyncMsgType, true, this.mStoreClient, this.mIsFullSync));
                this.mIsSearchFinished = false;
                this.mUIInterface.showInitsyncIndicator(true);
                return;
            case 4:
                this.mIsHandlerRunning = false;
                this.mINetAPIEventListener.onSyncFailed(new ParamOMAresponseforBufDB.Builder().setOMASyncEventType(OMASyncEventType.PAUSE_INITIAL_SYNC).setLine(this.mLine).setSyncType(this.mSyncMsgType).setIsFullSync(this.mIsFullSync).setActionType(ParamOMAresponseforBufDB.ActionType.SYNC_FAILED).build());
                return;
            case 5:
                if (!this.mIsHandlerRunning) {
                    this.mIsHandlerRunning = true;
                    if (!this.mIsSearchFinished) {
                        this.mStoreClient.getHttpController().execute(new CloudMessageObjectsOpSearch(this, this.mSearchCursor, this.mLine, this.mSyncMsgType, false, this.mStoreClient, this.mIsFullSync));
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else if (this.mWorkingDownloadQueue.size() > 0) {
                        checkNextMsgFromDownloadWorkingQueue(SyncOperation.DOWNLOAD);
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else if (this.mWorkingUploadQueue.size() > 0) {
                        checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else if (this.mBulkCreation != null) {
                        retryBulkUploadRequest();
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else if (this.mBulkUploadQueue.size() > 0) {
                        checkNextBulkUploadWorkingQueue();
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            case 6:
                this.mIsHandlerRunning = false;
                return;
            case 7:
                this.mIsHandlerRunning = false;
                this.mWorkingDownloadQueue.clear();
                this.mWorkingUploadQueue.clear();
                this.mWorkingDownloadSet.clear();
                if (this.isCmsEnabled) {
                    this.mFileHandler.stop();
                }
                if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldClearCursorUponInitSyncDone()) {
                    this.mSearchCursor = "";
                }
                this.mEventType = null;
                ParamOMAresponseforBufDB build = new ParamOMAresponseforBufDB.Builder().setOMASyncEventType(OMASyncEventType.CANCEL_INITIAL_SYNC).setMStoreClient(this.mStoreClient).setLine(this.mLine).setSyncType(this.mSyncMsgType).setIsFullSync(this.mIsFullSync).setActionType(ParamOMAresponseforBufDB.ActionType.SYNC_FAILED).build();
                this.mUIInterface.showInitsyncIndicator(false);
                this.mINetAPIEventListener.onSyncFailed(build);
                return;
            case 8:
                this.mINetAPIEventListener.onInitSyncCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_COMPLETE).setOMASyncEventType(valueOf).setMStoreClient(this.mStoreClient).setLine(this.mLine).setIsFullSync(this.mIsFullSync).setSyncType(this.mSyncMsgType).build());
                sendEmptyMessage(OMASyncEventType.ONE_LINE_INIT_SYNC_COMPLETE.getId());
                return;
            case 9:
                this.mIsSearchFinished = true;
                this.mINetAPIEventListener.onInitSyncSummaryCompleted((ParamOMAresponseforBufDB) message.obj);
                return;
            case 10:
                Log.i(this.TAG, "empty queue: " + this.mWorkingUploadQueue.isEmpty());
                if (this.mWorkingUploadQueue.isEmpty()) {
                    sendEmptyMessage(OMASyncEventType.OBJECT_END_UPLOAD.getId());
                    return;
                } else {
                    checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                    return;
                }
            case 11:
                this.mINetAPIEventListener.onOneMessageUploaded((ParamOMAresponseforBufDB) message.obj);
                this.mWorkingUploadQueue.poll();
                checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                return;
            case 12:
                this.mWorkingUploadQueue.poll();
                checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                return;
            case 13:
                this.mINetAPIEventListener.onOneMessageUploaded((ParamOMAresponseforBufDB) message.obj);
                this.mBulkCreation = null;
                checkIndividualResponseCodeUpload((ParamOMAresponseforBufDB) message.obj);
                checkNextBulkUploadWorkingQueue();
                return;
            case 14:
                this.mBulkCreation = null;
                fallbackOneMessageUplaod((ParamOMAresponseforBufDB) message.obj);
                checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                return;
            case 15:
                this.mINetAPIEventListener.onMessageUploadCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MESSAGE_UPLOAD_COMPLETE).setLine(this.mLine).build());
                if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isBulkCreationEnabled() && !this.mBulkUploadQueue.isEmpty()) {
                    gotoHandlerEvent(OMASyncEventType.OBJECT_BULK_UPLOAD_COMPLETED.getId(), (Object) null);
                    return;
                } else if (!this.isCmsEnabled) {
                    sendEmptyMessage(OMASyncEventType.INITIAL_SYNC_COMPLETE.getId());
                    return;
                } else {
                    return;
                }
            case 16:
                Log.i(this.TAG, "empty queue: " + this.mWorkingUploadQueue.isEmpty());
                if (!this.mWorkingDownloadQueue.isEmpty()) {
                    checkNextMsgFromDownloadWorkingQueue(SyncOperation.DOWNLOAD);
                    return;
                } else if (this.isCmsEnabled) {
                    sendEmptyMessage(OMASyncEventType.OBJECT_FETCH_DOWNLOAD_DONE.getId());
                    return;
                } else {
                    sendEmptyMessage(OMASyncEventType.OBJECT_END_DOWNLOAD.getId());
                    return;
                }
            case 17:
                Object obj = message.obj;
                if (obj != null) {
                    this.mINetAPIEventListener.onOneMessageDownloaded((ParamOMAresponseforBufDB) obj);
                    if (!this.mIsFTThumbnailDownload) {
                        pollFromDownloadSet();
                        this.mWorkingDownloadQueue.poll();
                    }
                    checkNextMsgFromDownloadWorkingQueue(SyncOperation.DOWNLOAD);
                    return;
                }
                return;
            case 18:
                pollFromDownloadSet();
                this.mWorkingDownloadQueue.poll();
                checkNextMsgFromDownloadWorkingQueue(SyncOperation.DOWNLOAD);
                return;
            case 19:
                this.mINetAPIEventListener.onMessageDownloadCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MESSAGE_DOWNLOAD_COMPLETE).setLine(this.mLine).setSyncType(this.mSyncMsgType).build());
                return;
            case 20:
                this.mINetAPIEventListener.onMessageDownloadCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.ALL_PAYLOAD_NOTIFY).setLine(this.mLine).setSyncType(this.mSyncMsgType).build());
                return;
            case 21:
                if (this.mIsHandlerRunning) {
                    pause();
                    resume();
                    return;
                }
                return;
            case 22:
                ParamOMAresponseforBufDB build2 = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine).build();
                Object obj2 = message.obj;
                this.mINetAPIEventListener.onOmaAuthenticationFailed(build2, (obj2 == null || !(obj2 instanceof Number)) ? 0 : ((Number) obj2).longValue());
                return;
            case 23:
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                return;
            case 25:
                this.mINetAPIEventListener.onPauseCMNNetApiWithResumeDelay(((Integer) message.obj).intValue());
                return;
            case 26:
                if (this.mWorkingDownloadQueue.size() == 0) {
                    z = true;
                }
                SyncOperation syncOperation = SyncOperation.DOWNLOAD;
                setWorkingQueue((BufferDBChangeParamList) message.obj, syncOperation);
                if (this.mIsHandlerRunning && z) {
                    checkNextMsgFromDownloadWorkingQueue(syncOperation);
                    this.mUIInterface.showInitsyncIndicator(true);
                    return;
                }
                return;
            case 27:
                if (this.mWorkingUploadQueue.size() == 0) {
                    z = true;
                }
                SyncOperation syncOperation2 = SyncOperation.UPLOAD;
                setWorkingQueue((BufferDBChangeParamList) message.obj, syncOperation2);
                if (z) {
                    this.mINetAPIEventListener.onInitUploadStarted(new ParamOMAresponseforBufDB.Builder().setLine(this.mLine).setOMASyncEventType(OMASyncEventType.INITIAL_UPLOAD_STARTED).setActionType(ParamOMAresponseforBufDB.ActionType.INIT_UPLOAD_STARTED).build());
                    if (this.mIsHandlerRunning) {
                        checkNextMsgFromUploadWorkingQueue(syncOperation2);
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    }
                    return;
                }
                return;
            case 28:
                if (this.mBulkUploadQueue.size() == 0) {
                    z = true;
                }
                setBulkUploadQueue((BufferDBChangeParamList) message.obj);
                if (this.mIsHandlerRunning && z) {
                    checkNextBulkUploadWorkingQueue();
                    this.mUIInterface.showInitsyncIndicator(true);
                    return;
                }
                return;
            case 29:
                Object obj3 = message.obj;
                if (obj3 != null) {
                    onApiTreatAsSucceed((IHttpAPICommonInterface) obj3);
                    return;
                }
                return;
            case 30:
                IHttpAPICommonInterface iHttpAPICommonInterface = (IHttpAPICommonInterface) message.obj;
                if (iHttpAPICommonInterface != null) {
                    this.mStoreClient.getMcsRetryMapAdapter().retryApi(iHttpAPICommonInterface, this);
                    return;
                }
                return;
            case 31:
                Object obj4 = message.obj;
                if (obj4 != null) {
                    HttpResParamsWrapper httpResParamsWrapper = (HttpResParamsWrapper) obj4;
                    onApiTreatAsSucceed(httpResParamsWrapper.mApi);
                    IHttpAPICommonInterface iHttpAPICommonInterface2 = httpResParamsWrapper.mApi;
                    if (iHttpAPICommonInterface2 instanceof CloudMessageCreateAllObjects) {
                        i = OMASyncEventType.OBJECT_ONE_UPLOAD_COMPLETED.getId();
                    } else if (iHttpAPICommonInterface2 instanceof CloudMessageObjectsOpSearch) {
                        i = OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE.getId();
                    } else if (iHttpAPICommonInterface2 instanceof CloudMessageBulkCreation) {
                        ParamOMAresponseforBufDB paramOMAresponseforBufDB = (ParamOMAresponseforBufDB) httpResParamsWrapper.mBufDbParams;
                        if (paramOMAresponseforBufDB == null || ParamOMAresponseforBufDB.ActionType.FALLBACK_MESSAGES_UPLOADED != paramOMAresponseforBufDB.getActionType()) {
                            i = OMASyncEventType.OBJECT_BULK_UPLOAD_COMPLETED.getId();
                        } else {
                            i = OMASyncEventType.FALLBACK_ONE_UPLOAD.getId();
                        }
                    } else if (iHttpAPICommonInterface2 instanceof CloudMessageCreateFile) {
                        i = OMASyncEventType.OBJECT_FT_UPLOAD_FAILED.getId();
                    } else if ((iHttpAPICommonInterface2 instanceof CloudMessageGetLargeFile) || (iHttpAPICommonInterface2 instanceof CloudMessageHeadLargeFile)) {
                        i = OMASyncEventType.DOWNLOAD_FILE_API_FAILED.getId();
                    } else {
                        i = OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.getId();
                    }
                    gotoHandlerEvent(i, httpResParamsWrapper.mBufDbParams);
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        /* JADX WARNING: Can't wrap try/catch for region: R(64:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|64) */
        /* JADX WARNING: Code restructure failed: missing block: B:65:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x009c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x00b4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x00c0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x00cc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x00d8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x00e4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x00f0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00fc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x0108 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x0114 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x0120 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:51:0x012c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:53:0x0138 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:55:0x0144 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:57:0x0150 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:59:0x015c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:61:0x0168 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType[] r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType = r0
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.START_INITIAL_SYNC     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.REQUEST_OPSEARCH_AFTER_PSF_REMOVED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.PAUSE_INITIAL_SYNC     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.TRANSIT_TO_RESUME     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.TRANSIT_TO_PAUSE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CANCEL_INITIAL_SYNC     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_START_UPLOAD     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_ONE_UPLOAD_COMPLETED     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_FT_UPLOAD_FAILED     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_BULK_UPLOAD_COMPLETED     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.FALLBACK_ONE_UPLOAD     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_END_UPLOAD     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_START_DOWNLOAD     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DOWNLOAD_FILE_API_FAILED     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_END_DOWNLOAD     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00f0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_FETCH_DOWNLOAD_DONE     // Catch:{ NoSuchFieldError -> 0x00f0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f0 }
                r2 = 20
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00f0 }
            L_0x00f0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00fc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MSTORE_REDIRECT     // Catch:{ NoSuchFieldError -> 0x00fc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fc }
                r2 = 21
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00fc }
            L_0x00fc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0108 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREDENTIAL_EXPIRED     // Catch:{ NoSuchFieldError -> 0x0108 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0108 }
                r2 = 22
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0108 }
            L_0x0108:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0114 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SYNC_ERR     // Catch:{ NoSuchFieldError -> 0x0114 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0114 }
                r2 = 23
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0114 }
            L_0x0114:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0120 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ONE_LINE_INIT_SYNC_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0120 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0120 }
                r2 = 24
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0120 }
            L_0x0120:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x012c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SELF_RETRY     // Catch:{ NoSuchFieldError -> 0x012c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x012c }
                r2 = 25
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x012c }
            L_0x012c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0138 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ADD_TO_WORKINGQUEUE     // Catch:{ NoSuchFieldError -> 0x0138 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0138 }
                r2 = 26
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0138 }
            L_0x0138:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0144 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ADD_TO_UPLOADWORKINGQUEUE     // Catch:{ NoSuchFieldError -> 0x0144 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0144 }
                r2 = 27
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0144 }
            L_0x0144:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0150 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ADD_TO_QUEUE_BULKUPLOAD     // Catch:{ NoSuchFieldError -> 0x0150 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0150 }
                r2 = 28
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0150 }
            L_0x0150:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x015c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_SUCCEED     // Catch:{ NoSuchFieldError -> 0x015c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x015c }
                r2 = 29
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x015c }
            L_0x015c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0168 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_FAILED     // Catch:{ NoSuchFieldError -> 0x0168 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0168 }
                r2 = 30
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0168 }
            L_0x0168:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0174 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MOVE_ON     // Catch:{ NoSuchFieldError -> 0x0174 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0174 }
                r2 = 31
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0174 }
            L_0x0174:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler.AnonymousClass1.<clinit>():void");
        }
    }

    private void gotoHandlerEvent(int i, Object obj) {
        if (obj != null) {
            if (obj instanceof ParamOMAresponseforBufDB) {
                ParamOMAresponseforBufDB paramOMAresponseforBufDB = (ParamOMAresponseforBufDB) obj;
                if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldClearCursorUponInitSyncDone()) {
                    this.mSearchCursor = paramOMAresponseforBufDB.getSearchCursor();
                } else if (!TextUtils.isEmpty(paramOMAresponseforBufDB.getSearchCursor())) {
                    this.mSearchCursor = paramOMAresponseforBufDB.getSearchCursor();
                }
                this.mEventType = paramOMAresponseforBufDB.getOMASyncEventType();
                String str = this.TAG;
                Log.i(str, "update cursor: [" + this.mSearchCursor + "], and event type: [" + this.mEventType + "]");
            }
            sendMessage(obtainMessage(i, obj));
            return;
        }
        sendEmptyMessage(i);
    }

    private void gotoHandlerEventOnFailure(IHttpAPICommonInterface iHttpAPICommonInterface) {
        boolean isRetryEnabled = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled();
        boolean isGbaSupported = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported();
        String str = this.TAG;
        Log.i(str, "gotoHandlerEventOnFailure isGbaSupported: " + isGbaSupported + ", isRetryEnabled: " + isRetryEnabled);
        if (isGbaSupported && ((iHttpAPICommonInterface instanceof CloudMessageObjectsOpSearch) || (iHttpAPICommonInterface instanceof CloudMessageGetAllPayloads))) {
            Log.i(this.TAG, "gotoHandlerEventOnFailure for TMO fail case");
            sendEmptyMessage(OMASyncEventType.CANCEL_INITIAL_SYNC.getId());
        } else if (isRetryEnabled) {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, -1);
        } else {
            sendEmptyMessage(OMASyncEventType.PAUSE_INITIAL_SYNC.getId());
        }
    }

    public void start() {
        start(this.mLine);
    }

    /* access modifiers changed from: protected */
    public void start(String str) {
        String str2 = this.TAG_CN;
        int clientID = this.mStoreClient.getClientID();
        EventLogHelper.infoLogAndAdd(str2, clientID, "start: " + IMSLog.checker(str) + " mEventType: " + this.mEventType);
        OMASyncEventType oMASyncEventType = this.mEventType;
        if (oMASyncEventType == null) {
            sendEmptyMessage(OMASyncEventType.START_INITIAL_SYNC.getId());
            return;
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[oMASyncEventType.ordinal()];
        if (i == 2) {
            this.mIsHandlerRunning = true;
            sendEmptyMessage(OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH.getId());
        } else if (i == 8) {
            sendEmptyMessage(OMASyncEventType.ONE_LINE_INIT_SYNC_COMPLETE.getId());
        } else if (i != 9) {
            sendEmptyMessage(OMASyncEventType.START_INITIAL_SYNC.getId());
        }
    }

    public void pause() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_PAUSE.getId());
    }

    public void resume() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_RESUME.getId());
    }

    public void stop() {
        sendEmptyMessage(OMASyncEventType.CANCEL_INITIAL_SYNC.getId());
    }

    public boolean update(int i) {
        String str = this.TAG;
        Log.i(str, "update with " + i);
        return sendEmptyMessage(i);
    }

    public boolean updateDelay(int i, long j) {
        String str = this.TAG;
        Log.i(str, "update with " + i + " delayed " + j);
        return sendEmptyMessageDelayed(i, j);
    }

    public boolean updateMessage(Message message) {
        return sendMessage(message);
    }

    private void onApiTreatAsSucceed(IHttpAPICommonInterface iHttpAPICommonInterface) {
        OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener;
        this.mINetAPIEventListener.onOmaSuccess(iHttpAPICommonInterface);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled() && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getControllerOfLastFailedApi() == null && (onApiSucceedOnceListener = this.mOnApiSucceedOnceListener) != null) {
            onApiSucceedOnceListener.onMoveOn();
            this.mOnApiSucceedOnceListener = null;
        }
    }

    public void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
        String str = this.TAG;
        Log.d(str, "onMoveOnToNext  " + iHttpAPICommonInterface.getClass().getSimpleName());
        gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(iHttpAPICommonInterface, obj));
    }

    public void onGoToEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendEmptyMessage(i);
        }
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
        gotoHandlerEvent(OMASyncEventType.API_SUCCEED.getId(), iHttpAPICommonInterface);
        gotoHandlerEvent(i, obj);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        String str2 = this.TAG;
        Log.i(str2, "onFailedCall code :" + str);
        gotoHandlerEventOnFailure(iHttpAPICommonInterface);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
        gotoHandlerEventOnFailure(iHttpAPICommonInterface);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        if (!this.isCmsEnabled) {
            gotoHandlerEventOnFailure(iHttpAPICommonInterface);
        } else if (this.mIsHandlerRunning) {
            onMoveOnToNext(iHttpAPICommonInterface, (Object) null);
        }
    }

    public void onFailedEvent(int i, Object obj) {
        gotoHandlerEvent(i, obj);
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        String str2 = this.TAG;
        Log.i(str2, iHttpAPICommonInterface.getClass().getSimpleName() + str + ", retry after isRetryEnabled: " + this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled());
        if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled()) {
            gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(iHttpAPICommonInterface, (Object) null));
        } else if (this.isCmsEnabled) {
            sendMessageDelayed(obtainMessage(OMASyncEventType.API_FAILED.getId(), iHttpAPICommonInterface), (long) i);
        } else {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, i);
        }
    }

    public void onFixedFlow(int i) {
        String str = this.TAG;
        Log.i(str, "onFixedFlow event is " + i);
        sendEmptyMessage(i);
    }

    public void onFixedFlowWithMessage(Message message) {
        Object obj;
        if (message == null || (obj = message.obj) == null) {
            Log.e(this.TAG, "onFixedFlowWithMessage message is null");
        } else if (!(obj instanceof ParamOMAresponseforBufDB)) {
            Log.e(this.TAG, "onFixedFlowWithMessage message not ParamOMAresponseforBufDB");
        } else {
            String str = this.TAG;
            Log.i(str, "onFixedFlowWithMessage message is " + ((ParamOMAresponseforBufDB) message.obj).getActionType());
            ParamOMAresponseforBufDB paramOMAresponseforBufDB = (ParamOMAresponseforBufDB) message.obj;
            if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldClearCursorUponInitSyncDone()) {
                this.mSearchCursor = paramOMAresponseforBufDB.getSearchCursor();
            } else if (!TextUtils.isEmpty(paramOMAresponseforBufDB.getSearchCursor())) {
                this.mSearchCursor = paramOMAresponseforBufDB.getSearchCursor();
            }
            this.mEventType = paramOMAresponseforBufDB.getOMASyncEventType();
            sendMessage(message);
        }
    }

    /* access modifiers changed from: protected */
    public void pollFromDownloadSet() {
        BufferDBChangeParam peek = this.mWorkingDownloadQueue.peek();
        if (peek != null) {
            Pair pair = new Pair(Integer.valueOf(peek.mDBIndex), Long.valueOf(peek.mRowId));
            if (this.mWorkingDownloadSet.contains(pair)) {
                this.mWorkingDownloadSet.remove(pair);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkNextMsgFromDownloadWorkingQueue(SyncOperation syncOperation) {
        String str = this.TAG;
        Log.i(str, "checkNextMsgFromDownloadWorkingQueue: " + syncOperation);
        if (!this.mWorkingDownloadQueue.isEmpty()) {
            Pair<HttpRequestParams, Boolean> peekDownloadQueue = peekDownloadQueue();
            String str2 = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("checkNextMsgFromDownloadWorkingQueue: fileReq:");
            sb.append(peekDownloadQueue != null ? (Boolean) peekDownloadQueue.second : null);
            Log.i(str2, sb.toString());
            if (peekDownloadQueue != null && ((Boolean) peekDownloadQueue.second).booleanValue()) {
                Log.i(this.TAG, "checkNextMsgFromDownloadWorkingQueue largefile download case ");
            } else if (peekDownloadQueue == null) {
                Log.i(this.TAG, "checkNextMsgFromDownloadWorkingQueue httpparam null ");
                this.mIsFTThumbnailDownload = false;
                pollFromDownloadSet();
                this.mWorkingDownloadQueue.poll();
                checkNextMsgFromDownloadWorkingQueue(syncOperation);
            } else {
                Object obj = peekDownloadQueue.first;
                if (obj instanceof BaseNMSRequest) {
                    ((BaseNMSRequest) obj).updateToken();
                    ((BaseNMSRequest) peekDownloadQueue.first).replaceUrlPrefix();
                }
                String str3 = this.TAG;
                Log.i(str3, "url : " + IMSLog.checker(((HttpRequestParams) peekDownloadQueue.first).getUrl()) + " ; method: " + ((HttpRequestParams) peekDownloadQueue.first).getMethod());
                if (TextUtils.isEmpty(((HttpRequestParams) peekDownloadQueue.first).getUrl()) || ((HttpRequestParams) peekDownloadQueue.first).getMethod() == null) {
                    pollFromDownloadSet();
                    this.mWorkingDownloadQueue.poll();
                    checkNextMsgFromDownloadWorkingQueue(syncOperation);
                    return;
                }
                this.mStoreClient.getHttpController().execute((HttpRequestParams) peekDownloadQueue.first);
            }
        } else if (SyncOperation.DOWNLOAD.equals(syncOperation)) {
            if (this.isCmsEnabled) {
                sendEmptyMessage(OMASyncEventType.OBJECT_FETCH_DOWNLOAD_DONE.getId());
            } else {
                sendEmptyMessage(OMASyncEventType.OBJECT_END_DOWNLOAD.getId());
            }
        } else if (SyncOperation.UPLOAD.equals(syncOperation)) {
            sendEmptyMessage(OMASyncEventType.OBJECT_END_UPLOAD.getId());
        }
    }

    /* access modifiers changed from: protected */
    public void checkNextMsgFromUploadWorkingQueue(SyncOperation syncOperation) {
        String str = this.TAG;
        Log.i(str, "checkNextMsgFromUploadWorkingQueue: " + syncOperation);
        if (!this.mWorkingUploadQueue.isEmpty()) {
            Pair<HttpRequestParams, Boolean> peekUploadQueue = peekUploadQueue();
            String str2 = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("checkNextMsgFromUploadWorkingQueue: fileReq:");
            sb.append(peekUploadQueue != null ? (Boolean) peekUploadQueue.second : null);
            Log.i(str2, sb.toString());
            if (peekUploadQueue != null && ((Boolean) peekUploadQueue.second).booleanValue()) {
                return;
            }
            if (peekUploadQueue == null) {
                Log.i(this.TAG, "checkNextMsgFromUploadWorkingQueue: http param is null");
                this.mWorkingUploadQueue.poll();
                checkNextMsgFromUploadWorkingQueue(syncOperation);
                return;
            }
            HttpRequestParams httpRequestParams = (HttpRequestParams) peekUploadQueue.first;
            if (httpRequestParams instanceof BaseNMSRequest) {
                BaseNMSRequest baseNMSRequest = (BaseNMSRequest) httpRequestParams;
                baseNMSRequest.updateToken();
                baseNMSRequest.replaceUrlPrefix();
            }
            String str3 = this.TAG;
            Log.i(str3, "url : " + IMSLog.checker(httpRequestParams.getUrl()) + " ; method: " + httpRequestParams.getMethod());
            if (TextUtils.isEmpty(httpRequestParams.getUrl()) || httpRequestParams.getMethod() == null) {
                this.mWorkingUploadQueue.poll();
                checkNextMsgFromUploadWorkingQueue(syncOperation);
                return;
            }
            this.mStoreClient.getHttpController().execute(httpRequestParams);
        } else if (SyncOperation.DOWNLOAD.equals(syncOperation)) {
            if (this.isCmsEnabled) {
                sendEmptyMessage(OMASyncEventType.OBJECT_FETCH_DOWNLOAD_DONE.getId());
            } else {
                sendEmptyMessage(OMASyncEventType.OBJECT_END_DOWNLOAD.getId());
            }
        } else if (SyncOperation.UPLOAD.equals(syncOperation)) {
            sendEmptyMessage(OMASyncEventType.OBJECT_END_UPLOAD.getId());
        }
    }

    /* access modifiers changed from: protected */
    public void checkNextBulkUploadWorkingQueue() {
        String str = this.TAG;
        Log.i(str, "checkNextBulkUploadWorkingQueue: mBulkUploadQueue is empty: " + this.mBulkUploadQueue.isEmpty());
        if (!this.mBulkUploadQueue.isEmpty()) {
            makeBulkUploadparameter();
            retryBulkUploadRequest();
            return;
        }
        sendEmptyMessage(OMASyncEventType.OBJECT_END_UPLOAD.getId());
    }

    /* access modifiers changed from: protected */
    public void retryBulkUploadRequest() {
        HttpRequestParams peekBulkUploadQueue = peekBulkUploadQueue();
        if (peekBulkUploadQueue == null) {
            checkNextBulkUploadWorkingQueue();
            return;
        }
        if (peekBulkUploadQueue instanceof BaseNMSRequest) {
            BaseNMSRequest baseNMSRequest = (BaseNMSRequest) peekBulkUploadQueue;
            baseNMSRequest.updateToken();
            baseNMSRequest.replaceUrlPrefix();
        }
        String str = this.TAG;
        Log.i(str, "retryBulkUploadRequest url : " + peekBulkUploadQueue.getUrl() + " ; method: " + peekBulkUploadQueue.getMethod());
        if (TextUtils.isEmpty(peekBulkUploadQueue.getUrl()) || peekBulkUploadQueue.getMethod() == null) {
            checkNextBulkUploadWorkingQueue();
        } else {
            this.mStoreClient.getHttpController().execute(peekBulkUploadQueue);
        }
    }

    /* access modifiers changed from: protected */
    public void setWorkingQueue(BufferDBChangeParamList bufferDBChangeParamList, SyncOperation syncOperation) {
        Iterator<BufferDBChangeParam> it = bufferDBChangeParamList.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam next = it.next();
            if (next != null) {
                setWorkingQueue(next, syncOperation);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logWorkingStatus() {
        String str = this.TAG;
        Log.d(str, "logWorkingStatus: [mSyncMsgType: " + this.mSyncMsgType + " mIsHandlerRunning: " + this.mIsHandlerRunning + " mEventType: " + this.mEventType + " mIsSearchFinished: " + this.mIsSearchFinished + " mWorkingDownloadQueue size: " + this.mWorkingDownloadQueue.size() + " mWorkingDownloadSet size: " + this.mWorkingDownloadSet.size() + " mWorkingUploadQueue size: " + this.mWorkingUploadQueue.size() + " mBulkUploadQueue size: " + this.mBulkUploadQueue.size() + " mLine: " + IMSLog.checker(this.mLine) + "]");
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
        this.mOnApiSucceedOnceListener = onApiSucceedOnceListener;
    }

    private void fallbackOneMessageUplaod(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB == null || paramOMAresponseforBufDB.getBufferDBChangeParamList() == null || paramOMAresponseforBufDB.getBufferDBChangeParamList().mChangelst == null) {
            Log.d(this.TAG, "DBchange list is empty: do nothting ");
        } else {
            setWorkingQueue(paramOMAresponseforBufDB.getBufferDBChangeParamList(), SyncOperation.UPLOAD);
        }
    }

    private void checkIndividualResponseCodeUpload(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        Log.i(this.TAG, "checkIndividualResponseCodeUpload: ");
        if (paramOMAresponseforBufDB != null && paramOMAresponseforBufDB.getBufferDBChangeParamList() != null && paramOMAresponseforBufDB.getBufferDBChangeParamList().mChangelst != null) {
            int i = 0;
            for (int i2 = 0; i2 < paramOMAresponseforBufDB.getBulkResponseList().response.length; i2++) {
                Response response = paramOMAresponseforBufDB.getBulkResponseList().response[i2];
                short s = response.code;
                if ((s == 403 || s == 503) && !this.mStoreClient.getCloudMessageStrategyManager().getStrategy().bulkOpTreatSuccessIndividualResponse(response.code)) {
                    setWorkingQueue(paramOMAresponseforBufDB.getBufferDBChangeParamList().mChangelst.get(i2), SyncOperation.UPLOAD);
                    i++;
                }
            }
            if (i > 0) {
                checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
            }
        }
    }
}
