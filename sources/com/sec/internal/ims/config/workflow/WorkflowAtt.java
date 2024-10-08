package com.sec.internal.ims.config.workflow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class WorkflowAtt extends WorkflowBase {
    private static final String INTENT_TOKEN_EXPIRED_AFTER_MAX_RETRY = "com.sec.internal.ims.config.workflow.token_expired_after_max_retry";
    public static final String LOG_TAG = WorkflowAtt.class.getSimpleName();
    private static final long RESET_TOKEN_TIMEOUT = 86400000;
    static final int[] RETRY_INTERVAL = {Id.REQUEST_SIP_DIALOG_SEND_SIP, 3600, 7200, 14400, 28800};
    private long expirationTime = 0;
    /* access modifiers changed from: private */
    public boolean isACSsuccessful = false;
    protected boolean isAirplaneModeObserverRegistered = false;
    private boolean isFailedToConnect = false;
    protected boolean isImsRegiListenerRegistered;
    /* access modifiers changed from: private */
    public boolean isLocalConfig = false;
    protected boolean isMainSwitchToggled = false;
    protected boolean isRcsUserSettingObserverRegistered = false;
    final ContentObserver mAirplaneModeObserver = new ContentObserver(this) {
        public void onChange(boolean z) {
            int i = Settings.Global.getInt(WorkflowAtt.this.mContext.getContentResolver(), "airplane_mode_on", 1);
            String str = WorkflowAtt.LOG_TAG;
            int i2 = WorkflowAtt.this.mPhoneId;
            IMSLog.i(str, i2, "onChange: Airplane Mode On: " + i);
            if (i == 0) {
                Handler handler = WorkflowAtt.this.mModule.getHandler();
                WorkflowAtt workflowAtt = WorkflowAtt.this;
                handler.sendMessage(workflowAtt.obtainMessage(17, Integer.valueOf(workflowAtt.mPhoneId)));
            }
        }
    };
    String mClientVendor;
    String mDefaultSmsApp;
    final IImsRegistrationListener mImsRegistrationListener = new IImsRegistrationListener.Stub() {
        public void onRegistered(ImsRegistration imsRegistration) {
            String str = WorkflowAtt.LOG_TAG;
            IMSLog.i(str, WorkflowAtt.this.mPhoneId, "onRegistered");
            WorkflowAtt workflowAtt = WorkflowAtt.this;
            if (!workflowAtt.mIsFirstImsRegistrationDone) {
                workflowAtt.mIsFirstImsRegistrationDone = true;
                workflowAtt.mMsisdn = imsRegistration.getOwnNumber();
                WorkflowAtt workflowAtt2 = WorkflowAtt.this;
                if (workflowAtt2.mMsisdn != null) {
                    IMSLog.i(str, workflowAtt2.mPhoneId, "MSISDN is registered.");
                }
                WorkflowAtt workflowAtt3 = WorkflowAtt.this;
                workflowAtt3.addEventLog(str + ": IMS registered, start autoconfig");
                WorkflowAtt.this.sendEmptyMessage(1);
                WorkflowAtt.this.unregisterImsRegistrationListener();
            }
        }

        public void onDeregistered(ImsRegistration imsRegistration, ImsRegistrationError imsRegistrationError) {
            IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "onDeregistered");
        }
    };
    boolean mIsAcsSkipped = false;
    boolean mIsFirstImsRegistrationDone;
    boolean mIsRetry = false;
    String mMsisdn;
    final ContentObserver mRcsUserSettingObserver = new ContentObserver(this) {
        public void onChange(boolean z) {
            WorkflowAtt workflowAtt = WorkflowAtt.this;
            workflowAtt.addEventLog(WorkflowAtt.LOG_TAG + ": RCS user switch is toggled, start autoconfig");
            WorkflowAtt workflowAtt2 = WorkflowAtt.this;
            workflowAtt2.isMainSwitchToggled = true;
            workflowAtt2.sendEmptyMessage(0);
        }
    };
    PendingIntent mResetTokenIntent = null;
    boolean mSetClientInfoCompleted;
    int mTrialCount = 0;

    public boolean checkNetworkConnectivity() {
        return false;
    }

    private void registerRcsUserSettingObserver() {
        Uri uri = ImsConstants.SystemSettings.RCS_USER_SETTING1.getUri();
        if (!this.isRcsUserSettingObserverRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "registerRcsUserSettingObserver");
            this.mContext.getContentResolver().registerContentObserver(uri, false, this.mRcsUserSettingObserver);
            this.isRcsUserSettingObserverRegistered = true;
        }
    }

    private void unregisterRcsUserSettingObserver() {
        if (this.isRcsUserSettingObserverRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterRcsUserSettingObserver");
            this.mContext.getContentResolver().unregisterContentObserver(this.mRcsUserSettingObserver);
            this.isRcsUserSettingObserverRegistered = false;
        }
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowAtt(android.os.Looper r13, android.content.Context r14, com.sec.internal.interfaces.ims.config.IConfigModule r15, com.sec.internal.constants.Mno r16, int r17) {
        /*
            r12 = this;
            r11 = r12
            r2 = r14
            r3 = r15
            r10 = r17
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceAtt r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceAtt
            r5.<init>(r14, r15, r10)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapter r7 = new com.sec.internal.ims.config.adapters.HttpAdapter
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
            r11.expirationTime = r0
            r0 = 0
            r11.isMainSwitchToggled = r0
            r1 = 0
            r11.mResetTokenIntent = r1
            r11.isLocalConfig = r0
            com.sec.internal.ims.config.workflow.WorkflowAtt$1 r1 = new com.sec.internal.ims.config.workflow.WorkflowAtt$1
            r1.<init>(r12)
            r11.mRcsUserSettingObserver = r1
            com.sec.internal.ims.config.workflow.WorkflowAtt$2 r1 = new com.sec.internal.ims.config.workflow.WorkflowAtt$2
            r1.<init>()
            r11.mImsRegistrationListener = r1
            com.sec.internal.ims.config.workflow.WorkflowAtt$11 r1 = new com.sec.internal.ims.config.workflow.WorkflowAtt$11
            r1.<init>(r12)
            r11.mAirplaneModeObserver = r1
            r11.mTrialCount = r0
            r11.isAirplaneModeObserverRegistered = r0
            r11.isRcsUserSettingObserverRegistered = r0
            r11.isACSsuccessful = r0
            r11.mIsAcsSkipped = r0
            r11.mIsRetry = r0
            r11.isFailedToConnect = r0
            r12.registerAirplaneModeObserver()
            r12.registerRcsUserSettingObserver()
            r11.isImsRegiListenerRegistered = r0
            r11.mIsFirstImsRegistrationDone = r0
            r11.mSetClientInfoCompleted = r0
            r12.registerImsRegistrationListener()
            r12.registerResetTokenIntentReceiver()
            boolean r0 = com.sec.internal.ims.rcs.util.RcsUtils.DualRcs.isDualRcsReg()
            if (r0 != 0) goto L_0x0072
            com.sec.internal.interfaces.ims.config.IStorageAdapter r0 = r11.mStorage
            r1 = 1
            r0.setDBTableMax(r1)
        L_0x0072:
            java.lang.String r0 = LOG_TAG
            int r1 = r11.mPhoneId
            java.lang.String r2 = "created"
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowAtt.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, int):void");
    }

    /* access modifiers changed from: package-private */
    public void initRcsClientConfiguration() {
        if (isClientVendorSetToGoog() && RcsUtils.isSingleRegiRequiredAndAndroidMessageAppInUsed(this.mContext, this.mPhoneId)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Set RCS configuration For AM");
            setRcsClientConfigurationforGoog();
            this.mDefaultSmsApp = "1";
        } else if (ConfigUtil.isSecDmaPackageInuse(this.mContext, this.mPhoneId)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Set RCS configuration For SM");
            setRcsClientConfigurationforSec();
            this.mDefaultSmsApp = "1";
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Set RCS configuration For others");
            this.mDefaultSmsApp = "2";
        }
        this.mSetClientInfoCompleted = !TextUtils.isEmpty(this.mRcsProfile) && !TextUtils.isEmpty(this.mRcsVersion) && !TextUtils.isEmpty(this.mClientVersion) && !TextUtils.isEmpty(this.mClientVendor);
    }

    private void setRcsClientConfigurationforSec() {
        this.mRcsProfile = ConfigUtil.getRcsProfileLoaderInternalWithFeature(this.mContext, this.mMno.getName(), this.mPhoneId);
        this.mRcsVersion = ImsRegistry.getString(this.mPhoneId, "rcs_version", "6.0");
        this.mClientVersion = this.mClientPlatform + ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_CLIENT_VERSION, "6.0");
        this.mClientVendor = "SEC";
    }

    private void setRcsClientConfigurationforGoog() {
        this.mRcsProfile = this.mParamHandler.getRcsProfile(false);
        this.mRcsVersion = this.mParamHandler.getRcsVersion(false);
        this.mClientVersion = this.mParamHandler.getClientVersion(false);
        this.mClientVendor = this.mParamHandler.getClientVendor(false);
        this.mRcsEnabledByUser = this.mParamHandler.isRcsEnabledByUser(false);
    }

    private void keepRcsClientConfiguration() {
        String str = "";
        this.mStorage.write(ConfigConstants.PATH.INFO_RCS_VERSION, TextUtils.isEmpty(this.mRcsVersion) ? str : this.mRcsVersion);
        this.mStorage.write(ConfigConstants.PATH.INFO_RCS_PROFILE, TextUtils.isEmpty(this.mRcsProfile) ? str : this.mRcsProfile);
        this.mStorage.write(ConfigConstants.PATH.INFO_CLIENT_VENDOR, TextUtils.isEmpty(this.mClientVendor) ? str : this.mClientVendor);
        this.mStorage.write(ConfigConstants.PATH.INFO_CLIENT_VERSION, TextUtils.isEmpty(this.mClientVersion) ? str : this.mClientVersion);
        IStorageAdapter iStorageAdapter = this.mStorage;
        if (!TextUtils.isEmpty(this.mRcsEnabledByUser)) {
            str = this.mRcsEnabledByUser;
        }
        iStorageAdapter.write(ConfigConstants.PATH.INFO_RCS_ENABLED_BY_USER, str);
    }

    /* access modifiers changed from: private */
    public boolean isClientVendorSetToGoog() {
        return TextUtils.equals(this.mParamHandler.getClientVendor(false), ConfigConstants.PVALUE.GOOG_DEFAULT_CLIENT_VENDOR);
    }

    private void notifyRcsConfigurationReceived() {
        if (isClientVendorSetToGoog() && RcsUtils.isSingleRegiRequiredAndAndroidMessageAppInUsed(this.mContext, this.mPhoneId)) {
            if (getLastErrorCode() == 987 || getLastErrorCode() == 200) {
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "notify Provisioning XML isLocalConfig " + this.isLocalConfig + " isACSskipped " + this.mIsAcsSkipped);
                if (this.isLocalConfig) {
                    SecImsNotifier.getInstance().notifyRcsPreConfigurationReceived(this.mPhoneId, this.mParamHandler.getProvisioningXml(false));
                } else if (!this.mIsAcsSkipped) {
                    SecImsNotifier.getInstance().notifyRcsAutoConfigurationReceived(this.mPhoneId, this.mParamHandler.getProvisioningXml(true), false);
                }
            } else {
                SecImsNotifier.getInstance().notifyRcsAutoConfigurationErrorReceived(this.mPhoneId, getLastErrorCode(), this.mSharedInfo.getHttpResponse() != null ? this.mSharedInfo.getHttpResponse().getStatusMessage() : "");
            }
        }
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: " + message.what);
        int i2 = message.what;
        if (i2 == 0) {
            IMSLog.i(str, this.mPhoneId, "forced startAutoConfig");
            removeMessages(0);
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 == 5) {
                addEventLog(str + ": sms default application is changed");
                if (RcsUtils.isImsSingleRegiRequired(this.mContext, this.mPhoneId)) {
                    if (getVersion() <= 1 || !ConfigUtil.isGoogDmaPackageInuse(this.mContext, this.mPhoneId) || !ConfigConstants.PVALUE.GOOG_DEFAULT_CLIENT_VENDOR.equals(this.mStorage.read(ConfigConstants.PATH.INFO_CLIENT_VENDOR))) {
                        clearStorage();
                    } else {
                        IMSLog.i(str, this.mPhoneId, "Skip forced ACS. It has already done.");
                        return;
                    }
                }
                this.isMainSwitchToggled = true;
                sendEmptyMessage(0);
                return;
            } else if (i2 != 16) {
                super.handleMessage(message);
                return;
            } else {
                String string = ((Bundle) message.obj).getString("rcsVersion");
                String string2 = ((Bundle) message.obj).getString("rcsProfile");
                String string3 = ((Bundle) message.obj).getString("clientVendor");
                String string4 = ((Bundle) message.obj).getString("clientVersion");
                String string5 = ((Bundle) message.obj).getString("rcsEnabledByUser");
                boolean rcsClientConfiguration = this.mParamHandler.setRcsClientConfiguration(string, string2, string3, string4);
                boolean rcsSwitchValue = this.mParamHandler.setRcsSwitchValue(string5);
                int i3 = this.mPhoneId;
                IMSLog.i(str, i3, "AutoConfig: client info is changed: " + rcsClientConfiguration);
                int i4 = this.mPhoneId;
                IMSLog.i(str, i4, "AutoConfig: AM switch is changed: " + rcsSwitchValue);
                if (!ConfigUtil.isGoogDmaPackageInuse(this.mContext, this.mPhoneId)) {
                    return;
                }
                if (rcsClientConfiguration || rcsSwitchValue) {
                    IMSLog.i(str, this.mPhoneId, "AutoConfig: client info is changed: need auto config to use the changed client info");
                    this.mModule.getHandler().sendMessage(obtainMessage(1, this.mPhoneId, 0, (Object) null));
                    return;
                }
                return;
            }
        }
        removeMessages(1);
        if (this.mIsConfigOngoing) {
            IMSLog.i(str, this.mPhoneId, "AutoConfig: Already started");
            return;
        }
        this.mIsConfigOngoing = true;
        IMSLog.i(str, this.mPhoneId, "AutoConfig: START");
        this.mPowerController.lock();
        initRcsClientConfiguration();
        int version = getVersion();
        if (needScheduleAutoconfig(this.mPhoneId)) {
            scheduleAutoconfig(version);
        }
        int version2 = getVersion();
        int i5 = this.mPhoneId;
        IMSLog.i(str, i5, "oldVersion : " + version + " newVersion : " + version2);
        IMSLog.i(str, this.mPhoneId, "AutoConfig: FINISH");
        setCompleted(true);
        this.mModule.getHandler().removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mModule.getHandler().sendMessage(obtainMessage(3, version, version2, Integer.valueOf(this.mPhoneId)));
        notifyRcsConfigurationReceived();
        this.mIsAcsSkipped = false;
        this.mStartForce = false;
        this.mPowerController.release();
        this.mIsConfigOngoing = false;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        switch (i) {
            case 1:
                return new WorkflowBase.Initialize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "Initialize:");
                        return super.run();
                    }
                };
            case 2:
                return new WorkflowBase.FetchHttp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "FetchHttp:");
                        return super.run();
                    }
                };
            case 3:
                return new WorkflowBase.FetchHttps() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "FetchHttps:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttps() {
                        if (WorkflowAtt.this.mParamHandler.isConfigProxy()) {
                            WorkflowAtt.this.mSharedInfo.setHttpDefault();
                        } else {
                            WorkflowAtt.this.mSharedInfo.setHttpsDefault();
                        }
                        WorkflowAtt workflowAtt = WorkflowAtt.this;
                        workflowAtt.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, String.valueOf(workflowAtt.getVersionFromServer()));
                        WorkflowAtt workflowAtt2 = WorkflowAtt.this;
                        workflowAtt2.mSharedInfo.addHttpParam("rcs_version", workflowAtt2.mRcsVersion);
                        WorkflowAtt workflowAtt3 = WorkflowAtt.this;
                        workflowAtt3.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, "NAGuidelines".equalsIgnoreCase(workflowAtt3.mRcsProfile) ? "UP_1.0" : WorkflowAtt.this.mRcsProfile);
                        WorkflowAtt workflowAtt4 = WorkflowAtt.this;
                        workflowAtt4.mSharedInfo.addHttpParam("client_vendor", workflowAtt4.mClientVendor);
                        WorkflowAtt workflowAtt5 = WorkflowAtt.this;
                        workflowAtt5.mSharedInfo.addHttpParam("client_version", workflowAtt5.mClientVersion);
                        WorkflowAtt workflowAtt6 = WorkflowAtt.this;
                        workflowAtt6.mSharedInfo.addHttpParam("default_sms_app", workflowAtt6.mDefaultSmsApp);
                        WorkflowAtt workflowAtt7 = WorkflowAtt.this;
                        workflowAtt7.mSharedInfo.addHttpParam("vers", String.valueOf(workflowAtt7.getVersionFromServer()));
                        WorkflowAtt workflowAtt8 = WorkflowAtt.this;
                        workflowAtt8.mSharedInfo.addHttpParam("IMSI", workflowAtt8.mTelephonyAdapter.getImsi());
                        WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, "2.0");
                        WorkflowAtt.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.TERMINAL_VENDOR);
                        WorkflowAtt.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
                        WorkflowAtt.this.mSharedInfo.addHttpParam("terminal_sw_version", ConfigConstants.PVALUE.TERMINAL_SW_VERSION);
                        WorkflowAtt workflowAtt9 = WorkflowAtt.this;
                        workflowAtt9.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, workflowAtt9.mTelephonyAdapter.getImei());
                        WorkflowAtt workflowAtt10 = WorkflowAtt.this;
                        workflowAtt10.mSharedInfo.addHttpParam("token", decrypt(workflowAtt10.getToken()));
                        if (!TextUtils.isEmpty(WorkflowAtt.this.getMsisdn())) {
                            WorkflowAtt workflowAtt11 = WorkflowAtt.this;
                            workflowAtt11.mSharedInfo.addHttpParam("msisdn", workflowAtt11.mParamHandler.encodeRFC3986(workflowAtt11.getMsisdn()));
                        }
                        if (WorkflowAtt.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                            WorkflowAtt workflowAtt12 = WorkflowAtt.this;
                            workflowAtt12.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, workflowAtt12.mTelephonyAdapter.getSmsDestPort());
                        }
                        WorkflowAtt workflowAtt13 = WorkflowAtt.this;
                        boolean z = true;
                        if (ImsConstants.SystemSettings.getRcsUserSetting(workflowAtt13.mContext, -1, workflowAtt13.mPhoneId) != 1) {
                            z = false;
                        }
                        String str = WorkflowAtt.LOG_TAG;
                        int i = WorkflowAtt.this.mPhoneId;
                        IMSLog.i(str, i, "isIpmeSwitchOn: " + z);
                        if (WorkflowAtt.this.isClientVendorSetToGoog()) {
                            WorkflowAtt workflowAtt14 = WorkflowAtt.this;
                            if (RcsUtils.isSingleRegiRequiredAndAndroidMessageAppInUsed(workflowAtt14.mContext, workflowAtt14.mPhoneId)) {
                                z = TextUtils.equals(WorkflowAtt.this.mRcsEnabledByUser, "1");
                                int i2 = WorkflowAtt.this.mPhoneId;
                                IMSLog.i(str, i2, "Chat settings from AM is " + z);
                            }
                        }
                        WorkflowAtt workflowAtt15 = WorkflowAtt.this;
                        if (workflowAtt15.mStartForce && !z) {
                            workflowAtt15.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()));
                        }
                        if (WorkflowAtt.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                            String versionBackup = WorkflowAtt.this.getVersionBackup();
                            int i3 = WorkflowAtt.this.mPhoneId;
                            IMSLog.i(str, i3, "DORMANT mode. use backup version :" + versionBackup);
                            WorkflowAtt.this.mSharedInfo.addHttpParam("vers", versionBackup);
                        }
                    }

                    private String decrypt(String str) {
                        if (str == null) {
                            return null;
                        }
                        try {
                            return new String(Base64.decode(str, 0));
                        } catch (IllegalArgumentException unused) {
                            IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "Failed to decrypt the data");
                            return str;
                        }
                    }
                };
            case 4:
                return new WorkflowBase.Authorize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "Authorize:");
                        return super.run();
                    }
                };
            case 5:
                return new WorkflowBase.FetchOtp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "FetchOtp:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttp() {
                        super.setHttp();
                        SharedInfo sharedInfo = WorkflowAtt.this.mSharedInfo;
                        sharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, sharedInfo.getHttpResponse().getHeader().get(HttpController.HEADER_SET_COOKIE));
                        if (!TextUtils.isEmpty(WorkflowAtt.this.getMsisdn())) {
                            WorkflowAtt workflowAtt = WorkflowAtt.this;
                            workflowAtt.mSharedInfo.addHttpParam("msisdn", workflowAtt.mParamHandler.encodeRFC3986(workflowAtt.getMsisdn()));
                        }
                        WorkflowAtt workflowAtt2 = WorkflowAtt.this;
                        workflowAtt2.mSharedInfo.addHttpParam("IMSI", workflowAtt2.mTelephonyAdapter.getImsi());
                        WorkflowAtt workflowAtt3 = WorkflowAtt.this;
                        workflowAtt3.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, workflowAtt3.mTelephonyAdapter.getImei());
                        WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, String.valueOf(0));
                    }
                };
            case 6:
                return new WorkflowBase.Parse() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "Parse:");
                        try {
                            return super.run();
                        } catch (UnknownStatusException unused) {
                            return WorkflowAtt.this.getNextWorkflow(8);
                        }
                    }

                    /* access modifiers changed from: protected */
                    public void parseParam(Map<String, String> map) {
                        WorkflowAtt.this.mParamHandler.parseParamForAtt(map);
                    }
                };
            case 7:
                return new WorkflowBase.Store() {
                    /* JADX WARNING: Removed duplicated region for block: B:8:0x0048  */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public com.sec.internal.ims.config.workflow.WorkflowBase.Workflow run() throws java.lang.Exception {
                        /*
                            r5 = this;
                            java.lang.String r0 = com.sec.internal.ims.config.workflow.WorkflowAtt.LOG_TAG
                            com.sec.internal.ims.config.workflow.WorkflowAtt r1 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            int r1 = r1.mPhoneId
                            java.lang.String r2 = "Store:"
                            com.sec.internal.log.IMSLog.i(r0, r1, r2)
                            com.sec.internal.ims.config.workflow.WorkflowAtt r0 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            java.util.Map r0 = r0.getParsedXml()
                            com.sec.internal.ims.config.workflow.WorkflowAtt r1 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            boolean r1 = r1.isClientVendorSetToGoog()
                            if (r1 == 0) goto L_0x0031
                            com.sec.internal.ims.config.workflow.WorkflowAtt r1 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            android.content.Context r2 = r1.mContext
                            int r1 = r1.mPhoneId
                            boolean r1 = com.sec.internal.ims.rcs.util.RcsUtils.isSingleRegiRequiredAndAndroidMessageAppInUsed(r2, r1)
                            if (r1 == 0) goto L_0x0031
                            com.sec.internal.ims.config.workflow.WorkflowAtt r1 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r2 = r1.getOpMode(r0)
                            r1.setOpMode(r2, r0)
                            goto L_0x003e
                        L_0x0031:
                            com.sec.internal.ims.config.workflow.WorkflowAtt r1 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            com.sec.internal.ims.config.workflow.WorkflowParamHandler r1 = r1.mParamHandler
                            boolean r2 = r1.getUserAccept(r0)
                            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r3 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE
                            r1.setOpModeWithUserAccept(r2, r0, r3)
                        L_0x003e:
                            com.sec.internal.ims.config.workflow.WorkflowAtt r0 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r0 = r0.getOpMode()
                            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.ACTIVE
                            if (r0 != r1) goto L_0x0059
                            com.sec.internal.ims.config.workflow.WorkflowAtt r0 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            int r1 = r0.getValidity()
                            double r1 = (double) r1
                            r3 = 4605380978949069210(0x3fe999999999999a, double:0.8)
                            double r1 = r1 * r3
                            int r1 = (int) r1
                            r0.setValidityTimer(r1)
                        L_0x0059:
                            com.sec.internal.ims.config.workflow.WorkflowAtt r0 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            int r1 = r0.getVersion()
                            r0.setVersionFromServer(r1)
                            com.sec.internal.ims.config.workflow.WorkflowAtt r0 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            r1 = 1
                            r0.isACSsuccessful = r1
                            com.sec.internal.ims.config.workflow.WorkflowAtt r0 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            r1 = 0
                            r0.isLocalConfig = r1
                            com.sec.internal.ims.config.workflow.WorkflowAtt r5 = com.sec.internal.ims.config.workflow.WorkflowAtt.this
                            r0 = 8
                            com.sec.internal.ims.config.workflow.WorkflowBase$Workflow r5 = r5.getNextWorkflow(r0)
                            return r5
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowAtt.AnonymousClass9.run():com.sec.internal.ims.config.workflow.WorkflowBase$Workflow");
                    }
                };
            case 8:
                return new WorkflowBase.Finish() {
                    public WorkflowBase.Workflow run() throws Exception {
                        int version = WorkflowAtt.this.getVersion();
                        String str = WorkflowAtt.LOG_TAG;
                        int i = WorkflowAtt.this.mPhoneId;
                        IMSLog.i(str, i, "Finish: currentVersion=" + version);
                        if (WorkflowAtt.this.mSharedInfo.getHttpResponse() != null) {
                            WorkflowAtt workflowAtt = WorkflowAtt.this;
                            workflowAtt.setLastErrorCode(workflowAtt.mSharedInfo.getHttpResponse().getStatusCode());
                        }
                        WorkflowAtt.this.createSharedInfo();
                        if (version <= 0) {
                            WorkflowAtt.this.loadPreConfig();
                        }
                        WorkflowAtt.this.setLastSwVersion(ConfigConstants.BUILD.TERMINAL_SW_VERSION);
                        return null;
                    }
                };
            default:
                return null;
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponse(WorkflowBase.Workflow workflow, int i) throws InvalidHeaderException, UnknownStatusException {
        this.mLastErrorCode = i;
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "handleResponse: mLastErrorCode: " + this.mLastErrorCode);
        addEventLog(str + ": handleResponse: mLastErrorCode: " + this.mLastErrorCode);
        if (i != 0) {
            if (i == 301) {
                IMSLog.i(str, this.mPhoneId, "http redirects");
                this.mSharedInfo.setUrl((String) this.mSharedInfo.getHttpResponse().getHeader().get("Location").get(0));
                this.mHttpRedirect = true;
                return getNextWorkflow(1);
            } else if (i == 401 || i == 403) {
                return getNextWorkflow(8);
            } else {
                if (i != 511) {
                    if (!(i == 800 || i == 801)) {
                        return super.handleResponse(workflow, i);
                    }
                } else if (workflow instanceof WorkflowBase.FetchHttp) {
                    return getNextWorkflow(3);
                } else {
                    setToken("", DiagnosisConstants.RCSA_TDRE.INVALID_TOKEN);
                    return getNextWorkflow(8);
                }
            }
        }
        IMSLog.i(str, this.mPhoneId, "Failed to reach ACS");
        this.isFailedToConnect = true;
        return getNextWorkflow(8);
    }

    /* access modifiers changed from: private */
    public String getMsisdn() {
        String str = this.mMsisdn;
        if (str != null) {
            return str;
        }
        String msisdn = this.mTelephonyAdapter.getMsisdn();
        IMSLog.i(LOG_TAG, this.mPhoneId, "getMsisdn: use telephony msisdn");
        return msisdn;
    }

    private void registerAirplaneModeObserver() {
        if (!this.isAirplaneModeObserverRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "registerAirplaneModeObserver");
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), false, this.mAirplaneModeObserver);
            this.isAirplaneModeObserverRegistered = true;
        }
    }

    private void unregisterAirplaneModeObserver() {
        if (this.isAirplaneModeObserverRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterAirplaneModeObserver");
            this.mContext.getContentResolver().unregisterContentObserver(this.mAirplaneModeObserver);
            this.isAirplaneModeObserverRegistered = false;
        }
    }

    private boolean isBinarySMSForcedEvent() {
        return this.mStartForce && !this.isMainSwitchToggled;
    }

    private boolean isReadyAndroidMessage() {
        return !this.mStartForce && !isClientVendorSetToGoog() && ConfigUtil.isGoogDmaPackageInuse(this.mContext, this.mPhoneId);
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow workflow;
        String str;
        if (isBinarySMSForcedEvent()) {
            setNextAutoconfigTimeAfter(0);
            cancelValidityTimer();
        } else if (((int) ((this.expirationTime - new Date().getTime()) / 1000)) > 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "30 sec has not passed after previous Autoconfig");
            if (getVersion() == 0) {
                loadPreConfig();
                return;
            }
            return;
        } else if (this.mIsFirstImsRegistrationDone && !isNetworkAvailable()) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, this.mPhoneId, "No network connection, try when connected");
            addEventLog(str2 + ": no network connection, try when connected");
            this.mModule.getHandler().sendMessage(obtainMessage(17, Integer.valueOf(this.mPhoneId)));
            return;
        }
        this.isMainSwitchToggled = false;
        stopResetTokenTimer();
        if (this.mIsRetry) {
            this.mIsRetry = false;
        } else {
            this.mTrialCount = 0;
        }
        WorkflowBase.Workflow nextWorkflow = getNextWorkflow(1);
        if (!this.mIsFirstImsRegistrationDone) {
            String str3 = LOG_TAG;
            IMSLog.i(str3, this.mPhoneId, "The first IMS registration didn't happen yet: skip autoconfig");
            addEventLog(str3 + ": IMS is not yet registered, skip autoconfig");
            IMSLog.c(LogClass.WFA_NO_FIRST_REGI, this.mPhoneId + ",NOFR");
            nextWorkflow = getNextWorkflow(8);
            this.isACSsuccessful = true;
            this.mIsAcsSkipped = true;
        } else if (isReadyAndroidMessage()) {
            String str4 = LOG_TAG;
            IMSLog.i(str4, this.mPhoneId, "Android Message is not ready : skip autoconfig");
            addEventLog(str4 + ": Android Message is not ready : skip autoconfig");
            nextWorkflow = getNextWorkflow(8);
            this.isACSsuccessful = true;
            this.mIsAcsSkipped = true;
        } else if (!this.mSetClientInfoCompleted) {
            String str5 = LOG_TAG;
            IMSLog.i(str5, this.mPhoneId, "RCS client info was not set yet: skip autoconfig");
            addEventLog(str5 + ": RCS client info was not set yet, skip autoconfig");
            nextWorkflow = getNextWorkflow(8);
            this.isACSsuccessful = true;
            this.mIsAcsSkipped = true;
        } else {
            this.expirationTime = new Date().getTime() + 30000;
        }
        int i = WorkflowBase.AUTO_CONFIG_MAX_FLOWCOUNT;
        while (workflow != null && i > 0) {
            try {
                workflow = workflow.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException occur:" + e.getMessage());
                this.isFailedToConnect = true;
                e.printStackTrace();
                workflow = getNextWorkflow(8);
            } catch (UnknownStatusException e2) {
                Log.i(LOG_TAG, "UnknownStatusException occur:" + e2.getMessage());
                e2.printStackTrace();
                workflow = getNextWorkflow(8);
            } catch (Exception e3) {
                String str6 = LOG_TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("unknown exception occur:");
                if (e3.getMessage() == null) {
                    str = "";
                } else {
                    str = e3.getMessage();
                }
                sb.append(str);
                Log.i(str6, sb.toString());
                e3.printStackTrace();
                workflow = getNextWorkflow(8);
            }
            i--;
        }
        if (this.isACSsuccessful) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Autoconfig is done");
            this.isACSsuccessful = false;
        } else {
            String str7 = LOG_TAG;
            IMSLog.i(str7, this.mPhoneId, "Autoconfig failed: isFailedToConnect=" + this.isFailedToConnect);
            addEventLog(str7 + ": Autoconfig failed: isFailedToConnect=" + this.isFailedToConnect);
            IMSLog.c(LogClass.WFA_CONNECT_FAILED, this.mPhoneId + ",FAIL,CON:" + this.isFailedToConnect);
            int trialInterval = getTrialInterval();
            int i2 = this.mLastErrorCode;
            if (i2 == 401 || i2 == 403) {
                this.mIsRetry = false;
                cancelValidityTimer();
            } else if (trialInterval < 0) {
                cancelValidityTimer();
                startResetTokenTimer();
                this.mModule.getHandler().sendMessage(obtainMessage(17, Integer.valueOf(this.mPhoneId)));
            } else {
                setValidityTimer(trialInterval);
                this.mTrialCount++;
            }
        }
        this.isFailedToConnect = false;
    }

    private int getTrialInterval() {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "mTrialCount=" + this.mTrialCount);
        int i2 = this.mTrialCount;
        int[] iArr = RETRY_INTERVAL;
        if (i2 < iArr.length) {
            this.mIsRetry = true;
            return iArr[i2];
        }
        IMSLog.i(str, this.mPhoneId, "Trial Count is bigger than retry count. No more retry!");
        this.mIsRetry = false;
        return -1;
    }

    private void startResetTokenTimer() {
        if (getToken() == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "startResetTokenTimer: token doesn't exist, vail.");
            return;
        }
        if (this.mResetTokenIntent != null) {
            stopResetTokenTimer();
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "startResetTokenTimer");
        Intent intent = new Intent(INTENT_TOKEN_EXPIRED_AFTER_MAX_RETRY);
        intent.setPackage(this.mContext.getPackageName());
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        this.mResetTokenIntent = broadcast;
        AlarmTimer.start(this.mContext, broadcast, RESET_TOKEN_TIMEOUT);
    }

    /* access modifiers changed from: private */
    public void stopResetTokenTimer() {
        if (this.mResetTokenIntent != null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "stopResetTokenTimer");
            AlarmTimer.stop(this.mContext, this.mResetTokenIntent);
            this.mResetTokenIntent = null;
        }
    }

    public void cleanup() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup");
        super.cleanup();
        unregisterAirplaneModeObserver();
        unregisterRcsUserSettingObserver();
        unregisterImsRegistrationListener();
    }

    /* access modifiers changed from: private */
    public void loadPreConfig() {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "loadPreConfig");
        addEventLog(str + ": loadPreConfig");
        IMSLog.c(LogClass.WFA_PRE_CONFIG, this.mPhoneId + ",LPC");
        int version = getVersion();
        String resourcesFromFile = ConfigUtil.getResourcesFromFile(this.mContext, this.mPhoneId, ConfigUtil.LOCAL_CONFIG_FILE, "utf-8");
        Map<String, String> parseJson = WorkflowLocalFile.parseJson(resourcesFromFile, "att_preconfig");
        if (parseJson != null) {
            parseJson.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, resourcesFromFile);
            setOpMode(getOpMode(parseJson), parseJson);
            setVersionFromServer(version);
            setLastErrorCode(this.mLastErrorCodeNonRemote);
            this.isLocalConfig = true;
        }
    }

    public void setRcsClientConfiguration(String str, String str2, String str3, String str4, String str5) {
        Bundle bundle = new Bundle();
        bundle.putString("rcsVersion", str);
        bundle.putString("rcsProfile", str2);
        bundle.putString("clientVendor", str3);
        bundle.putString("clientVersion", str4);
        bundle.putString("rcsEnabledByUser", str5);
        sendMessage(obtainMessage(16, bundle));
    }

    /* access modifiers changed from: protected */
    public void handleSwVersionChange(String str) {
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("handleSwVersionChange: last:");
        sb.append(str);
        sb.append(", current:");
        String str3 = ConfigConstants.BUILD.TERMINAL_SW_VERSION;
        sb.append(str3);
        IMSLog.i(str2, i, sb.toString());
        if (!str.equals(str3)) {
            IMSLog.i(str2, this.mPhoneId, "FOTA upgrade happened: clear previous RCS DB");
            clearStorage(DiagnosisConstants.RCSA_TDRE.CHANGE_SWVERSION);
            keepRcsClientConfiguration();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowAtt$13  reason: invalid class name */
    static /* synthetic */ class AnonymousClass13 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
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
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowAtt.AnonymousClass13.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public void setOpMode(WorkflowBase.OpMode opMode, Map<String, String> map) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setOpMode: " + opMode.name());
        int i2 = AnonymousClass13.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[opMode.ordinal()];
        if (i2 == 1) {
            if (map != null) {
                int i3 = this.mPhoneId;
                IMSLog.s(str, i3, "data :" + map);
                int version = getVersion();
                if (!TextUtils.isEmpty(map.get("root/application/1/services/ChatAuth".toLowerCase(Locale.US)))) {
                    IMSLog.i(str, this.mPhoneId, "Received XML has full config");
                    writeDataToStorage(map);
                    keepRcsClientConfiguration();
                } else if (getVersion(map) == version) {
                    IMSLog.i(str, this.mPhoneId, "Received XML does NOT have full config with the same version");
                    int validity = getValidity(map);
                    IMSLog.i(str, this.mPhoneId, "Update validity");
                    setValidity(validity);
                    String token = getToken(map);
                    if (!TextUtils.isEmpty(token) && !TextUtils.equals(token, getToken())) {
                        IMSLog.i(str, this.mPhoneId, "Token is changed so update it");
                        setToken(token, DiagnosisConstants.RCSA_TDRE.UPDATE_TOKEN);
                    }
                } else {
                    IMSLog.i(str, this.mPhoneId, "Received non-full XML and version is different: Ignore");
                }
            } else {
                IMSLog.i(str, this.mPhoneId, "null data. remain previous mode & data");
            }
            setNextAutoconfigTimeAfter((int) (((double) getValidity()) * 0.8d));
        } else if (i2 == 2 || i2 == 3 || i2 == 4) {
            clearStorage(DiagnosisConstants.RCSA_TDRE.DISABLE_RCS);
            keepRcsClientConfiguration();
            setVersion(opMode.value());
            setValidity(opMode.value());
        } else if (i2 == 5) {
            if (getVersion() != WorkflowBase.OpMode.DORMANT.value()) {
                setVersionBackup(getVersion());
            }
            setVersion(opMode.value());
        }
    }

    private void registerResetTokenIntentReceiver() {
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WorkflowAtt.INTENT_TOKEN_EXPIRED_AFTER_MAX_RETRY.equals(intent.getAction())) {
                    IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "onReceive: token expired 24hrs after no_more_retry state");
                    WorkflowAtt.this.stopResetTokenTimer();
                    WorkflowAtt.this.removeToken(DiagnosisConstants.RCSA_TDRE.TOKEN_EXPIRED);
                }
            }
        }, new IntentFilter(INTENT_TOKEN_EXPIRED_AFTER_MAX_RETRY));
    }

    private void registerImsRegistrationListener() {
        if (!this.isImsRegiListenerRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "registerImsRegistrationListener");
            try {
                ImsRegistry.getRegistrationManager().registerListener(this.mImsRegistrationListener, this.mPhoneId);
                this.isImsRegiListenerRegistered = true;
            } catch (Exception e) {
                IMSLog.e(LOG_TAG, this.mPhoneId, "registerImsRegistrationListener failed");
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public void unregisterImsRegistrationListener() {
        if (this.isImsRegiListenerRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterImsRegistrationListener");
            try {
                ImsRegistry.getRegistrationManager().unregisterListener(this.mImsRegistrationListener, this.mPhoneId);
                this.isImsRegiListenerRegistered = false;
            } catch (Exception e) {
                IMSLog.e(LOG_TAG, this.mPhoneId, "unregisterImsRegistrationListener failed");
                e.printStackTrace();
            }
        }
    }
}
