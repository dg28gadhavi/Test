package com.sec.internal.ims.cmstore.omanetapi.gcm;

import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLargeDataPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLongPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateNotificationChannels;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetActiveNotificationChannels;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetIndividualNotificationChannelInfo;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageUpdateNotificationChannelLifeTime;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateSubscriptionChannel;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualSubscription;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageUpdateSubscriptionChannel;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.CmsHttpController;
import com.sec.internal.ims.cmstore.utils.NotificationListContainer;
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

public class ChannelScheduler extends StateMachine implements IControllerCommonInterface, IAPICallFlowListener {
    private static final int STATE_EXPIRED = 2;
    private static final int STATE_GOING_EXPIRED = 1;
    private static final int STATE_NOT_EXPIRED = 0;
    private final int NO_RETRY_AFTER_VALUE = -1;
    public String TAG;
    public final String TAG_CN;
    State mChannelCheckingState = new ChannelCheckingState();
    State mChannelCreatedState = new ChannelCreatedState();
    State mChannelCreatingState = new ChannelCreatingState();
    State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public INetAPIEventListener mINetAPIEventListener = null;
    State mLargePollingState = new LargePollingState();
    /* access modifiers changed from: private */
    public String mLine;
    State mLongPollingState = new LongPollingState();
    private ArrayList<OMANetAPIHandler.OnApiSucceedOnceListener> mOnApiSucceedOnceListenerList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final ReSyncParam mReSyncParam = ReSyncParam.getInstance();
    /* access modifiers changed from: private */
    public SchedulerHelper mSchedulerHelper = null;
    /* access modifiers changed from: private */
    public MessageStoreClient mStoreClient;
    State mSubscribedState = new SubscribedState();
    State mSubscribingState = new SubscribingState();
    /* access modifiers changed from: private */
    public final IUIEventCallback mUIInterface;

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

    public boolean updateDelayRetry(int i, long j) {
        return false;
    }

    public ChannelScheduler(Looper looper, INetAPIEventListener iNetAPIEventListener, IUIEventCallback iUIEventCallback, MessageStoreClient messageStoreClient) {
        super("ChannelScheduler[" + messageStoreClient.getClientID() + "]", looper);
        Class<ChannelScheduler> cls = ChannelScheduler.class;
        this.TAG = cls.getSimpleName();
        this.TAG_CN = cls.getSimpleName();
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mStoreClient = messageStoreClient;
        this.mLine = messageStoreClient.getPrerenceManager().getUserTelCtn();
        this.mINetAPIEventListener = iNetAPIEventListener;
        this.mUIInterface = iUIEventCallback;
        ReSyncParam.update(messageStoreClient);
        this.mSchedulerHelper = SchedulerHelper.getInstance(getHandler());
        initStates();
    }

    /* access modifiers changed from: package-private */
    public OMASyncEventType InitEvent(Message message) {
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        return valueOf == null ? OMASyncEventType.DEFAULT : valueOf;
    }

