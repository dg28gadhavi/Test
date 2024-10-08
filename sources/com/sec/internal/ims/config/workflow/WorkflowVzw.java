package com.sec.internal.ims.config.workflow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.config.IXmlParserAdapter;
import com.sec.internal.log.IMSLog;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class WorkflowVzw extends WorkflowUpBase {
    protected static final int GENERAL_ERROR_MAX_RETRY = 3;
    protected static final long[] GENERAL_ERROR_RETRY_TIME = {0, 120000, 240000};
    protected static final String INTENT_GENERAL_ERROR_MAX_RETRY = "com.sec.internal.ims.config.workflow.general_error_max_retry";
    protected static final String LOCAL_CONFIG_BASE = "base";
    protected static final String LOCAL_CONFIG_FILE = "localconfig";
    protected static final int LOCAL_CONFIG_MAX_RETRY = 5;
    protected static final String LOCAL_CONFIG_TARGET = "vzw_up";
    protected static final int LOCAL_CONFIG_VERS = 59;
    protected static final String LOG_TAG = WorkflowVzw.class.getSimpleName();
    protected static final int NO_INITIAL_DATA_MAX_RETRY = 5;
    protected static final int UP_VERSION_DISABLED_VERS = 101;
    protected int m511ResponseRetryCount = 0;
    protected int mAdsSubId = 0;
    protected String mAppToken = null;
    protected int mBackupVersion = 0;
    protected String mCurClientVendor = null;
    protected String mCurClientVersion = null;
    protected boolean mCurConfigStartForce = false;
    protected String mCurRcsProfile = null;
    protected String mCurRcsVersion = null;
    protected int mCurVersion = 0;
    protected String mDmaPackage = null;
    protected PendingIntent mGeneralErrorRetryIntent = null;
    protected BroadcastReceiver mGeneralErrorRetryIntentReceiver = null;
    protected int mHttpResponse = 0;
    protected IImsRegistrationListener mImsRegistrationListener = null;
    protected IntentFilter mIntentFilter;
    protected boolean mIsDmaPackageChanged = false;
    protected boolean mIsGeneralErrorRetryFailed = false;
    protected boolean mIsGeneralErrorRetryTimerRunning = false;
    protected boolean mIsImsRegiNotifyReceived = false;
    protected boolean mIsMobileAutoConfigOngoing = false;
    protected boolean mIsMobileConnectionAvailable = false;
    protected boolean mIsSecAndGoogDmaPackageSwitched = false;
    protected ConnectivityManager.NetworkCallback mMobileStateCallback = null;
    protected WorkflowBase.Workflow mNextWorkflow = null;
    protected int mNoAppTokenRetryCount = 0;
    protected int mNoInitialDataRetryCount = 0;
    protected int mNoRemainingValidityRetryCount = 0;
    protected int mNoResponseRetryCount = 0;
    protected int mRcsDisabledStateRetryCount = 0;
    protected IXmlParserAdapter mXmlMultipleParser;

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        return null;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowVzw(android.os.Looper r13, android.content.Context r14, com.sec.internal.interfaces.ims.config.IConfigModule r15, com.sec.internal.constants.Mno r16, int r17) {
        /*
            r12 = this;
            r11 = r12
            r2 = r14
            r3 = r15
            r10 = r17
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw
            r5.<init>(r14, r15, r10)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterVzw r7 = new com.sec.internal.ims.config.adapters.HttpAdapterVzw
            r7.<init>(r10)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r9.<init>(r14, r15)
            r0 = r12
            r1 = r13
            r4 = r16
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            r0 = 0
            r11.mCurRcsVersion = r0
            r11.mCurRcsProfile = r0
            r11.mCurClientVendor = r0
            r11.mCurClientVersion = r0
            r11.mDmaPackage = r0
            r11.mAppToken = r0
            r1 = 0
            r11.mCurVersion = r1
            r11.mBackupVersion = r1
            r11.mAdsSubId = r1
            r11.mHttpResponse = r1
            r11.mNoInitialDataRetryCount = r1
            r11.mNoAppTokenRetryCount = r1
            r11.m511ResponseRetryCount = r1
            r11.mNoResponseRetryCount = r1
            r11.mRcsDisabledStateRetryCount = r1
            r11.mNoRemainingValidityRetryCount = r1
            r11.mCurConfigStartForce = r1
            r11.mIsMobileAutoConfigOngoing = r1
            r11.mIsImsRegiNotifyReceived = r1
            r11.mIsDmaPackageChanged = r1
            r11.mIsSecAndGoogDmaPackageSwitched = r1
            r11.mIsMobileConnectionAvailable = r1
            r11.mIsGeneralErrorRetryTimerRunning = r1
            r11.mIsGeneralErrorRetryFailed = r1
            r11.mNextWorkflow = r0
            r11.mImsRegistrationListener = r0
            r11.mMobileStateCallback = r0
            r11.mGeneralErrorRetryIntent = r0
            r11.mGeneralErrorRetryIntentReceiver = r0
            r12.registerImsRegistrationListener()
            android.content.IntentFilter r0 = new android.content.IntentFilter
            java.lang.String r1 = "com.sec.internal.ims.config.workflow.general_error_max_retry"
            r0.<init>(r1)
            r11.mIntentFilter = r0
            com.sec.internal.ims.config.adapters.XmlParserAdapterMultipleServer r0 = new com.sec.internal.ims.config.adapters.XmlParserAdapterMultipleServer
            r0.<init>()
            r11.mXmlMultipleParser = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowVzw.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, int):void");
    }

    /* access modifiers changed from: protected */
    public void registerImsRegistrationListener() {
        unregisterImsRegistrationListener();
        AnonymousClass1 r0 = new IImsRegistrationListener.Stub() {
            public void onRegistered(ImsRegistration imsRegistration) {
                WorkflowVzw.this.sendEmptyMessage(12);
            }

            public void onDeregistered(ImsRegistration imsRegistration, ImsRegistrationError imsRegistrationError) {
                WorkflowVzw.this.sendEmptyMessage(12);
            }
        };
        this.mImsRegistrationListener = r0;
        this.mRm.registerListener(r0, this.mPhoneId);
    }

    /* access modifiers changed from: protected */
    public void unregisterImsRegistrationListener() {
        removeMessages(12);
        IImsRegistrationListener iImsRegistrationListener = this.mImsRegistrationListener;
        if (iImsRegistrationListener != null) {
            this.mRm.unregisterListener(iImsRegistrationListener, this.mPhoneId);
            this.mImsRegistrationListener = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean registerMobileNetwork() {
        try {
            unregisterMobileNetwork();
            this.mAdsSubId = SimUtil.getSubId();
            this.mNetworkRequest = new NetworkRequest.Builder().addTransportType(0).addCapability(0).setNetworkSpecifier(Integer.toString(this.mAdsSubId)).build();
            ConnectivityManager.NetworkCallback mobileStateCallback = getMobileStateCallback();
            this.mMobileStateCallback = mobileStateCallback;
            this.mConnectivityManager.requestNetwork(this.mNetworkRequest, mobileStateCallback);
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "registerMobileNetwork: registered with ads subId: " + this.mAdsSubId + " instead of this subId: " + SimUtil.getSubId(this.mPhoneId));
            return true;
        } catch (IllegalArgumentException e) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "registerMobileNetwork: can not register: " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public ConnectivityManager.NetworkCallback getMobileStateCallback() {
        return new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                WorkflowVzw workflowVzw = WorkflowVzw.this;
                workflowVzw.mNetwork = network;
                workflowVzw.mIsMobileConnectionAvailable = true;
                String str = WorkflowVzw.LOG_TAG;
                int i = workflowVzw.mPhoneId;
                IMSLog.i(str, i, "mobileStateCallback: onAvailable with network: " + network + " registered with ads subId: " + WorkflowVzw.this.mAdsSubId + " cur ads subId: " + SimUtil.getSubId());
                if (WorkflowVzw.this.mAdsSubId == SimUtil.getSubId()) {
                    WorkflowVzw.this.sendEmptyMessage(3);
                    return;
                }
                IMSLog.i(str, WorkflowVzw.this.mPhoneId, "mobileStateCallback: onAvailable: ads subId is changed: the connection is not available");
                WorkflowVzw.this.sendEmptyMessage(4);
            }

            public void onLost(Network network) {
                WorkflowVzw workflowVzw = WorkflowVzw.this;
                workflowVzw.mNetwork = network;
                workflowVzw.mIsMobileConnectionAvailable = false;
                String str = WorkflowVzw.LOG_TAG;
                int i = workflowVzw.mPhoneId;
                IMSLog.i(str, i, "mobileStateCallback: onLost with network: " + network + " registered with ads subId: " + WorkflowVzw.this.mAdsSubId + " cur ads subId: " + SimUtil.getSubId());
                WorkflowVzw.this.sendEmptyMessage(4);
            }
        };
    }

    /* access modifiers changed from: protected */
    public void unregisterMobileNetwork() {
        try {
            ConnectivityManager.NetworkCallback networkCallback = this.mMobileStateCallback;
            if (networkCallback != null) {
                this.mConnectivityManager.unregisterNetworkCallback(networkCallback);
                IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterMobileNetwork: unregistered");
            }
        } catch (IllegalArgumentException e) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "unregisterMobileNetwork: can not unregister: " + e.getMessage());
        } catch (Throwable th) {
            this.mNetworkRequest = null;
            this.mMobileStateCallback = null;
            throw th;
        }
        this.mNetworkRequest = null;
        this.mMobileStateCallback = null;
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: msg: " + message.what);
        int i2 = message.what;
        if (i2 == 0) {
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 == 2) {
                IMSLog.i(str, this.mPhoneId, "handleMessage: config info is cleared");
                this.mPowerController.lock();
                clearStorage();
                this.mCurConfigStartForce = true;
                this.mStartForce = true;
                if (isGoogDmaPackageInuse(this.mDmaPackage)) {
                    IMSLog.i(str, this.mPhoneId, "handleMessage: config info is cleared: notify autoConfigRemoved");
                    SecImsNotifier.getInstance().notifyRcsAutoConfigurationRemoved(this.mPhoneId);
                }
                this.mPowerController.release();
                return;
            } else if (i2 == 3) {
                removeMessages(4);
                if (this.mIsMobileAutoConfigOngoing) {
                    IMSLog.i(str, this.mPhoneId, "autoConfig: mobile connection is successful: ongoing");
                    return;
                }
                IMSLog.i(str, this.mPhoneId, "autoConfig: mobile connection is successful");
                this.mPowerController.lock();
                this.mIsMobileAutoConfigOngoing = true;
                executeAutoConfig(this.mNextWorkflow);
                if (this.mIsGeneralErrorRetryTimerRunning) {
                    IMSLog.i(str, this.mPhoneId, "autoConfig: mobile connection is successful: generalErrorRetryTimer is running");
                    endMobileAutoConfig();
                } else {
                    endAutoConfig();
                }
                this.mIsConfigOngoing = false;
                this.mPowerController.release();
                return;
            } else if (i2 == 4) {
                removeMessages(4);
                if (this.mIsMobileAutoConfigOngoing) {
                    IMSLog.i(str, this.mPhoneId, "autoConfig: mobile connection is failure: ongoing");
                    return;
                }
                IMSLog.i(str, this.mPhoneId, "autoConfig: mobile connection is failure");
                this.mPowerController.lock();
                this.mIsMobileAutoConfigOngoing = true;
                endFailureAutoConfig();
                this.mIsConfigOngoing = false;
                this.mPowerController.release();
                return;
            } else if (i2 != 5) {
                switch (i2) {
                    case 11:
                        if (this.mIsConfigOngoing) {
                            IMSLog.i(str, this.mPhoneId, "curConfig: ongoing");
                            return;
                        }
                        int i3 = this.mPhoneId;
                        IMSLog.i(str, i3, "curConfig: start curConfig with curConfigStartForce: " + this.mCurConfigStartForce);
                        this.mPowerController.lock();
                        this.mIsConfigOngoing = true;
                        executeCurConfig();
                        endCurConfig();
                        this.mPowerController.release();
                        return;
                    case 12:
                        IMSLog.i(str, this.mPhoneId, "handleMessage: ims regi status is changed");
                        this.mPowerController.lock();
                        this.mIsImsRegiNotifyReceived = true;
                        sendRestartAutoConfigMsg();
                        this.mPowerController.release();
                        return;
                    case 13:
                        removeMessages(13);
                        if (this.mIsConfigOngoing) {
                            IMSLog.i(str, this.mPhoneId, "autoConfig: generalErrorRetryTimer is expired: ongoing");
                            return;
                        }
                        IMSLog.i(str, this.mPhoneId, "autoConfig: generalErrorRetryTimer is expired");
                        this.mPowerController.lock();
                        if (this.mIsGeneralErrorRetryTimerRunning) {
                            this.mIsConfigOngoing = true;
                            startMobileAutoConfig();
                        }
                        this.mPowerController.release();
                        return;
                    case 14:
                        IMSLog.i(str, this.mPhoneId, "handleMessage: cleanup");
                        this.mPowerController.lock();
                        cancelValidityTimer();
                        stopGeneralErrorRetryTimer();
                        unregisterImsRegistrationListener();
                        removeMessages(3);
                        removeMessages(4);
                        this.mIsConfigOngoing = false;
                        this.mPowerController.release();
                        return;
                    case 15:
                        IMSLog.i(str, this.mPhoneId, "handleMessage: ads is changed");
                        this.mPowerController.lock();
                        this.mModule.getHandler().sendMessage(obtainMessage(17, Integer.valueOf(this.mPhoneId)));
                        this.mPowerController.release();
                        return;
                    case 16:
                        if (message.obj == null) {
                            IMSLog.i(str, this.mPhoneId, "handleMessage: client info is empty");
                            return;
                        }
                        IMSLog.i(str, this.mPhoneId, "handleMessage: client info is changed");
                        this.mPowerController.lock();
                        Bundle bundle = (Bundle) message.obj;
                        if (isGoogDmaPackageInuse(this.mDmaPackage) && !this.mIsSecAndGoogDmaPackageSwitched && this.mParamHandler.setRcsClientConfiguration(bundle.getString("rcsVersion"), bundle.getString("rcsProfile"), bundle.getString("clientVendor"), bundle.getString("clientVersion"))) {
                            IMSLog.i(str, this.mPhoneId, "handleMessage: client info is changed: need autoConfig to use the changed client info");
                            this.mStartForce = true;
                            this.mModule.getHandler().sendMessageDelayed(obtainMessage(2, this.mPhoneId, 0, (Object) null), 1000);
                        }
                        this.mPowerController.release();
                        return;
                    default:
                        IMSLog.i(str, this.mPhoneId, "handleMessage: unknown msg");
                        super.handleMessage(message);
                        return;
                }
            } else {
                IMSLog.i(str, this.mPhoneId, "handleMessage: dmaPackage is changed");
                this.mPowerController.lock();
                checkAndUpdateDmaPackageInfo();
                this.mPowerController.release();
                return;
            }
        }
        if (this.mIsConfigOngoing) {
            IMSLog.i(str, this.mPhoneId, "autoConfig: ongoing");
            return;
        }
        int i4 = this.mPhoneId;
        IMSLog.i(str, i4, "autoConfig: start autoConfig with startForce: " + this.mStartForce);
        this.mPowerController.lock();
        this.mIsConfigOngoing = true;
        if (scheduleAutoconfig()) {
            initAutoConfig();
            startMobileAutoConfig();
        } else {
            IMSLog.i(str, this.mPhoneId, "autoConfig: schedule: false");
            endAutoConfig();
            this.mIsConfigOngoing = false;
        }
        this.mPowerController.release();
    }

    public void startCurConfig() {
        sendEmptyMessage(11);
    }

    /* access modifiers changed from: protected */
    public void executeCurConfig() {
        this.mOldVersion = getVersion();
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        if (this.mCurConfigStartForce) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "curConfig: need force localconfig info");
            startLocalConfig(WorkflowBase.OpMode.DISABLE_TEMPORARY.value(), WorkflowBase.OpMode.NONE);
        } else if (isNonActiveVersion(this.mOldVersion) || ((isActiveVersion(this.mOldVersion) && isValidRcsDisabledState(rcsDisabledState)) || this.mOldVersion == 101)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "curConfig: need localconfig info");
            startLocalConfig(this.mOldVersion, rcsDisabledState);
        }
    }

    /* access modifiers changed from: protected */
    public void startLocalConfig(int i, WorkflowBase.OpMode opMode) {
        Map<String, String> treeMap = new TreeMap<>();
        int i2 = 0;
        while (true) {
            if (i2 >= 5) {
                break;
            }
            treeMap = loadLocalConfig();
            if (treeMap == null) {
                i2++;
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "startLocalConfig: load localconfig");
                checkAndKeepData(treeMap);
                if (isValidRcsDisabledState(opMode)) {
                    checkAndKeepRcsDisabledState(treeMap, opMode);
                }
                checkAndKeepSpgUrl(treeMap);
                checkAndKeepRcsClientConfiguration(treeMap);
                clearStorage(DiagnosisConstants.RCSA_TDRE.UPDATE_LOCAL_CONFIG);
                this.mStorage.writeAll(treeMap);
                this.mSharedInfo.setParsedXml(treeMap);
                int validity = getValidity();
                WorkflowBase.OpMode opMode2 = WorkflowBase.OpMode.DISABLE_TEMPORARY;
                if (validity > opMode2.value()) {
                    setNextAutoconfigTimeAfter(getValidity());
                    setValidityTimer(getValidity());
                } else {
                    setNextAutoconfigTime((long) opMode2.value());
                    cancelValidityTimer();
                }
                setVersionBackup(i);
            }
        }
        if (treeMap == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "startLocalConfig: can not load localconfig");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0071, code lost:
        if (r4 != null) goto L_0x0073;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.util.Map<java.lang.String, java.lang.String> loadLocalConfig() {
        /*
            r11 = this;
            r0 = 0
            com.google.gson.stream.JsonReader r1 = new com.google.gson.stream.JsonReader     // Catch:{ JsonParseException | IOException -> 0x00fe }
            java.io.BufferedReader r2 = new java.io.BufferedReader     // Catch:{ JsonParseException | IOException -> 0x00fe }
            java.io.InputStreamReader r3 = new java.io.InputStreamReader     // Catch:{ JsonParseException | IOException -> 0x00fe }
            java.io.InputStream r4 = r11.getInputStream()     // Catch:{ JsonParseException | IOException -> 0x00fe }
            r3.<init>(r4)     // Catch:{ JsonParseException | IOException -> 0x00fe }
            r2.<init>(r3)     // Catch:{ JsonParseException | IOException -> 0x00fe }
            r1.<init>(r2)     // Catch:{ JsonParseException | IOException -> 0x00fe }
            com.google.gson.JsonParser r2 = new com.google.gson.JsonParser     // Catch:{ all -> 0x00f4 }
            r2.<init>()     // Catch:{ all -> 0x00f4 }
            com.google.gson.JsonElement r2 = r2.parse(r1)     // Catch:{ all -> 0x00f4 }
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ all -> 0x00f4 }
            java.lang.String r4 = "base"
            com.google.gson.JsonElement r3 = r3.get(r4)     // Catch:{ all -> 0x00f4 }
            com.google.gson.JsonObject r3 = r3.getAsJsonObject()     // Catch:{ all -> 0x00f4 }
            com.google.gson.JsonObject r2 = r2.getAsJsonObject()     // Catch:{ all -> 0x00f4 }
            java.util.Set r2 = r2.entrySet()     // Catch:{ all -> 0x00f4 }
            java.util.Iterator r2 = r2.iterator()     // Catch:{ all -> 0x00f4 }
            r4 = r0
        L_0x0038:
            boolean r5 = r2.hasNext()     // Catch:{ all -> 0x00f4 }
            if (r5 == 0) goto L_0x0073
            java.lang.Object r5 = r2.next()     // Catch:{ all -> 0x00f4 }
            java.util.Map$Entry r5 = (java.util.Map.Entry) r5     // Catch:{ all -> 0x00f4 }
            java.lang.Object r6 = r5.getKey()     // Catch:{ all -> 0x00f4 }
            java.lang.String r6 = (java.lang.String) r6     // Catch:{ all -> 0x00f4 }
            java.lang.String r6 = r6.trim()     // Catch:{ all -> 0x00f4 }
            java.lang.String r7 = ","
            java.lang.String[] r6 = r6.split(r7)     // Catch:{ all -> 0x00f4 }
            int r7 = r6.length     // Catch:{ all -> 0x00f4 }
            r8 = 0
        L_0x0056:
            if (r8 >= r7) goto L_0x0071
            r9 = r6[r8]     // Catch:{ all -> 0x00f4 }
            java.lang.String r10 = "vzw_up"
            boolean r9 = android.text.TextUtils.equals(r10, r9)     // Catch:{ all -> 0x00f4 }
            if (r9 == 0) goto L_0x006e
            java.lang.Object r4 = r5.getValue()     // Catch:{ all -> 0x00f4 }
            com.google.gson.JsonElement r4 = (com.google.gson.JsonElement) r4     // Catch:{ all -> 0x00f4 }
            com.google.gson.JsonObject r4 = r4.getAsJsonObject()     // Catch:{ all -> 0x00f4 }
            goto L_0x0071
        L_0x006e:
            int r8 = r8 + 1
            goto L_0x0056
        L_0x0071:
            if (r4 == 0) goto L_0x0038
        L_0x0073:
            r1.close()     // Catch:{ JsonParseException | IOException -> 0x00fe }
            if (r3 == 0) goto L_0x00ea
            if (r4 != 0) goto L_0x007b
            goto L_0x00ea
        L_0x007b:
            java.util.TreeMap r11 = new java.util.TreeMap
            r11.<init>()
            java.util.Set r0 = r3.entrySet()
            java.util.Iterator r0 = r0.iterator()
        L_0x0088:
            boolean r1 = r0.hasNext()
            java.lang.String r2 = "root/"
            if (r1 == 0) goto L_0x00b6
            java.lang.Object r1 = r0.next()
            java.util.Map$Entry r1 = (java.util.Map.Entry) r1
            java.lang.Object r3 = r1.getValue()
            com.google.gson.JsonElement r3 = (com.google.gson.JsonElement) r3
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r2)
            java.lang.Object r1 = r1.getKey()
            java.lang.String r1 = (java.lang.String) r1
            r5.append(r1)
            java.lang.String r1 = r5.toString()
            com.sec.internal.ims.config.workflow.WorkflowLocalFile.path(r3, r1, r11)
            goto L_0x0088
        L_0x00b6:
            java.util.Set r0 = r4.entrySet()
            java.util.Iterator r0 = r0.iterator()
        L_0x00be:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x00e9
            java.lang.Object r1 = r0.next()
            java.util.Map$Entry r1 = (java.util.Map.Entry) r1
            java.lang.Object r3 = r1.getValue()
            com.google.gson.JsonElement r3 = (com.google.gson.JsonElement) r3
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r2)
            java.lang.Object r1 = r1.getKey()
            java.lang.String r1 = (java.lang.String) r1
            r4.append(r1)
            java.lang.String r1 = r4.toString()
            com.sec.internal.ims.config.workflow.WorkflowLocalFile.path(r3, r1, r11)
            goto L_0x00be
        L_0x00e9:
            return r11
        L_0x00ea:
            java.lang.String r1 = LOG_TAG
            int r11 = r11.mPhoneId
            java.lang.String r2 = "loadLocalConfig: base/target object is empty"
            com.sec.internal.log.IMSLog.i(r1, r11, r2)
            return r0
        L_0x00f4:
            r2 = move-exception
            r1.close()     // Catch:{ all -> 0x00f9 }
            goto L_0x00fd
        L_0x00f9:
            r1 = move-exception
            r2.addSuppressed(r1)     // Catch:{ JsonParseException | IOException -> 0x00fe }
        L_0x00fd:
            throw r2     // Catch:{ JsonParseException | IOException -> 0x00fe }
        L_0x00fe:
            r1 = move-exception
            java.lang.String r2 = LOG_TAG
            int r11 = r11.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "loadLocalConfig: can not open/parse localconfig: "
            r3.append(r4)
            java.lang.String r1 = r1.getMessage()
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            com.sec.internal.log.IMSLog.i(r2, r11, r1)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowVzw.loadLocalConfig():java.util.Map");
    }

    /* access modifiers changed from: package-private */
    public InputStream getInputStream() {
        return this.mContext.getResources().openRawResource(this.mContext.getResources().getIdentifier("localconfig", "raw", this.mContext.getPackageName()));
    }

    private void endCurConfig() {
        this.mNewVersion = getVersion();
        this.mBackupVersion = getParsedIntVersionBackup();
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "curConfig: oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion + " backupVersion: " + this.mBackupVersion);
        checkAndUpdateDmaPackage();
        checkAndUpdateRcsClientConfiguration();
        if (isGoogDmaPackageInuse(this.mDmaPackage) && !this.mIsSecAndGoogDmaPackageSwitched) {
            boolean z = this.mNewVersion == 59 && isActiveVersion(this.mBackupVersion);
            if (z) {
                setVersion(this.mBackupVersion);
            }
            int i2 = this.mPhoneId;
            IMSLog.i(str, i2, "curConfig: notify preConfig: isBackupVersionUpdateNeeded: " + z);
            SecImsNotifier.getInstance().notifyRcsPreConfigurationReceived(this.mPhoneId, this.mParamHandler.getProvisioningXml(false));
            if (z) {
                setVersion(this.mNewVersion);
            }
        }
        this.mCurConfigStartForce = false;
        setCompleted(true);
        this.mModule.getHandler().sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        int i3 = this.mPhoneId;
        IMSLog.i(str, i3, "curConfig: isImsRegiNotifyReceived: " + this.mIsImsRegiNotifyReceived);
        if (this.mIsImsRegiNotifyReceived) {
            sendRestartAutoConfigMsg();
        } else {
            sendMessageDelayed(obtainMessage(12), SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
        }
    }

    /* access modifiers changed from: protected */
    public void sendRestartAutoConfigMsg() {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "curConfig: send restart autoConfig msg: isConfigOngoing: " + this.mIsConfigOngoing);
        if (this.mIsConfigOngoing) {
            this.mModule.getHandler().sendMessage(obtainMessage(19, this.mPhoneId, 0, (Object) null));
            unregisterImsRegistrationListener();
            this.mIsConfigOngoing = false;
        }
    }

    /* access modifiers changed from: protected */
    public void startGeneralErrorRetryTimer(long j) {
        stopGeneralErrorRetryTimer();
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "startGeneralErrorRetryTimer: retryTimer: " + j);
        this.mIsGeneralErrorRetryTimerRunning = true;
        if (j == 0) {
            sendMessageDelayed(obtainMessage(13), 1000);
            return;
        }
        BroadcastReceiver generalErrorRetryIntentReceiver = getGeneralErrorRetryIntentReceiver();
        this.mGeneralErrorRetryIntentReceiver = generalErrorRetryIntentReceiver;
        this.mContext.registerReceiver(generalErrorRetryIntentReceiver, this.mIntentFilter);
        Intent intent = new Intent(INTENT_GENERAL_ERROR_MAX_RETRY);
        intent.putExtra(PhoneConstants.PHONE_KEY, this.mPhoneId);
        intent.setPackage(this.mContext.getPackageName());
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, this.mPhoneId, intent, 33554432);
        this.mGeneralErrorRetryIntent = broadcast;
        AlarmTimer.start(this.mContext, broadcast, j);
    }

    /* access modifiers changed from: protected */
    public BroadcastReceiver getGeneralErrorRetryIntentReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (TextUtils.equals(WorkflowVzw.INTENT_GENERAL_ERROR_MAX_RETRY, intent.getAction())) {
                    int intExtra = intent.getIntExtra(PhoneConstants.PHONE_KEY, 0);
                    String str = WorkflowVzw.LOG_TAG;
                    int i = WorkflowVzw.this.mPhoneId;
                    IMSLog.i(str, i, "generalErrorRetryIntentReceiver: received with phoneId: " + intExtra);
                    WorkflowVzw workflowVzw = WorkflowVzw.this;
                    if (intExtra == workflowVzw.mPhoneId) {
                        workflowVzw.sendEmptyMessage(13);
                    }
                }
            }
        };
    }

    /* access modifiers changed from: protected */
    public void stopGeneralErrorRetryTimer() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "stopGeneralErrorRetryTimer:");
        removeMessages(13);
        try {
            PendingIntent pendingIntent = this.mGeneralErrorRetryIntent;
            if (pendingIntent != null) {
                AlarmTimer.stop(this.mContext, pendingIntent);
            }
            BroadcastReceiver broadcastReceiver = this.mGeneralErrorRetryIntentReceiver;
            if (broadcastReceiver != null) {
                this.mContext.unregisterReceiver(broadcastReceiver);
            }
        } catch (IllegalArgumentException e) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "stopGeneralErrorRetryTimer: can not stop/unregister: " + e.getMessage());
        } catch (Throwable th) {
            this.mGeneralErrorRetryIntent = null;
            this.mGeneralErrorRetryIntentReceiver = null;
            this.mIsGeneralErrorRetryTimerRunning = false;
            throw th;
        }
        this.mGeneralErrorRetryIntent = null;
        this.mGeneralErrorRetryIntentReceiver = null;
        this.mIsGeneralErrorRetryTimerRunning = false;
    }

    /* access modifiers changed from: protected */
    public boolean scheduleAutoconfig() {
        this.mOldVersion = getVersion();
        int parsedIntVersionBackup = getParsedIntVersionBackup();
        this.mBackupVersion = parsedIntVersionBackup;
        int i = this.mOldVersion;
        if (i != 59) {
            parsedIntVersionBackup = i;
        }
        this.mCurVersion = parsedIntVersionBackup;
        checkAndUpdateDmaPackage();
        checkAndUpdateRcsClientConfiguration();
        checkAndUpdateDmaPackageInfo();
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "autoConfig: schedule: oldVersion: " + this.mOldVersion + " curVersion: " + this.mCurVersion + " backupVersion: " + this.mBackupVersion);
        boolean z = false;
        if (!needScheduleAutoconfig(this.mPhoneId)) {
            return false;
        }
        String lastSwVersion = getLastSwVersion();
        String str2 = ConfigConstants.BUILD.TERMINAL_SW_VERSION;
        if (!TextUtils.equals(lastSwVersion, str2) && isDmaPackageInuse(this.mDmaPackage)) {
            IMSLog.i(str, this.mPhoneId, "autoConfig: schedule: software version is changed: force autoConfig");
            this.mStartForce = true;
            setLastSwVersion(str2);
            cancelValidityTimer();
            return true;
        } else if (this.mStartForce) {
            IMSLog.i(str, this.mPhoneId, "autoConfig: schedule: startForce is true: force autoConfig");
            cancelValidityTimer();
            return true;
        } else if (this.mIsDmaPackageChanged) {
            this.mStartForce = isNonActiveVersion(this.mCurVersion);
            IMSLog.i(str, this.mPhoneId, "autoConfig: schedule: dmaPackage is changed: need autoConfig with startForce: " + this.mStartForce);
            return true;
        } else if (this.mCurVersion == WorkflowBase.OpMode.DISABLE_PERMANENTLY.value() || this.mCurVersion == WorkflowBase.OpMode.DISABLE.value()) {
            IMSLog.i(str, this.mPhoneId, "autoConfig: schedule: disable_permanently/disable opMode: skip autoConfig");
            return false;
        } else if (this.mIsGeneralErrorRetryFailed) {
            IMSLog.i(str, this.mPhoneId, "autoConfig: schedule: generalErrorRetry is failed: skip autoConfig");
            return false;
        } else {
            long nextAutoconfigTime = getNextAutoconfigTime();
            int remainingValidityTime = getRemainingValidityTime(nextAutoconfigTime);
            WorkflowBase.OpMode opMode = WorkflowBase.OpMode.DISABLE_TEMPORARY;
            if (remainingValidityTime <= opMode.value()) {
                if (isNonActiveVersion(this.mCurVersion) || this.mStartForce) {
                    z = true;
                }
                this.mStartForce = z;
                IMSLog.i(str, this.mPhoneId, "autoConfig: schedule: validity is expired: need autoConfig with startForce: " + this.mStartForce);
                return true;
            }
            if (nextAutoconfigTime > ((long) opMode.value())) {
                IMSLog.i(str, this.mPhoneId, "autoConfig: schedule: validity is not expired: skip autoConfig");
                setValidityTimer(remainingValidityTime);
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public int getRemainingValidityTime(long j) {
        int time = (int) ((j - new Date().getTime()) / 1000);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "autoConfig: getRemainingValidityTime: nextAutoconfigTime: " + j + " remainingValidityTime: " + time);
        return time;
    }

    /* access modifiers changed from: protected */
    public void setValidityTimer(int i) {
        cancelValidityTimer();
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "setValidityTimer: validityPeriod: " + i);
        if (i > WorkflowBase.OpMode.DISABLE_TEMPORARY.value()) {
            Intent intent = new Intent("com.sec.internal.ims.config.workflow.validity_timeout");
            intent.putExtra(PhoneConstants.PHONE_KEY, this.mPhoneId);
            intent.setPackage(this.mContext.getPackageName());
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, this.mPhoneId, intent, 33554432);
            this.mValidityIntent = broadcast;
            AlarmTimer.start(this.mContext, broadcast, ((long) i) * 1000);
        }
    }

    /* access modifiers changed from: protected */
    public void initAutoConfig() {
        this.mNextWorkflow = new Initialize();
        this.mHttpResponse = 0;
        this.mNoInitialDataRetryCount = 0;
        this.mNoAppTokenRetryCount = 0;
        this.m511ResponseRetryCount = 0;
        this.mNoResponseRetryCount = 0;
        this.mRcsDisabledStateRetryCount = 0;
        this.mNoRemainingValidityRetryCount = 0;
        this.mAppToken = "";
        this.mNetwork = null;
    }

    /* access modifiers changed from: protected */
    public void startMobileAutoConfig() {
        this.mIsMobileAutoConfigOngoing = false;
        this.mIsMobileConnectionAvailable = false;
        stopGeneralErrorRetryTimer();
        checkAndUpdateDmaPackageInfo();
        if (TextUtils.isEmpty(this.mDmaPackage) || ((isGoogDmaPackageInuse(this.mDmaPackage) && !this.mIsSecAndGoogDmaPackageSwitched && this.mParamHandler.isRcsClientConfigurationInfoNotSet()) || !registerMobileNetwork())) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "mobileAutoConfig: dmaPackage/client info/mobile network is not available");
            sendEmptyMessage(4);
            return;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "mobileAutoConfig: start using mobile network");
        sendEmptyMessageDelayed(4, SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
    }

    /* access modifiers changed from: protected */
    public void endMobileAutoConfig() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "mobileAutoConfig: end using mobile network");
        unregisterMobileNetwork();
        this.mIsMobileConnectionAvailable = false;
    }

    public void onADSChanged() {
        if (hasMessages(4) && this.mIsConfigOngoing) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onADSChanged: waiting for the result of mobile connection");
            removeMessages(4);
            sendEmptyMessage(4);
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "onADSChanged: send ads changed msg");
        this.mTelephonyAdapter.onADSChanged();
        this.mHttp.close();
        endMobileAutoConfig();
        sendEmptyMessage(15);
    }

    /* access modifiers changed from: protected */
    public void executeAutoConfig(WorkflowBase.Workflow workflow) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "autoConfig: execute: next: " + workflow);
        while (workflow != null) {
            try {
                workflow = workflow.run();
            } catch (NoInitialDataException unused) {
                if (this.mNoInitialDataRetryCount >= 5 || !this.mIsMobileConnectionAvailable) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "autoConfig: execute: noInitialDataException: no need to retry anymore");
                    this.mNextWorkflow = null;
                } else {
                    String str2 = LOG_TAG;
                    int i2 = this.mPhoneId;
                    IMSLog.i(str2, i2, "autoConfig: execute: noInitialDataException: noInitialDataRetryCount: " + this.mNoInitialDataRetryCount + " wait 10 seconds and retry");
                    startGeneralErrorRetryTimer(10000);
                    this.mNoInitialDataRetryCount = this.mNoInitialDataRetryCount + 1;
                    this.mNextWorkflow = new Initialize();
                }
                workflow = new Finish();
            } catch (Exception e) {
                String str3 = LOG_TAG;
                int i3 = this.mPhoneId;
                IMSLog.i(str3, i3, "autoConfig: execute: exception: " + e.getMessage());
                this.mNextWorkflow = null;
                workflow = new Finish();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void endAutoConfig() {
        endMobileAutoConfig();
        this.mNewVersion = getVersion();
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        if (isNonActiveVersion(this.mNewVersion) || ((isActiveVersion(this.mNewVersion) && isValidRcsDisabledState(rcsDisabledState)) || this.mNewVersion == 101)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "autoConfig: end: need localconfig info");
            startLocalConfig(this.mNewVersion, rcsDisabledState);
            this.mNewVersion = getVersion();
        }
        this.mBackupVersion = getParsedIntVersionBackup();
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "autoConfig: end: oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion + " backupVersion: " + this.mBackupVersion);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPhoneId);
        sb.append("OV:");
        sb.append(this.mOldVersion);
        sb.append(",NV:");
        sb.append(this.mNewVersion);
        IMSLog.c(LogClass.WFVM_LAST_VERSION_INFO, sb.toString());
        addEventLog(str + ": OV: " + this.mOldVersion + " NV: " + this.mNewVersion);
        String spgUrl = getSpgUrl();
        String spgParamsUrl = getSpgParamsUrl();
        boolean isEmpty = TextUtils.isEmpty(spgUrl);
        boolean isEmpty2 = TextUtils.isEmpty(spgParamsUrl);
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "autoConfig: end: rcsDisabledState: " + displayRcsDisabledState(rcsDisabledState) + " isSpgUrlEmpty: " + isEmpty + " isSpgParamsUrlEmpty: " + isEmpty2);
        int i3 = this.mPhoneId;
        StringBuilder sb2 = new StringBuilder();
        sb2.append("autoConfig: end: spgUrl: ");
        sb2.append(spgUrl);
        sb2.append(" spgParamsUrl: ");
        sb2.append(spgParamsUrl);
        IMSLog.s(str, i3, sb2.toString());
        IMSLog.c(LogClass.WFVM_PARAM_INFO, this.mPhoneId + "DV:" + displayRcsDisabledState(rcsDisabledState) + ",SU:" + isEmpty + ",SPU:" + isEmpty2);
        addEventLog(str + ": rcsDisabledState: " + displayRcsDisabledState(rcsDisabledState) + " isSpgUrlEmpty: " + isEmpty + " isSpgParamsUrlEmpty: " + isEmpty2);
        notifyAutoConfig();
        this.mStartForce = false;
        this.mIsDmaPackageChanged = false;
        setCompleted(true);
        this.mModule.getHandler().sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        if (isDmaPackageInuse(this.mDmaPackage) && this.mIsSecAndGoogDmaPackageSwitched) {
            this.mCurConfigStartForce = true;
            this.mStartForce = true;
            IMSLog.i(str, this.mPhoneId, "autoConfig: end: need autoConfig to use the changed dmaPackage");
            this.mModule.getHandler().sendMessageDelayed(obtainMessage(2, this.mPhoneId, 0, (Object) null), 1000);
        }
        this.mIsSecAndGoogDmaPackageSwitched = false;
    }

    /* access modifiers changed from: protected */
    public void endFailureAutoConfig() {
        endMobileAutoConfig();
        this.mNewVersion = getVersion();
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        if (isNonActiveVersion(this.mNewVersion) || ((isActiveVersion(this.mNewVersion) && isValidRcsDisabledState(rcsDisabledState)) || this.mNewVersion == 101)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "autoConfig: endfailure: need localconfig info");
            startLocalConfig(this.mNewVersion, rcsDisabledState);
            this.mNewVersion = getVersion();
        }
        this.mBackupVersion = getParsedIntVersionBackup();
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "autoConfig: endfailure: oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion + " backupVersion: " + this.mBackupVersion);
        setCompleted(true);
        this.mModule.getHandler().sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        IMSLog.i(str, this.mPhoneId, "autoConfig: endfailure: need autoConfig next time with suitable network");
        this.mModule.getHandler().sendMessage(obtainMessage(17, Integer.valueOf(this.mPhoneId)));
    }

    /* access modifiers changed from: protected */
    public void notifyAutoConfig() {
        if (isGoogDmaPackageInuse(this.mDmaPackage) && !this.mIsSecAndGoogDmaPackageSwitched) {
            int i = this.mLastErrorCode;
            if (i == 987 || i == 200) {
                boolean z = this.mNewVersion == 59 && isActiveVersion(this.mBackupVersion);
                if (z) {
                    setVersion(this.mBackupVersion);
                }
                IMSLog.i(LOG_TAG, this.mPhoneId, "autoConfig: notify: autoConfigReceived");
                SecImsNotifier.getInstance().notifyRcsAutoConfigurationReceived(this.mPhoneId, this.mParamHandler.getProvisioningXml(false), false);
                if (z) {
                    setVersion(this.mNewVersion);
                    return;
                }
                return;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "autoConfig: notify: autoConfigError");
            SecImsNotifier.getInstance().notifyRcsAutoConfigurationErrorReceived(this.mPhoneId, this.mLastErrorCode, this.mLastErrorMessage);
        } else if (isSecDmaPackageInuse(this.mDmaPackage) && !this.mIsSecAndGoogDmaPackageSwitched && ImsProfile.isRcsUp2Profile(this.mCurRcsProfile)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "autoConfig: notify: autoConfigCompleted");
            this.mTelephonyAdapter.notifyAutoConfigurationListener(52, isActiveVersion(this.mNewVersion));
        }
    }

    /* access modifiers changed from: protected */
    public boolean isActiveVersion(int i) {
        return i >= WorkflowBase.OpMode.ACTIVE.value() && i != 59;
    }

    /* access modifiers changed from: protected */
    public boolean isNonActiveVersion(int i) {
        return i <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value();
    }

    /* access modifiers changed from: protected */
    public boolean isDmaPackageInuse(String str) {
        return isSecDmaPackageInuse(str) || isGoogDmaPackageInuse(str);
    }

    /* access modifiers changed from: protected */
    public boolean isSecDmaPackageInuse(String str) {
        return !TextUtils.isEmpty(str) && TextUtils.equals(str, PackageUtils.getMsgAppPkgName(this.mContext));
    }

    /* access modifiers changed from: protected */
    public boolean isGoogDmaPackageInuse(String str) {
        String string = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.GOOG_MESSAGE_APP_PACKAGE, "");
        return RcsUtils.isImsSingleRegiRequired(this.mContext, this.mPhoneId) && !TextUtils.isEmpty(str) && !TextUtils.isEmpty(string) && TextUtils.equals(str, string);
    }

    /* access modifiers changed from: protected */
    public boolean isSecAndGoogDmaPackageSwitched(String str, String str2) {
        return RcsUtils.isImsSingleRegiRequired(this.mContext, this.mPhoneId) && isDmaPackageInuse(str) && isDmaPackageInuse(str2) && !TextUtils.equals(str, str2);
    }

    /* access modifiers changed from: protected */
    public boolean isNonSecGoogDmaPackageInuse(String str) {
        return RcsUtils.isImsSingleRegiRequired(this.mContext, this.mPhoneId) && !isDmaPackageInuse(str);
    }

    public void setRcsClientConfiguration(String str, String str2, String str3, String str4, String str5) {
        Bundle bundle = new Bundle();
        bundle.putString("rcsVersion", str);
        bundle.putString("rcsProfile", str2);
        bundle.putString("clientVendor", str3);
        bundle.putString("clientVersion", str4);
        sendMessage(obtainMessage(16, bundle));
    }

    /* access modifiers changed from: protected */
    public boolean isValidRcsDisabledState(WorkflowBase.OpMode opMode) {
        return opMode == WorkflowBase.OpMode.TURNEDOFF_BY_RCS_DISABLED_STATE || super.isValidRcsDisabledState(opMode);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState() {
        return super.getRcsDisabledState(ConfigConstants.CONFIG_TYPE.STORAGE_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, (Map<String, String>) null);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState(Map<String, String> map) {
        return super.getRcsDisabledState(ConfigConstants.CONFIG_TYPE.PARSEDXML_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, map);
    }

    /* access modifiers changed from: protected */
    public void setRcsDisabledState(String str) {
        this.mStorage.write(ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, str);
    }

    /* access modifiers changed from: protected */
    public String getSpgUrl() {
        return this.mStorage.read(ConfigConstants.PATH.SPG_URL);
    }

    /* access modifiers changed from: protected */
    public String getSpgParamsUrl() {
        return this.mStorage.read(ConfigConstants.PATH.SPG_PARAMS_URL);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode convertRcsDisabledStateToOpMode(String str) {
        if (TextUtils.equals(String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()), str)) {
            return WorkflowBase.OpMode.TURNEDOFF_BY_RCS_DISABLED_STATE;
        }
        return super.convertRcsDisabledStateToOpMode(str);
    }

    /* access modifiers changed from: protected */
    public int convertRcsDisabledStateToValue(WorkflowBase.OpMode opMode) {
        if (opMode == WorkflowBase.OpMode.TURNEDOFF_BY_RCS_DISABLED_STATE) {
            return WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value();
        }
        return super.convertRcsDisabledStateToValue(opMode);
    }

    /* access modifiers changed from: protected */
    public String convertRcsStateWithSpecificParam(int i) {
        if (!isDmaPackageInuse(this.mDmaPackage) || this.mIsSecAndGoogDmaPackageSwitched) {
            return String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value());
        }
        if (this.mStartForce) {
            return String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        }
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        if (isValidRcsDisabledState(rcsDisabledState)) {
            return String.valueOf(convertRcsDisabledStateToValue(rcsDisabledState));
        }
        return String.valueOf(i);
    }

    /* access modifiers changed from: protected */
    public String displayRcsDisabledState(WorkflowBase.OpMode opMode) {
        int convertRcsDisabledStateToValue = convertRcsDisabledStateToValue(opMode);
        return convertRcsDisabledStateToValue == WorkflowBase.OpMode.NONE.value() ? "" : String.valueOf(convertRcsDisabledStateToValue);
    }

    /* access modifiers changed from: protected */
    public void checkAndKeepData(Map<String, String> map) {
        String str;
        String token = getToken();
        if (TextUtils.isEmpty(token)) {
            token = "";
        }
        map.put("root/token/token", token);
        int validity = getValidity();
        WorkflowBase.OpMode opMode = WorkflowBase.OpMode.DISABLE_TEMPORARY;
        if (validity > opMode.value()) {
            str = String.valueOf(getValidity());
        } else {
            str = String.valueOf(opMode.value());
        }
        map.put("root/vers/validity", str);
    }

    /* access modifiers changed from: protected */
    public void checkAndKeepRcsDisabledState(Map<String, String> map, WorkflowBase.OpMode opMode) {
        map.put(ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, String.valueOf(convertRcsDisabledStateToValue(opMode)));
    }

    /* access modifiers changed from: protected */
    public void checkAndKeepSpgUrl(Map<String, String> map) {
        String spgUrl = getSpgUrl();
        String str = "";
        if (TextUtils.isEmpty(spgUrl)) {
            spgUrl = str;
        }
        map.put(ConfigConstants.PATH.SPG_URL, spgUrl);
        String spgParamsUrl = getSpgParamsUrl();
        if (!TextUtils.isEmpty(spgParamsUrl)) {
            str = spgParamsUrl;
        }
        map.put(ConfigConstants.PATH.SPG_PARAMS_URL, str);
    }

    /* access modifiers changed from: protected */
    public void checkAndKeepRcsClientConfiguration(Map<String, String> map) {
        String rcsVersion = this.mParamHandler.getRcsVersion(false);
        String str = "";
        if (TextUtils.isEmpty(rcsVersion)) {
            rcsVersion = str;
        }
        map.put(ConfigConstants.PATH.INFO_RCS_VERSION, rcsVersion);
        String rcsProfile = this.mParamHandler.getRcsProfile(false);
        if (TextUtils.isEmpty(rcsProfile)) {
            rcsProfile = str;
        }
        map.put(ConfigConstants.PATH.INFO_RCS_PROFILE, rcsProfile);
        String clientVendor = this.mParamHandler.getClientVendor(false);
        if (TextUtils.isEmpty(clientVendor)) {
            clientVendor = str;
        }
        map.put(ConfigConstants.PATH.INFO_CLIENT_VENDOR, clientVendor);
        String clientVersion = this.mParamHandler.getClientVersion(false);
        if (!TextUtils.isEmpty(clientVersion)) {
            str = clientVersion;
        }
        map.put(ConfigConstants.PATH.INFO_CLIENT_VERSION, str);
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateData(Map<String, String> map) {
        String token = getToken(map);
        if (!TextUtils.isEmpty(token) && !TextUtils.equals(token, getToken())) {
            setToken(token, DiagnosisConstants.RCSA_TDRE.UPDATE_TOKEN);
        }
        int validity = getValidity(map);
        if (validity != getValidity()) {
            setValidity(Math.max(validity, WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
        }
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateDmaPackage() {
        this.mDmaPackage = TextUtils.isEmpty(this.mDmaPackage) ? ConfigUtil.getDmaPackage(this.mContext, this.mPhoneId) : this.mDmaPackage;
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateDmaPackageInfo() {
        String dmaPackage = ConfigUtil.getDmaPackage(this.mContext, this.mPhoneId);
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "checkAndUpdateDmaPackageInfo: dmaPackage: " + this.mDmaPackage + " cur dmaPackage: " + dmaPackage);
        if (!TextUtils.isEmpty(dmaPackage) && !TextUtils.equals(dmaPackage, this.mDmaPackage)) {
            boolean z = true;
            this.mIsDmaPackageChanged = true;
            this.mIsSecAndGoogDmaPackageSwitched = isSecAndGoogDmaPackageSwitched(this.mDmaPackage, dmaPackage);
            this.mCurConfigStartForce = (isNonSecGoogDmaPackageInuse(this.mDmaPackage) && isDmaPackageInuse(dmaPackage)) || this.mCurConfigStartForce;
            if ((!isNonSecGoogDmaPackageInuse(this.mDmaPackage) || !isDmaPackageInuse(dmaPackage)) && !this.mStartForce) {
                z = false;
            }
            this.mStartForce = z;
            IMSLog.i(str, this.mPhoneId, "checkAndUpdateDmaPackageInfo: dmaPackage is changed isSecAndGoogDmaPackageSwitched: " + this.mIsSecAndGoogDmaPackageSwitched + " curConfigStartForce: " + this.mCurConfigStartForce + " startForce: " + this.mStartForce);
            if (isGoogDmaPackageInuse(this.mDmaPackage) && !isGoogDmaPackageInuse(dmaPackage)) {
                IMSLog.i(str, this.mPhoneId, "checkAndUpdateDmaPackageInfo: dmaPackage is changed from goog to non-goog: notify autoConfigRemoved");
                SecImsNotifier.getInstance().notifyRcsAutoConfigurationRemoved(this.mPhoneId);
            }
            checkAndUpdateRcsClientConfiguration();
            this.mDmaPackage = dmaPackage;
            cancelValidityTimer();
            stopGeneralErrorRetryTimer();
        }
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateRcsClientConfiguration() {
        String str;
        String str2;
        if (isGoogDmaPackageInuse(this.mDmaPackage)) {
            this.mCurRcsVersion = TextUtils.isEmpty(this.mCurRcsVersion) ? this.mParamHandler.getRcsVersion(true) : this.mCurRcsVersion;
            this.mCurRcsProfile = TextUtils.isEmpty(this.mCurRcsProfile) ? this.mParamHandler.getRcsProfile(true) : this.mCurRcsProfile;
            this.mCurClientVendor = TextUtils.isEmpty(this.mCurClientVendor) ? this.mParamHandler.getClientVendor(true) : this.mCurClientVendor;
            this.mCurClientVersion = TextUtils.isEmpty(this.mCurClientVersion) ? this.mParamHandler.getClientVersion(true) : this.mCurClientVersion;
            return;
        }
        this.mCurRcsVersion = TextUtils.isEmpty(this.mCurRcsVersion) ? this.mRcsVersion : this.mCurRcsVersion;
        if (TextUtils.isEmpty(this.mCurRcsProfile)) {
            str = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.UP_PROFILE, "UP_1.0");
        } else {
            str = this.mCurRcsProfile;
        }
        this.mCurRcsProfile = str;
        this.mCurClientVendor = TextUtils.isEmpty(this.mCurClientVendor) ? "SEC" : this.mCurClientVendor;
        if (TextUtils.isEmpty(this.mCurClientVersion)) {
            str2 = this.mClientPlatform + this.mClientVersion;
        } else {
            str2 = this.mCurClientVersion;
        }
        this.mCurClientVersion = str2;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponseForUp(WorkflowBase.Workflow workflow, WorkflowBase.Workflow workflow2, WorkflowBase.Workflow workflow3) throws InvalidHeaderException, UnknownStatusException {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "handleResponseForUp: " + getLastErrorCode() + " response");
        int lastErrorCode = getLastErrorCode();
        if (lastErrorCode != 0) {
            if (lastErrorCode == 403) {
                setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            } else if (lastErrorCode != 500) {
                if (lastErrorCode == 503) {
                    startGeneralErrorRetryTimer(getretryAfterTime() * 1000);
                    this.mNextWorkflow = workflow2;
                } else if (lastErrorCode == 511) {
                    if (this.m511ResponseRetryCount < 3) {
                        int i = this.mPhoneId;
                        StringBuilder sb = new StringBuilder();
                        sb.append("handleResponseForUp: 511 response: retryCount: ");
                        sb.append(this.m511ResponseRetryCount);
                        sb.append(" retryTime: ");
                        long[] jArr = GENERAL_ERROR_RETRY_TIME;
                        sb.append(jArr[this.m511ResponseRetryCount]);
                        IMSLog.i(str, i, sb.toString());
                        startGeneralErrorRetryTimer(jArr[this.m511ResponseRetryCount]);
                        this.mNextWorkflow = workflow2;
                        this.m511ResponseRetryCount++;
                    } else {
                        IMSLog.i(str, this.mPhoneId, "handleResponseForUp: 511 response: no need to retry anymore");
                        this.mNextWorkflow = null;
                        this.mIsGeneralErrorRetryFailed = true;
                        cancelValidityTimer();
                    }
                    setToken("", DiagnosisConstants.RCSA_TDRE.INVALID_TOKEN);
                    this.mAppToken = "";
                } else {
                    throw new UnknownStatusException("handleResponseForUp: unknown https status code");
                }
            }
            stopGeneralErrorRetryTimer();
            this.mNextWorkflow = null;
            this.mIsGeneralErrorRetryFailed = true;
            cancelValidityTimer();
        } else if (this.mNoResponseRetryCount < 3) {
            int i2 = this.mPhoneId;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("handleResponseForUp: no response: retryCount: ");
            sb2.append(this.mNoResponseRetryCount);
            sb2.append(" retryTime: ");
            long[] jArr2 = GENERAL_ERROR_RETRY_TIME;
            sb2.append(jArr2[this.mNoResponseRetryCount]);
            IMSLog.i(str, i2, sb2.toString());
            startGeneralErrorRetryTimer(jArr2[this.mNoResponseRetryCount]);
            this.mNextWorkflow = workflow2;
            this.mNoResponseRetryCount++;
        } else {
            IMSLog.i(str, this.mPhoneId, "handleResponseForUp: no response: no need to retry anymore for no response");
            this.mNextWorkflow = null;
            this.mIsGeneralErrorRetryFailed = true;
            cancelValidityTimer();
        }
        return workflow3;
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowVzw$4  reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(20:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|(3:19|20|22)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(22:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|22) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode[] r0 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode = r0
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.ACTIVE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_TEMPORARY     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_PERMANENTLY     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DORMANT     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DORMANT_BY_RCS_DISABLED_STATE     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.TURNEDOFF_BY_RCS_DISABLED_STATE     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowVzw.AnonymousClass4.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public void setOpMode(WorkflowBase.OpMode opMode, Map<String, String> map) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setOpMode: " + opMode.name());
        switch (AnonymousClass4.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[opMode.ordinal()]) {
            case 1:
                if (map == null) {
                    IMSLog.i(str, this.mPhoneId, "setOpMode: active: data is empty");
                    return;
                }
                int i2 = this.mPhoneId;
                IMSLog.s(str, i2, "setOpMode: active: data: " + map);
                if ((this.mCurVersion <= getVersion(map) || this.mStartForce) && isDmaPackageInuse(this.mDmaPackage) && !this.mIsSecAndGoogDmaPackageSwitched) {
                    IMSLog.i(str, this.mPhoneId, "setOpMode: active: update the new config info");
                    checkAndKeepRcsClientConfiguration(map);
                    clearStorage(DiagnosisConstants.RCSA_TDRE.UPDATE_REMOTE_CONFIG);
                    this.mStorage.writeAll(map);
                    setVersionBackup(getVersion(map));
                } else {
                    IMSLog.i(str, this.mPhoneId, "setOpMode: active: maintain the previous config info");
                    checkAndUpdateData(map);
                }
                int validity = getValidity();
                setNextAutoconfigTimeAfter(validity);
                setValidityTimer(validity);
                return;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                if (map == null) {
                    IMSLog.i(str, this.mPhoneId, "setOpMode: data is empty");
                    String rcsVersion = this.mParamHandler.getRcsVersion(false);
                    String rcsProfile = this.mParamHandler.getRcsProfile(false);
                    String clientVendor = this.mParamHandler.getClientVendor(false);
                    String clientVersion = this.mParamHandler.getClientVersion(false);
                    clearStorage(DiagnosisConstants.RCSA_TDRE.DISABLE_RCS);
                    WorkflowBase.OpMode opMode2 = WorkflowBase.OpMode.DISABLE_TEMPORARY;
                    if (opMode == opMode2 || opMode == WorkflowBase.OpMode.DISABLE_PERMANENTLY || opMode == WorkflowBase.OpMode.DISABLE || opMode == WorkflowBase.OpMode.DORMANT) {
                        setVersion(opMode.value());
                    } else {
                        setVersion(opMode2.value());
                        setRcsDisabledState(String.valueOf(convertRcsDisabledStateToValue(opMode)));
                    }
                    setValidity(opMode2.value());
                    this.mParamHandler.setRcsClientConfiguration(rcsVersion, rcsProfile, clientVendor, clientVersion);
                    return;
                }
                IMSLog.i(str, this.mPhoneId, "setOpMode: update the new config info");
                checkAndKeepRcsClientConfiguration(map);
                clearStorage(DiagnosisConstants.RCSA_TDRE.DISABLE_RCS);
                this.mStorage.writeAll(map);
                return;
            default:
                IMSLog.i(str, this.mPhoneId, "setOpMode: unknown opMode");
                return;
        }
    }

    public void cleanup() {
        if (hasMessages(4) && this.mIsConfigOngoing) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup: waiting for the result of mobile connection");
            removeMessages(4);
            sendEmptyMessage(4);
        }
        super.cleanup();
        endMobileAutoConfig();
        IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup: send cleanup msg");
        sendEmptyMessage(14);
    }

    /* access modifiers changed from: protected */
    public IHttpAdapter.Response getHttpResponse() {
        this.mHttp.close();
        this.mHttp.setHeaders(this.mSharedInfo.getHttpHeaders());
        this.mHttp.setParams(this.mSharedInfo.getHttpParams());
        this.mHttp.setContext(this.mContext);
        this.mHttp.setNetwork(this.mNetwork);
        this.mHttp.open(this.mSharedInfo.getUrl());
        IHttpAdapter.Response request = this.mHttp.request();
        this.mHttp.close();
        return request;
    }

    /* access modifiers changed from: protected */
    public void setHttpParameter() {
        int i;
        String str;
        String str2;
        if (this.mStartForce || isNonActiveVersion(this.mCurVersion) || (i = this.mCurVersion) == 59) {
            i = WorkflowBase.OpMode.DISABLE_TEMPORARY.value();
        }
        this.mCurVersion = i;
        String str3 = LOG_TAG;
        IMSLog.i(str3, this.mPhoneId, "setHttpParameter: curVersion: " + this.mCurVersion + " startForce: " + this.mStartForce);
        this.mSharedInfo.addHttpParam("vers", String.valueOf(this.mCurVersion));
        this.mSharedInfo.addHttpParam("IMSI", this.mTelephonyAdapter.getImsi());
        String str4 = "SEC";
        this.mSharedInfo.addHttpParam("terminal_vendor", str4);
        this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
        this.mSharedInfo.addHttpParam("terminal_sw_version", this.mParamHandler.getModelInfoFromBuildVersion(ConfigUtil.getModelName(this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, this.mTelephonyAdapter.getImei());
        String msisdn = this.mTelephonyAdapter.getMsisdn(SimUtil.getSubId(this.mPhoneId));
        if (!TextUtils.isEmpty(msisdn)) {
            IMSLog.i(str3, this.mPhoneId, "setHttpParameter: use msisdn from telephony with current subId: " + SimUtil.getSubId(this.mPhoneId));
            this.mSharedInfo.addHttpParam("msisdn", this.mParamHandler.encodeRFC3986(msisdn));
        }
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, this.mTelephonyAdapter.getSmsDestPort());
        String token = getToken();
        if (TextUtils.isEmpty(token)) {
            IMSLog.i(str3, this.mPhoneId, "setHttpParameter: rcstoken is empty so use apptoken");
            this.mSharedInfo.addHttpParam("token", this.mAppToken);
        } else {
            IMSLog.i(str3, this.mPhoneId, "setHttpParameter: rcstoken is existed so use rcstoken");
            this.mSharedInfo.addHttpParam("token", token);
        }
        String convertRcsStateWithSpecificParam = convertRcsStateWithSpecificParam(this.mCurVersion);
        IMSLog.i(str3, this.mPhoneId, "setHttpParameter: rcsState: " + convertRcsStateWithSpecificParam);
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, convertRcsStateWithSpecificParam);
        if (isDmaPackageInuse(this.mDmaPackage) && !this.mIsSecAndGoogDmaPackageSwitched) {
            boolean isSecDmaPackageInuse = isSecDmaPackageInuse(this.mDmaPackage);
            this.mCurRcsVersion = isSecDmaPackageInuse ? this.mRcsVersion : this.mParamHandler.getRcsVersion(true);
            if (isSecDmaPackageInuse) {
                str = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.UP_PROFILE, "UP_1.0");
            } else {
                str = this.mParamHandler.getRcsProfile(true);
            }
            this.mCurRcsProfile = str;
            if (!isSecDmaPackageInuse) {
                str4 = this.mParamHandler.getClientVendor(true);
            }
            this.mCurClientVendor = str4;
            if (isSecDmaPackageInuse) {
                str2 = this.mClientPlatform + this.mClientVersion;
            } else {
                str2 = this.mParamHandler.getClientVersion(true);
            }
            this.mCurClientVersion = str2;
        }
        IMSLog.i(str3, this.mPhoneId, "setHttpParameter: curRcsVersion: " + this.mCurRcsVersion + " curRcsProfile: " + this.mCurRcsProfile + " curClientVendor: " + this.mCurClientVendor + " curClientVersion: " + this.mCurClientVersion);
        this.mSharedInfo.addHttpParam("rcs_version", this.mCurRcsVersion);
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, this.mCurRcsProfile);
        this.mSharedInfo.addHttpParam("client_vendor", this.mCurClientVendor);
        this.mSharedInfo.addHttpParam("client_version", this.mCurClientVersion);
        if (ImsProfile.isRcsUp2Profile(this.mCurRcsProfile)) {
            IMSLog.i(str3, this.mPhoneId, "setHttpParameter: app: ap2002 provisioningVersion: 5.0");
            this.mSharedInfo.addHttpParam("app", ConfigConstants.PVALUE.APP_ID_2);
            this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, ConfigConstants.PVALUE.PROVISIONING_VERSION_5_0);
        } else {
            this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, "2.0");
        }
        this.mSharedInfo.addHttpParam("default_sms_app", (!isDmaPackageInuse(this.mDmaPackage) || this.mIsSecAndGoogDmaPackageSwitched) ? "2" : "1");
    }

    class Initialize implements WorkflowBase.Workflow {
        Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowVzw workflowVzw = WorkflowVzw.this;
            workflowVzw.mHttpResponse = 0;
            IMSLog.i(WorkflowVzw.LOG_TAG, workflowVzw.mPhoneId, "initialize: initUrl and clearCookie");
            WorkflowVzw workflowVzw2 = WorkflowVzw.this;
            workflowVzw2.mSharedInfo.setUrl(workflowVzw2.mParamHandler.initUrl());
            WorkflowVzw.this.mCookieHandler.clearCookie();
            return new FetchToken();
        }
    }

    class FetchToken implements WorkflowBase.Workflow {
        FetchToken() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (TextUtils.isEmpty(WorkflowVzw.this.getToken())) {
                return new FetchAppToken();
            }
            IMSLog.i(WorkflowVzw.LOG_TAG, WorkflowVzw.this.mPhoneId, "fetchToken: rcstoken is existed");
            return new FetchHttps();
        }
    }

    class FetchAppToken implements WorkflowBase.Workflow {
        FetchAppToken() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowVzw.LOG_TAG, WorkflowVzw.this.mPhoneId, "fetchAppToken: apptoken is needed");
            WorkflowVzw.this.mPowerController.release();
            WorkflowVzw workflowVzw = WorkflowVzw.this;
            workflowVzw.mAppToken = workflowVzw.mTelephonyAdapter.getAppToken(false);
            WorkflowVzw.this.mPowerController.lock();
            return new AuthorizeAppToken();
        }
    }

    class AuthorizeAppToken implements WorkflowBase.Workflow {
        AuthorizeAppToken() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            String str = WorkflowVzw.LOG_TAG;
            IMSLog.i(str, WorkflowVzw.this.mPhoneId, "authorizeAppToken: apptoken is received");
            if (TextUtils.isEmpty(WorkflowVzw.this.mAppToken)) {
                IMSLog.i(str, WorkflowVzw.this.mPhoneId, "authorizeAppToken: apptoken is empty");
                return new ReFetchAppToken();
            }
            IMSLog.i(str, WorkflowVzw.this.mPhoneId, "authorizeAppToken: apptoken is existed");
            return new FetchHttps();
        }
    }

    class ReFetchAppToken implements WorkflowBase.Workflow {
        ReFetchAppToken() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowVzw workflowVzw = WorkflowVzw.this;
            if (workflowVzw.mNoAppTokenRetryCount < 3) {
                String str = WorkflowVzw.LOG_TAG;
                int i = workflowVzw.mPhoneId;
                StringBuilder sb = new StringBuilder();
                sb.append("reFetchAppToken: noAppTokenRetryCount: ");
                sb.append(WorkflowVzw.this.mNoAppTokenRetryCount);
                sb.append(" noAppTokenRetryTime: ");
                long[] jArr = WorkflowVzw.GENERAL_ERROR_RETRY_TIME;
                sb.append(jArr[WorkflowVzw.this.mNoAppTokenRetryCount]);
                IMSLog.i(str, i, sb.toString());
                WorkflowVzw workflowVzw2 = WorkflowVzw.this;
                workflowVzw2.startGeneralErrorRetryTimer(jArr[workflowVzw2.mNoAppTokenRetryCount]);
                WorkflowVzw workflowVzw3 = WorkflowVzw.this;
                workflowVzw3.mNextWorkflow = new FetchAppToken();
                WorkflowVzw.this.mNoAppTokenRetryCount++;
            } else {
                IMSLog.i(WorkflowVzw.LOG_TAG, workflowVzw.mPhoneId, "reFetchAppToken: no need to retry anymore for no apptoken");
                WorkflowVzw workflowVzw4 = WorkflowVzw.this;
                workflowVzw4.mNextWorkflow = null;
                workflowVzw4.mIsGeneralErrorRetryFailed = true;
                workflowVzw4.cancelValidityTimer();
            }
            return new Finish();
        }
    }

    class ReFetchAppTokenFor511Response implements WorkflowBase.Workflow {
        ReFetchAppTokenFor511Response() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowVzw.LOG_TAG, WorkflowVzw.this.mPhoneId, "reFetchAppTokenFor511Response: apptoken is needed");
            WorkflowVzw.this.mPowerController.release();
            WorkflowVzw workflowVzw = WorkflowVzw.this;
            workflowVzw.mAppToken = workflowVzw.mTelephonyAdapter.getAppToken(workflowVzw.m511ResponseRetryCount != 0);
            WorkflowVzw.this.mPowerController.lock();
            return new AuthorizeAppToken();
        }
    }

    class FetchHttps implements WorkflowBase.Workflow {
        FetchHttps() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowVzw workflowVzw = WorkflowVzw.this;
            workflowVzw.mSharedInfo.setUrl(workflowVzw.mParamHandler.initUrl());
            WorkflowVzw.this.mSharedInfo.setHttpClean();
            WorkflowVzw.this.mSharedInfo.setHttpsDefault();
            WorkflowVzw.this.setHttpParameter();
            WorkflowVzw workflowVzw2 = WorkflowVzw.this;
            workflowVzw2.mSharedInfo.setHttpResponse(workflowVzw2.getHttpResponse());
            IHttpAdapter.Response httpResponse = WorkflowVzw.this.mSharedInfo.getHttpResponse();
            WorkflowVzw.this.mHttpResponse = httpResponse.getStatusCode();
            WorkflowVzw workflowVzw3 = WorkflowVzw.this;
            workflowVzw3.setLastErrorCode(workflowVzw3.mHttpResponse);
            WorkflowVzw.this.setLastErrorMessage(httpResponse.getStatusMessage());
            String str = WorkflowVzw.LOG_TAG;
            int i = WorkflowVzw.this.mPhoneId;
            IMSLog.i(str, i, "fetchHttps: https response: " + WorkflowVzw.this.mHttpResponse + " https response msg: " + WorkflowVzw.this.getLastErrorMessage());
            WorkflowVzw workflowVzw4 = WorkflowVzw.this;
            int i2 = workflowVzw4.mHttpResponse;
            if (i2 == 200) {
                if (httpResponse.getBody() != null) {
                    IMSLog.i(str, WorkflowVzw.this.mPhoneId, "fetchHttps: https response's body is existed");
                    return new Parse();
                }
                throw new UnknownStatusException("fetchHttps: there is no https response's body");
            } else if (i2 == 511) {
                return workflowVzw4.handleResponseForUp(new Initialize(), new ReFetchAppTokenFor511Response(), new Finish());
            } else {
                return workflowVzw4.handleResponseForUp(new Initialize(), new FetchHttps(), new Finish());
            }
        }
    }

    class Parse implements WorkflowBase.Workflow {
        Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            Map<String, String> map;
            if (ImsProfile.isRcsUp2Profile(WorkflowVzw.this.mCurRcsProfile)) {
                WorkflowVzw workflowVzw = WorkflowVzw.this;
                map = workflowVzw.mXmlMultipleParser.parse(new String(workflowVzw.mSharedInfo.getHttpResponse().getBody(), "utf-8"));
            } else {
                WorkflowVzw workflowVzw2 = WorkflowVzw.this;
                map = workflowVzw2.mXmlParser.parse(new String(workflowVzw2.mSharedInfo.getHttpResponse().getBody(), "utf-8"));
            }
            if (map == null || TextUtils.isEmpty(map.get("root/vers/version")) || TextUtils.isEmpty(map.get("root/vers/validity"))) {
                throw new InvalidXmlException("parse: parsedXml is invalid!");
            }
            String str = WorkflowVzw.LOG_TAG;
            int i = WorkflowVzw.this.mPhoneId;
            IMSLog.i(str, i, "parse: parsedXml is received from the network server: version: " + map.get("root/vers/version") + " validity: " + map.get("root/vers/validity") + " rcsDisabledState: " + WorkflowVzw.this.getRcsDisabledState(map));
            WorkflowVzw.this.mParamHandler.checkSetToGS(map);
            WorkflowVzw.this.mSharedInfo.setParsedXml(map);
            return new Store();
        }
    }

    class Store implements WorkflowBase.Workflow {
        Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            Map<String, String> parsedXml = WorkflowVzw.this.mSharedInfo.getParsedXml();
            WorkflowBase.OpMode rcsDisabledState = WorkflowVzw.this.getRcsDisabledState(parsedXml);
            if (WorkflowVzw.this.isValidRcsDisabledState(rcsDisabledState)) {
                WorkflowVzw.this.setOpMode(rcsDisabledState, parsedXml);
                if (rcsDisabledState != WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE) {
                    IMSLog.i(WorkflowVzw.LOG_TAG, WorkflowVzw.this.mPhoneId, "store: no need to retry for rcsDisabledState");
                } else {
                    WorkflowVzw workflowVzw = WorkflowVzw.this;
                    if (workflowVzw.mRcsDisabledStateRetryCount < 3) {
                        String str = WorkflowVzw.LOG_TAG;
                        int i = workflowVzw.mPhoneId;
                        StringBuilder sb = new StringBuilder();
                        sb.append("store: rcsDisabledStateRetryCount: ");
                        sb.append(WorkflowVzw.this.mRcsDisabledStateRetryCount);
                        sb.append(" rcsDisabledStateRetryTime: ");
                        long[] jArr = WorkflowVzw.GENERAL_ERROR_RETRY_TIME;
                        sb.append(jArr[WorkflowVzw.this.mRcsDisabledStateRetryCount]);
                        IMSLog.i(str, i, sb.toString());
                        WorkflowVzw workflowVzw2 = WorkflowVzw.this;
                        workflowVzw2.startGeneralErrorRetryTimer(jArr[workflowVzw2.mRcsDisabledStateRetryCount]);
                        WorkflowVzw workflowVzw3 = WorkflowVzw.this;
                        workflowVzw3.mNextWorkflow = new FetchHttps();
                        WorkflowVzw.this.mRcsDisabledStateRetryCount++;
                    } else {
                        IMSLog.i(WorkflowVzw.LOG_TAG, workflowVzw.mPhoneId, "store: no need to retry anymore for rcsDisabledState");
                        WorkflowVzw.this.mNextWorkflow = null;
                    }
                }
                return new Finish();
            }
            WorkflowVzw workflowVzw4 = WorkflowVzw.this;
            workflowVzw4.setOpMode(workflowVzw4.getOpMode(parsedXml), parsedXml);
            int version = WorkflowVzw.this.getVersion();
            if (WorkflowVzw.this.isActiveVersion(version) && version != 101) {
                WorkflowVzw workflowVzw5 = WorkflowVzw.this;
                if (workflowVzw5.getRemainingValidityTime(workflowVzw5.getNextAutoconfigTime()) <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value()) {
                    WorkflowVzw workflowVzw6 = WorkflowVzw.this;
                    if (workflowVzw6.mNoRemainingValidityRetryCount < 3) {
                        String str2 = WorkflowVzw.LOG_TAG;
                        int i2 = workflowVzw6.mPhoneId;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("store: remainingValidityTime is not valid: noRemainingValidityRetryCount: ");
                        sb2.append(WorkflowVzw.this.mNoRemainingValidityRetryCount);
                        sb2.append(" noRemainingValidityRetryTime: ");
                        long[] jArr2 = WorkflowVzw.GENERAL_ERROR_RETRY_TIME;
                        sb2.append(jArr2[WorkflowVzw.this.mNoRemainingValidityRetryCount]);
                        IMSLog.i(str2, i2, sb2.toString());
                        WorkflowVzw workflowVzw7 = WorkflowVzw.this;
                        workflowVzw7.startGeneralErrorRetryTimer(jArr2[workflowVzw7.mNoRemainingValidityRetryCount]);
                        WorkflowVzw workflowVzw8 = WorkflowVzw.this;
                        workflowVzw8.mNextWorkflow = new FetchHttps();
                        WorkflowVzw.this.mNoRemainingValidityRetryCount++;
                    } else {
                        IMSLog.i(WorkflowVzw.LOG_TAG, workflowVzw6.mPhoneId, "store: no need to retry anymore for noRemainingValidityTime");
                        WorkflowVzw.this.mNextWorkflow = null;
                    }
                }
            }
            return new Finish();
        }
    }

    class Finish implements WorkflowBase.Workflow {
        Finish() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowVzw workflowVzw = WorkflowVzw.this;
            workflowVzw.setLastErrorCode(workflowVzw.mSharedInfo.getHttpResponse() == null ? IWorkflow.DEFAULT_ERROR_CODE : WorkflowVzw.this.mSharedInfo.getHttpResponse().getStatusCode());
            WorkflowVzw workflowVzw2 = WorkflowVzw.this;
            workflowVzw2.setLastErrorMessage(workflowVzw2.mSharedInfo.getHttpResponse() == null ? "" : WorkflowVzw.this.mSharedInfo.getHttpResponse().getStatusMessage());
            String str = WorkflowVzw.LOG_TAG;
            int i = WorkflowVzw.this.mPhoneId;
            IMSLog.i(str, i, "finish: lastErrorCode: " + WorkflowVzw.this.getLastErrorCode() + " lastErrorMsg: " + WorkflowVzw.this.getLastErrorMessage());
            return null;
        }
    }
}
