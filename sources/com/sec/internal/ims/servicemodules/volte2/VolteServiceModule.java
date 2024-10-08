package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.BarringInfo;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.Call;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.samsung.android.ims.cmc.SemCmcRecordingInfo;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IRttEventListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.cmc.CmcCallInfo;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.extensions.WiFiManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IVolteServiceEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.os.EmcBsIndication;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.Registration;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.QuantumSecurityStatusEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.core.handler.secims.UserAgent;
import com.sec.internal.ims.core.imsdc.IdcImsCallSessionData;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.entitlement.config.EntitlementConfigService;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;
import com.sec.internal.ims.servicemodules.volte2.data.DedicatedBearerEvent;
import com.sec.internal.ims.servicemodules.volte2.data.DtmfInfo;
import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;
import com.sec.internal.ims.servicemodules.volte2.data.SIPDataEvent;
import com.sec.internal.ims.servicemodules.volte2.data.TextInfo;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcServiceHelper;
import com.sec.internal.ims.settings.DmProfileLoader;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.ims.xq.att.ImsXqReporter;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.quantumencryption.IQuantumEncryptionServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import com.voltecrypt.service.SxHangUpEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.json.JSONException;

public class VolteServiceModule extends VolteServiceModuleInternal implements IVolteServiceModule {
    public String getTrn(String str, String str2) {
        return null;
    }

    public /* bridge */ /* synthetic */ IImsCallSession getForegroundSession() {
        return super.getForegroundSession();
    }

    public /* bridge */ /* synthetic */ IImsCallSession getSessionByCallId(int i) {
        return super.getSessionByCallId(i);
    }

    public VolteServiceModule(Looper looper, Context context, IRegistrationManager iRegistrationManager, IPdnController iPdnController, IVolteServiceInterface iVolteServiceInterface, IMediaServiceInterface iMediaServiceInterface, IOptionsServiceInterface iOptionsServiceInterface) {
        super(looper, context, iRegistrationManager, iPdnController, iVolteServiceInterface, iMediaServiceInterface, iOptionsServiceInterface);
    }

