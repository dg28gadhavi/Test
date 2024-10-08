package com.sec.internal.google;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsRegistrationAttributes;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.feature.RcsFeature;
import android.telephony.ims.stub.CapabilityExchangeEventListener;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imsphone.ImsConfigImpl;
import com.sec.internal.imsphone.ImsMultiEndPointImpl;
import com.sec.internal.imsphone.ImsRegistrationImpl;
import com.sec.internal.imsphone.MmTelFeatureImpl;
import com.sec.internal.imsphone.RcsFeatureImpl;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SecImsNotifier {
    private static final String LOG_TAG = "SecImsNotifier";
    private static SecImsNotifier sInstance;
    private final SimpleEventLog mEventLog;
    protected final SecImsServiceConnector mSecImsServiceConnector;

    private SecImsNotifier() {
        Context context = ImsRegistry.getContext();
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 500);
        this.mSecImsServiceConnector = new SecImsServiceConnector(context);
    }

    public static SecImsNotifier getInstance() {
        if (sInstance == null) {
            sInstance = new SecImsNotifier();
        }
        return sInstance;
    }

    public void notifyImsRegistration(ImsRegistration imsRegistration, boolean z, ImsRegistrationError imsRegistrationError) {
        int phoneId = imsRegistration.getPhoneId();
        ImsRegistrationImpl imsRegistrationImpl = this.mSecImsServiceConnector.getImsRegistrationImpl(phoneId);
        if (imsRegistrationImpl == null) {
            IMSLog.i(LOG_TAG, phoneId, "notifyImsRegistration: SecImsService not ready");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "notifyImsRegistration: registered: " + z + ", prev registered: " + imsRegistrationImpl.isRegistered());
        if (!imsRegistration.getImsProfile().hasEmergencySupport() && (imsRegistration.hasVolteService() || (ImsUtil.isSingleRegiAppConnected(phoneId) && z))) {
            if (z) {
                imsRegistrationImpl.onRegistered(convertToImsRegAttribute(imsRegistration));
                Uri[] extractOwnUrisFromReg = extractOwnUrisFromReg(imsRegistration);
                IMSLog.s(LOG_TAG, phoneId, "notifyImsRegistration: ownUris: " + Arrays.toString(extractOwnUrisFromReg));
                imsRegistrationImpl.onSubscriberAssociatedUriChanged(extractOwnUrisFromReg);
                imsRegistrationImpl.setRegistered();
            } else {
                imsRegistrationImpl.onDeregistered(new ImsReasonInfo(imsRegistrationError.getSipErrorCode(), imsRegistrationError.getDeregistrationReason(), imsRegistrationError.getSipErrorReason()), 0, getRegistrationTech(imsRegistration.getCurrentRat(), imsRegistration.isEpdgOverCellularData()));
                imsRegistrationImpl.setNotRegistered();
            }
        }
        if (ImsUtil.isSingleRegiAppConnected(phoneId) && ImsProfile.hasRcsService(imsRegistration.getImsProfile())) {
            this.mSecImsServiceConnector.getSipTransportImpl(phoneId).onRegistrationChanged(imsRegistration, z);
        }
    }

    private ImsRegistrationAttributes convertToImsRegAttribute(ImsRegistration imsRegistration) {
        return new ImsRegistrationAttributes.Builder(getRegistrationTech(imsRegistration.getCurrentRat(), imsRegistration.isEpdgOverCellularData())).setFeatureTags(getRegisteredFeatureTags(imsRegistration.getPhoneId())).build();
    }

    private int getRegistrationTech(int i, boolean z) {
        if (NetworkUtil.is3gppPsVoiceNetwork(i)) {
            return 0;
        }
        if (TelephonyManagerExt.getNetworkClass(i) == 2) {
            return 4;
        }
        if (i == 18) {
            return z ? 2 : 1;
        }
        return -1;
    }

    private Set<String> getRegisteredFeatureTags(int i) {
        return this.mSecImsServiceConnector.getSipTransportImpl(i).getRegisteredFeatureTags();
    }

    public void updateMmTelCapabilities(int i, MmTelFeature.MmTelCapabilities mmTelCapabilities) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "updateCapabilities: capabilities=" + mmTelCapabilities);
        MmTelFeatureImpl mmTelFeatureImpl = this.mSecImsServiceConnector.getMmTelFeatureImpl(i);
        if (mmTelFeatureImpl == null) {
            IMSLog.i(LOG_TAG, i, "updateRcsCapabilities: SecImsService not ready");
        } else if (mmTelFeatureImpl.queryCapabilityStatus().getMask() != mmTelCapabilities.getMask()) {
            try {
                mmTelFeatureImpl.notifyCapabilitiesStatusChanged(mmTelCapabilities);
            } catch (IllegalArgumentException | IllegalStateException e) {
                IMSLog.e(LOG_TAG, i, "notifyCapabilitiesStatusChanged: failed: " + e.getMessage());
            }
        }
    }

    public void updateRcsCapabilities(int i, RcsFeature.RcsImsCapabilities rcsImsCapabilities) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "updateRcsCapabilities: capabilities=" + rcsImsCapabilities);
        RcsFeatureImpl rcsFeatureImpl = this.mSecImsServiceConnector.getRcsFeatureImpl(i);
        if (rcsFeatureImpl == null) {
            IMSLog.i(LOG_TAG, i, "updateRcsCapabilities: SecImsService not ready");
        } else if (rcsFeatureImpl.queryCapabilityStatus().getMask() != rcsImsCapabilities.getMask()) {
            try {
                rcsFeatureImpl.notifyCapabilitiesStatusChanged(rcsImsCapabilities);
            } catch (IllegalArgumentException | IllegalStateException e) {
                IMSLog.e(LOG_TAG, i, "notifyCapabilitiesStatusChanged: failed: " + e.getMessage());
            }
        }
    }

    public void onRequestPublishCapabilities(int i, int i2) {
        this.mEventLog.logAndAdd(i, "onRequestPublishCapabilities");
        RcsFeatureImpl rcsFeatureImpl = this.mSecImsServiceConnector.getRcsFeatureImpl(i);
        if (rcsFeatureImpl == null) {
            IMSLog.i(LOG_TAG, i, "onRequestPublishCapabilities: SecImsService not ready");
        } else {
            rcsFeatureImpl.onRequestPublishCapabilities(i2);
        }
    }

    public void onUnPublish(int i) {
        this.mEventLog.logAndAdd(i, "onUnPublish");
        RcsFeatureImpl rcsFeatureImpl = this.mSecImsServiceConnector.getRcsFeatureImpl(i);
        if (rcsFeatureImpl == null) {
            IMSLog.i(LOG_TAG, i, "onUnPublish: SecImsService not ready");
        } else {
            rcsFeatureImpl.onUnPublish();
        }
    }

    public void onPublishUpdated(int i, int i2, String str, int i3, String str2) {
        this.mEventLog.logAndAdd(i, "onPublishUpdated");
        RcsFeatureImpl rcsFeatureImpl = this.mSecImsServiceConnector.getRcsFeatureImpl(i);
        if (rcsFeatureImpl == null) {
            IMSLog.i(LOG_TAG, i, "onPublishUpdated: SecImsService not ready");
        } else {
            rcsFeatureImpl.onPublishUpdated(i2, str, i3, str2);
        }
    }

    public void onRemoteCapabilityRequest(int i, Uri uri, Set<String> set, CapabilityExchangeEventListener.OptionsRequestCallback optionsRequestCallback) {
        this.mEventLog.logAndAdd(i, "onRemoteCapabilityRequest");
        RcsFeatureImpl rcsFeatureImpl = this.mSecImsServiceConnector.getRcsFeatureImpl(i);
        if (rcsFeatureImpl == null) {
            IMSLog.i(LOG_TAG, i, "onRemoteCapabilityRequest: SecImsService not ready");
        } else {
            rcsFeatureImpl.onRemoteCapabilityRequest(uri, set, optionsRequestCallback);
        }
    }

    public void notifyRcsAutoConfigurationReceived(int i, byte[] bArr, boolean z) {
        this.mEventLog.logAndAdd(i, String.format(Locale.US, "notifyAcsReceived: size [%d], compressed [%s]", new Object[]{Integer.valueOf(bArr.length), Boolean.valueOf(z)}));
        ImsConfigImpl imsConfigImpl = this.mSecImsServiceConnector.getImsConfigImpl(i);
        if (imsConfigImpl == null) {
            this.mEventLog.logAndAdd(i, "notifyAcsReceived: SecImsService not ready");
            return;
        }
        try {
            imsConfigImpl.getIImsConfig().notifyRcsAutoConfigurationReceived(bArr, z);
        } catch (RemoteException unused) {
            this.mEventLog.logAndAdd(i, "notifyRcsAutoConfigurationReceived: RemoteException");
        }
    }

    public void notifyRcsAutoConfigurationRemoved(int i) {
        this.mEventLog.logAndAdd(i, "notifyAcsRemoved");
        ImsConfigImpl imsConfigImpl = this.mSecImsServiceConnector.getImsConfigImpl(i);
        if (imsConfigImpl == null) {
            this.mEventLog.logAndAdd(i, "notifyAcsRemoved: SecImsService not ready");
            return;
        }
        try {
            imsConfigImpl.getIImsConfig().notifyRcsAutoConfigurationRemoved();
        } catch (RemoteException unused) {
            this.mEventLog.logAndAdd(i, "notifyRcsAutoConfigurationRemoved: RemoteException");
        }
    }

    public void notifyRcsAutoConfigurationErrorReceived(int i, int i2, String str) {
        this.mEventLog.logAndAdd(i, String.format(Locale.US, "notifyAcsErrorReceived: error [%d %s]", new Object[]{Integer.valueOf(i2), str}));
        ImsConfigImpl imsConfigImpl = this.mSecImsServiceConnector.getImsConfigImpl(i);
        if (imsConfigImpl == null) {
            this.mEventLog.logAndAdd(i, "notifyAcsErrorReceived: SecImsService not ready");
        } else {
            imsConfigImpl.notifyAutoConfigurationErrorReceived(i2, str);
        }
    }

    public void notifyRcsPreConfigurationReceived(int i, byte[] bArr) {
        this.mEventLog.logAndAdd(i, "notifyRcsPreConfigurationReceived");
        ImsConfigImpl imsConfigImpl = this.mSecImsServiceConnector.getImsConfigImpl(i);
        if (imsConfigImpl == null) {
            this.mEventLog.logAndAdd(i, "notifyRcsPreConfigurationReceived: SecImsService not ready");
        } else {
            imsConfigImpl.notifyPreProvisioningReceived(bArr);
        }
    }

    public void notifyProvisionedIntValueChanged(int i, int i2, int i3) {
        this.mEventLog.logAndAdd(i, String.format(Locale.US, "notifyProvisionedIntValueChanged: item=%d value=%d", new Object[]{Integer.valueOf(i2), Integer.valueOf(i3)}));
        ImsConfigImpl imsConfigImpl = this.mSecImsServiceConnector.getImsConfigImpl(i);
        if (imsConfigImpl == null) {
            this.mEventLog.logAndAdd(i, "notifyRcsPreConfigurationReceived: SecImsService not ready");
        } else {
            imsConfigImpl.notifyProvisionedValueChanged(i2, i3);
        }
    }

    public void notifyProvisionedStringValueChanged(int i, int i2, String str) {
        this.mEventLog.logAndAdd(i, String.format(Locale.US, "notifyProvisionedStringValueChanged: item=%d value=%s", new Object[]{Integer.valueOf(i2), str}));
        ImsConfigImpl imsConfigImpl = this.mSecImsServiceConnector.getImsConfigImpl(i);
        if (imsConfigImpl == null) {
            this.mEventLog.logAndAdd(i, "notifyProvisionedStringValueChanged: SecImsService not ready");
        } else {
            imsConfigImpl.notifyProvisionedValueChanged(i2, str);
        }
    }

    public String getRcsClientConfiguration(int i, int i2) {
        this.mEventLog.logAndAdd(i, String.format(Locale.US, "getRcsClientConfiguration: item=%d", new Object[]{Integer.valueOf(i2)}));
        ImsConfigImpl imsConfigImpl = this.mSecImsServiceConnector.getImsConfigImpl(i);
        if (imsConfigImpl != null) {
            return imsConfigImpl.getRcsClientConfiguration(i2);
        }
        this.mEventLog.logAndAdd(i, "notifyProvisionedStringValueChanged: SecImsService not ready");
        return null;
    }

    public void notifyIntImsConfigChanged(int i, int i2, int i3) {
        this.mEventLog.logAndAdd(i, String.format(Locale.US, "notifyIntImsConfigChanged: item=%d value=%d", new Object[]{Integer.valueOf(i2), Integer.valueOf(i3)}));
        ImsConfigImpl imsConfigImpl = this.mSecImsServiceConnector.getImsConfigImpl(i);
        if (imsConfigImpl == null) {
            this.mEventLog.logAndAdd(i, "notifyIntImsConfigChanged: SecImsService not ready");
            return;
        }
        try {
            imsConfigImpl.getIImsConfig().notifyIntImsConfigChanged(i2, i3);
        } catch (RemoteException unused) {
            this.mEventLog.logAndAdd(i, "notifyIntImsConfigChanged: RemoteException");
        }
    }

    public void notifyStringImsConfigChanged(int i, int i2, String str) {
        this.mEventLog.logAndAdd(i, String.format(Locale.US, "notifyStringImsConfigChanged: item=%d value=%s", new Object[]{Integer.valueOf(i2), str}));
        ImsConfigImpl imsConfigImpl = this.mSecImsServiceConnector.getImsConfigImpl(i);
        if (imsConfigImpl == null) {
            this.mEventLog.logAndAdd(i, "notifyStringImsConfigChanged: SecImsService not ready");
            return;
        }
        try {
            imsConfigImpl.getIImsConfig().notifyStringImsConfigChanged(i2, str);
        } catch (RemoteException unused) {
            this.mEventLog.logAndAdd(i, "notifyStringImsConfigChanged: RemoteException");
        }
    }

    public void notifySipMessage(int i, SipMsg sipMsg) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "notifySipMessage: " + sipMsg);
        this.mSecImsServiceConnector.getSipTransportImpl(i).notifySipMessage(sipMsg);
    }

    public void updateAdhocProfile(int i, ImsProfile imsProfile, boolean z) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "updateAdhocProfile: " + imsProfile);
        this.mSecImsServiceConnector.getSipTransportImpl(i).updateAdhocProfile(imsProfile, z);
    }

    private Uri[] extractOwnUrisFromReg(ImsRegistration imsRegistration) {
        return (imsRegistration == null || CollectionUtils.isNullOrEmpty((Collection<?>) imsRegistration.getImpuList())) ? new Uri[0] : (Uri[]) imsRegistration.getImpuList().stream().filter(new SecImsNotifier$$ExternalSyntheticLambda0()).map(new SecImsNotifier$$ExternalSyntheticLambda1()).toArray(new SecImsNotifier$$ExternalSyntheticLambda2());
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ Uri[] lambda$extractOwnUrisFromReg$2(int i) {
        return new Uri[i];
    }

    public void onIncomingCall(int i, int i2) {
        if (!SemSystemProperties.getBoolean("net.sip.vzthirdpartyapi", false)) {
            MmTelFeatureImpl mmTelFeatureImpl = this.mSecImsServiceConnector.getMmTelFeatureImpl(i);
            if (mmTelFeatureImpl == null) {
                IMSLog.i(LOG_TAG, i, "onIncomingCall: MmTelFeatureImpl not ready");
                return;
            }
            Intent intent = new Intent();
            try {
                IImsCallSession sessionByCallId = mmTelFeatureImpl.mVolteServiceModule.getSessionByCallId(i2);
                if (sessionByCallId != null) {
                    CallProfile callProfile = sessionByCallId.getCallProfile();
                    if (callProfile.getHistoryInfo() != null) {
                        intent.putExtra("com.samsung.telephony.extra.SEM_EXTRA_FORWARDED_CALL", callProfile.getHistoryInfo());
                    }
                    if (callProfile.getCallType() == 12) {
                        intent.putExtra("android.telephony.ims.feature.extra.IS_USSD", true);
                    }
                    if (!TextUtils.isEmpty(callProfile.getEchoCallId())) {
                        intent.putExtra("com.samsung.ims.extra.ECHO_CALL_ID", callProfile.getEchoCallId());
                        intent.putExtra("com.samsung.ims.extra.EPSFB_SUCCESS", callProfile.getEPSFBsuccess());
                    }
                    if (!TextUtils.isEmpty(callProfile.getEchoCellId())) {
                        intent.putExtra("com.samsung.ims.extra.ECHO_CELL_ID", callProfile.getEchoCellId());
                    }
                    mmTelFeatureImpl.getCmcImsServiceUtil().postProcessForCmcIncomingCall(i, intent, sessionByCallId);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mmTelFeatureImpl.onIncomingCall(i2, intent.getExtras());
        }
    }

    public void onIncomingPreAlerting(int i, ImsCallInfo imsCallInfo) {
        MmTelFeatureImpl mmTelFeatureImpl = this.mSecImsServiceConnector.getMmTelFeatureImpl(i);
        if (mmTelFeatureImpl == null) {
            IMSLog.i(LOG_TAG, i, "onIncomingCall: MmTelFeatureImpl not ready");
            return;
        }
        int callId = imsCallInfo.getCallId();
        if (mmTelFeatureImpl.mVolteServiceModule.getPendingSession(Integer.toString(callId)) != null) {
            onIncomingCall(i, callId);
        }
    }

    public void onDialogEvent(DialogEvent dialogEvent) {
        MmTelFeatureImpl mmTelFeatureImpl = this.mSecImsServiceConnector.getMmTelFeatureImpl(dialogEvent.getPhoneId());
        if (mmTelFeatureImpl == null) {
            IMSLog.i(LOG_TAG, dialogEvent.getPhoneId(), "onDialogEvent: MmTelFeatureImpl not ready");
            return;
        }
        try {
            ImsMultiEndPointImpl multiEndpointInterface = mmTelFeatureImpl.getMultiEndpointInterface();
            if (multiEndpointInterface != null) {
                multiEndpointInterface.setDialogInfo(dialogEvent, mmTelFeatureImpl.getCmcTypeFromRegHandle(dialogEvent.getRegId()));
                multiEndpointInterface.requestImsExternalCallStateInfo();
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onP2pRegCompleteEvent(int i) {
        Log.d(LOG_TAG, "onP2pRegCompleteEvent");
        final MmTelFeatureImpl mmTelFeatureImpl = this.mSecImsServiceConnector.getMmTelFeatureImpl(i);
        if (mmTelFeatureImpl == null) {
            IMSLog.i(LOG_TAG, i, "onP2pRegCompleteEvent: MmTelFeatureImpl not ready");
        } else if (mmTelFeatureImpl.isEnabledWifiDirectFeature()) {
            new Handler().post(new Runnable() {
                public void run() {
                    try {
                        mmTelFeatureImpl.getCmcImsServiceUtil().createP2pCallSession();
                    } catch (RemoteException unused) {
                    }
                }
            });
        }
    }

    public void onP2pPushCallEvent(final DialogEvent dialogEvent) {
        Log.d(LOG_TAG, "onP2pPushCallEvent");
        final MmTelFeatureImpl mmTelFeatureImpl = this.mSecImsServiceConnector.getMmTelFeatureImpl(dialogEvent.getPhoneId());
        if (mmTelFeatureImpl == null) {
            IMSLog.i(LOG_TAG, dialogEvent.getPhoneId(), "onP2pPushCallEvent: MmTelFeatureImpl not ready");
        } else {
            new Handler().post(new Runnable() {
                public void run() {
                    try {
                        mmTelFeatureImpl.preparePushCall(dialogEvent);
                    } catch (RemoteException unused) {
                    }
                }
            });
        }
    }

    public void onTriggerEpsFallback(int i, int i2) {
        Log.d(LOG_TAG, "onTriggerEpsFallback: reason=" + i2);
        MmTelFeatureImpl mmTelFeatureImpl = this.mSecImsServiceConnector.getMmTelFeatureImpl(i);
        if (mmTelFeatureImpl == null) {
            IMSLog.i(LOG_TAG, i, "onTriggerEpsFallback: SecImsService not ready");
            return;
        }
        try {
            mmTelFeatureImpl.onTriggerEpsFallback(i2);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasSipDelegate(int i) {
        return this.mSecImsServiceConnector.getSipTransportImpl(i).hasSipDelegate();
    }

    public List<String> getSipDelegateServiceList(int i) {
        List<String> serviceList = this.mSecImsServiceConnector.getSipTransportImpl(i).getServiceList();
        serviceList.add(SipMsg.EVENT_PRESENCE);
        return serviceList;
    }
}
