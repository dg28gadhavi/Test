package com.sec.internal.ims.core;

import android.content.Context;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import java.util.List;

public class UserEventController {
    private static final String LOG_TAG = "RegiMgr-UsrEvtCtr";
    IConfigModule mConfigModule;
    Context mContext;
    protected int mCurrentUserId;
    protected boolean mIsDeviceShutdown = false;
    PdnController mPdnController;
    RegistrationManagerHandler mRegHandler;
    RegistrationManagerBase mRegMan;
    List<ISimManager> mSimManagers;
    protected SimpleEventLog mSimpleEventLog;
    ITelephonyManager mTelephonyManager;
    IVolteServiceModule mVolteServiceModule;

    public UserEventController(Context context, RegistrationManagerBase registrationManagerBase, PdnController pdnController, List<ISimManager> list, ITelephonyManager iTelephonyManager, SimpleEventLog simpleEventLog) {
        this.mContext = context;
        this.mRegMan = registrationManagerBase;
        this.mPdnController = pdnController;
        this.mSimManagers = list;
        this.mTelephonyManager = iTelephonyManager;
        this.mSimpleEventLog = simpleEventLog;
        this.mCurrentUserId = Extensions.ActivityManager.getCurrentUser();
        Log.i(LOG_TAG, "Start with User " + this.mCurrentUserId);
    }

    public void setCurrentUserId(int i) {
        this.mCurrentUserId = i;
    }

    public void setVolteServiceModule(IVolteServiceModule iVolteServiceModule) {
        this.mVolteServiceModule = iVolteServiceModule;
    }

    public void setConfigModule(IConfigModule iConfigModule) {
        this.mConfigModule = iConfigModule;
    }

    public boolean isShuttingDown() {
        return this.mIsDeviceShutdown;
    }

    public int getCurrentUserId() {
        return this.mCurrentUserId;
    }

