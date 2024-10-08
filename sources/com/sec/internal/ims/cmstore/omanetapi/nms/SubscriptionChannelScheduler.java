package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRuleHandling;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.nc.McsCreateLargeDataPolling;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.CmsHttpController;
import com.sec.internal.ims.cmstore.utils.McsNotificationListContainer;
import com.sec.internal.ims.cmstore.utils.ReSyncParam;
import com.sec.internal.ims.cmstore.utils.RetryParam;
import com.sec.internal.ims.cmstore.utils.SchedulerHelper;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IMcsFcmPushNotificationListener;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.McsLargePollingNotification;
import com.sec.internal.omanetapi.nms.data.NmsEventList;

public class SubscriptionChannelScheduler extends StateMachine implements IAPICallFlowListener, IControllerCommonInterface {
    private static final String INTENT_ACTION_RETRY_SUBSCRIPTION_FAILED_API = "com.samsung.ims.mcs.ACTION_RETRY_SUBSCRIPTION_FAILED_API";
    private final int NO_RETRY_AFTER_VALUE = -1;
    public String TAG = SubscriptionChannelScheduler.class.getSimpleName();
    /* access modifiers changed from: private */
    public boolean isDelaySubscriptionUpdateInProgress = false;
    private Context mContext;
    State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public INetAPIEventListener mINetAPIEventListener = null;
    State mLargePollingState = new LargePollingState();
    /* access modifiers changed from: private */
    public String mLine;
    /* access modifiers changed from: private */
    public int mPhoneId;
    /* access modifiers changed from: private */
    public final ReSyncParam mReSyncParam = ReSyncParam.getInstance();
    private PendingIntent mRetryIntent;
    private SchedulerHelper mSchedulerHelper = null;
    MessageStoreClient mStoreClient;
    State mSubscribedState = new SubscribedState();
    State mSubscribingState = new SubscribingState();

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

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
    }

    public boolean updateDelayRetry(int i, long j) {
        return false;
    }

    public SubscriptionChannelScheduler(Looper looper, INetAPIEventListener iNetAPIEventListener, MessageStoreClient messageStoreClient) {
        super("SubscriptionChannelScheduler[" + messageStoreClient.getClientID() + "]", looper);
        this.mStoreClient = messageStoreClient;
        this.mPhoneId = messageStoreClient.getClientID();
        this.mContext = this.mStoreClient.getContext();
        this.mLine = this.mStoreClient.getPrerenceManager().getUserTelCtn();
        this.mINetAPIEventListener = iNetAPIEventListener;
        ReSyncParam.update(messageStoreClient);
        this.mSchedulerHelper = SchedulerHelper.getInstance(getHandler());
        initStates();
        registerLargePollingNotificationListener();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_RETRY_SUBSCRIPTION_FAILED_API);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                RetryParam retryParam;
                String str = SubscriptionChannelScheduler.this.TAG;
                IMSLog.i(str, "onReceive: " + intent.getAction());
                if (intent.getIntExtra("phoneId", -1) == SubscriptionChannelScheduler.this.mPhoneId && SubscriptionChannelScheduler.INTENT_ACTION_RETRY_SUBSCRIPTION_FAILED_API.equals(intent.getAction()) && (retryParam = SubscriptionChannelScheduler.this.mStoreClient.getMcsRetryMapAdapter().getRetryParam(intent.getStringExtra("apiName"))) != null) {
                    SubscriptionChannelScheduler.this.sendMessage(OMASyncEventType.API_FAILED.getId(), (Object) retryParam.getMrequest());
                }
            }
        }, intentFilter);
    }

    private void registerLargePollingNotificationListener() {
        this.mStoreClient.setMcsFcmPushNotificationListener(new IMcsFcmPushNotificationListener() {
            public void nmsEventListPushNotification(NmsEventList nmsEventList) {
            }

            public void syncBlockfilterPushNotification(String str) {
            }

            public void syncConfigPushNotification(String str) {
            }

            public void syncContactPushNotification(String str) {
            }

            public void syncMessagePushNotification(String str, int i) {
            }

            public void syncStatusPushNotification(String str) {
            }

            public void largePollingPushNotification(McsLargePollingNotification mcsLargePollingNotification) {
                if (Util.hasChannelExpired(mcsLargePollingNotification.channelExpiry)) {
                    IMSLog.i(SubscriptionChannelScheduler.this.TAG, "Large polling channel has expired");
                    return;
                }
                String str = mcsLargePollingNotification.channelURL;
                if (!TextUtils.isEmpty(str) && !SubscriptionChannelScheduler.this.isDelaySubscriptionUpdateInProgress) {
                    SubscriptionChannelScheduler.this.sendMessage(OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST.getId(), (Object) str);
                }
            }
        });
    }

    private void initStates() {
        addState(this.mDefaultState);
        addState(this.mSubscribingState, this.mDefaultState);
        addState(this.mSubscribedState, this.mSubscribingState);
        addState(this.mLargePollingState, this.mSubscribedState);
        setInitialState(this.mDefaultState);
        super.start();
    }

    /* access modifiers changed from: package-private */
    public OMASyncEventType InitEvent(Message message) {
        OMASyncEventType valueOf = OMASyncEventType.valueOf(message.what);
        return valueOf == null ? OMASyncEventType.DEFAULT : valueOf;
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            SubscriptionChannelScheduler.this.log("DefaultState, enter");
        }

        public boolean processMessage(Message message) {
            boolean z;
            OMASyncEventType InitEvent = SubscriptionChannelScheduler.this.InitEvent(message);
            String str = SubscriptionChannelScheduler.this.TAG;
            IMSLog.d(str, "Default state processMessage:  " + InitEvent);
            switch (AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()]) {
                case 1:
                case 2:
                    SubscriptionChannelScheduler.this.sendMessage(OMASyncEventType.RESET_STATE.getId());
                    break;
                case 3:
                    break;
                case 4:
                    SubscriptionChannelScheduler.this.checkAndUpdateSubscriptionChannel();
                    break;
                case 5:
                    ReSyncParam unused = SubscriptionChannelScheduler.this.mReSyncParam;
                    ReSyncParam.update(SubscriptionChannelScheduler.this.mStoreClient);
                    if (!TextUtils.isEmpty(SubscriptionChannelScheduler.this.mReSyncParam.getNotifyURL())) {
                        CmsHttpController httpController = SubscriptionChannelScheduler.this.mStoreClient.getHttpController();
                        SubscriptionChannelScheduler subscriptionChannelScheduler = SubscriptionChannelScheduler.this;
                        String notifyURL = subscriptionChannelScheduler.mReSyncParam.getNotifyURL();
                        String restartToken = SubscriptionChannelScheduler.this.mReSyncParam.getRestartToken();
                        SubscriptionChannelScheduler subscriptionChannelScheduler2 = SubscriptionChannelScheduler.this;
                        httpController.execute(new CloudMessageCreateSubscriptionChannel(subscriptionChannelScheduler, notifyURL, restartToken, subscriptionChannelScheduler2, false, subscriptionChannelScheduler2.mStoreClient));
                        SubscriptionChannelScheduler subscriptionChannelScheduler3 = SubscriptionChannelScheduler.this;
                        subscriptionChannelScheduler3.transitionTo(subscriptionChannelScheduler3.mSubscribingState);
                        break;
                    }
                    break;
                case 6:
                    CmsHttpController httpController2 = SubscriptionChannelScheduler.this.mStoreClient.getHttpController();
                    SubscriptionChannelScheduler subscriptionChannelScheduler4 = SubscriptionChannelScheduler.this;
                    httpController2.execute(new CloudMessageDeleteIndividualSubscription(subscriptionChannelScheduler4, (String) message.obj, subscriptionChannelScheduler4.mStoreClient));
                    SubscriptionChannelScheduler.this.mStoreClient.getPrerenceManager().saveOMASubscriptionResUrl("");
                    break;
                case 7:
                    IHttpAPICommonInterface iHttpAPICommonInterface = (IHttpAPICommonInterface) message.obj;
                    if (iHttpAPICommonInterface != null) {
                        SubscriptionChannelScheduler subscriptionChannelScheduler5 = SubscriptionChannelScheduler.this;
                        ErrorRuleHandling.handleMcsError(subscriptionChannelScheduler5, subscriptionChannelScheduler5.mStoreClient, iHttpAPICommonInterface);
                        break;
                    }
                    break;
                case 8:
                    ParamOMAresponseforBufDB build = new ParamOMAresponseforBufDB.Builder().setLine(SubscriptionChannelScheduler.this.mLine).build();
                    Object obj = message.obj;
                    SubscriptionChannelScheduler.this.mINetAPIEventListener.onOmaAuthenticationFailed(build, (obj == null || !(obj instanceof Number)) ? 0 : ((Number) obj).longValue());
                    break;
                default:
                    z = false;
                    break;
            }
            z = true;
            if (z) {
                SubscriptionChannelScheduler subscriptionChannelScheduler6 = SubscriptionChannelScheduler.this;
                subscriptionChannelScheduler6.log("Default, Handled : " + InitEvent);
            }
            return z;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.START     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.STOP     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.PAUSE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_SUBSCRIPTION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_SUBSCRIPTION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x003e }
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
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.API_FAILED     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREDENTIAL_EXPIRED     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESET_STATE     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_SUBSCRIPTION_FINISHED     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.UPDATE_DELAYED_SUBSCRIPTION_COMPLETE     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler.AnonymousClass3.<clinit>():void");
        }
    }

    private class SubscribingState extends State {
        private SubscribingState() {
        }

        public void enter() {
            SubscriptionChannelScheduler.this.log("SubscribingState, enter");
        }

        /* JADX WARNING: Removed duplicated region for block: B:9:0x0042  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(android.os.Message r4) {
            /*
                r3 = this;
                com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r4 = r0.InitEvent(r4)
                com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler.this
                java.lang.String r0 = r0.TAG
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "Subscribing state processMessage:  "
                r1.append(r2)
                r1.append(r4)
                java.lang.String r1 = r1.toString()
                com.sec.internal.log.IMSLog.d(r0, r1)
                int[] r0 = com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler.AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType
                int r1 = r4.ordinal()
                r0 = r0[r1]
                r1 = 9
                if (r0 == r1) goto L_0x0038
                r1 = 10
                if (r0 == r1) goto L_0x0030
                r0 = 0
                goto L_0x0040
            L_0x0030:
                com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler.this
                com.sec.internal.helper.State r1 = r0.mSubscribedState
                r0.transitionTo(r1)
                goto L_0x003f
            L_0x0038:
                com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler r0 = com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler.this
                com.sec.internal.helper.State r1 = r0.mDefaultState
                r0.transitionTo(r1)
            L_0x003f:
                r0 = 1
            L_0x0040:
                if (r0 == 0) goto L_0x0058
                com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler r3 = com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler.this
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "SubscribingState, Handled : "
                r1.append(r2)
                r1.append(r4)
                java.lang.String r4 = r1.toString()
                r3.log(r4)
            L_0x0058:
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nms.SubscriptionChannelScheduler.SubscribingState.processMessage(android.os.Message):boolean");
        }

        public void exit() {
            SubscriptionChannelScheduler.this.log("SubscribingState, exit");
        }
    }

    private class SubscribedState extends State {
        private SubscribedState() {
        }

        public void enter() {
            SubscriptionChannelScheduler.this.log("SubscribedState, enter");
        }

        public boolean processMessage(Message message) {
            OMASyncEventType InitEvent = SubscriptionChannelScheduler.this.InitEvent(message);
            IMSLog.d(SubscriptionChannelScheduler.this.TAG, "Subscribed state processMessage:  " + InitEvent);
            boolean z = false;
            switch (AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()]) {
                case 10:
                    break;
                case 11:
                    ReSyncParam unused = SubscriptionChannelScheduler.this.mReSyncParam;
                    ReSyncParam.update(SubscriptionChannelScheduler.this.mStoreClient);
                    if (!TextUtils.isEmpty(SubscriptionChannelScheduler.this.mReSyncParam.getChannelResURL())) {
                        CmsHttpController httpController = SubscriptionChannelScheduler.this.mStoreClient.getHttpController();
                        SubscriptionChannelScheduler subscriptionChannelScheduler = SubscriptionChannelScheduler.this;
                        String restartToken = subscriptionChannelScheduler.mReSyncParam.getRestartToken();
                        String channelResURL = SubscriptionChannelScheduler.this.mReSyncParam.getChannelResURL();
                        SubscriptionChannelScheduler subscriptionChannelScheduler2 = SubscriptionChannelScheduler.this;
                        httpController.execute(new CloudMessageUpdateSubscriptionChannel(subscriptionChannelScheduler, restartToken, channelResURL, subscriptionChannelScheduler2, subscriptionChannelScheduler2.mStoreClient));
                        break;
                    }
                    break;
                case 12:
                    String str = (String) message.obj;
                    IMSLog.s(SubscriptionChannelScheduler.this.TAG, "large data polling channelUrl:" + str);
                    SubscriptionChannelScheduler subscriptionChannelScheduler3 = SubscriptionChannelScheduler.this;
                    SubscriptionChannelScheduler.this.mStoreClient.getHttpController().execute(new McsCreateLargeDataPolling(subscriptionChannelScheduler3, subscriptionChannelScheduler3, str, subscriptionChannelScheduler3.mStoreClient));
                    SubscriptionChannelScheduler subscriptionChannelScheduler4 = SubscriptionChannelScheduler.this;
                    subscriptionChannelScheduler4.transitionTo(subscriptionChannelScheduler4.mLargePollingState);
                    break;
                case 13:
                    if (!McsNotificationListContainer.getInstance(SubscriptionChannelScheduler.this.mStoreClient.getClientID()).isEmpty()) {
                        SubscriptionChannelScheduler subscriptionChannelScheduler5 = SubscriptionChannelScheduler.this;
                        OMASyncEventType oMASyncEventType = OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST;
                        if (!subscriptionChannelScheduler5.hasMessages(oMASyncEventType.getId())) {
                            ReSyncParam unused2 = SubscriptionChannelScheduler.this.mReSyncParam;
                            ReSyncParam.update(SubscriptionChannelScheduler.this.mStoreClient);
                            if (!TextUtils.isEmpty(SubscriptionChannelScheduler.this.mReSyncParam.getChannelResURL())) {
                                CmsHttpController httpController2 = SubscriptionChannelScheduler.this.mStoreClient.getHttpController();
                                SubscriptionChannelScheduler subscriptionChannelScheduler6 = SubscriptionChannelScheduler.this;
                                String restartToken2 = subscriptionChannelScheduler6.mReSyncParam.getRestartToken();
                                String channelResURL2 = SubscriptionChannelScheduler.this.mReSyncParam.getChannelResURL();
                                SubscriptionChannelScheduler subscriptionChannelScheduler7 = SubscriptionChannelScheduler.this;
                                httpController2.execute(new CloudMessageUpdateSubscriptionChannel(subscriptionChannelScheduler6, restartToken2, channelResURL2, subscriptionChannelScheduler7, subscriptionChannelScheduler7.mStoreClient));
                                SubscriptionChannelScheduler subscriptionChannelScheduler8 = SubscriptionChannelScheduler.this;
                                EventLogHelper.add(subscriptionChannelScheduler8.TAG, subscriptionChannelScheduler8.mStoreClient.getClientID(), " UPDATE_SUBSCRIPTION_CHANNEL_DELAY restartToken " + SubscriptionChannelScheduler.this.mReSyncParam.getRestartToken());
                                McsNotificationListContainer.getInstance(SubscriptionChannelScheduler.this.mStoreClient.getClientID()).clear(SubscriptionChannelScheduler.this.mStoreClient.getClientID());
                                SubscriptionChannelScheduler.this.isDelaySubscriptionUpdateInProgress = true;
                                break;
                            }
                        } else {
                            SubscriptionChannelScheduler.this.removeMessages(oMASyncEventType.getId());
                            break;
                        }
                    }
                    break;
                case 14:
                    SubscriptionChannelScheduler.this.isDelaySubscriptionUpdateInProgress = false;
                    break;
            }
            z = true;
            if (z) {
                SubscriptionChannelScheduler.this.log("SubscribedState, Handled : " + InitEvent);
            }
            return z;
        }

        public void exit() {
            SubscriptionChannelScheduler.this.log("SubscribedState, exit");
        }
    }

    private class LargePollingState extends State {
        private LargePollingState() {
        }

        public void enter() {
            Log.i(SubscriptionChannelScheduler.this.TAG, "LargePollingState State Enter");
        }

        public boolean processMessage(Message message) {
            boolean z;
            OMASyncEventType InitEvent = SubscriptionChannelScheduler.this.InitEvent(message);
            String str = SubscriptionChannelScheduler.this.TAG;
            IMSLog.d(str, "LargePolling state processMessage:  " + InitEvent);
            if (AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[InitEvent.ordinal()] != 15) {
                z = false;
            } else {
                SubscriptionChannelScheduler subscriptionChannelScheduler = SubscriptionChannelScheduler.this;
                subscriptionChannelScheduler.transitionTo(subscriptionChannelScheduler.mSubscribedState);
                z = true;
            }
            String str2 = SubscriptionChannelScheduler.this.TAG;
            Log.i(str2, "LargePollingState processMessage : " + InitEvent + " " + z);
            return z;
        }

        public void exit() {
            Log.i(SubscriptionChannelScheduler.this.TAG, "LargePollingState State Exit");
        }
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
        gotoHandlerEvent(i, obj);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
        gotoHandlerEventOnFailure(iHttpAPICommonInterface);
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        gotoHandlerEventOnFailure(iHttpAPICommonInterface);
    }

    private void gotoHandlerEventOnFailure(IHttpAPICommonInterface iHttpAPICommonInterface) {
        String str = this.TAG;
        IMSLog.i(str, "gotoHandlerEventOnFailure isRetryEnabled: " + this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled());
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, iHttpAPICommonInterface, -1);
        }
    }

    private void gotoHandlerEvent(int i, Object obj) {
        if (obj != null) {
            sendMessage(obtainMessage(i, obj));
        } else {
            sendMessage(i);
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
        String str2 = this.TAG;
        IMSLog.i(str2, " OnOverRequest : request " + simpleName + "  error code " + str + "  retryAfter " + i);
        updateDelayRetryRequest(simpleName, (long) i);
    }

    public void updateDelayRetryRequest(String str, long j) {
        PendingIntent pendingIntent = this.mRetryIntent;
        if (pendingIntent != null) {
            AlarmTimer.stop(this.mContext, pendingIntent);
            this.mRetryIntent = null;
        }
        Intent intent = new Intent(INTENT_ACTION_RETRY_SUBSCRIPTION_FAILED_API);
        intent.setPackage(this.mContext.getPackageName());
        intent.putExtra("phoneId", this.mPhoneId);
        intent.putExtra("apiName", str);
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        this.mRetryIntent = broadcast;
        AlarmTimer.start(this.mContext, broadcast, j, false);
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

    public boolean update(int i) {
        if (i == OMASyncEventType.REMOVE_UPDATE_SUBSCRIPTION_CHANNEL.getId()) {
            removeMessages(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY.getId());
            return true;
        }
        sendMessage(obtainMessage(i));
        return true;
    }

    public boolean updateMessage(Message message) {
        sendMessage(message);
        return true;
    }

    public boolean updateDelay(int i, long j) {
        sendMessageDelayed(obtainMessage(i), j);
        return true;
    }

    /* access modifiers changed from: private */
    public void checkAndUpdateSubscriptionChannel() {
        ReSyncParam.update(this.mStoreClient);
        if (this.mStoreClient.getPrerenceManager().getOMASubscriptionTime() == 0) {
            sendMessage(OMASyncEventType.CREATE_SUBSCRIPTION_CHANNEL.getId());
        } else if (this.mSchedulerHelper.isSubscriptionChannelGoingExpired(this.mStoreClient)) {
            sendMessage(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId());
        }
    }
}
