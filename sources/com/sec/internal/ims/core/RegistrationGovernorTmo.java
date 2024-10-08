package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorTmoUs;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class RegistrationGovernorTmo extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnTmo";
    static final int WFC_STATUS_OFF = 2;
    static final int WFC_STATUS_ON = 1;
    protected boolean mAllPcscfFailed = false;
    protected boolean mHasPendingDeregistration = false;
    protected String mLastKnownCountryIso = "";
    protected byte mWfcPrefMode = 0;
    protected byte mWfcStatus = 0;

    /* access modifiers changed from: protected */
    public Set<String> applyInboundRoamingPolicy(Set<String> set, ISimManager iSimManager) {
        return set;
    }

    public RegistrationGovernorTmo(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        this.mNeedToCheckSrvcc = true;
        updateEutranValues();
    }

    public void onWfcProfileChanged(byte[] bArr) {
        this.mWfcPrefMode = bArr[4];
        this.mWfcStatus = bArr[5];
        Log.i(LOG_TAG, "[WFC] PrefMode = " + this.mWfcPrefMode + ", Status = " + this.mWfcStatus);
        if (this.mWfcStatus == 2) {
            Log.i(LOG_TAG, "WFC switch has turned off. Release throttle.");
            releaseThrottle(3);
        }
    }

    public void onVolteSettingChanged() {
        updateEutranValues();
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        this.mAllPcscfFailed = false;
    }

    public void onDeregistrationDone(boolean z) {
        this.mHasPendingDeregistration = false;
    }

    public void onRegistrationTerminated(SipError sipError, long j, boolean z) {
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        eventLog.logAndAdd("onRegistrationTerminated: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j);
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mRegiAt = 0;
        if (SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(sipError) && this.mTask.getRegistrationRat() == 18) {
            int i = this.mWFCSubscribeForbiddenCounter + 1;
            this.mWFCSubscribeForbiddenCounter = i;
            if (i <= 2) {
                if (j == 0) {
                    j = getWaitTime(i);
                }
                startRetryTimer(j);
            }
        } else if (j > 0) {
            startRetryTimer(j);
        } else {
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003b, code lost:
        if (r0 >= 2) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001f, code lost:
        if (r8 != 20) goto L_0x003e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0041  */
    /* JADX WARNING: Removed duplicated region for block: B:21:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onPdnRequestFailed(com.sec.internal.constants.ims.core.PdnFailReason r7, int r8) {
        /*
            r6 = this;
            super.onPdnRequestFailed(r7, r8)
            boolean r0 = com.sec.internal.helper.os.DeviceUtil.isApAssistedMode()
            r1 = 1
            if (r0 == 0) goto L_0x000c
            if (r8 != r1) goto L_0x0076
        L_0x000c:
            com.sec.internal.ims.core.RegisterTask r8 = r6.mTask
            int r8 = r8.getRegistrationRat()
            boolean r0 = r6.isMatchedPdnFailReason(r7)
            r2 = 0
            if (r0 == 0) goto L_0x0023
            com.sec.internal.constants.ims.core.PdnFailReason r0 = com.sec.internal.constants.ims.core.PdnFailReason.PDN_MAX_TIMEOUT
            if (r7 != r0) goto L_0x0021
            r0 = 20
            if (r8 != r0) goto L_0x003e
        L_0x0021:
            r0 = r1
            goto L_0x003f
        L_0x0023:
            r0 = 13
            if (r8 != r0) goto L_0x003e
            com.sec.internal.ims.core.RegistrationManagerInternal r0 = r6.mRegMan
            com.sec.internal.ims.core.RegisterTask r3 = r6.mTask
            int r3 = r3.getPhoneId()
            boolean r0 = r0.getCsfbSupported(r3)
            if (r0 != 0) goto L_0x003e
            int r0 = r6.mPdnRejectCounter
            int r0 = r0 + r1
            r6.mPdnRejectCounter = r0
            r3 = 2
            if (r0 < r3) goto L_0x003e
            goto L_0x0021
        L_0x003e:
            r0 = r2
        L_0x003f:
            if (r0 == 0) goto L_0x0076
            int r0 = r6.mPhoneId
            com.sec.internal.ims.core.RegisterTask r3 = r6.mTask
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "notifyImsNotAvailable. reason: "
            r4.append(r5)
            r4.append(r7)
            java.lang.String r7 = "Registration RAT: "
            r4.append(r7)
            r4.append(r8)
            java.lang.String r7 = "PDN Reject counter: "
            r4.append(r7)
            com.sec.internal.ims.core.PdnController r7 = r6.mPdnController
            r4.append(r7)
            java.lang.String r7 = r4.toString()
            java.lang.String r8 = "RegiGvnTmo"
            com.sec.internal.log.IMSLog.i(r8, r0, r3, r7)
            com.sec.internal.ims.core.RegistrationManagerInternal r7 = r6.mRegMan
            com.sec.internal.ims.core.RegisterTask r8 = r6.mTask
            r7.notifyImsNotAvailable(r8, r1)
            r6.mPdnRejectCounter = r2
        L_0x0076:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorTmo.onPdnRequestFailed(com.sec.internal.constants.ims.core.PdnFailReason, int):void");
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " unsolicit " + z);
        if (j < 0) {
            j = 0;
        }
        if (SipErrorBase.MISSING_P_ASSOCIATED_URI.equals(sipError)) {
            this.mTask.setKeepPdn(true);
        } else if (SipErrorBase.EMPTY_PCSCF.equals(sipError)) {
            this.mFailureCounter++;
            handlePcscfError();
            return;
        } else if (!SipErrorTmoUs.isCountryBlockingForbidden(sipError) || this.mTask.getRegistrationRat() != 18) {
            this.mFailureCounter++;
            if (!z) {
                this.mCurPcscfIpIdx++;
            }
        } else {
            this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, this.mTask, "onRegistrationError: Country blocking forbidden.");
            this.mTask.setDeregiReason(53);
            if (j == 0) {
                j = 10000;
            }
        }
        if (this.mCurPcscfIpIdx >= this.mNumOfPcscfIp && !this.mRegMan.getCsfbSupported(this.mPhoneId)) {
            this.mAllPcscfFailed = true;
        }
        handleRetryTimer(j);
    }

    public void onPublishError(SipError sipError) {
        Log.e(LOG_TAG, "onPublishError: state " + this.mTask.getState() + " error " + sipError);
        if (SipErrorBase.FORBIDDEN.equals(sipError)) {
            this.mTask.setReason("Publish Error. ReRegister..");
            this.mRegMan.sendReRegister(this.mTask);
        }
    }

    /* access modifiers changed from: protected */
    public long getWaitTime(int i) {
        long pow = this.mRegBaseTimeMs * ((long) Math.pow(2.0d, (double) (i - 1)));
        if (pow < 0) {
            return this.mRegMaxTimeMs;
        }
        return Math.min(this.mRegMaxTimeMs, pow);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x008e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onSubscribeError(int r5, com.sec.ims.util.SipError r6) {
        /*
            r4 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "onSubscribeError: state "
            r0.append(r1)
            com.sec.internal.ims.core.RegisterTask r1 = r4.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = r1.getState()
            r0.append(r1)
            java.lang.String r1 = ", error "
            r0.append(r1)
            r0.append(r6)
            java.lang.String r1 = ", event "
            r0.append(r1)
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "RegiGvnTmo"
            android.util.Log.e(r1, r0)
            if (r5 != 0) goto L_0x00aa
            com.sec.ims.util.SipError r5 = com.sec.internal.constants.ims.SipErrorBase.OK
            boolean r5 = r5.equals(r6)
            r0 = 0
            if (r5 == 0) goto L_0x003a
            r4.mSubscribeForbiddenCounter = r0
            return
        L_0x003a:
            com.sec.ims.util.SipError r5 = com.sec.internal.constants.ims.SipErrorBase.FORBIDDEN
            boolean r5 = r5.equals(r6)
            r1 = 1
            if (r5 == 0) goto L_0x005b
            com.sec.internal.ims.core.RegisterTask r5 = r4.mTask
            int r5 = r5.getRegistrationRat()
            r2 = 18
            if (r5 != r2) goto L_0x005b
            int r5 = r4.mWFCSubscribeForbiddenCounter
            int r5 = r5 + r1
            r4.mWFCSubscribeForbiddenCounter = r5
            r6 = 2
            if (r5 <= r6) goto L_0x0058
            r5 = r0
            r0 = r1
            goto L_0x008c
        L_0x0058:
            r5 = r1
            r0 = r5
            goto L_0x008c
        L_0x005b:
            com.sec.ims.util.SipError r5 = com.sec.internal.constants.ims.SipErrorBase.BAD_EXTENSION
            boolean r5 = r5.equals(r6)
            if (r5 != 0) goto L_0x007c
            com.sec.ims.util.SipError r5 = com.sec.internal.constants.ims.SipErrorBase.EXTENSION_REQUIRED
            boolean r5 = r5.equals(r6)
            if (r5 != 0) goto L_0x007c
            com.sec.ims.util.SipError r5 = com.sec.internal.constants.ims.SipErrorBase.SESSION_INTERVAL_TOO_SMALL
            boolean r5 = r5.equals(r6)
            if (r5 == 0) goto L_0x0074
            goto L_0x007c
        L_0x0074:
            r4.mWFCSubscribeForbiddenCounter = r0
            int r5 = r4.mSubscribeForbiddenCounter
            int r5 = r5 + r1
            r4.mSubscribeForbiddenCounter = r5
            goto L_0x0058
        L_0x007c:
            int r5 = r4.mFailureCounter
            int r5 = r5 + r1
            r4.mFailureCounter = r5
            int r5 = r4.mCurPcscfIpIdx
            int r5 = r5 + r1
            r4.mCurPcscfIpIdx = r5
            r5 = 0
            r4.handleRetryTimer(r5)
            r5 = r1
        L_0x008c:
            if (r0 == 0) goto L_0x009a
            int r6 = r4.mSubscribeForbiddenCounter
            int r0 = r4.mWFCSubscribeForbiddenCounter
            int r6 = r6 + r0
            long r2 = r4.getWaitTime(r6)
            r4.startRetryTimer(r2)
        L_0x009a:
            com.sec.internal.ims.core.RegisterTask r6 = r4.mTask
            r0 = 44
            r6.setDeregiReason(r0)
            com.sec.internal.ims.core.RegistrationManagerInternal r6 = r4.mRegMan
            com.sec.internal.ims.core.RegisterTask r4 = r4.mTask
            java.lang.String r0 = "Subscribe Error. Deregister.."
            r6.deregister(r4, r1, r5, r0)
        L_0x00aa:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorTmo.onSubscribeError(int, com.sec.ims.util.SipError):void");
    }

    public void onTelephonyCallStatusChanged(int i) {
        if (i == 0 && this.mTask.getProfile().hasEmergencySupport()) {
            this.mTask.setDeregiReason(7);
            this.mRegMan.deregister(this.mTask, true, false, "Call status changed. Deregister..");
        }
    }

    private boolean isDataAllowed() {
        boolean z = Settings.Global.getInt(this.mContext.getContentResolver(), "data_roaming", 0) == 1;
        boolean isNetworkRoaming = this.mTelephonyManager.isNetworkRoaming();
        if ((isNetworkRoaming || !NetworkUtil.isMobileDataOn(this.mContext)) && (!isNetworkRoaming || !z)) {
            return false;
        }
        return true;
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
        boolean isImsSwitchEnabled = DmConfigHelper.isImsSwitchEnabled(this.mContext, "volte", this.mPhoneId);
        boolean isImsSwitchEnabled2 = DmConfigHelper.isImsSwitchEnabled(this.mContext, DeviceConfigManager.RCS, this.mPhoneId);
        boolean z = false;
        boolean z2 = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == 1;
        boolean isDataAllowed = isDataAllowed();
        boolean isImsSwitchEnabled3 = DmConfigHelper.isImsSwitchEnabled(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId);
        IMSLog.i(LOG_TAG, this.mPhoneId, "VOLTE: " + isImsSwitchEnabled + ", RCS: " + isImsSwitchEnabled2 + ", rcs_user_setting: " + z2 + ", Data allowed: " + isDataAllowed + ", Default MSG app: " + isImsSwitchEnabled3);
        if (!RegistrationUtils.supportCsTty(this.mTask) || !SlotBasedConfig.getInstance(this.mPhoneId).getTTYMode()) {
            if (i == 13 || i == 20) {
                if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.SUPPORTED) {
                    if (this.mTask.getProfile().getPdn().equals("internet")) {
                        Log.i(LOG_TAG, "VoPS Supported. Registration over IMS pdn.");
                        return new HashSet();
                    }
                } else if (this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
                    if (!hasRcsSession()) {
                        Log.i(LOG_TAG, "VoPS NOT Supported. Registration over Internet PDN.");
                        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                        return new HashSet();
                    }
                    Log.i(LOG_TAG, "VoPS NOT Supported. But, there are rcs sessions");
                }
            }
            if (isImsSwitchEnabled) {
                hashSet2.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
                if (!hashSet2.contains("mmtel")) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
                }
            }
            if (!ImsUtil.isDualVideoCallAllowed(this.mPhoneId)) {
                removeService(hashSet, "mmtel-video", "Non-DDS operator SIM");
            }
            if (!isDataAllowed && i != 18) {
                removeService(hashSet, "mmtel-video", "MobileData OFF");
            }
            if (this.mConfigModule.isValidAcsVersion(this.mPhoneId) && isImsSwitchEnabled2) {
                hashSet2.addAll(servicesByImsSwitch(ImsProfile.getRcsServiceList()));
                Context context = this.mContext;
                int i2 = this.mPhoneId;
                Arrays.stream(ImsProfile.getRcsServiceList()).filter(new RegistrationGovernorTmo$$ExternalSyntheticLambda2(RcsConfigurationHelper.getRcsEnabledServiceList(context, i2, ConfigUtil.getRcsProfileWithFeature(context, i2, this.mTask.getProfile())))).forEach(new RegistrationGovernorTmo$$ExternalSyntheticLambda3(this, hashSet));
                if (RcsConfigurationHelper.getConfigData(this.mContext, "root/application/1/services/IR94VideoAuth", this.mPhoneId).readInt(ConfigConstants.ConfigTable.SERVICES_IR94_VIDEO_AUTH, -1).intValue() == 0) {
                    removeService(hashSet, "mmtel-video", "ir94videoauth disabled");
                }
                if (isImsSwitchEnabled3 && SlotBasedConfig.getInstance(this.mPhoneId).isSimMobilityActivated()) {
                    for (String str : ImsProfile.getRcsServiceList()) {
                        if (!SipMsg.EVENT_PRESENCE.equals(str)) {
                            removeService(hashSet, str, "SM SimMobility");
                        }
                    }
                }
                if (!isImsSwitchEnabled3 && ImsUtil.isSingleRegiAppConnected(this.mPhoneId)) {
                    Arrays.stream(ImsProfile.getRcsServiceList()).filter(new RegistrationGovernorTmo$$ExternalSyntheticLambda4(SecImsNotifier.getInstance().getSipDelegateServiceList(this.mPhoneId))).forEach(new RegistrationGovernorTmo$$ExternalSyntheticLambda5(this, hashSet));
                } else if (!z2 || !isImsSwitchEnabled3) {
                    if (this.mTask.isRcsOnly()) {
                        for (String removeService : ImsProfile.getRcsServiceList()) {
                            removeService(hashSet, removeService, "RCS service off");
                        }
                    } else {
                        for (String removeService2 : ImsProfile.getChatServiceList()) {
                            removeService(hashSet, removeService2, "chatservice off");
                        }
                    }
                }
            }
            if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, this.mPhoneId)) {
                for (String removeService3 : ImsProfile.getRcsServiceList()) {
                    removeService(hashSet, removeService3, "No DualRcs");
                }
            }
            if (!isImsSwitchEnabled || !isImsSwitchEnabled2) {
                hashSet2.remove("mmtel-call-composer");
            } else {
                int composerAuthValue = ImsUtil.getComposerAuthValue(this.mPhoneId, this.mContext);
                int vBCAuthValue = ImsUtil.getVBCAuthValue(this.mPhoneId, this.mContext);
                boolean z3 = ImsConstants.SystemSettings.ENRICHED_CALL_VBC.get(this.mContext, 1) == 1;
                Log.i(LOG_TAG, "composerAuthVal : " + composerAuthValue + "vbcAuthVal : " + vBCAuthValue + "vbcSettings:" + z3);
                boolean z4 = composerAuthValue == 2 || composerAuthValue == 3;
                if (vBCAuthValue == 1) {
                    z = true;
                }
                if (!z4 && (!z || !z3)) {
                    removeService(hashSet, "mmtel-call-composer", "MMTEL Composer off from ACS");
                }
            }
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
            if (simManagerFromSimSlot != null && TextUtils.equals("TMobile_US:Inbound", simManagerFromSimSlot.getSimMnoName())) {
                hashSet2 = applyInboundRoamingPolicy(hashSet2, simManagerFromSimSlot);
            }
            if (!hashSet.isEmpty()) {
                hashSet.retainAll(hashSet2);
            }
            return hashSet;
        }
        Log.i(LOG_TAG, "CS TTY Enabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.CS_TTY.getCode());
        return new HashSet();
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

    public SipError onSipError(String str, SipError sipError) {
        Log.e(LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        this.mIsValid = this.mNumOfPcscfIp > 0;
        if ("mmtel".equals(str)) {
            if (SipErrorBase.SIP_TIMEOUT.equals(sipError) || SipErrorBase.PROXY_AUTHENTICATION_REQUIRED.equals(sipError)) {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, true, this.mIsValid, "SIP ERROR[MMTEL] : INVITE_TIMEOUT, Deregister..");
            } else if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(sipError) || TextUtils.equals(sipError.getReason(), "TCP Connection Error")) {
                if (this.mHasVoLteCall) {
                    IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "onSipError: postpone deregi till call end");
                    this.mHasPendingDeregistration = true;
                } else {
                    removeCurrentPcscfAndInitialRegister(true);
                }
            } else if (SipErrorTmoUs.USER_NOT_REGISTERED_NR_NOWARNING.equals(sipError)) {
                this.mRegMan.updateRegistration(this.mTask, RegistrationConstants.UpdateRegiReason.SIPERROR_FORCED, false);
            }
        } else if (("im".equals(str) || "ft".equals(str)) && SipErrorBase.FORBIDDEN.equals(sipError)) {
            this.mTask.setReason("SIP ERROR[IM] : FORBIDDEN, Reregister..");
            this.mRegMan.sendReRegister(this.mTask);
        }
        return sipError;
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent callEvent, SipError sipError, int i) {
        Log.i(LOG_TAG, "onCallStatus: event=" + callEvent + " error=" + sipError);
        if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
            if (this.mHasPendingDeregistration) {
                removeCurrentPcscfAndInitialRegister(true);
                this.mHasPendingDeregistration = false;
            }
        } else if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_INITIAL_REGI && SipErrorBase.SERVER_TIMEOUT.equals(sipError)) {
            removeCurrentPcscfAndInitialRegister(true);
            return;
        }
        super.onCallStatus(callEvent, sipError, i);
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0) {
            return true;
        }
        if (this.mTask.getPdnType() != 11 || !isSrvccCase()) {
            return false;
        }
        return true;
    }

    private boolean checkVowifiSetting(int i) {
        if (i != 18 || this.mWfcStatus != 2) {
            return true;
        }
        Log.i(LOG_TAG, "Rat is IWLAN but WFC switch is OFF.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int i) {
        if (!hasRcsSession()) {
            return true;
        }
        NetworkEvent networkEvent = this.mRegMan.getNetworkEvent(this.mPhoneId);
        boolean z = networkEvent != null ? networkEvent.isVopsUpdated : false;
        if ((i != 13 || z) && i != 18) {
            return true;
        }
        Log.i(LOG_TAG, "RCS session is active");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_RCS_SESSION.getCode());
        return false;
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkCallStatus() && checkVowifiSetting(i) && checkRcsEvent(i));
    }

    /* access modifiers changed from: package-private */
    public boolean hasRcsSession() {
        IImModule imModule = ImsRegistry.getServiceModuleManager().getImModule();
        return imModule != null && imModule.hasEstablishedSession();
    }

    public boolean isThrottled() {
        return this.mRegiAt > SystemClock.elapsedRealtime() || this.mAllPcscfFailed || (this.mWFCSubscribeForbiddenCounter > 2 && this.mTask.getRegistrationRat() == 18);
    }

    public void releaseThrottle(int i) {
        if (i == 1) {
            this.mAllPcscfFailed = false;
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            stopRetryTimer();
            this.mSubscribeForbiddenCounter = 0;
        } else if (i == 2 || i == 3) {
            this.mWFCSubscribeForbiddenCounter = 0;
        } else {
            if (i == 6 || i == 9) {
                if (this.mAllPcscfFailed && !this.mRegMan.getCsfbSupported(this.mPhoneId)) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "releaseThrottle: Reset retry on new PLMN of EPS only/5GS");
                    resetUpcomingRetry();
                }
            } else if (i != 14) {
                return;
            }
            this.mAllPcscfFailed = false;
        }
    }

    /* access modifiers changed from: protected */
    public void resetUpcomingRetry() {
        Log.i(LOG_TAG, "resetUpcomingRetry: Maintain mRegiAt: " + new Date(this.mRegiAt));
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mThrottleReason = 0;
    }

    public void onContactActivated() {
        Log.i(LOG_TAG, "ContactActivated. Reset SRMR2 failure counter");
        this.mSubscribeForbiddenCounter = 0;
        this.mWFCSubscribeForbiddenCounter = 0;
    }

    public int getFailureType() {
        return (!this.mAllPcscfFailed || (this.mTask.getRegistrationRat() != 20 && this.mRegMan.getCsfbSupported(this.mTask.getPhoneId()))) ? 16 : 32;
    }

    private void updateEutranValues() {
        if (this.mTask.getProfile().hasService("mmtel")) {
            int voiceTechType = getVoiceTechType();
            Log.i(LOG_TAG, "updateEutranValues : voiceTech : " + voiceTechType);
            ContentValues contentValues = new ContentValues();
            if (voiceTechType == 0) {
                contentValues.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
            } else {
                contentValues.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 2);
            }
            this.mContext.getContentResolver().update(UriUtil.buildUri(GlobalSettingsConstants.CONTENT_URI.toString(), this.mPhoneId), contentValues, (String) null, (String[]) null);
        }
    }

    public boolean determineDeRegistration(int i, int i2) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToDeRegistration:");
        if (i != 0) {
            return super.determineDeRegistration(i, i2);
        }
        int i3 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i3, "isNeedToDeRegistration: no IMS service for network " + i2 + ". Deregister.");
        RegisterTask registerTask = this.mTask;
        registerTask.setReason("no IMS service for network : " + i2);
        this.mTask.setDeregiReason(4);
        RegistrationManagerInternal registrationManagerInternal = this.mRegMan;
        RegisterTask registerTask2 = this.mTask;
        registrationManagerInternal.tryDeregisterInternal(registerTask2, !registerTask2.isRcsOnly() && isSrvccCase(), false);
        return true;
    }

    public boolean isLocationInfoLoaded(int i) {
        if (i != 18) {
            return true;
        }
        Optional.ofNullable(ImsRegistry.getGeolocationController()).ifPresent(new RegistrationGovernorTmo$$ExternalSyntheticLambda1(this));
        return true;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$isLocationInfoLoaded$4(IGeolocationController iGeolocationController) {
        if (!iGeolocationController.isCountryCodeLoaded(this.mPhoneId)) {
            IMSLog.i(LOG_TAG, "isLocationInfoLoaded: No country code. Request to start Geolocation Update.");
            iGeolocationController.startGeolocationUpdate(this.mPhoneId, false);
        }
    }

    public int getDetailedDeRegiReason(int i) {
        if (this.mTask.getDeregiReason() == 53) {
            return 53;
        }
        return super.getDetailedDeRegiReason(i);
    }

    public boolean isDeregisterOnLocationUpdate() {
        boolean z = this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0;
        boolean z2 = this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && this.mTask.getRegistrationRat() == 18 && this.mHasPendingDeregistration;
        if (z) {
            this.mHasPendingDeregistration = false;
        }
        if (!z || !z2) {
            return false;
        }
        return true;
    }

    public boolean onUpdateGeolocation(LocationInfo locationInfo) {
        if (TextUtils.isEmpty(this.mLastKnownCountryIso)) {
            this.mLastKnownCountryIso = (String) Optional.ofNullable(ImsRegistry.getGeolocationController()).map(new RegistrationGovernorTmo$$ExternalSyntheticLambda0(this)).orElse("");
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "onUpdateGeolocation: No countryIso. Get from GeolocationController: " + this.mLastKnownCountryIso);
        }
        String str = locationInfo.mCountry;
        IMSLog.i(LOG_TAG, this.mPhoneId, String.format(Locale.US, "onUpdateGeolocation: countryIso [%s] -> [%s]", new Object[]{this.mLastKnownCountryIso, str}));
        if (TextUtils.isEmpty(str) || str.equalsIgnoreCase(this.mLastKnownCountryIso)) {
            return false;
        }
        this.mLastKnownCountryIso = str;
        this.mHasPendingDeregistration = true;
        return false;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ String lambda$onUpdateGeolocation$5(IGeolocationController iGeolocationController) {
        return iGeolocationController.getLastAccessedNetworkCountryIso(this.mPhoneId);
    }
}
