package com.sec.internal.ims.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RegistrationGovernorAtt extends RegistrationGovernorBase {
    protected static final long DEFAULT_TIMS_TIMER_MS = 300000;
    private static final String LOG_TAG = "RegiGvnAtt";
    protected boolean mIsIpmeDisabledBySipForbidden = false;

    public RegistrationGovernorAtt(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        ImsConstants.SystemSettings.setVoiceCallType(context, 0, registerTask.getPhoneId());
    }

    public void onRegistrationTerminated(SipError sipError, long j, boolean z) {
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        eventLog.logAndAdd("onRegistrationTerminated: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j);
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        startRetryTimer(1000);
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (j < 0) {
            j = 0;
        }
        if (!this.mTask.getProfile().hasEmergencySupport() || !SipErrorBase.SIP_TIMEOUT.equals(sipError) || this.mTask.getProfile().getE911RegiTime() <= 0) {
            if (SipErrorBase.USE_PROXY.equals(sipError)) {
                int i = this.mCurPcscfIpIdx;
                Log.i(LOG_TAG, "usedPcscf : " + i);
                this.mCurPcscfIpIdx = i > 0 ? 0 : 1;
            } else if (!z) {
                this.mCurPcscfIpIdx++;
            }
            if (this.mCurPcscfIpIdx >= this.mNumOfPcscfIp) {
                this.mFailureCounter++;
                this.mCurPcscfIpIdx = 0;
                if (j == 0) {
                    j = getWaitTime();
                }
            }
            if (j > 0) {
                startRetryTimer(j);
            } else {
                this.mRegHandler.sendTryRegister(this.mPhoneId, 1000);
            }
        } else {
            this.mFailureCounter++;
            this.mCurPcscfIpIdx++;
            handleTimeOutEmerRegiError();
        }
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        stopTimsEstablishTimer(this.mTask, RegistrationConstants.REASON_REGISTERED);
        if (this.mIsIpmeDisabledBySipForbidden) {
            this.mIsIpmeDisabledBySipForbidden = false;
            Log.i(LOG_TAG, "onRegistrationDone: reset IPME after forbidden");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:79:0x0238  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x024a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.Set<java.lang.String> filterService(java.util.Set<java.lang.String> r17, int r18) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            java.util.HashSet r2 = new java.util.HashSet
            r2.<init>()
            java.util.HashSet r3 = new java.util.HashSet
            r3.<init>()
            if (r1 == 0) goto L_0x0013
            r3.addAll(r1)
        L_0x0013:
            boolean r1 = r16.isImsDisabled()
            if (r1 == 0) goto L_0x001f
            java.util.HashSet r0 = new java.util.HashSet
            r0.<init>()
            return r0
        L_0x001f:
            android.content.Context r1 = r0.mContext
            java.lang.String r4 = "volte"
            int r5 = r0.mPhoneId
            int r1 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r1, (java.lang.String) r4, (int) r5)
            r5 = 1
            if (r1 != r5) goto L_0x002f
            r1 = r5
            goto L_0x0030
        L_0x002f:
            r1 = 0
        L_0x0030:
            android.content.Context r6 = r0.mContext
            java.lang.String r7 = "rcs"
            int r8 = r0.mPhoneId
            int r6 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r6, (java.lang.String) r7, (int) r8)
            if (r6 != r5) goto L_0x003f
            r6 = r5
            goto L_0x0040
        L_0x003f:
            r6 = 0
        L_0x0040:
            android.content.Context r7 = r0.mContext
            int r8 = r0.mPhoneId
            r9 = -1
            int r7 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getRcsUserSetting(r7, r9, r8)
            if (r7 != r5) goto L_0x004d
            r7 = r5
            goto L_0x004e
        L_0x004d:
            r7 = 0
        L_0x004e:
            android.content.Context r8 = r0.mContext
            boolean r8 = com.sec.internal.helper.NetworkUtil.isMobileDataOn(r8)
            com.sec.internal.helper.os.ITelephonyManager r10 = r0.mTelephonyManager
            boolean r10 = r10.isNetworkRoaming()
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r11 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.DATA_ROAMING
            android.content.Context r12 = r0.mContext
            int r13 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN
            int r11 = r11.get(r12, r13)
            int r12 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.ROAMING_DATA_ENABLED
            if (r11 != r12) goto L_0x006a
            r11 = r5
            goto L_0x006b
        L_0x006a:
            r11 = 0
        L_0x006b:
            android.content.Context r12 = r0.mContext
            int r13 = r0.mPhoneId
            int r12 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getVoiceCallType(r12, r9, r13)
            if (r12 != 0) goto L_0x0077
            r12 = r5
            goto L_0x0078
        L_0x0077:
            r12 = 0
        L_0x0078:
            android.content.Context r13 = r0.mContext
            int r14 = r0.mPhoneId
            int r9 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getVideoCallType(r13, r9, r14)
            if (r9 != 0) goto L_0x0084
            r9 = r5
            goto L_0x0085
        L_0x0084:
            r9 = 0
        L_0x0085:
            android.content.Context r13 = r0.mContext
            java.lang.String r14 = "defaultmsgappinuse"
            int r15 = r0.mPhoneId
            int r13 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r13, (java.lang.String) r14, (int) r15)
            if (r13 != r5) goto L_0x0093
            r13 = r5
            goto L_0x0094
        L_0x0093:
            r13 = 0
        L_0x0094:
            int r14 = r0.mPhoneId
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r4 = "filterService:  IPME setting="
            r15.append(r4)
            r15.append(r7)
            java.lang.String r4 = " Video setting="
            r15.append(r4)
            r15.append(r9)
            java.lang.String r4 = " Enhanced Data="
            r15.append(r4)
            r15.append(r12)
            java.lang.String r4 = " Mobile Data="
            r15.append(r4)
            r15.append(r8)
            java.lang.String r4 = " isRoaming="
            r15.append(r4)
            r15.append(r10)
            java.lang.String r4 = " Roaming Data="
            r15.append(r4)
            r15.append(r11)
            java.lang.String r4 = " SIP Forbidden="
            r15.append(r4)
            boolean r4 = r0.mIsIpmeDisabledBySipForbidden
            r15.append(r4)
            java.lang.String r4 = " Default Msg App="
            r15.append(r4)
            r15.append(r13)
            java.lang.String r4 = " RCSonly="
            r15.append(r4)
            com.sec.internal.ims.core.RegisterTask r4 = r0.mTask
            boolean r4 = r4.isRcsOnly()
            r15.append(r4)
            java.lang.String r4 = r15.toString()
            java.lang.String r15 = "RegiGvnAtt"
            com.sec.internal.log.IMSLog.i(r15, r14, r4)
            java.lang.String r4 = "mmtel"
            java.lang.String r14 = "mmtel-video"
            if (r12 == 0) goto L_0x025c
            r2.add(r4)
            java.lang.String r12 = "smsip"
            r2.add(r12)
            int r12 = r0.mPhoneId
            boolean r12 = com.sec.internal.ims.util.ImsUtil.isDualVideoCallAllowed(r12)
            if (r12 != 0) goto L_0x0110
            java.lang.String r12 = "Non-ADS operator SIM"
            r0.removeService(r3, r14, r12)
        L_0x0110:
            if (r10 == 0) goto L_0x0114
            if (r11 != 0) goto L_0x0118
        L_0x0114:
            if (r10 != 0) goto L_0x0124
            if (r8 == 0) goto L_0x0124
        L_0x0118:
            if (r9 == 0) goto L_0x011e
            r2.add(r14)
            goto L_0x0129
        L_0x011e:
            java.lang.String r8 = "VideoSetting off"
            r0.removeService(r3, r14, r8)
            goto L_0x0129
        L_0x0124:
            java.lang.String r8 = "MobileData unavailable"
            r0.removeService(r3, r14, r8)
        L_0x0129:
            if (r6 == 0) goto L_0x0221
            if (r13 != 0) goto L_0x01b3
            int r6 = r0.mPhoneId
            boolean r6 = com.sec.internal.ims.util.ImsUtil.isSingleRegiAppConnected(r6)
            if (r6 == 0) goto L_0x01b3
            com.sec.internal.interfaces.ims.config.IConfigModule r6 = r0.mConfigModule
            int r7 = r0.mPhoneId
            java.lang.Integer r6 = r6.getRcsConfVersion(r7)
            if (r6 == 0) goto L_0x0221
            int r6 = r6.intValue()
            if (r6 <= 0) goto L_0x0221
            java.lang.String[] r6 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            java.util.Set r6 = r0.servicesByImsSwitch(r6)
            r2.addAll(r6)
            android.content.Context r6 = r0.mContext
            int r7 = r0.mPhoneId
            com.sec.internal.ims.core.RegisterTask r8 = r0.mTask
            com.sec.ims.settings.ImsProfile r8 = r8.getProfile()
            java.lang.String r8 = com.sec.internal.ims.util.ConfigUtil.getRcsProfileWithFeature(r6, r7, r8)
            java.util.List r6 = com.sec.internal.helper.RcsConfigurationHelper.getRcsEnabledServiceList(r6, r7, r8)
            java.lang.String[] r7 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            java.util.stream.Stream r7 = java.util.Arrays.stream(r7)
            com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda0 r8 = new com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda0
            r8.<init>(r6)
            java.util.stream.Stream r6 = r7.filter(r8)
            com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda1 r7 = new com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda1
            r7.<init>(r0, r2)
            r6.forEach(r7)
            com.sec.internal.google.SecImsNotifier r6 = com.sec.internal.google.SecImsNotifier.getInstance()
            int r7 = r0.mPhoneId
            java.util.List r6 = r6.getSipDelegateServiceList(r7)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "filterService: allocated services via SRAPI: "
            r7.append(r8)
            r7.append(r6)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.i(r15, r7)
            java.lang.String[] r7 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            java.util.stream.Stream r7 = java.util.Arrays.stream(r7)
            com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda2 r8 = new com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda2
            r8.<init>(r6)
            java.util.stream.Stream r6 = r7.filter(r8)
            com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda3 r7 = new com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda3
            r7.<init>(r0, r3)
            r6.forEach(r7)
            goto L_0x0221
        L_0x01b3:
            com.sec.internal.interfaces.ims.config.IConfigModule r6 = r0.mConfigModule
            int r8 = r0.mPhoneId
            boolean r6 = r6.isValidAcsVersion(r8)
            if (r6 == 0) goto L_0x0221
            java.lang.String[] r6 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            java.util.Set r6 = r0.servicesByImsSwitch(r6)
            r2.addAll(r6)
            android.content.Context r6 = r0.mContext
            int r8 = r0.mPhoneId
            com.sec.internal.ims.core.RegisterTask r9 = r0.mTask
            com.sec.ims.settings.ImsProfile r9 = r9.getProfile()
            java.lang.String r6 = com.sec.internal.ims.util.ConfigUtil.getRcsProfileWithFeature(r6, r8, r9)
            android.content.Context r8 = r0.mContext
            int r9 = r0.mPhoneId
            java.util.List r8 = com.sec.internal.helper.RcsConfigurationHelper.getRcsEnabledServiceList(r8, r9, r6)
            java.lang.String[] r9 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            java.util.stream.Stream r9 = java.util.Arrays.stream(r9)
            com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda4 r11 = new com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda4
            r11.<init>(r8)
            java.util.stream.Stream r8 = r9.filter(r11)
            com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda5 r9 = new com.sec.internal.ims.core.RegistrationGovernorAtt$$ExternalSyntheticLambda5
            r9.<init>(r0, r2)
            r8.forEach(r9)
            java.lang.String r8 = "im"
            boolean r8 = r2.contains(r8)
            if (r8 != 0) goto L_0x0206
            java.lang.String r8 = "chatbot-communication"
            java.lang.String r9 = "CHAT disabled in autoconfig"
            r0.removeService(r2, r8, r9)
        L_0x0206:
            java.lang.String r8 = "gls"
            boolean r9 = r2.contains(r8)
            if (r9 == 0) goto L_0x021b
            int r9 = r0.mPhoneId
            boolean r6 = com.sec.internal.helper.RcsConfigurationHelper.isUp2NonTransitional(r6, r9)
            if (r6 != 0) goto L_0x021b
            java.lang.String r6 = "Disabled for non UP"
            r0.removeService(r2, r8, r6)
        L_0x021b:
            if (r7 == 0) goto L_0x021f
            if (r13 != 0) goto L_0x0221
        L_0x021f:
            r6 = r5
            goto L_0x0222
        L_0x0221:
            r6 = 0
        L_0x0222:
            if (r6 != 0) goto L_0x0230
            boolean r6 = r0.mIsIpmeDisabledBySipForbidden
            if (r6 != 0) goto L_0x0230
            if (r10 == 0) goto L_0x026a
            r6 = 18
            r7 = r18
            if (r7 == r6) goto L_0x026a
        L_0x0230:
            com.sec.internal.ims.core.RegisterTask r6 = r0.mTask
            boolean r6 = r6.isRcsOnly()
            if (r6 == 0) goto L_0x024a
            java.lang.String[] r6 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            int r7 = r6.length
            r8 = 0
        L_0x023e:
            if (r8 >= r7) goto L_0x026a
            r9 = r6[r8]
            java.lang.String r10 = "RCS service off"
            r0.removeService(r3, r9, r10)
            int r8 = r8 + 1
            goto L_0x023e
        L_0x024a:
            java.lang.String[] r6 = com.sec.ims.settings.ImsProfile.getChatServiceList()
            int r7 = r6.length
            r8 = 0
        L_0x0250:
            if (r8 >= r7) goto L_0x026a
            r9 = r6[r8]
            java.lang.String r10 = "IMPE service off"
            r0.removeService(r3, r9, r10)
            int r8 = r8 + 1
            goto L_0x0250
        L_0x025c:
            r3.clear()
            com.sec.internal.ims.core.RegisterTask r6 = r0.mTask
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r7 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF
            int r7 = r7.getCode()
            r6.setRegiFailReason(r7)
        L_0x026a:
            if (r1 != 0) goto L_0x028f
            java.lang.String[] r1 = com.sec.ims.settings.ImsProfile.getVoLteServiceList()
            int r5 = r1.length
            r6 = 0
        L_0x0272:
            if (r6 >= r5) goto L_0x02aa
            r7 = r1[r6]
            java.lang.String r8 = "VoLTE disabled"
            r0.removeService(r3, r7, r8)
            boolean r7 = r7.equalsIgnoreCase(r4)
            if (r7 == 0) goto L_0x028c
            com.sec.internal.ims.core.RegisterTask r7 = r0.mTask
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r8 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF
            int r8 = r8.getCode()
            r7.setRegiFailReason(r8)
        L_0x028c:
            int r6 = r6 + 1
            goto L_0x0272
        L_0x028f:
            android.content.Context r1 = r0.mContext
            int r4 = r0.mPhoneId
            int r1 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r1, (java.lang.String) r14, (int) r4)
            if (r1 != r5) goto L_0x02a5
            int r1 = r0.mPhoneId
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r1)
            boolean r1 = r1.isSimMobilityActivated()
            if (r1 == 0) goto L_0x02aa
        L_0x02a5:
            java.lang.String r1 = "ViLTE disabled"
            r0.removeService(r3, r14, r1)
        L_0x02aa:
            com.sec.internal.interfaces.ims.config.IConfigModule r1 = r0.mConfigModule
            int r4 = r0.mPhoneId
            boolean r1 = r1.isValidAcsVersion(r4)
            if (r1 != 0) goto L_0x02c6
            java.lang.String[] r1 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            int r4 = r1.length
            r5 = 0
        L_0x02ba:
            if (r5 >= r4) goto L_0x02c6
            r6 = r1[r5]
            java.lang.String r7 = "Invalid autoconf ver"
            r0.removeService(r3, r6, r7)
            int r5 = r5 + 1
            goto L_0x02ba
        L_0x02c6:
            android.content.Context r1 = r0.mContext
            int r4 = r0.mPhoneId
            boolean r1 = com.sec.internal.ims.rcs.util.RcsUtils.DualRcs.isRegAllowed(r1, r4)
            if (r1 != 0) goto L_0x02e2
            java.lang.String[] r1 = com.sec.ims.settings.ImsProfile.getRcsServiceList()
            int r4 = r1.length
            r5 = 0
        L_0x02d6:
            if (r5 >= r4) goto L_0x02e2
            r6 = r1[r5]
            java.lang.String r7 = "No DualRCS"
            r0.removeService(r3, r6, r7)
            int r5 = r5 + 1
            goto L_0x02d6
        L_0x02e2:
            r3.retainAll(r2)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorAtt.filterService(java.util.Set, int):java.util.Set");
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$filterService$0(List list, String str) {
        return !list.contains(str);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$filterService$1(Set set, String str) {
        removeService(set, str, "Disable from ACS.");
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$filterService$2(List list, String str) {
        return !list.contains(str);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$filterService$3(Set set, String str) {
        removeService(set, str, "Disable from singleregi");
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$filterService$4(List list, String str) {
        return !list.contains(str);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$filterService$5(Set set, String str) {
        removeService(set, str, "Disable from ACS.");
    }

    public SipError onSipError(String str, SipError sipError) {
        Log.i(LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        this.mIsValid = this.mNumOfPcscfIp > 0;
        if ("mmtel".equals(str)) {
            if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(sipError) || SipErrorBase.SIP_TIMEOUT.equals(sipError) || SipErrorBase.FORBIDDEN.equals(sipError) || SipErrorBase.SERVER_TIMEOUT.equals(sipError)) {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, true, this.mIsValid, "Sip Error[MMTEL]. DeRegister..");
            }
        } else if (("im".equals(str) || "ft".equals(str)) && ((ImsUtil.isSingleRegiAppConnected(this.mPhoneId) && sipError.getCode() == 403) || (SipErrorBase.FORBIDDEN_SERVICE_NOT_AUTHORISED.equals(sipError) && ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == 1))) {
            Log.i(LOG_TAG, "onSipError: [IPME] try re-register after forbidden");
            this.mIsIpmeDisabledBySipForbidden = true;
            this.mRegMan.updateChatService(this.mPhoneId, 1);
        }
        return sipError;
    }

    public boolean allowRoaming() {
        return this.mTask.getProfile().isAllowedOnRoaming() && getVoiceTechType() == 0;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        int registrationRat = this.mTask.getRegistrationRat();
        if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0) {
            return true;
        }
        if ((registrationRat == 10 || registrationRat == 3) && !SlotBasedConfig.getInstance(this.mPhoneId).getTTYMode()) {
            return true;
        }
        return false;
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkCallStatus() && checkRegiStatus());
    }

    public void releaseThrottle(int i) {
        if (i == 0) {
            this.mIsPermanentStopped = false;
        } else if (i == 1 || i == 9) {
            this.mIsPermanentStopped = false;
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            stopRetryTimer();
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + i);
        }
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        super.onPdnRequestFailed(pdnFailReason, i);
        if (isMatchedPdnFailReason(pdnFailReason) && this.mTask.getRegistrationRat() != 18) {
            if (!DeviceUtil.isApAssistedMode() || i == 1) {
                Log.i(LOG_TAG, "send ImsNotAvailable");
                this.mIsPermanentStopped = true;
                this.mRegMan.notifyImsNotAvailable(this.mTask, true);
            }
        }
    }

    public void updatePcscfIpList(List<String> list) {
        if (list == null) {
            Log.e(LOG_TAG, "updatePcscfIpList: null P-CSCF list!");
            return;
        }
        int size = list.size();
        this.mNumOfPcscfIp = size;
        this.mPcscfIpList = list;
        boolean z = false;
        this.mCurPcscfIpIdx = 0;
        if (size > 0) {
            z = true;
        }
        this.mIsValid = z;
    }

    public boolean isLocationInfoLoaded(int i) {
        if (i != 18 || this.mTask.getProfile().getSupportedGeolocationPhase() < 2 || !this.mRegMan.isVoWiFiSupported(this.mPhoneId)) {
            return true;
        }
        IMSLog.e(LOG_TAG, this.mPhoneId, "update geo location");
        Optional.ofNullable(ImsRegistry.getGeolocationController()).ifPresent(new RegistrationGovernorAtt$$ExternalSyntheticLambda6(this));
        return true;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$isLocationInfoLoaded$6(IGeolocationController iGeolocationController) {
        iGeolocationController.startGeolocationUpdate(this.mPhoneId, false);
    }

    public void onPdnConnecting(int i) {
        toggleTimsTimerByPdnTransport(i);
    }

    public void startTimsTimer(String str) {
        startTimsEstablishTimer(this.mTask, DEFAULT_TIMS_TIMER_MS, str);
    }

    public void stopTimsTimer(String str) {
        stopTimsEstablishTimer(this.mTask, str);
    }

    public String getUpdateRegiPendingReason(int i, NetworkEvent networkEvent, boolean z, boolean z2) {
        String updateRegiPendingReason = super.getUpdateRegiPendingReason(i, networkEvent, z, z2);
        return (z2 || z || !TextUtils.isEmpty(updateRegiPendingReason) || !this.mRegHandler.hasMessages(139, Integer.valueOf(this.mPhoneId))) ? updateRegiPendingReason : "Ignore by postponed update registration event by dma change";
    }
}
