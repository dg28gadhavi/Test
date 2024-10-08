package com.sec.internal.constants.ims.os;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import java.util.Objects;

public class NetworkEvent {
    private static final String LOG_TAG = "NetworkEvent";
    public boolean csOutOfService;
    public boolean isDataRoaming;
    public boolean isDataStateConnected;
    public boolean isEpdgAvailable;
    public boolean isEpdgConnected;
    public boolean isPsOnlyReg;
    public boolean isVoiceRoaming;
    public boolean isVopsUpdated;
    public boolean isWifiConnected;
    public int network;
    public String operatorNumeric;
    public boolean outOfService;
    public int voiceNetwork;
    public VoPsIndication voiceOverPs;

    public enum VopsState {
        KEEP,
        ENABLED,
        DISABLED
    }

    public static int blurNetworkType(int i) {
        if (!(i == 1 || i == 2)) {
            if (i == 15) {
                return 10;
            }
            if (i != 16) {
                switch (i) {
                    case 8:
                    case 9:
                    case 10:
                        return 10;
                    default:
                        return i;
                }
            }
        }
        return 16;
    }

    public NetworkEvent() {
        this.network = 0;
        this.voiceNetwork = 0;
        this.outOfService = true;
        this.csOutOfService = true;
        this.isDataRoaming = false;
        this.isVoiceRoaming = false;
        this.voiceOverPs = VoPsIndication.UNKNOWN;
        this.isWifiConnected = false;
        this.isEpdgConnected = false;
        this.isEpdgAvailable = false;
        this.operatorNumeric = "";
        this.isPsOnlyReg = false;
        this.isDataStateConnected = false;
    }

    public NetworkEvent(NetworkEvent networkEvent) {
        this.network = networkEvent.network;
        this.voiceNetwork = networkEvent.voiceNetwork;
        this.voiceOverPs = networkEvent.voiceOverPs;
        this.outOfService = networkEvent.outOfService;
        this.csOutOfService = networkEvent.csOutOfService;
        this.isDataRoaming = networkEvent.isDataRoaming;
        this.isDataStateConnected = networkEvent.isDataStateConnected;
        this.isVoiceRoaming = networkEvent.isVoiceRoaming;
        this.isWifiConnected = networkEvent.isWifiConnected;
        this.isEpdgConnected = networkEvent.isEpdgConnected;
        this.isEpdgAvailable = networkEvent.isEpdgAvailable;
        this.operatorNumeric = networkEvent.operatorNumeric;
        this.isPsOnlyReg = networkEvent.isPsOnlyReg;
        this.isVopsUpdated = networkEvent.isVopsUpdated;
    }

    public NetworkEvent(int i, int i2, boolean z, boolean z2, boolean z3, boolean z4, VoPsIndication voPsIndication, boolean z5, boolean z6, boolean z7, String str, boolean z8, boolean z9) {
        this.network = i;
        this.voiceNetwork = i2;
        this.outOfService = z;
        this.isDataRoaming = z3;
        this.isVoiceRoaming = z4;
        this.voiceOverPs = voPsIndication;
        this.csOutOfService = z2;
        this.isWifiConnected = z5;
        this.isEpdgConnected = z6;
        this.isEpdgAvailable = z7;
        this.operatorNumeric = str;
        this.isPsOnlyReg = z8;
        this.isDataStateConnected = z9;
    }

    public NetworkEvent(int i) {
        this(i, false, false, false, false, VoPsIndication.SUPPORTED, false, false, "00101");
    }

