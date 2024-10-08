package com.sec.internal.ims.config;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.im.strategy.ChnStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigTrigger {
    private static final String EXTRA_IS_IIDTOKEN = "is_iid_token_needed";
    private static final String EXTRA_IS_RCS_REGI = "is_rcs_registered";
    private static final String EXTRA_PHONE_ID = "phoneId";
    private static final String INTENT_ACTION_RCS_AUTOCONFIG_START = "com.android.ims.RCS_AUTOCONFIG_START";
    private static final String LOG_TAG = "ConfigTrigger";
    private static final String MESSAGE_PACKAGE_NAME = "com.samsung.android.messaging";
    private Map<Integer, DiagnosisConstants.RCSA_ATRE> mAcsTryReason = new ConcurrentHashMap();
    private IConfigModule mCm;
    private final Context mContext;
    private boolean mDualSimRcsAutoConfig = false;
    private final SimpleEventLog mEventLog;
    private boolean mNeedResetConfig = false;
    private boolean[] mReAutoConfigPerformed = null;
    private SparseBooleanArray mReadyStartCmdList = new SparseBooleanArray();
    private boolean[] mReadyStartForceCmd = null;
    private IRegistrationManager mRm;
    private Map<Integer, DiagnosisConstants.RCSA_TDRE> mTokenDeletedReason = new ConcurrentHashMap();

    public ConfigTrigger(Context context, IRegistrationManager iRegistrationManager, IConfigModule iConfigModule, SimpleEventLog simpleEventLog) {
        this.mContext = context;
        this.mRm = iRegistrationManager;
        this.mCm = iConfigModule;
        this.mEventLog = simpleEventLog;
        int phoneCount = SimUtil.getPhoneCount();
        if (phoneCount <= 0) {
            String str = LOG_TAG;
            IMSLog.i(str, "an amount of SIM slots (" + phoneCount + ") is not valid");
            return;
        }
        this.mReAutoConfigPerformed = new boolean[phoneCount];
        this.mReadyStartForceCmd = new boolean[phoneCount];
    }

    /* access modifiers changed from: package-private */
    public void resetReAutoConfigOption(int i) {
        try {
            this.mReAutoConfigPerformed[i] = false;
        } catch (ArrayIndexOutOfBoundsException unused) {
            String str = LOG_TAG;
            IMSLog.d(str, "there is no such a SIM slot number: " + i);
        }
    }

    /* access modifiers changed from: protected */
    public void setStateforTriggeringACS(int i) {
        IMSLog.i(LOG_TAG, i, "setStateforTriggeringACS:");
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        List<IRegisterTask> pendingRegistration = this.mRm.getPendingRegistration(i);
        if (simManagerFromSimSlot != null && pendingRegistration != null) {
            Mno simMno = simManagerFromSimSlot.getSimMno();
            if (!simManagerFromSimSlot.hasNoSim() && ConfigUtil.isRcsAvailable(this.mContext, i, simManagerFromSimSlot) && !this.mCm.getAcsConfig(i).isAcsCompleted() && !simMno.isKor()) {
                for (IRegisterTask next : pendingRegistration) {
                    if (isWaitAutoconfig(next) && (next.getState() == RegistrationConstants.RegisterTaskState.IDLE || (next.getState() == RegistrationConstants.RegisterTaskState.CONFIGURED && (simMno == Mno.SPRINT || simMno == Mno.TCE || simMno == Mno.CLARO_ARGENTINA || simMno == Mno.CLARO_COLOMBIA || simMno == Mno.CLARO_BRAZIL || simMno == Mno.TIM_BRAZIL)))) {
                        this.mEventLog.logAndAdd(i, "RegisterTask setState: CONFIGURING");
                        next.setState(RegistrationConstants.RegisterTaskState.CONFIGURING);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setRegisterFromApp(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "setRegisterFromApp:");
        List<IRegisterTask> pendingRegistration = this.mRm.getPendingRegistration(i);
        if (pendingRegistration != null) {
            if (z) {
                for (IRegisterTask next : pendingRegistration) {
                    if (next.isRcsOnly() && next.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONFIGURED)) {
                        IMSLog.i(LOG_TAG, i, "setRegisterFromApp: set AcsCompleteStatus as false");
                        this.mCm.getAcsConfig(i).setAcsCompleteStatus(false);
                    }
                }
                setAcsTryReason(i, DiagnosisConstants.RCSA_ATRE.FROM_APP);
            }
            this.mRm.requestTryRegister(i);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isWaitAutoconfig(IRegisterTask iRegisterTask) {
        int phoneId = iRegisterTask.getPhoneId();
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "isWaitAutoConfig:");
        Mno mno = iRegisterTask.getMno();
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        boolean isSimMobilityActivatedForAmRcs = ImsUtil.isSimMobilityActivatedForAmRcs(this.mContext, phoneId);
        if (!ConfigUtil.getGlobalGcEnabled(this.mContext, phoneId) && (ImsUtil.isSimMobilityActivatedForRcs(phoneId) || isSimMobilityActivatedForAmRcs)) {
            boolean z = true;
            if (iRegisterTask.getProfile().getEnableRcs() || isSimMobilityActivatedForAmRcs) {
                IMSLog.i(str, phoneId, "isWaitAutoConfig: RCS is enabled in SIM mobility");
            } else if (!OmcCode.isKorOpenOmcCode() || !mno.isKor()) {
                z = false;
            }
            if (!z) {
                IMSLog.i(str, phoneId, "isWaitAutoConfig: This is a other country SIM, RCS disabled in SIM mobility");
                return false;
            }
        }
        if (!ConfigUtil.isRcsAvailable(this.mContext, phoneId, simManagerFromSimSlot) || (this.mCm.getAcsConfig(phoneId).isAcsCompleted() && (!mno.isKor() || !this.mCm.getAcsConfig(phoneId).needForceAcs()))) {
            return false;
        }
        return iRegisterTask.getProfile().getNeedAutoconfig();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0115, code lost:
        r2 = !com.sec.internal.ims.registry.ImsRegistry.getConfigModule().isRcsEnabled(r8);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean triggerAutoConfig(boolean r7, int r8, java.util.List<com.sec.internal.interfaces.ims.core.IRegisterTask> r9) {
        /*
            r6 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "triggerAutoConfig: forceAutoconfig: "
            r1.append(r2)
            r1.append(r7)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r0, r8, r1)
            com.sec.internal.interfaces.ims.core.ISimManager r1 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r8)
            r2 = 0
            if (r1 == 0) goto L_0x0134
            if (r9 != 0) goto L_0x0022
            goto L_0x0134
        L_0x0022:
            com.sec.internal.constants.Mno r3 = r1.getSimMno()
            boolean r4 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((int) r8)
            if (r4 != 0) goto L_0x0032
            boolean r4 = com.sec.internal.ims.util.ConfigUtil.isRcsCanada(r3)
            if (r4 == 0) goto L_0x003b
        L_0x0032:
            com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager r4 = com.sec.internal.ims.registry.ImsRegistry.getServiceModuleManager()
            if (r4 == 0) goto L_0x003b
            r4.checkRcsServiceModules(r9, r8)
        L_0x003b:
            boolean r4 = r1.hasNoSim()
            if (r4 != 0) goto L_0x00fd
            android.content.Context r4 = r6.mContext
            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsAvailable(r4, r8, r1)
            if (r1 == 0) goto L_0x00fd
            com.sec.internal.interfaces.ims.config.IConfigModule r1 = r6.mCm
            com.sec.internal.ims.config.params.ACSConfig r1 = r1.getAcsConfig(r8)
            boolean r1 = r1.isAcsCompleted()
            if (r1 == 0) goto L_0x0067
            boolean r1 = r3.isKor()
            if (r1 == 0) goto L_0x00fd
            com.sec.internal.interfaces.ims.config.IConfigModule r1 = r6.mCm
            com.sec.internal.ims.config.params.ACSConfig r1 = r1.getAcsConfig(r8)
            boolean r1 = r1.needForceAcs()
            if (r1 == 0) goto L_0x00fd
        L_0x0067:
            java.lang.String r1 = "triggerAutoConfig: try to start autoConfig"
            com.sec.internal.log.IMSLog.i(r0, r8, r1)
            boolean r0 = r3.isKor()
            if (r0 == 0) goto L_0x0077
            r6.triggerAutoConfigForKor(r7, r8, r9)
            return r2
        L_0x0077:
            android.os.Bundle r9 = new android.os.Bundle
            r9.<init>()
            java.lang.String r0 = "phoneId"
            r9.putInt(r0, r8)
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.SWISSCOM
            if (r3 != r0) goto L_0x00cb
            com.sec.internal.interfaces.ims.core.IRegistrationManager r0 = r6.mRm
            monitor-enter(r0)
            int r1 = com.sec.internal.helper.SimUtil.getOppositeSimSlot(r8)     // Catch:{ all -> 0x00c8 }
            com.sec.internal.interfaces.ims.core.IRegistrationManager r4 = r6.mRm     // Catch:{ all -> 0x00c8 }
            java.util.List r1 = r4.getPendingRegistration(r1)     // Catch:{ all -> 0x00c8 }
            if (r1 == 0) goto L_0x00c3
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x00c8 }
        L_0x0098:
            boolean r4 = r1.hasNext()     // Catch:{ all -> 0x00c8 }
            if (r4 == 0) goto L_0x00c3
            java.lang.Object r4 = r1.next()     // Catch:{ all -> 0x00c8 }
            com.sec.internal.interfaces.ims.core.IRegisterTask r4 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r4     // Catch:{ all -> 0x00c8 }
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = r4.getState()     // Catch:{ all -> 0x00c8 }
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURING     // Catch:{ all -> 0x00c8 }
            if (r4 != r5) goto L_0x0098
            java.lang.String r9 = LOG_TAG     // Catch:{ all -> 0x00c8 }
            java.lang.String r1 = "stop triggerAutoConfig because other slot is configuring"
            com.sec.internal.log.IMSLog.i(r9, r8, r1)     // Catch:{ all -> 0x00c8 }
            com.sec.internal.interfaces.ims.config.IConfigModule r6 = r6.mCm     // Catch:{ all -> 0x00c8 }
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r7)     // Catch:{ all -> 0x00c8 }
            r9 = 25000(0x61a8, float:3.5032E-41)
            r1 = 15
            r6.sendConfigMessageDelayed(r1, r8, r7, r9)     // Catch:{ all -> 0x00c8 }
            monitor-exit(r0)     // Catch:{ all -> 0x00c8 }
            return r2
        L_0x00c3:
            r6.setStateforTriggeringACS(r8)     // Catch:{ all -> 0x00c8 }
            monitor-exit(r0)     // Catch:{ all -> 0x00c8 }
            goto L_0x00ce
        L_0x00c8:
            r6 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x00c8 }
            throw r6
        L_0x00cb:
            r6.setStateforTriggeringACS(r8)
        L_0x00ce:
            boolean r0 = r6.getDualSimRcsAutoConfig()
            r1 = 13
            if (r0 == 0) goto L_0x00f3
            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r3)
            if (r0 == 0) goto L_0x00e6
            com.sec.internal.interfaces.ims.config.IConfigModule r7 = r6.mCm
            android.os.Message r9 = r7.obtainConfigMessage(r1, r9)
            r7.startAutoConfigDualsim(r8, r9)
            goto L_0x00ef
        L_0x00e6:
            com.sec.internal.interfaces.ims.config.IConfigModule r0 = r6.mCm
            android.os.Message r9 = r0.obtainConfigMessage(r1, r9)
            r0.startAutoConfig(r7, r9, r8)
        L_0x00ef:
            r6.setDualSimRcsAutoConfig(r2)
            goto L_0x0133
        L_0x00f3:
            com.sec.internal.interfaces.ims.config.IConfigModule r6 = r6.mCm
            android.os.Message r9 = r6.obtainConfigMessage(r1, r9)
            r6.startAutoConfig(r7, r9, r8)
            goto L_0x0133
        L_0x00fd:
            java.lang.String r7 = "triggerAutoConfig: unable to start autoConfig"
            com.sec.internal.log.IMSLog.i(r0, r8, r7)
            boolean r7 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r3)
            if (r7 != 0) goto L_0x010f
            boolean r7 = r3.isOce()
            if (r7 == 0) goto L_0x0133
        L_0x010f:
            boolean[] r7 = r6.mReAutoConfigPerformed     // Catch:{ NullPointerException -> 0x012c }
            boolean r7 = r7[r8]     // Catch:{ NullPointerException -> 0x012c }
            if (r7 != 0) goto L_0x0133
            com.sec.internal.interfaces.ims.config.IConfigModule r7 = com.sec.internal.ims.registry.ImsRegistry.getConfigModule()     // Catch:{ NullPointerException -> 0x012c }
            boolean r7 = r7.isRcsEnabled(r8)     // Catch:{ NullPointerException -> 0x012c }
            r9 = 1
            r2 = r7 ^ 1
            if (r2 == 0) goto L_0x0133
            java.lang.String r7 = "attempt to start autoConfig will be made one more time"
            com.sec.internal.log.IMSLog.i(r0, r8, r7)     // Catch:{ NullPointerException -> 0x012c }
            boolean[] r6 = r6.mReAutoConfigPerformed     // Catch:{ NullPointerException -> 0x012c }
            r6[r8] = r9     // Catch:{ NullPointerException -> 0x012c }
            goto L_0x0133
        L_0x012c:
            java.lang.String r6 = LOG_TAG
            java.lang.String r7 = "information about performing re-autoconfiguration is unavailable"
            com.sec.internal.log.IMSLog.i(r6, r8, r7)
        L_0x0133:
            return r2
        L_0x0134:
            java.lang.String r6 = "triggerAutoConfig: sm/regiTaskList is null"
            com.sec.internal.log.IMSLog.i(r0, r8, r6)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.ConfigTrigger.triggerAutoConfig(boolean, int, java.util.List):boolean");
    }

    /* access modifiers changed from: protected */
    public void triggerAutoConfigForKor(boolean z, int i, List<IRegisterTask> list) {
        for (IRegisterTask next : list) {
            if (next.isRcsOnly()) {
                RegistrationConstants.RegisterTaskState state = next.getState();
                RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.CONFIGURING;
                if (state != registerTaskState || !isWaitAutoconfig(next)) {
                    if (next.getState() == RegistrationConstants.RegisterTaskState.CONNECTED && isWaitAutoconfig(next)) {
                        this.mEventLog.logAndAdd(i, "RegisterTask setState: CONFIGURING");
                        next.setState(registerTaskState);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt("phoneId", next.getPhoneId());
                    if (this.mCm.getAcsConfig(i).needForceAcs()) {
                        IConfigModule iConfigModule = this.mCm;
                        iConfigModule.startAutoConfig(true, iConfigModule.obtainConfigMessage(13, bundle), i);
                        return;
                    }
                    IConfigModule iConfigModule2 = this.mCm;
                    iConfigModule2.startAutoConfig(z, iConfigModule2.obtainConfigMessage(13, bundle), i);
                    return;
                }
                IMSLog.i(LOG_TAG, i, "triggerAutoConfigForKor: already autoconfiguration is processing and not get complete notify yet");
                return;
            }
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if (simManagerFromSimSlot != null && TextUtils.isEmpty(simManagerFromSimSlot.getMsisdn()) && next.getPdnType() == 11 && next.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
                IMSLog.i(LOG_TAG, i, "triggerAutoConfigForKor: MSISDN is null, try to RCS ACS after registered VoLTE");
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isValidAcsVersion(int i) {
        String str = LOG_TAG;
        IMSLog.i(str, i, "isValidAcsVersion:");
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null || simManagerFromSimSlot.hasNoSim()) {
            return false;
        }
        if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, i)) {
            IMSLog.i(str, i, "DDS set to other SIM");
            return false;
        } else if (ConfigUtil.getGlobalGcEnabled(this.mContext, i) || ConfigUtil.isSimMobilityRCS(this.mContext, i, simManagerFromSimSlot, this.mRm)) {
            boolean z = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 0, i) == 1;
            Integer rcsConfVersion = this.mCm.getRcsConfVersion(i);
            boolean isAcsCompleted = this.mCm.getAcsConfig(i).isAcsCompleted();
            boolean checkMdmRcsStatus = ConfigUtil.checkMdmRcsStatus(this.mContext, i);
            IMSLog.i(str, i, "RCS switch: " + z + ", version: " + rcsConfVersion + ", isRcsAcsCompleted: " + isAcsCompleted);
            if (!checkMdmRcsStatus) {
                IMSLog.i(str, i, "RCS service isn't allowed by MDM");
                return false;
            } else if (!isAcsCompleted) {
                IMSLog.i(str, i, "RCS switch is on & config version: " + rcsConfVersion + ". This shouldn't happen!");
                return z;
            } else {
                if (!z) {
                    Mno simMno = simManagerFromSimSlot.getSimMno();
                    String acsServerType = ConfigUtil.getAcsServerType(i);
                    if (!(simMno == Mno.ATT || simMno == Mno.VZW) || ImsConstants.RCS_AS.JIBE.equals(acsServerType)) {
                        IMSLog.i(str, i, "userSetting is disabled");
                        return false;
                    }
                }
                if (rcsConfVersion != null && rcsConfVersion.intValue() != 0 && rcsConfVersion.intValue() >= 0) {
                    return true;
                }
                IMSLog.i(str, i, "version is improper : " + rcsConfVersion);
                return false;
            }
        } else {
            IMSLog.i(str, i, "isValidAcsVersion: This is a other country SIM, RCS disabled in SIM mobility");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void tryAutoConfig(IWorkflow iWorkflow, int i, boolean z, boolean z2) {
        String str = LOG_TAG;
        IMSLog.i(str, i, "tryAutoConfig: mobileNetwork: " + z2);
        if (iWorkflow == null) {
            IMSLog.i(str, i, "tryAutoConfig: workflow is null");
            return;
        }
        IMSLog.c(LogClass.CM_TRY_ACS, i + ",FORCE:" + getReadyStartForceCmd(i) + ",RST:" + getNeedResetConfig());
        this.mCm.getAvailableNetwork(i);
        boolean updateMobileNetworkforDualRcs = this.mCm.updateMobileNetworkforDualRcs(i);
        StringBuilder sb = new StringBuilder();
        sb.append("tryAutoConfig: updateMobileNetworkforDualRcs: ");
        sb.append(updateMobileNetworkforDualRcs);
        IMSLog.i(str, i, sb.toString());
        if (getDualSimRcsAutoConfig()) {
            iWorkflow.startAutoConfigDualsim(updateMobileNetworkforDualRcs);
            setDualSimRcsAutoConfig(false);
        } else if (getReadyStartForceCmd(i)) {
            if (getNeedResetConfig()) {
                iWorkflow.forceAutoConfigNeedResetConfig(updateMobileNetworkforDualRcs);
                setNeedResetConfig(false);
            } else {
                iWorkflow.forceAutoConfig(updateMobileNetworkforDualRcs);
            }
            setReadyStartForceCmd(i, false);
        } else {
            IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
            if (!z || !(rcsStrategy instanceof ChnStrategy)) {
                iWorkflow.startAutoConfig(updateMobileNetworkforDualRcs);
            } else {
                iWorkflow.forceAutoConfig(updateMobileNetworkforDualRcs);
            }
        }
        if (!ConfigUtil.isRcsChn(SimUtil.getSimMno(i))) {
            setReadyStartCmdList(i, false);
        }
    }

    /* access modifiers changed from: protected */
    public void startAutoConfig(boolean z, Message message, int i) {
        String str = LOG_TAG;
        IMSLog.i(str, i, "startAutoConfig: forced: " + z);
        IMSLog.c(LogClass.CM_START_ACS, i + ",FORCE:" + z);
        if (!z) {
            startConfig(2, message, i);
        } else {
            startConfig(1, message, i);
        }
    }

    /* access modifiers changed from: protected */
    public void startAutoConfigDualsim(int i, Message message) {
        startConfig(9, message, i);
    }

    /* access modifiers changed from: protected */
    public void startConfig(int i, Message message, int i2) {
        this.mCm.sendConfigMessage(0, i2);
        String str = LOG_TAG;
        IMSLog.i(str, i2, "startConfig: cmd: " + i);
        if (i == 1 || i == 2) {
            sendRcsAutoconfigStart(i2);
        } else {
            if (!(i == 20 || i == 27)) {
                switch (i) {
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        break;
                    case 9:
                        break;
                    default:
                        Log.i(str, "unknown cmd");
                        return;
                }
            }
            this.mCm.sendConfigMessage(i, i2);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("onComplete: ");
        sb.append(message != null ? message.toString() : "null");
        Log.i(str, sb.toString());
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i2, "Autoconfig start: cmd: " + i);
        this.mCm.sendConfigMessage(i, i2);
    }

    /* access modifiers changed from: protected */
    public void setAcsTryReason(int i, DiagnosisConstants.RCSA_ATRE rcsa_atre) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "setAutoconfigTryReason: " + rcsa_atre.toString());
        IMSLog.c(LogClass.CM_ACS_TRY_REASON, i + ",TR:" + rcsa_atre.toString());
        this.mAcsTryReason.put(Integer.valueOf(i), rcsa_atre);
    }

    /* access modifiers changed from: protected */
    public DiagnosisConstants.RCSA_ATRE getAcsTryReason(int i) {
        DiagnosisConstants.RCSA_ATRE rcsa_atre = this.mAcsTryReason.get(Integer.valueOf(i));
        if (rcsa_atre != null) {
            return rcsa_atre;
        }
        DiagnosisConstants.RCSA_ATRE rcsa_atre2 = DiagnosisConstants.RCSA_ATRE.INIT;
        this.mAcsTryReason.put(Integer.valueOf(i), rcsa_atre2);
        return rcsa_atre2;
    }

    /* access modifiers changed from: protected */
    public void resetAcsTryReason(int i) {
        DiagnosisConstants.RCSA_ATRE rcsa_atre = this.mAcsTryReason.get(Integer.valueOf(i));
        if (rcsa_atre == null || rcsa_atre != DiagnosisConstants.RCSA_ATRE.INIT) {
            this.mAcsTryReason.put(Integer.valueOf(i), DiagnosisConstants.RCSA_ATRE.INIT);
        }
    }

    /* access modifiers changed from: protected */
    public void setTokenDeletedReason(int i, DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "setAutoconfigTryReason: " + rcsa_tdre.toString());
        IMSLog.c(LogClass.CM_ACS_TRY_REASON, i + ",TR:" + rcsa_tdre.toString());
        this.mTokenDeletedReason.put(Integer.valueOf(i), rcsa_tdre);
    }

    /* access modifiers changed from: protected */
    public DiagnosisConstants.RCSA_TDRE getTokenDeletedReason(int i) {
        return this.mTokenDeletedReason.computeIfAbsent(Integer.valueOf(i), new ConfigTrigger$$ExternalSyntheticLambda0());
    }

    /* access modifiers changed from: protected */
    public void resetTokenDeletedReason(int i) {
        if (this.mTokenDeletedReason.get(Integer.valueOf(i)) == null) {
            this.mTokenDeletedReason.put(Integer.valueOf(i), DiagnosisConstants.RCSA_TDRE.INIT);
        }
    }

    /* access modifiers changed from: protected */
    public boolean getDualSimRcsAutoConfig() {
        return this.mDualSimRcsAutoConfig;
    }

    /* access modifiers changed from: protected */
    public void setDualSimRcsAutoConfig(boolean z) {
        String str = LOG_TAG;
        Log.i(str, "setDualSimRcsAutoConfig: isDualSimAcs: " + z);
        this.mDualSimRcsAutoConfig = z;
    }

    /* access modifiers changed from: protected */
    public boolean getReadyStartCmdList(int i) {
        return this.mReadyStartCmdList.get(i);
    }

    /* access modifiers changed from: protected */
    public void setReadyStartCmdList(int i, boolean z) {
        this.mReadyStartCmdList.put(i, z);
    }

    /* access modifiers changed from: protected */
    public int getReadyStartCmdListIndexOfKey(int i) {
        return this.mReadyStartCmdList.indexOfKey(i);
    }

    /* access modifiers changed from: protected */
    public boolean getReadyStartForceCmd(int i) {
        return this.mReadyStartForceCmd[i];
    }

    /* access modifiers changed from: protected */
    public void setReadyStartForceCmd(int i, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "setReadyStartForceCmd[" + i + "]: readyStartForceCmd: " + z);
        this.mReadyStartForceCmd[i] = z;
    }

    /* access modifiers changed from: protected */
    public boolean getNeedResetConfig() {
        return this.mNeedResetConfig;
    }

    /* access modifiers changed from: protected */
    public void setNeedResetConfig(boolean z) {
        String str = LOG_TAG;
        Log.i(str, "setNeedResetConfig: needResetConfig: " + z);
        this.mNeedResetConfig = z;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0079  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean tryAutoconfiguration(com.sec.internal.interfaces.ims.core.IRegisterTask r9) {
        /*
            r8 = this;
            int r0 = r9.getPhoneId()
            com.sec.internal.interfaces.ims.config.IConfigModule r1 = r8.mCm
            com.sec.internal.ims.config.params.ACSConfig r1 = r1.getAcsConfig(r0)
            android.content.Context r2 = r8.mContext
            r3 = 1
            int r2 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getRcsUserSetting(r2, r3, r0)
            r4 = 0
            if (r2 != r3) goto L_0x0016
            r2 = r3
            goto L_0x0017
        L_0x0016:
            r2 = r4
        L_0x0017:
            if (r1 == 0) goto L_0x0044
            int r5 = r1.getAcsVersion()
            r6 = -2
            if (r5 != r6) goto L_0x0044
            if (r2 == 0) goto L_0x0044
            java.lang.String r2 = LOG_TAG
            java.lang.String r5 = "ACS version: -2, IMS RCS switch enabled - set force autoconfig NOW."
            android.util.Log.i(r2, r5)
            com.sec.internal.constants.Mno r5 = r9.getMno()
            boolean r5 = com.sec.internal.ims.util.ConfigUtil.isRcsChn(r5)
            if (r5 == 0) goto L_0x003f
            boolean r5 = r1.isRcsDisabled()
            if (r5 == 0) goto L_0x003f
            java.lang.String r5 = "CHN Need block ACS when user setting is ON."
            android.util.Log.i(r2, r5)
            goto L_0x0044
        L_0x003f:
            r1.clear()
            r2 = r3
            goto L_0x0045
        L_0x0044:
            r2 = r4
        L_0x0045:
            com.sec.internal.interfaces.ims.core.IRegistrationManager r5 = r8.mRm
            java.util.List r5 = r5.getPendingRegistration(r0)
            boolean r6 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.util.Collection<?>) r5)
            if (r6 == 0) goto L_0x0052
            return r4
        L_0x0052:
            boolean r6 = r8.isWaitAutoconfig(r9)
            if (r6 == 0) goto L_0x00a3
            java.lang.String r6 = LOG_TAG
            java.lang.String r7 = "autoconfig is not ready"
            com.sec.internal.log.IMSLog.i(r6, r0, r7)
            com.sec.internal.constants.Mno r0 = r9.getMno()
            boolean r0 = r0.isKor()
            if (r0 == 0) goto L_0x0079
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r0 = r9.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTED
            if (r0 != r1) goto L_0x00a3
            int r9 = r9.getPhoneId()
            r8.triggerAutoConfig(r4, r9, r5)
            return r3
        L_0x0079:
            if (r1 == 0) goto L_0x009b
            boolean r0 = r1.isRcsDisabled()
            if (r0 == 0) goto L_0x009b
            com.sec.internal.constants.Mno r0 = r9.getMno()
            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isRcsEurNonRjil(r0)
            if (r0 != 0) goto L_0x0095
            com.sec.internal.constants.Mno r0 = r9.getMno()
            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isRcsChn(r0)
            if (r0 == 0) goto L_0x009b
        L_0x0095:
            java.lang.String r8 = "Version & validity == 0. Autoconfiguration will be performed after next reboot"
            android.util.Log.i(r6, r8)
            return r3
        L_0x009b:
            int r9 = r9.getPhoneId()
            r8.triggerAutoConfig(r2, r9, r5)
            return r3
        L_0x00a3:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.ConfigTrigger.tryAutoconfiguration(com.sec.internal.interfaces.ims.core.IRegisterTask):boolean");
    }

    /* access modifiers changed from: protected */
    public void setRcsClientConfiguration(int i, IWorkflow iWorkflow, String str, String str2, String str3, String str4, String str5) {
        boolean isGoogDmaPackageInuse = ConfigUtil.isGoogDmaPackageInuse(this.mContext, i);
        String str6 = LOG_TAG;
        IMSLog.i(str6, i, "setRcsClientConfiguration: isAmDefault: " + isGoogDmaPackageInuse);
        if (RcsUtils.isImsSingleRegiRequired(this.mContext, i) && isGoogDmaPackageInuse && iWorkflow != null) {
            IMSLog.i(str6, i, "setRcsClientConfiguration: imsSingleRegi is required: try to set rcc info");
            iWorkflow.setRcsClientConfiguration(str, str2, str3, str4, str5);
        }
    }

    /* access modifiers changed from: protected */
    public void triggerAutoConfiguration(int i) {
        boolean isGoogDmaPackageInuse = ConfigUtil.isGoogDmaPackageInuse(this.mContext, i);
        String rcsClientConfiguration = SecImsNotifier.getInstance().getRcsClientConfiguration(i, 2);
        String str = LOG_TAG;
        IMSLog.i(str, i, "triggerAutoConfiguration: isAmDefault: " + isGoogDmaPackageInuse + "clientVendor: " + rcsClientConfiguration);
        if (TextUtils.equals(rcsClientConfiguration, ConfigConstants.PVALUE.GOOG_DEFAULT_CLIENT_VENDOR) && isGoogDmaPackageInuse) {
            IMSLog.i(str, i, "triggerAutoConfiguration: imsSingleRegi is required: try to trigger autoConfig forcibly");
            List<IRegisterTask> pendingRegistration = this.mRm.getPendingRegistration(i);
            if (pendingRegistration != null) {
                for (IRegisterTask profile : pendingRegistration) {
                    if (profile.getProfile().getNeedAutoconfig()) {
                        this.mCm.getAcsConfig(i).setAcsCompleteStatus(false);
                        triggerAutoConfig(true, i, pendingRegistration);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendRcsAutoconfigStart(int i) {
        String acsServerType = ConfigUtil.getAcsServerType(i);
        Mno simMno = SimUtil.getSimMno(i);
        String valueOf = String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY);
        IStorageAdapter storage = this.mCm.getStorage(i);
        if (storage != null) {
            valueOf = storage.read(ConfigConstants.PATH.RCS_STATE);
        }
        boolean isIidTokenNeeded = ConfigUtil.isIidTokenNeeded(this.mContext, i, valueOf);
        boolean z = false;
        for (ImsRegistration next : SlotBasedConfig.getInstance(i).getImsRegistrations().values()) {
            if (next.hasService("options") || next.hasService("im") || next.hasService("slm")) {
                z = true;
                continue;
            } else {
                z = false;
                continue;
            }
            if (z) {
                break;
            }
        }
        if (!ImsConstants.RCS_AS.JIBE.equals(acsServerType) && !ImsConstants.RCS_AS.SEC.equals(acsServerType) && !simMno.isEmeasewaoce()) {
            return;
        }
        if (this.mCm.isConfigModuleBootUp() || this.mCm.isMessagingReady()) {
            Log.i(LOG_TAG, "sendBroadcast com.android.ims.RCS_AUTOCONFIG_START");
            Intent intent = new Intent();
            intent.setAction(INTENT_ACTION_RCS_AUTOCONFIG_START);
            intent.putExtra(EXTRA_IS_IIDTOKEN, isIidTokenNeeded);
            intent.putExtra(EXTRA_IS_RCS_REGI, z);
            intent.putExtra("phoneId", i);
            intent.setPackage("com.samsung.android.messaging");
            intent.addFlags(LogClass.SIM_EVENT);
            this.mContext.sendBroadcast(intent);
            return;
        }
        this.mCm.sendConfigMessageDelayed(26, i, (Object) null, 2000);
    }
}
