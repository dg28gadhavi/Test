package com.sec.internal.ims.cmstore.omanetapi.synchandler;

import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetLargeFile;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageHeadLargeFile;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.CmsHttpController;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.omanetapi.file.LargeFileDownloadParams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileDownloadHandler extends StateMachine implements IControllerCommonInterface, IAPICallFlowListener {
    /* access modifiers changed from: private */
    public String TAG = FileDownloadHandler.class.getSimpleName();
    String mAccessURL;
    BufferDBTranslation mBufferDBTranslation;
    IAPICallFlowListener mCallFlowListener;
    State mDefaultState = new DefaultState();
    State mDownloadCompletedState = new DownloadCompletedState();
    State mDownloadingPartsState = new DownloadingPartsState();
    String mFileName;
    LargeFileDownloadParams mLargeFileDownloadParams;
    String mLocalFilePath;
    int mMaxRange;
    BufferDBChangeParam mParam;
    int mPartNum;
    State mRetrievingHeadState = new RetrievingHeadState();
    MessageStoreClient mStoreClient;
    IAPICallFlowListener mSyncHandlerCallFlowListener;
    int mTotalLength;
    int mTotalParts;

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam, SyncMsgType syncMsgType, int i) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
    }

    public void onFailedEvent(int i, Object obj) {
    }

    public void onFixedFlow(int i) {
    }

    public void onFixedFlowWithMessage(Message message) {
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

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
    }

    public void stop() {
    }

    public boolean update(int i) {
        return false;
    }

    public boolean updateDelay(int i, long j) {
        return false;
    }

    public boolean updateDelayRetry(int i, long j) {
        return false;
    }

    public boolean updateMessage(Message message) {
        return false;
    }

    public FileDownloadHandler(IAPICallFlowListener iAPICallFlowListener, Looper looper, MessageStoreClient messageStoreClient, BufferDBTranslation bufferDBTranslation) {
        super("FileDownloadHandler", looper);
        String str = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str;
        Log.i(str, "FileDownloadHandler Constructor");
        this.mStoreClient = messageStoreClient;
        this.mCallFlowListener = this;
        this.mSyncHandlerCallFlowListener = iAPICallFlowListener;
        this.mBufferDBTranslation = bufferDBTranslation;
        this.mMaxRange = 5991763;
        resetAllParams();
        initStates();
        start();
    }

    private void initStates() {
        addState(this.mDefaultState);
        setInitialState(this.mDefaultState);
        addState(this.mRetrievingHeadState, this.mDefaultState);
        addState(this.mDownloadingPartsState, this.mRetrievingHeadState);
        addState(this.mDownloadCompletedState, this.mDownloadingPartsState);
        super.start();
    }

    public void start() {
        Log.i(this.TAG, "start()");
    }

    public void start(String str, BufferDBChangeParam bufferDBChangeParam) {
        String str2 = this.TAG;
        Log.i(str2, "start() mAccessURL: " + this.mAccessURL);
        this.mAccessURL = str;
        this.mParam = bufferDBChangeParam;
        sendMessage(OMASyncEventType.START_LARGE_FILE_DOWNLOAD.getId());
    }

    /* access modifiers changed from: private */
    public void resetAllParams() {
        Log.i(this.TAG, "resetAllParams");
        this.mTotalLength = 0;
        this.mPartNum = 0;
        this.mTotalParts = 0;
        this.mLargeFileDownloadParams = null;
        this.mParam = null;
        this.mAccessURL = "";
    }

    public class DefaultState extends State {
        public DefaultState() {
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = FileDownloadHandler.this.InitEvent(message);
            String r1 = FileDownloadHandler.this.TAG;
            Log.i(r1, "processMessage: event " + InitEvent);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            boolean z = true;
            if (i == 1) {
                FileDownloadHandler.this.sendMessage(OMASyncEventType.DOWNLOAD_FILE_HEAD.getId());
                FileDownloadHandler fileDownloadHandler = FileDownloadHandler.this;
                fileDownloadHandler.transitionTo(fileDownloadHandler.mRetrievingHeadState);
            } else if (i == 2) {
                FileDownloadHandler.this.sendMessage(OMASyncEventType.RESET_STATE.getId());
                FileDownloadHandler.this.mSyncHandlerCallFlowListener.onGoToEvent(OMASyncEventType.DOWNLOAD_FILE_API_FAILED.getId(), (Object) null);
            } else if (i == 3) {
                FileDownloadHandler fileDownloadHandler2 = FileDownloadHandler.this;
                fileDownloadHandler2.transitionTo(fileDownloadHandler2.mDefaultState);
                FileUtils.removeFile(FileDownloadHandler.this.mLocalFilePath);
                FileDownloadHandler.this.resetAllParams();
            } else if (i != 4) {
                z = false;
            } else {
                IHttpAPICommonInterface iHttpAPICommonInterface = (IHttpAPICommonInterface) message.obj;
                if (iHttpAPICommonInterface != null) {
                    FileDownloadHandler.this.mStoreClient.getMcsRetryMapAdapter().retryApi(iHttpAPICommonInterface, FileDownloadHandler.this);
                }
            }
            if (z) {
                FileDownloadHandler fileDownloadHandler3 = FileDownloadHandler.this;
                fileDownloadHandler3.log("DefaultState, Handled : " + InitEvent);
            }
            return z;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.synchandler.FileDownloadHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|(3:17|18|20)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.START_LARGE_FILE_DOWNLOAD     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DOWNLOAD_FILE_API_FAILED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESET_STATE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_FAILED     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DOWNLOAD_FILE_HEAD     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DOWNLOAD_FILE_HEAD_COMPLETED     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DOWNLOAD_FILE_PART     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DOWNLOADED_PART     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DOWNLOAD_FILE_PART_COMPLETED     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.synchandler.FileDownloadHandler.AnonymousClass1.<clinit>():void");
        }
    }

    private class RetrievingHeadState extends State {
        private RetrievingHeadState() {
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = FileDownloadHandler.this.InitEvent(message);
            String r1 = FileDownloadHandler.this.TAG;
            Log.i(r1, "RetrievingHeadState processMessage " + message.what);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            boolean z = true;
            if (i == 5) {
                CmsHttpController httpController = FileDownloadHandler.this.mStoreClient.getHttpController();
                FileDownloadHandler fileDownloadHandler = FileDownloadHandler.this;
                httpController.execute(new CloudMessageHeadLargeFile(fileDownloadHandler.mCallFlowListener, fileDownloadHandler.mStoreClient, fileDownloadHandler.mAccessURL));
            } else if (i != 6) {
                z = false;
            } else {
                FileDownloadHandler fileDownloadHandler2 = FileDownloadHandler.this;
                LargeFileDownloadParams largeFileDownloadParams = (LargeFileDownloadParams) message.obj;
                fileDownloadHandler2.mLargeFileDownloadParams = largeFileDownloadParams;
                String str = largeFileDownloadParams.contentLength;
                if (str != null) {
                    fileDownloadHandler2.mTotalLength = Integer.parseInt(str);
                }
                FileDownloadHandler fileDownloadHandler3 = FileDownloadHandler.this;
                int i2 = fileDownloadHandler3.mTotalLength;
                if (i2 <= 0 || fileDownloadHandler3.mLargeFileDownloadParams.acceptRanges == null) {
                    fileDownloadHandler3.sendMessage(OMASyncEventType.DOWNLOAD_FILE_API_FAILED.getId());
                } else {
                    fileDownloadHandler3.mTotalParts = (int) Math.ceil(((double) i2) / ((double) fileDownloadHandler3.mMaxRange));
                }
                String r7 = FileDownloadHandler.this.TAG;
                Log.i(r7, "totalParts: " + FileDownloadHandler.this.mTotalParts + ", totalLength: " + FileDownloadHandler.this.mTotalLength + ", maxRange " + FileDownloadHandler.this.mMaxRange);
                String str2 = FileDownloadHandler.this.mLargeFileDownloadParams.contentDisposition;
                if (str2 != null && str2.contains(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)) {
                    FileDownloadHandler fileDownloadHandler4 = FileDownloadHandler.this;
                    fileDownloadHandler4.mFileName = fileDownloadHandler4.mLargeFileDownloadParams.contentDisposition.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)[1];
                    FileDownloadHandler fileDownloadHandler5 = FileDownloadHandler.this;
                    fileDownloadHandler5.mFileName = fileDownloadHandler5.mFileName.replaceAll(CmcConstants.E_NUM_STR_QUOTE, "");
                }
                String r72 = FileDownloadHandler.this.TAG;
                Log.i(r72, "fileName: " + FileDownloadHandler.this.mFileName + ", mAcceptRanges: " + FileDownloadHandler.this.mLargeFileDownloadParams.acceptRanges + ", mContentType: " + FileDownloadHandler.this.mLargeFileDownloadParams.contentType);
                FileDownloadHandler.this.sendMessage(OMASyncEventType.DOWNLOAD_FILE_PART.getId());
                FileDownloadHandler fileDownloadHandler6 = FileDownloadHandler.this;
                fileDownloadHandler6.transitionTo(fileDownloadHandler6.mDownloadingPartsState);
            }
            if (z) {
                String r6 = FileDownloadHandler.this.TAG;
                Log.d(r6, "RetrievingHeadState, Handled : " + InitEvent);
            }
            return z;
        }
    }

    private class DownloadingPartsState extends State {
        private DownloadingPartsState() {
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = FileDownloadHandler.this.InitEvent(message);
            String r1 = FileDownloadHandler.this.TAG;
            Log.i(r1, "DownloadingPartsState processMessage " + message.what);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            boolean z = true;
            if (i == 7) {
                FileDownloadHandler fileDownloadHandler = FileDownloadHandler.this;
                if (fileDownloadHandler.mBufferDBTranslation.isMessageStatusCancelled(fileDownloadHandler.mParam.mRowId)) {
                    FileDownloadHandler.this.sendMessage(OMASyncEventType.DOWNLOAD_FILE_API_FAILED.getId());
                } else {
                    FileDownloadHandler fileDownloadHandler2 = FileDownloadHandler.this;
                    int i2 = fileDownloadHandler2.mPartNum;
                    if (i2 < fileDownloadHandler2.mTotalParts) {
                        fileDownloadHandler2.mPartNum = i2 + 1;
                        StringBuilder sb = new StringBuilder();
                        sb.append(FileDownloadHandler.this.mLargeFileDownloadParams.acceptRanges);
                        sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                        FileDownloadHandler fileDownloadHandler3 = FileDownloadHandler.this;
                        sb.append((fileDownloadHandler3.mPartNum - 1) * fileDownloadHandler3.mMaxRange);
                        sb.append(CmcConstants.E_NUM_SLOT_SPLIT);
                        FileDownloadHandler fileDownloadHandler4 = FileDownloadHandler.this;
                        sb.append((fileDownloadHandler4.mMaxRange * fileDownloadHandler4.mPartNum) - 1);
                        String sb2 = sb.toString();
                        String r11 = FileDownloadHandler.this.TAG;
                        Log.i(r11, "rangeHeader: " + sb2 + ", partNum: " + FileDownloadHandler.this.mPartNum);
                        CmsHttpController httpController = FileDownloadHandler.this.mStoreClient.getHttpController();
                        FileDownloadHandler fileDownloadHandler5 = FileDownloadHandler.this;
                        httpController.execute(new CloudMessageGetLargeFile(fileDownloadHandler5.mCallFlowListener, fileDownloadHandler5.mStoreClient, sb2, fileDownloadHandler5.mAccessURL, fileDownloadHandler5.mLargeFileDownloadParams.contentType));
                    } else {
                        Log.i(fileDownloadHandler2.TAG, "All parts downloaded:");
                        FileDownloadHandler.this.sendMessage(OMASyncEventType.DOWNLOAD_FILE_PART_COMPLETED.getId());
                        FileDownloadHandler fileDownloadHandler6 = FileDownloadHandler.this;
                        fileDownloadHandler6.transitionTo(fileDownloadHandler6.mDownloadCompletedState);
                    }
                }
            } else if (i != 8) {
                z = false;
            } else {
                FileDownloadHandler.this.handleLargeFileDownloadSuccess(((LargeFileDownloadParams) message.obj).strbody);
                FileDownloadHandler.this.sendMessage(OMASyncEventType.DOWNLOAD_FILE_PART.getId());
            }
            if (z) {
                String r10 = FileDownloadHandler.this.TAG;
                Log.d(r10, "DownloadingPartsState, Handled : " + InitEvent);
            }
            return z;
        }
    }

    private class DownloadCompletedState extends State {
        private DownloadCompletedState() {
        }

        public boolean processMessage(Message message) {
            boolean z;
            OMASyncEventType InitEvent = FileDownloadHandler.this.InitEvent(message);
            String r1 = FileDownloadHandler.this.TAG;
            Log.i(r1, "DownloadCompletedState processMessage " + message.what);
            if (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()] != 9) {
                z = false;
            } else {
                if (!FileDownloadHandler.this.sendPayloadDownloaded()) {
                    FileDownloadHandler.this.sendMessage(OMASyncEventType.DOWNLOAD_FILE_API_FAILED.getId());
                }
                FileDownloadHandler.this.resetAllParams();
                FileDownloadHandler fileDownloadHandler = FileDownloadHandler.this;
                fileDownloadHandler.transitionTo(fileDownloadHandler.mDefaultState);
                z = true;
            }
            if (z) {
                String r4 = FileDownloadHandler.this.TAG;
                Log.d(r4, "DownloadCompletedState, Handled : " + InitEvent);
            }
            return z;
        }
    }

    /* access modifiers changed from: private */
    public void handleLargeFileDownloadSuccess(byte[] bArr) {
        FileOutputStream fileOutputStream;
        String str = this.TAG;
        Log.i(str, "handleLargeFileDownloadSuccess: partNum: " + this.mPartNum);
        if (this.mPartNum == 1) {
            File cacheDir = this.mStoreClient.getContext().getCacheDir();
            if (cacheDir == null) {
                Log.e(this.TAG, "handleLargeFileDownloadSuccess Unable to get Cache Dir!");
                return;
            }
            try {
                String generateUniqueFilePath = FilePathGenerator.generateUniqueFilePath(cacheDir.getAbsolutePath(), this.mFileName, 128);
                this.mLocalFilePath = generateUniqueFilePath;
                if (generateUniqueFilePath == null) {
                    Log.e(this.TAG, "handleLargeFileDownloadSuccess Create internal path failed!!!");
                    return;
                }
            } catch (NullPointerException | SecurityException e) {
                e.printStackTrace();
            }
        }
        try {
            if (!TextUtils.isEmpty(this.mLocalFilePath)) {
                Log.i(this.TAG, "handleLargeFileDownloadSuccess localFilePath isEmpty false");
            }
            fileOutputStream = new FileOutputStream(this.mLocalFilePath, true);
            fileOutputStream.write(bArr);
            fileOutputStream.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (Throwable th) {
            fileOutputStream.close();
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public boolean sendPayloadDownloaded() {
        Log.i(this.TAG, "sendPayloadDownloaded");
        if (!TextUtils.isEmpty(this.mLocalFilePath)) {
            ParamOMAresponseforBufDB.Builder bufferDBChangeParam = new ParamOMAresponseforBufDB.Builder().setPayloadUrl(this.mAccessURL).setFilePath(this.mLocalFilePath).setBufferDBChangeParam(this.mParam);
            bufferDBChangeParam.setActionType(ParamOMAresponseforBufDB.ActionType.ONE_PAYLOAD_DOWNLOAD);
            ParamOMAresponseforBufDB build = bufferDBChangeParam.build();
            IAPICallFlowListener iAPICallFlowListener = this.mSyncHandlerCallFlowListener;
            if (iAPICallFlowListener instanceof BaseSyncHandler) {
                iAPICallFlowListener.onSuccessfulEvent((IHttpAPICommonInterface) null, OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.getId(), build);
                return true;
            }
            iAPICallFlowListener.onSuccessfulEvent((IHttpAPICommonInterface) null, OMASyncEventType.DOWNLOAD_RETRIVED.getId(), build);
            return true;
        }
        Log.i(this.TAG, "sendPayloadDownloaded localFilePath empty case");
        return false;
    }

    /* access modifiers changed from: package-private */
    public OMASyncEventType InitEvent(Message message) {
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        return valueOf == null ? OMASyncEventType.DEFAULT : valueOf;
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
        this.mSyncHandlerCallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        Log.i(this.TAG, "onFailedCall");
        this.mSyncHandlerCallFlowListener.onFailedCall(iHttpAPICommonInterface);
        sendMessage(OMASyncEventType.RESET_STATE.getId());
    }

    public void onGoToEvent(int i, Object obj) {
        String str = this.TAG;
        Log.i(str, "onGoToEvent: " + i);
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendMessage(i);
        }
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        sendMessageDelayed(obtainMessage(OMASyncEventType.API_FAILED.getId(), (Object) iHttpAPICommonInterface), (long) i);
    }
}
