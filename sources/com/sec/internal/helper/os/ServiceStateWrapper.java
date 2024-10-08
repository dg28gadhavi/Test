package com.sec.internal.helper.os;

import android.os.Parcel;
import android.telephony.CellIdentity;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.ServiceState;

public class ServiceStateWrapper {
    public static final int NR_5G_BEARER_STATUS_ALLOCATED = 1;
    public static final int NR_5G_BEARER_STATUS_MMW_ALLOCATED = 2;
    public static final int NR_5G_BEARER_STATUS_NOT_ALLOCATED = 0;
    public static final int ROAMING_TYPE_DOMESTIC = 2;
    public static final int ROAMING_TYPE_INTERNATIONAL = 3;
    public static final int ROAMING_TYPE_NOT_ROAMING = 0;
    public static final int ROAMING_TYPE_UNKNOWN = 1;
    private static final String TAG = "ServiceStateWrapper";
    private final ServiceState mServiceState;

    public ServiceStateWrapper(ServiceState serviceState) {
        this.mServiceState = serviceState;
    }

    public void writeToParcel(Parcel parcel, int i) {
        this.mServiceState.writeToParcel(parcel, i);
    }

    public int describeContents() {
        return this.mServiceState.describeContents();
    }

    public int getState() {
        return this.mServiceState.getState();
    }

    public void setState(int i) {
        this.mServiceState.setState(i);
    }

    public void setStateOutOfService() {
        this.mServiceState.setStateOutOfService();
    }

    public void setStateOff() {
        this.mServiceState.setStateOff();
    }

    public String getOperatorAlphaLong() {
        return this.mServiceState.getOperatorAlphaLong();
    }

    public boolean getRoaming() {
        return this.mServiceState.getRoaming();
    }

    public void setRoaming(boolean z) {
        this.mServiceState.setRoaming(z);
    }

    public void setOperatorName(String str, String str2, String str3) {
        this.mServiceState.setOperatorName(str, str2, str3);
    }

    public String getOperatorNumeric() {
        return this.mServiceState.getOperatorNumeric();
    }

    public boolean getIsManualSelection() {
        return this.mServiceState.getIsManualSelection();
    }

    public void setIsManualSelection(boolean z) {
        this.mServiceState.setIsManualSelection(z);
    }

    public String getOperatorAlphaShort() {
        return this.mServiceState.getOperatorAlphaShort();
    }

    public String toString() {
        return this.mServiceState.toString();
    }

    public int getDataRegState() {
        return this.mServiceState.getDataRegState();
    }

    public int getVoiceRegState() {
        return this.mServiceState.getVoiceRegState();
    }

    public boolean getImsVoiceAvail() {
        NetworkRegistrationInfo mobileDataNetworkRegistrationInfo = getMobileDataNetworkRegistrationInfo();
        if (mobileDataNetworkRegistrationInfo == null || mobileDataNetworkRegistrationInfo.getDataSpecificInfo() == null || mobileDataNetworkRegistrationInfo.getDataSpecificInfo().getVopsSupportInfo() == null) {
            return false;
        }
        return mobileDataNetworkRegistrationInfo.getDataSpecificInfo().getVopsSupportInfo().isVopsSupported();
    }

    public boolean getIsEbSupported() {
        NetworkRegistrationInfo mobileDataNetworkRegistrationInfo = getMobileDataNetworkRegistrationInfo();
        if (mobileDataNetworkRegistrationInfo == null || mobileDataNetworkRegistrationInfo.getDataSpecificInfo() == null || mobileDataNetworkRegistrationInfo.getDataSpecificInfo().getVopsSupportInfo() == null) {
            return false;
        }
        return mobileDataNetworkRegistrationInfo.getDataSpecificInfo().getVopsSupportInfo().isEmergencyServiceSupported();
    }

    public CellIdentity getCellIdentity() {
        NetworkRegistrationInfo mobileDataNetworkRegistrationInfo = getMobileDataNetworkRegistrationInfo();
        if (mobileDataNetworkRegistrationInfo != null) {
            return mobileDataNetworkRegistrationInfo.getCellIdentity();
        }
        return null;
    }

    public int getMobileDataNetworkType() {
        NetworkRegistrationInfo mobileDataNetworkRegistrationInfo = getMobileDataNetworkRegistrationInfo();
        if (mobileDataNetworkRegistrationInfo != null) {
            return mobileDataNetworkRegistrationInfo.getAccessNetworkTechnology();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public NetworkRegistrationInfo getMobileDataNetworkRegistrationInfo() {
        return this.mServiceState.getNetworkRegistrationInfo(2, 1);
    }

    public boolean getDataRoaming() {
        return this.mServiceState.getDataRoaming();
    }

    public boolean getVoiceRoaming() {
        return this.mServiceState.getVoiceRoaming();
    }

    public int getDataNetworkType() {
        return this.mServiceState.getDataNetworkType();
    }

    public int getMobileDataRegState() {
        return this.mServiceState.getMobileDataRegState();
    }

    public static int rilRadioTechnologyToNetworkType(int i) {
        return ServiceState.rilRadioTechnologyToNetworkType(i);
    }

    public int getSnapshotStatus() {
        return this.mServiceState.getSnapshotStatus();
    }

    public int getVoiceNetworkType() {
        return this.mServiceState.getVoiceNetworkType();
    }

    public boolean isPsOnlyReg() {
        return this.mServiceState.isPsOnlyReg();
    }

    public int getVoiceRoamingType() {
        return this.mServiceState.getVoiceRoamingType();
    }

    public int getNrFrequencyRange() {
        return this.mServiceState.getNrFrequencyRange();
    }
}
