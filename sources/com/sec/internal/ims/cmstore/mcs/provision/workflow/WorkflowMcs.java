package com.sec.internal.ims.cmstore.mcs.provision.workflow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.sec.ims.ICentralMsgStoreServiceListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.NetAPIWorkingStatusControllerMcs;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestApproveSd;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestDeleteAccount;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestGetAccount;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestGetListOfSD;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestGetSD;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestGetUser;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestMCSToken;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestOtpSms;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestRemoveSd;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestUpdateAccount;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestUserAuthentication;
import com.sec.internal.ims.cmstore.mcs.provision.cloudrequest.RequestUserRegistration;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IMcsFcmPushNotificationListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.McsLargePollingNotification;
import com.sec.internal.omanetapi.nms.data.NmsEventList;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class WorkflowMcs extends StateMachine implements IAPICallFlowListener {
    private static final int API_FAILED = 2;
    private static final int API_SUCCEED = 1;
    private static final int APPROVE_SD = 15;
    private static final int AUTHENTICATION = 12;
    protected static final long A_DAY = 86400;
    private static final int DEFAULT = 0;
    protected static final int DEFAULT_OTP_VALIDITY = 60;
    private static final int DELETE_ACCOUNT = 23;
    private static final int GET_ACCOUNT = 21;
    private static final int GET_LIST_OF_SD = 17;
    private static final int GET_SD = 18;
    private static final int GET_USER = 11;
    protected static String OASIS_OTP_PREFIX = "-auth, otp=";
    protected static String OASIS_RECOVERY_PREFIX = "-recovery";
    private static final int REGISTRATION = 13;
    private static final int REMOVE_SD = 16;
    private static final int REQUEST_OTP = 19;
    private static final int REQUEST_OTP_TIMEOUT = 20;
    private static final int RESET_STATE = 3;
    private static final int START_PROVISION = 10;
    private static final int TOKEN = 14;
    private static final int UPDATE_ACCOUNT = 22;
    public String LOG_TAG = WorkflowMcs.class.getSimpleName();
    protected PendingIntent mAccessTokenValidityIntent = null;
    /* access modifiers changed from: private */
    public final Uri mAliasUri;
    private boolean mChangedToSamsungMessage = false;
    protected final Handler mClientHandler;
    /* access modifiers changed from: private */
    public String mConsentContext = null;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public final IAPICallFlowListener mListener;
    protected final Object mLock = new Object();
    protected RemoteCallbackList<ICentralMsgStoreServiceListener> mMcsProvisioningListener;
    protected boolean mNeedInternalRegistration = false;
    /* access modifiers changed from: private */
    public final NetAPIWorkingStatusControllerMcs mNetAPIWorkingController;
    /* access modifiers changed from: private */
    public String mOtp;
    protected PendingIntent mOtpTimeoutIntent = null;
    /* access modifiers changed from: private */
    public Set<Integer> mPendingRequests = new HashSet();
    protected int mPhoneId = 0;
    /* access modifiers changed from: private */
    public final CloudMessagePreferenceManager mPreferenceManager;
    /* access modifiers changed from: private */
    public State mProvisionedState = new ProvisionedState();
    /* access modifiers changed from: private */
    public State mProvisioningState = new ProvisioningState();
    RCSContentObserver mRCSContentObserver;
    protected PendingIntent mRefreshTokenValidityIntent = null;
    /* access modifiers changed from: private */
    public State mRegisteredState = new RegisteredState();
    /* access modifiers changed from: private */
    public State mRegisteringState = new RegisteringState();
    protected PendingIntent mRegistrationCodeValidityIntent = null;
    /* access modifiers changed from: private */
    public String mRequestType = McsConstants.Auth.TYPE_MOBILE_IP;
    private final ISimManager mSimManager;
    protected SmsReceiver mSmsReceiver;
    /* access modifiers changed from: private */
    public final MessageStoreClient mStoreClient;
    /* access modifiers changed from: private */
    public boolean mWaitOtp;

    public boolean isBearerAuthRequest(int i) {
        return i == 15 || i == 16 || i == 17 || i == 18 || i == 21 || i == 22 || i == 23;
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
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

    public void onGoToEvent(int i, Object obj) {
    }

    public void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
    }

    public int translateRequestToNotify(int i) {
        switch (i) {
            case 15:
                return 3;
            case 16:
                return 4;
            case 17:
                return 6;
            case 18:
                return 5;
            case 21:
            case 22:
                return 7;
            case 23:
                return 2;
            default:
                return 0;
        }
    }

    public WorkflowMcs(Looper looper, MessageStoreClient messageStoreClient, Handler handler, NetAPIWorkingStatusControllerMcs netAPIWorkingStatusControllerMcs) {
        super("WorkflowMcs[" + messageStoreClient.getClientID() + "]", looper);
        this.mSimManager = messageStoreClient.getSimManager();
        this.mStoreClient = messageStoreClient;
        this.mPhoneId = messageStoreClient.getClientID();
        Context context = messageStoreClient.getContext();
        this.mContext = context;
        this.mListener = this;
        this.mClientHandler = handler;
        this.mPreferenceManager = messageStoreClient.getPrerenceManager();
        this.mNetAPIWorkingController = netAPIWorkingStatusControllerMcs;
        this.mMcsProvisioningListener = messageStoreClient.getMcsProvisioningListener();
        this.mRCSContentObserver = new RCSContentObserver(getHandler());
        Uri.Builder buildUpon = McsConstants.Uris.RCS_USER_ALIAS_URI.buildUpon();
        this.mAliasUri = buildUpon.fragment("simslot" + this.mPhoneId).build();
        initStates();
        registerSmsReceiver();
        registerSyncStatusListener();
        registerContentObservers();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(McsConstants.McsActions.INTENT_ACCESS_TOKEN_VALIDITY_TIMEOUT);
        intentFilter.addAction(McsConstants.McsActions.INTENT_REFRESH_TOKEN_VALIDITY_TIMEOUT);
        intentFilter.addAction(McsConstants.McsActions.INTENT_REGISTRATION_CODE_VALIDITY_TIMEOUT);
        intentFilter.addAction(McsConstants.McsActions.INTENT_OTP_RESPONSE_TIMEOUT);
        context.registerReceiver(new BroadcastReceiver() {
            /* JADX WARNING: Code restructure failed: missing block: B:21:0x00d9, code lost:
                r6 = r7.getIntExtra(com.sec.internal.constants.ims.os.PhoneConstants.PHONE_KEY, 0);
                r7 = r5.this$0;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(android.content.Context r6, android.content.Intent r7) {
                /*
                    r5 = this;
                    java.lang.String r6 = r7.getAction()
                    java.lang.String r0 = "com.sec.imsservice.cmstore.mcs.action.ACCESS_TOKEN_VALIDITY_TIMEOUT"
                    boolean r6 = r0.equals(r6)
                    r0 = -1878851580(0xffffffff90030004, float:-2.5835207E-29)
                    java.lang.String r1 = "phone"
                    r2 = 0
                    r3 = 0
                    if (r6 == 0) goto L_0x0055
                    int r6 = r7.getIntExtra(r1, r3)
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    int r1 = r7.mPhoneId
                    if (r1 != r6) goto L_0x00fd
                    java.lang.String r6 = r7.LOG_TAG
                    java.lang.String r7 = "onReceive: access token is expired"
                    com.sec.internal.ims.cmstore.helper.EventLogHelper.infoLogAndAdd(r6, r1, r7)
                    java.lang.StringBuilder r6 = new java.lang.StringBuilder
                    r6.<init>()
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    int r7 = r7.mPhoneId
                    r6.append(r7)
                    java.lang.String r7 = ",PV:ATKN"
                    r6.append(r7)
                    java.lang.String r6 = r6.toString()
                    com.sec.internal.log.IMSLog.c(r0, r6)
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r6 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    r6.mAccessTokenValidityIntent = r2
                    com.sec.internal.helper.IState r6 = r6.getCurrentState()
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    com.sec.internal.helper.State r7 = r7.mProvisioningState
                    if (r6 == r7) goto L_0x00fd
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r5 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    r6 = 14
                    r5.sendMessage((int) r6)
                    goto L_0x00fd
                L_0x0055:
                    java.lang.String r6 = "com.sec.imsservice.cmstore.mcs.action.REFRESH_TOKEN_VALIDITY_TIMEOUT"
                    java.lang.String r4 = r7.getAction()
                    boolean r6 = r6.equals(r4)
                    if (r6 == 0) goto L_0x00a2
                    int r6 = r7.getIntExtra(r1, r3)
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    int r1 = r7.mPhoneId
                    if (r1 != r6) goto L_0x00fd
                    java.lang.String r6 = r7.LOG_TAG
                    java.lang.String r7 = "onReceive: refresh token is expired"
                    com.sec.internal.ims.cmstore.helper.EventLogHelper.infoLogAndAdd(r6, r1, r7)
                    java.lang.StringBuilder r6 = new java.lang.StringBuilder
                    r6.<init>()
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    int r7 = r7.mPhoneId
                    r6.append(r7)
                    java.lang.String r7 = ",PV:RTKN"
                    r6.append(r7)
                    java.lang.String r6 = r6.toString()
                    com.sec.internal.log.IMSLog.c(r0, r6)
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r6 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    r6.mRefreshTokenValidityIntent = r2
                    com.sec.internal.helper.IState r6 = r6.getCurrentState()
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    com.sec.internal.helper.State r7 = r7.mProvisioningState
                    if (r6 == r7) goto L_0x00fd
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r5 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    r6 = 12
                    r5.sendMessage((int) r6)
                    goto L_0x00fd
                L_0x00a2:
                    java.lang.String r6 = "com.sec.imsservice.cmstore.mcs.action.REGISTRATION_CODE_VALIDITY_TIMEOUT"
                    java.lang.String r0 = r7.getAction()
                    boolean r6 = r6.equals(r0)
                    if (r6 == 0) goto L_0x00cd
                    int r6 = r7.getIntExtra(r1, r3)
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    int r0 = r7.mPhoneId
                    if (r0 != r6) goto L_0x00fd
                    java.lang.String r6 = r7.LOG_TAG
                    java.lang.String r7 = "onReceive: registration code is expired, remove it"
                    com.sec.internal.ims.cmstore.helper.EventLogHelper.infoLogAndAdd(r6, r0, r7)
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r5 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    r5.mRegistrationCodeValidityIntent = r2
                    com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r5 = r5.mPreferenceManager
                    java.lang.String r6 = ""
                    r5.saveRegCode(r6)
                    goto L_0x00fd
                L_0x00cd:
                    java.lang.String r6 = "com.sec.imsservice.cmstore.mcs.action.OTP_RESPONSE_TIMEOUT"
                    java.lang.String r0 = r7.getAction()
                    boolean r6 = r6.equals(r0)
                    if (r6 == 0) goto L_0x00fd
                    int r6 = r7.getIntExtra(r1, r3)
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    int r0 = r7.mPhoneId
                    if (r0 != r6) goto L_0x00fd
                    java.lang.String r6 = r7.LOG_TAG
                    java.lang.String r7 = "onReceive: Time out, do not receive ported OTP SMS"
                    com.sec.internal.ims.cmstore.helper.EventLogHelper.infoLogAndAdd(r6, r0, r7)
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r6 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    r6.mOtpTimeoutIntent = r2
                    r6.mOtp = r2
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r6 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    r6.mWaitOtp = r3
                    com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r5 = com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.this
                    r6 = 20
                    r5.sendMessage((int) r6)
                L_0x00fd:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
            }
        }, intentFilter);
    }

    private void registerSyncStatusListener() {
        this.mStoreClient.setMcsFcmPushNotificationListener(new IMcsFcmPushNotificationListener() {
            public void largePollingPushNotification(McsLargePollingNotification mcsLargePollingNotification) {
            }

            public void nmsEventListPushNotification(NmsEventList nmsEventList) {
            }

            public void syncBlockfilterPushNotification(String str) {
            }

            public void syncContactPushNotification(String str) {
            }

            public void syncMessagePushNotification(String str, int i) {
            }

            public void syncStatusPushNotification(String str) {
                WorkflowMcs workflowMcs = WorkflowMcs.this;
                String str2 = workflowMcs.LOG_TAG;
                int i = workflowMcs.mPhoneId;
                EventLogHelper.infoLogAndAdd(str2, i, "syncStatusPushNotification: status: " + str);
                if (McsConstants.PushMessages.VALUE_ENABLE_MCS.equals(str)) {
                    WorkflowMcs.this.mStoreClient.setProvisionStatus(false);
                    WorkflowMcs.this.requestMcsReAuthentication();
                } else if (McsConstants.PushMessages.VALUE_DISABLE_MCS.equals(str)) {
                    WorkflowMcs.this.onDeRegistrationCompleted();
                }
                if (!TextUtils.isEmpty(str)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(McsConstants.BundleData.PUSH_TYPE, McsConstants.PushMessages.TYPE_SYNC_STATUS);
                    bundle.putString(McsConstants.BundleData.KEY, "status");
                    bundle.putString("value", str);
                    WorkflowMcs.this.notifyMcsProvisionListener(8, 0, 0, bundle);
                }
            }

            public void syncConfigPushNotification(String str) {
                str.hashCode();
                if (!str.equals("A")) {
                    WorkflowMcs workflowMcs = WorkflowMcs.this;
                    String str2 = workflowMcs.LOG_TAG;
                    int i = workflowMcs.mPhoneId;
                    IMSLog.i(str2, i, "unknown configtype occur:" + str);
                    return;
                }
                WorkflowMcs.this.updateMcsAlias(true);
            }
        });
    }

    private void initStates() {
        addState(this.mDefaultState);
        addState(this.mRegisteringState, this.mDefaultState);
        addState(this.mRegisteredState, this.mRegisteringState);
        addState(this.mProvisioningState, this.mRegisteringState);
        addState(this.mProvisionedState, this.mRegisteringState);
        setInitialState(this.mDefaultState);
        start();
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            WorkflowMcs.this.log("DefaultState, enter");
        }

        public boolean processMessage(Message message) {
            WorkflowMcs workflowMcs = WorkflowMcs.this;
            String str = workflowMcs.LOG_TAG;
            int i = workflowMcs.mPhoneId;
            IMSLog.i(str, i, "DefaultState, processMessage: " + message.what);
            int i2 = message.what;
            if (i2 == 1) {
                WorkflowMcs.this.handleSucceededEvent(message.arg1, message.obj);
                return true;
            } else if (i2 == 2) {
                WorkflowMcs.this.handleFailedEvent(message.arg1, message.arg2);
                return true;
            } else if (i2 != 3) {
                if (i2 == 10) {
                    int mcsUser = WorkflowMcs.this.mPreferenceManager.getMcsUser();
                    WorkflowMcs workflowMcs2 = WorkflowMcs.this;
                    String str2 = workflowMcs2.LOG_TAG;
                    int i3 = workflowMcs2.mPhoneId;
                    IMSLog.i(str2, i3, "McsUser : " + mcsUser);
                    if (mcsUser == -1) {
                        WorkflowMcs.this.sendMessage(11);
                        return true;
                    } else if ((mcsUser == 0 || WorkflowMcs.this.mNeedInternalRegistration) && message.obj != null) {
                        WorkflowMcs.this.mRequestType = McsConstants.Auth.TYPE_MOBILE_IP;
                        WorkflowMcs workflowMcs3 = WorkflowMcs.this;
                        workflowMcs3.transitionTo(workflowMcs3.mRegisteringState);
                        if (TextUtils.isEmpty(WorkflowMcs.this.mPreferenceManager.getRegCode())) {
                            WorkflowMcs.this.sendMessage(12, message.obj);
                            return true;
                        }
                        WorkflowMcs.this.sendMessage(13, message.obj);
                        return true;
                    } else if (mcsUser != 1) {
                        return true;
                    } else {
                        if (WorkflowMcs.this.isValidAccessToken()) {
                            WorkflowMcs workflowMcs4 = WorkflowMcs.this;
                            IMSLog.i(workflowMcs4.LOG_TAG, workflowMcs4.mPhoneId, "It's already MCS user");
                            WorkflowMcs.this.mStoreClient.setProvisionStatus(true);
                            WorkflowMcs workflowMcs5 = WorkflowMcs.this;
                            workflowMcs5.transitionTo(workflowMcs5.mProvisionedState);
                            WorkflowMcs.this.notifyMcsProvisionListener(1, 100, 3, (Object) null);
                            return true;
                        }
                        WorkflowMcs workflowMcs6 = WorkflowMcs.this;
                        workflowMcs6.transitionTo(workflowMcs6.mRegisteredState);
                        WorkflowMcs.this.sendMessage(10);
                        return true;
                    }
                } else if (i2 != 11) {
                    return false;
                } else {
                    if (!TextUtils.isEmpty(WorkflowMcs.this.getE164Msisdn())) {
                        WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestGetUser(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, WorkflowMcs.this.getE164Msisdn()));
                        return true;
                    }
                    WorkflowMcs workflowMcs7 = WorkflowMcs.this;
                    IMSLog.i(workflowMcs7.LOG_TAG, workflowMcs7.mPhoneId, "msisdn is null");
                    WorkflowMcs workflowMcs8 = WorkflowMcs.this;
                    workflowMcs8.mClientHandler.sendMessageDelayed(workflowMcs8.obtainMessage(0), 1000);
                    return true;
                }
            } else if (WorkflowMcs.this.mPreferenceManager.getMcsUser() != 1 || WorkflowMcs.this.mStoreClient.getSimManager().getSimState() == 1) {
                WorkflowMcs workflowMcs9 = WorkflowMcs.this;
                workflowMcs9.transitionTo(workflowMcs9.mDefaultState);
                return true;
            } else {
                WorkflowMcs workflowMcs10 = WorkflowMcs.this;
                workflowMcs10.transitionTo(workflowMcs10.mRegisteredState);
                return true;
            }
        }

        public void exit() {
            WorkflowMcs.this.log("DefaultState, exit");
        }
    }

    private class RegisteringState extends State {
        private RegisteringState() {
        }

        public void enter() {
            WorkflowMcs.this.log("RegisteringState, enter");
        }

        public boolean processMessage(Message message) {
            WorkflowMcs workflowMcs = WorkflowMcs.this;
            String str = workflowMcs.LOG_TAG;
            int i = workflowMcs.mPhoneId;
            IMSLog.i(str, i, "RegisteringState, processMessage: " + message.what);
            int i2 = message.what;
            if (i2 == 10) {
                return true;
            }
            if (i2 == 19) {
                WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestOtpSms(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, WorkflowMcs.this.getE164Msisdn(), WorkflowMcs.this.buildDeviceInfo()));
                WorkflowMcs.this.mWaitOtp = true;
                WorkflowMcs.this.startOtpTimer(60);
                return true;
            } else if (i2 != 20) {
                switch (i2) {
                    case 12:
                        WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestUserAuthentication(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, WorkflowMcs.this.getE164Msisdn(), WorkflowMcs.this.buildDeviceInfo(), WorkflowMcs.this.mOtp, WorkflowMcs.this.mRequestType, WorkflowMcs.this.mNetAPIWorkingController.getMobileIp(), Boolean.valueOf(WorkflowMcs.this.mNeedInternalRegistration), (String) message.obj));
                        return true;
                    case 13:
                        WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestUserRegistration(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, WorkflowMcs.this.getE164Msisdn(), (String) message.obj));
                        return true;
                    case 14:
                        WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestMCSToken(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, WorkflowMcs.this.isValidRefreshToken(), WorkflowMcs.this.buildDeviceInfo()));
                        return true;
                    default:
                        return false;
                }
            } else {
                WorkflowMcs.this.handleFailedEvent(20, 0);
                return true;
            }
        }

        public void exit() {
            WorkflowMcs.this.log("RegisteringState, exit");
        }
    }

    private class RegisteredState extends State {
        private RegisteredState() {
        }

        public void enter() {
            WorkflowMcs.this.log("RegisteredState, enter");
        }

        public boolean processMessage(Message message) {
            WorkflowMcs workflowMcs = WorkflowMcs.this;
            String str = workflowMcs.LOG_TAG;
            int i = workflowMcs.mPhoneId;
            IMSLog.i(str, i, "RegisteredState, processMessage: " + message.what);
            switch (message.what) {
                case 10:
                    int mcsUser = WorkflowMcs.this.mPreferenceManager.getMcsUser();
                    WorkflowMcs workflowMcs2 = WorkflowMcs.this;
                    String str2 = workflowMcs2.LOG_TAG;
                    int i2 = workflowMcs2.mPhoneId;
                    IMSLog.i(str2, i2, "McsUser : " + mcsUser);
                    if (mcsUser != 1) {
                        WorkflowMcs workflowMcs3 = WorkflowMcs.this;
                        workflowMcs3.transitionTo(workflowMcs3.mRegisteringState);
                        if (Util.isRegistrationCodeInvalid(WorkflowMcs.this.mPreferenceManager.getRegCode())) {
                            WorkflowMcs workflowMcs4 = WorkflowMcs.this;
                            workflowMcs4.sendMessage(12, (Object) workflowMcs4.mConsentContext);
                            return true;
                        }
                        WorkflowMcs.this.sendMessage(14);
                        return true;
                    } else if (!WorkflowMcs.this.isValidAccessToken()) {
                        WorkflowMcs workflowMcs5 = WorkflowMcs.this;
                        workflowMcs5.transitionTo(workflowMcs5.mProvisioningState);
                        if (WorkflowMcs.this.isValidRefreshToken()) {
                            WorkflowMcs.this.sendMessage(14);
                            return true;
                        }
                        WorkflowMcs.this.sendMessage(12);
                        return true;
                    } else {
                        WorkflowMcs.this.mStoreClient.setProvisionStatus(true);
                        WorkflowMcs workflowMcs6 = WorkflowMcs.this;
                        workflowMcs6.transitionTo(workflowMcs6.mProvisionedState);
                        WorkflowMcs.this.notifyMcsProvisionListener(1, 100, 3, (Object) null);
                        return true;
                    }
                case 12:
                case 14:
                    WorkflowMcs workflowMcs7 = WorkflowMcs.this;
                    workflowMcs7.transitionTo(workflowMcs7.mProvisioningState);
                    WorkflowMcs.this.sendMessage(message.what);
                    return true;
                case 15:
                case 16:
                case 17:
                case 18:
                case 21:
                case 22:
                case 23:
                    if (!CmsUtil.isDefaultMessageAppInUse(WorkflowMcs.this.mContext)) {
                        return true;
                    }
                    WorkflowMcs.this.mPendingRequests.add(Integer.valueOf(message.what));
                    WorkflowMcs.this.sendMessage(10);
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            WorkflowMcs.this.log("RegisteredState, exit");
        }
    }

    private class ProvisioningState extends State {
        private ProvisioningState() {
        }

        public void enter() {
            WorkflowMcs.this.log("ProvisioningState, enter");
        }

        public boolean processMessage(Message message) {
            String str = WorkflowMcs.this.LOG_TAG;
            IMSLog.i(str, "ProvisioningState, processMessage: " + message.what);
            switch (message.what) {
                case 10:
                    break;
                case 12:
                    WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestUserAuthentication(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, WorkflowMcs.this.getE164Msisdn(), WorkflowMcs.this.buildDeviceInfo(), WorkflowMcs.this.mOtp, WorkflowMcs.this.mRequestType, WorkflowMcs.this.mNetAPIWorkingController.getMobileIp(), Boolean.FALSE, (String) null));
                    break;
                case 15:
                case 16:
                case 17:
                case 18:
                case 21:
                case 22:
                case 23:
                    if (CmsUtil.isDefaultMessageAppInUse(WorkflowMcs.this.mContext)) {
                        WorkflowMcs.this.mPendingRequests.add(Integer.valueOf(message.what));
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WorkflowMcs.this.log("ProvisioningState, exit");
        }
    }

    private class ProvisionedState extends State {
        private ProvisionedState() {
        }

        public void enter() {
            WorkflowMcs.this.log("ProvisionedState, enter");
        }

        public boolean processMessage(Message message) {
            WorkflowMcs workflowMcs = WorkflowMcs.this;
            String str = workflowMcs.LOG_TAG;
            int i = workflowMcs.mPhoneId;
            IMSLog.i(str, i, "ProvisionedState, processMessage: " + message.what);
            switch (message.what) {
                case 12:
                case 14:
                    WorkflowMcs workflowMcs2 = WorkflowMcs.this;
                    workflowMcs2.transitionTo(workflowMcs2.mProvisioningState);
                    WorkflowMcs.this.sendMessage(message.what);
                    break;
                case 15:
                    String str2 = (String) message.obj;
                    if (!TextUtils.isEmpty(str2)) {
                        String stringPayloadFromToken = Util.getStringPayloadFromToken(str2, "iss");
                        if (!TextUtils.isEmpty(stringPayloadFromToken) && stringPayloadFromToken.contains(McsConstants.Protocol.SENDER_SD)) {
                            WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestApproveSd(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, str2));
                            break;
                        }
                    }
                    WorkflowMcs.this.notifyMcsProvisionListener(3, 200, 10, (Object) null);
                    break;
                case 16:
                    WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestRemoveSd(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, (String) message.obj));
                    break;
                case 17:
                    WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestGetListOfSD(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient));
                    break;
                case 18:
                    WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestGetSD(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, (String) message.obj));
                    break;
                case 21:
                    WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestGetAccount(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, Util.encodeRFC3986(WorkflowMcs.this.getE164Msisdn())));
                    break;
                case 22:
                    WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestUpdateAccount(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, Util.encodeRFC3986(WorkflowMcs.this.getE164Msisdn()), message));
                    break;
                case 23:
                    WorkflowMcs.this.mStoreClient.getHttpController().execute(new RequestDeleteAccount(WorkflowMcs.this.mListener, WorkflowMcs.this.mStoreClient, Util.encodeRFC3986(WorkflowMcs.this.getE164Msisdn())));
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WorkflowMcs.this.log("ProvisionedState, exit");
        }
    }

    /* access modifiers changed from: private */
    public String getE164Msisdn() {
        String formatNumberToE164 = PhoneNumberUtils.formatNumberToE164(this.mPreferenceManager.getUserCtn(), Util.getSimCountryCode(this.mContext, this.mPhoneId));
        return formatNumberToE164 != null ? formatNumberToE164 : "";
    }

    /* access modifiers changed from: private */
    public JSONObject buildDeviceInfo() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("client_id", CmsUtil.getMcsClientId(this.mContext));
            jSONObject.put("device_id", this.mPreferenceManager.getDeviceId());
            jSONObject.put("device_name", McsConstants.DeviceInfoValue.DEVICE_NAME);
            jSONObject.put(McsConstants.DeviceInfo.CLIENT_IP, this.mNetAPIWorkingController.getMobileIp());
            jSONObject.put("client_vendor", McsConstants.DeviceInfoValue.CLIENT_VENDOR);
            jSONObject.put("mno", getMnoName());
            jSONObject.put(McsConstants.DeviceInfo.OS_TYPE, McsConstants.DeviceInfoValue.OS_TYPE);
            jSONObject.put(McsConstants.DeviceInfo.OS_VERSION, McsConstants.DeviceInfoValue.OS_VERSION);
            jSONObject.put(McsConstants.DeviceInfo.DEVICE_KIND, McsConstants.DeviceInfoValue.DEVICE_KIND);
            jSONObject.put(McsConstants.DeviceInfo.FIRMWARE_VERSION, McsConstants.DeviceInfoValue.FIRMWARE_VERSION);
            jSONObject.put(McsConstants.DeviceInfo.SERVICE_VERSION, "1.0");
            jSONObject.put("client_version", CmsUtil.getSmAppVersion(this.mContext));
            jSONObject.put("native_info", buildNativeInfo());
            return jSONObject;
        } catch (JSONException e) {
            IMSLog.e(this.LOG_TAG, e.getMessage());
            return null;
        }
    }

    private JSONObject buildNativeInfo() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("imsi", this.mSimManager.getImsi());
            jSONObject.put(McsConstants.NativeInfo.IMEI, this.mPreferenceManager.getDeviceId());
            jSONObject.put(McsConstants.NativeInfo.SMS_PORT, McsConstants.DeviceInfoValue.SMS_PORT);
            jSONObject.put("default_sms_app", ConfigUtil.isSecDmaPackageInuse(this.mContext, this.mPhoneId) ? 1 : 2);
            return jSONObject;
        } catch (JSONException e) {
            IMSLog.e(this.LOG_TAG, e.getMessage());
            return null;
        }
    }

    private String getMnoName() {
        Mno simMno = this.mSimManager.getSimMno();
        if (simMno == Mno.SKT) {
            return "SKT";
        }
        if (simMno == Mno.LGU) {
            return "LGU";
        }
        return simMno == Mno.KT ? "KT" : "";
    }

    public void clearWorkflow() {
        sendMessage(3);
    }

    public void startProvisioning(String str) {
        EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "startProvisioning");
        registerSmsReceiver();
        this.mConsentContext = str;
        sendMessage(obtainMessage(10, (Object) str));
    }

    public void manageSd(int i, String str) {
        String str2 = this.LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "manageSd type : " + i);
        if (i == 1) {
            sendMessage(15, (Object) str);
        } else if (i == 2) {
            sendMessage(16, (Object) str);
        }
    }

    public void getSd(Boolean bool, String str) {
        String str2 = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "getSd type " + bool);
        if (bool.booleanValue()) {
            sendMessage(17);
        } else {
            sendMessage(18, (Object) str);
        }
    }

    public void updateAccountInfo(String str) {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "updateAccountInfo consent");
        this.mConsentContext = str;
        Bundle bundle = new Bundle();
        bundle.putBoolean(McsConstants.Auth.IS_CHANGED_CONSENT, true);
        bundle.putString(McsConstants.Auth.CONSENT_CONTEXT, str);
        sendMessage(22, (Object) bundle);
    }

    private void updatePendingAccountInfo() {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "updatePendingAccountInfo");
        Bundle bundle = new Bundle();
        if (this.mConsentContext != null) {
            bundle.putBoolean(McsConstants.Auth.IS_CHANGED_CONSENT, true);
            bundle.putString(McsConstants.Auth.CONSENT_CONTEXT, this.mConsentContext);
        }
        bundle.putBoolean(McsConstants.Auth.IS_CHANGED_ALIAS, true);
        bundle.putString("alias", this.mPreferenceManager.getMcsAlias());
        sendMessage(22, (Object) bundle);
    }

    public void getAccount() {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "getAccount");
        sendMessage(21);
    }

    public void disableMCS() {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "disableMCS");
        sendMessage(23);
    }

    public void onDefaultSmsPackageChanged() {
        if (!CmsUtil.isDefaultMessageAppInUse(this.mContext)) {
            sendMessage(3);
            this.mChangedToSamsungMessage = false;
            return;
        }
        this.mChangedToSamsungMessage = true;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:20:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateMcsAlias(boolean r9) {
        /*
            r8 = this;
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r0 = r8.mPreferenceManager
            java.lang.String r0 = r0.getMcsAlias()
            android.content.Context r1 = r8.mContext
            android.content.ContentResolver r2 = r1.getContentResolver()
            android.net.Uri r3 = r8.mAliasUri
            r4 = 0
            r5 = 0
            r6 = 0
            r7 = 0
            android.database.Cursor r1 = r2.query(r3, r4, r5, r6, r7)
            if (r1 == 0) goto L_0x0047
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x003d }
            if (r2 == 0) goto L_0x0047
            r2 = 0
            java.lang.String r2 = r1.getString(r2)     // Catch:{ all -> 0x003d }
            java.lang.String r3 = r8.LOG_TAG     // Catch:{ all -> 0x003d }
            int r4 = r8.mPhoneId     // Catch:{ all -> 0x003d }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x003d }
            r5.<init>()     // Catch:{ all -> 0x003d }
            java.lang.String r6 = "updateMcsAlias: mUserAlias "
            r5.append(r6)     // Catch:{ all -> 0x003d }
            r5.append(r2)     // Catch:{ all -> 0x003d }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x003d }
            com.sec.internal.log.IMSLog.i(r3, r4, r5)     // Catch:{ all -> 0x003d }
            goto L_0x0048
        L_0x003d:
            r8 = move-exception
            r1.close()     // Catch:{ all -> 0x0042 }
            goto L_0x0046
        L_0x0042:
            r9 = move-exception
            r8.addSuppressed(r9)
        L_0x0046:
            throw r8
        L_0x0047:
            r2 = r0
        L_0x0048:
            if (r1 == 0) goto L_0x004d
            r1.close()
        L_0x004d:
            if (r9 != 0) goto L_0x0055
            boolean r9 = r2.equals(r0)
            if (r9 != 0) goto L_0x0067
        L_0x0055:
            java.lang.String r9 = r8.LOG_TAG
            int r0 = r8.mPhoneId
            java.lang.String r1 = "updateMcsAlias: RCS user alias is changed, update Mcs Alias"
            com.sec.internal.ims.cmstore.helper.EventLogHelper.infoLogAndAdd(r9, r0, r1)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r9 = r8.mPreferenceManager
            r9.saveMcsAlias(r2)
            r8.notifyMcsAlias(r2)
        L_0x0067:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs.updateMcsAlias(boolean):void");
    }

    /* access modifiers changed from: protected */
    public void notifyMcsAlias(String str) {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "updateAccountInfo alias");
        Bundle bundle = new Bundle();
        bundle.putBoolean(McsConstants.Auth.IS_CHANGED_ALIAS, true);
        bundle.putString("alias", str);
        sendMessage(22, (Object) bundle);
    }

    public boolean isValidAccessToken() {
        long mcsAccessTokenExpireTime = this.mPreferenceManager.getMcsAccessTokenExpireTime();
        String mcsAccessToken = this.mPreferenceManager.getMcsAccessToken();
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "tokenExpireime = " + mcsAccessTokenExpireTime);
        if (!TextUtils.isEmpty(mcsAccessToken)) {
            long currentTimeMillis = mcsAccessTokenExpireTime - (System.currentTimeMillis() / 1000);
            if (currentTimeMillis - A_DAY > 0) {
                IMSLog.i(this.LOG_TAG, this.mPhoneId, "AccessToken is valid");
                setAccessTokenValidityTimer(currentTimeMillis);
                return true;
            }
        }
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "AccessToken is invalid");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isValidRefreshToken() {
        long mcsRefreshTokenExpireTime = this.mPreferenceManager.getMcsRefreshTokenExpireTime();
        String mcsRefreshToken = this.mPreferenceManager.getMcsRefreshToken();
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "tokenExpireime = " + mcsRefreshTokenExpireTime);
        if (!TextUtils.isEmpty(mcsRefreshToken)) {
            long currentTimeMillis = mcsRefreshTokenExpireTime - (System.currentTimeMillis() / 1000);
            if (currentTimeMillis - A_DAY > 0) {
                IMSLog.i(this.LOG_TAG, this.mPhoneId, "RefreshToken is valid");
                setRefreshTokenValidityTimer(currentTimeMillis);
                return true;
            }
        }
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "RefreshToken is invalid");
        return false;
    }

    public void setAccessTokenValidityTimer(long j) {
        if (this.mAccessTokenValidityIntent != null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "setAccessTokenValidityTimer: AccessTokenValidityTimer is already running. Stopping it.");
            cancelAccessTokenValidityTimer();
        }
        if (j != 0) {
            long j2 = j - A_DAY;
            if (j2 > 0) {
                Intent intent = new Intent(McsConstants.McsActions.INTENT_ACCESS_TOKEN_VALIDITY_TIMEOUT);
                intent.setPackage(this.mContext.getPackageName());
                intent.putExtra(PhoneConstants.PHONE_KEY, this.mPhoneId);
                this.mAccessTokenValidityIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
                String str = this.LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "setAccessTokenValidityTimer: start validity period timer (" + j2 + " sec)");
                AlarmTimer.start(this.mContext, this.mAccessTokenValidityIntent, j2 * 1000);
            }
        } else if (getCurrentState() != this.mProvisioningState) {
            sendMessage(14);
        }
    }

    public void setRefreshTokenValidityTimer(long j) {
        if (this.mRefreshTokenValidityIntent != null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "setRefreshTokenValidityTimer: RefreshTokenValidityTimer is already running. Stopping it.");
            cancelRefreshTokenValidityTimer();
        }
        if (j != 0) {
            long j2 = j - A_DAY;
            if (j2 > 0) {
                Intent intent = new Intent(McsConstants.McsActions.INTENT_REFRESH_TOKEN_VALIDITY_TIMEOUT);
                intent.setPackage(this.mContext.getPackageName());
                intent.putExtra(PhoneConstants.PHONE_KEY, this.mPhoneId);
                this.mRefreshTokenValidityIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
                String str = this.LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "setRefreshTokenValidityTimer: start validity period timer (" + j2 + " sec)");
                AlarmTimer.start(this.mContext, this.mRefreshTokenValidityIntent, j2 * 1000);
            }
        } else if (getCurrentState() != this.mProvisioningState) {
            sendMessage(12);
        }
    }

    public void setRegistrationCodeValidityTimer(long j) {
        if (this.mRegistrationCodeValidityIntent != null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "setRegistrationCodeValidityTimer: RegistrationCodeValidityTimer is already running. Stopping it.");
            cancelRegistrationCodeValidityTimer();
        }
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setRegistrationCodeValidityTimer: start validity period timer (" + j + " sec)");
        Intent intent = new Intent(McsConstants.McsActions.INTENT_REGISTRATION_CODE_VALIDITY_TIMEOUT);
        intent.setPackage(this.mContext.getPackageName());
        intent.putExtra(PhoneConstants.PHONE_KEY, this.mPhoneId);
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        this.mRegistrationCodeValidityIntent = broadcast;
        AlarmTimer.start(this.mContext, broadcast, j * 1000);
    }

    /* access modifiers changed from: protected */
    public void cancelAccessTokenValidityTimer() {
        if (this.mAccessTokenValidityIntent == null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "cancelValidityTimer: AccessToken validityTimer is not running.");
            return;
        }
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "cancel AccessToken ValidityTimer");
        AlarmTimer.stop(this.mContext, this.mAccessTokenValidityIntent);
        this.mAccessTokenValidityIntent = null;
    }

    /* access modifiers changed from: protected */
    public void cancelRefreshTokenValidityTimer() {
        if (this.mRefreshTokenValidityIntent == null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "cancelValidityTimer: RefreshToken validityTimer is not running.");
            return;
        }
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "cancel RefreshToken ValidityTimer");
        AlarmTimer.stop(this.mContext, this.mRefreshTokenValidityIntent);
        this.mRefreshTokenValidityIntent = null;
    }

    public void cancelRegistrationCodeValidityTimer() {
        if (this.mRegistrationCodeValidityIntent == null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "cancelValidityTimer: RegistrationCode validityTimer is not running.");
            return;
        }
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "cancel RegistrationCode ValidityTimer");
        AlarmTimer.stop(this.mContext, this.mRegistrationCodeValidityIntent);
        this.mRegistrationCodeValidityIntent = null;
    }

    public void clearToken() {
        this.mPreferenceManager.saveMcsAccessToken("");
        this.mPreferenceManager.saveMcsRefreshToken("");
        cancelAccessTokenValidityTimer();
        cancelRefreshTokenValidityTimer();
        this.mStoreClient.setProvisionStatus(false);
    }

    public void onDeRegistrationCompleted() {
        if (this.mPreferenceManager.getMcsUser() == 1) {
            EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "De-Registration is completed");
            resetMcsData();
            this.mPreferenceManager.saveMcsUser(0);
            unregisterSmsReceiver();
            notifyMcsProvisionListener(2, 100, 0, (Object) null);
        }
    }

    public void resetMcsData() {
        EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "reset MCS data");
        String userCtn = this.mPreferenceManager.getUserCtn();
        String simImsi = this.mPreferenceManager.getSimImsi();
        this.mNetAPIWorkingController.clearData();
        this.mNetAPIWorkingController.stopCMNWorking();
        clearData();
        this.mPreferenceManager.saveSimImsi(simImsi);
        this.mPreferenceManager.saveUserCtn(userCtn, false);
    }

    public void clearData() {
        this.mConsentContext = null;
        this.mPendingRequests.clear();
        this.mStoreClient.setProvisionStatus(false);
        clearWorkflow();
    }

    public void notifyMcsRegistrationStatus() {
        if (this.mPreferenceManager.getMcsUser() == 1) {
            notifyMcsProvisionListener(1, 100, 1, (Object) null);
        } else {
            notifyMcsProvisionListener(1, 200, 1, (Object) null);
        }
    }

    public void notifyMcsProvisionListener(int i, int i2, int i3, Object obj) {
        synchronized (this.mLock) {
            RemoteCallbackList<ICentralMsgStoreServiceListener> mcsProvisioningListener = this.mStoreClient.getMcsProvisioningListener();
            this.mMcsProvisioningListener = mcsProvisioningListener;
            if (mcsProvisioningListener == null) {
                IMSLog.i(this.LOG_TAG, this.mPhoneId, "notifyMcsProvisionListener: empty");
                return;
            }
            String str = this.LOG_TAG;
            int i4 = this.mPhoneId;
            EventLogHelper.infoLogAndAdd(str, i4, "notifyMcsProvisionListener: notify " + i + ", result " + i2 + ", details " + i3);
            try {
                int beginBroadcast = this.mMcsProvisioningListener.beginBroadcast();
                String str2 = this.LOG_TAG;
                int i5 = this.mPhoneId;
                IMSLog.i(str2, i5, "notifyMcsProvisionListener: length: " + beginBroadcast);
                for (int i6 = 0; i6 < beginBroadcast; i6++) {
                    ICentralMsgStoreServiceListener broadcastItem = this.mMcsProvisioningListener.getBroadcastItem(i6);
                    switch (i) {
                        case 1:
                            broadcastItem.onCmsRegistrationCompleted(i2, i3);
                            break;
                        case 2:
                            broadcastItem.onCmsDeRegistrationCompleted(i2);
                            break;
                        case 3:
                            broadcastItem.onCmsSdManagementCompleted(1, (String) obj, i2, i3);
                            break;
                        case 4:
                            broadcastItem.onCmsSdManagementCompleted(2, (String) obj, i2, i3);
                            break;
                        case 5:
                            broadcastItem.onCmsSdChanged(false, (String) obj, i2);
                            break;
                        case 6:
                            broadcastItem.onCmsSdChanged(true, (String) obj, i2);
                            break;
                        case 7:
                            Bundle bundle = (Bundle) obj;
                            broadcastItem.onCmsAccountInfoDelivered(bundle.getString("alias"), bundle.getString(McsConstants.Auth.CONSENT_CONTEXT), i2);
                            break;
                        case 8:
                            Bundle bundle2 = (Bundle) obj;
                            broadcastItem.onCmsPushMessageReceived(bundle2.getString(McsConstants.BundleData.PUSH_TYPE), bundle2.getString(McsConstants.BundleData.KEY), bundle2.getString("value"));
                            break;
                    }
                }
            } catch (RemoteException | AbstractMethodError | IllegalStateException | NullPointerException e) {
                String str3 = this.LOG_TAG;
                int i7 = this.mPhoneId;
                IMSLog.i(str3, i7, "notifyMcsProvisionListener: Exception: " + e.getMessage());
            }
            try {
                this.mMcsProvisioningListener.finishBroadcast();
            } catch (IllegalStateException e2) {
                String str4 = this.LOG_TAG;
                int i8 = this.mPhoneId;
                IMSLog.i(str4, i8, "notifyMcsProvisionListener: Exception: " + e2.getMessage());
            }
        }
        return;
    }

    /* access modifiers changed from: protected */
    public void registerSmsReceiver() {
        if (this.mSmsReceiver == null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "register mSmsReceiver");
            SmsReceiver smsReceiver = new SmsReceiver();
            this.mSmsReceiver = smsReceiver;
            this.mContext.registerReceiver(smsReceiver, smsReceiver.getIntentFilter());
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterSmsReceiver() {
        if (this.mSmsReceiver != null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "unregister mSmsReceiver");
            this.mContext.unregisterReceiver(this.mSmsReceiver);
            this.mSmsReceiver = null;
        }
    }

    protected class SmsReceiver extends BroadcastReceiver {
        protected IntentFilter mIntentFilter = null;

        public SmsReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            this.mIntentFilter = intentFilter;
            intentFilter.addAction(AECNamespace.Action.RECEIVED_SMS_NOTIFICATION);
            this.mIntentFilter.addDataScheme("sms");
            this.mIntentFilter.addDataAuthority("localhost", "16793");
        }

        public void onReceive(Context context, Intent intent) {
            SmsMessage smsMessage;
            if (AECNamespace.Action.RECEIVED_SMS_NOTIFICATION.equals(intent.getAction())) {
                try {
                    SmsMessage[] messagesFromIntent = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                    WorkflowMcs workflowMcs = WorkflowMcs.this;
                    IMSLog.i(workflowMcs.LOG_TAG, workflowMcs.mPhoneId, "onReceive");
                    if (messagesFromIntent != null && (smsMessage = messagesFromIntent[0]) != null) {
                        int slotId = SimManagerFactory.getSlotId(intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1));
                        String displayMessageBody = smsMessage.getDisplayMessageBody();
                        if (displayMessageBody == null) {
                            displayMessageBody = new String(smsMessage.getUserData(), StandardCharsets.UTF_16);
                        }
                        WorkflowMcs workflowMcs2 = WorkflowMcs.this;
                        int i = workflowMcs2.mPhoneId;
                        if (i == slotId) {
                            String str = workflowMcs2.LOG_TAG;
                            IMSLog.i(str, i, "response otp : " + IMSLog.numberChecker(displayMessageBody));
                            if (TextUtils.isEmpty(displayMessageBody)) {
                                WorkflowMcs workflowMcs3 = WorkflowMcs.this;
                                IMSLog.i(workflowMcs3.LOG_TAG, workflowMcs3.mPhoneId, "no SMS data!");
                            } else if (displayMessageBody.contains(WorkflowMcs.OASIS_RECOVERY_PREFIX)) {
                                WorkflowMcs.this.mRequestType = McsConstants.Auth.TYPE_MOBILE_IP;
                                WorkflowMcs.this.sendMessage(12);
                            } else if (displayMessageBody.contains(WorkflowMcs.OASIS_OTP_PREFIX)) {
                                WorkflowMcs workflowMcs4 = WorkflowMcs.this;
                                if (workflowMcs4.mOtpTimeoutIntent != null) {
                                    AlarmTimer.stop(workflowMcs4.mContext, WorkflowMcs.this.mOtpTimeoutIntent);
                                    WorkflowMcs.this.mOtpTimeoutIntent.cancel();
                                    WorkflowMcs workflowMcs5 = WorkflowMcs.this;
                                    workflowMcs5.mOtpTimeoutIntent = null;
                                    workflowMcs5.mOtp = displayMessageBody.substring(displayMessageBody.indexOf(AuthenticationHeaders.HEADER_PRARAM_SPERATOR) + 1);
                                }
                                if (TextUtils.isEmpty(WorkflowMcs.this.mOtp) || !WorkflowMcs.this.mWaitOtp) {
                                    WorkflowMcs.this.mRequestType = McsConstants.Auth.TYPE_MOBILE_IP;
                                    WorkflowMcs.this.sendMessage(20);
                                    return;
                                }
                                WorkflowMcs.this.mWaitOtp = false;
                                WorkflowMcs workflowMcs6 = WorkflowMcs.this;
                                workflowMcs6.sendMessage(12, (Object) workflowMcs6.mConsentContext);
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        public IntentFilter getIntentFilter() {
            return this.mIntentFilter;
        }
    }

    public void startOtpTimer(long j) {
        PendingIntent pendingIntent = this.mOtpTimeoutIntent;
        if (pendingIntent != null) {
            AlarmTimer.stop(this.mContext, pendingIntent);
            this.mOtpTimeoutIntent.cancel();
            this.mOtpTimeoutIntent = null;
        }
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "startOtpTimer");
        Intent intent = new Intent(McsConstants.McsActions.INTENT_OTP_RESPONSE_TIMEOUT);
        intent.setPackage(this.mContext.getPackageName());
        intent.putExtra(PhoneConstants.PHONE_KEY, this.mPhoneId);
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        this.mOtpTimeoutIntent = broadcast;
        AlarmTimer.start(this.mContext, broadcast, j * 1000);
    }

    /* access modifiers changed from: package-private */
    public void registerContentObservers() {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "registerContentObservers");
        try {
            this.mContext.getContentResolver().registerContentObserver(this.mAliasUri, false, this.mRCSContentObserver);
        } catch (SQLiteFullException | SecurityException e) {
            e.printStackTrace();
        }
    }

    class RCSContentObserver extends ContentObserver {
        public RCSContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean z, Uri uri) {
            if (uri.equals(WorkflowMcs.this.mAliasUri)) {
                WorkflowMcs workflowMcs = WorkflowMcs.this;
                IMSLog.i(workflowMcs.LOG_TAG, workflowMcs.mPhoneId, "onChange: RCS user alias is changed");
                WorkflowMcs.this.updateMcsAlias(false);
            }
        }
    }

    public void requestMcsAccessToken() {
        EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "requestMcsAccessToken: need to refresh");
        setAccessTokenValidityTimer(0);
    }

    public void requestMcsReAuthentication() {
        EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "requestMcsReAuthentication: token is no longer valid, need to ReAuthentication");
        IMSLog.c(LogClass.MCS_PV_REAUTH, this.mPhoneId + ",PV:REAUTH");
        clearToken();
        setRefreshTokenValidityTimer(0);
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "onSuccessfulCall request " + iHttpAPICommonInterface.getClass().getSimpleName());
        sendMessage(1, translateRequestCommand(iHttpAPICommonInterface.getClass().getSimpleName()), 0, obj);
    }

    public void handleSucceededEvent(int i, Object obj) {
        switch (i) {
            case 11:
                Bundle bundle = (Bundle) obj;
                String string = bundle.getString(McsConstants.Auth.ROOT_CLIENT_ID);
                String mcsClientId = CmsUtil.getMcsClientId(this.mContext);
                this.mConsentContext = bundle.getString(McsConstants.Auth.CONSENT_CONTEXT);
                String str = this.LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(str, i2, "mConsentContext " + this.mConsentContext);
                this.mPreferenceManager.saveMcsUser(1);
                if (!TextUtils.equals(string, mcsClientId)) {
                    this.mNeedInternalRegistration = true;
                    transitionTo(this.mDefaultState);
                } else {
                    transitionTo(this.mRegisteredState);
                }
                sendMessage(10, (Object) this.mConsentContext);
                notifyMcsRegistrationStatus();
                notifyMcsProvisionListener(7, 100, 0, bundle);
                return;
            case 12:
                this.mRequestType = McsConstants.Auth.TYPE_MOBILE_IP;
                if (obj == null) {
                    sendMessage(14);
                    return;
                }
                String string2 = ((Bundle) obj).getString(McsConstants.Auth.CONSENT_CONTEXT);
                if (TextUtils.isEmpty(string2)) {
                    return;
                }
                if (Util.isRegistrationCodeInvalid(this.mPreferenceManager.getRegCode())) {
                    IMSLog.i(this.LOG_TAG, this.mPhoneId, "registration code is expired, remove and retry to get it");
                    this.mPreferenceManager.saveRegCode("");
                    sendMessage(12);
                    return;
                }
                sendMessage(13, (Object) string2);
                return;
            case 13:
                sendMessage(14);
                return;
            case 14:
                transitionTo(this.mProvisionedState);
                updateMcsAlias(false);
                this.mNeedInternalRegistration = false;
                if (this.mPreferenceManager.getMcsUser() != 1) {
                    this.mPreferenceManager.saveMcsUser(1);
                    this.mStoreClient.setProvisionStatus(true);
                    notifyMcsProvisionListener(1, 100, 2, (Object) null);
                    return;
                }
                if (!this.mStoreClient.getProvisionStatus()) {
                    this.mStoreClient.setProvisionStatus(true);
                    if (this.mChangedToSamsungMessage) {
                        this.mChangedToSamsungMessage = false;
                        notifyMcsProvisionListener(1, 100, 5, (Object) null);
                    } else {
                        notifyMcsProvisionListener(1, 100, 4, (Object) null);
                    }
                } else {
                    notifyMcsProvisionListener(1, 100, 5, (Object) null);
                }
                if (!this.mPendingRequests.isEmpty()) {
                    String str2 = this.LOG_TAG;
                    int i3 = this.mPhoneId;
                    IMSLog.i(str2, i3, "pending requests" + this.mPendingRequests);
                    for (Integer intValue : this.mPendingRequests) {
                        int intValue2 = intValue.intValue();
                        if (intValue2 == 22) {
                            updatePendingAccountInfo();
                        } else {
                            sendMessage(intValue2);
                        }
                    }
                    this.mPendingRequests.clear();
                    return;
                }
                return;
            case 15:
                notifyMcsProvisionListener(3, 100, 1, (Object) null);
                return;
            case 16:
                notifyMcsProvisionListener(4, 100, 1, (Object) null);
                return;
            case 17:
                notifyMcsProvisionListener(6, 100, 0, obj);
                return;
            case 18:
                notifyMcsProvisionListener(5, 100, 0, obj);
                return;
            case 19:
                if (this.mWaitOtp) {
                    Bundle bundle2 = (Bundle) obj;
                    String oasisAuthRoot = this.mStoreClient.getPrerenceManager().getOasisAuthRoot();
                    if (oasisAuthRoot.contains("dev") || oasisAuthRoot.contains("stg")) {
                        this.mWaitOtp = false;
                        this.mOtp = "123456";
                        sendMessage(12, (Object) this.mConsentContext);
                        return;
                    } else if (oasisAuthRoot.equals(McsConstants.Auth.ROOT_URL)) {
                        startOtpTimer(bundle2.getLong("otpCodeValidity"));
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            case 21:
                notifyMcsProvisionListener(7, 100, 0, obj);
                return;
            case 22:
                notifyMcsProvisionListener(7, 100, 0, obj);
                if (((Bundle) obj).getInt(McsConstants.Auth.MCS_ACCOUNT_STATUS) == 9999) {
                    onDeRegistrationCompleted();
                    return;
                }
                return;
            case 23:
                onDeRegistrationCompleted();
                return;
            default:
                return;
        }
    }

    public int translateRequestCommand(String str) {
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -1980676141:
                if (str.equals("RequestDeleteAccount")) {
                    c = 0;
                    break;
                }
                break;
            case -1862082381:
                if (str.equals("RequestUserRegistration")) {
                    c = 1;
                    break;
                }
                break;
            case -1756838926:
                if (str.equals("RequestUserAuthentication")) {
                    c = 2;
                    break;
                }
                break;
            case 75215339:
                if (str.equals("RequestMCSToken")) {
                    c = 3;
                    break;
                }
                break;
            case 195383503:
                if (str.equals("RequestApproveSd")) {
                    c = 4;
                    break;
                }
                break;
            case 767503814:
                if (str.equals("RequestGetAccount")) {
                    c = 5;
                    break;
                }
                break;
            case 1023112949:
                if (str.equals("RequestUpdateAccount")) {
                    c = 6;
                    break;
                }
                break;
            case 1079181700:
                if (str.equals("RequestRemoveSd")) {
                    c = 7;
                    break;
                }
                break;
            case 1361745618:
                if (str.equals("RequestGetUser")) {
                    c = 8;
                    break;
                }
                break;
            case 1444990712:
                if (str.equals("RequestGetSD")) {
                    c = 9;
                    break;
                }
                break;
            case 1505457069:
                if (str.equals("RequestGetListOfSD")) {
                    c = 10;
                    break;
                }
                break;
            case 2087807357:
                if (str.equals("RequestOtpSms")) {
                    c = 11;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return 23;
            case 1:
                return 13;
            case 2:
                return 12;
            case 3:
                return 14;
            case 4:
                return 15;
            case 5:
                return 21;
            case 6:
                return 22;
            case 7:
                return 16;
            case 8:
                return 11;
            case 9:
                return 18;
            case 10:
                return 17;
            case 11:
                return 19;
            default:
                return 0;
        }
    }

    public void processFailResponseForGetUser(int i) {
        if (i == 404) {
            this.mPreferenceManager.saveMcsUser(0);
            notifyMcsRegistrationStatus();
        }
    }

    public void processFailResponseForAuthentication(int i) {
        if (i == 401) {
            this.mRequestType = McsConstants.Auth.TYPE_OTP;
            sendMessage(19);
        } else if (i == 900) {
            onDeRegistrationCompleted();
        } else if (this.mPreferenceManager.getMcsUser() != 1) {
            transitionTo(this.mDefaultState);
            notifyMcsProvisionListener(1, 200, 2, (Object) null);
        } else {
            transitionTo(this.mRegisteredState);
        }
    }

    public void processFailResponseForRegistration() {
        this.mWaitOtp = false;
        if (this.mPreferenceManager.getMcsUser() != 1) {
            transitionTo(this.mDefaultState);
            notifyMcsProvisionListener(1, 200, 2, (Object) null);
            return;
        }
        transitionTo(this.mRegisteredState);
    }

    public void processFailResponseForToken(int i) {
        if (i == 404) {
            sendMessage(12);
            return;
        }
        EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "onFailedCall: MCS provisioning is failed");
        int i2 = this.mPreferenceManager.getMcsUser() != 1 ? 2 : 4;
        if (this.mStoreClient.getProvisionStatus()) {
            i2 = 5;
        }
        this.mPendingRequests.clear();
        this.mStoreClient.setProvisionStatus(false);
        transitionTo(this.mRegisteredState);
        notifyMcsProvisionListener(1, 200, i2, (Object) null);
    }

    public void processFailResponseForBearerApi(int i, int i2) {
        if (i2 == 401) {
            int imsUserSetting = DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), this.mPhoneId);
            if (this.mStoreClient.isRcsRegistered() || imsUserSetting != 0) {
                clearToken();
                if (this.mPreferenceManager.getMcsUser() == 1) {
                    if (getCurrentState() != this.mProvisioningState) {
                        sendMessage(12);
                    }
                    this.mPendingRequests.add(Integer.valueOf(i));
                    String str = this.LOG_TAG;
                    int i3 = this.mPhoneId;
                    IMSLog.i(str, i3, "mPendingRequests" + this.mPendingRequests);
                    return;
                }
            } else {
                onDeRegistrationCompleted();
                return;
            }
        }
        notifyMcsProvisionListener(translateRequestToNotify(i), 200, 1, (Object) null);
    }

    public void handleFailedEvent(int i, int i2) {
        this.mRequestType = McsConstants.Auth.TYPE_MOBILE_IP;
        if (i == 11) {
            processFailResponseForGetUser(i2);
        } else if (i == 12) {
            processFailResponseForAuthentication(i2);
        } else if (i == 13 || i == 19 || i == 20) {
            processFailResponseForRegistration();
        } else if (i == 14) {
            processFailResponseForToken(i2);
        } else if (isBearerAuthRequest(i)) {
            processFailResponseForBearerApi(i, i2);
        }
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
        String str = this.LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "onFailedCall request " + iHttpAPICommonInterface.getClass().getSimpleName());
        if (i == 802) {
            String str2 = this.LOG_TAG;
            int i3 = this.mPhoneId;
            EventLogHelper.infoLogAndAdd(str2, i3, " curr state: " + getCurrentState().getName() + " request failure due to connection " + iHttpAPICommonInterface.getClass().getSimpleName());
        }
        sendMessage(2, translateRequestCommand(iHttpAPICommonInterface.getClass().getSimpleName()), i);
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        String simpleName = iHttpAPICommonInterface.getClass().getSimpleName();
        int parseInt = Integer.parseInt(str);
        String str2 = this.LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, " OnOverRequest : request " + simpleName + ", error code " + parseInt + ", retryAfter " + i + "ms");
        boolean checkAndIncreaseRetry = this.mStoreClient.getMcsRetryMapAdapter().checkAndIncreaseRetry(iHttpAPICommonInterface, parseInt);
        int translateRequestCommand = translateRequestCommand(simpleName);
        if (checkAndIncreaseRetry) {
            sendMessageDelayed(translateRequestCommand, (long) i);
        } else {
            sendMessage(2, translateRequestCommand, parseInt);
        }
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i, Object obj) {
        String simpleName = iHttpAPICommonInterface.getClass().getSimpleName();
        int parseInt = Integer.parseInt(str);
        String str2 = this.LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, " OnOverRequest2 : request " + simpleName + ", error code " + parseInt + ", retryAfter " + i + "ms");
        boolean checkAndIncreaseRetry = this.mStoreClient.getMcsRetryMapAdapter().checkAndIncreaseRetry(iHttpAPICommonInterface, parseInt);
        int translateRequestCommand = translateRequestCommand(simpleName);
        if (checkAndIncreaseRetry) {
            Bundle bundle = (Bundle) obj;
            if (translateRequestCommand == 12) {
                sendMessageDelayed(translateRequestCommand, (Object) bundle.getString(McsConstants.Auth.CONSENT_CONTEXT), (long) i);
            } else if (translateRequestCommand == 21 || translateRequestCommand == 22) {
                sendMessageDelayed(translateRequestCommand, (Object) bundle, (long) i);
            } else {
                sendMessageDelayed(translateRequestCommand, (long) i);
            }
        } else {
            sendMessage(2, translateRequestCommand, parseInt);
        }
    }
}
