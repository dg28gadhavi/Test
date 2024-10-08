package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.telephony.CellInfo;
import android.telephony.PreciseDataConnectionState;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.IImsDmConfigListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DelayedMessage;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.SystemWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.core.NetworkStateListener;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IMSLogTimer;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RegistrationManagerHandler extends Handler implements IRegistrationHandlerNotifiable {
    private static final String LOG_TAG = "RegiMgr-Handler";
    private static final long OMADM_TIMEOUT_MS = 30000;
    protected PhoneIdKeyMap<Integer> mAdhocProfileCounter;
    protected ICmcAccountManager mCmcAccountManager;
    protected IConfigModule mConfigModule;
    protected Context mContext;
    BroadcastReceiver mDsacEventReceiver;
    protected SimpleEventLog mEventLog;
    BroadcastReceiver mGvnIntentReceiver;
    protected boolean mHasPendingRecoveryAction;
    protected final RemoteCallbackList<IImsDmConfigListener> mImsDmConfigListener;
    protected IImsFramework mImsFramework;
    protected NetworkEventController mNetEvtCtr;
    protected final NetworkStateListener mNetworkStateListener;
    protected RegistrationObserverManager mObserverManager;
    protected PdnController mPdnController;
    protected int mPhoneCount;
    protected PreciseAlarmManager mPreAlarmMgr;
    protected RegistrationManagerBase mRegMan;
    final BroadcastReceiver mRetrySetupEventReceiver;
    final BroadcastReceiver mRilEventReceiver;
    protected List<ISimManager> mSimManagers;
    protected RegisterTask mTaskPendingRecoveryAction;
    protected ITelephonyManager mTelephonyManager;
    final BroadcastReceiver mUserEventReceiver;
    protected UserEventController mUserEvtCtr;
    final BroadcastReceiver mUserSwitchReceiver;
    protected IVolteServiceModule mVolteServiceModule;
    BroadcastReceiver mVzwEmmIntentReceiver;

    protected RegistrationManagerHandler(Looper looper, Context context, RegistrationManagerBase registrationManagerBase, NetworkEventController networkEventController) {
        super(looper);
        this.mImsDmConfigListener = new RemoteCallbackList<>();
        this.mHasPendingRecoveryAction = false;
        this.mTaskPendingRecoveryAction = null;
        this.mUserEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(RegistrationManagerHandler.LOG_TAG, "Received Intent : " + action);
                if (ImsConstants.Intents.ACTION_FLIGHT_MODE.equals(intent.getAction())) {
                    RegistrationManagerHandler.this.mUserEvtCtr.onShuttingDown(intent.getIntExtra("powerofftriggered", -1));
                } else if (ImsConstants.Intents.ACTION_DATAUSAGE_REACH_TO_LIMIT.equals(action)) {
                    boolean booleanExtra = intent.getBooleanExtra(ImsConstants.Intents.EXTRA_LIMIT_POLICY, false);
                    RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                    registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(Id.REQUEST_PRESENCE_UNSUBSCRIBE, booleanExtra ? 1 : 0, SimUtil.getActiveDataPhoneId(), (Object) null));
                }
            }
        };
        this.mUserSwitchReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int currentUserId;
                int currentUser;
                if (Extensions.Intent.ACTION_USER_SWITCHED.equals(intent.getAction()) && (currentUserId = RegistrationManagerHandler.this.mUserEvtCtr.getCurrentUserId()) != (currentUser = Extensions.ActivityManager.getCurrentUser())) {
                    Log.i(RegistrationManagerHandler.LOG_TAG, "User Switch " + currentUserId + " to " + currentUser);
                    RegistrationManagerHandler.this.mUserEvtCtr.setCurrentUserId(currentUser);
                    Extensions.Environment.initForCurrentUser();
                    RegistrationManagerHandler.this.removeMessages(1000);
                    RegistrationManagerHandler.this.sendEmptyMessage(1000);
                }
            }
        };
        this.mRilEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ImsConstants.Intents.ACTION_DCN_TRIGGERED.equals(intent.getAction())) {
                    int intExtra = intent.getIntExtra("phoneId", SimUtil.getActiveDataPhoneId());
                    RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                    registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(807, intExtra, -1));
                }
            }
        };
        this.mRetrySetupEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(RegistrationManagerHandler.LOG_TAG, "Received Intent : " + action);
                if (ImsConstants.Intents.ACTION_RETRYTIME_EXPIRED.equals(intent.getAction())) {
                    RegistrationManagerHandler.this.mNetEvtCtr.onRetryTimeExpired(intent.getIntExtra(ImsConstants.Intents.EXTRA_PHONE_ID, 0));
                } else if (ImsConstants.Intents.ACTION_T3396_EXPIRED.equals(intent.getAction())) {
                    onT3396Expired(intent.getIntExtra(PhoneConstants.PHONE_KEY, 0));
                }
            }

            private void onT3396Expired(int i) {
                if (RegistrationManagerHandler.this.mNetEvtCtr.hasRetryIntentOnPdnFail()) {
                    Log.i(RegistrationManagerHandler.LOG_TAG, "Operator default timer is running, No need update T3396 timer");
                } else {
                    SlotBasedConfig.getInstance(i).getRegistrationTasks().stream().filter(new RegistrationManagerHandler$4$$ExternalSyntheticLambda0()).forEach(new RegistrationManagerHandler$4$$ExternalSyntheticLambda1(this, i));
                }
            }

            /* access modifiers changed from: private */
            public static /* synthetic */ boolean lambda$onT3396Expired$0(RegisterTask registerTask) {
                return registerTask.getPdnType() == 11;
            }

            /* access modifiers changed from: private */
            public /* synthetic */ void lambda$onT3396Expired$1(int i, RegisterTask registerTask) {
                if (registerTask.getGovernor().isNonVoLteSimByPdnFail()) {
                    Log.i(RegistrationManagerHandler.LOG_TAG, "ignore T3396 expired, it is Non Volte sim");
                    return;
                }
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(2, Integer.valueOf(i)));
            }
        };
        this.mDsacEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ImsConstants.Intents.ACTION_DSAC_MODE_SWITCH.equals(intent.getAction())) {
                    IVolteServiceModule iVolteServiceModule = RegistrationManagerHandler.this.mVolteServiceModule;
                    if (iVolteServiceModule == null || iVolteServiceModule.getSessionCount() == 0 || RegistrationManagerHandler.this.mVolteServiceModule.hasEmergencyCall(SimUtil.getSimSlotPriority())) {
                        boolean z = true;
                        int intExtra = intent.getIntExtra(ImsConstants.Intents.EXTRA_DSAC_MODE, 1);
                        Log.i(RegistrationManagerHandler.LOG_TAG, "DsacEventReceiver, dsac Mode : " + intExtra);
                        if (RegistrationManagerHandler.this.mRegMan.getVolteAllowedWithDsac() && intExtra == 1) {
                            RegistrationManagerHandler.this.mRegMan.setVolteAllowedWithDsac(false);
                        } else if (RegistrationManagerHandler.this.mRegMan.getVolteAllowedWithDsac() || intExtra != 2) {
                            z = false;
                        } else {
                            RegistrationManagerHandler.this.mRegMan.setVolteAllowedWithDsac(true);
                        }
                        if (z) {
                            RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                            registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(146));
                        }
                    }
                }
            }
        };
        this.mGvnIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(RegistrationManagerHandler.LOG_TAG, "RegiGoverReceiver: received action " + action);
                if (ImsConstants.Intents.ACTION_WFC_SWITCH_PROFILE.equals(action)) {
                    byte[] byteArrayExtra = intent.getByteArrayExtra(ImsConstants.Intents.EXTRA_WFC_REQUEST);
                    RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                    registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(RegistrationEvents.EVENT_WFC_SWITCH_PROFILE, SimUtil.getActiveDataPhoneId(), 0, byteArrayExtra));
                }
            }
        };
        this.mVzwEmmIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ImsConstants.Intents.ACTION_EMM_ERROR.equals(intent.getAction())) {
                    try {
                        String stringExtra = intent.getStringExtra(IConfigModule.KEY_OMCNW_CODE);
                        if (!TextUtils.isEmpty(stringExtra)) {
                            int parseInt = Integer.parseInt(stringExtra);
                            RegistrationManagerHandler.this.mRegMan.setEmmCause(parseInt);
                            Log.i(RegistrationManagerHandler.LOG_TAG, "EMM Intent cause: " + parseInt);
                        }
                    } catch (Exception e) {
                        Log.e(RegistrationManagerHandler.LOG_TAG, "Exception occurred: " + e.toString());
                    }
                }
            }
        };
        this.mNetworkStateListener = new NetworkStateListener() {
            public void onDataConnectionStateChanged(int i, boolean z, int i2) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, i2, "onDataConnectionStateChanged(): networkType [" + TelephonyManagerExt.getNetworkTypeName(i) + "], isWifiConnected [" + z + "]");
                Bundle bundle = new Bundle();
                bundle.putInt("networkType", i);
                bundle.putInt("isWifiConnected", z ? 1 : 0);
                bundle.putInt("phoneId", i2);
                if (i == 13 || i == 20) {
                    IMSLogTimer.setLatchEndTime(i2);
                    IMSLogTimer.setPdnStartTime(i2, false);
                    ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i2);
                    if (simManagerFromSimSlot != null) {
                        IMSLog.LAZER_TYPE lazer_type = IMSLog.LAZER_TYPE.REGI;
                        IMSLog.lazer(lazer_type, "(" + i2 + ", " + ((String) Optional.ofNullable(simManagerFromSimSlot.getSimMnoName()).orElse("")) + ") [" + TelephonyManagerExt.getNetworkTypeName(i) + "] LATCH(DataConnect changed) : " + (((double) (IMSLogTimer.getLatchEndTime(i2) - IMSLogTimer.getLatchStartTime(i2))) / 1000.0d) + "s");
                    }
                }
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(3, bundle));
            }

            public void onCellInfoChanged(List<CellInfo> list, int i) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(24, i, -1, list));
            }

            public void onEpdgConnected(int i) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(26, i, -1));
            }

            public void onEpdgDisconnected(int i) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(27, i, -1));
            }

            public void onIKEAuthFAilure(int i) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, i, "onIKEAuthFAilure:");
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(52, Integer.valueOf(i)));
            }

            public void onEpdgIpsecDisconnected(int i) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(54, i, -1));
            }

            public void onEpdgDeregisterRequested(int i) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, i, "onEpdgDeregister: epdg deregister requested");
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(124, i, -1));
            }

            public void onEpdgRegisterRequested(int i, boolean z) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, i, "onEpdgRegister: cdmaAvailability : " + z);
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(123, i, z ? 1 : 0));
            }

            public void onDefaultNetworkStateChanged(int i) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, i, "onDefaultNetworkStateChanged: EVENT_TRY_REGISTER delayed");
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(RegistrationEvents.EVENT_DEFAULT_NETWORK_CHANGED, Integer.valueOf(i)));
            }

            public void onPreciseDataConnectionStateChanged(int i, PreciseDataConnectionState preciseDataConnectionState) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, i, "onPreciseDataConnectionStateChanged");
                if (((Boolean) Optional.ofNullable(preciseDataConnectionState.getApnSetting()).map(new RegistrationManagerHandler$8$$ExternalSyntheticLambda0()).map(new RegistrationManagerHandler$8$$ExternalSyntheticLambda1()).orElse(Boolean.FALSE)).booleanValue()) {
                    int state = preciseDataConnectionState.getState();
                    int transportType = preciseDataConnectionState.getTransportType();
                    int lastCauseCode = preciseDataConnectionState.getLastCauseCode();
                    if (lastCauseCode != 0) {
                        RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(129, i, lastCauseCode, Integer.valueOf(transportType)));
                    } else if (state == 1) {
                        RegistrationManagerHandler registrationManagerHandler2 = RegistrationManagerHandler.this;
                        registrationManagerHandler2.sendMessage(registrationManagerHandler2.obtainMessage(401, transportType, -1, Integer.valueOf(i)));
                    } else if (state == 0) {
                        RegistrationManagerHandler registrationManagerHandler3 = RegistrationManagerHandler.this;
                        registrationManagerHandler3.sendMessage(registrationManagerHandler3.obtainMessage(157, i, 0));
                    }
                }
            }

            /* access modifiers changed from: private */
            public static /* synthetic */ Boolean lambda$onPreciseDataConnectionStateChanged$0(Integer num) {
                return Boolean.valueOf((num.intValue() & 64) == 64);
            }

            public void onEpdgHandoverEnableChanged(int i, boolean z) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(154, i, z ? 1 : 0));
            }

            public void onMobileRadioConnected(int i) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(61, i, 0));
            }

            public void onMobileRadioDisconnected(int i) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(62, i, 0));
            }
        };
        this.mContext = context;
        this.mRegMan = registrationManagerBase;
        this.mNetEvtCtr = networkEventController;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 300);
        this.mPreAlarmMgr = PreciseAlarmManager.getInstance(context);
    }

    protected RegistrationManagerHandler(Looper looper, Context context, RegistrationManagerBase registrationManagerBase, IImsFramework iImsFramework, PdnController pdnController, List<ISimManager> list, ITelephonyManager iTelephonyManager, ICmcAccountManager iCmcAccountManager, NetworkEventController networkEventController, UserEventController userEventController, IVolteServiceModule iVolteServiceModule) {
        this(looper, context, registrationManagerBase, networkEventController);
        this.mPdnController = pdnController;
        this.mSimManagers = list;
        int size = list.size();
        this.mPhoneCount = size;
        this.mAdhocProfileCounter = new PhoneIdKeyMap<>(size, 10000);
        this.mTelephonyManager = iTelephonyManager;
        this.mCmcAccountManager = iCmcAccountManager;
        this.mImsFramework = iImsFramework;
        this.mObserverManager = new RegistrationObserverManager(this.mContext, this.mRegMan, this.mSimManagers, this);
        this.mUserEvtCtr = userEventController;
        this.mVolteServiceModule = iVolteServiceModule;
    }

    /* access modifiers changed from: protected */
    public void init() {
        registerInternalListeners();
        registerIntentReceivers();
    }

    /* access modifiers changed from: package-private */
    public void setConfigModule(IConfigModule iConfigModule) {
        this.mConfigModule = iConfigModule;
    }

    public void handleMessage(Message message) {
        Log.i(LOG_TAG, "handleMessage: " + RegistrationEvents.msgToString(message.what));
        if (!RegistrationEvents.handleEvent(message, this, this.mRegMan, this.mNetEvtCtr, this.mUserEvtCtr)) {
            Log.e(LOG_TAG, "handleMessage: unknown event " + message.what);
        }
    }

    class ImsStubActionReceiver extends BroadcastReceiver {
        protected static final String ACTION_MOCK_NETWORK_EVENT = "com.sec.ims.MOCK_IMS_EVENT";
        protected static final String EXTRA_EVENT = "event";
        protected static final String EXTRA_NETWORK = "network";
        protected static final String EXTRA_OOS = "oos";
        protected static final String EXTRA_PHONEID = "phoneid";
        protected static final String EXTRA_VOPS = "vops";

        ImsStubActionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.i(RegistrationManagerHandler.LOG_TAG, "onReceive: Intent " + intent);
            if (EXTRA_NETWORK.equalsIgnoreCase(intent.getStringExtra(EXTRA_EVENT))) {
                String stringExtra = intent.getStringExtra(EXTRA_NETWORK);
                String stringExtra2 = intent.getStringExtra(EXTRA_VOPS);
                String stringExtra3 = intent.getStringExtra(EXTRA_OOS);
                int parseInt = Integer.parseInt((String) Optional.ofNullable(intent.getStringExtra(EXTRA_PHONEID)).orElse("0"));
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, parseInt, "ImsStub: network event - network=" + stringExtra + " VoPS=" + stringExtra2 + " OutOfSvc=" + stringExtra3);
                NetworkEvent networkEvent = SlotBasedConfig.getInstance(parseInt).getNetworkEvent();
                if (networkEvent == null) {
                    IMSLog.i(RegistrationManagerHandler.LOG_TAG, parseInt, "onReceive, mNetworkEvent is not exist. Return..");
                    return;
                }
                NetworkEvent networkEvent2 = new NetworkEvent(networkEvent);
                if ("nr".equalsIgnoreCase(stringExtra)) {
                    networkEvent2.network = 20;
                } else if ("lte".equalsIgnoreCase(stringExtra)) {
                    networkEvent2.network = 13;
                } else if ("hspa".equalsIgnoreCase(stringExtra)) {
                    networkEvent2.network = 10;
                } else if ("ehrpd".equalsIgnoreCase(stringExtra)) {
                    networkEvent2.network = 14;
                } else if ("cdma".equalsIgnoreCase(stringExtra)) {
                    networkEvent2.network = 7;
                } else if ("iwlan".equalsIgnoreCase(stringExtra)) {
                    networkEvent2.network = 18;
                }
                if (intent.hasExtra(EXTRA_VOPS)) {
                    networkEvent2.voiceOverPs = VoPsIndication.translateVops(stringExtra2);
                }
                if (intent.hasExtra(EXTRA_OOS) && !TextUtils.isEmpty(stringExtra3)) {
                    networkEvent2.outOfService = Boolean.parseBoolean(stringExtra3);
                }
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(701, parseInt, 0, networkEvent2));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerIntentReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_FLIGHT_MODE);
        intentFilter.addAction(ImsConstants.Intents.ACTION_DATAUSAGE_REACH_TO_LIMIT);
        this.mContext.registerReceiver(this.mUserEventReceiver, intentFilter);
        ContextExt.registerReceiverAsUser(this.mContext.getApplicationContext(), this.mUserSwitchReceiver, ContextExt.ALL, new IntentFilter(Extensions.Intent.ACTION_USER_SWITCHED), (String) null, (Handler) null);
        this.mContext.registerReceiver(this.mGvnIntentReceiver, new IntentFilter(ImsConstants.Intents.ACTION_WFC_SWITCH_PROFILE));
        this.mContext.registerReceiver(this.mDsacEventReceiver, new IntentFilter(ImsConstants.Intents.ACTION_DSAC_MODE_SWITCH));
        this.mContext.registerReceiver(this.mRilEventReceiver, new IntentFilter(ImsConstants.Intents.ACTION_DCN_TRIGGERED));
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(ImsConstants.Intents.ACTION_RETRYTIME_EXPIRED);
        intentFilter2.addAction(ImsConstants.Intents.ACTION_T3396_EXPIRED);
        this.mContext.registerReceiver(this.mRetrySetupEventReceiver, intentFilter2);
        this.mContext.registerReceiver(this.mVzwEmmIntentReceiver, new IntentFilter(ImsConstants.Intents.ACTION_EMM_ERROR));
        if (!IMSLog.isShipBuild()) {
            ImsStubActionReceiver imsStubActionReceiver = new ImsStubActionReceiver();
            IntentFilter intentFilter3 = new IntentFilter();
            intentFilter3.addAction("com.sec.ims.MOCK_IMS_EVENT");
            this.mContext.registerReceiver(imsStubActionReceiver, intentFilter3);
        }
    }

    /* access modifiers changed from: protected */
    public void registerInternalListeners() {
        SimManagerFactory.registerForSubIdChange(this, 707, (Object) null);
        this.mSimManagers.forEach(new RegistrationManagerHandler$$ExternalSyntheticLambda19(this));
        if (this.mSimManagers.size() > 1) {
            SimManagerFactory.registerForADSChange(this, Id.REQUEST_PRESENCE_UNPUBLISH, (Object) null);
        }
        this.mPdnController.registerForNetworkState(this.mNetworkStateListener);
        this.mObserverManager.init();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$registerInternalListeners$0(ISimManager iSimManager) {
        int simSlotIndex = iSimManager.getSimSlotIndex();
        IMSLog.i(LOG_TAG, simSlotIndex, "Register SIM Event");
        iSimManager.registerForSimReady(this, 20, (Object) null);
        iSimManager.registerForUiccChanged(this, 21, Integer.valueOf(simSlotIndex));
        iSimManager.registerForSimRefresh(this, 36, (Object) null);
        iSimManager.registerForSimRemoved(this, 36, (Object) null);
        IMSLog.i(LOG_TAG, simSlotIndex, "Register PhoneStatelistener.");
        this.mPdnController.registerTelephonyCallback(simSlotIndex);
    }

    /* access modifiers changed from: protected */
    public void handleSimReady(int i, AsyncResult asyncResult) {
        if (!PackageUtils.isProcessRunning(this.mContext, "com.android.phone")) {
            IMSLog.i(LOG_TAG, i, "phone process is not ready.");
            sendMessageDelayed(obtainMessage(20, asyncResult), 500);
        } else if (!isReadyToStartRegistration(i)) {
            sendMessageDelayed(obtainMessage(20, asyncResult), 800);
        } else {
            boolean onEventSimReady = onEventSimReady(i);
            this.mCmcAccountManager.startCmcRegistration();
            if (!onEventSimReady && this.mCmcAccountManager.getCmcRegisterTask(i) != null) {
                IMSLog.i(LOG_TAG, i, "SimReady: readiness false but CMC exists");
                onEventSimReady = true;
            }
            this.mImsFramework.notifyImsReady(onEventSimReady, i);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isReadyToStartRegistration(int i) {
        IMSLog.i(LOG_TAG, i, "isReadyToStartRegistration:");
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager == null) {
            return false;
        }
        String simOperator = iSimManager.getSimOperator();
        if (hasMessages(36)) {
            IMSLog.e(LOG_TAG, i, "Sim refresh is ongoing. SIM readyretry after");
            return false;
        } else if (hasMessages(107)) {
            IMSLog.e(LOG_TAG, i, "Deregistering is not completed");
            return false;
        } else if (SimUtil.getPhoneCount() > 0 && TextUtils.isEmpty(this.mTelephonyManager.getImei(i))) {
            IMSLog.e(LOG_TAG, i, "IMEI is empty");
            return false;
        } else if (iSimManager.hasIsim() && iSimManager.getSimState() == 5 && TextUtils.isEmpty(simOperator)) {
            IMSLog.e(LOG_TAG, i, "OperatorCode is empty");
            return false;
        } else if (iSimManager.hasVsim() || !iSimManager.isSimAvailable() || !TextUtils.isEmpty(iSimManager.getImsi())) {
            return true;
        } else {
            IMSLog.e(LOG_TAG, i, "IMSI is not valid");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean onEventSimReady(int i) {
        IMSLog.i(LOG_TAG, i, "onEventSimReady:");
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager == null) {
            return false;
        }
        if (this.mRegMan.onSimReady(iSimManager.hasNoSim() || iSimManager.hasVsim(), i)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onRegistered(RegisterTask registerTask) {
        if (hasMessages(134)) {
            if (this.mHasPendingRecoveryAction) {
                this.mEventLog.logAndAdd(registerTask.getPhoneId(), registerTask, "onRegistered : mHasPendingRecoveryAction");
            } else {
                removeMessages(134);
            }
        }
        if (registerTask.getGovernor().isMobilePreferredForRcs()) {
            removeMessages(152);
        } else {
            removeMessages(132);
        }
        this.mRegMan.onRegistered(registerTask);
    }

    /* access modifiers changed from: protected */
    public void onDeregistered(Object obj) {
        Bundle bundle = (Bundle) obj;
        RegisterTask registerTaskByProfileId = this.mRegMan.getRegisterTaskByProfileId(bundle.getInt("profileId"), bundle.getInt("phoneId"));
        if (hasMessages(134)) {
            if (registerTaskByProfileId == null || !this.mHasPendingRecoveryAction) {
                removeMessages(134);
            } else {
                this.mEventLog.logAndAdd(registerTaskByProfileId.getPhoneId(), registerTaskByProfileId, "onDeregistered : mHasPendingRecoveryAction");
            }
        }
        removeMessages(107, registerTaskByProfileId);
        if (registerTaskByProfileId != null) {
            if (hasMessages(42, Integer.valueOf(registerTaskByProfileId.getPhoneId()))) {
                removeMessages(42, Integer.valueOf(registerTaskByProfileId.getPhoneId()));
            }
            this.mRegMan.onDeregistered(registerTaskByProfileId, (SipError) bundle.getParcelable("error", SipError.class), bundle.getLong("retryAfter"), bundle.getBoolean("isRequestedDeregi"), bundle.getBoolean("pcscfGone"));
        }
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeError(Object obj) {
        Bundle bundle = (Bundle) obj;
        RegisterTask registerTaskByProfileId = this.mRegMan.getRegisterTaskByProfileId(bundle.getInt("profileId"), bundle.getInt("phoneId"));
        if (registerTaskByProfileId != null) {
            this.mRegMan.onSubscribeError(registerTaskByProfileId, bundle.getParcelable("error"));
        }
    }

    /* access modifiers changed from: protected */
    public void onRegisterError(Object obj) {
        Bundle bundle = (Bundle) obj;
        RegisterTask registerTaskByProfileId = this.mRegMan.getRegisterTaskByProfileId(bundle.getInt("profileId"), bundle.getInt("phoneId"));
        if (registerTaskByProfileId == null) {
            IMSLog.i(LOG_TAG, "onRegisterError: task is null!");
            return;
        }
        int phoneId = registerTaskByProfileId.getPhoneId();
        if (hasMessages(134)) {
            if (this.mHasPendingRecoveryAction) {
                this.mEventLog.logAndAdd(phoneId, registerTaskByProfileId, "onRegisterError : mHasPendingRecoveryAction");
            } else {
                IMSLog.i(LOG_TAG, phoneId, "onRegisterError. Remove RegisteringRecovery message");
                removeMessages(134);
            }
        }
        if (hasMessages(107, registerTaskByProfileId) && (!registerTaskByProfileId.isRcsOnly() || !ConfigUtil.isRcsEur(registerTaskByProfileId.getMno()))) {
            IMSLog.i(LOG_TAG, phoneId, "onRegisterError. Remove EVENT_DEREGISTER_TIMEOUT");
            removeMessages(107, registerTaskByProfileId);
            registerTaskByProfileId.setReason("");
            registerTaskByProfileId.setDeregiReason(41);
        }
        this.mRegMan.onRegisterError(registerTaskByProfileId, bundle.getInt(EucTestIntent.Extras.HANDLE), (SipError) bundle.getParcelable("error", SipError.class), bundle.getLong("retryAfter"));
    }

    /* access modifiers changed from: package-private */
    public void onDeregistrationRequest(RegisterTask registerTask, boolean z, boolean z2) {
        Log.i(LOG_TAG, "onDeregistrationRequest: task=" + registerTask.getProfile().getName());
        this.mRegMan.tryDeregisterInternal(registerTask, z, z2);
    }

    /* access modifiers changed from: package-private */
    public void onUpdateRegistration(ImsProfile imsProfile, int i) {
        Log.i(LOG_TAG, "onUpdateRegistration:");
        RegisterTask registerTaskByProfileId = this.mRegMan.getRegisterTaskByProfileId(imsProfile.getId(), i);
        if (registerTaskByProfileId == null) {
            Log.i(LOG_TAG, "onUpdateRegistration: registration task not found.");
            return;
        }
        SlotBasedConfig.getInstance(i).addExtendedProfile(imsProfile.getId(), imsProfile);
        ImsProfile profile = registerTaskByProfileId.getProfile();
        if (!profile.equals(imsProfile)) {
            Log.i(LOG_TAG, "onUpdateRegistration: imsprofile changed.");
            profile.setExtImpuList(imsProfile.getExtImpuList());
            if (registerTaskByProfileId.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                registerTaskByProfileId.setReason("External IMPU list changed");
                registerTaskByProfileId.setDeregiReason(28);
                this.mRegMan.tryDeregisterInternal(registerTaskByProfileId, false, true);
                sendMessage(obtainMessage(2, Integer.valueOf(i)));
            }
        } else if (registerTaskByProfileId.getMno() == Mno.KDDI || registerTaskByProfileId.getGovernor().needImsNotAvailable()) {
            Log.i(LOG_TAG, "onUpdateRegistration: For KDDI, LGT(ImsNotAvailable) Send the Refresh Reg even thoug there is no change in services.");
            this.mRegMan.sendReRegister(registerTaskByProfileId);
        }
    }

    /* access modifiers changed from: protected */
    public void handleMnoMapUpdated(int i) {
        ITelephonyManager instance = TelephonyManagerWrapper.getInstance(this.mContext);
        if (instance.getCallState(i) == 0 && instance.getCallState(SimUtil.getOppositeSimSlot(i)) == 0) {
            Log.i(LOG_TAG, "imsservice reboot");
            IMSLog.c(LogClass.REGI_DO_RECOVERY_ACTION, "MnoMap Updated", true);
            SystemWrapper.exit(0);
            return;
        }
        IMSLog.i(LOG_TAG, i, " Call exist.. Delay imsservice reboot.");
        removeMessages(148);
        sendMessageDelayed(obtainMessage(148, i, 0, (Object) null), 5000);
    }

    /* access modifiers changed from: protected */
    public void handleDynamicImsUpdated(int i) {
        if (this.mRegMan.getTelephonyCallStatus(i) != 0) {
            removeMessages(408);
            sendMessageDelayed(obtainMessage(408, i, 0, (Object) null), 5000);
            return;
        }
        removeMessages(15);
        this.mRegMan.onImsProfileUpdated(i);
    }

    /* access modifiers changed from: protected */
    public void handleUiccChanged(int i) {
        this.mImsFramework.getServiceModuleManager().notifySimChange(i);
    }

    /* access modifiers changed from: protected */
    public void handleDelayedStopPdn(RegisterTask registerTask) {
        this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
        registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
        int phoneId = registerTask.getPhoneId();
        if (!registerTask.getMno().isKor()) {
            this.mRegMan.setOmadmState(phoneId, RegistrationManager.OmadmConfigState.IDLE);
            this.mConfigModule.getAcsConfig(phoneId).setForceAcs(true);
        } else if (!registerTask.isRcsOnly()) {
            this.mRegMan.setOmadmState(phoneId, RegistrationManager.OmadmConfigState.IDLE);
        } else {
            this.mConfigModule.getAcsConfig(phoneId).setForceAcs(true);
        }
        sendEmptyMessage(32);
        sendMessage(obtainMessage(2, Integer.valueOf(phoneId)));
    }

    /* access modifiers changed from: protected */
    public void onRequestNotifyVolteSettingsOff(RegisterTask registerTask) {
        Log.i(LOG_TAG, "onRequestNotifyVolteSettingsOff");
        removeMessages(131);
        registerTask.getGovernor().notifyVoLteOnOffToRil(false);
    }

    /* access modifiers changed from: protected */
    public void onLocationTimerExpired(RegisterTask registerTask) {
        Log.i(LOG_TAG, "onLocationTimerExpired");
        removeMessages(800);
        registerTask.getGovernor().notifyLocationTimeout();
    }

    /* access modifiers changed from: protected */
    public void onRequestLocation() {
        Log.i(LOG_TAG, "onRequestLocation");
        removeMessages(801);
        for (int i = 0; i < this.mPhoneCount; i++) {
            Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getRegistrationRat() == 18 && this.mPdnController.isWifiConnected() && registerTask.getProfile().isEpdgSupported()) {
                    Log.i(LOG_TAG, "onRequestLocation: request location fetch");
                    registerTask.getGovernor().requestLocation(registerTask.getPhoneId());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLocationCacheExpired(RegisterTask registerTask) {
        Log.i(LOG_TAG, "onLocationCacheExpired");
        removeMessages(803);
        registerTask.getGovernor().onLocationCacheExpiry();
    }

    /* access modifiers changed from: protected */
    public void onTimsTimerExpired(RegisterTask registerTask) {
        Log.i(LOG_TAG, "onTimsTimerExpired " + registerTask.getProfile().getName() + "(" + registerTask.getState() + ")");
        if (registerTask.getGovernor().isMobilePreferredForRcs()) {
            removeMessages(152);
        } else {
            removeMessages(132);
        }
        if (registerTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
            registerTask.getGovernor().onTimsTimerExpired();
        } else {
            Log.i(LOG_TAG, "Registered. Igonre onTimsTimerExpired.");
        }
    }

    /* access modifiers changed from: protected */
    public void onUpdateChatServiceByDmaChange(int i, boolean z) {
        for (ImsRegistration next : SlotBasedConfig.getInstance(i).getImsRegistrations().values()) {
            ImsProfile imsProfile = next.getImsProfile();
            if (!ImsProfile.hasChatService(imsProfile)) {
                IMSLog.i(LOG_TAG, i, "onUpdateChatServiceByDmaChange: Ignore " + IMSLog.numberChecker(next.toString()));
            } else if (!z) {
                this.mEventLog.logAndAdd(i, "onUpdateChatServiceByDmaChange: Postpone the update registration till next own cap. change by force ACS");
                this.mRegMan.postponeUpdateRegistrationByDmaChange(i);
            } else {
                this.mEventLog.logAndAdd(i, "onUpdateChatServiceByDmaChange: Trigger forced update registration. By timeout");
                this.mRegMan.forcedUpdateRegistration(imsProfile, i);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDisableChatFeatureBySipForbidden(int i) {
        for (ImsRegistration imsProfile : SlotBasedConfig.getInstance(i).getImsRegistrations().values()) {
            ImsProfile imsProfile2 = imsProfile.getImsProfile();
            if (imsProfile2.hasService("im") || imsProfile2.hasService("slm") || imsProfile2.hasService("ft_http") || imsProfile2.hasService("ft")) {
                this.mEventLog.logAndAdd(i, "onDisableChatFeatureBySipForbidden: Trigger forced update registration");
                this.mRegMan.forcedUpdateRegistration(imsProfile2, i);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onPcoInfo(int i, int i2, int i3) {
        IMSLog.i(LOG_TAG, i3, "onPcoInfo: " + RegistrationGovernor.PcoType.fromType(i2) + "(" + i2 + ")");
        SlotBasedConfig.getInstance(i3).getRegistrationTasks().stream().filter(new RegistrationManagerHandler$$ExternalSyntheticLambda1(i, i2)).forEach(new RegistrationManagerHandler$$ExternalSyntheticLambda2(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onPcoInfo$2(RegisterTask registerTask) {
        if (hasMessages(22, registerTask)) {
            removeMessages(22, registerTask);
            this.mNetEvtCtr.onPdnConnected(registerTask);
        }
    }

    /* access modifiers changed from: protected */
    public void onWfcSwitchProfile(byte[] bArr, int i) {
        IMSLog.i(LOG_TAG, i, "onWfcSwitchProfile:");
        SlotBasedConfig.getInstance(i).getRegistrationTasks().forEach(new RegistrationManagerHandler$$ExternalSyntheticLambda10(bArr));
    }

    /* access modifiers changed from: protected */
    public void onRcsDelayedDeregister() {
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            SlotBasedConfig.getInstance(i).getRegistrationTasks().stream().filter(new RegistrationManagerHandler$$ExternalSyntheticLambda4()).filter(new RegistrationManagerHandler$$ExternalSyntheticLambda5()).filter(new RegistrationManagerHandler$$ExternalSyntheticLambda6()).forEach(new RegistrationManagerHandler$$ExternalSyntheticLambda7(this));
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$onRcsDelayedDeregister$4(RegisterTask registerTask) {
        return registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onRcsDelayedDeregister$6(RegisterTask registerTask) {
        registerTask.setDeregiReason(4);
        this.mRegMan.tryDeregisterInternal(registerTask, true, false);
    }

    /* access modifiers changed from: protected */
    public void onRCSAllowedChangedbyMDM() {
        Log.i(LOG_TAG, "onRCSAllowedChangedbyMDM:");
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            SlotBasedConfig.getInstance(i).getRegistrationTasks().stream().filter(new RegistrationManagerHandler$$ExternalSyntheticLambda8(this)).forEach(new RegistrationManagerHandler$$ExternalSyntheticLambda9(this));
        }
    }

    /* access modifiers changed from: private */
    public boolean hasRcsChatService(RegisterTask registerTask) {
        return ImsUtil.isMatchedService(registerTask.getProfile().getAllServiceSetFromAllNetwork(), ChatServiceImpl.SUBJECT);
    }

    /* access modifiers changed from: private */
    public void onRcsAllowedChangedByMdm(RegisterTask registerTask) {
        int phoneId;
        int activeDataPhoneId;
        if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            registerTask.setReason("RCS allowed changed");
            this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.RCS_ALLOWED_CHANGED);
            return;
        }
        Mno mno = registerTask.getMno();
        if (!registerTask.isRcsOnly() || ((!ConfigUtil.isRcsEur(mno) && !ConfigUtil.isRcsChn(mno)) || (phoneId = registerTask.getPhoneId()) == (activeDataPhoneId = SimUtil.getActiveDataPhoneId()))) {
            this.mRegMan.tryRegister(registerTask);
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "skip RCS tryRegister due to non activeDataPhoneId : " + activeDataPhoneId);
    }

    /* access modifiers changed from: protected */
    public void onBlockRegistrationRoamingTimer(int i, int i2) {
        RegisterTask registerTaskByRegHandle = this.mRegMan.getRegisterTaskByRegHandle(i);
        if (registerTaskByRegHandle != null) {
            this.mRegMan.deregister(registerTaskByRegHandle, true, false, "Orange Group, VoWIFI Error in Roaming");
            registerTaskByRegHandle.getGovernor().addDelay((long) i2);
        }
    }

    /* access modifiers changed from: protected */
    public void onThirdParyFeatureTagsUpdated(int i) {
        IMSLog.i(LOG_TAG, i, "onThirdParyFeatureTagsUpdated");
        SlotBasedConfig.getInstance(i).getRegistrationTasks().stream().filter(new RegistrationManagerHandler$$ExternalSyntheticLambda14()).forEach(new RegistrationManagerHandler$$ExternalSyntheticLambda15(this, i));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onThirdParyFeatureTagsUpdated$8(int i, RegisterTask registerTask) {
        IMSLog.i(LOG_TAG, i, "onThirdParyFeatureTagsUpdated: force update " + registerTask);
        registerTask.setReason("3rd party feature tag updated");
        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.THIRDPARTY_FEATURETAG_UPDATED);
    }

    /* access modifiers changed from: package-private */
    public void onSSACRegiRequested(int i, boolean z) {
        IMSLog.i(LOG_TAG, i, "onSSACRegiRequested : enabled(" + z + ")");
        SlotBasedConfig.getInstance(i).enableSsac(z);
        removeMessages(121, Integer.valueOf(i));
        SlotBasedConfig.getInstance(i).getRegistrationTasks().stream().filter(new RegistrationManagerHandler$$ExternalSyntheticLambda11()).filter(new RegistrationManagerHandler$$ExternalSyntheticLambda12()).findFirst().ifPresent(new RegistrationManagerHandler$$ExternalSyntheticLambda13(this, i));
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$onSSACRegiRequested$10(RegisterTask registerTask) {
        return registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onSSACRegiRequested$11(int i, RegisterTask registerTask) {
        IMSLog.i(LOG_TAG, i, "onSSACRegiRequested: update registration " + registerTask);
        registerTask.setReason("SSAC updated");
        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SSAC_UPDATED);
    }

    /* access modifiers changed from: package-private */
    public void onDisconnectPdnByTimeout(RegisterTask registerTask) {
        int phoneId = registerTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "onDisconnectPdnByTimeout: " + registerTask.getState());
        registerTask.getGovernor().notifyReattachToRil();
        if (registerTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
            this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
            sendMessageDelayed(obtainMessage(2, Integer.valueOf(phoneId)), 1000);
            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            registerTask.getGovernor().resetAllRetryFlow();
        }
    }

    /* access modifiers changed from: package-private */
    public void onDisconnectPdnByVolteDisabled(RegisterTask registerTask) {
        int phoneId = registerTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "onDisconnectPdnByVolteDisabled: " + registerTask.getState());
        this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
        registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
        registerTask.getGovernor().resetAllRetryFlow();
    }

    /* access modifiers changed from: package-private */
    public void onGeoLocationUpdated() {
        Log.i(LOG_TAG, "onGeoLocationUpdated:");
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getGovernor().isDeregisterOnLocationUpdate()) {
                    this.mRegMan.sendDeregister(802, i);
                } else {
                    this.mRegMan.updatePani(registerTask);
                    if (registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && registerTask.getPdnType() == 11) {
                        RegistrationConstants.UpdateRegiReason updateRegiReason = RegistrationConstants.UpdateRegiReason.GEOLOCATION_CHANGED;
                        NetworkEvent networkEvent = SlotBasedConfig.getInstance(i).getNetworkEvent();
                        if (networkEvent == null) {
                            continue;
                        } else if (networkEvent.network != 18 || this.mPdnController.isEpdgConnected(i)) {
                            if (networkEvent.network != registerTask.getRegistrationRat()) {
                                updateRegiReason = RegistrationConstants.UpdateRegiReason.GEOLOCATION_CHANGED_FORCED;
                            }
                            registerTask.setReason("geolocation changed");
                            RegistrationManagerBase registrationManagerBase = this.mRegMan;
                            registrationManagerBase.updateRegistration(registerTask, updateRegiReason, registrationManagerBase.getTelephonyCallStatus(i) != 0);
                        } else {
                            return;
                        }
                    } else {
                        this.mRegMan.tryRegister(i);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSimSubscribeIdChanged(SubscriptionInfo subscriptionInfo) {
        int simSlotIndex = subscriptionInfo.getSimSlotIndex();
        IMSLog.i(LOG_TAG, simSlotIndex, "onSimSubscribeIdChanged, SimSlot: , subId: " + subscriptionInfo.getSubscriptionId());
        int simSlotIndex2 = subscriptionInfo.getSimSlotIndex();
        this.mPdnController.registerTelephonyCallback(simSlotIndex2);
        ImsIconManager imsIconManager = this.mRegMan.getImsIconManager(simSlotIndex2);
        if (imsIconManager != null) {
            imsIconManager.registerPhoneStateListener();
        }
        for (ISimManager next : this.mSimManagers) {
            if (next.getSimSlotIndex() == subscriptionInfo.getSimSlotIndex()) {
                next.setSubscriptionInfo(subscriptionInfo);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDsacModeChanged() {
        Iterator it = SlotBasedConfig.getInstance(SimUtil.getActiveDataPhoneId()).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            if (registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && !registerTask.getProfile().hasEmergencySupport()) {
                registerTask.setReason("re-regi by dsac");
                this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.DSAC_MODE_CHANGED, false);
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRegEventContactUriNotified(Object obj) {
        Bundle bundle = (Bundle) obj;
        RegisterTask registerTaskByRegHandle = this.mRegMan.getRegisterTaskByRegHandle(bundle.getInt(EucTestIntent.Extras.HANDLE));
        if (registerTaskByRegHandle != null) {
            ArrayList parcelableArrayList = bundle.getParcelableArrayList("contact_uri_list", ImsUri.class);
            int i = bundle.getInt("isRegi");
            String string = bundle.getString("contactUriType");
            registerTaskByRegHandle.getGovernor().onRegEventContactUriNotification(parcelableArrayList, i, string, bundle.getString("emergencyNumbers"));
            int cmcType = registerTaskByRegHandle.getProfile().getCmcType();
            Log.d(LOG_TAG, "cmcType: " + cmcType + ", isRegi: " + i + ", type: " + string);
            if (cmcType == 8 || (cmcType == 7 && i == 1)) {
                SecImsNotifier.getInstance().onP2pRegCompleteEvent(registerTaskByRegHandle.getPhoneId());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDelayedDeregisterInternal(RegisterTask registerTask, boolean z) {
        Log.i(LOG_TAG, "onDelayedDeregisterInternal: task=" + registerTask.getProfile().getName());
        this.mRegMan.deregisterInternal(registerTask, z);
    }

    /* access modifiers changed from: package-private */
    public void onDeregisterTimeout(IRegisterTask iRegisterTask) {
        int phoneId = iRegisterTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "onDeregisterTimeout:");
        iRegisterTask.clearUpdateRegisteringFlag();
        IUserAgent userAgent = iRegisterTask.getUserAgent();
        if (userAgent == null) {
            IMSLog.e(LOG_TAG, phoneId, "onDeregisterTimeout: no object");
            ImsProfile profile = iRegisterTask.getProfile();
            ImsRegistration imsRegistration = iRegisterTask.getImsRegistration();
            if (imsRegistration == null) {
                imsRegistration = ImsRegistration.getBuilder().setHandle(-1).setImsProfile(new ImsProfile(profile)).setServices(profile.getServiceSet(Integer.valueOf(iRegisterTask.getRegistrationRat()))).setEpdgStatus(this.mPdnController.isEpdgConnected(phoneId)).setPdnType(iRegisterTask.getPdnType()).setUuid(this.mRegMan.getUuid(phoneId, profile)).setInstanceId(this.mRegMan.getInstanceId(phoneId, iRegisterTask.getPdnType(), profile)).setNetwork(iRegisterTask.getNetworkConnected()).setRegiRat(iRegisterTask.getRegistrationRat()).setPhoneId(phoneId).build();
            }
            if ((iRegisterTask.getMno() == Mno.KDDI || iRegisterTask.getGovernor().needImsNotAvailable()) && iRegisterTask.getDeregiReason() == 72) {
                this.mRegMan.notifyImsRegistration(imsRegistration, false, iRegisterTask, new ImsRegistrationError(0, "", 72, 32));
            } else {
                RegistrationManagerBase registrationManagerBase = this.mRegMan;
                SipError sipError = SipErrorBase.TEMPORARILY_UNAVAIABLE;
                registrationManagerBase.notifyImsRegistration(imsRegistration, false, iRegisterTask, new ImsRegistrationError(sipError.getCode(), sipError.getReason(), 41, 16));
            }
            if (!profile.hasEmergencySupport()) {
                iRegisterTask.setRecoveryReason(RegistrationConstants.RecoveryReason.NO_USER_AGENT);
                sendMessage(obtainMessage(134, iRegisterTask));
            }
            iRegisterTask.setReason("");
            iRegisterTask.setDeregiReason(41);
            return;
        }
        if ("InitialState".equals(userAgent.getStateName())) {
            iRegisterTask.setRecoveryReason(RegistrationConstants.RecoveryReason.UA_STATE_MISMATCH);
            sendMessage(obtainMessage(134, iRegisterTask));
        }
        userAgent.deregisterLocal();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00a4, code lost:
        r7 = com.sec.internal.helper.httpclient.OkHostnameVerifierWrapper.verify(r11, r15[0]);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void verifyX509Certificate(java.security.cert.X509Certificate[] r15) {
        /*
            r14 = this;
            java.lang.String r0 = "verifyX509Certificate()"
            java.lang.String r1 = "RegiMgr-Handler"
            android.util.Log.i(r1, r0)
            r0 = 0
            r2 = 0
            if (r15 == 0) goto L_0x00ea
            int r3 = r15.length
            r4 = 1
            if (r3 >= r4) goto L_0x0012
            goto L_0x00ea
        L_0x0012:
            boolean r3 = com.sec.internal.helper.os.SystemUtil.verifyCerts(r15)
            int r5 = com.sec.internal.helper.SimUtil.getPhoneCount()
            r6 = r2
            r7 = r6
        L_0x001c:
            java.lang.String r8 = "verifyId "
            if (r6 >= r5) goto L_0x00b1
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r9.append(r8)
            r9.append(r6)
            java.lang.String r10 = ", maxSimCount "
            r9.append(r10)
            r9.append(r5)
            java.lang.String r9 = r9.toString()
            android.util.Log.i(r1, r9)
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r9 = r14.mSimManagers
            java.lang.Object r9 = r9.get(r6)
            com.sec.internal.interfaces.ims.core.ISimManager r9 = (com.sec.internal.interfaces.ims.core.ISimManager) r9
            com.sec.internal.ims.core.SlotBasedConfig r10 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r6)
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r10 = r10.getRegistrationTasks()
            if (r9 == 0) goto L_0x00ad
            if (r10 != 0) goto L_0x0050
            goto L_0x00ad
        L_0x0050:
            if (r3 == 0) goto L_0x00ad
            java.lang.String r9 = "Verifying certificate names..."
            android.util.Log.i(r1, r9)
            java.util.Iterator r9 = r10.iterator()
        L_0x005b:
            boolean r10 = r9.hasNext()
            if (r10 == 0) goto L_0x00ad
            java.lang.Object r10 = r9.next()
            com.sec.internal.interfaces.ims.core.IRegisterTask r10 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r10
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r11 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r12 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERING
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r11 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[]{r11, r12}
            boolean r11 = r10.isOneOf(r11)
            if (r11 == 0) goto L_0x005b
            java.lang.String r11 = r10.getPcscfHostname()
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "Checking task: "
            r12.append(r13)
            com.sec.ims.settings.ImsProfile r10 = r10.getProfile()
            java.lang.String r10 = r10.getName()
            r12.append(r10)
            java.lang.String r10 = " / "
            r12.append(r10)
            r12.append(r11)
            java.lang.String r10 = r12.toString()
            android.util.Log.i(r1, r10)
            boolean r10 = android.text.TextUtils.isEmpty(r11)
            if (r10 == 0) goto L_0x00a4
            goto L_0x005b
        L_0x00a4:
            r7 = r15[r2]
            boolean r7 = com.sec.internal.helper.httpclient.OkHostnameVerifierWrapper.verify(r11, r7)
            if (r7 == 0) goto L_0x005b
            goto L_0x00b1
        L_0x00ad:
            int r6 = r6 + 1
            goto L_0x001c
        L_0x00b1:
            if (r3 == 0) goto L_0x00bb
            if (r7 != 0) goto L_0x00bb
            com.sec.internal.ims.core.RegistrationManagerBase r14 = r14.mRegMan
            boolean r7 = r14.verifyCmcCertificate(r15)
        L_0x00bb:
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r14.append(r8)
            r14.append(r6)
            java.lang.String r15 = ", verified "
            r14.append(r15)
            r14.append(r3)
            java.lang.String r15 = ", nameMatch "
            r14.append(r15)
            r14.append(r7)
            java.lang.String r14 = r14.toString()
            android.util.Log.i(r1, r14)
            com.sec.internal.ims.core.handler.secims.StackIF r14 = com.sec.internal.ims.core.handler.secims.StackIF.getInstance()
            if (r3 == 0) goto L_0x00e6
            if (r7 == 0) goto L_0x00e6
            r2 = r4
        L_0x00e6:
            r14.sendX509CertVerifyResponse(r2, r0)
            return
        L_0x00ea:
            java.lang.String r14 = "there is no certificate"
            android.util.Log.i(r1, r14)
            com.sec.internal.ims.core.handler.secims.StackIF r14 = com.sec.internal.ims.core.handler.secims.StackIF.getInstance()
            r14.sendX509CertVerifyResponse(r2, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerHandler.verifyX509Certificate(java.security.cert.X509Certificate[]):void");
    }

    /* access modifiers changed from: package-private */
    public void onBootCompleted() {
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            SlotBasedConfig.getInstance(i).getRegistrationTasks().forEach(new RegistrationManagerHandler$$ExternalSyntheticLambda0(this));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onBootCompleted$12(RegisterTask registerTask) {
        if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.BOOT_COMPLETED);
        } else {
            this.mRegMan.tryRegister(registerTask);
        }
    }

    /* access modifiers changed from: package-private */
    public void onTelephonyCallStatusChanged(int i, int i2) {
        this.mRegMan.setCallState(i2);
        IMSLog.i(LOG_TAG, i, "onTelephonyCallStatusChanged: " + i2);
        if (i2 != 0 || !this.mHasPendingRecoveryAction || this.mTaskPendingRecoveryAction == null) {
            SlotBasedConfig.getInstance(i).getRegistrationTasks().forEach(new RegistrationManagerHandler$$ExternalSyntheticLambda3(i2));
            if (i2 == 0) {
                if (!hasMessages(32)) {
                    sendEmptyMessage(32);
                }
                sendMessage(obtainMessage(2, Integer.valueOf(i)));
                if (SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS())) {
                    sendMessage(obtainMessage(2, Integer.valueOf(SimUtil.getOppositeSimSlot(i))));
                }
                try {
                    ISimManager iSimManager = this.mSimManagers.get(i);
                    if (iSimManager.isSimAvailable() && iSimManager.getSimMno() == Mno.ZAIN_KUWAIT && this.mImsFramework.isServiceAvailable("mmtel", 20, i)) {
                        if (this.mPdnController.isWifiConnected()) {
                            this.mTelephonyManager.semSetNrMode(i, 4);
                        } else {
                            this.mTelephonyManager.semSetNrMode(i, 3);
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.mEventLog.logAndAdd("onTelephonyCallStatusChanged : do recovery after call end");
            IMSLog.c(LogClass.REGI_DO_RECOVERY_ACTION, this.mTaskPendingRecoveryAction.getPhoneId() + ",DO RECOVERY: CALL END", true);
            this.mTaskPendingRecoveryAction.setRecoveryReason(RegistrationConstants.RecoveryReason.POSTPONED_RECOVERY);
            sendMessage(obtainMessage(134, this.mTaskPendingRecoveryAction));
        }
    }

    /* access modifiers changed from: package-private */
    public void onFlightModeChanged(boolean z) {
        if (z) {
            removeMessages(134);
        }
        this.mUserEvtCtr.onFlightModeChanged(z);
    }

    /* access modifiers changed from: package-private */
    public void onConfigUpdated(String str, int i) {
        if (!this.mNetEvtCtr.onConfigUpdated(str, i)) {
            return;
        }
        if (this.mSimManagers.get(i).getSimMno().isKor()) {
            removeMessages(2, Integer.valueOf(i));
            sendMessageDelayed(obtainMessage(2, Integer.valueOf(i)), 500);
            return;
        }
        this.mRegMan.tryRegister(i);
    }

    public void removeRecoveryAction() {
        if (!hasMessages(134)) {
            return;
        }
        if (this.mHasPendingRecoveryAction) {
            this.mEventLog.logAndAdd("Do not remove RecoveryAction while pending");
        } else {
            removeMessages(134);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00aa, code lost:
        if (r1.equals(com.sec.internal.constants.ims.core.RegistrationConstants.RecoveryReason.NO_USER_AGENT) == false) goto L_0x0097;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void doRecoveryAction(com.sec.internal.ims.core.RegisterTask r7) {
        /*
            r6 = this;
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "doRecoveryAction; "
            r1.append(r2)
            java.lang.String r2 = r7.mRecoveryReason
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            int r1 = r7.getPhoneId()
            r0.append(r1)
            java.lang.String r1 = ",RECOVERY:"
            r0.append(r1)
            java.lang.String r1 = r7.mRecoveryReason
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r1 = 285278215(0x11010007, float:1.0176314E-28)
            r2 = 1
            com.sec.internal.log.IMSLog.c(r1, r0, r2)
            r0 = 0
            r3 = r0
        L_0x003b:
            int r4 = com.sec.internal.helper.SimUtil.getPhoneCount()
            r5 = -1
            if (r3 >= r4) goto L_0x0087
            com.sec.internal.ims.core.RegistrationManagerBase r4 = r6.mRegMan
            int r4 = r4.getTelephonyCallStatus(r3)
            if (r4 == r5) goto L_0x0084
            if (r4 == 0) goto L_0x0084
            r6.mHasPendingRecoveryAction = r2
            r6.mTaskPendingRecoveryAction = r7
            com.sec.internal.helper.SimpleEventLog r6 = r6.mEventLog
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r4 = "doRecoveryAction : active call in phoneId["
            r0.append(r4)
            r0.append(r3)
            java.lang.String r3 = "]. postpone recovery"
            r0.append(r3)
            java.lang.String r0 = r0.toString()
            r6.logAndAdd(r0)
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            int r7 = r7.getPhoneId()
            r6.append(r7)
            java.lang.String r7 = ",POSTPONE RECOVERY: ACTIVE CALL"
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.c(r1, r6, r2)
            return
        L_0x0084:
            int r3 = r3 + 1
            goto L_0x003b
        L_0x0087:
            r6.mHasPendingRecoveryAction = r0
            java.lang.String r1 = r7.getRecoveryReason()
            r1.hashCode()
            int r3 = r1.hashCode()
            switch(r3) {
                case -1839172045: goto L_0x00ad;
                case -746016487: goto L_0x00a4;
                case -604020475: goto L_0x0099;
                default: goto L_0x0097;
            }
        L_0x0097:
            r2 = r5
            goto L_0x00b7
        L_0x0099:
            java.lang.String r2 = "UACreateFailed"
            boolean r1 = r1.equals(r2)
            if (r1 != 0) goto L_0x00a2
            goto L_0x0097
        L_0x00a2:
            r2 = 2
            goto L_0x00b7
        L_0x00a4:
            java.lang.String r3 = "NoUserAgent"
            boolean r1 = r1.equals(r3)
            if (r1 != 0) goto L_0x00b7
            goto L_0x0097
        L_0x00ad:
            java.lang.String r2 = "UaStateMismatch"
            boolean r1 = r1.equals(r2)
            if (r1 != 0) goto L_0x00b6
            goto L_0x0097
        L_0x00b6:
            r2 = r0
        L_0x00b7:
            switch(r2) {
                case 0: goto L_0x00cf;
                case 1: goto L_0x00c5;
                case 2: goto L_0x00bb;
                default: goto L_0x00ba;
            }
        L_0x00ba:
            goto L_0x00d8
        L_0x00bb:
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r1 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.RECOVERY_UA_CREATION_FAIL
            int r1 = r1.getCode()
            r7.setRegiFailReason(r1)
            goto L_0x00d8
        L_0x00c5:
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r1 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.RECOVERY_UA_MISSING
            int r1 = r1.getCode()
            r7.setRegiFailReason(r1)
            goto L_0x00d8
        L_0x00cf:
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r1 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.RECOVERY_UA_MISMATCH
            int r1 = r1.getCode()
            r7.setRegiFailReason(r1)
        L_0x00d8:
            r7.clearUserAgent()
            com.sec.internal.ims.core.RegistrationManagerBase r1 = r6.mRegMan
            r1.reportRegistrationStatus(r7)
            r7 = 0
            r6.mTaskPendingRecoveryAction = r7
            com.sec.internal.helper.os.SystemWrapper.exit(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerHandler.doRecoveryAction(com.sec.internal.ims.core.RegisterTask):void");
    }

    /* access modifiers changed from: protected */
    public void setDeregisterTimeout(IRegisterTask iRegisterTask) {
        Mno mno = iRegisterTask.getMno();
        if (mno == Mno.H3G) {
            sendMessageDelayed(obtainMessage(107, iRegisterTask), (long) IRegistrationManager.getDeregistrationTimeout(iRegisterTask.getProfile(), 13));
        } else if ((mno.isKor() || mno.isOneOf(Mno.OPTUS, Mno.TELUS, Mno.KOODO)) && iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY) {
            Log.i(LOG_TAG, "KOR, OPTUS,KODO TELUS Emergency no need to Dereg Timer");
        } else if (mno == Mno.KDDI && this.mPdnController.isEpdgConnected(iRegisterTask.getPhoneId()) && iRegisterTask.getDeregiReason() == 72) {
            Log.i(LOG_TAG, "block Registration Retries for the T3402 Timer on Epdg");
        } else if (mno.isOneOf(Mno.ORANGE_POLAND, Mno.ORANGE_ROMANIA) && iRegisterTask.getDeregiReason() == 27) {
            Log.i(LOG_TAG, "EPDG Deregister, set as default dereg timeout");
            sendMessageDelayed(obtainMessage(107, iRegisterTask), 4000);
        } else if (mno == Mno.VZW && iRegisterTask.getDeregiReason() == 23) {
            Log.i(LOG_TAG, "APM/PWR OFF case. We don't have much time! Wait 2.5 sec!");
            sendMessageDelayed(obtainMessage(107, iRegisterTask), 2500);
        } else if (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY) {
            sendMessage(obtainMessage(107, iRegisterTask));
        } else {
            sendMessageDelayed(obtainMessage(107, iRegisterTask), (long) IRegistrationManager.getDeregistrationTimeout(iRegisterTask.getProfile(), iRegisterTask.getRegistrationRat()));
        }
    }

    public void notifyImsSettingUpdated(int i) {
        removeMessages(17, Integer.valueOf(i));
        sendMessage(obtainMessage(17, Integer.valueOf(i)));
    }

    public void notifySetupWizardCompleted() {
        for (int i = 0; i < this.mPhoneCount; i++) {
            Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
            while (true) {
                if (it.hasNext()) {
                    if (((RegisterTask) it.next()).isRcsOnly() && ConfigUtil.isRcsEur(i)) {
                        sendMessage(obtainMessage(RegistrationEvents.EVENT_SETUP_WIZARD_COMPLETED, Integer.valueOf(i)));
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    public void notifyRcsUserSettingChanged(int i, int i2) {
        sendMessage(obtainMessage(147, i2, -1, Integer.valueOf(i)));
    }

    public void notifyRoamingDataSettigChanged(int i, int i2) {
        sendMessage(obtainMessage(44, i, i2, (Object) null));
    }

    public void notifyImsSettingChanged(Uri uri, int i) {
        if (uri != null) {
            String path = uri.getPath();
            if (path.startsWith(ImsConstants.Uris.SETTINGS_PROVIDER_PROFILE_URI.getPath())) {
                removeMessages(15);
                sendMessage(obtainMessage(15, i, 0, (Object) null));
            } else if (path.startsWith(ImsConstants.Uris.SETTINGS_PROVIDER_DYNAMIC_IMS_UPDATE_URI.getPath()) || path.startsWith(ImsConstants.Uris.SETTINGS_PROVIDER_SMK_CONFIG_URI.getPath()) || path.startsWith(ImsConstants.Uris.SETTINGS_PROVIDER_SMK_CONFIG_RESET_URI.getPath())) {
                removeMessages(408);
                sendMessage(obtainMessage(408, i, 0, (Object) null));
                this.mImsFramework.getServiceModuleManager().forceCallOnServiceSwitched(i);
            }
        }
    }

    public void notifyMnoMapUpdated(Uri uri, int i) {
        if (uri != null) {
            removeMessages(148);
            sendMessage(obtainMessage(148, i, 0, (Object) null));
        }
    }

    public void notifyConfigChanged(Uri uri, int i) {
        sendMessage(obtainMessage(35, i, 0, uri != null ? uri.getLastPathSegment() : null));
    }

    public void notifyVowifiSettingChanged(int i, long j) {
        sendMessageDelayed(obtainMessage(122, i, 0, (Object) null), j);
    }

    public void notifyLteDataNetworkModeSettingChanged(boolean z, int i) {
        sendMessage(obtainMessage(50, z ? 1 : 0, i, (Object) null));
    }

    public void notifyLocationModeChanged() {
        sendMessage(obtainMessage(801));
    }

    public void notifyAirplaneModeChanged(int i) {
        sendMessage(obtainMessage(12, i, -1));
    }

    public void notifyMobileDataSettingeChanged(int i, int i2) {
        sendMessage(obtainMessage(34, i, i2, (Object) null));
    }

    public void notifyMobileDataPressedSettingeChanged(int i, int i2) {
        sendMessage(obtainMessage(153, i, i2, (Object) null));
    }

    public void notifyVolteSettingChanged(boolean z, boolean z2, int i) {
        sendMessage(obtainMessage(z2 ? 127 : 125, i, -1, Boolean.valueOf(z)));
    }

    public void notifyEcVbcSettingChanged(int i) {
        sendMessage(obtainMessage(MNO.TANGO_LUXEMBOURG, i, -1));
    }

    public void notifyChatbotAgreementChanged(int i) {
        sendMessage(obtainMessage(56, Integer.valueOf(i)));
    }

    public void notifyTriggeringRecoveryAction(IRegisterTask iRegisterTask, long j) {
        sendMessageDelayed(obtainMessage(134, iRegisterTask), j);
    }

    public void notifyVolteSettingOff(IRegisterTask iRegisterTask, long j) {
        sendMessageDelayed(obtainMessage(131, iRegisterTask), j);
    }

    public void notifyEmergencyReady(int i) {
        sendMessage(obtainMessage(119, i, -1));
    }

    public void notifyRegistered(int i, int i2, ImsRegistration imsRegistration) {
        RegisterTask registerTaskByProfileId = this.mRegMan.getRegisterTaskByProfileId(i2, i);
        if (registerTaskByProfileId != null) {
            registerTaskByProfileId.setImsRegistration(imsRegistration);
            sendMessage(obtainMessage(100, registerTaskByProfileId));
        }
    }

    public void notifyDeRegistered(Bundle bundle) {
        removeMessages(100, this.mRegMan.getRegisterTaskByProfileId(bundle.getInt("profileId"), bundle.getInt("phoneId")));
        sendMessage(obtainMessage(101, bundle));
    }

    public void notifyRegistrationError(Bundle bundle) {
        sendMessage(obtainMessage(104, bundle));
    }

    public void notifySubscribeError(Bundle bundle) {
        sendMessage(obtainMessage(108, bundle));
    }

    public void notifyRefreshRegNotification(int i) {
        sendMessage(obtainMessage(141, i, -1));
    }

    public void notifyContactActivated(int i, int i2) {
        sendMessage(obtainMessage(RegistrationEvents.EVENT_CONTACT_ACTIVATED, i, i2));
    }

    public void notifyRegEventContactUriNotification(Bundle bundle) {
        sendMessage(obtainMessage(RegistrationEvents.EVENT_REGEVENT_CONTACT_URI_NOTIFIED, bundle));
    }

    /* access modifiers changed from: protected */
    public void notifyPdnConnected(RegisterTask registerTask) {
        sendMessage(obtainMessage(22, registerTask));
    }

    /* access modifiers changed from: protected */
    public void notifyPdnDisconnected(RegisterTask registerTask) {
        sendMessage(obtainMessage(23, registerTask));
    }

    public void notifyLocalIpChanged(IRegisterTask iRegisterTask, boolean z) {
        int phoneId = iRegisterTask.getPhoneId();
        String acsServerType = ConfigUtil.getAcsServerType(phoneId);
        IMSLog.i(LOG_TAG, phoneId, "notifyLocalIpChanged: isStackedIpChanged [" + z + "], RCS AS [" + acsServerType + "]");
        if (z) {
            if (!iRegisterTask.isRcsOnly()) {
                return;
            }
            if (!ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(acsServerType) && (!iRegisterTask.getMno().isVodafone() || !RcsUtils.DualRcs.isDualRcsReg())) {
                return;
            }
        }
        sendMessage(obtainMessage(5, iRegisterTask));
        sendMessageDelayed(obtainMessage(2, Integer.valueOf(iRegisterTask.getPhoneId())), RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
    }

    public void notifyX509CertVerificationRequested(X509Certificate[] x509CertificateArr) {
        sendMessage(obtainMessage(30, x509CertificateArr));
    }

    public void notifyDnsResponse(List<String> list, int i, int i2) {
        sendMessage(obtainMessage(57, i, i2, list));
    }

    /* access modifiers changed from: protected */
    public void notifyManualRegisterRequested(List<Integer> list, int i) {
        for (Integer intValue : list) {
            ImsProfile profile = ImsProfileLoaderInternal.getProfile(this.mContext, intValue.intValue(), i);
            if (profile != null) {
                sendMessage(obtainMessage(9, i, 0, profile));
            }
        }
    }

    /* access modifiers changed from: protected */
    public int notifyManualRegisterRequested(ImsProfile imsProfile, boolean z, int i) {
        if (!imsProfile.isProper()) {
            return -1;
        }
        if (z) {
            imsProfile.setAppId("D;" + imsProfile.getAppId() + ";" + imsProfile.getDisplayName().replaceAll("[^a-zA-Z0-9\\s]", ""));
            imsProfile.setDisplayName("");
        }
        if (imsProfile.getCmcType() < 3) {
            imsProfile.setId(allocateAdhocProfileId(i));
        }
        sendMessage(obtainMessage(9, i, 0, imsProfile));
        ImsUtil.notifyImsProfileLoaded(this.mContext, i);
        Log.i(LOG_TAG, "registerProfile: id " + imsProfile.getId());
        return imsProfile.getId();
    }

    private int allocateAdhocProfileId(int i) {
        Integer num = this.mAdhocProfileCounter.get(i);
        if (num.intValue() < 0 || num.intValue() > 19999) {
            num = 10000;
        }
        this.mAdhocProfileCounter.put(i, Integer.valueOf(num.intValue() + 1));
        return num.intValue() + (i * 10000);
    }

    /* access modifiers changed from: protected */
    public void notifyManualDeRegisterRequested(List<Integer> list, boolean z, int i) {
        for (Integer intValue : list) {
            int intValue2 = intValue.intValue();
            if (ImsProfileLoaderInternal.getProfile(this.mContext, intValue2, i) != null) {
                Bundle bundle = new Bundle();
                bundle.putInt("id", intValue2);
                bundle.putBoolean("explicitDeregi", z);
                bundle.putInt("phoneId", i);
                sendMessage(obtainMessage(10, bundle));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyManualDeRegisterRequested(int i, int i2) {
        notifyManualDeRegisterRequested(i, i2, true);
    }

    /* access modifiers changed from: protected */
    public void notifyManualDeRegisterRequested(int i, int i2, boolean z) {
        if (SimUtil.isSoftphoneEnabled()) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(i2, "deregisterProfile : " + i);
            IMSLog.c(LogClass.REGI_DEREGISTER_PROFILE, i2 + ",DEREG REQ:" + i);
        }
        Bundle bundle = new Bundle();
        bundle.putInt("id", i);
        bundle.putBoolean("explicitDeregi", z);
        bundle.putInt("phoneId", i2);
        sendMessage(obtainMessage(10, bundle));
    }

    /* access modifiers changed from: protected */
    public int notifyUpdateRegisterRequested(ImsProfile imsProfile, int i) {
        if (imsProfile == null) {
            return -1;
        }
        sendMessage(obtainMessage(25, i, -1, imsProfile));
        return 0;
    }

    /* access modifiers changed from: protected */
    public void notifySendDeRegisterRequested(Mno mno, int i, int i2) {
        post(new RegistrationManagerHandler$$ExternalSyntheticLambda18(this, i2, mno, i));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifySendDeRegisterRequested$15(int i, Mno mno, int i2) {
        SlotBasedConfig.RegisterTaskList registrationTasks = SlotBasedConfig.getInstance(i).getRegistrationTasks();
        if (registrationTasks != null) {
            Iterator it = registrationTasks.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                int pdnType = registerTask.getPdnType();
                boolean z = true;
                boolean z2 = false;
                if (!registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING) || pdnType == 15) {
                    if (i2 == 130 && registerTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                        Log.i(LOG_TAG, "Stop pdn when device shut down...");
                        this.mRegMan.stopPdnConnectivity(pdnType, registerTask);
                        registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    } else if (i2 == 124 && registerTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED && pdnType == 11) {
                        boolean isSupportVoWiFiDisable5GSA = this.mRegMan.isSupportVoWiFiDisable5GSA(i);
                        boolean hasVolteService = ImsProfile.hasVolteService(registerTask.getProfile(), 20);
                        if (this.mImsFramework.getWfcEpdgManager().getNrInterworkingMode(i) == ImsConstants.NrInterworking.FULL_SUPPORT) {
                            z = false;
                        }
                        if (isSupportVoWiFiDisable5GSA || (hasVolteService && z)) {
                            Log.i(LOG_TAG, "TaskState is CONNECTED. Received Epdg Deregister Request.");
                            this.mRegMan.stopPdnConnectivity(pdnType, registerTask);
                            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                            sendTryRegister(i, 500);
                        }
                    }
                } else if (!RegistrationUtils.ignoreSendDeregister(i, mno, registerTask, i2)) {
                    if ("".equals(registerTask.getReason())) {
                        registerTask.setReason("sendDeregister : " + i2);
                    }
                    registerTask.setDeregiCause(i2);
                    if (registerTask.getDeregiReason() == 31) {
                        removeMessages(49);
                        sendMessageDelayed(obtainMessage(49), 6000);
                    }
                    boolean booleanValue = ((Boolean) Optional.ofNullable(SlotBasedConfig.getInstance(i).getNetworkEvent()).map(new RegistrationManagerHandler$$ExternalSyntheticLambda16()).orElse(Boolean.FALSE)).booleanValue();
                    if ((i2 == 1000 && mno.isKor()) || i2 == 143 || i2 == 12) {
                        this.mRegMan.tryDeregisterInternal(registerTask, false, true);
                    } else if (i2 == 807) {
                        RegistrationManagerBase registrationManagerBase = this.mRegMan;
                        if (mno != Mno.USCC) {
                            z2 = true;
                        }
                        registrationManagerBase.tryDeregisterInternal(registerTask, true, z2);
                    } else if ((mno == Mno.UMOBILE || mno == Mno.DIGI) && i2 == 124 && !booleanValue) {
                        Log.i(LOG_TAG, "Wifi disconnected, send local deregister");
                        this.mRegMan.tryDeregisterInternal(registerTask, true, false);
                    } else {
                        this.mRegMan.tryDeregisterInternal(registerTask, false, false);
                    }
                }
            }
        }
        removeMessages(42, Integer.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public void notifySendReRegisterRequested(RegisterTask registerTask) {
        post(new RegistrationManagerHandler$$ExternalSyntheticLambda17(this, registerTask));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifySendReRegisterRequested$16(RegisterTask registerTask) {
        if (registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            boolean z = true;
            registerTask.setUpdateRegistering(true);
            if (!ConfigUtil.isRcsEur(registerTask.getPhoneId()) || !registerTask.isRcsOnly()) {
                z = false;
            }
            Set<String> serviceForNetwork = this.mRegMan.getServiceForNetwork(registerTask.getProfile(), registerTask.getRegistrationRat(), z, registerTask.getPhoneId());
            if (CollectionUtils.isNullOrEmpty((Collection<?>) serviceForNetwork)) {
                Log.i(LOG_TAG, "sendReRegister : deregister task due to empty services");
                this.mRegMan.tryDeregisterInternal(registerTask, false, false);
                return;
            }
            this.mRegMan.registerInternal(registerTask, registerTask.getGovernor().getCurrentPcscfIp(), serviceForNetwork);
        }
    }

    /* access modifiers changed from: protected */
    public void requestDelayedDeRegister(IRegisterTask iRegisterTask, boolean z, long j) {
        sendMessageDelayed(obtainMessage(145, z ? 1 : 0, -1, iRegisterTask), j);
    }

    /* access modifiers changed from: protected */
    public void updateSipDelegateRegistration(int i, boolean z) {
        if (z) {
            obtainMessage(58, Integer.valueOf(i)).sendToTarget();
            removeMessages(59, Integer.valueOf(i));
            sendMessageDelayed(obtainMessage(59, Integer.valueOf(i)), 5000);
            return;
        }
        this.mEventLog.logAndAdd(i, "updateSipDelegateRegistration: Send delayed update");
        sendMessageDelayed(obtainMessage(58, Integer.valueOf(i)), 5000);
    }

    public void registerDmListener(IImsDmConfigListener iImsDmConfigListener) {
        Log.i(LOG_TAG, "registerListener: " + iImsDmConfigListener);
        synchronized (this.mImsDmConfigListener) {
            if (iImsDmConfigListener != null) {
                this.mImsDmConfigListener.register(iImsDmConfigListener);
            }
        }
    }

    public void unregisterDmListener(IImsDmConfigListener iImsDmConfigListener) {
        Log.i(LOG_TAG, "unregisterListener: " + iImsDmConfigListener);
        synchronized (this.mImsDmConfigListener) {
            if (iImsDmConfigListener != null) {
                this.mImsDmConfigListener.unregister(iImsDmConfigListener);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyDmValueChanged(String str, int i) {
        IMSLog.i(LOG_TAG, i, "notifyDmValueChanged:");
        try {
            int beginBroadcast = this.mImsDmConfigListener.beginBroadcast();
            boolean onDmState = onDmState(str, i);
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                this.mImsDmConfigListener.getBroadcastItem(i2).onChangeDmValue(str, onDmState);
            }
            this.mImsDmConfigListener.finishBroadcast();
        } catch (RemoteException | IllegalStateException | NullPointerException e) {
            e.printStackTrace();
        }
        Context context = this.mContext;
        String read = DmConfigHelper.read(context, "omadm/./3GPP_IMS/" + str, "", i);
        IMSLog.i(LOG_TAG, i, "item : " + str + ", value : " + IMSLog.checker(read));
        if (!TextUtils.isEmpty(str) && !TextUtils.isEmpty(read)) {
            Intent intent = new Intent(ImsConstants.Intents.ACTION_DM_CHANGED);
            intent.putExtra(ImsConstants.Intents.EXTRA_UPDATED_ITEM, str);
            intent.putExtra("value", read);
            intent.putExtra("phoneId", i);
            this.mContext.sendBroadcast(intent);
        }
    }

    private boolean onDmState(String str, int i) {
        boolean z;
        IMSLog.i(LOG_TAG, i, "onDmState:");
        if ("EAB_SETTING".equalsIgnoreCase(str)) {
            z = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, Boolean.FALSE, i).booleanValue();
        } else if ("LVC_ENABLED".equalsIgnoreCase(str)) {
            z = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_LVC_ENABLED, Boolean.FALSE, i).booleanValue();
        } else if ("VOLTE_ENABLED".equalsIgnoreCase(str)) {
            z = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, Boolean.FALSE, i).booleanValue();
        } else {
            IMSLog.i(LOG_TAG, i, "Ignore DM value");
            z = false;
        }
        IMSLog.i(LOG_TAG, i, "new onDmState: " + str + "- state: " + z);
        return z;
    }

    /* access modifiers changed from: protected */
    public DelayedMessage startPreciseAlarmTimer(int i, IRegisterTask iRegisterTask, long j) {
        return this.mPreAlarmMgr.sendMessageDelayed(getClass().getSimpleName(), obtainMessage(i, iRegisterTask.getPhoneId(), -1, iRegisterTask), j);
    }

    public void stopTimer(Message message) {
        this.mPreAlarmMgr.removeMessage(message);
    }

    public void stopTimer(DelayedMessage delayedMessage) {
        this.mPreAlarmMgr.removeMessage(delayedMessage);
    }

    public void sendUpdateRegistration(ImsProfile imsProfile, int i, long j) {
        sendMessageDelayed(obtainMessage(25, i, -1, imsProfile), j);
    }

    public void sendDisconnectPdnByVolteDisabled(RegisterTask registerTask, long j) {
        sendMessageDelayed(obtainMessage(RegistrationEvents.EVENT_DISCONNECT_PDN_BY_VOLTE_DISABLED, registerTask), j);
    }

    public void sendFinishOmadmProvisioningUpdate(IRegisterTask iRegisterTask, long j) {
        sendMessageDelayed(obtainMessage(39, iRegisterTask), j);
    }

    public Message startDisconnectPdnTimer(IRegisterTask iRegisterTask, long j) {
        return startPreciseAlarmTimer(404, iRegisterTask, j).getMsg();
    }

    public Message startDmConfigTimer(RegisterTask registerTask, long j) {
        return startPreciseAlarmTimer(28, registerTask, j).getMsg();
    }

    public void requestForcedUpdateRegistration(IRegisterTask iRegisterTask, long j) {
        sendMessageDelayed(obtainMessage(140, iRegisterTask), j);
    }

    public void requestPendingDeregistration(IRegisterTask iRegisterTask, boolean z, boolean z2, long j) {
        sendMessageDelayed(obtainMessage(120, z ? 1 : 0, z2 ? 1 : 0, iRegisterTask), j);
    }

    public void sendRequestDmConfig(RegisterTask registerTask) {
        sendMessage(obtainMessage(28, registerTask));
    }

    public void sendCheckUnprocessedOmadmConfig(RegisterTask registerTask) {
        sendMessage(obtainMessage(RegistrationEvents.EVENT_CHECK_UNPROCESSED_OMADM_CONFIG, registerTask));
    }

    public Message startLocationRequestTimer(IRegisterTask iRegisterTask, long j) {
        return startPreciseAlarmTimer(800, iRegisterTask, j).getMsg();
    }

    public boolean hasVolteSettingOffEvent() {
        return hasMessages(131);
    }

    public void removeVolteSettingOffEvent() {
        removeMessages(131);
    }

    public void sendDmConfigTimeout(int i, String str) {
        this.mPreAlarmMgr.sendMessageDelayed(str, obtainMessage(43, i, 0), 30000);
    }

    public void removeDmConfigTimeout(int i) {
        this.mPreAlarmMgr.removeMessage(obtainMessage(43, i, 0));
    }

    public boolean hasDelayedStopPdnEvent() {
        return hasMessages(133);
    }

    public boolean hasNetworModeChangeEvent() {
        return hasMessages(49);
    }

    public Message startRegistrationTimer(IRegisterTask iRegisterTask, long j) {
        return startPreciseAlarmTimer(4, iRegisterTask, j).getMsg();
    }

    /* access modifiers changed from: protected */
    public DelayedMessage startTimsEshtablishTimer(RegisterTask registerTask, long j) {
        if (registerTask.getGovernor().isMobilePreferredForRcs()) {
            return startPreciseAlarmTimer(152, registerTask, j);
        }
        return startPreciseAlarmTimer(132, registerTask, j);
    }

    public void sendOmadmProvisioningUpdateStarted(IRegisterTask iRegisterTask) {
        sendMessageAtFrontOfQueue(obtainMessage(38, iRegisterTask));
    }

    public void sendTryRegister(int i) {
        sendMessage(obtainMessage(2, Integer.valueOf(i)));
    }

    public void sendTryRegister(int i, long j) {
        sendMessageDelayed(obtainMessage(2, Integer.valueOf(i)), j);
    }

    public void sendSuspend(RegisterTask registerTask, boolean z, int i) {
        sendMessage(obtainMessage(151, z ? 1 : 0, i, registerTask));
    }

    public void requestTryEmergencyRegister(RegisterTask registerTask) {
        sendMessage(obtainMessage(118, registerTask));
    }
}
