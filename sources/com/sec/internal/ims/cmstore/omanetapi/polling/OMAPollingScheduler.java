package com.sec.internal.ims.cmstore.omanetapi.polling;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLargeDataPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLongPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateNotificationChannels;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetIndividualNotificationChannelInfo;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateSubscriptionChannel;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualSubscription;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageUpdateSubscriptionChannel;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.ReSyncParam;
import com.sec.internal.ims.cmstore.utils.SchedulerHelper;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.ChannelDeleteData;
import java.util.ArrayList;
import java.util.Iterator;

public class OMAPollingScheduler extends Handler implements IControllerCommonInterface, IAPICallFlowListener {
    public static final long POLLING_TIME_OUT = 360000;
    private final int NO_RETRY_AFTER_VALUE = -1;
    public String TAG = OMAPollingScheduler.class.getSimpleName();
    private INetAPIEventListener mINetAPIEventListener = null;
    private boolean mIsCreateSubscriptionRunning = false;
    private boolean mIsOnePollingRunning = false;
    private boolean mIsPollingNonStopRunning = false;
    private boolean mIsPollingStarted = false;
    private boolean mIsSchedulerRunning = false;
    private final String mLine;
    private ArrayList<OMANetAPIHandler.OnApiSucceedOnceListener> mOnApiSucceedOnceListenerList = new ArrayList<>();
    private final ReSyncParam mReSyncParam = ReSyncParam.getInstance();
    private SchedulerHelper mSchedulerHelper = null;
    private MessageStoreClient mStoreClient;
    private final IUIEventCallback mUIInterface;

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam, SyncMsgType syncMsgType, int i) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
    }

    public void onFixedFlow(int i) {
    }

    public void onFixedFlowWithMessage(Message message) {
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public boolean updateDelay(int i, long j) {
        return true;
    }

    public boolean updateDelayRetry(int i, long j) {
        return false;
    }

    public OMAPollingScheduler(Looper looper, INetAPIEventListener iNetAPIEventListener, IUIEventCallback iUIEventCallback, MessageStoreClient messageStoreClient) {
        super(looper);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mStoreClient = messageStoreClient;
        this.mLine = messageStoreClient.getPrerenceManager().getUserTelCtn();
        this.mINetAPIEventListener = iNetAPIEventListener;
        this.mUIInterface = iUIEventCallback;
        ReSyncParam.update(messageStoreClient);
    }

    public void handleMessage(Message message) {
        String str;
        Object obj;
        if (this.mSchedulerHelper == null) {
            this.mSchedulerHelper = SchedulerHelper.getInstance(this);
        }
        super.handleMessage(message);
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        String str2 = this.TAG;
        Log.i(str2, "message: " + valueOf);
        logWorkingStatus();
        if (valueOf == null) {
            valueOf = OMASyncEventType.DEFAULT;
        }
        boolean z = false;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[valueOf.ordinal()]) {
            case 1:
                this.mIsPollingStarted = true;
                if (shouldSendPollRequest()) {
                    sendEmptyMessage(OMASyncEventType.CHECK_NOTIFICATION_CHANNEL.getId());
                    return;
                }
                return;
            case 2:
                if (!this.mIsSchedulerRunning) {
                    this.mIsSchedulerRunning = true;
                    if (shouldSendPollRequest()) {
                        sendEmptyMessage(OMASyncEventType.CHECK_NOTIFICATION_CHANNEL.getId());
                        return;
                    } else {
                        this.mIsPollingNonStopRunning = true;
                        return;
                    }
                } else {
                    Log.i(this.TAG, "already running");
                    return;
                }
            case 3:
                this.mIsSchedulerRunning = false;
                this.mIsPollingStarted = false;
                return;
            case 4:
                this.mIsSchedulerRunning = false;
                this.mIsPollingNonStopRunning = false;
                return;
            case 5:
                String oMAChannelResURL = this.mStoreClient.getPrerenceManager().getOMAChannelResURL();
                String str3 = this.TAG;
                Log.i(str3, "resUrl: " + oMAChannelResURL);
                if (!TextUtils.isEmpty(oMAChannelResURL)) {
                    this.mStoreClient.getHttpController().execute(new CloudMessageGetIndividualNotificationChannelInfo(this, this, oMAChannelResURL.substring(oMAChannelResURL.lastIndexOf(47) + 1), this.mStoreClient));
                    return;
                } else {
                    sendEmptyMessage(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId());
                    return;
                }
            case 6:
                this.mStoreClient.getHttpController().execute(new CloudMessageCreateNotificationChannels(this, this, true, this.mStoreClient));
                return;
            case 7:
                if (shouldSendPollRequest()) {
                    checkAndUpdateSubscriptionChannel();
                    return;
                }
                return;
            case 8:
                if (!this.mIsCreateSubscriptionRunning) {
                    ReSyncParam.update(this.mStoreClient);
                    String str4 = this.TAG;
                    Log.i(str4, "getNotifyURL: " + this.mReSyncParam.getNotifyURL());
                    if (!TextUtils.isEmpty(this.mReSyncParam.getNotifyURL())) {
                        this.mIsCreateSubscriptionRunning = true;
                        this.mStoreClient.getHttpController().execute(new CloudMessageCreateSubscriptionChannel(this, this.mReSyncParam.getNotifyURL(), this.mReSyncParam.getRestartToken(), this, false, this.mStoreClient));
                        return;
                    }
                    return;
                }
                return;
            case 9:
                String str5 = this.TAG;
                Log.i(str5, "SEND_LONG_POLLING_REQUEST mIsSchedulerRunning: " + this.mIsSchedulerRunning);
                if (!this.mIsSchedulerRunning) {
                    this.mIsOnePollingRunning = false;
                    this.mIsPollingNonStopRunning = false;
                    this.mIsCreateSubscriptionRunning = false;
                    return;
                } else if (!this.mIsOnePollingRunning) {
                    this.mIsOnePollingRunning = true;
                    this.mIsPollingNonStopRunning = true;
                    CloudMessageCreateLongPolling cloudMessageCreateLongPolling = new CloudMessageCreateLongPolling(this, this.mStoreClient.getPrerenceManager().getOMAChannelURL(), this.mStoreClient);
                    cloudMessageCreateLongPolling.setReadTimeout(360000);
                    this.mStoreClient.getHttpController().execute(cloudMessageCreateLongPolling);
                    return;
                } else {
                    return;
                }
            case 10:
                this.mStoreClient.getHttpController().execute(new CloudMessageDeleteIndividualSubscription(this, (String) message.obj, this.mStoreClient));
                this.mStoreClient.getPrerenceManager().saveOMASubscriptionResUrl("");
                return;
            case 11:
                Object obj2 = message.obj;
                if (obj2 instanceof ChannelDeleteData) {
                    ChannelDeleteData channelDeleteData = (ChannelDeleteData) obj2;
                    str = channelDeleteData.channelUrl;
                    z = channelDeleteData.isNeedRecreateChannel;
                } else {
                    str = (String) obj2;
                }
                boolean z2 = z;
                if (!TextUtils.isEmpty(str)) {
                    this.mStoreClient.getHttpController().execute(new CloudMessageDeleteIndividualChannel(this, this, str.substring(str.lastIndexOf("/") + 1), z2, this.mStoreClient));
                    this.mStoreClient.getPrerenceManager().saveOMAChannelResURL("");
                    this.mStoreClient.getPrerenceManager().saveOMACallBackURL("");
                    this.mStoreClient.getPrerenceManager().saveOMAChannelURL("");
                    return;
                }
                return;
            case 12:
                ReSyncParam.update(this.mStoreClient);
                if (!TextUtils.isEmpty(this.mStoreClient.getPrerenceManager().getOMASubscriptionResUrl())) {
                    this.mStoreClient.getHttpController().execute(new CloudMessageUpdateSubscriptionChannel(this, this.mStoreClient.getPrerenceManager().getOMASSubscriptionRestartToken(), this.mStoreClient.getPrerenceManager().getOMASubscriptionResUrl(), this, this.mStoreClient));
                    return;
                }
                return;
            case 13:
                this.mINetAPIEventListener.onCloudSyncStopped(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MAILBOX_RESET).build());
                return;
            case 14:
                this.mINetAPIEventListener.onCloudObjectNotificationUpdated((ParamOMAresponseforBufDB) message.obj);
                return;
            case 15:
                String str6 = this.TAG;
                Log.i(str6, "MSTORE_REDIRECT mIsSchedulerRunning: " + this.mIsSchedulerRunning);
                if (this.mIsSchedulerRunning && (obj = message.obj) != null) {
                    HttpRequestParams httpRequestParams = (HttpRequestParams) obj;
                    String str7 = this.TAG;
                    Log.i(str7, "ReExecute API " + httpRequestParams.getClass().getSimpleName() + " after 302 by using new url");
                    this.mStoreClient.getHttpController().execute(httpRequestParams);
                    return;
                }
                return;
            case 16:
                this.mIsOnePollingRunning = false;
                this.mIsSchedulerRunning = false;
                this.mIsPollingNonStopRunning = false;
                this.mIsCreateSubscriptionRunning = false;
                ParamOMAresponseforBufDB build = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine).build();
                Object obj3 = message.obj;
                this.mINetAPIEventListener.onOmaAuthenticationFailed(build, (obj3 == null || !(obj3 instanceof Number)) ? 0 : ((Number) obj3).longValue());
                return;
            case 17:
                this.mINetAPIEventListener.onPauseCMNNetApiWithResumeDelay(((Integer) message.obj).intValue());
                return;
            case 18:
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                return;
            case 19:
                this.mIsOnePollingRunning = false;
                return;
            case 20:
                this.mIsCreateSubscriptionRunning = false;
                return;
            case 21:
                Object obj4 = message.obj;
                if (obj4 != null) {
                    onApiTreatAsSucceed((IHttpAPICommonInterface) obj4);
                    return;
                }
                return;
            case 22:
                Object obj5 = message.obj;
                if (obj5 != null) {
                    HttpResParamsWrapper httpResParamsWrapper = (HttpResParamsWrapper) obj5;
                    onApiTreatAsSucceed(httpResParamsWrapper.mApi);
                    gotoHandlerEvent(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId(), httpResParamsWrapper.mBufDbParams);
                    return;
                }
                return;
            case 23:
                pause();
                this.mSchedulerHelper.deleteNotificationSubscriptionResource(this.mStoreClient);
                return;
            default:
                return;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.polling.OMAPollingScheduler$1  reason: invalid class name */
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.START     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESUME     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.STOP     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.PAUSE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_NOTIFICATION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_NOTIFICATION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_SUBSCRIPTION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SEND_LONG_POLLING_REQUEST     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_SUBCRIPTION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_NOTIFICATION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MAILBOX_RESET     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CLOUD_UPDATE     // Catch:{ NoSuchFieldError -> 0x00a8 }
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREDENTIAL_EXPIRED     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SELF_RETRY     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SYNC_ERR     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ONE_POLLING_FINISHED     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00f0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_SUBSCRIPTION_FINISHED     // Catch:{ NoSuchFieldError -> 0x00f0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f0 }
                r2 = 20
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00f0 }
            L_0x00f0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00fc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_SUCCEED     // Catch:{ NoSuchFieldError -> 0x00fc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fc }
                r2 = 21
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00fc }
            L_0x00fc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0108 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MOVE_ON     // Catch:{ NoSuchFieldError -> 0x0108 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0108 }
                r2 = 22
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0108 }
            L_0x0108:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0114 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_NOTIFICATION_SUBSCRIPTION_RESOURCE     // Catch:{ NoSuchFieldError -> 0x0114 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0114 }
                r2 = 23
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0114 }
            L_0x0114:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.polling.OMAPollingScheduler.AnonymousClass1.<clinit>():void");
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
        boolean isRetryEnabled = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled();
        String str = this.TAG;
        Log.i(str, "gotoHandlerEventOnFailure isRetryEnabled: " + isRetryEnabled);
        if (isRetryEnabled) {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, -1);
            this.mIsOnePollingRunning = false;
            this.mIsSchedulerRunning = false;
            this.mIsPollingNonStopRunning = false;
            this.mIsCreateSubscriptionRunning = false;
            return;
        }
        sendEmptyMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
    }

    public void start() {
        sendEmptyMessage(OMASyncEventType.START.getId());
    }

    public void pause() {
        sendEmptyMessage(OMASyncEventType.PAUSE.getId());
    }

    public void resume() {
        sendEmptyMessage(OMASyncEventType.RESUME.getId());
    }

    public void stop() {
        sendEmptyMessage(OMASyncEventType.STOP.getId());
    }

    public void onGoToEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendEmptyMessage(i);
        }
    }

    private synchronized void onApiTreatAsSucceed(IHttpAPICommonInterface iHttpAPICommonInterface) {
        this.mINetAPIEventListener.onOmaSuccess(iHttpAPICommonInterface);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled() && ((this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getControllerOfLastFailedApi() == null || apiShouldMoveOn()) && this.mOnApiSucceedOnceListenerList.size() > 0)) {
            String str = this.TAG;
            Log.i(str, "mOnApiSucceedOnceListenerList.size() = " + this.mOnApiSucceedOnceListenerList.size());
            Iterator<OMANetAPIHandler.OnApiSucceedOnceListener> it = this.mOnApiSucceedOnceListenerList.iterator();
            while (it.hasNext()) {
                OMANetAPIHandler.OnApiSucceedOnceListener next = it.next();
                if (next != null) {
                    next.onMoveOn();
                }
            }
            this.mOnApiSucceedOnceListenerList.clear();
        }
    }

    private boolean apiShouldMoveOn() {
        Class<? extends IHttpAPICommonInterface> lastFailedApi = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getLastFailedApi();
        if (lastFailedApi == null) {
            return false;
        }
        String str = this.TAG;
        Log.i(str, "apiShouldMoveOn lastFailedApi:" + lastFailedApi);
        return CloudMessageCreateLargeDataPolling.class.getSimpleName().equalsIgnoreCase(lastFailedApi.getSimpleName());
    }

    public void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
        gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(iHttpAPICommonInterface, obj));
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        gotoHandlerEvent(OMASyncEventType.API_SUCCEED.getId(), iHttpAPICommonInterface);
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(iHttpAPICommonInterface, (Object) null));
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
        gotoHandlerEvent(OMASyncEventType.API_SUCCEED.getId(), iHttpAPICommonInterface);
        gotoHandlerEvent(i, obj);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
        gotoHandlerEventOnFailure(iHttpAPICommonInterface);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        gotoHandlerEventOnFailure(iHttpAPICommonInterface);
    }

    public void onFailedEvent(int i, Object obj) {
        gotoHandlerEvent(i, obj);
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, i);
        } else {
            sendEmptyMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
        }
    }

    public boolean update(int i) {
        sendMessage(obtainMessage(i));
        return true;
    }

    public boolean updateMessage(Message message) {
        return sendMessage(message);
    }

    private void checkAndUpdateSubscriptionChannel() {
        ReSyncParam.update(this.mStoreClient);
        if (this.mStoreClient.getPrerenceManager().getOMASubscriptionTime() == 0) {
            sendEmptyMessage(OMASyncEventType.CREATE_SUBSCRIPTION_CHANNEL.getId());
        } else if (this.mSchedulerHelper.isSubscriptionChannelGoingExpired(this.mStoreClient) || !this.mIsPollingNonStopRunning) {
            sendEmptyMessage(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId());
        } else {
            sendEmptyMessage(OMASyncEventType.SEND_LONG_POLLING_REQUEST.getId());
        }
    }

    private boolean shouldSendPollRequest() {
        return this.mIsPollingStarted && !this.mIsOnePollingRunning && this.mIsSchedulerRunning;
    }

    /* access modifiers changed from: protected */
    public void logWorkingStatus() {
        String str = this.TAG;
        Log.i(str, "mLine: " + IMSLog.checker(this.mLine) + " logWorkingStatus: [mIsPollingStarted: " + this.mIsPollingStarted + " mIsSchedulerRunning: " + this.mIsSchedulerRunning + " mIsPollingRunning: " + this.mIsOnePollingRunning + "]");
    }

    public synchronized void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
        if (onApiSucceedOnceListener == null) {
            Log.i(this.TAG, "listener == null, onOmaApiCredentialFailed, clear mOnApiSucceedOnceListenerList");
            this.mOnApiSucceedOnceListenerList.clear();
        } else {
            this.mOnApiSucceedOnceListenerList.add(onApiSucceedOnceListener);
        }
    }
}
