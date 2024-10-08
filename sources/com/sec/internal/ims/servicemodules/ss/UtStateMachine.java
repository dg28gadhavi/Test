package com.sec.internal.ims.servicemodules.ss;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.telephony.PreciseDataConnectionState;
import android.telephony.ims.ImsSsInfo;
import android.text.TextUtils;
import com.sec.ims.settings.UserConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.XmlCreator;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.httpclient.DnsController;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.gba.GbaException;
import com.sec.internal.ims.servicemodules.ss.CallBarringData;
import com.sec.internal.ims.servicemodules.ss.CallForwardingData;
import com.sec.internal.ims.servicemodules.ss.SsRuleData;
import com.sec.internal.ims.servicemodules.ss.UtConstant;
import com.sec.internal.ims.servicemodules.volte2.CallStateMachine;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.PdnEventListener;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.net.SocketFactory;
import okhttp3.Dns;

public class UtStateMachine extends StateMachine {
    public static final int DOCUMENT_CACHE_RESET_TIMEOUT = 1000;
    public static final long HTTP_CONNECTION_TIMEOUT = 10000;
    public static final long HTTP_READ_TIMEOUT = 10000;
    public static final long HTTP_READ_TIMEOUT_GCF = 2000;
    public static final long HTTP_READ_TIMEOUT_TMB = 32000;
    public static final String LOG_TAG = UtServiceModule.class.getSimpleName();
    public static final int MAX_RETRY_COUNT_412 = 3;
    public static final int PDN_LINGER_TIMEOUT = 5000;
    public static final long SECOND = 1000;
    private static int mCBIdCounter = 0;
    public boolean isGetBeforePut = false;
    public boolean isRetryingCreatePdn = false;
    /* access modifiers changed from: private */
    public ApnSettings mApn = null;
    public int mBsfRetryCounter = 0;
    protected CallForwardingData mCFCache = null;
    /* access modifiers changed from: private */
    public UtConfigData mConfig = null;
    /* access modifiers changed from: private */
    public Context mContext;
    public int mCount412RetryDone = 0;
    private Dns mDns = null;
    protected List<InetAddress> mDnsAddresses = new ArrayList();
    public UtFeatureData mFeature = null;
    public boolean mForce403Error = false;
    protected boolean mHasCFCache = false;
    protected boolean mHasICBCache = false;
    protected boolean mHasOCBCache = false;
    protected CallBarringData mICBCache = null;
    private final IImsFramework mImsFramework;
    public boolean mIsFailedBySuspended = false;
    public boolean mIsGetAfter412 = false;
    public boolean mIsGetForAllCb = false;
    public boolean mIsGetSdBy404 = false;
    private boolean mIsRequestFailed = false;
    private boolean mIsRunningRequest = false;
    public boolean mIsSuspended = false;
    public boolean mIsUtConnectionError = false;
    public int mMainCondition = -1;
    public int mNafRetryCounter = 0;
    /* access modifiers changed from: private */
    public Network mNetwork = null;
    protected CallBarringData mOCBCache = null;
    public IPdnController mPdnController = null;
    PdnEventListener mPdnListener = new PdnEventListener() {
        public void onConnected(int i, Network network) {
            String str = UtStateMachine.LOG_TAG;
            int i2 = UtStateMachine.this.mPhoneId;
            UtLog.i(str, i2, "onConnected " + i + " with " + network + " mPdnType " + UtStateMachine.this.mPdnType);
            UtStateMachine utStateMachine = UtStateMachine.this;
            if (i == utStateMachine.mPdnType && network != null) {
                utStateMachine.mSocketFactory = network.getSocketFactory();
                UtStateMachine.this.mNetwork = network;
                NetworkInfo networkInfo = ((ConnectivityManager) UtStateMachine.this.mContext.getSystemService("connectivity")).getNetworkInfo(network);
                String extraInfo = networkInfo != null ? networkInfo.getExtraInfo() : null;
                UtStateMachine utStateMachine2 = UtStateMachine.this;
                utStateMachine2.mApn = ApnSettings.load(utStateMachine2.mContext, extraInfo, UtStateMachine.this.mConfig.apnSelection, SimUtil.getSubId(UtStateMachine.this.mPhoneId));
                UtStateMachine.this.updateDnsInfo();
                UtStateMachine utStateMachine3 = UtStateMachine.this;
                utStateMachine3.sendMessage(utStateMachine3.obtainMessage(1));
            }
        }

        public void onDisconnected(int i) {
            String str = UtStateMachine.LOG_TAG;
            int i2 = UtStateMachine.this.mPhoneId;
            UtLog.i(str, i2, "onDisconnected " + i);
            UtStateMachine utStateMachine = UtStateMachine.this;
            utStateMachine.mSocketFactory = null;
            utStateMachine.mNetwork = null;
            UtStateMachine.this.refreshDns();
            UtStateMachine.this.sendMessage(2);
        }

        public void onSuspended(int i) {
            UtStateMachine utStateMachine = UtStateMachine.this;
            if (i == utStateMachine.mPdnType) {
                String str = UtStateMachine.LOG_TAG;
                int i2 = utStateMachine.mPhoneId;
                UtLog.i(str, i2, "onSuspended " + i);
                UtStateMachine.this.mIsSuspended = true;
            }
        }

        public void onResumed(int i) {
            UtStateMachine utStateMachine = UtStateMachine.this;
            if (i == utStateMachine.mPdnType) {
                String str = UtStateMachine.LOG_TAG;
                int i2 = utStateMachine.mPhoneId;
                UtLog.i(str, i2, "onResumed " + i);
                UtStateMachine utStateMachine2 = UtStateMachine.this;
                utStateMachine2.mIsSuspended = false;
                if (utStateMachine2.mIsFailedBySuspended) {
                    utStateMachine2.mIsFailedBySuspended = false;
                    utStateMachine2.sendMessage(utStateMachine2.obtainMessage(1));
                }
            }
        }
    };
    public int mPdnRetryCounter = 0;
    public int mPdnType = -1;
    private final List<UtProfile> mPendingRequests = new ArrayList();
    public int mPhoneId = -1;
    public int mPrevGetType = -1;
    protected CallForwardingData mPreviousCFCache = new CallForwardingData();
    protected UtProfile mProfile = null;
    protected RequestState mRequestState = null;
    protected ResponseState mResponseState = null;
    public boolean mSeparatedCFNL = false;
    public boolean mSeparatedCFNRY = false;
    public boolean mSeparatedCfAll = false;
    public boolean mSeparatedMedia = false;
    public SocketFactory mSocketFactory = null;
    protected UtStateMachine mThisSm = this;
    HttpRequestParams.HttpRequestCallback mUtCallback = new HttpRequestParams.HttpRequestCallback() {
        public void onComplete(HttpResponseParams httpResponseParams) {
            UtStateMachine.this.sendMessage(10, (Object) httpResponseParams);
        }

        public void onFail(IOException iOException) {
            int i;
            if (!(iOException.getCause() instanceof GbaException) || (i = ((GbaException) iOException.getCause()).getCode()) <= 99) {
                i = 1015;
            }
            UtStateMachine.this.sendMessage(11, i, 0, iOException.getMessage());
        }
    };
    public int mUtHttpRetryCounter = 0;
    public int mUtRetryCounter = 0;
    public final UtServiceModule mUtServiceModule;
    public boolean needPdnRequestForCW = true;

    private int getApnSettingFromPdnType(int i) {
        if (i == 0) {
            return 17;
        }
        if (i != 12) {
            return i != 27 ? -1 : 2048;
        }
        return 128;
    }

    protected UtStateMachine(String str, Looper looper, UtServiceModule utServiceModule, IImsFramework iImsFramework, Context context) {
        super(str, looper);
        this.mUtServiceModule = utServiceModule;
        this.mImsFramework = iImsFramework;
        this.mContext = context;
        this.mRequestState = new RequestState(this);
        this.mResponseState = new ResponseState(this);
    }

    /* access modifiers changed from: protected */
    public void init(int i) {
        addState(this.mRequestState);
        addState(this.mResponseState);
        this.mPhoneId = i;
        this.mPdnController = this.mImsFramework.getPdnController();
        setInitialState(this.mRequestState);
        this.mPendingRequests.clear();
        unlockProcessingRequest();
        removeMessages(14);
    }

    /* access modifiers changed from: protected */
    public void enqueueProfile(UtProfile utProfile) {
        synchronized (this.mPendingRequests) {
            this.mPendingRequests.add(utProfile);
        }
    }

    /* access modifiers changed from: protected */
    public UtProfile dequeueProfile() {
        UtProfile remove;
        synchronized (this.mPendingRequests) {
            remove = this.mPendingRequests.remove(0);
        }
        return remove;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0015, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean lockProcessingRequest() {
        /*
            r2 = this;
            java.util.List<com.sec.internal.ims.servicemodules.ss.UtProfile> r0 = r2.mPendingRequests
            monitor-enter(r0)
            java.util.List<com.sec.internal.ims.servicemodules.ss.UtProfile> r1 = r2.mPendingRequests     // Catch:{ all -> 0x0017 }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x0017 }
            if (r1 != 0) goto L_0x0014
            boolean r1 = r2.mIsRunningRequest     // Catch:{ all -> 0x0017 }
            if (r1 != 0) goto L_0x0014
            r1 = 1
            r2.mIsRunningRequest = r1     // Catch:{ all -> 0x0017 }
            monitor-exit(r0)     // Catch:{ all -> 0x0017 }
            return r1
        L_0x0014:
            monitor-exit(r0)     // Catch:{ all -> 0x0017 }
            r2 = 0
            return r2
        L_0x0017:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0017 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.lockProcessingRequest():boolean");
    }

    /* access modifiers changed from: protected */
    public boolean unlockProcessingRequest() {
        synchronized (this.mPendingRequests) {
            if (!this.mPendingRequests.isEmpty()) {
                return false;
            }
            this.mIsRunningRequest = false;
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void query(UtProfile utProfile) {
        enqueueProfile(utProfile);
        if (lockProcessingRequest()) {
            processUtRequest();
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Other request is processing now");
        }
    }

    /* access modifiers changed from: protected */
    public void processUtRequest() {
        removeMessages(15);
        sendMessageDelayed(15, 1017, 32500);
        this.mProfile = dequeueProfile();
        removeMessages(2);
        removeMessages(100);
        initializeUtParameters();
        int checkUtInternalError = checkUtInternalError();
        if (checkUtInternalError != 0) {
            sendMessageDelayed(12, checkUtInternalError, 100);
        } else if (isPutRequestBlocked()) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "Insertion of new rule is prohibited.");
            sendMessageDelayed(12, 1012, 100);
        } else {
            UtConfigData utConfigData = this.mConfig;
            if (utConfigData != null) {
                utConfigData.impu = this.mUtServiceModule.getPublicId(this.mPhoneId);
            }
            if (this.mUtServiceModule.isTerminalRequest(this.mPhoneId, this.mProfile)) {
                sendMessageDelayed(4, 100);
            } else {
                sendMessageDelayed(100, 100);
            }
        }
    }

    public boolean isPutRequestBlocked() {
        if (this.mFeature.insertNewRule) {
            return false;
        }
        int i = this.mProfile.type;
        if (i == 101) {
            if (this.mCFCache == null || hasConditionOnCfCache()) {
                return false;
            }
            return true;
        } else if (i == 105) {
            if (this.mOCBCache == null || hasConditionOnCbCache()) {
                return false;
            }
            return true;
        } else if (i != 103 || this.mICBCache == null || hasConditionOnCbCache()) {
            return false;
        } else {
            return true;
        }
    }

    private void initializeUtParameters() {
        this.mUtHttpRetryCounter = 0;
        this.mUtRetryCounter = 0;
        this.mBsfRetryCounter = 0;
        this.mNafRetryCounter = 0;
        this.mSeparatedCFNL = false;
        this.mIsUtConnectionError = false;
        this.mIsRequestFailed = false;
        this.mIsFailedBySuspended = false;
        this.mIsSuspended = false;
        this.mSeparatedMedia = false;
        this.mSeparatedCfAll = false;
        this.mSeparatedCFNRY = false;
        this.mMainCondition = -1;
    }

    /* access modifiers changed from: protected */
    public int checkUtInternalError() {
        if (isForbidden()) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "SS request is blocked by previous 403 error");
            return 1003;
        } else if (!UtUtils.isCallBarringType(this.mProfile.type) || this.mProfile.condition != 7) {
            int checkAvailabilityError = this.mUtServiceModule.checkAvailabilityError(this.mPhoneId);
            if (checkAvailabilityError != 0) {
                return checkAvailabilityError;
            }
            if (this.mUtServiceModule.isInvalidUtRequest(this.mPhoneId, this.mProfile)) {
                return 1008;
            }
            if (!this.mUtServiceModule.isTerminalRequest(this.mPhoneId, this.mProfile) && !this.mUtServiceModule.checkXcapApn(this.mPhoneId)) {
                return 1009;
            }
            Mno simMno = SimUtil.getSimMno(this.mPhoneId);
            if ((simMno == Mno.KOODO || simMno == Mno.TELUS) && UtUtils.isCallBarringType(this.mProfile.type)) {
                return 1010;
            }
            if (simMno != Mno.WIND_GREECE || !isServiceDeactive()) {
                return 0;
            }
            IMSLog.e(LOG_TAG, this.mPhoneId, "Service is disabled on network side");
            return 1011;
        } else {
            IMSLog.e(LOG_TAG, this.mPhoneId, "not support All CB over IMS. CSFB.");
            return 1002;
        }
    }

    /* access modifiers changed from: protected */
    public void completeUtRequest() {
        completeUtRequest((Bundle[]) null);
    }

    /* access modifiers changed from: protected */
    public void completeUtRequest(boolean z) {
        Bundle[] bundleArr = {new Bundle()};
        bundleArr[0].putBoolean("status", z);
        completeUtRequest(bundleArr);
    }

    /* access modifiers changed from: protected */
    public void completeUtRequest(Bundle bundle) {
        completeUtRequest(new Bundle[]{bundle});
    }

