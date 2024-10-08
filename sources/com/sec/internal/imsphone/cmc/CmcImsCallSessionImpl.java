package com.sec.internal.imsphone.cmc;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.imsphone.DataTypeConvertor;
import com.sec.internal.imsphone.ImsCallSessionImpl;
import com.sec.internal.imsphone.ImsVideoCallProviderImpl;
import com.sec.internal.imsphone.MmTelFeatureImpl;
import com.sec.internal.log.IMSLog;

public class CmcImsCallSessionImpl extends ImsCallSessionImpl {
    private static final String LOG_TAG = "CmcImsCallSessionImpl";
    private CmcCallSessionManager mP2pCSM;

    public CmcImsCallSessionImpl(ImsCallProfile imsCallProfile, CmcCallSessionManager cmcCallSessionManager, IImsCallSessionListener iImsCallSessionListener, MmTelFeatureImpl mmTelFeatureImpl) {
        super(imsCallProfile, (IImsCallSession) null, iImsCallSessionListener, mmTelFeatureImpl);
        this.mP2pCSM = cmcCallSessionManager;
        if (cmcCallSessionManager.getMainSession() == null) {
            Log.e(LOG_TAG, "mainSession is null");
            this.mImsVideoCallProvider = null;
            this.mCallId = "1";
            this.mCallIdInt = 1;
            return;
        }
        initP2pImpl();
    }

    public void initP2pImpl() {
        if (this.mImsVideoCallProvider != null) {
            Log.d(LOG_TAG, "initP2pImpl(), already impl is initialized, just return");
            return;
        }
        try {
            Log.d(LOG_TAG, "initP2pImpl()");
            this.mSession = this.mP2pCSM.getMainSession();
            this.mP2pCSM.registerSessionEventListener(this.mVolteEventListener);
            this.mVolteServiceModule.registerRttEventListener(this.mP2pCSM.getPhoneId(), this.mRttEventListener);
            int callId = this.mP2pCSM.getCallId();
            this.mCallIdInt = callId;
            if (callId > 0) {
                this.mCallId = Integer.toString(callId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mImsVideoCallProvider = new ImsVideoCallProviderImpl(this.mP2pCSM.getMainSession());
    }

    public CmcCallSessionManager getCmcCallSessionManager() {
        return this.mP2pCSM;
    }

    public String getProperty(String str) {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.getProperty(str);
    }

    public CallConstants.STATE getInternalState() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.getInternalState();
    }

    public CallConstants.STATE getPrevInternalState() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.getPrevInternalState();
    }

    public boolean isInCall() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.isInCall();
    }

