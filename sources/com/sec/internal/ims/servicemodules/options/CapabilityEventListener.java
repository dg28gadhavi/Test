package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.ims.RcsContactUceCapability;
import android.telephony.ims.stub.CapabilityExchangeEventListener;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CapabilityEventListener implements ICapabilityEventListener {
    private static final String LOG_TAG = "CapabilityEventListener";
    /* access modifiers changed from: private */
    public CapabilityDiscoveryModule mCapabilityDiscovery;
    private Context mContext;

    CapabilityEventListener(CapabilityDiscoveryModule capabilityDiscoveryModule, Context context) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mContext = context;
    }

    public void onCapabilityUpdate(List<ImsUri> list, long j, CapabilityConstants.CapExResult capExResult, String str, int i) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("URIS", new ArrayList(list));
        bundle.putString("PIDF", str);
        bundle.putLong("FEATURES", j);
        bundle.putInt("PHONEID", i);
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(4, capExResult.ordinal(), -1, bundle));
    }

    public void onCapabilityUpdate(final List<ImsUri> list, CapabilityConstants.CapExResult capExResult, String str, final OptionsEvent optionsEvent) {
        ArrayList arrayList = new ArrayList(2);
        arrayList.addAll(optionsEvent.getPAssertedIdSet());
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("URIS", new ArrayList(list));
        bundle.putString("PIDF", str);
        bundle.putLong("FEATURES", optionsEvent.getFeatures());
        bundle.putInt("LASTSEEN", optionsEvent.getLastSeen());
        bundle.putInt("PHONEID", optionsEvent.getPhoneId());
        bundle.putString("EXTFEATURE", optionsEvent.getExtFeature());
        bundle.putBoolean("ISTOKENUSED", optionsEvent.getIsTokenUsed());
        bundle.putParcelableArrayList("PAID", arrayList);
        bundle.putStringArrayList("CAPA_TAGS", (ArrayList) optionsEvent.getfeatureTags());
        bundle.putString("REASON_HDR", optionsEvent.getReasonHdr());
        bundle.putInt("RESP_CODE", optionsEvent.getRespCode());
        bundle.putBoolean("IS_RESPONSE", optionsEvent.isResponse());
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(4, capExResult.ordinal(), -1, bundle));
        if (!optionsEvent.isResponse() && optionsEvent.getTxId() != null) {
            if (!RcsUtils.isImsSingleRegiRequired(this.mContext, optionsEvent.getPhoneId()) || !RcsUtils.isSrRcsOptionsEnabled(this.mContext, optionsEvent.getPhoneId())) {
                this.mCapabilityDiscovery.prepareResponse(list, optionsEvent.getFeatures(), optionsEvent.getTxId(), optionsEvent.getPhoneId(), optionsEvent.getExtFeature());
                return;
            }
            HashSet hashSet = new HashSet(optionsEvent.getFeatureList());
            hashSet.addAll(optionsEvent.getFeatureList());
            SecImsNotifier.getInstance().onRemoteCapabilityRequest(optionsEvent.getPhoneId(), Uri.parse(list.get(0).uri().toString()), hashSet, new CapabilityExchangeEventListener.OptionsRequestCallback() {
                public void onRespondToCapabilityRequest(RcsContactUceCapability rcsContactUceCapability, boolean z) {
                    CapabilityEventListener.this.mCapabilityDiscovery.getOptionsModule().sendCapexResponse(new ImsUri(rcsContactUceCapability.getContactUri().toString()), !z ? rcsContactUceCapability.getFeatureTags() : null, optionsEvent.getTxId(), optionsEvent.getLastSeen(), optionsEvent.getPhoneId());
                }

                public void onRespondToCapabilityRequestWithError(int i, String str) {
                    CapabilityEventListener.this.mCapabilityDiscovery.getOptionsModule().sendCapexErrorResponse((ImsUri) list.get(0), optionsEvent.getTxId(), optionsEvent.getPhoneId(), i, str);
                }
            });
        }
    }

    public void onMediaReady(boolean z, boolean z2, int i) {
        ICapabilityExchangeControl capabilityControl;
        IMSLog.i(LOG_TAG, i, "onMediaReady: ready " + z + ", isPresence " + z2);
        CapabilityConfig capabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(i);
        if ((capabilityConfig == null || capabilityConfig.usePresence() == z2) && (capabilityControl = this.mCapabilityDiscovery.getCapabilityControl(i)) != null && capabilityControl.isReadyToRequest(i)) {
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(3, Integer.valueOf(i)));
        }
    }

    public void onPollingRequested(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "onPollingRequested: success " + z);
        if (!z) {
            this.mCapabilityDiscovery.stopPollingTimer(i);
        } else if (this.mCapabilityDiscovery.getPollingIntent(i) == null && this.mCapabilityDiscovery.getCapabilityControl(i).isReadyToRequest(i)) {
            this.mCapabilityDiscovery.startPollingTimer(i);
        }
    }

    public void onCapabilityAndAvailabilityPublished(int i, int i2) {
        this.mCapabilityDiscovery.notifyEABServiceAdvertiseResult(i, i2);
    }
}