    private void initStates() {
        addState(this.mDefaultState);
        addState(this.mChannelCheckingState, this.mDefaultState);
        addState(this.mChannelCreatingState, this.mChannelCheckingState);
        addState(this.mChannelCreatedState, this.mChannelCreatingState);
        addState(this.mSubscribingState, this.mChannelCreatedState);
        addState(this.mSubscribedState, this.mSubscribingState);
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            addState(this.mLargePollingState, this.mSubscribedState);
        } else {
            addState(this.mLongPollingState, this.mSubscribedState);
        }
        setInitialState(this.mDefaultState);
        super.start();
    }

    /* access modifiers changed from: private */
    public void gotoHandlerEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendMessage(i);
        }
    }

    private void gotoHandlerEventOnFailure(IHttpAPICommonInterface iHttpAPICommonInterface) {
        boolean isRetryEnabled = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled();
        String str = this.TAG_CN;
        int clientID = this.mStoreClient.getClientID();
        EventLogHelper.infoLogAndAdd(str, clientID, "gotoHandlerEventOnFailure isRetryEnabled: " + isRetryEnabled);
        if (isRetryEnabled) {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, -1);
            sendMessage(OMASyncEventType.RESET_STATE.getId());
            NotificationListContainer.getInstance(this.mStoreClient.getClientID()).clear();
            return;
        }
        sendMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
    }

    public void start() {
        sendMessage(OMASyncEventType.START.getId());
    }

    public void pause() {
        sendMessage(OMASyncEventType.PAUSE.getId());
    }

    public void resume() {
        sendMessage(OMASyncEventType.RESUME.getId());
    }

    public void stop() {
        sendMessage(OMASyncEventType.STOP.getId());
    }

    public void onGoToEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendMessage(i);
        }
    }

    /* access modifiers changed from: private */
    public synchronized void onApiTreatAsSucceed(IHttpAPICommonInterface iHttpAPICommonInterface) {
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
        String str = this.TAG_CN;
        int clientID = this.mStoreClient.getClientID();
        EventLogHelper.infoLogAndAdd(str, clientID, "apiShouldMoveOn lastFailedApi:" + lastFailedApi);
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
            sendMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
        }
    }

    public boolean update(int i) {
        if (i == OMASyncEventType.REMOVE_UPDATE_SUBSCRIPTION_CHANNEL.getId()) {
            removeMessages(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY.getId());
            return true;
        }
        sendMessage(obtainMessage(i));
        return true;
    }

    public boolean updateDelay(int i, long j) {
        String str = this.TAG;
        Log.i(str, "update with " + i + " delayed " + j);
        if (hasMessages(i)) {
            removeMessages(i);
        }
        sendMessageDelayed(obtainMessage(i), j);
        return true;
    }

    public boolean updateMessage(Message message) {
        sendMessage(message);
        return true;
    }

    /* access modifiers changed from: private */
    public void checkAndUpdateSubscriptionChannel() {
        ReSyncParam.update(this.mStoreClient);
        if (this.mStoreClient.getPrerenceManager().getOMASubscriptionTime() == 0) {
            sendMessage(OMASyncEventType.CREATE_SUBSCRIPTION_CHANNEL.getId());
        } else if (this.mSchedulerHelper.isSubscriptionChannelGoingExpired(this.mStoreClient)) {
            sendMessage(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId());
        } else if (!ATTGlobalVariables.isGcmReplacePolling()) {
            sendMessage(OMASyncEventType.SEND_LONG_POLLING_REQUEST.getId());
        }
    }

    public synchronized void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
        if (onApiSucceedOnceListener == null) {
            Log.i(this.TAG, "listener == null, onOmaApiCredentialFailed, clear mOnApiSucceedOnceListenerList");
            this.mOnApiSucceedOnceListenerList.clear();
        } else {
            this.mOnApiSucceedOnceListenerList.add(onApiSucceedOnceListener);
        }
    }

    public void updateNotificationChannnelLifeTime() {
        String oMAChannelResURL = this.mStoreClient.getPrerenceManager().getOMAChannelResURL();
        String str = this.TAG;
        Log.i(str, "updateNotificationChannnelLifeTime resUrl: " + IMSLog.checker(oMAChannelResURL));
        if (!TextUtils.isEmpty(oMAChannelResURL)) {
            this.mStoreClient.getHttpController().execute(new CloudMessageUpdateNotificationChannelLifeTime(this, this, oMAChannelResURL.substring(oMAChannelResURL.lastIndexOf(47) + 1), this.mStoreClient));
        }
    }

    /* access modifiers changed from: private */
    public int isNotificationChannelGoingExpired() {
        long oMAChannelCreateTime = (this.mStoreClient.getPrerenceManager().getOMAChannelCreateTime() + (this.mStoreClient.getPrerenceManager().getOMAChannelLifeTime() * 1000)) - System.currentTimeMillis();
        String str = this.TAG;
        Log.i(str, "isNotificationChannelGoingExpired remainingTime:" + oMAChannelCreateTime);
        if (oMAChannelCreateTime <= 0) {
            return 2;
        }
        if (oMAChannelCreateTime < 900000) {
            return 1;
        }
        OMASyncEventType oMASyncEventType = OMASyncEventType.UPDATE_NOTIFICATION_CHANNEL_LIFETIME;
        if (hasMessages(oMASyncEventType.getId())) {
            return 0;
        }
        updateDelay(oMASyncEventType.getId(), oMAChannelCreateTime - 900000);
        return 0;
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            ChannelScheduler.this.log("DefaultState, enter");
        }

        public boolean processMessage(Message message) {
            String str;
            OMASyncEventType InitEvent = ChannelScheduler.this.InitEvent(message);
            boolean z = false;
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()]) {
                case 1:
                    ChannelScheduler.this.sendMessage(OMASyncEventType.RESET_STATE.getId());
                    break;
                case 2:
                    ChannelScheduler.this.sendMessage(OMASyncEventType.CHECK_NOTIFICATION_CHANNEL.getId());
                    break;
                case 3:
                    ChannelScheduler.this.sendMessage(OMASyncEventType.RESET_STATE.getId());
                    break;
                case 4:
                    break;
                case 5:
                    if (ATTGlobalVariables.isGcmReplacePolling()) {
                        ChannelScheduler.this.sendMessage(OMASyncEventType.CHECK_ACTIVE_NOTIFICATION_CHANNEL.getId());
                    } else {
                        String oMAChannelResURL = ChannelScheduler.this.mStoreClient.getPrerenceManager().getOMAChannelResURL();
                        if (!TextUtils.isEmpty(oMAChannelResURL)) {
                            String substring = oMAChannelResURL.substring(oMAChannelResURL.lastIndexOf(47) + 1);
                            CmsHttpController httpController = ChannelScheduler.this.mStoreClient.getHttpController();
                            ChannelScheduler channelScheduler = ChannelScheduler.this;
                            httpController.execute(new CloudMessageGetIndividualNotificationChannelInfo(channelScheduler, channelScheduler, substring, channelScheduler.mStoreClient));
                        } else {
                            ChannelScheduler.this.sendMessage(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId());
                        }
                    }
                    ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                    channelScheduler2.transitionTo(channelScheduler2.mChannelCheckingState);
                    break;
                case 6:
                    CmsHttpController httpController2 = ChannelScheduler.this.mStoreClient.getHttpController();
                    ChannelScheduler channelScheduler3 = ChannelScheduler.this;
                    httpController2.execute(new CloudMessageDeleteIndividualSubscription(channelScheduler3, (String) message.obj, channelScheduler3.mStoreClient));
                    ChannelScheduler.this.mStoreClient.getPrerenceManager().saveOMASubscriptionResUrl("");
                    break;
                case 7:
                    Object obj = message.obj;
                    if (obj instanceof ChannelDeleteData) {
                        ChannelDeleteData channelDeleteData = (ChannelDeleteData) obj;
                        str = channelDeleteData.channelUrl;
                        z = channelDeleteData.isNeedRecreateChannel;
                        Log.d(ChannelScheduler.this.TAG, "need recreate channel");
                    } else {
                        str = (String) obj;
                    }
                    boolean z2 = z;
                    if (!TextUtils.isEmpty(str)) {
                        CmsHttpController httpController3 = ChannelScheduler.this.mStoreClient.getHttpController();
                        ChannelScheduler channelScheduler4 = ChannelScheduler.this;
                        httpController3.execute(new CloudMessageDeleteIndividualChannel(channelScheduler4, channelScheduler4, str.substring(str.lastIndexOf("/") + 1), z2, ChannelScheduler.this.mStoreClient));
                        break;
                    }
                    break;
                case 8:
                    ChannelScheduler.this.mINetAPIEventListener.onCloudSyncStopped(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MAILBOX_RESET).build());
                    break;
                case 9:
                    ChannelScheduler.this.mINetAPIEventListener.onCloudObjectNotificationUpdated((ParamOMAresponseforBufDB) message.obj);
                    break;
                case 10:
                    Object obj2 = message.obj;
                    if (obj2 != null) {
                        HttpRequestParams httpRequestParams = (HttpRequestParams) obj2;
                        ChannelScheduler channelScheduler5 = ChannelScheduler.this;
                        EventLogHelper.infoLogAndAdd(channelScheduler5.TAG_CN, channelScheduler5.mStoreClient.getClientID(), "ReExecute API " + httpRequestParams.getClass().getSimpleName() + " after 302 by using new url");
                        ChannelScheduler.this.mStoreClient.getHttpController().execute(httpRequestParams);
                        break;
                    }
                    break;
                case 11:
                    ParamOMAresponseforBufDB build = new ParamOMAresponseforBufDB.Builder().setLine(ChannelScheduler.this.mLine).build();
                    Object obj3 = message.obj;
                    ChannelScheduler.this.mINetAPIEventListener.onOmaAuthenticationFailed(build, (obj3 == null || !(obj3 instanceof Number)) ? 0 : ((Number) obj3).longValue());
                    ChannelScheduler.this.sendMessage(OMASyncEventType.RESET_STATE.getId());
                    break;
                case 12:
                    ChannelScheduler.this.mINetAPIEventListener.onPauseCMNNetApiWithResumeDelay(((Integer) message.obj).intValue());
                    break;
                case 13:
                    ChannelScheduler.this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                    break;
                case 14:
                    Object obj4 = message.obj;
                    if (obj4 != null) {
                        ChannelScheduler.this.onApiTreatAsSucceed((IHttpAPICommonInterface) obj4);
                        break;
                    }
                    break;
                case 15:
                    Object obj5 = message.obj;
                    if (obj5 != null) {
                        HttpResParamsWrapper httpResParamsWrapper = (HttpResParamsWrapper) obj5;
                        ChannelScheduler.this.onApiTreatAsSucceed(httpResParamsWrapper.mApi);
                        ChannelScheduler.this.gotoHandlerEvent(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId(), httpResParamsWrapper.mBufDbParams);
                        break;
                    }
                    break;
                case 16:
                    ChannelScheduler.this.pause();
                    ChannelScheduler.this.mSchedulerHelper.deleteNotificationSubscriptionResource(ChannelScheduler.this.mStoreClient);
                    break;
            }
            z = true;
            if (z) {
                ChannelScheduler.this.log("DefaultState, Handled : " + InitEvent);
            }
            return z;
        }

        public void exit() {
            ChannelScheduler.this.log("DefaultState, exit");
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler$1  reason: invalid class name */
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_SUBCRIPTION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_NOTIFICATION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MAILBOX_RESET     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CLOUD_UPDATE     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MSTORE_REDIRECT     // Catch:{ NoSuchFieldError -> 0x0078 }
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_SUCCEED     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.MOVE_ON     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DELETE_NOTIFICATION_SUBSCRIPTION_RESOURCE     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESET_STATE     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_ACTIVE_NOTIFICATION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_NOTIFICATION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00f0 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_NOTIFICATION_CHANNEL_FINISHED     // Catch:{ NoSuchFieldError -> 0x00f0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f0 }
                r2 = 20
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00f0 }
            L_0x00f0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00fc }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_SUBSCRIPTION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x00fc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fc }
                r2 = 21
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00fc }
            L_0x00fc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0108 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_NOTIFICATION_CHANNEL_LIFETIME     // Catch:{ NoSuchFieldError -> 0x0108 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0108 }
                r2 = 22
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0108 }
            L_0x0108:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0114 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING     // Catch:{ NoSuchFieldError -> 0x0114 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0114 }
                r2 = 23
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0114 }
            L_0x0114:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0120 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_SUBSCRIPTION_FINISHED     // Catch:{ NoSuchFieldError -> 0x0120 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0120 }
                r2 = 24
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0120 }
            L_0x0120:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x012c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.REQUEST_SUBSCRIPTION_AFTER_PSF_REMOVED     // Catch:{ NoSuchFieldError -> 0x012c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x012c }
                r2 = 25
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x012c }
            L_0x012c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0138 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0138 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0138 }
                r2 = 26
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0138 }
            L_0x0138:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0144 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SEND_LONG_POLLING_REQUEST     // Catch:{ NoSuchFieldError -> 0x0144 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0144 }
                r2 = 27
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0144 }
            L_0x0144:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0150 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST     // Catch:{ NoSuchFieldError -> 0x0150 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0150 }
                r2 = 28
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0150 }
            L_0x0150:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x015c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY     // Catch:{ NoSuchFieldError -> 0x015c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x015c }
                r2 = 29
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x015c }
            L_0x015c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0168 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.ONE_POLLING_FINISHED     // Catch:{ NoSuchFieldError -> 0x0168 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0168 }
                r2 = 30
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0168 }
            L_0x0168:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0174 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED     // Catch:{ NoSuchFieldError -> 0x0174 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0174 }
                r2 = 31
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0174 }
            L_0x0174:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.AnonymousClass1.<clinit>():void");
        }
    }

    private class ChannelCheckingState extends State {
        private ChannelCheckingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("ChannelCheckingState, enter");
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = ChannelScheduler.this.InitEvent(message);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            boolean z = true;
            if (i != 5) {
                switch (i) {
                    case 17:
                        ChannelScheduler channelScheduler = ChannelScheduler.this;
                        OMASyncEventType oMASyncEventType = OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST;
                        if (channelScheduler.hasMessages(oMASyncEventType.getId())) {
                            ChannelScheduler.this.removeMessages(oMASyncEventType.getId());
                        }
                        ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                        channelScheduler2.transitionTo(channelScheduler2.mDefaultState);
                        break;
                    case 18:
                        CmsHttpController httpController = ChannelScheduler.this.mStoreClient.getHttpController();
                        ChannelScheduler channelScheduler3 = ChannelScheduler.this;
                        httpController.execute(new CloudMessageGetActiveNotificationChannels(channelScheduler3, channelScheduler3, channelScheduler3.mStoreClient));
                        break;
                    case 19:
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            String gcmTokenFromVsim = ChannelScheduler.this.mStoreClient.getPrerenceManager().getGcmTokenFromVsim();
                            ChannelScheduler channelScheduler4 = ChannelScheduler.this;
                            EventLogHelper.infoLogAndAdd(channelScheduler4.TAG_CN, channelScheduler4.mStoreClient.getClientID(), "Get GCM token from NSDSProvider, gcmToken=" + IMSLog.checker(gcmTokenFromVsim));
                            if (TextUtils.isEmpty(gcmTokenFromVsim)) {
                                ChannelScheduler.this.mStoreClient.getPrerenceManager().getGcmTokenFromVsim();
                            } else if (message.obj == null) {
                                CmsHttpController httpController2 = ChannelScheduler.this.mStoreClient.getHttpController();
                                ChannelScheduler channelScheduler5 = ChannelScheduler.this;
                                httpController2.execute(new CloudMessageCreateNotificationChannels(channelScheduler5, channelScheduler5, false, channelScheduler5.mStoreClient));
                            } else {
                                CmsHttpController httpController3 = ChannelScheduler.this.mStoreClient.getHttpController();
                                ChannelScheduler channelScheduler6 = ChannelScheduler.this;
                                httpController3.execute(new CloudMessageCreateNotificationChannels(channelScheduler6, channelScheduler6, true, channelScheduler6.mStoreClient));
                            }
                        } else {
                            CmsHttpController httpController4 = ChannelScheduler.this.mStoreClient.getHttpController();
                            ChannelScheduler channelScheduler7 = ChannelScheduler.this;
                            httpController4.execute(new CloudMessageCreateNotificationChannels(channelScheduler7, channelScheduler7, true, channelScheduler7.mStoreClient));
                        }
                        ChannelScheduler channelScheduler8 = ChannelScheduler.this;
                        channelScheduler8.transitionTo(channelScheduler8.mChannelCreatingState);
                        break;
                    default:
                        z = false;
                        break;
                }
            }
            if (z) {
                ChannelScheduler.this.log("ChannelCheckingState, Handled : " + InitEvent);
            }
            return z;
        }

        public void exit() {
            ChannelScheduler.this.log("ChannelCheckingState, exit");
        }
    }

    private class ChannelCreatingState extends State {
        private ChannelCreatingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("ChannelCreatingState, enter");
        }

        public boolean processMessage(Message message) {
            boolean z;
            OMASyncEventType InitEvent = ChannelScheduler.this.InitEvent(message);
            if (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()] != 20) {
                z = false;
            } else {
                ChannelScheduler channelScheduler = ChannelScheduler.this;
                channelScheduler.transitionTo(channelScheduler.mChannelCreatedState);
                z = true;
            }
            if (z) {
                ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                channelScheduler2.log("ChannelCreatingState, Handled : " + InitEvent);
            }
            return z;
        }

        public void exit() {
            ChannelScheduler.this.log("ChannelCreatingState, exit");
        }
    }

    private class ChannelCreatedState extends State {
        private ChannelCreatedState() {
        }

        public void enter() {
            ChannelScheduler.this.log("ChannelCreatedState, enter");
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = ChannelScheduler.this.InitEvent(message);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()];
            boolean z = true;
            if (i != 5) {
                switch (i) {
                    case 21:
                        ReSyncParam unused = ChannelScheduler.this.mReSyncParam;
                        ReSyncParam.update(ChannelScheduler.this.mStoreClient);
                        if (!TextUtils.isEmpty(ChannelScheduler.this.mReSyncParam.getNotifyURL())) {
                            CmsHttpController httpController = ChannelScheduler.this.mStoreClient.getHttpController();
                            ChannelScheduler channelScheduler = ChannelScheduler.this;
                            String notifyURL = channelScheduler.mReSyncParam.getNotifyURL();
                            String restartToken = ChannelScheduler.this.mReSyncParam.getRestartToken();
                            ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                            httpController.execute(new CloudMessageCreateSubscriptionChannel(channelScheduler, notifyURL, restartToken, channelScheduler2, false, channelScheduler2.mStoreClient));
                            ChannelScheduler channelScheduler3 = ChannelScheduler.this;
                            String str = channelScheduler3.TAG;
                            int clientID = channelScheduler3.mStoreClient.getClientID();
                            EventLogHelper.add(str, clientID, " CREATE_SUBSCRIPTION_CHANNEL restartToken " + ChannelScheduler.this.mReSyncParam.getRestartToken());
                            ChannelScheduler channelScheduler4 = ChannelScheduler.this;
                            channelScheduler4.transitionTo(channelScheduler4.mSubscribingState);
                            break;
                        }
                        break;
                    case 22:
                        ChannelScheduler.this.updateNotificationChannnelLifeTime();
                        break;
                    case 23:
                        ChannelScheduler.this.checkAndUpdateSubscriptionChannel();
                        break;
                    default:
                        z = false;
                        break;
                }
            } else {
                String oMAChannelResURL = ChannelScheduler.this.mStoreClient.getPrerenceManager().getOMAChannelResURL();
                String str2 = ChannelScheduler.this.TAG;
                Log.i(str2, "resUrl: " + IMSLog.checker(oMAChannelResURL));
                if (TextUtils.isEmpty(oMAChannelResURL)) {
                    ChannelScheduler.this.sendMessage(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId());
                } else if (ATTGlobalVariables.isGcmReplacePolling()) {
                    int r1 = ChannelScheduler.this.isNotificationChannelGoingExpired();
                    if (r1 == 1) {
                        String substring = oMAChannelResURL.substring(oMAChannelResURL.lastIndexOf(47) + 1);
                        CmsHttpController httpController2 = ChannelScheduler.this.mStoreClient.getHttpController();
                        ChannelScheduler channelScheduler5 = ChannelScheduler.this;
                        httpController2.execute(new CloudMessageGetIndividualNotificationChannelInfo(channelScheduler5, channelScheduler5, substring, channelScheduler5.mStoreClient));
                    } else if (r1 == 2) {
                        ChannelScheduler.this.sendMessage(ChannelScheduler.this.obtainMessage(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId(), (Object) Boolean.TRUE));
                    }
                } else {
                    String substring2 = oMAChannelResURL.substring(oMAChannelResURL.lastIndexOf(47) + 1);
                    CmsHttpController httpController3 = ChannelScheduler.this.mStoreClient.getHttpController();
                    ChannelScheduler channelScheduler6 = ChannelScheduler.this;
                    httpController3.execute(new CloudMessageGetIndividualNotificationChannelInfo(channelScheduler6, channelScheduler6, substring2, channelScheduler6.mStoreClient));
                }
            }
            if (z) {
                ChannelScheduler channelScheduler7 = ChannelScheduler.this;
                channelScheduler7.log("ChannelCreatedState, Handled : " + InitEvent);
            }
            return z;
        }

        public void exit() {
            ChannelScheduler.this.log("ChannelCreatedState, exit");
        }
    }

    private class SubscribingState extends State {
        private SubscribingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("SubscribingState, enter");
        }

        /* JADX WARNING: Removed duplicated region for block: B:9:0x007b  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(android.os.Message r10) {
            /*
                r9 = this;
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r10 = r0.InitEvent(r10)
                int[] r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType
                int r1 = r10.ordinal()
                r0 = r0[r1]
                r1 = 24
                if (r0 == r1) goto L_0x0071
                r1 = 25
                if (r0 == r1) goto L_0x0018
                r0 = 0
                goto L_0x0079
            L_0x0018:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.ims.cmstore.MessageStoreClient r0 = r0.mStoreClient
                com.sec.internal.ims.cmstore.utils.CmsHttpController r0 = r0.getHttpController()
                com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateSubscriptionChannel r8 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateSubscriptionChannel
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.ims.cmstore.utils.ReSyncParam r1 = r2.mReSyncParam
                java.lang.String r3 = r1.getNotifyURL()
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r1 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.ims.cmstore.utils.ReSyncParam r1 = r1.mReSyncParam
                java.lang.String r4 = r1.getRestartToken()
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r5 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                r6 = 1
                com.sec.internal.ims.cmstore.MessageStoreClient r7 = r5.mStoreClient
                r1 = r8
                r1.<init>(r2, r3, r4, r5, r6, r7)
                r0.execute(r8)
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.String r1 = r0.TAG
                com.sec.internal.ims.cmstore.MessageStoreClient r0 = r0.mStoreClient
                int r0 = r0.getClientID()
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = " REQUEST_SUBSCRIPTION_AFTER_PSF_REMOVED restartToken "
                r2.append(r3)
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r3 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.ims.cmstore.utils.ReSyncParam r3 = r3.mReSyncParam
                java.lang.String r3 = r3.getRestartToken()
                r2.append(r3)
                java.lang.String r2 = r2.toString()
                com.sec.internal.ims.cmstore.helper.EventLogHelper.add(r1, r0, r2)
                goto L_0x0078
            L_0x0071:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.helper.State r1 = r0.mSubscribedState
                r0.transitionTo(r1)
            L_0x0078:
                r0 = 1
            L_0x0079:
                if (r0 == 0) goto L_0x0091
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r9 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "SubscribingState, Handled : "
                r1.append(r2)
                r1.append(r10)
                java.lang.String r10 = r1.toString()
                r9.log(r10)
            L_0x0091:
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.SubscribingState.processMessage(android.os.Message):boolean");
        }

        public void exit() {
            ChannelScheduler.this.log("SubscribingState, exit");
        }
    }

    private class SubscribedState extends State {
        private SubscribedState() {
        }

        public void enter() {
            ChannelScheduler.this.log("SubscribedState, enter");
        }

        public boolean processMessage(Message message) {
            boolean z;
            OMASyncEventType InitEvent = ChannelScheduler.this.InitEvent(message);
            String str = ChannelScheduler.this.TAG;
            Log.i(str, "event:  " + InitEvent.getId());
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()]) {
                case 24:
                    break;
                case 26:
                    ReSyncParam unused = ChannelScheduler.this.mReSyncParam;
                    ReSyncParam.update(ChannelScheduler.this.mStoreClient);
                    if (!TextUtils.isEmpty(ChannelScheduler.this.mReSyncParam.getChannelResURL())) {
                        CmsHttpController httpController = ChannelScheduler.this.mStoreClient.getHttpController();
                        ChannelScheduler channelScheduler = ChannelScheduler.this;
                        String restartToken = channelScheduler.mReSyncParam.getRestartToken();
                        String channelResURL = ChannelScheduler.this.mReSyncParam.getChannelResURL();
                        ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                        httpController.execute(new CloudMessageUpdateSubscriptionChannel(channelScheduler, restartToken, channelResURL, channelScheduler2, channelScheduler2.mStoreClient));
                        ChannelScheduler channelScheduler3 = ChannelScheduler.this;
                        String str2 = channelScheduler3.TAG;
                        int clientID = channelScheduler3.mStoreClient.getClientID();
                        EventLogHelper.add(str2, clientID, "UPDATE_SUBSCRIPTION_CHANNEL + restartToken " + ChannelScheduler.this.mReSyncParam.getRestartToken());
                        break;
                    }
                    break;
                case 27:
                    ChannelScheduler channelScheduler4 = ChannelScheduler.this;
                    CloudMessageCreateLongPolling cloudMessageCreateLongPolling = new CloudMessageCreateLongPolling(channelScheduler4, channelScheduler4.mReSyncParam.getChannelURL(), ChannelScheduler.this.mStoreClient);
                    cloudMessageCreateLongPolling.setReadTimeout(360000);
                    ChannelScheduler.this.mStoreClient.getHttpController().execute(cloudMessageCreateLongPolling);
                    ChannelScheduler channelScheduler5 = ChannelScheduler.this;
                    channelScheduler5.transitionTo(channelScheduler5.mLongPollingState);
                    break;
                case 28:
                    ChannelScheduler channelScheduler6 = ChannelScheduler.this;
                    CloudMessageCreateLargeDataPolling cloudMessageCreateLargeDataPolling = new CloudMessageCreateLargeDataPolling(channelScheduler6, channelScheduler6, (String) message.obj, channelScheduler6.mStoreClient);
                    cloudMessageCreateLargeDataPolling.setReadTimeout(360000);
                    ChannelScheduler.this.mStoreClient.getHttpController().execute(cloudMessageCreateLargeDataPolling);
                    ChannelScheduler channelScheduler7 = ChannelScheduler.this;
                    channelScheduler7.transitionTo(channelScheduler7.mLargePollingState);
                    break;
                case 29:
                    if (!NotificationListContainer.getInstance(ChannelScheduler.this.mStoreClient.getClientID()).isEmpty()) {
                        if (!ChannelScheduler.this.hasMessages(OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST.getId())) {
                            ReSyncParam unused2 = ChannelScheduler.this.mReSyncParam;
                            ReSyncParam.update(ChannelScheduler.this.mStoreClient);
                            if (!TextUtils.isEmpty(ChannelScheduler.this.mReSyncParam.getChannelResURL())) {
                                CmsHttpController httpController2 = ChannelScheduler.this.mStoreClient.getHttpController();
                                ChannelScheduler channelScheduler8 = ChannelScheduler.this;
                                String restartToken2 = channelScheduler8.mReSyncParam.getRestartToken();
                                String channelResURL2 = ChannelScheduler.this.mReSyncParam.getChannelResURL();
                                ChannelScheduler channelScheduler9 = ChannelScheduler.this;
                                httpController2.execute(new CloudMessageUpdateSubscriptionChannel(channelScheduler8, restartToken2, channelResURL2, channelScheduler9, channelScheduler9.mStoreClient));
                                ChannelScheduler channelScheduler10 = ChannelScheduler.this;
                                String str3 = channelScheduler10.TAG;
                                int clientID2 = channelScheduler10.mStoreClient.getClientID();
                                EventLogHelper.add(str3, clientID2, " UPDATE_SUBSCRIPTION_CHANNEL_DELAY restartToken " + ChannelScheduler.this.mReSyncParam.getRestartToken());
                                NotificationListContainer.getInstance(ChannelScheduler.this.mStoreClient.getClientID()).clear();
                                break;
                            }
                        } else {
                            ChannelScheduler.this.sendMessageDelayed(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY.getId(), (long) SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
                            break;
                        }
                    }
                    break;
                default:
                    z = false;
                    break;
            }
            z = true;
            if (z) {
                ChannelScheduler channelScheduler11 = ChannelScheduler.this;
                channelScheduler11.log("SubscribedState, Handled : " + InitEvent);
            }
            return z;
        }

        public void exit() {
            ChannelScheduler.this.log("SubscribedState, exit");
        }
    }

    private class LongPollingState extends State {
        private LongPollingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("LongPollingState, enter");
        }

        /* JADX WARNING: Removed duplicated region for block: B:8:0x003e  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(android.os.Message r4) {
            /*
                r3 = this;
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r4 = r0.InitEvent(r4)
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.String r0 = r0.TAG
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "event:  "
                r1.append(r2)
                int r2 = r4.getId()
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                android.util.Log.i(r0, r1)
                int[] r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType
                int r1 = r4.ordinal()
                r0 = r0[r1]
                r1 = 27
                if (r0 == r1) goto L_0x003b
                r1 = 30
                if (r0 == r1) goto L_0x0034
                r0 = 0
                goto L_0x003c
            L_0x0034:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.helper.State r1 = r0.mSubscribedState
                r0.transitionTo(r1)
            L_0x003b:
                r0 = 1
            L_0x003c:
                if (r0 == 0) goto L_0x0054
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r3 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "LongPollingState, Handled : "
                r1.append(r2)
                r1.append(r4)
                java.lang.String r4 = r1.toString()
                r3.log(r4)
            L_0x0054:
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.LongPollingState.processMessage(android.os.Message):boolean");
        }

        public void exit() {
            ChannelScheduler.this.log("LongPollingState, exit");
        }
    }

    private class LargePollingState extends State {
        private LargePollingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("LargePollingState, enter");
        }

        /* JADX WARNING: Removed duplicated region for block: B:9:0x0044  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(android.os.Message r5) {
            /*
                r4 = this;
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = r0.InitEvent(r5)
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r1 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.String r1 = r1.TAG
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "event:  "
                r2.append(r3)
                int r3 = r0.getId()
                r2.append(r3)
                java.lang.String r2 = r2.toString()
                android.util.Log.i(r1, r2)
                int[] r1 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType
                int r2 = r0.ordinal()
                r1 = r1[r2]
                r2 = 28
                if (r1 == r2) goto L_0x003c
                r5 = 31
                if (r1 == r5) goto L_0x0034
                r5 = 0
                goto L_0x0042
            L_0x0034:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r5 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.helper.State r1 = r5.mSubscribedState
                r5.transitionTo(r1)
                goto L_0x0041
            L_0x003c:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r1 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                r1.deferMessage(r5)
            L_0x0041:
                r5 = 1
            L_0x0042:
                if (r5 == 0) goto L_0x005a
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r4 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "LargePollingState, Handled : "
                r1.append(r2)
                r1.append(r0)
                java.lang.String r0 = r1.toString()
                r4.log(r0)
            L_0x005a:
                return r5
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.LargePollingState.processMessage(android.os.Message):boolean");
        }

        public void exit() {
            ChannelScheduler.this.log("LargePollingState, exit");
        }
    }
}
