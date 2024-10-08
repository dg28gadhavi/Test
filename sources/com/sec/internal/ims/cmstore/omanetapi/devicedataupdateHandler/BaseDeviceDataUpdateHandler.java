package com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.data.OperationEnum;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslationMcs;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkUpdate;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePutObjectFlag;
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
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import com.sec.internal.omanetapi.nms.data.BulkUpdate;
import com.sec.internal.omanetapi.nms.data.FlagList;
import com.sec.internal.omanetapi.nms.data.ObjectReferenceList;
import com.sec.internal.omanetapi.nms.data.Reference;
import com.sec.internal.omanetapi.nms.data.Response;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class BaseDeviceDataUpdateHandler extends Handler implements IAPICallFlowListener, IControllerCommonInterface {
    private final int NO_RETRY_AFTER_VALUE = -1;
    public String TAG = BaseDeviceDataUpdateHandler.class.getSimpleName();
    protected boolean isCmsEnabled = false;
    protected final BufferDBTranslation mBufferDBTranslation;
    private final INetAPIEventListener mINetAPIEventListener;
    protected boolean mIsHandlerRunning = false;
    protected String mLine;
    private OMANetAPIHandler.OnApiSucceedOnceListener mOnApiSucceedOnceListener = null;
    protected MessageStoreClient mStoreClient;
    protected SyncMsgType mSyncMsgType;
    protected final Queue<HttpRequestParams> mWorkingQueue = new LinkedList();

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onFixedFlow(int i) {
    }

    public void onGoToEvent(int i, Object obj) {
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
    public abstract void setWorkingQueue(BufferDBChangeParam bufferDBChangeParam);

    /* access modifiers changed from: protected */
    public abstract void setWorkingQueue(BufferDBChangeParamList bufferDBChangeParamList);

    public boolean updateDelayRetry(int i, long j) {
        return false;
    }

    public BaseDeviceDataUpdateHandler(Looper looper, MessageStoreClient messageStoreClient, INetAPIEventListener iNetAPIEventListener, String str, SyncMsgType syncMsgType, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
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
        this.mSyncMsgType = syncMsgType;
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        Log.i(this.TAG, "message :: " + valueOf);
        logWorkingStatus();
        if (valueOf == null) {
            valueOf = OMASyncEventType.DEFAULT;
        }
        boolean z = true;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[valueOf.ordinal()]) {
            case 1:
                this.mIsHandlerRunning = true;
                this.mINetAPIEventListener.onDeviceFlagUpdateSchedulerStarted();
                checkNextMsgFromWorkingQueue();
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
            case 6:
                this.mINetAPIEventListener.onOneDeviceFlagUpdated((ParamOMAresponseforBufDB) message.obj);
                this.mWorkingQueue.poll();
                if (this.mIsHandlerRunning) {
                    sendEmptyMessage(OMASyncEventType.UPDATE_NEXT.getId());
                    return;
                }
                return;
            case 7:
                this.mINetAPIEventListener.onOneDeviceFlagUpdated((ParamOMAresponseforBufDB) message.obj);
                this.mWorkingQueue.poll();
                sendEmptyMessage(OMASyncEventType.UPDATE_NEXT.getId());
                return;
            case 8:
                if (this.mWorkingQueue.isEmpty()) {
                    sendEmptyMessage(OMASyncEventType.UPDATE_COMPLETED.getId());
                    return;
                } else {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
            case 9:
                this.mINetAPIEventListener.onDeviceFlagUpdateCompleted((ParamOMAresponseforBufDB) message.obj);
                sendEmptyMessage(OMASyncEventType.ONE_LINE_FLAG_SYNC_COMPLETE.getId());
                return;
            case 11:
                this.mIsHandlerRunning = false;
                ParamOMAresponseforBufDB build = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine).build();
                Object obj = message.obj;
                this.mINetAPIEventListener.onOmaAuthenticationFailed(build, (obj == null || !(obj instanceof Number)) ? 0 : ((Number) obj).longValue());
                return;
            case 12:
                if (this.mIsHandlerRunning) {
                    pause();
                    resume();
                    return;
                }
                return;
            case 13:
                if (this.mWorkingQueue.size() != 0) {
                    z = false;
                }
                setWorkingQueue((BufferDBChangeParamList) message.obj);
                if (this.mIsHandlerRunning && z) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 14:
                if (this.mWorkingQueue.size() != 0) {
                    z = false;
                }
                this.mWorkingQueue.offer((CloudMessageBulkDeletion) message.obj);
                if (this.mIsHandlerRunning && z) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 15:
                if (this.mWorkingQueue.size() != 0) {
                    z = false;
                }
                this.mWorkingQueue.offer((CloudMessageBulkUpdate) message.obj);
                if (this.mIsHandlerRunning && z) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 16:
                this.mWorkingQueue.poll();
                if (this.mWorkingQueue.size() != 0) {
                    z = false;
                }
                fallbackOneMessageUpdate(message.obj);
                if (this.mIsHandlerRunning && z) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 17:
                this.mINetAPIEventListener.onOneDeviceFlagUpdated((ParamOMAresponseforBufDB) message.obj);
                this.mWorkingQueue.poll();
                handleSuccessBulkOpResponse(message.obj);
                if (this.mIsHandlerRunning) {
                    sendEmptyMessage(OMASyncEventType.UPDATE_NEXT.getId());
                    return;
                }
                return;
            case 18:
                this.mINetAPIEventListener.onPauseCMNNetApiWithResumeDelay(((Integer) message.obj).intValue());
                return;
            case 19:
                Object obj2 = message.obj;
                if (obj2 != null) {
                    onApiTreatAsSucceed((IHttpAPICommonInterface) obj2);
                    return;
                }
                return;
            case 20:
                IHttpAPICommonInterface iHttpAPICommonInterface = (IHttpAPICommonInterface) message.obj;
                if (iHttpAPICommonInterface != null) {
                    this.mStoreClient.getMcsRetryMapAdapter().retryApi(iHttpAPICommonInterface, this);
                    return;
                }
                return;
            case 21:
                Object obj3 = message.obj;
                if (obj3 != null) {
                    HttpResParamsWrapper httpResParamsWrapper = (HttpResParamsWrapper) obj3;
                    onApiTreatAsSucceed(httpResParamsWrapper.mApi);
                    sendMessage(obtainMessage(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId(), httpResParamsWrapper.mBufDbParams));
                    return;
                }
                return;
            case 22:
                this.mStoreClient.getRetryMapAdapter().retryApi((Pair) message.obj, this, (ICloudMessageManagerHelper) null, (IRetryStackAdapterHelper) null);
                return;
            case 23:
                this.mINetAPIEventListener.onOneMessageUploaded((ParamOMAresponseforBufDB) message.obj);
                this.mWorkingQueue.poll();
                checkNextMsgFromWorkingQueue();
                return;
            default:
                return;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        /* JADX WARNING: Can't wrap try/catch for region: R(48:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|48) */
        /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.PUT_OBJECT     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_ONE_SUCCESSFUL     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_FAILED     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_NEXT     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_COMPLETED     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ONE_LINE_FLAG_SYNC_COMPLETE     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREDENTIAL_EXPIRED     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MSTORE_REDIRECT     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ADD_TO_WORKINGQUEUE     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ADD_TO_QUEUE_BULKDELETE     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ADD_TO_QUEUE_BULKUPDATE     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.FALLBACK_ONE_UPDATE_OR_DELETE     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.BULK_UPDATE_OR_DELETE_SUCCESSFUL     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SELF_RETRY     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_SUCCEED     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00f0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_FAILED     // Catch:{ NoSuchFieldError -> 0x00f0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f0 }
                r2 = 20
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00f0 }
            L_0x00f0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00fc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MOVE_ON     // Catch:{ NoSuchFieldError -> 0x00fc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fc }
                r2 = 21
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00fc }
            L_0x00fc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0108 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_FALLBACK_TO_LAST_REQUEST     // Catch:{ NoSuchFieldError -> 0x0108 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0108 }
                r2 = 22
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0108 }
            L_0x0108:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0114 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_ONE_UPLOAD_COMPLETED     // Catch:{ NoSuchFieldError -> 0x0114 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0114 }
                r2 = 23
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0114 }
            L_0x0114:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler.AnonymousClass1.<clinit>():void");
        }
    }

    public void appendToWorkingQueue(BufferDBChangeParamList bufferDBChangeParamList) {
        Message message = new Message();
        message.obj = bufferDBChangeParamList;
        message.what = OMASyncEventType.ADD_TO_WORKINGQUEUE.getId();
        sendMessage(message);
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
        this.mINetAPIEventListener.onOmaSuccess(iHttpAPICommonInterface);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled() && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getControllerOfLastFailedApi() == null && this.mOnApiSucceedOnceListener != null) {
            Log.i(this.TAG, "API in BaseDeviceDataUpdateHandler succeed, ready to move on");
            this.mOnApiSucceedOnceListener.onMoveOn();
            this.mOnApiSucceedOnceListener = null;
        }
    }

    public void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
        sendMessage(obtainMessage(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(iHttpAPICommonInterface, obj)));
    }

    private void handleFailedBulkDeleteResponse(IHttpAPICommonInterface iHttpAPICommonInterface) {
        if (iHttpAPICommonInterface != null && (iHttpAPICommonInterface instanceof CloudMessageBulkDeletion)) {
            CloudMessageBulkDeletion cloudMessageBulkDeletion = (CloudMessageBulkDeletion) iHttpAPICommonInterface;
            boolean z = !this.mStoreClient.getCloudMessageStrategyManager().getStrategy().bulkOpTreatSuccessRequestResponse(cloudMessageBulkDeletion.getResponseCode());
            Log.i(this.TAG, "shouldRetry: " + z + " getRetryCount: " + cloudMessageBulkDeletion.getRetryCount());
            if (z && cloudMessageBulkDeletion.getRetryCount() < 1) {
                cloudMessageBulkDeletion.increaseRetryCount();
                sendMessage(obtainMessage(OMASyncEventType.ADD_TO_QUEUE_BULKDELETE.getId(), cloudMessageBulkDeletion));
            }
        }
    }

    private void handleFailedBulkUpdateResponse(IHttpAPICommonInterface iHttpAPICommonInterface) {
        if (iHttpAPICommonInterface instanceof CloudMessageBulkUpdate) {
            CloudMessageBulkUpdate cloudMessageBulkUpdate = (CloudMessageBulkUpdate) iHttpAPICommonInterface;
            boolean z = !this.mStoreClient.getCloudMessageStrategyManager().getStrategy().bulkOpTreatSuccessRequestResponse(cloudMessageBulkUpdate.getResponseCode());
            Log.i(this.TAG, "handleSuccessBulkOpResponse shouldRetry: " + z + " getRetryCount: " + cloudMessageBulkUpdate.getRetryCount());
            if (z && cloudMessageBulkUpdate.getRetryCount() < 1) {
                cloudMessageBulkUpdate.increaseRetryCount();
                sendMessage(obtainMessage(OMASyncEventType.ADD_TO_QUEUE_BULKUPDATE.getId(), cloudMessageBulkUpdate));
            }
        }
    }

    private void handleSuccessBulkOpResponse(Object obj) {
        if (obj != null && (obj instanceof ParamOMAresponseforBufDB)) {
            ParamOMAresponseforBufDB paramOMAresponseforBufDB = (ParamOMAresponseforBufDB) obj;
            if (paramOMAresponseforBufDB.getBulkResponseList() != null) {
                for (int i = 0; i < paramOMAresponseforBufDB.getBulkResponseList().response.length; i++) {
                    Response response = paramOMAresponseforBufDB.getBulkResponseList().response[i];
                    if (response.code == 403 && !this.mStoreClient.getCloudMessageStrategyManager().getStrategy().bulkOpTreatSuccessIndividualResponse(response.code)) {
                        setWorkingQueue(paramOMAresponseforBufDB.getBufferDBChangeParamList().mChangelst.get(i));
                    }
                }
            }
        }
    }

    private void gotoHandlerEventOnSuccess(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendEmptyMessage(i);
        }
    }

    private void gotoHandlerEventOnFailure(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendEmptyMessage(i);
        }
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
        sendMessage(obtainMessage(OMASyncEventType.API_SUCCEED.getId(), iHttpAPICommonInterface));
        gotoHandlerEventOnSuccess(iHttpAPICommonInterface, i, obj);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        onFailedCall(iHttpAPICommonInterface);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam, SyncMsgType syncMsgType, int i) {
        String str = this.TAG;
        Log.i(str, "onFailedCall, SyncMsgType : " + syncMsgType);
        if (syncMsgType == SyncMsgType.VM || syncMsgType == SyncMsgType.VM_GREETINGS) {
            Pair pair = new Pair(iHttpAPICommonInterface, syncMsgType);
            boolean searchAndPush = this.mStoreClient.getRetryMapAdapter().searchAndPush(pair, i);
            long timerValue = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getTimerValue(i);
            String str2 = this.TAG;
            Log.i(str2, "Timer Value : " + timerValue + ", isRetryAvailable: " + searchAndPush);
            if (!searchAndPush || timerValue == -1) {
                ParamOMAresponseforBufDB.Builder line = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine);
                if (iHttpAPICommonInterface instanceof CloudMessageDeleteIndividualObject) {
                    line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_DELETE_UPDATE_FAILED).setBufferDBChangeParam(bufferDBChangeParam);
                } else if (iHttpAPICommonInterface instanceof CloudMessagePutObjectFlag) {
                    line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_READ_UPDATE_FAILED).setBufferDBChangeParam(bufferDBChangeParam);
                } else if ((iHttpAPICommonInterface instanceof CloudMessageBulkDeletion) || (iHttpAPICommonInterface instanceof CloudMessageBulkUpdate)) {
                    line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE);
                }
                sendMessage(obtainMessage(OMASyncEventType.UPDATE_FAILED.getId(), line.build()));
                return;
            }
            sendMessageDelayed(obtainMessage(OMASyncEventType.VVM_FALLBACK_TO_LAST_REQUEST.getId(), pair), timerValue);
        }
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, -1);
        } else if (this.mIsHandlerRunning) {
            ParamOMAresponseforBufDB.Builder line = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine);
            if (iHttpAPICommonInterface instanceof CloudMessageDeleteIndividualObject) {
                line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_DELETE_UPDATE_FAILED).setBufferDBChangeParam(bufferDBChangeParam);
            } else if (iHttpAPICommonInterface instanceof CloudMessagePutObjectFlag) {
                line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_READ_UPDATE_FAILED).setBufferDBChangeParam(bufferDBChangeParam);
            } else if (iHttpAPICommonInterface instanceof CloudMessageBulkDeletion) {
                line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE);
                handleFailedBulkDeleteResponse(iHttpAPICommonInterface);
            } else if (iHttpAPICommonInterface instanceof CloudMessageBulkUpdate) {
                line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE);
                handleFailedBulkUpdateResponse(iHttpAPICommonInterface);
            }
            sendMessage(obtainMessage(OMASyncEventType.UPDATE_FAILED.getId(), line.build()));
        }
    }

    public void onFailedEvent(int i, Object obj) {
        gotoHandlerEventOnFailure(i, obj);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled() && !this.isCmsEnabled) {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, -1);
        } else if (this.mIsHandlerRunning) {
            ParamOMAresponseforBufDB.Builder line = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine);
            if (iHttpAPICommonInterface instanceof CloudMessageDeleteIndividualObject) {
                line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_DELETE_UPDATE_FAILED);
            } else if (iHttpAPICommonInterface instanceof CloudMessagePutObjectFlag) {
                line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_READ_UPDATE_FAILED);
            } else if (iHttpAPICommonInterface instanceof CloudMessageBulkDeletion) {
                line.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE);
                handleFailedBulkDeleteResponse(iHttpAPICommonInterface);
            }
            sendMessage(obtainMessage(OMASyncEventType.UPDATE_FAILED.getId(), line.build()));
        }
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled()) {
            String str2 = this.TAG;
            Log.i(str2, " on Over Request : " + iHttpAPICommonInterface.getClass().getSimpleName() + " errorCode " + str + " retryAfter " + i);
            if (this.isCmsEnabled) {
                sendMessageDelayed(obtainMessage(OMASyncEventType.API_FAILED.getId(), iHttpAPICommonInterface), (long) i);
            } else {
                this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, i);
            }
        } else {
            sendEmptyMessage(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId());
        }
    }

    public void onFixedFlowWithMessage(Message message) {
        if (!(message == null || message.obj == null)) {
            String str = this.TAG;
            Log.i(str, "onFixedFlowWithMessage message is " + ((ParamOMAresponseforBufDB) message.obj).getActionType());
        }
        sendMessage(message);
    }

    public void start() {
        this.mIsHandlerRunning = true;
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_START.getId());
    }

    public void pause() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_PAUSE.getId());
    }

    public void resume() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_RESUME.getId());
    }

    public void stop() {
        this.mIsHandlerRunning = false;
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_STOP.getId());
    }

    /* access modifiers changed from: protected */
    public void checkNextMsgFromWorkingQueue() {
        boolean z;
        if (!this.mWorkingQueue.isEmpty()) {
            HttpRequestParams peek = this.mWorkingQueue.peek();
            if (peek == null) {
                this.mWorkingQueue.poll();
                Log.e(this.TAG, " Should not be Null. Skip the current and plz check enqueue");
                checkNextMsgFromWorkingQueue();
                return;
            }
            if (peek instanceof BaseNMSRequest) {
                BaseNMSRequest baseNMSRequest = (BaseNMSRequest) peek;
                z = baseNMSRequest.updateToken();
                if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableATTHeader()) {
                    baseNMSRequest.updateServerRoot(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost());
                }
            } else {
                z = true;
            }
            if (z) {
                this.mStoreClient.getHttpController().execute(peek);
                return;
            }
            String str = this.TAG;
            Log.d(str, "Url: " + IMSLog.checker(peek.getUrl()));
            this.mWorkingQueue.poll();
            checkNextMsgFromWorkingQueue();
            return;
        }
        sendEmptyMessage(OMASyncEventType.UPDATE_COMPLETED.getId());
    }

    /* access modifiers changed from: protected */
    public void logWorkingStatus() {
        String str = this.TAG;
        Log.i(str, "mLine: " + IMSLog.checker(this.mLine) + " logWorkingStatus: [mSyncMsgType: " + this.mSyncMsgType + " mIsHandlerRunning: " + this.mIsHandlerRunning + " mWorkingQueue size: " + this.mWorkingQueue.size() + "]");
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
        this.mOnApiSucceedOnceListener = onApiSucceedOnceListener;
    }

    /* access modifiers changed from: protected */
    public BulkUpdate createNewBulkUpdateParam(List<Reference> list, String[] strArr, OperationEnum operationEnum) {
        BulkUpdate bulkUpdate = new BulkUpdate();
        bulkUpdate.operation = operationEnum;
        FlagList flagList = new FlagList();
        bulkUpdate.flags = flagList;
        flagList.flag = strArr;
        ObjectReferenceList objectReferenceList = new ObjectReferenceList();
        bulkUpdate.objects = objectReferenceList;
        objectReferenceList.objectReference = (Reference[]) list.toArray(new Reference[list.size()]);
        return bulkUpdate;
    }

    /* access modifiers changed from: protected */
    public BulkDelete createNewBulkDeleteParam(List<Reference> list) {
        BulkDelete bulkDelete = new BulkDelete();
        ObjectReferenceList objectReferenceList = new ObjectReferenceList();
        bulkDelete.objects = objectReferenceList;
        objectReferenceList.objectReference = (Reference[]) list.toArray(new Reference[list.size()]);
        return bulkDelete;
    }

    private void fallbackOneMessageUpdate(Object obj) {
        if (obj != null && (obj instanceof ParamOMAresponseforBufDB)) {
            ParamOMAresponseforBufDB paramOMAresponseforBufDB = (ParamOMAresponseforBufDB) obj;
            if (paramOMAresponseforBufDB.getBufferDBChangeParamList() != null && paramOMAresponseforBufDB.getBufferDBChangeParamList().mChangelst != null) {
                Iterator<BufferDBChangeParam> it = paramOMAresponseforBufDB.getBufferDBChangeParamList().mChangelst.iterator();
                while (it.hasNext()) {
                    setWorkingQueue(it.next());
                }
            }
        }
    }
}