    /* access modifiers changed from: protected */
    public void onDataUsageLimitReached(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "onDataUsageLimitReached: " + z);
        SlotBasedConfig.RegisterTaskList<RegisterTask> pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            SlotBasedConfig.getInstance(i).setDataUsageExceed(z);
            for (RegisterTask registerTask : pendingRegistrationInternal) {
                if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    registerTask.setReason("data limited exceed");
                    if (registerTask.getMno() == Mno.BELL) {
                        IMSLog.i(LOG_TAG, i, "onDataUsageLimitReached: force update " + registerTask);
                        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.DATAUSAGE_LIMIT_REACHED_FORCED);
                    } else {
                        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.DATAUSAGE_LIMIT_REACHED);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onChatbotAgreementChanged(int i) {
        Log.i(LOG_TAG, "onChatbotAgreementChanged");
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getProfile().hasService(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION) && registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                    registerTask.setReason("chatbot agreement changed");
                    this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.CHATBOT_AGREEMENT_CHANGED);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMobileDataChanged(int i, int i2, NetworkEventController networkEventController) {
        IMSLog.i(LOG_TAG, i2, "onMobileDataChanged: " + i);
        for (int i3 = 0; i3 < this.mSimManagers.size(); i3++) {
            Iterator it = SlotBasedConfig.getInstance(i3).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    registerTask.setReason("mobile data changed : " + i);
                    Mno mno = registerTask.getMno();
                    if (mno.isOneOf(Mno.ATT, Mno.BELL, Mno.VTR)) {
                        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_MOBILEDATA_CHANGED_FORCED);
                    } else if (mno == Mno.TMOUS && registerTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
                        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_MOBILEDATA_CHANGED);
                    } else if (!registerTask.getGovernor().isMobilePreferredForRcs() || !this.mPdnController.isWifiConnected() || i != 1) {
                        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_MOBILEDATA_CHANGED);
                    } else {
                        networkEventController.isPreferredPdnForRCSRegister(registerTask, i2, true);
                    }
                }
            }
        }
        this.mRegMan.tryRegister(i2);
        if (i == 1) {
            for (int i4 = 0; i4 < this.mSimManagers.size(); i4++) {
                if (i4 != i2 && RcsUtils.DualRcs.isRegAllowed(this.mContext, i4)) {
                    IMSLog.i(LOG_TAG, i2, "onMobileDataChanged: tryRegister RCS on other slot");
                    this.mRegMan.tryRegister(i4);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMobileDataPressedChanged(int i, int i2, NetworkEventController networkEventController) {
        IMSLog.i(LOG_TAG, i2, "onMobileDataPressedChanged: " + i);
        for (int i3 = 0; i3 < this.mSimManagers.size(); i3++) {
            Iterator it = SlotBasedConfig.getInstance(i3).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && registerTask.getGovernor().isMobilePreferredForRcs() && this.mPdnController.isWifiConnected() && i == 1) {
                    networkEventController.isPreferredPdnForRCSRegister(registerTask, i2, true);
                    this.mRegHandler.sendTryRegister(registerTask.getPhoneId(), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRoamingDataChanged(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "onRoamingDataChanged: " + z);
        ISimManager iSimManager = this.mSimManagers.get(i);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (iSimManager != null && pendingRegistrationInternal != null && !iSimManager.getSimMno().isKor()) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    registerTask.setReason("roaming data changed : " + z);
                    this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_ROAMINGDATA_CHANGED);
                }
            }
            this.mRegMan.tryRegister(i);
        }
    }

    /* access modifiers changed from: protected */
    public void onRoamingSettingsChanged(int i, int i2) {
        IMSLog.i(LOG_TAG, i2, "onRoamingSettingsChanged: " + i);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i2);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (!RegistrationUtils.hasVolteService(registerTask.getPhoneId(), registerTask.getProfile()) && ConfigUtil.isRcsEur(registerTask.getPhoneId()) && registerTask.isRcsOnly()) {
                    if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        if (i == 0) {
                            registerTask.setReason("Roaming Setting turned off");
                            this.mRegMan.tryDeregisterInternal(registerTask, false, true);
                        }
                    } else if (i == 1 || i == 2) {
                        this.mRegMan.tryRegister(registerTask.getPhoneId());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onVideoCallServiceSettingChanged(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "onVideoCallServiceSettingChanged:" + z);
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager != null) {
            Mno simMno = iSimManager.getSimMno();
            DmConfigHelper.setImsUserSetting(this.mContext, ImsConstants.SystemSettings.VILTE_SLOT1.getName(), z ^ true ? 1 : 0, i);
            SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
            if (pendingRegistrationInternal != null) {
                Iterator it = pendingRegistrationInternal.iterator();
                while (it.hasNext()) {
                    RegisterTask registerTask = (RegisterTask) it.next();
                    if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        if (simMno == Mno.VZW) {
                            registerTask.setReason("Video Call state changed : " + z);
                            this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_VIDEOCALLSTATE_CHANGED);
                        } else if (simMno != Mno.TMOUS) {
                            registerTask.setReason("Video Call state changed : " + z);
                            this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_VIDEOCALLSTATE_CHANGED_FORCED);
                        } else if (registerTask.getRegistrationRat() != 18 || !SemEmergencyManager.isEmergencyMode(this.mContext)) {
                            registerTask.setReason("Video Call state changed : " + z);
                            this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_VIDEOCALLSTATE_CHANGED_FORCED);
                        } else {
                            Log.i(LOG_TAG, "skip update registration");
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRcsUserSettingChanged(int i, int i2) {
        Log.i(LOG_TAG, "onRcsUserSettingChanged: switch: " + i);
        Context context = this.mContext;
        ImsConstants.SystemSettings.SettingsItem settingsItem = ImsConstants.SystemSettings.RCS_USER_SETTING1;
        int imsUserSetting = DmConfigHelper.getImsUserSetting(context, settingsItem.getName(), i2);
        Mno simMno = SimUtil.getSimMno(i2);
        int acsVersion = this.mConfigModule.getAcsConfig(i2).getAcsVersion();
        if (i == 0) {
            if (simMno == Mno.SKT && (imsUserSetting == 1 || imsUserSetting == 3)) {
                if (acsVersion == -1 || acsVersion == -2) {
                    IMSLog.e(LOG_TAG, i2, simMno.getName() + ": already turnned off - acs version=" + acsVersion);
                } else {
                    IMSLog.e(LOG_TAG, i2, simMno.getName() + ": treat RCS_DISABLED(0) as RCS_TURNING_OFF(2)");
                    i = 2;
                }
            }
        } else if (i == 2 && simMno != Mno.SKT) {
            IMSLog.e(LOG_TAG, i2, simMno.getName() + ": RCS_TURNING_OFF(2) is not allowed set rcs_user_setting to 0");
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 0, i2);
            return;
        }
        if (imsUserSetting == i) {
            Log.i(LOG_TAG, "same rcs_user_setting not changed : " + i);
            return;
        }
        if (imsUserSetting == -1 && i == 1) {
            IMSLog.i(LOG_TAG, i2, "Reset ACS settings : RCS user switch turned on first time.");
            this.mConfigModule.getAcsConfig(i2).resetAcsSettings();
        }
        updateOpMode(i2, imsUserSetting, i, simMno);
        Log.i(LOG_TAG, "modify internal ImsUserSetting(shared pref) from " + imsUserSetting + " to " + i);
        DmConfigHelper.setImsUserSetting(this.mContext, settingsItem.getName(), i, i2);
        updateRegistrationByRcsUserSettings(i2, i, simMno);
    }

    /* access modifiers changed from: package-private */
    public void onTTYmodeUpdated(int i, boolean z) {
        boolean z2;
        RegisterTask registerTask;
        boolean tTYMode = SlotBasedConfig.getInstance(i).getTTYMode();
        IMSLog.i(LOG_TAG, i, "onTTYmodeUpdated: current=" + tTYMode + " new=" + z);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null && tTYMode != z) {
            SlotBasedConfig.getInstance(i).setTTYMode(Boolean.valueOf(z));
            Iterator it = pendingRegistrationInternal.iterator();
            while (true) {
                if (!it.hasNext()) {
                    z2 = false;
                    registerTask = null;
                    break;
                }
                registerTask = (RegisterTask) it.next();
                if (RegistrationUtils.supportCsTty(registerTask)) {
                    z2 = true;
                    break;
                }
            }
            if (z2) {
                Log.i(LOG_TAG, "onTTYmodeUpdated: isSupportCsTTY=" + z2 + " new=" + z);
                if (z || registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    this.mRegMan.updateRegistration(i, RegistrationConstants.UpdateRegiReason.SETTING_TTYMODE_CHANGE);
                } else {
                    this.mRegMan.tryRegister(i);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005d A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0047  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onRTTmodeUpdated(int r7, boolean r8) {
        /*
            r6 = this;
            com.sec.internal.ims.core.SlotBasedConfig r0 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r7)
            boolean r1 = r0.getRTTMode()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onRTTmodeUpdated: current="
            r2.append(r3)
            r2.append(r1)
            java.lang.String r3 = " new="
            r2.append(r3)
            r2.append(r8)
            java.lang.String r2 = r2.toString()
            java.lang.String r4 = "RegiMgr-UsrEvtCtr"
            android.util.Log.i(r4, r2)
            if (r1 == r8) goto L_0x00c2
            java.lang.Boolean r1 = java.lang.Boolean.valueOf(r8)
            r0.setRTTMode(r1)
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r0 = com.sec.internal.ims.core.RegistrationUtils.getPendingRegistrationInternal(r7)
            if (r0 == 0) goto L_0x00bd
            boolean r1 = r0.isEmpty()
            if (r1 == 0) goto L_0x003d
            goto L_0x00bd
        L_0x003d:
            java.util.Iterator r0 = r0.iterator()
        L_0x0041:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x005d
            java.lang.Object r1 = r0.next()
            com.sec.internal.ims.core.RegisterTask r1 = (com.sec.internal.ims.core.RegisterTask) r1
            com.sec.ims.settings.ImsProfile r2 = r1.getProfile()
            int r2 = r2.getTtyType()
            r5 = 4
            if (r2 == r5) goto L_0x005b
            r5 = 3
            if (r2 != r5) goto L_0x0041
        L_0x005b:
            r0 = 1
            goto L_0x005f
        L_0x005d:
            r0 = 0
            r1 = 0
        L_0x005f:
            if (r0 == 0) goto L_0x00c2
            if (r1 == 0) goto L_0x00c2
            com.sec.internal.constants.Mno r2 = r1.getMno()
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.VZW
            if (r2 == r5) goto L_0x00c2
            com.sec.internal.constants.Mno r2 = r1.getMno()
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.USCC
            if (r2 == r5) goto L_0x00c2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r5 = "onRTTmodeUpdated: isSupportRTT="
            r2.append(r5)
            r2.append(r0)
            r2.append(r3)
            r2.append(r8)
            java.lang.String r0 = r2.toString()
            android.util.Log.i(r4, r0)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "onRTTmodeUpdated: force update "
            r0.append(r2)
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            com.sec.internal.log.IMSLog.i(r4, r7, r0)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r0 = "RTT changed : "
            r7.append(r0)
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            r1.setReason(r7)
            com.sec.internal.ims.core.RegistrationManagerBase r6 = r6.mRegMan
            com.sec.internal.constants.ims.core.RegistrationConstants$UpdateRegiReason r7 = com.sec.internal.constants.ims.core.RegistrationConstants.UpdateRegiReason.SETTING_RTTMODE_CHANGE
            r6.updateRegistration((com.sec.internal.ims.core.RegisterTask) r1, (com.sec.internal.constants.ims.core.RegistrationConstants.UpdateRegiReason) r7)
            goto L_0x00c2
        L_0x00bd:
            java.lang.String r6 = "RegiterTaskList is empty."
            com.sec.internal.log.IMSLog.i(r4, r7, r6)
        L_0x00c2:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.UserEventController.onRTTmodeUpdated(int, boolean):void");
    }

    /* access modifiers changed from: package-private */
    public void onVowifiServiceSettingChanged(int i, IRegistrationHandlerNotifiable iRegistrationHandlerNotifiable) {
        IMSLog.i(LOG_TAG, i, "onVowifiServiceSettingChanged:");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule == null) {
            Log.e(LOG_TAG, "VolteServiceModule is not create yet so retry after 3 seconds");
            iRegistrationHandlerNotifiable.notifyVowifiSettingChanged(i, RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
            return;
        }
        iVolteServiceModule.onVoWiFiSwitched(i);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    int mobileDataNetworkType = this.mPdnController.getNetworkState(i).getMobileDataNetworkType();
                    if (!NetworkUtil.is3gppPsVoiceNetwork(mobileDataNetworkType) || !this.mTelephonyManager.isNetworkRoaming() || this.mRegMan.getNetworkEvent(i).voiceOverPs != VoPsIndication.SUPPORTED) {
                        registerTask.setReason("VoWiFi settings changed");
                        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_VOWIFI_CHANGED);
                    } else {
                        String networkTypeName = TelephonyManagerExt.getNetworkTypeName(mobileDataNetworkType);
                        IMSLog.i(LOG_TAG, i, "Skip updateRegistration under " + networkTypeName + " roaming NW");
                    }
                }
            }
        }
        this.mRegMan.tryRegister(i);
    }

    /* access modifiers changed from: package-private */
    public void onVolteServiceSettingChanged(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "onVolteServiceSettingChanged:" + z);
        ISimManager iSimManager = this.mSimManagers.get(i);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (iSimManager != null && pendingRegistrationInternal != null) {
            if (!iSimManager.isSimLoaded()) {
                IMSLog.i(LOG_TAG, i, "onVolteServiceSettingChanged: SIM is not available don't save setting");
                return;
            }
            Mno simMno = iSimManager.getSimMno();
            DmConfigHelper.setImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), z ^ true ? 1 : 0, i);
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getRegistrationRat() != 18 || !registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) || !this.mRegMan.getNetworkEvent(i).isEpdgConnected) {
                    registerTask.getGovernor().onVolteSettingChanged();
                } else {
                    if (simMno.isOneOf(Mno.ORANGE_POLAND, Mno.TELIA_NORWAY, Mno.TELIA_SWE, Mno.ORANGE)) {
                        Log.i(LOG_TAG, "update eutrn param");
                        registerTask.getGovernor().onVolteSettingChanged();
                    }
                    Log.i(LOG_TAG, "WFC is enabled. Do not modify regi status");
                    return;
                }
            }
            updateRegistrationByVolteServiceSettings(i, z, simMno);
        }
    }

    /* access modifiers changed from: package-private */
    public void onEcVbcSettingChanged(int i) {
        Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                IMSLog.i(LOG_TAG, i, "set reason as EcVbc Setting Changed");
                registerTask.setReason("EcVbc Setting Changed");
                this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_ECVBC_CHANGED, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onUserSwitched() {
        this.mSimpleEventLog.logAndAdd("onUserSwitched by MUM");
        IMSLog.c(LogClass.REGI_USER_SWITCHED, ",USER SWITCHED");
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    this.mRegMan.sendDeregister(1000, registerTask.getPhoneId());
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onShuttingDown(int i) {
        boolean z;
        Log.i(LOG_TAG, "powerOff :" + i);
        if (i != -1) {
            this.mIsDeviceShutdown = true;
        }
        for (int i2 = 0; i2 < SimUtil.getPhoneCount(); i2++) {
            SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i2);
            if (pendingRegistrationInternal != null) {
                Iterator it = pendingRegistrationInternal.iterator();
                while (true) {
                    z = false;
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        RegisterTask registerTask = (RegisterTask) it.next();
                        if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                            registerTask.setDeregiReason(23);
                            z = true;
                        }
                        if (registerTask.getMno().isKor()) {
                        }
                    }
                }
            } else {
                z = false;
            }
            if (z) {
                this.mRegMan.sendDeregister(12, i2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onLteDataNetworkModeSettingChanged(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "onLteDataNetworkModeSettingChanged:");
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                ((RegisterTask) it.next()).getGovernor().onLteDataNetworkModeSettingChanged(z);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onFlightModeChanged(boolean z) {
        if (z) {
            for (int i = 0; i < this.mSimManagers.size(); i++) {
                this.mRegMan.setOmadmState(i, RegistrationManager.OmadmConfigState.IDLE);
                Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
                boolean z2 = false;
                while (it.hasNext()) {
                    RegisterTask registerTask = (RegisterTask) it.next();
                    try {
                        this.mConfigModule.getAcsConfig(i).setForceAcs(true);
                    } catch (NullPointerException unused) {
                        IMSLog.e(LOG_TAG, "ConfigModule - NullPointerException");
                    }
                    registerTask.setReason("FlightMode On");
                    if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                        this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                        registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    } else if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.CONFIGURED)) {
                        if (registerTask.getMno() == Mno.RJIL) {
                            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                        }
                    } else if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        if ((registerTask.getMno().isChn() || registerTask.getMno().isEur()) && RegistrationUtils.isDelayDeRegForNonADSOnFlightModeChanged(registerTask)) {
                            Log.i(LOG_TAG, "QCT , non-ads send de-reg later");
                            this.mRegMan.setNonADSDeRegRequired(true);
                            z2 = false;
                        } else {
                            z2 = true;
                        }
                    }
                    registerTask.mIsUpdateRegistering = false;
                    registerTask.getGovernor().resetPcscfList();
                    registerTask.getGovernor().releaseThrottle(1);
                    registerTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_AIRPLANE_MODE_ON);
                }
                if (z2) {
                    this.mRegMan.sendDeregister(12, i);
                }
            }
        }
        this.mRegMan.onFlightModeChanged(z);
    }

    private void updateOpMode(int i, int i2, int i3, Mno mno) {
        int i4;
        boolean z = false;
        boolean z2 = true;
        boolean z3 = i2 == 1 || i2 == 3;
        boolean z4 = i3 == 1;
        if (z4 == z3) {
            z2 = false;
        }
        if (!mno.isKor() || !(i2 == -2 || i2 == 3)) {
            z = z2;
        } else {
            IMSLog.i(LOG_TAG, i, "Changed rcs_user_setting by network. Skip change op mode.");
        }
        if (z) {
            String readStringParamWithPath = RcsConfigurationHelper.readStringParamWithPath(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.PATH.TC_POPUP_USER_ACCEPT, i));
            if (readStringParamWithPath != null) {
                try {
                    i4 = Integer.parseInt(readStringParamWithPath);
                } catch (NumberFormatException unused) {
                    IMSLog.e(LOG_TAG, i, "Error while parsing integer in getIntValue() - NumberFormatException");
                }
                this.mConfigModule.changeOpMode(z4, i, i4);
            }
            i4 = -1;
            this.mConfigModule.changeOpMode(z4, i, i4);
        }
    }

    private void updateRegistrationByVolteServiceSettings(int i, boolean z, Mno mno) {
        if (mno != Mno.TMOUS && !mno.isKor()) {
            if (z || mno.isOneOf(Mno.VZW, Mno.SPRINT, Mno.ATT)) {
                Log.i(LOG_TAG, "VoLTE switch changed, updateRegistration");
                this.mRegMan.updateRegistration(i, RegistrationConstants.UpdateRegiReason.SETTING_VOLTECALLSTATE_CHANGED);
                return;
            }
            Log.i(LOG_TAG, "VoLTE turned off, DeRegister");
            SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
            if (pendingRegistrationInternal != null) {
                Iterator it = pendingRegistrationInternal.iterator();
                while (it.hasNext()) {
                    RegisterTask registerTask = (RegisterTask) it.next();
                    if (!registerTask.isRcsOnly() && !RegistrationUtils.isCmcProfile(registerTask.getProfile())) {
                        if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                            registerTask.setReason("volte setting turned off");
                            registerTask.setDeregiReason(73);
                            this.mRegMan.tryDeregisterInternal(registerTask, false, false);
                        } else if (mno.isOneOf(Mno.CTC, Mno.CTCMO) || ConfigUtil.isRcsEur(mno) || mno.isOce()) {
                            RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.IDLE;
                            RegistrationConstants.RegisterTaskState registerTaskState2 = RegistrationConstants.RegisterTaskState.CONNECTING;
                            RegistrationConstants.RegisterTaskState registerTaskState3 = RegistrationConstants.RegisterTaskState.CONNECTED;
                            if (registerTask.isOneOf(registerTaskState, registerTaskState2, registerTaskState3)) {
                                this.mRegMan.getImsIconManager(i).updateRegistrationIcon();
                                if (registerTask.isOneOf(registerTaskState2, registerTaskState3)) {
                                    Log.i(LOG_TAG, "VoLTE turned off, no need to keep pdn.");
                                    this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                                    registerTask.setState(registerTaskState);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateRegistrationByRcsUserSettings(int i, int i2, Mno mno) {
        if (mno == Mno.SKT && i2 == 2) {
            Log.i(LOG_TAG, "RCS_TURNING_OFF: Ignore RCS disable for SKT until server responds");
            return;
        }
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (RegistrationUtils.hasRcsService(i, registerTask.getProfile(), this.mPdnController.isWifiConnected())) {
                    if (registerTask.isRcsOnly()) {
                        if (i2 == 1) {
                            this.mRegHandler.sendTryRegister(registerTask.getPhoneId());
                        } else {
                            this.mRegMan.deregister(registerTask, false, true, "RCS USER SWITCH OFF");
                        }
                    } else if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.SETTING_RCSUSERSETTING_CHANGED);
                    } else if (i2 == 1) {
                        this.mRegHandler.sendTryRegister(registerTask.getPhoneId());
                    } else {
                        this.mRegMan.deregister(registerTask, false, true, "RCS USER SWITCH OFF");
                    }
                }
            }
        }
    }
}