    public void start(String str, ImsCallProfile imsCallProfile) throws RemoteException {
        int i;
        int i2;
        if (this.mVolteServiceModule == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionInitiatingFailed(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        this.mState = 1;
        this.mCallProfile = imsCallProfile;
        setCallProfile(3);
        CallProfile callProfile = this.mP2pCSM.getCallProfile();
        int phoneId = this.mP2pCSM.getPhoneId();
        callProfile.setDialingNumber(str);
        if (isEmergencyCall()) {
            this.mCallProfile.setCallExtraInt("android.telephony.ims.extra.CALL_NETWORK_TYPE", ServiceStateWrapper.rilRadioTechnologyToNetworkType(this.mP2pCSM.getCallProfile().getRadioTech()));
        }
        this.mVolteServiceModule.setAutomaticMode(phoneId, this.mCallProfile.mMediaProfile.isRttCall());
        callProfile.getMediaProfile().setRttMode(this.mCallProfile.mMediaProfile.getRttMode());
        if (this.mCallProfile.getCallExtraBoolean("CallPull")) {
            Bundle bundle = this.mCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
            DialogEvent lastDialogEvent = this.mVolteServiceModule.getLastDialogEvent(this.mSession.getPhoneId());
            if (!(lastDialogEvent == null || bundle == null)) {
                int i3 = bundle.getInt("android.telephony.ImsExternalCallTracker.extra.EXTERNAL_CALL_ID");
                for (Dialog dialog : lastDialogEvent.getDialogList()) {
                    if (dialog != null) {
                        if (SimUtil.getSimMno(this.mSession.getPhoneId()) == Mno.VZW) {
                            i2 = ImsCallUtil.getIdForString(dialog.getSipCallId());
                        } else {
                            try {
                                i2 = Integer.parseInt(dialog.getDialogId());
                            } catch (NumberFormatException unused) {
                                continue;
                            }
                        }
                        if (i3 == i2 && !TextUtils.isEmpty(dialog.getSipCallId()) && !TextUtils.isEmpty(dialog.getSipLocalTag()) && !TextUtils.isEmpty(dialog.getSipRemoteTag())) {
                            this.mCallProfile.mCallType = DataTypeConvertor.convertToGoogleCallType(dialog.getCallType());
                            callProfile.setCallType(dialog.getCallType());
                            callProfile.setPullCall(true);
                            try {
                                this.mSession.pulling(lastDialogEvent.getMsisdn(), dialog);
                                return;
                            } catch (RemoteException unused2) {
                                i = 1015;
                            }
                        }
                    }
                }
            }
            i = 101;
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionInitiatingFailed(new ImsReasonInfo(i, 0));
                return;
            }
            return;
        }
        try {
            if (this.mP2pCSM.start(str, callProfile) < 0) {
                throw new RemoteException("start return -1");
            }
        } catch (RemoteException unused3) {
            IImsCallSessionListener iImsCallSessionListener3 = this.mListener;
            if (iImsCallSessionListener3 != null) {
                iImsCallSessionListener3.callSessionInitiatingFailed(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void accept(int i, ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.accept(i, imsStreamMediaProfile);
    }

    public void reject(int i) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.reject(i);
    }

    public void terminate(int i) throws RemoteException {
        try {
            if (this.mP2pCSM.getCallProfile().getCallType() == 12) {
                this.mP2pCSM.getMainSession().info(3, "1");
            } else if (!this.mP2pCSM.terminate(DataTypeConvertor.convertCallEndReasonFromFW(i))) {
                this.mListener.callSessionTerminated(new ImsReasonInfo(501, 200));
            }
        } catch (RemoteException unused) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionTerminated(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void hold(ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.hold(imsStreamMediaProfile);
    }

    public void resume(ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.resume(imsStreamMediaProfile);
    }

    public void sendDtmf(char c, Message message) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendDtmf(c, message);
    }

    public void startDtmf(char c) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.startDtmf(c);
    }

    public void stopDtmf() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.stopDtmf();
    }

    public void sendUssd(String str) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendUssd(str);
    }

    public boolean isMultiparty() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.isMultiparty();
    }

    public void updateCmcCallExtras(CallProfile callProfile) throws RemoteException {
        Bundle bundle;
        if (this.mCallProfile.mCallExtras.containsKey("android.telephony.ims.extra.OEM_EXTRAS")) {
            bundle = this.mCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
            if (bundle != null) {
                bundle.remove("com.samsung.telephony.extra.CMC_CS_DTMF_KEY");
                this.mCallProfile.mCallExtras.remove("android.telephony.ims.extra.OEM_EXTRAS");
            }
        } else {
            bundle = null;
        }
        if (bundle == null) {
            bundle = new Bundle();
        }
        int cmcType = this.mP2pCSM.getMainSession().getCmcType();
        int sessionId = this.mP2pCSM.getSessionId();
        if (ImsCallUtil.isP2pPrimaryType(cmcType)) {
            cmcType = 1;
        } else if (ImsCallUtil.isCmcSecondaryType(cmcType)) {
            cmcType = 2;
        }
        Log.i(LOG_TAG, "updateCmcCallExtras(), SEM_EXTRA_CMC_TYPE: (" + this.mP2pCSM.getMainSession().getCmcType() + " -> " + cmcType + ")");
        bundle.putInt("com.samsung.telephony.extra.CMC_TYPE", cmcType);
        bundle.putInt("com.samsung.telephony.extra.CMC_SESSION_ID", sessionId);
        if (cmcType == 1) {
            bundle.putString("com.samsung.telephony.extra.CMC_DIAL_TO", callProfile.getDialingNumber());
            int cmcDtmfKey = callProfile.getCmcDtmfKey();
            if (cmcDtmfKey > -1 && cmcDtmfKey < 12) {
                bundle.putString("com.samsung.telephony.extra.CMC_CS_DTMF_KEY", Character.toString((cmcDtmfKey < 0 || cmcDtmfKey > 9) ? cmcDtmfKey == 10 ? '*' : cmcDtmfKey == 11 ? '#' : 0 : (char) (cmcDtmfKey + 48)));
            }
        } else if (ImsCallUtil.isCmcSecondaryType(cmcType)) {
            bundle.putString("com.samsung.telephony.extra.CMC_PD_CALL_CONNECT_TIME", callProfile.getCmcCallTime());
            if (ImsRegistry.getCmcAccountManager().isSupportDualSimCMC()) {
                bundle.putInt("com.samsung.telephony.extra.CMC_EXTERNAL_CALL_SLOT", callProfile.getCmcEdCallSlot());
            }
        }
        String cmcDeviceId = callProfile.getCmcDeviceId();
        if (!TextUtils.isEmpty(callProfile.getReplaceSipCallId())) {
            bundle.putString("com.samsung.telephony.extra.CMC_DEVICE_ID_BY_SD", cmcDeviceId);
            IMSLog.c(LogClass.CMC_OEM_EXTRAS_TO_FW, cmcType + "," + sessionId + "," + callProfile.getCmcCallTime() + ",CMC_DEVICE_ID_BY_SD");
        } else if (cmcDeviceId != null) {
            bundle.putString("com.samsung.telephony.extra.CMC_DEVICE_ID", cmcDeviceId);
            IMSLog.c(LogClass.CMC_OEM_EXTRAS_TO_FW, cmcType + "," + sessionId + "," + callProfile.getCmcCallTime() + ",CMC_DEVICE_ID");
        }
        this.mCallProfile.mCallExtras.putBundle("android.telephony.ims.extra.OEM_EXTRAS", bundle);
    }

    public void updateCallProfile() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.updateCallProfile();
    }

    public void sendRttMessage(String str) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendRttMessage(str);
    }

    public void sendRttModifyRequest(ImsCallProfile imsCallProfile) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendRttModifyRequest(imsCallProfile);
    }

    public void sendRttModifyResponse(boolean z) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendRttModifyResponse(z);
    }

    public void transfer(String str, boolean z) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.transfer(str, z);
    }

    public void consultativeTransfer(com.android.ims.internal.IImsCallSession iImsCallSession) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.consultativeTransfer(iImsCallSession);
    }

    public void cancelTransferCall() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.cancelTransferCall();
    }

    public void sendImsCallEvent(String str, Bundle bundle) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendImsCallEvent(str, bundle);
    }
}
