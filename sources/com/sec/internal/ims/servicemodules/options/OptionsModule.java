package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.interfaces.ims.servicemodules.options.IOptionsModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OptionsModule extends ServiceModuleBase implements IOptionsModule, ICapabilityExchangeControl {
    protected static final int EVT_CAPABILITIES_UPDATE = 1;
    private static final String LOG_TAG = "OptionsModule";
    Context mContext = null;
    OptionsRequestController mController;
    private PhoneIdKeyMap<Boolean> mIsOptionsEnabled = new PhoneIdKeyMap<>(SimUtil.getPhoneCount(), Boolean.FALSE);
    ICapabilityEventListener mListener;

    public void deRegisterService(List<String> list, int i) {
    }

    public void handleIntent(Intent intent) {
    }

    public void readConfig(int i) {
    }

    public void registerService(String str, String str2, int i) {
    }

    public void reset(int i) {
    }

    public OptionsModule(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
    }

    public void registerCapabilityEventListener(ICapabilityEventListener iCapabilityEventListener) {
        this.mListener = iCapabilityEventListener;
    }

    public String getName() {
        return OptionsModule.class.getSimpleName();
    }

    public String[] getServicesRequiring() {
        return new String[]{"options"};
    }

    public void init() {
        super.init();
        Log.i(LOG_TAG, "init");
        OptionsRequestController optionsRequestController = new OptionsRequestController(getLooper(), this.mContext);
        this.mController = optionsRequestController;
        optionsRequestController.registerOptionsEvent(new OptionsModule$$ExternalSyntheticLambda0(this));
        this.mController.init();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$init$0(OptionsEvent optionsEvent) {
        sendMessage(obtainMessage(1, optionsEvent));
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        super.onRegistered(imsRegistration);
        this.mController.setImsRegistration(imsRegistration);
        this.mIsOptionsEnabled.put(imsRegistration.getPhoneId(), Boolean.TRUE);
        Log.i(LOG_TAG, "onRegistered: Options service is enabled.");
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        super.onDeregistered(imsRegistration, i);
        this.mController.setImsDeRegistration(imsRegistration);
        this.mIsOptionsEnabled.put(imsRegistration.getPhoneId(), Boolean.FALSE);
        Log.i(LOG_TAG, "onDeregistered: Options service is disabled.");
        ICapabilityEventListener iCapabilityEventListener = this.mListener;
        if (iCapabilityEventListener != null) {
            iCapabilityEventListener.onMediaReady(false, false, imsRegistration.getPhoneId());
        }
    }

    public boolean isReadyToRequest(int i) {
        return this.mIsOptionsEnabled.get(i).booleanValue();
    }

    public void setOwnCapabilities(long j, int i) {
        if (isRunning()) {
            this.mController.setOwnCapabilities(j, i);
            if (this.mListener != null && this.mIsOptionsEnabled.get(i).booleanValue()) {
                this.mListener.onMediaReady(true, false, i);
            }
        }
    }

    public int requestCapabilityExchange(Set<ImsUri> set, CapabilityConstants.RequestType requestType, int i, int i2) {
        Log.e(LOG_TAG, "requestCapabilityExchange: OPTIONS doesn't support list.");
        return 0;
    }

    public boolean requestCapabilityExchange(ImsUri imsUri, ICapabilityExchangeControl.ICapabilityExchangeCallback iCapabilityExchangeCallback, CapabilityConstants.RequestType requestType, boolean z, long j, int i, String str, int i2) {
        if (!isRunning()) {
            return false;
        }
        IMSLog.s(LOG_TAG, i, "requestCapabilityExchange: uri: " + imsUri.toString() + ", iari: " + str);
        if (SimUtil.getSimMno(i).isRjil() && !ChatbotUriUtil.hasUriBotPlatform(imsUri, i)) {
            UriGeneratorFactory instance = UriGeneratorFactory.getInstance();
            UriGenerator.URIServiceType uRIServiceType = UriGenerator.URIServiceType.RCS_URI;
            imsUri = instance.get(i, uRIServiceType).getNetworkPreferredUri(uRIServiceType, imsUri.getMsisdn(), (String) null);
        }
        ImsUri imsUri2 = imsUri;
        if (iCapabilityExchangeCallback != null) {
            iCapabilityExchangeCallback.onComplete((Capabilities) null);
        }
        if (this.mIsOptionsEnabled.get(i).booleanValue()) {
            return this.mController.requestCapabilityExchange(imsUri2, j, i, str);
        }
        return false;
    }

    public boolean sendOptionsRequest(ImsUri imsUri, boolean z, Set<String> set, int i) {
        if (!isRunning() || !this.mIsOptionsEnabled.get(i).booleanValue()) {
            return false;
        }
        return this.mController.requestCapabilityExchange(imsUri, set, i);
    }

    public boolean sendCapexResponse(ImsUri imsUri, long j, String str, int i, int i2, String str2) {
        if (!isRunning()) {
            return false;
        }
        int i3 = i2;
        IMSLog.s(LOG_TAG, i3, "sendCapexResponse: uri: " + imsUri.toString());
        return this.mController.sendCapexResponse(imsUri, j, str, i, i3, str2);
    }

    public boolean sendCapexResponse(ImsUri imsUri, Set<String> set, String str, int i, int i2) {
        if (!isRunning()) {
            return false;
        }
        IMSLog.s(LOG_TAG, i2, "sendCapexResponse: uri: " + imsUri.toString());
        IMSLog.s(LOG_TAG, i2, "sendCapexResponse: feature list:  " + set.toString());
        return this.mController.sendCapexResponse(imsUri, set, str, i, i2);
    }

    public boolean sendCapexErrorResponse(ImsUri imsUri, String str, int i, int i2, String str2) {
        if (!isRunning()) {
            return false;
        }
        IMSLog.s(LOG_TAG, i, "sendCapexErrorResponse: uri: " + imsUri.toString());
        return this.mController.sendCapexErrorResponse(imsUri, str, i, i2, str2);
    }

    /* access modifiers changed from: package-private */
    public void onCapabilitiesUpdate(OptionsEvent optionsEvent) {
        int phoneId = optionsEvent.getPhoneId();
        IMSLog.s(LOG_TAG, phoneId, "onCapabilitiesUpdate: success: " + optionsEvent.isSuccess() + ", uri: " + optionsEvent.getUri());
        CapabilityConstants.CapExResult capExResult = CapabilityConstants.CapExResult.SUCCESS;
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        Mno simMno = simManagerFromSimSlot != null ? simManagerFromSimSlot.getSimMno() : Mno.DEFAULT;
        if (!optionsEvent.isSuccess()) {
            capExResult = convertOptionsError(optionsEvent.getReason());
            if (capExResult == CapabilityConstants.CapExResult.USER_AVAILABLE_OFFLINE) {
                optionsEvent.setFeatures((long) Capabilities.FEATURE_OFFLINE_RCS_USER);
            } else if (!ChatbotUriUtil.hasUriBotPlatform(optionsEvent.getUri(), phoneId) || capExResult == CapabilityConstants.CapExResult.USER_NOT_FOUND || capExResult == CapabilityConstants.CapExResult.DOES_NOT_EXIST_ANYWHERE) {
                optionsEvent.setFeatures((long) Capabilities.FEATURE_NON_RCS_USER);
            } else {
                optionsEvent.setFeatures((long) Capabilities.FEATURE_OFFLINE_RCS_USER);
            }
        } else if ((ConfigUtil.isRcsEur(simMno) || simMno == Mno.TELSTRA || "TEL".equals(OmcCode.get())) && optionsEvent.getFeatures() == 0 && !ChatbotUriUtil.hasUriBotPlatform(optionsEvent.getUri(), phoneId)) {
            optionsEvent.setFeatures((long) Capabilities.FEATURE_NON_RCS_USER);
        }
        if (this.mListener != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(optionsEvent.getUri());
            IMSLog.i(LOG_TAG, phoneId, "onCapabilitiesUpdate: success: " + optionsEvent.isSuccess() + ", txID: " + optionsEvent.getTxId());
            this.mListener.onCapabilityUpdate(arrayList, capExResult, (String) null, optionsEvent);
        }
    }

    /* access modifiers changed from: package-private */
    public CapabilityConstants.CapExResult convertOptionsError(OptionsEvent.OptionsFailureReason optionsFailureReason) {
        if (optionsFailureReason == OptionsEvent.OptionsFailureReason.USER_NOT_AVAILABLE) {
            return CapabilityConstants.CapExResult.USER_NOT_FOUND;
        }
        if (optionsFailureReason == OptionsEvent.OptionsFailureReason.DOES_NOT_EXIST_ANYWHERE) {
            return CapabilityConstants.CapExResult.DOES_NOT_EXIST_ANYWHERE;
        }
        if (optionsFailureReason == OptionsEvent.OptionsFailureReason.USER_NOT_REACHABLE || optionsFailureReason == OptionsEvent.OptionsFailureReason.USER_NOT_REGISTERED) {
            return CapabilityConstants.CapExResult.USER_UNAVAILABLE;
        }
        if (optionsFailureReason == OptionsEvent.OptionsFailureReason.FORBIDDEN_403) {
            return CapabilityConstants.CapExResult.FORBIDDEN_403;
        }
        if (optionsFailureReason == OptionsEvent.OptionsFailureReason.REQUEST_TIMED_OUT) {
            return CapabilityConstants.CapExResult.REQUEST_TIMED_OUT;
        }
        if (optionsFailureReason == OptionsEvent.OptionsFailureReason.INVALID_DATA) {
            return CapabilityConstants.CapExResult.INVALID_DATA;
        }
        if (optionsFailureReason == OptionsEvent.OptionsFailureReason.USER_AVAILABLE_OFFLINE || optionsFailureReason == OptionsEvent.OptionsFailureReason.AUTOMATA_PRESENT) {
            return CapabilityConstants.CapExResult.USER_AVAILABLE_OFFLINE;
        }
        return CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR;
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        if (message.what == 1) {
            onCapabilitiesUpdate((OptionsEvent) message.obj);
        }
    }
}
