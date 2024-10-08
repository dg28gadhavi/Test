package com.sec.internal.ims.cmstore.omanetapi.synchandler;

import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateFile;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePostLargeFileComplete;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePostLargeFilePart;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.ims.cmstore.utils.CmsHttpController;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.FileUploadResponse;
import com.sec.internal.omanetapi.file.FileData;
import com.sec.internal.omanetapi.file.UploadPartInfo;
import com.sec.internal.omanetapi.file.UploadPartInfos;
import com.sec.internal.omanetapi.nms.data.Object;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler extends StateMachine implements IControllerCommonInterface, IAPICallFlowListener {
    /* access modifiers changed from: private */
    public String TAG = FileHandler.class.getSimpleName();
    File file = null;
    BufferDBTranslation mBufferDBTranslation;
    IAPICallFlowListener mCallFlowListener;
    private String mContentDisposition;
    String mContentType;
    State mDefaultState = new DefaultState();
    String mFileName;
    List<FileUploadResponse> mFileUploadData = new ArrayList(2);
    boolean mIsRunning = false;
    String mLocalFilePath;
    int mPartSize;
    State mRetrievingKeyState = new RetrievingKeyState();
    MessageStoreClient mStoreClient;
    IAPICallFlowListener mSyncHandlerCallFlowListener;
    int mTotalLength;
    int mTotalParts;
    State mUploadCompleteState = new UploadCompleteState();
    String mUploadKeyId;
    List<UploadPartInfo> mUploadPartInfoList;
    UploadPartInfos mUploadPartInfos;
    State mUploadingPartsState = new UploadingPartsState();
    State mUploadingSmallFileState = new UploadingSmallFileState();
    BufferDBChangeParam param;

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

    public FileHandler(IAPICallFlowListener iAPICallFlowListener, Looper looper, BufferDBTranslation bufferDBTranslation, MessageStoreClient messageStoreClient) {
        super("FileHandler", looper);
        String str = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str;
        Log.i(str, " File Handler Constructor");
        this.mStoreClient = messageStoreClient;
        this.mCallFlowListener = this;
        this.mBufferDBTranslation = bufferDBTranslation;
        this.mSyncHandlerCallFlowListener = iAPICallFlowListener;
        initStates();
    }

    private void initStates() {
        Log.i(this.TAG, " initStates ");
        addState(this.mDefaultState);
        setInitialState(this.mDefaultState);
        addState(this.mUploadingSmallFileState, this.mDefaultState);
        addState(this.mRetrievingKeyState, this.mDefaultState);
        addState(this.mUploadingPartsState, this.mRetrievingKeyState);
        addState(this.mUploadCompleteState, this.mUploadingPartsState);
        super.start();
    }

    public void start() {
        Log.i(this.TAG, " start");
    }

    public void start(BufferDBChangeParam bufferDBChangeParam) {
        this.mIsRunning = true;
        String str = this.TAG;
        Log.i(str, " start param " + bufferDBChangeParam);
        this.param = bufferDBChangeParam;
        this.mUploadPartInfoList = new ArrayList();
        this.mUploadPartInfos = new UploadPartInfos();
        FileData localFileData = this.mBufferDBTranslation.getLocalFileData(bufferDBChangeParam);
        if (localFileData != null) {
            String str2 = localFileData.filePath;
            this.mLocalFilePath = str2;
            if (!TextUtils.isEmpty(str2)) {
                this.file = new File(this.mLocalFilePath);
            }
            File file2 = this.file;
            if (file2 != null && file2.exists()) {
                this.mTotalLength = (int) this.file.length();
                this.mContentType = localFileData.contentType;
                this.mContentDisposition = localFileData.contentDisposition;
                this.mFileName = localFileData.fileName;
            }
        }
        this.mFileUploadData.add(0, (Object) null);
        this.mFileUploadData.add(1, (Object) null);
        ParamObjectUpload thumbnailPart = this.mBufferDBTranslation.getThumbnailPart(bufferDBChangeParam);
        if (thumbnailPart != null) {
            sendMessage(OMASyncEventType.START_THUMBNAIL_UPLOAD.getId(), (Object) thumbnailPart);
        } else if (TextUtils.isEmpty(this.mLocalFilePath)) {
            Log.i(this.TAG, " invalid File Data, upload failed");
            sendMessage(OMASyncEventType.FILE_API_FAILED.getId());
        } else if (!CmsUtil.isLargeSizeFile(this.mStoreClient, (long) this.mTotalLength)) {
            sendMessage(OMASyncEventType.START_SMALL_FILE_UPLOAD.getId());
        } else {
            Log.i(this.TAG, " no thumbnail exits and file size greater than 5 MB ");
            sendMessage(OMASyncEventType.FILE_API_FAILED.getId());
        }
    }

    /* access modifiers changed from: private */
    public void resetAllParams() {
        Log.i(this.TAG, "reset All Params");
        this.mUploadPartInfoList.clear();
        this.mUploadPartInfos = null;
        this.mUploadKeyId = "";
        this.mContentType = "";
        this.mFileName = "";
        this.mTotalLength = 0;
        this.mTotalParts = 0;
        this.mPartSize = 0;
        this.mLocalFilePath = null;
        this.param = null;
        this.file = null;
        this.mContentDisposition = null;
        this.mFileUploadData.clear();
    }

    /* access modifiers changed from: private */
    public ParamObjectUpload getMcsFtPayLoadData() {
        ArrayList arrayList = new ArrayList();
        Log.i(this.TAG, "getMcsFtPayLoadData localFilePath : " + this.mLocalFilePath);
        File file2 = this.file;
        if (file2 != null && file2.exists()) {
            String str = "form-data; name=\"file\"; filename=\"" + this.mFileName + CmcConstants.E_NUM_STR_QUOTE;
            if (!TextUtils.isEmpty(this.mContentType) && MIMEContentType.FT_HTTP.equals(this.mContentType)) {
                this.mContentType = FileUtils.getContentType(this.file);
            }
            byte[] fileContentInBytes = getFileContentInBytes(this.mLocalFilePath, CloudMessageBufferDBConstants.PayloadEncoding.None);
            if (!(fileContentInBytes == null || fileContentInBytes.length == 0 || TextUtils.isEmpty(this.mContentType))) {
                arrayList.add(new HttpPostBody(str, this.mContentType, fileContentInBytes));
                IMSLog.i(this.TAG, "data len: " + fileContentInBytes.length + " contentType:" + this.mContentType);
            }
        }
        Log.i(this.TAG, "Filepath: " + this.file + " File payload size: " + arrayList.size());
        FileUtils.removeFile(this.mLocalFilePath);
        if (arrayList.size() <= 0) {
            IMSLog.i(this.TAG, "multibody part is empty");
            return null;
        }
        ParamObjectUpload paramObjectUpload = new ParamObjectUpload(new Pair((Object) null, new HttpPostBody((List<HttpPostBody>) arrayList)), this.param);
        IMSLog.i(this.TAG, "full body is added!!!!");
        return paramObjectUpload;
    }

    /* access modifiers changed from: protected */
    public byte[] getFileContentInBytes(String str, CloudMessageBufferDBConstants.PayloadEncoding payloadEncoding) {
        FileInputStream fileInputStream;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                fileInputStream = new FileInputStream(str);
                byte[] bArr = new byte[256];
                int read = fileInputStream.read(bArr);
                while (read >= 0) {
                    byteArrayOutputStream.write(bArr, 0, read);
                    read = fileInputStream.read(bArr);
                }
                String str2 = this.TAG;
                Log.i(str2, "getFileContentInBytes: " + str + " " + payloadEncoding + " bytes " + read + " getRcsFilePayloadFromPath, all bytes: " + byteArrayOutputStream.size());
                if (CloudMessageBufferDBConstants.PayloadEncoding.Base64.equals(payloadEncoding)) {
                    byte[] encode = Base64.encode(byteArrayOutputStream.toByteArray(), 0);
                    fileInputStream.close();
                    byteArrayOutputStream.close();
                    return encode;
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                fileInputStream.close();
                byteArrayOutputStream.close();
                return byteArray;
            } catch (Throwable th) {
                byteArrayOutputStream.close();
                throw th;
            }
            throw th;
        } catch (IOException e) {
            String str3 = this.TAG;
            Log.e(str3, "getFileContentInBytes :: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    /* access modifiers changed from: private */
    public int calculatePartSize(Integer num, Integer num2) {
        return (num.intValue() + num2.intValue()) / 2;
    }

    /* access modifiers changed from: package-private */
    public OMASyncEventType InitEvent(Message message) {
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        return valueOf == null ? OMASyncEventType.DEFAULT : valueOf;
    }

    /* access modifiers changed from: private */
    public HttpPostBody getFilePartPayload(int i) throws IOException {
        FileInputStream fileInputStream;
        int ceil = (int) Math.ceil(((double) this.mTotalLength) / ((double) this.mPartSize));
        this.mTotalParts = ceil;
        if (i < 0 || i >= ceil) {
            String str = this.TAG;
            Log.i(str, " getFilePartPayload failed invalid partNumber " + i);
            return null;
        }
        try {
            fileInputStream = new FileInputStream(this.file);
            long j = ((long) i) * ((long) this.mPartSize);
            long skip = fileInputStream.skip(j);
            if (skip >= 0) {
                if (skip == j) {
                    byte[] bArr = new byte[this.mPartSize];
                    int read = fileInputStream.read(bArr);
                    if (read < this.mPartSize) {
                        bArr = Arrays.copyOf(bArr, read);
                    }
                    if (bArr == null || bArr.length == 0 || TextUtils.isEmpty(this.mContentType)) {
                        fileInputStream.close();
                        String str2 = this.TAG;
                        IMSLog.e(str2, "getFilePartPayload failed invalid partNumber " + i);
                        return null;
                    }
                    String str3 = this.TAG;
                    IMSLog.i(str3, "data len: " + bArr.length + " contentType:" + this.mContentType);
                    HttpPostBody httpPostBody = new HttpPostBody(this.mContentDisposition, this.mContentType, bArr);
                    fileInputStream.close();
                    return httpPostBody;
                }
            }
            String str4 = this.TAG;
            IMSLog.e(str4, "bytes skipped count not correct: count: " + j + ", bytes: " + skip + ", partSize: " + this.mPartSize);
            fileInputStream.close();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
        String str = this.TAG;
        Log.i(str, " onSuccessfulEvent: " + i);
        sendMessage(OMASyncEventType.MOVE_ON.getId(), (Object) new HttpResParamsWrapper(iHttpAPICommonInterface, obj));
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
        this.mSyncHandlerCallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        sendMessage(OMASyncEventType.RESET_STATE.getId());
        this.mSyncHandlerCallFlowListener.onFailedCall(iHttpAPICommonInterface);
    }

    public void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
        sendMessage(OMASyncEventType.MOVE_ON.getId(), (Object) new HttpResParamsWrapper(iHttpAPICommonInterface, obj));
    }

    public void onGoToEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendMessage(i);
        }
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        sendMessageDelayed(obtainMessage(OMASyncEventType.API_FAILED.getId(), (Object) iHttpAPICommonInterface), (long) i);
    }

    public void stop() {
        sendMessage(OMASyncEventType.STOP.getId());
    }

    public class DefaultState extends State {
        public DefaultState() {
        }

        public void enter() {
            Log.d(FileHandler.this.TAG, "DefaultState, enter");
        }

        public boolean processMessage(Message message) {
            FileHandler fileHandler = FileHandler.this;
            boolean z = false;
            if (!fileHandler.mIsRunning) {
                return false;
            }
            OMASyncEventType InitEvent = fileHandler.InitEvent(message);
            Log.i(FileHandler.this.TAG, "Default State processMessage " + message.what);
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()]) {
                case 1:
                    break;
                case 2:
                    CmsHttpController httpController = FileHandler.this.mStoreClient.getHttpController();
                    FileHandler fileHandler2 = FileHandler.this;
                    httpController.execute(new CloudMessageCreateFile(fileHandler2.mCallFlowListener, (ParamObjectUpload) message.obj, true, fileHandler2.mStoreClient));
                    break;
                case 3:
                    FileHandler.this.mFileUploadData.set(0, (FileUploadResponse) message.obj);
                    if (TextUtils.isEmpty(FileHandler.this.mLocalFilePath)) {
                        FileHandler.this.sendMessage(OMASyncEventType.CREATE_ALL_OBJECT.getId());
                        break;
                    } else {
                        FileHandler fileHandler3 = FileHandler.this;
                        if (CmsUtil.isLargeSizeFile(fileHandler3.mStoreClient, (long) fileHandler3.mTotalLength)) {
                            FileHandler.this.sendMessage(OMASyncEventType.CREATE_ALL_OBJECT.getId());
                            break;
                        } else {
                            FileHandler.this.sendMessage(OMASyncEventType.START_SMALL_FILE_UPLOAD.getId());
                            break;
                        }
                    }
                case 4:
                    FileHandler.this.sendMessage(OMASyncEventType.GET_UPLOAD_KEY_ID.getId());
                    FileHandler fileHandler4 = FileHandler.this;
                    fileHandler4.transitionTo(fileHandler4.mRetrievingKeyState);
                    break;
                case 5:
                    Object obj = message.obj;
                    if (obj != null) {
                        HttpResParamsWrapper httpResParamsWrapper = (HttpResParamsWrapper) obj;
                        if (httpResParamsWrapper.mApi instanceof CloudMessageCreateAllObjects) {
                            FileHandler.this.sendMessage(OMASyncEventType.RESET_STATE.getId());
                            FileHandler.this.mSyncHandlerCallFlowListener.onGoToEvent(OMASyncEventType.OBJECT_ONE_UPLOAD_COMPLETED.getId(), httpResParamsWrapper.mBufDbParams);
                            break;
                        }
                    }
                    break;
                case 6:
                    ParamObjectUpload r7 = FileHandler.this.getMcsFtPayLoadData();
                    if (r7 == null) {
                        FileHandler.this.sendMessage(OMASyncEventType.FILE_API_FAILED.getId());
                    } else {
                        FileHandler.this.sendMessage(OMASyncEventType.UPLOADING_FILE.getId(), (Object) r7);
                    }
                    FileHandler fileHandler5 = FileHandler.this;
                    fileHandler5.transitionTo(fileHandler5.mUploadingSmallFileState);
                    break;
                case 7:
                    FileHandler fileHandler6 = FileHandler.this;
                    Pair<Object, HttpPostBody> rCSObjectPairFromCursor = fileHandler6.mBufferDBTranslation.getRCSObjectPairFromCursor(fileHandler6.param, fileHandler6.mFileUploadData);
                    if (rCSObjectPairFromCursor != null && rCSObjectPairFromCursor.first != null) {
                        IMSLog.i(FileHandler.this.TAG, " CREATE_ALL_OBJECT CloudMessageCreateAllObjects " + rCSObjectPairFromCursor.first);
                        ParamObjectUpload paramObjectUpload = new ParamObjectUpload(rCSObjectPairFromCursor, FileHandler.this.param);
                        CmsHttpController httpController2 = FileHandler.this.mStoreClient.getHttpController();
                        FileHandler fileHandler7 = FileHandler.this;
                        httpController2.execute(new CloudMessageCreateAllObjects(fileHandler7.mCallFlowListener, paramObjectUpload, fileHandler7.mStoreClient));
                        break;
                    } else {
                        IMSLog.e(FileHandler.this.TAG, "invalid parameters, request not processed");
                        break;
                    }
                case 8:
                    FileHandler.this.resetAllParams();
                    FileHandler fileHandler8 = FileHandler.this;
                    fileHandler8.transitionTo(fileHandler8.mDefaultState);
                    break;
                case 9:
                    FileHandler.this.resetAllParams();
                    FileHandler.this.mSyncHandlerCallFlowListener.onGoToEvent(OMASyncEventType.OBJECT_FT_UPLOAD_FAILED.getId(), (Object) null);
                    FileHandler fileHandler9 = FileHandler.this;
                    fileHandler9.transitionTo(fileHandler9.mDefaultState);
                    break;
                case 10:
                    IHttpAPICommonInterface iHttpAPICommonInterface = (IHttpAPICommonInterface) message.obj;
                    if (iHttpAPICommonInterface != null) {
                        FileHandler.this.mStoreClient.getMcsRetryMapAdapter().retryApi(iHttpAPICommonInterface, FileHandler.this);
                        break;
                    }
                    break;
                case 11:
                    FileHandler fileHandler10 = FileHandler.this;
                    fileHandler10.mIsRunning = false;
                    fileHandler10.transitionTo(fileHandler10.mDefaultState);
                    break;
            }
            z = true;
            if (z) {
                Log.d(FileHandler.this.TAG, "DefaultState, Handled : " + InitEvent);
            }
            return z;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler$1  reason: invalid class name */
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.START     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.START_THUMBNAIL_UPLOAD     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.THUMBNAIL_UPLOADED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.START_LARGE_FILE_UPLOAD     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MOVE_ON     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.START_SMALL_FILE_UPLOAD     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_ALL_OBJECT     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESET_STATE     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.FILE_API_FAILED     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_FAILED     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.STOP     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPLOADING_FILE     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.FILE_UPLOADED     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.GET_UPLOAD_KEY_ID     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPLOAD_KEY_ID_RECEIVED     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPLOAD_FILE_PART     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.PART_UPLOADED     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPLOAD_FILE_COMPLETE     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.LARGE_FILE_UPLOAD_COMPLETED     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.AnonymousClass1.<clinit>():void");
        }
    }

    private class UploadingSmallFileState extends State {
        private UploadingSmallFileState() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:8:0x0070  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(android.os.Message r8) {
            /*
                r7 = this;
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r0 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = r0.InitEvent(r8)
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r1 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                java.lang.String r1 = r1.TAG
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "UploadingSmallFileState processMessage "
                r2.append(r3)
                int r3 = r8.what
                r2.append(r3)
                java.lang.String r2 = r2.toString()
                android.util.Log.i(r1, r2)
                int[] r1 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType
                int r2 = r0.ordinal()
                r1 = r1[r2]
                r2 = 12
                r3 = 0
                r4 = 1
                if (r1 == r2) goto L_0x0053
                r2 = 13
                if (r1 == r2) goto L_0x0035
                goto L_0x006e
            L_0x0035:
                java.lang.Object r8 = r8.obj
                com.sec.internal.omanetapi.common.data.FileUploadResponse r8 = (com.sec.internal.omanetapi.common.data.FileUploadResponse) r8
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r1 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                java.util.List<com.sec.internal.omanetapi.common.data.FileUploadResponse> r1 = r1.mFileUploadData
                r1.set(r4, r8)
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r8 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_ALL_OBJECT
                int r1 = r1.getId()
                r8.sendMessage((int) r1)
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r8 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.helper.State r1 = r8.mDefaultState
                r8.transitionTo(r1)
                goto L_0x006d
            L_0x0053:
                java.lang.Object r8 = r8.obj
                com.sec.internal.ims.cmstore.params.ParamObjectUpload r8 = (com.sec.internal.ims.cmstore.params.ParamObjectUpload) r8
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r1 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.ims.cmstore.MessageStoreClient r1 = r1.mStoreClient
                com.sec.internal.ims.cmstore.utils.CmsHttpController r1 = r1.getHttpController()
                com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateFile r2 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateFile
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r5 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r6 = r5.mCallFlowListener
                com.sec.internal.ims.cmstore.MessageStoreClient r5 = r5.mStoreClient
                r2.<init>(r6, r8, r3, r5)
                r1.execute(r2)
            L_0x006d:
                r3 = r4
            L_0x006e:
                if (r3 == 0) goto L_0x008a
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r7 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                java.lang.String r7 = r7.TAG
                java.lang.StringBuilder r8 = new java.lang.StringBuilder
                r8.<init>()
                java.lang.String r1 = "UploadingSmallFileState, Handled : "
                r8.append(r1)
                r8.append(r0)
                java.lang.String r8 = r8.toString()
                android.util.Log.d(r7, r8)
            L_0x008a:
                return r3
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.UploadingSmallFileState.processMessage(android.os.Message):boolean");
        }
    }

    private class RetrievingKeyState extends State {
        private RetrievingKeyState() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:18:0x00ba  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(android.os.Message r12) {
            /*
                r11 = this;
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r0 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = r0.InitEvent(r12)
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r1 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                java.lang.String r1 = r1.TAG
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "RetrievingKeyState processMessage "
                r2.append(r3)
                int r3 = r12.what
                r2.append(r3)
                java.lang.String r2 = r2.toString()
                android.util.Log.i(r1, r2)
                int[] r1 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType
                int r2 = r0.ordinal()
                r1 = r1[r2]
                r2 = 14
                r3 = 1
                if (r1 == r2) goto L_0x0095
                r2 = 15
                r4 = 0
                if (r1 == r2) goto L_0x0037
                r3 = r4
                goto L_0x00b8
            L_0x0037:
                java.lang.Object r12 = r12.obj
                com.sec.internal.omanetapi.common.data.LargeFileResponse r12 = (com.sec.internal.omanetapi.common.data.LargeFileResponse) r12
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r1 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                java.lang.String r2 = r12.uploadKeyId
                r1.mUploadKeyId = r2
                int r2 = r12.partSizeMin
                int r12 = r12.partSizeMax
                java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
                java.lang.Integer r12 = java.lang.Integer.valueOf(r12)
                int r12 = r1.calculatePartSize(r2, r12)
                r1.mPartSize = r12
                r12 = 0
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r1 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this     // Catch:{ IOException -> 0x0077 }
                com.sec.internal.helper.httpclient.HttpPostBody r1 = r1.getFilePartPayload(r4)     // Catch:{ IOException -> 0x0077 }
                if (r1 != 0) goto L_0x007e
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r2 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this     // Catch:{ IOException -> 0x0075 }
                java.lang.String r2 = r2.TAG     // Catch:{ IOException -> 0x0075 }
                java.lang.String r4 = " unable to get payload upload failed "
                android.util.Log.i(r2, r4)     // Catch:{ IOException -> 0x0075 }
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r2 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this     // Catch:{ IOException -> 0x0075 }
                com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r2 = r2.mSyncHandlerCallFlowListener     // Catch:{ IOException -> 0x0075 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.OBJECT_FT_UPLOAD_FAILED     // Catch:{ IOException -> 0x0075 }
                int r4 = r4.getId()     // Catch:{ IOException -> 0x0075 }
                r2.onGoToEvent(r4, r12)     // Catch:{ IOException -> 0x0075 }
                goto L_0x007e
            L_0x0075:
                r12 = move-exception
                goto L_0x007b
            L_0x0077:
                r1 = move-exception
                r10 = r1
                r1 = r12
                r12 = r10
            L_0x007b:
                r12.printStackTrace()
            L_0x007e:
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r12 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r2 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPLOAD_FILE_PART
                int r2 = r2.getId()
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r4 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                int r4 = r4.mPartSize
                r12.sendMessage(r2, r3, r4, r1)
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r12 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.helper.State r1 = r12.mUploadingPartsState
                r12.transitionTo(r1)
                goto L_0x00b8
            L_0x0095:
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r12 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.ims.cmstore.MessageStoreClient r12 = r12.mStoreClient
                com.sec.internal.ims.cmstore.utils.CmsHttpController r12 = r12.getHttpController()
                com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePostLargeFile r1 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePostLargeFile
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r2 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r5 = r2.mCallFlowListener
                java.lang.String r6 = r2.mContentType
                java.lang.String r7 = r2.mFileName
                int r2 = r2.mTotalLength
                java.lang.Integer r8 = java.lang.Integer.valueOf(r2)
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r2 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                com.sec.internal.ims.cmstore.MessageStoreClient r9 = r2.mStoreClient
                r4 = r1
                r4.<init>(r5, r6, r7, r8, r9)
                r12.execute(r1)
            L_0x00b8:
                if (r3 == 0) goto L_0x00d4
                com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler r11 = com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.this
                java.lang.String r11 = r11.TAG
                java.lang.StringBuilder r12 = new java.lang.StringBuilder
                r12.<init>()
                java.lang.String r1 = "RetrievingKeyState, Handled : "
                r12.append(r1)
                r12.append(r0)
                java.lang.String r12 = r12.toString()
                android.util.Log.d(r11, r12)
            L_0x00d4:
                return r3
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.synchandler.FileHandler.RetrievingKeyState.processMessage(android.os.Message):boolean");
        }
    }

    private class UploadingPartsState extends State {
        private UploadingPartsState() {
        }

        public boolean processMessage(Message message) {
            HttpPostBody httpPostBody;
            OMASyncEventType InitEvent = FileHandler.this.InitEvent(message);
            String r1 = FileHandler.this.TAG;
            Log.i(r1, " processMessage " + message.what);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            boolean z = true;
            if (i == 16) {
                CmsHttpController httpController = FileHandler.this.mStoreClient.getHttpController();
                FileHandler fileHandler = FileHandler.this;
                httpController.execute(new CloudMessagePostLargeFilePart(fileHandler.mCallFlowListener, fileHandler.mUploadKeyId, String.valueOf(message.arg1), (HttpPostBody) message.obj, fileHandler.mStoreClient));
            } else if (i != 17) {
                z = false;
            } else {
                UploadPartInfo uploadPartInfo = (UploadPartInfo) message.obj;
                if (uploadPartInfo == null) {
                    FileHandler.this.sendMessage(OMASyncEventType.UPLOAD_FILE_COMPLETE.getId());
                    FileHandler fileHandler2 = FileHandler.this;
                    fileHandler2.transitionTo(fileHandler2.mUploadCompleteState);
                } else {
                    FileHandler.this.mUploadPartInfoList.add(uploadPartInfo);
                    int i2 = uploadPartInfo.partNum + 1;
                    String r2 = FileHandler.this.TAG;
                    Log.i(r2, " part uploaded for partNum " + uploadPartInfo.partNum + "partTag " + uploadPartInfo.partTag);
                    FileHandler fileHandler3 = FileHandler.this;
                    if (i2 <= fileHandler3.mTotalParts) {
                        try {
                            httpPostBody = fileHandler3.getFilePartPayload(i2 - 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            httpPostBody = null;
                        }
                        FileHandler.this.sendMessage(OMASyncEventType.UPLOAD_FILE_PART.getId(), i2, FileHandler.this.mPartSize, httpPostBody);
                    } else {
                        fileHandler3.sendMessage(OMASyncEventType.UPLOAD_FILE_COMPLETE.getId());
                        FileHandler fileHandler4 = FileHandler.this;
                        fileHandler4.transitionTo(fileHandler4.mUploadCompleteState);
                    }
                }
            }
            if (z) {
                String r10 = FileHandler.this.TAG;
                Log.d(r10, "UploadingPartsState, Handled : " + InitEvent);
            }
            return z;
        }
    }

    private class UploadCompleteState extends State {
        private UploadCompleteState() {
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = FileHandler.this.InitEvent(message);
            String r1 = FileHandler.this.TAG;
            Log.i(r1, " processMessage " + message.what);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            boolean z = true;
            if (i == 18) {
                FileHandler fileHandler = FileHandler.this;
                UploadPartInfos uploadPartInfos = fileHandler.mUploadPartInfos;
                if (uploadPartInfos == null) {
                    Log.i(fileHandler.TAG, " upload failed ");
                    FileHandler.this.mSyncHandlerCallFlowListener.onGoToEvent(OMASyncEventType.OBJECT_FT_UPLOAD_FAILED.getId(), (Object) null);
                } else {
                    uploadPartInfos.uploadPartInfoArray = new UploadPartInfo[fileHandler.mUploadPartInfoList.size()];
                    FileHandler fileHandler2 = FileHandler.this;
                    UploadPartInfos uploadPartInfos2 = fileHandler2.mUploadPartInfos;
                    uploadPartInfos2.uploadPartInfoArray = (UploadPartInfo[]) fileHandler2.mUploadPartInfoList.toArray(uploadPartInfos2.uploadPartInfoArray);
                    CmsHttpController httpController = FileHandler.this.mStoreClient.getHttpController();
                    FileHandler fileHandler3 = FileHandler.this;
                    httpController.execute(new CloudMessagePostLargeFileComplete(fileHandler3.mCallFlowListener, fileHandler3.mUploadKeyId, fileHandler3.mUploadPartInfos, fileHandler3.mStoreClient));
                }
            } else if (i != 19) {
                z = false;
            } else {
                try {
                    URL url = new URL((String) message.obj);
                    FileUploadResponse fileUploadResponse = new FileUploadResponse();
                    FileHandler fileHandler4 = FileHandler.this;
                    fileUploadResponse.contentType = fileHandler4.mContentType;
                    fileUploadResponse.fileName = fileHandler4.mFileName;
                    fileUploadResponse.size = fileHandler4.mTotalLength;
                    fileUploadResponse.href = url;
                    fileHandler4.mFileUploadData.add(1, fileUploadResponse);
                    FileHandler.this.sendMessage(OMASyncEventType.CREATE_ALL_OBJECT.getId());
                    FileUtils.removeFile(FileHandler.this.mLocalFilePath);
                    FileHandler fileHandler5 = FileHandler.this;
                    fileHandler5.transitionTo(fileHandler5.mDefaultState);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            if (z) {
                String r7 = FileHandler.this.TAG;
                Log.d(r7, "UploadCompleteState, Handled : " + InitEvent);
            }
            return z;
        }
    }
}
