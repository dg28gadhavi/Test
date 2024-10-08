package com.sec.internal.ims.core;

import android.content.Context;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.google.SecImsServiceConnector;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IMSLogTimer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

abstract class RegistrationManagerInternal extends RegistrationManager {
    /* access modifiers changed from: protected */
    public abstract void notifyImsNotAvailable(RegisterTask registerTask, boolean z);

    /* access modifiers changed from: protected */
    public abstract void notifyImsNotAvailable(RegisterTask registerTask, boolean z, boolean z2);

    /* access modifiers changed from: protected */
    public abstract void onDelayedDeregister(RegisterTask registerTask);

    RegistrationManagerInternal(IImsFramework iImsFramework, Context context, PdnController pdnController, List<ISimManager> list, ITelephonyManager iTelephonyManager, ICmcAccountManager iCmcAccountManager, IRcsPolicyManager iRcsPolicyManager) {
        this.mContext = context;
        this.mEmmCause = -1;
        this.mEventLog = new SimpleEventLog(context, IRegistrationManager.LOG_TAG, 3000);
        this.mImsFramework = iImsFramework;
        this.mTelephonyManager = iTelephonyManager;
        this.mPdnController = pdnController;
        this.mSimManagers = list;
        this.mCmcAccountManager = iCmcAccountManager;
        this.mRcsPolicyManager = iRcsPolicyManager;
        this.mSecImsServiceConnector = new SecImsServiceConnector(context);
        this.mAuEmergencyProfile = new SparseArray<>();
    }

    public void initSequentially() {
        this.mNetEvtCtr.setRegistrationHandler(this.mHandler);
        this.mHandler.init();
        this.mlegacyPhoneCount = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getPhoneCount();
    }

    private boolean updateSuspended(RegisterTask registerTask, boolean z, int i) {
        if (i == 0) {
            if (z == registerTask.mSuspended) {
                return false;
            }
            registerTask.mSuspended = z;
            if (registerTask.mSuspendByIrat || registerTask.mSuspendBySnapshot) {
                return false;
            }
        } else if (i == 1) {
            if (z == registerTask.mSuspendByIrat) {
                return false;
            }
            registerTask.mSuspendByIrat = z;
            if (registerTask.mSuspended || registerTask.mSuspendBySnapshot) {
                return false;
            }
        } else if (i == 2) {
            if (z == registerTask.mSuspendBySnapshot) {
                return false;
            }
            registerTask.mSuspendBySnapshot = z;
            if (registerTask.mSuspended || registerTask.mSuspendByIrat) {
                return false;
            }
        }
        return true;
    }

