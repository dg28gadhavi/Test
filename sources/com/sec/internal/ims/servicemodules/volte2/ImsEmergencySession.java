package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.net.Network;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.mdmi.MdmiServiceModule;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.volte2.data.CallSetupData;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;

public class ImsEmergencySession extends ImsCallSession {
    /* access modifiers changed from: private */
    public String LOG_TAG = "ImsEmergencySession";
    /* access modifiers changed from: private */
    public int mE911RegiTime;

    /* access modifiers changed from: protected */
    public int getLte911Fail() {
        return DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_T_LTE_911_FAIL, 20, getPhoneId()).intValue();
    }

    /* access modifiers changed from: protected */
    public int getWlan911Fail() {
        return DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_TWLAN_911_CALLFAIL_TIMER, 10, getPhoneId()).intValue();
    }

    /* access modifiers changed from: protected */
    public int getLte911FailFromEmergencyProfile() {
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (imsProfile == null) {
            Log.i(this.LOG_TAG, "[ReadyToCall] EmergencyProfile is null!");
            return 10;
        }
        String str = this.LOG_TAG;
        Log.i(str, "[ReadyToCall] getLte911FailFromEmergencyProfile=" + imsProfile.getLte911Fail());
        return imsProfile.getLte911Fail();
    }

    /* access modifiers changed from: protected */
    public int getE911InviteTo18xTime() {
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (imsProfile == null) {
            Log.i(this.LOG_TAG, "[ReadyToCall] EmergencyProfile is null!");
            return 0;
        }
        String str = this.LOG_TAG;
        Log.i(str, "[ReadyToCall] getE911InviteTo18xTimeFromEmergencyProfile=" + imsProfile.getE911InviteTo18x());
        return imsProfile.getE911InviteTo18x();
    }

    /* access modifiers changed from: protected */
    public int getE911RegiTime() {
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (imsProfile == null) {
            Log.i(this.LOG_TAG, "[ReadyToCall] EmergencyProfile is null!");
            return 0;
        }
        String str = this.LOG_TAG;
        Log.i(str, "[ReadyToCall] getE911RegiTimeFromEmergencyProfile=" + imsProfile.getE911RegiTime());
        return imsProfile.getE911RegiTime();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00bc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isEmergencyAvailable(com.sec.internal.constants.Mno r7) {
        /*
            r6 = this;
            com.sec.internal.interfaces.ims.core.IRegistrationManager r0 = r6.mRegistrationManager
            int r1 = r6.mPhoneId
            com.sec.ims.settings.ImsProfile$PROFILE_TYPE r2 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.EMERGENCY
            com.sec.ims.settings.ImsProfile r0 = r0.getImsProfile(r1, r2)
            r1 = 0
            if (r0 == 0) goto L_0x0069
            com.sec.ims.volte2.data.CallProfile r2 = r6.mCallProfile
            java.lang.String r2 = r2.getEmergencyRat()
            java.lang.String r3 = "VoWIFI"
            boolean r2 = r3.equalsIgnoreCase(r2)
            if (r2 == 0) goto L_0x001e
            r2 = 18
            goto L_0x0041
        L_0x001e:
            com.sec.ims.volte2.data.CallProfile r2 = r6.mCallProfile
            java.lang.String r2 = r2.getEmergencyRat()
            java.lang.String r3 = "VoLTE"
            boolean r2 = r3.equalsIgnoreCase(r2)
            r3 = 13
            if (r2 == 0) goto L_0x002f
            goto L_0x0040
        L_0x002f:
            com.sec.ims.volte2.data.CallProfile r2 = r6.mCallProfile
            java.lang.String r2 = r2.getEmergencyRat()
            java.lang.String r4 = "VoNR"
            boolean r2 = r4.equalsIgnoreCase(r2)
            if (r2 == 0) goto L_0x0040
            r2 = 20
            goto L_0x0041
        L_0x0040:
            r2 = r3
        L_0x0041:
            java.lang.String r3 = r6.LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "networktype : "
            r4.append(r5)
            r4.append(r2)
            java.lang.String r4 = r4.toString()
            android.util.Log.e(r3, r4)
            java.lang.String r3 = "mmtel"
            boolean r0 = r0.hasService(r3, r2)
            if (r0 != 0) goto L_0x0067
            java.lang.String r0 = r6.LOG_TAG
            java.lang.String r2 = "emergency service unavailable in current RAT"
            android.util.Log.e(r0, r2)
            goto L_0x0069
        L_0x0067:
            r0 = 1
            goto L_0x006a
        L_0x0069:
            r0 = r1
        L_0x006a:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.H3G_SE
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.H3G
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TELIA_SWE
            com.sec.internal.constants.Mno[] r2 = new com.sec.internal.constants.Mno[]{r2, r3, r4}
            boolean r2 = r7.isOneOf(r2)
            if (r2 == 0) goto L_0x00a6
            com.sec.internal.interfaces.ims.core.IPdnController r2 = com.sec.internal.ims.registry.ImsRegistry.getPdnController()
            int r3 = r6.mPhoneId
            com.sec.internal.constants.ims.os.VoPsIndication r2 = r2.getVopsIndication(r3)
            com.sec.internal.constants.ims.os.VoPsIndication r3 = com.sec.internal.constants.ims.os.VoPsIndication.NOT_SUPPORTED
            if (r2 != r3) goto L_0x00a6
            android.content.Context r2 = r6.mContext
            com.sec.internal.helper.os.ITelephonyManager r2 = com.sec.internal.helper.os.TelephonyManagerWrapper.getInstance(r2)
            int r3 = r6.mPhoneId
            int r3 = com.sec.internal.helper.SimUtil.getSubId(r3)
            int r2 = r2.getDataNetworkType(r3)
            boolean r2 = com.sec.internal.helper.NetworkUtil.is3gppPsVoiceNetwork(r2)
            if (r2 == 0) goto L_0x00a6
            java.lang.String r0 = r6.LOG_TAG
            java.lang.String r2 = "if VoPS is not supported, do CSFB"
            android.util.Log.e(r0, r2)
            r0 = r1
        L_0x00a6:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.DOCOMO
            if (r7 != r2) goto L_0x00bc
            com.sec.internal.interfaces.ims.core.IRegistrationManager r7 = r6.mRegistrationManager
            int r2 = r6.mPhoneId
            boolean r7 = r7.isEmergencyCallProhibited(r2)
            if (r7 == 0) goto L_0x00bc
            java.lang.String r6 = r6.LOG_TAG
            java.lang.String r7 = "if DCM get 503 error in regi, do CSFB"
            android.util.Log.e(r6, r7)
            goto L_0x00bd
        L_0x00bc:
            r1 = r0
        L_0x00bd:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession.isEmergencyAvailable(com.sec.internal.constants.Mno):boolean");
    }

    public class EmergencyCallStateMachine extends CallStateMachine {
        private static final int EVENT_EMERGENCY_REGISTERED = 911;
        private int epdnCountCheck = 5;
        private int epdnFailCount = 0;
        /* access modifiers changed from: private */
        public boolean mEmergencyRegistered = false;
        /* access modifiers changed from: private */
        public boolean mHasEstablished = false;
        /* access modifiers changed from: private */
        public boolean mNextPcscfChangedWorking = false;
        /* access modifiers changed from: private */
        public boolean mRequstedStopPDN = false;
        /* access modifiers changed from: private */
        public boolean mStartDelayed = false;
        protected EmergencyCallStateMachine mThisEsm = this;
        final /* synthetic */ ImsEmergencySession this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        protected EmergencyCallStateMachine(ImsEmergencySession imsEmergencySession, Context context, ImsCallSession imsCallSession, ImsRegistration imsRegistration, IVolteServiceModuleInternal iVolteServiceModuleInternal, Mno mno, IVolteServiceInterface iVolteServiceInterface, RemoteCallbackList<IImsCallSessionEventListener> remoteCallbackList, IRegistrationManager iRegistrationManager, IImsMediaController iImsMediaController, String str, Looper looper) {
            super(context, imsCallSession, imsRegistration, iVolteServiceModuleInternal, mno, iVolteServiceInterface, remoteCallbackList, iRegistrationManager, iImsMediaController, str, looper);
            this.this$0 = imsEmergencySession;
            this.mReadyToCall = new ReadyToCall(this);
            this.mOutgoingCall = new OutgoingCall(this);
            this.mAlertingCall = new AlertingCall(this);
            this.mInCall = new InCall(this);
            this.mEndingCall = new EndingCall(this);
        }

        public class ReadyToCall extends ImsReadyToCall {
            ImsProfile emergencyProfile = null;

            ReadyToCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[ReadyToCall] processMessage " + message.what);
                int i = message.what;
                if (i != 1) {
                    if (i == 11) {
                        onStart(message);
                    } else if (i == 14) {
                        return onEmergecyInvite();
                    } else {
                        if (i != 306) {
                            if (i == 402) {
                                EmergencyCallStateMachine.this.mNextPcscfChangedWorking = false;
                                EmergencyCallStateMachine.this.mThisEsm.sendMessage(14);
                            } else if (i == EmergencyCallStateMachine.EVENT_EMERGENCY_REGISTERED) {
                                return onEventEmergencyRegistered(message);
                            } else {
                                if (i == 3) {
                                    return onEnded(message);
                                }
                                if (i == 4) {
                                    return onError(message);
                                }
                                if (!(i == 303 || i == 304)) {
                                    return super.processMessage(message);
                                }
                            }
                        }
                        return false;
                    }
                    return true;
                }
                EmergencyCallStateMachine.this.mThisEsm.sendMessage(3, 0, EmergencyCallStateMachine.EVENT_EMERGENCY_REGISTERED);
                return false;
            }

            private void onStart(Message message) {
                int i;
                String str = this.LOG_TAG;
                Log.i(str, "cmcType : " + EmergencyCallStateMachine.this.this$0.mCmcType);
                if (ImsCallUtil.isCmcSecondaryType(EmergencyCallStateMachine.this.this$0.mCmcType)) {
                    Log.e(this.LOG_TAG, "[ReadyToCall] start: E911 is not allowed on SD.");
                    EmergencyCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, SipErrorBase.E911_NOT_ALLOWED_ON_SD);
                    return;
                }
                ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(EmergencyCallStateMachine.this.this$0.mPhoneId);
                Mno simMno = simManagerFromSimSlot == null ? Mno.DEFAULT : simManagerFromSimSlot.getSimMno();
                if (simMno.isOneOf(Mno.TMOUS, Mno.DISH) && !EmergencyCallStateMachine.this.mStartDelayed && EmergencyCallStateMachine.this.this$0.hasInProgressEmergencyTask()) {
                    Log.i(this.LOG_TAG, "Deregistering is in progress. retry after 1sec");
                    EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(Message.obtain(message), 1000);
                    EmergencyCallStateMachine.this.mStartDelayed = true;
                } else if (simManagerFromSimSlot == null || !simManagerFromSimSlot.hasNoSim() || !Mno.fromSalesCode(OmcCode.get()).isJpn()) {
                    this.emergencyProfile = this.mRegistrationManager.getImsProfile(EmergencyCallStateMachine.this.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                    String systemProperty = ImsUtil.getSystemProperty("gsm.operator.numeric", EmergencyCallStateMachine.this.this$0.mPhoneId, "00101");
                    if (simManagerFromSimSlot == null || !simManagerFromSimSlot.getDevMno().isAus() || (this.emergencyProfile != null && !"00101".equals(systemProperty))) {
                        if (!EmergencyCallStateMachine.this.this$0.isEmergencyAvailable(simMno)) {
                            Log.i(this.LOG_TAG, "emergency service unavailable. do CSFB");
                            EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                            EmergencyCallStateMachine.this.mThisEsm.sendMessage(303);
                            return;
                        }
                        ImsProfile imsProfile = this.emergencyProfile;
                        if (imsProfile == null || imsProfile.getPdnType() != 11 || !ImsConstants.EmergencyRat.IWLAN.equalsIgnoreCase(EmergencyCallStateMachine.this.this$0.mCallProfile.getEmergencyRat()) || EmergencyCallStateMachine.this.this$0.getEmergencyRegistration() != null) {
                            setEmergencyRttCall();
                            Message obtainMessage = EmergencyCallStateMachine.this.obtainMessage(EmergencyCallStateMachine.EVENT_EMERGENCY_REGISTERED);
                            IMSLog.c(LogClass.VOLTE_E911_REGI_START, "" + EmergencyCallStateMachine.this.this$0.mPhoneId);
                            if (ImsConstants.EmergencyRat.IWLAN.equalsIgnoreCase(EmergencyCallStateMachine.this.this$0.getCallProfile().getEmergencyRat())) {
                                i = 18;
                            } else {
                                i = (!ImsConstants.EmergencyRat.LTE.equalsIgnoreCase(EmergencyCallStateMachine.this.this$0.getCallProfile().getEmergencyRat()) && ImsConstants.EmergencyRat.NR.equalsIgnoreCase(EmergencyCallStateMachine.this.this$0.getCallProfile().getEmergencyRat())) ? 20 : 13;
                            }
                            this.mRegistrationManager.startEmergencyRegistration(EmergencyCallStateMachine.this.this$0.mPhoneId, obtainMessage, i);
                            long delayLte911Fail = EmergencyCallStateMachine.this.this$0.getDelayLte911Fail(simMno);
                            if (delayLte911Fail > 0) {
                                if (simMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
                                    Log.i(this.LOG_TAG, "[ReadyToCall] TMO_E911 start E1 Timer");
                                    this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E1, ImsCallUtil.EMERGENCY_TIMER_STATE.STARTED);
                                } else if (this.mMno.isTmobile()) {
                                    Log.i(this.LOG_TAG, "[ReadyToCall] TMO_EUR_E911 start E1 REG timer");
                                } else {
                                    String str2 = this.LOG_TAG;
                                    Log.i(str2, "[ReadyToCall] start Tlte or TWlan-911fail" + delayLte911Fail + " millis.");
                                }
                                IMSLog.c(LogClass.VOLTE_E911_CALL_TIMER_START, EmergencyCallStateMachine.this.this$0.mPhoneId + "," + delayLte911Fail);
                                if (simMno != Mno.KDDI || SimUtil.getAvailableSimCount() <= 1) {
                                    EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(303, delayLte911Fail);
                                } else {
                                    EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(EmergencyCallStateMachine.this.obtainMessage(CallStateMachine.ON_E911_PERM_FAIL, 0, -1, new SipError(2697, "EMERGENCY PERM FAILURE")), delayLte911Fail);
                                }
                            }
                        } else {
                            Log.i(this.LOG_TAG, "[ReadyToCall] No IMS Registration, Do Call End");
                            EmergencyCallStateMachine.this.mThisEsm.sendMessage(4, 0, -1, new SipError(1003, "No VoWIFI Registration"));
                        }
                    } else if (!EmergencyCallStateMachine.this.mStartDelayed) {
                        Log.i(this.LOG_TAG, "switching network is in progress. retry after 1sec");
                        this.mRegistrationManager.refreshAuEmergencyProfile(EmergencyCallStateMachine.this.this$0.mPhoneId);
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(Message.obtain(message), 1000);
                        EmergencyCallStateMachine.this.mStartDelayed = true;
                    } else {
                        Log.i(this.LOG_TAG, "no Emergency profile, should CSFB now...");
                        EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                        EmergencyCallStateMachine.this.mThisEsm.sendMessage(303);
                    }
                } else {
                    Log.e(this.LOG_TAG, "[ReadyToCall] Not allowed emergency call in JPN without sim");
                    EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(EmergencyCallStateMachine.this.obtainMessage(4, 0, -1, new SipError(2698, "Tlte_911fail")), 300);
                }
            }

            private void setEmergencyRttCall() {
                EmergencyCallStateMachine emergencyCallStateMachine = EmergencyCallStateMachine.this;
                emergencyCallStateMachine.callType = emergencyCallStateMachine.this$0.mCallProfile.getCallType();
                if (this.mModule.getAutomaticMode(EmergencyCallStateMachine.this.this$0.getPhoneId())) {
                    EmergencyCallStateMachine emergencyCallStateMachine2 = EmergencyCallStateMachine.this;
                    int i = emergencyCallStateMachine2.callType;
                    if (i == 7) {
                        emergencyCallStateMachine2.callType = 18;
                    } else if (i == 8) {
                        emergencyCallStateMachine2.callType = 19;
                    }
                    emergencyCallStateMachine2.this$0.mCallProfile.setCallType(emergencyCallStateMachine2.callType);
                }
            }

            private boolean onEventEmergencyRegistered(Message message) {
                Mno mno;
                Mno mno2;
                EmergencyCallStateMachine.this.mEmergencyRegistered = true;
                IMSLog.c(LogClass.VOLTE_E911_REGI_DONE, "" + EmergencyCallStateMachine.this.this$0.mPhoneId);
                this.mRegistrationManager.removeE911RegiTimer();
                ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(EmergencyCallStateMachine.this.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                this.emergencyProfile = imsProfile;
                if (imsProfile == null) {
                    Log.i(this.LOG_TAG, "[ReadyToCall] EmergencyProfile is null!");
                    return false;
                }
                if ((imsProfile.getPdnType() == 11) || this.mRegistrationManager.isPdnConnected(this.emergencyProfile, EmergencyCallStateMachine.this.this$0.mPhoneId)) {
                    Bundle data = message.getData();
                    if (data == null || !data.getBoolean("isERegiAuthFailed", false)) {
                        Mno fromName = Mno.fromName(this.emergencyProfile.getMnoName());
                        this.mMno = fromName;
                        if (fromName == Mno.DOCOMO || fromName.isChn() || (mno = this.mMno) == Mno.GCF || mno == Mno.RJIL || mno == Mno.TMOUS || mno == Mno.DISH || mno.isTmobile() || (mno2 = this.mMno) == Mno.ATT || mno2 == Mno.SPRINT || mno2 == Mno.KDDI) {
                            EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                            if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) || this.mMno.isTmobile()) {
                                Log.i(this.LOG_TAG, "[ReadyToCall] Emergency E1 timer stopped");
                                this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E1, ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED);
                            } else {
                                Log.i(this.LOG_TAG, "[ReadyToCall] remove ON_LTE_911_FAIL");
                            }
                        }
                        EmergencyCallStateMachine emergencyCallStateMachine = EmergencyCallStateMachine.this;
                        if (emergencyCallStateMachine.mIsMdmiEnabled) {
                            emergencyCallStateMachine.mMdmiE911Listener.notifySipMsg(MdmiServiceModule.msgType.E911_REGI, 0);
                        }
                        EmergencyCallStateMachine.this.mThisEsm.sendMessage(14);
                        return true;
                    }
                    Log.i(this.LOG_TAG, "[ReadyToCall] Authentication failure. do CSFB");
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                    EmergencyCallStateMachine.this.mThisEsm.sendMessage(303);
                    return true;
                }
                Log.i(this.LOG_TAG, "[ReadyToCall] PDN disconnected. do CSFB");
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                EmergencyCallStateMachine.this.mThisEsm.sendMessage(303);
                return true;
            }

            private boolean onEmergecyInvite() {
                ImsUri imsUri;
                int i;
                ImsCallSession session;
                Network network;
                EmergencyCallStateMachine emergencyCallStateMachine = EmergencyCallStateMachine.this;
                emergencyCallStateMachine.callType = emergencyCallStateMachine.this$0.mCallProfile.getCallType();
                if (this.mModule.getAutomaticMode(EmergencyCallStateMachine.this.this$0.getPhoneId())) {
                    EmergencyCallStateMachine emergencyCallStateMachine2 = EmergencyCallStateMachine.this;
                    int i2 = emergencyCallStateMachine2.callType;
                    if (i2 == 7) {
                        emergencyCallStateMachine2.callType = 18;
                    } else if (i2 == 8) {
                        emergencyCallStateMachine2.callType = 19;
                    }
                    emergencyCallStateMachine2.this$0.mCallProfile.setCallType(emergencyCallStateMachine2.callType);
                }
                String dialingNumber = EmergencyCallStateMachine.this.this$0.mCallProfile.getDialingNumber();
                if (EmergencyCallStateMachine.this.this$0.mCallProfile.getUrn() != null) {
                    imsUri = ImsUri.parse(EmergencyCallStateMachine.this.this$0.mCallProfile.getUrn());
                } else {
                    ImsEmergencySession imsEmergencySession = EmergencyCallStateMachine.this.this$0;
                    imsUri = imsEmergencySession.buildUri(dialingNumber, (String) null, imsEmergencySession.mCallProfile.getCallType());
                }
                ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(EmergencyCallStateMachine.this.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                this.emergencyProfile = imsProfile;
                if (imsProfile == null) {
                    Log.i(this.LOG_TAG, "[ReadyToCall] EmergencyProfile is null!");
                    return false;
                }
                Mno fromName = Mno.fromName(imsProfile.getMnoName());
                this.mMno = fromName;
                if ((fromName == Mno.VZW && "922".equals(dialingNumber)) || ((SimUtil.isDSH(EmergencyCallStateMachine.this.this$0.mPhoneId) || SimUtil.isDSH5G(EmergencyCallStateMachine.this.this$0.mPhoneId)) && (("522".equals(dialingNumber) || "922".equals(dialingNumber)) && !SimUtil.isNoSIM(EmergencyCallStateMachine.this.this$0.mPhoneId) && !this.mModule.isRoaming(EmergencyCallStateMachine.this.this$0.mPhoneId)))) {
                    EmergencyCallStateMachine emergencyCallStateMachine3 = EmergencyCallStateMachine.this;
                    if (emergencyCallStateMachine3.mIsMdmiEnabled) {
                        emergencyCallStateMachine3.mMdmiE911Listener.notifySipMsg(MdmiServiceModule.msgType.E922_CALL, 0);
                    }
                    imsUri = UriGeneratorFactory.getInstance().get(EmergencyCallStateMachine.this.this$0.mCallProfile.getOriginatingUri(), UriGenerator.URIServiceType.VOLTE_URI).getNetworkPreferredUri(ImsUri.UriType.SIP_URI, dialingNumber);
                    Log.i(this.LOG_TAG, "[ReadyToCall] makecall target change to " + imsUri);
                }
                if (EmergencyCallStateMachine.this.mIsMdmiEnabled && "911".equals(dialingNumber)) {
                    EmergencyCallStateMachine.this.mMdmiE911Listener.notifySipMsg(MdmiServiceModule.msgType.E911_CALL, 0);
                }
                EmergencyCallStateMachine emergencyCallStateMachine4 = EmergencyCallStateMachine.this;
                CallSetupData callSetupData = new CallSetupData(imsUri, dialingNumber, emergencyCallStateMachine4.callType, emergencyCallStateMachine4.this$0.mCallProfile.getCLI());
                callSetupData.setOriginatingUri(EmergencyCallStateMachine.this.this$0.getOriginatingUri());
                callSetupData.setLteEpsOnlyAttached(this.mModule.getLteEpsOnlyAttached(EmergencyCallStateMachine.this.this$0.mPhoneId));
                callSetupData.setCmcBoundSessionId(EmergencyCallStateMachine.this.this$0.mCallProfile.getCmcBoundSessionId());
                ImsRegistration r0 = EmergencyCallStateMachine.this.this$0.getEmergencyRegistration();
                if (r0 != null) {
                    i = r0.getHandle();
                    Log.i(this.LOG_TAG, "bind network for MediaEngine " + r0.getNetwork());
                    this.mMediaController.bindToNetwork(r0.getNetwork());
                } else {
                    i = -1;
                }
                if ((this.mMno == Mno.ATT || SimUtil.isDSH(EmergencyCallStateMachine.this.this$0.mPhoneId) || SimUtil.isDSH5G(EmergencyCallStateMachine.this.this$0.mPhoneId) || SimUtil.isCSpire(EmergencyCallStateMachine.this.this$0.mPhoneId) || SimUtil.isUnited(EmergencyCallStateMachine.this.this$0.mPhoneId)) && ImsRegistry.getPdnController().isEmergencyEpdgConnected(EmergencyCallStateMachine.this.this$0.mPhoneId)) {
                    ImsEmergencySession imsEmergencySession2 = EmergencyCallStateMachine.this.this$0;
                    callSetupData.setPEmergencyInfo(imsEmergencySession2.getPEmergencyInfo((String) null, imsEmergencySession2.getImei(imsEmergencySession2.mPhoneId)));
                    Log.i(this.LOG_TAG, "e911Aid = " + callSetupData.getPEmergencyInfo());
                }
                IUserAgent r02 = EmergencyCallStateMachine.this.this$0.getEmergencyUa();
                if (!(r02 == null || (network = r02.getNetwork()) == null)) {
                    Log.i(this.LOG_TAG, "bind network for Emergency VT or RTT " + network);
                    this.mMediaController.bindToNetwork(network);
                }
                startEmergencyFailTimer();
                if (this.mMno != Mno.YTL || r02 == null || (!r02.isRegistering() && !r02.isDeregistring())) {
                    int makeCall = this.mVolteSvcIntf.makeCall(i, callSetupData, (HashMap<String, String>) null, EmergencyCallStateMachine.this.this$0.mPhoneId);
                    Log.i(this.LOG_TAG, "[ReadyToCall] makeCall() returned session id " + makeCall);
                    if (makeCall < 0) {
                        EmergencyCallStateMachine.this.mThisEsm.sendMessage(4, 0, -1, new SipError(1002, "stack return -1."));
                        return true;
                    }
                    int cmcBoundSessionId = this.mSession.getCallProfile().getCmcBoundSessionId();
                    if (cmcBoundSessionId > 0 && (session = this.mModule.getSession(cmcBoundSessionId)) != null) {
                        session.getCallProfile().setCmcBoundSessionId(makeCall);
                        Log.i(this.LOG_TAG, "[Emergency ReadyToCall] updated boundSessionId : " + session.getCallProfile().getCmcBoundSessionId());
                    }
                    EmergencyCallStateMachine.this.this$0.setSessionId(makeCall);
                    EmergencyCallStateMachine.this.this$0.mCallProfile.setDirection(0);
                    EmergencyCallStateMachine emergencyCallStateMachine5 = EmergencyCallStateMachine.this;
                    emergencyCallStateMachine5.transitionTo(emergencyCallStateMachine5.mOutgoingCall);
                    return true;
                }
                EmergencyCallStateMachine.this.mEmergencyRegistered = false;
                EmergencyCallStateMachine.this.mThisEsm.sendMessage(4, 0, -1, new SipError(1003, "UA is de/registring status"));
                return true;
            }

            private void startEmergencyFailTimer() {
                long e911InviteTo18xTime = ((long) EmergencyCallStateMachine.this.this$0.getE911InviteTo18xTime()) * 1000;
                if (!this.mMno.isOneOf(Mno.BELL, Mno.ROGERS, Mno.TELUS, Mno.KOODO, Mno.VTR, Mno.EASTLINK)) {
                    int i = (e911InviteTo18xTime > 0 ? 1 : (e911InviteTo18xTime == 0 ? 0 : -1));
                    if (i > 0 && !this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) && !this.mMno.isTmobile()) {
                        String str = this.LOG_TAG;
                        Log.i(str, "[ReadyToCall] start t_e911_invite_to_18x timer (" + e911InviteTo18xTime + "ms) for waiting SIP 18x");
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(307, e911InviteTo18xTime);
                    } else if (Mno.ATT.equals(this.mMno)) {
                        if (i > 0) {
                            String str2 = this.LOG_TAG;
                            Log.i(str2, "[ReadyToCall] start Tlte-t_e911_invite_to_18x timer (" + e911InviteTo18xTime + "ms) for waiting SIP 18x");
                            EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(307, e911InviteTo18xTime);
                        }
                    } else if (EmergencyCallStateMachine.this.this$0.mE911RegiTime > 0) {
                        long r0 = ((long) EmergencyCallStateMachine.this.this$0.mE911RegiTime) * 1000;
                        String str3 = this.LOG_TAG;
                        Log.i(str3, "[ReadyToCall] start Tlte-911fail timer (" + r0 + "ms) for waiting SIP 18x");
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(307, r0);
                    }
                } else if (!ImsRegistry.getPdnController().isEmergencyEpdgConnected(EmergencyCallStateMachine.this.this$0.mPhoneId)) {
                    EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(307, e911InviteTo18xTime);
                }
            }

            private boolean onError(Message message) {
                SipError sipError = (SipError) message.obj;
                if (this.mMno == Mno.YTL && !EmergencyCallStateMachine.this.mEmergencyRegistered && ImsCallUtil.isClientError(sipError)) {
                    ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(EmergencyCallStateMachine.this.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                    if (!EmergencyCallStateMachine.this.mRequstedStopPDN && imsProfile != null) {
                        int deregTimeout = imsProfile.getDeregTimeout(13);
                        Log.i(this.LOG_TAG, "Disconnect Emergency PDN.");
                        this.mRegistrationManager.stopEmergencyRegistration(EmergencyCallStateMachine.this.this$0.mPhoneId);
                        EmergencyCallStateMachine.this.mRequstedStopPDN = true;
                        EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(Message.obtain(message), (long) (deregTimeout + 500));
                        return true;
                    }
                }
                return super.processMessage(message);
            }

            private boolean onEnded(Message message) {
                if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
                    String str = this.LOG_TAG;
                    Log.i(str, "[ReadyToCall] mNextPcscfChangedWorking=" + EmergencyCallStateMachine.this.mNextPcscfChangedWorking);
                    if (EmergencyCallStateMachine.this.mNextPcscfChangedWorking) {
                        Log.i(this.LOG_TAG, "[ReadyToCall] TMO_E911 ON_NEXT_PCSCF_CHANGED is running, so just return");
                        return true;
                    }
                }
                return super.processMessage(message);
            }
        }

        public class OutgoingCall extends ImsOutgoingCall {
            OutgoingCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public void enter() {
                ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(EmergencyCallStateMachine.this.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                boolean z = true;
                if (imsProfile == null || imsProfile.get100tryingTimer() <= 0) {
                    ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(EmergencyCallStateMachine.this.this$0.mPhoneId);
                    boolean isSimMobilityFeatureEnabled = SimUtil.isSimMobilityFeatureEnabled();
                    if (simManagerFromSimSlot != null) {
                        z = simManagerFromSimSlot.hasNoSim();
                    }
                    String str = this.LOG_TAG;
                    Log.i(str, "isSimMobility : " + isSimMobilityFeatureEnabled + ", isNoSim : " + z);
                    if (isSimMobilityFeatureEnabled && z) {
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(208, (long) 10000);
                        String str2 = this.LOG_TAG;
                        Log.i(str2, "[OutgoingCall] Start 100 Trying Timer (" + 10000 + " msec).");
                    }
                } else {
                    int i = imsProfile.get100tryingTimer();
                    if (this.mMno == Mno.USCC && this.mModule.getSessionCount(EmergencyCallStateMachine.this.this$0.mPhoneId) == 1) {
                        Log.i(this.LOG_TAG, "[OutgoingCall] USCC G30 Timer (12 sec)");
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(208, 12000);
                    } else {
                        if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
                            Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911 start E2 Timer");
                            this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E2, ImsCallUtil.EMERGENCY_TIMER_STATE.STARTED);
                        } else {
                            String str3 = this.LOG_TAG;
                            Log.i(str3, "[OutgoingCall] Start 100 Trying Timer (" + i + " msec).");
                        }
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(208, (long) i);
                    }
                    if ((this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) || this.mMno.isTmobile()) && !EmergencyCallStateMachine.this.mThisEsm.hasMessages(307)) {
                        Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911, TMO_EUR_E911 start E3 Timer");
                        EmergencyCallStateMachine emergencyCallStateMachine = EmergencyCallStateMachine.this;
                        emergencyCallStateMachine.mThisEsm.sendMessageDelayed(307, ((long) emergencyCallStateMachine.this$0.getE911InviteTo18xTime()) * 1000);
                        this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, ImsCallUtil.EMERGENCY_TIMER_STATE.STARTED);
                    }
                }
                EmergencyCallStateMachine emergencyCallStateMachine2 = EmergencyCallStateMachine.this;
                if (emergencyCallStateMachine2.mIsMdmiEnabled) {
                    emergencyCallStateMachine2.mMdmiE911Listener.notifySipMsg(MdmiServiceModule.msgType.SIP_INVITE, System.currentTimeMillis());
                }
                super.enter();
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[OutgoingCall] processMessage " + message.what);
                int i = message.what;
                if (i == 31) {
                    return onTrying(message);
                }
                if (i == 32 || i == 34 || i == 35) {
                    return onSessionProgress(message);
                }
                if (i == 41) {
                    return onEstablished(message);
                }
                if (i == 208) {
                    return on100TryingTimeOut(message);
                }
                if (i == 306 || i == 303 || i == 304) {
                    return false;
                }
                return super.processMessage(message);
            }

            private boolean onTrying(Message message) {
                if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) || this.mMno.isTmobile()) {
                    Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911 stop E2 Timer");
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(208);
                    this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E2, ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED);
                } else if (this.mMno == Mno.KDDI) {
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(CallStateMachine.ON_E911_PERM_FAIL);
                }
                return super.processMessage(message);
            }

            private boolean onSessionProgress(Message message) {
                if (EmergencyCallStateMachine.this.this$0.needRemoveTimerOn18X()) {
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                } else if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) || this.mMno.isTmobile()) {
                    Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911 stop E2, E3 Timer");
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(208);
                    IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mModule;
                    int i = EmergencyCallStateMachine.this.this$0.mPhoneId;
                    ImsCallSession imsCallSession = this.mSession;
                    ImsCallUtil.EMERGENCY_TIMER emergency_timer = ImsCallUtil.EMERGENCY_TIMER.E2;
                    ImsCallUtil.EMERGENCY_TIMER_STATE emergency_timer_state = ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED;
                    iVolteServiceModuleInternal.sendEmergencyCallTimerState(i, imsCallSession, emergency_timer, emergency_timer_state);
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                    this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, emergency_timer_state);
                } else if (EmergencyCallStateMachine.this.this$0.mE911RegiTime > 0) {
                    Log.i(this.LOG_TAG, "[OutgoingCall] remove ON_E911_INVITE_TILL_180_TIMER_FAIL");
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                }
                return super.processMessage(message);
            }

            private boolean onEstablished(Message message) {
                if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) || this.mMno.isTmobile()) {
                    Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911 stop E2, E3 Timer");
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(208);
                    IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mModule;
                    int i = EmergencyCallStateMachine.this.this$0.mPhoneId;
                    ImsCallSession imsCallSession = this.mSession;
                    ImsCallUtil.EMERGENCY_TIMER emergency_timer = ImsCallUtil.EMERGENCY_TIMER.E2;
                    ImsCallUtil.EMERGENCY_TIMER_STATE emergency_timer_state = ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED;
                    iVolteServiceModuleInternal.sendEmergencyCallTimerState(i, imsCallSession, emergency_timer, emergency_timer_state);
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                    this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, emergency_timer_state);
                }
                return super.processMessage(message);
            }

            private boolean on100TryingTimeOut(Message message) {
                if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
                    Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911 E2 Timer expired");
                    this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E2, ImsCallUtil.EMERGENCY_TIMER_STATE.EXPIRED);
                    if (ImsCallUtil.isRttEmergencyCall(EmergencyCallStateMachine.this.this$0.mCallProfile.getCallType())) {
                        Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911 RTT stop E3 timer and end call");
                        EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                        this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED);
                    } else if (EmergencyCallStateMachine.this.mThisEsm.hasMessages(307)) {
                        Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911 E3 Timer active");
                        ImsEmergencySession imsEmergencySession = EmergencyCallStateMachine.this.this$0;
                        if (!imsEmergencySession.isNoNextPcscf(imsEmergencySession.mPhoneId)) {
                            Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911 redial to next p-cscf");
                            this.mRegistrationManager.moveNextPcscf(EmergencyCallStateMachine.this.this$0.mPhoneId, EmergencyCallStateMachine.this.obtainMessage(402));
                            EmergencyCallStateMachine.this.mNextPcscfChangedWorking = true;
                            EmergencyCallStateMachine emergencyCallStateMachine = EmergencyCallStateMachine.this;
                            emergencyCallStateMachine.transitionTo(emergencyCallStateMachine.mReadyToCall);
                            return true;
                        }
                        Log.i(this.LOG_TAG, "[OutgoingCall] TMO_E911 stop E3 timer and CSFB");
                        EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                        this.mModule.sendEmergencyCallTimerState(EmergencyCallStateMachine.this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED);
                    }
                }
                return super.processMessage(message);
            }
        }

        public class AlertingCall extends ImsAlertingCall {
            AlertingCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public void enter() {
                Log.i(this.LOG_TAG, "[AlertingCall] enter ");
                Mno simMno = SimUtil.getSimMno(EmergencyCallStateMachine.this.this$0.mPhoneId);
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                if ((!simMno.isOneOf(Mno.TMOUS, Mno.DISH) && !this.mMno.isTmobile() && EmergencyCallStateMachine.this.mThisEsm.hasMessages(303)) || EmergencyCallStateMachine.this.this$0.needStartTimerOnAlerting()) {
                    long lte911FailFromEmergencyProfile = ((long) EmergencyCallStateMachine.this.this$0.getLte911FailFromEmergencyProfile()) * 1000;
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                    String str = this.LOG_TAG;
                    Log.i(str, "[AlertingCall] refresh Tlte_911fail timer : " + lte911FailFromEmergencyProfile + " millis.");
                    EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(303, lte911FailFromEmergencyProfile);
                }
                super.enter();
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[AlertingCall] processMessage " + message.what);
                int i = message.what;
                if (i != 32) {
                    if (i == 306) {
                        return false;
                    }
                    if (!(i == 34 || i == 35)) {
                        if (i == 303) {
                            return onLte911Fail(message);
                        }
                        if (i != 304) {
                            return super.processMessage(message);
                        }
                        return false;
                    }
                }
                return onSessionProgress(message);
            }

            private boolean onSessionProgress(Message message) {
                if (EmergencyCallStateMachine.this.this$0.needRemoveTimerOn18X()) {
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                }
                return super.processMessage(message);
            }

            private boolean onLte911Fail(Message message) {
                Mno mno;
                if (!this.mMno.isCanada() && (mno = this.mMno) != Mno.VODAFONE_UK && !mno.isKor()) {
                    return false;
                }
                Log.i(this.LOG_TAG, "[AlertingCall] Ignore ON_LTE_911_FAIL");
                return super.processMessage(message);
            }
        }

        public class EndingCall extends ImsEndingCall {
            EndingCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[EndingCall] processMessage " + message.what);
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                int i = message.what;
                if (i == 2 || i == 3) {
                    boolean unused = EmergencyCallStateMachine.this.onEnded(message);
                }
                return super.processMessage(message);
            }

            public void exit() {
                Mno simMno = SimUtil.getSimMno(EmergencyCallStateMachine.this.this$0.mPhoneId);
                EmergencyCallStateMachine emergencyCallStateMachine = EmergencyCallStateMachine.this;
                if (emergencyCallStateMachine.mIsMdmiEnabled) {
                    emergencyCallStateMachine.mMdmiE911Listener.onCallEnded();
                }
                if (!this.mModule.isEmergencyRegistered(EmergencyCallStateMachine.this.this$0.mPhoneId)) {
                    if (simMno == Mno.VZW && (this.mModule.isEcbmMode(EmergencyCallStateMachine.this.this$0.mPhoneId) || EmergencyCallStateMachine.this.mHasEstablished)) {
                        Log.i(this.LOG_TAG, "ECBM mode. Keep Emergency PDN.");
                        super.exit();
                        return;
                    } else if (simMno != Mno.ATT && !this.mMno.isKor()) {
                        Log.i(this.LOG_TAG, "Disconnect Emergency PDN.");
                        this.mRegistrationManager.stopEmergencyRegistration(EmergencyCallStateMachine.this.this$0.mPhoneId);
                    }
                }
                if (simMno == Mno.ATT && this.mRegistrationManager.isVoWiFiSupported(EmergencyCallStateMachine.this.this$0.mPhoneId)) {
                    ImsRegistration r0 = EmergencyCallStateMachine.this.this$0.getEmergencyRegistration();
                    ImsRegistration r1 = EmergencyCallStateMachine.this.this$0.getIMSRegistration();
                    if (!(r1 == null || r0 == null || r1.getEpdgStatus() == r0.getEpdgStatus())) {
                        Log.i(this.LOG_TAG, "RAT is different between current IMS and Emergencywhich is already made but not de-registered.");
                        this.mRegistrationManager.stopEmergencyRegistration(EmergencyCallStateMachine.this.this$0.mPhoneId);
                    }
                }
                EmergencyCallStateMachine.this.mRequstedStopPDN = false;
                super.exit();
            }
        }

        public class InCall extends ImsInCall {
            InCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public void enter() {
                Log.i(this.LOG_TAG, "Enter [InCall]");
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(303);
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                super.enter();
                EmergencyCallStateMachine.this.mHasEstablished = true;
            }
        }

        /* access modifiers changed from: package-private */
        public void handleE911Fail() {
            IRegistrationGovernor registrationGovernor;
            Log.i(this.this$0.LOG_TAG, "[ANY_STATE] handleE911Fail()");
            IMSLog.c(LogClass.VOLTE_E911_CALL_TIMER_ERROR, "" + this.this$0.mPhoneId);
            this.mThisSm.sendMessage(4, 0, EVENT_EMERGENCY_REGISTERED, new SipError(Id.REQUEST_VSH_STOP_SESSION, "Tlte_911fail"));
            ImsRegistration imsRegistration = this.mRegistration;
            if (imsRegistration != null && (registrationGovernor = this.mRegistrationManager.getRegistrationGovernor(imsRegistration.getHandle())) != null) {
                registrationGovernor.onSipError(ImsCallUtil.isVideoCall(this.this$0.mCallProfile.getCallType()) ? "mmtel-video" : "mmtel", new SipError(2507));
            }
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x005e, code lost:
            if (r0 != 307) goto L_0x0090;
         */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0077 A[RETURN] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void unhandledMessage(android.os.Message r4) {
            /*
                r3 = this;
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession r0 = r3.this$0
                java.lang.String r0 = r0.LOG_TAG
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "[ANY_STATE] unhandledMessage "
                r1.append(r2)
                int r2 = r4.what
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                android.util.Log.i(r0, r1)
                com.sec.internal.constants.Mno r0 = r3.mMno
                com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.DEFAULT
                if (r0 != r1) goto L_0x0045
                com.sec.internal.interfaces.ims.core.IRegistrationManager r0 = r3.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession r1 = r3.this$0
                int r1 = r1.mPhoneId
                com.sec.ims.settings.ImsProfile$PROFILE_TYPE r2 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.EMERGENCY
                com.sec.ims.settings.ImsProfile r0 = r0.getImsProfile(r1, r2)
                if (r0 == 0) goto L_0x003b
                java.lang.String r0 = r0.getMnoName()
                com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.fromName(r0)
                r3.mMno = r0
                goto L_0x0045
            L_0x003b:
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession r0 = r3.this$0
                int r0 = r0.mPhoneId
                com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)
                r3.mMno = r0
            L_0x0045:
                int r0 = r4.what
                r1 = 1
                if (r0 == r1) goto L_0x008d
                r1 = 3
                if (r0 == r1) goto L_0x0086
                r1 = 4
                if (r0 == r1) goto L_0x007f
                r1 = 303(0x12f, float:4.25E-43)
                if (r0 == r1) goto L_0x0061
                r1 = 304(0x130, float:4.26E-43)
                if (r0 == r1) goto L_0x0078
                r1 = 306(0x132, float:4.29E-43)
                if (r0 == r1) goto L_0x0071
                r1 = 307(0x133, float:4.3E-43)
                if (r0 == r1) goto L_0x0068
                goto L_0x0090
            L_0x0061:
                boolean r0 = r3.onLte911Fail()
                if (r0 == 0) goto L_0x0068
                return
            L_0x0068:
                int r4 = r4.what
                boolean r4 = r3.onE911InviteTill180TimerFail(r4)
                if (r4 == 0) goto L_0x0071
                return
            L_0x0071:
                boolean r4 = r3.onEpdnSetupFail()
                if (r4 == 0) goto L_0x0078
                return
            L_0x0078:
                r3.onLte911FailAfterDelay()
                r3.handleE911Fail()
                return
            L_0x007f:
                boolean r0 = r3.onError(r4)
                if (r0 == 0) goto L_0x0090
                return
            L_0x0086:
                boolean r0 = r3.onEnded(r4)
                if (r0 == 0) goto L_0x0090
                return
            L_0x008d:
                r3.terminate()
            L_0x0090:
                super.unhandledMessage(r4)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession.EmergencyCallStateMachine.unhandledMessage(android.os.Message):void");
        }

        private boolean onLte911Fail() {
            if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) || this.mMno.isTmobile()) {
                Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 E1 timer expired");
                this.mThisEsm.removeMessages(303);
                this.mModule.sendEmergencyCallTimerState(this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E1, ImsCallUtil.EMERGENCY_TIMER_STATE.EXPIRED);
                if (ImsCallUtil.isRttEmergencyCall(this.this$0.mCallProfile.getCallType())) {
                    Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 RTT end call");
                } else {
                    IRegistrationManager iRegistrationManager = this.mRegistrationManager;
                    boolean isPdnConnected = iRegistrationManager.isPdnConnected(iRegistrationManager.getImsProfile(this.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY), this.this$0.mPhoneId);
                    String r2 = this.this$0.LOG_TAG;
                    Log.i(r2, "[ANY_STATE] TMO_E911 emergencyPdnConnected =" + isPdnConnected);
                    if (isPdnConnected) {
                        Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 anonymous INVITE to same p-cscf");
                        IUserAgent r0 = this.this$0.getEmergencyUa();
                        if (r0 != null) {
                            r0.notifyE911RegistrationFailed();
                        }
                        this.mThisEsm.sendMessage(14);
                        return true;
                    } else if (this.mMno.isTmobile()) {
                        Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_EUR epdn not connected, do CSFB");
                        return false;
                    } else if (!this.mModule.isRoaming(this.this$0.mPhoneId) && this.mModule.isRegisteredOver3gppPsVoice(this.this$0.mPhoneId)) {
                        Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 stopEmergencyRegistration and redial to IMS PDN");
                        this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                        IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mModule;
                        ImsEmergencySession imsEmergencySession = this.this$0;
                        if (iVolteServiceModuleInternal.triggerPsRedial(imsEmergencySession.mPhoneId, imsEmergencySession.getCallId(), 11)) {
                            Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 redial to IMS PDN success");
                            quit();
                            return true;
                        }
                    }
                }
                Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 CSFB");
            }
            return false;
        }

        private boolean onE911InviteTill180TimerFail(int i) {
            if (i == 307) {
                Log.i(this.this$0.LOG_TAG, "[ANY_STATE] ON_E911_INVITE_TILL_180_TIMER_FAIL expired");
                this.mThisEsm.removeMessages(307);
                this.mModule.sendEmergencyCallTimerState(this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, ImsCallUtil.EMERGENCY_TIMER_STATE.EXPIRED);
            }
            if (this.mTryingReceived) {
                this.mThisEsm.sendMessage(1, 17);
                this.mThisEsm.sendMessageDelayed(304, 500);
                return true;
            } else if (this.this$0.getCallState() == CallConstants.STATE.ReadyToCall) {
                return false;
            } else {
                this.mThisEsm.sendMessage(1, 17);
                return false;
            }
        }

        private boolean onEpdnSetupFail() {
            int i;
            if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
                Log.e(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 EPDN setup fail before E1 expire, stop E1 Timer");
                this.mThisEsm.removeMessages(303);
                this.mModule.sendEmergencyCallTimerState(this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E1, ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED);
                if (ImsCallUtil.isRttEmergencyCall(this.this$0.mCallProfile.getCallType())) {
                    String r0 = this.this$0.LOG_TAG;
                    Log.i(r0, "[ANY_STATE] TMO_E911 RTT, mRequstedStopPDN=" + this.mRequstedStopPDN);
                    if (this.mRequstedStopPDN) {
                        this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                    }
                } else if (!this.mModule.isRoaming(this.this$0.mPhoneId) && this.mModule.isRegisteredOver3gppPsVoice(this.this$0.mPhoneId)) {
                    Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 stopEmergencyRegistration and redial to IMS PDN");
                    this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                    IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mModule;
                    ImsEmergencySession imsEmergencySession = this.this$0;
                    if (iVolteServiceModuleInternal.triggerPsRedial(imsEmergencySession.mPhoneId, imsEmergencySession.getCallId(), 11)) {
                        Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 stopEmergencyRegistration");
                        this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                        quit();
                        return true;
                    }
                }
                Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 CSFB");
                handleE911Fail();
                return true;
            } else if (this.mMno != Mno.KDDI) {
                return false;
            } else {
                IRegistrationManager iRegistrationManager = this.mRegistrationManager;
                if (iRegistrationManager.isPdnConnected(iRegistrationManager.getImsProfile(this.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY), this.this$0.mPhoneId) || (i = this.epdnFailCount) >= this.epdnCountCheck) {
                    return false;
                }
                this.epdnFailCount = i + 1;
                return true;
            }
        }

        private void onLte911FailAfterDelay() {
            if (!this.mModule.isEmergencyRegistered(this.this$0.mPhoneId)) {
                Mno mno = this.mMno;
                if (mno == Mno.ATT || mno == Mno.EE) {
                    this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                }
            }
        }

        private boolean onError(Message message) {
            int code = ((SipError) message.obj).getCode();
            Mno mno = this.mMno;
            if (mno == Mno.VZW && code >= 300 && code < 700) {
                handleE911Fail();
                return true;
            } else if (mno.isTmobile() && !this.mRequstedStopPDN && code == 403) {
                Log.i(this.this$0.LOG_TAG, "Disconnect Emergency PDN TMOBILE 403 error");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(message), 500);
                return true;
            } else if (this.mMno.isKor() && !this.mRequstedStopPDN && (code == 380 || (code >= 400 && code < 500))) {
                Log.i(this.this$0.LOG_TAG, "Disconnect Emergency PDN.");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(message), 500);
                return true;
            } else if ("TEL".equals(OmcCode.get()) && !this.mRequstedStopPDN && code >= 400 && code < 600) {
                Log.i(this.this$0.LOG_TAG, "Disconnect Emergency PDN Telstra 4XX, 5XX error");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(message), 500);
                return true;
            } else if (this.mMno.isCanada() && !this.mRequstedStopPDN) {
                Log.i(this.this$0.LOG_TAG, "Disconnect Emergency PDN");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mThisEsm.sendMessageDelayed(Message.obtain(message), 500);
                this.mRequstedStopPDN = true;
                return true;
            } else if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
                return onErrorForTmo(message, code);
            } else {
                return false;
            }
        }

        private boolean onErrorForTmo(Message message, int i) {
            String r0 = this.this$0.LOG_TAG;
            Log.i(r0, "[ANY_STATE] TMO_E911 errCode=" + i + ", mRequstedStopPDN=" + this.mRequstedStopPDN + ", E2 = " + this.mThisEsm.hasMessages(208) + ", E3 = " + this.mThisEsm.hasMessages(307));
            CallProfile callProfile = this.this$0.mCallProfile;
            if (callProfile == null || !ImsCallUtil.isRttEmergencyCall(callProfile.getCallType())) {
                if (i >= 300 && i < 400) {
                    Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 stop E2, E3 timer and CSFB");
                    this.mThisEsm.removeMessages(208);
                    IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mModule;
                    int i2 = this.this$0.mPhoneId;
                    ImsCallSession imsCallSession = this.mSession;
                    ImsCallUtil.EMERGENCY_TIMER emergency_timer = ImsCallUtil.EMERGENCY_TIMER.E2;
                    ImsCallUtil.EMERGENCY_TIMER_STATE emergency_timer_state = ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED;
                    iVolteServiceModuleInternal.sendEmergencyCallTimerState(i2, imsCallSession, emergency_timer, emergency_timer_state);
                    this.mThisEsm.removeMessages(307);
                    this.mModule.sendEmergencyCallTimerState(this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, emergency_timer_state);
                    return false;
                } else if (i < 400 || i >= 700) {
                    return false;
                } else {
                    if (this.mThisEsm.hasMessages(208)) {
                        ImsEmergencySession imsEmergencySession = this.this$0;
                        if (imsEmergencySession.isNoNextPcscf(imsEmergencySession.mPhoneId)) {
                            Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 stop E2, E3 timer and CSFB");
                            this.mThisEsm.removeMessages(208);
                            IVolteServiceModuleInternal iVolteServiceModuleInternal2 = this.mModule;
                            int i3 = this.this$0.mPhoneId;
                            ImsCallSession imsCallSession2 = this.mSession;
                            ImsCallUtil.EMERGENCY_TIMER emergency_timer2 = ImsCallUtil.EMERGENCY_TIMER.E2;
                            ImsCallUtil.EMERGENCY_TIMER_STATE emergency_timer_state2 = ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED;
                            iVolteServiceModuleInternal2.sendEmergencyCallTimerState(i3, imsCallSession2, emergency_timer2, emergency_timer_state2);
                            this.mThisEsm.removeMessages(307);
                            this.mModule.sendEmergencyCallTimerState(this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, emergency_timer_state2);
                            return false;
                        }
                        Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 stop E2 timer and redial to next p-cscf");
                        this.mThisEsm.removeMessages(208);
                        this.mModule.sendEmergencyCallTimerState(this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E2, ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED);
                        this.mRegistrationManager.moveNextPcscf(this.this$0.mPhoneId, obtainMessage(402));
                        this.mNextPcscfChangedWorking = true;
                        transitionTo(this.mReadyToCall);
                        return true;
                    } else if (!this.mThisEsm.hasMessages(307)) {
                        return false;
                    } else {
                        Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 stop E3 timer and CSFB");
                        this.mThisEsm.removeMessages(307);
                        this.mModule.sendEmergencyCallTimerState(this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED);
                        return false;
                    }
                }
            } else if (this.mRequstedStopPDN) {
                return false;
            } else {
                Log.i(this.this$0.LOG_TAG, "[ANY_STATE] TMO_E911 RTT, stopEmergencyRegistration");
                this.mThisEsm.removeMessages(208);
                IVolteServiceModuleInternal iVolteServiceModuleInternal3 = this.mModule;
                int i4 = this.this$0.mPhoneId;
                ImsCallSession imsCallSession3 = this.mSession;
                ImsCallUtil.EMERGENCY_TIMER emergency_timer3 = ImsCallUtil.EMERGENCY_TIMER.E2;
                ImsCallUtil.EMERGENCY_TIMER_STATE emergency_timer_state3 = ImsCallUtil.EMERGENCY_TIMER_STATE.CANCELLED;
                iVolteServiceModuleInternal3.sendEmergencyCallTimerState(i4, imsCallSession3, emergency_timer3, emergency_timer_state3);
                this.mThisEsm.removeMessages(307);
                this.mModule.sendEmergencyCallTimerState(this.this$0.mPhoneId, this.mSession, ImsCallUtil.EMERGENCY_TIMER.E3, emergency_timer_state3);
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.errorCode = 2414;
                this.mThisEsm.sendMessageDelayed(Message.obtain(message), 5000);
                this.mRequstedStopPDN = true;
                return true;
            }
        }

        /* access modifiers changed from: private */
        public boolean onEnded(Message message) {
            Mno mno;
            Mno mno2;
            String str = (String) message.obj;
            String r1 = this.this$0.LOG_TAG;
            Log.i(r1, "[ANY_STATE] ErrorMessage " + str);
            if ((this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) || this.mMno.isKor() || (mno = this.mMno) == Mno.ORANGE || mno == Mno.TELSTRA || "TEL".equals(OmcCode.get()) || (mno2 = this.mMno) == Mno.TWO_DEGREE || mno2.isCanada() || this.mMno == Mno.VELCOM_BY) && !this.mRequstedStopPDN) {
                Log.i(this.this$0.LOG_TAG, "Disconnect Emergency PDN.");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(message), 500);
                return true;
            } else if (this.mMno != Mno.MTN_SOUTHAFRICA || !"Alternative Service".equalsIgnoreCase(str) || this.mRequstedStopPDN) {
                Mno mno3 = this.mMno;
                if ((mno3 == Mno.EE || mno3 == Mno.EE_ESN) && !this.mRequstedStopPDN) {
                    IPdnController pdnController = ImsRegistry.getPdnController();
                    boolean z = pdnController.getVoiceRegState(this.this$0.mPhoneId) != 0;
                    boolean isEpdgConnected = pdnController.isEpdgConnected(this.this$0.mPhoneId);
                    boolean isEmergencyEpdgConnected = pdnController.isEmergencyEpdgConnected(this.this$0.mPhoneId);
                    if (z && isEpdgConnected && !isEmergencyEpdgConnected) {
                        Log.i(this.this$0.LOG_TAG, "Disconnect Emergency PDN in LTE - No CS, Only Epdg");
                        this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                        this.mRequstedStopPDN = true;
                        this.mThisEsm.sendMessageDelayed(Message.obtain(message), 500);
                        return true;
                    }
                }
                if (this.mMno == Mno.DOCOMO && this.this$0.getCallState() != CallConstants.STATE.EndingCall && "RTP Timeout".equalsIgnoreCase(str)) {
                    Log.i(this.this$0.LOG_TAG, "Disconnect Emergency PDN for DCM.");
                    this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                    this.mRequstedStopPDN = true;
                    this.mThisEsm.sendMessageDelayed(Message.obtain(message), 500);
                }
                return false;
            } else {
                Log.i(this.this$0.LOG_TAG, "Disconnect Emergency PDN for MTN_ZA");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(message), 500);
                return true;
            }
        }

        private void terminate() {
            if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
                Log.i(this.this$0.LOG_TAG, "reset mNextPcscfChangedWorking to false");
                this.mNextPcscfChangedWorking = false;
            }
        }
    }

    public ImsRegistration getRegistration() {
        return getEmergencyRegistration();
    }

    /* access modifiers changed from: private */
    public ImsRegistration getEmergencyRegistration() {
        IRegistrationManager iRegistrationManager = this.mRegistrationManager;
        if (iRegistrationManager == null) {
            return null;
        }
        ImsProfile imsProfile = iRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        boolean z = imsProfile != null && imsProfile.getPdnType() == 11;
        for (ImsRegistration imsRegistration : this.mRegistrationManager.getRegistrationInfo()) {
            if (this.mPhoneId == imsRegistration.getPhoneId() && (imsRegistration.getImsProfile().hasEmergencySupport() || (z && imsRegistration.getImsProfile().getPdnType() == 11))) {
                return imsRegistration;
            }
        }
        return null;
    }

    public void setSessionId(int i) {
        this.LOG_TAG = IMSLog.appendSessionIdToLogTag(this.LOG_TAG, i);
        super.setSessionId(i);
    }

    /* access modifiers changed from: private */
    public ImsRegistration getIMSRegistration() {
        IRegistrationManager iRegistrationManager = this.mRegistrationManager;
        if (iRegistrationManager == null) {
            return null;
        }
        for (ImsRegistration imsRegistration : iRegistrationManager.getRegistrationInfo()) {
            if (imsRegistration != null && imsRegistration.getPhoneId() == this.mPhoneId && imsRegistration.getImsProfile() != null && imsRegistration.getImsProfile().getPdnType() == 11) {
                return imsRegistration;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public ImsUri getOriginatingUri() {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        Mno simMno = simManagerFromSimSlot == null ? Mno.DEFAULT : simManagerFromSimSlot.getSimMno();
        ImsRegistration emergencyRegistration = getEmergencyRegistration();
        ImsRegistration iMSRegistration = getIMSRegistration();
        if (emergencyRegistration != null) {
            ImsUri registeredImpu = emergencyRegistration.getRegisteredImpu();
            if (simMno != Mno.ATT) {
                Log.i(this.LOG_TAG, "getOriginatingUri: emergency call with registration.");
                return emergencyRegistration.getPreferredImpu().getUri();
            } else if (emergencyRegistration.getImpuList() == null) {
                return registeredImpu;
            } else {
                for (NameAddr nameAddr : emergencyRegistration.getImpuList()) {
                    if (nameAddr.getUri().getUriType().equals(ImsUri.UriType.TEL_URI)) {
                        Log.i(this.LOG_TAG, "getOriginatingUri: Found Tel-URI");
                        return nameAddr.getUri();
                    }
                }
                return registeredImpu;
            }
        } else if (simMno == Mno.VZW) {
            Log.i(this.LOG_TAG, "getOriginatingUri: emergency call without registration.");
            if (simManagerFromSimSlot == null) {
                return null;
            }
            return ImsUri.parse(simManagerFromSimSlot.getDerivedImpu());
        } else if (!simMno.isKor() || iMSRegistration != null) {
            Log.i(this.LOG_TAG, "getOriginatingUri: No emergency registration. Use IMEI-based preferred-ID");
            return null;
        } else {
            Log.i(this.LOG_TAG, "getOriginatingUri: emergency call without SIM");
            return ImsUri.parse("sip:anonymous@anonymous.invalid");
        }
    }

    public ImsEmergencySession(Context context, CallProfile callProfile, Looper looper, IVolteServiceModuleInternal iVolteServiceModuleInternal) {
        super(context, callProfile, (ImsRegistration) null, looper, iVolteServiceModuleInternal);
        setPhoneId(callProfile.getPhoneId());
    }

    public void init(IVolteServiceInterface iVolteServiceInterface, IRegistrationManager iRegistrationManager) {
        IRegistrationManager iRegistrationManager2 = iRegistrationManager;
        this.mVolteSvcIntf = iVolteServiceInterface;
        this.mRegistrationManager = iRegistrationManager2;
        iRegistrationManager2.refreshAuEmergencyProfile(this.mPhoneId);
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null) {
            this.mMno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        } else {
            ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
            if (imsProfile != null) {
                this.mMno = Mno.fromName(imsProfile.getMnoName());
            }
        }
        this.mE911RegiTime = getE911RegiTime();
        EmergencyCallStateMachine emergencyCallStateMachine = new EmergencyCallStateMachine(this, this.mContext, this, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, "EmergencyCallStateMachine", this.mLooper);
        this.smCallStateMachine = emergencyCallStateMachine;
        emergencyCallStateMachine.init();
        this.mMediaController.registerForMediaEvent(this);
        Log.i(this.LOG_TAG, "start EmergencyCallStateMachine");
        this.smCallStateMachine.start();
        this.mImsCallSessionEventHandler = new ImsCallSessionEventHandler(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine, this.mCallProfile, this.mVolteSvcIntf, this.mMediaController);
        this.mImsCallDedicatedBearer = new ImsCallDedicatedBearer(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine);
        this.mDiagnosisController = new DiagnosisController(this.smCallStateMachine);
        this.mVolteSvcIntf.registerForCallStateEvent(this.mVolteStackEventHandler, 1, (Object) null);
        this.mVolteSvcIntf.registerForCurrentLocationDiscoveryDuringEmergencyCallEvent(this.mVolteStackEventHandler, 8, (Object) null);
        setIsNrSaMode();
    }

    /* access modifiers changed from: private */
    public IUserAgent getEmergencyUa() {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager != null) {
            return registrationManager.getUserAgentOnPdn(15, this.mPhoneId);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void setPhoneId(int i) {
        this.mPhoneId = i;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r3 = r3.getEmergencyGovernor(r2.mPhoneId);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isNoNextPcscf(int r3) {
        /*
            r2 = this;
            com.sec.internal.interfaces.ims.core.IRegistrationManager r3 = r2.mRegistrationManager
            if (r3 == 0) goto L_0x0011
            int r0 = r2.mPhoneId
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r3 = r3.getEmergencyGovernor(r0)
            if (r3 == 0) goto L_0x0011
            boolean r3 = r3.isNoNextPcscf()
            goto L_0x0012
        L_0x0011:
            r3 = 0
        L_0x0012:
            java.lang.String r2 = r2.LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "TMO_E911 isNoNextPcscf = "
            r0.append(r1)
            r0.append(r3)
            java.lang.String r0 = r0.toString()
            android.util.Log.i(r2, r0)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession.isNoNextPcscf(int):boolean");
    }

    /* access modifiers changed from: private */
    public boolean hasInProgressEmergencyTask() {
        IRegistrationGovernor emergencyGovernor;
        IRegistrationManager iRegistrationManager = this.mRegistrationManager;
        if (iRegistrationManager == null || (emergencyGovernor = iRegistrationManager.getEmergencyGovernor(this.mPhoneId)) == null) {
            return false;
        }
        RegistrationConstants.RegisterTaskState state = emergencyGovernor.getState();
        String str = this.LOG_TAG;
        Log.i(str, "emergency Task status : " + state);
        return state == RegistrationConstants.RegisterTaskState.DEREGISTERING;
    }

    /* access modifiers changed from: private */
    public boolean needStartTimerOnAlerting() {
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (imsProfile != null) {
            return imsProfile.getNeedStartE911TimerOnAlerting();
        }
        Log.i(this.LOG_TAG, "needStartTimerOnAlerting, EmergencyProfile is null!");
        return false;
    }

    /* access modifiers changed from: private */
    public boolean needRemoveTimerOn18X() {
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (imsProfile == null) {
            Log.i(this.LOG_TAG, "needRemoveTimerOn18X, EmergencyProfile is null!");
            return false;
        }
        boolean needRemoveE911TimerOn18x = imsProfile.getNeedRemoveE911TimerOn18x();
        if (!this.mMno.isKor() && this.mMno != Mno.VZW && !needRemoveE911TimerOn18x) {
            return false;
        }
        Log.e(this.LOG_TAG, "E911 Timer removed if 180 / 183 received");
        return true;
    }

    /* access modifiers changed from: protected */
    public long getDelayLte911Fail(Mno mno) {
        int i;
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (mno == Mno.KDDI) {
            if (imsProfile != null && SimUtil.getAvailableSimCount() > 1) {
                i = imsProfile.getE911PermFail();
            } else if (!this.mModule.isRoaming(this.mPhoneId)) {
                return 0;
            } else {
                i = getLte911FailFromEmergencyProfile();
            }
        } else if (mno == Mno.VZW) {
            if (ImsRegistry.getPdnController().isEpdgConnected(this.mPhoneId)) {
                i = getWlan911Fail();
            } else {
                i = getLte911Fail();
            }
        } else if (this.mE911RegiTime > 0) {
            Log.i(this.LOG_TAG, "getDelayLte911Fail - 0, RegiMgr trigger Fail timer");
            return 0;
        } else {
            i = getLte911FailFromEmergencyProfile();
        }
        return ((long) i) * 1000;
    }

    public boolean isQuantumEncryptionServiceAvailable() {
        Log.i(this.LOG_TAG, "isQuantumEncryptionServiceAvailable: not support for emergency call");
        return false;
    }
}
