package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class WorkflowTmo extends WorkflowUpBase {
    static final Uri CONFIG_PARAMS_URI = Uri.parse("content://com.samsung.ims.entitlementconfig.provider/config");
    private static final String DEVICE_CONFIG = "device_config";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = WorkflowTmo.class.getSimpleName();
    private static final String TAG_AUTOCONFIG_HEAD = "<RCSConfig>";
    private static final String TAG_AUTOCONFIG_TAIL = "</RCSConfig>";
    private static final String TAG_NEW_XML_HEADER = "<?xml version=\"1.0\"?>";
    ConfigurationParamObserver mConfigurationParamObserver;
    protected String mConfigurationParams;
    protected boolean mIsNoInitialData = false;
    protected boolean mIsObserverRegistered = false;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowTmo(android.os.Looper r14, android.content.Context r15, com.sec.internal.interfaces.ims.config.IConfigModule r16, com.sec.internal.constants.Mno r17, int r18) {
        /*
            r13 = this;
            r11 = r13
            r12 = r15
            r3 = r16
            r10 = r18
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDevice r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDevice
            r5.<init>(r15, r3, r10)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapter r7 = new com.sec.internal.ims.config.adapters.HttpAdapter
            r7.<init>(r10)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r9.<init>(r15, r3)
            r0 = r13
            r1 = r14
            r2 = r15
            r4 = r17
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            r0 = 0
            r11.mIsObserverRegistered = r0
            r11.mIsNoInitialData = r0
            com.sec.internal.ims.config.workflow.WorkflowTmo$ConfigurationParamObserver r0 = new com.sec.internal.ims.config.workflow.WorkflowTmo$ConfigurationParamObserver
            r0.<init>(r15)
            r11.mConfigurationParamObserver = r0
            r13.registerContentObserver()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowTmo.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: " + message.what);
        int i2 = message.what;
        if (i2 == 0) {
            IMSLog.i(str, this.mPhoneId, "forced startAutoConfig");
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 != 5) {
                super.handleMessage(message);
                return;
            } else if (ConfigUtil.isGoogDmaPackageInuse(this.mContext, this.mPhoneId) && this.mSharedInfo.getParsedXml() != null && RcsUtils.isImsSingleRegiRequired(this.mContext, this.mPhoneId)) {
                IMSLog.i(str, this.mPhoneId, "default app is changed to google, notify Provisioning XML");
                SecImsNotifier.getInstance().notifyRcsAutoConfigurationReceived(this.mPhoneId, this.mParamHandler.getProvisioningXml(false), false);
                return;
            } else {
                return;
            }
        }
        if (this.mIsConfigOngoing) {
            IMSLog.i(str, this.mPhoneId, "AutoConfig:Already started");
            return;
        }
        this.mIsConfigOngoing = true;
        int version = getVersion();
        addEventLog("AutoConfig:START, oldVersion=" + version);
        this.mPowerController.lock();
        work();
        int version2 = getVersion();
        addEventLog("AutoConfig:FINISH, newVersion=" + version2);
        setCompleted(true);
        setLastErrorCode(this.mLastErrorCodeNonRemote);
        this.mModule.getHandler().sendMessage(obtainMessage(3, version, version2, Integer.valueOf(this.mPhoneId)));
        this.mStartForce = false;
        this.mPowerController.release();
        this.mIsConfigOngoing = false;
        if (this.mSharedInfo.getParsedXml() != null && RcsUtils.isImsSingleRegiRequired(this.mContext, this.mPhoneId)) {
            IMSLog.i(str, this.mPhoneId, "notify Provisioning XML");
            SecImsNotifier.getInstance().notifyRcsAutoConfigurationReceived(this.mPhoneId, this.mParamHandler.getProvisioningXml(false), false);
        }
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow workflow;
        WorkflowBase.Workflow nextWorkflow = getNextWorkflow(1);
        while (nextWorkflow != null) {
            try {
                nextWorkflow = nextWorkflow.run();
            } catch (NoInitialDataException e) {
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "NoInitialDataException occur: " + e.getMessage());
                addEventLog(LOG_TAG + ": No valid device config params, skip autoconfig");
                IMSLog.c(LogClass.WFTJ_EXCEPTION, this.mPhoneId + ",NODC");
                this.mIsNoInitialData = true;
                workflow = getNextWorkflow(8);
                e.printStackTrace();
                nextWorkflow = workflow;
            } catch (Exception e2) {
                String str2 = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(str2, i2, "unknown exception occur: " + e2);
                workflow = getNextWorkflow(8);
                e2.printStackTrace();
                nextWorkflow = workflow;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowTmo$6  reason: invalid class name */
    static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|(3:17|18|20)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
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
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DORMANT     // Catch:{ NoSuchFieldError -> 0x0028 }
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
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_PERMANENTLY     // Catch:{ NoSuchFieldError -> 0x003e }
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
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowTmo.AnonymousClass6.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        if (i == 1) {
            return new WorkflowBase.Initialize() {
                public WorkflowBase.Workflow run() throws Exception {
                    IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Initialize:");
                    WorkflowTmo workflowTmo = WorkflowTmo.this;
                    if (workflowTmo.mStartForce) {
                        return workflowTmo.getNextWorkflow(2);
                    }
                    int i = AnonymousClass6.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[workflowTmo.getOpMode().ordinal()];
                    if (i == 1 || i == 2 || i == 3) {
                        return WorkflowTmo.this.getNextWorkflow(2);
                    }
                    if (i == 4 || i == 5) {
                        return WorkflowTmo.this.getNextWorkflow(8);
                    }
                    return null;
                }
            };
        }
        if (i == 2) {
            return new WorkflowBase.FetchHttp() {
                public WorkflowBase.Workflow run() throws Exception {
                    IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Fetch:");
                    if (WorkflowTmo.this.mConfigurationParamObserver.retrieveConfiguration()) {
                        return WorkflowTmo.this.getNextWorkflow(6);
                    }
                    return WorkflowTmo.this.getNextWorkflow(8);
                }
            };
        }
        if (i == 6) {
            return new WorkflowBase.Parse() {
                public WorkflowBase.Workflow run() throws Exception {
                    IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Parse:");
                    WorkflowTmo workflowTmo = WorkflowTmo.this;
                    Map<String, String> parse = workflowTmo.mXmlParser.parse(workflowTmo.mConfigurationParams);
                    if (parse == null) {
                        throw new InvalidXmlException("no parsed xml data.");
                    } else if (parse.get("root/vers/version") == null || parse.get("root/vers/validity") == null) {
                        throw new InvalidXmlException("config xml must contain at least 2 items(version & validity).");
                    } else {
                        WorkflowTmo.this.mParamHandler.moveHttpParam(parse);
                        parse.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, WorkflowTmo.this.mConfigurationParams);
                        WorkflowTmo.this.mSharedInfo.setParsedXml(parse);
                        WorkflowTmo workflowTmo2 = WorkflowTmo.this;
                        workflowTmo2.mConfigurationParams = null;
                        return workflowTmo2.getNextWorkflow(7);
                    }
                }
            };
        }
        if (i == 7) {
            return new WorkflowBase.Store() {
                public WorkflowBase.Workflow run() throws Exception {
                    IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Store:");
                    Map<String, String> parsedXml = WorkflowTmo.this.mSharedInfo.getParsedXml();
                    WorkflowBase.OpMode rcsDisabledState = WorkflowTmo.this.getRcsDisabledState(parsedXml);
                    if (WorkflowTmo.this.isValidRcsDisabledState(rcsDisabledState)) {
                        WorkflowTmo.this.setOpMode(rcsDisabledState, parsedXml);
                        WorkflowTmo workflowTmo = WorkflowTmo.this;
                        workflowTmo.addEventLog(WorkflowTmo.LOG_TAG + ": Receive rcsDisabledState = " + WorkflowTmo.this.convertRcsDisabledStateToValue(rcsDisabledState));
                        return WorkflowTmo.this.getNextWorkflow(8);
                    }
                    WorkflowTmo workflowTmo2 = WorkflowTmo.this;
                    workflowTmo2.setOpMode(workflowTmo2.getOpMode(parsedXml), parsedXml);
                    return WorkflowTmo.this.getNextWorkflow(8);
                }
            };
        }
        if (i != 8) {
            return null;
        }
        return new WorkflowBase.Finish() {
            public WorkflowBase.Workflow run() throws Exception {
                IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Finish:");
                return null;
            }
        };
    }

    /* access modifiers changed from: protected */
    public void setOpMode(WorkflowBase.OpMode opMode, Map<String, String> map) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "new operation mode: " + opMode.name());
        switch (AnonymousClass6.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[opMode.ordinal()]) {
            case 1:
                if (map != null) {
                    if (getVersion() >= getVersion(map)) {
                        IMSLog.i(str, this.mPhoneId, "the same or lower version. update the data");
                    }
                    writeDataToStorage(map);
                    return;
                }
                IMSLog.i(str, this.mPhoneId, "null data. remain previous mode & data");
                return;
            case 2:
            case 4:
            case 5:
                clearStorage(DiagnosisConstants.RCSA_TDRE.DISABLE_RCS);
                setVersion(opMode.value());
                setValidity(opMode.value());
                return;
            case 3:
                if (getVersion() != WorkflowBase.OpMode.DORMANT.value()) {
                    setVersionBackup(getVersion());
                }
                setVersion(opMode.value());
                return;
            case 6:
            case 7:
            case 8:
            case 9:
                setDisabledStateOpMode(opMode, map);
                return;
            default:
                IMSLog.i(str, this.mPhoneId, "setOpMode: unknown");
                return;
        }
    }

    public boolean checkNetworkConnectivity() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "checkNetworkConnectivity is false because device config is used");
        return false;
    }

    private void registerContentObserver() {
        if (!this.mIsObserverRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "ConfigurationParamObserver is registered.");
            this.mConfigurationParamObserver.registerObserver();
            this.mIsObserverRegistered = true;
        }
    }

    private void unregisterContentObserver() {
        if (this.mIsObserverRegistered) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "ConfigurationParamObserver is unregistered.");
            this.mConfigurationParamObserver.unregisterObserver();
            this.mIsObserverRegistered = false;
        }
    }

    public void cleanup() {
        super.cleanup();
        unregisterContentObserver();
        this.mIsNoInitialData = false;
    }

    public void onBootCompleted() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onBootCompleted");
        if (this.mIsNoInitialData) {
            this.mIsNoInitialData = false;
            sendEmptyMessage(0);
        }
    }

    class ConfigurationParamObserver extends ContentObserver {
        private static final int AUTOCONFIG_START_DELAY = 2000;
        private Context mContext;

        ConfigurationParamObserver(Context context) {
            super(new Handler());
            this.mContext = context;
        }

        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "device config is changed so start autoconfiguration.");
            WorkflowTmo workflowTmo = WorkflowTmo.this;
            workflowTmo.addEventLog(WorkflowTmo.LOG_TAG + ": Device config is changed, start autoconfig");
            IMSLog.c(LogClass.WFTJ_ON_CHANGE, WorkflowTmo.this.mPhoneId + ",CHDC");
            WorkflowTmo workflowTmo2 = WorkflowTmo.this;
            workflowTmo2.mIsNoInitialData = false;
            workflowTmo2.sendEmptyMessageDelayed(0, UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        }

        /* access modifiers changed from: package-private */
        public void registerObserver() {
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "registerObserver");
            try {
                this.mContext.getContentResolver().registerContentObserver(WorkflowTmo.CONFIG_PARAMS_URI, false, this);
            } catch (SecurityException e) {
                String r1 = WorkflowTmo.LOG_TAG;
                int i = WorkflowTmo.this.mPhoneId;
                IMSLog.i(r1, i, "registerObserver is failed: " + e.getMessage());
            }
        }

        /* access modifiers changed from: package-private */
        public void unregisterObserver() {
            IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "unregisterObserver");
            try {
                this.mContext.getContentResolver().unregisterContentObserver(this);
            } catch (SecurityException e) {
                String r1 = WorkflowTmo.LOG_TAG;
                int i = WorkflowTmo.this.mPhoneId;
                IMSLog.i(r1, i, "unregisterObserver is failed: " + e.getMessage());
            }
        }

        /* access modifiers changed from: package-private */
        public boolean retrieveConfiguration() throws Exception {
            String str;
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(WorkflowTmo.this.mPhoneId);
            String str2 = "";
            if (simManagerFromSimSlot == null) {
                str = str2;
            } else {
                str = simManagerFromSimSlot.getImsi();
            }
            String r2 = WorkflowTmo.LOG_TAG;
            int i = WorkflowTmo.this.mPhoneId;
            IMSLog.s(r2, i, "imsi: " + str);
            Cursor query = this.mContext.getContentResolver().query(WorkflowTmo.CONFIG_PARAMS_URI, new String[]{"device_config"}, "imsi=?", new String[]{str}, (String) null);
            if (query != null) {
                try {
                    if (query.moveToNext()) {
                        str2 = query.getString(0);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (query != null) {
                query.close();
            }
            if (str2 == null) {
                IMSLog.i(WorkflowTmo.LOG_TAG, WorkflowTmo.this.mPhoneId, "Not the correct imsi");
                return false;
            }
            int indexOf = str2.indexOf(WorkflowTmo.TAG_AUTOCONFIG_HEAD) + 11;
            int indexOf2 = str2.indexOf(WorkflowTmo.TAG_AUTOCONFIG_TAIL);
            try {
                WorkflowTmo.this.mConfigurationParams = str2.substring(indexOf, indexOf2);
                WorkflowTmo workflowTmo = WorkflowTmo.this;
                workflowTmo.mConfigurationParams = WorkflowTmo.TAG_NEW_XML_HEADER + WorkflowTmo.this.mConfigurationParams;
                return true;
            } catch (StringIndexOutOfBoundsException unused) {
                WorkflowTmo.this.mConfigurationParams = null;
                throw new NoInitialDataException("Configuration Params in ContentProvider is not valid");
            }
            throw th;
        }
    }
}
