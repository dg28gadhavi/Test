package com.sec.internal.ims.servicemodules.options;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class CapabilityForIncall extends Handler {
    private static final String LOG_TAG = "CapabilityForIncall";
    public static final String NAME = CapabilityForIncall.class.getSimpleName();
    protected Map<Integer, List<ICall>> mActiveCallLists = new HashMap();
    private CapabilityUtil mCapabilityUtil = null;
    protected boolean mIsNeedUpdateCallState = false;
    private String mRcsProfile = "";
    IRegistrationManager mRegMan = null;
    private CapabilityDiscoveryModule mServiceModule = null;

    public CapabilityForIncall(CapabilityDiscoveryModule capabilityDiscoveryModule, CapabilityUtil capabilityUtil, IRegistrationManager iRegistrationManager) {
        this.mServiceModule = capabilityDiscoveryModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mRegMan = iRegistrationManager;
    }

    public void processCallStateChanged(int i, CopyOnWriteArrayList<ICall> copyOnWriteArrayList, Map<Integer, ImsRegistration> map) {
        post(new CapabilityForIncall$$ExternalSyntheticLambda1(this, copyOnWriteArrayList, i, map));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processCallStateChanged$0(CopyOnWriteArrayList copyOnWriteArrayList, int i, Map map) {
        ICall iCall;
        int i2 = i;
        Map map2 = map;
        int checkConnectedCalls = checkConnectedCalls(copyOnWriteArrayList);
        List<ICall> activeCalls = setActiveCalls(i2);
        int checkPrevConnectedCalls = checkPrevConnectedCalls(activeCalls);
        IMSLog.i(LOG_TAG, i2, "processCallStateChanged: nConnectedCalls=" + checkConnectedCalls + " nPrevConnectedCalls=" + checkPrevConnectedCalls);
        Capabilities ownCapabilitiesBase = this.mServiceModule.getOwnCapabilitiesBase(i2);
        CapabilityConfig capabilityConfig = this.mServiceModule.getCapabilityConfig(i2);
        this.mRcsProfile = capabilityConfig != null ? capabilityConfig.getRcsProfile() : "";
        if (capabilityConfig != null && ((!capabilityConfig.usePresence() || ImsProfile.isRcsUpProfile(this.mRcsProfile)) && map2.containsKey(Integer.valueOf(i)) && ownCapabilitiesBase.hasAnyFeature(Capabilities.FEATURE_CALL_SERVICE))) {
            long feature = ownCapabilitiesBase.getFeature();
            String extFeatureAsJoinedString = ownCapabilitiesBase.getExtFeatureAsJoinedString();
            long filterFeaturesWithService = this.mCapabilityUtil.filterFeaturesWithService(feature, this.mRegMan.getServiceForNetwork(((ImsRegistration) map2.get(Integer.valueOf(i))).getImsProfile(), ((ImsRegistration) map2.get(Integer.valueOf(i))).getRegiRat(), false, i2), this.mRegMan.getCurrentNetworkByPhoneId(i2), i);
            Iterator it = copyOnWriteArrayList.iterator();
            while (it.hasNext()) {
                ICall iCall2 = (ICall) it.next();
                ICall call = getCall(activeCalls, iCall2.getNumber());
                IMSLog.s(LOG_TAG, "prev: " + call + ", current: " + iCall2);
                if (call != null) {
                    if ((!call.isConnected() || checkPrevConnectedCalls > 1) && iCall2.isConnected() && checkConnectedCalls == 1) {
                        iCall = call;
                        setIncallFeature(i, iCall2.getNumber(), filterFeaturesWithService, extFeatureAsJoinedString, true);
                    } else {
                        iCall = call;
                        if ((iCall.isConnected() && checkPrevConnectedCalls == 1 && (!iCall2.isConnected() || checkConnectedCalls > 1)) || (!iCall.isConnected() && iCall2.isConnected() && checkConnectedCalls > 1)) {
                            setIncallFeature(i, iCall2.getNumber(), filterFeaturesWithService, extFeatureAsJoinedString, false);
                        }
                    }
                    activeCalls.remove(iCall);
                } else if (iCall2.isConnected() && checkConnectedCalls == 1) {
                    setIncallFeature(i, iCall2.getNumber(), filterFeaturesWithService, extFeatureAsJoinedString, true);
                } else if (checkConnectedCalls > 1) {
                    setIncallFeature(i, iCall2.getNumber(), filterFeaturesWithService, extFeatureAsJoinedString, false);
                }
            }
            for (ICall next : activeCalls) {
                IMSLog.s(LOG_TAG, "Disconnected call: " + next);
                if (next.isConnected() && checkPrevConnectedCalls == 1) {
                    this.mServiceModule.setCallNumber(i2, (String) null);
                    this.mServiceModule.updateOwnCapabilities(i2);
                    this.mServiceModule.setOwnCapabilities(i2, false);
                }
            }
        }
        this.mActiveCallLists.put(Integer.valueOf(i), copyOnWriteArrayList);
    }

    private int checkConnectedCalls(CopyOnWriteArrayList<ICall> copyOnWriteArrayList) {
        Iterator<ICall> it = copyOnWriteArrayList.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().isConnected()) {
                i++;
            }
        }
        return i;
    }

    private int checkPrevConnectedCalls(List<ICall> list) {
        int i = 0;
        for (ICall isConnected : list) {
            if (isConnected.isConnected()) {
                i++;
            }
        }
        return i;
    }

    private List<ICall> setActiveCalls(int i) {
        return this.mActiveCallLists.containsKey(Integer.valueOf(i)) ? this.mActiveCallLists.get(Integer.valueOf(i)) : new ArrayList();
    }

    public void processCallStateChangedOnDeregi(int i, CopyOnWriteArrayList<ICall> copyOnWriteArrayList) {
        post(new CapabilityForIncall$$ExternalSyntheticLambda0(this, copyOnWriteArrayList, i));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processCallStateChangedOnDeregi$1(CopyOnWriteArrayList copyOnWriteArrayList, int i) {
        Log.i(LOG_TAG, "mImsRegInfo: null");
        int checkConnectedCalls = checkConnectedCalls(copyOnWriteArrayList);
        List<ICall> activeCalls = setActiveCalls(i);
        int checkPrevConnectedCalls = checkPrevConnectedCalls(activeCalls);
        this.mIsNeedUpdateCallState = true;
        Iterator it = copyOnWriteArrayList.iterator();
        while (it.hasNext()) {
            ICall iCall = (ICall) it.next();
            ICall call = getCall(activeCalls, iCall.getNumber());
            if (call != null) {
                if ((!call.isConnected() || checkPrevConnectedCalls > 1) && iCall.isConnected() && checkConnectedCalls == 1) {
                    this.mServiceModule.setCallNumber(i, iCall.getNumber());
                } else if ((call.isConnected() && checkPrevConnectedCalls == 1 && (!iCall.isConnected() || checkConnectedCalls > 1)) || (!call.isConnected() && iCall.isConnected() && checkConnectedCalls > 1)) {
                    this.mServiceModule.setCallNumber(i, (String) null);
                }
                activeCalls.remove(call);
            } else if (iCall.isConnected() && checkConnectedCalls == 1) {
                this.mServiceModule.setCallNumber(i, iCall.getNumber());
            }
        }
        for (ICall isConnected : activeCalls) {
            if (isConnected.isConnected() && checkPrevConnectedCalls == 1) {
                this.mServiceModule.setCallNumber(i, (String) null);
            }
        }
        this.mActiveCallLists.put(Integer.valueOf(i), copyOnWriteArrayList);
    }

    /* access modifiers changed from: package-private */
    public ICall getCall(List<ICall> list, String str) {
        for (ICall next : list) {
            if (TextUtils.equals(next.getNumber(), str)) {
                return next;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setIncallFeature(int i, String str, long j, String str2, boolean z) {
        IMSLog.i(LOG_TAG, i, "SetIncallFeature");
        if (z) {
            Log.i(LOG_TAG, "Activate content share features.");
            this.mServiceModule.setCallNumber(i, str);
            this.mServiceModule.exchangeCapabilities(str, j, i, str2);
            this.mServiceModule.updateOwnCapabilities(i);
            this.mServiceModule.setOwnCapabilities(i, false);
            return;
        }
        Log.i(LOG_TAG, "Deactivate content share features.");
        this.mServiceModule.setCallNumber(i, (String) null);
        this.mServiceModule.exchangeCapabilities(str, j & ((long) (~Capabilities.FEATURE_VSH)) & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP) & (~Capabilities.FEATURE_ENRICHED_SHARED_SKETCH), i, str2);
        this.mServiceModule.updateOwnCapabilities(i);
        this.mServiceModule.setOwnCapabilities(i, false);
    }

    public void exchangeCapabilitiesForVSH(int i, boolean z, Map<Integer, ImsRegistration> map) {
        if (this.mRegMan == null || !map.containsKey(Integer.valueOf(i))) {
            Log.i(LOG_TAG, "exchangeCapabilitiesForVSH: mRegMan or mImsRegInfo is null ");
            return;
        }
        int currentNetworkByPhoneId = this.mRegMan.getCurrentNetworkByPhoneId(i);
        int i2 = 0;
        Set<String> serviceForNetwork = this.mRegMan.getServiceForNetwork(map.get(Integer.valueOf(i)).getImsProfile(), map.get(Integer.valueOf(i)).getRegiRat(), false, i);
        Capabilities ownCapabilities = this.mServiceModule.getOwnCapabilities(i);
        if (ownCapabilities != null) {
            long filterFeaturesWithService = this.mCapabilityUtil.filterFeaturesWithService(ownCapabilities.getFeature(), serviceForNetwork, currentNetworkByPhoneId, i);
            String extFeatureAsJoinedString = ownCapabilities.getExtFeatureAsJoinedString();
            if (this.mIsNeedUpdateCallState) {
                this.mIsNeedUpdateCallState = false;
            }
            List<ICall> arrayList = new ArrayList<>();
            if (this.mActiveCallLists.containsKey(Integer.valueOf(i))) {
                arrayList = this.mActiveCallLists.get(Integer.valueOf(i));
            }
            ICall iCall = null;
            for (ICall iCall2 : arrayList) {
                if (iCall2.isConnected()) {
                    i2++;
                    iCall = iCall2;
                }
            }
            if (i2 == 1) {
                if (z) {
                    this.mServiceModule.exchangeCapabilities(iCall.getNumber(), filterFeaturesWithService, i, extFeatureAsJoinedString);
                } else {
                    this.mServiceModule.exchangeCapabilities(iCall.getNumber(), filterFeaturesWithService & ((long) (~Capabilities.FEATURE_VSH)), i, extFeatureAsJoinedString);
                }
            }
        }
    }

    public void triggerCapexForIncallRegiDeregi(int i, ImsRegistration imsRegistration) {
        if (imsRegistration.hasService("options") && this.mActiveCallLists.containsKey(Integer.valueOf(i)) && this.mActiveCallLists.get(Integer.valueOf(i)).size() > 0) {
            List<ICall> list = this.mActiveCallLists.get(Integer.valueOf(i));
            UriGenerator uriGenerator = this.mServiceModule.getUriGenerator();
            if (uriGenerator != null) {
                for (ICall iCall : list) {
                    if (iCall.isConnected()) {
                        this.mServiceModule.requestCapabilityExchange(uriGenerator.getNormalizedUri(iCall.getNumber(), true), CapabilityConstants.RequestType.REQUEST_TYPE_NONE, true, i, 0);
                    }
                }
            }
        }
    }
}