    public NetworkEvent(int i, boolean z, boolean z2, boolean z3, boolean z4, VoPsIndication voPsIndication, boolean z5, boolean z6, String str) {
        this(i, i, z, z2, z3, z4, voPsIndication, z5, false, z6, str, false, false);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.network), Integer.valueOf(this.voiceNetwork), this.voiceOverPs, Boolean.valueOf(this.outOfService), Boolean.valueOf(this.isDataRoaming), Boolean.valueOf(this.isDataStateConnected), Boolean.valueOf(this.isVoiceRoaming), Boolean.valueOf(this.csOutOfService), Boolean.valueOf(this.isWifiConnected), Boolean.valueOf(this.isEpdgConnected), Boolean.valueOf(this.isEpdgAvailable), this.operatorNumeric, Boolean.valueOf(this.isPsOnlyReg), Boolean.valueOf(this.isVopsUpdated)});
    }

    public boolean equals(Object obj) {
        return equalsInternal(obj, false);
    }

    public boolean equalsIgnoreEpdg(Object obj) {
        return equalsInternal(obj, true);
    }

    private boolean equalsInternal(Object obj, boolean z) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof NetworkEvent)) {
            return false;
        }
        NetworkEvent networkEvent = (NetworkEvent) obj;
        if (blurNetworkType(this.network) != blurNetworkType(networkEvent.network) || this.voiceNetwork != networkEvent.voiceNetwork || this.isDataRoaming != networkEvent.isDataRoaming || this.isVoiceRoaming != networkEvent.isVoiceRoaming || this.outOfService != networkEvent.outOfService || this.voiceOverPs != networkEvent.voiceOverPs || this.csOutOfService != networkEvent.csOutOfService || this.isWifiConnected != networkEvent.isWifiConnected) {
            return false;
        }
        if ((!z && (this.isEpdgConnected != networkEvent.isEpdgConnected || this.isEpdgAvailable != networkEvent.isEpdgAvailable)) || this.isPsOnlyReg != networkEvent.isPsOnlyReg) {
            return false;
        }
        if (!TextUtils.isEmpty(this.operatorNumeric) && !TextUtils.equals(this.operatorNumeric, networkEvent.operatorNumeric)) {
            return false;
        }
        if (this.isDataStateConnected == networkEvent.isDataStateConnected) {
            return true;
        }
        return false;
    }

    public String changedEvent(NetworkEvent networkEvent) {
        String str = "Changed Event: ";
        if (this.isDataRoaming != networkEvent.isDataRoaming) {
            str = str + "DataRoaming(" + this.isDataRoaming + "=>" + networkEvent.isDataRoaming + "), ";
        }
        if (this.isVoiceRoaming != networkEvent.isVoiceRoaming) {
            str = str + "VoiceRoaming(" + this.isVoiceRoaming + "=>" + networkEvent.isVoiceRoaming + "), ";
        }
        if (blurNetworkType(this.network) != blurNetworkType(networkEvent.network)) {
            str = str + "Network type(" + this.network + "=>" + networkEvent.network + "), ";
        }
        if (this.voiceNetwork != networkEvent.voiceNetwork) {
            str = str + "Voice network(" + this.voiceNetwork + "=>" + networkEvent.voiceNetwork + "), ";
        }
        if (this.outOfService != networkEvent.outOfService) {
            str = str + "OoS(" + this.outOfService + "=>" + networkEvent.outOfService + "), ";
        }
        if (this.voiceOverPs != networkEvent.voiceOverPs) {
            str = str + "VoPS(" + this.voiceOverPs + "=>" + networkEvent.voiceOverPs + "), ";
        }
        if (this.csOutOfService != networkEvent.csOutOfService) {
            str = str + "CS_OoS(" + this.csOutOfService + "=>" + networkEvent.csOutOfService + "), ";
        }
        if (this.isWifiConnected != networkEvent.isWifiConnected) {
            str = str + "isWifiConnected(" + this.isWifiConnected + "=> " + networkEvent.isWifiConnected + "), ";
        }
        if (this.isPsOnlyReg != networkEvent.isPsOnlyReg) {
            str = str + "isPsOnlyReg(" + this.isPsOnlyReg + "=>" + networkEvent.isPsOnlyReg + "), ";
        }
        if (this.isDataStateConnected != networkEvent.isDataStateConnected) {
            str = str + "isDataConnected(" + this.isDataStateConnected + "=>" + networkEvent.isDataStateConnected + "), ";
        }
        if (!TextUtils.equals(this.operatorNumeric, networkEvent.operatorNumeric)) {
            str = str + "Operator(" + this.operatorNumeric + "=>" + networkEvent.operatorNumeric + "), ";
        }
        return str.replaceAll(", $", "");
    }

    public String toString() {
        return "NetworkEvent [network=" + this.network + ", voiceNetwork=" + this.voiceNetwork + ", voiceOverPs=" + this.voiceOverPs + ", outOfService=" + this.outOfService + ", isDataRoaming=" + this.isDataRoaming + ", isVoiceRoaming=" + this.isVoiceRoaming + ", csOutOfService=" + this.csOutOfService + ", isWifiConnected=" + this.isWifiConnected + ", isEpdgConnected=" + this.isEpdgConnected + ", isEpdgAvailable=" + this.isEpdgAvailable + ", operatorNumeric=" + this.operatorNumeric + ", isPsOnlyReg=" + this.isPsOnlyReg + ", isDataConnected=" + this.isDataStateConnected + "]";
    }

    public VopsState isVopsUpdated(NetworkEvent networkEvent) {
        VoPsIndication voPsIndication;
        if (!NetworkUtil.is3gppPsVoiceNetwork(this.network) || !NetworkUtil.is3gppPsVoiceNetwork(networkEvent.network) || this.network != networkEvent.network || (voPsIndication = this.voiceOverPs) == networkEvent.voiceOverPs || voPsIndication == VoPsIndication.UNKNOWN) {
            return VopsState.KEEP;
        }
        VopsState vopsState = voPsIndication == VoPsIndication.SUPPORTED ? VopsState.ENABLED : VopsState.DISABLED;
        Log.d(LOG_TAG, "VoPS changed. enabled = " + vopsState);
        return vopsState;
    }

    public boolean isEpdgHOEvent(NetworkEvent networkEvent) {
        int i;
        int i2 = this.network;
        if ((i2 == 13 || i2 == 14) && networkEvent.network == 18 && networkEvent.isEpdgConnected) {
            Log.d(LOG_TAG, "isEpdgHOEvent: From IWLAN to LTE.");
            return true;
        } else if (i2 == 18 && ((i = networkEvent.network) == 13 || i == 14)) {
            Log.d(LOG_TAG, "isEpdgHOEvent: From LTE to IWLAN.");
            return true;
        } else if (i2 != networkEvent.network || this.isWifiConnected == networkEvent.isWifiConnected) {
            return false;
        } else {
            Log.d(LOG_TAG, "isEpdgHOEvent: Only wifi connection is changed.");
            return true;
        }
    }

    public static NetworkEvent buildNetworkEvent(int i, int i2, int i3, int i4, boolean z, boolean z2, boolean z3, NetworkEvent networkEvent, NetworkState networkState) {
        boolean z4;
        int i5;
        boolean z5 = true;
        boolean z6 = i2 == 0 || networkState.getDataRegState() != 0;
        boolean z7 = networkState.getVoiceRegState() != 0;
        int voiceNetworkType = networkState.getVoiceNetworkType();
        boolean isDataRoaming2 = networkState.isDataRoaming();
        boolean isDataConnectedState = networkState.isDataConnectedState();
        boolean isVoiceRoaming2 = networkState.isVoiceRoaming();
        String operatorNumeric2 = networkState.getOperatorNumeric();
        VoPsIndication vopsIndication = networkState.getVopsIndication();
        boolean isPsOnlyReg2 = networkState.isPsOnlyReg();
        int blurNetworkType = blurNetworkType(i2);
        if (SimUtil.getSimMno(i) != Mno.TMOUS || !is2GNetworkInCall(blurNetworkType, z6, z7, i3, i4)) {
            z4 = z6;
            i5 = blurNetworkType;
        } else {
            z4 = false;
            i5 = blurNetworkType(i3);
        }
        NetworkEvent networkEvent2 = new NetworkEvent(i5, voiceNetworkType, z4, z7, isDataRoaming2, isVoiceRoaming2, vopsIndication, z, z2, z3, operatorNumeric2, isPsOnlyReg2, isDataConnectedState);
        if (networkEvent2.isVopsUpdated(networkEvent) == VopsState.KEEP) {
            z5 = false;
        }
        networkEvent2.isVopsUpdated = z5;
        return networkEvent2;
    }

    private static boolean is2GNetworkInCall(int i, boolean z, boolean z2, int i2, int i3) {
        return i == 0 && z && !z2 && TelephonyManagerExt.getNetworkClass(i2) == 1 && i3 != 0;
    }
}
