package com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslationMcs;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.FileDownloadHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public abstract class BaseDataChangeHandler extends Handler implements IControllerCommonInterface, IAPICallFlowListener {
    private final int NO_RETRY_AFTER_VALUE = -1;
    private String TAG = BaseDataChangeHandler.class.getSimpleName();
    protected boolean isCmsEnabled = false;
    protected final BufferDBTranslation mBufferDBTranslation;
    FileDownloadHandler mFileDownloadHandler;
    protected ICloudMessageManagerHelper mICloudMessageManagerHelper = null;
    private INetAPIEventListener mINetAPIEventListener = null;
    protected boolean mIsFTThumbnailDownload = false;
    protected boolean mIsHandlerRunning = false;
    protected final String mLine;
    private OMANetAPIHandler.OnApiSucceedOnceListener mOnApiSucceedOnceListener = null;
    protected MessageStoreClient mStoreClient;
    protected final SyncMsgType mSyncMsgType;
    private final IUIEventCallback mUIInterface;
    protected final Queue<BufferDBChangeParam> mWorkingQueue = new LinkedList();

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam, SyncMsgType syncMsgType, int i) {
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    /* access modifiers changed from: protected */
    public abstract Pair<HttpRequestParams, Boolean> peekWorkingQueue();

    /* access modifiers changed from: protected */
    public abstract void setWorkingQueue(BufferDBChangeParam bufferDBChangeParam);

    public boolean updateDelayRetry(int i, long j) {
        return false;
    }

    public BaseDataChangeHandler(Looper looper, MessageStoreClient messageStoreClient, INetAPIEventListener iNetAPIEventListener, IUIEventCallback iUIEventCallback, String str, SyncMsgType syncMsgType, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mINetAPIEventListener = iNetAPIEventListener;
        boolean isMcsSupported = CmsUtil.isMcsSupported(this.mStoreClient.getContext(), this.mStoreClient.getClientID());
        this.isCmsEnabled = isMcsSupported;
        if (isMcsSupported) {
            this.mBufferDBTranslation = new BufferDBTranslationMcs(this.mStoreClient, iCloudMessageManagerHelper);
        } else {
            this.mBufferDBTranslation = new BufferDBTranslation(this.mStoreClient, iCloudMessageManagerHelper);
        }
        this.mLine = str;
        this.mUIInterface = iUIEventCallback;
        this.mSyncMsgType = syncMsgType;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mFileDownloadHandler = new FileDownloadHandler(this, looper, messageStoreClient, this.mBufferDBTranslation);
    }

    public void handleMessage(Message message) {
        ArrayList<BufferDBChangeParam> arrayList;
        super.handleMessage(message);
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        Log.i(this.TAG, "message: " + valueOf);
        logWorkingStatus();
        if (valueOf == null) {
            valueOf = OMASyncEventType.DEFAULT;
        }
        boolean z = true;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[valueOf.ordinal()]) {
            case 1:
                this.mIsHandlerRunning = true;
                BufferDBChangeParamList bufferDBChangeParamList = (BufferDBChangeParamList) message.obj;
                if (bufferDBChangeParamList == null || (arrayList = bufferDBChangeParamList.mChangelst) == null || arrayList.size() <= 0) {
                    sendEmptyMessage(OMASyncEventType.OBJECTS_AND_PAYLOAD_DOWNLOAD_COMPLETE.getId());
                    return;
                }
                Log.i(this.TAG, "mWorkingQueue empty: " + this.mWorkingQueue.isEmpty());
                if (this.mWorkingQueue.isEmpty()) {
                    setWorkingQueue(bufferDBChangeParamList);
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                setWorkingQueue(bufferDBChangeParamList);
                return;
            case 2:
                if (!this.mIsHandlerRunning) {
                    this.mIsHandlerRunning = true;
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 3:
                this.mIsHandlerRunning = false;
                return;
            case 4:
                this.mIsHandlerRunning = false;
                this.mWorkingQueue.clear();
                return;
            case 5:
                ParamOMAresponseforBufDB paramOMAresponseforBufDB = (ParamOMAresponseforBufDB) message.obj;
                this.mINetAPIEventListener.onNotificationObjectDownloaded(paramOMAresponseforBufDB);
                Log.i(this.TAG, "mIsFTThumbnailDownload: " + this.mIsFTThumbnailDownload);
                if (!this.mIsFTThumbnailDownload) {
                    this.mWorkingQueue.poll();
                }
                if (this.isCmsEnabled && paramOMAresponseforBufDB != null && paramOMAresponseforBufDB.getActionType() == ParamOMAresponseforBufDB.ActionType.NOTIFICATION_ALL_PAYLOAD_DOWNLOADED) {
                    sendEmptyMessage(OMASyncEventType.OBJECT_FETCH_DOWNLOAD_DONE.getId());
                    return;
                } else if (this.mIsHandlerRunning) {
                    checkNextMsgFromWorkingQueue();
                    return;
                } else {
                    return;
                }
            case 6:
                this.mINetAPIEventListener.onMessageDownloadCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.ALL_PAYLOAD_NOTIFY).setLine(this.mLine).setSyncType(this.mSyncMsgType).build());
                return;
            case 7:
                this.mINetAPIEventListener.onMessageDownloadCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.NOTIFICATION_OBJECTS_DOWNLOAD_COMPLETE).build());
                sendEmptyMessage(OMASyncEventType.NORMAL_SYNC_COMPLETE.getId());
                return;
            case 8:
                this.mINetAPIEventListener.onNormalSyncComplete(false);
                sendEmptyMessage(OMASyncEventType.ONE_LINE_NORMAL_SYNC_COMPLETE.getId());
                return;
            case 9:
                this.mIsHandlerRunning = false;
                ParamOMAresponseforBufDB build = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine).build();
                Object obj = message.obj;
                this.mINetAPIEventListener.onOmaAuthenticationFailed(build, (obj == null || !(obj instanceof Number)) ? 0 : ((Number) obj).longValue());
                return;
            case 11:
                this.mWorkingQueue.clear();
                return;
            case 12:
                this.mINetAPIEventListener.onPauseCMNNetApiWithResumeDelay(((Integer) message.obj).intValue());
                return;
            case 13:
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                return;
            case 14:
                if (this.mWorkingQueue.size() != 0) {
                    z = false;
                }
                setWorkingQueue((BufferDBChangeParamList) message.obj);
                if (this.mIsHandlerRunning && z) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 15:
                if (this.mIsHandlerRunning) {
                    pause();
                    resume();
                    return;
                }
                return;
            case 16:
                Object obj2 = message.obj;
                if (obj2 != null) {
                    onApiTreatAsSucceed((IHttpAPICommonInterface) obj2);
                    return;
                }
                return;
            case 17:
                IHttpAPICommonInterface iHttpAPICommonInterface = (IHttpAPICommonInterface) message.obj;
                if (iHttpAPICommonInterface != null) {
                    this.mStoreClient.getMcsRetryMapAdapter().retryApi(iHttpAPICommonInterface, this);
                    return;
                }
                return;
            case 18:
                Object obj3 = message.obj;
                if (obj3 != null) {
                    HttpResParamsWrapper httpResParamsWrapper = (HttpResParamsWrapper) obj3;
                    onApiTreatAsSucceed(httpResParamsWrapper.mApi);
                    gotoHandlerEvent(OMASyncEventType.DOWNLOAD_RETRIVED.getId(), httpResParamsWrapper.mBufDbParams);
                    return;
                }
                return;
            case 19:
                this.mWorkingQueue.poll();
                if (this.mIsHandlerRunning) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        /* JADX WARNING: Can't wrap try/catch for region: R(38:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|(3:37|38|40)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(40:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|40) */
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
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType[] r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType = r0
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.TRANSIT_TO_START     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.TRANSIT_TO_RESUME     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.TRANSIT_TO_PAUSE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.TRANSIT_TO_STOP     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DOWNLOAD_RETRIVED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_FETCH_DOWNLOAD_DONE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECTS_AND_PAYLOAD_DOWNLOAD_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.NORMAL_SYNC_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREDENTIAL_EXPIRED     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ONE_LINE_NORMAL_SYNC_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CANCEL_DOWNLOADING     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SELF_RETRY     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SYNC_ERR     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ADD_TO_WORKINGQUEUE     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MSTORE_REDIRECT     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_SUCCEED     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_FAILED     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MOVE_ON     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DOWNLOAD_FILE_API_FAILED     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler.AnonymousClass1.<clinit>():void");
        }
    }

    private void gotoHandlerEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendEmptyMessage(i);
        }
    }

    private void gotoHandlerEventOnFailure(IHttpAPICommonInterface iHttpAPICommonInterface) {
        String str = this.TAG;
        Log.i(str, "gotoHandlerEventOnFailure: isRetryEnabled: " + this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled());
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, -1);
        } else {
            sendEmptyMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
        }
    }

    private boolean checkNonAdhocPayloadFail(Object obj) {
        BufferDBChangeParam bufferDBChangeParam;
        boolean isGbaSupported = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported();
        String str = this.TAG;
        Log.i(str, "checkNonAdhocPayloadFail isGbaSupported: " + isGbaSupported);
        if (!isGbaSupported) {
            return true;
        }
        if (obj instanceof ParamOMAresponseforBufDB) {
            bufferDBChangeParam = ((ParamOMAresponseforBufDB) obj).getBufferDBChangeParam();
        } else {
            bufferDBChangeParam = (BufferDBChangeParam) obj;
        }
        ParamOMAresponseforBufDB.Builder bufferDBChangeParam2 = new ParamOMAresponseforBufDB.Builder().setBufferDBChangeParam(bufferDBChangeParam);
        bufferDBChangeParam2.setActionType(ParamOMAresponseforBufDB.ActionType.ADHOC_PAYLOAD_DOWNLOAD_FAILED);
        gotoHandlerEvent(OMASyncEventType.DOWNLOAD_RETRIVED.getId(), bufferDBChangeParam2.build());
        return false;
    }

    public void appendToWorkingQueue(BufferDBChangeParam bufferDBChangeParam) {
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        bufferDBChangeParamList.mChangelst.add(bufferDBChangeParam);
        Message message = new Message();
        message.obj = bufferDBChangeParamList;
        message.what = OMASyncEventType.ADD_TO_WORKINGQUEUE.getId();
        sendMessage(message);
    }

    public void start() {
        sendEmptyMessage(OMASyncEventType.OBJECT_AND_PAYLOAD_DOWNLOAD.getId());
    }

    public void pause() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_PAUSE.getId());
    }

    public void resume() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_RESUME.getId());
    }

    public void stop() {
        pause();
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_STOP.getId());
    }

    public void onGoToEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendEmptyMessage(i);
        }
    }

    private void onApiTreatAsSucceed(IHttpAPICommonInterface iHttpAPICommonInterface) {
        this.mINetAPIEventListener.onOmaSuccess(iHttpAPICommonInterface);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled() && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getControllerOfLastFailedApi() == null && this.mOnApiSucceedOnceListener != null) {
            Log.i(this.TAG, "API in BaseDataChangeHandler succeed, ready to move on");
            this.mOnApiSucceedOnceListener.onMoveOn();
            this.mOnApiSucceedOnceListener = null;
        }
    }

    public void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
        String str = this.TAG;
        Log.d(str, "onMoveOnToNext  " + iHttpAPICommonInterface.getClass().getSimpleName());
        if (checkNonAdhocPayloadFail(obj)) {
            gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(iHttpAPICommonInterface, obj));
        }
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        Log.d(this.TAG, "not used in this handler");
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
        gotoHandlerEvent(OMASyncEventType.API_SUCCEED.getId(), iHttpAPICommonInterface);
        gotoHandlerEvent(i, obj);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        gotoHandlerEventOnFailure(iHttpAPICommonInterface);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
        if (checkNonAdhocPayloadFail(bufferDBChangeParam)) {
            gotoHandlerEventOnFailure(iHttpAPICommonInterface);
        }
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        if (!this.isCmsEnabled) {
            gotoHandlerEventOnFailure(iHttpAPICommonInterface);
        } else if (this.mIsHandlerRunning) {
            onMoveOnToNext(iHttpAPICommonInterface, (Object) null);
        }
    }

    public void onFailedEvent(int i, Object obj) {
        if (checkNonAdhocPayloadFail(obj)) {
            gotoHandlerEvent(i, obj);
        }
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled()) {
            String str2 = this.TAG;
            Log.d(str2, " on Over Request : " + iHttpAPICommonInterface.getClass().getSimpleName() + " errorCode " + str + " retryAfter " + i);
            if (this.isCmsEnabled) {
                sendMessageDelayed(obtainMessage(OMASyncEventType.API_FAILED.getId(), iHttpAPICommonInterface), (long) i);
                return;
            }
            Log.i(this.TAG, "onOverRequest, go to session gen API if necessary");
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, i);
            return;
        }
        sendEmptyMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
    }

    public void onFixedFlowWithMessage(Message message) {
        if (!(message == null || message.obj == null)) {
            String str = this.TAG;
            Log.i(str, "onFixedFlowWithMessage action is " + ((ParamOMAresponseforBufDB) message.obj).getActionType() + " event is " + message.what);
        }
        sendMessage(message);
    }

    public void onFixedFlow(int i) {
        String str = this.TAG;
        Log.i(str, "onFixedFlow event is " + i);
        sendEmptyMessage(i);
    }

    public boolean update(int i) {
        return sendEmptyMessage(i);
    }

    public boolean updateDelay(int i, long j) {
        return sendEmptyMessageDelayed(i, j);
    }

    public boolean updateMessage(Message message) {
        return sendMessage(message);
    }

    /* access modifiers changed from: protected */
    public void checkNextMsgFromWorkingQueue() {
        boolean z;
        if (!this.mWorkingQueue.isEmpty()) {
            Pair<HttpRequestParams, Boolean> peekWorkingQueue = peekWorkingQueue();
            if (peekWorkingQueue == null) {
                this.mIsFTThumbnailDownload = false;
                this.mWorkingQueue.poll();
                checkNextMsgFromWorkingQueue();
            } else if (((Boolean) peekWorkingQueue.second).booleanValue()) {
                Log.i(this.TAG, "checkNextMsgFromWorkingQueue largefile download case");
            } else {
                Object obj = peekWorkingQueue.first;
                if (obj instanceof BaseNMSRequest) {
                    z = ((BaseNMSRequest) obj).updateToken();
                    if (!z) {
                        String str = this.TAG;
                        Log.i(str, "updateToken is null, again using mLine: " + IMSLog.checker(this.mLine));
                        z = ((BaseNMSRequest) peekWorkingQueue.first).updateToken(this.mLine);
                    }
                    if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableATTHeader()) {
                        ((BaseNMSRequest) peekWorkingQueue.first).updateServerRoot(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost());
                    }
                } else {
                    z = true;
                }
                if (z) {
                    this.mStoreClient.getHttpController().execute((HttpRequestParams) peekWorkingQueue.first);
                    return;
                }
                String str2 = this.TAG;
                Log.d(str2, "Url: " + IMSLog.checker(((HttpRequestParams) peekWorkingQueue.first).getUrl()));
                this.mWorkingQueue.poll();
                checkNextMsgFromWorkingQueue();
            }
        } else {
            sendEmptyMessage(OMASyncEventType.OBJECTS_AND_PAYLOAD_DOWNLOAD_COMPLETE.getId());
        }
    }

    /* access modifiers changed from: protected */
    public void setWorkingQueue(BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.d(str, "setWorkingQueue: " + bufferDBChangeParamList);
        Iterator<BufferDBChangeParam> it = bufferDBChangeParamList.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam next = it.next();
            if (next != null) {
                setWorkingQueue(next);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logWorkingStatus() {
        String str = this.TAG;
        Log.d(str, "logWorkingStatus: [mLine: " + IMSLog.checker(this.mLine) + ", mSyncMsgType: " + this.mSyncMsgType + ", mIsHandlerRunning: " + this.mIsHandlerRunning + ", mWorkingQueue size: " + this.mWorkingQueue.size() + "]");
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
        this.mOnApiSucceedOnceListener = onApiSucceedOnceListener;
    }
}
