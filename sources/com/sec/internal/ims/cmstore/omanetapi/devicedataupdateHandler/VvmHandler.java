package com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGetVvmProfile;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingSearch;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageVvmProfileAttributePut;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageVvmProfileUpdate;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.omanetapi.nms.CloudMessageGetVVMQuotaInfo;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import com.sec.internal.omanetapi.nms.data.ObjectReferenceList;
import com.sec.internal.omanetapi.nms.data.Reference;
import java.util.ArrayList;
import java.util.Iterator;

public class VvmHandler extends Handler implements IAPICallFlowListener, IControllerCommonInterface {
    private String TAG = VvmHandler.class.getSimpleName();
    private IRetryStackAdapterHelper iRetryStackAdapterHelper;
    private final BufferDBTranslation mBufferDbTranslation;
    private final ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private final INetAPIEventListener mINetAPIEventListener;
    private MessageStoreClient mStoreClient;
    private final RegistrantList mUpdateFromCloudRegistrants = new RegistrantList();
    private boolean mbIsSyncing = false;

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
    }

    public void start() {
    }

    public void stop() {
    }

    public boolean updateDelayRetry(int i, long j) {
        return false;
    }

    public VvmHandler(Looper looper, MessageStoreClient messageStoreClient, INetAPIEventListener iNetAPIEventListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mBufferDbTranslation = new BufferDBTranslation(this.mStoreClient, iCloudMessageManagerHelper);
        this.mINetAPIEventListener = iNetAPIEventListener;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
    }

    public void resetDateFormat() {
        this.mBufferDbTranslation.resetDateFormat();
    }

    public void registerForUpdateFromCloud(Handler handler, int i, Object obj) {
        this.mUpdateFromCloudRegistrants.add(new Registrant(handler, i, obj));
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        String str = this.TAG;
        Log.i(str, "message: " + message.what);
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        if (valueOf != null) {
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[valueOf.ordinal()]) {
                case 1:
                    this.mStoreClient.getRetryMapAdapter().retryApi((Pair) message.obj, this, this.mICloudMessageManagerHelper, (IRetryStackAdapterHelper) null);
                    return;
                case 2:
                    setUpVvmDataUpdate((BufferDBChangeParamList) message.obj);
                    return;
                case 3:
                    this.mStoreClient.getHttpController().execute(new CloudMessageGetVVMQuotaInfo(this, (BufferDBChangeParam) message.obj, this.mStoreClient));
                    return;
                case 4:
                    BufferDBChangeParam bufferDBChangeParam = (BufferDBChangeParam) message.obj;
                    this.mStoreClient.getHttpController().execute(new CloudMessageGreetingSearch(this, "", bufferDBChangeParam.mLine, bufferDBChangeParam, this.mStoreClient));
                    return;
                case 5:
                    deleteGreeting((ParamOMAresponseforBufDB) message.obj);
                    return;
                case 6:
                    Object obj = message.obj;
                    if (obj != null) {
                        deleteGreetingAndSearch((ParamOMAresponseforBufDB) obj);
                        return;
                    }
                    return;
                case 7:
                    uploadGreeting((ParamOMAresponseforBufDB) message.obj);
                    return;
                case 8:
                    ParamOMAresponseforBufDB.Builder bufferDBChangeParam2 = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.VVM_FAX_ERROR_WITH_NO_RETRY).setBufferDBChangeParam((BufferDBChangeParam) message.obj);
                    Message obtainMessage = obtainMessage(OMASyncEventType.VVM_NOTIFY.getId());
                    obtainMessage.obj = bufferDBChangeParam2.build();
                    sendMessage(obtainMessage);
                    return;
                case 9:
                case 10:
                    Message obtainMessage2 = obtainMessage(OMASyncEventType.VVM_NOTIFY.getId());
                    obtainMessage2.obj = message.obj;
                    sendMessage(obtainMessage2);
                    return;
                case 11:
                    this.mINetAPIEventListener.onOmaAuthenticationFailed((ParamOMAresponseforBufDB) message.obj, 0);
                    return;
                case 12:
                    notifyBufferDB((ParamOMAresponseforBufDB) message.obj);
                    return;
                case 13:
                    if (this.mbIsSyncing) {
                        normalSyncRequest();
                        return;
                    } else {
                        sendNormalSyncRequest();
                        return;
                    }
                case 14:
                    notifyBufferDB((ParamOMAresponseforBufDB) message.obj);
                    sendNormalSyncRequest();
                    return;
                case 15:
                    notifyBufferDB((ParamOMAresponseforBufDB) message.obj);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        /* JADX WARNING: Can't wrap try/catch for region: R(32:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|32) */
        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_FALLBACK_TO_LAST_REQUEST     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.EVENT_VVM_DATA_UPDATE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_GET_QUOTA_INFO     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SEARCH_GREETING     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_GREETING     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPLOAD_GREETING     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_CHANGE_ERROR     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_CHANGE_ERROR_REASON     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_CHANGE_SUCCEED     // Catch:{ NoSuchFieldError -> 0x0078 }
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_NOTIFY     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_NORMAL_SYNC_REQUEST     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_NORMAL_SYNC_CONTINUE     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_NORMAL_SYNC_SUMMARY_COMPLETE     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler.AnonymousClass1.<clinit>():void");
        }
    }

    private void deleteGreetingAndSearch(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        String str = this.TAG;
        Log.i(str, "deleteGreetingAndSearch: " + paramOMAresponseforBufDB);
        if (paramOMAresponseforBufDB != null) {
            deleteGreeting(paramOMAresponseforBufDB);
            BufferDBChangeParam bufferDBChangeParam = paramOMAresponseforBufDB.getBufferDBChangeParam();
            this.mStoreClient.getHttpController().execute(new CloudMessageGreetingSearch(this, paramOMAresponseforBufDB.getSearchCursor(), bufferDBChangeParam.mLine, bufferDBChangeParam, this.mStoreClient));
        }
    }

    private void deleteGreeting(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        Object[] objectArr;
        Log.i(this.TAG, "deleteGreeting: " + paramOMAresponseforBufDB);
        if (paramOMAresponseforBufDB != null) {
            ObjectList objectList = paramOMAresponseforBufDB.getObjectList();
            if (objectList == null || (objectArr = objectList.object) == null || objectArr.length == 0) {
                Message obtainMessage = obtainMessage(OMASyncEventType.UPLOAD_GREETING.getId());
                obtainMessage.obj = paramOMAresponseforBufDB;
                sendMessage(obtainMessage);
                return;
            }
            BulkDelete bulkDelete = new BulkDelete();
            bulkDelete.objects = new ObjectReferenceList();
            ArrayList arrayList = new ArrayList();
            for (Object object : objectList.object) {
                Reference reference = new Reference();
                reference.resourceURL = object.resourceURL;
                arrayList.add(reference);
            }
            bulkDelete.objects.objectReference = (Reference[]) arrayList.toArray(new Reference[arrayList.size()]);
            this.mStoreClient.getHttpController().execute(new CloudMessageGreetingBulkDeletion(this, bulkDelete, paramOMAresponseforBufDB.getLine(), paramOMAresponseforBufDB.getSyncMsgType(), paramOMAresponseforBufDB.getBufferDBChangeParam(), this.mStoreClient));
        }
    }

    private void uploadGreeting(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        Object obj;
        if (paramOMAresponseforBufDB != null) {
            BufferDBChangeParam bufferDBChangeParam = paramOMAresponseforBufDB.getBufferDBChangeParam();
            Pair<Object, HttpPostBody> vVMGreetingObjectPairFromBufDb = this.mBufferDbTranslation.getVVMGreetingObjectPairFromBufDb(bufferDBChangeParam);
            ParamVvmUpdate.VvmGreetingType vVMGreetingTypeFromBufDb = this.mBufferDbTranslation.getVVMGreetingTypeFromBufDb(bufferDBChangeParam);
            String str = this.TAG;
            Log.i(str, "uploadGreeting: " + paramOMAresponseforBufDB + " greetingtype: " + vVMGreetingTypeFromBufDb);
            if (ParamVvmUpdate.VvmGreetingType.Default.equals(vVMGreetingTypeFromBufDb)) {
                ParamOMAresponseforBufDB.Builder bufferDBChangeParam2 = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_UPLOADED).setBufferDBChangeParam(bufferDBChangeParam);
                Message obtainMessage = obtainMessage(OMASyncEventType.VVM_CHANGE_SUCCEED.getId());
                obtainMessage.obj = bufferDBChangeParam2.build();
                sendMessage(obtainMessage);
            } else if (vVMGreetingObjectPairFromBufDb == null || (obj = vVMGreetingObjectPairFromBufDb.second) == null || ((HttpPostBody) obj).getMultiparts() == null || ((HttpPostBody) vVMGreetingObjectPairFromBufDb.second).getMultiparts().size() <= 0) {
                sendMessage(obtainMessage(OMASyncEventType.VVM_CHANGE_ERROR.getId(), paramOMAresponseforBufDB.getBufferDBChangeParam()));
            } else {
                this.mStoreClient.getHttpController().execute(new CloudMessageCreateAllObjects(this, new ParamObjectUpload(vVMGreetingObjectPairFromBufDb, bufferDBChangeParam), this.mStoreClient));
            }
        }
    }

    private void gotoHandlerEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendEmptyMessage(i);
        }
    }

    public void sendVvmUpdate(BufferDBChangeParamList bufferDBChangeParamList) {
        Message obtainMessage = obtainMessage(OMASyncEventType.EVENT_VVM_DATA_UPDATE.getId());
        obtainMessage.obj = bufferDBChangeParamList;
        sendMessage(obtainMessage);
    }

    public void getVvmQuota(BufferDBChangeParamList bufferDBChangeParamList) {
        ArrayList<BufferDBChangeParam> arrayList;
        if (bufferDBChangeParamList == null || (arrayList = bufferDBChangeParamList.mChangelst) == null || arrayList.isEmpty()) {
            Log.i(this.TAG, "Empty Buffer List");
            return;
        }
        Message obtainMessage = obtainMessage(OMASyncEventType.VVM_GET_QUOTA_INFO.getId());
        obtainMessage.obj = bufferDBChangeParamList.mChangelst.get(0);
        sendMessage(obtainMessage);
    }

    public void onGoToEvent(int i, Object obj) {
        gotoHandlerEvent(i, obj);
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
        this.mINetAPIEventListener.onOmaSuccess(iHttpAPICommonInterface);
        gotoHandlerEvent(i, obj);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        sendEmptyMessage(OMASyncEventType.VVM_CHANGE_ERROR.getId());
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
            if (searchAndPush && timerValue != -1) {
                sendMessageDelayed(obtainMessage(OMASyncEventType.VVM_FALLBACK_TO_LAST_REQUEST.getId(), pair), timerValue);
            } else if (pair.first instanceof CloudMessageObjectsOpSearchForVvmNormalSync) {
                this.mbIsSyncing = false;
                ParamOMAresponseforBufDB.Builder actionType = new ParamOMAresponseforBufDB.Builder().setLine(this.mStoreClient.getPrerenceManager().getUserTelCtn()).setSyncType(syncMsgType).setActionType(ParamOMAresponseforBufDB.ActionType.VVM_NORMAL_SYNC_SUMMARY_COMPLETE);
                Message message = new Message();
                message.obj = actionType.build();
                message.what = OMASyncEventType.VVM_NORMAL_SYNC_SUMMARY_COMPLETE.getId();
                onFixedFlowWithMessage(message);
            } else {
                sendMessage(obtainMessage(OMASyncEventType.VVM_CHANGE_ERROR.getId(), bufferDBChangeParam));
            }
        }
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
        sendMessage(obtainMessage(OMASyncEventType.VVM_CHANGE_ERROR.getId(), bufferDBChangeParam));
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        sendEmptyMessage(OMASyncEventType.VVM_CHANGE_ERROR.getId());
    }

    public void onFailedEvent(int i, Object obj) {
        gotoHandlerEvent(i, obj);
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        sendMessage(obtainMessage(OMASyncEventType.SELF_RETRY.getId(), Integer.valueOf(i)));
    }

    public void onFixedFlow(int i) {
        sendEmptyMessage(i);
    }

    public void onFixedFlowWithMessage(Message message) {
        if (!(message == null || message.obj == null)) {
            String str = this.TAG;
            Log.i(str, "onFixedFlowWithMessage message is " + message.what);
        }
        sendMessage(message);
    }

    public boolean update(int i) {
        return sendEmptyMessage(i);
    }

    public boolean updateMessage(Message message) {
        return sendMessage(message);
    }

    public boolean updateDelay(int i, long j) {
        String str = this.TAG;
        Log.i(str, "updateDelay: eventType: " + i + " delay: " + j);
        return sendMessageDelayed(obtainMessage(i), j);
    }

    private void notifyBufferDB(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB == null) {
            Log.e(this.TAG, "notifyBufferDB ParamOMAresponseforBufDB is null");
        }
        this.mUpdateFromCloudRegistrants.notifyRegistrants(new AsyncResult((Object) null, paramOMAresponseforBufDB, (Throwable) null));
    }

    private void setUpVvmDataUpdate(BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "setUpVvmDataUpdate param: " + bufferDBChangeParamList);
        Iterator<BufferDBChangeParam> it = bufferDBChangeParamList.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam next = it.next();
            if (next != null) {
                VvmServiceProfile vvmServiceProfile = new VvmServiceProfile();
                switch (next.mDBIndex) {
                    case 17:
                    case 20:
                        ParamVvmUpdate.VvmTypeChange vVMServiceProfileFromBufDb = this.mBufferDbTranslation.getVVMServiceProfileFromBufDb(next, vvmServiceProfile);
                        if (vVMServiceProfileFromBufDb != null) {
                            String str2 = this.TAG;
                            Log.i(str2, "setUpVvmDataUpdate :VvmTypeChange " + vVMServiceProfileFromBufDb.getId());
                            if (!vVMServiceProfileFromBufDb.equals(ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT)) {
                                if (!isProfileAttributePut(vVMServiceProfileFromBufDb)) {
                                    if (!vVMServiceProfileFromBufDb.equals(ParamVvmUpdate.VvmTypeChange.FULLPROFILE)) {
                                        break;
                                    } else {
                                        this.mStoreClient.getHttpController().execute(new CloudMessageGetVvmProfile(this, next, this.mStoreClient));
                                        break;
                                    }
                                } else {
                                    this.mStoreClient.getHttpController().execute(new CloudMessageVvmProfileAttributePut(this, vvmServiceProfile, next, this.mStoreClient));
                                    break;
                                }
                            } else {
                                this.mStoreClient.getHttpController().execute(new CloudMessageVvmProfileUpdate(this, vvmServiceProfile, next, this.mStoreClient));
                                break;
                            }
                        } else {
                            break;
                        }
                    case 18:
                        Message obtainMessage = obtainMessage(OMASyncEventType.SEARCH_GREETING.getId());
                        obtainMessage.obj = next;
                        sendMessage(obtainMessage);
                        break;
                    case 19:
                        this.mBufferDbTranslation.getVVMServiceProfileFromBufDb(next, vvmServiceProfile);
                        this.mStoreClient.getHttpController().execute(new CloudMessageVvmProfileUpdate(this, vvmServiceProfile, next, this.mStoreClient));
                        break;
                }
            }
        }
    }

    public void normalSyncRequest() {
        OMASyncEventType oMASyncEventType = OMASyncEventType.VVM_NORMAL_SYNC_REQUEST;
        removeMessages(oMASyncEventType.getId());
        sendEmptyMessageDelayed(oMASyncEventType.getId(), 1000);
    }

    private void sendNormalSyncRequest() {
        Log.d(this.TAG, "sendNormalSyncRequest ");
        String userTelCtn = this.mStoreClient.getPrerenceManager().getUserTelCtn();
        String str = this.TAG;
        Log.d(str, "line is " + userTelCtn);
        BufferDBTranslation bufferDBTranslation = this.mBufferDbTranslation;
        SyncMsgType syncMsgType = SyncMsgType.VM;
        OMASyncEventType initialSyncStatusByLine = bufferDBTranslation.getInitialSyncStatusByLine(userTelCtn, syncMsgType, CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS);
        if (initialSyncStatusByLine == null) {
            Log.d(this.TAG, "full sync is not complete yet mEventType is null");
        } else if (!OMASyncEventType.INITIAL_SYNC_COMPLETE.equals(initialSyncStatusByLine)) {
            Log.d(this.TAG, "full sync is not complete yet, do normal sync until initial sync is finished");
        } else if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported()) {
            Log.e(this.TAG, "Gba not supported");
        } else if (!TextUtils.isEmpty(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost())) {
            CloudMessageObjectsOpSearchForVvmNormalSync cloudMessageObjectsOpSearchForVvmNormalSync = new CloudMessageObjectsOpSearchForVvmNormalSync(this, (String) null, userTelCtn, syncMsgType, false, this.mStoreClient);
            this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(true);
            this.mStoreClient.getHttpController().execute(cloudMessageObjectsOpSearchForVvmNormalSync);
            this.mbIsSyncing = true;
        } else {
            Log.e(this.TAG, "NMS host is null");
        }
    }

    public void setSyncState(boolean z) {
        this.mbIsSyncing = z;
    }

    public boolean getSyncState() {
        return this.mbIsSyncing;
    }

    private boolean isProfileAttributePut(ParamVvmUpdate.VvmTypeChange vvmTypeChange) {
        return vvmTypeChange.equals(ParamVvmUpdate.VvmTypeChange.ACTIVATE) || vvmTypeChange.equals(ParamVvmUpdate.VvmTypeChange.DEACTIVATE) || vvmTypeChange.equals(ParamVvmUpdate.VvmTypeChange.NUTOFF) || vvmTypeChange.equals(ParamVvmUpdate.VvmTypeChange.NUTON) || vvmTypeChange.equals(ParamVvmUpdate.VvmTypeChange.V2TLANGUAGE) || vvmTypeChange.equals(ParamVvmUpdate.VvmTypeChange.ADHOC_V2T) || vvmTypeChange.equals(ParamVvmUpdate.VvmTypeChange.V2T_SMS) || vvmTypeChange.equals(ParamVvmUpdate.VvmTypeChange.V2T_EMAIL);
    }
}