    public void setUpTest(ImsCallSessionManager imsCallSessionManager, ImsCallSipErrorFactory imsCallSipErrorFactory, IVolteServiceInterface iVolteServiceInterface, ImsMediaController imsMediaController) {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "setUpTest:");
        this.mVolteSvcIntf.unregisterForIncomingCallEvent(this);
        this.mVolteSvcIntf.unregisterForCallStateEvent(this);
        this.mVolteSvcIntf.unregisterForDialogEvent(this);
        this.mVolteSvcIntf.unregisterForDedicatedBearerNotifyEvent(this);
        this.mVolteSvcIntf.unregisterForRtpLossRateNoti(this);
        this.mImsCallSessionManager = imsCallSessionManager;
        this.mImsCallSipErrorFactory = imsCallSipErrorFactory;
        this.mVolteSvcIntf = iVolteServiceInterface;
        this.mMediaController = imsMediaController;
        iVolteServiceInterface.registerForIncomingCallEvent(this, 1, (Object) null);
        this.mVolteSvcIntf.registerForCallStateEvent(this, 2, (Object) null);
        this.mVolteSvcIntf.registerForDialogEvent(this, 3, (Object) null);
        this.mVolteSvcIntf.registerForDedicatedBearerNotifyEvent(this, 8, (Object) null);
        this.mVolteSvcIntf.registerForRtpLossRateNoti(this, 18, (Object) null);
    }

    public Context getContext() {
        return this.mContext;
    }

    public CmcServiceHelper getCmcServiceHelper() {
        return this.mCmcServiceModule;
    }

    public String[] getServicesRequiring() {
        return new String[]{"mmtel", "mmtel-video", "mmtel-call-composer", "cdpn", "datachannel"};
    }

    public void onConfigured(int i) {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "onConfigured:");
        updateFeature(i);
    }

    public void onSimReady(int i) {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "onSimReady:");
    }

    private void onEventSimReady(int i) {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "onEventSimReady<" + i + ">");
        updateFeature(i);
        registerAllowedNetworkTypesListener(i);
        if (this.mEcholocateController != null) {
            Mno simMno = SimUtil.getSimMno(i);
            boolean z = simMno.equalsWithSalesCode(Mno.TMOUS, OmcCode.get()) || simMno.equalsWithSalesCode(Mno.SPRINT, OmcCode.get());
            Log.i(str, "EcholocateBroadcaster: " + z);
            if (DeviceUtil.isTablet() || !z) {
                this.mEcholocateController.stop();
            } else {
                this.mEcholocateController.start();
            }
        }
        if (this.mImsXqReporter == null && ImsRegistry.getBoolean(i, GlobalSettingsConstants.Call.SUPPORT_CIQ, false)) {
            this.mImsXqReporter = new ImsXqReporter(this.mContext, i);
            if (ImsXqReporter.isXqEnabled(this.mContext, i)) {
                this.mImsXqReporter.start();
            } else {
                this.mImsXqReporter.stop();
            }
        }
    }

    public boolean isVolteServiceStatus() {
        return isVolteServiceStatus(this.mActiveDataPhoneId);
    }

    public boolean isVolteServiceStatus(int i) {
        ImsRegistration imsRegistration = getImsRegistration(i);
        boolean isVolteServiceStatus = imsRegistration != null ? DmProfileLoader.getProfile(this.mContext, imsRegistration.getImsProfile(), i).isVolteServiceStatus() : true;
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "VolteServiceStatus : " + isVolteServiceStatus);
        return isVolteServiceStatus;
    }

    public boolean isVolteSupportECT() {
        return isVolteSupportECT(this.mActiveDataPhoneId);
    }

    public boolean isVolteSupportECT(int i) {
        boolean z;
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (!(imsRegistration == null || imsRegistration.getImsProfile() == null || !imsRegistration.getImsProfile().getSupportEct())) {
            if (hasEmergencyCall(imsRegistration.getPhoneId())) {
                Log.i(IVolteServiceModuleInternal.LOG_TAG, "Has emergency call");
            } else {
                z = true;
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str, "isVolteSupportECT : " + z);
                return z;
            }
        }
        z = false;
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "isVolteSupportECT : " + z);
        return z;
    }

    private void updateFeature(int i) {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "phoneId : " + i);
        this.mEnabledFeatures[i] = 0;
        updateFeatureMmtel(i);
        updateFeatureMmtelVideo(i);
        int composerAuthValue = ImsUtil.getComposerAuthValue(i, this.mContext);
        int vBCAuthValue = ImsUtil.getVBCAuthValue(i, this.mContext);
        IMSLog.i(str, i, "composerAuthVal" + composerAuthValue + "vbcAuthVal" + vBCAuthValue);
        Mno simMno = SimUtil.getSimMno(i);
        boolean z = false;
        boolean z2 = composerAuthValue == 2 || composerAuthValue == 3;
        if (vBCAuthValue == 1) {
            z = true;
        }
        if (Mno.TMOUS.equals(simMno)) {
            if ((z2 || z) && DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-call-composer", i) == 1) {
                IMSLog.i(str, i, "add MMTEL Composer feature" + simMno);
                long[] jArr = this.mEnabledFeatures;
                jArr[i] = jArr[i] | Capabilities.FEATURE_MMTEL_CALL_COMPOSER;
            } else {
                IMSLog.i(str, i, "do not add MMTEL Composer feature" + simMno);
            }
        } else if (!z2 || DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-call-composer", i) != 1) {
            IMSLog.i(str, i, "do not add MMTEL Composer feature" + simMno);
        } else {
            IMSLog.i(str, i, "add MMTEL Composer feature" + simMno);
            long[] jArr2 = this.mEnabledFeatures;
            jArr2[i] = jArr2[i] | Capabilities.FEATURE_MMTEL_CALL_COMPOSER;
        }
        this.mEventLog.add("Update Feature " + this);
    }

    private void updateFeatureMmtel(int i) {
        Mno simMno = SimUtil.getSimMno(i);
        boolean isVowifiEnabled = isVowifiEnabled(i);
        if (simMno.isOneOf(Mno.SKT, Mno.KT, Mno.LGU, Mno.TMOUS, Mno.DISH)) {
            long[] jArr = this.mEnabledFeatures;
            jArr[i] = jArr[i] | ((long) Capabilities.FEATURE_MMTEL);
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel", i) == 1 && DmConfigHelper.readSwitch(this.mContext, "mmtel", true, i)) {
            long[] jArr2 = this.mEnabledFeatures;
            jArr2[i] = jArr2[i] | ((long) Capabilities.FEATURE_MMTEL);
        }
        if ((simMno == Mno.VZW || simMno.isEmeasewaoce()) && isVowifiEnabled) {
            long[] jArr3 = this.mEnabledFeatures;
            jArr3[i] = jArr3[i] | ((long) Capabilities.FEATURE_MMTEL);
        }
        if (simMno == Mno.SPRINT && VowifiConfig.isEnabled(this.mContext, i)) {
            long[] jArr4 = this.mEnabledFeatures;
            jArr4[i] = jArr4[i] | ((long) Capabilities.FEATURE_MMTEL);
        }
        if (ImsRegistry.getCmcAccountManager().isCmcEnabled()) {
            long[] jArr5 = this.mEnabledFeatures;
            jArr5[i] = jArr5[i] | ((long) Capabilities.FEATURE_MMTEL);
        }
    }

    private void updateFeatureMmtelVideo(int i) {
        if (SimUtil.getSimMno(i).isOneOf(Mno.SKT, Mno.KT, Mno.LGU)) {
            boolean isVolteSettingEnabled = isVolteSettingEnabled();
            boolean isVolteServiceStatus = isVolteServiceStatus();
            boolean isLTEDataModeEnabled = isLTEDataModeEnabled(i);
            if (isVolteSettingEnabled && isVolteServiceStatus && isLTEDataModeEnabled) {
                long[] jArr = this.mEnabledFeatures;
                jArr[i] = jArr[i] | ((long) Capabilities.FEATURE_MMTEL_VIDEO);
            }
        }
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-video", i) == 1 && DmConfigHelper.readSwitch(this.mContext, "mmtel-video", true, i)) {
            long[] jArr2 = this.mEnabledFeatures;
            jArr2[i] = jArr2[i] | ((long) Capabilities.FEATURE_MMTEL_VIDEO);
        }
    }

    public void onVoWiFiSwitched(int i) {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "onVoWiFiSwitched:");
        updateFeature(i);
    }

    public void onServiceSwitched(int i, ContentValues contentValues) {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "onServiceSwitched");
        updateFeature(i);
    }

    /* access modifiers changed from: protected */
    public void startEpdnDisconnectTimer(int i, long j) {
        stopEpdnDisconnectTimer(i);
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "startRetryTimer: millis " + j);
        PreciseAlarmManager instance = PreciseAlarmManager.getInstance(this.mContext);
        Message obtainMessage = obtainMessage(16, i, -1);
        this.mEpdnDisconnectTimeOut.put(Integer.valueOf(i), obtainMessage);
        instance.sendMessageDelayed(getClass().getSimpleName(), obtainMessage, j);
    }

    /* access modifiers changed from: protected */
    public void stopEpdnDisconnectTimer(int i) {
        if (this.mEpdnDisconnectTimeOut.containsKey(Integer.valueOf(i))) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "stopEpdnDisconnectTimer[" + i + "]");
            this.mEpdnDisconnectTimeOut.remove(Integer.valueOf(i));
            PreciseAlarmManager.getInstance(this.mContext).removeMessage(this.mEpdnDisconnectTimeOut.get(Integer.valueOf(i)));
        }
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        if (imsRegistration != null && imsRegistration.getImsProfile() != null) {
            ImsProfile imsProfile = imsRegistration.getImsProfile();
            int phoneId = imsRegistration.getPhoneId();
            IMSLog.c(LogClass.VOLTE_REGISTERED, "" + phoneId);
            ImsRegistration imsRegistration2 = getImsRegistration(phoneId);
            this.mLastRegiErrorCode[phoneId] = SipErrorBase.OK.getCode();
            super.onRegistered(imsRegistration);
            Mno fromName = Mno.fromName(imsProfile.getMnoName());
            if (imsProfile.hasEmergencySupport()) {
                SimpleEventLog simpleEventLog = this.mEventLog;
                simpleEventLog.add("Emergency Registered Feature " + this.mEnabledFeatures[phoneId]);
                if (fromName == Mno.KDDI) {
                    startEpdnDisconnectTimer(phoneId, 240000);
                    return;
                }
                return;
            }
            if (this.mWfcEpdgMgr.isEpdgServiceConnected()) {
                boolean z = ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.Call.ALLOW_RELEASE_WFC_BEFORE_HO, false);
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.logAndAdd(fromName + " is allow release call " + z);
                this.mWfcEpdgMgr.getEpdgMgr().setReleaseCallBeforeHO(phoneId, z);
            }
            IRegistrationManager iRegistrationManager = this.mRegMan;
            if (!(iRegistrationManager == null || !iRegistrationManager.isVoWiFiSupported(phoneId) || imsRegistration2 == null || imsRegistration2.getEpdgStatus() == imsRegistration.getEpdgStatus())) {
                ImsRegistration imsRegistration3 = getImsRegistration(phoneId, true);
                if (fromName == Mno.ATT && imsRegistration3 != null && imsRegistration.getEpdgStatus() != imsRegistration3.getEpdgStatus() && !hasEmergencyCall(phoneId)) {
                    this.mRegMan.stopEmergencyRegistration(phoneId);
                }
                this.mImsCallSessionManager.handleEpdgHandover(phoneId, imsRegistration, fromName);
            }
            terminateMoWfcWhenWfcSettingOff(phoneId);
            String str = IVolteServiceModuleInternal.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Registered to VOLTE service. ");
            sb.append(IMSLog.checker(imsRegistration + ""));
            sb.append(" TTYMode ");
            sb.append(this.mTtyMode[phoneId]);
            Log.i(str, sb.toString());
            SimpleEventLog simpleEventLog3 = this.mEventLog;
            simpleEventLog3.logAndAdd("Registered Feature " + this.mEnabledFeatures[phoneId] + " with handle " + imsRegistration.getHandle());
            if (!(imsProfile.getTtyType() == 1 || imsProfile.getTtyType() == 3)) {
                this.mVolteSvcIntf.setTtyMode(phoneId, 0, this.mTtyMode[phoneId]);
            }
            if (ImsCallUtil.isCmcPrimaryType(imsProfile.getCmcType())) {
                this.mCmcMediaController.connectToSve(phoneId);
            }
            if (imsRegistration.hasService("mmtel")) {
                this.mMmtelAcquiredEver = true;
                this.mProhibited[phoneId] = false;
            } else {
                Log.i(str, "Registration Without MMTEL has DialogList notify empty dialog");
                clearDialogList(phoneId, imsRegistration.getHandle());
            }
            setIsLteRetrying(phoneId, false);
            this.mImsCallSessionManager.onRegistered(imsRegistration);
            this.mCmcServiceModule.onRegistered(imsRegistration);
            this.mIdcServiceModule.onRegistered(imsRegistration);
        }
    }

    public void onDeregistering(ImsRegistration imsRegistration) {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "onDeregistering");
        this.mCmcServiceModule.onDeregistering(imsRegistration);
        removeMessages(13);
        sendMessage(obtainMessage(13, imsRegistration));
    }

    private void handleDeregistering(ImsRegistration imsRegistration) {
        super.onDeregistering(imsRegistration);
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "handleDeregistering " + IMSLog.checker(imsRegistration));
        int handle = imsRegistration.getHandle();
        if (Mno.fromName(imsRegistration.getImsProfile().getMnoName()).isOneOf(Mno.TMOUS, Mno.DISH) && imsRegistration.getDeregiReason() == 11) {
            Log.i(str, "TMO_E911, deregReason is MOVE_NEXT_PCSCF, just return");
        } else if (isRunning()) {
            removeMessages(9);
            if (imsRegistration.getImsProfile().getCmcType() == 2 && this.mCmcServiceModule.getSessionCountByCmcType(imsRegistration.getPhoneId(), imsRegistration.getImsProfile().getCmcType()) > 0 && imsRegistration.getDeregiReason() == 2) {
                this.mCmcServiceModule.startCmcHandoverTimer(imsRegistration);
            } else {
                this.mImsCallSessionManager.endCallByDeregistered(imsRegistration);
            }
            clearDialogList(imsRegistration.getPhoneId(), handle);
        }
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "onDeregistered");
        IMSLog.c(LogClass.VOLTE_DEREGISTERED, imsRegistration.getPhoneId() + "," + i);
        this.mCmcServiceModule.onDeregistered(imsRegistration, i);
        this.mLastRegiErrorCode[imsRegistration.getPhoneId()] = i;
        removeMessages(12);
        sendMessage(obtainMessage(12, i, 0, imsRegistration));
    }

    private void handleDeregistered(ImsRegistration imsRegistration, int i) {
        super.onDeregistered(imsRegistration, i);
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "handleDeregistered");
        if (ImsCallUtil.isCmcPrimaryType(imsRegistration.getImsProfile().getCmcType())) {
            this.mCmcMediaController.disconnectToSve();
        }
        this.mImsCallSessionManager.handleDeregistered(this.mContext, imsRegistration.getPhoneId(), i, Mno.fromName(imsRegistration.getImsProfile().getMnoName()));
        if (imsRegistration.getImsProfile().hasEmergencySupport()) {
            Log.i(str, "Deregistered emergency profile = " + i + ", reason = " + imsRegistration.getDeregiReason());
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("Emergency Deregistered reason " + i + " with handle " + imsRegistration.getHandle());
            if (this.mEcbmMode[imsRegistration.getPhoneId()]) {
                return;
            }
            if (Mno.fromName(imsRegistration.getImsProfile().getMnoName()) == Mno.ATT && i != 200 && i != 1606) {
                Log.i(str, "Do not stopEmergencyRegistration It's ATT and error Code is not 200 nor 1606");
                this.mEventLog.add("Do not stopEmergencyRegistration It's ATT and error Code is not 200");
            } else if (!Mno.fromName(imsRegistration.getImsProfile().getMnoName()).isOneOf(Mno.TMOUS, Mno.DISH) || imsRegistration.getDeregiReason() != 11) {
                this.mRegMan.stopEmergencyRegistration(imsRegistration.getPhoneId());
            } else {
                Log.i(str, "TMO_E911, deregReason is MOVE_NEXT_PCSCF, just return");
            }
        } else {
            Log.i(str, "Deregistered from VOLTE service. reason " + i);
            int handle = imsRegistration.getHandle();
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("Deregistered reason " + i + " with handle " + handle);
            if (isRunning()) {
                if (imsRegistration.getImsProfile().getCmcType() == 2 && this.mCmcServiceModule.getSessionCountByCmcType(imsRegistration.getPhoneId(), imsRegistration.getImsProfile().getCmcType()) > 0 && imsRegistration.getDeregiReason() == 2) {
                    this.mCmcServiceModule.startCmcHandoverTimer(imsRegistration);
                } else {
                    this.mImsCallSessionManager.endCallByDeregistered(imsRegistration);
                }
                clearDialogList(imsRegistration.getPhoneId(), handle);
            }
        }
    }

    public void onNetworkChanged(NetworkEvent networkEvent, int i) {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "onNetworkChanged: " + networkEvent);
        NetworkEvent networkEvent2 = this.mNetworks.get(Integer.valueOf(i));
        if (!(networkEvent == null || networkEvent2 == null || networkEvent.network == networkEvent2.network)) {
            IMSLog.c(LogClass.VOLTE_RAT_CHANGE, i + "," + this.mNetworks.get(Integer.valueOf(i)).network + "->" + networkEvent.network);
        }
        this.mNetworks.put(Integer.valueOf(i), networkEvent);
        removeMessages(9);
        sendMessage(obtainMessage(9, 100, i));
    }

    private void tryDisconnect(int i, int i2) {
        int i3 = this.mNetworks.get(Integer.valueOf(i2)).network;
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "tryDisconnect(" + i2 + ") delay " + i);
        ImsRegistration imsRegistration = getImsRegistration(i2);
        if (imsRegistration != null) {
            Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            if (fromName.isKor() && TelephonyManagerExt.getNetworkClass(i3) == 2) {
                Log.i(str, "to do nothing");
            } else if (fromName == Mno.ATT && imsRegistration.getImsProfile().isSoftphoneEnabled() && i3 != 0) {
                Log.i(str, "to do nothing");
            } else if (hasActiveCall(i2) && this.mPdnController.isEpdgConnected(i2) && this.mPdnController.isWifiConnected()) {
                Log.i(str, "to do nothing - Continue Wifi call");
            } else if (ImsCallUtil.isMultiPdnRat(i3)) {
                if (this.mRegMan.isSuspended(imsRegistration.getHandle())) {
                    if (i > 2000) {
                        Log.e(str, "isSuspended(), waited enough...");
                    } else {
                        Log.e(str, "isSuspended(), retrying...");
                        sendMessageDelayed(obtainMessage(9, i * 2, i2), (long) i);
                        return;
                    }
                }
                this.mRatChanged[i2] = true;
                this.mImsCallSessionManager.endcallByNwHandover(imsRegistration);
            }
        }
    }

    public int getParticipantIdForMerge(int i, int i2) {
        return this.mImsCallSessionManager.getParticipantIdForMerge(i, i2);
    }

    public CallProfile createCallProfile(int i, int i2) {
        CallProfile callProfile = new CallProfile();
        callProfile.setCallType(i2);
        return callProfile;
    }

    public ImsRegistration getRegInfo(int i) {
        if (i == -1) {
            return getImsRegistration();
        }
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration next = it.next();
            if (i == next.getImsRegi().getHandle()) {
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str, "getRegInfo: found regId=" + next.getImsRegi().getHandle());
                return next.getImsRegi();
            }
        }
        return getImsRegistration();
    }

    public ImsCallSession createSession(CallProfile callProfile) throws RemoteException {
        return this.mImsCallSessionManager.createSession(this.mContext, callProfile, callProfile == null ? null : getImsRegistration(callProfile.getPhoneId()));
    }

    public ImsCallSession createSession(CallProfile callProfile, int i) throws RemoteException {
        return this.mImsCallSessionManager.createSession(this.mContext, callProfile, getRegInfo(i));
    }

    public void updateCmcP2pList(ImsRegistration imsRegistration, CallProfile callProfile) {
        this.mCmcServiceModule.updateCmcP2pList(imsRegistration, callProfile);
    }

    public synchronized int sendRttSessionModifyRequest(int i, boolean z) {
        return this.mImsCallSessionManager.sendRttSessionModifyRequest(i, z);
    }

    public void setAutomaticMode(int i, boolean z) {
        boolean[] zArr = this.mAutomaticMode;
        boolean z2 = zArr[i];
        zArr[i] = z;
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "setAutomaticMode: " + z2 + " -> " + z);
        if (z2 == z) {
            Log.e(str, "setAutomaticMode: ignored");
        } else {
            this.mVolteSvcIntf.setAutomaticMode(i, z);
        }
    }

    public boolean getAutomaticMode() {
        return getAutomaticMode(this.mActiveDataPhoneId);
    }

    public boolean getAutomaticMode(int i) {
        return this.mAutomaticMode[i];
    }

    public void setOutOfService(boolean z, int i) {
        this.mVolteSvcIntf.setOutOfService(z, i);
    }

    public synchronized void sendRttSessionModifyResponse(int i, boolean z) {
        this.mImsCallSessionManager.sendRttSessionModifyResponse(i, z);
    }

    public IImsMediaController getImsMediaController() {
        return this.mMediaController;
    }

    public ICmcMediaController getCmcMediaController() {
        return this.mCmcMediaController;
    }

    public ImsCallSession getPendingSession(String str) {
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "getPendingSession: callId " + str);
        try {
            if (!TextUtils.isEmpty(str)) {
                return this.mImsCallSessionManager.getSessionByCallId(Integer.parseInt(str));
            }
            return null;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean getExtMoCall() {
        return this.mImsCallSessionManager.getExtMoCall();
    }

    public void registerForVolteServiceEvent(int i, IVolteServiceEventListener iVolteServiceEventListener) {
        this.mVolteNotifier.registerForVolteServiceEvent(i, iVolteServiceEventListener);
    }

    public void deRegisterForVolteServiceEvent(int i, IVolteServiceEventListener iVolteServiceEventListener) {
        this.mVolteNotifier.deRegisterForVolteServiceEvent(i, iVolteServiceEventListener);
    }

    public void registerRttEventListener(int i, IRttEventListener iRttEventListener) {
        this.mVolteNotifier.registerRttEventListener(i, iRttEventListener);
    }

    public void unregisterRttEventListener(int i, IRttEventListener iRttEventListener) {
        this.mVolteNotifier.unregisterRttEventListener(i, iRttEventListener);
    }

    public void registerDialogEventListener(int i, IDialogEventListener iDialogEventListener) {
        this.mVolteNotifier.registerDialogEventListener(i, iDialogEventListener);
    }

    public void unregisterDialogEventListener(int i, IDialogEventListener iDialogEventListener) {
        this.mVolteNotifier.unregisterDialogEventListener(i, iDialogEventListener);
    }

    public void registerForCallStateEvent(IImsCallEventListener iImsCallEventListener) {
        registerForCallStateEvent(this.mActiveDataPhoneId, iImsCallEventListener);
    }

    public void registerForCallStateEvent(int i, IImsCallEventListener iImsCallEventListener) {
        this.mVolteNotifier.registerForCallStateEvent(i, iImsCallEventListener);
    }

    public void deregisterForCallStateEvent(IImsCallEventListener iImsCallEventListener) {
        deregisterForCallStateEvent(this.mActiveDataPhoneId, iImsCallEventListener);
    }

    public void deregisterForCallStateEvent(int i, IImsCallEventListener iImsCallEventListener) {
        this.mVolteNotifier.deregisterForCallStateEvent(i, iImsCallEventListener);
    }

    public void registerCmcRecordingListener(int i, ISemCmcRecordingListener iSemCmcRecordingListener) {
        this.mVolteNotifier.registerCmcRecordingListener(i, iSemCmcRecordingListener);
    }

    public void unregisterCmcRecordingListener(int i, ISemCmcRecordingListener iSemCmcRecordingListener) {
        this.mVolteNotifier.unregisterCmcRecordingListener(i, iSemCmcRecordingListener);
    }

    public void setUiTTYMode(int i, int i2, Message message) {
        Messenger messenger;
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "setUiTTYMode: phoneId = " + i + ", mode = " + i2 + ", do nothing");
        if (message != null && (messenger = message.replyTo) != null) {
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int getTtyMode() {
        return this.mTtyMode[this.mActiveDataPhoneId];
    }

    public int getTtyMode(int i) {
        return this.mTtyMode[i];
    }

    public boolean isRttCall(int i) {
        return this.mImsCallSessionManager.isRttCall(i);
    }

    public void setRttMode(int i) {
        super.setRttMode(i);
    }

    public void setRttMode(int i, int i2) {
        super.setRttMode(i, i2);
    }

    public int getRttMode() {
        return this.mRttMode[this.mActiveDataPhoneId];
    }

    public int getRttMode(int i) {
        return this.mRttMode[i];
    }

    public void sendRttMessage(String str) {
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "sendRttMessage: " + str);
        this.mImsCallSessionManager.sendRttMessage(str);
    }

    public synchronized void onSendRttSessionModifyRequest(int i, boolean z) {
        int phoneIdByCallId = this.mImsCallSessionManager.getPhoneIdByCallId(i);
        if (phoneIdByCallId == -1) {
            phoneIdByCallId = this.mActiveDataPhoneId;
        }
        getSessionByCallId(i).getCallProfile().getMediaProfile().setRttMode(z ? 1 : 0);
        this.mVolteNotifier.onSendRttSessionModifyRequest(phoneIdByCallId, getSessionByCallId(i), z);
    }

    public synchronized void onSendRttSessionModifyResponse(int i, boolean z, boolean z2) {
        int phoneIdByCallId = this.mImsCallSessionManager.getPhoneIdByCallId(i);
        if (phoneIdByCallId == -1) {
            phoneIdByCallId = this.mActiveDataPhoneId;
        }
        getSessionByCallId(i).getCallProfile().getMediaProfile().setRttMode(z == z2 ? 1 : 0);
        this.mVolteNotifier.onSendRttSessionModifyResponse(phoneIdByCallId, getSessionByCallId(i), z, z2);
    }

    private void onCallStatusChange(int i, int i2) {
        Mno mno;
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration == null) {
            mno = SimUtil.getSimMno(i);
        } else {
            mno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        }
        if (mno == Mno.VZW && i2 == 0) {
            this.mSsacManager.sendMessage(obtainMessage(i2, Integer.valueOf(i)));
        }
        IRegistrationManager iRegistrationManager = this.mRegMan;
        if (iRegistrationManager != null) {
            iRegistrationManager.updateTelephonyCallStatus(i, i2);
        }
        IConfigModule configModule = ImsRegistry.getConfigModule();
        if (configModule != null) {
            configModule.updateTelephonyCallStatus(i, i2);
        }
        EntitlementConfigService.updateTelephonyCallStatus(i, i2);
    }

    private void onImsDialogEvent(DialogEvent dialogEvent) {
        if (dialogEvent == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "ignoring dialog list is null");
            return;
        }
        ImsRegistration regInfo = getRegInfo(dialogEvent.getRegId());
        boolean z = true;
        if (!(regInfo == null || regInfo.getImsProfile() == null)) {
            if (Mno.fromName(regInfo.getImsProfile().getMnoName()) == Mno.VZW) {
                for (Dialog dialog : dialogEvent.getDialogList()) {
                    if (dialog.isExclusive()) {
                        dialog.setIsPullAvailable(false);
                        Log.i(IVolteServiceModuleInternal.LOG_TAG, "Exclusive call can't pulling");
                    } else if (dialog.isHeld()) {
                        dialog.setIsPullAvailable(false);
                        Log.i(IVolteServiceModuleInternal.LOG_TAG, "Held call can't pulling");
                    } else if (dialog.isVideoPortZero()) {
                        dialog.setIsPullAvailable(true);
                        dialog.setCallType(1);
                        Log.i(IVolteServiceModuleInternal.LOG_TAG, "Downgraded video call can pulling and change callType to Voice");
                    } else if (ImsCallUtil.isVideoCall(dialog.getCallType()) && dialog.getVideoDirection() == 1) {
                        dialog.setIsPullAvailable(false);
                        Log.i(IVolteServiceModuleInternal.LOG_TAG, "Backgrounded Video call can't pulling");
                    } else if (!ImsCallUtil.isVideoCall(dialog.getCallType()) || regInfo.hasService("mmtel-video")) {
                        dialog.setIsPullAvailable(true);
                    } else {
                        dialog.setIsPullAvailable(false);
                        Log.i(IVolteServiceModuleInternal.LOG_TAG, "video call can't pulling with video feature");
                    }
                }
            }
            if (ImsCallUtil.isCmcPrimaryType(regInfo.getImsProfile().getCmcType())) {
                Log.i(IVolteServiceModuleInternal.LOG_TAG, "Ignore DialogEvent");
                return;
            } else if (ImsCallUtil.isCmcSecondaryType(regInfo.getImsProfile().getCmcType())) {
                dialogEvent = this.mCmcServiceModule.onCmcImsDialogEvent(regInfo, dialogEvent);
                if (regInfo.getImsProfile().getCmcType() == 2 && ImsRegistry.getCmcAccountManager().isSupportSameWiFiOnly() && !this.mCmcServiceModule.isP2pDiscoveryDone()) {
                    Log.i(IVolteServiceModuleInternal.LOG_TAG, "Do not notify dialog event until P2P discovery done");
                    this.mCmcServiceModule.setNeedToNotifyAfterP2pDiscovery(true);
                    z = false;
                }
            }
        }
        this.mLastDialogEvent[dialogEvent.getPhoneId()] = dialogEvent;
        if (z) {
            this.mVolteNotifier.notifyOnDialogEvent(dialogEvent);
            SecImsNotifier.getInstance().onDialogEvent(dialogEvent);
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "Last Notified Dialog Event : " + IMSLog.checker(this.mLastDialogEvent[dialogEvent.getPhoneId()]));
        }
    }

    private void onEcbmStateChanged(int i, boolean z) {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "onEcbmStateChanged: ecbm=" + z + " oldEcbm[" + i + "]=" + this.mEcbmMode[i]);
        boolean[] zArr = this.mEcbmMode;
        boolean z2 = zArr[i];
        zArr[i] = z;
        IMSLog.c(LogClass.VOLTE_CHANGE_ECBM, i + "," + z2 + "," + this.mEcbmMode[i]);
        if (!this.mEcbmMode[i] && z2) {
            this.mRegMan.stopEmergencyRegistration(i);
        }
    }

    private void onScreenOnOffChanged(int i) {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "onScreenOnOffChanged: on =" + i);
        IVolteServiceInterface iVolteServiceInterface = this.mVolteSvcIntf;
        if (iVolteServiceInterface != null) {
            iVolteServiceInterface.updateScreenOnOff(this.mActiveDataPhoneId, i);
        }
    }

    public void updateSSACInfo(int i, int i2, int i3, int i4, int i5) {
        updateSSACInfo(i, new SsacInfo(i2, i3, i4, i5));
    }

    private void updateSSACInfo(int i, SsacInfo ssacInfo) {
        SsacManager ssacManager = this.mSsacManager;
        if (ssacManager == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "mSsacManager was not exist!");
        } else {
            ssacManager.updateSSACInfo(i, ssacInfo);
        }
    }

    private void onUpdateSSACInfo(int i, BarringInfo barringInfo) {
        updateSSACInfo(i, new SsacInfo(barringInfo));
    }

    public void updateAudioInterface(int i, int i2) {
        ImsRegistration imsRegistration;
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "updateAudioInterface, phoneId :" + i + ", direction: " + i2);
        String audioMode = ImsCallUtil.getAudioMode(i2);
        boolean z = false;
        if (i2 == 5 || i2 == 8) {
            imsRegistration = this.mCmcServiceModule.updateAudioInterfaceByCmc(i, i2);
        } else {
            imsRegistration = getImsRegistration(i);
            if (imsRegistration == null && this.mCmcServiceModule.isCmcRegExist(i)) {
                for (int i3 = 2; i3 <= 8; i3 += 2) {
                    imsRegistration = this.mCmcServiceModule.getCmcRegistration(i, false, i3);
                    if (imsRegistration != null) {
                        break;
                    }
                }
            }
        }
        if (imsRegistration == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "There is no IMS Registration take Emergency Regi");
            imsRegistration = getImsRegistration(i, true);
        }
        if (imsRegistration != null) {
            if (i2 == 4) {
                Iterator<ImsCallSession> it = this.mImsCallSessionManager.getEmergencySession(i).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ImsCallSession next = it.next();
                    if (next.getCallState() == CallConstants.STATE.InCall && next.getPhoneId() == i) {
                        IUserAgent emergencyUa = getEmergencyUa(i);
                        Log.i(IVolteServiceModuleInternal.LOG_TAG, "Emergency session. Invoke updateAudioInterface() with UserAgent");
                        this.mVolteSvcIntf.updateAudioInterface(0, audioMode, (UserAgent) emergencyUa);
                        z = true;
                        break;
                    }
                }
                if (!z) {
                    this.mVolteSvcIntf.updateAudioInterface(imsRegistration.getHandle(), audioMode);
                }
            } else {
                this.mVolteSvcIntf.updateAudioInterface(imsRegistration.getHandle(), audioMode);
            }
        }
        if (!"STOP".equals(audioMode)) {
            this.mImsCallSessionManager.forceNotifyCurrentCodec();
        }
    }

    private IUserAgent getEmergencyUa(int i) {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager != null) {
            return registrationManager.getUserAgentOnPdn(15, i);
        }
        return null;
    }

    public boolean isQSSSuccessAuthAndLogin(int i) {
        return super.isQSSSuccessAuthAndLogin(i);
    }

    public boolean hasQecInCall() {
        return this.mImsCallSessionManager.hasQecInCall();
    }

    public void setVideoCrtAudio(int i, boolean z) {
        ImsCallSession incomingCallSession = this.mImsCallSessionManager.getIncomingCallSession(i);
        if (incomingCallSession == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "setVideoCrtAudio() no valid incoming call session");
        } else {
            this.mVolteSvcIntf.setVideoCrtAudio(incomingCallSession.getSessionId(), z);
        }
    }

    public void sendDtmfEvent(int i, String str) {
        ImsCallSession alertingCallSession = this.mImsCallSessionManager.getAlertingCallSession(i);
        ImsCallSession incomingCallSession = this.mImsCallSessionManager.getIncomingCallSession(i);
        if (alertingCallSession != null) {
            this.mVolteSvcIntf.sendDtmfEvent(alertingCallSession.getSessionId(), str);
        } else if (incomingCallSession != null) {
            this.mVolteSvcIntf.sendDtmfEvent(incomingCallSession.getSessionId(), str);
        } else {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "sendDtmfEvent() no valid alerting or incoming call session");
        }
    }

    public void enableCallWaitingRule(boolean z) {
        this.mEnableCallWaitingRule = z;
    }

    public boolean isCallBarredBySSAC(int i, int i2) {
        if (this.mPdnController.isEpdgConnected(i)) {
            return false;
        }
        boolean isCallBarred = this.mSsacManager.isCallBarred(i, i2);
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "isCallBarredBySSAC[" + i + "]: result for call type " + i2 + " is " + isCallBarred);
        return isCallBarred;
    }

    public DialogEvent getLastDialogEvent() {
        return this.mLastDialogEvent[this.mActiveDataPhoneId];
    }

    public DialogEvent getLastDialogEvent(int i) {
        return this.mLastDialogEvent[i];
    }

    public void pushCall(int i, String str) {
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "pushCall: callId : " + i + ", targetNumber : " + IMSLog.checker(str));
        ImsCallSession sessionByCallId = getSessionByCallId(i);
        if (sessionByCallId == null) {
            Log.i(str2, "callId(" + i + ") is invalid");
            return;
        }
        this.mImsExternalCallController.pushCall(sessionByCallId, str, getImsRegistration(sessionByCallId.getPhoneId()));
    }

    public void consultativeTransferCall(int i, int i2) {
        ImsCallSession sessionByCallId = getSessionByCallId(i);
        ImsCallSession sessionByCallId2 = getSessionByCallId(i2);
        if (sessionByCallId == null) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "fgCallId(" + i + ") is invalid");
        } else if (sessionByCallId2 == null) {
            String str2 = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str2, "bgCallId(" + i2 + ") is invalid");
        } else {
            this.mImsExternalCallController.consultativeTransferCall(sessionByCallId, sessionByCallId2, getImsRegistration(sessionByCallId.getPhoneId()));
        }
    }

    public void transferCall(String str, String str2) throws RemoteException {
        this.mImsExternalCallController.transferCall(this.mActiveDataPhoneId, str, str2, this.mLastDialogEvent);
    }

    public void notifyOnPulling(int i, int i2) {
        this.mVolteNotifier.notifyOnPulling(i, i2);
    }

    public void notifyOnCmcRecordingEvent(int i, int i2, int i3, int i4) {
        this.mVolteNotifier.notifyOnCmcRecordingEvent(i, i2, i3);
        this.mCmcServiceModule.forwardCmcRecordingEventToSD(i, i2, i3, i4);
    }

    public void notifyOnCmcRelayEvent(int i, int i2, int i3) {
        ImsCallSession session;
        ImsCallSession session2;
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "notifyOnCmcRelayEvent event: " + i + " extPhoneId: " + i2 + " intSessionId: " + i3);
        if (i == 1) {
            if (hasMessages(35)) {
                removeMessages(35);
                updateAudioInterface(i2, 4);
            } else if (i2 > -1 && i3 > -1 && (session2 = getSession(i3)) != null) {
                session2.setRelayChTerminated(true);
            }
        } else if (i == 0 && i2 > -1 && i3 > -1 && (session = getSession(i3)) != null) {
            Log.i(str, "notifyOnCmcRelayEvent intSession.setRelayChTerminated(false)");
            session.setRelayChTerminated(false);
        }
    }

    /* access modifiers changed from: protected */
    public void onDedicatedBearerEvent(DedicatedBearerEvent dedicatedBearerEvent) {
        ImsCallSession session = getSession(dedicatedBearerEvent.getBearerSessionId());
        if (session == null) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "onDedicatedBearerEvent: unknown session " + dedicatedBearerEvent.getBearerSessionId());
            return;
        }
        session.setDedicatedBearerState(dedicatedBearerEvent.getQci(), dedicatedBearerEvent.getBearerState());
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "onDedicatedBearerEvent: received for session : " + session + " ,bearer state : " + dedicatedBearerEvent.getBearerState() + " ,qci : " + dedicatedBearerEvent.getQci());
        this.mVolteNotifier.onDedicatedBearerEvent(session, dedicatedBearerEvent);
    }

    /* access modifiers changed from: protected */
    public void onQuantumSecurityStatusEvent(QuantumSecurityStatusEvent quantumSecurityStatusEvent) {
        ImsCallSession session = getSession(quantumSecurityStatusEvent.getSessionId());
        if (session == null) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "onQuantumSecurityStatusEvent: unknown session " + quantumSecurityStatusEvent.getSessionId());
            return;
        }
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "onQuantumSecurityStatusEvent: received for sessionId : " + quantumSecurityStatusEvent.getSessionId() + ", event : " + quantumSecurityStatusEvent.getEvent() + ", qtSessionId : " + IMSLog.checker(quantumSecurityStatusEvent.getQtSessionId()));
        if (quantumSecurityStatusEvent.getEvent() == QuantumSecurityStatusEvent.QuantumEvent.FALLBACK_TO_NORMAL_CALL) {
            session.notifyQuantumEncryptionStatus(4);
            this.mCmcServiceModule.sendPublishDialogInternal(session.getPhoneId(), session.getCmcType());
        } else if (quantumSecurityStatusEvent.getEvent() == QuantumSecurityStatusEvent.QuantumEvent.SUCCESS) {
            session.notifyQuantumEncryptionStatus(3);
        } else if (quantumSecurityStatusEvent.getEvent() == QuantumSecurityStatusEvent.QuantumEvent.NOTIFY_SESSION_ID && !TextUtils.isEmpty(quantumSecurityStatusEvent.getQtSessionId())) {
            Log.i(str2, "QtSessionId notified by voice engine. Request session key");
            session.updateQuantumPeerProfileStatus(401, "NOTIFY_SESSION_ID", quantumSecurityStatusEvent.getQtSessionId(), "");
        }
    }

    public void setActiveImpu(int i, String str) {
        if (TextUtils.isEmpty(str)) {
            this.mActiveImpu[i] = null;
        } else {
            this.mActiveImpu[i] = ImsUri.parse(str);
        }
    }

    public ImsUri getActiveImpu() {
        return getActiveImpu(this.mActiveDataPhoneId);
    }

    public ImsUri getActiveImpu(int i) {
        ImsUri[] imsUriArr = this.mActiveImpu;
        if (imsUriArr == null) {
            return null;
        }
        return imsUriArr[i];
    }

    private void onReleaseWfcBeforeHO(int i) {
        this.mImsCallSessionManager.onReleaseWfcBeforeHO(i);
        this.mReleaseWfcBeforeHO[i] = true;
    }

    /* access modifiers changed from: protected */
    /* renamed from: onImsCallEvent */
    public void lambda$handleMessage$2(CallStateEvent callStateEvent) {
        ImsRegistration imsRegistration;
        ImsCallSession session = getSession(callStateEvent.getSessionID());
        if (session == null) {
            imsRegistration = getImsRegistration();
        } else {
            imsRegistration = getImsRegistration(session.getPhoneId());
        }
        boolean z = true;
        if (imsRegistration != null) {
            Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            if (fromName == Mno.VZW && ImsCallUtil.isImsOutageError(callStateEvent.getErrorCode())) {
                this.mProhibited[imsRegistration.getPhoneId()] = true;
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str, "onImsCallEvent: Receive 503 Outage Error session " + callStateEvent.getSessionID());
            } else if ((fromName == Mno.TELEFONICA_UK || fromName == Mno.TMOBILE) && session == null && callStateEvent.getErrorCode() != null) {
                SipError errorCode = callStateEvent.getErrorCode();
                SipError sipError = SipErrorBase.SIP_TIMEOUT;
                if (errorCode.equals(sipError)) {
                    Log.i(IVolteServiceModuleInternal.LOG_TAG, "onImsCallEvent: Notify 708 to RegiGvn even if session null");
                    IRegistrationGovernor registrationGovernor = this.mRegMan.getRegistrationGovernor(imsRegistration.getHandle());
                    if (registrationGovernor != null) {
                        registrationGovernor.onSipError("mmtel", sipError);
                        return;
                    }
                    return;
                }
            }
        }
        if (callStateEvent.getParams() != null) {
            String audioCodec = callStateEvent.getParams().getAudioCodec();
            String audioBitRate = callStateEvent.getParams().getAudioBitRate();
            if (!(callStateEvent.getState() == CallStateEvent.CALL_STATE.ENDED || callStateEvent.getState() == CallStateEvent.CALL_STATE.ERROR)) {
                z = false;
            }
            sendAudioCodecInfo(audioCodec, audioBitRate, z);
        }
        if (session == null) {
            String str2 = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str2, "onImsCallEvent: unknown session " + callStateEvent.getSessionID());
            return;
        }
        String str3 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str3, "onImsCallEvent: session=" + callStateEvent.getSessionID() + " state=" + callStateEvent.getState());
        onImsCallEventForState(imsRegistration, session, callStateEvent);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.VolteServiceModule$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE;

        /* JADX WARNING: Can't wrap try/catch for region: R(26:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|(3:25|26|28)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE[] r0 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE = r0
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CALLING     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ESTABLISHED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.MODIFIED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_LOCAL     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_REMOTE     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_BOTH     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_ADDED     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_REMOVED     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.MODIFY_REQUESTED     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ENDED     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ERROR     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.FORWARDED     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.SESSIONPROGRESS     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteServiceModule.AnonymousClass1.<clinit>():void");
        }
    }

    private void onImsCallEventForState(ImsRegistration imsRegistration, ImsCallSession imsCallSession, CallStateEvent callStateEvent) {
        ImsRegistration cmcRegistration;
        boolean z = false;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[callStateEvent.getState().ordinal()]) {
            case 1:
                updateNrSaModeOnStart(imsCallSession.getPhoneId(), callStateEvent.getSessionID());
                imsCallSession.requestQuantumPeerProfileStatus(false, false);
                break;
            case 2:
                handleIdcEvent(imsCallSession, callStateEvent);
                onImsCallEventForEstablish(imsRegistration, imsCallSession, callStateEvent);
                break;
            case 3:
            case 4:
            case 5:
            case 6:
                this.mCmcServiceModule.onImsCallEventWithHeldBoth(imsCallSession, imsRegistration);
                break;
            case 7:
            case 8:
            case 9:
                break;
            case 10:
            case 11:
                if (ImsCallUtil.isCmcSecondaryType(imsCallSession.getCmcType()) && callStateEvent.getState() == CallStateEvent.CALL_STATE.ERROR && (cmcRegistration = this.mCmcServiceModule.getCmcRegistration(imsCallSession.getPhoneId(), false, imsCallSession.getCmcType())) != null) {
                    clearDialogList(imsCallSession.getPhoneId(), cmcRegistration.getHandle());
                    break;
                }
            case 12:
                resetPeerProfileStatus(imsCallSession, callStateEvent);
                imsCallSession.requestQuantumPeerProfileStatus(false, true);
                break;
            case 13:
                handleIdcEvent(imsCallSession, callStateEvent);
                break;
        }
        z = true;
        if (z) {
            this.mVolteNotifier.notifyCallStateEvent(callStateEvent, imsCallSession);
        }
    }

    private void resetPeerProfileStatus(ImsCallSession imsCallSession, CallStateEvent callStateEvent) {
        if (imsCallSession.isQuantumEncryptionServiceAvailable()) {
            if (TextUtils.isEmpty(callStateEvent.getParams().getHistoryInfo())) {
                Log.e(IVolteServiceModuleInternal.LOG_TAG, "history info is null, Quantum Encryption disabled");
                imsCallSession.disableQuantumEncryption();
                return;
            }
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "Quantum Encrypted Call is forwarded");
            imsCallSession.getCallProfile().getQuantumSecurityInfo().setPeerProfileStatus(-1);
            imsCallSession.getCallProfile().getQuantumSecurityInfo().setEncryptStatus(0);
            imsCallSession.getCallProfile().getQuantumSecurityInfo().setQtSessionId("");
            imsCallSession.getCallProfile().getQuantumSecurityInfo().setSessionKey("");
            imsCallSession.getCallProfile().getQuantumSecurityInfo().setRemoteTelNum(callStateEvent.getParams().getHistoryInfo());
        }
    }

    private void handleIdcEvent(ImsCallSession imsCallSession, CallStateEvent callStateEvent) {
        if (isSupportImsDataChannel(imsCallSession.getPhoneId()) && imsCallSession.getIdcData() != null) {
            boolean z = true;
            boolean z2 = !TextUtils.isEmpty(callStateEvent.getIdcExtra().getString(IdcExtra.Key.SDP));
            boolean z3 = callStateEvent.getState() == CallStateEvent.CALL_STATE.SESSIONPROGRESS && z2;
            if (!(callStateEvent.getState() == CallStateEvent.CALL_STATE.ESTABLISHED && imsCallSession.getIdcData().getCurrentState() == IdcImsCallSessionData.State.NEGOTIATING)) {
                z = z3;
            }
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "[IDC] handleIdcEvent existSdp=" + z2 + ", needToNotify=" + z);
            if (z) {
                this.mIdcServiceModule.setBootstrapRemoteAnswerSdp(imsCallSession.getIdcData().getTelecomCallId(), callStateEvent.getIdcExtra());
                postDelayed(new VolteServiceModule$$ExternalSyntheticLambda0(this, imsCallSession), 1000);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: checkIdcNegotiated */
    public void lambda$handleIdcEvent$0(ImsCallSession imsCallSession) {
        if (imsCallSession != null && imsCallSession.getIdcData() != null) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "[IDC] checkIdcNegotiated IdcState=" + imsCallSession.getIdcData().getCurrentState());
            if (imsCallSession.getIdcData().getCurrentState() == IdcImsCallSessionData.State.NEGOTIATING) {
                imsCallSession.sendNegotiatedLocalSdp(IdcImsCallSessionData.NO_DATA);
            }
        }
    }

    public boolean isProhibited(int i) {
        return this.mProhibited[i];
    }

    /* access modifiers changed from: protected */
    public void onRtpLossRateNoti(RtpLossRateNoti rtpLossRateNoti) {
        this.mVolteNotifier.notifyOnRtpLossRate(this.mActiveDataPhoneId, rtpLossRateNoti);
    }

    public void onCallEnded(int i, int i2, int i3) {
        int i4 = i;
        int i5 = i2;
        int i6 = i3;
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "onCallEnded[" + i4 + "]: sessionId " + i5 + ", error=" + i6);
        Mno simMno = SimUtil.getSimMno(i);
        if (simMno == Mno.KDDI && hasEmergencyCall(i) && isEmergencyRegistered(i)) {
            startEpdnDisconnectTimer(i4, 240000);
        }
        if (!(i5 != -1 || i6 == 911 || i6 == 0)) {
            Log.e(str, "Stack Return -1 release all session in F/W layer");
            this.mImsCallSessionManager.releaseAllSession(i4);
        }
        this.mCmcServiceModule.onCallEndedWithSendPublish(i4, getSession(i5));
        ImsCallSession removeSession = this.mImsCallSessionManager.removeSession(i5);
        if (removeSession != null) {
            if (removeSession.getCmcType() == 0 && simMno.isChn()) {
                notifyDSDAVideoCapa(i);
            }
            onCallSessionEnded(removeSession, simMno);
            CallProfile callProfile = removeSession.getCallProfile();
            if (removeSession.isQuantumEncryptionServiceAvailable()) {
                int i7 = callProfile.getQuantumSecurityInfo().getEncryptStatus() == 3 ? 1 : 0;
                IQuantumEncryptionServiceModule quantumEncryptionServiceModule = getServiceModuleManager().getQuantumEncryptionServiceModule();
                SxHangUpEntity sxHangUpEntity = r7;
                SxHangUpEntity sxHangUpEntity2 = new SxHangUpEntity(callProfile.getQuantumSecurityInfo().getLocalPhoneNumber(), callProfile.getQuantumSecurityInfo().getRemoteTelNum(), callProfile.getDirection(), callProfile.getQuantumSecurityInfo().getQtSessionId(), i7, 0, "success", callProfile.getSipCallId());
                quantumEncryptionServiceModule.onHangUp(sxHangUpEntity);
            }
        }
        if (this.mReleaseWfcBeforeHO[i4] && getSessionCount(i) == 0) {
            Log.i(str, "All calls are release before HO, trigger HO to EPDG");
            if (this.mWfcEpdgMgr.isEpdgServiceConnected()) {
                this.mWfcEpdgMgr.getEpdgMgr().triggerHOAfterReleaseCall(i4);
            }
            this.mReleaseWfcBeforeHO[i4] = false;
        }
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (!(imsRegistration == null || Mno.fromName(imsRegistration.getImsProfile().getMnoName()) == Mno.VZW)) {
            this.mMediaController.startCameraForActiveExcept(i5);
        }
        this.mImsCallSessionManager.onCallEnded(i4);
    }

    private void onCallSessionEnded(ImsCallSession imsCallSession, Mno mno) {
        IRegistrationGovernor registrationGovernor;
        int sessionId = imsCallSession.getSessionId();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("Call End - " + sessionId + "(" + imsCallSession.getCallId() + ") Reason(" + imsCallSession.getEndType() + " - " + imsCallSession.getEndReason() + "), Error(" + imsCallSession.getErrorCode() + " - " + imsCallSession.getErrorMessage() + ") " + this);
        int callType = imsCallSession.getCallProfile().getCallType();
        boolean z = callType == 7 || callType == 8;
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str, "onCallEnded: callType: " + callType + ", isEmergency: " + z);
        if (mno == Mno.KDDI && z && this.mRegMan.isEpdnRequestPending(imsCallSession.getPhoneId())) {
            Log.i(str, "EPDN request is still pending, need to stop EPDN to avoid retry");
            this.mRegMan.stopEmergencyPdnOnly(imsCallSession.getPhoneId());
        }
        this.mVolteNotifier.notifyCallStateEvent(new CallStateEvent(CallStateEvent.CALL_STATE.ENDED), imsCallSession);
        ImsRegistration registration = imsCallSession.getRegistration();
        if (!(registration == null || hasActiveCall(imsCallSession.getPhoneId()) || (registrationGovernor = this.mRegMan.getRegistrationGovernor(registration.getHandle())) == null)) {
            registrationGovernor.onCallStatus(IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END, (SipError) null, imsCallSession.getCallProfile().getCallType());
        }
        if (this.mRegMan.isVoWiFiSupported(imsCallSession.getPhoneId()) && isVowifiEnabled(imsCallSession.getPhoneId()) && getTotalCallCount(imsCallSession.getPhoneId()) == 0) {
            WiFiManagerExt.setImsCallEstablished(this.mContext, false);
        }
        if (ImsCallUtil.isCmcPrimaryType(imsCallSession.getCmcType()) && imsCallSession.getErrorCode() == 6007) {
            int cmcBoundSessionId = imsCallSession.getCallProfile().getCmcBoundSessionId();
            Log.i(str, "call end due to call pull from SD to PD. bound session id = " + cmcBoundSessionId);
            if (cmcBoundSessionId > 0) {
                ImsCallSession session = getSession(cmcBoundSessionId);
                if (session != null) {
                    if (imsCallSession.getRelayChTerminated()) {
                        Log.i(str, "Relay ch already terminated. Start audio here");
                        updateAudioInterface(session.getPhoneId(), 4);
                    } else {
                        Log.i(str, "Relay ch not terminated yet. Delay start audio");
                        sendMessageDelayed(obtainMessage(35, -1, session.getPhoneId()), 500);
                    }
                }
            } else {
                Log.i(str, "Ext session is CS");
            }
        }
        if (ImsRegistry.getICmcConnectivityController().isEnabledWifiDirectFeature() && imsCallSession.getCmcType() == 0) {
            ImsRegistry.getICmcConnectivityController().stopP2p();
        }
    }

    public void onConferenceParticipantAdded(int i, String str) {
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "onConferenceParticipantAdded: sessionId " + i);
        ImsCallSession session = getSession(i);
        if (session != null && session.getCallProfile().isConferenceCall()) {
            CallStateEvent callStateEvent = new CallStateEvent(CallStateEvent.CALL_STATE.CONFERENCE_ADDED);
            callStateEvent.addUpdatedParticipantsList(str, 0, 0, 0);
            this.mVolteNotifier.notifyCallStateEvent(callStateEvent, session);
        }
    }

    public void onConferenceParticipantRemoved(int i, String str) {
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "onConferenceParticipantRemoved: sessionId " + i);
        ImsCallSession session = getSession(i);
        if (session != null && session.getCallProfile().isConferenceCall()) {
            CallStateEvent callStateEvent = new CallStateEvent(CallStateEvent.CALL_STATE.CONFERENCE_REMOVED);
            callStateEvent.addUpdatedParticipantsList(str, 0, 0, 0);
            this.mVolteNotifier.notifyCallStateEvent(callStateEvent, session);
        }
    }

    public void onUpdateGeolocation() {
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "onUpdateGeolocation: ");
        this.mImsCallSessionManager.onUpdateGeolocation();
    }

    public void handleMessage(Message message) {
        boolean z = true;
        if (this.mCheckRunningState || isRunning()) {
            super.handleMessage(message);
            switch (message.what) {
                case 1:
                    post(new VolteServiceModule$$ExternalSyntheticLambda1(this, (IncomingCallEvent) ((AsyncResult) message.obj).result));
                    return;
                case 2:
                    postDelayed(new VolteServiceModule$$ExternalSyntheticLambda2(this, (CallStateEvent) ((AsyncResult) message.obj).result), 50);
                    return;
                case 3:
                    onImsDialogEvent((DialogEvent) ((AsyncResult) message.obj).result);
                    return;
                case 5:
                    onCallStatusChange(message.arg1, message.arg2);
                    return;
                case 6:
                    if (!DeviceUtil.getGcfMode()) {
                        int i = message.arg1;
                        if (message.arg2 != 1) {
                            z = false;
                        }
                        onEcbmStateChanged(i, z);
                        return;
                    }
                    return;
                case 8:
                    onDedicatedBearerEvent((DedicatedBearerEvent) ((AsyncResult) message.obj).result);
                    return;
                case 9:
                    tryDisconnect(message.arg1, message.arg2);
                    return;
                case 10:
                    this.mImsCallSessionManager.onCallEndByCS(message.arg1);
                    return;
                case 11:
                    onImsIncomingCallEvent((IncomingCallEvent) message.obj, true);
                    return;
                case 12:
                    handleDeregistered((ImsRegistration) message.obj, message.arg1);
                    return;
                case 13:
                    handleDeregistering((ImsRegistration) message.obj);
                    return;
                case 14:
                    ImsCallSessionManager imsCallSessionManager = this.mImsCallSessionManager;
                    if (message.arg1 != 1) {
                        z = false;
                    }
                    imsCallSessionManager.onPSBarred(z);
                    return;
                case 15:
                    onImsDialogEvent((DialogEvent) message.obj);
                    return;
                case 16:
                    if (getSessionCount(message.arg1) > 0 && hasEmergencyCall(message.arg1)) {
                        return;
                    }
                    if (isEmergencyRegistered(message.arg1)) {
                        this.mRegMan.stopEmergencyRegistration(message.arg1);
                        return;
                    } else {
                        this.mRegMan.stopEmergencyPdnOnly(message.arg1);
                        return;
                    }
                case 17:
                    onDtmfInfo((DtmfInfo) ((AsyncResult) message.obj).result);
                    return;
                case 18:
                    onRtpLossRateNoti((RtpLossRateNoti) ((AsyncResult) message.obj).result);
                    return;
                case 19:
                    this.mImsCallSessionManager.handleEpdnSetupFail(message.arg1);
                    return;
                case 20:
                    onReleaseWfcBeforeHO(message.arg1);
                    return;
                case 21:
                    onConfigUpdated(message.arg1, (String) message.obj);
                    return;
                case 22:
                    onTextInfo((TextInfo) ((AsyncResult) message.obj).result);
                    return;
                case 23:
                    onScreenOnOffChanged(message.arg1);
                    return;
                case 24:
                    onSimSubscribeIdChanged((SubscriptionInfo) ((AsyncResult) message.obj).result);
                    return;
                case 25:
                    this.mImsCallSessionManager.getSIPMSGInfo((SIPDataEvent) ((AsyncResult) message.obj).result);
                    return;
                case 26:
                    onActiveDataSubscriptionChanged();
                    return;
                case 27:
                    onSrvccStateChange(message.arg1, (Call.SrvccState) message.obj);
                    return;
                case 28:
                    int activeDataPhoneId = getActiveDataPhoneId();
                    if (message.arg1 != 1) {
                        z = false;
                    }
                    onIQIServiceStateChanged(activeDataPhoneId, z);
                    return;
                case 30:
                    int intValue = ((Integer) ((AsyncResult) message.obj).result).intValue();
                    onEventSimReady(intValue);
                    registerMissedSmsReceiver(true, intValue);
                    return;
                case 31:
                    Log.i(IVolteServiceModuleInternal.LOG_TAG, "Removed Call State set to Idle");
                    AsyncResult asyncResult = (AsyncResult) message.obj;
                    onCallStatusChange(((Integer) asyncResult.result).intValue(), 0);
                    registerMissedSmsReceiver(false, ((Integer) asyncResult.result).intValue());
                    resetQuantumAuthStatus(((Integer) asyncResult.result).intValue());
                    return;
                case 35:
                    Log.i(IVolteServiceModuleInternal.LOG_TAG, "Delay audio engine timer expired. Start now. phoneId: " + message.arg2);
                    updateAudioInterface(message.arg2, 4);
                    return;
                case 36:
                    this.mImsCallSessionManager.onUssdEndByCS(message.arg1);
                    return;
                case 37:
                    onUpdateSSACInfo(message.arg1, (BarringInfo) message.obj);
                    return;
                case 38:
                    onQuantumSecurityStatusEvent((QuantumSecurityStatusEvent) ((AsyncResult) message.obj).result);
                    return;
                default:
                    return;
            }
        } else {
            this.mCheckRunningState = true;
            sendMessageDelayed(Message.obtain(message), 500);
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "VolteServiceModule not ready, retransmitting event " + message.what);
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$handleMessage$1(IncomingCallEvent incomingCallEvent) {
        onImsIncomingCallEvent(incomingCallEvent, false);
    }

    /* access modifiers changed from: protected */
    public void onConfigUpdated(int i, String str) {
        String str2 = IVolteServiceModuleInternal.LOG_TAG;
        Log.i(str2, "onConfigUpdated[" + i + "] : " + str);
        if ("VOLTE_ENABLED".equalsIgnoreCase(str) || "LVC_ENABLED".equalsIgnoreCase(str)) {
            onServiceSwitched(i, (ContentValues) null);
        }
    }

    private void onDtmfInfo(DtmfInfo dtmfInfo) {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "isBikeMode", 0) == 1) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "BikeMode Active - Dtmf Val = " + dtmfInfo.getEvent());
            Intent intent = new Intent("com.samsung.ims.DTMF_RX_DIGI");
            intent.putExtra("dtmf_digit", dtmfInfo.getEvent());
            intent.setPackage(ImsConstants.Packages.PACKAGE_BIKE_MODE);
            this.mContext.sendBroadcast(intent);
            return;
        }
        Log.i(IVolteServiceModuleInternal.LOG_TAG, "Bike Mode is disabled discarding event");
    }

    private synchronized void onTextInfo(TextInfo textInfo) {
        this.mVolteNotifier.notifyOnRttEvent(this.mActiveDataPhoneId, textInfo);
    }

    public boolean isVolteRetryRequired(int i, int i2, SipError sipError) {
        return isVolteRetryRequired(i, i2, sipError, 10);
    }

    public boolean isVolteRetryRequired(int i, int i2, SipError sipError, int i3) {
        Mno mno;
        boolean z;
        if (sipError == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "SipError was null!!");
            return false;
        } else if (!isSilentRedialEnabled(this.mContext, i)) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "isSilentRedialEnabled was false!");
            return false;
        } else {
            ImsRegistration imsRegistration = getImsRegistration(i);
            if (imsRegistration == null) {
                mno = SimManagerFactory.getSimManager().getSimMno();
            } else {
                mno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            }
            try {
                String[] stringArray = ImsRegistry.getStringArray(i, GlobalSettingsConstants.Call.ALL_VOLTE_RETRY_ERROR_CODE_LIST, (String[]) null);
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str, "all_volte_retry_error_code_list " + Arrays.asList(stringArray));
                z = this.mImsCallSessionManager.isMatchWithErrorCodeList(stringArray, sipError.getCode());
                if (!z) {
                    try {
                        if (ImsCallUtil.isVideoCall(i2)) {
                            String[] stringArray2 = ImsRegistry.getStringArray(i, GlobalSettingsConstants.Call.VIDEO_VOLTE_RETRY_ERROR_CODE_LIST, (String[]) null);
                            Log.i(str, "video_volte_retry_error_code_list " + Arrays.asList(stringArray2));
                            z = this.mImsCallSessionManager.isMatchWithErrorCodeList(stringArray2, sipError.getCode());
                        }
                    } catch (JSONException unused) {
                    }
                }
            } catch (JSONException unused2) {
                z = false;
            }
            if (mno != Mno.DOCOMO || this.mPdnController.getEmcBsIndication(i) == EmcBsIndication.SUPPORTED) {
                return z;
            }
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "do not volte retry under eb not supported N/W");
            return false;
        }
    }

    public int getSignalLevel() {
        return getSignalLevel(this.mActiveDataPhoneId);
    }

    public int getSignalLevel(int i) {
        return this.mMobileCareController.getSignalLevel(i);
    }

    public ImsUri getNormalizedUri(int i, String str) {
        UriGenerator uriGenerator = UriGeneratorFactory.getInstance().get(i, UriGenerator.URIServiceType.VOLTE_URI);
        if (uriGenerator == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "getNormalizedUri: FATAL - no UriGenerator found.");
            return null;
        }
        ImsUri normalizedUri = uriGenerator.getNormalizedUri(str);
        if (normalizedUri == null) {
            String str2 = IVolteServiceModuleInternal.LOG_TAG;
            Log.e(str2, "getNormalizedUri: invalid msisdn=" + IMSLog.checker(str));
            return null;
        }
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration == null) {
            Log.e(IVolteServiceModuleInternal.LOG_TAG, "getNormalizedUri: not registered!!");
            return null;
        }
        for (NameAddr nameAddr : imsRegistration.getImpuList()) {
            if (normalizedUri.equals(uriGenerator.getNormalizedUri(UriUtil.getMsisdnNumber(nameAddr.getUri())))) {
                return nameAddr.getUri();
            }
        }
        return null;
    }

    private void onIQIServiceStateChanged(int i, boolean z) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "onXqServiceStateChanged: " + z);
        if (this.mImsXqReporter != null) {
            boolean z2 = z && ImsXqReporter.isXqEnabled(this.mContext, i);
            if (z2) {
                this.mImsXqReporter.start();
            } else {
                this.mImsXqReporter.stop();
            }
            IVolteServiceInterface iVolteServiceInterface = this.mVolteSvcIntf;
            if (iVolteServiceInterface != null) {
                iVolteServiceInterface.updateXqEnable(this.mActiveDataPhoneId, z2);
            }
        }
    }

    public ImsCallInfo[] getImsCallInfos(int i) {
        ArrayList arrayList = new ArrayList();
        Iterator<ImsCallSession> it = getSessionList(i).iterator();
        while (it.hasNext()) {
            ImsCallSession next = it.next();
            Iterator<ImsCallSession> it2 = it;
            ImsCallInfo imsCallInfo = r3;
            ImsCallInfo imsCallInfo2 = new ImsCallInfo(next.getCallId(), next.getCallProfile().getCallType(), next.getCallProfile().isDowngradedVideoCall(), next.getCallProfile().isDowngradedAtEstablish(), next.getDedicatedBearerState(1), next.getDedicatedBearerState(2), next.getDedicatedBearerState(8), next.getErrorCode(), next.getErrorMessage(), next.getCallProfile().getDialingNumber(), next.getCallProfile().getDirection(), next.getCallProfile().isConferenceCall());
            arrayList.add(imsCallInfo);
            it = it2;
        }
        return (ImsCallInfo[]) arrayList.toArray(new ImsCallInfo[0]);
    }

    public int getVoWIFIEmergencyCallRat(int i) {
        for (ImsCallSession next : this.mImsCallSessionManager.getEmergencySession()) {
            if (i == next.getPhoneId() && ImsConstants.EmergencyRat.IWLAN.equalsIgnoreCase(next.getCallProfile().getEmergencyRat())) {
                return 18;
            }
        }
        return -1;
    }

    public void sendCmcRecordingEvent(int i, int i2, SemCmcRecordingInfo semCmcRecordingInfo) {
        getCmcMediaController().sendCmcRecordingEvent(i, i2, semCmcRecordingInfo);
    }

    public CmcCallInfo getCmcCallInfo() {
        return getCmcServiceHelper().getCmcCallInfo();
    }

    public ImsCallSessionManager getImsCallSessionManager() {
        return this.mImsCallSessionManager;
    }

    public IdcServiceHelper getIdcServiceHelper() {
        return this.mIdcServiceModule;
    }

    public void sendHandOffEvent(int i, int i2, int i3, int i4, long j) {
        String str = IVolteServiceModuleInternal.LOG_TAG;
        Log.e(str, "sendEPSFBEvent: event " + i2 + " sRat: " + i3 + "  dRat: " + i4);
        Mno simMno = SimUtil.getSimMno(i);
        if (this.mEcholocateController == null) {
            return;
        }
        if ((!simMno.isOneOf(Mno.TMOUS, Mno.DISH) && simMno != Mno.SPRINT) || !hasActiveCall(i)) {
            return;
        }
        if (getForegroundSession(i) != null) {
            this.mEcholocateController.handleTmoEcholocatePSHO(i, i2, i3, i4, j);
        } else {
            this.mEcholocateController.handleTmoEcholocateEPSFB(i, i2, j);
        }
    }

    public int getIncomingSessionPhoneIdForCmc() {
        return this.mImsCallSessionManager.getIncomingSessionPhoneIdForCmc();
    }

    private void sendAudioCodecInfo(String str, String str2, boolean z) {
        Intent intent = new Intent("com.samsung.ims.imsservice.handler.secims.audio_info");
        intent.putExtra("IS_ENDCALL", z);
        intent.putExtra("CODEC_NAME", str);
        intent.putExtra("BIT_RATE", str2);
        IntentUtil.sendBroadcast(this.mContext, intent);
    }

    public boolean hasPendingCall(int i) {
        return this.mImsCallSessionManager.hasPendingCall(i);
    }

    public void notifyEpsFallbackResult(int i, int i2) {
        if (i2 == 1) {
            setIsLteRetrying(i, true);
        }
        this.mImsCallSessionManager.endcallBeforeRetry(i, i2);
    }

    public void onSrvccStateChange(int i, Call.SrvccState srvccState) {
        super.onSrvccStateChange(i, srvccState);
    }

    private void resetQuantumAuthStatus(int i) {
        if (i == 0) {
            getServiceModuleManager().getQuantumEncryptionServiceModule().resetAuthStatus();
        }
    }
}
