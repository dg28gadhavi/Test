package com.sec.internal.ims.servicemodules.volte2;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.telephony.BarringInfo;
import android.telephony.CellIdentityNr;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseDataConnectionState;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.StringBuilderPrinter;
import com.android.internal.telephony.Call;
import com.sec.epdg.EpdgManager;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.WiFiManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.helper.os.SystemWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.WfcEpdgManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;
import com.sec.internal.ims.servicemodules.volte2.data.EcholocateEvent;
import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcServiceHelper;
import com.sec.internal.ims.servicemodules.volte2.vcid.VcidHelper;
import com.sec.internal.ims.util.ImsPhoneStateManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.xq.att.ImsXqReporter;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.core.IWfcEpdgManager;
import com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IIdcServiceHelper;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VolteServiceModuleInternal extends ServiceModuleBase implements IVolteServiceModuleInternal {
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    protected static final int RELAY_CHANNEL_ESTABLISHED = 0;
    protected static final int RELAY_CHANNEL_TERMINATED = 1;
    protected ImsUri[] mActiveImpu;
    protected long[] mAllowedNetworkType;
    protected AllowedNetworkTypesListener[] mAllowedNetworkTypesListener;
    protected boolean[] mAutomaticMode;
    protected boolean mCheckRunningState = false;
    protected ICmcMediaController mCmcMediaController;
    protected CmcServiceHelper mCmcServiceModule;
    protected final Context mContext;
    protected int[] mDataAccessNetwork;
    private boolean mDelayRinging = false;
    protected boolean[] mEcbmMode;
    protected TmoEcholocateController mEcholocateController = null;
    protected boolean mEnableCallWaitingRule = true;
    protected ImsManager.EpdgListener mEpdgListener = null;
    protected final Map<Integer, Message> mEpdnDisconnectTimeOut = new ArrayMap();
    protected SimpleEventLog mEventLog;
    protected IdcServiceHelper mIdcServiceModule;
    protected ImsCallSessionManager mImsCallSessionManager;
    protected ImsCallSipErrorFactory mImsCallSipErrorFactory;
    protected ImsExternalCallController mImsExternalCallController;
    protected ImsXqReporter mImsXqReporter = null;
    private boolean[] mIsDeregisterTimerRunning;
    protected boolean[] mIsLteEpsOnlyAttached;
    private boolean[] mIsLteRetrying;
    private boolean[] mIsMissedCallSmsChecking;
    protected DialogEvent[] mLastDialogEvent;
    protected int[] mLastRegiErrorCode;
    private int mMaxPhoneCount = 1;
    protected IImsMediaController mMediaController;
    protected IMediaServiceInterface mMediaSvcIntf;
    private BroadcastReceiver mMissedSmsIntentReceiver = null;
    protected boolean mMmtelAcquiredEver = false;
    protected MobileCareController mMobileCareController;
    protected Map<Integer, NetworkEvent> mNetworks = new ConcurrentHashMap();
    protected IOptionsServiceInterface mOptionsSvcIntf;
    protected final IPdnController mPdnController;
    protected final List<PhoneStateListenerInternal> mPhoneStateListener = new ArrayList();
    protected final ImsPhoneStateManager mPhoneStateManager;
    protected boolean[] mProhibited;
    protected boolean[] mRatChanged;
    protected final IRegistrationManager mRegMan;
    protected boolean[] mReleaseWfcBeforeHO;
    protected int[] mRttMode;
    private RttSettingObserver mRttSettingObserver = null;
    protected final List<? extends ISimManager> mSimManagers;
    protected SsacManager mSsacManager;
    protected final ITelephonyManager mTelephonyManager;
    protected int[] mTtyMode;
    protected VolteNotifier mVolteNotifier;
    protected IVolteServiceInterface mVolteSvcIntf;
    private WfcEpdgManager.WfcEpdgConnectionListener mWfcEpdgConnectionListener = null;
    protected IWfcEpdgManager mWfcEpdgMgr = null;

    public ICmcMediaController getCmcMediaController() {
        return null;
    }

    public IImsMediaController getImsMediaController() {
        return null;
    }

    public String[] getServicesRequiring() {
        return new String[0];
    }

    public void handleIntent(Intent intent) {
    }

    public void onConferenceParticipantAdded(int i, String str) {
    }

    public void onConferenceParticipantRemoved(int i, String str) {
    }

    public void onSendRttSessionModifyRequest(int i, boolean z) {
    }

    public void onSendRttSessionModifyResponse(int i, boolean z, boolean z2) {
    }

    public void updateCmcP2pList(ImsRegistration imsRegistration, CallProfile callProfile) {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public VolteServiceModuleInternal(Looper looper, Context context, IRegistrationManager iRegistrationManager, IPdnController iPdnController, IVolteServiceInterface iVolteServiceInterface, IMediaServiceInterface iMediaServiceInterface, IOptionsServiceInterface iOptionsServiceInterface) {
        super(looper);
        Context context2 = context;
        IRegistrationManager iRegistrationManager2 = iRegistrationManager;
        IPdnController iPdnController2 = iPdnController;
        this.mContext = context2;
        this.mEventLog = new SimpleEventLog(context2, IVolteServiceModuleInternal.NAME, 100);
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
        this.mVolteSvcIntf = iVolteServiceInterface;
        this.mMediaSvcIntf = iMediaServiceInterface;
        this.mOptionsSvcIntf = iOptionsServiceInterface;
        List<? extends ISimManager> allSimManagers = SimManagerFactory.getAllSimManagers();
        this.mSimManagers = allSimManagers;
        int size = allSimManagers.size();
        int phoneCount = SimUtil.getPhoneCount();
        this.mMaxPhoneCount = phoneCount;
        boolean[] zArr = new boolean[size];
        this.mProhibited = zArr;
        this.mIsLteEpsOnlyAttached = new boolean[size];
        this.mRatChanged = new boolean[size];
        this.mEcbmMode = new boolean[size];
        this.mLastDialogEvent = new DialogEvent[size];
        this.mActiveImpu = new ImsUri[size];
        this.mTtyMode = new int[size];
        this.mRttMode = new int[size];
        this.mAutomaticMode = new boolean[size];
        this.mReleaseWfcBeforeHO = new boolean[size];
        this.mLastRegiErrorCode = new int[size];
        this.mDataAccessNetwork = new int[size];
        this.mIsDeregisterTimerRunning = new boolean[size];
        this.mIsMissedCallSmsChecking = new boolean[phoneCount];
        this.mAllowedNetworkTypesListener = new AllowedNetworkTypesListener[size];
        this.mAllowedNetworkType = new long[size];
        this.mIsLteRetrying = new boolean[size];
        Arrays.fill(zArr, false);
        Arrays.fill(this.mIsLteEpsOnlyAttached, false);
        Arrays.fill(this.mRatChanged, false);
        Arrays.fill(this.mEcbmMode, false);
        Arrays.fill(this.mLastDialogEvent, (Object) null);
        Arrays.fill(this.mActiveImpu, (Object) null);
        Arrays.fill(this.mTtyMode, Extensions.TelecomManager.TTY_MODE_OFF);
        Arrays.fill(this.mRttMode, -1);
        Arrays.fill(this.mAutomaticMode, false);
        Arrays.fill(this.mReleaseWfcBeforeHO, false);
        Arrays.fill(this.mLastRegiErrorCode, 0);
        Arrays.fill(this.mIsDeregisterTimerRunning, false);
        Arrays.fill(this.mIsMissedCallSmsChecking, false);
        Arrays.fill(this.mAllowedNetworkType, -1);
        Arrays.fill(this.mAllowedNetworkTypesListener, (Object) null);
        Arrays.fill(this.mIsLteRetrying, false);
        this.mVolteSvcIntf.registerForIncomingCallEvent(this, 1, (Object) null);
        this.mVolteSvcIntf.registerForCallStateEvent(this, 2, (Object) null);
        this.mVolteSvcIntf.registerForDialogEvent(this, 3, (Object) null);
        this.mVolteSvcIntf.registerForDedicatedBearerNotifyEvent(this, 8, (Object) null);
        this.mVolteSvcIntf.registerQuantumSecurityStatusEvent(this, 38, (Object) null);
        this.mVolteSvcIntf.registerForDtmfEvent(this, 17, (Object) null);
        this.mVolteSvcIntf.registerForTextEvent(this, 22, (Object) null);
        this.mVolteSvcIntf.registerForSIPMSGEvent(this, 25, (Object) null);
        this.mVolteSvcIntf.registerForRtpLossRateNoti(this, 18, (Object) null);
        SimManagerFactory.registerForSubIdChange(this, 24, (Object) null);
        this.mPhoneStateManager = new ImsPhoneStateManager(context2, -2147462879);
        for (ISimManager iSimManager : allSimManagers) {
            PhoneStateListenerInternal phoneStateListenerInternal = new PhoneStateListenerInternal(iSimManager.getSimSlotIndex(), iSimManager.getSubscriptionId());
            this.mPhoneStateListener.add(phoneStateListenerInternal);
            this.mPhoneStateManager.registerListener(phoneStateListenerInternal, iSimManager.getSubscriptionId(), iSimManager.getSimSlotIndex());
            iSimManager.registerForSimReady(this, 30, (Object) null);
            iSimManager.registerForSimRemoved(this, 31, (Object) null);
            this.mNetworks.put(Integer.valueOf(iSimManager.getSimSlotIndex()), new NetworkEvent());
        }
        this.mSsacManager = new SsacManager(getLooper(), this, iRegistrationManager2, size);
        this.mEcholocateController = new TmoEcholocateController(this.mContext, this, iPdnController, size, getLooper());
        this.mRegMan = iRegistrationManager2;
        this.mPdnController = iPdnController2;
        this.mMediaController = new ImsMediaController(this, getLooper(), this.mContext, this.mEventLog);
        this.mMobileCareController = new MobileCareController(this.mContext);
        this.mImsCallSessionManager = new ImsCallSessionManager(this, this.mTelephonyManager, iPdnController, iRegistrationManager, getLooper());
        this.mImsCallSipErrorFactory = new ImsCallSipErrorFactory(this, this.mTelephonyManager, iPdnController2, iRegistrationManager2);
        int i = size;
        this.mCmcServiceModule = new CmcServiceHelper(looper, this.mContext, this.mRegistrationList, this.mVolteSvcIntf, this.mMediaController, this.mImsCallSessionManager, this.mOptionsSvcIntf, i);
        this.mCmcMediaController = new CmcMediaController(this, getLooper(), this.mImsCallSessionManager, this.mEventLog);
        this.mImsExternalCallController = new ImsExternalCallController(this);
        this.mIdcServiceModule = new IdcServiceHelper(this, looper, this.mContext, this.mImsCallSessionManager);
        this.mVolteNotifier = new VolteNotifier();
        setRttMode(ImsUtil.isRttModeOnFromCallSettings(this.mContext, 0) ? Extensions.TelecomManager.RTT_MODE : Extensions.TelecomManager.TTY_MODE_OFF);
        setTtyMode(0, Settings.System.getInt(this.mContext.getContentResolver(), "current_tty_mode", 0));
        if (i > 1) {
            setTtyMode(1, Settings.System.getInt(this.mContext.getContentResolver(), "current_tty_mode2", 0));
        }
        this.mRttSettingObserver = new RttSettingObserver(this.mContext, this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        intentFilter.addAction(IVolteServiceModuleInternal.INTENT_ACTION_LTE_BAND_CHANGED);
        intentFilter.addAction(IVolteServiceModuleInternal.ACTION_EMERGENCY_CALLBACK_MODE_INTERNAL);
        intentFilter.addAction(IVolteServiceModuleInternal.INTENT_ACTION_PS_BARRED);
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction(IVolteServiceModuleInternal.INTENT_ACTION_IQISERVICE_STATE_CHNAGED);
        this.mWfcEpdgMgr = ImsRegistry.getWfcEpdgManager();
        registerEpdgConnectionListener();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* JADX WARNING: Can't fix incorrect switch cases order */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(android.content.Context r6, android.content.Intent r7) {
                /*
                    r5 = this;
                    java.lang.String r6 = r7.getAction()
                    r6.hashCode()
                    int r0 = r6.hashCode()
                    r1 = 6
                    r2 = 1
                    r3 = 0
                    r4 = -1
                    switch(r0) {
                        case -2128145023: goto L_0x0056;
                        case -2065845397: goto L_0x004b;
                        case -1926447105: goto L_0x0040;
                        case -1840251113: goto L_0x0035;
                        case -1664867553: goto L_0x002a;
                        case -1454123155: goto L_0x001f;
                        case 1119176030: goto L_0x0014;
                        default: goto L_0x0012;
                    }
                L_0x0012:
                    r6 = r4
                    goto L_0x0060
                L_0x0014:
                    java.lang.String r0 = "com.samsung.intent.action.LTE_BAND"
                    boolean r6 = r6.equals(r0)
                    if (r6 != 0) goto L_0x001d
                    goto L_0x0012
                L_0x001d:
                    r6 = r1
                    goto L_0x0060
                L_0x001f:
                    java.lang.String r0 = "android.intent.action.SCREEN_ON"
                    boolean r6 = r6.equals(r0)
                    if (r6 != 0) goto L_0x0028
                    goto L_0x0012
                L_0x0028:
                    r6 = 5
                    goto L_0x0060
                L_0x002a:
                    java.lang.String r0 = "com.samsung.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED_INTERNAL"
                    boolean r6 = r6.equals(r0)
                    if (r6 != 0) goto L_0x0033
                    goto L_0x0012
                L_0x0033:
                    r6 = 4
                    goto L_0x0060
                L_0x0035:
                    java.lang.String r0 = "com.samsung.intent.action.PSBARRED_FOR_VOLTE"
                    boolean r6 = r6.equals(r0)
                    if (r6 != 0) goto L_0x003e
                    goto L_0x0012
                L_0x003e:
                    r6 = 3
                    goto L_0x0060
                L_0x0040:
                    java.lang.String r0 = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED"
                    boolean r6 = r6.equals(r0)
                    if (r6 != 0) goto L_0x0049
                    goto L_0x0012
                L_0x0049:
                    r6 = 2
                    goto L_0x0060
                L_0x004b:
                    java.lang.String r0 = "com.att.iqi.action.SERVICE_STATE_CHANGED"
                    boolean r6 = r6.equals(r0)
                    if (r6 != 0) goto L_0x0054
                    goto L_0x0012
                L_0x0054:
                    r6 = r2
                    goto L_0x0060
                L_0x0056:
                    java.lang.String r0 = "android.intent.action.SCREEN_OFF"
                    boolean r6 = r6.equals(r0)
                    if (r6 != 0) goto L_0x005f
                    goto L_0x0012
                L_0x005f:
                    r6 = r3
                L_0x0060:
                    r0 = 23
                    switch(r6) {
                        case 0: goto L_0x00d1;
                        case 1: goto L_0x00bf;
                        case 2: goto L_0x0097;
                        case 3: goto L_0x007f;
                        case 4: goto L_0x0097;
                        case 5: goto L_0x0075;
                        case 6: goto L_0x0067;
                        default: goto L_0x0065;
                    }
                L_0x0065:
                    goto L_0x00da
                L_0x0067:
                    java.lang.String r6 = "BAND"
                    java.lang.String r6 = r7.getStringExtra(r6)
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r5 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    com.sec.internal.ims.servicemodules.volte2.MobileCareController r5 = r5.mMobileCareController
                    r5.onLteBancChanged(r6)
                    goto L_0x00da
                L_0x0075:
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r5 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    android.os.Message r6 = r5.obtainMessage(r0, r2, r4)
                    r5.sendMessage(r6)
                    goto L_0x00da
                L_0x007f:
                    java.lang.String r6 = "cmd"
                    java.lang.String r6 = r7.getStringExtra(r6)
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r5 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    java.lang.String r7 = "1"
                    boolean r6 = r7.equals(r6)
                    r7 = 14
                    android.os.Message r6 = r5.obtainMessage(r7, r6, r4)
                    r5.sendMessage(r6)
                    goto L_0x00da
                L_0x0097:
                    java.lang.String r6 = "android.telephony.extra.PHONE_IN_ECM_STATE"
                    boolean r6 = r7.getBooleanExtra(r6, r3)
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r0 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    int r0 = r0.mActiveDataPhoneId
                    java.lang.String r4 = "phone"
                    int r7 = r7.getIntExtra(r4, r0)
                    if (r6 == 0) goto L_0x00b5
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r5 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    android.os.Message r6 = r5.obtainMessage(r1, r7, r2)
                    r5.sendMessage(r6)
                    goto L_0x00da
                L_0x00b5:
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r5 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    android.os.Message r6 = r5.obtainMessage(r1, r7, r3)
                    r5.sendMessage(r6)
                    goto L_0x00da
                L_0x00bf:
                    java.lang.String r6 = "com.att.iqi.extra.SERVICE_RUNNING"
                    boolean r6 = r7.getBooleanExtra(r6, r3)
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r5 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    r7 = 28
                    android.os.Message r6 = r5.obtainMessage(r7, r6, r4)
                    r5.sendMessage(r6)
                    goto L_0x00da
                L_0x00d1:
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r5 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    android.os.Message r6 = r5.obtainMessage(r0, r3, r4)
                    r5.sendMessage(r6)
                L_0x00da:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
            }
        }, intentFilter);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                action.hashCode();
                if (action.equals(IVolteServiceModuleInternal.INTENT_ACTION_TELEPHONY_NOT_RESPONDING)) {
                    Log.i(IVolteServiceModuleInternal.LOG_TAG, "receive INTENT_ACTION_TELEPHONY_NOT_RESPONDING");
                    VolteServiceModuleInternal.this.onTelephonyNotResponding();
                }
            }
        }, new IntentFilter(IVolteServiceModuleInternal.INTENT_ACTION_TELEPHONY_NOT_RESPONDING), "com.sec.imsservice.TELEPHONY_NOT_RESPONDING", (Handler) null);
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "VolteServiceModule created");
    }

    public void init() {
        super.init();
        this.mRttSettingObserver.init();
        this.mCmcServiceModule.init();
        this.mIdcServiceModule.init();
    }

    public ImsCallSession createSession(CallProfile callProfile) throws RemoteException {
        return this.mImsCallSessionManager.createSession(this.mContext, callProfile, callProfile == null ? null : getImsRegistration(callProfile.getPhoneId()));
    }

    public ImsCallSession createSession(CallProfile callProfile, int i) throws RemoteException {
        return this.mImsCallSessionManager.createSession(this.mContext, callProfile, getRegInfo(i));
    }

    public boolean isEmergencyRegistered(int i) {
        return getImsRegistration(i, true) != null;
    }

    public boolean isEcbmMode(int i) {
        return this.mEcbmMode[i];
    }

    private PhoneStateListenerInternal getPhoneStateListener(int i) {
        for (PhoneStateListenerInternal next : this.mPhoneStateListener) {
            if (next.getInternalPhoneId() == i) {
                return next;
            }
        }
        IMSLog.i(IVolteServiceModuleInternal.LOG_TAG, i, "getPhoneStateListener: psli is not exist.");
        return null;
    }

    /* access modifiers changed from: protected */
    public void registerPhoneStateListener(int i) {
        IMSLog.i(IVolteServiceModuleInternal.LOG_TAG, i, "registerPhoneStateListener:");
        int subId = SimUtil.getSubId(i);
        if (subId >= 0) {
            PhoneStateListenerInternal phoneStateListenerInternal = new PhoneStateListenerInternal(i, subId);
            if (getPhoneStateListener(i) == null) {
                this.mPhoneStateListener.add(phoneStateListenerInternal);
            }
            this.mPhoneStateManager.registerListener(phoneStateListenerInternal, subId, i);
        }
    }

    /* access modifiers changed from: protected */
    public void onActiveDataSubscriptionChanged() {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "onActiveDataSubscriptionChanged");
        for (ISimManager simSlotIndex : this.mSimManagers) {
            int simSlotIndex2 = simSlotIndex.getSimSlotIndex();
            unRegisterPhoneStateListener(simSlotIndex2);
            if (simSlotIndex2 == SimUtil.getActiveDataPhoneId()) {
                registerPhoneStateListener(simSlotIndex2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void unRegisterPhoneStateListener(int i) {
        IMSLog.i(IVolteServiceModuleInternal.LOG_TAG, i, "unRegisterPhoneStateListener:");
        this.mPhoneStateManager.unRegisterListener(i);
        PhoneStateListenerInternal phoneStateListener = getPhoneStateListener(i);
        if (phoneStateListener != null) {
            this.mPhoneStateListener.remove(phoneStateListener);
        }
    }

    public void dump() {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        IMSLog.dump(str, "Dump of " + str + ":");
        IMSLog.increaseIndent(str);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(str);
        IMSLog.increaseIndent(str);
        for (ImsCallSession imsCallSession : getSessionList()) {
            IMSLog.dump(IVolteServiceModuleInternal.LOG_TAG, imsCallSession.smCallStateMachine.toString());
        }
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        IMSLog.decreaseIndent(str2);
        Looper looper = getLooper();
        if (looper != null) {
            IMSLog.increaseIndent(str2);
            StringBuilder sb = new StringBuilder();
            looper.dump(new StringBuilderPrinter(sb), "Service Module");
            IMSLog.dump(str2, sb.toString());
            IMSLog.decreaseIndent(str2);
        }
    }

    public void sendMobileCareEvent(int i, int i2, int i3, String str) {
        if (this.mMobileCareController.isEnabled()) {
            this.mMobileCareController.sendMobileCareEvent(i, i2, i3, str, this.mPdnController.isEpdgConnected(i));
        }
    }

    public void onImsConifgChanged(int i, String str) {
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "onChange: config changed : " + str);
        if (str != null) {
            sendMessage(obtainMessage(21, i, 0, str));
        }
    }

    public boolean acceptCallWhileSmsipRegistered(ImsRegistration imsRegistration) {
        if (imsRegistration == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "Not registered.");
            return false;
        }
        int phoneId = imsRegistration.getPhoneId();
        int subId = SimUtil.getSubId(phoneId);
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "isVowifiEnabled=" + isVowifiEnabled(phoneId) + ", isVideoSettingEnabled=" + isVideoSettingEnabled() + ", isEpdgConnected=" + this.mPdnController.isEpdgConnected(phoneId) + ", VoiceNetworkType=" + this.mTelephonyManager.getVoiceNetworkType(subId) + ", DataNetworkType=" + this.mTelephonyManager.getDataNetworkType(subId) + ", SMSIP=" + imsRegistration.hasService("smsip") + ", VOICE=" + imsRegistration.hasService("mmtel") + ", VIDEO=" + imsRegistration.hasService("mmtel-video"));
        if (!isVowifiEnabled(phoneId) || isVideoSettingEnabled() || !this.mPdnController.isEpdgConnected(phoneId) || this.mTelephonyManager.getVoiceNetworkType(subId) != 7) {
            return false;
        }
        if ((this.mTelephonyManager.getDataNetworkType(subId) == 14 || this.mTelephonyManager.getDataNetworkType(subId) == 18) && imsRegistration.hasService("smsip") && !imsRegistration.hasService("mmtel") && !imsRegistration.hasService("mmtel-video")) {
            return true;
        }
        return false;
    }

    public void setTtyMode(int i) {
        for (ISimManager simSlotIndex : this.mSimManagers) {
            setTtyMode(simSlotIndex.getSimSlotIndex(), i);
        }
    }

    public synchronized void setTtyMode(int i, int i2) {
        int[] iArr = this.mTtyMode;
        int i3 = iArr[i];
        iArr[i] = i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setTtyMode: " + i3 + " -> " + this.mTtyMode[i]);
        this.mRegMan.setTtyMode(i, this.mTtyMode[i]);
        if (i3 == this.mTtyMode[i]) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "setTtyMode: not updating sessions");
            return;
        }
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "when non-registered status, do not pass TTY Mode");
            return;
        }
        if (imsRegistration.getImsProfile().getTtyType() != 1) {
            if (imsRegistration.getImsProfile().getTtyType() != 3) {
                IMSLog.c(LogClass.VOLTE_CHANGE_TTYMODE, i + "," + this.mTtyMode[i]);
                this.mVolteSvcIntf.setTtyMode(i, 0, this.mTtyMode[i]);
                this.mImsCallSessionManager.setTtyMode(i, i2);
                return;
            }
        }
        Log.e(IVolteServiceModuleInternal.LOG_TAG, "setTtyMode: do not call setTtyMode() for non IMS TTY operator");
        this.mTtyMode[i] = i3;
    }

    public void setRttMode(int i) {
        for (ISimManager simSlotIndex : this.mSimManagers) {
            setRttMode(simSlotIndex.getSimSlotIndex(), i);
        }
    }

    public void setRttMode(int i, int i2) {
        IRegistrationManager iRegistrationManager;
        int[] iArr = this.mRttMode;
        int i3 = iArr[i];
        iArr[i] = i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setRttMode: " + i3 + " -> " + this.mRttMode[i]);
        if (this.mImsCallSessionManager.getSessionCount() != 0 || (iRegistrationManager = this.mRegMan) == null) {
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "setRttMode: RTT registration is skiped because the call session exist.");
        } else {
            iRegistrationManager.setRttMode(i, this.mRttMode[i] == Extensions.TelecomManager.RTT_MODE);
        }
        if (i3 == this.mRttMode[i]) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "setRttMode: not updating sessions");
            return;
        }
        IMSLog.c(LogClass.VOLTE_CHANGE_RTTMODE, i + "," + this.mRttMode[i]);
        this.mVolteSvcIntf.setRttMode(i, this.mRttMode[i]);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0025, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onTextReceived(com.sec.internal.ims.servicemodules.volte2.data.TextInfo r3) {
        /*
            r2 = this;
            monitor-enter(r2)
            if (r3 == 0) goto L_0x0024
            int r0 = r3.getSessionId()     // Catch:{ all -> 0x0021 }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r2.getSession(r0)     // Catch:{ all -> 0x0021 }
            if (r0 != 0) goto L_0x000e
            goto L_0x0024
        L_0x000e:
            int r0 = r3.getSessionId()     // Catch:{ all -> 0x0021 }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r2.getSession(r0)     // Catch:{ all -> 0x0021 }
            int r0 = r0.getPhoneId()     // Catch:{ all -> 0x0021 }
            com.sec.internal.ims.servicemodules.volte2.VolteNotifier r1 = r2.mVolteNotifier     // Catch:{ all -> 0x0021 }
            r1.notifyOnRttEventBySession(r0, r3)     // Catch:{ all -> 0x0021 }
            monitor-exit(r2)
            return
        L_0x0021:
            r3 = move-exception
            monitor-exit(r2)
            throw r3
        L_0x0024:
            monitor-exit(r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.onTextReceived(com.sec.internal.ims.servicemodules.volte2.data.TextInfo):void");
    }

    public boolean isCsfbErrorCode(int i, CallProfile callProfile, SipError sipError) {
        return this.mImsCallSessionManager.isCsfbErrorCode(this.mContext, i, callProfile, sipError);
    }

    public void sendQualityStatisticsEvent() {
        this.mMobileCareController.sendQualityStatisticsEvent();
    }

    public void updateCapabilities(int i) {
        getServiceModuleManager().updateCapabilities(i);
    }

    public ImsFeature.Capabilities queryCapabilityStatus(int i) {
        ImsFeature.Capabilities capabilities = new ImsFeature.Capabilities();
        if (isCallServiceAvailable(i, "mmtel")) {
            capabilities.addCapabilities(1);
        }
        if (isCallServiceAvailable(i, "mmtel-video")) {
            capabilities.addCapabilities(2);
        }
        if (isCallServiceAvailable(i, "mmtel-call-composer")) {
            capabilities.addCapabilities(16);
        }
        return capabilities;
    }

    public boolean isCallServiceAvailable(int i, String str) {
        ImsRegistration imsRegistration = getImsRegistration(i);
        NetworkEvent network = getNetwork(i);
        boolean isRunning = isRunning();
        if (!isRunning || imsRegistration == null) {
            return this.mCmcServiceModule.isCallServiceAvailableOnSecondary(i, str, isRunning);
        }
        IUserAgent userAgent = getUserAgent(i);
        if (userAgent == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "UserAgent is null");
            return false;
        }
        if (userAgent.isRegistering()) {
            Set<String> serviceForNetwork = this.mRegMan.getServiceForNetwork(imsRegistration.getImsProfile(), imsRegistration.getRegiRat(), false, i);
            if (serviceForNetwork != null && !serviceForNetwork.contains(str)) {
                return false;
            }
        } else if (userAgent.isDeregistring()) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, " is not available due to Deregistring");
            return false;
        }
        Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        if (fromName == Mno.ATT && SimUtil.isSoftphoneEnabled()) {
            return imsRegistration.hasService(str);
        }
        if (network.outOfService) {
            String str2 = IVolteServiceModuleInternal.LOG_TAG;
            Log.e(str2, str + " is not available due to outOfService");
            return false;
        } else if (fromName == Mno.ATT) {
            int i2 = network.network;
            if (i2 == 18) {
                return imsRegistration.hasService(str);
            }
            if (NetworkUtil.is3gppPsVoiceNetwork(i2) && (network.voiceOverPs != VoPsIndication.NOT_SUPPORTED || hasActiveCall(i))) {
                return imsRegistration.hasService(str);
            }
            String str3 = IVolteServiceModuleInternal.LOG_TAG;
            Log.e(str3, str + " is not available due to unsupported N/W");
            return false;
        } else {
            if (fromName == Mno.VZW) {
                if (this.mRegMan.isInvite403DisabledService(i)) {
                    String str4 = IVolteServiceModuleInternal.LOG_TAG;
                    Log.e(str4, str + " is not available due to isInvite403DisabledService");
                    return false;
                } else if (!NetworkUtil.is3gppPsVoiceNetwork(network.network) && network.network != 18) {
                    String str5 = IVolteServiceModuleInternal.LOG_TAG;
                    Log.e(str5, str + " is not available due to unsupported N/W");
                    return false;
                }
            } else if (fromName == Mno.AIRTEL) {
                if (this.mRegMan.isSuspended(imsRegistration.getHandle())) {
                    String str6 = IVolteServiceModuleInternal.LOG_TAG;
                    Log.e(str6, str + " is not available due to N/W suspend");
                    return false;
                }
            } else if (fromName == Mno.TMOUS && str == "mmtel-call-composer" && ImsUtil.getComposerAuthValue(i, this.mContext) == 0) {
                Log.i(IVolteServiceModuleInternal.LOG_TAG, "TMO: return false for composerauth 0 and callcomposer service case");
                return false;
            }
            if (imsRegistration.getImsProfile().getDisallowReregi() && !NetworkUtil.is3gppPsVoiceNetwork(network.network) && network.network != 18) {
                String str7 = IVolteServiceModuleInternal.LOG_TAG;
                Log.e(str7, str + " is not available due to unsupported N/W");
                return false;
            } else if (this.mIsDeregisterTimerRunning[i]) {
                Log.e(IVolteServiceModuleInternal.LOG_TAG, "Call Service is not available for delayedDeregiTimer");
                return false;
            } else if (!fromName.isKor() || !str.equals("mmtel") || NetworkUtil.is3gppPsVoiceNetwork(network.network)) {
                return imsRegistration.hasService(str);
            } else {
                String str8 = IVolteServiceModuleInternal.LOG_TAG;
                Log.e(str8, "Call Service is not available for " + str);
                return false;
            }
        }
    }

    public long getRttDbrTimer(int i) {
        ImsProfile imsProfile;
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration != null) {
            imsProfile = imsRegistration.getImsProfile();
        } else {
            IRegistrationManager iRegistrationManager = this.mRegMan;
            imsProfile = iRegistrationManager != null ? iRegistrationManager.getImsProfile(i, ImsProfile.PROFILE_TYPE.EMERGENCY) : null;
        }
        if (imsProfile != null) {
            return (long) imsProfile.getDbrTimer();
        }
        return 20000;
    }

    public int startLocalRingBackTone(int i, int i2, int i3) {
        int i4;
        List<ImsCallSession> sessionByState = getSessionByState(CallConstants.STATE.OutGoingCall);
        List<ImsCallSession> sessionByState2 = getSessionByState(CallConstants.STATE.AlertingCall);
        if (sessionByState.size() > 0) {
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "has Outgoing call");
            i4 = sessionByState.get(0).getPhoneId();
        } else if (sessionByState2.size() > 0) {
            i4 = sessionByState2.get(0).getPhoneId();
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "has Alerting call");
        } else {
            i4 = -1;
        }
        if (i4 < 0 || i4 > SimUtil.getPhoneCount() || !this.mPdnController.isEpdgConnected(i4)) {
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "Do Not Use IMS RBT when non WiFi Calling");
            return -1;
        }
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "Use IMS RBT when WiFi Calling");
        return this.mMediaSvcIntf.startLocalRingBackTone(i, i2, i3);
    }

    public int stopLocalRingBackTone() {
        return this.mMediaSvcIntf.stopLocalRingBackTone();
    }

    public ICmcServiceHelperInternal getCmcServiceHelper() {
        return this.mCmcServiceModule;
    }

    public void transfer(int i, String str) {
        this.mImsExternalCallController.transfer(i, str);
    }

    public int getTotalCallCount(int i) {
        return this.mImsCallSessionManager.getTotalCallCount(i);
    }

    public int getVideoCallCount(int i) {
        return this.mImsCallSessionManager.getVideoCallCount(i);
    }

    public int getDowngradedCallCount(int i) {
        return this.mImsCallSessionManager.getDowngradedCallCount(i);
    }

    public int getE911CallCount(int i) {
        return this.mImsCallSessionManager.getE911CallCount(i);
    }

    public int getEpsFbCallCount(int i) {
        return this.mImsCallSessionManager.getEpsFbCallCount(i);
    }

    public int getNrSaCallCount(int i) {
        return this.mImsCallSessionManager.getNrSaCallCount(i);
    }

    public int getEpdgCallCount(int i) {
        return this.mImsCallSessionManager.getEpdgCallCount(i);
    }

    public void releaseSessionByState(int i, CallConstants.STATE state) {
        this.mImsCallSessionManager.releaseSessionByState(i, state);
    }

    public void sendRtpLossRate(int i, RtpLossRateNoti rtpLossRateNoti) {
        this.mVolteNotifier.notifyOnRtpLossRate(i, rtpLossRateNoti);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0054  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x006c A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String updateEccUrn(int r8, java.lang.String r9) {
        /*
            r7 = this;
            java.lang.String r0 = r7.updateCategoryList(r8)
            java.lang.String r1 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "eccCategoryList : "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            java.lang.String r2 = ""
            if (r1 != 0) goto L_0x004d
            boolean r1 = android.text.TextUtils.isEmpty(r9)
            if (r1 != 0) goto L_0x004d
            java.lang.String r1 = ","
            java.lang.String[] r0 = r0.split(r1)
            int r1 = r0.length
            r3 = 0
            r4 = r3
        L_0x0031:
            if (r4 >= r1) goto L_0x004d
            r5 = r0[r4]
            java.lang.String r6 = "/"
            java.lang.String[] r5 = r5.split(r6)
            r6 = r5[r3]
            boolean r6 = r6.equals(r9)
            if (r6 == 0) goto L_0x004a
            int r9 = r5.length
            r0 = 1
            if (r9 <= r0) goto L_0x004d
            r9 = r5[r0]
            goto L_0x004e
        L_0x004a:
            int r4 = r4 + 1
            goto L_0x0031
        L_0x004d:
            r9 = r2
        L_0x004e:
            boolean r0 = r2.equals(r9)
            if (r0 != 0) goto L_0x006c
            boolean r7 = r7.isRequiredKorSpecificURN(r8, r9)
            if (r7 == 0) goto L_0x0063
            int r7 = java.lang.Integer.parseInt(r9)
            java.lang.String r7 = com.sec.internal.helper.ImsCallUtil.convertEccCatToUrnSpecificKor(r7)
            goto L_0x006f
        L_0x0063:
            int r7 = java.lang.Integer.parseInt(r9)
            java.lang.String r7 = com.sec.internal.helper.ImsCallUtil.convertEccCatToUrn(r7)
            goto L_0x006f
        L_0x006c:
            java.lang.String r7 = "urn:service:sos"
        L_0x006f:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.updateEccUrn(int, java.lang.String):java.lang.String");
    }

    public IUserAgent getUserAgent(int i) {
        IRegistrationManager iRegistrationManager;
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration == null || (iRegistrationManager = this.mRegMan) == null) {
            return null;
        }
        return iRegistrationManager.getUserAgentByRegId(imsRegistration.getHandle());
    }

    private String updateCategoryList(int i) {
        String str;
        Mno simMno = SimUtil.getSimMno(i);
        String string = ImsRegistry.getString(i, GlobalSettingsConstants.Call.ECC_CATEGORY_LIST_CDMA, "");
        if (i <= 0) {
            str = "ril.ecclist_net0";
        } else {
            str = "ril.ecclist_net" + i;
        }
        String str2 = SemSystemProperties.get(str, "");
        String string2 = ImsRegistry.getString(i, GlobalSettingsConstants.Call.ECC_CATEGORY_LIST, "");
        int i2 = 0;
        String str3 = "";
        while (true) {
            String str4 = SemSystemProperties.get("ril.ecclist" + i + Integer.toString(i2));
            if (str4.length() == 0) {
                break;
            }
            if (str3.length() > 0) {
                str3 = str3 + "," + str4;
            } else {
                str3 = str3 + str4;
            }
            i2++;
        }
        if (str2.length() > 0) {
            if ("".equals(string2)) {
                string2 = str2;
            } else {
                string2 = string2 + "," + str2;
            }
        }
        if (str3.length() > 0) {
            if (!"".equals(string2)) {
                str3 = string2 + "," + str3;
            }
            string2 = str3;
        }
        if (string.length() > 0) {
            if (!"".equals(string2)) {
                string = string2 + "," + string;
            }
            string2 = string;
        }
        if (!this.mTelephonyManager.isNetworkRoaming()) {
            return string2;
        }
        if (simMno == Mno.SKT || simMno == Mno.KT) {
            return "000/4,08/4,110/4,999/4,118/4," + string2;
        } else if (simMno != Mno.LGU) {
            return string2;
        } else {
            return str2 + string2;
        }
    }

    public void registerAllowedNetworkTypesListener(int i) {
        int subId = SimUtil.getSubId(i);
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "registerAllowedNetworkTypesListener(" + i + ")  : not ValidSubscriptionId");
            return;
        }
        unregisterAllowedNetworkTypesListener(i);
        TelephonyManager createForSubscriptionId = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).createForSubscriptionId(subId);
        if (createForSubscriptionId == null) {
            String str2 = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str2, "registerAllowedNetworkTypesListener(" + i + ")  : TelephonyManager null");
            return;
        }
        this.mAllowedNetworkTypesListener[i] = new AllowedNetworkTypesListener(i);
        createForSubscriptionId.registerTelephonyCallback(this.mContext.getMainExecutor(), this.mAllowedNetworkTypesListener[i]);
        this.mAllowedNetworkType[i] = createForSubscriptionId.getAllowedNetworkTypesBitmask();
        String str3 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str3, "registerAllowedNetworkTypesListener(" + i + ") : " + this.mAllowedNetworkType[i] + " " + this.mAllowedNetworkTypesListener[i]);
        this.mRegMan.updateNrPreferredMode(i, ImsCallUtil.isNrAvailable(this.mAllowedNetworkType[i]));
    }

    public class AllowedNetworkTypesListener extends TelephonyCallback implements TelephonyCallback.AllowedNetworkTypesListener {
        int mPhoneId;

        AllowedNetworkTypesListener(int i) {
            this.mPhoneId = i;
        }

        public void onAllowedNetworkTypesChanged(int i, long j) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "onAllowedNetworkTypesChanged(" + this.mPhoneId + ") : prev = " + VolteServiceModuleInternal.this.mAllowedNetworkType[this.mPhoneId] + " new = " + j);
            VolteServiceModuleInternal.this.handleAllowedNetworkTypeChanged(this.mPhoneId, j);
        }
    }

    public void handleAllowedNetworkTypeChanged(int i, long j) {
        long[] jArr = this.mAllowedNetworkType;
        if (jArr[i] != j) {
            jArr[i] = j;
            this.mRegMan.updateNrPreferredMode(i, ImsCallUtil.isNrAvailable(j));
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterAllowedNetworkTypesListener(int i) {
        if (this.mAllowedNetworkTypesListener[i] != null) {
            TelephonyManager createForSubscriptionId = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).createForSubscriptionId(SimUtil.getSubId(i));
            if (createForSubscriptionId == null) {
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str, "unregisterAllowedNetworkTypesListener(" + i + ") : TelephonyManager null");
                return;
            }
            createForSubscriptionId.unregisterTelephonyCallback(this.mAllowedNetworkTypesListener[i]);
            this.mAllowedNetworkTypesListener[i] = null;
        }
    }

    private void registerEpdgConnectionListener() {
        AnonymousClass3 r0 = new WfcEpdgManager.WfcEpdgConnectionListener() {
            public void onEpdgServiceConnected() {
                Log.i(IVolteServiceModuleInternal.LOG_TAG, "EPDG onEpdgServiceConnected");
                VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                if (volteServiceModuleInternal.mEpdgListener == null) {
                    volteServiceModuleInternal.mEpdgListener = new ImsManager.EpdgListener() {
                        public void onEpdgReleaseCall(int i) {
                            String str = IVolteServiceModuleInternal.LOG_TAG;
                            Log.i(str, "onEpdgReleaseCall, " + i);
                            VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                            volteServiceModuleInternal.sendMessage(volteServiceModuleInternal.obtainMessage(20, i, 0));
                        }
                    };
                }
                VolteServiceModuleInternal volteServiceModuleInternal2 = VolteServiceModuleInternal.this;
                volteServiceModuleInternal2.mWfcEpdgMgr.registerEpdgHandoverListener(volteServiceModuleInternal2.mEpdgListener);
                for (int i = 0; i < VolteServiceModuleInternal.this.mTelephonyManager.getPhoneCount(); i++) {
                    boolean z = ImsRegistry.getBoolean(i, GlobalSettingsConstants.Call.ALLOW_RELEASE_WFC_BEFORE_HO, false);
                    String str = IVolteServiceModuleInternal.LOG_TAG;
                    Log.i(str, "Phone#" + i + " is allow release call " + z);
                    if (VolteServiceModuleInternal.this.mWfcEpdgMgr.getEpdgMgr() == null) {
                        Log.i(str, "epdgManager is null");
                    } else {
                        VolteServiceModuleInternal.this.mWfcEpdgMgr.getEpdgMgr().setReleaseCallBeforeHO(i, z);
                    }
                }
            }

            public void onEpdgServiceDisconnected() {
                Log.i(IVolteServiceModuleInternal.LOG_TAG, "WfcEpdgMgr : disconnected.");
                Arrays.fill(VolteServiceModuleInternal.this.mReleaseWfcBeforeHO, false);
                VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                volteServiceModuleInternal.mWfcEpdgMgr.unRegisterEpdgHandoverListener(volteServiceModuleInternal.mEpdgListener);
                VolteServiceModuleInternal.this.mEpdgListener = null;
            }
        };
        this.mWfcEpdgConnectionListener = r0;
        this.mWfcEpdgMgr.registerWfcEpdgConnectionListener(r0);
    }

    public boolean isVowifiEnabled(int i) {
        boolean isEnabled = VowifiConfig.isEnabled(this.mContext, i);
        if (!this.mTelephonyManager.isNetworkRoaming() || !isEnabled) {
            return isEnabled;
        }
        return VowifiConfig.getRoamPrefMode(this.mContext, 0, i) == 1;
    }

    /* access modifiers changed from: protected */
    public boolean isVolteSettingEnabled() {
        int i = ImsConstants.SystemSettings.VOLTE_SLOT1.get(this.mContext, 0);
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "voiceType : " + i);
        if (i == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:31:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isLTEDataModeEnabled(int r7) {
        /*
            r6 = this;
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r7)
            boolean r1 = com.sec.internal.helper.OmcCode.isKOROmcCode()
            r2 = 1
            if (r1 == 0) goto L_0x0075
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.KT
            if (r0 == r1) goto L_0x0075
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.LGU
            if (r0 != r1) goto L_0x0014
            goto L_0x0075
        L_0x0014:
            r0 = 0
            com.sec.internal.helper.os.ITelephonyManager r6 = r6.mTelephonyManager     // Catch:{ Exception -> 0x0053 }
            int r1 = com.sec.internal.helper.SimUtil.getSubId(r7)     // Catch:{ Exception -> 0x0053 }
            int r6 = r6.getPreferredNetworkType(r1)     // Catch:{ Exception -> 0x0053 }
            if (r6 == 0) goto L_0x002f
            r1 = 2
            if (r6 == r1) goto L_0x002f
            r1 = 14
            if (r6 == r1) goto L_0x002f
            r1 = 18
            if (r6 != r1) goto L_0x002d
            goto L_0x002f
        L_0x002d:
            r1 = r2
            goto L_0x0030
        L_0x002f:
            r1 = r0
        L_0x0030:
            java.lang.String r3 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG     // Catch:{ Exception -> 0x0054 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0054 }
            r4.<init>()     // Catch:{ Exception -> 0x0054 }
            java.lang.String r5 = "LTEDataMode : netType = "
            r4.append(r5)     // Catch:{ Exception -> 0x0054 }
            r4.append(r6)     // Catch:{ Exception -> 0x0054 }
            java.lang.String r6 = " subid = "
            r4.append(r6)     // Catch:{ Exception -> 0x0054 }
            int r6 = com.sec.internal.helper.SimUtil.getSubId(r7)     // Catch:{ Exception -> 0x0054 }
            r4.append(r6)     // Catch:{ Exception -> 0x0054 }
            java.lang.String r6 = r4.toString()     // Catch:{ Exception -> 0x0054 }
            android.util.Log.i(r3, r6)     // Catch:{ Exception -> 0x0054 }
            goto L_0x005b
        L_0x0053:
            r1 = r2
        L_0x0054:
            java.lang.String r6 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.String r7 = "LTEDataMode : getPreferredNetworkType fail"
            android.util.Log.i(r6, r7)
        L_0x005b:
            java.lang.String r6 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r3 = "LTEDataMode : "
            r7.append(r3)
            r7.append(r1)
            java.lang.String r7 = r7.toString()
            android.util.Log.i(r6, r7)
            if (r1 != r2) goto L_0x0074
            goto L_0x0075
        L_0x0074:
            r2 = r0
        L_0x0075:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.isLTEDataModeEnabled(int):boolean");
    }

    public boolean isRoaming(int i) {
        if (getNetwork(i) == null) {
            return false;
        }
        if (getNetwork(i).isVoiceRoaming || getNetwork(i).isDataRoaming) {
            return true;
        }
        return false;
    }

    public boolean isRegisteredOver3gppPsVoice(int i) {
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration == null) {
            return false;
        }
        return NetworkUtil.is3gppPsVoiceNetwork(imsRegistration.getCurrentRat());
    }

    public boolean triggerPsRedial(int i, int i2, int i3) {
        return this.mImsCallSessionManager.triggerPsRedial(i, i2, i3, getImsRegistration(i));
    }

    public void pushCallInternal() {
        this.mImsExternalCallController.pushCallInternal();
    }

    public int getLastRegiErrorCode(int i) {
        return this.mLastRegiErrorCode[i];
    }

    public boolean isSilentRedialEnabled(Context context, int i) {
        return DmConfigHelper.readBool(context, "silent_redial", Boolean.TRUE, i).booleanValue();
    }

    public IMediaServiceInterface getMediaSvcIntf() {
        return this.mMediaSvcIntf;
    }

    public CmcServiceHelper getCmcServiceModule() {
        return this.mCmcServiceModule;
    }

    public boolean isEnableCallWaitingRule() {
        return this.mEnableCallWaitingRule;
    }

    public boolean isMmtelAcquiredEver() {
        return this.mMmtelAcquiredEver;
    }

    private boolean isVideoSettingEnabled() {
        return ImsConstants.SystemSettings.VILTE_SLOT1.get(this.mContext, 0) == 0;
    }

    public boolean hasEmergencyCall(int i) {
        return this.mImsCallSessionManager.hasEmergencyCall(i);
    }

    public ImsCallSession getSessionByCallId(int i) {
        return this.mImsCallSessionManager.getSessionByCallId(i);
    }

    public ImsCallSession getSessionBySipCallId(String str) {
        return this.mImsCallSessionManager.getSessionBySipCallId(str);
    }

    public List<ImsCallSession> getSessionByState(CallConstants.STATE state) {
        return getSessionByState(-1, state);
    }

    public List<ImsCallSession> getSessionByState(int i, CallConstants.STATE state) {
        return this.mImsCallSessionManager.getSessionByState(i, state);
    }

    public boolean hasActiveCall(int i) {
        return this.mImsCallSessionManager.hasActiveCall(i);
    }

    public boolean hasEstablishedCall(int i) {
        return this.mImsCallSessionManager.hasEstablishedCall(i);
    }

    public int getSessionCount() {
        return this.mImsCallSessionManager.getSessionCount();
    }

    public int getSessionCount(int i) {
        return this.mImsCallSessionManager.getSessionCount(i);
    }

    public ImsCallSession getSession(int i) {
        return this.mImsCallSessionManager.getSession(i);
    }

    public ImsCallSession getSessionByRegId(int i) {
        return this.mImsCallSessionManager.getSessionByRegId(i);
    }

    public List<ImsCallSession> getSessionList() {
        return this.mImsCallSessionManager.getSessionList();
    }

    public List<ImsCallSession> getSessionList(int i) {
        return this.mImsCallSessionManager.getSessionList(i);
    }

    public ImsCallSession getForegroundSession() {
        return this.mImsCallSessionManager.getForegroundSession();
    }

    public ImsCallSession getForegroundSession(int i) {
        return this.mImsCallSessionManager.getForegroundSession(i);
    }

    public List<ImsCallSession> getSessionByCallType(int i) {
        return getSessionByCallType(-1, i);
    }

    public List<ImsCallSession> getSessionByCallType(int i, int i2) {
        return this.mImsCallSessionManager.getSessionByCallType(i, i2);
    }

    public boolean hasRingingCall() {
        return hasRingingCall(-1);
    }

    public boolean hasRingingCall(int i) {
        return this.mImsCallSessionManager.hasRingingCall(i);
    }

    public NetworkEvent getNetwork() {
        return getNetwork(this.mActiveDataPhoneId);
    }

    public NetworkEvent getNetwork(int i) {
        return this.mNetworks.get(Integer.valueOf(i));
    }

    public void onCallModifyRequested(int i) {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "onCallModifyRequested: sessionId " + i);
        ImsCallSession session = getSession(i);
        if (session != null) {
            this.mVolteNotifier.notifyCallStateEvent(new CallStateEvent(CallStateEvent.CALL_STATE.MODIFY_REQUESTED), session);
        }
    }

    public boolean hasCsCall(int i) {
        return hasCsCall(i, false);
    }

    public boolean hasCsCall(int i, boolean z) {
        int sessionCount = getSessionCount(i);
        ImsCallSession incomingCallSession = this.mImsCallSessionManager.getIncomingCallSession(i);
        boolean z2 = true;
        int i2 = 0;
        if (z && sessionCount == 1 && incomingCallSession != null && getSessionByCallId(incomingCallSession.getCallId()) != null) {
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "only one PS incoming call exists");
            sessionCount = 0;
        }
        ITelephonyManager instance = TelephonyManagerWrapper.getInstance(this.mContext);
        if (instance != null) {
            int callState = instance.getCallState(i);
            if (sessionCount != 0 || callState == 0) {
                z2 = false;
            }
            i2 = callState;
        } else {
            z2 = false;
        }
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "hasCsCall: numPsCall=" + sessionCount + ", callState[" + i + "]=" + i2 + ", ret=" + z2);
        return z2;
    }

    public boolean hasOutgoingCall(int i) {
        return this.mImsCallSessionManager.hasOutgoingCall(i);
    }

    public MobileCareController getMobileCareController() {
        return this.mMobileCareController;
    }

    public boolean getRatChanged(int i) {
        return this.mRatChanged[i];
    }

    public void setRatChanged(int i, boolean z) {
        this.mRatChanged[i] = z;
    }

    public void notifyProgressIncomingCall(int i, HashMap<String, String> hashMap) {
        this.mVolteSvcIntf.proceedIncomingCall(this.mImsCallSessionManager.convertToSessionId(i), hashMap, (String) null);
    }

    public int publishDialog(int i, String str, String str2, String str3, int i2) {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "publishDialog: ");
        return this.mVolteSvcIntf.publishDialog(i, str, str2, str3, i2, false);
    }

    /* access modifiers changed from: protected */
    public void clearDialogList(int i, int i2) {
        for (DialogEvent dialogEvent : this.mLastDialogEvent) {
            if (dialogEvent != null && dialogEvent.getDialogList().size() > 0 && i2 == dialogEvent.getRegId()) {
                Log.i(IVolteServiceModuleInternal.LOG_TAG, "Match RegId clear Dialog List");
                DialogEvent dialogEvent2 = new DialogEvent(dialogEvent.getMsisdn(), new ArrayList());
                dialogEvent2.setRegId(dialogEvent.getRegId());
                dialogEvent2.setPhoneId(i);
                removeMessages(15);
                sendMessage(obtainMessage(15, dialogEvent2));
            }
        }
    }

    public String toString() {
        String str;
        String str2;
        boolean z = true;
        String str3 = "[";
        if (SimUtil.isDualIMS()) {
            int i = 0;
            while (i < SimUtil.getPhoneCount()) {
                StringBuilder sb = new StringBuilder();
                sb.append(str3);
                sb.append(i != 0 ? ", [" : "");
                String sb2 = sb.toString();
                if (isEmergencyRegistered(i)) {
                    str2 = sb2 + "Emergency Registered - PhoneId <" + i + ">";
                } else {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(sb2);
                    sb3.append("phoneId <");
                    sb3.append(i);
                    sb3.append("> - Registered : ");
                    sb3.append(getImsRegistration(i) != null);
                    str2 = sb3.toString();
                }
                str3 = str2 + " Feature: " + this.mEnabledFeatures[i] + "]";
                i++;
            }
            return str3;
        }
        if (isEmergencyRegistered(this.mActiveDataPhoneId)) {
            str = str3 + "Emergency Registered";
        } else {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str3);
            sb4.append("Registered: ");
            if (getImsRegistration() == null) {
                z = false;
            }
            sb4.append(z);
            str = sb4.toString();
        }
        return str + " Feature: " + this.mEnabledFeatures[this.mActiveDataPhoneId] + "]";
    }

    /* access modifiers changed from: protected */
    public void terminateMoWfcWhenWfcSettingOff(int i) {
        if (SimUtil.getSimMno(i) == Mno.VZW && !isVowifiEnabled(i) && this.mPdnController.isEpdgConnected(i) && this.mTelephonyManager.getDataNetworkType(SimUtil.getSubId(i)) == 13) {
            this.mImsCallSessionManager.terminateMoWfcWhenWfcSettingOff(i);
        }
    }

    /* access modifiers changed from: protected */
    public void onImsCallEventForEstablish(ImsRegistration imsRegistration, ImsCallSession imsCallSession, CallStateEvent callStateEvent) {
        ImsRegistration cmcRegistration;
        if (imsRegistration != null) {
            int phoneId = imsRegistration.getPhoneId();
            Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            if (fromName == Mno.VZW && !this.mRegMan.isVoWiFiSupported(phoneId) && imsRegistration.getEpdgStatus() && callStateEvent.getCallType() == 1) {
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str, "onImsCallEvent: session=" + callStateEvent.getSessionID() + " releaseAllVideoCall due to the audio call");
                this.mImsCallSessionManager.releaseAllVideoCall();
            }
            if (imsCallSession.getCmcType() == 0 && fromName.isChn()) {
                notifyDSDAVideoCapa(phoneId);
            }
            if (this.mRegMan.isVoWiFiSupported(phoneId) && isVowifiEnabled(phoneId) && getTotalCallCount(phoneId) == 1) {
                WiFiManagerExt.setImsCallEstablished(this.mContext, true);
            }
            if (ImsCallUtil.isCmcPrimaryType(imsRegistration.getImsProfile().getCmcType())) {
                imsCallSession.getCallProfile().setCmcDeviceId(callStateEvent.getCmcDeviceId());
            }
            this.mCmcServiceModule.onImsCallEventWhenEstablished(phoneId, imsCallSession, imsRegistration);
        }
        if (imsCallSession.getCmcType() == 1) {
            this.mCmcServiceModule.sendCmcCallStateForRcs(imsCallSession.getPhoneId(), ImsConstants.CmcInfo.CMC_DUMMY_TEL_NUMBER, true);
        } else if (ImsCallUtil.isCmcSecondaryType(imsCallSession.getCmcType()) && (cmcRegistration = this.mCmcServiceModule.getCmcRegistration(imsCallSession.getPhoneId(), false, imsCallSession.getCmcType())) != null) {
            clearDialogList(imsCallSession.getPhoneId(), cmcRegistration.getHandle());
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigUpdated(int i, String str) {
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "onConfigUpdated[" + i + "] : " + str);
        if ("VOLTE_ENABLED".equalsIgnoreCase(str) || "LVC_ENABLED".equalsIgnoreCase(str)) {
            onServiceSwitched(i, (ContentValues) null);
        }
    }

    public EpdgManager getEpdgManager() {
        return this.mWfcEpdgMgr.getEpdgMgr();
    }

    public boolean getLteEpsOnlyAttached(int i) {
        return this.mIsLteEpsOnlyAttached[i];
    }

    public int getDataAccessNetwork(int i) {
        return this.mDataAccessNetwork[i];
    }

    public int getSrvccVersion(int i) {
        return ImsRegistry.getInt(i, GlobalSettingsConstants.Call.SRVCC_VERSION, 0);
    }

    public boolean isCallBarringByNetwork(int i) {
        return ImsRegistry.getBoolean(i, GlobalSettingsConstants.SS.CALLBARRING_BY_NETWORK, false);
    }

    public int getActiveDataPhoneId() {
        return this.mActiveDataPhoneId;
    }

    /* access modifiers changed from: protected */
    public void onSimSubscribeIdChanged(SubscriptionInfo subscriptionInfo) {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "onSimSubscribeIdChanged, SimSlot: " + subscriptionInfo.getSimSlotIndex() + ", subId: " + subscriptionInfo.getSubscriptionId());
        int simSlotIndex = subscriptionInfo.getSimSlotIndex();
        unRegisterPhoneStateListener(simSlotIndex);
        registerPhoneStateListener(simSlotIndex);
        registerAllowedNetworkTypesListener(simSlotIndex);
    }

    public int getMergeCallType(int i, boolean z) {
        return this.mImsCallSessionManager.getMergeCallType(i, z);
    }

    /* access modifiers changed from: protected */
    public void onSrvccStateChange(int i, Call.SrvccState srvccState) {
        Mno mno;
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "phoneId [" + i + "] handleReinvite");
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration == null) {
            mno = SimUtil.getSimMno(i);
        } else {
            mno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        }
        if (isRunning()) {
            this.mImsCallSessionManager.handleSrvccStateChange(i, srvccState, mno);
        }
    }

    public boolean hasDialingOrIncomingCall() {
        if (hasDialingOrIncomingCallOnCS()) {
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "SD has already CS dialing or incoming call on SIM");
            return true;
        } else if (!this.mCmcServiceModule.hasDialingOrIncomingCall()) {
            return false;
        } else {
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "SD has already PS dialing or incoming call on SIM");
            return true;
        }
    }

    private boolean hasDialingOrIncomingCallOnCS() {
        return this.mTelephonyManager.hasCall("csdialing") || this.mTelephonyManager.hasCall("csalerting") || this.mTelephonyManager.hasCall("csincoming");
    }

    private String getDialingNumber(IncomingCallEvent incomingCallEvent, Mno mno) {
        String remoteCallerId = ImsCallUtil.getRemoteCallerId(incomingCallEvent.getPeerAddr(), mno, Debug.isProductShip());
        ITelephonyManager iTelephonyManager = this.mTelephonyManager;
        if (iTelephonyManager == null || iTelephonyManager.isNetworkRoaming()) {
            return remoteCallerId;
        }
        if (mno == Mno.VZW || mno == Mno.USCC) {
            return ImsCallUtil.removeUriPlusPrefix(remoteCallerId, Debug.isProductShip());
        }
        if (mno == Mno.KT) {
            return ImsCallUtil.removeUriPlusPrefix(remoteCallerId, "+82", "0", Debug.isProductShip());
        }
        if (mno == Mno.TELENOR_MM) {
            return ImsCallUtil.removeUriPlusPrefix(remoteCallerId, "+95", "0", Debug.isProductShip());
        }
        return mno.isAus() ? ImsCallUtil.removeUriPlusPrefix(remoteCallerId, "+61", "0", Debug.isProductShip()) : remoteCallerId;
    }

    public boolean isNotifyRejectedCall(int i) {
        return ImsRegistry.getBoolean(i, GlobalSettingsConstants.Call.NOTIFY_REJECTED_CALL, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:91:0x02d1 A[SYNTHETIC, Splitter:B:91:0x02d1] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handlePreAlerting(com.sec.ims.ImsRegistration r26, com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent r27, boolean r28, boolean r29, com.sec.ims.util.SipError r30) {
        /*
            r25 = this;
            r1 = r25
            r0 = r26
            r8 = r27
            r2 = r28
            com.sec.ims.settings.ImsProfile r3 = r26.getImsProfile()
            java.lang.String r3 = r3.getMnoName()
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.fromName(r3)
            if (r2 == 0) goto L_0x001f
            java.lang.String r3 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.String r4 = "change mno to MDMN"
            android.util.Log.i(r3, r4)
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.MDMN
        L_0x001f:
            int r4 = r26.getPhoneId()
            boolean r4 = r1.hasCsCall(r4)
            if (r4 == 0) goto L_0x0048
            if (r29 != 0) goto L_0x0048
            com.sec.ims.settings.ImsProfile r4 = r26.getImsProfile()
            int r4 = r4.getCmcType()
            if (r4 != 0) goto L_0x0048
            java.lang.String r0 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.String r2 = "Has Active CS Call, try after"
            android.util.Log.i(r0, r2)
            r0 = 11
            android.os.Message r0 = r1.obtainMessage(r0, r8)
            r2 = 1000(0x3e8, double:4.94E-321)
            r1.sendMessageDelayed(r0, r2)
            return
        L_0x0048:
            com.sec.ims.volte2.data.CallProfile r4 = new com.sec.ims.volte2.data.CallProfile
            r4.<init>()
            int r5 = r27.getCallType()
            com.sec.ims.util.SipError r6 = com.sec.internal.constants.ims.SipErrorBase.OK
            r7 = r30
            if (r7 != r6) goto L_0x00a5
            com.sec.internal.ims.servicemodules.volte2.ImsCallSipErrorFactory r7 = r1.mImsCallSipErrorFactory
            android.content.Context r9 = r1.mContext
            com.sec.ims.util.SipError r7 = r7.getSipErrorForCheckRejectIncomingCall(r9, r0, r5)
            if (r7 == r6) goto L_0x00b6
            java.lang.String r6 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "onImsIncomingCallEvent: reject call. error="
            r9.append(r10)
            r9.append(r7)
            java.lang.String r9 = r9.toString()
            android.util.Log.i(r6, r9)
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r6 = r1.mVolteSvcIntf
            int r9 = r27.getSessionID()
            int r10 = r27.getCallType()
            r6.rejectCall(r9, r10, r7)
            com.sec.ims.settings.ImsProfile r6 = r26.getImsProfile()
            int r6 = r6.getCmcType()
            boolean r6 = com.sec.internal.helper.ImsCallUtil.isCmcPrimaryType(r6)
            if (r6 != 0) goto L_0x00a4
            int r6 = r26.getPhoneId()
            boolean r6 = r1.isNotifyRejectedCall(r6)
            if (r6 == 0) goto L_0x00a4
            int r6 = com.sec.internal.helper.ImsCallUtil.convertErrorToRejectReason(r7)
            r4.setRejectCause(r6)
            goto L_0x00b6
        L_0x00a4:
            return
        L_0x00a5:
            int r6 = r26.getPhoneId()
            boolean r6 = r1.isNotifyRejectedCall(r6)
            if (r6 == 0) goto L_0x00b6
            int r6 = com.sec.internal.helper.ImsCallUtil.convertErrorToRejectReason(r30)
            r4.setRejectCause(r6)
        L_0x00b6:
            r9 = r7
            com.sec.ims.settings.ImsProfile r6 = r26.getImsProfile()
            int r6 = r6.getCmcType()
            boolean r6 = com.sec.internal.helper.ImsCallUtil.isCmcPrimaryType(r6)
            r10 = 5
            r11 = 0
            r7 = 1
            if (r6 == 0) goto L_0x0172
            int r6 = r26.getPhoneId()
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r12 = com.sec.internal.ims.registry.ImsRegistry.getCmcAccountManager()
            boolean r12 = r12.isSupportDualSimCMC()
            r13 = -1
            if (r12 == 0) goto L_0x00ea
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r12 = r27.getParams()
            int r12 = r12.getCmcEdCallSlot()
            if (r12 != r13) goto L_0x00e2
            goto L_0x00ea
        L_0x00e2:
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r6 = r27.getParams()
            int r6 = r6.getCmcEdCallSlot()
        L_0x00ea:
            java.util.List<? extends com.sec.internal.interfaces.ims.core.ISimManager> r12 = r1.mSimManagers
            java.util.Iterator r12 = r12.iterator()
        L_0x00f0:
            boolean r14 = r12.hasNext()
            if (r14 == 0) goto L_0x0106
            java.lang.Object r14 = r12.next()
            com.sec.internal.interfaces.ims.core.ISimManager r14 = (com.sec.internal.interfaces.ims.core.ISimManager) r14
            int r15 = r14.getSimSlotIndex()
            if (r15 != r6) goto L_0x00f0
            int r13 = r14.getSimState()
        L_0x0106:
            boolean r12 = com.sec.internal.helper.ImsCallUtil.isE911Call(r5)
            if (r12 != 0) goto L_0x0138
            if (r13 == r10) goto L_0x0138
            r12 = 10
            if (r13 == r12) goto L_0x0138
            java.lang.String r0 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onImsIncomingCallEvent: reject call. simState : "
            r2.append(r3)
            r2.append(r13)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r0, r2)
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r0 = r1.mVolteSvcIntf
            int r1 = r27.getSessionID()
            int r2 = r27.getCallType()
            com.sec.ims.util.SipError r3 = com.sec.internal.constants.ims.SipErrorBase.TEMPORARILY_UNAVAIABLE
            r0.rejectCall(r1, r2, r3)
            return
        L_0x0138:
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r12 = com.sec.internal.ims.registry.ImsRegistry.getCmcAccountManager()
            boolean r12 = r12.isSupportDualSimCMC()
            if (r12 == 0) goto L_0x0150
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r12 = r27.getParams()
            java.lang.String r12 = r12.getReplaces()
            boolean r12 = android.text.TextUtils.isEmpty(r12)
            if (r12 == 0) goto L_0x0172
        L_0x0150:
            if (r6 != 0) goto L_0x0154
            r6 = r7
            goto L_0x0155
        L_0x0154:
            r6 = r11
        L_0x0155:
            boolean r6 = r1.hasCsCall(r6, r7)
            if (r6 == 0) goto L_0x0172
            java.lang.String r0 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.String r2 = "checkRejectIncomingCall: PD_CALL_EXISTS_ON_THE_OTHER_SLOT"
            android.util.Log.i(r0, r2)
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r0 = r1.mVolteSvcIntf
            int r1 = r27.getSessionID()
            int r2 = r27.getCallType()
            com.sec.ims.util.SipError r3 = com.sec.internal.constants.ims.SipErrorBase.PD_CALL_EXISTS_ON_THE_OTHER_SLOT
            r0.rejectCall(r1, r2, r3)
            return
        L_0x0172:
            com.sec.ims.settings.ImsProfile r6 = r26.getImsProfile()
            int r6 = r6.getCmcType()
            boolean r6 = com.sec.internal.helper.ImsCallUtil.isCmcSecondaryType(r6)
            if (r6 == 0) goto L_0x018e
            boolean r6 = r25.hasDialingOrIncomingCall()
            if (r6 == 0) goto L_0x018e
            java.lang.String r0 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.String r1 = "onImsIncomingCallEvent: Ignore incoming CMC reley call"
            android.util.Log.i(r0, r1)
            return
        L_0x018e:
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.VZW
            if (r3 != r6) goto L_0x01b5
            boolean r6 = com.sec.internal.helper.ImsCallUtil.isVideoCall(r5)
            if (r6 == 0) goto L_0x01b5
            android.content.Context r6 = r1.mContext
            android.content.ContentResolver r6 = r6.getContentResolver()
            java.lang.String r12 = com.sec.ims.extensions.Extensions.Settings.Global.MOBILE_DATA
            int r6 = android.provider.Settings.Global.getInt(r6, r12, r7)
            if (r6 != 0) goto L_0x01b5
            boolean r6 = r26.getEpdgStatus()
            if (r6 != 0) goto L_0x01b5
            java.lang.String r5 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.String r6 = "onImsIncomingCallEvent: mobile data is off. Downgrade video call to voice call."
            android.util.Log.i(r5, r6)
            r14 = r7
            goto L_0x01b6
        L_0x01b5:
            r14 = r5
        L_0x01b6:
            com.sec.internal.ims.servicemodules.volte2.data.IncomingCallProfileBuilder r5 = new com.sec.internal.ims.servicemodules.volte2.data.IncomingCallProfileBuilder
            r5.<init>()
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r5 = r5.builder(r4)
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r5 = r5.setCallType(r14)
            int r6 = r26.getPhoneId()
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r5 = r5.setPhoneId(r6)
            com.sec.ims.volte2.data.MediaProfile r6 = new com.sec.ims.volte2.data.MediaProfile
            r6.<init>()
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r5 = r5.setMediaProfile(r6)
            int r6 = r26.getNetworkType()
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r5 = r5.setNetworkType(r6)
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r5 = r5.setDirection(r7)
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r2 = r5.setSamsungMdmnCall(r2)
            java.lang.String r5 = r1.getDialingNumber(r8, r3)
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r2 = r2.setDialingNumber(r5)
            com.sec.internal.helper.os.ITelephonyManager r5 = r1.mTelephonyManager
            boolean r5 = r5.isNetworkRoaming()
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r2 = r2.setDisplayName(r8, r3, r5)
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r5 = r1.mEcholocateController
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r6 = r27.getParams()
            java.lang.String r6 = r6.getSipCallId()
            java.lang.String r5 = r5.getEchoCallId(r6)
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r2 = r2.setEchoCallId(r8, r3, r5)
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r2 = r2.setComposerData(r8)
            com.sec.internal.constants.ims.servicemodules.volte2.CallParams r3 = r27.getParams()
            int r3 = r3.getCmcEdCallSlot()
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r2 = r2.setCmcEdCallSlot(r3)
            com.sec.internal.ims.servicemodules.volte2.data.CallProfileBuilder r2 = r2.setCmcDeviceId(r8, r0, r4)
            com.sec.ims.volte2.data.CallProfile r4 = r2.build()
            com.sec.ims.settings.ImsProfile r2 = r26.getImsProfile()
            int r2 = r2.getCmcType()
            boolean r2 = com.sec.internal.helper.ImsCallUtil.isCmcSecondaryType(r2)
            if (r2 == 0) goto L_0x0239
            int r2 = r26.getPhoneId()
            int r3 = r26.getHandle()
            r1.clearDialogList(r2, r3)
        L_0x0239:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSessionManager r2 = r1.mImsCallSessionManager
            int[] r3 = r1.mTtyMode
            int r5 = r26.getPhoneId()
            r7 = r3[r5]
            r3 = r27
            r5 = r26
            r6 = r14
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r2 = r2.onImsIncomingCallEvent(r3, r4, r5, r6, r7)
            if (r2 != 0) goto L_0x0256
            java.lang.String r0 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.String r1 = "incomingCallSession is null"
            android.util.Log.e(r0, r1)
            return
        L_0x0256:
            int r3 = r26.getPhoneId()
            int r4 = r27.getSessionID()
            r1.updateNrSaModeOnStart(r3, r4)
            r2.requestQuantumPeerProfileStatus(r11, r11)
            com.sec.ims.util.SipError r3 = com.sec.internal.constants.ims.SipErrorBase.OK
            if (r9 != r3) goto L_0x02e9
            android.content.Context r3 = r1.mContext
            boolean r3 = com.sec.internal.helper.os.PackageUtils.isOneTalkFeatureEnabled(r3)
            if (r3 != 0) goto L_0x0311
            int r0 = r26.getPhoneId()
            boolean r0 = r1.isSupportImsDataChannel(r0)
            if (r0 == 0) goto L_0x02c2
            com.sec.internal.ims.core.imsdc.IdcImsCallSessionData r0 = r2.getIdcData()
            if (r0 == 0) goto L_0x02c2
            com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra r0 = new com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra
            java.lang.String r3 = r27.getIdcExtra()
            r0.<init>((java.lang.String) r3)
            java.lang.String r3 = "sdp"
            java.lang.String r3 = r0.getString(r3)
            boolean r4 = android.text.TextUtils.isEmpty(r3)
            if (r4 != 0) goto L_0x02bb
            com.sec.internal.ims.core.imsdc.IdcImsCallSessionData r4 = r2.getIdcData()
            r4.setRemoteBdcSdp(r3)
            com.sec.internal.ims.servicemodules.volte2.idc.IdcServiceHelper r3 = r1.mIdcServiceModule
            r3.onImsIncomingCallIdcEvent(r2, r0)
            com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra$Builder r0 = com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra.getBuilder()
            com.sec.internal.ims.core.imsdc.IdcImsCallSessionData r3 = r2.getIdcData()
            java.lang.String r3 = r3.getLocalBdcSdp()
            com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra$Builder r0 = r0.setSdp(r3)
            com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra r0 = r0.build()
            java.lang.String r0 = r0.encode()
            goto L_0x02c4
        L_0x02bb:
            java.lang.String r0 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.String r3 = "[IDC][FW] No BDC SDP within MT INVITE"
            android.util.Log.i(r0, r3)
        L_0x02c2:
            java.lang.String r0 = ""
        L_0x02c4:
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r3 = r1.mVolteSvcIntf
            int r4 = r27.getSessionID()
            r5 = 0
            int r0 = r3.proceedIncomingCall(r4, r5, r0)
            if (r0 == 0) goto L_0x0311
            r2.terminate(r10)     // Catch:{ RemoteException -> 0x02d5 }
            goto L_0x02df
        L_0x02d5:
            r0 = move-exception
            r3 = r0
            java.lang.String r0 = com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal.LOG_TAG
            java.lang.String r4 = "session already removed: "
            android.util.Log.e(r0, r4, r3)
        L_0x02df:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSessionManager r0 = r1.mImsCallSessionManager
            int r3 = r27.getSessionID()
            r0.removeSession(r3)
            goto L_0x0311
        L_0x02e9:
            com.sec.ims.volte2.data.ImsCallInfo r3 = new com.sec.ims.volte2.data.ImsCallInfo
            int r13 = r2.getCallId()
            r15 = 0
            r16 = 0
            r17 = 0
            r18 = 0
            r19 = 0
            r20 = 0
            r21 = 0
            r22 = 0
            r23 = 0
            r24 = 0
            r12 = r3
            r12.<init>(r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24)
            com.sec.internal.google.SecImsNotifier r4 = com.sec.internal.google.SecImsNotifier.getInstance()
            int r0 = r26.getPhoneId()
            r4.onIncomingPreAlerting(r0, r3)
        L_0x0311:
            com.sec.internal.ims.servicemodules.volte2.VolteNotifier r0 = r1.mVolteNotifier
            r0.notifyIncomingPreAlerting(r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.handlePreAlerting(com.sec.ims.ImsRegistration, com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent, boolean, boolean, com.sec.ims.util.SipError):void");
    }

    private int ignoreIncomingCallSession(ImsRegistration imsRegistration, ImsCallSession imsCallSession, IncomingCallEvent incomingCallEvent, Mno mno) {
        if (!ImsCallUtil.isCmcSecondaryType(imsRegistration.getImsProfile().getCmcType()) || !hasDialingOrIncomingCall()) {
            int phoneId = imsRegistration.getPhoneId();
            int callType = incomingCallEvent.getCallType();
            if (hasCsCall(phoneId, true)) {
                String replaces = incomingCallEvent.getParams().getReplaces();
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str, "ignoreIncomingCallSession: replaces " + replaces);
                if (mno.isKor() || mno.isOneOf(Mno.TMOUS, Mno.SPRINT) || (ImsCallUtil.isCmcPrimaryType(imsRegistration.getImsProfile().getCmcType()) && TextUtils.isEmpty(replaces))) {
                    Log.i(str, "need to reject incoming call.. due to CS Call");
                    this.mVolteSvcIntf.rejectCall(incomingCallEvent.getSessionID(), callType, SipErrorBase.BUSY_HERE);
                    return 1603;
                }
            }
            if (!hasOutgoingCall(phoneId)) {
                return 0;
            }
            String str2 = IVolteServiceModuleInternal.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("ignoreIncomingCallSession: found outgoing call, reject incoming call error=");
            SipError sipError = SipErrorBase.BUSY_HERE;
            sb.append(sipError);
            Log.i(str2, sb.toString());
            this.mVolteSvcIntf.rejectCall(incomingCallEvent.getSessionID(), callType, sipError);
            return 1607;
        }
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "onImsIncomingCallEvent: Ignore incoming CMC reley call");
        return 1602;
    }

    private void setProfileForIncomingCallSession(ImsCallSession imsCallSession, IncomingCallEvent incomingCallEvent, Mno mno, Boolean bool, int i) {
        if (mno != Mno.VZW && ImsCallUtil.isVideoCall(incomingCallEvent.getCallType())) {
            if (!mno.isChn() || !hasDsdaDialingOrIncomingVtOnOtherSlot(incomingCallEvent.getImsRegistration().getPhoneId())) {
                this.mMediaController.stopActiveCamera();
            } else {
                Log.i(IVolteServiceModuleInternal.LOG_TAG, "There's no need to stopActiveCamera when other slot has incoming/outgoing vt call.");
            }
        }
        CallProfile callProfile = imsCallSession.getCallProfile();
        callProfile.setCallType(incomingCallEvent.getCallType());
        callProfile.setRemoteVideoCapa(incomingCallEvent.getRemoteVideoCapa());
        if (isNotifyRejectedCall(incomingCallEvent.getImsRegistration().getPhoneId())) {
            callProfile.setRejectCause(i);
        }
        imsCallSession.updateCallProfile(incomingCallEvent.getParams());
        imsCallSession.startIncoming();
        String replaces = incomingCallEvent.getParams().getReplaces();
        if (bool.booleanValue() && !TextUtils.isEmpty(replaces)) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "Has replaces. Check Dialog Id");
            Log.i(str, "replaceSipCallId: " + replaces);
            callProfile.setReplaceSipCallId(replaces);
        }
        String notifyHistoryInfo = incomingCallEvent.getImsRegistration().getImsProfile().getNotifyHistoryInfo();
        if ((callProfile.getHistoryInfo() != null || callProfile.getHasDiversion()) && !"not_notify".equals(notifyHistoryInfo)) {
            if (callProfile.getHistoryInfo() == null && callProfile.getHasDiversion()) {
                callProfile.setHistoryInfo("");
            } else if ("change_number".equals(notifyHistoryInfo)) {
                callProfile.setDialingNumber(callProfile.getHistoryInfo());
                callProfile.setHistoryInfo("");
            } else if ("toast_only".equals(notifyHistoryInfo)) {
                callProfile.setHistoryInfo("");
            }
        } else if (mno != Mno.DOCOMO) {
            callProfile.setHistoryInfo((String) null);
        }
    }

    /* access modifiers changed from: protected */
    public void onImsIncomingCallEvent(IncomingCallEvent incomingCallEvent, boolean z) {
        SipError sipError;
        IncomingCallEvent incomingCallEvent2 = incomingCallEvent;
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "onImsIncomingCallEvent : sessionId=" + incomingCallEvent.getSessionID() + " peerURI=" + IMSLog.checker(incomingCallEvent.getPeerAddr() + "") + " param=" + incomingCallEvent.getParams() + " type=" + incomingCallEvent.getCallType() + " isDelayedIncoming=" + z);
        boolean z2 = this.mDelayRinging;
        this.mDelayRinging = incomingCallEvent.getParams().getDelayRinging();
        ImsRegistration imsRegistration = incomingCallEvent.getImsRegistration();
        SipError sipError2 = SipErrorBase.OK;
        if (imsRegistration == null) {
            Log.e(str, "Not registered.");
            this.mVolteSvcIntf.rejectCall(incomingCallEvent.getSessionID(), incomingCallEvent.getCallType(), SipErrorBase.NOT_ACCEPTABLE_HERE);
            return;
        }
        if (imsRegistration.getImsProfile().isEnableVcid() && incomingCallEvent.getCallType() != 1) {
            IMSLog.d(str, "VCID/MCID is enabled for only voice call, set Alertinfo null");
            incomingCallEvent.getParams().setAlertInfo((String) null);
        }
        if (imsRegistration.getImsProfile().isEnableVcid() && VcidHelper.isVcidUrlExist(incomingCallEvent.getParams().getAlertInfo()) && !VcidHelper.isVcidCapable(this.mContext, incomingCallEvent.getParams().getAlertInfo())) {
            IMSLog.d(str, "VCID is not capable, set Alertinfo null");
            incomingCallEvent.getParams().setAlertInfo((String) null);
        }
        int phoneId = imsRegistration.getPhoneId();
        ImsCallSession session = getSession(incomingCallEvent.getSessionID());
        int i = 0;
        if (session != null) {
            CallConstants.STATE callState = session.getCallState();
            if (callState == CallConstants.STATE.IncomingCall) {
                Log.e(str, "same session exist.");
                if (z2 && !this.mDelayRinging) {
                    Log.e(str, "something caused delay ringing false, notify to FW directly");
                    session.getCallProfile().setDelayRinging(false);
                    session.getCallProfile().setVideoCRBT(false);
                    notifyOnIncomingCall(phoneId, session.getCallId());
                    return;
                }
                return;
            } else if (ImsCallUtil.isEndCallState(callState)) {
                Log.e(str, "session is already Ending or Ended state");
                return;
            }
        }
        Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        boolean isSamsungMdmnEnabled = imsRegistration.getImsProfile().isSamsungMdmnEnabled();
        if (isSamsungMdmnEnabled) {
            fromName = Mno.MDMN;
        }
        Mno mno = fromName;
        if (!isRunning() || getRegInfo(imsRegistration.getHandle()) == null) {
            Log.e(str, "onImsNewIncomingCallEvent: Unexpected incoming call while volte is off.");
            SipError sipErrorIncomingCallWithVolteOff = this.mImsCallSipErrorFactory.getSipErrorIncomingCallWithVolteOff(this.mContext, incomingCallEvent, this.mMmtelAcquiredEver, imsRegistration);
            if (sipErrorIncomingCallWithVolteOff != sipError2) {
                this.mVolteSvcIntf.rejectCall(incomingCallEvent.getSessionID(), incomingCallEvent.getCallType(), sipErrorIncomingCallWithVolteOff);
                if (!isNotifyRejectedCall(phoneId)) {
                    return;
                }
            }
            sipError = sipErrorIncomingCallWithVolteOff;
        } else {
            sipError = sipError2;
        }
        Log.i(str, "getPreAlerting is " + incomingCallEvent.getPreAlerting());
        if (incomingCallEvent.getPreAlerting()) {
            handlePreAlerting(imsRegistration, incomingCallEvent, isSamsungMdmnEnabled, z, sipError);
            return;
        }
        ImsCallSession incomingCallSession = this.mImsCallSessionManager.getIncomingCallSession(phoneId);
        if (incomingCallSession == null) {
            Log.e(str, "onImsIncomingCallEvent: mIncomingCallSession is null");
            return;
        }
        if (sipError == sipError2) {
            i = ignoreIncomingCallSession(imsRegistration, incomingCallSession, incomingCallEvent, mno);
        }
        int i2 = i;
        setProfileForIncomingCallSession(incomingCallSession, incomingCallEvent, mno, Boolean.valueOf(isSamsungMdmnEnabled), i2);
        ImsProfile imsProfile = incomingCallEvent.getImsRegistration().getImsProfile();
        if (i2 == 0) {
            Log.i(str, "onImsIncomingCallEvent getCmcType : " + imsProfile.getCmcType());
            if (!ImsCallUtil.isCmcPrimaryType(imsProfile.getCmcType())) {
                this.mVolteNotifier.notifyOnIncomingCall(phoneId, incomingCallSession.getCallId());
            }
            post(new VolteServiceModuleInternal$$ExternalSyntheticLambda0(this, incomingCallSession));
            this.mCmcServiceModule.onImsIncomingCallEvent(phoneId, incomingCallSession.getCmcType());
        } else if (!isNotifyRejectedCall(phoneId)) {
            return;
        }
        if (!this.mDelayRinging) {
            notifyOnIncomingCall(phoneId, incomingCallSession.getCallId());
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onImsIncomingCallEvent$0(ImsCallSession imsCallSession) {
        this.mVolteNotifier.notifyIncomingCallEvent(imsCallSession);
    }

    public void notifyOnIncomingCall(int i, int i2) {
        int i3;
        if (ImsRegistry.getCmcAccountManager().isSupportDualSimCMC()) {
            ImsCallSession sessionByCallId = getSessionByCallId(i2);
            if (sessionByCallId.getCmcType() == 1) {
                if (!TextUtils.isEmpty(sessionByCallId.getCallProfile().getReplaceSipCallId())) {
                    i3 = getPhoneIdFromExternalCall();
                } else {
                    i3 = sessionByCallId.getCallProfile().getCmcEdCallSlot();
                }
                if (i3 > -1) {
                    String str = IVolteServiceModuleInternal.LOG_TAG;
                    Log.i(str, "notifyOnIncomingCall SD orig phoneId: " + i + " -> " + i3);
                    i = i3;
                }
            }
        }
        SecImsNotifier.getInstance().onIncomingCall(i, i2);
        this.mDelayRinging = false;
    }

    public boolean getIsLteRetrying(int i) {
        return this.mIsLteRetrying[i];
    }

    public void setIsLteRetrying(int i, boolean z) {
        this.mIsLteRetrying[i] = z;
    }

    private int getPhoneIdFromExternalCall() {
        for (ImsCallSession next : getSessionByState(CallConstants.STATE.InCall)) {
            if (next.getCmcType() == 0) {
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str, "phone id for ps call : " + next.getPhoneId());
                return next.getPhoneId();
            }
        }
        return getCmcServiceHelper().getCsCallPhoneIdByState(1);
    }

    public void setDelayedDeregisterTimerRunning(int i, boolean z) {
        this.mIsDeregisterTimerRunning[i] = z;
        updateCapabilities(i);
    }

    private class PhoneStateListenerInternal extends PhoneStateListener {
        int mPhoneId;
        int mState = 0;
        int mSubId;

        public PhoneStateListenerInternal(int i, int i2) {
            this.mSubId = i2;
            this.mPhoneId = i;
        }

        public int getInternalPhoneId() {
            return this.mPhoneId;
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            VolteServiceModuleInternal.this.mMobileCareController.onSignalStrengthsChanged(this.mPhoneId, signalStrength);
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "onServiceStateChanged(" + serviceState + ")");
            ServiceStateWrapper serviceStateWrapper = new ServiceStateWrapper(serviceState);
            boolean z = false;
            if (isOutOfServiceState(serviceStateWrapper)) {
                if (isCallEndByCsEvent(serviceStateWrapper)) {
                    VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                    volteServiceModuleInternal.sendMessage(volteServiceModuleInternal.obtainMessage(10, this.mPhoneId, 0));
                } else if (isUssdEndByCsEvent(serviceStateWrapper)) {
                    VolteServiceModuleInternal volteServiceModuleInternal2 = VolteServiceModuleInternal.this;
                    volteServiceModuleInternal2.sendMessage(volteServiceModuleInternal2.obtainMessage(36, this.mPhoneId, 0));
                }
            }
            boolean[] zArr = VolteServiceModuleInternal.this.mIsLteEpsOnlyAttached;
            int i = this.mPhoneId;
            if (serviceStateWrapper.getDataRegState() == 0 && NetworkUtil.is3gppPsVoiceNetwork(serviceStateWrapper.getDataNetworkType()) && serviceStateWrapper.isPsOnlyReg()) {
                z = true;
            }
            zArr[i] = z;
            VolteServiceModuleInternal.this.mDataAccessNetwork[this.mPhoneId] = serviceStateWrapper.getMobileDataNetworkType();
            Log.i(str, "mIsLteEpsOnlyAttached(" + this.mPhoneId + "):" + VolteServiceModuleInternal.this.mIsLteEpsOnlyAttached[this.mPhoneId]);
        }

        private boolean isOutOfServiceState(ServiceStateWrapper serviceStateWrapper) {
            return !VolteServiceModuleInternal.this.mPdnController.isEpdgConnected(this.mPhoneId) && serviceStateWrapper.getDataRegState() == 1;
        }

        private boolean isCallEndByCsEvent(ServiceStateWrapper serviceStateWrapper) {
            Mno simMno = SimUtil.getSimMno(this.mPhoneId);
            if (Mno.TMOUS.equals(simMno)) {
                if (serviceStateWrapper.getVoiceRegState() == 0 && serviceStateWrapper.getVoiceNetworkType() == 16 && serviceStateWrapper.getDataNetworkType() == 0) {
                    return true;
                }
                return false;
            } else if (simMno == Mno.TELSTRA || simMno == Mno.KDDI || simMno.isTw()) {
                if (serviceStateWrapper.getVoiceRegState() == 1 && serviceStateWrapper.getDataNetworkType() == 0) {
                    return true;
                }
                return false;
            } else if (!simMno.isKor() || serviceStateWrapper.getVoiceRegState() != 1) {
                return false;
            } else {
                return true;
            }
        }

        private boolean isUssdEndByCsEvent(ServiceStateWrapper serviceStateWrapper) {
            if (SimUtil.getSimMno(this.mPhoneId) == Mno.VODAFONE_INDIA && serviceStateWrapper.getVoiceRegState() == 0 && serviceStateWrapper.getVoiceNetworkType() == 16 && serviceStateWrapper.getDataNetworkType() == 0) {
                return true;
            }
            return false;
        }

        public void onCallStateChanged(int i, String str) {
            if (this.mState != i) {
                String str2 = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str2, "onCallStateChanged: state " + i);
                this.mState = i;
                VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                volteServiceModuleInternal.sendMessage(volteServiceModuleInternal.obtainMessage(5, this.mPhoneId, i));
            }
        }

        public void onBarringInfoChanged(BarringInfo barringInfo) {
            boolean z = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Call.SUPPORT_SSAC_NR, false);
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "onBarringInfoChanged: barringInfo " + barringInfo + " uacConvertSsac " + z);
            if (!(barringInfo.getCellIdentity() instanceof CellIdentityNr) || z) {
                VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                volteServiceModuleInternal.sendMessage(volteServiceModuleInternal.obtainMessage(37, this.mPhoneId, 0, barringInfo));
            }
        }

        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState preciseDataConnectionState) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            IMSLog.s(str, "onPreciseDataConnectionStateChanged: state=" + preciseDataConnectionState);
            if (preciseDataConnectionState != null && preciseDataConnectionState.getDataConnectionState() == 0) {
                int dataConnectionFailCause = preciseDataConnectionState.getDataConnectionFailCause();
                if (preciseDataConnectionState.getApnSetting() != null && (preciseDataConnectionState.getApnSetting().getApnTypeBitmask() & 512) == 512 && dataConnectionFailCause != 0) {
                    Log.i(str, "ePDN setup failed");
                    VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                    volteServiceModuleInternal.sendMessage(volteServiceModuleInternal.obtainMessage(19, this.mPhoneId, 0));
                }
            }
        }
    }

    public void notifyImsCallEventForVideo(ImsCallSession imsCallSession, IMSMediaEvent iMSMediaEvent) {
        this.mVolteNotifier.notifyImsCallEventForVideo(imsCallSession, iMSMediaEvent);
    }

    public void sendEmergencyCallTimerState(int i, ImsCallSession imsCallSession, ImsCallUtil.EMERGENCY_TIMER emergency_timer, ImsCallUtil.EMERGENCY_TIMER_STATE emergency_timer_state) {
        if (Mno.TMOUS.equals(SimUtil.getSimMno(i)) && imsCallSession != null && this.mEcholocateController != null) {
            if (imsCallSession.getTimerState(emergency_timer.ordinal()) == emergency_timer_state.ordinal()) {
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.e(str, "sendEmergencyCallTimerState: timer[" + emergency_timer.name() + "], state[" + emergency_timer_state.name() + "] is same, Just return");
                return;
            }
            imsCallSession.setTimerState(emergency_timer.ordinal(), emergency_timer_state.ordinal());
            EcholocateEvent.EcholocateEmergencyMessage echolocateEmergencyMessage = new EcholocateEvent.EcholocateEmergencyMessage(imsCallSession.getCallProfile().getDialingNumber(), emergency_timer.name(), emergency_timer_state.name(), imsCallSession.getCallProfile().getEchoCallId(), imsCallSession.isEpdgCall());
            String str2 = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str2, "sendEmergencyCallTimerState: timer[" + emergency_timer.name() + "], state[" + emergency_timer_state.name() + "]");
            this.mEcholocateController.handleEmergencyCallTimerState(i, echolocateEmergencyMessage);
        }
    }

    public void handleDedicatedEventAfterHandover(int i) {
        this.mEcholocateController.handleDedicatedEventAfterHandover(i);
    }

    /* access modifiers changed from: private */
    public void onTelephonyNotResponding() {
        this.mMobileCareController.sendTelephonyNotResponding(getSessionList());
        SystemWrapper.exit(0);
    }

    /* access modifiers changed from: protected */
    public void registerMissedSmsReceiver(boolean z, int i) {
        BroadcastReceiver broadcastReceiver;
        String phraseByMno = ImsCallUtil.getPhraseByMno(this.mContext, i);
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "missedcallSmsphrase = " + phraseByMno);
        this.mIsMissedCallSmsChecking[i] = !TextUtils.isEmpty(phraseByMno) && z;
        boolean z2 = false;
        for (int i2 = 0; i2 < this.mMaxPhoneCount; i2++) {
            if (this.mIsMissedCallSmsChecking[i2]) {
                z2 = true;
            }
        }
        if (z2 && this.mMissedSmsIntentReceiver == null) {
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "register mMissedSmsIntentReceiver");
            this.mMissedSmsIntentReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
                        Log.i(IVolteServiceModuleInternal.LOG_TAG, "receive android.provider.Telephony.SMS_RECEIVED");
                        VolteServiceModuleInternal.this.handleMissedCallSms(intent);
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            this.mContext.registerReceiver(this.mMissedSmsIntentReceiver, intentFilter);
        } else if (!z2 && (broadcastReceiver = this.mMissedSmsIntentReceiver) != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mMissedSmsIntentReceiver = null;
        }
    }

    /* access modifiers changed from: private */
    public void handleMissedCallSms(Intent intent) {
        this.mMobileCareController.sendMissedCallSms(intent);
    }

    public boolean hasDsdaDialingOrIncomingVtOnOtherSlot(int i) {
        return this.mImsCallSessionManager.hasDsdaDialingOrIncomingVtOnOtherSlot(i);
    }

    private boolean isRequiredKorSpecificURN(int i, String str) {
        Mno simMno = SimUtil.getSimMno(i);
        Mno simMnoAsNwPlmn = SimUtil.getSimMnoAsNwPlmn(i);
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration != null) {
            simMno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        }
        if (simMno.isKor()) {
            return true;
        }
        int parseInt = !TextUtils.isEmpty(str) ? Integer.parseInt(str) : -1;
        if (!simMnoAsNwPlmn.isKor()) {
            return false;
        }
        if (parseInt == 6 || parseInt == 7 || parseInt == 3 || parseInt == 18 || parseInt == 19 || parseInt == 9) {
            return true;
        }
        return false;
    }

    public boolean isMdmiEnabled(int i) {
        return ImsRegistry.getBoolean(i, GlobalSettingsConstants.Call.ENABLE_MDMI, false);
    }

    public boolean isQSSSuccessAuthAndLogin(int i) {
        if (i != 0) {
            return false;
        }
        boolean isSuccessAuthAndLogin = getServiceModuleManager().getQuantumEncryptionServiceModule().isSuccessAuthAndLogin();
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "phoneId: " + i + ", isQSSSuccessAuthAndLogin: " + isSuccessAuthAndLogin);
        return isSuccessAuthAndLogin;
    }

    public void updateNrSaModeOnStart(int i, int i2) {
        IRegistrationManager iRegistrationManager;
        if (getSessionCount(i) == 1 && (iRegistrationManager = this.mRegMan) != null && iRegistrationManager.isSupportVoWiFiDisable5GSAFromConfiguration(i)) {
            new Thread(new VolteServiceModuleInternal$$ExternalSyntheticLambda1(this, i, i2)).start();
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$updateNrSaModeOnStart$1(int i, int i2) {
        int subId = SimUtil.getSubId(i);
        byte[] bArr = {2, -124, 0, 4};
        byte[] bArr2 = {0, 0, 0, 0};
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "updateNrSaModeOnStart : start, subId : " + subId);
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (telephonyManager != null) {
            int invokeOemRilRequestRawForSubscriber = telephonyManager.invokeOemRilRequestRawForSubscriber(subId, bArr, bArr2);
            Log.i(str, "updateNrSaModeOnStart : respLen : " + invokeOemRilRequestRawForSubscriber);
            if (invokeOemRilRequestRawForSubscriber > 3) {
                byte b = bArr2[0];
                boolean z = b == 0 || b == 2 || b == 3;
                this.mRegMan.updateNrSaMode(i, z);
                if (ImsRegistry.getBoolean(i, GlobalSettingsConstants.Call.IS_SUPPORT_UPDATE_SA_MODE_ON_START, false) && z) {
                    this.mVolteSvcIntf.updateNrSaModeOnStart(i2);
                }
            }
        }
    }

    public void notifyDSDAVideoCapa(int i) {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "notifyDSDAVideoCapa : phoneId : " + i);
        int i2 = ImsConstants.Phone.SLOT_1;
        if (i == i2) {
            i2 = ImsConstants.Phone.SLOT_2;
        }
        if (!hasEstablishedCall(i2)) {
            Log.i(str, "notifyDSDAVideoCapa : There is no active call on phoneId " + i2);
            return;
        }
        this.mImsCallSessionManager.notifyDSDAVideoCapa(!hasEstablishedCall(i));
    }

    public IIdcServiceHelper getIdcServiceHelper() {
        return this.mIdcServiceModule;
    }
}