    public void suspended(RegisterTask registerTask, boolean z, int i) {
        if (updateSuspended(registerTask, z, i)) {
            if (this.mRegStackIf.suspended(registerTask, z) && !z) {
                if (!this.mHandler.hasMessages(32)) {
                    this.mHandler.sendEmptyMessage(32);
                }
                this.mHandler.sendTryRegister(registerTask.getPhoneId());
            }
            if (z) {
                return;
            }
            if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                this.mHandler.sendTryRegister(registerTask.getPhoneId());
            } else if (registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                this.mImsFramework.getServiceModuleManager().updateCapabilities(registerTask.getPhoneId());
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v11, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: com.sec.ims.settings.ImsProfile} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void buildTask(int r17) {
        /*
            r16 = this;
            r9 = r16
            r10 = r17
            java.lang.String r0 = "buildTask:"
            java.lang.String r1 = "RegiMgr"
            com.sec.internal.log.IMSLog.i(r1, r10, r0)
            boolean r0 = com.sec.internal.ims.core.RegistrationUtils.hasLoadedProfile(r17)
            if (r0 != 0) goto L_0x0017
            java.lang.String r0 = "buildTask: no profile found."
            com.sec.internal.log.IMSLog.i(r1, r10, r0)
            return
        L_0x0017:
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r11 = com.sec.internal.ims.core.RegistrationUtils.getPendingRegistrationInternal(r17)
            if (r11 != 0) goto L_0x001e
            return
        L_0x001e:
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r0 = r9.mSimManagers
            java.lang.Object r0 = r0.get(r10)
            r12 = r0
            com.sec.internal.interfaces.ims.core.ISimManager r12 = (com.sec.internal.interfaces.ims.core.ISimManager) r12
            if (r12 != 0) goto L_0x002a
            return
        L_0x002a:
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r17)
            java.util.List r1 = r1.getProfiles()
            java.util.Iterator r1 = r1.iterator()
        L_0x003b:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x006a
            java.lang.Object r2 = r1.next()
            com.sec.ims.settings.ImsProfile r2 = (com.sec.ims.settings.ImsProfile) r2
            com.sec.internal.ims.core.SlotBasedConfig r3 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r17)
            java.util.Map r3 = r3.getExtendedProfiles()
            int r4 = r2.getId()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.Object r3 = r3.get(r4)
            com.sec.ims.settings.ImsProfile r3 = (com.sec.ims.settings.ImsProfile) r3
            if (r3 == 0) goto L_0x0066
            java.util.List r3 = r3.getExtImpuList()
            r2.setExtImpuList(r3)
        L_0x0066:
            r0.add(r2)
            goto L_0x003b
        L_0x006a:
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r17)
            java.util.Map r1 = r1.getExtendedProfiles()
            java.util.Set r1 = r1.entrySet()
            java.util.Iterator r1 = r1.iterator()
        L_0x007a:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x009c
            java.lang.Object r2 = r1.next()
            java.util.Map$Entry r2 = (java.util.Map.Entry) r2
            java.lang.Object r3 = r2.getValue()
            com.sec.ims.settings.ImsProfile r3 = (com.sec.ims.settings.ImsProfile) r3
            boolean r3 = r9.isAdhocProfile(r3)
            if (r3 == 0) goto L_0x007a
            java.lang.Object r2 = r2.getValue()
            com.sec.ims.settings.ImsProfile r2 = (com.sec.ims.settings.ImsProfile) r2
            r0.add(r2)
            goto L_0x007a
        L_0x009c:
            java.util.Iterator r1 = r11.iterator()
        L_0x00a0:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x00b4
            java.lang.Object r2 = r1.next()
            com.sec.internal.ims.core.RegisterTask r2 = (com.sec.internal.ims.core.RegisterTask) r2
            com.sec.ims.settings.ImsProfile r2 = r2.getProfile()
            r0.remove(r2)
            goto L_0x00a0
        L_0x00b4:
            com.sec.internal.interfaces.ims.IImsFramework r1 = r9.mImsFramework
            java.lang.String r2 = "default_rcs_volte_registration"
            r3 = -1
            int r1 = r1.getInt(r10, r2, r3)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegistrationType r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegistrationType.valueOf((int) r1)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegistrationType r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG
            if (r1 == r2) goto L_0x00da
            com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager r2 = r9.mRcsPolicyManager
            com.sec.internal.helper.RegiConfig r2 = r2.getRegiConfig(r10)
            int r2 = r2.getRcsVolteSingleReg()
            if (r2 == r3) goto L_0x00d2
            goto L_0x00d6
        L_0x00d2:
            int r2 = r1.getValue()
        L_0x00d6:
            com.sec.internal.constants.ims.core.RegistrationConstants$RegistrationType r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegistrationType.valueOf((int) r2)
        L_0x00da:
            r13 = r1
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r17)
            r1.setRcsVolteSingleRegistration(r13)
            java.util.Iterator r14 = r0.iterator()
            r0 = 0
            r15 = r0
        L_0x00e8:
            boolean r0 = r14.hasNext()
            r1 = 0
            if (r0 == 0) goto L_0x01b2
            java.lang.Object r0 = r14.next()
            r2 = r0
            com.sec.ims.settings.ImsProfile r2 = (com.sec.ims.settings.ImsProfile) r2
            boolean r0 = r2.hasEmergencySupport()
            if (r0 == 0) goto L_0x00fe
            goto L_0x01ae
        L_0x00fe:
            int r0 = r2.getEnableStatus()
            r3 = 2
            java.lang.String r4 = "buildTask: ["
            if (r0 == r3) goto L_0x0126
            com.sec.internal.helper.SimpleEventLog r0 = r9.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r4)
            java.lang.String r2 = r2.getName()
            r1.append(r2)
            java.lang.String r2 = "] - Disabled profile"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r10, r1)
            goto L_0x01ae
        L_0x0126:
            com.sec.internal.constants.ims.core.RegistrationConstants$RegistrationType r0 = com.sec.internal.constants.ims.core.RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG
            if (r13 == r0) goto L_0x015d
            boolean r0 = r9.isSingleReg(r13, r10)
            if (r0 == 0) goto L_0x015d
            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isRcsOnly(r2)
            if (r0 == 0) goto L_0x015d
            com.sec.internal.interfaces.ims.config.IConfigModule r0 = r9.mConfigModule
            boolean r0 = r0.isValidConfigDb(r10)
            if (r0 == 0) goto L_0x015d
            com.sec.internal.helper.SimpleEventLog r0 = r9.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r4)
            java.lang.String r3 = r2.getName()
            r1.append(r3)
            java.lang.String r3 = "] - RcsVolteSingleRegistration"
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r10, r1)
            r15 = r2
            goto L_0x00e8
        L_0x015d:
            java.lang.String r0 = r2.getMnoName()
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.fromName(r0)
            com.sec.internal.interfaces.ims.IImsFramework r3 = r9.mImsFramework
            java.lang.String r5 = "enable_gba"
            int r1 = r3.getInt(r10, r5, r1)
            boolean r3 = r12.isGBASupported()
            boolean r0 = com.sec.internal.ims.core.RegistrationUtils.isSatisfiedCarrierRequirement(r10, r2, r0, r1, r3)
            if (r0 != 0) goto L_0x0195
            com.sec.internal.helper.SimpleEventLog r0 = r9.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r4)
            java.lang.String r2 = r2.getName()
            r1.append(r2)
            java.lang.String r2 = "] - Unsatisfying carrier requirement"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r10, r1)
            goto L_0x01ae
        L_0x0195:
            com.sec.internal.ims.core.RegisterTask r8 = new com.sec.internal.ims.core.RegisterTask
            com.sec.internal.helper.os.ITelephonyManager r3 = r9.mTelephonyManager
            com.sec.internal.ims.core.PdnController r4 = r9.mPdnController
            android.content.Context r5 = r9.mContext
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r6 = r9.mVsm
            com.sec.internal.interfaces.ims.config.IConfigModule r7 = r9.mConfigModule
            r0 = r8
            r1 = r2
            r2 = r16
            r9 = r8
            r8 = r17
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8)
            r11.add(r9)
        L_0x01ae:
            r9 = r16
            goto L_0x00e8
        L_0x01b2:
            if (r15 == 0) goto L_0x01c1
            java.lang.Object r0 = r11.get(r1)
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r0 = r0.getGovernor()
            r0.enableRcsOverIms(r15)
        L_0x01c1:
            com.sec.internal.ims.core.RegistrationManagerInternal$$ExternalSyntheticLambda1 r0 = new com.sec.internal.ims.core.RegistrationManagerInternal$$ExternalSyntheticLambda1
            r0.<init>()
            java.util.Collections.sort(r11, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerInternal.buildTask(int):void");
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ int lambda$buildTask$0(RegisterTask registerTask, RegisterTask registerTask2) {
        return registerTask2.mProfile.getPriority() - registerTask.mProfile.getPriority();
    }

    private boolean isSingleReg(RegistrationConstants.RegistrationType registrationType, int i) {
        return registrationType == RegistrationConstants.RegistrationType.SINGLE_REG || (registrationType == RegistrationConstants.RegistrationType.DUAL_WHEN_ROAMING_REG && !this.mTelephonyManager.isNetworkRoaming(SimUtil.getSubId(i)));
    }

    /* access modifiers changed from: protected */
    public void clearTask(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "clearTask:");
        if (SimUtil.isSoftphoneEnabled()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "skip clearTask for softphone");
            return;
        }
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            ArrayList arrayList = new ArrayList();
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (!RegistrationUtils.isCmcProfile(registerTask.getProfile()) || this.mHandler.hasMessages(42)) {
                    this.mHandler.removeMessages(22, registerTask);
                    registerTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_SIM_REFRESH);
                    registerTask.getGovernor().clear();
                    stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                    IMSLog.i(IRegistrationManager.LOG_TAG, i, "Remove task: " + registerTask);
                    arrayList.add(registerTask);
                    this.mRegStackIf.removeUserAgent(registerTask);
                    if (registerTask.mMno == Mno.TMOBILE && registerTask.getProfile().getPdnType() == 11) {
                        registerTask.stopDailyReRegisterTimer();
                    }
                }
            }
            pendingRegistrationInternal.removeAll(arrayList);
        }
    }

    /* access modifiers changed from: protected */
    public void tryRegister(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "tryRegister:");
        ISimManager simManager = getSimManager(i);
        if (simManager != null) {
            boolean pendingHasEmergencyTask = RegistrationUtils.pendingHasEmergencyTask(i, simManager.getSimMno());
            this.mHandler.removeMessages(2, Integer.valueOf(i));
            if (this.mUserEvtCtr.isShuttingDown()) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "Device is getting shutdown");
            } else if (this.mHandler.hasMessages(36)) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "Sim refresh is ongoing. retry after 2s");
                this.mHandler.sendTryRegister(i, UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            } else {
                logTask();
                SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
                if (pendingRegistrationInternal != null) {
                    Iterator it = pendingRegistrationInternal.iterator();
                    while (it.hasNext()) {
                        RegisterTask registerTask = (RegisterTask) it.next();
                        boolean pendingRcsRegister = this.mRcsPolicyManager.pendingRcsRegister(registerTask, getPendingRegistration(i), i);
                        boolean hasNoSim = simManager.hasNoSim();
                        boolean hasMessages = this.mHandler.hasMessages(107);
                        ITelephonyManager iTelephonyManager = this.mTelephonyManager;
                        PdnController pdnController = this.mPdnController;
                        IVolteServiceModule iVolteServiceModule = this.mVsm;
                        if (!RegistrationUtils.needToSkipTryRegister(registerTask, pendingRcsRegister, hasNoSim, hasMessages, iTelephonyManager, pdnController, iVolteServiceModule != null && iVolteServiceModule.hasEmergencyCall(SimUtil.getOppositeSimSlot(i)))) {
                            RegistrationConstants.RegistrationType rcsVolteSingleRegistration = SlotBasedConfig.getInstance(i).getRcsVolteSingleRegistration();
                            if (rcsVolteSingleRegistration != RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG && registerTask.isRcsOnly() && registerTask.getState() == RegistrationConstants.RegisterTaskState.CONFIGURED) {
                                int rcsVolteSingleReg = this.mRcsPolicyManager.getRegiConfig(i).getRcsVolteSingleReg();
                                if (rcsVolteSingleReg == -1) {
                                    rcsVolteSingleReg = rcsVolteSingleRegistration.getValue();
                                }
                                RegistrationConstants.RegistrationType valueOf = RegistrationConstants.RegistrationType.valueOf(rcsVolteSingleReg);
                                SlotBasedConfig.getInstance(i).setRcsVolteSingleRegistration(valueOf);
                                if (isSingleReg(valueOf, i)) {
                                    ImsProfile profile = registerTask.getProfile();
                                    pendingRegistrationInternal.remove(registerTask);
                                    ((RegisterTask) pendingRegistrationInternal.get(0)).getGovernor().enableRcsOverIms(profile);
                                }
                            }
                            if (!registerTask.getProfile().hasEmergencySupport() && registerTask.getGovernor().hasEmergencyTaskInPriority(pendingRegistrationInternal)) {
                                this.mHandler.sendTryRegister(i, 500);
                            } else if (tryRegister(registerTask) && pendingHasEmergencyTask && !getNetworkEvent(registerTask.getPhoneId()).outOfService) {
                                IMSLog.i(IRegistrationManager.LOG_TAG, i, "tryRegister: pending EM regi for the sequential regi of Lab TC.");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean tryRegister(RegisterTask registerTask) {
        if (checkForTryRegister(registerTask)) {
            return true;
        }
        int regiFailReason = registerTask.getRegiFailReason();
        if (regiFailReason > DiagnosisConstants.REGI_FRSN.UNKNOWN.getCode() && regiFailReason != registerTask.getLastRegiFailReason()) {
            reportRegistrationStatus(registerTask);
            IMSLog.c(LogClass.REGI_TRY_REGISTER, registerTask.getPhoneId() + "," + registerTask.getMno().getName() + "," + registerTask.getProfile().getPdn() + ",REG FAIL:" + DiagnosisConstants.REGI_FRSN.valueOf(regiFailReason));
        }
        DiagnosisConstants.REGI_FRSN valueOf = DiagnosisConstants.REGI_FRSN.valueOf(regiFailReason);
        if (!valueOf.isOneOf(DiagnosisConstants.REGI_FRSN.VOPS_OFF, DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF, DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF, DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED, DiagnosisConstants.REGI_FRSN.DATA_RAT_IS_NOT_PS_VOICE, DiagnosisConstants.REGI_FRSN.ONGOING_OTA)) {
            return false;
        }
        IMSLog.lazer((IRegisterTask) registerTask, "NOT_TRIGGERED : reason - " + valueOf);
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkForTryRegister(RegisterTask registerTask) {
        RegisterTask registerTask2 = registerTask;
        ImsProfile profile = registerTask.getProfile();
        int phoneId = registerTask.getPhoneId();
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask2, "checkForTryRegister id: " + profile.getId());
        if (this.mUserEvtCtr.isShuttingDown()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "Device is getting shutdown");
            return false;
        }
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (pendingRegistrationInternal == null || !pendingRegistrationInternal.contains(registerTask2)) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, registerTask2, "checkForTryRegister UNKNOWN task. (it should be removed task)");
            return false;
        }
        IRegistrationGovernor governor = registerTask.getGovernor();
        boolean isPdnConnected = isPdnConnected(profile, phoneId);
        PdnController pdnController = this.mPdnController;
        int findBestNetwork = RegistrationUtils.findBestNetwork(phoneId, profile, governor, isPdnConnected, pdnController, this.mVsm, pdnController.getNetworkState(phoneId).getMobileDataNetworkType(), this.mContext);
        registerTask2.setRegistrationRat(findBestNetwork);
        if (!registerTask.getGovernor().isReadyToDualRegister(isTryingCmcDualRegi(phoneId, registerTask2))) {
            this.mHandler.sendTryRegister(phoneId, 2500);
            return false;
        }
        ISimManager iSimManager = this.mSimManagers.get(phoneId);
        if (iSimManager == null) {
            registerTask2.setRegiFailReason(DiagnosisConstants.REGI_FRSN.SIMMANAGER_NULL.getCode());
            return false;
        }
        boolean z = true;
        boolean z2 = ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON;
        boolean isNetworkRoaming = this.mTelephonyManager.isNetworkRoaming();
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask2, "checkInitialRegistrationIsReady: APM ON [" + z2 + "], Roamimg [" + isNetworkRoaming + "]");
        if (!RegistrationUtils.checkInitialRegistrationIsReady(registerTask, getPendingRegistration(phoneId), z2, isNetworkRoaming, iSimManager.hasNoSim(), this.mRcsPolicyManager, this.mHandler)) {
            return false;
        }
        if (!registerTask.getGovernor().isReadyToRegister(findBestNetwork)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask2, "checkForTryRegister: isReadyToRegister = false");
            if (!registerTask.isKeepPdn() && registerTask2.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED) && registerTask.getPdnType() == 11) {
                Log.i(IRegistrationManager.LOG_TAG, "stopPdnConnectivity. IMS PDN should not be established in this case.");
                stopPdnConnectivity(registerTask.getPdnType(), registerTask2);
                registerTask2.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            if (registerTask.getRegiFailReason() == DiagnosisConstants.REGI_FRSN.UNKNOWN.getCode()) {
                registerTask2.setRegiFailReason(DiagnosisConstants.REGI_FRSN.GVN_NOT_READY.getCode());
            }
            return false;
        }
        RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.IDLE;
        if (registerTask2.isOneOf(registerTaskState, RegistrationConstants.RegisterTaskState.RESOLVED, RegistrationConstants.RegisterTaskState.CONFIGURED, RegistrationConstants.RegisterTaskState.CONNECTED)) {
            Context context = this.mContext;
            boolean isRcsAvailable = ConfigUtil.isRcsAvailable(context, phoneId, iSimManager);
            boolean isCdmConfigured = RegistrationUtils.isCdmConfigured(this.mImsFramework, phoneId);
            boolean z3 = getOmadmState(phoneId) != RegistrationManager.OmadmConfigState.FINISHED;
            IVolteServiceModule iVolteServiceModule = this.mVsm;
            if (!RegistrationUtils.checkConfigForInitialRegistration(context, registerTask, isRcsAvailable, isCdmConfigured, z3, iVolteServiceModule != null && iVolteServiceModule.hasEmergencyCall(phoneId), this.mRcsPolicyManager, this.mHandler, this.mNetEvtCtr)) {
                return false;
            }
            int selectPdnType = RegistrationUtils.selectPdnType(profile, findBestNetwork);
            registerTask2.setPdnType(selectPdnType);
            if (!ConfigUtil.isRcsEur(phoneId) || !registerTask.isRcsOnly()) {
                z = false;
            }
            Set<String> serviceForNetwork = getServiceForNetwork(profile, findBestNetwork, z, phoneId);
            if (!checkServicesForInitialRegistration(registerTask2, serviceForNetwork)) {
                return false;
            }
            return tryInitialRegistration(registerTask2, findBestNetwork, selectPdnType, serviceForNetwork);
        } else if (registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask2, "tryRegister: already registering.");
            registerTask2.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ALREADY_REGISTERING.getCode());
            return false;
        } else if (registerTask.getState() != RegistrationConstants.RegisterTaskState.CONNECTING || registerTask.getMno() != Mno.TELEFONICA_UK || !registerTask.isKeepPdn() || registerTask.getPdnType() != 11 || registerTask.getRegistrationRat() != 0) {
            return true;
        } else {
            Log.i(IRegistrationManager.LOG_TAG, "stopPdnConnectivity, Network Changing to 3G/2G during Task:CONNECTING");
            stopPdnConnectivity(registerTask.getPdnType(), registerTask2);
            registerTask2.setState(registerTaskState);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean tryInitialRegistration(RegisterTask registerTask, int i, int i2, Set<String> set) {
        RegisterTask registerTask2 = registerTask;
        int i3 = i;
        int i4 = i2;
        int phoneId = registerTask.getPhoneId();
        ImsProfile profile = registerTask.getProfile();
        if (this.mImsFramework.getCmcConnectivityController().isEnabledWifiDirectFeature() && tryInitialP2pRegistration(registerTask, i, i2, set)) {
            return true;
        }
        if (!this.mPdnController.isConnected(i4, registerTask) || (registerTask.getNetworkConnected() == null && !profile.hasEmergencySupport())) {
            return tryStartPdnConnectivity(registerTask, profile, i, i4);
        }
        if (registerTask.isSuspended()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "tryRegister: network is suspended " + i4 + ". try Register once network is resumed.");
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NETWORK_SUSPENDED.getCode());
            return false;
        }
        registerTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
        registerTask.setKeepPdn(true);
        if (registerTask.getGovernor().isMobilePreferredForRcs() && i4 == 0) {
            int phoneId2 = registerTask.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, "tryRegister: startTimsTimer connected pdn = " + i4);
            PdnController pdnController = this.mPdnController;
            if (pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer()) == 1) {
                registerTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
            }
            registerTask.getGovernor().startTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
        }
        this.mPdnController.startPdnConnectivity(i4, registerTask, RegistrationUtils.getPhoneIdForStartConnectivity(registerTask));
        if (registerTask.getGovernor().isReadyToGetReattach()) {
            Log.i(IRegistrationManager.LOG_TAG, "keep pdn and block trying registration. return");
            return false;
        } else if (registerTask.isRcsOnly() && ConfigUtil.isRcsEurNonRjil(registerTask.getMno()) && this.mTelephonyManager.getCallState(SimUtil.getOppositeSimSlot(phoneId)) != 0 && this.mPdnController.getDataState(phoneId) == 3) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "tryRegister: block trying registration because other slot is on calling. return");
            return false;
        } else if (!registerTask.isRcsOnly() || !ConfigUtil.isRcsChn(registerTask.getMno()) || this.mTelephonyManager.getCallState(phoneId) == 0 || phoneId == SimUtil.getSlotId(SubscriptionManager.getDefaultDataSubscriptionId())) {
            String pcscfIpAddress = this.mNetEvtCtr.getPcscfIpAddress(registerTask, this.mPdnController.getInterfaceName(registerTask));
            if (TextUtils.isEmpty(pcscfIpAddress)) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "tryRegister: pcscf is null. return..");
                registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.EMPTY_PCSCF.getCode());
                if (registerTask.getMno() != Mno.KT || profile.getPcscfPreference() == 0) {
                    if (profile.hasEmergencySupport()) {
                        if (registerTask.getMno() == Mno.KDDI) {
                            RegistrationManagerHandler registrationManagerHandler = this.mHandler;
                            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(2, Integer.valueOf(phoneId)), 1000);
                        } else {
                            RegistrationUtils.sendEmergencyRegistrationFailed(registerTask);
                        }
                    }
                    this.mEventLog.logAndAdd(phoneId, registerTask, "regi failed due to empty p-cscf");
                    if (registerTask.getPdnType() == 11) {
                        if (registerTask.getMno() == Mno.TMOUS) {
                            stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                            registerTask.setDeregiReason(42);
                            onRegisterError(registerTask, -1, SipErrorBase.EMPTY_PCSCF, 0);
                        }
                        if (registerTask.getMno().isOneOf(Mno.CTC, Mno.CTCMO)) {
                            Log.i(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is null. Notify registration state to CP.");
                            notifyImsNotAvailable(registerTask, false);
                            if (getImsIconManager(phoneId) != null) {
                                Log.i(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is null. fresh icon once.");
                                getImsIconManager(phoneId).updateRegistrationIcon();
                            }
                        }
                        if (registerTask.getMno().isOneOf(Mno.CMCC, Mno.CU) && i3 == 20) {
                            Log.i(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is null. Notify registration state to CP in NR rat.");
                            notifyImsNotAvailable(registerTask, true);
                        }
                        if (registerTask.getMno().isEmeasewaoce()) {
                            Log.i(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is null. Notify registration state to CP");
                            notifyImsNotAvailable(registerTask, true);
                        }
                    }
                    return false;
                }
                Log.i(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is null. return here for dns query retry");
                return false;
            }
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(phoneId, registerTask, "tryInitialRegistration on pdn: " + i4 + ". Register now.");
            StringBuilder sb = new StringBuilder();
            sb.append("InitialRegi : rat = ");
            sb.append(i);
            registerTask.setReason(sb.toString());
            if (i3 == 18 && this.mPdnController.getEpdgPhysicalInterface(registerTask.getPhoneId()) == 2) {
                registerTask.setReason("InitialRegi (Cross SIM Calling) : rat = " + i);
            }
            return registerInternal(registerTask, pcscfIpAddress, set);
        } else {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "tryRegister: block trying registration while on calling because default mobile data use the other slot. return");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean tryInitialP2pRegistration(RegisterTask registerTask, int i, int i2, Set<String> set) {
        int phoneId = registerTask.getPhoneId();
        ImsProfile profile = registerTask.getProfile();
        int cmcType = profile.getCmcType();
        if (cmcType == 5 || cmcType == 7 || cmcType == 8) {
            IMSLog.d(IRegistrationManager.LOG_TAG, phoneId, registerTask, "tryInitialRegistration, skip pdn connect");
            registerTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            registerTask.setKeepPdn(true);
            if (cmcType == 7) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "tryRegister, setPcscfHostname for [WIFI-DIRECT] server: " + registerTask.getProfile().getDomain());
                registerTask.setPcscfHostname(registerTask.getProfile().getDomain());
            }
            new ArrayList();
            List pcscfList = profile.getPcscfList();
            if (pcscfList == null || pcscfList.isEmpty()) {
                Log.d(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is invalid");
                return false;
            }
            registerTask.mGovernor.updatePcscfIpList(pcscfList);
            Iterator it = pcscfList.iterator();
            if (it.hasNext()) {
                String str = (String) it.next();
                Log.d(IRegistrationManager.LOG_TAG, "tryRegister: wifi-direct or mobile-hotspot registration: " + str);
                registerTask.setReason("InitialRegi : rat = " + i);
                registerInternal(registerTask, str, set);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean tryStartPdnConnectivity(RegisterTask registerTask, ImsProfile imsProfile, int i, int i2) {
        int phoneId = registerTask.getPhoneId();
        if (!RegistrationUtils.hasRcsService(phoneId, imsProfile) || i == 18 || RegistrationUtils.hasVolteService(phoneId, imsProfile) || RcsUtils.UiUtils.getRcsUserConsent(this.mContext, this.mTelephonyManager, phoneId)) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(phoneId, registerTask, "tryRegister: connecting to network " + i2);
            registerTask.setState(RegistrationConstants.RegisterTaskState.CONNECTING);
            if (registerTask.getGovernor().isMobilePreferredForRcs() && i2 == 0) {
                int phoneId2 = registerTask.getPhoneId();
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, "tryRegister: startTimsTimer rcs pdn = " + i2);
                PdnController pdnController = this.mPdnController;
                if (pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer()) == 1) {
                    registerTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
                    stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                }
                registerTask.getGovernor().startTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
            }
            this.mPdnController.startPdnConnectivity(i2, registerTask, RegistrationUtils.getPhoneIdForStartConnectivity(registerTask));
            Mno mno = registerTask.getMno();
            if (mno.isOneOf(Mno.VZW, Mno.KDDI, Mno.CTCMO, Mno.CTC, Mno.ATT) || mno.isEmeasewaoce() || (registerTask.mMno.isKor() && !registerTask.isRcsOnly() && !RegistrationUtils.isCmcProfile(imsProfile))) {
                registerTask.getGovernor().startTimsTimer(RegistrationConstants.REASON_IMS_PDN_REQUEST);
            }
            if (imsProfile.getPdnType() == 11) {
                IMSLogTimer.setPdnStartTime(phoneId, true);
                IMSLog.lazer((IRegisterTask) registerTask, "PDN REQUEST : type - " + i2 + " <+" + (((double) (IMSLogTimer.getPdnStartTime(phoneId) - IMSLogTimer.getLatchEndTime(phoneId))) / 1000.0d) + "s>");
            } else {
                IMSLog.lazer((IRegisterTask) registerTask, "PDN REQUEST : type - " + i2);
            }
            return true;
        }
        registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.RCS_ONLY_NEEDED.getCode());
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean checkServicesForInitialRegistration(RegisterTask registerTask, Set<String> set) {
        int phoneId = registerTask.getPhoneId();
        boolean z = true;
        if (CollectionUtils.isNullOrEmpty((Collection<?>) set)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask.getProfile().getName() + ": no ims service for current rat" + registerTask.getRegistrationRat());
            NetworkEvent networkEvent = getNetworkEvent(phoneId);
            if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) != ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                z = false;
            }
            if (!z) {
                if (networkEvent != null && !networkEvent.outOfService) {
                    notifyImsNotAvailable(registerTask, false);
                }
                if (registerTask.getMno().isOneOf(Mno.CTC, Mno.CTCMO) && getImsIconManager(phoneId) != null && registerTask.getPdnType() == 11) {
                    Log.i(IRegistrationManager.LOG_TAG, "no ims service. fresh icon once.");
                    getImsIconManager(phoneId).updateRegistrationIcon();
                }
            }
            if (networkEvent != null && !networkEvent.outOfService) {
                if (registerTask.getMno().isOneOf(Mno.OPTUS) && networkEvent.network == 20 && z) {
                    return false;
                }
                stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            IMSLog.c(LogClass.REGI_FILTERED_ALL_SERVICES, phoneId + ",FILTERED ALL:" + registerTask.getPdnType());
            return false;
        }
        int registrationRat = registerTask.getRegistrationRat();
        if (registrationRat == 0) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "tryRegister: crap. No service?");
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NETWORK_UNKNOWN.getCode());
            return false;
        } else if (!registerTask.getGovernor().isLocationInfoLoaded(registrationRat)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "location is not loaded");
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.LOCATION_NOT_LOADED.getCode());
            return false;
        } else {
            List<RegisterTask> priorityRegiedTask = RegistrationUtils.getPriorityRegiedTask(false, registerTask);
            if (priorityRegiedTask.isEmpty()) {
                return true;
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "deregi found lowerPriority task " + priorityRegiedTask);
            for (RegisterTask next : priorityRegiedTask) {
                next.setDeregiReason(46);
                deregister(next, false, false, "deregi found lowerPriority task");
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void onManualRegister(ImsProfile imsProfile, int i) {
        ImsProfile imsProfile2 = imsProfile;
        int i2 = i;
        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "onManualRegister: profile " + imsProfile.getName());
        ISimManager iSimManager = this.mSimManagers.get(i2);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (iSimManager != null && pendingRegistrationInternal != null) {
            this.mImsFramework.getServiceModuleManager().serviceStartDeterminer(Collections.singletonList(imsProfile), i2);
            Iterator it = pendingRegistrationInternal.iterator();
            boolean z = false;
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                ImsProfile profile = registerTask.getProfile();
                if (profile.getCmcType() == 1 || profile.getCmcType() == 2) {
                    if (imsProfile.getCmcType() == profile.getCmcType()) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "onManualRegister: cmc register task already manual registered");
                        return;
                    }
                } else if (profile.getCmcType() > 2 && profile.getName().equals(imsProfile.getName())) {
                    IMSLog.d(IRegistrationManager.LOG_TAG, "Task with profile name already exists, update imsprofile");
                    registerTask.setProfile(imsProfile2);
                    if (profile.getCmcType() == 4 || profile.getCmcType() == 8) {
                        IMSLog.d(IRegistrationManager.LOG_TAG, "onManualRegister: releaseThrottle, resetRetry");
                        registerTask.mGovernor.releaseThrottle(8);
                        registerTask.mGovernor.resetRetry();
                        registerTask.mGovernor.updatePcscfIpList(imsProfile.getPcscfList());
                    }
                    z = true;
                }
            }
            SlotBasedConfig.getInstance(i).addExtendedProfile(imsProfile.getId(), imsProfile2);
            RegisterTask registerTask2 = r0;
            RegisterTask registerTask3 = new RegisterTask(imsProfile, this, this.mTelephonyManager, this.mPdnController, this.mContext, this.mVsm, this.mConfigModule, i);
            SecImsNotifier.getInstance().updateAdhocProfile(i2, imsProfile2, true);
            if (iSimManager.isSimLoaded() || imsProfile.isSoftphoneEnabled() || imsProfile.isSamsungMdmnEnabled()) {
                this.mImsFramework.notifyImsReady(true, i2);
                if (!z) {
                    pendingRegistrationInternal.add(registerTask2);
                }
                tryRegister(i2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void tryEmergencyRegister(RegisterTask registerTask) {
        boolean z;
        IMSLog.i(IRegistrationManager.LOG_TAG, registerTask.getPhoneId(), "tryEmergencyRegister:");
        this.mHandler.removeMessages(118);
        this.mHandler.removeMessages(155);
        ImsProfile profile = registerTask.getProfile();
        if (profile.getE911RegiTime() > 0 && !this.mPdnController.isConnected(registerTask.getPdnType(), registerTask)) {
            RegistrationManagerHandler registrationManagerHandler = this.mHandler;
            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(155, registerTask), ((long) profile.getE911RegiTime()) * 1000);
        }
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(registerTask.getPhoneId());
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (registerTask.getProfile().equals(((RegisterTask) it.next()).getProfile())) {
                        z = true;
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
            if (!z) {
                pendingRegistrationInternal.add(registerTask);
            }
            tryRegister(registerTask.getPhoneId());
        }
    }

    /* access modifiers changed from: protected */
    public void onManualDeregister(int i, boolean z, int i2) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "onManualDeregister: profile id:" + i + ", explicitDeregi:" + z);
        RegisterTask registerTaskByProfileId = getRegisterTaskByProfileId(i, i2);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i2);
        if (registerTaskByProfileId == null || pendingRegistrationInternal == null) {
            Log.i(IRegistrationManager.LOG_TAG, "onManualDeregister: profile not found.");
            startSilentEmergency();
            return;
        }
        ImsProfile profile = registerTaskByProfileId.getProfile();
        if (RegistrationUtils.needToNotifyImsReady(profile, i2)) {
            this.mEventLog.logAndAdd(i2, "onManualDeregister: notify IMS ready [false]");
            this.mImsFramework.notifyImsReady(false, i2);
        }
        Log.i(IRegistrationManager.LOG_TAG, "onManualDeregister: deregistering profile " + profile.getName());
        registerTaskByProfileId.getGovernor().stopTimsTimer(RegistrationConstants.REASON_MANUAL_DEREGI);
        Optional.ofNullable(registerTaskByProfileId.getGovernor().onManualDeregister(z)).ifPresent(new RegistrationManagerInternal$$ExternalSyntheticLambda7(pendingRegistrationInternal));
        SlotBasedConfig.getInstance(registerTaskByProfileId.getPhoneId()).removeExtendedProfile(profile.getId());
        startSilentEmergency();
    }

    /* access modifiers changed from: protected */
    public void startSilentEmergency() {
        Message message = this.mHasSilentE911;
        if (message != null) {
            startEmergencyRegistration(this.mPhoneIdForSilentE911, message);
            this.mHasSilentE911 = null;
            this.mPhoneIdForSilentE911 = -1;
        }
    }

    /* access modifiers changed from: protected */
    public void triggerFullNetworkRegistration(int i, int i2) {
        Optional.ofNullable((RegisterTask) SlotBasedConfig.getInstance(i).getRegistrationTasks().stream().filter(new RegistrationManagerInternal$$ExternalSyntheticLambda3()).filter(new RegistrationManagerInternal$$ExternalSyntheticLambda4()).findFirst().orElseGet(new RegistrationManagerInternal$$ExternalSyntheticLambda5(this, i))).ifPresent(new RegistrationManagerInternal$$ExternalSyntheticLambda6(i, i2));
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$triggerFullNetworkRegistration$1(RegisterTask registerTask) {
        return registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ RegisterTask lambda$triggerFullNetworkRegistration$4(int i) {
        IMSLog.e(IRegistrationManager.LOG_TAG, i, "triggerFullNetworkRegistration: Not registered for chat.");
        tryRegister(i);
        return null;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$triggerFullNetworkRegistration$5(int i, int i2, RegisterTask registerTask) {
        IMSLog.e(IRegistrationManager.LOG_TAG, i, registerTask, "triggerFullNetworkRegistration: error code: " + i2);
        registerTask.getGovernor().onSipError("im", new SipError(i2));
    }

    /* access modifiers changed from: protected */
    public void onUpdateSipDelegateRegistration(int i) {
        boolean updateRegistration = updateRegistration(i, RegistrationConstants.UpdateRegiReason.SIPDELEGATE_UPDATE);
        SimpleEventLog simpleEventLog = this.mEventLog;
        StringBuilder sb = new StringBuilder();
        sb.append("onUpdateSipDelegateRegistration: ");
        sb.append(updateRegistration ? "Success" : "Fail");
        simpleEventLog.logAndAdd(i, sb.toString());
        if (updateRegistration) {
            this.mHandler.removeMessages(59, Integer.valueOf(i));
        }
    }

    /* access modifiers changed from: protected */
    public boolean updateRegistration(int i, RegistrationConstants.UpdateRegiReason updateRegiReason) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        boolean z = false;
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getPhoneId() == i) {
                    if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        z |= updateRegistration(registerTask, updateRegiReason);
                    } else {
                        tryRegister(registerTask);
                    }
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean updateRegistration(RegisterTask registerTask, RegistrationConstants.UpdateRegiReason updateRegiReason) {
        boolean updateRegistration = updateRegistration(registerTask, updateRegiReason, false);
        this.mRegStackIf.updatePani(registerTask);
        return updateRegistration;
    }

    /* access modifiers changed from: protected */
    public boolean updateRegistration(RegisterTask registerTask, RegistrationConstants.UpdateRegiReason updateRegiReason, boolean z) {
        boolean isForceUpdateReason = updateRegiReason.isForceUpdateReason();
        int phoneId = registerTask.getPhoneId();
        ImsProfile profile = registerTask.getProfile();
        IRegistrationGovernor governor = registerTask.getGovernor();
        boolean isPdnConnected = isPdnConnected(profile, phoneId);
        PdnController pdnController = this.mPdnController;
        int findBestNetwork = RegistrationUtils.findBestNetwork(phoneId, profile, governor, isPdnConnected, pdnController, this.mVsm, pdnController.getNetworkState(phoneId).getMobileDataNetworkType(), this.mContext);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, registerTask, "updateRegistration: reason=" + updateRegiReason.toString() + "|" + registerTask.getReason() + ", rat=" + findBestNetwork + ", isForceReRegi=" + isForceUpdateReason + ", immediately=" + z);
        NetworkEvent networkEvent = getNetworkEvent(phoneId);
        if (networkEvent == null) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "updateRegistration: profile=" + profile.getName() + ", NetworkEvent is null");
            return false;
        }
        String updateRegiPendingReason = registerTask.getGovernor().getUpdateRegiPendingReason(findBestNetwork, networkEvent, isForceUpdateReason, z);
        if (!TextUtils.isEmpty(updateRegiPendingReason)) {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd(phoneId, registerTask, "updateRegistration: Pending by [" + updateRegiPendingReason + "]");
            if (isForceUpdateReason) {
                registerTask.mHasForcedPendingUpdate = true;
            } else {
                registerTask.mHasPendingUpdate = true;
            }
            registerTask.setImmediatePendingUpdate(z);
            if (registerTask.isEpdgHandoverInProgress()) {
                registerTask.setHasPendingEpdgHandover(true);
            }
            return false;
        } else if (registerTask.getGovernor().determineDeRegistration(findBestNetwork, networkEvent.network) || registerTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
            this.mEventLog.logAndAdd(phoneId, registerTask, "Stop updateRegistration");
            return false;
        } else if (!registerTask.getGovernor().isLocationInfoLoaded(findBestNetwork)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "location info is not loaded");
            return false;
        } else {
            if (registerTask.getMno() == Mno.RJIL) {
                if (this.mRcsPolicyManager.doRcsConfig(registerTask, getPendingRegistration(phoneId))) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "RCS auto-configuration triggered..");
                    return false;
                }
            }
            return compareSvcAndDoUpdateRegistration(registerTask, isForceUpdateReason, z, findBestNetwork, networkEvent);
        }
    }

    private boolean compareSvcAndDoUpdateRegistration(RegisterTask registerTask, boolean z, boolean z2, int i, NetworkEvent networkEvent) {
        boolean z3;
        RegisterTask registerTask2 = registerTask;
        int i2 = i;
        NetworkEvent networkEvent2 = networkEvent;
        int registrationRat = registerTask.getRegistrationRat();
        registerTask2.setRegistrationRat(i2);
        int phoneId = registerTask.getPhoneId();
        ImsRegistration imsRegistration = SlotBasedConfig.getInstance(phoneId).getImsRegistrations().get(Integer.valueOf(IRegistrationManager.getRegistrationInfoId(registerTask.getProfile().getId(), phoneId)));
        if (imsRegistration == null) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "compareSvcAndDoUpdateRegistration: reg is null for " + registerTask.getProfile().getName());
            return false;
        }
        Set<String> services = imsRegistration.getServices();
        Set<String> serviceForNetwork = getServiceForNetwork(registerTask.getProfile(), i2, ConfigUtil.isRcsEur(phoneId) && registerTask.isRcsOnly(), phoneId);
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "compareSvcAndDoUpdateRegistration: registered service oldSvc" + services);
        if (CollectionUtils.isNullOrEmpty((Collection<?>) serviceForNetwork)) {
            registerTask2.setReason("empty service list : " + networkEvent2.network);
            registerTask2.setDeregiReason(72);
            if (!registerTask.isRcsOnly() || phoneId == SimUtil.getActiveDataPhoneId()) {
                tryDeregisterInternal(registerTask2, false, false);
            } else {
                tryDeregisterInternal(registerTask2, true, false);
            }
            return false;
        }
        if (registerTask.getMno() != Mno.TMOUS || i2 == registrationRat || registrationRat != 18 || this.mPdnController.isEpdgConnected(phoneId)) {
            z3 = z;
        } else {
            registerTask2.setReason("Force update registration due to RAT mismatch.");
            z3 = true;
        }
        if (!services.contains("datachannel") && serviceForNetwork.contains("datachannel")) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "compareSvcAndDoUpdateRegistration: Remove DataChannel service from newSvc");
            serviceForNetwork.remove("datachannel");
        }
        Set<String> set = serviceForNetwork;
        if (RegistrationUtils.determineUpdateRegistration(registerTask, registrationRat, i, services, serviceForNetwork, z3)) {
            HashSet hashSet = new HashSet();
            for (String str : services) {
                Set<String> set2 = set;
                if (!set2.contains(str)) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "compareSvcAndDoUpdateRegistration: Add to delete service" + str);
                    hashSet.add(str);
                }
                set = set2;
            }
            Set<String> set3 = set;
            if (!hashSet.isEmpty()) {
                this.mImsFramework.getServiceModuleManager().notifyRcsDeregistering(hashSet, imsRegistration);
            }
            if (registerTask.getImsRegistration() != null) {
                registerTask.getImsRegistration().setCurrentRat(i2);
            }
            if (!registerTask.getGovernor().isReadyToDualRegister(isTryingCmcDualRegi(phoneId, registerTask2))) {
                if (z3) {
                    registerTask2.mHasForcedPendingUpdate = true;
                } else {
                    registerTask2.mHasPendingUpdate = true;
                }
                if (registerTask.isEpdgHandoverInProgress()) {
                    registerTask2.setHasPendingEpdgHandover(true);
                }
                registerTask2.setImmediatePendingUpdate(z2);
                this.mHandler.sendEmptyMessageDelayed(32, 1500);
                return false;
            }
            registerTask2.setReason("rat = " + registerTask.getRegistrationRat() + "(" + networkEvent2.network + "), " + registerTask.getReason());
            registerInternal(registerTask2, (String) null, set3);
            return true;
        }
        if (RegistrationUtils.skipReRegi(registerTask2, registrationRat, i2)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "compareSvcAndDoUpdateRegistration: handle ImplicitReRegi. previousRat=" + registrationRat + ", rat=" + i2);
            ImsRegistration imsRegistration2 = registerTask.getImsRegistration();
            if (!(imsRegistration2 == null || imsRegistration2.getEpdgStatus() || i2 == 18 || registrationRat == 18)) {
                imsRegistration2.setCurrentRat(i2);
                if (i2 != registrationRat) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "compareSvcAndDoUpdateRegistration: handle ImplicitReRegi. notify Ims Phone");
                    SecImsNotifier.getInstance().notifyImsRegistration(imsRegistration2, true, new ImsRegistrationError());
                    this.mImsFramework.getServiceModuleManager().notifyImsRegistration(imsRegistration2, true, new ImsRegistrationError().getSipErrorCode());
                }
            }
            if (getImsIconManager(phoneId) != null) {
                boolean z4 = this.mImsFramework.getBoolean(phoneId, GlobalSettingsConstants.Registration.REMOVE_ICON_NOSVC, false);
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "compareSvcAndDoUpdateRegistration: updateRegistrationIcon: remove_icon_nosvc: " + z4);
                if (z4) {
                    getImsIconManager(phoneId).updateRegistrationIcon();
                }
            }
        }
        registerTask2.setReason("");
        return false;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x009d  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00af A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean registerInternal(com.sec.internal.ims.core.RegisterTask r26, java.lang.String r27, java.util.Set<java.lang.String> r28) {
        /*
            r25 = this;
            r0 = r25
            r15 = r26
            int r14 = r26.getPhoneId()
            com.sec.internal.interfaces.ims.core.IUserAgent r1 = r26.getUserAgent()
            java.lang.String r13 = "RegiMgr"
            r12 = 1
            if (r1 == 0) goto L_0x0031
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = r26.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            if (r1 == r2) goto L_0x0031
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = r26.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.DEREGISTERING
            if (r1 != r2) goto L_0x0028
            java.lang.String r0 = "registerInternal: skip re-register during deregistration"
            com.sec.internal.log.IMSLog.e(r13, r14, r0)
            return r12
        L_0x0028:
            com.sec.internal.helper.SimpleEventLog r0 = r0.mEventLog
            java.lang.String r1 = "registerInternal: re-register is not allowed if not registered. Delete UA first."
            r0.logAndAdd(r14, r15, r1)
            return r12
        L_0x0031:
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.AIRPLANE_MODE
            android.content.Context r2 = r0.mContext
            r11 = 0
            int r1 = r1.get(r2, r11)
            int r2 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.AIRPLANE_MODE_ON
            if (r1 != r2) goto L_0x0040
            r1 = r12
            goto L_0x0041
        L_0x0040:
            r1 = r11
        L_0x0041:
            java.util.List r2 = r0.getPendingRegistration(r14)
            com.sec.internal.constants.Mno r3 = r26.getMno()
            boolean r3 = r3.isKor()
            if (r3 == 0) goto L_0x0057
            boolean r3 = r26.isRcsOnly()
            if (r3 == 0) goto L_0x0057
            if (r1 == 0) goto L_0x0061
        L_0x0057:
            com.sec.internal.constants.Mno r1 = r26.getMno()
            boolean r1 = r1.isKor()
            if (r1 != 0) goto L_0x006f
        L_0x0061:
            com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager r1 = r0.mRcsPolicyManager
            boolean r1 = r1.doRcsConfig(r15, r2)
            if (r1 == 0) goto L_0x006f
            java.lang.String r0 = "RCS auto-configuration triggered. Stop."
            com.sec.internal.log.IMSLog.i(r13, r14, r15, r0)
            return r12
        L_0x006f:
            com.sec.internal.ims.core.RcsRegistration$Builder r7 = r26.getRcsRegistrationBuilder()
            com.sec.ims.settings.ImsProfile r10 = r26.getProfile()
            int r1 = r26.getPdnType()
            java.lang.String r1 = r0.getInstanceId(r14, r1, r10)
            boolean r2 = r10.isSamsungMdmnEnabled()
            if (r2 == 0) goto L_0x0091
            java.lang.String r2 = r10.getDuid()
            boolean r3 = android.text.TextUtils.isEmpty(r2)
            if (r3 != 0) goto L_0x0091
            r9 = r2
            goto L_0x0092
        L_0x0091:
            r9 = r1
        L_0x0092:
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
            int r2 = r10.getCmcType()
            if (r2 == 0) goto L_0x00a3
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r1 = r0.mCmcAccountManager
            android.os.Bundle r1 = r1.getCmcRegiConfigForUserAgent()
        L_0x00a3:
            r16 = r1
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r1 = r0.mSimManagers
            java.lang.Object r1 = r1.get(r14)
            com.sec.internal.interfaces.ims.core.ISimManager r1 = (com.sec.internal.interfaces.ims.core.ISimManager) r1
            if (r1 != 0) goto L_0x00b0
            return r12
        L_0x00b0:
            java.lang.String r8 = r25.getPrivateUserIdentity(r26)
            boolean r2 = r0.validateImpi(r15, r1, r8)
            if (r2 != 0) goto L_0x00e0
            int r1 = r0.mRegiRetryLimit
            r2 = 4
            if (r1 < r2) goto L_0x00d7
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "registerInternal: REGI_RETRY_LIMIT is "
            r1.append(r2)
            int r0 = r0.mRegiRetryLimit
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            com.sec.internal.log.IMSLog.i(r13, r14, r0)
            goto L_0x00df
        L_0x00d7:
            int r1 = r1 + r12
            r0.mRegiRetryLimit = r1
            com.sec.internal.interfaces.ims.config.IConfigModule r0 = r0.mConfigModule
            r0.startAcs(r14)
        L_0x00df:
            return r12
        L_0x00e0:
            r0.mRegiRetryLimit = r11
            java.lang.String r6 = r0.getPublicUserIdentity(r15, r1)
            boolean r2 = r0.validateImpu(r15, r6)
            if (r2 != 0) goto L_0x00ed
            return r12
        L_0x00ed:
            r5 = r27
            java.lang.String r17 = r0.getInterfaceName(r15, r5, r14)
            java.lang.String r2 = r10.getSipUserAgent()
            java.lang.String r2 = r0.buildUserAgentString(r10, r2, r14)
            r10.setSipUserAgent(r2)
            int r4 = r26.getPdnType()
            java.lang.String r2 = r26.getReason()
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r3 = r26.getGovernor()
            r3.startTimsTimer(r2)
            java.lang.String r3 = ""
            r15.setReason(r3)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r3 = r26.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r11 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            if (r3 != r11) goto L_0x011d
            r15.setUpdateRegistering(r12)
        L_0x011d:
            r11 = 0
            r15.setPendingUpdate(r11)
            r15.setHasForcedPendingUpdate(r11)
            r15.setHasPendingEpdgHandover(r11)
            r15.setImmediatePendingUpdate(r11)
            com.sec.internal.helper.SimpleEventLog r3 = r0.mEventLog
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "registerInternal : "
            r11.append(r12)
            r11.append(r2)
            java.lang.String r11 = r11.toString()
            r3.logAndAdd(r14, r15, r11)
            java.util.List r3 = r26.getFilteredReason()
            java.util.Iterator r3 = r3.iterator()
        L_0x0149:
            boolean r11 = r3.hasNext()
            if (r11 == 0) goto L_0x0173
            java.lang.Object r11 = r3.next()
            java.lang.String r11 = (java.lang.String) r11
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            r12.append(r14)
            r19 = r3
            java.lang.String r3 = ",RMSVC,"
            r12.append(r3)
            r12.append(r11)
            java.lang.String r3 = r12.toString()
            r11 = 285212672(0x11000000, float:1.00974196E-28)
            com.sec.internal.log.IMSLog.c(r11, r3)
            r3 = r19
            goto L_0x0149
        L_0x0173:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r14)
            java.lang.String r11 = ",REGI:"
            r3.append(r11)
            r3.append(r4)
            java.lang.String r11 = ":"
            r3.append(r11)
            r3.append(r2)
            java.lang.String r2 = r3.toString()
            r3 = 285343745(0x11020001, float:1.0255193E-28)
            com.sec.internal.log.IMSLog.c(r3, r2)
            android.os.Bundle r2 = new android.os.Bundle
            r2.<init>()
            java.util.Set r3 = r10.getAllServiceSetFromAllNetwork()
            boolean r3 = com.sec.internal.ims.rcs.util.RcsUtils.isAutoConfigNeeded(r3)
            if (r3 == 0) goto L_0x01d5
            com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager r2 = r0.mRcsPolicyManager
            com.sec.internal.constants.Mno r3 = r1.getSimMno()
            com.sec.internal.interfaces.ims.IImsFramework r1 = r0.mImsFramework
            com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager r1 = r1.getServiceModuleManager()
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r1 = r1.getImModule()
            if (r1 == 0) goto L_0x01c5
            com.sec.internal.interfaces.ims.IImsFramework r1 = r0.mImsFramework
            com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager r1 = r1.getServiceModuleManager()
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r1 = r1.getImModule()
            com.sec.internal.ims.servicemodules.im.ImConfig r1 = r1.getImConfig(r14)
            goto L_0x01c6
        L_0x01c5:
            r1 = 0
        L_0x01c6:
            r11 = r1
            r1 = r2
            r2 = r10
            r12 = r4
            r5 = r14
            r19 = r6
            r6 = r11
            android.os.Bundle r1 = r1.getRcsConfigForUserAgent(r2, r3, r4, r5, r6, r7)
            r20 = r1
            goto L_0x01da
        L_0x01d5:
            r12 = r4
            r19 = r6
            r20 = r2
        L_0x01da:
            com.sec.internal.interfaces.ims.core.handler.IRegistrationInterface r1 = r0.mRegStackIf
            com.sec.ims.options.Capabilities r6 = r0.getOwnCapabilities(r10, r14)
            java.lang.String r7 = r0.getHomeNetworkDomain(r10, r14)
            java.lang.String r11 = r0.getUuid(r14, r10)
            java.util.List<java.lang.String> r5 = r0.mThirdPartyFeatureTags
            boolean r21 = r0.isVoWiFiSupported(r14)
            r2 = r26
            r3 = r17
            r4 = r27
            r17 = r5
            r5 = r28
            r27 = r8
            r8 = r19
            r28 = r9
            r9 = r27
            r19 = r10
            r10 = r28
            r22 = r12
            r18 = 1
            r12 = r17
            r23 = r13
            r13 = r20
            r24 = r14
            r14 = r16
            r15 = r21
            boolean r1 = r1.registerInternal(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15)
            if (r1 != 0) goto L_0x0240
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            java.lang.String r2 = "registerInternal: failed to create UserAgent."
            r3 = r26
            r4 = r24
            r1.logAndAdd(r4, r3, r2)
            r26.clearUserAgent()
            com.sec.internal.ims.core.PdnController r1 = r0.mPdnController
            r2 = r22
            boolean r1 = r1.isConnected(r2, r3)
            if (r1 == 0) goto L_0x0239
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTED
            r3.setState(r1)
            goto L_0x023e
        L_0x0239:
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            r3.setState(r1)
        L_0x023e:
            r12 = 0
            goto L_0x0244
        L_0x0240:
            r3 = r26
            r12 = r18
        L_0x0244:
            boolean r1 = com.sec.internal.ims.core.RegistrationUtils.isCmcProfile(r19)
            if (r1 == 0) goto L_0x02a4
            java.lang.String r1 = "start p2p in registerInternal"
            r2 = r23
            android.util.Log.i(r2, r1)
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r1 = r0.mVsm
            if (r1 == 0) goto L_0x02a4
            com.sec.internal.interfaces.ims.core.IUserAgent r1 = r26.getUserAgent()
            if (r1 == 0) goto L_0x02a4
            java.lang.String r1 = "@"
            r3 = r27
            boolean r4 = r3.contains(r1)
            if (r4 == 0) goto L_0x0270
            int r1 = r3.indexOf(r1)
            r4 = 0
            java.lang.String r8 = r3.substring(r4, r1)
            goto L_0x0271
        L_0x0270:
            r8 = r3
        L_0x0271:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "lineId : "
            r1.append(r3)
            r1.append(r8)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r2, r1)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "deviceId : "
            r1.append(r3)
            r3 = r28
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r2, r1)
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r0 = r0.mVsm
            com.sec.internal.interfaces.ims.servicemodules.volte2.ICmcServiceHelper r0 = r0.getCmcServiceHelper()
            r0.startP2p(r3, r8)
        L_0x02a4:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerInternal.registerInternal(com.sec.internal.ims.core.RegisterTask, java.lang.String, java.util.Set):boolean");
    }

    private boolean needVolteOnlyRegForDualRcs(int i) {
        return SimUtil.isDualIMS() && RcsUtils.DualRcs.needToCheckOmcCodeAndSimMno(i) && !RcsUtils.DualRcs.dualRcsPolicyCase(this.mContext, i);
    }

    /* access modifiers changed from: package-private */
    public Capabilities getOwnCapabilities(ImsProfile imsProfile, int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "getOwnCapabilities:");
        ICapabilityDiscoveryModule capabilityDiscoveryModule = this.mImsFramework.getServiceModuleManager().getCapabilityDiscoveryModule();
        Integer rcsConfVersion = this.mConfigModule.getRcsConfVersion(i);
        if (!imsProfile.getNeedAutoconfig() || capabilityDiscoveryModule == null || !capabilityDiscoveryModule.isRunning() || rcsConfVersion == null || rcsConfVersion.intValue() <= 0 || !RcsUtils.UiUtils.isRcsEnabledinSettings(this.mContext, i) || (!RcsUtils.DualRcs.isDualRcsReg() && (needVolteOnlyRegForDualRcs(i) || i != SimUtil.getActiveDataPhoneId()))) {
            return setVolteFeatures(new Capabilities(), imsProfile, i);
        }
        Capabilities ownCapabilitiesBase = capabilityDiscoveryModule.getOwnCapabilitiesBase(i);
        if (ownCapabilitiesBase == null) {
            Log.i(IRegistrationManager.LOG_TAG, "getOwnCapabilities: ownCap is null, create empty Capabilities");
            return new Capabilities();
        } else if (ownCapabilitiesBase.getFeature() != ((long) Capabilities.FEATURE_OFFLINE_RCS_USER)) {
            return ownCapabilitiesBase;
        } else {
            Log.i(IRegistrationManager.LOG_TAG, "getOwnCapabilities: no feature present, check for VoLTE only features");
            return setVolteFeatures(ownCapabilitiesBase, imsProfile, i);
        }
    }

    /* access modifiers changed from: package-private */
    public Capabilities setVolteFeatures(Capabilities capabilities, ImsProfile imsProfile, int i) {
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        if (iVolteServiceModule != null) {
            capabilities.setFeatures(iVolteServiceModule.getSupportFeature(i));
        }
        if (RegistrationUtils.isCmcProfile(imsProfile)) {
            capabilities.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL));
            Log.i(IRegistrationManager.LOG_TAG, "getOwnCapabilities : add mmtel to Capabilities for CMC-REGI");
        }
        return capabilities;
    }

    /* access modifiers changed from: protected */
    public void tryEmergencyRegister(int i, ImsProfile imsProfile, Message message, boolean z) {
        if (imsProfile.getPdnType() == 11) {
            for (ImsRegistration imsProfile2 : getRegistrationInfo()) {
                if (imsProfile2.getImsProfile().getPdnType() == 11) {
                    Log.i(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: Emergency is supported via IMS PDN");
                    message.sendToTarget();
                    return;
                }
            }
        }
        RegisterTask registerTask = new RegisterTask(imsProfile, this, this.mTelephonyManager, this.mPdnController, this.mContext, this.mVsm, this.mConfigModule, i);
        registerTask.setResultMessage(message);
        registerTask.setProfile(imsProfile);
        if (registerTask.getMno() == Mno.ATT) {
            registerTask.mKeepPdn = true;
        }
        if (z && (registerTask.getMno() == Mno.TELSTRA || registerTask.getMno().isCanada())) {
            this.mRegStackIf.configure(i);
        }
        this.mHandler.requestTryEmergencyRegister(registerTask);
    }

    /* access modifiers changed from: protected */
    public void tryDeregisterInternal(IRegisterTask iRegisterTask, boolean z, boolean z2) {
        iRegisterTask.setKeepPdn(z2);
        IUserAgent userAgent = iRegisterTask.getUserAgent();
        ImsRegistration imsRegistration = SlotBasedConfig.getInstance(iRegisterTask.getPhoneId()).getImsRegistrations().get(Integer.valueOf(IRegistrationManager.getRegistrationInfoId(iRegisterTask.getProfile().getId(), iRegisterTask.getPhoneId())));
        if (imsRegistration != null) {
            imsRegistration.setDeregiReason(iRegisterTask.getDeregiReason());
            this.mImsFramework.getServiceModuleManager().notifyDeregistering(imsRegistration);
            this.mSecImsServiceConnector.getSipTransportImpl(imsRegistration.getPhoneId()).notifyDeRegistering(imsRegistration);
        }
        if (iRegisterTask.getGovernor().isNeedDelayedDeregister() || ((iRegisterTask.getProfile().getCmcType() == 1 || (userAgent != null && userAgent.getSuspendState())) && !z)) {
            iRegisterTask.getGovernor().setNeedDelayedDeregister(false);
            this.mHandler.requestDelayedDeRegister(iRegisterTask, z, 300);
            return;
        }
        deregisterInternal(iRegisterTask, z);
    }

    /* access modifiers changed from: protected */
    public void deregisterInternal(IRegisterTask iRegisterTask, boolean z) {
        int phoneId = iRegisterTask.getPhoneId();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, iRegisterTask, "deregisterInternal: local=" + z + " reason=" + iRegisterTask.getReason());
        if (this.mHandler.hasMessages(145, iRegisterTask)) {
            this.mHandler.removeMessages(145, iRegisterTask);
        }
        if (iRegisterTask.getUserAgent() == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, iRegisterTask, "deregister: ua is null");
            if (iRegisterTask.getMno() == Mno.KDDI || !iRegisterTask.getProfile().hasEmergencySupport()) {
                if (this.mPdnController.isConnected(iRegisterTask.getPdnType(), iRegisterTask)) {
                    iRegisterTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
                } else {
                    iRegisterTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
                if (iRegisterTask.getMno() == Mno.KDDI) {
                    this.mHandler.sendTryRegister(phoneId, 200);
                } else {
                    this.mHandler.sendTryRegister(phoneId, 500);
                }
            } else {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, iRegisterTask, "deregister: this task will be deleted. do nothing");
            }
        } else {
            this.mRegStackIf.deregisterInternal(iRegisterTask, z);
            iRegisterTask.setReason("");
            this.mHandler.setDeregisterTimeout(iRegisterTask);
            if (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING || (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY && iRegisterTask.needKeepEmergencyTask())) {
                iRegisterTask.setState(RegistrationConstants.RegisterTaskState.DEREGISTERING);
            }
            this.mHandler.removeMessages(100, iRegisterTask);
        }
    }

    /* access modifiers changed from: protected */
    public void onRegistered(IRegisterTask iRegisterTask) {
        int phoneId = iRegisterTask.getPhoneId();
        StringBuilder sb = new StringBuilder();
        IMSLogTimer.setVolteRegisterEndTime(phoneId);
        IMSLog.i(IRegistrationManager.LOG_TAG, "#IMSREGI - KPI[" + phoneId + "]: " + iRegisterTask.getRegistrationRat() + " LATCH " + (((double) (IMSLogTimer.getLatchEndTime(phoneId) - IMSLogTimer.getLatchStartTime(phoneId))) / 1000.0d) + "s");
        IMSLog.i(IRegistrationManager.LOG_TAG, "#IMSREGI - KPI[" + phoneId + "]: PDN Setup " + (((double) (IMSLogTimer.getPdnEndTime(phoneId) - IMSLogTimer.getPdnStartTime(phoneId))) / 1000.0d) + "s [Request by IMS : " + IMSLogTimer.getIsImsPdnRequest(phoneId) + "]");
        StringBuilder sb2 = new StringBuilder();
        sb2.append("#IMSREGI - KPI[");
        sb2.append(phoneId);
        sb2.append("]: SIP Registration ");
        sb2.append(((double) (IMSLogTimer.getVolteRegisterEndTime(phoneId) - IMSLogTimer.getVolteRegisterStartTime(phoneId))) / 1000.0d);
        sb2.append("s");
        IMSLog.i(IRegistrationManager.LOG_TAG, sb2.toString());
        sb.append("#IMSREGI - KPI[");
        sb.append(phoneId);
        sb.append("]: Total Time ");
        sb.append(((double) (IMSLogTimer.getVolteRegisterEndTime(phoneId) - IMSLogTimer.getLatchStartTime(phoneId))) / 1000.0d);
        sb.append("s, <Gap IMS PDN from Request to Connected : ");
        sb.append(((double) (IMSLogTimer.getVolteRegisterStartTime(phoneId) - IMSLogTimer.getPdnEndTime(phoneId))) / 1000.0d);
        sb.append("s>, <Gap Latch from Data Attch to PDN Request : ");
        sb.append(((double) (IMSLogTimer.getPdnStartTime(phoneId) - IMSLogTimer.getLatchEndTime(phoneId))) / 1000.0d);
        if (IMSLogTimer.getIsImsPdnRequest(phoneId)) {
            sb.append("s>");
        } else {
            sb.append("s (Request by RIL)>");
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, sb.toString());
        if (iRegisterTask.getUserAgent() == null) {
            this.mEventLog.logAndAdd(phoneId, iRegisterTask, "onRegistered: Failed to process. UA has already removed");
        } else if (!this.mRegStackIf.isUserAgentInRegistered(iRegisterTask)) {
            this.mEventLog.logAndAdd(phoneId, iRegisterTask, "onRegistered: Failed to process. UA is not registered!");
        } else {
            ImsRegistration imsRegistration = iRegisterTask.getImsRegistration();
            int registrationRat = iRegisterTask.getRegistrationRat();
            if (iRegisterTask.getPdnType() == 11) {
                if (this.mPdnController.isEpdgConnected(phoneId)) {
                    registrationRat = 18;
                } else {
                    registrationRat = this.mPdnController.getNetworkState(phoneId).getMobileDataNetworkType();
                }
            }
            imsRegistration.setRegiRat(registrationRat);
            imsRegistration.setCurrentRat(registrationRat);
            ImsProfile profile = iRegisterTask.getProfile();
            SlotBasedConfig.getInstance(phoneId).addImsRegistration(IRegistrationManager.getRegistrationInfoId(profile.getId(), phoneId), imsRegistration);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(phoneId, iRegisterTask, "onRegistered: RAT = " + iRegisterTask.getRegistrationRat() + ", profile=" + profile.getName() + ", service=" + Arrays.toString(imsRegistration.getServices().toArray()));
            IMSLog.c(LogClass.REGI_REGISTERED, phoneId + ",REG OK:" + iRegisterTask.getRegistrationRat() + ":" + iRegisterTask.getMno().getName() + ":" + profile.getPdn() + ":" + DiagnosisConstants.convertServiceSetToHex(imsRegistration.getServices()));
            if (ImsGateConfig.isGateEnabled()) {
                IMSLog.g("GATE", "<GATE-M>IMS_ENABLED_PS_IND_" + SemSystemProperties.get(ImsConstants.SystemProperties.PS_INDICATOR) + "</GATE-M>");
            }
            iRegisterTask.setState(RegistrationConstants.RegisterTaskState.REGISTERED);
            iRegisterTask.clearUpdateRegisteringFlag();
            iRegisterTask.setIsRefreshReg(false);
            UriGeneratorFactory.getInstance().updateUriGenerator(imsRegistration, this.mRcsPolicyManager.getRcsNetworkUriType(phoneId, profile.getRemoteUriType(), profile.getNeedAutoconfig()));
            RegistrationUtils.updateImsIcon(iRegisterTask);
            if (SimUtil.isDualIMS()) {
                tryRegister(ImsConstants.Phone.SLOT_1);
                tryRegister(ImsConstants.Phone.SLOT_2);
            } else {
                tryRegister(phoneId);
            }
            notifyImsRegistration(imsRegistration, true, iRegisterTask, new ImsRegistrationError());
            if (iRegisterTask.getProfile().hasEmergencySupport() && iRegisterTask.getResultMessage() != null) {
                iRegisterTask.getResultMessage().sendToTarget();
                iRegisterTask.setResultMessage((Message) null);
            }
            if (!SimUtil.isSoftphoneEnabled()) {
                RegistrationUtils.saveRegisteredImpu(this.mContext, imsRegistration, getSimManager(phoneId));
            } else {
                this.mAresLookupRequired = true;
            }
            iRegisterTask.getGovernor().onRegistrationDone();
            this.mHandler.sendEmptyMessage(32);
            reportRegistrationStatus(iRegisterTask);
            DiagnosisConstants.REGI_REQC regiRequestType = iRegisterTask.getRegiRequestType();
            DiagnosisConstants.REGI_REQC regi_reqc = DiagnosisConstants.REGI_REQC.REFRESH;
            if (regiRequestType != regi_reqc) {
                reportRegistrationCount(iRegisterTask);
            }
            reportDualImsStatus(phoneId);
            IMSLog.lazer(iRegisterTask, ImsConstants.Intents.EXTRA_REGISTERED);
            iRegisterTask.setReason("");
            iRegisterTask.setEpdgHandoverInProgress(false);
            iRegisterTask.setRegiRequestType(regi_reqc);
            iRegisterTask.setDeregiReason(41);
        }
    }

    /* access modifiers changed from: protected */
    public void onRegisterError(IRegisterTask iRegisterTask, int i, SipError sipError, long j) {
        IRegisterTask iRegisterTask2 = iRegisterTask;
        SipError sipError2 = sipError;
        long j2 = j;
        int phoneId = iRegisterTask.getPhoneId();
        this.mEventLog.logAndAdd(phoneId, iRegisterTask, "onRegisterError: error " + sipError2 + " retryAfterMs " + j2);
        IMSLog.c(LogClass.REGI_REGISTER_ERROR, iRegisterTask.getPhoneId() + ",REG ERR:" + iRegisterTask.getMno().getName() + ":" + iRegisterTask.getProfile().getPdn() + ":" + sipError2 + ":" + j2);
        iRegisterTask.setEpdgHandoverInProgress(false);
        if (!SipErrorBase.UNAUTHORIZED.equals(sipError2) || iRegisterTask.isRcsOnly()) {
            if ((iRegisterTask.getMno() == Mno.KDDI || iRegisterTask.getMno().isKor()) && (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || iRegisterTask.isRefreshReg())) {
                iRegisterTask.setIsRefreshReg(true);
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onRegisterError: mIsRefreshReg " + iRegisterTask.isRefreshReg());
            } else if (this.mPdnController.isConnected(iRegisterTask.getPdnType(), iRegisterTask)) {
                iRegisterTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            } else {
                iRegisterTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            removeAdhocProfile(phoneId, iRegisterTask);
            makeThrottle(phoneId, iRegisterTask);
            try {
                if (iRegisterTask.getProfile().hasEmergencySupport()) {
                    if (iRegisterTask.getMno() == Mno.VZW) {
                        if (SipErrorBase.SIP_TIMEOUT.equals(sipError2)) {
                            iRegisterTask.getGovernor().onRegistrationError(sipError2, j2, false);
                            if (iRegisterTask.getGovernor().getFailureCount() < 2) {
                                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onRegisterError: Emergency Registration timed out. Retry.");
                            }
                        }
                        RegistrationUtils.sendEmergencyRegistrationFailed(iRegisterTask);
                    } else if (iRegisterTask.getMno() == Mno.KDDI) {
                        iRegisterTask.getGovernor().onRegistrationError(sipError2, j2, false);
                        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onRegisterError: Emergency Registration Error Retry Infinitely.");
                    } else {
                        if (iRegisterTask.getProfile().getE911RegiTime() > 0 && SipErrorBase.SIP_TIMEOUT.equals(sipError2)) {
                            iRegisterTask.getGovernor().onRegistrationError(sipError2, j2, false);
                            if (iRegisterTask.getGovernor().getFailureCount() < iRegisterTask.getGovernor().getNumOfEmerPcscfIp()) {
                                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onRegisterError: Emergency Registration Error Retry to next PCSCF");
                            }
                        }
                        RegistrationUtils.sendEmergencyRegistrationFailed(iRegisterTask);
                    }
                    this.mRegStackIf.onRegisterError(iRegisterTask, i, sipError, j);
                    return;
                }
                iRegisterTask.getGovernor().onRegistrationError(sipError2, j2, false);
                int failureType = iRegisterTask.getGovernor().getFailureType();
                int detailedDeRegiReason = iRegisterTask.getGovernor().getDetailedDeRegiReason(failureType);
                if (iRegisterTask.getDeregiCause(sipError2) == 32) {
                    failureType = 32;
                }
                if (failureType != 16) {
                    iRegisterTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_IMS_NOT_AVAILABLE);
                }
                boolean isEpdgConnected = this.mPdnController.isEpdgConnected(phoneId);
                if (iRegisterTask.getImsRegistration() != null) {
                    isEpdgConnected = iRegisterTask.getImsRegistration().getEpdgStatus();
                }
                try {
                    notifyImsRegistration(ImsRegistration.getBuilder().setHandle(i).setImsProfile(new ImsProfile(iRegisterTask.getProfile())).setServices(iRegisterTask.getProfile().getServiceSet(Integer.valueOf(iRegisterTask.getRegistrationRat()))).setEpdgStatus(isEpdgConnected).setPdnType(iRegisterTask.getPdnType()).setUuid(getUuid(phoneId, iRegisterTask.getProfile())).setInstanceId(getInstanceId(phoneId, iRegisterTask.getPdnType(), iRegisterTask.getProfile())).setNetwork(iRegisterTask.getNetworkConnected()).setRegiRat(iRegisterTask.getRegistrationRat()).setPhoneId(phoneId).build(), false, iRegisterTask, new ImsRegistrationError(sipError.getCode(), sipError.getReason(), detailedDeRegiReason, failureType));
                    reportRegistrationStatus(iRegisterTask);
                    reportRegistrationCount(iRegisterTask);
                    IMSLog.lazer(iRegisterTask, "REGISTRATION FAILED : " + sipError2);
                    this.mRegStackIf.onRegisterError(iRegisterTask, i, sipError, j);
                } catch (Throwable th) {
                    th = th;
                    this.mRegStackIf.onRegisterError(iRegisterTask, i, sipError, j);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                int i2 = i;
                this.mRegStackIf.onRegisterError(iRegisterTask, i, sipError, j);
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x005e, code lost:
        if (((java.lang.Integer) java.util.Optional.ofNullable(r5.mAuEmergencyProfile.get(r6)).map(new com.sec.internal.ims.core.RegistrationManagerInternal$$ExternalSyntheticLambda0()).orElse(-1)).intValue() == r7.getProfile().getId()) goto L_0x008a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeAdhocProfile(int r6, com.sec.internal.interfaces.ims.core.IRegisterTask r7) {
        /*
            r5 = this;
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r0 = r5.mSimManagers
            java.lang.Object r0 = r0.get(r6)
            com.sec.internal.interfaces.ims.core.ISimManager r0 = (com.sec.internal.interfaces.ims.core.ISimManager) r0
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r1 = com.sec.internal.ims.core.RegistrationUtils.getPendingRegistrationInternal(r6)
            if (r0 == 0) goto L_0x00c7
            if (r1 != 0) goto L_0x0012
            goto L_0x00c7
        L_0x0012:
            com.sec.internal.constants.Mno r2 = r7.getMno()
            boolean r2 = r2.isAus()
            r3 = 1
            r4 = 0
            if (r2 == 0) goto L_0x0061
            com.sec.ims.settings.ImsProfile r2 = r7.getProfile()
            boolean r2 = r2.hasEmergencySupport()
            if (r2 == 0) goto L_0x0061
            com.sec.internal.constants.Mno r2 = r0.getDevMno()
            boolean r0 = com.sec.internal.ims.core.RegistrationUtils.checkAusEmergencyCall(r2, r6, r0)
            if (r0 == 0) goto L_0x0061
            android.util.SparseArray<com.sec.ims.settings.ImsProfile> r5 = r5.mAuEmergencyProfile
            java.lang.Object r5 = r5.get(r6)
            com.sec.ims.settings.ImsProfile r5 = (com.sec.ims.settings.ImsProfile) r5
            java.util.Optional r5 = java.util.Optional.ofNullable(r5)
            com.sec.internal.ims.core.RegistrationManagerInternal$$ExternalSyntheticLambda0 r0 = new com.sec.internal.ims.core.RegistrationManagerInternal$$ExternalSyntheticLambda0
            r0.<init>()
            java.util.Optional r5 = r5.map(r0)
            r0 = -1
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            java.lang.Object r5 = r5.orElse(r0)
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            com.sec.ims.settings.ImsProfile r0 = r7.getProfile()
            int r0 = r0.getId()
            if (r5 != r0) goto L_0x0089
            goto L_0x008a
        L_0x0061:
            com.sec.internal.ims.core.SlotBasedConfig r5 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r6)
            java.util.List r5 = r5.getProfiles()
            java.util.Iterator r5 = r5.iterator()
        L_0x006d:
            boolean r0 = r5.hasNext()
            if (r0 == 0) goto L_0x0089
            java.lang.Object r0 = r5.next()
            com.sec.ims.settings.ImsProfile r0 = (com.sec.ims.settings.ImsProfile) r0
            int r0 = r0.getId()
            com.sec.ims.settings.ImsProfile r2 = r7.getProfile()
            int r2 = r2.getId()
            if (r0 != r2) goto L_0x006d
            r4 = r3
            goto L_0x006d
        L_0x0089:
            r3 = r4
        L_0x008a:
            if (r3 != 0) goto L_0x00c7
            com.sec.internal.ims.core.SlotBasedConfig r5 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r6)
            java.util.Map r5 = r5.getExtendedProfiles()
            com.sec.ims.settings.ImsProfile r0 = r7.getProfile()
            int r0 = r0.getId()
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            boolean r5 = r5.containsKey(r0)
            if (r5 != 0) goto L_0x00c7
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r0 = "onDeregisterd: remove RegisterTask: "
            r5.append(r0)
            com.sec.ims.settings.ImsProfile r0 = r7.getProfile()
            java.lang.String r0 = r0.getName()
            r5.append(r0)
            java.lang.String r5 = r5.toString()
            java.lang.String r0 = "RegiMgr"
            com.sec.internal.log.IMSLog.i(r0, r6, r5)
            r1.remove(r7)
        L_0x00c7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerInternal.removeAdhocProfile(int, com.sec.internal.interfaces.ims.core.IRegisterTask):void");
    }

    /* access modifiers changed from: package-private */
    public void makeThrottle(int i, IRegisterTask iRegisterTask) {
        if (iRegisterTask.getProfile().getCmcType() == 8) {
            IMSLog.d(IRegistrationManager.LOG_TAG, i, "onRegisterError: don't retry register");
            iRegisterTask.getGovernor().makeThrottle();
        }
    }

    /* access modifiers changed from: protected */
    public void onDeregistered(IRegisterTask iRegisterTask, SipError sipError, long j, boolean z, boolean z2) {
        IRegisterTask iRegisterTask2 = iRegisterTask;
        boolean z3 = z;
        boolean z4 = z2;
        int phoneId = iRegisterTask.getPhoneId();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, iRegisterTask2, "onDeregistered: rat=" + iRegisterTask.getRegistrationRat() + ", error=" + sipError + ", retryAfterMs=" + j + ", isRequestedDeregi=" + z3 + ", pcscfGone=" + z4 + ", reason=" + iRegisterTask.getDeregiReason() + ", keepPdn=" + iRegisterTask.isKeepPdn());
        if (ImsGateConfig.isGateEnabled()) {
            IMSLog.g("GATE", "<GATE-M>IMS_DISABLED</GATE-M>");
        }
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (pendingRegistrationInternal != null) {
            if (iRegisterTask.getMno().isKor() && iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                iRegisterTask2.setIsRefreshReg(true);
            }
            iRegisterTask2.setRegiFailReason(DiagnosisConstants.REGI_FRSN.OFFSET_DEREGI_REASON.getCode() + iRegisterTask.getDeregiReason());
            reportRegistrationStatus(iRegisterTask);
            IMSLog.lazer(iRegisterTask2, "DE-REGISTERED : Reason - " + iRegisterTask.getDeregiReason());
            ImsRegistration remove = SlotBasedConfig.getInstance(phoneId).getImsRegistrations().remove(Integer.valueOf(IRegistrationManager.getRegistrationInfoId(iRegisterTask.getProfile().getId(), phoneId)));
            iRegisterTask.clearUpdateRegisteringFlag();
            if (!iRegisterTask.getMno().isKor() || !iRegisterTask.isRefreshReg() || z3) {
                iRegisterTask2.setIsRefreshReg(false);
            }
            RegistrationUtils.updateImsIcon(iRegisterTask);
            if (remove != null) {
                int deregiCause = iRegisterTask.getDeregiCause(sipError);
                notifyImsRegistration(remove, false, iRegisterTask2, new ImsRegistrationError(sipError.getCode(), sipError.getReason(), iRegisterTask.getDeregiReason(), deregiCause));
                if (deregiCause == 32) {
                    iRegisterTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_IMS_NOT_AVAILABLE);
                    Log.i(IRegistrationManager.LOG_TAG, "ImsNotAvailable has sent by onDeregistered.");
                    SlotBasedConfig.getInstance(phoneId).setNotifiedImsNotAvailable(true);
                }
            }
            if ((iRegisterTask.getMno().isChn() || iRegisterTask.getMno().isEur()) && this.mIsNonADSDeRegRequired) {
                sendDeregister(12, SimUtil.getOppositeSimSlot(phoneId));
                this.mIsNonADSDeRegRequired = false;
            }
            if (iRegisterTask.getProfile().hasEmergencySupport()) {
                if (this.mMoveNextPcscf) {
                    iRegisterTask.getProfile().setUicclessEmergency(true);
                    iRegisterTask.getGovernor().increasePcscfIdx();
                    this.mMoveNextPcscf = false;
                } else {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onDeregistered: leave it to EMERGENCY state.");
                    iRegisterTask2.setState(RegistrationConstants.RegisterTaskState.EMERGENCY);
                    iRegisterTask2.setIsRefreshReg(false);
                    if (iRegisterTask.getMno() == Mno.KDDI) {
                        this.mRegStackIf.onDeregistered(iRegisterTask, sipError, j, z);
                        if (!iRegisterTask.isKeepPdn()) {
                            stopPdnConnectivity(iRegisterTask.getPdnType(), iRegisterTask2);
                            iRegisterTask2.setState(RegistrationConstants.RegisterTaskState.IDLE);
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
            if (this.mPdnController.isConnected(iRegisterTask.getPdnType(), iRegisterTask2)) {
                iRegisterTask2.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            } else if (iRegisterTask.isKeepPdn()) {
                iRegisterTask2.setState(RegistrationConstants.RegisterTaskState.CONNECTING);
            } else {
                iRegisterTask2.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            iRegisterTask2.setImsRegistration((ImsRegistration) null);
            removeAdhocProfile(pendingRegistrationInternal, iRegisterTask2);
            SecImsNotifier.getInstance().updateAdhocProfile(phoneId, iRegisterTask.getProfile(), false);
            if (z3) {
                handleSolicitedDeregistration(iRegisterTask, sipError);
            } else if (z4) {
                iRegisterTask.getGovernor().resetPcscfList();
                this.mHandler.sendEmptyMessage(32);
                RegistrationManagerHandler registrationManagerHandler = this.mHandler;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(2, Integer.valueOf(iRegisterTask.getPhoneId())));
            } else {
                handleUnSolicitedDeregistration(iRegisterTask, sipError, j);
            }
            IMSLog.c(LogClass.REGI_DEREGISTERED, phoneId + ",DEREG:" + iRegisterTask.getMno().getName() + ":" + iRegisterTask.getProfile().getPdn() + ":" + iRegisterTask.getState());
            iRegisterTask2.setReason("");
            iRegisterTask.getGovernor().onDeregistrationDone(z3);
            iRegisterTask2.setDeregiReason(41);
            iRegisterTask2.setIsRefreshReg(false);
            this.mRegStackIf.onDeregistered(iRegisterTask, sipError, j, z);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleSolicitedDeregistration(IRegisterTask iRegisterTask, SipError sipError) {
        if (!iRegisterTask.isKeepPdn()) {
            if (iRegisterTask.getMno() == Mno.GCF && iRegisterTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && (iRegisterTask.getDeregiCause(sipError) == 2 || iRegisterTask.getDeregiReason() == 73)) {
                RegistrationManagerHandler registrationManagerHandler = this.mHandler;
                registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(133, iRegisterTask), 500);
            } else {
                stopPdnConnectivity(iRegisterTask.getPdnType(), iRegisterTask);
                iRegisterTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                if (!iRegisterTask.getMno().isKor() || !iRegisterTask.isRcsOnly()) {
                    setOmadmState(iRegisterTask.getPhoneId(), RegistrationManager.OmadmConfigState.IDLE);
                }
            }
        }
        tryNextRegistration(iRegisterTask, iRegisterTask.getDeregiReason());
    }

    /* access modifiers changed from: package-private */
    public void handleUnSolicitedDeregistration(IRegisterTask iRegisterTask, SipError sipError, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int phoneId = iRegisterTask.getPhoneId();
        simpleEventLog.logAndAdd(phoneId, iRegisterTask, "onDeregistered: registration error = " + sipError);
        if (SipErrorBase.isRegiTerminatedError(sipError)) {
            iRegisterTask.getGovernor().onRegistrationTerminated(sipError, j, true);
        } else {
            iRegisterTask.getGovernor().onRegistrationError(sipError, j, true);
        }
        if (!this.mPdnController.isConnected(iRegisterTask.getPdnType(), iRegisterTask)) {
            iRegisterTask.getGovernor().resetPcscfList();
        } else {
            iRegisterTask.setKeepPdn(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPendingUpdateRegistration() {
        this.mHandler.removeMessages(32);
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                RegistrationConstants.RegisterTaskState state = registerTask.getState();
                RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.REGISTERED;
                if (state == registerTaskState && registerTask.mHasForcedPendingUpdate) {
                    Log.i(IRegistrationManager.LOG_TAG, "onPendingUpdateRegistration: forced " + registerTask.getProfile().getName());
                    registerTask.mHasForcedPendingUpdate = false;
                    if (registerTask.hasPendingEpdgHandover()) {
                        registerTask.setHasPendingEpdgHandover(false);
                        registerTask.setEpdgHandoverInProgress(true);
                    }
                    if (registerTask.isImmediatePendingUpdate()) {
                        updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.FORCED_PENDING_UPDATE, true);
                    } else {
                        updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.FORCED_PENDING_UPDATE);
                    }
                } else if (registerTask.getState() == registerTaskState && registerTask.mHasPendingUpdate) {
                    Log.i(IRegistrationManager.LOG_TAG, "onPendingUpdateRegistration: " + registerTask.getProfile().getName());
                    registerTask.mHasPendingUpdate = false;
                    if (registerTask.hasPendingEpdgHandover()) {
                        registerTask.setHasPendingEpdgHandover(false);
                        registerTask.setEpdgHandoverInProgress(true);
                    }
                    if (registerTask.isImmediatePendingUpdate()) {
                        updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.PENDING_UPDATE, true);
                    } else {
                        updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.PENDING_UPDATE);
                    }
                } else if (registerTask.getState() == registerTaskState && registerTask.hasPendingDeregister()) {
                    Log.i(IRegistrationManager.LOG_TAG, "onPendingDeRegistration: " + registerTask.getProfile().getName());
                    registerTask.setHasPendingDeregister(false);
                    tryDeregisterInternal(registerTask, false, true);
                }
            }
        }
    }

    private void removeAdhocProfile(SlotBasedConfig.RegisterTaskList registerTaskList, IRegisterTask iRegisterTask) {
        boolean z;
        Iterator<ImsProfile> it = SlotBasedConfig.getInstance(iRegisterTask.getPhoneId()).getProfiles().iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().getId() == iRegisterTask.getProfile().getId()) {
                    z = true;
                    break;
                }
            } else {
                z = false;
                break;
            }
        }
        if (!z && !iRegisterTask.getProfile().hasEmergencySupport() && !SlotBasedConfig.getInstance(iRegisterTask.getPhoneId()).getExtendedProfiles().containsKey(Integer.valueOf(iRegisterTask.getProfile().getId()))) {
            int phoneId = iRegisterTask.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onDeregisterd: Remove RegiTask for [" + iRegisterTask.getProfile().getName() + "]");
            registerTaskList.remove(iRegisterTask);
        }
    }

    /* access modifiers changed from: package-private */
    public void tryNextRegistration(IRegisterTask iRegisterTask, int i) {
        if (iRegisterTask.getPdnType() == 11) {
            long j = RegistrationGovernor.RETRY_AFTER_PDNLOST_MS;
            if (i == 2) {
                iRegisterTask.getGovernor().addDelay(RegistrationGovernor.RETRY_AFTER_PDNLOST_MS, 1);
            } else if (i == 24) {
                iRegisterTask.getGovernor().addDelay(RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
            } else if (i == 27) {
                j = 1000;
                iRegisterTask.getGovernor().addDelay(1000);
            } else if (i != 21 || !iRegisterTask.isKeepPdn()) {
                j = 0;
            } else {
                iRegisterTask.getGovernor().addDelay(RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
            }
            if (iRegisterTask.getMno() == Mno.KDDI) {
                this.mHandler.sendEmptyMessage(32);
                RegistrationManagerHandler registrationManagerHandler = this.mHandler;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(2, Integer.valueOf(iRegisterTask.getPhoneId())));
                return;
            }
            long j2 = j + 300;
            this.mHandler.sendEmptyMessageDelayed(32, j2);
            RegistrationManagerHandler registrationManagerHandler2 = this.mHandler;
            registrationManagerHandler2.sendMessageDelayed(registrationManagerHandler2.obtainMessage(2, Integer.valueOf(iRegisterTask.getPhoneId())), j2);
            return;
        }
        this.mHandler.sendEmptyMessage(32);
        RegistrationManagerHandler registrationManagerHandler3 = this.mHandler;
        registrationManagerHandler3.sendMessage(registrationManagerHandler3.obtainMessage(2, Integer.valueOf(iRegisterTask.getPhoneId())));
    }

    /* access modifiers changed from: protected */
    public void onSubscribeError(IRegisterTask iRegisterTask, SipError sipError) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int phoneId = iRegisterTask.getPhoneId();
        simpleEventLog.logAndAdd(phoneId, iRegisterTask, "onSubscribeError: error " + sipError);
        iRegisterTask.getGovernor().onSubscribeError(0, sipError);
    }

    /* access modifiers changed from: protected */
    public void onForcedUpdateRegistrationRequested(RegisterTask registerTask) {
        registerTask.setReason("forced update registration");
        updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.FORCED_UPDATE);
    }

    /* access modifiers changed from: protected */
    public void onRefreshRegistration(IRegisterTask iRegisterTask, int i) {
        int phoneId = iRegisterTask.getPhoneId();
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onRefreshRegistration: profile " + iRegisterTask.getProfile().getName() + " handle : " + i);
        if (!SimUtil.isMultiSimSupported()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, iRegisterTask.getPhoneId(), "This model is not for Dual IMS.");
            return;
        }
        if (this.mVsm != null) {
            for (int i2 = 0; i2 < this.mSimManagers.size(); i2++) {
                Iterator it = SlotBasedConfig.getInstance(i2).getRegistrationTasks().iterator();
                while (it.hasNext()) {
                    ImsRegistration imsRegistration = ((RegisterTask) it.next()).mReg;
                    if (imsRegistration != null && imsRegistration.getHandle() != i && this.mVsm.getSessionCount(i2) > 0 && !this.mVsm.hasEmergencyCall(i2) && this.mVsm.hasActiveCall(i2)) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "Active VoLTE call exists on this slot. Try to de-regi.");
                        tryDeregisterInternal(iRegisterTask, true, true);
                        return;
                    }
                }
            }
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, iRegisterTask.getPhoneId(), "onRefreshRegistration: No de-registration has triggered");
    }

    /* access modifiers changed from: protected */
    public void setDelayedDeregisterTimerRunning(IRegisterTask iRegisterTask, boolean z) {
        if (iRegisterTask.getProfile().hasService("mmtel") || iRegisterTask.getProfile().hasService("mmtel-video")) {
            this.mVsm.setDelayedDeregisterTimerRunning(iRegisterTask.getPhoneId(), z);
        }
        if (iRegisterTask.getProfile().hasService("smsip")) {
            this.mImsFramework.getServiceModuleManager().getSmsServiceModule().setDelayedDeregisterTimerRunning(iRegisterTask.getPhoneId(), z);
        }
    }

    private boolean isTryingCmcDualRegi(int i, IRegisterTask iRegisterTask) {
        IRegisterTask cmcRegisterTask = this.mCmcAccountManager.getCmcRegisterTask(SimUtil.getOppositeSimSlot(i));
        return (cmcRegisterTask == null || cmcRegisterTask.getState() == RegistrationConstants.RegisterTaskState.IDLE || !RegistrationUtils.isCmcProfile(iRegisterTask.getProfile())) ? false : true;
    }
}
