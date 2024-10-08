package com.sec.internal.ims.cmstore.omanetapi.fcm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRuleHandling;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.nc.McsCreateNotificationChannel;
import com.sec.internal.ims.cmstore.omanetapi.nc.McsDeleteNotificationChannel;
import com.sec.internal.ims.cmstore.omanetapi.nc.McsGetNotificationChannelInfo;
import com.sec.internal.ims.cmstore.omanetapi.nc.McsGetNotificationChannelLifetime;
import com.sec.internal.ims.cmstore.omanetapi.nc.McsGetNotificationChannelListInfo;
import com.sec.internal.ims.cmstore.omanetapi.nc.McsUpdateNotificationChannelLifetime;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CmsHttpController;
import com.sec.internal.ims.cmstore.utils.RetryParam;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.ChannelDeleteData;

public class McsNotificationChannelScheduler extends StateMachine implements IAPICallFlowListener, IControllerCommonInterface {
    private static final String INTENT_ACTION_CHECK_NOTIFICATION_CHANNEL_LIFETIME = "com.samsung.ims.mcs.ACTION_CHECK_NOTIFICATION_CHANNEL_LIFETIME";
    private static final String INTENT_ACTION_RETRY_FAILED_API = "com.samsung.ims.mcs.ACTION_RETRY_FAILED_API";
    private final int NO_RETRY_AFTER_VALUE = -1;
    /* access modifiers changed from: private */
    public String TAG = McsNotificationChannelScheduler.class.getSimpleName();
    /* access modifiers changed from: private */
    public State mChannelCheckingState = new ChannelCheckingState();
    /* access modifiers changed from: private */
    public State mChannelCreatedState = new ChannelCreatedState();
    /* access modifiers changed from: private */
    public State mChannelCreatingState = new ChannelCreatingState();
    private PendingIntent mChannelLifeTimeExpiry;
    private Context mContext;
    /* access modifiers changed from: private */
    public State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public INetAPIEventListener mINetAPIEventListener = null;
    /* access modifiers changed from: private */
    public String mLine;
    /* access modifiers changed from: private */
    public IControllerCommonInterface mNetApiController = null;
    private OMANetAPIHandler.OnApiSucceedOnceListener mOnApiSucceedOnceListener = null;
    /* access modifiers changed from: private */
    public boolean mPaused = false;
    /* access modifiers changed from: private */
    public int mPhoneId;
    private PendingIntent mRetryIntent;
    /* access modifiers changed from: private */
    public MessageStoreClient mStoreClient;

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam, SyncMsgType syncMsgType, int i) {
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

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public boolean updateDelayRetry(int i, long j) {
        return false;
    }

    public McsNotificationChannelScheduler(Looper looper, IControllerCommonInterface iControllerCommonInterface, INetAPIEventListener iNetAPIEventListener, MessageStoreClient messageStoreClient) {
        super("McsNotificationChannelScheduler[" + messageStoreClient.getClientID() + "]", looper);
        this.mPhoneId = messageStoreClient.getClientID();
        this.TAG += "[" + this.mPhoneId + "]";
        this.mStoreClient = messageStoreClient;
        this.mNetApiController = iControllerCommonInterface;
        this.mINetAPIEventListener = iNetAPIEventListener;
        addState(this.mDefaultState);
        addState(this.mChannelCheckingState, this.mDefaultState);
        addState(this.mChannelCreatingState, this.mChannelCheckingState);
        addState(this.mChannelCreatedState, this.mChannelCreatingState);
        setInitialState(this.mDefaultState);
        this.mContext = this.mStoreClient.getContext();
        this.mLine = this.mStoreClient.getPrerenceManager().getUserTelCtn();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_CHECK_NOTIFICATION_CHANNEL_LIFETIME);
        intentFilter.addAction(INTENT_ACTION_RETRY_FAILED_API);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String r3 = McsNotificationChannelScheduler.this.TAG;
                IMSLog.i(r3, "onReceive: " + intent.getAction());
                int intExtra = intent.getIntExtra("phoneId", -1);
                if (intExtra == McsNotificationChannelScheduler.this.mPhoneId) {
                    String action = intent.getAction();
                    action.hashCode();
                    if (action.equals(McsNotificationChannelScheduler.INTENT_ACTION_RETRY_FAILED_API)) {
                        RetryParam retryParam = McsNotificationChannelScheduler.this.mStoreClient.getMcsRetryMapAdapter().getRetryParam(intent.getStringExtra("apiName"));
                        if (retryParam != null) {
                            McsNotificationChannelScheduler.this.sendMessage(OMASyncEventType.API_FAILED.getId(), (Object) retryParam.getMrequest());
                        }
                    } else if (action.equals(McsNotificationChannelScheduler.INTENT_ACTION_CHECK_NOTIFICATION_CHANNEL_LIFETIME)) {
                        EventLogHelper.add(McsNotificationChannelScheduler.this.TAG, intExtra, "onReceive: INTENT_ACTION_CHECK_NOTIFICATION_CHANNEL_LIFETIME");
                        IMSLog.c(LogClass.MCS_NC_LIFETIME_EXPIRY, McsNotificationChannelScheduler.this.mPhoneId + ",NC:LT_EX");
                        McsNotificationChannelScheduler.this.sendMessage(OMASyncEventType.CHECK_NOTIFICATION_CHANNEL_LIFETIME.getId());
                    }
                }
            }
        }, intentFilter);
        super.start();
    }

    /* access modifiers changed from: package-private */
    public OMASyncEventType InitEvent(Message message) {
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        return valueOf != null ? valueOf : OMASyncEventType.DEFAULT;
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        gotoHandlerEvent(OMASyncEventType.API_SUCCEED.getId(), iHttpAPICommonInterface);
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        if (iHttpAPICommonInterface instanceof McsDeleteNotificationChannel) {
            removeChannelLifeTimeEvent();
        }
    }

    /* access modifiers changed from: private */
    public void removeChannelLifeTimeEvent() {
        String str = this.TAG;
        IMSLog.i(str, "removeChannelLifeTimeEvent mChannelLifeTimeExpiry: " + this.mChannelLifeTimeExpiry);
        PendingIntent pendingIntent = this.mChannelLifeTimeExpiry;
        if (pendingIntent != null) {
            AlarmTimer.stop(this.mContext, pendingIntent);
            this.mChannelLifeTimeExpiry = null;
        }
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
        if (iHttpAPICommonInterface instanceof McsDeleteNotificationChannel) {
            removeChannelLifeTimeEvent();
        }
        gotoHandlerEvent(OMASyncEventType.API_SUCCEED.getId(), iHttpAPICommonInterface);
        gotoHandlerEvent(i, obj);
    }

    private void gotoHandlerEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendMessage(i);
        }
    }

    private void gotoHandlerEventOnFailure(IHttpAPICommonInterface iHttpAPICommonInterface) {
        String str = this.TAG;
        IMSLog.i(str, "gotoHandlerEventOnFailure isRetryEnabled: " + this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled());
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, -1);
        }
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        gotoHandlerEventOnFailure(iHttpAPICommonInterface);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        String simpleName = iHttpAPICommonInterface.getClass().getSimpleName();
        String str2 = this.TAG;
        IMSLog.i(str2, "onFailedCall: " + simpleName + " errorCode " + str);
        boolean checkAndIncreaseRetry = this.mStoreClient.getMcsRetryMapAdapter().checkAndIncreaseRetry(iHttpAPICommonInterface, Integer.valueOf(str).intValue());
        int retryCount = this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(simpleName);
        if (!checkAndIncreaseRetry) {
            return;
        }
        if (retryCount == 1) {
            updateDelayRetryRequest(simpleName, 10000);
        } else if (retryCount == 2) {
            updateDelayRetryRequest(simpleName, 30000);
        } else if (retryCount != 3) {
            String str3 = this.TAG;
            IMSLog.i(str3, " onFailed Call retry count " + retryCount);
            if (simpleName.equalsIgnoreCase(McsCreateNotificationChannel.class.getSimpleName())) {
                handleChannelCreationRecovery(iHttpAPICommonInterface, simpleName, retryCount);
            }
        } else {
            updateDelayRetryRequest(simpleName, SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
        }
    }

    public void onGoToEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendMessage(i);
        }
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        String simpleName = iHttpAPICommonInterface.getClass().getSimpleName();
        int retryCount = this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(simpleName);
        String str2 = this.TAG;
        IMSLog.i(str2, " OnOverRequest : request " + simpleName + "  error code " + str + "  retryAfter " + i);
        if (!simpleName.equalsIgnoreCase(McsCreateNotificationChannel.class.getSimpleName()) || retryCount <= 3) {
            updateDelayRetryRequest(simpleName, (long) i);
            return;
        }
        EventLogHelper.infoLogAndAdd(this.TAG, this.mPhoneId, "onOverRequest: max retry limit of create NC reached");
        this.mStoreClient.getMcsRetryMapAdapter().remove(iHttpAPICommonInterface);
    }

    private void handleChannelCreationRecovery(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        if (i < 4 || i > 9) {
            EventLogHelper.infoLogAndAdd(this.TAG, this.mPhoneId, "handleChannelCreationRecovery: max retry limit of create NC reached");
            this.mStoreClient.getMcsRetryMapAdapter().remove(iHttpAPICommonInterface);
            return;
        }
        long pow = ((long) Math.pow(2.0d, (double) (i - 4))) * 60 * 60 * 1000;
        String str2 = this.TAG;
        int i2 = this.mPhoneId;
        EventLogHelper.infoLogAndAdd(str2, i2, "exponential backoff retry for create NC delay:" + pow);
        updateDelayRetryRequest(str, pow);
    }

    public void start() {
        sendMessage(OMASyncEventType.RESET_STATE.getId());
        sendMessage(OMASyncEventType.START.getId());
    }

    public void pause() {
        sendMessage(OMASyncEventType.RESET_STATE.getId());
        sendMessage(OMASyncEventType.PAUSE.getId());
    }

    public void resume() {
        if (this.mPaused) {
            sendMessage(OMASyncEventType.RESET_STATE.getId());
            sendMessage(OMASyncEventType.RESUME.getId());
        }
    }

    public void stop() {
        sendMessage(OMASyncEventType.RESET_STATE.getId());
        sendMessage(OMASyncEventType.STOP.getId());
    }

    public boolean update(int i) {
        sendMessage(obtainMessage(i));
        return true;
    }

    public boolean updateMessage(Message message) {
        sendMessage(message);
        return true;
    }

    public boolean updateDelay(int i, long j) {
        if (i == OMASyncEventType.CHECK_NOTIFICATION_CHANNEL_LIFETIME.getId()) {
            PendingIntent pendingIntent = this.mChannelLifeTimeExpiry;
            if (pendingIntent != null) {
                AlarmTimer.stop(this.mContext, pendingIntent);
                this.mChannelLifeTimeExpiry = null;
            }
            Intent intent = new Intent(INTENT_ACTION_CHECK_NOTIFICATION_CHANNEL_LIFETIME);
            intent.setPackage(this.mContext.getPackageName());
            intent.putExtra("phoneId", this.mPhoneId);
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
            this.mChannelLifeTimeExpiry = broadcast;
            AlarmTimer.start(this.mContext, broadcast, j, false);
            return true;
        }
        if (hasMessages(i)) {
            removeMessages(i);
        }
        sendMessageDelayed(obtainMessage(i), j);
        return true;
    }

    public void updateDelayRetryRequest(String str, long j) {
        removeRetryEvent();
        Intent intent = new Intent(INTENT_ACTION_RETRY_FAILED_API);
        intent.setPackage(this.mContext.getPackageName());
        intent.putExtra("phoneId", this.mPhoneId);
        intent.putExtra("apiName", str);
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        this.mRetryIntent = broadcast;
        AlarmTimer.start(this.mContext, broadcast, j, false);
    }

    /* access modifiers changed from: private */
    public void removeRetryEvent() {
        PendingIntent pendingIntent = this.mRetryIntent;
        if (pendingIntent != null) {
            AlarmTimer.stop(this.mContext, pendingIntent);
            this.mRetryIntent = null;
        }
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
        if (onApiSucceedOnceListener == null) {
            IMSLog.e(this.TAG, "setOnApiSucceedOnceListener: listener is null");
        } else {
            this.mOnApiSucceedOnceListener = onApiSucceedOnceListener;
        }
    }

    /* access modifiers changed from: private */
    public synchronized void onApiTreatAsSucceed() {
        String str = this.TAG;
        IMSLog.i(str, "onApiTreatAsSucceed: mOnApiSucceedOnceListener: " + this.mOnApiSucceedOnceListener);
        OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener = this.mOnApiSucceedOnceListener;
        if (onApiSucceedOnceListener != null) {
            onApiSucceedOnceListener.onMoveOn();
        }
        this.mOnApiSucceedOnceListener = null;
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            IMSLog.i(McsNotificationChannelScheduler.this.TAG, "DefaultState: enter");
        }

        public boolean processMessage(Message message) {
            String str;
            String str2;
            OMASyncEventType InitEvent = McsNotificationChannelScheduler.this.InitEvent(message);
            String r1 = McsNotificationChannelScheduler.this.TAG;
            IMSLog.i(r1, "DefaultState: processMessage: " + InitEvent);
            boolean z = false;
            switch (AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()]) {
                case 1:
                case 2:
                case 3:
                    McsNotificationChannelScheduler.this.removeRetryEvent();
                    McsNotificationChannelScheduler.this.sendMessage(OMASyncEventType.CHECK_NOTIFICATION_CHANNEL_LIST_INFO.getId());
                    McsNotificationChannelScheduler.this.mPaused = false;
                    break;
                case 4:
                    if (McsNotificationChannelScheduler.this.mPaused) {
                        IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelScheduler already paused");
                        break;
                    }
                case 5:
                    McsNotificationChannelScheduler.this.mPaused = true;
                    McsNotificationChannelScheduler.this.removeRetryEvent();
                    String oMAChannelResURL = McsNotificationChannelScheduler.this.mStoreClient.getPrerenceManager().getOMAChannelResURL();
                    if (!TextUtils.isEmpty(oMAChannelResURL)) {
                        String substring = oMAChannelResURL.substring(oMAChannelResURL.lastIndexOf(47) + 1);
                        String r15 = McsNotificationChannelScheduler.this.TAG;
                        IMSLog.s(r15, "DefaultState: processMessage: channelId: " + substring + " resUrl: " + oMAChannelResURL + " isNeedRecreateChannel: " + false);
                        if (McsNotificationChannelScheduler.this.isAlreadyInRetry(McsDeleteNotificationChannel.class.getSimpleName())) {
                            McsNotificationChannelScheduler.this.removeRetryEvent();
                        }
                        CmsHttpController httpController = McsNotificationChannelScheduler.this.mStoreClient.getHttpController();
                        McsNotificationChannelScheduler mcsNotificationChannelScheduler = McsNotificationChannelScheduler.this;
                        httpController.execute(new McsDeleteNotificationChannel(mcsNotificationChannelScheduler, mcsNotificationChannelScheduler.mStoreClient, substring, false, McsConstants.ChannelDeleteReason.NORMAL, oMAChannelResURL));
                        break;
                    } else {
                        IMSLog.e(McsNotificationChannelScheduler.this.TAG, "Empty url, do not process delete");
                        break;
                    }
                case 6:
                    if (!McsNotificationChannelScheduler.this.mPaused) {
                        McsNotificationChannelScheduler.this.mPaused = true;
                        McsNotificationChannelScheduler.this.resetChannelData();
                        break;
                    }
                    break;
                case 7:
                    if (McsNotificationChannelScheduler.this.isAlreadyInRetry(McsGetNotificationChannelListInfo.class.getSimpleName())) {
                        McsNotificationChannelScheduler.this.removeRetryEvent();
                    }
                    CmsHttpController httpController2 = McsNotificationChannelScheduler.this.mStoreClient.getHttpController();
                    McsNotificationChannelScheduler mcsNotificationChannelScheduler2 = McsNotificationChannelScheduler.this;
                    httpController2.execute(new McsGetNotificationChannelListInfo(mcsNotificationChannelScheduler2, mcsNotificationChannelScheduler2.mStoreClient));
                    McsNotificationChannelScheduler mcsNotificationChannelScheduler3 = McsNotificationChannelScheduler.this;
                    mcsNotificationChannelScheduler3.transitionTo(mcsNotificationChannelScheduler3.mChannelCheckingState);
                    break;
                case 8:
                    String oMAChannelResURL2 = McsNotificationChannelScheduler.this.mStoreClient.getPrerenceManager().getOMAChannelResURL();
                    if (!TextUtils.isEmpty(oMAChannelResURL2)) {
                        String substring2 = oMAChannelResURL2.substring(oMAChannelResURL2.lastIndexOf(47) + 1);
                        String r12 = McsNotificationChannelScheduler.this.TAG;
                        IMSLog.s(r12, "DefaultState: processMessage: channelId: " + substring2 + " resUrl: " + oMAChannelResURL2);
                        if (McsNotificationChannelScheduler.this.isAlreadyInRetry(McsGetNotificationChannelInfo.class.getSimpleName())) {
                            McsNotificationChannelScheduler.this.removeRetryEvent();
                        }
                        CmsHttpController httpController3 = McsNotificationChannelScheduler.this.mStoreClient.getHttpController();
                        McsNotificationChannelScheduler mcsNotificationChannelScheduler4 = McsNotificationChannelScheduler.this;
                        httpController3.execute(new McsGetNotificationChannelInfo(mcsNotificationChannelScheduler4, mcsNotificationChannelScheduler4, substring2, mcsNotificationChannelScheduler4.mStoreClient));
                        break;
                    }
                    break;
                case 9:
                    Object obj = message.obj;
                    if (obj == null || !(obj instanceof ChannelDeleteData)) {
                        str2 = "";
                        str = McsConstants.ChannelDeleteReason.NORMAL;
                    } else {
                        ChannelDeleteData channelDeleteData = (ChannelDeleteData) obj;
                        str2 = channelDeleteData.channelUrl;
                        z = channelDeleteData.isNeedRecreateChannel;
                        str = channelDeleteData.deleteReason;
                    }
                    String str3 = str2;
                    boolean z2 = z;
                    if (!TextUtils.isEmpty(str3)) {
                        String substring3 = str3.substring(str3.lastIndexOf(47) + 1);
                        String r0 = McsNotificationChannelScheduler.this.TAG;
                        IMSLog.s(r0, "DefaultState: processMessage: channelId: " + substring3 + " resUrl: " + str3 + " isNeedRecreateChannel: " + z2);
                        if (McsNotificationChannelScheduler.this.isAlreadyInRetry(McsDeleteNotificationChannel.class.getSimpleName())) {
                            McsNotificationChannelScheduler.this.removeRetryEvent();
                        }
                        CmsHttpController httpController4 = McsNotificationChannelScheduler.this.mStoreClient.getHttpController();
                        McsNotificationChannelScheduler mcsNotificationChannelScheduler5 = McsNotificationChannelScheduler.this;
                        httpController4.execute(new McsDeleteNotificationChannel(mcsNotificationChannelScheduler5, mcsNotificationChannelScheduler5.mStoreClient, substring3, z2, str, str3));
                    }
                    if (str == McsConstants.ChannelDeleteReason.NONDMA) {
                        IMSLog.i(McsNotificationChannelScheduler.this.TAG, "DMA change, move to default state and pause scheduler");
                        McsNotificationChannelScheduler.this.mPaused = true;
                        McsNotificationChannelScheduler mcsNotificationChannelScheduler6 = McsNotificationChannelScheduler.this;
                        mcsNotificationChannelScheduler6.transitionTo(mcsNotificationChannelScheduler6.mDefaultState);
                        break;
                    }
                    break;
                case 10:
                    IHttpAPICommonInterface iHttpAPICommonInterface = (IHttpAPICommonInterface) message.obj;
                    if (iHttpAPICommonInterface != null) {
                        McsNotificationChannelScheduler mcsNotificationChannelScheduler7 = McsNotificationChannelScheduler.this;
                        ErrorRuleHandling.handleMcsError(mcsNotificationChannelScheduler7, mcsNotificationChannelScheduler7.mStoreClient, iHttpAPICommonInterface);
                        break;
                    }
                    break;
                case 11:
                    ParamOMAresponseforBufDB build = new ParamOMAresponseforBufDB.Builder().setLine(McsNotificationChannelScheduler.this.mLine).build();
                    Object obj2 = message.obj;
                    McsNotificationChannelScheduler.this.mINetAPIEventListener.onOmaAuthenticationFailed(build, (obj2 == null || !(obj2 instanceof Number)) ? 0 : ((Number) obj2).longValue());
                    break;
                case 12:
                    if (message.obj != null) {
                        McsNotificationChannelScheduler.this.onApiTreatAsSucceed();
                        break;
                    }
                    break;
                default:
                    IMSLog.i(McsNotificationChannelScheduler.this.TAG, "DefaultState: processMessage: unknown event");
                    return false;
            }
            return true;
        }

        public void exit() {
            IMSLog.i(McsNotificationChannelScheduler.this.TAG, "DefaultState: exit");
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.fcm.McsNotificationChannelScheduler$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        /* JADX WARNING: Can't wrap try/catch for region: R(36:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|(3:35|36|38)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(38:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|38) */
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESUME     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESUME_ON_FCM_TOKEN_REFRESH     // Catch:{ NoSuchFieldError -> 0x0028 }
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.STOP     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.PAUSE_ON_DEREGISTRATION     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_NOTIFICATION_CHANNEL_LIST_INFO     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_NOTIFICATION_CHANNEL_INFO     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_NOTIFICATION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x006c }
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREDENTIAL_EXPIRED     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_SUCCEED     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESET_STATE     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_NOTIFICATION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_NOTIFICATION_CHANNEL_FINISHED     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_NOTIFICATION_CHANNEL_LIFETIME     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_NOTIFICATION_CHANNEL_LIFETIME     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_NOTIFICATION_CHANNEL_LIFETIME_FINISHED     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.fcm.McsNotificationChannelScheduler.AnonymousClass2.<clinit>():void");
        }
    }

    private class ChannelCheckingState extends State {
        private ChannelCheckingState() {
        }

        public void enter() {
            IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCheckingState: enter");
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = McsNotificationChannelScheduler.this.InitEvent(message);
            String r0 = McsNotificationChannelScheduler.this.TAG;
            IMSLog.i(r0, "ChannelCheckingState: processMessage: " + InitEvent);
            int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            if (i == 13) {
                McsNotificationChannelScheduler.this.removeChannelLifeTimeEvent();
                McsNotificationChannelScheduler mcsNotificationChannelScheduler = McsNotificationChannelScheduler.this;
                mcsNotificationChannelScheduler.transitionTo(mcsNotificationChannelScheduler.mDefaultState);
            } else if (i != 14) {
                IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCheckingState: processMessage: unknown event");
                return false;
            } else {
                String fcmRegistrationToken = McsNotificationChannelScheduler.this.mStoreClient.getPrerenceManager().getFcmRegistrationToken();
                if (TextUtils.isEmpty(fcmRegistrationToken)) {
                    EventLogHelper.infoLogAndAdd(McsNotificationChannelScheduler.this.TAG, McsNotificationChannelScheduler.this.mPhoneId, "fcm registration token is empty wait for token");
                    McsNotificationChannelScheduler.this.mNetApiController.update(20);
                    McsNotificationChannelScheduler mcsNotificationChannelScheduler2 = McsNotificationChannelScheduler.this;
                    mcsNotificationChannelScheduler2.transitionTo(mcsNotificationChannelScheduler2.mDefaultState);
                } else if (McsNotificationChannelScheduler.this.isAlreadyInRetry(McsCreateNotificationChannel.class.getSimpleName())) {
                    McsNotificationChannelScheduler.this.removeRetryEvent();
                }
                CmsHttpController httpController = McsNotificationChannelScheduler.this.mStoreClient.getHttpController();
                McsNotificationChannelScheduler mcsNotificationChannelScheduler3 = McsNotificationChannelScheduler.this;
                httpController.execute(new McsCreateNotificationChannel(mcsNotificationChannelScheduler3, mcsNotificationChannelScheduler3, fcmRegistrationToken, mcsNotificationChannelScheduler3.mStoreClient));
                McsNotificationChannelScheduler mcsNotificationChannelScheduler4 = McsNotificationChannelScheduler.this;
                mcsNotificationChannelScheduler4.transitionTo(mcsNotificationChannelScheduler4.mChannelCreatingState);
            }
            return true;
        }

        public void exit() {
            IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCheckingState: exit");
        }
    }

    /* access modifiers changed from: private */
    public boolean isAlreadyInRetry(String str) {
        return this.mStoreClient.getMcsRetryMapAdapter() != null && this.mStoreClient.getMcsRetryMapAdapter().isAlreadyInRetry(str);
    }

    private class ChannelCreatingState extends State {
        private ChannelCreatingState() {
        }

        public void enter() {
            IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCreatingState: enter");
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = McsNotificationChannelScheduler.this.InitEvent(message);
            String r0 = McsNotificationChannelScheduler.this.TAG;
            IMSLog.i(r0, "ChannelCreatingState: processMessage: " + InitEvent);
            int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            if (i == 13) {
                McsNotificationChannelScheduler.this.removeChannelLifeTimeEvent();
                McsNotificationChannelScheduler mcsNotificationChannelScheduler = McsNotificationChannelScheduler.this;
                mcsNotificationChannelScheduler.transitionTo(mcsNotificationChannelScheduler.mDefaultState);
            } else if (i != 15) {
                IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCreatingState: processMessage: unknown event");
                return false;
            } else {
                IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCreatingState: processMessage: update EVENT_CHECK_SUBSCRIPTION_CHANNEL");
                McsNotificationChannelScheduler.this.mNetApiController.update(18);
                McsNotificationChannelScheduler mcsNotificationChannelScheduler2 = McsNotificationChannelScheduler.this;
                mcsNotificationChannelScheduler2.transitionTo(mcsNotificationChannelScheduler2.mChannelCreatedState);
            }
            return true;
        }

        public void exit() {
            IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCreatingState: exit");
        }
    }

    private class ChannelCreatedState extends State {
        private ChannelCreatedState() {
        }

        public void enter() {
            IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCreatedState: enter");
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = McsNotificationChannelScheduler.this.InitEvent(message);
            String r1 = McsNotificationChannelScheduler.this.TAG;
            IMSLog.i(r1, "ChannelCreatedState: processMessage: " + InitEvent);
            int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            if (i != 13) {
                switch (i) {
                    case 16:
                        String oMAChannelResURL = McsNotificationChannelScheduler.this.mStoreClient.getPrerenceManager().getOMAChannelResURL();
                        if (TextUtils.isEmpty(oMAChannelResURL)) {
                            return true;
                        }
                        String substring = oMAChannelResURL.substring(oMAChannelResURL.lastIndexOf(47) + 1);
                        String r4 = McsNotificationChannelScheduler.this.TAG;
                        IMSLog.s(r4, "ChannelCreatedState: processMessage: channelId: " + substring + " resUrl: " + oMAChannelResURL);
                        if (McsNotificationChannelScheduler.this.isAlreadyInRetry(McsGetNotificationChannelLifetime.class.getSimpleName())) {
                            McsNotificationChannelScheduler.this.removeRetryEvent();
                        }
                        CmsHttpController httpController = McsNotificationChannelScheduler.this.mStoreClient.getHttpController();
                        McsNotificationChannelScheduler mcsNotificationChannelScheduler = McsNotificationChannelScheduler.this;
                        httpController.execute(new McsGetNotificationChannelLifetime(mcsNotificationChannelScheduler, mcsNotificationChannelScheduler, substring, mcsNotificationChannelScheduler.mStoreClient));
                        return true;
                    case 17:
                        String oMAChannelResURL2 = McsNotificationChannelScheduler.this.mStoreClient.getPrerenceManager().getOMAChannelResURL();
                        if (TextUtils.isEmpty(oMAChannelResURL2)) {
                            return true;
                        }
                        String substring2 = oMAChannelResURL2.substring(oMAChannelResURL2.lastIndexOf(47) + 1);
                        String r42 = McsNotificationChannelScheduler.this.TAG;
                        IMSLog.s(r42, "ChannelCreatedState: processMessage: channelId: " + substring2 + " resUrl: " + oMAChannelResURL2);
                        if (McsNotificationChannelScheduler.this.isAlreadyInRetry(McsUpdateNotificationChannelLifetime.class.getSimpleName())) {
                            McsNotificationChannelScheduler.this.removeRetryEvent();
                        }
                        CmsHttpController httpController2 = McsNotificationChannelScheduler.this.mStoreClient.getHttpController();
                        McsNotificationChannelScheduler mcsNotificationChannelScheduler2 = McsNotificationChannelScheduler.this;
                        httpController2.execute(new McsUpdateNotificationChannelLifetime(mcsNotificationChannelScheduler2, mcsNotificationChannelScheduler2, substring2, mcsNotificationChannelScheduler2.mStoreClient));
                        return true;
                    case 18:
                        boolean booleanValue = ((Boolean) message.obj).booleanValue();
                        String r0 = McsNotificationChannelScheduler.this.TAG;
                        IMSLog.i(r0, "ChannelCreatedState: processMessage: success: " + booleanValue);
                        if (!booleanValue) {
                            ChannelDeleteData channelDeleteData = new ChannelDeleteData();
                            channelDeleteData.deleteReason = McsConstants.ChannelDeleteReason.NORMAL;
                            channelDeleteData.isNeedRecreateChannel = false;
                            channelDeleteData.channelUrl = McsNotificationChannelScheduler.this.mStoreClient.getPrerenceManager().getOMAChannelResURL();
                            String r12 = McsNotificationChannelScheduler.this.TAG;
                            IMSLog.s(r12, "ChannelCreatedState: processMessage: send DELETE_NOTIFICATION_CHANNEL resUrl: " + channelDeleteData.channelUrl);
                            McsNotificationChannelScheduler mcsNotificationChannelScheduler3 = McsNotificationChannelScheduler.this;
                            mcsNotificationChannelScheduler3.sendMessage(mcsNotificationChannelScheduler3.obtainMessage(OMASyncEventType.DELETE_NOTIFICATION_CHANNEL.getId(), (Object) channelDeleteData));
                        }
                        McsNotificationChannelScheduler.this.mNetApiController.updateMessage(McsNotificationChannelScheduler.this.obtainMessage(19, (Object) Boolean.valueOf(booleanValue)));
                        return true;
                    default:
                        IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCreatedState: processMessage: unknown event");
                        return false;
                }
            } else {
                McsNotificationChannelScheduler.this.removeChannelLifeTimeEvent();
                McsNotificationChannelScheduler mcsNotificationChannelScheduler4 = McsNotificationChannelScheduler.this;
                mcsNotificationChannelScheduler4.transitionTo(mcsNotificationChannelScheduler4.mDefaultState);
                return true;
            }
        }

        public void exit() {
            IMSLog.i(McsNotificationChannelScheduler.this.TAG, "ChannelCreatedState: exit");
        }
    }

    public void resetChannelData() {
        IMSLog.i(this.TAG, "resetChannelData");
        CloudMessagePreferenceManager prerenceManager = this.mStoreClient.getPrerenceManager();
        prerenceManager.saveOMAChannelResURL("");
        prerenceManager.saveOMACallBackURL("");
        prerenceManager.saveOMAChannelLifeTime(0);
        prerenceManager.saveOMAChannelCreateTime(0);
    }
}
