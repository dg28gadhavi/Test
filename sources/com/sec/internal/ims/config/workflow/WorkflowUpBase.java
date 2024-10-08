package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.params.ACSConfig;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IDialogAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;
import com.sec.internal.interfaces.ims.config.IXmlParserAdapter;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class WorkflowUpBase extends WorkflowBase {
    protected static final int AUTH_HIDDENTRY_MAX_COUNT = 3;
    protected static final int AUTH_TRY_MAX_COUNT = 1;
    protected static final int INTERNALERR_RETRY_MAX_COUNT = 5;
    protected static final String LOG_TAG = WorkflowUpBase.class.getSimpleName();
    protected String[] mAlternateVersions;
    protected ConnectivityManager mConnectivityManager;
    protected int mNewVersion;
    protected int mOldVersion;

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponseForUpOther(WorkflowBase.Workflow workflow, WorkflowBase.Workflow workflow2, WorkflowBase.Workflow workflow3) throws InvalidHeaderException {
        return null;
    }

    /* access modifiers changed from: package-private */
    public void work() {
    }

    public WorkflowUpBase(Looper looper, Context context, IConfigModule iConfigModule, Mno mno, ITelephonyAdapter iTelephonyAdapter, IStorageAdapter iStorageAdapter, IHttpAdapter iHttpAdapter, IXmlParserAdapter iXmlParserAdapter, IDialogAdapter iDialogAdapter, int i) {
        super(looper, context, iConfigModule, mno, iTelephonyAdapter, iStorageAdapter, iHttpAdapter, iXmlParserAdapter, iDialogAdapter, i);
        this.mConnectivityManager = null;
        this.mOldVersion = 0;
        this.mNewVersion = 0;
        this.mAlternateVersions = ImsRegistry.getStringArray(this.mPhoneId, GlobalSettingsConstants.RCS.ALT_PROVISIONING_VERSION, (String[]) null);
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        int i2 = this.mPhoneId;
        this.mRcsUPProfile = ImsRegistry.getString(i2, GlobalSettingsConstants.RCS.UP_PROFILE, ImsRegistry.getRcsProfileType(i2));
    }

    public void startAutoConfig(boolean z) {
        if (this.mIsConfigOngoing) {
            Log.i(LOG_TAG, "startAutoConfig ongoing");
            return;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "startAutoConfig mobile: " + z);
        this.mMobileNetwork = z;
        sendEmptyMessage(1);
    }

    public void forceAutoConfig(boolean z) {
        if (this.mIsConfigOngoing) {
            Log.i(LOG_TAG, "forceAutoConfig ongoing");
            return;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "forceAutoConfig mobile:" + z);
        this.mMobileNetwork = z;
        sendEmptyMessage(0);
    }

    public void forceAutoConfigNeedResetConfig(boolean z) {
        if (this.mIsConfigOngoing) {
            Log.i(LOG_TAG, "forceAutoConfigNeedResetConfig ongoing");
            return;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "forceAutoConfigNeedResetConfig mobile:" + z);
        this.mMobileNetwork = z;
        ACSConfig acsConfig = this.mModule.getAcsConfig(this.mPhoneId);
        if (getVersion() != -2 || !acsConfig.isTriggeredByNrcr() || !this.mMno.isOneOf(Mno.SWISSCOM, Mno.TMOBILE)) {
            setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
        } else {
            setOpMode(WorkflowBase.OpMode.DISABLE, (Map<String, String>) null);
            acsConfig.setAcsVersion(-2);
        }
        sendEmptyMessage(0);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponseForUp(WorkflowBase.Workflow workflow, WorkflowBase.Workflow workflow2, WorkflowBase.Workflow workflow3) throws InvalidHeaderException, UnknownStatusException {
        String str;
        IHttpAdapter.Response httpResponse = this.mSharedInfo.getHttpResponse();
        setLastErrorCode(httpResponse.getStatusCode());
        String str2 = LOG_TAG;
        IMSLog.i(str2, this.mPhoneId, "handleResponseForUp: mLastErrorCode: " + this.mLastErrorCode);
        addEventLog(str2 + "handleResponseForUp: mLastErrorCode: " + this.mLastErrorCode);
        String str3 = null;
        if (this.mAlternateVersions != null) {
            str = (String) Optional.ofNullable(httpResponse.getHeader().get(HttpRequest.HEADER_SUPPORTED_VERSIONS)).map(new WorkflowUpBase$$ExternalSyntheticLambda4()).orElse((Object) null);
            if (str != null) {
                setAcsSeverSupportedVersions(str.trim());
            }
        } else {
            str = null;
        }
        if (this.mMno.isOneOf(Mno.TELEFONICA_GERMANY, Mno.TELEFONICA_SPAIN, Mno.TELEFONICA_UK)) {
            return handleResponseForUpOther(workflow, workflow2, workflow3);
        }
        int i = this.mLastErrorCode;
        if (i != 0) {
            if (i == 200) {
                Log.i(str2, "normal case");
            } else if (i == 403) {
                Log.i(str2, "set version to zero");
                setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            } else if (i == 406) {
                if (this.mAlternateVersions != null) {
                    String string = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PROVISIONING_VERSION, "2.0");
                    if (str != null) {
                        if (!str.contains(string)) {
                            String[] strArr = this.mAlternateVersions;
                            int length = strArr.length;
                            int i2 = 0;
                            while (true) {
                                if (i2 >= length) {
                                    break;
                                }
                                String str4 = strArr[i2];
                                if (str.contains(str4)) {
                                    str3 = str4;
                                    break;
                                }
                                i2++;
                            }
                        } else {
                            str3 = string;
                        }
                    }
                }
                if (str3 != null) {
                    this.mRcsProvisioningVersion = str3;
                }
            } else if (i == 500) {
                Log.i(str2, "internal server error");
                int internalErrRetryCount = this.mSharedInfo.getInternalErrRetryCount();
                Log.i(str2, "retryCount: " + internalErrRetryCount);
                if (internalErrRetryCount <= 5) {
                    this.mSharedInfo.setInternalErrRetryCount(internalErrRetryCount + 1);
                    return workflow;
                }
            } else if (i == 503) {
                long j = getretryAfterTime();
                Log.i(str2, "retry after " + j + " sec");
                int i3 = (int) j;
                setValidityTimer(i3);
                setNextAutoconfigTimeAfter(i3);
                if (this.mMno == Mno.SWISSCOM && this.mSharedInfo.getHttpParams().containsKey(ConfigConstants.PNAME.OTP)) {
                    Log.d(str2, "got 503 response for request with OTP, restart autoconf");
                    this.mSharedInfo.setHttpResponse((IHttpAdapter.Response) null);
                    this.mSharedInfo.setHttpClean();
                }
            } else if (i == 511) {
                Log.i(str2, "The token is no longer valid");
                setToken("", DiagnosisConstants.RCSA_TDRE.INVALID_TOKEN);
                removeValidToken();
                return workflow;
            } else if (i == 800) {
                Log.i(str2, "SSL handshake is failed");
            } else {
                throw new UnknownStatusException("unknown http status code");
            }
            return workflow2;
        }
        Log.i(str2, "RCS configuration server is unreachable");
        return workflow3;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ String lambda$handleResponseForUp$0(List list) {
        return (String) list.get(0);
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateData(Map<String, String> map) {
        String token = getToken();
        String token2 = getToken(map);
        String str = LOG_TAG;
        IMSLog.s(str, "checkAndUpdateData: oldToken: " + token + " newToken: " + token2);
        if (!TextUtils.isEmpty(token2) && !token2.equals(token)) {
            Log.i(str, "checkAndUpdateData: token is changed, update it");
            setToken(token2, DiagnosisConstants.RCSA_TDRE.UPDATE_TOKEN);
        }
        String str2 = "";
        String valueOf = getVersion() > 0 ? String.valueOf(getValidity()) : str2;
        if (getVersion(map) > 0) {
            str2 = String.valueOf(getValidity(map));
        }
        IMSLog.s(str, "checkAndUpdateData: oldValidity: " + valueOf + " newValidity: " + str2);
        if (!TextUtils.isEmpty(str2) && !str2.equals(valueOf)) {
            Log.i(str, "checkAndUpdateData: validity is changed, update it");
            setValidity(Integer.parseInt(str2));
        }
    }

    /* access modifiers changed from: protected */
    public void setOpMode(WorkflowBase.OpMode opMode, Map<String, String> map) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setOpMode: mode: " + opMode.name());
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[opMode.ordinal()]) {
            case 1:
                if (map == null) {
                    setActiveOpModeWithEmptyData();
                    return;
                } else {
                    setActiveOpMode(map);
                    return;
                }
            case 2:
                if (map != null) {
                    int validity = getValidity(map);
                    if (getVersion(map) == 0 && validity > 0) {
                        setVersion(getVersion(map));
                        setValidity(validity);
                        setNextAutoconfigTimeAfter(validity);
                        return;
                    }
                }
                break;
            case 3:
            case 4:
                break;
            case 5:
                setDormantOpMode(opMode);
                return;
            case 6:
            case 7:
            case 8:
            case 9:
                setDisabledStateOpMode(opMode, map);
                return;
            case 10:
                setDisableRcsByUserOpMode();
                return;
            case 11:
                setEnableRcsByUserOpMode();
                return;
            default:
                Log.i(str, "unknown mode");
                return;
        }
        setDisableOpMode(opMode, map);
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowUpBase$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(22:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|(3:21|22|24)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(24:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|24) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
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
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_RCS_BY_USER     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.ENABLE_RCS_BY_USER     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowUpBase.AnonymousClass1.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDataFullUpdateNeeded(Map<String, String> map) {
        return getVersion() < getVersion(map) || (this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState()));
    }

    /* access modifiers changed from: protected */
    public void setActiveOpMode(Map<String, String> map) {
        String str = LOG_TAG;
        IMSLog.s(str, "data: " + map);
        if (isDataFullUpdateNeeded(map)) {
            writeDataToStorage(map);
            setVersionBackup(getVersion(map));
            String token = getToken(map);
            if (!TextUtils.isEmpty(token)) {
                Log.i(str, "save valid token");
                ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.VALID_RCS_CONFIG, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(this.mPhoneId), token);
            }
        } else {
            Log.i(str, "the same or lower version, remain previous data");
            checkAndUpdateData(map);
        }
        setNextAutoconfigTimeAfter(getValidity());
    }

    /* access modifiers changed from: protected */
    public void setActiveOpModeWithEmptyData() {
        int parsedIntVersionBackup = getParsedIntVersionBackup();
        if (parsedIntVersionBackup >= WorkflowBase.OpMode.ACTIVE.value()) {
            Log.i(LOG_TAG, "retreive backup version of configuration");
            setVersion(parsedIntVersionBackup);
            return;
        }
        Log.i(LOG_TAG, "data is empty, remain previous data and mode");
    }

    /* access modifiers changed from: protected */
    public void setDisableOpMode(WorkflowBase.OpMode opMode, Map<String, String> map) {
        clearStorage(DiagnosisConstants.RCSA_TDRE.DISABLE_RCS);
        if (map != null) {
            this.mStorage.writeAll(map);
        }
        setVersion(opMode.value());
        setValidity(opMode.value());
    }

    /* access modifiers changed from: protected */
    public void setDormantOpMode(WorkflowBase.OpMode opMode) {
        int version = getVersion();
        if (version != WorkflowBase.OpMode.DORMANT.value() && getParsedIntVersionBackup() < WorkflowBase.OpMode.ACTIVE.value()) {
            setVersionBackup(version);
        }
        setVersion(opMode.value());
    }

    /* access modifiers changed from: protected */
    public void setDisabledStateOpMode(WorkflowBase.OpMode opMode, Map<String, String> map) {
        if (map != null) {
            this.mStorage.writeAll(map);
        }
        if (getOpMode() == WorkflowBase.OpMode.ACTIVE) {
            setVersionBackup(getVersion());
        }
        if (opMode == WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE) {
            setVersion(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        } else if (opMode == WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE) {
            setVersion(WorkflowBase.OpMode.DISABLE_PERMANENTLY.value());
        } else if (opMode == WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE) {
            setVersion(WorkflowBase.OpMode.DISABLE.value());
        } else {
            setVersion(WorkflowBase.OpMode.DORMANT.value());
        }
        WorkflowBase.OpMode opMode2 = WorkflowBase.OpMode.DISABLE_TEMPORARY;
        setRcsState(String.valueOf(opMode2.value()));
        cancelValidityTimer();
        setNextAutoconfigTime((long) opMode2.value());
    }

    /* access modifiers changed from: protected */
    public void setDisableRcsByUserOpMode() {
        if (getOpMode() == WorkflowBase.OpMode.ACTIVE) {
            setVersionBackup(getVersion());
        }
        setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()));
        String str = LOG_TAG;
        Log.i(str, "rcsState: " + getRcsState());
        cancelValidityTimer();
        setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
    }

    /* access modifiers changed from: protected */
    public void setEnableRcsByUserOpMode() {
        int parsedIntVersionBackup = getParsedIntVersionBackup();
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        WorkflowBase.OpMode opMode = getOpMode();
        WorkflowBase.OpMode opMode2 = WorkflowBase.OpMode.ACTIVE;
        if (opMode == opMode2) {
            setRcsState(String.valueOf(getVersion()));
        } else if (parsedIntVersionBackup >= opMode2.value()) {
            setVersion(parsedIntVersionBackup);
            setRcsState(getVersionBackup());
        } else {
            clearStorage(DiagnosisConstants.RCSA_TDRE.DISABLE_RCS);
            WorkflowBase.OpMode opMode3 = WorkflowBase.OpMode.DISABLE_TEMPORARY;
            setVersion(opMode3.value());
            setRcsState(String.valueOf(opMode3.value()));
        }
        if (!ConfigUtil.isRcsChn(SimUtil.getSimMno(this.mPhoneId)) || !isValidRcsDisabledState(rcsDisabledState)) {
            setRcsDisabledState("");
        } else {
            setRcsDisabledState(String.valueOf(convertRcsDisabledStateToValue(rcsDisabledState)));
        }
        cancelValidityTimer();
        setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        this.mModule.getAcsConfig(this.mPhoneId).disableRcsByAcs(false);
    }

    public void setEnableRcsByMigration() {
        WorkflowBase.OpMode opMode = WorkflowBase.OpMode.DISABLE_TEMPORARY;
        setVersion(opMode.value());
        setRcsState(String.valueOf(opMode.value()));
        setRcsDisabledState("");
        setValidity(opMode.value());
        cancelValidityTimer();
        setNextAutoconfigTime((long) opMode.value());
    }

    public void changeOpMode(boolean z) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "changeOpMode: isRcsEnabled: " + z);
        if (z) {
            setOpMode(WorkflowBase.OpMode.ENABLE_RCS_BY_USER, (Map<String, String>) null);
        } else {
            setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
        }
    }

    public void registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
        this.mTelephonyAdapter.registerAutoConfigurationListener(iAutoConfigurationListener);
    }

    public void unregisterAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
        this.mTelephonyAdapter.unregisterAutoConfigurationListener(iAutoConfigurationListener);
    }

    public void sendVerificationCode(String str) {
        IMSLog.c(LogClass.WFB_OTP_CODE, this.mPhoneId + ",VC:" + IMSLog.numberChecker(str));
        this.mTelephonyAdapter.sendVerificationCode(str);
    }

    public void sendMsisdnNumber(String str) {
        this.mTelephonyAdapter.sendMsisdnNumber(str);
    }

    public void sendIidToken(String str) {
        this.mTelephonyAdapter.sendIidToken(str);
    }

    /* access modifiers changed from: protected */
    public void registerMobileNetwork(ConnectivityManager connectivityManager, NetworkRequest networkRequest, ConnectivityManager.NetworkCallback networkCallback) {
        try {
            Log.i(LOG_TAG, "register mobile network");
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
            connectivityManager.requestNetwork(networkRequest, networkCallback);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterMobileNetwork(ConnectivityManager connectivityManager, ConnectivityManager.NetworkCallback networkCallback) {
        try {
            Log.i(LOG_TAG, "unregister mobile network");
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkWifiConnection(ConnectivityManager connectivityManager) {
        return Optional.ofNullable(connectivityManager.getActiveNetwork()).map(new WorkflowUpBase$$ExternalSyntheticLambda0(connectivityManager)).filter(new WorkflowUpBase$$ExternalSyntheticLambda1()).filter(new WorkflowUpBase$$ExternalSyntheticLambda2()).filter(new WorkflowUpBase$$ExternalSyntheticLambda3()).isPresent();
    }

    /* access modifiers changed from: protected */
    public boolean checkMobileConnection(ConnectivityManager connectivityManager) {
        return Optional.ofNullable(connectivityManager.getActiveNetwork()).map(new WorkflowUpBase$$ExternalSyntheticLambda5(connectivityManager)).filter(new WorkflowUpBase$$ExternalSyntheticLambda6()).filter(new WorkflowUpBase$$ExternalSyntheticLambda7()).filter(new WorkflowUpBase$$ExternalSyntheticLambda8()).isPresent();
    }

    /* access modifiers changed from: protected */
    public boolean isMobilePreferred() {
        return !checkWifiConnection(this.mConnectivityManager) && checkMobileConnection(this.mConnectivityManager);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState() {
        return getRcsDisabledState(ConfigConstants.CONFIG_TYPE.STORAGE_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE, (Map<String, String>) null);
    }

    /* access modifiers changed from: protected */
    public void setAcsSeverSupportedVersions(String str) {
        this.mStorage.write(ConfigConstants.PATH.SERVER_SUPPORTED_VESIONS, str);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState(Map<String, String> map) {
        return getRcsDisabledState(ConfigConstants.CONFIG_TYPE.PARSEDXML_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE, map);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState(String str, String str2, Map<String, String> map) {
        String str3;
        WorkflowBase.OpMode opMode = WorkflowBase.OpMode.NONE;
        if (ConfigConstants.CONFIG_TYPE.STORAGE_DATA.equals(str)) {
            str3 = this.mStorage.read(str2);
        } else if (!ConfigConstants.CONFIG_TYPE.PARSEDXML_DATA.equals(str) || map == null) {
            str3 = "";
        } else {
            str3 = map.get(str2);
        }
        if (TextUtils.isEmpty(str3)) {
            Log.i(LOG_TAG, "getRcsDisabledState: empty");
            return opMode;
        }
        WorkflowBase.OpMode convertRcsDisabledStateToOpMode = convertRcsDisabledStateToOpMode(str3);
        String str4 = LOG_TAG;
        Log.i(str4, "getRcsDisabledState: mode: " + convertRcsDisabledStateToOpMode.name());
        return convertRcsDisabledStateToOpMode;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode convertRcsDisabledStateToOpMode(String str) {
        WorkflowBase.OpMode opMode = WorkflowBase.OpMode.NONE;
        if (String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()).equals(str)) {
            return WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE;
        }
        if (String.valueOf(WorkflowBase.OpMode.DISABLE_PERMANENTLY.value()).equals(str)) {
            return WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE;
        }
        if (String.valueOf(WorkflowBase.OpMode.DISABLE.value()).equals(str)) {
            return WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE;
        }
        return String.valueOf(WorkflowBase.OpMode.DORMANT.value()).equals(str) ? WorkflowBase.OpMode.DORMANT_BY_RCS_DISABLED_STATE : opMode;
    }

    /* access modifiers changed from: protected */
    public int convertRcsDisabledStateToValue(WorkflowBase.OpMode opMode) {
        int value = WorkflowBase.OpMode.NONE.value();
        if (opMode == WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE) {
            return WorkflowBase.OpMode.DISABLE_TEMPORARY.value();
        }
        if (opMode == WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE) {
            return WorkflowBase.OpMode.DISABLE_PERMANENTLY.value();
        }
        if (opMode == WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE) {
            return WorkflowBase.OpMode.DISABLE.value();
        }
        return opMode == WorkflowBase.OpMode.DORMANT_BY_RCS_DISABLED_STATE ? WorkflowBase.OpMode.DORMANT.value() : value;
    }

    /* access modifiers changed from: protected */
    public void setRcsDisabledState(String str) {
        this.mStorage.write(ConfigConstants.PATH.RCS_DISABLED_STATE, str);
    }

    /* access modifiers changed from: protected */
    public boolean isValidRcsDisabledState(WorkflowBase.OpMode opMode) {
        return opMode == WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE || opMode == WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE || opMode == WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE || opMode == WorkflowBase.OpMode.DORMANT_BY_RCS_DISABLED_STATE;
    }

    /* access modifiers changed from: protected */
    public String convertRcsStateWithSpecificParam() {
        return convertRcsStateWithSpecificParam(getRcsState(), getRcsDisabledState());
    }

    /* access modifiers changed from: protected */
    public String convertRcsStateWithSpecificParam(String str, WorkflowBase.OpMode opMode) {
        if (String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(str)) {
            return str;
        }
        if (isValidRcsDisabledState(opMode)) {
            return String.valueOf(convertRcsDisabledStateToValue(opMode));
        }
        int version = getVersion();
        return isActiveVersion(version) ? String.valueOf(version) : String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
    }

    /* access modifiers changed from: protected */
    public boolean isActiveVersion(int i) {
        return i >= WorkflowBase.OpMode.ACTIVE.value();
    }

    /* access modifiers changed from: protected */
    public String getRcsState() {
        return this.mStorage.read(ConfigConstants.PATH.RCS_STATE);
    }

    /* access modifiers changed from: protected */
    public void setRcsState(String str) {
        this.mStorage.write(ConfigConstants.PATH.RCS_STATE, str);
    }

    /* access modifiers changed from: protected */
    public void executeAutoConfig() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "executeAutoConfig");
        work();
    }

    /* access modifiers changed from: protected */
    public void endAutoConfig(boolean z) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "endAutoConfig: result: " + z);
        if (this.mOldVersion >= 0 && !isValidRcsDisabledState(getRcsDisabledState())) {
            this.mTelephonyAdapter.notifyAutoConfigurationListener(52, !z ? this.mOldVersion > 0 : this.mNewVersion > 0);
        }
        if (this.mSharedInfo.getHttpResponse() != null) {
            setLastErrorCode(this.mSharedInfo.getHttpResponse().getStatusCode());
        }
        setCompleted(true);
        this.mStartForce = false;
        this.mIsConfigOngoing = false;
    }
}
