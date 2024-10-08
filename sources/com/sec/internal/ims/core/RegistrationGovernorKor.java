package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Message;
import android.os.SemSystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImModule;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.DmProfileLoader;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import com.sec.internal.ims.settings.ImsProfileCache;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RegistrationGovernorKor extends RegistrationGovernorBase {
    private static final Long DEFAULT_RETRY_AFTER_BUFFER_MS = 500L;
    private static final long DEFAULT_TIMS_TIMER_MS = 60000;
    private static final long DNS_RETRY_TIME_MS = 4000;
    private static final int IMS_NOT_AVAILABLE_REG_FAIL_RETRY = 2;
    protected static final String INTENT_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
    protected static final String INTENT_USIMDOWNLOAD_END = "com.sec.android.UsimRegistrationKOR.UsimDownload.end";
    protected static final String INTENT_WAP_PUSH_DM_NOTI_RECEIVED = "com.samsung.provider.Telephony.WAP_PUSH_DM_NOTI_RECEIVED";
    private static final int MAX_REQUESTPDN_COUNT = 5;
    private static final String OMADM_KT_DEFAULT_PCSCF = "volte.imskt.com";
    private static final String OMADM_SKT_DEFAULT_PCSCF = "172.28.109.141,fd00:0e15:0501:5::141,172.28.109.73,fd00:e15:301:5::73,211.188.227.140,2001:2d8:e0:227::140";
    static final long REG_RETRY_MAX_TIME_FOR_UNLIMITED_404_MS = 14400000;
    private static final int REQUESTPDN_INTERVAL = 3;
    private static final long REQUEST_INTERNETPDN_TIMER_MS = 30000;
    /* access modifiers changed from: private */
    public String LOG_TAG = null;
    private final String[] allowedPackages = {"com.sec.imsservice", "com.skt.skaf.OA00199800", "com.samsung.android.app.telephonyui", "root", "com.android.shell"};
    /* access modifiers changed from: private */
    public long mAllowedNetworkType = -1;
    private AllowedNetworkTypesListener mAllowedNetworkTypesListener = null;
    private int mConsecutiveForbiddenCounter = 0;
    Message mDmPollingTimer = null;
    boolean mDmUpdatedFlag = false;
    int mDnsQueryCount = 0;
    private boolean mHasNetworkFailure = false;
    protected boolean mHasPendingInitRegistrationByDMConfigChange = false;
    protected boolean mHasPendingNotifyImsNotAvailable = false;
    protected BroadcastReceiver mIntentReceiverKor;
    private boolean mIpsecEnabled = true;
    private boolean mIsAkaChallengeTimeout = false;
    protected boolean mIsShipBuild = false;
    private List<InetAddress> mLocalAddress = null;
    private boolean mLteModeOn = true;
    private boolean mNeedDelayedDeregister = false;
    protected Message mPDNdisconnectTimeout = null;
    private List<String> mPcscfList;
    List<String> mRcsPcscfList;
    private int mRequestPdnTimeoutCount = 0;
    private boolean mSmsOverIp = false;
    private int mSubId;
    protected boolean mVolteServiceStatus = true;

    protected enum VoltePreferenceChangedReason {
        VOLTE_SETTING,
        LTE_MODE
    }

    private boolean checkValidRejectCode(int i) {
        return i == 2 || i == 3 || i == 6 || i == 8;
    }

    public RegistrationGovernorKor(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        if (registerTask.isRcsOnly()) {
            this.LOG_TAG = "RegiGvnKor-RCS<" + this.mPhoneId + ">";
        } else if (this.mTask.getProfile().hasEmergencySupport()) {
            this.LOG_TAG = "RegiGvnKor-EMC<" + this.mPhoneId + ">";
        } else {
            this.LOG_TAG = "RegiGvnKor<" + this.mPhoneId + ">";
        }
        this.mDmUpdatedFlag = false;
        this.mVolteServiceStatus = getVolteServiceStatus();
        this.mDnsQueryCount = 0;
        this.mPcscfList = new ArrayList();
        this.mRcsPcscfList = new ArrayList();
        this.mIpsecEnabled = true;
        this.mSmsOverIp = false;
        this.mLteModeOn = true;
        this.mThrottledforImsNotAvailable = false;
        this.mIsShipBuild = CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_PERIODIC_POLLING_TIMEOUT);
        intentFilter.addAction(ImsConstants.Intents.ACTION_FLIGHT_MODE);
        intentFilter.addAction(ImsConstants.Intents.ACTION_AIRPLANE_MODE);
        intentFilter.addAction(INTENT_USIMDOWNLOAD_END);
        intentFilter.addAction(ImsConstants.Intents.INTENT_ACTION_REGIST_REJECT);
        intentFilter.addAction(ImsConstants.Intents.INTENT_ACTION_LTE_REJECT);
        intentFilter.addAction(INTENT_WAP_PUSH_DM_NOTI_RECEIVED);
        intentFilter.addAction(INTENT_NEW_OUTGOING_CALL);
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        Log.i(this.LOG_TAG, "intent added");
        AnonymousClass1 r4 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String r3 = RegistrationGovernorKor.this.LOG_TAG;
                Log.i(r3, "onReceive:" + intent.getAction() + " mTask:" + RegistrationGovernorKor.this.mTask.getProfile().getName() + "(" + RegistrationGovernorKor.this.mTask.getState() + ")");
                String action = intent.getAction();
                action.hashCode();
                char c = 65535;
                switch (action.hashCode()) {
                    case -2046982008:
                        if (action.equals(ImsConstants.Intents.INTENT_ACTION_LTE_REJECT)) {
                            c = 0;
                            break;
                        }
                        break;
                    case -1822432213:
                        if (action.equals(ImsConstants.Intents.ACTION_FLIGHT_MODE)) {
                            c = 1;
                            break;
                        }
                        break;
                    case -1422817023:
                        if (action.equals(ImsConstants.Intents.INTENT_ACTION_REGIST_REJECT)) {
                            c = 2;
                            break;
                        }
                        break;
                    case -1076576821:
                        if (action.equals(ImsConstants.Intents.ACTION_AIRPLANE_MODE)) {
                            c = 3;
                            break;
                        }
                        break;
                    case -1065317266:
                        if (action.equals(RegistrationGovernorKor.INTENT_WAP_PUSH_DM_NOTI_RECEIVED)) {
                            c = 4;
                            break;
                        }
                        break;
                    case -909058917:
                        if (action.equals(RegistrationGovernorKor.INTENT_USIMDOWNLOAD_END)) {
                            c = 5;
                            break;
                        }
                        break;
                    case 798292259:
                        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                            c = 6;
                            break;
                        }
                        break;
                    case 1402086673:
                        if (action.equals(ImsConstants.Intents.ACTION_PERIODIC_POLLING_TIMEOUT)) {
                            c = 7;
                            break;
                        }
                        break;
                    case 1901012141:
                        if (action.equals(RegistrationGovernorKor.INTENT_NEW_OUTGOING_CALL)) {
                            c = 8;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                    case 2:
                        RegistrationGovernorKor.this.handleNwRejectIntent(intent);
                        return;
                    case 1:
                        RegistrationGovernorKor.this.handleFlightModeIntent(intent);
                        return;
                    case 3:
                        RegistrationGovernorKor.this.handleAirplaneModeIntent(intent);
                        return;
                    case 4:
                        RegistrationGovernorKor.this.handleWapPushDmNotiReceivedIntent();
                        return;
                    case 5:
                        RegistrationGovernorKor.this.handleUsimDownloadEndIntent();
                        return;
                    case 6:
                        RegistrationGovernorKor.this.handleBootCompletedIntent();
                        return;
                    case 7:
                        RegistrationGovernorKor.this.handlePeriodicPollingTimeoutIntent();
                        return;
                    case 8:
                        RegistrationGovernorKor.this.handleNewOutgoingCallIntent();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mIntentReceiverKor = r4;
        this.mContext.registerReceiver(r4, intentFilter);
        updateEutranValues();
        int rcsUserSetting = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId);
        if (rcsUserSetting == -2) {
            this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "Stucked on RCS_DISABLED_BY_NETWORK. Force to DISABLED.");
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 0, this.mPhoneId);
        } else if (rcsUserSetting == 2) {
            this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "Stucked on RCS_TURNING_OFF. Force to ENABLED.");
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 1, this.mPhoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkUnprocessedOmadmConfig() {
        NetworkEvent networkEvent;
        if (this.mRegMan != null && this.mTask.isNeedOmadmConfig() && OmcCode.isKOROmcCode() && (networkEvent = this.mRegMan.getNetworkEvent(this.mPhoneId)) != null && !networkEvent.isDataRoaming) {
            Log.i(this.LOG_TAG, "checkUnprocessedOmadmConfig");
            this.mRegHandler.sendCheckUnprocessedOmadmConfig(this.mTask);
        }
    }

    public void onRegistrationTerminated(SipError sipError, long j, boolean z) {
        if (!needImsNotAvailable() || (needImsNotAvailable() && j > 0)) {
            stopTimsTimer(RegistrationConstants.REASON_REGISTRATION_ERROR);
        }
        if (SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(sipError) || SipErrorBase.NOTIFY_TERMINATED_PROBATION.equals(sipError)) {
            Log.e(this.LOG_TAG, "onRegistrationError: Notify terminated expired.");
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        } else if (SipErrorBase.OK.equals(sipError)) {
            this.mFailureCounter = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            stopPDNdisconnectTimer();
            stopRetryTimer();
            startRetryTimer(1000);
            this.mDnsQueryCount = 0;
        } else if (SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(sipError)) {
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            stopPDNdisconnectTimer();
            stopRetryTimer();
            resetIPSecAllow();
            this.mRegMan.getEventLog().logAndAdd("onRegistrationError: Notify terminated rejected.");
            this.mIsPermanentStopped = true;
            this.mDnsQueryCount = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void handleForbiddenError(long j) {
        int i = this.mConsecutiveForbiddenCounter + 1;
        this.mConsecutiveForbiddenCounter = i;
        if (i >= 2) {
            this.mRegMan.getEventLog().logAndAdd("onRegistrationError: Two consecutive 403 errors");
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            stopPDNdisconnectTimer();
            stopRetryTimer();
            resetIPSecAllow();
            this.mIsPermanentStopped = true;
            makeRegistrationFailedToast();
            return;
        }
        Log.i(this.LOG_TAG, "onRegistrationError: 403 error. Need OmaDM trial only for KOR device in domestic");
        if (this.mTask.isNeedOmadmConfig()) {
            if (!OmcCode.isKOROmcCode() || this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming) {
                this.mRegHandler.sendTryRegister(this.mPhoneId);
            } else {
                this.mRegHandler.sendRequestDmConfig(this.mTask);
            }
        }
        if (this.mTask.getProfile().getNeedAutoconfig()) {
            this.mConfigModule.startAcs(this.mPhoneId);
        }
        if (!this.mTask.isNeedOmadmConfig() && !this.mTask.getProfile().getNeedAutoconfig()) {
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:106:0x02e2  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x02e8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onRegistrationError(com.sec.ims.util.SipError r8, long r9, boolean r11) {
        /*
            r7 = this;
            com.sec.internal.ims.core.RegistrationManagerInternal r11 = r7.mRegMan
            com.sec.internal.helper.SimpleEventLog r11 = r11.getEventLog()
            int r0 = r7.mPhoneId
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onRegistrationError: state "
            r1.append(r2)
            com.sec.internal.ims.core.RegisterTask r2 = r7.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = r2.getState()
            r1.append(r2)
            java.lang.String r2 = " error "
            r1.append(r2)
            r1.append(r8)
            java.lang.String r2 = " retryAfterMs "
            r1.append(r2)
            r1.append(r9)
            java.lang.String r2 = " mCurPcscfIpIdx "
            r1.append(r2)
            int r2 = r7.mCurPcscfIpIdx
            r1.append(r2)
            java.lang.String r2 = " mNumOfPcscfIp "
            r1.append(r2)
            int r2 = r7.mNumOfPcscfIp
            r1.append(r2)
            java.lang.String r2 = " mFailureCounter "
            r1.append(r2)
            int r2 = r7.mFailureCounter
            r1.append(r2)
            java.lang.String r2 = " mIsPermanentStopped "
            r1.append(r2)
            boolean r2 = r7.mIsPermanentStopped
            r1.append(r2)
            java.lang.String r2 = " mTask.mIsRefreshReg "
            r1.append(r2)
            com.sec.internal.ims.core.RegisterTask r2 = r7.mTask
            boolean r2 = r2.isRefreshReg()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r11.logAndAdd(r0, r1)
            com.sec.internal.ims.core.RegisterTask r11 = r7.mTask
            boolean r11 = r11.isRefreshReg()
            com.sec.internal.ims.core.RegisterTask r0 = r7.mTask
            r1 = 41
            r0.setDeregiReason(r1)
            r0 = 0
            int r2 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r2 >= 0) goto L_0x007c
            r9 = r0
        L_0x007c:
            boolean r2 = r7.needImsNotAvailable()
            if (r2 == 0) goto L_0x008c
            boolean r2 = r7.needImsNotAvailable()
            if (r2 == 0) goto L_0x0091
            int r2 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r2 <= 0) goto L_0x0091
        L_0x008c:
            java.lang.String r2 = "RegistrationError"
            r7.stopTimsTimer(r2)
        L_0x0091:
            com.sec.ims.util.SipError r2 = com.sec.internal.constants.ims.SipErrorKor.AKA_CHANLENGE_TIMEOUT
            boolean r2 = r2.equals(r8)
            r3 = 1
            r4 = 0
            if (r2 == 0) goto L_0x00f9
            com.sec.internal.constants.Mno r8 = r7.mMno
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.SKT
            if (r8 != r9) goto L_0x00b5
            android.content.Context r8 = r7.mContext
            android.content.res.Resources r9 = r8.getResources()
            r10 = 2131165187(0x7f070003, float:1.7944584E38)
            java.lang.String r9 = r9.getString(r10)
            android.widget.Toast r8 = android.widget.Toast.makeText(r8, r9, r3)
            r8.show()
        L_0x00b5:
            java.lang.String r8 = r7.LOG_TAG
            java.lang.String r9 = "onRegistrationError: Permanently prohibited."
            android.util.Log.e(r8, r9)
            r7.mFailureCounter = r4
            r7.mRegiAt = r0
            r7.mConsecutiveForbiddenCounter = r4
            r7.mIsAkaChallengeTimeout = r3
            r7.mDnsQueryCount = r4
            r7.stopPDNdisconnectTimer()
            r7.stopRetryTimer()
            r7.resetIPSecAllow()
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            r9 = 71
            r8.setDeregiReason(r9)
            com.sec.internal.ims.core.RegistrationManagerInternal r8 = r7.mRegMan
            com.sec.internal.helper.SimpleEventLog r8 = r8.getEventLog()
            java.lang.String r9 = "onRegistrationError: Aka challenge timeout"
            r8.logAndAdd(r9)
            r7.mIsPermanentStopped = r3
            r7.resetPcscfList()
            com.sec.internal.ims.core.RegistrationManagerInternal r8 = r7.mRegMan
            com.sec.internal.ims.core.RegisterTask r9 = r7.mTask
            java.lang.String r10 = "Aka challenge timeout"
            r8.deregister(r9, r3, r3, r10)
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r9 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            r8.setState(r9)
            r7.mDnsQueryCount = r4
            return
        L_0x00f9:
            boolean r2 = r7.needImsNotAvailable()
            if (r2 == 0) goto L_0x0103
            r7.onRegErrorforImsNotAvailable(r8, r9)
            return
        L_0x0103:
            com.sec.ims.util.SipError r2 = com.sec.internal.constants.ims.SipErrorBase.USE_PROXY
            boolean r2 = r2.equals(r8)
            r5 = 1000(0x3e8, double:4.94E-321)
            if (r2 == 0) goto L_0x015a
            com.sec.internal.ims.core.RegisterTask r2 = r7.mTask
            boolean r2 = r2.isRefreshReg()
            if (r2 == 0) goto L_0x015a
            int r8 = r7.mCurPcscfIpIdx
            int r8 = r8 + r3
            int r11 = r7.mNumOfPcscfIp
            if (r8 != r11) goto L_0x014a
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r8 = r8.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r9 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTED
            if (r8 != r9) goto L_0x0132
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            boolean r8 = r8.isRcsOnly()
            if (r8 != 0) goto L_0x0132
            r7.startPDNdisconnectTimer(r5)
            goto L_0x0133
        L_0x0132:
            r3 = r4
        L_0x0133:
            r7.mCurPcscfIpIdx = r4
            r7.mFailureCounter = r4
            r7.mConsecutiveForbiddenCounter = r4
            r7.mIsAkaChallengeTimeout = r4
            r7.mDnsQueryCount = r4
            r7.resetIPSecAllow()
            java.lang.String r8 = r7.LOG_TAG
            java.lang.String r9 = "onRegistrationError: 305 error. do initial regi. at the 1st P-CSCF after disconnecting/connecting IMS PDN"
            android.util.Log.i(r8, r9)
            r4 = r3
            r9 = r5
            goto L_0x0154
        L_0x014a:
            r7.moveToNextPcscfAndInitialRegister()
            java.lang.String r8 = r7.LOG_TAG
            java.lang.String r11 = "onRegistrationError: 305 error. do initial regi. at the next P-CSCF"
            android.util.Log.i(r8, r11)
        L_0x0154:
            if (r4 != 0) goto L_0x0159
            r7.startRetryTimer(r9)
        L_0x0159:
            return
        L_0x015a:
            boolean r2 = com.sec.internal.constants.ims.SipErrorBase.isImsForbiddenError(r8)
            if (r2 == 0) goto L_0x0164
            r7.handleForbiddenError(r9)
            return
        L_0x0164:
            com.sec.ims.util.SipError r2 = com.sec.internal.constants.ims.SipErrorBase.NOT_ACCEPTABLE
            boolean r2 = r2.equals(r8)
            if (r2 == 0) goto L_0x0185
            java.lang.String r8 = r7.LOG_TAG
            java.lang.String r11 = "onRegistrationError: 406 error. Ipsec not allow"
            android.util.Log.i(r8, r11)
            r7.mIPsecAllow = r4
            int r8 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r8 <= 0) goto L_0x017d
            r7.startRetryTimer(r9)
            goto L_0x0184
        L_0x017d:
            com.sec.internal.ims.core.RegistrationManagerHandler r8 = r7.mRegHandler
            int r7 = r7.mPhoneId
            r8.sendTryRegister(r7)
        L_0x0184:
            return
        L_0x0185:
            com.sec.ims.util.SipError r2 = com.sec.internal.constants.ims.SipErrorBase.SERVICE_UNAVAILABLE
            boolean r2 = r2.equals(r8)
            if (r2 == 0) goto L_0x01de
            int r2 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r2 > 0) goto L_0x01de
            if (r11 != 0) goto L_0x01de
            java.lang.String r8 = r7.LOG_TAG
            java.lang.String r9 = "onRegistrationError: 503 error with no retry-after. do initial regi."
            android.util.Log.i(r8, r9)
            int r8 = r7.mCurPcscfIpIdx
            int r8 = r8 + r3
            int r9 = r7.mNumOfPcscfIp
            if (r8 != r9) goto L_0x01ce
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r8 = r8.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r9 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTED
            if (r8 != r9) goto L_0x01b7
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            boolean r8 = r8.isRcsOnly()
            if (r8 != 0) goto L_0x01b7
            r7.startPDNdisconnectTimer(r5)
            goto L_0x01b8
        L_0x01b7:
            r3 = r4
        L_0x01b8:
            r7.resetIPSecAllow()
            r7.mDnsQueryCount = r4
            r7.mCurPcscfIpIdx = r4
            r7.mConsecutiveForbiddenCounter = r4
            r7.mIsAkaChallengeTimeout = r4
            r7.mFailureCounter = r4
            java.lang.String r8 = r7.LOG_TAG
            java.lang.String r9 = "onRegistrationError: 503 error with no retry-after. do initial regi. at the 1st P-CSCF after disconnecting/connecting IMS PDN"
            android.util.Log.i(r8, r9)
            r4 = r3
            goto L_0x01d8
        L_0x01ce:
            r7.moveToNextPcscfAndInitialRegister()
            java.lang.String r8 = r7.LOG_TAG
            java.lang.String r9 = "onRegistrationError: 503 error with no retry-after. do initial regi. at the next P-CSCF"
            android.util.Log.i(r8, r9)
        L_0x01d8:
            if (r4 != 0) goto L_0x01dd
            r7.startRetryTimer(r5)
        L_0x01dd:
            return
        L_0x01de:
            java.lang.String r2 = r7.LOG_TAG
            java.lang.String r5 = "onRegistrationError: etc mIsRefreshReg"
            android.util.Log.i(r2, r5)
            if (r11 == 0) goto L_0x022f
            java.lang.String r8 = r7.LOG_TAG
            android.util.Log.i(r8, r5)
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r8 = r8.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r11 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTED
            if (r8 != r11) goto L_0x020f
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            boolean r8 = r8.isRcsOnly()
            if (r8 != 0) goto L_0x020f
            r7.notifyReattachToRil()
            com.sec.internal.ims.core.RegistrationManagerInternal r8 = r7.mRegMan
            com.sec.internal.ims.core.RegisterTask r11 = r7.mTask
            int r11 = r11.getPdnType()
            com.sec.internal.ims.core.RegisterTask r2 = r7.mTask
            r8.stopPdnConnectivity(r11, r2)
            goto L_0x0210
        L_0x020f:
            r3 = r4
        L_0x0210:
            r7.mCurPcscfIpIdx = r4
            r7.mFailureCounter = r4
            r7.mConsecutiveForbiddenCounter = r4
            r7.mIsAkaChallengeTimeout = r4
            r7.mDnsQueryCount = r4
            r7.resetIPSecAllow()
            int r8 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r8 <= 0) goto L_0x0227
            if (r3 != 0) goto L_0x022e
            r7.startRetryTimer(r9)
            goto L_0x022e
        L_0x0227:
            com.sec.internal.ims.core.RegistrationManagerHandler r8 = r7.mRegHandler
            int r7 = r7.mPhoneId
            r8.sendTryRegister(r7)
        L_0x022e:
            return
        L_0x022f:
            int r11 = r7.mCurPcscfIpIdx
            int r11 = r11 + r3
            r7.mCurPcscfIpIdx = r11
            int r11 = r7.mNumOfPcscfIp
            r2 = 2
            if (r11 < r2) goto L_0x023c
            r7.resetIPSecAllow()
        L_0x023c:
            com.sec.ims.util.SipError r11 = com.sec.internal.constants.ims.SipErrorBase.NOT_FOUND
            boolean r11 = r11.equals(r8)
            if (r11 == 0) goto L_0x0276
            boolean r11 = r7.needToHandleUnlimited404()
            if (r11 == 0) goto L_0x0276
            long r9 = r7.getActualWaitTimeForUnlimited404()
            java.lang.String r8 = r7.LOG_TAG
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r5 = "it would be infinite 404 response. "
            r11.append(r5)
            r11.append(r9)
            java.lang.String r11 = r11.toString()
            android.util.Log.d(r8, r11)
            int r8 = r7.mFailureCounter
            int r8 = r8 + r3
            r7.mFailureCounter = r8
            int r8 = r7.mCurPcscfIpIdx
            int r11 = r7.mNumOfPcscfIp
            int r11 = java.lang.Math.max(r2, r11)
            if (r8 != r11) goto L_0x02dd
            r7.mCurPcscfIpIdx = r4
            goto L_0x02dd
        L_0x0276:
            int r11 = r7.mCurPcscfIpIdx
            int r5 = r7.mNumOfPcscfIp
            int r2 = java.lang.Math.max(r2, r5)
            if (r11 != r2) goto L_0x02dd
            r7.mCurPcscfIpIdx = r4
            int r11 = r7.mFailureCounter
            int r11 = r11 + r3
            r7.mFailureCounter = r11
            int r11 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r11 <= 0) goto L_0x0293
            java.lang.String r11 = r7.LOG_TAG
            java.lang.String r2 = "onRegistrationError: retryAfter from SIP header"
            android.util.Log.i(r11, r2)
            goto L_0x0297
        L_0x0293:
            long r9 = r7.getActualWaitTime()
        L_0x0297:
            com.sec.ims.util.SipError r11 = com.sec.internal.constants.ims.SipErrorBase.SIP_TIMEOUT
            boolean r8 = r11.equals(r8)
            if (r8 == 0) goto L_0x02c0
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r8 = r8.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r11 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTED
            if (r8 != r11) goto L_0x02c0
            com.sec.internal.ims.core.RegisterTask r8 = r7.mTask
            boolean r8 = r8.isRcsOnly()
            if (r8 != 0) goto L_0x02b5
            r7.startPDNdisconnectTimer(r9)
            goto L_0x02c1
        L_0x02b5:
            com.sec.internal.interfaces.ims.config.IConfigModule r8 = r7.mConfigModule
            int r11 = r7.mPhoneId
            com.sec.internal.ims.config.params.ACSConfig r8 = r8.getAcsConfig(r11)
            r8.setForceAcs(r3)
        L_0x02c0:
            r3 = r4
        L_0x02c1:
            java.lang.String r8 = r7.LOG_TAG
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r2 = "onRegistrationError: retry at the 1st P-CSCF in "
            r11.append(r2)
            r11.append(r9)
            java.lang.String r2 = " milliseconds."
            r11.append(r2)
            java.lang.String r11 = r11.toString()
            android.util.Log.i(r8, r11)
            goto L_0x02de
        L_0x02dd:
            r3 = r4
        L_0x02de:
            int r8 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r8 <= 0) goto L_0x02e8
            if (r3 != 0) goto L_0x02ef
            r7.startRetryTimer(r9)
            goto L_0x02ef
        L_0x02e8:
            com.sec.internal.ims.core.RegistrationManagerHandler r8 = r7.mRegHandler
            int r9 = r7.mPhoneId
            r8.sendTryRegister(r9)
        L_0x02ef:
            r7.mConsecutiveForbiddenCounter = r4
            r7.mIsAkaChallengeTimeout = r4
            r7.mDnsQueryCount = r4
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorKor.onRegistrationError(com.sec.ims.util.SipError, long, boolean):void");
    }

    /* access modifiers changed from: package-private */
    public void onRegErrorforImsNotAvailable(SipError sipError, long j) {
        Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable:");
        boolean isRefreshReg = this.mTask.isRefreshReg();
        this.mFailureCounter++;
        this.mTask.setDeregiReason(41);
        if (getCallStatus() != 0 && isRefreshReg) {
            this.mCurPcscfIpIdx = 0;
            this.mFailureCounter = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            resetIPSecAllow();
            this.mHasPendingNotifyImsNotAvailable = true;
            Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable(Postpone notifyImsNotAvailable during call)");
        } else if (SipErrorBase.INTERVAL_TOO_BRIEF.equals(sipError)) {
            this.mCurPcscfIpIdx = 0;
            this.mFailureCounter = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            resetIPSecAllow();
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mRegMan.notifyImsNotAvailable(this.mTask, true);
            this.mRegMan.getEventLog().logAndAdd("onRegErrorforImsNotAvailable(423)");
        } else if (SipErrorBase.NOT_ACCEPTABLE.equals(sipError)) {
            Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable: 406 error. Ipsec not allow");
            this.mIPsecAllow = false;
            this.mFailureCounter = 0;
            if (j > 0) {
                startRetryTimer(j);
            } else {
                this.mRegHandler.sendTryRegister(this.mPhoneId);
            }
        } else {
            Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable: ETC error");
            if (!isRefreshReg) {
                this.mCurPcscfIpIdx++;
            }
            if (!isRefreshReg && this.mNumOfPcscfIp >= 2) {
                resetIPSecAllow();
            }
            if (this.mFailureCounter == 2) {
                this.mCurPcscfIpIdx = 0;
                this.mFailureCounter = 0;
                resetIPSecAllow();
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                this.mRegMan.notifyImsNotAvailable(this.mTask, true);
                this.mRegMan.getEventLog().logAndAdd("onRegErrorforImsNotAvailable(ETC)");
            } else if (!isRefreshReg) {
                if (j > 0) {
                    startRetryTimer(j);
                } else {
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                }
                Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable: ETC error. Initial Reg retry");
            } else {
                if (j == 0) {
                    j = 1000;
                }
                this.mTask.mKeepPdn = true;
                Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable: ETC error. Refresh Reg retry with same Call-ID");
                this.mRegHandler.sendUpdateRegistration(this.mTask.getProfile(), this.mPhoneId, j - DEFAULT_RETRY_AFTER_BUFFER_MS.longValue());
            }
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
        }
    }

    public void onRegistrationDone() {
        Log.i(this.LOG_TAG, "onRegistrationDone: clear mConsecutiveForbiddenCounter.");
        this.mFailureCounter = 0;
        this.mConsecutiveForbiddenCounter = 0;
        this.mIsAkaChallengeTimeout = false;
        this.mThrottledforImsNotAvailable = false;
        this.mRegiAt = 0;
        this.mDnsQueryCount = 0;
        stopPDNdisconnectTimer();
        stopRetryTimer();
        stopTimsTimer(RegistrationConstants.REASON_REGISTERED);
    }

    public void onSubscribeError(int i, SipError sipError) {
        String str = this.LOG_TAG;
        Log.i(str, "onSubscribeError: state " + this.mTask.getState() + " error " + sipError + ", event " + i);
        if (i == 0 && sipError.equals(new SipError(Id.REQUEST_IM_SENDMSG, "Subscribe 504 with init-regi"))) {
            Log.e(this.LOG_TAG, "onSubscribeError: SUBSCRIBE 504 with init regi.");
            this.mTask.setDeregiReason(44);
            this.mRegMan.deregister(this.mTask, true, true, "SUBSCRIBE 504 with init regi.");
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            stopPDNdisconnectTimer();
            stopRetryTimer();
        }
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent callEvent, SipError sipError, int i) {
        String str = this.LOG_TAG;
        Log.i(str, "onCallStatus: event=" + callEvent + " error=" + sipError);
        if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END && this.mTask.getState() == RegistrationConstants.RegisterTaskState.IDLE) {
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        }
        super.onCallStatus(callEvent, sipError, i);
    }

    public SipError onSipError(String str, SipError sipError) {
        Log.i(this.LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(sipError)) {
            removeCurrentPcscfAndInitialRegister(true);
            if (needImsNotAvailable()) {
                Log.i(this.LOG_TAG, "onSipError: 709 error. Initial Reg at the next P-CSCF");
                this.mFailureCounter++;
            }
        } else if (!SipErrorBase.FORBIDDEN.equals(sipError)) {
            ImsProfile profile = this.mTask.getProfile();
            if (SipErrorBase.NOT_ACCEPTABLE.equals(sipError) && (profile.hasService("mmtel") || profile.hasService("mmtel-video"))) {
                Log.e(this.LOG_TAG, "onSipError: 406 error. Ipsec not allow");
                this.mIPsecAllow = false;
                if (this.mTask.getUserAgent() != null) {
                    int deregTimeout = profile.getDeregTimeout(13);
                    Log.i(this.LOG_TAG, "try regsiter after " + deregTimeout);
                    this.mRegHandler.startRegistrationTimer(this.mTask, (long) deregTimeout);
                }
                this.mTask.setDeregiReason(21);
                this.mRegMan.deregister(this.mTask, true, true, "user triggered");
            } else if (!"initial_registration".equals(sipError.getReason())) {
                return super.onSipError(str, sipError);
            } else {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, true, true, sipError.getCode() + " Initial Registration");
            }
        } else if ("smsip".equals(str) && this.mTask.getMno() == Mno.LGU) {
            return sipError;
        } else {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, true, "403 Forbidden");
        }
        return sipError;
    }

    public void onAdsChanged(int i) {
        super.onAdsChanged(i);
        if (!this.mTask.isRcsOnly()) {
            String str = this.LOG_TAG;
            Log.i(str, "onAdsChanged: ActiveDataPhoneId[" + i + "] mTask:" + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ")");
            int oppositeSimSlot = SimUtil.getOppositeSimSlot(i);
            NetworkEvent networkEvent = this.mRegMan.getNetworkEvent(oppositeSimSlot);
            if (!networkEvent.isDataRoaming && SimUtil.getPhoneCount() > 1 && oppositeSimSlot != SimUtil.getActiveDataPhoneId() && SemSystemProperties.get("ro.boot.hardware", "").contains("qcom") && !networkEvent.csOutOfService && networkEvent.voiceNetwork == 3) {
                this.mTask.setDeregiReason(35);
                this.mTask.setReason("onAdsChanged: VoLTE disabled(qcom non DDS is cs only in 3G)");
                this.mRegMan.tryDeregisterInternal(this.mTask, false, false);
                if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                    this.mRegHandler.sendDisconnectPdnByVolteDisabled(this.mTask, 0);
                }
                Log.i(this.LOG_TAG, "onAdsChanged: VoLTE disabled(qcom non DDS is cs only in 3G)");
            }
        }
    }

    private long getActualWaitTime() {
        return (long) (((double) getWaitTime()) * ((Math.random() * 0.5d) + 0.5d));
    }

    /* access modifiers changed from: package-private */
    public long getActualWaitTimeForUnlimited404() {
        long pow = this.mRegBaseTimeMs * ((long) Math.pow(2.0d, (double) this.mFailureCounter));
        if (pow < 0) {
            return REG_RETRY_MAX_TIME_FOR_UNLIMITED_404_MS;
        }
        return Math.min(REG_RETRY_MAX_TIME_FOR_UNLIMITED_404_MS, pow);
    }

    /* access modifiers changed from: protected */
    public long getWaitTime() {
        long pow = this.mRegBaseTimeMs * ((long) Math.pow(2.0d, (double) this.mFailureCounter));
        if (pow < 0) {
            return this.mRegMaxTimeMs;
        }
        return Math.min(this.mRegMaxTimeMs, pow);
    }

    /* access modifiers changed from: protected */
    public void removeCurrentPcscfAndInitialRegister(boolean z) {
        Log.i(this.LOG_TAG, "removeCurrentPcscfAndInitialRegister()");
        this.mMoveToNextPcscfAfterTimerB = true;
        resetIPSecAllow();
        String moveToNextPcscfIp = moveToNextPcscfIp();
        updatePcscfIpList(this.mPcscfIpList, z);
        String str = this.LOG_TAG;
        Log.i(str, "removeCurrentPcscfAndInitialRegister(): nextPcscfIp " + moveToNextPcscfIp + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mPcscfIpList " + this.mPcscfIpList);
    }

    private void moveToNextPcscfAndInitialRegister() {
        Log.i(this.LOG_TAG, "moveToNextPcscfAndInitialRegister()");
        resetIPSecAllow();
        String moveToNextPcscfIp = moveToNextPcscfIp();
        if (this.mPcscfIpList == null) {
            Log.e(this.LOG_TAG, "moveToNextPcscfAndInitialRegister: null P-CSCF list!");
            return;
        }
        boolean z = this.mNumOfPcscfIp > 0;
        this.mIsValid = z;
        if (this.mCurPcscfIpIdx >= 0 && z) {
            Log.i(this.LOG_TAG, "moveToNextPcscfAndInitialRegister: forceInitialRegi");
            this.mFailureCounter = 0;
            this.mCurImpu = 0;
            this.mRegiAt = 0;
            if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                this.mTask.setDeregiReason(8);
                this.mRegMan.deregister(this.mTask, true, this.mIsValid, "pcscf updated");
            }
        }
        String str = this.LOG_TAG;
        Log.i(str, "moveToNextPcscfAndInitialRegister(): nextPcscfIp " + moveToNextPcscfIp + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mPcscfIpList " + this.mPcscfIpList);
    }

    /* access modifiers changed from: protected */
    public String moveToNextPcscfIp() {
        String str = this.LOG_TAG;
        Log.i(str, "moveToNextPcscfIp: mCurPcscfIpIdx = " + this.mCurPcscfIpIdx + " mPcscfIpList = " + this.mPcscfIpList);
        List<String> list = this.mPcscfIpList;
        if (list == null || list.isEmpty()) {
            Log.e(this.LOG_TAG, "moveToNextPcscfIp: empty P-CSCF list.");
            return "";
        }
        int size = (this.mCurPcscfIpIdx + 1) % this.mPcscfIpList.size();
        this.mCurPcscfIpIdx = size;
        return this.mPcscfIpList.get(size);
    }

    public boolean isThrottled() {
        return super.isThrottled();
    }

    /* JADX WARNING: Removed duplicated region for block: B:56:0x01b4  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0227  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0242  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x01ea A[EDGE_INSN: B:95:0x01ea->B:63:0x01ea ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkProfileUpdateFromDM(boolean r11) {
        /*
            r10 = this;
            com.sec.internal.ims.core.RegisterTask r0 = r10.mTask
            com.sec.ims.settings.ImsProfile r0 = r0.getProfile()
            java.lang.String r1 = "mmtel"
            boolean r0 = r0.hasService(r1)
            if (r0 != 0) goto L_0x001d
            com.sec.internal.ims.core.RegisterTask r0 = r10.mTask
            com.sec.ims.settings.ImsProfile r0 = r0.getProfile()
            java.lang.String r1 = "mmtel-video"
            boolean r0 = r0.hasService(r1)
            if (r0 != 0) goto L_0x001d
            return
        L_0x001d:
            java.lang.String r0 = r10.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "checkProfileUpdateFromDM: force="
            r1.append(r2)
            r1.append(r11)
            java.lang.String r2 = " pcscf_pref="
            r1.append(r2)
            com.sec.internal.ims.core.RegisterTask r2 = r10.mTask
            com.sec.ims.settings.ImsProfile r2 = r2.getProfile()
            int r2 = r2.getPcscfPreference()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            com.sec.internal.constants.Mno r0 = r10.mMno
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.KT
            java.lang.String r2 = ""
            java.lang.String r3 = "ril.simtype"
            r4 = 1
            r5 = 0
            if (r0 != r1) goto L_0x006b
            com.sec.internal.helper.os.ITelephonyManager r0 = r10.mTelephonyManager
            int r1 = r10.mPhoneId
            java.lang.String r0 = r0.semGetTelephonyProperty(r1, r3, r2)
            java.lang.String r1 = "20"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x006b
            java.lang.String r0 = r10.LOG_TAG
            java.lang.String r1 = "checkProfileUpdateFromDM : KT_unreg SIM"
            android.util.Log.i(r0, r1)
            r0 = r4
            goto L_0x006c
        L_0x006b:
            r0 = r5
        L_0x006c:
            com.sec.internal.constants.Mno r1 = r10.mMno
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.LGU
            if (r1 != r6) goto L_0x008b
            com.sec.internal.helper.os.ITelephonyManager r1 = r10.mTelephonyManager
            int r6 = r10.mPhoneId
            java.lang.String r1 = r1.semGetTelephonyProperty(r6, r3, r2)
            java.lang.String r2 = "18"
            boolean r1 = r2.equals(r1)
            if (r1 == 0) goto L_0x008b
            java.lang.String r1 = r10.LOG_TAG
            java.lang.String r2 = "checkProfileUpdateFromDM : LGT_unreg SIM"
            android.util.Log.i(r1, r2)
            r1 = r4
            goto L_0x008c
        L_0x008b:
            r1 = r5
        L_0x008c:
            android.content.Context r2 = r10.mContext
            com.sec.internal.ims.core.RegisterTask r3 = r10.mTask
            com.sec.ims.settings.ImsProfile r3 = r3.getProfile()
            int r6 = r10.mPhoneId
            com.sec.ims.settings.ImsProfile r2 = com.sec.internal.ims.settings.DmProfileLoader.getProfile(r2, r3, r6)
            int r3 = r2.getRegRetryBaseTime()
            long r6 = (long) r3
            r8 = 1000(0x3e8, double:4.94E-321)
            long r6 = r6 * r8
            r10.mRegBaseTimeMs = r6
            int r3 = r2.getRegRetryMaxTime()
            long r6 = (long) r3
            long r6 = r6 * r8
            r10.mRegMaxTimeMs = r6
            boolean r3 = r10.isNeedForcibleSmsOverImsOn()
            if (r3 == 0) goto L_0x00d3
            android.content.Context r3 = r10.mContext
            int r6 = r10.mPhoneId
            com.sec.ims.settings.NvConfiguration.setSmsIpNetworkIndi(r3, r4, r6)
            r2.setSupportSmsOverIms(r4)
            com.sec.internal.ims.core.RegisterTask r3 = r10.mTask
            r3.setProfile(r2)
            java.lang.String r3 = r10.LOG_TAG
            java.lang.String r6 = "checkProfileUpdateFromDM: SmsOverIms is false. set it as true forcibly"
            android.util.Log.e(r3, r6)
            com.sec.internal.ims.core.RegistrationManagerInternal r3 = r10.mRegMan
            com.sec.internal.helper.SimpleEventLog r3 = r3.getEventLog()
            java.lang.String r6 = "checkProfileUpdateFromDM : SmsOverIms is false. set it as true forcibly"
            r3.logAndAdd(r6)
        L_0x00d3:
            com.sec.internal.ims.core.RegisterTask r3 = r10.mTask
            boolean r3 = r3.isNeedOmadmConfig()
            if (r3 != 0) goto L_0x00df
            if (r0 != 0) goto L_0x00df
            if (r1 == 0) goto L_0x02ce
        L_0x00df:
            com.sec.internal.ims.core.RegistrationManagerInternal r3 = r10.mRegMan
            int r6 = r10.mPhoneId
            boolean r3 = r3.hasOmaDmFinished(r6)
            if (r3 != 0) goto L_0x013b
            if (r0 != 0) goto L_0x013b
            if (r1 == 0) goto L_0x00ee
            goto L_0x013b
        L_0x00ee:
            boolean r11 = com.sec.internal.helper.OmcCode.isKOROmcCode()
            if (r11 == 0) goto L_0x0122
            com.sec.internal.ims.core.RegistrationManagerInternal r11 = r10.mRegMan
            int r0 = r10.mPhoneId
            com.sec.internal.constants.ims.os.NetworkEvent r11 = r11.getNetworkEvent(r0)
            boolean r11 = r11.isDataRoaming
            if (r11 == 0) goto L_0x0119
            java.lang.String r11 = r10.LOG_TAG
            java.lang.String r0 = "Roaming, so use PCO"
            android.util.Log.i(r11, r0)
            java.util.ArrayList r11 = new java.util.ArrayList
            r11.<init>()
            r2.setPcscfPreference(r5)
            r2.setPcscfList(r11)
            com.sec.internal.ims.core.RegisterTask r10 = r10.mTask
            r10.setProfile(r2)
            goto L_0x02ce
        L_0x0119:
            java.lang.String r10 = r10.LOG_TAG
            java.lang.String r11 = "not Roaming"
            android.util.Log.i(r10, r11)
            goto L_0x02ce
        L_0x0122:
            java.lang.String r11 = r10.LOG_TAG
            java.lang.String r0 = "oversea device and KOR SIM, so use PCO"
            android.util.Log.i(r11, r0)
            java.util.ArrayList r11 = new java.util.ArrayList
            r11.<init>()
            r2.setPcscfPreference(r5)
            r2.setPcscfList(r11)
            com.sec.internal.ims.core.RegisterTask r10 = r10.mTask
            r10.setProfile(r2)
            goto L_0x02ce
        L_0x013b:
            java.lang.String r0 = r10.LOG_TAG
            java.lang.String r3 = "checkProfileUpdateFromDM()"
            android.util.Log.i(r0, r3)
            com.sec.internal.constants.Mno r0 = r10.mMno
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.LGU
            if (r0 != r3) goto L_0x0161
            if (r1 == 0) goto L_0x0161
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r2.setPcscfPreference(r5)
            r2.setPcscfList(r0)
            com.sec.internal.ims.core.RegisterTask r0 = r10.mTask
            r0.setProfile(r2)
            java.lang.String r0 = r10.LOG_TAG
            java.lang.String r1 = "checkProfileUpdateFromDM: LGTUnregSIM PCO"
            android.util.Log.i(r0, r1)
        L_0x0161:
            boolean r0 = r10.mDmUpdatedFlag
            if (r0 == 0) goto L_0x016f
            if (r11 != 0) goto L_0x016f
            java.lang.String r10 = r10.LOG_TAG
            java.lang.String r11 = "mDmUpdatedFlag true"
            android.util.Log.i(r10, r11)
            return
        L_0x016f:
            com.sec.internal.constants.Mno r11 = r10.mMno
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.KT
            if (r11 != r0) goto L_0x019e
            int r11 = r2.getPcscfPreference()
            if (r11 != 0) goto L_0x019e
            java.util.ArrayList r11 = new java.util.ArrayList
            r11.<init>()
            r2.setPcscfPreference(r5)
            r2.setPcscfList(r11)
            java.lang.String r0 = r10.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "[KT 5G] P-CSCF discovery PCO>DM>DEFAULT pcscf: "
            r1.append(r3)
            r1.append(r11)
            java.lang.String r11 = r1.toString()
            android.util.Log.i(r0, r11)
            goto L_0x024f
        L_0x019e:
            java.util.List r11 = r2.getLboPcscfAddressList()
            if (r11 == 0) goto L_0x01ea
            int r0 = r11.size()
            if (r0 <= 0) goto L_0x01ea
            java.util.Iterator r0 = r11.iterator()
        L_0x01ae:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x01ea
            java.lang.Object r1 = r0.next()
            java.lang.String r1 = (java.lang.String) r1
            java.util.regex.Pattern r3 = android.util.Patterns.DOMAIN_NAME
            java.util.regex.Matcher r3 = r3.matcher(r1)
            boolean r3 = r3.matches()
            if (r3 != 0) goto L_0x01d2
            boolean r3 = com.sec.internal.helper.NetworkUtil.isIPv4Address(r1)
            if (r3 != 0) goto L_0x01d2
            boolean r3 = com.sec.internal.helper.NetworkUtil.isIPv6Address(r1)
            if (r3 == 0) goto L_0x01ae
        L_0x01d2:
            java.lang.String r0 = r10.LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r6 = "DM pcscf is valid : "
            r3.append(r6)
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            android.util.Log.i(r0, r1)
            r0 = r4
            goto L_0x01eb
        L_0x01ea:
            r0 = r5
        L_0x01eb:
            if (r11 == 0) goto L_0x021c
            int r1 = r11.size()
            if (r1 <= 0) goto L_0x021c
            if (r0 == 0) goto L_0x021c
            int r0 = r2.getLboPcscfPort()
            r1 = 5
            r2.setPcscfPreference(r1)
            r2.setPcscfList(r11)
            if (r0 <= 0) goto L_0x024f
            r2.setSipPort(r0)
            java.lang.String r11 = r10.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "DM updated lbo pcscf port found : "
            r1.append(r3)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            android.util.Log.i(r11, r0)
            goto L_0x024f
        L_0x021c:
            java.util.ArrayList r11 = new java.util.ArrayList
            r11.<init>()
            com.sec.internal.constants.Mno r0 = r10.mMno
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.KT
            if (r0 != r1) goto L_0x0242
            java.util.List r11 = r10.getPcscfFromFile(r0)
            java.lang.String r0 = r10.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "DM pcscf is empty. [KT 5G] P-CSCF discovery PCO>DM>DEFAULT pcscfList: "
            r1.append(r3)
            r1.append(r11)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            goto L_0x024c
        L_0x0242:
            java.lang.String r0 = r10.LOG_TAG
            java.lang.String r1 = "DM pcscf is empty"
            android.util.Log.i(r0, r1)
            r2.setPcscfPreference(r5)
        L_0x024c:
            r2.setPcscfList(r11)
        L_0x024f:
            boolean r11 = r2.isSupportSmsOverIms()
            if (r11 == 0) goto L_0x029d
            java.lang.String r11 = r10.LOG_TAG
            java.lang.String r0 = "SMS over IMS is enabled by OMADM"
            android.util.Log.i(r11, r0)
            java.util.Set r11 = r2.getNetworkSet()
            java.util.Iterator r11 = r11.iterator()
        L_0x0264:
            boolean r0 = r11.hasNext()
            if (r0 == 0) goto L_0x029d
            java.lang.Object r0 = r11.next()
            java.lang.Integer r0 = (java.lang.Integer) r0
            java.util.Set r1 = r2.getServiceSet(r0)
            java.util.Iterator r3 = r1.iterator()
        L_0x0278:
            boolean r6 = r3.hasNext()
            java.lang.String r7 = "smsip"
            if (r6 == 0) goto L_0x028f
            java.lang.Object r6 = r3.next()
            java.lang.String r6 = (java.lang.String) r6
            boolean r6 = r7.equals(r6)
            if (r6 == 0) goto L_0x0278
            r3 = r4
            goto L_0x0290
        L_0x028f:
            r3 = r5
        L_0x0290:
            if (r3 != 0) goto L_0x0264
            r1.add(r7)
            int r0 = r0.intValue()
            r2.setServiceSet(r0, r1)
            goto L_0x0264
        L_0x029d:
            int r11 = r2.getDmPollingPeriod()
            if (r11 <= 0) goto L_0x02c4
            java.lang.String r11 = r10.LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "DmPollingPeriod : "
            r0.append(r1)
            int r1 = r2.getDmPollingPeriod()
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            android.util.Log.i(r11, r0)
            int r11 = r2.getDmPollingPeriod()
            r10.startDmPollingTimer(r11)
        L_0x02c4:
            r10.checkDMConfigChange(r2)
            com.sec.internal.ims.core.RegisterTask r11 = r10.mTask
            r11.setProfile(r2)
            r10.mDmUpdatedFlag = r4
        L_0x02ce:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorKor.checkProfileUpdateFromDM(boolean):void");
    }

    public void updatePcscfIpList(List<String> list) {
        boolean z;
        if (list == null) {
            Log.e(this.LOG_TAG, "updatePcscfIpList: null P-CSCF list!");
            return;
        }
        ArrayList arrayList = new ArrayList();
        LinkPropertiesWrapper linkProperties = this.mPdnController.getLinkProperties(this.mTask);
        if (linkProperties == null) {
            Log.e(this.LOG_TAG, "updatePcscfIpList: null LinkProperties");
            this.mIsValid = false;
            return;
        }
        Iterator<String> it = list.iterator();
        while (true) {
            if (it.hasNext()) {
                if (NetworkUtil.isIPv4Address(it.next())) {
                    z = true;
                    break;
                }
            } else {
                z = false;
                break;
            }
        }
        int i = (linkProperties.hasGlobalIPv6Address() || linkProperties.hasIPv6DefaultRoute()) ? 2 : 1;
        int ipVer = this.mTask.getProfile().getIpVer();
        if (this.mTask.isRcsOnly() && z) {
            Log.i(this.LOG_TAG, "updatePcscfIpList: value ipv4 addr above ipv6 addr for RCS");
            i = linkProperties.hasIPv4Address() ? 1 : 2;
        }
        Log.i(this.LOG_TAG, "updatePcscfIpList: localIpType=" + i + ", profileIpType=" + ipVer);
        for (int i2 = 0; i2 < list.size(); i2++) {
            if (ipVer != 1) {
                if (ipVer != 2) {
                    if (ipVer == 3) {
                        if (i == 1) {
                            if (NetworkUtil.isIPv4Address(list.get(i2))) {
                                arrayList.add(list.get(i2));
                            }
                        } else if (NetworkUtil.isIPv6Address(list.get(i2))) {
                            arrayList.add(list.get(i2));
                        }
                    }
                } else if (NetworkUtil.isIPv6Address(list.get(i2))) {
                    arrayList.add(list.get(i2));
                }
            } else if (NetworkUtil.isIPv4Address(list.get(i2))) {
                arrayList.add(list.get(i2));
            }
        }
        Log.i(this.LOG_TAG, "updatePcscfIpList tmpPcscfIpList = " + arrayList);
        super.updatePcscfIpList(arrayList);
    }

    public void checkAcsPcscfListChange() {
        if (this.mTask.isRcsOnly()) {
            ArrayList arrayList = new ArrayList();
            String readStringParam = RcsConfigurationHelper.readStringParam(this.mContext, "address", (String) null);
            if (readStringParam == null) {
                Log.i(this.LOG_TAG, "checkAcsPcscfIpListChange : lboPcscfAddress is null");
                return;
            }
            arrayList.add(readStringParam);
            String str = this.LOG_TAG;
            Log.i(str, "checkAcsPcscfIpListChange : previous pcscf = " + this.mRcsPcscfList + ", new pcscf = " + arrayList);
            if (!arrayList.equals(this.mRcsPcscfList)) {
                resetPcscfList();
                ArrayList arrayList2 = new ArrayList();
                this.mRcsPcscfList = arrayList2;
                arrayList2.add(readStringParam);
                if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    this.mTask.setDeregiReason(8);
                    this.mRegMan.deregister(this.mTask, true, true, "checkAcsPcscfIpListChange : pcscf updated");
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                }
                Log.i(this.LOG_TAG, "checkAcsPcscfIpListChange : resetPcscfList");
            }
        }
    }

    public void notifyReattachToRil() {
        Log.i(this.LOG_TAG, "notifyReattachToRil");
        sendRawRequestToTelephony(this.mContext, buildReattachNotiOemHookCmd());
    }

    private void startDmPollingTimer(int i) {
        if (this.mDmPollingTimer != null) {
            stopPollingTimer();
        }
        String str = this.LOG_TAG;
        Log.i(str, "startDmPollingTimer: Timer " + i + " sec");
        this.mDmPollingTimer = this.mRegHandler.startDmConfigTimer(this.mTask, ((long) i) * 1000);
    }

    /* access modifiers changed from: package-private */
    public void stopPollingTimer() {
        if (this.mDmPollingTimer != null) {
            Log.i(this.LOG_TAG, "stopPollingTimer");
            this.mRegHandler.stopTimer(this.mDmPollingTimer);
            this.mDmPollingTimer = null;
        }
    }

    public byte[] buildReattachNotiOemHookCmd() {
        return new byte[]{9, 11, 0, 4};
    }

    public Set<String> filterService(Set<String> set, int i) {
        HashSet hashSet;
        if (isImsDisabled()) {
            return new HashSet();
        }
        Set hashSet2 = new HashSet();
        if (set == null) {
            hashSet = new HashSet();
        }
        if (!this.mTask.isRcsOnly()) {
            NetworkEvent networkEvent = this.mRegMan.getNetworkEvent(this.mPhoneId);
            if (networkEvent.isDataRoaming) {
                if (!NetworkUtil.is3gppPsVoiceNetwork(networkEvent.network) || networkEvent.voiceOverPs != VoPsIndication.SUPPORTED) {
                    Log.i(this.LOG_TAG, "filterService: NW is not LTE/NR or VoPS is not supported in roaming");
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                    return new HashSet();
                }
            } else if (SimUtil.getActiveSubInfoCount() > 1 && this.mPhoneId != SimUtil.getActiveDataPhoneId() && SemSystemProperties.get("ro.boot.hardware", "").contains("qcom") && !networkEvent.csOutOfService && networkEvent.voiceNetwork == 3) {
                Log.i(this.LOG_TAG, "filterService: QC non DDS is CS only in 3G");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NON_DDS_CS_ONLY_IN_3G.getCode());
                return new HashSet();
            }
            boolean z = false;
            if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1) {
                hashSet2.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
                if (!hashSet2.contains("mmtel")) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
                }
            }
            hashSet2 = applyMmtelUserSettings(hashSet2, i);
            ImsProfile profile = DmProfileLoader.getProfile(this.mContext, this.mTask.getProfile(), this.mPhoneId);
            if (networkEvent.isDataRoaming && this.mMno == Mno.KT && OmcCode.isKTTOmcCode()) {
                z = true;
            }
            Log.i(this.LOG_TAG, "filterService: isKTInRoaming: " + z);
            if (!profile.isSupportSmsOverIms() && !z) {
                removeService(hashSet2, "smsip", "isSupportSmsOverIms disabled");
            }
        }
        if (this.mTask.isRcsOnly()) {
            if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
                hashSet2.addAll(servicesByImsSwitch(ImsProfile.getRcsServiceList()));
            }
            if (!this.mTask.getProfile().getSupportRcsAcrossSalesCode() || SimUtil.getActiveSubInfoCount() <= 1 || RcsUtils.DualRcs.isRegAllowed(this.mContext, this.mPhoneId)) {
                UserManager userManager = (UserManager) this.mContext.getSystemService("user");
                if (userManager != null && UserManager.supportsMultipleUsers()) {
                    int currentUser = Extensions.ActivityManager.getCurrentUser();
                    if (userManager.hasUserRestriction("no_sms", UserHandle.of(currentUser))) {
                        Log.i(this.LOG_TAG, "filterService: RCS is not supported for MUM with DISALLOW_SMS (" + currentUser + "|" + Extensions.UserHandle.myUserId() + ")");
                        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.RCS_MUM_DISALLOW_SMS.getCode());
                        return new HashSet();
                    }
                }
            } else {
                Log.i(this.LOG_TAG, "filterService: RCS is not supported for non DDS");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.RCS_NOT_DDS.getCode());
                return new HashSet();
            }
        }
        if (!hashSet.isEmpty()) {
            hashSet.retainAll(hashSet2);
        }
        Log.i(this.LOG_TAG, "filterService : filteredServices = " + hashSet);
        return hashSet;
    }

    /* access modifiers changed from: package-private */
    public boolean checkOtaStatus() {
        if (this.mMno != Mno.SKT || !CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ril.domesticOtaStart"))) {
            return true;
        }
        Log.i(this.LOG_TAG, "isReadyToRegister : OTA is working, don't try register");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_OTA.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkRegiStatus() {
        RegisterTask registerTask = this.mTask;
        if (registerTask.mIsUpdateRegistering || registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
            return false;
        }
        if (this.mTask.getState() != RegistrationConstants.RegisterTaskState.CONNECTED || this.mPDNdisconnectTimeout == null || this.mTask.isRcsOnly()) {
            return true;
        }
        Log.i(this.LOG_TAG, "isReadyToRegister: mPDNdisconnectTimeout is not null");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int i) {
        if (this.mTask.isRcsOnly()) {
            boolean z = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == 1;
            if (RcsConfigurationHelper.readIntParam(this.mContext, "version", 0).intValue() <= 0 && !z) {
                Log.i(this.LOG_TAG, "isReadyToRegister: User don't try RCS service yet");
                return false;
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
                Log.i(this.LOG_TAG, "isReadyToRegister: Default MSG app isn't used for RCS");
                return false;
            } else {
                ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
                if (simManagerFromSimSlot != null) {
                    String str = "IMSI_" + simManagerFromSimSlot.getImsi();
                    if (TextUtils.isEmpty(simManagerFromSimSlot.getMsisdn()) && TextUtils.isEmpty(ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, IConfigModule.MSISDN_FROM_PAU, str, ""))) {
                        Log.i(this.LOG_TAG, "isReadyToRegister: MSISDN is null, try to RCS ACS after registered VoLTE");
                        IMSLog.c(LogClass.KOR_PENDING_RCS, this.mPhoneId + "PENDING RCS");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            return true;
        }
        int telephonyCallStatus = this.mRegMan.getTelephonyCallStatus(this.mTask.getPhoneId());
        String str = this.LOG_TAG;
        Log.i(str, "isReadyToRegister : getTelephonyCallStatus is " + telephonyCallStatus);
        if (telephonyCallStatus == 0 || telephonyCallStatus == 2) {
            return true;
        }
        return false;
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkOtaStatus() && i != 0 && checkRegiStatus() && checkRcsEvent(i) && checkCallStatus());
    }

    public void releaseThrottle(int i) {
        if ((this.mIsAkaChallengeTimeout && (i == 1 || i == 5)) || ((this.mThrottledforImsNotAvailable && (i == 9 || i == 1)) || i == 4 || i == 10)) {
            this.mIsPermanentStopped = false;
            this.mThrottledforImsNotAvailable = false;
            resetIPSecAllow();
            this.mCurImpu = 0;
        } else if (i == 1) {
            resetRetry();
            resetAllRetryFlow();
        }
        if (!this.mIsPermanentStopped) {
            SimpleEventLog eventLog = this.mRegMan.getEventLog();
            eventLog.logAndAdd("releaseThrottle: case by " + i);
        }
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        super.onPdnRequestFailed(pdnFailReason, i);
        if (isMatchedPdnFailReason(pdnFailReason)) {
            SimpleEventLog eventLog = this.mRegMan.getEventLog();
            eventLog.logAndAdd(pdnFailReason + ": Release throttle.");
            releaseThrottle(10);
            this.mIsReadyToGetReattach = false;
        }
    }

    public void startTimsTimer(String str) {
        String str2 = this.LOG_TAG;
        Log.i(str2, "startTimsTimer : " + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ") Pdn(" + this.mTask.getPdnType() + "," + this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask) + ")");
        if (!RegistrationUtils.isCmcProfile(this.mTask.getProfile())) {
            if (!this.mTask.isRcsOnly()) {
                if (!needImsNotAvailable()) {
                    int i = this.mRequestPdnTimeoutCount;
                    if (i < 5) {
                        this.mRequestPdnTimeoutCount = i + 1;
                        startTimsEstablishTimer(this.mTask, 180000, str);
                    }
                } else if (!SlotBasedConfig.getInstance(this.mPhoneId).isNotifiedImsNotAvailable()) {
                    startTimsEstablishTimer(this.mTask, 60000, str);
                }
            } else if (isMobilePreferredForRcs()) {
                PdnController pdnController = this.mPdnController;
                int translateNetworkBearer = pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer());
                if (this.mTask.getPdnType() != 0 || !NetworkUtil.isMobileDataOn(this.mContext) || !NetworkUtil.isMobileDataPressed(this.mContext) || ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON || this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).outOfService || translateNetworkBearer != 1 || this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
                    int i2 = this.mRequestPdnTimeoutCount;
                    if (i2 < 5) {
                        this.mRequestPdnTimeoutCount = i2 + 1;
                        startTimsEstablishTimer(this.mTask, 180000, str);
                        return;
                    }
                    return;
                }
                startTimsEstablishTimer(this.mTask, 30000, str);
            }
        }
    }

    public void stopTimsTimer(String str) {
        String str2 = this.LOG_TAG;
        Log.i(str2, "stopTimsTimer : " + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ") Pdn(" + this.mTask.getPdnType() + "," + this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask) + ")");
        if (!RegistrationUtils.isCmcProfile(this.mTask.getProfile())) {
            if (this.mTask.isRcsOnly()) {
                this.mHasNetworkFailure = false;
            }
            stopTimsEstablishTimer(this.mTask, str);
        }
    }

    public void onTimsTimerExpired() {
        String str;
        Log.i(this.LOG_TAG, "onTimsTimerExpired : " + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ") Pdn(" + this.mTask.getPdnType() + "," + this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask) + ")");
        boolean needImsNotAvailable = needImsNotAvailable();
        if (!RegistrationUtils.isCmcProfile(this.mTask.getProfile())) {
            if (!this.mTask.isRcsOnly()) {
                SimpleEventLog eventLog = this.mRegMan.getEventLog();
                StringBuilder sb = new StringBuilder();
                sb.append("onTimsTimerExpired. ");
                sb.append(needImsNotAvailable);
                if (needImsNotAvailable) {
                    str = "";
                } else {
                    str = ",Count is " + this.mRequestPdnTimeoutCount;
                }
                sb.append(str);
                eventLog.logAndAdd(sb.toString());
                if (needImsNotAvailable) {
                    super.onTimsTimerExpired();
                    return;
                }
                stopTimsEstablishTimer(this.mTask, RegistrationConstants.REASON_TIMS_EXPIRED);
                deregisterIfConnecting(13);
            } else if (isMobilePreferredForRcs()) {
                this.mRegMan.getEventLog().logAndAdd("onTimsTimerExpired for RCS. " + "Count is " + this.mRequestPdnTimeoutCount);
                stopTimsEstablishTimer(this.mTask, RegistrationConstants.REASON_TIMS_EXPIRED);
                this.mHasNetworkFailure = true;
                deregisterIfConnecting(13);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        Mno mno = this.mMno;
        if (mno == Mno.LGU || (mno == Mno.KT && this.mIsShipBuild)) {
            Log.i(this.LOG_TAG, "getVoiceTechType : LGU device or KT ship device have to enable VOLTE always, regardless of DB");
            forceTurnOnVoLte();
            return 0;
        }
        if (mno == Mno.SKT && this.mIsShipBuild) {
            String str = (this.mPhoneId == ImsConstants.Phone.SLOT_1 ? ImsConstants.SystemSettings.VOLTE_SLOT1 : ImsConstants.SystemSettings.VOLTE_SLOT2).getPackage();
            if (!Arrays.asList(this.allowedPackages).contains(str)) {
                String str2 = this.LOG_TAG;
                Log.i(str2, "getVoiceTechType : modifier pkg:" + str);
                forceTurnOnVoLte();
                return 0;
            }
        } else if (ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, this.mPhoneId) == -1) {
            Log.i(this.LOG_TAG, "getVoiceTechType : voicecall_type is corrupted. recover it");
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, this.mPhoneId);
        }
        return super.getVoiceTechType();
    }

    public boolean isIPSecAllow() {
        if (!OmcCode.isKOROmcCode() && this.mMno == Mno.SKT) {
            Log.i(this.LOG_TAG, "isIPSecAllow : oversea device and SKT sim. do not use IPSec");
            return false;
        } else if (!this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming) {
            return this.mIPsecAllow;
        } else {
            Mno mno = this.mMno;
            if (mno == Mno.SKT || mno == Mno.LGU) {
                return false;
            }
            return this.mIPsecAllow;
        }
    }

    public String toString() {
        return "RegistrationGovernorKor [mRegBaseTimeMs=" + this.mRegBaseTimeMs + ", mDmUpdatedFlag=" + this.mDmUpdatedFlag + ", mConsecutiveForbiddenCounter=" + this.mConsecutiveForbiddenCounter + ", mHasPendingInitRegistrationByDMConfigChange=" + this.mHasPendingInitRegistrationByDMConfigChange + ", mIsAkaChallengeTimeout=" + this.mIsAkaChallengeTimeout + ", mHasPendingNotifyImsNotAvailable=" + this.mHasPendingNotifyImsNotAvailable + ", pcscf_pref " + this.mTask.getProfile().getPcscfPreference() + "] " + super.toString();
    }

    private boolean isVolteEnabled() {
        return isVolteSettingEnabled() && getVolteServiceStatus() && isLTEDataModeEnabled();
    }

    /* access modifiers changed from: package-private */
    public void setOldVolteServiceStatus(boolean z) {
        String str = this.LOG_TAG;
        Log.i(str, "setOldVolteServiceStatus : " + z);
        this.mVolteServiceStatus = z;
    }

    private boolean getVolteServiceStatus() {
        boolean isVolteServiceStatus = DmProfileLoader.getProfile(this.mContext, this.mTask.getProfile(), this.mPhoneId).isVolteServiceStatus();
        String str = this.LOG_TAG;
        Log.i(str, "getVolteServiceStatus : " + isVolteServiceStatus);
        return isVolteServiceStatus;
    }

    public boolean needImsNotAvailable() {
        boolean z = this.mMno == Mno.LGU && this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming;
        boolean supportImsNotAvailable = this.mTask.getProfile().getSupportImsNotAvailable();
        boolean z2 = !this.mTask.isRcsOnly() && !RegistrationUtils.isCmcProfile(this.mTask.getProfile());
        String str = this.LOG_TAG;
        Log.i(str, "isLGUInVoLTERoaming : " + z + " isImsNotAvailableSupported : " + supportImsNotAvailable + " isVoLTEOnly : " + z2);
        if (!z || !supportImsNotAvailable || !z2) {
            return false;
        }
        return true;
    }

    public boolean hasNetworkFailure() {
        return this.mHasNetworkFailure;
    }

    public boolean isMobilePreferredForRcs() {
        return this.mTask.isRcsOnly() && !this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).isDataRoaming;
    }

    public void onVolteSettingChanged() {
        Log.i(this.LOG_TAG, "onVolteSettingChanged ");
        if (!this.mTask.isRcsOnly()) {
            checkVoLTEStatusChanged(VoltePreferenceChangedReason.VOLTE_SETTING);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isVolteSettingEnabled() {
        return getVoiceTechType() == 0;
    }

    public void onLteDataNetworkModeSettingChanged(boolean z) {
        String str = this.LOG_TAG;
        Log.i(str, "onLteDataNetworkModeSettingChanged : " + z);
        if (!this.mTask.isRcsOnly()) {
            if (z || SimUtil.getActiveSubInfoCount() <= 1 || this.mPhoneId == SimUtil.getActiveDataPhoneId() || !SemSystemProperties.get("ro.boot.hardware", "").contains("qcom")) {
                checkVoLTEStatusChanged(VoltePreferenceChangedReason.LTE_MODE);
                return;
            }
            this.mTask.setDeregiReason(31);
            this.mTask.setReason("onLteDataNetworkModeSettingChanged: VoLTE disabled(qcom non DDS is cs only in 3G)");
            this.mRegMan.tryDeregisterInternal(this.mTask, false, false);
            Log.i(this.LOG_TAG, "onLteDataNetworkModeSettingChanged: VoLTE disabled(qcom non DDS is cs only in 3G)");
        }
    }

    public void onPreferredNetworkModeChanged() {
        String str = this.LOG_TAG;
        Log.i(str, "onPreferredNetworkModeChanged mLteModeOn: " + this.mLteModeOn);
        this.mRegHandler.notifyLteDataNetworkModeSettingChanged(this.mLteModeOn, this.mPhoneId);
    }

    public void registerAllowedNetworkTypesListener() {
        Mno mno;
        this.mSubId = SimUtil.getSubId(this.mPhoneId);
        unregisterAllowedNetworkTypesListener();
        if (OmcCode.isKOROmcCode() && (mno = this.mMno) != Mno.KT && mno != Mno.LGU) {
            if (!SubscriptionManager.isValidSubscriptionId(this.mSubId)) {
                Log.i(this.LOG_TAG, "registerAllowedNetworkTypesListener : not ValidSubscriptionId");
                return;
            }
            TelephonyManager createForSubscriptionId = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).createForSubscriptionId(this.mSubId);
            if (createForSubscriptionId == null) {
                Log.i(this.LOG_TAG, "registerAllowedNetworkTypesListener : TelephonyManager null");
                return;
            }
            if (this.mAllowedNetworkTypesListener == null) {
                Log.i(this.LOG_TAG, "registerAllowedNetworkTypesListener : AllowedNetworkTypesListener null");
                this.mAllowedNetworkTypesListener = new AllowedNetworkTypesListener();
            }
            createForSubscriptionId.registerTelephonyCallback(this.mContext.getMainExecutor(), this.mAllowedNetworkTypesListener);
            this.mAllowedNetworkType = createForSubscriptionId.getAllowedNetworkTypesBitmask();
            String str = this.LOG_TAG;
            Log.i(str, "registerAllowedNetworkTypesListener : " + this.mAllowedNetworkType + " " + this.mAllowedNetworkTypesListener);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterAllowedNetworkTypesListener() {
        if (this.mAllowedNetworkTypesListener != null) {
            TelephonyManager createForSubscriptionId = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).createForSubscriptionId(this.mSubId);
            if (createForSubscriptionId == null) {
                Log.i(this.LOG_TAG, "unregisterAllowedNetworkTypesListener : TelephonyManager null");
                return;
            }
            createForSubscriptionId.unregisterTelephonyCallback(this.mAllowedNetworkTypesListener);
            this.mAllowedNetworkTypesListener = null;
        }
    }

    public class AllowedNetworkTypesListener extends TelephonyCallback implements TelephonyCallback.AllowedNetworkTypesListener {
        public AllowedNetworkTypesListener() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:2:0x0006, code lost:
            r4 = r3.this$0;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onAllowedNetworkTypesChanged(int r4, long r5) {
            /*
                r3 = this;
                boolean r4 = com.sec.internal.helper.OmcCode.isKOROmcCode()
                if (r4 == 0) goto L_0x004d
                com.sec.internal.ims.core.RegistrationGovernorKor r4 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                com.sec.internal.constants.Mno r0 = r4.mMno
                com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.KT
                if (r0 == r1) goto L_0x004d
                com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.LGU
                if (r0 != r1) goto L_0x0013
                goto L_0x004d
            L_0x0013:
                java.lang.String r4 = r4.LOG_TAG
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "onAllowedNetworkTypesChanged : prev= "
                r0.append(r1)
                com.sec.internal.ims.core.RegistrationGovernorKor r1 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                long r1 = r1.mAllowedNetworkType
                r0.append(r1)
                java.lang.String r1 = " new= "
                r0.append(r1)
                r0.append(r5)
                java.lang.String r0 = r0.toString()
                android.util.Log.i(r4, r0)
                com.sec.internal.ims.core.RegistrationGovernorKor r4 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                long r0 = r4.mAllowedNetworkType
                int r4 = (r0 > r5 ? 1 : (r0 == r5 ? 0 : -1))
                if (r4 == 0) goto L_0x004d
                com.sec.internal.ims.core.RegistrationGovernorKor r4 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                r4.mAllowedNetworkType = r5
                com.sec.internal.ims.core.RegistrationGovernorKor r3 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                r3.handleAllowedNetworkTypeChanged()
            L_0x004d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorKor.AllowedNetworkTypesListener.onAllowedNetworkTypesChanged(int, long):void");
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0099  */
    /* JADX WARNING: Removed duplicated region for block: B:29:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isLTEDataModeInternal() {
        /*
            r9 = this;
            r0 = 0
            r1 = 1
            int r2 = r9.mPhoneId     // Catch:{ Exception -> 0x006a }
            int r2 = com.sec.internal.helper.SimUtil.getSubId(r2)     // Catch:{ Exception -> 0x006a }
            android.content.Context r3 = r9.mContext     // Catch:{ Exception -> 0x006a }
            java.lang.Class<android.telephony.TelephonyManager> r4 = android.telephony.TelephonyManager.class
            java.lang.Object r3 = r3.getSystemService(r4)     // Catch:{ Exception -> 0x006a }
            android.telephony.TelephonyManager r3 = (android.telephony.TelephonyManager) r3     // Catch:{ Exception -> 0x006a }
            android.telephony.TelephonyManager r3 = r3.createForSubscriptionId(r2)     // Catch:{ Exception -> 0x006a }
            if (r3 != 0) goto L_0x0020
            java.lang.String r2 = r9.LOG_TAG     // Catch:{ Exception -> 0x006a }
            java.lang.String r3 = "isLTEDataModeInternal : TelephonyManager null"
            android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x006a }
            return r1
        L_0x0020:
            long r4 = r3.getAllowedNetworkTypesForReason(r0)     // Catch:{ Exception -> 0x006a }
            long r6 = r3.getAllowedNetworkTypesForReason(r1)     // Catch:{ Exception -> 0x006a }
            long r4 = r4 & r6
            r6 = 2
            long r7 = r3.getAllowedNetworkTypesForReason(r6)     // Catch:{ Exception -> 0x006a }
            long r4 = r4 & r7
            r7 = 3
            long r7 = r3.getAllowedNetworkTypesForReason(r7)     // Catch:{ Exception -> 0x006a }
            long r3 = r4 & r7
            int r3 = (int) r3     // Catch:{ Exception -> 0x006a }
            int r3 = android.telephony.RadioAccessFamily.getNetworkTypeFromRaf(r3)     // Catch:{ Exception -> 0x006a }
            if (r3 == 0) goto L_0x004a
            if (r3 == r6) goto L_0x004a
            r4 = 14
            if (r3 == r4) goto L_0x004a
            r4 = 18
            if (r3 != r4) goto L_0x0048
            goto L_0x004a
        L_0x0048:
            r4 = r1
            goto L_0x004b
        L_0x004a:
            r4 = r0
        L_0x004b:
            java.lang.String r5 = r9.LOG_TAG     // Catch:{ Exception -> 0x006b }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x006b }
            r6.<init>()     // Catch:{ Exception -> 0x006b }
            java.lang.String r7 = "isLTEDataModeInternal : netType = "
            r6.append(r7)     // Catch:{ Exception -> 0x006b }
            r6.append(r3)     // Catch:{ Exception -> 0x006b }
            java.lang.String r3 = " subid = "
            r6.append(r3)     // Catch:{ Exception -> 0x006b }
            r6.append(r2)     // Catch:{ Exception -> 0x006b }
            java.lang.String r2 = r6.toString()     // Catch:{ Exception -> 0x006b }
            android.util.Log.i(r5, r2)     // Catch:{ Exception -> 0x006b }
            goto L_0x0072
        L_0x006a:
            r4 = r1
        L_0x006b:
            java.lang.String r2 = r9.LOG_TAG
            java.lang.String r3 = "isLTEDataModeInternal : getAllowedNetworkTypesForReason fail"
            android.util.Log.i(r2, r3)
        L_0x0072:
            java.lang.String r2 = r9.LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r5 = "isLTEDataModeInternal : prev= "
            r3.append(r5)
            boolean r9 = r9.mLteModeOn
            r3.append(r9)
            java.lang.String r9 = " new="
            r3.append(r9)
            if (r4 != r1) goto L_0x008c
            r9 = r1
            goto L_0x008d
        L_0x008c:
            r9 = r0
        L_0x008d:
            r3.append(r9)
            java.lang.String r9 = r3.toString()
            android.util.Log.i(r2, r9)
            if (r4 != r1) goto L_0x009a
            r0 = r1
        L_0x009a:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorKor.isLTEDataModeInternal():boolean");
    }

    /* access modifiers changed from: protected */
    public void handleAllowedNetworkTypeChanged() {
        boolean isLTEDataModeInternal;
        if (!this.mTask.isRcsOnly() && !RegistrationUtils.isCmcProfile(this.mTask.getProfile()) && this.mLteModeOn != (isLTEDataModeInternal = isLTEDataModeInternal())) {
            this.mLteModeOn = isLTEDataModeInternal;
            onPreferredNetworkModeChanged();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x006f A[Catch:{ Exception -> 0x009f }] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0071 A[Catch:{ Exception -> 0x009f }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0074 A[Catch:{ Exception -> 0x009f }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isLTEDataModeEnabled() {
        /*
            r8 = this;
            boolean r0 = com.sec.internal.helper.OmcCode.isKOROmcCode()
            r1 = 1
            if (r0 == 0) goto L_0x00c1
            com.sec.internal.constants.Mno r0 = r8.mMno
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.KT
            if (r0 == r2) goto L_0x00c1
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.LGU
            if (r0 != r2) goto L_0x0013
            goto L_0x00c1
        L_0x0013:
            com.sec.internal.ims.core.RegisterTask r0 = r8.mTask
            boolean r0 = r0.isRcsOnly()
            if (r0 != 0) goto L_0x00c1
            com.sec.internal.ims.core.RegisterTask r0 = r8.mTask
            com.sec.ims.settings.ImsProfile r0 = r0.getProfile()
            boolean r0 = com.sec.internal.ims.core.RegistrationUtils.isCmcProfile(r0)
            if (r0 == 0) goto L_0x0029
            goto L_0x00c1
        L_0x0029:
            int r0 = r8.mPhoneId     // Catch:{ Exception -> 0x009f }
            int r0 = com.sec.internal.helper.SimUtil.getSubId(r0)     // Catch:{ Exception -> 0x009f }
            com.sec.internal.helper.os.ITelephonyManager r2 = r8.mTelephonyManager     // Catch:{ Exception -> 0x009f }
            int r3 = r8.mPhoneId     // Catch:{ Exception -> 0x009f }
            int r3 = com.sec.internal.helper.SimUtil.getSubId(r3)     // Catch:{ Exception -> 0x009f }
            int r2 = r2.getPreferredNetworkType(r3)     // Catch:{ Exception -> 0x009f }
            r3 = 0
            if (r2 == 0) goto L_0x004c
            r4 = 2
            if (r2 == r4) goto L_0x004c
            r4 = 14
            if (r2 == r4) goto L_0x004c
            r4 = 18
            if (r2 != r4) goto L_0x004a
            goto L_0x004c
        L_0x004a:
            r4 = r1
            goto L_0x004d
        L_0x004c:
            r4 = r3
        L_0x004d:
            java.lang.String r5 = r8.LOG_TAG     // Catch:{ Exception -> 0x009f }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x009f }
            r6.<init>()     // Catch:{ Exception -> 0x009f }
            java.lang.String r7 = "isLTEDataModeEnabled : netType = "
            r6.append(r7)     // Catch:{ Exception -> 0x009f }
            r6.append(r2)     // Catch:{ Exception -> 0x009f }
            java.lang.String r2 = " subid = "
            r6.append(r2)     // Catch:{ Exception -> 0x009f }
            r6.append(r0)     // Catch:{ Exception -> 0x009f }
            java.lang.String r0 = r6.toString()     // Catch:{ Exception -> 0x009f }
            android.util.Log.i(r5, r0)     // Catch:{ Exception -> 0x009f }
            boolean r0 = r8.mLteModeOn     // Catch:{ Exception -> 0x009f }
            if (r4 != r1) goto L_0x0071
            r2 = r1
            goto L_0x0072
        L_0x0071:
            r2 = r3
        L_0x0072:
            if (r0 == r2) goto L_0x00a6
            java.lang.String r0 = r8.LOG_TAG     // Catch:{ Exception -> 0x009f }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x009f }
            r2.<init>()     // Catch:{ Exception -> 0x009f }
            java.lang.String r5 = "isLTEDataModeEnabled : not match! mLteModeOn = "
            r2.append(r5)     // Catch:{ Exception -> 0x009f }
            boolean r5 = r8.mLteModeOn     // Catch:{ Exception -> 0x009f }
            r2.append(r5)     // Catch:{ Exception -> 0x009f }
            java.lang.String r5 = " isLTEDataMode ="
            r2.append(r5)     // Catch:{ Exception -> 0x009f }
            if (r4 != r1) goto L_0x008d
            goto L_0x008e
        L_0x008d:
            r1 = r3
        L_0x008e:
            r2.append(r1)     // Catch:{ Exception -> 0x009f }
            java.lang.String r1 = r2.toString()     // Catch:{ Exception -> 0x009f }
            android.util.Log.i(r0, r1)     // Catch:{ Exception -> 0x009f }
            boolean r0 = r8.isLTEDataModeInternal()     // Catch:{ Exception -> 0x009f }
            r8.mLteModeOn = r0     // Catch:{ Exception -> 0x009f }
            goto L_0x00a6
        L_0x009f:
            java.lang.String r0 = r8.LOG_TAG
            java.lang.String r1 = "isLTEDataModeEnabled : getPreferredNetworkType fail"
            android.util.Log.i(r0, r1)
        L_0x00a6:
            java.lang.String r0 = r8.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "isLTEDataModeEnabled : "
            r1.append(r2)
            boolean r2 = r8.mLteModeOn
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            boolean r8 = r8.mLteModeOn
            return r8
        L_0x00c1:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorKor.isLTEDataModeEnabled():boolean");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0013, code lost:
        if (r2 != false) goto L_0x0015;
     */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:28:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkVoLTEStatusChanged(com.sec.internal.ims.core.RegistrationGovernorKor.VoltePreferenceChangedReason r7) {
        /*
            r6 = this;
            boolean r0 = r6.getVolteServiceStatus()
            boolean r1 = r6.isVolteSettingEnabled()
            boolean r2 = r6.isLTEDataModeEnabled()
            com.sec.internal.ims.core.RegistrationGovernorKor$VoltePreferenceChangedReason r3 = com.sec.internal.ims.core.RegistrationGovernorKor.VoltePreferenceChangedReason.VOLTE_SETTING
            r4 = 1
            if (r7 != r3) goto L_0x0017
            if (r0 == 0) goto L_0x0021
            if (r2 == 0) goto L_0x0021
        L_0x0015:
            r0 = r4
            goto L_0x0023
        L_0x0017:
            com.sec.internal.ims.core.RegistrationGovernorKor$VoltePreferenceChangedReason r3 = com.sec.internal.ims.core.RegistrationGovernorKor.VoltePreferenceChangedReason.LTE_MODE
            if (r7 != r3) goto L_0x0021
            if (r0 == 0) goto L_0x0021
            if (r1 == 0) goto L_0x0021
            r1 = r2
            goto L_0x0015
        L_0x0021:
            r0 = 0
            r1 = r4
        L_0x0023:
            java.lang.String r2 = r6.LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r5 = "checkVoLTEStatusChanged : needReregi = "
            r3.append(r5)
            r3.append(r0)
            java.lang.String r5 = ", isVolteOn = "
            r3.append(r5)
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            android.util.Log.i(r2, r1)
            if (r0 == 0) goto L_0x00ac
            com.sec.internal.ims.core.RegisterTask r0 = r6.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r0 = r0.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            if (r0 != r1) goto L_0x0084
            com.sec.internal.ims.core.RegistrationGovernorKor$VoltePreferenceChangedReason r0 = com.sec.internal.ims.core.RegistrationGovernorKor.VoltePreferenceChangedReason.LTE_MODE
            if (r7 != r0) goto L_0x0084
            com.sec.internal.ims.core.RegistrationManagerHandler r7 = r6.mRegHandler
            r0 = 107(0x6b, float:1.5E-43)
            boolean r7 = r7.hasMessages(r0)
            if (r7 != 0) goto L_0x007c
            com.sec.internal.ims.core.RegisterTask r7 = r6.mTask
            r0 = 31
            r7.setDeregiReason(r0)
            com.sec.internal.ims.core.RegistrationManagerInternal r7 = r6.mRegMan
            com.sec.internal.ims.core.RegisterTask r0 = r6.mTask
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "checkVoLTEStatusChanged : abnormal case need de-reg and init reg"
            r1.append(r2)
            com.sec.internal.ims.core.RegisterTask r2 = r6.mTask
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r7.deregister(r0, r4, r4, r1)
        L_0x007c:
            com.sec.internal.ims.core.RegistrationManagerHandler r7 = r6.mRegHandler
            int r6 = r6.mPhoneId
            r7.sendTryRegister(r6)
            return
        L_0x0084:
            java.lang.String r0 = r6.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "checkVoLTEStatusChanged: force update "
            r1.append(r2)
            com.sec.internal.ims.core.RegisterTask r2 = r6.mTask
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.core.RegistrationManagerHandler r0 = r6.mRegHandler
            com.sec.internal.ims.core.RegisterTask r6 = r6.mTask
            com.sec.internal.ims.core.RegistrationGovernorKor$VoltePreferenceChangedReason r1 = com.sec.internal.ims.core.RegistrationGovernorKor.VoltePreferenceChangedReason.LTE_MODE
            if (r7 != r1) goto L_0x00a7
            r1 = 150(0x96, double:7.4E-322)
            goto L_0x00a9
        L_0x00a7:
            r1 = 0
        L_0x00a9:
            r0.requestForcedUpdateRegistration(r6, r1)
        L_0x00ac:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorKor.checkVoLTEStatusChanged(com.sec.internal.ims.core.RegistrationGovernorKor$VoltePreferenceChangedReason):void");
    }

    public boolean isThrottledforImsNotAvailable() {
        return this.mThrottledforImsNotAvailable;
    }

    public boolean isOmadmConfigAvailable() {
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming) {
            return false;
        }
        if (this.mMno == Mno.KT && "20".equals(this.mTelephonyManager.semGetTelephonyProperty(this.mPhoneId, "ril.simtype", ""))) {
            Log.i(this.LOG_TAG, "isOmadmConfigAvailable : KT_unreg SIM. do not trigger DM");
            return false;
        } else if (this.mMno == Mno.LGU && "18".equals(this.mTelephonyManager.semGetTelephonyProperty(this.mPhoneId, "ril.simtype", ""))) {
            Log.i(this.LOG_TAG, "isOmadmConfigAvailable : LGT_unreg SIM. do not trigger DM");
            return false;
        } else if (!OmcCode.isKOROmcCode()) {
            Log.i(this.LOG_TAG, "isOmadmConfigAvailable : oversea device and KOR sim. do not trigger DM");
            return false;
        } else {
            try {
                this.mContext.getPackageManager().getPackageInfo(ImsConstants.Packages.PACKAGE_DM_CLIENT, 0);
                return true;
            } catch (PackageManager.NameNotFoundException unused) {
                Log.i(this.LOG_TAG, "isOmadmConfigAvailable : DM Package not found");
                return false;
            }
        }
    }

    public void retryDNSQuery() {
        Log.i(this.LOG_TAG, "retryDNSQuery : getPcscfPreference(" + this.mTask.getProfile().getPcscfPreference() + ") mDnsQueryCount(" + this.mDnsQueryCount + ") mDmUpdatedFlag(" + this.mDmUpdatedFlag + ")");
        Mno mno = this.mMno;
        if (mno == Mno.KT) {
            int i = this.mDnsQueryCount;
            if (i < 1) {
                if (this.mTask.getProfile().getPcscfPreference() == 0) {
                    this.mTask.getProfile().setPcscfPreference(5);
                    if (this.mDmUpdatedFlag) {
                        checkProfileUpdateFromDM(true);
                    } else {
                        List lboPcscfAddressList = this.mTask.getProfile().getLboPcscfAddressList();
                        if (lboPcscfAddressList.isEmpty()) {
                            List<String> pcscfFromFile = getPcscfFromFile(this.mMno);
                            this.mTask.getProfile().setPcscfList(pcscfFromFile);
                            Log.i(this.LOG_TAG, "retryDNSQuery : use DEFAULT pcscf: " + pcscfFromFile);
                        } else {
                            this.mTask.getProfile().setPcscfList(lboPcscfAddressList);
                            Log.i(this.LOG_TAG, "retryDNSQuery : use OMADM pcscf: " + lboPcscfAddressList);
                        }
                    }
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                    this.mDnsQueryCount = 0;
                    return;
                }
                this.mRegHandler.sendTryRegister(this.mPhoneId, DNS_RETRY_TIME_MS);
            } else if (i == 1) {
                List<String> pcscfFromFile2 = getPcscfFromFile(mno);
                this.mTask.getProfile().setPcscfList(pcscfFromFile2);
                Log.i(this.LOG_TAG, "retryDNSQuery : use DEFAULT pcscf: " + pcscfFromFile2);
                this.mRegHandler.sendTryRegister(this.mPhoneId, DNS_RETRY_TIME_MS);
            } else {
                this.mTask.getProfile().setPcscfPreference(0);
                this.mDnsQueryCount = 0;
                return;
            }
            this.mDnsQueryCount++;
        } else if (mno == Mno.SKT && this.mTask.getProfile().getPcscfPreference() == 0) {
            this.mTask.getProfile().setPcscfPreference(5);
            List<String> pcscfFromFile3 = getPcscfFromFile(this.mMno);
            this.mTask.getProfile().setPcscfList(pcscfFromFile3);
            this.mTask.getProfile().setLboPcscfAddressList(pcscfFromFile3);
            Log.i(this.LOG_TAG, "retryDNSQuery : use DEFAULT pcscf: " + pcscfFromFile3);
            this.mRegHandler.sendTryRegister(this.mPhoneId);
            this.mDnsQueryCount = 0;
        }
    }

    private List<String> getPcscfFromFile(Mno mno) {
        List<String> asList;
        ArrayList arrayList = new ArrayList();
        if (mno == Mno.SKT) {
            String readFromJsonFile = ImsAutoUpdate.readFromJsonFile("SKT VoLTE", "pcscf");
            if (!CollectionUtils.isNullOrEmpty(readFromJsonFile)) {
                String str = this.LOG_TAG;
                Log.i(str, "getPcscfFromFile : SKT ImsAutoUpdate " + readFromJsonFile);
                asList = Arrays.asList(TextUtils.split(readFromJsonFile, ","));
            } else {
                String readFromJsonFile2 = ImsProfileCache.readFromJsonFile(this.mContext, "SKT VoLTE", "pcscf");
                if (!CollectionUtils.isNullOrEmpty(readFromJsonFile2)) {
                    String str2 = this.LOG_TAG;
                    Log.i(str2, "getPcscfFromFile : SKT ImsProfileCache " + readFromJsonFile2);
                    asList = Arrays.asList(TextUtils.split(readFromJsonFile2, ","));
                } else {
                    Log.i(this.LOG_TAG, "getPcscfFromFile : SKT fail to read pcscf from file");
                    asList = Arrays.asList(TextUtils.split(OMADM_SKT_DEFAULT_PCSCF, ","));
                }
            }
            return asList;
        } else if (mno != Mno.KT) {
            return arrayList;
        } else {
            String readFromJsonFile3 = ImsAutoUpdate.readFromJsonFile("KT VoLTE", "pcscf");
            if (!CollectionUtils.isNullOrEmpty(readFromJsonFile3)) {
                String str3 = this.LOG_TAG;
                Log.i(str3, "getPcscfFromFile : KT ImsAutoUpdate " + readFromJsonFile3);
                arrayList.add(readFromJsonFile3);
                return arrayList;
            }
            String readFromJsonFile4 = ImsProfileCache.readFromJsonFile(this.mContext, "KT VoLTE", "pcscf");
            if (!CollectionUtils.isNullOrEmpty(readFromJsonFile4)) {
                String str4 = this.LOG_TAG;
                Log.i(str4, "getPcscfFromFile : KT ImsProfileCache " + readFromJsonFile4);
                arrayList.add(readFromJsonFile4);
                return arrayList;
            }
            Log.i(this.LOG_TAG, "getPcscfFromFile : KT fail to read pcscf from file");
            arrayList.add(OMADM_KT_DEFAULT_PCSCF);
            return arrayList;
        }
    }

    public boolean isNeedDelayedDeregister() {
        String str = this.LOG_TAG;
        Log.i(str, "isNeedDelayedDeregister :  mNeedDelayedDeregister = " + this.mNeedDelayedDeregister);
        return this.mNeedDelayedDeregister || ((Boolean) Optional.ofNullable((ImModule) ImsRegistry.getServiceModuleManager().getImModule()).map(new RegistrationGovernorKor$$ExternalSyntheticLambda0(this)).orElse(Boolean.FALSE)).booleanValue();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$isNeedDelayedDeregister$0(ImModule imModule) {
        return Boolean.valueOf(imModule.hasIncomingSessionForA2P(this.mTask.getPhoneId()));
    }

    public void setNeedDelayedDeregister(boolean z) {
        String str = this.LOG_TAG;
        Log.i(str, "setNeedDelayedDeregister :  val = " + z);
        this.mNeedDelayedDeregister = z;
    }

    /* access modifiers changed from: package-private */
    public void checkDMConfigChange(ImsProfile imsProfile) {
        if (imsProfile == null) {
            Log.i(this.LOG_TAG, "checkDMConfigChange : dmProfile in null");
            return;
        }
        List lboPcscfAddressList = imsProfile.getLboPcscfAddressList();
        boolean z = ((lboPcscfAddressList == null || lboPcscfAddressList.equals(this.mPcscfList)) && !this.mPcscfList.isEmpty() && imsProfile.isIpSecEnabled() == this.mIpsecEnabled && imsProfile.isSupportSmsOverIms() == this.mSmsOverIp && imsProfile.isVolteServiceStatus() == this.mVolteServiceStatus) ? false : true;
        String str = this.LOG_TAG;
        Log.i(str, "checkDMConfigChange : previous pcscf = " + this.mPcscfList + ", new pcscf = " + lboPcscfAddressList);
        String str2 = this.LOG_TAG;
        Log.i(str2, "checkDMConfigChange : previous IpSecEnabled = " + this.mIpsecEnabled + ", new IpSecEnabled = " + imsProfile.isIpSecEnabled());
        String str3 = this.LOG_TAG;
        Log.i(str3, "checkDMConfigChange : previous SmsOverIp = " + this.mSmsOverIp + ", new SmsOverIp = " + imsProfile.isSupportSmsOverIms());
        String str4 = this.LOG_TAG;
        Log.i(str4, "checkDMConfigChange : previous ServiceStatus = " + this.mVolteServiceStatus + ", new ServiceStatus = " + imsProfile.isVolteServiceStatus());
        if (lboPcscfAddressList != null && !lboPcscfAddressList.equals(this.mPcscfList)) {
            resetPcscfList();
            resetIPSecAllow();
            Log.i(this.LOG_TAG, "checkDMConfigChange : resetPcscfList");
        }
        this.mPcscfList = imsProfile.getLboPcscfAddressList();
        this.mIpsecEnabled = imsProfile.isIpSecEnabled();
        this.mSmsOverIp = imsProfile.isSupportSmsOverIms();
        setOldVolteServiceStatus(imsProfile.isVolteServiceStatus());
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING) && z) {
            if (this.mTelephonyManager.getCallState() == 0) {
                Log.i(this.LOG_TAG, "checkDMConfigChange : need de-reg and init reg");
                this.mHasPendingInitRegistrationByDMConfigChange = false;
                this.mTask.setDeregiReason(29);
                this.mRegMan.deregister(this.mTask, true, true, "checkDMConfigChange : need de-reg and init reg");
                this.mRegHandler.sendTryRegister(this.mPhoneId);
                return;
            }
            Log.i(this.LOG_TAG, "checkDMConfigChange : de-reg and init reg after call end");
            this.mHasPendingInitRegistrationByDMConfigChange = true;
        }
    }

    public void onTelephonyCallStatusChanged(int i) {
        IVolteServiceModule iVolteServiceModule;
        setCallStatus(i);
        String str = this.LOG_TAG;
        Log.i(str, "onTelephonyCallStatusChanged: " + i + " mTask:" + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ")");
        super.onTelephonyCallStatusChanged(i);
        if (!this.mTask.isRcsOnly()) {
            if (getCallStatus() == 0) {
                if (this.mHasPendingInitRegistrationByDMConfigChange) {
                    Log.i(this.LOG_TAG, "onTelephonyCallStatusChanged : do pending de-reg and init reg");
                    this.mHasPendingInitRegistrationByDMConfigChange = false;
                    this.mTask.setDeregiReason(29);
                    this.mRegMan.deregister(this.mTask, true, true, "onTelephonyCallStatusChanged : do pending de-reg and init reg");
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                    return;
                } else if (this.mHasPendingNotifyImsNotAvailable) {
                    this.mRegMan.getEventLog().logAndAdd("onTelephonyCallStatusChanged : send pending notifyImsNotAvailable");
                    this.mHasPendingNotifyImsNotAvailable = false;
                    this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                    this.mRegMan.notifyImsNotAvailable(this.mTask, true);
                    return;
                }
            }
            if (SimUtil.getPhoneCount() > 1 && getCallStatus() == 2 && this.mTask.getState() == RegistrationConstants.RegisterTaskState.IDLE && (iVolteServiceModule = this.mVsm) != null && iVolteServiceModule.hasCsCall(this.mPhoneId)) {
                Log.i(this.LOG_TAG, "onTelephonyCallStatusChanged : tryregister during cs call");
                this.mRegHandler.sendTryRegister(this.mPhoneId);
            }
        }
    }

    public void resetIPSecAllow() {
        this.mIPsecAllow = true;
    }

    public void resetPcscfPreference() {
        if (!this.mTask.isRcsOnly() && this.mTask.getProfile().getPcscfPreference() != 2) {
            if (this.mMno == Mno.KT) {
                this.mTask.getProfile().setPcscfPreference(0);
            } else if (this.mTask.isNeedOmadmConfig()) {
                this.mTask.getProfile().setPcscfPreference(5);
            }
            String str = this.LOG_TAG;
            Log.i(str, "resetPcscfPreference : getPcscfPreference = " + this.mTask.getProfile().getPcscfPreference());
        }
    }

    /* access modifiers changed from: protected */
    public void startPDNdisconnectTimer(long j) {
        stopPDNdisconnectTimer();
        String str = this.LOG_TAG;
        Log.i(str, "startPDNdisconnectTimer: millis " + j);
        this.mPDNdisconnectTimeout = this.mRegHandler.startDisconnectPdnTimer(this.mTask, j);
    }

    /* access modifiers changed from: protected */
    public void stopPDNdisconnectTimer() {
        if (this.mPDNdisconnectTimeout != null) {
            Log.i(this.LOG_TAG, "stopPDNdisconnectTimer");
            this.mRegHandler.stopTimer(this.mPDNdisconnectTimeout);
            this.mPDNdisconnectTimeout = null;
        }
    }

    public void resetAllRetryFlow() {
        this.mConsecutiveForbiddenCounter = 0;
        this.mIsAkaChallengeTimeout = false;
        this.mDnsQueryCount = 0;
        stopPDNdisconnectTimer();
        stopRetryTimer();
    }

    public void unRegisterIntentReceiver() {
        Log.i(this.LOG_TAG, "Un-register Intent receiver(s)");
        try {
            this.mContext.unregisterReceiver(this.mIntentReceiverKor);
        } catch (IllegalArgumentException unused) {
            Log.e(this.LOG_TAG, "unRegisterIntentReceiver: Receiver not registered!");
        }
    }

    /* access modifiers changed from: package-private */
    public void makeRegistrationFailedToast() {
        Mno mno = this.mMno;
        if (mno == Mno.SKT) {
            Context context = this.mContext;
            Toast.makeText(context, context.getResources().getString(R.string.regi_failed_msg_skt), 1).show();
        } else if (mno == Mno.KT) {
            Context context2 = this.mContext;
            Toast.makeText(context2, context2.getResources().getString(R.string.regi_failed_msg_kt), 1).show();
            this.mIsReadyToGetReattach = true;
        } else if (mno == Mno.LGU) {
            Context context3 = this.mContext;
            Toast.makeText(context3, context3.getResources().getString(R.string.regi_failed_msg_lgu, new Object[]{"1544-0010"}), 1).show();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x007d, code lost:
        if (r1 == false) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0086, code lost:
        if (r1 == false) goto L_0x0088;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isNeedForcibleSmsOverImsOn() {
        /*
            r9 = this;
            com.sec.internal.constants.Mno r0 = r9.mMno
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.KT
            r2 = 0
            if (r0 == r1) goto L_0x000b
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.LGU
            if (r0 != r1) goto L_0x0089
        L_0x000b:
            boolean r0 = r9.isVolteEnabled()
            android.content.Context r1 = r9.mContext
            java.lang.String r3 = ""
            int r4 = r9.mPhoneId
            java.lang.String r5 = "sms_over_ip_network_indication"
            java.lang.String r1 = com.sec.ims.settings.NvConfiguration.get(r1, r5, r3, r4)
            java.lang.String r3 = "1"
            boolean r1 = android.text.TextUtils.equals(r1, r3)
            java.lang.String r3 = r9.LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "isNeedForcibleSmsOverImsOn: isVolteEnabled "
            r4.append(r5)
            r4.append(r0)
            java.lang.String r5 = " isSMSIP "
            r4.append(r5)
            r4.append(r1)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            com.sec.internal.constants.Mno r3 = r9.mMno
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.KT
            r5 = 1
            if (r3 != r4) goto L_0x0080
            com.sec.internal.ims.core.RegistrationManagerInternal r3 = r9.mRegMan
            int r4 = r9.mPhoneId
            com.sec.internal.constants.ims.os.NetworkEvent r3 = r3.getNetworkEvent(r4)
            boolean r3 = r3.isDataRoaming
            com.sec.internal.ims.core.PdnController r4 = r9.mPdnController
            int r6 = r9.mPhoneId
            boolean r4 = r4.isEpsOnlyReg(r6)
            java.lang.String r6 = r9.LOG_TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "isNeedForcibleSmsOverImsOn: isDataRoaming "
            r7.append(r8)
            r7.append(r3)
            java.lang.String r8 = " isEpsOnlyReg "
            r7.append(r8)
            r7.append(r4)
            java.lang.String r7 = r7.toString()
            android.util.Log.i(r6, r7)
            if (r3 != 0) goto L_0x0089
            if (r4 == 0) goto L_0x0089
            if (r0 == 0) goto L_0x0089
            if (r1 != 0) goto L_0x0089
            goto L_0x0088
        L_0x0080:
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.LGU
            if (r3 != r4) goto L_0x0089
            if (r0 == 0) goto L_0x0089
            if (r1 != 0) goto L_0x0089
        L_0x0088:
            r2 = r5
        L_0x0089:
            java.lang.String r9 = r9.LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "isNeedForcibleSmsOverImsOn: isNeedSmsOverImsOn "
            r0.append(r1)
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            android.util.Log.i(r9, r0)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorKor.isNeedForcibleSmsOverImsOn():boolean");
    }

    private boolean hasCall() {
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        boolean z = iVolteServiceModule != null && iVolteServiceModule.getSessionCount(this.mPhoneId) > 0 && this.mVsm.hasActiveCall(this.mPhoneId);
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "hasCall:" + z);
        return z;
    }

    /* access modifiers changed from: private */
    public void handlePeriodicPollingTimeoutIntent() {
        if (!this.mTask.isRcsOnly()) {
            Log.i(this.LOG_TAG, "onReceive: dm polling timeout");
            this.mRegHandler.sendRequestDmConfig(this.mTask);
        }
    }

    /* access modifiers changed from: private */
    public void handleFlightModeIntent(Intent intent) {
        Intent registerReceiver;
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            this.mTask.setDeregiReason(23);
            int intExtra = intent.getIntExtra("powerofftriggered", -1);
            String str = this.LOG_TAG;
            Log.i(str, "powerOff :" + intExtra);
            if (!(intExtra == -1 || (registerReceiver = this.mContext.registerReceiver((BroadcastReceiver) null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) == null)) {
                int intExtra2 = (registerReceiver.getIntExtra("level", 0) * 100) / registerReceiver.getIntExtra("scale", 100);
                String str2 = this.LOG_TAG;
                Log.i(str2, "battery level: " + intExtra2);
                if (intExtra2 <= 2) {
                    this.mTask.setDeregiReason(33);
                }
            }
            String str3 = this.LOG_TAG;
            Log.i(str3, "onReceive: FLIGHT_MODE is changed - reason : " + this.mTask.getDeregiReason());
            setNeedDelayedDeregister(true);
            Log.i(this.LOG_TAG, "deregister delay 300 ms for sending BYE");
            this.mRegMan.deregister(this.mTask, false, false, "flight mode enabled");
            resetRetry();
            resetAllRetryFlow();
        }
    }

    /* access modifiers changed from: private */
    public void handleAirplaneModeIntent(Intent intent) {
        if (!this.mTask.isRcsOnly()) {
            resetPcscfPreference();
        } else if (!((Boolean) Optional.ofNullable(intent.getExtras()).map(new RegistrationGovernorKor$$ExternalSyntheticLambda1()).orElse(Boolean.FALSE)).booleanValue()) {
            this.mConfigModule.getAcsConfig(this.mPhoneId).setAcsCompleteStatus(false);
            this.mConfigModule.getAcsConfig(this.mPhoneId).setForceAcs(true);
            Log.i(this.LOG_TAG, "onReceive: AIRPLANE_MODE off. reset ACS Info");
        }
    }

    /* access modifiers changed from: private */
    public void handleUsimDownloadEndIntent() {
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleNwRejectIntent(Intent intent) {
        int i;
        String str;
        if (this.mTask.isRcsOnly()) {
            String stringExtra = intent.getStringExtra(ImsConstants.Intents.EXTRA_CAUSE_KEY);
            if (stringExtra == null || stringExtra.isEmpty()) {
                Log.e(this.LOG_TAG, "empty CAUSE");
                return;
            }
            try {
                i = Integer.parseInt(stringExtra);
            } catch (NumberFormatException unused) {
                Log.e(this.LOG_TAG, "invalid CAUSE");
                i = 0;
            }
            Log.i(this.LOG_TAG, "onReceive: " + intent.getAction() + ", CAUSE: " + i);
            if (checkValidRejectCode(i)) {
                this.mTask.setDeregiReason(10);
                if (ImsConstants.Intents.INTENT_ACTION_REGIST_REJECT.equals(intent.getAction())) {
                    str = "nw_regist_reject";
                } else {
                    str = ImsConstants.Intents.INTENT_ACTION_LTE_REJECT.equals(intent.getAction()) ? "nw_lte_reject" : null;
                }
                this.mRegMan.deregister(this.mTask, false, true, str);
                resetRetry();
                resetAllRetryFlow();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleWapPushDmNotiReceivedIntent() {
        if (!this.mTask.isRcsOnly()) {
            Log.i(this.LOG_TAG, "onReceive: INTENT_WAP_PUSH_DM_NOTI_RECEIVED is received");
            if (this.mIsPermanentStopped) {
                this.mIsPermanentStopped = false;
                resetIPSecAllow();
                this.mCurImpu = 0;
                this.mRegMan.getEventLog().logAndAdd("handleWapPushDmNotiReceivedIntent: reset mIsPermanentStopped");
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleNewOutgoingCallIntent() {
        if ((this.mTask.getProfile().hasService("mmtel") || this.mTask.getProfile().hasService("mmtel-video")) && this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 1 && !this.mTask.getProfile().hasEmergencySupport() && !hasCall()) {
            Log.i(this.LOG_TAG, "onReceive: INTENT_NEW_OUTGOING_CALL is received");
            resetRetry();
            resetAllRetryFlow();
            deregisterIfConnecting(37);
        }
    }

    /* access modifiers changed from: private */
    public void handleBootCompletedIntent() {
        if (!this.mTask.isRcsOnly()) {
            Log.i(this.LOG_TAG, "onReceive: ACTION_BOOT_COMPLETED is received");
            checkUnprocessedOmadmConfig();
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyMmtelUserSettings(Set<String> set, int i) {
        if (set == null) {
            return new HashSet();
        }
        if (!isVolteEnabled()) {
            if (!isVolteSettingEnabled()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
            } else if (!getVolteServiceStatus()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_DM_OFF.getCode());
            } else if (!isLTEDataModeEnabled()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_3G_PREFERRED_MODE.getCode());
            }
            removeService(set, "mmtel", "isVolteEnabled disabled.");
        }
        return set;
    }

    public void onPdnConnected() {
        LinkPropertiesWrapper linkProperties = this.mPdnController.getLinkProperties(this.mTask);
        if (linkProperties == null) {
            Log.e(this.LOG_TAG, "onPdnConnected: LinkProperties are not exist! return..");
        } else if (this.mTask.getPdnType() == 11) {
            this.mTask.clearSuspended();
            this.mTask.clearSuspendedBySnapshot();
            if (this.mLocalAddress == null) {
                this.mLocalAddress = linkProperties.getAddresses();
            }
            if (!this.mLocalAddress.equals(linkProperties.getAddresses())) {
                Log.i(this.LOG_TAG, "onPdnConnected: local IP is changed. dm&initial regi. are needed.");
                resetRetry();
                this.mLocalAddress = linkProperties.getAddresses();
                this.mRegMan.setOmadmState(this.mPhoneId, RegistrationManager.OmadmConfigState.IDLE);
                resetPcscfPreference();
                resetIPSecAllow();
                releaseThrottle(5);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void deregisterIfConnecting(int i) {
        this.mTask.setDeregiReason(i);
        if (i == 13 && this.mTask.getUserAgent() == null && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mRegHandler.sendTryRegister(this.mPhoneId, 1000);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            Log.i(this.LOG_TAG, "deregisterIfConnecting : stopPdnConnectivity");
            return;
        }
        boolean z = this.mTask.getState() != RegistrationConstants.RegisterTaskState.CONNECTING;
        RegisterTask registerTask = this.mTask;
        registerTask.mKeepPdn = z;
        this.mRegMan.deregister(registerTask, true, z, "user triggered");
        Log.i(this.LOG_TAG, "deregisterIfConnecting : deregister");
    }

    public void resetPdnFailureInfo() {
        super.resetPdnFailureInfo();
        if (this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
            this.mRequestPdnTimeoutCount = 0;
            if (isMobilePreferredForRcs() && this.mTask.getPdnType() == 0) {
                Log.i(this.LOG_TAG, "resetPdnFailureInfo: rcs");
                this.mHasNetworkFailure = false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean needToHandleUnlimited404() {
        return !OmcCode.isKOROmcCode() && this.mTask.getMno() == Mno.KT;
    }

    /* access modifiers changed from: package-private */
    public void updateEutranValues() {
        if (this.mTask.getProfile().hasService("mmtel")) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri.Builder buildUpon = Uri.parse("content://com.sec.ims.settings/global").buildUpon();
            contentResolver.update(buildUpon.fragment("simslot" + this.mPhoneId).build(), contentValues, (String) null, (String[]) null);
        }
    }

    public void clear() {
        super.clear();
        unRegisterIntentReceiver();
        unregisterAllowedNetworkTypesListener();
    }
}
