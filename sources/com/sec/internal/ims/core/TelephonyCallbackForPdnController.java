package com.sec.internal.ims.core;

import android.telephony.CellInfo;
import android.telephony.PreciseDataConnectionState;
import android.telephony.ServiceState;
import android.telephony.TelephonyCallback;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.sec.ims.extensions.ServiceStateExt;
import com.sec.internal.constants.ims.os.EmcBsIndication;
import com.sec.internal.constants.ims.os.NetworkState;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.NetworkStateListener;
import com.sec.internal.interfaces.ims.core.PdnEventListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class TelephonyCallbackForPdnController extends TelephonyCallback implements TelephonyCallback.DataConnectionStateListener, TelephonyCallback.ServiceStateListener, TelephonyCallback.CellInfoListener, TelephonyCallback.PreciseDataConnectionStateListener {
    private static final String LOG_TAG = TelephonyCallbackForPdnController.class.getSimpleName();
    private final IImsFramework mImsFramework;
    int mInternalSimSlot;
    boolean mMobileRadioConnected = false;
    private final PdnController mPdnController;
    int mSubId;

    public TelephonyCallbackForPdnController(PdnController pdnController, IImsFramework iImsFramework, int i, int i2) {
        this.mPdnController = pdnController;
        this.mImsFramework = iImsFramework;
        this.mInternalSimSlot = i;
        this.mSubId = i2;
    }

    public int getSubId() {
        return this.mSubId;
    }

    public int getInternalSimSlot() {
        return this.mInternalSimSlot;
    }

    public void onServiceStateChanged(ServiceState serviceState) {
        boolean z;
        NetworkState networkState = this.mPdnController.getNetworkState(this.mInternalSimSlot);
        ServiceStateWrapper serviceStateWrapper = new ServiceStateWrapper(serviceState);
        ArrayList arrayList = new ArrayList();
        if (serviceStateWrapper.getCellIdentity() != null) {
            networkState.setCellIdentity(serviceStateWrapper.getCellIdentity());
        }
        int dataRegState = serviceStateWrapper.getDataRegState();
        boolean z2 = dataRegState == 2 || dataRegState == 1;
        if (z2 != networkState.isEmergencyOnly()) {
            arrayList.add("EmergencyOnlyReg:" + networkState.isEmergencyOnly() + "=>" + z2);
            this.mPdnController.getNetworkState(this.mInternalSimSlot).setEmergencyOnly(z2);
        }
        VoPsIndication translateVops = VoPsIndication.translateVops(serviceStateWrapper.getImsVoiceAvail(), dataRegState == 1 || dataRegState == 3);
        if (translateVops != networkState.getVopsIndication()) {
            arrayList.add("VoPS:" + networkState.getVopsIndication() + "=>" + translateVops);
            networkState.setVopsIndication(translateVops);
            z = true;
        } else {
            z = false;
        }
        EmcBsIndication translateEmcbs = EmcBsIndication.translateEmcbs(serviceStateWrapper.getIsEbSupported());
        int mobileDataNetworkType = serviceStateWrapper.getMobileDataNetworkType();
        if (mobileDataNetworkType != 13) {
            if (mobileDataNetworkType == 0) {
                translateEmcbs = EmcBsIndication.UNKNOWN;
            } else {
                translateEmcbs = EmcBsIndication.NOT_SUPPORTED;
            }
        }
        if (networkState.getEmcBsIndication() != translateEmcbs) {
            arrayList.add("EmcBsIndi:" + networkState.getEmcBsIndication() + "=>" + translateEmcbs);
            networkState.setEmcBsIndication(translateEmcbs);
        }
        int voiceRegState = serviceStateWrapper.getVoiceRegState();
        if (networkState.getVoiceRegState() != voiceRegState) {
            arrayList.add("VoiceReg:" + networkState.getVoiceRegState() + "=>" + voiceRegState);
            networkState.setVoiceRegState(voiceRegState);
            z = true;
        }
        int voiceNetworkType = serviceStateWrapper.getVoiceNetworkType();
        if (networkState.getVoiceNetworkType() != voiceNetworkType) {
            arrayList.add("VoiceNet:" + networkState.getVoiceNetworkType() + "=>" + voiceNetworkType);
            networkState.setVoiceNetworkType(voiceNetworkType);
            z = true;
        }
        boolean isPsOnlyReg = serviceStateWrapper.isPsOnlyReg();
        if (isPsOnlyReg != networkState.isPsOnlyReg()) {
            arrayList.add("PsOnly:" + networkState.isPsOnlyReg() + "=>" + isPsOnlyReg);
            networkState.setPsOnlyReg(isPsOnlyReg);
            z = true;
        }
        networkState.setInternationalRoaming(serviceStateWrapper.getVoiceRoamingType() == 3);
        boolean dataRoaming = serviceStateWrapper.getDataRoaming();
        if (dataRoaming != networkState.isDataRoaming()) {
            arrayList.add("DataRoaming:" + networkState.isDataRoaming() + "=>" + dataRoaming);
            networkState.setDataRoaming(dataRoaming);
            z = true;
        }
        boolean voiceRoaming = serviceStateWrapper.getVoiceRoaming();
        if (voiceRoaming != networkState.isVoiceRoaming()) {
            arrayList.add("VoiceRoaming:" + networkState.isVoiceRoaming() + "=>" + voiceRoaming);
            networkState.setVoiceRoaming(voiceRoaming);
            z = true;
        }
        String operatorNumeric = serviceStateWrapper.getOperatorNumeric();
        if (!TextUtils.isEmpty(operatorNumeric) && !TextUtils.equals(networkState.getOperatorNumeric(), operatorNumeric)) {
            arrayList.add("Operator:" + networkState.getOperatorNumeric() + "=>" + operatorNumeric);
            networkState.setOperatorNumeric(operatorNumeric);
            z = true;
        }
        boolean z3 = this.mInternalSimSlot == SimUtil.getActiveDataPhoneId() && NetworkUtil.isMobileDataConnected(ImsRegistry.getContext());
        if (z3 != networkState.isDataConnectedState()) {
            arrayList.add("DataConnState:" + networkState.isDataConnectedState() + "=>" + z3);
            networkState.setDataConnectionState(z3);
            z = true;
        }
        if (mobileDataNetworkType == 19) {
            mobileDataNetworkType = 13;
        }
        networkState.setMobileDataNetworkType(mobileDataNetworkType);
        networkState.setMobileDataRegState(serviceStateWrapper.getMobileDataRegState());
        if (serviceStateWrapper.getMobileDataRegState() == 0) {
            if (!this.mMobileRadioConnected) {
                this.mMobileRadioConnected = true;
                for (NetworkStateListener onMobileRadioConnected : this.mPdnController.mNetworkStateListeners) {
                    onMobileRadioConnected.onMobileRadioConnected(this.mInternalSimSlot);
                }
            }
        } else if (this.mMobileRadioConnected) {
            this.mMobileRadioConnected = false;
            for (NetworkStateListener onMobileRadioDisconnected : this.mPdnController.mNetworkStateListeners) {
                onMobileRadioDisconnected.onMobileRadioDisconnected(this.mInternalSimSlot);
            }
        }
        IMSLog.i(LOG_TAG, this.mInternalSimSlot, "onServiceStateChanged: state=" + serviceState + "Changed=" + String.join(", ", arrayList));
        PdnController pdnController = this.mPdnController;
        pdnController.mNeedCellLocationUpdate = z;
        pdnController.notifyDataConnectionState(serviceStateWrapper.getDataNetworkType(), dataRegState, z, this.mInternalSimSlot);
        notifySnapshotState(serviceStateWrapper.getSnapshotStatus(), this.mInternalSimSlot);
        IGeolocationController geolocationController = this.mImsFramework.getGeolocationController();
        if (geolocationController != null) {
            geolocationController.notifyServiceStateChanged(this.mInternalSimSlot, serviceStateWrapper);
        }
    }

    public void onCellInfoChanged(List<CellInfo> list) {
        NetworkState networkState = this.mPdnController.getNetworkState(this.mInternalSimSlot);
        String str = LOG_TAG;
        int i = this.mInternalSimSlot;
        IMSLog.i(str, i, "onCellInfoChanged: " + IMSLog.checker(list));
        if (list != null || networkState == null || networkState.getAllCellInfo() != null) {
            if (networkState != null) {
                networkState.setAllCellInfo(list);
            }
            for (NetworkStateListener onCellInfoChanged : this.mPdnController.mNetworkStateListeners) {
                onCellInfoChanged.onCellInfoChanged(list, this.mInternalSimSlot);
            }
        }
    }

    public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState preciseDataConnectionState) {
        String str = LOG_TAG;
        int i = this.mInternalSimSlot;
        IMSLog.s(str, i, "onPreciseDataConnectionStateChanged: state=" + preciseDataConnectionState);
        Optional.ofNullable(preciseDataConnectionState.getApnSetting()).ifPresent(new TelephonyCallbackForPdnController$$ExternalSyntheticLambda0(this, preciseDataConnectionState.getTransportType(), preciseDataConnectionState));
        for (NetworkStateListener onPreciseDataConnectionStateChanged : this.mPdnController.mNetworkStateListeners) {
            onPreciseDataConnectionStateChanged.onPreciseDataConnectionStateChanged(this.mInternalSimSlot, preciseDataConnectionState);
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onPreciseDataConnectionStateChanged$0(int i, PreciseDataConnectionState preciseDataConnectionState, ApnSetting apnSetting) {
        if ((apnSetting.getApnTypeBitmask() & 64) == 64) {
            this.mPdnController.getNetworkState(this.mInternalSimSlot).setPreciseDataConnectionState(i, preciseDataConnectionState);
        }
    }

    public void onDataConnectionStateChanged(int i, int i2) {
        String str = LOG_TAG;
        int i3 = this.mInternalSimSlot;
        IMSLog.s(str, i3, "onDataConnectionStateChanged: state " + i + ", networkType " + i2);
        this.mPdnController.setDataState(this.mInternalSimSlot, i);
    }

    private void notifySnapshotState(int i, int i2) {
        if (this.mPdnController.getNetworkState(i2) != null) {
            String str = LOG_TAG;
            IMSLog.i(str, i2, "notifySnapshotState: snapshotState=" + i + " old=" + this.mPdnController.getNetworkState(i2).getSnapshotState());
            if (this.mPdnController.getNetworkState(i2).getSnapshotState() != i) {
                this.mPdnController.getNetworkState(i2).setSnapshotState(i);
                boolean z = this.mPdnController.getNetworkState(i2).getSnapshotState() == ServiceStateExt.SNAPSHOT_STATUS_ACTIVATED;
                synchronized (this.mPdnController.mNetworkCallbacks) {
                    for (Map.Entry next : this.mPdnController.mNetworkCallbacks.entrySet()) {
                        PdnEventListener pdnEventListener = (PdnEventListener) next.getKey();
                        NetworkCallback networkCallback = (NetworkCallback) next.getValue();
                        if (networkCallback.mPhoneId == i2) {
                            int i3 = networkCallback.mNetworkType;
                            if (i3 != 1) {
                                if (z) {
                                    pdnEventListener.onSuspendedBySnapshot(i3);
                                } else {
                                    pdnEventListener.onResumedBySnapshot(i3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