    /* access modifiers changed from: protected */
    public void completeUtRequest(Bundle[] bundleArr) {
        UtProfile utProfile = this.mProfile;
        int i = utProfile.type;
        int i2 = utProfile.requestId;
        printCompleteLog(bundleArr, i, i2);
        if (SimUtil.getSimMno(this.mPhoneId).isChn()) {
            DnsController.correctServerAddr(this.mNafRetryCounter, this.mBsfRetryCounter);
        }
        removeMessages(15);
        if (this.mFeature.isDisconnectXcapPdn) {
            sendDisconnectPdnWithDelay();
        }
        this.mProfile = null;
        transitionTo(this.mRequestState);
        this.mUtServiceModule.notifySuccessResult(this.mPhoneId, i, i2, bundleArr);
        if (!unlockProcessingRequest()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Process next request...");
            processUtRequest();
        }
    }

    private void notifyFailResult(int i, Bundle bundle) {
        int i2 = this.mProfile.type;
        if (!(i2 == 101 || i2 == 103 || i2 == 105 || i2 == 119)) {
            switch (i2) {
                case 114:
                    if (this.mIsGetAfter412) {
                        this.mCount412RetryDone = 0;
                        bundle.putInt("errorCode", UtConstant.UtError.HTTP_412_PRECONDITION_FAILED);
                        bundle.putString("errorMsg", "Precondition Failed");
                        i2 = 115;
                        break;
                    }
                    break;
                case 115:
                    break;
                case 116:
                    if (!this.mIsGetSdBy404) {
                        this.mUtServiceModule.setSentSimServDoc(this.mPhoneId, false);
                        return;
                    }
                    return;
            }
        }
        this.mCount412RetryDone = 0;
        this.mUtServiceModule.notifyFailResult(this.mPhoneId, i2, i, bundle);
    }

    /* access modifiers changed from: protected */
    public void failUtRequest(Bundle bundle) {
        UtProfile utProfile = this.mProfile;
        int i = utProfile.type;
        int i2 = utProfile.requestId;
        printFailLog(bundle, i, i2);
        this.mIsGetForAllCb = false;
        this.mIsGetAfter412 = false;
        this.isGetBeforePut = false;
        removeMessages(15);
        UtFeatureData utFeatureData = this.mFeature;
        if (utFeatureData == null || utFeatureData.isDisconnectXcapPdn) {
            sendDisconnectPdnWithDelay();
        }
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        if ((simMno == Mno.CTC || simMno == Mno.CTCMO) && bundle.getInt("errorCode", 0) == 403) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "CTC have to retry to CDMA dial");
            bundle.putInt("errorCode", CallStateMachine.DELAYED_EPSFB_CHECK_TIMING);
        }
        notifyFailResult(i2, bundle);
        this.mProfile = null;
        transitionTo(this.mRequestState);
        if (!unlockProcessingRequest()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Process next request...");
            processUtRequest();
        }
    }

    public boolean isServiceDeactive() {
        int i = this.mProfile.type;
        if (i == 101) {
            CallForwardingData callForwardingData = this.mCFCache;
            if (callForwardingData == null || callForwardingData.active) {
                return false;
            }
            return true;
        } else if (i == 103) {
            CallBarringData callBarringData = this.mICBCache;
            if (callBarringData == null || callBarringData.active) {
                return false;
            }
            return true;
        } else if (i != 105) {
            return false;
        } else {
            CallBarringData callBarringData2 = this.mOCBCache;
            if (callBarringData2 == null || callBarringData2.active) {
                return false;
            }
            return true;
        }
    }

    public boolean hasConditionOnCfCache() {
        int i = this.mProfile.condition;
        if (i == 7) {
            return true;
        }
        if (i == 4 || i == 5) {
            for (int i2 = i == 5 ? 1 : 0; i2 < 4; i2++) {
                if (!this.mCFCache.isExist(i2)) {
                    String str = LOG_TAG;
                    int i3 = this.mPhoneId;
                    IMSLog.e(str, i3, "The network doesn't have CF condition " + i2);
                    return false;
                }
            }
        } else if (!this.mCFCache.isExist(i)) {
            String str2 = LOG_TAG;
            int i4 = this.mPhoneId;
            IMSLog.e(str2, i4, "The network doesn't have CF condition " + this.mProfile.condition);
            return false;
        }
        return true;
    }

    public boolean hasConditionOnCbCache() {
        UtProfile utProfile = this.mProfile;
        if (utProfile.type == 105) {
            if (this.mOCBCache.isExist(utProfile.condition)) {
                return true;
            }
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.e(str, i, "The network doesn't have OCB condition " + this.mProfile.condition);
            return false;
        } else if (this.mICBCache.isExist(utProfile.condition)) {
            return true;
        } else {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.e(str2, i2, "The network doesn't have ICB condition " + this.mProfile.condition);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void processTerminalRequest() {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "process terminal request " + UtLog.getStringRequestType(this.mProfile.type));
        UtProfile utProfile = this.mProfile;
        int i = utProfile.type;
        if (i != 114) {
            if (i != 115) {
                boolean z = true;
                switch (i) {
                    case 102:
                    case 104:
                        int userSetToInt = getUserSetToInt(this.mPhoneId, "ss_volte_cb_pref", 0) & getUserSetToInt(this.mPhoneId, "ss_video_cb_pref", 0);
                        int convertCbTypeToBitMask = UtUtils.convertCbTypeToBitMask(this.mProfile.condition);
                        Bundle[] bundleArr = new Bundle[1];
                        if ((userSetToInt & convertCbTypeToBitMask) != convertCbTypeToBitMask) {
                            z = false;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putInt("status", z ? 1 : 0);
                        bundle.putInt(UtConstant.CONDITION, this.mProfile.condition);
                        bundle.putInt(UtConstant.SERVICECLASS, this.mProfile.serviceClass);
                        bundleArr[0] = bundle;
                        IMSLog.i(str, this.mPhoneId, "terminal CallBarring " + this.mProfile.condition + " " + z);
                        completeUtRequest(bundleArr);
                        return;
                    case 103:
                    case 105:
                        int convertCbTypeToBitMask2 = UtUtils.convertCbTypeToBitMask(utProfile.condition);
                        if (this.mProfile.action != 1) {
                            z = false;
                        }
                        setCbUserConfig(MEDIA.AUDIO, z, convertCbTypeToBitMask2);
                        setCbUserConfig(MEDIA.VIDEO, z, convertCbTypeToBitMask2);
                        completeUtRequest();
                        return;
                    case 106:
                        int userSetToInt2 = getUserSetToInt(this.mPhoneId, "ss_clip_pref", 1);
                        ImsSsInfo build = new ImsSsInfo.Builder(userSetToInt2).setIncomingCommunicationBarringNumber("").build();
                        Bundle bundle2 = new Bundle();
                        bundle2.putParcelable(UtConstant.IMSSSINFO, build);
                        IMSLog.i(str, this.mPhoneId, "terminal CLIP = " + userSetToInt2);
                        completeUtRequest(bundle2);
                        return;
                    case 107:
                        setUserSet(this.mPhoneId, "ss_clip_pref", utProfile.enable ? 1 : 0);
                        completeUtRequest();
                        return;
                    case 108:
                        int[] iArr = {getUserSetToInt(this.mPhoneId, "ss_clir_pref", 0), 4};
                        Bundle bundle3 = new Bundle();
                        bundle3.putIntArray(UtConstant.QUERYCLIR, iArr);
                        IMSLog.i(str, this.mPhoneId, "terminal CLIR = " + iArr[0]);
                        completeUtRequest(bundle3);
                        return;
                    case 109:
                        setUserSet(this.mPhoneId, "ss_clir_pref", utProfile.condition);
                        completeUtRequest();
                        return;
                    default:
                        IMSLog.i(str, this.mPhoneId, "no matched type " + this.mProfile.type);
                        Bundle bundle4 = new Bundle();
                        bundle4.putInt("errorCode", 0);
                        failUtRequest(bundle4);
                        return;
                }
            } else {
                ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
                if ((simManagerFromSimSlot == null ? Mno.DEFAULT : simManagerFromSimSlot.getSimMno()) != Mno.TELSTRA || !this.needPdnRequestForCW) {
                    setUserSet(this.mPhoneId, "enable_call_wait", this.mProfile.enable);
                    completeUtRequest();
                    return;
                }
                IMSLog.i(str, this.mPhoneId, "Telstra needs to connect xcap pdn for call waiting to check non VoLTE SIM.");
                sendMessage(100);
            }
        } else if (SimUtil.getSimMno(this.mPhoneId) != Mno.TELSTRA || !this.needPdnRequestForCW) {
            boolean userSetToBoolean = getUserSetToBoolean(this.mPhoneId, "enable_call_wait");
            IMSLog.i(str, this.mPhoneId, "terminal CallWaiting " + userSetToBoolean);
            completeUtRequest(userSetToBoolean);
        } else {
            sendMessage(100);
        }
    }

    private void setCbUserConfig(MEDIA media, boolean z, int i) {
        String str;
        int i2 = 0;
        if (media == MEDIA.AUDIO) {
            str = "ss_volte_cb_pref";
            i2 = getUserSetToInt(this.mPhoneId, str, 0);
        } else if (media == MEDIA.VIDEO) {
            str = "ss_video_cb_pref";
            i2 = getUserSetToInt(this.mPhoneId, str, 0);
        } else {
            str = null;
        }
        setUserSet(this.mPhoneId, str, z ? i2 | i : (~i) & i2);
    }

    /* access modifiers changed from: protected */
    public void updateConfig(UtConfigData utConfigData) {
        this.mConfig = utConfigData;
        this.mFeature = UtFeatureData.getBuilder().setPhoneId(this.mPhoneId).build();
        if (SimUtil.getSimMno(this.mPhoneId) == Mno.GCF && "CHM".equalsIgnoreCase(OmcCode.get())) {
            this.mFeature.setTurnOffGcfCondition();
        }
        UtServiceModule utServiceModule = this.mUtServiceModule;
        int i = this.mPhoneId;
        utServiceModule.writeDump(i, "mConfig = " + this.mConfig.toString() + " mFeature = " + this.mFeature.toString() + " ssDomain = " + UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.DOMAIN, "CS") + " ussdDomain = " + UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.Call.USSD_DOMAIN, "CS"));
        this.needPdnRequestForCW = true;
        this.mIsGetForAllCb = false;
        this.mIsGetSdBy404 = false;
        this.isRetryingCreatePdn = false;
        this.isGetBeforePut = false;
        clearCachedSsData(-1);
        setForce403Error(false);
        removeMessages(14);
    }

    /* access modifiers changed from: protected */
    public UtConfigData getConfig() {
        return this.mConfig;
    }

    public void clearCachedSsData(int i) {
        if (i == 101) {
            this.mCFCache = null;
            this.mHasCFCache = false;
        } else if (i == 103) {
            this.mICBCache = null;
            this.mHasICBCache = false;
        } else if (i != 105) {
            this.mCFCache = null;
            this.mICBCache = null;
            this.mOCBCache = null;
            this.mHasICBCache = false;
            this.mHasOCBCache = false;
            this.mHasCFCache = false;
        } else {
            this.mOCBCache = null;
            this.mHasOCBCache = false;
        }
    }

    /* access modifiers changed from: protected */
    public void onAirplaneModeChanged(int i) {
        if (i == 1) {
            if (SimUtil.getSimMno(this.mPhoneId).isChn()) {
                setForce403Error(false);
            }
            removeMessages(2);
            transitionTo(this.mRequestState);
            sendMessage(2);
        }
    }

    /* access modifiers changed from: protected */
    public ImsUri.UriType getPreferredUriType() {
        if ("TEL".equalsIgnoreCase(this.mFeature.cfUriType)) {
            return ImsUri.UriType.TEL_URI;
        }
        return ImsUri.UriType.SIP_URI;
    }

    private boolean isTelUriUsePhoneContext(Mno mno) {
        return mno.isOneOf(Mno.VODAFONE_UK, Mno.SFR, Mno.SOFTBANK, Mno.TELSTRA, Mno.ETISALAT_UAE);
    }

    private boolean isGcfTelUri(Mno mno) {
        String str = OmcCode.get();
        if (mno == Mno.GCF) {
            return "CHM".equalsIgnoreCase(str) || "CBK".equalsIgnoreCase(str) || "CHC".equalsIgnoreCase(str);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String getNetworkPreferredUri(String str) {
        ImsUri imsUri;
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        ImsUri.UriType preferredUriType = getPreferredUriType();
        String domain = UtUtils.getDomain(this.mUtServiceModule.getPublicId(this.mPhoneId));
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        if (isGcfTelUri(simMno)) {
            preferredUriType = ImsUri.UriType.TEL_URI;
        }
        String replaceAll = str.replaceAll("\\p{Z}|\\p{Space}", "");
        if (replaceAll.charAt(0) != '+') {
            replaceAll = makeInternationalFormat(simMno, preferredUriType, replaceAll, domain);
        }
        if (domain == null || preferredUriType != ImsUri.UriType.SIP_URI) {
            imsUri = ImsUri.parse("tel:" + replaceAll);
        } else {
            imsUri = ImsUri.parse("sip:" + replaceAll + "@" + domain);
            imsUri.setUserParam(PhoneConstants.PHONE_KEY);
        }
        return imsUri.toString();
    }

    private int getPdnType() {
        UtConfigData utConfigData = this.mConfig;
        String str = utConfigData != null ? utConfigData.apnSelection : "xcap";
        if ("cbs".equalsIgnoreCase(str)) {
            return 12;
        }
        if ("default".equalsIgnoreCase(str)) {
            return 0;
        }
        return "wifi".equalsIgnoreCase(str) ? 1 : 27;
    }

    private String makeInternationalFormat(Mno mno, ImsUri.UriType uriType, String str, String str2) {
        String generate3GPPDomain = UtUtils.generate3GPPDomain(SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId));
        if (this.mFeature.isNeedInternationalNumber) {
            return UtUtils.makeInternationalNumber(str, mno);
        }
        if (mno == Mno.EASTLINK) {
            if (str.length() == 11) {
                return "+" + str;
            } else if (str.length() != 10) {
                return str;
            } else {
                return "+1" + str;
            }
        } else if (str2 == null || uriType != ImsUri.UriType.SIP_URI) {
            if (mno == Mno.DTAC) {
                if (this.mProfile.action == 0) {
                    return UtUtils.makeInternationalNumber(str, mno);
                }
                return str + ";phone-context=ims.mnc005.mcc520.3gppnetwork.org";
            } else if (mno == Mno.SINGTEL) {
                return str + ";phone-context=+65";
            } else if (mno == Mno.SMARTONE) {
                return str + ";phone-context=+852";
            } else if (str2 != null && isTelUriUsePhoneContext(mno)) {
                return str + ";phone-context=" + str2;
            } else if ((mno != Mno.CTC && mno != Mno.CTCMO && mno != Mno.ETISALAT_UAE) || generate3GPPDomain == null) {
                return str;
            } else {
                return str + ";phone-context=" + generate3GPPDomain;
            }
        } else if (mno.isTmobile() || mno == Mno.TELEKOM_ALBANIA) {
            if (generate3GPPDomain != null) {
                return str + ";phone-context=" + generate3GPPDomain;
            }
            return str + ";phone-context=" + str2;
        } else if (mno == Mno.TELENOR_SWE) {
            return str;
        } else {
            return str + ";phone-context=" + str2;
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasConnection() {
        if (this.mPdnType == -1) {
            this.mPdnType = getPdnType();
        }
        return this.mPdnController.isConnected(this.mPdnType, this.mPdnListener);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004a, code lost:
        if (r8.mPhoneId == r4) goto L_0x004d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendDisconnectPdnWithDelay() {
        /*
            r8 = this;
            r0 = 2
            r8.removeMessages(r0)
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r8.mProfile
            int r1 = r1.type
            r2 = 116(0x74, float:1.63E-43)
            r3 = 0
            if (r1 != r2) goto L_0x000f
            r1 = r3
            goto L_0x0053
        L_0x000f:
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r1 = r8.mFeature
            r2 = 5000(0x1388, float:7.006E-42)
            if (r1 == 0) goto L_0x0051
            int r1 = r1.delay_disconnect_pdn
            if (r1 <= r2) goto L_0x004d
            java.util.List r4 = com.sec.internal.ims.core.sim.SimManagerFactory.getAllSimManagers()
            java.util.Iterator r4 = r4.iterator()
        L_0x0021:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x0042
            java.lang.Object r5 = r4.next()
            com.sec.internal.interfaces.ims.core.ISimManager r5 = (com.sec.internal.interfaces.ims.core.ISimManager) r5
            boolean r6 = r5.isSimAvailable()
            if (r6 != 0) goto L_0x003f
            int r5 = r5.getSimState()
            if (r5 != 0) goto L_0x0021
            boolean r5 = com.sec.internal.helper.SimUtil.isDualIMS()
            if (r5 == 0) goto L_0x0021
        L_0x003f:
            int r3 = r3 + 1
            goto L_0x0021
        L_0x0042:
            int r4 = com.sec.internal.helper.SimUtil.getActiveDataPhoneId()
            if (r3 >= r0) goto L_0x0051
            int r5 = r8.mPhoneId
            if (r5 == r4) goto L_0x004d
            goto L_0x0051
        L_0x004d:
            r7 = r3
            r3 = r1
            r1 = r7
            goto L_0x0053
        L_0x0051:
            r1 = r3
            r3 = r2
        L_0x0053:
            java.lang.String r2 = LOG_TAG
            int r4 = r8.mPhoneId
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "sendDisconnectPdnWithDelay: "
            r5.append(r6)
            r5.append(r3)
            java.lang.String r6 = "ms, loadedSim : "
            r5.append(r6)
            r5.append(r1)
            java.lang.String r1 = r5.toString()
            com.sec.internal.log.IMSLog.i(r2, r4, r1)
            long r1 = (long) r3
            r8.sendMessageDelayed((int) r0, (long) r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.sendDisconnectPdnWithDelay():void");
    }

    public void handlePdnFail(PreciseDataConnectionState preciseDataConnectionState) {
        Message message;
        if (!this.mIsRequestFailed && this.mProfile != null) {
            int dataConnectionFailCause = preciseDataConnectionState.getDataConnectionFailCause();
            int apnSettingFromPdnType = getApnSettingFromPdnType(this.mPdnType);
            if (preciseDataConnectionState.getApnSetting() != null && (preciseDataConnectionState.getApnSetting().getApnTypeBitmask() & apnSettingFromPdnType) == apnSettingFromPdnType && !isRetryPdnFailCause(dataConnectionFailCause)) {
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "XCAP PDN setup failed. failCause = " + dataConnectionFailCause + ", mPdnRetryCounter : " + this.mPdnRetryCounter);
                Mno simMno = SimUtil.getSimMno(this.mPhoneId);
                if ((simMno == Mno.CHT || simMno == Mno.SINGTEL) && (dataConnectionFailCause == 55 || dataConnectionFailCause == 38)) {
                    IMSLog.e(str, this.mPhoneId, "MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED or NETWORK_FAILURE need retry.");
                    this.isRetryingCreatePdn = true;
                    removeMessages(2);
                    removeMessages(100);
                    sendMessageDelayed(obtainMessage(2), 1000);
                    sendMessageDelayed(obtainMessage(100), 1500);
                } else if (simMno == Mno.ETISALAT_UAE && dataConnectionFailCause == 38) {
                    IMSLog.i(str, "Etisalat isRetryFailCause: " + dataConnectionFailCause);
                } else {
                    if (simMno == Mno.VODAFONE_UK && dataConnectionFailCause == 27) {
                        IMSLog.e(str, this.mPhoneId, "Vodafone UK returns MISSING_UNKNOWN_APN for non VoLTE SIM.");
                        dataConnectionFailCause = 33;
                    }
                    UtServiceModule utServiceModule = this.mUtServiceModule;
                    int i2 = this.mPhoneId;
                    utServiceModule.writeDump(i2, "PDN failCause : " + dataConnectionFailCause);
                    IMSLog.c(LogClass.UT_PDN_FAILURE, this.mPhoneId + "," + dataConnectionFailCause);
                    this.needPdnRequestForCW = false;
                    if (getCurrentState() == this.mRequestState) {
                        removeMessages(2);
                        sendMessage(2);
                    }
                    if (this.mUtServiceModule.isTerminalRequest(this.mPhoneId, this.mProfile)) {
                        IMSLog.i(str, this.mPhoneId, "Terminal request, should ignore pdn failed event");
                        return;
                    }
                    if (needToCsfb(dataConnectionFailCause, simMno)) {
                        message = obtainMessage(12, 403);
                    } else {
                        IMSLog.e(str, this.mPhoneId, "Disconnect xcap pdn");
                        message = obtainMessage(12, dataConnectionFailCause + 10022);
                    }
                    sendMessage(message);
                }
            }
        }
    }

    private boolean needToCsfb(int i, Mno mno) {
        if (i == 33) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "needToCsfb :This SIM is not subscribed for xcap");
            return true;
        } else if ((mno != Mno.ORANGE && mno != Mno.ORANGE_POLAND && mno != Mno.ORANGE_SLOVAKIA) || i == 0 || i == 65540) {
            return false;
        } else {
            IMSLog.e(LOG_TAG, this.mPhoneId, "needToCsfb : xcap pdn rejected for orange group");
            return true;
        }
    }

    private boolean isRetryPdnFailCause(int i) {
        if (i != 0 && i != 14 && i != 65537) {
            return false;
        }
        String str = LOG_TAG;
        IMSLog.i(str, "isRetryFailCause: " + i);
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        if ((simMno == Mno.CTC || simMno == Mno.CU) && i == 0) {
            IMSLog.i(str, "pdnRetryCounter: " + this.mPdnRetryCounter);
            int i2 = this.mPdnRetryCounter;
            if (i2 > 1) {
                return false;
            }
            this.mPdnRetryCounter = i2 + 1;
        }
        return true;
    }

    public void handleEpdgAvailabilityChanged(boolean z) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleEpdgAvailabilityChanged: to " + z);
        if (!z && this.mProfile == null && hasConnection()) {
            removeMessages(2);
            sendMessage(2);
        }
    }

    /* access modifiers changed from: protected */
    public void disconnectPdn() {
        removeMessages(1);
        removeMessages(2);
        if (this.mPdnType != -1) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "disconnectPdn: mPdnType " + this.mPdnType);
            this.mPdnController.stopPdnConnectivity(this.mPdnType, this.mPhoneId, this.mPdnListener);
            sendMessage(3);
        }
    }

    public boolean isForbidden() {
        return this.mForce403Error;
    }

    public void setForce403Error(boolean z) {
        this.mForce403Error = z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x006e, code lost:
        r1 = r5.mProfile;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String updateCallforwardingInfo(com.sec.internal.constants.Mno r6) {
        /*
            r5 = this;
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r0 = r5.mFeature
            boolean r0 = r0.support_media
            if (r0 != 0) goto L_0x000d
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r5.mProfile
            r1 = 255(0xff, float:3.57E-43)
            r0.serviceClass = r1
            goto L_0x0022
        L_0x000d:
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.RJIL
            if (r6 == r0) goto L_0x0022
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r5.mProfile
            int r0 = r0.serviceClass
            com.sec.internal.ims.servicemodules.ss.MEDIA r0 = com.sec.internal.ims.servicemodules.ss.UtUtils.convertToMedia(r0)
            com.sec.internal.ims.servicemodules.ss.MEDIA r1 = com.sec.internal.ims.servicemodules.ss.MEDIA.ALL
            if (r0 != r1) goto L_0x0022
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r5.mProfile
            r1 = 1
            r0.serviceClass = r1
        L_0x0022:
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r5.mProfile
            int r1 = r0.action
            if (r1 != 0) goto L_0x0067
            java.lang.String r0 = r0.number
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 == 0) goto L_0x0067
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r5.mProfile
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r5.mPreviousCFCache
            int r2 = r0.condition
            int r3 = r0.serviceClass
            com.sec.internal.ims.servicemodules.ss.MEDIA r3 = com.sec.internal.ims.servicemodules.ss.UtUtils.convertToMedia(r3)
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r1 = r1.getRule((int) r2, (com.sec.internal.ims.servicemodules.ss.MEDIA) r3)
            com.sec.internal.ims.servicemodules.ss.ForwardTo r1 = r1.fwdElm
            java.lang.String r1 = r1.target
            r0.number = r1
            java.lang.String r0 = LOG_TAG
            int r1 = r5.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "previous activated number set "
            r2.append(r3)
            com.sec.internal.ims.servicemodules.ss.UtProfile r3 = r5.mProfile
            java.lang.String r3 = r3.number
            java.lang.String r3 = com.sec.internal.log.IMSLog.checker(r3)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
        L_0x0067:
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r0 = r5.mFeature
            boolean r1 = r0.isCFSingleElement
            r2 = 4
            if (r1 == 0) goto L_0x00c9
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r5.mProfile
            int r3 = r1.condition
            r4 = 5
            if (r3 == r4) goto L_0x00c9
            if (r3 != r2) goto L_0x0078
            goto L_0x00c9
        L_0x0078:
            r4 = 7
            if (r3 != r4) goto L_0x0088
            int r6 = r1.timeSeconds
            boolean r0 = r0.support_ss_namespace
            com.sec.internal.constants.ims.XmlElement r6 = com.sec.internal.ims.servicemodules.ss.UtUtils.makeNoReplyTimerXml(r6, r0)
            java.lang.String r6 = com.sec.internal.constants.ims.XmlCreator.toXcapXml(r6)
            goto L_0x00d9
        L_0x0088:
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.SINGTEL
            if (r6 == r0) goto L_0x0090
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.FET
            if (r6 != r0) goto L_0x00b2
        L_0x0090:
            boolean r0 = r5.mSeparatedCFNRY
            if (r0 != 0) goto L_0x00b2
            r0 = 2
            if (r3 != r0) goto L_0x00b2
            int r0 = r1.serviceClass
            com.sec.internal.ims.servicemodules.ss.MEDIA r0 = com.sec.internal.ims.servicemodules.ss.UtUtils.convertToMedia(r0)
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r0 = r5.getCallForwardRule(r3, r0)
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r1 = r5.mFeature
            boolean r1 = r1.support_ss_namespace
            com.sec.internal.ims.servicemodules.ss.UtProfile r3 = r5.mProfile
            int r3 = r3.timeSeconds
            com.sec.internal.constants.ims.XmlElement r6 = com.sec.internal.ims.servicemodules.ss.UtUtils.makeSingleXml(r0, r1, r6, r3)
            java.lang.String r6 = com.sec.internal.constants.ims.XmlCreator.toXcapXml(r6)
            goto L_0x00d9
        L_0x00b2:
            int r0 = r1.serviceClass
            com.sec.internal.ims.servicemodules.ss.MEDIA r0 = com.sec.internal.ims.servicemodules.ss.UtUtils.convertToMedia(r0)
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r0 = r5.getCallForwardRule(r3, r0)
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r1 = r5.mFeature
            boolean r1 = r1.support_ss_namespace
            com.sec.internal.constants.ims.XmlElement r6 = com.sec.internal.ims.servicemodules.ss.UtUtils.makeSingleXml((com.sec.internal.ims.servicemodules.ss.CallForwardingData.Rule) r0, (boolean) r1, (com.sec.internal.constants.Mno) r6)
            java.lang.String r6 = com.sec.internal.constants.ims.XmlCreator.toXcapXml(r6)
            goto L_0x00d9
        L_0x00c9:
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r0 = r5.getCfRuleSet()
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r1 = r5.mFeature
            boolean r1 = r1.support_ss_namespace
            com.sec.internal.constants.ims.XmlElement r6 = com.sec.internal.ims.servicemodules.ss.UtUtils.makeMultipleXml(r0, r6, r1)
            java.lang.String r6 = com.sec.internal.constants.ims.XmlCreator.toXcapXml(r6)
        L_0x00d9:
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r5.mProfile
            int r1 = r0.action
            if (r1 != r2) goto L_0x00f0
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r5 = r5.mPreviousCFCache
            int r1 = r0.condition
            int r0 = r0.serviceClass
            com.sec.internal.ims.servicemodules.ss.MEDIA r0 = com.sec.internal.ims.servicemodules.ss.UtUtils.convertToMedia(r0)
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r5 = r5.getRule((int) r1, (com.sec.internal.ims.servicemodules.ss.MEDIA) r0)
            r5.clear()
        L_0x00f0:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.updateCallforwardingInfo(com.sec.internal.constants.Mno):java.lang.String");
    }

    /* access modifiers changed from: protected */
    public String updateUtDetailInfo() {
        String str;
        int i;
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        UtProfile utProfile = this.mProfile;
        int i2 = utProfile.type;
        if (i2 != 101) {
            boolean z = true;
            if (i2 == 103 || i2 == 105) {
                UtFeatureData utFeatureData = this.mFeature;
                if (!utFeatureData.support_media || utFeatureData.noMediaForCB) {
                    utProfile.serviceClass = 255;
                } else if (simMno != Mno.RJIL && UtUtils.convertToMedia(utProfile.serviceClass) == MEDIA.ALL) {
                    this.mProfile.serviceClass = 1;
                }
                if (simMno == Mno.VODAFONE_AUSTRALIA) {
                    UtProfile utProfile2 = this.mProfile;
                    if (utProfile2.serviceClass == 8) {
                        utProfile2.serviceClass = 1;
                    }
                }
                if (simMno.isOneOf(Mno.HK3, Mno.FET) && ((i = this.mProfile.condition) == 8 || i == 9)) {
                    UtLog.i(LOG_TAG, this.mPhoneId, "3HK & FET mo mt");
                    str = XmlCreator.toXcapXml(UtUtils.makeMultipleXml(getCbRuleSetForAll(this.mProfile.type, simMno), this.mProfile.type, simMno, this.mFeature.support_ss_namespace));
                } else if (!this.mFeature.isCBSingleElement) {
                    str = XmlCreator.toXcapXml(UtUtils.makeMultipleXml(getCbRuleSet(this.mProfile.type), this.mProfile.type, simMno, this.mFeature.support_ss_namespace));
                } else {
                    UtProfile utProfile3 = this.mProfile;
                    str = XmlCreator.toXcapXml(UtUtils.makeSingleXml(getCallBarringRule(utProfile3.type, UtUtils.convertToMedia(utProfile3.serviceClass)), simMno, this.mFeature.support_ss_namespace));
                }
            } else if (i2 == 107) {
                str = XmlCreator.toXcapXml(UtUtils.makeSingleXml(UtElement.ELEMENT_OIP, utProfile.enable, this.mFeature.support_ss_namespace));
            } else if (i2 != 109) {
                str = i2 != 115 ? "" : XmlCreator.toXcapXml(UtUtils.makeSingleXml("communication-waiting", utProfile.enable, this.mFeature.support_ss_namespace));
            } else if (simMno == Mno.VINAPHONE) {
                if (utProfile.condition != 1) {
                    z = false;
                }
                str = XmlCreator.toXcapXml(UtUtils.makeSingleXml(UtElement.ELEMENT_OIR, z, this.mFeature.support_ss_namespace));
            } else {
                str = XmlCreator.toXcapXml(UtUtils.makeSingleXml(UtElement.ELEMENT_OIR, utProfile.condition, this.mFeature.support_ss_namespace));
            }
        } else {
            str = updateCallforwardingInfo(simMno);
        }
        String str2 = LOG_TAG;
        int i3 = this.mPhoneId;
        UtLog.i(str2, i3, "Print PUT Body : " + IMSLog.numberChecker(str));
        return str;
    }

    private boolean isSupportfwd(Mno mno) {
        return (mno == Mno.KOODO || mno == Mno.VIVACOM_BULGARIA || mno == Mno.WIND_GREECE || mno == Mno.CLARO_DOMINICAN || mno == Mno.TELUS) ? false : true;
    }

    /* access modifiers changed from: protected */
    public CallForwardingData getCfRuleSet() {
        int i;
        int i2;
        int i3;
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        CallForwardingData callForwardingData = this.mCFCache;
        if (callForwardingData == null || (i2 = this.mProfile.condition) == 5 || i2 == 4) {
            CallForwardingData callForwardingData2 = new CallForwardingData();
            makeRuleSet(simMno, callForwardingData2);
            if (simMno == Mno.GCF) {
                CallForwardingData callForwardingData3 = this.mCFCache;
                if (callForwardingData3 != null) {
                    callForwardingData3.replyTimer = 0;
                }
            } else {
                UtProfile utProfile = this.mProfile;
                if (utProfile.condition == 2 && (i = utProfile.timeSeconds) > 0) {
                    callForwardingData2.replyTimer = i;
                }
            }
            this.mCFCache = callForwardingData2;
            return callForwardingData2;
        }
        CallForwardingData clone = callForwardingData.clone();
        Iterator<SsRuleData.SsRule> it = clone.rules.iterator();
        boolean z = false;
        while (it.hasNext()) {
            CallForwardingData.Rule rule = (CallForwardingData.Rule) it.next();
            List<ForwardElm> list = rule.fwdElm.fwdElm;
            if (list != null && list.size() > 0 && isSupportfwd(simMno)) {
                rule.fwdElm.fwdElm.clear();
            }
            Condition condition = rule.conditions;
            int i4 = condition.condition;
            UtProfile utProfile2 = this.mProfile;
            if (i4 == utProfile2.condition && (condition.media.contains(UtUtils.convertToMedia(utProfile2.serviceClass)) || (!this.mFeature.supportAlternativeMediaForCf ? simMno.isOneOf(Mno.BELL, Mno.CSL, Mno.PCCW) : rule.conditions.media.contains(MEDIA.ALL)))) {
                UtProfile utProfile3 = this.mProfile;
                int i5 = utProfile3.action;
                if (i5 == 3) {
                    rule.conditions.state = true;
                    rule.fwdElm.target = utProfile3.number;
                } else if (i5 == 1) {
                    rule.conditions.state = true;
                    if (!TextUtils.isEmpty(utProfile3.number)) {
                        rule.fwdElm.target = this.mProfile.number;
                    }
                } else {
                    rule.conditions.state = false;
                    if (i5 == 4) {
                        rule.fwdElm.target = "";
                    }
                }
                rule.conditions.action = this.mProfile.action;
                if (!TextUtils.isEmpty(rule.fwdElm.target) && !rule.fwdElm.target.startsWith("sip:") && !rule.fwdElm.target.startsWith("tel:") && !rule.fwdElm.target.startsWith("voicemail:")) {
                    ForwardTo forwardTo = rule.fwdElm;
                    forwardTo.target = getNetworkPreferredUri(forwardTo.target);
                }
                if (this.mSeparatedCFNL) {
                    this.mCFCache.replaceRule(rule);
                }
                z = true;
            }
        }
        if (this.mProfile.condition == 0 && z) {
            if (simMno == Mno.BELL) {
                return clone;
            }
            if (simMno.isOneOf(Mno.CSL, Mno.PCCW)) {
                clone.replyTimer = 0;
                return clone;
            }
        }
        UtProfile utProfile4 = this.mProfile;
        if (!clone.isExist(utProfile4.condition, UtUtils.convertToMedia(utProfile4.serviceClass))) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "CF rule is not present. Make new rule.");
            UtProfile utProfile5 = this.mProfile;
            CallForwardingData.Rule makeCFRule = makeCFRule(utProfile5.condition, utProfile5.serviceClass, utProfile5.action, utProfile5.number);
            clone.setRule(makeCFRule);
            if (this.mSeparatedCFNL) {
                this.mCFCache.replaceRule(makeCFRule);
            }
        }
        if (simMno == Mno.GCF) {
            clone.replyTimer = 0;
        } else {
            UtProfile utProfile6 = this.mProfile;
            if (utProfile6.condition == 2 && (i3 = utProfile6.timeSeconds) > 0) {
                clone.replyTimer = i3;
            } else if (simMno.isOneOf(Mno.CSL, Mno.PCCW)) {
                clone.replyTimer = 0;
            }
        }
        return clone;
    }

    private void makeRuleSet(Mno mno, CallForwardingData callForwardingData) {
        int i;
        CallForwardingData callForwardingData2;
        boolean z;
        CallForwardingData callForwardingData3;
        boolean z2;
        UtProfile utProfile = this.mProfile;
        int i2 = utProfile.condition;
        if (i2 == 5 || i2 == 4) {
            for (int i3 = 0; i3 <= 3; i3++) {
                boolean z3 = true;
                if (this.mProfile.condition == 5 && i3 == 0) {
                    if (mno.isOneOf(Mno.CMHK, Mno.HK3, Mno.ASIACELL_IRAQ) && (callForwardingData3 = this.mCFCache) != null) {
                        MEDIA media = MEDIA.AUDIO;
                        if (callForwardingData3.isExist(i3, media)) {
                            callForwardingData.rules.add(this.mCFCache.getRule(i3, media));
                            z2 = true;
                        } else {
                            z2 = false;
                        }
                        CallForwardingData callForwardingData4 = this.mCFCache;
                        MEDIA media2 = MEDIA.VIDEO;
                        if (callForwardingData4.isExist(i3, media2)) {
                            callForwardingData.rules.add(this.mCFCache.getRule(i3, media2));
                        } else {
                            z3 = z2;
                        }
                        if (!z3 && this.mCFCache.isExist(i3, UtUtils.convertToMedia(this.mProfile.serviceClass))) {
                            callForwardingData.rules.add(this.mCFCache.getRule(i3, UtUtils.convertToMedia(this.mProfile.serviceClass)));
                        }
                    }
                } else if (!mno.isOneOf(Mno.CSL, Mno.PCCW, Mno.CMHK, Mno.HK3, Mno.ASIACELL_IRAQ) || (callForwardingData2 = this.mCFCache) == null) {
                    callForwardingData.rules.add(getCallForwardRule(i3, UtUtils.convertToMedia(this.mProfile.serviceClass)));
                } else {
                    MEDIA media3 = MEDIA.AUDIO;
                    if (callForwardingData2.isExist(i3, media3)) {
                        callForwardingData.rules.add(getCallForwardRule(i3, media3));
                        z = true;
                    } else {
                        z = false;
                    }
                    CallForwardingData callForwardingData5 = this.mCFCache;
                    MEDIA media4 = MEDIA.VIDEO;
                    if (!callForwardingData5.isExist(i3, media4)) {
                        z3 = z;
                    } else if (mno == Mno.CMHK) {
                        callForwardingData.rules.add(this.mCFCache.getRule(i3, media4));
                    } else {
                        callForwardingData.rules.add(getCallForwardRule(i3, media4));
                    }
                    if (!z3) {
                        callForwardingData.rules.add(getCallForwardRule(i3, UtUtils.convertToMedia(this.mProfile.serviceClass)));
                    }
                }
            }
            if (mno == Mno.ATT) {
                callForwardingData.rules.add(getCallForwardRule(6, MEDIA.ALL));
            }
            if (mno == Mno.ZAIN_BAHRAIN || mno.isCanada()) {
                CallForwardingData callForwardingData6 = this.mCFCache;
                if (callForwardingData6 != null) {
                    callForwardingData6.replyTimer = 0;
                }
                callForwardingData.replyTimer = 0;
            }
            int i4 = this.mProfile.timeSeconds;
            if (i4 > 0) {
                callForwardingData.replyTimer = i4;
            } else {
                CallForwardingData callForwardingData7 = this.mCFCache;
                if (callForwardingData7 != null && (i = callForwardingData7.replyTimer) > 0) {
                    callForwardingData.replyTimer = i;
                }
            }
            if (mno.isOneOf(Mno.CSL, Mno.PCCW)) {
                CallForwardingData callForwardingData8 = this.mCFCache;
                if (callForwardingData8 != null) {
                    callForwardingData8.replyTimer = 0;
                }
                callForwardingData.replyTimer = 0;
                return;
            }
            return;
        }
        callForwardingData.setRule(getCallForwardRule(i2, UtUtils.convertToMedia(utProfile.serviceClass)));
    }

    /* access modifiers changed from: protected */
    public ArrayList<CallBarringData.Rule> parseSIBtarget(String[] strArr) {
        ArrayList<CallBarringData.Rule> arrayList = new ArrayList<>();
        if (strArr == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Empty password");
            return arrayList;
        }
        for (String split : strArr) {
            CallBarringData.Rule rule = new CallBarringData.Rule();
            String[] split2 = split.split(",");
            rule.ruleId = split2[0];
            rule.conditions.condition = 10;
            int i = 1;
            rule.target.add(UtUtils.cleanBarringNum(split2[1]));
            rule.allow = false;
            rule.conditions.state = split2[2].equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE);
            Condition condition = rule.conditions;
            if (!condition.state) {
                i = 3;
            }
            condition.action = i;
            arrayList.add(rule);
        }
        return arrayList;
    }

    private CallBarringData addKddiCbRules(CallBarringData callBarringData) {
        if (callBarringData == null) {
            callBarringData = new CallBarringData();
        }
        UtProfile utProfile = this.mProfile;
        int i = utProfile.condition;
        if (i == 10) {
            CallBarringData callBarringData2 = new CallBarringData();
            Iterator<CallBarringData.Rule> it = parseSIBtarget(this.mProfile.valueList).iterator();
            while (it.hasNext()) {
                CallBarringData.Rule next = it.next();
                callBarringData2.rules.add(next);
                String str = LOG_TAG;
                IMSLog.d(str, "KDDI_UT added rule id = " + next.ruleId + " conditions = " + next.conditions + " media = " + next.conditions.media);
            }
            if (callBarringData.isExist(6)) {
                callBarringData2.rules.add(callBarringData.getRule(6, MEDIA.ALL));
            }
            return callBarringData2;
        }
        if (i == 6) {
            CallBarringData.Rule makeCBRule = makeCBRule(i, utProfile.serviceClass, utProfile.action);
            makeCBRule.ruleId = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.ICB_ANONYMOUS_RULEID, "");
            callBarringData.setRule(makeCBRule);
            String str2 = LOG_TAG;
            IMSLog.d(str2, "KDDI_UT added rule id = " + makeCBRule.ruleId + " conditions = " + makeCBRule.conditions + " media = " + makeCBRule.conditions.media);
        }
        return callBarringData;
    }

    /* access modifiers changed from: protected */
    public CallBarringData getCbRuleSet(int i) {
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        CallBarringData callBarringData = this.mICBCache;
        if (i == 105) {
            callBarringData = this.mOCBCache;
        }
        if (simMno == Mno.KDDI) {
            return addKddiCbRules(callBarringData);
        }
        if (callBarringData != null) {
            CallBarringData clone = callBarringData.clone();
            boolean z = false;
            for (SsRuleData.SsRule next : clone.rules) {
                if (simMno.isOneOf(Mno.ELISA_FINLAND, Mno.TELEFONICA_CZ, Mno.VODAFONE_NEWZEALAND, Mno.CU)) {
                    Condition condition = next.conditions;
                    if (condition.condition == this.mProfile.condition) {
                        condition.media.clear();
                        next.conditions.media.add(UtUtils.convertToMedia(this.mProfile.serviceClass));
                    }
                }
                Condition condition2 = next.conditions;
                int i2 = condition2.condition;
                UtProfile utProfile = this.mProfile;
                if (i2 == utProfile.condition && (condition2.media.contains(UtUtils.convertToMedia(utProfile.serviceClass)) || (this.mFeature.supportAlternativeMediaForCb && next.conditions.media.contains(MEDIA.ALL)))) {
                    Condition condition3 = next.conditions;
                    int i3 = this.mProfile.action;
                    condition3.state = i3 == 1 || i3 == 3;
                    condition3.action = i3;
                    z = true;
                }
            }
            if (!z) {
                UtProfile utProfile2 = this.mProfile;
                clone.setRule(makeCBRule(utProfile2.condition, utProfile2.serviceClass, utProfile2.action));
            }
            return clone;
        }
        CallBarringData callBarringData2 = new CallBarringData();
        callBarringData2.setRule(getCallBarringRule(i, UtUtils.convertToMedia(this.mProfile.serviceClass)));
        return callBarringData2;
    }

    /* access modifiers changed from: protected */
    public CallBarringData getCbRuleSetForAll(int i, Mno mno) {
        String str = LOG_TAG;
        UtLog.i(str, this.mPhoneId, "getCbRuleSetForAll");
        CallBarringData callBarringData = this.mICBCache;
        if (i == 105) {
            callBarringData = this.mOCBCache;
        }
        if (callBarringData != null) {
            UtLog.i(str, this.mPhoneId, "CBCache not null");
            CallBarringData clone = callBarringData.clone();
            for (SsRuleData.SsRule next : clone.rules) {
                if (mno == Mno.FET || next.conditions.media.contains(MEDIA.AUDIO) || next.conditions.media.contains(MEDIA.VIDEO)) {
                    Condition condition = next.conditions;
                    int i2 = this.mProfile.action;
                    boolean z = true;
                    if (!(i2 == 1 || i2 == 3)) {
                        z = false;
                    }
                    condition.state = z;
                    condition.action = i2;
                }
            }
            return clone;
        }
        CallBarringData callBarringData2 = new CallBarringData();
        callBarringData2.setRule(getCallBarringRule(i, UtUtils.convertToMedia(this.mProfile.serviceClass)));
        return callBarringData2;
    }

    /* access modifiers changed from: protected */
    public CallBarringData.Rule getCallBarringRule(int i, MEDIA media) {
        MEDIA matchedMediaForCB;
        CallBarringData callBarringData = this.mICBCache;
        if (i == 105) {
            callBarringData = this.mOCBCache;
        }
        boolean z = false;
        if (callBarringData == null || (matchedMediaForCB = getMatchedMediaForCB(callBarringData, media)) == null) {
            CallBarringData.Rule rule = new CallBarringData.Rule();
            rule.allow = false;
            rule.ruleId = getCbRuleId();
            Condition condition = new Condition();
            rule.conditions = condition;
            UtProfile utProfile = this.mProfile;
            condition.condition = utProfile.condition;
            int i2 = utProfile.action;
            if (i2 == 3 || i2 == 1) {
                z = true;
            }
            condition.state = z;
            condition.action = i2;
            if (callBarringData == null) {
                callBarringData = new CallBarringData();
            }
            rule.conditions.media = new ArrayList();
            rule.conditions.media.add(media);
            callBarringData.setRule(rule);
            return rule;
        }
        CallBarringData.Rule rule2 = callBarringData.getRule(this.mProfile.condition, matchedMediaForCB);
        if (rule2.conditions.media.contains(matchedMediaForCB)) {
            Condition condition2 = rule2.conditions;
            int i3 = this.mProfile.action;
            if (i3 == 3 || i3 == 1) {
                z = true;
            }
            condition2.state = z;
            condition2.action = i3;
        }
        return rule2;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x010b  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0143  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.ss.CallForwardingData.Rule getCallForwardRule(int r9, com.sec.internal.ims.servicemodules.ss.MEDIA r10) {
        /*
            r8 = this;
            int r0 = r8.mPhoneId
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            r2 = 1
            r3 = 0
            if (r1 == 0) goto L_0x004e
            boolean r1 = r1.isExist(r9, r10)
            if (r1 != 0) goto L_0x004c
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r1 = r8.mFeature
            boolean r1 = r1.supportAlternativeMediaForCf
            if (r1 != 0) goto L_0x0042
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CMCC
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.H3G_SE
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[]{r1, r4}
            boolean r1 = r0.isOneOf(r1)
            if (r1 == 0) goto L_0x0027
            goto L_0x0042
        L_0x0027:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CU
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.CTC
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.CSL
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.PCCW
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[]{r1, r4, r5, r6}
            boolean r1 = r0.isOneOf(r1)
            if (r1 == 0) goto L_0x004e
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            boolean r1 = r1.isExist(r9)
            if (r1 == 0) goto L_0x004e
            goto L_0x004c
        L_0x0042:
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r4 = com.sec.internal.ims.servicemodules.ss.MEDIA.ALL
            boolean r1 = r1.isExist(r9, r4)
            if (r1 == 0) goto L_0x004e
        L_0x004c:
            r1 = r2
            goto L_0x004f
        L_0x004e:
            r1 = r3
        L_0x004f:
            r4 = 4
            if (r1 == 0) goto L_0x01a1
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r1 = r8.mFeature
            boolean r1 = r1.supportAlternativeMediaForCf
            if (r1 == 0) goto L_0x0069
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r5 = com.sec.internal.ims.servicemodules.ss.MEDIA.ALL
            boolean r1 = r1.isExist(r9, r5)
            if (r1 == 0) goto L_0x0069
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r10 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r9 = r10.getRule((int) r9, (com.sec.internal.ims.servicemodules.ss.MEDIA) r5)
            goto L_0x00d7
        L_0x0069:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CMCC
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.H3G_SE
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[]{r1, r5}
            boolean r1 = r0.isOneOf(r1)
            if (r1 == 0) goto L_0x0098
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            boolean r1 = r1.isExist(r9, r10)
            if (r1 != 0) goto L_0x0098
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r10 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r1 = com.sec.internal.ims.servicemodules.ss.MEDIA.ALL
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r9 = r10.getRule((int) r9, (com.sec.internal.ims.servicemodules.ss.MEDIA) r1)
            com.sec.internal.ims.servicemodules.ss.Condition r10 = r9.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r10 = r10.media
            r10.remove(r1)
            com.sec.internal.ims.servicemodules.ss.Condition r10 = r9.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r10 = r10.media
            com.sec.internal.ims.servicemodules.ss.MEDIA r1 = com.sec.internal.ims.servicemodules.ss.MEDIA.AUDIO
            r10.add(r1)
            goto L_0x00d7
        L_0x0098:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CU
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.CTC
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.CSL
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.PCCW
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[]{r1, r5, r6, r7}
            boolean r1 = r0.isOneOf(r1)
            if (r1 == 0) goto L_0x00d1
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            boolean r1 = r1.isExist(r9, r10)
            if (r1 != 0) goto L_0x00d1
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r5 = com.sec.internal.ims.servicemodules.ss.MEDIA.AUDIO
            boolean r1 = r1.isExist(r9, r5)
            if (r1 == 0) goto L_0x00d1
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r9 = r1.getRule((int) r9, (com.sec.internal.ims.servicemodules.ss.MEDIA) r5)
            com.sec.internal.ims.servicemodules.ss.Condition r1 = r9.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r1 = r1.media
            r1.remove(r5)
            com.sec.internal.ims.servicemodules.ss.Condition r1 = r9.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r1 = r1.media
            r1.add(r10)
            goto L_0x00d7
        L_0x00d1:
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r9 = r1.getRule((int) r9, (com.sec.internal.ims.servicemodules.ss.MEDIA) r10)
        L_0x00d7:
            com.sec.internal.ims.servicemodules.ss.UtProfile r10 = r8.mProfile
            java.lang.String r10 = r10.number
            boolean r10 = android.text.TextUtils.isEmpty(r10)
            if (r10 != 0) goto L_0x010b
            com.sec.internal.ims.servicemodules.ss.UtProfile r10 = r8.mProfile
            int r1 = r10.action
            if (r1 != 0) goto L_0x00f5
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.WIND_GREECE
            if (r0 != r1) goto L_0x00f5
            java.lang.String r10 = LOG_TAG
            int r1 = r8.mPhoneId
            java.lang.String r4 = "number change prevented for deactivation"
            com.sec.internal.log.IMSLog.i(r10, r1, r4)
            goto L_0x00fb
        L_0x00f5:
            com.sec.internal.ims.servicemodules.ss.ForwardTo r1 = r9.fwdElm
            java.lang.String r10 = r10.number
            r1.target = r10
        L_0x00fb:
            com.sec.internal.ims.servicemodules.ss.Condition r10 = r9.conditions
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r8.mProfile
            int r1 = r1.action
            if (r1 == r2) goto L_0x0108
            r4 = 3
            if (r1 != r4) goto L_0x0107
            goto L_0x0108
        L_0x0107:
            r2 = r3
        L_0x0108:
            r10.state = r2
            goto L_0x0131
        L_0x010b:
            com.sec.internal.ims.servicemodules.ss.UtProfile r10 = r8.mProfile
            int r1 = r10.action
            if (r1 != r2) goto L_0x0120
            com.sec.internal.ims.servicemodules.ss.Condition r1 = r9.conditions
            r1.state = r2
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.ATT
            if (r0 != r1) goto L_0x0131
            com.sec.internal.ims.servicemodules.ss.ForwardTo r1 = r9.fwdElm
            java.lang.String r10 = r10.number
            r1.target = r10
            goto L_0x0131
        L_0x0120:
            if (r1 != r4) goto L_0x012d
            com.sec.internal.ims.servicemodules.ss.ForwardTo r10 = r9.fwdElm
            java.lang.String r1 = ""
            r10.target = r1
            com.sec.internal.ims.servicemodules.ss.Condition r10 = r9.conditions
            r10.state = r3
            goto L_0x0131
        L_0x012d:
            com.sec.internal.ims.servicemodules.ss.Condition r10 = r9.conditions
            r10.state = r3
        L_0x0131:
            com.sec.internal.ims.servicemodules.ss.Condition r10 = r9.conditions
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r8.mProfile
            int r1 = r1.action
            r10.action = r1
            com.sec.internal.ims.servicemodules.ss.ForwardTo r10 = r9.fwdElm
            java.lang.String r10 = r10.target
            boolean r10 = android.text.TextUtils.isEmpty(r10)
            if (r10 != 0) goto L_0x0187
            com.sec.internal.ims.servicemodules.ss.ForwardTo r10 = r9.fwdElm
            java.lang.String r10 = r10.target
            java.lang.String r1 = "sip:"
            boolean r10 = r10.startsWith(r1)
            if (r10 != 0) goto L_0x0175
            com.sec.internal.ims.servicemodules.ss.ForwardTo r10 = r9.fwdElm
            java.lang.String r10 = r10.target
            java.lang.String r1 = "tel:"
            boolean r10 = r10.startsWith(r1)
            if (r10 != 0) goto L_0x0175
            com.sec.internal.ims.servicemodules.ss.ForwardTo r10 = r9.fwdElm
            java.lang.String r10 = r10.target
            java.lang.String r1 = "voicemail:"
            boolean r10 = r10.startsWith(r1)
            if (r10 != 0) goto L_0x0175
            com.sec.internal.ims.servicemodules.ss.ForwardTo r10 = r9.fwdElm
            java.lang.String r1 = r10.target
            java.lang.String r1 = r8.getNetworkPreferredUri(r1)
            r10.target = r1
            goto L_0x0187
        L_0x0175:
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.TMOBILE_PL
            if (r0 != r10) goto L_0x0187
            com.sec.internal.ims.servicemodules.ss.ForwardTo r10 = r9.fwdElm
            java.lang.String r1 = r10.target
            java.lang.String r1 = com.sec.internal.ims.servicemodules.ss.UtUtils.getNumberFromURI(r1)
            java.lang.String r1 = r8.getNetworkPreferredUri(r1)
            r10.target = r1
        L_0x0187:
            com.sec.internal.ims.servicemodules.ss.ForwardTo r10 = r9.fwdElm
            java.util.List<com.sec.internal.ims.servicemodules.ss.ForwardElm> r10 = r10.fwdElm
            if (r10 == 0) goto L_0x01a0
            int r10 = r10.size()
            if (r10 <= 0) goto L_0x01a0
            boolean r8 = r8.isSupportfwd(r0)
            if (r8 == 0) goto L_0x01a0
            com.sec.internal.ims.servicemodules.ss.ForwardTo r8 = r9.fwdElm
            java.util.List<com.sec.internal.ims.servicemodules.ss.ForwardElm> r8 = r8.fwdElm
            r8.clear()
        L_0x01a0:
            return r9
        L_0x01a1:
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r0 = new com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule
            r0.<init>()
            com.sec.internal.ims.servicemodules.ss.ForwardTo r1 = new com.sec.internal.ims.servicemodules.ss.ForwardTo
            r1.<init>()
            r0.fwdElm = r1
            com.sec.internal.ims.servicemodules.ss.Condition r1 = new com.sec.internal.ims.servicemodules.ss.Condition
            r1.<init>()
            r0.conditions = r1
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r8.mProfile
            java.lang.String r1 = r1.number
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x01cc
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r8.mProfile
            java.lang.String r2 = r1.number
            java.lang.String r2 = com.sec.internal.ims.servicemodules.ss.UtUtils.getNumberFromURI(r2)
            java.lang.String r2 = r8.getNetworkPreferredUri(r2)
            r1.number = r2
        L_0x01cc:
            com.sec.internal.ims.servicemodules.ss.ForwardTo r1 = r0.fwdElm
            com.sec.internal.ims.servicemodules.ss.UtProfile r2 = r8.mProfile
            java.lang.String r2 = r2.number
            r1.target = r2
            java.lang.String r1 = r8.getCfRuleId(r9)
            r0.ruleId = r1
            com.sec.internal.ims.servicemodules.ss.Condition r1 = r0.conditions
            r1.condition = r9
            com.sec.internal.ims.servicemodules.ss.UtProfile r9 = r8.mProfile
            int r9 = r9.action
            if (r9 == 0) goto L_0x01e6
            if (r9 != r4) goto L_0x01e8
        L_0x01e6:
            r1.state = r3
        L_0x01e8:
            r1.action = r9
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r9 = r8.mCFCache
            if (r9 != 0) goto L_0x01f5
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r9 = new com.sec.internal.ims.servicemodules.ss.CallForwardingData
            r9.<init>()
            r8.mCFCache = r9
        L_0x01f5:
            com.sec.internal.ims.servicemodules.ss.Condition r9 = r0.conditions
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            r9.media = r1
            com.sec.internal.ims.servicemodules.ss.Condition r9 = r0.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r9 = r9.media
            r9.add(r10)
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r8 = r8.mCFCache
            r8.setRule(r0)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.getCallForwardRule(int, com.sec.internal.ims.servicemodules.ss.MEDIA):com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule");
    }

    private CallForwardingData.Rule makeCFRule(int i, int i2, int i3, String str) {
        CallForwardingData.Rule makeRule = CallForwardingData.makeRule(i, UtUtils.convertToMedia(i2));
        makeRule.ruleId = getCfRuleId(i);
        if (i3 == 1 || i3 == 3) {
            makeRule.conditions.state = true;
            makeRule.fwdElm.target = str;
        } else {
            makeRule.conditions.state = false;
            if (i3 == 4) {
                makeRule.fwdElm.target = "";
            }
        }
        makeRule.conditions.action = i3;
        if (!TextUtils.isEmpty(makeRule.fwdElm.target)) {
            ForwardTo forwardTo = makeRule.fwdElm;
            forwardTo.target = getNetworkPreferredUri(UtUtils.getNumberFromURI(forwardTo.target));
        }
        return makeRule;
    }

    private CallBarringData.Rule makeCBRule(int i, int i2, int i3) {
        CallBarringData.Rule makeRule = CallBarringData.makeRule(i, UtUtils.convertToMedia(i2));
        makeRule.ruleId = getCbRuleId();
        Condition condition = makeRule.conditions;
        boolean z = true;
        if (!(i3 == 1 || i3 == 3)) {
            z = false;
        }
        condition.state = z;
        condition.action = i3;
        return makeRule;
    }

    private MEDIA getMatchedMediaForCB(CallBarringData callBarringData, MEDIA media) {
        if (callBarringData.isExist(this.mProfile.condition, media)) {
            return media;
        }
        if (this.mFeature.supportAlternativeMediaForCb) {
            int i = this.mProfile.condition;
            MEDIA media2 = MEDIA.ALL;
            if (callBarringData.isExist(i, media2)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "no exact CB rule media match -> media ALL should be used");
                return media2;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String getCbRuleId() {
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        int i = this.mProfile.type;
        if (i == 103) {
            if (simMno.isOneOf(Mno.CMHK, Mno.HK3)) {
                int i2 = this.mProfile.condition;
                if ((i2 == 1 || i2 == 7 || i2 == 9) && this.mFeature.cbbaic.length() > 0) {
                    return this.mFeature.cbbaic;
                }
                if (this.mProfile.condition == 5 && this.mFeature.cbbicwr.length() > 0) {
                    return this.mFeature.cbbicwr;
                }
                if (this.mFeature.cbbaic.length() > 0) {
                    return this.mFeature.cbbaic;
                }
            }
            if (this.mProfile.condition == 5 && this.mFeature.cbbicwr.length() > 0) {
                return this.mFeature.cbbicwr;
            }
            if (this.mProfile.condition == 1 && this.mFeature.cbbaic.length() > 0) {
                return this.mFeature.cbbaic;
            }
            return "ICB" + createCBRequestId();
        } else if (i != 105) {
            return "";
        } else {
            String cbRuleIdFromFeature = getCbRuleIdFromFeature(simMno);
            if (cbRuleIdFromFeature != null) {
                return cbRuleIdFromFeature;
            }
            return "OCB" + createCBRequestId();
        }
    }

    private String getCbRuleIdFromFeature(Mno mno) {
        if (mno.isOneOf(Mno.CMHK, Mno.HK3)) {
            int i = this.mProfile.condition;
            if ((i == 2 || i == 8) && this.mFeature.cbbaoc.length() > 0) {
                return this.mFeature.cbbaoc;
            }
            if (this.mProfile.condition == 3 && this.mFeature.cbboic.length() > 0) {
                return this.mFeature.cbboic;
            }
            if (this.mProfile.condition == 4 && this.mFeature.cbboic_exhc.length() > 0) {
                return this.mFeature.cbboic_exhc;
            }
            if (this.mFeature.cbbaoc.length() > 0) {
                return this.mFeature.cbbaoc;
            }
        }
        if (mno != Mno.DTAC) {
            return null;
        }
        if (this.mProfile.condition == 2 && this.mFeature.cbbaoc.length() > 0) {
            return this.mFeature.cbbaoc;
        }
        if (this.mProfile.condition == 3 && this.mFeature.cbboic.length() > 0) {
            return this.mFeature.cbboic;
        }
        if (this.mProfile.condition != 4 || this.mFeature.cbboic_exhc.length() <= 0) {
            return null;
        }
        return this.mFeature.cbboic_exhc;
    }

    /* access modifiers changed from: protected */
    public String getCfRuleId(int i) {
        String str;
        if (i == 1) {
            str = this.mFeature.cfb;
        } else if (i == 2) {
            str = this.mFeature.cfnr;
        } else if (i != 3) {
            str = i != 6 ? this.mFeature.cfu : this.mFeature.cfni;
        } else {
            str = this.mFeature.cfnrc;
        }
        if (!this.mFeature.support_media || !UtUtils.convertToMedia(this.mProfile.serviceClass).equals(MEDIA.VIDEO)) {
            return str;
        }
        return str + "_video";
    }

    /* access modifiers changed from: protected */
    public String getCfURL() {
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        UtProfile utProfile = this.mProfile;
        int i = utProfile.condition;
        if (i == 5 || i == 4) {
            return simMno == Mno.CHT ? "?xmlns(ss=http://uri.etsi.org/ngn/params/xml/simservs/xcap)" : "?xmlns(cp=urn:ietf:params:xml:ns:common-policy)";
        }
        if (i == 7) {
            return UtUrl.NOREPLY_URL;
        }
        if (this.mCFCache != null) {
            MEDIA convertToMedia = UtUtils.convertToMedia(utProfile.serviceClass);
            String str = this.mCFCache.getRule(this.mProfile.condition, convertToMedia).ruleId;
            if ((simMno == Mno.CU || simMno == Mno.CTC) && !this.mCFCache.isExist(this.mProfile.condition, convertToMedia)) {
                CallForwardingData callForwardingData = this.mCFCache;
                int i2 = this.mProfile.condition;
                MEDIA media = MEDIA.AUDIO;
                if (callForwardingData.isExist(i2, media)) {
                    str = this.mCFCache.getRule(this.mProfile.condition, media).ruleId;
                }
            }
            if (str != null) {
                return UtUrl.DIV_START_URL + str + UtUrl.DIV_END_URL;
            }
        }
        return UtUrl.DIV_START_URL + getCfRuleId(this.mProfile.condition) + UtUrl.DIV_END_URL;
    }

    /* access modifiers changed from: protected */
    public String getCbURL() {
        MEDIA matchedMediaForCB;
        MEDIA matchedMediaForCB2;
        UtProfile utProfile = this.mProfile;
        if (utProfile.type == 105) {
            CallBarringData callBarringData = this.mOCBCache;
            if (!(callBarringData == null || (matchedMediaForCB2 = getMatchedMediaForCB(callBarringData, UtUtils.convertToMedia(utProfile.serviceClass))) == null)) {
                return UtUrl.DIV_START_URL + this.mOCBCache.getRule(this.mProfile.condition, matchedMediaForCB2).ruleId + UtUrl.DIV_END_URL;
            }
        } else {
            CallBarringData callBarringData2 = this.mICBCache;
            if (!(callBarringData2 == null || (matchedMediaForCB = getMatchedMediaForCB(callBarringData2, UtUtils.convertToMedia(utProfile.serviceClass))) == null)) {
                return UtUrl.DIV_START_URL + this.mICBCache.getRule(this.mProfile.condition, matchedMediaForCB).ruleId + UtUrl.DIV_END_URL;
            }
        }
        return UtUrl.DIV_START_URL + getCbRuleId() + UtUrl.DIV_END_URL;
    }

    /* access modifiers changed from: protected */
    public HashMap<String, String> makeHeader() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(HttpController.HEADER_HOST, this.mConfig.nafServer);
        hashMap.put("Accept-Encoding", UtUtils.getAcceptEncoding(this.mPhoneId));
        hashMap.put("Accept", "*/*");
        hashMap.put("X-3GPP-Intended-Identity", CmcConstants.E_NUM_STR_QUOTE + this.mConfig.impu + CmcConstants.E_NUM_STR_QUOTE);
        hashMap.put("User-Agent", this.mConfig.xdmUserAgent);
        if (UtUtils.isPutRequest(this.mProfile.type)) {
            hashMap.put("Content-Type", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        }
        return hashMap;
    }

    /* access modifiers changed from: protected */
    public int createCBRequestId() {
        if (mCBIdCounter >= 255) {
            mCBIdCounter = 0;
        }
        int i = mCBIdCounter + 1;
        mCBIdCounter = i;
        return i;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:81:0x017f, code lost:
        r4 = r12.mProfile;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String makeUri() {
        /*
            r12 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            com.sec.internal.ims.servicemodules.ss.UtConfigData r1 = r12.mConfig
            int r1 = r1.nafPort
            r2 = 443(0x1bb, float:6.21E-43)
            if (r1 == r2) goto L_0x001a
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r1 = r12.mFeature
            boolean r1 = r1.support_tls
            if (r1 == 0) goto L_0x0014
            goto L_0x001a
        L_0x0014:
            java.lang.String r1 = "http://"
            r0.append(r1)
            goto L_0x001f
        L_0x001a:
            java.lang.String r1 = "https://"
            r0.append(r1)
        L_0x001f:
            com.sec.internal.ims.servicemodules.ss.UtConfigData r1 = r12.mConfig
            java.lang.String r1 = r1.nafServer
            r0.append(r1)
            com.sec.internal.ims.servicemodules.ss.UtConfigData r1 = r12.mConfig
            int r1 = r1.nafPort
            r2 = 80
            if (r1 == r2) goto L_0x003a
            java.lang.String r1 = ":"
            r0.append(r1)
            com.sec.internal.ims.servicemodules.ss.UtConfigData r1 = r12.mConfig
            int r1 = r1.nafPort
            r0.append(r1)
        L_0x003a:
            com.sec.internal.ims.servicemodules.ss.UtConfigData r1 = r12.mConfig
            java.lang.String r1 = r1.xcapRootUri
            boolean r1 = r1.isEmpty()
            if (r1 != 0) goto L_0x004b
            com.sec.internal.ims.servicemodules.ss.UtConfigData r1 = r12.mConfig
            java.lang.String r1 = r1.xcapRootUri
            r0.append(r1)
        L_0x004b:
            int r1 = r12.mPhoneId
            com.sec.internal.constants.Mno r1 = com.sec.internal.helper.SimUtil.getSimMno(r1)
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.XPLORE
            if (r1 != r2) goto L_0x005a
            java.lang.String r2 = "/rem/sentinel/xcap"
            r0.append(r2)
        L_0x005a:
            java.lang.String r2 = "/simservs.ngn.etsi.org/users/"
            r0.append(r2)
            com.sec.internal.ims.servicemodules.ss.UtConfigData r2 = r12.mConfig
            java.lang.String r2 = r2.impu
            r0.append(r2)
            java.lang.String r2 = "/simservs.xml"
            r0.append(r2)
            com.sec.internal.ims.servicemodules.ss.UtProfile r2 = r12.mProfile
            int r2 = r2.type
            r3 = 9
            r4 = 8
            r5 = 4
            java.lang.String r6 = "/~~/simservs/communication-diversion"
            java.lang.String r7 = "/~~/simservs/ss:outgoing-communication-barring"
            java.lang.String r8 = "/~~/simservs/outgoing-communication-barring"
            java.lang.String r9 = "/~~/simservs/ss:incoming-communication-barring"
            java.lang.String r10 = "/~~/simservs/incoming-communication-barring"
            java.lang.String r11 = "/~~/simservs/ss:communication-diversion"
            switch(r2) {
                case 100: goto L_0x0155;
                case 101: goto L_0x0124;
                case 102: goto L_0x0118;
                case 103: goto L_0x00e8;
                case 104: goto L_0x00da;
                case 105: goto L_0x00a8;
                case 106: goto L_0x00a1;
                case 107: goto L_0x00a1;
                case 108: goto L_0x009a;
                case 109: goto L_0x009a;
                case 110: goto L_0x0093;
                case 111: goto L_0x0093;
                case 112: goto L_0x008c;
                case 113: goto L_0x008c;
                case 114: goto L_0x0085;
                case 115: goto L_0x0085;
                default: goto L_0x0083;
            }
        L_0x0083:
            goto L_0x0165
        L_0x0085:
            java.lang.String r2 = "/~~/simservs/communication-waiting"
            r0.append(r2)
            goto L_0x0165
        L_0x008c:
            java.lang.String r2 = "/~~/terminating-identity-presentation-restriction"
            r0.append(r2)
            goto L_0x0165
        L_0x0093:
            java.lang.String r2 = "/~~/terminating-identity-presentation"
            r0.append(r2)
            goto L_0x0165
        L_0x009a:
            java.lang.String r2 = "/~~/simservs/originating-identity-presentation-restriction"
            r0.append(r2)
            goto L_0x0165
        L_0x00a1:
            java.lang.String r2 = "/~~/simservs/originating-identity-presentation"
            r0.append(r2)
            goto L_0x0165
        L_0x00a8:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.CHT
            if (r1 != r2) goto L_0x00b0
            r0.append(r7)
            goto L_0x00b3
        L_0x00b0:
            r0.append(r8)
        L_0x00b3:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.HK3
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.FET
            com.sec.internal.constants.Mno[] r2 = new com.sec.internal.constants.Mno[]{r2, r6}
            boolean r2 = r1.isOneOf(r2)
            if (r2 == 0) goto L_0x00cb
            com.sec.internal.ims.servicemodules.ss.UtProfile r2 = r12.mProfile
            int r2 = r2.condition
            if (r2 == r4) goto L_0x0165
            if (r2 != r3) goto L_0x00cb
            goto L_0x0165
        L_0x00cb:
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r2 = r12.mFeature
            boolean r2 = r2.isCBSingleElement
            if (r2 == 0) goto L_0x0165
            java.lang.String r2 = r12.getCbURL()
            r0.append(r2)
            goto L_0x0165
        L_0x00da:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.CHT
            if (r1 != r2) goto L_0x00e3
            r0.append(r7)
            goto L_0x0165
        L_0x00e3:
            r0.append(r8)
            goto L_0x0165
        L_0x00e8:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.CHT
            if (r1 != r2) goto L_0x00f0
            r0.append(r9)
            goto L_0x00f3
        L_0x00f0:
            r0.append(r10)
        L_0x00f3:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.HK3
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.FET
            com.sec.internal.constants.Mno[] r2 = new com.sec.internal.constants.Mno[]{r2, r6}
            boolean r2 = r1.isOneOf(r2)
            if (r2 == 0) goto L_0x010a
            com.sec.internal.ims.servicemodules.ss.UtProfile r2 = r12.mProfile
            int r2 = r2.condition
            if (r2 == r4) goto L_0x0165
            if (r2 != r3) goto L_0x010a
            goto L_0x0165
        L_0x010a:
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r2 = r12.mFeature
            boolean r2 = r2.isCBSingleElement
            if (r2 == 0) goto L_0x0165
            java.lang.String r2 = r12.getCbURL()
            r0.append(r2)
            goto L_0x0165
        L_0x0118:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.CHT
            if (r1 != r2) goto L_0x0120
            r0.append(r9)
            goto L_0x0165
        L_0x0120:
            r0.append(r10)
            goto L_0x0165
        L_0x0124:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.CHT
            if (r1 == r2) goto L_0x0135
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.SPRINT
            if (r1 == r2) goto L_0x0135
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.XPLORE
            if (r1 != r2) goto L_0x0131
            goto L_0x0135
        L_0x0131:
            r0.append(r6)
            goto L_0x0138
        L_0x0135:
            r0.append(r11)
        L_0x0138:
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r2 = r12.mFeature
            boolean r2 = r2.isCFSingleElement
            if (r2 == 0) goto L_0x0145
            java.lang.String r2 = r12.getCfURL()
            r0.append(r2)
        L_0x0145:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.SMARTONE
            if (r1 != r2) goto L_0x0165
            com.sec.internal.ims.servicemodules.ss.UtProfile r2 = r12.mProfile
            int r2 = r2.action
            if (r2 != r5) goto L_0x0165
            java.lang.String r2 = "/cp:conditions"
            r0.append(r2)
            goto L_0x0165
        L_0x0155:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.CHT
            if (r1 == r2) goto L_0x0162
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.XPLORE
            if (r1 != r2) goto L_0x015e
            goto L_0x0162
        L_0x015e:
            r0.append(r6)
            goto L_0x0165
        L_0x0162:
            r0.append(r11)
        L_0x0165:
            java.lang.String r2 = "cp:"
            int r2 = r0.indexOf(r2)
            java.lang.String r3 = "ss:"
            int r3 = r0.indexOf(r3)
            if (r2 > 0) goto L_0x0176
            if (r3 <= 0) goto L_0x01bb
        L_0x0176:
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.CHT
            r6 = 101(0x65, float:1.42E-43)
            java.lang.String r7 = "xmlns(cp=urn:ietf:params:xml:ns:common-policy)"
            if (r1 != r4) goto L_0x0190
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r12.mProfile
            int r8 = r4.condition
            r9 = 5
            if (r8 == r9) goto L_0x0188
            if (r8 != r5) goto L_0x0190
        L_0x0188:
            int r4 = r4.type
            if (r4 != r6) goto L_0x0190
            r0.append(r7)
            goto L_0x01bb
        L_0x0190:
            java.lang.String r4 = "?"
            r0.append(r4)
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.XPLORE
            java.lang.String r5 = "xmlns(ss=http://uri.etsi.org/ngn/params/xml/simservs/xcap)"
            if (r1 != r4) goto L_0x01a7
            if (r3 <= 0) goto L_0x01a1
            r0.append(r5)
        L_0x01a1:
            if (r2 <= 0) goto L_0x01bb
            r0.append(r7)
            goto L_0x01bb
        L_0x01a7:
            if (r2 <= 0) goto L_0x01ac
            r0.append(r7)
        L_0x01ac:
            if (r3 > 0) goto L_0x01b8
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.SFR
            if (r1 != r2) goto L_0x01bb
            com.sec.internal.ims.servicemodules.ss.UtProfile r12 = r12.mProfile
            int r12 = r12.type
            if (r12 != r6) goto L_0x01bb
        L_0x01b8:
            r0.append(r5)
        L_0x01bb:
            java.lang.String r12 = r0.toString()
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.makeUri():java.lang.String");
    }

    /* access modifiers changed from: protected */
    public HttpRequestParams makeHttpParams() {
        boolean z;
        int i;
        int i2;
        String str;
        int i3;
        LinkPropertiesWrapper linkProperties;
        Dns dns;
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        HttpRequestParams httpRequestParams = new HttpRequestParams();
        HashMap<String, String> makeHeader = makeHeader();
        SocketFactory socketFactory = this.mSocketFactory;
        if (socketFactory != null) {
            httpRequestParams.setSocketFactory(socketFactory);
        }
        UtFeatureData utFeatureData = this.mFeature;
        if (utFeatureData.isReuseConnection && (dns = this.mDns) != null) {
            httpRequestParams.setDns(dns);
        } else if (this.mNetwork != null) {
            int i4 = utFeatureData.ip_version;
            if (i4 > 0) {
                if (simMno != Mno.CTCMO || (linkProperties = this.mPdnController.getLinkProperties(this.mPdnListener)) == null || !linkProperties.hasIPv4Address() || linkProperties.hasGlobalIPv6Address()) {
                    i3 = i4;
                } else {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "Local ip only has ipv4, use TYPE_A for DNS query");
                    i3 = 1;
                }
                if (!simMno.isChn()) {
                    this.mBsfRetryCounter = this.mNafRetryCounter;
                }
                if (simMno.isOneOf(Mno.TELSTRA, Mno.TELEFONICA_CZ, Mno.ETISALAT_UAE, Mno.SINGTEL, Mno.FET)) {
                    int i5 = this.mNafRetryCounter;
                    int i6 = this.mBsfRetryCounter;
                    Network network = this.mNetwork;
                    List<InetAddress> list = this.mDnsAddresses;
                    UtConfigData utConfigData = this.mConfig;
                    DnsController dnsController = r2;
                    DnsController dnsController2 = new DnsController(i5, i6, network, list, i3, true, simMno, utConfigData.nafServer, utConfigData.bsfServer, true);
                    this.mDns = dnsController;
                } else {
                    this.mDns = new DnsController(this.mNafRetryCounter, this.mBsfRetryCounter, this.mNetwork, this.mDnsAddresses, i3, true, simMno);
                }
            } else {
                this.mDns = new UtStateMachine$$ExternalSyntheticLambda0(this);
            }
            httpRequestParams.setDns(this.mDns);
        }
        httpRequestParams.setReuseConnection(this.mFeature.isReuseConnection);
        httpRequestParams.setCallback(this.mUtCallback).setHeaders(makeHeader);
        if (UtUtils.isPutRequest(this.mProfile.type)) {
            httpRequestParams.setMethod(HttpRequestParams.Method.PUT);
            httpRequestParams.setPostBody(new HttpPostBody(updateUtDetailInfo().getBytes()));
        } else {
            httpRequestParams.setMethod(HttpRequestParams.Method.GET);
        }
        httpRequestParams.setUrl(makeUri()).setBsfUrl(this.mConfig.bsfServer).setPhoneId(this.mPhoneId);
        if (this.mConfig.username.isEmpty()) {
            httpRequestParams.setUserName(this.mConfig.impu);
        } else {
            httpRequestParams.setUserName(this.mConfig.username);
        }
        httpRequestParams.setPassword(this.mConfig.passwd).setUseTls(this.mFeature.support_tls).setConnectionTimeout(10000);
        if (simMno == Mno.GCF) {
            httpRequestParams.setReadTimeout(HTTP_READ_TIMEOUT_GCF);
        } else if (simMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
            httpRequestParams.setReadTimeout(HTTP_READ_TIMEOUT_TMB);
        } else {
            httpRequestParams.setReadTimeout(10000);
        }
        httpRequestParams.setIpVersion(this.mFeature.ip_version);
        if (simMno == Mno.ORANGE) {
            ApnSettings apnSettings = this.mApn;
            if (apnSettings != null) {
                str = apnSettings.getProxyAddress();
                i2 = this.mApn.getProxyPort();
            } else {
                str = null;
                i2 = 80;
            }
            Proxy proxy = Proxy.NO_PROXY;
            try {
                if (!TextUtils.isEmpty(str) && this.mNetwork != null) {
                    String str2 = LOG_TAG;
                    int i7 = this.mPhoneId;
                    IMSLog.i(str2, i7, "proxyAddress : " + str + " ProxyPort : " + i2);
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.mNetwork.getByName(str), i2));
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            z = true;
            httpRequestParams.setProxy(proxy).setUseProxy(true);
        } else {
            z = true;
        }
        if (simMno == Mno.CU) {
            httpRequestParams.setProxy(Proxy.NO_PROXY).setUseProxy(z);
        }
        if (simMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
            httpRequestParams.setUseImei(z);
        }
        if (simMno == Mno.HK3 || simMno == Mno.TWO_DEGREE) {
            i = 1;
            httpRequestParams.setIgnoreServerCert(true);
        } else {
            httpRequestParams.setIgnoreServerCert(false);
            i = 1;
        }
        if (simMno == Mno.VODAFONE_AUSTRALIA) {
            httpRequestParams.setCipherSuiteType(i);
        }
        return httpRequestParams;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ List lambda$makeHttpParams$0(String str) throws UnknownHostException {
        if (str != null) {
            try {
                return Arrays.asList(this.mNetwork.getAllByName(str));
            } catch (NullPointerException unused) {
                throw new UnknownHostException("android.net.Network.getAllByName returned null");
            }
        } else {
            throw new UnknownHostException("hostname == null");
        }
    }

    /* access modifiers changed from: protected */
    public void unhandledMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        UtLog.i(str, i, "handleMessage " + UtLog.getStringMessage(message.what));
        int i2 = message.what;
        String str2 = null;
        if (i2 != 12) {
            if (i2 == 14) {
                this.mProfile = null;
                setForce403Error(false);
                this.mUtServiceModule.unregisterCwdbObserver(this.mPhoneId);
                this.mUtServiceModule.updateCapabilities(this.mPhoneId);
                transitionTo(this.mRequestState);
                return;
            } else if (i2 != 15) {
                return;
            }
        }
        if (i2 == 15) {
            if (this.isRetryingCreatePdn) {
                removeMessages(100);
                this.isRetryingCreatePdn = false;
            }
            sendMessage(2);
        }
        int i3 = message.arg1;
        Object obj = message.obj;
        if (obj != null) {
            str2 = (String) obj;
        }
        this.mIsRequestFailed = true;
        requestFailed(i3, str2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:107:0x0248  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x025b  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x025d  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x01b0  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01b4  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x01f3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void requestFailed(int r17, java.lang.String r18) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            r2 = r18
            int r3 = com.sec.internal.helper.httpclient.DnsController.getNafAddrSize()
            int r4 = com.sec.internal.helper.httpclient.DnsController.getBsfAddrSize()
            com.sec.internal.ims.servicemodules.ss.UtProfile r5 = r0.mProfile
            int r5 = r5.type
            boolean r5 = com.sec.internal.ims.servicemodules.ss.UtUtils.isCallBarringType(r5)
            r6 = 32500(0x7ef4, double:1.6057E-319)
            r8 = 1017(0x3f9, float:1.425E-42)
            r9 = 15
            r10 = 1
            if (r5 == 0) goto L_0x0030
            com.sec.internal.ims.servicemodules.ss.UtProfile r5 = r0.mProfile
            int r5 = r5.condition
            r11 = 7
            if (r5 != r11) goto L_0x0030
            java.lang.String r5 = LOG_TAG
            int r11 = r0.mPhoneId
            java.lang.String r12 = "performing CSFB for CB_BA_ALL, ignoring handling NAPTR IP type"
            com.sec.internal.log.IMSLog.e(r5, r11, r12)
            goto L_0x0074
        L_0x0030:
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r5 = r0.mFeature
            if (r5 == 0) goto L_0x0074
            int r5 = r5.ip_version
            r11 = 3
            if (r5 != r11) goto L_0x0074
            int r5 = r0.mNafRetryCounter
            int r11 = r5 + 1
            if (r11 >= r3) goto L_0x0074
            int r5 = r5 + r10
            r0.mNafRetryCounter = r5
            r16.refreshDns()
            java.lang.String r1 = LOG_TAG
            int r2 = r0.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "mNafRetryCounter: "
            r3.append(r4)
            int r4 = r0.mNafRetryCounter
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r1, r2, r3)
            r0.removeMessages(r9)
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r1 = r0.mThisSm
            r1.sendMessageDelayed((int) r9, (int) r8, (long) r6)
            com.sec.internal.ims.servicemodules.ss.RequestState r1 = r0.mRequestState
            r0.transitionTo(r1)
            android.os.Message r1 = r0.obtainMessage(r10)
            r0.sendMessage((android.os.Message) r1)
            return
        L_0x0074:
            int r5 = r0.mPhoneId
            com.sec.internal.constants.Mno r5 = com.sec.internal.helper.SimUtil.getSimMno(r5)
            com.sec.internal.constants.Mno r11 = com.sec.internal.constants.Mno.TELEKOM_SVN
            r12 = 403(0x193, float:5.65E-43)
            if (r5 != r11) goto L_0x0086
            r11 = 404(0x194, float:5.66E-43)
            if (r1 != r11) goto L_0x0086
            r11 = r12
            goto L_0x0087
        L_0x0086:
            r11 = r1
        L_0x0087:
            com.sec.internal.constants.Mno r13 = com.sec.internal.constants.Mno.CHT
            r14 = 1004(0x3ec, float:1.407E-42)
            r15 = 503(0x1f7, float:7.05E-43)
            r6 = 0
            if (r5 != r13) goto L_0x00b7
            if (r11 == r15) goto L_0x00b3
            r7 = 1002(0x3ea, float:1.404E-42)
            if (r11 == r7) goto L_0x00b3
            r7 = 1009(0x3f1, float:1.414E-42)
            if (r11 == r7) goto L_0x00b3
            if (r11 == r14) goto L_0x00b3
            r7 = 1006(0x3ee, float:1.41E-42)
            if (r11 == r7) goto L_0x00b3
            r7 = 1007(0x3ef, float:1.411E-42)
            if (r11 == r7) goto L_0x00b3
            r7 = 1013(0x3f5, float:1.42E-42)
            if (r11 == r7) goto L_0x00b3
            r7 = 1014(0x3f6, float:1.421E-42)
            if (r11 == r7) goto L_0x00b3
            r7 = 10022(0x2726, float:1.4044E-41)
            if (r11 < r7) goto L_0x00b1
            goto L_0x00b3
        L_0x00b1:
            r7 = r6
            goto L_0x00b4
        L_0x00b3:
            r7 = r10
        L_0x00b4:
            if (r7 == 0) goto L_0x00b7
            r11 = r12
        L_0x00b7:
            if (r11 != r12) goto L_0x0101
            boolean r7 = r16.isForbidden()
            if (r7 != 0) goto L_0x0101
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r7 = r0.mFeature
            if (r7 == 0) goto L_0x0101
            int r7 = r7.timerFor403
            if (r7 == 0) goto L_0x0101
            boolean r7 = r16.isChnNoRuleCbPut403Error()
            if (r7 != 0) goto L_0x0101
            r0.removeMessages(r9)
            r0.setForce403Error(r10)
            com.sec.internal.ims.servicemodules.ss.UtServiceModule r1 = r0.mUtServiceModule
            int r3 = r0.mPhoneId
            r1.updateCapabilities(r3)
            java.lang.String r1 = LOG_TAG
            int r3 = r0.mPhoneId
            java.lang.String r4 = "By 403 Error, SS request will block"
            com.sec.internal.log.IMSLog.e(r1, r3, r4)
            com.sec.internal.ims.servicemodules.ss.UtServiceModule r1 = r0.mUtServiceModule
            int r3 = r0.mPhoneId
            java.lang.String r4 = "set force CSFB by 403 Error "
            r1.writeDump(r3, r4)
            com.sec.internal.ims.servicemodules.ss.UtServiceModule r1 = r0.mUtServiceModule
            int r3 = r0.mPhoneId
            r1.registerCwdbObserver(r3)
            r0.setTimerFor403(r2)
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r0.mThisSm
            r1 = 12
            r2 = 150(0x96, double:7.4E-322)
            r0.sendMessageDelayed((int) r1, (int) r12, (long) r2)
            return
        L_0x0101:
            boolean r7 = r0.isCsfbErrorCode(r11, r5)
            if (r7 == 0) goto L_0x0108
            r11 = r12
        L_0x0108:
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.TMOUS
            com.sec.internal.constants.Mno r13 = com.sec.internal.constants.Mno.DISH
            com.sec.internal.constants.Mno r15 = com.sec.internal.constants.Mno.ATT
            com.sec.internal.constants.Mno[] r7 = new com.sec.internal.constants.Mno[]{r7, r13, r15}
            boolean r7 = r5.isOneOf(r7)
            if (r7 == 0) goto L_0x011d
            if (r11 != r12) goto L_0x011d
            r15 = 503(0x1f7, float:7.05E-43)
            goto L_0x011e
        L_0x011d:
            r15 = r11
        L_0x011e:
            java.lang.String r7 = LOG_TAG
            int r11 = r0.mPhoneId
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            java.lang.String r12 = "errorCode "
            r13.append(r12)
            r13.append(r1)
            java.lang.String r12 = " is converted to "
            r13.append(r12)
            r13.append(r15)
            java.lang.String r12 = r13.toString()
            com.sec.internal.log.IMSLog.e(r7, r11, r12)
            boolean r5 = r5.isChn()
            if (r5 == 0) goto L_0x023c
            if (r1 == r14) goto L_0x023c
            boolean r5 = r0.mIsUtConnectionError
            r11 = 100
            r12 = 2
            if (r5 == 0) goto L_0x0208
            if (r2 == 0) goto L_0x0208
            java.lang.String r5 = r18.toLowerCase()
            r0.mIsUtConnectionError = r6
            int r13 = r0.mPhoneId
            java.lang.String r14 = "UT connection failed."
            com.sec.internal.log.IMSLog.e(r7, r13, r14)
            int r13 = r0.mNafRetryCounter
            int r13 = r13 + r10
            if (r13 < r3) goto L_0x0166
            int r13 = r0.mBsfRetryCounter
            int r13 = r13 + r10
            if (r13 >= r4) goto L_0x01a0
        L_0x0166:
            java.lang.String r13 = "failed to connect"
            boolean r14 = r5.contains(r13)
            if (r14 == 0) goto L_0x0185
            java.lang.String r14 = "xcap"
            boolean r14 = r5.contains(r14)
            if (r14 == 0) goto L_0x0185
            int r14 = r0.mNafRetryCounter
            int r6 = r14 + 1
            if (r6 >= r3) goto L_0x0185
            int r14 = r14 + r10
            r0.mNafRetryCounter = r14
            r16.refreshDns()
        L_0x0183:
            r3 = r10
            goto L_0x01a1
        L_0x0185:
            boolean r3 = r5.contains(r13)
            if (r3 == 0) goto L_0x01a0
            java.lang.String r3 = "bsf"
            boolean r3 = r5.contains(r3)
            if (r3 == 0) goto L_0x01a0
            int r3 = r0.mBsfRetryCounter
            int r6 = r3 + 1
            if (r6 >= r4) goto L_0x01a0
            int r3 = r3 + r10
            r0.mBsfRetryCounter = r3
            r16.refreshDns()
            goto L_0x0183
        L_0x01a0:
            r3 = 0
        L_0x01a1:
            if (r3 != 0) goto L_0x01b4
            java.lang.String r4 = "timeout"
            boolean r4 = r5.contains(r4)
            if (r4 == 0) goto L_0x01b4
            int r4 = r0.mUtHttpRetryCounter
            if (r4 >= r12) goto L_0x01b4
            int r4 = r4 + r10
            r0.mUtHttpRetryCounter = r4
            goto L_0x01b5
        L_0x01b4:
            r10 = r3
        L_0x01b5:
            int r3 = r0.mPhoneId
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "errStr: "
            r4.append(r5)
            r4.append(r2)
            java.lang.String r5 = ", needRetry = "
            r4.append(r5)
            r4.append(r10)
            java.lang.String r5 = ", mNafRetryCounter: "
            r4.append(r5)
            int r5 = r0.mNafRetryCounter
            r4.append(r5)
            java.lang.String r5 = ", mBsfRetryCounter: "
            r4.append(r5)
            int r5 = r0.mBsfRetryCounter
            r4.append(r5)
            java.lang.String r5 = ", mUtHttpRetryCounter: "
            r4.append(r5)
            int r5 = r0.mUtHttpRetryCounter
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.e(r7, r3, r4)
            if (r10 == 0) goto L_0x023c
            r0.removeMessages(r9)
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r1 = r0.mThisSm
            r2 = 32500(0x7ef4, double:1.6057E-319)
            r1.sendMessageDelayed((int) r9, (int) r8, (long) r2)
            com.sec.internal.ims.servicemodules.ss.RequestState r1 = r0.mRequestState
            r0.transitionTo(r1)
            r1 = 100
            r0.sendMessageDelayed((int) r11, (long) r1)
            return
        L_0x0208:
            if (r5 != 0) goto L_0x023c
            r3 = 403(0x193, float:5.65E-43)
            if (r1 == r3) goto L_0x023e
            int r4 = r0.mUtRetryCounter
            if (r4 >= r12) goto L_0x023e
            int r1 = r0.mPhoneId
            java.lang.String r2 = "CHN operator UT failed, retry after 5s"
            com.sec.internal.log.IMSLog.e(r7, r1, r2)
            int r1 = r0.mUtRetryCounter
            int r1 = r1 + r10
            r0.mUtRetryCounter = r1
            com.sec.internal.ims.util.httpclient.GbaHttpController r1 = com.sec.internal.ims.util.httpclient.GbaHttpController.getInstance()
            int r2 = r0.mPhoneId
            r1.clearLastAuthInfo(r2)
            r0.removeMessages(r9)
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r1 = r0.mThisSm
            r2 = 32500(0x7ef4, double:1.6057E-319)
            r1.sendMessageDelayed((int) r9, (int) r8, (long) r2)
            com.sec.internal.ims.servicemodules.ss.RequestState r1 = r0.mRequestState
            r0.transitionTo(r1)
            r1 = 5000(0x1388, double:2.4703E-320)
            r0.sendMessageDelayed((int) r11, (long) r1)
            return
        L_0x023c:
            r3 = 403(0x193, float:5.65E-43)
        L_0x023e:
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r0.mProfile
            int r4 = r4.type
            boolean r4 = com.sec.internal.ims.servicemodules.ss.UtUtils.isPutRequest(r4)
            if (r4 == 0) goto L_0x0254
            r4 = 0
            r0.mSeparatedMedia = r4
            r0.mSeparatedCfAll = r4
            r0.mSeparatedCFNRY = r4
            r0.mSeparatedCFNL = r4
            r4 = -1
            r0.mMainCondition = r4
        L_0x0254:
            android.os.Bundle r4 = new android.os.Bundle
            r4.<init>()
            if (r15 <= 0) goto L_0x025d
            r12 = r15
            goto L_0x025e
        L_0x025d:
            r12 = r3
        L_0x025e:
            java.lang.String r3 = "errorCode"
            r4.putInt(r3, r12)
            java.lang.String r3 = "originErrorCode"
            r4.putInt(r3, r1)
            if (r2 == 0) goto L_0x0280
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r1 = r0.mFeature
            if (r1 == 0) goto L_0x0280
            boolean r1 = r1.isErrorMsgDisplay
            if (r1 == 0) goto L_0x0280
            com.sec.internal.ims.servicemodules.ss.UtXmlParse r1 = new com.sec.internal.ims.servicemodules.ss.UtXmlParse
            r1.<init>()
            java.lang.String r1 = r1.parseError(r2)
            java.lang.String r2 = "errorMsg"
            r4.putString(r2, r1)
        L_0x0280:
            r0.failUtRequest(r4)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.requestFailed(int, java.lang.String):void");
    }

    /* access modifiers changed from: private */
    public void refreshDns() {
        this.mDns = null;
    }

    private boolean isCsfbErrorCode(int i, Mno mno) {
        UtFeatureData utFeatureData;
        String[] strArr;
        if (mno.isUSA() && i == 1009) {
            return false;
        }
        if (i == 1009 || i == 1003 || i == 1013 || (utFeatureData = this.mFeature) == null || (strArr = utFeatureData.csfbErrorCodeList) == null) {
            return true;
        }
        if (strArr.length == 0) {
            return false;
        }
        if ("all".equalsIgnoreCase(strArr[0])) {
            return true;
        }
        for (String replace : this.mFeature.csfbErrorCodeList) {
            if (String.valueOf(i).matches(replace.replace("x", "[0-9]").trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isChnNoRuleCbPut403Error() {
        CallBarringData callBarringData;
        CallBarringData callBarringData2;
        if (SimUtil.getSimMno(this.mPhoneId).isChn()) {
            UtProfile utProfile = this.mProfile;
            int i = utProfile.type;
            int i2 = utProfile.serviceClass;
            if (i == 103 && (callBarringData2 = this.mICBCache) != null && getMatchedMediaForCB(callBarringData2, UtUtils.convertToMedia(i2)) == null) {
                return true;
            }
            if (i == 105 && (callBarringData = this.mOCBCache) != null && getMatchedMediaForCB(callBarringData, UtUtils.convertToMedia(i2)) == null) {
                return true;
            }
            return false;
        }
        return false;
    }

    private void setTimerFor403(String str) {
        int i = this.mFeature.timerFor403;
        if (!TextUtils.isEmpty(str) && str.contains("10 minutes")) {
            i = 600;
        }
        if (i > 0) {
            sendMessageDelayed(14, ((long) i) * 1000);
        }
    }

    /* access modifiers changed from: private */
    public void updateDnsInfo() {
        List<String> dnsServers = this.mPdnController.getDnsServers(this.mPdnListener);
        if (dnsServers == null || dnsServers.size() <= 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Dns Service List is null");
            sendMessage(obtainMessage(12, 1018));
            return;
        }
        try {
            this.mDnsAddresses.clear();
            for (String byName : dnsServers) {
                this.mDnsAddresses.add(this.mNetwork.getByName(byName));
            }
        } catch (UnknownHostException unused) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "UnknownHostException");
        }
    }

    /* access modifiers changed from: protected */
    public boolean getUserSetToBoolean(int i, String str) {
        return UserConfiguration.getUserConfig(this.mContext, i, str, true);
    }

    /* access modifiers changed from: protected */
    public int getUserSetToInt(int i, String str, int i2) {
        return UserConfiguration.getUserConfig(this.mContext, i, str, i2);
    }

    /* access modifiers changed from: protected */
    public void setUserSet(int i, String str, int i2) {
        UserConfiguration.setUserConfig(this.mContext, i, str, i2);
    }

    /* access modifiers changed from: protected */
    public void setUserSet(int i, String str, boolean z) {
        UserConfiguration.setUserConfig(this.mContext, i, str, z);
    }

    public boolean hasProfile() {
        if (this.mProfile != null) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "mProfile is null. so ignore");
        return false;
    }

    private void printCompleteLog(Bundle[] bundleArr, int i, int i2) {
        String str;
        String str2 = "UtXcap[" + i2 + "]< ";
        if (UtUtils.isPutRequest(i)) {
            str = str2 + UtLog.extractLogFromUtProfile(this.mProfile) + " {Success}";
        } else {
            str = str2 + UtLog.extractLogFromResponse(i, bundleArr);
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, str);
        this.mUtServiceModule.writeDump(this.mPhoneId, str);
        IMSLog.c(LogClass.UT_RESPONSE, this.mPhoneId + "," + i2 + ",<,T" + UtLog.extractCrLogFromResponse(i, bundleArr));
    }

    private void printFailLog(Bundle bundle, int i, int i2) {
        String str = "UtXcap[" + i2 + "]< [!ERROR] " + UtLog.extractLogFromUtProfile(this.mProfile) + UtLog.extractLogFromError(bundle);
        IMSLog.i(LOG_TAG, this.mPhoneId, str);
        this.mUtServiceModule.writeDump(this.mPhoneId, str);
        IMSLog.c(LogClass.UT_RESPONSE, this.mPhoneId + "," + i2 + ",<,F," + bundle.getInt("originErrorCode") + "," + bundle.getInt("errorCode"));
    }
}
